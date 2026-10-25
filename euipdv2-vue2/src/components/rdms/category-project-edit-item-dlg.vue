<!--
 * Author: 周朔鹏
 * Date: 2025-02-12
 * Email: 391902958@qq.com
 * Website: https://www.euipd.com
-->

<template>
  <div class="dialog-container">
    <div style="padding-left: 80px; padding-right: 80px;">
      <el-row>
        <el-form label-width="200px">
          <el-form-item label="分类名称:">
            <el-col :span="16">
              <el-input v-model="category.name">
              </el-input>
            </el-col>
          </el-form-item>
          <el-form-item label="分类序号:">
            <el-col :span="4">
              <el-input type="number" v-model="category.sort">
              </el-input>
            </el-col>
          </el-form-item>
          <el-form-item label="是否隐藏:">
            <el-col :span="16">
              <el-checkbox v-model="isOpen">不显示</el-checkbox>
            </el-col>
          </el-form-item>
        </el-form>
      </el-row>
    </div>
    <div class="clearfix form-actions" style="margin-top: 30px;">
      <div style="display: flex; justify-content: center;">
        <button v-on:click="updateCategoryItem()" class="ca-btn-primary" type="button">
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
  name: "category-project-edit-item-dlg",

  data: function() {
    return {
      loginUser: {},
      loginCustomer: {},

      authUserIds: [],
      workers: [],
      workerList: [],
      titles: ['可选人员','授权人员'],

      categoryName: '',
      sort: 5,
      isOpen: false, // 1 隐藏 ; 0 显示
    }
  },

  props: {
    category: {
      default: null,
    },
    closeDlg: {
      type: Function,
      default: null
    },
  },

  computed: {
/*    getWorkerIdList(){
      let _this = this;
      return _this.workerList;
    }*/
  },

  mounted: function() {
    let _this = this;
    _this.getLoginUser();
    if(_this.category && _this.category.isOpen && _this.category.isOpen === 1){
      _this.isOpen = true;
    }else{
      _this.isOpen = false;
    }
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

      // transfer控件, 右侧值的变量必须不能赋值为空,否则报错
      if(_this.category.authUserIds !== null){
        _this.authUserIds = _this.category.authUserIds;
      }
    },

    updateCategoryItem() {
      let _this = this;
      // 保存校验
      if (1 !== 1
          || !Validator.require(_this.category.name, "分类名称", Swal)
          || !Validator.length(_this.category.name, "分类名称", 2, 20, Swal)
      ) {
        return;
      }

      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/category-project/updateCategoryItem', {
        name: _this.category.name,
        id: _this.category.id,
        parent: _this.loginCustomer.id,
        sort: _this.category.sort,
        customerId: _this.category.customerId,
        isOpen: _this.isOpen ? 1 : 0,
      }).then((response)=>{
        let resp = response.data;
        if (resp.success) {
          Toast.success("保存成功", Swal);
          _this.$bus.$emit('refresh-project-category-tabs', true);
          _this.$bus.$emit('refresh-category-project-edit-dlg', true);
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
