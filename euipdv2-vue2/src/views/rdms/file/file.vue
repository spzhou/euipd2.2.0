<!--
 * Author: 周朔鹏
 * Date: 2025-02-25
 * Email: 391902958@qq.com
 * Website: https://www.euipd.com
-->

<template>
  <div>
    <div style="position: relative;">
      <el-badge v-show="applyNum>0" :value="applyNum" class="item" style="position: absolute; left: 362px; top: 3px; z-index: 100;"></el-badge>
    </div>
    <el-tabs v-model="activeName" type="card" @tab-click="handleClick">
      <el-tab-pane label="我的文件" name="first">
        <div v-if="activeName === 'first'">
          <MyDoc
              id="first"
          ></MyDoc>
        </div>
      </el-tab-pane>
      <el-tab-pane label="下载记录" name="second">
        <div v-if="activeName === 'second'">
          <MyReference
              id="second"
          ></MyReference>
        </div>
      </el-tab-pane>
      <el-tab-pane label="文献检索" name="third">
        <div v-if="activeName === 'third'">
          <FileSearch
              id="third"
          ></FileSearch>
        </div>
      </el-tab-pane>
      <el-tab-pane label="文献审批" name="fourth">
        <div v-if="activeName === 'fourth'">
          <FileApplyApprove
              id="fourth"
          ></FileApplyApprove>
        </div>
      </el-tab-pane>

    </el-tabs>
  </div>
</template>

<script>
  import Pagination from "../../../components/pagination";
  import MyDoc from "@/views/rdms/file/my-doc.vue";
  import MyReference from "@/views/rdms/file/my-reference.vue";
  import FileSearch from "@/views/rdms/file/file-search.vue";
  import FileApplyApprove from "@/views/rdms/file/file-apply-approve.vue";
  import Swal from "sweetalert2";

  export default {
    components: {
      Pagination,
      MyDoc,
      MyReference,
      FileSearch,
      FileApplyApprove
    },
    name: "file",
    data: function() {
      return {
        loginUser: {},
        loginCustomer: {},
        activeName: 'first',
        applyNum: 0,
      }
    },
    mounted: function() {
      let _this = this;
      _this.getLoginUser();
      _this.getApplyNum();

      _this.$bus.$on('refresh-file-apply-num', (flag) => {
        if (flag) {
          _this.getApplyNum();
        }
      });
    },

    beforeDestroy() {
      let _this = this;
      _this.$bus.$off('refresh-file-apply-num');
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

      handleClick(tab, event) {
        // console.log(tab, event);
      },

      getApplyNum(){
        let _this = this;
        _this.$ajax.post(process.env.VUE_APP_SERVER + '/file/rdms/getApplyNum', {
          customerId: _this.loginCustomer.id,
          executorId: _this.loginUser.customerUserId
        }).then((response)=>{
          let resp = response.data;
          if (resp.success) {
            _this.applyNum = resp.content;
          } else {
            Toast.warning(resp.message,Swal);
          }
        })
      },
    }
  }
</script>

<style>
.el-input--mini .el-input__inner {
  height: 28px;
  line-height: 28px;
  text-align: center !important;
  width: 222px;
}
</style>
