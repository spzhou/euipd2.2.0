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

      <!--     质量任务 -->
      <el-tab-pane label="质量任务" name="develop">
        <div v-if="activeName === 'develop'">
          <QualityJobitemIssue
              :key="qualityId"
              ref="jobitemList"
              :quality-id="qualityId"
              :job-type="jobType"
              :project-type="projectType"
              :index="index"
              :entrance="entrance"
          ></QualityJobitemIssue>
        </div>
      </el-tab-pane>

      <!--     任务审核 -->
      <el-tab-pane label="任务审核" name="examine">
        <div v-if="activeName === 'examine'">
          <QualityJobitemExamine
              ref="examineJobitemList"
              :index="index"
              :key="qualityId"
              :quality-id="qualityId"
              :job-type="jobType"
              :project-type="projectType"
              :entrance="entrance"
          ></QualityJobitemExamine>
        </div>
      </el-tab-pane>

      <el-tab-pane label="结案反馈" name="review-submit">
        <div v-if="activeName === 'review-submit'">
          <QualityFeedbackContainer
              :key="qualityId"
              :quality-id="qualityId"
              :job-type="jobType"
              :project-type="projectType"
              :index="index"
              :entrance="entrance"
          ></QualityFeedbackContainer>
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

import QualityJobitemIssue from "@/components/rdms/quality-jobitem-issue.vue";
import QualityJobitemExamine from "@/components/rdms/quality-jobitem-examine.vue";
import QualityFeedbackContainer from "@/components/rdms/quality-feedback-container.vue";

import Swal from "sweetalert2";

export default {
  components: {
    QualityJobitemIssue,
    QualityJobitemExamine,

    JobitemReviewPjmContainer,
    DevelopJoblistCharacterDevelop,
    ProjectTaskSubmitContainer,

    ReviewDevelopContainer,
    DevelopCharacterJobitemIssue,
    DevelopCharacterJobitemExamine,
    QualityFeedbackContainer,
  },
  name: "quality-manage-task-tabs",

  props: {
    qualityId: {
      default: null,
    },
    characterId: {
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
    if(Tool.isNotEmpty(_this.qualityId)){
      _this.getExamineNum(_this.qualityId);
    }

    _this.$bus.$on('refresh-quality-manage-task-tab-num', (data) => {
      if(data){
        _this.getExamineNum(_this.qualityId);
      }
    });

  },

  beforeDestroy(){
    let _this = this;
    _this.$bus.$off('refresh-quality-manage-task-tab-num');
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
        // _this.$refs.jobitemList.getCharacterAndJobitemList(_this.characterId);
      }else if(tab.name === 'examine'){
        // _this.$refs.examineJobitemList.getCharacterAndSubmitJobitems(_this.characterId);
      }else if(tab.name === 'review-submit'){
        // _this.$refs.taskSubmitContainer.getJobitemDetailInfo();
      }
    },

    getExamineNum(qualityId){
      let _this = this;
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/job-items/getExamineNum_quality/' + qualityId).then((response)=>{
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
