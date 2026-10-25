<!--
 * Author: 周朔鹏
 * Date: 2024-04-01
 * Email: 391902958@qq.com
 * Website: https://www.euipd.com
-->

<template>
  <div>
    <div class="form-horizontal">
      <div class="row">
        <!-- 第一列-->
        <div class="col-md-6">
          <div class="form-group">
            <label class="col-sm-3 control-label no-padding-right" for="form-field-user-loginName"> 登录名(手机)<span style="color:red;">*</span> </label>
            <div class="col-sm-9">
              <input v-model="user.loginName" type="text" id="form-field-user-loginName" placeholder="登录名(手机)" class="col-xs-10 col-sm-5" />
              <span class="input-group-btn">
                <button v-on:click="loginSearch()" class="ca-btn-search" type="button">
                  <i class="ace-icon fa fa-search bigger-110"></i>
                </button>
               </span>
            </div>
          </div>
          <div class="form-group">
            <label class="col-sm-3 control-label no-padding-right" for="form-field-user-id"> 用户ID </label>
            <div class="col-sm-9">
              <input v-model="user.id" readonly type="text" id="form-field-user-id" placeholder="用户ID" class="col-xs-10 col-sm-5" />
            </div>
          </div>
          <div class="form-group">
            <label class="col-sm-3 control-label no-padding-right" for="form-field-user-trueName"> 姓名<span style="color:red;">*</span> </label>
            <div class="col-sm-9">
              <input v-model="user.trueName" type="text" id="form-field-user-trueName" placeholder="姓名" class="col-xs-10 col-sm-5" />
            </div>
          </div>
          <div class="form-group">
            <label class="col-sm-3 control-label no-padding-right" for="form-field-user-subject"> 专业</label>
            <div class="col-sm-9">
              <input v-model="user.subject" type="text" id="form-field-user-subject" placeholder="专业" class="col-xs-10 col-sm-5" />
            </div>
          </div>
          <div class="form-group">
            <label class="col-sm-3 control-label no-padding-right" for="form-field-user-educationBackground"> 学历</label>
            <div class="col-sm-9">
              <input v-model="user.educationBackground" type="text" id="form-field-user-educationBackground" placeholder="学历" class="col-xs-10 col-sm-5" />
            </div>
          </div>
          <div class="form-group">
            <label class="col-sm-3 control-label no-padding-right" for="form-field-user-degree"> 学位</label>
            <div class="col-sm-9">
              <input v-model="user.degree" type="text" id="form-field-user-degree" placeholder="学位" class="col-xs-10 col-sm-5" />
            </div>
          </div>
          <div class="form-group">
            <label class="col-sm-3 control-label no-padding-right" for="form-field-user-email"> 电子邮件 </label>
            <div class="col-sm-9">
              <input v-model="user.email" type="text" id="form-field-user-email" placeholder="电子邮件" class="col-xs-10 col-sm-10" />
            </div>
          </div>
          <div class="form-group">
            <label class="col-sm-3 control-label no-padding-right"> 能力描述<span style="color:red;">*</span> </label>
            <div class="col-sm-9">
              <textarea rows="5" v-model="user.description" type="text" placeholder="能力描述" class="col-xs-12 col-sm-10"></textarea>
            </div>
          </div>

        </div>
        <!-- 第二列-->
        <div class="col-md-6">
          <div class="form-group">
            <label class="col-sm-3 control-label no-padding-right" for="form-field-user-customerName"> 团队名称 </label>
            <div class="col-sm-9">
              <input v-model="user.companyName" type="text" id="form-field-user-customerName" placeholder="团队名称" class="col-xs-12 col-sm-9" />
            </div>
          </div>
          <div class="form-group">
            <label class="col-sm-3 control-label no-padding-right" for="form-field-user-officeAddress"> 工作地址 </label>
            <div class="col-sm-9">
              <input v-model="user.baseAddress" type="text" id="form-field-user-officeAddress" placeholder="工作地址" class="col-xs-12 col-sm-9" />
            </div>
          </div>
          <div class="form-group">
            <label class="col-sm-3 control-label no-padding-right" for="form-field-user-receiveAddress"> 收件地址</label>
            <div class="col-sm-9">
              <input v-model="user.receiveAddress" type="text" id="form-field-user-receiveAddress" placeholder="收件地址" class="col-xs-12 col-sm-9" />
            </div>
          </div>
          <div class="form-group">
            <label class="col-sm-3 control-label no-padding-right" for="form-field-user-joinTime"> 注册时间</label>
            <div class="col-sm-9">
              <input v-model="user.createTimeStr" readonly type="text" id="form-field-user-joinTime" placeholder="注册时间" class="col-xs-12 col-sm-9" />
            </div>
          </div>
          <div class="form-group">
            <label class="col-sm-3 control-label no-padding-right">员工头像</label>
            <div class="col-sm-6">
              <file-rdms v-bind:input-id="'image-upload-user-profile-edit'"
                    v-bind:text="'上传员工头像'"
                    v-bind:suffixs="['jpg', 'jpeg', 'png']"
                    v-bind:customer="loginCustomer.id"
                    v-bind:use="FILE_GROUPING.USER.key"
                    v-bind:after-upload="afterUpload">
              </file-rdms>
              <div v-show="user.profile" class="row">
                <div class="col-md-4">
                  <img v-bind:src="user.profile" class="img-responsive">
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <div class="clearfix form-actions">
        <div class="col-md-offset-4 col-md-6">
          <button v-on:click="modifyUser()" class="ca-btn-primary" type="button">
            <i class="ace-icon fa fa-check bigger-110"></i>
            保存
          </button>
          &nbsp; &nbsp; &nbsp;
          <button v-on:click="modifyClose()" class="ca-btn-primary" type="button">
            <i class="ace-icon fa fa-undo bigger-110"></i>
            返回
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import FileRdms from "@/components/rdms/file-rdms";
import Swal from "sweetalert2";
export default {
  components: {
    FileRdms,
  },
  name: "developer-modify",

  props: {
    userInitial: {
      default: null
    },
  },

  data: function() {
    return {
      loginUser: {},
      loginCustomer: {},
      // user: {},
      FILE_GROUPING: FILE_GROUPING,
    }
  },

  computed: {
    //使用计算属性,可以将传入的props参数,返回给一个本地变量,
    user: function (){
      return this.userInitial;
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

    modifyUser() {
      let _this = this;

      // 保存校验
      if (1 !== 1
          || !Validator.require(_this.user.loginName, "手机号码",Swal)
          || !Validator.require(_this.user.trueName, "真实姓名",Swal)
          // || !Validator.require(_this.user.customerName, "团队名称",Swal)
          // || !Validator.require(_this.user.baseAddress, "工作地址",Swal)
          // || !Validator.require(_this.user.subject, "学科专业",Swal)
          // || !Validator.require(_this.user.educationBackground, "学历",Swal)
          // || !Validator.require(_this.user.degree, "学位",Swal)
          || !Validator.require(_this.user.description, "能力描述",Swal)

          || !Validator.length(_this.user.loginName, "手机号码", 11, 11,Swal)
          || !Validator.length(_this.user.trueName, "真实姓名", 2, 4,Swal)
          // || !Validator.length(_this.user.customerName, "团队名称", 5, 50,Swal)
          // || !Validator.length(_this.user.baseAddress, "工作地址", 5, 100,Swal)
          // || !Validator.length(_this.user.subject, "学科专业", 2, 20,Swal)
          // || !Validator.length(_this.user.educationBackground, "学历", 2, 20,Swal)
          // || !Validator.length(_this.user.degree, "学位", 2, 20,Swal)
          || !Validator.length(_this.user.description, "能力描述", 10, 500,Swal)

      ) {
        return;
      }
      //从session中取出登录用户信息
      // let loginUser = Tool.getLoginUser();
      // _this.user.customerId = loginUser.id;
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/system/rdms/user/modify', _this.user).then((response)=>{
        let resp = response.data;
        if (resp.success) {
          Toast.success("保存成功",Swal);
        } else {
          Toast.warning(resp.message,Swal);
        }
      })
    },

    afterUpload(resp) {
      let _this = this;
      let image = resp.content.path;
      _this.user.profile = image;
      // 新增讲师，上传头像后不能实时预览，解决方法二
      _this.$forceUpdate();
    },
    modifyClose(){
      let _this = this;
      _this.$parent.modifyClose();
    },
  }
}
</script>

<style lang="scss">
@import './../../../public/static/scss/caipd.scss';
</style>
