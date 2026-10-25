<!--
 * Author: 周朔鹏
 * Date: 2025-03-04
 * Email: 391902958@qq.com
 * Website: https://www.euipd.com
-->

<template>
  <div class="paper-height" style="overflow: scroll">
    <div class="a4-paper-big-simple">
      <div>
        <div style="background-color: rgba(64,158,255,0.04); margin-bottom: 20px;">
          <div style="font-size: 26px; text-align: center; padding-top: 60px; padding-bottom: 17px;">
            请对被拒收的任务进行处理
          </div>
          <div style="font-size: 15px; text-align: center; padding-bottom: 10px;">
            请根据实际情况调整任务的 执行人 工时 或者 工作的内容 并重新下发任务
          </div>
        </div>
        <el-form
            :model="jobItem"
            label-width="130px"
            :show-message=false
            class="dialog-width">
          <el-row>
            <el-col :span="23">
              <el-form-item label="任务号">
                {{ jobItem.jobSerial }}
              </el-form-item>
            </el-col>
          </el-row>
          <el-row>
            <el-col :span="11">
              <el-form-item label="任务名称" required>
                <el-col :span="23">
                  <el-input v-model="jobItem.jobName"></el-input>
                </el-col>
              </el-form-item>
            </el-col>
            <el-col :span="6">
              <el-form-item label="任务执行人" required>
                <el-col :span="24">
                  <el-select v-model="jobItem.executorId" filterable @change="onExecutorChange" placeholder="任务执行人">
                    <el-option v-for="o in executors" :key="o.id" :value="o.id" :label="o.trueName"></el-option>
                  </el-select>
                </el-col>
              </el-form-item>
            </el-col>
          </el-row>
          <el-row>
            <el-col :span="23">
              <el-form-item label="任务描述" required>
                <el-input type="textarea" rows="5" v-model="jobItem.taskDescription"></el-input>
              </el-form-item>
            </el-col>
          </el-row>
          <el-row style="margin-top: 5px;">
            <el-col :span="11">
              <el-form-item label="任务工时" required>
                <el-col :span="22">
                  <el-form-item required>
                    <div style="display: flex; flex-direction: row;">
                      <el-input style="width: 100px;" type="number" v-model="jobItem.manhour"></el-input>
                      <div style="width: 70px; margin-left: 5px;"> 小时</div>
<!--                      <div v-if="projectType !== PROJECT_TYPE.PRE_PROJECT.key" style="width: 160px;">
                        总工时预算:{{ 'TODO' }} 元
                      </div>-->
                    </div>
                  </el-form-item>
                </el-col>
              </el-form-item>
            </el-col>
            <el-col :span="7">
              <el-form-item label="提交时间" required>
                <el-date-picker
                    type="date"
                    placeholder="选择日期"
                    :picker-options="pickerOptions"
                    v-model="jobItem.planSubmissionTime">
                </el-date-picker>
              </el-form-item>
            </el-col>
          </el-row>
        </el-form>
        <div style="margin: 15px 110px 20px 0">
          <div class="hr hr-18 dotted hr-double"></div>
          <FileUploadCore
              :input-id="('jobitem-re-issue-dlg-' + (jobItem.id))"
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
      </div>
      <div>
        <div class="clearfix form-actions">
          <div style="display: flex; justify-content: center;">
            <button type="button" v-on:click="saveJobItem()" class="ca-btn-primary">重发任务</button>
            <button type="button" v-on:click="cancelJobitem()" class="ca-btn-primary">取消任务</button>
          </div>
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

export default {
  components: {
    FileRdms,
    FileUploadCore,
    SelectJobitemFileList,
  },
  name: "jobitem-re-issue-page",

  props: {
    jobitemId: {
      default: null
    },
    index: {
      default: null,
    },
    entrance: {
      default: null,
    },
  },

  data() {
    return {
      loginUser: {},
      loginCustomer: {},

      jobType: {},
      projectType: {},

      jobItem: {},

      executors: [],
      resFiles: [],
      resFileIds: [],

      FILE_GROUPING: FILE_GROUPING,
      JOB_ITEM_TYPE: JOB_ITEM_TYPE,
      PROJECT_TYPE: PROJECT_TYPE,
      ENTRANCE_CLASS: ENTRANCE_CLASS,
      DOC_TYPE: DOC_TYPE,

      parentManhourInfo: {},
      remainFee: 0,
      workerManhour: 0,

      pickerOptions: {
        disabledDate(v) {
          return v.getTime() < new Date().getTime() - 86400000;//  - 86400000是否包括当天
        }
      },
    };
  },

  computed: {
    getXProjectId() {
      let _this = this;
      if (_this.projectType === PROJECT_TYPE.PRE_PROJECT.key) {
        return _this.jobItem.preProjectId;
      }
      if (_this.projectType === PROJECT_TYPE.PROJECT.key) {
        return _this.jobItem.projectId;
      }
      if (_this.projectType === PROJECT_TYPE.SUB_PROJECT.key) {
        return _this.jobItem.subprojectId;
      }
      return 0;
    },
  },

  mounted: function () {
    let _this = this;
    _this.getLoginUser();
    if (Tool.isNotEmpty(_this.jobitemId)) {
      _this.listExecutors();
      _this.getJobitemSimpleInfo(_this.jobitemId);
    }
  },

  methods: {
    getLoginUser() {
      let _this = this;
      if (Tool.getLoginUser().userGroup === LOGIN_USER_GROUP.CUSTOMER.key) {
        _this.loginUser = Tool.getLoginUser().adminUser;
        _this.loginCustomer = Tool.getLoginUser();
      } else {
        _this.loginUser = Tool.getLoginUser();
        _this.loginCustomer = Tool.getLoginUser().customer;
      }
    },

    getUploadFileList(resFiles) {
      let _this = this;
      _this.resFiles = resFiles;
      if (_this.resFiles !== null) {
        _this.resFileIds = [];
        if(resFiles && resFiles.length>0){
        resFiles.forEach(item => {
          _this.resFileIds.push(item.id);
        })
      }
      }
    },

    getJobitemSimpleInfo(jobitemId) {
      let _this = this;
      let formData = new window.FormData();
      formData.append('jobitemId', jobitemId);
      formData.append('loginUserId', _this.loginUser.customerUserId);
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/job-items/getJobitemSimpleInfo', formData).then((response) => {
        let resp = response.data;
        if (resp.success) {
          _this.jobItem = resp.content;
          _this.jobType = _this.jobItem.type;
          _this.projectType = _this.jobItem.projectType;
        } else {
          Toast.warning(resp.message, Swal);
        }
      });
    },

    listExecutors() {
      let _this = this;
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/worker/listAllWorkers/' + _this.loginCustomer.id).then((response) => {
        let resp = response.data;
        if (resp.success) {
          _this.executors = resp.content;
        } else {
          Toast.warning(resp.message, Swal);
        }
      })
    },

    onExecutorChange() {
      let _this = this;
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/worker/getWorkerManhourInfo/' + _this.jobItem.executorId).then((response) => {
        let resp = response.data;
        if (resp.success) {
          _this.workerManhour = resp.content;
        } else {
          Toast.warning(resp.message, Swal);
        }
      })
    },

    /**
     * 保存客户信息
     */
    saveJobItem() {
      let _this = this;
      // 保存校验
      if (1 !== 1
          || !Validator.require(_this.jobItem.jobName, "任务名称", Swal)
          || !Validator.require(_this.jobItem.executorId, "工作执行人", Swal)  /*任务的执行人,在任务流转全周期保持不变; 这与任务Process的执行人概念不同*/
          || !Validator.require(_this.jobItem.taskDescription, "任务描述", Swal)
          || !Validator.require(_this.jobItem.manhour, "工时", Swal)
          || !Validator.require(_this.jobItem.planSubmissionTime, "提交时间", Swal)

          || !Validator.length(_this.jobItem.jobName, "任务名称", 5, 50, Swal)
          || !Validator.length(_this.jobItem.taskDescription, "任务描述", 10, 1000, Swal)
          || !Validator.length(_this.jobItem.manhour, "工时", 1, 4, Swal)
      ) {
        return;
      }

      _this.jobItem.customerId = _this.loginCustomer.id;
      _this.jobItem.fileList = _this.resFiles;

      _this.jobItem.type = _this.jobType;
      _this.jobItem.projectType = _this.projectType;
      _this.jobItem.loginUserId = _this.loginUser.customerUserId;

      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/job-items/reIssueJobitem', _this.jobItem).then((response) => {
        let resp = response.data;
        if (resp.success) {
          _this.jobItem = {};
          _this.resFiles = [];
          Toast.success("任务已发出", Swal);
          _this.$bus.$emit('close-manhour-tab', _this.index);
          _this.$bus.$emit('notify-jobitem-count-flag', true); //执行管理
        } else {
          Toast.warning(resp.message, Swal);
        }
      })
    },

    cancelJobitem() {
      let _this = this;
      Swal.fire({
        title: '确认',
        text: "是否取消本项工作任务?",
        icon: 'question',
        showCancelButton: true,
        confirmButtonColor: '#409EFF',
        cancelButtonColor: '#909399',
        confirmButtonText: '确认!',
        cancelButtonText: '取消!'
      }).then((result) => {
        if (result.isConfirmed) {
          let formData = new window.FormData();
          formData.append('jobitemId', _this.jobItem.id);
          formData.append('loginUserId', _this.loginUser.customerUserId);
          _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/job-items/cancelJobitem', formData).then((response) => {
            let resp = response.data;
            if (resp.success) {
              Toast.success("任务取消成功", Swal);
              _this.$bus.$emit('close-manhour-tab', _this.index);
              _this.$bus.$emit('notify-jobitem-count-flag', true); //执行管理
            } else {
              Toast.warning(resp.message, Swal);
            }
          })
        }
      }).catch(_ => {
      });
    },
  },
}
</script>

<style lang="scss" scoped>
@import './../../../public/static/scss/caipd.scss';

.text-box {
  display: inline-block;
  padding-left: 3px;
  padding-right: 15px;
  line-height: 30px;
  //width: 100px;
  background-color: rgba(204, 204, 204, 0.20);
}

.dialog-width {
  width: 100%;
}
</style>
