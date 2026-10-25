<!--
 * Author: 周朔鹏
 * Date: 2025-02-23
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
      :over-load10="overLoad10"
      cancel-text="关闭"
      @executor-change="manhourChange"
      @submit="issueJobitem"
      @cancel="close">
    <template #beside-name>
      <el-col :span="6">
        <el-form-item label="产品经理">
          <div class="text-box">{{ dialogInfo.productManagerName }}</div>
        </el-form-item>
      </el-col>
    </template>
    <template #chart>
      <el-row>
        <el-col :span="22">
          <div v-show="executorId" style="width: 100%">
            <MyChart
                :key="executorId"
                :chart-id="((executorId) + '-' + (random))"
                :executor-id="executorId"
                :over-load-callback="getOverLoad"
            ></MyChart>
          </div>
        </el-col>
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
      <el-row v-if="jobItemOrigin.rdType !== RD_TYPE.MODULE.key">
        <el-col :span="24">
          <el-form-item label="目标里程碑" required>
            <el-col :span="6">
              <el-select v-model="characterOrigin.milestoneId" filterable placeholder="目标里程碑" style="width: 100%;;">
                <el-option v-for="o in milestones" :key="o.id" :value="o.id" :label="o.milestoneName"></el-option>
              </el-select>
            </el-col>
            <el-col>
              <small><em>( 选择一个里程碑. 当进入开发阶段后, 这个里程碑时间将作为特性开发完成时间进行控制.)</em></small>
            </el-col>
          </el-form-item>
        </el-col>
      </el-row>
    </template>
    <template #in-form>
      <div class="hr hr-9 dotted"></div>
      <div style="margin: 15px 110px 20px 0">
        <FileUploadCore
            :input-id="('jobitem-create-by-suggest-' + (jobItem.id))"
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
import MyChart from "@/components/util/myChart-suggest.vue";

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
  name: "jobitem-create-by-suggest",

  props: {
    jobItemOrigin: {
      default: null,
    },
    jobType: {
      default: null,
    },
    projectType: {
      default: null,
    },
    index: {
      default: null
    },
    entrance: {
      default: null,
    },
  },

  data() {
    return {
      loginUser: {},
      loginCustomer: {},

      dialogInfo: {
        manhour: 0,
      },

      jobItem: {
        manhour: 0,
      },
      characterOrigin: {},
      projectManagers: [],
      executors: [],
      milestones: [],
      characters: [],
      subProject: {},
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

      pickerOptions: {
        disabledDate(v) {
          return v.getTime() < new Date().getTime() - 86400000;//  - 86400000是否包括当天
        }
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

    if(_this.jobItemOrigin.characterId){
      _this.getJobItemCreateDlgInfo(_this.jobItemOrigin.characterId);
      _this.getCharacterSimpleInfo(_this.jobItemOrigin.characterId);
      _this.getSubProjectInfo(_this.jobItemOrigin.subprojectId);
    }
  },

  methods: {


    getUploadFileList(resFiles){
      let _this = this;
      _this.resFiles = resFiles;
      if (_this.resFiles !== null) {
        _this.resFileIds = [];
        if (resFiles && resFiles.length > 0) {
          resFiles.forEach(item => {
            _this.resFileIds.push(item.id);
          })
        }
      }
    },

    manhourChange(){
      let _this = this;
      if(_this.dialogInfo && _this.dialogInfo.manhour){
        if(_this.dialogInfo.manhour < 0){
          _this.dialogInfo.manhour =0;
        }
      }
      _this.renderChart(_this.dialogInfo.executorId);
    },

    getJobItemCreateDlgInfo(characterId){
      let _this = this;
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/job-items/getJobItemCreateDlgInfo/'+ characterId).then((response)=>{
        let resp = response.data;
        if (resp.success) {
          _this.dialogInfo = resp.content;
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

    getCharacterSimpleInfo(characterId){
      let _this = this;
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/character/getCharacterSimpleInfo/'+ characterId).then((response)=>{
        let resp = response.data;
        if (resp.success) {
          _this.characterOrigin = resp.content;
        } else {
          Toast.warning(resp.message, Swal);
        }
      })
    },

    getSubProjectInfo(subProjectId) {
      // debugger
      let _this = this;
      let formData = new window.FormData();
      formData.append('subProjectId', subProjectId);
      formData.append('loginUserId', _this.loginUser.customerUserId);
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/sub-project/getSubProjectInfo', formData).then((response)=>{
        let resp = response.data;
        if (resp.success) {
          _this.subProject = resp.content;
          _this.milestones = _this.subProject.milestoneList;
        } else {
          Toast.warning(resp.message, Swal);
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
      if(_this.jobItemOrigin.rdType !== RD_TYPE.MODULE.key){
        if (1 !== 1
            || !Validator.require(_this.characterOrigin.milestoneId, "里程碑",Swal)
        ) {
          return;
        }
      }

      function extracted() {
        //然后,针对这条虚拟的立项功能 发修订任务
        _this.jobItem.jobName = _this.dialogInfo.jobName;
        _this.jobItem.executorId = _this.dialogInfo.executorId;
        _this.jobItem.parentJobitemId = _this.dialogInfo.createCharacterJobitemId;
        _this.jobItem.taskDescription = _this.dialogInfo.taskDescription;
        _this.jobItem.characterId = _this.dialogInfo.characterId;
        _this.jobItem.projectId = _this.dialogInfo.projectId;
        _this.jobItem.preProjectId = _this.dialogInfo.preProjectId;
        _this.jobItem.subprojectId = _this.dialogInfo.subprojectId;
        _this.jobItem.manhour = _this.dialogInfo.manhour;
        _this.jobItem.planSubmissionTime = _this.dialogInfo.planSubmissionTime;
        _this.jobItem.customerId = _this.loginCustomer.id;
        _this.jobItem.fileList = _this.resFiles;
        _this.jobItem.type = JOB_ITEM_TYPE.ITERATION.key;  //功能修订

        if (_this.jobItemOrigin.rdType !== RD_TYPE.MODULE.key) {
          _this.jobItem.projectType = PROJECT_TYPE.DEV_PROJECT.key;  //项目经理处理的类型
          _this.jobItem.productManagerId = _this.dialogInfo.productManagerId;
          _this.jobItem.projectManagerId = _this.loginUser.customerUserId;
        } else {
          _this.jobItem.projectType = PROJECT_TYPE.PRE_PROJECT.key;  //项目经理处理的类型
          _this.jobItem.productManagerId = _this.loginUser.customerUserId;
          _this.jobItem.projectManagerId = null;
          _this.jobItem.rdType = RD_TYPE.MODULE.key;
        }
        _this.jobItem.loginUserId = _this.loginUser.customerUserId;

        _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/job-items/save', _this.jobItem).then((response) => {
          let resp = response.data;
          if (resp.success) {
            _this.jobItem = {};
            _this.resFile = {};
            _this.resFiles = [];
            _this.resFileIds = [];
            _this.jobItem.id = Uuid.shortUuid(8); //重新生成一个ID
            _this.close();
            Toast.success("任务已发出", Swal);
            _this.$bus.$emit('notify-jobitem-count-flag', true); //执行管理

          } else {
            Toast.warning(resp.message, Swal);
          }
        })
      }

      //先将原始定义作为 已立项的功能进行保存
      if(_this.jobItemOrigin.rdType !== RD_TYPE.MODULE.key){
        _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/character/saveSuggestCharacterToSetuped', _this.characterOrigin).then((response) => {
          let resp = response.data;
          if (resp.success) {
            // Toast.success("保存成功", Swal);
            extracted();
          } else {
            Toast.warning(resp.message, Swal);
          }
        })
      }else{
        extracted();
      }



    },

    close(){
      let _this = this;
      _this.$bus.$emit('close-manhour-tab', _this.index);
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
