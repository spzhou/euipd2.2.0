<!--
 * Author: 周朔鹏
 * Date: 2024-01-23
 * Email: 391902958@qq.com
 * Website: https://www.euipd.com
-->

<template>
  <div class="page-container">
    <div class="page-left">
      <QualityOwnInfoPlain
          :key="qualityId"
          :quality-id="qualityId"
      ></QualityOwnInfoPlain>
    </div>
    <div class="page-right">
      <QualityFeedbackEditPage
          :key="qualityId"
          :quality-id="qualityId"
          :job-type="jobType"
          :project-type="projectType"
          :index="index"
          :entrance="entrance">
      </QualityFeedbackEditPage>
    </div>
  </div>
</template>

<script>
import File from "../../components/rdms/file";
import Swal from "sweetalert2";
import QualityFeedbackEditPage from "@/components/rdms/quality-feedback-edit-page.vue";
import QualityOwnInfoPlain from "@/components/rdms/quality-own-info-plain.vue";

export default {
  components: {
    QualityOwnInfoPlain,
    File,
    QualityFeedbackEditPage,
  },
  name: "quality-feedback-container",

  props: {
    qualityId: {
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
    if(Tool.isNotEmpty(_this.qualityId)){
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
