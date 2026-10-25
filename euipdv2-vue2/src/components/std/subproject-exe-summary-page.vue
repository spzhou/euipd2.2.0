<!--
 * Author: 周朔鹏
 * Date: 2025-02-26
 * Email: 391902958@qq.com
 * Website: https://www.euipd.com
-->

<template>
  <div style="height: 100%;">
    <div class="paper-height">
      <div style="height: 100%; overflow: scroll;">
        <div class="a4-paper-big-simple">
          <div style="margin: 50px 60px;">
            <div style="font-size: 14px; line-height: 28px; margin: 5px 0; font-weight: bold">
              项目开发及预算执行信息概览:
            </div>
            <div>
              <div class="hr hr-9 dotted"></div>
              <div style="margin: 10px 10px;">
                <div style="font-size: 14px; line-height: 28px; margin: 5px 0; font-weight: 600">
                  项目总体预算执行情况:
                </div>
                <el-form label-width="110px">
                  <el-form-item label="项目名称:">
                    {{ subprojectExeInfo.itemName }}
                  </el-form-item>

<!--TODO 以下注释掉的代码块的数据统计需要重新确认-->
<!--                  <el-form-item label="预算执行信息:">
                    <div style="display: flex; flex-direction: row; justify-content: flex-start;">
                      <div style="margin-right: 40px;">
                        审批预算 <small><em>(含下级子项目)</em></small>: {{ subprojectExeInfo.sumBudgetApproved | rmbFormat }}
                      </div>
                      <div style="margin-right: 40px;">
                        已执行: {{ subprojectExeInfo.sumBudgetActive | rmbFormat  }}
                      </div>
                      <div>
                        预算执行率: {{ subprojectExeInfo.budgetRate * 100 | fixed2fractional}}%
                      </div>
                    </div>
                  </el-form-item>-->
                </el-form>
                <div class="hr hr-6 hr-dotted"></div>
                <div v-if="subprojectExeInfo.childSubprojectBudgetInfoList && subprojectExeInfo.childSubprojectBudgetInfoList.length>0">
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
<!--                  <div style="width: 100%; text-align: right;">
                    <i class="el-icon-info"></i>
                    <small style="margin-left: 3px;">备注: 上表物料单数仅统计费用化领料单. 其他类型领料单在评审中会给出详单.</small>
                  </div>-->
                </div>
                <div v-if="subprojectExeInfo.characterBudgetInfoList && subprojectExeInfo.characterBudgetInfoList.length>0">
                  <div class="hr hr-6 hr-dotted"></div>
                  <div style="font-size: 14px; line-height: 28px; margin: 5px 0; font-weight: 600">
                    特性清单及预算执行情况 <small><em style="font-weight: normal"> ( 操作指引: 您可以点击对应的行, 查看相应特性的全部详细信息. ) </em></small>:
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
                        :formatter="testManhourActiveFormat"
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
<!--                  <div style="width: 100%; text-align: right;">
                    <i class="el-icon-info"></i>
                    <small style="margin-left: 3px;">备注: 上表物料单数仅统计费用化领料单. 其他类型领料单在评审中会给出详单.</small>
                  </div>-->
                </div>
              </div>
            </div>
            <div class="hr hr-10 hr-dotted hr-double"></div>
            <!--费用化物料盘点表-->
            <div class="ca-form">
              <div class="ca-form-header">
                <div style="font-size: 14px; font-weight: bold; margin-top: 20px;">
                  费用化物料盘点表:
                </div>
                <div class="hr hr-6 dotted"></div>
              </div>
              <table style="width: 100%;" class="ca-table" v-if="expenditureMaterialList">
                <tr class="ca-table-header">
                  <th class="ca-table-header-column-cell" style="width: 40px;">
                    序号
                  </th>
                  <th class="ca-table-header-column-cell" style="width: 180px;">
                    申请单号
                  </th>
                  <th class="ca-table-header-column-cell" style="width: 180px;">
                    物料编码
                  </th>
                  <th class="ca-table-header-column-cell" style="width: 100px;">
                    型号
                  </th>
                  <th class="ca-table-header-column-cell" style="width: 65px;">
                    单位
                  </th>
                  <th class="ca-table-header-column-cell" style="width: 65px;">
                    数量
                  </th>
                  <th class="ca-table-header-column-cell" style="width: 85px;">
                    小计/元
                  </th>
                  <th class="ca-table-header-column-cell" style="width: 85px;">
                    领料人
                  </th>
                </tr>
                <tr class="ca-table-content" v-for="(item, index) in expenditureMaterialList" :key="index">
                  <td class="ca-table-content-column-cell" style="width: 40px;">
                    {{ index + 1 }}
                  </td>
                  <td class="ca-table-content-column-cell" style="width: 180px;">
                    {{ item.code }}
                  </td>
                  <td class="ca-table-content-column-cell" style="width: 180px;">
                    {{ item.bomCode }}
                  </td>
                  <td class="ca-table-content-column-cell" style="width: 100px;">
                    {{ item.model }}
                  </td>
                  <td class="ca-table-content-column-cell" style="width: 65px;">
                    {{ item.unit }}
                  </td>
                  <td class="ca-table-content-column-cell" style="width: 65px;">
                    {{ item.amount }}
                  </td>
                  <td class="ca-table-content-column-cell" style="width: 85px;">
                    {{ item.preTaxSumPrice }}
                  </td>
                  <td class="ca-table-content-column-cell" style="width: 85px;">
                    {{ item.writerName }}
                  </td>
                </tr>
                <tr class="ca-table-sum">
                  <td class="ca-table-sum-column-cell">
                    总计
                  </td>
                  <td class="ca-table-sum-column-cell">
                  </td>
                  <td class="ca-table-sum-column-cell">
                  </td>
                  <td class="ca-table-sum-column-cell">
                  </td>
                  <td class="ca-table-sum-column-cell">
                  </td>
                  <td class="ca-table-sum-column-cell">
                  </td>
                  <td class="ca-table-sum-column-cell">
                    {{ getExpenditureSumPrice | rmbFormat }}
                  </td>
                  <td class="ca-table-sum-column-cell">
                    {{ '' }}
                  </td>
                </tr>
              </table>
            </div>

            <!--资产化物料盘点表-->
            <div class="ca-form">
              <div class="ca-form-header">
                  <div style="font-size: 14px; font-weight: bold; margin-top: 20px;">
                    资产化物料盘点表:
                  </div>
                <div class="hr hr-6 dotted"></div>
              </div>
              <table style="width: 100%;" class="ca-table" v-if="capitalizationMaterialList">
                <tr class="ca-table-header">
                  <th class="ca-table-header-column-cell" style="width: 40px;">
                    序号
                  </th>
                  <th class="ca-table-header-column-cell" style="width: 180px;">
                    申请单号
                  </th>
                  <th class="ca-table-header-column-cell" style="width: 180px;">
                    物料编码
                  </th>
                  <th class="ca-table-header-column-cell" style="width: 100px;">
                    型号
                  </th>
                  <th class="ca-table-header-column-cell" style="width: 65px;">
                    单位
                  </th>
                  <th class="ca-table-header-column-cell" style="width: 65px;">
                    数量
                  </th>
                  <th class="ca-table-header-column-cell" style="width: 85px;">
                    小计/元
                  </th>
                  <th class="ca-table-header-column-cell" style="width: 85px;">
                    领料人
                  </th>
                </tr>
                <tr class="ca-table-content" v-for="(item, index) in capitalizationMaterialList" :key="index">
                  <td class="ca-table-content-column-cell" style="width: 40px;">
                    {{ index + 1 }}
                  </td>
                  <td class="ca-table-content-column-cell" style="width: 180px;">
                    {{ item.code }}
                  </td>
                  <td class="ca-table-content-column-cell" style="width: 180px;">
                    {{ item.bomCode }}
                  </td>
                  <td class="ca-table-content-column-cell" style="width: 100px;">
                    {{ item.model }}
                  </td>
                  <td class="ca-table-content-column-cell" style="width: 65px;">
                    {{ item.unit }}
                  </td>
                  <td class="ca-table-content-column-cell" style="width: 65px;">
                    {{ item.amount }}
                  </td>
                  <td class="ca-table-content-column-cell" style="width: 85px;">
                    {{ item.preTaxSumPrice }}
                  </td>
                  <td class="ca-table-content-column-cell" style="width: 85px;">
                    {{ item.writerName }}
                  </td>
                </tr>
                <tr class="ca-table-sum">
                  <td class="ca-table-sum-column-cell">
                    总计
                  </td>
                  <td class="ca-table-sum-column-cell">
                  </td>
                  <td class="ca-table-sum-column-cell">
                  </td>
                  <td class="ca-table-sum-column-cell">
                  </td>
                  <td class="ca-table-sum-column-cell">
                  </td>
                  <td class="ca-table-sum-column-cell">
                  </td>
                  <td class="ca-table-sum-column-cell">
                    {{ getCapitalizationSumPrice | rmbFormat }}
                  </td>
                  <td class="ca-table-sum-column-cell">
                    {{ '' }}
                  </td>
                </tr>
              </table>
            </div>

            <!--借用物料盘点表-->
            <div class="ca-form">
              <div class="ca-form-header">
                  <div style="font-size: 14px; font-weight: bold; margin-top: 20px;">
                    借用物料盘点表:
                  </div>
                <div class="hr hr-6 dotted"></div>
              </div>
              <table style="width: 100%;" class="ca-table" v-if="borrowMaterialList">
                <tr class="ca-table-header">
                  <th class="ca-table-header-column-cell" style="width: 40px;">
                    序号
                  </th>
                  <th class="ca-table-header-column-cell" style="width: 180px;">
                    申请单号
                  </th>
                  <th class="ca-table-header-column-cell" style="width: 180px;">
                    物料编码
                  </th>
                  <th class="ca-table-header-column-cell" style="width: 100px;">
                    型号
                  </th>
                  <th class="ca-table-header-column-cell" style="width: 65px;">
                    单位
                  </th>
                  <th class="ca-table-header-column-cell" style="width: 65px;">
                    数量
                  </th>
                  <th class="ca-table-header-column-cell" style="width: 85px;">
                    小计/元
                  </th>
                  <th class="ca-table-header-column-cell" style="width: 85px;">
                    领料人
                  </th>
                </tr>
                <tr class="ca-table-content" v-for="(item, index) in borrowMaterialList" :key="index">
                  <td class="ca-table-content-column-cell" style="width: 40px;">
                    {{ index + 1 }}
                  </td>
                  <td class="ca-table-content-column-cell" style="width: 180px;">
                    {{ item.code }}
                  </td>
                  <td class="ca-table-content-column-cell" style="width: 180px;">
                    {{ item.bomCode }}
                  </td>
                  <td class="ca-table-content-column-cell" style="width: 100px;">
                    {{ item.model }}
                  </td>
                  <td class="ca-table-content-column-cell" style="width: 65px;">
                    {{ item.unit }}
                  </td>
                  <td class="ca-table-content-column-cell" style="width: 65px;">
                    {{ item.amount }}
                  </td>
                  <td class="ca-table-content-column-cell" style="width: 85px;">
                    {{ item.preTaxSumPrice }}
                  </td>
                  <td class="ca-table-content-column-cell" style="width: 85px;">
                    {{ item.writerName }}
                  </td>
                </tr>
                <tr class="ca-table-sum">
                  <td class="ca-table-sum-column-cell">
                    总计
                  </td>
                  <td class="ca-table-sum-column-cell">
                  </td>
                  <td class="ca-table-sum-column-cell">
                  </td>
                  <td class="ca-table-sum-column-cell">
                  </td>
                  <td class="ca-table-sum-column-cell">
                  </td>
                  <td class="ca-table-sum-column-cell">
                  </td>
                  <td class="ca-table-sum-column-cell">
                    {{ getBorrowSumPrice | rmbFormat }}
                  </td>
                  <td class="ca-table-sum-column-cell">
                    {{ '' }}
                  </td>
                </tr>
              </table>
            </div>
            <div class="hr hr-10 hr-dotted hr-double"></div>
            <div>
              <div style="font-size: 14px; font-weight: bold; margin-top: 20px;">
                评审提交附件( <small><em>(包括但不限于物料盘点附件等)</em></small>):
              </div>
              <div v-if="reviewInfo.taskFileList && reviewInfo.taskFileList.length>0" style="display: flex; flex-direction: column; margin-bottom: 15px; margin-left: 20px; margin-top: 8px;">
                <div v-for="(item, i) in reviewInfo.taskFileList" :key="i" style="display: flex; flex-direction: row; align-items: center;">
                  <div v-if="item">
                    {{item.name}}
                  </div>
                  <div v-if="item && item.authStatus">
                    <el-button type="text" style="margin-left: 50px;" @click="downloadFile(item)">下载</el-button>
                  </div>
                </div>
              </div>
            </div>
            <div v-if="reviewInfo.status === JOB_ITEM_STATUS.ARCHIVED.key">
              <div class="ca-form-content">
                <div class="ca-form-content-line">
                  <div class="ca-form-content-line-label">
                    评审组长:
                  </div>
                  <div class="ca-form-content-line-content">
                    {{ reviewInfo.nextNodeName }}
                  </div>
                </div>
                <div class="ca-form-content-line">
                  <div class="ca-form-content-line-label">
                    评审意见:
                  </div>
                  <div class="ca-form-content-line-content">
                    <div v-rich-html="reviewInfo.submitDescription"></div>
                  </div>
                </div>
                <div class="ca-form-content-line">
                  <div class="ca-form-content-line-label">
                    评审附件:
                  </div>
                  <div class="ca-form-content-line-content">
                    <div v-if="reviewInfo.submitFileList && reviewInfo.submitFileList.length>0" style="display: flex; flex-direction: column;">
                      <div v-for="(item, i) in reviewInfo.submitFileList" :key="i" style="display: flex; flex-direction: row; align-items: center;">
                        <div v-if="item">
                          {{item.name}}
                        </div>
                        <div v-if="item && item.authStatus">
                          <el-button type="text" style="margin-left: 20px;" @click="downloadFile(item)">下载</el-button>
                        </div>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            </div>
            <div class="hr hr-10 hr-dotted hr-double"></div>
          </div>
        </div>
      </div>
    </div>
    <!--显示character Doc 信息-->
    <el-dialog
        width="29cm"
        :visible.sync="characterDocInfoDlg"
        :before-close="handleClose"
        append-to-body
        :close-on-click-modal=false>
      <div slot="title" style="font-size: 20px;">{{ dialogTitle }}</div>
      <div v-if="characterDocInfoDlg">
        <CharacterDocInfoPage
            :key="characterExeInfo.id"
            :character-id="characterExeInfo.id">
        </CharacterDocInfoPage>
      </div>
    </el-dialog>

  </div>
</template>

<script>
import Swal from "sweetalert2";
import JobitemDetailInfoDlg from "@/components/std/jobitem-detail-info-dlg.vue";
import {fixed2fractional, optionKV, rmbFormat} from "@/filter/filter";
import ApproveSignatureCore from "@/components/rdms/approve-signature-core.vue";
import FileSaver from 'file-saver';
import FileRdms from "@/components/rdms/file-rdms.vue";
import CharacterDocInfoPage from "@/components/std/character-doc-info-page.vue";

export default {
  components: {
    CharacterDocInfoPage,
    FileRdms,
    ApproveSignatureCore,
    JobitemDetailInfoDlg,

  },
  name: "subproject-exe-summary-page",

  props: {
    subprojectId: {
      default: null,
    },
  },

  computed: {
    getCapitalizationSumPrice() {
      let _this = this;
      let sums = 0;
      if(_this.capitalizationMaterialList){
        const values = _this.capitalizationMaterialList.map(item => Number(item.preTaxSumPrice));
        if (!values.every(value => isNaN(value))) {
          sums = values.reduce((prev, curr) => {
            const value = Number(curr);
            if (!isNaN(value)) {
              return prev + curr;
            } else {
              return prev;
            }
          }, 0);
        } else {
          sums = 0;
        }
      }
      return sums;
    },

    getExpenditureSumPrice() {
      let _this = this;
      let sums = 0;
      if(_this.expenditureMaterialList){
        const values = _this.expenditureMaterialList.map(item => Number(item.preTaxSumPrice));
        if (!values.every(value => isNaN(value))) {
          sums = values.reduce((prev, curr) => {
            const value = Number(curr);
            if (!isNaN(value)) {
              return prev + curr;
            } else {
              return prev;
            }
          }, 0);
        } else {
          sums = 0;
        }
      }
      return sums;
    },

    getBorrowSumPrice() {
      let _this = this;
      let sums = 0;
      if(_this.borrowMaterialList){
        const values = _this.borrowMaterialList.map(item => Number(item.preTaxSumPrice));
        if (!values.every(value => isNaN(value))) {
          sums = values.reduce((prev, curr) => {
            const value = Number(curr);
            if (!isNaN(value)) {
              return prev + curr;
            } else {
              return prev;
            }
          }, 0);
        } else {
          sums = 0;
        }
      }
      return sums;
    },
  },

  data() {
    return {
      loginUser: {},
      loginCustomer: {},
      subprojectExeInfo: [],
      characterExeInfoList: [],

      capitalizationMaterialList: [],
      expenditureMaterialList: [],
      borrowMaterialList: [],

      materialCheckFileList: [],

      characterDocInfoDlg: false,
      dialogTitle: '',

      characterExeInfo: {},

      reviewInfo: {},
      JOB_ITEM_STATUS: JOB_ITEM_STATUS,
    };
  },

  mounted: function () {
    let _this = this;
    _this.getLoginUser();
    if (Tool.isNotEmpty(_this.subprojectId)) {
      _this.getSubprojectBudgetExeInfoList(_this.subprojectId);
      _this.getCapitalizationListBySubprojectId(_this.subprojectId);
      _this.getExpenditureListBySubProjectId(_this.subprojectId);
      _this.getBorrowListBySubprojectId(_this.subprojectId);
      _this.getReviewJobInfo(_this.subprojectId);
    }
  },

  methods: {
    fixed2fractional,
    getLoginUser() {
      let _this = this;
      if (Tool.getLoginUser().userGroup === LOGIN_USER_GROUP.CUSTOMER.key) {
        _this.loginUser = Tool.getLoginUser().adminUser;
        _this.loginCustomer = Tool.getLoginUser();
      } else {
        _this.loginUser = Tool.getLoginUser();
        _this.loginCustomer = Tool.getLoginUser().customer;
      }
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

    testManhourActiveFormat(row, column) {
      if (row.testManhourActive) {
        return row.testManhourActive.toFixed(2);  //保留两位小数
      }
      return 0;
    },

    typeFormat(row, column) {
      return optionKV(JOB_ITEM_TYPE, row.type);
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


    getSubprojectBudgetExeInfoList(subProjectId) {
      let _this = this;
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/sub-project/getSubprojectBudgetExeSummary_researchExe/' + subProjectId).then((response) => {
        let resp = response.data;
        if (resp.success) {
          _this.subprojectExeInfo = resp.content;
          _this.characterExeInfoList = resp.content.children;

        }else{
          Toast.warning(resp.message, Swal);
        }
      });
    },

    getCapitalizationListBySubprojectId(subprojectId) {
      let _this = this;
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/material/getCapitalizationListBySubprojectId/' + subprojectId).then((response) => {
        let resp = response.data;
        if (resp.success) {
          _this.capitalizationMaterialList = resp.content;
        } else {
          Toast.warning(resp.message, Swal);
        }
      });
    },

    getExpenditureListBySubProjectId(subprojectId) {
      let _this = this;
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/material/getExpenditureListBySubProjectId/' + subprojectId).then((response) => {
        let resp = response.data;
        if (resp.success) {
          _this.expenditureMaterialList = resp.content;
        } else {
          Toast.warning(resp.message, Swal);
        }
      });
    },

    getBorrowListBySubprojectId(subprojectId) {
      let _this = this;
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/material/getBorrowListBySubprojectId/' + subprojectId).then((response) => {
        let resp = response.data;
        if (resp.success) {
          _this.borrowMaterialList = resp.content;
        } else {
          Toast.warning(resp.message, Swal);
        }
      });
    },

    getReviewJobInfo(subprojectId) {
      let _this = this;
      let formData = new window.FormData();
      formData.append('subprojectId', subprojectId);
      formData.append('loginUserId', _this.loginUser.customerUserId);
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/sub-project/getReviewJobInfo', formData).then((response)=>{
        let resp = response.data;
        if (resp.success) {
          _this.reviewInfo = resp.content;
        } else {
          Toast.warning(resp.message,Swal);
        }
      })
    },

    downloadFile(file) {
      let _this = this;
      _this.getSignedUrl(file.path, file.suffix);
    },
    getSignedUrl (fileFullName, suffix) {
      let _this = this;
      let formData = new window.FormData();
      formData.append('fileFullName', fileFullName);
      formData.append('customer', _this.loginCustomer.id);
      formData.append('user', _this.loginUser.customerUserId);
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/file/rdms/oss-get-signed-url', formData).then((response)=>{
        let resp = response.data;
        Tool.openOssSignedUrl(resp, suffix);
        // FileSaver.saveAs(resp.content, fileName);
      });
    },

    rowClick(row, column, e){
      let _this = this;
      _this.characterExeInfo = row;
      _this.characterDocInfoDlg = true;
      _this.dialogTitle = "特性文档汇总";
    },

    handleClose(done) {
      let _this = this;
      _this.characterDocInfoDlg = false;
      done();
    },

    closeDialog() {
      let _this = this;
      _this.characterDocInfoDlg = false;
    },

  },
}
</script>

<style lang="scss" scoped>
@import './../../../public/static/scss/caipd';

.paper-height {
  height: calc(100vh - 290px);
}

</style>
