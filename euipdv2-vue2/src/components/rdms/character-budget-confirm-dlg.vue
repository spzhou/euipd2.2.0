<!--
 * Author: 周朔鹏
 * Date: 2024-06-01
 * Email: 391902958@qq.com
 * Website: https://www.euipd.com
-->

<template>
  <div class="dialog-container">
    <el-row>
      <el-col :offset="4" :span="16">
        <el-col :span="12">
          <el-form
              label-width="150px">
            <el-form-item
                label="计划开发工时">
              {{ planManhour.devManhour }} <span>小时</span>
            </el-form-item>
            <el-form-item
                label="计划测试工时">
              {{ planManhour.testManhour }} <span>小时</span>
            </el-form-item>
            <el-form-item
                label="物料费计划">
              {{ planManhour.material }} <span>元</span>
            </el-form-item>
            <el-form-item
                label="三方测试费计划">
              {{ planManhour.test }} <span>元</span>
            </el-form-item>
            <el-form-item
                label="动力费计划">
              {{ planManhour.power }} <span>元</span>
            </el-form-item>
            <el-form-item
                label="会议费计划">
              {{ planManhour.conference }} <span>元</span>
            </el-form-item>
            <el-form-item
                label="差旅费计划">
              {{ planManhour.business }} <span>元</span>
            </el-form-item>
            <el-form-item
                label="合作费计划">
              {{ planManhour.cooperation }} <span>元</span>
            </el-form-item>
            <el-form-item
                label="知识产权类费用计划">
              {{ planManhour.property }} <span>元</span>
            </el-form-item>
            <el-form-item
                label="劳务费计划">
              {{ planManhour.labor }} <span>元</span>
            </el-form-item>
            <el-form-item
                label="专家费计划">
              {{ planManhour.consulting }} <span>元</span>
            </el-form-item>
            <el-form-item
                label="管理费计划">
              {{ planManhour.management }} <span>元</span>
            </el-form-item>
          </el-form>
        </el-col>
        <el-col :span="12">
          <el-form
              label-width="150px">
            <el-form-item
                label="核定开发工时">
              <el-input type="number" style="width: 110px;" v-model="confirmManhour.devManhourApproved" placeholder="核定开发工时"></el-input><span>小时</span>
            </el-form-item>
            <el-form-item
                label="核定测试工时">
              <el-input type="number" style="width: 110px;" v-model="confirmManhour.testManhourApproved" placeholder="核定测试工时"></el-input><span>小时</span>
            </el-form-item>
            <el-form-item
                label="核定物料费">
              <el-input type="number" style="width: 110px;" v-model="confirmManhour.materialFeeApproved" placeholder="核定物料费"></el-input><span>元</span>
            </el-form-item>
            <el-form-item
                label="核定三方测试费">
              <el-input type="number" style="width: 110px;" v-model="confirmManhour.test" placeholder="核定其他杂费"></el-input><span>元</span>
            </el-form-item>
            <el-form-item
                label="核定动力费">
              <el-input type="number" style="width: 110px;" v-model="confirmManhour.power" placeholder="核定其他杂费"></el-input><span>元</span>
            </el-form-item>
            <el-form-item
                label="核定会议费">
              <el-input type="number" style="width: 110px;" v-model="confirmManhour.conference" placeholder="核定其他杂费"></el-input><span>元</span>
            </el-form-item>
            <el-form-item
                label="核定差旅费">
              <el-input type="number" style="width: 110px;" v-model="confirmManhour.business" placeholder="核定其他杂费"></el-input><span>元</span>
            </el-form-item>
            <el-form-item
                label="核定合作费">
              <el-input type="number" style="width: 110px;" v-model="confirmManhour.cooperation" placeholder="核定其他杂费"></el-input><span>元</span>
            </el-form-item>
            <el-form-item
                label="核定知识产权类费用">
              <el-input type="number" style="width: 110px;" v-model="confirmManhour.property" placeholder="核定其他杂费"></el-input><span>元</span>
            </el-form-item>
            <el-form-item
                label="核定劳务费">
              <el-input type="number" style="width: 110px;" v-model="confirmManhour.labor" placeholder="核定其他杂费"></el-input><span>元</span>
            </el-form-item>
            <el-form-item
                label="核定专家费">
              <el-input type="number" style="width: 110px;" v-model="confirmManhour.consulting" placeholder="核定其他杂费"></el-input><span>元</span>
            </el-form-item>
            <el-form-item
                label="核定管理费">
              <el-input type="number" style="width: 110px;" v-model="confirmManhour.management" placeholder="核定其他杂费"></el-input><span>元</span>
            </el-form-item>
          </el-form>
        </el-col>
      </el-col>

    </el-row>
    <div class="clearfix form-actions">
      <div style="display: flex; justify-content: center;">
        <button v-on:click="confirm(confirmManhour)" class="ca-btn-primary" type="button">保存</button>
        <button type="button" v-on:click="close()" class="ca-btn-primary">关闭</button>
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
  name: "character-budget-confirm-dlg",

  data: function() {
    return {
      loginUser: {},
      loginCustomer: {},

      character: {},

      planManhour: {
        devManhour: 0,
        testManhour: 0,
        material: 0,
        test: 0,
        power: 0,
        conference: 0,
        business: 0,
        cooperation: 0,
        property: 0,
        labor: 0,
        consulting: 0,
        management: 0,
      },
      confirmManhour: {
        devManhourApproved: 0,
        testManhourApproved: 0,
        materialFeeApproved: 0,
        test: 0,
        power: 0,
        conference: 0,
        business: 0,
        cooperation: 0,
        property: 0,
        labor: 0,
        consulting: 0,
        management: 0,
      }
    }
  },

  props: {
    characterItem: {
      default: null,
    },
    closeFunction: {
      type: Function,
      default: null
    },
  },

  computed: {
  },

  mounted: function() {
    let _this = this;
    _this.getLoginUser();
    _this.character = _this.characterItem;
    // console.log("_this.character:",_this.character);
    _this.planManhour.devManhour = _this.characterItem.devManhour;
    _this.confirmManhour.devManhourApproved = _this.characterItem.devManhourApproved;
    _this.planManhour.testManhour = _this.characterItem.testManhour;
    _this.confirmManhour.testManhourApproved = _this.characterItem.testManhourApproved;
    _this.planManhour.material = _this.characterItem.materialFee;
    _this.confirmManhour.materialFeeApproved = _this.characterItem.materialFeeApproved;

    _this.planManhour.test = _this.characterItem.testFee;
    _this.confirmManhour.test = _this.characterItem.testFee;
    _this.planManhour.power = _this.characterItem.powerFee;
    _this.confirmManhour.power = _this.characterItem.powerFee;
    _this.planManhour.conference = _this.characterItem.conferenceFee;
    _this.confirmManhour.conference = _this.characterItem.conferenceFee;
    _this.planManhour.business = _this.characterItem.businessFee;
    _this.confirmManhour.business = _this.characterItem.businessFee;
    _this.planManhour.cooperation = _this.characterItem.cooperationFee;
    _this.confirmManhour.cooperation = _this.characterItem.cooperationFee;
    _this.planManhour.property = _this.characterItem.propertyFee;
    _this.confirmManhour.property = _this.characterItem.propertyFee;
    _this.planManhour.labor = _this.characterItem.laborFee;
    _this.confirmManhour.labor = _this.characterItem.laborFee;
    _this.planManhour.consulting = _this.characterItem.consultingFee;
    _this.confirmManhour.consulting = _this.characterItem.consultingFee;
    _this.planManhour.management = _this.characterItem.managementFee;
    _this.confirmManhour.management = _this.characterItem.managementFee;
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

    confirm(manhour){
      let _this = this;
      _this.character.devManhourApproved = manhour.devManhourApproved;
      _this.character.testManhourApproved = manhour.testManhourApproved;
      _this.character.materialFeeApproved = manhour.materialFeeApproved;
      _this.character.testFee = manhour.test;
      _this.character.powerFee = manhour.power;
      _this.character.conferenceFee = manhour.conference;
      _this.character.businessFee = manhour.business;
      _this.character.cooperationFee = manhour.cooperation;
      _this.character.propertyFee = manhour.property;
      _this.character.laborFee = manhour.labor;
      _this.character.consultingFee = manhour.consulting;
      _this.character.managementFee = manhour.management;
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/character/save-confirm-manhour', _this.character).then((response)=>{
        let resp = response.data;
        if (resp.success) {
          _this.$bus.$emit('refresh-project-append-character-list', true);
          _this.$bus.$emit('refresh-project-setup-budget-table', true);
          _this.closeFunction();
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
@import './../../../public/static/scss/caipd';

</style>
