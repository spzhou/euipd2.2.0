<!--
 * Author: 周朔鹏
 * Date: 2025-02-27
 * Email: 391902958@qq.com
 * Website: https://www.euipd.com
-->

<template>
  <div>
    <div class="row">
      <div style="margin: 0 15px;">
        <p>缺陷反馈: 缺陷反馈是为了公开收集产品缺陷而设置的功能页面. 注意: 缺陷管理是研发过程中发现的问题, 提交给研发部门进行修复. 而质量管理是对已经发布的产品进行质量管理.</p>
        <div class="hr hr-18 dotted hr-double"></div>
        <div style="display: flex; justify-content: space-between;">
          <pagination
              key="bug_feedback_list_recognize"
              ref="pagination_bug"
              v-bind:list="list"
              v-bind:itemCount="6"
          ></pagination>
          <div>
            <el-button type="primary" size="small" style="margin-right: 15px;" @click="createBugFeedback()">缺陷反馈单</el-button>
          </div>
        </div>
        <div>
          <el-table
              :data="bugList"
              border
              stripe
              highlight-current-row
              style="width: 100%"
              @row-click="rowClick">
            <el-table-column  label="序号" align="center" width="50">
              <template slot-scope="scope">
                {{scope.$index+1}}
              </template>
            </el-table-column>
            <el-table-column
                prop="bugSerial"
                width="180"
                align="center"
                label="处理编号"
            ></el-table-column>
            <el-table-column
                prop="name"
                align="center"
                show-overflow-tooltip
                label="缺陷标题"
            ></el-table-column>
            <el-table-column
                prop="projectName"
                show-overflow-tooltip
                align="center"
                sortable
                label="关联项目"
            ></el-table-column>
            <el-table-column
                prop="subprojectName"
                show-overflow-tooltip
                align="center"
                sortable
                label="关联子项目"
            ></el-table-column>
            <el-table-column
                prop="writerName"
                sortable
                align="center"
                width="110"
                label="反馈人"
            ></el-table-column>
            <el-table-column
                prop="updateTime"
                :formatter="updateTimeFormat"
                align="center"
                width="110"
                label="更新时间"
            ></el-table-column>
            <el-table-column
                prop="status"
                :formatter="statusFormat"
                align="center"
                label="处理状态"
                width="100"
            ></el-table-column>
            <el-table-column
                fixed="right"
                label="操作"
                align="center"
                width="150">
              <template slot-scope="scope">
                <el-button v-if="scope.row.status === QUALITY_STATUS.SAVE.key" type="primary" plain size="small" v-on:click.stop="submitBugFeedback(scope.row)">
                  编辑/提交
                </el-button>
                <el-button v-if="scope.row.status === QUALITY_STATUS.RECHECK.key" type="primary" plain size="small" v-on:click.stop="recheckBugFeedback(scope.row)">
                  <el-badge :is-dot="scope.row.status === QUALITY_STATUS.RECHECK.key" >
                    缺陷复核
                  </el-badge>
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </div>
    </div>

    <!--    提交缺陷-->
    <el-dialog
        :visible.sync="dialogVisible"
        width="29cm"
        :before-close="handleClose"
        :close-on-click-modal=false>
      <div slot="title" style="font-size: 20px;">{{ dialogTitle }}</div>
      <BugfeedbackCreateDlg
          :key="timestamp"
          :bug-item="bugItem"
          :close-function="closeDialog">
      </BugfeedbackCreateDlg>
    </el-dialog>

    <!--    缺陷复核-->
    <el-dialog
        :visible.sync="recheckDialogVisible"
        width="29cm"
        :before-close="handleClose"
        :close-on-click-modal=false>
      <div slot="title" style="font-size: 20px;">{{ dialogTitle }}</div>
      <BugRecheckDlg
          :key="timestamp"
          :bug-item="bugItem"
          :close-function="closeDialog">
      </BugRecheckDlg>
    </el-dialog>

    <!--    缺陷详情-->
    <el-dialog
        :visible.sync="bugDetailVisible"
        width="29cm"
        :before-close="handleClose"
        :close-on-click-modal=false>
      <BugfeedbackDetailPlain
          :key="bugFeedbackId"
          :bugfeedback-id="bugFeedbackId">
      </BugfeedbackDetailPlain>
    </el-dialog>

  </div>
</template>

<script>
import TabButton from "@/components/rdms/tab-button";
import File from "@/components/rdms/file";
import Swal from "sweetalert2";
import {dateFormat, fixed2fractional, optionKV, rmbFormat, shortDateFormat} from "@/filter/filter";
import SubprojectTaskTab from "@/components/rdms/subproject-task-tab.vue";
import DevelopCharacterTaskTab from "@/components/rdms/develop-character-task-tab.vue";
import FeeApproveInfoDlg from "@/components/std/fee-approve-info-dlg.vue";
import FeeDealWithDlg from "@/components/rdms/fee-deal-with-dlg.vue";
import Pagination from "@/components/pagination.vue";
import BugfeedbackCreateDlg from "@/components/rdms/bugfeedback-create-dlg.vue";
import BugfeedbackDetailPlain from "@/components/rdms/bugfeedback-detail-plain.vue";
import BugRecheckDlg from "@/components/rdms/bug-recheck-dlg.vue";
import {Loading} from "element-ui";

export default {
  components: {
    BugfeedbackCreateDlg, BugRecheckDlg, BugfeedbackDetailPlain,
    Pagination,
    SubprojectTaskTab,
    DevelopCharacterTaskTab,
    File,
    TabButton,
    FeeDealWithDlg,
    FeeApproveInfoDlg,
  },
  name: "bug-feedback-list",

  props: {},

  data: function () {
    return {
      loginUser: {},
      loginCustomer: {},
      jobItem: {},
      jobitemShowList: [],

      bugList: [],
      QUALITY_STATUS: QUALITY_STATUS,

      activePageId: 'home',
      historyPageId: 'home',

      dialogVisible: false,
      recheckDialogVisible: false,

      dialogTitle: '',
      bugDetailVisible: false,
      timestamp: '',
      bugItem: null,
      bugFeedbackId: '',

      essentialAnalysisItems: [],
      PROJECT_TYPE: PROJECT_TYPE,
      ENTRANCE_CLASS: ENTRANCE_CLASS,

      modifyJobitemDlg: false,
    }
  },
  mounted: function () {
    let _this = this;
    _this.$refs.pagination_bug.size = 6;

    _this.getLoginUser();
    _this.list(1);

    _this.$bus.$on('refresh-bug-feedback-list', (data) => {
      if(data){
        _this.list(1);
      }
    });
  },

  beforeDestroy: function () {
    let _this = this;
    _this.$bus.$off('refresh-bug-feedback-list');
  },

  methods: {
    createBugFeedback() {
      let _this = this;
      _this.timestamp = new Date().getTime();
      _this.dialogVisible = true;
      _this.bugItem = null;
      _this.dialogTitle = "缺陷反馈单";
    },

    submitBugFeedback(bugfeedback) {
      let _this = this;
      _this.timestamp = new Date().getTime();
      _this.dialogVisible = true;
      _this.bugItem = bugfeedback;
      _this.dialogTitle = "缺陷反馈单";
    },

    recheckBugFeedback(bugfeedback) {
      let _this = this;
      _this.timestamp = new Date().getTime();
      _this.recheckDialogVisible = true;
      _this.bugItem = bugfeedback;
      _this.dialogTitle = "缺陷处理复核";
    },


    rowClick(row, column, e){
      let _this = this;
      if(row.status === QUALITY_STATUS.RECHECK.key){
        _this.recheckBugFeedback(row);
      }else{
        _this.showBugFeedback(row);
      }
    },

    showBugFeedback(bugfeedback) {
      let _this = this;
      _this.timestamp = new Date().getTime();
      _this.bugDetailVisible = true;
      _this.bugItem = bugfeedback;
      _this.bugFeedbackId = bugfeedback.id;
      _this.dialogTitle = "缺陷反馈单";
    },

    handleClose(done) {
      done();
    },

    closeDialog() {
      let _this = this;
      _this.dialogVisible = false;
      _this.recheckDialogVisible = false;
      _this.bugDetailVisible = false;
    },

    statusFormat(row) {
      return optionKV(QUALITY_STATUS, row.status);
    },

    updateTimeFormat(row, column){
      return shortDateFormat(row.updateTime);
    },

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

    list(page) {
      let _this = this;
      let loadingInstance = Loading.service({ fullscreen: true });
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/bugfeedback/list', {
        page: page,
        size: _this.$refs.pagination_bug.size,
        customerId: _this.loginCustomer.id, //customerId
        actor: _this.loginUser.customerUserId,
      }).then((response)=>{
        loadingInstance.close();
        let resp = response.data;
        if (resp.success) {
          _this.bugList = resp.content.list;
          _this.$refs.pagination_bug.render(page, resp.content.total);
        } else {
          Toast.warning(resp.message,Swal);
        }
      })
    },

    tabHomeClick() {
      let _this = this;
      _this.activePageId = 'home';
      _this.historyPageId = 'home';
    },

  }
}
</script>

<style lang="scss">
@import './../../../public/static/scss/caipd.scss';

</style>
