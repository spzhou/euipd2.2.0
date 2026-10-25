<!--
 * Author: 周朔鹏
 * Date: 2025-02-27
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
      :show-manhour="superJobItem.rdType !== RD_TYPE.RESEARCH.key && jobType !== JOB_ITEM_TYPE.BUG.key"
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
        <div v-if="projectType === PROJECT_TYPE.SUB_PROJECT.key">
          <div v-if="jobType===JOB_ITEM_TYPE.ASSIST.key || (jobType===JOB_ITEM_TYPE.TEST.key && (inheritJobType===JOB_ITEM_TYPE.CHARACTER_INT.key ))">
            <div style="display: inline-block; "><em>成本概要: 工时成本: {{ hourCost | rmbFormat }}元</em></div>
            <div style="display: inline-block; margin-left: 30px;" :class="[ availableRemainBudget < 0 ? redColor : '']"><em> 预算余额:{{ availableRemainBudget | rmbFormat }} 元</em></div>
          </div>
          <div v-else-if="jobType===JOB_ITEM_TYPE.ASSIST.key || (jobType===JOB_ITEM_TYPE.TEST.key && (inheritJobType===JOB_ITEM_TYPE.TASK_SUBP.key || inheritJobType===JOB_ITEM_TYPE.QUALITY.key))">
            <div style="display: inline-block; "><em>成本概要: 工时成本: {{ hourCost | rmbFormat }}元</em></div>
          </div>
          <div v-else>
            <div style="display: inline-block; "><em>成本概要: 工时成本: {{ hourCost | rmbFormat }}元</em></div>
            <div style="display: inline-block; margin-left: 30px;" :class="[ remainBudget < 0 ? redColor : '']"><em> 特性测试预算余额:{{ remainBudget | rmbFormat }} 元</em></div>
            <div style="display: inline-block; margin-left: 30px;" :class="[ availableRemainBudget < 0 ? redColor : '']"><em> 功能预算余额:{{ availableRemainBudget | rmbFormat }} 元</em></div>
          </div>
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
            :read-db-record="true"
            :en-delete="true"
            v-on:uploadFileList="getUploadFileList"
        ></FileUploadCore>
      </div>
      
      <div style="width: 100%; display: flex; flex-direction: row; justify-content: end; align-items: center;">
        <small style="margin-right: 80px;"><em>( 操作指引: 发任务的时候, 如果能够为工作执行人提供一些辅助资料, 将有助于提高工作效率. )</em></small>
      </div>
      <div v-if="inheritJobType !== JOB_ITEM_TYPE.QUALITY.key">
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
  name: "jobitem-create-by-jobitem-id-dlg",

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

      parentManhourInfo: {},
      remainFee: 0,

      hourCost: 0,

      budget: 0,
      originCharacterRemainBudget: 0,
      availableRemainBudget: 0,

      remainBudget: 0,
      originRemainBudget: 0,
      stdTestManhourFee: 0,

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
      let requestPath = '';
      if(Tool.isNotEmpty(_this.inheritJobType) && (_this.inheritJobType === JOB_ITEM_TYPE.CHARACTER_INT.key || _this.inheritJobType === JOB_ITEM_TYPE.TASK_SUBP.key))
      {
        if(_this.jobType===JOB_ITEM_TYPE.ASSIST.key){
          requestPath = '/customer/job-items/getJobItemCreateDlgInfo-assist/';  //集成的协作任务, 走集成任务费用
        }else if(_this.jobType===JOB_ITEM_TYPE.TEST.key) {
          requestPath = '/customer/job-items/getJobItemCreateDlgInfo-assist-test/';  //集成协作任务的测试, 走集成任务费用; //任务任务的测试任务, 走任务任务费用
        }else{
          requestPath = '/customer/job-items/getJobItemCreateDlgInfo-develop/';   //集成测试任务的集成类型是develop, 所以走这一条路径
        }
      }else if(Tool.isNotEmpty(_this.inheritJobType) && (_this.inheritJobType === JOB_ITEM_TYPE.QUALITY.key )){
        if(_this.jobType===JOB_ITEM_TYPE.ASSIST.key){
          requestPath = '/customer/job-items/getJobItemCreateDlgInfo-quality-assist/';
        }else if(_this.jobType===JOB_ITEM_TYPE.TEST.key) {
          requestPath = '/customer/job-items/getJobItemCreateDlgInfo-assist-test/';
        }
      }else if(Tool.isNotEmpty(_this.inheritJobType) && (_this.inheritJobType === JOB_ITEM_TYPE.TASK_BUG.key )){
        if(_this.jobType===JOB_ITEM_TYPE.ASSIST.key){
          requestPath = '/customer/job-items/getJobItemCreateDlgInfo-quality-assist/';  //处理是一致的
        }
      }else if(Tool.isNotEmpty(_this.inheritJobType) && (_this.inheritJobType === JOB_ITEM_TYPE.TASK_FUNCTION.key )){
        if(_this.jobType===JOB_ITEM_TYPE.ASSIST.key){
          requestPath = '/customer/job-items/getJobItemCreateDlgInfo-function-assist/';
        }else if(_this.jobType===JOB_ITEM_TYPE.TEST.key) {
          requestPath = '/customer/job-items/getJobItemCreateDlgInfo-function-assist-test/';
        }
      }else{
        requestPath = '/customer/job-items/getJobItemCreateDlgInfo-develop/';
      }

      _this.$ajax.post(process.env.VUE_APP_SERVER + requestPath + jobitemId).then((response)=>{
        let resp = response.data;
        if (resp.success) {
          _this.superJobItem = resp.content;
          _this.executors = resp.content.executors;
          if(resp.content.budgetExeInfo){
            _this.originRemainBudget = resp.content.budgetExeInfo.testManhourFeeRemain;
            _this.remainBudget = resp.content.budgetExeInfo.testManhourFeeRemain;
            _this.stdTestManhourFee = resp.content.budgetExeInfo.standTestManhourFee;
            _this.availableRemainBudget = resp.content.budgetExeInfo.remainBudget;
            _this.originCharacterRemainBudget = resp.content.budgetExeInfo.remainBudget;
            _this.budget = resp.content.budgetExeInfo.budget;
          }

        } else {
          Toast.warning(resp.message,Swal);
        }
      })
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

        } else {
          Toast.warning(resp.message, Swal);
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
            _this.remainBudget = _this.originRemainBudget - _this.hourCost;
            _this.availableRemainBudget = _this.originCharacterRemainBudget - _this.hourCost;
          } else {
            Toast.warning(resp.message,Swal);
          }
        })
      } else{
        _this.hourCost = _this.jobItem.manhour * _this.stdTestManhourFee;
        _this.remainBudget = _this.originRemainBudget - _this.hourCost;
        _this.availableRemainBudget = _this.originCharacterRemainBudget - _this.hourCost;
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
          || !Validator.require(_this.jobItem.planSubmissionTime, "提交时间",Swal)

          || !Validator.length(_this.jobItem.jobName, "任务名称", 5, 50,Swal)
          || !Validator.length(_this.jobItem.taskDescription, "任务描述", 10, 500,Swal)
      ) {
        return;
      }

      if(_this.superJobItem.rdType !== RD_TYPE.RESEARCH.key){
        // 保存校验
        if (1 !== 1
            || !Validator.require(_this.jobItem.manhour, "工时",Swal)
            || !Validator.length(_this.jobItem.manhour, "工时", 1, 4,Swal)
        ) {
          return;
        }
      }else{
        _this.jobItem.manhour =0;
      }

      _this.jobItem.customerId = _this.loginCustomer.id;

      if(_this.jobType === JOB_ITEM_TYPE.ASSIST.key){
        _this.jobItem.parentJobitemId = _this.jobitemId;
        _this.jobItem.qualityId = _this.superJobItem.qualityId;  //如果存在,则被继承
        _this.jobItem.testManagerId = _this.superJobItem.testManagerId;  //如果存在,则被继承
        _this.jobItem.projectManagerId = _this.superJobItem.projectManagerId;  //如果存在,则被继承
        _this.jobItem.productManagerId = _this.superJobItem.productManagerId;  //如果存在,则被继承
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
