<!--
 * Author: 周朔鹏
 * Date: 2025-02-24
 * Email: 391902958@qq.com
 * Website: https://www.euipd.com
-->

<template>
  <div class="paper-height">
    <div style="display: flex; justify-content: space-between;">
      <Pagination ref="pagination" v-bind:list="list_jobitems" v-bind:itemCount="6"></Pagination>
      <div style="margin-right: 20px;">
        <el-input size="mini" placeholder="请输入任务名称搜索" v-model="keyWord">
          <el-button slot="append" icon="el-icon-search" @click="search(1)"></el-button>
        </el-input>
      </div>
    </div>
    <div>
      <el-table
          :data="jobItems"
          border
          highlight-current-row
          style="width: 100%"
          @row-click="rowClick">
        <el-table-column
            prop="jobSerial"
            align="center"
            label="任务号"
            show-overflow-tooltip
            width="160"
        ></el-table-column>
        <el-table-column
            prop="type"
            align="center"
            label="任务类型"
            :formatter="jobitemTypeFormat"
            min-width="95"
        ></el-table-column>
        <el-table-column
            prop="processStatus"
            align="center"
            label="任务状态"
            :formatter="processStatusFormat"
            width="80"
        ></el-table-column>
        <el-table-column
            prop="jobName"
            align="center"
            show-overflow-tooltip
            :min-width="200"
            label="任务名称"
        ></el-table-column>
        <el-table-column
            prop="projectName"
            align="center"
            :min-width="200"
            label="预立项 / 项目名称"
            show-overflow-tooltip
            :formatter="projectNameFormat"
        ></el-table-column>
        <el-table-column
            prop="subprojectName"
            align="center"
            :min-width="200"
            label="子项目名称"
            show-overflow-tooltip
        ></el-table-column>
        <el-table-column
            prop="executorName"
            align="center"
            label="执行人"
            width="90"
        ></el-table-column>
        <el-table-column
            prop="planSubmissionTimeStr"
            label="计划提交"
            align="center"
            min-width="100"
        ></el-table-column>
        <el-table-column
            fixed="right"
            label="操作"
            align="center"
            :width="150">
          <template slot-scope="scope">
            <el-button v-on:click.stop="jobItemExeInfoShow(scope.row)" type="primary" plain size="small">
                查看任务
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!--  显示任务详细件  -->
    <el-dialog
        width="29cm"
        :visible.sync="showJobitemDetailDlg"
        :before-close="handleClose"
        :close-on-click-modal=false>
      <div slot="title" style="font-size: 20px;">{{ dialogTitle }}</div>
      <div>
        <JobitemRelatedJobInfoDlg
            :key="jobItem.id"
            :jobitem-id="jobItem.id"
        ></JobitemRelatedJobInfoDlg>
      </div>
    </el-dialog>

  </div>
</template>

<script>
import Swal from "sweetalert2";
import { Loading } from 'element-ui';
import {formatSecond, optionKV} from "@/filter/filter";
import Pagination from "@/components/pagination.vue";
import JobitemRelatedJobInfoDlg from "@/components/std/jobitem-related-job-info-dlg.vue";

export default {
  components: {
    JobitemRelatedJobInfoDlg,
    Pagination

  },
  name: "bug-subproject-joblist",

  props: {
    subprojectId: {
      default: null,
    },
  },

  data: function () {
    return {
      loginUser: {},
      loginCustomer: {},
      jobItem: {},
      jobItems: [],
      keyWord: '',
      showJobitemDetailDlg: false,
      dialogTitle: '',
    }
  },

  computed: {
  },

  mounted: function () {
    let _this = this;
    _this.$refs.pagination.size = 12;
    _this.getLoginUser();
    if(_this.subprojectId !== null){
      _this.list_jobitems(1);
    }
  },

  methods: {
    getLoginUser() {
      let _this = this;
      if (Tool.getLoginUser().userGroup === LOGIN_USER_GROUP.CUSTOMER.key) {
        _this.loginUser = Tool.getLoginUser().adminUser;
        _this.loginCustomer = Tool.getLoginUser();
      } else {
        _this.loginUser = Tool.getLoginUser();
        _this.loginCustomer = Tool.getLoginUser().customer;
      }
    },
    rowClick(row, column, e){
      let _this = this;
      _this.jobItem = row;
      _this.showJobitemDetailDlg = true;
      _this.dialogTitle = "开发任务";
    },
    closeDialog() {
      let _this = this;
      _this.showJobitemDetailDlg = false;
    },
    handleClose(done) {
      done(); //el-dialog用于关闭对话框的内置函数.
    },

    jobitemTypeFormat(row) {
      return optionKV(JOB_ITEM_TYPE, row.type);
    },

    processStatusFormat(row) {
      return optionKV(JOB_ITEM_PROCESS, row.processStatus);
    },
    projectNameFormat(row) {
      if (Tool.isEmpty(row.projectName)) {
        return row.preProjectName;
      }
      return row.projectName;
    },

    jobItemExeInfoShow(row){
      let _this = this;
      _this.rowClick(row)

    },

    list_jobitems(page) {
      let _this = this;
      _this.currentPageNo = page;
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/job-items/getBugJobListBySubproject', {
        page: page,
        size: _this.$refs.pagination.size,
        subprojectId: _this.subprojectId,
      }).then((response) => {
        // debugger
        let resp = response.data;
        if (resp.success) {
          _this.jobItems = resp.content.list;
          _this.$refs.pagination.render(page, resp.content.total);
        } else {
          Toast.warning(resp.message, Swal);
        }
      })
    },

    search(page) {
      let _this = this;
      if (Tool.isNotEmpty(_this.keyWord)) {
        _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/job-items/getBugJobListBySubproject', {
          page: page,
          size: _this.$refs.pagination.size,
          subprojectId: _this.subprojectId,
          keyWord: _this.keyWord,
        }).then((response) => {
          let resp = response.data;
          _this.jobItems = resp.content.list;
          _this.$refs.pagination.render(page, resp.content.total);
        })
      } else {
        _this.list_jobitems(page);
      }
    },

  },
}
</script>

<style lang="scss" scoped>
@import './../../../public/static/scss/caipd.scss';
.paper-height {
  height: calc(100vh - 210px);
}
.content-height {
  height: 100%;
  overflow: scroll;
}

</style>
