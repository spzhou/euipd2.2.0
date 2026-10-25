<!--
 * Author: 周朔鹏
 * Date: 2026-10-12
 * Email: 391902958@qq.com
 * Website: https://www.euipd.com
-->

<template>
  <el-dialog :title="title" :visible="visible" width="880px" append-to-body @close="$emit('close')">
    <div v-loading="loading">
      <el-row v-if="summary" :gutter="10" class="person-cards">
        <el-col :span="4" v-for="card in cards" :key="card.label">
          <div class="person-card">
            <div class="person-card-value">{{ card.value }}</div>
            <div class="person-card-label">{{ card.label }}</div>
          </div>
        </el-col>
      </el-row>
      <el-tabs v-model="tab">
        <el-tab-pane label="按项目" name="project">
          <el-table :data="detail.byProject" size="mini" border max-height="300">
            <el-table-column prop="name" label="项目" min-width="180" show-overflow-tooltip></el-table-column>
            <el-table-column prop="inProgressNum" label="进行中" width="70" align="right"></el-table-column>
            <el-table-column prop="completedNum" label="完成" width="60" align="right"></el-table-column>
            <el-table-column prop="overdueCompletedNum" label="延期完成" width="80" align="right"></el-table-column>
            <el-table-column label="交付标准工时" width="110" align="right">
              <template slot-scope="s">{{ fmt.hours(s.row.deliverStandHours) }}</template>
            </el-table-column>
            <el-table-column label="交付个人工时" width="110" align="right">
              <template slot-scope="s">{{ fmt.hours(s.row.deliverWorkerHours) }}</template>
            </el-table-column>
            <el-table-column label="人员费" width="110" align="right">
              <template slot-scope="s">{{ fmt.money(s.row.staffFee) }}</template>
            </el-table-column>
          </el-table>
        </el-tab-pane>
        <el-tab-pane label="按类型" name="type">
          <el-row :gutter="10">
            <el-col :span="10">
              <stat-chart v-if="tab === 'type'" type="doughnut" :height="240"
                          :labels="detail.byType.map(t => t.name)"
                          :datasets="[{label: '交付标准工时', data: detail.byType.map(t => t.deliverStandHours)}]"></stat-chart>
            </el-col>
            <el-col :span="14">
              <el-table :data="detail.byType" size="mini" border max-height="260">
                <el-table-column prop="name" label="工单类型" min-width="120"></el-table-column>
                <el-table-column prop="inProgressNum" label="进行中" width="70" align="right"></el-table-column>
                <el-table-column prop="completedNum" label="完成" width="60" align="right"></el-table-column>
                <el-table-column label="交付标准工时" width="110" align="right">
                  <template slot-scope="s">{{ fmt.hours(s.row.deliverStandHours) }}</template>
                </el-table-column>
              </el-table>
            </el-col>
          </el-row>
        </el-tab-pane>
        <el-tab-pane label="近 6 个月" name="month">
          <stat-chart v-if="tab === 'month'" type="bar" :height="220" y-label="工时"
                      :labels="detail.byMonth.map(m => m.name)"
                      :datasets="[
                        {label: '交付个人工时', data: detail.byMonth.map(m => m.deliverWorkerHours)},
                        {label: '完成工单数', type: 'line', data: detail.byMonth.map(m => m.completedNum)}
                      ]"></stat-chart>
          <el-table :data="detail.byMonth" size="mini" border>
            <el-table-column prop="name" label="月份" width="90"></el-table-column>
            <el-table-column prop="completedNum" label="完成" align="right"></el-table-column>
            <el-table-column prop="overdueCompletedNum" label="延期完成" align="right"></el-table-column>
            <el-table-column label="交付个人工时" align="right">
              <template slot-scope="s">{{ fmt.hours(s.row.deliverWorkerHours) }}</template>
            </el-table-column>
            <el-table-column label="人员费" align="right">
              <template slot-scope="s">{{ fmt.money(s.row.staffFee) }}</template>
            </el-table-column>
            <el-table-column v-if="showPerformance" label="绩效" align="right">
              <template slot-scope="s">{{ fmt.money(s.row.performance) }}</template>
            </el-table-column>
          </el-table>
        </el-tab-pane>
      </el-tabs>
    </div>
    <span slot="footer">
      <el-button size="small" @click="goJobs">工单清单</el-button>
      <el-button size="small" @click="goPerformance">绩效统计</el-button>
      <el-button size="small" type="primary" @click="$emit('close')">关闭</el-button>
    </span>
  </el-dialog>
</template>

<script>
import Swal from "sweetalert2";
import StatChart from "@/components/statistics/stat-chart.vue";
import * as fmt from "@/shared/stat-format";
import { getPersonWorkload } from "@/shared/statistics-api";

export default {
  name: "person-workload-dlg",
  components: { StatChart },
  props: {
    visible: { type: Boolean, default: false },
    customerUserId: { type: String, default: '' },
    trueName: { type: String, default: '' },
    // {periodType, periodValue}
    period: { type: Object, default: null },
    // 绩效金额仅对机构级、部门级显示
    showPerformance: { type: Boolean, default: false },
  },

  data() {
    return {
      fmt: fmt,
      tab: 'project',
      loading: false,
      detail: { summary: null, byProject: [], byType: [], byMonth: [] },
      seq: 0,
    };
  },

  computed: {
    title() {
      const key = this.detail.period || '';
      const period = key.indexOf(':') >= 0 ? key.substring(key.indexOf(':') + 1) : key;
      return (this.trueName || '成员') + ' 工作量分解' + (period ? '（' + period + '）' : '');
    },
    summary() {
      return this.detail.summary;
    },
    cards() {
      const s = this.summary || {};
      return [
        { label: '进行中 / 逾期', value: fmt.count(s.inProgressNum) + ' / ' + fmt.count(s.overdueNum) },
        { label: '完成 / 延期完成', value: fmt.count(s.completedNum) + ' / ' + fmt.count(s.overdueCompletedNum) },
        { label: '交付个人工时', value: fmt.hours(s.deliverWorkerHours) },
        { label: '工时负荷率', value: fmt.percent(s.loadRate) },
        { label: '延期率 / 返工率', value: fmt.percent(s.delayRate) + ' / ' + fmt.percent(s.reworkRate) },
        { label: '平均评分', value: fmt.score(s.avgScore) },
      ];
    }
  },

  watch: {
    visible(v) {
      if (v) {
        this.load();
      }
    },
    customerUserId() {
      if (this.visible) {
        this.load();
      }
    }
  },

  mounted() {
    if (this.visible) {
      this.load();
    }
  },

  methods: {
    load() {
      if (!this.customerUserId) {
        return;
      }
      const seq = ++this.seq;
      this.loading = true;
      getPersonWorkload(this.customerUserId, Object.assign({}, this.period || {})).then(content => {
        if (seq !== this.seq) {
          return;
        }
        this.detail = Object.assign({ summary: null, byProject: [], byType: [], byMonth: [] }, content || {});
      }).catch(err => {
        Toast.warning(err.message, Swal);
      }).finally(() => {
        if (seq === this.seq) {
          this.loading = false;
        }
      });
    },

    goJobs() {
      this.$emit('close');
      this.$router.push({ name: 'customer/manhour' });
    },

    goPerformance() {
      this.$emit('go-performance', this.customerUserId);
    }
  }
}
</script>

<style scoped>
.person-cards {
  margin-bottom: 10px;
}

.person-card {
  background: #f5f7fa;
  border-radius: 4px;
  padding: 8px 6px;
  text-align: center;
}

.person-card-value {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.person-card-label {
  font-size: 12px;
  color: #909399;
  margin-top: 2px;
}
</style>
