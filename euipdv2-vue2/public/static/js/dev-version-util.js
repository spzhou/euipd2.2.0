/*
 * Author: 周朔鹏
 * Date: 2026-08-02
 * Email: 391902958@qq.com
 * Website: https://www.euipd.com
 *
 * 主版本：xx.xx.xxxx（第一段 01～99）。特性编号为四位数字。
 * 锁定版本的特性编号为主版本-特性编号，如 01.02.0003-0001。
 */

var DEV_VERSION_PATTERN = /^(?:0[1-9]|[1-9]\d)\.\d{2}\.\d{4}$/;
var DEV_VERSION_UNPADDED = /^([1-9]\d?)\.(\d{2})\.(\d{4})$/;

var DevVersionUtil = {
  PATTERN_DESC: 'xx.xx.xxxx（第一段 01～99，如 01.01.0001）',

  isDevVersion: function (v) {
    if (v === undefined || v === null) {
      return false;
    }
    return DEV_VERSION_PATTERN.test(String(v).trim());
  },

  /**
   * 规范为 xx.xx.xxxx。已是该格式则原样返回；第一段未补零时补成两位。
   */
  normalizeLegacy: function (raw) {
    if (raw === undefined || raw === null || raw === '') {
      return null;
    }
    var s = String(raw).trim();
    if (DevVersionUtil.isDevVersion(s)) {
      return s;
    }
    var unpadded = s.match(DEV_VERSION_UNPADDED);
    if (unpadded) {
      var unpaddedMajor = parseInt(unpadded[1], 10);
      if (unpaddedMajor < 1 || unpaddedMajor > 99) {
        return null;
      }
      return String(unpaddedMajor).padStart(2, '0') + '.' + unpadded[2] + '.' + unpadded[3];
    }
    var n = Number(s);
    if (Number.isNaN(n)) {
      return null;
    }
    var major = Math.floor(Math.abs(n));
    var minor = Math.round((Math.abs(n) - major) * 100);
    if (minor >= 100) {
      major += 1;
      minor = 0;
    }
    if (major < 1 || major > 99) {
      return null;
    }
    return (
      String(major).padStart(2, '0') +
      '.' +
      String(minor).padStart(2, '0') +
      '.0000'
    );
  },

  /**
   * 第三段（patch）+1；非法输入返回 null。
   */
  bumpDevVersion: function (v) {
    var s = String(v || '').trim();
    if (!DevVersionUtil.isDevVersion(s)) {
      var normalized = DevVersionUtil.normalizeLegacy(s);
      if (!normalized) {
        return null;
      }
      s = normalized;
    }
    var parts = s.split('.');
    var patch = parseInt(parts[2], 10) + 1;
    if (patch > 9999) {
      return null;
    }
    return parts[0].padStart(2, '0') + '.' + parts[1] + '.' + String(patch).padStart(4, '0');
  },

  requireValid: function (v, text, swal) {
    var label = text || '开发版本';
    if (!DevVersionUtil.isDevVersion(v)) {
      Toast.warning(label + '格式须为 ' + DevVersionUtil.PATTERN_DESC, swal);
      return false;
    }
    return true;
  }
};
