<!--
 * Author: 周朔鹏
 * Date: 2026-10-12
 * Email: 391902958@qq.com
 * Website: https://www.euipd.com
-->

<!-- 写信区：收件人、抄送、密送、主题、附件、正文；每 60 秒自动保存草稿 -->
<template>
  <div class="mail-compose" v-loading="initializing"
       @dragenter.prevent="onDragEnter" @dragover.prevent @dragleave="onDragLeave" @drop.prevent="onDrop">
    <div class="mail-compose__toolbar">
      <el-button type="primary" size="small" :loading="sending" @click="send">
        <i class="fa fa-paper-plane"></i> 发送
      </el-button>
      <el-dropdown trigger="click" @command="onAttachCommand">
        <el-button size="small"><i class="fa fa-paperclip"></i> 添加附件 <i class="el-icon-arrow-down"></i></el-button>
        <el-dropdown-menu slot="dropdown">
          <el-dropdown-item command="local" icon="el-icon-upload2">上传本地文件</el-dropdown-item>
          <el-dropdown-item command="system" icon="el-icon-folder-opened">从系统文件选择</el-dropdown-item>
        </el-dropdown-menu>
      </el-dropdown>
      <el-button size="small" :loading="saving" @click="save(false)"><i class="fa fa-floppy-o"></i> 保存草稿</el-button>
      <el-button size="small" @click="discard"><i class="fa fa-trash-o"></i> 舍弃</el-button>
      <span class="mail-compose__status">{{ statusText }}</span>
    </div>

    <div class="mail-compose__fields">
      <div v-for="kind in visibleKinds" :key="kind" class="mail-field">
        <a class="mail-field__label" title="从通讯录选择" @click="openPicker">{{ kindLabel[kind] }}</a>
        <el-select v-model="recipients[kind]" multiple filterable default-first-option :filter-method="q => (queries[kind] = q)"
                   class="mail-field__select" popper-class="mail-person-popper" placeholder=""
                   @visible-change="v => !v && (queries[kind] = '')" @change="queries[kind] = ''">
          <el-option v-for="p in optionsFor(kind)" :key="p.memberId" :label="p.name" :value="p.memberId"
                     :disabled="p.disabled">
            <span class="mail-avatar mail-avatar--xs" :style="{ background: avatar(p).color }">{{ avatar(p).text }}</span>
            <span class="mail-opt-name">{{ p.name }}</span>
            <span class="mail-opt-sub">{{ p.departmentName }}</span>
            <span class="mail-opt-addr">{{ p.address }}</span>
          </el-option>
        </el-select>
        <span v-if="kind === 'to'" class="mail-field__extra">
          <a v-if="!showCc" @click="showCc = true">抄送</a>
          <a v-if="!showBcc" @click="showBcc = true">密送</a>
        </span>
      </div>
      <div class="mail-field">
        <span class="mail-field__label plain">主题</span>
        <input v-model="subject" class="mail-field__input" maxlength="200" placeholder="添加主题">
      </div>
    </div>

    <div v-if="attachments.length || uploads.length" class="mail-compose__files">
      <div v-for="(a, i) in attachments" :key="a.fileId" class="mail-chip">
        <i :class="fileIcon(a.suffix).cls" :style="{ color: fileIcon(a.suffix).color }"></i>
        <span class="mail-chip__name" :title="a.name">{{ a.name }}</span>
        <span class="mail-chip__size">{{ fileSize(a.size) }}</span>
        <i class="el-icon-close mail-chip__x" title="移除" @click="attachments.splice(i, 1)"></i>
      </div>
      <div v-for="u in uploads" :key="u.id" class="mail-chip uploading">
        <i class="el-icon-loading"></i>
        <span class="mail-chip__name" :title="u.name">{{ u.name }}</span>
        <span class="mail-chip__size">上传中</span>
      </div>
    </div>

    <div class="mail-compose__editor">
      <rich-editor v-if="!initializing" v-model="bodyHtml" mode="full" :max-length="50000" :min-height="360"
                   placeholder="在此输入正文"></rich-editor>
    </div>

    <input ref="file" type="file" multiple class="mail-hidden" @change="onPickFiles">
    <div v-if="dragging" class="mail-compose__drop"><i class="fa fa-cloud-upload"></i>松开鼠标，添加为附件</div>

    <mail-recipient-picker :visible.sync="pickerVisible" :contacts="contacts" :value="recipients"
                           :names="extraNames" @confirm="onPicked"></mail-recipient-picker>
    <mail-attachment-picker :visible.sync="attachPickerVisible" :attached-ids="attachments.map(a => a.fileId)"
                            @confirm="onAttachPicked"></mail-attachment-picker>
  </div>
</template>

<script>
import RichEditor from '@/components/rich/rich-editor.vue';
import MailRecipientPicker from '@/components/mail/mail-recipient-picker.vue';
import MailAttachmentPicker from '@/components/mail/mail-attachment-picker.vue';
import * as api from '@/shared/mail-api';

const AUTOSAVE_MS = 60 * 1000;
const MAX_ATTACHMENTS = 20;
const MAX_FILE_BYTES = 1024 * 1024 * 1024;
const OPTION_LIMIT = 60;

export default {
  name: 'mail-compose',
  components: { RichEditor, MailRecipientPicker, MailAttachmentPicker },
  props: {
    account: { type: Object, default: () => ({}) },
    contacts: { type: Object, default: null }
  },

  data() {
    return {
      initializing: true,
      recipients: { to: [], cc: [], bcc: [] },
      queries: { to: '', cc: '', bcc: '' },
      extraNames: {},
      showCc: false,
      showBcc: false,
      subject: '',
      bodyHtml: '',
      attachments: [],
      uploads: [],
      draftId: null,
      draftBoxId: null,
      sourceBoxId: null,
      sourceMode: null,
      dirty: false,
      saving: false,
      sending: false,
      lastSavedAt: null,
      autosaveTimer: null,
      pickerVisible: false,
      attachPickerVisible: false,
      dragging: false,
      dragDepth: 0,
      kindLabel: { to: '收件人', cc: '抄送', bcc: '密送' }
    };
  },

  computed: {
    visibleKinds() {
      const kinds = ['to'];
      if (this.showCc) kinds.push('cc');
      if (this.showBcc) kinds.push('bcc');
      return kinds;
    },
    members() {
      return (this.contacts && this.contacts.members) || [];
    },
    statusText() {
      if (this.saving) return '正在保存草稿…';
      if (this.lastSavedAt) {
        const d = this.lastSavedAt;
        const pad = n => (n < 10 ? '0' + n : n);
        return '草稿已于 ' + pad(d.getHours()) + ':' + pad(d.getMinutes()) + ' 保存';
      }
      return '';
    }
  },

  watch: {
    recipients: { deep: true, handler() { this.touch(); } },
    subject() { this.touch(); },
    bodyHtml() { this.touch(); },
    attachments() { this.touch(); }
  },

  mounted() {
    this.init().finally(() => {
      this.initializing = false;
      this.$nextTick(() => { this.dirty = false; });
    });
    this.autosaveTimer = setInterval(() => {
      if (this.dirty && !this.saving && !this.sending && this.hasContent()) this.save(true).catch(() => {});
    }, AUTOSAVE_MS);
    window.addEventListener('beforeunload', this.onBeforeUnload);
  },

  beforeDestroy() {
    clearInterval(this.autosaveTimer);
    window.removeEventListener('beforeunload', this.onBeforeUnload);
  },

  methods: {
    fileIcon: api.fileIcon,
    fileSize: api.fileSize,

    avatar(p) {
      return api.avatarOf(p.name, p.memberId);
    },

    touch() {
      if (!this.initializing) this.dirty = true;
    },

    init() {
      const q = this.$route.query || {};
      const self = api.getSelf().catch(() => ({}));
      if (q.draft) {
        return api.getMessage(q.draft).then(d => {
          this.draftBoxId = d.boxId;
          this.draftId = d.messageId;
          this.fillPeople(d);
          this.recipients = {
            to: (d.to || []).map(p => p.memberId),
            cc: (d.cc || []).map(p => p.memberId),
            bcc: (d.bcc || []).map(p => p.memberId)
          };
          this.showCc = this.recipients.cc.length > 0;
          this.showBcc = this.recipients.bcc.length > 0;
          this.subject = d.subject || '';
          this.bodyHtml = d.bodyHtml || '';
          this.attachments = (d.attachments || []).map(a => ({
            fileId: a.fileId, name: a.name, size: a.size, suffix: a.suffix, source: a.source
          }));
        }).catch(e => this.$message.error(e.message));
      }
      if (q.mode && q.source) {
        return Promise.all([self, api.getMessage(q.source)]).then(([me, d]) => {
          this.prefillFromSource(q.mode, d, me || {});
        }).catch(e => this.$message.error(e.message));
      }
      return self.then(me => {
        if (q.to) this.recipients.to = [].concat(q.to);
        this.bodyHtml = this.signatureBlock(me, '<p></p><p></p>');
      });
    },

    signatureBlock(me, lead) {
      return me && me.signature ? lead + me.signature : '';
    },

    fillPeople(d) {
      const names = Object.assign({}, this.extraNames);
      [d.sender].concat(d.to || [], d.cc || [], d.bcc || []).forEach(p => {
        if (p && p.memberId) names[p.memberId] = p.name;
      });
      this.extraNames = names;
    },

    prefillFromSource(mode, d, me) {
      this.sourceBoxId = d.boxId;
      this.sourceMode = mode;
      this.fillPeople(d);
      const selfId = me.memberId;
      const senderId = d.sender && d.sender.memberId;
      const ids = list => (list || []).map(p => p.memberId);
      if (mode === 'FORWARD') {
        this.subject = api.prefixedSubject('转发：', d.subject);
        this.attachments = (d.attachments || []).map(a => ({
          fileId: a.fileId, name: a.name, size: a.size, suffix: a.suffix, source: 'MAIL'
        }));
      } else {
        this.subject = api.prefixedSubject('回复：', d.subject);
        let to = senderId === selfId ? ids(d.to) : [senderId];
        let cc = [];
        if (mode === 'REPLY_ALL') {
          to = to.concat(ids(d.to));
          cc = ids(d.cc);
        }
        const uniq = list => list.filter((id, i) => id && id !== selfId && list.indexOf(id) === i);
        to = uniq(to);
        if (!to.length && senderId) to = [senderId];
        cc = uniq(cc).filter(id => to.indexOf(id) < 0);
        this.recipients = { to: to, cc: cc, bcc: [] };
        this.showCc = cc.length > 0;
      }
      this.bodyHtml = '<p></p>' + this.signatureBlock(me, '<p></p>') + api.quoteBlock(d);
    },

    optionsFor(kind) {
      const chosen = this.recipients[kind] || [];
      const known = {};
      const list = [];
      this.members.forEach(m => { known[m.memberId] = true; });
      chosen.forEach(id => {
        if (!known[id]) list.push({ memberId: id, name: (this.extraNames[id] || id) + '（不可用）', disabled: true });
      });
      const kw = (this.queries[kind] || '').trim().toLowerCase();
      const matched = !kw ? this.members : this.members.filter(m => [m.name, m.jobNum, m.departmentName, m.address]
        .some(v => v && String(v).toLowerCase().indexOf(kw) >= 0));
      const picked = {};
      chosen.forEach(id => { picked[id] = true; });
      const head = matched.filter(m => picked[m.memberId]);
      const rest = matched.filter(m => !picked[m.memberId]).slice(0, OPTION_LIMIT);
      return list.concat(head, rest);
    },

    openPicker() {
      this.pickerVisible = true;
    },

    onPicked(value) {
      this.recipients = value;
      if (value.cc.length) this.showCc = true;
      if (value.bcc.length) this.showBcc = true;
    },

    // ---------- 附件 ----------

    onAttachCommand(cmd) {
      if (cmd === 'local') {
        this.$refs.file.value = '';
        this.$refs.file.click();
      } else {
        this.attachPickerVisible = true;
      }
    },

    onAttachPicked(list) {
      const have = {};
      this.attachments.forEach(a => { have[a.fileId] = true; });
      const added = list.filter(a => !have[a.fileId]);
      if (this.attachments.length + added.length > MAX_ATTACHMENTS) {
        this.$message.warning('附件不能超过 ' + MAX_ATTACHMENTS + ' 个');
        return;
      }
      this.attachments = this.attachments.concat(added);
    },

    onPickFiles(e) {
      this.uploadFiles(Array.prototype.slice.call(e.target.files || []));
    },

    onDragEnter(e) {
      if (!e.dataTransfer || Array.prototype.indexOf.call(e.dataTransfer.types || [], 'Files') < 0) return;
      this.dragDepth += 1;
      this.dragging = true;
    },

    onDragLeave() {
      this.dragDepth = Math.max(0, this.dragDepth - 1);
      if (!this.dragDepth) this.dragging = false;
    },

    onDrop(e) {
      this.dragDepth = 0;
      this.dragging = false;
      const files = e.dataTransfer ? Array.prototype.slice.call(e.dataTransfer.files || []) : [];
      if (files.length) this.uploadFiles(files);
    },

    uploadFiles(files) {
      const allowed = (typeof G_SUFFIXS !== 'undefined' ? G_SUFFIXS : []).map(s => String(s).toLowerCase());
      if (this.attachments.length + this.uploads.length + files.length > MAX_ATTACHMENTS) {
        this.$message.warning('附件不能超过 ' + MAX_ATTACHMENTS + ' 个');
        return;
      }
      const customerId = (Tool.getLoginCustomer() || {}).id;
      files.forEach(file => {
        const suffix = file.name.substring(file.name.lastIndexOf('.') + 1).toLowerCase();
        if (allowed.length && allowed.indexOf(suffix) < 0) {
          this.$message.warning(file.name + '：不支持的文件类型');
          return;
        }
        if (file.size > MAX_FILE_BYTES) {
          this.$message.warning(file.name + '：单个文件不能超过 1 GB');
          return;
        }
        const task = { id: Date.now() + '-' + Math.random(), name: file.name };
        this.uploads.push(task);
        api.uploadLocalFile(file, customerId).then(saved => {
          this.attachments.push({
            fileId: saved.id, name: saved.name, size: saved.size, suffix: saved.suffix, source: 'UPLOADED'
          });
        }).catch(err => {
          this.$message.error(file.name + '：' + (err.message || '上传失败'));
        }).finally(() => {
          this.uploads = this.uploads.filter(u => u !== task);
        });
      });
    },

    // ---------- 保存与发送 ----------

    hasContent() {
      const r = this.recipients;
      return r.to.length + r.cc.length + r.bcc.length > 0 || !!this.subject.trim()
        || !!this.bodyHtml || this.attachments.length > 0;
    },

    isDirty() {
      return this.dirty && this.hasContent() && !this.sending;
    },

    payload() {
      return {
        draftId: this.draftId,
        to: this.recipients.to,
        cc: this.showCc ? this.recipients.cc : [],
        bcc: this.showBcc ? this.recipients.bcc : [],
        subject: this.subject,
        bodyHtml: this.bodyHtml,
        fileIds: this.attachments.map(a => a.fileId),
        sourceBoxId: this.sourceBoxId,
        sourceMode: this.sourceMode
      };
    },

    save(silent) {
      if (this.uploads.length && !silent) {
        this.$message.warning('附件正在上传，请稍候');
        return Promise.reject(new Error('uploading'));
      }
      this.saving = true;
      return api.saveDraft(this.payload()).then(r => {
        this.draftId = r.draftId;
        this.draftBoxId = r.boxId;
        this.dirty = false;
        this.lastSavedAt = new Date();
        this.$emit('saved', r);
        if (!silent) this.$message.success('草稿已保存');
        return r;
      }).catch(e => {
        if (!silent) this.$message.error(e.message);
        throw e;
      }).finally(() => {
        this.saving = false;
      });
    },

    send() {
      if (this.uploads.length) {
        this.$message.warning('附件正在上传，请稍候');
        return;
      }
      if (!this.recipients.to.length) {
        this.$message.warning('请至少填写一名收件人');
        return;
      }
      const checks = [];
      if (!this.subject.trim()) checks.push('这封邮件没有主题');
      const text = (this.bodyHtml || '').replace(/<blockquote>[\s\S]*<\/blockquote>/, '') + this.subject;
      if (!this.attachments.length && /附件|附上|见附/.test(text)) checks.push('正文提到了附件，但没有添加附件');
      const go = () => {
        this.sending = true;
        api.sendMail(this.payload()).then(() => {
          this.dirty = false;
          this.$emit('sent');
        }).catch(e => {
          this.$message.error(e.message);
        }).finally(() => {
          this.sending = false;
        });
      };
      if (!checks.length) {
        go();
        return;
      }
      this.$confirm(checks.join('；') + '。仍要发送吗？', '发送确认', {
        type: 'warning', confirmButtonText: '仍然发送', cancelButtonText: '返回修改'
      }).then(go).catch(() => {});
    },

    discard() {
      const hasDraft = !!this.draftBoxId;
      if (!hasDraft && !this.isDirty()) {
        this.$emit('discard');
        return;
      }
      this.$confirm(hasDraft ? '舍弃后草稿将被删除，确定舍弃吗？' : '确定舍弃这封邮件吗？', '舍弃邮件', {
        type: 'warning', confirmButtonText: '舍弃', cancelButtonText: '取消'
      }).then(() => (hasDraft ? api.deleteBoxes([this.draftBoxId]) : null)).then(() => {
        this.dirty = false;
        this.$emit('discard');
      }).catch(e => { if (e && e.message) this.$message.error(e.message); });
    },

    onBeforeUnload(e) {
      if (!this.isDirty()) return undefined;
      e.preventDefault();
      e.returnValue = '';
      return '';
    }
  }
};
</script>
