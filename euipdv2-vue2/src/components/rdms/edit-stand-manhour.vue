<!--
 * Author: 周朔鹏
 * Date: 2025-03-10
 * Email: 391902958@qq.com
 * Website: https://www.euipd.com
-->

<template>
  <div class="a4-paper-big-simple">
    <div style="display: flex; flex-direction: column; align-items: center; justify-content: center; margin-top: 150px;">
      <el-form ref="form" label-width="155px">
        <el-form-item label="团队标准工时费率">
          <el-input type="number" v-model="standardManhour.devManhourFee"></el-input>
        </el-form-item>
<!--        <el-form-item label="机构测试标准工时费率">
          <el-input type="number" v-model="standardManhour.testManhourFee"></el-input>
        </el-form-item>-->
      </el-form>
      <div style="width: 400px; margin: 50px auto;">
        <em>说明: 标准费率是指团队内部核算使用的标准费率. 每位员工的职级不同, 不同的职级有不同的工时费率, 在核素处理过程中, 系统将不同的职级费率转换为标准费率进行数据统计.</em>
      </div>
    </div>
    <div class="clearfix form-actions" style="margin-top: 80px;">
      <div style="display: flex; justify-content: center;">
        <button v-on:click="saveStandManhour()" class="ca-btn-primary" type="button">
          保存
        </button>
      </div>
    </div>
  </div>

</template>

<script>
import Swal from "sweetalert2";

export default {
  name: 'edit-stand-manhour',

  data: function() {
    return {
      loginUser: {},
      loginCustomer: {},

      standardManhour: {},

      value: [],
      titles: ['可选人员','项目经理'],

      customerId: '',
    };
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

      _this.getStandardManhour();
    },

    getStandardManhour(){
      let _this = this;
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/manhour/get-standard/' + _this.loginCustomer.id).then((response) => {
        let resp = response.data;
        if (resp.success) {
          _this.standardManhour = resp.content;
        } else {
          Toast.warning(resp.message, Swal);
        }
      })
    },

    saveStandManhour() {
      let _this = this;
      let devStdManhourFee = Number(_this.standardManhour.devManhourFee);

      if(isNaN(devStdManhourFee) || devStdManhourFee < 20){
        Toast.warning("工时费率不能小于20元每小时", Swal);
        return;
      }

      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/manhour/save', {
        customerId: _this.loginCustomer.id,
        devManhourFee: devStdManhourFee,
      }).then((response)=>{
        let resp = response.data;
        if (resp.success) {
          Toast.success("保存成功", Swal);
        } else {
          Toast.warning(resp.message, Swal);
        }
      })
    },
  },

};
</script>

<style lang="scss">
@import './../../../public/static/scss/caipd.scss';
</style>
