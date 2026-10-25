<!--
 * Author: 周朔鹏
 * Date: 2024-11-17
 * Email: 391902958@qq.com
 * Website: https://www.euipd.com
-->


<template>
  <div class="page-container">
    <div class="a4-paper-big-plain">
      <div id="myContent">
        <!--特性编辑表-->
        <div>
          <character-detail-edit-core
              id="decompose-edit"
              :key="('character-edit-' + (random))"
              ref="character_edit"
              :character="editCharacterItem"
              :save-character-info="saveCharacter"
          ></character-detail-edit-core>
        </div>
        <div v-if="editCharacterItem" style="margin: 15px 50px 20px 0">
          <FileUploadCore
              :input-id="('character-decompose-edit-' + (editCharacterItem.id))"
              :key="('file-upload-' + (editCharacterItem.id))"
              :x-project-id="getXProjectId"
              :item-id="editCharacterItem.id"
              :file-group="FILE_GROUPING.CHARACTER.key"
              :project-type="projectType"
              :en-delete="true"
              :read-db-record="true"
              v-on:uploadFileList="getUploadFileList"
          ></FileUploadCore>
        </div>

        <el-row v-if="editCharacterItem.rdType !== RD_TYPE.RESEARCH.key && projectType === PROJECT_TYPE.DEV_PROJECT.key">
          <div class="hr hr-18 dotted hr-double"></div>
          <el-row>
            <el-col :offset="1" :span="20">
              <el-col :span="12">
                <el-form
                    label-width="150px">
                  <el-form-item
                      label="开发工时">
                    <el-input style="width: 100px;" type="number"  v-model="editCharacterItem.devManhour" placeholder="开发工时"></el-input>
                    <span style="width: 60px; margin-left: 8px;">小时</span>
                    <span style="margin-left: 30px;">剩余: {{ characterBudget.remainDevManhour - editCharacterItem.devManhour }}<span style="width: 20px;">小时</span></span>
                  </el-form-item>
                  <el-form-item
                      label="测试工时">
                    <el-input style="width: 100px;" type="number"  v-model="editCharacterItem.testManhour" placeholder="测试工时"></el-input>
                    <span style="width: 60px; margin-left: 8px;">小时</span>
                    <span style="margin-left: 30px;">剩余: {{ characterBudget.remainTestManhour - editCharacterItem.testManhour }} <span style="width: 20px;">小时</span></span>
                  </el-form-item>
                  <el-form-item
                      label="物料费">
                    <el-input style="width: 100px;" type="number"  v-model="editCharacterItem.materialFee" placeholder="物料费"></el-input>
                    <span style="width: 60px; margin-left: 8px;">元</span>
                    <span style="margin-left: 30px;">剩余: {{ characterBudget.remainMaterialBudget - editCharacterItem.materialFee }} <span style="width: 20px;">元</span></span>
                  </el-form-item>
<!--                  <el-form-item
                      label="设备费">
                    <el-input style="width: 100px;" type="number"  v-model="editCharacterItem.equipmentFee" placeholder="设备费"></el-input>
                    <span style="width: 60px; margin-left: 8px;">元</span>
                    <span style="margin-left: 30px;">剩余: {{ characterBudget.remainEquipmentFee - editCharacterItem.equipmentFee }} <span style="width: 20px;">元</span></span>
                  </el-form-item>-->
                  <el-form-item
                      label="第三方测试费">
                    <el-input style="width: 100px;" type="number"  v-model="editCharacterItem.testFee" placeholder="第三方测试费"></el-input>
                    <span style="width: 60px; margin-left: 8px;">元</span>
                    <span style="margin-left: 30px;">剩余: {{ characterBudget.remainTestFee - editCharacterItem.testFee }} <span style="width: 20px;">元</span></span>
                  </el-form-item>
                  <el-form-item
                      label="动力费">
                    <el-input style="width: 100px;" type="number"  v-model="editCharacterItem.powerFee" placeholder="动力费"></el-input>
                    <span style="width: 60px; margin-left: 8px;">元</span>
                    <span style="margin-left: 30px;">剩余: {{ characterBudget.remainPowerFee - editCharacterItem.powerFee }} <span style="width: 20px;">元</span></span>
                  </el-form-item>
                  <el-form-item
                      label="会议费">
                    <el-input style="width: 100px;" type="number"  v-model="editCharacterItem.conferenceFee" placeholder="会议费"></el-input>
                    <span style="width: 60px; margin-left: 8px;">元</span>
                    <span style="margin-left: 30px;">剩余: {{ characterBudget.remainConferenceFee - editCharacterItem.conferenceFee }} <span style="width: 20px;">元</span></span>
                  </el-form-item>
                </el-form>
              </el-col>
              <el-col :span="12">
                <el-form
                    label-width="150px">
                  <el-form-item
                      label="差旅费">
                    <el-input style="width: 100px;" type="number"  v-model="editCharacterItem.businessFee" placeholder="差旅费"></el-input>
                    <span style="width: 60px; margin-left: 8px;">元</span>
                    <span style="margin-left: 30px;">剩余: {{ characterBudget.remainBusinessFee - editCharacterItem.businessFee }} <span style="width: 20px;">元</span></span>
                  </el-form-item>
                  <el-form-item
                      label="合作费">
                    <el-input style="width: 100px;" type="number"  v-model="editCharacterItem.cooperationFee" placeholder="合作费"></el-input>
                    <span style="width: 60px; margin-left: 8px;">元</span>
                    <span style="margin-left: 30px;">剩余: {{ characterBudget.remainCooperationFee - editCharacterItem.cooperationFee }} <span style="width: 20px;">元</span></span>
                  </el-form-item>
                  <el-form-item
                      label="知识产权费">
                    <el-input style="width: 100px;" type="number"  v-model="editCharacterItem.propertyFee" placeholder="知识产权费"></el-input>
                    <span style="width: 60px; margin-left: 8px;">元</span>
                    <span style="margin-left: 30px;">剩余: {{ characterBudget.remainPropertyFee - editCharacterItem.propertyFee }} <span style="width: 20px;">元</span></span>
                  </el-form-item>
                  <el-form-item
                      label="劳务费">
                    <el-input style="width: 100px;" type="number"  v-model="editCharacterItem.laborFee" placeholder="劳务费"></el-input>
                    <span style="width: 60px; margin-left: 8px;">元</span>
                    <span style="margin-left: 30px;">剩余: {{ characterBudget.remainLaborFee - editCharacterItem.laborFee }} <span style="width: 20px;">元</span></span>
                  </el-form-item>
                  <el-form-item
                      label="专家咨询费">
                    <el-input style="width: 100px;" type="number"  v-model="editCharacterItem.consultingFee" placeholder="专家咨询费"></el-input>
                    <span style="width: 60px; margin-left: 8px;">元</span>
                    <span style="margin-left: 30px;">剩余: {{ characterBudget.remainConsultingFee - editCharacterItem.consultingFee }} <span style="width: 20px;">元</span></span>
                  </el-form-item>
                  <el-form-item
                      label="管理费">
                    <el-input style="width: 100px;" type="number"  v-model="editCharacterItem.managementFee" placeholder="管理费"></el-input>
                    <span style="width: 60px; margin-left: 8px;">元</span>
                    <span style="margin-left: 30px;">剩余: {{ characterBudget.remainManagementFee - editCharacterItem.managementFee }} <span style="width: 20px;">元</span></span>
                  </el-form-item>
<!--                  <el-form-item
                      label="其他费用">
                    <el-input style="width: 100px;" type="number"  v-model="editCharacterItem.otherFee" placeholder="其他费用"></el-input>
                    <span style="width: 60px; margin-left: 8px;">元</span>
                    <span style="margin-left: 30px;">剩余: {{ characterBudget.remainOtherBudget - editCharacterItem.otherFee }} <span style="width: 20px;">元</span></span>
                  </el-form-item>-->
                </el-form>
              </el-col>
            </el-col>
          </el-row>

<!--          <el-col :span="21">
            <div style="display: flex; flex-direction: row; justify-content: space-between; margin-bottom: 5px;">
              <div style="display: flex; flex-direction: row; justify-content: left; align-items: center; line-height: 32px;">
                <span style="width: 120px; text-align: right; margin-right:5px;">开发工时:</span>
                <el-input style="width: 100px;" type="number"  v-model="editCharacterItem.devManhour" placeholder="开发工时"></el-input>
                <span  style="width: 60px; margin-left: 8px;">小时</span>
                <div style="line-height: 10px;">
                  <p>剩余:  {{ characterBudget.remainDevManhour - editCharacterItem.devManhour }}<span style="width: 20px;">小时</span></p>
                </div>
              </div>

              <div style="display: flex; flex-direction: row; align-items: center; line-height: 32px;">
                <span  style="width: 120px; text-align: right; margin-right:5px;">测试工时:</span>
                <el-input style="width: 100px;" type="number"  v-model="editCharacterItem.testManhour" placeholder="测试工时"></el-input>
                <span  style="width: 60px; margin-left: 8px;">小时</span>
                <div style="line-height: 10px;">
                  <p>剩余: {{ characterBudget.remainTestManhour - editCharacterItem.testManhour }} <span style="width: 20px;">小时</span></p>
                </div>
              </div>
            </div>
            <div style="display: flex; flex-direction: row; justify-content: space-between">
              <div style="display: flex; flex-direction: row; align-items: center; line-height: 32px;">
                <span  style="width: 120px; text-align: right; margin-right:5px;">物料费:</span>
                <el-input style="width: 100px;" type="number"  v-model="editCharacterItem.materialFee" placeholder="物料费"></el-input>
                <span  style="width: 60px; margin-left: 8px;">元</span>
                <div style="line-height: 10px;">
                  <p>剩余: {{ characterBudget.remainMaterialBudget - editCharacterItem.materialFee }}<span style="width: 20px;">元</span></p>
                </div>
              </div>
              <div style="display: flex; flex-direction: row; align-items: center; line-height: 32px;">
                <span  style="width: 120px; text-align: right; margin-right:5px;">差旅等费用:</span>
                <el-input style="width: 100px;" type="number"  v-model="editCharacterItem.otherFee" placeholder="差旅等费用"></el-input>
                <span  style="width: 60px; margin-left: 8px;">元</span>
                <div style="line-height: 10px;">
                  <p>剩余: {{ characterBudget.remainOtherBudget - editCharacterItem.otherFee }}<span style="width: 20px;">元</span></p>
                </div>
              </div>
            </div>
          </el-col>-->
        </el-row>
        <div class="hr hr-18 dotted hr-double"></div>
        <el-row>
          <div style="display: flex; justify-content: space-evenly; align-items: center;">
            <div>
              <span>
                填表人 :
              </span>
              <span>
                {{ editCharacterItem.writerName || loginUser.trueName }}
              </span>
            </div>
            <div>
              <span>
                填表日期 :
              </span>
              <span>
                {{ currentTimeStr }}
              </span>
            </div>
          </div>
        </el-row>
      </div>
      <div style="height: 20px;"></div>
      <div class="clearfix form-actions">
        <div style="display: flex; justify-content: center;">
          <button type="button" v-on:click="saveCharacterItem()" class="ca-btn-primary">保 存</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import FileRdms from "../../components/rdms/file-rdms";
import Swal from "sweetalert2";
import FileSaver from 'file-saver';


import CharacterDetailEditCore from "@/components/rdms/character-detail-edit-core";
import FileUploadCore from "@/components/std/file-upload-core.vue";

export default {
  components: {
    FileUploadCore,
    FileRdms,
    CharacterDetailEditCore,
  },
  name: "character-decompose-edit",

  props: {
    characterId: {
      default: null,
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

  computed: {
    getXProjectId(){
      if(this.projectType === PROJECT_TYPE.PRE_PROJECT.key){
        return  this.editCharacterItem.preProjectId;
      }
      if(this.projectType === PROJECT_TYPE.PROJECT.key){
        return  this.editCharacterItem.projectId;
      }
      if(this.projectType === PROJECT_TYPE.SUB_PROJECT.key || this.projectType === PROJECT_TYPE.DEV_PROJECT.key){
        return  this.editCharacterItem.subprojectId;
      }
      return 0;
    },
  },

  data() {
    return {
      loginUser: {},
      loginCustomer: {},

      random: 0,

      editCharacterItem: {
        devManhour: 0,
        testManhour: 0,
        materialFee: 0,
        equipmentFee: 0,
        testFee: 0,
        powerFee: 0,
        conferenceFee: 0,
        businessFee: 0,
        cooperationFee: 0,
        propertyFee: 0,
        laborFee: 0,
        consultingFee: 0,
        managementFee: 0,
        otherFee: 0,
        id: 0,
      },

      characterBudget: {},

      FILE_GROUPING: FILE_GROUPING,
      CHARACTER_STATUS: CHARACTER_STATUS,
      resFile: {},
      resFiles: [],
      resFileIds: [],
      currentTime: {},
      currentTimeStr: '',

      PROJECT_TYPE: PROJECT_TYPE,
      RD_TYPE: RD_TYPE,
    };
  },

  mounted: function () {
    let _this = this;
    _this.getLoginUser();

    if(Tool.isNotEmpty(_this.characterId) && Tool.isNotEmpty(_this.projectType)){
      _this.getRecordAndData(_this.characterId);

    }
    _this.currentTime = new Date();
    _this.currentTimeStr = _this.currentTime.toLocaleString()

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

    getRecordAndData(characterId) {
      let _this = this;
      let formData = new window.FormData();
      formData.append('characterId', characterId);
      formData.append('loginUserId', _this.loginUser.customerUserId);
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/character/getRecordAndData', formData).then((response) => {
        let resp = response.data;
        if (resp.success) {
          _this.editCharacterItem = resp.content;
          if(!_this.editCharacterItem.iterationVersion){
            _this.editCharacterItem.iterationVersion = 1;
          }
          _this.random = Math.random();
          if(_this.projectType === PROJECT_TYPE.DEV_PROJECT.key){
            _this.getBudgetWhenEdit(_this.editCharacterItem.id);
          }
        }else{
          Toast.warning(resp.message, Swal);
        }
      });
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

    getBudgetWhenEdit(characterId){
      let _this = this;
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/budget/getCharacterBudgetWhenEdit/' + characterId).then((response) => {
        let resp = response.data;
        if (resp.success) {
          _this.characterBudget = resp.content;
        }else{
          Toast.warning(resp.message, Swal);
        }
      });
    },

    saveCharacterItem(){
      let _this = this;
      _this.$refs.character_edit.saveCharacterItem();
    },

    saveCharacter(characterInfo) {
      let _this = this;
      // debugger
      if(! characterInfo.iterationVersion || characterInfo.iterationVersion === 0){
        _this.editCharacterItem.iterationVersion = 1;
      }else {
        _this.editCharacterItem.iterationVersion = characterInfo.iterationVersion;
      }
      _this.editCharacterItem.characterName = characterInfo.characterName;
      _this.editCharacterItem.functionDescription = characterInfo.functionDescription;
      _this.editCharacterItem.workCondition = characterInfo.workCondition;
      _this.editCharacterItem.inputLogicalDesc = characterInfo.inputLogicalDesc;
      _this.editCharacterItem.functionLogicalDesc = characterInfo.functionLogicalDesc;
      _this.editCharacterItem.outputLogicalDesc = characterInfo.outputLogicalDesc;
      _this.editCharacterItem.testMethod = characterInfo.testMethod;

      exeSaveCharacter();

      /*if (1 !== 1
          || !Validator.require(_this.editCharacterItem.devManhour, "开发工时", Swal)
          || !Validator.require(_this.editCharacterItem.testManhour, "测试工时", Swal)
          || !Validator.require(_this.editCharacterItem.materialFee, "物料费", Swal)
          || !Validator.require(_this.editCharacterItem.otherFee, "差旅等其他费用", Swal)

          || !Validator.isNumber(_this.editCharacterItem.devManhour, "开发工时", Swal)
          || !Validator.isNumber(_this.editCharacterItem.testManhour, "测试工时", Swal)
          || !Validator.isNumber(_this.editCharacterItem.materialFee, "物料费", Swal)
          || !Validator.isNumber(_this.editCharacterItem.otherFee, "差旅等其他费用", Swal)
      ) {
        return;
      }*/

/*      if(1 !== 1
          || _this.characterBudget.remainDevManhour - _this.editCharacterItem.devManhour < 0
          || _this.characterBudget.remainTestManhour - _this.editCharacterItem.testManhour < 0
          || _this.characterBudget.remainMaterialBudget - _this.editCharacterItem.materialFee < 0
          || _this.characterBudget.remainOtherBudget - _this.editCharacterItem.otherFee < 0
      ){
        Swal.fire({
          title: '预算超支!',
          text: "是否确认提交?",
          icon: 'warning',
          showCancelButton: true,
          confirmButtonColor: '#409EFF',
          cancelButtonColor: '#909399',
          confirmButtonText: '确认!',
          cancelButtonText: '取消!'
        }).then((result) => {
          if (result.value) {
            exeSaveCharacter();
          }
        }).catch(_ => {
        });
      }else{
        exeSaveCharacter();
      }*/

      function exeSaveCharacter() {
        _this.editCharacterItem.customerId = _this.loginCustomer.id;
        _this.editCharacterItem.loginUserId = _this.loginUser.customerUserId;
        _this.editCharacterItem.fileIds = _this.resFileIds;

        _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/character/saveDecomposeCharacter', _this.editCharacterItem).then((response) => {
          let resp = response.data;
          if (resp.success) {
            Toast.success("保存成功", Swal);
            _this.editCharacterItem = {
              devManhour: 0,
              testManhour: 0,
              materialFee: 0,
              otherFee: 0,
            };
            _this.resFiles = [];
            _this.closeDlg();
            _this.$bus.$emit('refresh-character-decompose-create', true);
          } else {
            Toast.warning(resp.message, Swal);
          }
        })
      }
    },

/*    reset(){
      let _this = this;
      _this.editCharacterItem = {
        devManhour: 0,
        testManhour: 0,
        materialFee: 0,
        otherFee: 0,
      };
      _this.resFiles = [];
      if(!_this.editCharacterItem.iterationVersion){
        _this.editCharacterItem.iterationVersion = 1;
      }
    },*/

  },
}
</script>

<style lang="scss" scoped>
@import './../../../public/static/scss/caipd';

</style>
