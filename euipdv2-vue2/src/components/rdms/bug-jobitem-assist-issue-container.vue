<!--
 * Author: 周朔鹏
 * Date: 2025-02-26
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
            :tree-props="{children: 'children', hasChildren: 'hasChildren'}"
            @row-click="rowClick">
          <el-table-column   label="序号" align="center" width="80">
            <template slot-scope="scope" >
              <div v-if="scope.row.showFlag === 'children'">
                {{ scope.$index }}
              </div>
              <div v-else>
                原任务
              </div>
            </template>
          </el-table-column>
          <el-table-column
              prop="jobName"
              label="任务名称"
              width="300">
          </el-table-column>
          <el-table-column
              fixed="right"
              label="操作/状态"
              align="center"
              width="102">
            <template slot-scope="scope">
              <div v-if="scope.row.showFlag !== 'children'">
                <el-button type="primary" size="small" v-on:click="issueJobitem(scope.row)">
                  创建任务
                </el-button>
              </div>
              <div v-else>
                {{ JOB_ITEM_STATUS | optionKV(scope.row.status) }}
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
      <div v-if="jobitem && jobitem.parentOrChildren === PARENT_CHILDREN.PARENT.key">
        <div>
          <JobitemDetailInfoPage
              :key="jobitem.id"
              :doc-type="DOC_TYPE.JOBITEM.key"
              :jobitem-id="jobitem.id"
          ></JobitemDetailInfoPage>
        </div>
      </div>
      <div v-if="jobitem && jobitem.parentOrChildren === PARENT_CHILDREN.CHILDREN.key">
        <JobitemDetailInfoPage
            :key="jobitem.id + 'jobitem'"
            :jobitem-id="jobitem.id"
            :doc-type="jobitem.docType"
        ></JobitemDetailInfoPage>
      </div>
    </div>

    <!--创建任务-->
    <el-dialog
        width="29cm"
        :visible.sync="issueJobitemDlg"
        :before-close="handleClose"
        :close-on-click-modal=false>
      <div slot="title" style="font-size: 20px;">{{ dialogTitle }}</div>
      <div>
        <JobitemCreateByJobitemIdDlg
            :key="('assist-' + (dlgJobItem.id) + '-' + (random))"
            :jobitem-id="dlgJobItem.id"
            :project-type="projectType"
            :job-type="computeJobType"
            :inherit-job-type="jobType"
            :pre-fix="'缺陷'"
            :index="index"
            :entrance="entrance"
            :close-dlg = closeDialog
        ></JobitemCreateByJobitemIdDlg>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import File from "../../components/rdms/file";
import Swal from "sweetalert2";
import JobitemDetailInfoPage from "@/components/std/jobitem-detail-info-page.vue";
import JobitemCreateByJobitemIdDlg from "@/components/std/jobitem-create-by-jobitem-id-dlg.vue";

import {optionKV} from "@/filter/filter";

export default {
  components: {
    File,
    JobitemDetailInfoPage,
    JobitemCreateByJobitemIdDlg,
  },
  name: "bug-jobitem-assist-issue-container",

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
      PARENT_CHILDREN: PARENT_CHILDREN,
      DOC_TYPE: DOC_TYPE,
      JOB_ITEM_TYPE: JOB_ITEM_TYPE,
      JOB_ITEM_STATUS: JOB_ITEM_STATUS,

      currentRow: {},
      jobitem: {},
      dlgJobItem: {},

    }
  },

  computed: {
    computeJobType(){
      let _this = this;
      return JOB_ITEM_TYPE.ASSIST.key;  //固定为 协作任务
    },
  },

  mounted: function () {
    let _this = this;
    _this.random = Math.random();
    _this.getLoginUser();
    if(Tool.isNotEmpty(_this.jobitemId)){
      _this.getJobitemAndAllAssistJobitems(_this.jobitemId);
    }
    _this.dialogTitle = "协作任务";

    _this.$bus.$on('refresh-bug-jobitem-assist-issue-container', (data) => {
      if(data){
        _this.getJobitemAndAllAssistJobitems(_this.jobitemId);
      }
    });
  },

  beforeDestroy(){
    let _this = this;
    _this.$bus.$off('refresh-bug-jobitem-assist-issue-container');
  },

  methods: {
    issueJobitem(jobitem) {
      let _this = this;
      _this.random = Math.random(); //每次点击按钮, 都创建一个新的对话框
      _this.dlgJobItem = jobitem;
      _this.issueJobitemDlg = true;
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

    getJobitemAndAllAssistJobitems(jobitemId) {
      let _this = this;
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/job-items/getJobitemAndAllAssistJobitems/' + jobitemId).then((response)=>{
        let resp = response.data;
        if (resp.success) {
          _this.jobitemList = resp.content;
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
