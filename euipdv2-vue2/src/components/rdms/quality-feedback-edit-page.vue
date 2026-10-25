<!--
 * Author: 周朔鹏
 * Date: 2024-11-17
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
                  <div>质量编号: {{ qualityItem.qualitySerial }}</div>
                </div>
              </el-col>
            </el-row>
            <div>
              <table class="table-out">
                <tr>
                  <table class="table-inn">
                    <tr>
                      <th style="width: 160px; text-align: center;">
                        <span>特性名称</span>
                      </th>
                      <th>
                        <span style="margin-left: 10px;">
                          {{ qualityItem.characterName }}
                        </span>
                      </th>
                      <th style="width: 120px; text-align: center;">
                        <span>子项目名称</span>
                      </th>
                      <th>
                        <span style="margin-left: 10px;">
                          {{ qualityItem.subprojectName }}
                        </span>
                      </th>
                    </tr>
                    <tr>
                      <th style="width: 160px; text-align: center;">
                        <span>质量任务编号</span>
                      </th>
                      <th>
                        <span style="margin-left: 10px;">
                          {{ qualityItem.jobSerial }}
                        </span>
                      </th>
                      <th style="width: 120px; text-align: center;">
                        <span>任务名称</span>
                      </th>
                      <th>
                        <span style="margin-left: 10px;">
                         {{ qualityItem.jobName }}
                        </span>
                      </th>
                    </tr>
                    <tr>
                      <th style="width: 160px; text-align: center;">
                        <span>工作执行人</span>
                      </th>
                      <th>
                        <span style="margin-left: 10px;">
                          {{ qualityItem.executorName }}
                        </span>
                      </th>
                      <th style="width: 120px; text-align: center;">
                        <span>任务完成时间</span>
                      </th>
                      <th>
                        <span style="margin-left: 10px;">
                          {{ qualityItem.jobCompleteTime | dateFormat }}
                        </span>
                      </th>
                    </tr>
                    <tr>
                      <th style="width: 160px; text-align: center;">
                        <span>产品经理</span>
                      </th>
                      <th>
                        <span style="margin-left: 10px;">
                          {{ qualityItem.productManagerName }}
                        </span>
                      </th>
                      <th style="width: 120px; text-align: center;">
                        <span>项目经理</span>
                      </th>
                      <th>
                        <span style="margin-left: 10px;">
                         {{ qualityItem.projectManagerName }}
                        </span>
                      </th>
                    </tr>
                  </table>
                </tr>
                <tr>
                  <table class="table-inn">
                    <tr style="border-top: 1px solid #1a1a1a;border-left: 1px solid #1a1a1a;border-right: 1px solid #1a1a1a;">
                      <th style="width: 19.39%; text-align: center;">
                        <span>处理及结论描述</span>
                      </th>
                      <th style="width: 48.78%;">
                    <span style="margin-left: 10px;">
                      填表说明: 对质量问题处理进行概要描述, 明确处理结论.
                    </span>
                      </th>
                      <th style="width: 31.71%;">
                  <span style="margin-left: 10px;">
                    字数 <small><em>(不计空白与格式)</em></small>: {{ getDescriptionFontNum }} / 5000
                  </span>
                      </th>
                    </tr>
                  </table>
                </tr>
                <tr>
                  <table class="table-inn">
                    <tr style="border-top: 1px solid #1a1a1a; border-left: 1px solid #1a1a1a; border-right: 1px solid #1a1a1a;">
                      <rich-editor
                          key="character-desc"
                          v-model="qualityItem.feedbackDescription"
                          placeholder="请输入正文（不计空白与格式，5000 字以内）"
                          :max-length="5000"
                          :show-count="false"
                      />
                    </tr>
                  </table>
                </tr>
              </table>
            </div>
            <div class="hr hr-5 dotted"></div>
            <div style="margin-right: 75px;">
              <FileUploadCore
                  :input-id="('quality-feedback-edit-page-' + (qualityItem.id))"
                  :key="('file-upload-' + (qualityItem.id))"
                  :x-project-id="getXProjectId"
                  :item-id="qualityItem.id"
                  :file-group="FILE_GROUPING.PROPERTY.key"
                  :project-type="projectType"
                  :read-db-record="true"
                  :en-delete="true"
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
                <button type="button" v-on:click="saveQualityFeedbackInfo()" class="ca-btn-primary">保 存</button>
                <button v-if="jobCompleteFlag" type="button" v-on:click="submitJobitemProperty()" class="ca-btn-primary">提 交</button>
                <button v-if="!jobCompleteFlag" type="button" class="ca-btn-primary disabled">提 交</button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import FileRdms from "@/components/rdms/file-rdms";
import FileSaver from 'file-saver';
import Swal from "sweetalert2";
import CharacterBudgetConfirmDlg from "@/components/rdms/character-budget-confirm-dlg";
import JobitemDetailInfoDlg from "@/components/std/jobitem-detail-info-dlg.vue";

import RichEditor from '@/components/rich/rich-editor.vue'
import { hasPendingUploads } from '@/components/rich/rich-image'
import {formatSecond, optionKV} from "@/filter/filter";
import FileUploadCore from "@/components/std/file-upload-core.vue";


export default {
  components: {
    FileUploadCore,
    FileRdms,
    FileSaver,
    CharacterBudgetConfirmDlg,
    RichEditor,
    JobitemDetailInfoDlg,

  },
  name: "quality-feedback-edit-page",

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


  data() {
    return {
      loginUser: {},
      loginCustomer: {},

      jobitemId: '',

      PROJECT_TYPE: PROJECT_TYPE,
      FILE_GROUPING: FILE_GROUPING,
      JOB_ITEM_PROCESS: JOB_ITEM_PROCESS,
      DOC_TYPE: DOC_TYPE,
      G_SUFFIXS: G_SUFFIXS,

      dialogTitle: '',

      currentTime: {},
      currentTimeStr: '',

      qualityItem: {},
      jobCompleteFlag: false,

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
        return  this.qualityItem.preProjectId;
      }
      if(this.projectType === PROJECT_TYPE.PROJECT.key){
        return  this.qualityItem.projectId;
      }
      if(this.projectType === PROJECT_TYPE.SUB_PROJECT.key || this.projectType === PROJECT_TYPE.DEV_PROJECT.key){
        return  this.qualityItem.subprojectId;
      }
      return 0;
    },

    getDescriptionFontNum() {
      if (this.qualityItem?.feedbackDescription) {
        return Tool.richTextLength(this.qualityItem.feedbackDescription);
      }
      return 0;
    },

  },

  mounted: function () {
    let _this = this;
    _this.random = Math.random();
    _this.currentTime = new Date();
    _this.currentTimeStr = _this.currentTime.toLocaleString()
    _this.getLoginUser();
    if(Tool.isNotEmpty(_this.qualityId)){
      _this.getQualityDetailInfo(_this.qualityId);
    }

    _this.$bus.$on('refresh-quality-feedback-edit-page', (data) => {
      if(data){
        _this.getQualityDetailInfo(_this.qualityId);
      }
    });
  },

  beforeDestroy() {
    let _this = this;
    _this.$bus.$off('refresh-quality-feedback-edit-page');
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

    getQualityDetailInfo(qualityId){
      let _this = this;
      let formData = new window.FormData();
      formData.append('qualityId', qualityId);
      formData.append('loginUserId', _this.loginUser.customerUserId);
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/quality/getQualityDetailInfo', formData).then((response)=>{
        let resp = response.data;
        if (resp.success) {
          _this.qualityItem = resp.content;
          _this.jobCompleteFlag = _this.qualityItem.jobCompleteFlag;
        } else {
          Toast.warning(resp.message,Swal);
        }
      })
    },

    saveQualityFeedbackInfo() {
      let _this = this;
      if (hasPendingUploads()) {
        Toast.warning("图片仍在上传，请稍候再保存", Swal);
        return;
      }
      // 保存校验
      if (1 !== 1
          || !Validator.require(_this.qualityItem.feedbackDescription, "反馈描述", Swal)
          || !Validator.richLength(_this.qualityItem.feedbackDescription, "反馈描述", 10, 5000, Swal)
      ) {
        return;
      }
      _this.qualityItem.feedbackFileIdListStr = JSON.stringify(_this.resFileIds);
      _this.qualityItem.loginUserId = _this.loginUser.customerUserId;
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/quality/saveQualityFeedbackInfo', _this.qualityItem).then((response) => {
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
              || !Validator.require(_this.qualityItem.feedbackDescription, "反馈描述", Swal)
              || !Validator.richLength(_this.qualityItem.feedbackDescription, "反馈描述", 10, 5000, Swal)
          ) {
            return;
          }
          _this.qualityItem.feedbackFileIdListStr = JSON.stringify(_this.resFileIds);
          _this.qualityItem.loginUserId = _this.loginUser.customerUserId;
          _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/quality/submitQualityFeedbackInfo', _this.qualityItem).then((response) => {
            let resp = response.data;
            if (resp.success) {
              Toast.success("保存成功", Swal);
              _this.$bus.$emit('close-quality-tab', _this.index);  //关闭manhour tab
              _this.$bus.$emit('notify-jobitem-count-flag', true);  //执行管理
              _this.$bus.$emit('refresh-quality-list', true);
              _this.$bus.$emit('notify-quality-count-flag', true); //质量管理

            } else {
              Toast.warning(resp.message, Swal);
            }
          })
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
