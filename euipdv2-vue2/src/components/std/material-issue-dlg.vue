<!--
 * Author: 周朔鹏
 * Date: 2026-10-09
 * Email: 391902958@qq.com
 * Website: https://www.euipd.com
-->

<template>
  <div>
    <div class="dialog-container">
      <el-form
          label-width="130px"
          :show-message=false
          @submit.native.prevent
          class="dialog-width">
        <el-row>
          <el-col :span="7">
            <el-form-item label="申请单号:">
              <el-col :span="23">
                <el-input type="text" style="font-size: 14px;" :value="dialogInfo.code" readonly></el-input>
              </el-col>
            </el-form-item>
          </el-col>
          <el-col :span="7">
            <el-form-item label="申请标题:">
              <el-col :span="23">
                <el-input type="text" style="font-size: 14px;" :value="dialogInfo.name" readonly></el-input>
              </el-col>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="使用方式:">
              <el-col :span="23">
                <el-input type="text" style="font-size: 14px;" :value="usageModeName" readonly></el-input>
              </el-col>
            </el-form-item>
          </el-col>
        </el-row>
        <div class="hr hr-6 dotted"></div>
        <el-row>
          <div style="margin: 0 80px 20px 80px;">
            <el-table :data="issueRows" stripe style="width: 100%">
              <el-table-column label="序号" align="center" width="80">
                <template slot-scope="scope">
                  {{ scope.$index + 1 }}
                </template>
              </el-table-column>
              <el-table-column prop="bomCode" label="物料编码"></el-table-column>
              <el-table-column prop="name" align="center" label="物料名称"></el-table-column>
              <el-table-column prop="unit" width="80" align="center" label="计量单位"></el-table-column>
              <el-table-column prop="amount" width="100" align="center" label="批准数量"></el-table-column>
              <el-table-column prop="warehouseLabel" width="150" align="center" label="出库仓库"></el-table-column>
              <el-table-column width="100" align="center" label="库存余量">
                <template slot-scope="scope">
                  <span :class="{ 'red-color': scope.row.short }">{{ scope.row.onHand }}</span>
                </template>
              </el-table-column>
            </el-table>
            <div v-if="anyShort" class="red-color" style="margin-top: 8px;">
              <i class="el-icon-warning"></i> 有物料库存不足，需先入库或退回申请人。
            </div>
          </div>
        </el-row>
        <el-row>
          <el-col :span="22">
            <el-form-item label="审批意见:">
              {{ dialogInfo.approveDescription }}
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="22">
            <el-form-item label="发料结论:" required>
              <el-radio-group v-model="issueResult">
                <el-radio label="ISSUED">发料</el-radio>
                <el-radio label="REJECT">退回申请人</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="22">
            <el-form-item label="发料说明:" required>
              <el-input type="textarea" rows="4" maxlength="150" show-word-limit
                        v-model="issueDescription"></el-input>
            </el-form-item>
          </el-col>
        </el-row>
        <div class="hr hr-8 dotted"></div>
        <el-row>
          <el-col :offset="1" :span="6">
            <el-form-item label="申请人:">
              <el-col :span="24">
                <el-input type="text" style="font-size: 14px;" :value="dialogInfo.writerName" readonly></el-input>
              </el-col>
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="发料人:">
              <el-col :span="24">
                <el-input type="text" style="font-size: 14px;" :value="loginUser.trueName" readonly></el-input>
              </el-col>
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <div class="clearfix form-actions">
        <div style="display: flex; justify-content: center;">
          <button type="button" v-on:click="submitIssue" class="ca-btn-primary">确 定</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import Swal from "sweetalert2";
import {optionKV} from "@/filter/filter";

export default {
  name: "material-issue-dlg",

  props: {
    applicationCode: {
      default: null
    },
    closeDlg: {
      type: Function,
      default: null
    },
  },

  data() {
    return {
      dialogInfo: {},
      materialList: [],
      stockList: [],
      issueResult: 'ISSUED',
      issueDescription: '',
      loginUser: {},
      submitting: false,
    };
  },

  computed: {
    usageModeName() {
      return optionKV(MATERIAL_USAGE_MODE, this.dialogInfo.usageMode);
    },
    issueRows() {
      let _this = this;
      let warehouseOf = function (item) {
        return item.warehouseCode || _this.dialogInfo.warehouseCode || 'LOCAL';
      };
      let requested = {};
      (_this.materialList || []).forEach(function (item) {
        let key = warehouseOf(item) + '|' + item.bomCode;
        requested[key] = (requested[key] || 0) + (item.amount || 0);
      });
      return (_this.materialList || []).map(function (item) {
        let warehouse = warehouseOf(item);
        let stock = (_this.stockList || []).find(function (s) {
          return s.bomCode === item.bomCode && (s.warehouseCode || 'LOCAL') === warehouse;
        });
        let onHand = stock && stock.quantity != null ? stock.quantity : 0;
        return Object.assign({}, item, {
          warehouseLabel: stock && stock.warehouseName ? stock.warehouseName + '（' + warehouse + '）' : warehouse,
          onHand: onHand,
          short: onHand < requested[warehouse + '|' + item.bomCode],
        });
      });
    },
    anyShort() {
      return this.issueRows.some(function (row) { return row.short; });
    },
  },

  mounted: function () {
    let _this = this;
    if (Tool.getLoginUser().userGroup === LOGIN_USER_GROUP.CUSTOMER.key) {
      _this.loginUser = Tool.getLoginUser().adminUser;
    } else {
      _this.loginUser = Tool.getLoginUser();
    }
    if (Tool.isNotEmpty(_this.applicationCode)) {
      _this.getMaterialApplicationByCode(_this.applicationCode);
    }
  },

  methods: {
    getMaterialApplicationByCode(code) {
      let _this = this;
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/material/getMaterialApplicationByCode/' + code).then((response) => {
        let resp = response.data;
        if (resp.success) {
          _this.dialogInfo = resp.content || {};
          _this.materialList = _this.dialogInfo.materialList || [];
          _this.getStockList(_this.dialogInfo.customerId);
        } else {
          Toast.warning(resp.message, Swal);
        }
      });
    },

    getStockList(customerId) {
      let _this = this;
      if (!customerId) {
        return;
      }
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/material/stock/list/' + customerId).then((response) => {
        let resp = response.data;
        if (resp.success) {
          _this.stockList = resp.content || [];
        }
      });
    },

    submitIssue() {
      let _this = this;
      let text = (_this.issueDescription || '').trim();
      if (text.length < 2 || text.length > 150) {
        Toast.warning("发料说明须为 2 到 150 字", Swal);
        return;
      }
      if (_this.issueResult === 'ISSUED' && _this.anyShort) {
        Toast.warning("有物料库存不足，不能发料", Swal);
        return;
      }
      if (_this.submitting) {
        return;
      }
      _this.submitting = true;
      let payload = {
        code: _this.dialogInfo.code,
        issueResult: _this.issueResult,
        issueDescription: text,
        loginUserId: _this.loginUser.customerUserId || _this.loginUser.id,
      };
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/material/issueMaterialApplication', payload).then((response) => {
        let resp = response.data;
        if (resp.success) {
          _this.$bus.publish({ areas: ['material', 'character', 'project', 'product', 'develop', 'quality', 'badge'], reason: 'material-issue' });
          Toast.success(_this.issueResult === 'ISSUED' ? "已发料" : "已退回申请人", Swal);
          if (_this.closeDlg) {
            _this.closeDlg();
          }
        } else {
          Toast.warning(resp.message, Swal);
        }
      }).finally(() => {
        _this.submitting = false;
      });
    },
  }
}
</script>

<style scoped>
.red-color {
  color: red;
}
</style>
