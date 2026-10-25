<!--
 * Author: 周朔鹏
 * Date: 2024-12-05
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
                <el-input type="text" style="font-size: 14px;" :value="subProject.projectCode" readonly></el-input>
              </el-col>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="当前项目:">
              <el-col :span="23">
                <el-input type="text" style="font-size: 14px;" :value="subProject.label" readonly></el-input>
              </el-col>
            </el-form-item>
          </el-col>
        </el-row>
        <div class="hr hr-6 dotted"></div>
        <el-row>
          <el-col :span="11">
            <el-form-item label="下级项目" required>
              <el-col :span="24">
                <el-select v-model="dialogInfo.targetProjectId" filterable style="width: 350px;" placeholder="选择下级项目">
                  <el-option v-for="o in codeAndNameList"
                             :key="o.id"
                             :value="o.id"
                             :label="o.name"></el-option>
                </el-select>
              </el-col>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="7">
            <el-form-item label="分配金额(元)" required>
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
            <el-form-item label="分配原因" required>
              <el-input type="textarea" rows="5" v-model="dialogInfo.reason"></el-input>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :offset="8" :span="6">
            <el-form-item label="项目经理">
              <el-col :span="24">
                <el-input type="text" style="font-size: 14px;" :value="subProject.projectManagerName" readonly></el-input>
              </el-col>
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="执行时间">
              <el-col :span="24">
                <el-input type="text" style="font-size: 14px;" :value="getNowTime" readonly></el-input>
              </el-col>
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <div class="clearfix form-actions">
        <div style="display: flex; justify-content: center;">
          <button type="button" v-on:click="distribute()" class="ca-btn-primary">分 配</button>
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
  name: "budget-distribute-dlg",

  props: {
    subProject: {
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

      random: 0,
      jobitemId: '',

      loginUser: {},
      loginCustomer: {},
      jobItem: {},
      nextLeveProjectList:[],
      codeAndNameList:[],

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
    _this.getNextLevelSubproject(_this.subProject.id);
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

    targetProjectLabel() {
      let _this = this;
      const target = _this.codeAndNameList.find(o => o.id === _this.dialogInfo.targetProjectId);
      return target ? target.name : _this.dialogInfo.targetProjectId;
    },

    budgetSubjectFormat(row){
      return optionKV(BUDGET_CLASSIFY_STD, row.budgetSubject);
    },

    getNextLevelSubproject(subProjectId) {
      // debugger
      let _this = this;
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/sub-project/getNextLevelSubproject/'+ subProjectId).then((response)=>{
        let resp = response.data;
        if (resp.success) {
          _this.nextLeveProjectList = resp.content;
          _this.codeAndNameList = [];
          if(Tool.isNotEmpty_ES5(_this.nextLeveProjectList)){
            _this.nextLeveProjectList.forEach((item, index) => {
              let element = {};
              element.id = item.id;
              element.name = item.projectCode + ':' + item.label;
              _this.codeAndNameList.push(element)
            })
          }
        } else {
          Toast.warning(resp.message,Swal);
        }
      })
    },

    /**
     * 提交申请
     */
    distribute() {
      let _this = this;
      _this.dialogInfo.code = Uuid.shortUuid();
      // 保存校验
      if (1 !== 1
          || !Validator.require(_this.dialogInfo.targetProjectId, "下级项目",Swal)
          || !Validator.require(_this.dialogInfo.amount, "分配金额",Swal)
          || !Validator.require(_this.dialogInfo.reason, "分配原因",Swal)
          || !Validator.require(_this.dialogInfo.budgetSubject, "费用科目",Swal)

          || !Validator.length(_this.dialogInfo.reason, "分配原因", 6, 800,Swal)
          || !Validator.length(_this.dialogInfo.budgetSubject, "费用科目", 2, 20,Swal)
          || !Validator.isNumber(_this.dialogInfo.amount, "分配金额", Swal)
          || !Validator.greatThanZero(_this.dialogInfo.amount, "分配金额", Swal)
      ) {
        return;
      }
      _this.dialogInfo.title = "分配预算:" + _this.targetProjectLabel();

      Swal.fire({
        title: '确认',
        text: "是否确认预算调整相关信息",
        icon: 'question',
        showCancelButton: true,
        confirmButtonColor: '#409EFF',
        cancelButtonColor: '#909399',
        confirmButtonText: '确认!',
        cancelButtonText: '取消!'
      }).then((result) => {
        if (result.isConfirmed) {
          _this.dialogInfo.customerId = _this.subProject.customerId;
          _this.dialogInfo.projectId = _this.subProject.id;
          _this.dialogInfo.applicantId = _this.loginUser.customerUserId;
          _this.dialogInfo.status = BUDGET_APPLICANT_STATUS.SUBMIT.key;
          _this.dialogInfo.type = BUDGET_TYPE.PROJECT.key;

          _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/budget-mng/saveBudgetDistribution', _this.dialogInfo).then((response) => {
            let resp = response.data;
            if (resp.success) {
              _this.close();
              _this.$bus.publish({ areas: ['budget', 'badge'], reason: 'budget-distribute' });
              Toast.success("分配单已提交，待预算台账审批人审批", Swal);
            } else {
              Toast.warning(resp.message, Swal);
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
