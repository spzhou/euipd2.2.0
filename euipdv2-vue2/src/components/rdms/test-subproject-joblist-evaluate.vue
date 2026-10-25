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
      <pagination-small ref="pagination" v-bind:list="getSubProjectAndTestJobitems" v-bind:itemCount="3"></pagination-small>
      <div style="height: calc(100vh - 278px); overflow: scroll; overflow-x: hidden">
        <el-table
            ref="evaluateItemTable"
            :data="subProjectJobitemMix"
            row-key="id"
            border
            default-expand-all
            stripe
            style="width: 100%;"
            highlight-current-row
            :tree-props="{children: 'children', hasChildren: 'hasChildren'}"
            @row-click="rowClick">
          <el-table-column   label="序号" align="center" width="80">
            <template slot-scope="scope" v-if="scope.row.parentOrChildren === PARENT_CHILDREN.CHILDREN.key">
              {{ scope.$index }}
            </template>
          </el-table-column>
          <el-table-column
              prop="itemName"
              label="任务/协作任务名称"
              width="300">
          </el-table-column>
          <el-table-column
              fixed="right"
              label="操作提示"
              align="center"
              width="102">
            <template slot-scope="scope">
              <div v-if="scope.row.parentOrChildren === PARENT_CHILDREN.PARENT.key">
                完工列表
              </div>
              <div v-else>
                项目任务
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
        <MixProjectDetailInfoPage>
        </MixProjectDetailInfoPage>
      </div>
      <div v-if="currentRow && currentRow.parentOrChildren === PARENT_CHILDREN.CHILDREN.key">
        <jobitem-test-evaluate-page
            :key="currentRow.id"
            :jobitem-id="currentRow.id"
            :job-type="jobType"
            :project-type="projectType"
            :index="index"
            :entrance="entrance"
        ></jobitem-test-evaluate-page>
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
        <JobitemCreateByProjectIdDlg
            :key="('jobitem-create-' + (random))"
            :x-project-id="dlgProject.id"
            :job-type="JOB_ITEM_TYPE.TASK_PRO.key"
            :project-type="PROJECT_TYPE.PROJECT.key"
            :entrance="ENTRANCE_CLASS.PROJECT.key"
            :close-dlg=closeDialog
        ></JobitemCreateByProjectIdDlg>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import File from "../../components/rdms/file";
import Swal from "sweetalert2";
import PaginationSmall from "@/components/pagination-small";
import JobitemDetailInfoPage from "@/components/std/jobitem-detail-info-page.vue";
import MixProjectDetailInfoPage from "@/components/rdms/mix-project-detail-info-page.vue";
import JobitemCreateByProjectIdDlg from "@/components/std/jobitem-create-by-project-id-dlg.vue";

import { optionKV } from "@/filter/filter";
import JobitemTestEvaluatePage from "@/components/std/jobitem-test-evaluate-page.vue";

export default {
  components: {
    JobitemTestEvaluatePage,
    File,
    PaginationSmall,
    JobitemDetailInfoPage,
    MixProjectDetailInfoPage,
    JobitemCreateByProjectIdDlg,
  },
  name: "test-subproject-joblist-evaluate",

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
      random: 0,

      projectId: {
        default: null,
      },
      jobitemId: {
        default: null,
      },
      subProjectJobitemMix: [],

      issueJobitemDlg: false,
      dialogTitle: '',

      PARENT_CHILDREN: PARENT_CHILDREN,
      DOC_TYPE: DOC_TYPE,
      FILE_GROUPING: FILE_GROUPING,
      JOB_ITEM_TYPE: JOB_ITEM_TYPE,
      PROJECT_TYPE: PROJECT_TYPE,
      ENTRANCE_CLASS: ENTRANCE_CLASS,

      currentRow: {},
      dlgProject: {},

    }
  },

  computed: {},

  mounted: function () {
    let _this = this;
    _this.random = Math.random();
    _this.getLoginUser();
    if(Tool.isNotEmpty(_this.subProjectId) ){
      _this.getSubProjectAndTestJobitems();
    }

    _this.$bus.$on('refresh-test-subproject-joblist-evaluate', (data) => {
      if(data){
        _this.getSubProjectAndTestJobitems();
      }
    });
  },

  beforeDestroy(){
    let _this = this;
    _this.$bus.$off('refresh-test-subproject-joblist-evaluate');
  },

  methods: {
    issueJobitem(currentRow) {
      let _this = this;
      _this.dlgProject = currentRow;
      _this.issueJobitemDlg = true;
      _this.dialogTitle = "下发任务";
    },
    closeDialog() {
      let _this = this;
      _this.random = Math.random();
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
    },

    getSubProjectAndTestJobitems() {
      let _this = this;
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/project/getSubProjectAndTestJobitems_submit/' + _this.subProjectId).then((response)=>{
        let resp = response.data;
        if (resp.success) {
          let respItemMix = resp.content;
          let respItems = [];
          respItems.push(respItemMix);
          _this.subProjectJobitemMix =respItems;
          _this.currentRow = _this.subProjectJobitemMix[0];
          _this.$refs.evaluateItemTable.setCurrentRow(_this.currentRow);
          _this.$bus.$emit('refresh-test-subproject-joblist-evaluate', false);
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
