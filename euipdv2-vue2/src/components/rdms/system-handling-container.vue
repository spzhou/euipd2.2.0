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
      <pagination-small ref="pagination" v-bind:list="getJobitemHandlingList_subproject" v-bind:itemCount="3"></pagination-small>
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
              label="任务名称"
              width="400">
          </el-table-column>
          <el-table-column
              fixed="right"
              prop="planSubmissionTime"
              :formatter="dateFormat"
              label="计划提交"
              width="90">
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
            :key="currentRow.id"
            :jobitem-id="currentRow.id"
            :job-type="currentRow.type"
            :project-type="currentRow.projectType"
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

import {shortDateFormat} from "@/filter/filter";

export default {
  components: {
    File,
    PaginationSmall,
    JobitemDetailInfoPage,
  },
  name: "system-handling-container",

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

      operator: '',
      status: '',
      ACTOR_TYPE: ACTOR_TYPE,
      CHARACTER_STATUS: CHARACTER_STATUS,
      JOB_ITEM_TYPE: JOB_ITEM_TYPE,

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
      _this.getJobitemHandlingList_subproject(1);
    }

    _this.$bus.$on('refresh-system-handling-container-subproject', (data) => {
      if(data){
        _this.getJobitemHandlingList_subproject(1);
      }
    });


  },

  beforeDestroy(){
    let _this = this;
    _this.$bus.$off('refresh-system-handling-container-subproject');
  },

  methods: {
    dateFormat(row, column) {
      return shortDateFormat(row.planSubmissionTime);
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
      _this.currentRow = row; //用于显示当前行的状态
    },

    getJobitemHandlingList_subproject(page) {
      let _this = this;
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/job-items/getSystemJobitemHandlingList',{
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
            _this.currentRow = _this.items[0];
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
