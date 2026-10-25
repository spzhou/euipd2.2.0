<!--
 * Author: 周朔鹏
 * Date: 2026-10-06
 * Email: 391902958@qq.com
 * Website: https://www.euipd.com
-->
<template>
  <div>
    <div v-if="!fixedBizId" style="display: flex; gap: 8px; align-items: center; margin-bottom: 8px; flex-wrap: wrap;">
      <span>业务</span>
      <el-select v-model="bizCode" size="small" style="width: 180px;">
        <el-option v-for="item in bizCodes" :key="item.code" :label="item.name" :value="item.code"></el-option>
      </el-select>
      <span>业务主键</span>
      <el-input v-model="bizId" size="small" style="width: 180px;" v-on:keyup.enter.native="load"></el-input>
      <el-button size="small" type="primary" v-on:click="load">查看</el-button>
    </div>
    <div style="display: flex; align-items: center; gap: 16px; margin-bottom: 8px; flex-wrap: wrap; color: #666;">
      <span v-if="hint">{{ hint }}</span>
      <span v-if="loaded" class="flow-legend">
        <i class="flow-legend-box flow-legend-finished"></i>已完成
        <i class="flow-legend-box flow-legend-current"></i>当前
        <i class="flow-legend-box"></i>未到达
      </span>
      <el-button v-if="loaded" size="mini" v-on:click="fit">适应画布</el-button>
      <el-button v-if="loaded" size="mini" v-on:click="download">下载 SVG</el-button>
      <span v-if="loaded" style="font-size: 12px;">点击已完成或当前节点查看该步办理情况</span>
    </div>
    <div ref="canvas" class="flow-canvas"></div>
    <div style="margin-top: 12px; font-weight: 600;">办理记录</div>
    <el-table :data="history" size="small" border style="margin-top: 8px;" :row-class-name="rowClass"
        v-on:row-click="onRowClick">
      <el-table-column prop="nodeName" label="节点" min-width="120"></el-table-column>
      <el-table-column label="处理人" min-width="100">
        <template slot-scope="scope">{{ scope.row.assigneeName || scope.row.assigneeId || '' }}</template>
      </el-table-column>
      <el-table-column label="开始" min-width="150">
        <template slot-scope="scope">{{ formatTime(scope.row.startTime) }}</template>
      </el-table-column>
      <el-table-column label="结束" min-width="150">
        <template slot-scope="scope">{{ formatTime(scope.row.endTime) }}</template>
      </el-table-column>
      <el-table-column label="停留" width="100">
        <template slot-scope="scope">{{ formatDuration(scope.row.durationSeconds) }}</template>
      </el-table-column>
      <el-table-column label="结论" min-width="90">
        <template slot-scope="scope"><span :class="{ 'flow-raw-code': conclusionText(scope.row) === scope.row.resultStatus }">{{ conclusionText(scope.row) }}</span></template>
      </el-table-column>
      <el-table-column prop="opinion" label="意见" min-width="160" show-overflow-tooltip></el-table-column>
      <el-table-column label="当前" width="60">
        <template slot-scope="scope">{{ scope.row.current ? '是' : '' }}</template>
      </el-table-column>
    </el-table>

    <el-dialog :title="detailTitle" :visible.sync="detailVisible" width="640px" append-to-body>
      <el-table :data="detailRows" size="small" border>
        <el-table-column label="处理人" min-width="100">
          <template slot-scope="scope">{{ scope.row.assigneeName || scope.row.assigneeId || '' }}</template>
        </el-table-column>
        <el-table-column label="开始" min-width="150">
          <template slot-scope="scope">{{ formatTime(scope.row.startTime) }}</template>
        </el-table-column>
        <el-table-column label="结束" min-width="150">
          <template slot-scope="scope">{{ scope.row.current ? '办理中' : formatTime(scope.row.endTime) }}</template>
        </el-table-column>
        <el-table-column label="停留" width="100">
          <template slot-scope="scope">{{ formatDuration(scope.row.durationSeconds) }}</template>
        </el-table-column>
        <el-table-column label="结论" min-width="90">
          <template slot-scope="scope"><span :class="{ 'flow-raw-code': conclusionText(scope.row) === scope.row.resultStatus }">{{ conclusionText(scope.row) }}</span></template>
        </el-table-column>
      </el-table>
      <div v-for="(row, index) in detailRows" :key="index" style="margin-top: 8px;">
        <div v-if="row.opinion" style="white-space: pre-wrap; color: #333;">
          <span style="color: #909399;">第 {{ index + 1 }} 次意见：</span>{{ row.opinion }}
        </div>
      </div>
      <div v-if="!detailRows.some(function (row) { return row.opinion; })" style="margin-top: 8px; color: #909399;">
        该步没有记录意见
      </div>
    </el-dialog>
  </div>
</template>

<script>
import BpmnViewer from 'bpmn-js/dist/bpmn-viewer.production.min.js';
import 'bpmn-js/dist/assets/diagram-js.css';
import 'bpmn-js/dist/assets/bpmn-js.css';
import 'bpmn-js/dist/assets/bpmn-font/css/bpmn-embedded.css';
import { withLayout } from './flow-layout';
import { downloadSvg } from './flow-svg';
import { statusLabel } from './flow-catalog';

const STATUS_TEXT = {
  RUNNING: '运行中',
  FINISHED: '已结束',
  TERMINATED: '已终止',
  SUSPENDED: '已挂起'
};

/** 平台流程所经连线未命名时的结论文字。 */
const RESULT_TEXT = {
  SAVE: '已保存',
  APPLICATION: '已申请',
  SUBMIT: '已提交',
  HANDLING: '已办理',
  RECHECK: '待复核',
  COMPLETE: '已完成',
  COMPLETED: '已完成',
  APPROVED: '已批准',
  PRE_APPROVED: '已预批准',
  REJECT: '已拒绝',
  SUPPLEMENT: '已退回',
  PRE_COMPLETE: '复核通过',
  CANCEL: '已取消',
  ARCHIVED: '已归档',
  TESTING: '测试中',
  REFUSE: '已拒收'
};

export default {
  name: 'FlowViewer',
  props: {
    fixedBizCode: {
      type: String,
      default: ''
    },
    fixedBizId: {
      type: String,
      default: ''
    }
  },
  data: function () {
    return {
      bizCodes: [],
      bizCode: '',
      bizId: '',
      history: [],
      hint: '',
      loaded: false,
      seq: 0,
      selectedNodeId: '',
      detailVisible: false,
      detailTitle: '',
      detailRows: [],
      viewer: null
    };
  },
  mounted: function () {
    const _this = this;
    this.viewer = new BpmnViewer({ container: this.$refs.canvas });
    this.viewer.on('element.click', function (event) {
      _this.onElementClick(event.element);
    });
    this.onResize = function () {
      _this.fit();
    };
    window.addEventListener('resize', this.onResize);
    if (this.fixedBizId) {
      this.bizCode = this.fixedBizCode || 'FEE_APPLY';
      this.bizId = this.fixedBizId;
      this.load();
    } else {
      this.loadCapability();
    }
  },
  beforeDestroy: function () {
    window.removeEventListener('resize', this.onResize);
    if (this.viewer) {
      this.viewer.destroy();
    }
  },
  watch: {
    fixedBizId: function (value) {
      if (value) {
        this.bizId = value;
        this.load();
      }
    }
  },
  methods: {
    loadCapability: function () {
      const _this = this;
      this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/flow/capability/list', {}).then(function (response) {
        const resp = response.data;
        const content = resp && resp.content;
        _this.bizCodes = (content && content.bizCodes) || [];
        if (_this.bizCodes.length && !_this.bizCode) {
          _this.bizCode = _this.bizCodes[0].code;
        }
      });
    },
    /** 流程图与办理记录出自同一次查询，只请求一次，避免两边口径不一致。 */
    load: function () {
      const _this = this;
      if (!this.bizCode || !this.bizId) {
        this.hint = '请填写业务和业务主键';
        return;
      }
      const embedded = !!this.fixedBizId;
      const body = embedded ? { code: this.bizId } : { bizCode: this.bizCode, bizId: this.bizId };
      const diagramUrl = embedded ? '/customer/fee/diagram' : '/customer/flow/instance/diagram';
      const seq = ++this.seq;
      this.hint = '加载中';
      this.$ajax.post(process.env.VUE_APP_SERVER + diagramUrl, body).then(function (response) {
        if (seq !== _this.seq) {
          return;
        }
        const resp = response.data;
        if (!resp || resp.success === false) {
          _this.hint = (resp && resp.message) || '没有可显示的流程实例';
          _this.reset();
          return;
        }
        const diagram = resp.content || {};
        _this.history = diagram.nodeDetails || [];
        _this.hint = diagram.instanceStatus
          ? ('实例状态：' + (STATUS_TEXT[diagram.instanceStatus] || diagram.instanceStatus))
          : '';
        _this.paint(diagram);
      }).catch(function () {
        if (seq === _this.seq) {
          _this.hint = '查询失败';
          _this.reset();
        }
      });
    },
    reset: function () {
      this.history = [];
      this.loaded = false;
      this.selectedNodeId = '';
      if (this.viewer) {
        this.viewer.clear();
      }
    },
    paint: function (diagram) {
      const _this = this;
      // 平台默认定义多数不带图形信息，显示前按层次自动排布，排布结果不回写。
      const xml = withLayout(diagram.bpmnXml || '');
      this.viewer.importXML(xml).then(function () {
        const canvas = _this.viewer.get('canvas');
        const registry = _this.viewer.get('elementRegistry');
        const mark = function (id, marker) {
          if (registry.get(id)) {
            canvas.addMarker(id, marker);
          }
        };
        (diagram.finishedNodes || []).forEach(function (id) {
          mark(id, 'flow-finished');
        });
        (diagram.finishedFlows || []).forEach(function (id) {
          mark(id, 'flow-finished');
        });
        (diagram.currentNodes || []).forEach(function (id) {
          mark(id, 'flow-current');
        });
        _this.annotateCurrent(diagram.currentNodes || []);
        _this.loaded = true;
        _this.selectedNodeId = '';
        _this.fit();
      }).catch(function () {
        _this.hint = '流程图无法显示';
        _this.loaded = false;
      });
    },
    /** 当前节点下方标注处理人与停留时长。 */
    annotateCurrent: function (currentNodes) {
      const _this = this;
      const overlays = this.viewer.get('overlays');
      const registry = this.viewer.get('elementRegistry');
      currentNodes.forEach(function (id) {
        const shape = registry.get(id);
        if (!shape) {
          return;
        }
        const rows = _this.history.filter(function (row) {
          return row.nodeId === id && row.current;
        });
        if (!rows.length) {
          return;
        }
        const names = rows.map(function (row) {
          return row.assigneeName || row.assigneeId || '待指派';
        });
        const text = (names.length > 2 ? names.slice(0, 2).join('、') + ' 等 ' + names.length + ' 人' : names.join('、'))
          + ' · 停留 ' + _this.formatDuration(rows[0].durationSeconds);
        const el = document.createElement('div');
        el.className = 'flow-current-note';
        el.textContent = text;
        overlays.add(id, 'flow-note', {
          position: { bottom: -6, left: 0 },
          html: el
        });
      });
    },
    onElementClick: function (element) {
      if (!element || !element.businessObject) {
        return;
      }
      const id = element.id;
      const rows = this.history.filter(function (row) {
        return row.nodeId === id;
      });
      if (!rows.length) {
        return;
      }
      this.selectedNodeId = id;
      this.detailTitle = (element.businessObject.name || id) + ' · 办理情况';
      this.detailRows = rows;
      this.detailVisible = true;
    },
    onRowClick: function (row) {
      this.selectedNodeId = row.nodeId;
      const registry = this.viewer.get('elementRegistry');
      const shape = registry.get(row.nodeId);
      if (shape) {
        this.viewer.get('canvas').scrollToElement(shape);
      }
    },
    rowClass: function (scope) {
      return scope.row.nodeId === this.selectedNodeId ? 'flow-row-selected' : '';
    },
    fit: function () {
      if (this.viewer && this.loaded) {
        this.viewer.get('canvas').zoom('fit-viewport', 'auto');
      }
    },
    download: function () {
      const _this = this;
      this.viewer.saveSVG().then(function (result) {
        downloadSvg(result.svg, _this.bizCode + '-' + _this.bizId + '-流程进度.svg');
      });
    },
    /** 主线业务按办结后的状态显示中文名，其余业务显示所经连线的名称。 */
    conclusionText: function (row) {
      if (!row || row.current) {
        return '';
      }
      const label = statusLabel(this.bizCode, row.resultStatus);
      if (label) {
        return label;
      }
      return row.conclusion || RESULT_TEXT[row.resultStatus] || row.resultStatus || '';
    },
    formatTime: function (value) {
      if (!value) {
        return '';
      }
      const date = new Date(value);
      if (isNaN(date.getTime())) {
        return value;
      }
      return date.toLocaleString();
    },
    formatDuration: function (seconds) {
      if (seconds === null || seconds === undefined) {
        return '';
      }
      const total = Number(seconds);
      if (isNaN(total)) {
        return '';
      }
      if (total < 60) {
        return total + ' 秒';
      }
      if (total < 3600) {
        return Math.floor(total / 60) + ' 分';
      }
      if (total < 86400) {
        return Math.floor(total / 3600) + ' 时 ' + Math.floor((total % 3600) / 60) + ' 分';
      }
      return Math.floor(total / 86400) + ' 天 ' + Math.floor((total % 86400) / 3600) + ' 时';
    }
  }
};
</script>

<style scoped>
.flow-canvas {
  height: 480px;
  border: 1px solid #dcdfe6;
  background: #fff;
}
.flow-legend {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
}
.flow-legend-box {
  display: inline-block;
  width: 14px;
  height: 10px;
  border: 1px solid #333;
  background: #fff;
  margin-left: 8px;
}
.flow-legend-finished {
  border-color: #67c23a;
  background: #f0f9eb;
}
.flow-legend-current {
  border-color: #409eff;
  background: #ecf5ff;
}
.flow-raw-code {
  color: #909399;
  font-family: Menlo, Consolas, monospace;
  font-size: 12px;
}
</style>

<style>
.flow-finished:not(.djs-connection) .djs-visual > :nth-child(1) {
  stroke: #67c23a !important;
  fill: #f0f9eb !important;
}
.flow-current:not(.djs-connection) .djs-visual > :nth-child(1) {
  stroke: #409eff !important;
  stroke-width: 3px !important;
  fill: #ecf5ff !important;
}
.djs-connection.flow-finished .djs-visual > path {
  stroke: #67c23a !important;
}
.flow-finished .djs-hit, .flow-current .djs-hit {
  cursor: pointer;
}
.flow-current-note {
  white-space: nowrap;
  font-size: 11px;
  color: #409eff;
  background: rgba(236, 245, 255, 0.95);
  border: 1px solid #409eff;
  border-radius: 3px;
  padding: 1px 6px;
}
.flow-row-selected {
  background: #ecf5ff !important;
}
</style>
