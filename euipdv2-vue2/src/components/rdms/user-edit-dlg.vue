<!--
 * Author: 周朔鹏
 * Date: 2024-11-12
 * Email: 391902958@qq.com
 * Website: https://www.euipd.com
-->

<template>
  <div class="dialog-container">
    <div style="padding-left: 80px; padding-right: 80px;">
      <el-row>
        <el-col :span="12">
          <el-form label-width="90px">
            <el-form-item label="员工ID:">
              <el-col :span="16">
                {{ user.id }}
              </el-col>
            </el-form-item>
            <el-form-item label="登录手机:" required>
              <el-col :span="16">
                <div>
                  {{ user.loginName }} <!--loginName和user表中的loginName是同一个, 改了就不能对应了-->
                </div>
              </el-col>
            </el-form-item>
            <el-form-item label="姓名:" required>
              <el-col :span="16">
                <el-input v-model="user.trueName"></el-input>
              </el-col>
            </el-form-item>
            <el-form-item label="专业:" required>
              <el-col :span="16">
                <el-input v-model="user.subject"></el-input>
              </el-col>
            </el-form-item>
            <el-form-item label="学历:" required>
              <el-col :span="16">
                <el-input v-model="user.educationBackground"></el-input>
              </el-col>
            </el-form-item>
            <el-form-item label="学位:">
              <el-col :span="16">
                <el-input v-model="user.degree"></el-input>
              </el-col>
            </el-form-item>
            <el-form-item label="生日:">
              <el-col :span="16">
                <el-date-picker
                    v-model="user.birthday"
                    type="date"
                    placeholder="选择日期">
                </el-date-picker>
              </el-col>
            </el-form-item>
            <el-form-item label="能力描述:">
              <el-col :span="16">
                <el-input type="textarea" rows="4" v-model="user.description"></el-input>
              </el-col>
            </el-form-item>
            <el-form-item label="员工头像:">
              <el-col :span="16">
                <div>
                  <file-rdms v-bind:input-id="'image-upload-profile-user-create'"
                             v-bind:text="'上传员工头像'"
                             v-bind:suffixs="['jpg', 'jpeg', 'png']"
                             v-bind:customer="loginCustomer.id"
                             v-bind:use="FILE_GROUPING.USER.key"
                             v-bind:is-web-file=true
                             v-bind:after-upload="afterUpload">
                  </file-rdms>
                  <div v-show="user.profile" style="width: 140px;">
                    <div style="width: 80px; height: 80px; border-radius: 50%; margin: 8px auto;">
                      <div>
                        <img style="width: 80px; height: 80px; object-fit: cover; border-radius: 50%;" v-bind:src="user.profile" alt="profile">
                      </div>
                    </div>
                  </div>
                </div>
              </el-col>
            </el-form-item>
          </el-form>
        </el-col>
        <el-col :span="12">
          <el-form label-width="100px">
            <el-form-item label="工作地址:">
              <el-col :span="21">
                <el-input v-model="user.officeAddress"></el-input>
              </el-col>
            </el-form-item>
            <el-form-item label="工号:">
              <el-col :span="21">
                <el-input v-model="user.jobNum"></el-input>
              </el-col>
            </el-form-item>
            <el-form-item label="职务:" required>
              <el-col :span="21">
                <el-input v-model="user.title"></el-input>
              </el-col>
            </el-form-item>
            <el-form-item label="标准岗位名称:">
              <el-col :span="21">
                <el-input v-model="user.jobPosition"></el-input>
              </el-col>
            </el-form-item>
            <el-form-item label="办公邮件:">
              <el-col :span="21">
                <el-input v-model="user.officeEmail"></el-input>
              </el-col>
            </el-form-item>
            <el-form-item label="办公电话:">
              <el-col :span="21">
                <el-input v-model="user.officeTel"></el-input>
              </el-col>
            </el-form-item>
            <el-form-item label="职级:" required>
              <el-col :span="21">
                <el-select v-model="user.jobLevel" filterable placeholder="请选择">
                  <el-option
                      v-for="item in jobLevelList"
                      :key="item.levelCode"
                      :label="item.levelCode"
                      :value="item.levelCode">
                  </el-option>
                </el-select>
              </el-col>
            </el-form-item>
            <el-form-item label="加入时间:" required>
              <el-col :span="20">
                <el-date-picker
                    v-model="user.joinTime"
                    type="date"
                    placeholder="选择日期">
                </el-date-picker>
              </el-col>
            </el-form-item>
            <el-form-item label="状态:" required>
              <el-col :span="21">
                <el-select v-model="user.status" filterable placeholder="请选择">
                  <el-option
                      v-for="item in WORKER_STATUS"
                      :key="item.key"
                      :label="item.value"
                      :value="item.key">
                  </el-option>
                </el-select>
              </el-col>
            </el-form-item>
          </el-form>
        </el-col>
      </el-row>
    </div>
    <div class="clearfix form-actions">
      <div style="display: flex; justify-content: center;">
        <button v-on:click="saveWorker()" class="ca-btn-primary" type="button">
          保存
        </button>
<!--        <button v-on:click="reset()" class="ca-btn-primary" type="button">
          重填
        </button>-->
        <button v-on:click="close()" class="ca-btn-primary" type="button">
          关闭
        </button>
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
  name: "user-edit-dlg",

  data: function() {
    return {
      loginUser: {},
      loginCustomer: {},

      FILE_GROUPING: FILE_GROUPING,
      WORKER_STATUS: WORKER_STATUS,

      resFile: {},

      jobLevelList: [],

      pickerOptions: {
        disabledDate(v) {
          return v.getTime() < new Date().getTime() - 86400000;//  - 86400000是否包括当天
        }
      },
    }
  },

  props: {
    user: {
      default: null,
    },
    closeFunction: {
      type: Function,
      default: null
    },
  },

  computed: {
  },

  mounted: function() {
    let _this = this;
    _this.getLoginUser();
    _this.listAllJobLevel();
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

    saveWorker() {
      let _this = this;
      // 保存校验
      if (1 !== 1
          || !Validator.require(_this.user.loginName, "登录手机", Swal)
          || !Validator.require(_this.user.trueName, "真实姓名", Swal)
          || !Validator.require(_this.user.subject, "学科专业", Swal)
          || !Validator.require(_this.user.educationBackground, "学历", Swal)
          // || !Validator.require(_this.user.degree, "学位", Swal)
          // || !Validator.require(_this.user.description, "能力描述", Swal)
          // || !Validator.require(_this.user.jobLevel, "职级代码", Swal)
          || !Validator.require(_this.user.title, "职务", Swal)
          || !Validator.require(_this.user.joinTime, "加入时间", Swal)
          || !Validator.require(_this.user.status, "状态", Swal)


          || !Validator.length(_this.user.loginName, "登录手机", 11, 11, Swal)
          || !Validator.length(_this.user.trueName, "真实姓名", 2, 4, Swal)
          || !Validator.length(_this.user.subject, "学科专业", 2, 20, Swal)
          || !Validator.length(_this.user.educationBackground, "学历", 2, 20, Swal)
          // || !Validator.length(_this.user.degree, "学位", 2, 20, Swal)
          // || !Validator.length(_this.user.description, "能力描述", 2, 500, Swal)
          // || !Validator.length(_this.user.jobLevel, "职级代码", 2, 8, Swal)
          || !Validator.length(_this.user.title, "职务", 2, 20, Swal)
      ) {
        return;
      }

      _this.user.customerId = _this.loginCustomer.id;
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/worker/save', _this.user).then((response)=>{
        let resp = response.data;
        if (resp.success) {
          Toast.success("保存成功", Swal);
          _this.close();
        } else {
          Toast.warning(resp.message, Swal);
        }
      })
    },

//根据客户登录名查询
    loginNameSearch(){
      let _this = this;
      _this.user.customerId = _this.loginCustomer.id;
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/worker/login-search', _this.user).then((response)=>{
        let resp = response.data;
        if (resp.success) {
          _this.user = resp.content;
        } else {
          Toast.warning(resp.message,Swal);
        }
      })
    },

    listAllJobLevel(){
      let _this = this;
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/job-level/list-all/' + _this.loginCustomer.id).then((response)=>{
        let resp = response.data;
        if (resp.success) {
          _this.jobLevelList = resp.content;
        }else{
          Toast.warning(resp.message,Swal);
        }
      })
    },

    afterUpload(resp) {
      let _this = this;
      _this.user.profile = resp.content.absPath;
      // 新增讲师，上传头像后不能实时预览，解决方法二
      _this.$forceUpdate();
    },

    close(){
      let _this = this;
      _this.closeFunction();
    },

    reset(){
      let _this = this;
      _this.user = {};
    },


  }
}
</script>

<style lang="scss">
@import './../../../public/static/scss/caipd.scss';

</style>
