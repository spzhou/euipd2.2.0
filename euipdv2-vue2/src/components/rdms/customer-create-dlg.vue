<!--
 * Author: 周朔鹏
 * Date: 2025-08-04
 * Email: 391902958@qq.com
 * Website: https://www.euipd.com
-->

<template>
  <div>
    <div style="padding-right: 100px;">
      <el-alert
          v-if="editType === EDIT_TYPE.CREATE.key"
          type="info"
          :closable="false"
          show-icon
          style="margin-bottom: 16px;"
          title="开户说明"
          description="管理员使用「管理员手机」登录并选择本机构；创建后请立即用「修改管理员密码」设置密码。新机构自动获得全量业务菜单与按钮权限，角色菜单由 JSON 配置控制。">
      </el-alert>
      <el-form label-width="150px" :model="customer" ref="customerForm">
        <el-form-item label="登录名称:" required>
          <div v-if="editType === EDIT_TYPE.CREATE.key">
            <el-input v-model.trim="customer.loginName" maxlength="6" show-word-limit placeholder="2～6 个字符，机构登录名"/>
          </div>
          <div v-else>
            {{ customer.loginName }}
          </div>
        </el-form-item>
        <el-form-item label="团队名称:" required>
          <el-input v-model.trim="customer.customerName" maxlength="50" show-word-limit placeholder="团队/机构名称"/>
        </el-form-item>
        <el-form-item label="管理员姓名:" required>
          <el-input v-model.trim="customer.adminName" maxlength="10" show-word-limit placeholder="机构管理员"/>
        </el-form-item>
        <el-form-item label="管理员手机:" required>
          <div v-if="editType === EDIT_TYPE.CREATE.key">
            <el-input v-model.trim="customer.contactPhone" maxlength="15" placeholder="11 位手机号，用作管理员登录名"/>
          </div>
          <div v-else>
            {{ customer.contactPhone }}
          </div>
        </el-form-item>
        <el-form-item label="用户数量限制:">
          <el-input-number v-model="customer.userNumLimit" :min="1" :max="100000" :controls="true"/>
        </el-form-item>
        <el-form-item label="过期时间:">  <!--过期时间为  NULL 时, 不做时间限制-->
          <el-date-picker
              type="date"
              placeholder="不选则不限制"
              v-model="customer.expirationTime"
              :picker-options="pickerOptions">
          </el-date-picker>
        </el-form-item>
        <el-form-item label="预算模式:">
          <el-select style="width: 150px;" v-model="customer.budgetMode" filterable placeholder="请选择">
            <el-option
                v-for="item in BUDGET_MODE"
                :key="item.key"
                :label="item.value"
                :value="item.key">
            </el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="存储空间(GB):">
          <el-input-number v-model="customer.storageSpace" :min="1" :max="1000000" :controls="true"/>
        </el-form-item>
        <el-form-item label="机构状态:" required>
          <el-select style="width: 150px;" v-model="customer.status" filterable placeholder="请选择">
            <el-option
                v-for="item in CUSTOMER_STATUS"
                :key="item.key"
                :label="item.value"
                :value="item.key">
            </el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="机构类型:" required>
          <el-select style="width: 150px;" v-model="customer.customerType" filterable placeholder="请选择">
            <el-option
                v-for="item in CUSTOMER_TYPE"
                :key="item.key"
                :label="item.value"
                :value="item.key">
            </el-option>
          </el-select>
        </el-form-item>
      </el-form>
    </div>
    <div class="clearfix form-actions">
      <div style="display: flex; justify-content: center;">
        <button type="button" v-on:click="saveCustomer()" class="ca-btn-primary" :disabled="saving">
          {{ saving ? '保存中…' : '保 存' }}
        </button>
        <button type="button" v-on:click="close()" class="ca-btn-primary" :disabled="saving">取 消</button>
      </div>
    </div>
  </div>

</template>


<script>
import Swal from "sweetalert2";

export default {
  components: {
  },
  name: "customer-create-dlg",

  props: {
    editType: {
      default: null,
    },
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
      customer: {
        loginName: '',
        customerName: '',
        creditCode: '',
        regAddress: '',
        officeAddress: '',
        contactPhone: '',
        adminName: '',
        status: CUSTOMER_STATUS.NORMAL.key,
        customerType: CUSTOMER_TYPE.PRODUCT_COMPANY.key,
        budgetMode: BUDGET_MODE.FREE.key,
        userNumLimit: 2000,
        storageSpace: 10000,
        expirationTime: null,
      },
      CUSTOMER_STATUS: CUSTOMER_STATUS,
      CUSTOMER_TYPE: CUSTOMER_TYPE,
      BUDGET_MODE: BUDGET_MODE,
      EDIT_TYPE: EDIT_TYPE,

      pickerOptions: {
        disabledDate(v) {
          return v.getTime() < new Date().getTime() - 86400000;//  - 86400000是否包括当天
        }
      },
      saving: false,
    }
  },

  computed: {
  },

  mounted: function () {
    let _this = this;
    if (_this.editType === EDIT_TYPE.MODIFY.key && _this.modifiedCustomer) {
      _this.customer = Object.assign({}, _this.modifiedCustomer);
      if (!_this.customer.budgetMode) {
        _this.customer.budgetMode = BUDGET_MODE.FREE.key;
      }
      if (_this.customer.userNumLimit == null) {
        _this.customer.userNumLimit = 2000;
      }
      if (_this.customer.storageSpace == null) {
        _this.customer.storageSpace = 10000;
      }
    }
    _this.getLoginUser();
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
    },

    saveCustomer() {
      let _this = this;
      if (_this.saving) {
        return;
      }

      // 保存校验
      if (1 !== 1
          || !Validator.require(_this.customer.loginName, "登录名称", Swal)
          || !Validator.require(_this.customer.customerName, "团队名称", Swal)
          || !Validator.require(_this.customer.adminName, "管理员姓名", Swal)
          || !Validator.require(_this.customer.contactPhone, "管理员手机", Swal)
          || !Validator.require(_this.customer.status, "机构状态", Swal)
          || !Validator.require(_this.customer.customerType, "机构类型", Swal)

          || !Validator.length(_this.customer.loginName, "登录名称", 2, 6, Swal)
          || !Validator.length(_this.customer.customerName, "团队名称", 5, 50, Swal)
          || !Validator.length(_this.customer.adminName, "管理员姓名", 2, 10, Swal)
          || !Validator.isMobile(_this.customer.contactPhone, "管理员手机", Swal)
      ) {
        return;
      }

      if (!_this.customer.budgetMode) {
        _this.customer.budgetMode = BUDGET_MODE.FREE.key;
      }
      if (!_this.customer.userNumLimit || _this.customer.userNumLimit < 1) {
        _this.customer.userNumLimit = 2000;
      }
      if (!_this.customer.storageSpace || _this.customer.storageSpace < 1) {
        _this.customer.storageSpace = 10000;
      }

      let requestUrl = '/system/rdms/customer/save';
      if (_this.editType === EDIT_TYPE.MODIFY.key) {
        requestUrl = '/system/rdms/customer/modify';
      }
      // 新开户默认全量菜单/按钮，机构内勾选授权已无意义；固定不使能
      if (_this.editType === EDIT_TYPE.CREATE.key) {
        _this.customer.enAuthEdit = 0;
      }

      // 平台超管写操作前端预检（无 CUSTOMER 权限时给出明确提示）
      if (typeof Tool !== 'undefined' && Tool.canCallApi) {
        const check = Tool.canCallApi(requestUrl, 'post');
        if (check && check.allowed === false) {
          Toast.warning(check.message || '没有权限创建/修改机构', Swal);
          return;
        }
      }

      _this.saving = true;
      const payload = Object.assign({}, _this.customer);
      _this.$ajax.post(process.env.VUE_APP_SERVER + requestUrl, payload).then((response) => {
        let resp = response.data;
        if (resp && resp.success) {
          Toast.success(_this.editType === EDIT_TYPE.CREATE.key
              ? "机构创建成功，请设置管理员密码后登录"
              : "保存成功", Swal);
          _this.close();
        } else {
          Toast.warning((resp && resp.message) || '保存失败', Swal);
        }
      }).catch((err) => {
        const msg = (err && err.response && err.response.data && err.response.data.message)
            || (err && err.message)
            || '保存失败，请稍后重试';
        Toast.warning(msg, Swal);
      }).finally(() => {
        _this.saving = false;
      });
    },

    close() {
      let _this = this;
      if (typeof _this.closeDlg === 'function') {
        _this.closeDlg();
      }
    },
  }
}
</script>
