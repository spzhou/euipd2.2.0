<!--
 * Author: 周朔鹏
 * Date: 2025-02-27
 * Email: 391902958@qq.com
 * Website: https://www.euipd.com
-->

<template>
  <div>
    <div class="dialog-container">
      <el-form
          :model="jobItem"
          ref="jobitem_reject"
          label-width="130px"
          :show-message=false
          class="dialog-width">
        <el-row style="margin-left: -1px;">
          <el-col :offset="2">
            <el-form>
              <el-form-item prop="jobName">
                <span slot="label">{{ "任务名称:" }}</span>
                <el-col :span="18">
                  {{ jobItem.jobName }}
                </el-col>
              </el-form-item>
            </el-form>
          </el-col>
        </el-row>
        <el-row style="margin-left: 14px;">
          <el-col :offset="2">
            <el-form>
              <el-form-item prop="jobSerial">
                <span slot="label">{{ "任务号:" }}</span>
                <el-col :span="18">
                  {{ jobItem.jobSerial }}
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
                  <el-input type="textarea" rows="2" v-model="jobitemProcess.jobDescription" placeholder="请填写驳回意见"></el-input>
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
import FileRdms from "../rdms/file-rdms.vue";
import Swal from "sweetalert2";
import FileSaver from 'file-saver';
export default {
  components: {
    FileRdms,
    FileSaver,
  },
  name: "jobitem-reject-dlg",

  props: {
    jobitemId: {
      default: null,
    },
    jobType: {
      default: null,
    },
    inheritJobType: {
      default: null,
    },
    projectType: {
      default: null,
    },
    index: {
      default: null,
    },
    entrance: {
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

      jobItem: {},
      characterNameListStr: '',
      jobitemProcess: {},

      characterItem: {},
      characterBudget: {},
      characterProcess: {},

      projectManagers: [],
      executors: [],
      milestones: [],
      characters: [],
      resFile: {},
      resFiles: [],
      resFileIds: [],
      FILE_GROUPING: FILE_GROUPING,

      characterId: '',
    };
  },

  computed: {
    getXProjectId(){
      if(this.projectType === PROJECT_TYPE.PRE_PROJECT.key){
        return  this.jobItem.preProjectId;
      }
      if(this.projectType === PROJECT_TYPE.PROJECT.key){
        return  this.jobItem.projectId;
      }
      if(this.projectType === PROJECT_TYPE.SUB_PROJECT.key || this.projectType === PROJECT_TYPE.DEV_PROJECT.key){
        return  this.jobItem.subprojectId;
      }
      return 0;
    },
  },

  mounted: function () {
    let _this = this;
    _this.getLoginUser();
    if(Tool.isNotEmpty(_this.jobitemId)){
      _this.getJobitemSimpleInfo(_this.jobitemId);
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

    getJobitemSimpleInfo(jobitemId){
      let _this = this;
      let formData = new window.FormData();
      formData.append('jobitemId', jobitemId);
      formData.append('loginUserId', _this.loginUser.customerUserId);
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/job-items/getJobitemSimpleInfo', formData).then((response) => {
        let resp = response.data;
        if (resp.success) {
          _this.jobItem = resp.content;
        }else{
          Toast.warning(resp.message, Swal);
        }
      });
    },

    downloadFile(file) {
      let _this = this;
      _this.getSignedUrl(file.path, file.suffix);
    },
    getSignedUrl (fileFullName, suffix) {
      let _this = this;
      let formData = new window.FormData();
      formData.append('fileFullName', fileFullName);
      formData.append('customer', _this.loginCustomer.id);
      formData.append('user', _this.loginUser.customerUserId);
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/file/rdms/oss-get-signed-url', formData).then((response)=>{
        let resp = response.data;
        Tool.openOssSignedUrl(resp, suffix);
      });
    },

    reject() {
      let _this = this;
      if (1 !== 1
          || !Validator.require(_this.jobitemProcess.jobDescription, "工作描述", Swal)
          || !Validator.length(_this.jobitemProcess.jobDescription, "工作描述", 2, 500, Swal)
      ) {
        return;
      }

      _this.jobitemProcess.jobItemId = _this.jobitemId;
      _this.jobitemProcess.executorId = _this.loginUser.customerUserId;
      _this.jobitemProcess.loginUserId = _this.loginUser.customerUserId;
      _this.jobitemProcess.projectType = _this.projectType;


      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/job-items/reject', _this.jobitemProcess).then((response) => {
        let resp = response.data;
        if (resp.success) {
          _this.resFile = {};
          _this.resFiles = [];
          _this.resFileIds = [];
          _this.close();
          Toast.success("任务已驳回", Swal);
          _this.$bus.publish({ areas: _this.$bus.areasForEntrance(_this.entrance), reason: 'jobitem-reject' });
          if (_this.entrance === ENTRANCE_CLASS.MANHOUR.key
              && (_this.projectType === PROJECT_TYPE.DEV_PROJECT.key
                  || _this.projectType === PROJECT_TYPE.PRE_PROJECT.key)) {
            _this.$bus.$emit('close-manhour-tab', _this.index);
          }
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
