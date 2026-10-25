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
            <el-form-item label="退料单号:">
              <el-col :span="23">
                <el-input type="text" style="font-size: 14px;" :value="dialogInfo.code" readonly></el-input>
              </el-col>
            </el-form-item>
          </el-col>
          <el-col :span="7">
            <el-form-item label="原领料单号:">
              <el-col :span="23">
                <el-input type="text" style="font-size: 14px;" :value="dialogInfo.originCode" readonly></el-input>
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
            <el-table :data="receiveRows" stripe style="width: 100%">
              <el-table-column label="序号" align="center" width="80">
                <template slot-scope="scope">
                  {{ scope.$index + 1 }}
                </template>
              </el-table-column>
              <el-table-column prop="bomCode" label="物料编码"></el-table-column>
              <el-table-column prop="name" align="center" label="物料名称"></el-table-column>
              <el-table-column prop="unit" width="80" align="center" label="计量单位"></el-table-column>
              <el-table-column prop="amount" width="100" align="center" label="退料数量"></el-table-column>
              <el-table-column prop="batchCode" width="140" align="center" label="批次号/序列号"></el-table-column>
              <el-table-column width="160" align="center" label="入库仓库">
                <template slot-scope="scope">
                  {{ scope.row.warehouseLabel }}
                </template>
              </el-table-column>
            </el-table>
          </div>
        </el-row>
        <el-row>
          <el-col :span="22">
            <el-form-item label="退料事由:">
              {{ dialogInfo.reason }}
            </el-form-item>
          </el-col>
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
            <el-form-item label="收料结论:" required>
              <el-radio-group v-model="receiveResult">
                <el-radio label="RECEIVED">实物已收，入库</el-radio>
                <el-radio label="REJECT">驳回退料</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="22">
            <el-form-item label="收料说明:" required>
              <el-input type="textarea" rows="4" maxlength="150" show-word-limit
                        v-model="receiveDescription"></el-input>
            </el-form-item>
          </el-col>
        </el-row>
        <div class="hr hr-8 dotted"></div>
        <el-row>
          <el-col :offset="1" :span="6">
            <el-form-item label="退料人:">
              <el-col :span="24">
                <el-input type="text" style="font-size: 14px;" :value="dialogInfo.writerName" readonly></el-input>
              </el-col>
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="收料人:">
              <el-col :span="24">
                <el-input type="text" style="font-size: 14px;" :value="loginUser.trueName" readonly></el-input>
              </el-col>
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <div class="clearfix form-actions">
        <div style="display: flex; justify-content: center;">
          <button type="button" v-on:click="submitReceive" class="ca-btn-primary">确 定</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import Swal from "sweetalert2";
import {optionKV} from "@/filter/filter";

export default {
  name: "material-receive-dlg",

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
      warehouseList: [],
      receiveResult: 'RECEIVED',
      receiveDescription: '',
      loginUser: {},
      submitting: false,
    };
  },

  computed: {
    usageModeName() {
      return optionKV(MATERIAL_USAGE_MODE, this.dialogInfo.usageMode);
    },
    receiveRows() {
      let _this = this;
      return (_this.materialList || []).map(function (item) {
        let code = item.targetWarehouseCode || item.warehouseCode || 'LOCAL';
        let warehouse = (_this.warehouseList || []).find(function (w) { return w.code === code; });
        return Object.assign({}, item, { warehouseLabel: warehouse ? warehouse.name + '（' + code + '）' : code });
      });
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
          _this.getWarehouseList(_this.dialogInfo.customerId);
        } else {
          Toast.warning(resp.message, Swal);
        }
      });
    },

    getWarehouseList(customerId) {
      let _this = this;
      if (!customerId) {
        return;
      }
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/material/active-warehouses/' + customerId).then((response) => {
        let resp = response.data;
        if (resp.success) {
          _this.warehouseList = resp.content || [];
        }
      });
    },

    submitReceive() {
      let _this = this;
      let text = (_this.receiveDescription || '').trim();
      if (text.length < 2 || text.length > 150) {
        Toast.warning("收料说明须为 2 到 150 字", Swal);
        return;
      }
      if (_this.submitting) {
        return;
      }
      _this.submitting = true;
      let payload = {
        code: _this.dialogInfo.code,
        issueResult: _this.receiveResult,
        issueDescription: text,
        loginUserId: _this.loginUser.customerUserId || _this.loginUser.id,
      };
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/material/receiveReturnApplication', payload).then((response) => {
        let resp = response.data;
        if (resp.success) {
          _this.$bus.publish({ areas: ['material', 'character', 'project', 'product', 'develop', 'quality', 'badge'], reason: 'material-receive' });
          Toast.success(_this.receiveResult === 'RECEIVED' ? "已收料入库" : "已驳回退料", Swal);
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
