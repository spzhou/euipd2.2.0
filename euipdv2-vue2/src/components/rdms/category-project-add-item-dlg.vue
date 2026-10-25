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
              <el-input v-model="categoryName">
              </el-input>
            </el-col>
          </el-form-item>
          <el-form-item label="排序编号:">
            <el-row>
              <el-col :span="4">
                <el-input type="number" v-model="sort" @change="sortChang">
                </el-input>
              </el-col>
              <el-col :span="12">
                <small style="margin-left: 10px;"><em>说明: 分类卡片的排列顺序</em></small>
              </el-col>
            </el-row>

          </el-form-item>
        </el-form>
      </el-row>
    </div>
    <div class="clearfix form-actions" style="margin-top: 30px;">
      <div style="display: flex; justify-content: center;">
        <button v-on:click="saveCategoryItem()" class="ca-btn-primary" type="button">
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
  name: "category-project-add-item-dlg",

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
      isOpen: false,
      notShow: false,
    }
  },

  props: {
    customerId: {
      default: null,
    },
    closeDlg: {
      type: Function,
      default: null
    },
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

    sortChang(){
      let _this = this;
      if(!_this.sort || _this.sort < 0){
        _this.sort = 5;
      }
    },

    saveCategoryItem() {
      let _this = this;
      // 保存校验
      if (1 !== 1
          || !Validator.require(_this.categoryName, "分类名称", Swal)
          || !Validator.length(_this.categoryName, "分类名称", 2, 20, Swal)
      ) {
        return;
      }

      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/category-project/save', {
        name: _this.categoryName,
        customerId: _this.loginCustomer.id,
        parent: _this.loginCustomer.id,
        sort: _this.sort,
      }).then((response)=>{
        let resp = response.data;
        if (resp.success) {
          Toast.success("保存成功", Swal);
          _this.$bus.$emit('refresh-project-category-tabs', true);
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
