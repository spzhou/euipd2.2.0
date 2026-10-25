<!--
 * Author: 周朔鹏
 * Date: 2026-10-12
 * Email: 391902958@qq.com
 * Website: https://www.euipd.com
-->

<template>
  <el-drawer :visible="visible" direction="rtl" size="78%" :with-header="false" append-to-body
             @close="$emit('close')">
    <div class="psd" v-loading="loading">
      <div class="psd-head">
        <div class="psd-title">
          {{ card.name || title }}
          <el-tag size="small" :type="fmt.healthType(card.health)" effect="dark">{{ fmt.healthName(card.health) }}</el-tag>
          <span class="psd-sub">{{ card.projectCode }} · {{ statusName(card.status) }}</span>
        </div>
        <i class="el-icon-close psd-close" @click="$emit('close')"></i>
      </div>
      <div class="psd-sub psd-people">
        <span>产品经理：{{ card.productManagerName || '-' }}</span>
        <span>项目经理：{{ card.projectManagerName || '-' }}</span>
        <span>测试主管：{{ card.testManagerName || '-' }}</span>
        <span>立项：{{ fmt.date(card.setupedTime) }}</span>
        <span>发布：{{ fmt.date(card.releaseTime) }}</span>
      </div>
      <div v-if="card.healthReasons && card.healthReasons.length" class="psd-reasons">
        <i class="el-icon-warning-outline"></i> {{ card.healthReasons.join('；') }}
      </div>

      <el-row :gutter="8" class="psd-cards">
        <el-col :span="4" v-for="c in cards" :key="c.label">
          <div class="psd-card">
            <div class="psd-card-value" :class="c.cls">{{ c.value }}</div>
            <div class="psd-card-label">{{ c.label }}</div>
          </div>
        </el-col>
      </el-row>

      <template v-if="budget">
        <div class="psd-section">预算执行</div>
        <div v-for="b in budgetLines" :key="b.label" class="psd-budget">
          <div class="psd-budget-text">
            <span>{{ b.label }}</span>
            <span class="psd-muted">{{ fmt.money(b.active) }} / {{ fmt.money(b.total) }} 元，{{ fmt.percent(b.rate) }}</span>
          </div>
          <el-progress :percentage="clamp(b.rate)" :stroke-width="12" :show-text="false"
                       :color="b.rate > 1 ? '#F56C6C' : '#409EFF'"></el-progress>
        </div>
        <div v-if="budgetItems.length" class="psd-rings">
          <a v-for="item in budgetItems" :key="item.subprojectId" class="psd-ring" @click="openSubSummary(item)">
            <el-progress type="dashboard" :width="96" :percentage="clamp(item.budgetExeRate)" :color="ringColors"></el-progress>
            <div class="psd-ring-name" :title="item.projectName">{{ item.projectName }}</div>
          </a>
        </div>
      </template>

      <div class="psd-section">子项目分解</div>
      <el-table :data="subprojects" size="mini" border stripe empty-text="无子项目">
        <el-table-column label="子项目" min-width="160" show-overflow-tooltip>
          <template slot-scope="s">{{ s.row.name }}<span class="psd-muted"> {{ s.row.projectCode }}</span></template>
        </el-table-column>
        <el-table-column label="状态" width="80">
          <template slot-scope="s">{{ statusName(s.row.status) }}</template>
        </el-table-column>
        <el-table-column prop="totalNum" label="工单" width="56" align="right"></el-table-column>
        <el-table-column label="完工率" width="70" align="right">
          <template slot-scope="s">{{ fmt.percent(s.row.completeRate) }}</template>
        </el-table-column>
        <el-table-column prop="inProgressNum" label="在办" width="56" align="right"></el-table-column>
        <el-table-column label="逾期" width="56" align="right">
          <template slot-scope="s"><span :class="{'psd-danger': s.row.overdueNum > 0}">{{ s.row.overdueNum }}</span></template>
        </el-table-column>
        <el-table-column label="延期率" width="70" align="right">
          <template slot-scope="s">{{ fmt.percent(s.row.overdueRate) }}</template>
        </el-table-column>
        <el-table-column label="缺陷未结/总数" width="100" align="right">
          <template slot-scope="s">{{ s.row.defectOpenNum }} / {{ s.row.defectNum }}</template>
        </el-table-column>
        <el-table-column label="交付标准工时" width="100" align="right">
          <template slot-scope="s">{{ fmt.hours(s.row.deliverStandHours) }}</template>
        </el-table-column>
        <el-table-column label="预算执行率" width="90" align="right">
          <template slot-scope="s">{{ fmt.percent(s.row.budgetExeRate) }}</template>
        </el-table-column>
        <el-table-column label="目标里程碑" min-width="120" show-overflow-tooltip>
          <template slot-scope="s">
            <span v-if="s.row.overdueMilestoneNum" class="psd-danger">已超期未评审</span>
            <span v-else>{{ s.row.nextMilestoneName || '-' }} {{ s.row.nextMilestoneTime ? fmt.date(s.row.nextMilestoneTime) : '' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="健康度" width="70" align="center">
          <template slot-scope="s">
            <el-tooltip :disabled="!s.row.healthReasons || !s.row.healthReasons.length"
                        :content="(s.row.healthReasons || []).join('；')" placement="top">
              <el-tag size="mini" :type="fmt.healthType(s.row.health)">{{ fmt.healthName(s.row.health) }}</el-tag>
            </el-tooltip>
          </template>
        </el-table-column>
      </el-table>

      <div class="psd-section">
        成员工作量
        <stat-period-picker v-model="period" class="psd-period" @change="loadDetail"></stat-period-picker>
      </div>
      <el-table :data="members" size="mini" border stripe empty-text="当期无成员工作量"
                :default-sort="{prop: 'deliverStandHours', order: 'descending'}">
        <el-table-column prop="trueName" label="姓名" width="90"></el-table-column>
        <el-table-column prop="departmentName" label="部门" min-width="110" show-overflow-tooltip></el-table-column>
        <el-table-column prop="inProgressNum" label="进行中" width="66" align="right" sortable></el-table-column>
        <el-table-column prop="overdueNum" label="逾期" width="60" align="right" sortable></el-table-column>
        <el-table-column prop="completedNum" label="完成" width="60" align="right" sortable></el-table-column>
        <el-table-column prop="overdueCompletedNum" label="延期完成" width="80" align="right" sortable></el-table-column>
        <el-table-column prop="deliverStandHours" label="交付标准工时" width="104" align="right" sortable>
          <template slot-scope="s">{{ fmt.hours(s.row.deliverStandHours) }}</template>
        </el-table-column>
        <el-table-column prop="deliverWorkerHours" label="交付个人工时" width="104" align="right" sortable>
          <template slot-scope="s">{{ fmt.hours(s.row.deliverWorkerHours) }}</template>
        </el-table-column>
        <el-table-column prop="staffFee" label="人员费" width="96" align="right" sortable>
          <template slot-scope="s">{{ fmt.money(s.row.staffFee) }}</template>
        </el-table-column>
        <el-table-column v-if="showPerformance" prop="projectPerformance" label="项目内绩效" width="100" align="right" sortable>
          <template slot-scope="s">{{ fmt.money(s.row.projectPerformance) }}</template>
        </el-table-column>
        <el-table-column prop="projectScore" label="项目内评分" width="90" align="right">
          <template slot-scope="s">{{ fmt.score(s.row.projectScore) }}</template>
        </el-table-column>
      </el-table>
      <div class="psd-note">
        期间内数据：进行中、逾期为当前值，完成与工时按完成时间计入期间；项目内{{ showPerformance ? '绩效与' : '' }}评分为项目累计值，不随期间变化。
      </div>

      <div class="psd-section">近 12 个月趋势</div>
      <el-row :gutter="12">
        <el-col :span="14">
          <stat-chart type="bar" rate-axis y-label="数量 / 小时" :height="240" :labels="trendLabels"
                      :datasets="trendDatasets" empty-text="近 12 个月无完成工单"></stat-chart>
        </el-col>
        <el-col :span="10">
          <stat-chart type="line" y-label="元" :height="240" :labels="trendLabels"
                      :datasets="feeDatasets" empty-text="近 12 个月无人员费"></stat-chart>
        </el-col>
      </el-row>
      <div class="psd-note">带“*”的月份取自月末快照，其余为实时计算；预算执行率只在快照月份与当月给出。</div>
    </div>

    <el-dialog :visible.sync="subSummaryVisible" width="40cm" append-to-body :close-on-click-modal="false">
      <div slot="title" style="font-size: 20px;">项目执行信息汇总</div>
      <subproject-summary-tabs v-if="subSummaryVisible" :key="subSummaryId" :subproject-id="subSummaryId"
                               :project-type="PROJECT_TYPE.SUB_PROJECT.key"></subproject-summary-tabs>
    </el-dialog>
  </el-drawer>
</template>

<script>
import Swal from "sweetalert2";
import { optionKV } from "@/filter/filter";
import StatChart from "@/components/statistics/stat-chart.vue";
import StatPeriodPicker from "@/components/statistics/stat-period-picker.vue";
import * as fmt from "@/shared/stat-format";
import { getProjectDetail, getProjectTrend } from "@/shared/statistics-api";

export default {
  name: "project-stat-detail-drawer",
  components: {
    StatChart,
    StatPeriodPicker,
    SubprojectSummaryTabs: () => import("@/components/std/subproject-summary-tabs.vue"),
  },
  props: {
    visible: { type: Boolean, default: false },
    projectId: { type: String, default: '' },
    title: { type: String, default: '' },
    // 绩效金额仅对机构级、部门级显示
    showPerformance: { type: Boolean, default: false },
  },

  data() {
    return {
      fmt: fmt,
      PROJECT_TYPE: PROJECT_TYPE,
      period: { periodType: 'YEAR', periodValue: String(new Date().getFullYear()) },
      detail: null,
      trend: [],
      loading: false,
      seq: 0,
      subSummaryVisible: false,
      subSummaryId: '',
      ringColors: [
        { color: '#67C23A', percentage: 80 },
        { color: '#E6A23C', percentage: 100 },
        { color: '#F56C6C', percentage: 101 },
      ],
    };
  },

  computed: {
    card() {
      return (this.detail && this.detail.project) || {};
    },
    subprojects() {
      return (this.detail && this.detail.subprojects) || [];
    },
    members() {
      return (this.detail && this.detail.members) || [];
    },
    budget() {
      return this.detail && this.detail.budget;
    },
    budgetItems() {
      return (this.budget && this.budget.subprojectStatisticsItemList) || [];
    },
    budgetLines() {
      const b = this.budget || {};
      const line = (label, active, total) => ({
        label: label, active: active, total: total,
        rate: Number(total) > 0 ? Number(active || 0) / Number(total) : null,
      });
      return [
        line('预算总执行（支出 + 应付未付）', b.sumActiveBudget, b.sumBudget),
        line('人员费（工时费累计）', b.sumActiveManhourBudget, b.sumManhourBudget),
        line('物料费（批准的费用化物料）', b.sumActiveMaterialBudget, b.sumMaterialBudget),
        line('设备费（已批准）', b.sumActiveEquipmentBudget, b.sumEquipmentBudget),
        line('其他费用（已批准）', b.sumActiveOtherBudget, b.sumOtherBudget),
      ];
    },
    cards() {
      const c = this.card;
      const budgetVisible = c.budgetExeRate !== null && c.budgetExeRate !== undefined;
      const list = [
        { label: '工单总数 / 完成', value: fmt.count(c.totalNum) + ' / ' + fmt.count(c.completedNum) },
        { label: '完工率', value: fmt.percent(c.completeRate) },
        { label: '在办 / 逾期', value: fmt.count(c.inProgressNum) + ' / ' + fmt.count(c.overdueNum), cls: c.overdueNum > 0 ? 'psd-danger' : '' },
        { label: '延期率', value: fmt.percent(c.overdueRate) },
        { label: '缺陷未结 / 总数', value: fmt.count(c.defectOpenNum) + ' / ' + fmt.count(c.defectNum) },
        { label: '交付标准工时', value: fmt.hours(c.deliverStandHours) },
        { label: '提交文档数', value: fmt.count(c.docNum) },
        { label: '人员费 已付 / 未付', value: fmt.money(c.staffFeePaid, 0) + ' / ' + fmt.money(c.staffFeeUnpaid, 0) },
        { label: '时间进度', value: fmt.percent(c.timeProgress) },
        { label: '发布倒计时', value: c.toReleaseDay === null || c.toReleaseDay === undefined ? '-' : c.toReleaseDay + ' 天', cls: c.toReleaseDay < 0 ? 'psd-danger' : '' },
        { label: '下一里程碑', value: c.nextMilestoneName ? c.nextMilestoneName + ' ' + fmt.date(c.nextMilestoneTime) : '-' },
        { label: '超期未评审里程碑', value: fmt.count(c.overdueMilestoneNum), cls: c.overdueMilestoneNum > 0 ? 'psd-danger' : '' },
      ];
      if (budgetVisible) {
        list.push({ label: '预算执行率', value: fmt.percent(c.budgetExeRate) });
        list.push({ label: '工时执行率', value: fmt.percent(c.manhourExeRate) });
        list.push({ label: '质量成本', value: fmt.money(this.detail && this.detail.qualityCost, 0) });
      }
      return list;
    },
    trendLabels() {
      return this.trend.map(p => p.month + (p.source === 'SNAPSHOT' ? '*' : ''));
    },
    trendDatasets() {
      return [
        { label: '完成工单', data: this.trend.map(p => p.completedNum || 0) },
        { label: '交付标准工时', data: this.trend.map(p => p.deliverStandHours || 0) },
        { label: '预算执行率', type: 'line', yAxisID: 'rate', color: '#F56C6C',
          data: this.trend.map(p => (p.budgetExeRate === null || p.budgetExeRate === undefined ? null : Number(p.budgetExeRate))) },
      ];
    },
    feeDatasets() {
      return [{ label: '人员费', color: '#E6A23C', data: this.trend.map(p => Number(p.staffFee || 0)) }];
    },
  },

  watch: {
    visible(v) {
      if (v) {
        this.loadAll();
      }
    },
    projectId() {
      if (this.visible) {
        this.loadAll();
      }
    },
  },

  mounted() {
    if (this.visible) {
      this.loadAll();
    }
  },

  methods: {
    statusName(status) {
      return status ? optionKV(PROJECT_STATUS, status) : '-';
    },

    clamp(rate) {
      const n = Math.round((Number(rate) || 0) * 100);
      return Math.max(0, Math.min(100, n));
    },

    loadAll() {
      if (!this.projectId) {
        return;
      }
      this.detail = null;
      this.trend = [];
      this.loadDetail();
      getProjectTrend(this.projectId, 12).then(points => {
        this.trend = points || [];
      }).catch(err => Toast.warning(err.message, Swal));
    },

    loadDetail() {
      if (!this.projectId) {
        return;
      }
      const seq = ++this.seq;
      this.loading = true;
      getProjectDetail(this.projectId, this.period).then(detail => {
        if (seq === this.seq) {
          this.detail = detail;
        }
      }).catch(err => {
        Toast.warning(err.message, Swal);
      }).finally(() => {
        if (seq === this.seq) {
          this.loading = false;
        }
      });
    },

    openSubSummary(item) {
      this.subSummaryId = item.subprojectId;
      this.subSummaryVisible = true;
    },
  }
}
</script>

<style scoped>
.psd {
  padding: 14px 18px 24px;
  min-height: 100%;
}

.psd-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.psd-title {
  font-size: 17px;
  font-weight: 600;
}

.psd-title .el-tag {
  margin-left: 8px;
  vertical-align: middle;
}

.psd-close {
  font-size: 20px;
  cursor: pointer;
  color: #909399;
}

.psd-sub {
  font-size: 12px;
  font-weight: normal;
  color: #909399;
  margin-left: 8px;
}

.psd-people {
  margin: 6px 0 0;
}

.psd-people > span {
  margin-right: 16px;
}

.psd-reasons {
  margin-top: 8px;
  padding: 6px 10px;
  background: #fef0f0;
  color: #F56C6C;
  border-radius: 4px;
  font-size: 13px;
}

.psd-cards {
  margin-top: 10px;
}

.psd-card {
  background: #f5f7fa;
  border-radius: 4px;
  padding: 8px 6px;
  margin-bottom: 8px;
  text-align: center;
}

.psd-card-value {
  font-size: 15px;
  font-weight: 600;
  color: #303133;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.psd-card-label {
  font-size: 12px;
  color: #909399;
  margin-top: 2px;
}

.psd-section {
  font-size: 15px;
  font-weight: 600;
  margin: 16px 0 8px;
  padding-left: 8px;
  border-left: 3px solid #409EFF;
}

.psd-period {
  margin-left: 12px;
  font-weight: normal;
}

.psd-budget {
  margin: 0 8px 8px;
}

.psd-budget-text {
  display: flex;
  justify-content: space-between;
  font-size: 13px;
  margin-bottom: 3px;
}

.psd-muted {
  color: #909399;
  font-size: 12px;
}

.psd-rings {
  display: flex;
  flex-wrap: wrap;
  margin-top: 6px;
}

.psd-ring {
  width: 130px;
  text-align: center;
  cursor: pointer;
  margin: 0 6px 6px 0;
}

.psd-ring-name {
  font-size: 12px;
  color: #606266;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.psd-danger {
  color: #F56C6C;
}

.psd-note {
  margin-top: 6px;
  color: #909399;
  font-size: 12px;
}
</style>
