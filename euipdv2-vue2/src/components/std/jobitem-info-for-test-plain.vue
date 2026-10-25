<!--
 * Author: 周朔鹏
 * Date: 2025-02-24
 * Email: 391902958@qq.com
 * Website: https://www.euipd.com
-->

<template>
  <div class="original-container" >
    <div style="display: flex; flex-direction: row; line-height: 30px; font-weight: bold;">
      <div style="width: 90px; text-align: right;">
        {{ ((preFix) + '任务') }}:
      </div>
      <div style="line-height: 30px; margin-left: 6px;">
        <a href="javascript:void(0)" @click="showJobitemInfo(jobItem)">{{ jobItem.testedJobName }}</a>
      </div>
      <div class="hr hr-6 dotted"></div>
    </div>
    <div style="display: flex; flex-direction: row; line-height: 30px; font-weight: bold;">
      <div style="width: 90px; text-align: right;">
        任务号:
      </div>
      <div style="line-height: 30px; margin-left: 6px;">
        {{ jobItem.testedJobSerial }}
      </div>
      <div class="hr hr-6 dotted"></div>
    </div>
    <div>
      <div style="text-align: left; line-height: 30px; font-weight: bold;">
        任务描述:
      </div>
      <div style="margin-left: 15px; line-height: 21px;"
           v-rich-html="jobItem.testedTaskDescription">
      </div>
      <div class="hr hr-6 dotted"></div>
    </div>
    <div v-if="jobItem.testedPropertyDto && jobItem.testedPropertyDto.jobDescription">
      <div style="text-align: left; line-height: 30px; font-weight: bold;">
        工作描述:
      </div>
      <div style="margin-left: 15px; line-height: 21px;"
           v-rich-html="jobItem.testedPropertyDto.jobDescription">
      </div>
      <div class="hr hr-6 dotted"></div>
    </div>
    <div style="line-height: 30px;"  v-if="jobItem.testedPropertyDto && jobItem.testedPropertyDto.fileItems && jobItem.testedPropertyDto.fileItems.length>0">
      <div style="font-weight: bold;">
        任务交付件:
      </div>
      <div v-for="(item, i ) in jobItem.testedPropertyDto.fileItems" :key="item.id+'-'+i" style="display: flex; flex-direction: row; margin-left: 16px;">
        <div v-if="item">
          {{ item.name }}
        </div>
        <div v-if="item && item.authStatus">
          <el-button type="text" style="margin-left: 25px;" @click="downloadFile(item)">下载</el-button>
        </div>
      </div>
      <div class="hr hr-6 dotted"></div>
    </div>
    <div style="line-height: 30px;"  v-if="jobItem.testedPropertyDto && jobItem.testedPropertyDto.referenceFileList && jobItem.testedPropertyDto.referenceFileList.length>0">
      <div style="font-weight: bold;">
        参考文献:
      </div>
      <div v-for="(item, i ) in jobItem.testedPropertyDto.referenceFileList" :key="item.id+'-'+i" style="display: flex; flex-direction: row; margin-left: 16px;">
        <div v-if="item">
          {{ item.name }}
        </div>
        <div v-if="item && item.authStatus">
          <el-button type="text" style="margin-left: 25px;" @click="downloadFile(item)">下载</el-button>
        </div>
      </div>
      <div class="hr hr-6 dotted"></div>
    </div>
    <div class="hr hr-18 dotted hr-double"></div>
    <div v-for="(assist, i) in jobItem.bugJobitemList" :key="('assist-' + (i))">
      <div style="display: flex; flex-direction: row; line-height: 30px; font-weight: bold;">
        <div style="width: 90px; text-align: right;">
          缺陷处理:
        </div>
        <div style="line-height: 30px; margin-left: 6px;">
          <a href="javascript:void(0)" @click="showJobitemInfo(assist)">{{ assist.jobName }}</a>
        </div>
        <div class="hr hr-6 dotted"></div>
      </div>
      <div style="display: flex; flex-direction: row; line-height: 30px; font-weight: bold;">
        <div style="width: 90px; text-align: right;">
          任务号:
        </div>
        <div style="line-height: 30px; margin-left: 6px;">
          {{ assist.jobSerial }}
        </div>
        <div class="hr hr-6 dotted"></div>
      </div>
      <div>
        <div style="text-align: left; line-height: 30px; font-weight: bold;">
          任务描述:
        </div>
        <div style="margin-left: 15px; line-height: 21px;"
             v-rich-html="assist.taskDescription">
        </div>
        <div class="hr hr-6 dotted"></div>
      </div>
      <div>
        <div style="text-align: left; line-height: 30px; font-weight: bold;">
          工作描述:
        </div>
        <div  v-if="assist.propertyDto && assist.propertyDto.jobDescription" style="margin-left: 15px; line-height: 21px;"
             v-rich-html="assist.propertyDto.jobDescription">
        </div>
        <div class="hr hr-6 dotted"></div>
      </div>
      <div style="line-height: 30px;"  v-if="assist.propertyDto && assist.propertyDto.fileItems && assist.propertyDto.fileItems.length > 0">
        <div style="font-weight: bold;">
          任务交付件:
        </div>
        <div v-for="(item, i ) in assist.propertyDto.fileItems" :key="item.id+'-'+i" style="display: flex; flex-direction: row; margin-left: 16px;">
          <div v-if="item">
            {{ item.name }}
          </div>
          <div v-if="item && item.authStatus">
            <el-button type="text" style="margin-left: 25px;" @click="downloadFile(item)">下载</el-button>
          </div>
        </div>
        <div class="hr hr-6 dotted"></div>
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
            :key="dlgJobItem.id"
            :jobitem-id="dlgJobItem.id">
        </JobitemDetailInfoDlg>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import FileRdms from "@/components/rdms/file-rdms.vue";
import Swal from "sweetalert2";
import FileSaver from 'file-saver';
import JobitemDetailInfoDlg from "@/components/std/jobitem-detail-info-dlg.vue";
import JobitemOwnInfoPlain from "@/components/std/jobitem-own-info-plain.vue";

export default {
  components: {
    JobitemOwnInfoPlain,
    JobitemDetailInfoDlg,
    FileRdms,
  },
  name: "jobitem-info-for-test-plain",

  props: {
    jobitemId: {
      default: null,
    },
    preFix: {
      default: '',
    }
  },

  data: function () {
    return {
      loginUser: {},
      loginCustomer: {},

      jobItem: {},

      dlgJobItem: {},
      testJobList: [],

      dialogTitle: '',
      showJobitemDlg: false,

      resFile: {},
      resFiles: [],
      resFileIds: [],
    }
  },

  computed: {
  },

  mounted: function () {
    let _this = this;
    _this.getLoginUser();
    if(Tool.isNotEmpty(_this.jobitemId)){
      _this.getTestJobitemAuxPlainInfo(_this.jobitemId);
    }

    _this.$bus.$on('refresh-jobitem-info-for-test-plain', (data)=>{
      if(data){
        _this.getTestJobitemAuxPlainInfo(_this.jobitemId);
      }
    });
  },

  beforeDestroy() {
    let _this = this;
    _this.$bus.$off('refresh-jobitem-info-for-test-plain');
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

    getTestJobitemAuxPlainInfo(jobitemId) {
      let _this = this;
      let formData = new window.FormData();
      formData.append('jobitemId', jobitemId);
      formData.append('loginUserId', _this.loginUser.customerUserId);
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/job-items/getTestJobitemAuxPlainInfo', formData).then((response) => {
        let resp = response.data;
        if (resp.success) {
          _this.jobItem = resp.content;
        }else{
          Toast.warning(resp.message, Swal);
        }
      });
    },

    showJobitemInfo(jobItem){
      let _this = this;
      _this.dialogTitle = '任务信息';
      _this.dlgJobItem = jobItem;
      _this.showJobitemDlg = true;

    },
    handleClose(done) {
      let _this = this;
      _this.showJobitemDlg = false;
      done(); //el-dialog用于关闭对话框的内置函数.
    },

    closeDialog() {
      let _this = this;
      _this.showJobitemDlg = false;
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
@import '../../../public/static/scss/caipd';

.original-container{
  height: 100%;
  margin: 12px 2px 5px 15px;
  padding-right: 10px;
  //min-width: 360px;
  overflow-y: scroll;
}
</style>
