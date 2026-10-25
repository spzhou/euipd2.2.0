<!--
 * Author: 周朔鹏
 * Date: 2025-01-15
 * Email: 391902958@qq.com
 * Website: https://www.euipd.com
-->

<template>
  <div>
    <div class="dialog-container" v-loading="ipmtLoading">
      <div class="ca-form">
        <el-form label-width="150px" size="small">
          <el-form-item
              label="项目名称:"
              required>
            <el-input style="width: 300px;"
                      v-model="setupProject.projectName"
                      placeholder="请输入项目名称"
                      :disabled="saving"
            ></el-input>
          </el-form-item>
          <el-form-item required
              label="规划开发版本:">
            <el-input style="width: 300px;"
                      v-model="setupProject.devVersion"
                      placeholder="01.01.0001"
                      :disabled="saving"
            ></el-input>
          </el-form-item>
          <el-form-item
              required
              label="所属产品:">
            <div style="width: 500px;">
              <product-owner-picker ref="productPicker" :value="setupProject"></product-owner-picker>
            </div>
          </el-form-item>
          <el-form-item
              required
              label="项目简介:">
            <el-input style="width: 783px;"
                      type="textarea"
                      :autosize="{ minRows: 5, maxRows: 15}"
                      placeholder="请输入项目简介"
                      v-model="setupProject.projectIntroduce"
                      :disabled="saving"
            ></el-input>
          </el-form-item>
          <el-form-item
              required
              label="监管委员:">
            <el-select v-model="setupProject.supervise" filterable placeholder="请选择" :disabled="saving || !supervises.length">
              <el-option
                  v-for="item in supervises"
                  :key="item.id"
                  :label="item.trueName"
                  :value="item.id">
              </el-option>
            </el-select>
          </el-form-item>
        </el-form>
      </div>
      <div>
        <div class="clearfix form-actions">
          <div style="display: flex; justify-content: center;">
            <button v-on:click="create()" class="ca-btn-primary" type="button"
                    :disabled="saving || ipmtLoading || !supervises.length">
              {{ saving ? '创建中...' : '创建' }}
            </button>
            <button v-on:click="close()" class="ca-btn-primary" type="button" :disabled="saving">
              关闭
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>
<script>
import Swal from "sweetalert2";
import DevVersionUtil from "@/utils/dev-version-util";
import ProductOwnerPicker from "@/components/rdms/product-owner-picker";
import { postWithMaintenance } from "@/shared/dev-version-guard";

export default {

  name: 'project-create-direct',
  components: {
    ProductOwnerPicker,
  },
  props:{
    closeFunction: {
      type: Function,
      default: null
    },
  },

  data() {
    return {
      loginUser: {},
      loginCustomer: {},
      supervises: [],
      setupProject: {
        projectName: '',
        devVersion: '',
        projectIntroduce: '',
        supervise: '',
      },
      ipmtLoading: false,
      ipmtLoadFailed: false,
      saving: false,
    };
  },

  mounted: function() {
    let _this =this;
    _this.getLoginUser();
    _this.listIpmtByCustomerId();
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
    },

    create() {
      let _this = this;
      // 按钮权限 520103：仅最终管理人（以后台 buttonPolicy 为准）
      if (typeof Tool !== 'undefined' && Tool.hasButtonPermission
          && !Tool.hasButtonPermission('520103')) {
        Toast.warning("没有权限", Swal);
        return;
      }
      if (_this.saving) {
        Toast.warning("正在创建中，请稍候", Swal);
        return;
      }
      if (_this.ipmtLoading) {
        Toast.warning("IPMT 列表加载中，请稍候", Swal);
        return;
      }
      if (!_this.supervises.length) {
        Toast.warning("暂无可用的 IPMT/ITMT 委员，请先在资质设置中配置", Swal);
        return;
      }
      if (1 !== 1
          || !Validator.require(_this.setupProject.projectName, "项目名称",Swal)
          || !Validator.require(_this.setupProject.devVersion, "规划开发版本",Swal)
          || !Validator.require(_this.setupProject.projectIntroduce, "项目简介",Swal)
          || !Validator.require(_this.setupProject.supervise, "监管委员",Swal)

          || !Validator.length(_this.setupProject.projectName, "项目名称", 2, 50,Swal)
          || !Validator.length(_this.setupProject.projectIntroduce, "项目简介", 5, 1500,Swal)
          || !Validator.length(_this.setupProject.supervise, "监管委员", 8, 8,Swal)
          || !DevVersionUtil.requireValid(_this.setupProject.devVersion, "规划开发版本", Swal)
          || !_this.$refs.productPicker.validate()
      ) {
        return;
      }

      // 先快照请求数据；关闭对话框会销毁本组件，确认后再用快照调 API
      const payload = {
        customerId: _this.loginCustomer.id,
        projectName: (_this.setupProject.projectName || '').trim(),
        projectIntroduce: (_this.setupProject.projectIntroduce || '').trim(),
        supervise: _this.setupProject.supervise,
        devVersion: String(_this.setupProject.devVersion || '').trim(),
        productId: _this.setupProject.productId,
        newProductName: (_this.setupProject.newProductName || '').trim(),
        newProductCode: (_this.setupProject.newProductCode || '').trim(),
        loginUserId: _this.loginUser.customerUserId,
      };
      const ajax = _this.$ajax;
      const bus = _this.$bus;

      if (document.activeElement && typeof document.activeElement.blur === 'function') {
        document.activeElement.blur();
      }
      // 先关闭创建对话框，再弹出确认框
      if (typeof _this.closeFunction === 'function') {
        _this.closeFunction();
      }

      // 对话框关闭会销毁本组件，延后弹出确认框，避免依赖已销毁实例
      setTimeout(() => {
        Swal.fire({
          title: '确认',
          text: "确认创建项目吗?",
          icon: 'question',
          showCancelButton: true,
          confirmButtonColor: '#409EFF',
          cancelButtonColor: '#909399',
          confirmButtonText: '确认!',
          cancelButtonText: '取消!',
          heightAuto: false,
          returnFocus: false,
        }).then((result) => {
          if (!(result.isConfirmed || result.value)) {
            // 取消：不调用后台 API
            return;
          }
          postWithMaintenance({ $ajax: ajax }, process.env.VUE_APP_SERVER + '/customer/project/direct-setup', payload).then((response) => {
            let resp = response.data;
            if (resp.cancelled) {
              return;
            }
            if (resp.success) {
              const content = resp.content || {};
              const name = content.projectName || payload.projectName;
              const id = content.id || '';
              Toast.success(id ? `创建成功：${name}（${id}）` : `创建成功：${name}`, Swal);
              bus.$emit('refresh-setup-list', true);
              bus.$emit('notify-setup-count-flag', true);
            } else {
              Toast.warning(resp.message, Swal);
            }
          }).catch((e) => {
            console.error(e);
            Toast.error("创建失败，请重试", Swal);
          });
        }).catch((e) => {
          console.error(e);
        });
      }, 0);
    },

    listIpmtByCustomerId() {
      let _this = this;
      _this.ipmtLoading = true;
      _this.ipmtLoadFailed = false;
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/project/listIpmtByCustomerId/'+ _this.loginCustomer.id).then((response)=>{
        let resp = response.data;
        if (resp.success) {
          _this.supervises = resp.content || [];
          if (!_this.supervises.length) {
            Toast.warning("暂无可用的 IPMT/ITMT 委员，请先在资质设置中配置", Swal);
          }
        } else {
          _this.ipmtLoadFailed = true;
          _this.supervises = [];
          Toast.warning(resp.message,Swal);
        }
      }).catch((e) => {
        console.error(e);
        _this.ipmtLoadFailed = true;
        _this.supervises = [];
        Toast.error("加载 IPMT 列表失败", Swal);
      }).finally(() => {
        _this.ipmtLoading = false;
      });
    },

    close(){
      let _this = this;
      if (_this.saving) {
        return;
      }
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
