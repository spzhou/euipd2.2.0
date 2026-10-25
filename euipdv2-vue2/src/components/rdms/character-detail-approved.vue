<!--
 * Author: 周朔鹏
 * Date: 2025-04-18
 * Email: 391902958@qq.com
 * Website: https://www.euipd.com
-->

<template>
  <div class="paper-height">
    <div style="height: 100%; overflow: scroll; ">
      <el-row>
        <el-col>
          <div class="a4-paper-big">
            <div id="submit-print">
              <el-row>
                <el-col :span="24" style="width: 100%; text-align: center; font-size: 28px;margin-top: 25px;">
                  <div style="display: flex; flex-direction: column; justify-content: center; align-items: center;">
                    <div>
                      {{characterItem.characterName}}
                    </div>
<!--                    <div>
                      <el-tooltip content="直接投入到特性的研发管理过程中, 无需整体立项." placement="top" effect="light" :open-delay=1000>
                        <el-button type="primary" v-on:click="setupApplication">启动特性研发</el-button>
                      </el-tooltip>
                    </div>-->
                    <div v-if="canRevise" class="no-print">
                      <el-button type="primary" size="small" @click="iterationDlgVisible = true">功能修订</el-button>
                    </div>
                  </div>
                </el-col>
              </el-row>
              <div>
                <CharacterDetailInfoCore
                    :id="('character-item-detail-approve')"
                    :key="characterItem.id"
                    :character-id="characterItem.id"
                ></CharacterDetailInfoCore>
              </div>
              <div class="hr hr-18 dotted hr-double"></div>
              <div v-if="characterProcessList.length > 0">
                <el-row>
                  <div style="width: var(--table-out-width, 100%); max-width: 22cm; margin: 0 auto;">
                    <div>
                      <el-table
                          ref="characterProcessTable"
                          :data="characterProcessList"
                          border
                          stripe
                          style="width: 100%"
                          highlight-current-row>
                        <el-table-column   label="序号" align="center" width="50">
                          <template slot-scope="scope">
                            {{scope.$index+1}}
                          </template>
                        </el-table-column>
                        <el-table-column
                            prop="processStatusName"
                            label="执行过程"
                            align="center"
                            width="70">
                        </el-table-column>
                        <el-table-column
                            prop="jobDescription"
                            label="审批描述">
                        </el-table-column>
                        <el-table-column
                            prop="executorName"
                            label="签审人"
                            align="center"
                            width="70">
                        </el-table-column>
                        <el-table-column
                            prop="nextNodeName"
                            label="下一节点"
                            align="center"
                            width="70">
                        </el-table-column>
                        <el-table-column
                            prop="createTimeStr"
                            label="创建时间"
                            align="center"
                            width="150">
                        </el-table-column>
                      </el-table>
                    </div>
                  </div>
                </el-row>
              </div>
            </div>
          </div>
        </el-col>
      </el-row>
    </div>

    <!--申请立项 对话框-->
    <el-dialog
        :visible.sync="dialogVisible"
        width="29cm"
        :before-close="handleClose"
        :close-on-click-modal=false>
      <div slot="title" style="font-size: 20px;">{{ dialogTitle }}</div>
      <CharacterSetupApplication
          :key="characterItem.id"
          :character-item="characterItem"
          :close-function="closeDialog"
      ></CharacterSetupApplication>
    </el-dialog>

    <!--功能修订-->
    <el-dialog
        title="创建功能修订任务"
        width="29cm"
        :visible.sync="iterationDlgVisible"
        :close-on-click-modal=false>
      <JobitemCreateByCharacterIdDlg
          v-if="iterationDlgVisible"
          :key="characterItem.id + 'iteration'"
          :character-id="characterItem.id"
          :project-type="PROJECT_TYPE.PRE_PROJECT.key"
          :job-type="JOB_ITEM_TYPE.ITERATION.key"
          :close-dlg="closeIterationDlg">
      </JobitemCreateByCharacterIdDlg>
    </el-dialog>
  </div>
</template>

<script>

import Swal from "sweetalert2";
import printJS from "print-js";
import CharacterDetailInfoCore from "@/components/std/character-detail-info-core.vue";
import CharacterSetupApplication from "@/components/rdms/character-setup-application.vue";
import JobitemCreateByCharacterIdDlg from "@/components/std/jobitem-create-by-character-id-dlg.vue";

export default {
  components: {
    CharacterSetupApplication,
    CharacterDetailInfoCore,
    JobitemCreateByCharacterIdDlg,
  },
  name: "character-item-detail-approved",

  props: {
    character: {
      default: null,
    },
  },

  data() {
    return {
      loginUser: {},
      loginCustomer: {},

      fileItems: [],
      characterItem: {},
      setupProjectItem: {},
      dialogVisible: false,
      iterationDlgVisible: false,
      PROJECT_TYPE: PROJECT_TYPE,
      JOB_ITEM_TYPE: JOB_ITEM_TYPE,
      characterProcessList: [],
      timeStamp: '',
      dialogTitle: 'Dialog',
      printData: {
        printable: 'submit-print',
        type: 'html',
        // header: this.characterItem.preProjectName,
        // headerStyle: 'font-weight: 600; font-size: 30px;',
        ignore: ['no-print'],
        targetStyles: ['*'],  //css样式，写成*代表打印样式完全继承你页面的样式
        // maxWidth:'800px'  //打印界面最大宽度，适当调整可以解决打印页面过宽，显示不完整的问题
      }
    };
  },

  computed: {
    /** 预立项修订只能由特性的产品经理派发，其他角色发起会被后端拒绝。 */
    canRevise() {
      return this.characterItem.status === CHARACTER_STATUS.APPROVED.key
          && Tool.isNotEmpty(this.loginUser.customerUserId)
          && this.characterItem.productManagerId === this.loginUser.customerUserId;
    },
  },

  mounted: function () {
    let _this = this;
    _this.getLoginUser();
    _this.characterItem = _this.character;
    _this.listAttachment(_this.characterItem.id);
    _this.listProcessByCharacterId(_this.characterItem.id);
  },

  updated() {
  },

  beforeDestroy(){
    let _this = this;
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

    listProcessByCharacterId(id){
      let _this = this;
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/character-process/list-by-characterId/' + id).then((response)=>{
        let resp = response.data;
        if (resp.success) {
          _this.characterProcessList = resp.content;
        } else {
          Toast.warning(resp.message,Swal);
        }
      })
    },

    handlePrint(params) {
      printJS({
        printable: params.printable, // 'printFrom', // 标签元素id
        type: params.type || 'html',
        header: params.header, // '表单',
        targetStyles: ['*'],
        style: '@page {margin:20 10mm};', // 可选-打印时去掉眉页眉尾
        ignoreElements: params.ignore || [], // ['no-print']
        properties: params.properties || null
      })
    },

    listAttachment(characterId) {
      let _this = this;
      let formData = new window.FormData();
      formData.append('characterId', characterId);
      formData.append('loginUserId', _this.loginUser.customerUserId);
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/file/rdms/getFileListByCharacterId', formData).then((response)=>{
        let resp = response.data;
        if (resp.success) {
          _this.fileItems = resp.content;
          // debugger
        } else {
          Toast.warning(resp.message,Swal);
        }
      })
    },

    setupApplication() {
      let _this = this;
      _this.timeStamp = Date.now();
      _this.dialogVisible = true;
      _this.dialogTitle = '特性启动研发申请';
    },

    closeDialog() {
      let _this = this;
      _this.dialogVisible = false;
    },

    closeIterationDlg() {
      this.iterationDlgVisible = false;
    },

    handleClose(done) {
      done();
    },
  },
}
</script>

<style lang="scss" scoped>
@import './../../../public/static/scss/caipd';

.paper-height{
  height: calc(100vh - 241px);
}

.item-container {
  //margin-top: 10px;
  width: 100%;
  height: 100%;
  display: -webkit-box; /* 老版本语法: Safari, iOS, Android browser, older WebKit browsers. */
  display: -moz-box; /* 老版本语法: Firefox (buggy) */
  display: -ms-flexbox; /* 混合版本语法: IE 10 */
  display: -webkit-flex; /* 新版本语法: Chrome 21+ */
  display: flex; /* 新版本语法: Opera 12.1, Firefox 22+ */
  .item-left {
    width: 100px;
    text-align: right;
    //margin-bottom: 20px;
    //background-color: #00B83F;
  }
  .item-right {
    margin-left: 10px;
    background-color: white !important;
    -webkit-flex: 1; /* Chrome */
    -ms-flex: 1; /* IE 10 */
    flex: 1; /* NEW, Spec - Opera 12.1, Firefox 20+ */
    -webkit-box-flex: 1; /* OLD - iOS 6-, Safari 3.1-6 */
    -moz-box-flex: 1; /* OLD - Firefox 19- */
    background-color: rgba(71, 148, 239, 0.08);
  }
}

</style>
