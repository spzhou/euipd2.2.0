<!--
 * Author: 周朔鹏
 * Date: 2024-11-04
 * Email: 391902958@qq.com
 * Website: https://www.euipd.com
-->

<template>
  <div class="page-container">
    <div class="page-left">
      <JobitemAllInfoPlain
          :key="jobitemId"
          :jobitem-id="jobitemId"
          :job-type="jobType"
          :project-type="projectType"
          :index="index"
          :entrance="entrance"
      ></JobitemAllInfoPlain>
    </div>
    <div class="page-right">
      <JobitemTaskSubmitPage
          :key="jobItem.id"
          :jobitem-id="jobItem.id"
          :job-type="jobType"
          :project-type="projectType"
          :index="index"
          :entrance="entrance">
      </JobitemTaskSubmitPage>
    </div>
  </div>
</template>

<script>
import File from "../../components/rdms/file";
import Swal from "sweetalert2";
import JobitemTaskSubmitPage from "@/components/rdms/jobitem-task-submit-page.vue";
import JobitemReviewNotifySheetPlaint from "@/components/rdms/jobitem-review-notify-sheet-plaint";
import JobitemAllInfoPlain from "@/components/std/jobitem-all-info-plain.vue";

export default {
  components: {
    JobitemAllInfoPlain,
    File,
    JobitemTaskSubmitPage,
    JobitemReviewNotifySheetPlaint,
  },
  name: "jobitem-project-task-submit-container",

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
      jobitemId: '',
      jobItem: {},
      PROJECT_TYPE: PROJECT_TYPE,
    }
  },

  computed: {},

  mounted: function () {
    let _this = this;
    _this.getLoginUser();
    if(Tool.isNotEmpty(_this.jobitemId)){
      _this.getJobitemDetailInfo(_this.jobitemId)
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
    getJobitemDetailInfo(jobitemId) {
      let _this = this;
      let formData = new window.FormData();
      formData.append('jobitemId', jobitemId);
      formData.append('loginUserId', _this.loginUser.customerUserId);
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/job-items/getJobitemDetailInfo', formData).then((response) => {
        let resp = response.data;
        if (resp.success) {
          _this.jobItem = resp.content;
        }else{
          Toast.warning(resp.message, Swal);
        }
      });
    },


  },
}
</script>

<style lang="scss" scoped>
@import './../../../public/static/scss/caipd.scss';

</style>
