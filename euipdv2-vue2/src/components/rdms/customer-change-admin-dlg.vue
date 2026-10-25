<!--
 * Author: 周朔鹏
 * Date: 2024-12-03
 * Email: 391902958@qq.com
 * Website: https://www.euipd.com
-->

<template>
  <div>
    <div style="width: 100%; margin-bottom: 80px; display: flex; justify-content: center;">
      <div style="margin-right: 80px;">
        <el-form label-width="150px">
          <el-form-item label="机构名称:">
            <div>
              {{ customer.customerName }}
            </div>
          </el-form-item>
          <el-form-item label="新管理员:" required>
            <el-select v-model="adminWorkerId" filterable placeholder="请选择">
              <el-option
                  v-for="item in workerList"
                  :key="item.id"
                  :label="item.trueName"
                  :value="item.id">
              </el-option>
            </el-select>
          </el-form-item>
        </el-form>
      </div>
    </div>
    <div class="clearfix form-actions">
      <div style="display: flex; justify-content: center;">
        <button type="button" v-on:click="saveCustomerAdmin()" class="ca-btn-primary">保 存</button>
        <button type="button" v-on:click="close()" class="ca-btn-primary">取 消</button>
      </div>
    </div>
  </div>

</template>

<script>
import Swal from "sweetalert2";

export default {
  components: {
  },
  name: "customer-change-admin-dlg",

  props: {
    modifiedCustomer: {
      default: null,
    },
    closeDlg: {
      type: Function,
      default: null,
    },
  },


  data: function () {
    return {
      loginUser: {},
      loginCustomer: {},
      superInfo: {},
      adminWorkerId: '',
      workerList: [],

      customer: {
        loginName: '',
        customerName: '',
        creditCode: '',
        regAddress: '',
        officeAddress: '',
        contactPhone: '',
        status: '',
      },
      CUSTOMER_STATUS: CUSTOMER_STATUS,
      EDIT_TYPE: EDIT_TYPE,
    }
  },

  computed: {},

  mounted: function () {
    let _this = this;
    _this.customer = _this.modifiedCustomer;
    _this.listAllWorker(_this.customer.id);
    _this.getSuperInfo(_this.customer.id);
  },

  methods: {
    listAllWorker(customerId) {
      let _this = this;
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/worker/listAllWorkers/'+ customerId).then((response)=>{
        let resp = response.data;
        if (resp.success) {
          _this.workerList = resp.content;
        } else {
          Toast.warning(resp.message,Swal);
        }
      })
    },

    getSuperInfo(customerId) {
      let _this = this;
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/worker/getSuperInfo/'+ customerId).then((response)=>{
        let resp = response.data;
        if (resp.success) {
          _this.superInfo = resp.content;
          _this.adminWorkerId = _this.superInfo.id;
        } else {
          Toast.warning(resp.message,Swal);
        }
      })
    },

    saveCustomerAdmin() {
      let _this = this;
      // 保存校验
      if (1 !== 1
          || !Validator.require(_this.adminWorkerId, "管理员", Swal)
      ) {
        return;
      }
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/system/rdms/customer/changeAdmin', {
        customerId: _this.customer.id,
        adminId: _this.adminWorkerId,
      }).then((response) => {
        let resp = response.data;
        if (resp.success) {
          Swal.fire({
            title: '提示',
            text: "管理员已修改,请新管理员及时通过注册手机重置密码!",
            icon: 'info',
            showCancelButton: true,
            confirmButtonColor: '#409EFF',
            cancelButtonColor: '#909399',
            confirmButtonText: '确认!',
            cancelButtonText: '取消!'
          }).then((result) => {
            _this.close();
          }).catch(_ => {
          });
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

