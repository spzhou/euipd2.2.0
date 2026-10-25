<!--
 * Author: 周朔鹏
 * Date: 2024-05-26
 * Email: 391902958@qq.com
 * Website: https://www.euipd.com
-->

<template>
  <div>
    <div id="container" ref="printRef">
      <div class="a4-paper-print">
        <div class="ca-form">
          <div class="ca-form-title">
            <div class="ca-form-title-line">
              费用化物料盘点表
            </div>
            <div class="hr hr-6 dotted"></div>
          </div>
          <table class="ca-table" v-if="materialList">
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
              <th class="ca-table-header-column-cell" style="width: 85px;">
                备注
              </th>
            </tr>
            <tr class="ca-table-content" v-for="(item, index) in materialList" :key="index">
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
              <td class="ca-table-content-column-cell" style="width: 85px;">
                {{ '' }}
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
                {{ getSumPrice | rmbFormat }}
              </td>
              <td class="ca-table-sum-column-cell">
                {{ '' }}
              </td>
              <td class="ca-table-sum-column-cell">
                {{ '' }}
              </td>
            </tr>
          </table>
          <div>
            <div class="hr hr-6 dotted hr-double"></div>
            <div style="margin-top: 30px; margin-bottom: 5px;">
              使用说明: 以下为部分由参与盘点人员或部门签字.
            </div>
            <ApproveSignatureCore
                :serial-no="1"
            ></ApproveSignatureCore>
            <ApproveSignatureCore
                :serial-no="2"
            ></ApproveSignatureCore>
            <ApproveSignatureCore
                :serial-no="3"
            ></ApproveSignatureCore>
            <div class="hr hr-18 dotted hr-double"></div>
            <div id="no-print" class="no-print">
              <div style="display: flex; flex-direction: row; justify-content: end; margin-top: 30px;padding-bottom: 20px;">
                <button type="button" class="ca-btn-primary-sm" @click="toPrint()">打印</button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import Swal from "sweetalert2";
import {dateFormat, optionKV, rmbFormat} from "@/filter/filter";
import MaterialAddDlg from "@/components/std/material-add-dlg.vue";
import ApproveSignatureCore from "@/components/rdms/approve-signature-core.vue";

export default {
  components: {
    MaterialAddDlg,
    ApproveSignatureCore,
  },
  name: "material-expenditure-check-list-dlg",

  props: {
    xId: {
      default: null
    },
    docType: {
      default: null
    },
    closeDlg: {
      type: Function,
      default: null
    },
  },

  data() {
    return {
      materialList: [],

      loginUser: {},
      loginCustomer: {},
    };
  },

  computed: {
    getSumPrice() {
      let _this = this;
      let sums = 0;
      const values = _this.materialList.map(item => Number(item.preTaxSumPrice));
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
      return sums;
    },
  },

  mounted: function () {
    let _this = this;
    _this.getLoginUser();

    if (Tool.isNotEmpty(_this.xId) && _this.docType === DOC_TYPE.CHARACTER.key) {
      _this.getExpenditureListByCharacterId(_this.xId);
    }
    if (Tool.isNotEmpty(_this.xId) && _this.docType === DOC_TYPE.SUB_PROJECT.key) {
      _this.getExpenditureListBySubProjectId(_this.xId);
    }

  },

  methods: {
    toPrint () {
      let _this = this;
      _this.$print(this.$refs.printRef);

    },

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

    getExpenditureListByCharacterId(characterId) {
      let _this = this;
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/material/getExpenditureListByCharacterId/' + characterId).then((response) => {
        let resp = response.data;
        if (resp.success) {
          _this.materialList = resp.content;
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
          _this.materialList = resp.content;
        } else {
          Toast.warning(resp.message, Swal);
        }
      });
    },

    close() {
      let _this = this;
      _this.closeDlg();
    },

  },
}
</script>

<style lang="scss" scoped>
@import '../../../public/static/scss/caipd';

.dialog-width {
  width: 100%;
}
.red-color {
  color: red;
}
</style>
