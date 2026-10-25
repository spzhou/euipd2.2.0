<!--
 * Author: 周朔鹏
 * Date: 2025-02-25
 * Email: 391902958@qq.com
 * Website: https://www.euipd.com
-->

<template>
  <div>
    <div style="position: relative;">
      <el-badge v-show="examineNum>0" :value="examineNum" class="item" style="position: absolute; left: 172px; top: 3px; z-index: 100;"></el-badge>
    </div>
    <el-tabs v-model="activeName" type="card" @tab-click="handleClick">

      <!--     系统任务 -->
      <el-tab-pane label="系统任务" name="develop">
        <div v-if="activeName === 'develop'">
          <PreprojectTaskIssue
              ref="taskJoblist"
              :key="('issue-' + (preProjectId))"
              :pre-project-id="preProjectId"
              :job-type="jobType"
              :project-type="projectType"
              :index="index"
              :entrance="entrance"
          ></PreprojectTaskIssue>
        </div>
      </el-tab-pane>

      <!--     工作审核 -->
      <el-tab-pane label="任务审核" name="evaluate">
        <div v-if="activeName === 'evaluate'">
          <PreprojectTaskExamine
              ref="evaluateJobitemList"
              :key="('evaluate-' + (preProjectId))"
              :index="index"
              :pre-project-id="preProjectId"
              :job-type="jobType"
              :project-type="projectType"
              :entrance="entrance"
          ></PreprojectTaskExamine>
        </div>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script>
import ProjectTaskSubmitContainer from "@/components/rdms/project-task-submit-container.vue";
import ReviewSubprojectContainer from "@/components/rdms/review-subproject-container.vue";

import PreprojectTaskIssue from "@/components/rdms/preproject-task-issue.vue";
import PreprojectTaskExamine from "@/components/rdms/preproject-task-examine.vue";

import Swal from "sweetalert2";

export default {
  components: {
    ReviewSubprojectContainer,
    PreprojectTaskIssue,
    ProjectTaskSubmitContainer,
    PreprojectTaskExamine,
  },
  name: "preproject-task-tab",

  props: {
    preProjectId: {
      default: null,
    },
    jobType: {
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

      JOB_ITEM_TYPE: JOB_ITEM_TYPE,
      PROJECT_STATUS: PROJECT_STATUS,

      activeName: 'develop',
      examineNum: 0,
      preProject: {},

    }
  },

  computed: {},

  mounted: function () {
    let _this = this;
    _this.getLoginUser();

    if(Tool.isNotEmpty(_this.preProjectId)){
      _this.getPreProjectInfo(_this.preProjectId);
    }

    _this.$bus.$on('refresh-preproject-task-tab-num', (data) => {
      if(data){
        _this.getExamineNum(_this.loginUser.customerUserId);
      }
    });
  },

  beforeDestroy(){
    let _this = this;
    _this.$bus.$off('refresh-preproject-task-tab-num');
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

      _this.getExamineNum(_this.loginUser.customerUserId);
    },

    handleClick(tab, event) {
      let _this = this;
      if(tab.name === 'develop'){
        // _this.$refs.taskJoblist.getSubProjectAndTaskJobitems(_this.preProjectId);
      }else if(tab.name === 'evaluate'){
        // _this.$refs.evaluateJobitemList.getProjectAndTaskJobitems_submit(_this.preProjectId);
      }else if(tab.name === 'task-submit'){
        // _this.$refs.taskSubmitContainer.getJobitemDetailInfo();
      }
    },

    getPreProjectInfo(preProjectId){
      let _this = this;
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/pre-project/getPreProjectSimpleInfo/' + preProjectId).then((response) => {
        let resp = response.data;
        if (resp.success) {
          _this.preProject = resp.content;
        } else {
          Toast.warning(resp.message, Swal);
        }
      })
    },

    getExamineNum(sysgmId){
      let _this = this;
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/job-items/getSubmitPreTaskJobitemNumBySysgmAndPreprojectId', {
        preProjectId: _this.preProjectId,
        sysgmId: sysgmId,
      }).then((response)=>{
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
