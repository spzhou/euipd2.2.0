<!--
 * Author: 周朔鹏
 * Date: 2024-11-12
 * Email: 391902958@qq.com
 * Website: https://www.euipd.com
-->

<template>
  <div class="dialog-container">
    <div style="padding-left: 80px; padding-right: 80px;">
      <el-row>
        <el-form label-width="200px">
          <el-form-item label="条目编号:">
            <el-col :span="16">
              {{ feeItem.serialNo }}
            </el-col>
          </el-form-item>
          <el-form-item label="费用条目名称:">
            <el-col :span="16">
              {{ feeItem.name }}
            </el-col>
          </el-form-item>
          <el-form-item label="费用分类:">
            <el-col :span="8">
              {{ BUDGET_CLASSIFY_STD | optionKV(feeItem.classify) }}
            </el-col>
          </el-form-item>
          <el-form-item label="金额:">
            <el-col :span="4">
              {{ feeItem.amount | rmbFormat }}
            </el-col>
          </el-form-item>
          <el-form-item label="支付状态:">
            <el-select v-model="paymentStatue" placeholder="请选择">
              <el-option
                  v-for="item in PAYMENT_STATUS"
                  :key="item.key"
                  :label="item.value"
                  :value="item.key">
              </el-option>
            </el-select>
          </el-form-item>
        </el-form>
      </el-row>
    </div>
    <div class="clearfix form-actions" style="margin-top: 30px;">
      <div style="display: flex; justify-content: center;">
        <button v-on:click="updateFeeItem()" class="ca-btn-primary" type="button">
          保存
        </button>
        <button v-on:click="close()" class="ca-btn-primary" type="button">
          关闭
        </button>
      </div>
    </div>
  </div>
</template>

<script>
import FileRdms from "@/components/rdms/file-rdms";
import Swal from "sweetalert2";

export default {
  components: {
    FileRdms,
  },
  name: "fee-payment-edit-dlg",

  data: function() {
    return {
      loginUser: {},
      loginCustomer: {},

      PAYMENT_STATUS: PAYMENT_STATUS,
      BUDGET_CLASSIFY_STD: BUDGET_CLASSIFY_STD,
      paymentStatue: this.feeItem ? this.feeItem.paymentStatue : '',
    }
  },

  props: {
    feeItem: {
      default: null,
    },
    closeDlg: {
      type: Function,
      default: null
    },
  },

  computed: {
  },

  mounted: function() {
    let _this = this;
    _this.getLoginUser();
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

    updateFeeItem() {
      let _this = this;
      if(_this.paymentStatue !== PAYMENT_STATUS.PAID.key){
        _this.close();
        return;
      }
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/budget-direct-exe/updatePaymentStatus/' + _this.feeItem.id).then((response)=>{
        let resp = response.data;
        if (resp.success) {
          Toast.success("保存成功", Swal);
          _this.$bus.$emit('refresh-feeItem-edit-dlg', true);
          _this.close();
        } else {
          Toast.warning(resp.message, Swal);
        }
      })
    },

    close(){
      let _this = this;
      _this.closeDlg();
    },

  }
}
</script>

<style lang="scss">
@import './../../../public/static/scss/caipd.scss';
</style>
