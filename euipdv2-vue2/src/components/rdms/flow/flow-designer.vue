<!--
 * Author: 周朔鹏
 * Date: 2026-10-06
 * Email: 391902958@qq.com
 * Website: https://www.euipd.com
-->
<template>
  <div>
    <div style="display: flex; gap: 8px; align-items: center; margin-bottom: 8px; flex-wrap: wrap;">
      <span>业务</span>
      <el-select v-model="bizCode" size="small" style="width: 180px;" v-on:change="onBizChange">
        <el-option v-for="item in bizCodes" :key="item.code" :label="item.name" :value="item.code"></el-option>
      </el-select>
      <span style="color: #888;">流程标识 {{ processKey || '—' }}</span>
      <el-button size="small" v-on:click="newBlank">空白图</el-button>
      <el-button size="small" v-on:click="saveDraft">保存草稿</el-button>
      <el-button size="small" v-on:click="validate">校验</el-button>
      <el-button size="small" type="primary" v-on:click="publish">发布</el-button>
      <el-button size="small" v-on:click="fit">适应画布</el-button>
      <el-button size="small" v-on:click="importFile">导入 XML</el-button>
      <el-button size="small" v-on:click="exportFile">导出 XML</el-button>
      <el-button size="small" v-on:click="downloadSvg">下载 SVG</el-button>
      <input ref="file" type="file" accept=".xml,.bpmn,.bpmn20.xml" style="display: none;" v-on:change="onFileChosen">
    </div>
    <div style="color: #666; margin-bottom: 8px;">{{ hint }}</div>
    <div style="display: flex; gap: 12px; align-items: flex-start;">
      <div ref="canvas" class="flow-canvas"></div>
      <div style="width: 320px; flex: none;">
        <div v-if="flowForm">
          <div style="font-weight: 600; margin-bottom: 8px;">连线 {{ flowForm.id }}</div>
          <div class="flow-label">名称（显示为该步结论，如“通过”“驳回”）</div>
          <el-input v-model="flowForm.name" size="small" v-on:change="applyFlowName"></el-input>
          <div class="flow-label">条件表达式，如 ${approveResult=='YES'}</div>
          <el-input v-model="flowForm.condition" size="small" type="textarea" :rows="2" placeholder="默认分支留空"
              :disabled="flowForm.isDefault" v-on:change="applyFlowCondition"></el-input>
          <el-checkbox v-if="flowForm.fromGateway" v-model="flowForm.isDefault" style="margin-top: 8px;"
              v-on:change="applyFlowDefault">设为网关默认分支</el-checkbox>
          <div style="color: #888; font-size: 12px; margin-top: 8px;">
            排他网关的每条非默认出口都应当有条件表达式；默认分支在其他条件都不成立时走。
          </div>
        </div>
        <div v-else>
        <div style="font-weight: 600; margin-bottom: 8px;">用户任务</div>
        <el-select v-model="selectedId" size="small" placeholder="选择任务" style="width: 100%; margin-bottom: 8px;" v-on:change="onSelect">
          <el-option v-for="task in tasks" :key="task.id" :label="task.label" :value="task.id"></el-option>
        </el-select>
        <div v-if="!form" style="color: #888;">选中用户任务后，从登记清单选择处理人解析器和领域动作；选中连线可以编辑名称、条件与默认分支。双击元素可改名；Delete 删除，Ctrl+Z 撤销。</div>
        <div v-else>
          <div class="flow-label">处理人解析器</div>
          <el-select v-model="form.resolver" size="small" clearable filterable style="width: 100%;">
            <el-option v-for="item in resolvers" :key="item.code" :label="item.name" :value="item.code"></el-option>
          </el-select>
          <div class="flow-label">回退解析器</div>
          <el-select v-model="form.fallback" size="small" clearable filterable style="width: 100%;">
            <el-option v-for="item in resolvers" :key="'f-' + item.code" :label="item.name" :value="item.code"></el-option>
          </el-select>
          <div class="flow-label">找不到处理人时的拒绝码</div>
          <el-input v-model="form.denyCode" size="small"></el-input>
          <div v-if="needsRole" class="flow-label">机构角色</div>
          <el-select v-if="needsRole" v-model="form.roleCode" size="small" clearable filterable style="width: 100%;">
            <el-option v-for="item in roleCodes" :key="item.code" :label="item.name" :value="item.code"></el-option>
          </el-select>
          <div v-if="needsUser" class="flow-label">指定成员标识</div>
          <el-input v-if="needsUser" v-model="form.userId" size="small"></el-input>
          <div class="flow-label">表单</div>
          <el-select v-model="form.formCode" size="small" clearable filterable placeholder="可空" style="width: 100%;">
            <el-option v-for="item in forms" :key="item.code" :label="item.name" :value="item.code"></el-option>
          </el-select>
          <div class="flow-label">时限时长</div>
          <el-input v-model="form.timerDuration" size="small" placeholder="ISO-8601，如 P3D，可空"></el-input>
          <div class="flow-label">时限动作</div>
          <el-select v-model="form.timerAction" size="small" clearable style="width: 100%;">
            <el-option label="提醒" value="NOTIFY"></el-option>
            <el-option label="转交回退处理人" value="REASSIGN"></el-option>
            <el-option label="自动完成" value="AUTO_COMPLETE"></el-option>
          </el-select>
          <el-checkbox v-model="form.timerRepeat" style="margin-top: 6px;">重复提醒</el-checkbox>
          <div class="flow-label">守卫来源状态，逗号分隔</div>
          <el-input v-model="form.guardStates" size="small"></el-input>
          <div class="flow-label">守卫拒绝码</div>
          <el-input v-model="form.guardDenyCode" size="small"></el-input>
          <div class="flow-label">进入时 <el-button type="text" v-on:click="addAction(form.onEnter)">添加</el-button></div>
          <div v-for="(row, index) in form.onEnter" :key="'enter-' + index" style="margin-bottom: 6px;">
            <el-select v-model="row.action" size="small" clearable filterable placeholder="动作" style="width: 100%;">
              <el-option v-for="item in actions" :key="'e-' + item.code" :label="item.name" :value="item.code"></el-option>
            </el-select>
            <el-input v-model="row.when" size="small" placeholder="when，可空" style="margin-top: 4px;"></el-input>
            <el-input v-model="row.params" size="small" placeholder="参数，如 status=APPROVED" style="margin-top: 4px;"></el-input>
          </div>
          <div class="flow-label">完成时 <el-button type="text" v-on:click="addAction(form.onComplete)">添加</el-button></div>
          <div v-for="(row, index) in form.onComplete" :key="'complete-' + index" style="margin-bottom: 6px;">
            <el-select v-model="row.action" size="small" clearable filterable placeholder="动作" style="width: 100%;">
              <el-option v-for="item in actions" :key="'c-' + item.code" :label="item.name" :value="item.code"></el-option>
            </el-select>
            <el-input v-model="row.when" size="small" placeholder="when，可空" style="margin-top: 4px;"></el-input>
            <el-input v-model="row.params" size="small" placeholder="参数，如 status=APPROVED" style="margin-top: 4px;"></el-input>
          </div>
        </div>
        </div>
      </div>
    </div>
    <div v-if="problems.length" style="margin-top: 8px; color: #c45656;">
      <div v-for="(item, index) in problems" :key="index">{{ item }}</div>
    </div>
    <div style="margin-top: 16px; font-weight: 600;">版本</div>
    <el-table :data="versions" size="small" border style="margin-top: 8px;">
      <el-table-column label="版本" width="80">
        <template slot-scope="scope">{{ versionText(scope.row) }}</template>
      </el-table-column>
      <el-table-column label="来源" width="100">
        <template slot-scope="scope">{{ scope.row.customerId ? '本机构' : '平台默认' }}</template>
      </el-table-column>
      <el-table-column prop="status" label="状态" width="80">
        <template slot-scope="scope">{{ statusText(scope.row.status) }}</template>
      </el-table-column>
      <el-table-column label="发布时间" min-width="150">
        <template slot-scope="scope">{{ formatTime(scope.row.publishTime) }}</template>
      </el-table-column>
      <el-table-column label="更新时间" min-width="150">
        <template slot-scope="scope">{{ formatTime(scope.row.updateTime) }}</template>
      </el-table-column>
      <el-table-column label="操作" width="240">
        <template slot-scope="scope">
          <el-button type="text" v-on:click="openVersion(scope.row)">打开</el-button>
          <el-button v-if="scope.row.status !== 'DRAFT'" type="text" v-on:click="copyAsDraft(scope.row)">以此版本建草稿</el-button>
          <el-button v-if="scope.row.status === 'ACTIVE' && scope.row.customerId" type="text" v-on:click="disable(scope.row)">停用</el-button>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>

<script>
import BpmnModeler from 'bpmn-js/dist/bpmn-modeler.production.min.js';
import 'bpmn-js/dist/assets/diagram-js.css';
import 'bpmn-js/dist/assets/bpmn-js.css';
import 'bpmn-js/dist/assets/bpmn-font/css/bpmn-embedded.css';
import { applyNodeConfigs, blankDiagram, emptyConfig, readNodeConfigs } from './flow-xml';
import { withLayout } from './flow-layout';
import { downloadSvg } from './flow-svg';

export default {
  name: 'FlowDesigner',
  data: function () {
    return {
      bizCodes: [],
      bizCode: '',
      resolvers: [],
      actions: [],
      forms: [],
      roleCodes: [
        { code: 'ADMIN', name: '管理员' },
        { code: 'UltiMng', name: '最终管理人' },
        { code: 'IPMT', name: 'IPMT' },
        { code: 'PDM', name: '产品经理' },
        { code: 'PJM', name: '项目经理' },
        { code: 'SM', name: '系统工程师' },
        { code: 'TM', name: '测试主管' },
        { code: 'SYSGM', name: '系统工程总监' },
        { code: 'PDGM', name: '产品总监' },
        { code: 'QCGM', name: '质量总监' },
        { code: 'FGGM', name: '法规总监' },
        { code: 'TGM', name: '测试总监' },
        { code: 'MATM', name: '物料管理员' }
      ],
      versions: [],
      tasks: [],
      selectedId: '',
      form: null,
      flowForm: null,
      nodeConfigs: {},
      problems: [],
      hint: '正在读取登记清单',
      modeler: null
    };
  },
  computed: {
    currentBiz: function () {
      const code = this.bizCode;
      return this.bizCodes.filter(function (item) { return item.code === code; })[0] || null;
    },
    processKey: function () {
      return this.currentBiz ? this.currentBiz.processKey : '';
    },
    needsRole: function () {
      return this.form && (this.form.resolver === 'ROLE_IN_ORG' || this.form.fallback === 'ROLE_IN_ORG');
    },
    needsUser: function () {
      return this.form && (this.form.resolver === 'FIXED_USER' || this.form.fallback === 'FIXED_USER');
    }
  },
  mounted: function () {
    this.modeler = new BpmnModeler({ container: this.$refs.canvas, keyboard: { bindTo: document } });
    const _this = this;
    this.modeler.on('selection.changed', function (event) {
      const selected = event.newSelection && event.newSelection[0];
      if (selected && selected.type === 'bpmn:UserTask') {
        _this.flowForm = null;
        _this.onSelect(selected.id);
      } else if (selected && selected.type === 'bpmn:SequenceFlow') {
        _this.selectFlow(selected);
      } else {
        _this.flowForm = null;
      }
    });
    this.modeler.on('commandStack.changed', function () {
      _this.refreshTasks();
    });
    this.onResize = function () {
      _this.fit();
    };
    window.addEventListener('resize', this.onResize);
    this.loadCapability();
  },
  beforeDestroy: function () {
    window.removeEventListener('resize', this.onResize);
    if (this.modeler) {
      this.modeler.destroy();
    }
  },
  methods: {
    loadCapability: function () {
      const _this = this;
      this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/flow/capability/list', {}).then(function (response) {
        const resp = response.data;
        if (!resp || resp.success === false) {
          _this.hint = (resp && resp.message) || '无法读取登记清单';
          return;
        }
        const content = resp.content || {};
        _this.bizCodes = content.bizCodes || [];
        _this.resolvers = toOptions(content.resolvers);
        _this.actions = toOptions(content.actions);
        _this.forms = toOptions(content.forms);
        if (_this.bizCodes.length && !_this.bizCode) {
          _this.bizCode = _this.bizCodes[0].code;
          _this.onBizChange();
        }
        _this.hint = '画完用户任务后，在右侧选择解析器和动作，再保存或发布。';
      });
    },
    onBizChange: function () {
      const _this = this;
      const bizCode = this.bizCode;
      this.loadVersions().then(function () {
        if (bizCode !== _this.bizCode) {
          return;
        }
        const active = _this.versions.filter(function (row) { return row.status === 'ACTIVE'; });
        const chosen = active.filter(function (row) { return row.customerId; })[0] || active[0];
        if (chosen) {
          _this.openVersion(chosen);
        } else {
          _this.newBlank();
        }
      });
    },
    loadVersions: function () {
      const _this = this;
      if (!this.bizCode) {
        return Promise.resolve();
      }
      return this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/flow/def/list', { bizCode: this.bizCode }).then(function (response) {
        const resp = response.data;
        _this.versions = (resp && resp.success !== false && resp.content) || [];
      });
    },
    newBlank: function () {
      const biz = this.currentBiz;
      this.openXml(blankDiagram(biz ? biz.processKey : 'process', biz ? biz.name : ''));
    },
    openVersion: function (row) {
      const _this = this;
      this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/flow/def/get', { id: row.id }).then(function (response) {
        const resp = response.data;
        if (!resp || resp.success === false) {
          _this.hint = (resp && resp.message) || '无法打开该版本';
          return;
        }
        _this.openXml((resp.content && resp.content.bpmnXml) || '');
        _this.hint = (row.customerId ? '已打开本机构' : '已打开平台默认') + _this.versionText(row) + '（' + _this.statusText(row.status)
          + '）。修改后保存或发布为本机构的新版本。';
      });
    },
    openXml: function (source) {
      const xml = withLayout(source);
      this.nodeConfigs = readNodeConfigs(xml);
      this.selectedId = '';
      this.form = null;
      this.flowForm = null;
      this.problems = [];
      const _this = this;
      this.modeler.importXML(xml).then(function () {
        _this.refreshTasks();
        _this.fit();
      }).catch(function (error) {
        _this.hint = '流程图无法打开：' + (error && error.message ? error.message : error);
      });
    },
    fit: function () {
      if (this.modeler) {
        this.modeler.get('canvas').zoom('fit-viewport', 'auto');
      }
    },
    /** 选中连线：读名称、条件与是否为来源网关的默认分支。 */
    selectFlow: function (element) {
      const bo = element.businessObject;
      const source = bo.sourceRef;
      const fromGateway = !!source && source.$type === 'bpmn:ExclusiveGateway';
      const condition = bo.conditionExpression && bo.conditionExpression.body;
      this.flowForm = {
        id: element.id,
        name: bo.name || '',
        condition: condition || '',
        fromGateway: fromGateway,
        isDefault: fromGateway && !!source.default && source.default.id === element.id
      };
    },
    flowElement: function () {
      return this.flowForm ? this.modeler.get('elementRegistry').get(this.flowForm.id) : null;
    },
    applyFlowName: function () {
      const element = this.flowElement();
      if (element) {
        this.modeler.get('modeling').updateProperties(element, { name: this.flowForm.name || undefined });
      }
    },
    applyFlowCondition: function () {
      const element = this.flowElement();
      if (!element) {
        return;
      }
      const body = (this.flowForm.condition || '').trim();
      const expression = body
        ? this.modeler.get('moddle').create('bpmn:FormalExpression', { body: body })
        : undefined;
      this.modeler.get('modeling').updateProperties(element, { conditionExpression: expression });
    },
    applyFlowDefault: function (checked) {
      const element = this.flowElement();
      if (!element || !element.source) {
        return;
      }
      const modeling = this.modeler.get('modeling');
      if (checked) {
        // 默认分支不带条件，引擎对带条件的默认分支也不求值。
        this.flowForm.condition = '';
        modeling.updateProperties(element, { conditionExpression: undefined });
        modeling.updateProperties(element.source, { default: element.businessObject });
      } else {
        modeling.updateProperties(element.source, { default: undefined });
      }
    },
    importFile: function () {
      this.$refs.file.value = '';
      this.$refs.file.click();
    },
    onFileChosen: function (event) {
      const file = event.target.files && event.target.files[0];
      if (!file) {
        return;
      }
      const _this = this;
      const reader = new FileReader();
      reader.onload = function () {
        _this.openXml(String(reader.result || ''));
        _this.hint = '已导入 ' + file.name + '，校验通过后可以保存或发布为本机构的新版本。';
      };
      reader.readAsText(file, 'utf-8');
    },
    exportFile: function () {
      const _this = this;
      this.exportXml().then(function (xml) {
        const blob = new Blob([xml], { type: 'application/xml;charset=utf-8' });
        const url = URL.createObjectURL(blob);
        const link = document.createElement('a');
        link.href = url;
        link.download = (_this.processKey || 'process') + '.bpmn20.xml';
        document.body.appendChild(link);
        link.click();
        document.body.removeChild(link);
        setTimeout(function () {
          URL.revokeObjectURL(url);
        }, 1000);
      });
    },
    downloadSvg: function () {
      const _this = this;
      this.modeler.saveSVG().then(function (result) {
        downloadSvg(result.svg, (_this.processKey || 'process') + '.svg');
      });
    },
    formatTime: function (value) {
      if (!value) {
        return '';
      }
      const date = new Date(value);
      return isNaN(date.getTime()) ? value : date.toLocaleString();
    },
    refreshTasks: function () {
      if (!this.modeler) {
        return;
      }
      const registry = this.modeler.get('elementRegistry');
      const tasks = [];
      registry.forEach(function (element) {
        if (element.type === 'bpmn:UserTask') {
          tasks.push({ id: element.id, label: (element.businessObject && element.businessObject.name) || element.id });
        }
      });
      this.tasks = tasks;
    },
    onSelect: function (id) {
      if (!id) {
        this.form = null;
        return;
      }
      if (!this.nodeConfigs[id]) {
        this.$set(this.nodeConfigs, id, emptyConfig());
      }
      this.selectedId = id;
      this.form = this.nodeConfigs[id];
    },
    exportXml: function () {
      const _this = this;
      const biz = this.currentBiz;
      return this.modeler.saveXML({ format: true }).then(function (result) {
        return applyNodeConfigs(result.xml, biz ? biz.processKey : '', biz ? biz.name : '', _this.nodeConfigs);
      });
    },
    saveDraft: function () {
      const _this = this;
      this.exportXml().then(function (xml) {
        return _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/flow/def/save', {
          bizCode: _this.bizCode,
          bpmnXml: xml
        });
      }).then(function (response) {
        const resp = response.data;
        _this.hint = (resp && resp.message) || ((resp && resp.success === false) ? '保存失败' : '草稿已保存');
        if (resp && resp.success !== false) {
          _this.loadVersions();
        }
      });
    },
    validate: function () {
      const _this = this;
      this.exportXml().then(function (xml) {
        return _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/flow/def/validate', {
          bizCode: _this.bizCode,
          bpmnXml: xml
        });
      }).then(function (response) {
        const resp = response.data;
        if (!resp || resp.success === false) {
          _this.problems = [(resp && resp.message) || '校验失败'];
          return;
        }
        _this.problems = resp.content || [];
        _this.hint = _this.problems.length ? '校验未通过' : '校验通过';
      });
    },
    publish: function () {
      const _this = this;
      this.exportXml().then(function (xml) {
        return _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/flow/def/publish', {
          bizCode: _this.bizCode,
          bpmnXml: xml
        });
      }).then(function (response) {
        const resp = response.data;
        if (!resp || resp.success === false) {
          _this.hint = (resp && resp.message) || '发布失败';
          return;
        }
        _this.problems = [];
        _this.hint = resp.message || '已发布';
        _this.loadVersions();
      });
    },
    disable: function (row) {
      const _this = this;
      this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/flow/def/disable', { id: row.id }).then(function (response) {
        const resp = response.data;
        _this.hint = (resp && resp.message) || ((resp && resp.success === false) ? '停用失败' : '已停用');
        if (resp && resp.success !== false) {
          _this.loadVersions();
        }
      });
    },
    copyAsDraft: function (row) {
      const _this = this;
      const hasDraft = this.versions.some(function (item) { return item.status === 'DRAFT' && item.customerId; });
      const message = (row.customerId ? '以本机构' : '以平台默认') + this.versionText(row) + '建立草稿。'
        + (hasDraft ? '现有草稿将被覆盖。' : '') + '发布后成为新的版本，在途单据仍按原版本执行。';
      this.$confirm(message, '以此版本建草稿', { type: 'warning' }).then(function () {
        return _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/flow/def/save-draft-from/' + row.id);
      }).then(function (response) {
        const resp = response.data;
        if (!resp || resp.success === false) {
          _this.hint = (resp && resp.message) || '建草稿失败';
          return;
        }
        _this.loadVersions().then(function () {
          const draft = resp.content;
          if (draft && draft.bpmnXml) {
            _this.openXml(draft.bpmnXml);
          }
          _this.hint = '已按' + _this.versionText(row) + '建立草稿，修改后保存或发布为本机构的新版本。';
        });
      }).catch(function () {});
    },
    versionText: function (row) {
      if (!row || row.status === 'DRAFT' || !row.version) {
        return '草稿';
      }
      return '第 ' + row.version + ' 版';
    },
    addAction: function (rows) {
      rows.push({ action: '', when: '', params: '' });
    },
    statusText: function (status) {
      if (status === 'DRAFT') return '草稿';
      if (status === 'ACTIVE') return '启用';
      if (status === 'INACTIVE') return '停用';
      return status || '';
    }
  }
};

function toOptions(map) {
  const list = [];
  if (!map) {
    return list;
  }
  Object.keys(map).forEach(function (code) {
    list.push({ code: code, name: map[code] ? (code + ' ' + map[code]) : code });
  });
  return list;
}
</script>

<style scoped>
.flow-canvas {
  flex: 1;
  height: 560px;
  min-width: 0;
  border: 1px solid #dcdfe6;
  background: #fff;
}
.flow-label {
  margin-top: 8px;
  margin-bottom: 4px;
  color: #666;
  font-size: 12px;
}
</style>
