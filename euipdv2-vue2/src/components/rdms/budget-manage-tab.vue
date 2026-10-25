<!--
 * Author: 周朔鹏
 * Date: 2025-02-25
 * Email: 391902958@qq.com
 * Website: https://www.euipd.com
-->

<template>
  <div>
    <div style="position: relative;">
      <el-badge v-show="examineNum>0" :value="examineNum" class="item" style="position: absolute; left: 75px; top: 3px; z-index: 100;"></el-badge>
    </div>
    <el-tabs
        v-model="activeName"
        type="card"
        @tab-click="handleClick">
      <el-tab-pane label="预算审批" name="examine">
        <div v-if="activeName === 'examine'">
          <BudgetApplicationTabContent
              :key="budgetId"
              ref="budgetApplicationList"
              :budget-id="budgetId"
              :budget-type="budgetType"
              :project-type="projectType"
              :index="index"
              :entrance="entrance"
          ></BudgetApplicationTabContent>
        </div>
      </el-tab-pane>

      <el-tab-pane label="已批准预算" name="approved">
        <div v-if="activeName === 'approved'">
          <BudgetApplicationCompleteTabContent
              :key="budgetId"
              ref="budgetApplicationList"
              :budget-id="budgetId"
              :budget-type="budgetType"
              :project-type="projectType"
              :index="index"
              :entrance="entrance"
          ></BudgetApplicationCompleteTabContent>
        </div>
      </el-tab-pane>

    </el-tabs>
  </div>
</template>

<script>
import Swal from "sweetalert2";

import BudgetApplicationTabContent from "@/components/rdms/budget-application-tab-content.vue";
import BudgetApplicationCompleteTabContent from "@/components/rdms/budget-application-complete-tab-content.vue";

export default {
  components: {
    BudgetApplicationTabContent,
    BudgetApplicationCompleteTabContent,
  },
  name: "budget-manage-tab",

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

      budgetInfo: {},

      activeName: 'examine',
      evaluateNum: 0,
      examineNum: 0,
    }
  },

  computed: {},

  mounted: function () {
    let _this = this;
    _this.getLoginUser();

    if(Tool.isNotEmpty(_this.budgetId)){
      _this.getBudgetSimpleInfo(_this.budgetId);
    }

    _this.$bus.$on('refresh-budget-manage-tab-num', (data) => {
      if(data){
        _this.getBudgetSimpleInfo(_this.budgetId);
      }
    });

  },

  beforeDestroy(){
    let _this = this;
    _this.$bus.$off('refresh-budget-manage-tab-num');
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

    handleClick(tab, event) {
      let _this = this;
      if(tab.name === 'develop'){
        // _this.$refs.budgetApplicationList.getCharacterAndJobitemList(_this.projectId);
      }else if(tab.name === 'examine'){
        // _this.$refs.examineJobitemList.getCharacterAndSubmitJobitems(_this.projectId);
      }else if(tab.name === 'approved'){
        // _this.$refs.taskSubmitContainer.getJobitemDetailInfo();
      }
    },

    getBudgetSimpleInfo(budgetId){
      let _this = this;
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/budget-mng/getBudgetSimpleInfo/' + budgetId).then((response) => {
        let resp = response.data;
        if (resp.success) {
          _this.budgetInfo = resp.content;
          _this.getExamineNum(_this.budgetInfo.projectId);
        } else {
          Toast.warning(resp.message, Swal);
        }
      })
    },

    getExamineNum(projectId){
      let _this = this;
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/budget-mng/getExamineNum/' + projectId).then((response)=>{
        let resp = response.data;
        if (resp.success) {
          _this.examineNum = resp.content;
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
