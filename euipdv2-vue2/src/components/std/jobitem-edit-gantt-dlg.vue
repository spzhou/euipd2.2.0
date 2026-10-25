<!--
 * Author: 周朔鹏
 * Date: 2025-01-26
 * Email: 391902958@qq.com
 * Website: https://www.euipd.com
-->

<template>
  <div>
    <div v-if="jobitemGanttItem" class="dialog-container">
      <el-form
          label-width="130px"
          :show-message=false
          :disabled="!editable"
          class="dialog-width">
        <el-row>
          <el-col :span="22">
            <el-form-item label="计划状态">
              <el-tag size="small" :type="statusTagType">{{ statusText }}</el-tag>
              <span v-if="isFailed && jobitemGanttItem.failReason" class="plan-fail-reason">
                失败原因：{{ jobitemGanttItem.failReason }}
              </span>
              <span v-if="!statusEditable" class="plan-hint">该计划{{ statusText }}，不能在甘特图中修改</span>
              <span v-else-if="!canEdit" class="plan-hint">只有计划创建人、子项目经理与主项目经理可以修改该计划</span>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="12">
            <el-form-item label="任务名称" required>
              <el-col :span="22">
                <el-input v-model="jobitemGanttItem.jobName "></el-input>
              </el-col>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="6">
            <el-form-item label="任务执行人" required>
              <el-col :span="24">
                <el-select v-model="jobitemGanttItem.executorId" filterable placeholder="任务执行人">
                  <el-option v-for="o in executors" :key="o.id" :value="o.id" :label="o.trueName"></el-option>
                </el-select>
              </el-col>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="22">
            <el-form-item label="任务描述" required>
              <el-input type="textarea" rows="5" v-model="jobitemGanttItem.taskDescription"></el-input>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row style="margin-top: 5px;">
          <el-col :span="22">
            <el-form-item label="任务工时" required>
              <el-col :span="24">
                <el-form-item required>
                  <div style="display: flex; flex-direction: row;">
                    <el-input style="width: 100px;" type="number" v-model="jobitemGanttItem.manhour" @change="manhourChange"></el-input>
                    <div style="width: 70px; margin-left: 5px;"> 小时</div>
                  </div>
                </el-form-item>
              </el-col>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="7">
            <el-form-item label="开始时间" required>
              <el-date-picker
                  type="date"
                  placeholder="选择日期"
                  :picker-options="pickerOptions_start"
                  v-model="jobitemGanttItem.planStartTime"
                  @change="startDateChange"
              >
              </el-date-picker>
            </el-form-item>
          </el-col>
          <el-col :span="7">
            <el-form-item label="提交时间" required>
              <el-date-picker
                  type="date"
                  placeholder="选择日期"
                  :picker-options="pickerOptions_submit"
                  v-model="jobitemGanttItem.planSubmissionTime">
              </el-date-picker>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="22" class="plan-hint-row">
            <div class="plan-hint">计划开始日须晚于今天；开始日不变时可直接保存或立即下发</div>
            <div v-if="loadPreview" class="plan-hint" :class="{ 'is-overload': loadPreview.overload }">
              计划区间 {{ loadPreview.workDays }} 个工作日，执行人日均负荷约 {{ loadPreview.averageDayLoad }} 小时
              （在办工单 {{ loadPreview.jobCount }} 条，其他计划 {{ loadPreview.planCount }} 条）<span v-if="loadPreview.overload">，超过每日上限，下发时可能因超负荷失败</span>
            </div>
          </el-col>
        </el-row>
      </el-form>

      <div style="margin: 15px 110px 20px 0">
        <div class="hr hr-10 dotted"></div>
        <FileUploadCore
            :input-id="('jobitem-create-by-project-id-' + (jobitemGanttItem.id))"
            :key="('file-upload-' + (jobitemGanttItem.id))"
            :x-project-id="jobitemGanttItem.subprojectId"
            :item-id="jobitemGanttItem.id"
            :file-group="FILE_GROUPING.JOBITEM.key"
            :project-type="projectType"
            :read-db-record="true"
            :en-delete="true"
            v-on:uploadFileList="getUploadFileList"
        ></FileUploadCore>
      </div>
      <div class="clearfix form-actions">
        <div style="display: flex; justify-content: center; gap: 8px; flex-wrap: wrap;">
          <template v-if="editable">
            <button type="button" v-on:click="saveJobItem()" class="ca-btn-primary">修改计划</button>
            <button type="button" v-on:click="issueNow()" class="ca-btn-primary" style="background:#e6a23c;border-color:#e6a23c;">立即下发</button>
            <button type="button" v-on:click="deletePlan()" class="ca-btn-primary" style="background:#f56c6c;border-color:#f56c6c;">删除计划</button>
          </template>
          <button type="button" v-on:click="close()" class="ca-btn-primary">取消</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import FileRdms from "../../components/rdms/file-rdms";
import FileUploadCore from "@/components/std/file-upload-core.vue";
import SelectJobitemFileList from "@/components/std/select-jobitem-file-list.vue";
import Swal from "sweetalert2";
import {Loading} from "element-ui";

import jobitemCreateDlgMixin from "@/mixins/jobitemCreateDlgMixin";
import {optionKV} from "@/filter/filter";

export default {
  mixins: [jobitemCreateDlgMixin],
  components: {
    FileRdms,
    FileUploadCore,
    SelectJobitemFileList,
  },
  name: "jobitem-edit-gantt-dlg",

  props: {
    taskId: {
      default: null,
    },
    projectType: {
      default: null,
    },
    canEdit: {
      type: Boolean,
      default: true,
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

      jobitemGanttItem: {},
      executors: [],
      resFiles: [],
      JOB_ITEM_TYPE: JOB_ITEM_TYPE,

      FILE_GROUPING: FILE_GROUPING,

      saving: false,
      acting: false,

      pickerOptions_start: {
        disabledDate: (v) => this.disabledDateBeforeTomorrow(v),
      },

      pickerOptions_submit: {
        disabledDate: (v) => this.disabledPlanSubmitDate(v, this.jobitemGanttItem.planStartTime, this.jobitemGanttItem.manhour),
      },

      loadPreview: null,
      loadPreviewSeq: 0,
      loadPreviewTimer: null,
    };
  },

  computed: {
    planStatus() {
      return (this.jobitemGanttItem && this.jobitemGanttItem.status) || 'PLAN';
    },
    isFailed() {
      return this.planStatus === 'FAILED';
    },
    statusEditable() {
      return this.planStatus === 'PLAN' || this.planStatus === 'FAILED';
    },
    editable() {
      return this.canEdit && this.statusEditable;
    },
    statusText() {
      return optionKV(GANTT_PLAN_STATUS, this.planStatus) || this.planStatus;
    },
    statusTagType() {
      return { PLAN: 'warning', FAILED: 'danger', ISSUED: 'success', CANCEL: 'info' }[this.planStatus] || 'info';
    },
    loadPreviewKey() {
      const info = this.jobitemGanttItem || {};
      return [info.executorId, this.formatLocalYmd(info.planStartTime),
        this.formatLocalYmd(info.planSubmissionTime), info.manhour].join('|');
    },
  },

  watch: {
    loadPreviewKey() {
      if (!this.editable) {
        return;
      }
      if (this.loadPreviewTimer) {
        clearTimeout(this.loadPreviewTimer);
      }
      this.loadPreviewTimer = setTimeout(() => this.fetchPlanLoadPreview(this.jobitemGanttItem), 300);
    },
  },

  beforeDestroy() {
    if (this.loadPreviewTimer) {
      clearTimeout(this.loadPreviewTimer);
    }
  },

  mounted: function () {
    let _this = this;
    _this.getLoginUser();
    _this.getJobItemGanttById(this.taskId);

    _this.listExecutors();

  },

  methods: {
    startDateChange() {
      this.alignPlanSubmit(this.jobitemGanttItem);
    },

    manhourChange() {
      this.alignPlanSubmit(this.jobitemGanttItem);
    },

    getJobItemGanttById(jobitemGanttId) {
      let loadingInstance = Loading.service({ fullscreen: true });
      this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/gantt/getJobItemGanttById/'+ jobitemGanttId).then((response) => {
        loadingInstance.close();
        let resp = response.data;
        if (resp.success) {
          this.jobitemGanttItem = resp.content;
          if(this.jobitemGanttItem === null){
            this.close();
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
      if (_this.saving || _this.acting) {
        return;
      }
      // 保存校验
      if (!_this.validateJobCoreFields(_this.jobitemGanttItem)
          || !Validator.require(_this.jobitemGanttItem.planStartTime, "开始时间", Swal)) {
        return;
      }

      _this.jobitemGanttItem.customerId = _this.jobitemGanttItem.customerId || (_this.loginCustomer && _this.loginCustomer.id);
      _this.jobitemGanttItem.projectType = _this.projectType || _this.jobitemGanttItem.projectType;
      _this.jobitemGanttItem.type = _this.jobitemGanttItem.type || (_this.JOB_ITEM_TYPE && _this.JOB_ITEM_TYPE.TASK_SUBP ? _this.JOB_ITEM_TYPE.TASK_SUBP.key : _this.jobitemGanttItem.type);
      _this.jobitemGanttItem.planStartTime = _this.formatLocalYmd(_this.jobitemGanttItem.planStartTime);
      _this.jobitemGanttItem.planSubmissionTime = _this.formatLocalYmd(_this.jobitemGanttItem.planSubmissionTime);
      _this.applyJobSaveMeta(_this.jobitemGanttItem, { fileList: _this.resFiles });
      _this.saving = true;
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/gantt/saveTaskPlanJobitem', _this.jobitemGanttItem).then((response)=>{
        _this.saving = false;
        let resp = response.data;
        if (resp.success) {
          _this.close();
          this.$bus.$emit('refresh-gantt-list', true);
          Toast.success("计划已经修改", Swal);
        } else {
          Toast.warning(resp.message, Swal);
        }
      }).catch(() => {
        _this.saving = false;
        Toast.warning('保存失败', Swal);
      })
    },

    issueNow() {
      let _this = this;
      if (!_this.jobitemGanttItem || !_this.jobitemGanttItem.id || _this.acting) {
        return;
      }
      Swal.fire({
        title: '立即下发',
        text: '将立即下发该计划任务（不校验开始日期），是否继续？',
        icon: 'question',
        showCancelButton: true,
        confirmButtonColor: '#e6a23c',
        cancelButtonColor: '#909399',
        confirmButtonText: '确认下发',
        cancelButtonText: '取消',
      }).then((result) => {
        if (!result.isConfirmed) {
          return;
        }
        _this.acting = true;
        let loadingInstance = Loading.service({ fullscreen: true });
        _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/gantt/issuePlanTaskNow/' + _this.jobitemGanttItem.id).then((response) => {
          loadingInstance.close();
          _this.acting = false;
          let resp = response.data;
          if (resp.success) {
            _this.close();
            _this.$bus.$emit('refresh-gantt-list', true);
            Toast.success("计划已下发", Swal);
          } else {
            Toast.warning(resp.message, Swal);
          }
        }).catch(() => {
          loadingInstance.close();
          _this.acting = false;
          Toast.warning("下发失败", Swal);
        });
      });
    },

    deletePlan() {
      let _this = this;
      if (!_this.jobitemGanttItem || !_this.jobitemGanttItem.id || _this.acting) {
        return;
      }
      Swal.fire({
        title: '删除计划',
        text: '删除后不可恢复，确认删除该计划任务？',
        icon: 'warning',
        showCancelButton: true,
        confirmButtonColor: '#f56c6c',
        cancelButtonColor: '#909399',
        confirmButtonText: '确认删除',
        cancelButtonText: '取消',
      }).then((result) => {
        if (!result.isConfirmed) {
          return;
        }
        _this.acting = true;
        let loadingInstance = Loading.service({ fullscreen: true });
        _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/gantt/deletePlanJobitem/' + _this.jobitemGanttItem.id).then((response) => {
          loadingInstance.close();
          _this.acting = false;
          let resp = response.data;
          if (resp.success) {
            _this.close();
            _this.$bus.$emit('refresh-gantt-list', true);
            Toast.success("计划已删除", Swal);
          } else {
            Toast.warning(resp.message, Swal);
          }
        }).catch(() => {
          loadingInstance.close();
          _this.acting = false;
          Toast.warning("删除失败", Swal);
        });
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
.dialog-width{
  width: 100%;
}
.red-color{
  color: red;
}
.plan-hint-row {
  padding-left: 122px;
}
.plan-hint {
  margin-left: 8px;
  font-size: 12px;
  line-height: 1.5;
  color: #909399;
}
.plan-hint.is-overload,
.plan-fail-reason {
  margin-left: 8px;
  font-size: 12px;
  color: #f56c6c;
}
</style>
