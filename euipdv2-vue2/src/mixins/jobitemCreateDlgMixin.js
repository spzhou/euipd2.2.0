/**
 * 任务创建对话框公共逻辑
 * - 登录人/机构（统一 ActorUser）
 * - 本地日期 yyyy-MM-dd
 * - 执行人列表
 * - 核心字段校验（名称/执行人/描述/提交时间/工时）
 * 不改变各对话框自身的保存 API 与类型分支。
 */
import Swal from 'sweetalert2';

/** 即时下发窗口（自然月），与后端 rdms.job-item.issue-window-months 一致。 */
export const ISSUE_WINDOW_MONTHS = 3;
/** 受下发窗口约束的项目执行类任务，与后端 JobIssueWindowGuard 登记一致。 */
export const ISSUE_WINDOW_TYPES = [
  'TASK_SUBP', 'TASK_PRO', 'TASK', 'DEVELOP', 'CHARACTER_INT', 'SUBPROJECT_INT', 'PROJECT_INT',
  'ASSIST', 'TEST', 'TASK_TEST', 'BUG', 'TASK_BUG', 'QUALITY',
];
export const ISSUE_WINDOW_HINT = `超过 ${ISSUE_WINDOW_MONTHS} 个月的任务请在子项目计划（甘特图）中编制`;

export default {
  data() {
    return {
      loginUser: {},
      loginCustomer: {},
    };
  },

  methods: {
    /**
     * 初始化当前操作者与所属机构（替代各对话框复制的 getLoginUser 分支）
     */
    initLoginContext() {
      this.loginUser = (typeof Tool !== 'undefined' && Tool.getActorUser)
        ? (Tool.getActorUser() || {})
        : {};
      this.loginCustomer = (typeof Tool !== 'undefined' && Tool.getLoginCustomer)
        ? (Tool.getLoginCustomer() || {})
        : {};
    },

    /**
     * 兼容旧调用名；子组件可覆盖并先调 initLoginContext 再做额外请求
     */
    getLoginUser() {
      this.initLoginContext();
    },

    /**
     * 将 Date/字符串格式化为本地日历日 yyyy-MM-dd，避免 JSON 序列化时区偏移
     */
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

    /**
     * 开始日期：禁用今天之前（允许今天）
     */
    disabledDateBeforeToday(v) {
      if (!v) {
        return false;
      }
      const today = new Date();
      today.setHours(0, 0, 0, 0);
      return v.getTime() < today.getTime();
    },

    /**
     * 开始日期：禁用今天及之前（甘特计划须从次日开始）
     */
    disabledDateBeforeTomorrow(v) {
      if (!v) {
        return false;
      }
      const today = new Date();
      today.setHours(0, 0, 0, 0);
      return v.getTime() <= today.getTime();
    },

    /**
     * 下发窗口截止日（含当日）：今天起满 ISSUE_WINDOW_MONTHS 个自然月的同日，月末顺延到当月最后一天
     */
    issueWindowEnd() {
      const today = new Date();
      today.setHours(0, 0, 0, 0);
      const target = new Date(today.getFullYear(), today.getMonth() + ISSUE_WINDOW_MONTHS, 1);
      const lastDay = new Date(target.getFullYear(), target.getMonth() + 1, 0).getDate();
      target.setDate(Math.min(today.getDate(), lastDay));
      return target;
    },

    /** 该类型是否受下发窗口约束 */
    inIssueWindow(jobType) {
      return ISSUE_WINDOW_TYPES.includes(jobType);
    },

    /** 提交日期下方的窗口提示；不受窗口约束的类型返回空串 */
    issueWindowHintFor(jobType) {
      return this.inIssueWindow(jobType) ? ISSUE_WINDOW_HINT : '';
    },

    /**
     * 提交日期：禁用今天之前；项目执行类任务另禁用窗口截止日之后
     */
    disabledSubmitDateInWindow(v, jobType) {
      if (!v) {
        return false;
      }
      if (this.disabledDateBeforeToday(v)) {
        return true;
      }
      return this.inIssueWindow(jobType) && v.getTime() > this.issueWindowEnd().getTime();
    },

    /**
     * 计划提交日下限：开始日 + 按每日最大工时折算的天数 − 1（工期含开始日与提交日）
     */
    planEarliestSubmit(planStart, manhour) {
      if (!planStart) {
        return null;
      }
      const start = new Date(planStart);
      if (isNaN(start.getTime())) {
        return null;
      }
      start.setHours(0, 0, 0, 0);
      const dayHour = typeof MAX_DAY_HOUR !== 'undefined' ? MAX_DAY_HOUR : 10;
      const days = Math.max(1, Math.ceil(Number(manhour || 0) / dayHour));
      return new Date(start.getFullYear(), start.getMonth(), start.getDate() + days - 1);
    },

    /** 计划提交日：禁用早于下限的日期；开始日未选时按次日计算 */
    disabledPlanSubmitDate(v, planStart, manhour) {
      if (!v) {
        return false;
      }
      let start = planStart;
      if (!start) {
        start = new Date();
        start.setDate(start.getDate() + 1);
      }
      const earliest = this.planEarliestSubmit(start, manhour);
      return !!earliest && v.getTime() < earliest.getTime();
    },

    /** 开始日或工时变化后，提交日早于下限时推到下限 */
    alignPlanSubmit(info) {
      if (!info || !info.planStartTime) {
        return;
      }
      const earliest = this.planEarliestSubmit(info.planStartTime, info.manhour);
      if (!earliest) {
        return;
      }
      const submit = info.planSubmissionTime ? new Date(info.planSubmissionTime) : null;
      if (!submit || isNaN(submit.getTime()) || submit.getTime() < earliest.getTime()) {
        info.planSubmissionTime = earliest;
      }
    },

    /** 计划区间内执行人的日均负荷预览，结果写入 this.loadPreview（需组件 data 中声明） */
    fetchPlanLoadPreview(info) {
      if (!info || !info.executorId || !info.planStartTime || !info.planSubmissionTime) {
        this.loadPreview = null;
        return;
      }
      const payload = {
        id: info.id,
        executorId: info.executorId,
        manhour: Number(info.manhour || 0),
        planStartTime: this.formatLocalYmd(info.planStartTime),
        planSubmissionTime: this.formatLocalYmd(info.planSubmissionTime),
        customerId: this.loginCustomer && this.loginCustomer.id,
      };
      const seq = (this.loadPreviewSeq || 0) + 1;
      this.loadPreviewSeq = seq;
      this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/gantt/plan/loadPreview', payload).then((response) => {
        if (seq !== this.loadPreviewSeq) {
          return;
        }
        const resp = response.data;
        this.loadPreview = resp.success ? resp.content : null;
      }).catch(() => {
        this.loadPreview = null;
      });
    },

    /**
     * 拉取全部执行人到 this.executors（需组件 data 中声明 executors）
     */
    listExecutors() {
      const customerId = this.loginCustomer && this.loginCustomer.id;
      if (!customerId) {
        return;
      }
      this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/worker/listAllWorkers/' + customerId).then((response) => {
        const resp = response.data;
        if (resp.success) {
          this.executors = resp.content;
        } else if (typeof Toast !== 'undefined') {
          Toast.warning(resp.message, Swal);
        }
      });
    },

    /**
     * 核心字段校验。options 可关闭某项：
     * { requireManhour, requirePlanSubmissionTime, requireExecutor, requireTaskDescription, requireJobName }
     * @returns {boolean} true 表示通过
     */
    validateJobCoreFields(info, options = {}) {
      const opts = Object.assign({
        requireJobName: true,
        requireExecutor: true,
        requireTaskDescription: true,
        requirePlanSubmissionTime: true,
        requireManhour: true,
        jobNameMin: 5,
        jobNameMax: 50,
        taskDescriptionMin: 10,
        taskDescriptionMax: 500,
      }, options || {});

      if (!info) {
        return false;
      }
      if (opts.requireJobName && !Validator.require(info.jobName, '任务名称', Swal)) {
        return false;
      }
      if (opts.requireExecutor && !Validator.require(info.executorId, '工作执行人', Swal)) {
        return false;
      }
      if (opts.requireTaskDescription && !Validator.require(info.taskDescription, '任务描述', Swal)) {
        return false;
      }
      if (opts.requirePlanSubmissionTime && !Validator.require(info.planSubmissionTime, '提交时间', Swal)) {
        return false;
      }
      if (opts.requireManhour && !Validator.require(info.manhour, '任务工时', Swal)) {
        return false;
      }
      if (opts.requireJobName && opts.jobNameMin != null
          && !Validator.length(info.jobName, '任务名称', opts.jobNameMin, opts.jobNameMax, Swal)) {
        return false;
      }
      if (opts.requireTaskDescription && opts.taskDescriptionMin != null
          && !Validator.length(info.taskDescription, '任务描述', opts.taskDescriptionMin, opts.taskDescriptionMax, Swal)) {
        return false;
      }
      return true;
    },

    /**
     * 写入保存时公共元数据
     */
    applyJobSaveMeta(target, extra = {}) {
      if (!target) {
        return target;
      }
      target.customerId = this.loginCustomer && this.loginCustomer.id;
      target.loginUserId = this.loginUser && this.loginUser.customerUserId;
      Object.keys(extra || {}).forEach((k) => {
        target[k] = extra[k];
      });
      return target;
    },
  },
};
