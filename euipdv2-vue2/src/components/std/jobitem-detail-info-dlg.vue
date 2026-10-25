<!--
 * Author: 周朔鹏
 * Date: 2024-01-14
 * Email: 391902958@qq.com
 * Website: https://www.euipd.com
-->

<template>
  <div style="height: calc(100vh - 241px); margin-top: -20px;">
    <div style="width: 26.3cm; height: 100%; overflow-y: scroll; margin: 0 auto;">
      <JobitemDetailInfoCore
          :key="jobitemId"
          :jobitem-id="jobitemId"
      ></JobitemDetailInfoCore>
    </div>
  </div>
</template>

<script>
import File from "../rdms/file.vue";
import JobitemDetailInfoCore from "@/components/std/jobitem-detail-info-core.vue";

export default {
  components: {
    File,
    JobitemDetailInfoCore,
  },
  name: "jobitem-detail-info-dlg",

  props: {
    jobitemId: {
      default: null,
    },
  },

  data() {
    return {

    };
  },

  computed: {

  },

  mounted: function () {
    let _this = this;

  },

  beforeDestroy(){
    let _this = this;

  },

  methods: {

  },
}
</script>

<style lang="scss" scoped>

</style>
