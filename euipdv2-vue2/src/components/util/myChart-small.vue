<!--
 * Author: 周朔鹏
 * Date: 2025-01-13
 * Email: 391902958@qq.com
 * Website: https://www.euipd.com
-->

<template>
  <div style="width: 100%;" class="chartBox">
    <div class="chart-wrap">
      <canvas ref="chartCanvas" :id="chartId"></canvas>
    </div>
    <div style="display: flex; flex-direction: row; justify-content: center; align-items: center; margin-top:-5px; padding-bottom: 5px; font-size: 14px;">
      <div style="margin-right: 3px;">姓名: </div>
      <div style="margin-right: 10px; font-weight: 500;">
        <a href="javascript:void(0)" @click="showJobitemList(worker.id)">{{ worker.trueName }}</a>
      </div>
      <div style="margin-right: 3px;">职级:</div>
      <div style="margin-right: 10px;"> {{ worker.jobLevel }}</div>
      <div style="margin-right: 3px;">岗位名称:</div>
      <div> {{ worker.jobPosition }}</div>
    </div>

    <!--  显示工作人员的所有在执行工单  -->
    <el-dialog
        width="40cm"
        :visible.sync="showJobitemListDlg"
        :before-close="handleClose"
        :close-on-click-modal=false>
      <div slot="title" style="font-size: 20px;">{{ dialogTitle }}</div>
      <div>
        <JobitemListWorkerDlg
            :key="worker.id"
            :worker-id="worker.id"
            :close-dlg="closeDialog"
        ></JobitemListWorkerDlg>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import Swal from "sweetalert2";
import JobitemListWorkerDlg from "@/components/std/jobitem-list-worker-dlg.vue";
import workloadChartMixin from "./workload-chart-mixin";

export default {

  name: 'myChartSmall',
  components: {JobitemListWorkerDlg},
  mixins: [workloadChartMixin],

  data() {
    return {
      worker: {},
      showJobitemListDlg: false,
      dialogTitle: '',
    };
  },

  mounted: function () {
    this.getWorkerInfo();
  },

  watch: {
    executorId() {
      this.worker = {};
      this.getWorkerInfo();
    }
  },

  methods: {
    showJobitemList(){
      let _this = this;
      _this.showJobitemListDlg = true;
      _this.dialogTitle = "任务清单";
    },
    closeDialog() {
      let _this = this;
      _this.showJobitemListDlg = false;
    },
    handleClose(done) {
      done(); //el-dialog用于关闭对话框的内置函数.
    },

    getWorkerInfo() {
      let _this = this;
      const executorId = _this.executorId;
      if (Tool.isEmpty(executorId)) {
        return;
      }
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/worker/get-worker-info/' + executorId).then((response)=>{
        if (_this.isDestroyed || executorId !== _this.executorId) {
          return;
        }
        let resp = response.data;
        if (resp.success) {
          _this.worker = resp.content;
        } else {
          Toast.warning(resp.message, Swal);
        }
      }).catch((error) => {
        if (!_this.isDestroyed) {
          console.error('获取工作人员信息失败:', error);
        }
      });
    },
  }
};
</script>

<style lang="scss" scoped>
@import './../../../public/static/scss/caipd.scss';
.chartBox{
  margin: 8px 0;
  box-shadow: 0 0 0.2cm rgba(0, 0, 0, 0.2);
}
.chart-wrap {
  position: relative;
  width: 600px;
  max-width: 100%;
  height: 200px;
  margin: 0 0 10px 0;
}
</style>
