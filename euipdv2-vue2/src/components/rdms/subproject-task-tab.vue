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

      <!--     项目任务 -->
      <el-tab-pane :disabled="subProject.status === PROJECT_STATUS.OUT_SOURCE.key" label="子项目任务" name="develop">
        <div v-if="activeName === 'develop'">
          <SubprojectTaskIssue
              ref="taskJoblist"
              :key="('issue-' + (subProjectId))"
              :sub-project-id="subProjectId"
              :job-type="jobType"
              :project-type="projectType"
              :index="index"
              :entrance="entrance"
          ></SubprojectTaskIssue>
        </div>
      </el-tab-pane>

      <!--     工作审核 -->
      <el-tab-pane :disabled="subProject.status === PROJECT_STATUS.OUT_SOURCE.key" label="任务审核" name="evaluate">
        <div v-if="activeName === 'evaluate'">
          <SubprojectTaskExamine
              ref="evaluateJobitemList"
              :key="('evaluate-' + (subProjectId))"
              :index="index"
              :sub-project-id="subProjectId"
              :job-type="jobType"
              :project-type="projectType"
              :entrance="entrance"
          ></SubprojectTaskExamine>
        </div>
      </el-tab-pane>

      <!--      子项目评审  -->
      <el-tab-pane :disabled="!developCompleteFlag && subProject.status !== PROJECT_STATUS.OUT_SOURCE.key" label="子项目评审" name="task-submit">
        <div v-if="activeName === 'task-submit'">
          <ReviewSubprojectContainer
              :key="subProjectId"
              :sub-project-id="subProjectId"
              :job-type="JOB_ITEM_TYPE.REVIEW_SUBP.key"
              :project-type="projectType"
              :index="index"
              :entrance="entrance"
          ></ReviewSubprojectContainer>
        </div>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script>
import ProjectTaskSubmitContainer from "@/components/rdms/project-task-submit-container.vue";
import SubprojectTaskIssue from "@/components/rdms/subproject-task-issue.vue";
import SubprojectTaskExamine from "@/components/rdms/subproject-task-examine.vue";
import ReviewSubprojectContainer from "@/components/rdms/review-subproject-container.vue";
import Swal from "sweetalert2";

export default {
  components: {
    ReviewSubprojectContainer,
    SubprojectTaskIssue,
    ProjectTaskSubmitContainer,
    SubprojectTaskExamine,
  },
  name: "subproject-task-tab",

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
      PROJECT_STATUS: PROJECT_STATUS,
      developCompleteFlag: false,

      activeName: 'develop',
      examineNum: 0,
      subProject: {},

    }
  },

  computed: {},

  mounted: function () {
    let _this = this;
    _this.getLoginUser();
    _this.developCompleteFlag = false;

    if(Tool.isNotEmpty(_this.subProjectId)){
      _this.getSubprojectDevelopCompleteStatus(_this.subProjectId);
      _this.getSubProjectInfo(_this.subProjectId);
    }

    _this.$bus.$on('refresh-subproject-task-tab-num', (data) => {
      if(data && _this.subProject){
        _this.getExamineNum(_this.subProject.projectManagerId);
      }
    });
    _this.$bus.$on('refresh-subproject-task-tab', (data) => {
      if(data){
        _this.getSubprojectDevelopCompleteStatus(_this.subProjectId);
      }
    });
  },

  beforeDestroy(){
    let _this = this;
    _this.$bus.$off('refresh-subproject-task-tab-num');
    _this.$bus.$off('refresh-subproject-task-tab');
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

    getSubprojectDevelopCompleteStatus(subprojectId) {
      let _this = this;
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/sub-project/getSubprojectDevelopCompleteStatus/' + subprojectId).then((response)=>{
        let resp = response.data;
        if (resp.success) {
          _this.developCompleteFlag = resp.content;
        } else {
          Toast.warning(resp.message,Swal);
        }
      })
    },

    handleClick(tab, event) {
      let _this = this;
      if(tab.name === 'develop'){
        _this.$refs.taskJoblist.getSubProjectAndTaskJobitems(_this.subProjectId);
      }else if(tab.name === 'evaluate'){
        // _this.$refs.evaluateJobitemList.getProjectAndTaskJobitems_submit(_this.subProjectId);
      }else if(tab.name === 'task-submit'){
        // _this.$refs.taskSubmitContainer.getJobitemDetailInfo();
      }
    },

    getSubProjectInfo(subProjectId){
      let _this = this;
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/sub-project/getSubProjectSimpleInfo/' + subProjectId).then((response) => {
        let resp = response.data;
        if (resp.success) {
          _this.subProject = resp.content;
          if(_this.subProject.status === PROJECT_STATUS.OUT_SOURCE.key){
            _this.activeName = 'task-submit';
          }
          if(_this.subProject && _this.subProject.projectManagerId){
            _this.getExamineNum(_this.subProject.projectManagerId);
          }
        } else {
          Toast.warning(resp.message, Swal);
        }
      })
    },

    getExamineNum(pjmId){
      let _this = this;
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/job-items/getSubmitSubTaskJobitemNumByPjmAndSubprojectId', {
        subprojectId: _this.subProjectId,
        projectManagerId: pjmId,
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
