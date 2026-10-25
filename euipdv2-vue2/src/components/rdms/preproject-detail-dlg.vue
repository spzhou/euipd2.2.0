<!--
 * Author: 周朔鹏
 * Date: 2024-01-12
 * Email: 391902958@qq.com
 * Website: https://www.euipd.com
-->

<template>
  <div class="dialog-container">
    <el-row>
      <div style="margin: 0 40px 20px;">
        <el-form :label-position="labelPosition" label-width="150px" :model="project">
          <el-form-item label="预立项项目名称" >
            <p style="font-weight: bold;"> {{project.preProjectName}}</p>
          </el-form-item>
          <el-form-item label="开发版本">
            <div>{{ project.devVersion }}</div>
          </el-form-item>
          <el-form-item label="项目简介">
            <div v-rich-html="project.preProjectIntroduce"></div>
          </el-form-item>
          <el-form-item label="产品经理">
            <div>{{ project.productManagerName }}</div>
          </el-form-item>
        </el-form>
      </div>
    </el-row>
    <div style="width: 100%; height: 50px;"></div>
  </div>
</template>

<script>
import File from "../../components/rdms/file";
import Swal from "sweetalert2";
export default {
  components: {
    File,
  },
  name: "project-prepare-create",

  props:{
    preProject: {
      default: null,
    },
  },

  data: function() {
    return {
      loginUser: {},
      loginCustomer: {},

      project: {},
      productManagers: [],

      labelPosition: 'right',

    }
  },

  computed: {
  },

  mounted: function() {
    let _this =this;
    _this.getLoginUser();
    if(_this.preProject === null || _this.preProject === undefined){
      _this.project = {};
    }else{
      _this.project = _this.preProject;
    }

    _this.listProductManagerByCustomerId();

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

    listProductManagerByCustomerId() {
      let _this = this;
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/project/listProductManagerByCustomerId/'+ _this.loginCustomer.id).then((response)=>{
        let resp = response.data;
        if (resp.success) {
          _this.productManagers = resp.content;
        } else {
          Toast.warning(resp.message,Swal);
        }
      })
    },

    close(){
      let _this = this
      _this.$parent.handleClose();
    },
  }
}
</script>

<style lang="scss">
@import './../../../public/static/scss/caipd.scss';

</style>
