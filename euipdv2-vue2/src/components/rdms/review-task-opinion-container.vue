<!--
 * Author: 周朔鹏
 * Date: 2024-11-05
 * Email: 391902958@qq.com
 * Website: https://www.euipd.com
-->

<template>
  <div class="page-container">
    <div class="page-left">
      <div v-if="jobType === JOB_ITEM_TYPE.REVIEW.key || jobType === JOB_ITEM_TYPE.REVIEW_CHANGE.key">
        <CharacterIntJobitemAssistShowPlain
            :key="jobItem.characterId"
            :character-id="jobItem.characterId"
            :index="index"
            :project-type="projectType"
            :entrance="entrance"
        ></CharacterIntJobitemAssistShowPlain>
      </div>
      <div v-if="jobType === JOB_ITEM_TYPE.REVIEW_SUBP.key || jobType === JOB_ITEM_TYPE.REVIEW_PRO.key">
        <ReviewSubprojectInfoPlain
            :key="jobItem.subprojectId"
            :sub-project-id="jobItem.subprojectId"
            :review-jobitem-id="jobitemId"
        ></ReviewSubprojectInfoPlain>
      </div>
      <div v-if="jobType === JOB_ITEM_TYPE.REVIEW_MILESTONE.key">
        <ReviewMilestoneInfoPlain
            :key="jobItem.subprojectId"
            :sub-project-id="jobItem.subprojectId"
            :review-jobitem-id="jobitemId"
        ></ReviewMilestoneInfoPlain>
      </div>

    </div>
    <div class="page-right">
      <div v-if="jobItem.status === JOB_ITEM_STATUS.HANDLING.key" style="margin: 30px; font-size: 14px; color: #909399;">
        评审尚未审结。委员办理情况见“已发任务”页签；评审组长在“执行管理”中对本评审单“任务处理”填写结论并审结。
      </div>
      <div v-if="jobItem.status === JOB_ITEM_STATUS.COMPLETED.key
      || jobItem.status === JOB_ITEM_STATUS.CHA_RECHECK.key || jobItem.status === JOB_ITEM_STATUS.SUB_RECHECK.key">
        <ReviewTaskResultPage
            :key="jobitemId"
            :jobitem-id="jobitemId"
            :job-type="jobType"
            :project-type="projectType"
            :index="index"
            :entrance="entrance">
        </ReviewTaskResultPage>
      </div>
      <div v-if="jobItem.status === JOB_ITEM_STATUS.SUBMIT.key">
        <div v-if="jobItem.type === JOB_ITEM_TYPE.REVIEW.key || jobItem.type === JOB_ITEM_TYPE.REVIEW_CHANGE.key">
          <ReviewContentTabs
              :key="jobitemId"
              :jobitem-id="jobitemId"
              :job-type="jobType"
              :project-type="projectType"
              :index="index"
              :entrance="entrance">
          </ReviewContentTabs>
        </div>
        <div v-if="jobItem.type === JOB_ITEM_TYPE.REVIEW_SUBP.key || jobItem.type === JOB_ITEM_TYPE.REVIEW_PRO.key">
          <ReviewSubprojectContentTabs
              :key="jobitemId"
              :jobitem-id="jobitemId"
              :job-type="jobType"
              :project-type="projectType"
              :index="index"
              :entrance="entrance">
          </ReviewSubprojectContentTabs>
        </div>
        <div v-if="jobItem.type === JOB_ITEM_TYPE.REVIEW_MILESTONE.key">
          <ReviewMilestoneContentTabs
              :key="jobitemId"
              :jobitem-id="jobitemId"
              :job-type="jobType"
              :project-type="projectType"
              :index="index"
              :entrance="entrance">
          </ReviewMilestoneContentTabs>
        </div>

      </div>
    </div>
  </div>
</template>

<script>
import File from "../../components/rdms/file";
import Swal from "sweetalert2";
import ReviewTaskResultPage from "@/components/rdms/review-task-result-page.vue";
import ReviewSubprojectInfoPlain from "@/components/rdms/review-subproject-info-plain.vue";
import CharacterIntJobitemAssistShowPlain from "@/components/rdms/character-int-jobitem-info-plain.vue";
import ReviewContentTabs from "@/components/std/review-content-tabs.vue";
import ReviewSubprojectContentTabs from "@/components/std/review-subproject-content-tabs.vue";
import ReviewMilestoneInfoPlain from "@/components/rdms/review-milestone-info-plain.vue";
import ReviewMilestoneContentTabs from "@/components/std/review-milestone-content-tabs.vue";

export default {
  components: {
    CharacterIntJobitemAssistShowPlain,
    ReviewTaskResultPage,
    File,
    ReviewContentTabs,
    ReviewSubprojectContentTabs,
    ReviewSubprojectInfoPlain,
    ReviewMilestoneInfoPlain,
    ReviewMilestoneContentTabs,
  },
  name: "review-task-opinion-container",

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
      JOB_ITEM_STATUS: JOB_ITEM_STATUS,
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

    _this.$bus.$on('refresh-review-task-opinion-container', (data) => {
      if(data){
        // _this.getReviewProcessTaskSubmitJobitems(_this.jobitemId);
      }
    });
  },

  beforeDestroy(){
    let _this = this;
    _this.$bus.$off('refresh-review-task-opinion-container');
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
