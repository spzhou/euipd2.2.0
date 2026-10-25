<!--
 * Author: 周朔鹏
 * Date: 2024-11-04
 * Email: 391902958@qq.com
 * Website: https://www.euipd.com
-->

<template>
  <div class="dialog-container">
    <el-row>
      <div style="margin: 0 40px 20px;">
        <el-form :label-position="labelPosition" label-width="150px" :model="demand">
          <el-form-item label="需求名称" >
            <p style="font-weight: bold;"> {{demand.demandName}}</p>
          </el-form-item>
          <el-form-item label="需求描述">
            <div v-rich-html="demand.demandDescription"></div>
          </el-form-item>
          <el-form-item label="生效条件或使用前提">
            <div v-rich-html="demand.workCondition"></div>
          </el-form-item>
          <el-form-item label="关联项目">
            <div>{{ preProject.preProjectName }}</div>
          </el-form-item>
          <el-form-item label="需求方名称">
            <div>{{ demand.demandCustomerName }}</div>
          </el-form-item>
          <el-form-item label="需求确认人姓名">
            <div>{{ demand.confirmPersonName }}</div>
          </el-form-item>
          <el-form-item label="需求确认人电话">
            <div>{{ demand.confirmContactTel }}</div>
          </el-form-item>
          <div class="hr hr-8 hr-dotted"></div>
          <el-row>
            <div v-for="(item, i) in fileItems" :key="i" style="display: flex; flex-direction: row; justify-content: right; align-items: center; margin-right: 15px;">
              <div v-if="item">
                {{ item.name }}
              </div>
              <div v-if="item && item.authStatus">
                <el-button type="text" style="margin-left: 20px;"
                           @click="downloadFile(item)">下载
                </el-button>
              </div>
            </div>
          </el-row>
        </el-form>
      </div>
    </el-row>
    <div style="width: 100%; height: 50px;"></div>
  </div>
</template>

<script>
import Swal from "sweetalert2";
import File from "../../components/rdms/file";
import FileSaver from 'file-saver';

export default {
  components: {
    File,
    FileSaver,
  },
  name: "demand-detail-dlg",

  props: {
    demandItem: {
      default: null,
    },
  },

  data: function () {
    return {
      loginUser: {},
      loginCustomer: {},

      labelPosition: 'right',

      demand: {},
      preProject: {},
      fileItems: [],
      imgList: [],
    }
  },

  mounted: function () {
    let _this = this;
    _this.getLoginUser();
    _this.demand = _this.demandItem;
    _this.getPreProjectSimpleInfo();
    _this.listAttachment(_this.demand.id);
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


    getPreProjectSimpleInfo() {
      let _this = this;
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/pre-project/getPreProjectSimpleInfo/'+ _this.demand.preProjectId).then((response)=>{
        let resp = response.data;
        if (resp.success) {
          _this.preProject = resp.content;
        } else {
          Toast.warning(resp.message,Swal);
        }
      })
    },

    listAttachment(demandId) {
      let _this = this;
      let formData = new window.FormData();
      formData.append('demandId', demandId);
      formData.append('loginUserId', _this.loginUser.customerUserId);
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/file/rdms/getFileListByDemandId', formData).then((response)=>{
        let resp = response.data;
        if (resp.success) {
          _this.fileItems = resp.content;
          if(_this.fileItems !== null && Tool.isNotEmpty_ES5(_this.fileItems)){
            _this.fileItems.forEach((item)=>{
              _this.imgList.push(item.absPath);
            })
          }
        } else {
          Toast.warning(resp.message,Swal);
        }
      })
    },

  },
}
</script>

<style lang="scss" scoped>
@import './../../../public/static/scss/caipd.scss';

</style>

<style>
.el-form-item--mini.el-form-item, .el-form-item--small.el-form-item {
  margin-bottom: 8px !important;
}
</style>
