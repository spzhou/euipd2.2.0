<!--
 * Author: 周朔鹏
 * Date: 2025-02-23
 * Email: 391902958@qq.com
 * Website: https://www.euipd.com
-->

<!--本模板功能是仅对特性定义进行修改,不涉及的预算等事项-->
<template>
  <div  id="myContent" class="item-container" v-on:scroll="myContentScroll" style="display: flex; flex-direction: column;">
    <div class="a4-paper-big-simple">
      <div>
        <QualityOwnInfoPlain
            :key="qualityId"
            :quality-id="qualityId"
        ></QualityOwnInfoPlain>
      </div>
    </div>
  </div>
</template>

<script>
import FileRdms from "../../components/rdms/file-rdms";
import Swal from "sweetalert2";
import FileSaver from 'file-saver';


import JobitemDetailInfoDlg from "@/components/std/jobitem-detail-info-dlg.vue";
import FileUploadCore from "@/components/std/file-upload-core.vue";
import QualityAnalysisCore from "@/components/rdms/quality-analysis-core.vue";
import QualityOwnInfoPlain from "@/components/rdms/quality-own-info-plain.vue";


export default {
  components: {
    FileUploadCore,
    FileRdms,
    QualityAnalysisCore,
    JobitemDetailInfoDlg,
    QualityOwnInfoPlain,
  },
  name: "quality-info-page",

  props: {
    qualityId: {
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

  },

  data() {
    return {
      loginUser: {},
      loginCustomer: {},

      qualityItem: {},
      jobitemId: '',
      saveFlag: 'save',
      PROJECT_TYPE: PROJECT_TYPE,

      showRecheckBtn: true,

      myContentHeight: 1000,

      FILE_GROUPING: FILE_GROUPING,

      resFile: {},
      resFiles: [],
      resFileIds: [],
      currentTimeStr: '',

    };
  },

  mounted: function () {
    let _this = this;
    _this.getLoginUser();

    if(Tool.isNotEmpty(_this.qualityId )){
      _this.getQualityDetailInfo(_this.qualityId);
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

    getQualityDetailInfo(qualityId){
      let _this = this;
      let formData = new window.FormData();
      formData.append('qualityId', qualityId);
      formData.append('loginUserId', _this.loginUser.customerUserId);
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/quality/getQualityDetailInfo', formData).then((response)=>{
        let resp = response.data;
        if (resp.success) {
          _this.qualityItem = resp.content;
        } else {
          Toast.warning(resp.message,Swal);
        }
      })
    },

    myContentScroll(){
      let _this = this;
      _this.myContentHeight = document.getElementById("myContent").scrollHeight;
    },

    saveQualityAnalysis(flag){
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
            _this.$refs.qualityAnalysisComp.getQualityAnalysisData();
          }
        })
      }else{
        _this.$refs.qualityAnalysisComp.getQualityAnalysisData();
      }
    },

    saveQualityAnalysisData(qualityInfo) {
      let _this = this;

      qualityInfo.customerId = _this.loginCustomer.id;
      qualityInfo.projectType = PROJECT_TYPE.SUB_PROJECT.key;
      qualityInfo.executorId = _this.loginUser.customerUserId;
      qualityInfo.loginUserId = _this.loginUser.customerUserId;

      qualityInfo.fileListStr = JSON.stringify(_this.resFileIds);
      if(_this.saveFlag === 'submit'){
        qualityInfo.status = QUALITY_STATUS.RECHECK.key;
      }else{
        qualityInfo.status = QUALITY_STATUS.SAVE_ANALYSIS.key;
      }

      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/quality/saveAnalysis', qualityInfo).then((response) => {
        let resp = response.data;
        if (resp.success) {
          Toast.success("保存成功", Swal);
          if(_this.saveFlag === 'submit'){
            // _this.$bus.$emit('close-quality-tab', _this.index);
            _this.$bus.$emit('refresh-quality-list', true);
            _this.$bus.$emit('notify-quality-recheck-num', true);
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
