<!--
 * Author: 周朔鹏
 * Date: 2025-02-27
 * Email: 391902958@qq.com
 * Website: https://www.euipd.com
-->

<template>
  <div class="paper-height">
    <div style="display: flex; justify-content: space-between;">
      <pagination
          key="pagination_bug_archive"
          ref="pagination_bug_archive"
          v-bind:list="getBuaArchiveListBySubprojectId"
          v-bind:itemCount="6"
      ></pagination>
      <div style="margin-right: 20px;">
<!--        <el-input size="mini" placeholder="请输入任务名称搜索" v-model="keyWord">
          <el-button slot="append" icon="el-icon-search" @click="search(1)"></el-button>
        </el-input>-->
      </div>
    </div>
    <div>
      <el-table
          :data="bugList"
          border
          stripe
          highlight-current-row
          style="width: 100%"
          @row-click="showBugFeedback">
        <el-table-column  label="序号" align="center" width="50">
          <template slot-scope="scope">
            {{scope.$index+1}}
          </template>
        </el-table-column>
        <el-table-column
            prop="bugSerial"
            width="180"
            align="center"
            label="处理编号"
        ></el-table-column>
        <el-table-column
            prop="name"
            align="center"
            show-overflow-tooltip
            label="缺陷标题"
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
            width="150">
          <template slot-scope="scope">
            <div>
              <el-button type="primary" plain size="small" v-on:click.stop="showBugFeedback(scope.row)">
                  查看详情
              </el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!--    缺陷详情-->
    <el-dialog
        :visible.sync="bugDetailVisible"
        width="29cm"
        :before-close="handleClose"
        :close-on-click-modal=false>
      <div v-if="bugDetailVisible">
        <BugfeedbackDetailPlain
            :key="bugFeedbackId"
            :bugfeedback-id="bugFeedbackId">
        </BugfeedbackDetailPlain>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import Swal from "sweetalert2";
import { Loading } from 'element-ui';
import { optionKV, shortDateFormat} from "@/filter/filter";
import Pagination from "@/components/pagination.vue";
import BugfeedbackDetailPlain from "@/components/rdms/bugfeedback-detail-plain.vue";

export default {
  components: {
    BugfeedbackDetailPlain,
    Pagination

  },
  name: "bug-subproject-archive-list",

  props: {
    subprojectId: {
      default: null,
    },
  },

  data: function () {
    return {
      loginUser: {},
      loginCustomer: {},

      bugList: [],
      dialogTitle: '',
      bugDetailVisible: false,
      bugFeedbackId: '',
      keyWord: '',
    }
  },

  computed: {
  },

  mounted: function () {
    let _this = this;
    _this.$refs.pagination_bug_archive.size = 12;
    _this.getLoginUser();
    if(_this.subprojectId !== null){
      _this.getBuaArchiveListBySubprojectId(1);
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

    statusFormat(row) {
      return optionKV(QUALITY_STATUS, row.status);
    },
    updateTimeFormat(row, column){
      return shortDateFormat(row.updateTime);
    },

    showBugFeedback(bugfeedback) {
      let _this = this;
      _this.bugDetailVisible = true;
      _this.bugFeedbackId = bugfeedback.id;
      _this.dialogTitle = "缺陷反馈单";
    },

    handleClose(done) {
      done();
    },

    closeDialog() {
      let _this = this;
      _this.bugDetailVisible = false;
    },

    getBuaArchiveListBySubprojectId(page) {
      let _this = this;
      let loadingInstance = Loading.service({ fullscreen: true });
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/bugfeedback/getBuaArchiveListBySubprojectId', {
        page: page,
        size: _this.$refs.pagination_bug_archive.size,
        customerId: _this.loginCustomer.id,
        subprojectId: _this.subprojectId,
      }).then((response)=>{
        loadingInstance.close();
        let resp = response.data;
        if (resp.success) {
          _this.bugList = resp.content.list;
          _this.$refs.pagination_bug_archive.render(page, resp.content.total);
        } else {
          Toast.warning(resp.message,Swal);
        }
      })
    },

/*    list_jobitems(page) {
      let _this = this;
      _this.currentPageNo = page;
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/job-items/getBugJobListBySubproject', {
        page: page,
        size: _this.$refs.pagination_bug_archive.size,
        subprojectId: _this.subprojectId,
      }).then((response) => {
        // debugger
        let resp = response.data;
        if (resp.success) {
          _this.jobItems = resp.content.list;
          _this.$refs.pagination_bug_archive.render(page, resp.content.total);
        } else {
          Toast.warning(resp.message, Swal);
        }
      })
    },*/

/*    search(page) {
      let _this = this;
      if (Tool.isNotEmpty(_this.keyWord)) {
        _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/job-items/getBugJobListBySubproject', {
          page: page,
          size: _this.$refs.pagination_bug_archive.size,
          subprojectId: _this.subprojectId,
          keyWord: _this.keyWord,
        }).then((response) => {
          let resp = response.data;
          _this.jobItems = resp.content.list;
          _this.$refs.pagination_bug_archive.render(page, resp.content.total);
        })
      } else {
        _this.list_jobitems(page);
      }
    },*/

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
