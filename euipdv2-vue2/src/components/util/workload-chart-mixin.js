/**
 * 工作负荷折线图公共逻辑：拉取执行人负荷数据，渲染 Chart.js 图表，并回传未来 5 个工作日平均负荷。
 * 使用方模板需提供 ref="chartCanvas" 的 canvas，其父容器须为相对定位且设定高度（图表不保持宽高比，随容器缩放）。
 */
import Swal from "sweetalert2";

const DATASET_STYLES = [
  {
    label: "每日工作负荷图( 小时 / 日期 )",
    backgroundColor: "rgba(64, 158, 255, 0.6)",
    borderColor: "#409EFF",
    borderWidth: 2
  },
  {
    label: "平均工作负荷( 小时 / 日期 )",
    backgroundColor: "rgba(255,255,255,0.21)",
    borderColor: "#a340ff",
    borderWidth: 1
  },
  {
    label: "未来5个工作日平均工作负荷( 小时 / 日期 )",
    backgroundColor: "rgba(255,255,255,0.21)",
    borderColor: "#ffb340",
    borderWidth: 1
  }
];

function loadChartJs() {
  return import(/* webpackChunkName: "chartjs" */ 'chart.js').then(mod => mod.default || mod);
}

function toHours(value) {
  const hours = Number(value);
  return Number.isFinite(hours) ? hours : 0;
}

function buildConfig(labels, series) {
  return {
    type: "line",
    data: {
      labels,
      datasets: DATASET_STYLES.map((style, i) => Object.assign({}, style, { data: series[i] }))
    },
    options: {
      responsive: true,
      maintainAspectRatio: false,
      scales: {
        yAxes: [
          {
            ticks: {
              beginAtZero: true,
              padding: 25
            }
          }
        ]
      }
    }
  };
}

export default {
  props: {
    chartId: {
      default: null,
    },
    executorId: {
      default: null,
    },
    overLoadCallback: {
      type: Function,
      default: null,
    }
  },

  created() {
    // Chart 实例持有 DOM 与循环引用，放在 data 外以免被 Vue 深度观测
    this.chartInstance = null;
    this.isDestroyed = false;
    this.workloadSeq = 0;
  },

  mounted() {
    this.getWorkload(this.executorId);
  },

  watch: {
    executorId(val) {
      this.getWorkload(val);
    }
  },

  beforeDestroy() {
    this.isDestroyed = true;
    this.destroyChart();
  },

  methods: {
    getWorkload(executorId) {
      const seq = ++this.workloadSeq;
      if (Tool.isEmpty(executorId)) {
        this.destroyChart();
        return;
      }
      this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/worker/getWorkload/' + executorId).then((response) => {
        if (this.isDestroyed || seq !== this.workloadSeq) {
          return;
        }
        const resp = response.data;
        if (!resp.success) {
          Toast.warning(resp.message, Swal);
          return;
        }
        const labels = [];
        const series = [[], [], []];
        (resp.content || []).forEach((item) => {
          if (item && item.dateStr != null && item.dayLoad != null
              && item.averageDayLoad != null && item.averageDayLoadOfWeek != null) {
            labels.push(item.dateStr);
            series[0].push(toHours(item.dayLoad));
            series[1].push(toHours(item.averageDayLoad));
            series[2].push(toHours(item.averageDayLoadOfWeek));
          }
        });
        if (typeof this.overLoadCallback === 'function') {
          this.overLoadCallback(series[2]);
        }
        return this.renderChart(labels, series, seq);
      }).catch((error) => {
        if (!this.isDestroyed) {
          console.error('获取工作负荷数据失败:', error);
        }
      });
    },

    async renderChart(labels, series, seq) {
      const Chart = await loadChartJs();
      await this.$nextTick();
      if (this.isDestroyed || seq !== this.workloadSeq) {
        return;
      }
      const canvas = this.$refs.chartCanvas;
      if (!canvas) {
        return;
      }
      if (this.chartInstance) {
        this.chartInstance.data.labels = labels;
        series.forEach((data, i) => {
          this.chartInstance.data.datasets[i].data = data;
        });
        this.chartInstance.update();
        return;
      }
      this.chartInstance = new Chart(canvas, buildConfig(labels, series));
    },

    destroyChart() {
      if (this.chartInstance) {
        this.chartInstance.destroy();
        this.chartInstance = null;
      }
    }
  }
};
