<!--
 * Author: 周朔鹏
 * Date: 2024-11-24
 * Email: 391902958@qq.com
 * Website: https://www.euipd.com
-->

<template>
  <div class="a4-paper-big-simple">
    <div style="display: flex; flex-direction: column; align-items: center; justify-content: center; margin: 50px 20px;">
      <el-table
          :data="authList"
          border
          stripe
          highlight-current-row
          style="width: 100%">
        <el-table-column label="序号" align="center" width="80">
          <template slot-scope="scope">
            {{ scope.$index + 1 }}
          </template>
        </el-table-column>
        <el-table-column
            prop="name"
            align="center"
            label="名称"
            width="150"
        ></el-table-column>
        <el-table-column
            prop="note"
            align="left"
            label="描述"
        ></el-table-column>
        <el-table-column
            fixed="right"
            label="操作"
            align="center"
            width="90">
          <template slot-scope="scope">
            <el-button v-on:click="deleteKey(scope.row)" type="primary" size="mini">
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>
    <div class="clearfix form-actions" style="margin-top: 80px;">
      <div style="display: flex; justify-content: center;">
        <button v-on:click="createKey()" class="ca-btn-primary" type="button">
          创建
        </button>
      </div>
    </div>

    <!--    添加Auth-->
    <el-dialog
        :visible.sync="jobLevelDialogVisible"
        width="23cm"
        :before-close="handleClose"
        :close-on-click-modal=false>
      <div slot="title" style="font-size: 20px;">{{ dialogTitle }}</div>
      <div>
        <AuthEdit></AuthEdit>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import Swal from "sweetalert2";
import AuthEdit from "@/components/rdms/auth-edit.vue";

export default {
  name: 'edit-auth-manage',

  components: {
    AuthEdit,
  },

  data: function () {
    return {
      loginUser: {},
      loginCustomer: {},

      jobLevelDialogVisible: false,
      dialogTitle: "",
      standardManhour: {},

      value: [],
      authList: [],
      titles: ['可选人员', '项目经理'],

      customerId: '',
    };
  },

  computed: {},

  mounted: function () {
    let _this = this;
    _this.getLoginUser();

    _this.$bus.$on('close-auth-edit-dlg', (data) => {
      if (data) {
        _this.getAccessByCustomerId();
      }
    });

  },

    beforeDestroy(){
    let _this = this;
    _this.$bus.$off('close-auth-edit-dlg');
  },

  methods: {
    getLoginUser() {
      let _this = this;
      if (Tool.getLoginUser().userGroup === LOGIN_USER_GROUP.CUSTOMER.key) {
        _this.loginUser = Tool.getLoginUser().adminUser;
        _this.loginCustomer = Tool.getLoginUser();
      } else {
        _this.loginUser = Tool.getLoginUser();
        _this.loginCustomer = Tool.getLoginUser().customer;
      }

      _this.getAccessByCustomerId();
    },

    createKey(row) {
      let _this = this;
      _this.jobLevelDialogVisible = true;
      _this.dialogTitle = "添加第三方登录授权";
    },
    handleClose(done) {
      done();
    },


    getAccessByCustomerId() {
      let _this = this;
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/system/auth2/getAccessByCustomerId/' + _this.loginCustomer.id).then((response) => {
        let resp = response.data;
        if (resp.success) {
          _this.authList = resp.content;
        } else {
          Toast.warning(resp.message, Swal);
        }
      })
    },

    deleteKey(authority) {
      let _this = this;
      Swal.fire({
        title: '警告',
        text: "删除后不可恢复!!!",
        icon: 'warning',
        showCancelButton: true,
        confirmButtonColor: '#409EFF',
        cancelButtonColor: '#909399',
        confirmButtonText: '确认!',
        cancelButtonText: '取消!'
      }).then((result) => {
        if (result.value) {
          _this.$ajax.delete(process.env.VUE_APP_SERVER + '/system/auth2/del/' + authority.id).then((response) => {
            let resp = response.data;
            if (resp.success) {
              _this.getAccessByCustomerId();
            } else {
              Toast.warning(resp.message, Swal);
            }
          })
        }
      })
    },
  },

};
</script>

<style lang="scss">
@import './../../../public/static/scss/caipd.scss';
</style>
