<!--
 * Author: 周朔鹏
 * Date: 2025-02-23
 * Email: 391902958@qq.com
 * Website: https://www.euipd.com
-->

<template>
  <div class="jobitem-content">
    <div id="editing-print">
      <el-row style="margin-top: 10px; margin-left: 55px; padding-right: 20px;">
        <el-col :span="23">
          <div style="display: flex; justify-content: space-between; line-height: 35px;">
            <div style="width:310px; display: flex; flex-direction: row; justify-content: space-between;">
              <div>任务号: {{ jobItem.jobSerial }}</div>
            </div>
            <div v-if="hasCharacter">特性定义: <a href="javascript:void(0);" @click="characterDetailDlg(characterInfo.id)">
              {{ characterInfo.characterName }} </a></div>
          </div>
        </el-col>
      </el-row>
      <div class="hr hr-8 dotted"></div>
      <el-row>
        <el-col :span="24" style="width: 100%; text-align: center; font-size: 28px;margin-top: 5px;">
          {{ jobItem.jobName }}
        </el-col>
      </el-row>
      <div>
        <table class="table-out" style="border-bottom: 1px solid #1a1a1a">
          <tr>
            <table class="table-inn">
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
                  <span>任务名称</span>
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
                  <span>工作执行人</span>
                </th>
                <th>
                  <span style="margin-left: 10px;">
                    {{ jobItem.executorName }}
                  </span>
                </th>
                <th style="width: 120px; text-align: center;">
                  <span>任务工时</span>
                </th>
                <th>
                  <span style="margin-left: 10px;">
                   {{ jobItem.manhour }}
                  </span>
                </th>
              </tr>
              <tr>
                <th style="width: 120px; text-align: center;">
                  <span>产品经理</span>
                </th>
                <th>
                  <span style="margin-left: 10px;">
                    {{ jobItem.productManagerName }}
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
                  <span>计划提交时间</span>
                </th>
                <th>
                  <span style="margin-left: 10px;">
                    {{ jobItem.planSubmissionTime | dateFormat }}
                  </span>
                </th>
                <th style="width: 120px; text-align: center;">
                  <span>提交倒计时</span>
                </th>
                <th>
                  <span style="margin-left: 10px;">
                   {{ remainTime }}
                  </span>
                </th>
              </tr>
            </table>
          </tr>
          <tr>
            <table class="table-inn">
              <tr style="border-top: 2px solid #1a1a1a;">
                <th style="width: 120px; text-align: center;">
                  <span>任务描述</span>
                </th>
                <th>
                  <span style="margin-left: 10px; font-size: 12px; font-style: italic; ">
                    (内容说明: 以下为任务下发人对工作要求的描述.)
                  </span>
                </th>
              </tr>
            </table>
          </tr>
          <tr>
            <table class="table-inn">
              <tr>
                <th>
                  <span style="margin-left: 10px;">
                    <span v-rich-html="jobItem.taskDescription"></span>
                  </span>
                </th>
              </tr>
            </table>
          </tr>
          <!--  任务附件-->
          <tr  v-if="jobItem.fileList && jobItem.fileList.length > 0">
            <table class="table-inn">
              <tr style="border-top: 2px solid #1a1a1a;">
                <th style="width: 120px; text-align: center;">
                  <span>任务附件</span>
                </th>
                <th>
                  <span style="margin-left: 10px; font-size: 12px; font-style: italic;">
                    (内容说明: 任务任务下发时, 提供的参考附件.)
                  </span>
                </th>
              </tr>
            </table>
          </tr>
          <tr  v-if="jobItem.fileList && jobItem.fileList.length > 0">
            <table class="table-inn">
              <tr>
                <th>
                  <div v-for="file in jobItem.fileList" :key="file.id"
                       style="display: flex; flex-direction: row; align-items: center;">
                    <div style="margin-top: 3px; margin-left: 10px;">
                      {{ file.name }}
                    </div>
                    <div v-if="file && file.authStatus">
                      <el-button type="text" style="margin-left: 15px; margin-right: 25px;" @click="downloadFile(file)">下载
                      </el-button>
                    </div>
                  </div>
                </th>
              </tr>
            </table>
          </tr>
        </table>
      </div>
      <!--    任务提交说明-->
      <div v-if="jobItem.propertyDto && jobItem.propertyDto.jobDescription">
        <div class="hr hr-8 dotted"></div>
        <div style="margin-left: 55px;">
          工作提交信息:
        </div>
        <table class="table-out">
          <tr>
            <table class="table-inn">
              <tr style="border-top: 1px solid #1a1a1a;">
                <th style="width: 19.51%; text-align: center;">
                  <span>工作描述</span>
                </th>
                <th style="width: 48.78%;">
                    <span style="margin-left: 10px; font-size: 12px; font-style: italic;">
                      (本项内容是员工对工作进行总结并对交付件相关信息进行详细描述)
                    </span>
                </th>
              </tr>
            </table>
          </tr>
          <tr>
            <table class="table-inn">
              <tr>
                <th>
                  <div style="margin-left: 10px;" v-rich-html="jobItem.propertyDto.jobDescription">
                  </div>
                </th>
              </tr>
            </table>
          </tr>
          <tr v-if="jobItem.propertyDto && jobItem.propertyDto.testKeyPoints">
            <table class="table-inn">
              <tr style="border-top: 2px solid #1a1a1a;">
                <th style="width: 19.51%; text-align: center;">
                  <span>测试/验收关键点说明</span>
                </th>
                <th style="width: 48.78%;">
                  <span style="margin-left: 10px; font-size: 12px; font-style: italic;">
                    (本项内容是员工对测试关键点进行详细描述, 以便测试工作开展.)
                  </span>
                </th>
              </tr>
            </table>
          </tr>
          <tr  v-if="jobItem.propertyDto && jobItem.propertyDto.testKeyPoints">
            <table class="table-inn">
              <tr>
                <th>
                  <div style="margin-left: 10px;" v-rich-html="jobItem.propertyDto.testKeyPoints">
                  </div>
                </th>
              </tr>
            </table>
          </tr>
          <tr  v-if="jobItem.propertyDto.fileItems">
            <table class="table-inn">
              <tr>
                <th>
                  <div v-for="file in jobItem.propertyDto.fileItems" :key="file.id"
                       style="display: flex; flex-direction: row; align-items: center;">
                    <div style="margin-top: 3px; margin-left: 10px;">
                      {{ file.name }}
                    </div>
                    <div v-if="file && file.authStatus">
                      <el-button type="text" style="margin-left: 15px; margin-right: 25px;" @click="downloadFile(file)">下载
                      </el-button>
                    </div>
                  </div>
                </th>
              </tr>
            </table>
          </tr>
          <tr v-if="jobItem.type === JOB_ITEM_TYPE.TASK_MILESTONE.key">
            <table class="table-inn">
              <tr style="border-top: 2px solid #1a1a1a;">
                <th style="width: 160px; text-align: center;">
                  <span>预算执行表</span>
                </th>
                <th>
                  <span style="margin-left: 10px;">
                    <small><em>(内容说明: 显示当前项目的预算执行统计表)</em></small>
                  </span>
                </th>
              </tr>
            </table>
          </tr>
          <tr v-if="jobItem.type === JOB_ITEM_TYPE.TASK_MILESTONE.key">
            <table class="table-inn">
              <tr>
                <th>
                  <el-button type="text" style="margin-left: 15px; margin-right: 25px;" @click="showBudgetStatisticsInfo">预算执行统计表 </el-button>
                </th>
              </tr>
            </table>
          </tr>
        </table>
      </div>

      <!--    评审结果信息-->
      <div v-if="isReviewNotifyFlag && reviewJobitem.propertyDto">
        <div class="hr hr-8 dotted"></div>
        <div style="margin-left: 55px;">
          评审结果信息:
        </div>
        <table class="table-out">
          <tr>
            <table class="table-inn">
              <tr style="border-top: 1px solid #1a1a1a;">
                <th style="width: 19.51%; text-align: center;">
                  <span>评审意见</span>
                </th>
                <th>
                  <span style="margin-left: 10px;">
                    评审会审批意见.
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
                  <div style="padding: 0 8px; min-height: 50px; word-wrap:break-word;" v-rich-html="reviewJobitem.propertyDto ? reviewJobitem.propertyDto.jobDescription : '无内容'"></div>
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

      <div class="hr hr-18 dotted hr-double"></div>
      <!--过程处理信息-->
      <!--过程处理信息-->
      <!--过程处理信息-->
      <!--过程处理信息-->
      <div v-if="!isOutline">
        <el-row>
          <div v-for="(item , index) in jobItemProcessList" :key="index">
            <div style="margin-left: 55px;">
              处理过程 : {{ JOB_ITEM_PROCESS | optionKV(item.processStatus) }}
            </div>
            <table class="table-out">
              <tr>
                <table class="table-inn">
                  <tr>
                    <th style="width: 120px; text-align: center;">
                      <span>任务来源</span>
                    </th>
                    <th>
                    <span style="margin-left: 10px;">
                      {{ item.executorName }}
                    </span>
                    </th>
                    <th style="width: 120px; text-align: center;">
                      <span>工作执行人</span>
                    </th>
                    <th>
                    <span style="margin-left: 10px;">
                      {{ item.nextNodeExecutorName }}
                    </span>
                    </th>
                  </tr>
                  <tr>
                    <th style="width: 14.63%; text-align: center;">
                      <span>提交时间</span>
                    </th>
                    <th style="width: 30.49%;">
                    <span style="margin-left: 10px;">
                      {{ item.actualSubmissionTimeStr }}
                    </span>
                    </th>
                    <th style="width: 14.63%; text-align: center;">
                      <span>创建时间</span>
                    </th>
                    <th>
                    <span style="margin-left: 10px;">
                     {{ item.createTime | dateFormat }}
                    </span>
                    </th>
                  </tr>
                </table>
              </tr>
              <tr>
                <table class="table-inn">
                  <tr style="border-top: 2px solid #1a1a1a;">
                    <th style="width: 120px; text-align: center;">
                      <span>工作描述</span>
                    </th>
                    <th>
                    <span style="margin-left: 10px; font-size: 12px; font-style: italic;">
                      (内容说明: 工作执行人应当对工作交付内容进行详细描述,并对附件内容进行概要描述.)
                    </span>
                    </th>
                  </tr>
                </table>
              </tr>
              <tr>
                <table class="table-inn">
                  <tr>
                    <th>
                    <span style="margin-left: 10px;">
                      {{ item.jobDescription }}
                    </span>
                    </th>
                  </tr>
                </table>
              </tr>
              <!-- Character-->
              <tr v-if=" item.characterPlainDtos !== null">
                <table class="table-inn">
                  <tr style="border-top: 2px solid #1a1a1a;">
                    <th style="width: 120px; text-align: center;">
                      <span>特性定义表</span>
                    </th>
                    <th>
                      <div style="margin-left: 10px; font-size: 12px; font-style: italic;">
                        (内容说明: 工作人员提交的特性定义表.)
                      </div>
                    </th>
                  </tr>
                </table>
              </tr>
              <tr v-if=" item.characterPlainDtos !== null">
                <table class="table-inn">
                  <tr>
                    <th>
                      <span v-for="character in item.characterPlainDtos" :key="character.id">
<!--                        <a href="javascript:void(0)" @click="characterDetailDlg(character.id)">-->
                          《{{ character.characterName }}》
<!--                        </a>-->
                      </span>
                    </th>
                  </tr>
                </table>
              </tr>
              <!-- 任务提交时 交付的附件-->
              <tr v-if="item.fileList !== null && item.fileList !== undefined && item.fileList.length >0">
                <table class="table-inn">
                  <tr style="border-top: 2px solid #1a1a1a;">
                    <th style="width: 120px; text-align: center;">
                      <span>任务附件</span>
                    </th>
                    <th>
                      <div style="margin-left: 10px; font-size: 12px; font-style: italic;">
                        (内容说明: 工作人员提交的特性定义表时交付的相关附件.)
                      </div>
                    </th>
                  </tr>
                </table>
              </tr>
              <tr v-if="item.fileList !== null && item.fileList !== undefined && item.fileList.length >0">
                <table class="table-inn">
                  <tr>
                    <th>
                      <div v-for="file in item.fileList" :key="file.id"
                           style="display: flex; flex-direction: row; align-items: center; ">
                        <div style="margin-top: 3px; margin-left: 10px;">
                          {{ file.name }}
                        </div>
                        <div v-if="file && file.authStatus">
                          <el-button type="text" style="margin-left: 15px; margin-right: 25px;"
                                     @click="downloadFile(file)">下载
                          </el-button>
                        </div>
                      </div>
                    </th>
                  </tr>
                </table>
              </tr>
            </table>
            <div class="hr hr-8 dotted"></div>
          </div>
        </el-row>
      </div>
    </div>

    <!--    显示特性定义表-->
    <el-dialog
        :visible.sync="detailDialogVisible"
        append-to-body
        width="29cm"
        :before-close="handleClose"
        :close-on-click-modal=false>
      <div slot="title" style="font-size: 20px;">{{ dialogTitle }}</div>
      <CharacterDetailInfoCoreAll2
          :id="('jobitem-detail-infor-core-' + (random))"
          :key="('jobitem-detail-infor-core-' + (random))"
          :character-id="characterInfo.id"
          pin-type="JOBITEM"
          :ref-id="jobitemId"
      ></CharacterDetailInfoCoreAll2>
    </el-dialog>

    <!--查看预算执行汇总表-->
    <el-dialog
        width="40cm"
        append-to-body
        :visible.sync="showStatisticsDlg"
        destroy-on-close
        :before-close="handleClose"
        :close-on-click-modal=false>
      <div slot="title" style="font-size: 20px;">{{ dialogTitle }}</div>
      <div>
        <BudgetExecutionStatisticsDlg
            :key="((jobitemId) + '-subproject')"
            :jobitem-id="jobitemId"
            :project-type="PROJECT_TYPE.SUB_PROJECT.key"
            :close-dlg = closeDialog
        ></BudgetExecutionStatisticsDlg>
      </div>
    </el-dialog>

  </div>
</template>

<script>
import File from "../rdms/file.vue";
import FileSaver from 'file-saver';
import Swal from "sweetalert2";
import printJS from "print-js";
import CharacterDetailInfoCore from "@/components/std/character-detail-info-core.vue";
import CharacterItemModify from "@/components/rdms/character-item-modify.vue";
import {formatSecond} from "@/filter/filter";
import CharacterDetailInfoCoreAll2 from "@/components/std/character-detail-info-core-all2.vue";
import BudgetExecutionStatisticsDlg from "@/components/std/budget-execution-statistics-dlg.vue";

export default {

  components: {
    BudgetExecutionStatisticsDlg,
    File,
    FileSaver,
    CharacterDetailInfoCore,
    CharacterItemModify,
    CharacterDetailInfoCoreAll2,
  },
  name: "jobitem-detail-info-core",

  props: {
    jobitemId: {
      default: null,
    },
    isOutline: {  // true 时 不显示 过程信息
      default: false,
    },
  },

  data() {
    return {
      loginUser: {},
      loginCustomer: {},

      jobItem: {
        default: null,
      },

      jobItemProcessList: [],
      jobitemProperty: {},
      reviewJobitem: {},
      isReviewNotifyFlag: false,

      fileItems: [],
      resFile: {},
      resFiles: [],
      resFileIds: [],
      detailDialogVisible: false,
      showStatisticsDlg: false,

      dialogTitle: 'Dialog',

      characterInfo: {},
      JOB_ITEM_PROCESS: JOB_ITEM_PROCESS,
      DOC_TYPE: DOC_TYPE,
      JOB_ITEM_STATUS: JOB_ITEM_STATUS,
      JOB_ITEM_TYPE: JOB_ITEM_TYPE,
      PROJECT_TYPE: PROJECT_TYPE,

      printData: {
        printable: 'editing-print',
        type: 'html',
        ignore: ['no-print'],
        targetStyles: ['*'],  //css样式，写成*代表打印样式完全继承你页面的样式
        // maxWidth:'800px'  //打印界面最大宽度，适当调整可以解决打印页面过宽，显示不完整的问题
      },
      random: 0,
    };
  },

  computed: {
    remainTime(){
      let _this = this;
      let planSubmissionTime = new Date(_this.jobItem.planSubmissionTime).getTime();
      let left = planSubmissionTime/1000 - Date.now()/1000;
      return  formatSecond(left);
    },

    hasCharacter(){
      let _this = this;
      if(Tool.isNotEmpty_ES5(_this.characterInfo)){
        return true;
      }else{
        return false;
      }

    }
  },

  mounted: function () {
    let _this = this;
    _this.getLoginUser();
    if(Tool.isNotEmpty(_this.jobitemId)){
      _this.getJobitemRecordDetailInfo(_this.jobitemId);

      if(Tool.isNotEmpty(_this.jobItem.type) && _this.jobItem.type === JOB_ITEM_TYPE.REVIEW_NOTIFY.key){
        _this.isReviewNotifyFlag = true;
        _this.getReviewNotifyInfo(_this.jobitemId);
      }
    }
  },

  methods: {
    getLoginUser(){
      let _this = this;
      if(Tool.getLoginUser().userGroup === LOGIN_USER_GROUP.CUSTOMER.key){
        _this.loginUser = Tool.getLoginUser().adminUser;
        _this.loginCustomer =Tool.getLoginUser();
      }else{
        _this.loginUser = Tool.getLoginUser();
        _this.loginCustomer = Tool.getLoginUser().customer;
      }
    },

    getJobitemRecordDetailInfo(jobitemId){
      let _this = this;
      let formData = new window.FormData();
      formData.append('jobitemId', jobitemId);
      formData.append('loginUserId', _this.loginUser.customerUserId);
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/job-items/getJobitemRecordDetailInfo', formData).then((response) => {
        let resp = response.data;
        if (resp.success) {
          _this.jobItem = resp.content;
          _this.getJobItemProcessInfos(jobitemId);
          if(_this.jobItem.characterId){
            _this.getCharacterSimpleInfo(_this.jobItem.characterId)
          }

        }else{
          Toast.warning(resp.message, Swal);
        }
      });
    },

    getJobItemProcessInfos(jobitemId){
      let _this = this;
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/job-item-process/get-jobitem-process-info-list/'+ jobitemId).then((response)=>{
        let resp = response.data;
        if (resp.success) {
          _this.jobItemProcessList = resp.content;
        } else {
          Toast.warning(resp.message, Swal);
        }
      })
    },

    getReviewNotifyInfo(jobitemId) {
      let _this = this;
      let formData = new window.FormData();
      formData.append('jobitemId', jobitemId);
      formData.append('loginUserId', _this.loginUser.customerUserId);
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/job-items/getReviewNotifyInfo', formData).then((response) => {
        let resp = response.data;
        if (resp.success) {
          _this.reviewJobitem = resp.content.reviewJobitemInfo.jobitem;
        } else {
          Toast.warning(resp.message, Swal);
        }
      });
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

    characterDetailDlg(characterId) {
      let _this = this;
      _this.getCharacterSimpleInfo(characterId);
      _this.detailDialogVisible = true;
      _this.dialogTitle = "特性定义表";
    },

    getCharacterSimpleInfo(characterId){
      let _this = this;
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/character/getCharacterSimpleInfo/'+ characterId).then((response)=>{
        let resp = response.data;
        if (resp.success) {
          _this.characterInfo = resp.content;
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

    showBudgetStatisticsInfo() {
      let _this = this;
      _this.showStatisticsDlg = true;
      _this.dialogTitle = "查看预算执行汇总表";
    },

    handleClose(done) {
      done(); //el-dialog用于关闭对话框的内置函数.
    },

    closeDialog() {
      let _this = this;
      _this.detailDialogVisible = false;
      _this.showStatisticsDlg = false;
    },

  },
}
</script>

<style lang="scss" scoped>
@import './../../../public/static/scss/caipd.scss';

.jobitem-content {
  background: white;
  margin: 0 auto;
  padding-bottom: 20px;
  width: 100%;
  display: block;
  box-sizing: border-box;
  .table-out{
    width: var(--table-out-width, 100%);
    max-width: 22cm;
    margin: 20px auto;
    border-top: 1px solid #1a1a1a;
    border-left: 1px solid #1a1a1a;
    border-right: 1px solid #1a1a1a;
    border-bottom: 2px solid #1a1a1a;
    .table-inn{
      width: 100%;
      table-layout: fixed;
      tr{
        line-height: 28px;
        width: 100%;
        th{
          border-top: 1px solid #1a1a1a;
          border-left: 1px solid #1a1a1a;
          border-right: 1px solid #1a1a1a;
          font-weight: 350;
          word-break: break-word;
        }
      }

    }
  }
}
</style>
