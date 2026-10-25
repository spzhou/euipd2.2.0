<!--
 * Author: 周朔鹏
 * Date: 2024-11-03
 * Email: 391902958@qq.com
 * Website: https://www.euipd.com
-->

<template>
  <div style="height: 100%;">
    <div class="paper-height">
      <div style="height: 100%; overflow: scroll;">
        <div class="a4-paper-big-simple">
          <div style="margin: 30px 0 0;">
            <el-row style="margin-top: 10px; margin-left: 55px; padding-right: 20px;">
              <el-col :span="23">
                <div style="display: flex; justify-content: space-between;">
                  <div>任务号: {{ jobItem.jobSerial }}</div>
                  <div>任务状态: <a href="javascript:void(0);" @click="showJobitemProcess">
                    {{ JOB_ITEM_PROCESS | optionKV(jobItem.processStatus) }} </a></div>
                </div>
              </el-col>
            </el-row>
            <div>
              <table class="table-out">
                <tr>
                  <table  class="table-head">
                    <tr style="border-bottom: 1px solid #1a1a1a;">
                      <th style="width: 100%; text-align: center; font-size: 16px; font-weight: bold;">
                        <span>评审结果通知</span>
                      </th>
                    </tr>
                  </table>
                </tr>
                <tr>
                  <table class="table-inn">
                    <tr style="border-top: 1px solid #1a1a1a; width: 100%;">
                      <th style="width: 120px; text-align: center;">
                        <span style="font-size: 15px; font-weight: bold;">评审结论</span>
                      </th>
                      <th>
                        <div style="margin-left: 20px; line-height: 30px; margin-top: 5px;">
                          <el-radio-group v-model="reviewResult">
                            <el-radio :disabled="reviewResult!==2" :label="2">通过</el-radio>
                            <el-radio :disabled="reviewResult!==4" :label="4">通过(有条件)</el-radio>  <!--评审任务通过状态, 产品经理发任务给项目经理, 完成具体的任务-->
                            <el-radio :disabled="reviewResult!==6" :label="6">增补</el-radio>  <!-- 评审任务, 保持现有状态, 产品经理发任务给项目经理, 增补完成后, 再批准-->
                            <el-radio :disabled="reviewResult!==8" :label="8">驳回</el-radio>  <!--功能状态回到系统集成阶段, 由项目经理重新发集成任务, 全部完成后, 重新提交审批-->
                          </el-radio-group>
                        </div>
                      </th>
                    </tr>
                  </table>
                </tr>
                <tr style="border-bottom: 1px solid #1a1a1a;">
                  <table class="table-inn">
                    <tr>
                      <th style="width: 120px; text-align: center;">
                        <span>评审标题</span>
                      </th>
                      <th>
                      <span style="margin-left: 10px;">
                        {{ jobItem.jobName }}
                      </span>
                      </th>
                      <th style="width: 120px; text-align: center;">
                        <span>子项目名称</span>
                      </th>
                      <th>
                      <span style="margin-left: 10px;">
                       {{ jobItem.subprojectName }}
                      </span>
                      </th>
                    </tr>
                    <tr>
                      <th style="width: 120px; text-align: center;">
                        <span>项目名称</span>
                      </th>
                      <th>
                      <span style="margin-left: 10px;">
                        {{ jobItem.projectName === null ? jobItem.preProjectName : jobItem.projectName }}
                      </span>
                      </th>
                      <th style="width: 120px; text-align: center;">
                        <span>团队名称</span>
                      </th>
                      <th>
                      <span style="margin-left: 10px;">
                        {{ jobItem.customerName }}
                      </span>
                      </th>
                    </tr>

                    <tr>
                      <th style="width: 120px; text-align: center;">
                        <span>工作执行人</span>
                      </th>
                      <th>
                  <span style="margin-left: 10px;">
                    {{ jobItem.executorName }}
                  </span>
                      </th>
                      <th style="width: 120px; text-align: center;">
                        <span>项目经理</span>
                      </th>
                      <th>
                      <span style="margin-left: 10px;">
                       {{ jobItem.projectManagerName }}
                      </span>
                      </th>
                    </tr>
                    <tr>
                      <th style="width: 120px; text-align: center;">
                        <span>评审组长</span>
                      </th>
                      <th>
                      <span style="margin-left: 10px;">
                        {{ reviewJobitem.reviewGroupLeaderName }}
                      </span>
                      </th>
                      <th style="width: 120px; text-align: center;">
                        <span>评审时间</span>
                      </th>
                      <th>
                      <span style="margin-left: 10px;">
                        {{ reviewJobitem.actualSubmissionTime | dateFormat }}
                      </span>
                      </th>
                    </tr>
                  </table>
                </tr>

                <div  v-for="(reviewInfo, i) in otherReviewJobitemList" :key="i">
                  <tr>
                    <table class="table-inn">
                      <tr style="border-top: 1px solid #1a1a1a; width: 100%;">
                        <th style="width: 19.51%; text-align: center;">
                          <span>委员会签意见</span>
                        </th>
                        <th>
                        <span style="margin-left: 10px;">
                          说明: 评审委员对特性开发的评审意见.
                        </span>
                        </th>
                        <th style="width: 12.2%; text-align: center;">
                          评审委员
                        </th>
                        <th style="width: 12.2%; text-align: center;">
                          {{ reviewInfo.executorName ? reviewInfo.executorName : '' }}
                        </th>
                        <th style="width: 12.2%; text-align: center;">
                          提交时间
                        </th>
                        <th style="width: 17.07%; text-align: center;">
                          <div  v-if="reviewInfo.propertyDto">
                            {{ reviewInfo.propertyDto.createTime | dateFormat }}
                          </div>
                        </th>
                      </tr>
                    </table>
                  </tr>
                  <tr>
                    <table class="table-inn">
                      <tr>
                        <th>
                          <div style="width: 880px; padding: 0 8px; min-height: 50px; word-wrap:break-word;" v-rich-html="reviewInfo.propertyDto ? reviewInfo.propertyDto.jobDescription : '无内容'"></div>
                        </th>
                      </tr>
                    </table>
                  </tr>
                  <tr v-if="reviewInfo.propertyDto && reviewInfo.propertyDto.fileItems">
                    <table class="table-inn">
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

                </div>
                <!--产品经理填写部分-->
                <tr>
                  <table class="table-inn">
                    <tr style="border-top: 1px solid #1a1a1a;">
                      <th style="width: 19.51%; text-align: center;">
                        <span>评审意见</span>
                      </th>
                      <th>
                      <span style="margin-left: 10px;">
                        填表说明: 请提供具体的明确的评审意见. 如需增补材料请在此处详细说明.
                      </span>
                      </th>
                      <th style="width: 12.2%; text-align: center;">
                        提交时间
                      </th>
                      <th style="width: 17.07%; text-align: center;">
                        <div v-if="reviewJobitem.propertyDto">
                          {{ reviewJobitem.propertyDto.createTime | dateFormat }}
                        </div>
                      </th>
                    </tr>
                  </table>
                </tr>
                <tr>
                  <table class="table-inn">
                    <tr>
                      <th>
                        <div style="width: 880px; padding: 0 8px; min-height: 50px; word-wrap:break-word;" v-rich-html="reviewJobitem.propertyDto ? reviewJobitem.propertyDto.jobDescription : '无内容'"></div>
                      </th>
                    </tr>
                  </table>
                </tr>
                <tr v-if="reviewJobitem.propertyDto && reviewJobitem.propertyDto.fileItems">
                  <table class="table-inn">
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
              <div class="clearfix form-actions" style="margin-top: 50px;">
                <div style="display: flex; justify-content: center;">
                  <el-tooltip content="确定后, 可以在任务列表中查看!" placement="top" effect="light" :open-delay=1000>
                    <button type="button" v-on:click="confirmReviewResult()" class="ca-btn-primary">确定</button>
                  </el-tooltip>
                </div>
              </div>
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
            :key="('detail-' + (jobitemId))"
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
    if (Tool.isNotEmpty(_this.jobitemId)) {
      _this.getReviewNotifyInfo(_this.jobitemId);
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
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/job-items/getReviewNotifyInfo', formData).then((response) => {
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
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/job-items/confirmReviewResult/' + _this.jobitemId).then((response) => {
        let resp = response.data;
        if (resp.success) {
          Toast.success("保存成功", Swal);
          _this.$bus.$emit('close-manhour-tab', _this.index);
          _this.$bus.$emit('notify-jobitem-count-flag', true);  //执行管理
          _this.$bus.$emit('notify-character-review-count-flag', true);  //评审管理
          _this.$bus.$emit('notify-develop-count-flag', true);  //研发管理
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
@import './../../../public/static/scss/caipd';

.paper-height {
  height: calc(100vh - 200px);
}

</style>
