<!--
 * Author: 周朔鹏
 * Date: 2024-11-05
 * Email: 391902958@qq.com
 * Website: https://www.euipd.com
-->

<template>
  <div style="height: 100%;">
    <div class="paper-height">
      <div style="height: 100%; overflow: scroll; ">
        <div class="a4-paper-big-simple">
          <div style="margin: 30px 0 0;">
            <el-row style="margin-top: 10px; margin-left: 55px; padding-right: 20px;">
              <el-col :span="23" >
                <div style="display: flex; justify-content: space-between;">
                  <div>任务号: {{ jobItem.jobSerial }}</div>
                  <div>任务状态: <a href="javascript:void(0);" @click="showJobitemProcess"> {{ JOB_ITEM_PROCESS | optionKV(jobItem.processStatus) }} </a></div>
                </div>

              </el-col>
            </el-row>
            <div>
              <table class="table-out">
                <tr>
                  <table class="table-inn">
                    <tr>
                      <th style="width: 120px; text-align: center;">
                        <span>项目名称</span>
                      </th>
                      <th>
                  <span style="margin-left: 10px;">
                    {{ jobItem.projectName === null ? jobItem.preProjectName : jobItem.projectName }}
                  </span>
                      </th>
                      <th style="width: 120px; text-align: center;">
                        <span>团队名称</span>
                      </th>
                      <th>
                  <span style="margin-left: 10px;">
                    {{ jobItem.customerName }}
                  </span>
                      </th>
                    </tr>
                    <tr>
                      <th style="width: 120px; text-align: center;">
                        <span>任务名称</span>
                      </th>
                      <th>
                  <span style="margin-left: 10px;">
                    {{ jobItem.jobName }}
                  </span>
                      </th>
                      <th style="width: 120px; text-align: center;">
                        <span>子项目名称</span>
                      </th>
                      <th>
                  <span style="margin-left: 10px;">
                   {{ jobItem.subprojectName }}
                  </span>
                      </th>
                    </tr>
                    <tr>
                      <th style="width: 120px; text-align: center;">
                        <span>工作执行人</span>
                      </th>
                      <th>
                  <span style="margin-left: 10px;">
                    {{ jobItem.executorName }}
                  </span>
                      </th>
                      <th style="width: 120px; text-align: center;">
                        <span>任务工时</span>
                      </th>
                      <th>
                  <span style="margin-left: 10px;">
                   {{ jobItem.manhour }}
                  </span>
                      </th>
                    </tr>
                    <tr>
                      <th style="width: 120px; text-align: center;">
                        <span>产品经理</span>
                      </th>
                      <th>
                  <span style="margin-left: 10px;">
                    {{ jobItem.productManagerName }}
                  </span>
                      </th>
                      <th style="width: 120px; text-align: center;">
                        <span>项目经理</span>
                      </th>
                      <th>
                  <span style="margin-left: 10px;">
                   {{ jobItem.projectManagerName }}
                  </span>
                      </th>
                    </tr>
                    <tr>
                      <th style="width: 120px; text-align: center;">
                        <span>计划提交时间</span>
                      </th>
                      <th>
                  <span style="margin-left: 10px;">
                    {{ jobItem.planSubmissionTime | dateFormat }}
                  </span>
                      </th>
                      <th style="width: 120px; text-align: center;">
                        <span>提交倒计时</span>
                      </th>
                      <th>
                  <span style="margin-left: 10px;">
                   {{ remainTime }}
                  </span>
                      </th>
                    </tr>
                  </table>
                </tr>
                <tr>
                  <table class="table-inn">
                    <tr style="border-top: 2px solid #1a1a1a;">
                      <th style="width: 120px; text-align: center;">
                        <span>任务描述</span>
                      </th>
                      <th>
                  <span style="margin-left: 10px;">
                    内容说明: 以下为任务下发人对工作要求的描述.
                  </span>
                      </th>
                    </tr>
                  </table>
                </tr>
                <tr>
                  <table class="table-inn">
                    <tr>
                      <th>
                  <span style="margin-left: 10px;">
                    {{ jobItem.taskDescription }}
                  </span>
                      </th>
                    </tr>
                  </table>
                </tr>
                <!--  任务附件-->
                <tr  v-show="jobItem.fileList !==null && jobItem.fileList !== undefined && jobItem.fileList.length > 0">
                  <table class="table-inn">
                    <tr style="border-top: 2px solid #1a1a1a;">
                      <th style="width: 120px; text-align: center;">
                        <span>任务附件</span>
                      </th>
                      <th>
                  <span style="margin-left: 10px;">
                    内容说明: 任务任务下发时, 提供的参考附件.
                  </span>
                      </th>
                    </tr>
                  </table>
                </tr>
                <tr  v-show="jobItem.fileList !==null && jobItem.fileList !== undefined && jobItem.fileList.length > 0">
                  <table class="table-inn">
                    <tr>
                      <th>
                        <div v-for="file in jobItem.fileList" :key="file.id"
                             style="display: flex; flex-direction: row; align-items: center;">
                          <div style="margin-top: 3px; margin-left: 10px;">
                            {{ file.name }}
                          </div>
                          <div v-if="file && file.authStatus">
                            <el-button type="text" style="margin-left: 15px; margin-right: 25px;" @click="downloadFile(file)">下载
                            </el-button>
                          </div>
                        </div>
                      </th>
                    </tr>
                  </table>
                </tr>
              </table>
            </div>
            <div class="hr hr-8 dotted"></div>
              <MilestoneReviewCreatePage
                  :key="jobItem.id"
                  :origin-job-item="jobItem"
                  :project-type="projectType"
                  :index="index"
                  :entrance="entrance"
              ></MilestoneReviewCreatePage>
          </div>
        </div>
      </div>
    </div>

    <!--显示任务过程信息-->
    <el-dialog
        width="29cm"
        :visible.sync="showJobitemProcessDlg"
        :before-close="handleClose"
        :close-on-click-modal=false>
      <div slot="title" style="font-size: 20px;">{{ dialogTitle }}</div>
      <div>
        <JobitemDetailInfoDlg
            :key="('detail-' + (jobitemId))"
            :jobitem-id="jobItem.id"
        ></JobitemDetailInfoDlg>
      </div>
    </el-dialog>

  </div>
</template>

<script>
import FileRdms from "@/components/rdms/file-rdms";
import FileSaver from 'file-saver';
import Swal from "sweetalert2";
import printJS from "print-js";
import CharacterBudgetConfirmDlg from "@/components/rdms/character-budget-confirm-dlg";
import JobitemDetailInfoDlg from "@/components/std/jobitem-detail-info-dlg.vue";

import {formatSecond, optionKV} from "@/filter/filter";
import FileUploadCore from "@/components/std/file-upload-core.vue";
import MilestoneReviewCreatePage from "@/components/std/milestone-review-create-page.vue";


export default {
  components: {
    FileUploadCore,
    FileRdms,
    FileSaver,
    CharacterBudgetConfirmDlg,
    JobitemDetailInfoDlg,
    MilestoneReviewCreatePage,

  },
  name: "jobitem-milestone-review-create-page",

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


  data() {
    return {
      loginUser: {},
      loginCustomer: {},

      PROJECT_TYPE: PROJECT_TYPE,
      FILE_GROUPING: FILE_GROUPING,
      JOB_ITEM_PROCESS: JOB_ITEM_PROCESS,
      DOC_TYPE: DOC_TYPE,

      showJobitemProcessDlg: false,
      dialogTitle: '',

      currentTime: {},
      currentTimeStr: '',

      jobItem: {},
      jobitemProperty: {
        jobDescription: "",
        testKeyPoints: "",
      },

      characterItem: {},

      resFile: {},
      resFiles: [],
      resFileIds: [],

      random: 0,

      printData: {
        printable: 'examine-print',
        type: 'html',
        // header: this.characterItem.preProjectName,
        // headerStyle: 'font-weight: 600; font-size: 30px;',
        ignore: ['no-print'],
        targetStyles: ['*'],  //css样式，写成*代表打印样式完全继承你页面的样式
        // maxWidth:'800px'  //打印界面最大宽度，适当调整可以解决打印页面过宽，显示不完整的问题
      },

    };
  },

  computed: {
    getXProjectId(){
      if(this.projectType === PROJECT_TYPE.PRE_PROJECT.key){
        return  this.jobItem.preProjectId;
      }
      if(this.projectType === PROJECT_TYPE.PROJECT.key){
        return  this.jobItem.projectId;
      }
      if(this.projectType === PROJECT_TYPE.SUB_PROJECT.key || this.projectType === PROJECT_TYPE.DEV_PROJECT.key){
        return  this.jobItem.subprojectId;
      }
      return 0;
    },

    getJobDescriptionFontNum() {
      // console.log( "this.jobitemProperty?.jobDescription", this.jobitemProperty?.jobDescription);
      if (this.jobitemProperty?.jobDescription) {
        return Tool.richTextLength(this.jobitemProperty.jobDescription);
      }
      return 0;
    },
    getTestKeyPointFontNum() {
      if (this.jobitemProperty?.testKeyPoints) {
        return Tool.richTextLength(this.jobitemProperty.testKeyPoints);
      }
      return 0;
    },
    remainTime(){
      let _this = this;
      let planSubmissionTime = new Date(_this.jobItem.planSubmissionTime).getTime();
      let left = planSubmissionTime/1000 - Date.now()/1000;
      return  formatSecond(left);
    },
  },

  mounted: function () {
    let _this = this;
    _this.random = Math.random();
    _this.currentTime = new Date();
    _this.currentTimeStr = _this.currentTime.toLocaleString()
    _this.getLoginUser();
    if(Tool.isNotEmpty(_this.jobitemId)){
      _this.getJobitemDetailInfo(_this.jobitemId);
    }
  },

  beforeDestroy() {
  },

  methods: {
    getLoginUser() {
      let _this = this;
      if (Tool.getLoginUser().userGroup === LOGIN_USER_GROUP.CUSTOMER.key) {
        _this.loginUser = Tool.getLoginUser().adminUser;
        _this.loginCustomer = Tool.getLoginUser();
      } else {
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

    getJobitemDetailInfo(jobitemId){
      let _this = this;
      let formData = new window.FormData();
      formData.append('jobitemId', jobitemId);
      formData.append('loginUserId', _this.loginUser.customerUserId);
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/job-items/getJobitemDetailInfo', formData).then((response) => {
        let resp = response.data;
        if (resp.success) {
          _this.jobItem = resp.content;
          _this.getJobitemProperty(_this.jobItem.id);
        }else{
          Toast.warning(resp.message, Swal);
        }
      });
    },

    getJobitemProperty(jobitemId) {
      let _this = this;
      let formData = new window.FormData();
      formData.append('jobitemId', jobitemId);
      formData.append('loginUserId', _this.loginUser.customerUserId);
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/jobitem-property/getJobItemProperty', formData).then((response)=>{
        let resp = response.data;
        if (resp.success) {
          if(Tool.isEmpty(resp.content)){
            _this.jobitemProperty.jobDescription = '';
            _this.jobitemProperty.testKeyPoints = '';
          }else{
            _this.jobitemProperty = resp.content;
            _this.fileItems = _this.jobitemProperty.fileItems;
            // debugger
            /*if(_this.fileItems !== null){
              for(let i= 0 ; i < _this.fileItems.length; i++){
                _this.resFiles.push(_this.fileItems[i]);
                _this.resFileIds.push(_this.fileItems[i].id);
              }
            }*/
          }

        } else {
          Toast.warning(resp.message,Swal);
        }
      })
    },

    saveJobitemProperty() {
      let _this = this;
      // 保存校验
      if (1 !== 1
          || !Validator.require(_this.jobitemProperty.jobDescription, "工作描述", Swal)
          || !Validator.require(_this.jobitemProperty.testKeyPoints, "测试/验收关键点说明", Swal)

          || !Validator.richLength(_this.jobitemProperty.jobDescription, "工作描述", 10, 5000, Swal)
          || !Validator.richLength(_this.jobitemProperty.testKeyPoints, "测试/验收关键点说明", 10, 5000, Swal)
      ) {
        return;
      }
      _this.jobitemProperty.loginUserId = _this.loginUser.customerUserId;
      _this.jobitemProperty.fileListStr = JSON.stringify(_this.resFileIds);
      _this.jobitemProperty.jobItemId = _this.jobitemId;

      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/jobitem-property/saveJobitemProperty', _this.jobitemProperty).then((response) => {
        let resp = response.data;
        if (resp.success) {
          Toast.success("保存成功", Swal);
        } else {
          Toast.warning(resp.message, Swal);
        }
      })
    },

    submitJobitemProperty() {
      let _this = this;
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
          // 保存校验
          if (1 !== 1
              || !Validator.require(_this.jobitemProperty.jobDescription, "工作描述", Swal)
              || !Validator.require(_this.jobitemProperty.testKeyPoints, "测试/验收关键点说明", Swal)

              || !Validator.richLength(_this.jobitemProperty.jobDescription, "工作描述", 10, 5000, Swal)
              || !Validator.richLength(_this.jobitemProperty.testKeyPoints, "测试/验收关键点说明", 10, 5000, Swal)
          ) {
            return;
          }
          _this.jobitemProperty.jobItemId = _this.jobitemId;
          _this.jobitemProperty.fileListStr = JSON.stringify(_this.resFileIds);
          _this.jobitemProperty.loginUserId = _this.loginUser.customerUserId;

          _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/jobitem-property/submitJobitemProperty', _this.jobitemProperty).then((response) => {
            let resp = response.data;
            if (resp.success) {
              Toast.success("保存成功", Swal);
              _this.$bus.$emit('close-manhour-tab', _this.index);  //关闭manhour tab
              _this.$bus.$emit('notify-jobitem-count-flag', true);  //执行管理
              _this.$bus.$emit('notify-develop-count-flag', true);  //研发管理

              if(_this.entrance === ENTRANCE_CLASS.MANHOUR.key){
                if(_this.projectType === PROJECT_TYPE.SUB_PROJECT.key && _this.jobType === JOB_ITEM_TYPE.ASSIST.key){
                  _this.$bus.$emit('refresh-jobitem-task-tab-num', true);
                  _this.$bus.$emit('refresh-jobitem-task-examine-container', true);
                  _this.$bus.$emit('refresh-jobitem-task-issue-container', true);
                  _this.$bus.$emit('refresh-jobitem-task-submit-page', true);

                }
                if(_this.projectType === PROJECT_TYPE.SUB_PROJECT.key && _this.jobType === JOB_ITEM_TYPE.BUG.key){
                  _this.$bus.$emit('refresh-jobitem-task-tab-test-num', true);
                  _this.$bus.$emit('refresh-jobitem-task-examine-container', true);
                  _this.$bus.$emit('refresh-jobitem-task-issue-container', true);
                  _this.$bus.$emit('refresh-jobitem-submit-testing-page', true);
                }
              }
            } else {
              Toast.warning(resp.message, Swal);
            }
          })
        }
      })
    },

    showJobitemProcess() {
      let _this = this;
      _this.showJobitemProcessDlg = true;
      _this.dialogTitle = "任务处理过程";
    },
    handleClose(done) {
      let _this = this;
      _this.showJobitemProcessDlg = false;
      done();
    },

    closeDialog() {
      let _this = this;
      _this.showJobitemProcessDlg = false;
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

.paper-height {
  height: calc(100vh - 200px);
}

</style>
