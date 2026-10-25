<!--
 * Author: 周朔鹏
 * Date: 2025-03-10
 * Email: 391902958@qq.com
 * Website: https://www.euipd.com
-->

<template>
  <div class="a4-paper-big-simple" v-loading="pageLoading">
    <div>
      <div class="qual-section">
        <div>
          <h1 class="qual-title">编辑 IPMT/ITMT 成员</h1>
        </div>
        <div class="hr hr-10 hr-dotted"></div>
        <div>
          <el-transfer
              filterable
              filter-placeholder="请输入姓名"
              v-model="value"
              :data="data_ipmt"
              :titles="titles">
          </el-transfer>
        </div>
        <div class="qual-actions">
          <button v-on:click="saveIpmt()" class="ca-btn-primary" type="button" :disabled="savingIpmt">
            {{ savingIpmt ? '保存中...' : '保存' }}
          </button>
        </div>
      </div>
      <div class="hr hr-18 hr-dotted hr-double"></div>
      <div class="qual-section">
        <div>
          <h1 class="qual-title">编辑 产品经理 成员</h1>
        </div>
        <div class="hr hr-10 hr-dotted"></div>
        <div>
          <el-transfer
              filterable
              filter-placeholder="请输入姓名"
              v-model="value_pdm"
              :data="data_pdm"
              :titles="titles_pdm"
          >
          </el-transfer>
        </div>
        <div class="qual-actions">
          <button v-on:click="saveProductManager()" class="ca-btn-primary" type="button" :disabled="savingPdm">
            {{ savingPdm ? '保存中...' : '保存' }}
          </button>
        </div>
      </div>
      <div class="hr hr-18 hr-dotted hr-double"></div>
      <div class="qual-section">
        <div>
          <h1 class="qual-title">编辑 项目经理 成员</h1>
        </div>
        <div class="hr hr-10 hr-dotted"></div>
        <div>
          <el-transfer
              filterable
              filter-placeholder="请输入姓名"
              v-model="value_pjm"
              :data="data_pjm"
              :titles="titles_pjm"
          ></el-transfer>
        </div>
        <div class="qual-actions">
          <button v-on:click="saveProjectManager()" class="ca-btn-primary" type="button" :disabled="savingPjm">
            {{ savingPjm ? '保存中...' : '保存' }}
          </button>
        </div>
      </div>
      <div class="hr hr-18 hr-dotted hr-double"></div>
      <div class="qual-section">
        <div>
          <h1 class="qual-title">编辑 系统工程师 成员</h1>
        </div>
        <div class="hr hr-10 hr-dotted"></div>
        <div>
          <el-transfer
              filterable
              filter-placeholder="请输入姓名"
              v-model="value_sys"
              :data="data_sys"
              :titles="titles_sys"
          ></el-transfer>
        </div>
        <div class="qual-actions">
          <button v-on:click="saveSystemManager()" class="ca-btn-primary" type="button" :disabled="savingSys">
            {{ savingSys ? '保存中...' : '保存' }}
          </button>
        </div>
      </div>
      <div class="hr hr-18 hr-dotted hr-double"></div>
    </div>
  </div>

</template>

<script>
import Swal from "sweetalert2";

export default {
  name: "edit-project-team",

  data: function() {
    return {
      loginUser: {},
      loginCustomer: {},

      data_ipmt: [],
      data_pdm: [],
      data_pjm: [],
      data_sys: [],

      value: [],
      titles: ['可选人员','IPMT成员'],

      value_pdm: [],
      titles_pdm: ['可选人员','产品经理'],

      value_pjm: [],
      titles_pjm: ['可选人员','项目经理'],

      value_sys: [],
      titles_sys: ['可选人员','系统工程师'],

      workers: [],
      customerId: '',
      pageLoading: false,
      savingIpmt: false,
      savingPdm: false,
      savingPjm: false,
      savingSys: false,
    };
  },

  mounted: function() {
    let _this = this;
    _this.getLoginUser();
    _this.listAllWorker();
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

    // 最终管理人是各资质名单的默认成员，固定在右侧、不可移出，保存时不提交
    buildTransferData(disableField) {
      let _this = this;
      return (_this.workers || []).map((worker) => ({
        label: worker.isBoss ? worker.trueName + '（最终管理人，默认）' : worker.trueName,
        key: worker.id,
        disabled: !!worker.isBoss || !!worker[disableField],
      }));
    },

    withoutBoss(ids) {
      const bossIds = (this.workers || []).filter((w) => w.isBoss).map((w) => w.id);
      return (ids || []).filter((id) => bossIds.indexOf(id) < 0);
    },

    listAllWorker() {
      let _this = this;
      _this.pageLoading = true;
      _this.data_ipmt = [];
      _this.data_pdm = [];
      _this.data_pjm = [];
      _this.data_sys = [];
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/worker/listAllWorkers/'+ _this.loginCustomer.id).then((response)=>{
        let resp = response.data;
        if (resp.success) {
          _this.workers = resp.content || [];
          _this.data_ipmt = _this.buildTransferData('ipmtTransferDisable');
          _this.data_pdm = _this.buildTransferData('pdmTransferDisable');
          _this.data_pjm = _this.buildTransferData('pjmTransferDisable');
          // 后端暂无 smTransferDisable，复用 pjm 禁用规则
          _this.data_sys = _this.buildTransferData('pjmTransferDisable');
          return Promise.all([
            _this.listByCustomerId(),
            _this.listByCustomerId_pdm(),
            _this.listByCustomerId_pjm(),
            _this.listByCustomerId_sys(),
          ]);
        } else {
          Toast.warning(resp.message,Swal);
        }
      }).catch((e) => {
        console.error(e);
        Toast.error('加载员工列表失败', Swal);
      }).finally(() => {
        _this.pageLoading = false;
      })
    },

    listByCustomerId() {
      let _this = this;
      return _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/ipmt/listByCustomerId/'+ _this.loginCustomer.id).then((response)=>{
        let resp = response.data;
        if (resp.success) {
          _this.value = resp.content || [];
        } else {
          Toast.warning(resp.message,Swal);
        }
      })
    },

    saveIpmt() {
      let _this = this;
      if (_this.savingIpmt) {
        return;
      }
      _this.savingIpmt = true;
      _this.customerId = _this.loginCustomer.id;
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/ipmt/save', {
        customerId: _this.loginCustomer.id,
        ipmtIds: _this.withoutBoss(_this.value),
      }).then((response)=>{
        let resp = response.data;
        if (resp.success) {
          Toast.success("保存成功", Swal);
          _this.listByCustomerId();
        } else {
          Toast.warning(resp.message, Swal);
        }
      }).catch((e) => {
        console.error(e);
        Toast.error('保存失败', Swal);
      }).finally(() => {
        _this.savingIpmt = false;
      })
    },

    listByCustomerId_pdm() {
      let _this = this;
      return _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/product-manager/listByCustomerId/'+ _this.loginCustomer.id).then((response)=>{
        let resp = response.data;
        if (resp.success) {
          _this.value_pdm = resp.content || [];
        } else {
          Toast.warning(resp.message,Swal);
        }
      })
    },

    saveProductManager() {
      let _this = this;
      if (_this.savingPdm) {
        return;
      }
      _this.savingPdm = true;
      _this.customerId = _this.loginCustomer.id;
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/product-manager/save', {
        customerId: _this.loginCustomer.id,
        productManagerIds: _this.withoutBoss(_this.value_pdm),
      }).then((response)=>{
        let resp = response.data;
        if (resp.success) {
          Toast.success("保存成功", Swal);
          _this.listByCustomerId_pdm();
        } else {
          Toast.warning(resp.message, Swal);
        }
      }).catch((e) => {
        console.error(e);
        Toast.error('保存失败', Swal);
      }).finally(() => {
        _this.savingPdm = false;
      })
    },

    listByCustomerId_pjm() {
      let _this = this;
      return _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/project-manager/listByCustomerId/'+ _this.loginCustomer.id).then((response)=>{
        let resp = response.data;
        if (resp.success) {
          _this.value_pjm = resp.content || [];
        } else {
          Toast.warning(resp.message,Swal);
        }
      })
    },

    saveProjectManager() {
      let _this = this;
      if (_this.savingPjm) {
        return;
      }
      _this.savingPjm = true;
      _this.customerId = _this.loginCustomer.id;
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/project-manager/save', {
        customerId: _this.loginCustomer.id,
        projectManagerIds: _this.withoutBoss(_this.value_pjm),
      }).then((response)=>{
        let resp = response.data;
        if (resp.success) {
          Toast.success("保存成功", Swal);
          _this.listByCustomerId_pjm();
        } else {
          Toast.warning(resp.message, Swal);
        }
      }).catch((e) => {
        console.error(e);
        Toast.error('保存失败', Swal);
      }).finally(() => {
        _this.savingPjm = false;
      })
    },

    listByCustomerId_sys() {
      let _this = this;
      return _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/system-manager/listByCustomerId/'+ _this.loginCustomer.id).then((response)=>{
        let resp = response.data;
        if (resp.success) {
          _this.value_sys = resp.content || [];
        } else {
          Toast.warning(resp.message,Swal);
        }
      })
    },

    saveSystemManager() {
      let _this = this;
      if (_this.savingSys) {
        return;
      }
      _this.savingSys = true;
      _this.customerId = _this.loginCustomer.id;
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/system-manager/save', {
        customerId: _this.loginCustomer.id,
        systemManagerIds: _this.withoutBoss(_this.value_sys),
      }).then((response)=>{
        let resp = response.data;
        if (resp.success) {
          Toast.success("保存成功", Swal);
          _this.listByCustomerId_sys();
        } else {
          Toast.warning(resp.message, Swal);
        }
      }).catch((e) => {
        console.error(e);
        Toast.error('保存失败', Swal);
      }).finally(() => {
        _this.savingSys = false;
      })
    },

  },

};
</script>

<style lang="scss">
@import './../../../public/static/scss/caipd.scss';

.qual-section {
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  margin-top: 40px;
}

.qual-title {
  font-size: 20px;
}

.qual-actions {
  margin-top: 16px;
}
</style>
