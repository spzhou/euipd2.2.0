<!--
 * Author: 周朔鹏
 * Date: 2024-11-05
 * Email: 391902958@qq.com
 * Website: https://www.euipd.com
-->

<template>
  <div style="height: 100%;">
    <div class="a4-paper-big-plain">
      <div>
        <JobitemOwnInfoCore
            :key="jobitemId"
            :jobitem-id="jobitemId"
            :pre-fix="''"
        ></JobitemOwnInfoCore>
      </div>

      <div v-if="assistJobIdList !== null">
        <div style="margin-left: 55px; font-weight: bold">
          协作任务列表
        </div>
        <div class="hr hr-5 dotted"></div>
        <div v-for="( itemId, i ) in assistJobIdList" :key="i">
          <JobitemOwnInfoCore
              :key="itemId"
              :jobitem-id="itemId"
              :pre-fix="'协作'"
          ></JobitemOwnInfoCore>
        </div>
      </div>

      <div v-if="testJobIdList !== null">
        <div style="margin-left: 55px; font-weight: bold">
          测试情况部分
        </div>
        <div class="hr hr-5 dotted"></div>
        <div v-for="( itemId, i ) in testJobIdList" :key="i">
          <JobitemOwnInfoCore
              :key="itemId"
              :jobitem-id="itemId"
              :pre-fix="'测试'"
          ></JobitemOwnInfoCore>
        </div>
      </div>

    </div>
  </div>
</template>

<script>
import FileRdms from "@/components/rdms/file-rdms.vue";
import FileSaver from 'file-saver';
import Swal from "sweetalert2";
import printJS from "print-js";
import JobitemOwnInfoCore from "@/components/std/jobitem-own-info-core.vue";

export default {
  components: {
    FileRdms,
    FileSaver,
    JobitemOwnInfoCore,
  },
  name: "jobitem-related-job-info-dlg",

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

      jobItem: {},
      testJobIdList: [],
      assistJobIdList: [],

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

  },

  mounted: function () {
    let _this = this;
    _this.getLoginUser();
    if(Tool.isNotEmpty(_this.jobitemId)){
      _this.getJobitemSimpleInfo(_this.jobitemId);
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

    getJobitemSimpleInfo(jobitemId){
      let _this = this;
      let formData = new window.FormData();
      formData.append('jobitemId', jobitemId);
      formData.append('loginUserId', _this.loginUser.customerUserId);
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/job-items/getJobitemSimpleInfo', formData).then((response) => {
        let resp = response.data;
        if (resp.success) {
          _this.jobItem = resp.content;
          _this.testJobIdList = _this.jobItem.testJobitemIdList;
          _this.assistJobIdList = _this.jobItem.assistJobitemIdList;
        }else{
          Toast.warning(resp.message, Swal);
        }
      });
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

  },
}
</script>

<style lang="scss" scoped>
@import './../../../public/static/scss/caipd.scss';

.paper-height {
  height: calc(100vh - 316px);
}

</style>
