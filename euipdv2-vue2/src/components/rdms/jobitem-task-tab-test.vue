<!--
 * Author: 周朔鹏
 * Date: 2025-02-25
 * Email: 391902958@qq.com
 * Website: https://www.euipd.com
-->

<template>
  <div>
    <div style="position: relative;">
      <el-badge v-show="examineNum>0" :value="examineNum" class="item" style="position: absolute; left: 180px; top: 3px; z-index: 100;"></el-badge>
    </div>
    <el-tabs v-model="activeName" type="card" @tab-click="handleClick">

<!--     特性开发 -->
      <el-tab-pane label="缺陷处理" name="develop">
        <div v-if="activeName === 'develop'">
          <JobitemTaskIssueContainer
              ref="assistJobitemList"
              :key="('assist-' + (jobitemId))"
              :jobitem-id="jobitemId"
              :job-type="jobType"
              :project-type="projectType"
              :index="index"
              :entrance="entrance"
          ></JobitemTaskIssueContainer>
        </div>
      </el-tab-pane>

<!--     工作审核 -->
      <el-tab-pane label="任务处理" name="examine">
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
      <el-tab-pane label="测试交付" name="submit">
        <div v-if="activeName === 'submit'">
          <JobitemTestSubmitContainer
              :key="('submit-' + (jobitemId))"
              :project-type="projectType"
              :jobitem-id="jobitemId"
              :job-type="jobType"
              :index="index"
              :entrance="entrance">
          </JobitemTestSubmitContainer>
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
import JobitemTaskIssueContainer from "@/components/rdms/jobitem-task-issue-container.vue";
import JobitemTaskExamineContainer from "@/components/rdms/jobitem-task-examine-container.vue";
import JobitemTaskReferenceContainer from "@/components/rdms/jobitem-task-reference-container.vue";
import JobitemTestSubmitContainer from "@/components/rdms/jobitem-test-submit-container.vue";

export default {
  components: {
    JobitemTestSubmitContainer,
    DevelopJoblistJobitemTesting,
    JobitemTaskExamineContainer,
    JobitemTaskIssueContainer,
    JobitemTaskReferenceContainer,
  },
  name: "jobitem-task-tab-test",

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

      subProjectId: {
        default: null,
      },

      activeName: 'submit',
      evaluateNum: 0,
      examineNum: 0,

      preProject: {
        default: null,
      },
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

    if(Tool.isNotEmpty(_this.jobitemId)){
      _this.getExamineNum(_this.jobitemId);
    }

    _this.$bus.$on('refresh-jobitem-task-tab-test-num', (data) => {
      if(data && Tool.isNotEmpty(_this.jobitemId)){
        _this.getExamineNum(_this.jobitemId);
      }
    });

  },

  beforeDestroy(){
    let _this = this;
    _this.$bus.$off('refresh-jobitem-task-tab-test-num');
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
        // _this.$refs.assistJobitemList.getJobitemAndAllAssistJobitems(_this.jobitemId);
      }else if(tab.name === 'examine'){
        _this.$refs.examineJobitemList.getJobitemAndAllEvaluateJobitems(_this.jobitemId);
      }else if(tab.name === 'submit'){
        // _this.$refs.characterSubmitContainer.getJobitemDetailInfo();
      }
    },

    getExamineNum(taskJobitemId){
      let _this = this;
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/job-items/getExamineNum_test/' + taskJobitemId).then((response)=>{
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
