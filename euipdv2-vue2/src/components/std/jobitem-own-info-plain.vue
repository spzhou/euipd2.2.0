<!--
 * Author: 周朔鹏
 * Date: 2025-03-04
 * Email: 391902958@qq.com
 * Website: https://www.euipd.com
-->

<template>
  <div class="ca-plain-container" >
    <div class="ca-plain-form">
      <div style="display: flex;flex-direction: row; justify-content: space-between;">
        <div class="ca-form-line" style="font-weight: bold">
          <div class="ca-form-label-f">
            <div class="ca-form-label-title">
              <i class="el-icon-star-on"></i>
              {{ ((prefix) + '任务名称:')}}
            </div>
          </div>
          <div class="ca-form-content">
            <a href="javascript:void(0)" @click="showJobitemInfo()">{{ jobItem.jobName }}</a>
<!--            <span style="margin-left: 5px;">-->
<!--            <small><em>( <i class="fa fa-hand-o-left" aria-hidden="true"></i>查看任务流转记录 )</em></small>-->
<!--            </span>-->
          </div>
        </div>
        <div v-if="enReject && in24Hour" style="margin-right: 15px;">
          <el-button size="mini" @click="refuseTheJob">拒收</el-button>
        </div>
      </div>
      <div class="ca-form-line">
        <div class="ca-form-label">
          <div class="ca-form-label-title">
            任务号:
          </div>
        </div>
        <div class="ca-form-content">
          {{ jobItem.jobSerial }}
        </div>
      </div>
      <div v-if="jobItem.taskDescription">
        <div class="ca-form-line">
          <div class="ca-form-label">
            <div class="ca-form-label-title">
              任务描述:
            </div>
          </div>
        </div>
        <div class="ca-form-line-content"
             v-rich-html="jobItem.taskDescription">
        </div>
      </div>
      <div v-if="jobItem.fileList && jobItem.fileList.length>0">
        <div class="ca-form-line">
          <div class="ca-form-label">
            <div class="ca-form-label-link">
              任务附件:
            </div>
          </div>
        </div>
        <div v-for="(item, i ) in jobItem.fileList" :key="item.id+'-'+i"
             style="display: flex; flex-direction: row; align-items: center; margin-left: 16px;">
          <div v-if="item">
            {{ item.name }}
          </div>
          <div v-if="item && item.authStatus">
            <el-button type="text" style="margin-left: 25px;" @click="downloadFile(item)">下载</el-button>
          </div>
        </div>
      </div>
      <div v-if="jobItem.processFileList && jobItem.processFileList.length>0">
        <div class="ca-form-line">
          <div class="ca-form-label">
            <div class="ca-form-label-link">
              流转附件:
            </div>
          </div>
        </div>
        <div v-for="(item, i ) in jobItem.processFileList" :key="item.id+'-'+i"
             style="display: flex; flex-direction: row; align-items: center; margin-left: 16px;">
          <div v-if="item">
            {{ item.name }}
          </div>
          <div v-if="item && item.authStatus">
            <el-button type="text" style="margin-left: 25px;" @click="downloadFile(item)">下载</el-button>
          </div>
        </div>
      </div>
      <div v-if="jobItem.propertyDto && jobItem.propertyDto.jobDescription">
        <div class="ca-form-line">
          <div class="ca-form-label">
            <div class="ca-form-label-title">
              工作内容:
            </div>
          </div>
        </div>
        <div class="ca-form-line-content"
             v-rich-html="jobItem.propertyDto.jobDescription">
        </div>
      </div>
      <div v-if="jobItem.propertyDto && jobItem.propertyDto.fileItems && jobItem.propertyDto.fileItems.length >0">
        <div class="ca-form-line">
          <div class="ca-form-label">
            <div class="ca-form-label-link">
              交付附件:
            </div>
          </div>
        </div>
        <div v-for="(item, i ) in jobItem.propertyDto.fileItems" :key="item.id+'-'+i"
             style="display: flex; flex-direction: row; align-items: center; margin-left: 16px;">
          <div v-if="item">
            {{ item.name }}
          </div>
          <div v-if="item && item.authStatus">
            <el-button type="text" style="margin-left: 25px;" @click="downloadFile(item)">下载</el-button>
          </div>
        </div>
      </div>
      <div v-if="jobItem.propertyDto && jobItem.propertyDto.referenceFileList && jobItem.propertyDto.referenceFileList.length >0">
        <div class="ca-form-line">
          <div class="ca-form-label">
            <div class="ca-form-label-link">
              参考文献:
            </div>
          </div>
        </div>
        <div v-for="(item, i ) in jobItem.propertyDto.referenceFileList" :key="item.id+'-'+i"
             style="display: flex; flex-direction: row; align-items: center; margin-left: 16px;">
          <div v-if="item">
            {{ item.name }}
          </div>
          <div v-if="item && item.authStatus">
            <el-button type="text" style="margin-left: 25px;" @click="downloadFile(item)">下载</el-button>
          </div>
        </div>
      </div>
    </div>

    <!--显示任务信息-->
    <el-dialog
        :visible.sync="showJobitemDlg"
        width="29cm"
        :before-close="handleClose"
        :close-on-click-modal=false>
      <div slot="title" style="font-size: 20px;">{{ dialogTitle }}</div>
      <div v-if="showJobitemDlg">
        <JobitemDetailInfoDlg
            :key="jobItem.id"
            :jobitem-id="jobItem.id">
        </JobitemDetailInfoDlg>
      </div>
    </el-dialog>

    <!--拒收任务-->
    <el-dialog
        :visible.sync="refuseJobitemDlg"
        width="29cm"
        :before-close="handleClose"
        :close-on-click-modal=false>
      <div slot="title" style="font-size: 20px;">{{ dialogTitle }}</div>
      <div>
        <JobitemRefuseDlg
            :key="jobitemId"
            :jobitem-id="jobitemId"
            :project-type="projectType"
            :index="index"
            :close-dlg="closeDialog">
        </JobitemRefuseDlg>
      </div>
    </el-dialog>

  </div>
</template>

<script>
import FileRdms from "@/components/rdms/file-rdms.vue";
import Swal from "sweetalert2";
import FileSaver from 'file-saver';
import JobitemDetailInfoDlg from "@/components/std/jobitem-detail-info-dlg.vue";
import JobitemRefuseDlg from "@/components/rdms/jobitem-refuse-dlg.vue";

export default {
  components: {
    JobitemDetailInfoDlg,
    JobitemRefuseDlg,
    FileRdms,
  },
  name: "jobitem-own-info-plain",

  props: {
    jobitemId: {
      default: null,
    },
    enReject: {
      default: false,
    },
    prefix: {
      default: '',
    },
    index: {
      default: null
    },
    projectType: {
      default: null,
    },
    jobType: {
      default: null,
    },
    entrance: {
      default: null,
    },
  },

  data: function () {
    return {
      loginUser: {},
      loginCustomer: {},

      jobItem: {},

      dialogTitle: '',
      showJobitemDlg: false,
      refuseJobitemDlg: false,

      resFile: {},
      resFiles: [],
      resFileIds: [],
      JOB_ITEM_STATUS: JOB_ITEM_STATUS,
      JOB_ITEM_TYPE: JOB_ITEM_TYPE,
      JOB_ITEM_PROCESS: JOB_ITEM_PROCESS,
    }
  },

  computed: {
    in24Hour(){
      let _this = this;
      //以jobitem.createTime为基准,判断是否在24小时内
      let now = new Date();
      let createTime = new Date(_this.jobItem.createTime);
      let diff = now.getTime() - createTime.getTime();
      return diff < 86400000;

    }
  },

  mounted: function () {
    let _this = this;
    _this.getLoginUser();
    if(Tool.isNotEmpty(_this.jobitemId)){
      _this.getJobitemRecordDetailInfo(_this.jobitemId);
    }
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

    getJobitemRecordDetailInfo(jobitemId) {
      let _this = this;
      let formData = new window.FormData();
      formData.append('jobitemId', jobitemId);
      formData.append('loginUserId', _this.loginUser.customerUserId);
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/job-items/getJobitemRecordDetailInfo', formData).then((response) => {
        let resp = response.data;
        if (resp.success) {
          _this.jobItem = resp.content;
        }else{
          Toast.warning(resp.message, Swal);
        }
      });
    },

    showJobitemInfo(){
      let _this = this;
      _this.dialogTitle = '任务信息';
      _this.showJobitemDlg = true;

    },
    refuseTheJob(){
      let _this = this;
      _this.dialogTitle = '任务拒收';
      _this.refuseJobitemDlg = true;

    },

    handleClose(done) {
      done();
    },

    closeDialog() {
      let _this = this;
      _this.showJobitemDlg = false;
      _this.refuseJobitemDlg = false;
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

  }
}
</script>

<style lang="scss" scoped>
@import './../../../public/static/scss/caipd.scss';

</style>
