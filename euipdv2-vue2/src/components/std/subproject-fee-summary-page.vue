<!--
 * Author: 周朔鹏
 * Date: 2025-02-26
 * Email: 391902958@qq.com
 * Website: https://www.euipd.com
-->

<template>
  <div style="height: 100%;">
    <div class="paper-height">
      <div style="height: 100%; overflow: scroll;">
        <div class="a4-paper-big-simple">
          <div style="margin: 50px 60px;">
            <div style="margin: 0 0 10px; color: #909399; font-size: 12px; line-height: 1.5;">
              口径：与预算执行表一致，仅展示开发阶段相关记账流水（功能/任务/物料/费用/子项目等）；合计为全量金额。
            </div>
            <div style="display: flex; justify-content: space-between; align-items: center; margin-right: 20px; margin-bottom: 8px;">
              <div style="font-size: 13px; color: #606266;">
                全量合计：{{ totalAmountYuan | rmbFormat }} 元
                <span style="margin-left: 12px; color: #909399;">（共 {{ totalCount }} 条）</span>
              </div>
              <pagination :key="((subprojectId) + '-' + (projectType))" ref="pagination" v-bind:list="list" v-bind:itemCount="6"></pagination>
            </div>
            <div>
              <el-table
                  v-loading="loading"
                  :data="feeSummaryList"
                  row-key="id"
                  show-summary
                  :summary-method="getSummaries"
                  border
                  @row-click="rowClick">
                <el-table-column
                    prop="serialNo"
                    align="center"
                    show-overflow-tooltip
                    min-width="120"
                    label="费用编号">
                </el-table-column>
                <el-table-column
                    prop="name"
                    show-overflow-tooltip
                    min-width="120"
                    align="center"
                    label="支出名称">
                </el-table-column>
                <el-table-column
                    prop="classify"
                    :formatter="classifyFormat"
                    show-overflow-tooltip
                    width="120"
                    align="center"
                    label="费用科目">
                </el-table-column>
                <el-table-column
                    prop="capitalSource"
                    :formatter="capitalFormat"
                    align="center"
                    width="80"
                    label="经费来源">
                </el-table-column>
                <el-table-column
                    prop="amount"
                    align="center"
                    width="110"
                    label="数额(元)">
                  <template slot-scope="scope">
                    {{ numOrZero(scope.row.amount) | rmbFormat }}
                  </template>
                </el-table-column>
                <el-table-column
                    prop="paymentStatue"
                    :formatter="paymentStatueFormat"
                    align="center"
                    width="80"
                    label="支付状态">
                </el-table-column>
                <el-table-column
                    prop="createTime"
                    :formatter="creatTimeFormat"
                    align="center"
                    width="110"
                    label="记录时间">
                </el-table-column>
              </el-table>
            </div>
          </div>
        </div>
      </div>
    </div>
    <!--支付状态编辑-->
    <el-dialog
        width="29cm"
        :visible.sync="feeItemEditDlg"
        :before-close="handleClose"
        append-to-body
        :close-on-click-modal=false>
      <div slot="title" style="font-size: 20px;">{{ dialogTitle }}</div>
      <div v-if="feeItemEditDlg">
        <FeePaymentEditDlg
            :key="feeItem.id"
            :fee-item="feeItem"
            :close-dlg="closeDialog"
        ></FeePaymentEditDlg>
      </div>
    </el-dialog>

  </div>
</template>

<script>
import Swal from "sweetalert2";
import CharacterDocInfoPage from "@/components/std/character-doc-info-page.vue";
import Pagination from "@/components/pagination.vue";
import {optionKV, shortDateFormat, rmbFormat} from "@/filter/filter";
import FeePaymentEditDlg from "@/components/rdms/fee-payment-edit-dlg.vue";

export default {
  components: {
    Pagination,
    CharacterDocInfoPage,
    FeePaymentEditDlg,
  },
  name: "subproject-fee-summary-page",

  props: {
    subprojectId: {
      default: null,
    },
    projectType: {
      default: PROJECT_TYPE.SUB_PROJECT.key,
    },
  },

  computed: {
    totalAmountYuan() {
      return this.numOrZero(this.sumAmount);
    },
  },

  data() {
    return {
      loginUser: {},
      loginCustomer: {},
      feeSummaryList: [],
      feeItemEditDlg: false,
      dialogTitle: '',
      feeItem: {},
      loading: false,
      sumAmount: 0,
      totalCount: 0,

      currentPage: 1,
    };
  },

  mounted: function () {
    let _this = this;
    _this.getLoginUser();
    _this.$refs.pagination.size = 24;
    if (Tool.isNotEmpty(_this.subprojectId)) {
      _this.list(1);
    }

    _this.$bus.$on('refresh-feeItem-edit-dlg', (data) => {
      if(data){
        _this.list(_this.currentPage);
      }
    });
  },

  beforeDestroy(){
    let _this = this;
    _this.$bus.$off('refresh-feeItem-edit-dlg');
  },

  methods: {
    numOrZero(v) {
      const n = Number(v);
      return Number.isFinite(n) ? n : 0;
    },

    getLoginUser() {
      let _this = this;
      if (Tool.getLoginUser().userGroup === LOGIN_USER_GROUP.CUSTOMER.key) {
        _this.loginUser = Tool.getLoginUser().adminUser;
        _this.loginCustomer = Tool.getLoginUser();
      } else {
        _this.loginUser = Tool.getLoginUser();
        _this.loginCustomer = Tool.getLoginUser().customer;
      }
    },

    classifyFormat(row){
      return optionKV(BUDGET_CLASSIFY_STD, row.classify);
    },
    capitalFormat(row){
      return optionKV(CAPITAL_SOURCE, row.capitalSource);
    },
    paymentStatueFormat(row){
      return optionKV(PAYMENT_STATUS, row.paymentStatue);
    },

    getSummaries(param) {
      const { columns, data } = param;
      const sums = [];
      let pageSum = 0;
      (data || []).forEach((row) => {
        pageSum += this.numOrZero(row.amount);
      });
      columns.forEach((column, index) => {
        if (index === 0) {
          sums[index] = '本页合计';
        } else if (column.property === 'amount') {
          sums[index] = rmbFormat(pageSum);
        } else {
          sums[index] = '';
        }
      });
      return sums;
    },

    list(page){
      let _this = this;
      _this.currentPage = page;
      if(_this.projectType === PROJECT_TYPE.SUB_PROJECT.key){
        _this.listByKeywordIsSubprojectId(page);
      }else{
        _this.listByKeywordIsProjectId(page);
      }
    },

    applyPageResult(respContent) {
      const content = respContent || {};
      this.feeSummaryList = content.list || [];
      this.sumAmount = content.sumAmount != null ? content.sumAmount : 0;
      this.totalCount = content.total != null ? content.total : 0;
      this.$refs.pagination.render(this.currentPage, this.totalCount);
    },

    listByKeywordIsSubprojectId(page) {
      let _this = this;
      _this.loading = true;
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/budget-direct-exe/listByKeywordIsSubprojectId', {
        page: page,
        size: _this.$refs.pagination.size,
        keyWord: _this.subprojectId,
      }).then((response) => {
        let resp = response.data;
        if (resp.success) {
          _this.applyPageResult(resp.content);
        } else {
          Toast.warning(resp.message, Swal);
        }
      }).catch((e) => {
        console.error(e);
        Toast.error('费用清单加载失败', Swal);
      }).finally(() => {
        _this.loading = false;
      });
    },

    listByKeywordIsProjectId(page) {
      let _this = this;
      _this.loading = true;
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/budget-direct-exe/listByKeywordIsProjectId', {
        page: page,
        size: _this.$refs.pagination.size,
        keyWord: _this.subprojectId,
      }).then((response) => {
        let resp = response.data;
        if (resp.success) {
          _this.applyPageResult(resp.content);
        } else {
          Toast.warning(resp.message, Swal);
        }
      }).catch((e) => {
        console.error(e);
        Toast.error('费用清单加载失败', Swal);
      }).finally(() => {
        _this.loading = false;
      });
    },

    creatTimeFormat(row, column){
      return shortDateFormat(row.createTime);
    },

    hasFullAuthority() {
      return typeof RoleAccess !== 'undefined'
        && RoleAccess.hasAny(['UltiMng'], Tool.getLoginUser().roleList);
    },

    rowClick(row, column, e){
      let _this = this;
      // 只有手工录入（payClassify=SUB_PROJECT）的应付未付流水可由最终管理人改为已支付
      if(row.paymentStatue === PAYMENT_STATUS.UNPAID.key
          && row.payClassify === 'SUB_PROJECT'
          && _this.hasFullAuthority()){
        _this.feeItem = row;
        _this.feeItemEditDlg = true;
        _this.dialogTitle = "编辑支付状态";
      }
    },

    handleClose(done) {
      let _this = this;
      _this.feeItemEditDlg = false;
      done();
    },

    closeDialog() {
      let _this = this;
      _this.feeItemEditDlg = false;
    },

  },
}
</script>

<style lang="scss" scoped>
@import './../../../public/static/scss/caipd';

.paper-height {
  height: calc(100vh - 0px);
}

</style>
