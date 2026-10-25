<!--
 * Author: 周朔鹏
 * Date: 2024-11-06
 * Email: 391902958@qq.com
 * Website: https://www.euipd.com
-->

<template>
  <div class="dialog-container">
    <div style="padding: 10px 108px 60px 0">
      <el-form label-width="180px">
        <el-form-item label="质量问题名称:">
          <el-col :span="11">
            {{ quality.name }}
          </el-col>
        </el-form-item>
        <el-form-item label="问题描述:">
          <el-col :span="24">
            <div v-rich-html="quality.qualityDescription"></div>
          </el-col>
        </el-form-item>
        <el-form-item v-if="quality.triggerCondition" label="触发条件或过程:">
          <el-col :span="24">
            <div v-rich-html="quality.triggerCondition"></div>
          </el-col>
        </el-form-item>
        <el-form-item label="关联项目:">
          {{ quality.associateProjectNameStr }}
        </el-form-item>
        <el-form-item label="场景或需求方名称:">
          <el-col :span="6">
            {{ quality.consumer }}
          </el-col>
        </el-form-item>
        <el-form-item label="质量反馈人:">
          <el-col :span="6">
            {{quality.writerName }}
          </el-col>
        </el-form-item>
        <el-form-item label="联系电话:">
          <el-col :span="6">
            {{ quality.confirmTel }}
          </el-col>
        </el-form-item>
      </el-form>
      <div v-if="quality.analysisName">
        <div class="hr hr-10 hr-dotted"></div>
        <el-form label-width="180px">
          <el-form-item v-if="quality.analysisName" label="质量分析标题:">
            <el-col :span="11">
              {{ quality.analysisName }}
            </el-col>
          </el-form-item>
          <el-form-item v-if="quality.executorName" label="质量分析人:">
            <el-col :span="6">
              {{quality.executorName }}
            </el-col>
          </el-form-item>
          <el-form-item v-if="quality.essentialAnalysis" label="质量问题本质分析:">
            <el-col :span="24">
              <div v-rich-html="quality.essentialAnalysis"></div>
            </el-col>
          </el-form-item>
          <el-form-item v-if="quality.solution" label="解决方案/处理措施:">
            <el-col :span="24">
              <div v-rich-html="quality.solution"></div>
            </el-col>
          </el-form-item>
          <el-form-item v-if="quality.outputIndicator" label="输出逻辑/交付指标:">
            <el-col :span="24">
              <div v-rich-html="quality.outputIndicator"></div>
            </el-col>
          </el-form-item>
          <el-form-item v-if="quality.testMethod" label="验证方法/测试方法:">
            <el-col :span="24">
              <div v-rich-html="quality.testMethod"></div>
            </el-col>
          </el-form-item>
        </el-form>
      </div>
      <div class="hr hr-10 hr-dotted"></div>
      <el-form  v-if="resFiles && resFiles.length>0" label-width="180px">
        <el-form-item label="附件列表:">
          <el-col :span="24">
            <div style="display: flex; flex-direction: column;">
              <div v-for="(item, i) in resFiles" :key="i" style="display: flex; align-items: center; flex-direction: row;">
                <div v-if="item">
                  {{ item.name }}
                </div>
                <div style="margin-left: 15px; width: 90px;">
                  <div v-if="item && item.authStatus">
                    <el-button type="text" @click="downloadFile(item)">下载</el-button>
                  </div>
                </div>
              </div>
            </div>
          </el-col>
        </el-form-item>
      </el-form>
      <div v-if="quality.feedbackDescription">
        <div class="hr hr-10 hr-dotted"></div>
        <el-form label-width="180px">
          <el-form-item v-if="quality.feedbackDescription" label="处理结论与意见反馈:">
            <el-col :span="24">
              <div v-rich-html="quality.feedbackDescription"></div>
            </el-col>
          </el-form-item>
        </el-form>
        <div class="hr hr-10 hr-dotted"></div>
        <el-form  v-if="quality.feedbackFileList && quality.feedbackFileList.length>0" label-width="180px">
          <el-form-item label="处理反馈附件列表:">
            <el-col :span="24">
              <div style="display: flex; flex-direction: column;">
                <div v-for="(item, i) in quality.feedbackFileList" :key="i" style="display: flex; align-items: center; flex-direction: row;">
                  <div v-if="item">
                    {{ item.name }}
                  </div>
                  <div style="margin-left: 15px; width: 90px;">
                    <div v-if="item && item.authStatus">
                      <el-button type="text" @click="downloadFile(item)">下载</el-button>
                    </div>
                  </div>
                </div>
              </div>
            </el-col>
          </el-form-item>
        </el-form>
      </div>
    </div>
  </div>
</template>
<script>
import Swal from "sweetalert2";
import FileSaver from 'file-saver';
import {shortDateFormat} from "@/filter/filter";

export default {
  name: 'quality-detail-plain',

  components: {
    FileSaver,
  },

  props:{
    qualityId: {
      default: null,
    },
  },

  data() {
    return {
      loginUser: {},
      loginCustomer: {},

      resFiles: [],
      quality: {},

    };
  },

  computed: {
    getQualityDescFontNum(){
      if(this.quality.qualityDescription){
        return  Tool.richTextLength(this.quality.qualityDescription);
      }
      return 0;
    },

    getQualityConditionFontNum(){
      if(this.quality.triggerCondition){
        return  Tool.richTextLength(this.quality.triggerCondition);
      }
      return 0;
    }
  },

  mounted: function() {
    let _this =this;
    _this.getLoginUser();
    if(_this.qualityId){
      _this.getQualityDetailInfo(_this.qualityId)
    }
  },
  methods:{
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

    shortDateFormat,
    getQualityDetailInfo(qualityId){
      let _this = this;
      let formData = new window.FormData();
      formData.append('qualityId', qualityId);
      formData.append('loginUserId', _this.loginUser.customerUserId);
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/quality/getQualityDetailInfo', formData).then((response)=>{
        let resp = response.data;
        if (resp.success) {
          _this.quality = resp.content;
          _this.resFiles = _this.quality.fileList;
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
};
</script>

<style lang="scss" scoped>
@import './../../../public/static/scss/caipd.scss';

</style>

