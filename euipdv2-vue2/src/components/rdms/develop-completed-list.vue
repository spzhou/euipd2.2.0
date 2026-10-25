<!--
 * Author: 周朔鹏
 * Date: 2024-05-26
 * Email: 391902958@qq.com
 * Website: https://www.euipd.com
-->

<template>
  <div style="height: 100%; width: 100%;" >
    <div class="paper-height">
      <div style="height: 100%; overflow: scroll;">
        <div class="a4-paper-big">
          <div v-show=" buttonShowFlag  && !completeFlag &&  completeJobNum === sumJobNum  && sumJobNum !== 0"  style="float: right; margin: 0 0 5px 15px;">
            <el-button type="primary" @click="issueCharacterInt">
              特性集成
            </el-button>
          </div>
          <div v-show=" buttonShowFlag  && completeFlag  &&  completeJobNum === sumJobNum" style="float: right; margin: 0 0 5px 15px;">
            <el-button type="primary" @click="issueReview">
              提交评审
            </el-button>
          </div>
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
                :formatter="dateFormat"
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

    <!--功能集成-->
    <el-dialog
        width="29cm"
        :visible.sync="issueCharacterIntDlg"
        :before-close="handleClose"
        :close-on-click-modal=false>
      <div slot="title" style="font-size: 20px;">{{ dialogTitle }}</div>
      <div>
        <JobitemCreateByCharacterIdDlg
            :key="('character-int-' + (characterId))"
            :character-id="characterId"
            :index="index"
            :job-type="JOB_ITEM_TYPE.CHARACTER_INT.key"
            :project-type="projectType"
            :entrance="entrance"
            :close-dlg=closeDialog
        ></JobitemCreateByCharacterIdDlg>
      </div>
    </el-dialog>

    <!--提交评审-->
    <el-dialog
        width="29cm"
        :visible.sync="issueReviewDlg"
        :before-close="handleClose"
        :close-on-click-modal=false>
      <div slot="title" style="font-size: 20px;">{{ dialogTitle }}</div>
        <ReviewCreateIssueDlg
            :key="('review-issue-' + (characterId))"
            :x-id="characterId"
            :doc-type="DOC_TYPE.CHARACTER.key"
            :job-type="JOB_ITEM_TYPE.REVIEW.key"
            :project-type="projectType"
            :index="index"
            :entrance="entrance"
            :close-dlg=closeDialog
        ></ReviewCreateIssueDlg>
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
import JobitemCreateByCharacterIdDlg from "@/components/std/jobitem-create-by-character-id-dlg.vue";
import ReviewCreateIssueDlg from "@/components/std/review-create-issue-dlg.vue";


export default {
  components: {
    FileRdms,
    FileSaver,
    JobitemRelatedJobInfoDlg,
    JobitemCreateByCharacterIdDlg,
    ReviewCreateIssueDlg,
  },
  name: "develop-completed-list",

  props: {
    characterId: {
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
      issueCharacterIntDlg: false,
      issueReviewDlg: false,
      dialogTitle: '',

      buttonShowFlag: false,

      jobItems: [],
      jobItem: {},
      reviewJobitem: {},

      PROJECT_TYPE: PROJECT_TYPE,
      DOC_TYPE: DOC_TYPE,
      JOB_ITEM_TYPE: JOB_ITEM_TYPE,

      sumJobNum: 0,
      completeJobNum: 0,
      completeFlag: false,

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
    // _this.loadReviewInfo();

    if(Tool.isNotEmpty(_this.characterId)){
      _this.getCompletedJobitemList(_this.characterId);
    }

    _this.$bus.$on('refresh-develop-completed-list', (data) => {
      if(data){
        _this.getCompletedJobitemList(_this.characterId);
      }
    });
  },

  beforeDestroy(){
    let _this = this;
    _this.$bus.$off('refresh-develop-completed-list');
  },

  methods: {
    dateFormat(row, column) {
      return shortDateFormat(row.planSubmissionTime);
    },

    //控制按钮不要在数据还没有读取出来就提前显示按钮, 以避免按钮闪动
    showButton: function () {
      let _this = this;
      _this.buttonShowFlag = true;
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
      _this.issueCharacterIntDlg = false;
      _this.issueReviewDlg = false;
    },
    handleClose(done) {
      done(); //el-dialog用于关闭对话框的内置函数.
    },

    issueCharacterInt() {
      let _this = this;
      _this.issueCharacterIntDlg = true;
      _this.dialogTitle = "特性集成";
    },

    issueReview() {
      let _this = this;
      _this.issueReviewDlg = true;
      _this.dialogTitle = "特性评审";
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

    getCompletedJobitemList(characterId) {
      let _this = this;
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/job-items/getCompletedJobitemList/' + characterId).then((response) => {
        let resp = response.data;
        if (resp.success) {
          _this.jobItems = resp.content.completeJobitemList;
          _this.sumJobNum = resp.content.sumJobNum;
          _this.completeJobNum = resp.content.completeJobNum;
          _this.completeFlag = resp.content.completeFlag;
          _this.showButton();

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

  },
}
</script>

<style lang="scss" scoped>
@import './../../../public/static/scss/caipd.scss';

.paper-height {
  height: calc(100vh - 250px);
}

</style>
