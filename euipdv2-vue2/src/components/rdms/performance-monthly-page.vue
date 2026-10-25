<!--
 * Author: 周朔鹏
 * Date: 2025-02-07
 * Email: 391902958@qq.com
 * Website: https://www.euipd.com
-->


<template>
  <div>
    <div class="performance-page-height" style="overflow-y: scroll">
      <div style="height: 100%;" v-loading="loading">
        <el-row>
          <div style="display: flex; justify-content: space-between; align-items: center;">
            <div style="display: flex; flex-direction: row; justify-content: flex-start; align-items: center;">
              <div style="margin-right: 3px; font-size: 14px;">请选择查询月份:</div>
              <el-date-picker
                  v-model="month"
                  type="month"
                  value-format="yyyy-MM"
                  :clearable="false"
                  size="mini"
                  placeholder="选择月"
                  @change="listPerformance"
              ></el-date-picker>
              <el-select v-model="departmentKey" size="mini" clearable placeholder="全部部门"
                         style="width: 180px; margin-left: 20px;">
                <el-option v-for="d in departments" :key="keyOf(d)" :label="d.departmentName" :value="keyOf(d)"></el-option>
              </el-select>
              <div style="margin-left: 10px;">
                <el-input
                    v-model="searchName"
                    size="mini"
                    placeholder="页面内容搜索-输入姓名"/>
              </div>
            </div>
            <div style="margin-left: 5px;">
              <el-button size="mini" @click="exportToExcel">导出为Excel</el-button>
            </div>
          </div>

          <div class="monthly-section">部门小计</div>
          <el-table :data="subtotalRows" border size="mini" style="width: 100%" :row-class-name="subtotalClass"
                    @row-click="selectDepartment">
            <el-table-column prop="departmentName" label="部门" min-width="140" show-overflow-tooltip></el-table-column>
            <el-table-column prop="memberCount" label="人数" width="70" align="center"></el-table-column>
            <el-table-column label="总工时(标准)" min-width="110" align="center">
              <template slot-scope="s">{{ fmt.hours(s.row.submitHours) }}</template>
            </el-table-column>
            <el-table-column label="绩效工时费" min-width="110" align="center">
              <template slot-scope="s">{{ rmbFormat(s.row.performance) }}</template>
            </el-table-column>
            <el-table-column label="平均绩效评分" min-width="100" align="center">
              <template slot-scope="s">{{ fmt.score(s.row.avgScore) }}</template>
            </el-table-column>
            <el-table-column label="吸收率" min-width="90" align="center">
              <template slot-scope="s">{{ fmt.percent(s.row.absorptionRate) }}</template>
            </el-table-column>
          </el-table>

          <div class="monthly-section">人员明细</div>
          <div>
            <el-table
                :data="filteredRows"
                border
                stripe
                highlight-current-row
                style="width: 100%">
              <el-table-column  label="序号" align="center" width="50">
                <template slot-scope="scope">
                  {{scope.$index+1}}
                </template>
              </el-table-column>
              <el-table-column
                  prop="jobNum"
                  min-width="100"
                  show-overflow-tooltip
                  align="center"
                  label="工号"
              ></el-table-column>
              <el-table-column
                  prop="customerUserName"
                  min-width="100"
                  show-overflow-tooltip
                  align="center"
                  label="姓名"
              ></el-table-column>
              <el-table-column
                  prop="departmentName"
                  min-width="110"
                  show-overflow-tooltip
                  align="center"
                  label="部门"
              ></el-table-column>
              <el-table-column
                  prop="yearMonth"
                  min-width="100"
                  show-overflow-tooltip
                  align="center"
                  label="绩效周期"
              ></el-table-column>
              <el-table-column
                  prop="submitHours"
                  :formatter="submitHoursFormat"
                  min-width="100"
                  show-overflow-tooltip
                  align="center"
                  label="总工时(标准)"
              ></el-table-column>
              <el-table-column
                  prop="delayRate"
                  align="center"
                  label="延期率"
                  min-width="100"
                  show-overflow-tooltip
              ></el-table-column>
              <el-table-column
                  prop="rebateRate"
                  align="center"
                  label="延期绩效折扣"
                  min-width="100"
                  show-overflow-tooltip
              ></el-table-column>
              <el-table-column
                  prop="performance"
                  :formatter="performanceFormat"
                  align="center"
                  label="绩效工时费"
                  min-width="100"
                  show-overflow-tooltip
              ></el-table-column>
              <el-table-column
                  prop="loadRate"
                  align="center"
                  label="负荷率"
                  min-width="100"
                  show-overflow-tooltip
              ></el-table-column>
              <el-table-column
                  prop="score"
                  :formatter="scoreFormat"
                  align="center"
                  label="绩效评分"
                  min-width="100"
                  show-overflow-tooltip
              ></el-table-column>
            </el-table>
            <div class="monthly-note">
              部门归属取该月快照；部门小计按人员明细分组求和，各部门小计之和等于合计。吸收率即部门工时负荷率（交付个人工时 ÷ 法定工时），
              按该月统计范围内该部门直属成员计算，与“组织工作量”部门直属数值一致。点击小计行可筛选该部门。
            </div>
          </div>
        </el-row>
      </div>
    </div>
  </div>
</template>

<script>
import Swal from "sweetalert2";
import {rmbFormat} from "@/filter/filter";
import * as fmt from "@/shared/stat-format";
import { getPerformanceMonthlyReport } from "@/shared/statistics-api";

const UNASSIGNED = 'UNASSIGNED';

export default {
  name: "performance-monthly-page",

  props: {
    customerId: {
      default: null,
    }
  },

  data: function () {
    return {
      fmt: fmt,
      month: '',
      searchName: '',
      departmentKey: '',
      loading: false,
      report: { rows: [], departments: [], total: null },
    }
  },

  computed: {
    departments() {
      return this.report.departments || [];
    },
    subtotalRows() {
      const rows = this.departments.slice();
      if (this.report.total) {
        rows.push(Object.assign({}, this.report.total, { departmentName: '合计', isTotal: true }));
      }
      return rows;
    },
    filteredRows() {
      const name = (this.searchName || '').toLowerCase();
      return (this.report.rows || []).filter(r =>
        (!this.departmentKey || this.keyOf(r) === this.departmentKey)
        && (!name || (r.customerUserName || '').toLowerCase().includes(name)));
    },
  },

  mounted: function () {
    const now = new Date();
    const last = new Date(now.getFullYear(), now.getMonth() - 1, 1);
    this.month = last.getFullYear() + '-' + String(last.getMonth() + 1).padStart(2, '0');
    this.listPerformance();
  },

  methods: {
    rmbFormat,

    keyOf(item) {
      return item.departmentId || UNASSIGNED;
    },

    listPerformance() {
      if (!this.month) {
        return;
      }
      this.loading = true;
      getPerformanceMonthlyReport(this.month).then(report => {
        this.report = Object.assign({ rows: [], departments: [], total: null }, report || {});
        if (this.departmentKey && !this.departments.some(d => this.keyOf(d) === this.departmentKey)) {
          this.departmentKey = '';
        }
      }).catch(err => {
        Toast.warning(err.message, Swal);
      }).finally(() => {
        this.loading = false;
      });
    },

    selectDepartment(row) {
      if (row.isTotal) {
        this.departmentKey = '';
        return;
      }
      const key = this.keyOf(row);
      this.departmentKey = this.departmentKey === key ? '' : key;
    },

    subtotalClass({ row }) {
      if (row.isTotal) {
        return 'monthly-total-row';
      }
      return this.departmentKey && this.keyOf(row) === this.departmentKey ? 'monthly-selected-row' : '';
    },

    submitHoursFormat(row){
      return row.submitHours === null || row.submitHours === undefined ? '' : row.submitHours.toFixed(2);
    },
    scoreFormat(row){
      return row.score?.toFixed(2);
    },

    performanceFormat(row){
      return rmbFormat(row.performance);
    },

    async exportToExcel() {
      const details = this.filteredRows.map(item => ({
        '工号': item.jobNum,
        '姓名': item.customerUserName,
        '部门': item.departmentName,
        '绩效周期': item.yearMonth,
        '总工时(标准)': item.submitHours,
        '延期率': item.delayRate,
        '延期绩效折扣': item.rebateRate,
        '负荷率': item.loadRate,
        '绩效工时费': item.performance,
        '绩效评分': item.score,
      }));
      const subtotals = this.subtotalRows.map(d => ({
        '部门': d.departmentName,
        '人数': d.memberCount,
        '总工时(标准)': d.submitHours,
        '绩效工时费': d.performance,
        '平均绩效评分': d.avgScore,
        '吸收率': fmt.percent(d.absorptionRate),
      }));

      const XLSX = await import(/* webpackChunkName: "xlsx" */ 'xlsx');
      const workbook = XLSX.utils.book_new();
      XLSX.utils.book_append_sheet(workbook, XLSX.utils.json_to_sheet(details), '人员明细');
      XLSX.utils.book_append_sheet(workbook, XLSX.utils.json_to_sheet(subtotals), '部门小计');
      XLSX.writeFile(workbook, 'performance-monthly-' + this.month + '.xlsx');
    }
  }
}
</script>
<style>
.no-wrap-ellipsis {
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.performance-page-height{
  display: block;
  width: 100%;
  height: calc(100vh - 170px);
}
.monthly-section {
  font-size: 14px;
  font-weight: 600;
  margin: 12px 0 6px;
}
.monthly-note {
  margin-top: 6px;
  font-size: 12px;
  color: #909399;
  line-height: 1.6;
}
.el-table .monthly-total-row td {
  font-weight: 600;
  background: #f5f7fa;
}
.el-table .monthly-selected-row td {
  background: #ecf5ff;
}
</style>
