<!--
 * Author: 周朔鹏
 * Date: 2025-02-26
 * Email: 391902958@qq.com
 * Website: https://www.euipd.com
-->

<template>
  <div style="height: 100%;">
    <div class="paper-height">
      <div style="height: 100%; overflow: scroll;">
        <div class="a4-paper-big-simple">
          <div style="margin: 50px 60px;">
            <small style="float: right; margin-bottom: 15px; margin-right: 10px;"> <em>操作指引: 点击条目可以查看详细信息.</em></small>
            <el-table
                :data="milestoneTaskJobList"
                row-key="id"
                stripe
                highlight-current-row
                @row-click="rowClick"
                style="width: 100%">
              <el-table-column
                  prop="jobSerial"
                  align="center"
                  show-overflow-tooltip
                  width="180"
                  label="任务号">
              </el-table-column>
              <el-table-column
                  prop="jobName"
                  align="center"
                  show-overflow-tooltip
                  min-width="180"
                  label="任务名称">
              </el-table-column>
              <el-table-column
                  prop="executorName"
                  align="center"
                  show-overflow-tooltip
                  width="100"
                  label="执行人">
              </el-table-column>
              <el-table-column
                  prop="status"
                  :formatter="statusFormat"
                  align="center"
                  show-overflow-tooltip
                  width="90"
                  label="执行状态">
              </el-table-column>
              <el-table-column
                  prop="planSubmissionTime"
                  :formatter="shortTimeFormat"
                  align="center"
                  show-overflow-tooltip
                  width="100"
                  label="计划完成时间">
              </el-table-column>
              <el-table-column
                  fixed="right"
                  label="操作"
                  align="center"
                  width="100">
                <template slot-scope="scope">
                  <div>
                    <div style="display: flex; flex-direction: row; justify-content: center;">
                      <div>
                        <el-tooltip content="查看执行过程和执行提交情况" placement="top"
                                    effect="light" :open-delay=1000>
                          <el-button style="margin: 0 5px;"
                                     type="primary"
                                     plain
                                     size="small"
                                     @click.stop="showJobitemProcess(scope.row)">
                            查看
                          </el-button>
                        </el-tooltip>
                      </div>
                    </div>
                  </div>
                </template>
              </el-table-column>
            </el-table>
          </div>
        </div>
      </div>
    </div>

    <!--显示任务过程信息-->
    <el-dialog
        width="29cm"
        :visible.sync="showJobitemProcessDlg"
        :before-close="handleClose"
        :close-on-click-modal=false>
      <div slot="title" style="font-size: 20px;">{{ dialogTitle }}</div>
      <div v-if="showJobitemProcessDlg">
        <JobitemDetailInfoDlg
            :key="('detail-' + (JobitemDetail.id))"
            :jobitem-id="JobitemDetail.id"
        ></JobitemDetailInfoDlg>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import Swal from "sweetalert2";
import JobitemDetailInfoDlg from "@/components/std/jobitem-detail-info-dlg.vue";
import {optionKV, rmbFormat, shortDateFormat} from "@/filter/filter";

export default {
  components: {
    JobitemDetailInfoDlg,

  },
  name: "review-milestone-task-list-page",

  props: {
    jobitemId: {
      default: null,
    },
    jobType: {
      default: null,
    },
    projectType: {
      default: null,
    },
    index: {
      default: null,
    },
    entrance: {
      default: null,
    },
  },

  computed: {

  },

  data() {
    return {
      loginUser: {},
      loginCustomer: {},
      milestoneTaskJobList: [],

      dialogTitle: '',

      jobItem: {},

      showJobitemProcessDlg: false,
      JobitemDetail: {},
    };
  },

  mounted: function () {
    let _this = this;
    _this.getLoginUser();
    if (Tool.isNotEmpty(_this.jobitemId)) {
      _this.getJobitemSimpleInfo(_this.jobitemId);
    }
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

    shortTimeFormat(row, column){
      return shortDateFormat(row.planSubmissionTime);
    },
    statusFormat(row, column) {
      return optionKV(JOB_ITEM_STATUS, row.status);
    },

    getJobitemSimpleInfo(jobitemId){
      let _this = this;
      let formData = new window.FormData();
      formData.append('jobitemId', jobitemId);
      formData.append('loginUserId', _this.loginUser.customerUserId);
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/job-items/getJobitemSimpleInfo', formData).then((response) => {
        let resp = response.data;
        if (resp.success) {
          _this.jobItem = resp.content;
          if(_this.jobItem.note){
            _this.getMilestoneTaskJobList(_this.jobItem.note);
          }
        }else{
          Toast.warning(resp.message, Swal);
        }
      });
    },

    getMilestoneTaskJobList(milestoneId){
      let _this = this;
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/job-items/getMilestoneTaskJobList/' + milestoneId).then((response) => {
        let resp = response.data;
        if (resp.success) {
          _this.milestoneTaskJobList = resp.content;
        }else{
          Toast.warning(resp.message, Swal);
        }
      });
    },

    rowClick(row, column, e){
      let _this = this;
      _this.showJobitemProcess(row);
    },
    showJobitemProcess(row) {
      let _this = this;
      _this.JobitemDetail = row;
      _this.showJobitemProcessDlg = true;
      _this.dialogTitle = "任务处理过程";
    },

    handleClose(done) {
      let _this = this;
      _this.showJobitemProcessDlg = false;
      done();
    },

    closeDialog() {
      let _this = this;
      _this.showJobitemProcessDlg = false;
    },

  },
}
</script>

<style lang="scss" scoped>
@import './../../../public/static/scss/caipd';

.paper-height {
  height: calc(100vh - 200px);
}

</style>
