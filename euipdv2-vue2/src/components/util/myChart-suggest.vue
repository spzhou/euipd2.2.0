<!--
 * Author: 周朔鹏
 * Date: 2024-07-09
 * Email: 391902958@qq.com
 * Website: https://www.euipd.com
-->

<template>
  <div style="width: 100%;">
    <div class="chart-wrap" style="width: 788px;">
      <canvas ref="chartCanvas" :id="chartId"></canvas>
    </div>
    <div style="width: 100%; display: flex; flex-direction: row; justify-content: flex-end; padding-right: 120px; margin-bottom: 10px;">
      <small><em> <i class="el-icon-info"></i>操作说明：如果未来 5 个工作日（按机构假日设置）的日均工作负荷超过 10 小时，将无法给相应工作人员发任务，请对其在手任务进行协调。</em></small>
    </div>
  </div>
</template>

<script>
import workloadChartMixin from "./workload-chart-mixin";

export default {
  name: 'myChartSuggest',
  mixins: [workloadChartMixin],
};
</script>

<style lang="scss" scoped>
@import './../../../public/static/scss/caipd.scss';

.chart-wrap {
  position: relative;
  height: 200px;
  max-width: calc(100% - 120px);
  margin: 0 0 10px 120px;
}
</style>
