<!--
 * Author: 周朔鹏
 * Date: 2025-02-17
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
        <el-row>
          <el-col :span="9">
            <el-form-item label="退料单号:">
              <el-col :span="23">
                <el-input type="text" style="font-size: 14px;" v-text="dialogInfo.code"></el-input>
              </el-col>
            </el-form-item>
          </el-col>
          <el-col :span="7">
            <el-form-item label="原始申请:">
              <el-col :span="23">
                <el-input type="text" style="font-size: 14px;" v-text="originAppItem.name"></el-input>
              </el-col>
            </el-form-item>
          </el-col>
          <el-col :span="7">
            <el-form-item label="原始单号:">
              <el-col :span="23">
                <el-input type="text" style="font-size: 14px;" v-text="originAppItem.code"></el-input>
              </el-col>
            </el-form-item>
          </el-col>

        </el-row>
        <div class="hr hr-6 dotted"></div>
        <el-row>
          <div v-if="originAppItem" style="margin: 0 80px 20px 80px;">
            <el-table
                :data="materialList"
                stripe
                style="width: 100%">
              <el-table-column label="序号" align="center" width="80">
                <template slot-scope="scope">
                  {{ scope.$index + 1 }}
                </template>
              </el-table-column>
              <el-table-column
                  prop="bomCode"
                  align="center"
                  label="物料编码">
              </el-table-column>
              <el-table-column
                  prop="name"
                  align="center"
                  label="物料名称">
              </el-table-column>
              <el-table-column
                  prop="model"
                  align="center"
                  label="型号">   <!--借用 资产化 费用化-->
              </el-table-column>
              <el-table-column
                  prop="batchCode"
                  align="center"
                  width="100"
                  label="批次号/序列号">
                <template slot-scope="scope">
                  <el-input v-model="scope.row.batchCode" size="small" maxlength="50"></el-input>
                </template>
              </el-table-column>
              <el-table-column
                  prop="unit"
                  align="center"
                  label="计量单位">
              </el-table-column>
              <el-table-column
                  prop="amount"
                  width="90"
                  align="center"
                  label="退料数量">
                <template slot-scope="scope">
                  <el-input type="number" v-model="scope.row.amount" @change="amountChange(scope.row)" size="small"></el-input>
                </template>
              </el-table-column>
              <el-table-column
                  prop="targetWarehouseCode"
                  width="160"
                  align="center"
                  label="目标仓库">
                <template slot-scope="scope">
                  <el-select v-model="scope.row.targetWarehouseCode" size="small" placeholder="选择仓库">
                    <el-option
                        v-for="w in warehouseList"
                        :key="w.code"
                        :label="w.name + '（' + w.code + '）'"
                        :value="w.code">
                    </el-option>
                  </el-select>
                </template>
              </el-table-column>
              <el-table-column
                  prop="checkOrNot"
                  width="90"
                  align="center"
                  label="是否检查">
                <template slot-scope="scope">
<!--                  <el-input  v-model="scope.row.checkOrNot" size="small"></el-input>-->
                  <el-select v-model="scope.row.checkOrNot" placeholder="选">
                    <el-option
                        v-for="item in options"
                        :key="item.value"
                        :label="item.label"
                        :value="item.value">
                    </el-option>
                  </el-select>
                </template>
              </el-table-column>
              <el-table-column
                  fixed="right"
                  label="操作"
                  align="center"
                  width="100">
                <template slot-scope="scope">
                  <div>
                    <el-button type="primary" plain size="small" v-on:click.stop="deleteItem(scope.$index)">
                      删除
                    </el-button>
                  </div>
                </template>
              </el-table-column>
            </el-table>
          </div>
        </el-row>
        <el-row>
          <el-col :span="10">
            <el-form-item label="申请标题" required>
              <el-input type="text" v-model="dialogInfo.name"></el-input>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="22">
            <el-form-item label="退料说明" required>
              <el-input type="textarea" rows="5" v-model="dialogInfo.reason"></el-input>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :offset="10" :span="6">
            <el-form-item label="申请人">
              <el-col :span="24">
                <el-input type="text" style="font-size: 14px;" v-text="loginUser.name"></el-input>
              </el-col>
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="申请时间">
              <el-col :span="24">
                <el-input type="text" style="font-size: 14px;" v-text="getNowTime"></el-input>
              </el-col>
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <div class="clearfix form-actions">
        <div style="display: flex; justify-content: center;">
          <button v-if="getMaterialListStatus"  type="button" v-on:click="application()" class="ca-btn-primary">提 交</button>
          <button v-if="! getMaterialListStatus"  type="button" class="ca-btn-primary-disable">提 交</button>
          <button type="button" v-on:click="close()" class="ca-btn-primary">取 消</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import Swal from "sweetalert2";
import {dateFormat, optionKV} from "@/filter/filter";

export default {
  components: {
  },
  name: "material-application-return-dlg",

  props: {
    appItem: {
      default: null
    },
    closeDlg: {
      type: Function,
      default: null
    },
  },

  data() {
    return {
      jobitemId: {
        default: null
      },
      inheritJobType: {
        default: null
      },
      projectType: {
        default: null
      },

      dialogInfo: {
        code: '',
      },
      materialList: [],
      warehouseList: [],

      loginUser: {},
      loginCustomer: {},
      jobItem: {},
      originAppItem: {},

      timeStamp: 0,

      options: [{
        value: 0,
        label: '是'
      }, {
        value: 1,
        label: '否'
      }],
    };
  },

  computed: {
    getNowTime(){
      return dateFormat(new Date());
    },
    getMaterialListStatus() {
      let _this = this;
      return Tool.isNotEmpty_ES5(_this.materialList);
    }
  },

  mounted: function () {
    let _this = this;
    _this.dialogInfo.code = Uuid.orderNo();
    _this.timeStamp = Date.now();

    _this.getLoginUser();
    _this.getWarehouseList();
    if(Tool.isNotEmpty(_this.appItem)){
      _this.getMaterialManageItemByCode(_this.appItem.code);
      _this.getJobitemSimpleInfo(_this.appItem.jobitemId);
    }
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

    getJobitemSimpleInfo(jobitemId){
      let _this = this;
      let formData = new window.FormData();
      formData.append('jobitemId', jobitemId);
      formData.append('loginUserId', _this.loginUser.customerUserId);
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/job-items/getJobitemSimpleInfo', formData).then((response) => {
        let resp = response.data;
        if (resp.success) {
          _this.jobItem = resp.content;
        }else{
          Toast.warning(resp.message, Swal);
        }
      });
    },

    getMaterialManageItemByCode(code){
      let _this = this;
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/material/getMaterialManageItemByCode/' + code).then((response) => {
        let resp = response.data;
        if (resp.success) {
          _this.originAppItem = resp.content;
          _this.materialList = JSON.parse(JSON.stringify( _this.originAppItem.materialList));
          _this.applyDefaultWarehouse();
        }else{
          Toast.warning(resp.message, Swal);
        }
      });
    },

    getWarehouseList() {
      let _this = this;
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/material/active-warehouses/' + _this.loginCustomer.id).then((response) => {
        let resp = response.data;
        if (resp.success) {
          _this.warehouseList = resp.content || [];
          _this.applyDefaultWarehouse();
        } else {
          Toast.warning(resp.message, Swal);
        }
      });
    },

    // 默认退回原出库仓库；原仓库已停用时留空，由退料人选择
    applyDefaultWarehouse() {
      let _this = this;
      if (!_this.warehouseList.length) {
        return;
      }
      _this.materialList.forEach(item => {
        if (item.targetWarehouseCode) {
          return;
        }
        let origin = item.warehouseCode || _this.originAppItem.warehouseCode || 'LOCAL';
        let active = _this.warehouseList.some(w => w.code === origin);
        _this.$set(item, 'targetWarehouseCode', active ? origin : '');
      });
    },

    amountChange(material){
      let _this = this;
      _this.originAppItem.materialList.filter(item => {
        if(item.id === material.id){
          if(item.amount < material.amount){
            Toast.warning("退料数量不能超过领用数量 " + item.amount + "，已调整", Swal);
            material.amount = item.amount;
          }
        }
      });
    },

    deleteItem(index){
      let _this = this;
      _this.materialList.splice(index, 1);
    },
    /**
     * 提交申请
     */
    application() {
      let _this = this;
      // 保存校验
      if (1 !== 1
          || !Validator.require(_this.dialogInfo.code, "申请单号",Swal)
          || !Validator.require(_this.dialogInfo.name, "申请标题",Swal)
          || !Validator.require(_this.dialogInfo.reason, "领用事由",Swal)

          || !Validator.length(_this.dialogInfo.reason, "领用事由", 6, 800,Swal)
          || !Validator.length(_this.dialogInfo.name, "申请标题", 2, 30,Swal)
      ) {
        return;
      }

      _this.dialogInfo.customerId = _this.originAppItem.customerId;
      _this.dialogInfo.preProjectId = _this.originAppItem.preProjectId;
      _this.dialogInfo.projectId = _this.originAppItem.projectId;
      _this.dialogInfo.subprojectId = _this.originAppItem.subprojectId;
      _this.dialogInfo.characterId = _this.originAppItem.characterId;
      _this.dialogInfo.jobitemId = _this.originAppItem.jobitemId;
      _this.dialogInfo.writerId = _this.originAppItem.writerId;
      _this.dialogInfo.jobType = _this.originAppItem.jobType;
      // _this.dialogInfo.nextNode = _this.originAppItem.nextNode;   //由后台根据情况给出
      _this.dialogInfo.qualityId = _this.originAppItem.qualityId;
      _this.dialogInfo.status = APPLICATION_STATUS.APPLICATION.key;
      _this.dialogInfo.auxStatus = _this.originAppItem.auxStatus;
      _this.dialogInfo.capitalSource = _this.originAppItem.capitalSource;
      _this.dialogInfo.originCode = _this.originAppItem.code;
      _this.dialogInfo.usageMode = _this.originAppItem.usageMode;
      _this.dialogInfo.warehouseCode = _this.originAppItem.warehouseCode;
      _this.dialogInfo.requestId = _this.originAppItem.requestId;

      _this.dialogInfo.projectType = _this.jobItem.projectType;
      _this.dialogInfo.materialList = _this.materialList;
      // 库存按仓库+物料编码记账，批次号/序列号为选填的追溯备注
      let lineMissing = _this.materialList.some(item => Tool.isEmpty(item.checkOrNot)
          || Tool.isEmpty(item.targetWarehouseCode));
      if (lineMissing) {
        Toast.warning("请为每条物料填写是否检查并选择目标仓库", Swal);
        return;
      }

      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/material/saveMaterialApplication_return', _this.dialogInfo).then((response)=>{
        let resp = response.data;
        if (resp.success) {
          _this.close();
          _this.$bus.$emit('refresh-material-application-list', true);
          _this.$bus.$emit('notify-quality-material-application-num', true);
          _this.$bus.$emit('refresh-manhour-num', true);
          Toast.success("申请已发出", Swal);
        }else{
          Toast.warning(resp.message, Swal);
        }
      })
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

.text-box{
  display: inline-block;
  padding-left: 3px;
  padding-right: 15px;
  line-height: 30px;
  //width: 100px;
  background-color: rgba(204, 204, 204, 0.20);
}
.dialog-width{
  width: 100%;
}
.red-color{
  color: red;
}
</style>
