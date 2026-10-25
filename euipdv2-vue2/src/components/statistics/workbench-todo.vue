<!--
 * Author: 周朔鹏
 * Date: 2026-10-12
 * Email: 391902958@qq.com
 * Website: https://www.euipd.com
-->

<template>
  <div class="wbt" v-loading="loading">
    <div class="wbt-group" v-for="g in groups" :key="g.title">
      <div class="wbt-group-head">
        <span class="wbt-group-title">{{ g.title }}</span>
        <span class="wbt-group-total" :class="{'wbt-zero': !g.total}">{{ g.total }}</span>
      </div>
      <div v-if="g.items.length" class="wbt-items">
        <a v-for="it in g.items" :key="it.key" class="wbt-item" @click="go(it)">
          <span>{{ it.title }}</span>
          <el-badge v-if="it.count" :value="it.count" :max="99" class="wbt-badge"></el-badge>
          <el-badge v-else-if="it.dot" is-dot class="wbt-badge"></el-badge>
        </a>
      </div>
      <div v-else class="wbt-empty">暂无</div>
    </div>
  </div>
</template>

<script>
/*
 * 待办块。计数取自侧栏同源的 badge-summary，跳转路由与可见性取自 RoleAccess.SIDEBAR_MENUS，
 * 侧栏不显示的菜单在此也不显示。
 */
const GROUPS = [
  {
    title: '待我审批',
    entries: [
      { menu: 'setup', label: '立项审批', counts: ['setupCount'] },
      // 研发管理与项目管理两处列出同一批研发领料/费用单，合计只取两者较大值
      { menu: 'project', label: '项目物料与费用', counts: ['materialAppNum_project', 'feeAppNum_project'], totalGroup: 'rdApp' },
      { menu: 'develop', label: '研发物料与费用', counts: ['materialAppNum', 'feeAppNum'], totalGroup: 'rdApp' },
      { menu: 'budget', label: '预算调整', counts: ['budgetManageJobNum'] },
      { menu: 'quality', label: '质量物料与费用', counts: ['qualityMaterialApplicationNum', 'qualityFeeApplicationNum'] },
      { menu: 'file', label: '档案调阅', counts: ['waitForApproveNum'] },
    ],
  },
  {
    title: '待我评审',
    entries: [
      { menu: 'review', label: '评审管理', counts: ['reviewJobNum', 'reviewRelatedJobNum', 'reviewSubprojectNum', 'reviewMilestoneJobNum'] },
    ],
  },
  {
    title: '待我测试与复核',
    entries: [
      { menu: 'test', label: '测试任务', counts: ['testManageJobNum'] },
      { menu: 'function', label: '系统工程', counts: ['functionManageJobNum'] },
      { menu: 'qualitydefine', label: '质量复核', counts: ['qualityRecheckNum'] },
      { menu: 'quality', label: '质量问题处理', counts: ['qualitySubmitNum', 'qualityDefineSubmitNum'] },
    ],
  },
  {
    title: '我的任务',
    entries: [
      { menu: 'manhour', label: '执行管理待办', counts: ['jobCount'] },
      { menu: 'develop', label: '研发分解', counts: ['developNum'] },
    ],
  },
  {
    title: '预警与通知',
    entries: [
      { menu: 'manhour', label: '我的逾期工单', counts: ['__overdue'] },
      { menu: 'project', label: '项目通知', counts: ['projectNotifyNum'] },
      { menu: 'product', label: '产品通知', counts: ['productNotifyNum'] },
      { menu: 'character', label: '预立项', counts: ['updateCharacterNum'],
        flags: ['characterJobFlag', 'characterJobFlag_evaluate', 'demandJobFlag'] },
    ],
  },
];

export default {
  name: "workbench-todo",
  props: {
    // 本人当前逾期工单数，由工作台传入
    overdueNum: { type: Number, default: 0 },
  },

  data() {
    return {
      loading: false,
      summary: {},
    };
  },

  computed: {
    groups() {
      const roleList = (Tool.getLoginUser() || {}).roleList || [];
      const values = Object.assign({}, this.summary, { __overdue: this.overdueNum });
      return GROUPS.map(g => {
        const items = g.entries
          .filter(e => typeof RoleAccess !== 'undefined' && RoleAccess.canShowSidebar(e.menu, { roleList: roleList }))
          .map(e => ({
            key: e.menu + ':' + e.label,
            title: e.label,
            menu: e.menu,
            count: e.counts.reduce((sum, f) => sum + (Number(values[f]) || 0), 0),
            dot: (e.flags || []).some(f => values[f] === true),
            totalGroup: e.totalGroup,
          }))
          .filter(it => it.count > 0 || it.dot);
        const shared = {};
        const sharedSize = {};
        let total = 0;
        items.forEach(it => {
          if (it.totalGroup) {
            shared[it.totalGroup] = Math.max(shared[it.totalGroup] || 0, it.count);
            sharedSize[it.totalGroup] = (sharedSize[it.totalGroup] || 0) + 1;
          } else {
            total += it.count;
          }
        });
        // 两处都可见时用后端去重合计，只见一处时取该处计数
        if (sharedSize.rdApp > 1 && typeof values.rdAppDistinctNum === 'number') {
          shared.rdApp = values.rdAppDistinctNum;
        }
        Object.keys(shared).forEach(k => { total += shared[k]; });
        return { title: g.title, items: items, total: total };
      });
    },
  },

  mounted() {
    this.load();
  },

  methods: {
    load() {
      const user = Tool.getActorUser() || {};
      if (!user.customerUserId) {
        return;
      }
      this.loading = true;
      const url = process.env.VUE_APP_SERVER + '/customer/notify/badge-summary/' + user.customerUserId;
      this.$ajax.get(url, { skipScopeCheck: true }).then(response => {
        const body = (response && response.data) || {};
        this.summary = body.success && body.content ? body.content : {};
      }).catch(() => {
        this.summary = {};
      }).finally(() => {
        this.loading = false;
      });
    },

    go(item) {
      const def = RoleAccess.SIDEBAR_MENUS[item.menu];
      if (def) {
        this.$router.push('/' + def.page);
      }
    },
  },
}
</script>

<style scoped>
.wbt {
  display: flex;
  gap: 10px;
  min-height: 92px;
}

.wbt-group {
  flex: 1;
  min-width: 0;
  background: #f5f7fa;
  border-radius: 4px;
  padding: 8px 10px;
}

.wbt-group-head {
  display: flex;
  justify-content: space-between;
  align-items: baseline;
  margin-bottom: 6px;
}

.wbt-group-title {
  font-size: 13px;
  color: #606266;
}

.wbt-group-total {
  font-size: 20px;
  font-weight: 600;
  color: #E6A23C;
}

.wbt-group-total.wbt-zero {
  color: #C0C4CC;
}

.wbt-items {
  display: flex;
  flex-wrap: wrap;
  gap: 4px 14px;
}

.wbt-item {
  font-size: 13px;
  cursor: pointer;
  white-space: nowrap;
}

.wbt-badge {
  margin-left: 2px;
  vertical-align: top;
}

.wbt-empty {
  font-size: 12px;
  color: #C0C4CC;
}
</style>
