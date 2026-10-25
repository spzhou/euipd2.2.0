<!--
 * Author: 周朔鹏
 * Date: 2024-12-26
 * Email: 391902958@qq.com
 * Website: https://www.euipd.com
-->

<template>
  <div class="paper-height">
    <div id="scrollshow" style="height: 100%; overflow: scroll;">
      <div class="a4-paper-big-plain" style="width: 100%;">
        <div ref="printRef">
          <el-table
              :data="budgetExeSummary.statisticsItemDtoList"
              style="width: 100%;margin-bottom: 20px;"
              row-key="id"
              show-summary
              border>
            <el-table-column
                align="center"
                label="项目资金支出情况审计表">
              <el-table-column
                  prop="classify"
                  align="center"
                  width="80"
                  label="编号">
              </el-table-column>
              <el-table-column
                  prop="classifyName"
                  align="center"
                  width="150"
                  label="科目">
              </el-table-column>
              <el-table-column
                  align="center"
                  label="批准预算数(元)">
                <el-table-column
                    prop="approvedBudgetBt"
                    align="center"
                    label="国拨资金">
                </el-table-column>
                <el-table-column
                    prop="approvedBudgetZc"
                    align="center"
                    label="自筹资金">
                </el-table-column>
                <el-table-column
                    prop="approvedBudget"
                    align="center"
                    label="合计">
                </el-table-column>
              </el-table-column>
              <el-table-column
                  align="center"
                  label="调整后预算数(元)">
                <el-table-column
                    prop="budgetBt"
                    align="center"
                    label="国拨资金">
                </el-table-column>
                <el-table-column
                    prop="budgetZc"
                    align="center"
                    label="自筹资金">
                </el-table-column>
                <el-table-column
                    prop="budget"
                    align="center"
                    label="合计">
                </el-table-column>
              </el-table-column>
              <el-table-column
                  align="center"
                  label="账面支出数(元)">
                <el-table-column
                    prop="budgetExeBt"
                    align="center"
                    label="国拨资金">
                </el-table-column>
                <el-table-column
                    prop="budgetExeZc"
                    align="center"
                    label="自筹资金">
                </el-table-column>
                <el-table-column
                    prop="budgetExe"
                    align="center"
                    label="合计">
                </el-table-column>
              </el-table-column>
              <el-table-column
                  align="center"
                  label="应付未付数(元)">
                <el-table-column
                    prop="budgetOngoingBt"
                    align="center"
                    label="国拨资金">
                </el-table-column>
                <el-table-column
                    prop="budgetOngoingZc"
                    align="center"
                    label="自筹资金">
                </el-table-column>
                <el-table-column
                    prop="budgetOngoing"
                    align="center"
                    label="合计">
                </el-table-column>
              </el-table-column>
              <el-table-column
                  align="center"
                  label="结余数(元)">
                <el-table-column
                    prop="budgetResidueBt"
                    align="center"
                    label="国拨资金">
                </el-table-column>
                <el-table-column
                    prop="budgetResidueZc"
                    align="center"
                    label="自筹资金">
                </el-table-column>
                <el-table-column
                    prop="budgetResidue"
                    align="center"
                    label="合计">
                </el-table-column>
              </el-table-column>
            </el-table-column>
          </el-table>
        </div>
        <div id="no-print" class="no-print">
          <div style="display: flex; flex-direction: row; justify-content: end; margin-top: 30px;padding-bottom: 20px; margin-right: 30px;">
            <el-button @click="toPrint()">打印</el-button>
          </div>
        </div>
      </div>
    </div>
  </div>

</template>

<script>
import Swal from "sweetalert2";
import BudgetApplicationDlg from "@/components/rdms/budget-application-dlg.vue";
import { Loading } from 'element-ui';
import {rmbFormat} from "@/filter/filter";

export default {
  components: {BudgetApplicationDlg},
  name: "budget-stage-statistics-page",

  props: {
    customerId: {
      default: null,
    },
    stage: {
      default: null,
    },
  },

  computed: {},

  data() {
    return {
      loginUser: {},
      loginCustomer: {},

      budgetExeSummary: [],
    };
  },

  mounted: function () {
    let _this = this;
    _this.getLoginUser();
    if (Tool.isNotEmpty(_this.customerId) && Tool.isNotEmpty(_this.stage)) {
      if(_this.stage === BUDGET_TYPE.PRE_PROJECT.key){
        _this.getBudgetStageExecutionSummary_preProject(_this.customerId);
      }
      if(_this.stage === BUDGET_TYPE.PROJECT.key){
        _this.getBudgetStageExecutionSummary_project(_this.customerId);
      }
      if(_this.stage === BUDGET_TYPE.PRODUCT.key){
        _this.getBudgetStageExecutionSummary_product(_this.customerId);
      }
      if(_this.stage === 'CUSTOMER'){
        _this.getBudgetExecutionSummary_customer(_this.customerId);
      }

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

    rmbFormat_1(row, column) {
      if (row.budgetResidue) {
        return rmbFormat(row.budgetResidue);
      }
      return 0;
    },

    getBudgetStageExecutionSummary_preProject(customerId) {
      let _this = this;
      let loadingInstance = Loading.service({ fullscreen: true });
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/budget-direct-exe/getBudgetStageExecutionSummary_preProject/' + customerId).then((response)=>{
        let resp = response.data;
        loadingInstance.close();
        if (resp.success) {
          _this.budgetExeSummary = resp.content;
        }else{
          Toast.warning(resp.message, Swal);
        }
      })
    },

    getBudgetStageExecutionSummary_product(customerId) {
      let _this = this;
      let loadingInstance = Loading.service({ fullscreen: true });
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/budget-direct-exe/getBudgetStageExecutionSummary_product/' + customerId).then((response)=>{
        let resp = response.data;
        loadingInstance.close();
        if (resp.success) {
          _this.budgetExeSummary = resp.content;
        }else{
          Toast.warning(resp.message, Swal);
        }
      })
    },

    getBudgetStageExecutionSummary_project(customerId) {
      let _this = this;
      let loadingInstance = Loading.service({ fullscreen: true });
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/budget-direct-exe/getBudgetStageExecutionSummary_project/' + customerId).then((response)=>{
        let resp = response.data;
        loadingInstance.close();
        if (resp.success) {
          _this.budgetExeSummary = resp.content;
        }else{
          Toast.warning(resp.message, Swal);
        }
      })
    },

    getBudgetExecutionSummary_customer(customerId) {
      let _this = this;
      let loadingInstance = Loading.service({ fullscreen: true });
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/budget-direct-exe/getBudgetExecutionSummary_customer/' + customerId).then((response)=>{
        let resp = response.data;
        loadingInstance.close();
        if (resp.success) {
          _this.budgetExeSummary = resp.content;
        }else{
          Toast.warning(resp.message, Swal);
        }
      })
    },

    toPrint () {
      let _this = this;
      _this.$print(this.$refs.printRef);
    },
  },
}
</script>

<!--<style>
#scrollshow::-webkit-scrollbar {
  width: 10px !important;
}
#scrollshow::-webkit-scrollbar-thumb{
  background-color: #e4393c !important;
}
</style>-->

<style lang="scss" scoped>
@import './../../../public/static/scss/caipd';

.paper-height {
  height: 900px;
}

</style>
