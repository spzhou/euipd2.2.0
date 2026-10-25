<!--
 * Author: 周朔鹏
 * Date: 2026-10-12
 * Email: 391902958@qq.com
 * Website: https://www.euipd.com
-->

<template>
  <div class="stat-chart" :style="{height: height + 'px'}">
    <canvas v-show="!empty" ref="canvas"></canvas>
    <div v-if="empty" class="stat-chart-empty">{{ emptyText }}</div>
  </div>
</template>

<script>
import { STAT_COLORS } from "@/shared/stat-format";

function loadChartJs() {
  return import(/* webpackChunkName: "chartjs" */ 'chart.js').then(mod => mod.default || mod);
}

function alpha(color, a) {
  const hex = color.replace('#', '');
  const r = parseInt(hex.substring(0, 2), 16);
  const g = parseInt(hex.substring(2, 4), 16);
  const b = parseInt(hex.substring(4, 6), 16);
  return 'rgba(' + r + ',' + g + ',' + b + ',' + a + ')';
}

export default {
  name: "stat-chart",
  props: {
    type: { type: String, default: 'bar' },
    labels: { type: Array, default: () => [] },
    // [{label, data, color?, colors?（饼图逐扇区）, type?, yAxisID?}]
    datasets: { type: Array, default: () => [] },
    stacked: { type: Boolean, default: false },
    height: { type: Number, default: 260 },
    emptyText: { type: String, default: '暂无数据' },
    yLabel: { type: String, default: '' },
    // 第二纵轴（右侧，0 至 1 的比率按百分数显示）
    rateAxis: { type: Boolean, default: false },
  },

  created() {
    // Chart 实例持有 DOM 与循环引用，放在 data 外以免被 Vue 深度观测
    this.chart = null;
    this.destroyed = false;
    this.renderSeq = 0;
  },

  computed: {
    empty() {
      return !this.labels.length || !this.datasets.some(ds => (ds.data || []).some(v => Number(v)));
    }
  },

  watch: {
    labels() {
      this.render();
    },
    datasets: {
      deep: true,
      handler() {
        this.render();
      }
    },
    type() {
      this.destroyChart();
      this.render();
    }
  },

  mounted() {
    this.render();
  },

  beforeDestroy() {
    this.destroyed = true;
    this.destroyChart();
  },

  methods: {
    buildDatasets() {
      const round = this.type === 'doughnut' || this.type === 'pie';
      return this.datasets.map((ds, i) => {
        const color = ds.color || STAT_COLORS[i % STAT_COLORS.length];
        const line = (ds.type || this.type) === 'line';
        const item = {
          label: ds.label,
          data: ds.data,
          type: ds.type,
          fill: false,
          borderWidth: line ? 2 : 1,
          lineTension: 0.2,
        };
        if (round) {
          item.backgroundColor = ds.colors || ds.data.map((v, k) => STAT_COLORS[k % STAT_COLORS.length]);
        } else {
          item.backgroundColor = line ? color : alpha(color, 0.7);
          item.borderColor = color;
        }
        if (ds.yAxisID) {
          item.yAxisID = ds.yAxisID;
        }
        return item;
      });
    },

    buildOptions() {
      const _this = this;
      const options = {
        responsive: true,
        maintainAspectRatio: false,
        legend: { position: 'bottom', labels: { boxWidth: 12 } },
        onClick(event, elements) {
          if (elements && elements.length) {
            _this.$emit('point-click', elements[0]._index);
          }
        },
      };
      if (this.type === 'doughnut' || this.type === 'pie') {
        return options;
      }
      const yAxes = [{
        id: 'y',
        stacked: this.stacked,
        ticks: { beginAtZero: true },
        scaleLabel: { display: !!this.yLabel, labelString: this.yLabel },
      }];
      if (this.rateAxis) {
        yAxes.push({
          id: 'rate',
          position: 'right',
          gridLines: { drawOnChartArea: false },
          ticks: { beginAtZero: true, callback: v => Number((v * 100).toFixed(2)) + '%' },
        });
      }
      options.scales = {
        xAxes: [{ stacked: this.stacked, ticks: { autoSkip: false, maxRotation: 45 } }],
        yAxes: yAxes,
      };
      options.tooltips = {
        mode: 'index',
        intersect: false,
        callbacks: {
          label(item, data) {
            const ds = data.datasets[item.datasetIndex];
            const v = Number(item.yLabel);
            const text = ds.yAxisID === 'rate' ? (v * 100).toFixed(1) + '%' : (Math.round(v * 100) / 100);
            return ds.label + '：' + text;
          }
        }
      };
      return options;
    },

    async render() {
      const seq = ++this.renderSeq;
      if (this.empty) {
        this.destroyChart();
        return;
      }
      const Chart = await loadChartJs();
      await this.$nextTick();
      if (this.destroyed || seq !== this.renderSeq || !this.$refs.canvas) {
        return;
      }
      if (this.chart) {
        this.chart.data.labels = this.labels;
        this.chart.data.datasets = this.buildDatasets();
        this.chart.options = this.buildOptions();
        this.chart.update();
        return;
      }
      this.chart = new Chart(this.$refs.canvas, {
        type: this.type,
        data: { labels: this.labels, datasets: this.buildDatasets() },
        options: this.buildOptions(),
      });
    },

    destroyChart() {
      if (this.chart) {
        this.chart.destroy();
        this.chart = null;
      }
    }
  }
}
</script>

<style scoped>
.stat-chart {
  position: relative;
  width: 100%;
}

.stat-chart-empty {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 100%;
  color: #909399;
  font-size: 13px;
}
</style>
