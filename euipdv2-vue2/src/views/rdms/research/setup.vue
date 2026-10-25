<!--
 * Author: 周朔鹏
 * Date: 2025-02-26
 * Email: 391902958@qq.com
 * Website: https://www.euipd.com
-->

<template>
  <div>
    <div>
      <!--Tab 卡片-->
            <WorkspaceFrame>
          <template v-slot:nav>
              <li :class="{active : activePageId==='home'}">
                <a @click="tabHomeClick">
                  <i class="green ace-icon fa fa-home bigger-120"></i>
                  项目申请列表
                </a>
              </li>
              <li :class="{active : activePageId==='setuped'}">
                <a @click="tabSetupedClick">
                  已立项项目列表
                </a>
              </li>
              <li :class="{active : activePageId==='milestone'}">
                <a @click="tabMilestoneClick">
                  里程碑汇总表
                </a>
              </li>

              <!--编辑 操作页面-->
              <DynamicTabItem
                  v-for="(item, index) in editProjectItems"
                  :key="item.id"
                  :index="index"
                  :active-page-id="activePageId"
                  prefix="edit-item"
                  :close="closeFunction_edit"
                  @select="tabEditClick">{{ ((item.projectName) + '-编辑') }}
              </DynamicTabItem>

              <!--概要 已立项-->
              <DynamicTabItem
                  v-for="(item, index) in detailProjectItems"
                  :key="item.id"
                  :index="index"
                  :active-page-id="activePageId"
                  prefix="detail-item"
                  :close="closeFunction_detail_setuped"
                  @select="tabDetailClick">{{ ((item.projectName) + '-详情') }}
              </DynamicTabItem>

              <!--仪表盘-->
              <DynamicTabItem
                  v-for="(item, index) in summaryProjectItems"
                  :key="item.id"
                  :index="index"
                  :active-page-id="activePageId"
                  prefix="summary-item"
                  :close="closeFunction_summary_setuped"
                  @select="tabSummaryClick">{{ ((item.projectName) + '-仪表盘') }}
              </DynamicTabItem>

              <!--概要 申请立项-->
              <DynamicTabItem
                  v-for="(item, index) in detailProjectItems_setup"
                  :key="item.id"
                  :index="index"
                  :active-page-id="activePageId"
                  prefix="detail-setup-item"
                  :close="closeFunction_detail_setup"
                  @select="tabDetailClick_setup">{{ ((item.projectName) + '-详情') }}
              </DynamicTabItem>
            
          </template>

                            <DynamicTabPane
                  page-id="home"
                  :active-page-id="activePageId">
                                <template v-slot:default>
                <div style="display: flex; flex-direction: row; justify-content: space-between; align-items: center;">
                  <div>
                    <p>项目申请管理: 是给 <strong>IPMT/ITMT</strong> 提供的一个综合管理界面. </p>
                  </div>
                  <div style="display: flex; flex-direction: row; justify-content: center; align-items: center;">
                    <div v-permission="'520103'">
                      <el-button type="primary" size="small" style="margin-right: 15px;" @click="createDirectProject()">创建直接立项项目</el-button>
                    </div>
                    <div v-if="respContent.status === null">
                      <el-tooltip content="创建特性开发项目, 特性开发项目只能创建一个" placement="top" effect="light" :open-delay=1000>
                        <el-button type="primary" size="small" style="margin-right: 15px;" @click="createModuleProject()">创建特性开发项目</el-button>
                      </el-tooltip>
                    </div>
                  </div>
                </div>

                <div class="hr hr-18 dotted hr-double"></div>
                <div>
                  <div style="display: flex; justify-content: space-between;">
                    <div>
<!--                    <pagination ref="pagination" v-bind:list="list" v-bind:itemCount="6"></pagination>-->
                    </div>
                    <div></div>
<!--                    <div style="display: flex; flex-direction: row; justify-content: center; align-items: center;">
                      <div>
                        <el-tooltip content="科研类项目或已有项目可以直接创建项目,并进行后续操作" placement="top" effect="light" :open-delay=1000>
                          <el-button type="primary" size="medium" style="margin-right: 15px;" @click="createDirectProject()">创建直接立项项目</el-button>
                        </el-tooltip>
                      </div>
                      <div v-if="respContent.status === null">
                        <el-tooltip content="创建特性开发项目, 特性开发项目只能创建一个" placement="top" effect="light" :open-delay=1000>
                          <el-button type="primary" size="medium" style="margin-right: 15px;" @click="createModuleProject()">创建特性开发项目</el-button>
                        </el-tooltip>
                      </div>
                    </div>-->
                  </div>
                  <el-table
                      :data="projects"
                      border
                      stripe
                      highlight-current-row
                      style="width: 100%"
                      @row-click="rowClick_setup">
                    <el-table-column  label="序号" align="center" width="50">
                      <template slot-scope="scope">
                        {{scope.$index+1}}
                      </template>
                    </el-table-column>
                    <el-table-column
                        prop="projectName"
                        align="center"
                        min-width="250"
                        show-overflow-tooltip
                        label="项目名称"
                    ></el-table-column>
                    <el-table-column
                        prop="productManagerName"
                        align="center"
                        label="产品经理(前端)"
                        min-width="150"
                        show-overflow-tooltip
                    ></el-table-column>
                    <el-table-column
                        prop="systemManagerName"
                        align="center"
                        label="系统工程师"
                        min-width="100"
                    ></el-table-column>
                    <el-table-column
                        prop="superviseName"
                        align="center"
                        label="监管委员"
                        min-width="150"
                        show-overflow-tooltip
                    ></el-table-column>
                    <el-table-column
                        prop="rdType"
                        align="center"
                        :formatter="rdTypeFormat"
                        label="研发类型"
                        min-width="150"
                        show-overflow-tooltip
                    ></el-table-column>
                    <el-table-column
                        prop="createTimeStr"
                        align="center"
                        label="申请时间"
                        min-width="150"
                        show-overflow-tooltip
                    ></el-table-column>
                    <el-table-column
                        prop="status"
                        align="center"
                        :formatter="statusFormat"
                        label="操作状态"
                        min-width="120"
                        show-overflow-tooltip
                    ></el-table-column>
                    <el-table-column
                        fixed="right"
                        label="操作"
                        align="center"
                        :width="getOpeAreaWidth">
                      <template slot-scope="scope">
                        <el-tooltip content="对预立项项目信息进行编辑" placement="top" effect="light" :open-delay=1000>
                          <el-button type="primary" plain size="small" v-on:click.stop="detailProject_setup(scope.row)">
                            概要
                          </el-button>
                        </el-tooltip>
                        <el-tooltip content="根据预立项定义的功能、特性进行研发立项，立项后将进入正式研发过程" placement="top" effect="light" :open-delay=1000>
                          <el-button v-if="!(scope.row.rdType === RD_TYPE.MODULE.key && respContent.flag === 'hasSetupedModuleProject')" type="primary" plain size="small" v-on:click.stop="setupApprove(scope.row)">
                            立项
                          </el-button>
                        </el-tooltip>
                        <el-tooltip content="将本项目的相关功能和特性追加到已立项的项目" placement="top" effect="light" :open-delay=1000>
                          <el-button v-if="scope.row.rdType !== RD_TYPE.DIRECT.key" type="primary" plain size="small" v-on:click.stop="setupAppend(scope.row)">
                            追加
                          </el-button>
                        </el-tooltip>
<!--                        备注: 已经使用页面内部的 驳回 按钮了 -->
<!--                        <el-tooltip content="不同意立项申请" placement="top" effect="light" :open-delay=1000>
                          <el-button v-if="scope.row.rdType !== RD_TYPE.DIRECT.key" type="warning" plain size="small" v-on:click.stop="reject(scope.row)">
                            驳回
                          </el-button>
                        </el-tooltip>-->
                      </template>
                    </el-table-column>
                  </el-table>
                </div>
              
              
                                </template></DynamicTabPane>

              <!--已经立项项目列表页面-->
                            <DynamicTabPane
                  page-id="setuped"
                  :active-page-id="activePageId">
                                <template v-slot:default>
                <div style="display: flex; flex-direction: row; justify-content: space-between; align-items: center;">
                  <p>已立项项目管理: 是给 <strong>IPMT/ITMT/产品负责人</strong> 提供的一个综合管理界面. </p>
                  <div v-permission="'520101'">
                    <el-button v-on:click="addCategoryItem()" size="small">添加分类</el-button>
                    <el-button v-on:click="editCategorys()" size="small">管理分类</el-button>
                  </div>
                </div>
                <div class="hr hr-8 dotted hr-double"></div>
                <el-tabs v-model="editableTabsValue" type="card" @tab-click="handleClick">
                  <el-tab-pane
                      :key="item.name"
                      v-for="item in editableTabs"
                      :label="item.title"
                      :name="item.name">
                    <ProjectSetupedAll
                        v-if="activePageId === 'setuped' && editableTabsValue === item.name"
                        :key="((categoryId) + '-' + (timeStamp))"
                        :category-id="categoryId"
                        :detail-project_setuped="detailProject_setuped"
                        :summary-project_setuped="summaryProject_setuped"
                        :edit-project="editProject"
                        :abandon-project="abandonProject"
                    ></ProjectSetupedAll>
                  </el-tab-pane>
                </el-tabs>
              
              
                                </template></DynamicTabPane>

              <!--已经立项项目列表页面-->
                            <DynamicTabPane
                  page-id="milestone"
                  :active-page-id="activePageId">
                                <template v-slot:default>
                <p>里程碑汇总表: 是给 <strong>产品经理/IPMT</strong> 提供的相关历程碑信息和相关立项时间节点参考的页面. </p>
                <div class="hr hr-18 dotted hr-double"></div>
                <div style="display: flex; justify-content: space-between; align-items: center;">
                  <div style="display: flex; flex-direction: row; justify-content: flex-start; align-items: center;">
                    <pagination ref="pagination_milestone" v-bind:list="getMilestoneList" v-bind:itemCount="6"></pagination>
                    <div>
                      <em><small>( 备注: 条目数不包含逾期里程碑 )</small></em>
                    </div>
                  </div>
                  <div style="margin-right: 20px;">
<!--                    <el-input
                        v-model="search"
                        size="mini"
                        suffix-icon="el-icon-search"
                        placeholder="输入关键词搜索">
                    </el-input>-->
                    <div>
                      <el-input size="mini" placeholder="请输入项目名称关键词" v-model="keyWord">
                        <el-button slot="append" icon="el-icon-search" @click="getMilestoneList(1)"></el-button>
                      </el-input>
                    </div>
                  </div>
                </div>
                <div>
                  <el-table
                      :data="milestoneList"
                      border
                      highlight-current-row
                      :row-class-name="tableRowClass"
                      style="width: 100%">
                    <el-table-column  label="序号" align="center" width="50">
                      <template slot-scope="scope">
                        {{scope.$index+1}}
                      </template>
                    </el-table-column>
                    <el-table-column
                        prop="projectCode"
                        align="center"
                        label="项目编号"
                        width="150"
                        show-overflow-tooltip
                    ></el-table-column>
                    <el-table-column
                        prop="projectName"
                        align="center"
                        label="项目名称"
                        min-width="200"
                        show-overflow-tooltip
                    ></el-table-column>
                    <el-table-column
                        prop="productManagerName"
                        align="center"
                        label="产品经理"
                        min-width="100"
                        show-overflow-tooltip
                    ></el-table-column>
                    <el-table-column
                        prop="milestoneName"
                        align="center"
                        label="里程碑名称"
                        min-width="100"
                        show-overflow-tooltip
                    ></el-table-column>
                    <el-table-column
                        prop="reviewTime"
                        :formatter="reviewTimeFormat"
                        label="评审时间"
                        align="center"
                        min-width="100"
                        show-overflow-tooltip
                    ></el-table-column>
                  </el-table>
                </div>
              
              
                                </template></DynamicTabPane>

              <!--编辑 操作-->
              <DynamicTabPane
                  v-for="(item, index) in editProjectItems"
                  :key="('edit-' + (item.id))"
                  :index="index"
                  prefix="edit-item"
                  :active-page-id="activePageId">
                  <template v-slot:default>
                <project-edit
                    v-if="activePageId === ('edit-item-' + (index))"
                    :key="('edit-' + (item.id))"
                    :project-id="item.id"
                    :index="index"
                    :close-function="closeFunction_edit"
                ></project-edit>
              
                  </template></DynamicTabPane>

              <!--概要 -->
              <DynamicTabPane
                  v-for="(item, index) in detailProjectItems"
                  :key="('detail-' + (item.id))"
                  :index="index"
                  prefix="detail-item"
                  :active-page-id="activePageId">
                  <template v-slot:default>
                <ProjectDetailInfo
                    v-if="activePageId === ('detail-item-' + (index))"
                    :key="('detail-' + (item.id))"
                    :x-project-id="item.id"
                    :project-type="PROJECT_TYPE.PROJECT.key"
                    :index="index"
                    :setup-stage="SETUP_STAGE.SETUPED.key"
                ></ProjectDetailInfo>
              
                  </template></DynamicTabPane>

              <!--仪表盘-->
              <DynamicTabPane
                  v-for="(item, index) in summaryProjectItems"
                  :key="('summary-item-' + (item.id))"
                  :index="index"
                  prefix="summary-item"
                  :active-page-id="activePageId">
                  <template v-slot:default>
                <ProjectItemSummary
                    v-if="activePageId === ('summary-item-' + (index))"
                    :key="('detail-' + (item.id))"
                    :project-id="item.id"
                    :project-type="PROJECT_TYPE.PROJECT.key"
                    :index="index"
                    :setup-stage="SETUP_STAGE.SETUPED.key"
                ></ProjectItemSummary>
              
                  </template></DynamicTabPane>

              <!--概要 setup-->
              <DynamicTabPane
                  v-for="(item, index) in detailProjectItems_setup"
                  :key="('detail-setup-' + (item.id))"
                  :index="index"
                  prefix="detail-setup-item"
                  :active-page-id="activePageId">
                  <template v-slot:default>
                <ProjectDetailInfo
                    v-if="activePageId === ('detail-setup-item-' + (index))"
                    :key="('detail-' + (item.id))"
                    :x-project-id="item.id"
                    :project-type="PROJECT_TYPE.PROJECT.key"
                    :index="index"
                    :setup-stage="SETUP_STAGE.SETUP.key"
                    :entrance="ENTRANCE_CLASS.SETUP.key"
                ></ProjectDetailInfo>
              
                  </template></DynamicTabPane>
            
      </WorkspaceFrame>
    </div>

    <!--    立项 对话框-->
    <el-dialog
        :visible.sync="dialogVisible"
        width="29cm"
        append-to-body
        :close-on-click-modal=false>
      <div slot="title" style="font-size: 20px;">{{ dialogTitle }}</div>
      <div v-if="dialogVisible">
        <div v-if="setupProject.rdType !== RD_TYPE.MODULE.key">
          <project-setup
              :id="setupProject.id"
              :key="('setup-' + (setupProject.id) + '-' + (timeStamp))"
              :project-id="setupProject.id"
              :index="0"
              :close-function="closeDialog"
          ></project-setup>
        </div>
        <div v-else>
          <ProjectSetupModule
              :id="setupProject.id"
              :key="('setup-' + (setupProject.id))"
              :project-id="setupProject.id"
              :index="0"
              :close-function="closeDialog"
          ></ProjectSetupModule>
        </div>
      </div>
    </el-dialog>

    <!--    创建直接立项项目 对话框-->
    <el-dialog
        :visible.sync="createDirectSetupDlg"
        width="29cm"
        :close-on-click-modal=false>
      <div slot="title" style="font-size: 20px;">{{ dialogTitle }}</div>
      <div v-if="createDirectSetupDlg">
        <ProjectCreateDirect
            :id="timeStamp"
            :key="('direct-' + (timeStamp))"
            :close-function="closeDialog"
        ></ProjectCreateDirect>
      </div>
    </el-dialog>

    <!--    创建自由开发项目 对话框-->
    <el-dialog
        :visible.sync="createModuleSetupDlg"
        width="29cm"
        :close-on-click-modal=false>
      <div slot="title" style="font-size: 20px;">{{ dialogTitle }}</div>
      <div v-if="createModuleSetupDlg">
        <ProjectCreateModule
            :id="timeStamp"
            :key="('direct-' + (timeStamp))"
            :close-function="closeDialog"
        ></ProjectCreateModule>
      </div>
    </el-dialog>

    <!--    项目功能追加-->
    <el-dialog
        :visible.sync="dialogAppendVisible"
        width="29cm"
        :close-on-click-modal=false>
      <div slot="title" style="font-size: 20px;">{{ dialogTitle }}</div>
      <div v-if="dialogAppendVisible">
        <project-append
            :key="setupProject.id"
            :project="setupProject"
            :index="0"
            :close-function="closeDialog"
        ></project-append>
      </div>
    </el-dialog>

    <!--    组件项目功能追加-->
    <el-dialog
        :visible.sync="dialogAppendModuleVisible"
        width="29cm"
        :close-on-click-modal=false>
      <div slot="title" style="font-size: 20px;">{{ dialogTitle }}</div>
      <div v-if="dialogAppendModuleVisible">
        <ProjectAppendModule
            :key="setupProject.id"
            :project="setupProject"
            :index="0"
            :close-function="closeDialog"
        ></ProjectAppendModule>
      </div>
    </el-dialog>

    <!--    驳回立项申请-->
    <el-dialog
        :visible.sync="dialogRejectVisible"
        width="29cm"
        :close-on-click-modal=false>
      <div slot="title" style="font-size: 20px;">{{ dialogTitle }}</div>
      <div v-if="dialogRejectVisible">
        <ProjectApplicationRejectDlg
            :key="setupProject.id"
            :project-id="setupProject.id"
            :close-dlg="closeDialog">
        </ProjectApplicationRejectDlg>
      </div>
    </el-dialog>

    <!--添加分类-->
    <el-dialog
        width="23cm"
        append-to-body
        :visible.sync="addCategoryItemDlg"
        :before-close="handleClose"
        :close-on-click-modal=false>
      <div slot="title" style="font-size: 20px;">{{ dialogTitle }}</div>
      <div v-if="addCategoryItemDlg">
        <CategoryProjectAddItemDlg
            :key="loginCustomer.id"
            :customer-id="loginCustomer.id"
            :close-dlg="closeDialog"
        ></CategoryProjectAddItemDlg>
      </div>
    </el-dialog>

    <!--管理分类-->
    <el-dialog
        width="23cm"
        append-to-body
        :visible.sync="editCategorysDlg"
        :before-close="handleClose"
        :close-on-click-modal=false>
      <div slot="title" style="font-size: 20px;">{{ dialogTitle }}</div>
      <div v-if="editCategorysDlg">
        <CategoryProjectEditDlg
            :key="loginCustomer.id"
            :customer-id="loginCustomer.id"
            :close-dlg="closeDialog"
        ></CategoryProjectEditDlg>
      </div>
    </el-dialog>

  </div>
</template>

<script>
import Pagination from "@/components/pagination";
import {closeDynamicTab, openOrFocusTab} from "@/shared/dynamic-tab";
import DynamicTabItem from "@/components/rdms/dynamic-tab-item";
import DynamicTabPane from "@/components/rdms/dynamic-tab-pane";
import WorkspaceFrame from "@/components/rdms/workspace-frame";
import ProjectPrepareEdit from "@/components/rdms/project-prepare-create";
import ProjectSetup from "@/components/rdms/project-setup";
import ProjectCreateDirect from "@/components/rdms/project-create-direct.vue";
import ProjectAppend from "@/components/rdms/project-append";
import CharacterAppend from "@/components/rdms/character-append";
import DemandSubmittedList from "@/components/rdms/demand-submitted-list";
import ProjectEdit from "@/components/rdms/project-edit";
import File from "@/components/rdms/file";
import Swal from "sweetalert2";
import ProjectDetailInfo from "@/components/rdms/project-detail-info.vue";
import { optionKV, shortDateFormat} from "@/filter/filter";
import ProjectApplicationRejectDlg from "@/components/std/project-application-reject-dlg.vue";
import ProjectCreateModule from "@/components/rdms/project-create-module.vue";
import ProjectSetupModule from "@/components/rdms/project-setup-module.vue";
import ProjectAppendModule from "@/components/rdms/project-append-module.vue";
import ProjectItemSummary from "@/components/rdms/project-item-summary.vue";
import { Loading } from 'element-ui'
import CategoryProjectAddItemDlg from "@/components/rdms/category-project-add-item-dlg.vue";
import ProjectSetupedAll from "@/components/rdms/project-setuped-all.vue";
import CategoryProjectEditDlg from "@/components/rdms/category-project-edit-dlg.vue";

export default {
  components: {
    ProjectSetupedAll,
    CategoryProjectAddItemDlg,
    Pagination,
    File,
    DynamicTabItem,
    DynamicTabPane,
    WorkspaceFrame,
    ProjectSetup,
    ProjectPrepareEdit,
    ProjectCreateDirect,
    ProjectAppend,
    CharacterAppend,
    DemandSubmittedList,
    ProjectEdit,
    ProjectDetailInfo,
    ProjectApplicationRejectDlg,
    ProjectCreateModule,
    ProjectSetupModule,
    ProjectAppendModule,
    ProjectItemSummary,
    CategoryProjectEditDlg,
  },
  name: "setup",
  data: function () {
    return {
      loginUser: {},
      loginCustomer: {},

      categoryId: "",

      activePageId: 'home',
      preActivePageId: 'home',
      keyWord: '',
      search: '',

      setupStage: SETUP_STAGE.SETUP.key,

      dialogVisible: false,
      dialogAppendVisible: false,
      dialogAppendModuleVisible: false,
      dialogRejectVisible: false,
      createDirectSetupDlg: false,
      createModuleSetupDlg: false,
      dialogTitle: 'Dialog',

      addCategoryItemDlg: false,
      editCategorysDlg: false,

      project: {},
      setupProject: {},

      setupProjectItems: [],
      appendProjectItems: [],
      editProjectItems: [],
      detailProjectItems: [],
      summaryProjectItems: [],
      detailProjectItems_setup: [],

      milestoneList: [],

      projects: [], //预立项项目列表
      setupedProjects: [], //预立项项目列表
      setupProjectItem: {}, //用于处理立项数据

      timeStamp: '',
      PROJECT_TYPE: PROJECT_TYPE,
      PROJECT_STATUS: PROJECT_STATUS,
      SETUP_STAGE: SETUP_STAGE,
      ENTRANCE_CLASS: ENTRANCE_CLASS,
      RD_TYPE: RD_TYPE,

      respContent: {},

      loadMilestone: false,

      categoryList: [],
      editableTabsValue: '',
      editableTabs: [],
    }
  },
  mounted: function () {
    let _this = this;
    _this.getLoginUser();
    _this.list(1);

    _this.$bus.$on('close-setup-detail-tab', (index) => {
      _this.closeFunction_detail_setup(index);
    });

    _this.unsubscribeSetup = _this.$bus.subscribe(['project'], function () {
      if (_this.activePageId === 'home') {
        _this.list(1);
      }
      if (_this.activePageId === 'setuped') {
        _this.getCategoryList_all_show(_this.loginCustomer.id);
      }
    });
    _this.timeStamp = new Date().getTime();
  },

  beforeDestroy(){
    let _this = this;
    _this.$bus.$off('close-setup-detail-tab');
    if (_this.unsubscribeSetup) {
      _this.unsubscribeSetup();
    }
  },

  computed: {
    getOpeAreaWidth(){
      return 300;
    },
  },

  methods: {
    getLoginUser(){
      let _this = this;
      if(Tool.getLoginUser().userGroup === LOGIN_USER_GROUP.CUSTOMER.key){
        _this.loginUser = Tool.getLoginUser().adminUser;
        _this.loginCustomer =Tool.getLoginUser();
      }else{
        _this.loginUser = Tool.getLoginUser();
        _this.loginCustomer = Tool.getLoginUser().customer;
      }

      _this.categoryId = _this.loginCustomer.id;
    },

    handleClick(tab, event){
      let _this = this;
      _this.categoryId = tab.name;
      // console.log("点击了:", tab.name);

    },

    reviewTimeFormat(row, column){
      return shortDateFormat(row.reviewTime);
    },
    rdTypeFormat(row, column) {
      return optionKV(RD_TYPE, row.rdType);
    },

    statusFormat(row, column) {
      return optionKV(PROJECT_STATUS, row.status);
    },

    rowClick_setup(row){
      let _this = this;
      _this.detailProject_setup(row);
    },

    rowClick_setuped(row){
      let _this = this;
      _this.detailProject_setuped(row);
    },

    tabEditClick(i) {
      let _this = this;
      _this.activePageId = `edit-item-${i}`;
    },

    tabDetailClick(i) {
      let _this = this;
      _this.activePageId = `detail-item-${i}`;
    },

    tabSummaryClick(i) {
      let _this = this;
      _this.activePageId = `summary-item-${i}`;
    },

    tabDetailClick_setup(i) {
      let _this = this;
      _this.activePageId = `detail-setup-item-${i}`;
    },

    tabHomeClick() {
      let _this = this;
      _this.setupStage = SETUP_STAGE.SETUP.key;
      _this.activePageId = 'home';
      _this.preActivePageId = 'home';
      _this.list(1);
    },

    tabSetupedClick() {
      let _this = this;
      _this.setupStage = SETUP_STAGE.SETUPED.key
      _this.activePageId = 'setuped';
      _this.preActivePageId = 'setuped';
      _this.getCategoryList_all_show(_this.loginCustomer.id);
    },

    tabMilestoneClick() {
      let _this = this;
      _this.activePageId = 'milestone';
      _this.preActivePageId = 'milestone';
      _this.$nextTick(function () {
        _this.$refs.pagination_milestone.size = 12;
        _this.getMilestoneList(1);
      });
    },

    setupApprove(row) {
      let _this = this;
      _this.timeStamp = new Date().getTime();
      _this.dialogTitle = "新建立项";
      _this.setupProject = row;
      _this.dialogVisible = true;
    },

    setupAppend(row) {
      let _this = this;
      _this.setupProject = row;
      if(_this.setupProject.rdType !== RD_TYPE.MODULE.key){
        _this.dialogTitle = "项目特性追加";
        _this.dialogAppendVisible = true;
      }else{
        _this.dialogTitle = "特性开发项目追加";
        _this.dialogAppendModuleVisible = true;
      }
    },

    reject(row) {
      let _this = this;
      _this.dialogTitle = "驳回立项申请";
      _this.setupProject = row;
      _this.dialogRejectVisible = true;
    },

    addCategoryItem() {
      let _this = this;
      _this.addCategoryItemDlg = true;
      _this.dialogTitle = "添加分类";
    },

    editCategorys() {
      let _this = this;
      _this.editCategorysDlg = true;
      _this.dialogTitle = "管理分类";
    },

    handleClose(done) {
      done();
    },

    closeDialog() {
      let _this = this;
      _this.dialogVisible = false;
      _this.dialogAppendVisible = false;
      _this.dialogAppendModuleVisible = false;
      _this.dialogRejectVisible = false;
      _this.createDirectSetupDlg = false;
      _this.createModuleSetupDlg = false;
      _this.addCategoryItemDlg = false;
      _this.editCategorysDlg = false;
    },

    createDirectProject() {
      let _this = this;
      if (typeof Tool !== 'undefined' && Tool.hasButtonPermission
          && !Tool.hasButtonPermission('520103')) {
        Toast.warning("没有权限", Swal);
        return;
      }
      _this.timeStamp = new Date().getTime();
      _this.dialogTitle = "创建直接立项项目";
      _this.createDirectSetupDlg = true;
    },

    createModuleProject() {
      let _this = this;
      _this.timeStamp = new Date().getTime();
      _this.dialogTitle = "创建特性开发项目";
      _this.createModuleSetupDlg = true;
    },

    getCategoryList_all_show(customerId) {
      let _this = this;
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/category-project/getCategoryList_all_show/' + customerId).then((response) => {
        let resp = response.data;
        if (resp.success) {
          _this.categoryList = [];
          _this.editableTabs = [];
          _this.categoryList = resp.content;
          _this.categoryList.forEach((item) => {
            _this.editableTabs.push({
              title: item.name,
              name: item.id,
            });
            _this.editableTabsValue = _this.editableTabs? _this.editableTabs[0].name : '';
          });
        } else {
          Toast.warning(resp.message, Swal);
        }
      })
    },

    editProject(project) {
      openOrFocusTab(this, this.editProjectItems, project, 'edit-item');
    },
    closeFunction_edit(index) {
      closeDynamicTab(this, {
        list: this.editProjectItems,
        index: index,
        prefix: 'edit-item',
        fallback: () => this.preActivePageId,
        afterClose: () => this.list(1),
      });
    },


    detailProject_setuped(project) {
      openOrFocusTab(this, this.detailProjectItems, project, 'detail-item');
    },
    closeFunction_detail_setuped(index) {
      closeDynamicTab(this, {
        list: this.detailProjectItems,
        index: index,
        prefix: 'detail-item',
        fallback: () => this.preActivePageId,
        afterClose: () => this.list(1),
      });
    },


    summaryProject_setuped(project) {
      openOrFocusTab(this, this.summaryProjectItems, project, 'summary-item');
    },
    closeFunction_summary_setuped(index) {
      closeDynamicTab(this, {
        list: this.summaryProjectItems,
        index: index,
        prefix: 'summary-item',
        fallback: () => this.preActivePageId,
        afterClose: () => this.list(1),
      });
    },

    detailProject_setup(project) {
      openOrFocusTab(this, this.detailProjectItems_setup, project, 'detail-setup-item');
    },
    closeFunction_detail_setup(index) {
      closeDynamicTab(this, {
        list: this.detailProjectItems_setup,
        index: index,
        prefix: 'detail-setup-item',
        fallback: () => this.preActivePageId,
        afterClose: () => this.list(1),
      });
    },

    list(page) {
      let _this = this;
      let formData = new window.FormData();
      formData.append('customerId', _this.loginCustomer.id);
      formData.append('loginUserId', _this.loginUser.customerUserId);
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/project/list-setup-project', formData).then((response) => {
        let resp = response.data;
        if (resp.success) {
          _this.respContent = resp.content;
          _this.projects = resp.content;
          // _this.$refs.pagination.render(page, resp.content.total);
        } else {
          Toast.warning(resp.message, Swal);
        }
      })
    },

/*    setupedList(page) {
      let _this = this;
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/project/list-setuped-project', {
        page: page,
        size: _this.$refs.pagination_setuped.size,
        customerId: _this.loginCustomer.id, //customerId
        actor: _this.loginUser.customerUserId,
        keyWord: _this.keyWord,
      }).then((response) => {
        let resp = response.data;
        if (resp.success) {
          _this.setupedProjects = resp.content.list;
          _this.$refs.pagination_setuped.render(page, resp.content.total);
        } else {
          Toast.warning(resp.message, Swal);
        }
      })
    },*/

    getMilestoneList(page) {
      let _this = this;
      let loadingInstance = Loading.service({ fullscreen: true });
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/milestone/customer-milestone-list', {
        page: page,
        size: _this.$refs.pagination_milestone.size,
        customerId: _this.loginCustomer.id, //customerId
        keyWord: _this.keyWord,
      }).then((response) => {
        let resp = response.data;
        loadingInstance.close();
        if (resp.success) {
          _this.milestoneList = resp.content.list;
          _this.$refs.pagination_milestone.render(page, resp.content.total);
        } else {
          Toast.warning(resp.message, Swal);
        }
      })
    },

    abandonProject(project) {
      let _this = this;
      let text = `是否确认废弃"${project.projectName}"？废弃项目将不在任何管理界面中显示.`;
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
          _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/project/abandonProject/' + project.id).then((response) => {
            let resp = response.data;
            if (resp.success) {
              Toast.success("项目已废弃!", Swal);
              // _this.setupedList(1);
              _this.timeStamp = new Date().getTime();
            } else {
              Toast.warning(resp.message, Swal);
            }
          })
        }
      })
    },

    delete(project) {
      let _this = this;
      let text = `是否确认删除"${project.preProjectName}"？`;
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
          _this.$ajax.delete(process.env.VUE_APP_SERVER + '/customer/pre-project/del/' + project.id).then((response) => {
            let resp = response.data;
            if (resp.success) {
              Toast.success("删除成功!", Swal);
              _this.list(1);
            } else {
              Toast.warning(resp.message, Swal);
            }
          })
        }
      })
    },

    tableRowClass({row, rowIndex}) {
      // console.log("row:", Date.parse(row.planCompleteTime));
      let nowDate = Date.now();
      let reviewDate = Date.parse(row.reviewTime);
      if (nowDate > reviewDate) {
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
