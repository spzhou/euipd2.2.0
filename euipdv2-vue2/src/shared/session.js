/**
 * 读取登录会话。权限与用户对象仍来自 index.html 引入的 Tool，不在打包里再实现一套。
 */

function loginRecord() {
  if (typeof Tool === 'undefined' || !Tool.getLoginUser) {
    return {};
  }
  return Tool.getLoginUser() || {};
}

function customerGroupKey() {
  if (typeof LOGIN_USER_GROUP !== 'undefined' && LOGIN_USER_GROUP.CUSTOMER) {
    return LOGIN_USER_GROUP.CUSTOMER.key;
  }
  return 'CUSTOMER';
}

export function currentSession() {
  const login = loginRecord();
  if (login.userGroup === customerGroupKey()) {
    const operator = login.adminUser || {};
    return {
      login: login,
      operator: operator,
      customer: login,
      customerId: login.id,
      operatorId: operator.customerUserId
    };
  }
  const customer = login.customer || {};
  return {
    login: login,
    operator: login,
    customer: customer,
    customerId: customer.id,
    operatorId: login.customerUserId
  };
}
