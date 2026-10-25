/*
 * Author: 周朔鹏
 * Date: 2025-02-23
 * Email: 391902958@qq.com
 * Website: https://www.euipd.com
 */

import Vue from 'vue';
import './plugins/element';
import App from './app.vue';
import router from './router';
import axios from 'axios';
import filter from './filter/filter';
import textFormat from 'vue-text-format';
import Print from '../public/static/js/print';
import Jquery from "jquery";
import { installNotify } from "./shared/notify";
import { richHtmlDirective } from "./utils/rich-html";
import { clearReadCache } from "./shared/http";
import { clearAssetCache } from "./components/rich/rich-asset-cache";

Vue.config.productionTip = false; //生产模式下,关闭提示

Vue.prototype.$ajax = axios;
installNotify(Vue);
Vue.prototype.$ = Jquery;

Vue.use(textFormat);
Vue.use(Print);

// 解决每次ajax请求，对应的sessionId不一致的问题
axios.defaults.withCredentials = true;
axios.defaults.timeout = 20000; // 20s，避免挂起请求长时间锁死感知

if (process.env.NODE_ENV === 'development') {
    Vue.config.devtools = true;
}

/**
 * axios拦截器 — Bearer + Scope 预检 + refresh（对齐租户 UI / JWT Scope）
 */
let _refreshing = null;
/** 会话失效跳转锁：避免角标并发 401 反复清会话 / 刷屏 */
let _authRedirecting = false;

function getAccessToken() {
  const u = Tool.getLoginUser() || {};
  return u.accessToken;
}

// 写入有效会话时解除跳转锁（登录 / refresh 落盘）；clear 时传 null 不解除
(function patchSetLoginUser() {
  if (typeof Tool === 'undefined' || typeof Tool.setLoginUser !== 'function') {
    return;
  }
  const original = Tool.setLoginUser.bind(Tool);
  Tool.setLoginUser = function (loginUser) {
    if (loginUser) {
      _authRedirecting = false;
    }
    return original(loginUser);
  };
})();

/** Scope 预检白名单（含 logout 等；与 module-path.yml 对齐） */
function isScopeCheckWhitelisted(url) {
  if (typeof PermissionUtil !== 'undefined' && PermissionUtil.isApiWhitelisted) {
    return PermissionUtil.isApiWhitelisted(url);
  }
  const u = String(url || '');
  return /\/system\/auth\//.test(u)
    || /\/system\/rdms\/kaptcha/.test(u);
}

/** 真正公开接口：不带 Bearer（logout / tenant/switch / password 仍需带） */
function shouldSkipAuthHeader(url) {
  const u = String(url || '');
  return /\/system\/auth\/(login|refresh|jwks|public-key|password\/initial)(?:\?|$)/.test(u)
    || /\/system\/rdms\/kaptcha\//.test(u)
    || /\/system\/rdms\/sms\/send(?:\?|$)/.test(u)
    || /\/system\/rdms\/user\/(list-customer|reset-password|login-salt)/.test(u)
    || /\/system\/rdms\/customer\/get-customer-by-loginname\//.test(u)
    || /\/system\/auth2\/getToken/.test(u);
}

function notifyNoPermission(message) {
  const msg = message || '没有权限!';
  try {
    if (Vue.prototype.$message) {
      Vue.prototype.$message({ type: 'warning', message: msg, showClose: true, duration: 2500 });
    }
  } catch (e) {
    console.warn(msg);
  }
}

/**
 * 统一清理登录会话与会话内缓存（退出、刷新失败、令牌失效共用）。
 */
function clearSession() {
  Tool.setLoginUser(null);
  clearReadCache();
  clearAssetCache();
}

function loginLocation(fullPath) {
  const target = fullPath || (router.currentRoute && router.currentRoute.fullPath);
  if (!target || target === '/' || target.indexOf('/login') === 0) {
    return { path: '/login' };
  }
  return { path: '/login', query: { redirect: target } };
}

function clearLoginAndGoLogin() {
  if (_authRedirecting) {
    return;
  }
  _authRedirecting = true;
  clearSession();
  router.push(loginLocation()).catch(() => {});
}

/**
 * 主动退出：先离开业务页，使卸载时的释放类请求仍带有效令牌发出，再吊销令牌并清理会话。
 */
let _loggingOut = false;
Vue.prototype.$logout = async function () {
  if (_loggingOut) {
    return;
  }
  _loggingOut = true;
  const u = Tool.getLoginUser() || {};
  try {
    await router.push('/login').catch(() => {});
    await new Promise(resolve => setTimeout(resolve, 300));
    if (u.accessToken || u.refreshToken) {
      await axios.post(process.env.VUE_APP_SERVER + '/system/auth/logout', {
        refreshToken: u.refreshToken
      }, {
        _retry: true,
        skipScopeCheck: true
      }).catch(e => console.warn('登出接口调用失败，继续清除本地登录状态', e && e.message));
    }
  } finally {
    clearSession();
    _loggingOut = false;
  }
};

/**
 * 返回新 accessToken；凭证被拒返回 null；网络失败抛出，由调用方保留会话。
 */
async function refreshAccessToken() {
  const u = Tool.getLoginUser() || {};
  if (!u.refreshToken) {
    return null;
  }
  let resp;
  try {
    resp = await axios.post(process.env.VUE_APP_SERVER + '/system/auth/refresh', {
      refreshToken: u.refreshToken
    }, { _skipAuth: true, skipScopeCheck: true });
  } catch (e) {
    const status = e && e.response && e.response.status;
    if (status === 400 || status === 401 || status === 403) {
      return null;
    }
    const networkError = new Error('refresh network error');
    networkError.refreshNetworkError = true;
    throw networkError;
  }
  const data = resp && resp.data;
  if (data && data.success === false && !/^TOKEN_/.test(String(data.code || ''))) {
    const transient = new Error('refresh failed: ' + (data.code || 'UNKNOWN'));
    transient.refreshNetworkError = true;
    throw transient;
  }
  if (resp && resp.data && resp.data.success && resp.data.content) {
    const c = resp.data.content;
    // refresh 响应偶发不带 adminUser/customer，禁止用 null 覆盖会话中已有值
    const next = Object.assign({}, u, c, {
      accessToken: c.accessToken,
      refreshToken: c.refreshToken || u.refreshToken,
      scopes: c.scopes || u.scopes || [],
      modulePermissions: c.modulePermissions || u.modulePermissions || {},
      resources: c.resources || u.resources || [],
      adminUser: c.adminUser || u.adminUser || null,
      customer: c.customer || u.customer || null,
      customerUserId: c.customerUserId
        || (c.adminUser && c.adminUser.customerUserId)
        || u.customerUserId
        || (u.adminUser && u.adminUser.customerUserId)
        || null,
      roleList: c.roleList || u.roleList || []
    });
    Tool.setLoginUser(next);
    return next.accessToken;
  }
  return null;
}

function rejectUnauthorizedAdapter(config) {
  return Promise.reject({
    config: config,
    response: {
      status: 401,
      statusText: 'Unauthorized',
      data: { success: false, code: 'TOKEN_INVALID', message: '登录已失效' },
      headers: {},
      config: config
    },
    isAxiosError: true,
    message: '登录已失效'
  });
}

/** 供布局角标等查询：是否正在踢回登录（避免继续扇出业务请求） */
Vue.prototype.$isAuthRedirecting = function () {
  return _authRedirecting;
};

//请求拦截
axios.interceptors.request.use(function (config) {
  // 公开接口不带 Bearer；logout / tenant/switch 仍附带
  if (config._skipAuth || shouldSkipAuthHeader(config.url)) {
    if (config.headers) {
      delete config.headers.Authorization;
      delete config.headers.token;
    }
  } else {
    // 会话失效跳转中：一律短路（含残留 token），避免角标并发继续打 401
    if (_authRedirecting) {
      return Object.assign({}, config, {
        adapter: function () {
          return rejectUnauthorizedAdapter(config);
        }
      });
    }
    let token = getAccessToken();
    if (Tool.isNotEmpty(token)) {
      // 网关与服务侧都优先读 Authorization，不再附带同值 token 头，避免请求头超限
      config.headers.Authorization = 'Bearer ' + token;
    }
  }

  // 前端 Scope 预检：未通过则不发起请求，业务侧收到 { success: false, message }
  // 可通过 config.skipScopeCheck = true 跳过
  if (!config.skipScopeCheck && !config._skipAuth && !isScopeCheckWhitelisted(config.url)
      && typeof PermissionUtil !== 'undefined' && PermissionUtil.canCallApi) {
    const check = PermissionUtil.canCallApi(config.url, config.method);
    if (check && check.allowed === false) {
      const message = check.message || '没有权限!';
      notifyNoPermission(message);
      // 用自定义 adapter 直接返回，保证 .then 能拿到 success:false
      return Object.assign({}, config, {
        adapter: function () {
          return Promise.resolve({
            data: { success: false, code: 'NO_PERMISSION', message: message },
            status: 200,
            statusText: 'OK',
            headers: {},
            config: config,
            request: {}
          });
        }
      });
    }
  }
  return config;
}, error => Promise.reject(error));

/** 令牌失效的业务码：网关与服务鉴权返回 401，白名单接口（改密码、切换机构）自行验令牌时以 HTTP 200 返回同一组码 */
const AUTH_FAILURE_CODES = ['TOKEN_INVALID', 'TOKEN_EXPIRED', 'TOKEN_MISSING'];

/** 先刷新令牌再重发；刷新被拒则清会话回登录页；刷新网络失败保留会话。并发合并为一次 refresh */
async function retryWithRefreshOrLogin(original, rejection) {
  if (!original._retry && !original._skipAuth && !String(original.url || '').includes('/auth/refresh')) {
    original._retry = true;
    try {
      if (!_refreshing) {
        _refreshing = refreshAccessToken().finally(() => { _refreshing = null; });
      }
      const newToken = await _refreshing;
      if (newToken) {
        _authRedirecting = false;
        original.headers = original.headers || {};
        original.headers.Authorization = 'Bearer ' + newToken;
        delete original.headers.token;
        return axios(original);
      }
    } catch (e) {
      if (e && e.refreshNetworkError) {
        return Promise.reject(rejection);
      }
    }
  }
  clearLoginAndGoLogin();
  return Promise.reject(rejection);
}

//返回拦截
axios.interceptors.response.use(function (response) {
  // 业务失败仍是 HTTP 200：签名下载统一标记，避免各页重复提示时丢 code
  const data = response && response.data;
  const config = (response && response.config) || {};
  const url = String(config.url || '');
  if (data && data.success === false && AUTH_FAILURE_CODES.indexOf(data.code) >= 0
      && !config._skipAuth && !shouldSkipAuthHeader(url)) {
    const rejection = { config: config, response: response, isAxiosError: true, message: data.message || '登录已失效' };
    if (_authRedirecting) {
      return Promise.reject(rejection);
    }
    return retryWithRefreshOrLogin(config, rejection);
  }
  if (data && data.success === false && /oss-get-signed-url/.test(url)) {
    if (!data._downloadDeniedNotified) {
      notifyNoPermission(data.message || '没有文件下载权限');
      data._downloadDeniedNotified = true;
    }
  }
  return response;
}, async error => {
  let response = error.response || {};
  const status = response.status;
  const original = error.config || {};
  const url = String(original.url || '');
  const body = response.data || {};

  // 已在跳登录：静默拒绝，避免 console/Toast 风暴
  if (_authRedirecting && (status === 401 || status === 412 || !status)) {
    return Promise.reject(error);
  }

  if (status === 401 || status === 412) {
    return retryWithRefreshOrLogin(original, error);
  }

  if (status === 429) {
    const message = body.message || '请求过于频繁，请稍后再试';
    notifyNoPermission(message);
    return {
      data: {
        success: false,
        code: body.code || 'REQUEST_TOO_FREQUENT',
        message: message
      }
    };
  }

  // 403：已登录但无权限，不清会话、不踢登录（对齐网关 ResponseDto JSON）
  if (status === 403) {
    const message = body.message || '没有权限!';
    notifyNoPermission(message);
    return {
      data: {
        success: false,
        code: body.code || 'NO_PERMISSION',
        message: message
      }
    };
  }
  if (status === 500) {
    if (body.message === "No message available") {
      body.message = "数据返回异常!";
    }
    return {
      data: {
        success: false,
        code: body.code || 'UNKNOWN_ERROR',
        message: body.message || "服务异常",
      }
    };
  }
  if (status && status !== 401 && status !== 412) {
    console.warn("返回拦截：", status, url);
  }
  return Promise.reject(error);
});

// 全局过滤器
Object.keys(filter).forEach(key => {
  Vue.filter(key, filter[key]) //key是filter函数名, filter[key]是函数体
});

// 全局前置路由守卫----路由登录拦截
router.beforeEach(async (to, from, next) => {
  if (!onLoginRequiredRoute(to)) {
    next();
    return;
  }
  if (await ensureSessionAlive()) {
    next();
    return;
  }
  _authRedirecting = true;
  clearSession();
  next(loginLocation(to.fullPath));
});

function accessTokenExpiresAt(token) {
  try {
    const payload = String(token || '').split('.')[1];
    if (!payload) {
      return 0;
    }
    const json = JSON.parse(decodeURIComponent(escape(atob(payload.replace(/-/g, '+').replace(/_/g, '/')))));
    return json && json.exp ? json.exp * 1000 : 0;
  } catch (e) {
    return 0;
  }
}

/**
 * 本地核对会话：无访问令牌返回 false；访问令牌未过期返回 true；已过期则刷新，刷新被拒返回 false。
 * 刷新网络失败返回 true（保留会话，下次请求再判断）。
 */
async function ensureSessionAlive() {
  const token = getAccessToken();
  if (!token) {
    return false;
  }
  if (accessTokenExpiresAt(token) > Date.now() + 5000) {
    return true;
  }
  try {
    if (!_refreshing) {
      _refreshing = refreshAccessToken().finally(() => { _refreshing = null; });
    }
    return Boolean(await _refreshing);
  } catch (e) {
    return true;
  }
}

function onLoginRequiredRoute(route) {
  return route.matched.some(item => item.meta.loginRequire);
}

/**
 * 页面回到前台与定时核对：休眠、后台或无轮询的页面不发请求时，令牌过期或刷新被拒也回登录页。
 */
async function checkSessionInBackground() {
  if (_authRedirecting || !onLoginRequiredRoute(router.currentRoute)) {
    return;
  }
  if (!(await ensureSessionAlive())) {
    clearLoginAndGoLogin();
  }
}

document.addEventListener('visibilitychange', () => {
  if (document.visibilityState === 'visible') {
    checkSessionInBackground();
  }
});
setInterval(checkSessionInBackground, 60 * 1000);

//全局后置路由守卫————初始化的时候被调用、每次路由切换之后被调用
router.afterEach((to, from) => {
  document.title = to.meta.title || 'EUIPD-产品研发管理'    //修改网页的title
})

// Vue.use(ElementUI);
// Vue.use(VueSweetalert2);

/**
 * 按钮/DOM 权限指令
 * 用法：
 *   v-permission="'520101'"                    // 按钮 ID（buttonPolicy）
 *   v-permission="'PROJECT:CREATE'"            // 模块操作
 *   v-permission="{ module: 'PROJECT', operation: 'CREATE' }"
 *   v-permission="{ resourceId: '520101' }"    // 按钮/资源 ID（走 hasButtonPermission）
 *   v-permission="{ buttonId: '520101' }"
 */
Vue.directive('permission', {
  inserted(el, binding) {
    const value = binding.value;
    let allowed = false;
    if (typeof value === 'string') {
      if (value.indexOf(':') > 0) {
        const parts = value.split(':');
        allowed = Tool.hasModulePermission && Tool.hasModulePermission(parts[0], parts[1] || 'VIEW');
      } else {
        allowed = Tool.hasButtonPermission
          ? Tool.hasButtonPermission(value)
          : true;
      }
    } else if (value && typeof value === 'object') {
      if (value.module) {
        allowed = Tool.hasModulePermission && Tool.hasModulePermission(value.module, value.operation || 'VIEW');
      } else if (value.buttonId || value.resourceId) {
        const bid = value.buttonId || value.resourceId;
        allowed = Tool.hasButtonPermission
          ? Tool.hasButtonPermission(bid)
          : (Tool.hasResource && Tool.hasResource(bid));
      }
    }
    if (!allowed && el && el.parentNode) {
      el.parentNode.removeChild(el);
    }
  }
});

// 富文本显示：替代 v-html，输出前经白名单过滤
Vue.directive('rich-html', richHtmlDirective);

// 自定义指令: 让el-select支持滚动
Vue.directive('selectLoadMore', {
  bind(el, binding) {
    const selectDom = el.querySelector('.el-select-dropdown .el-select-dropdown__wrap');
    if (!selectDom) {
      return;
    }
    selectDom.addEventListener('scroll', () => {
      const height = selectDom.scrollHeight - Math.ceil(selectDom.scrollTop) <= selectDom.clientHeight;
      if (height && selectDom.scrollTop !== 0) {
        binding.value(); // 触发加载更多数据的函数
      }
    });
  }
});

new Vue({
  router, //加载路由
  el: '#app-index',  //加载index入口页面
  render: h => h(App),   //渲染app.vue
}).$mount('#app-vue');  //通过app.vue页面的#app-vue加载DOM, 并完成与index入口页面的关联
