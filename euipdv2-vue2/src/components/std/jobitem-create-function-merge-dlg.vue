<!--
 * Author: 周朔鹏
 * Date: 2025-04-18
 * Email: 391902958@qq.com
 * Website: https://www.euipd.com
-->

<template>
  <JobitemCreateForm
      :info="dialogInfo"
      :executors="executors"
      executor-on-name-row
      :name-span="11"
      :name-inner-span="23"
      :executor-span="6"
      name-row-style="margin-bottom: 8px;"
      :desc-span="23"
      :manhour-span="11"
      :manhour-inner-span="14"
      manhour-input-width=""
      time-with-manhour
      :picker-options="pickerOptions"
      :gate-submit="false"
      cancel-text="关闭"
      @submit="saveJobItem"
      @cancel="close">
    <template #beside-name>
      <el-col :span="6">
        <el-form-item label="产品经理">
          <div class="text-box">{{ productManagerName }}</div>
        </el-form-item>
      </el-col>
    </template>
    <template #after-description>
      <el-row style="margin-top: 5px;">
        <el-col :span="24">
          <el-form-item label="原特性名称">
            <el-col :span="22">
              <div>{{ characterNameListStr }}</div>
            </el-col>
          </el-form-item>
        </el-col>
      </el-row>
    </template>
    <template #in-form>
      <div class="hr hr-9 dotted"></div>
      <div style="margin: 8px 0 20px;">
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
    </template>
  </JobitemCreateForm>
</template>

<script>
import FileRdms from "../rdms/file-rdms.vue";
import Swal from "sweetalert2";

import JobitemCreateForm from "@/components/std/jobitem-create-form.vue";
import jobitemCreateDlgMixin from "@/mixins/jobitemCreateDlgMixin";

export default {
  mixins: [jobitemCreateDlgMixin],
  components: {
    JobitemCreateForm,
    FileRdms,
  },
  name: "jobitem-create-function-merge-dlg",

  props: {
    characterIdList: {
      default: null
    },
    jobType: {
      default: null
    },
    functionType: {
      default: null,
    },
    projectType: {
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
      characterInfo: {},

      dialogInfo: {},
      mergeCharacterDto: {},
      characterNameListStr: '',
      preProjectId: '',
      productManagerName: '',

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
    };
  },

  computed: {

  },

  mounted: function () {
    let _this = this;
    _this.jobItem.id = Uuid.shortUuid(8);

    _this.getLoginUser();
    if(Tool.isNotEmpty(_this.characterIdList)){
      _this.listExecutors();
      _this.getCharacterNameListByIdList(_this.characterIdList);
    }
  },

  methods: {


    getCharacterNameListByIdList(characterIdList){
      let _this = this;
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/character/getCharacterNameListByIdList', {
        customerId: _this.loginCustomer.id,
        characterIdList: characterIdList
      }).then((response)=>{
        let resp = response.data;
        if (resp.success) {
          _this.mergeCharacterDto = resp.content;
          _this.preProjectId = _this.mergeCharacterDto.preProjectId;
          _this.productManagerName = _this.mergeCharacterDto.productManagerName;
          if( Tool.isNotEmpty(_this.mergeCharacterDto.characterNameList) ){
            _this.characterNameListStr = '';
            _this.mergeCharacterDto.characterNameList.forEach(item => {
              _this.characterNameListStr = _this.characterNameListStr + "《" + item + "》 ";
            })
          }
        } else {
          Toast.warning(resp.message, Swal);
        }
      })
    },

    /**
     * 下发修订任务
     */
    saveJobItem() {
      let _this = this;
      // 保存校验
      if (!_this.validateJobCoreFields(_this.dialogInfo)
          || !Validator.length(_this.dialogInfo.manhour, "工时", 1, 4, Swal)) {
        return;
      }

      _this.jobItem.jobName = _this.dialogInfo.jobName;
      _this.jobItem.taskDescription = _this.dialogInfo.taskDescription;
      _this.jobItem.executorId = _this.dialogInfo.executorId;
      _this.jobItem.manhour = _this.dialogInfo.manhour;
      _this.jobItem.planSubmissionTime = _this.dialogInfo.planSubmissionTime;
      _this.jobItem.productManagerId = _this.mergeCharacterDto.productManagerId;
      _this.jobItem.preProjectId = _this.preProjectId;
      _this.jobItem.characterIdList = _this.characterIdList;

      _this.applyJobSaveMeta(_this.jobItem, {
        // FUNCTION 已废弃：优先使用父组件传入的 TASK_FUNCTION
        type: _this.jobType || JOB_ITEM_TYPE.TASK_FUNCTION.key,
        functionType: _this.functionType || JOB_ITEM_TYPE.MERGE.key,
        projectType: _this.projectType || PROJECT_TYPE.PRE_PROJECT.key,
        fileList: _this.resFiles,
      });

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
          _this.$bus.$emit('refresh-system-function-tab', true); //执行管理
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
