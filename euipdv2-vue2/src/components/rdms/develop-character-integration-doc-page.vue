<!--
 * Author: 周朔鹏
 * Date: 2025-02-23
 * Email: 391902958@qq.com
 * Website: https://www.euipd.com
-->

<template>
  <div style="height: 100%; width: 100%;" >
    <div class="paper-height">
      <div style="height: 100%; overflow: scroll; ">
        <div class="a4-paper-big">
          <div style="margin: 10px 15px 10px 0;">
            <div style="line-height: 32px;">
              <div style="font-weight: bold; font-size: 15px; background-color: #F5F5F5" >
                <span>特性名称 : </span> <a href="javascript:void(0)" @click="showCharacterDlg(characterDoc.id)"> {{ characterDoc.characterName }} </a>
                <span style="float: right; margin-left: 5px;"><small> {{ characterDoc.iterationVersion | iterationFormat }}</small></span>
                <span style="float: right;"> <small>特性编号 : </small> </span>
              </div>
              <div style="font-weight: bold;">
                <i class="el-icon-magic-stick"></i> <span>特性描述 : </span>
              </div>
              <div style="padding-left: 20px;" v-rich-html="characterDoc.functionDescription">
              </div>
            </div>
            <div v-if="characterDoc.fileList">
              <div style="padding-left: 15px; font-weight: bold;">
                <i class="el-icon-paperclip"></i> <span>特性定义附件 : </span>
              </div>
              <div v-for="(file, i) in characterDoc.fileList" :key="('character-' + (i))" style="padding-left: 20px;">
                <div v-if="file.authStatus" style="line-height: 25px;">
                  <a href="javascript:void(0)" @click="downloadFile(file)">  {{ file.name }} </a>
                </div>
                <div v-else style="line-height: 25px;">
                  {{ file.name }}
                </div>
              </div>
            </div>
            <div v-if="characterDoc.jobItemFileList">
              <div style="padding-left: 15px; font-weight: bold;">
                <i class="el-icon-paperclip"></i> <span>任务过程附件 : </span>
              </div>
              <div v-for="(file, i) in characterDoc.jobItemFileList" :key="('character-job-' + (i))" style="padding-left: 20px;">
                <div v-if="file.authStatus" style="line-height: 25px;">
                  <a href="javascript:void(0)" @click="downloadFile(file)">  {{ file.name }} </a>
                </div>
                <div v-else style="line-height: 25px;">
                  {{ file.name }}
                </div>
              </div>
            </div>
            <div v-for="(jobitem, i) in characterDoc.jobitemDocList" :key="i" style="line-height: 26px;">
              <div class="hr hr-10 dotted hr-double"></div>
              <div style="padding-left: 15px;">
                <div style="font-weight: bold; font-size: 15px; background-color: #F5F5F5">
                  <i class="el-icon-document"></i> <span>任务名称 : </span> <a href="javascript:void(0)" @click="showJobitemDlg(jobitem)"> {{ jobitem.jobName }} </a>
                  <span style="float: right; margin-left: 5px;"><small> {{ jobitem.jobSerial }}</small></span>
                  <span style="float: right;"> <small>任务号 : </small> </span>
                </div>
                <div>
                  <div style="padding-left: 15px; font-weight: bold;">
                    <i class="el-icon-magic-stick"></i> <span>工作内容 : </span>
                  </div>
                  <div style="padding-left: 20px;" v-rich-html="jobitem.taskDescription">
                  </div>
                </div>
                <div v-if="jobitem.propertyDto">
                  <div>
                    <div style="padding-left: 15px; font-weight: bold;">
                      <i class="el-icon-magic-stick"></i> <span>交付说明 : </span>
                    </div>
                    <div style="padding-left: 20px;" v-rich-html="jobitem.propertyDto.jobDescription">
                    </div>
                  </div>
                  <div v-if="jobitem.propertyDto.fileItems">
                    <div style="padding-left: 15px; font-weight: bold;">
                      <i class="el-icon-paperclip"></i> <span>任务交付附件 : </span>
                    </div>
                    <div v-for="(file, i) in jobitem.propertyDto.fileItems" :key="('submit-' + (i))" style="padding-left: 20px;">
                      <div v-if="file.authStatus" style="line-height: 25px;">
                        <a href="javascript:void(0)" @click="downloadFile(file)">  {{ file.name }} </a>
                      </div>
                      <div v-else style="line-height: 25px;">
                        {{ file.name }}
                      </div>
                    </div>
                  </div>
                </div>
                <div v-if="jobitem.fileList">
                  <div style="padding-left: 15px; font-weight: bold;">
                    <i class="el-icon-paperclip"></i> <span>任务任务附件 : </span>
                  </div>
                  <div v-for="(file, i) in jobitem.fileList" :key="('task-' + (i))"  style="padding-left: 20px;">
                    <div v-if="file.authStatus" style="line-height: 25px;">
                      <a href="javascript:void(0)" @click="downloadFile(file)">  {{ file.name }} </a>
                    </div>
                    <div v-else style="line-height: 25px;">
                      {{ file.name }}
                    </div>
                  </div>
                </div>
                <div v-if="jobitem.processFileList">
                  <div style="padding-left: 15px; font-weight: bold;">
                    <i class="el-icon-paperclip"></i> <span>任务流转附件 : </span>
                  </div>
                  <div v-for="(file, i) in jobitem.processFileList" :key="('process-' + (i))"  style="padding-left: 20px;">
                    <div v-if="file.authStatus" style="line-height: 25px;">
                      <a href="javascript:void(0)" @click="downloadFile(file)">  {{ file.name }} </a>
                    </div>
                    <div v-else style="line-height: 25px;">
                      {{ file.name }}
                    </div>
                  </div>
                </div>
                <div class="hr hr-8 dotted"></div>
                <div v-for="(testJob, key) in jobitem.testJobitemList" :key="key">
                  <div>
                    <div  style="font-weight: bold; font-size: 14px;">
                      <i class="el-icon-cpu"></i> <span>测试任务名称 : </span> <a href="javascript:void(0)"  @click="showJobitemDlg(testJob)"> {{ testJob.jobName }} </a>
                      <span style="float: right; margin-left: 5px;"><small> {{ testJob.jobSerial }}</small></span>
                      <span style="float: right;"> <small>测试单号 : </small> </span>
                    </div>
                    <div style="padding-left: 15px; font-weight: bold;">
                      <i class="el-icon-magic-stick"></i> <span>测试描述 : </span>
                    </div>
                    <div style="padding-left: 20px;" v-rich-html="testJob.taskDescription">
                    </div>
                  </div>
                  <div v-if="testJob.propertyDto && testJob.propertyDto.fileItems">
                    <div style="padding-left: 15px; font-weight: bold;">
                      <i class="el-icon-paperclip"></i> <span>任务交付附件 : </span>
                    </div>
                    <div v-for="(file, i) in testJob.propertyDto.fileItems" :key="('test-submit-' + (i))" style="padding-left: 20px;">
                      <div v-if="file.authStatus" style="line-height: 25px;">
                        <a href="javascript:void(0)" @click="downloadFile(file)">  {{ file.name }} </a>
                      </div>
                      <div v-else style="line-height: 25px;">
                        {{ file.name }}
                      </div>
                    </div>
                  </div>
                  <div v-if="testJob.fileList">
                    <div style="padding-left: 15px; font-weight: bold;">
                      <i class="el-icon-paperclip"></i> <span>任务任务附件 : </span>
                    </div>
                    <div v-for="(file, i) in testJob.fileList" :key="('test-task-' + (i))" style="padding-left: 20px;">
                      <div v-if="file.authStatus" style="line-height: 25px;">
                        <a href="javascript:void(0)" @click="downloadFile(file.path)">  {{ file.name }} </a>
                      </div>
                      <div v-else style="line-height: 25px;">
                        {{ file.name }}
                      </div>
                    </div>
                  </div>
                  <div v-if="testJob.processFileList">
                    <div style="padding-left: 15px; font-weight: bold;">
                      <i class="el-icon-paperclip"></i> <span>任务流转附件 : </span>
                    </div>
                    <div v-for="(file, i) in testJob.processFileList" :key="('process-' + (i))"  style="padding-left: 20px;">
                      <div v-if="file.authStatus" style="line-height: 25px;">
                        <a href="javascript:void(0)" @click="downloadFile(file.path)">  {{ file.name }} </a>
                      </div>
                      <div v-else style="line-height: 25px;">
                        {{ file.name }}
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

  <!--显示特性详细信息-->
    <el-dialog
        width="29cm"
        :visible.sync="showCharacterDialog"
        :before-close="handleClose"
        :close-on-click-modal=false>
      <div slot="title" style="font-size: 20px;">{{ dialogTitle }}</div>
      <div>
        <CharacterDetailInfoCoreAll
            :key="characterDoc.id"
            :character-id="characterDoc.id"
        ></CharacterDetailInfoCoreAll>
      </div>
    </el-dialog>

<!--  显示任务信息  -->
    <el-dialog
        width="29cm"
        :visible.sync="showJobitemDialog"
        :before-close="handleClose"
        :close-on-click-modal=false>
      <div slot="title" style="font-size: 20px;">{{ dialogTitle }}</div>
      <div>
        <JobitemDetailInfoDlg
            :key="jobitem.id"
            :jobitem-id="jobitem.id"
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
import {iterationFormat, shortDateFormat} from "@/filter/filter";

import CharacterDetailInfoCoreAll from "@/components/std/character-detail-info-core-all.vue";
import JobitemDetailInfoDlg from "@/components/std/jobitem-detail-info-dlg.vue";

export default {
  components: {
    FileRdms,
    FileSaver,
    CharacterDetailInfoCoreAll,
    JobitemDetailInfoDlg,
  },
  name: "develop-character-integration-doc-page",

  props: {
    characterId: {
      default: null,
    },
    docType: {
      default: null
    }
  },

  data() {
    return {
      loginUser: {},
      loginCustomer: {},

      showCharacterDialog: false,
      showJobitemDialog: false,
      dialogTitle: '',

      characterDoc: {},
      jobitem: {},

    };
  },

  computed: {},

  mounted: function () {
    let _this = this;
    _this.getLoginUser();
    if(_this.docType === DOC_TYPE.CHARACTER.key && Tool.isNotEmpty(_this.characterId)){
      _this.getHmiCharacterDocDto(_this.characterId);
    }
  },

  beforeDestroy(){
    let _this = this;
  },

  methods: {
    iterationFormat,
    dateFormat(row, column) {
      return shortDateFormat(row.planSubmissionTime);
    },

    closeDialog() {
      let _this = this;
      _this.showCharacterDialog = false;
      _this.showJobitemDialog = false;
    },
    handleClose(done) {
      done(); //el-dialog用于关闭对话框的内置函数.
    },

    showCharacterDlg() {
      let _this = this;
      _this.showCharacterDialog = true;
      _this.dialogTitle = "特性定义信息";
    },

    showJobitemDlg(jobitem) {
      let _this = this;
      _this.jobitem = jobitem;
      _this.showJobitemDialog = true;
      _this.dialogTitle = "任务信息";
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

    getHmiCharacterDocDto(characterId) {
      let _this = this;
      let formData = new window.FormData();
      formData.append('characterId', characterId);
      formData.append('loginUserId', _this.loginUser.customerUserId);
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/character/getHmiCharacterDocDto', formData).then((response) => {
        let resp = response.data;
        if (resp.success) {
          _this.characterDoc = resp.content;
          _this.$forceUpdate();
        } else {
          Toast.warning(resp.message, Swal);
        }
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
@import './../../../public/static/scss/caipd.scss';

.paper-height {
  height: calc(100vh - 250px);
}

</style>
