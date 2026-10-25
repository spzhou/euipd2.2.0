<!--
 * Author: 周朔鹏
 * Date: 2025-02-27
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
                  <span style="margin-left: 10px; white-space: pre-line;">{{ jobItem.taskDescription }}</span>
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
                            <el-button type="text" style="margin-left: 15px; margin-right: 25px;" @click="downloadFile(file)">下载</el-button>
                          </div>
                        </div>
                      </th>
                    </tr>
                  </table>
                </tr>
              </table>
            </div>
            <div class="hr hr-8 dotted"></div>

            <!--    任务提交说明-->
            <div style="margin-left: 55px;">
              工作提交信息填写:
            </div>
            <table class="table-out">
              <tr>
                <table class="table-inn">
                  <tr style="border-top: 1px solid #1a1a1a;border-left: 1px solid #1a1a1a;border-right: 1px solid #1a1a1a;">
                    <th style="width: 19.51%; text-align: center;">
                      <span>工作描述</span>
                    </th>
                    <th style="width: 48.78%;">
                    <span style="margin-left: 10px;">
                      填表说明: 请对工作进行总结并对交付件相关信息进行详细描述.
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
                  <tr style="border: 1px solid #1a1a1a;">
                    <rich-editor
                        key="character-desc"
                        v-model="jobitemProperty.jobDescription"
                        placeholder="请输入正文（不计空白与格式，5000 字以内）"
                        :max-length="5000"
                        :show-count="false"
                    />
                  </tr>
                </table>
              </tr>
              <tr>
                <table class="table-inn">
                  <tr style="border-top: 1px solid #1a1a1a;border-left: 1px solid #1a1a1a;border-right: 1px solid #1a1a1a;">
                    <th style="width: 19.51%; text-align: center;">
                      <span>测试/验收关键点说明</span>
                    </th>
                    <th style="width: 48.78%;">
                    <span style="margin-left: 10px;">
                      填表说明: 请对测试关键点进行详细描述, 以便测试工作开展.
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
                  <tr style="border-top: 1px solid #1a1a1a;border-left: 1px solid #1a1a1a;border-right: 1px solid #1a1a1a;">
                    <rich-editor
                        key="character-condition"
                        v-model="jobitemProperty.testKeyPoints"
                        placeholder="请输入正文（不计空白与格式，5000 字以内）"
                        :max-length="5000"
                        :show-count="false"
                    />
                  </tr>
                </table>
              </tr>
            </table>

            <div v-if="jobType === JOB_ITEM_TYPE.TASK_MILESTONE.key && jobItem.rdType === RD_TYPE.DIRECT.key">
              <div class="hr hr-5 dotted"></div>
              <div style="display: flex; flex-direction: row; justify-content: center; align-items: center;">
                <div style="display: flex; flex-direction: row; justify-content: center; align-items: center;">
                  <div style="margin-right: 40px;">
                    当期预算支出总额: {{ sumBudgetExe | rmbFormat }} 元
                  </div>
                  <el-button type="primary" size="small" @click="budgetExecution">添加预算执行条目</el-button>
                </div>
                <div style="margin-left: 30px;">
                  <el-button size="small" @click="showBudgetStatisticsInfo">项目预算执行汇总表</el-button>
                </div>
                <div v-if="jobItem.projectId === jobItem.subprojectId" style="margin-left: 30px;">
                  <el-button size="small" @click="showBudgetStatisticsInfo_main">"主项目"预算执行汇总表</el-button>
                </div>
                <div style="margin-left: 30px;">
                  <el-button size="small" @click="feeSummaryList">费用清单</el-button>
                </div>
              </div>

            </div>

            <div class="hr hr-5 dotted"></div>
            <div style="margin-right: 75px;">
              <FileUploadCore
                  :input-id="('jobitem-submit-property-page-' + (jobItem.id))"
                  :key="('file-upload-' + (jobItem.id))"
                  :x-project-id="getXProjectId"
                  :item-id="jobItem.id"
                  :file-group="FILE_GROUPING.PROPERTY.key"
                  :project-type="projectType"
                  :en-delete="true"
                  :read-db-record="true"
                  v-on:uploadFileList="getUploadFileList">
              </FileUploadCore>
            </div>
            <div style="text-align: right; margin-right: 80px;">
              <div style="margin-top: 16px;">
                <small><em>注1: 允许上传文件格式: [{{((G_SUFFIXS))}}]</em></small>
              </div>
              <div>
                <small><em>注2: 代码等程序文件为了便于使用, 请 压缩后 上传</em></small>
              </div>
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
            <div class="clearfix form-actions" style="margin-top:20px;">
              <div style="display: flex; justify-content: center;">
                <button type="button" v-on:click="saveJobitemProperty()" class="ca-btn-primary">保存</button>
                <button type="button" v-on:click="submitJobitemProperty()" class="ca-btn-primary">提交</button>
              </div>
            </div>
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

    <!--工时申报-->
    <el-dialog
        width="23cm"
        :visible.sync="manhourDeclareDlg"
        :before-close="handleClose"
        :close-on-click-modal=false>
      <div slot="title" style="font-size: 20px;">{{ dialogTitle }}</div>
      <div>
        <ManhourDeclare
            v-if="manhourDeclareDlg"
            :key="jobitemId"
            :jobitem-id="jobitemId"
            :confirm-clicked="confirmDeclare"
            :close-dlg="closeDialog"
        ></ManhourDeclare>
      </div>
    </el-dialog>

    <!--填写预算执行表-->
    <el-dialog
        width="29cm"
        :visible.sync="budgetExecutionDlg"
        :before-close="handleClose"
        :close-on-click-modal=false>
      <div slot="title" style="font-size: 20px;">{{ dialogTitle }}</div>
      <div>
        <BudgetExecutionDlg
            :key="((jobitemId))"
            :jobitem-id="jobitemId"
            :close-dlg = closeDialog
        ></BudgetExecutionDlg>
      </div>
    </el-dialog>

    <!--查看预算执行汇总表-->
    <el-dialog
        width="40cm"
        :visible.sync="showStatisticsDlg"
        destroy-on-close
        :before-close="handleClose"
        :close-on-click-modal=false>
      <div slot="title" style="font-size: 20px;">{{ dialogTitle }}</div>
      <div>
        <BudgetExecutionStatisticsDlg
            :key="((jobitemId) + '-subproject-' + (random))"
            :jobitem-id="jobitemId"
            :project-type="PROJECT_TYPE.SUB_PROJECT.key"
            :close-dlg = closeDialog
        ></BudgetExecutionStatisticsDlg>
      </div>
    </el-dialog>

    <!--查看预算执行汇总表-->
    <el-dialog
        width="40cm"
        :visible.sync="showStatisticsDlg_main"
        destroy-on-close
        :before-close="handleClose"
        :close-on-click-modal=false>
      <div slot="title" style="font-size: 20px;">{{ dialogTitle }}</div>
      <div>
        <BudgetExecutionStatisticsDlg
            :key="((jobitemId) + '-' + (timeStamp))"
            :jobitem-id="jobitemId"
            :project-type="PROJECT_TYPE.PROJECT.key"
            :close-dlg = closeDialog
        ></BudgetExecutionStatisticsDlg>
      </div>
    </el-dialog>

    <!--费用清单-->
    <el-dialog
        width="32cm"
        :visible.sync="feeSummaryListDlg"
        destroy-on-close
        :before-close="handleClose"
        :close-on-click-modal=false>
      <div slot="title" style="font-size: 20px;">{{ dialogTitle }}</div>
      <div v-if="projectType === PROJECT_TYPE.SUB_PROJECT.key">
        <SubprojectFeeSummaryPage
            :key="((jobItem.subprojectId) + '-' + (projectType))"
            :subproject-id="jobItem.subprojectId"
            :project-type="projectType"
        ></SubprojectFeeSummaryPage>
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

import RichEditor from '@/components/rich/rich-editor.vue'
import { hasPendingUploads } from '@/components/rich/rich-image'
import {formatSecond, optionKV} from "@/filter/filter";
import FileUploadCore from "@/components/std/file-upload-core.vue";
import BudgetExecutionDlg from "@/components/std/budget-execution-dlg.vue";
import BudgetExecutionStatisticsDlg from "@/components/std/budget-execution-statistics-dlg.vue";
import SubprojectFeeSummaryPage from "@/components/std/subproject-fee-summary-page.vue";
import {webAssetUrl} from "@/shared/oss";
import ManhourDeclare from "@/components/module/manhour-declare.vue";


export default {
  components: {
    ManhourDeclare,
    BudgetExecutionDlg,
    FileUploadCore,
    FileRdms,
    FileSaver,
    CharacterBudgetConfirmDlg,
    RichEditor,
    JobitemDetailInfoDlg,
    BudgetExecutionStatisticsDlg,
    SubprojectFeeSummaryPage,
  },
  name: "jobitem-submit-property-page",

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
      RD_TYPE: RD_TYPE,
      JOB_ITEM_TYPE: JOB_ITEM_TYPE,
      G_SUFFIXS: G_SUFFIXS,

      showJobitemProcessDlg: false,
      budgetExecutionDlg: false,
      showStatisticsDlg: false,
      showStatisticsDlg_main: false,
      feeSummaryListDlg: false,
      manhourDeclareDlg: false,
      dialogTitle: '',

      sumBudgetExe: 0,

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
      templatePath: webAssetUrl("import_budget_execution_table.xlsx"),
      timeStamp: 0,

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
    _this.timeStamp = Date.now();
    _this.currentTime = new Date();
    _this.currentTimeStr = _this.currentTime.toLocaleString()
    _this.getLoginUser();
    if(Tool.isNotEmpty(_this.jobitemId)){
      _this.getJobitemDetailInfo(_this.jobitemId);
    }

    _this.$bus.$on('refresh-jobitem-submit-property-page-subBudget', (flag) => {
      if (flag) {
        _this.getSumBudgetBySubprojectId(_this.jobItem.subprojectId);
      }
    });

  },

  beforeDestroy(){
    let _this = this;
    _this.$bus.$off('refresh-jobitem-submit-property-page-subBudget');
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

    showJobitemProcess() {
      let _this = this;
      _this.showJobitemProcessDlg = true;
      _this.dialogTitle = "任务处理过程";
    },

    showBudgetStatisticsInfo() {
      let _this = this;
      _this.random = Math.random();
      _this.showStatisticsDlg = true;
      _this.dialogTitle = "查看预算执行汇总表";
    },

    showBudgetStatisticsInfo_main() {
      let _this = this;
      _this.timeStamp = Date.now();
      _this.showStatisticsDlg_main = true;
      _this.dialogTitle = "查看预算执行汇总表";
    },

    feeSummaryList() {
      let _this = this;
      _this.feeSummaryListDlg = true;
      _this.dialogTitle = "费用清单";
    },

    budgetExecution() {
      let _this = this;
      _this.budgetExecutionDlg = true;
      _this.dialogTitle = "填写预算执行表";
    },

    closeDialog() {
      let _this = this;
      _this.showJobitemProcessDlg = false;
      _this.budgetExecutionDlg = false;
      _this.showStatisticsDlg = false;
      _this.showStatisticsDlg_main = false;
      _this.feeSummaryListDlg = false;
      _this.manhourDeclareDlg = false;
    },
    handleClose(done) {
      let _this = this;
      _this.showJobitemProcessDlg = false;
      _this.budgetExecutionDlg = false;
      _this.showStatisticsDlg = false;
      _this.showStatisticsDlg_main = false;
      _this.feeSummaryListDlg = false;
      _this.manhourDeclareDlg = false;
      done();
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
          _this.getSumBudgetBySubprojectId(_this.jobItem.subprojectId);
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
          }

        } else {
          Toast.warning(resp.message,Swal);
        }
      })
    },

    getSumBudgetBySubprojectId(subprojectId) {
      let _this = this;
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/budget-direct-exe/getSumBudgetBySubprojectId/' + subprojectId).then((response)=>{
        let resp = response.data;
        if (resp.success) {
          _this.sumBudgetExe = resp.content;
        }else{
          Toast.warning(resp.message, Swal);
        }
      })
    },

    saveJobitemProperty() {
      let _this = this;
      if (hasPendingUploads()) {
        Toast.warning("图片仍在上传，请稍候再保存", Swal);
        return;
      }
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
      if (hasPendingUploads()) {
        Toast.warning("图片仍在上传，请稍候再保存", Swal);
        return;
      }
      if (1 !== 1
          || !Validator.require(_this.jobitemProperty.jobDescription, "工作描述", Swal)
          || !Validator.require(_this.jobitemProperty.testKeyPoints, "测试/验收关键点说明", Swal)

          || !Validator.richLength(_this.jobitemProperty.jobDescription, "工作描述", 10, 5000, Swal)
          || !Validator.richLength(_this.jobitemProperty.testKeyPoints, "测试/验收关键点说明", 10, 5000, Swal)
      ) {
        return;
      }
      _this.manhourDeclareDlg = true;
      _this.dialogTitle = "提交确认";
    },

    confirmDeclare() {
      let _this = this;
      _this.manhourDeclareDlg = false;
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
            if(_this.projectType === PROJECT_TYPE.PRE_PROJECT.key && _this.jobType === JOB_ITEM_TYPE.ASSIST.key){
              _this.$bus.$emit('refresh-jobitem-task-tab-num', true);
              _this.$bus.$emit('refresh-jobitem-task-examine-container', true);
              _this.$bus.$emit('refresh-jobitem-task-issue-container', true);
              _this.$bus.$emit('refresh-jobitem-task-submit-page', true);

            }
            if(_this.projectType === PROJECT_TYPE.PROJECT.key && _this.jobType === JOB_ITEM_TYPE.TASK_TEST.key){
              _this.$bus.$emit('refresh-test-character-task-tab-num', true);
              _this.$bus.$emit('refresh-test-character-task-tab', true);
              _this.$bus.$emit('refresh-test-character-jobitem-issue', true);
              _this.$bus.$emit('refresh-test-character-jobitem-examine', true);

            }
            if(_this.projectType === PROJECT_TYPE.SUB_PROJECT.key && _this.jobType === JOB_ITEM_TYPE.ASSIST.key){
              _this.$bus.$emit('refresh-jobitem-task-tab-num', true);
              _this.$bus.$emit('refresh-jobitem-task-examine-container', true);
              _this.$bus.$emit('refresh-jobitem-task-issue-container', true);
              _this.$bus.$emit('refresh-jobitem-task-submit-page', true);
              _this.$bus.$emit('refresh-manhour-jobitem-list', true);

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
