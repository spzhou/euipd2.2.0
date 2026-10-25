<!--
 * Author: 周朔鹏
 * Date: 2024-06-12
 * Email: 391902958@qq.com
 * Website: https://www.euipd.com
-->

<!-- -->
<template>
  <div class="tab-container">
    <div class="tab-left">
      <div style="height: calc(100vh - 278px); overflow: scroll; overflow-x: hidden">
        <el-table
            ref="issueItemTable"
            :data="subprojectJobitemMix"
            row-key="id"
            border
            default-expand-all
            stripe
            style="width: 100%;"
            highlight-current-row
            :tree-props="{children: 'children', hasChildren: 'hasChildren'}"
            @row-click="rowClick">
          <el-table-column label="序号" align="center" width="80">
            <template slot-scope="scope" v-if="scope.row.parentOrChildren === PARENT_CHILDREN.CHILDREN.key">
              {{ scope.$index }}
            </template>
          </el-table-column>
          <el-table-column
              prop="itemName"
              label="项目/任务名称"
              show-overflow-tooltip
              min-width="350">
          </el-table-column>
          <el-table-column
              fixed="right"
              label="操作/状态"
              align="center"
              width="102">
            <template slot-scope="scope">
              <div v-if="scope.row.parentOrChildren === PARENT_CHILDREN.PARENT.key">
                <div v-if="isReviewing || isDevCompleted">
                  资料汇总
                </div>
                <div v-else>
                  <el-button type="primary" size="small" v-on:click="issueJobitem(scope.row)">
                    创建任务
                  </el-button>
                </div>
              </div>
              <div v-else>
                {{ JOB_ITEM_STATUS | optionKV( scope.row.status)  }}
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
      <div v-if="currentRow && currentRow.parentOrChildren === PARENT_CHILDREN.PARENT.key">
        <SubprojectExecuteInfoPage
            :key="currentRow.id"
            :sub-project-id="currentRow.id"
            :entrance="entrance"
            :project-type="PROJECT_TYPE.SUB_PROJECT.key"
        ></SubprojectExecuteInfoPage>
      </div>
      <div v-if="currentRow && currentRow.parentOrChildren === PARENT_CHILDREN.CHILDREN.key">
        <JobitemDetailInfoPage
            :key="currentRow.id"
            :jobitem-id="currentRow.id"
            :doc-type="DOC_TYPE.JOBITEM.key"
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
      <div v-if="currentRow.parentOrChildren === PARENT_CHILDREN.PARENT.key">
        <JobitemCreateByProjectIdDlg
            :key="((currentRow.id) + '-' + (random))"
            :x-project-id="currentRow.id"
            :job-type="JOB_ITEM_TYPE.TASK_SUBP.key"
            :project-type="projectType"
            :entrance="entrance"
            :close-dlg=closeDialog
        ></JobitemCreateByProjectIdDlg>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import File from "../../components/rdms/file";
import Swal from "sweetalert2";
import JobitemDetailInfoPage from "@/components/std/jobitem-detail-info-page.vue";
import MixProjectDetailInfoPage from "@/components/rdms/mix-project-detail-info-page.vue";
import JobitemCreateByProjectIdDlg from "@/components/std/jobitem-create-by-project-id-dlg.vue";

import { optionKV } from "@/filter/filter";
import SubprojectExecuteInfoPage from "@/components/rdms/subproject-execute-info-page.vue";

export default {
  components: {
    SubprojectExecuteInfoPage,
    File,
    JobitemDetailInfoPage,
    MixProjectDetailInfoPage,
    JobitemCreateByProjectIdDlg,
  },
  name: "subproject-task-issue",

  props: {
    subProjectId: {
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

      jobitemId: {
        default: null,
      },
      subprojectJobitemMix: [],

      isReviewing: false,
      isDevCompleted: false,

      issueJobitemDlg: false,
      dialogTitle: '',

      PARENT_CHILDREN: PARENT_CHILDREN,
      DOC_TYPE: DOC_TYPE,
      FILE_GROUPING: FILE_GROUPING,
      JOB_ITEM_TYPE: JOB_ITEM_TYPE,
      PROJECT_TYPE: PROJECT_TYPE,
      ENTRANCE_CLASS: ENTRANCE_CLASS,
      JOB_ITEM_STATUS: JOB_ITEM_STATUS,

      currentRow: {},
      dlgProject: {},

      random: 0,
    }
  },

  computed: {},

  mounted: function () {
    let _this = this;
    _this.getLoginUser();
    _this.random = Math.random();
    if(Tool.isNotEmpty(_this.subProjectId) ){
      _this.getSubProjectAndTaskJobitems(_this.subProjectId);
    }

    _this.$bus.$on('refresh-subproject-task-issue', (data) => {
      if(data){
        _this.getSubProjectAndTaskJobitems(_this.subProjectId);
      }
    });
  },

  beforeDestroy(){
    let _this = this;
    _this.$bus.$off('refresh-subproject-task-issue');
  },

  methods: {
    issueJobitem(currentRow) {
      let _this = this;
      _this.dlgProject = currentRow;
      _this.random = Math.random();
      _this.issueJobitemDlg = true;
      _this.dialogTitle = "下发任务";
    },
    closeDialog() {
      let _this = this;
      _this.issueJobitemDlg = false;
    },
    handleClose(done) {
      done();
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
    },

    getSubProjectAndTaskJobitems(subProjectId) {
      let _this = this;
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/project/getSubProjectAndTaskJobitems/' + subProjectId).then((response)=>{
        let resp = response.data;
        if (resp.success) {
          let respItemMix = resp.content;
          _this.isReviewing = respItemMix.isReviewing;
          _this.isDevCompleted = respItemMix.isDevCompleted;
          let respItems = [];
          respItems.push(respItemMix);
          _this.subprojectJobitemMix =respItems;
          _this.currentRow = _this.subprojectJobitemMix[0];
          _this.$refs.issueItemTable.setCurrentRow(_this.currentRow);

          _this.$bus.$emit('refresh-subproject-task-issue', false);
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
