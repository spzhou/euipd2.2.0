<!--
 * Author: 周朔鹏
 * Date: 2025-04-25
 * Email: 391902958@qq.com
 * Website: https://www.euipd.com
-->

<template>
  <div class="login">
    <div class="container" style="position: relative;">
      <div class="logo-bar">
        <div class="logo">
          <a href="https://www.euipd.com/index"><img class="logo-img" src="/static/rdms/image/EUIPD.png" alt=""></a>
        </div>
        <div class="name">
          Elastic Universal Integration Product Development
        </div>
      </div>
      <div style="position: absolute; right: 20px; top:30px;">
        <img style="width: 220px;" :src="headerLogoUrl" alt="EUIPD">
      </div>
    </div><!--顶部导航条-->
    <div class="box-wrapper">
      <div class="login-container">
        <div class="login-visual">
          <img :src="mainLogoUrl" alt="EUIPD">
        </div>
        <div class="login-panel">
          <LoginComponent></LoginComponent>
        </div>
      </div>
    </div>
    <nav-footer></nav-footer>

    <!--机构注册-->
    <el-dialog
        title="机构注册"
        width="25cm"
        :before-close="handleClose"
        :visible.sync="registerDlg"
        :close-on-click-modal=false>
      <div style="display: flex; flex-direction: row; justify-content: center; align-items: center; width: 100%">
        <el-form
            label-width="130px">
          <el-form-item label="团队名称:" required>
            <el-input type="text" placeholder="3~4字为宜"
                      v-model="customer.loginName" @change="checkLoginName()" autocomplete="off"></el-input>
          </el-form-item>
          <el-form-item :label="phoneTitle" required>
            <el-input type="text" :placeholder="phoneTitle"
                      v-model="customer.contactPhone" @change="checkContactPhone(customer.contactPhone)"
                      autocomplete="off"></el-input>
          </el-form-item>
          <div>
            <el-form-item label="机构全称:" required>
              <el-input v-model="customer.customerName" placeholder="机构全称"/>
            </el-form-item>
            <el-form-item label="管理员姓名:" required>
              <el-input v-model="customer.adminName" placeholder="管理员姓名"/>
            </el-form-item>
          </div>
          <el-form-item label="密码(新旧均可):" required>
            <el-input type="password" placeholder="登录密码"
                      v-model="customer.passwordOriginal" autocomplete="off"></el-input>
          </el-form-item>
          <el-form-item label="短信验证码" required>
            <el-row>
              <el-col :span="16">
                <el-input type="text" placeholder="手机验证码"
                          v-model="customer.smsCode"
                          autocomplete="off"
                ></el-input>
              </el-col>
              <el-col :span="8">
                <div>
                  <el-button type="primary" id="send-code-btn" v-on:click="sendSmsForRegister(customer.contactPhone)"
                             style="width: 100%; margin-bottom: 4px;">
                    发送验证码
                  </el-button>
                </div>
              </el-col>
            </el-row>
          </el-form-item>
          <el-form-item required>
            <div style="font-size: 13px;">
              <el-checkbox v-model="userProtocolChecked"></el-checkbox>
              <span>
                    请仔细阅读<a href="javascript:void(0)" @click="userProtocol">用户协议</a>和<a
                  href="javascript:void(0)" @click="privacyPolicy">隐私政策</a>, 确认后才能使用本系统.
                  </span>
            </div>
          </el-form-item>
        </el-form>
      </div>
      <div style="margin-top: 50px;">
        <div class="clearfix form-actions">
          <div style="display: flex; justify-content: center;">
            <div v-if="userProtocolChecked">
              <el-button type="primary" v-on:click="saveCustomer()">提交注册</el-button>
            </div>
            <div v-else>
              <el-button type="primary" disabled>提交注册</el-button>
            </div>
          </div>
        </div>
      </div>
    </el-dialog>

    <!--用户协议-->
    <el-dialog
        width="29cm"
        :visible.sync="userProtocolDlg"
        :before-close="handleClose"
        :show-close="true"
        :close-on-click-modal=false>
      <div slot="title" style="font-size: 20px;">{{ dialogTitle }}</div>
      <div>
        <div style="height: 100%; display: flex; flex-direction: row; justify-content: center;">
          <iframe style="width: 28cm; height: 700px;" id="protocol" src="./user-protocol.html" frameborder="0"></iframe>
        </div>
      </div>
    </el-dialog>

    <!--隐私政策-->
    <el-dialog
        width="29cm"
        :visible.sync="privacyPolicyDlg"
        :before-close="handleClose"
        :show-close="true"
        :close-on-click-modal=false>
      <div slot="title" style="font-size: 20px;">{{ dialogTitle }}</div>
      <div>
        <div style="height: 100%; display: flex; flex-direction: row; justify-content: center;">
          <iframe style="width: 28cm; height: 700px;" id="protocol" src="./privacy-policy.html"
                  frameborder="0"></iframe>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import NavFooter from '../../components/NavFooter';
import LoginComponent from '../../components/rdms/login-component.vue'
import Swal from "sweetalert2";
import CharacterUpdateDlg from "@/components/rdms/character-update-dlg.vue";
import {webAssetUrl} from "@/shared/oss";

export default {
  name: 'login',
  components: {
    CharacterUpdateDlg,
    NavFooter,
    LoginComponent
  },
  data: function () {
    return {
      dialogTitle: '',
      registerDlg: false,
      customer: {},

      userProtocolDlg: false,
      privacyPolicyDlg: false,
      userProtocolChecked: false,

      player: {
        default: null,
      },

      phoneTitle: '管理员联系电话',
      headerLogoUrl: webAssetUrl('login-header-logo.png'),
      mainLogoUrl: webAssetUrl('login-visual.png')
    }
  },

  computed: {},

  mounted() {
    let _this = this;
    // _this.onLoadFun();
  },
  methods: {
    register(data) {
      let _this = this;
      _this.registerDlg = true;
    },
    closeDlg() {
      let _this = this;
      _this.registerDlg = false;
    },

    checkContactPhone(mobile) {
      let _this = this;
      //判读手机号码是否规范
      var re = /^1[3,4,5,6,7,8,9][0-9]{9}$/;
      if (!re.test(mobile)) {
        Swal.fire({
          icon: 'warning',
          title: "手机号码格式不正确!",
          showConfirmButton: false,
          timer: 1500
        })
        return;
      }
      _this.customer.regType = "common";
    },

    checkLoginName() {
      let _this = this;
      // 保存校验
      if (1 !== 1
          || !Validator.require(_this.customer.loginName, "登录名称", Swal)
          || !Validator.length(_this.customer.loginName, "登录名称", 2, 5, Swal)
      ) {
        return;
      }

      _this.$ajax.post(process.env.VUE_APP_SERVER + '/system/rdms/customer/get-customer-by-loginname/' + _this.customer.loginName).then((response) => {
        let resp = response.data;
        if (resp.success) {
          let customer = resp.content;
          if (Tool.isNotEmpty_ES5(customer)) {
            Swal.fire({
              title: '团队名称已存在',
              text: "不能使用重复的团队名称,还是换一个吧",
              icon: 'warning',
              showCancelButton: false,
              confirmButtonColor: '#409EFF',
              confirmButtonText: '确认!',
            }).then((result) => {
              if (result.isConfirmed) {
                // _this.closeDlg();
              }
            }).catch(_ => {
            });
          }
        } else {
          Toast.warning(resp.message, Swal);
        }
      })

    },

    sendSmsForRegister(phone) {
      let _this = this;
      if (!Validator.isMobile(phone, "手机号码", Swal)) {
        return;
      }

      let sms = {
        mobile: phone,
        use: SMS_USE.REGISTER.key
      };

      _this.$ajax.get(process.env.VUE_APP_SERVER + '/system/rdms/customer/is-mobile-exist/' + phone).then((res) => {
        let response = res.data;
        if (response.success) {
          Toast.warning("电话号码已注册!", Swal);
        } else {
          _this.sendSmsCode(sms, "send-code-btn");
        }
      });
    },

    sendSmsCode(sms, btnId) {
      let _this = this;

      // 调服务端发短信接口
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/system/rdms/sms/send', sms).then((res) => {
        let response = res.data;
        if (response.success) {
          //Toast.success("短信已发送");
          Swal.fire({
            position: 'top-end',
            icon: 'success',
            title: "短信已发送",
            showConfirmButton: false,
            timer: 1000
          })
          // 开始倒计时
          _this.countdown = 60;
          _this.setTime(btnId);
        } else {
          //Toast.warning(response.message);
          Swal.fire({
            position: 'top-end',
            icon: 'warning',
            title: response.message,
            showConfirmButton: false,
            timer: 1500
          })
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

    userProtocol() {
      let _this = this;
      _this.userProtocolDlg = true;
      _this.dialogTitle = "EUIPD用户协议";
    },
    privacyPolicy() {
      let _this = this;
      _this.privacyPolicyDlg = true;
      _this.dialogTitle = "EUIPD隐私政策";
    },
    closeDialog() {
      let _this = this;
      _this.userProtocolDlg = false;
      _this.privacyPolicyDlg = false;
    },
    handleClose(done) {
      done();
    },

    saveCustomer() {
      let _this = this;
      const passwordKey = process.env.VUE_APP_PASSWORD_KEY;
      if (!passwordKey) {
        Swal.fire({icon: 'warning', title: '缺少配置', text: '请在前端 .env 文件中设置 VUE_APP_PASSWORD_KEY 后重新构建'});
        return;
      }
      _this.customer.password = hex_md5(_this.customer.passwordOriginal + passwordKey);

      // 保存校验
      if (1 !== 1
          || !Validator.require(_this.customer.loginName, "登录名称", Swal)
          || !Validator.require(_this.customer.contactPhone, "管理员手机", Swal)

          || !Validator.length(_this.customer.loginName, "登录名称", 2, 20, Swal)
          || !Validator.isMobile(_this.customer.contactPhone, "管理员手机", Swal)
      ) {
        return;
      }

      _this.customer.regType = 'common';
      if (_this.customer.regType === 'common') {
        if (1 !== 1
            || !Validator.require(_this.customer.customerName, "机构全称", Swal)
            || !Validator.require(_this.customer.adminName, "管理员姓名", Swal)

            || !Validator.length(_this.customer.customerName, "机构全称", 2, 50, Swal)
            || !Validator.length(_this.customer.adminName, "管理员姓名", 2, 10, Swal)
        ) {
          return;
        }
      }
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/system/rdms/customer/register', _this.customer).then((response) => {
        let resp = response.data;
        if (resp.success) {
          if (_this.customer.regType === 'common') {
            Swal.fire({
              title: '注册提交成功',
              text: "平台将尽快核实您的注册信息,审核通过后,您会收到短信通知.",
              icon: 'info',
              showCancelButton: false,
              confirmButtonColor: '#409EFF',
              confirmButtonText: '确认!',
            }).then((result) => {
              // if (result.isConfirmed) {
              //   _this.closeDlg();
              // }
            }).catch(_ => {
            });
          } else {
            Toast.success("注册成功!", Swal);
          }
          _this.closeDlg();
          _this.customer = {};
        } else {
          _this.customer = {};
          Toast.warning(resp.message, Swal);
        }
      })
    },


    async playVideo(playAuth, videoId, cover, autoPlay) {
      let _this = this;
      if (typeof LazySdk !== 'undefined') {
        await LazySdk.loadAliplayer();
      }
      if (Tool.isNotEmpty_ES5(_this.player)) {
        $("#" + 'J_prismPlayer').remove();
      }
      // 初始化播放器
      $("#" + 'playerId').append("<div id=\"" + 'J_prismPlayer' + "\"></div>");
      _this.player = new Aliplayer({
        id: "J_prismPlayer",
        autoplay: autoPlay,
        width: "1226px",
        height: "700px",
        vid: videoId,
        playauth: playAuth,
        cover: cover,
      });
    },

    videoPlay(videoId, cover, autoPlay) {
      let _this = this;
      $.ajax({
        type: "GET", //提交方式
        url: process.env.VUE_APP_SERVER + "/system/vod/getVideoPlayAuth/" + videoId,
        async: true,  // 重点，false是同步，true是异步
        dataType: 'json',  // 需要写上
        /*data: {
            videoId: videoId,
        },*/
        success: function (response) {
          // console.log(response);
          if (response.success) {
            let playAuth = response.content.body.playAuth;
            _this.playVideo(playAuth, videoId, cover, autoPlay);
          } else {
            //失败了要报错
            window.alert("获取视频授权失败!");
          }
        }
      })
    },
    onLoadFun() {
      let _this = this;
      // console.log("执行了onLoadFun");
      let videoId = '405e817bf64b71ee80475420848d0102';
      let cover = webAssetUrl('微流程封面.png');
      _this.videoPlay(videoId, cover, false);
    },
  }
}
</script>


<style lang="scss">
@import "../../../public/static/scss/caipd";

.box-height {
  height: calc(100vh - 370px);
}

.login {
  background-color: $buttonOnPage;

  & > .container {
    padding: 0;
    position: relative;
    height: 113px;

    .logo-bar {
      height: 95px;
      display: flex;
      flex-direction: row;
      justify-content: left;
      align-items: flex-end;

      .logo {
        width: auto;
        height: 60px;

        .logo-img {
          height: 100%;
        }
      }

      .name {
        font-size: 25px;
        font-weight: 450;
        margin-left: 30px;
      }
    }

    .btn-group {
      position: absolute;
      right: 0;
      top: 0;
    }
  }

  .box-wrapper {
    background-color: white;

    // 113px 页头 + 154px 页脚
    .login-container {
      box-sizing: border-box;
      max-width: 1200px;
      width: 100%;
      min-height: calc(100vh - 267px);
      margin: 0 auto;
      padding: 40px;
      display: flex;
      align-items: center;
      justify-content: center;
      gap: 80px;
    }

    .login-visual {
      flex: 1 1 auto;
      min-width: 0;
      max-width: 640px;
      display: flex;
      align-items: center;
      justify-content: center;

      img {
        width: 100%;
        height: auto;
        display: block;
      }
    }

    .login-panel {
      flex: 0 0 auto;
    }

    @media (max-width: 1100px) {
      .login-visual {
        display: none;
      }
    }
  }

}
</style>
