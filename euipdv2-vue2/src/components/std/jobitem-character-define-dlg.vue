<!--
 * Author: 周朔鹏
 * Date: 2025-01-24
 * Email: 391902958@qq.com
 * Website: https://www.euipd.com
-->

<template>
  <div>
    <div class="dialog-container">
      <el-form
          :model="dialogInfo"
          label-width="130px"
          :show-message=false
          class="dialog-width">
        <el-row>
          <el-col :span="11">
            <el-form-item label="任务名称" required>
              <el-col :span="23">
                <el-input v-model="dialogInfo.jobName"></el-input>
              </el-col>
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="产品经理">
              <div class="text-box">{{ dialogInfo.productManagerName }}</div>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="6">
            <el-form-item label="任务执行人" required>
              <el-col :span="24">
                <el-select v-model="dialogInfo.executorId" filterable placeholder="任务执行人" @change="renderChart(dialogInfo.executorId)">
                  <el-option v-for="o in executors" :key="o.id" :value="o.executorId" :label="o.trueName"></el-option>
                </el-select>
              </el-col>
            </el-form-item>
          </el-col>
        </el-row>
        <div v-if="executorId">
          <MyChart
              :key="executorId"
              :chart-id="((executorId) + '-' + (random))"
              :executor-id="executorId"
              :over-load-callback="getOverLoad"
          ></MyChart>
        </div>
        <el-row>
          <el-col :span="22">
            <el-form-item label="任务描述" required>
              <el-input type="textarea" rows="5" v-model="dialogInfo.taskDescription"></el-input>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row v-if="dialogInfo.demandPlains" style="margin-top: 5px;">
          <el-col :span="24">
            <el-form-item label="原始需求列表">
              <el-col :span="20">
                <div v-for="(item, index) in dialogInfo.demandPlains" :key="index" class="text-box">《{{ item.demandName }}》</div>
              </el-col>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="11">
            <el-form-item label="任务工时" required>
              <el-col :span="14">
                <el-form-item required>
                  <div style="display: flex; flex-direction: row;">
                    <el-input type="number" v-model="dialogInfo.manhour"></el-input>
                    <div style="width: 70px; margin-left: 5px;"> 小时</div>
                  </div>
                </el-form-item>
              </el-col>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="7">
            <el-form-item label="提交时间" required>
              <el-date-picker
                  type="date"
                  placeholder="选择日期"
                  v-model="dialogInfo.planSubmissionTime"
                  :picker-options="pickerOptions">
              </el-date-picker>
            </el-form-item>
          </el-col>
        </el-row>
        <div class="hr hr-9 dotted"></div>
        <div style="margin: 15px 90px 20px 0">
          <FileUploadCore
              :input-id="('jobitem-character-define-dlg')"
              :key="('file-upload-' + (jobItem.id))"
              :x-project-id="preProjectId"
              :item-id="jobItem.id"
              :file-group="FILE_GROUPING.JOBITEM.key"
              :project-type="projectType"
              :read-db-record="true"
              :en-delete="true"
              v-on:uploadFileList="getUploadFileList"
          ></FileUploadCore>
        </div>
      </el-form>

      <div class="clearfix form-actions">
        <div style="display: flex; justify-content: center;">
          <button v-if="!overLoad10" type="button" v-on:click="saveJobItem()" class="ca-btn-primary">发任务</button>
          <button v-if="overLoad10" type="button" class="ca-btn-primary disabled">发任务</button>
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
import Chart from "chart.js";
import MyChart from "@/components/util/myChart.vue";

import jobitemCreateDlgMixin from "@/mixins/jobitemCreateDlgMixin";

export default {
  mixins: [jobitemCreateDlgMixin],
  components: {
    FileUploadCore,
    FileRdms,
    MyChart,
  },
  name: "jobitem-character-define-dlg",

  props: {
    demandIdList: {
      default: null
    },
    preProjectId: {
      default: null
    },
    jobType: {
      default: null
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

      jobItem: {},
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

      pickerOptions: {
        disabledDate(v) {
          return v.getTime() < new Date().getTime() - 86400000;//  - 86400000是否包括当天
        }
      },

      executorId: '',
      overLoad10: false,
      random: 0,
    };
  },

  computed: {
  },

  mounted: function () {
    let _this = this;
    _this.getLoginUser();
    _this.random = Math.random();

    _this.jobItem.id = Uuid.shortUuid(8);
    if(Tool.isNotEmpty(_this.demandIdList)){
      _this.getJobItemCreateDlgInfo(_this.preProjectId, _this.demandIdList);
    }else{
      _this.getPreProjectSimpleInfo();
      _this.listExecutors();
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

    getJobItemCreateDlgInfo(preProjectId, demandIdList){
      let _this = this;
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/job-items/getJobItemCreateDlgInfo-demand', {
        preProjectId,
        demandIdList,
      }).then((response)=>{
        let resp = response.data;
        if (resp.success) {
          _this.dialogInfo = resp.content;
          _this.executors = _this.dialogInfo.executors;
          // debugger
        } else {
          Toast.warning(resp.message,Swal);
        }
      })
    },

    getPreProjectSimpleInfo() {
      let _this = this;
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/pre-project/getPreProjectSimpleInfo/'+ _this.preProjectId).then((response)=>{
        let resp = response.data;
        if (resp.success) {
          _this.preProject = resp.content;
          _this.dialogInfo.productManagerName = _this.preProject.productManagerName;
        } else {
          Toast.warning(resp.message,Swal);
        }
      })
    },

    listExecutors(){
      let _this = this;
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/worker/listAllWorkersByPreProjectId/'+ _this.preProjectId).then((response)=>{
        let resp = response.data;
        if (resp.success) {
          _this.executors = resp.content;
        } else {
          Toast.warning(resp.message,Swal);
        }
      })
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

    /**
     * 下发修订任务
     */
    saveJobItem() {
      let _this = this;
      // 保存校验
      if (1 !== 1
          || !Validator.require(_this.dialogInfo.jobName, "任务名称",Swal)
          || !Validator.require(_this.dialogInfo.executorId, "工作执行人",Swal)  /*任务的执行人,在任务流转全周期保持不变; 这与任务Process的执行人概念不同*/
          || !Validator.require(_this.dialogInfo.taskDescription, "任务描述",Swal)
          // || !Validator.require(_this.dialogInfo.manhour, "工时",Swal)
          || !Validator.require(_this.dialogInfo.planSubmissionTime, "提交时间",Swal)

          || !Validator.length(_this.dialogInfo.jobName, "任务名称", 5, 50,Swal)
          || !Validator.length(_this.dialogInfo.taskDescription, "任务描述", 10, 500,Swal)
          // || !Validator.length(_this.dialogInfo.manhour, "工时", 1, 4,Swal)
      ) {
        return;
      }

      _this.jobItem.jobName = _this.dialogInfo.jobName;
      _this.jobItem.taskDescription = _this.dialogInfo.taskDescription;
      _this.jobItem.executorId = _this.dialogInfo.executorId;
      if(!_this.dialogInfo || !_this.dialogInfo.manhour){
        _this.jobItem.manhour = 10;
      }else{
        _this.jobItem.manhour = _this.dialogInfo.manhour;
      }
      _this.jobItem.planSubmissionTime = _this.dialogInfo.planSubmissionTime;

      _this.jobItem.customerId = _this.loginCustomer.id;
      _this.jobItem.preProjectId = _this.preProjectId;

      _this.jobItem.demandIdStr = JSON.stringify(_this.demandIdList)

      _this.jobItem.type = JOB_ITEM_TYPE.DEMAND.key;
      _this.jobItem.fileList = _this.resFiles;
      _this.jobItem.loginUserId = _this.loginUser.customerUserId;

      _this.jobItem.projectType = PROJECT_TYPE.PRE_PROJECT.key;
      // 已添加文件权限代码
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
          _this.$bus.$emit('refresh-demand-submitted-list', true);
          _this.$bus.$emit('notify-character-submit-job-flag', true);
          _this.$bus.$emit('notify-demand-job-flag', true);
          _this.$bus.$emit('notify-jobitem-count-flag', true); //执行管理
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
