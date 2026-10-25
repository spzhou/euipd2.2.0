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
        <div v-if="testedJobType === JOB_ITEM_TYPE.REVIEW.key || testedJobType === JOB_ITEM_TYPE.REVIEW_CHANGE.key">
          <CharacterIntJobitemAssistShowPlain
              :key="jobItem.characterId"
              :character-id="jobItem.characterId"
              :index="index"
              :project-type="projectType"
              :entrance="entrance"
          ></CharacterIntJobitemAssistShowPlain>
        </div>
        <div v-if="testedJobType === JOB_ITEM_TYPE.REVIEW_SUBP.key">
          <ReviewSubprojectInfoPlain
              :key="jobItem.subprojectId"
              :sub-project-id="jobItem.subprojectId"
              :review-jobitem-id="jobItem.testedJobitemId"
          ></ReviewSubprojectInfoPlain>
        </div>
        <div v-if="testedJobType === JOB_ITEM_TYPE.REVIEW_MILESTONE.key">
          <ReviewMilestoneInfoPlain
              :key="jobItem.subprojectId"
              :sub-project-id="jobItem.subprojectId"
              :review-jobitem-id="jobItem.testedJobitemId"
          ></ReviewMilestoneInfoPlain>
        </div>

      </div>
      <div v-if="testedJobType === JOB_ITEM_TYPE.REVIEW_PRO.key" style="height: 100%;">
        <ReviewSubprojectInfoPlain
            :key="jobItem.subprojectId"
            :sub-project-id="jobItem.subprojectId"
            :review-jobitem-id="jobItem.testedJobitemId"
        ></ReviewSubprojectInfoPlain>
      </div>
    </div>
    <div class="page-right">
      <div  v-if="testedJobType === JOB_ITEM_TYPE.REVIEW.key || testedJobType === JOB_ITEM_TYPE.REVIEW_CHANGE.key">
        <ReviewCooContentTabs
            :key="jobitemId"
            :jobitem-id="jobitemId"
            :job-type="jobType"
            :project-type="projectType"
            :index="index"
            :entrance="entrance">
        </ReviewCooContentTabs>
      </div>
      <div v-if="testedJobType === JOB_ITEM_TYPE.REVIEW_SUBP.key || testedJobType === JOB_ITEM_TYPE.REVIEW_PRO.key">
        <ReviewSubprojectCooContentTabs
            :key="jobitemId"
            :jobitem-id="jobitemId"
            :job-type="jobType"
            :project-type="projectType"
            :index="index"
            :entrance="entrance">
        </ReviewSubprojectCooContentTabs>
      </div>
      <div v-if="testedJobType === JOB_ITEM_TYPE.REVIEW_MILESTONE.key">
        <ReviewMilestoneCooContentTabs
            :key="jobitemId"
            :jobitem-id="jobitemId"
            :job-type="jobType"
            :project-type="projectType"
            :index="index"
            :entrance="entrance">
        </ReviewMilestoneCooContentTabs>
      </div>

    </div>
  </div>
</template>

<script>
import File from "../../components/rdms/file";
import Swal from "sweetalert2";
import ReviewProjectInfoPlain from "@/components/rdms/review-project-info-plain.vue";
import ReviewSubprojectInfoPlain from "@/components/rdms/review-subproject-info-plain.vue";
import CharacterIntJobitemAssistShowPlain from "@/components/rdms/character-int-jobitem-info-plain.vue";
import ReviewCooContentTabs from "@/components/std/review-coo-content-tabs.vue";
import ReviewSubprojectCooContentTabs from "@/components/std/review-subproject-coo-content-tabs.vue";
import ReviewMilestoneInfoPlain from "@/components/rdms/review-milestone-info-plain.vue";
import ReviewMilestoneCooContentTabs from "@/components/std/review-milestone-coo-content-tabs.vue";

export default {
  components: {
    ReviewMilestoneInfoPlain,
    CharacterIntJobitemAssistShowPlain,
    File,
    ReviewCooContentTabs,
    ReviewProjectInfoPlain,
    ReviewSubprojectInfoPlain,
    ReviewSubprojectCooContentTabs,
    ReviewMilestoneCooContentTabs,
  },
  name: "jobitem-review-coo-container",

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
      JOB_ITEM_TYPE: JOB_ITEM_TYPE,

      testedJobType: '',
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
          _this.testedJobType = _this.jobItem.testedJobType;
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
