<!--
 * Author: 周朔鹏
 * Date: 2025-02-23
 * Email: 391902958@qq.com
 * Website: https://www.euipd.com
-->

<template>
  <JobitemCreateForm
      :info="dialogInfo"
      :executors="filterExecutors"
      executor-value-key="executorId"
      :name-span="11"
      :name-inner-span="23"
      :show-manhour="dialogInfo.rdType !== RD_TYPE.RESEARCH.key || projectType === PROJECT_TYPE.PRE_PROJECT.key"
      :manhour-margin="false"
      manhour-input-width=""
      hour-label-width="50px"
      :picker-options="pickerOptions"
      :window-hint="issueWindowHintFor(jobType)"
      :over-load10="overLoad10"
      @executor-change="manhourChange"
      @manhour-change="manhourChange"
      @submit="issueJobitem"
      @cancel="close">
    <template #beside-name>
      <el-col :span="6">
        <el-form-item v-if="jobType === JOB_ITEM_TYPE.TASK_TEST.key" label="测试主管">
          <div class="text-box">{{ dialogInfo.testManagerName }}</div>
        </el-form-item>
        <el-form-item v-else label="产品经理">
          <div class="text-box">{{ dialogInfo.productManagerName }}</div>
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
          <el-form-item label="特性定义表">
            <el-col :span="20">
              <div class="text-box">{{ dialogInfo.characterName }}</div>
            </el-col>
          </el-form-item>
        </el-col>
      </el-row>
    </template>
    <template #submit-hint>
      <el-col :span="17">
        <div style="margin-left: 80px; line-height: 28px;">
          特性开发评审时间为: <span style="margin-left: 5px; font-weight: 500;">{{ planCompleteTimeStr }}</span>, 所有工作应于评审日期之前完成.
        </div>
      </el-col>
    </template>
    <template #in-form>
      <div class="hr hr-9 dotted"></div>
      <div style="margin: 15px 110px 20px 0">
        <FileUploadCore
            :input-id="('jobitem-create-by-character-id-dlg-' + (jobItem.id))"
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
import {shortDateFormat} from "@/filter/filter";

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
  name: "jobitem-create-by-character-id-dlg",

  props: {
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

      dialogInfo: {
        manhour: 0,
      },
      remainBudget: 0,
      // projectChargePool: 0,
      originRemainBudget: 0,
      hourCost: 0,
      stdDevManhourFee: 0,

      budget: 0,
      originCharacterRemainBudget: 0,
      characterRemainBudget: 0,

      jobItem: {
        manhour: 0,
      },
      character: {},
      projectManagers: [],
      executors: [],
      milestones: [],
      characters: [],
      resFile: {},
      resFiles: [],
      resFileIds: [],
      FILE_GROUPING: FILE_GROUPING,
      JOB_ITEM_TYPE: JOB_ITEM_TYPE,
      PROJECT_TYPE: PROJECT_TYPE,
      RD_TYPE: RD_TYPE,

      executorId: '',
      overLoad10: false,
      random: 0,

      projectTeamInfo: {},

      pickerOptions: {
        disabledDate: (v) => this.disabledSubmitDateInWindow(v, this.jobType),
      },
      redColor: 'red-color',
      planCompleteTime: {},
      planCompleteTimeStr: '',
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

    filterExecutors(){
      let _this = this;
      let resultList = [];
      if(_this.projectTeamInfo && _this.projectTeamInfo.projectSecLevel > 0){
        _this.dialogInfo.executors.forEach(item => {
          if(_this.projectTeamInfo.teamIdList.includes(item.executorId)){
            resultList.push(item);
          }
        })
      }else{
        resultList = _this.dialogInfo.executors;
      }
      return resultList;
    },
  },

  mounted: function () {
    let _this = this;
    _this.jobItem.id = Uuid.shortUuid(8);
    _this.random = Math.random();
    _this.getLoginUser();

    if(_this.characterId){
      _this.getJobItemCreateDlgInfo(_this.characterId);
      _this.getProjectTeamIdsByCharacterId(_this.characterId);
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

    getProjectTeamIdsByCharacterId(characterId) {
      let _this = this;
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/character/getProjectTeamIdsByCharacterId/' + characterId).then((response) => {
        let resp = response.data;
        if (resp.success) {
          _this.projectTeamInfo = resp.content;
        } else {
          Toast.warning(resp.message, Swal);
        }
      })
    },

    manhourChange(){
      let _this = this;
      if(_this.dialogInfo && _this.dialogInfo.manhour){
        if(_this.dialogInfo.manhour < 0){
          _this.dialogInfo.manhour =0;
        }
      }else{
        _this.dialogInfo.manhour =0;
      }
      if(Tool.isNotEmpty(_this.dialogInfo.executorId)){
        let workerManhourFee = 0;
        _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/manhour/getManhourFeeByWorkerId/'+ _this.dialogInfo.executorId).then((response)=>{
          let resp = response.data;
          if (resp.success) {
            workerManhourFee = resp.content;
            _this.hourCost = _this.dialogInfo.manhour * workerManhourFee;
            _this.remainBudget = _this.originRemainBudget - _this.hourCost;
            _this.characterRemainBudget = _this.originCharacterRemainBudget - _this.hourCost;
          } else {
            Toast.warning(resp.message,Swal);
          }
        })
      } else{
        _this.hourCost = _this.dialogInfo.manhour * _this.stdDevManhourFee;
        _this.remainBudget = _this.originRemainBudget - _this.hourCost;
        _this.characterRemainBudget = _this.originCharacterRemainBudget - _this.hourCost;
      }
      _this.renderChart(_this.dialogInfo.executorId);
    },

    getJobItemCreateDlgInfo(characterId){
      let _this = this;
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/job-items/getJobItemCreateDlgInfo/'+ characterId).then((response)=>{
        let resp = response.data;
        if (resp.success) {
          _this.dialogInfo = resp.content;
          _this.planCompleteTime = resp.content?.planCompleteTime;
          if(_this.planCompleteTime){
            _this.planCompleteTimeStr = shortDateFormat(_this.planCompleteTime);
          }

          if(!_this.dialogInfo.manhour){
            _this.dialogInfo.manhour = 0;
          }
          if(resp.content.budgetExeInfo){
            _this.originRemainBudget = resp.content.budgetExeInfo.devManhourFeeRemain;
            _this.remainBudget = resp.content.budgetExeInfo.devManhourFeeRemain;
            _this.stdDevManhourFee = resp.content.budgetExeInfo.standDevManhourFee;
            _this.characterRemainBudget = resp.content.budgetExeInfo.remainBudget;
            _this.originCharacterRemainBudget = resp.content.budgetExeInfo.remainBudget;
            _this.budget = resp.content.budgetExeInfo.budget;
          }else{
            _this.originRemainBudget = 0;
            _this.remainBudget = 0;
            _this.stdDevManhourFee = 0;
            _this.characterRemainBudget = 0;
            _this.originCharacterRemainBudget = 0;
            _this.budget = 0;
          }
        } else {
          Toast.warning(resp.message,Swal);
        }
      })
    },

    listExecutors(){
      let _this = this;
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/worker/listAllWorkers/'+ _this.project.customerId).then((response)=>{
        let resp = response.data;
        if (resp.success) {
          _this.executors = resp.content;
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
          || !Validator.require(_this.dialogInfo.planSubmissionTime, "提交时间",Swal)

          || !Validator.length(_this.dialogInfo.jobName, "任务名称", 2, 50,Swal)
          || !Validator.length(_this.dialogInfo.taskDescription, "任务描述", 10, 500,Swal)
      ) {
        return;
      }

      // 确保 dialogInfo 存在且 manhour 字段不为空
      if (!_this.dialogInfo || _this.dialogInfo.manhour == null || _this.dialogInfo.manhour === '') {
        _this.dialogInfo = _this.dialogInfo || {};
        _this.dialogInfo.manhour = 0;
      }


      _this.jobItem.jobName = _this.dialogInfo.jobName;
      _this.jobItem.executorId = _this.dialogInfo.executorId;
      _this.jobItem.parentJobitemId = _this.dialogInfo.createCharacterJobitemId;
      _this.jobItem.productManagerId = _this.dialogInfo.productManagerId;
      _this.jobItem.testManagerId = _this.dialogInfo.testManagerId;
      if(_this.projectType === PROJECT_TYPE.DEV_PROJECT.key){
        _this.jobItem.projectManagerId = _this.loginUser.customerUserId;
      }
      _this.jobItem.taskDescription = _this.dialogInfo.taskDescription;
      _this.jobItem.characterId = _this.dialogInfo.characterId;
      _this.jobItem.projectId = _this.dialogInfo.projectId;
      _this.jobItem.preProjectId = _this.dialogInfo.preProjectId;
      _this.jobItem.subprojectId = _this.dialogInfo.subprojectId;
      _this.jobItem.manhour = _this.dialogInfo.manhour;
      _this.jobItem.planSubmissionTime = _this.dialogInfo.planSubmissionTime;
      _this.jobItem.customerId = _this.loginCustomer.id;
      _this.jobItem.fileList = _this.resFiles;

      _this.jobItem.type = _this.jobType;
      _this.jobItem.projectType = _this.projectType;
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
          _this.$bus.publish({ areas: _this.$bus.areasForEntrance(_this.entrance), reason: 'jobitem-create' });

          
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
