<!--
 * Author: 周朔鹏
 * Date: 2024-12-19
 * Email: 391902958@qq.com
 * Website: https://www.euipd.com
-->

<!-- -->
<template>
  <div class="tab-container">
    <div class="tab-left">
      <div style="height: calc(100vh - 278px); overflow: scroll; overflow-x: hidden">
        <el-table
            ref="completeItemTable"
            :data="projectBudgetMix"
            row-key="id"
            border
            default-expand-all
            stripe
            style="width: 100%;"
            highlight-current-row
            :tree-props="{children: 'children', hasChildren: 'hasChildren'}"
            @row-click="rowClick">
          <el-table-column   label="序号" align="center" width="70">
            <template slot-scope="scope" v-if="scope.row.docType === DOC_TYPE.BUDGET_ADJUST.key">
              {{ scope.row.index }}
            </template>
          </el-table-column>
          <el-table-column
              prop="itemName"
              label="项目/预算名称"
              width="300">
          </el-table-column>
          <el-table-column
              fixed="right"
              label="操作/状态"
              align="center"
              width="102">
            <template slot-scope="scope">
              <div v-if="scope.row.docType === DOC_TYPE.BUDGET.key">
                <div>
                    预算总表
                </div>
              </div>
              <div v-else>
                {{ BUDGET_APPLICANT_STATUS | optionKV(scope.row.status) }}
              </div>
            </template>
          </el-table-column>
          <el-table-column
              fixed="right"
              width="35">
            <template slot-scope="scope">
              <i v-if="currentRow && currentRow.id && scope.row.id === currentRow.id" class="el-icon-caret-right" style="font-size: 24px; color: #4794EF;"></i>
            </template>
          </el-table-column>
        </el-table>
      </div>

    </div>
    <div class="tab-right">
      <div v-if="currentRow && currentRow.docType === DOC_TYPE.BUDGET.key">
        <div>
          <BudgetDirectStatisticsPage
              :key="((currentRow.projectId) + '-project')"
              :x-project-id="currentRow.projectId"
              :project-type="currentRow.projectType"
          ></BudgetDirectStatisticsPage>
        </div>
      </div>
      <div v-if="currentRow && currentRow.docType === DOC_TYPE.BUDGET_ADJUST.key">
        <BudgetApprovePage
            :key="currentRow.id"
            :budget-adjust-id="currentRow.id"
            :index="index"
            :is-complete="true"
            :entrance="entrance"
        ></BudgetApprovePage>
      </div>
    </div>
  </div>
</template>

<script>
import Swal from "sweetalert2";
import BudgetApprovePage from "@/components/std/budget-approve-page.vue";
import BudgetDirectStatisticsPage from "@/components/std/budget-direct-statistics-page.vue";

export default {
  components: {
    BudgetDirectStatisticsPage,
    BudgetApprovePage,
  },
  name: "budget-application-complete-tab-content",

  props: {
    budgetId: {
      default: null,
    },
    budgetType: {
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

      projectBudgetMix: [],

      DOC_TYPE: DOC_TYPE,
      BUDGET_APPLICANT_STATUS: BUDGET_APPLICANT_STATUS,
      PROJECT_TYPE: PROJECT_TYPE,

      currentRow: {},

      sumJobNum: 0,
      completeJobNum: 0,
      completeFlag: false,
    }
  },

  computed: {},

  mounted: function () {
    let _this = this;
    _this.getLoginUser();
    if(Tool.isNotEmpty(_this.budgetId) ){
      _this.getProjectAndApplicationBudgetItems(_this.budgetId);
    }

    _this.$bus.$on('refresh-budget-application-examine', (data) => {
      if(data){
        _this.getProjectAndApplicationBudgetItems(_this.budgetId);
      }
    });
  },

  beforeDestroy(){
    let _this = this;
    _this.$bus.$off('refresh-budget-application-examine');
  },

  methods: {
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

    rowClick(row, column, e){
      let _this = this;
      _this.currentRow = row;
    },

    getProjectAndApplicationBudgetItems(budgetId) {
      let _this = this;
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/budget-mng/getProjectAndApplicationCompleteBudgetItems/' + budgetId).then((response)=>{
        let resp = response.data;
        if (resp.success) {
          let respItemMix = resp.content;
          let respItems = [];
          respItems.push(respItemMix);
          _this.projectBudgetMix =respItems;
          if(Tool.isNotEmpty_ES5(_this.projectBudgetMix)){
            if(Tool.isNotEmpty(_this.projectBudgetMix[0].children)){
              if(_this.projectBudgetMix[0].children.length > 0){
                _this.currentRow = _this.projectBudgetMix[0].children[0];
              }else{
                _this.currentRow = _this.projectBudgetMix[0];
              }
            }else{
              _this.currentRow = _this.projectBudgetMix[0];
            }
          }else{
            _this.currentRow = _this.projectBudgetMix[0];
          }
          _this.$refs.completeItemTable.setCurrentRow(_this.currentRow);

          _this.getCompletedJobitemList(budgetId);
          _this.$bus.$emit('refresh-budget-application-examine', false);
        } else {
          Toast.warning(resp.message,Swal);
        }
      })
    },

    getCompletedJobitemList(budgetId) {
      let _this = this;
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/job-items/getCompletedJobitemList/' + budgetId ).then((response) => {
        let resp = response.data;
        if (resp.success) {
          _this.sumJobNum = resp.content.sumJobNum;
          _this.completeJobNum = resp.content.completeJobNum;
          _this.completeFlag = resp.content.completeFlag;

        } else {
          Toast.warning(resp.message, Swal);
        }
      })
    },

  },
}
</script>

<style lang="scss" scoped>
@import './../../../public/static/scss/caipd.scss';

</style>
