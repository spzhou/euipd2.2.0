<!--
 * Author: 周朔鹏
 * Date: 2024-05-26
 * Email: 391902958@qq.com
 * Website: https://www.euipd.com
-->

<!--
1. 对已经提交的功能能定义任务进行列表显示
2. 点击列表的任务, 显示任务的执行过程, 提供的附件暂时不能查看, 只能看到附件的目录显示
-->
<template>
  <div class="tab-container">
    <div class="tab-left">
      <pagination-small ref="pagination" v-bind:list="getJobitemExamineList_subproject" v-bind:itemCount="3"></pagination-small>
      <div style="height: calc(100vh - 278px); overflow: scroll; overflow-x: hidden">
        <el-table
            ref="editingItemTable"
            :data="items"
            row-key="id"
            border
            default-expand-all
            stripe
            style="width: 100%; "
            highlight-current-row
            :tree-props="{children: 'children', hasChildren: 'hasChildren'}"
            @row-click="rowClick">
          <el-table-column   label="序号" align="center" width="80">
            <template slot-scope="scope" v-if="scope.row.showFlag === 'jobitem'">
              {{(scope.row.showFlag === 'jobitem') ?  scope.row.index : ''}}
            </template>
          </el-table-column>
          <el-table-column
              prop="jobName"
              label="任务/特性名称"
              width="400">
          </el-table-column>
          <el-table-column
              fixed="right"
              align="center"
              prop="docType"
              :formatter="docTypeFormat"
              label="文件类型"
              width="70">
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
      <div v-if="items.length > 0 && currentRow.docType === DOC_TYPE.JOBITEM.key">
        <JobitemDetailInfoPage
            :key="currentRow.id + 'jobitem'"
            :jobitem-id="currentRow.id"
            :job-type="currentRow.type"
            :doc-type="currentRow.docType"
            :project-type="currentRow.projectType"
            :index="index"
            :entrance="entrance"
        ></JobitemDetailInfoPage>
      </div>
      <div v-if="items.length > 0 && currentRow.docType === DOC_TYPE.CHARACTER.key">
        <CharacterDetailExaminePage
            :key="currentRow.id + 'character'"
            :character="currentRow"
            :doc-type="currentRow.docType"
            :project-type="projectType"
            :index="index"
            :entrance="entrance"
        ></CharacterDetailExaminePage>
      </div>
    </div>
  </div>
</template>

<script>
import File from "../../components/rdms/file";
import Swal from "sweetalert2";
import PaginationSmall from "@/components/pagination-small";
import JobitemDetailInfoPage from "@/components/std/jobitem-detail-info-page.vue";
import CharacterDetailExaminePage from "@/components/rdms/character-detail-examine-page";

import {optionKV, shortDateFormat} from "@/filter/filter";

export default {
  components: {
    File,
    PaginationSmall,
    JobitemDetailInfoPage,
    CharacterDetailExaminePage,
  },
  name: "system-examine-container",

  props: {
    xProjectId: {
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

      operator: '',
      status: '',
      ACTOR_TYPE: ACTOR_TYPE,
      CHARACTER_STATUS: CHARACTER_STATUS,
      JOB_ITEM_TYPE: JOB_ITEM_TYPE,
      DOC_TYPE: DOC_TYPE,
      PROJECT_TYPE: PROJECT_TYPE,

      items: [],
      currentRow: {},

    }
  },

  computed: {},

  mounted: function () {
    let _this = this;
    _this.getLoginUser();

    _this.$refs.pagination.size = 12;

    if(Tool.isNotEmpty(_this.xProjectId)){
      _this.getJobitemExamineList_subproject(1);
    }

    _this.$bus.$on('refresh-system-examine-container-subproject', (data) => {
      if(data){
        _this.getJobitemExamineList_subproject(1);
      }
    });


  },

  beforeDestroy(){
    let _this = this;
    _this.$bus.$off('refresh-system-examine-container-subproject');
  },

  methods: {
/*    dateFormat(row, column) {
      return shortDateFormat(row.planSubmissionTime);
    },*/

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

    testEmit(){
      console.log('emitEvent')
    },

    rowClick(row, column, e){
      let _this = this;
        _this.currentRow = row;
    },

    getJobitemExamineList_subproject(page) {
      let _this = this;
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/job-items/getSystemJobitemExamineList',{
        page: page,
        size: _this.$refs.pagination.size,
        actor: _this.loginUser.customerUserId,
        keyWord: _this.xProjectId,
        type: PROJECT_TYPE.DEV_PROJECT.key,
      }).then((response)=>{
        let resp = response.data;
        if (resp.success) {
          _this.items = resp.content.list;
          if(_this.items.length > 0){
            // _this.currentRow = _this.items[0];
            if(Tool.isNotEmpty_ES5(_this.items)){
              if(Tool.isNotEmpty(_this.items[0].children)){
                _this.currentRow = _this.items[0].children[0];
              }else{
                _this.currentRow = _this.items[0];
              }
            }else{
              _this.currentRow = _this.items[0];
            }
            _this.$refs.editingItemTable.setCurrentRow(_this.currentRow);
            _this.$refs.pagination.render(page, resp.content.total);
          }
          _this.$bus.$emit('refresh-system-joblist-tab-num', true);

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
