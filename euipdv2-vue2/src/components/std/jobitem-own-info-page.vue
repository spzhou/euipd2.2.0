<!--
 * Author: 周朔鹏
 * Date: 2024-01-01
 * Email: 391902958@qq.com
 * Website: https://www.euipd.com
-->

<template>
  <div style="height: 100%;">
    <div class="paper-height">
      <div style="height: 100%; overflow: scroll;">
        <div class="a4-paper-big-simple">
          <div style="margin: 50px 50px">
            <JobitemOwnInfoPlain
                :key="jobitemId"
                :jobitem-id="jobitemId"
            ></JobitemOwnInfoPlain>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import JobitemOwnInfoPlain from "@/components/std/jobitem-own-info-plain.vue";

export default {
  components: {
    JobitemOwnInfoPlain,

  },
  name: "jobitem-own-info-page",

  props: {
    jobitemId: {
      default: null,
    },
  },

  data: function () {
    return {

    }
  },

  computed: {
  },

  mounted: function () {
    let _this = this;
  },

  methods: {
  }
}
</script>

<style lang="scss" scoped>
@import './../../../public/static/scss/caipd.scss';
.paper-height {
  height: calc(100vh - 200px);
}
</style>
