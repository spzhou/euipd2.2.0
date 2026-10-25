<!--
 * Author: 周朔鹏
 * Date: 2025-02-27
 * Email: 391902958@qq.com
 * Website: https://www.euipd.com
-->

<template>
  <JobitemCreateForm
      :info="dialogInfo"
      :executors="dialogInfo.executors"
      executor-value-key="executorId"
      :name-span="11"
      :name-inner-span="23"
      :manhour-margin="false"
      manhour-input-width=""
      hour-label-width="50px"
      :picker-options="pickerOptions"
      :window-hint="issueWindowHintFor('TASK_BUG')"
      :over-load10="overLoad10"
      @executor-change="manhourChange"
      @manhour-change="manhourChange"
      @submit="issueJobitem"
      @cancel="close">
    <template #beside-name>
      <el-col :span="6">
        <el-form-item label="测试主管">
          <div class="text-box">{{ dialogInfo.testManagerName }}</div>
        </el-form-item>
      </el-col>
    </template>
    
    <template #chart>
      <el-row>
        <div v-show="executorId">
          <MyChart
              :key="executorId"
              :chart-id="((executorId) + '-' + (random))"
              :executor-id="executorId"
              :over-load-callback="getOverLoad"
          ></MyChart>
        </div>
      </el-row>
    </template>
    <template #after-description>
      <el-row style="margin-top: 5px;">
        <el-col :span="24">
          <el-form-item label="缺陷反馈单">
            <el-col :span="20">
              <div class="text-box">{{ dialogInfo.bugfeedbackName }}</div>
            </el-col>
          </el-form-item>
        </el-col>
      </el-row>
      <el-row>
        <el-form-item label="关联项目:" required>
          <el-cascader
              style="width: 500px"
              :options="projectTreeList"
              v-model="optionValue"
              :props="{ checkStrictly: true }"
              clearable
              @change="getSelectedProjectId"
          >
          </el-cascader>
        </el-form-item>
      </el-row>
      <el-row>
        <el-form-item label="关联特性">
          <el-col :span="24">
            <el-select v-model="selectCharacterId" filterable clearable placeholder="已发布特性（可不选）" no-data-text="该子项目暂无已发布特性" style="width: 220px;">
              <el-option v-for="o in characterDtoList" :key="((o.id))" :value="o.id" :label="o.characterName"></el-option>
            </el-select>
          </el-col>
        </el-form-item>
      </el-row>
    </template>
    <template #manhour-extra>
      <div v-if="projectType !== PROJECT_TYPE.PRE_PROJECT.key">
        <div style="display: inline-block; "><em>成本概要: 工时成本: {{ hourCost | rmbFormat }}元</em></div>
      </div>
    </template>
    <template #in-form>
      <div class="hr hr-9 dotted"></div>
      <div style="margin: 15px 110px 20px 0">
        <FileUploadCore
            :input-id="('jobitem-create-by-quality-id-dlg-' + (jobItem.id))"
            :key="('file-upload-' + (jobItem.id))"
            :x-project-id="getXProjectId"
            :item-id="jobItem.id"
            :file-group="FILE_GROUPING.JOBITEM.key"
            :project-type="projectType"
            :read-db-record="true"
            :en-delete="true"
            v-on:uploadFileList="getUploadFileList"
        ></FileUploadCore>
      </div>
      
      <div style="width: 100%; display: flex; flex-direction: row; justify-content: end; align-items: center;">
        <small style="margin-right: 80px;"><em>( 操作指引: 发任务的时候, 如果能够为工作执行人提供一些辅助资料, 将有助于提高工作效率. )</em></small>
      </div>
    </template>
  </JobitemCreateForm>
</template>

<script>
import FileRdms from "../rdms/file-rdms.vue";
import Swal from "sweetalert2";
import FileUploadCore from "@/components/std/file-upload-core.vue";
import MyChart from "@/components/util/myChart.vue";

import JobitemCreateForm from "@/components/std/jobitem-create-form.vue";
import jobitemCreateDlgMixin from "@/mixins/jobitemCreateDlgMixin";

export default {
  mixins: [jobitemCreateDlgMixin],
  components: {
    JobitemCreateForm,
    FileUploadCore,
    FileRdms,
    MyChart,
  },
  name: "jobitem-create-by-bugfeedback-id-dlg",

  props: {
    bugfeedbackId: {
      default: null
    },
    characterId: {
      default: null
    },
    jobType: {
      default: null,
    },
    projectType: {
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

      dialogInfo: {},
      hourCost: 0,
      stdDevManhourFee: 0,

      jobItem: {
        manhour: 0,
      },
      character: {},
      characterDtoList: [],
      selectCharacterId: '',

      projectTreeList: [],
      optionValue: [],
      bugfeedbackItem: {},

      resFile: {},
      resFiles: [],
      resFileIds: [],

      FILE_GROUPING: FILE_GROUPING,
      JOB_ITEM_TYPE: JOB_ITEM_TYPE,
      PROJECT_TYPE: PROJECT_TYPE,

      executorId: '',
      overLoad10: false,
      random: 0,

      pickerOptions: {
        disabledDate: (v) => this.disabledSubmitDateInWindow(v, JOB_ITEM_TYPE.TASK_BUG.key),
      },
      redColor: 'red-color',
    };
  },

  computed: {
    getXProjectId(){
      if(this.projectType === PROJECT_TYPE.PRE_PROJECT.key){
        return  this.dialogInfo.preProjectId;
      }
      if(this.projectType === PROJECT_TYPE.PROJECT.key){
        return  this.dialogInfo.projectId;
      }
      if(this.projectType === PROJECT_TYPE.SUB_PROJECT.key || this.projectType === PROJECT_TYPE.DEV_PROJECT.key){
        return  this.dialogInfo.subprojectId;
      }
      return 0;
    },
  },

  mounted: function () {
    let _this = this;
    _this.jobItem.id = Uuid.shortUuid(8);
    _this.random = Math.random();
    _this.getLoginUser();
    _this.getProjectTreeListByCustomerId();
    if(_this.bugfeedbackId){
      _this.getBugDetailInfo(_this.bugfeedbackId);
      _this.getJobItemCreateDlgInfoByBugfeedbackId(_this.bugfeedbackId);
    }

  },

  methods: {


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

    getBugDetailInfo(bugfeedbackId){
      let _this = this;
      let formData = new window.FormData();
      formData.append('bugfeedbackId', bugfeedbackId);
      formData.append('loginUserId', _this.loginUser.customerUserId);
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/bugfeedback/getBugDetailInfo', formData).then((response)=>{
        let resp = response.data;
        if (resp.success) {
          _this.bugfeedbackItem = resp.content;
          let associateProject = _this.bugfeedbackItem.associateProjectStr ? JSON.parse(_this.bugfeedbackItem.associateProjectStr) : null;
          _this.optionValue = Array.isArray(associateProject) ? associateProject : [];
          _this.getSelectedProjectId(_this.optionValue);
        } else {
          Toast.warning(resp.message,Swal);
        }
      })
    },

    getProjectTreeListByCustomerId(){
      let _this = this;
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/bugfeedback/getProjectTreeListByCustomerId/'+ _this.loginCustomer.id).then((response)=>{
        let resp = response.data;
        if (resp.success) {
          _this.projectTreeList = resp.content;
        } else {
          Toast.warning(resp.message,Swal);
        }
      })
    },

    getSelectedProjectId(value){
      let _this = this;
      _this.selectCharacterId = "";
      _this.characterDtoList = [];
      if (!Array.isArray(value) || value.length === 0) {
        return;
      }
      _this.getCharacterListIncludeNextLevelBySubprojectId(value[value.length - 1]);
    },

    getCharacterListIncludeNextLevelBySubprojectId(subprojectId){
      let _this = this;
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/bugfeedback/getCharacterListIncludeNextLevelBySubprojectId/'+ subprojectId).then((response)=>{
        let resp = response.data;
        if (resp.success) {
          _this.characterDtoList = resp.content;
        } else {
          Toast.warning(resp.message,Swal);
        }
      })
    },

    manhourChange(){
      let _this = this;
      if(_this.dialogInfo && _this.dialogInfo.manhour){
        if(_this.dialogInfo.manhour < 0){
          _this.dialogInfo.manhour =0;
        }
      }
      if(Tool.isNotEmpty(_this.dialogInfo.executorId)){
        let workerManhourFee = 0;
        _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/manhour/getManhourFeeByWorkerId/'+ _this.dialogInfo.executorId).then((response)=>{
          let resp = response.data;
          if (resp.success) {
            workerManhourFee = resp.content;
            _this.hourCost = _this.dialogInfo.manhour * workerManhourFee;
          } else {
            Toast.warning(resp.message,Swal);
          }
        })
      } else{
        _this.hourCost = _this.dialogInfo.manhour * _this.stdDevManhourFee;
      }

      _this.renderChart(_this.dialogInfo.executorId);
    },

    getJobItemCreateDlgInfoByBugfeedbackId(bugfeedbackId){
      let _this = this;
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/job-items/getJobItemCreateDlgInfoByBugfeedbackId/'+ bugfeedbackId).then((response)=>{
        let resp = response.data;
        if (resp.success) {
          _this.dialogInfo = resp.content;
        } else {
          Toast.warning(resp.message,Swal);
        }
      })
    },

    /**
     * 下发任务
     */
    issueJobitem() {
      let _this = this;
      // 保存校验
      if (1 !== 1
          || !Validator.require(_this.dialogInfo.jobName, "任务名称",Swal)
          || !Validator.require(_this.dialogInfo.executorId, "工作执行人",Swal)  /*任务的执行人,在任务流转全周期保持不变; 这与任务Process的执行人概念不同*/
          || !Validator.require(_this.dialogInfo.taskDescription, "任务描述",Swal)
          || !Validator.require(_this.dialogInfo.manhour, "工时",Swal)
          || !Validator.require(_this.dialogInfo.planSubmissionTime, "提交时间",Swal)

          || !Validator.length(_this.dialogInfo.jobName, "任务名称", 2, 50,Swal)
          || !Validator.length(_this.dialogInfo.taskDescription, "任务描述", 10, 500,Swal)
          || !Validator.length(_this.dialogInfo.manhour, "工时", 1, 4,Swal)
      ) {
        return;
      }

      if(Tool.isEmpty_ES5(_this.optionValue) || _this.optionValue.length === 0){
        Toast.warning("关联项目不能为空!", Swal);
        return;
      }

 /*     if(Tool.isEmpty(_this.selectCharacterId) || _this.selectCharacterId === ""){
        Toast.warning("关联特性不能为空!", Swal);
        return;
      }*/

      let iter = _this.optionValue.length;
      _this.jobItem.subprojectId = _this.optionValue[iter -1];
      _this.jobItem.projectId = _this.optionValue[0];
      _this.jobItem.associateProjectStr = JSON.stringify(_this.optionValue);
      _this.jobItem.characterId = _this.selectCharacterId;

      _this.jobItem.jobName = _this.dialogInfo.jobName;
      _this.jobItem.executorId = _this.dialogInfo.executorId;
      _this.jobItem.qualityId = _this.bugfeedbackId;   //共用qualityId
      _this.jobItem.testManagerId = _this.dialogInfo.testManagerId;   //测试主管
      _this.jobItem.taskDescription = _this.dialogInfo.taskDescription;
      _this.jobItem.manhour = _this.dialogInfo.manhour;
      _this.jobItem.planSubmissionTime = _this.dialogInfo.planSubmissionTime;
      _this.jobItem.customerId = _this.loginCustomer.id;
      _this.jobItem.fileList = _this.resFiles;

      _this.jobItem.type = JOB_ITEM_TYPE.TASK_BUG.key;  //关键代码
      _this.jobItem.projectType = PROJECT_TYPE.SUB_PROJECT.key;
      _this.jobItem.loginUserId = _this.loginUser.customerUserId;
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/job-items/save', _this.jobItem).then((response)=>{
        let resp = response.data;
        if (resp.success) {
          _this.jobItem = {};
          _this.resFile = {};
          _this.resFiles = [];
          _this.resFileIds = [];
          _this.jobItem.id = Uuid.shortUuid(8); //重新生成一个ID
          _this.close();
          Toast.success("任务已发出", Swal);
          _this.$bus.$emit('refresh-gantt-list', true);
          _this.$bus.$emit('notify-jobitem-count-flag', true); //执行管理
          _this.$bus.$emit('refresh-bug-task-jobitem-issue',true);
          _this.$bus.$emit('refresh-bug-task-jobitem-examine',true);

        } else {
          Toast.warning(resp.message, Swal);
        }
      })
    },

    close(){
      let _this = this;
      _this.closeDlg();
    },

    renderChart(executorId){
      let _this = this;
      _this.executorId = executorId;
    },
    getOverLoad(value){
      let _this = this;
      if(value && value[0]){
        if(value[0] > MAX_DAY_HOUR){
          _this.overLoad10 = true;
        }else{
          _this.overLoad10 = false;
        }
      }else{
        _this.overLoad10 = false;
      }
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
.red-color{
  color: red;
}
</style>
