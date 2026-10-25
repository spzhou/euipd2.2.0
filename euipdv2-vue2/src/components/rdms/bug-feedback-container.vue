<!--
 * Author: 周朔鹏
 * Date: 2025-02-27
 * Email: 391902958@qq.com
 * Website: https://www.euipd.com
-->

<template>
  <div class="page-container">
    <div class="page-left">
      <BugOwnInfoPlain
          :key="bugfeedbackId"
          :bugfeedback-id="bugfeedbackId"
      ></BugOwnInfoPlain>
    </div>
    <div class="page-right">
      <BugFeedbackEditPage
          :key="bugfeedbackId"
          :bugfeedback-id="bugfeedbackId"
          :job-type="jobType"
          :project-type="projectType"
          :index="index"
          :entrance="entrance">
      </BugFeedbackEditPage>
    </div>
  </div>
</template>

<script>
import File from "../../components/rdms/file";
import Swal from "sweetalert2";
import BugOwnInfoPlain from "@/components/rdms/bug-own-info-plain.vue";
import BugFeedbackEditPage from "@/components/rdms/bug-feedback-edit-page.vue";

export default {
  components: {
    BugOwnInfoPlain,
    File,
    BugFeedbackEditPage,
  },
  name: "bug-feedback-container",

  props: {
    bugfeedbackId: {
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
      DOC_TYPE: DOC_TYPE,
    }
  },

  computed: {},

  mounted: function () {
    let _this = this;
    if(Tool.isNotEmpty(_this.bugfeedbackId)){
      // _this.getJobitemSimpleInfo(_this.jobitemId);
    }
  },

  methods: {

  },
}
</script>

<style lang="scss" scoped>
@import './../../../public/static/scss/caipd.scss';

</style>
