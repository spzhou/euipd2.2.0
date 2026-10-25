<!--
 * Author: 周朔鹏
 * Date: 2026-10-10
 * Email: 391902958@qq.com
 * Website: https://www.euipd.com
-->

<template>
  <el-dialog title="修改密码" :visible="visible" width="460px" :close-on-click-modal="false"
             append-to-body @close="$emit('update:visible', false)" @closed="reset">
    <el-form label-width="90px" @submit.native.prevent>
      <el-form-item label="原密码">
        <el-input type="password" v-model="form.oldPassword" autocomplete="current-password"></el-input>
      </el-form-item>
      <el-form-item label="新密码">
        <el-input type="password" v-model="form.newPassword" autocomplete="new-password"
                  placeholder="8 至 32 位，含字母与数字"></el-input>
      </el-form-item>
      <el-form-item label="确认密码">
        <el-input type="password" v-model="form.confirm" autocomplete="new-password"
                  @keyup.enter.native="submit"></el-input>
      </el-form-item>
    </el-form>
    <div style="color: #909399; font-size: 13px;">修改成功后，该手机号在所有设备上的登录将失效，需要重新登录。</div>
    <span slot="footer">
      <el-button @click="$emit('update:visible', false)">取消</el-button>
      <el-button type="primary" :loading="submitting" @click="submit">确定</el-button>
    </span>
  </el-dialog>
</template>

<script>
import { fetchLoginSalt, digestPassword, passwordPolicyError } from "@/shared/password";

export default {
  name: 'change-password-dlg',
  props: {
    visible: { type: Boolean, default: false }
  },
  data() {
    return {
      submitting: false,
      form: { oldPassword: '', newPassword: '', confirm: '' }
    };
  },
  methods: {
    reset() {
      this.form = { oldPassword: '', newPassword: '', confirm: '' };
      this.submitting = false;
    },
    loginPhone() {
      const u = Tool.getLoginUser() || {};
      return u.loginPhone || (u.adminUser && u.adminUser.loginName) || u.loginName || '';
    },
    submit() {
      if (!this.form.oldPassword) {
        this.$message.warning('请输入原密码');
        return;
      }
      const error = passwordPolicyError(this.form.newPassword);
      if (error) {
        this.$message.warning(error);
        return;
      }
      if (this.form.newPassword !== this.form.confirm) {
        this.$message.warning('两次输入的新密码不一致');
        return;
      }
      if (this.form.newPassword === this.form.oldPassword) {
        this.$message.warning('新密码不能与原密码相同');
        return;
      }
      this.submitting = true;
      fetchLoginSalt(this.loginPhone()).then((salt) => {
        return this.$ajax.post(process.env.VUE_APP_SERVER + '/system/auth/password', {
          oldPassword: digestPassword(this.form.oldPassword, salt),
          newPassword: digestPassword(this.form.newPassword, salt)
        });
      }).then((response) => {
        this.submitting = false;
        const resp = (response && response.data) || {};
        if (!resp.success) {
          this.$message.warning(resp.message || '修改密码失败');
          return;
        }
        this.$emit('update:visible', false);
        this.$alert('密码已修改，请使用新密码重新登录。', '修改密码', {
          confirmButtonText: '确定',
          showClose: false
        }).catch(() => {}).then(() => this.$logout());
      }).catch((message) => {
        this.submitting = false;
        this.$message.warning(typeof message === 'string' ? message : '修改密码失败，请稍后再试');
      });
    }
  }
};
</script>
