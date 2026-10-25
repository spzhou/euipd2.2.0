<!--
 * Author: 周朔鹏
 * Date: 2025-08-04
 * Email: 391902958@qq.com
 * Website: https://www.euipd.com
-->

<template>
  <div>
<!--Tab 卡片-->
    <div class="row">
      <div class="col-md-12">
        <div class="tabbable">
          <ul class="nav nav-tabs" id="myTab">
            <li :class="{active : activePageId==='home'}">
<!--              <a @click="tabHomeClick">-->
              <a>
                <i class="green ace-icon fa fa-home bigger-120"></i>
                机构列表
              </a>
            </li>
          </ul>
<!--客户列表-->
          <div class="tab-content">
            <div id="home" class="tab-pane fade" :class="{active : activePageId === 'home', in : activePageId === 'home' }">
              <el-row>
                <div style="display: flex; flex-direction: row; justify-content: space-between; align-items: center;">
                  <div>
                    <div class="input-group">
                      <input  v-model="keyWord" type="text" class="form-control" style="border-radius: 4px;" name="keywords" placeholder="团队名称关键词" />
                      <div class="input-group-btn">
                        <button v-on:click="customerList(1)" type="button" class="ca-btn-search">
                          <i class="ace-icon fa fa-search icon-on-right bigger-110"></i>
                        </button>
                      </div>
                    </div>
                  </div>
                  <div>
                  </div>
                </div>
              </el-row>
              <div class="hr hr-18 dotted hr-double"></div>
              <div>
                <div style="display: flex; justify-content: space-between;">
                  <pagination style="display: block;" ref="pagination" v-bind:list="customerList" v-bind:itemCount="6"></pagination>
                  <div>
                    <el-button type="primary" size="small" style="margin-right: 15px;" @click="addCustomer()">
                      添加机构/团队
                    </el-button>
                  </div>
                </div>
                <el-table
                    :data="customers"
                    border
                    stripe
                    highlight-current-row
                    style="width: 100%">
                  <el-table-column  label="序号" align="center" width="50">
                    <template slot-scope="scope">
                      {{scope.$index+1}}
                    </template>
                  </el-table-column>
                  <el-table-column
                      prop="id"
                      align="center"
                      label="机构ID"
                      width="100"
                  ></el-table-column>
                  <el-table-column
                      prop="loginName"
                      min-width="100"
                      show-overflow-tooltip
                      align="center"
                      label="登录名"
                  ></el-table-column>
                  <el-table-column
                      prop="customerName"
                      show-overflow-tooltip
                      align="center"
                      min-width="250"
                      label="团队名称"
                  ></el-table-column>
                  <el-table-column
                      prop="bossName"
                      min-width="110"
                      align="center"
                      label="最终管理人"
                  ></el-table-column>
                  <el-table-column
                      prop="adminName"
                      min-width="110"
                      align="center"
                      label="机构管理员"
                  ></el-table-column>
                  <el-table-column
                      prop="contactPhone"
                      min-width="110"
                      align="center"
                      label="机构联系电话"
                  ></el-table-column>
                  <el-table-column
                      prop="statusName"
                      align="center"
                      min-width="110"
                      sortable
                      label="机构状态"
                  ></el-table-column>
                  <el-table-column
                      prop="createTimeStr"
                      align="center"
                      min-width="150"
                      label="创建时间"
                  ></el-table-column>
                  <el-table-column
                      fixed="right"
                      label="操作"
                      align="center"
                      width="500">
                    <template slot-scope="scope">
                      <el-button v-on:click.stop="settingBoss(scope.row)" type="primary" plain size="mini">
                        设置最终管理人
                      </el-button>
                      <el-button v-on:click.stop="modifyCustomer(scope.row)" type="primary" plain size="mini">
                        编辑信息
                      </el-button>
                      <el-button v-if="scope.row.id !== SUPER_ADMIN_ID" v-on:click.stop="changeAdmin(scope.row)" type="primary" plain size="mini">
                        变更机构管理员
                      </el-button>
                      <el-button v-on:click.stop="changeAdminPassword(scope.row)" type="primary" plain size="mini">
                        修改密码
                      </el-button>
                      <el-button v-if="scope.row.id !== SUPER_ADMIN_ID" v-on:click.stop="del(scope.row)" type="warning" plain size="mini">
                        删除机构
                      </el-button>
                    </template>
                  </el-table-column>
                </el-table>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 添加机构/团队-->
    <el-dialog
        :visible.sync="addCustomerDialogVisible"
        width="23cm"
        :before-close="handleClose"
        :close-on-click-modal=false>
      <div slot="title" style="font-size: 20px;">{{ dialogTitle }}</div>
      <div v-if="addCustomerDialogVisible">
        <CustomerCreateDlg
            :key="random"
            :edit-type="EDIT_TYPE.CREATE.key"
            :close-dlg="closeDialog"
        ></CustomerCreateDlg>
      </div>
    </el-dialog>

    <!-- 修改机构-->
    <el-dialog
        :visible.sync="modifyCustomerDlg"
        width="29cm"
        :before-close="handleClose"
        :close-on-click-modal=false>
      <div slot="title" style="font-size: 20px;">{{ dialogTitle }}</div>
      <div v-if="modifyCustomerDlg">
        <CustomerCreateDlg
            :key="random"
            :edit-type="EDIT_TYPE.MODIFY.key"
            :modified-customer="modifiedCustomer"
            :close-dlg="closeDialog"
        ></CustomerCreateDlg>
      </div>
    </el-dialog>

    <!-- 变更管理员-->
    <el-dialog
        :visible.sync="changeAdminDlg"
        width="29cm"
        :before-close="handleClose"
        :close-on-click-modal=false>
      <div slot="title" style="font-size: 20px;">{{ dialogTitle }}</div>
      <div v-if="changeAdminDlg">
        <CustomerChangeAdminDlg
            :key="random"
            :modified-customer="modifiedCustomer"
            :close-dlg="closeDialog"
        ></CustomerChangeAdminDlg>
      </div>
    </el-dialog>

    <!-- 变更管理员密码-->
    <el-dialog
        :visible.sync="changeAdminPasswordDlg"
        width="29cm"
        :before-close="handleClose"
        :close-on-click-modal=false>
      <div slot="title" style="font-size: 20px;">{{ dialogTitle }}</div>
      <div v-if="changeAdminPasswordDlg">
        <CustomerChangePasswordDlg
            :key="random"
            :modified-customer="modifiedCustomer"
            :close-dlg="closeDialog"
        ></CustomerChangePasswordDlg>
      </div>
    </el-dialog>

    <!-- 设定最终管理人-->
    <el-dialog
        :visible.sync="settingBossDlg"
        width="23cm"
        :before-close="handleClose"
        :close-on-click-modal=false>
      <div slot="title" style="font-size: 20px;">{{ dialogTitle }}</div>
      <div v-if="settingBossDlg">
        <SettingBossDlg
            :key="random"
            :modified-customer="modifiedCustomer"
            :close-dlg="closeDialog"
        ></SettingBossDlg>
      </div>
    </el-dialog>

    <!-- 删除机构-->
    <el-dialog
        :visible.sync="deleteDlg"
        width="14cm"
        :close-on-click-modal=false>
      <div slot="title" style="font-size: 20px;">删除机构</div>
      <div v-if="deleteDlg">
        <el-alert type="error" :closable="false" show-icon
                  :title="'即将删除“' + deleteTarget.customerName + '”，删除后不可恢复。'"
                  style="margin-bottom: 15px;"></el-alert>
        <el-form label-width="90px" @submit.native.prevent>
          <el-form-item label="机构名称">
            <el-input v-model="deleteForm.customerName" placeholder="请手动输入完整的机构名称"
                      @input="deleteVerified = false"></el-input>
          </el-form-item>
          <el-form-item label="超管密码">
            <el-input v-model="deleteForm.password" type="password" show-password autocomplete="new-password"
                      placeholder="请输入当前超管的登录密码" @input="deleteVerified = false"></el-input>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" plain :loading="deleteChecking"
                       :disabled="!deleteForm.customerName || !deleteForm.password"
                       v-on:click="verifyDelete()">验证</el-button>
            <span v-if="deleteVerified" style="margin-left: 10px; color: #67C23A;">验证通过</span>
          </el-form-item>
        </el-form>
      </div>
      <div slot="footer">
        <el-button v-on:click="deleteDlg = false">取消</el-button>
        <el-button type="danger" :disabled="!deleteVerified" v-on:click="confirmDelete()">确认删除</el-button>
      </div>
    </el-dialog>

    <!-- 机构基本信息-->
    <el-dialog
        :visible.sync="detailCustomerDlg"
        width="29cm"
        :before-close="handleClose"
        :close-on-click-modal=false>
      <div slot="title" style="font-size: 20px;">{{ dialogTitle }}</div>
      <div v-if="detailCustomerDlg">
        <CustomerInfo
            :key="customerItem.id"
            :customer-id="customerItem.id"
        ></CustomerInfo>
      </div>
    </el-dialog>

  </div>
</template>

<script>
  import Pagination from "../../../components/pagination";
  import CustomerInfo from "@/views/rdms/system/customer-info.vue";
  import CustomerCreate from "@/components/rdms/customer-create-dlg.vue";
  import Swal from "sweetalert2";
  import CustomerCreateDlg from "@/components/rdms/customer-create-dlg.vue";
  import CustomerChangeAdminDlg from "@/components/rdms/customer-change-admin-dlg.vue";
  import CustomerChangePasswordDlg from "@/components/rdms/customer-change-password-dlg.vue";
  import SettingBossDlg from "@/components/rdms/setting-boss-dlg.vue";
  import { fetchLoginSalt, digestPassword } from "@/shared/password";

  export default {
    components: {
      CustomerCreateDlg,
      Pagination,
      CustomerInfo,
      CustomerCreate,
      CustomerChangeAdminDlg,
      CustomerChangePasswordDlg,
      SettingBossDlg,
    },
    name: "customer",
    data: function() {
      return {
        loginUser: {},
        loginCustomer: {},

        customer: {},
        customers: [],

        random:0,
        keyWord: '',
        currentCustomerId: null,
        activePageId: 'home',
        customerItem: {},
        modifiedCustomer: '',

        showProfileDlg: false,
        dialogTitle: '',
        addCustomerDialogVisible: false,
        modifyCustomerDlg: false,
        changeAdminDlg: false,
        changeAdminPasswordDlg: false,
        settingBossDlg: false,
        detailCustomerDlg: false,
        deleteDlg: false,
        deleteTarget: {},
        deleteForm: { customerName: '', password: '' },
        deleteVerified: false,
        deleteChecking: false,

        detailPageShow: false,
        modifyPageShow: false,
        selectCoursePageShow: false,

        customerEditIsActive: false,
        customerQueryIsActive: false,
        customerDetailIsActive: false,
        customerModifyIsActive: false,
        selectCourseIsActive: false,

        EDIT_TYPE: EDIT_TYPE,
        FILE_GROUPING: FILE_GROUPING,
        SUPER_ADMIN_ID: SUPER_ADMIN_ID,
      }
    },
    mounted: function() {
      let _this = this;
      _this.random = Math.random();
      _this.$refs.pagination.size = 12;
      _this.customerList(1);

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

      /**
       * 查找是否有权限
       * @param id
       */
      hasResource(id) {
        return Tool.hasResource(id);
      },

      rowClick(row, column, e){
        let _this = this;
        _this.customerItem = row;
        _this.detailCustomerDlg = true;
        _this.dialogTitle = "机构信息";
      },

      handleClose(done) {
        done();
      },

      closeDialog() {
        let _this = this;
        _this.addCustomerDialogVisible = false;
        _this.modifyCustomerDlg = false;
        _this.changeAdminDlg = false;
        _this.changeAdminPasswordDlg = false;
        _this.settingBossDlg = false;
        _this.detailCustomerDlg = false;
        _this.customerList(1);
      },

      addCustomer() {
        let _this = this;
        _this.addCustomerDialogVisible = true;
        _this.dialogTitle = "添加机构/团队";
      },

      modifyCustomer(customer) {
        let _this = this;
        _this.random = Math.random();
        _this.modifiedCustomer = customer;
        _this.modifyCustomerDlg = true;
        _this.dialogTitle = "修改机构信息";
      },

      changeAdmin(customer) {
        let _this = this;
        _this.random = Math.random();
        _this.modifiedCustomer = customer;
        _this.changeAdminDlg = true;
        _this.dialogTitle = "变更管理员";
      },

      changeAdminPassword(customer) {
        let _this = this;
        _this.random = Math.random();
        _this.modifiedCustomer = customer;
        _this.changeAdminPasswordDlg = true;
        _this.dialogTitle = "修改管理员密码";
      },

      settingBoss(customer) {
        let _this = this;
        _this.random = Math.random();
        _this.modifiedCustomer = customer;
        _this.settingBossDlg = true;
        _this.dialogTitle = "设置最终管理人";
      },

      /**
       * 列表查询
       */
      customerList(page) {
        let _this = this;
        _this.$ajax.post(process.env.VUE_APP_SERVER + '/system/rdms/customer/customerList', {
          page: page,
          size: _this.$refs.pagination.size,
          keyWord: _this.keyWord,
        }).then((response)=>{
          let resp = response.data;
          if (resp.success) {
            _this.customers = resp.content.list;
            _this.$refs.pagination.render(page, resp.content.total);
          }
        })
      },

      /**
       * 点击【删除机构】：手动输入机构名称与超管密码，服务端验证通过后才能确认
       */
      del(customer) {
        let _this = this;
        _this.deleteTarget = customer;
        _this.deleteForm = { customerName: '', password: '' };
        _this.deleteVerified = false;
        _this.deleteDlg = true;
      },

      /** 删除接口的请求体：机构名称原样提交，密码按超管手机号取盐后提交摘要 */
      deletePayload() {
        let _this = this;
        let superPhone = _this.loginCustomer.contactPhone || _this.loginCustomer.loginName;
        return fetchLoginSalt(superPhone).then((salt) => ({
          id: _this.deleteTarget.id,
          customerName: _this.deleteForm.customerName,
          password: digestPassword(_this.deleteForm.password, salt),
        }));
      },

      verifyDelete() {
        let _this = this;
        _this.deleteChecking = true;
        _this.deletePayload().then((body) => {
          return _this.$ajax.post(process.env.VUE_APP_SERVER + '/system/rdms/customer/delete-check', body);
        }).then((response) => {
          let resp = response.data;
          _this.deleteVerified = !!resp.success;
          if (!resp.success) {
            Toast.warning(resp.message || "验证未通过", Swal);
          }
        }).catch((message) => {
          Toast.warning(typeof message === 'string' ? message : "验证失败，请稍后再试", Swal);
        }).finally(() => {
          _this.deleteChecking = false;
        });
      },

      confirmDelete() {
        let _this = this;
        Swal.fire({
          title: '删除后，不可恢复',
          text: '确定删除“' + _this.deleteTarget.customerName + '”？',
          icon: 'warning',
          showCancelButton: true,
          focusCancel: true,
          confirmButtonColor: '#F56C6C',
          cancelButtonColor: '#909399',
          confirmButtonText: '确认删除',
          cancelButtonText: '取消'
        }).then((result) => {
          if (!result.value) {
            return;
          }
          _this.deletePayload().then((body) => {
            return _this.$ajax.post(process.env.VUE_APP_SERVER + '/system/rdms/customer/delete', body);
          }).then((response) => {
            let resp = response.data;
            if (resp.success) {
              Toast.success("删除成功！", Swal);
              _this.deleteDlg = false;
              _this.customerList(1);
              _this.detailPageShow = false;
            } else {
              _this.deleteVerified = false;
              Toast.warning(resp.message || "删除失败", Swal);
            }
          }).catch((message) => {
            Toast.warning(typeof message === 'string' ? message : "删除失败，请稍后再试", Swal);
          });
        });
      },

    }
  }
</script>

<style lang="scss">
@import './../../../../public/static/scss/caipd.scss';

</style>
