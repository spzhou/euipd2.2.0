<!--
 * Author: 周朔鹏
 * Date: 2023-12-26
 * Email: 391902958@qq.com
 * Website: https://www.euipd.com
-->

<template>
  <div class="page-container">
    <div class="page-left">
      <CharacterIntJobitemAssistShowPlain
          :key="characterId"
          :character-id="characterId"
          :index="index"
          :project-type="projectType"
          :entrance="entrance"
      ></CharacterIntJobitemAssistShowPlain>
    </div>
    <div class="page-right">
      <ReviewCreatePage
          :key="characterId"
          :character-id="characterId"
          :project-type="projectType"
          :index="index"
          :entrance="entrance"
      ></ReviewCreatePage>
    </div>
  </div>
</template>

<script>
import File from "../../components/rdms/file";
import Swal from "sweetalert2";
import ReviewCreatePage from "@/components/std/review-create-page.vue";
import CharacterIntJobitemAssistShowPlain from "@/components/rdms/character-int-jobitem-info-plain.vue";

export default {
  components: {
    File,
    CharacterIntJobitemAssistShowPlain,
    ReviewCreatePage,
  },
  name: "review-develop-container",

  props: {
    characterId: {
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
