<!--
 * Author: 周朔鹏
 * Date: 2024-02-08
 * Email: 391902958@qq.com
 * Website: https://www.euipd.com
-->

<template>
  <div>
    <el-row>
      <el-col :span="12">
        <div style="margin: 0 30px 0 0;">
<!--          <div style="margin-right: 10px;">
            <p>上传用户信息模版文件: import_workers_template.xlsx</p>
          </div>
          <div>
            <FileUploadCore
                :input-id="('user-info-template-' + (loginCustomer.id))"
                :key="('file-upload-user-info-template-' + (loginCustomer.id))"
                :item-id="loginCustomer.id"
                :file-group="FILE_GROUPING.USER_INFO_TEMPLATE.key"
                :read-db-record="true"
                v-on:uploadFileList="getUploadFileList">
            </FileUploadCore>
          </div>-->
        </div>
      </el-col>
      <el-col :span="12">
        内容
      </el-col>
    </el-row>

  </div>
</template>

<script>
  import Swal from "sweetalert2";
  import FileUploadCore from "@/components/std/file-upload-core.vue";

  export default {
    components: {
      FileUploadCore

    },
    name: "general",

    data: function() {
      return {
        loginUser: {},
        loginCustomer: {},

        resFile: {},
        resFiles: [],
        resFileIds: [],

        FILE_GROUPING: FILE_GROUPING,
      }
    },

    mounted: function() {
      let _this = this;
      _this.getLoginUser();
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

      getUploadFileList(resFiles){
        let _this = this;
        _this.resFiles = resFiles;
        if(_this.resFiles !== null){
          _this.resFileIds= [];
          resFiles.forEach(item => {
            _this.resFileIds.push(item.id);
          })
        }
      },
    }
  }
</script>

<style lang="scss">
@import './../../../../public/static/scss/caipd.scss';

</style>
