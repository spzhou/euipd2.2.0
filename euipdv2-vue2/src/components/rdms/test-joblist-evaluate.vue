<!--
 * Author: 周朔鹏
 * Date: 2024-07-01
 * Email: 391902958@qq.com
 * Website: https://www.euipd.com
-->

<!-- -->
<template>
  <div class="tab-container">
    <div class="tab-left">
      <pagination-small ref="pagination" v-bind:list="getJobitemListBySubprojectId" v-bind:itemCount="3"></pagination-small>
      <div style="height: calc(100vh - 278px); overflow: scroll; overflow-x: hidden">
        <el-table
            ref="examineItemTable"
            :data="items"
            row-key="id"
            border
            default-expand-all
            stripe
            style="width: 100%;"
            highlight-current-row
            @row-click="rowClick">
          <el-table-column   label="序号" align="center" width="80">
            <template slot-scope="scope">
              {{ scope.$index + 1}}
            </template>
          </el-table-column>
          <el-table-column
              prop="jobName"
              label="任务名称"
              width="300">
          </el-table-column>
          <el-table-column
              fixed="right"
              label="状态"
              align="center"
              width="80">
            <template slot-scope="scope">
              {{ JOB_ITEM_STATUS | optionKV(scope.row.status) }}
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
      <div v-if="items.length > 0 ">
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
  </div>
</template>

<script>
import File from "../../components/rdms/file";
import Swal from "sweetalert2";
import PaginationSmall from "@/components/pagination-small";
import JobitemTestEvaluatePage from "@/components/std/jobitem-test-evaluate-page.vue";

import {optionKV, shortDateFormat} from "@/filter/filter";

export default {
  components: {
    File,
    PaginationSmall,
    JobitemTestEvaluatePage,
  },
  name: "test-joblist-evaluate",

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
      lineIndex: 0,

      directJobItemDlg: false,
      dialogTitle: '',

      operator: '',
      status: '',
      ACTOR_TYPE: ACTOR_TYPE,
      CHARACTER_STATUS: CHARACTER_STATUS,
      JOB_ITEM_TYPE: JOB_ITEM_TYPE,
      DOC_TYPE: DOC_TYPE,
      PROJECT_TYPE: PROJECT_TYPE,
      ENTRANCE_CLASS: ENTRANCE_CLASS,
      JOB_ITEM_STATUS: JOB_ITEM_STATUS,

      items: [],
      currentRow: {},
      jobItem: {},

    }
  },

  computed: {},

  mounted: function () {
    let _this = this;
    _this.getLoginUser();

    _this.$refs.pagination.size = 12;

    if(Tool.isNotEmpty(_this.subProjectId)){
      _this.getJobitemListBySubprojectId(1);
    }

    _this.$bus.$on('refresh-test-joblist-evaluate', (data) => {
      if(data){
        _this.getJobitemListBySubprojectId(1);
      }
    });
  },

  beforeDestroy(){
    let _this = this;
    _this.$bus.$off('refresh-test-joblist-evaluate');
  },

  methods: {
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

    getJobitemListBySubprojectId(page) {
      let _this = this;
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/job-items/listJobitemListBySubProjectId',{
        page: page,
        size: _this.$refs.pagination.size,
        actor: null,
        keyWord: _this.subProjectId,
        type: _this.jobType,
        status: JOB_ITEM_STATUS.SUBMIT.key,
      }).then((response)=>{
        let resp = response.data;
        if (resp.success) {
          _this.items = resp.content.list;
          if(_this.items.length > 0){
            _this.currentRow = _this.items[0];
            _this.$refs.examineItemTable.setCurrentRow(_this.currentRow);
            _this.$refs.pagination.render(page, resp.content.total);
          }
          _this.$bus.$emit('refresh-test-joblist-testing', false);
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
