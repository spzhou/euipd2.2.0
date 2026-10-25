<!--
 * Author: 周朔鹏
 * Date: 2025-02-26
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
          <el-col :span="8">
            <el-form-item label="申请单号:">
              <el-col :span="23">
                <el-input type="text" style="font-size: 14px;" v-text="dialogInfo.code"></el-input>
              </el-col>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="任务名称:">
              <el-col :span="23">
                <el-input type="text" style="font-size: 14px;" v-text="jobItem.jobName"></el-input>
              </el-col>
            </el-form-item>
          </el-col>
        </el-row>
        <div class="hr hr-6 dotted"></div>
        <el-row>
          <el-col :span="10">
            <el-form-item label="申请标题" required>
              <el-col :span="24">
                <el-input v-model="dialogInfo.name"></el-input>
              </el-col>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="7">
            <el-form-item label="申请金额" required>
              <el-col :span="24">
                <el-input v-model="dialogInfo.appliedAmount" type="number"></el-input>
              </el-col>
            </el-form-item>
          </el-col>
          <el-col :span="11">
            <el-form-item label="费用科目" required>
              <el-col :span="24">
                <el-select v-model="dialogInfo.type" filterable placeholder="费用科目">
                  <el-option v-for="o in FEE_TYPE_STD"
                             :key="o.key"
                             :value="o.key"
                             :label="o.value"></el-option>
                </el-select>
              </el-col>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="22">
            <el-form-item label="申请事由" required>
              <el-input type="textarea" rows="5" v-model="dialogInfo.reason"></el-input>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :offset="4" :span="6">
            <el-form-item label="申请人">
              <el-col :span="24">
                <el-input type="text" style="font-size: 14px;" v-text="jobItem.executorName"></el-input>
              </el-col>
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="审批人">
              <el-col :span="24">
                <el-input type="text" style="font-size: 14px;" v-text="approverName" readonly></el-input>
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
          <button type="button" v-on:click="application()" class="ca-btn-primary">提 交</button>
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
  name: "fee-application-dlg",

  props: {
    jobitemId: {
      default: null
    },
    inheritJobType: {
      default: null
    },
    projectType: {
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
        code: '',
      },
      FEE_TYPE_STD: FEE_TYPE_STD,

      random: 0,

      loginUser: {},
      loginCustomer: {},
      jobItem: {},
      approverName: '',

    };
  },

  computed: {
    getNowTime(){
      return dateFormat(new Date());
    },
  },

  mounted: function () {
    let _this = this;
    _this.dialogInfo.code = Uuid.orderNo();

    _this.getLoginUser();
    if(Tool.isNotEmpty(_this.jobitemId)){
      _this.getJobitemSimpleInfo(_this.jobitemId);
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
          _this.previewApprover();
        }else{
          Toast.warning(resp.message, Swal);
        }
      });
    },

    feeTypeFormat(row){
      return optionKV(FEE_TYPE_STD, row.type);
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
          || !Validator.require(_this.dialogInfo.appliedAmount, "申请金额",Swal)
          || !Validator.require(_this.dialogInfo.reason, "申请事由",Swal)
          || !Validator.require(_this.dialogInfo.type, "费用科目",Swal)

          || !Validator.length(_this.dialogInfo.name, "申请标题", 2, 30,Swal)
          || !Validator.length(_this.dialogInfo.reason, "领用事由", 6, 800,Swal)
          || !Validator.length(_this.dialogInfo.type, "费用科目", 2, 20,Swal)
          || !Validator.greatThanZero(_this.dialogInfo.appliedAmount, "申请金额", Swal)
      ) {
        return;
      }

      _this.dialogInfo.customerId = _this.jobItem.customerId;
      _this.dialogInfo.preProjectId = _this.jobItem.preProjectId;
      _this.dialogInfo.projectId = _this.jobItem.projectId;
      _this.dialogInfo.subprojectId = _this.jobItem.subprojectId;
      _this.dialogInfo.characterId = _this.jobItem.characterId;
      _this.dialogInfo.jobitemId = _this.jobitemId;
      _this.dialogInfo.writerId = _this.loginUser.customerUserId;
      _this.dialogInfo.projectType = _this.projectType;

      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/fee/saveFeeApplication', _this.dialogInfo).then((response)=>{
        let resp = response.data;
        if (resp.success) {
          _this.close();
          _this.$bus.$emit('refresh-fee-application-list', true);
          _this.$bus.$emit('notify-quality-fee-application-num', true);
          _this.$bus.$emit('refresh-manhour-num', true);
          Toast.success("申请已发出", Swal);
        } else {
          Toast.warning(resp.message, Swal);
        }
      })
    },

    previewApprover() {
      let _this = this;
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/fee/previewHandler/query', {
        jobitemId: _this.jobitemId
      }).then((response) => {
        let resp = response.data;
        if (resp.success && resp.content) {
          _this.approverName = resp.content.approverName || '';
        } else {
          _this.approverName = '';
          Toast.warning(resp.message || '未能确定审批人', Swal);
        }
      });
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
