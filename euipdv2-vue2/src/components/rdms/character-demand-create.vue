<!--
 * Author: 周朔鹏
 * Date: 2025-04-14
 * Email: 391902958@qq.com
 * Website: https://www.euipd.com
-->

<template>
  <div  id="myContent" class="item-container" v-on:scroll="myContentScroll" style="display: flex; flex-direction: column;">
    <div style="width: 26.3cm; max-width: calc(100% - 24px); margin: 0 auto;">
      <div v-if="characterList.length>0">
        <div style="font-weight: bold; font-size: 15px;">特性定义文件列表:</div>
        <span v-for="(item, index) in characterList" :key="index" style="font-weight: bold;">
          <a href="javascript:void(0);" @click="editCharacter(item)">《{{ item.characterName}}》</a>
        </span>
      </div>
      <div style="margin-top: 10px;">
        <div style="float: left; margin-top: 20px;">说明: 根据具体情况, 您可以根据需求定义多条功能.</div>
        <el-button
            v-if="characterList.length>0"
            @click="jobItemSubmitDlg" type="primary" style="float: right;">
          提交任务
        </el-button>
      </div>
    </div>

    <div class="a4-paper-big" :style="{ height: contentHeight }">
      <div>
        <!--特性编辑表-->
        <div>
          <character-detail-create-core
              :key="('demand-' + (random))"
              ref="demand_character_create"
              :project-type="PROJECT_TYPE.PRE_PROJECT"
              :project-id="jobItem.preProjectId"
              :save-character-info = demandCharacterSave
          ></character-detail-create-core>
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
        <div style="margin: 15px 50px 20px 0">
          <FileUploadCore
              :input-id="('character-demand-create')"
              :key="('file-upload-' + (characterItem.id))"
              :x-project-id="jobItem.preProjectId"
              :item-id="characterItem.id"
              :file-group="FILE_GROUPING.CHARACTER.key"
              :project-type="projectType"
              :read-db-record="true"
              :en-delete="true"
              v-on:uploadFileList="getUploadFileList"
          ></FileUploadCore>
        </div>
        <div class="hr hr-18 dotted"></div>
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
          <button type="button" v-on:click="saveDemandCharacter()" class="ca-btn-primary">保存</button>
<!--          <button type="button" v-on:click="reset()" class="ca-btn-primary">重填</button>-->
        </div>
      </div>
    </div>

    <!--对新分解的功能特性进行再编辑-->
    <el-dialog
        title="编辑特性定义表"
        :visible.sync="dialogVisible"
        destroy-on-close
        width="32cm"
        :close-on-click-modal=false
        :before-close="handleClose">
      <character-demand-edit
          :key="editCharacterId"
          :character-id="editCharacterId"
          :close-dlg="closeDialog"
      ></character-demand-edit>
    </el-dialog>

    <!--任务提交 对话框-->
    <el-dialog
        title="任务提交"
        width="29cm"
        :visible.sync="jobItemSubmitVisible"
        :close-on-click-modal=false>
      <div>
        <JobitemSubmitDlg
            :key="('submit- ' + (jobItem.id))"
            ref="jobItemSubmit_demand_dlg"
            :jobitem="jobItem"
            :job-type="jobItem.type"
            :project-type="jobItem.projectType"
            :index="index"
            :entrance="entrance"
            :close-dlg="submitDlgClose">
        </JobitemSubmitDlg>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import FileRdms from "../../components/rdms/file-rdms";
import CharacterDemandEdit from "@/components/rdms/character-demand-edit";
import JobitemDetailInfoDlg from "@/components/std/jobitem-detail-info-dlg.vue";
import JobitemSubmitDlg from "@/components/std/jobitem-submit-dlg.vue";

import Swal from "sweetalert2";
import FileSaver from 'file-saver';

import CharacterDetailCreateCore from "@/components/rdms/character-detail-create-core";
import FileUploadCore from "@/components/std/file-upload-core.vue";

export default {
  components: {
    FileUploadCore,
    FileRdms,
    CharacterDemandEdit,
    JobitemDetailInfoDlg,
    JobitemSubmitDlg,
    CharacterDetailCreateCore,
  },

  name: "character-demand-create",

  props: {
    jobitemId: {
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
    refresh: {
      type: Function,
      default: null
    },
  },

  computed: {
    contentHeight(){
      return this.myContentHeight+120 +'px';
    },
  },

  data() {
    return {
      loginUser: {},
      loginCustomer: {},

      preProjectId: '',
      jobItem: {},
      // jobItemInfoVisible: false,
      jobItemSubmitVisible: false,

      characterItem: {
        devManhour: 0,
        testManhour: 0,
        materialFee: 0,
        otherFee: 0,
      },
      dialogVisible: false,
      editCharacterId: '',
      random: 0,

      characterBudget: {},
      characterList: [],

      myContentHeight: 1000,

      FILE_GROUPING: FILE_GROUPING,
      CHARACTER_STATUS: CHARACTER_STATUS,
      PROJECT_TYPE: PROJECT_TYPE,
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
    if(Tool.isNotEmpty(_this.jobitemId)){
      _this.getJobitemDetailInfo(_this.jobitemId);
    }

    _this.currentTime = new Date();
    _this.currentTimeStr = _this.currentTime.toLocaleString()
    _this.characterItem.id = Uuid.shortUuid(8);

    _this.myContentHeight = document.getElementById("myContent").scrollHeight;
  },

  methods: {
    getLoginUser(){
      let _this = this;
      if(Tool.getLoginUser().userGroup === LOGIN_USER_GROUP.CUSTOMER.key){
        _this.loginUser = Tool.getLoginUser().adminUser;
        // console.log("loginUser:", _this.loginUser);
        _this.loginCustomer =Tool.getLoginUser();
      }else{
        _this.loginUser = Tool.getLoginUser();
        _this.loginCustomer = Tool.getLoginUser().customer;
      }
      _this.getCbbListByCustomerId(_this.loginCustomer.id);
    },

    getUploadFileList(resFiles){
      let _this = this;
      _this.resFiles = resFiles;
      if(_this.resFiles !== null){
        _this.resFileIds= [];
        if(resFiles && resFiles.length>0){
        resFiles.forEach(item => {
          _this.resFileIds.push(item.id);
        })
      }
      }
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

    closeDialog(){
      let _this = this;
      _this.dialogVisible = false;
      if (Tool.isNotEmpty(_this.jobitemId)) {
        _this.getDemandCharacterList(_this.jobitemId);
      }
      },

    submitDlgClose(){
      let _this = this;
      _this.jobItemSubmitVisible = false;
      },

    editCharacter(item){
      let _this = this;
      _this.editCharacterId = item.id;
      _this.dialogVisible = true;
    },

    jobItemSubmitDlg(){
      let _this = this;
      _this.jobItemSubmitVisible = true;
    },

    handleClose(done) {
      let _this = this;
      Swal.fire({
        title: '确认',
        text: "如果有修订, 关闭前请及时保存",
        icon: 'question',
        showCancelButton: true,
        confirmButtonColor: '#409EFF',
        cancelButtonColor: '#909399',
        confirmButtonText: '确认!',
        cancelButtonText: '取消!'
      }).then((result) => {
        if (result.isConfirmed) {
          _this.dialogVisible = false;
          done();
          _this.refresh();
        }
      }).catch(_ => {
      });
    },

    myContentScroll(){
      let _this = this;
      _this.myContentHeight = document.getElementById("myContent").scrollHeight;
    },

    getJobitemDetailInfo(jobitemId){
      let _this = this;
      let formData = new window.FormData();
      formData.append('jobitemId', jobitemId);
      formData.append('loginUserId', _this.loginUser.customerUserId);
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/job-items/getJobitemDetailInfo', formData).then((response) => {
        let resp = response.data;
        if (resp.success) {
          _this.jobItem = resp.content;
          _this.random = Math.random();
          _this.getDemandCharacterList(jobitemId);
        }else{
          Toast.warning(resp.message, Swal);
        }
      });
    },

    getDemandCharacterList(jobitemId){
      let _this = this;
      let formData = new window.FormData();
      formData.append('jobitemId', jobitemId);
      formData.append('loginUserId', _this.loginUser.customerUserId);
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/character/getCharacterListByJobitemId', formData).then((response) => {
        let resp = response.data;
        if (resp.success) {
          _this.characterList = resp.content;
        }else{
          Toast.warning(resp.message, Swal);
        }
      });
    },

    saveDemandCharacter(){
      let _this = this;
      _this.$refs.demand_character_create.saveCharacterItem();
    },

    demandCharacterSave(characterInfo) {
      let _this = this;
      _this.characterItem.iterationVersion = 1;

      _this.characterItem.characterName = characterInfo.characterName;
      _this.characterItem.functionDescription = characterInfo.functionDescription;
      _this.characterItem.workCondition = characterInfo.workCondition;
      _this.characterItem.inputLogicalDesc = characterInfo.inputLogicalDesc;
      _this.characterItem.functionLogicalDesc = characterInfo.functionLogicalDesc;
      _this.characterItem.outputLogicalDesc = characterInfo.outputLogicalDesc;
      _this.characterItem.testMethod = characterInfo.testMethod;

      _this.characterItem.customerId = _this.jobItem.customerId;
      _this.characterItem.demandListStr = _this.jobItem.demandIdStr;

      _this.characterItem.preProjectId = _this.jobItem.preProjectId;
      _this.characterItem.productManagerId = _this.jobItem.productManagerId;
      _this.characterItem.writerId = _this.loginUser.customerUserId;
      _this.characterItem.fileIds = _this.resFileIds;

      {
        _this.characterItem.jobitemId = _this.jobItem.id;
        _this.characterItem.jobitemType = _this.jobType;
        _this.characterItem.projectType = _this.projectType;
        _this.characterItem.loginUserId = _this.loginUser.customerUserId;
      }

      _this.characterItem.moduleIdListStr = JSON.stringify(_this.cbbValue);
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/character/saveRecordAndData', _this.characterItem).then((response) => {
        let resp = response.data;
        if (resp.success) {
          Toast.success("保存成功", Swal);
          _this.characterItem = {};
          _this.resFiles = [];
          _this.refresh(); //刷新组建
        } else {
          Toast.warning(resp.message, Swal);
        }
      })

    },

    reset(){
      let _this = this;
      _this.characterItem = {
        devManhour: 0,
        testManhour: 0,
        materialFee: 0,
        otherFee: 0,
      };
      _this.resFiles = [];
      if(!_this.characterItem.iterationVersion){
        _this.characterItem.iterationVersion = 1;
      }
    },

    afterUpload(resp) {
      let _this = this;
      _this.resFile = resp.content;
      // debugger
      _this.resFile.customerId = _this.loginCustomer.id;
      _this.resFile.preProjectId = _this.jobItem.preProjectId;
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
/*
.a4-paper-big {
  background: white;
  width: 26.3cm;
  min-height: 29.7cm;
  display: block;
  margin: 20px auto;
  padding: 10px 25px;
  margin-bottom: 0.5cm;
  box-shadow: 0 0 0.5cm rgba(0, 0, 0, 0.5);
  overflow-y: scroll;
  box-sizing: border-box;
}*/
</style>
