<!--
 * Author: 周朔鹏
 * Date: 2025-02-26
 * Email: 391902958@qq.com
 * Website: https://www.euipd.com
-->

<template>
  <div style="height: 100%;">
    <div class="paper-height">
      <div style="height: 100%; overflow: scroll;">
        <div class="a4-paper-big-simple">
          <div style="margin: 30px 0 20px;">
            <el-row style="margin-top: 10px; margin-left: 55px; padding-right: 20px;">
              <el-col :span="23" >
                <div style="display: flex; flex-direction: row; justify-content: space-between;">
                  <div>任务号: {{ jobItem.jobSerial }}</div>
                  <div>
                    <el-button type="text" @click="showOriginJobProperty" style="margin-right: -10px;">被测任务信息</el-button>
                  </div>
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
                          <div style="margin-left: 10px;">
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

            <!--    任务提交说明-->
            <div style="margin-left: 55px;">
              工作提交信息:
            </div>
            <table class="table-out">
              <tr>
                <table class="table-inn">
                  <tr style="border-top: 1px solid #1a1a1a;">
                    <th style="width: 19.51%; text-align: center;">
                      <span>工作描述</span>
                    </th>
                    <th style="width: 48.78%;">
                    <span style="margin-left: 10px;">
                      <small><em>(本项内容是员工对工作进行总结并对交付件相关信息进行详细描述)</em></small>
                    </span>
                    </th>
                    <th style="width: 31.71%;">
                  <span style="margin-left: 10px;">
                    字数 <small><em>(不计空白与格式)</em></small>: {{ getJobDescriptionFontNum }} / 5000
                  </span>
                    </th>
                  </tr>
                </table>
              </tr>
              <tr>
                <table class="table-inn">
                  <tr>
                    <th>
                      <div style="margin-left: 10px;" v-rich-html="jobitemProperty.jobDescription">
                      </div>
                    </th>
                  </tr>
                </table>
              </tr>
              <tr>
                <table class="table-inn">
                  <tr style="border-top: 2px solid #1a1a1a;">
                    <th style="width: 19.51%; text-align: center;">
                      <span>测试/验收关键点说明</span>
                    </th>
                    <th style="width: 48.78%;">
                    <span style="margin-left: 10px;">
                      <small><em>(本项内容是员工对测试关键点进行详细描述, 以便测试工作开展.)</em></small>
                    </span>
                    </th>
                    <th style="width: 31.71%;">
                  <span style="margin-left: 10px;">
                    字数 <small><em>(不计空白与格式)</em></small>: {{ getTestKeyPointFontNum }} / 5000
                  </span>
                    </th>
                  </tr>
                </table>
              </tr>
              <tr>
                <table class="table-inn">
                  <tr>
                    <th>
                      <div style="margin-left: 10px;" v-rich-html="jobitemProperty.testKeyPoints">
                      </div>
                    </th>
                  </tr>
                </table>
              </tr>
              <!--  交付附件-->
              <tr v-if="resFiles !==null && resFiles !== undefined && resFiles.length > 0">
                <table class="table-inn">
                  <tr style="border-top: 2px solid #1a1a1a;">
                    <th style="width: 160px; text-align: center;">
                      <span>交付附件</span>
                    </th>
                    <th>
                  <span style="margin-left: 10px;">
                    <small><em>(内容说明: 任务交付时, 提交的工作交付成果)</em></small>
                  </span>
                    </th>
                  </tr>
                </table>
              </tr>
              <tr v-if="resFiles !==null && resFiles !== undefined && resFiles.length > 0">
                <table class="table-inn">
                  <tr>
                    <th>
                      <div v-for="file in resFiles" :key="file.id"
                           style="display: flex; flex-direction: row; align-items: center;">
                        <div style="margin-left: 10px;">
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
                <el-button type="primary" @click="testJobReject">任务驳回</el-button>
              </div>
            </el-col>
          </el-row>
        </div>
      </el-row>
    </div>

    <!-- 批准&评价-->
    <el-dialog
        :visible.sync="dialogVisible"
        width="29cm"
        :before-close="handleClose"
        :close-on-click-modal=false>
      <div slot="title" style="font-size: 20px;">{{ ((dialogTitle)) }}</div>
      <div v-if="dialogVisible">
        <JobitemApproveDlg
            :key="jobitemId"
            :jobitem-id="jobitemId"
            :job-type="jobType"
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
        :visible.sync="testJobItemDlg"
        :before-close="handleClose"
        :close-on-click-modal=false>
      <div slot="title" style="font-size: 20px;">{{ dialogTitle }}</div>
      <div v-if="testJobItemDlg">
        <jobitem-reject-dlg
            :key="('test-' + (jobitemId))"
            :jobitem-id="jobitemId"
            :job-type="jobType"
            :project-type="projectType"
            :index="index"
            :entrance="entrance"
            :close-dlg = closeDialog
        ></jobitem-reject-dlg>
      </div>
    </el-dialog>

    <!--展示原始任务交付信息-->
    <el-dialog
        width="29cm"
        :visible.sync="originJobPropertyDlg"
        :before-close="handleClose"
        :close-on-click-modal=false>
      <div slot="title" style="font-size: 20px;">{{ dialogTitle }}</div>
      <div v-if="originJobPropertyDlg">
        <JobitemRelatedJobInfoDlg
            :key="jobItem.testedJobitemId"
            :jobitem-id="jobItem.testedJobitemId"
            :project-type="projectType"
        ></JobitemRelatedJobInfoDlg>
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
import JobitemRelatedJobInfoDlg from "@/components/std/jobitem-related-job-info-dlg.vue";
import JobitemRejectDlg from "@/components/std/jobitem-reject-dlg.vue";
import JobitemApproveDlg from "@/components/std/jobitem-approve-dlg.vue";
import JobitemAssistDetailCore from "@/components/rdms/jobitem-assist-detail-core.vue";

export default {
  components: {
    JobitemAssistDetailCore,
    FileRdms,
    FileSaver,
    CharacterBudgetConfirmDlg,
    JobitemApproveDlg,
    JobitemRelatedJobInfoDlg,
    JobitemRejectDlg,
  },
  name: "jobitem-test-evaluate-page",

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
      ENTRANCE_CLASS: ENTRANCE_CLASS,

      jobItem: {},
      jobitemProperty: {},

      fileItems: [],
      characterItem: {},
      dialogVisible: false,
      testJobItemDlg: false,
      originJobPropertyDlg: false,

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
            if(_this.fileItems !== null){
              for(let i= 0 ; i < _this.fileItems.length; i++){
                _this.resFiles.push(_this.fileItems[i]);
                _this.resFileIds.push(_this.fileItems[i].id);
              }
            }
          }

        } else {
          Toast.warning(resp.message,Swal);
        }
      })
    },

    approveAndEvaluateDlg() {
      let _this = this;
      _this.dialogVisible = true;
      _this.dialogTitle = "对测试工作质量进行评价";
    },

    developJobReject() {
      let _this = this;
      _this.dialogTitle = "被测试任务驳回";
    },


    testJobReject() {
      let _this = this;
      _this.testJobItemDlg = true;
      _this.dialogTitle = "测试/复核任务驳回";
    },



    showOriginJobProperty() {
      let _this = this;
      _this.originJobPropertyDlg = true;
      _this.dialogTitle = "被测试/评测任务信息";
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
      _this.testJobItemDlg = false;
      _this.originJobPropertyDlg = false;
      done();
    },

    closeDialog() {
      let _this = this;
      _this.dialogVisible = false;
      _this.testJobItemDlg = false;
      _this.originJobPropertyDlg = false;
    },
  },
}
</script>

<style lang="scss" scoped>
@import './../../../public/static/scss/caipd';

.paper-height {
  height: calc(100vh - 316px);
}

/*.a4-paper-big {
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
  .table-out{
    width: 23.4cm;
    margin: 10px auto 26px;
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
