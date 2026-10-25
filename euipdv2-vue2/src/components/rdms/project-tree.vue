<!--
 * Author: 周朔鹏
 * Date: 2025-02-26
 * Email: 391902958@qq.com
 * Website: https://www.euipd.com
-->

<template>
  <div>
    <div style="width: 100%;">
      <p>项目管理</p>
      <div class="hr hr-8 dotted hr-double"></div>
      <div style="width: 100%;">
        <div style="display: block;">
          <el-tree
              ref="tree"
              :data="data"
              :indent="10"
              :show-checkbox="false"
              node-key="id"
              :default-expand-all="true"
              :highlight-current="true"
              :check-on-click-node="true"
              :expand-on-click-node="false"
              :accordion =false
              @node-click="nodeClick">
              <span class="custom-tree-node" slot-scope="{ node, data }">
                <span class="scroll-container">{{ ((node.label) + ' (' + (data.obj.code) + ')')}}</span>
                <span style="margin-left: 10px;">
                  <el-tooltip content="为本项目添加子项目。比如进一步分解为机械、电器、软件、硬件等子项目" placement="top" effect="light" :open-delay=1000>
                    <div v-if="data.obj.status !== PROJECT_STATUS.REVIEW_SUBP.key
                                  && data.obj.status !== PROJECT_STATUS.COMPLETE.key
                                  && data.obj.status !== PROJECT_STATUS.DEV_COMPLETE.key
                                  && data.obj.status !== PROJECT_STATUS.ARCHIVED.key">
                      <el-button  v-if="data.id === subProjectId"
                                  size="mini"
                                  type="primary" plain
                                  @click.stop="() => addSubProject()">
                        添加
                      </el-button>
                    </div>
                    <div v-else style="width: 56px; display: flex; justify-content: center; font-size: 12px;">
                      <div v-if="data.id === subProjectId">
                        {{ PROJECT_STATUS | optionKV(data.obj.status) }}
                      </div>
                    </div>
                  </el-tooltip>
                  <el-tooltip content="对本子项目信息进行编辑" placement="top" effect="light" :open-delay=1000>
                    <div v-if="data.obj.status !== PROJECT_STATUS.REVIEW_SUBP.key
                                  && data.obj.status !== PROJECT_STATUS.COMPLETE.key
                                  && data.obj.status !== PROJECT_STATUS.DEV_COMPLETE.key
                                  && data.obj.status !== PROJECT_STATUS.ARCHIVED.key">
                      <el-button v-if="data.id !== subProjectId && data.obj.creatorId === loginUser.customerUserId"
                                 size="mini"
                                 plain
                                 @click.stop="() => editSubProject(data)">
                        编辑
                      </el-button>
                    </div>
                    <div v-else style="width: 56px; display: flex; justify-content: center; font-size: 12px;">
                      <div  v-if="data.id !== subProjectId">
                        {{ PROJECT_STATUS | optionKV(data.obj.status) }}
                      </div>
                    </div>
                  </el-tooltip>
                </span>
              </span>
          </el-tree>
        </div>
      </div>
    </div>

    <!--添加子项目-->
    <el-dialog
        title="添加子项目"
        width="29cm"
        :visible.sync="createSubProjectDlg"
        :before-close="handleClose"
        :close-on-click-modal=false>
      <div>
        <SubprojectCreateDlg
            :key="subProjectId"
            :sub-project-id="subProjectId"
            :project-type="projectType"
            :close-dialog="closeDialog"
        ></SubprojectCreateDlg>
      </div>
    </el-dialog>

    <!--添加模块开发子项目-->
    <el-dialog
        title="添加模块开发子项目"
        width="29cm"
        :visible.sync="createModuleSubProjectDlg"
        :before-close="handleClose"
        :close-on-click-modal=false>
      <div>
        <SubprojectCreateModuleDlg
            :key="subProjectId"
            :sub-project-id="subProjectId"
            :project-type="projectType"
            :close-dialog="closeDialog"
        ></SubprojectCreateModuleDlg>
      </div>
    </el-dialog>

    <!--编辑子项目对话框-->
    <el-dialog
        title="编辑子项目"
        width="29cm"
        :visible.sync="editSubProjectDlg"
        :before-close="handleClose"
        :close-on-click-modal=false>
      <div>
        <SubprojectEditDlg
            :key="subProjectId_edit"
            :sub-project-id="subProjectId_edit"
            :project-type="projectType_edit"
            :close-dialog="closeDialog"
        ></SubprojectEditDlg>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import Swal from "sweetalert2";
import SubprojectCreateDlg from "@/components/rdms/subproject-create-dlg.vue";
import SubprojectEditDlg from "@/components/rdms/subproject-edit-dlg.vue";
import SubprojectCreateModuleDlg from "@/components/rdms/subproject-create-module-dlg.vue";

export default {
  components: {
    SubprojectCreateDlg,
    SubprojectEditDlg,
    SubprojectCreateModuleDlg,
  },
  name: "project-tree",

  props:{
    subProjectId: {
      default: null,
    },
    projectType: {
      default: null,
    },
    refreshCharacterList: {
      type: Function,
      default: null,
    }
  },

  data: function() {
    return {
      loginUser: {},
      loginCustomer: {},

      characterList: [],

      projectTree: [],

      createSubProjectDlg: false,
      createModuleSubProjectDlg: false,
      editSubProjectDlg: false,

      PROJECT_TYPE: PROJECT_TYPE,
      PROJECT_STATUS: PROJECT_STATUS,

      subProject: {}, //子项目信息

      callBackData: {
        subprojectId: '',
        projectType: '',
        characterList: [],
      },

      subProjectId_edit: '',
      projectType_edit: '',
    }
  },

  computed: {
    //使用计算属性实现传入数据的动态响应
    data: function(){
      return  this.projectTree;
    },

  },

  mounted: function() {
    let _this = this;
    _this.getLoginUser();

    if(Tool.isNotEmpty(_this.subProjectId)){
      _this.getSubProjectTree(_this.subProjectId);
      _this.getSubProjectInfo(_this.subProjectId);
    }

    _this.$bus.$on('refresh-project-tree', (data)=>{
      if(data){
        if(Tool.isNotEmpty(_this.subProjectId)){
          _this.getSubProjectTree(_this.subProjectId);
        }
      }
    });
  },

  beforeDestroy() {
    let _this = this;
    _this.$bus.$off('refresh-project-tree');
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

    getSubProjectInfo(subProjectId) {
      let _this = this;
      let formData = new window.FormData();
      formData.append('subProjectId', subProjectId);
      formData.append('loginUserId', _this.loginUser.customerUserId);
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/sub-project/getSubProjectInfo', formData).then((response)=>{
        let resp = response.data;
        if (resp.success) {
          _this.subProject = resp.content;
        } else {
          Toast.warning(resp.message,Swal);
        }
      })
    },

    getSubProjectTree(subProjectId) {
      // debugger
      let _this = this;
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/sub-project/getSubProjectTree/'+ subProjectId).then((response)=>{
        let resp = response.data;
        if (resp.success) {
          _this.projectTree = resp.content;
          _this.getSetupedCharacterListBySubProjectId(subProjectId);
        } else {
          Toast.warning(resp.message,Swal);
        }
      })
    },


    getSetupedCharacterListBySubProjectId(subprojectId){
      let _this = this;
      let formData = new window.FormData();
      formData.append('subprojectId', subprojectId);
      formData.append('loginUserId', _this.loginUser.customerUserId);
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/character/getSetupedCharacterListBySubProjectId', formData).then((response) => {
        let resp = response.data;
        if (resp.success) {
          _this.characterList = resp.content;
          _this.callBackData.subprojectId = subprojectId;
          _this.callBackData.projectType = PROJECT_TYPE.DEV_PROJECT.key;
          _this.callBackData.characterList = _this.characterList;
          _this.refreshCharacterList(_this.callBackData);
        } else {
          Toast.warning(resp.message,Swal);
        }
      })
    },

    nodeClick(data){
      let _this = this;
      _this.getSetupedCharacterListBySubProjectId(data.id);
    },

    addSubProject(){
      let _this = this;
      if(_this.subProject.rdType !== RD_TYPE.MODULE.key){
        _this.createSubProjectDlg = true;
      }else{
        _this.createModuleSubProjectDlg = true;
      }
    },

    editSubProject(data){
      let _this = this;
      console.log("data:",data);
      _this.editSubProjectDlg = true;
      _this.subProjectId_edit = data.id;
      _this.projectType_edit = PROJECT_TYPE.SUB_PROJECT.key;
    },

    closeDialog() {
      let _this = this;
      _this.createSubProjectDlg = false;
      _this.createModuleSubProjectDlg = false;
      _this.editSubProjectDlg = false;
      _this.getSubProjectTree(_this.subProjectId);
    },

    handleClose(done) {
      done(); //el-dialog用于关闭对话框的内置函数.
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
.scroll-container {
  width: 300px; /* 设置容器宽度 */
  overflow: auto; /* 允许滚动 */
  scrollbar-width: none; /* Firefox */
  -ms-overflow-style: none; /* IE 10+ */
}

.scroll-container::-webkit-scrollbar {
  display: none; /* Chrome, Safari, Edge */
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
