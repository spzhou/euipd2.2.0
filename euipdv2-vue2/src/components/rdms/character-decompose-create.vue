<!--
 * Author: 周朔鹏
 * Date: 2024-11-17
 * Email: 391902958@qq.com
 * Website: https://www.euipd.com
-->

<template>
  <div  id="myContent" class="item-container" v-on:scroll="myContentScroll" style="display: flex; flex-direction: column;">
    <div style="width: 26.3cm; margin: 0 auto;">
      <div v-if="characterList.length>0">
        <div style="font-weight: bold; font-size: 15px;">功能分解定义文件列表:</div>
        <span v-for="(item, i) in characterList" :key="i" style="font-weight: bold;">
        <a href="javascript:void(0);" @click="editCharacter(item)">《{{ item.characterName}}》</a>
      </span>
      </div>
      <div style="margin-top: 10px;">
        <div style="float: left; margin-top: 20px;">说明: 根据具体情况, 您可以根据需求定义多条功能.</div>
        <el-button
            v-if="characterList.length>0"
            @click="jobItemSubmitDlg" type="primary" style="float: right;">
          分解完成,提交任务
        </el-button>
      </div>
    </div>

    <div class="a4-paper-big-simple" :style="{ height: contentHeight }">
      <div>
        <!--特性编辑表-->
        <div>
          <character-detail-edit-core
              :id="characterItem.id"
              :key="('character-decompose-' + (characterItem.id))"
              ref="character_decompose"
              :save-character-info="decomposeCharacter"
          ></character-detail-edit-core>
        </div>
        <div style="margin: 15px 50px 20px 0">
          <FileUploadCore
              :input-id="('character-decompose-create-' + (characterItem.id))"
              :key="('file-upload-' + (characterItem.id))"
              :x-project-id="getXProjectId"
              :item-id="characterItem.id"
              :file-group="FILE_GROUPING.CHARACTER.key"
              :project-type="projectType"
              :en-delete="true"
              :read-db-record="true"
              v-on:uploadFileList="getUploadFileList"
          ></FileUploadCore>
        </div>
        <div v-if="(jobItem.rdType !== RD_TYPE.RESEARCH.key && projectType === PROJECT_TYPE.DEV_PROJECT.key)">
          <div class="hr hr-18 dotted hr-double"></div>
          <el-row>
            <el-col :offset="1" :span="20">
              <el-col :span="12">
                <el-form
                  label-width="150px">
                  <el-form-item
                      label="开发工时">
                    <el-input style="width: 100px;" type="number"  v-model="characterItem.devManhour" placeholder="开发工时"></el-input>
                    <span style="width: 60px; margin-left: 8px;">小时</span>
                    <span style="margin-left: 30px;">剩余: {{ characterBudget.remainDevManhour - characterItem.devManhour }}<span style="width: 20px;">小时</span></span>
                  </el-form-item>
                  <el-form-item
                      label="测试工时">
                    <el-input style="width: 100px;" type="number"  v-model="characterItem.testManhour" placeholder="测试工时"></el-input>
                    <span style="width: 60px; margin-left: 8px;">小时</span>
                    <span style="margin-left: 30px;">剩余: {{ characterBudget.remainTestManhour - characterItem.testManhour }} <span style="width: 20px;">小时</span></span>
                  </el-form-item>
                  <el-form-item
                      label="物料费">
                    <el-input style="width: 100px;" type="number"  v-model="characterItem.materialFee" placeholder="物料费"></el-input>
                    <span style="width: 60px; margin-left: 8px;">元</span>
                    <span style="margin-left: 30px;">剩余: {{ characterBudget.remainMaterialBudget - characterItem.materialFee }} <span style="width: 20px;">元</span></span>
                  </el-form-item>
<!--                  <el-form-item
                      label="设备费">
                    <el-input style="width: 100px;" type="number"  v-model="characterItem.equipmentFee" placeholder="设备费"></el-input>
                    <span style="width: 60px; margin-left: 8px;">元</span>
                    <span style="margin-left: 30px;">剩余: {{ characterBudget.remainEquipmentFee - characterItem.equipmentFee }} <span style="width: 20px;">元</span></span>
                  </el-form-item>-->
                  <el-form-item
                      label="第三方测试费">
                    <el-input style="width: 100px;" type="number"  v-model="characterItem.testFee" placeholder="第三方测试费"></el-input>
                    <span style="width: 60px; margin-left: 8px;">元</span>
                    <span style="margin-left: 30px;">剩余: {{ characterBudget.remainTestFee - characterItem.testFee }} <span style="width: 20px;">元</span></span>
                  </el-form-item>
                  <el-form-item
                      label="动力费">
                    <el-input style="width: 100px;" type="number"  v-model="characterItem.powerFee" placeholder="动力费"></el-input>
                    <span style="width: 60px; margin-left: 8px;">元</span>
                    <span style="margin-left: 30px;">剩余: {{ characterBudget.remainPowerFee - characterItem.powerFee }} <span style="width: 20px;">元</span></span>
                  </el-form-item>
                  <el-form-item
                      label="会议费">
                    <el-input style="width: 100px;" type="number"  v-model="characterItem.conferenceFee" placeholder="会议费"></el-input>
                    <span style="width: 60px; margin-left: 8px;">元</span>
                    <span style="margin-left: 30px;">剩余: {{ characterBudget.remainConferenceFee - characterItem.conferenceFee }} <span style="width: 20px;">元</span></span>
                  </el-form-item>
                </el-form>
              </el-col>
              <el-col :span="12">
                <el-form
                    label-width="150px">
                  <el-form-item
                      label="差旅费">
                    <el-input style="width: 100px;" type="number"  v-model="characterItem.businessFee" placeholder="差旅费"></el-input>
                    <span style="width: 60px; margin-left: 8px;">元</span>
                    <span style="margin-left: 30px;">剩余: {{ characterBudget.remainBusinessFee - characterItem.businessFee }} <span style="width: 20px;">元</span></span>
                  </el-form-item>
                  <el-form-item
                      label="合作费">
                    <el-input style="width: 100px;" type="number"  v-model="characterItem.cooperationFee" placeholder="合作费"></el-input>
                    <span style="width: 60px; margin-left: 8px;">元</span>
                    <span style="margin-left: 30px;">剩余: {{ characterBudget.remainCooperationFee - characterItem.cooperationFee }} <span style="width: 20px;">元</span></span>
                  </el-form-item>
                  <el-form-item
                      label="知识产权费">
                    <el-input style="width: 100px;" type="number"  v-model="characterItem.propertyFee" placeholder="知识产权费"></el-input>
                    <span style="width: 60px; margin-left: 8px;">元</span>
                    <span style="margin-left: 30px;">剩余: {{ characterBudget.remainPropertyFee - characterItem.propertyFee }} <span style="width: 20px;">元</span></span>
                  </el-form-item>
                  <el-form-item
                      label="劳务费">
                    <el-input style="width: 100px;" type="number"  v-model="characterItem.laborFee" placeholder="劳务费"></el-input>
                    <span style="width: 60px; margin-left: 8px;">元</span>
                    <span style="margin-left: 30px;">剩余: {{ characterBudget.remainLaborFee - characterItem.laborFee }} <span style="width: 20px;">元</span></span>
                  </el-form-item>
                  <el-form-item
                      label="专家咨询费">
                    <el-input style="width: 100px;" type="number"  v-model="characterItem.consultingFee" placeholder="专家咨询费"></el-input>
                    <span style="width: 60px; margin-left: 8px;">元</span>
                    <span style="margin-left: 30px;">剩余: {{ characterBudget.remainConsultingFee - characterItem.consultingFee }} <span style="width: 20px;">元</span></span>
                  </el-form-item>
                  <el-form-item
                      label="管理费">
                    <el-input style="width: 100px;" type="number"  v-model="characterItem.managementFee" placeholder="管理费"></el-input>
                    <span style="width: 60px; margin-left: 8px;">元</span>
                    <span style="margin-left: 30px;">剩余: {{ characterBudget.remainManagementFee - characterItem.managementFee }} <span style="width: 20px;">元</span></span>
                  </el-form-item>
<!--                  <el-form-item
                      label="其他费用">
                    <el-input style="width: 100px;" type="number"  v-model="characterItem.otherFee" placeholder="其他费用"></el-input>
                    <span style="width: 60px; margin-left: 8px;">元</span>
                    <span style="margin-left: 30px;">剩余: {{ characterBudget.remainOtherBudget - characterItem.otherFee }} <span style="width: 20px;">元</span></span>
                  </el-form-item>-->
                </el-form>
              </el-col>
            </el-col>
          </el-row>

          <div style="text-align: right; margin-top: 10px; margin-right: 100px;">资金概要: 特性总预算: {{ characterBudget.budget | rmbFormat }}元, <span :class="[ getRemainBudget < 0 ? redColor : '']">可分配预算: {{ getRemainBudget | rmbFormat }}元</span> . </div>
          <div style="text-align: right; margin-top: 10px; margin-right: 100px;">
            <small><em>( 备注: 如果预算分配有富余, 不必全部分配出去, 在项目工时统计时, 会作为资金池在子项目内根据其他工时调整所需进行调配. )</em></small>
          </div>
        </div>

        <div class="hr hr-18 dotted"></div>
        <el-row>
          <div style="display: flex; justify-content: space-evenly; align-items: center;">
            <div>
              <span>
                填表人 :
              </span>
              <span>
                {{ loginUser.trueName }}
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
          <button type="button" v-on:click="saveDecomposeCharacter()" class="ca-btn-primary">保 存</button>
        </div>
      </div>
    </div>

    <!--对新分解的功能特性进行再编辑-->
    <el-dialog
        title="编辑特性定义表"
        :visible.sync="dialogVisible"
        width="32cm"
        :close-on-click-modal=false
        :before-close="handleClose">
      <div v-if="editCharacterItem">
        <CharacterDecomposeEdit
            :key="editCharacterItem.id + random"
            :character-id=editCharacterItem.id
            :job-type="jobType"
            :project-type="projectType"
            :index="index"
            :entrance="entrance"
            :close-dlg="closeDialog"
        ></CharacterDecomposeEdit>
      </div>

    </el-dialog>

    <!--任务提交 对话框-->
    <el-dialog
        title="任务提交"
        width="29cm"
        :visible.sync="jobItemSubmitVisible"
        :close-on-click-modal=false>
      <div>
        <jobitem-submit-decompose-dlg
            :key="('submit- ' + (jobItem.id))"
            ref="jobItemSubmit_dlg"
            :job-item-in="jobItem"
            :project-type="projectType"
            :index="index"
            :entrance="entrance"
            :close-dlg="submitDlgClose">
        </jobitem-submit-decompose-dlg>
      </div>
    </el-dialog>

  </div>
</template>

<script>
import FileRdms from "../../components/rdms/file-rdms";
import CharacterDecomposeEdit from "@/components/rdms/character-decompose-edit";
import JobitemDetailInfoDlg from "@/components/std/jobitem-detail-info-dlg.vue";
import JobitemSubmitDecomposeDlg from "@/components/rdms/jobitem-submit-decompose-dlg";

import Swal from "sweetalert2";
import FileSaver from 'file-saver';

import CharacterDetailEditCore from "@/components/rdms/character-detail-edit-core";
import FileUploadCore from "@/components/std/file-upload-core.vue";
// import jobitem from "@/views/rdms/execute/jobitem.vue";

export default {
  components: {
    FileUploadCore,
    FileRdms,
    CharacterDecomposeEdit,
    JobitemDetailInfoDlg,
    JobitemSubmitDecomposeDlg,
    CharacterDetailEditCore,
  },

  name: "character-decompose-create",

  props: {
    jobitemId: {
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
    refresh: {
      type: Function,
      default: null
    },
  },

  computed: {
    // jobitem() {
    //   return jobitem
    // },
    contentHeight(){
      return this.myContentHeight+120 +'px';
    },

    getRemainBudget(){
      let _this = this;
      return _this.characterBudget.remainBudget
          - _this.characterItem.devManhour * _this.characterBudget.standDevManhourFee
          - _this.characterItem.testManhour * _this.characterBudget.standTestManhourFee
          - _this.characterItem.materialFee
          - _this.characterItem.testFee
          - _this.characterItem.powerFee
          - _this.characterItem.conferenceFee
          - _this.characterItem.businessFee
          - _this.characterItem.cooperationFee
          - _this.characterItem.propertyFee
          - _this.characterItem.laborFee
          - _this.characterItem.consultingFee
          - _this.characterItem.managementFee
    },

    getXProjectId(){
      if(this.projectType === PROJECT_TYPE.PRE_PROJECT.key){
        return  this.jobItem.preProjectId;
      }
      if(this.projectType === PROJECT_TYPE.PROJECT.key){
        return  this.jobItem.projectId;
      }
      if(this.projectType === PROJECT_TYPE.SUB_PROJECT.key || this.projectType === PROJECT_TYPE.DEV_PROJECT.key){
        return  this.jobItem.subprojectId;
      }
      return 0;
    },
  },

  data() {
    return {
      loginUser: {},
      loginCustomer: {},

      jobItem: {},
      jobItemSubmitVisible: false,

      parentCharacterId: '',
      characterItem: {
        devManhour: 0,
        testManhour: 0,
        materialFee: 0,
        testFee: 0,
        powerFee: 0,
        conferenceFee: 0,
        businessFee: 0,
        cooperationFee: 0,
        propertyFee: 0,
        laborFee: 0,
        consultingFee: 0,
        managementFee: 0,
        id: 0,
      },
      editCharacterItem: {},

      dialogVisible: false,
      random: 0,
      redColor: 'red-color',

      characterBudget: {},
      characterList: [],

      myContentHeight: 1000,

      RD_TYPE: RD_TYPE,
      FILE_GROUPING: FILE_GROUPING,
      CHARACTER_STATUS: CHARACTER_STATUS,
      resFile: {},
      resFiles: [],
      resFileIds: [],
      currentTime: {},
      currentTimeStr: '',

      PROJECT_TYPE: PROJECT_TYPE,

      warningShowed: false,
    };
  },

  mounted: function () {
    let _this = this;
    _this.random = Math.random();
    _this.getLoginUser();
    if(Tool.isNotEmpty(_this.jobitemId)){
      _this.getJobitemDetailInfo(_this.jobitemId);
    }

    _this.currentTime = new Date();
    _this.currentTimeStr = _this.currentTime.toLocaleString()
    _this.characterItem.id = Uuid.shortUuid(8);

    _this.myContentHeight = document.getElementById("myContent").scrollHeight;

    _this.$bus.$on('refresh-character-decompose-create', (data) => {
      if(data && Tool.isNotEmpty(_this.parentCharacterId)){
        _this.getBudget(_this.parentCharacterId);
      }
    });

  },

  methods: {
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

    closeDialog(){
      let _this = this;
      _this.dialogVisible = false;
      },

    submitDlgClose(){
      let _this = this;
      _this.jobItemSubmitVisible = false;
      },

    editCharacter(item){
      let _this = this;
      _this.random = Math.random();
      _this.dialogVisible = true;
      _this.editCharacterItem = item;
    },

    jobItemSubmitDlg(){
      let _this = this;
      _this.jobItemSubmitVisible = true;
    },

    handleClose(done) {
      let _this = this;
      Swal.fire({
        title: '确认',
        text: "如果有修订, 关闭前请及时保存",
        icon: 'question',
        showCancelButton: true,
        confirmButtonColor: '#409EFF',
        cancelButtonColor: '#909399',
        confirmButtonText: '确认!',
        cancelButtonText: '取消!'
      }).then((result) => {
        if (result.isConfirmed) {
          _this.dialogVisible = false;
          done();
          _this.refresh();
        }
      }).catch(_ => {
      });
    },

    myContentScroll(){
      let _this = this;
      _this.myContentHeight = document.getElementById("myContent").scrollHeight;
      // console.log("myContentHeight",_this.myContentHeight);
    },

    getLoginUser(){
      let _this = this;
      if(Tool.getLoginUser().userGroup === LOGIN_USER_GROUP.CUSTOMER.key){
        _this.loginUser = Tool.getLoginUser().adminUser;
        // console.log("loginUser:", _this.loginUser);
        _this.loginCustomer =Tool.getLoginUser();
      }else{
        _this.loginUser = Tool.getLoginUser();
        _this.loginCustomer = Tool.getLoginUser().customer;
      }
    },

    getBudget(parentCharacterId){
      let _this = this;
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/budget/getCharacterBudget_decompose/' + parentCharacterId).then((response) => {
        let resp = response.data;
        if (resp.success) {
          _this.characterBudget = resp.content;
        }else{
          Toast.warning(resp.message, Swal);
        }
      });
    },

    getDecomposeCharacterList(parentCharacterId){
      let _this = this;
      let formData = new window.FormData();
      formData.append('parentCharacterId', parentCharacterId);
      formData.append('loginUserId', _this.loginUser.customerUserId);
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/character/getDecomposedChildrenCharacters', formData).then((response) => {
        let resp = response.data;
        if (resp.success) {
          _this.characterList = resp.content;
          // debugger
        }else{
          Toast.warning(resp.message, Swal);
        }
      });
    },

    getJobitemDetailInfo(jobitemId){
      let _this = this;
      let formData = new window.FormData();
      formData.append('jobitemId', jobitemId);
      formData.append('loginUserId', _this.loginUser.customerUserId);
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/job-items/getJobitemDetailInfo', formData).then((response) => {
        let resp = response.data;
        if (resp.success) {
          _this.jobItem = resp.content;
          if(_this.jobItem.type === 'DECOMPOSE'){
            if(_this.jobItem.projectType === 'DEV_PROJECT'){
              _this.parentCharacterId = _this.jobItem.characterId;
              _this.getBudget(_this.parentCharacterId);
              _this.getDecomposeCharacterList(_this.parentCharacterId);
            }

            if(_this.jobItem.projectType === 'PRE_PROJECT'){
              _this.parentCharacterId = _this.jobItem.characterId;
              _this.getDecomposeCharacterList(_this.parentCharacterId);
            }

            _this.characterItem.id = Uuid.shortUuid(8);
            _this.characterItem.devManhour = 0;
            _this.characterItem.testManhour = 0;
            _this.characterItem.materialFee = 0;
            _this.characterItem.testFee = 0;
            _this.characterItem.powerFee = 0;
            _this.characterItem.conferenceFee = 0;
            _this.characterItem.businessFee = 0;
            _this.characterItem.cooperationFee = 0;
            _this.characterItem.propertyFee = 0;
            _this.characterItem.laborFee = 0;
            _this.characterItem.consultingFee = 0;
            _this.characterItem.managementFee = 0;
          }
        }else{
          Toast.warning(resp.message, Swal);
        }
      });
    },

    /**
     * 保存客户信息
     */
    saveDecomposeCharacter(){
      let _this = this;
      _this.$refs.character_decompose.saveCharacterItem();
    },

    decomposeCharacter(characterInfo) {
      let _this = this;
      if(_this.getRemainBudget < 0){
        Swal.fire({
          title: '确认',
          text: "特性预算超支! 请做好沟通工作, 否则可能在复核审批时被驳回. 确认提交吗?",
          icon: 'warning',
          showCancelButton: true,
          confirmButtonColor: '#409EFF',
          cancelButtonColor: '#909399',
          confirmButtonText: '确认!',
          cancelButtonText: '取消!'
        }).then((result) => {
          if (result.isConfirmed) {
            _this.warningShowed = true;
            saveDecomposeCharacter();
          }else{
            //什么都不做!
          }
        }).catch(_ => {
        });
      }else{
        saveDecomposeCharacter();
      }

      function saveDecomposeCharacter() {
        _this.characterItem.characterName = characterInfo.characterName;
        _this.characterItem.functionDescription = characterInfo.functionDescription;
        _this.characterItem.workCondition = characterInfo.workCondition;
        _this.characterItem.inputLogicalDesc = characterInfo.inputLogicalDesc;
        _this.characterItem.functionLogicalDesc = characterInfo.functionLogicalDesc;
        _this.characterItem.outputLogicalDesc = characterInfo.outputLogicalDesc;
        _this.characterItem.testMethod = characterInfo.testMethod;

        exeSaveCharacter();
      }

      function exeSaveCharacter() {
        _this.characterItem.customerId = _this.jobItem.customerId;
        _this.characterItem.milestoneId = _this.jobItem.characterDto.milestoneId;
        _this.characterItem.demandListStr = _this.jobItem.characterDto.demandListStr;

        _this.characterItem.projectId = _this.jobItem.projectId;
        _this.characterItem.preProjectId = _this.jobItem.preProjectId;
        _this.characterItem.subprojectId = _this.jobItem.subprojectId;
        _this.characterItem.productManagerId = _this.jobItem.productManagerId;
        _this.characterItem.writerId = _this.loginUser.customerUserId;
        _this.characterItem.loginUserId = _this.loginUser.customerUserId;
        _this.characterItem.fileIds = _this.resFileIds;
        _this.characterItem.parent = _this.jobItem.characterId;  //重要代码 --- 父级Character的ID
        _this.characterItem.jobitemId = _this.jobItem.id; //产生本条功能所对应的任务ID
        _this.characterItem.projectType = _this.projectType; //产生本条功能所对应的任务ID

        _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/character/saveDecomposeCharacter', _this.characterItem).then((response) => {
          let resp = response.data;
          if (resp.success) {
            Toast.success("保存成功", Swal);
            _this.characterItem = {};
            _this.characterItem.id = Uuid.shortUuid(8);
            _this.resFiles = [];
            _this.refresh(); //刷新组建
          } else {
            Toast.warning(resp.message, Swal);
          }
        })
      }
    },

  },
}
</script>

<style lang="scss" scoped>
@import './../../../public/static/scss/caipd';

.item-container {
  height: 100%;
  width: 100%;
  //margin: 0 2px 0 12px;
  //padding-right: 10px;
  overflow-y: scroll;
  display: -webkit-box; /* 老版本语法: Safari, iOS, Android browser, older WebKit browsers. */
  display: -moz-box; /* 老版本语法: Firefox (buggy) */
  display: -ms-flexbox; /* 混合版本语法: IE 10 */
  display: -webkit-flex; /* 新版本语法: Chrome 21+ */
  display: flex; /* 新版本语法: Opera 12.1, Firefox 22+ */

}

.red-color{
  color: red;
}
</style>
