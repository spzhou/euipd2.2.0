<!--
 * Author: 周朔鹏
 * Date: 2026-10-11
 * Email: 391902958@qq.com
 * Website: https://www.euipd.com
-->

<template>
  <div class="dialog-container">
    <el-form label-width="130px" :show-message=false class="dialog-width">
      <el-row>
        <el-col :span="22">
          <el-form-item label="任务名称">{{ jobItem.jobName }}</el-form-item>
        </el-col>
      </el-row>
      <el-row>
        <el-col :span="11">
          <el-form-item label="任务类型">{{ typeText }}</el-form-item>
        </el-col>
        <el-col :span="11">
          <el-form-item label="任务状态">{{ statusText }}</el-form-item>
        </el-col>
      </el-row>
      <el-row>
        <el-col :span="11">
          <el-form-item label="执行人">{{ jobItem.executorName }}</el-form-item>
        </el-col>
        <el-col :span="11">
          <el-form-item label="任务工时">{{ manhourText }} 标准工时</el-form-item>
        </el-col>
      </el-row>
      <el-row>
        <el-col :span="11">
          <el-form-item label="下发时间">{{ formatDateTime(jobItem.createTime) }}</el-form-item>
        </el-col>
        <el-col :span="11">
          <el-form-item label="计划提交时间">{{ formatDay(jobItem.planSubmissionTime) }}</el-form-item>
        </el-col>
      </el-row>
      <el-row>
        <el-col :span="11">
          <el-form-item label="实际提交时间">{{ formatDateTime(jobItem.actualSubmissionTime) || '--' }}</el-form-item>
        </el-col>
        <el-col :span="11">
          <el-form-item label="下发方式">{{ jobItem.fromPlan ? '按计划下发' : '即时下发' }}</el-form-item>
        </el-col>
      </el-row>
      <el-row>
        <el-col :span="22">
          <el-form-item label="任务描述">
            <div v-rich-html="jobItem.taskDescription"></div>
          </el-form-item>
        </el-col>
      </el-row>
    </el-form>

    <div class="view-hint">已下发任务的工期请在工期管理中调整</div>

    <div class="clearfix form-actions">
      <div style="display: flex; justify-content: center; gap: 8px;">
        <button v-if="jobItem.timeEditable" type="button" class="ca-btn-primary" @click="gotoTimeManage()">工期管理</button>
        <button type="button" class="ca-btn-primary" @click="close()">关闭</button>
      </div>
    </div>
  </div>
</template>

<script>
import Swal from "sweetalert2";
import {Loading} from "element-ui";
import {dateFormat, optionKV, shortDateFormat} from "@/filter/filter";

export default {
  name: "gantt-jobitem-view-dlg",

  props: {
    jobitemId: {
      type: String,
      default: null,
    },
    closeDlg: {
      type: Function,
      default: null,
    },
  },

  data() {
    return {
      loginUser: {},
      jobItem: {},
    };
  },

  computed: {
    typeText() {
      return optionKV(JOB_ITEM_TYPE, this.jobItem.type) || this.jobItem.type || '';
    },
    statusText() {
      return optionKV(JOB_ITEM_STATUS, this.jobItem.status) || this.jobItem.status || '';
    },
    manhourText() {
      const h = Number(this.jobItem.manhour || 0);
      return h.toFixed(2);
    },
  },

  mounted() {
    this.loginUser = Tool.getActorUser() || {};
    if (this.jobitemId) {
      this.getDetail();
    }
  },

  methods: {
    getDetail() {
      let loadingInstance = Loading.service({ fullscreen: true });
      let formData = new window.FormData();
      formData.append('jobitemId', this.jobitemId);
      formData.append('loginUserId', this.loginUser.customerUserId || '');
      this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/job-items/getJobitemDetailInfo', formData).then((response) => {
        loadingInstance.close();
        const resp = response.data;
        if (resp.success && resp.content) {
          this.jobItem = resp.content;
        } else {
          Toast.warning(resp.message || '工单不存在', Swal);
        }
      }).catch(() => {
        loadingInstance.close();
        Toast.warning('读取工单失败', Swal);
      });
    },

    formatDay(value) {
      return value ? shortDateFormat(value) : '';
    },

    formatDateTime(value) {
      return value ? dateFormat(value) : '';
    },

    gotoTimeManage() {
      const jobId = this.jobItem.id;
      this.close();
      this.$router.push({ path: '/customer/jobitem', query: { jobId } }).catch(() => {});
    },

    close() {
      if (this.closeDlg) {
        this.closeDlg();
      }
    },
  },
};
</script>

<style lang="scss" scoped>
@import './../../../public/static/scss/caipd.scss';

.dialog-width {
  width: 100%;
}
.view-hint {
  margin: 0 0 12px 130px;
  font-size: 12px;
  color: #909399;
}
</style>
