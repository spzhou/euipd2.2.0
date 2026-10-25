/*
 * Author: 周朔鹏
 * Date: 2025-11-27
 * Email: 391902958@qq.com
 * Website: https://www.euipd.com
 * 
 * V3.0 权限工具类 - 支持模块级权限检查
 */

/**
 * 资源ID到模块代码的映射表
 * 用于兼容旧的资源ID权限系统
 */
const RESOURCE_TO_MODULE_MAP = {
  // 系统管理
  '5101': ['CUSTOMER'],      // 机构/团队管理
  '5103': ['ROLE'],          // 权限管理
  
  // 研发管理
  '5200': ['SUMMARY'],       // 绩效/盘点
  '5201': ['PROJECT'],       // 立项管理
  '5202': ['MANHOUR', 'JOB_ITEM'],  // 执行管理（工时/工作项）
  '5203': ['FILE'],          // 档案管理
  '5204': ['DEMAND'],        // 需求识别
  '5205': ['CBB'],           // 特性仓库
  '5206': ['JOB_ITEM', 'WORKER'],    // 研发管理
  '5207': ['PRE_PROJECT', 'CHARACTER'],  // 预立项管理
  '5208': ['QUALITY'],       // 测试管理
  '5209': ['PROJECT', 'SUB_PROJECT', 'MILESTONE', 'GANTT'],  // 项目管理
  '5210': ['MATERIAL'],      // 物料管理
  '5211': ['QUALITY'],       // 质量反馈
  '5212': ['WORKER'],        // 团队管理
  '5213': ['QUALITY'],       // 质量管理
  '5215': ['JOB_ITEM'],      // 评审管理（工作项审批）
  '5216': ['PERFORMANCE'],   // 绩效管理
  '5217': ['JOB_ITEM'],      // 工期管理
  '5218': ['PRODUCT'],       // 产品管理
  '5219': ['FILE'],          // 文档下载（文件导出）
  '5220': ['BUDGET', 'BUDGET_RESEARCH'],  // 预算管理
  '5221': ['JOB_ITEM'],      // 系统工程（工作项）
  '5223': ['WAREHOUSE'],     // 仓库管理
  '5225': ['INVENTORY'],     // 库存管理
  '5227': ['FLOW'],          // 流程配置
  '5301': ['MAIL']           // 我的邮件
};

/**
 * 权限模块枚举（与后端PermissionModuleEnum对应）
 */
const PERMISSION_MODULES = {
  // 系统管理
  SYSTEM: { code: 'SYSTEM', name: '系统管理', pathPrefix: '/system/' },
  USER: { code: 'USER', name: '用户管理', pathPrefix: '/system/rdms/user/' },
  CUSTOMER: { code: 'CUSTOMER', name: '客户管理', pathPrefix: '/system/rdms/customer/' },
  ROLE: { code: 'ROLE', name: '角色管理', pathPrefix: '/system/rdms/role/' },
  RESOURCE: { code: 'RESOURCE', name: '资源管理', pathPrefix: '/system/rdms/resource/' },
  AUTH2: { code: 'AUTH2', name: '外部接口', pathPrefix: '/system/auth2/api/' },
  
  // 项目管理
  PROJECT: { code: 'PROJECT', name: '项目管理', pathPrefix: '/customer/project/' },
  PRE_PROJECT: { code: 'PRE_PROJECT', name: '预研管理', pathPrefix: '/customer/pre-project/' },
  SUB_PROJECT: { code: 'SUB_PROJECT', name: '子项目管理', pathPrefix: '/customer/sub-project/' },
  MILESTONE: { code: 'MILESTONE', name: '里程碑管理', pathPrefix: '/customer/milestone/' },
  GANTT: { code: 'GANTT', name: '甘特图', pathPrefix: '/customer/gantt/' },
  PROJECT_TASK: { code: 'PROJECT_TASK', name: '项目任务', pathPrefix: '/customer/project-task/' },
  
  // 预算管理
  BUDGET: { code: 'BUDGET', name: '预算管理', pathPrefix: '/customer/budget/' },
  BUDGET_RESEARCH: { code: 'BUDGET_RESEARCH', name: '研发预算', pathPrefix: '' },
  BUDGET_SHEET: { code: 'BUDGET_SHEET', name: '预算表单', pathPrefix: '' },
  FEE: { code: 'FEE', name: '费用管理', pathPrefix: '/customer/fee/' },
  FLOW: { code: 'FLOW', name: '流程配置', pathPrefix: '/customer/flow/' },
  
  // 工作项管理
  JOB_ITEM: { code: 'JOB_ITEM', name: '工作项管理', pathPrefix: '/customer/job-items/' },
  JOB_ITEM_PROCESS: { code: 'JOB_ITEM_PROCESS', name: '工作项工序', pathPrefix: '/customer/job-item-process/' },
  JOB_LEVEL: { code: 'JOB_LEVEL', name: '工作层级', pathPrefix: '/customer/job-level/' },
  
  // 人员管理
  WORKER: { code: 'WORKER', name: '员工管理', pathPrefix: '/customer/worker/' },
  MEMBER: { code: 'MEMBER', name: '成员查询', pathPrefix: '/customer/worker/listAllWorkers' },
  MANHOUR: { code: 'MANHOUR', name: '工时管理', pathPrefix: '/customer/manhour/' },
  MANHOUR_DECLARE: { code: 'MANHOUR_DECLARE', name: '工时申报', pathPrefix: '/customer/manhour-declare/' },
  PERFORMANCE: { code: 'PERFORMANCE', name: '绩效管理', pathPrefix: '/customer/performance/' },
  
  // 组织管理
  DEPARTMENT: { code: 'DEPARTMENT', name: '部门管理', pathPrefix: '/customer/department/' },
  BOSS: { code: 'BOSS', name: '最终管理人管理', pathPrefix: '/customer/boss/' },
  DIRECTOR: { code: 'DIRECTOR', name: '主管管理', pathPrefix: '/customer/pdgm-sysgm/' },
  
  // 产品管理
  PRODUCT: { code: 'PRODUCT', name: '产品管理', pathPrefix: '/customer/product/' },
  PRODUCT_MANAGER: { code: 'PRODUCT_MANAGER', name: '产品经理', pathPrefix: '/customer/product-manager/' },
  CHARACTER: { code: 'CHARACTER', name: '产品特性', pathPrefix: '/customer/character/' },
  CHARACTER_PROCESS: { code: 'CHARACTER_PROCESS', name: '特性流程', pathPrefix: '/customer/character-process/' },
  CATEGORY: { code: 'CATEGORY', name: '分类管理', pathPrefix: '/customer/category/' },
  
  // 物料管理
  MATERIAL: { code: 'MATERIAL', name: '物料管理', pathPrefix: '/customer/material/' },
  CBB: { code: 'CBB', name: '特性仓库', pathPrefix: '/customer/cbb/' },
  
  // 质量管理
  QUALITY: { code: 'QUALITY', name: '质量管理', pathPrefix: '/customer/quality/' },
  BUG_FEEDBACK: { code: 'BUG_FEEDBACK', name: '缺陷反馈', pathPrefix: '/customer/bugfeedback/' },
  
  // 需求管理
  DEMAND: { code: 'DEMAND', name: '需求管理', pathPrefix: '/customer/demand/' },
  
  // 文件管理
  FILE: { code: 'FILE', name: '文件管理', pathPrefix: '/file/' },
  
  // 研发物料仓库与库存
  WAREHOUSE: { code: 'WAREHOUSE', name: '仓库管理', pathPrefix: '/customer/material-warehouse/' },
  INVENTORY: { code: 'INVENTORY', name: '库存管理', pathPrefix: '/customer/material-stock/' },
  
  // 课程管理
  COURSE: { code: 'COURSE', name: '课程管理', pathPrefix: '/customer/course/' },
  CHAPTER: { code: 'CHAPTER', name: '章节管理', pathPrefix: '/customer/chapter/' },
  SECTION: { code: 'SECTION', name: '小节管理', pathPrefix: '/customer/section/' },
  COURSE_CATEGORY: { code: 'COURSE_CATEGORY', name: '课程分类', pathPrefix: '' },
  TEACHER: { code: 'TEACHER', name: '教师管理', pathPrefix: '' },
  
  // 其他
  SUMMARY: { code: 'SUMMARY', name: '汇总统计', pathPrefix: '/customer/summary/' },
  GITLAB: { code: 'GITLAB', name: 'GitLab集成', pathPrefix: '/customer/api/gitlab/' },
  WEB: { code: 'WEB', name: 'Web公开接口', pathPrefix: '/customer/web/' },
  MAIL: { code: 'MAIL', name: '内部邮件', pathPrefix: '/customer/mail/' },
  COMMON: { code: 'COMMON', name: '通用权限', pathPrefix: '' }
};

/**
 * 操作类型枚举（与后端OperationType对应）
 */
const OPERATION_TYPES = {
  VIEW: { code: 'VIEW', name: '查看' },
  CREATE: { code: 'CREATE', name: '创建' },
  UPDATE: { code: 'UPDATE', name: '更新' },
  DELETE: { code: 'DELETE', name: '删除' },
  APPROVE: { code: 'APPROVE', name: '审批' },
  EXPORT: { code: 'EXPORT', name: '导出' },
  IMPORT: { code: 'IMPORT', name: '导入' },
  DOWNLOAD: { code: 'DOWNLOAD', name: '下载' },
  ALL: { code: '*', name: '全部权限' }
};

/**
 * 权限工具类（全局对象）
 */
PermissionUtil = {
  /** 平台超管：id / SUPER 角色 / PLATFORM:* */
  isPlatformSuperUser: function (loginUser) {
    const u = loginUser || (typeof Tool !== 'undefined' ? Tool.getLoginUser() : null);
    if (Tool.isEmpty(u)) {
      return false;
    }
    const sid = (typeof SUPER_ADMIN_ID !== 'undefined') ? SUPER_ADMIN_ID : 'fm4ek0Zy';
    if (u.id === sid) {
      return true;
    }
    const roles = u.roleList || [];
    if (roles.indexOf('SUPER') >= 0) {
      return true;
    }
    const scopes = u.scopes || [];
    return scopes.indexOf('PLATFORM:*') >= 0;
  },

  /**
   * 检查用户是否有模块操作权限（V3.0新方式）
   * @param {string} module - 模块代码（如 'PROJECT'）
   * @param {string} operation - 操作类型（如 'CREATE'），默认为'VIEW'
   * @returns {boolean} 是否有权限
   */
  hasModulePermission: function(module, operation = 'VIEW') {
    if (!module) {
      return false;
    }
    
    const loginUser = Tool.getLoginUser();
    if (Tool.isEmpty(loginUser)) {
      return false;
    }

    if (this.isPlatformSuperUser(loginUser)) {
      return true;
    }

    // Prefer JWT scopes (MODULE:ACTION)
    const scopes = loginUser.scopes;
    if (scopes && scopes.length) {
      if (scopes.indexOf('PLATFORM:*') >= 0) {
        return true;
      }
      if (scopes.indexOf(module + ':*') >= 0) {
        return true;
      }
      if (scopes.indexOf(module + ':' + (operation || 'VIEW')) >= 0) {
        return true;
      }
    }
    
    // modulePermissions map（数组 / Set / 普通对象值）
    if (loginUser.modulePermissions && typeof loginUser.modulePermissions === 'object') {
      const modulePerms = loginUser.modulePermissions[module];
      if (modulePerms && (Array.isArray(modulePerms) || modulePerms instanceof Set)) {
        const list = Array.isArray(modulePerms) ? modulePerms : Array.from(modulePerms);
        return list.includes('*') || list.includes(operation);
      }
      // 兼容某些序列化把 Set 变成 {0:'VIEW'} 的情况
      if (modulePerms && typeof modulePerms === 'object') {
        const list = Object.keys(modulePerms).map(function (k) { return modulePerms[k]; });
        return list.indexOf('*') >= 0 || list.indexOf(operation) >= 0;
      }
    }
    
    return false;
  },
  
  /**
   * 按钮权限（opt-in 管控）：
   * - 未在 buttonPolicy.managedIds 登记 → 默认显示
   * - 已登记 → 仅 allowedIds 含之可见
   * - 平台 SUPER → 放行
   */
  hasButtonPermission: function (buttonId) {
    if (!buttonId) {
      return true;
    }
    const loginUser = (typeof Tool !== 'undefined' && Tool.getLoginUser) ? Tool.getLoginUser() : null;
    if (this.isPlatformSuperUser(loginUser)) {
      return true;
    }
    const policy = (loginUser && loginUser.buttonPolicy) ? loginUser.buttonPolicy : {};
    const managed = policy.managedIds || policy.managedButtonIds || [];
    if (!managed.length || managed.indexOf(buttonId) < 0) {
      return true;
    }
    const allowed = policy.allowedIds || policy.allowedButtonIds || [];
    return allowed.indexOf(buttonId) >= 0;
  },

  /**
   * 资源/按钮权限：严格匹配 loginUser.resources[].id（含 6 位按钮 ID）
   * 禁止经 RESOURCE_TO_MODULE_MAP 放宽（侧栏请用 hasMenuPermission）
   */
  hasResource: function(resourceId) {
    if (!resourceId) {
      return false;
    }

    const loginUser = Tool.getLoginUser();
    if (Tool.isEmpty(loginUser)) {
      return false;
    }

    const scopes = loginUser.scopes;
    if (scopes && scopes.length && scopes.indexOf('PLATFORM:*') >= 0) {
      return true;
    }

    const resources = loginUser.resources;
    if (resources && Array.isArray(resources)) {
      for (let i = 0; i < resources.length; i++) {
        if (resourceId === resources[i].id) {
          return true;
        }
      }
    }

    return false;
  },

  /**
   * 侧栏菜单：scopes/modulePermissions 优先，resources 资源 ID 严格匹配兜底
   * @param {string} resourceId - 4 位菜单资源 ID
   * @param {string} [moduleCode] - 模块码（如 PROJECT）
   * @param {string} [operation='VIEW']
   */
  hasMenuPermission: function(resourceId, moduleCode, operation) {
    const op = operation || 'VIEW';
    const loginUser = Tool.getLoginUser();
    if (Tool.isEmpty(loginUser)) {
      return false;
    }

    if (this.isPlatformSuperUser(loginUser)) {
      return true;
    }

    const policy = loginUser.resourcePolicy || {};
    const managed = policy.managedIds || [];
    if (resourceId && managed.indexOf(resourceId) >= 0) {
      const allowed = policy.allowedIds || [];
      return allowed.indexOf(resourceId) >= 0;
    }

    if (moduleCode && this.hasModulePermission(moduleCode, op)) {
      return true;
    }

    // 资源 ID 严格匹配（不经 RESOURCE_TO_MODULE_MAP）
    if (resourceId && this.hasResource(resourceId)) {
      return true;
    }

    return false;
  },

  /**
   * 规范化路由名/路径为以 / 开头的 path（如 customer/project → /customer/project）
   */
  normalizeRoutePath: function(routePath) {
    if (!routePath || typeof routePath !== 'string') {
      return '';
    }
    let p = routePath.trim();
    if (p.indexOf('http') === 0) {
      try {
        p = new URL(p).pathname;
      } catch (e) { /* ignore */ }
    }
    // 去掉网关前缀后的绝对 path
    const idx = p.indexOf('/system/');
    const idx2 = p.indexOf('/customer/');
    const idx3 = p.indexOf('/file/');
    const cut = [idx, idx2, idx3].filter(function (i) { return i >= 0; }).sort(function (a, b) { return a - b; })[0];
    if (typeof cut === 'number') {
      p = p.substring(cut);
    }
    if (p.charAt(0) !== '/') {
      p = '/' + p;
    }
    return p;
  },

  /**
   * 前端路由名 → 菜单资源 ID + 模块（与 admin-v2 侧栏对齐；UI path 不一定等于 API 前缀）
   */
  UI_ROUTE_MENU_MAP: {
    'system/customer': { resourceId: '5101', module: 'CUSTOMER' },
    'system/customer-role': { resourceId: '5103', module: 'ROLE' },
    'customer/worker': { resourceId: '5212', module: 'WORKER' },
    'system/tenant-ui-policy': { resourceId: '5226', module: 'WORKER' },
    'customer/work-calendar': { resourceId: '5228', module: 'WORKER' },
    'customer/mail': { resourceId: '5301', module: 'MAIL' },
    'customer/mail-compose': { resourceId: '5301', module: 'MAIL' },
    'customer/budget': { resourceId: '5220', module: 'BUDGET' },
    'customer/summary': { resourceId: '5200', module: 'SUMMARY' },
    'customer/module': { resourceId: '5205', module: 'CBB' },
    'customer/demand': { resourceId: '5204', module: 'DEMAND' },
    'customer/character': { resourceId: '5207', module: 'PRE_PROJECT' },
    'customer/function': { resourceId: '5221', module: 'JOB_ITEM' },
    'customer/setup': { resourceId: '5201', module: 'PROJECT' },
    'customer/project': { resourceId: '5209', module: 'PROJECT' },
    'customer/develop': { resourceId: '5206', module: 'JOB_ITEM' },
    'customer/test': { resourceId: '5208', module: 'QUALITY' },
    'customer/manhour': { resourceId: '5202', module: 'MANHOUR' },
    'customer/material': { resourceId: '5210', module: 'MATERIAL' },
    'customer/jobitem': { resourceId: '5217', module: 'JOB_ITEM' },
    'customer/review': { resourceId: '5215', module: 'JOB_ITEM' },
    'customer/product': { resourceId: '5218', module: 'PRODUCT' },
    'customer/performance': { resourceId: '5216', module: 'PERFORMANCE' },
    'customer/quality': { resourceId: '5213', module: 'QUALITY' },
    'customer/qualitydefine': { resourceId: '5211', module: 'QUALITY' },
    'file/file': { resourceId: '5203', module: 'FILE' },
    'customer/download': { resourceId: '5219', module: 'FILE' },
    'customer/warehouse': { resourceId: '5223', module: 'WAREHOUSE' },
    'customer/inventory': { resourceId: '5225', module: 'INVENTORY' },
    'customer/flow': { resourceId: '5227', module: 'FLOW' }
  },

  /**
   * 检查用户是否有路由权限（scopes / modulePermissions / resources.page）
   * 不再依赖已废弃的 requests[]
   * @param {string} routePath - 路由路径或名称（如 '/customer/project' 或 'customer/project'）
   * @returns {boolean}
   */
  hasRoutePermission: function(routePath) {
    if (!routePath) {
      return false;
    }
    if (routePath === 'welcome' || routePath === 'euipdWelcome' || routePath === '/welcome') {
      return true;
    }

    const loginUser = Tool.getLoginUser();
    if (Tool.isEmpty(loginUser)) {
      return false;
    }

    const path = this.normalizeRoutePath(routePath);
    const pageKey = path.charAt(0) === '/' ? path.substring(1) : path;

    // 团队管理：机构 UltiMng / ADMIN（禁止超管直链；侧栏同构 hasMenuPermission）
    if (pageKey === 'customer/worker' || path === '/customer/worker') {
      if (this.isPlatformSuperUser(loginUser)) {
        return false;
      }
      return this.hasMenuPermission('5212', 'WORKER', 'VIEW');
    }

    // 0) 与侧栏同构：已映射 UI 路由只看 hasMenuPermission（JSON resources/scopes），不再用模块前缀放宽
    const uiMeta = this.UI_ROUTE_MENU_MAP[pageKey] || this.UI_ROUTE_MENU_MAP[routePath];
    if (uiMeta) {
      return this.hasMenuPermission(uiMeta.resourceId, uiMeta.module, 'VIEW');
    }

    // 1) 未映射路由：scopes / modulePermissions（按 API 前缀匹配模块 VIEW）
    const moduleCode = this.matchModuleByApiPath(path);
    if (moduleCode && this.hasModulePermission(moduleCode, 'VIEW')) {
      return true;
    }
    const moduleCode2 = this.matchModuleByApiPath(path.endsWith('/') ? path : path + '/');
    if (moduleCode2 && this.hasModulePermission(moduleCode2, 'VIEW')) {
      return true;
    }

    // 2) resources[].page（租户 JSON / 旧资源树）
    const resources = loginUser.resources;
    if (resources && Array.isArray(resources)) {
      for (let i = 0; i < resources.length; i++) {
        const page = resources[i].page;
        if (!page) continue;
        if (page === routePath || page === path || page === pageKey
            || ('/' + page) === path || page === path.substring(1)) {
          return true;
        }
      }
    }

    return false;
  },

  /**
   * 与后端 module-path.yml 逐条一致：路径包含前缀即命中，多条命中取最长前缀，等长取先出现者
   */
  API_PATH_MODULE_MAP: [
    { prefix: '/system/rdms/user/', module: 'USER' },
    { prefix: '/system/rdms/customer/getOutSourcingCustomerList/', module: 'SUB_PROJECT' },
    { prefix: '/system/rdms/customer/initDefaultDepartment', module: 'WORKER' },
    { prefix: '/system/rdms/customer/', module: 'CUSTOMER' },
    { prefix: '/system/rdms/role/user/', module: 'WORKER' },
    { prefix: '/system/rdms/role/', module: 'ROLE' },
    { prefix: '/system/rdms/resource/', module: 'RESOURCE' },
    { prefix: '/system/auth2/api/', module: 'AUTH2' },
    { prefix: '/system/auth2/', module: 'WORKER' },
    { prefix: '/system/tenant-ui-policy', module: 'WORKER' },
    { prefix: '/system/app-info/', module: 'MEMBER' },
    { prefix: '/system/auth/', module: 'SYSTEM' },
    { prefix: '/system/', module: 'SYSTEM' },
    { prefix: '/customer/pre-project/', module: 'PRE_PROJECT' },
    { prefix: '/customer/function-tree/', module: 'PRE_PROJECT' },
    { prefix: '/customer/sub-project/', module: 'SUB_PROJECT' },
    { prefix: '/customer/project-task/', module: 'PROJECT_TASK' },
    { prefix: '/customer/project-sec-level/', module: 'PROJECT' },
    { prefix: '/customer/project-manager/', module: 'DIRECTOR' },
    { prefix: '/customer/project/', module: 'PROJECT' },
    { prefix: '/customer/milestone/', module: 'MILESTONE' },
    { prefix: '/customer/gantt/', module: 'GANTT' },
    { prefix: '/customer/budget-direct-exe/', module: 'FEE' },
    { prefix: '/customer/budget-direct/', module: 'PROJECT' },
    { prefix: '/customer/budget-mng/', module: 'BUDGET' },
    { prefix: '/customer/budget/', module: 'BUDGET' },
    { prefix: '/customer/fee/', module: 'FEE' },
    { prefix: '/customer/flow/', module: 'FLOW' },
    { prefix: '/customer/job-item-process/', module: 'JOB_ITEM_PROCESS' },
    { prefix: '/customer/job-items/', module: 'JOB_ITEM' },
    { prefix: '/customer/jobitem-property/', module: 'JOB_ITEM' },
    { prefix: '/customer/job-level/', module: 'JOB_LEVEL' },
    { prefix: '/customer/manhour-declare/', module: 'MANHOUR_DECLARE' },
    { prefix: '/customer/manhour/', module: 'MANHOUR' },
    { prefix: '/customer/cal-manhour/', module: 'MANHOUR' },
    { prefix: '/customer/worker/listAllWorkers', module: 'MEMBER' },
    { prefix: '/customer/worker/listWorkers', module: 'MEMBER' },
    { prefix: '/customer/worker/getWorkload/', module: 'MEMBER' },
    { prefix: '/customer/worker/getWorkerManhourInfo/', module: 'MEMBER' },
    { prefix: '/customer/worker/get-worker-info/', module: 'MEMBER' },
    { prefix: '/customer/notify/', module: 'MEMBER' },
    { prefix: '/customer/worker/', module: 'WORKER' },
    { prefix: '/customer/work-calendar/', module: 'WORKER' },
    { prefix: '/customer/mail/', module: 'MAIL' },
    { prefix: '/customer/ipmt/', module: 'WORKER' },
    { prefix: '/customer/performance/', module: 'PERFORMANCE' },
    { prefix: '/customer/underline/', module: 'PERFORMANCE' },
    { prefix: '/customer/department/', module: 'DEPARTMENT' },
    { prefix: '/customer/boss/', module: 'BOSS' },
    { prefix: '/customer/pdgm-sysgm/', module: 'DIRECTOR' },
    { prefix: '/customer/system-manager/', module: 'DIRECTOR' },
    { prefix: '/customer/product-manager/', module: 'PRODUCT_MANAGER' },
    { prefix: '/customer/product/', module: 'PRODUCT' },
    { prefix: '/customer/system/', module: 'PRODUCT' },
    { prefix: '/customer/character-process/', module: 'CHARACTER_PROCESS' },
    { prefix: '/customer/character/', module: 'CHARACTER' },
    { prefix: '/customer/category-project/', module: 'CATEGORY' },
    { prefix: '/customer/category-file/', module: 'CATEGORY' },
    { prefix: '/customer/category/', module: 'CATEGORY' },
    { prefix: '/customer/material/', module: 'MATERIAL' },
    { prefix: '/customer/cbb/', module: 'CBB' },
    { prefix: '/customer/quality/', module: 'QUALITY' },
    { prefix: '/customer/bugfeedback/', module: 'BUG_FEEDBACK' },
    { prefix: '/customer/demand/', module: 'DEMAND' },
    { prefix: '/customer/course/', module: 'COURSE' },
    { prefix: '/customer/chapter/', module: 'CHAPTER' },
    { prefix: '/customer/section/', module: 'SECTION' },
    { prefix: '/customer/summary/', module: 'SUMMARY' },
    { prefix: '/customer/statistics/', module: 'SUMMARY' },
    { prefix: '/customer/api/gitlab/', module: 'GITLAB' },
    { prefix: '/customer/web/', module: 'WEB' },
    { prefix: '/customer/material-stock/', module: 'INVENTORY' },
    { prefix: '/customer/material-warehouse/', module: 'WAREHOUSE' },
    { prefix: '/customer/file-apply/', module: 'FILE' },
    { prefix: '/file/', module: 'FILE' },
    { prefix: '/ftp/', module: 'FILE' }
  ],

  /** 与后端 module-path.yml 的 whitelist 对齐：路径与条目相同才免预检 */
  API_WHITELIST_EXACT: [
    '/system/rdms/user/reset-password',
    '/system/rdms/sms/send',
    '/system/auth2/getToken',
    '/system/auth/login',
    '/system/auth/refresh',
    '/system/auth/logout',
    '/system/auth/jwks',
    '/system/auth/public-key',
    '/system/auth/tenant/switch',
    '/system/auth/password',
    '/system/auth/password/initial'
  ],

  /** 与后端 module-path.yml 的 whitelist-prefixes 对齐：路径以条目开头才免预检 */
  API_WHITELIST_PREFIXES: [
    '/system/rdms/user/login-salt/',
    '/system/rdms/user/list-customer/',
    '/system/rdms/customer/get-customer-by-loginname/',
    '/system/rdms/kaptcha/',
    '/system/vod/',
    '/file/wopi/'
  ],

  matchModuleByApiPath: function(apiPath) {
    const path = this.normalizeRoutePath(apiPath);
    if (!path) {
      return null;
    }
    let best = null;
    let bestLen = -1;
    const map = this.API_PATH_MODULE_MAP;
    for (let i = 0; i < map.length; i++) {
      const pref = map[i].prefix;
      if (path.indexOf(pref) >= 0) {
        if (pref.length > bestLen) {
          bestLen = pref.length;
          best = map[i].module;
        }
      }
    }
    return best;
  },

  isApiWhitelisted: function(apiPath) {
    const path = this.normalizeRoutePath(apiPath);
    if (this.API_WHITELIST_EXACT.indexOf(path) >= 0) {
      return true;
    }
    const list = this.API_WHITELIST_PREFIXES;
    for (let i = 0; i < list.length; i++) {
      if (path.indexOf(list[i]) === 0) {
        return true;
      }
    }
    return false;
  },

  /**
   * 从 method + url 推断操作类型，规则与网关 GatewayModulePathMatcher.inferOperation 相同。
   */
  inferOperation: function(method, apiPath) {
    const m = (method || 'get').toUpperCase();
    const p = (apiPath || '').toLowerCase();
    const has = function () {
      for (let i = 0; i < arguments.length; i++) {
        if (p.indexOf(arguments[i]) >= 0) {
          return true;
        }
      }
      return false;
    };
    if (m === 'GET') {
      if (has('/export')) return 'EXPORT';
      if (has('/download')) return 'DOWNLOAD';
      return 'VIEW';
    }
    if (m === 'POST') {
      // 签名下载只要求 FILE:VIEW，是否可下载由服务侧判定
      if (has('signed-url')) return 'VIEW';
      if (has('/download')) return 'DOWNLOAD';
      if (has('/save', '/create', '/add',
        '/setup-application', '/direct-setup', '/module-setup', '/withdraw')) return 'CREATE';
      if (has('/approve', '/review', '/audit',
        '/setup-module-project', '/reject-application', '/define/reject', '/terminate/reject') ||
        /\/setup$/.test(p) || has('/setup/')) return 'APPROVE';
      if (has('/export')) return 'EXPORT';
      if (has('/import')) return 'IMPORT';
      if (has('/update', '/edit', '/modify')) return 'UPDATE';
      if (has('/delete', '/del', '/remove')) return 'DELETE';
      if (has('/list', '/search', '/query', '/get', '/count', '/validate', '/diagram', '/history', '/capability', '/where-used')) return 'VIEW';
      return 'UPDATE';
    }
    if (m === 'PUT' || m === 'PATCH') return 'UPDATE';
    if (m === 'DELETE') return 'DELETE';
    return 'VIEW';
  },

  /**
   * 前端 Scope 预检：无对应模块权限则不应发起业务请求（网关仍是最终裁决）
   * @returns {{ allowed: boolean, module?: string, operation?: string, message?: string }}
   */
  canCallApi: function(url, method) {
    if (!url) {
      return { allowed: true };
    }
    const path = this.normalizeRoutePath(url);
    if (this.isApiWhitelisted(path)) {
      return { allowed: true };
    }

    const loginUser = Tool.getLoginUser();
    if (Tool.isEmpty(loginUser)) {
      return { allowed: false, message: '未登录' };
    }
    // 平台超管
    const scopes = loginUser.scopes || [];
    if (scopes.indexOf('PLATFORM:*') >= 0) {
      return { allowed: true };
    }

    const moduleCode = this.matchModuleByApiPath(path);
    if (!moduleCode) {
      // 未映射路径：交给网关，前端不拦
      return { allowed: true };
    }
    const operation = this.inferOperation(method, path);
    if (this.hasModulePermission(moduleCode, operation)) {
      return { allowed: true, module: moduleCode, operation: operation };
    }
    return {
      allowed: false,
      module: moduleCode,
      operation: operation,
      message: '没有权限访问 ' + moduleCode + ':' + operation
    };
  },

  /**
   * 获取资源ID对应的模块代码列表
   */
  getModulesByResourceId: function(resourceId) {
    return RESOURCE_TO_MODULE_MAP[resourceId] || [];
  },

  getModuleInfo: function(moduleCode) {
    return PERMISSION_MODULES[moduleCode] || null;
  },

  getAllModules: function() {
    return PERMISSION_MODULES;
  },

  getAllOperations: function() {
    return OPERATION_TYPES;
  }
};

/**
 * 初始化函数：将PermissionUtil的方法添加到Tool对象中
 */
function initPermissionUtil() {
  if (typeof Tool !== 'undefined') {
    Tool.hasModulePermission = PermissionUtil.hasModulePermission.bind(PermissionUtil);
    Tool.hasButtonPermission = PermissionUtil.hasButtonPermission.bind(PermissionUtil);
    Tool.hasResourceEnhanced = PermissionUtil.hasResource.bind(PermissionUtil);
    Tool.hasMenuPermission = PermissionUtil.hasMenuPermission.bind(PermissionUtil);
    Tool.hasRoutePermission = PermissionUtil.hasRoutePermission.bind(PermissionUtil);
    Tool.canCallApi = PermissionUtil.canCallApi.bind(PermissionUtil);
    Tool.matchModuleByApiPath = PermissionUtil.matchModuleByApiPath.bind(PermissionUtil);
    Tool.getModulesByResourceId = PermissionUtil.getModulesByResourceId.bind(PermissionUtil);
    Tool.getModuleInfo = PermissionUtil.getModuleInfo.bind(PermissionUtil);
    Tool.getAllPermissionModules = PermissionUtil.getAllModules.bind(PermissionUtil);
    Tool.getAllOperations = PermissionUtil.getAllOperations.bind(PermissionUtil);
  }
}

// 立即执行初始化
if (typeof Tool !== 'undefined') {
  initPermissionUtil();
} else {
  // 如果Tool还未加载，延迟初始化
  if (typeof window !== 'undefined') {
    window.addEventListener('load', function() {
      if (typeof Tool !== 'undefined') {
        initPermissionUtil();
      }
    });
  }
}

// 如果在模块环境中，导出PermissionUtil
if (typeof module !== 'undefined' && module.exports) {
  module.exports = PermissionUtil;
}

