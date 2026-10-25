<!--
 * Author: 周朔鹏
 * Date: 2025-02-11
 * Email: 391902958@qq.com
 * Website: https://www.euipd.com
-->

<template>
  <div class="dialog-container">
    <div style="margin: 0 20px;">
      <el-table
          :data="categoryList"
          border
          stripe
          highlight-current-row
          style="width: 100%">
        <el-table-column  label="序号" align="center" width="50">
          <template slot-scope="scope">
            {{scope.$index + 1}}
          </template>
        </el-table-column>
        <el-table-column
            prop="name"
            align="center"
            show-overflow-tooltip
            label="分类名称"
            min-width="150"
        ></el-table-column>
        <el-table-column
            prop="sort"
            align="center"
            show-overflow-tooltip
            label="分类排序"
            min-width="80"
        ></el-table-column>
        <el-table-column
            prop="isOpen"
            :formatter="isShowFormat"
            align="center"
            show-overflow-tooltip
            label="是否显示"
            min-width="80"
        ></el-table-column>
        <el-table-column
            fixed="right"
            label="操作"
            align="center"
            min-width="100">
          <template slot-scope="scope">
            <div style="display: flex; flex-direction: row; justify-content: center;">
              <el-button type="primary" plain size="small" v-on:click.stop="editCategoryItem(scope.row)">
                编辑分类
              </el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>
    </div>
    <div class="clearfix form-actions" style="margin-top: 30px;">
      <div style="display: flex; justify-content: center;">
        <button v-on:click="close()" class="ca-btn-primary" type="button">
          关闭
        </button>
      </div>
    </div>

    <!--编辑分类条目-->
    <el-dialog
        width="23cm"
        append-to-body
        :visible.sync="addCategoryItemDlg"
        :before-close="handleClose"
        :close-on-click-modal=false>
      <div slot="title" style="font-size: 20px;">{{ dialogTitle }}</div>
      <div>
        <CategoryProjectEditItemDlg
            :key="category.id"
            :category="category"
            :close-dlg="closeDialog"
        ></CategoryProjectEditItemDlg>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import FileRdms from "@/components/rdms/file-rdms";
import Swal from "sweetalert2";
import CategoryProjectEditItemDlg from "@/components/rdms/category-project-edit-item-dlg.vue";

export default {
  components: {
    CategoryProjectEditItemDlg,
    FileRdms,
  },
  name: "category-project-edit-dlg",

  data: function() {
    return {
      loginUser: {},
      loginCustomer: {},

      category: {},
      categoryList: [],

      categoryName: '',
      addCategoryItemDlg: false,
      dialogTitle: '',
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

    _this.$bus.$on('refresh-category-project-edit-dlg', (data) => {
      if(data){
        _this.getCategoryList_all(_this.loginCustomer.id);
      }
    });

  },

  beforeDestroy(){
    let _this = this;
    _this.$bus.$off('refresh-category-project-edit-dlg');
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
    },

    getCategoryList_all(customerId) {
      let _this = this;
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/category-project/getCategoryList_all/' + customerId).then((response) => {
        let resp = response.data;
        if (resp.success) {
          _this.categoryList = resp.content;
        } else {
          Toast.warning(resp.message, Swal);
        }
      })
    },

    close(){
      let _this = this;
      _this.closeDlg();
    },

    isShowFormat(row){
      if(row.isOpen){
        return "隐藏";
      }else{
        return "显示";
      }
    },

    editCategoryItem(category) {
      let _this = this;
      _this.category = category;
      _this.addCategoryItemDlg = true;
      _this.dialogTitle = "编辑分类条目";
    },
    closeDialog() {
      let _this = this;
      _this.addCategoryItemDlg = false;
    },
    handleClose(done) {
      done();
    },
  }
}
</script>

<style lang="scss">
@import './../../../public/static/scss/caipd.scss';
</style>
