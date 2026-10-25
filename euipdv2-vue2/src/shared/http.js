/**
 * 对已挂载拦截器的 axios 实例做薄封装。不改 Bearer、刷新和 401 跳转。
 */
import axios from 'axios';

const inflight = new Map();
const readCache = new Map();
const COALESCE_MS = 300;

const CACHE_HINTS = ['customer-user', '/dict', 'department', 'category-project', '/org'];

function storageKey(path, body) {
  return path + '|' + JSON.stringify(body || {});
}

function absolute(path) {
  if (!path) {
    return path;
  }
  if (path.indexOf('http') === 0) {
    return path;
  }
  const prefix = process.env.VUE_APP_SERVER || '';
  return prefix + path;
}

function unwrap(response) {
  const resp = response.data || {};
  if (!resp.success) {
    const error = new Error(resp.message || '请求失败');
    error.response = resp;
    throw error;
  }
  return resp.content;
}

function cacheable(path) {
  return CACHE_HINTS.some(function (hint) {
    return path.indexOf(hint) >= 0;
  });
}

export function post(path, body) {
  const key = storageKey(path, body);
  const now = Date.now();
  const hit = inflight.get(key);
  if (hit && now - hit.at < COALESCE_MS) {
    return hit.promise;
  }
  const promise = axios.post(absolute(path), body).then(unwrap);
  inflight.set(key, { at: now, promise: promise });
  setTimeout(function () {
    const current = inflight.get(key);
    if (current && current.promise === promise) {
      inflight.delete(key);
    }
  }, COALESCE_MS);
  return promise;
}

/**
 * 人员、机构、字典类读取。会话内缓存，保存成功后按域清除，不清 sessionStorage。
 */
export function read(path, body) {
  const key = storageKey(path, body);
  if (cacheable(path) && readCache.has(key)) {
    return Promise.resolve(readCache.get(key));
  }
  return post(path, body).then(function (content) {
    if (cacheable(path)) {
      readCache.set(key, content);
    }
    return content;
  });
}

export function invalidateAreas(areas) {
  if (!areas || !areas.length) {
    return;
  }
  readCache.clear();
}

/**
 * 退出或会话失效时调用，避免下一位登录者读到上一会话的缓存。
 */
export function clearReadCache() {
  readCache.clear();
  inflight.clear();
}
