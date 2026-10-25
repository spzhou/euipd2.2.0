<!--
 * Author: 周朔鹏
 * Date: 2025-02-12
 * Email: 391902958@qq.com
 * Website: https://www.euipd.com
-->

<template>
  <div>
    <div style="display: flex; justify-content: center; margin-top: 10px;">
      <el-transfer
          filterable
          filter-placeholder="分类名称"
          v-model="value"
          :data="data"
          :titles="titles">
      </el-transfer>
    </div>
    <div class="clearfix form-actions" style="margin-top: 50px;">
      <div style="display: flex; justify-content: center;">
        <button v-on:click="saveFileCategory()" class="ca-btn-primary" type="button">
          保存
        </button>
        <button type="button" v-on:click="close()" class="ca-btn-primary">
          取消
        </button>
      </div>
    </div>
  </div>
</template>

<script>
import Swal from "sweetalert2";

export default {
  name: "edit-project-category",

  components: {
  },

  props: {
    projectId: {
      default: null,
    },
    closeDlg: {
      type: Function,
      default: null
    },
  },

  data: function() {
    return {
      loginUser: {},
      loginCustomer: {},
      categoryFile: {},

      data: [],
      value: [],
      titles: ['可选分类','项目分类'],

      categoryList: [],

      addCategoryItemDlg: false,
      editCategoryDlg: false,
      dialogTitle: '',
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

      _this.getCategoryList_all(_this.loginCustomer.id);
      _this.getCategoryList(_this.projectId);
    },

    getCategoryList_all(customerId) {
      let _this = this;
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/category-project/getCategoryList_all/' + customerId).then((response) => {
        let resp = response.data;
        if (resp.success) {
          _this.categoryList = [];
          _this.data = [];
          _this.categoryList = resp.content;
          _this.categoryList.forEach((item) => {
            _this.data.push({
              label: item.name,
              key: item.id,
              disabled: item.id === _this.loginCustomer.id,  //全部项目 的预制分类ID
            });
          });
        } else {
          Toast.warning(resp.message, Swal);
        }
      })
    },

    getCategoryList(project) {
      let _this = this;
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/category-project/getCategoryList/' + project).then((response) => {
        let resp = response.data;
        if (resp.success) {
          if(resp.content){
            _this.value = resp.content;  //Json 字符串
          }else{
            _this.value = [_this.loginCustomer.id];
          }
        } else {
          Toast.warning(resp.message, Swal);
        }
      })
    },

    saveFileCategory() {
      let _this = this;
      let categoryIdList = JSON.stringify(_this.value);
      let formData = new window.FormData();
      formData.append('projectId', _this.projectId);
      formData.append('categoryIdList', categoryIdList);
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/category-project/saveCategoryList', formData).then((response)=>{
        let resp = response.data;
        if (resp.success) {
          Toast.success("保存成功", Swal);
          _this.close();
          // _this.$bus.$emit('refresh-product-plm-doc-page', true);
        } else {
          Toast.warning(resp.message, Swal);
        }
      })
    },

    close(){
      let _this = this;
      _this.closeDlg();
    },

  },
};
</script>

<style lang="scss">
@import './../../../public/static/scss/caipd.scss';
</style>
