<!--
 * Author: 周朔鹏
 * Date: 2024-04-17
 * Email: 391902958@qq.com
 * Website: https://www.euipd.com
-->

<template>
  <div>
    <div class="dialog-container">
      <el-form
          :model="projectInfo"
          ref="jobitem_reject"
          label-width="130px"
          :show-message=false
          class="dialog-width">
        <el-row style="margin-left: -1px;">
          <el-col :offset="2">
            <el-form>
              <el-form-item prop="jobName">
                <span slot="label">{{ "项目名称:" }}</span>
                <el-col :span="18">
                  {{ projectInfo.projectName }}
                </el-col>
              </el-form-item>
            </el-form>
          </el-col>
        </el-row>
        <el-row>
          <el-col :offset="2">
            <el-form>
              <el-form-item prop="desc">
                <span slot="label">{{ "驳回意见:" }}</span>
                <el-col :span="18">
                  <el-input type="textarea" rows="2" v-model="projectInfo.rejectReason" placeholder="请填写驳回意见"></el-input>
                </el-col>
              </el-form-item>
            </el-form>
          </el-col>
        </el-row>
      </el-form>

      <div style="height: 50px;"></div>
      <div class="clearfix form-actions">
        <div style="display: flex; justify-content: center;">
          <el-button type="primary" v-on:click="reject()" >确定</el-button>
          <el-button type="primary" v-on:click="close()" >取消</el-button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import Swal from "sweetalert2";


export default {
  components: {
  },
  name: "project-application-reject-dlg",

  props: {
    projectId: {
      default: null,
    },
    closeDlg: {
      type: Function,
      default: null
    },
  },

  data() {
    return {
      loginUser: {},
      loginCustomer: {},

      projectInfo: {},
    };
  },

  computed: {

  },

  mounted: function () {
    let _this = this;
    _this.getLoginUser();
    if(Tool.isNotEmpty(_this.projectId)){

      _this.getProjectSimpleInfo(_this.projectId);
    }
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

    getProjectSimpleInfo(projectId){
      let _this = this;
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/project/getProjectSimpleInfo/' + projectId).then((response) => {
        let resp = response.data;
        if (resp.success) {
          _this.projectInfo = resp.content;
        } else {
          Toast.warning(resp.message, Swal);
        }
      })
    },

    reject() {
      let _this = this;
      if (1 !== 1
          || !Validator.require(_this.projectInfo.rejectReason, "驳回原因", Swal)
          || !Validator.length(_this.projectInfo.rejectReason, "驳回原因", 2, 500, Swal)
      ) {
        return;
      }
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/project/reject-application', _this.projectInfo).then((response) => {
        let resp = response.data;
        if (resp.success) {
          Toast.success("操作成功", Swal);
          _this.close();
          _this.$bus.$emit('refresh-setup-list', true);
        } else {
          Toast.warning(resp.message, Swal);
        }
      })
    },

    close(){
      let _this = this;
      _this.closeDlg();
    },
  },
}
</script>

<style lang="scss" scoped>
@import '../../../public/static/scss/caipd';

.text-box{
  display: inline-block;
  padding-left: 3px;
  padding-right: 15px;
  line-height: 30px;
  //width: 100px;
  background-color: rgba(204, 204, 204, 0.20);
}
.dialog-width{
  width: 100%;
}
</style>
