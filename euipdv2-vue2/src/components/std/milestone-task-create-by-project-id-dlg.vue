<!--
 * Author: 周朔鹏
 * Date: 2024-12-12
 * Email: 391902958@qq.com
 * Website: https://www.euipd.com
-->

<template>
  <div>
    <div class="dialog-container">
      <div style="margin: 0 50px;">
        <div style="font-size: 15px; font-weight: 500;">
          里程碑任务下发说明:
        </div>
        1. 里程碑任务将发送给包括主项目和所有子项目的项目经理;<br/>
        2. 里程碑到达前, 系统会自动发出里程碑任务. 因此, 如果在里程碑到达前, 没有额外的工作安排, 不需要手动发出里程碑任务.<br/>
        3. 里程碑任务的执行逻辑与普通的项目任务不同. 如果是其他项目任务, 请在项目管理-任务管理中进行下发.<br/>
      </div>
      <div class="hr hr-16 hr-dotted"></div>
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
        </el-row>
        <el-row>
          <el-col :span="22">
            <el-form-item label="任务描述" required>
              <el-input type="textarea" rows="6" v-model="dialogInfo.taskDescription"></el-input>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="7">
            <el-form-item label="提交时间" required>
              <el-date-picker
                  type="date"
                  placeholder="选择日期"
                  :picker-options="pickerOptions"
                  v-model="dialogInfo.planSubmissionTime">
              </el-date-picker>
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <div style="margin: 15px 110px 20px 0">
        <div class="hr hr-10 dotted"></div>
        <FileUploadCore
            :input-id="('milestone-task-create-by-project-id-' + (taskJobitem.id))"
            :key="('file-upload-' + (taskJobitem.id))"
            :x-project-id="projectId"
            :item-id="taskJobitem.id"
            :file-group="FILE_GROUPING.JOBITEM.key"
            :project-type="PROJECT_TYPE.PROJECT.key"
            :read-db-record="true"
            :en-delete="true"
            v-on:uploadFileList="getUploadFileList"
        ></FileUploadCore>
      </div>
      <div class="clearfix form-actions">
        <div style="display: flex; justify-content: center;">
<!--          <button type="button" v-on:click="sendMilestoneTask()" class="ca-btn-primary">发里程碑任务</button>-->
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
import MyChart from "@/components/util/myChart.vue";

import Swal from "sweetalert2";

export default {
  components: {
    FileRdms,
    FileUploadCore,
    SelectJobitemFileList,
    MyChart,
  },
  name: "milestone-task-create-by-project-id-dlg",

  props: {
    projectId: {
      default: null
    },
    milestoneId: {
      default:null,
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
      taskJobitem: {},
      resFiles: [],

      FILE_GROUPING: FILE_GROUPING,
      PROJECT_TYPE: PROJECT_TYPE,

      pickerOptions: {
        disabledDate(v) {
          return v.getTime() < new Date().getTime() - 86400000;//  - 86400000是否包括当天
        }
      },
    };
  },

  computed: {
  },

  mounted: function () {
    let _this = this;
    _this.taskJobitem.id = Uuid.shortUuid(8);
  },

  methods: {
    getLoginUser(){
      let _this = this;
      if(Tool.getLoginUser().userGroup === LOGIN_USER_GROUP.CUSTOMER.key){
        _this.loginUser = Tool.getLoginUser().adminUser;
        _this.loginCustomer =Tool.getLoginUser();
      }else{
        _this.loginUser = Tool.getLoginUser();
        _this.loginCustomer = Tool.getLoginUser().customer;
      }
    },

    getUploadFileList(resFiles){
      let _this = this;
      _this.resFiles = resFiles;
    },

    /**
     * 保存客户信息
     */
    sendMilestoneTask() {
      let _this = this;
      // 保存校验
      if (1 !== 1
          || !Validator.require(_this.dialogInfo.jobName, "任务名称",Swal)
          || !Validator.require(_this.dialogInfo.taskDescription, "任务描述",Swal)
          || !Validator.require(_this.dialogInfo.planSubmissionTime, "提交时间",Swal)

          || !Validator.length(_this.dialogInfo.jobName, "任务名称", 5, 50,Swal)
          || !Validator.length(_this.dialogInfo.taskDescription, "任务描述", 10, 500,Swal)
      ) {
        return;
      }

      _this.taskJobitem.jobName = _this.dialogInfo.jobName;
      _this.taskJobitem.taskDescription = _this.dialogInfo.taskDescription;
      _this.taskJobitem.planSubmissionTime = _this.dialogInfo.planSubmissionTime;

      _this.taskJobitem.projectId = _this.projectId;
      _this.taskJobitem.milestoneId = _this.milestoneId;

      _this.taskJobitem.fileList = _this.resFiles;  //来自子组件

      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/milestone/sendMilestoneTask', _this.taskJobitem).then((response)=>{
        let resp = response.data;
        if (resp.success) {
          _this.taskJobitem = {};
          _this.resFiles = [];
          _this.taskJobitem.id = Uuid.shortUuid(8); //重新生成一个ID
          _this.close();
          Toast.success("任务已发出", Swal);
          _this.$bus.publish({ areas: ['project', 'jobitem', 'badge'], reason: 'milestone-task-create' });
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
