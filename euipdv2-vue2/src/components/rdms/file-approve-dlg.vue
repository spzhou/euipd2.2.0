<!--
 * Author: 周朔鹏
 * Date: 2024-02-22
 * Email: 391902958@qq.com
 * Website: https://www.euipd.com
-->

<template>
  <div class="dialog-container">
    <div style="margin: 0 90px 0 0">
      <el-form label-width="180px">
        <el-form-item label="申请文件名称:">
          {{ fileItem.fileName }}（第 {{ fileItem.fileVersionNo || 1 }} 版）
        </el-form-item>
        <el-form-item label="申请原因:">
          <div v-rich-html="fileItem.reason"></div>
        </el-form-item>
        <el-form-item label="文件提供者:">
          {{ fileItem.operatorName }}
        </el-form-item>
        <el-form-item label="所属项目:">
          {{ fileItem.projectName }}
        </el-form-item>
        <el-form-item label="文件上传时间:">
          {{ fileItem. fileCreateTime | dateFormat}}
        </el-form-item>
        <el-form-item label="申请人:">
          <el-col :span="7">
            <div style="width: 220px;">
              {{fileItem.proposerName }}
            </div>
          </el-col>
        </el-form-item>
        <el-form-item label="审批人:">
          <el-col :span="7">
            <div style="width: 220px;">
              {{loginUser.trueName }}
            </div>
          </el-col>
        </el-form-item>
      </el-form>
    </div>

    <div class="clearfix form-actions">
      <div style="display: flex; justify-content: center;">
        <button v-on:click="agree()" class="ca-btn-primary" type="button">
          同 意
        </button>
        <button v-on:click="reject()" class="ca-btn-primary" type="button">
          拒 绝
        </button>
      </div>
    </div>
  </div>
</template>
<script>
import Swal from "sweetalert2";

import {Loading} from "element-ui";
import {dateFormat} from "@/filter/filter";

export default {
  name: 'file-approve-dlg',

  components: {
  },

  props:{
    fileApplyId: {
      default: null,
    },
    closeFunction: {
      type: Function,
      default: null
    },
  },

  data() {
    return {
      loginUser: {},
      loginCustomer: {},
      fileItem: {},

    };
  },

  computed: {
  },

  mounted: function() {
    let _this =this;
    _this.getLoginUser();
    _this.getApplyInfo();
  },
  methods:{
    dateFormat,
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

    getApplyInfo() {
      let _this = this;
      let loadingInstance = Loading.service({ fullscreen: true });
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/file/rdms/getApplyInfo/'+ _this.fileApplyId).then((response) => {
        loadingInstance.close();
        let resp = response.data;
        _this.fileItem = resp.content;
      })
    },

    agree() {
      let _this = this;
      // 保存校验
      if (1 !== 1
          || !Validator.require(_this.fileApplyId, "申请Id",Swal)
      ) {
        return;
      }
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/file/rdms/applyFile-agree/' + _this.fileApplyId).then((response)=>{
        let resp = response.data;
        if (resp.success) {
          Toast.success("操作成功", Swal);
          _this.close();
          _this.$bus.$emit('refresh-file-apply-list', true);
          _this.$bus.$emit('waiting-for-approve-num', true);
          _this.$bus.$emit('refresh-file-apply-num', true);
        } else {
          Toast.warning(resp.message, Swal);
        }
      })
    },

    reject() {
      let _this = this;
      // 保存校验
      if (1 !== 1
          || !Validator.require(_this.fileApplyId, "申请Id",Swal)
      ) {
        return;
      }
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/file/rdms/applyFile-reject/' + _this.fileApplyId).then((response)=>{
        let resp = response.data;
        if (resp.success) {
          Toast.success("操作成功", Swal);
          _this.close();
          _this.$bus.$emit('refresh-file-apply-list', true);
          _this.$bus.$emit('waiting-for-approve-num', true);
          _this.$bus.$emit('refresh-file-apply-num', true);
        } else {
          Toast.warning(resp.message, Swal);
        }
      })
    },

    close(){
      let _this = this;
      _this.closeFunction();
    },

  }
};
</script>

<style lang="scss" scoped>
@import './../../../public/static/scss/caipd.scss';

</style>

