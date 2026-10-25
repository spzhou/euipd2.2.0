<!--
 * Author: 周朔鹏
 * Date: 2024-05-26
 * Email: 391902958@qq.com
 * Website: https://www.euipd.com
-->

<!-- -->
<template>
  <div class="tab-container">
    <div class="tab-left">
      <div style="height: calc(100vh - 278px); overflow: scroll; overflow-x: hidden">
        <el-table
            ref="editingItemTable"
            :data="jobitemList"
            row-key="id"
            border
            default-expand-all
            stripe
            style="width: 100%;"
            highlight-current-row
            @row-click="rowClick">
          <el-table-column   label="序号" align="center" width="80">
            <template slot-scope="scope" >
              {{ scope.$index + 1 }}
            </template>
          </el-table-column>
          <el-table-column
              prop="jobName"
              label="任务名称"
              width="300">
          </el-table-column>
          <el-table-column
              fixed="right"
              label="操作状态"
              align="center"
              width="102">
            <template slot-scope="scope">
              <div>
                {{ JOB_ITEM_STATUS |optionKV( scope.row.status) }}
              </div>
            </template>
          </el-table-column>
          <el-table-column
              fixed="right"
              width="35">
            <template slot-scope="scope">
              <i v-show="scope.row.id === currentRow.id" class="el-icon-caret-right" style="font-size: 24px; color: #4794EF;"></i>
            </template>
          </el-table-column>
        </el-table>
      </div>

    </div>
    <div class="tab-right">
      <div v-if="jobitem" >
        <JobitemDetailInfoPage
            :key="jobitem.id + 'jobitem'"
            :jobitem-id="jobitem.id"
            :doc-type="jobitem.docType"
        ></JobitemDetailInfoPage>
      </div>
    </div>
  </div>
</template>

<script>
import File from "../../components/rdms/file";
import Swal from "sweetalert2";
import PaginationSmall from "@/components/pagination-small";
import JobitemDetailInfoPage from "@/components/std/jobitem-detail-info-page.vue";
import JobitemCreateByJobitemIdDlg from "@/components/std/jobitem-create-by-jobitem-id-dlg.vue";
import JobitemReviewNotifySheetPage from "@/components/rdms/jobitem-review-notify-sheet-page.vue";
import DevelopCharacterIntegrationDocPage from "@/components/rdms/develop-character-integration-doc-page";

import {optionKV} from "@/filter/filter";

export default {
  components: {
    JobitemReviewNotifySheetPage,
    File,
    PaginationSmall,
    JobitemDetailInfoPage,
    JobitemCreateByJobitemIdDlg,
    DevelopCharacterIntegrationDocPage,
  },
  name: "review-task-issue-container",

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

  data: function () {
    return {
      loginUser: {},
      loginCustomer: {},
      random: 0,

      jobitemList: [],

      issueJobitemDlg: false,
      dialogTitle: '',

      DOC_TYPE: DOC_TYPE,
      JOB_ITEM_STATUS: JOB_ITEM_STATUS,

      currentRow: {},
      jobitem: {},
      dlgJobItem: {},

    }
  },

  computed: {},

  mounted: function () {
    let _this = this;
    _this.random = Math.random();
    _this.getLoginUser();
    if(Tool.isNotEmpty(_this.jobitemId)){
      _this.getReviewRelatedJobitems_handling(_this.jobitemId);
    }

    _this.$bus.$on('refresh-review-task-issue-container', (data) => {
      if(data){
        _this.getReviewRelatedJobitems_handling(_this.jobitemId);
      }
    });
  },

  beforeDestroy(){
    let _this = this;
    _this.$bus.$off('refresh-review-task-issue-container');
  },

  methods: {
    optionKV,
    issueJobitem(jobitem) {
      let _this = this;
      _this.random = Math.random(); //每次点击按钮, 都创建一个新的对话框
      _this.dlgJobItem = jobitem;
      _this.issueJobitemDlg = true;
      _this.dialogTitle = "协作任务";
    },
    closeDialog() {
      let _this = this;
      _this.issueJobitemDlg = false;
    },
    handleClose(done) {
      done(); //el-dialog用于关闭对话框的内置函数.
    },

    docTypeFormat(row, column) {
      return optionKV(DOC_TYPE, row.docType);
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

    rowClick(row, column, e){
      let _this = this;
      _this.currentRow = row;
      _this.jobitem = row;
    },

    getReviewRelatedJobitems_handling(jobitemId) {
      let _this = this;
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/job-items/getReviewRelatedJobitems_handling/' + jobitemId).then((response)=>{
        let resp = response.data;
        if (resp.success) {
          _this.jobitemList = resp.content.childJobitemList;
          if(_this.jobitemList){
            _this.currentRow = _this.jobitemList[0];
            if(_this.$refs.editingItemTable){
              _this.$refs.editingItemTable.setCurrentRow(_this.currentRow);
            }
            _this.jobitem = _this.currentRow;
          }
        } else {
          Toast.warning(resp.message,Swal);
        }
      })
    },

  },
}
</script>

<style lang="scss" scoped>
@import './../../../public/static/scss/caipd.scss';

</style>
