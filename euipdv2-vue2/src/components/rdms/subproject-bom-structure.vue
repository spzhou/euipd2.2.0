<!--
 * Author: 周朔鹏
 * Date: 2025-02-20
 * Email: 391902958@qq.com
 * Website: https://www.euipd.com
-->

<template>
  <div>
    <div class="row">
      <div style="margin: 0 15px;">
        <p v-if="!isDlg">BOM结构化编辑: 是为<strong>项目经理</strong>提供的进行BOM结构化编辑的界面.</p>
        <div v-if="!isDlg" class="hr hr-18 dotted hr-double"></div>
        <div v-if="isDlg" class="hr hr-8 dotted"></div>
        <div>
<!--          <el-tree
              :data="bomTree"
              node-key="id"
              default-expand-all
              @node-drag-start="handleDragStart"
              @node-drag-enter="handleDragEnter"
              @node-drag-leave="handleDragLeave"
              @node-drag-over="handleDragOver"
              @node-drag-end="handleDragEnd"
              @node-drop="handleDrop"
              draggable>
          </el-tree>-->
          <el-tree
              :data="bomTree"
              node-key="id"
              default-expand-all
              draggable>
          </el-tree>
        </div>
        <div class="hr hr-12 hr-dotted hr-double"></div>
        <div style="display: flex; justify-content: center;">
<!--          <button  type="button" v-on:click="saveBomTree()" class="ca-btn-primary">保 存</button>-->
<!--          将子项目的bom同步到主项目bom中来-->
<!--          <button  type="button" v-on:click="synchronizationBomTree()" class="ca-btn-primary">同步bomTree</button>-->
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import Swal from "sweetalert2";
import { Loading } from 'element-ui';

export default {
  components: {

  },
  name: "subproject-bom-structure",

  props: {
    subprojectId: {
      default: null,
    },
    isMainProject: {
      default: false,
    },
    isDlg: {
      default: false,
    },

  },

  data: function () {
    return {
      loginUser: {},
      loginCustomer: {},

      bomTreeDto: {},
      bomStructure: [],

      bomTree: [],
      defaultProps: {
        children: 'children',
        label: 'label',
      }

    }
  },
  mounted: function () {
    let _this = this;
    _this.getLoginUser();

  },

  beforeDestroy: function () {
    let _this = this;
  },

  methods: {
    getLoginUser() {
      let _this = this;
      if (Tool.getLoginUser().userGroup === LOGIN_USER_GROUP.CUSTOMER.key) {
        _this.loginUser = Tool.getLoginUser().adminUser;
        _this.loginCustomer = Tool.getLoginUser();
      } else {
        _this.loginUser = Tool.getLoginUser();
        _this.loginCustomer = Tool.getLoginUser().customer;
      }

      if(_this.isMainProject){
        _this.synchronizationBomTree();
      }else{
        _this.getBomTree(_this.subprojectId);
      }

    },

    saveBomTree() {
      let _this = this;
      if(Tool.isEmpty_ES5(_this.bomTree)){
        return;
      }
      let requestPath = '';
      if(_this.isMainProject){
        requestPath = '/customer/material/saveBomTree_main'
      }else{
        requestPath = '/customer/material/saveBomTree'
      }

      _this.$ajax.post(process.env.VUE_APP_SERVER + requestPath, _this.bomTree).then((response)=>{
        let resp = response.data;
        if (resp.success) {
          Toast.success("保存成功", Swal);
        }else{
          Toast.error(resp.message, Swal);
        }
      })
    },

    getBomTree(subprojectId) {
      let _this = this;
      let loadingInstance = Loading.service({ fullscreen: true });
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/material/getBomTree/' + subprojectId).then((response)=>{
        let resp = response.data;
        loadingInstance.close();
        if (resp.success) {
          _this.bomTreeDto = resp.content;
          _this.bomTree = _this.bomTreeDto.children;
        }else{
          Toast.error(resp.message, Swal);
        }
      })
    },

    getBomTree_main(projectId) {
      let _this = this;
      let loadingInstance = Loading.service({ fullscreen: true });
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/material/getBomTree_main/' + projectId).then((response)=>{
        let resp = response.data;
        loadingInstance.close();
        if (resp.success) {
          _this.bomTreeDto = resp.content;
          _this.bomTree = _this.bomTreeDto.children;
        }else{
          Toast.error(resp.message, Swal);
        }
      })
    },

    synchronizationBomTree() {
      let _this = this;
      let loadingInstance = Loading.service({ fullscreen: true });
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/material/synchronizationBomTree/' + _this.subprojectId).then((response)=>{
        let resp = response.data;
        loadingInstance.close();
        if (resp.success) {
          _this.getBomTree_main(_this.subprojectId);
        }else{
          Toast.error(resp.message, Swal);
        }
      })
    },


/*    handleDragStart(node, ev) {
      console.log('drag start', node);
    },
    handleDragEnter(draggingNode, dropNode, ev) {
      console.log('tree drag enter: ', dropNode.label);
    },
    handleDragLeave(draggingNode, dropNode, ev) {
      console.log('tree drag leave: ', dropNode.label);
    },
    handleDragOver(draggingNode, dropNode, ev) {
      console.log('tree drag over: ', dropNode.label);
    },
    handleDragEnd(draggingNode, dropNode, dropType, ev) {
      console.log('tree drag end: ', dropNode && dropNode.label, dropType);
    },
    handleDrop(draggingNode, dropNode, dropType, ev) {
      console.log('tree drop: ', dropNode.label, dropType);
    },*/

  }
}
</script>

<style lang="scss">
@import './../../../public/static/scss/caipd.scss';

</style>
