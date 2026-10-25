<!--
 * Author: 周朔鹏
 * Date: 2024-11-17
 * Email: 391902958@qq.com
 * Website: https://www.euipd.com
-->

<template>
  <div>
    <div class="dialog-container">
      <el-form
          :model="dialogInfo"
          ref="demand_iteration"
          label-width="130px"
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
<!--          <el-col :span="6">
            <el-form-item label="评审组长">
              <div class="text-box">{{ productManagerName }}</div>
            </el-form-item>
          </el-col>-->
        </el-row>
        <el-row>
          <el-col :span="23">
            <el-form-item label="任务描述" required>
              <el-input type="textarea" rows="5" v-model="dialogInfo.taskDescription"></el-input>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row style="margin-top: 5px;">
          <el-col :span="7">
            <el-form-item label="截至时间" required>
              <el-date-picker
                  type="date"
                  placeholder="选择日期"
                  :picker-options="pickerOptions"
                  v-model="dialogInfo.planSubmissionTime">
              </el-date-picker>
            </el-form-item>
          </el-col>
        </el-row>
        <div class="hr hr-9 dotted"></div>
        <el-row>
          <el-col :span="24">
            <el-form-item label="评审委员">
              <div v-if="!hasReviewJobitem">
                <el-col :offset="3">
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
              <div v-else>
                <div class="text-box">{{ workerNameStr }}</div>
              </div>
            </el-form-item>
          </el-col>
        </el-row>
        <div class="hr hr-9 dotted"></div>
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
              <file-rdms v-bind:input-id="'file-upload-job-item-iteration'"
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

      <div class="clearfix form-actions">
        <div style="display: flex; justify-content: center;">
          <button type="button" v-on:click="saveJobItem()" class="ca-btn-primary">提交评审</button>
          <button type="button" v-on:click="close()" class="ca-btn-primary">取消</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import FileRdms from "../rdms/file-rdms.vue";
import Swal from "sweetalert2";
import jobitemCreateDlgMixin from "@/mixins/jobitemCreateDlgMixin";

export default {
  mixins: [jobitemCreateDlgMixin],
  components: {
    FileRdms,
  },
  name: "review-create-issue-dlg",

  props: {
    xId:{
      default: null,
      // characterId jobitemId projectId etc.
    },
    docType: {
      default: null,
      //character jobitem project etc.
    },
    projectType: {
      default: null,
    },
    // 特性定义变更评审（REVIEW_CHANGE）的评审对象：修改分支；xId 为该分支所属特性
    richBranchId: {
      default: null,
    },
    index: {
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

      productManagerName: '',
      hasReviewJobitem: false,   //在要求补充材料的时候, 提交材料后,还是由原评审组评审
      workerNameStr: '',
      reviewItem: {},
      characterId: '',

      dialogInfo: {},
      value: [],
      workers: [],
      workerList: [],
      titles: ['可选人员','评审委员'],

      jobItem: {},
      character: {},
      project: {},
      subProject: {},

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

    if(Tool.isNotEmpty(_this.xId)){
      switch (_this.docType ){
        case DOC_TYPE.CHARACTER.key: {
          _this.getCharacter(_this.xId);
          break;
        }
        case DOC_TYPE.PROJECT.key: {
          _this.getProjectInfo(_this.xId);
          break;
        }
      }
    }

  },

  methods: {
    getLoginUser(){
      this.initLoginContext();
      this.listAllWorker(this.loginCustomer.id);
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


    getCharacter(characterId) {
      let _this = this;
      let formData = new window.FormData();
      formData.append('characterId', characterId);
      formData.append('loginUserId', _this.loginUser.customerUserId);
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/character/getCharacterRecordDetailById', formData).then((response) => {
        let resp = response.data;
        if (resp.success) {
          _this.character = resp.content;
          _this.hasReviewJobitem = Tool.isEmpty(_this.richBranchId) && resp.content.hasReviewJobitem;
          _this.productManagerName = _this.character.productManagerName;
          if(_this.hasReviewJobitem){
            _this.loadReviewInfoByCharacterId(characterId);
          }
        }else{
          Toast.warning(resp.message, Swal);
        }
      });
    },

    loadReviewInfoByCharacterId(characterId) {
      let _this = this;
      //查询是否已有评审任务
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/job-items/getReviewJobitemsByCharacterId/' + characterId).then((response)=>{
        let resp = response.data;
        if (resp.success) {
          _this.reviewItem = resp.content[0];
          _this.jobItem = _this.reviewItem;  //更新成已有任务
          _this.value = _this.reviewItem.reviewWorkerIdList || [];
          if(Tool.isNotEmpty(_this.reviewItem.fileList)){
            _this.reviewItem.fileList.forEach((item) => {
              _this.resFiles.push(item);
              _this.resFileIds.push(item.id);
            });
          }
          _this.workerNameStr += _this.reviewItem.executorName + ' ';
          if(Tool.isNotEmpty(_this.reviewItem.reviewWorkerNameList)){
            _this.reviewItem.reviewWorkerNameList.forEach((item) => {
              _this.workerNameStr += item + ' ';
            });
          }
        } else {
          Toast.warning(resp.message,Swal);
        }
      })
    },


    getProjectInfo(projectId){
      let _this = this;
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/project/getProjectInfo/' + projectId).then((response) => {
        let resp = response.data;
        if (resp.success) {
          _this.project = resp.content;
          _this.hasReviewJobitem = resp.content.hasReviewJobitem;
          _this.productManagerName = _this.project.productManagerName;
          if(_this.hasReviewJobitem){
            _this.loadReviewInfoByProjectId(projectId);
          }
        } else {
          Toast.warning(resp.message, Swal);
        }
      })
    },

    loadReviewInfoByProjectId(projectId) {
      let _this = this;
      //查询是否已有评审任务
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/job-items/getReviewJobitemsByProjectId/' + projectId).then((response)=>{
        let resp = response.data;
        if (resp.success) {
          _this.reviewItem = resp.content[0];
          _this.jobItem = _this.reviewItem;
          _this.value = _this.reviewItem.reviewWorkerIdList || [];
          // debugger
          if(Tool.isNotEmpty(_this.reviewItem.fileList)){
            _this.reviewItem.fileList.forEach((item) => {
              _this.resFiles.push(item);
              _this.resFileIds.push(item.id);
            });
          }
          _this.workerNameStr += _this.reviewItem.executorName + ' ';
          if(Tool.isNotEmpty(_this.reviewItem.reviewWorkerNameList)){
            _this.reviewItem.reviewWorkerNameList.forEach((item) => {
              _this.workerNameStr += item + ' ';
            });
          }
        } else {
          Toast.warning(resp.message,Swal);
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
          || !Validator.require(_this.dialogInfo.jobName, "评审标题",Swal)
          || !Validator.require(_this.dialogInfo.taskDescription, "任务描述",Swal)
          || !Validator.require(_this.dialogInfo.planSubmissionTime, "截至时间",Swal)

          || !Validator.length(_this.dialogInfo.jobName, "评审标题", 5, 50,Swal)
          || !Validator.length(_this.dialogInfo.taskDescription, "任务描述", 10, 500,Swal)
      ) {
        return;
      }
      if (!_this.value || !_this.value.length) {
        Toast.warning("评审委员不能为空", Swal);
        return;
      }

      switch (_this.docType ){
        case DOC_TYPE.CHARACTER.key: {
          _this.jobItem.characterId = _this.character.id;
          _this.jobItem.productManagerId = _this.character.productManagerId;
          _this.jobItem.type = JOB_ITEM_TYPE.REVIEW.key;   //创建评审任务  固定参数
          if (Tool.isNotEmpty(_this.richBranchId)) {
            _this.jobItem.type = JOB_ITEM_TYPE.REVIEW_CHANGE.key;
            _this.jobItem.richBranchId = _this.richBranchId;
          }
          break;
        }
        case DOC_TYPE.PROJECT.key: {
          _this.jobItem.projectId = _this.project.id;
          _this.jobItem.subprojectId = _this.project.id;
          _this.jobItem.productManagerId = _this.project.productManagerId;
          _this.jobItem.type = JOB_ITEM_TYPE.REVIEW_PRO.key;   //创建评审任务  固定参数
          break;
        }
        case DOC_TYPE.SUB_PROJECT.key: {
          _this.jobItem.projectId = _this.subProject.id;
          _this.jobItem.subprojectId = _this.subProject.subprojectId;
          _this.jobItem.projectManagerId = _this.subProject.projectManagerId;
          _this.jobItem.type = JOB_ITEM_TYPE.REVIEW_SUBP.key;   //创建评审任务  固定参数
          break;
        }
      }

      _this.jobItem.jobName = _this.dialogInfo.jobName;
      _this.jobItem.taskDescription = _this.dialogInfo.taskDescription;
      _this.jobItem.planSubmissionTime = _this.dialogInfo.planSubmissionTime;

      _this.jobItem.customerId = _this.loginCustomer.id;

      _this.jobItem.fileList = _this.resFiles;
      _this.jobItem.reviewWorkerIdList = _this.value;
      _this.jobItem.loginUserId = _this.loginUser.customerUserId;

      _this.jobItem.docType = _this.docType;
      _this.jobItem.projectType = Tool.isNotEmpty(_this.richBranchId) ? PROJECT_TYPE.SUB_PROJECT.key : _this.projectType;
      const isChangeReview = Tool.isNotEmpty(_this.richBranchId);

      //将Character的状态改为 integration
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

          if (isChangeReview) {
            _this.$bus.publish({ areas: ['project', 'jobitem', 'review', 'badge'], reason: 'change-review-create' });
            _this.$bus.$emit('refresh-system-examine-container-subproject', true);
          }

          if(_this.entrance === ENTRANCE_CLASS.PROJECT.key){
            if(_this.projectType === PROJECT_TYPE.PROJECT.key && _this.docType === DOC_TYPE.PROJECT.key){
              _this.$bus.publish({ areas: ['project', 'jobitem', 'review', 'badge'], reason: 'review-create' });
            }
          }

          if(_this.entrance === ENTRANCE_CLASS.DEVELOP.key){
            if(_this.projectType === PROJECT_TYPE.SUB_PROJECT.key && _this.docType === DOC_TYPE.CHARACTER.key){
              _this.$bus.publish({ areas: ['develop', 'jobitem', 'review', 'badge'], reason: 'review-create' });
            }
          }

        } else {
          Toast.warning(resp.message, Swal);
        }
      })
    },

    close(){
      let _this = this;
      _this.closeDlg();
    },

    afterUpload(resp) {
      let _this = this;
      // debugger
      _this.resFile = resp.content;
      _this.resFile.customerId = _this.loginCustomer.id;
      _this.resFile.projectId = _this.character.projectId;
      _this.resFile.subprojectId = _this.character.subprojectId;
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
