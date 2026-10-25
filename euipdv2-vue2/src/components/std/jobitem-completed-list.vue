<!--
 * Author: 周朔鹏
 * Date: 2025-03-02
 * Email: 391902958@qq.com
 * Website: https://www.euipd.com
-->

<template>
  <div style="height: 100%;" >
    <div class="a4-paper-big-simple">
      <div style="margin: 30px 30px; ">
        <div class="paper-content">
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
              style="width: 100%;"
              highlight-current-row
              @row-click="rowClick">
            <el-table-column   label="序号" align="center" width="80">
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
                label="执行人员"
                align="center"
                width="80">
            </el-table-column>
            <el-table-column
                prop="createTime"
                :formatter="createTimeFormat"
                label="开始时间"
                align="center"
                width="100">
            </el-table-column>
            <el-table-column
                prop="actualSubmissionTime"
                :formatter="planSubmissionTimeFormat"
                label="提交时间"
                align="center"
                width="100">
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

<!--  显示任务详细件  -->
    <el-dialog
        width="29cm"
        :visible.sync="showJobitemDetailDlg"
        :before-close="handleClose"
        :close-on-click-modal=false>
      <div slot="title" style="font-size: 20px;">{{ dialogTitle }}</div>
      <div v-if="showJobitemDetailDlg">
        <JobitemRelatedJobInfoDlg
            :key="jobItem.id"
            :jobitem-id="jobItem.id"
        ></JobitemRelatedJobInfoDlg>
      </div>
    </el-dialog>

  </div>
</template>

<script>
import FileRdms from "@/components/rdms/file-rdms";
import FileSaver from 'file-saver';
import Swal from "sweetalert2";
import printJS from "print-js";
import {formatSecond, shortDateFormat} from "@/filter/filter";
import JobitemRelatedJobInfoDlg from "@/components/std/jobitem-related-job-info-dlg.vue";


export default {
  components: {
    FileRdms,
    FileSaver,
    JobitemRelatedJobInfoDlg,
  },
  name: "jobitem-completed-list",

  props: {
    xId:{
      default: null,
    // characterId jobitemId projectId qualityId etc.
    },
    docType: {
      default: null,
      //character jobitem project quality etc.
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

      characterId: '',
      projectId: '',

      requestPath: '',
      isReviewing: false,
      completeFlag: false,

      showJobitemDetailDlg: false,
      dialogTitle: '',

      buttonShowFlag: false,

      jobItems: [],
      jobItem: {},
      reviewJobitem: false,

      PROJECT_TYPE: PROJECT_TYPE,
      JOB_ITEM_TYPE: JOB_ITEM_TYPE,
      DOC_TYPE: DOC_TYPE,

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
    _this.buttonShowFlag = false;
    _this.getLoginUser();

    _this.getCompletedJobitemList(_this.xId);

    _this.$bus.$on('refresh-jobitem-completed-list', (data) => {
      if(data){
        _this.getCompletedJobitemList(_this.xId);
      }
    });
  },

  beforeDestroy(){
    let _this = this;
    _this.$bus.$off('refresh-jobitem-completed-list');
  },

  methods: {
    createTimeFormat(row, column) {
      return shortDateFormat(row.createTime);
    },

    planSubmissionTimeFormat(row, column) {
      return shortDateFormat(row.planSubmissionTime);
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
    },
    handleClose(done) {
      done(); //el-dialog用于关闭对话框的内置函数.
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

    getCompletedJobitemList(xId) {
      let _this = this;
      switch (_this.docType) {
        case DOC_TYPE.CHARACTER.key: {
          if(Tool.isNotEmpty(xId)){
            if(_this.jobType === JOB_ITEM_TYPE.TASK_TEST.key){
              let characterId = xId;
              _this.requestPath = '/customer/job-items/getCompletedJobitemList_testTask/' + characterId;
            }else{
              let characterId = xId;
              _this.requestPath = '/customer/job-items/getCompletedJobitemList/' + characterId;
            }
          }
          break;
        }
        case DOC_TYPE.SUB_PROJECT.key:
        {
          if(Tool.isNotEmpty(xId)){
            let subprojectId = xId;
            _this.requestPath = '/customer/job-items/getCompletedJobitemListBySubProjectId/' + subprojectId;
          }
          break;
        }
        case DOC_TYPE.PROJECT.key:
        {
          if(Tool.isNotEmpty(xId)){
            let projectId = xId;
            _this.requestPath = '/customer/job-items/getCompletedJobitemListByProjectId/' + projectId;
          }
          break;
        }
        case DOC_TYPE.QUALITY.key: {
          if(Tool.isNotEmpty(xId)){
            let qualityId = xId;
            _this.requestPath = '/customer/job-items/getCompletedJobitemListByQualityId/' + qualityId;
          }
          break;
        }
        case DOC_TYPE.BUG.key: {
          if(Tool.isNotEmpty(xId)){
            let bugfeedbackId = xId;
            _this.requestPath = '/customer/job-items/getCompletedJobitemListByBugfeedbackId/' + bugfeedbackId;
          }
          break;
        }
        case DOC_TYPE.PRE_PROJECT.key: {
          if(Tool.isNotEmpty(xId)){
            let qualityId = xId;
            _this.requestPath = '/customer/job-items/getCompletedJobitemListByPreProjectId/' + qualityId;
          }
          break;
        }
      }

      _this.$ajax.post(process.env.VUE_APP_SERVER + _this.requestPath ).then((response) => {
        let resp = response.data;
        if (resp.success) {
          _this.jobItems = resp.content.completeJobitemList;
          _this.sumJobNum = resp.content.sumJobNum;
          _this.completeJobNum = resp.content.completeJobNum;
          _this.completeFlag = resp.content.completeFlag;
          _this.isReviewing = resp.content.isReviewing;

          _this.$bus.$emit('refresh-jobitem-completed-list', false);

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
