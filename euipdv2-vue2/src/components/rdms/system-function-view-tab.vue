<!--
 * Author: 周朔鹏
 * Date: 2025-02-23
 * Email: 391902958@qq.com
 * Website: https://www.euipd.com
-->

<template>
  <div>
    <el-tree
        ref="tree"
        :data="functionTree"
        node-key="id"
        default-expand-all
        :props="defaultProps"
        :render-content="renderContent"
    ></el-tree>
    <div class="hr hr-10 hr-dotted"></div>

    <!--显示特性定义表-->
    <el-dialog
        :visible.sync="showCharacterDlg"
        width="32cm"
        :before-close="handleClose"
        :close-on-click-modal=false>
      <div slot="title" style="font-size: 20px;">{{ dialogTitle }}</div>
      <div v-if="showCharacterDlg && characterItemId">
        <CharacterDocTabs
            :key="characterItemId"
            :character-id="characterItemId"
        ></CharacterDocTabs>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import Swal from "sweetalert2";
import {Loading} from "element-ui";
import CharacterDocTabs from "@/components/rdms/character-doc-tabs.vue";

export default {
  components: {
    CharacterDocTabs,
  },
  name: "system-function-view-tab",

  props: {
    preProjectId: {
      default: null,
    },
    jobType: {
      default: null,
    },
    projectType: {
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
      loginUser: {},
      loginCustomer: {},

      dialogTitle: '',
      showCharacterDlg: false,
      characterItemId: '',

      functionTree: [],
      defaultProps: {
        children: 'children',
        label: 'label',
      }
    }
  },

  computed: {},

  mounted: function () {
    let _this = this;
    _this.getLoginUser();
    _this.synchronizationBomTree();

    _this.$bus.$on('refresh-system-function-tab', (flag) => {
      if (flag) {
        _this.synchronizationBomTree();
      }
    });

  },

  beforeDestroy(){
    let _this = this;
    _this.$bus.$off('refresh-system-function-tab');
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

    renderContent(h, { node, data, store }) {
      if(data.haveUpdated){
        return (
            <span style="display: flex; flex-direction: row; justify-content: left; align-items: center;">
              {node.label}
              <el-button style="margin-left: 20px" size="mini" type="text"
                         onClick={(e) => {
                           e.stopPropagation(); // 阻止冒泡
                           this.handleButtonClick(data, node);
                         }}
              >查看</el-button>
              <div style="margin-left: 20px;">版本已升级</div>
            </span>
        );
      }
      if (data.children === null || data.children === undefined) {
        return (
            <span>
            {node.label}
              <el-button style="margin-left: 20px" size="mini" type="text"
                         onClick={(e) => {
                           e.stopPropagation(); // 阻止冒泡
                           this.handleButtonClick(data, node);
                         }}
              >查看</el-button>
          </span>
        );
      }else{
        return (
            <span>
            {node.label}
              <el-button style="margin-left: 20px" size="mini" type="text"
                         onClick={(e) => {
                           e.stopPropagation(); // 阻止冒泡
                           this.handleButtonClick(data, node);
                         }}
              >查看</el-button>
          </span>
        );
      }
    },

    handleButtonClick(data) {
      let _this = this;
      _this.showCharacterInfo(data.id);
      // console.log('Button clicked for node:', data);
      // 处理按钮点击事件
    },

    showCharacterInfo(id){
      let _this = this;
      _this.dialogTitle = '特性定义表';
      _this.characterItemId = id;
      _this.showCharacterDlg = true;
    },
    closeDialog() {
      let _this = this;
      _this.showCharacterDlg = false;
    },
    handleClose(done) {
      done(); //el-dialog用于关闭对话框的内置函数.
    },

    getFunctionTree(preprojectId) {
      let _this = this;
      let loadingInstance = Loading.service({ fullscreen: true });
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/function-tree/getFunctionTree/' + preprojectId).then((response)=>{
        let resp = response.data;
        loadingInstance.close();
        if (resp.success) {
          _this.functionTreeDto = resp.content;
          _this.functionTree = _this.functionTreeDto.children;
        }else{
          Toast.error(resp.message, Swal);
        }
      })
    },

    synchronizationBomTree() {
      let _this = this;
      let loadingInstance = Loading.service({ fullscreen: true });
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/function-tree/synchronizationFunctionTree/' + _this.preProjectId).then((response)=>{
        let resp = response.data;
        loadingInstance.close();
        if (resp.success) {
          _this.getFunctionTree(_this.preProjectId);
        }else{
          Toast.error(resp.message, Swal);
        }
      })
    },

  },
}
</script>

<style lang="scss" scoped>
@import './../../../public/static/scss/caipd.scss';

</style>
