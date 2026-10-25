<!--
 * Author: 周朔鹏
 * Date: 2024-03-20
 * Email: 391902958@qq.com
 * Website: https://www.euipd.com
-->

<template>
  <div class="page-container">
    <div class="page-left">
      <ProjectCharacterTree
          ref="characterTreeList"
          :key="projectId"
          :project-id="projectId"
          :get-bom-info="getBomListFun"
      ></ProjectCharacterTree>
    </div>
    <div class="page-right">
      <div class="a4-paper-big-simple">
        <div style="margin: 50px 60px; min-height: 500px;">
          <BomListShow
              :key="((projectId) + '-' + (random))"
              :bom-material-list="bomMaterialList"
              :project-id="projectId"
              :title="activeBomConfig.name"
          ></BomListShow>
        </div>
      </div>
    </div>

  </div>
</template>

<script>
import BomListShow from "@/components/std/bom-list-show.vue";
import ProjectCharacterTree from "@/components/rdms/project-character-tree.vue";
import BomConfigCreateDlg from "@/components/rdms/bom-config-create-dlg.vue";

export default {
  components: {
    BomConfigCreateDlg,
    ProjectCharacterTree,
    BomListShow,
  },
  name: "project-config-container",

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

      bomMaterialList: [],
      random: 0,
      activeBomConfig: {},
    }
  },

  computed: {
  },

  mounted: function () {

  },

  methods: {
    getBomListFun(bomInfo){
      let _this = this;
      _this.random = Math.random();
      _this.activeBomConfig = bomInfo;
      _this.bomMaterialList = bomInfo.bomMaterialList;
    },

  },
}
</script>

<style lang="scss" scoped>
@import './../../../public/static/scss/caipd.scss';

</style>
