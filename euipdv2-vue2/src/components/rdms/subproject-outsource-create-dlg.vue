<!--
 * Author: 周朔鹏
 * Date: 2024-11-12
 * Email: 391902958@qq.com
 * Website: https://www.euipd.com
-->

<template>
  <div>
    <div class="dialog-container">
      <div style="margin: 0 80px 0 10px">
        <el-form
            label-width="150px" size="small">
          <el-form-item label="项目外包说明">
            <template>
              <p>1. 您可以将一个项目外包给另一个团队进行执行, 项目外包后, 不可撤回, 请做好沟通工作。 如果需要签署项目外包开发协议, 请提前沟通处理好。</p>
              <p>2. 如果您需要参与外包项目的里程碑评审、特性评审以及项目评审工作，您也可以以某种角色身份加入到外包开发团队中去，以便更加深入的参与项目开发全过程管理。</p>
            </template>
          </el-form-item>
          <el-form-item label="外包团队" required>
              <el-select v-model="outCustomerId" filterable @change="selectChange(outCustomerId)" placeholder="请选择">
                <el-option
                    v-for="item in outCustomerList"
                    :key="item.id"
                    :label="item.customerName"
                    :value="item.id">
                </el-option>
              </el-select>
          </el-form-item>
          <el-form-item label="外包方监管委员" required>
              <el-select v-model="superviseId" filterable placeholder="请选择">
                <el-option
                    v-for="item in superviseList"
                    :key="item.id"
                    :label="item.trueName"
                    :value="item.id">
                </el-option>
              </el-select>
          </el-form-item>
        </el-form>
      </div>
      <div style="margin-top: 50px;">
        <div class="clearfix form-actions">
          <div style="display: flex; justify-content: center;">
            <button v-on:click="confirm()" class="ca-btn-primary" type="button">
              确定
            </button>
            <button v-on:click="close()" class="ca-btn-primary" type="button">
              取消
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>
<script>
import Swal from "sweetalert2";

export default {
  components:{

  },

  name: 'subproject-outsource-create-dlg',
  props:{
    subprojectId: {
      default: null,
    },
    closeFunction: {
      type: Function,
      default: null
    },
  },

  data() {
    return {
      loginUser: {},
      loginCustomer: {},

      outCustomerList:[],
      superviseList:[],
      outCustomerId: '',
      superviseId: '',
    };
  },

  computed: {
  },

  mounted: function() {
    let _this =this;
    _this.getLoginUser();
  },
  methods:{
    getLoginUser(){
      let _this = this;
      if(Tool.getLoginUser().userGroup === LOGIN_USER_GROUP.CUSTOMER.key){
        _this.loginUser = Tool.getLoginUser().adminUser;
        _this.loginCustomer =Tool.getLoginUser();
      }else{
        _this.loginUser = Tool.getLoginUser();
        _this.loginCustomer = Tool.getLoginUser().customer;
      }
      _this.getOutSourcingCustomerList();
    },

    getOutSourcingCustomerList() {
      let _this = this;
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/system/rdms/customer/getOutSourcingCustomerList/' + _this.loginCustomer.id).then((response)=>{
        let resp = response.data;
        if (resp.success) {
          _this.outCustomerList = resp.content;
        }
      })
    },

    selectChange(customerId) {
      let _this = this;
      _this.superviseList = [];
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/project/listIpmtByCustomerId/'+ customerId).then((response)=>{
        let resp = response.data;
        if (resp.success) {
          if(resp.content === null){
            _this.superviseList = [];
          }else{
            _this.superviseList = resp.content;
          }
        } else {
          Toast.warning(resp.message,Swal);
        }
      })
    },


    confirm() {
      let _this = this;
      if (1 !== 1
          || !Validator.require(_this.outCustomerId, "外包团队", Swal)
          || !Validator.require(_this.superviseId, "外包方监管委员", Swal)
      ) {
        return;
      }
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/sub-project/saveOutSourcing', {
        subprojectId: _this.subprojectId,
        outCustomerId: _this.outCustomerId,
        superviseId: _this.superviseId,
      } ).then((response) => {
        let resp_submit = response.data;
        if (resp_submit.success) {
          _this.close();
          Toast.success("提交成功", Swal);
        } else {
          Toast.warning(resp_submit.message, Swal);
        }
      })
    },

    close(){
      let _this = this;
      _this.closeFunction();
    },
  }
};
</script>

<style lang="scss" scoped>
@import './../../../public/static/scss/caipd.scss';
.ca-form{
  font-size: 14px;
  margin-left: 30px;
}
</style>
