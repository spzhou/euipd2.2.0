<!--
 * Author: 周朔鹏
 * Date: 2025-02-14
 * Email: 391902958@qq.com
 * Website: https://www.euipd.com
-->

<template>
  <div>
    <div class="row">
      <div style="margin: 0 15px;">
        <div class="perf-trend-title">近 13 个月趋势<span class="perf-trend-hint">（* 为当月实时值，其余为已结算记录；无记录的月份按 0 显示）</span></div>
        <el-row :gutter="12">
          <el-col :span="14">
            <stat-chart type="bar" rate-axis y-label="工时 / 评分" :height="240" :labels="trendLabels"
                        :datasets="[
                          {label: '提交工时(标准)', data: trend.map(t => t.submitHours)},
                          {label: '绩效评分', type: 'line', color: '#E6A23C', data: trend.map(t => t.score)},
                          {label: '负荷率', type: 'line', yAxisID: 'rate', color: '#F56C6C', data: trend.map(t => t.loadRate)}
                        ]"></stat-chart>
          </el-col>
          <el-col :span="10">
            <stat-chart type="line" y-label="元" :height="240" :labels="trendLabels"
                        :datasets="[{label: '绩效工时费', color: '#67C23A', data: trend.map(t => t.performance)}]"></stat-chart>
          </el-col>
        </el-row>
        <div>
          <el-table
              :data="performanceList"
              row-key="id"
              border
              default-expand-all
              highlight-current-row
              show-summary
              :summary-method="getSummaries"
              style="width: 100%">
            <el-table-column label="序号" align="center" width="90">
              <template slot-scope="scope">
                {{ scope.$index + 1 }}
              </template>
            </el-table-column>
            <el-table-column
                prop="yearMonth"
                label="月份"
                align="center"
                show-overflow-tooltip
            ></el-table-column>
            <el-table-column
                prop="customerUserName"
                label="姓名"
                align="center"
                show-overflow-tooltip
            ></el-table-column>
            <el-table-column
                prop="jobLevel"
                label="职级"
                align="center"
            ></el-table-column>
            <el-table-column
                prop="title"
                label="职务"
                align="center"
            ></el-table-column>
            <el-table-column
                prop="submitHours"
                :formatter="submitHoursFormat"
                sortable
                label="交付总工时(标准)"
                align="center"
            ></el-table-column>
            <el-table-column
                prop="delayRate"
                sortable
                label="延期率"
                align="center"
            ></el-table-column>
            <el-table-column
                prop="rebateRate"
                label="延期绩效折扣"
                align="center"
            ></el-table-column>
            <el-table-column
                prop="performance"
                :formatter="performanceFormat"
                sortable
                label="绩效工时费"
                align="center"
            ></el-table-column>
            <el-table-column
                prop="loadRate"
                sortable
                label="负荷率"
                align="center"
            ></el-table-column>
            <el-table-column
                prop="score"
                :formatter="scoreFormat"
                sortable
                label="绩效评分"
                align="center"
            ></el-table-column>
          </el-table>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import Swal from "sweetalert2";
import {rmbFormat} from "@/filter/filter";
import { getPerformanceDetail } from "@/shared/statistics-api";

const TREND_MONTHS = 13;

function monthKey(d) {
  return d.getFullYear() + '-' + String(d.getMonth() + 1).padStart(2, '0');
}

export default {
  components: {
    StatChart: () => import(/* webpackChunkName: "statistics" */ "@/components/statistics/stat-chart.vue"),
  },
  name: "performance-worker-list",

  props: {
    customerUserId: {
      default: null,
    }
  },

  data: function () {
    return {
      performanceList: [],
      current: null,
    }
  },

  computed: {
    trendMonths() {
      const now = new Date();
      const months = [];
      for (let i = TREND_MONTHS - 1; i >= 0; i--) {
        months.push(monthKey(new Date(now.getFullYear(), now.getMonth() - i, 1)));
      }
      return months;
    },
    trendLabels() {
      const last = this.trendMonths[this.trendMonths.length - 1];
      return this.trendMonths.map(m => m === last ? m + '*' : m);
    },
    trend() {
      const byMonth = {};
      (this.performanceList || []).forEach(p => { byMonth[p.yearMonth] = p; });
      const last = this.trendMonths[this.trendMonths.length - 1];
      if (this.current) {
        byMonth[last] = this.current;
      }
      return this.trendMonths.map(m => {
        const p = byMonth[m] || {};
        return {
          submitHours: Number(p.submitHours || 0),
          performance: m === last && this.current && this.current.performance == null ? null : Number(p.performance || 0),
          score: Number(p.score || 0),
          loadRate: Number(p.loadRate || 0),
        };
      });
    },
  },

  mounted: function () {
    let _this = this;
    _this.getPerformanceListByUserId(_this.customerUserId);
    getPerformanceDetail(_this.customerUserId).then(d => {
      _this.current = d && d.summary ? d.summary : null;
    }).catch(() => {
      _this.current = null;
    });
  },

  methods: {
    getPerformanceListByUserId(customerUserId) {
      let _this = this;
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/performance/getPerformanceListByUserId/' + customerUserId).then((response) => {
        let resp = response.data;
        if (resp.success) {
          _this.performanceList = resp.content;
        } else {
          Toast.warning(resp.message, Swal);
        }
      })
    },

    submitHoursFormat(row){
      return row.submitHours.toFixed(2);
    },

    scoreFormat(row){
      return row.score?.toFixed(2);
    },

    delayRateFormat(row){
      return row.score?.toFixed(2);
    },

    performanceFormat(row){
      return rmbFormat(row.performance);
    },


    getSummaries(param) {
      const { columns, data } = param;
      const sums = [];
      columns.forEach((column, index) => {
        if (index === 0) {
          sums[index] = '均值';
          return;
        }
        if (index === 1 || index === 2 || index === 3 || index === 4 || index === 5 ) {
          sums[index] = '';
          return;
        }
        const values = data.map(item => Number(item[column.property]));
        if (!values.every(value => isNaN(value))) {
          sums[index] = values.reduce((prev, curr) => {
            const value = Number(curr);
            if (!isNaN(value)) {
              return prev + curr;
            } else {
              return prev;
            }
          }, 0);
          sums[index] = (sums[index] / values.length).toFixed(2);
        } else {
          sums[index] = 'N/A';
        }
      });
      return sums;
    }

  }
}
</script>

<style lang="scss">
@import './../../../public/static/scss/caipd.scss';

</style>

<style scoped>
.perf-trend-title {
  font-size: 14px;
  font-weight: 600;
  margin: 6px 0;
}
.perf-trend-hint {
  font-size: 12px;
  font-weight: normal;
  color: #909399;
}
</style>
