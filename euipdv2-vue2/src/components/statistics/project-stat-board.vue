<!--
 * Author: 周朔鹏
 * Date: 2026-10-12
 * Email: 391902958@qq.com
 * Website: https://www.euipd.com
-->

<template>
  <div class="psb">
    <div class="psb-toolbar">
      <el-select v-model="filter.status" size="small" clearable placeholder="全部状态" style="width: 120px" @change="search">
        <el-option v-for="s in statusOptions" :key="s" :label="statusName(s)" :value="s"></el-option>
      </el-select>
      <el-select v-model="filter.health" size="small" clearable placeholder="全部健康度" style="width: 110px" @change="search">
        <el-option v-for="h in fmt.HEALTH_LEVELS" :key="h" :label="fmt.healthName(h)" :value="h"></el-option>
      </el-select>
      <el-select v-model="filter.managerId" size="small" filterable clearable placeholder="全部经理" style="width: 130px"
                 @change="search">
        <el-option v-for="m in managerOptions" :key="m.id" :label="m.name" :value="m.id"></el-option>
      </el-select>
      <el-date-picker v-model="filter.release" type="daterange" size="small" value-format="yyyy-MM-dd" range-separator="至"
                      start-placeholder="发布自" end-placeholder="发布至" style="width: 240px" @change="search"></el-date-picker>
      <el-input v-model="filter.keyword" size="small" clearable placeholder="项目名称或编号" style="width: 180px"
                prefix-icon="el-icon-search" @keyup.enter.native="search" @clear="search"></el-input>
      <el-button size="small" icon="el-icon-refresh" @click="load">刷新</el-button>
      <span class="psb-tip">
        共 {{ total }} 个项目
        <template v-for="h in fmt.HEALTH_LEVELS">
          <el-tag :key="h" size="mini" :type="fmt.healthType(h)" class="psb-tip-tag">
            {{ fmt.healthName(h) }} {{ healthCount[h] || 0 }}
          </el-tag>
        </template>
        <span class="psb-tip-note">（本页）</span>
      </span>
    </div>

    <div v-loading="loading" class="psb-grid-box">
      <el-row :gutter="12">
        <el-col :xs="24" :sm="12" :md="8" :lg="6" v-for="c in cards" :key="c.id">
          <div class="psb-card" :style="{borderTopColor: fmt.healthColor(c.health)}" @click="open(c)">
            <div class="psb-card-head">
              <span class="psb-card-name" :title="c.name">{{ c.name }}</span>
              <el-tooltip :disabled="!c.healthReasons || !c.healthReasons.length"
                          :content="(c.healthReasons || []).join('；')" placement="top">
                <el-tag size="mini" :type="fmt.healthType(c.health)" effect="dark">{{ fmt.healthName(c.health) }}</el-tag>
              </el-tooltip>
            </div>
            <div class="psb-card-sub">
              {{ c.projectCode }} · {{ statusName(c.status) }} · 项目经理 {{ c.projectManagerName || '-' }}
            </div>
            <div class="psb-metrics">
              <div class="psb-metric">
                <div class="psb-metric-value">{{ fmt.percent(c.completeRate, 0) }}</div>
                <div class="psb-metric-label">完工率 {{ c.completedNum }}/{{ c.totalNum }}</div>
              </div>
              <div class="psb-metric">
                <div class="psb-metric-value" :class="{'psb-danger': c.overdueNum > 0}">{{ c.overdueNum }}</div>
                <div class="psb-metric-label">逾期工单</div>
              </div>
              <div class="psb-metric">
                <div class="psb-metric-value">{{ fmt.percent(c.budgetExeRate, 0) }}</div>
                <div class="psb-metric-label">预算执行率</div>
              </div>
              <div class="psb-metric">
                <div class="psb-metric-value" :class="{'psb-danger': c.toReleaseDay < 0}">{{ releaseText(c) }}</div>
                <div class="psb-metric-label">距发布</div>
              </div>
            </div>
            <el-progress :percentage="progress(c.completeRate)" :stroke-width="6" :show-text="false"></el-progress>
            <div class="psb-card-foot">
              <span v-if="c.overdueMilestoneNum" class="psb-danger">超期未评审里程碑 {{ c.overdueMilestoneNum }} 个</span>
              <span v-else-if="c.nextMilestoneName">下一里程碑：{{ c.nextMilestoneName }} {{ fmt.date(c.nextMilestoneTime) }}</span>
              <span v-else>无待评审里程碑</span>
            </div>
          </div>
        </el-col>
      </el-row>
      <div v-if="!loading && !cards.length" class="psb-empty">没有符合条件的项目</div>
    </div>

    <el-pagination class="psb-pager" background layout="total, sizes, prev, pager, next" :total="total"
                   :page-size="size" :page-sizes="[12, 24, 48]" :current-page="page"
                   @current-change="p => { page = p; load(); }"
                   @size-change="s => { size = s; page = 1; load(); }"></el-pagination>
    <div class="psb-note">
      健康度：当前逾期工单超过在办 20%、预算执行率超出时间进度 20 个百分点或有超期未评审里程碑为“预警”，前两项超过 10% 为“关注”。
      工单指标不含通知类、计划、取消与拒绝的工单。
    </div>

    <project-stat-detail-drawer :visible="drawerVisible" :project-id="current.id" :title="current.name"
                                :show-performance="managerial"
                                @close="drawerVisible = false"></project-stat-detail-drawer>
  </div>
</template>

<script>
import Swal from "sweetalert2";
import { optionKV } from "@/filter/filter";
import ProjectStatDetailDrawer from "@/components/statistics/project-stat-detail-drawer.vue";
import * as fmt from "@/shared/stat-format";
import { getScope, listProjectCards } from "@/shared/statistics-api";

const ACTIVE = 'ACTIVE';
const STAT_STATUSES = [ACTIVE, 'SETUPED', 'ONGOING', 'OUT_SOURCE', 'DEV_COMPLETE', 'REVIEW_SUBP', 'REVIEW_PRO', 'EN_REVIEW',
  'INTEGRATION', 'SUSPEND', 'COMPLETE', 'CLOSE', 'ARCHIVED'];

export default {
  name: "project-stat-board",
  components: { ProjectStatDetailDrawer },
  props: {
    // 由驾驶舱跳转时预置的筛选：{status, health, keyword}
    preset: { type: Object, default: null },
  },

  data() {
    return {
      fmt: fmt,
      statusOptions: STAT_STATUSES,
      filter: { status: '', health: '', managerId: '', release: null, keyword: '' },
      scopeLevel: '',
      cards: [],
      total: 0,
      page: 1,
      size: 12,
      loading: false,
      seq: 0,
      managers: {},
      drawerVisible: false,
      current: { id: '', name: '' },
    };
  },

  computed: {
    managerial() {
      return fmt.isManagerial(this.scopeLevel);
    },
    managerOptions() {
      return Object.keys(this.managers).map(id => ({ id: id, name: this.managers[id] }))
        .sort((a, b) => a.name.localeCompare(b.name, 'zh-CN'));
    },
    healthCount() {
      const count = {};
      this.cards.forEach(c => {
        count[c.health] = (count[c.health] || 0) + 1;
      });
      return count;
    },
  },

  watch: {
    preset: {
      immediate: true,
      handler(p) {
        if (p) {
          this.filter.status = p.status || '';
          this.filter.health = p.health || '';
          this.filter.keyword = p.keyword || '';
          this.search();
        }
      }
    }
  },

  mounted() {
    if (!this.preset) {
      this.load();
    }
    getScope().then(scope => {
      this.scopeLevel = (scope && scope.level) || '';
    }).catch(() => {});
  },

  methods: {
    statusName(status) {
      if (status === ACTIVE) {
        return '在研（全部）';
      }
      return status ? optionKV(PROJECT_STATUS, status) : '-';
    },

    releaseText(c) {
      if (c.toReleaseDay === null || c.toReleaseDay === undefined) {
        return '-';
      }
      return c.toReleaseDay < 0 ? '超 ' + (-c.toReleaseDay) + ' 天' : c.toReleaseDay + ' 天';
    },

    progress(rate) {
      return Math.max(0, Math.min(100, Math.round((Number(rate) || 0) * 100)));
    },

    search() {
      this.page = 1;
      this.load();
    },

    load() {
      const seq = ++this.seq;
      const release = this.filter.release || [];
      this.loading = true;
      listProjectCards({
        status: this.filter.status || null,
        health: this.filter.health || null,
        managerId: this.filter.managerId || null,
        releaseFrom: release[0] || null,
        releaseTo: release[1] || null,
        keyword: this.filter.keyword ? this.filter.keyword.trim() : null,
        page: this.page,
        size: this.size,
      }).then(pageDto => {
        if (seq !== this.seq) {
          return;
        }
        this.cards = (pageDto && pageDto.list) || [];
        this.total = (pageDto && pageDto.total) || 0;
        this.collectManagers(this.cards);
      }).catch(err => {
        Toast.warning(err.message, Swal);
      }).finally(() => {
        if (seq === this.seq) {
          this.loading = false;
        }
      });
    },

    // 经理筛选项取自已加载过的卡片，按产品、项目、测试经理任一匹配
    collectManagers(cards) {
      const next = Object.assign({}, this.managers);
      cards.forEach(c => {
        [[c.productManagerId, c.productManagerName], [c.projectManagerId, c.projectManagerName],
          [c.testManagerId, c.testManagerName]].forEach(([id, name]) => {
          if (id && name) {
            next[id] = name;
          }
        });
      });
      this.managers = next;
    },

    open(card) {
      this.current = { id: card.id, name: card.name };
      this.drawerVisible = true;
    },
  }
}
</script>

<style scoped>
.psb-toolbar {
  margin-bottom: 10px;
}

.psb-toolbar > * {
  margin-right: 8px;
  vertical-align: middle;
}

.psb-tip {
  color: #909399;
  font-size: 12px;
}

.psb-tip-tag {
  margin-left: 6px;
}

.psb-tip-note {
  margin-left: 4px;
}

.psb-grid-box {
  min-height: 200px;
}

.psb-card {
  border: 1px solid #ebeef5;
  border-top: 3px solid #909399;
  border-radius: 4px;
  padding: 10px 12px;
  margin-bottom: 12px;
  cursor: pointer;
  background: #fff;
  transition: box-shadow .2s;
}

.psb-card:hover {
  box-shadow: 0 2px 12px rgba(0, 0, 0, .1);
}

.psb-card-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.psb-card-name {
  flex: 1;
  min-width: 0;
  font-size: 15px;
  font-weight: 600;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  margin-right: 6px;
}

.psb-card-sub {
  font-size: 12px;
  color: #909399;
  margin: 4px 0 8px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.psb-metrics {
  display: flex;
  justify-content: space-between;
  margin-bottom: 6px;
}

.psb-metric {
  flex: 1;
  text-align: center;
}

.psb-metric-value {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  white-space: nowrap;
}

.psb-metric-label {
  font-size: 11px;
  color: #909399;
  white-space: nowrap;
}

.psb-card-foot {
  margin-top: 6px;
  font-size: 12px;
  color: #606266;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.psb-danger {
  color: #F56C6C;
}

.psb-empty {
  text-align: center;
  color: #909399;
  padding: 60px 0;
}

.psb-pager {
  text-align: right;
}

.psb-note {
  margin-top: 6px;
  color: #909399;
  font-size: 12px;
}
</style>
