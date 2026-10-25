<!--
 * Author: 周朔鹏
 * Date: 2024-11-03
 * Email: 391902958@qq.com
 * Website: https://www.euipd.com
-->

<template>
  <div class="paper-height">
    <div style="height: 100%; overflow-y: scroll;">
      <div class="a4-paper-big-simple" style="padding: 10px 25px 30px;">
        <div style="margin: 30px 0 20px;">
          <JobitemDetailInfoCore
              :key="jobitemId"
              :jobitem-id="jobitemId"
          ></JobitemDetailInfoCore>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import File from "../rdms/file.vue";
import Swal from "sweetalert2";

import JobitemDetailInfoCore from "@/components/std/jobitem-detail-info-core.vue";

export default {
  components: {
    File,
    JobitemDetailInfoCore,
  },
  name: "jobitem-detail-info-page",

  props: {
    docType: {
      default: null,
    },
    jobitemId: {
      default: null,
    },
    index: {
      default: null,
    },
    entrance: {
      default: null,
    }
  },

  data() {
    return {
      loginUser: {},
      jobItem: {},
      DOC_TYPE: DOC_TYPE,
    };
  },

  computed: {
  },

  mounted: function () {
    let _this = this;
    _this.getLoginUser();
    if(_this.jobitemId && (_this.docType === DOC_TYPE.JOBITEM.key || Tool.isEmpty(_this.docType))){
      _this.getJobitemDetailInfo(_this.jobitemId);
    }
  },

  beforeDestroy(){
    let _this = this;
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

    getJobitemDetailInfo(jobitemId){
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
@import './../../../public/static/scss/caipd';

.paper-height {
  height: calc(100vh - 250px);
}
</style>
