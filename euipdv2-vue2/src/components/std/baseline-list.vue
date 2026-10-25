<!--
 * Author: 周朔鹏
 * Date: 2026-10-13
 * Email: 391902958@qq.com
 * Website: https://www.euipd.com
-->

<template>
  <div v-loading="loading">
    <el-table
        v-if="!baselineId"
        :data="baselines"
        border
        size="small"
        row-key="id"
        empty-text="尚无基线"
        style="width: 100%"
        @expand-change="onExpand">
      <el-table-column type="expand">
        <template slot-scope="props">
          <div style="padding: 0 30px;">
            <BaselineItemTable :items="itemsOf(props.row.id)" @open="openItem"></BaselineItemTable>
          </div>
        </template>
      </el-table-column>
      <el-table-column label="序号" align="center" width="50">
        <template slot-scope="scope">{{ scope.$index + 1 }}</template>
      </el-table-column>
      <el-table-column prop="baselineTypeName" label="类别" align="center" width="100"></el-table-column>
      <el-table-column prop="name" label="名称" align="center" min-width="220" show-overflow-tooltip></el-table-column>
      <el-table-column label="主开发版本" align="center" width="110">
        <template slot-scope="scope">{{ scope.row.devVersion || '—' }}</template>
      </el-table-column>
      <el-table-column label="生成时间" align="center" width="160">
        <template slot-scope="scope">{{ timeFormat(scope.row.createTime) }}</template>
      </el-table-column>
      <el-table-column prop="creatorName" label="操作人" align="center" width="100"></el-table-column>
    </el-table>
    <div v-else>
      <div v-if="single" style="margin-bottom: 8px;">
        <strong>{{ single.name }}</strong>
        <small style="margin-left: 12px;">{{ single.baselineTypeName }}，生成于 {{ timeFormat(single.createTime) }}</small>
      </div>
      <BaselineItemTable :items="itemsOf(baselineId)" @open="openItem"></BaselineItemTable>
    </div>

    <el-dialog :visible.sync="detailVisible" :title="detailTitle" width="70%" append-to-body :close-on-click-modal="false">
      <div v-if="detailType === 'CHARACTER_VERSION'">
        <el-table :data="detailVersions" border size="mini" style="width: 100%">
          <el-table-column prop="versionLabel" label="特性版本" align="center" min-width="140"></el-table-column>
          <el-table-column prop="contentVersionLabel" label="内容版本" align="center" min-width="80"></el-table-column>
          <el-table-column prop="sourceName" label="产生方式" align="center" min-width="90"></el-table-column>
          <el-table-column label="时间" align="center" min-width="140">
            <template slot-scope="scope">{{ timeFormat(scope.row.createTime) }}</template>
          </el-table-column>
          <el-table-column prop="creatorName" label="操作人" align="center" min-width="80"></el-table-column>
          <el-table-column label="说明" align="center" min-width="110">
            <template slot-scope="scope">
              <el-tag v-if="scope.row.id === detailItemId" size="mini" type="success">本基线所含版本</el-tag>
            </template>
          </el-table-column>
        </el-table>
      </div>
      <div v-else-if="detailType === 'BOM_SNAPSHOT'">
        <el-table :data="detailBomItems" border size="mini" style="width: 100%" empty-text="清单为空">
          <el-table-column label="序号" align="center" width="50">
            <template slot-scope="scope">{{ scope.$index + 1 }}</template>
          </el-table-column>
          <el-table-column label="类别" align="center" width="70">
            <template slot-scope="scope">{{ scope.row.itemType === 'CBB' ? '软件（CBB）' : '物料' }}</template>
          </el-table-column>
          <el-table-column prop="bomCode" label="物料编码" align="center" show-overflow-tooltip></el-table-column>
          <el-table-column prop="name" label="名称" align="center" show-overflow-tooltip></el-table-column>
          <el-table-column prop="model" label="型号" align="center" show-overflow-tooltip></el-table-column>
          <el-table-column prop="amount" label="数量" align="center" width="70"></el-table-column>
          <el-table-column prop="unit" label="单位" align="center" width="70"></el-table-column>
          <el-table-column prop="characterName" label="所属特性" align="center" show-overflow-tooltip></el-table-column>
        </el-table>
      </div>
      <div v-else-if="detailType === 'CBB'">
        <CbbDetailInfoPage
            v-if="detailVisible"
            :key="detailItemId"
            :cbb-id="detailItemId"
            :doc-type="DOC_TYPE.CHARACTER.key">
        </CbbDetailInfoPage>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import Swal from "sweetalert2";
import { dateFormat } from "@/filter/filter";
import CbbDetailInfoPage from "@/components/rdms/cbb-detail-info-page.vue";

const BaselineItemTable = {
  name: "baseline-item-table",
  props: { items: { type: Array, default: () => [] } },
  render(h) {
    return h('el-table', {
      props: { data: this.items, border: true, size: 'mini', emptyText: '无条目' },
      style: { width: '100%' },
    }, [
      h('el-table-column', { props: { label: '序号', align: 'center', width: 50 },
        scopedSlots: { default: (scope) => String(scope.$index + 1) } }),
      h('el-table-column', { props: { prop: 'itemTypeName', label: '类别', align: 'center', width: 100 } }),
      h('el-table-column', { props: { prop: 'label', label: '内容', align: 'left', minWidth: 260, showOverflowTooltip: true } }),
      h('el-table-column', { props: { label: '操作', align: 'center', width: 90 },
        scopedSlots: {
          default: (scope) => h('el-button', {
            props: { type: 'text', size: 'mini' },
            on: { click: () => this.$emit('open', scope.row) },
          }, scope.row.itemType === 'FILE' ? '下载' : '打开'),
        } }),
    ]);
  },
};

/**
 * 基线列表：按所属对象列出基线，展开查看条目；传入 baselineId 时只显示该基线的条目。
 * 条目可逐项打开：特性版本、物料清单快照、CBB 详情，交付文件直接下载。
 */
export default {
  name: "baseline-list",
  components: { BaselineItemTable, CbbDetailInfoPage },

  props: {
    ownerType: { type: String, default: 'PROJECT' },
    ownerId: { default: null },
    baselineId: { default: null },
  },

  data() {
    return {
      loading: false,
      baselines: [],
      items: {},
      single: null,
      loginUser: {},
      loginCustomer: {},
      detailVisible: false,
      detailTitle: '',
      detailType: '',
      detailItemId: '',
      detailVersions: [],
      detailBomItems: [],
      DOC_TYPE: DOC_TYPE,
    };
  },

  mounted() {
    const login = Tool.getLoginUser();
    if (login.userGroup === LOGIN_USER_GROUP.CUSTOMER.key) {
      this.loginUser = login.adminUser;
      this.loginCustomer = login;
    } else {
      this.loginUser = login;
      this.loginCustomer = login.customer;
    }
    if (this.baselineId) {
      this.loadItems(this.baselineId);
    } else {
      this.list();
    }
  },

  methods: {
    list() {
      let _this = this;
      if (!_this.ownerId) {
        return;
      }
      _this.loading = true;
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/project/baseline/list', {
        ownerType: _this.ownerType,
        ownerId: _this.ownerId,
      }).then((response) => {
        _this.loading = false;
        let resp = response.data;
        if (resp.success) {
          _this.baselines = resp.content || [];
        } else {
          Toast.warning(resp.message, Swal);
        }
      }).catch(() => {
        _this.loading = false;
      });
    },

    onExpand(row, expanded) {
      if (Array.isArray(expanded) ? expanded.indexOf(row) >= 0 : expanded) {
        this.loadItems(row.id);
      }
    },

    loadItems(id) {
      let _this = this;
      _this.$ajax.get(process.env.VUE_APP_SERVER + '/customer/project/baseline/get/' + id).then((response) => {
        let resp = response.data;
        if (resp.success) {
          _this.$set(_this.items, id, resp.content.items || []);
          if (_this.baselineId === id) {
            _this.single = resp.content;
          }
        } else {
          Toast.warning(resp.message, Swal);
        }
      });
    },

    itemsOf(id) {
      return this.items[id] || [];
    },

    openItem(item) {
      let _this = this;
      _this.detailItemId = item.itemId;
      _this.detailTitle = item.label;
      if (item.itemType === 'FILE') {
        _this.download(item);
        return;
      }
      if (item.itemType === 'CHARACTER_VERSION') {
        if (!item.characterId) {
          Toast.warning('特性版本记录不存在', Swal);
          return;
        }
        _this.$ajax.get(process.env.VUE_APP_SERVER + '/customer/character/version/list/' + item.characterId).then((response) => {
          let resp = response.data;
          if (resp.success) {
            _this.detailVersions = resp.content || [];
            _this.detailType = item.itemType;
            _this.detailVisible = true;
          } else {
            Toast.warning(resp.message, Swal);
          }
        });
        return;
      }
      if (item.itemType === 'BOM_SNAPSHOT') {
        _this.$ajax.get(process.env.VUE_APP_SERVER + '/customer/material/bom-snapshot/get/' + item.itemId).then((response) => {
          let resp = response.data;
          if (resp.success) {
            _this.detailBomItems = resp.content.items || [];
            _this.detailType = item.itemType;
            _this.detailVisible = true;
          } else {
            Toast.warning(resp.message, Swal);
          }
        });
        return;
      }
      _this.detailType = item.itemType;
      _this.detailVisible = true;
    },

    download(item) {
      let _this = this;
      if (!item.path) {
        Toast.warning('文件不存在', Swal);
        return;
      }
      let formData = new window.FormData();
      formData.append('fileFullName', item.path);
      formData.append('customer', _this.loginCustomer.id);
      formData.append('user', _this.loginUser.customerUserId);
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/file/rdms/oss-get-signed-url', formData).then((response) => {
        Tool.openOssSignedUrl(response.data, item.suffix);
      });
    },

    timeFormat(t) {
      return t ? dateFormat(t) : '';
    },
  },
};
</script>
