<!--
 * Author: 周朔鹏
 * Date: 2024-12-23
 * Email: 391902958@qq.com
 * Website: https://www.euipd.com
-->

<template>
  <div class="paper-height">
    <div id="scrollshow" style="height: 100%; overflow: scroll;">
      <div class="a4-paper-big-plain" style="width: 100%;">
        <div ref="printRef">
          <div style="font-size: 14px; line-height: 28px; font-weight: bold; padding-left: 10px; margin-bottom: 5px;">
            <div v-if="projectType===PROJECT_TYPE.PROJECT.key" >
              <div style="display: flex; flex-direction: row; justify-content: space-between; align-items: center;">
                <div>
                  项目名称( 完整 ): {{ (budgetExeSummary && budgetExeSummary.projectName) || '-' }}
                </div>
                <div v-if="enButton" style="margin-right: 20px;">
                  <el-button type="primary" size="small">预算申请</el-button>
                </div>
              </div>
            </div>
            <div v-else-if="projectType===PROJECT_TYPE.PRODUCT.key">
              <div style="display: flex; flex-direction: row; justify-content: space-between; align-items: center;">
                <div>
                  项目名称( 产品 ): {{ (budgetExeSummary && budgetExeSummary.projectName) || '-' }}
                </div>
                <div v-if="enButton" style="margin-right: 20px;">
                  <el-button type="primary" plain size="small" v-on:click.stop="budgetApplication()">
                    预算申请
                  </el-button>
                </div>
              </div>
            </div>
            <div v-else>
              项目名称( 当前 ): {{ (budgetExeSummary && budgetExeSummary.projectName) || '-' }}
            </div>
          </div>
          <div style="padding: 0 10px 8px; color: #909399; font-size: 12px; line-height: 1.5;">
            <span v-if="projectType===PROJECT_TYPE.PROJECT.key">
              口径：批准取主项目根预算；调整后可用为全树子项目预算合计；支出为项目级流水（已支付/应付未付）。
            </span>
            <span v-else-if="projectType===PROJECT_TYPE.SUB_PROJECT.key">
              口径：计划与支出均按当前子项目统计；结余=调整后预算−账面支出−应付未付。
            </span>
          </div>
          <el-table
              v-loading="loading"
              :data="(budgetExeSummary && budgetExeSummary.statisticsItemDtoList) || []"
              style="width: 100%;margin-bottom: 20px;"
              row-key="classify"
              show-summary
              border>
            <el-table-column
                align="center"
                label="项目资金支出情况审计表">
<!--              <el-table-column label="序号" align="center" width="60">
                <template slot-scope="scope">
                  {{ scope.$index }}
                </template>
              </el-table-column>-->
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

    <!--申请预算-->
    <el-dialog
        width="29cm"
        append-to-body
        :visible.sync="budgetApplicationDlg"
        :before-close="handleClose"
        :close-on-click-modal=false>
      <div slot="title" style="font-size: 20px;">{{ dialogTitle }}</div>
      <div>
        <BudgetApplicationDlg
            :key="projectSimpleInfo.id"
            :project="projectSimpleInfo"
            :type="BUDGET_TYPE.PRODUCT.key"
            :close-function="closeDialog"
        ></BudgetApplicationDlg>
      </div>
    </el-dialog>
  </div>

</template>

<script>
import Swal from "sweetalert2";
import BudgetApplicationDlg from "@/components/rdms/budget-application-dlg.vue";

export default {
  components: {BudgetApplicationDlg},
  name: "budget-direct-statistics-page",

  props: {
    xProjectId: {
      default: null,
    },
    projectType: {
      default: null,
    },
    enButton: {
      default: false,
    }
  },

  computed: {},

  data() {
    return {
      loginUser: {},
      loginCustomer: {},

      projectSimpleInfo: {},
      budgetExeSummary: {
        projectName: '',
        statisticsItemDtoList: [],
      },
      loading: false,

      PROJECT_TYPE: PROJECT_TYPE,
      BUDGET_TYPE: BUDGET_TYPE,
      budgetApplicationDlg: false,
      dialogTitle: '',
    };
  },

  mounted: function () {
    let _this = this;
    _this.getLoginUser();
    if (Tool.isNotEmpty(_this.xProjectId) && Tool.isNotEmpty(_this.projectType)) {
      if(_this.projectType === PROJECT_TYPE.SUB_PROJECT.key){
        _this.getBudgetExecutionSummary(_this.xProjectId);
      }
      if(_this.projectType === PROJECT_TYPE.PRE_PROJECT.key){
        _this.getBudgetExecutionSummary_preproject(_this.xProjectId);
      }
      if(_this.projectType === PROJECT_TYPE.PROJECT.key){
        _this.getBudgetExecutionSummary_mainProject(_this.xProjectId);
      }
      if(_this.projectType === PROJECT_TYPE.PRODUCT.key){
        _this.getBudgetExecutionSummary_product(_this.xProjectId);
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

    budgetApplication() {
      let _this = this;
      _this.budgetApplicationDlg = true;
      _this.dialogTitle = "申请预算";
    },
    closeDialog() {
      let _this = this;
      _this.budgetApplicationDlg = false;
    },
    handleClose(done) {
      done();
    },

    applySummary(content) {
      this.budgetExeSummary = content || {
        projectName: '',
        statisticsItemDtoList: [],
      };
      if (!Array.isArray(this.budgetExeSummary.statisticsItemDtoList)) {
        this.$set(this.budgetExeSummary, 'statisticsItemDtoList', []);
      }
    },

    fetchSummary(url) {
      let _this = this;
      _this.loading = true;
      _this.$ajax.post(url).then((response) => {
        let resp = response.data;
        if (resp.success) {
          _this.applySummary(resp.content);
        } else {
          Toast.warning(resp.message || '预算执行表加载失败', Swal);
        }
      }).catch((e) => {
        console.error(e);
        Toast.error('预算执行表加载失败', Swal);
      }).finally(() => {
        _this.loading = false;
      });
    },

    getBudgetExecutionSummary(xProjectId) {
      this.fetchSummary(process.env.VUE_APP_SERVER + '/customer/budget-direct-exe/getBudgetExecutionSummary/' + xProjectId);
    },

    getBudgetExecutionSummary_preproject(xProjectId) {
      this.fetchSummary(process.env.VUE_APP_SERVER + '/customer/budget-direct-exe/getBudgetExecutionSummary_preproject/' + xProjectId);
    },

    getBudgetExecutionSummary_mainProject(xProjectId) {
      this.fetchSummary(process.env.VUE_APP_SERVER + '/customer/budget-direct-exe/getBudgetExecutionSummary_mainProject/' + xProjectId);
    },

    getBudgetExecutionSummary_product(xProjectId) {
      let _this = this;
      _this.loading = true;
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/budget-direct-exe/getBudgetExecutionSummary_product/' + xProjectId).then((response)=>{
        let resp = response.data;
        if (resp.success) {
          _this.applySummary(resp.content);
          if (_this.budgetExeSummary.projectId) {
            _this.getProjectSimpleInfo(_this.budgetExeSummary.projectId);
          }
        }else{
          Toast.warning(resp.message || '预算执行表加载失败', Swal);
        }
      }).catch((e) => {
        console.error(e);
        Toast.error('预算执行表加载失败', Swal);
      }).finally(() => {
        _this.loading = false;
      });
    },

    getProjectSimpleInfo(projectId){
      let _this = this;
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/project/getProjectSimpleInfo/'+ projectId).then((response)=>{
        let resp = response.data;
        if (resp.success) {
          _this.projectSimpleInfo = resp.content;
        } else {
          Toast.warning(resp.message,Swal);
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
  height: calc(100vh - 0px);
}

</style>
