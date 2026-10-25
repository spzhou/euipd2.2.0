<!--
 * Author: 周朔鹏
 * Date: 2024-12-17
 * Email: 391902958@qq.com
 * Website: https://www.euipd.com
-->

<template>
  <div>
    <div class="dialog-container">
      <el-form
        :model="project"
        ref="preproject"
        label-width="130px"
        :show-message=false>
      <el-row>
        <el-col :span="11">
          <el-form-item label="预立项项目名称" required>
            <el-col :span="23">
              <el-input v-model="project.preProjectName"></el-input>
            </el-col>
          </el-form-item>
        </el-col>
        <el-col :span="6">
          <el-form-item label="开发版本">
            <el-input v-model="project.devVersion" placeholder="01.01.0001"></el-input>
          </el-form-item>
        </el-col>
      </el-row>
      <el-row>
        <el-col :span="17">
          <el-form-item label="所属产品" required>
            <product-owner-picker ref="productPicker" :value="project"></product-owner-picker>
          </el-form-item>
        </el-col>
      </el-row>
      <el-row>
        <el-col :span="23">
          <el-form-item label="项目简介" required>
            <el-input type="textarea" rows="5" v-model="project.preProjectIntroduce" placeholder="项目简介"></el-input>
          </el-form-item>
        </el-col>
      </el-row>
      <el-row style="margin-top: 5px;">
        <el-col :span="7">
          <el-form-item
              required
              label="系统工程师">
            <el-select v-model="project.systemManagerId" filterable placeholder="请选择">
              <el-option
                  v-for="item in systemManagers"
                  :key="item.id"
                  :label="item.trueName"
                  :value="item.id">
              </el-option>
            </el-select>
          </el-form-item>
        </el-col>
      </el-row>
      <el-row style="margin-top: 5px;">
        <el-col :span="7">
          <el-form-item label="产品经理" required>
            <el-col :span="24">
              <div v-if="isPM">
                {{ loginUser.trueName}}
              </div>
              <div v-else>
                {{""}}
              </div>
            </el-col>
          </el-form-item>
        </el-col>
      </el-row>

    </el-form>
    <div class="clearfix form-actions">
      <div style="display: flex; justify-content: center;">
        <button v-if="isPM" type="button" v-on:click="savePreProject()" class="ca-btn-primary">保 存</button>
        <button v-if="!isPM" type="button" class="ca-btn-primary disabled">保 存</button>
        <button v-on:click="close()" class="ca-btn-primary" type="button">
          关 闭
        </button>
      </div>
    </div>
    </div>
  </div>
</template>

<script>
import File from "../../components/rdms/file";
import Swal from "sweetalert2";
import DevVersionUtil from "@/utils/dev-version-util";
import ProductOwnerPicker from "@/components/rdms/product-owner-picker";
import { postWithMaintenance } from "@/shared/dev-version-guard";
export default {
  components: {
    File,
    ProductOwnerPicker,
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
      systemManagers: [],

      isPM: false,
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
    _this.listSystemManagerByCustomerId();

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
    /**
     * 保存客户信息
     */
    savePreProject() {
      let _this = this;
      // 保存校验
      if (1 !== 1
          || !Validator.require(_this.project.preProjectName, "项目名称",Swal)
          || !Validator.require(_this.project.preProjectIntroduce, "项目简介",Swal)
          || !Validator.require(_this.project.devVersion, "开发版本",Swal)
          || !Validator.require(_this.project.systemManagerId, "系统工程师",Swal)

          || !Validator.length(_this.project.preProjectName, "项目名称", 2, 50,Swal)
          || !Validator.length(_this.project.preProjectIntroduce, "项目简介", 5, 500,Swal)
          || !DevVersionUtil.requireValid(_this.project.devVersion, "开发版本", Swal)
          || !_this.$refs.productPicker.validate()
      ) {
        return;
      }

      _this.project.customerId = _this.loginCustomer.id;
      _this.project.productManagerId = _this.loginUser.customerUserId;
      _this.project.devVersion = String(_this.project.devVersion || '').trim();
      postWithMaintenance(_this, process.env.VUE_APP_SERVER + '/customer/pre-project/save', _this.project).then((response)=>{
        let resp = response.data;
        if (resp.success) {
          Toast.success("保存成功", Swal);
          _this.$parent.handleClose();
        } else if (!resp.cancelled) {
          Toast.warning(resp.message, Swal);
        }
      })
    },

    listProductManagerByCustomerId() {
      let _this = this;
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/project/listProductManagerByCustomerId/'+ _this.loginCustomer.id).then((response)=>{
        let resp = response.data;
        if (resp.success) {
          _this.productManagers = resp.content;
          if(_this.productManagers && _this.productManagers.length>0){
            _this.productManagers.forEach(item => {
              if(item.id === _this.loginUser.customerUserId){
                _this.isPM = true;
              }
            })
          }
        } else {
          Toast.warning(resp.message,Swal);
        }
      })
    },

    listSystemManagerByCustomerId() {
      let _this = this;
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/system-manager/listSystemManagerByCustomerId/'+ _this.loginCustomer.id).then((response)=>{
        let resp = response.data;
        if (resp.success) {
          _this.systemManagers = resp.content;
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
