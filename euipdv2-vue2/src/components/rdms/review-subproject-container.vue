<!--
 * Author: 周朔鹏
 * Date: 2023-12-26
 * Email: 391902958@qq.com
 * Website: https://www.euipd.com
-->

<template>
  <div class="page-container">
    <div class="page-left">
      <ReviewSubprojectInfoPlain
          :key="subProjectId"
          :sub-project-id="subProjectId">
      </ReviewSubprojectInfoPlain>
    </div>
    <div class="page-right">
      <ReviewSubprojectCreatePage
          :key="subProjectId"
          :subproject-id="subProjectId"
          :project-type="projectType"
          :index="index"
          :entrance="entrance"
      ></ReviewSubprojectCreatePage>
    </div>
  </div>
</template>

<script>
import File from "../../components/rdms/file";
import ReviewSubprojectInfoPlain from "@/components/rdms/review-subproject-info-plain.vue";
import ReviewSubprojectCreatePage from "@/components/std/review-subproject-create-page.vue";

export default {
  components: {
    File,
    ReviewSubprojectInfoPlain,
    ReviewSubprojectCreatePage,
  },
  name: "review-subproject-container",

  props: {
    subProjectId: {
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
      characterId: {
        default: null,
      },
      DOC_TYPE: DOC_TYPE,
    }
  },

  computed: {},

  mounted: function () {
    let _this = this;
    if(Tool.isNotEmpty(_this.characterId)){
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
