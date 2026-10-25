<!--
 * Author: 周朔鹏
 * Date: 2025-04-15
 * Email: 391902958@qq.com
 * Website: https://www.euipd.com
-->

<template>
  <div class="content-wrapper">
    <div>
      <el-row>
        <el-col :span="7">
          <ProjectTree
              :key="((subProjectId) + '-' + (random))"
              :sub-project-id="subProjectId"
              :project-type="projectType"
              :refresh-character-list="refreshCharacterList"
          ></ProjectTree>
        </el-col>
        <el-col :span="17">
          <ProjectInfoOutline
              :key="currentProjectId"
              :x-project-id="currentProjectId"
              :project-type="currentProjectType"
              :entrance="entrance"
          ></ProjectInfoOutline>
        </el-col>
      </el-row>
    </div>
    <div class="hr hr-8 dotted hr-double"></div>
    <div>
      <ProjectCharacterHandle
          :key="currentProjectId"
          :sub-project-id="currentProjectId"
          :project-type="currentProjectType"
          :index="index"
          :entrance="entrance"
      ></ProjectCharacterHandle>
    </div>
  </div>
</template>

<script>
import Swal from "sweetalert2";
import ProjectTree from "@/components/rdms/project-tree.vue";
import ProjectInfoOutline from "@/components/rdms/project-info-outline.vue";
import ProjectCharacterHandle from "@/components/rdms/project-character-handle.vue";

export default {
  components: {
    ProjectCharacterHandle,
    ProjectTree,
    ProjectInfoOutline,
    File,
  },
  name: "project-subproject",

  props:{
    subProjectId: {
      default: null,
    },
    index: {
      type: Number,
      default: 0,
    },
    projectType: {
      default: null,
    },
    entrance: {
      default: null,
    },
  },

  data: function() {
    return {
      currentProjectId: '',
      currentProjectType: '',
      random: 0,
    }
  },

  mounted: function() {
    let _this = this;

    _this.$bus.$on('refresh-project-subproject', (data)=>{
      if(data){
        _this.random = Math.random();
      }
    });
  },

  beforeDestroy(){
    let _this = this;
    _this.$bus.$off('refresh-project-subproject');
  },

  methods: {
    refreshCharacterList(data){
      let _this = this;
      _this.currentProjectId = data.subprojectId;
      _this.currentProjectType = data.projectType;
    },
  }
}
</script>

<style lang="scss" scoped>
.container {
  width: 100%;
  //height: 100%;
  display: -webkit-box; /* 老版本语法: Safari, iOS, Android browser, older WebKit browsers. */
  display: -moz-box; /* 老版本语法: Firefox (buggy) */
  display: -ms-flexbox; /* 混合版本语法: IE 10 */
  display: -webkit-flex; /* 新版本语法: Chrome 21+ */
  display: flex; /* 新版本语法: Opera 12.1, Firefox 22+ */
  .left {
    min-width: 400px;
    //height: 100%;
  }
  .right {
    //height: 100%;
    -webkit-flex: 1; /* Chrome */
    -ms-flex: 1; /* IE 10 */
    flex: 1; /* NEW, Spec - Opera 12.1, Firefox 20+ */
    -webkit-box-flex: 1; /* OLD - iOS 6-, Safari 3.1-6 */
    -moz-box-flex: 1; /* OLD - Firefox 19- */
  }
}

</style>
