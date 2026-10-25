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

      <!--     已发任务 -->
      <el-tab-pane label="已发任务" name="develop">
        <div v-if="activeName === 'develop'">
          <ReviewTaskIssueContainer
              ref="assistJobitemList"
              :key="('assist-' + (jobitemId))"
              :jobitem-id="jobitemId"
              :job-type="jobType"
              :project-type="projectType"
              :index="index"
              :entrance="entrance"
          ></ReviewTaskIssueContainer>
        </div>
      </el-tab-pane>

      <!--     工作审核 -->
      <el-tab-pane label="工作审核" name="evaluate">
        <div v-if="activeName === 'evaluate'">
          <ReviewTaskExamineContainer
              ref="evaluateJobitemList"
              :key="('evaluate-' + (jobitemId))"
              :jobitem-id="jobitemId"
              :job-type="jobType"
              :project-type="projectType"
              :entrance="entrance"
              :index="index"
          ></ReviewTaskExamineContainer>
        </div>
      </el-tab-pane>

      <!--      评审意见-->
      <el-tab-pane label="评审意见" name="opinion">
        <div v-if="activeName === 'opinion'">
          <ReviewTaskOpinionContainer
              ref="reviewOpinion"
              :key="jobitemId"
              :jobitem-id="jobitemId"
              :job-type="jobType"
              :project-type="projectType"
              :index="index"
              :entrance="entrance">
          </ReviewTaskOpinionContainer>
        </div>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script>
import DevelopJoblistJobitemTesting from "@/components/rdms/develop-joblist-jobitem-testing";
import Swal from "sweetalert2";
import ReviewTaskIssueContainer from "@/components/rdms/review-task-issue-container.vue";
import ReviewTaskExamineContainer from "@/components/rdms/review-task-examine-container.vue";
import JobitemTaskSubmitContainer from "@/components/rdms/jobitem-task-submit-container.vue";
import ReviewTaskOpinionContainer from "@/components/rdms/review-task-opinion-container.vue";

export default {
  components: {
    ReviewTaskOpinionContainer,
    DevelopJoblistJobitemTesting,
    ReviewTaskExamineContainer,
    ReviewTaskIssueContainer,
    JobitemTaskSubmitContainer,
  },
  name: "review-joblist-tab",

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

      activeName: 'opinion',
      evaluateNum: 0,
      examineNum: 0,

    }
  },

  computed: {},

  mounted: function () {
    let _this = this;
    _this.getLoginUser();
    _this.$refs.reviewOpinion.getJobitemSimpleInfo(_this.jobitemId);

    _this.getExamineNum(_this.jobitemId);

    _this.$bus.$on('refresh-jobitem-task-tab-num', (data) => {
      if(data){
        _this.getExamineNum(_this.jobitemId);
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

    handleClick(tab, event) {
      let _this = this;
      if(tab.name === 'develop'){
        // _this.$refs.assistJobitemList.getReviewProcessTaskJobitems(_this.jobitemId);
      }else if(tab.name === 'evaluate'){
        // _this.$refs.evaluateJobitemList.getReviewProcessTaskSubmitJobitems();
      }else if(tab.name === 'opinion'){
        _this.$refs.reviewOpinion.getJobitemSimpleInfo(_this.jobitemId);
      }
    },

    getExamineNum(reviewJobitemId){
      let _this = this;
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/job-items/getReviewRelatedSubmitJobNum/' + reviewJobitemId).then((response)=>{
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
