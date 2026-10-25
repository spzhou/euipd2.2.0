<!--
 * Author: 周朔鹏
 * Date: 2026-10-12
 * Email: 391902958@qq.com
 * Website: https://www.euipd.com
-->

<template>
  <span class="stat-period-picker">
    <el-select v-model="type" size="small" style="width: 90px" @change="typeChanged">
      <el-option label="月度" value="MONTH"></el-option>
      <el-option label="季度" value="QUARTER"></el-option>
      <el-option label="年度" value="YEAR"></el-option>
      <el-option label="自定义" value="CUSTOM"></el-option>
    </el-select>
    <el-date-picker v-if="type === 'MONTH'" v-model="month" type="month" size="small" value-format="yyyy-MM"
                    :clearable="false" :picker-options="noFuture" style="width: 130px" @change="emit"></el-date-picker>
    <template v-if="type === 'QUARTER'">
      <el-date-picker v-model="year" type="year" size="small" value-format="yyyy" :clearable="false"
                      :picker-options="noFuture" style="width: 100px" @change="emit"></el-date-picker>
      <el-select v-model="quarter" size="small" style="width: 80px" @change="emit">
        <el-option v-for="q in 4" :key="q" :label="'Q' + q" :value="q"></el-option>
      </el-select>
    </template>
    <el-date-picker v-if="type === 'YEAR'" v-model="year" type="year" size="small" value-format="yyyy"
                    :clearable="false" :picker-options="noFuture" style="width: 100px" @change="emit"></el-date-picker>
    <el-date-picker v-if="type === 'CUSTOM'" v-model="range" type="daterange" size="small" value-format="yyyy-MM-dd"
                    range-separator="至" start-placeholder="开始日期" end-placeholder="结束日期" :clearable="false"
                    style="width: 240px" @change="emit"></el-date-picker>
  </span>
</template>

<script>
function pad(n) {
  return n < 10 ? '0' + n : '' + n;
}

function today() {
  const d = new Date();
  return d.getFullYear() + '-' + pad(d.getMonth() + 1) + '-' + pad(d.getDate());
}

export default {
  name: "stat-period-picker",
  props: {
    // {periodType, periodValue}，与 StatQueryDto 同名
    value: { type: Object, default: null },
  },

  data() {
    const now = new Date();
    return {
      type: 'MONTH',
      month: now.getFullYear() + '-' + pad(now.getMonth() + 1),
      year: String(now.getFullYear()),
      quarter: Math.floor(now.getMonth() / 3) + 1,
      range: [now.getFullYear() + '-' + pad(now.getMonth() + 1) + '-01', today()],
      noFuture: {
        disabledDate(d) {
          return d.getTime() > Date.now();
        }
      },
    };
  },

  watch: {
    value: {
      immediate: true,
      handler(v) {
        this.load(v);
      }
    }
  },

  mounted() {
    if (!this.value || !this.value.periodType) {
      this.emit();
    }
  },

  methods: {
    load(v) {
      if (!v || !v.periodType || !v.periodValue || this.current().periodValue === v.periodValue && this.type === v.periodType) {
        return;
      }
      this.type = v.periodType;
      const value = v.periodValue;
      if (v.periodType === 'MONTH') {
        this.month = value;
      } else if (v.periodType === 'QUARTER') {
        this.year = value.substring(0, 4);
        this.quarter = Number(value.substring(6));
      } else if (v.periodType === 'YEAR') {
        this.year = value;
      } else if (v.periodType === 'CUSTOM') {
        this.range = value.split('~');
      }
    },

    current() {
      let periodValue;
      if (this.type === 'MONTH') {
        periodValue = this.month;
      } else if (this.type === 'QUARTER') {
        periodValue = this.year + '-Q' + this.quarter;
      } else if (this.type === 'YEAR') {
        periodValue = this.year;
      } else {
        periodValue = (this.range || []).join('~');
      }
      return { periodType: this.type, periodValue: periodValue };
    },

    typeChanged() {
      this.emit();
    },

    emit() {
      const v = this.current();
      this.$emit('input', v);
      this.$emit('change', v);
    }
  }
}
</script>

<style scoped>
.stat-period-picker > * {
  margin-right: 6px;
  vertical-align: middle;
}
</style>
