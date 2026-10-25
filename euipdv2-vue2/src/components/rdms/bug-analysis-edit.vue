<!--
 * Author: 周朔鹏
 * Date: 2025-02-25
 * Email: 391902958@qq.com
 * Website: https://www.euipd.com
-->

<!--本模板功能是仅对特性定义进行修改,不涉及的预算等事项-->
<template>
  <div  id="myContent" class="item-container" v-on:scroll="myContentScroll" style="display: flex; flex-direction: column;">
    <div class="a4-paper-big-simple">
      <div>
        <div>
          <BugAnalysisCore
              :key="('bug-analysis-' + (bugfeedbackItem.id))"
              ref="bugAnalysisComp"
              :bugfeedback="bugfeedbackItem"
              :get-bug-analysis-info="saveBugAnalysisData"
          ></BugAnalysisCore>
        </div>
        <div class="hr hr-10 hr-dotted"></div>
        <div style="margin: 15px 70px 20px 0">
          <FileUploadCore
              :input-id="('quality-analysis-edit-' + (jobItem.id))"
              :key="('file-upload-' + (jobItem.id))"
              :x-project-id="jobItem.subprojectId"
              :item-id="jobItem.id"
              :file-group="FILE_GROUPING.PROPERTY.key"
              :project-type="PROJECT_TYPE.SUB_PROJECT.key"
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
        <div class="clearfix form-actions" style="margin-top: 30px;">
          <div style="display: flex; justify-content: center;">
            <button type="button" v-on:click="saveBugAnalysis('save')" class="ca-btn-primary">保 存</button>
            <button v-if="showRecheckBtn" type="button" v-on:click="saveBugAnalysis('submit')" class="ca-btn-primary">提 交</button>
            <button v-if="!showRecheckBtn" type="button" class="ca-btn-primary disabled">提 交</button>
          </div>
        </div>
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
import BugAnalysisCore from "@/components/rdms/bug-analysis-core.vue";

export default {
  components: {
    FileUploadCore,
    FileRdms,
    BugAnalysisCore,
    JobitemDetailInfoDlg,
  },
  name: "bug-analysis-edit",

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

  },

  data() {
    return {
      loginUser: {},
      loginCustomer: {},

      bugfeedbackItem: {},
      jobItem: {},
      saveFlag: 'save',
      PROJECT_TYPE: PROJECT_TYPE,

      // characterPlainList: [],
      showRecheckBtn: true,
      notCompleteNum: 0,

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

    if(Tool.isNotEmpty(_this.jobitemId )){
      _this.getJobitemSimpleInfo(_this.jobitemId);
      _this.getChildrenJobitemNotCompleteNum(_this.jobitemId);
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

    getJobitemSimpleInfo(jobitemId){
      let _this = this;
      let formData = new window.FormData();
      formData.append('jobitemId', jobitemId);
      formData.append('loginUserId', _this.loginUser.customerUserId);
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/job-items/getJobitemSimpleInfo', formData).then((response) => {
        let resp = response.data;
        if (resp.success) {
          _this.jobItem = resp.content;
          _this.getBugDetailInfo(_this.jobItem.qualityId);  //共用qualityId 其实是 bugfeedbackId
        }else{
          Toast.warning(resp.message, Swal);
        }
      });
    },

    getChildrenJobitemNotCompleteNum(jobitemId) {
      let _this = this;
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/job-items/getChildrenJobitemNotCompleteNum/' + jobitemId).then((response) => {
        let resp = response.data;
        if (resp.success) {
          _this.notCompleteNum = resp.content;
          if(_this.notCompleteNum && _this.notCompleteNum>0){
            _this.showRecheckBtn = false;
          }
        } else {
          Toast.warning(resp.message, Swal);
        }
      })
    },

    getBugDetailInfo(bugfeedbackId){
      let _this = this;
      let formData = new window.FormData();
      formData.append('bugfeedbackId', bugfeedbackId);
      formData.append('loginUserId', _this.loginUser.customerUserId);
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/bugfeedback/getBugDetailInfo', formData).then((response)=>{
        let resp = response.data;
        if (resp.success) {
          _this.bugfeedbackItem = resp.content;
        } else {
          Toast.warning(resp.message,Swal);
        }
      })
    },

    myContentScroll(){
      let _this = this;
      _this.myContentHeight = document.getElementById("myContent").scrollHeight;
    },

    saveBugAnalysis(flag){
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
            _this.$refs.bugAnalysisComp.getBugAnalysisData();
          }
        })
      }else{
        _this.$refs.bugAnalysisComp.getBugAnalysisData();
      }
    },

    saveBugAnalysisData(bugAnalysisInfo) {
      let _this = this;

      bugAnalysisInfo.customerId = _this.loginCustomer.id;
      bugAnalysisInfo.projectType = PROJECT_TYPE.SUB_PROJECT.key;
      bugAnalysisInfo.executorId = _this.loginUser.customerUserId;
      bugAnalysisInfo.loginUserId = _this.loginUser.customerUserId;
      bugAnalysisInfo.jobitemId = _this.jobitemId;

      bugAnalysisInfo.jobitemPropertyFileIdListStr = JSON.stringify(_this.resFileIds);
      if(_this.saveFlag === 'submit'){
        bugAnalysisInfo.status = QUALITY_STATUS.JOB_SUBMIT.key;
      }else{
        bugAnalysisInfo.status = QUALITY_STATUS.SAVE_ANALYSIS.key;
      }

      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/bugfeedback/saveAnalysis', bugAnalysisInfo).then((response) => {
        let resp = response.data;
        if (resp.success) {
          Toast.success("保存成功", Swal);
          if(_this.saveFlag === 'submit'){
            _this.$bus.$emit('close-manhour-tab', _this.index);
            _this.$bus.$emit('refresh-manhour-jobitem-list', true); //刷新任务列表
            _this.$bus.$emit('notify-jobitem-count-flag', true);  //执行管理
            _this.$bus.$emit('notify-quality-count-flag', true);  //执行管理
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
