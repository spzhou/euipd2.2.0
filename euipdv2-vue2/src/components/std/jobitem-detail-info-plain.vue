<!--
 * Author: 周朔鹏
 * Date: 2025-02-23
 * Email: 391902958@qq.com
 * Website: https://www.euipd.com
-->

<template>
  <div>
    <el-form label-width="100px" autocomplete="off">
      <el-form-item  label="任务名称:">
        {{ jobItem.jobName }}
      </el-form-item>
      <el-form-item  label="执行人:">
        {{ jobItem.executorName }}
      </el-form-item>
      <el-form-item  label="任务描述:">
        <div style="word-wrap: break-word;" v-rich-html="jobItem.taskDescription"></div>
      </el-form-item>
      <div v-if="jobItem.propertyDto && jobItem.propertyDto.jobDescription">
        <el-form-item label="工作描述:">
          <div style="word-wrap: break-word;" v-rich-html="jobItem.propertyDto.jobDescription"></div>
        </el-form-item>
        <el-form-item label="交付附件:" v-if="jobItem.propertyDto.fileItems && jobItem.propertyDto.fileItems.length > 0">
          <div v-for="(item, i ) in jobItem.propertyDto.fileItems" :key="item.id+'-'+i" style="display: flex; flex-direction: row; margin-left: 16px;">
            <div v-if="item">
              {{ item.name }}
            </div>
            <div v-if="item && item.authStatus">
              <el-button type="text" style="margin-left: 25px;" @click="downloadFile(item)">下载</el-button>
            </div>
          </div>
        </el-form-item>
      </div>
    </el-form>
  </div>
</template>

<script>
import File from "../rdms/file.vue";
import FileSaver from 'file-saver';
import Swal from "sweetalert2";
import printJS from "print-js";
import CharacterDetailInfoCore from "@/components/std/character-detail-info-core.vue";
import CharacterItemModify from "@/components/rdms/character-item-modify.vue";
import {formatSecond} from "@/filter/filter";
import CharacterDetailInfoCoreAll from "@/components/std/character-detail-info-core-all.vue";

export default {

  components: {
    File,
    FileSaver,
    CharacterDetailInfoCore,
    CharacterItemModify,
    CharacterDetailInfoCoreAll,
  },
  name: "jobitem-detail-info-plain",

  props: {
    jobitemId: {
      default: null,
    },
  },

  data() {
    return {
      loginUser: {},
      loginCustomer: {},

      jobItem: {
        default: null,
      },

      jobItemProcessList: [],
      jobitemProperty: {},
      reviewJobitem: {},
      isReviewNotifyFlag: false,

      fileItems: [],
      resFile: {},
      resFiles: [],
      resFileIds: [],
      detailDialogVisible: false,

      dialogTitle: 'Dialog',

      characterInfo: {},
      JOB_ITEM_PROCESS: JOB_ITEM_PROCESS,
      DOC_TYPE: DOC_TYPE,
      JOB_ITEM_STATUS: JOB_ITEM_STATUS,

      printData: {
        printable: 'editing-print',
        type: 'html',
        ignore: ['no-print'],
        targetStyles: ['*'],  //css样式，写成*代表打印样式完全继承你页面的样式
        // maxWidth:'800px'  //打印界面最大宽度，适当调整可以解决打印页面过宽，显示不完整的问题
      }
    };
  },

  computed: {
    remainTime(){
      let _this = this;
      let planSubmissionTime = new Date(_this.jobItem.planSubmissionTime).getTime();
      let left = planSubmissionTime/1000 - Date.now()/1000;
      return  formatSecond(left);
    },

    hasCharacter(){
      let _this = this;
      if(Tool.isNotEmpty_ES5(_this.characterInfo)){
        return true;
      }else{
        return false;
      }

    }
  },

  mounted: function () {
    let _this = this;
    _this.getLoginUser();
    if(Tool.isNotEmpty(_this.jobitemId)){
      _this.getJobitemRecordDetailInfo(_this.jobitemId);

      if(Tool.isNotEmpty(_this.jobItem.type) && _this.jobItem.type === JOB_ITEM_TYPE.REVIEW_NOTIFY.key){
        _this.isReviewNotifyFlag = true;
        _this.getReviewNotifyInfo(_this.jobitemId);
      }
    }
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

    getJobitemRecordDetailInfo(jobitemId){
      let _this = this;
      let formData = new window.FormData();
      formData.append('jobitemId', jobitemId);
      formData.append('loginUserId', _this.loginUser.customerUserId);
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/job-items/getJobitemRecordDetailInfo', formData).then((response) => {
        let resp = response.data;
        if (resp.success) {
          _this.jobItem = resp.content;
          _this.getJobItemProcessInfos(jobitemId);
          if(_this.jobItem.characterId){
            _this.getCharacterSimpleInfo(_this.jobItem.characterId)
          }

        }else{
          Toast.warning(resp.message, Swal);
        }
      });
    },

    getJobItemProcessInfos(jobitemId){
      let _this = this;
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/job-item-process/get-jobitem-process-info-list/'+ jobitemId).then((response)=>{
        let resp = response.data;
        if (resp.success) {
          _this.jobItemProcessList = resp.content;
        } else {
          Toast.warning(resp.message, Swal);
        }
      })
    },

    getReviewNotifyInfo(jobitemId) {
      let _this = this;
      let formData = new window.FormData();
      formData.append('jobitemId', jobitemId);
      formData.append('loginUserId', _this.loginUser.customerUserId);
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/job-items/getReviewNotifyInfo', formData).then((response) => {
        let resp = response.data;
        if (resp.success) {
          _this.reviewJobitem = resp.content.reviewJobitemInfo.jobitem;
        } else {
          Toast.warning(resp.message, Swal);
        }
      });
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

    characterDetailDlg(characterId) {
      let _this = this;
      _this.getCharacterSimpleInfo(characterId);
      _this.detailDialogVisible = true;
      _this.dialogTitle = "特性定义表";
    },

    getCharacterSimpleInfo(characterId){
      let _this = this;
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/character/getCharacterSimpleInfo/'+ characterId).then((response)=>{
        let resp = response.data;
        if (resp.success) {
          _this.characterInfo = resp.content;
        } else {
          Toast.warning(resp.message, Swal);
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

    handleClose(done) {
      done(); //el-dialog用于关闭对话框的内置函数.
    },

    closeDialog() {
      let _this = this;
      _this.detailDialogVisible = false;
    },

  },
}
</script>

<style lang="scss" scoped>
@import './../../../public/static/scss/caipd.scss';

.form-group>label[class*=col-] {
   margin-bottom: 0 !important;
}
.form-group{
  margin-bottom: 0 !important;
}

</style>
