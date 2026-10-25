<!--
 * Author: 周朔鹏
 * Date: 2025-02-26
 * Email: 391902958@qq.com
 * Website: https://www.euipd.com
-->

<template>
  <div>
    <!--Tab 卡片-->
        <WorkspaceFrame>
        <template v-slot:nav>
            <li :class="{active : activePageId==='home'}">
              <a @click="tabHomeClick">
                <i class="green ace-icon fa fa-home bigger-120"></i>
                开发执行中任务
              </a>
            </li>

            <li :class="{active : activePageId==='complete'}">
              <a @click="tabCompleteClick">
                开发已完成任务
              </a>
            </li>

            <li :class="{active : activePageId==='demand_product'}">
              <a @click="tabDemandProductClick">
                特性/产品执行中任务
              </a>
            </li>

            <li :class="{active : activePageId==='demand_product_complete'}">
              <a @click="tabDemandProductCompleteClick">
                特性/产品已完成任务
              </a>
            </li>

            <!--显示任务信息-->
            <DynamicTabItem
                v-for="(item, index) in jobitemShowList"
                :key="item.id"
                :index="index"
                :active-page-id="activePageId"
                prefix="jobitem-show-item"
                :close="closeFunction_jobitem"
                @select="tabJobitemShowClick">{{ ('绩效统计 : ' + (item.jobName)) }}
            </DynamicTabItem>

          
        </template>

                        <DynamicTabPane
                page-id="home"
                :active-page-id="activePageId">
                            <template v-slot:default>
              <p>执行中任务: 对该项目经理所管辖的所有执行中的任务进行管理.</p>
              <div class="hr hr-18 dotted hr-double"></div>
              <div style="display: flex; justify-content: space-between;">
                <pagination
                    key="execute_pagination"
                    ref="pagination_exe"
                    v-bind:list="list"
                    v-bind:itemCount="6"
                ></pagination>
                <div style="margin-right: 20px;">
                  <el-input size="mini" placeholder="请输入任务名称搜索" v-model="keyWord">
                    <el-button slot="append" icon="el-icon-search" @click="list(1)"></el-button>
                  </el-input>
                </div>
              </div>
              <div>
                <el-table
                    v-loading="tableLoading"
                    :data="jobitemList"
                    border
                    highlight-current-row
                    :row-class-name="tableRowClass"
                    style="width: 100%"
                    @row-click="rowClick">
                  <el-table-column  label="序号" align="center" width="50">
                    <template slot-scope="scope">
                      {{scope.$index+1}}
                    </template>
                  </el-table-column>
                  <el-table-column
                      prop="jobSerial"
                      width="180"
                      align="center"
                      label="任务号"
                  ></el-table-column>
                  <el-table-column
                      prop="jobName"
                      show-overflow-tooltip
                      align="center"
                      min-width="200"
                      label="任务名称"
                  ></el-table-column>
                  <el-table-column
                      prop="manhour"
                      :formatter="manhourFormat"
                      align="center"
                      min-width="100"
                      sortable
                      label="标准工时"
                  ></el-table-column>
                  <el-table-column
                      prop="executorName"
                      sortable
                      align="center"
                      min-width="100"
                      label="执行人"
                  ></el-table-column>
                  <el-table-column
                      prop="type"
                      :formatter="jobitemTypeFormat"
                      align="center"
                      min-width="100"
                      label="任务类型"
                  ></el-table-column>
                  <el-table-column
                      prop="status"
                      :formatter="jobitemStatusFormat"
                      align="center"
                      label="任务状态"
                      min-width="100"
                  ></el-table-column>
                  <el-table-column
                      prop="subprojectName"
                      align="center"
                      label="项目/子项目名称"
                      show-overflow-tooltip
                      min-width="150"
                  ></el-table-column>
                  <el-table-column
                      prop="planSubmissionTime"
                      :formatter="shortTimeFormat"
                      align="center"
                      sortable
                      label="计划交付时间"
                      show-overflow-tooltip
                      min-width="120"
                  ></el-table-column>
                  <el-table-column
                      prop="jobRemainSecond"
                      :formatter="secondFormat"
                      sortable
                      align="center"
                      label="剩余时间"
                      show-overflow-tooltip
                      min-width="150"
                  ></el-table-column>
                  <el-table-column
                      prop="milestoneTime"
                      :formatter="milestoneTimeFormat"
                      sortable
                      align="center"
                      label="里程碑时间"
                      show-overflow-tooltip
                      min-width="130"
                  ></el-table-column>
                  <el-table-column
                      fixed="right"
                      label="操作"
                      align="center"
                      width="240">
                    <template slot-scope="scope">
                      <el-button type="primary" plain size="small" v-on:click.stop="showJobitemInfo(scope.row)">
                        修改工期
                      </el-button>
                      <el-button type="primary" plain size="small" v-on:click.stop="showJobitemInfo_manhour(scope.row)">
                        修改工时
                      </el-button>
                    </template>
                  </el-table-column>
                </el-table>
              </div>
            
            
                            </template></DynamicTabPane>

                        <DynamicTabPane
                page-id="complete"
                :active-page-id="activePageId">
                            <template v-slot:default>
              <p>已完工任务: 对已经完工的任务进行列表显示, 任务归档后, 将不再显示在此页面中.</p>
              <div class="hr hr-18 dotted hr-double"></div>
              <div style="display: flex; justify-content: space-between;">
                <pagination
                    key="pagination_complete"
                    ref="pagination_complete"
                    v-bind:list="list_complete"
                    v-bind:itemCount="6"></pagination>
                <div>
                  <div style="margin-right: 20px;">
                    <el-input size="mini" placeholder="请输入任务名称搜索" v-model="keyWord">
                      <el-button slot="append" icon="el-icon-search" @click="list_complete(1)"></el-button>
                    </el-input>
                  </div>
                  <!--                  <el-button @click="createPerformance">绩效生成测试</el-button>-->
                </div>
              </div>
              <div>
                <el-table
                    :data="completeJobitemList"
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
                      prop="jobSerial"
                      width="180"
                      align="center"
                      label="任务号"
                  ></el-table-column>
                  <el-table-column
                      prop="jobName"
                      align="center"
                      label="任务名称"
                      min-width="200"
                      show-overflow-tooltip
                  ></el-table-column>
                  <el-table-column
                      prop="manhour"
                      :formatter="manhourFormat"
                      align="center"
                      sortable
                      label="标准工时"
                      min-width="110"
                  ></el-table-column>
                  <el-table-column
                      prop="executorName"
                      sortable
                      align="center"
                      label="执行人"
                      min-width="110"
                  ></el-table-column>
                  <el-table-column
                      prop="type"
                      :formatter="jobitemTypeFormat"
                      align="center"
                      label="任务类型"
                      min-width="110"
                  ></el-table-column>
                  <el-table-column
                      prop="status"
                      :formatter="jobitemStatusFormat"
                      align="center"
                      label="任务状态"
                      min-width="110"
                  ></el-table-column>
                  <el-table-column
                      prop="subprojectName"
                      align="center"
                      label="项目/子项目名称"
                      show-overflow-tooltip
                      min-width="200"
                  ></el-table-column>
                  <el-table-column
                      prop="planSubmissionTime"
                      :formatter="shortTimeFormat"
                      align="center"
                      sortable
                      label="计划交付时间"
                      show-overflow-tooltip
                      min-width="120"
                  ></el-table-column>
                  <el-table-column
                      prop="completeTime"
                      :formatter="completeTimeFormat"
                      sortable
                      align="center"
                      label="完成时间"
                      show-overflow-tooltip
                      min-width="150"
                  ></el-table-column>
                </el-table>
              </div>
            
            
                            </template></DynamicTabPane>

                        <DynamicTabPane
                page-id="demand_product"
                :active-page-id="activePageId">
                            <template v-slot:default>
              <p>特性/产品执行中任务: 对该产品经理所管辖的所有执行中的任务进行管理.</p>
              <div class="hr hr-18 dotted hr-double"></div>
              <div style="display: flex; justify-content: space-between;">
                <pagination
                    key="execute_pagination_demand_product"
                    ref="pagination_exe_demand_product"
                    v-bind:list="listJobsByPdm"
                    v-bind:itemCount="6"
                ></pagination>
                <div style="margin-right: 20px;">
                  <el-input size="mini" placeholder="请输入任务名称搜索" v-model="keyWord">
                    <el-button slot="append" icon="el-icon-search" @click="listJobsByPdm(1)"></el-button>
                  </el-input>
                </div>
              </div>
              <div>
                <el-table
                    :data="jobitemList_pdm"
                    border
                    highlight-current-row
                    :row-class-name="tableRowClass"
                    style="width: 100%"
                    @row-click="rowClick">
                  <el-table-column  label="序号" align="center" width="50">
                    <template slot-scope="scope">
                      {{scope.$index+1}}
                    </template>
                  </el-table-column>
                  <el-table-column
                      prop="jobSerial"
                      width="180"
                      align="center"
                      label="任务号"
                  ></el-table-column>
                  <el-table-column
                      prop="jobName"
                      show-overflow-tooltip
                      align="center"
                      min-width="200"
                      label="任务名称"
                  ></el-table-column>
                  <el-table-column
                      prop="manhour"
                      :formatter="manhourFormat"
                      align="center"
                      min-width="100"
                      sortable
                      label="标准工时"
                  ></el-table-column>
                  <el-table-column
                      prop="executorName"
                      sortable
                      align="center"
                      min-width="100"
                      label="执行人"
                  ></el-table-column>
                  <el-table-column
                      prop="type"
                      :formatter="jobitemTypeFormat"
                      align="center"
                      min-width="100"
                      label="任务类型"
                  ></el-table-column>
                  <el-table-column
                      prop="status"
                      :formatter="jobitemStatusFormat"
                      align="center"
                      label="任务状态"
                      min-width="100"
                  ></el-table-column>
                  <el-table-column
                      prop="subprojectName"
                      align="center"
                      label="项目/子项目名称"
                      show-overflow-tooltip
                      min-width="150"
                  ></el-table-column>
                  <el-table-column
                      prop="planSubmissionTime"
                      :formatter="shortTimeFormat"
                      align="center"
                      sortable
                      label="计划交付时间"
                      show-overflow-tooltip
                      min-width="120"
                  ></el-table-column>
                  <el-table-column
                      prop="jobRemainSecond"
                      :formatter="secondFormat"
                      sortable
                      align="center"
                      label="剩余时间"
                      show-overflow-tooltip
                      min-width="150"
                  ></el-table-column>
                  <el-table-column
                      prop="milestoneTime"
                      :formatter="milestoneTimeFormat"
                      sortable
                      align="center"
                      label="里程碑时间"
                      show-overflow-tooltip
                      min-width="130"
                  ></el-table-column>
                  <el-table-column
                      fixed="right"
                      label="操作"
                      align="center"
                      width="240">
                    <template slot-scope="scope">
                      <el-button type="primary" plain size="small" v-on:click.stop="showJobitemInfo(scope.row)">
                        修改工期
                      </el-button>
                      <el-button type="primary" plain size="small" v-on:click.stop="showJobitemInfo_manhour(scope.row)">
                        修改工时
                      </el-button>
                    </template>
                  </el-table-column>
                </el-table>
              </div>
            
            
                            </template></DynamicTabPane>

                        <DynamicTabPane
                page-id="demand_product_complete"
                :active-page-id="activePageId">
                            <template v-slot:default>
              <p>特性/产品已完工任务: 对已经完工的任务进行列表显示, 任务归档后, 将不再显示在此页面中.</p>
              <div class="hr hr-18 dotted hr-double"></div>
              <div style="display: flex; justify-content: space-between;">
                <pagination
                    key="pagination_complete_demand_product"
                    ref="pagination_complete_demand_product"
                    v-bind:list="listJobsByPdm_complete"
                    v-bind:itemCount="6"></pagination>
                <div>
                  <div style="margin-right: 20px;">
                    <el-input size="mini" placeholder="请输入任务名称搜索" v-model="keyWord">
                      <el-button slot="append" icon="el-icon-search" @click="listJobsByPdm_complete(1)"></el-button>
                    </el-input>
                  </div>
<!--                  <el-button @click="createPerformance">绩效生成测试</el-button>-->
                </div>
              </div>
              <div>
                <el-table
                    :data="completeJobitemList_pdm"
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
                      prop="jobSerial"
                      width="180"
                      align="center"
                      label="任务号"
                  ></el-table-column>
                  <el-table-column
                      prop="jobName"
                      align="center"
                      label="任务名称"
                      min-width="200"
                      show-overflow-tooltip
                  ></el-table-column>
                  <el-table-column
                      prop="manhour"
                      :formatter="manhourFormat"
                      align="center"
                      sortable
                      label="标准工时"
                      min-width="110"
                  ></el-table-column>
                  <el-table-column
                      prop="executorName"
                      sortable
                      align="center"
                      label="执行人"
                      min-width="110"
                  ></el-table-column>
                  <el-table-column
                      prop="type"
                      :formatter="jobitemTypeFormat"
                      align="center"
                      label="任务类型"
                      min-width="110"
                  ></el-table-column>
                  <el-table-column
                      prop="status"
                      :formatter="jobitemStatusFormat"
                      align="center"
                      label="任务状态"
                      min-width="110"
                  ></el-table-column>
                  <el-table-column
                      prop="subprojectName"
                      align="center"
                      label="项目/子项目名称"
                      show-overflow-tooltip
                      min-width="200"
                  ></el-table-column>
                  <el-table-column
                      prop="planSubmissionTime"
                      :formatter="shortTimeFormat"
                      align="center"
                      sortable
                      label="计划交付时间"
                      show-overflow-tooltip
                      min-width="120"
                  ></el-table-column>
                  <el-table-column
                      prop="completeTime"
                      :formatter="completeTimeFormat"
                      sortable
                      align="center"
                      label="完成时间"
                      show-overflow-tooltip
                      min-width="150"
                  ></el-table-column>
                </el-table>
              </div>
            
            
                            </template></DynamicTabPane>

            <!-- 任务页面-->
            <DynamicTabPane
                v-for="(item, index) in jobitemShowList"
                :key="('jobitem-show-item-' + (item.id))"
                :index="index"
                prefix="jobitem-show-item"
                :active-page-id="activePageId">
                <template v-slot:default>
              <JobitemOwnInfoPage
                  :key="item.id"
                  :jobitem-id="item.id"
              ></JobitemOwnInfoPage>
            
                </template></DynamicTabPane>

          
    </WorkspaceFrame>

    <!--修改工期-->
    <el-dialog
        :visible.sync="modifyJobitemDlg"
        width="25cm"
        :before-close="handleClose"
        :close-on-click-modal=false>
      <div slot="title" style="font-size: 20px;">{{ dialogTitle }}</div>
      <div v-if="modifyJobitemDlg">
        <JobitemModifyTimeLimitDlg
            :key="jobItem.id"
            :jobitem-id="jobItem.id"
            :close-dlg="closeDialog"
        ></JobitemModifyTimeLimitDlg>
      </div>
    </el-dialog>

    <!--修改工时-->
    <el-dialog
        :visible.sync="modifyJobitemDlg_manhour"
        width="25cm"
        :before-close="handleClose"
        :close-on-click-modal=false>
      <div slot="title" style="font-size: 20px;">{{ dialogTitle }}</div>
      <div v-if="modifyJobitemDlg_manhour">
        <JobitemModifyManhourDlg
            :key="jobItem.id"
            :jobitem-id="jobItem.id"
            :close-dlg="closeDialog"
        ></JobitemModifyManhourDlg>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import {closeDynamicTab, openOrFocusTab} from "@/shared/dynamic-tab";
import DynamicTabItem from "@/components/rdms/dynamic-tab-item";
import DynamicTabPane from "@/components/rdms/dynamic-tab-pane";
import WorkspaceFrame from "@/components/rdms/workspace-frame";
import Swal from "sweetalert2";
import Pagination from "@/components/pagination.vue";
import {dateFormat, formatSecond, optionKV, shortDateFormat} from "@/filter/filter";
import JobitemOwnInfoPage from "@/components/std/jobitem-own-info-page.vue";
import JobitemModifyTimeLimitDlg from "@/components/std/jobitem-modify-time-limit-dlg.vue";
import JobitemModifyManhourDlg from "@/components/std/jobitem-modify-manhour-dlg.vue";

export default {
  components: {
    Pagination,
    DynamicTabItem,
    DynamicTabPane,
    WorkspaceFrame,
    JobitemOwnInfoPage,
    JobitemModifyTimeLimitDlg,
    JobitemModifyManhourDlg,
  },
  name: "jobitem",
  data: function () {
    return {
      loginUser: {},
      loginCustomer: {},
      jobItem: {},
      jobitemShowList: [],

      jobitemList: [],
      jobitemList_pdm: [],
      completeJobitemList: [],
      completeJobitemList_pdm: [],

      activePageId: 'home',
      historyPageId: 'home',

      currentPage: 1,

      modifyJobitemDlg: false,
      modifyJobitemDlg_manhour: false,
      dialogTitle: '',
      openedByJobId: false,

      JOB_ITEM_TYPE: JOB_ITEM_TYPE,
      jobitemMaterialItemList: [],
      jobitemFeeItemList: [],

      keyWord: '',
      tableLoading: false,
    }
  },
  mounted: function () {
    let _this = this;
    _this.$refs.pagination_exe.size = 12;
    _this.getLoginUser();
    _this.list(_this.currentPage);
    _this.openModifyByRouteJobId();

    _this.$bus.$on('close-jobitem-manage-material-tab', (index)=>{
      _this.closeFunction_material(index);
    });

  },
  methods: {
    /**
     * 甘特图“工期管理”入口：按路由参数 jobId 读取工单，有权调整时直接打开修改工期对话框
     */
    openModifyByRouteJobId() {
      let _this = this;
      const jobId = _this.$route.query && _this.$route.query.jobId;
      if (!jobId) {
        return;
      }
      let formData = new window.FormData();
      formData.append('jobitemId', jobId);
      formData.append('loginUserId', _this.loginUser.customerUserId);
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/job-items/getJobitemDetailInfo', formData).then((response) => {
        let resp = response.data;
        if (resp.success && resp.content && resp.content.timeEditable) {
          _this.jobItem = resp.content;
          _this.dialogTitle = '修改工期';
          _this.openedByJobId = true;
          _this.modifyJobitemDlg = true;
        } else {
          Toast.warning(resp.success ? '您无权调整该工单的工期，或工单已结束' : resp.message, Swal);
          _this.returnFromJobIdEntry();
        }
      }).catch(() => {
        _this.returnFromJobIdEntry();
      });
    },

    returnFromJobIdEntry() {
      this.openedByJobId = false;
      if (window.history.length > 1) {
        this.$router.back();
      } else {
        this.$router.replace({ path: this.$route.path }).catch(() => {});
      }
    },

    showJobitemInfo(jobItem){
      let _this = this;
      _this.jobItem = jobItem;
      _this.dialogTitle = '修改工期';
      _this.modifyJobitemDlg = true;
    },
    showJobitemInfo_manhour(jobItem){
      let _this = this;
      _this.jobItem = jobItem;
      _this.dialogTitle = '修改工时';
      _this.modifyJobitemDlg_manhour = true;
    },


    handleClose(done) {
      done();
      if (this.openedByJobId) {
        this.returnFromJobIdEntry();
      }
    },

    closeDialog() {
      let _this = this;
      _this.modifyJobitemDlg = false;
      _this.modifyJobitemDlg_manhour = false;
      if (_this.openedByJobId) {
        _this.returnFromJobIdEntry();
        return;
      }
      _this.list(_this.currentPage);
    },

    secondFormat(row){
      if(row.jobRemainSecond){
        return formatSecond(row.jobRemainSecond);
      }else{
        return 0;
      }
    },

    completeSecondFormat(row){
      if(row.jobCompleteSecond){
        return formatSecond(row.jobCompleteSecond);
      }else{
        return 0;
      }
    },

    manhourFormat(row){
      if(row.manhour){
        return row.manhour.toFixed(2);
      }else{
        return 0;
      }

    },

    jobitemTypeFormat(row) {
      return optionKV(JOB_ITEM_TYPE, row.type);
    },

    jobitemStatusFormat(row) {
      return optionKV(JOB_ITEM_STATUS, row.status);
    },

    shortTimeFormat(row, column){
      return shortDateFormat(row.planSubmissionTime);
    },

    milestoneTimeFormat(row, column){
      if(row.milestoneTime === null){
        return "--";
      }else{
        return shortDateFormat(row.milestoneTime);
      }
    },

    completeTimeFormat(row, column){
      return dateFormat(row.completeTime);
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

    list(page) {
      let _this = this;
      _this.currentPage = page;
      _this.tableLoading = true;
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/job-items/getJobitemListByCreater', {
        page: page,
        size: _this.$refs.pagination_exe.size,
        customerId: _this.loginCustomer.id, //customerId
        actor: _this.loginUser.customerUserId,
        keyWord: _this.keyWord,
      }).then((response)=>{
        let resp = response.data;
        if (resp.success) {
          _this.jobitemList = resp.content.list;
          _this.$refs.pagination_exe.render(page, resp.content.total);
        } else {
          Toast.warning(resp.message,Swal);
        }
      }).finally(() => { _this.tableLoading = false; })
    },

    list_complete(page) {
      let _this = this;
      _this.currentPage = page;
      _this.tableLoading = true;
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/job-items/listJobitemListByPjm_complete', {
        page: page,
        size: _this.$refs.pagination_complete.size,
        customerId: _this.loginCustomer.id, //customerId
        actor: _this.loginUser.customerUserId,
        keyWord: _this.keyWord,
        status: JOB_ITEM_STATUS.COMPLETED.key,
      }).then((response)=>{
        let resp = response.data;
        if (resp.success) {
          _this.completeJobitemList = resp.content.list;
          _this.$refs.pagination_complete.render(page, resp.content.total);
        } else {
          Toast.warning(resp.message,Swal);
        }
      }).finally(() => { _this.tableLoading = false; })
    },

    listJobsByPdm(page) {
      let _this = this;
      _this.currentPage = page;
      _this.tableLoading = true;
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/job-items/listJobsByPdm', {
        page: page,
        size: _this.$refs.pagination_exe_demand_product.size,
        customerId: _this.loginCustomer.id, //customerId
        actor: _this.loginUser.customerUserId,
        keyWord: _this.keyWord,
      }).then((response)=>{
        let resp = response.data;
        if (resp.success) {
          _this.jobitemList_pdm = resp.content.list;
          _this.$refs.pagination_exe_demand_product.render(page, resp.content.total);
        } else {
          Toast.warning(resp.message,Swal);
        }
      }).finally(() => { _this.tableLoading = false; })
    },

    listJobsByPdm_complete(page) {
      let _this = this;
      _this.currentPage = page;
      _this.tableLoading = true;
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/job-items/listJobsByPdm_complete', {
        page: page,
        size: _this.$refs.pagination_complete_demand_product.size,
        customerId: _this.loginCustomer.id, //customerId
        actor: _this.loginUser.customerUserId,
        keyWord: _this.keyWord,
        status: JOB_ITEM_STATUS.COMPLETED.key,
      }).then((response)=>{
        let resp = response.data;
        if (resp.success) {
          _this.completeJobitemList_pdm = resp.content.list;
          _this.$refs.pagination_complete_demand_product.render(page, resp.content.total);
        } else {
          Toast.warning(resp.message,Swal);
        }
      }).finally(() => { _this.tableLoading = false; })
    },


    rowClick(row, column, e){
      let _this = this;
      _this.jobitemListShow(row);
    },

    tabHomeClick() {
      let _this = this;
      _this.activePageId = 'home';
      _this.historyPageId = 'home';
    },

    tabDemandProductClick() {
      let _this = this;
      _this.activePageId = 'demand_product';
      _this.historyPageId = 'demand_product';
      _this.$nextTick(function () {
        _this.$refs.pagination_exe_demand_product.size = 12;
        _this.listJobsByPdm(_this.currentPage);
      });
    },

    tabCompleteClick() {
      let _this = this;
      _this.activePageId = 'complete';
      _this.historyPageId = 'complete';
      _this.$nextTick(function () {
        _this.$refs.pagination_complete.size = 12;
        _this.list_complete(_this.currentPage);
      });
    },

    tabDemandProductCompleteClick() {
      let _this = this;
      _this.activePageId = 'demand_product_complete';
      _this.historyPageId = 'demand_product_complete';
      _this.$nextTick(function () {
        _this.$refs.pagination_complete_demand_product.size = 12;
        _this.listJobsByPdm_complete(_this.currentPage);
      });
    },

    tabJobitemShowClick(index) {
      let _this = this;
      _this.activePageId = `jobitem-show-item-${index}`;
    },

    jobitemListShow(jobitem) {
      openOrFocusTab(this, this.jobitemShowList, jobitem, 'jobitem-show-item');
    },
    closeFunction_jobitem(index) {
      closeDynamicTab(this, {
        list: this.jobitemShowList,
        index: index,
        prefix: 'jobitem-show-item',
        fallback: () => this.historyPageId,
      });
    },

    tableRowClass({row, rowIndex}) {
      // console.log("row:", Date.parse(row.planCompleteTime));
      if (Date.now() > Date.parse(row.planSubmissionTime) && (row.status !== "ARCHIVED" && row.status !== "COMPLETE")) {
        return 'warning-row-3';
      }
      return '';
    }
  }
}
</script>

<style>
.el-table .warning-row-1 {
  background: #FDF5E6FF;
}
.el-table .warning-row-2 {
  background: #fde8c1;
}
.el-table .warning-row-3 {
  background: #fcdba0;
}
.el-table .warning-row-4 {
  background: #facd7c;
}
.el-table .warning-row-5 {
  background: #fac059;
}

</style>
