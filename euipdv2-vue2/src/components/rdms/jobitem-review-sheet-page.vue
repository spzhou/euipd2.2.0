<!--
 * Author: 周朔鹏
 * Date: 2024-11-17
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
                  <table class="table-head">
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
                        请尽快完成
                        <!--                        {{ jobItem.planSubmissionTime | dateFormat }}-->
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
                <tr>
                  <table class="table-inn">
                    <tr style="border-top: 2px solid #1a1a1a;">
                      <th style="width: 19.51%; text-align: center;">
                        <span>评审意见</span>
                      </th>
                      <th style="width: 48.78%;">
                    <span style="margin-left: 10px;">
                      <small><em>(填表说明: 请忠实描述您对工作提交情况和提交成果的意见或建议.)</em></small>
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
                    <tr style="border-top: 1px solid #1a1a1a; border-right: 1px solid #1a1a1a; border-left: 1px solid #1a1a1a;">
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
              </table>
              <div style="text-align: right; margin-right: 60px; margin-top: -15px;">
                <i class="el-icon-info"></i>
                <small><em>备注: 最终评审结论将由评审组长在综合所有评审委员的意见后,
                  同意做出"通过""驳回"或"有条件通过"等评审结论.</em></small>
              </div>
            </div>
            <div class="hr hr-18 dotted"></div>
            <el-row>
              <el-col :span="22">
                <div v-show="resFiles && resFiles.length>0"
                     style="display: flex; align-items: end; flex-direction: column; margin-bottom: 15px;">
                  <div v-for="(item, i) in resFiles" :key="i"
                       style="display: flex; flex-direction: row; align-items: center; margin: 5px 0 0 50px;">
                    <div v-if="item">
                      {{ item.name }}
                    </div>
                    <div v-if="item && item.authStatus">
                      <button type="button" class="ca-btn-primary-sm" style="margin-left: 50px;"
                              @click="downloadFile(item)">下载
                      </button>
                    </div>
                  </div>
                </div>
                <div style="text-align: right;">
                  <file-rdms v-bind:input-id="'file-upload-jobitem-submit-sheet'"
                             v-bind:text="'上传附件'"
                             v-bind:suffixs=G_SUFFIXS
                             v-bind:customer="loginCustomer.id"
                             v-bind:use="FILE_GROUPING.JOBITEM.key"
                             v-bind:folder="'jobitem'"
                             v-bind:after-upload="afterUpload">
                  </file-rdms>
                  <div style="margin-top: 8px;">
                    注1: 允许上传文件格式: [{{((G_SUFFIXS))}}]
                  </div>
                </div>
              </el-col>
            </el-row>
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


export default {
  components: {
    FileRdms,
    FileSaver,
    CharacterBudgetConfirmDlg,
    RichEditor,
    JobitemDetailInfoDlg,

  },
  name: "jobitem-review-sheet-page",

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
      G_SUFFIXS: G_SUFFIXS,

      showJobitemProcessDlg: false,
      dialogTitle: '',

      currentTime: {},
      currentTimeStr: '',

      jobItem: {},
      jobitemProperty: {
        jobDescription: '',
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

  mounted: function () {
    let _this = this;
    _this.random = Math.random();
    _this.currentTime = new Date();
    _this.currentTimeStr = _this.currentTime.toLocaleString()
    _this.getLoginUser();
    if (Tool.isNotEmpty(_this.jobitemId)) {
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

    getJobitemDetailInfo(jobitemId) {
      let _this = this;
      let formData = new window.FormData();
      formData.append('jobitemId', jobitemId);
      formData.append('loginUserId', _this.loginUser.customerUserId);
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/job-items/getJobitemDetailInfo', formData).then((response) => {
        let resp = response.data;
        if (resp.success) {
          _this.jobItem = resp.content;
          _this.getJobitemProperty(_this.jobItem.id);
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
            _this.fileItems = _this.jobitemProperty.fileItems;
            // debugger
            if (_this.fileItems !== null) {
              for (let i = 0; i < _this.fileItems.length; i++) {
                _this.resFiles.push(_this.fileItems[i]);
                _this.resFileIds.push(_this.fileItems[i].id);
              }
            }
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

    submitJobitemProperty() {
      let _this = this;
      if (hasPendingUploads()) {
        Toast.warning("图片仍在上传，请稍候再保存", Swal);
        return;
      }
      // 保存校验
      if (1 !== 1
          || !Validator.require(_this.jobitemProperty.jobDescription, "工作描述", Swal)
          || !Validator.richLength(_this.jobitemProperty.jobDescription, "工作描述", 10, 5000, Swal)
      ) {
        return;
      }

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
          _this.jobitemProperty.jobItemId = _this.jobitemId;
          _this.jobitemProperty.fileListStr = JSON.stringify(_this.resFileIds);
          _this.jobitemProperty.loginUserId = _this.loginUser.customerUserId;
          _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/jobitem-property/submitJobitemProperty', _this.jobitemProperty).then((response) => {
            let resp = response.data;
            if (resp.success) {
              Toast.success("保存成功", Swal);
              _this.$bus.$emit('close-manhour-tab', _this.index);
              _this.$bus.$emit('notify-jobitem-count-flag', true);  //执行管理
              _this.$bus.$emit('notify-character-review-count-flag', true);  //评审管理
              _this.$bus.$emit('refresh-manhour-jobitem-list', true);  //执行页面 任务列表
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
    getSignedUrl(fileFullName, suffix) {
      let _this = this;
      let formData = new window.FormData();
      formData.append('fileFullName', fileFullName);
      formData.append('customer', _this.loginCustomer.id);
      formData.append('user', _this.loginUser.customerUserId);
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/file/rdms/oss-get-signed-url', formData).then((response) => {
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
          if (!_this.resFiles) {
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
