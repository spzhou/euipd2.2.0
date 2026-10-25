<!--
 * Author: 周朔鹏
 * Date: 2024-11-03
 * Email: 391902958@qq.com
 * Website: https://www.euipd.com
-->

<template>
  <div style="height: 100%;">
    <div class="paper-height">
      <div style="height: 100%; overflow: scroll; ">
        <div class="narrow-sheet">
          <el-row style="margin-top: 10px; margin-left: 20px;">
            <el-col :span="23">
              <div style="display: flex; justify-content: space-between;">
                <div>任务号: {{ reviewJobitem.jobSerial }}</div>
              </div>
            </el-col>
          </el-row>
          <div>
            <table class="table-out-narrow">
              <tr>
                <table  class="table-head-narrow">
                  <tr style="border-bottom: 1px solid #1a1a1a;">
                    <th style="width: 100%; text-align: center; font-size: 16px; font-weight: bold;">
                      <span>评审结果通知</span>
                    </th>
                  </tr>
                </table>
              </tr>
              <tr>
                <table class="table-inn-narrow">
                  <tr style="border-top: 1px solid #1a1a1a; width: 100%;">
                    <th style="width: 100px; text-align: center;">
                      <span style="font-size: 15px; font-weight: bold;">评审结论</span>
                    </th>
                    <th>
                      <div style="margin-left: 10px; line-height: 25px; font-weight: bold;">
                        <div v-if="reviewResult === 2">通过</div>
                        <div v-else-if="reviewResult === 4">通过(有条件)</div>
                        <div v-else-if="reviewResult === 6">增补</div>
                        <div v-else-if="reviewResult === 8">驳回</div>
                      </div>
                    </th>
                  </tr>
                </table>
              </tr>
              <tr style="border-bottom: 1px solid #1a1a1a;">
                <table class="table-inn-narrow">
                  <tr>
                    <th style="width: 100px; text-align: center;">
                      <span>评审标题</span>
                    </th>
                    <th>
                      <span style="margin-left: 10px;">
                        {{ jobItem.jobName }}
                      </span>
                    </th>
                  </tr>
                  <tr>
                    <th style="width: 100px; text-align: center;">
                      <span>子项目名称</span>
                    </th>
                    <th>
                      <span style="margin-left: 10px;">
                        {{ jobItem.subprojectName }}
                      </span>
                    </th>
                  </tr>

                  <tr>
                    <th style="width: 100px; text-align: center;">
                      <span>工作执行人</span>
                    </th>
                    <th>
                      <span style="margin-left: 10px;">
                        {{ jobItem.executorName }}
                      </span>
                    </th>
                  </tr>
                  <tr>
                    <th style="width: 100px; text-align: center;">
                      <span>评审组长</span>
                    </th>
                    <th>
                      <span style="margin-left: 10px;">
                        {{ reviewJobitem.reviewGroupLeaderName }}
                      </span>
                    </th>
                  </tr>
                </table>
              </tr>
              <tr>
                <div  v-for="(reviewInfo, i) in otherReviewJobitemList" :key="i" >
                  <table style="width: 100%"> <!--div内table设置width: 100%可以撑开表格-->
                    <tr>
                      <table class="table-inn-narrow">
                        <tr style="border-top: 1px solid #1a1a1a; width: 100%;">
                          <th style="width: 120px; text-align: center;">
                            <span>委员会签意见</span>
                          </th>
                          <th style=" text-align: center;">
                            评审委员
                          </th>
                          <th style=" text-align: center;">
                            {{ reviewInfo.executorName ? reviewInfo.executorName : '' }}
                          </th>
                          <th style=" text-align: center;">
                            提交时间
                          </th>
                          <th style=" text-align: center;">
                            <div  v-if="reviewInfo.propertyDto">
                              {{ reviewInfo.propertyDto.createTime | dateFormat }}
                            </div>
                          </th>
                        </tr>
                      </table>
                    </tr>
                    <tr>
                      <table class="table-inn-narrow">
                        <tr>
                          <th>
                            <div style=" padding: 0 8px; min-height: 50px; word-wrap:break-word;" v-rich-html="reviewInfo.propertyDto ? reviewInfo.propertyDto.jobDescription : '无内容'"></div>
                          </th>
                        </tr>
                      </table>
                    </tr>
                    <tr v-if="reviewInfo.propertyDto && reviewInfo.propertyDto.fileItems">
                      <table class="table-inn-narrow">
                        <tr>
                          <th>
                            <div v-for="(item, i) in reviewInfo.propertyDto.fileItems" :key="i" style="display: flex; flex-direction: row; align-items: center; margin-left: 15px;">
                              <div v-if="item">
                                {{ item.name }}
                              </div>
                              <div v-if="item && item.authStatus">
                                <el-button type="text" style="margin-left: 20px;"
                                           @click="downloadFile(item)">下载
                                </el-button>
                              </div>
                            </div>
                          </th>
                        </tr>
                      </table>
                    </tr>
                  </table>
                </div>
              </tr>
              <!--产品经理填写部分-->
              <tr>
                <table class="table-inn-narrow">
                  <tr style="border-top: 1px solid #1a1a1a;">
                    <th style="width: 120px; text-align: center;">
                      <span>评审意见</span>
                    </th>
                    <th style=" text-align: center;">
                      提交时间
                    </th>
                    <th style=" text-align: center;">
                      <div v-if="reviewJobitem.propertyDto">
                        {{ reviewJobitem.propertyDto.createTime | dateFormat }}
                      </div>
                    </th>
                  </tr>
                </table>
              </tr>
              <tr>
                <table class="table-inn-narrow">
                  <tr>
                    <th>
                      <div style=" padding: 0 8px; min-height: 50px; word-wrap:break-word;" v-rich-html="reviewJobitem.propertyDto ? reviewJobitem.propertyDto.jobDescription : '无内容'"></div>
                    </th>
                  </tr>
                </table>
              </tr>
              <tr v-if="reviewJobitem.propertyDto && reviewJobitem.propertyDto.fileItems">
                <table class="table-inn-narrow">
                  <tr>
                    <th>
                      <div v-for="(item, i) in reviewJobitem.propertyDto.fileItems" :key="i" style="display: flex; flex-direction: row; align-items: center; margin-left: 15px;">
                        <div v-if="item">
                          {{ item.name }}
                        </div>
                        <div v-if="item && item.authStatus">
                          <el-button type="text" style="margin-left: 20px;"
                                     @click="downloadFile(item)">下载
                          </el-button>
                        </div>
                      </div>
                    </th>
                  </tr>
                </table>
              </tr>
            </table>
          </div>
          <el-row>
            <div style="display: flex; justify-content: space-evenly; align-items: center;">
              <div>
                <span>
                  填表人 :
                </span>
                <span>
                  {{ reviewJobitem.productManagerName }}
                </span>
              </div>
              <div>
                <span>
                  填表日期 :
                </span>
                <span>
                  {{ reviewJobitem.createTime | dateFormat }}
                </span>
              </div>
            </div>
          </el-row>
          <div v-if="reviewResult === 2 || reviewResult === 8">
            <div class="hr hr-18 dotted hr-double"></div>
            <div style="display: flex; justify-content: center;">
              <el-tooltip content="确定后, 可以在任务列表中查看!" placement="top" effect="light" :open-delay=1000>
                <button type="button" v-on:click="confirmReviewResult()" class="ca-btn-primary">确定</button>
              </el-tooltip>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!--显示任务过程信息-->
    <el-dialog
        width="29cm"
        :visible.sync="showJobitemProcessDlg"
        :before-close="handleClose"
        :close-on-click-modal=false>
      <div slot="title" style="font-size: 20px;">{{ dialogTitle }}</div>
      <div>
        <JobitemDetailInfoDlg
            :key="('detail-' + (jobItemId))"
            :jobitem-id="jobItem.id"
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
import CharacterBudgetConfirmDlg from "@/components/rdms/character-budget-confirm-dlg";
import JobitemDetailInfoDlg from "@/components/std/jobitem-detail-info-dlg.vue";


export default {
  components: {
    FileRdms,
    FileSaver,
    CharacterBudgetConfirmDlg,
    JobitemDetailInfoDlg,

  },
  name: "jobitem-review-notify-sheet-page",

  props: {
    jobItemId: {
      default: null,
    },
    index: {
      default: null,
    },
    projectType: {
      default: PROJECT_TYPE.PRE_PROJECT.key,
    }
  },

  computed: {
  },

  data() {
    return {
      loginUser: {},
      loginCustomer: {},

      PROJECT_TYPE: PROJECT_TYPE,
      JOB_ITEM_PROCESS: JOB_ITEM_PROCESS,
      REVIEW_RESULT: REVIEW_RESULT,

      showJobitemProcessDlg: false,
      dialogTitle: '',

      jobItem: {},
      jobitemProperty: {},
      reviewJobitem: {},
      otherReviewJobitemList: [],
      reviewResult: 0,

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

  mounted: function () {
    let _this = this;
    _this.getLoginUser();
    if (Tool.isNotEmpty(_this.jobItemId)) {
      _this.getReviewNotifyInfo(_this.jobItemId);
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

    getReviewNotifyInfo(jobitemId) {
      let _this = this;
      let formData = new window.FormData();
      formData.append('jobitemId', jobitemId);
      formData.append('loginUserId', _this.loginUser.customerUserId);
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/job-items/getReviewNotifyInfo', formDatad).then((response) => {
        let resp = response.data;
        if (resp.success) {
          _this.jobItem = resp.content.jobitem;
          _this.reviewJobitem = resp.content.reviewJobitemInfo.jobitem;
          _this.reviewResult = resp.content.reviewResult;
          _this.otherReviewJobitemList = resp.content.reviewJobitemInfo.otherReviewJobitemList;
        } else {
          Toast.warning(resp.message, Swal);
        }
      });
    },

    confirmReviewResult() {
      let _this = this;
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/job-items/confirmReviewResult/' + _this.jobItemId).then((response) => {
        let resp = response.data;
        if (resp.success) {
          Toast.success("保存成功", Swal);
          _this.$bus.$emit('close-manhour-tab', _this.index);
        } else {
          Toast.warning(resp.message, Swal);
        }
      })
    },

    showJobitemProcess() {
      let _this = this;
      _this.showJobitemProcessDlg = true;
      _this.dialogTitle = "任务处理过程";
    },
    handleClose(done) {
      let _this = this;
      _this.showJobitemProcessDlg = false;
      done();
    },

    closeDialog() {
      let _this = this;
      _this.showJobitemProcessDlg = false;
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

.narrow-sheet {
  //margin-top: 20px;
  background: $tableBackground;
  width: 100%;
  //height: 29.7cm;
  min-height: 28cm;
  display: block;
  margin: 20px auto;
  padding: 10px 15px 10px 10px;
  margin-bottom: 0.5cm;
  box-shadow: 0 0 0.5cm rgba(0, 0, 0, 0.5);
  overflow-y: scroll;
  box-sizing: border-box;

  .table-out-narrow {
    width: 100%;
    margin: 10px auto 26px;
    border-top: 1px solid #1a1a1a;
    border-left: 1px solid #1a1a1a;
    border-right: 1px solid #1a1a1a;
    border-bottom: 2px solid #1a1a1a;

    .table-head-narrow {
      width: 100%;
      tr {
        line-height: 35px;
        width: 100%;

        th {
          border-top: 1px solid #1a1a1a;
          border-left: 1px solid #1a1a1a;
          border-right: 1px solid #1a1a1a;
          font-weight: 350;
        }
      }
    }
    .table-inn-narrow {
      width: 100%;
      tr {
        line-height: 28px;
        width: 100%;

        th {
          border-top: 1px solid #1a1a1a;
          border-left: 1px solid #1a1a1a;
          border-right: 1px solid #1a1a1a;
          font-weight: 350;
        }
      }
    }

  }
}
</style>
