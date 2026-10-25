<!--
 * Author: 周朔鹏
 * Date: 2025-03-04
 * Email: 391902958@qq.com
 * Website: https://www.euipd.com
-->

<template>
  <div>
    <div class="dialog-container">
      <el-form
          :model="jobItem"
          ref="jobItem"
          label-width="130px"
          :show-message=false>
        <el-row>
          <el-col :span="11">
            <el-form-item label="任务名称:">
              <el-col :span="23">
                <span>{{ jobItem.jobName }}</span>
              </el-col>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="11">
            <el-form-item label="任务工时:" required>
              <el-col :span="14">
                <el-form-item prop="manhour">
                  <span>{{ jobItem.manhour }}</span>
                </el-form-item>
              </el-col>
            </el-form-item>
          </el-col>
          <el-col :span="7">
            <el-form-item label="计划提交时间:">
              <el-col :span="24">
                <el-form-item>
                  <span>{{ jobItem.planSubmissionTime |dateFormat }}</span>
                </el-form-item>
              </el-col>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="23">
            <el-form-item label="任务描述:" >
              <span style="white-space: pre-line;">{{ jobItem.taskDescription }}</span>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="23">
            <el-form-item label="拒收原因" required >
              <el-input type="textarea" rows="5" v-model="jobitemProcess.jobDescription"></el-input>
            </el-form-item>
          </el-col>
        </el-row>
<!--        <div class="hr hr-9 dotted"></div>
        <div style="margin: 15px 110px 20px 0">
          <FileUploadCore
              :input-id="('jobitem-submit-decompose-dlg-' + (jobItem.id))"
              :key="('file-upload-' + (jobItem.id))"
              :x-project-id="getXProjectId"
              :item-id="jobItem.id"
              :file-group="FILE_GROUPING.JOBITEM.key"
              :project-type="projectType"
              :read-db-record="true"
              :en-delete="true"
              v-on:uploadFileList="getUploadFileList">
            <div style="margin-left: 60px; font-size: 15px; margin-bottom: 5px;">相关附件: <small><u><i>(此处只需提供与任务相关的附件, 这里的附件不会与特性定义关联.)</i></u></small></div>
          </FileUploadCore>
        </div>-->
      </el-form>

      <div  class="dialog-footer">
        <div style="display: flex; justify-content: center;">
          <button type="button" v-on:click="refuseJobitem()" class="ca-btn-primary">拒收</button>
          <button type="button" v-on:click="close()" class="ca-btn-primary">关闭</button>
        </div>
      </div>

    </div>
  </div>
</template>

<script>
import FileRdms from "../../components/rdms/file-rdms";
import Swal from "sweetalert2";
import FileUploadCore from "@/components/std/file-upload-core.vue";
export default {
  components: {
    FileUploadCore,
    FileRdms,
  },
  name: "jobitem-refuse-dlg",

  props: {
    jobitemId: {
      default: null,
    },
    projectType: {
      default: null,
    },
    index : {
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
      jobitemProcess: {},
      characterDecomposeList: [],

      parentCharacter: {},
      project: {},
      projectManagers: [],
      executors: [],
      milestones: [],
      characters: [],
      resFile: {},
      resFiles: [],
      resFileIds: [],
      FILE_GROUPING: FILE_GROUPING,
      JOB_ITEM_TYPE: JOB_ITEM_TYPE,
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

    getUploadFileList(resFiles){
      let _this = this;
      _this.resFiles = resFiles;
      if(_this.resFiles !== null){
        _this.resFileIds= [];
        if(resFiles && resFiles.length>0){
        resFiles.forEach(item => {
          _this.resFileIds.push(item.id);
        })
      }
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

    refuseJobitem() {
      let _this = this;
      // 保存校验
      if (1 !== 1
          || !Validator.require(_this.jobitemProcess.jobDescription, "拒收原因", Swal)
          || !Validator.length(_this.jobitemProcess.jobDescription, "拒收原因", 3, 1000, Swal)
      ) {
        return;
      }
      Swal.fire({
        title: '确认',
        text: "是否确认拒收此项任务?",
        icon: 'question',
        showCancelButton: true,
        confirmButtonColor: '#409EFF',
        cancelButtonColor: '#909399',
        confirmButtonText: '确认!',
        cancelButtonText: '取消!'
      }).then((result) => {
        if (result.isConfirmed) {
          _this.jobitemProcess.jobItemId = _this.jobItem.id;
          _this.jobitemProcess.fileListStr = JSON.stringify(_this.resFileIds);
          _this.jobitemProcess.loginUserId = _this.loginUser.customerUserId;

          _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/job-items/refuseJobitem', _this.jobitemProcess).then((response) => {
            let resp = response.data;
            if (resp.success) {
              Toast.success("保存成功", Swal);
              _this.$bus.$emit('close-manhour-tab', _this.index);
              _this.$bus.$emit('notify-jobitem-count-flag', true); //执行管理
            } else {
              Toast.warning(resp.message, Swal);
            }
          })
        }
      }).catch(_ => {
      });
    },

    close(){
      let _this = this;
      _this.closeDlg();
    },

  },
}
</script>

<style lang="scss" scoped>
@import './../../../public/static/scss/caipd.scss';

.text-box{
  display: inline-block;
  padding-left: 3px;
  padding-right: 15px;
  line-height: 30px;
  //width: 100px;
  background-color: rgba(204, 204, 204, 0.20);
}
.dialog-footer{
  background-color: $colorBackground;
  margin-top: 20px;
  padding: 20px 0;
}
</style>
