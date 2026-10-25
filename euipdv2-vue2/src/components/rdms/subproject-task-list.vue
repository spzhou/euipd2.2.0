<!--
 * Author: 周朔鹏
 * Date: 2025-03-01
 * Email: 391902958@qq.com
 * Website: https://www.euipd.com
-->

<template>
  <div class="paper-height">
    <p>任务清单: 是为<strong>产品经理</strong>提供的一个进行项目任务监管的页面.</p>
    <div class="hr hr-18 dotted hr-double"></div>
    <div>
      <el-table
          :data="taskList"
          border
          highlight-current-row
          style="width: 100%">
        <el-table-column
            prop="jobSerial"
            label="任务编号"
            min-width="160"
            show-overflow-tooltip
            align="center"
        ></el-table-column>
        <el-table-column
            prop="jobName"
            label="任务名称"
            align="center"
            min-width="200"
            show-overflow-tooltip
        ></el-table-column>
        <el-table-column
            prop="executorName"
            label="执行人"
            align="center"
            min-width="100"
            show-overflow-tooltip
        ></el-table-column>
        <el-table-column
            prop="projectManagerName"
            label="项目经理"
            align="center"
            min-width="100"
            show-overflow-tooltip
        ></el-table-column>
        <el-table-column
            prop="projectName"
            align="center"
            label="项目名称"
            show-overflow-tooltip
            min-width="200"
        ></el-table-column>
        <el-table-column
            prop="subprojectName"
            align="center"
            label="子项目名称"
            show-overflow-tooltip
            min-width="200"
        ></el-table-column>
        <el-table-column
            prop="planSubmissionTime"
            :formatter="setupedTimeFormat"
            label="计划完成"
            align="center"
            min-width="100"
        ></el-table-column>
        <el-table-column
            fixed="right"
            label="操作"
            align="center"
            width="150">
          <template slot-scope="scope">
            <el-button type="primary" plain size="small" v-on:click.stop="showJobitemInfo(scope.row)">
              任务详情
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!--显示任务信息-->
    <el-dialog
        :visible.sync="showJobitemDlg"
        width="29cm"
        :before-close="handleClose"
        :close-on-click-modal=false>
      <div slot="title" style="font-size: 20px;">{{ dialogTitle }}</div>
      <div v-if="showJobitemDlg">
        <JobitemDetailInfoDlg
            :key="jobItem.id"
            :jobitem-id="jobItem.id">
        </JobitemDetailInfoDlg>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import Swal from "sweetalert2";
import { Loading } from 'element-ui';
import {fixed2fractional, shortDateFormat} from "@/filter/filter";
import JobitemDetailInfoDlg from "@/components/std/jobitem-detail-info-dlg.vue";


export default {
  components: {
    JobitemDetailInfoDlg

  },
  name: "subproject-task-list",

  props: {
    subprojectId: {
      default: null,
    },
    index: {
      default: null,
    },
    projectType: {
      default: null,
    },
    entrance: {
      default: null,
    },
  },

  data: function () {
    return {
      loginUser: {},
      loginCustomer: {},

      jobItem: {},
      taskList: [],
      dialogTitle: '',
      showJobitemDlg: false,
    }
  },

  computed: {

  },

  mounted: function () {
    let _this = this;
    _this.getLoginUser();
    _this.getSubprojectTaskList(_this.subprojectId);
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
    },

    showJobitemInfo(row){
      let _this = this;
      _this.jobItem = row;
      _this.dialogTitle = '任务信息';
      _this.showJobitemDlg = true;
    },
    closeDialog() {
      let _this = this;
      _this.showJobitemDlg = false;
    },
    handleClose(done) {
      done();
    },

    getSubprojectTaskList() {
      let _this = this;
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/sub-project/getSubprojectTaskList/' + _this.subprojectId).then((response)=>{
        let resp = response.data;
        if (resp.success) {
          _this.taskList = resp.content;
        } else {
          Toast.warning(resp.message,Swal);
        }
      })
    },

    versionFormat(row, column) {
      return row.devVersion == null ? '' : String(row.devVersion);
    },

    setupedTimeFormat(row, column) {
      return shortDateFormat(row.planSubmissionTime);
    },

    jobItemManager(row){

    }
  },
}
</script>

<style lang="scss" scoped>
@import './../../../public/static/scss/caipd.scss';
.paper-height {
  height: calc(100vh - 210px);
}
</style>
