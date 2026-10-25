<!--
 * Author: 周朔鹏
 * Date: 2024-05-26
 * Email: 391902958@qq.com
 * Website: https://www.euipd.com
-->

<template>
  <div style="height: 100%;">
    <div class="paper-height">
      <div style="height: 100%; overflow: scroll;">
        <div class="a4-paper-big-simple" style="width: 30cm;">
          <div style="margin: 50px 60px;">
            <div>
              <div class="ca-form-header">
                <div style="font-size: 14px; font-weight: bold; margin-top: 20px;">
                  费用化物料汇总表:
                </div>
                <div class="hr hr-6 dotted"></div>
              </div>
              <el-table
                  :data="expenditureMaterialList"
                  stripe
                  style="width: 100%">
                <el-table-column   label="序号" align="center" width="50">
                  <template slot-scope="scope">
                    {{scope.$index + 1}}
                  </template>
                </el-table-column>
                <el-table-column
                    prop="code"
                    align="center"
                    show-overflow-tooltip
                    label="申请单号">
                </el-table-column>
                <el-table-column
                    prop="bomCode"
                    align="center"
                    show-overflow-tooltip
                    label="物料编码">
                </el-table-column>
                <el-table-column
                    prop="name"
                    align="center"
                    show-overflow-tooltip
                    label="名称">
                </el-table-column>
                <el-table-column
                    prop="model"
                    show-overflow-tooltip
                    align="center"
                    label="型号">
                </el-table-column>
                <el-table-column
                    prop="unit"
                    sortable
                    show-overflow-tooltip
                    width="90"
                    align="center"
                    label="单位">
                </el-table-column>
                <el-table-column
                    prop="amount"
                    sortable
                    show-overflow-tooltip
                    width="90"
                    align="center"
                    label="数量">
                </el-table-column>
                <el-table-column
                    prop="status"
                    :formatter="statusFormat"
                    width="90"
                    sortable
                    show-overflow-tooltip
                    align="center"
                    label="状态">
                </el-table-column>
                <el-table-column
                    prop="writerName"
                    width="90"
                    sortable
                    show-overflow-tooltip
                    align="center"
                    label="申领人">
                </el-table-column>
              </el-table>
            </div>

            <div>
              <div class="ca-form-header">
                <div style="font-size: 14px; font-weight: bold; margin-top: 20px;">
                  资产化物料汇总表:
                </div>
                <div class="hr hr-6 dotted"></div>
              </div>
              <el-table
                  :data="capitalizationMaterialList"
                  stripe
                  style="width: 100%">
                <el-table-column   label="序号" align="center" width="50">
                  <template slot-scope="scope">
                    {{scope.$index + 1}}
                  </template>
                </el-table-column>
                <el-table-column
                    prop="code"
                    align="center"
                    show-overflow-tooltip
                    label="申请单号">
                </el-table-column>
                <el-table-column
                    prop="bomCode"
                    align="center"
                    show-overflow-tooltip
                    label="物料编码">
                </el-table-column>
                <el-table-column
                    prop="name"
                    align="center"
                    show-overflow-tooltip
                    label="名称">
                </el-table-column>
                <el-table-column
                    prop="model"
                    show-overflow-tooltip
                    align="center"
                    label="型号">
                </el-table-column>
                <el-table-column
                    prop="unit"
                    width="90"
                    sortable
                    show-overflow-tooltip
                    align="center"
                    label="单位">
                </el-table-column>
                <el-table-column
                    prop="amount"
                    width="90"
                    sortable
                    show-overflow-tooltip
                    align="center"
                    label="数量">
                </el-table-column>
                <el-table-column
                    prop="status"
                    :formatter="statusFormat"
                    sortable
                    width="90"
                    show-overflow-tooltip
                    align="center"
                    label="状态">
                </el-table-column>
                <el-table-column
                    prop="writerName"
                    sortable
                    width="90"
                    show-overflow-tooltip
                    align="center"
                    label="申领人">
                </el-table-column>
              </el-table>
            </div>

           <div>
              <div class="ca-form-header">
                <div style="font-size: 14px; font-weight: bold; margin-top: 20px;">
                  借用物料汇总表:
                </div>
                <div class="hr hr-6 dotted"></div>
              </div>
              <el-table
                  :data="borrowMaterialList"
                  stripe
                  style="width: 100%">
                <el-table-column   label="序号" align="center" width="50">
                  <template slot-scope="scope">
                    {{scope.$index + 1}}
                  </template>
                </el-table-column>
                <el-table-column
                    prop="code"
                    align="center"
                    show-overflow-tooltip
                    label="申请单号">
                </el-table-column>
                <el-table-column
                    prop="bomCode"
                    align="center"
                    show-overflow-tooltip
                    label="物料编码">
                </el-table-column>
                <el-table-column
                    prop="name"
                    align="center"
                    show-overflow-tooltip
                    label="名称">
                </el-table-column>
                <el-table-column
                    prop="model"
                    show-overflow-tooltip
                    align="center"
                    label="型号">
                </el-table-column>
                <el-table-column
                    prop="unit"
                    width="90"
                    sortable
                    show-overflow-tooltip
                    align="center"
                    label="单位">
                </el-table-column>
                <el-table-column
                    prop="amount"
                    width="90"
                    sortable
                    show-overflow-tooltip
                    align="center"
                    label="数量">
                </el-table-column>
                <el-table-column
                    prop="status"
                    :formatter="statusFormat"
                    sortable
                    width="90"
                    show-overflow-tooltip
                    align="center"
                    label="状态">
                </el-table-column>
                <el-table-column
                    prop="writerName"
                    sortable
                    width="90"
                    show-overflow-tooltip
                    align="center"
                    label="申领人">
                </el-table-column>
              </el-table>
            </div>
            <div class="hr hr-10 hr-dotted hr-double"></div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import Swal from "sweetalert2";
import JobitemDetailInfoDlg from "@/components/std/jobitem-detail-info-dlg.vue";
import {optionKV, rmbFormat} from "@/filter/filter";
import ApproveSignatureCore from "@/components/rdms/approve-signature-core.vue";
import FileSaver from 'file-saver';

export default {
  components: {
    ApproveSignatureCore,
    JobitemDetailInfoDlg,

  },
  name: "material-summary-list-page",

  props: {
    characterId: {
      default: null,
    },
    jobType: {
      default: null,
    },
    projectType: {
      default: null,
    },
    index: {
      default: null,
    },
    entrance: {
      default: null,
    },
  },

  data() {
    return {
      loginUser: {},
      loginCustomer: {},
      capitalizationMaterialList: [],
      expenditureMaterialList: [],
      borrowMaterialList: [],

      showJobitemProcessDlg: false,
      dialogTitle: '',

      APPLICATION_STATUS: APPLICATION_STATUS,
    };
  },

  computed: {
  },

  mounted: function () {
    let _this = this;
    _this.getLoginUser();
    if (Tool.isNotEmpty(_this.characterId)) {
      _this.getCapitalizationSummaryListByCharacterId(_this.characterId);
      _this.getExpenditureSummaryListByCharacterId(_this.characterId);
      _this.getBorrowSummaryListByCharacterId(_this.characterId);
    }
  },

  methods: {
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

    statusFormat(row, column) {
      return optionKV(APPLICATION_STATUS, row.status);
    },

    itemBudgetFormat(row, column) {
      if (row.itemBudget) {
        return rmbFormat(row.itemBudget);
      }
      return 0;
    },

    activeChargeFormat(row, column) {
      if (row.activeCharge) {
        return rmbFormat(row.activeCharge);
      }
      return 0;
    },

    budgetRateFormat(row, column) {
      if (row.budgetRate) {
        return (row.budgetRate * 100).toFixed(2);  //保留两位小数
      }
      return 0;
    },

    getCapitalizationSummaryListByCharacterId(characterId) {
      let _this = this;
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/material/getCapitalizationSummaryListByCharacterId/' + characterId).then((response) => {
        let resp = response.data;
        if (resp.success) {
          _this.capitalizationMaterialList = resp.content;
        } else {
          Toast.warning(resp.message, Swal);
        }
      });
    },

    getExpenditureSummaryListByCharacterId(characterId) {
      let _this = this;
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/material/getExpenditureSummaryListByCharacterId/' + characterId).then((response) => {
        let resp = response.data;
        if (resp.success) {
          _this.expenditureMaterialList = resp.content;
        } else {
          Toast.warning(resp.message, Swal);
        }
      });
    },

    getBorrowSummaryListByCharacterId(characterId) {
      let _this = this;
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/material/getBorrowSummaryListByCharacterId/' + characterId).then((response) => {
        let resp = response.data;
        if (resp.success) {
          _this.borrowMaterialList = resp.content;
        } else {
          Toast.warning(resp.message, Swal);
        }
      });
    },

    showJobitemProcess() {
      let _this = this;
      _this.showJobitemProcessDlg = true;
      _this.dialogTitle = "任务处理过程";
    },
    handleClose(done) {
      let _this = this;
      _this.showJobitemProcessDlg = false;
      done();
    },

    closeDialog() {
      let _this = this;
      _this.showJobitemProcessDlg = false;
    },
  },
}
</script>

<style lang="scss" scoped>
@import './../../../public/static/scss/caipd';

.paper-height {
  height: calc(100vh - 200px);
}

</style>
