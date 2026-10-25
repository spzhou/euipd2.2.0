<!--
 * Author: 周朔鹏
 * Date: 2024-01-27
 * Email: 391902958@qq.com
 * Website: https://www.euipd.com
-->

<template>
  <div>
    <div style="width: 100%; margin-bottom: 80px; display: flex; justify-content: center;">
      <div style="margin-right: 80px;">
        <el-form label-width="150px">
          <el-form-item label="登录名称:">
            <div>
              {{ customer.loginName }}
            </div>
          </el-form-item>
          <el-form-item label="输入新密码:" required>
            <el-input type="password" v-model="customer.password" placeholder="8 至 32 位，含字母与数字" autocomplete="new-password"/>
          </el-form-item>
          <el-form-item label="重复新密码:" required>
            <el-input type="password" v-model="customer.confirm" placeholder="重复新密码" autocomplete="new-password"/>
          </el-form-item>

        </el-form>
      </div>
    </div>
    <div class="clearfix form-actions">
      <div style="display: flex; justify-content: center;">
        <button type="button" v-on:click="changeAdminPassword()" class="ca-btn-primary">保 存</button>
        <button type="button" v-on:click="close()" class="ca-btn-primary">取 消</button>
      </div>
    </div>
  </div>

</template>

<script>
import Swal from "sweetalert2";
import { fetchLoginSalt, digestPassword, passwordPolicyError } from "@/shared/password";

export default {
  components: {
  },
  name: "customer-change-password-dlg",

  props: {
    modifiedCustomer: {
      default: null,
    },
    closeDlg: {
      type: Function,
      default: null,
    },
  },


  data: function () {
    return {
      loginUser: {},
      loginCustomer: {},

      customer: {},
    }
  },

  computed: {},

  mounted: function () {
    let _this = this;
    _this.customer = Object.assign({}, _this.modifiedCustomer, { password: '', confirm: '' });
  },

  methods: {

    changeAdminPassword() {
      let _this = this;
      // 保存校验
      if (1 !== 1
          || !Validator.require(_this.customer.password, "管理员密码", Swal)
          || !Validator.require(_this.customer.confirm, "校对密码", Swal)
      ) {
        return;
      }

      let policyError = passwordPolicyError(_this.customer.password);
      if (policyError) {
        Toast.warning(policyError, Swal);
        return;
      }
      if(_this.customer.password !== _this.customer.confirm){
        Toast.error("两次输入的密码不一致!", Swal);
        return;
      }
      if (!_this.customer.contactPhone) {
        Toast.warning("该机构未登记管理员手机号，无法设置密码", Swal);
        return;
      }

      fetchLoginSalt(_this.customer.contactPhone).then((salt) => {
        return _this.$ajax.post(process.env.VUE_APP_SERVER + '/system/rdms/customer/changeAdminPassword', {
          customerId: _this.customer.id,
          password: digestPassword(_this.customer.password, salt),
        });
      }).then((response) => {
        let resp = response.data;
        if (resp.success) {
          Toast.success("保存成功，该机构管理员需重新登录", Swal);
          _this.close();
        } else {
          Toast.warning(resp.message, Swal);
        }
      }).catch((message) => {
        Toast.warning(typeof message === 'string' ? message : "保存失败，请稍后再试", Swal);
      })
    },

    close(){
      let _this = this;
      _this.closeDlg();
    },
  }
}
</script>

