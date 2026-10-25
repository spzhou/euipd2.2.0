/*
 * Author: 周朔鹏
 * Date: 2025-11-27
 * Email: 391902958@qq.com
 * Website: https://www.euipd.com
 * 
 * V3.0 权限混入（Mixin）- 用于Vue组件中快速使用权限检查
 */

export default {
  methods: {
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
     * 检查用户是否有资源权限（V2.x兼容方式，已增强支持模块权限）
     * @param {string} resourceId - 资源ID
     * @returns {boolean} 是否有权限
     */
    hasResource(resourceId) {
      if (typeof Tool !== 'undefined' && Tool.hasResource) {
        return Tool.hasResource(resourceId);
      }
      return false;
    },

    /**
     * 按钮显隐：未在 JSON 登记默认 true；已登记看 buttonPolicy.allowedIds
     * @param {string} buttonId - 如 '520101'
     */
    hasButtonPermission(buttonId) {
      if (typeof Tool !== 'undefined' && Tool.hasButtonPermission) {
        return Tool.hasButtonPermission(buttonId);
      }
      return true;
    },
    
    /**
     * 检查菜单项是否应该显示（V3.0增强）
     * 优先使用模块权限，如果没有模块权限配置则回退到资源权限
     * @param {string} resourceId - 资源ID（用于兼容旧权限）
     * @param {string} moduleCode - 模块代码（V3.0新方式）
     * @param {string} operation - 操作类型，默认为'VIEW'
     * @returns {boolean} 是否显示菜单
     */
    hasMenuPermission(resourceId, moduleCode, operation) {
      if (typeof Tool !== 'undefined' && Tool.hasMenuPermission) {
        return Tool.hasMenuPermission(resourceId, moduleCode, operation || 'VIEW');
      }
      return this.hasResource(resourceId);
    },
    
    /**
     * 检查路由权限
     * @param {string} routePath - 路由路径
     * @returns {boolean} 是否有权限
     */
    hasRoutePermission(routePath) {
      if (typeof Tool !== 'undefined' && Tool.hasRoutePermission) {
        return Tool.hasRoutePermission(routePath);
      }
      return false;
    },

    /**
     * 写操作按钮是否可点（模块操作级）
     * @param {string} module - 如 PROJECT
     * @param {string} operation - CREATE / UPDATE / DELETE / APPROVE
     */
    canDo(module, operation) {
      return this.hasModulePermission(module, operation || 'VIEW');
    },

    /**
     * 发起请求前自检（一般由 axios 拦截器自动完成；业务可显式调用）
     */
    canCallApi(url, method) {
      if (typeof Tool !== 'undefined' && Tool.canCallApi) {
        return Tool.canCallApi(url, method);
      }
      return { allowed: true };
    },

    /** 是否具备任一角色，如 hasAnyRole(['PDM','PDGM']) */
    hasAnyRole(roleCodes) {
      if (typeof RoleAccess !== 'undefined') {
        return RoleAccess.hasAny(roleCodes);
      }
      return false;
    },

    /** 侧栏菜单显隐（规则集中在 RoleAccess.SIDEBAR_MENUS） */
    canShowSidebar(menuKey, opts) {
      if (typeof RoleAccess !== 'undefined') {
        return RoleAccess.canShowSidebar(menuKey, opts || {});
      }
      return false;
    },

    /** 页内 Tab：仅角色门禁 */
    canAccessByRoles(allowedRoles) {
      if (typeof RoleAccess !== 'undefined') {
        return RoleAccess.canAccessByRoles(allowedRoles);
      }
      return false;
    }
  }
};

