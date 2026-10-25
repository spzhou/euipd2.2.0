<!--
 * Author: 周朔鹏
 * Date: 2026-10-11
 * Email: 391902958@qq.com
 * Website: https://www.euipd.com
 * CBB 技术文档：概要、正文、定义、物料清单、开发档案、组成特性。embedded 为真时直接嵌入页面，否则以对话框打开。
-->

<template>
  <component :is="embedded ? 'div' : 'el-dialog'" v-bind="wrapperAttrs" v-on="wrapperListeners">
    <div v-if="embedded" class="cbb-tech-doc__title">
      <span>{{ cbb.cbbName }}</span>
      <el-tag v-if="inactive" :type="cbb.status === CBB_STATUS.DISCARD.key ? 'danger' : 'warning'" size="small">
        {{ statusLabel(cbb.status) }}
      </el-tag>
    </div>
    <div v-else slot="title" class="cbb-tech-doc__title">
      <span>CBB 技术文档：{{ cbb.cbbName }}</span>
      <el-tag v-if="inactive" :type="cbb.status === CBB_STATUS.DISCARD.key ? 'danger' : 'warning'" size="small">
        {{ statusLabel(cbb.status) }}
      </el-tag>
    </div>

    <div v-loading="loading" class="cbb-tech-doc">
      <el-alert v-if="inactive" :closable="false" show-icon
                :type="cbb.status === CBB_STATUS.DISCARD.key ? 'error' : 'warning'"
                :title="inactiveHint"></el-alert>
      <el-tabs v-model="activeTab">
        <el-tab-pane label="概要" name="summary">
          <table class="cbb-tech-doc__table">
            <tr><th>CBB 编号</th><td>{{ cbb.id }}</td><th>类型</th><td>{{ typeLabel }}</td></tr>
            <tr><th>发布版本</th><td>{{ cbb.devVersion }}</td><th>状态</th><td>{{ statusLabel(cbb.status) }}</td></tr>
            <tr><th>发布时间</th><td>{{ cbb.releaseTime | dateFormat }}</td><th>承载特性编号</th><td>{{ cbb.characterId }}</td></tr>
            <tr><th>产品经理</th><td>{{ cbb.productManagerName }}</td><th>系统工程师</th><td>{{ cbb.systemManagerName }}</td></tr>
            <tr v-if="doc.sourceSubprojectId">
              <th>来源子项目</th><td colspan="3">{{ doc.sourceSubprojectName }}（{{ doc.sourceSubprojectId }}）</td>
            </tr>
            <tr><th>简介</th><td colspan="3">{{ cbb.cbbIntroduce }}</td></tr>
            <template v-if="cbb.status === CBB_STATUS.DISCARD.key">
              <tr><th>废弃人</th><td>{{ cbb.discardUserName }}</td><th>废弃时间</th><td>{{ cbb.discardTime | dateFormat }}</td></tr>
              <tr><th>废弃原因</th><td colspan="3">{{ cbb.discardReason }}</td></tr>
            </template>
          </table>
          <div v-if="doc.integratedCbbs && doc.integratedCbbs.length > 0" class="cbb-tech-doc__block">
            <div class="cbb-tech-doc__label">集成的 CBB</div>
            <div v-for="item in doc.integratedCbbs" :key="item.id" class="cbb-tech-doc__link">
              <a href="javascript:void(0);" @click="openNested(item.id)">{{ item.cbbName }}</a>
              <span class="cbb-tech-doc__muted">（{{ item.id }}，{{ statusLabel(item.status) }}）</span>
            </div>
          </div>
        </el-tab-pane>

        <el-tab-pane label="正文" name="content">
          <div v-for="section in sections" :key="section.field" class="cbb-tech-doc__block">
            <div class="cbb-tech-doc__label">{{ section.label }}</div>
            <div class="cbb-tech-doc__rich" v-rich-html="cbb[section.field]"></div>
          </div>
        </el-tab-pane>

        <el-tab-pane label="定义" name="define">
          <div v-if="carrier.id">
            <table class="cbb-tech-doc__table">
              <tr><th>承载记录</th><td>{{ carrier.characterName }}</td><th>编号</th><td>{{ carrier.id }}</td></tr>
              <tr><th>特性编号</th><td>{{ carrier.iterationVersion | iterationFormat }}</td><th>状态</th><td>{{ characterStatusLabel(carrier.status) }}</td></tr>
            </table>
            <p v-if="integrated" class="cbb-tech-doc__muted">集成级 CBB 的定义记录正文即“正文”页签内容；组成特性见“组成特性”页签。</p>
            <template v-else>
              <div v-for="section in sections" :key="section.field" class="cbb-tech-doc__block">
                <div class="cbb-tech-doc__label">{{ section.label }}</div>
                <div class="cbb-tech-doc__rich" v-rich-html="carrier[section.field]"></div>
              </div>
            </template>
          </div>
          <p v-else class="cbb-tech-doc__muted">无承载记录。</p>
        </el-tab-pane>

        <el-tab-pane label="物料清单" name="bom">
          <el-table :data="doc.bomList" border stripe size="small" empty-text="无物料清单">
            <el-table-column prop="bomCode" label="物料编码" width="140" align="center"></el-table-column>
            <el-table-column prop="name" label="名称" min-width="160" show-overflow-tooltip></el-table-column>
            <el-table-column prop="model" label="型号" min-width="120" show-overflow-tooltip></el-table-column>
            <el-table-column prop="unit" label="单位" width="70" align="center"></el-table-column>
            <el-table-column prop="amount" label="数量" width="80" align="center"></el-table-column>
            <el-table-column prop="itemType" label="类别" width="90" align="center" :formatter="itemTypeFormat"></el-table-column>
            <el-table-column prop="characterName" label="所属特性" min-width="140" show-overflow-tooltip></el-table-column>
          </el-table>
        </el-tab-pane>

        <el-tab-pane v-if="showArchive" label="开发档案" name="archive">
          <el-tree :data="archiveTree" node-key="id" :default-expand-all="true" :expand-on-click-node="false"
                   empty-text="无开发档案">
            <span class="custom-tree-node" slot-scope="{ data }">
              <span>{{ docTypeLabel(data.status) }}：{{ data.label }}</span>
              <el-button v-if="data.status === DOC_TYPE.FILE.key && data.obj && data.obj.authStatus" type="text"
                         style="margin-left: 16px;" @click.stop="downloadFile(data.obj)">下载</el-button>
            </span>
          </el-tree>
        </el-tab-pane>

        <el-tab-pane v-if="integrated" label="组成特性" name="components">
          <p v-if="!doc.components || doc.components.length === 0" class="cbb-tech-doc__muted">来源子项目内无已归档特性。</p>
          <el-collapse v-else>
            <el-collapse-item v-for="item in doc.components" :key="item.id" :name="item.id">
              <template slot="title">
                {{ item.characterName }}<span class="cbb-tech-doc__muted">（{{ item.id }}，{{ characterStatusLabel(item.status) }}）</span>
              </template>
              <div v-for="section in sections" :key="section.field" class="cbb-tech-doc__block">
                <div class="cbb-tech-doc__label">{{ section.label }}</div>
                <div class="cbb-tech-doc__rich" v-rich-html="item[section.field]"></div>
              </div>
            </el-collapse-item>
          </el-collapse>
        </el-tab-pane>
      </el-tabs>
    </div>

    <cbb-tech-doc-dialog v-if="nested.visible" :cbb-id="nested.cbbId" :visible.sync="nested.visible"></cbb-tech-doc-dialog>
  </component>
</template>

<script>
import Swal from "sweetalert2";
import {optionKV} from "@/filter/filter";

const SECTIONS = [
  {field: 'functionDescription', label: '特性描述'},
  {field: 'workCondition', label: '生效条件/应用范围'},
  {field: 'inputLogicalDesc', label: '输入逻辑/使用条件'},
  {field: 'functionLogicalDesc', label: '功能逻辑/功能要求'},
  {field: 'outputLogicalDesc', label: '输出逻辑/交付指标'},
  {field: 'testMethod', label: '验证方法/测试方法'},
];

export default {
  name: "cbb-tech-doc-dialog",

  props: {
    cbbId: {
      default: null,
    },
    visible: {
      type: Boolean,
      default: false,
    },
    embedded: {
      type: Boolean,
      default: false,
    },
    showArchive: {
      type: Boolean,
      default: true,
    },
  },

  data() {
    return {
      loginUser: {},
      loginCustomer: {},
      loading: false,
      loadedId: null,
      activeTab: 'summary',
      doc: {},
      archiveTree: [],
      nested: {visible: false, cbbId: null},
      sections: SECTIONS,
      CBB_STATUS: CBB_STATUS,
      DOC_TYPE: DOC_TYPE,
    };
  },

  computed: {
    cbb() {
      return this.doc.cbb || {};
    },
    carrier() {
      return this.doc.carrier || {};
    },
    integrated() {
      return this.cbb.type === DOC_TYPE.PROJECT.key;
    },
    typeLabel() {
      return this.integrated ? '集成级' : '特性级';
    },
    inactive() {
      return this.cbb.status === CBB_STATUS.DISABLED.key || this.cbb.status === CBB_STATUS.DISCARD.key;
    },
    inactiveHint() {
      return this.cbb.status === CBB_STATUS.DISCARD.key
          ? '该 CBB 已废弃，不再作为选用候选；已有引用不变，技术文档照常可查看。'
          : '该 CBB 已停用，不再作为选用候选；已有引用不变，技术文档照常可查看。';
    },
    wrapperAttrs() {
      if (this.embedded) {
        return {};
      }
      return {visible: this.visible, width: '30cm', appendToBody: true, closeOnClickModal: false};
    },
    wrapperListeners() {
      if (this.embedded) {
        return {};
      }
      return {'update:visible': (v) => this.$emit('update:visible', v)};
    },
  },

  watch: {
    cbbId() {
      this.loadIfNeeded();
    },
    visible() {
      this.loadIfNeeded();
    },
  },

  mounted() {
    this.getLoginUser();
    this.loadIfNeeded();
  },

  methods: {
    getLoginUser() {
      if (Tool.getLoginUser().userGroup === LOGIN_USER_GROUP.CUSTOMER.key) {
        this.loginUser = Tool.getLoginUser().adminUser;
        this.loginCustomer = Tool.getLoginUser();
      } else {
        this.loginUser = Tool.getLoginUser();
        this.loginCustomer = Tool.getLoginUser().customer;
      }
    },
    loadIfNeeded() {
      if (!this.cbbId || (!this.embedded && !this.visible) || this.loadedId === this.cbbId) {
        return;
      }
      this.loadedId = this.cbbId;
      this.activeTab = 'summary';
      this.getTechDoc();
      if (this.showArchive) {
        this.getArchiveTree();
      }
    },
    getTechDoc() {
      let _this = this;
      _this.loading = true;
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/cbb/tech-doc/get/' + _this.cbbId).then((response) => {
        _this.loading = false;
        let resp = response.data;
        if (resp.success) {
          _this.doc = resp.content || {};
        } else {
          _this.loadedId = null;
          Toast.warning(resp.message, Swal);
        }
      }).catch(() => {
        _this.loading = false;
        _this.loadedId = null;
      });
    },
    getArchiveTree() {
      let _this = this;
      let formData = new window.FormData();
      formData.append('loginUserId', _this.loginUser.customerUserId);
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/file/rdms/getCbbArchiveTree/' + _this.cbbId, formData).then((response) => {
        let resp = response.data;
        if (resp.success) {
          _this.archiveTree = resp.content || [];
        }
      });
    },
    openNested(cbbId) {
      this.nested = {visible: true, cbbId: cbbId};
    },
    statusLabel(status) {
      return optionKV(CBB_STATUS, status);
    },
    characterStatusLabel(status) {
      return optionKV(CHARACTER_STATUS, status);
    },
    docTypeLabel(status) {
      return optionKV(DOC_TYPE, status);
    },
    itemTypeFormat(row) {
      return optionKV(BOM_ITEM_TYPE, row.itemType);
    },
    downloadFile(file) {
      let _this = this;
      let formData = new window.FormData();
      formData.append('fileFullName', file.path);
      formData.append('customer', _this.loginCustomer.id);
      formData.append('user', _this.loginUser.customerUserId);
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/file/rdms/oss-get-signed-url', formData).then((response) => {
        Tool.openOssSignedUrl(response.data, file.suffix);
      });
    },
  },
}
</script>

<style lang="scss" scoped>
.cbb-tech-doc {
  min-height: 300px;
}
.cbb-tech-doc__title {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 18px;
}
.cbb-tech-doc__table {
  width: 100%;
  border-collapse: collapse;
  margin-bottom: 12px;
  th, td {
    border: 1px solid #dcdfe6;
    padding: 6px 10px;
    text-align: left;
  }
  th {
    width: 120px;
    background: #f5f7fa;
    font-weight: 500;
  }
}
.cbb-tech-doc__block {
  margin: 10px 0;
}
.cbb-tech-doc__label {
  font-weight: 600;
  margin-bottom: 4px;
}
.cbb-tech-doc__rich {
  padding: 4px 12px;
  border-left: 3px solid #e4e7ed;
}
.cbb-tech-doc__link {
  line-height: 26px;
  margin-left: 12px;
}
.cbb-tech-doc__muted {
  color: #909399;
  margin-left: 6px;
}
</style>
