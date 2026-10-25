<!--
 * Author: 周朔鹏
 * Date: 2025-04-15
 * Email: 391902958@qq.com
 * Website: https://www.euipd.com
-->

<template>
  <div style="height: 100%;">
    <div class="paper-height">
      <div style="height: 100%; overflow: scroll;"> <!--  overflow-x: hidden -->
        <div class="a4-paper-big-simple">
          <div style="margin: 30px 0 0;">
            <el-row style="margin-top: 10px; margin-left: 55px; padding-right: 20px;">
              <el-col :span="23">
                <div style="display: flex; justify-content: space-between;">
                  <div>任务号: {{ jobItem.jobSerial }}</div>
                  <div>任务状态: <a href="javascript:void(0);" @click="showJobitemProcess">
                    {{ JOB_ITEM_PROCESS | optionKV(jobItem.processStatus) }} </a></div>
                </div>
              </el-col>
            </el-row>
            <div>
              <table class="table-out">
                <tr>
                  <table  class="table-head">
                    <tr style="border-bottom: 1px solid #1a1a1a;">
                      <th style="width: 100%; text-align: center; font-size: 16px; font-weight: bold;">
                        <span>评审会签表</span>
                      </th>
                    </tr>
                  </table>
                </tr>
                <tr>
                  <table class="table-inn">
                    <tr>
                      <th style="width: 120px; text-align: center;">
                        <span>评审标题</span>
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
                        <span>工作执行人</span>
                      </th>
                      <th>
                  <span style="margin-left: 10px;">
                    {{ jobItem.executorName }}
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
                        <span>评审组长</span>
                      </th>
                      <th>
                      <span style="margin-left: 10px;">
                        {{ jobItem.reviewGroupLeaderName }}
                      </span>
                      </th>
                      <th style="width: 120px; text-align: center;">
                        <span>计划提交时间</span>
                      </th>
                      <th>
                      <span style="margin-left: 10px;">
                        <span v-if="jobItem.planSubmissionTime">
                          {{ jobItem.planSubmissionTime | dateFormat }}
                        </span>
                        <span v-else>
                          请尽快完成
                        </span>
                      </span>
                      </th>
                    </tr>
                  </table>
                </tr>
                <!--  交付附件-->
                <tr v-if="jobItem.fileList !==null && jobItem.fileList !== undefined && jobItem.fileList.length > 0">
                  <table class="table-inn">
                    <tr style="border-top: 2px solid #1a1a1a;">
                      <th style="width: 160px; text-align: center;">
                        <span>评审申请附件</span>
                      </th>
                      <th>
                        <span style="margin-left: 10px;">
                          <small><em>(内容说明: 任务交付时, 提交的工作交付成果)</em></small>
                        </span>
                      </th>
                    </tr>
                  </table>
                </tr>
                <tr v-if="jobItem.fileList !==null && jobItem.fileList !== undefined && jobItem.fileList.length > 0">
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

                <div  v-for="(reviewInfo, i) in otherReviewJobitemList" :key="i">
                  <tr>
                    <table class="table-inn">
                      <tr style="border-top: 2px solid #1a1a1a; width: 100%;">
                        <th style="width: 19.51%; text-align: center;">
                          <span>委员会签意见</span>
                        </th>
                        <th>
                        <span style="margin-left: 10px;">
                          <small><em>说明: 评审委员对特性开发的评审意见.</em></small>
                        </span>
                        </th>
                        <th style="width: 12.2%; text-align: center;">
                          评审委员
                        </th>
                        <th style="width: 12.2%; text-align: center;">
                          {{ reviewInfo.executorName ? reviewInfo.executorName : '' }}
                        </th>
                        <th style="width: 12.2%; text-align: center;">
                          提交时间
                        </th>
                        <th style="width: 15.12%; text-align: center;">
                          <div v-if="reviewInfo.status === JOB_ITEM_STATUS.SUBMIT.key && reviewInfo.propertyDto.createTime">
                            {{ reviewInfo.propertyDto.createTime | dateFormat }}
                          </div>
                          <div v-else>
                            尚未提交
                          </div>
                        </th>
                      </tr>
                    </table>
                  </tr>
                  <tr>
                    <table class="table-inn">
                      <tr>
                        <th>
                          <div style="width: 880px; padding: 0 8px; min-height: 50px; word-wrap:break-word;" v-rich-html="reviewInfo.propertyDto ? reviewInfo.propertyDto.jobDescription : '尚未提交'"></div>
                        </th>
                      </tr>
                    </table>
                  </tr>
                  <tr v-if="reviewInfo.propertyDto && reviewInfo.propertyDto.fileItems">
                    <table class="table-inn">
                      <tr>
                        <th>
                          <div v-for="(item, i) in reviewInfo.propertyDto.fileItems" :key="i" style="display: flex; flex-direction: row; align-items: center; margin-left: 15px;">
                            <div v-if="item">
                              {{ item.name }}
                            </div>
                            <div v-if="item && item.authStatus">
                              <el-button type="text" style="margin-left: 20px;"
                                         @click="downloadFile(item)">下载
                              </el-button>
                            </div>
                          </div>
                        </th>
                      </tr>
                    </table>
                  </tr>

                </div>
                <!--产品经理填写部分-->
                <tr>
                  <table class="table-inn">
                    <tr style="border-top: 2px solid #1a1a1a;">
                      <th style="width: 19.51%; text-align: center;">
                        <span>评审意见</span>
                      </th>
                      <th style="width: 48.78%;">
                  <span style="margin-left: 10px;">
                    <small><em>填表说明: 请提供具体的明确的评审意见. 如需增补材料请在此处详细说明.</em></small>
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
                    <tr  style="border: 1px solid #1a1a1a;">
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
                <tr v-if="jobType !== JOB_ITEM_TYPE.REVIEW_MILESTONE.key">
                  <table class="table-inn">
                    <tr style="border-top: 1px solid #1a1a1a; width: 100%;">
                      <th style="width: 160px; text-align: center;">
                        <span style="font-size: 15px; font-weight: bold;">评审结论:</span>
                      </th>
                      <th>
                        <div style="margin-left: 20px; line-height: 30px; margin-top: 5px;">
                          <el-radio-group v-model="reviewResult">
                            <el-radio :label="2">通过</el-radio>
                            <el-radio v-if="outSourceStatus !== PROJECT_STATUS.OUT_SOURCE.key" :label="4">通过(有条件)</el-radio>  <!--评审任务通过状态, 产品经理发任务给项目经理, 完成具体的任务-->
                            <el-radio v-if="outSourceStatus !== PROJECT_STATUS.OUT_SOURCE.key" :label="6">增补</el-radio>  <!-- 评审任务, 保持现有状态, 产品经理发任务给项目经理, 增补完成后, 再批准-->
                            <el-radio :label="8">驳回</el-radio>  <!--功能状态回到系统集成阶段, 由项目经理重新发集成任务, 全部完成后, 重新提交审批-->
                          </el-radio-group>
                        </div>
                      </th>
                    </tr>
                  </table>
                </tr>
                <tr v-if="reviewResult === 4 || reviewResult === 6">
                  <table class="table-inn">
                    <tr style="border-top: 1px solid #1a1a1a; width: 100%;">
                      <th style="width: 160px; text-align: center;">
                        <span style="font-size: 15px; font-weight: bold;">要求完成时间:</span>
                      </th>
                      <th>
                        <el-date-picker
                            type="date"
                            placeholder="选择日期"
                            :picker-options="pickerOptions"
                            v-model="jobitemProperty.planSubmissionTime">
                        </el-date-picker>
                      </th>
                    </tr>
                  </table>
                </tr>
                <tr v-if="(reviewResult === 2 || reviewResult === 4) && jobType !== JOB_ITEM_TYPE.REVIEW_PRO.key && jobType !== JOB_ITEM_TYPE.REVIEW_MILESTONE.key">
                  <table class="table-inn">
                    <tr style="border-top: 1px solid #1a1a1a; width: 100%;">
                      <th style="width: 160px; text-align: center;">
                        <span style="font-size: 15px; font-weight: bold;">{{ jobType === JOB_ITEM_TYPE.REVIEW_SUBP.key ? '是否发布集成级 CBB:' : '是否作为特性发布:' }}</span>
                      </th>
                      <th>
                        <div style="margin-left: 20px;">
                          <el-radio v-model="isCbb" label="0">{{ jobType === JOB_ITEM_TYPE.REVIEW_SUBP.key ? '不发布' : '普通特性/项目' }}</el-radio>
                          <el-radio v-model="isCbb" label="1"><span style="font-weight: 500;">{{ jobType === JOB_ITEM_TYPE.REVIEW_SUBP.key ? '发布集成级 CBB' : '作为特性发布' }}</span></el-radio>
                        </div>
                      </th>
                    </tr>
                  </table>
                </tr>

              </table>
              <div style="margin-top: -16px; margin-left: 53px;" v-if="jobType === JOB_ITEM_TYPE.REVIEW_MILESTONE.key">
                <span>备注: 里程碑评审结论请在"评审结论"中详细说明; 如需进行其他任务处理, 请在评审后, 通过任务工单的形式, 给相关人员分配任务.</span>
              </div>
            </div>
            <div class="hr hr-18 dotted"></div>
            <div style="margin-right: 75px;">
              <FileUploadCore
                  :input-id="('jobitem-submit-review-sheep-page-' + (jobItem.id))"
                  :key="('review-sheet-page-' + (jobItem.id))"
                  :x-project-id="jobItem.subprojectId"
                  :item-id="jobItem.id"
                  :file-group="FILE_GROUPING.PROPERTY.key"
                  :project-type="projectType"
                  :read-db-record="true"
                  :en-delete="true"
                  v-on:uploadFileList="getUploadFileList">
              </FileUploadCore>
              <div style="text-align: right; margin-right: 10px;">
                <div style="margin-top: 16px;">
                  <small><em>注: 允许上传文件格式: [{{((G_SUFFIXS))}}]</em></small>
                </div>
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
            <div class="clearfix form-actions" style="margin-top: 20px;">
              <div style="display: flex; justify-content: center;">
                <el-tooltip content="填写内容作为草稿保存, 不会改变流程状态!" placement="top" effect="light" :open-delay=1000>
                  <el-button type="primary" v-on:click="saveJobitemProperty()" >保存草稿</el-button>
                </el-tooltip>
                <el-tooltip content="提交评审结论, 将结束当前评审过程!" placement="top" effect="light" :open-delay=1000>
                  <el-button v-if="childrenJobComplateFlag" type="primary" v-on:click="submitReviewResult()" >审结提交</el-button>
                  <el-button v-if="!childrenJobComplateFlag" @click.stop="warnChildrenOpen()"><span style="color: #CCCCCC;">审结提交</span>
                  </el-button>
                </el-tooltip>
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

  </div>
</template>

<script>
import FileRdms from "@/components/rdms/file-rdms.vue";
import FileSaver from 'file-saver';
import Swal from "sweetalert2";
import printJS from "print-js";
import CharacterBudgetConfirmDlg from "@/components/rdms/character-budget-confirm-dlg.vue";
import JobitemDetailInfoDlg from "@/components/std/jobitem-detail-info-dlg.vue";
import FileUploadCore from "@/components/std/file-upload-core.vue";
import RichEditor from '@/components/rich/rich-editor.vue'
import { hasPendingUploads } from '@/components/rich/rich-image'
import JobitemDetailInfoCore from "@/components/std/jobitem-detail-info-core.vue";

export default {
  components: {
    JobitemDetailInfoCore,
    FileRdms,
    FileSaver,
    CharacterBudgetConfirmDlg,
    RichEditor,
    JobitemDetailInfoDlg,
    FileUploadCore,
  },
  name: "review-sheet-page",

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
  },

  data() {
    return {
      loginUser: {},
      loginCustomer: {},

      PROJECT_TYPE: PROJECT_TYPE,
      FILE_GROUPING: FILE_GROUPING,
      JOB_ITEM_PROCESS: JOB_ITEM_PROCESS,
      DOC_TYPE: DOC_TYPE,
      JOB_ITEM_STATUS: JOB_ITEM_STATUS,
      JOB_ITEM_TYPE: JOB_ITEM_TYPE,
      PROJECT_STATUS: PROJECT_STATUS,
      G_SUFFIXS: G_SUFFIXS,

      showJobitemProcessDlg: false,
      dialogTitle: '',

      currentTime: {},
      currentTimeStr: '',

      outSourceStatus: '',

      childrenJobComplateFlag: false,
      jobItem: {},
      jobitemProperty: {
        jobDescription: '',
      },
      supplementJobitemList: [],
      otherReviewJobitemList: [],
      reviewResult: 0,
      isCbb: '0',

      reviewCooStatus: 'AllSubmit',

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

      pickerOptions: {
        disabledDate(v) {
          return v.getTime() < new Date().getTime() - 86400000;//  - 86400000是否包括当天
        }
      },
    };
  },

  mounted: function () {
    let _this = this;
    _this.random = Math.random();
    _this.currentTime = new Date();
    _this.currentTimeStr = _this.currentTime.toLocaleString()
    _this.getLoginUser();
    if (Tool.isNotEmpty(_this.jobitemId)) {
      _this.getReviewJobitemListDetail(_this.jobitemId);
    }
    _this.$bus.$on('refresh-review-sheet-page', (data) => {
      if(data){
        _this.getReviewJobitemListDetail(_this.jobitemId);
      }
    });
  },


  beforeDestroy(){
    let _this = this;
    _this.$bus.$off('refresh-review-sheet-page');
  },

  methods: {
    warnChildrenOpen() {
      Toast.warning("存在未完成的评审派生任务，完成后才能审结", Swal);
    },

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

    getReviewJobitemListDetail(jobitemId) {
      let _this = this;
      let formData = new window.FormData();
      formData.append('jobitemId', jobitemId);
      formData.append('loginUserId', _this.loginUser.customerUserId);
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/job-items/getReviewJobitemListDetail', formData).then((response) => {
        let resp = response.data;
        if (resp.success) {
          _this.jobItem = resp.content.jobitem;
          _this.supplementJobitemList = resp.content.supplementJobitemList;
          _this.otherReviewJobitemList = resp.content.otherReviewJobitemList;
          _this.childrenJobComplateFlag = resp.content.childrenJobComplateFlag;
          _this.outSourceStatus = resp.content.outSourceStatus;
          let submittedStatusList = [JOB_ITEM_STATUS.SUBMIT.key, JOB_ITEM_STATUS.COMPLETED.key, JOB_ITEM_STATUS.ARCHIVED.key, JOB_ITEM_STATUS.CANCEL.key];
          _this.reviewCooStatus = 'AllSubmit';
          _this.otherReviewJobitemList.forEach(item => {
            if(!submittedStatusList.includes(item.status)){
              _this.reviewCooStatus = 'handling';
            }
          })
          _this.getJobitemProperty(_this.jobItem.id);
          _this.$bus.$emit('refresh-review-sheet-page', false);
        } else {
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
          if (Tool.isEmpty(resp.content)) {
            _this.jobitemProperty.jobDescription = '';
            _this.jobitemProperty.testKeyPoints = '';
          } else {
            _this.jobitemProperty = resp.content;
          }
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
          || !Validator.richLength(_this.jobitemProperty.jobDescription, "工作描述", 10, 5000, Swal)
          // || !Validator.greatThanZero(_this.reviewResult, "评审结论" , Swal)
      ) {
        return;
      }
      _this.jobitemProperty.jobItemId = _this.jobitemId;
      _this.jobitemProperty.fileListStr = JSON.stringify(_this.resFileIds);
      _this.jobitemProperty.loginUserId = _this.loginUser.customerUserId;

      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/jobitem-property/saveJobitemProperty', _this.jobitemProperty).then((response) => {
        let resp = response.data;
        if (resp.success) {
          Toast.success("保存成功", Swal);
        } else {
          Toast.warning(resp.message, Swal);
        }
      })
    },

    submitReviewResult() {
      let _this = this;
      if (hasPendingUploads()) {
        Toast.warning("图片仍在上传，请稍候再保存", Swal);
        return;
      }
      // 保存校验
      if(_this.reviewResult === 4 || _this.reviewResult === 6 ){
        if (1 !== 1
            || !Validator.require(_this.jobitemProperty.planSubmissionTime, "计划完成时间", Swal)
        ) {
          return;
        }
      }
      if(_this.jobType === JOB_ITEM_TYPE.REVIEW_MILESTONE.key){
        _this.reviewResult = 2;  //通过
      }

      if (1 !== 1
          || !Validator.require(_this.jobitemProperty.jobDescription, "工作描述", Swal)
          || !Validator.richLength(_this.jobitemProperty.jobDescription, "工作描述", 10, 5000, Swal)
          || !Validator.greatThanZero(_this.reviewResult, "评审结论" , Swal)

      ) {
        return;
      }

      _this.jobitemProperty.jobItemId = _this.jobitemId;
      _this.jobitemProperty.fileListStr = JSON.stringify(_this.resFileIds);
      _this.jobitemProperty.reviewResult = _this.reviewResult;
      _this.jobitemProperty.projectType = _this.projectType;
      _this.jobitemProperty.jobType = _this.jobType;
      _this.jobitemProperty.loginUserId = _this.loginUser.customerUserId;
      _this.jobitemProperty.projectId = _this.jobItem.projectId;
      _this.jobitemProperty.subprojectId = _this.jobItem.subprojectId;
      _this.jobitemProperty.isCbb = _this.isCbb;  //作为特性发布

      Swal.fire({
        title: '确认',
        text: _this.reviewCooStatus === 'AllSubmit' ? "请确认审结意见" : "尚有评审委员未提交意见，是否继续审结？",
        icon: 'question',
        showCancelButton: true,
        confirmButtonColor: '#409EFF',
        cancelButtonColor: '#909399',
        confirmButtonText: '确认!',
        cancelButtonText: '取消!'
      }).then((result) => {
        if (result.value) {
          _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/jobitem-property/submitReviewResult', _this.jobitemProperty).then((response) => {
            let resp = response.data;
            if (resp.success) {
              Toast.success("保存成功", Swal);
              _this.$bus.$emit('close-review-tab', _this.index);
              _this.$bus.$emit('notify-jobitem-count-flag', true);  //执行管理
              _this.$bus.$emit('notify-character-review-count-flag', true);  //评审管理
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

    saveFile(file) {
      let _this = this;
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/file/rdms/save', file).then((response) => {
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
  height: calc(100vh - 255px);
}

</style>
