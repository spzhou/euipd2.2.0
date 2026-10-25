<!--
 * Author: 周朔鹏
 * Date: 2025-04-15
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
          <el-col :span="6">
            <el-form-item label="任务执行人:">
              <el-col :span="23">
                <span>{{ jobItem.executorName }}</span>
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
              <span>{{ jobItem.taskDescription }}</span>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="23">
            <el-form-item label="工作陈述" required >
              <el-input type="textarea" rows="5" v-model="jobItem.jobDescription"></el-input>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <div style="margin-left: 60px; margin-top: 15px;">
            <div style=" font-size: 15px; margin-bottom: 5px;">特性定义:</div>
            <span v-for="(item, i) in characterDemandList">
              <span style="font-size: 14px;">《{{ item.characterName}}》</span>
            </span>
          </div>
        </el-row>
      </el-form>
      <div  class="dialog-footer">
        <div style="display: flex; justify-content: center;">
          <button type="button" v-on:click="submitJobitem()" class="ca-btn-primary">提交</button>
          <button type="button" v-on:click="close()" class="ca-btn-primary">关闭</button>
        </div>
      </div>

    </div>
  </div>
</template>

<script>
import FileRdms from "../rdms/file-rdms.vue";
import Swal from "sweetalert2";
import FileUploadCore from "@/components/std/file-upload-core.vue";
export default {
  components: {
    FileUploadCore,
    FileRdms,
  },
  name: "cbb-submit-dlg",

  props: {
    jobitem: {
      default: null,
    },
    jobType: {
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
      characterDemandList: [],

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
        return  this.jobitem.preProjectId;
      }
      if(this.projectType === PROJECT_TYPE.PROJECT.key){
        return  this.jobitem.projectId;
      }
      if(this.projectType === PROJECT_TYPE.SUB_PROJECT.key || this.projectType === PROJECT_TYPE.DEV_PROJECT.key){
        return  this.jobitem.subprojectId;
      }
      return 0;
    },
  },

  mounted: function () {
    let _this = this;
    _this.jobItem = _this.jobitem;
    _this.getLoginUser();
    _this.getDemandCharacterList(_this.jobItem.id);

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

    getDemandCharacterList(jobitemId){
      let _this = this;
      let formData = new window.FormData();
      formData.append('jobitemId', jobitemId);
      formData.append('loginUserId', _this.loginUser.customerUserId);
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/character/getCharacterListByJobitemId', formData).then((response) => {
        let resp = response.data;
        if (resp.success) {
          _this.characterDemandList = resp.content;
        }else{
          Toast.warning(resp.message, Swal);
        }
      });
    },

    submitJobitem() {
      let _this = this;
      // 保存校验
      if (1 !== 1
          || !Validator.require(_this.jobItem.jobDescription, "工作陈述",Swal)
          || !Validator.length(_this.jobItem.jobDescription, "工作陈述", 4, 2000,Swal)
      ) {
        return;
      }

      _this.jobItem.type = _this.jobType;
      _this.jobItem.projectType = _this.projectType;
      _this.jobItem.loginUserId = _this.loginUser.customerUserId;

      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/job-items/submit', _this.jobItem).then((response)=>{
        let resp = response.data;
        if (resp.success) {
          Toast.success("提交成功", Swal);
          _this.$bus.$emit('close-manhour-tab', _this.index);
          _this.$bus.$emit('notify-character-submit-job-flag', true);

          _this.$bus.$emit('notify-jobitem-count-flag', true); //执行管理
          _this.$bus.$emit('notify-character-submit-job-flag', true); //特性管理 - 功能提交
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
.dialog-footer{
  background-color: $colorBackground;
  margin-top: 20px;
  padding: 20px 0;
}
</style>
