<!--
 * Author: 周朔鹏
 * Date: 2025-03-02
 * Email: 391902958@qq.com
 * Website: https://www.euipd.com
-->

<template>
  <div style="width: 100%;">
    <div style="width: 100%;">
      <div>
        <div style="display: flex; flex-direction: row; justify-content: space-between; align-items: center;">
          <p>完工清单: 列出<strong>工作执行人员</strong>已经完成的工作任务的详细信息.</p>
          <div style="display: flex; flex-direction: row; justify-content: flex-start; align-items: center; margin-right: 20px;">
            <div style="margin-right: 3px; font-size: 14px;">请选择查询月份:</div>
            <el-date-picker
                v-model="month"
                type="month"
                size="mini"
                placeholder="选择月"
                @change="getCompletedJobitemList"
            ></el-date-picker>
          </div>
        </div>
        <div class="hr hr-18 dotted hr-double"></div>
      </div>
    </div>
    <div style="margin-top: -10px;">
      <el-table
          ref="completedList"
          :data="jobItems"
          row-key="id"
          border
          default-expand-all
          stripe
          style="width: 100%;"
          highlight-current-row
          @row-click="rowClick">
        <el-table-column   label="序号" align="center" width="80">
          <template slot-scope="scope">
            {{scope.$index + 1}}
          </template>
        </el-table-column>
        <el-table-column
            prop="jobSerial"
            label="任务号"
            align="center"
            width="170">
        </el-table-column>
        <el-table-column
            prop="jobName"
            label="任务名称">
        </el-table-column>
        <el-table-column
            prop="executorName"
            label="执行人员"
            align="center"
            width="80">
        </el-table-column>
        <el-table-column
            prop="type"
            :formatter="typeFormat"
            label="任务类型"
            align="center"
            width="100">
        </el-table-column>
        <el-table-column
            prop="createTime"
            :formatter="createTimeFormat"
            label="开始时间"
            align="center"
            width="100">
        </el-table-column>
        <el-table-column
            prop="actualSubmissionTime"
            :formatter="actualSubmissionTimeFormat"
            label="实际提交时间"
            align="center"
            width="100">
        </el-table-column>
        <el-table-column
            prop="planSubmissionTime"
            :formatter="planSubmissionTimeFormat"
            label="计划提交时间"
            align="center"
            width="100">
        </el-table-column>
        <el-table-column
            prop="evaluateScore"
            label="评分"
            align="center"
            width="50">
        </el-table-column>
        <el-table-column
            fixed="right"
            label="操作"
            align="center"
            width="120">
          <template slot-scope="scope">
            <el-button type="primary" plain size="small" @click="rowClick(scope.row)">
              任务详情
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!--  显示任务详细件  -->
    <el-dialog
        width="29cm"
        :visible.sync="showJobitemDetailDlg"
        :before-close="handleClose"
        :close-on-click-modal=false>
      <div slot="title" style="font-size: 20px;">{{ dialogTitle }}</div>
      <div v-if="showJobitemDetailDlg">
        <JobitemRelatedJobInfoDlg
            :key="jobItem.id"
            :jobitem-id="jobItem.id"
        ></JobitemRelatedJobInfoDlg>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import Swal from "sweetalert2";
import {optionKV, shortDateFormat} from "@/filter/filter";
import { Loading } from 'element-ui'
import JobitemRelatedJobInfoDlg from "@/components/std/jobitem-related-job-info-dlg.vue";

export default {
  components: {
    JobitemRelatedJobInfoDlg,
    File,
  },
  name: "worker-complete-task-list",

  props: {
    workerId: {
      default: null,
    }
  },

  data: function () {
    return {
      loginUser: {},
      loginCustomer: {},

      jobItems: [],
      jobItem: {},

      month: {},
      workerPerformanceList: [],

      showJobitemDetailDlg: false,
      dialogTitle: '',
    }
  },

  computed: {

  },

  mounted: function () {
    let _this = this;
    _this.getLoginUser();
  },

  beforeDestroy: function () {
  },

  methods: {
    getLoginUser() {
      let _this = this;
      if (Tool.getLoginUser().userGroup === LOGIN_USER_GROUP.CUSTOMER.key) {
        _this.loginUser = Tool.getLoginUser().adminUser;
        _this.loginCustomer = Tool.getLoginUser();
      } else {
        _this.loginUser = Tool.getLoginUser();
        _this.loginCustomer = Tool.getLoginUser().customer;
      }
      let currentDate = new Date();
      // 获取上个月的年份和月份
      let year = currentDate.getFullYear();
      let month = currentDate.getMonth();
      _this.month = new Date(year, month, 10);
      _this. getCompletedJobitemList(_this.month)
    },

    typeFormat(row) {
      return optionKV(JOB_ITEM_TYPE, row.type);
    },
    createTimeFormat(row, column) {
      return shortDateFormat(row.createTime);
    },

    planSubmissionTimeFormat(row, column) {
      return shortDateFormat(row.planSubmissionTime);
    },

    actualSubmissionTimeFormat(row, column) {
      return shortDateFormat(row.actualSubmissionTime);
    },

    getCompletedJobitemList(month) {
      let _this = this;
      let formData = new window.FormData();
      formData.append('month', month);
      formData.append('customerId', _this.loginCustomer.id);
      formData.append('workerId', _this.loginUser.customerUserId);
      let loadingInstance = Loading.service({ fullscreen: true });
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/job-items/getCompletedJobitemListByWorkerId', formData).then((response) => {
        let resp = response.data;
        loadingInstance.close();
        if (resp.success) {
          _this.jobItems = resp.content;
        } else {
          Toast.warning(resp.message, Swal);
        }
      })
    },

    rowClick(row, column, e){
      let _this = this;
      _this.jobItem = row;
      _this.showJobitemDetailDlg = true;
      _this.dialogTitle = "任务执行信息";
    },
    closeDialog() {
      let _this = this;
      _this.showJobitemDetailDlg = false;
    },
    handleClose(done) {
      done(); //el-dialog用于关闭对话框的内置函数.
    },


  }
}
</script>

<style lang="scss">
@import './../../../public/static/scss/caipd.scss';

</style>
