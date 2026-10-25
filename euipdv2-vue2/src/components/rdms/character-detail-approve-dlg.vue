<!--
 * Author: 周朔鹏
 * Date: 2025-02-14
 * Email: 391902958@qq.com
 * Website: https://www.euipd.com
-->

<template>
  <div>
    <div class="dialog-container">
      <el-form
          ref="character_approve"
          label-width="164px"
          :show-message=false
          class="dialog-width">
        <el-form-item
            label="特性名称:">
          {{ characterItem.characterName }}
        </el-form-item>
        <el-form-item
            label="签审意见:">
          <el-row>
            <el-col :span="21">
              <el-input type="textarea" rows="4" v-model="characterProcess.jobDescription"></el-input>
            </el-col>
          </el-row>
        </el-form-item>
        <el-form-item
            label="功能预算:">
          <el-row>
            <el-col :span="7">
              <el-form
                  label-width="120px">
                <el-form-item
                    label="开发工时">
                  <el-input type="number" style="width: 100px;" v-model="characterItem.devManhourApproved" placeholder="开发工时"></el-input> <span>小时</span>
                </el-form-item>
                <el-form-item
                    label="内测工时">
                  <el-input type="number" style="width: 100px;" v-model="characterItem.testManhourApproved" placeholder="内测工时"></el-input> <span>小时</span>
                </el-form-item>
                <el-form-item
                    label="第三方测试费">
                  <el-input type="number" style="width: 100px;" v-model="characterItem.testFee" placeholder="三方测试费"></el-input> <span>元</span>
                </el-form-item>
                <el-form-item
                    label="物料费">
                  <el-input type="number" style="width: 100px;" v-model="characterItem.materialFeeApproved" placeholder="物料费"></el-input> <span>元</span>
                </el-form-item>
              </el-form>
            </el-col>
            <el-col :span=7>
              <el-form
                  label-width="120px">
                <el-form-item
                    label="动力费">
                  <el-input type="number" style="width: 110px;" v-model="characterItem.powerFee" placeholder="动力费"></el-input> <span>元</span>
                </el-form-item>
                <el-form-item
                    label="会议费">
                  <el-input type="number" style="width: 110px;" v-model="characterItem.conferenceFee" placeholder="会议费"></el-input> <span>元</span>
                </el-form-item>
                <el-form-item
                    label="差旅费">
                  <el-input type="number" style="width: 110px;" v-model="characterItem.businessFee" placeholder="差旅费"></el-input> <span>元</span>
                </el-form-item>
                <el-form-item
                    label="合作费">
                  <el-input type="number" style="width: 110px;" v-model="characterItem.cooperationFee" placeholder="合作费"></el-input> <span>元</span>
                </el-form-item>
              </el-form>
            </el-col>
            <el-col :span="7">
              <el-form
                  label-width="120px">
                <el-form-item
                    label="知识产权费">
                  <el-input type="number" style="width: 110px;" v-model="characterItem.propertyFee" placeholder="知识产权费"></el-input> <span>元</span>
                </el-form-item>
                <el-form-item
                    label="劳务费">
                  <el-input type="number" style="width: 110px;" v-model="characterItem.laborFee" placeholder="劳务费"></el-input> <span>元</span>
                </el-form-item>
                <el-form-item
                    label="专家咨询费">
                  <el-input type="number" style="width: 110px;" v-model="characterItem.consultingFee" placeholder="专家咨询费"></el-input> <span>元</span>
                </el-form-item>
                <el-form-item
                    label="管理费">
                  <el-input type="number" style="width: 110px;" v-model="characterItem.managementFee" placeholder="管理费"></el-input> <span>元</span>
                </el-form-item>
              </el-form>
            </el-col>
          </el-row>
        </el-form-item>
        <div style="height: 50px;"></div>
      </el-form>

      <div class="clearfix form-actions">
        <div style="display: flex; justify-content: center;">
          <el-button type="primary" v-on:click="approve()" >批准</el-button>
          <el-button type="primary" v-on:click="close()" >关闭</el-button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import FileRdms from "../../components/rdms/file-rdms";
import Swal from "sweetalert2";
import FileSaver from 'file-saver';
import FileUploadCore from "@/components/std/file-upload-core.vue";

export default {
  components: {
    FileUploadCore,
    FileRdms,
    FileSaver,
  },
  name: "character-detail-approve-dlg",

  props: {
    characterId: {
      default: null
    },
    jobType: {
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
    closeDlg: {
      type: Function,
      default: null
    },
  },

  data() {
    return {
      loginUser: {},
      loginCustomer: {},

      characterItem: {
        devManhourApproved: 0,
        devManhour: 0,
        testManhourApproved: 0,
        testManhour: 0,
        testFee: 0,
        materialFeeApproved: 0,
        materialFee: 0,
        powerFee: 0,
        conferenceFee: 0,
        businessFee: 0,
        cooperationFee: 0,
        propertyFee: 0,
        laborFee: 0,
        consultingFee: 0,
        managementFee: 0,
      },
      characterBudget: {},
      parentCharacterBudget: {},

      redColor: 'red-color',

      originCharacterBudget: {},
      copyCharacterItem: {},
      budgetPool: 0,

      characterProcess: {},

      projectManagers: [],
      executors: [],
      milestones: [],
      characters: [],
      resFile: {},
      resFiles: [],
      resFileIds: [],
      FILE_GROUPING: FILE_GROUPING,
      PROJECT_TYPE: PROJECT_TYPE,
      JOB_ITEM_TYPE: JOB_ITEM_TYPE,
    };
  },

  computed: {
    getXProjectId(){
      if(this.projectType === PROJECT_TYPE.PRE_PROJECT.key){
        return  this.characterItem.preProjectId;
      }
      if(this.projectType === PROJECT_TYPE.PROJECT.key){
        return  this.characterItem.projectId;
      }
      if(this.projectType === PROJECT_TYPE.SUB_PROJECT.key || this.projectType === PROJECT_TYPE.DEV_PROJECT.key){
        return  this.characterItem.subprojectId;
      }
      return 0;
    },
  },

  mounted: function () {
    let _this = this;
    _this.getLoginUser();
    if(Tool.isNotEmpty(_this.characterId)){
      _this.getCharacterInfo(_this.characterId);
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

    getUploadFileList(resFiles){
      let _this = this;
      _this.resFiles = resFiles;
      if(_this.resFiles !== null){
        _this.resFileIds= [];
        if(resFiles && resFiles.length>0){
        resFiles.forEach(item => {
          _this.resFileIds.push(item.id);
        })
      }
      }
    },

    getCharacterInfo(characterId){
      let _this = this;
      let formData = new window.FormData();
      formData.append('characterId', characterId);
      formData.append('loginUserId', _this.loginUser.customerUserId);
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/character/getCharacterRecordDetailById', formData).then((response) => {
        let resp = response.data;
        if (resp.success) {
          _this.characterItem = resp.content;
          if (_this.characterItem.materialFeeApproved == null) {
            _this.characterItem.materialFeeApproved = 0;
          }
          _this.copyCharacterItem = JSON.parse(JSON.stringify( _this.characterItem ));
          if(_this.jobType === JOB_ITEM_TYPE.DECOMPOSE.key && _this.projectType !== PROJECT_TYPE.PRE_PROJECT.key){
            _this.getBudget_decompose(_this.characterItem.parent);
          }
          if(_this.jobType === JOB_ITEM_TYPE.ITERATION.key && _this.projectType !== PROJECT_TYPE.PRE_PROJECT.key){
            _this.getBudget_iteration(_this.characterItem.id);
          }

          if(_this.projectType === PROJECT_TYPE.SUB_PROJECT.key || _this.projectType === PROJECT_TYPE.DEV_PROJECT.key){
            _this.getProjectBudgetPoolBySubprojectId(_this.characterItem.subprojectId);
          }
        } else {
          Toast.warning(resp.message,Swal);
        }
      })
    },

    getBudget_decompose(characterId){
      let _this = this;
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/budget/getCharacterBudget_decompose/' + characterId).then((response) => {
        let resp = response.data;
        if (resp.success) {
          _this.parentCharacterBudget = resp.content;
          _this.originCharacterBudget = JSON.parse(JSON.stringify( _this.parentCharacterBudget ));
        }else{
          Toast.warning(resp.message, Swal);
        }
      });
    },
    getBudget_iteration(characterId){
      let _this = this;
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/budget/getCharacterBudget_iteration/' + characterId).then((response) => {
        let resp = response.data;
        if (resp.success) {
          _this.parentCharacterBudget = resp.content;
          _this.originCharacterBudget = JSON.parse(JSON.stringify( _this.parentCharacterBudget ));
        }else{
          Toast.warning(resp.message, Swal);
        }
      });
    },

    getProjectBudgetPoolBySubprojectId(subprojectId){
      let _this = this;
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/budget/getProjectBudgetPoolBySubprojectId/' + subprojectId).then((response) => {
        let resp = response.data;
        if (resp.success) {
          _this.budgetPool = resp.content;
        }else{
          Toast.warning(resp.message, Swal);
        }
      });
    },

    approve() {
      let _this = this;

      if (1 !== 1
          || !Validator.require(_this.characterProcess.jobDescription, "签审意见", Swal) /*这个是审批给的的意见*/
          || !Validator.require(_this.characterItem.devManhourApproved, "计划工时", Swal)
          || !Validator.require(_this.characterItem.testManhourApproved, "测试工时", Swal)
          || !Validator.require(_this.characterItem.materialFeeApproved, "物料费", Swal)

          || !Validator.length(_this.characterProcess.jobDescription, "签审意见", 2, 500, Swal)
          || !Validator.isNumber(_this.characterItem.devManhourApproved, "计划工时",  Swal)
          || !Validator.isNumber(_this.characterItem.testManhourApproved, "测试工时",  Swal)
          || !Validator.isNumber(_this.characterItem.materialFeeApproved, "物料费",  Swal)
      ) {
        return;
      }
      //character 都是产品经理负责组织开发和修订审批的
      _this.characterProcess.nextNode = _this.characterItem.productManagerId;

      //批准
      _this.characterProcess.customerId = _this.loginCustomer.id;
      _this.characterProcess.characterId = _this.characterItem.id;
      _this.characterProcess.executorId = _this.loginUser.customerUserId;
      _this.characterProcess.fileListStr = JSON.stringify(_this.resFileIds);

      _this.characterBudget.devManhourApproved = _this.characterItem.devManhourApproved;
      _this.characterBudget.testManhourApproved = _this.characterItem.testManhourApproved;
      _this.characterBudget.materialFeeApproved = _this.characterItem.materialFeeApproved;

      _this.characterBudget.testFee = _this.characterItem.testFee;
      _this.characterBudget.powerFee = _this.characterItem.powerFee;
      _this.characterBudget.conferenceFee = _this.characterItem.conferenceFee;
      _this.characterBudget.businessFee = _this.characterItem.businessFee;
      _this.characterBudget.cooperationFee = _this.characterItem.cooperationFee;
      _this.characterBudget.propertyFee = _this.characterItem.propertyFee;
      _this.characterBudget.laborFee = _this.characterItem.laborFee;
      _this.characterBudget.consultingFee = _this.characterItem.consultingFee;
      _this.characterBudget.managementFee = _this.characterItem.managementFee;

      if(_this.projectType === PROJECT_TYPE.PRE_PROJECT.key ){
        _this.characterBudget.devManhour = _this.characterItem.devManhourApproved;
        _this.characterBudget.testManhour = _this.characterItem.testManhourApproved;
        _this.characterBudget.materialFee = _this.characterItem.materialFeeApproved;
      }

      _this.characterProcess.characterBudget = _this.characterBudget;  //预算
      _this.characterProcess.loginUserId = _this.loginUser.customerUserId;
      _this.characterProcess.projectType = _this.projectType;
      _this.characterProcess.jobType = _this.jobType;

      if(_this.projectType === PROJECT_TYPE.DEV_PROJECT.key || _this.projectType === PROJECT_TYPE.SUB_PROJECT.key){
        Swal.fire({
          title: '复核',
          text: "确认后, 该功能将由系统工程负责人进行复核",
          icon: 'question',
          showCancelButton: true,
          confirmButtonColor: '#409EFF',
          cancelButtonColor: '#909399',
          confirmButtonText: '确认!',
          cancelButtonText: '取消!'
        }).then((result) => {
          if (result.value) {
            extracted();
          }
        })
      }else{
        extracted();
      }

      function extracted() {
        _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/character/approve', _this.characterProcess).then((response) => {
          let resp = response.data;
          if (resp.success) {
            _this.resFile = {};
            _this.resFiles = [];
            _this.resFileIds = [];
            _this.close();

            _this.$bus.$emit('notify-character-submit-job-flag', true);
            _this.$bus.$emit('notify-character-evaluate-job-flag', true);
            _this.$bus.$emit('notify-function-job-flag', true);

            switch (_this.entrance) {
              case ENTRANCE_CLASS.FUNCTION.key:{
                _this.$bus.$emit('refresh-character-joblist-examine', true);
                _this.$bus.$emit('refresh-character-joblist-evaluate', true);
                _this.$bus.$emit('refresh-function-preproject-writing', true);
                _this.$bus.$emit('refresh-preproject-function-tab-num', true);
                break;
              }
              case ENTRANCE_CLASS.SYSTEM.key: {
                if (_this.projectType === PROJECT_TYPE.DEV_PROJECT.key) {
                  _this.$bus.$emit('refresh-system-joblist-tab-num', true);
                  _this.$bus.$emit('refresh-system-handling-container-subproject', true);
                  _this.$bus.$emit('refresh-system-examine-container-subproject', true);
                  _this.$bus.$emit('refresh-system-evaluate-container-subproject', true);
                }
                _this.$bus.$emit('notify-project-job-flag', true);   //项目管理
                _this.$bus.$emit('notify-jobitem-count-flag', true); //执行管理
                break;
              }

              case ENTRANCE_CLASS.CHARACTER.key: {
                if (_this.projectType === PROJECT_TYPE.PRE_PROJECT.key) {
                  _this.$bus.$emit('refresh-character-joblist-tab-num', true);
                  _this.$bus.$emit('refresh-character-joblist-editing', true);
                  _this.$bus.$emit('refresh-character-joblist-examine', true);
                  _this.$bus.$emit('refresh-character-joblist-evaluate', true);
                  _this.$bus.$emit('refresh-character-joblist-approved', true);
                }
                _this.$bus.$emit('notify-project-job-flag', true);   //项目管理
                _this.$bus.$emit('notify-jobitem-count-flag', true); //执行管理
                break;
              }
            }

            if (_this.projectType === PROJECT_TYPE.DEV_PROJECT.key) {
              _this.$bus.$emit('refresh-system-handling-container', true);
              _this.$bus.$emit('refresh-system-examine-container', true);
              _this.$bus.$emit('refresh-system-joblist-tab-num', true);
            }

          } else {
            Toast.warning(resp.message, Swal);
          }
        })
      }
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
