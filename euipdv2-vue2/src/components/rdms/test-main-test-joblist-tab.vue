<!--
 * Author: 周朔鹏
 * Date: 2025-02-25
 * Email: 391902958@qq.com
 * Website: https://www.euipd.com
-->

<template>
  <div>
    <div style="position: relative;">
<!--      <el-badge v-show="examineNum>0" :value="examineNum" class="item" style="position: absolute; left: 180px; top: 3px; z-index: 100;"></el-badge>-->
<!--      <el-badge v-show="evaluateNum>0" :value="evaluateNum" class="item" style="position: absolute; left: 260px; top: 3px; z-index: 100;"></el-badge>-->
    </div>
    <el-tabs v-model="activeName" type="card" @tab-click="handleClick">
<!--      :operate="operate === 'editing_operate'"  这句代码只用于默认加载的时候起作用, handleClick会直接调用子组件的方法-->

<!--     项目任务 -->
      <el-tab-pane label="在测任务" name="develop">
        <div v-if="activeName === 'develop'">
          <test-main-test-joblist-issue
              ref="testingJoblist"
              :key="('issue-' + (subProjectId))"
              :index="index"
              :sub-project-id="subProjectId"
          ></test-main-test-joblist-issue>
        </div>
      </el-tab-pane>

<!--     工作审核 -->
      <el-tab-pane label="任务审核" name="evaluate">
        <div v-if="activeName === 'evaluate'">
          <test-subproject-joblist-evaluate
              ref="evaluateJobitemList"
              :key="('evaluate-' + (subProjectId))"
              :index="index"
              :sub-project-id="subProjectId"
              :job-type="jobType"
              :project-type="projectType"
              :entrance="entrance"
          ></test-subproject-joblist-evaluate>
        </div>
      </el-tab-pane>

<!--      任务交付-->
      <el-tab-pane :disabled="!showIntSubmitFlag" label="任务交付" name="task-submit">
        <div v-if="false && activeName === 'task-submit'">
          <project-task-submit-container
              ref="taskSubmitContainer"
              :key="('task-submit-' + (subProjectId))"
              :index="index"
              :project-id="subProjectId">
          </project-task-submit-container>
        </div>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script>
import ProjectTaskSubmitContainer from "@/components/rdms/project-task-submit-container.vue";
import TestMainTestJoblistIssue from "@/components/rdms/test-main-test-joblist-issue.vue";
import TestSubprojectJoblistEvaluate from "@/components/rdms/test-subproject-joblist-evaluate.vue";
import Swal from "sweetalert2";

export default {
  components: {
    TestMainTestJoblistIssue,
    ProjectTaskSubmitContainer,
    TestSubprojectJoblistEvaluate,
  },
  name: "test-main-test-joblist-tab",

  props: {
    subProjectId: {
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
      showIntSubmitFlag: false,

      activeName: 'develop',
      evaluateNum: 0,
      examineNum: 0,

    }
  },

  computed: {},

  mounted: function () {
    let _this = this;
    _this.getLoginUser();
    _this.showIntSubmitFlag = false;
    // _this.$refs.testingJoblist.getProjectAndTaskJobitems();

    // _this.getEvaluateNum();
    // _this.getExamineNum();

    _this.$bus.$on('refresh-test-main-test-joblist-tab-num', (data) => {
      if(data){
        // _this.getEvaluateNum();
        // _this.getExamineNum();
      }
    });
    // 调用 showIntSubmit 函数
    _this.$bus.$on('refresh-test-main-test-joblist-tab', (data) => {
      console.log("refresh-test-main-test-joblist-tab");
      if(data){
        // _this.getCompletedAssistJobitemList(_this.jobitemId);
      }
    });

    // _this.getCompletedAssistJobitemList(_this.jobitemId);

  },

  beforeDestroy(){
    let _this = this;
    _this.$bus.$off('refresh-test-main-test-joblist-tab-num');
    _this.$bus.$off('refresh-test-main-test-joblist-tab');
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

/*    getCompletedAssistJobitemList(jobitemId) {
      let _this = this;
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/job-items/getCompletedAssistJobitemList/' + jobitemId).then((response) => {
        let resp = response.data;
        if (resp.success) {
          _this.showIntSubmitFlag = resp.content.sumJobNum === resp.content.completeJobNum;
        } else {
          Toast.warning(resp.message, Swal);
        }
      })
    },*/

    handleClick(tab, event) {
      let _this = this;
      if(tab.name === 'develop'){
        _this.$refs.testingJoblist.getSubProjectAndTestJobitems(_this.subProjectId);
      }else if(tab.name === 'evaluate'){
        // _this.$refs.evaluateJobitemList.getSubProjectAndTestJobitems();
      }else if(tab.name === 'task-submit'){
        _this.showIntSubmitFlag = true;
        // _this.$refs.taskSubmitContainer.getJobitemDetailInfo();
      }
    },

/*    getExamineNum(){
      let _this = this;
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/job-items/get-system-examine-items-num',{
        customerId: _this.loginCustomer.id,
        subProjectId: _this.subProjectId,
        customerUserId: _this.loginUser.customerUserId,
      }).then((response)=>{
        let resp = response.data;
        if (resp.success) {
          _this.examineNum = resp.content;
        } else {
          Toast.warning(resp.message,Swal);
        }
      })
    },*/

/*    getEvaluateNum(){
      let _this = this;
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/job-items/get-system-evaluate-items-num',{
        customerId: _this.loginCustomer.id,
        subProjectId: _this.subProjectId,
        customerUserId: _this.loginUser.customerUserId,
      }).then((response)=>{
        let resp = response.data;
        if (resp.success) {
          _this.evaluateNum = resp.content;
        } else {
          Toast.warning(resp.message,Swal);
        }
      })
    },*/

  },
}
</script>

<style lang="scss" scoped>
@import './../../../public/static/scss/caipd.scss';

</style>
