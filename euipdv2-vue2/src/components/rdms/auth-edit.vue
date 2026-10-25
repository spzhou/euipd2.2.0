<!--
 * Author: 周朔鹏
 * Date: 2024-11-24
 * Email: 391902958@qq.com
 * Website: https://www.euipd.com
-->

<template>
  <div>
    <el-form :label-position="labelPosition" label-width="140px">
      <el-form-item label="应用名称" >
        <el-col :span="20">
          <el-input v-model="authority.name" placeholder="被授权应用的名称"></el-input>
        </el-col>
      </el-form-item>
      <el-form-item label="备注或说明" >
        <el-col :span="20">
          <el-input type="textarea" :rows="3" v-model="authority.note" placeholder="备注或说明"></el-input>
        </el-col>
      </el-form-item>
      <el-form-item v-if="authority.accessSecret" label="请注意" >
        <el-col :span="20">
          <div style="color: #f56c6c;"><em>请及时保存您的AccessKey和AccessSecret!!! 本对话框关闭之后,相应秘钥信息将无法重新展示.</em></div>
        </el-col>
      </el-form-item>
      <el-form-item v-if="authority.accessSecret"  label="AccessKey" >
        <el-col :span="20">
          <el-input v-text="authority.accessKey" ></el-input>
        </el-col>
      </el-form-item>
      <el-form-item v-if="authority.accessSecret"  label="AccessSecret" >
        <el-col :span="20">
          <el-input v-text="authority.accessSecret" ></el-input>
        </el-col>
      </el-form-item>


    </el-form>
    <div class="clearfix form-actions">
      <div style="display: flex; justify-content: center;">
        <button v-if="!authority.accessSecret" v-on:click="saveAuth()" class="ca-btn-primary" type="button">
          保存
        </button>
        <button v-on:click="close()" class="ca-btn-primary" type="button">
          关闭
        </button>
      </div>
    </div>
  </div>
</template>

<script>
import Swal from "sweetalert2";

export default {
  components: {
  },
  name: "auth-edit",

  data: function() {
    return {
      loginUser: {},
      loginCustomer: {},

      authority: {},

      labelPosition: 'right',
    }
  },

  computed: {
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

    close(){
      let _this = this
      _this.$bus.$emit('close-auth-edit-dlg', true);
      _this.$parent.handleClose();
    },

    saveAuth(){
      let _this = this;
      if (1 !== 1
          || !Validator.require(_this.authority.name, "被授权应用名称",Swal)
          || !Validator.length(_this.authority.name, "被授权应用名称", 2, 50,Swal)
      ) {
        return;
      }

      Swal.fire({
        title: '确认',
        text: "是否确认创建此授权登录信息",
        icon: 'question',
        showCancelButton: true,
        confirmButtonColor: '#409EFF',
        cancelButtonColor: '#909399',
        confirmButtonText: '确认!',
        cancelButtonText: '取消!'
      }).then((result) => {
        if (result.value) {
          _this.$ajax.post(process.env.VUE_APP_SERVER + '/system/auth2/save', {
            customerId: _this.loginCustomer.id,
            name: _this.authority.name,
            note: _this.authority.note,
          }).then((response)=>{
            let resp = response.data;
            if (resp.success) {
              _this.authority = resp.content;
              // Toast.success("保存成功", Swal);
            }else{
              Toast.warning(resp.message,Swal);
            }
          })
        }
      })
    },
  }
}
</script>

<style lang="scss">
@import './../../../public/static/scss/caipd.scss';

.el-form-item__label {
  text-align: right;
  vertical-align: middle;
  float: left;
  font-size: 14px;
  color: #606266;
  line-height: 35px !important;
  padding: 0 12px 0 0;
  -webkit-box-sizing: border-box;
  box-sizing: border-box;
}
.el-dialog__body {
  padding: 30px 0 0;
  color: #606266;
  font-size: 14px;
  word-break: break-all;
}
</style>

<style lang="scss" scoped>
.el-form-item--mini.el-form-item, .el-form-item--small.el-form-item {
  margin-bottom: 18px !important;
}

</style>
