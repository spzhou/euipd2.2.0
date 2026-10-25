/**
 * 内部邮件接口与显示工具。机构与成员由后端取自令牌，请求体不传身份。
 */
import axios from 'axios';
import { post } from './http';

const BASE = '/customer/mail';

export const FOLDERS = [
  { key: 'inbox', code: 'INBOX', name: '收件箱', icon: 'fa fa-inbox' },
  { key: 'starred', code: 'STARRED', name: '星标邮件', icon: 'fa fa-star-o' },
  { key: 'draft', code: 'DRAFT', name: '草稿箱', icon: 'fa fa-file-text-o' },
  { key: 'sent', code: 'SENT', name: '已发送', icon: 'fa fa-paper-plane-o' },
  { key: 'trash', code: 'TRASH', name: '已删除', icon: 'fa fa-trash-o' }
];

export function folderByKey(key) {
  return FOLDERS.find(f => f.key === key) || FOLDERS[0];
}

function get(path) {
  return axios.get((process.env.VUE_APP_SERVER || '') + path).then(response => {
    const resp = response.data || {};
    if (!resp.success) {
      const error = new Error(resp.message || '请求失败');
      error.response = resp;
      throw error;
    }
    return resp.content;
  });
}

export const getSelf = () => get(BASE + '/account/getSelf');
export const updateSignature = signature => post(BASE + '/account/update', { signature });
export const listContacts = keyword => post(BASE + '/contact/list', { keyword: keyword || '' });
export const listBox = query => post(BASE + '/box/list', query);
export const countBox = () => get(BASE + '/box/count');
export const getMessage = boxId => get(BASE + '/message/get/' + encodeURIComponent(boxId));
export const saveDraft = body => post(BASE + '/draft/save', body);
export const sendMail = body => post(BASE + '/message/send', body);
export const markBoxes = (boxIds, action) => post(BASE + '/box/mark', { boxIds, action });
export const moveBoxes = (boxIds, action) => post(BASE + '/box/move', { boxIds, action });
export const deleteBoxes = boxIds => post(BASE + '/box/delete', { boxIds });
export const listAttachable = (source, keyword, page, size) =>
  post(BASE + '/attachable/list', { source, keyword: keyword || '', page, size });
export const listRichVersions = (keyword, page, size) =>
  post(BASE + '/attachable/rich-version/list', { keyword: keyword || '', page, size });
export const saveVersionAsFile = (versionId, format) =>
  post('/file/rdms/rich-render/save-as-file', { versionId, format });
export const attachmentUrl = (boxId, attachmentId, preview) =>
  post(BASE + '/attachment/signed-url', { boxId, attachmentId, preview: !!preview });

/** 写信时本地上传：先传对象存储，再登记 rdms_file（use=mail），返回文件记录 */
export function uploadLocalFile(file, customerId) {
  const form = new window.FormData();
  form.append('folder', '');
  form.append('file', file);
  form.append('size', file.size);
  form.append('use', 'mail');
  form.append('customer', customerId || '');
  return axios.post((process.env.VUE_APP_SERVER || '') + '/file/rdms/oss-simple-rdms', form).then(response => {
    const resp = response.data || {};
    if (!resp.success || !resp.content) {
      throw new Error(resp.message || '上传失败');
    }
    const saved = Object.assign({}, resp.content, { customerId: customerId, itemId: null });
    return post('/file/rdms/save', saved);
  });
}

// ---------- 显示 ----------

const pad = n => (n < 10 ? '0' + n : '' + n);

/** 列表时间：当天显示时分，当年显示月日，其余显示年月日 */
export function shortTime(value) {
  if (!value) return '';
  const d = new Date(value);
  if (isNaN(d.getTime())) return '';
  const now = new Date();
  if (d.toDateString() === now.toDateString()) return pad(d.getHours()) + ':' + pad(d.getMinutes());
  const y = new Date(now);
  y.setDate(now.getDate() - 1);
  if (d.toDateString() === y.toDateString()) return '昨天 ' + pad(d.getHours()) + ':' + pad(d.getMinutes());
  if (d.getFullYear() === now.getFullYear()) return (d.getMonth() + 1) + '月' + d.getDate() + '日';
  return d.getFullYear() + '/' + (d.getMonth() + 1) + '/' + d.getDate();
}

const WEEK = ['日', '一', '二', '三', '四', '五', '六'];

export function fullTime(value) {
  if (!value) return '';
  const d = new Date(value);
  if (isNaN(d.getTime())) return '';
  return d.getFullYear() + '年' + (d.getMonth() + 1) + '月' + d.getDate() + '日（星期' + WEEK[d.getDay()] + '）'
    + pad(d.getHours()) + ':' + pad(d.getMinutes());
}

export function fileSize(bytes) {
  const n = Number(bytes) || 0;
  if (n < 1024) return n + ' B';
  if (n < 1024 * 1024) return (n / 1024).toFixed(1) + ' KB';
  if (n < 1024 * 1024 * 1024) return (n / 1024 / 1024).toFixed(1) + ' MB';
  return (n / 1024 / 1024 / 1024).toFixed(2) + ' GB';
}

const SUFFIX_ICONS = [
  [['pdf'], 'fa-file-pdf-o', '#e2574c'],
  [['doc', 'docx'], 'fa-file-word-o', '#2b579a'],
  [['xls', 'xlsx'], 'fa-file-excel-o', '#217346'],
  [['ppt', 'pptx'], 'fa-file-powerpoint-o', '#d24726'],
  [['zip', 'rar', '7z'], 'fa-file-archive-o', '#8a6d3b'],
  [['jpg', 'jpeg', 'png', 'gif', 'bmp', 'webp'], 'fa-file-image-o', '#7a5af8'],
  [['txt'], 'fa-file-text-o', '#606266']
];

export function fileIcon(suffix) {
  const s = String(suffix || '').toLowerCase();
  const hit = SUFFIX_ICONS.find(item => item[0].indexOf(s) >= 0);
  return hit ? { cls: 'fa ' + hit[1], color: hit[2] } : { cls: 'fa fa-file-o', color: '#909399' };
}

const AVATAR_COLORS = ['#1a73e8', '#d93025', '#188038', '#e37400', '#9334e6', '#00838f', '#c2185b', '#5f6368'];

/** 头像：取姓名末字（中文）或首字母，颜色按成员编号散列 */
export function avatarOf(name, id) {
  const text = String(name || '?').trim();
  const isCjk = /[\u4e00-\u9fa5]/.test(text);
  const letter = isCjk ? text.slice(-2) : text.charAt(0).toUpperCase();
  let hash = 0;
  const key = String(id || text);
  for (let i = 0; i < key.length; i++) hash = (hash * 31 + key.charCodeAt(i)) >>> 0;
  return { text: letter, color: AVATAR_COLORS[hash % AVATAR_COLORS.length] };
}

export function escapeHtml(text) {
  return String(text == null ? '' : text)
    .replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;').replace(/"/g, '&quot;');
}

/** 回复、转发时在正文下方引用原邮件 */
export function quoteBlock(detail) {
  const names = list => (list || []).map(p => escapeHtml(p.name)).join('、');
  const lines = [
    '<p>------------------ 原始邮件 ------------------</p>',
    '<p><strong>发件人：</strong>' + escapeHtml(detail.sender && detail.sender.name) + '</p>',
    '<p><strong>发送时间：</strong>' + escapeHtml(fullTime(detail.sendTime)) + '</p>',
    '<p><strong>收件人：</strong>' + names(detail.to) + '</p>'
  ];
  if (detail.cc && detail.cc.length) lines.push('<p><strong>抄送：</strong>' + names(detail.cc) + '</p>');
  lines.push('<p><strong>主题：</strong>' + escapeHtml(detail.subject) + '</p>');
  return '<p></p><p></p>' + lines.join('') + '<blockquote>' + (detail.bodyHtml || '') + '</blockquote>';
}

export function prefixedSubject(prefix, subject) {
  const s = String(subject || '');
  return s.indexOf(prefix) === 0 ? s : prefix + s;
}
