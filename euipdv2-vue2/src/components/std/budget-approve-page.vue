<!--
 * Author: 周朔鹏
 * Date: 2024-12-23
 * Email: 391902958@qq.com
 * Website: https://www.euipd.com
-->

<template>
  <div style="height: 100%;">
    <div class="paper-height">
      <div style="height: 100%; overflow: scroll;">
        <div class="a4-paper-big-simple">
          <div class="dialog-container">
            <div style="padding: 20px 20px 40px">
              <div style="font-size: 20px;">
                预算申请审批单
              </div>
            </div>
            <el-form
                label-width="130px"
                :show-message=false
                class="dialog-width">
              <el-row>
                <el-col :span="8">
                  <el-form-item label="项目编号:">
                    <el-col :span="23">
                      <el-input type="text" style="font-size: 14px;" v-text="projectInfo.projectCode"></el-input>
                    </el-col>
                  </el-form-item>
                </el-col>
                <el-col :span="8">
                  <el-form-item label="项目名称:">
                    <el-col :span="23">
                      <el-input type="text" style="font-size: 14px;" v-text="getProjectName"></el-input>
                    </el-col>
                  </el-form-item>
                </el-col>
                <el-col :span="8">
                  <el-form-item label="费用阶段:">
                    <el-col :span="23">
                      {{ BUDGET_TYPE | optionKV(projectType)  }}
                    </el-col>
                  </el-form-item>
                </el-col>

              </el-row>
              <div class="hr hr-6 dotted"></div>
              <el-row>
                <el-col :span="10">
                  <el-form-item label="申请标题">
                    <el-col :span="24">
                      <el-input type="text" v-text="budgetAdjustInfo.title"></el-input>
                    </el-col>
                  </el-form-item>
                </el-col>
              </el-row>
              <el-row>
                <el-col :span="7">
                  <el-form-item label="申请金额">
                    <el-col :span="24">
                      <el-input type="text" v-text="getFormatRMB"></el-input>
                    </el-col>
                  </el-form-item>
                </el-col>
                <el-col :span="11">
                  <el-form-item label="费用科目">
                    <el-col :span="24">
                      <el-input type="text" v-text="getSubject"></el-input>
                    </el-col>
                  </el-form-item>
                </el-col>
              </el-row>
              <el-row>
                <el-col :span="22">
                  <el-form-item label="申请原因">
                    <el-input type="text" v-text="budgetAdjustInfo.reason"></el-input>
                  </el-form-item>
                </el-col>
              </el-row>
              <div class="hr hr-6 dotted"></div>
              <el-row>
                <el-col :offset="2" :span="6">
                  <el-form-item label="审批人">
                    <el-col :span="24">
                      <el-input type="text" style="font-size: 14px;" v-text="budgetAdjustInfo.approverName"></el-input>
                    </el-col>
                  </el-form-item>
                </el-col>
                <el-col :span="6">
                  <el-form-item label="申请人">
                    <el-col :span="24">
                      <el-input type="text" style="font-size: 14px;" v-text="budgetAdjustInfo.applicantName"></el-input>
                    </el-col>
                  </el-form-item>
                </el-col>
                <el-col :span="10">
                  <el-form-item label="申请时间">
                    <el-col :span="24">
                      <el-input type="text" style="font-size: 14px;" v-text="getDateTime"></el-input>
                    </el-col>
                  </el-form-item>
                </el-col>
              </el-row>
            </el-form>
            <div v-if="!isComplete" class="clearfix form-actions">
              <div style="display: flex; justify-content: center;">
                <button type="button" v-on:click="approvalPass" class="ca-btn-primary">批 准</button>
                <button type="button" v-on:click="rejectJobitemDlg" class="ca-btn-primary">驳 回</button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 批准-->
    <el-dialog
        :visible.sync="dialogVisible"
        width="23cm"
        :before-close="handleClose"
        :close-on-click-modal=false>
      <div slot="title" style="font-size: 20px;">{{ ((dialogTitle)) }}</div>
      <div v-if="budgetAdjustInfo.id">
        <BudgetAdjustApproveDlg
            :key="budgetAdjustInfo.id"
            :budget-adjust-info="budgetAdjustInfo"
            :close-function="closeDialog"
        ></BudgetAdjustApproveDlg>
      </div>
    </el-dialog>

    <!--驳回-->
    <el-dialog
        width="29cm"
        :visible.sync="rejectJobItemDlg"
        :before-close="handleClose"
        :close-on-click-modal=false>
      <div slot="title" style="font-size: 20px;">{{ dialogTitle }}</div>
      <div v-if="budgetAdjustInfo.id">
        <BudgetAdjustRejectDlg
            :key="budgetAdjustInfo.id"
            :budget-adjust-info="budgetAdjustInfo"
            :close-function="closeDialog"
        ></BudgetAdjustRejectDlg>
      </div>
    </el-dialog>

  </div>
</template>

<script>
import Swal from "sweetalert2";
import {dateFormat, optionKV, rmbFormat} from "@/filter/filter";
import BudgetAdjustApproveDlg from "@/components/std/budget-adjust-approve-dlg.vue";
import BudgetAdjustRejectDlg from "@/components/std/budget-adjust-reject-dlg.vue";

export default {
  components: {
    BudgetAdjustApproveDlg,
    BudgetAdjustRejectDlg,
  },
  name: "budget-approve-page",

  props: {
    budgetAdjustId: {
      default: null,
    },
    isComplete: {
      default: false,
    },
    index: {
      default: null,
    },
    projectType: {
      default: null,
    },
    entrance: {
      default: null,
    },
  },

  data() {
    return {
      loginUser: {},
      loginCustomer: {},

      budgetAdjustInfo: {},
      projectInfo: {},

      dialogVisible: false,
      rejectJobItemDlg: false,
      PROJECT_TYPE: PROJECT_TYPE,
      BUDGET_TYPE: BUDGET_TYPE,

      dialogTitle: '',
    };
  },

  computed: {
    getFormatRMB(){
      let _this = this;
      return rmbFormat(_this.budgetAdjustInfo.amount);
    },
    getDateTime(){
      let _this = this;
      return dateFormat(_this.budgetAdjustInfo.createTime);
    },
    getSubject() {
      let _this = this;
      return optionKV(BUDGET_CLASSIFY_STD, _this.budgetAdjustInfo.budgetSubject);
    },
    getProjectName(){
      let _this =this;
      if(_this.projectType === PROJECT_TYPE.PRE_PROJECT.key){
        return _this.projectInfo.preProjectName;
      }else{
        return _this.projectInfo.projectName;
      }
    },

  },

  mounted: function () {
    let _this = this;
    _this.getLoginUser();
        if(Tool.isNotEmpty(_this.budgetAdjustId)){
          _this.getBudgetAdjustSimpleInfo(_this.budgetAdjustId);
        }
  },

  beforeDestroy() {
  },

  methods: {
    getLoginUser() {
      let _this = this;
      if (Tool.getLoginUser().userGroup === LOGIN_USER_GROUP.CUSTOMER.key) {
        _this.loginUser = Tool.getLoginUser().adminUser;
        _this.loginCustomer = Tool.getLoginUser();
      } else {
        _this.loginUser = Tool.getLoginUser();
        _this.loginCustomer = Tool.getLoginUser().customer;
      }
    },

    getBudgetAdjustSimpleInfo(budgetAdjustId) {
      let _this = this;
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/budget-mng/getBudgetAdjustSimpleInfo/' + budgetAdjustId).then((response) => {
        let resp = response.data;
        if (resp.success) {
          _this.budgetAdjustInfo = resp.content;
          if(_this.projectType === PROJECT_TYPE.PRE_PROJECT.key){
            _this.getPreProjectSimpleInfo(_this.budgetAdjustInfo.preProjectId);
          }else{
            _this.getProjectSimpleInfo(_this.budgetAdjustInfo.projectId);
          }

        } else {
          Toast.warning(resp.message, Swal);
        }
      })
    },

    getProjectSimpleInfo(projectId) {
      let _this = this;
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/project/getProjectSimpleInfo/' + projectId).then((response) => {
        let resp = response.data;
        if (resp.success) {
          _this.projectInfo = resp.content;
        } else {
          Toast.warning(resp.message, Swal);
        }
      })
    },

    getPreProjectSimpleInfo(preprojectId) {
      let _this = this;
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/pre-project/getPreProjectSimpleInfo/' + preprojectId).then((response) => {
        let resp = response.data;
        if (resp.success) {
          _this.projectInfo = resp.content;
        } else {
          Toast.warning(resp.message, Swal);
        }
      })
    },

    approvalPass() {
      let _this = this;
      _this.dialogVisible = true;
      _this.dialogTitle = "填写审批意见";
    },


    rejectJobitemDlg() {
      let _this = this;
      _this.rejectJobItemDlg = true;
      _this.dialogTitle = "填写驳回意见";
    },

    handleClose(done) {
      let _this = this;
      _this.dialogVisible = false;
      _this.rejectJobItemDlg = false;
      done();
    },

    closeDialog() {
      let _this = this;
      _this.dialogVisible = false;
      _this.rejectJobItemDlg = false;
    },
  },
}
</script>

<style lang="scss" scoped>
@import './../../../public/static/scss/caipd';

.paper-height {
  height: calc(100vh - 310px);
}

</style>
