<!--
 * Author: 周朔鹏
 * Date: 2025-02-06
 * Email: 391902958@qq.com
 * Website: https://www.euipd.com
-->

<template>
  <div>
    <div style="display: flex; flex-direction: row; justify-content: left; align-items: center">
      <div style="font-size: 16px; font-weight: 500; margin-left: 5px;">
        {{ "团队成员工作压力图" }}
      </div>
      <div style="margin-left: 50px;">
        <el-input
            v-model="searchName"
            size="mini"
            placeholder="输入员工姓名搜索"/>
      </div>
    </div>
    <div class="hr hr-10 hr-dotted"></div>
    <!--团队成员工作压力图-->
    <div style="width: 100%; display: flex; flex-direction: row; justify-content: space-evenly; align-items: center; flex-wrap: wrap;">
      <div v-for="(item, i) in workersByFilter">
        <div v-show="item.id">
          <MyChartSmall
              :key="item.id"
              :chart-id="((item.id))"
              :executor-id="item.id"
          ></MyChartSmall>
        </div>
      </div>
    </div>

    <div class="hr hr-16 hr-dotted hr-double"></div>
    <div style="display: flex; flex-direction: row; justify-content: left; align-items: center">
      <div style="font-size: 16px; font-weight: 500; margin-left: 5px;">
        {{ "团队成员当月工作绩效" }}
      </div>
      <div style="margin-left: 50px;">
        <el-input
            v-model="search"
            size="mini"
            placeholder="输入员工姓名搜索"/>
      </div>
    </div>

    <div class="hr hr-10 hr-dotted"></div>
    <!--团队成员工作压力图-->
    <div>
      <el-table
          :data="workerPerformanceList.filter(data => !search || data.customerUserName.toLowerCase().includes(search.toLowerCase()))"
          border
          stripe
          highlight-current-row
          @row-click="rowClick"
          style="width: 100%">
        <el-table-column  label="序号" align="center" width="50">
          <template slot-scope="scope">
            {{scope.$index+1}}
          </template>
        </el-table-column>
        <el-table-column
            prop="customerUserId"
            width="100"
            align="center"
            label="员工ID"
        ></el-table-column>
        <el-table-column
            prop="jobNum"
            min-width="100"
            show-overflow-tooltip
            align="center"
            label="工号"
        ></el-table-column>
        <el-table-column
            prop="customerUserName"
            min-width="100"
            show-overflow-tooltip
            align="center"
            label="姓名"
        ></el-table-column>
        <el-table-column
            prop="yearMonth"
            min-width="100"
            show-overflow-tooltip
            align="center"
            label="绩效周期"
        ></el-table-column>
        <el-table-column
            prop="submitHours"
            :formatter="submitHoursFormat"
            min-width="100"
            show-overflow-tooltip
            align="center"
            label="总工时"
        ></el-table-column>
        <el-table-column
            prop="delayRate"
            align="center"
            label="延期率"
            min-width="100"
            show-overflow-tooltip
        ></el-table-column>
        <el-table-column
            prop="rebateRate"
            align="center"
            label="延期绩效折扣"
            min-width="100"
            show-overflow-tooltip
        ></el-table-column>
        <el-table-column
            prop="performance"
            :formatter="performanceFormat"
            align="center"
            label="绩效工时费"
            min-width="100"
            show-overflow-tooltip
        ></el-table-column>
        <el-table-column
            prop="loadRate"
            align="center"
            label="负荷率"
            min-width="100"
            show-overflow-tooltip
        ></el-table-column>
        <el-table-column
            prop="score"
            :formatter="scoreFormat"
            align="center"
            label="绩效评分"
            min-width="100"
            show-overflow-tooltip
        ></el-table-column>
        <el-table-column
            fixed="right"
            label="操作"
            align="center"
            width="220">
          <template slot-scope="scope">
            <div style="display: flex; flex-direction: row; justify-content: center; align-items: center;">
              <div style="margin: 0 5px;">
                <el-button type="primary" plain size="small" v-on:click="taskClick(scope.row)">
                  任务清单
                </el-button>
              </div>
              <div v-if="isUnderLine(scope.row.customerUserId)" style="margin: 0 5px;">
                <el-button type="primary" plain size="small" v-on:click.stop="showPerformance(scope.row)">
                  绩效统计
                </el-button>
              </div>
            </div>
          </template>
        </el-table-column>
      </el-table>
      <div>
        <small><em>
          <i class="el-icon-info"></i> 备注1 : 工单结算绩效工时 = 工单绩效工时 * ( 1 - 延期率 * 延期折扣 ) * ( 绩效基础 + (1 - 绩效基础)) * ( 任务单评分 / 10));   延期折扣 = 0.3 ; 绩效基础 = 0.5
        </em></small>
      </div>
      <div>
        <small><em>
          <i class="el-icon-info"></i> 备注2 : 绩效工时费 = 求和(工单结算绩效工时 * 标准工时费率)
        </em></small>
      </div>
      <div>
        <small><em>
          <i class="el-icon-info"></i> 备注3 : 绩效评分 = 求和(工单结算绩效工时) / 总计划工时 * 100
        </em></small>
      </div>
      <div>
        <small><em>
          <i class="el-icon-info"></i> 备注4 : 负荷率 = 总工时 / 当月法定工时
        </em></small>
      </div>
    </div>

    <!--  显示工作人员的所有在执行工单  -->
    <el-dialog
        width="40cm"
        :visible.sync="showJobitemListDlg"
        :before-close="handleClose"
        :close-on-click-modal=false>
      <div slot="title" style="font-size: 20px;">{{ dialogTitle }}</div>
      <div>
        <JobitemListWorkerDlg
            :key="worker.customerUserId"
            :worker-id="worker.customerUserId"
            :close-dlg="closeDialog"
        ></JobitemListWorkerDlg>
      </div>
    </el-dialog>

    <!--  显示工作人员的所有在执行工单  -->
    <el-dialog
        width="40cm"
        :visible.sync="performanceListDlg"
        :before-close="handleClose"
        :close-on-click-modal=false>
      <div slot="title" style="font-size: 20px;">{{ dialogTitle }}</div>
      <div style="margin: 0 10px; padding-bottom: 20px;">
        <PerformanceWorkerList
            :key="worker.customerUserId"
            :customer-user-id="worker.customerUserId"
            :close-dlg="closeDialog"
        ></PerformanceWorkerList>
      </div>
    </el-dialog>


  </div>
</template>

<script>
import Swal from "sweetalert2";
import {fixed2fractional, rmbFormat} from "@/filter/filter";
import MyChartSmall from "@/components/util/myChart-small.vue";
import {Loading} from "element-ui";
// import Pagination from "@/components/pagination.vue";
import JobitemListWorkerDlg from "@/components/std/jobitem-list-worker-dlg.vue";
import PerformanceWorkerList from "@/components/rdms/performance-worker-list.vue";

export default {
  components: {
    JobitemListWorkerDlg,
    PerformanceWorkerList,
    // Pagination,
    MyChartSmall,
  },
  name: "task-performance",

  data: function() {
    return {
      loginUser: {},
      loginCustomer: {},
      worker: {},
      search: '',
      searchName: '',

      workerPerformanceList: [],
      workers: [],
      performanceList: [],
      underlineIdList: [],

      dialogTitle: '',
      showJobitemListDlg: false,
      performanceListDlg: false,
    }
  },

  mounted: function() {
    let _this = this;
    // _this.$refs.pagination.size = 12;
    _this.getLoginUser();
  },

  computed: {
    workersByFilter(){
      return this.workers.filter(data => !this.searchName || data.trueName.toLowerCase().includes(this.searchName.toLowerCase()))
    },
  },

  methods: {
    getLoginUser(){
      let _this = this;
      const sessionUser = Tool.getLoginUser() || {};
      if (sessionUser.userGroup !== LOGIN_USER_GROUP.CUSTOMER.key) {
        _this.role_ADMIN = false;
      }
      _this.loginUser = Tool.getActorUser();
      _this.loginCustomer = Tool.getLoginCustomer();

      if (!_this.loginUser || !_this.loginUser.customerUserId) {
        console.warn('[TaskPerformance] 缺少 customerUserId，请重新登录');
        return;
      }
      _this.listWorkers();
      _this.listPerformance();
      _this.getUnderline(_this.loginUser.customerUserId);
    },

    getPerformanceListByUserId(customerUserId) {
      let _this = this;
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/performance/getPerformanceListByUserId/' + customerUserId).then((response) => {
        let resp = response.data;
        if (resp.success) {
          _this.performanceList = resp.content;
        } else {
          Toast.warning(resp.message, Swal);
        }
      })
    },

    scoreFormat(row){
      return row.score?.toFixed(2);
    },

    performanceFormat(row){
      return rmbFormat(row.performance);
    },
    submitHoursFormat(row){
      return row.submitHours.toFixed(2);
    },

    listWorkers() {
      let _this = this;
      let loadingInstance = Loading.service({ fullscreen: true });
      let formData = new window.FormData();
      formData.append('customerId', _this.loginCustomer.id);
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/worker/listWorkers', formData).then((response)=>{
        loadingInstance.close();
        let resp = response.data;
        if (resp.success) {
          _this.workers = resp.content;
        } else {
          Toast.warning(resp.message,Swal);
        }
      })
    },

    listPerformance() {
      let _this = this;
      let loadingInstance = Loading.service({ fullscreen: true });
      let formData = new window.FormData();
      formData.append('customerId', _this.loginCustomer.id);
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/performance/listPerformance', formData).then((response)=>{
        loadingInstance.close();
        let resp = response.data;
        if (resp.success) {
          _this.workerPerformanceList = resp.content;
        } else {
          Toast.warning(resp.message,Swal);
        }
      })
    },

    getUnderline(workerId) {
      let _this = this;
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/underline/getUnderline/'+ workerId).then((response)=>{
        let resp = response.data;
        if (resp.success) {
          if(resp.content === null){
            _this.value = [];
          }else{
            let underlines = resp.content;
            if(underlines){
              underlines.forEach(item => {
                _this.underlineIdList.push(item.id);
              })
            }
          }
        } else {
          Toast.warning(resp.message,Swal);
        }
      })
    },

    isUnderLine(itemId) {
      let _this = this;
      if(_this.underlineIdList.includes(itemId)){
        return true;
      }else{
        return false;
      }
    },

    taskClick(row){
      let _this = this;
      _this.rowClick(row);
    },

    rowClick(row, column, e){
      let _this = this;
      _this.worker = row;
      _this.showJobitemListDlg = true;
      _this.dialogTitle = "任务清单";
    },

    showPerformance(row, column, e){
      let _this = this;
      _this.worker = row;
      _this.performanceListDlg = true;
      _this.dialogTitle = "绩效清单";
    },

    handleClose(done) {
      done(); //el-dialog用于关闭对话框的内置函数.
    },

    closeDialog() {
      let _this = this;
      _this.showJobitemListDlg = false;
      _this.performanceListDlg = false;
    },

  }
}
</script>

<style scoped>
.comment-text{
  margin-right: 20px;
}
</style>
