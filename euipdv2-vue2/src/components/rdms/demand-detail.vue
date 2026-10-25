<!--
 * Author: 周朔鹏
 * Date: 2023-12-24
 * Email: 391902958@qq.com
 * Website: https://www.euipd.com
-->

<template>
  <div class="paper-container">
    <el-row>
      <div style="margin: 30px 30px;">
        <el-form :label-position="labelPosition" label-width="150px" :model="formLabelAlign">
          <el-form-item label="需求名称" >
            <p style="font-weight: bold;"> {{demand.demandName}}</p>
          </el-form-item>
          <el-form-item label="需求描述">
            <div v-rich-html="demand.demandDescription"></div>
          </el-form-item>
          <el-form-item label="生效条件或使用前提">
            <div v-rich-html="demand.workCondition"></div>
          </el-form-item>
          <el-form-item label="关联项目">
            <div>{{ preProject.preProjectName }}</div>
          </el-form-item>
          <el-form-item label="需求方名称">
            <div>{{ demand.demandCustomerName }}</div>
          </el-form-item>
          <el-form-item label="需求确认人姓名">
            <div>{{ demand.confirmPersonName }}</div>
          </el-form-item>
          <el-form-item label="需求确认人电话">
            <div>{{ demand.confirmContactTel }}</div>
          </el-form-item>
          <el-row>
            <div style="width: 100%; display: flex; flex-wrap: wrap; justify-content: space-between;">
              <div v-for="item in imgList" >
                <el-image
                    :src="item"
                    style="width: 300px;"
                    :preview-src-list="imgList"
                ></el-image>
              </div>
            </div>
          </el-row>
<!--          <el-row>
            <div class="clearfix form-actions">
              <div style="display: flex; justify-content: center;">
                <button v-on:click="close()" class="ca-btn-primary" type="button">
                  关闭
                </button>
              </div>
            </div>
          </el-row>-->
        </el-form>
      </div>
    </el-row>
  </div>
</template>

<script>
import Swal from "sweetalert2";
import File from "../../components/rdms/file";
import FileSaver from 'file-saver';

export default {
  components: {
    File,
    FileSaver,
  },
  name: "demand-detail",

  props: {
    demandItem: {
      default: null,
    },
    closeFunction: {
      type: Function,
      default: null
    },
  },

  data: function () {
    return {
      loginUser: {},
      loginCustomer: {},

      labelPosition: 'right',
      formLabelAlign: {
        name: '需求名称',
        region: '',
        type: ''
      },

      demand: {},
      preProject: {},
      fileItems: [],
      imgList: [],
    }
  },

  mounted: function () {
    let _this = this;
    _this.getLoginUser();
    _this.demand = _this.demandItem;
    // console.log("_this.demand:", _this.demand);
    _this.getPreProjectSimpleInfo();
    _this.listAttachment(_this.demand.id);
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

    getPreProjectSimpleInfo() {
      let _this = this;
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/pre-project/getPreProjectSimpleInfo/'+ _this.demand.preProjectId).then((response)=>{
        let resp = response.data;
        if (resp.success) {
          _this.preProject = resp.content;
        } else {
          Toast.warning(resp.message,Swal);
        }
      })
    },

    listAttachment(itemId) {
      let _this = this;
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/file/rdms/list-file-info', {
        use: FILE_GROUPING.DEMAND.key,
        itemId: itemId,
        customerId: _this.loginCustomer.id,
      }).then((response)=>{
        let resp = response.data;
        if (resp.success) {
          _this.fileItems = resp.content;
          _this.fileItems.forEach((item, index)=>{
            _this.imgList.push(item.absPath);
          })
        } else {
          Toast.warning(resp.message,Swal);
        }
      })
    },

    close(){
      let _this = this;
      _this.closeFunction();
    },

  },
}
</script>

<style lang="scss" scoped>
@import './../../../public/static/scss/caipd.scss';

</style>

<style>
.el-form-item--mini.el-form-item, .el-form-item--small.el-form-item {
  margin-bottom: 8px !important;
}
</style>
