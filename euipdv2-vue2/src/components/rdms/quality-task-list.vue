<!--
 * Author: 周朔鹏
 * Date: 2025-02-05
 * Email: 391902958@qq.com
 * Website: https://www.euipd.com
-->

<template>
  <div>
    <div style="display: flex; justify-content: flex-start;">
      <pagination
          key="quality_recognize"
          ref="pagination_quality"
          v-bind:list="listByProjectId_handling"
          v-bind:itemCount="6"
      ></pagination>
    </div>
    <div>
      <el-table
          :data="qualityList"
          border
          stripe
          highlight-current-row
          style="width: 100%"
          @row-click="rowClick">
        <el-table-column  label="序号" align="center" width="50">
          <template slot-scope="scope">
            {{scope.$index+1}}
          </template>
        </el-table-column>
        <el-table-column
            prop="qualitySerial"
            width="180"
            align="center"
            label="处理编号"
        ></el-table-column>
        <el-table-column
            prop="name"
            align="center"
            show-overflow-tooltip
            label="问题标题"
        ></el-table-column>
        <el-table-column
            prop="projectName"
            show-overflow-tooltip
            align="center"
            sortable
            label="关联项目"
        ></el-table-column>
        <el-table-column
            prop="subprojectName"
            show-overflow-tooltip
            align="center"
            sortable
            label="关联子项目"
        ></el-table-column>
        <el-table-column
            prop="consumer"
            show-overflow-tooltip
            align="center"
            sortable
            label="需求方名称"
        ></el-table-column>
        <el-table-column
            prop="writerName"
            sortable
            align="center"
            width="110"
            label="反馈人"
        ></el-table-column>
        <el-table-column
            prop="updateTime"
            :formatter="updateTimeFormat"
            align="center"
            width="110"
            label="更新时间"
        ></el-table-column>
        <el-table-column
            prop="status"
            :formatter="statusFormat"
            align="center"
            label="处理状态"
            width="100"
        ></el-table-column>
        <el-table-column
            fixed="right"
            label="操作"
            align="center"
            width="120">
          <template slot-scope="scope">
            <el-button type="primary" plain size="small" v-on:click.stop="showJobitemInfo(scope.row)">
              任务信息
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!--显示任务信息-->
    <el-dialog
        :visible.sync="showJobitemDlg"
        width="29cm"
        :before-close="handleClose"
        :close-on-click-modal=false>
      <div slot="title" style="font-size: 20px;">{{ dialogTitle }}</div>
      <div v-if="showJobitemDlg && jobitemId">
        <JobitemDetailInfoDlg
            :key="jobitemId"
            :jobitem-id="jobitemId">
        </JobitemDetailInfoDlg>
      </div>
    </el-dialog>

  </div>
</template>

<script>
import TabButton from "@/components/rdms/tab-button";
import Swal from "sweetalert2";
import Pagination from "@/components/pagination.vue";
import {Loading} from "element-ui";
import {optionKV, shortDateFormat} from "@/filter/filter";
import QualityDetailPlain from "@/components/rdms/quality-detail-plain.vue";
import JobitemDetailInfoDlg from "@/components/std/jobitem-detail-info-dlg.vue";

export default {
  components: {
    JobitemDetailInfoDlg,
    QualityDetailPlain,
    Pagination,
    TabButton,
  },
  name: "quality-task-list",

  props: {
    projectId: {
      default: null
    },
  },

  data: function () {
    return {
      loginUser: {},
      loginCustomer: {},

      qualityList: [],
      QUALITY_STATUS: QUALITY_STATUS,

      qualityDetailVisible: false,
      qualityId: '',
      jobitemId: '',

      showJobitemDlg: false,
      dialogTitle: '',
    }
  },
  mounted: function () {
    let _this = this;
    _this.$refs.pagination_quality.size = 6;
    _this.getLoginUser();
    if(_this.projectId){
      _this.listByProjectId_handling(1);
    }
  },

  methods: {
    rowClick(row, column, e){
      let _this = this;
      _this.showJobitemInfo(row);
    },

    showJobitemInfo(row){
      let _this = this;
      _this.dialogTitle = '任务信息';
      if(row && row.jobitemId){
        _this.jobitemId = row.jobitemId;
        _this.showJobitemDlg = true;
      }

    },

    handleClose(done) {
      done();
    },

    closeDialog() {
      let _this = this;
      _this.showJobitemDlg = false;
    },

    statusFormat(row) {
      return optionKV(QUALITY_STATUS, row.status);
    },

    updateTimeFormat(row, column){
      return shortDateFormat(row.updateTime);
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

    listByProjectId_handling(page) {
      let _this = this;
      let loadingInstance = Loading.service({ fullscreen: true });
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/quality/listByProjectId_handling', {
        page: page,
        size: _this.$refs.pagination_quality.size,
        customerId: _this.loginCustomer.id, //customerId
        keyWord: _this.projectId,
      }).then((response)=>{
        loadingInstance.close();
        let resp = response.data;
        if (resp.success) {
          _this.qualityList = resp.content.list;
          _this.$refs.pagination_quality.render(page, resp.content.total);
        } else {
          Toast.warning(resp.message,Swal);
        }
      })
    },
  }
}
</script>
