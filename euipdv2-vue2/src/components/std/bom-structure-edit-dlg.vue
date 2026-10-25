<!--
 * Author: 周朔鹏
 * Date: 2024-12-11
 * Email: 391902958@qq.com
 * Website: https://www.euipd.com
-->

<template>
  <div>
    <div class="dialog-container">
      <el-tree
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
      </el-tree>
      <div class="clearfix form-actions">
        <div style="display: flex; justify-content: center;">
          <button  type="button" v-on:click="saveBomTree()" class="ca-btn-primary">保 存</button>
          <button type="button" v-on:click="close()" class="ca-btn-primary">关 闭</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import Swal from "sweetalert2";

export default {
  components: {
  },
  name: "bom-structure-edit-dlg",

  props: {
    subprojectId: {
      default: null
    },
    closeDlg: {
      type: Function,
      default: null
    },
  },

  data: function () {
    return {
      loginUser: {},
      loginCustomer: {},

      bomTreeDto: {},
      bomStructure: [],

      bomTree: [{
        id: 1,
        label: '一级 1',
        children: [{
          id: 4,
          label: '二级 1-1',
          children: [{
            id: 9,
            label: '三级 1-1-1',
          }, {
            id: 10,
            label: '三级 1-1-2',
          }]
        }]
      }],
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
      _this.getBomTree(_this.subprojectId)
    },

    saveBomTree() {
      let _this = this;
      if(Tool.isEmpty_ES5(_this.bomTree)){
        return;
      }
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/material/saveBomTree', _this.bomTree).then((response)=>{
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
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/material/getBomTree/' + subprojectId).then((response)=>{
        let resp = response.data;
        if (resp.success) {
          _this.bomTreeDto = resp.content;
          _this.bomTree = _this.bomTreeDto.children;
        }else{
          Toast.error(resp.message, Swal);
        }
      })
    },

    close(){
      let _this = this;
      _this.closeDlg();
    },

    handleDragStart(node, ev) {
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
    },

  }
}
</script>

<style lang="scss" scoped>
@import '../../../public/static/scss/caipd';

.dialog-width{
  width: 100%;
}

</style>
