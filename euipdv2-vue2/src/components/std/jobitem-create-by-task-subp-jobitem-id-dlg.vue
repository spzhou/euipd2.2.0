<!--
 * Author: 周朔鹏
 * Date: 2025-02-19
 * Email: 391902958@qq.com
 * Website: https://www.euipd.com
-->

<template>
  <JobitemCreateForm
      :info="jobItem"
      :executors="executors"
      executor-value-key="executorId"
      :name-span="11"
      :name-inner-span="23"
      :show-manhour="jobType!==JOB_ITEM_TYPE.BUG.key && superJobItem.rdType !== RD_TYPE.RESEARCH.key"
      :picker-options="pickerOptions"
      :window-hint="issueWindowHintFor(jobType)"
      :over-load10="overLoad10"
      @executor-change="renderChart"
      @manhour-change="manhourChange"
      @submit="saveJobItem"
      @cancel="close">
    
    <template #chart>
      <el-row v-if="superJobItem.rdType !== RD_TYPE.RESEARCH.key">
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
          <el-form-item :label="((preFix) + '任务名称')">
            <el-col :span="20">
              <div class="text-box">《{{ superJobItem.jobName }}》</div>
            </el-col>
          </el-form-item>
        </el-col>
      </el-row>
    </template>
    <template #manhour-extra>
      <div>
        <div>
          <div style="display: inline-block; "><em>成本概要: 工时成本: {{ hourCost | rmbFormat }}元</em></div>
          <div style="display: inline-block; margin-left: 30px;" :class="[ availableRemainBudget < 0 ? redColor : '']"><em> 预算余额:{{ availableRemainBudget | rmbFormat }} 元</em></div>
        </div>
      </div>
    </template>
    <template #submit-hint>
      <el-col :span="17">
        <div style="margin-left: 80px; line-height: 28px;">
          父级任务计划提交时间为: <span style="margin-left: 5px; font-weight: 500;">{{ planCompleteTimeStr }}</span>, 所有工作应于上述日期之前完成.
        </div>
      </el-col>
    </template>
    <template #below-form>
      <div style="margin: 15px 110px 20px 0">
        <div class="hr hr-10 dotted"></div>
        <FileUploadCore
            :input-id="('jobitem-create-by-jobitem-id-dlg-' + (jobItem.id))"
            :key="('file-upload-' + (jobItem.id))"
            :x-project-id="xProjectId"
            :item-id="jobItem.id"
            :file-group="FILE_GROUPING.JOBITEM.key"
            :project-type="projectType"
            :en-delete="true"
            :read-db-record="true"
            v-on:uploadFileList="getUploadFileList"
        ></FileUploadCore>
      </div>
      
      <div style="width: 100%; display: flex; flex-direction: row; justify-content: end; align-items: center;">
        <small style="margin-right: 80px;"><em>( 操作指引: 发任务的时候, 如果能够为工作执行人提供一些辅助资料, 将有助于提高工作效率. )</em></small>
      </div>
      <div>
        <div class="hr hr-8 hr-dotted"></div>
        <SelectJobitemFileList
            :jobitem-id="jobitemId"
            v-on:checkedFileIdList="getCheckedFileList"
        ></SelectJobitemFileList>
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
  name: "jobitem-create-by-task-subp-jobitem-id-dlg",

  props: {
    jobitemId: {   //superJobitemId
      default: null
    },
    jobType: {
      default: null,
    },
    preFix: {
      default: '',
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
    inheritJobType: {
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

      xProjectId: '',
      jobItem: {
        manhour: 0,
      },
      superJobItem: {},

      executors: [],
      resFiles: [],
      checkedFileIdList: [],

      FILE_GROUPING: FILE_GROUPING,
      JOB_ITEM_TYPE: JOB_ITEM_TYPE,
      PROJECT_TYPE: PROJECT_TYPE,
      ENTRANCE_CLASS: ENTRANCE_CLASS,
      DOC_TYPE: DOC_TYPE,
      RD_TYPE: RD_TYPE,

      hourCost: 0,
      availableRemainBudget: 0,
      originRemainBudget: 0,

      pickerOptions: {
        disabledDate: (v) => this.disabledSubmitDateInWindow(v, this.jobType),
      },

      redColor: 'red-color',
      executorId: '',
      overLoad10: false,
      random: 0,
      planCompleteTime: {},
      planCompleteTimeStr: '',
    };
  },

  computed: {

  },

  mounted: function () {
    let _this = this;
    _this.jobItem.id = Uuid.shortUuid(8);

    _this.getLoginUser();
    if(Tool.isNotEmpty(_this.jobitemId)){
      _this.getIdsDtoByJobitemId(_this.jobitemId);
      _this.getJobItemCreateDlgInfo(_this.jobitemId);
    }
  },

  methods: {


    getJobItemCreateDlgInfo(jobitemId){
      let _this = this;
      if(Tool.isNotEmpty(_this.inheritJobType) && (_this.inheritJobType === JOB_ITEM_TYPE.TASK_SUBP.key)){
        _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/job-items/getJobItemCreateDlgInfo-taskSubpAssist/' + jobitemId).then((response)=>{
          let resp = response.data;
          if (resp.success) {
            _this.superJobItem = resp.content;
            _this.executors = resp.content.executors;
            if(resp.content.budgetExeInfo){
              _this.availableRemainBudget = resp.content.budgetExeInfo.remainBudget;
              _this.originRemainBudget = resp.content.budgetExeInfo.remainBudget;
            }
          } else {
            Toast.warning(resp.message,Swal);
          }
        })
      }
    },

    getIdsDtoByJobitemId(jobitemId){
      let _this = this;
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/job-items/getIdsDtoByJobitemId/' + jobitemId).then((response) => {
        let resp = response.data;
        if (resp.success) {
          _this.comIds = resp.content;
          _this.planCompleteTime = resp.content?.planCompleteTime;
          if(_this.planCompleteTime){
            _this.planCompleteTimeStr = shortDateFormat(_this.planCompleteTime);
          }

          if(Tool.isNotEmpty(_this.comIds.projectId)){
            if(_this.projectType === PROJECT_TYPE.PROJECT.key){
              _this.xProjectId = _this.comIds.projectId;
            }
            _this.jobItem.projectId = _this.comIds.projectId;
            _this.jobItem.projectManagerId = _this.comIds.projectManagerId;
            _this.jobItem.productManagerId = _this.comIds.productManagerId;
          }
          if(Tool.isNotEmpty(_this.comIds.preProjectId)){
            _this.jobItem.preProjectId = _this.comIds.preProjectId;
            _this.jobItem.productManagerId = _this.comIds.productManagerId;
          }
          if(Tool.isNotEmpty(_this.comIds.subprojectId)){
            if(_this.projectType === PROJECT_TYPE.SUB_PROJECT.key){
              _this.xProjectId = _this.comIds.subprojectId;
            }
            _this.jobItem.subprojectId = _this.comIds.subprojectId;
            _this.jobItem.characterId = _this.comIds.characterId;
            _this.jobItem.projectManagerId = _this.comIds.projectManagerId;
            _this.jobItem.productManagerId = _this.comIds.productManagerId;
          }

          _this.listExecutors();
        } else {
          Toast.warning(resp.message, Swal);
        }
      })
    },

    listExecutors(){
      let _this = this;
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/worker/listAllWorkersByProjectId/'+ _this.comIds.projectId).then((response)=>{
        let resp = response.data;
        if (resp.success) {
          _this.executors = resp.content;
        } else {
          Toast.warning(resp.message,Swal);
        }
      })
    },

    getUploadFileList(resFiles){
      let _this = this;
      _this.resFiles = resFiles;
    },

    getCheckedFileList(checkedFileIdList){
      let _this = this;
      _this.checkedFileIdList = checkedFileIdList;
    },

    manhourChange(){
      let _this = this;
      if(_this.jobItem && _this.jobItem.manhour){
        if(_this.jobItem.manhour < 0){
          _this.jobItem.manhour =0;
        }
      }

      if(Tool.isNotEmpty(_this.jobItem.executorId)){
        let workerManhourFee = 0;
        _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/manhour/getManhourFeeByWorkerId/'+ _this.jobItem.executorId).then((response)=>{
          let resp = response.data;
          if (resp.success) {
            workerManhourFee = resp.content;
            _this.hourCost = _this.jobItem.manhour * workerManhourFee;
            _this.availableRemainBudget = _this.originRemainBudget - _this.hourCost;
          } else {
            Toast.warning(resp.message,Swal);
          }
        })
      }
    },

    /**
     * 保存客户信息
     */
    saveJobItem() {
      let _this = this;
      if(_this.jobType === JOB_ITEM_TYPE.BUG.key){
        _this.jobItem.manhour = 0;
      }
      // 保存校验
      if (1 !== 1
          || !Validator.require(_this.jobItem.jobName, "任务名称",Swal)
          || !Validator.require(_this.jobItem.executorId, "工作执行人",Swal)  /*任务的执行人,在任务流转全周期保持不变; 这与任务Process的执行人概念不同*/
          || !Validator.require(_this.jobItem.taskDescription, "任务描述",Swal)
          || !Validator.require(_this.jobItem.manhour, "工时",Swal)
          || !Validator.require(_this.jobItem.planSubmissionTime, "提交时间",Swal)

          || !Validator.length(_this.jobItem.jobName, "任务名称", 5, 50,Swal)
          || !Validator.length(_this.jobItem.taskDescription, "任务描述", 10, 500,Swal)
          || !Validator.length(_this.jobItem.manhour, "工时", 1, 4,Swal)
      ) {
        return;
      }

      _this.jobItem.customerId = _this.loginCustomer.id;

      if(_this.jobType === JOB_ITEM_TYPE.ASSIST.key){
        _this.jobItem.parentJobitemId = _this.jobitemId;
      }
      if(_this.jobType === JOB_ITEM_TYPE.TEST.key){
        _this.jobItem.testedJobitemId = _this.jobitemId;
      }
      if(_this.jobType === JOB_ITEM_TYPE.BUG.key){
        _this.jobItem.parentJobitemId = _this.jobitemId;
      }
      _this.jobItem.checkedFileIdList = _this.checkedFileIdList;  //来自子组件
      _this.jobItem.fileList = _this.resFiles;  //来自子组件

      _this.jobItem.type = _this.jobType;    //注意
      _this.jobItem.auxType = _this.inheritJobType;    //继承类型
      _this.jobItem.projectType = _this.projectType;
      _this.jobItem.loginUserId = _this.loginUser.customerUserId;
      _this.jobItem.qualityId = _this.superJobItem.qualityId;
      // 已经对文件权限进行处理
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/job-items/save', _this.jobItem).then((response)=>{
        let resp = response.data;
        if (resp.success) {
          _this.jobItem = {};
          _this.resFiles = [];
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
      _this.manhourChange();
    },

    getOverLoad(value){
      let _this = this;
      console.log("overload:", value);
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
