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
      <el-tab-pane label="PLM任务" name="develop">
        <div v-if="activeName === 'develop'">
          <ProductTaskIssue
              ref="taskJoblist"
              :key="('issue-' + (projectId))"
              :project-id="projectId"
              :job-type="jobType"
              :project-type="projectType"
              :index="index"
              :entrance="entrance"
          ></ProductTaskIssue>
        </div>
      </el-tab-pane>

      <!--     工作审核 -->
      <el-tab-pane label="任务审核" name="evaluate">
        <div v-if="activeName === 'evaluate'">
          <ProductTaskExamine
              ref="evaluateJobitemList"
              :key="('evaluate-' + (projectId))"
              :index="index"
              :project-id="projectId"
              :job-type="jobType"
              :project-type="projectType"
              :entrance="entrance"
          ></ProductTaskExamine>
        </div>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script>
import ProjectTaskSubmitContainer from "@/components/rdms/project-task-submit-container.vue";
import ReviewSubprojectContainer from "@/components/rdms/review-subproject-container.vue";
import Swal from "sweetalert2";
import ProductTaskIssue from "@/components/rdms/product-task-issue.vue";
import ProductTaskExamine from "@/components/rdms/product-task-examine.vue";

export default {
  components: {
    ReviewSubprojectContainer,
    ProductTaskIssue,
    ProjectTaskSubmitContainer,
    ProductTaskExamine,
  },
  name: "product-task-tab",

  props: {
    projectId: {
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

    if(Tool.isNotEmpty(_this.projectId)){
      _this.getSubprojectDevelopCompleteStatus(_this.projectId);
      _this.getSubProjectInfo(_this.projectId);
    }

    _this.$bus.$on('refresh-product-task-tab-num', (data) => {
      if(data && _this.subProject){
        _this.getExamineNum(_this.subProject.projectManagerId);
      }
    });
    _this.$bus.$on('refresh-product-task-tab', (data) => {
      if(data){
        _this.getSubprojectDevelopCompleteStatus(_this.projectId);
      }
    });
  },

  beforeDestroy(){
    let _this = this;
    _this.$bus.$off('refresh-product-task-tab-num');
    _this.$bus.$off('refresh-product-task-tab');
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
        _this.$refs.taskJoblist.getProjectAndTaskJobitems(_this.projectId);
      }else if(tab.name === 'evaluate'){
        // _this.$refs.evaluateJobitemList.getProjectAndTaskJobitems_submit(_this.projectId);
      }else if(tab.name === 'task-submit'){
        // _this.$refs.taskSubmitContainer.getJobitemDetailInfo();
      }
    },

    getSubProjectInfo(projectId){
      let _this = this;
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/sub-project/getSubProjectSimpleInfo/' + projectId).then((response) => {
        let resp = response.data;
        if (resp.success) {
          _this.subProject = resp.content;
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
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/job-items/getSubmitTaskJobitemNumByPjmAndProjectId', {
        projectId: _this.projectId,
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
