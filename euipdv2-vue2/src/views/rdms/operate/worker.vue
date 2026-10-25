<!--
 * Author: 周朔鹏
 * Date: 2025-03-25
 * Email: 391902958@qq.com
 * Website: https://www.euipd.com
-->

<template>
  <div>
    <!--Tab 卡片-->
    <div class="row">
      <div class="col-md-12">
        <div class="tabbable">
          <ul class="nav nav-tabs" id="myTab">
            <li :class="{active : activePageId==='home'}">
              <a @click="tabHomeClick">
                <i class="green ace-icon fa fa-home bigger-120"></i>
                员工列表
              </a>
            </li>
            <li :class="{active : activePageId==='job-level'}">
              <a @click="tabJobLevelClick">
                职级管理
              </a>
            </li>
            <!-- 后台直接给完全权限 -->
            <!-- <li :class="{active : activePageId==='group-authorize-manager'}">
              <a @click="tabAuthGroupManagerClick">
                权限管理
              </a>
            </li> -->
            <li :class="{active : activePageId==='edit-project-team'}">
              <a @click="tabEditTeamClick">
                资质设置
              </a>
            </li>
            <li :class="{active : activePageId==='edit-organization-manager'}">
              <a @click="tabOrganizationClick">
                组织关系
              </a>
            </li>
            <li :class="{active : activePageId==='stand-manhour'}">
              <a @click="tabStandManhourClick">
                标准费率
              </a>
            </li>
<!--            TODO 注释掉Auth管理 以后放到 systemSetting中去-->
            <li v-if="false" :class="{active : activePageId==='auth-manage'}">
              <a @click="tabAuthManageClick">
                Auth管理
              </a>
            </li>

          </ul>

          <div class="tab-content">
            <div id="home" class="tab-pane fade" :class="{active : activePageId === 'home', in : activePageId === 'home' }">
              <div style="display: flex; flex-direction: row; justify-content: space-between; align-items: center;">
                <div>
                  <p>人力资源: 是为人力资源部门提供的一套综合管理页面. </p>
                </div>
                <div>
                  <el-tooltip content="下载员工信息上传模板, 不要修改文件名称, 填写完整后上传." placement="top" effect="light" :open-delay=1000>
                    <el-button type="text" @click="downloadFile(templatePath)">下载员工信息表模板: import_workers_template.xlsx</el-button>
                  </el-tooltip>
                </div>
              </div>

              <div class="hr hr-18 dotted hr-double"></div>
              <div>
                <div style="display: flex; justify-content: space-between;">
                  <pagination id="worker-page" ref="pagination" v-bind:list="list" v-bind:itemCount="6"></pagination>
                  <div style="display: flex; align-items: center;">
                    <el-select size="mini" v-model="departmentId" clearable filterable placeholder="按部门筛选"
                               style="width: 160px; margin-right: 8px;" @change="list(1)">
                      <el-option label="未分配" value="UNASSIGNED"></el-option>
                      <el-option v-for="d in departmentList" :key="d.id" :label="d.departName" :value="d.id"></el-option>
                    </el-select>
                    <el-input size="mini" placeholder="请输入姓名搜索" v-model="keyWord">
                      <el-button slot="append" icon="el-icon-search" @click="search(1)"></el-button>
                    </el-input>
                  </div>
                  <div style="display: flex; flex-direction: row; justify-content: center; align-items: center;">
                    <el-tooltip content="通过手机号码添加员工, 添加后员工可以通过手机号码登录到本机构" placement="top" effect="light" :open-delay=1000>
                      <el-button type="primary" size="small" style="margin-right: 15px;" @click="addWorker()">
                        添加员工
                      </el-button>
                    </el-tooltip>
                    <el-tooltip content="通过导入完整填写员工信息的excel表格的方式添加系统用户" placement="top" effect="light" :open-delay=1000>
                      <ExcelFileUpload
                          :key="timeStamp"
                          :text="'导入员工'"
                          @import-success="importSuccess"
                      ></ExcelFileUpload>
                    </el-tooltip>
                  </div>
                </div>
                <el-table
                    :data="users"
                    border
                    stripe
                    highlight-current-row
                    style="width: 100%">
                  <el-table-column  label="序号" align="center" width="50">
                    <template slot-scope="scope">
                      {{scope.$index+1}}
                    </template>
                  </el-table-column>
                  <el-table-column
                      prop="jobNum"
                      align="center"
                      label="工号"
                      width="100"
                  ></el-table-column>
                  <el-table-column
                      prop="loginName"
                      align="center"
                      label="手机"
                      width="110"
                  ></el-table-column>
                  <el-table-column
                      prop="trueName"
                      align="center"
                      label="姓名"
                  ></el-table-column>
                  <el-table-column
                      prop="jobLevel"
                      align="center"
                      sortable
                      label="职级"
                  ></el-table-column>
                  <el-table-column
                      prop="title"
                      align="center"
                      label="职务"
                  ></el-table-column>
                  <el-table-column
                      prop="jobPosition"
                      align="center"
                      label="岗位名称"
                  ></el-table-column>
                  <el-table-column
                      prop="joinTimeStr"
                      align="center"
                      sortable
                      label="创建时间"
                  ></el-table-column>
                  <el-table-column
                      prop="departmentName"
                      align="center"
                      label="所在部门"
                  ></el-table-column>
<!--                  <el-table-column
                      prop="userRoleListStr"
                      align="center"
                      label="角色组名称"
                  ></el-table-column>-->
                  <el-table-column
                      prop="status"
                      align="center"
                      :formatter="statusFormat"
                      sortable
                      label="状态"
                  ></el-table-column>
                  <el-table-column
                      fixed="right"
                      label="操作"
                      align="center"
                      width="370">
                    <template slot-scope="scope">
                      <el-button v-on:click.stop="editWorker(scope.row)" type="primary" plain size="small">
                        编辑
                      </el-button>
<!--                      <el-button v-on:click.stop="authWorker(scope.row)" type="primary" plain size="small">
                        授权
                      </el-button>-->
                      <el-button v-if="!scope.row.isAdmin" v-on:click.stop="turnOver(scope.row)" type="primary" plain size="small">
                        移交
                      </el-button>
                      <el-button v-if="canResetPassword(scope.row)" v-on:click.stop="resetPassword(scope.row)" type="warning" plain size="small">
                        重置密码
                      </el-button>
                      <el-button v-if="!scope.row.isAdmin && !scope.row.isBoss" v-on:click.stop="del(scope.row.id)" type="warning" plain size="small">
                        删除
                      </el-button>
                    </template>
                  </el-table-column>
                </el-table>
              </div>
            </div>

            <div id="job-level" class="tab-pane fade" :class="{active : activePageId === 'job-level', in : activePageId === 'job-level' }">
              <p>职级管理</p>
              <div class="hr hr-18 dotted hr-double"></div>
              <job-level></job-level>
            </div>
            <div id="group-authorize-manager" class="tab-pane fade" :class="{active : activePageId === 'group-authorize-manager', in : activePageId === 'group-authorize-manager' }">
              <p>权限组管理</p>
              <div class="hr hr-18 dotted hr-double"></div>
              <user-role></user-role>
            </div>
            <div id="edit-project-team" class="tab-pane fade" :class="{active : activePageId === 'edit-project-team', in : activePageId === 'edit-project-team' }">
              <p>对项目管理资质人员进行设置</p>
              <div class="hr hr-18 dotted hr-double"></div>
              <EditProjectTeam ref="editProjectTeam"></EditProjectTeam>
            </div>
            <div id="edit-organization-manager" class="tab-pane fade"
                 :class="{active : activePageId === 'edit-organization-manager', in : activePageId === 'edit-organization-manager' }">
              <p>对组织架构进行设置和管理</p>
              <div class="hr hr-18 dotted hr-double"></div>
              <div v-if="loadOrganizationChart">
                <EditOrganizationChart></EditOrganizationChart>
              </div>
            </div>
            <div id="stand-manhour" class="tab-pane fade"
                 :class="{active : activePageId === 'stand-manhour', in : activePageId === 'stand-manhour' }">
              <p>编辑机构标准工时费率</p>
              <div class="hr hr-18 dotted hr-double"></div>
              <EditStandManhour></EditStandManhour>
            </div>
            <div id="auth-manage" class="tab-pane fade"
                 :class="{active : activePageId === 'auth-manage', in : activePageId === 'auth-manage' }">
              <p>编辑机构标准工时费率</p>
              <div class="hr hr-18 dotted hr-double"></div>
              <EditAuthManage></EditAuthManage>
            </div>

          </div>
        </div>
      </div>
    </div>
    <!-- 添加员工-->
    <el-dialog
        :visible.sync="addWorkerDialogVisible"
        width="29cm"
        :before-close="handleClose"
        :close-on-click-modal=false>
      <div slot="title" style="font-size: 20px;">{{ dialogTitle }}</div>
      <div v-if="addWorkerDialogVisible">
        <user-create-dlg
            :key="timeStamp"
            :close-function="closeDialog"
        ></user-create-dlg>
      </div>
    </el-dialog>

    <!-- 编辑开发者-->
    <el-dialog
        :visible.sync="editWorkerDialogVisible"
        width="29cm"
        :before-close="handleClose"
        :close-on-click-modal=false>
      <div slot="title" style="font-size: 20px;">{{ dialogTitle }}</div>
      <div v-if="editWorkerDialogVisible">
        <user-edit-dlg
            :key="worker.id"
            :user = worker
            :close-function = closeDialog
        ></user-edit-dlg>
      </div>
    </el-dialog>

    <!-- 员工授权-->
    <el-dialog
        :visible.sync="authWorkerDialogVisible"
        width="29cm"
        :before-close="handleClose"
        :close-on-click-modal=false>
      <div slot="title" style="font-size: 20px;">{{ dialogTitle }}</div>
      <div v-if="authWorkerDialogVisible">
        <user-authorize-dlg
            :key="worker.id"
            :worker="worker"
            :close-function = closeDialog
        ></user-authorize-dlg>
      </div>
    </el-dialog>

    <!-- 将工作移交给...-->
    <el-dialog
        :visible.sync="turnOverDialogVisible"
        width="23cm"
        :before-close="handleClose"
        :close-on-click-modal=false>
      <div slot="title" style="font-size: 20px;">{{ ('将 ' + (worker.trueName) + ' 的工作移交给 ...') }}</div>
      <div v-if="turnOverDialogVisible">
        <TurnOverSetting
            :id="worker.id"
            :key="worker.id"
            :worker-id="worker.id"
            :close-function = closeDialog
        ></TurnOverSetting>
      </div>
    </el-dialog>

    <!--  个人信息  -->
    <el-dialog
        width="29cm"
        :visible.sync="showProfileDlg"
        :before-close="handleClose"
        :close-on-click-modal=false>
      <div slot="title" style="font-size: 20px;">{{ dialogTitle }}</div>
      <div v-if="showProfileDlg">
        <Profile
            :id="workerItem.id"
            :key="workerItem.id"
            :worker-id="workerItem.id"
        ></Profile>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import Pagination from "../../../components/pagination";
import TabButton from "@/components/rdms/tab-button";
import EditProjectTeam from "@/components/rdms/edit-project-team.vue";
import UserEditDlg from "@/components/rdms/user-edit-dlg";
import UserCreateDlg from "@/components/rdms/user-create-dlg";
import File from "../../../components/rdms/file";
import Swal from "sweetalert2";
import UserAuthorizeDlg from "@/components/rdms/user-authorize-dlg";
import UserRole from "@/components/rdms/user-role";
import JobLevel from "@/components/rdms/job-level";
import {dateFormat, optionKV} from "@/filter/filter";
import JobitemRelatedJobInfoDlg from "@/components/std/jobitem-related-job-info-dlg.vue";
import Profile from "@/views/rdms/system/profile.vue";
import ExcelFileUpload from "@/components/rdms/excel-file-upload.vue";
import EditStandManhour from "@/components/rdms/edit-stand-manhour.vue";
import EditAuthManage from "@/components/rdms/edit-auth-manage.vue";
import TurnOverSetting from "@/components/rdms/turn-over-setting.vue";
import EditOrganizationChart from "@/components/rdms/edit-organization-chart.vue";

export default {
  components: {
    ExcelFileUpload,
    JobitemRelatedJobInfoDlg,
    EditProjectTeam,
    Pagination,
    File,
    TabButton,
    UserEditDlg,
    UserCreateDlg,
    UserAuthorizeDlg,
    UserRole,
    JobLevel,
    Profile,
    EditStandManhour,
    EditAuthManage,
    TurnOverSetting,
    EditOrganizationChart,
  },
  name: "worker",

  data: function() {
    return {
      loginUser: {},
      loginCustomer: {},
      activePageId: 'home',
      worker: {},
      workerItem: {},
      users: [],
      addWorkerDialogVisible: false,
      editWorkerDialogVisible: false,
      authWorkerDialogVisible: false,
      turnOverDialogVisible: false,
      showProfileDlg: false,
      dialogTitle: '',
      timeStamp: 0,
      keyWord: '',
      departmentId: '',
      departmentList: [],
      resFiles: [],
      resFile: {},
      // 优先使用前端 public 静态模板；若需走 OSS，可改为 VUE_APP_OSS_WEB_BASE + '/import_workers_template.xlsx'
      templatePath: (process.env.BASE_URL || '/') + 'import_workers_template.xlsx',
      loadOrganizationChart: false,
    }
  },

  mounted: function() {
    let _this = this;
    _this.timeStamp = Date.now();
    _this.getLoginUser();
    _this.$refs.pagination.size = 12;
    this.$nextTick(() => {
      _this.list(1);
    });
    // console.log("process.env.VUE_APP_VERSION:", process.env.VUE_APP_VERSION);
  },

  computed: {
  },

  methods: {
    downloadFile(path) {
      // let path = "'http://127.0.0.1:18100/euipd-web/fm4ek0Zy/system/tHnkCpKT-import_workers_template.xlsx'";  // 必须使用变量, 静态值会提示"无法从网站提取文件"
      let fileName = "import_workers_template.xlsx";
      window.open(path, '_self');
      // FileSaver.saveAs(path, fileName);
    },

    statusFormat(row) {
      return optionKV(WORKER_STATUS, row.status);
    },

    getLoginUser(){
      let _this = this;
      if(Tool.getLoginUser().userGroup === LOGIN_USER_GROUP.CUSTOMER.key){
        _this.loginUser = Tool.getLoginUser().adminUser;
        _this.loginCustomer =Tool.getLoginUser();
      }else{
        _this.loginUser = Tool.getLoginUser();
        _this.loginCustomer = Tool.getLoginUser().customer;
      }
      _this.listDepartments();
    },

    listDepartments() {
      let _this = this;
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/department/listDepartment/' + _this.loginCustomer.id).then((response) => {
        let resp = response.data;
        if (resp.success) {
          _this.departmentList = resp.content || [];
        }
      })
    },

    addWorker() {
      let _this = this;
      _this.timeStamp = Date.now();
      _this.addWorkerDialogVisible = true;
      _this.dialogTitle = "添加员工";
    },

    editWorker(user) {
      let _this = this;
      _this.timeStamp = Date.now();
      _this.worker = user;
      _this.editWorkerDialogVisible = true;
      _this.dialogTitle = "编辑员工";
    },

    authWorker(user) {
      let _this = this;
      _this.timeStamp = Date.now();
      _this.worker = user;
      _this.authWorkerDialogVisible = true;
      _this.dialogTitle = "员工授权";
    },

    turnOver(user) {
      let _this = this;
      _this.worker = user;
      _this.turnOverDialogVisible = true;
      _this.dialogTitle = "加工作移交给他人...";
    },

    tabHomeClick(){
      let _this = this;
      _this.activePageId = 'home';
      _this.list(1);
    },
    tabJobLevelClick(){
      let _this = this;
      _this.activePageId = 'job-level';
    },
    tabEditTeamClick(){
      let _this = this;
      _this.activePageId = 'edit-project-team';
      // 组件常驻页面，只在挂载时取过名单；切入时重取，带上新增或离职的员工
      if (_this.$refs.editProjectTeam) {
        _this.$refs.editProjectTeam.listAllWorker();
      }
    },
    tabOrganizationClick(){
      let _this = this;
      _this.loadOrganizationChart = true;
      _this.activePageId = 'edit-organization-manager';
    },
    tabStandManhourClick(){
      let _this = this;
      _this.activePageId = 'stand-manhour';
    },
    tabAuthManageClick(){
      let _this = this;
      _this.activePageId = 'auth-manage';
    },

    tabAuthGroupManagerClick(){
      let _this = this;
      _this.activePageId = 'group-authorize-manager';
    },

    search(page) {
      let _this = this;
      if (Tool.isNotEmpty(_this.keyWord)) {
        _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/worker/searchByKeyWord', {
          page: page,
          size: _this.$refs.pagination.size,
          customerId: _this.loginCustomer.id,
          keyWord: _this.keyWord,
        }).then((response) => {
          let resp = response.data;
          _this.users = resp.content.list;
          _this.$refs.pagination.render(page, resp.content.total);
        })
      } else {
        _this.list(page);
      }
    },

    list(page) {
      let _this = this;
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/worker/list', {
        page: page,
        size: _this.$refs.pagination.size,
        customerId: _this.loginCustomer.id, //这里获取的是当前登录的customerId
        departmentId: _this.departmentId || null,
      }).then((response)=>{
        let resp = response.data;
        if (resp.success) {
          _this.users = resp.content.list;
          _this.$refs.pagination.render(page, resp.content.total);
        } else {
          Toast.warning(resp.message,Swal);
        }
      })
    },

    /**
     * 查找是否有权限
     * @param id
     */
    hasResource(id) {
      return Tool.hasResource(id);
    },

    getUploadedFiles(){
      let _this = this;
      _this.resFiles = [];
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/file/rdms/getFileListByUseAndItemId', {
        use: FILE_GROUPING.USER_INFO_TEMPLATE.key,
        itemId: SUPER_ADMIN_ID,
      }).then((response) => {
        let resp = response.data;
        if (resp.success) {
          _this.resFiles = resp.content;
          if(_this.resFiles && _this.resFiles.length>0){
            _this.resFile = _this.resFiles[0];
          }
        } else {
          Toast.warning(resp.message, Swal);
        }
      })
    },

    canResetPassword(row) {
      return !row.isAdmin && row.id !== this.loginUser.customerUserId && Tool.hasAnyRole(['ADMIN', 'UltiMng']);
    },

    resetPassword(row) {
      let _this = this;
      Swal.fire({
        title: '重置密码？',
        text: '将 ' + row.trueName + ' 的密码恢复为初始密码，其已登录的会话随即失效，登录锁定一并解除。',
        icon: 'warning',
        showCancelButton: true,
        confirmButtonColor: '#409EFF',
        cancelButtonColor: '#909399',
        confirmButtonText: '确认重置',
        cancelButtonText: '取消'
      }).then((result) => {
        if (!result.value) {
          return;
        }
        _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/worker/reset-password', {memberId: row.id}).then((response) => {
          let resp = response.data;
          if (resp.success) {
            const esc = (s) => String(s || '').replace(/[&<>"']/g, (c) => '&#' + c.charCodeAt(0) + ';');
            Swal.fire({
              title: '已重置',
              html: '已将 ' + esc(resp.content.trueName) + ' 的密码重置为初始密码：<b>' + esc(resp.content.initialPassword)
                  + '</b><br>请告知本人；登录后须先设置新密码。',
              icon: 'success',
              confirmButtonColor: '#409EFF',
              confirmButtonText: '知道了'
            });
          } else {
            Toast.warning(resp.message, Swal);
          }
        });
      });
    },

    /**
     * 点击【删除】
     */
    del(id) {
      let _this = this;
      Swal.fire({
        title: '确认？',
        text: "删除后不可恢复，确认删除？",
        icon: 'warning',
        showCancelButton: true,
        confirmButtonColor: '#409EFF',
        cancelButtonColor: '#909399',
        confirmButtonText: '确认!',
        cancelButtonText: '取消!'
      }).then((result) => {
        if (result.value) {
          _this.$ajax.delete(process.env.VUE_APP_SERVER + '/customer/worker/del/' + id).then((response)=>{
            let resp = response.data;
            if (resp.success) {
              _this.list(1);
              Toast.success("删除成功！" , Swal);
            } else {
              Toast.warning(resp.message, Swal);
            }
          });
        }
      })
    },

    afterUpload(resp) {
      let _this = this;
      const content = resp && resp.content ? resp.content : {};
      // 优先存可直接访问的绝对地址；兼容仅返回对象键的旧响应
      const webBase = (process.env.VUE_APP_OSS_WEB_BASE || '').replace(/\/$/, '');
      _this.worker.profile = content.absPath
        || (content.path && /^https?:\/\//i.test(content.path) ? content.path : null)
        || (content.path && webBase ? `${webBase}/${String(content.path).replace(/^\//, '')}` : content.path);
      _this.$forceUpdate();
    },

    rowClick(row, column, e){
      let _this = this;
      _this.workerItem = row;
      _this.showProfileDlg = true;
      _this.dialogTitle = "用户信息";
    },

    handleClose(done) {
      done();
      this.list(1);
    },

    closeDialog() {
      let _this = this;
      _this.addWorkerDialogVisible = false;
      _this.editWorkerDialogVisible = false;
      _this.authWorkerDialogVisible = false;
      _this.turnOverDialogVisible = false;
      _this.showProfileDlg = false;
      this.list(1);
    },

    importSuccess() {
      // 仅在真正导入成功时由子组件触发；刷新列表（成功提示在上传组件内）
      this.list(1);
    },
  }
}
</script>
