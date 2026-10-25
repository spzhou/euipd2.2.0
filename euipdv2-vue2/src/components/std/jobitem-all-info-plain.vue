<!--
 * Author: 周朔鹏
 * Date: 2024-11-03
 * Email: 391902958@qq.com
 * Website: https://www.euipd.com
-->

<template>
  <div class="ca-plain-container" >
    <div v-if="jobItem">
      <JobitemOwnInfoPlain
          :key="jobItem.id"
          :jobitem-id="jobItem.id"
          :en-reject="enReject"
          :project-type="jobItem.projectType"
          :index="index"
          :entrance="entrance"
      ></JobitemOwnInfoPlain>
    </div>

    <div v-if="jobItem.assistJobitemList && jobItem.assistJobitemList.length >0">
      <div class="hr hr-10 dotted hr-double"></div>
      <div style="margin-left: 20px; font-weight: bold">
        协作任务信息
      </div>
      <div v-for="( item, i ) in jobItem.assistJobitemList" :key="i">
        <div class="hr hr-10 dotted"></div>
        <div>
          <JobitemOwnInfoPlain
              :key="item.id"
              :jobitem-id="item.id"
              :project-type="item.projectType"
              :index="index"
              :entrance="entrance"
          ></JobitemOwnInfoPlain>
        </div>
      </div>
    </div>

    <div v-if="testJobList.length > 0">
      <div class="hr hr-10 dotted hr-double"></div>
      <div style="margin-left: 20px; font-weight: bold">
        测试任务信息
      </div>
      <div v-for="( itemId, i ) in testJobList" :key="i">
        <div class="hr hr-10 dotted"></div>
        <div>
          <JobitemOwnInfoPlain
              :key="itemId"
              :jobitem-id="itemId"
              :job-type="jobType"
              :project-type="projectType"
              :index="index"
              :entrance="entrance"
          ></JobitemOwnInfoPlain>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import Swal from "sweetalert2";
import JobitemOwnInfoPlain from "@/components/std/jobitem-own-info-plain.vue";

export default {
  components: {
    JobitemOwnInfoPlain,
  },
  name: "jobitem-all-info-plain",

  props: {
    jobitemId: {
      default: null,
    },
    enReject: {
      default: false,
    },
    prefix: {
      default: '',
    },
    index: {
      default: null
    },
    projectType: {
      default: null,
    },
    jobType: {
      default: null,
    },
    entrance: {
      default: null,
    },
    excludeJobitemId: {
      default: null,
    }
  },

  data: function () {
    return {
      loginUser: {},
      loginCustomer: {},

      jobItem: {},

      testJobList: [],
    }
  },

  computed: {
  },

  mounted: function () {
    let _this = this;
    _this.getLoginUser();
    if(Tool.isNotEmpty(_this.jobitemId)){
      _this.getJobitemDetailInfo(_this.jobitemId);
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

    getJobitemDetailInfo(jobitemId) {
      let _this = this;
      let formData = new window.FormData();
      formData.append('jobitemId', jobitemId);
      formData.append('loginUserId', _this.loginUser.customerUserId);
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/job-items/getJobitemDetailInfo', formData).then((response) => {
        let resp = response.data;
        if (resp.success) {
          _this.jobItem = resp.content;
          let testJobListTemp = _this.jobItem.testJobitemIdList;
          if(_this.excludeJobitemId){
            testJobListTemp.forEach(item => {
              if(item !== _this.excludeJobitemId){
                _this.testJobList.push(item);
              }
            })
          }
        }else{
          Toast.warning(resp.message, Swal);
        }
      });
    },
  }
}
</script>

<style lang="scss" scoped>
@import '../../../public/static/scss/caipd';
</style>
