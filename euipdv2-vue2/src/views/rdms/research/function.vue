<!--
 * Author: 周朔鹏
 * Date: 2025-02-26
 * Email: 391902958@qq.com
 * Website: https://www.euipd.com
-->

<template>
  <div>
    <!--Tab 卡片-->
        <WorkspaceFrame>
        <template v-slot:nav>
            <li :class="{active : activePageId==='home'}">
              <a @click="tabHomeClick">
                <i class="green ace-icon fa fa-home bigger-120"></i>
                预立项列表
              </a>
            </li>

            <li v-permission="'522101'" :class="{active : activePageId==='function'}">
              <a @click="functionTabClick">
                <i class="green ace-icon fa bigger-120"></i>
                <el-badge :is-dot="hasApplication && role_SYSGM">
                  系统工程列表
                </el-badge>
              </a>
            </li>

            <!--特性管理-->
            <DynamicTabItem
                v-for="(item, index) in preProject_function"
                :key="item.id"
                :index="index"
                :active-page-id="activePageId"
                prefix="pre-project-item"
                :close="closeFunction_function"
                @select="tabFunctionClick">{{ ('特性管理 : ' + (item.preProjectName)) }}
            </DynamicTabItem>

            <!--系统工程-->
            <DynamicTabItem
                v-for="(item, index) in systemFunctions"
                :key="item.id"
                :index="index"
                :active-page-id="activePageId"
                prefix="system-function-item"
                :close="closeFunction_systemFunction"
                @select="tabSystemFunctionClick">{{ ('系统工程 : ' + (item.preProjectName)) }}
            </DynamicTabItem>

            <!--系统工程查看-->
            <DynamicTabItem
                v-for="(item, index) in systemFunctionsView"
                :key="item.id"
                :index="index"
                :active-page-id="activePageId"
                prefix="system-function-view-item"
                :close="closeFunction_systemFunctionView"
                @select="tabSystemFunctionViewClick">{{ ('系统工程查看 : ' + (item.preProjectName)) }}
            </DynamicTabItem>

            <!--任务管理-->
            <DynamicTabItem
                v-for="(item, index) in projectTaskItems"
                :key="item.id"
                :index="index"
                :active-page-id="activePageId"
                prefix="task-manage"
                :close="closeFunction_develop"
                @select="funTaskManagerClick">{{ ('任务:' + (item.preProjectName)) }}
            </DynamicTabItem>

            <!--执行数据-->
            <DynamicTabItem
                v-for="(item, index) in projectArchiveItems"
                :key="item.id"
                :index="index"
                :active-page-id="activePageId"
                prefix="archive-tree"
                :close="closeFunction_archive_data"
                @select="developArchiveClick">{{ ('文档:' + (item.preProjectName)) }}
            </DynamicTabItem>

            <!--物料管理-->
            <DynamicTabItem
                v-for="(item, index) in functionTaskMaterialList"
                :key="item.id"
                :index="index"
                :active-page-id="activePageId"
                prefix="material-manage-item"
                :close="closeFunction_material"
                @select="tabMaterialItemClick">{{ ('物料管理 : ' + (item.preProjectName)) }}
            </DynamicTabItem>

            <!--费用管理-->
            <DynamicTabItem
                v-for="(item, index) in functionFeeList"
                :key="item.id"
                :index="index"
                :active-page-id="activePageId"
                prefix="fee-manage-item"
                :close="closeFunction_fee"
                @select="tabFeeItemClick">{{ ('费用管理 : ' + (item.preProjectName)) }}
            </DynamicTabItem>

          
        </template>

                        <DynamicTabPane
                page-id="home"
                :active-page-id="activePageId">
                            <template v-slot:default>
              <p>预立项项目: 是为<strong>系统工程师</strong>提供的一个对产品特性进行管理的页面.</p>
              <div class="hr hr-18 dotted hr-double"></div>
              <div>
                <el-table
                    :data="preProjects"
                    border
                    highlight-current-row
                    style="width: 100%"
                    @row-click="rowClick">
                  <el-table-column
                      prop="projectCode"
                      label="项目编号"
                      min-width="180"
                      show-overflow-tooltip
                      align="center"
                  ></el-table-column>
                  <el-table-column
                      prop="preProjectName"
                      label="项目名称"
                      min-width="250"
                      show-overflow-tooltip
                  ></el-table-column>
                  <el-table-column
                      prop="productManagerName"
                      label="产品经理"
                      align="center"
                      min-width="100"
                  ></el-table-column>
                  <el-table-column
                      prop="systemManagerName"
                      label="系统工程师"
                      align="center"
                      min-width="100"
                  ></el-table-column>
                  <el-table-column
                      prop="devVersion"
                      :formatter="versionFormat"
                      label="开发版本"
                      align="center"
                      min-width="100"
                  ></el-table-column>
                  <el-table-column
                      prop="createTime"
                      :formatter="creatTimeFormat"
                      align="center"
                      label="创建时间"
                      min-width="160"
                  ></el-table-column>
                  <el-table-column
                      fixed="right"
                      label="操作"
                      align="center"
                      width="550">
                    <template slot-scope="scope">
                      <el-tooltip content="请执行人对相应特性进行定义." placement="top" effect="light" :open-delay=1000>
                        <el-button type="primary" plain size="small" v-on:click.stop="characterDefine(scope.row)">
                          特性定义
                        </el-button>
                      </el-tooltip>
                      <el-tooltip content="对预立项特性进行管理" placement="top" effect="light" :open-delay=1000>
                        <el-button type="primary" plain size="small" v-on:click.stop="functionManage(scope.row)">
                          <el-badge :is-dot="scope.row.submitJobNum >0">
                            特性管理
                          </el-badge>
                        </el-button>
                      </el-tooltip>
                      <el-tooltip content="对本预立项项目定义的所有特性进行结构化管理" placement="top" effect="light"
                                  :open-delay=1000>
                        <el-button type="primary" plain size="small" v-on:click.stop="systemFunctionManage(scope.row)">
                          系统工程
                        </el-button>
                      </el-tooltip>
                      <el-tooltip content="对系统工程任务进行管理." placement="top" effect="light" :open-delay=1000>
                        <el-button v-on:click.s.stop="funTaskManagement(scope.row)" type="primary" plain size="small">
                          <el-badge :is-dot="scope.row.taskSubmitJobNum >0">
                            任务管理
                          </el-badge>
                        </el-button>
                      </el-tooltip>
                      <el-tooltip content="结构化查看全部执行数据." placement="top" effect="light" :open-delay=1000>
                        <el-button v-on:click.s.stop="developArchiveShow(scope.row)" type="primary" plain size="small">
                          执行数据
                        </el-button>
                      </el-tooltip>
                    </template>
                  </el-table-column>
                </el-table>
              </div>
            
            
                            </template></DynamicTabPane>
                        <DynamicTabPane
                page-id="function"
                :active-page-id="activePageId">
                            <template v-slot:default>
              <div style="display: flex; flex-direction: row; justify-content: space-between; align-items: center;">
                <div>
                  <p>系统工程列表: 是为<strong>系统工程总监</strong>提供的一个对产品特性进行管理的页面.</p>
                </div>
                <div style="margin-right: 30px;">
                  <el-button type="primary" @click.stop="showStageStatisticsInfo">预算汇总数据</el-button>
                </div>
              </div>
              <div class="hr hr-18 dotted hr-double"></div>
              <div>
                <el-table
                    :data="preProjectsView"
                    border
                    highlight-current-row
                    style="width: 100%">
                  <el-table-column
                      prop="projectCode"
                      label="项目编号"
                      min-width="180"
                      show-overflow-tooltip
                      align="center"
                  ></el-table-column>
                  <el-table-column
                      prop="preProjectName"
                      label="项目名称"
                      align="center"
                      min-width="250"
                      show-overflow-tooltip
                  ></el-table-column>
                  <el-table-column
                      prop="productManagerName"
                      label="产品经理"
                      align="center"
                      min-width="100"
                  ></el-table-column>
                  <el-table-column
                      prop="systemManagerName"
                      label="系统工程师"
                      align="center"
                      min-width="100"
                  ></el-table-column>
                  <el-table-column
                      prop="devVersion"
                      :formatter="versionFormat"
                      label="开发版本"
                      align="center"
                      min-width="100"
                  ></el-table-column>
                  <el-table-column
                      prop="createTime"
                      :formatter="creatTimeFormat"
                      align="center"
                      label="创建时间"
                      min-width="160"
                  ></el-table-column>
                  <el-table-column
                      fixed="right"
                      label="操作"
                      align="center"
                      width="550">
                    <template slot-scope="scope">
                      <el-tooltip content="查看预立项项目全部特性定义情况" placement="top" effect="light"
                                  :open-delay=1000>
                        <el-button type="primary" plain size="small" v-on:click.stop="systemFunctionView(scope.row)">
                          查看功能
                        </el-button>
                      </el-tooltip>
                      <el-tooltip content="对系统工程任务所需物料进行管理" placement="top" effect="light"
                                  :open-delay=1000>
                        <el-button v-if="scope.row.sysgmId === loginUser.customerUserId" type="primary" plain size="small"
                                   v-on:click.stop="functionTaskMaterialManage(scope.row)">
                          <el-badge :is-dot="scope.row.materialAppNum >0">
                            物料管理
                          </el-badge>
                        </el-button>
                      </el-tooltip>
                      <el-tooltip v-if="scope.row.sysgmId === loginUser.customerUserId" content="对系统工程任务处理过程中其他相关费用进行管理" placement="top" effect="light"
                                  :open-delay=1000>
                        <el-button v-if="scope.row.sysgmId === loginUser.customerUserId" type="primary" plain size="small" v-on:click.stop="functionFeeManage(scope.row)">
                          <el-badge :is-dot="scope.row.feeAppNum >0">
                            费用管理
                          </el-badge>
                        </el-button>
                      </el-tooltip>
                      <el-tooltip content="查看预立项项目预算执行数据" placement="top" effect="light" :open-delay=1000>
                        <el-button type="primary" plain size="small"
                                   v-on:click.stop="showBudgetStatisticsInfo(scope.row)">
                          预算数据
                        </el-button>
                      </el-tooltip>
                      <el-tooltip content="申请系统工程预算" placement="top" effect="light" :open-delay=1000>
                        <el-button v-if="scope.row.sysgmId === loginUser.customerUserId" type="primary" plain size="small" v-on:click.stop="budgetApplication(scope.row)">
                          预算申请
                        </el-button>
                      </el-tooltip>

                    </template>
                  </el-table-column>
                </el-table>
              </div>
            
            
                            </template></DynamicTabPane>
            <!-- 系统工程查看-->
            <DynamicTabPane
                v-for="(item, index) in systemFunctionsView"
                :key="('system-function-view-item-' + (item.id))"
                :index="index"
                prefix="system-function-view-item"
                :active-page-id="activePageId">
                <template v-slot:default>
              <div>
                <SystemFunctionViewTab
                    :key="item.id"
                    :pre-project-id="item.id"
                    :job-type="JOB_ITEM_TYPE.DEMAND.key"
                    :project-type="PROJECT_TYPE.PRE_PROJECT.key"
                    :index="index"
                    :entrance="ENTRANCE_CLASS.FUNCTION.key"
                ></SystemFunctionViewTab>
              </div>
            
                </template></DynamicTabPane>

            <!-- 物料管理-->
            <DynamicTabPane
                v-for="(item, index) in functionTaskMaterialList"
                :key="('material-manage-item-' + (item.id))"
                :index="index"
                prefix="material-manage-item"
                :active-page-id="activePageId">
                <template v-slot:default>
              <FunctionTaskMaterialList
                  :key="item.id"
                  :preproject-id="item.id"
              ></FunctionTaskMaterialList>
            
                </template></DynamicTabPane>

            <!-- 费用管理-->
            <DynamicTabPane
                v-for="(item, index) in functionFeeList"
                :key="('fee-manage-item-' + (item.id))"
                :index="index"
                prefix="fee-manage-item"
                :active-page-id="activePageId">
                <template v-slot:default>
              <FunctionTaskFeeList
                  :key="item.id"
                  :preproject-id="item.id"
              ></FunctionTaskFeeList>
            
                </template></DynamicTabPane>

            <!-- 特性管理-->
            <DynamicTabPane
                v-for="(item, index) in preProject_function"
                :key="('pre-project-item-' + (item.id))"
                :index="index"
                prefix="pre-project-item"
                :active-page-id="activePageId">
                <template v-slot:default>
              <div>
                <!--测试管理的projectType 固定为 Sub_project, 即便是产品管理过程中 实际为Project类型, 也使用sub_project类型处理-->
                <PreprojectFunctionTab
                    :key="item.id"
                    :pre-project-id="item.id"
                    :job-type="JOB_ITEM_TYPE.DEMAND.key"
                    :project-type="PROJECT_TYPE.PRE_PROJECT.key"
                    :index="index"
                    :entrance="ENTRANCE_CLASS.FUNCTION.key"
                ></PreprojectFunctionTab>
              </div>
            
                </template></DynamicTabPane>

            <!-- 系统工程-->
            <DynamicTabPane
                v-for="(item, index) in systemFunctions"
                :key="('system-function-item-' + (item.id))"
                :index="index"
                prefix="system-function-item"
                :active-page-id="activePageId">
                <template v-slot:default>
              <div>
                <SystemFunctionTab
                    :key="item.id"
                    :pre-project-id="item.id"
                    :job-type="JOB_ITEM_TYPE.DEMAND.key"
                    :project-type="PROJECT_TYPE.PRE_PROJECT.key"
                    :index="index"
                    :entrance="ENTRANCE_CLASS.FUNCTION.key"
                ></SystemFunctionTab>
              </div>
            
                </template></DynamicTabPane>

            <!-- 任务管理-->
            <DynamicTabPane
                v-for="(item, index) in projectTaskItems"
                :key="('task-' + (item.id))"
                :index="index"
                prefix="task-manage"
                :active-page-id="activePageId">
                <template v-slot:default>
              <div>
                <PreprojectTaskTab
                    :key="item.id "
                    :pre-project-id="item.id"
                    :job-type="JOB_ITEM_TYPE.TASK_FUNCTION.key"
                    :project-type="PROJECT_TYPE.PRE_PROJECT.key"
                    :index="index"
                    :entrance="ENTRANCE_CLASS.FUNCTION.key"
                ></PreprojectTaskTab>
              </div>
            
                </template></DynamicTabPane>

            <!-- 执行数据-->
            <DynamicTabPane
                v-for="(item, index) in projectArchiveItems"
                :key="('archive-' + (item.id))"
                :index="index"
                prefix="archive-tree"
                :active-page-id="activePageId">
                <template v-slot:default>
              <div>
                <PreprojectArchiveContainer
                    :key="item.id"
                    :index="index"
                    :pre-project-id="item.id"
                    :project-type="PROJECT_TYPE.PRE_PROJECT.key"
                    :entrance="ENTRANCE_CLASS.FUNCTION.key">
                </PreprojectArchiveContainer>
              </div>
            
                </template></DynamicTabPane>
          
    </WorkspaceFrame>

    <!--特性定义任务-->
    <el-dialog
        width="29cm"
        :visible.sync="directJobItemDlg"
        :before-close="handleClose"
        :close-on-click-modal=false>
      <div slot="title" style="font-size: 20px;">{{ dialogTitle }}</div>
      <div v-if="directJobItemDlg">
        <JobitemCharacterDefineDlg
            :key="((timeStamp) + '-' + (preproject.id))"
            :demand-id-list="null"
            :pre-project-id="preproject.id"
            :project-type="PROJECT_TYPE.PRE_PROJECT.key"
            :close-dlg=closeDialog
        ></JobitemCharacterDefineDlg>
      </div>
    </el-dialog>

    <!--查看预算执行汇总表-->
    <el-dialog
        width="40cm"
        :visible.sync="showStatisticsDlg"
        destroy-on-close
        :before-close="handleClose"
        :close-on-click-modal=false>
      <div slot="title" style="font-size: 20px;">{{ dialogTitle }}</div>
      <div v-if="showStatisticsDlg">
        <budgetDirectStatisticsPage
            :key="preproject.id"
            :x-project-id="preproject.id"
            :project-type="PROJECT_TYPE.PRE_PROJECT.key"
        ></budgetDirectStatisticsPage>
      </div>
    </el-dialog>

    <!--系统工程预算汇总-->
    <el-dialog
        width="40cm"
        :visible.sync="showStageStatisticsDlg"
        :before-close="handleClose"
        :close-on-click-modal=false>
      <div slot="title" style="font-size: 20px;">{{ dialogTitle }}</div>
      <div v-if="showStageStatisticsDlg">
        <BudgetStageStatisticsPage
            :key="loginCustomer.id"
            :customer-id="loginCustomer.id"
            :stage="BUDGET_TYPE.PRE_PROJECT.key"
        ></BudgetStageStatisticsPage>
      </div>
    </el-dialog>

    <!--申请预算-->
    <el-dialog
        width="29cm"
        :visible.sync="budgetApplicationDlg"
        :before-close="handleClose"
        :close-on-click-modal=false>
      <div slot="title" style="font-size: 20px;">{{ dialogTitle }}</div>
      <div v-if="budgetApplicationDlg">
        <BudgetApplicationDlg
            :key="preproject.id"
            :project="preproject"
            :type="BUDGET_TYPE.PRE_PROJECT.key"
            :close-function="closeDialog"
        ></BudgetApplicationDlg>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import {closeDynamicTab, openOrFocusTab} from "@/shared/dynamic-tab";
import DynamicTabItem from "@/components/rdms/dynamic-tab-item";
import DynamicTabPane from "@/components/rdms/dynamic-tab-pane";
import WorkspaceFrame from "@/components/rdms/workspace-frame";
import File from "@/components/rdms/file";
import Swal from "sweetalert2";
import TestJoblistTab from "@/components/rdms/test-joblist-tab";

import {dateFormat, fixed2fractional} from "@/filter/filter";
import TestMainTestJoblistTab from "@/components/rdms/test-main-test-joblist-tab.vue";
import PreprojectFunctionTab from "@/components/rdms/preproject-function-tab.vue";
import PreprojectTaskTab from "@/components/rdms/preproject-task-tab.vue";
import PreprojectArchiveContainer from "@/components/rdms/preproject-archive-container.vue";
import JobitemCharacterDefineDlg from "@/components/std/jobitem-character-define-dlg.vue";
import SystemFunctionTab from "@/components/rdms/system-function-tab.vue";
import SystemFunctionViewTab from "@/components/rdms/system-function-view-tab.vue";
import BudgetDirectStatisticsPage from "@/components/std/budget-direct-statistics-page.vue";
import BudgetApplicationDlg from "@/components/rdms/budget-application-dlg.vue";
import FunctionTaskMaterialList from "@/components/rdms/function-task-material-list.vue";
import FunctionTaskFeeList from "@/components/rdms/function-task-fee-list.vue";
import BudgetStageStatisticsPage from "@/components/std/budget-stage-statistics-page.vue";

export default {
  components: {
    FunctionTaskFeeList,
    FunctionTaskMaterialList,
    BudgetApplicationDlg,
    BudgetDirectStatisticsPage,
    JobitemCharacterDefineDlg,
    PreprojectArchiveContainer,
    PreprojectTaskTab,
    TestMainTestJoblistTab,
    File,
    DynamicTabItem,
    DynamicTabPane,
    WorkspaceFrame,
    TestJoblistTab,
    PreprojectFunctionTab,
    SystemFunctionTab,
    SystemFunctionViewTab,
    BudgetStageStatisticsPage,
  },
  name: "function",

  data: function () {
    return {
      loginUser: {},
      loginCustomer: {},

      JOB_ITEM_TYPE: JOB_ITEM_TYPE,
      PROJECT_TYPE: PROJECT_TYPE,
      ENTRANCE_CLASS: ENTRANCE_CLASS,
      BUDGET_TYPE: BUDGET_TYPE,

      activePageId: 'home',
      preActivePageId: 'home',
      aProject: {},
      preProjects: [],
      preProjectsView: [],

      budgetApplicationDlg: false,
      directJobItemDlg: false,
      dialogTitle: 'Dialog',
      timeStamp: '',

      preProject_function: [],
      systemFunctions: [],
      systemFunctionsView: [],
      projectItems_demand: [],
      projectTaskItems: [],
      projectArchiveItems: [],
      functionTaskMaterialList: [],
      functionFeeList: [],

      preproject: '',
      role_SYSGM: false,
      showStatisticsDlg: false,
      showStageStatisticsDlg: false,
      hasApplication: false,
    }
  },

  computed: {
  },

  mounted: function () {
    let _this = this;
    _this.getLoginUser();
    _this.getPreProjectListBySm();

    _this.$bus.$on('refresh-function-list', (data) => {
      if (data) {
        _this.getPreProjectListBySm();
      }
    });
    _this.$bus.$on('refresh-function-list-sysgm', (data) => {
      if (data) {
        _this.getPreProjectListBySysgm();
      }
    });

  },

  beforeDestroy() {
    let _this = this;
    _this.$bus.$off('refresh-function-list');
    _this.$bus.$off('refresh-function-list-sysgm');
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

      RoleAccess.applyFlags(_this, {
        role_SYSGM: 'SYSGM'
      });
      if (Tool.hasButtonPermission && Tool.hasButtonPermission('522101')) {
        _this.getPreProjectListBySysgm();
      }

    },

    budgetApplication(preproject) {
      let _this = this;
      _this.preproject = preproject;
      _this.budgetApplicationDlg = true;
      _this.dialogTitle = "申请预算";
    },

    showBudgetStatisticsInfo(preproject) {
      let _this = this;
      _this.preproject = preproject;
      _this.showStatisticsDlg = true;
      _this.dialogTitle = "查看预算执行汇总表";
    },

    showStageStatisticsInfo() {
      let _this = this;
      _this.showStageStatisticsDlg = true;
      _this.dialogTitle = "系统工程预算汇总表";
    },

    creatTimeFormat(row, column) {
      return dateFormat(row.createTime);
    },

    tabFunctionClick(i) {
      let _this = this;
      _this.activePageId = `pre-project-item-${i}`;
    },

    tabSystemFunctionClick(i) {
      let _this = this;
      _this.activePageId = `system-function-item-${i}`;
    },

    tabSystemFunctionViewClick(i) {
      let _this = this;
      _this.activePageId = `system-function-view-item-${i}`;
    },

    tabHomeClick() {
      let _this = this;
      _this.activePageId = 'home';
      _this.preActivePageId = 'home';
      _this.getPreProjectListBySm();
    },

    functionTabClick() {
      let _this = this;
      _this.activePageId = 'function';
      _this.preActivePageId = 'function';
      _this.getPreProjectListBySm();
    },

    characterDefine(preproject) {
      let _this = this;
      _this.preproject = preproject;
      _this.timeStamp = Date.now();
      _this.directJobItemDlg = true;
      _this.dialogTitle = "直接创建特性定义任务";
    },
    closeDialog() {
      let _this = this;
      _this.directJobItemDlg = false;
      _this.showStatisticsDlg = false;
      _this.showStageStatisticsDlg = false;
      _this.budgetApplicationDlg = false;
    },

    tabMaterialItemClick(i) {
      let _this = this;
      _this.activePageId = `material-manage-item-${i}`;
    },

    tabFeeItemClick(i) {
      let _this = this;
      _this.activePageId = `fee-manage-item-${i}`;
    },

    functionTaskMaterialManage(preproject) {
      openOrFocusTab(this, this.functionTaskMaterialList, preproject, 'material-manage-item');
    },
    closeFunction_material(index) {
      closeDynamicTab(this, {
        list: this.functionTaskMaterialList,
        index: index,
        prefix: 'material-manage-item',
        fallback: () => this.preActivePageId,
      });
    },

    functionFeeManage(preproject) {
      openOrFocusTab(this, this.functionFeeList, preproject, 'fee-manage-item');
    },
    closeFunction_fee(index) {
      closeDynamicTab(this, {
        list: this.functionFeeList,
        index: index,
        prefix: 'fee-manage-item',
        fallback: () => this.preActivePageId,
      });
    },

    funTaskManagement(preProject) {
      openOrFocusTab(this, this.projectTaskItems, preProject, 'task-manage');
    },
    closeFunction_develop(index) {
      closeDynamicTab(this, {
        list: this.projectTaskItems,
        index: index,
        prefix: 'task-manage',
        fallback: () => this.preActivePageId,
      });
    },
    funTaskManagerClick(i) {
      let _this = this;
      _this.activePageId = `task-manage-${i}`;
    },

    functionManage(aProject) {
      openOrFocusTab(this, this.preProject_function, aProject, 'pre-project-item');
    },
    closeFunction_function(index) {
      closeDynamicTab(this, {
        list: this.preProject_function,
        index: index,
        prefix: 'pre-project-item',
        fallback: () => this.preActivePageId,
        afterClose: () => this.getPreProjectListBySm(),
      });
    },

    systemFunctionManage(aProject) {
      openOrFocusTab(this, this.systemFunctions, aProject, 'system-function-item');
    },
    closeFunction_systemFunction(index) {
      closeDynamicTab(this, {
        list: this.systemFunctions,
        index: index,
        prefix: 'system-function-item',
        fallback: () => this.preActivePageId,
      });
    },

    systemFunctionView(aProject) {
      openOrFocusTab(this, this.systemFunctionsView, aProject, 'system-function-view-item');
    },
    closeFunction_systemFunctionView(index) {
      closeDynamicTab(this, {
        list: this.systemFunctionsView,
        index: index,
        prefix: 'system-function-view-item',
        fallback: () => this.preActivePageId,
      });
    },

    developArchiveShow(mixProject) {
      openOrFocusTab(this, this.projectArchiveItems, mixProject, 'archive-tree');
    },
    closeFunction_archive_data(index) {
      closeDynamicTab(this, {
        list: this.projectArchiveItems,
        index: index,
        prefix: 'archive-tree',
        fallback: () => this.preActivePageId,
      });
    },
    developArchiveClick(i) {
      let _this = this;
      _this.activePageId = `archive-tree-${i}`;
    },

    versionFormat(row, column) {
      return row.devVersion == null ? '' : String(row.devVersion);
    },

    rowClick(row, column, e) {
      let _this = this;
      _this.functionManage(row);
    },

    handleClose(done) {
      done(); //el-dialog用于关闭对话框的内置函数.
      this.getPreProjectListBySm();
    },

    getPreProjectListBySm() {
      let _this = this;
      let formData = new window.FormData();
      formData.append('customerId', _this.loginCustomer.id);
      formData.append('loginUserId', _this.loginUser.customerUserId);
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/pre-project/getPreProjectListBySm', formData).then((response) => {
        let resp = response.data;
        if (resp.success) {
          _this.preProjects = resp.content;
        } else {
          Toast.warning(resp.message, Swal);
        }
      })
    },

    getPreProjectListBySysgm() {
      let _this = this;
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/pre-project/getPreProjectListBySysgm/' + _this.loginCustomer.id).then((response) => {
        let resp = response.data;
        if (resp.success) {
          _this.preProjectsView = resp.content;
          _this.hasApplication = false;
          _this.preProjectsView.forEach((projectItem, index) => {
            if(projectItem.materialAppNum > 0){
              _this.hasApplication = true;
            }
            if(projectItem.feeAppNum > 0){
              _this.hasApplication = true;
            }
          })
        } else {
          Toast.warning(resp.message, Swal);
        }
      })
    },

    delete(preproject) {
      let _this = this;
      let text = `是否确认删除"${preproject.preProjectName}"？`;
      Swal.fire({
        title: '确认？',
        text: text,
        icon: 'warning',
        showCancelButton: true,
        confirmButtonColor: '#409EFF',
        cancelButtonColor: '#909399',
        confirmButtonText: '确认!',
        cancelButtonText: '取消!'
      }).then((result) => {
        if (result.value) {
          _this.$ajax.delete(process.env.VUE_APP_SERVER + '/customer/pre-project/del/' + preproject.id).then((response) => {
            let resp = response.data;
            if (resp.success) {
              Toast.success("删除成功!", Swal);
              _this.getPreProjectListBySm();
            } else {
              Toast.warning(resp.message, Swal);
            }
          })
        }
      })
    },

    tableRowClass({row, rowIndex}) {
      // console.log("row:", Date.parse(row.planCompleteTime));
      if (Date.now() > Date.parse(row.releaseTime)) {
        return 'warning-row-3';
      }
      return '';
    }
  }
}
</script>

<style lang="scss">
@import './../../../../public/static/scss/caipd.scss';

.el-badge__content.is-dot {
  height: 10px;
  width: 10px;
  padding: 0;
  right: -2px !important;
  border-radius: 50%;
}
</style>
