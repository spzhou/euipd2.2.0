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
                当月绩效
              </a>
            </li>
            <li :class="{active : activePageId==='monthly'}">
              <a @click="tabMonthlyClick">
                往期绩效
              </a>
            </li>
            <li :class="{active : activePageId==='summary'}">
              <a @click="tabSummaryClick">
                执行信息
              </a>
            </li>

            <!--展示个人绩效统计信息-->
            <DynamicTabItem
                v-for="(item, index) in performanceList"
                :key="item.id"
                :index="index"
                :active-page-id="activePageId"
                prefix="worker-performance-item"
                :close="closeFunction_performance"
                @select="tabPerformanceClick">{{ ('绩效统计 : ' + (item.customerUserName)) }}
            </DynamicTabItem>

          
        </template>

                        <DynamicTabPane
                page-id="home"
                :active-page-id="activePageId">
                            <template v-slot:default>
              <p>绩效管理: 绩效管理是对所有参与工作执行的人员的工作绩效的综合统计页面, 绩效信息是工资支付 奖金支付 员工评级的基础..</p>
              <div class="hr hr-18 dotted hr-double"></div>
              <div style="display: flex; justify-content: space-between;">
                <pagination ref="pagination" v-bind:list="list" v-bind:itemCount="6"></pagination>
<!--                <div>
                  <el-button @click="showPerformance">显示统计数据</el-button>
                </div>-->
                <div style="margin-left: 50px; display: flex; align-items: center;">
                  <el-select v-if="departmentOptions.length" v-model="departmentId" size="mini" clearable filterable
                             placeholder="全部部门" style="width: 180px; margin-right: 10px;" @change="list(1)">
                    <el-option v-for="d in departmentOptions" :key="d.id" :label="d.name" :value="d.id"></el-option>
                  </el-select>
                  <el-input
                      v-model="searchName"
                      size="mini"
                      placeholder="页面内容搜索-输入姓名"/>
                </div>
              </div>
              <div>
                <el-table
                    :data="workerPerformanceList.filter(data => !searchName || data.customerUserName.toLowerCase().includes(searchName.toLowerCase()))"
                    border
                    stripe
                    highlight-current-row
                    style="width: 100%">
                  <el-table-column  label="序号" align="center" width="50">
                    <template slot-scope="scope">
                      {{scope.$index+1}}
                    </template>
                  </el-table-column>
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
                      prop="departmentName"
                      min-width="110"
                      show-overflow-tooltip
                      align="center"
                      label="部门"
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
                      label="交付总工时(标准)"
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
                      width="190">
                    <template slot-scope="scope">
                      <el-button plain size="mini" v-on:click.stop="openDetail(scope.row)">
                        计入明细
                      </el-button>
                      <el-button type="primary" plain size="mini" v-on:click.stop="performanceListShow(scope.row)">
                        绩效统计
                      </el-button>
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
            
            
                            </template></DynamicTabPane>

                        <DynamicTabPane
                page-id="summary"
                :active-page-id="activePageId">
                            <template v-slot:default>
              <div>
                <ProjectPerformanceListPageAll v-bind:customer-id="loginCustomer.id"></ProjectPerformanceListPageAll>
              </div>
            
            
                            </template></DynamicTabPane>

                        <DynamicTabPane
                page-id="monthly"
                :active-page-id="activePageId">
                            <template v-slot:default>
              <div>
                <PerformanceMonthlyPage
                    v-bind:customer-id="loginCustomer.id"
                ></PerformanceMonthlyPage>
              </div>
            
            
                            </template></DynamicTabPane>

            <!-- 绩效统计-->
            <DynamicTabPane
                v-for="(item, index) in performanceList"
                :key="('worker-performance-item-' + (item.id))"
                :index="index"
                prefix="worker-performance-item"
                :active-page-id="activePageId">
                <template v-slot:default>
              <PerformanceWorkerList
                  :key="item.customerUserId"
                  :customer-user-id="item.customerUserId"
              ></PerformanceWorkerList>
            
                </template></DynamicTabPane>
          
    </WorkspaceFrame>
    <PerformanceDetailDlg :visible="detailVisible" :customer-user-id="detailUserId"
                          @close="detailVisible = false"></PerformanceDetailDlg>
    <div>
      <el-dialog
          fullscreen
          :visible.sync="issueJobitemDlg"
          :before-close="handleClose"
          :close-on-click-modal=false>
        <div style="margin-top: -10px; margin-left: 30px; position: relative;">
          <div style="position: absolute; right: 45px; top: -62px; font-size: 26px; font-weight: 600; color: #303133;">
            {{ loginCustomer.customerName || loginCustomer.name }}
          </div>
          <div v-if="issueJobitemDlg">
            <ProjectPerformanceListPage
                v-bind:customer-id="loginCustomer.id">
            </ProjectPerformanceListPage>
          </div>
        </div>
        <div style="margin: 0 30px ">
          <div style="display: flex; flex-direction: row; justify-content: space-between; align-items: center;">
            <div style="font-size: 16px;">上述数据由<span style="color: #ae0404; font-weight: 550">EUIPD</span>统计发布，每日更新</div>
          </div>
        </div>
      </el-dialog>
    </div>

  </div>
</template>

<script>
import {closeDynamicTab, openOrFocusTab} from "@/shared/dynamic-tab";
import DynamicTabItem from "@/components/rdms/dynamic-tab-item";
import DynamicTabPane from "@/components/rdms/dynamic-tab-pane";
import WorkspaceFrame from "@/components/rdms/workspace-frame";
import Swal from "sweetalert2";
import Pagination from "@/components/pagination.vue";
import {Loading} from "element-ui";
import PerformanceWorkerList from "@/components/rdms/performance-worker-list.vue";
import {rmbFormat} from "@/filter/filter";
import ProjectPerformanceListPage from "@/components/rdms/project-performance-list-page.vue";
import ProjectPerformanceListPageAll from "@/components/rdms/project-performance-list-page-all.vue";
import PerformanceMonthlyPage from "@/components/rdms/performance-monthly-page.vue";
import PerformanceDetailDlg from "@/components/rdms/performance-detail-dlg.vue";
import { getScope } from "@/shared/statistics-api";

export default {
  components: {
    PerformanceDetailDlg,
    ProjectPerformanceListPage,
    ProjectPerformanceListPageAll,
    Pagination,
    PerformanceWorkerList,
    DynamicTabItem,
    DynamicTabPane,
    WorkspaceFrame,
    PerformanceMonthlyPage,
  },
  name: "performance",
  data: function () {
    return {
      loginUser: {},
      loginCustomer: {},
      performanceList: [],
      workerPerformanceList: [],

      searchName: '',
      activePageId: 'home',
      issueJobitemDlg: false,
      departmentId: '',
      departmentOptions: [],
      detailVisible: false,
      detailUserId: '',
    }
  },
  mounted: function () {
    let _this = this;
    _this.$refs.pagination.size = 6;
    _this.getLoginUser();
    _this.list(1);
    _this.loadDepartments();
  },

  methods: {
    // 部门选项取统计范围；无统计权限时不显示部门筛选
    loadDepartments() {
      getScope().then(scope => {
        const options = ((scope && scope.departments) || []).map(d => ({ id: d.id, name: d.name }));
        if (options.length && scope.level === 'ORG') {
          options.push({ id: 'UNASSIGNED', name: '未分配' });
        }
        this.departmentOptions = options;
      }).catch(() => {
        this.departmentOptions = [];
      });
    },

    openDetail(row) {
      this.detailUserId = row.customerUserId;
      this.detailVisible = true;
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

    showPerformance() {
      let _this = this;
      _this.issueJobitemDlg = true;
    },

    list(page) {
      let _this = this;
      let loadingInstance = Loading.service({ fullscreen: true });
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/performance/list', {
        page: page,
        size: _this.$refs.pagination.size,
        customerId: _this.loginCustomer.id, //customerId
        actor: _this.loginUser.customerUserId,
        departmentId: _this.departmentId || null,
      }).then((response)=>{
        loadingInstance.close();
        let resp = response.data;
        if (resp.success) {
          _this.workerPerformanceList = resp.content.list;
          _this.$refs.pagination.render(page, resp.content.total);
        } else {
          Toast.warning(resp.message,Swal);
        }
      })
    },

    submitHoursFormat(row){
      return row.submitHours.toFixed(2);
    },

    scoreFormat(row){
      return row.score?.toFixed(2);
    },

    performanceFormat(row){
      return rmbFormat(row.performance);
    },

    rowClick(row, column, e){
      let _this = this;
      _this.performanceListShow(row);
    },

    tabHomeClick() {
      let _this = this;
      _this.activePageId = 'home';
    },

    tabSummaryClick() {
      let _this = this;
      _this.activePageId = 'summary';
    },

    tabMonthlyClick() {
      let _this = this;
      _this.activePageId = 'monthly';
    },

    tabPerformanceClick(index) {
      let _this = this;
      _this.activePageId = `worker-performance-item-${index}`;
    },

    performanceListShow(performanceItem) {
      openOrFocusTab(this, this.performanceList, performanceItem, 'worker-performance-item');
    },
    closeFunction_performance(index) {
      closeDynamicTab(this, {
        list: this.performanceList,
        index: index,
        prefix: 'worker-performance-item',
        fallback: 'home',
      });
    },

    handleClose(done) {
      done();
    },

  }
}
</script>

<style scoped>

</style>
