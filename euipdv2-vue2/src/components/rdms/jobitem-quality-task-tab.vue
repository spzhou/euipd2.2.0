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

<!--     特性开发 -->
      <el-tab-pane label="协作任务" name="develop">
        <div v-if="activeName === 'develop'">
          <QualityJobitemAssistIssueContainer
              ref="assistJobitemList"
              :key="('assist-' + (jobitemId))"
              :jobitem-id="jobitemId"
              :job-type="jobType"
              :project-type="projectType"
              :index="index"
              :entrance="entrance"
          ></QualityJobitemAssistIssueContainer>
        </div>
      </el-tab-pane>

<!--     工作审核 -->
      <el-tab-pane label="任务审核" name="examine">
        <div v-if="activeName === 'examine'">
          <JobitemTaskExamineContainer
              ref="examineJobitemList"
              :key="('examine-' + (jobitemId))"
              :jobitem-id="jobitemId"
              :job-type="jobType"
              :project-type="projectType"
              :entrance="entrance"
              :index="index"
          ></JobitemTaskExamineContainer>
        </div>
      </el-tab-pane>

<!--      任务交付-->
      <el-tab-pane label="任务交付" name="submit">
        <div v-if="activeName === 'submit'">
          <JobitemQualityAnalysisContainer
              ref="characterSubmitContainer"
              :key="('submit-' + (jobitemId) + '-' + (random))"
              :jobitem-id="jobitemId"
              :job-type="jobType"
              :project-type="projectType"
              :index="index"
              :entrance="entrance">
          </JobitemQualityAnalysisContainer>
        </div>
      </el-tab-pane>
<!--      参考资料-->
      <el-tab-pane label="参考资料" name="reference">
        <div v-if="activeName === 'reference'">
          <JobitemTaskReferenceContainer
              :key="('reference-' + (jobitemId))"
              :jobitem-id="jobitemId"
              :job-type="jobType"
              :project-type="projectType"
              :index="index"
              :entrance="entrance">
          </JobitemTaskReferenceContainer>
        </div>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script>
import DevelopJoblistJobitemTesting from "@/components/rdms/develop-joblist-jobitem-testing";
import Swal from "sweetalert2";
import JobitemTaskExamineContainer from "@/components/rdms/jobitem-task-examine-container.vue";
import JobitemTaskReferenceContainer from "@/components/rdms/jobitem-task-reference-container.vue";
import JobitemQualityAnalysisContainer from "@/components/rdms/jobitem-quality-analysis-container.vue";
import QualityJobitemAssistIssueContainer from "@/components/rdms/quality-jobitem-assist-issue-container.vue";

export default {
  components: {
    DevelopJoblistJobitemTesting,
    JobitemTaskExamineContainer,
    QualityJobitemAssistIssueContainer,
    JobitemQualityAnalysisContainer,
    JobitemTaskReferenceContainer,
  },
  name: "jobitem-quality-task-tab",

  props: {
    jobitemId: {
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

      // showIntSubmitFlag: false,
      // sumJobNum: 0,
      subProjectId: {
        default: null,
      },

      activeName: 'submit',
      evaluateNum: 0,
      examineNum: 0,

      preProject: {
        default: null,
      },
      random: 0,
    }
  },

  computed: {
    showReferenceDoc(){
      let _this = this;
      if(_this.jobType === JOB_ITEM_TYPE.CHARACTER_INT.key || _this.jobType === JOB_ITEM_TYPE.SUBPROJECT_INT.key || _this.jobType === JOB_ITEM_TYPE.PROJECT_INT.key){
        return true;
      }
      return false;
    }
  },

  mounted: function () {
    let _this = this;
    _this.getLoginUser();
    // _this.showIntSubmitFlag = false;

    if(Tool.isNotEmpty(_this.jobitemId)){
      _this.getExamineNum(_this.jobitemId);
      // _this.getCompletedAssistJobitemList(_this.jobitemId);
    }

    _this.$bus.$on('refresh-jobitem-task-tab-num', (data) => {
      if(data){
        _this.getExamineNum();
      }
    });
  },

  beforeDestroy(){
    let _this = this;
    _this.$bus.$off('refresh-jobitem-task-tab-num');
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

    getCompletedAssistJobitemList(jobitemId) {
      let _this = this;
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/job-items/getCompletedAssistJobitemList/' + jobitemId).then((response) => {
        let resp = response.data;
        if (resp.success) {
          _this.showIntSubmitFlag = resp.content.sumJobNum === resp.content.completeJobNum;
          _this.sumJobNum = resp.content.sumJobNum;
        } else {
          Toast.warning(resp.message, Swal);
        }
      })
    },

    handleClick(tab, event) {
      let _this = this;
      if(tab.name === 'develop'){
        // _this.$refs.assistJobitemList.getJobitemAndAllAssistJobitems(_this.jobitemId);
      }else if(tab.name === 'examine'){
        // _this.$refs.examineJobitemList.getJobitemAndAllEvaluateJobitems(_this.jobitemId);
      }else if(tab.name === 'submit'){
        _this.random = Math.random();
        // _this.$refs.characterSubmitContainer.getJobitemDetailInfo();
      }
    },

    getExamineNum(taskJobitemId){
      let _this = this;
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/job-items/getExamineNum_task/' + taskJobitemId).then((response)=>{
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
