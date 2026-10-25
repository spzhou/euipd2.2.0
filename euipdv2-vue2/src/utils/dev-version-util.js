/*
 * Author: 周朔鹏
 * Date: 2026-08-02
 * Email: 391902958@qq.com
 * Website: https://www.euipd.com
 *
 * 主版本：xx.xx.xxxx（第一段 01～99）。特性编号为四位数字。
 * 锁定版本的特性编号为主版本-特性编号，如 01.02.0003-0001。
 * 供 Vue 组件 import，避免仅依赖 index.html 全局脚本导致热更新后未定义。
 */

const DEV_VERSION_PATTERN = /^(?:0[1-9]|[1-9]\d)\.\d{2}\.\d{4}$/;
const DEV_VERSION_UNPADDED = /^([1-9]\d?)\.(\d{2})\.(\d{4})$/;

export const PATTERN_DESC = 'xx.xx.xxxx（第一段 01～99，如 01.01.0001）';

export function isDevVersion(v) {
  if (v === undefined || v === null) {
    return false;
  }
  return DEV_VERSION_PATTERN.test(String(v).trim());
}

export function normalizeLegacy(raw) {
  if (raw === undefined || raw === null || raw === '') {
    return null;
  }
  const s = String(raw).trim();
  if (isDevVersion(s)) {
    return s;
  }
  const unpadded = s.match(DEV_VERSION_UNPADDED);
  if (unpadded) {
    const major = parseInt(unpadded[1], 10);
    if (major < 1 || major > 99) {
      return null;
    }
    return String(major).padStart(2, '0') + '.' + unpadded[2] + '.' + unpadded[3];
  }
  const n = Number(s);
  if (Number.isNaN(n)) {
    return null;
  }
  let major = Math.floor(Math.abs(n));
  let minor = Math.round((Math.abs(n) - major) * 100);
  if (minor >= 100) {
    major += 1;
    minor = 0;
  }
  if (major < 1 || major > 99) {
    return null;
  }
  return String(major).padStart(2, '0') + '.' + String(minor).padStart(2, '0') + '.0000';
}

/** 维护版本：第三段加一。 */
export function bumpDevVersion(v) {
  let s = String(v || '').trim();
  if (!isDevVersion(s)) {
    const normalized = normalizeLegacy(s);
    if (!normalized) {
      return null;
    }
    s = normalized;
  }
  const parts = s.split('.');
  const patch = parseInt(parts[2], 10) + 1;
  if (patch > 9999) {
    return null;
  }
  return parts[0].padStart(2, '0') + '.' + parts[1] + '.' + String(patch).padStart(4, '0');
}

function partsOf(v) {
  let s = String(v || '').trim();
  if (!isDevVersion(s)) {
    s = normalizeLegacy(s);
    if (!s) {
      return null;
    }
  }
  return s.split('.').map(p => parseInt(p, 10));
}

/** 大版本：第一段加一，其后归零。 */
export function bumpMajor(v) {
  const p = partsOf(v);
  if (!p || p[0] + 1 > 99) {
    return null;
  }
  return String(p[0] + 1).padStart(2, '0') + '.00.0000';
}

/** 功能版本：第二段加一，第三段归零。 */
export function bumpMinor(v) {
  const p = partsOf(v);
  if (!p || p[1] + 1 > 99) {
    return null;
  }
  return String(p[0]).padStart(2, '0') + '.' + String(p[1] + 1).padStart(2, '0') + '.0000';
}

export function featureNo(version) {
  if (version === undefined || version === null || version === '') {
    return null;
  }
  const n = Number(version);
  if (!Number.isFinite(n) || n < 0 || n > 9999) {
    return null;
  }
  return String(Math.floor(n)).padStart(4, '0');
}

export function lockedCharacterNo(mainVersion, featureVersion) {
  const main = normalizeLegacy(mainVersion);
  const feature = featureNo(featureVersion);
  if (!main || !feature) {
    return null;
  }
  return main + '-' + feature;
}

export function requireValid(v, text, swal) {
  const label = text || '开发版本';
  if (!isDevVersion(v)) {
    if (typeof Toast !== 'undefined' && Toast.warning) {
      Toast.warning(label + '格式须为 ' + PATTERN_DESC, swal);
    }
    return false;
  }
  return true;
}

const DevVersionUtil = {
  PATTERN_DESC,
  isDevVersion,
  normalizeLegacy,
  bumpDevVersion,
  bumpMajor,
  bumpMinor,
  featureNo,
  lockedCharacterNo,
  requireValid,
};

export default DevVersionUtil;
