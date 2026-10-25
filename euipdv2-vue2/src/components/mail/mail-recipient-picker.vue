<!--
 * Author: 周朔鹏
 * Date: 2026-10-12
 * Email: 391902958@qq.com
 * Website: https://www.euipd.com
-->

<!-- 通讯录：左部门树与最近联系人，中成员列表，右收件人、抄送、密送三栏 -->
<template>
  <el-dialog title="选择联系人" :visible="visible" width="980px" append-to-body top="6vh"
             custom-class="mail-picker-dlg" @close="$emit('update:visible', false)">
    <div class="mail-picker">
      <div class="mail-picker__side">
        <el-tabs v-model="tab" stretch>
          <el-tab-pane label="组织架构" name="org">
            <el-tree :data="treeData" node-key="key" :props="{ label: 'label', children: 'children' }"
                     default-expand-all highlight-current :expand-on-click-node="false"
                     class="mail-picker__tree" @node-click="node => (deptKey = node.key)">
              <span slot-scope="{ data }" class="mail-picker__node">
                <span>{{ data.label }}</span><em>{{ data.count }}</em>
              </span>
            </el-tree>
          </el-tab-pane>
          <el-tab-pane label="最近联系人" name="recent">
            <div class="mail-picker__hint">近 90 天内发信最多的同事</div>
          </el-tab-pane>
        </el-tabs>
      </div>

      <div class="mail-picker__members">
        <el-input v-model="keyword" size="small" clearable prefix-icon="el-icon-search"
                  placeholder="姓名、工号、部门或地址"></el-input>
        <div class="mail-picker__list-head">
          <el-checkbox :value="allPicked" :indeterminate="somePicked" @change="pickAll">{{ listTitle }}</el-checkbox>
          <span>{{ visibleMembers.length }} 人</span>
        </div>
        <div class="mail-picker__list">
          <div v-for="m in visibleMembers" :key="m.memberId" class="mail-picker__row"
               :class="{ on: picked[m.memberId] }" @click="togglePick(m.memberId)"
               @dblclick="addTo('to', [m.memberId])">
            <el-checkbox :value="!!picked[m.memberId]" @click.native.stop
                         @change="v => $set(picked, m.memberId, v)"></el-checkbox>
            <span class="mail-avatar mail-avatar--sm" :style="{ background: avatar(m).color }">{{ avatar(m).text }}</span>
            <div class="mail-picker__who">
              <div>{{ m.name }} <span class="sub">{{ m.jobPosition }}</span></div>
              <div class="sub">{{ m.departmentName }} · {{ m.address }}</div>
            </div>
          </div>
          <div v-if="!visibleMembers.length" class="mail-picker__empty">没有符合条件的成员</div>
        </div>
      </div>

      <div class="mail-picker__targets">
        <div v-for="t in targets" :key="t.kind" class="mail-picker__target">
          <el-button size="mini" :disabled="!pickedIds.length" @click="addTo(t.kind, pickedIds)">
            {{ t.name }} <i class="el-icon-arrow-right"></i>
          </el-button>
          <div class="mail-picker__box">
            <el-tag v-for="id in chosen[t.kind]" :key="id" size="small" closable disable-transitions
                    @close="remove(t.kind, id)">{{ nameOf(id) }}</el-tag>
          </div>
        </div>
      </div>
    </div>
    <span slot="footer">
      <el-button size="small" @click="$emit('update:visible', false)">取消</el-button>
      <el-button size="small" type="primary" @click="confirm">确定</el-button>
    </span>
  </el-dialog>
</template>

<script>
import { avatarOf } from '@/shared/mail-api';

const ALL = '__all__';

export default {
  name: 'mail-recipient-picker',
  props: {
    visible: { type: Boolean, default: false },
    contacts: { type: Object, default: null },
    value: { type: Object, default: () => ({ to: [], cc: [], bcc: [] }) },
    names: { type: Object, default: () => ({}) }
  },

  data() {
    return {
      tab: 'org',
      deptKey: ALL,
      keyword: '',
      picked: {},
      chosen: { to: [], cc: [], bcc: [] },
      targets: [
        { kind: 'to', name: '收件人' },
        { kind: 'cc', name: '抄送' },
        { kind: 'bcc', name: '密送' }
      ]
    };
  },

  computed: {
    members() {
      return (this.contacts && this.contacts.members) || [];
    },
    byId() {
      const map = {};
      this.members.forEach(m => { map[m.memberId] = m; });
      return map;
    },
    treeData() {
      const root = this.contacts && this.contacts.tree;
      const all = { key: ALL, label: '全部成员', count: this.members.length, ids: null, children: [] };
      if (!root) return [all];
      const walk = (node, path) => {
        const key = node.id || path;
        const children = (node.children || []).map((c, i) => walk(c, path + '-' + i));
        const ids = (node.memberIds || []).filter(id => this.byId[id]);
        children.forEach(c => c.ids.forEach(id => { if (ids.indexOf(id) < 0) ids.push(id); }));
        return { key: key, label: node.name, count: ids.length, ids: ids, children: children };
      };
      const tree = walk(root, 'root');
      return [all, tree];
    },
    deptIds() {
      if (this.deptKey === ALL) return null;
      const find = list => {
        for (let i = 0; i < list.length; i++) {
          if (list[i].key === this.deptKey) return list[i];
          const hit = find(list[i].children || []);
          if (hit) return hit;
        }
        return null;
      };
      const node = find(this.treeData);
      return node ? node.ids : null;
    },
    visibleMembers() {
      let list = this.tab === 'recent' ? ((this.contacts && this.contacts.recent) || []) : this.members;
      if (this.tab === 'org' && this.deptIds) {
        const set = {};
        this.deptIds.forEach(id => { set[id] = true; });
        list = list.filter(m => set[m.memberId]);
      }
      const kw = (this.keyword || '').trim().toLowerCase();
      if (kw) {
        list = list.filter(m => [m.name, m.jobNum, m.departmentName, m.address]
          .some(v => v && String(v).toLowerCase().indexOf(kw) >= 0));
      }
      return list;
    },
    listTitle() {
      return this.tab === 'recent' ? '最近联系人' : '全选';
    },
    pickedIds() {
      return Object.keys(this.picked).filter(id => this.picked[id]);
    },
    allPicked() {
      return this.visibleMembers.length > 0 && this.visibleMembers.every(m => this.picked[m.memberId]);
    },
    somePicked() {
      return this.pickedIds.length > 0 && !this.allPicked;
    }
  },

  watch: {
    visible(v) {
      if (!v) return;
      this.chosen = {
        to: (this.value.to || []).slice(),
        cc: (this.value.cc || []).slice(),
        bcc: (this.value.bcc || []).slice()
      };
      this.picked = {};
      this.keyword = '';
    }
  },

  methods: {
    avatar(m) {
      return avatarOf(m.name, m.memberId);
    },
    nameOf(id) {
      return (this.byId[id] && this.byId[id].name) || this.names[id] || id;
    },
    togglePick(id) {
      this.$set(this.picked, id, !this.picked[id]);
    },
    pickAll(v) {
      const next = Object.assign({}, this.picked);
      this.visibleMembers.forEach(m => { next[m.memberId] = !!v; });
      this.picked = next;
    },
    addTo(kind, ids) {
      ids.forEach(id => {
        ['to', 'cc', 'bcc'].forEach(k => {
          if (k !== kind) this.chosen[k] = this.chosen[k].filter(x => x !== id);
        });
        if (this.chosen[kind].indexOf(id) < 0) this.chosen[kind].push(id);
      });
      this.picked = {};
    },
    remove(kind, id) {
      this.chosen[kind] = this.chosen[kind].filter(x => x !== id);
    },
    confirm() {
      this.$emit('confirm', {
        to: this.chosen.to.slice(),
        cc: this.chosen.cc.slice(),
        bcc: this.chosen.bcc.slice()
      });
      this.$emit('update:visible', false);
    }
  }
};
</script>
