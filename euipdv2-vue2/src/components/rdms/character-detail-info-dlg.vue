<!--
 * Author: 周朔鹏
 * Date: 2025-02-23
 * Email: 391902958@qq.com
 * Website: https://www.euipd.com
-->

<!--这个页面只用在character-detail-show-core中进行迭代显示-->
<template>
  <div style="width: 100%;">
    <div class="a4-paper-big-plain" style="padding-bottom: 20px;">
      <div style="margin-bottom: 20px;">
        <table class="table-out" v-if="characterItem">
          <tr>
            <table class="table-inn">
              <tr>
                <th style="width: 19.51%; text-align: center;">
                  <span>项目名称</span>
                </th>
                <th>
                  <span style="margin-left: 5px;" v-if="characterItem && characterItem.projectName">
                    {{ characterItem.projectName }}
                  </span>
                </th>
                <th style="width: 14.63%; text-align: center;">
                  <span>产品经理</span>
                </th>
                <th style="width: 24.39%;">
                  <span style="margin-left: 10px;" v-if="characterItem && characterItem.productManagerName">
                    {{ characterItem.productManagerName }}
                  </span>
                </th>
              </tr>
              <tr>
                <th style="width: 160px; text-align: center;">
                  <span>特性名称</span>
                </th>
                <th>
                  <span style="width: 400px; border: 0; margin-left: 5px;" v-if="characterItem && characterItem.characterName">
                    {{characterItem.characterName}}</span>
                </th>
                <th style="width: 120px; text-align: center;">
                  <span>特性编号</span>
                </th>
                <th>
                  <span style="margin-left: 10px;" v-if="characterItem && characterItem.iterationVersion">
                    {{ characterItem.iterationVersion | iterationFormat }}
                  </span>
                </th>
              </tr>
            </table>
          </tr>
          <tr>
            <table class="table-inn">
              <tr style="border-top: 2px solid #1a1a1a;">
                <th style="width: 160px; text-align: center;">
                  <span>特性描述</span>
                </th>
                <th>
                    <span style="margin-left: 10px;">
                      填表说明: 请概要描述功能或特性的概要, 要求清晰明了.
                    </span>
                </th>
              </tr>
            </table>
          </tr>
          <tr>
            <table class="table-inn">
              <tr>
                <th style="text-align: left; padding: 3px 15px;" v-if="characterItem && characterItem.functionDescription">
                  <div v-rich-html="characterItem.functionDescription"></div>
                </th>
              </tr>
            </table>
          </tr>
          <tr>
            <table class="table-inn">
              <tr style="border-top: 2px solid #1a1a1a;">
                <th style="width: 160px; text-align: center;">
                  <span>生效条件/应用范围</span>
                </th>
                <th>
                    <span style="margin-left: 10px;">
                      填表说明: 请详细说明这个功能或特性的生效条件.
                    </span>
                </th>
              </tr>
            </table>
          </tr>
          <tr>
            <table class="table-inn">
              <tr>
                <th style="text-align: left; padding: 3px 15px;" v-if="characterItem && characterItem.workCondition">
                  <div v-rich-html="characterItem.workCondition"></div>
                </th>
              </tr>
            </table>
          </tr>
          <tr>
            <table class="table-inn">
              <tr style="border-top: 2px solid #1a1a1a;">
                <th style="width: 160px; text-align: center;">
                  <span>输入逻辑/使用条件</span>
                </th>
                <th>
                    <span style="margin-left: 10px;">
                      填表说明: 请详细说明这个功能的输入是什么,输入的逻辑或条件是什么?
                    </span>
                </th>
              </tr>
            </table>
          </tr>
          <tr>
            <table class="table-inn">
              <tr>
                <th style="text-align: left; padding: 3px 15px;" v-if="characterItem && characterItem.inputLogicalDesc">
                  <div v-rich-html="characterItem.inputLogicalDesc"></div>
                </th>
              </tr>
            </table>
          </tr>
          <tr>
            <table class="table-inn">
              <tr style="border-top: 2px solid #1a1a1a;">
                <th style="width: 160px; text-align: center;">
                  <span>功能逻辑/功能要求</span>
                </th>
                <th>
                    <span style="margin-left: 10px;">
                      填表说明: 请对这个功能的执行逻辑或这行过程进行描述.
                    </span>
                </th>
              </tr>
            </table>
          </tr>
          <tr>
            <table class="table-inn">
              <tr>
                <th style="text-align: left; padding: 3px 15px;" v-if="characterItem && characterItem.functionLogicalDesc">
                  <div v-rich-html="characterItem.functionLogicalDesc"></div>
                </th>
              </tr>
            </table>
          </tr>
          <tr>
            <table class="table-inn">
              <tr style="border-top: 2px solid #1a1a1a;">
                <th style="width: 300px; text-align: center;">
                  <span>输出逻辑/交付指标/开发目标(Objectives)</span>
                </th>
                <th>
                    <span style="margin-left: 10px;">
                      填表说明: 请对这个功能的输出进行描述.
                    </span>
                </th>
              </tr>
            </table>
          </tr>
          <tr>
            <table class="table-inn">
              <tr>
                <th style="text-align: left; padding: 3px 15px;" v-if="characterItem && characterItem.outputLogicalDesc">
                  <div v-rich-html="characterItem.outputLogicalDesc"></div>
                </th>
              </tr>
            </table>
          </tr>
          <tr>
            <table class="table-inn">
              <tr style="border-top: 2px solid #1a1a1a;">
                <th style="width: 300px; text-align: center;">
                  <span>验证方法/测试方法/关键成果(Key Results)</span>
                </th>
                <th>
                    <span style="margin-left: 10px;">
                      填表说明: 请对如何测试和验证这项功能进行描述.
                    </span>
                </th>
              </tr>
            </table>
          </tr>
          <tr>
            <table class="table-inn">
              <tr>
                <th style="text-align: left; padding: 3px 15px;" v-if="characterItem && characterItem.testMethod">
                  <div v-rich-html="characterItem.testMethod"></div>
                </th>
              </tr>
            </table>
          </tr>
        </table>
        <el-row>
          <el-col :span="22">
            <div v-show="resFiles && resFiles.length>0" style="display: flex; align-items: end; flex-direction: column; margin-bottom: 15px;">
              <div v-for="(item, i) in resFiles" :key="i" style="display: flex; flex-direction: row; margin: 5px 0 0 50px;">
                <div v-if="item" style="margin-top: 6px;">
                  {{item.name}}
                </div>
                <div v-if="item && item.authStatus">
                  <el-button type="text" style="margin-left: 50px;" @click="downloadFile(item)">下载</el-button>
                </div>
              </div>
            </div>
          </el-col>
        </el-row>
        <div  v-if="demandList !== null">
          <div class="hr hr-18 dotted hr-double"></div>
          <el-row>
            <el-col :span="24">
              <div class="item-container">
                <div class="item-left" style="line-height: 28px;">需求:</div>
                <div class="item-right" style="display: flex; flex-direction: column;">
                  <div v-for="(item, i) in demandList" :key="i"
                       style="display: flex; flex-direction: row;">
                    <div style="line-height: 28px; width: 265px; white-space: nowrap;">
                      <a href="javascript:void(0);" @click="showDemandInfo(item)">{{ item.demandName }}</a>
                    </div>
                  </div>
                </div>
              </div>
            </el-col>
          </el-row>
        </div>
        <!--    显示父级特性定义表-->
        <div v-if="characterItem && characterItem.parent !== null">
          <el-row>
            <el-col :span="16">
              <div class="item-container">
                <div class="item-left" style="line-height: 28px;">原始特性定义:</div>
                <div class="item-right" style="display: flex; flex-direction: column;">
                  <div style="display: flex; justify-content: space-between;">
                    <div style="line-height: 28px; width: 265px; white-space: nowrap;">
                      <a href="javascript:void(0);" @click="showParentCharacterInfo(parentCharacter)">{{ parentCharacter.characterName }}</a>
                    </div>
                  </div>
                </div>
              </div>
            </el-col>
          </el-row>
        </div>
        <div v-if="characterItem && characterItem.referenceCharacterList">
          <div class="hr hr-10 dotted"></div>
          <div  style="margin-left: 55px;">
            <div class="ca-form-line">
              <div class="ca-form-label-xl">
                <div class="ca-form-label-title">
                  参考特性:
                </div>
              </div>
            </div>
            <div v-for="(item, i ) in characterItem.referenceCharacterList" :key="i"
                 style="margin-left: 16px; line-height: 25px; font-weight: 500;">
              <div v-if="item">
                  <a v-if="item.referenceCbbId" href="javascript:void(0);" @click="openReference(item)">{{ item.characterName }}</a>
                  <span v-else>{{ item.characterName }}</span>
                  <el-tag v-if="item.referenceCbbStatus === CBB_STATUS.DISABLED.key || item.referenceCbbStatus === CBB_STATUS.DISCARD.key"
                        size="mini" type="warning" style="margin-left: 6px;">{{ CBB_STATUS | optionKV(item.referenceCbbStatus) }}</el-tag>
              </div>
            </div>
          </div>
        </div>
        <div class="hr hr-18 dotted"></div>
        <el-row>
          <div style="display: flex; justify-content: space-evenly; align-items: center;">
            <div>
              <span>
                填表人 :
              </span>
              <span>
                {{ characterItem.writerName }}
              </span>
            </div>
            <div>
              <span>
                公司 :
              </span>
              <span v-if="characterItem && characterItem.customerName">
                {{ characterItem.customerName }}
              </span>
            </div>
            <div>
              <span>
                填表日期 :
              </span>
              <span>
                {{ currentTimeStr }}
              </span>
            </div>
          </div>
        </el-row>
      </div>
    </div>

    <!--Dialog: 显示原始需求-->
    <el-dialog
        title="参阅原始需求"
        :visible.sync="demandDialogVisible"
        width="32cm"
        append-to-body
        :close-on-click-modal=false
        :before-close="demandDialogClose">
      <demand-detail-dialog
          :key="demand.id"
          :demand-item="demand"
      ></demand-detail-dialog>
    </el-dialog>

    <!--Dialog: 显示上一层特性定义-->
    <el-dialog
        title="参阅原始特性定义表"
        :visible.sync="characterOriginalDialog"
        width="32cm"
        append-to-body
        :close-on-click-modal=false
        :before-close="parentCharacterDlgClose">
      <CharacterDetailInfoParentDlg
          :key="parentCharacter.id"
          :character="parentCharacter"
      ></CharacterDetailInfoParentDlg>
    </el-dialog>

    <CbbTechDocDialog :cbb-id="techDocCbbId" :visible.sync="techDocVisible"></CbbTechDocDialog>
  </div>
</template>

<script>
import CbbTechDocDialog from "@/components/rdms/cbb-tech-doc-dialog.vue";
import File from "../../components/rdms/file";
import Swal from "sweetalert2";
import FileSaver from 'file-saver';
import DemandDetailDialog from "@/components/rdms/demand-detail-dlg";
import CharacterDetailInfoParentDlg from "@/components/rdms/character-detail-info-parent-dlg.vue";

export default {
  components: {
    CbbTechDocDialog,
    CharacterDetailInfoParentDlg,
    File,
    DemandDetailDialog,
  },
  name: "character-detail-info-dlg",

  props: {
    characterId: {},
    closeDialogAndUpdate: {
      type: Function,
      default: null
    },
  },

  data() {
    return {
      techDocCbbId: null,
      techDocVisible: false,
      CBB_STATUS: CBB_STATUS,
      loginUser: {},
      loginCustomer: {},

      characterItem: {
        projectName: '',
        productManagerName: '',
        characterName: '',
        iterationVersion: 0,
        functionDescription: '',
        inputLogicalDesc: '',
        functionLogicalDesc: '',
        outputLogicalDesc: '',
        workCondition: '',
        testMethod: '',
        customerName: '',
      },
      demandList: [],

      FILE_GROUPING: FILE_GROUPING,
      CHARACTER_STATUS: CHARACTER_STATUS,
      resFile: {},
      resFiles: [],
      resFileIds: [],
      currentTime: {},
      currentTimeStr: '',

      demand: {},
      demandDialogVisible: false,
      characterOriginalDialog: false,

      random: 0,
      parentCharacter: {
        default: null,
      },
    };
  },

  computed: {},

  mounted: function () {
    let _this = this;
    _this.random = Math.random();
    _this.getLoginUser();

    _this.currentTime = new Date();
    _this.currentTimeStr = _this.currentTime.toLocaleString()


    if(_this.characterId){
      _this.getMergedCharacter(_this.characterId);
    }
  },

  methods: {
    openReference(item) {
      this.techDocCbbId = item.referenceCbbId;
      this.techDocVisible = true;
    },
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
          if(_this.fileItems !== null){
            for(let i= 0 ; i < _this.fileItems.length; i++){
              _this.resFiles.push(_this.fileItems[i]);
              _this.resFileIds.push(_this.fileItems[i].id);
            }
          }
        } else {
          Toast.warning(resp.message,Swal);
        }
      })
    },

    listDemand(characterId) {
      let _this = this;
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/demand/getDemandListByCharacterId/' + characterId).then((response)=>{
        let resp = response.data;
        if (resp.success) {
          _this.demandList = resp.content;
        } else {
          Toast.warning(resp.message,Swal);
        }
      })
    },

    getMergedCharacter(characterId) {
      let _this = this;
      let formData = new window.FormData();
      formData.append('characterId', characterId);
      formData.append('loginUserId', _this.loginUser.customerUserId);
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/character/getCharacterRecordDetailById', formData).then((response) => {
        let resp = response.data;
        if (resp.success) {
          _this.characterItem = resp.content;
          _this.listAttachment(_this.characterItem.id);
          _this.listDemand(_this.characterItem.id);
          if(_this.characterItem.parent !== null){
            _this.getParentCharacter(_this.characterItem.parent);
          }
        }else{
          Toast.warning(resp.message, Swal);
        }
      });
    },
    getParentCharacter(characterId) {
      let _this = this;
      let formData = new window.FormData();
      formData.append('characterId', characterId);
      formData.append('loginUserId', _this.loginUser.customerUserId);
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/character/getCharacterRecordDetailById', formData).then((response) => {
        let resp = response.data;
        if (resp.success) {
          _this.parentCharacter = resp.content;
        }else{
          Toast.warning(resp.message, Swal);
        }
      });
    },

    showParentCharacterInfo(item) {
      let _this = this;
      _this.parentCharacter = item;
      _this.characterOriginalDialog = true;
    },
    parentCharacterDlgClose(done) {
      let _this = this;
      _this.characterOriginalDialog = false;
      done();
    },


    downloadFile(file) {
      let _this = this;
      _this.getSignedUrl(file.path, file.suffix);
    },
    getSignedUrl (fileFullName, suffix) {
      let _this = this;
      let formData = new window.FormData();
      formData.append('fileFullName', fileFullName);
      formData.append('customer', _this.loginCustomer.id);
      formData.append('user', _this.loginUser.customerUserId);
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/file/rdms/oss-get-signed-url', formData).then((response)=>{
        let resp = response.data;
        Tool.openOssSignedUrl(resp, suffix);
        // FileSaver.saveAs(resp.content, fileName);
      });
    },

    showDemandInfo(item) {
      let _this = this;
      _this.demandDialogVisible = true;
      _this.demand = item;
    },

    demandDialogClose(done) {
      let _this = this;
      _this.demandDialogVisible = false;
      done();
    },
  },
}
</script>

<style lang="scss" scoped>
@import './../../../public/static/scss/caipd';

.a4-paper-big-plain .table-out {
  width: var(--table-out-width, 100%);
  max-width: 22cm;
}
.a4-paper-big-plain .table-inn {
  width: 100%;
  table-layout: fixed;
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

/*.a4-paper-big {
  margin: -20px auto 0;
  //margin-top: -20px;
  //margin-bottom: 0.5cm;
  background: white;
  width: 24.3cm;
  //min-height: 33cm;
  display: block;
  //margin: 20px auto;
  //padding: 10px 25px;
  box-shadow: 0 0 0cm rgba(0, 0, 0, 0) !important;
  overflow-y: scroll;
  box-sizing: border-box;
  .table-out{
    width: 23.4cm;
    margin: 26px auto;
    border-top: 1px solid #1a1a1a;
    border-left: 1px solid #1a1a1a;
    border-right: 1px solid #1a1a1a;
    border-bottom: 2px solid #1a1a1a;
    .table-inn{
      width: 100%;
      tr{
        line-height: 28px;
        width: 100%;
        th{
          border-top: 1px solid #1a1a1a;
          border-left: 1px solid #1a1a1a;
          border-right: 1px solid #1a1a1a;
          font-weight: 350;
        }
      }

    }
  }
}*/
</style>
