<!--
 * Author: 周朔鹏
 * Date: 2026-10-07
 * Email: 391902958@qq.com
 * Website: https://www.euipd.com
-->
<template>
  <div style="display: flex; gap: 12px; align-items: flex-start; flex-wrap: wrap;">
    <div class="flow-list">
      <div v-for="group in groups" :key="group.title">
        <div class="flow-group">{{ group.title }}</div>
        <div v-for="item in group.items" :key="item.code"
             :class="['flow-item', { active: item.code === code }]"
             v-on:click="select(item.code)">
          {{ item.name }}<span class="flow-code">{{ item.code }}</span>
        </div>
      </div>
    </div>
    <div style="flex: 1 1 480px; min-width: 0;">
      <div style="display: flex; gap: 8px; align-items: center; margin-bottom: 8px; flex-wrap: wrap;">
        <span style="font-weight: 600;">{{ title }}</span>
        <el-radio-group v-if="business && executable" v-model="view" size="small" v-on:change="paint">
          <el-radio-button label="business">业务流程图</el-radio-button>
          <el-radio-button label="definition">执行定义</el-radio-button>
        </el-radio-group>
        <el-button size="small" v-on:click="fit">适应画布</el-button>
        <el-button size="small" v-on:click="download">下载 SVG</el-button>
        <span style="color: #888;">{{ caption }}</span>
      </div>
      <div ref="canvas" class="flow-canvas"></div>
      <div v-if="note" style="color: #666; margin-top: 8px;">{{ note }}</div>
      <div style="color: #999; margin-top: 4px; font-size: 12px;">滚轮缩放，拖动平移。绿色为正常收口，红色为驳回、关闭或废弃。</div>
    </div>
  </div>
</template>

<script>
import NavigatedViewer from 'bpmn-js/dist/bpmn-navigated-viewer.production.min.js';
import 'bpmn-js/dist/assets/diagram-js.css';
import 'bpmn-js/dist/assets/bpmn-js.css';
import 'bpmn-js/dist/assets/bpmn-font/css/bpmn-embedded.css';
import { BUSINESS_FLOWS, businessFlow, businessXml } from './flow-catalog';
import { withLayout } from './flow-layout';
import { downloadSvg } from './flow-svg';

export default {
  name: 'FlowGallery',
  data: function () {
    return {
      bizCodes: [],
      code: 'MAINLINE',
      view: 'business',
      caption: '',
      note: '',
      viewer: null,
      seq: 0
    };
  },
  computed: {
    business: function () {
      return businessFlow(this.code);
    },
    executable: function () {
      const code = this.code;
      return this.bizCodes.filter(function (item) { return item.code === code; })[0] || null;
    },
    title: function () {
      return (this.business && this.business.name) || (this.executable && this.executable.name) || this.code;
    },
    groups: function () {
      const mainline = {};
      BUSINESS_FLOWS.forEach(function (item) {
        mainline[item.code] = true;
      });
      return [
        { title: '全景', items: BUSINESS_FLOWS.filter(function (item) { return item.code === 'MAINLINE'; }) },
        { title: '平台流程', items: this.bizCodes.filter(function (item) { return !mainline[item.code]; }) },
        { title: '主线业务', items: this.bizCodes.filter(function (item) { return mainline[item.code]; }) }
      ];
    }
  },
  mounted: function () {
    this.viewer = new NavigatedViewer({ container: this.$refs.canvas });
    this.paint();
    const _this = this;
    this.onResize = function () {
      _this.fit();
    };
    window.addEventListener('resize', this.onResize);
    this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/flow/capability/list', {}).then(function (response) {
      const resp = response.data;
      const content = resp && resp.success !== false && resp.content;
      _this.bizCodes = (content && content.bizCodes) || [];
    });
  },
  beforeDestroy: function () {
    window.removeEventListener('resize', this.onResize);
    if (this.viewer) {
      this.viewer.destroy();
    }
  },
  methods: {
    select: function (code) {
      this.code = code;
      this.view = businessFlow(code) ? 'business' : 'definition';
      this.paint();
    },
    paint: function () {
      const seq = ++this.seq;
      if (this.view === 'business' && this.business) {
        this.caption = '整理自' + this.business.source + '，只读';
        this.note = this.business.note || '';
        this.show(businessXml(this.business), seq);
        return;
      }
      this.note = businessFlow(this.code)
        ? '主线业务共用“状态步骤”循环：每次写状态推进一步，到终态结束实例。状态能否迁移由服务端门禁判定，见业务流程图。'
        : '';
      this.loadDefinition(seq);
    },
    loadDefinition: function (seq) {
      const _this = this;
      this.caption = '正在读取流程定义';
      this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/flow/def/list', { bizCode: this.code }).then(function (response) {
        if (seq !== _this.seq) {
          return null;
        }
        const resp = response.data;
        const rows = (resp && resp.success !== false && resp.content) || [];
        const active = rows.filter(function (row) { return row.status === 'ACTIVE'; });
        const own = active.filter(function (row) { return row.customerId; })[0];
        const chosen = own || active[0];
        if (!chosen) {
          _this.caption = '尚无启用的流程定义';
          _this.viewer.clear();
          return null;
        }
        _this.caption = (own ? '本机构启用' : '平台默认') + '，第 ' + chosen.version + ' 版';
        return _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/flow/def/get', { id: chosen.id });
      }).then(function (response) {
        if (!response || seq !== _this.seq) {
          return;
        }
        const resp = response.data;
        if (!resp || resp.success === false) {
          _this.caption = (resp && resp.message) || '无法读取流程定义';
          return;
        }
        _this.show(withLayout((resp.content && resp.content.bpmnXml) || ''), seq);
      });
    },
    show: function (xml, seq) {
      const _this = this;
      this.viewer.importXML(xml).then(function () {
        if (seq === _this.seq) {
          _this.fit();
        }
      }).catch(function (error) {
        _this.caption = '流程图无法显示：' + (error && error.message ? error.message : error);
      });
    },
    fit: function () {
      if (this.viewer) {
        this.viewer.get('canvas').zoom('fit-viewport', 'auto');
      }
    },
    download: function () {
      const _this = this;
      this.viewer.saveSVG().then(function (result) {
        downloadSvg(result.svg, _this.title + (_this.view === 'business' ? '-业务流程图' : '-执行定义') + '.svg');
      });
    }
  }
};
</script>

<style scoped>
.flow-list {
  width: 200px;
  flex: none;
  border: 1px solid #dcdfe6;
  max-height: 660px;
  overflow-y: auto;
}
.flow-group {
  padding: 6px 10px;
  background: #f5f7fa;
  color: #909399;
  font-size: 12px;
}
.flow-item {
  padding: 7px 10px;
  cursor: pointer;
  border-top: 1px solid #f0f0f0;
}
.flow-item:hover {
  background: #f5f7fa;
}
.flow-item.active {
  color: #409eff;
  background: #ecf5ff;
}
.flow-code {
  margin-left: 6px;
  color: #aaa;
  font-size: 12px;
}
.flow-canvas {
  height: 600px;
  border: 1px solid #dcdfe6;
  background: #fff;
}
</style>
