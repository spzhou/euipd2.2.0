<!--
 * Author: 周朔鹏
 * Date: 2026-10-03
 * Email: 391902958@qq.com
 * Website: https://www.euipd.com
-->
<template>
  <div style="padding: 12px 16px 24px;">
    <div style="display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: 12px;">
      <div style="padding-right: 16px;">
        <div style="font-size: 18px;">权限配置</div>
        <div style="color: #666; margin-top: 4px; line-height: 1.6;">
          勾选本机构其他角色的菜单和按钮。管理员与最终管理人为锁定列：最终管理人拥有全部菜单，管理员按其专用模板（团队管理、假日设置、绩效/盘点、绩效管理与通用菜单）；团队管理、假日设置只属于最终管理人和管理员，仓库管理、库存管理只属于最终管理人和物料管理员，物料管理员的物料管理也不可取消，我的邮件为全员功能、不可取消，这些格子不可改动。勾选模板以外的菜单时，同时授予该菜单所属模块的操作权限；取消模板内的菜单时，撤回该模块。保存后，对应用户重新登录生效。
        </div>
      </div>
      <div style="white-space: nowrap;">
        <el-button size="small" v-on:click="resetPolicy">全部恢复默认</el-button>
        <el-button size="small" type="primary" :disabled="!dirty && !stale && !overlayInvalid && !(diff.customized && !diff.recorded) && !diffVisible" v-on:click="savePolicy">保存</el-button>
      </div>
    </div>
    <el-alert v-if="overlayInvalid" type="error" :closable="false" show-icon style="margin-bottom: 8px;"
              title="本机构保存的权限调整无法解析，当前按平台默认显示。重新保存或恢复默认后即可修正。">
    </el-alert>
    <el-alert v-if="stale && !diff.recorded" type="warning" :closable="false" show-icon style="margin-bottom: 8px;"
              :title="'本机构的调整基于平台模板 ' + baseVersion + '，当前模板为 ' + version + '。平台新增的菜单不会自动加入已调整的角色，请核对后重新保存。'">
    </el-alert>
    <el-alert v-if="diff.customized && !diff.recorded" type="info" :closable="false" show-icon style="margin-bottom: 8px;"
              title="本机构的调整保存时未记录平台模板基准，无法列出平台模板差异。下次保存后生效。">
    </el-alert>
    <div v-if="diffVisible" style="border: 1px solid #f0d9a8; background: #fdf8ec; padding: 8px 12px; margin-bottom: 8px; line-height: 1.8; font-size: 13px;">
      <div style="display: flex; justify-content: space-between; align-items: center;">
        <span style="font-weight: 600;">平台模板差异（本机构保存时为 {{ diff.baseVersion || '未知' }}，当前为 {{ diff.version }}）</span>
        <el-button size="mini" type="warning" :disabled="!diff.pendingCount" v-on:click="mergePending">
          并入新增资源{{ diff.pendingCount ? '（' + diff.pendingCount + ' 项）' : '' }}
        </el-button>
      </div>
      <div v-if="diff.added.length">平台新增资源：{{ namesOf(diff.added) }}</div>
      <div v-if="diff.removed.length">平台删除资源：{{ namesOf(diff.removed) }}。服务启动时已从本机构调整中移除。</div>
      <div v-if="diff.renamed.length">资源改名：{{ renamedText }}</div>
      <div v-for="role in diff.roles" :key="role.code">
        {{ role.name || role.code }}：<span v-if="role.defaultAdded.length">平台默认新增 {{ idNames(role.defaultAdded) }}；</span><span v-if="role.defaultRemoved.length">平台默认取消 {{ idNames(role.defaultRemoved) }}；</span>{{ role.pending.length ? '尚未并入 ' + role.pending.length + ' 项。' : '新增项已在本机构调整中。' }}
      </div>
      <div style="color: #999;">并入后需保存。保存时以当前模板为新的基准，未并入的新增资源不再列出。</div>
    </div>
    <div v-if="catalog.length" style="color: #666; margin-bottom: 8px; font-size: 13px;">
      {{ statusText }}<span v-if="dirty" style="color: #c47b00;">有未保存的修改。</span>
    </div>
    <el-table v-if="catalog.length" :data="catalog" border size="small" max-height="640">
      <el-table-column label="资源" min-width="220" fixed>
        <template slot-scope="scope">
          <span :style="{ paddingLeft: scope.row.kind === 'BUTTON' ? '18px' : '0' }">
            {{ scope.row.name }}
            <span style="color: #999;"> {{ scope.row.id }}</span>
          </span>
          <div v-if="scope.row.kind === 'MENU' && scope.row.module" style="color: #aaa; font-size: 12px;"
               :title="scope.row.operations && scope.row.operations.length ? '补授操作：' + scope.row.operations.join('、') : '该模块不随勾选补授'">
            模块 {{ scope.row.module }}
          </div>
          <div v-if="scope.row.id === '5301'" style="color: #aaa; font-size: 12px;">全员功能，不可收回</div>
        </template>
      </el-table-column>
      <el-table-column
          v-for="role in roles"
          :key="role.code"
          width="120"
          align="center">
        <template v-slot:header>
          <div>{{ role.name || role.code }}</div>
          <div style="font-size: 12px; font-weight: normal; height: 18px;">
            <el-button v-show="!role.fixed && roleDiffers(role)" type="text" size="mini" style="padding: 0;"
                       v-on:click="resetColumn(role)">恢复本列默认</el-button>
          </div>
        </template>
        <template slot-scope="scope">
          <el-checkbox
              :value="checked(role, scope.row.id)"
              :disabled="role.fixed || locked(role, scope.row.id)"
              v-on:change="function (val) { toggle(role, scope.row, val); }">
          </el-checkbox>
          <div v-if="!role.fixed && differs(role, scope.row.id)" style="color: #c47b00; font-size: 12px;">已改</div>
        </template>
      </el-table-column>
    </el-table>
    <div v-else style="color: #666;">{{ hint }}</div>
  </div>
</template>

<script>
function sameIds(a, b) {
  let left = a || [];
  let right = b || [];
  if (left.length !== right.length) {
    return false;
  }
  let set = {};
  left.forEach(function (id) { set[id] = true; });
  return right.every(function (id) { return set[id]; });
}

function emptyDiff() {
  return { customized: false, recorded: false, version: '', baseVersion: '', added: [], removed: [], renamed: [], roles: [], pendingCount: 0 };
}

export default {
  name: 'tenant-ui-policy',
  data: function () {
    return {
      catalog: [],
      roles: [],
      draft: {},
      saved: {},
      defaults: {},
      version: '',
      baseVersion: '',
      stale: false,
      overlayInvalid: false,
      updatedBy: '',
      updatedByName: '',
      updatedAt: null,
      diff: emptyDiff(),
      hint: '正在读取配置'
    };
  },
  computed: {
    diffVisible: function () {
      let d = this.diff;
      return d.recorded && (d.added.length || d.removed.length || d.renamed.length || d.roles.length);
    },
    renamedText: function () {
      return this.diff.renamed.map(function (item) {
        return item.oldName + ' 改为 ' + item.name;
      }).join('、');
    },
    dirty: function () {
      let _this = this;
      return _this.roles.some(function (role) {
        return !role.fixed && !sameIds(_this.draft[role.code], _this.saved[role.code]);
      });
    },
    customizedNames: function () {
      let _this = this;
      return _this.roles.filter(function (role) {
        return !role.fixed && !sameIds(_this.saved[role.code], _this.defaults[role.code]);
      }).map(function (role) {
        return role.name || role.code;
      });
    },
    statusText: function () {
      let text = '平台模板 ' + this.version + '。';
      text += this.customizedNames.length
          ? '已调整角色：' + this.customizedNames.join('、') + '。'
          : '本机构未调整，各角色使用平台默认。';
      if (this.updatedAt) {
        text += '最后修改：' + (this.updatedByName || this.updatedBy || '未知') + '，' + this.formatTime(this.updatedAt) + '。';
      }
      return text;
    }
  },
  mounted: function () {
    window.addEventListener('beforeunload', this.onBeforeUnload);
    this.loadPolicy();
  },
  beforeDestroy: function () {
    window.removeEventListener('beforeunload', this.onBeforeUnload);
  },
  beforeRouteLeave: function (to, from, next) {
    if (!this.dirty) {
      next();
      return;
    }
    this.$confirm('权限配置有未保存的修改，离开后将丢失。', '离开页面', {
      type: 'warning',
      confirmButtonText: '离开',
      cancelButtonText: '留在本页'
    }).then(function () {
      next();
    }).catch(function () {
      next(false);
    });
  },
  methods: {
    onBeforeUnload: function (event) {
      if (!this.dirty) {
        return undefined;
      }
      event.preventDefault();
      event.returnValue = '';
      return '';
    },
    loadPolicy: function () {
      let _this = this;
      _this.$ajax.get(process.env.VUE_APP_SERVER + '/system/tenant-ui-policy').then(function (response) {
        let resp = response.data;
        if (!resp || resp.success === false) {
          _this.hint = (resp && resp.message) || '无法读取权限配置';
          return;
        }
        let content = resp.content || {};
        _this.catalog = content.catalog || [];
        _this.roles = content.roles || [];
        _this.version = content.version || '';
        _this.baseVersion = content.baseVersion || '';
        _this.stale = !!content.stale;
        _this.overlayInvalid = !!content.overlayInvalid;
        _this.updatedBy = content.updatedBy || '';
        _this.updatedByName = content.updatedByName || '';
        _this.updatedAt = content.updatedAt || null;
        let draft = {};
        let saved = {};
        let defaults = {};
        _this.roles.forEach(function (role) {
          defaults[role.code] = role.defaultIds || [];
          draft[role.code] = (role.effectiveIds || []).slice();
          saved[role.code] = (role.effectiveIds || []).slice();
        });
        _this.defaults = defaults;
        _this.saved = saved;
        _this.draft = draft;
        if (!_this.catalog.length) {
          _this.hint = '目录为空';
        }
        _this.loadDiff(!!content.customized);
      });
    },
    loadDiff: function (customized) {
      let _this = this;
      _this.diff = emptyDiff();
      if (!customized) {
        return;
      }
      _this.$ajax.get(process.env.VUE_APP_SERVER + '/system/tenant-ui-policy/diff').then(function (response) {
        let resp = response.data;
        if (!resp || resp.success === false || !resp.content) {
          return;
        }
        _this.diff = Object.assign(emptyDiff(), resp.content);
      });
    },
    namesOf: function (items) {
      return items.map(function (item) { return item.name || item.id; }).join('、');
    },
    idNames: function (ids) {
      let byId = {};
      this.catalog.forEach(function (item) { byId[item.id] = item.name; });
      this.diff.removed.forEach(function (item) { byId[item.id] = byId[item.id] || item.name; });
      return ids.map(function (id) { return byId[id] || id; }).join('、');
    },
    mergePending: function () {
      let _this = this;
      let byId = {};
      _this.catalog.forEach(function (item) { byId[item.id] = item; });
      let merged = 0;
      _this.diff.roles.forEach(function (diffRole) {
        let role = _this.roles.filter(function (r) { return r.code === diffRole.code; })[0];
        if (!role || role.fixed) {
          return;
        }
        let set = {};
        (_this.draft[role.code] || []).forEach(function (id) { set[id] = true; });
        diffRole.pending.forEach(function (id) {
          let item = byId[id];
          if (!item || set[id]) {
            return;
          }
          set[id] = true;
          merged++;
          if (item.parentId && byId[item.parentId]) {
            set[item.parentId] = true;
          }
        });
        let next = [];
        _this.catalog.forEach(function (item) {
          if (set[item.id]) {
            next.push(item.id);
          }
        });
        _this.$set(_this.draft, role.code, next);
      });
      _this.$message.success(merged ? '已并入 ' + merged + ' 项，保存后生效' : '新增资源已在本机构调整中');
    },
    formatTime: function (value) {
      let d = new Date(value);
      if (isNaN(d.getTime())) {
        return String(value);
      }
      let pad = function (n) { return n < 10 ? '0' + n : '' + n; };
      return d.getFullYear() + '-' + pad(d.getMonth() + 1) + '-' + pad(d.getDate())
          + ' ' + pad(d.getHours()) + ':' + pad(d.getMinutes());
    },
    checked: function (role, id) {
      let list = this.draft[role.code] || [];
      return list.indexOf(id) >= 0;
    },
    locked: function (role, id) {
      return (role.lockedIds || []).indexOf(id) >= 0;
    },
    differs: function (role, id) {
      let now = (this.draft[role.code] || []).indexOf(id) >= 0;
      let origin = (this.defaults[role.code] || []).indexOf(id) >= 0;
      return now !== origin;
    },
    roleDiffers: function (role) {
      return !sameIds(this.draft[role.code], this.defaults[role.code]);
    },
    resetColumn: function (role) {
      if (role.fixed) {
        return;
      }
      this.$set(this.draft, role.code, (this.defaults[role.code] || []).slice());
    },
    toggle: function (role, row, val) {
      if (role.fixed || this.locked(role, row.id)) {
        return;
      }
      let list = (this.draft[role.code] || []).slice();
      let set = {};
      list.forEach(function (id) { set[id] = true; });
      if (val) {
        set[row.id] = true;
        if (row.parentId) {
          set[row.parentId] = true;
        }
      } else {
        delete set[row.id];
        if (row.kind === 'MENU') {
          this.catalog.forEach(function (item) {
            if (item.parentId === row.id) {
              delete set[item.id];
            }
          });
        }
      }
      let next = [];
      this.catalog.forEach(function (item) {
        if (set[item.id]) {
          next.push(item.id);
        }
      });
      this.$set(this.draft, role.code, next);
    },
    savePolicy: function () {
      let _this = this;
      let roles = {};
      _this.roles.forEach(function (role) {
        if (!role.fixed && _this.roleDiffers(role)) {
          roles[role.code] = _this.draft[role.code] || [];
        }
      });
      _this.$ajax.put(process.env.VUE_APP_SERVER + '/system/tenant-ui-policy', { roles: roles }).then(function (response) {
        let resp = response.data || {};
        if (resp.success === false) {
          _this.$message.error(resp.message || '保存失败');
          return;
        }
        _this.$message.success(resp.message || '已保存');
        _this.loadPolicy();
      });
    },
    resetPolicy: function () {
      let _this = this;
      _this.$confirm('恢复后，本机构的调整全部删除，各角色回到平台默认。', '全部恢复默认', { type: 'warning' }).then(function () {
        _this.$ajax.post(process.env.VUE_APP_SERVER + '/system/tenant-ui-policy/reset').then(function (response) {
          let resp = response.data || {};
          if (resp.success === false) {
            _this.$message.error(resp.message || '恢复失败');
            return;
          }
          _this.$message.success(resp.message || '已恢复默认');
          _this.loadPolicy();
        });
      }).catch(function () {});
    }
  }
};
</script>
