<!--
 * Author: 周朔鹏
 * Date: 2025-02-18
 * Email: 391902958@qq.com
 * Website: https://www.euipd.com
-->

<template>
  <div>
    <div class="gantt-toolbar">
      <el-button type="primary" icon="el-icon-plus" size="small" @click="addTask()">新增计划</el-button>
      <el-button type="warning" icon="el-icon-s-promotion" size="small" :loading="issuingDue" @click="issueDuePlans()">下发到期计划</el-button>
      <el-radio-group v-model="scale" size="small" @change="applyScale(true)">
        <el-radio-button label="day">日</el-radio-button>
        <el-radio-button label="week">周</el-radio-button>
        <el-radio-button label="month">月</el-radio-button>
      </el-radio-group>
      <el-select v-model="filter.executorId" size="small" clearable filterable placeholder="执行人"
                 style="width: 130px;" @change="refreshFilter">
        <el-option v-for="o in executorOptions" :key="o.id" :label="o.name" :value="o.id"></el-option>
      </el-select>
      <el-select v-model="filter.status" size="small" clearable placeholder="状态"
                 style="width: 130px;" @change="refreshFilter">
        <el-option v-for="o in STATUS_OPTIONS" :key="o.key" :label="o.value" :value="o.key"></el-option>
      </el-select>
      <el-checkbox v-model="filter.planOnly" @change="onPlanOnlyChange">只看计划</el-checkbox>
      <el-checkbox v-model="filter.openOnly" @change="onOpenOnlyChange">只看在办</el-checkbox>
      <el-checkbox v-model="filter.hideComplete" @change="refreshFilter">隐藏已完成</el-checkbox>
    </div>
    <div class="gantt-tip">
      只有待下发、下发失败的计划可在图中拖动、拉伸与双击编辑；已下发任务只读，工期请在“工期管理”中调整。依赖连线至少一端须为可编辑的计划。
    </div>
    <div ref="gantt" :style="'height:' + (tabHeight - 110) + 'px'" />

    <!--新增计划-->
    <el-dialog
        width="29cm"
        :visible.sync="ganttDlg"
        :before-close="handleClose"
        :close-on-click-modal=false>
      <div slot="title" style="font-size: 20px;">{{ dialogTitle }}</div>
      <div>
        <JobitemCreateByProjectIdDlgGantt
            v-if="ganttDlg"
            :key="((subprojectId) + '-' + (random))"
            :x-project-id="subprojectId"
            :job-type="JOB_ITEM_TYPE.TASK_SUBP.key"
            :project-type="PROJECT_TYPE.SUB_PROJECT.key"
            :entrance="ENTRANCE_CLASS.PROJECT.key"
            :close-dlg=closeDialog
        ></JobitemCreateByProjectIdDlgGantt>
      </div>
    </el-dialog>

    <!--编辑计划-->
    <el-dialog
        width="29cm"
        :visible.sync="ganttDlg_dblClick"
        :before-close="handleClose"
        :close-on-click-modal=false>
      <div slot="title" style="font-size: 20px;">{{ dialogTitle }}</div>
      <div>
        <JobitemEditGanttDlg
            v-if="ganttDlg_dblClick"
            :key="((dblClickedItem.id) + '-' + (random))"
            :task-id="dblClickedItem.id"
            :can-edit="!!dblClickedItem.editable"
            :project-type="PROJECT_TYPE.SUB_PROJECT.key"
            :close-dlg=closeDialog_modify
        ></JobitemEditGanttDlg>
      </div>
    </el-dialog>

    <!--工单只读详情-->
    <el-dialog
        width="25cm"
        :visible.sync="jobViewDlg"
        :before-close="handleClose"
        :close-on-click-modal=false>
      <div slot="title" style="font-size: 20px;">{{ dialogTitle }}</div>
      <div>
        <GanttJobitemViewDlg
            v-if="jobViewDlg"
            :key="((dblClickedItem.id) + '-' + (random))"
            :jobitem-id="dblClickedItem.id"
            :close-dlg=closeDialog_view
        ></GanttJobitemViewDlg>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import gantt from 'dhtmlx-gantt';
import 'dhtmlx-gantt/codebase/dhtmlxgantt.css';
import {Loading} from "element-ui";
import Swal from "sweetalert2";
import {optionKV} from "@/filter/filter";
import JobitemCreateByProjectIdDlgGantt from "@/components/std/jobitem-create-by-project-id-dlg-gantt.vue";
import JobitemEditGanttDlg from "@/components/std/jobitem-edit-gantt-dlg.vue";
import GanttJobitemViewDlg from "@/components/std/gantt-jobitem-view-dlg.vue";

const KIND = {
  SUBPROJECT: 'subproject',
  CHARACTER: 'character',
  MILESTONE: 'milestone',
  JOB: 'job',
  PLAN: 'plan',
};

const STATUS_OPTIONS = [
  { key: 'going', value: '进行中' },
  { key: 'refused', value: '拒收待重下发' },
  { key: 'overdue', value: '逾期' },
  { key: 'complete', value: '已完成' },
  { key: 'plan', value: '待下发' },
  { key: 'failed', value: '下发失败' },
];

const STATUS_TEXT = STATUS_OPTIONS.reduce((m, o) => {
  m[o.key] = o.value;
  return m;
}, { milestone: '里程碑' });

const OPEN_JOB_STATUSES = ['going', 'refused', 'overdue'];

/** 链接类型 “完成-开始” */
const LINK_FINISH_TO_START = '0';

function escapeHtml(value) {
  if (value === null || value === undefined) {
    return '';
  }
  return String(value)
      .replace(/&/g, '&amp;')
      .replace(/</g, '&lt;')
      .replace(/>/g, '&gt;')
      .replace(/"/g, '&quot;')
      .replace(/'/g, '&#39;');
}

export default {
  name: 'gantt',
  components: {JobitemCreateByProjectIdDlgGantt, JobitemEditGanttDlg, GanttJobitemViewDlg},
  props: ['subprojectId', 'dateType'],

  data() {
    return {
      loginUser: {},
      loginCustomer: {},
      ganttEventIds: [],
      ganttInited: false,
      ganttDlg: false,
      ganttDlg_dblClick: false,
      jobViewDlg: false,
      dblClickedItem: {},
      dialogTitle: '',

      ganttList: [],
      JOB_ITEM_TYPE: JOB_ITEM_TYPE,
      PROJECT_TYPE: PROJECT_TYPE,
      ENTRANCE_CLASS: ENTRANCE_CLASS,
      STATUS_OPTIONS: STATUS_OPTIONS,

      scale: ['day', 'week', 'month'].includes(this.dateType) ? this.dateType : 'day',
      filter: {
        executorId: '',
        status: '',
        planOnly: false,
        openOnly: false,
        hideComplete: false,
      },

      tabHeight: 0,
      random: 0,
      scheduleSaveTimers: {},
      issuingDue: false,
      suppressLinkEvents: false,
      todayMarkerId: null,
      loadSeq: 0,
      deactivated: false,
    };
  },

  computed: {
    executorOptions() {
      const seen = {};
      const options = [];
      this.ganttList.forEach((item) => {
        if ((item.kind === KIND.JOB || item.kind === KIND.PLAN) && item.executorId && !seen[item.executorId]) {
          seen[item.executorId] = true;
          options.push({ id: item.executorId, name: item.executorName || item.executorId });
        }
      });
      return options;
    },
  },

  mounted() {
    this.random = Math.random();
    let h = window.innerHeight || document.documentElement.clientHeight || document.body.clientHeight;
    this.tabHeight = h - 170;

    this.getLoginUser();
    this.initGanttConfig();
    this.bindGanttEvents();
    this.loadGanttData();

    this.$bus.$on('refresh-gantt-list', this.onRefreshEvent);
  },

  activated() {
    if (this.deactivated) {
      this.deactivated = false;
      this.loadGanttData({ silent: true });
    }
  },

  deactivated() {
    this.deactivated = true;
  },

  beforeDestroy() {
    Object.keys(this.scheduleSaveTimers).forEach((id) => clearTimeout(this.scheduleSaveTimers[id]));
    this.scheduleSaveTimers = {};
    this.ganttEventIds.forEach((id) => {
      try {
        gantt.detachEvent(id);
      } catch (e) { /* ignore */ }
    });
    this.ganttEventIds = [];
    this.$bus.$off('refresh-gantt-list', this.onRefreshEvent);
    this.removeTodayMarker();
    if (this.ganttInited) {
      try {
        gantt.clearAll();
      } catch (e) { /* ignore */ }
      this.ganttInited = false;
    }
  },

  methods: {
    onRefreshEvent(data) {
      if (data && !this.deactivated) {
        this.loadGanttData({ silent: true });
      }
    },

    attachGanttEvent(name, handler, config) {
      const id = gantt.attachEvent(name, handler, config);
      this.ganttEventIds.push(id);
      return id;
    },

    formatLocalYmd(dateLike) {
      if (!dateLike) {
        return null;
      }
      const x = dateLike instanceof Date ? dateLike : new Date(dateLike);
      if (isNaN(x.getTime())) {
        return null;
      }
      const p = (n) => String(n).padStart(2, '0');
      return `${x.getFullYear()}-${p(x.getMonth() + 1)}-${p(x.getDate())}`;
    },

    startOfToday() {
      const d = new Date();
      d.setHours(0, 0, 0, 0);
      return d;
    },

    getLoginUser() {
      this.loginUser = Tool.getActorUser() || {};
      this.loginCustomer = Tool.getLoginCustomer() || {};
    },

    getNode(id) {
      return id !== undefined && id !== null && gantt.isTaskExists(id) ? gantt.getTask(id) : null;
    },

    isEditablePlan(id) {
      const node = this.getNode(id);
      return !!(node && node.kind === KIND.PLAN && node.editable);
    },

    statusText(task) {
      if (!task) {
        return '';
      }
      if (task.kind === KIND.SUBPROJECT || task.kind === KIND.CHARACTER) {
        return STATUS_TEXT[task.status] || '';
      }
      return STATUS_TEXT[task.status] || task.status || '';
    },

    initGanttConfig() {
      gantt.plugins({ marker: true, tooltip: true });
      gantt.i18n.setLocale('cn');
      gantt.locale.date = {
        month_full: ['1月', '2月', '3月', '4月', '5月', '6月', '7月', '8月', '9月', '10月', '11月', '12月'],
        month_short: ['1月', '2月', '3月', '4月', '5月', '6月', '7月', '8月', '9月', '10月', '11月', '12月'],
        day_full: ['星期日', '星期一', '星期二', '星期三', '星期四', '星期五', '星期六'],
        day_short: ['星期日', '星期一', '星期二', '星期三', '星期四', '星期五', '星期六']
      };

      gantt.config.drag_move = true;
      gantt.config.drag_project = false;
      gantt.config.drag_resize = true;
      gantt.config.drag_progress = false;
      gantt.config.drag_links = true;
      gantt.config.details_on_dblclick = false;
      gantt.config.details_on_create = false;
      gantt.config.round_dnd_dates = true;

      gantt.config.autosize = false;
      gantt.config.show_grid = true;
      gantt.config.scale_height = 50;
      gantt.config.bar_height = 24;
      gantt.config.row_height = 32;
      gantt.config.date_format = "%Y-%m-%d";
      gantt.config.duration_unit = "day";
      gantt.config.grid_width = 560;
      gantt.config.columns = [
        {name: "text", label: "任务名称", tree: true, width: 220},
        {name: "executorName", label: "执行人", align: "center", width: 80,
          template: (task) => escapeHtml(task.executorName || '')},
        {name: "start_date", label: "开始日期", align: "center", width: 90},
        {name: "duration", label: "工期(天)", align: "center", width: 60},
        {name: "statusText", label: "状态", align: "center", width: 100,
          template: (task) => escapeHtml(this.statusText(task))},
      ];
      gantt.config.fit_tasks = true;
      gantt.config.link_line_width = 2;
      gantt.config.link_arrow_size = 7;
      gantt.config.tooltip_timeout = 300;

      this.applyScale(false);

      gantt.templates.task_class = (start, end, task) => this.taskClass(task);
      gantt.templates.grid_row_class = (start, end, task) => (task && task.kind === KIND.PLAN ? 'gantt-row-plan' : '');

      gantt.templates.task_text = (start, end, task) => {
        const name = escapeHtml(task.text);
        if ((task.kind === KIND.JOB || task.kind === KIND.PLAN) && task.executorName) {
          return `${name}（${escapeHtml(task.executorName)}：${task.manhour}H）`;
        }
        return name;
      };

      gantt.templates.tooltip_text = (start, end, task) => this.tooltipText(task);
    },

    applyScale(rerender) {
      if (this.scale === 'month') {
        gantt.config.min_column_width = 60;
        gantt.config.scales = [
          { unit: 'year', step: 1, format: '%Y 年' },
          { unit: 'month', step: 1, format: '%F' },
        ];
      } else if (this.scale === 'week') {
        gantt.config.min_column_width = 50;
        gantt.config.scales = [
          { unit: 'month', step: 1, format: '%Y 年 %F' },
          { unit: 'week', step: 1, format: '第%W周' },
        ];
      } else {
        gantt.config.min_column_width = 30;
        gantt.config.scales = [
          { unit: 'month', step: 1, format: '%Y 年 %F' },
          { unit: 'day', step: 1, format: '%d' },
        ];
      }
      if (rerender && this.ganttInited) {
        gantt.render();
        this.showToday();
      }
    },

    taskClass(task) {
      if (!task || typeof task !== 'object') {
        return '';
      }
      const classes = [];
      switch (task.kind) {
        case KIND.SUBPROJECT:
          classes.push('gantt-node-subproject');
          break;
        case KIND.CHARACTER:
          classes.push('gantt-node-character');
          break;
        case KIND.MILESTONE:
          classes.push('gantt-node-milestone');
          break;
        case KIND.JOB:
          classes.push('gantt-job', 'gantt-job-' + task.status);
          break;
        case KIND.PLAN:
          classes.push('gantt-plan');
          if (task.status === 'failed') {
            classes.push('gantt-plan-failed');
          }
          break;
        default:
          break;
      }
      if (!(task.kind === KIND.PLAN && task.editable)) {
        classes.push('gantt-locked');
      }
      return classes.join(' ');
    },

    tooltipText(task) {
      if (!task) {
        return '';
      }
      const rows = [`<b>${escapeHtml(task.text)}</b>`];
      const add = (label, value) => {
        if (value !== undefined && value !== null && value !== '') {
          rows.push(`${label}：${escapeHtml(value)}`);
        }
      };
      if (task.kind === KIND.JOB) {
        add('工单类型', optionKV(JOB_ITEM_TYPE, task.jobType) || task.jobType);
        add('执行人', task.executorName);
        add('工时', task.manhour + ' 小时');
        add('计划起止', `${task.planStart || ''} 至 ${task.planEnd || ''}`);
        add('实际下发', task.actualStart);
        add('实际提交', task.actualEnd);
        add('状态', this.statusText(task));
        rows.push('<span style="color:#909399">已下发任务的工期请在工期管理中调整</span>');
      } else if (task.kind === KIND.PLAN) {
        add('状态', this.statusText(task));
        add('执行人', task.executorName);
        add('工时', task.manhour + ' 小时');
        add('计划起止', `${task.planStart || ''} 至 ${task.planEnd || ''}`);
        if (task.status === 'failed') {
          add('失败原因', task.failReason);
        }
        if (!task.editable) {
          rows.push('<span style="color:#909399">无权编辑该计划</span>');
        }
      } else if (task.kind === KIND.MILESTONE) {
        add('评审日', task.planStart);
      } else {
        add('起止', `${task.planStart || ''} 至 ${task.planEnd || ''}`);
        add('状态', this.statusText(task));
      }
      return rows.join('<br/>');
    },

    passFilter(task) {
      const f = this.filter;
      if (task.kind === KIND.SUBPROJECT || task.kind === KIND.CHARACTER) {
        return true;
      }
      const nodeFilterOn = f.planOnly || f.openOnly || f.status || f.executorId;
      if (task.kind === KIND.MILESTONE) {
        return !nodeFilterOn;
      }
      if (f.planOnly && task.kind !== KIND.PLAN) {
        return false;
      }
      if (f.openOnly && !(task.kind === KIND.JOB && OPEN_JOB_STATUSES.includes(task.status))) {
        return false;
      }
      if (f.hideComplete && task.status === 'complete') {
        return false;
      }
      if (f.status && task.status !== f.status) {
        return false;
      }
      if (f.executorId && task.executorId !== f.executorId) {
        return false;
      }
      return true;
    },

    onPlanOnlyChange(value) {
      if (value) {
        this.filter.openOnly = false;
      }
      this.refreshFilter();
    },

    onOpenOnlyChange(value) {
      if (value) {
        this.filter.planOnly = false;
      }
      this.refreshFilter();
    },

    refreshFilter() {
      if (this.ganttInited) {
        gantt.refreshData();
      }
    },

    removeTodayMarker() {
      if (this.todayMarkerId) {
        try {
          gantt.deleteMarker(this.todayMarkerId);
        } catch (e) { /* ignore */ }
        this.todayMarkerId = null;
      }
    },

    showToday() {
      this.removeTodayMarker();
      this.todayMarkerId = gantt.addMarker({
        start_date: new Date(),
        css: 'gantt-today-marker',
        text: '今天',
        title: '今天：' + this.formatLocalYmd(new Date()),
      });
    },

    bindGanttEvents() {
      this.attachGanttEvent("onBeforeTaskDisplay", (id, task) => this.passFilter(task));

      this.attachGanttEvent("onBeforeLightbox", () => false);

      this.attachGanttEvent("onTaskDblClick", (id) => {
        const node = this.getNode(id);
        if (!node) {
          return false;
        }
        this.dblClickedItem = node;
        this.random = Math.random();
        if (node.kind === KIND.PLAN) {
          this.dialogTitle = node.editable ? "编辑子项目任务计划" : "查看子项目任务计划";
          this.ganttDlg_dblClick = true;
        } else if (node.kind === KIND.JOB) {
          this.dialogTitle = "工单详情";
          this.jobViewDlg = true;
        } else {
          Swal.fire({
            title: '提示',
            text: '子项目、特性与里程碑节点在甘特图中只读',
            icon: 'info',
            confirmButtonColor: '#409EFF',
            confirmButtonText: '确认',
          });
        }
        return false;
      });

      this.attachGanttEvent("onBeforeTaskDrag", (id, mode) => {
        if (mode === gantt.config.drag_mode.progress) {
          return false;
        }
        return this.isEditablePlan(id);
      });

      this.attachGanttEvent("onBeforeTaskChanged", (id, mode, original) => {
        const task = this.getNode(id);
        if (!task || task.kind !== KIND.PLAN) {
          return true;
        }
        const start = task.start_date;
        if (!(start instanceof Date) || isNaN(start.getTime())) {
          return false;
        }
        const unchanged = original && original.start_date
            && this.formatLocalYmd(original.start_date) === this.formatLocalYmd(start);
        if (!unchanged && start.getTime() <= this.startOfToday().getTime()) {
          Toast.warning('计划开始日须晚于今天，当日任务请直接下发', Swal);
          return false;
        }
        if (!task.duration || task.duration < 1) {
          Toast.warning('工期至少为 1 天', Swal);
          return false;
        }
        this.warnPredecessorOverlap(task);
        return true;
      });

      this.attachGanttEvent("onAfterTaskDrag", (id) => {
        const task = this.getNode(id);
        if (!task || task.kind !== KIND.PLAN) {
          return;
        }
        if (this.scheduleSaveTimers[id]) {
          clearTimeout(this.scheduleSaveTimers[id]);
        }
        this.scheduleSaveTimers[id] = setTimeout(() => {
          delete this.scheduleSaveTimers[id];
          this.updatePlanSchedule(task);
        }, 300);
      });

      this.attachGanttEvent("onBeforeLinkAdd", (id, link) => {
        if (this.suppressLinkEvents) {
          return true;
        }
        if (String(link.type) !== LINK_FINISH_TO_START) {
          Toast.warning('依赖连线只允许“完成-开始”类型', Swal);
          return false;
        }
        if (!this.isEditablePlan(link.source) && !this.isEditablePlan(link.target)) {
          Toast.warning('依赖连线至少一端须为可编辑的计划', Swal);
          return false;
        }
        return true;
      });

      this.attachGanttEvent("onAfterLinkAdd", (id, link) => {
        if (this.suppressLinkEvents) {
          return;
        }
        this.saveLink(id, link);
      });

      this.attachGanttEvent("onBeforeLinkDelete", (id, link) => {
        if (this.suppressLinkEvents) {
          return true;
        }
        if (!this.isEditablePlan(link.source) && !this.isEditablePlan(link.target)) {
          Toast.warning('两端均为已下发任务的连线不能在甘特图中删除', Swal);
          return false;
        }
        this.delLink(id);
        return true;
      });
    },

    warnPredecessorOverlap(task) {
      const names = [];
      (task.$target || []).forEach((linkId) => {
        if (!gantt.isLinkExists(linkId)) {
          return;
        }
        const pred = this.getNode(gantt.getLink(linkId).source);
        if (pred && pred.end_date && pred.end_date.getTime() > task.start_date.getTime()) {
          names.push(pred.text);
        }
      });
      if (names.length > 0) {
        Toast.warning('计划开始早于前置节点结束：' + names.join('、') + '，已按所选日期保存', Swal);
      }
    },

    async loadGanttData(options = {}) {
      if (!this.subprojectId) {
        return;
      }
      const seq = ++this.loadSeq;
      let loadingInstance = options.silent ? null : Loading.service({ fullscreen: true });
      try {
        const [tasksResponse, linksResponse] = await Promise.all([
          this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/gantt/getGanttListBySubprojectId/' + this.subprojectId),
          this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/gantt/getGanttLinkListBySubprojectId/' + this.subprojectId),
        ]);
        if (seq !== this.loadSeq || !this.$refs.gantt) {
          return;
        }
        const tasksResp = tasksResponse.data;
        const linksResp = linksResponse.data;
        if (!tasksResp.success) {
          Toast.warning(tasksResp.message, Swal);
          return;
        }
        if (!linksResp.success) {
          Toast.warning(linksResp.message, Swal);
          return;
        }

        this.ganttList = tasksResp.content || [];
        const data = this.ganttList.map((item) => ({
          id: item.id,
          text: item.text,
          start_date: item.start_date,
          parent: item.parent || 0,
          duration: item.duration,
          type: item.type,
          kind: item.kind,
          status: item.status,
          executorId: item.executorId,
          executorName: item.executorName,
          manhour: item.manhour != null ? Number(item.manhour).toFixed(2) : '0.00',
          progress: item.progress || 0,
          open: item.open === 1,
          editable: !!item.editable,
          timeEditable: !!item.timeEditable,
          planStart: item.planStart,
          planEnd: item.planEnd,
          actualStart: item.actualStart,
          actualEnd: item.actualEnd,
          jobType: item.jobType,
          jobStatus: item.jobStatus,
          planStatus: item.planStatus,
          failReason: item.failReason,
        }));
        const links = (linksResp.content || []).map((l) => ({
          id: l.id,
          source: l.source,
          target: l.target,
          type: String(l.type != null ? l.type : LINK_FINISH_TO_START),
        }));

        this.suppressLinkEvents = true;
        try {
          gantt.clearAll();
          if (!this.ganttInited) {
            gantt.init(this.$refs.gantt);
            this.ganttInited = true;
          }
          gantt.parse({ data, links });
          this.showToday();
          gantt.render();
        } finally {
          this.suppressLinkEvents = false;
        }
      } catch (e) {
        console.error(e);
        Toast.warning('加载甘特图失败', Swal);
      } finally {
        if (loadingInstance) {
          loadingInstance.close();
        }
      }
    },

    updatePlanSchedule(task) {
      if (!task || !task.id || !task.start_date || !task.duration) {
        return;
      }
      const planStartTime = this.formatLocalYmd(task.start_date);
      if (!planStartTime) {
        Toast.warning('改期日期无效', Swal);
        this.loadGanttData({ silent: true });
        return;
      }
      const payload = {
        id: task.id,
        planStartTime: planStartTime,
        duration: task.duration,
      };
      this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/gantt/updatePlanSchedule', payload).then((response) => {
        const resp = response.data;
        if (!resp.success) {
          Toast.warning(resp.message || '改期保存失败', Swal);
        }
        this.loadGanttData({ silent: true });
      }).catch(() => {
        Toast.warning('改期保存失败', Swal);
        this.loadGanttData({ silent: true });
      });
    },

    issueDuePlans() {
      if (this.issuingDue) {
        return;
      }
      Swal.fire({
        title: '下发到期计划',
        text: '将下发本子项目中开始日期已到的待下发计划；下发失败的计划请修改后在编辑对话框中立即下发。是否继续？',
        icon: 'question',
        showCancelButton: true,
        confirmButtonColor: '#409EFF',
        cancelButtonColor: '#909399',
        confirmButtonText: '确认下发',
        cancelButtonText: '取消',
      }).then((result) => {
        if (!result.isConfirmed) {
          return;
        }
        this.issuingDue = true;
        this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/gantt/issuePlanTaskBySubprojectId/' + this.subprojectId).then((response) => {
          this.issuingDue = false;
          const resp = response.data;
          if (!resp.success) {
            Toast.warning(resp.message, Swal);
            return;
          }
          const r = resp.content || {};
          const issued = r.issued || 0;
          const failed = r.failed || 0;
          const cancelled = r.cancelled || 0;
          let text = `成功 ${issued} 条，失败 ${failed} 条，取消 ${cancelled} 条。`;
          if (issued + failed + cancelled === 0) {
            text = '本子项目没有到期的待下发计划。';
          } else if (failed > 0) {
            text += '失败的计划已标红，悬停可查看原因，创建人同时收到通知。';
          }
          Swal.fire({
            title: '下发结果',
            text: text,
            icon: failed > 0 ? 'warning' : 'success',
            confirmButtonColor: '#409EFF',
            confirmButtonText: '确认',
          });
          this.loadGanttData({ silent: true });
        }).catch(() => {
          this.issuingDue = false;
          Toast.warning('下发失败', Swal);
        });
      });
    },

    silentDeleteLink(id) {
      if (!gantt.isLinkExists(id)) {
        return;
      }
      this.suppressLinkEvents = true;
      try {
        gantt.deleteLink(id);
      } finally {
        this.suppressLinkEvents = false;
      }
    },

    saveLink(tempId, link) {
      const payload = { source: link.source, target: link.target, type: Number(link.type) };
      this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/gantt/saveLink', payload).then((response) => {
        const resp = response.data;
        if (!resp.success) {
          Toast.warning(resp.message || '依赖连线保存失败', Swal);
          this.silentDeleteLink(tempId);
          return;
        }
        const serverId = resp.content;
        if (!serverId || String(serverId) === String(tempId) || !gantt.isLinkExists(tempId)) {
          return;
        }
        if (gantt.isLinkExists(serverId)) {
          this.silentDeleteLink(tempId);
        } else {
          gantt.changeLinkId(tempId, serverId);
        }
      }).catch(() => {
        Toast.warning('依赖连线保存失败', Swal);
        this.silentDeleteLink(tempId);
      });
    },

    delLink(id) {
      this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/gantt/delLink/' + id).then((response) => {
        const resp = response.data;
        if (!resp.success) {
          Toast.warning(resp.message || '依赖连线删除失败', Swal);
          this.loadGanttData({ silent: true });
        }
      }).catch(() => {
        Toast.warning('依赖连线删除失败', Swal);
        this.loadGanttData({ silent: true });
      });
    },

    addTask() {
      this.random = Math.random();
      this.ganttDlg = true;
      this.dialogTitle = "创建子项目任务计划";
    },

    handleClose(done) {
      done();
      this.ganttDlg = false;
      this.ganttDlg_dblClick = false;
      this.jobViewDlg = false;
    },
    closeDialog() {
      this.ganttDlg = false;
    },
    closeDialog_modify() {
      this.ganttDlg_dblClick = false;
    },
    closeDialog_view() {
      this.jobViewDlg = false;
    },
  }
};
</script>

<style lang="scss">
.gantt-toolbar {
  display: flex;
  flex-direction: row;
  flex-wrap: wrap;
  justify-content: flex-start;
  align-items: center;
  gap: 8px;
}
.gantt-tip {
  margin: 6px 0;
  font-size: 12px;
  color: #909399;
}

/* 工单：进行中蓝、拒收待重下发蓝虚线、逾期红、已完成灰 */
.gantt-job-going {
  border: 1px solid #409eff;
  background: #409eff;
  .gantt_task_progress { background: #2b7fd6; }
}
.gantt-job-refused {
  border: 2px dashed #409eff;
  background: #d9ecff;
  .gantt_task_content { color: #1f5d99; }
}
.gantt-job-overdue {
  border: 1px solid #f56c6c;
  background: #f56c6c;
  .gantt_task_progress { background: #d94a4a; }
}
.gantt-job-complete {
  border: 1px solid #b1b3b8;
  background: #c8c9cc;
  .gantt_task_content { color: #303133; }
  .gantt_task_progress { background: #b1b3b8; }
}

/* 计划：待下发橙、下发失败红边框 */
.gantt-plan {
  border: 2px solid #e6a23c;
  background: #f0c78a;
  .gantt_task_content { color: #5a3d0a; }
  .gantt_task_progress { background: #d9a15b; }
}
.gantt-plan-failed {
  border: 2px solid #f56c6c;
  background: #f0c78a;
}
.gantt-plan.gantt_task_line.gantt_selected {
  box-shadow: 0 0 5px #e6a23c;
}

/* 子项目与特性 */
.gantt-node-subproject {
  border: 1px solid #65c16f;
  background: #65c16f;
  .gantt_task_progress { background: #46ad51; }
  .gantt_task_progress_drag { display: none; }
}
.gantt-node-character {
  border: 1px solid #8cc5ff;
  background: #b3d8ff;
  .gantt_task_content { color: #1f5d99; }
}

/* 里程碑 */
.gantt-node-milestone {
  background-color: rgba(240, 248, 255, 0.01);
  box-sizing: content-box;
  width: 22px !important;
  height: 22px !important;

  .gantt_task_progress_wrapper {
    border-radius: inherit;
    position: relative;
    width: 100%;
    height: 100%;
    overflow: hidden;
    transform: rotate(45deg);
    background-color: #d33daf;
  }
  .gantt_task_content {
    width: 22px !important;
    height: 22px !important;
    margin-top: 3px !important;
  }
}

/* 只读节点不显示拉伸柄；连线拖拽柄保留，用于以工单为前置或后继连接计划 */
.gantt-locked .gantt_task_drag {
  display: none !important;
}
.gantt-locked {
  cursor: default;
}

.gantt-row-plan {
  background-color: #fdf6ec;
}

.gantt-today-marker {
  background-color: #f56c6c;
  opacity: 0.6;
}

.gantt_tree_content {
  overflow: hidden;
  text-overflow: ellipsis;
}
</style>
