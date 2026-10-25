<!--
 * Author: 周朔鹏
 * Date: 2025-04-18
 * Email: 391902958@qq.com
 * Website: https://www.euipd.com
-->

<template>
  <div>
    <div class="a4-paper-big-simple">
      <div style="margin: 50px 60px 20px">
        <div style="font-size: 22px; text-align: center; width: 100%;">
          特性评审申请表
        </div>
        <div class="hr hr-9 dotted"></div>
        <div style="margin: 0 30px;">
          <el-form>
            <el-row>
              <el-col :span="8">
                <el-form-item label="特性名称:">
                  {{ character.characterName }}
                </el-form-item>
              </el-col>
              <el-col :span="6">
                <el-form-item label="评审组长:">
                  {{ groupLeader }}
                </el-form-item>
              </el-col>
              <el-col :span="6">
                <el-form-item label="项目经理:">
                  {{ character.projectManagerName }}
                </el-form-item>
              </el-col>
              <el-col :span="4">
                <el-form-item label="产品经理:">
                  {{ character.productManagerName }}
                </el-form-item>
              </el-col>
            </el-row>
          </el-form>
        </div>
        <div style="margin: 10px 10px;">
          <div class="hr hr-9 dotted"></div>
          <div style="font-size: 14px; line-height: 28px; margin: 5px 0;">
            特性开发执行信息表:
          </div>
          <el-table
              :data="characterExeInfoList"
              style="width: 100%;margin-bottom: 20px;"
              row-key="id"
              border
              :tree-props="{children: 'children', hasChildren: 'hasChildren'}">
            <el-table-column
                prop="typeName"
                align="center"
                label="支出分类">
            </el-table-column>
            <el-table-column
                prop="itemNum"
                align="center"
                label="任务数量(单)">
            </el-table-column>
            <el-table-column
                prop="completeItemNum"
                align="center"
                label="完成数量(单)">
            </el-table-column>
            <el-table-column
                prop="sumManhour"
                :formatter="manhourFormat"
                align="center"
                label="工时累计(小时)">
            </el-table-column>
            <el-table-column
                prop="itemBudget"
                :formatter="itemBudgetFormat"
                align="center"
                label="分项预算(元)">
            </el-table-column>
            <el-table-column
                prop="activeCharge"
                :formatter="activeChargeFormat"
                align="center"
                label="已执行(元)">
            </el-table-column>
            <el-table-column
                prop="budgetRate"
                :formatter="budgetRateFormat"
                align="center"
                label="预算执行率(%)">
            </el-table-column>
          </el-table>
          <div style="width: 100%; display: flex; flex-direction: column; align-items: flex-end;">
            <div>
              <i class="el-icon-info"></i>
              <small style="margin-left: 3px;">备注: 上表物料单数仅统计费用化领料单. 其他类型领料单在评审中会给出详单.</small>
            </div>
            <div>
              <i class="el-icon-info"></i>
              <small style="margin-left: 3px;">备注: 表单显示工时为标准工时.</small>
            </div>
          </div>
        </div>
        <div class="hr hr-18 dotted"></div>
        <div style="margin-left: 15px;">
          <div style="font-size: 14px; line-height: 28px; margin: 5px 0; font-weight: 600">
            <span v-if="!character.bomStatus || character.bomStatus === CHARACTER_BOM_STATUS.EDIT.key">
              编辑特性物料清单:
              <el-button type="primary" size="small" style="margin-left: 5px;" @click="characterBomEdit('edit')">BOM编辑</el-button>
            </span>
            <span v-else>
              BOM编辑状态:
              <i v-if="character.bomStatus && character.bomStatus === CHARACTER_BOM_STATUS.SUBMIT.key" class="el-icon-check" style="font-size: 16px; font-weight: bold; color: green;"></i>
              已提交
              <span>
                <el-button type="primary" size="small" style="margin-left: 10px;" @click="characterBomEdit('view')">
                  查看BOM
                </el-button>
              </span>
            </span>
          </div>
        </div>
        <div class="hr hr-18 dotted hr-double"></div>
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
                  <el-col>
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
          <div>
            <div class="hr 9 hr-dotted"></div>
            <div style="margin: 20px 0; font-size: 14px;">
              资产盘点表打印及签署: <small><em>(在提交评审之前,请打印下列资产盘点表, 协同相关部门盘点签署后, 作为附件, 在本表单下方作为附件上传扫描件.)</em></small>
            </div>
            <div style="width: 100%; display: flex; flex-direction: row; justify-content: space-evenly;">
              <div>
                <el-button type="primary" @click="characterExpenditureCheckSheet(characterId)">打印费用化料盘点表</el-button>
                <el-button type="primary" @click="characterCapitalizationCheckSheet(characterId)">打印资产化物料盘点表</el-button>
                <el-button type="primary" @click="characterBorrowCheckSheet(characterId)">打印借用物料盘点表</el-button>
              </div>
            </div>
            <div style="display: flex; flex-direction: column; align-items: center;">
              <div style=" margin-top: 20px;">
                <i class="el-icon-info"></i>
                <small style="margin-left: 3px;">备注: 上述资产盘点表全部签署完成, 作为附件上传后, 再提交本评审申请表.</small>
              </div>
              <div style=" margin-top: 1px;">
                <i class="el-icon-info"></i>
                <small style="margin-left: 3px;">备注: 如果申领的资产化物料, 已经完成资产化处理; 如果借用物料已经归还, 则不在评审过程中体现.</small>
              </div>
            </div>
          </div>
          <div class="hr hr-9 hr-dotted"></div>
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
          <div v-if="intCompleteFlag && (character.bomStatus === CHARACTER_BOM_STATUS.SUBMIT.key) ">
            <el-button type="primary" v-on:click="saveJobItem()">提交特性评审申请表</el-button>
          </div>
          <div v-else style="display: flex; flex-direction: column; justify-content: center; align-items: center;">
            <div style="margin-bottom: 10px;">
              <small>( 提交评审的前提条件是: 1. 所有任务全部完成, 包括特性集成工单(<i :class="intStatusClass"></i>); 2. 所有"费用化"物料处理全部完成(<i :class="materialStatusClass"></i>); 3. 所有"费用申请"处理完成(<i :class="feeStatusClass"></i>); 4. 在物料管理模块中, 完成BOM编制(<i :class="bomStatusClass"></i>). )</small>
            </div>
            <el-button type="info" disabled>提交特性评审申请表</el-button>
          </div>
        </div>
      </div>
    </div>

    <!--资产化物料盘点表-->
    <el-dialog
        width="29cm"
        :visible.sync="capitalizationCheckSheetDlg"
        :before-close="handleClose"
        :close-on-click-modal=false>
      <div slot="title" style="font-size: 20px;">{{ dialogTitle }}</div>
      <div v-if="capitalizationCheckSheetDlg">
        <MaterialCapitalizationCheckListDlg
            :key="character.id"
            :x-id="character.id"
            :doc-type="DOC_TYPE.CHARACTER.key"
            :close-dlg = closeDialog
        ></MaterialCapitalizationCheckListDlg>
      </div>
    </el-dialog>

    <!--借用物料盘点表-->
    <el-dialog
        width="29cm"
        :visible.sync="borrowCheckSheetDlg"
        :before-close="handleClose"
        :close-on-click-modal=false>
      <div slot="title" style="font-size: 20px;">{{ dialogTitle }}</div>
      <div v-if="borrowCheckSheetDlg">
        <MaterialBorrowCheckListDlg
            :key="character.id"
            :x-id="character.id"
            :doc-type="DOC_TYPE.CHARACTER.key"
            :close-dlg = closeDialog
        ></MaterialBorrowCheckListDlg>
      </div>
    </el-dialog>

    <!--费用化物料盘点表-->
    <el-dialog
        width="29cm"
        :visible.sync="expenditureCheckSheetDlg"
        :before-close="handleClose"
        :close-on-click-modal=false>
      <div slot="title" style="font-size: 20px;">{{ dialogTitle }}</div>
      <div v-if="expenditureCheckSheetDlg">
        <MaterialExpenditureCheckListDlg
            :key="character.id"
            :x-id="character.id"
            :doc-type="DOC_TYPE.CHARACTER.key"
            :close-dlg = closeDialog
        ></MaterialExpenditureCheckListDlg>
      </div>
    </el-dialog>

    <!--BOM编辑-->
    <el-dialog
        width="29cm"
        :visible.sync="characterBomEditDlg"
        :before-close="handleClose"
        append-to-body
        :close-on-click-modal=false>
      <div slot="title" style="font-size: 20px;">{{ dialogTitle }}</div>
      <div v-if="characterBomEditDlg">
        <BomEditList
            :key="characterId"
            :character-id="characterId"
            :close-dlg="closeDialog">
        </BomEditList>
      </div>
    </el-dialog>

  </div>
</template>

<script>
import FileRdms from "../rdms/file-rdms.vue";
import Swal from "sweetalert2";
import {rmbFormat} from "@/filter/filter";
import MaterialCapitalizationCheckListDlg from "@/components/std/material-capitalization-check-list-dlg.vue";
import MaterialBorrowCheckListDlg from "@/components/std/material-borrow-check-list-dlg.vue";
import MaterialExpenditureCheckListDlg from "@/components/std/material-expenditure-check-list-dlg.vue";
import BomEditList from "@/components/std/bom-edit-list.vue";

export default {
  components: {
    BomEditList,
    MaterialCapitalizationCheckListDlg,
    MaterialBorrowCheckListDlg,
    MaterialExpenditureCheckListDlg,
    FileRdms,
  },
  name: "review-create-page",

  props: {
    characterId:{
      default: null,
    },
/*    docType: {
      default: null,
    },*/
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
      characterBomEditDlg: false,

      groupLeader: '',

      productManagerName: '',
      workerNameStr: '',
      reviewItem: {},

      dialogInfo: {},
      value: [],
      workers: [],
      workerList: [],
      titles: ['可选人员','评审委员'],

      characterExeInfoList: [],

      jobItem: {},

      character: {},
      project: {},
      subProject: {},

      intCompleteFlag: false,

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
      DOC_TYPE: DOC_TYPE,
      RD_TYPE: RD_TYPE,
      CHARACTER_BOM_STATUS: CHARACTER_BOM_STATUS,
      G_SUFFIXS: G_SUFFIXS,

      capitalizationCheckSheetDlg: false,
      borrowCheckSheetDlg: false,
      expenditureCheckSheetDlg: false,

      xIdForCheckDlg: '',
      reviewDocType: '',  // character subproject or project
      dialogTitle: '',
      characterCompleteStatus: {},

      pickerOptions: {
        disabledDate(v) {
          return v.getTime() < new Date().getTime() - 86400000;//  - 86400000是否包括当天
        }
      },
    };
  },

  computed: {
    intStatusClass() {
      if (!this.characterCompleteStatus) {
        return {'el-icon-close' : true};
      }
      const isComplete = this.characterCompleteStatus.intComplete;
      return {
        'el-icon-check': isComplete,
        'el-icon-close': !isComplete
      };
    },
    materialStatusClass() {
      const status = this.characterCompleteStatus;
      if (!status) {
        return {'el-icon-close' : true};
      }

      const isMaterialComplete = status.materialCompleteNum === status.materialApplicationNum;

      return {
        'el-icon-check': isMaterialComplete,
        'el-icon-close': !isMaterialComplete
      };
    },

    feeStatusClass() {
      const status = this.characterCompleteStatus;
      if (!status) return {'el-icon-close' : true};

      const isFeeComplete = status.feeCompleteNum === status.feeApplicationNum;

      return {
        'el-icon-check': isFeeComplete,
        'el-icon-close': !isFeeComplete
      };
    },
    bomStatusClass() {
      // 空值检查
      const bomStatus = this.character?.bomStatus;

      // 提取条件判断到变量中
      const isSubmitStatus = bomStatus === CHARACTER_BOM_STATUS.SUBMIT.key;

      return {
        'el-icon-check': isSubmitStatus,
        'el-icon-close': !isSubmitStatus
      };
    }

  },

  mounted: function () {
    let _this = this;
    _this.intCompleteFlag = false;

    _this.getLoginUser();
    _this.jobItem.id = Uuid.shortUuid(8);

    if(Tool.isNotEmpty(_this.characterId)){
      _this.getCharacter(_this.characterId);
      _this.getCharacterManhourExeInfoList(_this.characterId);
      _this.getCharacterDevelopCompleteStatus(_this.characterId);
    }

    _this.$bus.$on('refresh-review-create-page', (data) => {
      if(data){
        _this.getCharacter(_this.characterId);
        _this.getCharacterManhourExeInfoList(_this.characterId);
        _this.getCharacterDevelopCompleteStatus(_this.characterId);
      }
    });
  },

  beforeDestroy(){
    let _this = this;
    _this.$bus.$off('refresh-review-create-page');
  },

  methods: {
    manhourFormat(row, column) {
      if (row.sumManhour) {
        return row.sumManhour.toFixed(2);
      }
      return 0;
    },

    itemBudgetFormat(row, column) {
      if (row.itemBudget) {
        return rmbFormat(row.itemBudget);
      }
      return 0;
    },

    activeChargeFormat(row, column) {
      if (row.activeCharge) {
        return rmbFormat(row.activeCharge);
      }
      return 0;
    },

    budgetRateFormat(row, column) {
      if (row.budgetRate) {
        return (row.budgetRate * 100).toFixed(2);  //保留两位小数
      }
      return 0;
    },

    characterBomEdit(flag) {
      let _this = this;
      _this.characterBomEditDlg = true;
      if(flag==='edit'){
        _this.dialogTitle = "BOM编辑";
      }else{
        _this.dialogTitle = "BOM查看";
      }
    },

    characterCapitalizationCheckSheet(characterId) {
      let _this = this;
      _this.xIdForCheckDlg = characterId;
      _this.reviewDocType = DOC_TYPE.CHARACTER.key;
      _this.capitalizationCheckSheetDlg = true;
      _this.dialogTitle = "资产化物料盘点表";
    },

    characterBorrowCheckSheet(characterId) {
      let _this = this;
      _this.xIdForCheckDlg = characterId;
      _this.reviewDocType = DOC_TYPE.CHARACTER.key;
      _this.borrowCheckSheetDlg = true;
      _this.dialogTitle = "借用物料盘点表";
    },

    characterExpenditureCheckSheet(characterId) {
      let _this = this;
      _this.xIdForCheckDlg = characterId;
      _this.reviewDocType = DOC_TYPE.CHARACTER.key;
      _this.expenditureCheckSheetDlg = true;
      _this.dialogTitle = "费用化物料盘点表";
    },

    handleClose(done) {
      let _this = this;
      _this.capitalizationCheckSheetDlg = false;
      _this.borrowCheckSheetDlg = false;
      _this.expenditureCheckSheetDlg = false;
      _this.characterBomEditDlg = false;
      done();
    },

    closeDialog() {
      let _this = this;
      _this.capitalizationCheckSheetDlg = false;
      _this.borrowCheckSheetDlg = false;
      _this.expenditureCheckSheetDlg = false;
      _this.characterBomEditDlg = false;
    },

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

    getCharacter(characterId) {
      let _this = this;
      let formData = new window.FormData();
      formData.append('characterId', characterId);
      formData.append('loginUserId', _this.loginUser.customerUserId);
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/character/getCharacterRecordDetailById', formData).then((response) => {
        let resp = response.data;
        if (resp.success) {
          _this.character = resp.content;
          _this.groupLeader = _this.character.productManagerName;
        }else{
          Toast.warning(resp.message, Swal);
        }
      });
    },

    getCharacterManhourExeInfoList(characterId) {
      let _this = this;
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/character/getCharacterManhourExeInfoList/' + characterId).then((response) => {
        let resp = response.data;
        if (resp.success) {
          _this.characterExeInfoList = resp.content;
          _this.groupLeader = _this.character.productManagerName;
        }else{
          Toast.warning(resp.message, Swal);
        }
      });
    },

    getCharacterDevelopCompleteStatus(characterId) {
      let _this = this;
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/character/getCharacterDevelopCompleteStatus/' + characterId).then((response)=>{
        let resp = response.data;
        if (resp.success) {
          _this.characterCompleteStatus = resp.content;
          _this.intCompleteFlag = _this.characterCompleteStatus?.completeStatus;
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

          || !Validator.length(_this.dialogInfo.jobName, "评审标题", 5, 50,Swal)
          || !Validator.length(_this.dialogInfo.taskDescription, "任务描述", 10, 500,Swal)
      ) {
        return;
      }
      if(Tool.isEmpty(_this.value) || Tool.isEmpty_ES5(_this.value)){
        Toast.warning("评审委员不能为空", Swal);
        return;
      }

      _this.jobItem.characterId = _this.character.id;
      _this.jobItem.productManagerId = _this.character.productManagerId;
      _this.jobItem.type = JOB_ITEM_TYPE.REVIEW.key;   //创建评审任务  固定参数
      _this.jobItem.manhour = 0;

      _this.jobItem.jobName = _this.dialogInfo.jobName;
      _this.jobItem.taskDescription = _this.dialogInfo.taskDescription;
      // _this.jobItem.planSubmissionTime = _this.dialogInfo.planSubmissionTime;

      _this.jobItem.customerId = _this.loginCustomer.id;

      _this.jobItem.fileList = _this.resFiles;
      _this.jobItem.reviewWorkerIdList = _this.value;
      _this.jobItem.loginUserId = _this.loginUser.customerUserId;

      _this.jobItem.docType = DOC_TYPE.CHARACTER.key;
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
              _this.$bus.$emit('notify-character-review-count-flag', true);  //评审管理
              _this.$bus.$emit('notify-project-job-flag', true);   //项目管理

              _this.$bus.$emit('refresh-develop-character-task-tab', true);
              _this.$bus.$emit('refresh-develop-character-task-tab-num', true);
              _this.$bus.$emit('notify-character-review-count-flag', true);
              _this.$bus.$emit('close-develop-tab', _this.index);

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
