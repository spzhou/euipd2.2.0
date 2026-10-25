<!--
 * Author: 周朔鹏
 * Date: 2026-10-12
 * Email: 391902958@qq.com
 * Website: https://www.euipd.com
-->

<!-- 从系统文件添加附件：我上传的、我下载的、我编写的、正文版本（导出为 PDF 或 Word） -->
<template>
  <el-dialog title="从系统文件添加附件" :visible="visible" width="900px" append-to-body top="8vh"
             @close="$emit('update:visible', false)">
    <div class="mail-att-picker">
      <div class="mail-att-picker__bar">
        <el-radio-group v-model="source" size="small" @change="switchSource">
          <el-radio-button v-for="s in sources" :key="s.key" :label="s.key">{{ s.name }}</el-radio-button>
        </el-radio-group>
        <el-input v-model="keyword" size="small" clearable prefix-icon="el-icon-search"
                  :placeholder="isRich ? '按文档标题搜索' : '按文件名搜索'"
                  style="width: 240px;" @keyup.enter.native="reload" @clear="reload"></el-input>
      </div>
      <div class="mail-att-picker__tip">
        {{ currentSource.tip }}
        <span v-if="isRich" class="mail-att-picker__format">
          导出格式
          <el-radio-group v-model="format" size="mini">
            <el-radio-button label="pdf">PDF</el-radio-button>
            <el-radio-button label="docx">Word</el-radio-button>
          </el-radio-group>
        </span>
      </div>

      <el-table v-if="!isRich" ref="table" :key="'f-' + source" :data="rows" v-loading="loading" size="small"
                height="380" row-key="fileId" empty-text="没有可添加的文件" @selection-change="onSelect">
        <el-table-column type="selection" width="42" :selectable="row => !attached[row.fileId]"
                         reserve-selection></el-table-column>
        <el-table-column label="文件名" min-width="260" show-overflow-tooltip>
          <template slot-scope="s">
            <i :class="icon(s.row.suffix).cls" :style="{ color: icon(s.row.suffix).color, marginRight: '6px' }"></i>{{ s.row.name }}
            <el-tag v-if="attached[s.row.fileId]" size="mini" type="info">已添加</el-tag>
          </template>
        </el-table-column>
        <el-table-column v-if="source === 'WRITTEN'" prop="origin" label="来源" width="100"></el-table-column>
        <el-table-column label="大小" width="90" align="right">
          <template slot-scope="s">{{ size(s.row.size) }}</template>
        </el-table-column>
        <el-table-column :label="currentSource.timeLabel" width="150" align="center">
          <template slot-scope="s">{{ s.row.fileTime | dateFormat }}</template>
        </el-table-column>
      </el-table>

      <el-table v-else ref="table" key="rich" :data="rows" v-loading="loading" size="small" height="380"
                row-key="versionId" empty-text="没有本人编辑保存的正文版本" @selection-change="onSelect">
        <el-table-column type="selection" width="42" reserve-selection></el-table-column>
        <el-table-column prop="title" label="文档" min-width="220" show-overflow-tooltip></el-table-column>
        <el-table-column prop="docTypeName" label="类型" width="110" show-overflow-tooltip></el-table-column>
        <el-table-column label="版本" width="140" show-overflow-tooltip>
          <template slot-scope="s">{{ s.row.versionLabel }} {{ s.row.kindText }}</template>
        </el-table-column>
        <el-table-column label="保存时间" width="150" align="center">
          <template slot-scope="s">{{ s.row.saveTime | dateFormat }}</template>
        </el-table-column>
      </el-table>

      <div class="mail-att-picker__foot">
        <span>已选 {{ selection.length }} 个</span>
        <el-pagination small layout="prev, pager, next" :current-page.sync="page" :page-size="pageSize"
                       :total="total" @current-change="load"></el-pagination>
      </div>
    </div>
    <span slot="footer">
      <el-button size="small" @click="$emit('update:visible', false)">取消</el-button>
      <el-button size="small" type="primary" :disabled="!selection.length" :loading="exporting" @click="confirm">
        {{ isRich ? '导出并添加' : '添加' }}
      </el-button>
    </span>
  </el-dialog>
</template>

<script>
import * as api from '@/shared/mail-api';

export default {
  name: 'mail-attachment-picker',
  props: {
    visible: { type: Boolean, default: false },
    attachedIds: { type: Array, default: () => [] }
  },

  data() {
    return {
      sources: [
        { key: 'UPLOADED', name: '我上传的', timeLabel: '上传时间', tip: '本人在系统中上传过的文件。' },
        { key: 'DOWNLOADED', name: '我下载的', timeLabel: '最近下载', tip: '本人下载过的他人文件。' },
        { key: 'WRITTEN', name: '我编写的', timeLabel: '上传时间',
          tip: '本人编写的需求、特性、质量、物料等业务记录上挂的附件。' },
        { key: 'RICH', name: '正文版本', timeLabel: '保存时间',
          tip: '本人编辑保存的富文本正文版本，按所选格式生成文件后添加。' }
      ],
      source: 'UPLOADED',
      format: 'pdf',
      keyword: '',
      rows: [],
      total: 0,
      page: 1,
      pageSize: 10,
      loading: false,
      exporting: false,
      selection: []
    };
  },

  computed: {
    isRich() {
      return this.source === 'RICH';
    },
    currentSource() {
      return this.sources.find(s => s.key === this.source) || this.sources[0];
    },
    attached() {
      const map = {};
      this.attachedIds.forEach(id => { map[id] = true; });
      return map;
    }
  },

  watch: {
    visible(v) {
      if (!v) return;
      this.selection = [];
      if (this.$refs.table) this.$refs.table.clearSelection();
      this.reload();
    }
  },

  methods: {
    icon(suffix) {
      return api.fileIcon(suffix);
    },
    size(n) {
      return api.fileSize(n);
    },
    switchSource() {
      this.selection = [];
      this.rows = [];
      this.reload();
    },
    reload() {
      this.page = 1;
      this.load();
    },
    load() {
      this.loading = true;
      const req = this.isRich
        ? api.listRichVersions(this.keyword, this.page, this.pageSize)
        : api.listAttachable(this.source, this.keyword, this.page, this.pageSize);
      req.then(p => {
        this.rows = (p && p.list) || [];
        this.total = (p && p.total) || 0;
      }).catch(e => this.$message.error(e.message)).finally(() => {
        this.loading = false;
      });
    },
    onSelect(list) {
      this.selection = list;
    },
    confirm() {
      if (!this.isRich) {
        this.done(this.selection.map(r => ({
          fileId: r.fileId, name: r.name, size: r.size, suffix: r.suffix, source: r.source
        })));
        return;
      }
      this.exporting = true;
      const out = [];
      const run = this.selection.reduce((chain, v) => chain.then(() =>
        api.saveVersionAsFile(v.versionId, this.format).then(f => {
          out.push({ fileId: f.id, name: f.name, size: f.size, suffix: f.suffix, source: 'UPLOADED' });
        })), Promise.resolve());
      run.then(() => this.done(out)).catch(e => {
        this.$message.error(e.message || '正文导出失败');
        if (out.length) this.done(out);
      }).finally(() => {
        this.exporting = false;
      });
    },
    done(list) {
      this.$emit('confirm', list);
      this.$emit('update:visible', false);
    }
  }
};
</script>

<style scoped>
.mail-att-picker__bar { display: flex; justify-content: space-between; align-items: center; }
.mail-att-picker__tip { display: flex; justify-content: space-between; align-items: center; min-height: 28px;
  margin: 8px 0; font-size: 12px; color: #909399; }
.mail-att-picker__format { color: #606266; }
.mail-att-picker__foot { display: flex; justify-content: space-between; align-items: center; margin-top: 8px;
  font-size: 12px; color: #606266; }
</style>
