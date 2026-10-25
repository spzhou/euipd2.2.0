<!--
 * Author: 周朔鹏
 * Date: 2025-08-09
 * Email: 391902958@qq.com
 * Website: https://www.euipd.com
-->

<template>
  <div>
    <div>
      <div class="login-form">
        <div class="login-title">欢迎登录</div>
        <div class="tab-content">
          <div id="developerLogin" class="tab-pane" :class="{active: isActive, in: isActive }">
            <!--登录窗口-->
            <div v-show="MODAL_STATUS === STATUS_LOGIN">
              <el-form :label-position="labelPosition">
                <el-form-item label="手机号码">
                  <el-input
                      v-model="user.loginName"
                      placeholder="请输入注册手机号码"
                  ></el-input>
                </el-form-item>
                <el-form-item label="密码">
                  <el-input type="password" placeholder="密码"
                            v-model="user.password"
                  ></el-input>
                </el-form-item>
                <el-form-item label="验证码">
                  <div class="code-row">
                    <el-input id="code-input-2" type="text" placeholder="验证码"
                              v-model="user.imageCode"
                              autocomplete="off"
                              @focus="selectCustomer"
                              v-on:keyup.enter="login()"></el-input>
                    <img id="image-code" class="code-img" :src="kaptcha_url" alt="验证码" title="看不清？点击刷新"
                         v-on:click="loadImageCode()"/>
                  </div>
                </el-form-item>
                <el-form-item label="登录机构" v-show="customerSelectFlag">
                  <el-select v-model="selectedCustomerId" filterable placeholder="请选择登录机构" style="width: 100%;">
                    <el-option
                        v-for="item in customerList"
                        :key="item.id"
                        :label="item.customerName"
                        :value="item.id">
                    </el-option>
                  </el-select>
                </el-form-item>
              </el-form>
              <el-button class="submit-btn" v-on:click="login()" type="primary">账号登录</el-button>
              <div class="register-remember">
                <el-checkbox v-model="remember">记住手机号</el-checkbox>
                <!-- <a href="javascript:;" v-on:click="toForgetDiv()">忘记密码</a> -->
              </div>
            </div>

            <!--重置密码窗口-->
            <div v-show="MODAL_STATUS === STATUS_FORGET">
              <el-form :label-position="labelPosition">
                <el-form-item label="手机号码">
                  <el-input
                      v-model="userForget.loginName"
                      placeholder="请填写注册手机号码"
                  ></el-input>
                </el-form-item>
                <el-form-item label="验证码">
                  <div class="code-row">
                    <el-input type="text" placeholder="手机验证码"
                              v-model="userForget.smsCode"
                              autocomplete="off"
                    ></el-input>
                    <el-button id="forget-send-code-btn" class="code-btn" v-on:click="sendSmsForForget(userForget.loginName)">
                      发送验证码
                    </el-button>
                  </div>
                </el-form-item>
                <el-form-item label="新密码">
                  <el-input type="password" placeholder="8 至 32 位，含字母与数字"
                            v-model="userForget.newPassword" autocomplete="new-password"
                  ></el-input>
                </el-form-item>
                <el-form-item label="确认密码">
                  <el-input type="password" placeholder="确认密码"
                            v-model="userForget.confirm" autocomplete="new-password"
                  ></el-input>
                </el-form-item>
              </el-form>
              <el-button class="submit-btn" v-on:click="resetPassword()" type="primary">重置</el-button>
              <div class="register-remember">
                <a href="javascript:;" v-on:click="toLoginDiv()">我要登录</a>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <el-dialog title="设置新密码" :visible.sync="initialDialogVisible" width="460px"
               :close-on-click-modal="false" append-to-body @closed="resetInitialForm">
      <div style="margin-bottom: 15px; color: #606266;">当前使用的是初始密码，请设置新密码后进入系统。</div>
      <el-form label-width="90px" @submit.native.prevent>
        <el-form-item label="新密码">
          <el-input type="password" v-model="initialForm.newPassword" autocomplete="new-password"
                    placeholder="8 至 32 位，含字母与数字"></el-input>
        </el-form-item>
        <el-form-item label="确认密码">
          <el-input type="password" v-model="initialForm.confirm" autocomplete="new-password"
                    placeholder="再次输入新密码" @keyup.enter.native="submitInitialPassword"></el-input>
        </el-form-item>
      </el-form>
      <span slot="footer">
        <el-button @click="initialDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="initialSubmitting" @click="submitInitialPassword">确定</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>

import Swal from "sweetalert2";
import {Loading} from "element-ui";
import { fetchLoginSalt, digestPassword, passwordPolicyError } from "@/shared/password";

function safeRedirect(target) {
  const value = Array.isArray(target) ? target[0] : target;
  if (typeof value !== 'string' || value.charAt(0) !== '/' || value.indexOf('//') === 0
      || value.indexOf('/\\') === 0 || value.indexOf('/login') === 0) {
    return '/welcome';
  }
  return value;
}

export default {
  name: 'login-component',
  components:{
  },

  props: {
  },

  data: function () {
    return {
      labelPosition: 'top',
      isActive: true,

      // 模态框内容切换：登录、忘记密码
      STATUS_LOGIN: "STATUS_LOGIN",
      STATUS_FORGET: "STATUS_FORGET",
      MODAL_STATUS: "",

      user: {},
      userForget: {},

      remember: true, // 记住手机号
      imageCodeToken: "",
      kaptcha_url:"",

      customerList: [],
      customerSelectFlag: false,
      selectedCustomerId: '',

      initialDialogVisible: false,
      initialSubmitting: false,
      initialForm: { newPassword: '', confirm: '' },
      initialTicket: '',
      initialSalt: '',
      initialLoginName: '',
    }
  },
  mounted() {
    let _this = this;
    _this.toLoginDiv();
  },

  methods: {
    //---------------登录框、忘记密码框切换-----------------
    toLoginDiv() {
      let _this = this;
      // “记住我”只保存手机号；旧版本缓存可能含口令摘要，读取时改写为仅手机号
      let rememberUser = LocalStorage.get(LOCAL_KEY_REMEMBER_USER);
      if (rememberUser && rememberUser.loginName) {
        _this.user = { loginName: rememberUser.loginName };
        LocalStorage.set(LOCAL_KEY_REMEMBER_USER, { loginName: rememberUser.loginName });
        _this.remember = true;
      } else {
        if (rememberUser) {
          LocalStorage.remove(LOCAL_KEY_REMEMBER_USER);
        }
        _this.user = {};
      }
      _this.loadImageCode();
      _this.MODAL_STATUS = _this.STATUS_LOGIN
    },

    toForgetDiv() {
      let _this = this;
      _this.userForget = { loginName: _this.user.loginName || '' };
      _this.MODAL_STATUS = _this.STATUS_FORGET
    },

    showMessage(message, icon) {
      Swal.fire({
        text: message,
        icon: icon || 'info',
        showCancelButton: false,
        confirmButtonColor: '#409EFF',
        confirmButtonText: '确认!'
      });
    },

    toast(message, icon) {
      Swal.fire({
        position: 'top-end',
        icon: icon || 'warning',
        title: message,
        showConfirmButton: false,
        timer: 1500
      });
    },

    //---------------登录框-----------------
    login() {
      let _this = this;
      let loginName = String(_this.user.loginName || '').trim();
      if (!Validator.isMobile(loginName, "登录手机", Swal)) {
        return;
      }
      if (!_this.user.password) {
        _this.toast('请输入密码');
        return;
      }
      if (!_this.user.imageCode) {
        _this.toast('请输入验证码');
        return;
      }
      if (!_this.selectedCustomerId) {
        _this.toast('请选择登录机构');
        return;
      }
      let loadingInstance = Loading.service({ fullscreen: true });
      fetchLoginSalt(loginName).then((salt) => {
        let payload = {
          loginName: loginName,
          password: digestPassword(_this.user.password, salt),
          imageCode: _this.user.imageCode,
          imageCodeToken: _this.imageCodeToken,
          selectedCustomerId: _this.selectedCustomerId
        };
        return _this.$ajax.post(process.env.VUE_APP_SERVER + '/system/auth/login', payload).then((response) => {
          loadingInstance.close();
          let resp = response.data || {};
          if (!resp.success) {
            _this.showMessage(resp.message || '登录失败');
            _this.user.password = "";
            _this.user.imageCode = "";
            _this.loadImageCode();
            return;
          }
          let content = resp.content || {};
          _this.rememberPhone(loginName);
          if (content.passwordChangeRequired) {
            _this.initialTicket = content.passwordChangeTicket;
            _this.initialSalt = salt;
            _this.initialLoginName = loginName;
            _this.user.password = "";
            _this.initialDialogVisible = true;
            return;
          }
          _this.completeLogin(content, loginName);
        });
      }).catch((error) => {
        loadingInstance.close();
        _this.showMessage(typeof error === 'string' ? error : '登录失败，请稍后再试');
        _this.loadImageCode();
      });
    },

    rememberPhone(loginName) {
      if (this.remember) {
        LocalStorage.set(LOCAL_KEY_REMEMBER_USER, { loginName: loginName });
      } else {
        LocalStorage.remove(LOCAL_KEY_REMEMBER_USER);
      }
    },

    completeLogin(loginUser, loginPhone) {
      Tool.setLoginUser({
        ...loginUser,
        loginPhone: loginPhone,
        accessToken: loginUser.accessToken,
        refreshToken: loginUser.refreshToken,
        scopes: loginUser.scopes || [],
        modulePermissions: loginUser.modulePermissions || {},
        resources: loginUser.resources || []
      });
      this.$router.push(safeRedirect(this.$route.query.redirect)).catch(() => {});
    },

    resetInitialForm() {
      this.initialForm = { newPassword: '', confirm: '' };
      this.initialSubmitting = false;
    },

    submitInitialPassword() {
      let _this = this;
      let error = passwordPolicyError(_this.initialForm.newPassword);
      if (error) {
        _this.toast(error);
        return;
      }
      if (_this.initialForm.newPassword !== _this.initialForm.confirm) {
        _this.toast('两次输入的密码不一致');
        return;
      }
      _this.initialSubmitting = true;
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/system/auth/password/initial', {
        passwordChangeTicket: _this.initialTicket,
        newPassword: digestPassword(_this.initialForm.newPassword, _this.initialSalt)
      }).then((response) => {
        _this.initialSubmitting = false;
        let resp = response.data || {};
        if (resp.success) {
          _this.initialDialogVisible = false;
          _this.initialTicket = '';
          _this.completeLogin(resp.content || {}, _this.initialLoginName);
          return;
        }
        if (resp.code === 'PASSWORD_TICKET_INVALID') {
          _this.initialDialogVisible = false;
          _this.initialTicket = '';
          _this.loadImageCode();
        }
        _this.showMessage(resp.message || '设置新密码失败');
      }).catch(() => {
        _this.initialSubmitting = false;
        _this.showMessage('设置新密码失败，请稍后再试');
      });
    },

    selectCustomer(event){
      let _this = this;
      let loginName = String(_this.user.loginName || '').trim();
      if(! Validator.isMobile(loginName, "登录手机",Swal)){
        return;
      }
      _this.$ajax.get(process.env.VUE_APP_SERVER + '/system/rdms/user/list-customer/' + loginName).then((response)=>{
        let resp = response.data;
        if (resp.success) {
          _this.customerList = resp.content || [];
          _this.customerSelectFlag = _this.customerList.length > 0;
          let stillListed = _this.customerList.some(item => item.id === _this.selectedCustomerId);
          if (!stillListed) {
            _this.selectedCustomerId = _this.customerList.length > 0 ? _this.customerList[0].id : '';
          }
        } else {
          _this.toast(resp.message);
        }
      });
    },

    /**
     * 加载图形验证码
     */
    loadImageCode: function () {
      let _this = this;
      _this.imageCodeToken = Tool.uuid(8);
      _this.kaptcha_url = process.env.VUE_APP_SERVER + '/system/rdms/kaptcha/image-code/' + _this.imageCodeToken;
    },

    /**
     * 发送短信
     */
    sendSmsCode(sms, btnId) {
      let _this = this;
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/system/rdms/sms/send', sms).then((res)=> {
        let response = res.data;
        if (response.success) {
          _this.toast("如该手机号已注册，将收到验证码", 'success');
          _this.countdown = 60;
          _this.setTime(btnId);
        } else {
          _this.toast(response.message);
        }
      })
    },

    /**
     * 倒计时
     * @param btnId
     */
    setTime(btnId) {
      let _this = this;
      let btn = $("#" + btnId);
      if (_this.countdown === 0) {
        btn.removeAttr("disabled");
        btn.text("获取验证码");
        return;
      } else {
        btn.attr("disabled", true);
        btn.text("重新发送(" + _this.countdown + ")");
        _this.countdown--;
      }
      setTimeout(function () {
        _this.setTime(btnId);
      }, 1000);
    },

    /**
     * 发送忘记密码短信
     */
    sendSmsForForget(phone) {
      let _this = this;
      let mobile = String(phone || '').trim();
      if(! Validator.isMobile(mobile, "手机号码", Swal)){
        return;
      }
      _this.sendSmsCode({ mobile: mobile, use: SMS_USE.FORGET.key }, "forget-send-code-btn");
    },

    resetPassword() {
      let _this = this;
      let loginName = String(_this.userForget.loginName || '').trim();
      if (!Validator.isMobile(loginName, "手机号码", Swal)) {
        return;
      }
      if (!_this.userForget.smsCode) {
        _this.toast('请输入短信验证码');
        return;
      }
      let error = passwordPolicyError(_this.userForget.newPassword);
      if (error) {
        _this.toast(error);
        return;
      }
      if (_this.userForget.newPassword !== _this.userForget.confirm) {
        _this.toast("两次输入的密码不一致");
        return;
      }
      fetchLoginSalt(loginName).then((salt) => {
        return _this.$ajax.post(process.env.VUE_APP_SERVER + '/system/rdms/user/reset-password', {
          loginName: loginName,
          password: digestPassword(_this.userForget.newPassword, salt),
          smsCode: _this.userForget.smsCode
        }).then((res)=>{
          let response = res.data;
          if (response.success) {
            _this.toast("密码重置成功", 'success');
            _this.user = { loginName: loginName };
            _this.userForget = {};
            _this.loadImageCode();
            _this.MODAL_STATUS = _this.STATUS_LOGIN;
          } else {
            _this.toast(response.message);
          }
        });
      }).catch((message) => {
        _this.toast(typeof message === 'string' ? message : '密码重置失败，请稍后再试');
      });
    },

  }
}
</script>

<style lang="scss" scoped>
@import "./../../../src/../public/static/scss/caipd.scss";

.login-form{
  box-sizing: border-box;
  width: 420px;
  padding: 32px 36px 24px;
  border: 1px solid #ebeef5;
  border-radius: 8px;
  box-shadow: 0 4px 20px rgba(0,0,0,.06);
  background-color: #FFFFFF;

  .login-title{
    margin-bottom: 24px;
    text-align: center;
    font-size: 22px;
    font-weight: 600;
    letter-spacing: 6px;
    color: rgba(10,69,145,0.9);
  }

  .tab-content{
    padding: 0;
    border: 0;
    background-color: #FFFFFF;
  }

  ::v-deep .el-form-item{
    margin-bottom: 18px;
  }
  ::v-deep .el-form-item__label{
    padding-bottom: 4px;
    line-height: 22px;
    color: #606266;
  }
  ::v-deep .el-input__inner{
    height: 40px;
    line-height: 40px;
  }

  .code-row{
    display: flex;
    align-items: center;
    gap: 8px;

    .el-input{
      flex: 1;
    }
    .code-img{
      flex: 0 0 auto;
      width: 110px;
      height: 40px;
      border: 1px solid #dcdfe6;
      border-radius: 4px;
      box-sizing: border-box;
      object-fit: cover;
      cursor: pointer;
    }
    .code-btn{
      flex: 0 0 auto;
      height: 40px;
    }
  }

  .submit-btn{
    width: 100%;
    height: 42px;
    margin-top: 6px;
    font-size: 16px;
    letter-spacing: 4px;
  }

  .register-remember{
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-top: 16px;
    min-height: 22px;
  }
}

.tabActive{
  color: $buttonOnBackground;
}

</style>
