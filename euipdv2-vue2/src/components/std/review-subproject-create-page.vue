<!--
 * Author: 周朔鹏
 * Date: 2025-02-19
 * Email: 391902958@qq.com
 * Website: https://www.euipd.com
-->

<template>
  <div>
    <div class="a4-paper-big-simple">
      <div style="margin: 50px 60px 20px">
        <div style="font-size: 22px; text-align: center; width: 100%;">
          子项目评审申请表
        </div>
        <div class="hr hr-9 dotted"></div>
        <div style="margin: 0 30px;">
          <el-form>
            <el-row>
              <el-col :span="8">
                <el-form-item label="项目名称:">
                  {{ subProject.label }}
                </el-form-item>
              </el-col>
              <el-col :span="6">
                <el-form-item label="评审组长:">
                  {{ groupLeader }}
                </el-form-item>
              </el-col>
              <el-col :span="6">
                <el-form-item label="项目经理:">
                  {{ subProject.projectManagerName }}
                </el-form-item>
              </el-col>
              <el-col :span="4">
                <el-form-item label="产品经理:">
                  {{ subProject.productManagerName }}
                </el-form-item>
              </el-col>
            </el-row>
          </el-form>
        </div>
        <div v-if="subProject.rdType !== RD_TYPE.RESEARCH.key" style="margin: 10px 10px;">
          <div class="hr hr-9 dotted"></div>
<!--          <div style="font-size: 14px; line-height: 28px; margin: 5px 0; font-weight: 600">
            项目总体预算执行情况:
          </div>
          <el-form label-width="110px">
            <el-form-item label="项目名称:">
              {{ subprojectExeInfo.itemName }}
            </el-form-item>
            <el-form-item label="预算执行信息:">
              <div style="display: flex; flex-direction: row; justify-content: flex-start;">
                <div style="margin-right: 40px;">
                  审批预算<small><em>(含下级子项目)</em></small>: {{ subprojectExeInfo.sumBudgetApproved | rmbFormat }}
                </div>
                <div style="margin-right: 40px;">
                  已执行: {{ subprojectExeInfo.sumBudgetActive | rmbFormat  }}
                </div>
                <div>
                  预算执行率: {{ subprojectExeInfo.budgetRate * 100 | fixed2fractional}}%
                </div>
              </div>
            </el-form-item>
          </el-form>-->
          <div v-if="subprojectExeInfo.childSubprojectBudgetInfoList && subprojectExeInfo.childSubprojectBudgetInfoList.length>0">
            <div class="hr hr-6 hr-dotted"></div>
            <div style="font-size: 14px; line-height: 28px; margin: 5px 0; font-weight: 600">
              下级子项目开发预算执行情况:
            </div>
            <el-table
                :data="subprojectExeInfo.childSubprojectBudgetInfoList"
                style="width: 100%;margin-bottom: 20px;"
                row-key="id"
                border
                :tree-props="{children: 'children', hasChildren: 'hasChildren'}">
              <el-table-column
                  prop="itemName"
                  align="center"
                  show-overflow-tooltip
                  width="150"
                  label="特性名称">
              </el-table-column>
              <el-table-column
                  prop="sumBudgetApproved"
                  :formatter="sumBudgetApprovedFormat"
                  show-overflow-tooltip
                  width="100"
                  align="center"
                  label="总预算">
              </el-table-column>
              <el-table-column
                  prop="sumBudgetActive"
                  :formatter="sumBudgetActiveFormat"
                  show-overflow-tooltip
                  width="100"
                  align="center"
                  label="已执行">
              </el-table-column>
              <el-table-column
                  prop="budgetRate"
                  :formatter="budgetRateFormat"
                  align="center"
                  width="110"
                  label="预算执行率(%)">
              </el-table-column>
              <el-table-column
                  prop="sumManhourBudget"
                  :formatter="manhourApprovedFormat"
                  align="center"
                  width="110"
                  label="人工费(预算)">
              </el-table-column>
              <el-table-column
                  prop="sumActiveManhourBudget"
                  :formatter="manhourActiveFormat"
                  align="center"
                  width="110"
                  label="人工费(实际)">
              </el-table-column>
              <el-table-column
                  prop="materialFeeApproved"
                  :formatter="materialApproveFormat"
                  width="100"
                  align="center"
                  label="物料费(预算)">
              </el-table-column>
              <el-table-column
                  prop="materialFeeActive"
                  :formatter="materialActiveFormat"
                  width="100"
                  align="center"
                  label="物料费(实际)">
              </el-table-column>
              <el-table-column
                  prop="otherFeeApproved"
                  :formatter="feeApproveFormat"
                  width="110"
                  align="center"
                  label="其他费用 (预算)">
              </el-table-column>
              <el-table-column
                  prop="otherFeeActive"
                  :formatter="feeActiveFormat"
                  width="110"
                  align="center"
                  :label="('其他费用(实际)')">
              </el-table-column>
            </el-table>
          </div>
          <div v-if="subprojectExeInfo.characterBudgetInfoList && subprojectExeInfo.characterBudgetInfoList.length>0">
            <div class="hr hr-6 hr-dotted"></div>
            <div style="font-size: 14px; line-height: 28px; margin: 5px 0; font-weight: 600">
              特性清单预算执行情况 <small><em style="font-weight: normal"> ( 操作指引: 您可以点击对应的行, 查看相应特性的全部详细信息. ) </em></small>:
            </div>
            <el-table
                :data="subprojectExeInfo.characterBudgetInfoList"
                style="width: 100%;margin-bottom: 20px;"
                row-key="id"
                border
                stripe
                highlight-current-row
                @row-click="rowClick"
                :tree-props="{children: 'children', hasChildren: 'hasChildren'}">
              <el-table-column
                  prop="itemName"
                  align="center"
                  show-overflow-tooltip
                  width="150"
                  label="特性名称">
              </el-table-column>
              <el-table-column
                  prop="sumBudgetApproved"
                  :formatter="sumBudgetApprovedFormat"
                  show-overflow-tooltip
                  width="100"
                  align="center"
                  label="总预算">
              </el-table-column>
              <el-table-column
                  prop="sumBudgetActive"
                  :formatter="sumBudgetActiveFormat"
                  show-overflow-tooltip
                  width="100"
                  align="center"
                  label="已执行">
              </el-table-column>
              <el-table-column
                  prop="budgetRate"
                  :formatter="budgetRateFormat"
                  align="center"
                  width="80"
                  label="预算执行率(%)">
              </el-table-column>
              <el-table-column
                  prop="devManhourApproved"
                  :formatter="devManhourApprovedFormat"
                  align="center"
                  width="80"
                  label="开发工时(预算)">
              </el-table-column>
              <el-table-column
                  prop="devManhourActive"
                  :formatter="devManhourActiveFormat"
                  align="center"
                  width="80"
                  label="开发工时(已执行)">
              </el-table-column>
              <el-table-column
                  prop="testManhourApproved"
                  :formatter="testManhourApprovedFormat"
                  align="center"
                  width="80"
                  label="测试工时(预算)">
              </el-table-column>
              <el-table-column
                  prop="testManhourActive"
                  align="center"
                  :formatter="testManhourFormat"
                  width="80"
                  label="测试工时(已执行)">
              </el-table-column>
              <el-table-column
                  prop="materialFeeApproved"
                  :formatter="materialApproveFormat"
                  width="100"
                  align="center"
                  label="物料费(预算)">
              </el-table-column>
              <el-table-column
                  prop="materialFeeActive"
                  :formatter="materialActiveFormat"
                  width="100"
                  align="center"
                  label="物料费(实际)">
              </el-table-column>
              <el-table-column
                  prop="otherFeeApproved"
                  :formatter="feeApproveFormat"
                  width="100"
                  align="center"
                  label="其他费用 (预算)">
              </el-table-column>
              <el-table-column
                  prop="otherFeeActive"
                  :formatter="feeActiveFormat"
                  width="100"
                  align="center"
                  :label="('其他费用(实际)')">
              </el-table-column>
            </el-table>
          </div>
        </div>
        <div class="hr hr-10 dotted"></div>
        <div style="margin-left: 15px;">
          <div style="font-size: 14px; line-height: 28px; margin: 5px 0; font-weight: 600">
            <span v-if="!subProject.bomStatus || subProject.bomStatus === CHARACTER_BOM_STATUS.EDIT.key">
              编辑项目BOM:
              <el-button type="primary" size="small" style="margin-left: 5px;" @click="projectBomEdit('edit')">BOM编辑</el-button>
            </span>
            <span v-else>
              BOM编辑状态:
              <i v-if="subProject.bomStatus && subProject.bomStatus === CHARACTER_BOM_STATUS.SUBMIT.key" class="el-icon-check" style="font-size: 16px; font-weight: bold; color: green;"></i>
              已提交
              <span>
                <el-button type="primary" size="small" style="margin-left: 10px;" @click="projectBomEdit('view')">
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
          <div class="hr 9 hr-dotted"></div>
          <div style="display: flex; flex-direction: row; justify-content: center; align-items: center;">
            <div style="display: flex; flex-direction: row; justify-content: center; align-items: center;">
              <div style="margin-right: 40px;">
                当期预算支出总额: {{ sumBudgetExe | rmbFormat }} 元
              </div>
              <el-button type="primary" size="small" @click="budgetExecution">添加预算执行条目</el-button>
            </div>
            <div style="margin-left: 30px;">
              <el-button size="small" @click="showBudgetStatisticsInfo">项目预算执行汇总表</el-button>
            </div>
            <div v-if="subProject.projectId === subProject.id" style="margin-left: 30px;">
              <el-button size="small" @click="showBudgetStatisticsInfo_main">"主项目"预算执行汇总表</el-button>
            </div>
            <div style="margin-left: 30px;">
              <el-button size="small" @click="feeSummaryList">费用清单</el-button>
            </div>
          </div>
          <div class="hr 9 hr-dotted"></div>
          <div>
            <div style="margin: 20px 0; font-size: 14px;">
              资产盘点表打印及签署: <small><em>(在提交评审之前,请打印下列资产盘点表, 协同相关部门盘点签署后, 作为附件, 在本表单下方作为附件上传扫描件.)</em></small>
            </div>
            <div style="width: 100%; display: flex; flex-direction: row; justify-content: space-evenly;">
              <div>
                <el-button type="primary" @click="subProjectExpenditureCheckSheet(subprojectId)">打印费用化料盘点表</el-button>
                <el-button type="primary" @click="subProjectCapitalizationCheckSheet(subprojectId)">打印资产化物料盘点表</el-button>
                <el-button type="primary" @click="subProjectBorrowCheckSheet(subprojectId)">打印借用物料盘点表</el-button>
              </div>
            </div>
            <div style="display: flex; flex-direction: column; align-items: center;">
              <div style="margin-top: 20px;">
                <i class="el-icon-info"></i>
                <small style="margin-left: 3px;">备注: 上述资产盘点表全部签署完成, 作为附件上传后, 再提交本评审申请表.</small>
              </div>
              <div style="margin-top: 1px;">
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
                <file-rdms v-bind:input-id="'file-upload-job-item-iteration'"
                           v-bind:text="'上传附件'"
                           v-bind:suffixs=G_SUFFIXS
                           v-bind:customer="loginCustomer.id"
                           v-bind:use="FILE_GROUPING.JOBITEM.key"
                           v-bind:folder="'jobitem'"
                           v-bind:after-upload="afterUpload">
                </file-rdms>
              </div>
              <div>
                <small style="float: right; margin: 5px 0;"><em>说明: 请上传所有线下经过签署的文件扫描件. 包括但不限于物料盘点表等.</em></small>
              </div>
            </el-col>
          </el-row>
        </el-form>
      </div>
      <div class="clearfix form-actions">
        <div style="display: flex; justify-content: center;">
          <div>
            <div><small><em>评审的条件是: 1. 项目项下所有子项目全部评审完成; 2. 项目项下所有特性开发全部完成; 3. 项目项下所有任务任务全部执行完成; 4. BOM编辑完成</em></small></div>
            <div style="display: flex; justify-content: center; margin-top: 10px;">
              <div v-if="intCompleteFlag || (subProject.status === PROJECT_STATUS.OUT_SOURCE.key && subProject.bomStatus === CHARACTER_BOM_STATUS.SUBMIT.key)">
                <el-button type="primary" v-on:click="saveJobItem()">提交子项目评审申请表</el-button>
              </div>
              <div v-else>
                <el-button type="primary" plain disabled >提交子项目评审申请表</el-button>
              </div>

            </div>
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
      <div>
        <MaterialCapitalizationCheckListDlg
            :key="xIdForCheckDlg"
            :x-id="xIdForCheckDlg"
            :doc-type="DOC_TYPE.SUB_PROJECT.key"
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
      <div>
        <MaterialBorrowCheckListDlg
            :key="xIdForCheckDlg"
            :x-id="xIdForCheckDlg"
            :doc-type="DOC_TYPE.SUB_PROJECT.key"
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
      <div>
        <MaterialExpenditureCheckListDlg
            :key="xIdForCheckDlg"
            :x-id="xIdForCheckDlg"
            :doc-type="DOC_TYPE.SUB_PROJECT.key"
            :close-dlg = closeDialog
        ></MaterialExpenditureCheckListDlg>
      </div>
    </el-dialog>

    <!--显示character Doc 信息-->
    <el-dialog
        width="29cm"
        :visible.sync="characterDocInfoDlg"
        :before-close="handleClose"
        append-to-body
        :close-on-click-modal=false>
      <div slot="title" style="font-size: 20px;">{{ dialogTitle }}</div>
      <div>
        <CharacterDocInfoPage
            :key="characterExeInfo.id"
            :character-id="characterExeInfo.id">
        </CharacterDocInfoPage>
      </div>
    </el-dialog>

    <!--BOM编辑-->
    <el-dialog
        width="32cm"
        :visible.sync="projectBomEditDlg"
        :before-close="handleClose"
        append-to-body
        :close-on-click-modal=false>
      <div slot="title" style="font-size: 20px;">{{ dialogTitle }}</div>
      <div>
        <BomEditListSubprojectDlg
            :key="subprojectId"
            :subproject-id="subprojectId"
            :close-dlg="closeDialog">
        </BomEditListSubprojectDlg>
      </div>
    </el-dialog>

    <!--填写预算执行表-->
    <el-dialog
        width="29cm"
        :visible.sync="budgetExecutionDlg"
        :before-close="handleClose"
        :close-on-click-modal=false>
      <div slot="title" style="font-size: 20px;">{{ dialogTitle }}</div>
      <div>
        <BudgetExecutionSubprojectDlg
            :key="((subprojectId))"
            :subproject-id="subprojectId"
            :close-dlg = closeDialog
        ></BudgetExecutionSubprojectDlg>
      </div>
    </el-dialog>

    <!--查看预算执行汇总表-->
    <el-dialog
        width="40cm"
        :visible.sync="showStatisticsDlg"
        destroy-on-close
        :before-close="handleClose"
        :close-on-click-modal=false>
      <div slot="title" style="font-size: 20px;">{{ dialogTitle }}</div>
      <div>
        <budgetDirectStatisticsPage
            :key="((subprojectId) + '-' + (projectType) + '-' + (random))"
            :x-project-id="subprojectId"
            :project-type="projectType"
        ></budgetDirectStatisticsPage>
      </div>
    </el-dialog>

    <!--查看预算执行汇总表-->
    <el-dialog
        width="40cm"
        :visible.sync="showStatisticsDlg_main"
        destroy-on-close
        :before-close="handleClose"
        :close-on-click-modal=false>
      <div slot="title" style="font-size: 20px;">{{ dialogTitle }}</div>
      <div>
        <budgetDirectStatisticsPage
            :key="((subprojectId) + '-' + (projectType) + '-' + (random))"
            :x-project-id="subprojectId"
            :project-type="PROJECT_TYPE.PROJECT.key"
        ></budgetDirectStatisticsPage>
      </div>
    </el-dialog>

    <!--费用清单-->
    <el-dialog
        width="32cm"
        :visible.sync="feeSummaryListDlg"
        destroy-on-close
        :before-close="handleClose"
        :close-on-click-modal=false>
      <div slot="title" style="font-size: 20px;">{{ dialogTitle }}</div>
      <div>
        <SubprojectFeeSummaryPage
            :key="((subprojectId) + '-' + (getProjectType) + '-' + (random))"
            :subproject-id="subprojectId"
            :project-type="getProjectType"
        ></SubprojectFeeSummaryPage>
      </div>
    </el-dialog>

  </div>
</template>

<script>
import FileRdms from "../rdms/file-rdms.vue";
import Swal from "sweetalert2";
import {fixed2fractional, rmbFormat} from "@/filter/filter";
import MaterialCapitalizationCheckListDlg from "@/components/std/material-capitalization-check-list-dlg.vue";
import MaterialBorrowCheckListDlg from "@/components/std/material-borrow-check-list-dlg.vue";
import MaterialExpenditureCheckListDlg from "@/components/std/material-expenditure-check-list-dlg.vue";
import CharacterDocInfoPage from "@/components/std/character-doc-info-page.vue";
import BomEditListSubprojectDlg from "@/components/std/bom-edit-list-subproject-dlg.vue";
import BudgetExecutionSubprojectDlg from "@/components/std/budget-execution-subproject-dlg.vue";
import BudgetExecutionStatisticsDlg from "@/components/std/budget-execution-statistics-dlg.vue";
import budgetDirectStatisticsPage from "@/components/std/budget-direct-statistics-page.vue";
import SubprojectFeeSummaryPage from "@/components/std/subproject-fee-summary-page.vue";

export default {
  components: {
    SubprojectFeeSummaryPage,
    budgetDirectStatisticsPage,
    BudgetExecutionStatisticsDlg,
    BudgetExecutionSubprojectDlg,
    CharacterDocInfoPage,
    MaterialCapitalizationCheckListDlg,
    MaterialBorrowCheckListDlg,
    MaterialExpenditureCheckListDlg,
    FileRdms,
    BomEditListSubprojectDlg,
  },
  name: "review-subproject-create-page",

  props: {
    subprojectId:{
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

      groupLeader: '',

      productManagerName: '',
      workerNameStr: '',
      reviewItem: {},

      dialogInfo: {},
      value: [],
      workers: [],
      workerList: [],
      titles: ['可选人员','评审委员'],

      subprojectExeInfo: [],
      characterExeInfoList: [],

      characterDocInfoDlg: false,
      projectBomEditDlg: false,
      characterExeInfo: {},

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
      PROJECT_STATUS: PROJECT_STATUS,
      G_SUFFIXS: G_SUFFIXS,

      capitalizationCheckSheetDlg: false,
      borrowCheckSheetDlg: false,
      expenditureCheckSheetDlg: false,

      xIdForCheckDlg: '',
      reviewDocType: '',  // character subproject or project
      dialogTitle: '',

      sumBudgetExe: 0,
      budgetExecutionDlg: false,
      showStatisticsDlg: false,
      showStatisticsDlg_main: false,
      feeSummaryListDlg: false,
      random: 0,

      pickerOptions: {
        disabledDate(v) {
          return v.getTime() < new Date().getTime() - 86400000;//  - 86400000是否包括当天
        }
      },
    };
  },

  computed: {
    getProjectType(){
      let _this = this;
      if(_this.subProject.id === _this.subProject.projectId){
        return PROJECT_TYPE.PROJECT.key;
      }else{
        return PROJECT_TYPE.SUB_PROJECT.key;
      }
    }
  },

  mounted: function () {
    let _this = this;
    _this.intCompleteFlag = false;

    _this.getLoginUser();
    _this.jobItem.id = Uuid.shortUuid(8);

    if(Tool.isNotEmpty(_this.subprojectId)){
      _this.getSubProjectInfo(_this.subprojectId);
      _this.getSumBudgetBySubprojectId(_this.subprojectId);
    }

    _this.$bus.$on('refresh-review-subproject-create-page', (data) => {
      if(data){
        _this.getSubProjectInfo(_this.subprojectId);
        _this.getSumBudgetBySubprojectId(_this.subprojectId);
        _this.random = Math.random();
      }
    });
  },

  beforeDestroy(){
    let _this = this;
    _this.$bus.$off('refresh-review-subproject-create-page');
  },

  methods: {
    fixed2fractional,

    getSumBudgetBySubprojectId(subprojectId) {
      let _this = this;
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/budget-direct-exe/getSumBudgetBySubprojectId/' + subprojectId).then((response)=>{
        let resp = response.data;
        if (resp.success) {
          _this.sumBudgetExe = resp.content;
        }else{
          Toast.warning(resp.message, Swal);
        }
      })
    },

    budgetExecution() {
      let _this = this;
      _this.budgetExecutionDlg = true;
      _this.dialogTitle = "填写预算执行表";
    },

    showBudgetStatisticsInfo() {
      let _this = this;
      _this.random = Math.random();
      _this.showStatisticsDlg = true;
      _this.dialogTitle = "查看预算执行汇总表";
    },

    showBudgetStatisticsInfo_main() {
      let _this = this;
      _this.timeStamp = Date.now();
      _this.showStatisticsDlg_main = true;
      _this.dialogTitle = "查看预算执行汇总表";
    },

    feeSummaryList() {
      let _this = this;
      _this.feeSummaryListDlg = true;
      _this.dialogTitle = "费用清单";
    },

    sumBudgetApprovedFormat(row, column) {
      if (row.sumBudgetApproved) {
        return rmbFormat(row.sumBudgetApproved);
      }
      return 0;
    },

    sumBudgetActiveFormat(row, column) {
      if (row.sumBudgetActive) {
        return rmbFormat(row.sumBudgetActive);
      }
      return 0;
    },

    budgetRateFormat(row, column) {
      if (row.budgetRate) {
        return (row.budgetRate * 100).toFixed(2);  //保留两位小数
      }
      return 0;
    },

    materialApproveFormat(row, column) {
      if (row.materialFeeApproved) {
        return rmbFormat(row.materialFeeApproved);
      }
      return 0;
    },

    materialActiveFormat(row, column) {
      if (row.materialFeeActive) {
        return rmbFormat(row.materialFeeActive);
      }
      return 0;
    },

    feeApproveFormat(row, column) {
      if (row.otherFeeApproved) {
        return rmbFormat(row.otherFeeApproved);
      }
      return 0;
    },

    feeActiveFormat(row, column) {
      if (row.otherFeeActive) {
        return rmbFormat(row.otherFeeActive);
      }
      return 0;
    },

    manhourApprovedFormat(row, column) {
      if (row.sumManhourBudget) {
        return row.sumManhourBudget.toFixed(2);  //保留两位小数
      }
      return 0;
    },

    manhourActiveFormat(row, column) {
      if (row.sumActiveManhourBudget) {
        return row.sumActiveManhourBudget.toFixed(2);  //保留两位小数
      }
      return 0;
    },



    devManhourApprovedFormat(row, column) {
      if (row.devManhourApproved) {
        return row.devManhourApproved.toFixed(2);  //保留两位小数
      }
      return 0;
    },

    devManhourActiveFormat(row, column) {
      if (row.devManhourActive) {
        return row.devManhourActive.toFixed(2);  //保留两位小数
      }
      return 0;
    },


    testManhourApprovedFormat(row, column) {
      if (row.testManhourApproved) {
        return row.testManhourApproved.toFixed(2);  //保留两位小数
      }
      return 0;
    },

    testManhourFormat(row, column) {
      if (row.testManhourActive) {
        return row.testManhourActive.toFixed(2);  //保留两位小数
      }
      return 0;
    },

    subProjectCapitalizationCheckSheet(subprojectId) {
      let _this = this;
      _this.xIdForCheckDlg = subprojectId;
      _this.reviewDocType = DOC_TYPE.SUB_PROJECT.key;
      _this.capitalizationCheckSheetDlg = true;
      _this.dialogTitle = "资产化物料盘点表";
    },

    subProjectBorrowCheckSheet(subprojectId) {
      let _this = this;
      _this.xIdForCheckDlg = subprojectId;
      _this.reviewDocType = DOC_TYPE.SUB_PROJECT.key;
      _this.borrowCheckSheetDlg = true;
      _this.dialogTitle = "借用物料盘点表";
    },

    subProjectExpenditureCheckSheet(subprojectId) {
      let _this = this;
      _this.xIdForCheckDlg = subprojectId;
      _this.reviewDocType = DOC_TYPE.SUB_PROJECT.key;
      _this.expenditureCheckSheetDlg = true;
      _this.dialogTitle = "费用化物料盘点表";
    },

    projectBomEdit(flag) {
      let _this = this;
      _this.projectBomEditDlg = true;
      if(flag==='edit'){
        _this.dialogTitle = "项目BOM编辑";
      }else{
        _this.dialogTitle = "项目BOM查看";
      }
    },

    rowClick(row, column, e){
      let _this = this;
      _this.characterExeInfo = row;
      _this.characterDocInfoDlg = true;
      _this.dialogTitle = "特性文档汇总";
    },

    handleClose(done) {
      let _this = this;
      _this.capitalizationCheckSheetDlg = false;
      _this.borrowCheckSheetDlg = false;
      _this.expenditureCheckSheetDlg = false;
      _this.characterDocInfoDlg = false;
      _this.projectBomEditDlg = false;
      _this.budgetExecutionDlg = false;
      _this.showStatisticsDlg = false;
      _this.showStatisticsDlg_main = false;
      _this.feeSummaryListDlg = false;
      done();
    },

    closeDialog() {
      let _this = this;
      _this.capitalizationCheckSheetDlg = false;
      _this.borrowCheckSheetDlg = false;
      _this.expenditureCheckSheetDlg = false;
      _this.characterDocInfoDlg = false;
      _this.projectBomEditDlg = false;
      _this.budgetExecutionDlg = false;
      _this.showStatisticsDlg = false;
      _this.showStatisticsDlg_main = false;
      _this.feeSummaryListDlg = false;
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

    getSubProjectInfo(subprojectId){
      let _this = this;
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/sub-project/getSubProjectSimpleInfo/' + subprojectId).then((response) => {
        let resp = response.data;
        if (resp.success) {
          _this.subProject = resp.content;
          _this.groupLeader = _this.subProject.productManagerName; //子项目评审的组长是产品经理
          _this.getSubprojectBudgetExeInfoList(subprojectId)
          _this.getSubprojectIntCompleteStatus(subprojectId)
        } else {
          Toast.warning(resp.message, Swal);
        }
      })
    },

    getSubprojectIntCompleteStatus(subprojectId) {
      let _this = this;
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/sub-project/getSubprojectIntCompleteStatus/' + subprojectId).then((response)=>{
        let resp = response.data;
        if (resp.success) {
          _this.intCompleteFlag = resp.content;
        } else {
          Toast.warning(resp.message,Swal);
        }
      })
    },

    getSubprojectBudgetExeInfoList(subprojectId) {
      let _this = this;
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/sub-project/getSubprojectBudgetExeSummary_researchExe/' + subprojectId).then((response) => {
        let resp = response.data;
        if (resp.success) {
          _this.subprojectExeInfo = resp.content;
          _this.characterExeInfoList = resp.content.children;

        }else{
          Toast.warning(resp.message, Swal);
        }
      });
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

          || !Validator.length(_this.dialogInfo.jobName, "评审标题", 5, 50,Swal)
          || !Validator.length(_this.dialogInfo.taskDescription, "任务描述", 10, 500,Swal)
      ) {
        return;
      }
      if(Tool.isEmpty(_this.value) || Tool.isEmpty_ES5(_this.value)){
        Toast.warning("评审委员不能为空", Swal);
        return;
      }
      _this.jobItem.projectId = _this.subProject.projectId;
      _this.jobItem.subprojectId = _this.subProject.id;
      _this.jobItem.productManagerId = _this.subProject.productManagerId;
      _this.jobItem.type = JOB_ITEM_TYPE.REVIEW_SUBP.key;   //创建评审任务  固定参数
      _this.jobItem.manhour = 0;

      _this.jobItem.jobName = _this.dialogInfo.jobName;
      _this.jobItem.taskDescription = _this.dialogInfo.taskDescription;

      _this.jobItem.customerId = _this.loginCustomer.id;

      _this.jobItem.fileList = _this.resFiles;
      _this.jobItem.reviewWorkerIdList = _this.value;
      _this.jobItem.loginUserId = _this.loginUser.customerUserId;

      _this.jobItem.docType = DOC_TYPE.SUB_PROJECT.key;
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

              if(_this.entrance === ENTRANCE_CLASS.PROJECT.key){
                if(_this.projectType === PROJECT_TYPE.SUB_PROJECT.key){
                  _this.$bus.$emit('close-project-develop-tab', _this.index);
                  _this.$bus.$emit('refresh-project-list', true);
                }
              }
              if(_this.entrance === ENTRANCE_CLASS.DEVELOP.key){
                if(_this.projectType === PROJECT_TYPE.SUB_PROJECT.key){
                  _this.$bus.$emit('refresh-subproject-task-tab', true);
                  _this.$bus.$emit('refresh-subproject-task-tab-num', true);
                  _this.$bus.$emit('close-subproject-task-tab', _this.index);
                }
              }
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

.jobItem{
  width: 100%;
}
</style>
