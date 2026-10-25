<!--
 * Author: 周朔鹏
 * Date: 2024-04-01
 * Email: 391902958@qq.com
 * Website: https://www.euipd.com
-->

<template>
  <div class="page-wrapper">
    <div style="margin-top: -20px; padding-bottom: 50px;">
      <el-row>
        <div style="width: 100%; height: 50px; background-color: #F5F5F5">
          <div style="line-height: 50px; font-size: 18px; margin-left: 20px;">
            <span style="margin-right: 3px;">登录名称:</span><span style="margin-right: 15px;">{{ customer.loginName }}</span>
          </div>
        </div>
      </el-row>
      <el-row>
        <el-col :span="8">
          <div style="width: 60%; margin: 50px auto 0; background-color: #F5F5F5;">
            <img :src="customer.profile" alt="" style="width: 100%;">
          </div>
        </el-col>
        <el-col :span="16">
          <table class="table">
            <tr class="table-tr">
              <th class="first-th">
                团队名称
              </th>
              <th class="second-th">
                {{ customer.customerName }}
              </th>
            </tr>
            <tr class="table-tr">
              <th class="first-th">
                信用代码
              </th>
              <th class="second-th">
                {{ customer.creditCode }}
              </th>
            </tr>
            <tr class="table-tr">
              <th class="first-th">
                注册地址
              </th>
              <th class="second-th">
                {{ customer.regAddress }}
              </th>
            </tr>
            <tr class="table-tr">
              <th class="first-th">
                办公地址
              </th>
              <th class="second-th">
                {{ customer.officeAddress }}
              </th>
            </tr>
            <tr class="table-tr">
              <th class="first-th">
                机构管理员
              </th>
              <th class="second-th">
                {{ customer.adminName }}
              </th>
            </tr>
            <tr class="table-tr">
              <th class="first-th">
                管理员手机
              </th>
              <th class="second-th">
                {{ customer.contactPhone }}
              </th>
            </tr>
            <tr class="table-tr">
              <th class="first-th">
                机构状态
              </th>
              <th class="second-th">
                {{ customer.status }}
              </th>
            </tr>
          </table>
        </el-col>
      </el-row>
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
  name: "customer-info",

  data: function() {
    return {
      loginUser: {},
      loginCustomer: {},

      labelPosition: 'right',
      customer: {},
    }
  },

  props: {
    customerId:{
      default: null,
    },
  },

  computed: {
  },

  mounted: function() {
    let _this = this;
    _this.getLoginUser();
    _this.getCustomerInfo();

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

    getCustomerInfo() {
      let _this = this;
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/system/rdms/customer/get-customer-info/' + _this.customerId).then((response)=>{
        let resp = response.data;
        if (resp.success) {
          _this.customer = resp.content;
        } else {
          Toast.warning(resp.message,Swal);
        }
      })
    },
  }
}
</script>

<style lang="scss">
@import './../../../../public/static/scss/caipd.scss';

.table{
  width: 100%;
  border-style: solid;
  border-width: 1px 0;
  border-color: #F5F5F5;
}
.table-tr{
  height: 35px;
  border-style: solid;
  border-width: 0 0 1px 0;
  border-color: #F5F5F5;
}
.first-th{
  width: 25%;
  line-height: 30px;
  text-align: center;
  background-color: rgba(245,245,245,0.50);
}
.second-th{
  width: 75%;
  line-height: 30px;
  text-align: center;
}
</style>
