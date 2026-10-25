<!--
 * Author: 周朔鹏
 * Date: 2025-08-04
 * Email: 391902958@qq.com
 * Website: https://www.euipd.com
-->

<template>
  <div>
    <el-container>
      <el-aside width="190px" class="ca-sidebar">
        <div class="ca-sidebar-nav">
          <div class="profile">
            <div @click="showUserInfo()" style="cursor: pointer;">
              <div class="profile-img">
                <img :src="avatarSrc" alt="avatar" @error="onAvatarError" />
              </div>
            </div>
            <div class="profile-title">
              <div class="greetings">Good Day !</div>
              <div class="user-name">
                {{ worker.trueName }}
              </div>
            </div>
          </div>
          <div class="ca-menu-line-fix"></div>

          <!--超级用户菜单-->
          <div v-if="hasSuperMenu" id="super-manage-menu" class="ca-menu ca-menu-group"
            :class="{ 'ca-menu-open': superUserMenuOpenStatus === true }">
            <a href="javascript:void(0);" @click="superUserMenu">
              <div class="ca-menu-title-form">
                <div class="menu-title">
                  超级用户
                </div>
                <div class="menu-spread">
                  <div v-if="!superUserMenuOpenStatus">
                    <i class="fa fa-chevron-left" aria-hidden="true"></i>
                  </div>
                  <div v-else>
                    <i class="fa fa-chevron-down" aria-hidden="true"></i>
                  </div>
                </div>
              </div>
            </a>
            <menu-item v-show="hasMenuPermission('5101', 'CUSTOMER')" id="system-customer-sidebar" route="/system/customer"
              icon="fa fa-address-book-o" title="机构/团队" />
            <!-- <menu-item v-if="isManufacturer" v-show="hasMenuPermission('5103', 'ROLE')" id="system-customer-role-sidebar"
              route="/system/customer-role" icon="fa fa-users" title="权限管理" />
           -->
            </div>

          <!--我的邮件-->
          <div v-if="loginCustomer.id !== SUPER_ADMIN_ID && canShowSidebar('mail')" id="mail-manage-menu"
            class="ca-menu ca-menu-group" :class="{ 'ca-menu-open': mailMenuOpenStatus === true }">
            <a href="javascript:void(0);" @click="mailMenuOpenStatus = !mailMenuOpenStatus">
              <div class="ca-menu-title-form">
                <div class="menu-title">
                  我的邮件
                </div>
                <div class="menu-spread">
                  <div v-if="!mailMenuOpenStatus">
                    <i class="fa fa-chevron-left" aria-hidden="true"></i>
                  </div>
                  <div v-else>
                    <i class="fa fa-chevron-down" aria-hidden="true"></i>
                  </div>
                </div>
              </div>
            </a>
            <menu-item id="mail-box-sidebar" route="/customer/mail/inbox" icon="fa fa-envelope-o" title="邮箱"
              :badge="true" :badge-value="mailUnread > 0 ? mailUnread : ''" />
            <menu-item id="mail-compose-sidebar" route="/customer/mail-compose" icon="fa fa-pencil-square-o"
              title="写邮件" />
          </div>

          <!--研发管理主菜单-->
          <div v-if="loginCustomer.id !== SUPER_ADMIN_ID" id="develop-manage-menu" class="ca-menu ca-menu-group"
            :class="{ 'ca-menu-open-product': productMenuOpenStatus === true }">
            <a href="javascript:void(0);" @click="productManageMenu">
              <div class="ca-menu-title-form">
                <div class="menu-title">
                  研发管理
                </div>
                <div class="menu-spread">
                  <div v-if="!productMenuOpenStatus">
                    <i class="fa fa-chevron-left" aria-hidden="true"></i>
                  </div>
                  <div v-else>
                    <i class="fa fa-chevron-down" aria-hidden="true"></i>
                  </div>
                </div>
              </div>
            </a>
            <!-- 团队管理：机构 UltiMng / ADMIN（非超管） -->
            <menu-item v-if="canShowSidebar('worker')"
              id="customer-worker-sidebar" route="/customer/worker"
              icon="fa fa-graduation-cap" title="团队管理" />
            <!-- <menu-item v-if="canShowSidebar('tenantPolicy')"
              id="system-tenant-ui-policy-sidebar" route="/system/tenant-ui-policy"
              icon="fa fa-check-square-o" title="权限配置" /> -->
            <menu-item v-if="canShowSidebar('workCalendar')"
              id="customer-work-calendar-sidebar" route="/customer/work-calendar"
              icon="fa fa-calendar" title="假日设置" />
            <!-- <menu-item v-if="canShowSidebar('flow')"
              id="customer-flow-sidebar" route="/customer/flow"
              icon="fa fa-random" title="流程配置" /> -->
            <menu-item v-if="canShowSidebar('budget')"
              id="customer-budget-sidebar" route="/customer/budget" icon="fa fa-jpy" title="预算管理" :badge="true"
              :badge-is-dot="budgetManageJobNum > 0" />
            <menu-item v-if="canShowSidebar('summary')"
              id="customer-summary-sidebar" route="/customer/summary" icon="fa fa-dashboard" title="绩效/盘点" />
            <menu-item v-show="canShowSidebar('module')" id="customer-module-sidebar" route="/customer/module"
              icon="fa fa-puzzle-piece" title="特性仓库" />
            <menu-item v-show="canShowSidebar('demand')" id="customer-demand-sidebar" route="/customer/demand"
              icon="fa fa-question" :icon-style="{ fontSize: '18px' }" title="需求识别"
              :link-style="{ position: 'relative' }" :has-list="demandHasList" />
            <menu-item v-if="canShowSidebar('character')" id="customer-character-sidebar"
              route="/customer/character" icon="el-icon-cpu" title="预立项管理" :link-style="{ position: 'relative' }"
              :badge="true"
              :badge-is-dot="characterJobFlag || demandJobFlag || characterJobFlag_evaluate || updateCharacterNum > 0"
              :has-list="preProjectHasList" />
            <menu-item v-if="canShowSidebar('function')" id="customer-function-sidebar"
              route="/customer/function" icon="el-icon-check" title="系统工程" :link-style="{ position: 'relative' }"
              :badge="true" :badge-is-dot="functionManageJobNum > 0" :has-list="testHasList" />
            <menu-item v-if="canShowSidebar('setup')"
              id="customer-setup-sidebar" route="/customer/setup" icon="fa fa-gavel" title="立项管理"
              :link-style="{ position: 'relative' }" :badge="true" :badge-is-dot="setupCount > 0"
              :has-list="setupProjectHasList" />
            <menu-item v-if="canShowSidebar('project')" id="customer-project-sidebar" route="/customer/project"
              icon="el-icon-finished" title="项目管理" :link-style="{ position: 'relative' }" :badge="true"
              :badge-is-dot="projectNotifyNum > 0 || materialAppNum_project > 0 || feeAppNum_project > 0"
              :has-list="projectHasList" />
            <menu-item v-if="canShowSidebar('develop')" id="customer-develop-sidebar" route="/customer/develop"
              icon="fa fa-cogs" title="研发管理" :link-style="{ position: 'relative' }" :badge="true"
              :badge-is-dot="developNum > 0 || materialAppNum > 0 || feeAppNum > 0" :has-list="developHasList" />
            <menu-item v-if="canShowSidebar('test')" id="customer-test-sidebar"
              route="/customer/test" icon="el-icon-check" title="测试管理" :link-style="{ position: 'relative' }"
              :badge="true" :badge-is-dot="testManageJobNum > 0" :has-list="testHasList" />
            <menu-item v-show="canShowSidebar('manhour')" id="customer-manhour-sidebar" route="/customer/manhour"
              icon="fa fa-bolt" title="执行管理" :badge="true" :badge-is-dot="jobCount > 0" :has-list="jobCount > 0" />
            <menu-item v-if="canShowSidebar('material')"
              id="customer-material-sidebar" route="/customer/material" icon="fa fa-link" title="物料管理"
              :has-list="materialHasList" />
            <menu-item v-if="canShowSidebar('jobitem')" id="customer-jobitem-sidebar" route="/customer/jobitem"
              icon="el-icon-s-order" title="工期管理" :badge="true" :badge-is-dot="false" :has-list="jobitemHasList" />
            <menu-item v-if="canShowSidebar('review')"
              id="customer-review-sidebar" route="/customer/review" icon="el-icon-coordinate" title="评审管理"
              :link-style="{ position: 'relative' }" :badge="true"
              :badge-is-dot="reviewJobNum > 0 || reviewRelatedJobNum > 0 || reviewSubprojectNum > 0 || reviewMilestoneJobNum > 0"
              :has-list="reviewHasList" />
            <menu-item v-if="canShowSidebar('product')"
              id="customer-product-sidebar" route="/customer/product" icon="fa fa-superpowers" title="产品管理"
              :badge="true" :badge-is-dot="productNotifyNum > 0" :has-list="productHasList" />
            <menu-item v-if="canShowSidebar('performance')"
              id="customer-performance-sidebar" route="/customer/performance" icon="fa fa-line-chart" title="绩效管理" />
            <menu-item v-if="canShowSidebar('quality')"
              id="customer-quality-sidebar" route="/customer/quality" icon="fa fa-recycle" title="质量管理" :badge="true"
              :badge-is-dot="qualitySubmitNum > 0 || qualityMaterialApplicationNum > 0 || qualityFeeApplicationNum > 0 || qualityDefineSubmitNum > 0"
              :has-list="qualityHasList" />
            <menu-item v-if="canShowSidebar('qualitydefine')" id="customer-qualitydefine-sidebar"
              route="/customer/qualitydefine" icon="fa fa-undo" title="质量反馈" :badge="true"
              :badge-is-dot="qualityRecheckNum > 0" />
            <menu-item v-show="canShowSidebar('file')" id="file-file-sidebar" route="/file/file" icon="fa fa-folder-o"
              title="档案管理" :badge="true" :badge-is-dot="waitForApproveNum > 0" />
            <menu-item v-if="canShowSidebar('download')"
              route="/customer/download" icon="fa fa-download" title="文档下载" id="customer-download-sidebar" />
            <menu-item v-if="canShowSidebar('warehouse')" id="customer-warehouse-sidebar"
              route="/customer/warehouse" icon="fa fa-cubes" title="仓库管理" />
            <menu-item v-if="canShowSidebar('inventory')" id="customer-inventory-sidebar"
              route="/customer/inventory" icon="fa fa-archive" title="库存管理" />
            
            
              <!--            <div v-if="(role_UltiMng || role_ADMIN) && hasResource('5222')" class="ca-menu-item" id="customer-course-sidebar">
              <router-link to="/customer/course" >
                <div class="light"></div>
                <div class="left-icon">
                  <i class="fa fa-book" aria-hidden="true" style="font-size: 19px;"></i>
                </div>
                <div class="menu-content-fix">
                    课程管理
                </div>
                <div class="right-icon">
                  <i class="fa fa-chevron-right iconfont" aria-hidden="true"></i>
                </div>
              </router-link>
            </div>-->
          </div>
          <div style="width: 190px; height: 200px;"></div>
        </div>
      </el-aside>
      <el-container>
        <el-header height="50px" class="admin-header">
          <router-link to="/welcome" class="navbar-brand">
            <div class="header-font">
              EUIPD研发管理系统 ( Elastic Universal Integration Product Development ) <!--欢迎：{{ loginCustomer.loginName }}-->
            </div>
          </router-link>
          <div style="float: right; margin-right: 10px;">
            <el-button type="text" title="修改密码" style="margin-right: 12px;" @click="showChangePasswordDlg = true">
              <i class="el-icon-key" style="line-height: 32px; font-size: 24px;"></i>
            </el-button>
            <el-button type="text" title="退出登录" @click="logout()">
              <span class="glyphicon glyphicon-off" aria-hidden="true"
                style="line-height: 32px; font-size: 28px; color: #FF0000;"></span>
            </el-button>
          </div>
        </el-header>
        <el-main>
          <!-- 高频列表 keep-alive；其余路由正常销毁 -->
          <keep-alive :include="keepAliveIncludes">
            <router-view v-if="routeAllowed && $route.meta.keepAlive" :key="$route.name" />
          </keep-alive>
          <router-view v-if="routeAllowed && !$route.meta.keepAlive" :key="$route.fullPath" />
        </el-main>
        <el-footer>
          <div
            style="width: 100%; display: flex; flex-direction: column; justify-content: center; align-items: center; font-size: 16px;">
            <div class="hr hr-4 hr-double" style="width: 100%;"></div>
            <div style="color: rgba(2,35,79,0.9); font-weight: 500;">
              EUIPD研发管理系统
            </div>
            <div style="color: #888; font-size: 12px;" :title="backendBuildText">
              系统版本 前端 {{ frontendVersion }} / 后端 {{ (backendInfo && backendInfo.version) || '-' }}
            </div>
          </div>
        </el-footer>
      </el-container>
    </el-container>

    <!--  个人信息  -->
    <el-dialog width="29cm" :visible.sync="showProfileDlg" :before-close="handleClose" :close-on-click-modal=true>
      <div slot="title" style="font-size: 20px;">{{ dialogTitle }}</div>
      <div v-if="showProfileDlg">
        <Profile :id="loginUser.id" :key="loginUser.id" :worker-id="loginUser.id"></Profile>
      </div>
    </el-dialog>
    <change-password-dlg :visible.sync="showChangePasswordDlg"></change-password-dlg>
  </div>
</template>

<script>
import Swal from "sweetalert2";
import $ from "jquery";
import Profile from "@/views/rdms/system/profile.vue";
import MenuItem from "@/components/MenuItem.vue";
import TaskStateMixin from "@/mixins/taskStateMixin.js";
import ChangePasswordDlg from "@/components/rdms/change-password-dlg.vue";
import { countBox as countMailBox } from "@/shared/mail-api";
const MAIL_POLL_MS = 60 * 1000;

export default {
  name: "admin-v2",
  components: {
    Profile,
    MenuItem,
    ChangePasswordDlg
  },
  mixins: [TaskStateMixin],

  data: function () {
    return {
      keepAliveIncludes: 'welcome,project,setup,character,develop,demand,material,manhour',
      showChangePasswordDlg: false,
      frontendVersion: process.env.VUE_APP_BUILD_VERSION || '-',
      backendInfo: null,
      mailMenuOpenStatus: true,
      mailUnread: 0,
      mailUnreadLoaded: false,
      mailTimer: null,
      badgeRefreshTimer: null,
      busHandlers: null,
      // 聚合接口已上线：优先一次拉取；失败后本会话回退扇出（badgeAuthDead 阻断鉴权失效风暴）
      badgeSummaryUnavailable: false,
      badgeAuthDead: false,

      // 用户相关数据
      userInfo: {
        directLoginUser: {},
        worker: {},
        loginUser: {},
        loginCustomer: {},
        loginUserRoleList: []
      },
      // 侧栏头像：本地默认图，避免 OSS/MinIO 私有或缺失导致裂图
      avatarSrc: '/imgs/panda.png',

      // 用户角色状态
      userRoles: {
        role_SUPER: false,
        role_ADMIN: false,
        role_SYSGM: false,
        role_PDGM: false,
        role_IPMT: false,
        role_UltiMng: false,
        role_PDM: false,
        role_SM: false,
        role_PJM: false,
        role_TM: false,
        role_TGM: false,
        role_MATM: false
      },

      // 界面状态
      uiState: {
        showProfileDlg: false,
        dialogTitle: '',
        superUserMenuOpenStatus: true,
        productMenuOpenStatus: true,
        fileMenuOpenStatus: false
      },

      // 任务状态数量
      taskCounts: {
        jobCount: 0,
        setupCount: 0,
        projectNotifyNum: 0,
        productNotifyNum: 0,
        developNum: 0,
        qualitySubmitNum: 0,
        materialAppNum: 0,
        materialAppNum_project: 0,
        feeAppNum: 0,
        feeAppNum_project: 0,
        reviewJobNum: 0,
        reviewMilestoneJobNum: 0,
        reviewSubprojectNum: 0,
        reviewRelatedJobNum: 0,
        updateCharacterNum: 0,
        testManageJobNum: 0,
        functionManageJobNum: 0,
        budgetManageJobNum: 0,
        qualityRecheckNum: 0,
        waitForApproveNum: 0,
        qualityDefineSubmitNum: 0,
        qualityMaterialApplicationNum: 0,
        qualityFeeApplicationNum: 0
      },

      // 任务标志状态
      taskFlags: {
        characterJobFlag: false,
        characterJobFlag_evaluate: false,
        demandJobFlag: false,
        developHasList: false,
        demandHasList: false,
        preProjectHasList: false,
        projectHasList: false,
        testHasList: false,
        setupProjectHasList: false,
        reviewHasList: false,
        productHasList: false,
        qualityHasList: false,
        materialHasList: false,
        jobitemHasList: false
      },

      // 定时器
      timer: {},

      // 常量
      SUPER_ADMIN_ID: SUPER_ADMIN_ID
    }
  },

  computed: {
    // 无权页面不挂载，避免页面先发请求再被拦回欢迎页
    routeAllowed () {
      return this.hasResourceRouter(this.$route.name)
    },
    backendBuildText () {
      const b = this.backendInfo
      if (!b) return ''
      return '后端构建时间 ' + (b.buildTime || '-') + (b.commit ? '，提交 ' + b.commit : '')
    },
    // 系统功能相关
    hasSuperMenu() {
      // 超管侧栏：role_SUPER 或机构 ID 为平台超管（避免 roleList 未带 SUPER 时整栏空白）
      const cid = (this.loginCustomer && this.loginCustomer.id)
        || (this.directLoginUser && this.directLoginUser.id);
      return !!(this.role_SUPER || cid === this.SUPER_ADMIN_ID
        || (typeof RoleAccess !== 'undefined' && RoleAccess.hasRole('SUPER', this.loginUserRoleList)));
    },
    hasFileMenu() {
      return this.hasResource('0301');
    },
    isManufacturer() {
      return process.env.VUE_APP_USERTYPE === 'manufacturer';
    },

    // 为保证向后兼容性，提供简化访问属性
    directLoginUser: {
      get() { return this.userInfo.directLoginUser; },
      set(value) { this.userInfo.directLoginUser = value; }
    },
    worker: {
      get() { return this.userInfo.worker; },
      set(value) { this.userInfo.worker = value; }
    },
    loginUser: {
      get() { return this.userInfo.loginUser; },
      set(value) { this.userInfo.loginUser = value; }
    },
    loginCustomer: {
      get() { return this.userInfo.loginCustomer; },
      set(value) { this.userInfo.loginCustomer = value; }
    },
    loginUserRoleList: {
      get() { return this.userInfo.loginUserRoleList; },
      set(value) { this.userInfo.loginUserRoleList = value; }
    },

    // 角色状态简化访问
    role_SUPER: {
      get() { return this.userRoles.role_SUPER; },
      set(value) { this.userRoles.role_SUPER = value; }
    },
    role_ADMIN: {
      get() { return this.userRoles.role_ADMIN; },
      set(value) { this.userRoles.role_ADMIN = value; }
    },
    role_UltiMng: {
      get() { return this.userRoles.role_UltiMng; },
      set(value) { this.userRoles.role_UltiMng = value; }
    },
    role_PDM: {
      get() { return this.userRoles.role_PDM; },
      set(value) { this.userRoles.role_PDM = value; }
    },
    role_PDGM: {
      get() { return this.userRoles.role_PDGM; },
      set(value) { this.userRoles.role_PDGM = value; }
    },
    role_IPMT: {
      get() { return this.userRoles.role_IPMT; },
      set(value) { this.userRoles.role_IPMT = value; }
    },
    role_PJM: {
      get() { return this.userRoles.role_PJM; },
      set(value) { this.userRoles.role_PJM = value; }
    },
    role_SYSGM: {
      get() { return this.userRoles.role_SYSGM; },
      set(value) { this.userRoles.role_SYSGM = value; }
    },
    role_SM: {
      get() { return this.userRoles.role_SM; },
      set(value) { this.userRoles.role_SM = value; }
    },
    role_TM: {
      get() { return this.userRoles.role_TM; },
      set(value) { this.userRoles.role_TM = value; }
    },
    role_TGM: {
      get() { return this.userRoles.role_TGM; },
      set(value) { this.userRoles.role_TGM = value; }
    },
    role_MATM: {
      get() { return this.userRoles.role_MATM; },
      set(value) { this.userRoles.role_MATM = value; }
    },

    // UI状态简化访问
    showProfileDlg: {
      get() { return this.uiState.showProfileDlg; },
      set(value) { this.uiState.showProfileDlg = value; }
    },
    dialogTitle: {
      get() { return this.uiState.dialogTitle; },
      set(value) { this.uiState.dialogTitle = value; }
    },
    superUserMenuOpenStatus: {
      get() { return this.uiState.superUserMenuOpenStatus; },
      set(value) { this.uiState.superUserMenuOpenStatus = value; }
    },
    productMenuOpenStatus: {
      get() { return this.uiState.productMenuOpenStatus; },
      set(value) { this.uiState.productMenuOpenStatus = value; }
    },
    fileMenuOpenStatus: {
      get() { return this.uiState.fileMenuOpenStatus; },
      set(value) { this.uiState.fileMenuOpenStatus = value; }
    }
  },

  mounted: function () {
    let _this = this;
    _this.getLoginUser();
    _this.getWorkerInfo();
    _this.loadBackendVersion();
    // 角标轮询拉长到 20 分钟，降低后台噪声
    _this.timer = setInterval(function () {
      _this.scheduleBadgeRefresh();
    }, 1000 * 60 * 20);

    // 侧栏只订阅工作区变更，仍走聚合接口和防抖
    _this.unsubscribeBadge = _this.$bus.subscribe('workspace.changed', function (payload) {
      const areas = ((payload && payload.areas) || []).filter(a => a !== 'mail');
      if (areas.length) {
        _this.scheduleBadgeRefresh();
      }
    });

    // 邮件未读数独立轮询，不触发业务角标整体刷新
    _this.unsubscribeMail = _this.$bus.subscribe('mail', function () {
      _this.refreshMailUnread();
    });
    _this.mailTimer = setInterval(function () {
      _this.refreshMailUnread();
    }, MAIL_POLL_MS);
    setTimeout(function () {
      _this.refreshMailUnread();
    }, 600);

    //主页控制及路由拦截代码
    {
      _this.activeSidebar(_this.sidebarIdOf(_this.$route));
      if (_this.$route.name !== "euipdWelcome"
        && _this.$route.name !== "welcome"
      ) {
        if (!_this.hasResourceRouter(_this.$route.name)) {
          console.log("页面拦截(admin-v2页面)：", _this.$route.name);
          _this.$message && _this.$message.warning('没有权限访问该页面');
          _this.$router.push("/welcome");
        }
      }

    }
  },

  beforeDestroy() {
    this.stopBadgeRefresh();
    if (this.unsubscribeBadge) {
      this.unsubscribeBadge();
      this.unsubscribeBadge = null;
    }
    if (this.unsubscribeMail) {
      this.unsubscribeMail();
      this.unsubscribeMail = null;
    }
    if (this.mailTimer) {
      clearInterval(this.mailTimer);
      this.mailTimer = null;
    }
  },

  watch: {
    $route: {
      handler: function (val, oldVal) {
        // sidebar激活样式方法二
        console.log("---->页面跳转：", val, oldVal);
        let _this = this;

        if (!_this.hasResourceRouter(val.name)) {
          console.log("页面拦截：", val.name);
          _this.$message && _this.$message.warning('没有权限访问该页面');
          _this.$router.push("/welcome");
          return;
        }

        _this.$nextTick(function () {  //页面加载完成后执行
          _this.activeSidebar(_this.sidebarIdOf(_this.$route));
        })
      }
    }
  },

  methods: {
    loadBackendVersion () {
      this.$ajax.get(process.env.VUE_APP_SERVER + '/system/app-info/version').then(response => {
        const resp = response.data
        if (resp && resp.success) this.backendInfo = resp.content
      }).catch(() => {})
    },
    /**
     * 统一的API调用方法
     * @param {string} url - API地址
     * @param {object} params - 请求参数，默认空对象
     * @param {function|null} successCallback - 可选的成功回调函数，接收响应数据作为参数
     * @param {string} method - 请求方法（'get', 'post', 'put', 'delete'等），默认'post'
     * @returns {Promise<any|null>} 成功时返回响应数据，失败时返回null
     *
     * 使用示例：
     * 1. 返回值模式: const result = await this.apiCall('/api/data');
     * 2. 回调模式: await this.apiCall('/api/data', {}, (data) => this.handleData(data));
     * 3. 混合模式: const result = await this.apiCall('/api/data', {}, (data) => console.log(data));
     */
    markBadgeAuthDead() {
      this.badgeAuthDead = true;
      this.stopBadgeRefresh();
    },

    isBadgeAuthOk() {
      if (this.badgeAuthDead) {
        return false;
      }
      if (typeof this.$isAuthRedirecting === 'function' && this.$isAuthRedirecting()) {
        return false;
      }
      return this.hasAccessToken();
    },

    async apiCall(url, params = {}, successCallback = null, method = 'post') {
      if (!this.isBadgeAuthOk()) {
        return null;
      }
      try {
        const response = await this.$ajax[method](url, params);
        const resp = response.data || {};
        if (resp.success) {
          // 如果提供了成功回调，则调用它
          if (successCallback && typeof successCallback === 'function') {
            try {
              successCallback(resp.content);
            } catch (callbackError) {
              console.error('回调函数执行错误:', callbackError);
            }
          }
          return resp.content;
        }
        // 鉴权失效：停角标，不 Toast（main.js 统一跳登录）
        if (resp.code === 'TOKEN_INVALID' || resp.code === 'UNAUTHORIZED') {
          this.markBadgeAuthDead();
          return null;
        }
        Toast.warning(resp.message, Swal);
        return null;
      } catch (error) {
        const status = error && error.response && error.response.status;
        if (status === 401 || status === 412) {
          this.markBadgeAuthDead();
          return null;
        }
        // 已在踢登录：静默
        if (typeof this.$isAuthRedirecting === 'function' && this.$isAuthRedirecting()) {
          return null;
        }
        console.error('API调用错误:', error);
        Toast.warning('网络请求失败', Swal);
        return null;
      }
    },

    /**
     * 获取用户状态数量的通用方法
     * @param {string} endpoint - API端点
     * @param {string} userId - 用户ID
     * @param {string} dataProperty - 数据属性名
     */
    async getUserStatusCount(endpoint, userId, dataProperty) {
      const url = `${process.env.VUE_APP_SERVER}${endpoint}/${userId}`;
      const result = await this.apiCall(url);
      if (result !== null) {
        this[dataProperty] = result;
      }
    },

    /**
     * 获取用户标志状态的通用方法
     * @param {string} endpoint - API端点
     * @param {string} userId - 用户ID
     * @param {string} flagProperty - 标志属性名
     */
    async getUserStatusFlag(endpoint, userId, flagProperty) {
      const url = `${process.env.VUE_APP_SERVER}${endpoint}/${userId}`;
      const result = await this.apiCall(url);
      if (result !== null) {
        this[flagProperty] = result;
      }
    },
    /**
     * 显示用户信息对话框
     */
    showUserInfo() {
      this.showProfileDlg = true;
      this.dialogTitle = "用户信息";
    },

    /**
     * 获取当前用户工作信息
     */
    async getWorkerInfo() {
      const userId = this.loginUser && this.loginUser.customerUserId;
      if (!userId) {
        // 平台超管等场景可能无 membership，跳过员工信息
        this.avatarSrc = '/imgs/panda.png';
        return;
      }
      const url = `${process.env.VUE_APP_SERVER}/customer/worker/get-worker-info/${userId}`;
      const result = await this.apiCall(url);
      if (result) {
        this.worker = result;
        this.avatarSrc = this.resolveAvatarUrl(result.profile);
      }
    },

    /**
     * 解析头像地址：
     * - 空 / 默认占位 / 旧阿里云不可访问地址 → 本地 panda
     * - 对象键（无 http）→ 拼 MinIO 公共 web 前缀
     * - 其余绝对地址原样使用，加载失败由 @error 回退
     */
    resolveAvatarUrl(profile) {
      const fallback = '/imgs/panda.png';
      if (!profile || typeof profile !== 'string') {
        return fallback;
      }
      const p = profile.trim();
      if (!p || p.includes('defaultProfile')) {
        return fallback;
      }
      // 历史阿里云公网桶已变更为私有，直接访问 403
      if (/aliyuncs\.com/i.test(p)) {
        const key = p.replace(/^https?:\/\/[^/]+\//i, '');
        const webBase = (process.env.VUE_APP_OSS_WEB_BASE || '').replace(/\/$/, '');
        return key && webBase ? `${webBase}/${key}` : fallback;
      }
      if (/^https?:\/\//i.test(p)) {
        return p;
      }
      const webBase = (process.env.VUE_APP_OSS_WEB_BASE || '').replace(/\/$/, '');
      return webBase ? `${webBase}/${p.replace(/^\//, '')}` : fallback;
    },

    onAvatarError() {
      if (this.avatarSrc !== '/imgs/panda.png') {
        this.avatarSrc = '/imgs/panda.png';
      }
    },

    handleClose(done) {
      done();
    },

    /**
     * 切换超级用户菜单展开状态
     */
    superUserMenu() {
      this.superUserMenuOpenStatus = !this.superUserMenuOpenStatus;
    },

    /**
     * 切换产品管理菜单展开状态
     */
    productManageMenu() {
      this.productMenuOpenStatus = !this.productMenuOpenStatus;
    },

    /**
     * 切换文件管理菜单展开状态
     */
    fileManageMenu() {
      this.fileMenuOpenStatus = !this.fileMenuOpenStatus;
    },

    hasAccessToken() {
      const user = (typeof Tool !== 'undefined' && Tool.getLoginUser()) || {};
      return !!user.accessToken;
    },

    /** 角标刷新防抖：合并 bus 风暴为一次刷新 */
    scheduleBadgeRefresh() {
      if (!this.isBadgeAuthOk()) {
        this.stopBadgeRefresh();
        return;
      }
      if (this.badgeRefreshTimer) {
        clearTimeout(this.badgeRefreshTimer);
      }
      this.badgeRefreshTimer = setTimeout(() => {
        this.badgeRefreshTimer = null;
        this.refreshStatusFlag();
      }, 400);
    },

    stopBadgeRefresh() {
      if (this.badgeRefreshTimer) {
        clearTimeout(this.badgeRefreshTimer);
        this.badgeRefreshTimer = null;
      }
      if (this.timer) {
        clearInterval(this.timer);
        this.timer = null;
      }
    },

    /**
     * 刷新所有状态标志
     * 1) 可选聚合接口（未上线则跳过）
     * 2) 先用现有接口鉴权探活，401 立即停，避免扇出几十个角标请求
     */
    async refreshStatusFlag() {
      if (!this.isBadgeAuthOk()) {
        this.stopBadgeRefresh();
        return;
      }
      const userId = this.loginUser && this.loginUser.customerUserId;
      if (!userId) {
        return;
      }
      // 平台超管无业务角标，跳过（membership 仅为占位）
      const loginId = this.directLoginUser && this.directLoginUser.id;
      const customerId = this.loginCustomer && this.loginCustomer.id;
      if (loginId === SUPER_ADMIN_ID || customerId === SUPER_ADMIN_ID) {
        return;
      }

      // 1) 聚合接口（仅当已开启；404/失败后本会话不再请求）
      if (!this.badgeSummaryUnavailable) {
        try {
          const summaryUrl = process.env.VUE_APP_SERVER + '/customer/notify/badge-summary/' + userId;
          const resp = await this.$ajax.get(summaryUrl, { skipScopeCheck: true });
          const body = (resp && resp.data) || {};
          if (body.code === 'TOKEN_INVALID' || body.code === 'UNAUTHORIZED') {
            this.markBadgeAuthDead();
            return;
          }
          if (body.success && body.content
              && typeof body.content === 'object' && !Array.isArray(body.content)) {
            const c = body.content;
            Object.keys(c).forEach((key) => {
              this[key] = c[key];
              if (this.taskCounts && Object.prototype.hasOwnProperty.call(this.taskCounts, key)) {
                this.taskCounts[key] = c[key];
              }
              if (this.taskFlags && Object.prototype.hasOwnProperty.call(this.taskFlags, key)) {
                this.taskFlags[key] = c[key];
              }
            });
            return;
          }
          this.badgeSummaryUnavailable = true;
        } catch (e) {
          const status = e && e.response && e.response.status;
          if (status === 401 || status === 412) {
            this.markBadgeAuthDead();
            return;
          }
          this.badgeSummaryUnavailable = true;
        }
      }

      // 2) 鉴权探活（现有接口，不依赖未上线的 badge-summary）
      try {
        const probeUrl = process.env.VUE_APP_SERVER
          + '/customer/job-items/countJobitemsByLoginUserId/' + userId;
        const probeResp = await this.$ajax.post(probeUrl, {}, { skipScopeCheck: true });
        const probeBody = (probeResp && probeResp.data) || {};
        if (probeBody.code === 'TOKEN_INVALID' || probeBody.code === 'UNAUTHORIZED') {
          this.markBadgeAuthDead();
          return;
        }
        if (probeBody.success && probeBody.content != null) {
          this.jobCount = probeBody.content;
        }
      } catch (e) {
        const status = e && e.response && e.response.status;
        if (status === 401 || status === 412) {
          this.markBadgeAuthDead();
          return;
        }
      }
      if (!this.isBadgeAuthOk()) {
        return;
      }

      const tasks = [];
      if (this.hasMenuPermission('5207', 'PRE_PROJECT') || this.hasMenuPermission('5201', 'PROJECT')) {
        tasks.push(this.getCharacterJobNotify_submit());
        tasks.push(this.getCharacterJobNotify_evaluate());
      }
      if (this.hasMenuPermission('5204', 'DEMAND')) {
        tasks.push(this.getDemandJobNotify());
      }
      if (this.hasMenuPermission('5209', 'PROJECT')) {
        tasks.push(this.getPjmProjectJobNotifyNum());
      }
      if (this.hasMenuPermission('5201', 'PROJECT') || this.hasMenuPermission('5218', 'PRODUCT')) {
        tasks.push(this.getPdmProjectJobNotifyNum());
      }
      tasks.push(this.refreshUserStatusCounts(userId));
      tasks.push(this.refreshUserStatusFlags(userId));
      await Promise.all(tasks);
    },

    /**
     * 刷新用户状态数量（按可见菜单裁剪）
     */
    async refreshUserStatusCounts(userId) {
      const countApis = [
        { endpoint: '/customer/job-items/countJobitemsByLoginUserId', property: 'jobCount', menu: ['5202', 'MANHOUR'] },
        { endpoint: '/customer/project/countSetupProjectBySupervise', property: 'setupCount', menu: ['5201', 'PROJECT'] },
        { endpoint: '/customer/job-items/getNotifyDevelopNum', property: 'developNum', menu: ['5206', 'JOB_ITEM'] },
        { endpoint: '/customer/job-items/countQualityJobFlag', property: 'qualitySubmitNum', menu: ['5213', 'QUALITY'] },
        { endpoint: '/customer/material/getMaterialApplicationNum', property: 'materialAppNum', menu: ['5210', 'MATERIAL'] },
        { endpoint: '/customer/material/getMaterialApplicationNum_project', property: 'materialAppNum_project', menu: ['5209', 'PROJECT'] },
        { endpoint: '/customer/fee/getFeeApplicationNum', property: 'feeAppNum', menu: ['5206', 'JOB_ITEM'] },
        { endpoint: '/customer/fee/getFeeApplicationNum_project', property: 'feeAppNum_project', menu: ['5209', 'PROJECT'] },
        { endpoint: '/customer/character/getNumOfReviewCharactersByNextNode', property: 'reviewJobNum', menu: ['5215', 'JOB_ITEM'] },
        { endpoint: '/customer/job-items/getNumOfMilestoneReviewByNextNodeAndStatus', property: 'reviewMilestoneJobNum', menu: ['5215', 'JOB_ITEM'] },
        { endpoint: '/customer/sub-project/getNumOfReviewSubprojectByNextNode', property: 'reviewSubprojectNum', menu: ['5215', 'JOB_ITEM'] },
        { endpoint: '/customer/job-items/getReviewRelatedSubmitJobNumByUserId', property: 'reviewRelatedJobNum', menu: ['5215', 'JOB_ITEM'] },
        { endpoint: '/customer/character/getUpdateCharacterNumByUserId', property: 'updateCharacterNum', menu: ['5207', 'PRE_PROJECT'] },
        { endpoint: '/customer/job-items/getTestManageJobNumByUserId', property: 'testManageJobNum', menu: ['5208', 'QUALITY'] },
        { endpoint: '/customer/job-items/getFunctionManageJobNumByUserId', property: 'functionManageJobNum', menu: ['5221', 'JOB_ITEM'] },
        { endpoint: '/customer/budget-mng/getBudgetManageJobNumByUserId', property: 'budgetManageJobNum', menu: ['5220', 'BUDGET'] },
        { endpoint: '/customer/quality/getQualityRecheckNumByUserId', property: 'qualityRecheckNum', menu: ['5211', 'QUALITY'] },
        { endpoint: '/customer/quality/getQualityDefineSubmitNumByPdm', property: 'qualityDefineSubmitNum', menu: ['5213', 'QUALITY'] },
        { endpoint: '/customer/material/getQualityMaterialApplicationNumByUserId', property: 'qualityMaterialApplicationNum', menu: ['5213', 'QUALITY'] },
        { endpoint: '/customer/fee/getQualityFeeApplicationNumByUserId', property: 'qualityFeeApplicationNum', menu: ['5213', 'QUALITY'] }
      ];

      // jobCount 已在鉴权探活时写入，避免重复请求
      const visible = countApis.filter(api =>
        api.property !== 'jobCount' && this.hasMenuPermission(api.menu[0], api.menu[1]));
      await Promise.all(
        visible.map(api => this.getUserStatusCount(api.endpoint, userId, api.property))
      );
    },

    /**
     * 刷新用户标志状态（按可见菜单裁剪）
     */
    async refreshUserStatusFlags(userId) {
      const flagApis = [
        { endpoint: '/customer/character/getDevelopCharacterHasListFlag', property: 'developHasList', menu: ['5206', 'JOB_ITEM'] },
        { endpoint: '/customer/demand/getDemandHasListFlag', property: 'demandHasList', menu: ['5204', 'DEMAND'] },
        { endpoint: '/customer/pre-project/getPreProjectHasListFlag', property: 'preProjectHasList', menu: ['5207', 'PRE_PROJECT'] },
        { endpoint: '/customer/project/getProjectHasListFlag', property: 'projectHasList', menu: ['5209', 'PROJECT'] },
        { endpoint: '/customer/sub-project/getTestFlagSubprojectListByTM', property: 'testHasList', menu: ['5208', 'QUALITY'] },
        { endpoint: '/customer/project/getSetupProjectHasListFlag', property: 'setupProjectHasList', menu: ['5201', 'PROJECT'] },
        { endpoint: '/customer/job-items/getReviewHasListFlag', property: 'reviewHasList', menu: ['5215', 'JOB_ITEM'] },
        { endpoint: '/customer/project/getProductHasListFlag', property: 'productHasList', menu: ['5218', 'PRODUCT'] },
        { endpoint: '/customer/quality/getQualityHasListFlag', property: 'qualityHasList', menu: ['5213', 'QUALITY'] },
        { endpoint: '/customer/character/getMaterialHasListFlag', property: 'materialHasList', menu: ['5210', 'MATERIAL'] },
        { endpoint: '/customer/job-items/getJobitemHasListFlag', property: 'jobitemHasList', menu: ['5217', 'JOB_ITEM'] }
      ];

      const visible = flagApis.filter(api => this.hasMenuPermission(api.menu[0], api.menu[1]));
      await Promise.all(
        visible.map(api => this.getUserStatusFlag(api.endpoint, userId, api.property))
      );

      if (this.hasMenuPermission('5203', 'FILE')) {
        await this.getWaitForApproveNumByUserId(userId);
      }
    },

    getLoginUser() {
      let _this = this;
      const sessionUser = Tool.getLoginUser() || {};
      // 角色标志一律由 roleList → RoleAccess.applyRoleStarFlags 解析，不再因 CUSTOMER 登录强行 role_ADMIN
      _this.loginUser = Tool.getActorUser();
      _this.loginCustomer = Tool.getLoginCustomer();
      getUserRole();
      _this.directLoginUser = sessionUser;

      function getUserRole() {
        _this.loginUserRoleList = Tool.getLoginUser().roleList || [];
        if (typeof RoleAccess !== 'undefined') {
          RoleAccess.applyRoleStarFlags(_this.userRoles, _this.loginUserRoleList);
        } else {
          const flags = {};
          (_this.loginUserRoleList || []).forEach(r => { flags[r] = true; });
          _this.role_SUPER = !!flags.SUPER;
          _this.role_ADMIN = !!flags.ADMIN;
          _this.role_IPMT = !!flags.IPMT;
          _this.role_UltiMng = !!flags.UltiMng;
          _this.role_PDM = !!flags.PDM;
          _this.role_PDGM = !!flags.PDGM;
          _this.role_SYSGM = !!flags.SYSGM;
          _this.role_PJM = !!flags.PJM;
          _this.role_TM = !!flags.TM;
          _this.role_SM = !!flags.SM;
          _this.role_TGM = !!flags.TGM;
          _this.role_MATM = !!flags.MATM;
        }
        // 平台超管兜底：机构 ID 即超管时强制 SUPER（后端 roleList 偶发未带 SUPER）
        const loginId = Tool.getLoginUser() && Tool.getLoginUser().id;
        const customerId = _this.loginCustomer && _this.loginCustomer.id;
        if (loginId === SUPER_ADMIN_ID || customerId === SUPER_ADMIN_ID) {
          _this.role_SUPER = true;
        }
      }

      // 重新登录/进入布局：允许角标恢复；延迟到空闲后再拉
      this.badgeAuthDead = false;
      const delayRefresh = () => this.scheduleBadgeRefresh();
      if (typeof window !== 'undefined' && window.requestIdleCallback) {
        window.requestIdleCallback(delayRefresh, { timeout: 800 });
      } else {
        setTimeout(delayRefresh, 400);
      }

    },

    /**
     * 查找是否有路由权限（V2.x兼容方式）
     * @param router 路由路径或路由名称
     */
    hasResourceRouter(router) {
      if (!router || router === 'welcome' || router === 'euipdWelcome' || router === '/welcome') {
        return true;
      }
      // 统一走 PermissionUtil：scopes/modulePermissions + resources.page（规范化路由名）
      if (typeof Tool !== 'undefined' && Tool.hasRoutePermission) {
        return Tool.hasRoutePermission(router);
      }
      const loginUser = Tool.getLoginUser();
      if (Tool.isEmpty(loginUser) || Tool.isEmpty(loginUser.resources)) {
        return false;
      }
      const path = router.charAt(0) === '/' ? router.substring(1) : router;
      for (let i = 0; i < loginUser.resources.length; i++) {
        const page = loginUser.resources[i].page;
        if (page === router || page === path || ('/' + page) === router) {
          return true;
        }
      }
      return false;
    },
    /**
     * 查找是否有资源权限（V2.x兼容方式）
     * @param id 资源ID
     */
    hasResource(id) {
      // 直接使用Tool.hasResource，它已经支持双轨权限检查
      return Tool.hasResource(id);
    },
    /**
     * 检查用户是否有模块操作权限（V3.0新增）
     * @param {string} module - 模块代码（如 'PROJECT'）
     * @param {string} operation - 操作类型（如 'CREATE'），默认为'VIEW'
     * @returns {boolean} 是否有权限
     */
    hasModulePermission(module, operation) {
      if (typeof Tool !== 'undefined' && Tool.hasModulePermission) {
        return Tool.hasModulePermission(module, operation || 'VIEW');
      }
      return false;
    },
    /**
     * 检查菜单项是否应该显示（V3.0增强）
     * 优先使用模块权限，如果没有模块权限配置则回退到资源权限
     * @param {string} resourceId - 资源ID（用于兼容旧权限）
     * @param {string} moduleCode - 模块代码（V3.0新方式）
     * @param {string} operation - 操作类型，默认为'VIEW'
     * @returns {boolean} 是否显示菜单
     */
    /**
     * 侧栏标准化显隐：角色门禁 + 菜单权限（规则见 RoleAccess.SIDEBAR_MENUS）
     */
    canShowSidebar(menuKey) {
      if (typeof RoleAccess !== 'undefined') {
        return RoleAccess.canShowSidebar(menuKey, {
          roleList: this.loginUserRoleList
        });
      }
      return false;
    },

    /** 页内：是否具备任一角色 */
    hasAnyRole(roleCodes) {
      if (typeof RoleAccess !== 'undefined') {
        return RoleAccess.hasAny(roleCodes, this.loginUserRoleList);
      }
      return false;
    },

    hasMenuPermission(resourceId, moduleCode, operation) {
      if (typeof Tool !== 'undefined' && Tool.hasMenuPermission) {
        return Tool.hasMenuPermission(resourceId, moduleCode, operation || 'VIEW');
      }
      return this.hasResource(resourceId);
    },

    /** 写操作按钮是否可点 */
    canDo(module, operation) {
      return this.hasModulePermission(module, operation || 'VIEW');
    },

    login() {
      this.$router.push("/admin")
    },

    /** 路由对应的侧栏项：邮件各文件夹共用“邮箱”一项 */
    sidebarIdOf(route) {
      if (route.name === 'customer/mail') {
        return 'mail-box-sidebar';
      }
      if (route.name === 'customer/mail-compose') {
        return 'mail-compose-sidebar';
      }
      return route.name.replace("/", "-") + "-sidebar";
    },

    refreshMailUnread() {
      const customerId = this.loginCustomer && this.loginCustomer.id;
      if (!customerId || customerId === SUPER_ADMIN_ID || !this.canShowSidebar('mail') || !this.isBadgeAuthOk()) {
        return;
      }
      if (typeof document !== 'undefined' && document.hidden) {
        return;
      }
      countMailBox().then(c => {
        const next = (c && c.inbox) || 0;
        const added = next - this.mailUnread;
        const arrived = this.mailUnreadLoaded && added > 0;
        if (arrived && this.$route.name !== 'customer/mail') {
          this.$notify({ title: '新邮件', message: '收到 ' + added + ' 封新邮件', position: 'bottom-right',
            type: 'info', duration: 5000, onClick: () => this.$router.push('/customer/mail/inbox').catch(() => {}) });
        }
        this.mailUnread = next;
        this.mailUnreadLoaded = true;
        if (arrived && this.$bus && this.$bus.publish) {
          this.$bus.publish({ areas: ['mail'], reason: 'mail-arrived' });
        }
      }).catch(() => {});
    },

    /**
     * 菜单激活样式，id是当前点击的菜单的id
     */
    activeSidebar: function (id) {
      // 先清掉全部菜单的选中态，再只点亮当前路由对应的一项
      $(".ca-sidebar .ca-menu-item").removeClass("active");
      $("#" + id).addClass("active");

      // 找到自己的父级菜单
      let parentMenu = $("#" + id).parent(".ca-menu-group");
      // 将其他父级菜单下的 子菜单去掉active
      if (parentMenu.length > 0) {
        if (parentMenu[0].id === 'super-manage-menu') {
          $("#develop-manage-menu").find(".ca-menu-item").removeClass("active");
          $("#file-manage-menu").find(".ca-menu-item").removeClass("active");
        }
        if (parentMenu[0].id === 'develop-manage-menu') {
          $("#super-manage-menu").find(".ca-menu-item").removeClass("active");
          $("#file-manage-menu").find(".ca-menu-item").removeClass("active");
        }
        if (parentMenu[0].id === 'file-manage-menu') {
          $("#super-manage-menu").find(".ca-menu-item").removeClass("active");
          $("#develop-manage-menu").find(".ca-menu-item").removeClass("active");
        }
      }
      // console.log("parentMenu=", parentMenu);
    },

    logout() {
      return this.$logout();
    },

    // 这些方法已经被 refreshUserStatusFlags 方法替代
    // 保留以保证向后兼容性
    getDemandHasListFlag(userId) {
      return this.getUserStatusFlag('/customer/demand/getDemandHasListFlag', userId, 'demandHasList');
    },

    getPreProjectHasListFlag(userId) {
      return this.getUserStatusFlag('/customer/pre-project/getPreProjectHasListFlag', userId, 'preProjectHasList');
    },

    getProjectHasListFlag(userId) {
      return this.getUserStatusFlag('/customer/project/getProjectHasListFlag', userId, 'projectHasList');
    },

    getTestHasListFlag(userId) {
      return this.getUserStatusFlag('/customer/sub-project/getTestFlagSubprojectListByTM', userId, 'testHasList');
    },

    /**
     * 获取立项项目列表标志
     */
    async getSetupProjectHasListFlag(userId) {
      const url = `${process.env.VUE_APP_SERVER}/customer/project/getSetupProjectHasListFlag/${userId}`;
      const result = await this.apiCall(url);
      if (result !== null) {
        this.setupProjectHasList = result;
      }
    },

    /**
     * 获取审查列表标志
     */
    async getReviewHasListFlag(userId) {
      const url = `${process.env.VUE_APP_SERVER}/customer/job-items/getReviewHasListFlag/${userId}`;
      const result = await this.apiCall(url);
      if (result !== null) {
        this.reviewHasList = result;
      }
    },

    /**
     * 获取产品列表标志
     */
    async getProductHasListFlag(userId) {
      const url = `${process.env.VUE_APP_SERVER}/customer/project/getProductHasListFlag/${userId}`;
      const result = await this.apiCall(url);
      if (result !== null) {
        this.productHasList = result;
      }
    },

    /**
     * 获取质量列表标志
     */
    async getQualityHasListFlag(userId) {
      const url = `${process.env.VUE_APP_SERVER}/customer/quality/getQualityHasListFlag/${userId}`;
      const result = await this.apiCall(url);
      if (result !== null) {
        this.qualityHasList = result;
      }
    },

    /**
     * 获取物料列表标志
     */
    async getMaterialHasListFlag(userId) {
      const url = `${process.env.VUE_APP_SERVER}/customer/character/getMaterialHasListFlag/${userId}`;
      const result = await this.apiCall(url);
      if (result !== null) {
        this.materialHasList = result;
      }
    },

    /**
     * 获取任务项列表标志
     */
    async getJobitemHasListFlag(userId) {
      const url = `${process.env.VUE_APP_SERVER}/customer/job-items/getJobitemHasListFlag/${userId}`;
      const result = await this.apiCall(url);
      if (result !== null) {
        this.jobitemHasList = result;
      }
    },

    /**
     * 获取character任务提交通知
     */
    async getCharacterJobNotify_submit() {
      const url = `${process.env.VUE_APP_SERVER}/customer/pre-project/getCharacterJobNotify`;
      const params = {
        customerId: this.loginCustomer.id,
        actorId: this.loginUser.customerUserId,
        projectType: PROJECT_TYPE.PRE_PROJECT.key,
        status: JOB_ITEM_STATUS.SUBMIT.key
      };
      const result = await this.apiCall(url, params);
      if (result) {
        this.characterJobFlag = result.flag;
      }
    },
    /**
     * 获取character任务评估通知
     */
    async getCharacterJobNotify_evaluate() {
      const url = `${process.env.VUE_APP_SERVER}/customer/pre-project/getCharacterJobNotify`;
      const params = {
        customerId: this.loginCustomer.id,
        actorId: this.loginUser.customerUserId,
        projectType: PROJECT_TYPE.PRE_PROJECT.key,
        status: JOB_ITEM_STATUS.EVALUATE.key
      };
      const result = await this.apiCall(url, params);
      if (result) {
        this.characterJobFlag_evaluate = result.flag;
      }
    },

    /**
     * 获取需求任务通知
     */
    async getDemandJobNotify() {
      const url = `${process.env.VUE_APP_SERVER}/customer/pre-project/getDemandJobNotify`;
      const params = {
        customerId: this.loginCustomer.id,
        actorId: this.loginUser.customerUserId,
        status: JOB_ITEM_STATUS.SUBMIT.key
      };
      const result = await this.apiCall(url, params);
      if (result) {
        this.demandJobFlag = result.flag;
      }
    },

    /**
     * 获取项目经理项目任务通知数量
     */
    async getPjmProjectJobNotifyNum() {
      const url = `${process.env.VUE_APP_SERVER}/customer/job-items/getPjmProjectJobNotifyNum`;
      const params = {
        customerId: this.loginCustomer.id,
        projectManagerId: this.loginUser.customerUserId
      };
      const result = await this.apiCall(url, params);
      if (result !== null) {
        this.projectNotifyNum = result;
      }
    },

    /**
     * 获取产品经理项目任务通知数量
     */
    async getPdmProjectJobNotifyNum() {
      const url = `${process.env.VUE_APP_SERVER}/customer/job-items/getPdmProjectJobNotifyNum`;
      const params = {
        customerId: this.loginCustomer.id,
        productManagerId: this.loginUser.customerUserId
      };
      const result = await this.apiCall(url, params);
      if (result !== null) {
        this.productNotifyNum = result;
      }
    },

    // 这些方法已经被 refreshUserStatusCounts 方法替代
    // 保留以保证向后兼容性
    countJobitemsByLoginUserId(userId) {
      return this.getUserStatusCount('/customer/job-items/countJobitemsByLoginUserId', userId, 'jobCount');
    },

    countSetupProjectBySupervise(userId) {
      return this.getUserStatusCount('/customer/project/countSetupProjectBySupervise', userId, 'setupCount');
    },

    countDevelopJobFlag(userId) {
      return this.getUserStatusCount('/customer/job-items/getNotifyDevelopNum', userId, 'developNum');
    },

    /**
     * 统计质量任务标志
     */
    async countQualityJobFlag(userId) {
      const url = `${process.env.VUE_APP_SERVER}/customer/job-items/countQualityJobFlag/${userId}`;
      const result = await this.apiCall(url);
      if (result !== null) {
        this.qualitySubmitNum = result;
      }
    },

    /**
     * 统计物料申请标志
     */
    async countMaterialAppFlag(userId) {
      const url = `${process.env.VUE_APP_SERVER}/customer/material/getMaterialApplicationNum/${userId}`;
      const result = await this.apiCall(url);
      if (result !== null) {
        this.materialAppNum = result;
      }
    },

    /**
     * 统计项目物料申请标志
     */
    async countMaterialAppFlag_project(userId) {
      const url = `${process.env.VUE_APP_SERVER}/customer/material/getMaterialApplicationNum_project/${userId}`;
      const result = await this.apiCall(url);
      if (result !== null) {
        this.materialAppNum_project = result;
      }
    },

    /**
     * 统计费用申请标志
     */
    async countFeeAppFlag(userId) {
      const url = `${process.env.VUE_APP_SERVER}/customer/fee/getFeeApplicationNum/${userId}`;
      const result = await this.apiCall(url);
      if (result !== null) {
        this.feeAppNum = result;
      }
    },

    /**
     * 统计项目费用申请标志
     */
    async countFeeAppFlag_project(userId) {
      const url = `${process.env.VUE_APP_SERVER}/customer/fee/getFeeApplicationNum_project/${userId}`;
      const result = await this.apiCall(url);
      if (result !== null) {
        this.feeAppNum_project = result;
      }
    },

    /**
     * 获取开发character列表标志
     */
    async getDevelopCharacterHasListFlag(userId) {
      const url = `${process.env.VUE_APP_SERVER}/customer/character/getDevelopCharacterHasListFlag/${userId}`;
      const result = await this.apiCall(url);
      if (result !== null) {
        this.developHasList = result;
      }
    },


    /**
     * 获取下一节点审查character数量
     */
    async getNumOfReviewCharactersByNextNode(userId) {
      const url = `${process.env.VUE_APP_SERVER}/customer/character/getNumOfReviewCharactersByNextNode/${userId}`;
      const result = await this.apiCall(url);
      if (result !== null) {
        this.reviewJobNum = result;
      }
    },

    /**
     * 获取里程碑审查任务数量
     */
    async getNumOfMilestoneReviewByNextNodeAndStatus(userId) {
      const url = `${process.env.VUE_APP_SERVER}/customer/job-items/getNumOfMilestoneReviewByNextNodeAndStatus/${userId}`;
      const result = await this.apiCall(url);
      if (result !== null) {
        this.reviewMilestoneJobNum = result;
      }
    },

    /**
     * 获取下一节点审查子项目数量
     */
    async getNumOfReviewSubprojectByNextNode(userId) {
      const url = `${process.env.VUE_APP_SERVER}/customer/sub-project/getNumOfReviewSubprojectByNextNode/${userId}`;
      const result = await this.apiCall(url);
      if (result !== null) {
        this.reviewSubprojectNum = result;
      }
    },

    /**
     * 获取用户审查相关提交任务数量
     */
    async getReviewRelatedSubmitJobNumByUserId(userId) {
      const url = `${process.env.VUE_APP_SERVER}/customer/job-items/getReviewRelatedSubmitJobNumByUserId/${userId}`;
      const result = await this.apiCall(url);
      if (result !== null) {
        this.reviewRelatedJobNum = result;
      }
    },

    /**
     * 获取用户更新character数量
     */
    async getUpdateCharacterNumByUserId(userId) {
      const url = `${process.env.VUE_APP_SERVER}/customer/character/getUpdateCharacterNumByUserId/${userId}`;
      const result = await this.apiCall(url);
      if (result !== null) {
        this.updateCharacterNum = result;
      }
    },

    /**
     * 获取用户测试管理任务数量
     */
    async getTestManageJobNumByUserId(userId) {
      const url = `${process.env.VUE_APP_SERVER}/customer/job-items/getTestManageJobNumByUserId/${userId}`;
      const result = await this.apiCall(url);
      if (result !== null) {
        this.testManageJobNum = result;
      }
    },

    /**
     * 获取用户特性管理任务数量
     */
    async getFunctionManageJobNumByUserId(userId) {
      const url = `${process.env.VUE_APP_SERVER}/customer/job-items/getFunctionManageJobNumByUserId/${userId}`;
      const result = await this.apiCall(url);
      if (result !== null) {
        this.functionManageJobNum = result;
      }
    },

    /**
     * 获取用户预算管理任务数量
     */
    async getBudgetManageJobNumByUserId(userId) {
      const url = `${process.env.VUE_APP_SERVER}/customer/budget-mng/getBudgetManageJobNumByUserId/${userId}`;
      const result = await this.apiCall(url);
      if (result !== null) {
        this.budgetManageJobNum = result;
      }
    },

    /**
     * 获取用户质量复检数量
     */
    async getQualityRecheckNumByUserId(userId) {
      const url = `${process.env.VUE_APP_SERVER}/customer/quality/getQualityRecheckNumByUserId/${userId}`;
      const result = await this.apiCall(url);
      if (result !== null) {
        this.qualityRecheckNum = result;
      }
    },

    /**
     * 获取等待审批数量
     */
    async getWaitForApproveNumByUserId(userId) {
      const url = `${process.env.VUE_APP_SERVER}/file/rdms/getApplyNum`;
      const params = {
        customerId: this.loginCustomer.id,
        executorId: userId
      };
      const result = await this.apiCall(url, params);
      if (result !== null) {
        this.waitForApproveNum = result;
      }
    },

    /**
     * 获取PDM质量定义提交数量
     */
    async getQualityDefineSubmitNumByPdm(userId) {
      const url = `${process.env.VUE_APP_SERVER}/customer/quality/getQualityDefineSubmitNumByPdm/${userId}`;
      const result = await this.apiCall(url);
      if (result !== null) {
        this.qualityDefineSubmitNum = result;
      }
    },

    /**
     * 获取用户质量物料申请数量
     */
    async getQualityMaterialApplicationNumByUserId(userId) {
      const url = `${process.env.VUE_APP_SERVER}/customer/material/getQualityMaterialApplicationNumByUserId/${userId}`;
      const result = await this.apiCall(url);
      if (result !== null) {
        this.qualityMaterialApplicationNum = result;
      }
    },

    /**
     * 获取用户质量费用申请数量
     */
    async getQualityFeeApplicationNumByUserId(userId) {
      const url = `${process.env.VUE_APP_SERVER}/customer/fee/getQualityFeeApplicationNumByUserId/${userId}`;
      const result = await this.apiCall(url);
      if (result !== null) {
        this.qualityFeeApplicationNum = result;
      }
    }
  }
}
</script>

<style lang="scss" scoped>
@import "../../../public/static/scss/sidebar";

/* 强制分组标题横向排列（避免被全局 a/div 样式冲掉） */
.ca-menu > a {
  display: block;
  width: 100%;
}

.ca-menu-title-form {
  display: flex !important;
  flex-direction: row !important;
  flex-wrap: nowrap !important;
  align-items: center !important;
  box-sizing: border-box;
  width: 100%;
  max-width: 190px;
  height: 40px;
}

.ca-menu-title-form .menu-title {
  flex: 1 1 auto;
  white-space: nowrap;
  line-height: 40px;
}

.ca-menu-title-form .menu-spread {
  flex: 0 0 auto;
  display: flex;
  align-items: center;
}

.admin-header {
  background-color: $mainBackground;
}

.header-font {
  color: $fontOnBackground;
  font-size: 20px;
}

.navbar-brand {
  float: left;
  height: 50px;
  padding: 15px 0;
  font-size: 18px;
  line-height: 20px;
}

.navbar-brand:hover,
.navbar-brand:focus {
  text-decoration: none;
}

.navbar-brand>img {
  display: block;
}

/* MenuItem组件样式通过父级CSS自然继承，无需额外定义 */
</style>

<!--2024年1月8日迁移到EUIPD Gitlab-->
