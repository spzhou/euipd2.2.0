<!--
 * Author: 周朔鹏
 * Date: 2024-11-03
 * Email: 391902958@qq.com
 * Website: https://www.euipd.com
-->

<template>
  <div class="ca-plain-container">
    <div>
      <div>
        <SubprojectShowSummary
            :key="subProject.id"
            :entrance="ENTRANCE_CLASS.MANHOUR.key"
            :sub-project="subProject">
        </SubprojectShowSummary>
      </div>
      <div v-if="reviewJobitemId">
        <div class="hr hr-18 hr-dotted hr-double"></div>
        <JobitemDetailInfoPlain
            :key="reviewJobitemId"
            :jobitem-id="reviewJobitemId"
        ></JobitemDetailInfoPlain>
      </div>

    </div>
    <!--显示任务信息-->
<!--    <el-dialog
        :visible.sync="showJobitemDlg"
        width="29cm"
        :before-close="handleClose"
        :close-on-click-modal=false>
      <div slot="title" style="font-size: 20px;">{{ dialogTitle }}</div>
      <div v-if="showJobitemDlg">
        <JobitemDetailInfoDlg
            :key="jobitem.id"
            :jobitem-id="dlgJobItem.id">
        </JobitemDetailInfoDlg>
      </div>
    </el-dialog>-->
  </div>
</template>

<script>
import FileRdms from "@/components/rdms/file-rdms";
import Swal from "sweetalert2";
import FileSaver from 'file-saver';
import JobitemDetailInfoDlg from "@/components/std/jobitem-detail-info-dlg.vue";
import ProjectInfoOutline from "@/components/rdms/project-info-outline.vue";
import SubprojectShowSummary from "@/components/rdms/subproject-show-summary.vue";
import JobitemDetailInfoPlain from "@/components/std/jobitem-detail-info-plain.vue";

export default {
  components: {
    SubprojectShowSummary,
    JobitemDetailInfoDlg,
    FileRdms,
    ProjectInfoOutline,
    JobitemDetailInfoPlain,
  },
  name: "review-milestone-info-plain",

  props: {
    subProjectId: {
      default: null,
    },
    reviewJobitemId: {
      default: null,
    },
  },

  data: function () {
    return {
      loginUser: {},
      loginCustomer: {},

      subProject: {},

      jobitem: {
        propertyDto: {
          jobDescription: '',
        },
      },
      reviewJobitemList: [],

      dlgJobItem: {},
      dialogTitle: '',
      // showJobitemDlg: false,

      fileItems: [],
      resFile: {},
      resFiles: [],
      resFileIds: [],
      ENTRANCE_CLASS: ENTRANCE_CLASS,
    }
  },

  computed: {},

  mounted: function () {
    let _this = this;
    _this.getLoginUser();
    if(Tool.isNotEmpty(_this.subProjectId)){
      // _this.getSubprojectReviewInfoList(_this.subProjectId);
      _this.getSubProjectInfo(_this.subProjectId);
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

    getSubProjectInfo(subProjectId){
      let _this = this;
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/sub-project/getSubProjectSimpleInfo/' + subProjectId).then((response) => {
        let resp = response.data;
        if (resp.success) {
          _this.subProject = resp.content;
        } else {
          Toast.warning(resp.message, Swal);
        }
      })
    },

    getSubprojectReviewInfoList(subProjectId) {
      let _this = this;
      let formData = new window.FormData();
      formData.append('subProjectId', subProjectId);
      formData.append('loginUserId', _this.loginUser.customerUserId);
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/sub-project/getSubprojectReviewInfoList', formData).then((response) => {
        let resp = response.data;
        if (resp.success) {
          _this.reviewJobitemList = resp.content;

        }else{
          Toast.warning(resp.message, Swal);
        }
      });
    },

/*    showJobitemInfo(jobitem){
      let _this = this;
      _this.dialogTitle = '任务信息';
      _this.dlgJobItem = jobitem;
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
    },*/

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
