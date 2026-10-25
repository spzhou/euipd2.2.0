<!--
 * Author: 周朔鹏
 * Date: 2024-11-03
 * Email: 391902958@qq.com
 * Website: https://www.euipd.com
-->

<template>
  <div class="original-container" >
    <div>
      <div style="font-weight: bold; display: flex; flex-direction: row; line-height: 30px;">
        <div style="width: 115px; text-align: right;">
          前级任务:
        </div>
        <div style="line-height: 30px; margin-left: 10px;">
          {{ jobitemDetail.jobName }}
        </div>
      </div>
      <div style="font-weight: bold; display: flex; flex-direction: row; line-height: 30px;">
        <div style="width: 115px; text-align: right;">
          任务号:
        </div>
        <div style="line-height: 30px; margin-left: 10px;">
          {{ jobitemDetail.jobSerial }}
        </div>
      </div>
      <div style="display: flex; flex-direction: row; line-height: 30px; font-weight: bold;">
        <div style="width: 115px; text-align: right;">
          子项目名称:
        </div>
        <div style="line-height: 30px;margin-left: 10px;">
          {{ jobitemDetail.subprojectName }}
        </div>
      </div>
      <div style="display: flex; flex-direction: row; line-height: 30px; font-weight: bold;">
        <div style="width: 115px; text-align: right;">
          上级执行人:
        </div>
        <div style="line-height: 30px;margin-left: 10px;">
          {{ jobitemDetail.executorName }}
        </div>
      </div>
      <div style="display: flex; flex-direction: row; line-height: 30px; font-weight: bold;">
        <div style="width: 115px; text-align: right;">
          子项目经理:
        </div>
        <div style="line-height: 30px;margin-left: 10px;">
          {{ jobitemDetail.projectManagerName }}
        </div>
      </div>

      <div class="hr hr-6 dotted"></div>
      <div>
        <div style="text-align: left; line-height: 30px; font-weight: bold;">
          任务任务描述:
        </div>
        <div style="margin-left: 15px; line-height: 21px;"
             v-rich-html="jobitemDetail.taskDescription">
        </div>
      </div>

      <div class="hr hr-6 dotted"></div>
      <div v-if="jobitemDetail.fileList && jobitemDetail.fileList.length >0" style="line-height: 30px;">
        <div style="font-weight: bold;">
          任务附件列表:
        </div>
        <div v-for="(item, i ) in jobitemDetail.fileList" :key="item.id+'-'+i" style="display: flex; flex-direction: row; align-items: center; margin-left: 16px;">
          <div v-if="item">
            {{ item.name }}
          </div>
          <div v-if="item && item.authStatus">
            <el-button type="text" style="margin-left: 25px;" @click="downloadFile(item)">下载</el-button>
          </div>
        </div>
        <div class="hr hr-6 dotted"></div>
      </div>
      <div v-if="jobitemDetail.processFileList" style="line-height: 30px;">
        <div style="font-weight: bold;">
          过程附件列表:
        </div>
        <div v-for="(item, i ) in jobitemDetail.processFileList" :key="item.id+'-'+i" style="display: flex; flex-direction: row; align-items: center; margin-left: 16px;">
          <div v-if="item">
            {{ item.name }}
          </div>
          <div v-if="item && item.authStatus">
            <el-button type="text" style="margin-left: 25px;" @click="downloadFile(item)">下载</el-button>
          </div>
        </div>
        <div class="hr hr-6 dotted"></div>
      </div>
    </div>
    <div v-if="parentJobitemDetail.jobSerial">
      <div class="hr hr-6 dotted"></div>
      <div style="font-weight: bold; display: flex; flex-direction: row; line-height: 30px;">
        <div style="width: 115px; text-align: right;">
          再上级任务名称:
        </div>
        <div style="line-height: 30px; margin-left: 10px;">
          {{ parentJobitemDetail.jobName }}
        </div>
      </div>
      <div style="font-weight: bold; display: flex; flex-direction: row; line-height: 30px;">
        <div style="width: 115px; text-align: right;">
          任务号:
        </div>
        <div style="line-height: 30px; margin-left: 10px;">
          {{ parentJobitemDetail.jobSerial }}
        </div>
      </div>
      <div style="display: flex; flex-direction: row; line-height: 30px; font-weight: bold;">
        <div style="width: 115px; text-align: right;">
          上级执行人:
        </div>
        <div style="line-height: 30px;margin-left: 10px;">
          {{ parentJobitemDetail.executorName }}
        </div>
      </div>
      <div class="hr hr-6 dotted"></div>
      <div>
        <div style="text-align: left; line-height: 30px; font-weight: bold;">
          任务任务描述:
        </div>
        <div style="margin-left: 15px; line-height: 21px;"
             v-rich-html="parentJobitemDetail.taskDescription">
        </div>
      </div>
      <div class="hr hr-6 dotted"></div>
      <div v-if="parentJobitemDetail.fileList" style="line-height: 30px;">
        <div style="font-weight: bold;">
          任务附件列表:
        </div>
        <div v-for="(item, i ) in parentJobitemDetail.fileList" :key="item.id+'-'+i" style="display: flex; flex-direction: row; align-items: center; margin-left: 16px;">
          <div v-if="item">
            {{ item.name }}
          </div>
          <div v-if="item && item.authStatus">
            <el-button type="text" style="margin-left: 25px;" @click="downloadFile(item)">下载</el-button>
          </div>
        </div>
        <div class="hr hr-6 dotted"></div>
      </div>
      <div v-if="parentJobitemDetail.processFileList" style="line-height: 30px;">
        <div style="font-weight: bold;">
          过程附件列表:
        </div>
        <div v-for="(item, i ) in parentJobitemDetail.processFileList" :key="item.id+'-'+i" style="display: flex; flex-direction: row; align-items: center; margin-left: 16px;">
          <div v-if="item">
            {{ item.name }}
          </div>
          <div v-if="item && item.authStatus">
            <el-button type="text" style="margin-left: 25px;" @click="downloadFile(item)">下载</el-button>
          </div>
        </div>
        <div class="hr hr-6 dotted"></div>
      </div>
      <div>
        <div style="text-align: left; line-height: 30px; font-weight: bold;">
          工作成果描述:
        </div>
        <div style="margin-left: 15px; line-height: 21px;"
             v-rich-html="parentJobitemDetail.propertyDto.jobDescription">
        </div>
      </div>
      <div v-if="parentJobitemDetail.propertyDto.fileItems" style="line-height: 30px;">
        <div style="font-weight: bold;">
          提交附件列表:
        </div>
        <div v-for="(item, i ) in parentJobitemDetail.propertyDto.fileItems" :key="item.id+'-'+i" style="display: flex; flex-direction: row; align-items: center; margin-left: 16px;">
          <div v-if="item">
            {{ item.name }}
          </div>
          <div v-if="item && item.authStatus">
            <el-button type="text" style="margin-left: 25px;" @click="downloadFile(item)">下载</el-button>
          </div>
        </div>
        <div class="hr hr-6 dotted"></div>
      </div>
    </div>

  </div>
</template>

<script>
import FileRdms from "@/components/rdms/file-rdms";
import DemandDetailDialog from "@/components/rdms/demand-detail-dlg";
import Swal from "sweetalert2";
import FileSaver from 'file-saver';

export default {
  components: {
    FileRdms,
    DemandDetailDialog,
  },
  name: "jobitem-detail-parent-show-plain",

  props: {
    jobitemId: {
      default: null,
    },
  },

  data: function () {
    return {
      loginUser: {},
      loginCustomer: {},

      characterId: {
        default: null,
      },

      jobitemDetail: {
        propertyDto: {
          jobDescription: '',
          testKeyPoints: '',
        }
      },
      parentJobitemDetail: {
        propertyDto: {
          jobDescription: '',
          testKeyPoints: '',
        }
      },

    }
  },

  computed: {},

  mounted: function () {
    let _this = this;
    _this.getLoginUser();
    if(Tool.isNotEmpty(_this.jobitemId)){
      _this.getJobitemDetailInfo(_this.jobitemId);
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

    getJobitemDetailInfo(jobitemId) {
      let _this = this;
      let formData = new window.FormData();
      formData.append('jobitemId', jobitemId);
      formData.append('loginUserId', _this.loginUser.customerUserId);
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/job-items/getJobitemRecordDetailInfo', formData).then((response) => {
        let resp = response.data;
        if (resp.success) {
          _this.jobitemDetail = resp.content;

          if(_this.jobitemDetail.type === JOB_ITEM_TYPE.TASK.key && _this.jobitemDetail.reviewResult === REVIEW_RESULT.SUPPLEMENT.key){
            let formData1 = new window.FormData();
            formData1.append('jobitemId', _this.jobitemDetail.parentJobitemId);
            formData1.append('loginUserId', _this.loginUser.customerUserId);
            _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/job-items/getJobitemRecordDetailInfo', formData1).then((response) => {
              let resp = response.data;
              if (resp.success) {
                _this.parentJobitemDetail = resp.content;
              }else{
                Toast.warning(resp.message, Swal);
              }
            });
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

  }
}
</script>

<style lang="scss" scoped>
@import './../../../public/static/scss/caipd.scss';

.original-container{
  height: 100%;
  margin: 20px 2px 5px 15px;
  padding-right: 10px;
  //min-width: 360px;
  overflow-y: scroll;
}
</style>
