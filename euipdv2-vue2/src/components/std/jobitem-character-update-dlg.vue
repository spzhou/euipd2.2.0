<!--
 * Author: 周朔鹏
 * Date: 2025-04-18
 * Email: 391902958@qq.com
 * Website: https://www.euipd.com
-->

<template>
  <div>
    <div class="dialog-container">
      <el-form
          label-width="130px"
          :show-message=false
          class="dialog-width">
        <el-row style="margin-bottom: 8px;">
          <el-col :span="11">
            <el-form-item label="任务名称" required>
              <el-col :span="23">
                <el-input v-model="dialogInfo.jobName"></el-input>
              </el-col>
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="产品经理">
              <div class="text-box">{{ preProject.productManagerName }}</div>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="6">
            <el-form-item label="任务执行人" required>
              <el-col :span="24">
                <el-select v-model="dialogInfo.executorId" filterable placeholder="任务执行人" @change="renderChart(dialogInfo.executorId)">
                  <el-option v-for="o in executors" :key="o.id" :value="o.id" :label="o.trueName"></el-option>
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
        <el-row style="margin-top: 5px;">
          <el-col :span="24">
            <el-form-item label="原特性名称">
              <el-col :span="20">
                <div>《{{ characterInfo.characterName }}》</div>
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
        <div style="margin: 6px 0 20px">
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
                <file-rdms v-bind:input-id="'file-upload-job-item-iteration-demand'"
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
        </div>
      </el-form>

      <div class="clearfix form-actions">
        <div style="display: flex; justify-content: center;">
          <button type="button" v-on:click="saveJobItem()" class="ca-btn-primary">发任务</button>
          <button type="button" v-on:click="close()" class="ca-btn-primary">关闭</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import FileRdms from "../rdms/file-rdms.vue";
import Swal from "sweetalert2";
import MyChart from "@/components/util/myChart.vue";

export default {
  components: {
    MyChart,
    FileRdms,
  },
  name: "jobitem-character-update-dlg",

  props: {
    characterId: {
      default: null
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
      characterInfo: {},
      preProject: {},

      projectType: {
        default: null
      },
      dialogInfo: {},

      jobItem: {},
      executors: [],
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

      executorId: '',
      overLoad10: false,
      random: 0,
    };
  },

  computed: {
  },

  mounted: function () {
    let _this = this;
    _this.random = Math.random();
    _this.jobItem.id = Uuid.shortUuid(8);

    _this.getLoginUser();
    if(Tool.isNotEmpty(_this.characterId)){
      _this.listExecutors();
      _this.getCharacterSimpleInfo(_this.characterId);
    }
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

    listExecutors(){
      let _this = this;
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/worker/listAllWorkers/'+ _this.loginCustomer.id).then((response)=>{
        let resp = response.data;
        if (resp.success) {
          _this.executors = resp.content;
        } else {
          Toast.warning(resp.message,Swal);
        }
      })
    },

    getCharacterSimpleInfo(characterId){
      let _this = this;
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/character/getCharacterSimpleInfo/'+ characterId).then((response)=>{
        let resp = response.data;
        if (resp.success) {
          _this.characterInfo = resp.content;
          if(Tool.isNotEmpty_ES5(_this.characterInfo)){
            _this.getPreProjectSimpleInfo(_this.characterInfo.preProjectId);
          }
        } else {
          Toast.warning(resp.message, Swal);
        }
      })
    },

    getPreProjectSimpleInfo(preProjectId) {
      let _this = this;
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/pre-project/getPreProjectSimpleInfo/'+ preProjectId).then((response)=>{
        let resp = response.data;
        if (resp.success) {
          _this.preProject = resp.content;
        } else {
          Toast.warning(resp.message,Swal);
        }
      })
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
          || !Validator.require(_this.dialogInfo.manhour, "工时",Swal)
          || !Validator.require(_this.dialogInfo.planSubmissionTime, "提交时间",Swal)

          || !Validator.length(_this.dialogInfo.jobName, "任务名称", 5, 50,Swal)
          || !Validator.length(_this.dialogInfo.taskDescription, "任务描述", 10, 500,Swal)
          || !Validator.length(_this.dialogInfo.manhour, "工时", 1, 4,Swal)
      ) {
        return;
      }

      _this.jobItem.jobName = _this.dialogInfo.jobName;
      _this.jobItem.taskDescription = _this.dialogInfo.taskDescription;
      _this.jobItem.executorId = _this.dialogInfo.executorId;
      _this.jobItem.manhour = _this.dialogInfo.manhour;
      _this.jobItem.planSubmissionTime = _this.dialogInfo.planSubmissionTime;

      _this.jobItem.customerId = _this.loginCustomer.id;
      _this.jobItem.preProjectId = _this.preProject.id;

      _this.jobItem.fileList = _this.resFiles;
      _this.jobItem.characterId = _this.characterId;  //关键

      _this.jobItem.loginUserId = _this.loginUser.customerUserId;
      _this.jobItem.type = JOB_ITEM_TYPE.UPDATE.key;
      _this.jobItem.projectType = PROJECT_TYPE.PRE_PROJECT.key;

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
          _this.$bus.$emit('notify-jobitem-count-flag', true); //执行管理
          _this.$bus.$emit('refresh-character-update-doc-menu', true); //更新迭代menu
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
      _this.resFile = resp.content;
      _this.resFile.customerId = _this.loginCustomer.id;
      if(Tool.isNotEmpty(_this.dialogInfo.projectId) ){
        _this.resFile.projectId = _this.dialogInfo.projectId;
      }
      if(Tool.isNotEmpty(_this.dialogInfo.preProjectId)){
        _this.resFile.preProjectId = _this.dialogInfo.preProjectId;
      }
      if(Tool.isNotEmpty(_this.dialogInfo.subprojectId)){
        _this.resFile.subprojectId = _this.dialogInfo.subprojectId;
      }
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
.dialog-width{
  width: 100%;
}
</style>
