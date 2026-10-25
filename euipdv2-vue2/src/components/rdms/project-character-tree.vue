<!--
 * Author: 周朔鹏
 * Date: 2024-11-12
 * Email: 391902958@qq.com
 * Website: https://www.euipd.com
-->

<template>
  <div>
    <div class="tree-height">
      <div style="display: flex; flex-direction: row; justify-content: space-between; align-items: center;">
        <div style="margin-left: 5px;">
          <el-select v-model="currentBomConfigId" filterable placeholder="请选择产品配置" @change="getMaterialListById(currentBomConfigId)">
            <el-option v-for="o in bomConfigList" :key="o.id" :value="o.id" :label="o.name"></el-option>
          </el-select>
        </div>

        <div style="margin-right: 6px;">
          <el-tooltip  content="可以通过项目归并, 实现项目的分解开发, 最后实现所有功能的合并. 也可以对项目的部分进行迭代, 然后进行项目归并." placement="top" effect="light" :open-delay=1000>
            <div>
<!--              <el-button
                  type="primary"
                  plain
                  size="small" @click.stop="linkProject">
                合并项目
              </el-button>-->
              <el-button
                  type="primary"
                  size="small" @click.stop="getCheckedKeys">
                生成BOM
              </el-button>
            </div>

          </el-tooltip>
        </div>
      </div>
      <div style="margin: 4px 5px 0;"><small>产品配置：勾选纳入产品的特性，发布时按此配置生成发布基线。</small></div>
      <div class="hr hr-8 dotted hr-double"></div>
      <div class="custom-tree-container">
        <div class="block">
          <el-tree style="background-color: rgba(187,215,248,0);"
              ref="characterTree"
              :data="data"
              :indent="24"
              :show-checkbox="true"
              node-key="id"
              :default-expand-all="true"
              :highlight-current="true"
              :check-on-click-node="true"
              :expand-on-click-node="false"
              :accordion =false>
          </el-tree>
        </div>
      </div>
    </div>
    <div class="clearfix form-actions" >
      <div style="display: flex; justify-content: center;">
        <button v-on:click="updateBomConfig()" class="ca-btn-primary-sm-l" type="button" style="margin: 0 5px;">
          更新现有配置
        </button>
        <button v-on:click="createBomConfig()" class="ca-btn-primary-sm-l" type="button" style="margin: 0 5px;">
          保存为新配置
        </button>
      </div>
    </div>
    <!-- 合并项目-->
    <el-dialog
        :visible.sync="dialogVisible"
        width="20cm"
        :before-close="handleClose"
        :close-on-click-modal=false>
      <div slot="title" style="font-size: 20px;">{{ dialogTitle }}</div>
      <ProjectLinkDlg
          :key="projectId"
          :project-id="projectId"
          :close-function="closeDialog">
      </ProjectLinkDlg>
    </el-dialog>

    <!-- 创建配置 -->
    <el-dialog
        :visible.sync="createBomConfigVisible"
        width="20cm"
        :before-close="handleClose"
        :close-on-click-modal=false>
      <div slot="title" style="font-size: 20px;">{{ dialogTitle }}</div>
      <BomConfigCreateDlg
          :key="projectId"
          :project-id="projectId"
          :close-function="closeDialog">
      </BomConfigCreateDlg>
    </el-dialog>
  </div>
</template>

<script>
import Swal from "sweetalert2";
import CharacterUpdateDlg from "@/components/rdms/character-update-dlg.vue";
import ProjectLinkDlg from "@/components/rdms/project-link-dlg.vue";
import BomConfigCreateDlg from "@/components/rdms/bom-config-create-dlg.vue";

export default {
  components: {
    BomConfigCreateDlg,
    ProjectLinkDlg,
    CharacterUpdateDlg,

  },
  name: "project-character-tree",

  props:{
    projectId: {
      default: null,
    },
    getBomInfo: {
      type: Function,
      default: null,
    }
  },

  data: function() {
    return {
      loginUser: {},

      characterTree: [],
      characterIdTreeList: [], //初始化树
      characterIds: [],
      bomMaterialList: [],
      bomConfigList: [],
      currentBomConfigId: '',

      characterInfo: {}, //子项目信息
      activeBomConfig: {},

      dialogVisible: false,
      createBomConfigVisible: false,
      dialogTitle: 'Dialog',

    }
  },

  computed: {
    //使用计算属性实现传入数据的动态响应
    data: function(){
      return  this.characterTree;
    },

  },

  mounted: function() {
    let _this = this;
    _this.getLoginUser();

    if(Tool.isNotEmpty(_this.projectId)){
      _this.getCharacterTreeByProjectIdWithLink(_this.projectId);
      _this.getMaterialListByProjectId(_this.projectId);
    }

    _this.$bus.$on('refresh-project-character-tree', (data)=>{
      if(data){
        if(Tool.isNotEmpty(_this.projectId)){
          _this.getCharacterTreeByProjectIdWithLink(_this.projectId);
        }
      }
    });

    _this.$bus.$on('save-new-bom-config-item', (data)=>{
          _this.saveBomConfig(data);
    });

  },

  beforeDestroy() {
    let _this = this;
    _this.$bus.$off('refresh-project-character-tree');
    _this.$bus.$off('save-new-bom-config-item');
  },

  methods: {
    getLoginUser(){
      let _this = this;
      if(Tool.getLoginUser().userGroup === LOGIN_USER_GROUP.CUSTOMER.key){
        _this.loginUser = Tool.getLoginUser().adminUser;
        _this.loginCustomer =Tool.getLoginUser();
      }else{
        _this.loginUser = Tool.getLoginUser();
        _this.loginCustomer = Tool.getLoginUser().customer;
      }
    },


    createBomConfig() {
      let _this = this;
      _this.createBomConfigVisible = true;
      _this.dialogTitle = "创建产品配置";
    },

    linkProject() {
      let _this = this;
      _this.dialogVisible = true;
      _this.dialogTitle = "合并项目";
    },

    handleClose(done) {
      done();
    },

    closeDialog() {
      let _this = this;
      _this.dialogVisible = false;
      _this.createBomConfigVisible = false;
    },

    getCheckedKeys() {
      let _this = this;
      // console.log(_this.$refs.characterTree.getCheckedKeys());
      const selectedNodeList = _this.$refs.characterTree.getCheckedNodes();
      const selectedCharacterList = selectedNodeList.filter(item => item.deep !== -1);  //排除选中的项目
      _this.characterIds = selectedCharacterList.map(character => character.id);
      _this.getBomMaterialListByCharacterIdList(_this.characterIds);
    },

    getBomMaterialListByCharacterIdList(characterIdList) {
      let _this = this;
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/material/getBomMaterialListByCharacterIdList', characterIdList).then((response) => {
        let resp = response.data;
        if (resp.success) {
          _this.bomMaterialList = resp.content;
          _this.activeBomConfig.bomMaterialList = _this.bomMaterialList;
          _this.getBomInfo(_this.activeBomConfig);
        } else {
          Toast.warning(resp.message, Swal);
        }
      });
    },

    getCharacterTreeByProjectIdWithLink(projectId) {
      let _this = this;
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/project/getCharacterTreeByProjectIdWithLink/' + projectId).then((response) => {
        let resp = response.data;
        if (resp.success) {
          _this.characterTree = resp.content;
        }else{
          Toast.warning(resp.message, Swal);
        }
      });
    },

    getMaterialListByProjectId(projectId) {
      let _this = this;
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/material/getMaterialListByProjectId/' + projectId).then((response) => {
        let resp = response.data;
        if (resp.success) {
          _this.activeBomConfig= resp.content;
          _this.currentBomConfigId = _this.activeBomConfig.id;
          _this.bomMaterialList = resp.content.materialList;
          _this.characterIdTreeList = resp.content.characterIdList;
          _this.$refs.characterTree.setCheckedKeys(_this.characterIdTreeList);
          _this.activeBomConfig.bomMaterialList = _this.bomMaterialList;
          _this.getBomInfo(_this.activeBomConfig);
          _this.getBomConfigListByProjectId(_this.projectId);
        }else{
          Toast.warning(resp.message, Swal);
        }
      });
    },

    getBomConfigListByProjectId(projectId) {
      let _this = this;
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/project/getBomConfigListByProjectId/' + projectId).then((response) => {
        let resp = response.data;
        if (resp.success) {
          _this.bomConfigList= resp.content;
        }else{
          Toast.warning(resp.message, Swal);
        }
      });
    },

    getMaterialListById(bomConfigId){
      let _this = this;
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/material/getMaterialListById/' + bomConfigId).then((response) => {
        let resp = response.data;
        if (resp.success) {
          _this.activeBomConfig= resp.content;
          _this.bomMaterialList = resp.content.materialList;
          _this.characterIdTreeList = resp.content.characterIdList;
          _this.$refs.characterTree.setCheckedKeys(_this.characterIdTreeList);
          _this.activeBomConfig.bomMaterialList = _this.bomMaterialList;
          _this.getBomInfo(_this.activeBomConfig);
        }else{
          Toast.warning(resp.message, Swal);
        }
      });
    },

    saveBomConfig(bomConfig){
      let _this = this;
      const selectedNodeList = _this.$refs.characterTree.getCheckedNodes();
      const selectedCharacterList = selectedNodeList.filter(item => item.deep !== -1);  //排除选中的项目
      _this.characterIds = selectedCharacterList.map(character => character.id);

      bomConfig.projectId = _this.projectId;
      bomConfig.characterIdList = _this.characterIds;
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/project/saveBomConfig',  bomConfig).then((response) => {
        let resp = response.data;
        if (resp.success) {
          _this.getMaterialListByProjectId(_this.projectId);
          Toast.success("保存成功", Swal);
        }else{
          Toast.warning(resp.message, Swal);
        }
      });
    },

    updateBomConfig(){
      let _this = this;
      const selectedNodeList = _this.$refs.characterTree.getCheckedNodes();
      const selectedCharacterList = selectedNodeList.filter(item => item.deep !== -1);  //排除选中的项目
      _this.characterIds = selectedCharacterList.map(character => character.id);

      _this.activeBomConfig.projectId = _this.projectId;
      _this.activeBomConfig.characterIdList = _this.characterIds;
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/project/saveBomConfig',  _this.activeBomConfig).then((response) => {
        let resp = response.data;
        if (resp.success) {
          _this.getMaterialListByProjectId(_this.projectId);
          Toast.success("保存成功", Swal);
        }else{
          Toast.warning(resp.message, Swal);
        }
      });
    },

  }
}
</script>
<style lang="scss">
.el-tree-node__content {
  display: -webkit-box;
  display: -ms-flexbox;
  display: flex;
  -webkit-box-align: center;
  -ms-flex-align: center;
  align-items: center;
  height: 30px;
  cursor: pointer;
}
</style>

<style lang="scss" scoped>
.custom-tree-node {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-size: 14px;
  padding-right: 8px;
}
.tree-height{
  display: block;
  width: 100%;
  height: calc(100vh - 276px);
}

</style>
