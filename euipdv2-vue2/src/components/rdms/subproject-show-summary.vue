<!--
 * Author: 周朔鹏
 * Date: 2025-04-14
 * Email: 391902958@qq.com
 * Website: https://www.euipd.com
-->

<template>
  <div>
    <el-row>
      <el-col :span="getLeftWidth">
        <div class="ca-plain-form" style="border-left: dotted 1px;">
          <div class="ca-form-line" style="font-weight: bold">
            <div class="ca-form-label-xl" style="line-height: 38px;">
              <div class="ca-form-label-title">
                项目名称:
              </div>
            </div>
            <div class="ca-form-content">
              <div style="display: flex; flex-direction: row; flex-wrap: wrap; justify-content: space-between; align-items: center;">
                <div style="line-height: 32px; flex: 1 1 8em; min-width: 8em;">
                  {{ subProject.label }}
                </div>
                <div v-if="!disableApplication" style="display: flex; flex-direction: row; flex-shrink: 0; justify-content: right;align-items: center;">
                  <div v-if="isMainProject" style="margin-right: 10px;">
                    <el-button @click="budgetApplication()">申请预算</el-button>
                  </div>
                  <div v-if="canDistributeBudget" style="margin-right: 20px;">
                    <el-button @click="distributeBudget()">分配预算</el-button>
                  </div>
                </div>
                <!--TODO 项目外包处理-->
                <!-- <div style="margin-right: 20px;">
                       <el-button v-if="canOutsourceFlag" @click="outSource()">项目外包</el-button>
                     </div>-->
              </div>
            </div>
          </div>
          <div class="ca-form-line">
            <div class="ca-form-label-xl">
              <div class="ca-form-label-title">
                项目编号:
              </div>
            </div>
            <div class="ca-form-content">
              {{ subProject.projectCode }}
            </div>
          </div>
          <div class="ca-form-line">
            <div class="ca-form-label-xl">
              <div class="ca-form-label-title">
                项目概要:
              </div>
            </div>
            <div class="ca-form-content">
              <div v-rich-html="subProject.introduce">
              </div>
            </div>
          </div>
          <div class="ca-form-line">
            <div class="ca-form-label-xl">
              <div class="ca-form-label-title">
                管理团队:
              </div>
            </div>
            <div class="ca-form-content">
              项目经理: {{ subProject.projectManagerName }} ;&nbsp;&nbsp; 产品经理: {{ subProject.productManagerName }}
              ;&nbsp;&nbsp; 测试主管: {{ subProject.testManagerName }};&nbsp;&nbsp; 系统工程: {{ subProject.systemManagerName }}
            </div>
          </div>
          <div class="ca-form-line">
            <div class="ca-form-label-xl">
              <div class="ca-form-label-title">
                开发预算:
              </div>
            </div>
            <div class="ca-form-content">
              {{ subProject.budget | rmbFormat }}
            </div>
          </div>
          <div v-if="subProject.rdType!==RD_TYPE.MODULE.key" class="ca-form-line">
            <div class="ca-form-label-xl">
              <div class="ca-form-label-title">
                直接预算:
              </div>
            </div>
            <div class="ca-form-content">
              {{ subProject.addCharge | rmbFormat }}
            </div>
          </div>
          <div class="ca-form-line">
            <div class="ca-form-label-xl">
              <div class="ca-form-label-title">
                总预算:
              </div>
            </div>
            <div class="ca-form-content">
              {{ totalBudgetYuan | rmbFormat }}
            </div>
          </div>
          <div class="ca-form-line" :class="tableRowClassName">
            <div class="ca-form-label-xl">
              <div class="ca-form-label-title" style="padding-top: 3px;">
                预算统计表:
              </div>
            </div>
            <div class="ca-form-content" style="margin-top: 0;">
              <div
                  style="display: flex; flex-direction: row; justify-content: flex-start; align-items: center; flex-wrap: wrap;">
                <div>
                  <el-button type="text" style="margin-left: 0; margin-right: 25px;" @click="showBudgetStatisticsInfo">
                    当前项目预算执行表
                  </el-button>
                </div>
                <div v-if="subProject.id === subProject.projectId">
                  <el-button type="text" style="margin-left: 0; margin-right: 25px;"
                             @click="showBudgetStatisticsInfo_main">完整项目预算执行表
                  </el-button>
                </div>
                <div>
                  <el-button type="text" @click="feeSummaryList">费用清单</el-button>
                </div>
              </div>
            </div>
          </div>
          <div v-if="subProject.rdType!==RD_TYPE.MODULE.key" class="ca-form-line">
            <div class="ca-form-label-xl">
              <div class="ca-form-label-title">
                激励政策:
              </div>
            </div>
            <div class="ca-form-content">
              <div style="word-wrap: break-word;" v-rich-html="subProject.incentivePolicy"></div>
            </div>
          </div>
          <div v-if="subProject.rdType!==RD_TYPE.MODULE.key" class="ca-form-line" style="align-items: center;">
            <div class="ca-form-label-xl" style="margin-top: -4px;">
              <div class="ca-form-label-title">
                计划发布:
              </div>
            </div>
            <div class="ca-form-content">
              {{ subProject.releaseTime | shortDateFormat }}
              <span style="margin-left: 10px;">
                <el-button type="text" size="mini" @click="showProjectMilestones">显示里程碑</el-button>
              </span>
            </div>
          </div>
          <div
              v-if="subProject.rdType!==RD_TYPE.MODULE.key && entrance === ENTRANCE_CLASS.PROJECT.key && subProject.fileList && subProject.fileList.length > 0"
              style="display: flex; flex-direction: column; margin-bottom: 15px;">
            <div style="margin-left: 62px;">附件:</div>
            <div v-for="(item, i) in subProject.fileList" :key="i"
                 style="display: flex; flex-direction: row; align-items: center; margin: 0 0 0 100px;">
              <div v-if="item">
                {{ item.name }}
              </div>
              <div v-if="item && item.authStatus">
                <el-button type="text" style="margin-left: 30px;" @click="downloadFile(item)">下载</el-button>
              </div>
            </div>
          </div>
        </div>
      </el-col>
      <el-col :span="getRightWidth">
        <div class="ca-plain-form" style="border-left: dotted 1px;">
          <div class="ca-form-line" style="font-weight: bold">
            <div class="ca-form-label-xxl">
              <div class="ca-form-label-title">
                预算总额:
              </div>
            </div>
            <div class="ca-form-content">
              {{ subprojectBoardInfo.sumBudget | rmbFormat }}
            </div>
          </div>
          <div class="ca-form-line">
            <div class="ca-form-label-xxl">
              <div class="ca-form-label-title">
                预算投入:
              </div>
            </div>
            <div class="ca-form-content">
              {{ subprojectBoardInfo.activeBudget | rmbFormat }}
            </div>
          </div>
          <div class="ca-form-line">
            <div class="ca-form-label-xxl">
              <div class="ca-form-label-title">
                工时预算:
              </div>
            </div>
            <div class="ca-form-content">
              {{ subprojectBoardInfo.sumManhourBudget | rmbFormat }}
            </div>
          </div>
          <div class="ca-form-line">
            <div class="ca-form-label-xxl">
              <div class="ca-form-label-title">
                工时投入:
              </div>
            </div>
            <div class="ca-form-content">
              {{ subprojectBoardInfo.sumManhourFee | rmbFormat }}
            </div>
          </div>
          <div class="ca-form-line">
            <div class="ca-form-label-xxl">
              <div class="ca-form-label-title">
                物料预算:
              </div>
            </div>
            <div class="ca-form-content">
              {{ subprojectBoardInfo.sumMaterialBudget | rmbFormat }}
            </div>
          </div>
          <div class="ca-form-line">
            <div class="ca-form-label-xxl">
              <div class="ca-form-label-title">
                物料投入:
              </div>
            </div>
            <div class="ca-form-content">
              {{ subprojectBoardInfo.sumMaterialFee | rmbFormat }}
            </div>
          </div>
          <div class="ca-form-line">
            <div class="ca-form-label-xxl">
              <div class="ca-form-label-title">
                设备预算:
              </div>
            </div>
            <div class="ca-form-content">
              {{ subprojectBoardInfo.sumEquipmentBudget | rmbFormat }}
            </div>
          </div>
          <div class="ca-form-line">
            <div class="ca-form-label-xxl">
              <div class="ca-form-label-title">
                设备投入:
              </div>
            </div>
            <div class="ca-form-content">
              {{ subprojectBoardInfo.sumEquipmentFee | rmbFormat }}
            </div>
          </div>
          <div v-if="subProject.rdType!==RD_TYPE.RESEARCH.key" class="ca-form-line">
            <div class="ca-form-label-xxl">
              <div class="ca-form-label-title">
                其他费预算:
              </div>
            </div>
            <div class="ca-form-content">
              {{ subprojectBoardInfo.sumFeeBudget | rmbFormat }}
            </div>
          </div>
          <div class="ca-form-line">
            <div class="ca-form-label-xxl">
              <div class="ca-form-label-title">
                其他费支出:
              </div>
            </div>
            <div class="ca-form-content">
              {{ subprojectBoardInfo.sumFee | rmbFormat }}
            </div>
          </div>
          <div class="ca-form-line">
            <div class="ca-form-label-xxl">
              <div class="ca-form-label-title">
                下发任务总数:
              </div>
            </div>
            <div class="ca-form-content">
              {{ subprojectBoardInfo.sumJobitemNum }}
            </div>
          </div>
          <div class="ca-form-line">
            <div class="ca-form-label-xxl">
              <div class="ca-form-label-title">
                完工任务数:
              </div>
            </div>
            <div class="ca-form-content">
              {{ subprojectBoardInfo.complateJobNum }}
            </div>
          </div>
          <div class="ca-form-line">
            <div class="ca-form-label-xxl">
              <div class="ca-form-label-title">
                提交文档总数:
              </div>
            </div>
            <div class="ca-form-content">
              {{ subprojectBoardInfo.submitDocNum }}
            </div>
          </div>
        </div>
      </el-col>
    </el-row>

    <!--    显示里程碑信息-->
    <el-dialog
        :visible.sync="dialogVisible"
        width="29cm"
        :before-close="handleClose"
        :close-on-click-modal=false>
      <div slot="title" style="font-size: 20px;">{{ dialogTitle }}</div>
      <ProjectMilestoneListDlg
          :key="subProject.projectId"
          :project-id="subProject.projectId"
          :is-main-project="isMainProject"
          :close-function="closeDialog">
      </ProjectMilestoneListDlg>
    </el-dialog>

    <!--查看当前项目预算执行汇总表-->
    <el-dialog
        width="40cm"
        :visible.sync="showStatisticsDlg"
        destroy-on-close
        :before-close="handleClose"
        :close-on-click-modal=false>
      <div slot="title" style="font-size: 20px;">{{ dialogTitle }}</div>
      <div>
        <budgetDirectStatisticsPage
            :key="subProject.id"
            :x-project-id="subProject.id"
            :project-type="PROJECT_TYPE.SUB_PROJECT.key"
        ></budgetDirectStatisticsPage>
      </div>
    </el-dialog>

    <!--查看完整项目预算执行汇总表-->
    <el-dialog
        width="40cm"
        :visible.sync="showStatisticsDlg_main"
        destroy-on-close
        :before-close="handleClose"
        :close-on-click-modal=false>
      <div slot="title" style="font-size: 20px;">{{ dialogTitle }}</div>
      <div>
        <budgetDirectStatisticsPage
            :key="((subProject.projectId) + '-project')"
            :x-project-id="subProject.projectId"
            :project-type="PROJECT_TYPE.PROJECT.key"
        ></budgetDirectStatisticsPage>
      </div>
    </el-dialog>

    <!--项目外包-->
    <el-dialog
        width="23cm"
        :visible.sync="outSourceDlg"
        :before-close="handleClose"
        :close-on-click-modal=false>
      <div slot="title" style="font-size: 20px;">{{ dialogTitle }}</div>
      <div>
        <SubprojectOutsourceCreateDlg
            :key="subProject.id"
            :subproject-id="subProject.id"
            :close-function="closeDialog"
        ></SubprojectOutsourceCreateDlg>
      </div>
    </el-dialog>

    <!--申请预算-->
    <el-dialog
        width="29cm"
        :visible.sync="budgetApplicationDlg"
        :before-close="handleClose"
        :close-on-click-modal=false>
      <div slot="title" style="font-size: 20px;">{{ dialogTitle }}</div>
      <div v-if="isMainProject">
        <BudgetApplicationDlg
            :key="subProject.id"
            :project="subProject"
            :type="BUDGET_TYPE.PROJECT.key"
            :close-function="closeDialog"
        ></BudgetApplicationDlg>
      </div>
      <div v-else>
        子项目不能直接向管理员申请费用，请向上级项目沟通处理。
      </div>
    </el-dialog>

    <!--分配预算-->
    <el-dialog
        width="29cm"
        :visible.sync="distributeBudgetDlg"
        destroy-on-close
        :before-close="handleClose"
        :close-on-click-modal=false>
      <div slot="title" style="font-size: 20px;">{{ dialogTitle }}</div>
      <div>
        <BudgetDistributeDlg
            :key="subProject.id"
            :sub-project="subProject"
            :type="BUDGET_TYPE.PROJECT.key"
            :close-function="closeDialog"
        ></BudgetDistributeDlg>
      </div>
    </el-dialog>

    <!--费用清单-->
    <el-dialog
        width="32cm"
        :visible.sync="feeSummaryListDlg"
        destroy-on-close
        :before-close="handleClose"
        :close-on-click-modal=false>
      <div slot="title" style="font-size: 20px;">{{ dialogTitle }}</div>
      <div>
        <SubprojectFeeSummaryPage
            :key="((getProjectType) + '-' + (feeListScopeId))"
            :subproject-id="feeListScopeId"
            :project-type="getProjectType"
            :close-function="closeDialog"
        ></SubprojectFeeSummaryPage>
      </div>
    </el-dialog>

  </div>
</template>

<script>
import Swal from "sweetalert2";
import ProjectMilestoneListDlg from "@/components/rdms/project-milestone-list-dlg.vue";
import BudgetDirectStatisticsPage from "@/components/std/budget-direct-statistics-page.vue";
import SubprojectOutsourceCreateDlg from "@/components/rdms/subproject-outsource-create-dlg.vue";
import SubprojectFeeSummaryPage from "@/components/std/subproject-fee-summary-page.vue";
import BudgetApplicationDlg from "@/components/rdms/budget-application-dlg.vue";
import BudgetDistributeDlg from "@/components/rdms/budget-distribute-dlg.vue";

export default {
  components: {
    SubprojectFeeSummaryPage,
    BudgetDirectStatisticsPage,
    ProjectMilestoneListDlg,
    SubprojectOutsourceCreateDlg,
    BudgetApplicationDlg,
    BudgetDistributeDlg,
  },
  name: "subproject-show-summary",

  props: {
    subProject: {
      default: null,
    },
    entrance: {
      default: null,
    },
    disableApplication: {
      default: false,
    },
  },

  data: function () {
    return {
      loginUser: {},
      loginCustomer: {},
      subprojectBoardInfo: {},

      ENTRANCE_CLASS: ENTRANCE_CLASS,
      RD_TYPE: RD_TYPE,
      PROJECT_TYPE: PROJECT_TYPE,
      BUDGET_TYPE: BUDGET_TYPE,
      haveUsedBudgetList: [],
      characters: [],
      sumHaveUsedCharacterCharge: 0,
      sumHaveUsedTaskCharge: 0,
      sumHaveUsedTaskCharge_direct: 0,

      showStatisticsDlg: false,
      showStatisticsDlg_main: false,
      feeSummaryListDlg: false,
      outSourceDlg: false,
      budgetApplicationDlg: false,
      distributeBudgetDlg: false,
      dialogVisible: false,
      canOutsourceFlag: false,
      nextLevelSubprojectCount: 0,
      dialogTitle: '',
    }
  },

  computed: {
    getLeftWidth() {
      return 16;
    },
    getRightWidth() {
      return 8;
    },

    isMainProject() {
      let _this = this;
      if (_this.subProject.id === _this.subProject.projectId) {
        return true;
      } else {
        return false;
      }
    },
    /** 分配单由当前子项目经理发起，最终管理人可代办；只能分给直接下级子项目 */
    canDistributeBudget() {
      let _this = this;
      if (_this.nextLevelSubprojectCount <= 0) {
        return false;
      }
      const memberId = (_this.loginUser || {}).customerUserId;
      if (memberId && memberId === _this.subProject.projectManagerId) {
        return true;
      }
      return typeof RoleAccess !== 'undefined'
        && RoleAccess.hasAny(['UltiMng'], (Tool.getLoginUser() || {}).roleList);
    },
    getProjectType() {
      let _this = this;
      if (_this.subProject.id === _this.subProject.projectId) {
        return PROJECT_TYPE.PROJECT.key;
      } else {
        return PROJECT_TYPE.SUB_PROJECT.key;
      }
    },
    /** 费用清单查询作用域：根项目用 projectId，子项目用自身 id */
    feeListScopeId() {
      const p = this.subProject || {};
      if (p.id && p.projectId && p.id === p.projectId) {
        return p.projectId;
      }
      return p.id;
    },
    totalBudgetYuan() {
      const p = this.subProject || {};
      return (Number(p.budget) || 0) + (Number(p.addCharge) || 0);
    },
    tableRowClassName() {
      let _this = this;
      const denom = Number(_this.totalBudgetYuan);
      if (!Number.isFinite(denom) || denom <= 0) {
        return '';
      }
      const used = (Number(_this.sumHaveUsedCharacterCharge) || 0)
        + (Number(_this.sumHaveUsedTaskCharge) || 0)
        + (Number(_this.sumHaveUsedTaskCharge_direct) || 0);
      const rate = used / denom;
      if (!Number.isFinite(rate)) {
        return '';
      }
      if (Number(1.2) >= rate && rate > Number(1.0)) {
        return 'warning-row-1';
      } else if (Number(1.5) >= rate && rate > Number(1.2)) {
        return 'warning-row-2';
      } else if (Number(1.8) >= rate && rate > Number(1.5)) {
        return 'warning-row-3';
      } else if (Number(2.0) >= rate && rate > Number(1.8)) {
        return 'warning-row-4';
      } else if (rate > Number(2.0)) {
        return 'warning-row-5';
      }
      return '';
    }
  },

  watch: {
    'subProject.id': {
      immediate: false,
      handler(newId, oldId) {
        if (!newId || newId === oldId) {
          return;
        }
        this.reloadBudgetPanel();
      },
    },
  },

  mounted: function () {
    let _this = this;
    _this.getLoginUser();
    _this.reloadBudgetPanel();
  },

  methods: {
    reloadBudgetPanel() {
      let _this = this;
      if (!Tool.isNotEmpty_ES5(_this.subProject) || !_this.subProject.id) {
        return;
      }
      _this.haveUsedBudgetList = [];
      _this.characters = [];
      _this.sumHaveUsedCharacterCharge = 0;
      _this.sumHaveUsedTaskCharge = 0;
      _this.sumHaveUsedTaskCharge_direct = 0;
      _this.subprojectBoardInfo = {};
      _this.listAllCharactersBySubProjectId(_this.subProject.id);
      _this.listProjectTaskJobChargeBySubProjectId(_this.subProject.id);
      if (_this.subProject.rdType === RD_TYPE.DIRECT.key) {
        _this.getSumBudgetBySubprojectId(_this.subProject.id);
      }
      _this.getSubprojectBoardInfo(_this.subProject.id);
      _this.getCanBeOutsourcingFlag(_this.subProject.id);
      _this.getNextLevelSubprojectCount(_this.subProject.id);
    },

    getNextLevelSubprojectCount(subProjectId) {
      let _this = this;
      _this.nextLevelSubprojectCount = 0;
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/sub-project/getNextLevelSubproject/' + subProjectId).then((response) => {
        let resp = response.data;
        if (resp.success && subProjectId === _this.subProject.id) {
          _this.nextLevelSubprojectCount = (resp.content || []).length;
        }
      })
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

    showProjectMilestones() {
      let _this = this;
      _this.dialogVisible = true;
      _this.dialogTitle = "项目里程碑信息";
    },
    showBudgetStatisticsInfo() {
      let _this = this;
      _this.showStatisticsDlg = true;
      _this.dialogTitle = "当前项目预算执行表";
    },
    showBudgetStatisticsInfo_main() {
      let _this = this;
      _this.showStatisticsDlg_main = true;
      _this.dialogTitle = "完整项目预算执行表";
    },
    feeSummaryList() {
      let _this = this;
      _this.feeSummaryListDlg = true;
      _this.dialogTitle = _this.getProjectType === PROJECT_TYPE.PROJECT.key
          ? "费用清单（完整项目）"
          : "费用清单（当前项目）";
    },
    outSource() {
      let _this = this;
      _this.outSourceDlg = true;
      _this.dialogTitle = "项目外包";
    },
    budgetApplication() {
      let _this = this;
      _this.budgetApplicationDlg = true;
      _this.dialogTitle = "申请预算";
    },

    distributeBudget() {
      let _this = this;
      _this.distributeBudgetDlg = true;
      _this.dialogTitle = "向子项目分配预算";
    },

    closeDialog() {
      let _this = this;
      _this.dialogVisible = false;
      _this.showStatisticsDlg = false;
      _this.showStatisticsDlg_main = false;
      _this.feeSummaryListDlg = false;
      _this.outSourceDlg = false;
      _this.budgetApplicationDlg = false;
      _this.distributeBudgetDlg = false;
    },
    handleClose(done) {
      done(); //el-dialog用于关闭对话框的内置函数.
    },

    downloadFile(file) {
      let _this = this;
      _this.getSignedUrl(file.path, file.suffix);
    },
    getSignedUrl(fileFullName, suffix) {
      let _this = this;
      let formData = new window.FormData();
      formData.append('fileFullName', fileFullName);
      formData.append('customer', _this.loginCustomer.id);
      formData.append('user', _this.loginUser.customerUserId);
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/file/rdms/oss-get-signed-url', formData).then((response) => {
        let resp = response.data;
        Tool.openOssSignedUrl(resp, suffix);
        // FileSaver.saveAs(resp.content, fileName);
      });
    },

    listAllCharactersBySubProjectId(subprojectId) {
      let _this = this;
      let formData = new window.FormData();
      formData.append('subprojectId', subprojectId);
      formData.append('loginUserId', _this.loginUser.customerUserId);
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/character/listAllCharactersBySubProjectId', formData).then((response) => {
        let resp = response.data;
        if (resp.success) {
          _this.characters = resp.content || [];
          _this.haveUsedBudgetList = [];
          _this.characters.forEach(item => {
            if (item && item.haveUsedBudget != null && item.haveUsedBudget !== '') {
              _this.haveUsedBudgetList.push(item.haveUsedBudget);
            }
          });
          _this.sumHaveUsedCharacterCharge = _this.haveUsedBudgetList.reduce((prev, curr) => {
            const value = Number(curr);
            if (!isNaN(value)) {
              return prev + value;
            }
            return prev;
          }, 0);

          _this.$bus.$emit('refresh-project-subproject-character-list', false);
        } else {
          Toast.warning(resp.message, Swal);
        }
      })
    },

    listProjectTaskJobChargeBySubProjectId(subProjectId) {
      let _this = this;
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/sub-project/listProjectTaskJobChargeBySubProjectId/' + subProjectId).then((response) => {
        let resp = response.data;
        if (resp.success) {
          _this.sumHaveUsedTaskCharge = resp.content;
        } else {
          Toast.warning(resp.message, Swal);
        }
      })
    },

    //直接立项项目, 直接预算支出总额
    getSumBudgetBySubprojectId(subprojectId) {
      let _this = this;
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/budget-direct-exe/getSumBudgetBySubprojectId/' + subprojectId).then((response) => {
        let resp = response.data;
        if (resp.success) {
          _this.sumHaveUsedTaskCharge_direct = resp.content;
        } else {
          Toast.warning(resp.message, Swal);
        }
      })
    },

    getSubprojectBoardInfo(subprojectId) {
      let _this = this;
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/sub-project/getSubprojectBoardInfo/' + subprojectId).then((response) => {
        let resp = response.data;
        if (resp.success) {
          _this.subprojectBoardInfo = resp.content;
        } else {
          Toast.warning(resp.message, Swal);
        }
      })
    },

    getCanBeOutsourcingFlag(subprojectId) {
      let _this = this;
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/sub-project/getCanBeOutsourcingFlag/' + subprojectId).then((response) => {
        let resp = response.data;
        if (resp.success) {
          _this.canOutsourceFlag = resp.content;
        } else {
          Toast.warning(resp.message, Swal);
        }
      })
    },
  }
}
</script>

<style>
.warning-row-1 {
  background: #FDF5E6FF;
}
.warning-row-2 {
  background: #fde8c1;
}
.warning-row-3 {
  background: #fcdba0;
}
.warning-row-4 {
  background: #facd7c;
}
.warning-row-5 {
  background: #fac059;
}

</style>
<style lang="scss" scoped>
@import "./../../../public/static/scss/caipd";

</style>
