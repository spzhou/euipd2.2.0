<!--
 * Author: 周朔鹏
 * Date: 2024-04-26
 * Email: 391902958@qq.com
 * Website: https://www.euipd.com
-->

<template>
  <div class="page-container">
    <div class="page-left">
      <ProjectCharacterDocMenu
          :key="projectId"
          :project-id="projectId"
          :refresh-detail-page="refreshDetailPage"
      ></ProjectCharacterDocMenu>
    </div>
    <div class="page-right">
      <div v-if="character && docType === DOC_TYPE.CHARACTER.key">
        <CharacterDocTabs
            :key="character.id"
            :character-id="character.id"
        ></CharacterDocTabs>
      </div>
      <div v-if="docType === DOC_TYPE.PROJECT.key">
        <SubprojectSummaryPage
            :key="projectInfo.id"
            :subproject-id="projectInfo.id"
        ></SubprojectSummaryPage>
      </div>
    </div>
  </div>
</template>

<script>
import ProjectCharacterDocMenu from "@/components/rdms/project-character-doc-menu.vue";
import CharacterDocTabs from "@/components/rdms/character-doc-tabs.vue";
import SubprojectSummaryPage from "@/components/std/subproject-summary-page.vue";

export default {
  components: {
    ProjectCharacterDocMenu,
    CharacterDocTabs,
    SubprojectSummaryPage,
  },
  name: "project-character-doc-container",

  props: {
    projectId: {
      default: null,
    },
    index: {
      default: null,
    },
    projectType: {
      default: null,
    },
    entrance: {
      default: null,
    },
  },

  data: function () {
    return {
      character: {
        id: '',
      },
      projectInfo: {
        id: '',
      },
      docType: '',
      DOC_TYPE: DOC_TYPE,
      PROJECT_TYPE: PROJECT_TYPE,
    }
  },

  computed: {
  },

  mounted: function () {

  },

  methods: {
    refreshDetailPage(data, docType){
      let _this = this;
      if(Tool.isNotEmpty_ES5(data) && Tool.isNotEmpty(docType)){
        if(docType === DOC_TYPE.CHARACTER.key){
          _this.character = data;
          _this.docType = docType;
        }
        if(docType === DOC_TYPE.PROJECT.key){
          _this.projectInfo = data;
          _this.docType = docType;
        }
      }
    }
  },
}
</script>

<style lang="scss" scoped>
@import './../../../public/static/scss/caipd.scss';

</style>
