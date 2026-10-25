<!--
 * Author: 周朔鹏
 * Date: 2026-10-12
 * Email: 391902958@qq.com
 * Website: https://www.euipd.com
-->

<!-- 阅读区：工具栏、主题、发件人与收件人、附件、正文 -->
<template>
  <div class="mail-reader" v-loading="loading">
    <template v-if="detail">
      <div class="mail-reader__toolbar">
        <template v-if="!inTrash">
          <button type="button" @click="compose('REPLY')"><i class="fa fa-reply"></i>回复</button>
          <button type="button" @click="compose('REPLY_ALL')"><i class="fa fa-reply-all"></i>全部回复</button>
          <button type="button" @click="compose('FORWARD')"><i class="fa fa-share"></i>转发</button>
          <span class="sep"></span>
          <button type="button" @click="trash"><i class="fa fa-trash-o"></i>删除</button>
        </template>
        <template v-else>
          <button type="button" @click="restore"><i class="fa fa-undo"></i>恢复</button>
          <button type="button" class="danger" @click="destroy"><i class="fa fa-times-circle-o"></i>彻底删除</button>
        </template>
        <span class="sep"></span>
        <button type="button" @click="markUnread"><i class="fa fa-envelope-o"></i>标为未读</button>
        <button type="button" @click="toggleStar">
          <i class="fa" :class="detail.starred ? 'fa-star on' : 'fa-star-o'"></i>{{ detail.starred ? '取消星标' : '星标' }}
        </button>
      </div>

      <div class="mail-reader__scroll">
        <h2 class="mail-reader__subject">{{ detail.subject || '（无主题）' }}</h2>

        <div class="mail-reader__head">
          <span class="mail-avatar mail-avatar--lg" :style="{ background: senderAvatar.color }">{{ senderAvatar.text }}</span>
          <div class="mail-reader__people">
            <div class="mail-reader__from">
              <strong>{{ detail.sender.name }}</strong>
              <span class="addr">&lt;{{ detail.sender.address }}&gt;</span>
              <span v-if="detail.sender.jobPosition" class="pos">{{ detail.sender.jobPosition }}</span>
              <span class="mail-reader__time">{{ fullTime(detail.sendTime || detail.updateTime) }}</span>
            </div>
            <div class="mail-reader__line">
              <span class="label">收件人</span>
              <span class="mail-reader__names">
                <span v-for="p in shown(detail.to, 'to')" :key="'to' + p.memberId" class="mail-person"
                      :title="p.address">{{ p.name }}<i v-if="p.memberId === selfId">（我）</i></span>
                <a v-if="detail.to.length > LIMIT && !expanded.to" @click="expanded.to = true">
                  等 {{ detail.to.length }} 人</a>
              </span>
            </div>
            <div class="mail-reader__line" v-if="detail.cc.length">
              <span class="label">抄送</span>
              <span class="mail-reader__names">
                <span v-for="p in shown(detail.cc, 'cc')" :key="'cc' + p.memberId" class="mail-person"
                      :title="p.address">{{ p.name }}<i v-if="p.memberId === selfId">（我）</i></span>
                <a v-if="detail.cc.length > LIMIT && !expanded.cc" @click="expanded.cc = true">
                  等 {{ detail.cc.length }} 人</a>
              </span>
            </div>
            <div class="mail-reader__line" v-if="detail.bcc.length">
              <span class="label">密送</span>
              <span class="mail-reader__names">
                <span v-for="p in detail.bcc" :key="'bcc' + p.memberId" class="mail-person"
                      :title="p.address">{{ p.name }}<i v-if="p.memberId === selfId">（我）</i></span>
              </span>
            </div>
          </div>
        </div>

        <div v-if="detail.attachments.length" class="mail-attachments">
          <div class="mail-attachments__title">
            <i class="fa fa-paperclip"></i> 附件 {{ detail.attachments.length }} 个（共 {{ totalSize }}）
          </div>
          <div class="mail-attachments__list">
            <div v-for="a in detail.attachments" :key="a.id" class="mail-file-card">
              <i :class="fileIcon(a.suffix).cls" :style="{ color: fileIcon(a.suffix).color }"></i>
              <div class="mail-file-card__text">
                <div class="mail-file-card__name" :title="a.name">{{ a.name }}</div>
                <div class="mail-file-card__size">{{ fileSize(a.size) }}</div>
              </div>
              <div class="mail-file-card__ops">
                <a v-if="previewable(a)" title="预览" @click="openAttachment(a, true)"><i class="fa fa-eye"></i></a>
                <a title="下载" @click="openAttachment(a, false)"><i class="fa fa-download"></i></a>
              </div>
            </div>
          </div>
        </div>

        <div class="mail-reader__body" v-rich-html="detail.bodyHtml"></div>
      </div>
    </template>
    <div v-else-if="error" class="mail-empty mail-empty--reader">
      <i class="fa fa-exclamation-circle"></i>
      <div>{{ error }}</div>
    </div>
  </div>
</template>

<script>
import * as api from '@/shared/mail-api';

const PREVIEW_SUFFIXES = ['pdf', 'jpg', 'jpeg', 'png', 'gif', 'bmp', 'webp', 'txt'];

export default {
  name: 'mail-reader',
  props: {
    boxId: { type: String, required: true },
    selfId: { type: String, default: '' }
  },

  data() {
    return {
      LIMIT: 6,
      detail: null,
      loading: false,
      error: '',
      expanded: { to: false, cc: false }
    };
  },

  computed: {
    inTrash() {
      return this.detail && this.detail.folder === 'TRASH';
    },
    senderAvatar() {
      const s = (this.detail && this.detail.sender) || {};
      return api.avatarOf(s.name, s.memberId);
    },
    totalSize() {
      const sum = (this.detail.attachments || []).reduce((n, a) => n + (Number(a.size) || 0), 0);
      return api.fileSize(sum);
    }
  },

  mounted() {
    this.load();
  },

  methods: {
    fullTime: api.fullTime,
    fileSize: api.fileSize,
    fileIcon: api.fileIcon,

    load() {
      this.loading = true;
      this.error = '';
      api.getMessage(this.boxId).then(d => {
        d.to = d.to || [];
        d.cc = d.cc || [];
        d.bcc = d.bcc || [];
        d.attachments = d.attachments || [];
        d.sender = d.sender || {};
        this.detail = d;
        this.$emit('loaded', d);
      }).catch(e => {
        this.error = e.message || '邮件加载失败';
      }).finally(() => {
        this.loading = false;
      });
    },

    shown(list, kind) {
      return this.expanded[kind] ? list : list.slice(0, this.LIMIT);
    },

    previewable(a) {
      return PREVIEW_SUFFIXES.indexOf(String(a.suffix || '').toLowerCase()) >= 0;
    },

    openAttachment(a, preview) {
      api.attachmentUrl(this.boxId, a.id, preview).then(url => {
        if (preview) {
          window.open(url, '_blank', 'noopener');
          return;
        }
        const link = document.createElement('a');
        link.href = url;
        link.rel = 'noopener';
        document.body.appendChild(link);
        link.click();
        document.body.removeChild(link);
      }).catch(e => this.$message.error(e.message));
    },

    compose(mode) {
      this.$emit('compose', { mode: mode, source: this.boxId });
    },

    trash() {
      api.moveBoxes([this.boxId], 'TRASH').then(() => {
        this.$message.success('邮件已移至“已删除”');
        this.$emit('removed', this.boxId);
      }).catch(e => this.$message.error(e.message));
    },

    restore() {
      api.moveBoxes([this.boxId], 'RESTORE').then(() => {
        this.$message.success('邮件已恢复');
        this.$emit('removed', this.boxId);
      }).catch(e => this.$message.error(e.message));
    },

    destroy() {
      this.$confirm('彻底删除后无法恢复，确定删除这封邮件吗？', '彻底删除', {
        type: 'warning', confirmButtonText: '彻底删除', cancelButtonText: '取消'
      }).then(() => api.deleteBoxes([this.boxId])).then(() => {
        this.$emit('removed', this.boxId);
      }).catch(e => { if (e && e.message) this.$message.error(e.message); });
    },

    markUnread() {
      api.markBoxes([this.boxId], 'UNREAD').then(() => {
        this.$emit('changed', { boxId: this.boxId, readFlag: 0 });
      }).catch(e => this.$message.error(e.message));
    },

    toggleStar() {
      const next = this.detail.starred ? 0 : 1;
      api.markBoxes([this.boxId], next ? 'STAR' : 'UNSTAR').then(() => {
        this.detail.starred = next;
        this.$emit('changed', { boxId: this.boxId, starred: next });
      }).catch(e => this.$message.error(e.message));
    }
  }
};
</script>
