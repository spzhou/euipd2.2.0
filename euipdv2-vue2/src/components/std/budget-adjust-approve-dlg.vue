<!--
 * Author: 周朔鹏
 * Date: 2024-12-19
 * Email: 391902958@qq.com
 * Website: https://www.euipd.com
-->

<template>
  <div class="dialog-container">
    <el-row style="margin-bottom: 5px;">
      <el-col :offset="2">
        <el-form>
          <el-form-item prop="desc">
            <span slot="label">{{ "审批意见:" }}</span>
            <el-col :span="18">
              <el-input type="textarea" rows="2" v-model="budgetAdjustInfo.approvalOpinion"></el-input>
            </el-col>
          </el-form-item>
        </el-form>
      </el-col>
    </el-row>
    <div style="height: 50px;"></div>
    <div class="clearfix form-actions">
      <div style="display: flex; justify-content: center;">
        <button v-on:click="approvalPass()" class="ca-btn-primary" type="button">通 过</button>
        <button type="button" v-on:click="close()" class="ca-btn-primary">关 闭</button>
      </div>
    </div>
  </div>
</template>

<script>
import FileRdms from "@/components/rdms/file-rdms.vue";
import Swal from "sweetalert2";

export default {
  components: {
    FileRdms,
  },

  name: "budget-adjust-approve-dlg",

  props: {
    budgetAdjustInfo: {
      default: null,
    },
    closeFunction: {
      type: Function,
      default: null
    },
  },

  data: function() {
    return {
      loginUser: {},
      loginCustomer: {},

      JOB_ITEM_TYPE: JOB_ITEM_TYPE,
      ENTRANCE_CLASS: ENTRANCE_CLASS,

    }
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

    approvalPass() {
      let _this = this;

      if (1 !== 1
          || !Validator.require(_this.budgetAdjustInfo.approvalOpinion, "审批意见", Swal)
          || !Validator.length(_this.budgetAdjustInfo.approvalOpinion, "审批意见",2, 1000,  Swal)
      ) {
        return;
      }

      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/budget-mng/approvalPass', _this.budgetAdjustInfo).then((response) => {
        let resp = response.data;
        if (resp.success) {
          Toast.success("保存成功", Swal);
          _this.close();
          _this.$bus.publish({ areas: ['budget', 'badge'], reason: 'budget-approve' });
        } else {
          Toast.warning(resp.message, Swal);
        }
      })
    },

    close(){
      let _this = this;
      _this.closeFunction();
    },

  }
}
</script>

<style lang="scss">
@import '../../../public/static/scss/caipd';

</style>
