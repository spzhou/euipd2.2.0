<!--
 * Author: 周朔鹏
 * Date: 2025-01-26
 * Email: 391902958@qq.com
 * Website: https://www.euipd.com
-->

<template>
  <JobitemCreateForm
      :info="dialogInfo"
      :executors="executors"
      :picker-options="pickerOptions_submit"
      :over-load10="overLoad10"
      submit-text="添加计划"
      :submit-disabled="!idsReady || saving"
      @manhour-change="manhourChange"
      @submit="saveJobItem"
      @cancel="close">
    <template #time-prefix>
      <el-col :span="7">
        <el-form-item label="开始时间" required>
          <el-date-picker
              type="date"
              placeholder="选择日期"
              :picker-options="pickerOptions_start"
              v-model="dialogInfo.planStartTime"
              @change="startDateChange"
          >
          </el-date-picker>
        </el-form-item>
      </el-col>
    </template>
    <template #in-form>
      <el-row>
        <el-col :span="22" class="plan-hint-row">
          <div class="plan-start-hint">计划开始日须晚于今天，当日任务请直接下发</div>
          <div v-if="loadPreview" class="plan-load-preview" :class="{ 'is-overload': loadPreview.overload }">
            计划区间 {{ loadPreview.workDays }} 个工作日，执行人日均负荷约 {{ loadPreview.averageDayLoad }} 小时
            （在办工单 {{ loadPreview.jobCount }} 条，其他计划 {{ loadPreview.planCount }} 条）<span v-if="loadPreview.overload">，超过每日上限，下发时可能因超负荷失败</span>
          </div>
        </el-col>
      </el-row>
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
    </template>
  </JobitemCreateForm>
</template>

<script>
import FileRdms from "../../components/rdms/file-rdms";
import FileUploadCore from "@/components/std/file-upload-core.vue";
import SelectJobitemFileList from "@/components/std/select-jobitem-file-list.vue";
import Swal from "sweetalert2";

import JobitemCreateForm from "@/components/std/jobitem-create-form.vue";
import jobitemCreateDlgMixin from "@/mixins/jobitemCreateDlgMixin";

export default {
  mixins: [jobitemCreateDlgMixin],
  components: {
    JobitemCreateForm,
    FileRdms,
    FileUploadCore,
    SelectJobitemFileList,
  },
  name: "jobitem-create-by-project-id-dlg-gantt",

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
      availableRemainBudget: 0,

      remainBudget: 0,
      stdTestManhourFee: 0,

      subprojectBudgetExeSummary: {},

      idsReady: false,
      saving: false,

      pickerOptions_start: {
        disabledDate: (v) => this.disabledDateBeforeTomorrow(v),
      },

      pickerOptions_submit: {
        disabledDate: (v) => this.disabledPlanSubmitDate(v, this.dialogInfo.planStartTime, this.dialogInfo.manhour),
      },

      loadPreview: null,
      loadPreviewSeq: 0,
      loadPreviewTimer: null,

      redColor: 'red-color',
      overLoad10: false,
      random: 0,

      comIds: {},
    };
  },

  computed: {
    loadPreviewKey() {
      const info = this.dialogInfo;
      return [info.executorId, this.formatLocalYmd(info.planStartTime),
        this.formatLocalYmd(info.planSubmissionTime), info.manhour].join('|');
    },
  },

  watch: {
    loadPreviewKey() {
      if (this.loadPreviewTimer) {
        clearTimeout(this.loadPreviewTimer);
      }
      this.loadPreviewTimer = setTimeout(() => {
        this.fetchPlanLoadPreview(Object.assign({}, this.dialogInfo, { id: this.taskJobitem.id }));
      }, 300);
    },
  },

  beforeDestroy() {
    if (this.loadPreviewTimer) {
      clearTimeout(this.loadPreviewTimer);
    }
  },

  mounted: function () {
    let _this = this;
    _this.taskJobitem.id = Uuid.shortUuid(8);

    _this.getLoginUser();
    if(Tool.isNotEmpty(_this.xProjectId) && Tool.isNotEmpty(_this.projectType)){
      _this.getIdsDtoByProjectIdAndType(_this.projectType, _this.xProjectId);

    }

    _this.listExecutors();

  },

  methods: {
    startDateChange() {
      this.alignPlanSubmit(this.dialogInfo);
    },

    manhourChange() {
      this.alignPlanSubmit(this.dialogInfo);
    },

    getIdsDtoByProjectIdAndType(projectType, xProjectId){
      let _this = this;
      _this.idsReady = false;
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/project/getIdsDtoByProjectIdAndType', {
        anyProjectId: xProjectId,
        projectType: projectType,
      }).then((response) => {
        let resp = response.data;
        if (resp.success) {
          _this.comIds = resp.content;
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
          _this.idsReady = !!( _this.taskJobitem.projectId && _this.taskJobitem.subprojectId && _this.taskJobitem.projectManagerId );
          if (!_this.idsReady) {
            Toast.warning('项目信息未就绪，请稍后重试', Swal);
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

    /**
     * 保存客户信息
     */
    saveJobItem() {
      let _this = this;
      if (!_this.idsReady || _this.saving) {
        Toast.warning('项目信息加载中，请稍候再试', Swal);
        return;
      }
      // 保存校验
      if (!_this.validateJobCoreFields(_this.dialogInfo)
          || !Validator.require(_this.dialogInfo.planStartTime, "开始时间", Swal)) {
        return;
      }

      _this.taskJobitem.jobName = _this.dialogInfo.jobName;
      _this.taskJobitem.taskDescription = _this.dialogInfo.taskDescription;
      _this.taskJobitem.executorId = _this.dialogInfo.executorId;
      _this.taskJobitem.manhour = _this.dialogInfo.manhour;
      _this.taskJobitem.planStartTime = _this.formatLocalYmd(_this.dialogInfo.planStartTime);
      _this.taskJobitem.planSubmissionTime = _this.formatLocalYmd(_this.dialogInfo.planSubmissionTime);

      _this.applyJobSaveMeta(_this.taskJobitem, {
        type: _this.jobType,
        projectType: _this.projectType,
        fileList: _this.resFiles,
      });

      _this.saving = true;
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/gantt/saveTaskPlanJobitem', _this.taskJobitem).then((response)=>{
        _this.saving = false;
        let resp = response.data;
        if (resp.success) {
          _this.taskJobitem = {};
          _this.resFiles = [];
          _this.taskJobitem.id = Uuid.shortUuid(8); //重新生成一个ID
          _this.close();
          _this.$bus.publish({ areas: _this.$bus.areasForEntrance(_this.entrance), reason: 'jobitem-create' });
          _this.$bus.$emit('refresh-gantt-list', true);
          Toast.success("计划已添加，到开始日自动下发", Swal);
        } else {
          Toast.warning(resp.message, Swal);
        }
      }).catch(() => {
        _this.saving = false;
        Toast.warning('保存失败', Swal);
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
.plan-hint-row {
  padding-left: 130px;
}
.plan-start-hint,
.plan-load-preview {
  font-size: 12px;
  line-height: 1.5;
  color: #909399;
  padding-top: 4px;
}
.plan-load-preview.is-overload {
  color: #f56c6c;
}
</style>
