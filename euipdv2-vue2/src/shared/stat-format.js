/**
 * 统计数值格式化。服务端比率为 0 至 1 的小数，空值显示“-”。
 */
const EMPTY = '-';

function toNumber(value) {
  if (value === null || value === undefined || value === '') {
    return null;
  }
  const n = Number(value);
  return Number.isFinite(n) ? n : null;
}

export function percent(rate, digits) {
  const n = toNumber(rate);
  return n === null ? EMPTY : (n * 100).toFixed(digits === undefined ? 1 : digits) + '%';
}

export function hours(value, digits) {
  const n = toNumber(value);
  return n === null ? EMPTY : n.toFixed(digits === undefined ? 1 : digits);
}

export function money(value, digits) {
  const n = toNumber(value);
  if (n === null) {
    return EMPTY;
  }
  const d = digits === undefined ? 2 : digits;
  return n.toLocaleString('zh-CN', { minimumFractionDigits: d, maximumFractionDigits: d });
}

export function count(value) {
  const n = toNumber(value);
  return n === null ? EMPTY : String(Math.round(n));
}

export function score(value) {
  const n = toNumber(value);
  return n === null ? EMPTY : n.toFixed(2);
}

/** 负荷率着色：低于 60% 偏低，超过 110% 偏高 */
export function loadLevel(rate) {
  const n = toNumber(rate);
  if (n === null) {
    return '';
  }
  if (n > 1.1) {
    return 'danger';
  }
  return n < 0.6 ? 'warning' : 'success';
}

/** 机构级、部门级为管理范围，可见绩效金额与驾驶舱 */
export function isManagerial(level) {
  return level === 'ORG' || level === 'DEPARTMENT';
}

const HEALTH = {
  NORMAL: { name: '正常', type: 'success', color: '#67C23A' },
  WATCH: { name: '关注', type: 'warning', color: '#E6A23C' },
  ALERT: { name: '预警', type: 'danger', color: '#F56C6C' },
};

export const HEALTH_LEVELS = ['NORMAL', 'WATCH', 'ALERT'];

export function healthName(level) {
  return HEALTH[level] ? HEALTH[level].name : EMPTY;
}

/** Element 标签类型 */
export function healthType(level) {
  return HEALTH[level] ? HEALTH[level].type : 'info';
}

export function healthColor(level) {
  return HEALTH[level] ? HEALTH[level].color : '#909399';
}

export function date(value) {
  if (!value) {
    return EMPTY;
  }
  if (typeof value === 'string' && /^\d{4}-\d{2}-\d{2}( |$)/.test(value)) {
    return value.slice(0, 10);
  }
  const d = new Date(value);
  if (isNaN(d.getTime())) {
    return EMPTY;
  }
  const pad = n => (n < 10 ? '0' + n : '' + n);
  return d.getFullYear() + '-' + pad(d.getMonth() + 1) + '-' + pad(d.getDate());
}

export const STAT_COLORS = ['#409EFF', '#67C23A', '#E6A23C', '#F56C6C', '#909399', '#9B59B6', '#1ABC9C', '#34495E'];
