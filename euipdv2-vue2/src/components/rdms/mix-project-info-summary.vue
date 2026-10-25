<!--
 * Author: 周朔鹏
 * Date: 2024-11-14
 * Email: 391902958@qq.com
 * Website: https://www.euipd.com
-->

<template>
  <div>
    <div class="row">
      <div style="margin: 20px 25px">
        <div style="display: flex; flex-direction: row; justify-content: space-between;">
          <div>项目概要</div>
          <div>
            <div v-if="characterNum === 0 && showFlag === true">
<!--              <el-button type="warning" size="mini" @click="deleteProject">删除</el-button>-->
            </div>
          </div>
        </div>
        <div class="hr hr-8 dotted hr-double"></div>
        <div v-if="projectType === PROJECT_TYPE.PROJECT.key">
          <ProjectShowSummary
              :key="project.id"
              :project="project"
              :setup-stage="setupStage"
          ></ProjectShowSummary>
        </div>
        <div v-if="projectType === PROJECT_TYPE.SUB_PROJECT.key">
          <SubprojectShowSummary
              :key="subProject.id"
              :entrance="entrance"
              :sub-project="subProject">
          </SubprojectShowSummary>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import ProjectShowSummary from "@/components/rdms/project-show-summary.vue";
import SubprojectShowSummary from "@/components/rdms/subproject-show-summary.vue";
import Swal from "sweetalert2";

export default {
  components: {
    SubprojectShowSummary,
    ProjectShowSummary,
  },
  name: "mix-project-info-summary",

  props:{
    xProjectId: {
      default: null,
    },
    projectType: {
      default: null,
    },
    index: {
      default: null,
    },
    setupStage: {
      default: SETUP_STAGE.SETUPED.key,
    },
    entrance: {
      default: null,
    }
  },

  data: function() {
    return {
      project: {},
      subProject: {},
      PROJECT_TYPE: PROJECT_TYPE,
      characterNum: 0,
      showFlag: false,
      xProjectName: '',
    }
  },

  mounted: function() {
    let _this = this;
    _this.getLoginUser();

    if(Tool.isNotEmpty(_this.xProjectId) && Tool.isNotEmpty(_this.projectType)){
      if(_this.projectType === PROJECT_TYPE.PROJECT.key){
        _this.getProjectInfo(_this.xProjectId);
      }
      if(_this.projectType === PROJECT_TYPE.SUB_PROJECT.key){
        _this.getSubProjectInfo(_this.xProjectId);
      }
    }

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

    getProjectInfo(projectId){
      let _this = this;
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/project/getProjectInfo/' + projectId).then((response) => {
        let resp = response.data;
        if (resp.success) {
          _this.project = resp.content;
          _this.characterNum = _this.project.characterNum;
          _this.showFlag = true;
          _this.xProjectName = _this.project.projectName;
        } else {
          Toast.warning(resp.message, Swal);
        }
      })
    },

    getSubProjectInfo(subProjectId) {
      // debugger
      let _this = this;
      let formData = new window.FormData();
      formData.append('subProjectId', subProjectId);
      formData.append('loginUserId', _this.loginUser.customerUserId);
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/sub-project/getSubProjectInfo', formData).then((response)=>{
        let resp = response.data;
        if (resp.success) {
          _this.subProject = resp.content;
          _this.xProjectName = _this.subProject.label;
        } else {
          Toast.warning(resp.message,Swal);
        }
      })
    },

    deleteProject() {
      let _this = this;
      let text = `是否确认删除"${ _this.xProjectName }"？`;
      Swal.fire({
        title: '确认？',
        text: text,
        icon: 'warning',
        showCancelButton: true,
        confirmButtonColor: '#409EFF',
        cancelButtonColor: '#909399',
        confirmButtonText: '确认!',
        cancelButtonText: '取消!'
      }).then((result) => {
        if (result.value) {
          let requestPath = '';
          if(Tool.isNotEmpty(_this.xProjectId) && Tool.isNotEmpty(_this.projectType)){
            if(_this.projectType === PROJECT_TYPE.PROJECT.key){
              requestPath = '/customer/project/del/';
            }
            if(_this.projectType === PROJECT_TYPE.SUB_PROJECT.key){
              requestPath = '/customer/sub-project/del/';
            }
          }

          _this.$ajax.delete(process.env.VUE_APP_SERVER + requestPath+ _this.xProjectId).then((response)=>{
            let resp = response.data;
            if (resp.success) {
              Toast.success("删除成功!", Swal);
            _this.$bus.$emit('close-setup-detail-tab', _this.index);
            } else {
              Toast.warning(resp.message, Swal);
            }
          })
        }
      })
    },

  }
}
</script>

<style lang="scss" scoped>

</style>
