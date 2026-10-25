<!--
 * Author: 周朔鹏
 * Date: 2025-02-20
 * Email: 391902958@qq.com
 * Website: https://www.euipd.com
-->

<template>
  <div>
    <div style="width: 100%;">
      <div style="display: flex; flex-direction: row; justify-content: space-between; align-items: center;">
        <div>
          <p>特性清单</p>
        </div>
        <div>
          <el-button type="primary" plain size="small" v-on:click.stop="projectUpdateDlg()">
            版本升级
          </el-button>
        </div>
      </div>

      <div class="hr hr-8 dotted hr-double"></div>
      <div class="custom-tree-container">
        <div class="block">
          <el-tree style="background-color: rgba(187,215,248,0);"
              ref="characterTree"
              :data="data"
              :indent="6"
              :show-checkbox="false"
              node-key="id"
              :default-expand-all="true"
              :highlight-current="true"
              :check-on-click-node="true"
              :expand-on-click-node="false"
              :accordion =false
              @node-click="nodeClick">
              <span class="custom-tree-node" slot-scope="{ node, data }">
                <span>{{ ((node.label))}}</span>
                <span> <!--注释: deep 等于 -1 是对项目条目的表示, 不是真正的 deep 概念-->
                  <el-button
                      size="mini"
                  >查看</el-button>
                  <el-button
                      v-if=" !(data.deep === -1 || data.status === CHARACTER_STATUS.UPDATED.key || data.status === CHARACTER_STATUS.UPDATED_HISTORY.key || data.auxStatus === CHARACTER_STATUS.HISTORY.key || data.versionUpdate === CHARACTER_STATUS.UPDATED.key)"
                      size="mini"
                      @click.stop="iterationDlg(data)"
                  >迭代</el-button>
                  <el-button
                      v-if="data.deep === -1"
                      size="mini"
                      @click.stop=""
                  ><span style="color: #CCCCCC;">项目</span></el-button>
                  <el-button
                      v-if="data.deep !== -1 && (data.status === CHARACTER_STATUS.UPDATED.key || data.status === CHARACTER_STATUS.UPDATED_HISTORY.key || data.auxStatus === CHARACTER_STATUS.HISTORY.key || data.versionUpdate === CHARACTER_STATUS.UPDATED.key)"
                      size="mini"
                      @click.stop=""
                  ><span style="color: #CCCCCC;">迭代</span></el-button>
                </span>
              </span>
          </el-tree>
        </div>
      </div>
    </div>

    <!--特性迭代-->
    <el-dialog
        title="特性迭代"
        width="800px"
        :visible.sync="jobItemDlgVisible_iteration"
        :close-on-click-modal=false>
      <div>
        <CharacterUpdateDlg
            :key="characterItem.id"
            :character-id="characterItem.id"
            :close-function="closeDlg"
        ></CharacterUpdateDlg>
      </div>
    </el-dialog>

    <!--项目迭代-->
    <el-dialog
        title="项目迭代"
        width="23cm"
        :visible.sync="projectDlgVisible"
        :close-on-click-modal=false>
      <div>
        <ProjectUpdateDlg
            :key="((projectSimpleInfo.id) + '-' + (random))"
            :project-id="projectSimpleInfo.id"
            :close-function="closeDlg"
        ></ProjectUpdateDlg>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import Swal from "sweetalert2";
import CharacterUpdateDlg from "@/components/rdms/character-update-dlg.vue";
import ProjectUpdateDlg from "@/components/rdms/project-update-dlg.vue";

export default {
  components: {
    ProjectUpdateDlg,
    CharacterUpdateDlg,

  },
  name: "character-tree",

  props:{
    projectId: {
      default: null,
    },
    callbackFunction: {
      type: Function,
      default: null,
    }
  },

  data: function() {
    return {
      loginUser: {},

      characterTree: [],

      projectSimpleInfo: {}, //子项目信息
      characterInfo: {}, //子项目信息

      projectDlgVisible: false,
      jobItemDlgVisible_iteration: false,
      characterItem: {},

      CHARACTER_STATUS: CHARACTER_STATUS,
      random: 0,
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
    _this.random = Math.random();
    _this.getLoginUser();

    if(Tool.isNotEmpty(_this.projectId)){
      _this.getCharacterTreeByProjectId(_this.projectId);
    }

    _this.$bus.$on('refresh-character-tree', (data)=>{
      if(data){
        if(Tool.isNotEmpty(_this.projectId)){
          _this.getCharacterTreeByProjectId(_this.projectId);
        }
      }
    });
  },

  beforeDestroy() {
    let _this = this;
    _this.$bus.$off('refresh-character-tree');
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

    iterationDlg(data) {
      let _this = this;
      _this.characterItem = data;
      _this.jobItemDlgVisible_iteration = true;
    },

    closeDlg(){
      let _this = this;
      _this.jobItemDlgVisible_iteration = false;
      _this.projectDlgVisible = false;
    },

    nodeClick(data){
      let _this = this;
      if(data.deep === -1){  //树中对项目根节点的deep赋值为 -1
        _this.getProjectSimpleInfo(data.id);
      }else{
        _this.getCharacterRecordDetailById(data.id);
      }
    },

    getCharacterTreeByProjectId(projectId) {
      let _this = this;
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/character/getCharacterTreeByProjectId/' + projectId).then((response) => {
        let resp = response.data;
        if (resp.success) {
          _this.characterTree = resp.content;
          if(Tool.isNotEmpty_ES5(_this.characterTree) && _this.characterTree[0].deep === -1){
            _this.getProjectSimpleInfo(_this.characterTree[0].id);
          }
        }else{
          Toast.warning(resp.message, Swal);
        }
      });
    },

    getProjectSimpleInfo(projectId){
      let _this = this;
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/project/getProjectSimpleInfo/'+ projectId).then((response)=>{
        let resp = response.data;
        if (resp.success) {
          _this.projectSimpleInfo = resp.content;
          _this.callbackFunction(_this.projectSimpleInfo, DOC_TYPE.PROJECT.key);
        } else {
          Toast.warning(resp.message,Swal);
        }
      })
    },

    getCharacterRecordDetailById(characterId){
      let _this = this;
      let formData = new window.FormData();
      formData.append('characterId', characterId);
      formData.append('loginUserId', _this.loginUser.customerUserId);
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/character/getCharacterRecordDetailById', formData).then((response) => {
        let resp = response.data;
        if (resp.success) {
          _this.characterInfo = resp.content;
          _this.callbackFunction(_this.characterInfo, DOC_TYPE.CHARACTER.key);
        } else {
          Toast.warning(resp.message,Swal);
        }
      })
    },

    projectUpdateDlg(project){
      let _this = this;
      _this.random = Math.random();
      _this.projectItem = project;
      _this.projectDlgVisible = true;
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

</style>
