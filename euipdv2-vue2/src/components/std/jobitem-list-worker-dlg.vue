<!--
 * Author: 周朔鹏
 * Date: 2025-01-13
 * Email: 391902958@qq.com
 * Website: https://www.euipd.com
-->

<template>
  <div>
    <div class="dialog-container" style="margin-left: 10px; margin-right: 10px;">
      <el-table
          :data="jobItems"
          border
          highlight-current-row
          @row-click="rowClick"
          style="width: 100%">
        <el-table-column
            prop="jobSerial"
            align="center"
            label="任务号"
            show-overflow-tooltip
            width="160"
        ></el-table-column>
        <el-table-column
            prop="type"
            align="center"
            label="任务类型"
            :formatter="jobitemTypeFormat"
            min-width="95"
        ></el-table-column>
        <el-table-column
            prop="processStatus"
            align="center"
            label="任务状态"
            :formatter="processStatusFormat"
            width="80"
        ></el-table-column>
        <el-table-column
            prop="jobName"
            align="center"
            show-overflow-tooltip
            :min-width="200"
            label="任务名称"
        ></el-table-column>
        <el-table-column
            prop="projectName"
            align="center"
            :min-width="200"
            label="预立项 / 项目名称"
            show-overflow-tooltip
            :formatter="projectNameFormat"
        ></el-table-column>
        <el-table-column
            prop="subprojectName"
            align="center"
            :min-width="200"
            label="子项目名称"
            show-overflow-tooltip
        ></el-table-column>
        <el-table-column
            prop="executorName"
            align="center"
            label="执行人"
            width="90"
        ></el-table-column>
        <el-table-column
            prop="planSubmissionTimeStr"
            label="计划提交时间"
            align="center"
            min-width="100"
        ></el-table-column>
      </el-table>

      <div class="clearfix form-actions">
        <div style="display: flex; justify-content: center;">
          <button type="button" v-on:click="close" class="ca-btn-primary">关 闭</button>
        </div>
      </div>
    </div>

    <!--任务信息 显示对话框-->
    <el-dialog
        title="任务信息"
        width="29cm"
        append-to-body
        :visible.sync="jobItemInfoVisible"
        :close-on-click-modal=true>
      <div>
        <JobitemDetailInfoDlg
            :key="('show- ' + (jobItem.id))"
            ref="jobItemDetail_show"
            :jobitem-id="jobItem.id">
        </JobitemDetailInfoDlg>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import Swal from "sweetalert2";
import {dateFormat, optionKV, rmbFormat, shortDateFormat} from "@/filter/filter";
import MaterialAddDlg from "@/components/std/material-add-dlg.vue";
import FileSaver from 'file-saver';
import JobitemDetailInfoDlg from "@/components/std/jobitem-detail-info-dlg.vue";

export default {
  components: {
    JobitemDetailInfoDlg,
    MaterialAddDlg,
  },
  name: "jobitem-list-worker-dlg",

  props: {
    workerId: {
      default: null
    },
    closeDlg: {
      type: Function,
      default: null
    },
  },

  data() {
    return {
      loginUser: {},
      loginCustomer: {},

      jobItems: [],
      jobItem: {},
      // dialogTitle: '',
      jobItemInfoVisible: false,
    };
  },

  computed: {
  },

  mounted: function () {
    let _this = this;
    _this.getLoginUser();
    _this.list_jobitems(_this.workerId);
  },

  methods: {
    getLoginUser(){
      let _this = this;
      if(Tool.getLoginUser().userGroup === LOGIN_USER_GROUP.CUSTOMER.key){
        _this.loginUser = Tool.getLoginUser().adminUser;
        _this.loginCustomer =Tool.getLoginUser();
      }else{
        _this.loginUser = Tool.getLoginUser();
        _this.loginCustomer = Tool.getLoginUser().customer;
      }
    },

    rowClick(row, column, e){
      let _this = this;
      _this.jobItem = row;
      _this.jobItemInfoVisible = true;
      // _this.dialogTitle = "任务清单";
    },
    closeDialog() {
      let _this = this;
      _this.jobItemInfoVisible = false;
    },

    jobitemTypeFormat(row) {
      return optionKV(JOB_ITEM_TYPE, row.type);
    },

    processStatusFormat(row) {
      return optionKV(JOB_ITEM_PROCESS, row.processStatus);
    },

    projectNameFormat(row){
      if(Tool.isEmpty(row.projectName)) {
        return row.preProjectName;
      }
      return row.projectName;
    },

    close(){
      let _this = this;
      _this.closeDlg();
    },

    list_jobitems(workerId) {
      let _this = this;
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/job-items/jobListByLoginUserId/' + workerId).then((response)=>{
        let resp = response.data;
        if (resp.success) {
          _this.jobItems = resp.content;
        } else {
          Toast.warning(resp.message,Swal);
        }
      })
    },
  },
}
</script>

<style lang="scss" scoped>
@import '../../../public/static/scss/caipd';
</style>
