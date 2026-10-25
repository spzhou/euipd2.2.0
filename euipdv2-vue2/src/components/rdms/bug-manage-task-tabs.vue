<!--
 * Author: 周朔鹏
 * Date: 2025-02-27
 * Email: 391902958@qq.com
 * Website: https://www.euipd.com
-->

<template>
  <div>
    <div style="position: relative;">
      <el-badge v-show="examineNum>0" :value="examineNum" class="item" style="position: absolute; left: 172px; top: 3px; z-index: 100;"></el-badge>
    </div>
    <el-tabs v-model="activeName" type="card">

      <!--     质量任务 -->
      <el-tab-pane label="缺陷处理" name="develop">
        <div v-if="activeName === 'develop'">
          <BugTaskJobitemIssue
              :key="bugfeedbackId"
              :bugfeedback-id="bugfeedbackId"
              :job-type="jobType"
              :project-type="projectType"
              :index="index"
              :entrance="entrance"
          ></BugTaskJobitemIssue>
        </div>
      </el-tab-pane>

      <!--     任务审核 -->
      <el-tab-pane label="任务审核" name="examine">
        <div v-if="activeName === 'examine'">
          <BugJobitemExamine
              ref="examineJobitemList"
              :index="index"
              :key="bugfeedbackId"
              :bugfeedback-id="bugfeedbackId"
              :job-type="jobType"
              :project-type="projectType"
              :entrance="entrance"
          ></BugJobitemExamine>
        </div>
      </el-tab-pane>

      <el-tab-pane label="结案反馈" name="review-submit">
        <div v-if="activeName === 'review-submit'">
          <BugFeedbackContainer
              :key="bugfeedbackId"
              :bugfeedback-id="bugfeedbackId"
              :job-type="jobType"
              :project-type="projectType"
              :index="index"
              :entrance="entrance"
          ></BugFeedbackContainer>
        </div>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script>
import ProjectTaskSubmitContainer from "@/components/rdms/project-task-submit-container.vue";

import DevelopCharacterJobitemIssue from "@/components/rdms/develop-character-jobitem-issue.vue";
import DevelopJoblistCharacterDevelop from "@/components/rdms/develop-joblist-character-develop.vue";
import DevelopCharacterJobitemExamine from "@/components/rdms/develop-character-jobitem-examine.vue";
import ReviewDevelopContainer from "@/components/rdms/review-develop-container.vue";
import JobitemReviewPjmContainer from "@/components/rdms/jobitem-review-pjm-container.vue";

import BugTaskJobitemIssue from "@/components/rdms/bug-task-jobitem-issue.vue";
import BugJobitemExamine from "@/components/rdms/bug-jobitem-examine.vue";
import BugFeedbackContainer from "@/components/rdms/bug-feedback-container.vue";

import Swal from "sweetalert2";

export default {
  components: {
    BugTaskJobitemIssue,
    BugJobitemExamine,

    JobitemReviewPjmContainer,
    DevelopJoblistCharacterDevelop,
    ProjectTaskSubmitContainer,

    ReviewDevelopContainer,
    DevelopCharacterJobitemIssue,
    DevelopCharacterJobitemExamine,
    BugFeedbackContainer,
  },
  name: "bug-manage-task-tabs",

  props: {
    bugfeedbackId: {
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

      activeName: 'develop',
      examineNum: 0,

    }
  },

  computed: {},

  mounted: function () {
    let _this = this;
    _this.getLoginUser();
    if(Tool.isNotEmpty(_this.bugfeedbackId)){
      _this.getExamineNum(_this.bugfeedbackId);
    }

    _this.$bus.$on('refresh-bug-manage-task-tab-num', (data) => {
      if(data){
        _this.getExamineNum(_this.bugfeedbackId);
      }
    });
  },

  beforeDestroy(){
    let _this = this;
    _this.$bus.$off('refresh-bug-manage-task-tab-num');
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

    getExamineNum(bugfeedbackId){
      let _this = this;
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/job-items/getExamineNum_bugfeedback/' + bugfeedbackId).then((response)=>{
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
