<!--
 * Author: 周朔鹏
 * Date: 2025-01-04
 * Email: 391902958@qq.com
 * Website: https://www.euipd.com
-->

<template>
  <div class="container">
    <div class="a4-paper-big">
      <div>
        <table class="table-out">
          <tr>
            <table class="table-inn">
              <tr>
                <th style="width: 19.51%; text-align: center;">
                  <span>项目名称</span>
                </th>
                <th>
                  <span style="margin-left: 5px;">
                    {{ characterItem.projectName }}
                  </span>
                </th>
                <th style="width: 14.63%; text-align: center;">
                  <span>产品经理</span>
                </th>
                <th style="width: 24.39%;">
                  <span style="margin-left: 10px;">
                    {{ characterItem.productManagerName }}
                  </span>
                </th>
              </tr>
              <tr>
                <th style="width: 160px; text-align: center;">
                  <span>特性名称</span>
                </th>
                <th>
                  <input style="width: 400px; border: 0;" v-model="characterItem.characterName" placeholder="请输入特性名称,不少于4个字">
                </th>
                <th style="width: 120px; text-align: center;">
                  <span>特性编号</span>
                </th>
                <th>
                  <span style="margin-left: 10px;">
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
                <th style="text-align: center;">
                  <el-input type="textarea" :rows="5" v-model="characterItem.functionDescription" placeholder="请编写特性描述,以便阅读者快速了解相关功能. 10~500字"></el-input>
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
                <th style="text-align: center;">
                  <el-input type="textarea" :rows="5" v-model="characterItem.workCondition" placeholder="请描述功能生效的条件或应用的范围. 10~500字"></el-input>
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
                <th style="text-align: center;">
                  <el-input type="textarea" :rows="5" v-model="characterItem.inputLogicalDesc" placeholder="请描述输入的逻辑和使用的条件. 10~900字"></el-input>
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
                <th style="text-align: center;">
                  <el-input type="textarea" :rows="5" v-model="characterItem.functionLogicalDesc" placeholder="请描述相应功能内部处理的逻辑或相关开发要求. 10~900字"></el-input>
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
                <th style="text-align: center;">
                  <el-input type="textarea" :rows="5" v-model="characterItem.outputLogicalDesc" placeholder="请描述输出的逻辑或交付的指标. 10~900字"></el-input>
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
                <th style="text-align: center;">
                  <el-input type="textarea" :rows="5" v-model="characterItem.testMethod" placeholder="请对功能验证的方法或测试的方法进行简述. 10~500字"></el-input>
                </th>
              </tr>
            </table>
          </tr>
        </table>
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
              <file-rdms v-bind:input-id="'character-item-modify'"
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
                公司 :
              </span>
              <span>
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
      <div class="hr hr-18 dotted hr-double"></div>
      <div style="display: flex; justify-content: center;">
        <button type="button" v-on:click="saveCharacterModify()" class="ca-btn-primary">保存</button>
        <button type="button" v-on:click="submitCharacterModify()" class="ca-btn-primary">提交</button>
      </div>
      <p style="text-align: center; margin-top: 15px;">备注: 保存后, 可以在列表中进行编辑, 然后提交; 提交就是保存并执行提交操作, 本人将不能修改, 除非产品经理驳回.</p>
    </div>
  </div>
</template>

<script>
import FileRdms from "@/components/rdms/file-rdms";
import Swal from "sweetalert2";
import FileSaver from 'file-saver';

export default {
  components: {
    FileRdms,
  },
  name: "character-item-update",

  props: {
    character: {},
    closeModifyDlg: {
      type: Function,
      default: null
    },
  },

  data() {
    return {
      loginUser: {},
      loginCustomer: {},

      characterItem: {},
      G_SUFFIXS: G_SUFFIXS,
      FILE_GROUPING: FILE_GROUPING,
      CHARACTER_STATUS: CHARACTER_STATUS,
      JOB_ITEM_TYPE: JOB_ITEM_TYPE,
      resFile: {},
      resFiles: [],
      resFileIds: [],
      resAttachFiles: [],
      resAttachFileIds: [],


      currentTime: {},
      currentTimeStr: '',
    };
  },

  computed: {},

  mounted: function () {
    let _this = this;
    _this.getLoginUser();

    _this.characterItem = _this.character;
    _this.characterItem.characterSerialNo = _this.characterItem.id;
    _this.characterItem.id = Uuid.shortUuid(8);
    // console.log("_this.characterItem.id:", _this.characterItem.id);
    _this.currentTime = new Date();
    _this.currentTimeStr = _this.currentTime.toLocaleString()

    _this.listAttachment(_this.characterItem.id);
    _this.listAttachmentByOperatorId();
  },

  beforeDestroy(){
    let _this = this;
    // _this.$bus.$off('characterEditChanged');
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


    /**
     * 保存客户信息
     */
    saveCharacterModify() {
      let _this = this;
      // 保存校验
      if (1 !== 1
          || !Validator.require(_this.characterItem.characterName, "特性名称",Swal)
          || !Validator.require(_this.characterItem.functionDescription, "特性描述",Swal)
          || !Validator.require(_this.characterItem.workCondition, "生效条件/应用范围",Swal)
          || !Validator.require(_this.characterItem.inputLogicalDesc, "输入逻辑/使用条件",Swal)
          || !Validator.require(_this.characterItem.functionLogicalDesc, "功能逻辑/功能要求",Swal)
          || !Validator.require(_this.characterItem.outputLogicalDesc, "输出逻辑/交付指标",Swal)
          || !Validator.require(_this.characterItem.testMethod, "验证方法/测试方法",Swal)

          || !Validator.length(_this.characterItem.characterName, "特性名称", 4, 80,Swal)
          || !Validator.richLength(_this.characterItem.functionDescription, "特性描述", 10, 500,Swal)
          || !Validator.richLength(_this.characterItem.workCondition, "生效条件/应用范围", 10, 500,Swal)
          || !Validator.richLength(_this.characterItem.inputLogicalDesc, "输入逻辑/使用条件", 20, 900,Swal)
          || !Validator.richLength(_this.characterItem.functionLogicalDesc, "功能逻辑/功能要求", 20, 900,Swal)
          || !Validator.richLength(_this.characterItem.outputLogicalDesc, "输出逻辑/交付指标", 20, 900,Swal)
          || !Validator.richLength(_this.characterItem.testMethod, "验证方法/测试方法", 10, 500,Swal)

      ) {
        return;
      }
      _this.characterItem.customerId = _this.loginCustomer.id;
      _this.characterItem.writerId = _this.loginUser.customerUserId;
      _this.characterItem.fileIds = _this.resFileIds;
      _this.characterItem.status = CHARACTER_STATUS.SAVED.key;

      if(_this.characterItem.modifyVersion === null){
        _this.characterItem.modifyVersion = 0;
      }
      _this.characterItem.modifyVersion += 1;
      _this.characterItem.createTime = null; //让后台重新生产创建时间

      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/character/save', _this.characterItem).then((response)=>{
        let resp = response.data;
        if (resp.success) {
          Toast.success("保存成功", Swal);
          _this.closeModifyDlg(); //回调函数
        } else {
          Toast.warning(resp.message, Swal);
        }
      })
    },

    /**
     * 保存客户信息
     */
    submitCharacterModify() {
      let _this = this;
      // 保存校验
      if (1 !== 1
          || !Validator.require(_this.characterItem.characterName, "特性名称",Swal)
          || !Validator.require(_this.characterItem.functionDescription, "特性描述",Swal)
          || !Validator.require(_this.characterItem.workCondition, "生效条件/应用范围",Swal)
          || !Validator.require(_this.characterItem.inputLogicalDesc, "输入逻辑/使用条件",Swal)
          || !Validator.require(_this.characterItem.functionLogicalDesc, "功能逻辑/功能要求",Swal)
          || !Validator.require(_this.characterItem.outputLogicalDesc, "输出逻辑/交付指标",Swal)
          || !Validator.require(_this.characterItem.testMethod, "验证方法/测试方法",Swal)

          || !Validator.length(_this.characterItem.characterName, "特性名称", 4, 80,Swal)
          || !Validator.richLength(_this.characterItem.functionDescription, "特性描述", 10, 500,Swal)
          || !Validator.richLength(_this.characterItem.workCondition, "生效条件/应用范围", 10, 500,Swal)
          || !Validator.richLength(_this.characterItem.inputLogicalDesc, "输入逻辑/使用条件", 20, 900,Swal)
          || !Validator.richLength(_this.characterItem.functionLogicalDesc, "功能逻辑/功能要求", 20, 900,Swal)
          || !Validator.richLength(_this.characterItem.outputLogicalDesc, "输出逻辑/交付指标", 20, 900,Swal)
          || !Validator.richLength(_this.characterItem.testMethod, "验证方法/测试方法", 10, 500,Swal)
      ) {
        return;
      }

      _this.characterItem.customerId = _this.loginCustomer.id;
      _this.characterItem.writerId = _this.loginUser.customerUserId;
      _this.characterItem.fileIds = _this.resAttachFileIds;  //提交的时候,把所有自己编写的附件全部放上去
      _this.characterItem.status = CHARACTER_STATUS.SUBMIT.key;

      Swal.fire({
        title: '确认？',
        text: "确认提交特性定义文件吗？",
        icon: 'warning',
        showCancelButton: true,
        confirmButtonColor: '#409EFF',
        cancelButtonColor: '#909399',
        confirmButtonText: '确认!',
        cancelButtonText: '取消!'
      }).then((result) => {
        if(result.isConfirmed){
          _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/character/submit', _this.characterItem).then((response)=>{
            let resp = response.data;
            if (resp.success) {
              Toast.success("提交成功", Swal);
              _this.$bus.$emit('refresh-character-joblist-editing', true);
              // _this.$bus.$emit('refreshCharacterList-submit', true);
              // _this.$bus.$emit('refresh-character-joblist-examine', true);
            } else {
              Toast.warning(resp.message, Swal);
            }
          })
        }
      }).catch(_ => {});
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

    //列出登录用户自己上传的附件
    listAttachmentByOperatorId() {
      let _this = this;
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/file/rdms/list-file-info', {
        use: FILE_GROUPING.CHARACTER.key,
        itemId: _this.character.id,
        operatorId: _this.loginUser.customerUserId,
        customerId: _this.loginCustomer.id,
      }).then((response)=>{
        let resp = response.data;
        if (resp.success) {
          let fileItems = resp.content;
          // debugger
          for(let i= 0 ; i < fileItems.length; i++){
            _this.resAttachFiles.push(fileItems[i]);
            _this.resAttachFileIds.push(fileItems[i].id);
          }
        } else {
          Toast.warning(resp.message,Swal);
        }
      })
    },

    afterUpload(resp) {
      let _this = this;
      _this.resFile = resp.content;
      _this.resFile.customerId = _this.loginCustomer.id;
      _this.resFile.preProjectId = _this.characterItem.preProjectId;
      _this.resFile.operatorId = _this.loginUser.customerUserId;
      _this.resFile.itemId = _this.characterItem.id;
      _this.saveFile(_this.resFile); // 将上传完成的数据保存到数据库
      _this.$forceUpdate();
      // _this.onEditChang();
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

.a4-paper-big .table-out {
  width: var(--table-out-width, 100%);
  max-width: 22cm;
}
.a4-paper-big .table-inn {
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
  //margin-top: 20px;
  background: white;
  width: 27.3cm;
  min-height: 33cm;
  display: block;
  margin: 20px auto;
  padding: 10px 25px;
  margin-bottom: 0.5cm;
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
