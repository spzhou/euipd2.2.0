<!--
 * Author: 周朔鹏
 * Date: 2025-02-23
 * Email: 391902958@qq.com
 * Website: https://www.euipd.com
-->

<template>
  <div>
<!--    <div class="buttons">-->
<!--      <el-button type="primary" @click="saveFunctionTree">编辑完成后,请保存</el-button>-->
<!--    </div>-->
<!--    <div class="hr hr-10 hr-dotted"></div>-->
    <el-tree
        ref="tree"
        :data="functionTree"
        node-key="id"
        default-expand-all
        draggable
        :props="defaultProps"
        @node-drop="handleDrop"
        :render-content="renderContent"
    ></el-tree>
    <div class="hr hr-10 hr-dotted"></div>
    <div>
      <small>查看: <em>查看特性定义及开发相关文件</em></small><br/>
      <small>修订: <em>功能修订（ITERATION），修正定义而不升版</em></small><br/>
      <small>版本迭代: <em>特性升级/版本迭代（UPDATE），请走迭代定义入口</em></small><br/>
      <small>分解: <em>下发功能分解工单（DECOMPOSE），由执行人编写子特性；节点为保存、提交、已批准、退回状态时可用</em></small><br/>
      <small>合并: <em>下发功能合并工单（MERGE），合并节点自身及全部子孙；来源须为已批准或迭代中</em></small><br/>
      <small>归并: <em>已废弃（JOIN_TO）；请使用合并或其他任务类型</em></small>
    </div>

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

    <!--功能修订（ITERATION；FUNCTION 入口已废弃）-->
    <el-dialog
        title="创建功能修订任务"
        width="29cm"
        :visible.sync="jobItemDlgVisible_iteration"
        :close-on-click-modal=false>
      <div>
        <JobitemCreateByCharacterIdDlg
            :key="characterItem.id + 'iteration'"
            :character-id="characterItem.id"
            :project-type="PROJECT_TYPE.PRE_PROJECT.key"
            :job-type="JOB_ITEM_TYPE.ITERATION.key"
            :close-dlg = closeDialog>
        </JobitemCreateByCharacterIdDlg>
      </div>
    </el-dialog>

    <!--功能分解-->
    <el-dialog
        title="创建功能分解任务"
        width="29cm"
        :visible.sync="jobItemDlgVisible_decompose"
        :close-on-click-modal=false>
      <div>
        <JobitemCreateByCharacterIdDlg
            v-if="jobItemDlgVisible_decompose"
            :key="characterItem.id + 'decompose'"
            :character-id="characterItem.id"
            :project-type="PROJECT_TYPE.PRE_PROJECT.key"
            :job-type="JOB_ITEM_TYPE.DECOMPOSE.key"
            :entrance="entrance"
            :close-dlg = closeDialog>
        </JobitemCreateByCharacterIdDlg>
      </div>
    </el-dialog>


    <!--功能合并任务-->
    <el-dialog
        width="29cm"
        :visible.sync="characterMergeDlg"
        :close-on-click-modal=false>
      <div slot="title" style="font-size: 20px;">{{ dialogTitle }}</div>
      <div>
        <JobitemCharacterMergeDlg
            v-if="characterMergeDlg"
            :key="random"
            :character-id-list="mergeIdList"
            :close-dlg = closeDialog
        ></JobitemCharacterMergeDlg>
      </div>
    </el-dialog>

  </div>
</template>

<script>
import Swal from "sweetalert2";
import {Loading} from "element-ui";
import CharacterDocTabs from "@/components/rdms/character-doc-tabs.vue";
import CharacterUpdateDlg from "@/components/rdms/character-update-dlg.vue";
import JobitemCharacterMergeDlg from "@/components/std/jobitem-character-merge-dlg.vue";
import JobitemCreateByCharacterIdDlg from "@/components/std/jobitem-create-by-character-id-dlg.vue";

export default {
  components: {
    JobitemCreateByCharacterIdDlg,
    JobitemCharacterMergeDlg,
    CharacterUpdateDlg,
    CharacterDocTabs,
  },
  name: "system-function-tab",

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
      jobItemDlgVisible_iteration: false,
      jobItemDlgVisible_decompose: false,
      characterItem: {},
      characterMergeDlg: false,
      random: 0,
      checkedCharacterIdList: [],
      mergeIdList: [],
      JOB_ITEM_TYPE: JOB_ITEM_TYPE,
      PROJECT_TYPE: PROJECT_TYPE,

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

    decomposeDlg(data) {
      let _this = this;
      _this.characterItem = data;
      _this.jobItemDlgVisible_decompose = true;
    },

    iterationDlg(data, node) {
      let _this = this;
      _this.characterItem = data;
      _this.jobItemDlgVisible_iteration = true;
    },

    canDecompose(data) {
      return [CHARACTER_STATUS.SAVED.key, CHARACTER_STATUS.SUBMIT.key, CHARACTER_STATUS.APPROVED.key,
        CHARACTER_STATUS.REFUSE.key].includes(data.status);
    },

    lockText(data) {
      if (data.status === CHARACTER_STATUS.DECOMPOSED.key) {
        return '已分解';
      }
      if (data.status === CHARACTER_STATUS.MERGED.key) {
        return '已合并';
      }
      return '版本已升级';
    },

    renderContent(h, { node, data, store }) {
      const view = (
          <el-button style="margin-left: 20px" size="mini" type="text"
                     onClick={(e) => {
                       e.stopPropagation(); // 阻止冒泡
                       this.handleButtonClick(data, node);
                     }}
          >查看</el-button>
      );
      if(data.haveUpdated){
        return (
            <span style="display: flex; flex-direction: row; justify-content: left; align-items: center;">
              {node.label}
              {view}
              <div style="margin-left: 20px;">{this.lockText(data)}</div>
            </span>
        );
      }
      const hasChildren = Array.isArray(data.children) && data.children.length > 0;
      return (
          <span>
            {node.label}
            {view}
            <el-button size="mini" type="text"
                       onClick={(e) => {
                         e.stopPropagation(); // 阻止冒泡
                         this.iterationDlg(data, node);
                       }}
            >修订</el-button>
            {this.canDecompose(data) ? (
                <el-button size="mini" type="text"
                           onClick={(e) => {
                             e.stopPropagation(); // 阻止冒泡
                             this.decomposeDlg(data, node);
                           }}
                >分解</el-button>
            ) : null}
            {hasChildren ? (
                <el-button size="mini" type="text"
                           onClick={(e) => {
                             e.stopPropagation(); // 阻止冒泡
                             this.handleMergeClick(data, node);
                           }}
                >合并</el-button>
            ) : null}
          </span>
      );
    },
    handleDrop(draggingNode, dropNode, dropType, ev) {
      let _this = this;
      _this.saveFunctionTree();
      // console.log('tree drop: ', dropNode.label, dropType);
    },

    handleMergeClick(data) {
      let _this = this;
      if(!Array.isArray(data.children) || data.children.length === 0){
        Toast.warning('没有可合并项!', Swal);
        return;
      }

      _this.characterMergeDlg = true;
      _this.random = Math.random();
      _this.dialogTitle = "功能合并定义";

      _this.mergeIdList = [];
      function getChildrenIdList(children) {
        for (let i = 0; i < children.length; i++) {
          _this.mergeIdList.push(children[i].id);
          if(children[i].children !== null){
            getChildrenIdList(children[i].children);
          }
        }
      }

      if(data.children !== null){
        _this.mergeIdList.push(data.id)
        let children = data.children;
        getChildrenIdList(children);
      }else{
        return;
      }

      console.log('mergeIdList:', _this.mergeIdList);
      // 处理按钮点击事件
    },

    handleMergeIntoClick(data) {
      let _this = this;
      if(data.children === null){
        Toast.warning('没有可归并项!', Swal);
        return;
      }
      // 主功能记录
      let mainCharacterId = data.id;

      function getChildrenIdList(children) {
        for (let i = 0; i < children.length; i++) {
          _this.mergeIdList.push(children[i].id);
          if(children[i].children !== null){
            getChildrenIdList(children[i].children);
          }
        }
      }

      Swal.fire({
        title: '确认？',
        text: "归并后, 被归并项将作为主定义项的支持文件归档存储",
        icon: 'question',
        showCancelButton: true,
        confirmButtonColor: '#409EFF',
        cancelButtonColor: '#909399',
        confirmButtonText: '确认!',
        cancelButtonText: '取消!'
      }).then((result) => {
        if (result.value) {
          _this.mergeIdList = [];

          //收集并入记录
          if(data.children !== null){
            let children = data.children;
            getChildrenIdList(children);
          }else{
            return;
          }
          // console.log('Button clicked for node:', data);
          console.log('mainCharacterId:', mainCharacterId);
          console.log('mergeIdList:', _this.mergeIdList);
        }
      })
    },

    handleButtonClick(data) {
      let _this = this;
      _this.showCharacterInfo(data.id);
      // console.log('Button clicked for node:', data);
      // 处理按钮点击事件
    },

    getCheckedKeys() {
      console.log(this.$refs.tree.getCheckedKeys());
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
      _this.characterMergeDlg = false;
      _this.jobItemDlgVisible_iteration = false;
      _this.jobItemDlgVisible_decompose = false;
    },
    handleClose(done) {
      done(); //el-dialog用于关闭对话框的内置函数.
    },

    saveFunctionTree() {
      let _this = this;
      if(Tool.isEmpty_ES5(_this.functionTree)){
        return;
      }
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/function-tree/saveFunctionTree', _this.functionTree).then((response)=>{
        let resp = response.data;
        if (resp.success) {
          _this.functionTreeDto = resp.content;
          _this.functionTree = _this.functionTreeDto.children;
        }else{
          Toast.error(resp.message, Swal);
        }
      })
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
