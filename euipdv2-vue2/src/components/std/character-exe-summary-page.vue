<!--
 * Author: 周朔鹏
 * Date: 2024-11-06
 * Email: 391902958@qq.com
 * Website: https://www.euipd.com
-->

<template>
  <div style="height: 100%;">
    <div class="paper-height">
      <div style="height: 100%; overflow: scroll;">
        <div class="a4-paper-big-simple">
          <div style="margin: 50px 60px;">
            <div v-if="characterItem.rdType !== RD_TYPE.RESEARCH.key">
              <div style="font-size: 14px; line-height: 28px; margin: 5px 0; font-weight: bold">
                特性开发及预算执行信息概览:
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
                    label="任务类型">
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
            </div>

            <div v-if="characterItem.rdType !== RD_TYPE.RESEARCH.key && characterExeInfoList && characterExeInfoList.length>0" style="width: 100%; text-align: right;">
              <i class="el-icon-info"></i>
              <small style="margin-left: 3px;">备注: 上表物料单数仅统计费用化领料单. 其他类型领料单在资产盘点表中给出详单.</small>
            </div>
            <div class="hr hr-10 hr-dotted hr-double"></div>
            <!--费用化物料盘点表-->
            <div v-if="expenditureMaterialList" class="ca-form">
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
            <div v-if="capitalizationMaterialList" class="ca-form">
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
            <div v-if="borrowMaterialList" class="ca-form">
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

            <div  v-if="reviewInfo && reviewInfo.taskFileList && reviewInfo.taskFileList.length>0">
              <div class="hr hr-10 hr-dotted hr-double"></div>
              <div style="font-size: 14px; font-weight: bold;">
                评审信息:
              </div>
              <div style="font-size: 14px; margin-top: 20px;">
                评审提交附件( <small><em>(包括但不限于物料盘点附件等)</em></small>):
              </div>
              <div v-if="reviewInfo.taskFileList && reviewInfo.taskFileList.length>0" style="display: flex; flex-direction: column; margin: 8px 0 8px 60px;">
                <div v-for="(item, i) in reviewInfo.taskFileList" :key="i" style="display: flex; align-items: center; flex-direction: row;">
                  <div v-if="item" style="margin-top: 6px;">
                    {{item.name}}
                  </div>
                  <div v-if="item && item.authStatus">
                    <el-button type="text" style="margin-left: 50px;" @click="downloadFile(item)">下载</el-button>
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
                        <div v-for="(item, i) in reviewInfo.submitFileList" :key="i" style="display: flex; align-items: center; flex-direction: row;">
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
            </div>
<!--            <div class="hr hr-10 hr-dotted hr-double"></div>-->
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import Swal from "sweetalert2";
import JobitemDetailInfoDlg from "@/components/std/jobitem-detail-info-dlg.vue";
import {optionKV, rmbFormat} from "@/filter/filter";
import ApproveSignatureCore from "@/components/rdms/approve-signature-core.vue";
import FileSaver from 'file-saver';

export default {
  components: {
    ApproveSignatureCore,
    JobitemDetailInfoDlg,

  },
  name: "character-exe-summary-page",

  props: {
    characterId: {
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
      characterExeInfoList: [],
      capitalizationMaterialList: [],
      expenditureMaterialList: [],
      borrowMaterialList: [],

      materialCheckFileList: [],
      reviewInfo: {},
      characterItem: {},

      JOB_ITEM_STATUS: JOB_ITEM_STATUS,
      RD_TYPE: RD_TYPE,
    };
  },

  mounted: function () {
    let _this = this;
    _this.getLoginUser();
    if (Tool.isNotEmpty(_this.characterId)) {
      _this.getCharacterRecordDetailById(_this.characterId);
      _this.getCharacterManhourExeInfoList(_this.characterId);
      _this.getCapitalizationListByCharacterId(_this.characterId);
      _this.getExpenditureListByCharacterId(_this.characterId);
      _this.getBorrowListByCharacterId(_this.characterId);
      _this.getLastReviewInfo(_this.characterId);
    }
  },

  methods: {
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

    getCharacterRecordDetailById(characterId) {
      let _this = this;
      let formData = new window.FormData();
      formData.append('characterId', characterId);
      formData.append('loginUserId', _this.loginUser.customerUserId);
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/character/getCharacterRecordDetailById', formData).then((response) => {
        let resp = response.data;
        if (resp.success) {
          _this.characterItem = resp.content;
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
        }else{
          Toast.warning(resp.message, Swal);
        }
      });
    },

    getCapitalizationListByCharacterId(characterId) {
      let _this = this;
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/material/getCapitalizationListByCharacterId/' + characterId).then((response) => {
        let resp = response.data;
        if (resp.success) {
          _this.capitalizationMaterialList = resp.content;
        } else {
          Toast.warning(resp.message, Swal);
        }
      });
    },

    getExpenditureListByCharacterId(characterId) {
      let _this = this;
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/material/getExpenditureListByCharacterId/' + characterId).then((response) => {
        let resp = response.data;
        if (resp.success) {
          _this.expenditureMaterialList = resp.content;
        } else {
          Toast.warning(resp.message, Swal);
        }
      });
    },

    getBorrowListByCharacterId(characterId) {
      let _this = this;
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/material/getBorrowListByCharacterId/' + characterId).then((response) => {
        let resp = response.data;
        if (resp.success) {
          _this.borrowMaterialList = resp.content;
        } else {
          Toast.warning(resp.message, Swal);
        }
      });
    },

    getLastReviewInfo(characterId) {
      let _this = this;
      let formData = new window.FormData();
      formData.append('characterId', characterId);
      formData.append('loginUserId', _this.loginUser.customerUserId);
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/character/getLastReviewInfo', formData).then((response)=>{
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

  },
}
</script>

<style lang="scss" scoped>
@import './../../../public/static/scss/caipd';

.paper-height {
  height: calc(100vh - 255px);
}

</style>
