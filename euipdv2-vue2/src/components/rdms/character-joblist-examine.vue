<!--
 * Author: 周朔鹏
 * Date: 2025-03-01
 * Email: 391902958@qq.com
 * Website: https://www.euipd.com
-->


<template>
  <div class="tab-container">
    <div class="tab-left">
      <el-pagination
          @size-change="handleSizeChange"
          @current-change="getJobitemExamineList"
          :current-page="currentPage"
          :page-sizes="[5, 15, 30, 50, 100]"
          :page-size="15"
          layout="sizes, prev, pager, next, total"
          :total="total">
      </el-pagination>

      <div style="height: calc(100vh - 278px); overflow: scroll; overflow-x: hidden">
        <el-table
            ref="editingItemTable"
            :data="items"
            row-key="id"
            border
            default-expand-all
            stripe
            style="width: 100%;"
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
              show-overflow-tooltip
              min-width="140">
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
            :doc-type="currentRow.docType"
            :job-type="currentRow.type"
            :index="index"
            :project-type="projectType"
            :entrance="entrance"
        ></JobitemDetailInfoPage>
      </div>
      <div v-if="items.length > 0 && currentRow.docType === DOC_TYPE.CHARACTER.key">
        <character-detail-examine-page
            :key="currentRow.id + 'character'"
            :character="currentRow"
            :doc-type="currentRow.docType"
            :project-type="projectType"
            :index="index"
            :entrance="entrance"
        ></character-detail-examine-page>
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

import {optionKV } from "@/filter/filter";

export default {
  components: {
    File,
    PaginationSmall,
    JobitemDetailInfoPage,
    CharacterDetailExaminePage,
  },
  name: "character-joblist-examine",

  props: {
    preProject: {
      default: null,
    },
    projectType: {
      default: null,
    },
    index: {
      default: null,
    },
    docType: {
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

      DOC_TYPE: DOC_TYPE,
      PROJECT_TYPE: PROJECT_TYPE,
      ENTRANCE_CLASS: ENTRANCE_CLASS,

      items: [],
      currentRow: {},

      total: 0,
      pageSize: 15,
      currentPage: 1,
    }
  },

  computed: {},

  mounted: function () {
    let _this = this;
    _this.getLoginUser();

    if (typeof _this.preProject === 'object' && _this.preProject !== null && _this.preProject.id) {
      // _this.preProject 是一个有效的对象
      _this.getJobitemExamineList(1);
    }

    _this.$bus.$on('refresh-character-joblist-examine', (data) => {
      if(data){
        _this.getJobitemExamineList(1);
      }
    });
  },

  beforeDestroy(){
    let _this = this;
    _this.$bus.$off('refresh-character-joblist-examine');
  },

  methods: {
    handleSizeChange(val) {
      let _this = this;
      _this.pageSize = val;
      _this.getJobitemExamineList(1)
    },

    docTypeFormat(row) {
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

    rowClick(row ){
      let _this = this;
      _this.currentRow = row; //用于显示当前行的状态

    },

    getJobitemExamineList(page) {
      let _this = this;
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/job-items/getJobitemExamineList',{
        page: page,
        size: _this.pageSize,
        actor: _this.loginUser.customerUserId,
        keyWord: _this.preProject.id,
        type: PROJECT_TYPE.PRE_PROJECT.key,
      }).then((response)=>{
        let resp = response.data;
        if (resp.success) {
          _this.items = resp.content.list;
          if(_this.items.length > 0){
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
            _this.total = resp.content.total;
          }
          _this.$bus.$emit('refreshCharacterNum', true);

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
