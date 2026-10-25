<!--
 * Author: 周朔鹏
 * Date: 2024-12-23
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
            <el-form-item label="项目编号:">
              <el-col :span="23">
                <el-input type="text" style="font-size: 14px;" :value="project.projectCode" readonly></el-input>
              </el-col>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="项目名称:">
              <el-col :span="23">
                <el-input type="text" style="font-size: 14px;" :value="getProjectName" readonly></el-input>
              </el-col>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="预算阶段:">
              <el-col :span="23">
                {{ BUDGET_TYPE | optionKV(type)}}
              </el-col>
            </el-form-item>
          </el-col>

        </el-row>
        <div class="hr hr-6 dotted"></div>
        <el-row>
          <el-col :span="10">
            <el-form-item label="申请标题" required>
              <el-col :span="24">
                <el-input v-model="dialogInfo.title"></el-input>
              </el-col>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="7">
            <el-form-item label="申请金额" required>
              <el-col :span="24">
                <el-input v-model="dialogInfo.amount" type="number"></el-input>
              </el-col>
            </el-form-item>
          </el-col>
          <el-col :span="11">
            <el-form-item label="费用科目" required>
              <el-col :span="24">
                <el-select v-model="dialogInfo.budgetSubject" filterable placeholder="费用科目">
                  <el-option v-for="o in BUDGET_CLASSIFY_STD"
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
            <el-form-item label="申请原因" required>
              <el-input type="textarea" rows="5" v-model="dialogInfo.reason"></el-input>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :offset="2" :span="6">
            <el-form-item label="审批人">
              <el-col :span="24">
                <el-input type="text" style="font-size: 14px;" :value="project.bossName" readonly></el-input>
              </el-col>
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="申请人">
              <el-col :span="24">
                <el-input type="text" style="font-size: 14px;" :value="loginUser.trueName" readonly></el-input>
              </el-col>
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="申请时间">
              <el-col :span="24">
                <el-input type="text" style="font-size: 14px;" :value="getNowTime" readonly></el-input>
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
  name: "budget-application-dlg",

  props: {
    project: {
      default: null
    },
    type: {
      default: null
    },
    closeFunction: {
      type: Function,
      default: null
    },
  },

  data() {
    return {
      dialogInfo: {
        code: '',
      },
      BUDGET_CLASSIFY_STD: BUDGET_CLASSIFY_STD,
      BUDGET_TYPE: BUDGET_TYPE,
      random: 0,
      jobitemId: '',

      loginUser: {},
      loginCustomer: {},
      jobItem: {},

    };
  },

  computed: {
    getNowTime(){
      return dateFormat(new Date());
    },
    getProjectName(){
      let _this = this;
      if(_this.type === BUDGET_TYPE.PRE_PROJECT.key){
        return _this.project.preProjectName;
      }else if(_this.type === BUDGET_TYPE.PRODUCT.key){
        return _this.project.projectName;
      }else{
        return _this.project.label;
      }
    },
  },

  mounted: function () {
    let _this = this;
    _this.dialogInfo.code = Uuid.orderNo();
    _this.getLoginUser();
  },

  methods: {
    optionKV,
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

    budgetSubjectFormat(row){
      return optionKV(BUDGET_CLASSIFY_STD, row.budgetSubject);
    },

    /**
     * 提交申请
     */
    application() {
      let _this = this;
      // 保存校验
      if (1 !== 1
          || !Validator.require(_this.dialogInfo.code, "申请单号",Swal)
          || !Validator.require(_this.dialogInfo.title, "申请标题",Swal)
          || !Validator.require(_this.dialogInfo.amount, "申请金额",Swal)
          || !Validator.require(_this.dialogInfo.reason, "申请原因",Swal)
          || !Validator.require(_this.dialogInfo.budgetSubject, "费用科目",Swal)

          || !Validator.length(_this.dialogInfo.title, "申请标题", 2, 30,Swal)
          || !Validator.length(_this.dialogInfo.reason, "申请原因", 6, 800,Swal)
          || !Validator.length(_this.dialogInfo.budgetSubject, "费用科目", 2, 20,Swal)
          || !Validator.isNumber(_this.dialogInfo.amount, "申请金额", Swal)
          || !Validator.greatThanZero(_this.dialogInfo.amount, "申请金额", Swal)
      ) {
        return;
      }

      Swal.fire({
        title: '确认',
        text: "是否确认提交预算申请",
        icon: 'question',
        showCancelButton: true,
        confirmButtonColor: '#409EFF',
        cancelButtonColor: '#909399',
        confirmButtonText: '确认!',
        cancelButtonText: '取消!'
      }).then((result) => {
        if (result.isConfirmed) {
          _this.dialogInfo.customerId = _this.project.customerId;
          _this.dialogInfo.applicantId = _this.loginUser.customerUserId;
          _this.dialogInfo.status = BUDGET_APPLICANT_STATUS.SUBMIT.key;

          if(_this.type === BUDGET_TYPE.PROJECT.key){
            _this.dialogInfo.projectId = _this.project.id;
            _this.dialogInfo.type = BUDGET_TYPE.PROJECT.key;
            _this.dialogInfo.approverId = _this.project.supervise;
          }else if(_this.type === BUDGET_TYPE.PRODUCT.key){
            _this.dialogInfo.projectId = _this.project.id;
            _this.dialogInfo.type = BUDGET_TYPE.PRODUCT.key;
            _this.dialogInfo.approverId = _this.project.bossId;
          }else if(_this.type === BUDGET_TYPE.PRE_PROJECT.key){
            _this.dialogInfo.preProjectId = _this.project.id;
            _this.dialogInfo.type = BUDGET_TYPE.PRE_PROJECT.key;
            _this.dialogInfo.approverId = _this.project.bossId;
          }else{
            return;
          }

          _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/budget-mng/saveBudgetApplication', _this.dialogInfo).then((response) => {
            let resp = response.data;
            if (resp.success) {
              _this.close();
              _this.$bus.publish({ areas: ['budget', 'badge'], reason: 'budget-apply' });
              Toast.success("申请已发出", Swal);
            }
          })
        }
      }).catch(_ => {
      });

    },

    close(){
      let _this = this;
      _this.closeFunction();
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
