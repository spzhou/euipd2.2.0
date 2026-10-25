/*
 * Author: 周朔鹏
 * Date: 2026-08-01
 * Email: 391902958@qq.com
 * Website: https://www.euipd.com
 *
 * 角色可见性标准化：
 * - SUPER：平台超管（admin-v2 超级用户菜单）
 * - UltiMng：机构全权（跳过 roles 列表门禁，仍受 hasMenuPermission / JSON）
 * - ADMIN：专用模板（团队管理、假日设置、绩效/盘点、绩效管理与通用菜单），不代办业务
 * - 其它角色：侧栏以 hasMenuPermission（tenant-ui-permission.json）为准
 * SIDEBAR_MENUS.roles 保留作文档/同步对照，不再硬拦截（避免与 JSON 双源打架）
 */

RoleAccess = {
  ALL_ROLE_CODES: [
    'STAFF', 'SUPER', 'ADMIN', 'IPMT', 'UltiMng', 'PDM', 'TM', 'PJM',
    'SM', 'SYSGM', 'PDGM', 'QCGM', 'FGGM', 'TGM', 'MATM'
  ],

  /** 机构负责人：跳过 SIDEBAR roles 列表门禁 */
  TENANT_FULL_ACCESS_ROLES: ['UltiMng'],

  /**
   * 现网侧栏菜单规则（key 供模板 canShowSidebar 使用）
   * roles: 文档对照（与后端 JSON 角色模板对齐），侧栏硬门禁已改为 hasMenuPermission
   * superOnly 仍生效
   */
  SIDEBAR_MENUS: {
    // 团队管理、假日设置：机构 UltiMng / ADMIN；权限配置、流程配置：只 UltiMng（均非 SUPER）
    worker:       { id: '5212', module: 'WORKER',      title: '团队管理',   page: 'customer/worker',       roles: ['UltiMng', 'ADMIN'] },
    tenantPolicy: { id: '5226', module: 'WORKER',      title: '权限配置',   page: 'system/tenant-ui-policy', roles: ['UltiMng'] },
    workCalendar: { id: '5228', module: 'WORKER',      title: '假日设置',   page: 'customer/work-calendar', roles: ['UltiMng', 'ADMIN'] },
    mail:         { id: '5301', module: 'MAIL',        title: '我的邮件',   page: 'customer/mail',         roles: null },
    flow:         { id: '5227', module: 'FLOW',        title: '流程配置',   page: 'customer/flow',         roles: ['UltiMng'] },
    budget:       { id: '5220', module: 'BUDGET',      title: '预算管理',   page: 'customer/budget',       roles: ['UltiMng', 'IPMT'] },
    summary:      { id: '5200', module: 'SUMMARY',     title: '绩效/盘点', page: 'customer/summary',      roles: ['UltiMng', 'IPMT', 'ADMIN', 'PDM', 'PJM', 'PDGM', 'SYSGM', 'TM', 'TGM', 'QCGM', 'FGGM'] },
    module:       { id: '5205', module: 'CBB',         title: '特性仓库',   page: 'customer/module',       roles: null },
    demand:       { id: '5204', module: 'DEMAND',      title: '需求识别',   page: 'customer/demand',       roles: null },
    character:    { id: '5207', module: 'PRE_PROJECT', title: '预立项管理', page: 'customer/character',    roles: ['PDM', 'PDGM', 'PJM'] },
    function:     { id: '5221', module: 'JOB_ITEM',    title: '系统工程',   page: 'customer/function',     roles: ['SYSGM', 'SM'] },
    setup:        { id: '5201', module: 'PROJECT',     title: '立项管理',   page: 'customer/setup',        roles: ['UltiMng', 'IPMT', 'PDM', 'PDGM'] },
    project:      { id: '5209', module: 'PROJECT',     title: '项目管理',   page: 'customer/project',      roles: ['PJM'] },
    develop:      { id: '5206', module: 'JOB_ITEM',    title: '研发管理',   page: 'customer/develop',      roles: ['PJM'] },
    test:         { id: '5208', module: 'QUALITY',     title: '测试管理',   page: 'customer/test',         roles: ['TM', 'TGM'] },
    manhour:      { id: '5202', module: 'MANHOUR',     title: '执行管理',   page: 'customer/manhour',      roles: null },
    material:     { id: '5210', module: 'MATERIAL',    title: '物料管理',   page: 'customer/material',     roles: ['PJM', 'MATM'] },
    jobitem:      { id: '5217', module: 'JOB_ITEM',    title: '工期管理',   page: 'customer/jobitem',      roles: null },
    review:       { id: '5215', module: 'JOB_ITEM',    title: '评审管理',   page: 'customer/review',       roles: ['IPMT', 'PDM', 'PDGM', 'PJM'] },
    product:      { id: '5218', module: 'PRODUCT',     title: '产品管理',   page: 'customer/product',      roles: ['PDM', 'PDGM'] },
    performance:  { id: '5216', module: 'PERFORMANCE', title: '绩效管理',   page: 'customer/performance',  roles: ['IPMT', 'ADMIN', 'UltiMng'] },
    quality:      { id: '5213', module: 'QUALITY',     title: '质量管理',   page: 'customer/quality',      roles: ['PDM', 'PDGM', 'QCGM'] },
    qualitydefine:{ id: '5211', module: 'QUALITY',     title: '质量反馈',   page: 'customer/qualitydefine',roles: null },
    file:         { id: '5203', module: 'FILE',        title: '档案管理',   page: 'file/file',             roles: null },
    download:     { id: '5219', module: 'FILE',        title: '文档下载',   page: 'customer/download',     roles: null },
    warehouse:    { id: '5223', module: 'WAREHOUSE',   title: '仓库管理',   page: 'customer/warehouse',    roles: ['UltiMng', 'MATM'] },
    inventory:    { id: '5225', module: 'INVENTORY',   title: '库存管理',   page: 'customer/inventory',    roles: ['UltiMng', 'MATM'] }
  },

  getRoleList: function (roleList) {
    if (roleList && roleList.length) {
      return roleList;
    }
    if (typeof Tool !== 'undefined' && Tool.getLoginUser) {
      const u = Tool.getLoginUser();
      if (u && u.roleList) {
        return u.roleList;
      }
    }
    return [];
  },

  /** 是否机构全权角色（UltiMng） */
  isTenantFullAccess: function (roleList) {
    return this.hasAny(this.TENANT_FULL_ACCESS_ROLES, roleList);
  },

  /** 是否拥有任一角色 */
  hasAny: function (roleCodes, roleList) {
    if (!roleCodes || !roleCodes.length) {
      return true;
    }
    const set = {};
    const list = this.getRoleList(roleList);
    for (let i = 0; i < list.length; i++) {
      if (list[i]) {
        set[String(list[i])] = true;
      }
    }
    for (let j = 0; j < roleCodes.length; j++) {
      if (set[roleCodes[j]]) {
        return true;
      }
    }
    return false;
  },

  /** 是否拥有全部角色 */
  hasAll: function (roleCodes, roleList) {
    if (!roleCodes || !roleCodes.length) {
      return true;
    }
    const set = {};
    const list = this.getRoleList(roleList);
    for (let i = 0; i < list.length; i++) {
      if (list[i]) {
        set[String(list[i])] = true;
      }
    }
    for (let j = 0; j < roleCodes.length; j++) {
      if (!set[roleCodes[j]]) {
        return false;
      }
    }
    return true;
  },

  hasRole: function (roleCode, roleList) {
    return this.hasAny([roleCode], roleList);
  },

  /**
   * 解析为 flags：{ PDM: true, role_PDM: true, ... }
   */
  parseFlags: function (roleList) {
    const list = this.getRoleList(roleList);
    const set = {};
    for (let i = 0; i < list.length; i++) {
      if (list[i]) {
        set[String(list[i])] = true;
      }
    }
    const flags = {};
    for (let k = 0; k < this.ALL_ROLE_CODES.length; k++) {
      const code = this.ALL_ROLE_CODES[k];
      const on = !!set[code];
      flags[code] = on;
      flags['role_' + code] = on;
    }
    return flags;
  },

  /**
   * 将角色写入目标对象
   * @param {object} target
   * @param {object} mapping 如 { isBoss: 'UltiMng', isPDM: 'PDM', role_ADMIN: 'ADMIN' }
   * @param {string[]} [roleList]
   */
  applyFlags: function (target, mapping, roleList) {
    if (!target || !mapping) {
      return target;
    }
    const flags = this.parseFlags(roleList);
    Object.keys(mapping).forEach(function (prop) {
      const code = mapping[prop];
      target[prop] = !!flags[code];
    });
    return target;
  },

  /**
   * 将 parseFlags 的 role_* 写入 target（覆盖 userRoles）
   */
  applyRoleStarFlags: function (target, roleList) {
    const flags = this.parseFlags(roleList);
    for (let k = 0; k < this.ALL_ROLE_CODES.length; k++) {
      const prop = 'role_' + this.ALL_ROLE_CODES[k];
      if (prop in target) {
        target[prop] = flags[prop];
      }
    }
    return target;
  },

  /**
   * 页内 Tab / 区块：仅角色门禁
   * @param {string[]} allowedRoles 为空表示不限制角色
   */
  canAccessByRoles: function (allowedRoles, roleList) {
    if (!allowedRoles || !allowedRoles.length) {
      return true;
    }
    return this.hasAny(allowedRoles, roleList);
  },

  /**
   * 侧栏单项是否显示：
   * - 平台 SUPER / superOnly → 不走租户侧栏
   * - 权威：hasMenuPermission（后端 JSON 下发的 resources/scopes）
   * - UltiMng：不因 SIDEBAR roles 列表被挡（机构全权）
   * - ADMIN 及其它：不再使用 ADMIN||UltiMng 旁路；完全由 JSON → hasMenuPermission
   */
  canShowSidebar: function (menuKey, opts) {
    opts = opts || {};
    const def = this.SIDEBAR_MENUS[menuKey];
    if (!def) {
      return false;
    }
    // 平台超管不走租户侧栏（由 admin-v2 的 hasSuperMenu 展示）
    if (typeof PermissionUtil !== 'undefined' && PermissionUtil.isPlatformSuperUser
        && PermissionUtil.isPlatformSuperUser()) {
      return false;
    }
    // 仅超管菜单：租户侧栏永不展示
    if (def.superOnly) {
      return false;
    }
    // ADMIN/其它角色：以 JSON→hasMenuPermission 为准（无 ADMIN||UltiMng 旁路）
    // UltiMng：后端 ALL_MENUS，此处同样走 hasMenuPermission 即可看见全量租户菜单
    if (typeof Tool !== 'undefined' && Tool.hasMenuPermission) {
      return Tool.hasMenuPermission(def.id, def.module, 'VIEW');
    }
    // 无 PermissionUtil 时：UltiMng 放行；其余按文档 roles（兼容降级）
    if (this.isTenantFullAccess(opts.roleList)) {
      return true;
    }
    if (def.roles && def.roles.length) {
      return this.hasAny(def.roles, opts.roleList);
    }
    return true;
  },

  /** 导出侧栏定义（给文档/调试） */
  listSidebarMenus: function () {
    return this.SIDEBAR_MENUS;
  }
};

function initRoleAccess() {
  if (typeof Tool !== 'undefined') {
    Tool.hasAnyRole = RoleAccess.hasAny.bind(RoleAccess);
    Tool.hasRole = RoleAccess.hasRole.bind(RoleAccess);
    Tool.canShowSidebar = RoleAccess.canShowSidebar.bind(RoleAccess);
    Tool.canAccessByRoles = RoleAccess.canAccessByRoles.bind(RoleAccess);
    Tool.parseRoleFlags = RoleAccess.parseFlags.bind(RoleAccess);
    Tool.isTenantFullAccess = RoleAccess.isTenantFullAccess.bind(RoleAccess);
  }
}

if (typeof Tool !== 'undefined') {
  initRoleAccess();
} else if (typeof window !== 'undefined') {
  window.addEventListener('load', function () {
    if (typeof Tool !== 'undefined') {
      initRoleAccess();
    }
  });
}

if (typeof module !== 'undefined' && module.exports) {
  module.exports = RoleAccess;
}
