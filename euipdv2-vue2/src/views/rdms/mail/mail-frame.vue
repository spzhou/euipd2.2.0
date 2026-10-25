<!--
 * Author: 周朔鹏
 * Date: 2026-10-12
 * Email: 391902958@qq.com
 * Website: https://www.euipd.com
-->

<!-- 内部邮件主页：左文件夹、中邮件列表、右阅读区；写信时右侧两栏换为写信区 -->
<template>
  <div class="mail-app">
    <aside class="mail-nav">
      <button type="button" class="mail-new" title="写邮件" @click="openCompose()">
        <i class="fa fa-pencil"></i><span>写邮件</span>
      </button>
      <div class="mail-account" v-if="account.address">
        <span class="mail-avatar mail-avatar--sm" :style="{ background: selfAvatar.color }">{{ selfAvatar.text }}</span>
        <div class="mail-account__text">
          <div class="mail-account__name">{{ account.name }}</div>
          <div class="mail-account__addr" :title="account.address">{{ account.address }}</div>
        </div>
      </div>
      <ul class="mail-folders">
        <li v-for="f in folders" :key="f.key" :class="{ active: !composing && folderKey === f.key }"
            :title="f.name" @click="goFolder(f.key)">
          <i :class="f.icon"></i>
          <span class="mail-folders__name">{{ f.name }}</span>
          <span v-if="folderBadge(f.key)" class="mail-folders__count" :class="{ strong: f.key === 'inbox' }">
            {{ folderBadge(f.key) }}
          </span>
        </li>
      </ul>
      <div class="mail-nav__foot">
        <a title="签名设置" @click="signatureVisible = true"><i class="fa fa-pencil-square-o"></i><span> 签名设置</span></a>
      </div>
    </aside>

    <template v-if="!composing">
      <section class="mail-list-pane">
        <div class="mail-search">
          <el-input v-model="query.keyword" size="small" clearable placeholder="搜索主题、正文、发件人"
                    prefix-icon="el-icon-search" @keyup.enter.native="search" @clear="search"></el-input>
          <el-popover v-model="filterVisible" placement="bottom-end" width="320" trigger="click">
            <el-form label-width="72px" size="mini" class="mail-filter">
              <el-form-item label="范围">
                <el-radio-group v-model="filter.scope">
                  <el-radio-button label="folder">当前文件夹</el-radio-button>
                  <el-radio-button label="all">全部邮件</el-radio-button>
                </el-radio-group>
              </el-form-item>
              <el-form-item label="发件人">
                <el-select v-model="filter.senderId" filterable clearable placeholder="不限" style="width: 100%;">
                  <el-option v-for="m in contacts" :key="m.memberId" :label="m.name" :value="m.memberId">
                    <span>{{ m.name }}</span><span class="mail-opt-sub">{{ m.departmentName }}</span>
                  </el-option>
                </el-select>
              </el-form-item>
              <el-form-item label="时间">
                <el-date-picker v-model="filter.dates" type="daterange" value-format="yyyy-MM-dd"
                                range-separator="至" start-placeholder="开始" end-placeholder="结束"
                                style="width: 100%;"></el-date-picker>
              </el-form-item>
              <el-form-item label="条件">
                <el-checkbox v-model="filter.hasAttachment">有附件</el-checkbox>
                <el-checkbox v-model="filter.unread">未读</el-checkbox>
                <el-checkbox v-model="filter.starred">星标</el-checkbox>
              </el-form-item>
              <div class="mail-filter__ops">
                <el-button size="mini" @click="resetFilter">清除</el-button>
                <el-button size="mini" type="primary" @click="search">搜索</el-button>
              </div>
            </el-form>
            <el-button slot="reference" size="small" class="mail-filter-btn" :class="{ on: filterActive }"
                       title="高级搜索"><i class="fa fa-sliders"></i></el-button>
          </el-popover>
        </div>

        <div class="mail-list-toolbar">
          <el-checkbox :value="allChecked" :indeterminate="someChecked" @change="toggleAll"></el-checkbox>
          <span class="mail-list-toolbar__title">
            {{ searching ? '搜索结果' : currentFolder.name }}
            <em v-if="page.total">{{ page.total }}</em>
          </span>
          <span class="mail-list-toolbar__ops">
            <template v-if="checkedIds.length">
              <a v-if="folderKey !== 'draft' && folderKey !== 'trash'" title="标为已读" @click="bulkMark('READ')">
                <i class="fa fa-envelope-open-o"></i></a>
              <a v-if="folderKey !== 'draft' && folderKey !== 'trash'" title="标为未读" @click="bulkMark('UNREAD')">
                <i class="fa fa-envelope-o"></i></a>
              <a v-if="folderKey === 'trash'" title="恢复" @click="bulkMove('RESTORE')"><i class="fa fa-undo"></i></a>
              <a v-if="folderKey === 'trash' || folderKey === 'draft'" title="彻底删除" class="danger"
                 @click="bulkDelete"><i class="fa fa-times-circle-o"></i></a>
              <a v-else title="删除" @click="bulkMove('TRASH')"><i class="fa fa-trash-o"></i></a>
            </template>
            <a title="刷新" @click="refreshAll"><i class="fa fa-refresh" :class="{ 'fa-spin': loading }"></i></a>
          </span>
        </div>

        <div class="mail-items" v-loading="loading && !items.length">
          <div v-for="item in items" :key="item.boxId" class="mail-item"
               :class="{ unread: !item.readFlag && item.folder !== 'DRAFT', selected: item.boxId === selectedId,
                         checked: checked[item.boxId] }"
               @click="openItem(item)">
            <div class="mail-item__check" @click.stop>
              <el-checkbox :value="!!checked[item.boxId]" @change="v => setChecked(item.boxId, v)"></el-checkbox>
            </div>
            <span class="mail-avatar" :style="{ background: itemAvatar(item).color }">{{ itemAvatar(item).text }}</span>
            <div class="mail-item__body">
              <div class="mail-item__row">
                <span class="mail-item__who">{{ whoOf(item) }}</span>
                <span class="mail-item__time">{{ shortTime(item.mailTime) }}</span>
              </div>
              <div class="mail-item__row">
                <span class="mail-item__subject">
                  <span v-if="item.folder === 'DRAFT'" class="mail-tag-draft">[草稿]</span>
                  {{ item.subject || '（无主题）' }}
                </span>
                <span class="mail-item__icons">
                  <i v-if="item.hasAttachment" class="fa fa-paperclip" title="有附件"></i>
                  <i v-if="item.folder !== 'DRAFT'" class="fa mail-star"
                     :class="item.starred ? 'fa-star on' : 'fa-star-o'"
                     :title="item.starred ? '取消星标' : '标记星标'" @click.stop="toggleStar(item)"></i>
                  <i class="fa fa-trash-o mail-item__del" title="删除" @click.stop="removeOne(item)"></i>
                </span>
              </div>
              <div class="mail-item__summary">{{ item.summary || '（无正文）' }}</div>
            </div>
          </div>
          <div v-if="!loading && !items.length" class="mail-empty mail-empty--list">
            <i class="fa fa-inbox"></i>
            <div>{{ searching ? '没有找到符合条件的邮件' : emptyText }}</div>
          </div>
        </div>

        <div class="mail-pager" v-if="page.total > page.size">
          <span>{{ rangeText }}</span>
          <a :class="{ disabled: page.page <= 1 }" @click="turn(-1)"><i class="fa fa-angle-left"></i></a>
          <a :class="{ disabled: page.page * page.size >= page.total }" @click="turn(1)">
            <i class="fa fa-angle-right"></i></a>
        </div>
      </section>

      <section class="mail-read-pane">
        <mail-reader v-if="selectedId" :key="selectedId" :box-id="selectedId" :self-id="account.memberId"
                     @changed="onReaderChanged" @removed="onReaderRemoved" @compose="openCompose"
                     @loaded="onReaderLoaded"></mail-reader>
        <div v-else class="mail-empty mail-empty--reader">
          <i class="fa fa-envelope-o"></i>
          <div>选择一封邮件阅读</div>
          <div class="mail-empty__sub" v-if="unread.inbox">收件箱有 {{ unread.inbox }} 封未读邮件</div>
        </div>
      </section>
    </template>

    <section v-else class="mail-compose-pane">
      <mail-compose ref="compose" :key="composeKey" :account="account" :contacts="contactData"
                    @sent="onSent" @saved="onDraftSaved" @discard="onDiscard"></mail-compose>
    </section>

    <el-dialog title="签名设置" :visible.sync="signatureVisible" width="560px" append-to-body>
      <p class="mail-sig-tip">签名会在新建邮件时自动添加到正文末尾，回复和转发时同样带入。</p>
      <rich-editor v-if="signatureVisible" v-model="signatureDraft" mode="simple" :max-length="500"
                   :min-height="120" placeholder="例如：张三 | 研发部 | 分机 8001"></rich-editor>
      <span slot="footer">
        <el-button size="small" @click="signatureVisible = false">取消</el-button>
        <el-button size="small" type="primary" :loading="signatureSaving" @click="saveSignature">保存</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
import RichEditor from '@/components/rich/rich-editor.vue';
import MailReader from './mail-reader.vue';
import MailCompose from './mail-compose.vue';
import * as api from '@/shared/mail-api';

const PAGE_SIZE = 30;
const POLL_MS = 60 * 1000;

export default {
  name: 'mail-frame',
  components: { RichEditor, MailReader, MailCompose },

  data() {
    return {
      folders: api.FOLDERS,
      account: {},
      unread: { inbox: 0, draft: 0, trash: 0 },
      contactData: null,
      items: [],
      page: { page: 1, size: PAGE_SIZE, total: 0 },
      loading: false,
      selectedId: null,
      checked: {},
      query: { keyword: '' },
      filter: { scope: 'folder', senderId: '', dates: null, hasAttachment: false, unread: false, starred: false },
      filterVisible: false,
      searching: false,
      signatureVisible: false,
      signatureDraft: '',
      signatureSaving: false,
      composeKey: 0,
      pollTimer: null,
      leaveConfirmed: false,
      composing: this.$route.name === 'customer/mail-compose'
    };
  },

  computed: {
    folderKey() {
      return (this.$route.params && this.$route.params.folder) || 'inbox';
    },
    currentFolder() {
      return api.folderByKey(this.folderKey);
    },
    contacts() {
      return (this.contactData && this.contactData.members) || [];
    },
    selfAvatar() {
      return api.avatarOf(this.account.name, this.account.memberId);
    },
    checkedIds() {
      return Object.keys(this.checked).filter(id => this.checked[id]);
    },
    allChecked() {
      return this.items.length > 0 && this.items.every(i => this.checked[i.boxId]);
    },
    someChecked() {
      return this.checkedIds.length > 0 && !this.allChecked;
    },
    filterActive() {
      const f = this.filter;
      return f.scope === 'all' || !!f.senderId || !!(f.dates && f.dates.length) || f.hasAttachment || f.unread || f.starred;
    },
    rangeText() {
      const from = (this.page.page - 1) * this.page.size + 1;
      const to = Math.min(this.page.page * this.page.size, this.page.total);
      return from + '–' + to + ' / ' + this.page.total;
    },
    emptyText() {
      return {
        inbox: '收件箱是空的',
        starred: '没有星标邮件',
        draft: '没有草稿',
        sent: '还没有发出邮件',
        trash: '已删除文件夹是空的'
      }[this.folderKey] || '没有邮件';
    }
  },

  watch: {
    '$route'(to, from) {
      if (to.name !== from.name || to.name !== (this.composing ? 'customer/mail-compose' : 'customer/mail')) return;
      if (this.composing) {
        this.composeKey += 1;
        return;
      }
      if (to.params.folder !== from.params.folder) {
        this.resetList();
        this.loadList();
      }
      if (to.query.box) {
        this.selectedId = to.query.box;
      }
    }
  },

  mounted() {
    this.loadAccount();
    this.loadCounts();
    this.loadContacts();
    if (!this.composing) {
      if (this.$route.query.box) this.selectedId = this.$route.query.box;
      this.loadList();
    }
    this.pollTimer = setInterval(this.poll, POLL_MS);
  },

  beforeDestroy() {
    if (this.pollTimer) clearInterval(this.pollTimer);
  },

  beforeRouteLeave(to, from, next) {
    this.confirmLeave(next);
  },

  methods: {
    shortTime: api.shortTime,

    loadAccount() {
      api.getSelf().then(a => {
        this.account = a || {};
        this.signatureDraft = this.account.signature || '';
        if (a && a.unread) this.unread = Object.assign({}, this.unread, a.unread);
      }).catch(e => this.$message.error(e.message));
    },

    loadCounts() {
      return api.countBox().then(c => {
        this.unread = Object.assign({ inbox: 0, draft: 0, trash: 0 }, c || {});
      }).catch(() => {});
    },

    loadContacts() {
      api.listContacts('').then(c => { this.contactData = c || { members: [], recent: [], tree: [] }; })
        .catch(() => { this.contactData = { members: [], recent: [], tree: [] }; });
    },

    folderBadge(key) {
      if (key === 'inbox') return this.unread.inbox || '';
      if (key === 'draft') return this.unread.draft || '';
      if (key === 'trash') return this.unread.trash || '';
      return '';
    },

    buildQuery() {
      const f = this.filter;
      const q = {
        folder: f.scope === 'all' && this.searching ? 'ALL' : this.currentFolder.code,
        keyword: this.query.keyword || '',
        page: this.page.page,
        size: this.page.size
      };
      if (this.searching) {
        if (f.senderId) q.senderId = f.senderId;
        if (f.dates && f.dates.length === 2) {
          q.dateFrom = f.dates[0];
          q.dateTo = f.dates[1];
        }
        if (f.hasAttachment) q.hasAttachment = true;
        if (f.unread) q.unread = true;
        if (f.starred) q.starred = true;
      }
      return q;
    },

    loadList(silent) {
      if (!silent) this.loading = true;
      return api.listBox(this.buildQuery()).then(p => {
        this.items = (p && p.list) || [];
        this.page.total = (p && p.total) || 0;
        const present = {};
        this.items.forEach(i => { present[i.boxId] = true; });
        Object.keys(this.checked).forEach(id => { if (!present[id]) this.$delete(this.checked, id); });
      }).catch(e => {
        this.$message.error(e.message);
      }).finally(() => {
        this.loading = false;
      });
    },

    resetList() {
      this.items = [];
      this.page.page = 1;
      this.page.total = 0;
      this.checked = {};
      this.selectedId = null;
      this.searching = false;
      this.query.keyword = '';
    },

    refreshAll() {
      this.loadCounts();
      return this.loadList();
    },

    poll() {
      if (document.hidden) return;
      this.loadCounts();
      if (!this.composing && this.page.page === 1 && !this.loading) this.loadList(true);
    },

    search() {
      this.filterVisible = false;
      this.searching = !!(this.query.keyword && this.query.keyword.trim()) || this.filterActive;
      this.page.page = 1;
      this.checked = {};
      this.loadList();
    },

    resetFilter() {
      this.filter = { scope: 'folder', senderId: '', dates: null, hasAttachment: false, unread: false, starred: false };
      this.search();
    },

    turn(step) {
      const next = this.page.page + step;
      if (next < 1 || (step > 0 && this.page.page * this.page.size >= this.page.total)) return;
      this.page.page = next;
      this.checked = {};
      this.loadList();
    },

    goFolder(key) {
      const path = '/customer/mail/' + key;
      if (this.$route.path === path && !Object.keys(this.$route.query).length) {
        this.resetList();
        this.refreshAll();
        return;
      }
      this.$router.push(path);
    },

    openCompose(query) {
      const q = Object.assign({ from: this.composing ? this.$route.query.from : this.$route.fullPath }, query || {});
      Object.keys(q).forEach(k => q[k] === undefined && delete q[k]);
      if (!this.composing) {
        this.$router.push({ name: 'customer/mail-compose', query: q });
        return;
      }
      this.confirmLeave(ok => {
        if (ok === false) return;
        this.leaveConfirmed = true;
        this.$router.push({ name: 'customer/mail-compose', query: Object.assign(q, { t: Date.now() }) })
          .catch(() => {}).finally(() => { this.leaveConfirmed = false; });
      });
    },

    whoOf(item) {
      if (item.folder === 'SENT' || item.folder === 'DRAFT' || (this.folderKey === 'sent')) {
        return item.recipientNames ? '收件人：' + item.recipientNames : '（未填写收件人）';
      }
      return item.senderName || item.senderAddress || '';
    },

    itemAvatar(item) {
      if (item.folder === 'SENT' || item.folder === 'DRAFT') {
        const first = (item.recipientNames || '').split('、')[0];
        return api.avatarOf(first || '?', first);
      }
      return api.avatarOf(item.senderName, item.senderId);
    },

    openItem(item) {
      if (item.folder === 'DRAFT') {
        this.openCompose({ draft: item.boxId });
        return;
      }
      this.selectedId = item.boxId;
      if (!item.readFlag) {
        item.readFlag = 1;
        if (item.folder === 'INBOX' && this.unread.inbox > 0) this.unread.inbox -= 1;
      }
    },

    setChecked(id, v) {
      this.$set(this.checked, id, !!v);
    },

    toggleAll(v) {
      const next = {};
      if (v) this.items.forEach(i => { next[i.boxId] = true; });
      this.checked = next;
    },

    afterChange() {
      this.loadCounts();
      this.notifyShell();
    },

    notifyShell() {
      if (this.$bus && this.$bus.publish) this.$bus.publish({ areas: ['mail'], reason: 'mail' });
    },

    bulkMark(action) {
      const ids = this.checkedIds;
      api.markBoxes(ids, action).then(() => {
        this.items.forEach(i => {
          if (ids.indexOf(i.boxId) >= 0) i.readFlag = action === 'READ' ? 1 : 0;
        });
        this.checked = {};
        this.afterChange();
      }).catch(e => this.$message.error(e.message));
    },

    bulkMove(action) {
      const ids = this.checkedIds;
      api.moveBoxes(ids, action).then(n => {
        this.$message.success(action === 'TRASH' ? '已删除 ' + n + ' 封邮件' : '已恢复 ' + n + ' 封邮件');
        if (ids.indexOf(this.selectedId) >= 0) this.selectedId = null;
        this.checked = {};
        this.loadList(true);
        this.afterChange();
      }).catch(e => this.$message.error(e.message));
    },

    bulkDelete() {
      const ids = this.checkedIds;
      this.$confirm('彻底删除后无法恢复，确定删除选中的 ' + ids.length + ' 封邮件吗？', '彻底删除', {
        type: 'warning', confirmButtonText: '彻底删除', cancelButtonText: '取消'
      }).then(() => api.deleteBoxes(ids)).then(() => {
        if (ids.indexOf(this.selectedId) >= 0) this.selectedId = null;
        this.checked = {};
        this.loadList(true);
        this.afterChange();
      }).catch(e => { if (e && e.message) this.$message.error(e.message); });
    },

    removeOne(item) {
      if (item.folder === 'TRASH' || item.folder === 'DRAFT') {
        this.checked = { [item.boxId]: true };
        this.bulkDelete();
        return;
      }
      api.moveBoxes([item.boxId], 'TRASH').then(() => {
        this.$message.success('邮件已删除');
        if (this.selectedId === item.boxId) this.selectedId = null;
        this.items = this.items.filter(i => i.boxId !== item.boxId);
        this.page.total = Math.max(0, this.page.total - 1);
        this.afterChange();
      }).catch(e => this.$message.error(e.message));
    },

    toggleStar(item) {
      const next = item.starred ? 0 : 1;
      api.markBoxes([item.boxId], next ? 'STAR' : 'UNSTAR').then(() => {
        item.starred = next;
        if (this.folderKey === 'starred' && !next) {
          this.items = this.items.filter(i => i.boxId !== item.boxId);
          this.page.total = Math.max(0, this.page.total - 1);
        }
      }).catch(e => this.$message.error(e.message));
    },

    onReaderLoaded(detail) {
      const item = this.items.find(i => i.boxId === detail.boxId);
      if (item) {
        item.readFlag = 1;
        item.starred = detail.starred;
      }
      this.notifyShell();
    },

    onReaderChanged(change) {
      const item = this.items.find(i => i.boxId === change.boxId);
      if (item && change.readFlag !== undefined) item.readFlag = change.readFlag;
      if (item && change.starred !== undefined) item.starred = change.starred;
      if (change.readFlag === 0) this.selectedId = null;
      this.afterChange();
    },

    onReaderRemoved(boxId) {
      const idx = this.items.findIndex(i => i.boxId === boxId);
      this.items = this.items.filter(i => i.boxId !== boxId);
      this.page.total = Math.max(0, this.page.total - 1);
      const next = this.items[idx] || this.items[idx - 1];
      this.selectedId = next && next.folder !== 'DRAFT' ? next.boxId : null;
      this.afterChange();
    },

    backFromCompose() {
      const from = this.$route.query.from;
      this.leaveConfirmed = true;
      this.$router.push(from && from.indexOf('/customer/mail/') === 0 ? from : '/customer/mail/inbox')
        .catch(() => {}).finally(() => { this.leaveConfirmed = false; });
    },

    onSent() {
      this.$message.success('邮件已发送');
      this.afterChange();
      this.backFromCompose();
    },

    onDraftSaved() {
      this.loadCounts();
    },

    onDiscard() {
      this.afterChange();
      this.backFromCompose();
    },

    confirmLeave(next) {
      const compose = this.$refs.compose;
      if (!this.composing || this.leaveConfirmed || !compose || !compose.isDirty()) {
        next();
        return;
      }
      this.$confirm('邮件尚未发送，是否保存到草稿箱？', '离开写信', {
        distinguishCancelAndClose: true,
        confirmButtonText: '保存草稿',
        cancelButtonText: '不保存',
        type: 'warning'
      }).then(() => compose.save(true).then(() => next()).catch(() => next(false)))
        .catch(action => next(action === 'cancel'));
    },

    saveSignature() {
      this.signatureSaving = true;
      api.updateSignature(this.signatureDraft).then(() => {
        this.account.signature = this.signatureDraft;
        this.signatureVisible = false;
        this.$message.success('签名已保存');
      }).catch(e => this.$message.error(e.message)).finally(() => {
        this.signatureSaving = false;
      });
    }
  }
};
</script>

<style lang="scss" src="./mail.scss"></style>
