<!--
 * Author: 周朔鹏
 * Date: 2025-02-24
 * Email: 391902958@qq.com
 * Website: https://www.euipd.com
-->

<template>
  <div>
    <div class="row">
      <div style="margin-left: 20px;">
        <p>项目概要</p>
        <div class="hr hr-8 dotted hr-double"></div>
        <div>
          <SubprojectShowSummary
              :key="((subProject.id) + '-' + (random))"
              :entrance="entrance"
              :sub-project="subProject">
          </SubprojectShowSummary>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import SubprojectShowSummary from "@/components/rdms/subproject-show-summary.vue";
import Swal from "sweetalert2";

export default {
  components: {
    SubprojectShowSummary,
  },
  name: "project-info-outline",

  props:{
    xProjectId: {
      default: null,
    },
    projectType: {
      default: null,
    },
    entrance: {
      default: null,
    }
  },

  data: function() {
    return {
      subProject: {},
      PROJECT_TYPE: PROJECT_TYPE,
      random: 0,
    }
  },

  computed: {

  },

  mounted: function() {
    let _this = this;
    _this.getLoginUser();

    if(Tool.isNotEmpty(_this.xProjectId)){
      _this.getSubProjectInfo(_this.xProjectId);
    }

    _this.$bus.$on('refresh-project-info-outline', (data)=> {
      if(data && _this.xProjectId){
        _this.getSubProjectInfo(_this.xProjectId);
      }
    });
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
          _this.random = Math.random();
        } else {
          Toast.warning(resp.message,Swal);
        }
      })
    },

  }
}
</script>

<style lang="scss" scoped>

</style>
