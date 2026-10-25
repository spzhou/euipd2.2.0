<!--
 * Author: 周朔鹏
 * Date: 2024-11-05
 * Email: 391902958@qq.com
 * Website: https://www.euipd.com
-->

<template>
  <div class="page-container">
    <div class="page-left">
      <div>
        <CharacterOriginalPlainPage
            :key="jobItem.parentJobitemId"
            :jobitem-id="jobItem.parentJobitemId"
            :job-name-prefix="'被拒'"
            :project-type="projectType"
            :character-id="jobItem.characterId"> <!--这是原始记录的id-->
        </CharacterOriginalPlainPage>
      </div>
    </div>
    <div class="page-right">
      <JobitemReIssuePage
          :key="jobItem.parentJobitemId"
          :jobitem-id="jobItem.parentJobitemId"
          :index="index"
          :project-type="projectType"
          :entrance="entrance"
      ></JobitemReIssuePage>
    </div>
  </div>
</template>

<script>
import File from "../../components/rdms/file";
import Swal from "sweetalert2";
import JobitemReviewNotifySheetPage from "@/components/rdms/jobitem-review-notify-sheet-page";
import CharacterOriginalPlainPage from "@/components/rdms/character-original-plain-page.vue";
import JobitemReIssuePage from "@/components/std/jobitem-re-issue-page.vue";

export default {
  components: {
    JobitemReIssuePage,
    File,
    JobitemReviewNotifySheetPage,
    CharacterOriginalPlainPage,
  },
  name: "jobitem-refuse-notify-container",

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
      randomInt: 0,
      PROJECT_TYPE: PROJECT_TYPE,
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
    refreshIterationCreate(){
      let _this = this;
      _this.randomInt = Math.random();
    },

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
