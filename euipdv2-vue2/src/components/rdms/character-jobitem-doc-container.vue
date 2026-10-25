<!--
 * Author: 周朔鹏
 * Date: 2025-02-20
 * Email: 391902958@qq.com
 * Website: https://www.euipd.com
-->

<template>
  <div class="page-container">
    <div class="page-left">
      <CharacterUpdateDocMenu
          :key="preProjectId"
          :pre-project-id="preProjectId"
          :refresh-detail-page="refreshDetailPage"
      ></CharacterUpdateDocMenu>
    </div>
    <div class="page-right">
      <div v-if="character && character.parent && docType === DOC_TYPE.CHARACTER.key">
        <CharacterDocTabs
            :key="character.parent"
            :character-id="character.parent"
        ></CharacterDocTabs>
      </div>
    </div>
  </div>
</template>

<script>
import CharacterUpdateDocMenu from "@/components/rdms/character-update-doc-menu.vue";
import CharacterDocTabs from "@/components/rdms/character-doc-tabs.vue";

export default {
  components: {
    CharacterDocTabs,
    CharacterUpdateDocMenu,
  },
  name: "character-jobitem-doc-container",

  props: {
    preProjectId: {
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
      character: {
        id: '',
      },
      docType: '',
      DOC_TYPE: DOC_TYPE,
      ENTRANCE_CLASS: ENTRANCE_CLASS,
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
      }
    }
  },
}
</script>

<style lang="scss" scoped>
@import './../../../public/static/scss/caipd.scss';
</style>
