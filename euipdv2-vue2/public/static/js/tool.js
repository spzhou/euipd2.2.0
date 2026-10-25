/*
 * Author: 周朔鹏
 * Date: 2025-02-19
 * Email: 391902958@qq.com
 * Website: https://www.euipd.com
 */


Tool = {
  /**
   * 空校验 null或""都返回true
   */
  isEmpty: function (obj) {
    if ((typeof obj == 'string')) {
      return !obj || obj.replace(/\s+/g, "") == ""
    } else {
      if(obj !== null && obj !== undefined && (typeof Number(obj) == 'number')){
        return false;
      }else {
        return (!obj || JSON.stringify(obj) === "{}" || obj.length === 0);
      }
    }
  },

  /**
   * 非空校验
   */
  isNotEmpty: function (obj) {
    return !this.isEmpty(obj);
  },

  isNotEmpty_ES5: function (obj) {
    if(obj === null || obj === undefined || JSON.stringify(obj) === '{}'){
      return false;
    }else{
      return true;
    }
  },

  isEmpty_ES5: function (obj) {
      return Object.keys(obj).length === 0;
  },

  /**
   * 长度校验
   */
  isLength: function (str, min, max) {
    let tempStr = str.toString().trim();
    return tempStr.length >= min && tempStr.length <= max;
  },

  /**
   * 富文本纯文本字数：去除标签，空白不计，图片不计。DOMParser 解析不执行脚本、不加载图片。
   */
  richTextLength: function (html) {
    if (html === null || html === undefined || html === '') {
      return 0;
    }
    let doc = new DOMParser().parseFromString(String(html), 'text/html');
    doc.querySelectorAll('script, style, template').forEach(function (n) { n.remove(); });
    let text = (doc.body && doc.body.textContent) || '';
    return Array.from(text.replace(/[\s\u00a0\u3000]/g, '')).length;
  },

  /**
   * 10进制转62进制
   * @param number
   * @returns {string}
   * @private
   */
  _10to62: function (number) {
    let chars = '0123456789abcdefghigklmnopqrstuvwxyzABCDEFGHIGKLMNOPQRSTUVWXYZ';
    let radix = chars.length;
    let arr = [];
    do {
      let mod = number % radix;
      number = (number - mod) / radix;
      arr.unshift(chars[mod]);
    } while (number);
    return arr.join('');
  },

  /**
   * 保存登录用户信息
   */
  setLoginUser: function (loginUser) {
    if (!loginUser) {
      SessionStorage.remove(SESSION_KEY_LOGIN_USER);
      return;
    }
    SessionStorage.set(SESSION_KEY_LOGIN_USER, loginUser);
  },

  /**
   * 获取登录用户信息
   */
  getLoginUser: function () {
    return SessionStorage.get(SESSION_KEY_LOGIN_USER) || {};
  },

  /**
   * 业务操作人（含 customerUserId）
   * 机构客户登录：优先 session.adminUser；缺失时用 customerUserId 兜底
   * 员工登录：返回 session 自身
   */
  getActorUser: function () {
    const sessionUser = this.getLoginUser() || {};
    const isCustomer = sessionUser.userGroup === 'CUSTOMER'
      || (typeof LOGIN_USER_GROUP !== 'undefined'
        && sessionUser.userGroup === LOGIN_USER_GROUP.CUSTOMER.key);
    if (isCustomer) {
      if (sessionUser.adminUser && sessionUser.adminUser.customerUserId) {
        return sessionUser.adminUser;
      }
      return {
        customerUserId: sessionUser.customerUserId || null,
        trueName: sessionUser.adminName || sessionUser.name || sessionUser.loginName || '管理员',
        loginName: sessionUser.loginName,
        profile: sessionUser.profile
      };
    }
    return sessionUser;
  },

  /**
   * 当前登录所属机构
   * 机构客户登录：session 自身即为机构
   * 员工登录：session.customer
   */
  getLoginCustomer: function () {
    const sessionUser = this.getLoginUser() || {};
    const isCustomer = sessionUser.userGroup === 'CUSTOMER'
      || (typeof LOGIN_USER_GROUP !== 'undefined'
        && sessionUser.userGroup === LOGIN_USER_GROUP.CUSTOMER.key);
    if (isCustomer) {
      return sessionUser;
    }
    return sessionUser.customer || {};
  },

  /**
   * 随机生成[len]长度的[radix]进制数
   * @param len
   * @param radix 默认62
   * @returns {string}
   */
  uuid: function (len, radix) {
    let chars = '0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz'.split('');
    let uuid = [];
    radix = radix || chars.length;

    for (let i = 0; i < len; i++) {
      uuid[i] = chars[0 | Math.random() * radix];
    }

    return uuid.join('');
  },

  /**
   * 查找是否有权限（V2.x兼容方式，已增强支持模块权限）
   * @param id 资源id
   */
  hasResource: function (id) {
    let _this = this;
    
    // V3.0优先：如果有PermissionUtil，优先使用增强版的资源权限检查
    if (typeof PermissionUtil !== 'undefined' && PermissionUtil.hasResource) {
      return PermissionUtil.hasResource(id);
    }
    
    // V2.x兼容：传统的资源权限检查
    let resources = _this.getLoginUser().resources;
    if (_this.isEmpty(resources)) {
      return false;
    }
    for (let i = 0; i < resources.length; i++) {
      if (id === resources[i].id) {
        return true;
      }
    }
    return false;
  },
  
  /**
   * 检查用户是否有模块操作权限（V3.0新增）
   * @param {string} module - 模块代码（如 'PROJECT'）
   * @param {string} operation - 操作类型（如 'CREATE'），默认为'VIEW'
   * @returns {boolean} 是否有权限
   */
  hasModulePermission: function(module, operation) {
    if (typeof PermissionUtil !== 'undefined' && PermissionUtil.hasModulePermission) {
      return PermissionUtil.hasModulePermission(module, operation);
    }
    return false;
  },

  /**
   * 菜单显隐：scopes/模块优先，资源 ID 严格匹配兜底
   */
  hasMenuPermission: function(resourceId, moduleCode, operation) {
    if (typeof PermissionUtil !== 'undefined' && PermissionUtil.hasMenuPermission) {
      return PermissionUtil.hasMenuPermission(resourceId, moduleCode, operation);
    }
    return this.hasResource(resourceId);
  },

  /**
   * 前端 Scope 预检（一般由 axios 拦截器自动调用）
   */
  canCallApi: function(url, method) {
    if (typeof PermissionUtil !== 'undefined' && PermissionUtil.canCallApi) {
      return PermissionUtil.canCallApi(url, method);
    }
    return { allowed: true };
  },
  
  /**
   * 检查用户是否有路由权限（V3.0新增）
   * @param {string} routePath - 路由路径
   * @returns {boolean} 是否有权限
   */
  hasRoutePermission: function(routePath) {
    if (typeof PermissionUtil !== 'undefined' && PermissionUtil.hasRoutePermission) {
      return PermissionUtil.hasRoutePermission(routePath);
    }
    return false;
  },

  /**
   * 打开 oss-get-signed-url 返回的签名地址。
   * 业务失败（HTTP 200 + success:false，如 FILE_DOWNLOAD_DENIED）时提示且不打开空 URL。
   * 图片、PDF 等由浏览器直接显示的文件在新标签页打开，不替换当前页。
   * @param {object} resp ResponseDto
   * @param {string} suffix 文件后缀（保留入参，打开方式不再按后缀区分）
   * @returns {boolean} 是否已打开
   */
  openOssSignedUrl: function (resp, suffix) {
    if (!resp || resp.success === false || !resp.content) {
      if (resp && resp._downloadDeniedNotified) {
        return false;
      }
      var msg = (resp && resp.message) || '没有文件下载权限';
      try {
        if (typeof Vue !== 'undefined' && Vue.prototype && Vue.prototype.$message) {
          Vue.prototype.$message({ type: 'warning', message: msg, showClose: true, duration: 2500 });
        } else if (typeof Swal !== 'undefined' && Swal.fire) {
          Swal.fire({ icon: 'warning', title: msg, timer: 2500, showConfirmButton: false });
        } else {
          console.warn(msg);
        }
      } catch (e) {
        console.warn(msg);
      }
      if (resp) {
        resp._downloadDeniedNotified = true;
      }
      return false;
    }
    window.open(resp.content, '_blank');
    return true;
  }
};
