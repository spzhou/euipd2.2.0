<!--
 * Author: 周朔鹏
 * Date: 2024-11-03
 * Email: 391902958@qq.com
 * Website: https://www.euipd.com
-->

<template>
  <div style="height: 100%;">
    <div>
      <el-row style="margin-top: 10px;">
        <el-col :span="23">
          <div style="margin-left: 55px;">
            {{ getPreFix !== null ? getPreFix : '' }}任务号: {{ jobItem.jobSerial }}
          </div>
        </el-col>
      </el-row>
      <div>
        <table class="table-out">
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
                  <span style="margin-left: 10px;">
                    <small><em>(内容说明: 以下为任务下发人对工作要求的描述)</em></small>
                  </span>
                </th>
              </tr>
            </table>
          </tr>
          <tr>
            <table class="table-inn">
              <tr>
                <th>
                  <span style="margin-left: 10px; white-space: pre-line;">{{ jobItem.taskDescription }}</span>
                </th>
              </tr>
            </table>
          </tr>
          <!--  任务附件-->
          <tr v-if="jobItem.fileList !==null && jobItem.fileList !== undefined && jobItem.fileList.length > 0">
            <table class="table-inn">
              <tr style="border-top: 2px solid #1a1a1a;">
                <th style="width: 120px; text-align: center;">
                  <span>任务附件</span>
                </th>
                <th>
                  <span style="margin-left: 10px;">
                    <small><em>(内容说明: 任务任务下发时, 提供的参考附件)</em></small>
                  </span>
                </th>
              </tr>
            </table>
          </tr>
          <tr v-if="jobItem.fileList !==null && jobItem.fileList !== undefined && jobItem.fileList.length > 0">
            <table class="table-inn">
              <tr>
                <th>
                  <div v-for="file in jobItem.fileList" :key="file.id"
                       style="display: flex; flex-direction: row; align-items: center;">
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
      </div>

      <div v-if="jobitemProperty">
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
                    <span style="margin-left: 10px;">
                      <small><em>(本项内容是员工对工作进行总结并对交付件相关信息进行详细描述)</em></small>
                    </span>
                </th>
                <th style="width: 31.71%;">
                  <span style="margin-left: 10px;">
                    字数 <small><em>(不计空白与格式)</em></small>: {{ getJobDescriptionFontNum }} / 5000
                  </span>
                </th>
              </tr>
            </table>
          </tr>
          <tr>
            <table class="table-inn">
              <tr>
                <th>
                  <div style="margin-left: 10px;" v-rich-html="jobitemProperty.jobDescription">
                  </div>
                </th>
              </tr>
            </table>
          </tr>
          <tr>
            <table class="table-inn">
              <tr style="border-top: 2px solid #1a1a1a;">
                <th style="width: 19.51%; text-align: center;">
                  <span>测试/验收关键点说明</span>
                </th>
                <th style="width: 48.78%;">
                    <span style="margin-left: 10px;">
                      <small><em>(本项内容是员工对测试关键点进行详细描述, 以便测试工作开展.)</em></small>
                    </span>
                </th>
                <th style="width: 31.71%;">
                  <span style="margin-left: 10px;">
                    字数 <small><em>(不计空白与格式)</em></small>: {{ getTestKeyPointFontNum }} / 5000
                  </span>
                </th>
              </tr>
            </table>
          </tr>
          <tr>
            <table class="table-inn">
              <tr>
                <th>
                  <div style="margin-left: 10px;" v-rich-html="jobitemProperty.testKeyPoints">
                  </div>
                </th>
              </tr>
            </table>
          </tr>
          <!--  交付附件-->
          <tr v-if="resFiles !==null && resFiles !== undefined && resFiles.length > 0">
            <table class="table-inn">
              <tr style="border-top: 2px solid #1a1a1a;">
                <th style="width: 160px; text-align: center;">
                  <span>交付附件</span>
                </th>
                <th>
                  <span style="margin-left: 10px;">
                    <small><em>(内容说明: 任务交付时, 提交的工作交付成果)</em></small>
                  </span>
                </th>
              </tr>
            </table>
          </tr>
          <tr v-if="resFiles !==null && resFiles !== undefined && resFiles.length > 0">
            <table class="table-inn">
              <tr>
                <th>
                  <div v-for="file in resFiles" :key="file.id"
                       style="display: flex; flex-direction: row; align-items: center;">
                    <div style="margin-left: 10px;">
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
        <div class="hr hr-18 dotted hr-double"></div>
      </div>
    </div>
  </div>
</template>

<script>
import FileRdms from "@/components/rdms/file-rdms.vue";
import FileSaver from 'file-saver';
import Swal from "sweetalert2";
import {formatSecond, optionKV} from "@/filter/filter";

export default {
  components: {
    FileRdms,
    FileSaver,

  },
  name: "jobitem-own-info-core",

  props: {
    jobitemId: {
      default: null,
    },
    preFix: {
      default: '',
    }
  },

  data() {
    return {
      loginUser: {},
      loginCustomer: {},

      jobItem: {},

      jobitemProperty: {},

      fileItems: [],
      characterItem: {},
      dialogVisible: false,
      testJobItemDlg: false,

      dialogTitle: '',
      resFile: {},
      resFiles: [],
      resFileIds: [],

    };
  },

  computed: {
    getJobDescriptionFontNum() {
      if (Tool.isNotEmpty(this.jobitemProperty.jobDescription)) {
        return Tool.richTextLength(this.jobitemProperty.jobDescription);
      }
      return 0;
    },
    getTestKeyPointFontNum() {
      if (Tool.isNotEmpty( this.jobitemProperty.testKeyPoints)) {
        return Tool.richTextLength(this.jobitemProperty.testKeyPoints);
      }
      return 0;
    },
    getPreFix() {
      if(Tool.isNotEmpty(this.preFix)){
        return this.preFix;
      }
      return null;
    },
    remainTime(){
      let _this = this;
      let planSubmissionTime = new Date(_this.jobItem.planSubmissionTime).getTime();
      let left = planSubmissionTime/1000 - Date.now()/1000;
      return  formatSecond(left);
    },
  },

  mounted: function () {
    let _this = this;
    _this.getLoginUser();
    if(Tool.isNotEmpty(_this.jobitemId)){
      _this.getJobitemRecordDetailInfo(_this.jobitemId);
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

    getJobitemRecordDetailInfo(jobitemId){
      let _this = this;
      let formData = new window.FormData();
      formData.append('jobitemId', jobitemId);
      formData.append('loginUserId', _this.loginUser.customerUserId);
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/job-items/getJobitemRecordDetailInfo', formData).then((response) => {
        let resp = response.data;
        if (resp.success) {
          _this.jobItem = resp.content;
          if(_this.jobItem.propertyDto){
            _this.jobitemProperty = _this.jobItem.propertyDto;
            _this.fileItems = _this.jobitemProperty.fileItems;
            if(_this.fileItems !== null){
              for(let i= 0 ; i < _this.fileItems.length; i++){
                _this.resFiles.push(_this.fileItems[i]);
                _this.resFileIds.push(_this.fileItems[i].id);
              }
            }
          }
        }else{
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

  },
}
</script>

<style lang="scss" scoped>
@import './../../../public/static/scss/caipd.scss';

.table-out {
  width: var(--table-out-width, 100%);
  max-width: 22cm;
}
.table-inn {
  width: 100%;
  table-layout: fixed;
}

</style>
