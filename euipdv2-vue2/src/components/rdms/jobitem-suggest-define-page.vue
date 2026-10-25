<!--
 * Author: 周朔鹏
 * Date: 2024-11-07
 * Email: 391902958@qq.com
 * Website: https://www.euipd.com
-->

<template>
  <div style="height: 100%;">
    <div class="paper-height">
      <div style="height: 100%; overflow: scroll; ">
        <div class="a4-paper-big-simple">
          <div style="margin: 60px 0 0">
            <JobitemCreateBySuggest
                :key="jobItem.id + 'suggest'"
                :job-item-origin="jobItem"
                :project-type="PROJECT_TYPE.SUB_PROJECT.key"
                :job-type="JOB_ITEM_TYPE.SUGGEST.key"
                :index="index"
                :entrance="entrance">
              ></JobitemCreateBySuggest>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>

import Swal from "sweetalert2";
import JobitemCreateBySuggest from "@/components/std/jobitem-create-by-suggest.vue";

export default {
  components: {
    JobitemCreateBySuggest,
  },
  name: "jobitem-suggest-define-page",

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


  data() {
    return {
      loginUser: {},
      loginCustomer: {},

      jobItem: {},
      characterItem: {},
      PROJECT_TYPE: PROJECT_TYPE,
      JOB_ITEM_TYPE: JOB_ITEM_TYPE,
    };
  },

  computed: {

  },

  mounted: function () {
    let _this = this;
    _this.getJobitemSimpleInfo(_this.jobitemId);
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
@import './../../../public/static/scss/caipd';

.paper-height {
  height: calc(100vh - 200px);
}

</style>
