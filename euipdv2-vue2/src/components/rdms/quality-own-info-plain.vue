<!--
 * Author: 周朔鹏
 * Date: 2025-02-05
 * Email: 391902958@qq.com
 * Website: https://www.euipd.com
-->

<template>
  <div class="ca-plain-container" >
    <div class="ca-plain-form">
      <div class="ca-form-line" style="font-weight: bold">
        <div class="ca-form-label-f">
          <div class="ca-form-label-title">
            <i class="el-icon-star-on"></i>
            质量反馈标题:
          </div>
        </div>
        <div class="ca-form-content">
          {{ qualityItem.name }}
        </div>
      </div>
      <div>
        <div class="ca-form-line">
          <div class="ca-form-label-f">
            <div class="ca-form-label-title">
              质量问题描述:
            </div>
          </div>
        </div>
        <div class="ca-form-line-content"
             v-rich-html="qualityItem.qualityDescription">
        </div>
      </div>
      <div>
        <div class="ca-form-line">
          <div class="ca-form-label-xxl">
            <div class="ca-form-label-title">
              触发条件/问题现象:
            </div>
          </div>
        </div>
        <div class="ca-form-line-content"
             v-rich-html="qualityItem.triggerCondition">
        </div>
      </div>
      <div v-if="qualityItem.essentialAnalysis">
        <div class="ca-form-line">
          <div class="ca-form-label-xxl">
            <div class="ca-form-label-title">
              质量问题本质分析:
            </div>
          </div>
        </div>
        <div class="ca-form-line-content"
             v-rich-html="qualityItem.essentialAnalysis">
        </div>
      </div>
      <div v-if="qualityItem.solution">
        <div class="ca-form-line">
          <div class="ca-form-label-xxl">
            <div class="ca-form-label-title">
              解决方案/处理措施:
            </div>
          </div>
        </div>
        <div class="ca-form-line-content"
             v-rich-html="qualityItem.solution">
        </div>
      </div>
      <div v-if="qualityItem.outputIndicator">
        <div class="ca-form-line">
          <div class="ca-form-label-xxl">
            <div class="ca-form-label-title">
              输出逻辑/交付指标:
            </div>
          </div>
        </div>
        <div class="ca-form-line-content"
             v-rich-html="qualityItem.outputIndicator">
        </div>
      </div>
      <div v-if="qualityItem.testMethod">
        <div class="ca-form-line">
          <div class="ca-form-label-xxl">
            <div class="ca-form-label-title">
              验证方法/测试方法:
            </div>
          </div>
        </div>
        <div class="ca-form-line-content"
             v-rich-html="qualityItem.testMethod">
        </div>
      </div>
      <div v-if="qualityItem.fileList && qualityItem.fileList.length > 0">
        <div class="ca-form-line">
          <div class="ca-form-label">
            <div class="ca-form-label-title">
              附件列表:
            </div>
          </div>
        </div>
        <div v-for="(item, i ) in qualityItem.fileList" :key="i" style="display: flex; flex-direction: row; align-items: center; margin-left: 16px;">
          <div v-if="item">
            {{ item.name }}
          </div>
          <div v-if="item && item.authStatus">
            <el-button type="text" style="margin-left: 25px;" @click="downloadFile(item)">下载</el-button>
          </div>
        </div>
      </div>
      <div v-if="qualityItem.feedbackDescription">
        <div class="ca-form-line-l">
          <div class="ca-form-label-xxl">
            <div class="ca-form-label-title">
              处理结论与反馈:
            </div>
          </div>
        </div>
        <div class="ca-form-line-content"
             v-rich-html="qualityItem.feedbackDescription">
        </div>
      </div>
      <div v-if="qualityItem.feedbackFileList && qualityItem.feedbackFileList.length>0">
        <div class="ca-form-line-l">
          <div class="ca-form-label">
            <div class="ca-form-label-title">
              反馈附件列表:
            </div>
          </div>
        </div>
        <div v-for="(item, i ) in qualityItem.feedbackFileList" :key="i" style="display: flex; flex-direction: row; align-items: center; margin-left: 16px;">
          <div v-if="item">
            {{ item.name }}
          </div>
          <div v-if="item && item.authStatus">
            <el-button type="text" style="margin-left: 25px;" @click="downloadFile(item)">下载</el-button>
          </div>
        </div>
      </div>
      <div v-if="qualityItem.propertyDto && qualityItem.propertyDto.fileItems && qualityItem.propertyDto.fileItems.length>0">
        <div class="ca-form-line-l">
          <div class="ca-form-label">
            <div class="ca-form-label-title">
              质量分析附件:
            </div>
          </div>
        </div>
        <div v-for="(item, i ) in qualityItem.propertyDto.fileItems" :key="i" style="display: flex; flex-direction: row; align-items: center; margin-left: 16px;">
          <div v-if="item">
            {{ item.name }}
          </div>
          <div v-if="item && item.authStatus">
            <el-button type="text" style="margin-left: 25px;" @click="downloadFile(item)">下载</el-button>
          </div>
        </div>
      </div>

      <div class="hr hr-10 dotted"></div>
      <div class="ca-form-line">
        <div class="ca-form-label">
          <div class="ca-form-label-title">
            编制人:
          </div>
        </div>
        <div class="ca-form-content">
          {{ qualityItem.writerName }}
        </div>
      </div>
      <div class="ca-form-line">
        <div class="ca-form-label">
          <div class="ca-form-label-title">
            编制时间:
          </div>
        </div>
        <div class="ca-form-content">
          {{ qualityItem.createTime | dateFormat }}
        </div>
      </div>
    </div>

  </div>
</template>

<script>
import FileRdms from "@/components/rdms/file-rdms";
import Swal from "sweetalert2";
import FileSaver from 'file-saver';

export default {
  components: {
    FileRdms,
  },
  name: "quality-own-info-plain",

  props: {
    qualityId: {
      default: null,
    },
  },

  data: function () {
    return {
      loginUser: {},
      loginCustomer: {},

      qualityItem: {},

      resFile: {},
      resFiles: [],
      resFileIds: [],
    }
  },

  computed: {},

  mounted: function () {
    let _this = this;
    _this.getLoginUser();
    if(Tool.isNotEmpty(_this.qualityId)){
      _this.getQualityDetailInfo(_this.qualityId);
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

    getQualityDetailInfo(qualityId){
      let _this = this;
      let formData = new window.FormData();
      formData.append('qualityId', qualityId);
      formData.append('loginUserId', _this.loginUser.customerUserId);
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/quality/getQualityDetailInfo', formData).then((response)=>{
        let resp = response.data;
        if (resp.success) {
          _this.qualityItem = resp.content;
        } else {
          Toast.warning(resp.message,Swal);
        }
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

  }
}
</script>

<style lang="scss" scoped>
@import './../../../public/static/scss/caipd.scss';

.original-container{
  height: 100%;
  margin: 12px 2px 5px 15px;
  padding-right: 10px;
  //min-width: 360px;
  overflow-y: scroll;
}
</style>
