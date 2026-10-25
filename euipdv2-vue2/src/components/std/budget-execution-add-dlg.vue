<!--
 * Author: 周朔鹏
 * Date: 2024-11-12
 * Email: 391902958@qq.com
 * Website: https://www.euipd.com
-->

<template>
  <div>
    <div class="dialog-container">
      <el-form
          label-width="130px"
          :show-message=false
          class="dialog-width">
<!-- 此行输入数据暂不启用 -->
        <el-row>
          <el-col :span="7">
            <el-form-item label="支出序号">
              <el-col :span="24">
                <el-input v-model="dialogInfo.serialNo"></el-input>
              </el-col>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="支出名称">
              <el-col :span="24">
                <el-input v-model="dialogInfo.name"></el-input>
              </el-col>
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="支付状态">
              <el-col :span="24">
                <el-select v-model="dialogInfo.paymentStatue" filterable placeholder="支付状态">
                  <el-option
                      v-for="item in PAYMENT_STATUS"
                      :key="item.key"
                      :label="item.value"
                      :value="item.key">
                  </el-option>
                </el-select>
              </el-col>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row>
          <el-col :span="7">
            <el-form-item label="费用科目" required>
              <el-col :span="24">
                <el-select v-model="dialogInfo.classify" filterable placeholder="费用科目">
                  <el-option
                      v-for="item in BUDGET_CLASSIFY_STD"
                      :key="item.key"
                      :label="item.value"
                      :value="item.key">
                  </el-option>
                </el-select>
              </el-col>
            </el-form-item>
          </el-col>
          <el-col :span="7">
            <el-form-item label="资金来源" required>
              <el-col :span="24">
                <el-select v-model="dialogInfo.capitalSource" filterable placeholder="资金来源">
                  <el-option
                      v-for="item in CAPITAL_SOURCE"
                      :key="item.key"
                      :label="item.value"
                      :value="item.key">
                  </el-option>
                </el-select>
              </el-col>
            </el-form-item>
          </el-col>
          <el-col :span="7">
            <el-form-item label="支出数额(元)" required>
              <el-col :span="24">
                <el-input type="number" v-model="dialogInfo.amount"></el-input>
              </el-col>
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <div class="clearfix form-actions" style="margin-top: 50px;">
        <div style="display: flex; justify-content: center;">
          <button  type="button" v-on:click="addToList(dialogInfo)" class="ca-btn-primary">添 加</button>
          <button type="button" v-on:click="close()" class="ca-btn-primary">取 消</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import Swal from "sweetalert2";

export default {
  components: {
  },
  name: "budget-execution-add-dlg",

  props: {
    budgetExecutionList: {
      default: null
    },
    closeDlg: {
      type: Function,
      default: null
    },
  },

  data() {
    return {
      dialogInfo: {
        id: '',
        serialNo: '',
        name: '项目费用',
        classify: '',
        capitalSource: '',
        amount: 0,
        paymentStatue: PAYMENT_STATUS.PAID.key,
        paymentTime: '',
      },
      budgetExecutionInfoList: [],

      BUDGET_CLASSIFY_STD: BUDGET_CLASSIFY_STD,
      CAPITAL_SOURCE: CAPITAL_SOURCE,
      PAYMENT_STATUS: PAYMENT_STATUS,

    };
  },

  computed: {

  },

  mounted: function () {
    let _this = this;
    _this.dialogInfo.id = Uuid.shortUuid(8);
    _this.budgetExecutionInfoList = _this.budgetExecutionList;
  },

  methods: {
    addToList(dialogInfo) {
      let _this = this;
      if (1 !== 1
          // || !Validator.require(dialogInfo.serialNo, "支出序号",Swal)
          // || !Validator.require(dialogInfo.name, "支出名称",Swal)
          || !Validator.require(dialogInfo.classify, "费用科目",Swal)
          || !Validator.require(dialogInfo.capitalSource, "资金来源",Swal)
          || !Validator.require(dialogInfo.amount, "支出数额",Swal)
          || !Validator.require(dialogInfo.paymentStatue, "支付状态",Swal)

          // || !Validator.length(dialogInfo.serialNo, "支出序号", 2, 50,Swal)
          // || !Validator.length(dialogInfo.name, "支出名称", 2, 200,Swal)
          || !Validator.length(dialogInfo.classify, "费用科目", 2, 20,Swal)
          || !Validator.length(dialogInfo.capitalSource, "资金来源", 2, 20,Swal)
          || !Validator.length(dialogInfo.amount, "支出数额", 1, 12,Swal)
          || !Validator.length(dialogInfo.paymentStatue, "支付状态", 1, 20,Swal)

          // || !Validator.greatThanZero(dialogInfo.amount, "支出数额", Swal)
      ) {
        return;
      }

      if(Tool.isEmpty(dialogInfo.serialNo)){
        dialogInfo.serialNo = Uuid.shortUuid(8);
      }

      _this.budgetExecutionInfoList.push(dialogInfo);
      this.$emit('budgetExecutionInfoList', _this.budgetExecutionInfoList);  //监听 事件
      _this.close();
    },

    close(){
      let _this = this;
      _this.closeDlg();
    },
  },
}
</script>

<style lang="scss" scoped>
@import '../../../public/static/scss/caipd';

.dialog-width{
  width: 100%;
}

</style>
