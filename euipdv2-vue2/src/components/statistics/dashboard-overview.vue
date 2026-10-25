<!--
 * Author: 周朔鹏
 * Date: 2026-10-12
 * Email: 391902958@qq.com
 * Website: https://www.euipd.com
-->

<template>
  <div class="dbo" v-loading="loading">
    <div v-if="denied" class="dbo-denied">管理驾驶舱仅对机构级与部门负责人开放，您可在“项目仪表盘”“组织工作量”查看所辖范围的数据。</div>
    <template v-else>
      <div class="dbo-toolbar">
        <span class="dbo-scope">数据范围：{{ levelName }}，{{ o.month || '' }} 月</span>
        <span v-if="o.unassignedMemberCount" class="dbo-warn">
          <i class="el-icon-warning-outline"></i> {{ o.unassignedMemberCount }} 人未分配部门，部门负荷中不含这部分人员
        </span>
        <el-button size="small" icon="el-icon-refresh" @click="load">刷新</el-button>
      </div>

      <el-row :gutter="10">
        <el-col :span="4" v-for="k in kpis" :key="k.label">
          <div class="dbo-kpi" :class="{'dbo-kpi-link': !!k.click}" @click="k.click && k.click()">
            <div class="dbo-kpi-value" :class="k.cls">{{ k.value }}</div>
            <div class="dbo-kpi-label">{{ k.label }}</div>
          </div>
        </el-col>
      </el-row>

      <el-row :gutter="12">
        <el-col :span="8">
          <div class="dbo-section">在研项目健康分布</div>
          <stat-chart type="doughnut" :height="200" :labels="healthLabels"
                      :datasets="[{label: '项目数', data: healthValues, colors: healthColors}]"
                      empty-text="范围内没有在研项目" @point-click="i => goProjects(fmt.HEALTH_LEVELS[i])"></stat-chart>
          <div class="dbo-health">
            <a v-for="h in fmt.HEALTH_LEVELS" :key="h" class="dbo-health-item" @click="goProjects(h)">
              <el-tag size="small" :type="fmt.healthType(h)" effect="dark">{{ fmt.healthName(h) }}</el-tag>
              <span class="dbo-health-num">{{ health[h] || 0 }}</span>
            </a>
          </div>
        </el-col>
        <el-col :span="16">
          <div class="dbo-section">部门负荷<span class="dbo-section-note">本月，按负荷率降序；点击进入组织工作量</span></div>
          <el-table :data="o.departments || []" size="mini" border stripe max-height="260" empty-text="没有下级部门"
                    class="dbo-link-table" @row-click="row => $emit('go-department', row.id)">
            <el-table-column prop="name" label="部门" min-width="120" show-overflow-tooltip></el-table-column>
            <el-table-column prop="managerName" label="负责人" width="80"></el-table-column>
            <el-table-column prop="memberCount" label="成员" width="56" align="right"></el-table-column>
            <el-table-column prop="inProgressNum" label="在办" width="56" align="right"></el-table-column>
            <el-table-column label="逾期" width="56" align="right">
              <template slot-scope="s"><span :class="{'dbo-danger': s.row.overdueNum > 0}">{{ s.row.overdueNum }}</span></template>
            </el-table-column>
            <el-table-column label="负荷率" min-width="150">
              <template slot-scope="s">
                <el-progress :percentage="bar(s.row.loadRate)" :stroke-width="10" :show-text="false"
                             :color="loadColor(s.row.loadRate)" class="dbo-bar"></el-progress>
                <span class="dbo-bar-text">{{ fmt.percent(s.row.loadRate, 0) }}</span>
              </template>
            </el-table-column>
            <el-table-column label="未来5个工作日日均负荷" width="150" align="right">
              <template slot-scope="s">{{ fmt.hours(s.row.weekDayLoad) }}</template>
            </el-table-column>
          </el-table>
        </el-col>
      </el-row>

      <el-row :gutter="12">
        <el-col :span="14">
          <div class="dbo-section">近 6 个月趋势<span class="dbo-section-note">带“*”的月份取自月末快照，当月为实时</span></div>
          <stat-chart type="bar" rate-axis y-label="工时 / 工单数" :height="250" :labels="trendLabels"
                      :datasets="trendDatasets" empty-text="近 6 个月无交付"></stat-chart>
          <stat-chart type="line" y-label="元" :height="170" :labels="trendLabels"
                      :datasets="[{label: '人员费', color: '#E6A23C', data: trend.map(t => Number(t.staffFee || 0))}]"
                      empty-text="近 6 个月无人员费"></stat-chart>
        </el-col>
        <el-col :span="10">
          <div class="dbo-section">本月绩效排行<span class="dbo-section-note">实时绩效金额前十</span></div>
          <el-table :data="o.topPerformers || []" size="mini" border stripe empty-text="本月暂无绩效">
            <el-table-column type="index" label="排名" width="50" align="center"></el-table-column>
            <el-table-column prop="trueName" label="姓名" width="80"></el-table-column>
            <el-table-column prop="departmentName" label="部门" min-width="90" show-overflow-tooltip></el-table-column>
            <el-table-column label="交付标准工时" width="100" align="right">
              <template slot-scope="s">{{ fmt.hours(s.row.deliverStandHours) }}</template>
            </el-table-column>
            <el-table-column label="绩效" width="96" align="right">
              <template slot-scope="s">{{ fmt.money(s.row.performance, 0) }}</template>
            </el-table-column>
            <el-table-column label="评分" width="60" align="right">
              <template slot-scope="s">{{ fmt.score(s.row.score) }}</template>
            </el-table-column>
          </el-table>
        </el-col>
      </el-row>

      <div class="dbo-note">
        在研项目为已立项至集成阶段（含暂停），不含完成、关闭与归档；预算执行率 = 在研项目已发生费用 ÷ 预算合计。
        在办、逾期为当前值，交付工时、人员费与绩效按本月完成的工单计。数字与“项目仪表盘”“组织工作量”同口径。
      </div>
    </template>
  </div>
</template>

<script>
import Swal from "sweetalert2";
import StatChart from "@/components/statistics/stat-chart.vue";
import * as fmt from "@/shared/stat-format";
import { getDashboardOverview } from "@/shared/statistics-api";

const LEVEL_NAMES = { ORG: '全机构', DEPARTMENT: '所辖部门' };

export default {
  name: "dashboard-overview",
  components: { StatChart },

  data() {
    return {
      fmt: fmt,
      o: {},
      loading: false,
      denied: false,
    };
  },

  computed: {
    levelName() {
      return LEVEL_NAMES[this.o.level] || this.o.level || '';
    },
    health() {
      return this.o.health || {};
    },
    healthLabels() {
      return fmt.HEALTH_LEVELS.map(h => fmt.healthName(h));
    },
    healthValues() {
      return fmt.HEALTH_LEVELS.map(h => this.health[h] || 0);
    },
    healthColors() {
      return fmt.HEALTH_LEVELS.map(h => fmt.healthColor(h));
    },
    kpis() {
      const o = this.o;
      return [
        { label: '在研项目', value: fmt.count(o.activeProjectNum), click: () => this.goProjects('') },
        { label: '在办工单', value: fmt.count(o.inProgressNum) },
        { label: '当前逾期', value: fmt.count(o.overdueNum), cls: o.overdueNum > 0 ? 'dbo-danger' : '' },
        { label: '本月交付标准工时', value: fmt.hours(o.deliverStandHours) },
        { label: '本月人员费', value: fmt.money(o.staffFee, 0) },
        { label: '在研项目预算执行率', value: fmt.percent(o.budgetExeRate) },
      ];
    },
    trend() {
      return this.o.trend || [];
    },
    trendLabels() {
      return this.trend.map(t => t.month + (t.source === 'SNAPSHOT' ? '*' : ''));
    },
    trendDatasets() {
      return [
        { label: '交付标准工时', data: this.trend.map(t => t.deliverStandHours || 0) },
        { label: '完成工单', color: '#67C23A', data: this.trend.map(t => t.completedNum || 0) },
        { label: '延期完成占比', type: 'line', yAxisID: 'rate', color: '#F56C6C',
          data: this.trend.map(t => Number(t.overdueCompletedRate || 0)) },
      ];
    },
  },

  mounted() {
    this.load();
  },

  methods: {
    load() {
      this.loading = true;
      getDashboardOverview().then(content => {
        this.o = content || {};
        this.denied = false;
      }).catch(err => {
        if (err && err.response && err.response.code === 'NO_PERMISSION') {
          this.denied = true;
        } else {
          Toast.warning(err.message, Swal);
        }
      }).finally(() => {
        this.loading = false;
      });
    },

    goProjects(health) {
      this.$emit('go-projects', { status: 'ACTIVE', health: health || '' });
    },

    bar(rate) {
      return Math.max(0, Math.min(100, Math.round((Number(rate) || 0) * 100)));
    },

    loadColor(rate) {
      const level = fmt.loadLevel(rate);
      return level === 'danger' ? '#F56C6C' : (level === 'warning' ? '#E6A23C' : '#67C23A');
    },
  }
}
</script>

<style scoped>
.dbo-toolbar {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 10px;
}

.dbo-scope {
  color: #606266;
  font-size: 13px;
}

.dbo-warn {
  color: #E6A23C;
  font-size: 12px;
}

.dbo-kpi {
  background: #f5f7fa;
  border-radius: 4px;
  padding: 12px 6px;
  text-align: center;
}

.dbo-kpi-link {
  cursor: pointer;
}

.dbo-kpi-link:hover {
  background: #ecf5ff;
}

.dbo-kpi-value {
  font-size: 22px;
  font-weight: 600;
  color: #303133;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.dbo-kpi-label {
  font-size: 12px;
  color: #909399;
  margin-top: 2px;
}

.dbo-section {
  font-size: 15px;
  font-weight: 600;
  margin: 14px 0 8px;
}

.dbo-section-note {
  font-size: 12px;
  font-weight: normal;
  color: #909399;
  margin-left: 10px;
}

.dbo-health {
  display: flex;
  justify-content: space-around;
  margin-top: 8px;
}

.dbo-health-item {
  cursor: pointer;
}

.dbo-health-num {
  font-size: 18px;
  font-weight: 600;
  margin-left: 6px;
  vertical-align: middle;
}

.dbo-link-table >>> .el-table__row {
  cursor: pointer;
}

.dbo-bar {
  display: inline-block;
  width: calc(100% - 46px);
  vertical-align: middle;
}

.dbo-bar-text {
  display: inline-block;
  width: 42px;
  text-align: right;
  font-size: 12px;
}

.dbo-danger {
  color: #F56C6C;
}

.dbo-note {
  margin-top: 10px;
  color: #909399;
  font-size: 12px;
  line-height: 1.8;
}

.dbo-denied {
  padding: 60px 0;
  text-align: center;
  color: #909399;
}
</style>
