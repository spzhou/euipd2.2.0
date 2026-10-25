/**
 * 口令提交协议：前端只提交 md5(明文 + 盐)，盐按手机号从 login-salt 读取。
 */
import axios from 'axios';

export function fetchLoginSalt(loginName) {
  const phone = String(loginName || '').trim();
  return axios.get(process.env.VUE_APP_SERVER + '/system/rdms/user/login-salt/' + encodeURIComponent(phone))
    .then(function (response) {
      const resp = response.data || {};
      if (resp.success && resp.content) {
        return resp.content;
      }
      return Promise.reject(resp.message || '读取口令参数失败，请稍后再试');
    });
}

export function digestPassword(plain, salt) {
  return hex_md5(String(plain) + salt);
}

/**
 * 新口令规则：8 至 32 位，至少含字母与数字。服务端只收到摘要，无法复核长度与组成。
 */
export function passwordPolicyError(plain) {
  const value = String(plain || '');
  if (value.length < 8 || value.length > 32) {
    return '新密码长度须为 8 至 32 位';
  }
  if (!/[A-Za-z]/.test(value) || !/\d/.test(value)) {
    return '新密码须同时包含字母与数字';
  }
  return null;
}
