<!--
 * Author: 周朔鹏
 * Date: 2024-12-01
 * Email: 391902958@qq.com
 * Website: https://www.euipd.com
-->

<template>
  <div class="turn-over">
    <div class="turn-over-row">
      <span class="turn-over-label">接收人</span>
      <el-select v-model="newWorkerId" filterable placeholder="选择工作接收人" style="width: 260px;"
                 :disabled="executing" @change="onReceiverChange">
        <el-option v-for="o in receivers" :key="o.id" :value="o.id" :label="o.trueName"></el-option>
      </el-select>
      <el-button type="primary" plain :disabled="!newWorkerId || loading || executing" @click="preview">预览</el-button>
    </div>

    <div v-if="result" class="turn-over-block">
      <div class="turn-over-title">
        {{ executed ? '移交结果' : '待移交事项' }}：{{ result.fromName }} → {{ result.toName }}，合计 {{ result.total }} 项
      </div>
      <div v-if="result.positions && result.positions.length" class="turn-over-positions">
        担任的岗位：{{ result.positions.join('、') }}
      </div>
      <el-alert v-if="!executed && result.total === 0" type="info" :closable="false" show-icon
                title="没有需要移交的事项"></el-alert>
      <el-table v-else :data="shownItems" size="mini" border style="width: 100%;">
        <el-table-column prop="name" label="事项类别"></el-table-column>
        <el-table-column prop="count" label="数量" width="120" align="center"></el-table-column>
      </el-table>
      <div class="turn-over-note">
        已结束的事项、过程记录与执行流水保留原记录；移交人的在职状态不变。
      </div>
    </div>

    <div class="turn-over-actions">
      <el-button v-if="!executed" type="primary" :disabled="!canExecute" :loading="executing" @click="execute">确认移交</el-button>
      <el-button v-else type="primary" @click="finish">完成</el-button>
    </div>

    <div class="turn-over-block">
      <div class="turn-over-title">历次移交记录</div>
      <el-table :data="logs" size="mini" border style="width: 100%;" empty-text="暂无记录">
        <el-table-column prop="createTime" label="时间" width="170"></el-table-column>
        <el-table-column prop="fromName" label="移交人"></el-table-column>
        <el-table-column prop="toName" label="接收人"></el-table-column>
        <el-table-column prop="operatorName" label="操作人"></el-table-column>
        <el-table-column prop="total" label="事项合计" width="100" align="center"></el-table-column>
      </el-table>
    </div>
  </div>
</template>

<script>
import Swal from "sweetalert2";

export default {
  name: "turn-over-setting",

  props: {
    workerId: {
      default: null,
    },
    closeFunction: {
      type: Function,
      default: null
    },
  },

  data: function() {
    return {
      loginUser: {},
      loginCustomer: {},

      workers: [],
      newWorkerId: '',
      result: null,
      loading: false,
      executing: false,
      executed: false,
      logs: [],
    };
  },

  computed: {
    receivers() {
      return this.workers.filter(o => o.id !== this.workerId);
    },
    shownItems() {
      if (!this.result || !this.result.items) {
        return [];
      }
      return this.result.items.filter(o => o.count > 0);
    },
    canExecute() {
      return !!this.result && !this.executing && this.result.toId === this.newWorkerId && this.result.total > 0;
    },
  },

  mounted: function() {
    let _this = this;
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

      _this.listAllWorker();
      _this.listLogs();
    },

    listAllWorker() {
      let _this = this;
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/worker/listAllWorkers/'+ _this.loginCustomer.id).then((response)=>{
        let resp = response.data;
        if (resp.success) {
          _this.workers = resp.content || [];
        } else {
          Toast.warning(resp.message,Swal);
        }
      })
    },

    listLogs() {
      let _this = this;
      let url = process.env.VUE_APP_SERVER + '/customer/worker/turnOver/logs/' + encodeURIComponent(_this.workerId)
          + '?customerId=' + encodeURIComponent(_this.loginCustomer.id);
      _this.$ajax.get(url).then((response)=>{
        let resp = response.data;
        if (resp.success) {
          _this.logs = resp.content || [];
        }
      })
    },

    onReceiverChange() {
      this.result = null;
    },

    preview() {
      let _this = this;
      _this.loading = true;
      let url = process.env.VUE_APP_SERVER + '/customer/worker/turnOver/preview'
          + '?customerId=' + encodeURIComponent(_this.loginCustomer.id)
          + '&workerId=' + encodeURIComponent(_this.workerId)
          + '&newWorkerId=' + encodeURIComponent(_this.newWorkerId);
      _this.$ajax.get(url).then((response)=>{
        _this.loading = false;
        let resp = response.data;
        if (resp.success) {
          _this.result = resp.content;
        } else {
          Toast.warning(resp.message, Swal);
        }
      }).catch(() => {
        _this.loading = false;
      })
    },

    execute() {
      let _this = this;
      let text = '将 ' + _this.result.fromName + ' 名下的 ' + _this.result.total + ' 项事项移交给 '
          + _this.result.toName + '，移交后不能自动撤回。';
      _this.$confirm(text, '确认移交', { type: 'warning' }).then(function () {
        _this.executing = true;
        let formData = new window.FormData();
        formData.append('customerId', _this.loginCustomer.id);
        formData.append('workerId', _this.workerId);
        formData.append('newWorkerId', _this.newWorkerId);
        _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/worker/turnOver', formData).then((response)=>{
          _this.executing = false;
          let resp = response.data;
          if (resp.success) {
            _this.result = resp.content;
            _this.executed = true;
            _this.listLogs();
            Toast.success("移交完成", Swal);
          } else {
            Toast.warning(resp.message, Swal);
          }
        }).catch(() => {
          _this.executing = false;
        })
      }).catch(function () {});
    },

    finish() {
      if (this.closeFunction) {
        this.closeFunction();
      }
    },
  },

};
</script>

<style lang="scss">
@import './../../../public/static/scss/caipd.scss';

.turn-over {
  padding: 0 10px;
}
.turn-over-row {
  display: flex;
  align-items: center;
  gap: 12px;
}
.turn-over-label {
  color: #606266;
}
.turn-over-block {
  margin-top: 16px;
}
.turn-over-title {
  font-weight: 600;
  margin-bottom: 8px;
}
.turn-over-positions {
  margin-bottom: 8px;
  color: #606266;
}
.turn-over-note {
  margin-top: 6px;
  font-size: 12px;
  color: #909399;
}
.turn-over-actions {
  display: flex;
  justify-content: center;
  margin-top: 16px;
}
</style>
