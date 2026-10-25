<!--
 * Author: 周朔鹏
 * Date: 2025-02-25
 * Email: 391902958@qq.com
 * Website: https://www.euipd.com
-->

<!-- -->
<template>
  <div class="tab-container">
    <div class="tab-left">
      <div style="height: calc(100vh - 278px); overflow: scroll; overflow-x: hidden">
        <el-table
            ref="issueJobitemTable"
            :data="items"
            row-key="id"
            border
            default-expand-all
            stripe
            style="width: 100%;"
            highlight-current-row
            :tree-props="{children: 'children', hasChildren: 'hasChildren'}"
            @row-click="rowClick">
          <el-table-column   label="序号" align="center" width="70">
            <template slot-scope="scope" v-if="scope.row.docType === DOC_TYPE.JOBITEM.key">
              {{ scope.row.index }}
            </template>
          </el-table-column>
          <el-table-column
              prop="itemName"
              label="缺陷/任务名称"
              width="300">
          </el-table-column>
          <el-table-column
              fixed="right"
              label="操作/状态"
              align="center"
              width="102">
            <template slot-scope="scope">
              <div v-if="scope.row.docType === DOC_TYPE.BUG.key ">
                <div v-if="scope.row.children === null || scope.row.children.length === 0">
                  <el-button type="primary" size="small" v-on:click="issueJobitem(scope.row)">
                    创建任务
                  </el-button>
                </div>
                <div v-else>
                  缺陷处理
                </div>
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
      <div v-if="items.length > 0 && currentRow.docType === DOC_TYPE.BUG.key">
        <BugfeedbackDetailPage
            :key="currentRow.id"
            :bugfeedback-id="currentRow.id"
        ></BugfeedbackDetailPage>
      </div>
      <div v-if="items.length > 0 && currentRow.docType === DOC_TYPE.JOBITEM.key">
        <JobitemDetailInfoPage
            :key="currentRow.id + 'jobitem'"
            :jobitem-id="currentRow.id"
            :doc-type="currentRow.docType"
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
        <JobitemCreateByBugfeedbackIdDlg
            :key="('bugTask-' + (bugfeedbackItem.id) + '-' + (random))"
            :bugfeedback-id="bugfeedbackItem.id"
            :project-type="projectType"
            :job-type="jobType"
            :entrance="entrance"
            :close-dlg = closeDialog
        ></JobitemCreateByBugfeedbackIdDlg>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import File from "../../components/rdms/file";
import Swal from "sweetalert2";
import PaginationSmall from "@/components/pagination-small";
import JobitemDetailInfoPage from "@/components/std/jobitem-detail-info-page.vue";
import CharacterDetailInfoPage from "@/components/rdms/character-detail-info-page.vue";

import {optionKV, shortDateFormat} from "@/filter/filter";
import QualityHandleEdit from "@/components/rdms/quality-handle-edit.vue";
import QualityInfoPage from "@/components/rdms/quality-info-page.vue";
import JobitemCreateByBugfeedbackIdDlg from "@/components/std/jobitem-create-by-bugfeedback-id-dlg.vue";
import QualityDetailPlain from "@/components/rdms/quality-detail-plain.vue";
import BugfeedbackDetailPage from "@/components/rdms/bugfeedback-detail-page.vue";

export default {
  components: {
    BugfeedbackDetailPage,
    QualityDetailPlain,
    File,
    PaginationSmall,
    JobitemDetailInfoPage,
    CharacterDetailInfoPage,
    QualityHandleEdit,
    QualityInfoPage,
    JobitemCreateByBugfeedbackIdDlg,
  },
  name: "bug-task-jobitem-issue",

  props: {
    bugfeedbackId: {
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
    }
  },

  data: function () {
    return {
      loginUser: {},
      loginCustomer: {},
      lineIndex: 0,

      issueJobitemDlg: false,
      dialogTitle: '',
      random: 0,

      status: '',

      CHARACTER_STATUS: CHARACTER_STATUS,
      DOC_TYPE: DOC_TYPE,
      JOB_ITEM_STATUS: JOB_ITEM_STATUS,

      character: {},
      bugfeedback: {},
      items: [],
      currentRow: {},
      bugfeedbackItem: {},

      hasIntJobitem: false,

    }
  },

  computed: {},

  mounted: function () {
    let _this = this;
    _this.getLoginUser();

    if(Tool.isNotEmpty(_this.bugfeedbackId)){
      _this.getBugAndJobitemList(_this.bugfeedbackId);
    }

    _this.$bus.$on('refresh-bug-task-jobitem-issue', (data) => {
      if(data){
        _this.getBugAndJobitemList(_this.bugfeedbackId);
      }
    });
  },

  beforeDestroy(){
    let _this = this;
    _this.$bus.$off('refresh-bug-task-jobitem-issue');
  },

  methods: {
    issueJobitem(bugTask) {
      let _this = this;
      _this.bugfeedbackItem = bugTask;
      _this.issueJobitemDlg = true;
      _this.dialogTitle = "创建缺陷处理任务";
      _this.random = Math.random();
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
        _this.currentRow = row; //用于显示当前行的状态
    },

    getBugAndJobitemList(bugfeedbackId) {
      let _this = this;
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/bugfeedback/getBugAndJobitemList/' + bugfeedbackId).then((response)=>{
        let resp = response.data;
        if (resp.success) {
          _this.bugfeedback = resp.content;
          _this.items = [];
          _this.items.push(_this.bugfeedback);
          if(_this.items.length > 0){
            _this.currentRow = _this.items[0];
            _this.$refs.issueJobitemTable.setCurrentRow(_this.currentRow);
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
