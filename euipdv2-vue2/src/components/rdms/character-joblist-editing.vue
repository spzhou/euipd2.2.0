<!--
 * Author: 周朔鹏
 * Date: 2024-05-26
 * Email: 391902958@qq.com
 * Website: https://www.euipd.com
-->


<template>
  <div class="tab-container">
    <div class="tab-left">
      <el-pagination
          @size-change="handleSizeChange"
          @current-change="getJobitemHandlingList"
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
            border
            stripe
            style="width: 100%;"
            highlight-current-row
            @row-click="rowClick">
          <el-table-column   label="序号" align="center" width="50">
            <template slot-scope="scope">
              {{scope.$index+1}}
            </template>
          </el-table-column>
          <el-table-column
              prop="jobName"
              show-overflow-tooltip
              label="任务名称"
              width="250">
          </el-table-column>
          <el-table-column
              prop="planSubmissionTime"
              align="center"
              :formatter="dateFormat"
              label="计划提交"
              width="90">
          </el-table-column>
          <el-table-column
              fixed="right"
              prop="status"
              :formatter="jobitemStatusFormat"
              align="center"
              label="状态"
              width="80">
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
      <div v-if="items.length > 0">
        <JobitemDetailInfoPage
            :key="jobitemItem.id"
            :jobitem-id="jobitemItem.id"
            :index="index"
            :entrance="entrance"
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

import {optionKV, shortDateFormat} from "@/filter/filter";

export default {
  components: {
    File,
    PaginationSmall,
    JobitemDetailInfoPage,
  },
  name: "character-joblist-editing",

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
    entrance: {
      default: null,
    }
  },

  data: function () {
    return {
      loginUser: {},
      loginCustomer: {},

      operator: '',
      status: '',
      ACTOR_TYPE: ACTOR_TYPE,
      CHARACTER_STATUS: CHARACTER_STATUS,
      JOB_ITEM_TYPE: JOB_ITEM_TYPE,

      items: [],
      currentRow: {},
      jobitemItem: {},

      total: 0,
      pageSize: 15,
      currentPage: 1,
    }
  },

  computed: {},

  mounted: function () {
    let _this = this;
    _this.getLoginUser();
    if(Tool.isNotEmpty_ES5(_this.preProject)){
      _this.getJobitemHandlingList(1);
    }

    _this.$bus.$on('refresh-character-joblist-editing', (data) => {
      if(data){
        _this.getJobitemHandlingList(1);
      }
    });
  },

  beforeDestroy(){
    let _this = this;
    _this.$bus.$off('refresh-character-joblist-editing');
  },

  methods: {
    handleSizeChange(val) {
      let _this = this;
      _this.pageSize = val;
      _this.getJobitemHandlingList(1)
    },

    dateFormat(row) {
      return shortDateFormat(row.planSubmissionTime);
    },

    jobitemStatusFormat(row) {
      return optionKV(JOB_ITEM_STATUS, row.status);
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

    rowClick(row ){
      let _this = this;
      _this.currentRow = row; //用于显示当前行的状态
      _this.jobitemItem = row;
    },

    getJobitemHandlingList(page) {
      let _this = this;
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/job-items/getJobitemHandlingList',{
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
            _this.currentRow = _this.items[0];
            _this.$refs.editingItemTable.setCurrentRow(_this.currentRow);
            _this.jobitemItem = _this.currentRow;
            _this.total = resp.content.total;
          }
          _this.$bus.$emit('refresh-character-joblist-editing', false);
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
