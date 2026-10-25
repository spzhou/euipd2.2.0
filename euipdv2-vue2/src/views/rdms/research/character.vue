<!--
 * Author: 周朔鹏
 * Date: 2025-03-24
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
                预立项项目列表
              </a>
            </li>

            <!--特性管理-->
            <DynamicTabItem
                v-for="(item, index) in preProjectItemList"
                :key="item.id"
                :index="index"
                :active-page-id="activePageId"
                prefix="project-character-item"
                :close="closeFunction_manage"
                @select="tabManageClick">{{ ('预立项 : ' + (item.preProjectName)) }}
            </DynamicTabItem>

            <!--需求清单 管理-->
            <DynamicTabItem
                v-for="(item, index) in preProjectItems_demand"
                :key="item.id"
                :index="index"
                :active-page-id="activePageId"
                prefix="list-demand-items"
                :close="closeFunction_demand"
                @select="tabDemandListClick">{{ ('需求 : ' + (item.preProjectName)) }}
            </DynamicTabItem>

            <!--迭代管理-->
            <DynamicTabItem
                v-for="(item, index) in preProjectItems_update_container"
                :key="item.id"
                :index="index"
                :active-page-id="activePageId"
                prefix="list-update-container-items"
                :close="closeFunction_update_container"
                @select="tabUpdateContainerListClick">{{ ('迭代 : ' + (item.preProjectName)) }}
            </DynamicTabItem>
          
        </template>

                        <DynamicTabPane
                page-id="home"
                :active-page-id="activePageId">
                            <template v-slot:default>
              <p>预立项管理: 是为<strong>产品经理</strong>提供的特性定义与规划界面；在此进行特性定义与特性管理，正式评审通过后可入库至特性仓库.</p>
              <div class="hr hr-18 dotted hr-double"></div>
              <div>
                <div style="display: flex; justify-content: space-between;">
                  <pagination ref="pagination" v-bind:list="list" v-bind:itemCount="6"></pagination>
                  <div style="display: flex; align-items: center;">
                    <el-checkbox v-model="showClosed" @change="toggleShowClosed" style="margin-right: 15px;">只看已关闭</el-checkbox>
                    <el-tooltip content="创建新的预立项项目,用于在正式立项之前对项目特性进行定义和管理" placement="top" effect="light" :open-delay=1000>
                      <el-button type="primary" size="small" style="margin-right: 15px;" @click="createPreProject()">
                        新建预立项项目
                      </el-button>
                    </el-tooltip>
                  </div>
                </div>

                <el-table
                    :data="preProjects"
                    border
                    stripe
                    highlight-current-row
                    style="width: 100%"
                    @row-click="rowClick">
                  <el-table-column
                      prop="projectCode"
                      align="center"
                      label="项目编号"
                      width="180"
                      show-overflow-tooltip
                  ></el-table-column>
                  <el-table-column
                      prop="preProjectName"
                      align="center"
                      min-width="200"
                      label="预立项项目名称"
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
                      prop="systemManagerName"
                      align="center"
                      label="系统工程师"
                      min-width="100"
                      show-overflow-tooltip
                  ></el-table-column>
                  <el-table-column
                      prop="devVersion"
                      align="center"
                      :formatter="versionFormat"
                      label="开发版本"
                      min-width="100"
                  ></el-table-column>
                  <el-table-column
                      prop="lifecycleStatus"
                      align="center"
                      :formatter="lifecycleStatusFormat"
                      label="生命周期"
                      min-width="100"
                  ></el-table-column>
                  <el-table-column
                      prop="createTimeStr"
                      align="center"
                      label="创建时间"
                      min-width="160"
                  ></el-table-column>
                  <el-table-column
                      fixed="right"
                      label="特性管理"
                      align="center"
                      width="620">
                    <template slot-scope="scope">
                      <el-tooltip content="对已定义的特性条目进行管理，可编辑、审批等" placement="top" effect="light" :open-delay=1000>
                        <el-button type="primary" plain size="small" v-on:click.stop="projectManagement(scope.row)">
                          <el-badge :is-dot="scope.row.handlingItems > 0" >
                            特性管理
                          </el-badge>
                        </el-button>
                      </el-tooltip>
                      <template v-if="scope.row.lifecycleStatus !== PRE_PROJECT_LIFECYCLE.CLOSED.key">
                      <el-tooltip content="显示全部提交给产品经理的客户需求，产品经理可以查看需求详情，并定义产品特性" placement="top" effect="light" :open-delay=1000>
                        <el-button type="primary" plain size="small" v-on:click.stop="listDemandItems(scope.row)">
                          <el-badge :is-dot="scope.row.demandHandlingItems >0" >
                            需求管理
                          </el-badge>
                        </el-button>
                      </el-tooltip>
                      <el-tooltip content="对已经完成开发的特性, 进行迭代管理." placement="top" effect="light" :open-delay=1000>
                        <el-button v-if="scope.row.rdType !== RD_TYPE.MODULE.key" type="primary" plain size="small" v-on:click.stop="updateManageContainer(scope.row)">
                          <el-badge :is-dot="scope.row.updateItems >0" >
                            升级/迭代
                          </el-badge>
                        </el-button>
                      </el-tooltip>
                      <el-tooltip content="直接通过任务提出需求, 请执行人对相应特性进行定义. 产品经理可以发任务给自己,直接定义特性." placement="top" effect="light" :open-delay=1000>
                        <el-button type="primary" plain size="small" v-on:click.stop="directJobItem(scope.row)">
                          特性定义
                        </el-button>
                      </el-tooltip>
                      <el-tooltip v-if="scope.row.rdType !== RD_TYPE.MODULE.key" content="根据预立项定义的特性进行研发立项，立项后将进入正式研发过程" placement="top" effect="light" :open-delay=1000>
                        <el-button type="primary" plain size="small" v-on:click.stop="setupApplication(scope.row, RD_TYPE.DEVELOPMENT.key)">
                          发起立项
                        </el-button>
                      </el-tooltip>
                      <el-tooltip content="对预立项项目信息进行编辑" placement="top" effect="light" :open-delay=1000>
                        <el-button type="primary" plain size="small" v-on:click.stop="editPreProject(scope.row)">
                          编辑
                        </el-button>
                      </el-tooltip>
                      </template>
                      <el-tooltip content="关闭未立项的预立项（已立项不可关闭）" placement="top" effect="light" :open-delay=1000>
                        <el-button
                            v-if="scope.row.lifecycleStatus !== PRE_PROJECT_LIFECYCLE.CONVERTED.key && scope.row.lifecycleStatus !== PRE_PROJECT_LIFECYCLE.CLOSED.key"
                            type="warning"
                            plain
                            size="small"
                            v-on:click.stop="closePreProject(scope.row)">
                          关闭
                        </el-button>
                      </el-tooltip>
                    </template>
                  </el-table-column>
                </el-table>
              </div>
            
            
                            </template></DynamicTabPane>

            <!-- 特性管理-->
            <DynamicTabPane
                v-for="(item, index) in preProjectItemList"
                :key="('manager-' + (item.id))"
                :index="index"
                prefix="project-character-item"
                :active-page-id="activePageId">
                <template v-slot:default>
              <CharacterJoblistTab
                  :key="item.id"
                  :pre-project="item"
                  :project-type="PROJECT_TYPE.PRE_PROJECT.key"
                  :index="index"
                  :doc-type="DOC_TYPE.CHARACTER.key"
                  :entrance="ENTRANCE_CLASS.CHARACTER.key"
              ></CharacterJoblistTab>
            
                </template></DynamicTabPane>

            <!-- 需求清单 管理-->
            <DynamicTabPane
                v-for="(item, index) in preProjectItems_demand"
                :key="('demand-' + (item.id))"
                :index="index"
                prefix="list-demand-items"
                :active-page-id="activePageId">
                <template v-slot:default>
              <p>{{ ((item.preProjectName) + ' 的需求清单') }}</p>
              <div class="hr hr-18 dotted hr-double"></div>
              <DemandSubmittedList
                  :key="item.id"
                  :pre-project-id=item.id>
              </DemandSubmittedList>
            
                </template></DynamicTabPane>

            <!-- 升级 管理-->
            <DynamicTabPane
                v-for="(item, index) in preProjectItems_update_container"
                :key="('update-container-' + (item.id))"
                :index="index"
                prefix="list-update-container-items"
                :active-page-id="activePageId">
                <template v-slot:default>
              <p>{{ ((item.preProjectName) + ' 的迭代清单') }}</p>
              <div class="hr hr-18 dotted hr-double"></div>
              <CharacterJobitemDocContainer
                  :key="item.id"
                  :pre-project-id=item.id
                  :index="index"
                  :entrance="ENTRANCE_CLASS.CHARACTER.key"
              ></CharacterJobitemDocContainer>
            
                </template></DynamicTabPane>
          
    </WorkspaceFrame>

    <!--    创建预立项立项对话框-->
    <el-dialog
        :visible.sync="preProjectDialogVisible"
        width="29cm"
        :before-close="handleClose"
        :close-on-click-modal=false>
      <div slot="title" style="font-size: 20px;">{{ dialogTitle }}</div>
      <div v-if="preProjectDialogVisible">
        <project-prepare-create
            :id="'prepare-setup'"
            :pre-project="{}"
        ></project-prepare-create>
      </div>
    </el-dialog>

    <!--    申请立项 对话框-->
    <el-dialog
        :visible.sync="dialogVisible"
        width="29cm"
        :before-close="handleClose"
        :close-on-click-modal=false>
      <div slot="title" style="font-size: 20px;">{{ dialogTitle }}</div>
      <div v-if="dialogVisible">
        <project-setup-application
            :key="timeStamp"
            :pre-project="setupProjectItem"
            :close-function="closeDialog"
        ></project-setup-application>
      </div>
    </el-dialog>

    <!--    编辑对话框-->
    <el-dialog
        :visible.sync="editDialogVisible"
        width="29cm"
        :before-close="handleClose"
        :close-on-click-modal=false>
      <div slot="title" style="font-size: 20px;">{{ dialogTitle }}</div>
      <div v-if="editDialogVisible">
        <project-prepare-create
            :id="'prepare-edit'"
            :key="timeStamp"
            :pre-project="project"
            :callback="closeDialog"
        ></project-prepare-create>
      </div>
    </el-dialog>

    <!--特性定义任务-->
    <el-dialog
        width="29cm"
        :visible.sync="directJobItemDlg"
        :before-close="handleClose"
        :close-on-click-modal=false>
      <div slot="title" style="font-size: 20px;">{{ dialogTitle }}</div>
      <div v-if="directJobItemDlg">
        <JobitemCharacterDefineDlg
            :key="((timeStamp) + '-' + (project.id))"
            :demand-id-list="null"
            :pre-project-id="project.id"
            :project-type="PROJECT_TYPE.PRE_PROJECT.key"
            :close-dlg = closeDialog
        ></JobitemCharacterDefineDlg>
      </div>
    </el-dialog>

    <!--    预立项信息 对话框-->
    <el-dialog
        :visible.sync="preprojectDialogVisible"
        width="29cm"
        :before-close="handleClose"
        :close-on-click-modal=false>
      <div slot="title" style="font-size: 20px;">{{ dialogTitle }}</div>
      <div v-if="preprojectDialogVisible">
        <PreprojectDetailDlg
            :key="project.id"
            :pre-project="project"
            :close-function="closeDialog"
        ></PreprojectDetailDlg>
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
import CharacterJoblistTab from "@/components/rdms/character-joblist-tab";
import ProjectPrepareCreate from "@/components/rdms/project-prepare-create";
import ProjectSetupApplication from "@/components/rdms/project-setup-application";
import CharacterAppend from "@/components/rdms/character-append";
import DemandSubmittedList from "@/components/rdms/demand-submitted-list";
import JobitemCharacterDefineDlg from "@/components/std/jobitem-character-define-dlg.vue";
import CharacterJobitemDocContainer from "@/components/rdms/character-jobitem-doc-container.vue";
import PreprojectDetailDlg from "@/components/rdms/preproject-detail-dlg.vue";

import File from "@/components/rdms/file";
import Swal from "sweetalert2";
import { fixed2fractional } from "@/filter/filter";

export default {
  components: {
    Pagination,
    File,
    DynamicTabItem,
    DynamicTabPane,
    WorkspaceFrame,
    CharacterJoblistTab,
    ProjectPrepareCreate,
    ProjectSetupApplication,
    CharacterAppend,
    DemandSubmittedList,
    JobitemCharacterDefineDlg,
    CharacterJobitemDocContainer,
    PreprojectDetailDlg,
  },
  name: "character",
  data: function () {
    return {
      loginUser: {},
      loginCustomer: {},

      activePageId: 'home',
      preProject: {},
      preProjects: [],
      showClosed: false,
      preProjectItemList: [],

      preProjectItems_demand: [],
      preProjectItems_update: [],
      preProjectItems_update_container: [],

      dialogVisible: false,
      editDialogVisible: false,
      preProjectDialogVisible: false,
      directJobItemDlg: false,
      preprojectDialogVisible: false,

      PROJECT_TYPE: PROJECT_TYPE,
      DOC_TYPE: DOC_TYPE,
      ENTRANCE_CLASS: ENTRANCE_CLASS,
      RD_TYPE: RD_TYPE,
      PRE_PROJECT_LIFECYCLE: PRE_PROJECT_LIFECYCLE,

      dialogTitle: 'Dialog',
      project: {},
      setupProjectItem: {}, //用于处理立项数据

      timeStamp: '',

      notifyCharacter: false,
      notifyDemand: false,

      //枚举类型的使用
      dialogFlag: {
        type: String,
        validator: function (value) {
          return ["SETUP", "CREATE", "EDIT"].includes(value);
        },
      },
    }
  },
  mounted: function () {
    let _this = this;
    _this.getLoginUser();
    _this.$refs.pagination.size = 12;
    _this.list(1);

    _this.$bus.$on('close-character-functionManage-tab', (index)=>{
      _this.closeFunction_manage(index);
    });
    _this.unsubscribeCharacter = _this.$bus.subscribe(['character'], function () {
      _this.list(1);
    });
  },

  beforeDestroy: function () {
    let _this = this;
    _this.$bus.$off('close-character-functionManage-tab');
    if (_this.unsubscribeCharacter) {
      _this.unsubscribeCharacter();
    }
  },

  computed: {
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
    },

    //tab点击事件
    tabManageClick(i) {
      let _this = this;
      _this.activePageId = `project-character-item-${i}`;
    },

    tabDemandListClick(i) {
      let _this = this;
      _this.activePageId = `list-demand-items-${i}`;
    },

    tabUpdateContainerListClick(i) {
      let _this = this;
      _this.activePageId = `list-update-container-items-${i}`;
    },

    versionFormat(row ) {
      return row.devVersion == null ? '' : String(row.devVersion);
    },

    lifecycleStatusFormat(row) {
      const key = row.lifecycleStatus;
      if (!key || !PRE_PROJECT_LIFECYCLE) {
        return '';
      }
      for (const k in PRE_PROJECT_LIFECYCLE) {
        if (PRE_PROJECT_LIFECYCLE[k].key === key) {
          return PRE_PROJECT_LIFECYCLE[k].value;
        }
      }
      return key;
    },

    closePreProject(row) {
      let _this = this;
      Swal.fire({
        title: '确认',
        text: `是否关闭预立项「${row.preProjectName}」？关闭后移入“已关闭”列表。`,
        icon: 'warning',
        showCancelButton: true,
        confirmButtonColor: '#409EFF',
        cancelButtonColor: '#909399',
        confirmButtonText: '确认!',
        cancelButtonText: '取消!'
      }).then((result) => {
        if (result.value || result.isConfirmed) {
          _this.requestClosePreProject(row, false);
        }
      });
    },

    requestClosePreProject(row, cascade) {
      let _this = this;
      let formData = new window.FormData();
      formData.append('cascade', cascade);
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/pre-project/close/' + row.id, formData).then((response) => {
        let resp = response.data;
        if (resp.success) {
          Toast.success(resp.message || '关闭成功', Swal);
          _this.list(1);
          return;
        }
        if (resp.code !== 'PRE_PROJECT_CLOSE_OPEN_JOBS') {
          Toast.warning(resp.message, Swal);
          return;
        }
        Swal.fire({
          title: '仍有未完成的工单',
          text: resp.message,
          icon: 'warning',
          showCancelButton: true,
          confirmButtonColor: '#F56C6C',
          cancelButtonColor: '#909399',
          confirmButtonText: '一并关闭',
          cancelButtonText: '取消'
        }).then((again) => {
          if (again.isConfirmed) {
            _this.requestClosePreProject(row, true);
          }
        });
      });
    },

    toggleShowClosed() {
      this.list(1);
    },

    rowClick(row ){
      let _this = this;
      _this.preprojectInfoShowDlg(row);
    },

    tabHomeClick() {
      let _this = this;
      _this.activePageId = 'home';
      _this.list(1);
    },

    projectManagement(preProject) {
      openOrFocusTab(this, this.preProjectItemList, preProject, 'project-character-item');
    },
    closeFunction_manage(index) {
      closeDynamicTab(this, {
        list: this.preProjectItemList,
        index: index,
        prefix: 'project-character-item',
        fallback: 'home',
        afterClose: () => this.list(1),
      });
    },

    listDemandItems(preProject) {
      openOrFocusTab(this, this.preProjectItems_demand, preProject, 'list-demand-items');
    },
    closeFunction_demand(index) {
      closeDynamicTab(this, {
        list: this.preProjectItems_demand,
        index: index,
        prefix: 'list-demand-items',
        fallback: 'home',
        afterClose: () => this.list(1),
      });
    },

    updateManageContainer(preProject) {
      openOrFocusTab(this, this.preProjectItems_update_container, preProject, 'list-update-container-items');
    },
    closeFunction_update_container(index) {
      closeDynamicTab(this, {
        list: this.preProjectItems_update_container,
        index: index,
        prefix: 'list-update-container-items',
        fallback: 'home',
        afterClose: () => this.list(1),
      });
    },

    handleClose(done) {
      done(); //el-dialog用于关闭对话框的内置函数.
      this.list(1);
    },

    setupApplication(row, type) {
      let _this = this;
      _this.preProject = row
      _this.timeStamp = Date.now();
      _this.dialogVisible = true;
      _this.dialogFlag = 'SETUP';
      if(type === RD_TYPE.DEVELOPMENT.key){
        _this.dialogTitle = "创建立项申请条目(创建后请在[立项管理]中进行操作)";
      }else{
        _this.dialogTitle = "创建立项申请条目(创建后请在[立项管理]中进行操作)";
      }
      _this.setupProjectItem.id = _this.preProject.id;
      _this.setupProjectItem.customerId = _this.preProject.customerId;
      _this.setupProjectItem.projectName = _this.preProject.preProjectName;
      _this.setupProjectItem.devVersion = _this.preProject.devVersion;
      _this.setupProjectItem.projectIntroduce = _this.preProject.preProjectIntroduce;
      _this.setupProjectItem.productManagerId = _this.preProject.productManagerId;
      _this.setupProjectItem.rdType = type;

    },

    createPreProject() {
      let _this = this;
      _this.project = {};
      _this.preProjectDialogVisible = true;
      _this.dialogFlag = 'CREATE';
      _this.dialogTitle = "创建预立项项目";
    },

    editPreProject(project) {
      let _this = this;
      _this.timeStamp = Date.now();
      _this.project = project;
      _this.editDialogVisible = true;
      _this.dialogFlag = 'EDIT';
      _this.dialogTitle = "编辑预立项项目";
    },

    directJobItem(project) {
      let _this = this;
      _this.project = project;
      _this.timeStamp = Date.now();
      _this.directJobItemDlg = true;
      _this.dialogTitle = "直接创建特性定义任务";
    },

    preprojectInfoShowDlg(project) {
      let _this = this;
      _this.project = project;
      _this.preprojectDialogVisible = true;
      _this.dialogTitle = "预立项项目信息";
    },

    list(page, retried) {
      let _this = this;
      // 列表页签未选中时不渲染分页组件；切回列表页签会重新查询
      if (!_this.$refs.pagination) {
        if (_this.activePageId === 'home' && !retried) {
          _this.$nextTick(() => _this.list(page, true));
        }
        return;
      }
      // Loading.show();
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/pre-project/list', {
        page: page,
        size: _this.$refs.pagination.size,
        keyWord: _this.loginCustomer.id, //customerId
        actor: _this.loginUser.customerUserId,
        status: _this.showClosed ? PRE_PROJECT_LIFECYCLE.CLOSED.key : null,
      }).then((response) => {
        // Loading.hide();
        let resp = response.data;
        if (resp.success) {
          _this.preProjects = resp.content.list;
          _this.$refs.pagination.render(page, resp.content.total);
        } else {
          Toast.warning(resp.message, Swal);
          // Loading.hide();
        }
      })
    },

    closeDialog() {
      let _this = this;
      _this.dialogVisible = false;
      _this.editDialogVisible = false;
      _this.directJobItemDlg = false;
      _this.preprojectDialogVisible = false;
      _this.list(1);
    },
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
