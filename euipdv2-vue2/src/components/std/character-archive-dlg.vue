<!--
 * Author: 周朔鹏
 * Date: 2024-04-26
 * Email: 391902958@qq.com
 * Website: https://www.euipd.com
-->

<template>
  <div style="height: 100%;">
    <div class="paper-height">
      <div style="height: 100%; overflow: scroll;">
        <div style="margin: 0 80px">
          <CharacterDocTabs
              :key="characterId"
              :character-id="characterId"
          ></CharacterDocTabs>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import CharacterDocTabs from "@/components/rdms/character-doc-tabs.vue";

export default {
  components: {
    CharacterDocTabs,
  },
  name: "character-archive-dlg",

  props: {
    characterId: {
      default: null,
    },
  },

  data() {
    return {

    };
  },

  computed: {},

  mounted: function () {

  },

  beforeDestroy() {
  },

  methods: {

  },
}
</script>

<style lang="scss" scoped>
@import './../../../public/static/scss/caipd';

.paper-height {
  height: calc(100vh - 325px);
}

</style>
