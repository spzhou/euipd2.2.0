<!--
 * Author: 周朔鹏
 * Date: 2025-02-27
 * Email: 391902958@qq.com
 * Website: https://www.euipd.com
-->

<template>
  <div style="height: 100%;">
    <div class="paper-height">
      <div style="height: 100%; overflow: scroll;">
        <div class="a4-paper-big-simple">
          <div style="margin: 20px 0 50px;">
            <div>
              <el-row style="margin-top: 10px; margin-left: 55px; padding-right: 20px;">
                <el-col :span="23" >
                  <div  style="display: flex; justify-content: space-between;">
                    <div>任务号: {{ jobItem.jobSerial }}</div>
                    <div>任务状态: <a href="javascript:void(0);" @click="showJobitemProcess">
                      {{ JOB_ITEM_PROCESS | optionKV(jobItem.processStatus) }} </a></div>
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
                    <small><em>(内容说明: 以下为任务下发人对工作要求的描述)</em></small>
                  </span>
                        </th>
                      </tr>
                    </table>
                  </tr>
                  <tr>
                    <table class="table-inn">
                      <tr>
                        <th>
                  <span style="margin-left: 10px; white-space: pre-line;">{{ jobItem.taskDescription }}</span>
                        </th>
                      </tr>
                    </table>
                  </tr>
                  <!--  任务附件-->
                  <tr  v-if="jobItem.fileList !==null && jobItem.fileList !== undefined && jobItem.fileList.length > 0">
                    <table class="table-inn">
                      <tr style="border-top: 2px solid #1a1a1a;">
                        <th style="width: 120px; text-align: center;">
                          <span>任务附件</span>
                        </th>
                        <th>
                  <span style="margin-left: 10px;">
                    <small><em>(内容说明: 任务任务下发时, 提供的参考附件)</em></small>
                  </span>
                        </th>
                      </tr>
                    </table>
                  </tr>
                  <tr  v-if="jobItem.fileList !==null && jobItem.fileList !== undefined && jobItem.fileList.length > 0">
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
            </div>
            <BugAnalysisCore
                :key="('bug-analysis-' + (bugfeedbackItem.id))"
                ref="bugAnalysisComp"
                :bugfeedback="bugfeedbackItem"
            ></BugAnalysisCore>
            <div v-show="resFiles && resFiles.length>0" style="display: flex; align-items: end; flex-direction: column; margin-right: 55px;">
              <div v-for="(item, i) in resFiles" :key="i" style="display: flex; flex-direction: row; align-items: center; margin: 0 0 0 20px;">
                <div v-if="item">
                  {{item.name}}
                </div>
                <div v-if="item && item.authStatus">
                  <el-button type="text" style="margin-left: 50px;" @click="downloadFile(item)">下载</el-button>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
    <div class="hr hr-5 dotted"></div>
    <div id="approve-handle" style="height: 50px;">
      <el-row style="height: 100%; overflow: scroll; overflow-x: hidden">
        <div>
          <el-row>
            <el-col>
              <div style="display: flex; justify-content: center;">
                <el-tooltip content="对任务执行人的工作作业评价非常重要, 将直接影响工作执行人的工作业绩!" placement="top" effect="light" :open-delay=1000>
                  <el-button type="primary" @click="approveAndEvaluateDlg">批准 & 评价</el-button>
                </el-tooltip>
                <el-button type="primary" @click="rejectJobitemDlg">任务驳回</el-button>
              </div>
            </el-col>
          </el-row>
        </div>
      </el-row>
    </div>

    <!--显示任务过程信息-->
    <el-dialog
        width="29cm"
        :visible.sync="showJobitemProcessDlg"
        :before-close="handleClose"
        :close-on-click-modal=false>
      <div slot="title" style="font-size: 20px;">{{ dialogTitle }}</div>
      <div v-if="showJobitemProcessDlg">
        <JobitemDetailInfoDlg
            :key="('detail-' + (jobitemId))"
            :jobitem-id="jobItem.id"
        ></JobitemDetailInfoDlg>
      </div>
    </el-dialog>

    <!-- 批准&评价-->
    <el-dialog
        :visible.sync="dialogVisible"
        width="29cm"
        :before-close="handleClose"
        :close-on-click-modal=false>
      <div slot="title" style="font-size: 20px;">{{ ((dialogTitle)) }}</div>
      <div v-if="dialogVisible">
        <JobitemApproveDlg
            :key="jobItem.id"
            :jobitem-id="jobItem.id"
            :job-type="jobItem.type"
            :inherit-job-type="inheritJobType"
            :project-type="projectType"
            :index="index"
            :entrance="entrance"
            :close-function="closeDialog"
        ></JobitemApproveDlg>
      </div>
    </el-dialog>

    <!--任务驳回-->
    <el-dialog
        width="29cm"
        :visible.sync="rejectJobItemDlg"
        :before-close="handleClose"
        :close-on-click-modal=false>
      <div slot="title" style="font-size: 20px;">{{ dialogTitle }}</div>
      <div v-if="rejectJobItemDlg">
        <jobitem-reject-dlg
            :key="jobItem.id"
            :jobitem-id="jobItem.id"
            :job-type="jobItem.type"
            :inherit-job-type="inheritJobType"
            :project-type="projectType"
            :index="index"
            :entrance="entrance"
            :close-dlg = closeDialog
        ></jobitem-reject-dlg>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import FileRdms from "@/components/rdms/file-rdms.vue";
import FileSaver from 'file-saver';
import Swal from "sweetalert2";
import printJS from "print-js";
import {formatSecond, optionKV} from "@/filter/filter";
import CharacterBudgetConfirmDlg from "@/components/rdms/character-budget-confirm-dlg.vue";
import JobitemCreateByJobitemIdDlg from "@/components/std/jobitem-create-by-jobitem-id-dlg.vue";
import JobitemOwnInfoCore from "@/components/std/jobitem-own-info-core.vue";
import JobitemRejectDlg from "@/components/std/jobitem-reject-dlg.vue";
import JobitemDetailInfoDlg from "@/components/std/jobitem-detail-info-dlg.vue";
import JobitemApproveDlg from "@/components/std/jobitem-approve-dlg.vue";
import JobitemAssistDetailCore from "@/components/rdms/jobitem-assist-detail-core.vue";
import BugAnalysisCore from "@/components/rdms/bug-analysis-core.vue";

export default {
  components: {
    BugAnalysisCore,
    JobitemAssistDetailCore,
    FileRdms,
    FileSaver,
    CharacterBudgetConfirmDlg,
    JobitemApproveDlg,
    JobitemCreateByJobitemIdDlg,
    JobitemOwnInfoCore,
    JobitemRejectDlg,
    JobitemDetailInfoDlg,
  },
  name: "bug-examine-page",

  props: {
    jobitemId: {
      default: null,
    },
    jobType: {
      default: null,
    },
    inheritJobType: {
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
      JOB_ITEM_TYPE: JOB_ITEM_TYPE,
      ENTRANCE_CLASS: ENTRANCE_CLASS,
      JOB_ITEM_PROCESS: JOB_ITEM_PROCESS,

      bugfeedbackItem: {},

      jobItem: {},
      jobitemProperty: {},
      assistJobList: [],

      testJobList: [],

      fileItems: [],
      referenceFileList: [],
      characterItem: {},
      dialogVisible: false,
      showJobitemProcessDlg: false,
      rejectJobItemDlg: false,

      dialogTitle: '',
      resFile: {},
      resFiles: [],
      resFileIds: [],

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
    getJobDescriptionFontNum() {
      if (Tool.isNotEmpty(this.jobitemProperty.jobDescription)) {
        return Tool.richTextLength(this.jobitemProperty.jobDescription);
      }
      return 0;
    },
    getTestKeyPointFontNum() {
      if (Tool.isNotEmpty( this.jobitemProperty.testKeyPoints)) {
        return Tool.richTextLength(this.jobitemProperty.testKeyPoints);
      }
      return 0;
    },

    noTestProcess() {
      let _this = this;
      if(!(_this.inheritJobType===JOB_ITEM_TYPE.TEST.key  || _this.inheritJobType === JOB_ITEM_TYPE.REVIEW.key)){
        return true;
      }
      return false;
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

    getJobitemDetailInfo(jobitemId){
      let _this = this;
      let formData = new window.FormData();
      formData.append('jobitemId', jobitemId);
      formData.append('loginUserId', _this.loginUser.customerUserId);
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/job-items/getJobitemDetailInfo', formData).then((response) => {
        let resp = response.data;
        if (resp.success) {
          _this.jobItem = resp.content;
          _this.getBugDetailInfo(_this.jobItem.qualityId);  //qualityId复用 其实是 bugfeedbackId
        }else{
          Toast.warning(resp.message, Swal);
        }
      });
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
          _this.resFiles = _this.bugfeedbackItem.fileList;
          let resFileList = _this.bugfeedbackItem.propertyDto.fileItems;
          if(resFileList){
            if(_this.resFiles){
              _this.resFiles.push(...resFileList);
            }else{
              _this.resFiles = resFileList;
            }
          }
        } else {
          Toast.warning(resp.message,Swal);
        }
      })
    },

    showJobitemProcess() {
      let _this = this;
      _this.showJobitemProcessDlg = true;
      _this.dialogTitle = "任务处理过程";
    },

    approveAndEvaluateDlg() {
      let _this = this;
      _this.dialogVisible = true;
      _this.dialogTitle = "对工作提交质量进行评价";
    },

    rejectJobitemDlg() {
      let _this = this;
      _this.rejectJobItemDlg = true;
      _this.dialogTitle = "任务驳回";
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


    handleClose(done) {
      let _this = this;
      _this.dialogVisible = false;
      _this.rejectJobItemDlg = false;
      _this.showJobitemProcessDlg = false;
      done();
    },

    closeDialog() {
      let _this = this;
      _this.dialogVisible = false;
      _this.rejectJobItemDlg = false;
      _this.showJobitemProcessDlg = false;
    },
  },
}
</script>

<style lang="scss" scoped>
@import './../../../public/static/scss/caipd';

.paper-height {
  height: calc(100vh - 310px);
}

</style>
