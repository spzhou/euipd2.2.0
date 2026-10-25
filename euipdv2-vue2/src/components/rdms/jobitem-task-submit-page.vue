<!--
 * Author: 周朔鹏
 * Date: 2025-03-06
 * Email: 391902958@qq.com
 * Website: https://www.euipd.com
-->

<template>
  <div style="height: 100%;">
    <div class="paper-height">
      <div style="height: 100%; overflow: scroll;">
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
                        <span  v-if="jobItem.planSubmissionTime" style="margin-left: 10px;">
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
                    <div v-rich-html="jobItem.taskDescription"></div>
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

            <!--    任务提交说明-->
            <div style="margin-left: 55px;">
              工作提交信息填写:
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
                  <tr style="border-top: 1px solid #1a1a1a;">
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
                  <tr style="border-top: 1px solid #1a1a1a; border-left: 1px solid #1a1a1a; border-right: 1px solid #1a1a1a;">
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
            <div class="hr hr-5 dotted"></div>
            <div style="margin-right: 75px;">
              <FileUploadCore
                  :input-id="('jobitem-submit-task-page-' + (jobItem.id))"
                  :key="('file-upload-' + (jobItem.id))"
                  :x-project-id="jobItem.subprojectId"
                  :item-id="jobItem.id"
                  :file-group="FILE_GROUPING.PROPERTY.key"
                  :project-type="projectType"
                  :en-delete=true
                  :read-db-record="true"
                  v-on:uploadFileList="getUploadFileList">
              </FileUploadCore>
              <div style="margin-top: 8px; display: flex; flex-direction: column; align-items: flex-end;">
                <div>
                  <small><em>注1: 请对开发任务进行总结, 形成附件上传, 这些附件将作为下一步工作重要的参考文献</em></small>
                </div>
                <div>
                  <small><em>注2: 代码等程序文件为了便于使用, 请 压缩后 上传</em></small>
                </div>
                <div>
                  <small><em>注3: 允许上传文件格式: [{{((G_SUFFIXS))}}]</em></small>
                </div>
              </div>
            </div>
            <div v-if="fileList && fileList.length>0" class="hr hr-18 dotted hr-double"></div>
            <div v-if="fileList && fileList.length>0" style="margin: 0 20px 0 50px;">
              <div style="margin: 5px 0 10px;">
                参考文献: <small><u><em>(请选择协助任务附件, 作为本任务提交的参考文献)</em></u></small>
              </div>
              <el-checkbox-group
                  v-model="checkedFileIdList">
                <el-checkbox v-for="(file, i) in fileList" :label="file.id" :key="i">{{file.name}}</el-checkbox>
              </el-checkbox-group>
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
            <div class="clearfix form-actions" style="margin-top: 20px;">
              <div style="display: flex; justify-content: center;">
                <button type="button" v-on:click="saveJobitemProperty()" class="ca-btn-primary">保存</button>
                <button v-if="jobItem.childrenJobComplateFlag"  type="button" v-on:click="showManhourDeclare()" class="ca-btn-primary">提交</button>
                <button v-if="!jobItem.childrenJobComplateFlag" type="button" v-on:click="warnChildrenOpen()" title="存在未关闭的子任务" class="ca-btn-primary-disable" >提交</button>
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
            :key="jobitemId"
            :jobitem-id="jobitemId"
            :confirm-clicked="confirmDeclare"
            :close-dlg="closeDialog"
        ></ManhourDeclare>
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
import FileUploadCore from "@/components/std/file-upload-core.vue";
import RichEditor from '@/components/rich/rich-editor.vue'
import { hasPendingUploads } from '@/components/rich/rich-image'
import {formatSecond, optionKV} from "@/filter/filter";
import ManhourDeclare from "@/components/module/manhour-declare.vue";


export default {
  components: {
    FileRdms,
    FileSaver,
    CharacterBudgetConfirmDlg,
    RichEditor,
    JobitemDetailInfoDlg,
    FileUploadCore,
    ManhourDeclare,
  },
  name: "jobitem-task-submit-page",

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
      if(_this.jobItem.planSubmissionTime){
        let planSubmissionTime = new Date(_this.jobItem.planSubmissionTime).getTime();
        let left = planSubmissionTime/1000 - Date.now()/1000;
        return  formatSecond(left);
      }
      return '';
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
      G_SUFFIXS: G_SUFFIXS,

      showJobitemProcessDlg: false,
      manhourDeclareDlg: false,
      dialogTitle: '',
      checkedFileIdList: [],

      currentTime: {},
      currentTimeStr: '',

      jobItem: {},
      jobitemProperty: {
        jobDescription: '',
        testKeyPoints: '',
      },

      resFile: {},
      resFiles: [],
      resFileIds: [],

      //可选附件列表
      fileList: [],

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

  mounted: function () {
    let _this = this;
    _this.random = Math.random();
    _this.currentTime = new Date();
    _this.currentTimeStr = _this.currentTime.toLocaleString()
    _this.getLoginUser();
    if(Tool.isNotEmpty(_this.jobitemId)){
      _this.getJobitemRecordDetailInfo(_this.jobitemId);
    }

    _this.$bus.$on('refresh-jobitem-task-submit-page', (data) =>{
      if(data && _this.jobitemId){
        _this.getJobitemRecordDetailInfo(_this.jobitemId);
      }
    });
  },

  beforeDestroy() {
    let _this = this;
    _this.$bus.$off('refresh-jobitem-task-submit-page');
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

    getJobitemRecordDetailInfo(jobitemId){
      let _this = this;
      let formData = new window.FormData();
      formData.append('jobitemId', jobitemId);
      formData.append('loginUserId', _this.loginUser.customerUserId);
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/job-items/getJobitemRecordDetailInfo', formData).then((response) => {
        let resp = response.data;
        if (resp.success) {
          _this.jobItem = resp.content;
          _this.getJobitemProperty(_this.jobItem.id);

          if(_this.jobItem.type === JOB_ITEM_TYPE.TASK.key || _this.jobItem.type === JOB_ITEM_TYPE.TASK_PRO.key || _this.jobItem.type === JOB_ITEM_TYPE.TASK_SUBP.key){
            _this.getJobitemAllAttachments(jobitemId);
          }else if(_this.jobItem.type === JOB_ITEM_TYPE.CHARACTER_INT.key){
            _this.getHmiAllAttachmentsOfIntCharacter(_this.jobItem.characterId);
          }
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
            _this.checkedFileIdList = JSON.parse(_this.jobitemProperty.referenceFileListStr);
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

    getHmiAllAttachmentsOfIntCharacter(characterId) {
      let _this = this;
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/character/getHmiAllAttachmentsOfIntCharacter/' + characterId).then((response) => {
        let resp = response.data;
        if (resp.success) {
          _this.fileList = resp.content;
        } else {
          Toast.warning(resp.message, Swal);
        }
      })
    },

    getJobitemAllAttachments(jobitemId) {
      let _this = this;
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/job-items/getJobitemAllAttachments/' + jobitemId).then((response) => {
        let resp = response.data;
        if (resp.success) {
          _this.fileList = resp.content;
        } else {
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
      _this.jobitemProperty.jobItemId = _this.jobitemId;
      _this.jobitemProperty.fileListStr = JSON.stringify(_this.resFileIds);
      _this.jobitemProperty.referenceFileListStr = JSON.stringify(_this.checkedFileIdList);
      _this.jobitemProperty.loginUserId = _this.loginUser.customerUserId;
      // 已经将文件权限进行处理
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/jobitem-property/saveJobitemProperty', _this.jobitemProperty).then((response) => {
        let resp = response.data;
        if (resp.success) {
          Toast.success("保存成功", Swal);
        } else {
          Toast.warning(resp.message, Swal);
        }
      })
    },

    confirmDeclare(declareInfo){
      let _this = this;
      // console.log("declareInfo=", declareInfo);
      _this.submitJobitemProperty();
    },

    submitJobitemProperty() {
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
      _this.jobitemProperty.jobItemId = _this.jobitemId;
      _this.jobitemProperty.fileListStr = JSON.stringify(_this.resFileIds);
      _this.jobitemProperty.referenceFileListStr = JSON.stringify(_this.checkedFileIdList);
      _this.jobitemProperty.loginUserId = _this.loginUser.customerUserId;
      // 已完成文件权限程序处理
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/jobitem-property/submitJobitemProperty', _this.jobitemProperty).then((response) => {
        let resp = response.data;
        if (resp.success) {
          Toast.success("保存成功", Swal);
          if(_this.entrance === ENTRANCE_CLASS.MANHOUR.key){
            _this.$bus.$emit('close-manhour-tab', _this.index);
            _this.$bus.$emit('notify-jobitem-count-flag', true);  //执行管理
            _this.$bus.$emit('notify-develop-count-flag', true);  //研发管理
            _this.$bus.$emit('notify-project-job-flag', true);   //项目管理
            _this.$bus.$emit('notify-product-job-flag', true);   //产品管理
            _this.$bus.$emit('notify-character-review-count-flag', true);  //评审管理
          }
        } else {
          Toast.warning(resp.message, Swal);
        }
      })

/*      Swal.fire({
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

        }
      })*/
    },

    showJobitemProcess() {
      let _this = this;
      _this.showJobitemProcessDlg = true;
      _this.dialogTitle = "任务处理过程";
    },
    warnChildrenOpen() {
      Toast.warning("存在未关闭的子任务，子任务完成后才能提交", Swal);
    },
    showManhourDeclare() {
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
    handleClose(done) {
      let _this = this;
      _this.showJobitemProcessDlg = false;
      _this.manhourDeclareDlg = false;
      done();
    },

    closeDialog() {
      let _this = this;
      _this.showJobitemProcessDlg = false;
      _this.manhourDeclareDlg = false;
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

    afterUpload(resp) {
      let _this = this;
      _this.resFile = resp.content;
      _this.resFile.customerId = _this.loginCustomer.id;
      _this.resFile.projectId = _this.jobItem.projectId;
      _this.resFile.subprojectId = _this.jobItem.subprojectId;
      _this.resFile.operatorId = _this.loginUser.customerUserId;
      _this.resFile.itemId = _this.jobItem.id;
      _this.saveFile(_this.resFile); // 将上传完成的数据保存到数据库
      _this.$forceUpdate();
    },

    saveFile(file){
      let _this = this;
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/file/rdms/save', file).then((response)=>{
        let resp = response.data;
        if (resp.success) {
          let file = resp.content;
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

.paper-height {
  height: calc(100vh - 200px);
}

</style>
