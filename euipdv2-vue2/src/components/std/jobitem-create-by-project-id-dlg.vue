<!--
 * Author: 周朔鹏
 * Date: 2025-02-19
 * Email: 391902958@qq.com
 * Website: https://www.euipd.com
-->

<template>
  <JobitemCreateForm
      :info="dialogInfo"
      :executors="executors"
      :show-manhour="comIds.rdType !== RD_TYPE.RESEARCH.key"
      :picker-options="pickerOptions"
      :window-hint="issueWindowHintFor(jobType)"
      :over-load10="overLoad10"
      @executor-change="manhourChange"
      @manhour-change="manhourChange"
      @submit="saveJobItem"
      @cancel="close">
    <template #beside-name>
      <el-col :span="11" v-if="entrance === ENTRANCE_CLASS.PROJECT.key">
        <el-row>
          <el-row>
            <el-col :span="4">
              <el-checkbox style="line-height: 28px;" v-model="checked" @change="getCompleteJobitemListBySubprojectId">关联任务</el-checkbox>
            </el-col>
            <el-col v-if="checked" :span="18">
              <el-select
                  style="width: 100%"
                  v-model="dialogInfo.associatedJobitemId"
                  filterable placeholder="选择关联任务"
                  @change="getFileListByJobitemId(dialogInfo.associatedJobitemId)">
                <el-option v-for="o in completeJobList" :key="o.id" :value="o.id" :label="o.completeJobNameList"></el-option>
              </el-select>
            </el-col>
          </el-row>
          <div v-if="checked">
            <div v-for="(file, i) in attachmentFileList" :key="i">
              <el-row>
                <el-col :offset="1" :span="4">
                  <el-checkbox style="line-height: 28px;" v-model="attachmentChecked[i]" @change="setReplaceFiles(file, i)">替换附件:</el-checkbox>
                </el-col>
                <el-col v-if="checked" :span="18">
                  <div style="line-height: 28px;">
                    {{ file.name }}
                  </div>
                </el-col>
              </el-row>
            </div>
          </div>
        </el-row>
      </el-col>
    </template>
    
    <template #chart>
      <el-row v-if="comIds.rdType !== RD_TYPE.RESEARCH.key">
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
    <template #submit-hint>
      <el-col :span="17">
        <div style="margin-left: 80px; line-height: 28px;">
          项目评审时间为: <span style="margin-left: 5px; font-weight: 500;">{{ planCompleteTimeStr }}</span>, 所有工作应于评审日期之前完成.
        </div>
      </el-col>
    </template>
    <template #below-form>
      <div style="margin: 15px 110px 20px 0">
        <div class="hr hr-10 dotted"></div>
        <FileUploadCore
            :input-id="('jobitem-create-by-project-id-' + (taskJobitem.id))"
            :key="('file-upload-' + (taskJobitem.id))"
            :x-project-id="xProjectId"
            :item-id="taskJobitem.id"
            :file-group="FILE_GROUPING.JOBITEM.key"
            :project-type="projectType"
            :read-db-record="true"
            :en-delete="true"
            v-on:uploadFileList="getUploadFileList"
        ></FileUploadCore>
      </div>
      <div>
        <select-jobitem-file-list
            :jobitem-id="taskJobitem.id"
            v-on:checkedFileList="getCheckedFileList"
        ></select-jobitem-file-list>
      </div>
    </template>
  </JobitemCreateForm>
</template>

<script>
import FileRdms from "../../components/rdms/file-rdms";
import FileUploadCore from "@/components/std/file-upload-core.vue";
import SelectJobitemFileList from "@/components/std/select-jobitem-file-list.vue";
import MyChart from "@/components/util/myChart.vue";

import Swal from "sweetalert2";
import {shortDateFormat} from "@/filter/filter";

import JobitemCreateForm from "@/components/std/jobitem-create-form.vue";
import jobitemCreateDlgMixin from "@/mixins/jobitemCreateDlgMixin";

export default {
  mixins: [jobitemCreateDlgMixin],
  components: {
    JobitemCreateForm,
    FileRdms,
    FileUploadCore,
    SelectJobitemFileList,
    MyChart,
  },
  name: "jobitem-create-by-project-id-dlg",

  props: {
    xProjectId: {
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

      checked: false,
      attachmentChecked: [],
      dialogInfo: {
        manhour: 0,
      },
      taskJobitem: {},
      executors: [],
      resFiles: [],
      checkedFileList: [],
      completeJobList: [],
      executorId: '',

      attachmentFileList: [],
      replaceFileList: [],

      FILE_GROUPING: FILE_GROUPING,
      JOB_ITEM_TYPE: JOB_ITEM_TYPE,
      PROJECT_TYPE: PROJECT_TYPE,
      ENTRANCE_CLASS: ENTRANCE_CLASS,
      DOC_TYPE: DOC_TYPE,
      RD_TYPE: RD_TYPE,

      parentManhourInfo: {},
      remainFee: 0,
      workerManhour: 0,
      hourCost: 0,

      budget: 0,
      originSubprojectRemainBudget: 0,
      availableRemainBudget: 0,

      remainBudget: 0,
      originRemainBudget: 0,
      stdTestManhourFee: 0,

      subprojectBudgetExeSummary: {},

      pickerOptions: {
        disabledDate: (v) => this.disabledSubmitDateInWindow(v, this.jobType),
      },

      redColor: 'red-color',
      overLoad10: false,
      random: 0,

      comIds: {},
      planCompleteTime: {},
      planCompleteTimeStr: '',
    };
  },

  computed: {

  },

  mounted: function () {
    let _this = this;
    _this.taskJobitem.id = Uuid.shortUuid(8);

    _this.getLoginUser();
    if(Tool.isNotEmpty(_this.xProjectId) && Tool.isNotEmpty(_this.projectType)){
      _this.getIdsDtoByProjectIdAndType(_this.projectType, _this.xProjectId);

      if(_this.projectType === PROJECT_TYPE.SUB_PROJECT.key){
        _this.calSubprojectSelfBudgetActiveExeInfo(_this.xProjectId);
      }
    }

    _this.listExecutors();

  },

  methods: {


    getIdsDtoByProjectIdAndType(projectType, xProjectId){
      let _this = this;
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/project/getIdsDtoByProjectIdAndType', {
        anyProjectId: xProjectId,
        projectType: projectType,
      }).then((response) => {
        let resp = response.data;
        if (resp.success) {
          _this.comIds = resp.content;
          _this.planCompleteTime = resp.content?.planCompleteTime;
          if(_this.planCompleteTime){
            _this.planCompleteTimeStr = shortDateFormat(_this.planCompleteTime);
          }
          if(Tool.isNotEmpty(_this.comIds.projectId)){
            _this.taskJobitem.projectId = _this.comIds.projectId;
            _this.taskJobitem.projectManagerId = _this.comIds.projectManagerId;
            _this.taskJobitem.productManagerId = _this.comIds.productManagerId;
          }
          if(Tool.isNotEmpty(_this.comIds.preProjectId)){
            _this.taskJobitem.preProjectId = _this.comIds.preProjectId;
            _this.taskJobitem.productManagerId = _this.comIds.productManagerId;
          }
          if(Tool.isNotEmpty(_this.comIds.subprojectId)){
            _this.taskJobitem.subprojectId = _this.comIds.subprojectId;
            _this.taskJobitem.projectManagerId = _this.comIds.projectManagerId;
            _this.taskJobitem.productManagerId = _this.comIds.productManagerId;
          }

        } else {
          Toast.warning(resp.message, Swal);
        }
      })
    },

    getCompleteJobitemListBySubprojectId() {
      let _this = this;
      if(_this.checked && _this.projectType === PROJECT_TYPE.SUB_PROJECT.key){
        _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/job-items/getCompleteJobitemListBySubprojectId/' + _this.xProjectId).then((response) => {
          let resp = response.data;
          if (resp.success) {
            _this.completeJobList = resp.content;
            if(Tool.isNotEmpty_ES5(_this.completeJobList)){
              _this.completeJobList.forEach(item => {
                item.completeJobNameList = item.jobSerial + ":" +item.jobName;
              })
            }
          } else {
            Toast.warning(resp.message, Swal);
          }
        })
      }
    },

    getFileListByJobitemId(jobitemId){
      let _this = this;
      let formData = new window.FormData();
      formData.append('jobitemId', jobitemId);
      formData.append('loginUserId', _this.loginUser.customerUserId);
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/file/rdms/getFileListByJobitemId', formData).then((response) => {
        let resp = response.data;
        if (resp.success) {
          _this.attachmentFileList = resp.content;
          _this.replaceFileList = resp.content;  //初始化替换文件数组
          if(Tool.isNotEmpty_ES5(_this.replaceFileList)){
            _this.replaceFileList.forEach(item => {
              item.replace = false;
            })
          }
        } else {
          Toast.warning(resp.message, Swal);
        }
      })
    },

    setReplaceFiles(file, i){
      let _this = this;
      if(_this.attachmentChecked[i]){
        _this.replaceFileList[i].replace = true;
      }else{
        _this.replaceFileList[i].replace = false;
      }
    },

    onExecutorChange(){
      let _this = this;
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/worker/getWorkerManhourInfo/'+ _this.dialogInfo.executorId).then((response)=>{
        let resp = response.data;
        if (resp.success) {
          _this.workerManhour = resp.content;
        } else {
          Toast.warning(resp.message,Swal);
        }
      })
    },


    getUploadFileList(resFiles){
      let _this = this;
      _this.resFiles = resFiles;
    },

    getCheckedFileList(checkedFileList){
      let _this = this;
      _this.checkedFileList = checkedFileList;
    },

    manhourChange(){
      let _this = this;
      if(_this.dialogInfo && _this.dialogInfo.manhour){
        if(_this.dialogInfo.manhour < 0){
          _this.dialogInfo.manhour =0;
        }
      }
      //MyChart
      _this.executorId = _this.dialogInfo.executorId;

      if(Tool.isNotEmpty(_this.dialogInfo.executorId)){
        let workerManhourFee = 0;
        _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/manhour/getManhourFeeByWorkerId/'+ _this.dialogInfo.executorId).then((response)=>{
          let resp = response.data;
          if (resp.success) {
            workerManhourFee = resp.content;
            _this.hourCost = _this.dialogInfo.manhour * workerManhourFee;
            _this.remainBudget = _this.originRemainBudget - _this.hourCost;
            _this.availableRemainBudget = _this.originSubprojectRemainBudget - _this.hourCost;
          } else {
            Toast.warning(resp.message,Swal);
          }
        })
      } else{
        _this.hourCost = _this.dialogInfo.manhour * _this.stdTestManhourFee;
        _this.remainBudget = _this.originRemainBudget - _this.hourCost;
        _this.availableRemainBudget = _this.originSubprojectRemainBudget - _this.hourCost;
      }
    },

    calSubprojectSelfBudgetActiveExeInfo(subprojectId) {
      let _this = this;
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/sub-project/calSubprojectSelfBudgetActiveExeInfo/' + subprojectId).then((response) => {
        let resp = response.data;
        if (resp.success) {
          _this.subprojectBudgetExeSummary = resp.content;
          _this.originSubprojectRemainBudget = _this.subprojectBudgetExeSummary.sumBudgetApproved - _this.subprojectBudgetExeSummary.sumBudgetActive;
          _this.originRemainBudget = _this.originSubprojectRemainBudget;
        }else{
          Toast.warning(resp.message, Swal);
        }
      });
    },

    /**
     * 保存客户信息
     */
    saveJobItem() {
      let _this = this;
      // 保存校验（公共核心字段 + 科研类型工时特例）
      if (!_this.validateJobCoreFields(_this.dialogInfo, { requireManhour: false })) {
        return;
      }

      if(_this.comIds.rdType !== RD_TYPE.RESEARCH.key){
        if (1 !== 1
            || !Validator.require(_this.dialogInfo.manhour, "工时",Swal)
            || !Validator.length(_this.dialogInfo.manhour, "工时", 1, 4,Swal)
        ) {
          return;
        }
      }else{
        _this.dialogInfo.manhour =0;
      }

      _this.taskJobitem.jobName = _this.dialogInfo.jobName;
      _this.taskJobitem.taskDescription = _this.dialogInfo.taskDescription;
      _this.taskJobitem.executorId = _this.dialogInfo.executorId;
      _this.taskJobitem.manhour = _this.dialogInfo.manhour;
      _this.taskJobitem.planSubmissionTime = _this.dialogInfo.planSubmissionTime;
      _this.taskJobitem.associatedJobitemId = _this.dialogInfo.associatedJobitemId;  //关联任务

      let replaceFileIdList = [];
      if(Tool.isNotEmpty_ES5(_this.replaceFileList)){
        _this.replaceFileList.forEach(item => {
          if(item.replace){
            replaceFileIdList.push(item.id);
          }
        })
      }
      _this.taskJobitem.replaceFileIdsStr = JSON.stringify(replaceFileIdList);  //被替换的文件Id的json字符串

      _this.applyJobSaveMeta(_this.taskJobitem, {
        type: _this.jobType,
        projectType: _this.projectType,
        fileList: _this.resFiles,
      });

      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/job-items/save', _this.taskJobitem).then((response)=>{
        let resp = response.data;
        if (resp.success) {
          _this.taskJobitem = {};
          _this.resFiles = [];
          _this.taskJobitem.id = Uuid.shortUuid(8); //重新生成一个ID
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
      _this.manhourChange();
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
@import './../../../public/static/scss/caipd.scss';

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
