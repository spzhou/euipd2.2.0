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
                预算列表
              </a>
            </li>

            <!--研发管理-->
            <DynamicTabItem
                v-for="(item, index) in budgetItemList"
                :key="item.id"
                :index="index"
                :active-page-id="activePageId"
                prefix="budget-manage-item"
                :close="closeFunction_budget"
                @select="tabBudgetItemClick">{{ ('预算管理 : ' + (item.name)) }}
            </DynamicTabItem>
          
        </template>

                        <DynamicTabPane
                page-id="home"
                :active-page-id="activePageId">
                            <template v-slot:default>
              <p>预算管理: 对各预算口径进行管理. 预算口径包括预立项,研发项目以及产品管理项目等</p>
              <div class="hr hr-18 dotted hr-double"></div>
              <div>
                <div style="display: flex; justify-content: space-between;">
                  <pagination ref="pagination" v-bind:list="list" v-bind:itemCount="6"></pagination>
                  <div>
                  <!--  这里可以放置按钮-->
                    <div style="display: flex; flex-direction: row; justify-content: right; align-items: center;">
                      <div style="margin-right: 10px;">
                        <el-button type="primary" size="mini" @click.stop="showStageStatisticsInfo">系统工程阶段预算汇总</el-button>
                      </div>
                      <div style="margin-right: 10px;">
                        <el-button type="primary" size="mini" @click.stop="showStageStatisticsInfo_pd">产品PLM阶段预算汇总</el-button>
                      </div>
                      <div style="margin-right: 10px;">
                        <el-button type="primary" size="mini" @click.stop="showStageStatisticsInfo_pj">研发开发阶段预算汇总</el-button>
                      </div>
                      <div style="margin-right: 10px;">
                        <el-button type="primary" size="mini" @click.stop="showStageStatisticsInfo_customer">公司研发预算汇总总表</el-button>
                      </div>
                    </div>
                  </div>
                </div>
                <el-table
                    :data="budgetItems"
                    row-key="id"
                    border
                    default-expand-all
                    :row-class-name="tableRowClass"
                    highlight-current-row
                    style="width: 100%"
                    @row-click="rowClick">
                  <el-table-column  label="序号" align="center" width="90">
                    <template slot-scope="scope">
                      {{scope.$index + 1 }}
                    </template>
                  </el-table-column>
                  <el-table-column
                      prop="projectCode"
                      align="center"
                      label="项目编号"
                      min-width="150"
                      show-overflow-tooltip
                  ></el-table-column>
                  <el-table-column
                      prop="name"
                      align="center"
                      label="项目名称"
                      min-width="250"
                      show-overflow-tooltip
                  ></el-table-column>
                  <el-table-column
                      prop="type"
                      :formatter="budgetTypeFormat"
                      label="预算类型"
                      align="center"
                      min-width="90"
                  ></el-table-column>
                  <el-table-column
                      prop="applicantName"
                      label="责任人"
                      align="center"
                      min-width="90"
                  ></el-table-column>
                  <el-table-column
                      prop="approverName"
                      label="审批人"
                      align="center"
                      min-width="90"
                  ></el-table-column>
                  <el-table-column
                      prop="budget"
                      :formatter= "budgetFormat"
                      label="预算金额"
                      align="center"
                      min-width="100"
                  ></el-table-column>
                  <el-table-column
                      prop="status"
                      :formatter="budgetStatusFormat"
                      label="执行状态"
                      align="center"
                      min-width="90"
                  ></el-table-column>
                  <el-table-column
                      prop="createTime"
                      :formatter="shortTimeFormat"
                      label="创建时间"
                      align="center"
                      min-width="100"
                  ></el-table-column>
                  <el-table-column
                      fixed="right"
                      label="操作"
                      align="center"
                      :width="getOpeAreaWidth">
                    <template slot-scope="scope">
                      <div>
                        <el-tooltip content="对相应条目的预算申请进行审批或者对预算进行调整等操作" placement="top" effect="light" :open-delay=1000>
                          <el-button type="primary" plain size="small" v-on:click.stop="budgetManager(scope.row)">
                            <el-badge :is-dot="scope.row.hasSubmitBudgetAdjust" >
                              预算管理
                            </el-badge>
                          </el-button>
                        </el-tooltip>
                      </div>
                    </template>
                  </el-table-column>
                </el-table>
              </div>
            
            
                            </template></DynamicTabPane>

            <!-- 研发管理-->
            <DynamicTabPane
                v-for="(item, index) in budgetItemList"
                :key="('budget-manage-item-' + (item.id))"
                :index="index"
                prefix="budget-manage-item"
                :active-page-id="activePageId">
                <template v-slot:default>
              <BudgetManageTab
                  v-if="activePageId === ('budget-manage-item-' + (index))"
                  :key="item.projectId"
                  :budget-id="item.id"
                  :budget-type="item.type"
                  :project-type="item.type"
                  :index="index"
                  :entrance="ENTRANCE_CLASS.BUDGET.key"
              ></BudgetManageTab>
            
                </template></DynamicTabPane>

          
    </WorkspaceFrame>

    <!--系统工程预算汇总-->
    <el-dialog
        width="40cm"
        :visible.sync="showStageStatisticsDlg"
        :before-close="handleClose"
        :close-on-click-modal=false>
      <div slot="title" style="font-size: 20px;">{{ dialogTitle }}</div>
      <div v-if="showStageStatisticsDlg">
        <BudgetStageStatisticsPage
            :key="loginCustomer.id"
            :customer-id="loginCustomer.id"
            :stage="BUDGET_TYPE.PRE_PROJECT.key"
        ></BudgetStageStatisticsPage>
      </div>
    </el-dialog>

    <!--产品PLM阶段预算汇总-->
    <el-dialog
        width="40cm"
        :visible.sync="showStageStatisticsDlg_pd"
        :before-close="handleClose"
        :close-on-click-modal=false>
      <div slot="title" style="font-size: 20px;">{{ dialogTitle }}</div>
      <div v-if="showStageStatisticsDlg_pd">
        <BudgetStageStatisticsPage
            :key="loginCustomer.id"
            :customer-id="loginCustomer.id"
            :stage="BUDGET_TYPE.PRODUCT.key"
        ></BudgetStageStatisticsPage>
      </div>
    </el-dialog>

    <!--产品开发阶段预算汇总-->
    <el-dialog
        width="40cm"
        :visible.sync="showStageStatisticsDlg_pj"
        :before-close="handleClose"
        :close-on-click-modal=false>
      <div slot="title" style="font-size: 20px;">{{ dialogTitle }}</div>
      <div v-if="showStageStatisticsDlg_pj">
        <BudgetStageStatisticsPage
            :key="loginCustomer.id"
            :customer-id="loginCustomer.id"
            :stage="BUDGET_TYPE.PROJECT.key"
        ></BudgetStageStatisticsPage>
      </div>
    </el-dialog>

    <!--公司研发总预算汇总-->
    <el-dialog
        width="40cm"
        :visible.sync="showStageStatisticsDlg_customer"
        :before-close="handleClose"
        :close-on-click-modal=false>
      <div slot="title" style="font-size: 20px;">{{ dialogTitle }}</div>
      <div v-if="showStageStatisticsDlg_customer">
        <BudgetStageStatisticsPage
            :key="loginCustomer.id"
            :customer-id="loginCustomer.id"
            :stage="'CUSTOMER'"
        ></BudgetStageStatisticsPage>
      </div>
    </el-dialog>

  </div>
</template>

<script>
import Pagination from "@/components/pagination";
import {closeDynamicTab, openOrFocusTab} from "@/shared/dynamic-tab";
import DynamicTabItem from "@/components/rdms/dynamic-tab-item";
import DynamicTabPane from "@/components/rdms/dynamic-tab-pane";
import WorkspaceFrame from "@/components/rdms/workspace-frame";
import Swal from "sweetalert2";
import {optionKV, rmbFormat, shortDateFormat} from "@/filter/filter";
import BudgetManageTab from "@/components/rdms/budget-manage-tab.vue";
import BudgetStageStatisticsPage from "@/components/std/budget-stage-statistics-page.vue";

export default {
  components: {
    BudgetStageStatisticsPage,
    BudgetManageTab,
    Pagination,
    DynamicTabItem,
    DynamicTabPane,
    WorkspaceFrame,
  },
  name: "budget",
  data: function () {
    return {
      loginUser: {},
      loginCustomer: {},

      activePageId: 'home',

      budgetItemList: [],
      budgetItems: [],
      pageSize: 24,

      ENTRANCE_CLASS: ENTRANCE_CLASS,
      BUDGET_TYPE: BUDGET_TYPE,
      PROJECT_TYPE: PROJECT_TYPE,
      BUDGET_STATUS: BUDGET_STATUS,
      showStageStatisticsDlg: false,
      showStageStatisticsDlg_pd: false,
      showStageStatisticsDlg_pj: false,
      showStageStatisticsDlg_customer: false,

      dialogTitle: 'Dialog',
    }
  },
  mounted: function () {
    let _this = this;
    _this.getLoginUser();
    _this.$refs.pagination.size = _this.pageSize;
    _this.list(1);

    _this.$bus.$on('close-develop-tab', (index) => {
      _this.closeFunction_budget(index);
    });

    _this.$bus.$on('refresh-develop-character-list', (data) => {
      if(data){
        _this.list(1);
      }
    });
  },

  beforeDestroy: function () {
    let _this = this;
    _this.$bus.$off('close-develop-tab');
    _this.$bus.$off('refresh-develop-character-list');
  },

  computed: {
    getOpeAreaWidth(){
      return 150;
    }
  },


  methods: {
    getLoginUser(){
      let _this = this;
      _this.loginUser = Tool.getActorUser();
      _this.loginCustomer = Tool.getLoginCustomer();
    },

    showStageStatisticsInfo() {
      let _this = this;
      _this.showStageStatisticsDlg = true;
      _this.dialogTitle = "系统工程阶段预算汇总表";
    },
    showStageStatisticsInfo_pd() {
      let _this = this;
      _this.showStageStatisticsDlg_pd = true;
      _this.dialogTitle = "产品PLM阶段预算汇总表";
    },
    showStageStatisticsInfo_pj() {
      let _this = this;
      _this.showStageStatisticsDlg_pj = true;
      _this.dialogTitle = "产品开发阶段预算汇总表";
    },
    showStageStatisticsInfo_customer() {
      let _this = this;
      _this.showStageStatisticsDlg_customer = true;
      _this.dialogTitle = "公司研发总预算汇总表";
    },
    closeDialog() {
      let _this = this;
      _this.showStageStatisticsDlg = false;
      _this.showStageStatisticsDlg_pd = false;
      _this.showStageStatisticsDlg_pj = false;
      _this.showStageStatisticsDlg_customer = false;
    },
    shortTimeFormat(row, column){
      return shortDateFormat(row.planCompleteTime);
    },

    budgetFormat(row, column){
      if(row.rdType !== RD_TYPE.RESEARCH.key){
        return rmbFormat(row.budget);
      }else{
        return '-';
      }

    },

    docTypeFormat(row, column) {
      return optionKV(DOC_TYPE, row.docType);
    },

    budgetTypeFormat(row, column) {
      return optionKV(BUDGET_TYPE, row.type);
    },
    budgetStatusFormat(row, column) {
      return optionKV(BUDGET_STATUS, row.status);
    },
    tabBudgetItemClick(i) {
      let _this = this;
      _this.activePageId = `budget-manage-item-${i}`;
    },

    tabHomeClick() {
      let _this = this;
      _this.activePageId = 'home';
      _this.refreshHomeList();
    },

    budgetManager(budgetItem) {
      openOrFocusTab(this, this.budgetItemList, budgetItem, 'budget-manage-item');
    },
    closeFunction_budget(index) {
      closeDynamicTab(this, {
        list: this.budgetItemList,
        index: index,
        prefix: 'budget-manage-item',
        fallback: 'home',
        afterClose: () => {
          if (this.activePageId === 'home') {
            this.refreshHomeList();
          }
        },
      });
    },

    refreshHomeList() {
      this.$nextTick(() => {
        if (this.$refs.pagination) {
          this.$refs.pagination.size = this.pageSize;
        }
        this.list(1);
      });
    },

    rowClick(row, column, e){
      let _this = this;
      //TODO: 使用对话框显示相应项目的详细信息
    },

    handleClose(done) {
      done(); //el-dialog用于关闭对话框的内置函数.
      this.list(1);
    },

    list(page) {
      let _this = this;
      if (!_this.loginUser || !_this.loginUser.customerUserId || !_this.loginCustomer || !_this.loginCustomer.id) {
        console.warn('[Budget] 缺少 loginUser/customerUserId，请重新登录');
        return;
      }
      if (_this.$refs.pagination && _this.$refs.pagination.size) {
        _this.pageSize = _this.$refs.pagination.size;
      }
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/budget-mng/list', {
        page: page,
        size: _this.pageSize,
        customerId: _this.loginCustomer.id, //customerId
        loginUserId: _this.loginUser.customerUserId,
      }).then((response)=>{
        let resp = response.data;
        if (resp.success) {
          _this.budgetItems = resp.content.list;
          _this.$refs.pagination?.render(page, resp.content.total);
        } else {
          Toast.warning(resp.message,Swal);
        }
      })
    },

    tableRowClass({row, rowIndex}) {
      // console.log("row:", Date.parse(row.planCompleteTime));
      if (Date.now() > Date.parse(row.planCompleteTime)) {
        return 'warning-row-3';
      }
      return '';
    }
  }
}
</script>

<style lang="scss">
@import './../../../../public/static/scss/caipd.scss';

.el-badge__content.is-dot {
  height: 10px;
  width: 10px;
  padding: 0;
  right: -2px !important;
  border-radius: 50%;
}
</style>
