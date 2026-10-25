<!--
 * Author: 周朔鹏
 * Date: 2024-11-05
 * Email: 391902958@qq.com
 * Website: https://www.euipd.com
-->

<template>
  <div class="page-container">
    <div class="page-left">
      <div v-if="projectType === PROJECT_TYPE.SUB_PROJECT.key" style="height: 100%;">
        <div v-if="parentJobType === JOB_ITEM_TYPE.REVIEW.key">
          <CharacterOriginalPlainPage
              :key="jobItem.characterId"
              :jobitem-id="jobitemId"
              :character-id="jobItem.characterId"
              :job-type="jobType"
              :project-type="projectType"
              :index="index"
              :entrance="entrance"
          > <!--这是原始记录的id-->
          </CharacterOriginalPlainPage>
        </div>
        <div v-if="parentJobType === JOB_ITEM_TYPE.REVIEW_SUBP.key">
          <ReviewSubprojectInfoPlain
              :key="jobItem.subprojectId"
              :sub-project-id="jobItem.subprojectId"
              :review-jobitem-id="jobItem.parentJobitemId"
          ></ReviewSubprojectInfoPlain>
        </div>
      </div>
<!--      <div v-else-if="projectType === PROJECT_TYPE.PROJECT.key"  style="height: 100%;">
        <ReviewProjectInfoPlain
            :project-id="jobItem.projectId"
            :key="jobItem.projectId"
            :job-type="jobType"
            :project-type="projectType"
            :index="index"
            :entrance="entrance"
        ></ReviewProjectInfoPlain>
      </div>-->
    </div>
    <div class="page-right">
      <jobitem-review-notify-sheet-page
          :key="jobitemId"
          :jobitem-id="jobitemId"
          :job-type="jobType"
          :project-type="projectType"
          :index="index"
          :entrance="entrance">
      </jobitem-review-notify-sheet-page>
    </div>
  </div>
</template>

<script>
import File from "../../components/rdms/file";
import Swal from "sweetalert2";
import JobitemReviewNotifySheetPage from "@/components/rdms/jobitem-review-notify-sheet-page";
import CharacterOriginalPlainPage from "@/components/rdms/character-original-plain-page.vue";
import ReviewProjectInfoPlain from "@/components/rdms/review-project-info-plain.vue";
import ReviewSubprojectInfoPlain from "@/components/rdms/review-subproject-info-plain.vue";

export default {
  components: {
    ReviewProjectInfoPlain,
    File,
    JobitemReviewNotifySheetPage,
    CharacterOriginalPlainPage,
    ReviewSubprojectInfoPlain,
  },
  name: "jobitem-review-notify-container",

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
      parentJobType: null,
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

    refreshIterationCreate(){
      let _this = this;
      _this.randomInt = Math.random();
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
          _this.parentJobType = _this.jobItem.parentJobType;
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
