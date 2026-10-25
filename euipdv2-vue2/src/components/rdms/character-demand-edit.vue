<!--
 * Author: 周朔鹏
 * Date: 2025-04-14
 * Email: 391902958@qq.com
 * Website: https://www.euipd.com
-->

<!--本模板在更新特性定义是,对功能的各项描述以及预算等相关信息进行全面修改-->
<template>
  <div class="item-container">
    <div class="a4-paper-big-plain" style="margin-bottom: 20px;">
      <div id="myContent">
        <!--特性编辑表-->
        <div>
          <character-detail-edit-core
              :key="('character-demand-' + (random))"
              ref="character_demand_edit"
              :character="characterItem"
              :save-character-info="saveCharacter"
          ></character-detail-edit-core>
        </div>
        <div style="margin-left: 28px; font-size: 14px; font-weight: bold; margin-bottom: 15px;">集成特性清单 <small>(选择 CBB)</small>:</div>
        <div style="display: flex; justify-content: center;">
          <el-transfer
              filterable
              filter-placeholder="请输入关键词"
              v-model="cbbValue"
              :data="cbbData"
              :titles="cbbTitles">
          </el-transfer>
        </div>
        <el-row>
          <el-col :span="22">
            <div v-show="resFiles && resFiles.length>0" style="display: flex; align-items: end; flex-direction: column; margin-bottom: 15px;">
              <div v-for="(item, i) in resFiles" :key="i" style="display: flex; flex-direction: row; align-items: center; margin: 5px 0 0 50px;">
                <div v-if="item">
                  {{item.name}}
                </div>
                <div v-if="item && item.authStatus">
                  <el-button type="text" style="margin-left: 50px;" @click="downloadFile(item)">下载</el-button>
                </div>
              </div>
            </div>
            <div style="text-align: right;">
              <file-rdms v-bind:input-id="'file-upload-demand-edit'"
                         v-bind:text="'上传附件'"
                         v-bind:suffixs=G_SUFFIXS
                         v-bind:customer="loginCustomer.id"
                         v-bind:use="FILE_GROUPING.CHARACTER.key"
                         v-bind:folder="'character'"
                         v-bind:after-upload="afterUpload">
              </file-rdms>
            </div>
          </el-col>
        </el-row>
        <div class="hr hr-18 dotted hr-double"></div>

        <el-row>
          <div style="display: flex; justify-content: space-evenly; align-items: center;">
            <div>
              <span>
                填表人 :
              </span>
              <span>
                {{ loginUser.trueName }}
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
        <div class="hr hr-18 dotted hr-double"></div>
        <div style="display: flex; justify-content: center;">
          <button type="button" v-on:click="saveCharacterItem()" class="ca-btn-primary">保存</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import FileRdms from "../../components/rdms/file-rdms";
import Swal from "sweetalert2";
import FileSaver from 'file-saver';


import CharacterDetailEditCore from "@/components/rdms/character-detail-edit-core";

export default {
  components: {
    FileRdms,
    CharacterDetailEditCore,
  },
  name: "character-demand-edit",

  props: {
    characterId: {
      default: null,
    },
    closeDlg: {
      type: Function,
      default: null
    },
  },

  computed: {
    contentHeight(){
      return this.myContentHeight+60 +'px';
    },

    getCharacterDescFontNum(){
      if(this.characterItem.functionDescription){
        return  Tool.richTextLength(this.characterItem.functionDescription);
      }
      return 0;
    },
    getWorkConditionFontNum(){
      if(this.characterItem.workCondition){
        return  Tool.richTextLength(this.characterItem.workCondition);
      }
      return 0;
    },
    getInputLogicalFontNum(){
      if(this.characterItem.inputLogicalDesc){
        return  Tool.richTextLength(this.characterItem.inputLogicalDesc);
      }
      return 0;
    },
    getFunctionLogicalFontNum(){
      if(this.characterItem.functionLogicalDesc){
        return  Tool.richTextLength(this.characterItem.functionLogicalDesc);
      }
      return 0;
    },
    getOutputLogicalFontNum(){
      if(this.characterItem.outputLogicalDesc){
        return  Tool.richTextLength(this.characterItem.outputLogicalDesc);
      }
      return 0;
    },
    getTestMethodFontNum(){
      if(this.characterItem.testMethod){
        return  Tool.richTextLength(this.characterItem.testMethod);
      }
      return 0;
    },
  },

  data() {
    return {
      loginUser: {},
      loginCustomer: {},

      random: 0,

      characterItem: {},
      dialogVisible: false,

      myContentHeight: 1000,

      G_SUFFIXS: G_SUFFIXS,
      FILE_GROUPING: FILE_GROUPING,
      CHARACTER_STATUS: CHARACTER_STATUS,
      resFile: {},
      resFiles: [],
      resFileIds: [],
      currentTime: {},
      currentTimeStr: '',

      cbbData: [],
      cbbValue: [],
      archivedCbbList: [],
      cbbTitles: ['可选 特性','已选 特性'],
    };
  },

  mounted: function () {
    let _this = this;
    _this.getLoginUser();

    if(Tool.isNotEmpty(_this.characterId)){
      _this.getRecordAndData(_this.characterId);
    }

    _this.currentTime = new Date();
    _this.currentTimeStr = _this.currentTime.toLocaleString()
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
      _this.getCbbListByCustomerId(_this.loginCustomer.id);
    },

    getCbbListByCustomerId(customerId) {
      let _this = this;
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/cbb/getCbbListByCustomerId/' + customerId).then((response) => {
        let resp = response.data;
        if (resp.success) {
          _this.archivedCbbList = resp.content;
          if(Tool.isNotEmpty_ES5(_this.archivedCbbList)){
            _this.archivedCbbList.forEach((item) => {
              _this.cbbData.push({
                label: item.characterName,
                key: item.id,
              });
            });
          }
        } else {
          Toast.warning(resp.message, Swal);
        }
      });
    },

    getRecordAndData(characterId) {
      let _this = this;
      let formData = new window.FormData();
      formData.append('characterId', characterId);
      formData.append('loginUserId', _this.loginUser.customerUserId);
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/character/getRecordAndData', formData).then((response) => {
        let resp = response.data;
        if (resp.success) {
          _this.characterItem = resp.content;
          if(!_this.characterItem.iterationVersion){
            _this.characterItem.iterationVersion = 1;
          }
          if(_this.characterItem.moduleIdListStr){
            _this.cbbValue = JSON.parse(_this.characterItem.moduleIdListStr);
          }
          _this.listAttachment(characterId);
        }else{
          Toast.warning(resp.message, Swal);
        }
      });
    },

    listAttachment(characterId) {
      let _this = this;
      let formData = new window.FormData();
      formData.append('characterId', characterId);
      formData.append('loginUserId', _this.loginUser.customerUserId);
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/file/rdms/getFileListByCharacterId', formData).then((response)=>{
        let resp = response.data;
        if (resp.success) {
          _this.random = Math.random();
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

    saveCharacterItem(){
      let _this = this;
      _this.$refs.character_demand_edit.saveCharacterItem();
    },

    saveCharacter(characterInfo) {
      let _this = this;
      // debugger
      if(! characterInfo.iterationVersion || characterInfo.iterationVersion === 0){
        _this.characterItem.iterationVersion = 1;
      }else {
        _this.characterItem.iterationVersion = characterInfo.iterationVersion;
      }
      _this.characterItem.characterName = characterInfo.characterName;
      _this.characterItem.functionDescription = characterInfo.functionDescription;
      _this.characterItem.workCondition = characterInfo.workCondition;
      _this.characterItem.inputLogicalDesc = characterInfo.inputLogicalDesc;
      _this.characterItem.functionLogicalDesc = characterInfo.functionLogicalDesc;
      _this.characterItem.outputLogicalDesc = characterInfo.outputLogicalDesc;
      _this.characterItem.testMethod = characterInfo.testMethod;

      _this.characterItem.customerId = _this.loginCustomer.id;
      _this.characterItem.fileIds = _this.resFileIds;

      _this.characterItem.moduleIdListStr = JSON.stringify(_this.cbbValue);
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/character/saveRecordAndData', _this.characterItem).then((response) => {
        let resp = response.data;
        if (resp.success) {
          Toast.success("保存成功", Swal);
          _this.characterItem = {
            devManhour: 0,
            testManhour: 0,
            materialFee: 0,
            otherFee: 0,
          };
          _this.resFiles = [];
          _this.closeDlg();
        } else {
          Toast.warning(resp.message, Swal);
        }
      })

    },

/*    reset(){
      let _this = this;
      _this.random = Math.random();
      _this.characterItem = {};
      _this.resFiles = [];
      if(!_this.characterItem.iterationVersion){
        _this.characterItem.iterationVersion = 1;
      }
    },*/

    afterUpload(resp) {
      let _this = this;
      _this.resFile = resp.content;
      _this.resFile.customerId = _this.loginCustomer.id;
      _this.resFile.preProjectId = _this.characterItem.preProjectId;
      _this.resFile.operatorId = _this.loginUser.customerUserId;
      _this.resFile.itemId = _this.characterItem.id;
      _this.saveFile(_this.resFile); // 将上传完成的数据保存到数据库
      _this.$forceUpdate();
    },

    saveFile(file){
      let _this = this;
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/file/rdms/save', file).then((response)=>{
        let resp = response.data;
        if (resp.success) {
          let file = resp.content;
          // debugger
          // Toast.success("保存成功", Swal);
          if(! _this.resFiles){
            _this.resFiles = [];
          }
          _this.resFiles.push(file);
          _this.resFileIds.push(file.id);
        } else {
          Toast.warning(resp.message, Swal);
        }
      })
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


  },
}
</script>

<style lang="scss" scoped>
@import './../../../public/static/scss/caipd';

.item-container {
  height: 100%;
  width: 100%;
  //margin: 0 2px 0 12px;
  //padding-right: 10px;
  overflow-y: scroll;
  display: -webkit-box; /* 老版本语法: Safari, iOS, Android browser, older WebKit browsers. */
  display: -moz-box; /* 老版本语法: Firefox (buggy) */
  display: -ms-flexbox; /* 混合版本语法: IE 10 */
  display: -webkit-flex; /* 新版本语法: Chrome 21+ */
  display: flex; /* 新版本语法: Opera 12.1, Firefox 22+ */

}

</style>
