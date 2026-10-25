<!--
 * Author: 周朔鹏
 * Date: 2024-11-22
 * Email: 391902958@qq.com
 * Website: https://www.euipd.com
-->

<template>
  <div class="page-container">
    <div class="page-left">
      <div v-if="projectType === PROJECT_TYPE.PROJECT.key" style="height: 100%;">
<!--        <div v-if="parentJobType === JOB_ITEM_TYPE.REVIEW.key">
          <CharacterOriginalPlainPage
              :key="jobItem.characterId"
              :jobitem-id="jobitemId"
              :character-id="jobItem.characterId"
              :job-type="jobType"
              :project-type="projectType"
              :index="index"
              :entrance="entrance"
          > &lt;!&ndash;这是原始记录的id&ndash;&gt;
          </CharacterOriginalPlainPage>
        </div>
        <div v-if="parentJobType === JOB_ITEM_TYPE.REVIEW_SUBP.key">
          <ReviewSubprojectInfoPlain
              :key="jobItem.subprojectId"
              :sub-project-id="jobItem.subprojectId"
              :review-jobitem-id="jobItem.parentJobitemId"
          ></ReviewSubprojectInfoPlain>
        </div>-->
      </div>
    </div>
    <div class="page-right">
      <budgetAdjustNotifySheetPage
          :key="jobitemId"
          :jobitem-id="jobitemId"
          :job-type="jobType"
          :project-type="projectType"
          :index="index"
          :entrance="entrance"
          :confirm-path="confirmPath">
      </budgetAdjustNotifySheetPage>
    </div>
  </div>
</template>

<script>
import File from "../../components/rdms/file";
import Swal from "sweetalert2";
import CharacterOriginalPlainPage from "@/components/rdms/character-original-plain-page.vue";
import ReviewSubprojectInfoPlain from "@/components/rdms/review-subproject-info-plain.vue";
import budgetAdjustNotifySheetPage from "@/components/rdms/budget-adjust-notify-sheet-page.vue";

export default {
  components: {
    File,
    budgetAdjustNotifySheetPage,
    CharacterOriginalPlainPage,
    ReviewSubprojectInfoPlain,
  },
  name: "budget-adjust-notify-container",

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
    confirmPath: {
      type: String,
      default: '/customer/job-items/confirmBudgetAdjustNotify/',
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
