<!--
 * Author: 周朔鹏
 * Date: 2024-12-07
 * Email: 391902958@qq.com
 * Website: https://www.euipd.com
-->

<template>
  <div class="page-container">
    <div class="page-left">
      <div v-if="jobItem.auxType !== JOB_ITEM_TYPE.QUALITY.key">
        <JobitemDetailParentShowPlain
            :key="jobItem.parentJobitemId"
            :jobitem-id="jobItem.parentJobitemId">
        </JobitemDetailParentShowPlain>
      </div>
      <div v-else>
        <JobitemDetailParentShowPlain
            :key="jobItem.parentJobitemId"
            :jobitem-id="jobItem.parentJobitemId">
        </JobitemDetailParentShowPlain>
        <QualityOriginalPlainPage
            :key="jobItem.id"
            :quality-id="jobItem.qualityId"
            :jobitem-id="jobItem.id"
            :index="index"
            :project-type="projectType"
            :entrance="entrance"
        ></QualityOriginalPlainPage>
      </div>
    </div>
    <div class="page-right">
      <JobitemSubmitPropertyPage
          :key="jobitemId"
          :jobitem-id="jobitemId"
          :job-type="jobType"
          :project-type="projectType"
          :index="index"
          :entrance="entrance">
      </JobitemSubmitPropertyPage>
    </div>
  </div>
</template>

<script>
import File from "../../components/rdms/file";
import Swal from "sweetalert2";
import JobitemDetailParentShowPlain from "@/components/rdms/jobitem-detail-parent-show-plain";
import JobitemSubmitPropertyPage from "@/components/rdms/jobitem-submit-property-page";
import QualityOriginalPlainPage from "@/components/rdms/quality-original-plain-page.vue";

export default {
  components: {
    QualityOriginalPlainPage,
    File,
    JobitemDetailParentShowPlain,
    JobitemSubmitPropertyPage,
  },
  name: "jobitem-assist-container",

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

      jobItem: {
        default: null,
      },

      PROJECT_TYPE: PROJECT_TYPE,
      JOB_ITEM_TYPE: JOB_ITEM_TYPE,
    }
  },

  computed: {},

  mounted: function () {
    let _this = this;
    _this.getLoginUser();
    if(Tool.isNotEmpty(_this.jobitemId)){
      _this.getJobitemSimpleInfo(_this.jobitemId);
    }
  },

  methods: {
    getLoginUser() {
      let _this = this;
      if (Tool.getLoginUser().userGroup === LOGIN_USER_GROUP.CUSTOMER.key) {
        _this.loginUser = Tool.getLoginUser().adminUser;
        _this.loginCustomer = Tool.getLoginUser();
      } else {
        _this.loginUser = Tool.getLoginUser();
        _this.loginCustomer = Tool.getLoginUser().customer;
      }
    },

    getJobitemSimpleInfo(jobitemId){
      let _this = this;
      let formData = new window.FormData();
      formData.append('jobitemId', jobitemId);
      formData.append('loginUserId', _this.loginUser.customerUserId);
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/job-items/getJobitemSimpleInfo', formData).then((response) => {
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
