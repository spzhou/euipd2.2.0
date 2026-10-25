<!--
 * Author: 周朔鹏
 * Date: 2025-02-23
 * Email: 391902958@qq.com
 * Website: https://www.euipd.com
-->

<!--本模板功能是仅对特性定义进行修改,不涉及的预算等事项-->
<template>
  <div  id="myContent" class="item-container" v-on:scroll="myContentScroll" style="display: flex; flex-direction: column;">
    <div class="a4-paper-big">
      <div>
        <!--特性编辑表-->
        <div>
          <character-detail-edit-core
              :key="('iteration-' + (randomInt))"
              ref="character_merge"
              :character="characterItem"
              :save-character-info="mergedSave"
          ></character-detail-edit-core>
        </div>
        <div style="margin: 15px 110px 20px 0">
          <FileUploadCore
              :input-id="('character-iteration-edit-' + (characterItem.id))"
              :key="('file-upload-' + (characterItem.id))"
              :x-project-id="getXProjectId"
              :item-id="characterItem.id"
              :file-group="FILE_GROUPING.CHARACTER.key"
              :project-type="projectType"
              :read-db-record="true"
              :en-delete="true"
              v-on:uploadFileList="getUploadFileList"
          ></FileUploadCore>
        </div>
        <div class="hr hr-18 dotted hr-double"></div>
        <el-row>
          <div style="display: flex; justify-content: space-evenly; align-items: center;">
            <div>
              <span>
                填表人 :
              </span>
              <span>
                {{ characterItem.writerName || loginUser.trueName }}
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
          <button type="button" v-on:click="saveCharacter('save')" class="ca-btn-primary">保存</button>
          <button type="button" v-on:click="saveCharacter('submit')" class="ca-btn-primary">提交</button>
        </div>
      </div>
    </div>

    <!--任务信息 显示对话框-->
    <el-dialog
        title="任务信息"
        width="29cm"
        :visible.sync="jobItemInfoVisible"
        :close-on-click-modal=true>
      <div>
        <JobitemDetailInfoDlg
            :key="('show- ' + (jobitemId))"
            ref="jobItemDetail_show"
            :jobitem-id="jobitemId">
        </JobitemDetailInfoDlg>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import FileRdms from "../../components/rdms/file-rdms";
import Swal from "sweetalert2";
import FileSaver from 'file-saver';


import CharacterDetailEditCore from "@/components/rdms/character-detail-edit-core";
import JobitemDetailInfoDlg from "@/components/std/jobitem-detail-info-dlg.vue";
import FileUploadCore from "@/components/std/file-upload-core.vue";


export default {
  components: {
    FileUploadCore,
    FileRdms,
    CharacterDetailEditCore,
    JobitemDetailInfoDlg,
  },
  name: "character-merge-edit",

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
  },

  computed: {
    getXProjectId(){
      if(this.projectType === PROJECT_TYPE.PRE_PROJECT.key){
        return  this.characterItem.preProjectId;
      }
      if(this.projectType === PROJECT_TYPE.PROJECT.key){
        return  this.characterItem.projectId;
      }
      if(this.projectType === PROJECT_TYPE.SUB_PROJECT.key || this.projectType === PROJECT_TYPE.DEV_PROJECT.key){
        return  this.characterItem.subprojectId;
      }
      return 0;
    },

  },

  data() {
    return {
      loginUser: {},
      loginCustomer: {},

      randomInt: 0,

      characterItem: {},
      characterItemEdit: {},
      dialogVisible: false,
      saveFlag: 'save',

      characterBudget: {},
      characterDecomposeList: [],
      jobItemInfoVisible: false,


      myContentHeight: 1000,

      FILE_GROUPING: FILE_GROUPING,
      CHARACTER_STATUS: CHARACTER_STATUS,
      JOB_ITEM_TYPE: JOB_ITEM_TYPE,

      resFile: {},
      resFiles: [],
      resFileIds: [],
      currentTimeStr: '',

    };
  },

  mounted: function () {
    let _this = this;
    _this.getLoginUser();

    if(Tool.isNotEmpty(_this.jobitemId)
        && Tool.isNotEmpty(_this.jobType)
        && _this.jobType === JOB_ITEM_TYPE.MERGE.key){
      _this.getMergedCharacterByJobitemId(_this.jobitemId);
    }
    _this.currentTime = new Date();
    _this.currentTimeStr = _this.currentTime.toLocaleString()

    _this.myContentHeight = document.getElementById("myContent").scrollHeight;
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
    getMergedCharacterByJobitemId(jobitemId) {
      let _this = this;
      let formData = new window.FormData();
      formData.append('jobitemId', jobitemId);
      formData.append('loginUserId', _this.loginUser.customerUserId);
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/character/getMergedCharacterByJobitemId', formData).then((response) => {
        let resp = response.data;
        if (resp.success) {
          _this.characterItem = resp.content;
          _this.randomInt = Math.random();
        }else{
          Toast.warning(resp.message, Swal);
        }
      });
    },

    myContentScroll(){
      let _this = this;
      _this.myContentHeight = document.getElementById("myContent").scrollHeight;
    },

    saveCharacter(flag){
      let _this = this;
      _this.saveFlag = flag;

      if(_this.saveFlag === 'submit'){
        Swal.fire({
          title: '确认',
          text: "是否确认提交?",
          icon: 'question',
          showCancelButton: true,
          confirmButtonColor: '#409EFF',
          cancelButtonColor: '#909399',
          confirmButtonText: '确认!',
          cancelButtonText: '取消!'
        }).then((result) => {
          if (result.value) {
            _this.$refs.character_merge.saveCharacterItem();
          }
        })
      }else{
        _this.$refs.character_merge.saveCharacterItem();
      }
    },

    mergedSave(characterInfo) {
      let _this = this;
      _this.characterItem.characterName = characterInfo.characterName;
      _this.characterItem.functionDescription = characterInfo.functionDescription;
      _this.characterItem.workCondition = characterInfo.workCondition;
      _this.characterItem.inputLogicalDesc = characterInfo.inputLogicalDesc;
      _this.characterItem.functionLogicalDesc = characterInfo.functionLogicalDesc;
      _this.characterItem.outputLogicalDesc = characterInfo.outputLogicalDesc;
      _this.characterItem.testMethod = characterInfo.testMethod;

      _this.characterItem.customerId = _this.loginCustomer.id;
      _this.characterItem.jobitemId = _this.jobitemId;
      _this.characterItem.projectType = _this.projectType;
      _this.characterItem.loginUserId = _this.loginUser.customerUserId;

      _this.characterItem.fileIds = _this.resFileIds;
      if(_this.saveFlag === 'submit'){
        _this.characterItem.status = CHARACTER_STATUS.SUBMIT.key;
      }else{
        _this.characterItem.status = CHARACTER_STATUS.SAVED.key;
      }

      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/character/saveMergeCharacter', _this.characterItem).then((response) => {
        let resp = response.data;
        if (resp.success) {
          Toast.success("保存成功", Swal);
          _this.characterItem = {};
          _this.resFiles = [];
          if(_this.saveFlag === 'submit'){
            _this.$bus.$emit('close-manhour-tab', _this.index);
            _this.$bus.$emit('notify-project-job-flag', true);  //项目管理
            _this.$bus.$emit('notify-jobitem-count-flag', true);  //执行管理
            _this.$bus.$emit('refresh-function-list', true);  //系统工程
          }
        } else {
          Toast.warning(resp.message, Swal);
        }
      })
    },
  },
}
</script>

<style lang="scss" scoped>
@import './../../../public/static/scss/caipd.scss';

.item-container {
  height: 100%;
  width: 100%;
  overflow-y: scroll;
  display: -webkit-box; /* 老版本语法: Safari, iOS, Android browser, older WebKit browsers. */
  display: -moz-box; /* 老版本语法: Firefox (buggy) */
  display: -ms-flexbox; /* 混合版本语法: IE 10 */
  display: -webkit-flex; /* 新版本语法: Chrome 21+ */
  display: flex; /* 新版本语法: Opera 12.1, Firefox 22+ */
}

</style>
