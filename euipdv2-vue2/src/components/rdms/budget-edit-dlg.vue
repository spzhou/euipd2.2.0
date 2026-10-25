<!--
 * Author: 周朔鹏
 * Date: 2025-02-13
 * Email: 391902958@qq.com
 * Website: https://www.euipd.com
-->

<template>
  <div class="dialog-container" v-loading="actionBusy">
    <div style="margin: 0 20px 20px;">
      <div
          v-if="showParentAvailable"
          style="margin: 0 0 12px; color: #909399; font-size: 13px; line-height: 1.5;">
        父级剩余预算合计：{{ fixedParentRemaining }} 万元。批准合计不得小于特性开发基本预算需求，且不得超过父级剩余；各科目国拨/自筹分别不得超过父级可用。
      </div>
      <el-table
          :data="tableData"
          :summary-method="getSummaries"
          show-summary
          style="width: 100%">
        <el-table-column
            prop="num"
            align="center"
            label="编号"
            min-width="60">
        </el-table-column>
        <el-table-column
            prop="itemType"
            align="center"
            label="科目"
            min-width="120">
        </el-table-column>
        <el-table-column
            v-if="showParentAvailable"
            align="center"
            label="父级可用预算">
          <el-table-column
              prop="parentAvailableBt"
              align="center"
              label="国拨资金( 万元 )"
              min-width="110">
            <template slot-scope="scope">
              {{ formatWan(scope.row.parentAvailableBt) }}
            </template>
          </el-table-column>
          <el-table-column
              prop="parentAvailableZc"
              align="center"
              label="自筹资金( 万元 )"
              min-width="110">
            <template slot-scope="scope">
              {{ formatWan(scope.row.parentAvailableZc) }}
            </template>
          </el-table-column>
        </el-table-column>
        <el-table-column
            prop="devBudget"
            align="center"
            label="特性开发基本预算需求( 万元 )"
            min-width="100">
        </el-table-column>
        <el-table-column
            align="center"
            label="预算编制">
          <el-table-column
              prop="budgetApprovedBt"
              align="center"
              label="国拨资金( 万元 )"
              width="180">
            <template slot-scope="scope">
              <el-input
                  type="number"
                  :min="0"
                  :max="inputMaxOf(scope.row, 'budgetApprovedBt')"
                  :disabled="actionBusy"
                  v-model="scope.row.budgetApprovedBt"
                  @input="clampRowAmount(scope.row, 'budgetApprovedBt')"
                  @blur="normalizeRowAmount(scope.row, 'budgetApprovedBt')">
              </el-input>
            </template>
          </el-table-column>
          <el-table-column
              prop="budgetApprovedZc"
              align="center"
              width="180"
              label="自筹资金( 万元 )">
            <template slot-scope="scope">
              <el-input
                  type="number"
                  :min="0"
                  :max="inputMaxOf(scope.row, 'budgetApprovedZc')"
                  :disabled="actionBusy"
                  v-model="scope.row.budgetApprovedZc"
                  @input="clampRowAmount(scope.row, 'budgetApprovedZc')"
                  @blur="normalizeRowAmount(scope.row, 'budgetApprovedZc')">
              </el-input>
            </template>
          </el-table-column>
        </el-table-column>
      </el-table>
    </div>
    <div class="clearfix form-actions">
      <div style="display: flex; justify-content: center;">
        <div style="margin-right: 10px;">
          <div v-if="projectType===PROJECT_TYPE.PROJECT.key">
            <button
                v-on:click="saveBudget()"
                class="ca-btn-primary"
                type="button"
                :disabled="actionBusy">
              {{ actionBusy ? '保存中...' : '保存' }}
            </button>
          </div>
          <div v-if="projectType===PROJECT_TYPE.SUB_PROJECT.key">
            <button
                v-on:click="saveBudget()"
                class="ca-btn-primary"
                type="button"
                :disabled="actionBusy">
              {{ actionBusy ? '处理中...' : '确定' }}
            </button>
          </div>
        </div>
        <button
            type="button"
            v-on:click="close()"
            class="ca-btn-primary"
            :disabled="actionBusy">
          关闭
        </button>
      </div>
    </div>
  </div>
</template>

<script>
import Swal from "sweetalert2";
import {fixed4fractional} from "@/filter/filter";

export default {
  components: {
  },
  name: "budget-edit-dlg",

  data: function() {
    return {
      loginUser: {},
      loginCustomer: {},
      actionBusy: false,

      character: {},
      PROJECT_TYPE: PROJECT_TYPE,

      tableData: [],
      budgetData: {},
    }
  },

  props: {
    projectId:{
      default: null,
    },
    projectType:{
      default: null,
    },
    devBudget: {
      type: Object,
      default: function () {
        return {
          equipment: 0,
          test: 0,
          material: 0,
          power: 0,
          conference: 0,
          business: 0,
          cooperation: 0,
          property: 0,
          labor: 0,
          staff: 0,
          consulting: 0,
          management: 0,
          other: 0,
          performance: 0,
          infrastructure: 0,
        };
      },
    },
    closeFunction: {
      type: Function,
      default: null
    },
    /** 父级剩余预算合计（万元），子项目拆分时用于上限校验 */
    parentRemainingWan: {
      type: Number,
      default: null,
    },
    /** 父级预算明细（万元），含各科目 FeeBt/FeeZc */
    parentBudgetDetailWan: {
      type: Object,
      default: null,
    },
  },

  computed: {
    showParentAvailable() {
      return this.projectType === PROJECT_TYPE.SUB_PROJECT.key
          && this.parentBudgetDetailWan != null;
    },
    fixedParentRemaining() {
      const n = Number(this.parentRemainingWan);
      return Number.isFinite(n) ? fixed4fractional(n) : '-';
    },
  },

  mounted: function() {
    let _this = this;
    _this.getLoginUser();
    _this.initTableData();
    if(_this.projectType === PROJECT_TYPE.PROJECT.key){
      _this.getBudget();
    } else if (_this.projectType === PROJECT_TYPE.SUB_PROJECT.key) {
      _this.suggestApprovedFromDevBudget();
    }
  },


  methods: {
    feeFieldByNum() {
      return {
        [BUDGET_CLASSIFY_STD.EQUIPMENT.key]: 'equipment',
        [BUDGET_CLASSIFY_STD.MATERIAL.key]: 'material',
        [BUDGET_CLASSIFY_STD.TEST.key]: 'test',
        [BUDGET_CLASSIFY_STD.POWER.key]: 'power',
        [BUDGET_CLASSIFY_STD.CONFERENCE.key]: 'conference',
        [BUDGET_CLASSIFY_STD.BUSINESS.key]: 'business',
        [BUDGET_CLASSIFY_STD.COOPERATION.key]: 'cooperation',
        [BUDGET_CLASSIFY_STD.PROPERTY.key]: 'property',
        [BUDGET_CLASSIFY_STD.CONSULTING.key]: 'consulting',
        [BUDGET_CLASSIFY_STD.OTHER.key]: 'other',
        [BUDGET_CLASSIFY_STD.LABOR.key]: 'labor',
        [BUDGET_CLASSIFY_STD.MANAGEMENT.key]: 'management',
        [BUDGET_CLASSIFY_STD.PERFORMANCE.key]: 'performance',
        [BUDGET_CLASSIFY_STD.STAFF.key]: 'staff',
        [BUDGET_CLASSIFY_STD.INFRASTRUCTURE.key]: 'infrastructure',
      };
    },

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

    formatWan(value) {
      const n = Number(value);
      return Number.isFinite(n) ? fixed4fractional(n) : fixed4fractional(0);
    },

    parentAvailableOf(prefix) {
      const detail = this.parentBudgetDetailWan || {};
      const bt = Number(detail[`${prefix}FeeBt`]);
      const zc = Number(detail[`${prefix}FeeZc`]);
      return {
        parentAvailableBt: Number.isFinite(bt) ? bt : 0,
        parentAvailableZc: Number.isFinite(zc) ? zc : 0,
      };
    },

    devBudgetOf(key) {
      const src = this.devBudget || {};
      const n = Number(src[key]);
      return Number.isFinite(n) ? n : 0;
    },

    initTableData(budgetData) {
      const src = budgetData || {};
      const pick = (approvedBt, approvedZc, feeBt, feeZc) => ({
        budgetApprovedBt: this.toNonNegNumber(approvedBt),
        budgetApprovedZc: this.toNonNegNumber(approvedZc),
        budgetBt: this.toNonNegNumber(feeBt),
        budgetZc: this.toNonNegNumber(feeZc),
      });
      const row = (classify, prefix) => ({
        num: classify.key,
        itemType: classify.value,
        devBudget: this.devBudgetOf(prefix),
        ...this.parentAvailableOf(prefix),
        ...pick(
            src[`${prefix}FeeApprovedBt`],
            src[`${prefix}FeeApprovedZc`],
            src[`${prefix}FeeBt`],
            src[`${prefix}FeeZc`]
        ),
      });
      this.tableData = [
        row(BUDGET_CLASSIFY_STD.EQUIPMENT, 'equipment'),
        row(BUDGET_CLASSIFY_STD.MATERIAL, 'material'),
        row(BUDGET_CLASSIFY_STD.TEST, 'test'),
        row(BUDGET_CLASSIFY_STD.POWER, 'power'),
        row(BUDGET_CLASSIFY_STD.CONFERENCE, 'conference'),
        row(BUDGET_CLASSIFY_STD.BUSINESS, 'business'),
        row(BUDGET_CLASSIFY_STD.COOPERATION, 'cooperation'),
        row(BUDGET_CLASSIFY_STD.PROPERTY, 'property'),
        row(BUDGET_CLASSIFY_STD.CONSULTING, 'consulting'),
        row(BUDGET_CLASSIFY_STD.OTHER, 'other'),
        row(BUDGET_CLASSIFY_STD.LABOR, 'labor'),
        row(BUDGET_CLASSIFY_STD.MANAGEMENT, 'management'),
        row(BUDGET_CLASSIFY_STD.PERFORMANCE, 'performance'),
        row(BUDGET_CLASSIFY_STD.STAFF, 'staff'),
        row(BUDGET_CLASSIFY_STD.INFRASTRUCTURE, 'infrastructure'),
      ];
    },

    toNonNegNumber(value) {
      if (value === '' || value === null || value === undefined) {
        return 0;
      }
      const n = Number(value);
      return Number.isFinite(n) ? n : NaN;
    },

    /** 子项目拆分时，输入上限为对应父级可用；否则不限 */
    inputMaxOf(row, field) {
      if (!this.showParentAvailable || !row) {
        return undefined;
      }
      if (field === 'budgetApprovedBt') {
        const max = Number(row.parentAvailableBt);
        return Number.isFinite(max) ? max : 0;
      }
      if (field === 'budgetApprovedZc') {
        const max = Number(row.parentAvailableZc);
        return Number.isFinite(max) ? max : 0;
      }
      return undefined;
    },

    clampRowAmount(row, field) {
      if (row[field] === '' || row[field] === null || row[field] === undefined) {
        return;
      }
      const n = this.toNonNegNumber(row[field]);
      if (!Number.isFinite(n) || n < 0) {
        row[field] = 0;
        return;
      }
      const max = this.inputMaxOf(row, field);
      if (max != null && n > max) {
        row[field] = max;
        return;
      }
      row[field] = n;
    },

    normalizeRowAmount(row, field) {
      const n = this.toNonNegNumber(row[field]);
      let value = Number.isFinite(n) && n >= 0 ? n : 0;
      const max = this.inputMaxOf(row, field);
      if (max != null && value > max) {
        value = max;
      }
      row[field] = value;
    },

    validateBudgetAmounts() {
      for (let i = 0; i < this.tableData.length; i++) {
        const row = this.tableData[i];
        const bt = this.toNonNegNumber(row.budgetApprovedBt);
        const zc = this.toNonNegNumber(row.budgetApprovedZc);
        if (!Number.isFinite(bt) || bt < 0 || !Number.isFinite(zc) || zc < 0) {
          Toast.warning(`科目「${row.itemType}」批准金额须为大于等于 0 的数字`, Swal);
          return false;
        }
        row.budgetApprovedBt = bt;
        row.budgetApprovedZc = zc;
      }
      return true;
    },

    /** 子项目拆分：批准为空时，按特性开发需求预填自筹列，且不超过父级自筹可用 */
    suggestApprovedFromDevBudget() {
      (this.tableData || []).forEach((row) => {
        const need = Number(row.devBudget);
        const bt = Number(row.budgetApprovedBt) || 0;
        const zc = Number(row.budgetApprovedZc) || 0;
        if (Number.isFinite(need) && need > 0 && bt + zc === 0) {
          const parentZc = Number(row.parentAvailableZc);
          const maxZc = Number.isFinite(parentZc) ? parentZc : need;
          row.budgetApprovedZc = Math.min(need, Math.max(0, maxZc));
        }
      });
    },

    sumDevBudgetWan() {
      return (this.tableData || []).reduce((sum, row) => {
        const d = Number(row.devBudget);
        return sum + (Number.isFinite(d) ? d : 0);
      }, 0);
    },

    /** 按科目校验批准国拨/自筹不超过父级可用 */
    validateParentAvailableByItem() {
      if (!this.showParentAvailable) {
        return true;
      }
      for (let i = 0; i < this.tableData.length; i++) {
        const row = this.tableData[i];
        const approvedBt = Number(row.budgetApprovedBt) || 0;
        const approvedZc = Number(row.budgetApprovedZc) || 0;
        const parentBt = Number(row.parentAvailableBt) || 0;
        const parentZc = Number(row.parentAvailableZc) || 0;
        if (approvedBt - parentBt > 1e-9) {
          Toast.warning(`科目「${row.itemType}」国拨批准不能超过父级可用`, Swal);
          return false;
        }
        if (approvedZc - parentZc > 1e-9) {
          Toast.warning(`科目「${row.itemType}」自筹批准不能超过父级可用`, Swal);
          return false;
        }
      }
      return true;
    },

    validateSubProjectSplit(sumBudgetWan) {
      const need = this.sumDevBudgetWan();
      if (!(sumBudgetWan > 0)) {
        Toast.warning("批准预算合计必须大于 0", Swal);
        return false;
      }
      if (sumBudgetWan + 1e-9 < need) {
        Toast.warning("批准预算不能小于特性开发基本预算需求", Swal);
        return false;
      }
      if (this.parentRemainingWan != null
          && Number.isFinite(Number(this.parentRemainingWan))
          && sumBudgetWan - Number(this.parentRemainingWan) > 1e-9) {
        Toast.warning("批准预算不能超过父级剩余预算", Swal);
        return false;
      }
      if (!this.validateParentAvailableByItem()) {
        return false;
      }
      return true;
    },

    getSummaries(param) {
      const { columns, data } = param;
      const sums = [];
      let sumParentBt = 0;
      let sumParentZc = 0;
      let sumDev = 0;
      let sumBt = 0;
      let sumZc = 0;
      (data || []).forEach((item) => {
        const pBt = Number(item.parentAvailableBt);
        const pZc = Number(item.parentAvailableZc);
        const d = Number(item.devBudget);
        const bt = Number(item.budgetApprovedBt);
        const zc = Number(item.budgetApprovedZc);
        if (Number.isFinite(pBt)) sumParentBt += pBt;
        if (Number.isFinite(pZc)) sumParentZc += pZc;
        if (Number.isFinite(d)) sumDev += d;
        if (Number.isFinite(bt)) sumBt += bt;
        if (Number.isFinite(zc)) sumZc += zc;
      });
      columns.forEach((column, index) => {
        if (index === 0) {
          sums[index] = '合计';
          return;
        }
        const prop = column.property;
        if (prop === 'parentAvailableBt') {
          sums[index] = this.formatWan(sumParentBt) + ' 万元';
        } else if (prop === 'parentAvailableZc') {
          sums[index] = this.formatWan(sumParentZc) + ' 万元';
        } else if (prop === 'devBudget') {
          sums[index] = this.formatWan(sumDev) + ' 万元';
        } else if (prop === 'budgetApprovedBt') {
          sums[index] = this.formatWan(sumBt) + ' 万元';
        } else if (prop === 'budgetApprovedZc') {
          sums[index] = this.formatWan(sumZc) + ' 万元';
        } else {
          sums[index] = '';
        }
      });
      return sums;
    },

    buildBudgetPayload() {
      const payload = {};
      const fieldMap = this.feeFieldByNum();
      this.tableData.forEach((data) => {
        const prefix = fieldMap[data.num];
        if (!prefix) {
          return;
        }
        const bt = this.toNonNegNumber(data.budgetApprovedBt);
        const zc = this.toNonNegNumber(data.budgetApprovedZc);
        payload[`${prefix}FeeApprovedBt`] = bt;
        payload[`${prefix}FeeApprovedZc`] = zc;
        // 保持现有业务：批准值同步写入可用额度字段
        payload[`${prefix}FeeBt`] = bt;
        payload[`${prefix}FeeZc`] = zc;
      });
      payload.customerId = this.loginCustomer.id;
      payload.projectId = this.projectId;
      payload.parentId = this.projectId;
      payload.subprojectId = this.projectId;
      payload.createrId = this.loginUser.customerUserId;
      payload.stage = BUDGET_TYPE.PROJECT.key;
      return payload;
    },

    calcSumBudget(payload) {
      const prefixes = Object.values(this.feeFieldByNum());
      return prefixes.reduce((sum, prefix) => {
        return sum
            + Number(payload[`${prefix}FeeBt`] || 0)
            + Number(payload[`${prefix}FeeZc`] || 0);
      }, 0);
    },

    saveBudget(){
      let _this = this;
      if (_this.actionBusy) {
        return;
      }
      if (!_this.validateBudgetAmounts()) {
        return;
      }
      _this.budgetData = _this.buildBudgetPayload();

      if(_this.projectType === PROJECT_TYPE.PROJECT.key){
        _this.actionBusy = true;
        _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/budget-direct/saveBudget', _this.budgetData).then((response)=>{
          let resp = response.data;
          if (resp.success) {
            Toast.success("预算保存成功", Swal);
            _this.$bus.$emit('refresh-project-setup-budget', true);
            _this.getBudget();
            _this.close();
          } else {
            Toast.warning(resp.message || "保存失败", Swal);
          }
        }).catch((e) => {
          console.error(e);
          Toast.error("保存失败，请重试", Swal);
        }).finally(() => {
          _this.actionBusy = false;
        });
        return;
      }

      if(_this.projectType === PROJECT_TYPE.SUB_PROJECT.key){
        _this.budgetData.sumBudget = _this.calcSumBudget(_this.budgetData);
        if (!_this.validateSubProjectSplit(_this.budgetData.sumBudget)) {
          return;
        }
        _this.$emit('subProjectBudgetData', _this.budgetData);
        _this.close();
      }
    },

    getBudget(){
      let _this = this;
      let formData = new window.FormData();
      formData.append('projectId', _this.projectId);
      formData.append('stage', BUDGET_TYPE.PROJECT.key);
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/budget-direct/getBudget', formData).then((response)=>{
        let resp = response.data;
        if (resp.success) {
          let budgetData = resp.content;
          if(budgetData && budgetData.hasRecord){
            _this.initTableData(budgetData);
          }
        } else {
          Toast.warning(resp.message, Swal);
        }
      }).catch((e) => {
        console.error(e);
        Toast.error("预算加载失败", Swal);
      })
    },

    close(){
      let _this = this;
      if (_this.actionBusy) {
        return;
      }
      _this.closeFunction();
    },

  }
}
</script>

<style lang="scss">
@import './../../../public/static/scss/caipd';

</style>
