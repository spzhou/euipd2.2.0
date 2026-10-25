<!--
 * Author: 周朔鹏
 * Date: 2023-12-23
 * Email: 391902958@qq.com
 * Website: https://www.euipd.com
-->

<template>
  <div class="page-container">
    <div class="page-left">
        <demand-detail-original-show-plain
            :key="jobitemId"
            :jobitem-id="jobitemId"
            :job-type="jobType"
            :entrance="entrance"
            :project-type="projectType"
            :index="index"
        ></demand-detail-original-show-plain>
    </div>
    <div class="page-right">
      <character-demand-create
          :key="jobitemId + randomInt"
          :jobitem-id="jobitemId"
          :job-type="jobType"
          :project-type="projectType"
          :index="index"
          :entrance="entrance"
          :refresh="refreshDecomposeCreate">
      </character-demand-create>
    </div>
  </div>
</template>

<script>
import File from "../../components/rdms/file";
import DemandDetailOriginalShowPlain from "@/components/rdms/demand-detail-original-info-plain.vue";
import CharacterDemandCreate from "@/components/rdms/character-demand-create";

export default {
  components: {
    File,
    DemandDetailOriginalShowPlain,
    CharacterDemandCreate,
  },
  name: "jobitem-demand-container",

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
      randomInt: 0,
      ENTRANCE_CLASS: ENTRANCE_CLASS,
    }
  },

  computed: {},

  mounted: function () {
    let _this = this;
    console.log("jobitemType:", _this.jobitemId);
  },

  methods: {
    refreshDecomposeCreate(){
      let _this = this;
      _this.randomInt = Math.random();
    },
  },
}
</script>

<style lang="scss" scoped>
@import './../../../public/static/scss/caipd.scss';

</style>
