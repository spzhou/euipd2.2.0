<!--
 * Author: 周朔鹏
 * Date: 2024-12-23
 * Email: 391902958@qq.com
 * Website: https://www.euipd.com
-->

<template>
  <div>
    <div>
      <div style="display: flex; flex-direction: row; justify-content: space-between; align-items: center;">
        <div style="font-size: 16px; font-weight: 500; margin-left: 5px;">
          {{ "项目整体执行进度情况" }}
        </div>
        <div style="margin-right: 30px;">
          <!--空-->
        </div>
      </div>
      <div class="hr hr-10 hr-dotted"></div>
      <div style="margin: 8px 60px;">
        <div style="display: flex; flex-direction: row; justify-content: space-between; align-items: center;">
          <div style="margin-bottom: 4px;">预算执行进度 = ( 支出数额 + 应付未付 ) / 预算总额</div>
          <div>
                <span class="comment-text">
                  支出数额 + 应付未付: {{ getCurrentProject.sumActiveBudget }} 元
                </span>
            <span>
                  预算总额: {{ getCurrentProject.sumBudget }} 元
                </span>
          </div>
        </div>
        <el-progress :text-inside="true" :stroke-width="26" :percentage=getBudgetRate></el-progress>
        <div class="hr hr-8 hr-dotted"></div>
      </div>

      <div style="margin: 8px 60px;">
        <div style="display: flex; flex-direction: row; justify-content: space-between; align-items: center;">
          <div style="margin-bottom: 4px;">工时投入进度 = 工时费累计 / 总工时预算</div>
          <div>
                <span class="comment-text">
                  工时费累计: {{ getCurrentProject.sumActiveManhourBudget }} 元
                </span>
            <span>
                  总工时预算: {{ getCurrentProject.sumManhourBudget }} 元
                </span>
          </div>
        </div>
        <el-progress :text-inside="true" :stroke-width="26" :percentage=getManhourBudgetRate></el-progress>
        <div class="hr hr-8 hr-dotted"></div>
      </div>
      <div style="margin: 8px 60px;">
        <div style="display: flex; flex-direction: row; justify-content: space-between; align-items: center;">
          <div style="margin-bottom: 4px;">物料投入进度 = 批准后的费用化物料 / 物料费总预算</div>
          <div>
                <span class="comment-text">
                  批准后的费用化物料: {{ getCurrentProject.sumActiveMaterialBudget }} 元
                </span>
            <span>
                  物料费总预算: {{ getCurrentProject.sumMaterialBudget }} 元
                </span>
          </div>
        </div>
        <el-progress :text-inside="true" :stroke-width="26" :percentage=getMaterialBudgetRate></el-progress>
        <div class="hr hr-8 hr-dotted"></div>
      </div>
      <div style="margin: 8px 60px;">
        <div style="display: flex; flex-direction: row; justify-content: space-between; align-items: center;">
          <div style="margin-bottom: 4px;">设备购置进度 = 已批准的设备费 / 总设备费预算</div>
          <div>
                <span class="comment-text">
                  已批准的设备费: {{ getCurrentProject.sumActiveEquipmentBudget }} 元
                </span>
            <span>
                  总设备费预算: {{ getCurrentProject.sumEquipmentBudget }} 元
                </span>
          </div>
        </div>
        <el-progress :text-inside="true" :stroke-width="26" :percentage=getEquipmentBudgetRate></el-progress>
        <div class="hr hr-8 hr-dotted"></div>
      </div>
      <div style="margin: 8px 60px;">
        <div style="display: flex; flex-direction: row; justify-content: space-between; align-items: center;">
          <div style="margin-bottom: 4px;">费用支出进度 = 已批准的所有其他费用 / 其他费用总预算</div>
          <div>
                <span class="comment-text">
                  已批准的所有其他费用: {{ getCurrentProject.sumActiveOtherBudget }} 元
                </span>
            <span>
                  其他费用总预算: {{ getCurrentProject.sumOtherBudget }} 元
                </span>
          </div>
        </div>
        <el-progress :text-inside="true" :stroke-width="26" :percentage=getOtherBudgetRate></el-progress>
        <div class="hr hr-8 hr-dotted"></div>
      </div>
      <div class="hr hr-16 hr-dotted hr-double"></div>
      <div style="font-size: 16px; font-weight: 500; margin-left: 5px;">
        {{ "子项目预算执行情况" }}
      </div>
      <div class="hr hr-10 hr-dotted"></div>
      <div
          style="display: flex; flex-direction: row; justify-content: space-evenly; align-items: center; flex-wrap: wrap;">
        <div v-for="(item, i) in getCurrentProject.subprojectStatisticsItemList" style="margin: 10px 10px 0; ">
          <a href="javascript:void(0);" @click="showSummaryDlg(item)">
            <div
                style="display: flex; flex-direction: column; justify-content: center; align-items: center; width: 240px;">
              <el-progress type="dashboard" :percentage="ratePercent(item.budgetExeRate)" :color="colors"></el-progress>
              <div style="margin-top: -15px; font-size: 11px;">
                {{ "预算执行进度" }}
              </div>
              <div style="margin-top: 5px; font-size: 14px; width: 230px; text-align: center;">
                {{ item.projectCode }}
              </div>
              <div style="margin-top: 5px; font-size: 14px; width: 230px; text-align: center; font-weight: 500">
                {{ item.projectName }}
              </div>
            </div>
          </a>
        </div>
      </div>
      <div class="hr hr-16 hr-dotted hr-double"></div>
      <div style="padding-top: 10px;">
        <subprojectSummaryTabs
            :id="('project-' + (projectId))"
            :key="projectId"
            :subproject-id="projectId"
            :project-type="PROJECT_TYPE.PROJECT.key"
        ></subprojectSummaryTabs>
      </div>
    </div>


    <!--项目统计信息-->
    <el-dialog
        :visible.sync="summaryShowDlg"
        width="40cm"
        :before-close="handleClose"
        :close-on-click-modal=false>
      <div slot="title" style="font-size: 20px;">{{ dialogTitle }}</div>
      <div>
        <subprojectSummaryTabs
            :id="('subproject-' + (subprojectId))"
            :key="subprojectId"
            :subproject-id="subprojectId"
            :project-type="subprojectType"
        ></subprojectSummaryTabs>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import Swal from "sweetalert2";
import {fixed2fractional, rmbFormat} from "@/filter/filter";
import subprojectSummaryTabs from "@/components/std/subproject-summary-tabs.vue";
import {Loading} from "element-ui";

export default {
  components: {
    subprojectSummaryTabs,
  },
  name: "project-item-summary",

  props: {
    projectId: {
      default: null,
    },
    projectType: {
      default: null,
    },
    setupStage: {
      default: SETUP_STAGE.SETUPED.key,
    },
    index: {
      default: null,
    },
    entrance: {
      default: null,
    }
  },

  data: function () {
    return {
      loginUser: {},
      loginCustomer: {},

      editableTabsValue: 0,
      budgetExeSummary: [],
      workerPerformanceList: [],

      subprojectType: '',
      subprojectId: '',
      dialogTitle: '',
      summaryShowDlg: false,
      PROJECT_TYPE: PROJECT_TYPE,

      colors: [
        {color: '#f56c6c', percentage: 20},
        {color: '#e6a23c', percentage: 40},
        {color: '#5cb87a', percentage: 60},
        {color: '#1989fa', percentage: 80},
        {color: '#6f7ad3', percentage: 100}
      ]
    }
  },

  mounted: function () {
    let _this = this;
    _this.getLoginUser();
  },

  computed: {
    getCurrentProject() {
      let _this = this;
      return _this.budgetExeSummary;
    },

    getBudgetRate() {
      let _this = this;
      if(_this.getCurrentProject.sumBudget !== null && Number(_this.getCurrentProject.sumBudget) > 0){
        _this.budgetRate = Number(fixed2fractional(_this.getCurrentProject.sumActiveBudget/_this.getCurrentProject.sumBudget*100));
        if(_this.budgetRate > 100){
          return 100.0;
        }else if(_this.budgetRate < 0){
          return 0.0;
        }else{
          return _this.budgetRate;
        }
      }else{
        return 0.0;
      }
    },
    getManhourBudgetRate() {
      let _this = this;
      if(_this.getCurrentProject.sumManhourBudget !== null && _this.getCurrentProject.sumManhourBudget > 0){
        _this.manhourBudgetRate = Number(fixed2fractional(_this.getCurrentProject.sumActiveManhourBudget/_this.getCurrentProject.sumManhourBudget*100));
        if(_this.manhourBudgetRate > 100){
          return 100.0;
        }else if(_this.manhourBudgetRate < 0){
          return 0.0;
        }else{
          return _this.manhourBudgetRate;
        }
      }else{
        return 0.0;
      }
    },
    getMaterialBudgetRate() {
      let _this = this;
      if(_this.getCurrentProject.sumMaterialBudget !== null && Number(_this.getCurrentProject.sumMaterialBudget) > 0){
        _this.materialBudgetRate = Number(fixed2fractional(_this.getCurrentProject.sumActiveMaterialBudget/_this.getCurrentProject.sumMaterialBudget*100));
        if(_this.materialBudgetRate > 100){
          return 100.0;
        }else if(_this.materialBudgetRate < 0){
          return 0.0;
        }else{
          return _this.materialBudgetRate;
        }
      }else{
        return 0.0;
      }
    },
    getEquipmentBudgetRate() {
      let _this = this;
      if(_this.getCurrentProject.sumEquipmentBudget !== null && Number(_this.getCurrentProject.sumEquipmentBudget) > 0){
        _this.equipmentBudgetRate = Number(fixed2fractional(_this.getCurrentProject.sumActiveEquipmentBudget/_this.getCurrentProject.sumEquipmentBudget*100));
        if(_this.equipmentBudgetRate > 100){
          return 100.0;
        }else if(_this.equipmentBudgetRate < 0){
          return 0.0;
        }else{
          return _this.equipmentBudgetRate;
        }
      }else{
        return 0.0;
      }
    },
    getOtherBudgetRate() {
      let _this = this;
      if(_this.getCurrentProject.sumOtherBudget !== null && Number(_this.getCurrentProject.sumOtherBudget) > 0){
        _this.otherBudgetRate = Number(fixed2fractional(_this.getCurrentProject.sumActiveOtherBudget/_this.getCurrentProject.sumOtherBudget*100));
        if(_this.otherBudgetRate > 100){
          return 100.0;
        }else if(_this.otherBudgetRate < 0){
          return 0.0;
        }else{
          return _this.otherBudgetRate;
        }
      }else{
        return 0.0;
      }
    },

  },

  methods: {
    ratePercent(rate) {
      const value = Math.round((Number(rate) || 0) * 100);
      return Math.max(0, Math.min(100, value));
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
      _this.getMainProjectBudgetExecutionSummary(_this.projectId);

    },

    getMainProjectBudgetExecutionSummary(projectId) {
      let _this = this;
      let loadingInstance = Loading.service({fullscreen: true});
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/budget-direct-exe/getMainProjectBudgetExecutionSummary/' + projectId).then((response) => {
        loadingInstance.close();
        let resp = response.data;
        if (resp.success) {
          _this.budgetExeSummary = resp.content;
        } else {
          Toast.warning(resp.message, Swal);
        }
      })
    },

    showSummaryDlg(subproject) {
      let _this = this;
      _this.subprojectId = subproject.subprojectId;
      _this.subprojectType = PROJECT_TYPE.SUB_PROJECT.key;
      _this.dialogTitle = '项目执行信息汇总';
      _this.summaryShowDlg = true;

    },

    handleClose(done) {
      done(); //el-dialog用于关闭对话框的内置函数.
    },

    closeDialog() {
      let _this = this;
      _this.summaryShowDlg = false;
    },
  }
}
</script>

<style scoped>
.comment-text {
  margin-right: 20px;
}
</style>
