<!--
 * Author: 周朔鹏
 * Date: 2024-02-15
 * Email: 391902958@qq.com
 * Website: https://www.euipd.com
-->

<template>
  <div class="dialog-container">
    <div style="width: 700px; margin: 0 auto;">
      <el-row>
        <el-col :span="11" :offset="3">
          <el-form label-width="110px">
            <el-form-item label="员工ID">
              {{ worker.id }}
            </el-form-item>
            <el-form-item label="登录手机">
              {{ worker.loginName }}
            </el-form-item>
            <el-form-item label="姓名">
              {{ worker.trueName }}
            </el-form-item>
            <el-form-item label="职务">
              {{ worker.title }}
            </el-form-item>
            <el-form-item label="岗位名称">
              {{ worker.jobPosition }}
            </el-form-item>
            <el-form-item label="职级">
              {{ worker.jobLevel }}
            </el-form-item>
          </el-form>
        </el-col>
        <el-col :span="10">
          <el-col>
            <div style="height: 180px; width: 180px; border-radius: 50%; margin-top: 20px;">
              <img style="height: 180px; width: 180px; border-radius: 50%; object-fit: cover;" v-bind:src="worker.profile">
            </div>
          </el-col>
        </el-col>
      </el-row>
    </div>

    <div class="hr hr-18 dotted hr-double"></div>
    <div class="row">
      <div class="col-md-12">
        <div style="display: flex; justify-content: center;">
          <el-transfer
              filterable
              filter-placeholder="请输入权限名称"
              v-model="value"
              :data="data"
              :titles="titles">
          </el-transfer>
        </div>
      </div>
    </div>

    <div class="clearfix form-actions">
      <div style="display: flex; justify-content: center;">
        <button v-on:click="saveAuth()" class="ca-btn-primary" type="button">
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
  name: "user-authorize-dlg",

  props: {
    worker: {
      default: null,
    },
    closeFunction: {
      type: Function,
      default: null
    },
  },

  data: function() {
    return {
      loginUser: {},
      loginCustomer: {},

      data: [],
      value: [],
      titles: ['可选权限','授权权限'],

      roleList: [],
      customerId: '',
    };
  },

  computed: {
  },

  mounted: function() {
    let _this = this;
    _this.getLoginUser();
    _this.listRole();
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

    listRole() {
      let _this = this;
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/system/rdms/role/user/get-customer-roles/'+ _this.loginCustomer.id).then((response)=>{
        let resp = response.data;
        if (resp.success) {
          _this.roleList = resp.content;
          _this.roleList.forEach((role) => {
            _this.data.push({
              label: role.name,
              key: role.id,
            });
          });
          _this.listAuthedRole();
        } else {
          Toast.warning(resp.message,Swal);
        }
      })
    },

    listAuthedRole() {
      let _this = this;
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/system/rdms/role/user/get-user-authed-roles',{
        customerId: _this.loginCustomer.id,
        userId: _this.worker.id,
      }).then((response)=>{
        let resp = response.data;
        if (resp.success) {
          if(resp.content === null){
            _this.value = [];
          }else{
            _this.value = resp.content;
          }
          console.log("_this.value:",_this.value);
        } else {
          Toast.warning(resp.message,Swal);
        }
      })
    },



    saveAuth() {
      let _this = this;
      console.log(_this.value);
      _this.customerId = _this.loginCustomer.id;
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/system/rdms/role/user/save-auth', {
        customerId: _this.loginCustomer.id,
        userId: _this.worker.id,
        roleIds: _this.value,
      }).then((response)=>{
        let resp = response.data;
        if (resp.success) {
          Toast.success("保存成功", Swal);
          _this.closeFunction();
        } else {
          Toast.warning(resp.message, Swal);
        }
      })
    },

    close(){
      let _this = this;
      _this.closeFunction();
    },
  },
};
</script>

<style lang="scss">
@import './../../../public/static/scss/caipd.scss';
</style>
