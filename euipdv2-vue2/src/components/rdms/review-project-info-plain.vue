<!--
 * Author: 周朔鹏
 * Date: 2024-11-03
 * Email: 391902958@qq.com
 * Website: https://www.euipd.com
-->

<template>
  <div class="ca-plain-container" >
    <div style="display: flex; flex-direction: row; line-height: 30px; font-weight: bold;">
      <div style="width: 90px; text-align: right;">
        项目名称:
      </div>
      <div style="line-height: 30px; margin-left: 6px;">
        <a href="javascript:void(0)" @click="showJobitemInfo(projectDto)">{{ projectDto.projectName }}</a>
      </div>
      <div class="hr hr-6 dotted"></div>
    </div>
    <div style="display: flex; flex-direction: row; line-height: 30px; font-weight: bold;">
      <div style="width: 90px; text-align: right;">
        项目号:
      </div>
      <div style="line-height: 30px; margin-left: 6px;">
        {{ projectDto.projectCode }}
      </div>
    </div>
    <div style="display: flex; flex-direction: row; line-height: 30px; font-weight: bold;">
      <div style="width: 90px; text-align: right;">
        监管委员:
      </div>
      <div style="line-height: 30px; margin-left: 6px;">
        {{ projectDto.superviseName }}
      </div>
    </div>
    <div style="display: flex; flex-direction: row; line-height: 30px; font-weight: bold;">
      <div style="width: 90px; text-align: right;">
        产品经理:
      </div>
      <div style="line-height: 30px; margin-left: 6px;">
        {{ projectDto.productManagerName }}
      </div>
    </div>
    <div style="display: flex; flex-direction: row; line-height: 30px; font-weight: bold;">
      <div style="width: 90px; text-align: right;">
        项目经理:
      </div>
      <div style="line-height: 30px; margin-left: 6px;">
        {{ projectDto.projectManagerName }}
      </div>
    </div>
    <div style="display: flex; flex-direction: row; line-height: 30px; font-weight: bold;">
      <div style="width: 90px; text-align: right;">
        测试主管:
      </div>
      <div style="line-height: 30px; margin-left: 6px;">
        {{ projectDto.testManagerName }}
      </div>
    </div>
    <div style="display: flex; flex-direction: row; line-height: 30px; font-weight: bold;">
      <div style="width: 90px; text-align: right;">
        核心成员:
      </div>
      <div style="line-height: 30px; margin-left: 6px;">
        {{ keyMemberNameListStr }}
      </div>
    </div>
    <div style="display: flex; flex-direction: row; line-height: 30px; font-weight: bold;">
      <div style="width: 90px; text-align: right;">
        项目预算:
      </div>
      <div style="line-height: 30px; margin-left: 6px;">
        {{ projectDto.budget }}
      </div>
    </div>
    <div class="hr hr-6 dotted"></div>
    <div>
      <div style="text-align: left; line-height: 30px; font-weight: bold;">
        任务描述:
      </div>
      <div style="margin-left: 15px; line-height: 21px;"
           v-rich-html="projectDto.projectIntroduce">
      </div>
      <div class="hr hr-6 dotted"></div>
    </div>
    <div>
      <div style="text-align: left; line-height: 30px; font-weight: bold;">
        激励政策:
      </div>
      <div style="margin-left: 15px; line-height: 21px;"
           v-rich-html="projectDto.incentivePolicy">
      </div>
      <div class="hr hr-6 dotted"></div>
    </div>

    <!--    TODO 需要调试-->
    <div v-for="(item, index) in reviewJobitemList">
      <JobitemOwnInfoPlain
          :key="item.id"
          :jobitem-id="item.id"
          :project-type="item.projectType"
          :index="index"
          :entrance="entrance"
      ></JobitemOwnInfoPlain>
    </div>

    <!--    TODO 需要调试-->
    <div v-for="(item, index) in subProjectReviewDetailList">
      <SubProjectReviewShowPlain
          :key="item.id"
          :subproject-review-detail="item"
          :job-type="jobType"
          :project-type="projectType"
          :index="index"
          :entrance="entrance"
      ></SubProjectReviewShowPlain>
    </div>


    <!--  显示任务信息--><!--    TODO 需要调试-->
    <el-dialog
        :visible.sync="showJobitemDlg"
        width="29cm"
        :before-close="handleClose"
        :close-on-click-modal=false>
      <div slot="title" style="font-size: 20px;">{{ dialogTitle }}</div>
      <div v-if="showJobitemDlg">
        <JobitemDetailInfoDlg
            :key="projectId.id"
            :jobitem-id="dlgJobItem.id"
            :job-type="jobType"
            :project-type="projectType"
            :index="index"
            :entrance="entrance">
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
import CharacterReviewShowPlain from "@/components/std/character-review-show-plain.vue";
import SubProjectReviewShowPlain from "@/components/std/subproject-review-show-plain.vue";
import JobitemOwnInfoPlain from "@/components/std/jobitem-own-info-plain.vue";

export default {
  components: {
    JobitemOwnInfoPlain,
    SubProjectReviewShowPlain,
    CharacterReviewShowPlain,
    JobitemDetailInfoDlg,
    FileRdms,
  },
  name: "review-project-info-plain",

  props: {
    projectId: {
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
    excludeJobitemId: {
      default: null,
    }
  },

  data: function () {
    return {
      loginUser: {},
      loginCustomer: {},

      keyMemberNameListStr: '',
      dlgJobItem: {},

      projectDto: {},
      reviewJobitemList: [],
      subProjectReviewDetailList: [],

      dialogTitle: '',
      showJobitemDlg: false,
    }
  },

  computed: {},

  mounted: function () {
    let _this = this;
    _this.getLoginUser();
    if (Tool.isNotEmpty(_this.projectId)) {
      _this.getProjectReviewDetailInfo(_this.projectId);
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

    getProjectReviewDetailInfo(projectId) {
      let _this = this;
      let formData = new window.FormData();
      formData.append('projectId', projectId);
      formData.append('loginUserId', _this.loginUser.customerUserId);
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/project/getProjectReviewDetailInfo', formData).then((response) => {
        let resp = response.data;
        if (resp.success) {
          _this.projectDto = resp.content.projectDto;
          _this.projectDto.keyMemberList.forEach(item => {
            _this.keyMemberNameListStr += item + ' ';
          })
          _this.reviewJobitemList = resp.content.reviewJobitemList;
          _this.subProjectReviewDetailList = resp.content.subProjectReviewDetailList;
        } else {
          Toast.warning(resp.message, Swal);
        }
      })
    },


    showJobitemInfo(projectId) {
      let _this = this;
      _this.dialogTitle = '任务信息';
      _this.dlgJobItem = projectId;
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

</style>
