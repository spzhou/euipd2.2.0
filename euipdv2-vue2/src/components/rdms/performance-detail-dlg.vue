<!--
 * Author: 周朔鹏
 * Date: 2026-10-12
 * Email: 391902958@qq.com
 * Website: https://www.euipd.com
-->

<template>
  <el-dialog :title="title" :visible="visible" width="90%" class="perf-detail-dlg" append-to-body @close="$emit('close')">
    <div v-loading="loading">
      <el-row v-if="summary" :gutter="10" class="perf-detail-cards">
        <el-col :span="4" v-for="card in cards" :key="card.label">
          <div class="perf-detail-card">
            <div class="perf-detail-card-value">{{ card.value }}</div>
            <div class="perf-detail-card-label">{{ card.label }}</div>
          </div>
        </el-col>
      </el-row>
      <el-table :data="rows" size="mini" border stripe max-height="420" show-summary :summary-method="sumRow">
        <el-table-column type="index" label="序号" width="50" align="center"></el-table-column>
        <el-table-column prop="jobSerial" label="工单编号" width="120" show-overflow-tooltip></el-table-column>
        <el-table-column prop="jobName" label="工单名称" min-width="160" show-overflow-tooltip></el-table-column>
        <el-table-column prop="typeName" label="类型" width="90" show-overflow-tooltip></el-table-column>
        <el-table-column prop="projectName" label="项目" min-width="130" show-overflow-tooltip></el-table-column>
        <el-table-column label="计划提交" width="96" align="center">
          <template slot-scope="s">{{ fmt.date(s.row.planSubmissionTime) }}</template>
        </el-table-column>
        <el-table-column label="实际提交" width="96" align="center">
          <template slot-scope="s">
            <span :class="{ 'perf-detail-overdue': s.row.overdue }">{{ fmt.date(s.row.actualSubmissionTime) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="performanceManhour" label="绩效工时" width="80" align="right">
          <template slot-scope="s">{{ fmt.hours(s.row.performanceManhour) }}</template>
        </el-table-column>
        <el-table-column prop="evaluateScore" label="评分" width="60" align="right">
          <template slot-scope="s">{{ fmt.score(s.row.evaluateScore) }}</template>
        </el-table-column>
        <el-table-column prop="qualityFactor" label="质量系数" width="76" align="right">
          <template slot-scope="s">{{ factor(s.row.qualityFactor) }}</template>
        </el-table-column>
        <el-table-column prop="settledHours" label="结算工时" width="80" align="right">
          <template slot-scope="s">{{ fmt.hours(s.row.settledHours) }}</template>
        </el-table-column>
        <el-table-column v-if="!amountHidden" prop="standManhourFee" label="工时单价" width="80" align="right">
          <template slot-scope="s">{{ fmt.money(s.row.standManhourFee) }}</template>
        </el-table-column>
        <el-table-column v-if="!amountHidden" prop="fee" label="绩效金额" width="96" align="right">
          <template slot-scope="s">{{ fmt.money(s.row.fee) }}</template>
        </el-table-column>
      </el-table>
      <div class="perf-detail-note">
        结算工时 = 绩效工时 ×（1 − 当月延期率 × 延期折扣系数）× 质量系数；质量系数按评分折算，当月延期率按全部计入工单的延期时长 ÷ 计划周期计。
        绩效金额 = 结算工时 × 工时单价，逐单保留两位小数，合计可能与汇总相差分位。
        <span v-if="amountHidden">按统计权限，查看他人时不显示金额。</span>
      </div>
    </div>
    <span slot="footer">
      <el-button size="small" @click="exportExcel" :disabled="!rows.length">导出为Excel</el-button>
      <el-button size="small" type="primary" @click="$emit('close')">关闭</el-button>
    </span>
  </el-dialog>
</template>

<script>
import Swal from "sweetalert2";
import * as fmt from "@/shared/stat-format";
import { getPerformanceDetail } from "@/shared/statistics-api";

export default {
  name: "performance-detail-dlg",
  props: {
    visible: { type: Boolean, default: false },
    customerUserId: { type: String, default: '' },
  },

  data() {
    return {
      fmt: fmt,
      loading: false,
      detail: null,
      seq: 0,
    };
  },

  computed: {
    title() {
      const d = this.detail || {};
      return (d.trueName || '成员') + ' 当月绩效计入明细' + (d.yearMonth ? '（' + d.yearMonth + '）' : '');
    },
    summary() {
      return this.detail && this.detail.summary;
    },
    rows() {
      return (this.detail && this.detail.rows) || [];
    },
    amountHidden() {
      return !!(this.detail && this.detail.amountHidden);
    },
    cards() {
      const s = this.summary || {};
      const cards = [
        { label: '计入工单', value: fmt.count(this.rows.length) },
        { label: '提交工时（标准）', value: fmt.hours(s.submitHours) },
        { label: '延期率（折扣系数 ' + factor(s.rebateRate) + '）', value: fmt.percent(s.delayRate) },
        { label: '负荷率', value: fmt.percent(s.loadRate) },
        { label: '绩效评分', value: fmt.score(s.score) },
      ];
      if (!this.amountHidden) {
        cards.push({ label: '绩效金额', value: fmt.money(s.performance) });
      }
      return cards;
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

  methods: {
    factor(v) {
      return factor(v);
    },

    load() {
      if (!this.customerUserId) {
        return;
      }
      const seq = ++this.seq;
      this.loading = true;
      this.detail = null;
      getPerformanceDetail(this.customerUserId).then(d => {
        if (seq === this.seq) {
          this.detail = d;
        }
      }).catch(err => {
        Toast.warning(err.message, Swal);
      }).finally(() => {
        if (seq === this.seq) {
          this.loading = false;
        }
      });
    },

    sumRow({ columns }) {
      return columns.map((col, i) => {
        if (i === 0) {
          return '合计';
        }
        if (col.property === 'performanceManhour' || col.property === 'settledHours') {
          return fmt.hours(this.rows.reduce((a, r) => a + Number(r[col.property] || 0), 0));
        }
        if (col.property === 'fee') {
          return fmt.money(this.rows.reduce((a, r) => a + Number(r.fee || 0), 0));
        }
        return '';
      });
    },

    async exportExcel() {
      const XLSX = await import(/* webpackChunkName: "xlsx" */ 'xlsx');
      const data = this.rows.map(r => {
        const row = {
          '工单编号': r.jobSerial,
          '工单名称': r.jobName,
          '类型': r.typeName,
          '项目': r.projectName,
          '计划提交': fmt.date(r.planSubmissionTime),
          '实际提交': fmt.date(r.actualSubmissionTime),
          '延期': r.overdue ? '是' : '否',
          '绩效工时': r.performanceManhour,
          '评分': r.evaluateScore,
          '质量系数': r.qualityFactor,
          '结算工时': r.settledHours,
        };
        if (!this.amountHidden) {
          row['工时单价'] = r.standManhourFee;
          row['绩效金额'] = r.fee;
        }
        return row;
      });
      const book = XLSX.utils.book_new();
      XLSX.utils.book_append_sheet(book, XLSX.utils.json_to_sheet(data), '当月计入明细');
      const d = this.detail || {};
      XLSX.writeFile(book, ((d.trueName || '成员') + '-绩效明细-' + (d.yearMonth || '')).replace(/[\\/:*?"<>|~]/g, '_') + '.xlsx');
    }
  }
}

function factor(v) {
  return v === null || v === undefined || v === '' ? '-' : Number(v).toFixed(2);
}
</script>

<style scoped>
.perf-detail-dlg >>> .el-dialog {
  max-width: 1400px;
}
.perf-detail-cards {
  margin-bottom: 10px;
}
.perf-detail-card {
  border: 1px solid #ebeef5;
  border-radius: 4px;
  padding: 8px 10px;
  text-align: center;
}
.perf-detail-card-value {
  font-size: 18px;
  font-weight: 600;
  color: #303133;
}
.perf-detail-card-label {
  font-size: 12px;
  color: #909399;
  margin-top: 2px;
}
.perf-detail-overdue {
  color: #F56C6C;
}
.perf-detail-note {
  margin-top: 8px;
  font-size: 12px;
  color: #909399;
  line-height: 1.6;
}
</style>
