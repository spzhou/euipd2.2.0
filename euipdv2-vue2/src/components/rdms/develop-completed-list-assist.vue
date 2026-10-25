<!--
 * Author: 周朔鹏
 * Date: 2024-05-26
 * Email: 391902958@qq.com
 * Website: https://www.euipd.com
-->

<template>
  <div style="height: 100%; width: 100%;" >
    <div class="paper-height">
      <div style="height: 100%; overflow: scroll; ">
        <div class="a4-paper-big">
          <div style="float: right; margin-top: 10px;">
            {{ ('已完成任务数/已下发任务数: ' + (completeJobNum) + '/' + (sumJobNum)) }}
          </div>
          <el-table
              ref="completedList"
              :data="jobItems"
              row-key="id"
              border
              default-expand-all
              stripe
              style="width: 100%; height: calc(100vh - 278px)"
              highlight-current-row
              @row-click="rowClick">
            <el-table-column   label="序号" align="center" width="50">
              <template slot-scope="scope">
                {{scope.$index + 1}}
              </template>
            </el-table-column>
            <el-table-column
                prop="jobSerial"
                label="任务号"
                align="center"
                width="170">
            </el-table-column>
            <el-table-column
                prop="jobName"
                label="任务名称">
            </el-table-column>
            <el-table-column
                prop="executorName"
                label="开发人员"
                align="center"
                width="80">
            </el-table-column>
            <el-table-column
                prop="createTime"
                :formatter="dateFormat"
                label="开始时间"
                align="center"
                width="100">
            </el-table-column>
            <el-table-column
                prop="actualSubmissionTime"
                :formatter="dateFormatActual"
                label="提交时间"
                align="center"
                width="100">
            </el-table-column>
            <el-table-column
                prop="executorName"
                label="提前/延期"
                align="center"
                width="90">
            </el-table-column>
            <el-table-column
                prop="evaluateScore"
                label="评分"
                align="center"
                width="50">
            </el-table-column>
          </el-table>
        </div>
      </div>
    </div>

<!--  显示任务交付件  -->
    <el-dialog
        width="29cm"
        :visible.sync="showJobitemDetailDlg"
        :before-close="handleClose"
        :close-on-click-modal=false>
      <div slot="title" style="font-size: 20px;">{{ dialogTitle }}</div>
      <div>
        <JobitemRelatedJobInfoDlg
            :id="'property-show-dlg-complete-list'"
            :key="jobItem.id"
            :jobitem-id="jobItem.id"
            :project-type="PROJECT_TYPE.SUB_PROJECT.key"
        ></JobitemRelatedJobInfoDlg>
      </div>
    </el-dialog>

    <!--提交系统集成任务-->
        <el-dialog
            width="29cm"
            :visible.sync="issueCharacterTestDlg"
            :before-close="handleClose"
            :close-on-click-modal=false>
          <div slot="title" style="font-size: 20px;">{{ dialogTitle }}</div>
          <div v-if="issueCharacterTestDlg">
            <JobitemCreateByJobitemIdDlg
                :key="('assist-' + (jobitemId))"
                :jobitem-id="jobitemId"
                :index="index"
                :project-type="PROJECT_TYPE.SUB_PROJECT.key"
                :job-type="JOB_ITEM_TYPE.TEST.key"
                :entrance="ENTRANCE_CLASS.DEVELOP.key"
                :close-dlg = closeDialog
            ></JobitemCreateByJobitemIdDlg>
          </div>
        </el-dialog>
  </div>
</template>

<script>
import FileRdms from "@/components/rdms/file-rdms";
import FileSaver from 'file-saver';
import Swal from "sweetalert2";
import printJS from "print-js";
import {shortDateFormat} from "@/filter/filter";
import JobitemRelatedJobInfoDlg from "@/components/std/jobitem-related-job-info-dlg.vue";
import JobitemCreateByJobitemIdDlg from "@/components/std/jobitem-create-by-jobitem-id-dlg.vue";

export default {
  components: {
    FileRdms,
    FileSaver,
    JobitemRelatedJobInfoDlg,
    JobitemCreateByJobitemIdDlg,
  },
  name: "develop-completed-list-assist",

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

      showJobitemDetailDlg: false,
      issueCharacterTestDlg: false,
      dialogTitle: '',

      jobItems: [],
      jobItem: {},
      PROJECT_TYPE: PROJECT_TYPE,
      JOB_ITEM_TYPE: JOB_ITEM_TYPE,
      ENTRANCE_CLASS: ENTRANCE_CLASS,

      sumJobNum: 0,
      completeJobNum: 0,

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

  computed: {},

  mounted: function () {
    let _this = this;
    _this.getLoginUser();
    if(Tool.isNotEmpty(_this.jobitemId)){
      _this.getCompletedAssistJobitemList(_this.jobitemId);
    }

    _this.$bus.$on('refresh-develop-completed-list-assist', (data) => {
      if(data){
        _this.getCompletedAssistJobitemList(_this.jobitemId);
      }
    });

  },

  beforeDestroy(){
    let _this = this;
    _this.$bus.$off('refresh-develop-completed-list-assist');
  },

  methods: {
    dateFormat(row, column) {
      return shortDateFormat(row.planSubmissionTime);
    },

    dateFormatActual(row, column) {
      return shortDateFormat(row.actualSubmissionTime);
    },

    rowClick(row, column, e){
      let _this = this;
      _this.jobItem = row;
      _this.showJobitemDetailDlg = true;
      _this.dialogTitle = "开发任务";
    },
    closeDialog() {
      let _this = this;
      _this.showJobitemDetailDlg = false;
      _this.issueCharacterTestDlg = false;
    },
    handleClose(done) {
      done(); //el-dialog用于关闭对话框的内置函数.
    },

    issueCharacterTest() {
      let _this = this;
      _this.issueCharacterTestDlg = true;
      _this.dialogTitle = "测试工单";
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

    getCompletedAssistJobitemList(jobitemId) {
      let _this = this;
      let path = '';
      if(_this.jobType===JOB_ITEM_TYPE.TEST.key){
        path = '/customer/job-items/getCompletedBugJobitemList/';
      }else{
        path = '/customer/job-items/getCompletedAssistJobitemList/';
      }
      _this.$ajax.post(process.env.VUE_APP_SERVER + path + jobitemId).then((response) => {
        let resp = response.data;
        if (resp.success) {
          _this.jobItems = resp.content.completeJobitemList;
          _this.sumJobNum = resp.content.sumJobNum;
          _this.completeJobNum = resp.content.completeJobNum;
          _this.$bus.$emit('refresh-develop-completed-list-assist', false);
        } else {
          Toast.warning(resp.message, Swal);
        }
      })
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
  height: calc(100vh - 200px);
}

</style>
