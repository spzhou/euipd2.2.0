<!--
 * Author: 周朔鹏
 * Date: 2024-07-02
 * Email: 391902958@qq.com
 * Website: https://www.euipd.com
-->

<template>
  <div>
    <div class="a4-paper-big">
      <div style="display: flex; flex-direction: column;">
        <div>
          <MixProjectInfoSummary
              :key="xProjectId"
              :x-project-id="xProjectId"
              :project-type="projectType"
              :index="index"
              :setup-stage="setupStage"
              :entrance="entrance"
          ></MixProjectInfoSummary>
        </div>
        <div class="hr hr-10 dotted"></div>
        <div style="margin: 0 10px;">
          研发特性清单
          <div class="hr hr-8 dotted hr-double"></div>
          <div>
            <ProjectCharacterList
                :key="xProjectId"
                :x-project-id="xProjectId"
                :project-type="projectType"
                :setup-stage="setupStage"
            ></ProjectCharacterList>
          </div>
        </div>
        <div class="hr hr-10 dotted"></div>
        <div style="margin: 0 10px;">
          集成特性清单
          <div class="hr hr-8 dotted hr-double"></div>
          <div>
            <ProjectCbbList
                :key="xProjectId"
                :project-id="xProjectId"
            ></ProjectCbbList>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import ProjectTree from "@/components/rdms/project-tree.vue";
import ProjectCharacterList from "@/components/rdms/project-character-list.vue";
import MixProjectInfoSummary from "@/components/rdms/mix-project-info-summary.vue";
import ProjectCbbList from "@/components/rdms/project-cbb-list.vue";

export default {
  components: {
    ProjectCharacterList,
    ProjectTree,
    MixProjectInfoSummary,
    File,
    ProjectCbbList,
  },
  name: "project-detail-info",

  props:{
    xProjectId: {
      default: null,
    },
    projectType: {
      default: null,
    },
    setupStage: {
      default: SETUP_STAGE.SETUPED.key,
    },
    index: {
      default: null,
    },
    entrance: {
      default: null,
    }
  },

  data: function() {
    return {
    }
  },

  mounted: function() {
  },

  methods: {
  }
}
</script>

<style lang="scss" scoped>
.container {
  width: 100%;
  height: 100%;
  display: -webkit-box; /* 老版本语法: Safari, iOS, Android browser, older WebKit browsers. */
  display: -moz-box; /* 老版本语法: Firefox (buggy) */
  display: -ms-flexbox; /* 混合版本语法: IE 10 */
  display: -webkit-flex; /* 新版本语法: Chrome 21+ */
  display: flex; /* 新版本语法: Opera 12.1, Firefox 22+ */
}

</style>
