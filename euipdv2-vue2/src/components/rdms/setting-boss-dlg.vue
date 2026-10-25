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
          <el-form-item label="最终管理人:" required>
            <el-select v-model="bossUserId" filterable placeholder="请选择">
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
        <button type="button" v-on:click="saveBoss()" class="ca-btn-primary">保 存</button>
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
  name: "setting-boss-dlg",

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
      bossInfo: {},
      bossUserId: '',
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
    _this.getBossInfo(_this.customer.id);
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

    getBossInfo(customerId) {
      let _this = this;
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/worker/getBossInfo/'+ customerId).then((response)=>{
        let resp = response.data;
        if (resp.success) {
          _this.bossInfo = resp.content;
          _this.bossUserId = _this.bossInfo.bossId;
        } else {
          Toast.warning(resp.message,Swal);
        }
      })
    },

    saveBoss() {
      let _this = this;
      // 保存校验
      if (1 !== 1
          || !Validator.require(_this.bossUserId, "最终管理人", Swal)
      ) {
        return;
      }
      const changed = !!(_this.bossInfo && _this.bossInfo.bossId) && _this.bossInfo.bossId !== _this.bossUserId;
      const confirm = changed
        ? Swal.fire({
          title: '更换最终管理人？',
          text: '原负责人为管理员时，其资质名单、未结束项目上的岗位与固定部门负责人一并转给新最终管理人，原负责人只保留管理员菜单。新旧负责人须重新登录。',
          icon: 'warning',
          showCancelButton: true,
          confirmButtonText: '确定更换',
          cancelButtonText: '取消',
        })
        : Promise.resolve({ isConfirmed: true });
      confirm.then((result) => {
        if (!result.isConfirmed) {
          return;
        }
        let formData = new window.FormData();
        formData.append('customerId', _this.customer.id);
        formData.append('bossId', _this.bossUserId);
        _this.$ajax.post(process.env.VUE_APP_SERVER + '/system/rdms/customer/saveBoss', formData).then((response) => {
          let resp = response.data;
          if (resp.success) {
            const transferred = (resp.content && resp.content.transferredPositions) || [];
            if (transferred.length) {
              Swal.fire({
                title: '保存成功',
                text: '原负责人的以下名单与岗位已转给新最终管理人：' + transferred.join('、'),
                icon: 'info',
              });
            } else {
              Toast.success("保存成功", Swal);
            }
            _this.close();
          } else {
            Toast.warning(resp.message, Swal);
          }
        })
      });
    },

    close(){
      let _this = this;
      _this.closeDlg();
    },
  }
}
</script>

