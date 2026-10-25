<!--
 * Author: 周朔鹏
 * Date: 2024-12-25
 * Email: 391902958@qq.com
 * Website: https://www.euipd.com
-->

<template>
  <div>
    <div id="container" ref="printRef">
      <div class="a4-paper-print-no-height">
        <div class="ca-form">
          <div class="ca-form-title">
            <div class="ca-form-title-line">
              {{getTypeName}}物料盘点表
            </div>
            <div class="hr hr-6 dotted"></div>
          </div>
          <table class="ca-table"  v-if="materialList">
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
                借用人
              </th>
              <th class="ca-table-header-column-cell" style="width: 85px;">
                备注
              </th>

            </tr>
            <tr class="ca-table-content" v-for="(item, index) in materialList" :key="index">
              <td class="ca-table-content-column-cell" style="width: 40px; font-size: 12px;">
                {{ index + 1 }}
              </td>
              <td class="ca-table-content-column-cell" style="width: 180px; font-size: 12px;">
                {{ item.code }}
              </td>
              <td class="ca-table-content-column-cell" style="width: 180px; font-size: 12px;">
                {{ item.bomCode }}
              </td>
              <td class="ca-table-content-column-cell" style="width: 100px; font-size: 12px;">
                {{ item.model }}
              </td>
              <td class="ca-table-content-column-cell" style="width: 65px; font-size: 12px;">
                {{ item.unit }}
              </td>
              <td class="ca-table-content-column-cell" style="width: 65px; font-size: 12px;">
                {{ item.amount }}
              </td>
              <td class="ca-table-content-column-cell" style="width: 85px; font-size: 12px;">
                {{ item.preTaxSumPrice }}
              </td>
              <td class="ca-table-content-column-cell" style="width: 85px; font-size: 12px;">
                {{ item.writerName }}
              </td>
              <td class="ca-table-content-column-cell" style="width: 85px; font-size: 12px;">
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

export default {
  components: {
  },
  name: "material-check-list-customer",

  props: {
    applicationType: {
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
      if(_this.materialList !== null){
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
      }else{
        sums = 0;
      }
      return sums;
    },

    getTypeName() {
      let _this = this;
      if(_this.applicationType === MATERIAL_USAGE_MODE.BORROW.key){
        return '借用';
      }else if(_this.applicationType === MATERIAL_USAGE_MODE.EXPENDITURE.key){
        return '费用化';
      }else if(_this.applicationType === MATERIAL_USAGE_MODE.CAPITALIZATION.key){
        return '资产化';
      }else{
        return '';
      }

    },
  },

  mounted: function () {
    let _this = this;
    _this.getLoginUser();

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

      if(_this.applicationType === MATERIAL_USAGE_MODE.BORROW.key){
        _this.getBorrowListByCustomerId();
      }
      if(_this.applicationType === MATERIAL_USAGE_MODE.EXPENDITURE.key){
        _this.getExpenditureListByCustomerId();
      }
      if(_this.applicationType === MATERIAL_USAGE_MODE.CAPITALIZATION.key){
        _this.getCapitalizationListByCustomerId();
      }

    },

    getBorrowListByCustomerId() {
      let _this = this;
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/material/getBorrowListByCustomerId/' + _this.loginCustomer.id).then((response) => {
        let resp = response.data;
        if (resp.success) {
          _this.materialList = resp.content;
        } else {
          Toast.warning(resp.message, Swal);
        }
      });
    },

    getExpenditureListByCustomerId() {
      let _this = this;
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/material/getExpenditureListByCustomerId/' + _this.loginCustomer.id).then((response) => {
        let resp = response.data;
        if (resp.success) {
          _this.materialList = resp.content;
        } else {
          Toast.warning(resp.message, Swal);
        }
      });
    },

    getCapitalizationListByCustomerId() {
      let _this = this;
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/material/getCapitalizationListByCustomerId/' + _this.loginCustomer.id).then((response) => {
        let resp = response.data;
        if (resp.success) {
          _this.materialList = resp.content;
        } else {
          Toast.warning(resp.message, Swal);
        }
      });
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
