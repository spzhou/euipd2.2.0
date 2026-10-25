<!--
 * Author: 周朔鹏
 * Date: 2025-04-15
 * Email: 391902958@qq.com
 * Website: https://www.euipd.com
-->

<template>
  <div  id="myContent" class="item-container" v-on:scroll="myContentScroll" style="display: flex; flex-direction: column;">
    <div class="a4-paper-big" :style="{ height: contentHeight }">
      <div>
        <!--特性编辑表-->
        <div>
          <CbbDetailCreateCore
              :key="('demand-' + (random))"
              ref="cbb_define_create"
              :project-type="PROJECT_TYPE.SUB_PROJECT"
              :project-id="jobItem.subprojectId"
              :cbb-project-id="jobItem.subprojectId"
              :character-id="jobItem.characterId"
              :save-character-info = saveCharacterInfo
          ></CbbDetailCreateCore>
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
        <div v-if="characterItem" style="margin: 15px 50px 20px 0">
          <FileUploadCore
              :input-id="('character-demand-create')"
              :key="('file-upload-' + (characterItem.id))"
              :x-project-id="jobItem.subprojectId"
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
          <button type="button" @click="saveCbbDefineInfo('save')" class="ca-btn-primary">保存</button>
          <button type="button" @click="saveCbbDefineInfo('submit')" class="ca-btn-primary">提交</button>
        </div>
      </div>
    </div>

    <!--任务提交 对话框-->
    <el-dialog
        title="任务提交"
        width="29cm"
        :visible.sync="cbbSubmitVisible"
        :close-on-click-modal=false>
      <div v-if="jobItem">
        <CbbSubmitDlg
            :key="('submit- ' + (jobItem.id))"
            ref="cbb_define_dlg"
            :jobitem="jobItem"
            :job-type="jobItem.type"
            :project-type="jobItem.projectType"
            :index="index"
            :entrance="entrance"
            :close-dlg="submitDlgClose">
        </CbbSubmitDlg>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import FileRdms from "../../components/rdms/file-rdms";
import CharacterDemandEdit from "@/components/rdms/character-demand-edit";
import JobitemDetailInfoDlg from "@/components/std/jobitem-detail-info-dlg.vue";
import CbbDetailCreateCore from "@/components/rdms/cbb-detail-create-core.vue";
import CbbSubmitDlg from "@/components/std/cbb-submit-dlg.vue";

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
    CbbSubmitDlg,
    CharacterDetailCreateCore,
    CbbDetailCreateCore,
  },

  name: "cbb-define-create",

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
      cbbSimpleInfo: {},
      cbbSubmitVisible: false,

      characterItem: {
        devManhour: 0,
        testManhour: 0,
        materialFee: 0,
        otherFee: 0,
      },
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
    _this.characterItem.id = Uuid.shortUuid(8);
    _this.currentTime = new Date();
    _this.currentTimeStr = _this.currentTime.toLocaleString()
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
      _this.getCbbDefineCandidates(_this.jobitemId);
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

    getCbbDefineCandidates(jobitemId) {
      let _this = this;
      if (Tool.isEmpty(jobitemId)) {
        return;
      }
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/cbb/getCbbDefineCandidates/' + jobitemId).then((response) => {
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

    submitDlgClose(){
      let _this = this;
      _this.cbbSubmitVisible = false;
      },

    jobItemSubmitDlg(){
      let _this = this;
      _this.cbbSubmitVisible = true;
    },

    handleClose(done) {
      done();
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
          if (_this.jobItem.characterId) {
            _this.getCbbDefineRecord(_this.jobItem.characterId);
          } else {
            _this.getCbbSimpleInfoByCode(_this.jobItem.subprojectId);
          }
        }else{
          Toast.warning(resp.message, Swal);
        }
      });
    },

    getCbbDefineRecord(characterId) {
      let _this = this;
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/cbb/getCbbDefineData/' + characterId).then((response) => {
        let resp = response.data;
        if (resp.success) {
          if (resp.content) {
            _this.characterItem = resp.content;
            _this.cbbValue = _this.characterItem.moduleIdListStr ? JSON.parse(_this.characterItem.moduleIdListStr) : [];
          }
        } else {
          Toast.warning(resp.message, Swal);
        }
      })
    },

    getCbbSimpleInfoByCode(cbbProjectId) {
      let _this = this;
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/cbb/getCbbSimpleInfoByCode/' + cbbProjectId).then((response) => {
        let resp = response.data;
        if (resp.success) {
          if(resp.content){
            _this.characterItem = resp.content;
            //将json字符串转换为数组
            _this.cbbValue = JSON.parse(_this.characterItem.moduleIdListStr);
          }
        } else {
          Toast.warning(resp.message, Swal);
        }
      })
    },

    saveCbbDefineInfo(flag){
      let _this = this;
      _this.$refs.cbb_define_create.saveCharacterItem(flag);   //character 和 cbb 复用 data 表
    },

    saveCharacterInfo(cbbInfo) {
      let _this = this;
      _this.characterItem.characterName = cbbInfo.characterName;
      _this.characterItem.functionDescription = cbbInfo.functionDescription;
      _this.characterItem.workCondition = cbbInfo.workCondition;
      _this.characterItem.inputLogicalDesc = cbbInfo.inputLogicalDesc;
      _this.characterItem.functionLogicalDesc = cbbInfo.functionLogicalDesc;
      _this.characterItem.outputLogicalDesc = cbbInfo.outputLogicalDesc;
      _this.characterItem.testMethod = cbbInfo.testMethod;

      _this.characterItem.customerId = _this.jobItem.customerId;
      _this.characterItem.fileIds = _this.resFileIds;

      {
        _this.characterItem.projectType = _this.projectType;
        _this.characterItem.jobitemId = _this.jobItem.id;
        _this.characterItem.jobitemType = _this.jobType;
        _this.characterItem.loginUserId = _this.loginUser.customerUserId;
      }

      _this.characterItem.moduleIdListStr = JSON.stringify(_this.cbbValue);
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/cbb/saveRecordAndData', _this.characterItem).then((response) => {
        let resp = response.data;
        if (resp.success) {
          if(cbbInfo.flag === 'save'){
            Toast.success("保存成功", Swal);
          }else{
            _this.jobItemSubmitDlg();
          }
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
