<!--
 * Author: 周朔鹏
 * Date: 2024-11-07
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
          <el-form-item label="可否下载:">
            <el-col :span="16">
              <el-checkbox v-model="isOpen">可下载</el-checkbox>
            </el-col>
          </el-form-item>
          <el-form-item label="是否隐藏:">
            <el-col :span="16">
              <el-checkbox v-model="notShow">不显示</el-checkbox>
            </el-col>
          </el-form-item>
        </el-form>
      </el-row>
      <el-row>
        <el-transfer style="margin-left: 80px; margin-bottom: 15px;"
                     ref="character-edit"
                     filterable
                     filter-placeholder="请输入姓名"
                     v-model="authUserIds"
                     :data="workerList"
                     :titles="titles">
        </el-transfer>
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
  name: "category-edit-item-dlg",

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
    if(_this.category &&_this.category.isOpen && _this.category.isOpen === 1){
      _this.isOpen = true;
    }else{
      _this.isOpen = false;
    }
    if(_this.category &&_this.category.isDelete && _this.category.isDelete === 1){
      _this.notShow = true;
    }else{
      _this.notShow = false;
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
      _this.listAllWorker();
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

      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/category/updateCategoryItem', {
        name: _this.category.name,
        id: _this.category.id,
        parent: "00000000",
        sort: _this.category.sort,
        customerId: _this.category.customerId,
        isOpen: _this.isOpen ? 1 : 0,
        isDelete: _this.notShow ? 1 : 0,
        authUserIds: _this.authUserIds,
      }).then((response)=>{
        let resp = response.data;
        if (resp.success) {
          Toast.success("保存成功", Swal);
          _this.$bus.$emit('refresh-edit-file-category', true);
          _this.$bus.$emit('refresh-product-plm-doc-page-category', true);
          _this.$bus.$emit('refresh-category-edit-dlg', true);
          _this.close();
        } else {
          Toast.warning(resp.message, Swal);
        }
      })
    },

    listAllWorker() {
      let _this = this;
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/worker/listAllWorkers/'+ _this.loginCustomer.id).then((response)=>{
        let resp = response.data;
        if (resp.success) {
          _this.workers = resp.content;
          _this.workers.forEach((worker) => {
            if(Tool.isNotEmpty_ES5(worker)){
              _this.workerList.push({
                label: worker.trueName,
                key: worker.id
              });
            }
          });
        } else {
          Toast.warning(resp.message,Swal);
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
