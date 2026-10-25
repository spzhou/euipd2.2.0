<!--
 * Author: 周朔鹏
 * Date: 2024-11-07
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
    <div style="padding-left: 282px; margin: 5px 0">
      <el-button v-on:click="addCategoryItem()" type="primary" size="small">
        添加分类
      </el-button>
      <el-button v-on:click="editCategoryItem()" type="primary" size="small">
        编辑分类
      </el-button>

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

    <!--添加分类-->
    <el-dialog
        width="29cm"
        append-to-body
        :visible.sync="addCategoryItemDlg"
        :before-close="handleClose"
        :close-on-click-modal=false>
      <div slot="title" style="font-size: 20px;">{{ dialogTitle }}</div>
      <div>
        <CategoryAddItemDlg
            :key="customerId"
            :customer-id="customerId"
            :close-dlg="closeDialog"
        ></CategoryAddItemDlg>
      </div>
    </el-dialog>

    <!--编辑分类-->
    <el-dialog
        width="29cm"
        append-to-body
        :visible.sync="editCategoryDlg"
        :before-close="handleClose"
        :close-on-click-modal=false>
      <div slot="title" style="font-size: 20px;">{{ dialogTitle }}</div>
      <div>
        <CategoryEditDlg
            :key="customerId"
            :customer-id="customerId"
            :close-dlg="closeDialog"
        ></CategoryEditDlg>
      </div>
    </el-dialog>

  </div>
</template>

<script>
import Swal from "sweetalert2";
import CategoryAddItemDlg from "@/components/rdms/category-add-item-dlg.vue";
import CategoryEditDlg from "@/components/rdms/category-edit-dlg.vue";

export default {
  name: "edit-file-category",

  components: {
    CategoryAddItemDlg,
    CategoryEditDlg,
  },

  props: {
    customerId: {
      default: null,
    },
    fileId: {
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
      titles: ['可选分类','文件分类'],

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
    _this.getCategoryList_all(_this.customerId);
    _this.getCategoryFileSimpleInfo(_this.fileId);

    _this.$bus.$on('refresh-edit-file-category', (data) => {
      if(data){
        _this.getCategoryList_all(_this.customerId);
      }
    });
  },

  beforeDestroy(){
    let _this = this;
    _this.$bus.$off('refresh-edit-file-category');
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

    getCategoryList_all(customerId) {
      let _this = this;
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/category/getCategoryList_all/' + customerId).then((response) => {
        let resp = response.data;
        if (resp.success) {
          _this.categoryList = [];
          _this.data = [];
          _this.categoryList = resp.content;
          _this.categoryList.forEach((item) => {
            _this.data.push({
              label: item.name,
              key: item.id,
            });
          });
        } else {
          Toast.warning(resp.message, Swal);
        }
      })
    },

    getCategoryFileSimpleInfo(fileId) {
      let _this = this;
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/category-file/getCategoryFileSimpleInfo/' + fileId).then((response) => {
        let resp = response.data;
        if (resp.success) {
          _this.categoryFile = resp.content;
          if(_this.categoryFile.categoryIdList === null){
            _this.value = [];
          }else{
            _this.value = _this.categoryFile.categoryIdList;
          }
        } else {
          Toast.warning(resp.message,Swal);
        }
      })
    },

    saveFileCategory() {
      let _this = this;
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/category-file/save', {
        fileId: _this.fileId,
        categoryIdList: _this.value,
      }).then((response)=>{
        let resp = response.data;
        if (resp.success) {
          Toast.success("保存成功", Swal);
          _this.close();
          _this.$bus.$emit('refresh-product-plm-doc-page', true);
        } else {
          Toast.warning(resp.message, Swal);
        }
      })
    },

    close(){
      let _this = this;
      _this.closeDlg();
    },

    addCategoryItem() {
      let _this = this;
      _this.addCategoryItemDlg = true;
      _this.dialogTitle = "添加分类";
    },
    editCategoryItem() {
      let _this = this;
      _this.editCategoryDlg = true;
      _this.dialogTitle = "编辑分类";
    },
    closeDialog() {
      let _this = this;
      _this.addCategoryItemDlg = false;
      _this.editCategoryDlg = false;
    },
    handleClose(done) {
      done();
    },
  },
};
</script>

<style lang="scss">
@import './../../../public/static/scss/caipd.scss';
</style>
