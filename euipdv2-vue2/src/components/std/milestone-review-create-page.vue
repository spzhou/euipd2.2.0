<!--
 * Author: 周朔鹏
 * Date: 2024-11-17
 * Email: 391902958@qq.com
 * Website: https://www.euipd.com
-->

<template>
  <div>
    <div class="a4-paper-big-plain">
      <div style="margin: 50px 60px 20px">
        <div style="font-size: 22px; text-align: center; width: 100%;">
          里程碑阶段评审申请表
        </div>
        <div class="hr hr-9 dotted"></div>
        <el-form
            :model="dialogInfo"
            ref="demand_iteration"
            label-width="80px"
            :show-message=false
            class="jobItem">
          <el-row>
            <el-col :span="11">
              <el-form-item label="评审标题" required>
                <el-col :span="23">
                  <el-input v-model="dialogInfo.jobName"></el-input>
                </el-col>
              </el-form-item>
            </el-col>
          </el-row>
          <el-row>
            <el-col :span="22">
              <el-form-item label="任务描述" required>
                <el-input type="textarea" rows="5" v-model="dialogInfo.taskDescription"></el-input>
              </el-form-item>
            </el-col>
          </el-row>
          <el-row>
            <el-col :span="24">
              <el-form-item label="评审委员" required>
                <div>
                  <el-col :offset="2">
                    <el-transfer
                        ref="review-member"
                        filterable
                        filter-placeholder="请输入姓名"
                        v-model="value"
                        :data="workerList"
                        :titles="titles">
                    </el-transfer>
                  </el-col>
                </div>
              </el-form-item>
            </el-col>
          </el-row>
          <el-row>
            <el-col :span="10">
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
          <div class="hr 9 hr-dotted"></div>
          <el-row>
            <el-col :span="22">
              <div v-show="resFiles && resFiles.length>0" style="display: flex; align-items: end; flex-direction: column; margin-bottom: 15px;">
                <div v-for="(item, i) in resFiles" :key="i" style="display: flex; flex-direction: row; margin: 5px 0 0 50px;">
                  <div style="margin-top: 6px;">
                    {{item.name}}
                  </div>
                </div>
              </div>
              <div style="text-align: right;">
                <file-rdms v-bind:input-id="'file-upload-review-create-page'"
                           v-bind:text="'上传附件'"
                           v-bind:suffixs=G_SUFFIXS
                           v-bind:customer="loginCustomer.id"
                           v-bind:use="FILE_GROUPING.JOBITEM.key"
                           v-bind:folder="'jobitem'"
                           v-bind:after-upload="afterUpload">
                </file-rdms>
              </div>
            </el-col>
          </el-row>
        </el-form>
      </div>
      <div class="clearfix form-actions">
        <div style="display: flex; justify-content: center;">
          <el-button type="primary" v-on:click="saveJobItem()">提交评审申请表</el-button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import FileRdms from "../rdms/file-rdms.vue";
import Swal from "sweetalert2";

export default {
  components: {
    FileRdms,
  },
  name: "milestone-review-create-page",

  props: {
    originJobItem:{
      default: null,
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
  },

  data() {
    return {
      loginUser: {},
      loginCustomer: {},

      dialogInfo: {},
      value: [],
      workers: [],
      workerList: [],
      titles: ['可选人员','评审委员'],

      jobItem: {},

      resFile: {},
      resFiles: [],
      resFileIds: [],
      FILE_GROUPING: FILE_GROUPING,
      JOB_ITEM_TYPE: JOB_ITEM_TYPE,
      DOC_TYPE: DOC_TYPE,
      G_SUFFIXS: G_SUFFIXS,

      pickerOptions: {
        disabledDate(v) {
          return v.getTime() < new Date().getTime() - 86400000;//  - 86400000是否包括当天
        }
      },
    };
  },

  computed: {},

  mounted: function () {
    let _this = this;
    _this.getLoginUser();
    _this.jobItem.id = Uuid.shortUuid(8);

  },

  beforeDestroy(){
    let _this = this;
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
      _this.listAllWorker(_this.loginCustomer.id);
    },

    listAllWorker(customerId) {
      let _this = this;
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/worker/listAllWorkers/'+ customerId ).then((response)=>{
        let resp = response.data;
        if (resp.success) {
          _this.workers = resp.content;
          _this.workers.forEach((worker) => {
            if(worker !== null){
              _this.workerList.push({
                label: worker.trueName,
                key: worker.id
              });
            }
          });
        } else {
          Toast.warning(resp.message,Swal);
        }
      })
    },

    /**
     * 保存评审信息
     */
    saveJobItem() {
      let _this = this;
      // 保存校验
      if (1 !== 1
          || !Validator.require(_this.dialogInfo.jobName, "评审标题",Swal)
          || !Validator.require(_this.dialogInfo.taskDescription, "任务描述",Swal)
          || !Validator.require(_this.dialogInfo.planSubmissionTime, "提交时间",Swal)

          || !Validator.length(_this.dialogInfo.jobName, "评审标题", 5, 50,Swal)
          || !Validator.length(_this.dialogInfo.taskDescription, "任务描述", 10, 500,Swal)
      ) {
        return;
      }
      if(Tool.isEmpty(_this.value) || Tool.isEmpty_ES5(_this.value)){
        Toast.warning("评审委员不能为空", Swal);
        return;
      }

      _this.jobItem.type = JOB_ITEM_TYPE.REVIEW_MILESTONE.key;   //创建评审任务  固定参数
      _this.jobItem.manhour = 0;

      _this.jobItem.projectId = _this.originJobItem.projectId;
      _this.jobItem.subprojectId = _this.originJobItem.subprojectId;
      _this.jobItem.originJobitemId = _this.originJobItem.id;

      _this.jobItem.jobName = _this.dialogInfo.jobName;
      _this.jobItem.taskDescription = _this.dialogInfo.taskDescription;
      _this.jobItem.planSubmissionTime = _this.dialogInfo.planSubmissionTime;

      _this.jobItem.customerId = _this.loginCustomer.id;

      _this.jobItem.fileList = _this.resFiles;
      _this.jobItem.reviewWorkerIdList = _this.value;
      _this.jobItem.loginUserId = _this.loginUser.customerUserId;
      _this.jobItem.executorId = _this.loginUser.customerUserId;

      _this.jobItem.docType = DOC_TYPE.MILESTONE.key;
      _this.jobItem.projectType = _this.projectType;

      Swal.fire({
        title: '确认',
        text: "是否确认提交?",
        icon: 'question',
        showCancelButton: true,
        confirmButtonColor: '#409EFF',
        cancelButtonColor: '#909399',
        confirmButtonText: '确认!',
        cancelButtonText: '取消!'
      }).then((result) => {
        if (result.value) {
          _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/job-items/save', _this.jobItem).then((response)=>{
            let resp = response.data;
            if (resp.success) {
              _this.jobItem = {};
              _this.resFile = {};
              _this.resFiles = [];
              _this.resFileIds = [];
              _this.jobItem.id = Uuid.shortUuid(8); //重新生成一个ID
              Toast.success("任务已发出", Swal);
              _this.$bus.$emit('notify-jobitem-count-flag', true); //执行管理
              _this.$bus.$emit('notify-develop-count-flag', true);  //研发管理
              _this.$bus.$emit('notify-milestone-review-count-flag', true);  //评审管理
              _this.$bus.$emit('notify-project-job-flag', true);   //项目管理

              _this.$bus.$emit('close-manhour-tab', _this.index);
            } else {
              Toast.warning(resp.message, Swal);
            }
          })
        }
      })
    },

    afterUpload(resp) {
      let _this = this;
      // debugger
      _this.resFile = resp.content;
      _this.resFile.customerId = _this.loginCustomer.id;
      _this.resFile.projectId = _this.originJobItem.projectId;
      _this.resFile.subprojectId = _this.originJobItem.subprojectId;
      _this.resFile.operatorId = _this.loginUser.customerUserId;
      _this.resFile.itemId = _this.jobItem.id;
      _this.saveFile(_this.resFile); // 将上传完成的数据保存到数据库
      _this.$forceUpdate();
    },

    saveFile(file){
      let _this = this;
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/file/rdms/save', file).then((response)=>{
        let resp = response.data;
        if (resp.success) {
          let file = resp.content;
          if(! _this.resFiles){
            _this.resFiles = [];
          }
          _this.resFiles.push(file);
          _this.resFileIds.push(file.id);
        } else {
          Toast.warning(resp.message, Swal);
        }
      })
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
.jobItem{
  width: 100%;
}
</style>
