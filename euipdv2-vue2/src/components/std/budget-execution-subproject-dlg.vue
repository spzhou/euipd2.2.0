<!--
 * Author: 周朔鹏
 * Date: 2024-06-14
 * Email: 391902958@qq.com
 * Website: https://www.euipd.com
-->

<template>
  <div>
    <div class="dialog-container">
<!--      <div style="width: 100%; padding: 0 30px; margin-top: -30px;">
        <div style="width: 100%; display: flex; flex-direction: row; justify-content: flex-end;">
          <el-tooltip content="下载预算执行表导入模板, 不要修改文件名称和字段名称, 填写完整后上传." placement="top" effect="light" :open-delay=1000>
            <el-button type="text" @click="downloadFile(templatePath)">下载预算执行表模板: import_budget_execution_table.xlsx</el-button>
          </el-tooltip>
        </div>
      </div>-->
      <div class="hr hr-dotted hr-8"></div>
      <el-form
          label-width="130px"
          :show-message=false
          class="dialog-width">
        <el-row>
          <el-col :span="16">
            <el-form-item label="项目名称:">
              <el-col :span="23">
                <el-input type="text" style="font-size: 14px;" v-text="subProject.label"></el-input>
              </el-col>
            </el-form-item>
          </el-col>
          <el-col :span="7" >
            <div style="display: flex; flex-direction: row; justify-content: flex-end; margin-right: 36px;">
              <el-button type="primary" @click="addBudgetExecutionItem">添加预算执行信息</el-button>
<!--TODO 如果需要导入清单, 需要重新修订-->
<!--              <div style="margin-left: 10px;">
                <el-tooltip content="通过导入预算执行清单, 进行预算数据添加" placement="top" effect="light" :open-delay=1000>
                  <BudgetExecutionListUpload
                      :key="subProject.id"
                      :text="'导入预算执行表'"
                      :input-id="'budget-execution-list-upload'"
                      :budget-execution-list="budgetExecutionList"
                      v-on:budgetExecutionDataList="getBudgetExecutionList"
                  ></BudgetExecutionListUpload>
                </el-tooltip>
              </div>-->
            </div>
          </el-col>
        </el-row>
        <div class="hr hr-6 dotted"></div>
        <el-row>
          <div style="margin: 0 80px 20px 80px;">
            <el-table
                :data="budgetExecutionList"
                stripe
                show-summary
                style="width: 100%">
<!--              <el-table-column
                  prop="serialNo"
                  min-width="100"
                  show-overflow-tooltip
                  label="支出序号">
              </el-table-column>
              <el-table-column
                  prop="name"
                  min-width="150"
                  show-overflow-tooltip
                  label="支出名称">
              </el-table-column>-->
              <el-table-column
                  prop="classify"
                  :formatter="classifyFormat"
                  align="center"
                  min-width="90"
                  show-overflow-tooltip
                  label="费用科目">
              </el-table-column>
              <el-table-column
                  prop="capitalSource"
                  :formatter="capitalFormat"
                  min-width="80"
                  show-overflow-tooltip
                  align="center"
                  label="经费来源">
              </el-table-column>
              <el-table-column
                  prop="amount"
                  min-width="100"
                  show-overflow-tooltip
                  align="center"
                  label="支出金额(元)">
              </el-table-column>
<!--              <el-table-column
                  prop="paymentStatue"
                  :formatter="paymentStatueFormat"
                  min-width="90"
                  show-overflow-tooltip
                  align="center"
                  label="支付状态">
              </el-table-column>-->
              <el-table-column
                  fixed="right"
                  label="操作"
                  align="center"
                  width="80">
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
<!--        <div class="hr hr-8 hr-dotted"></div>
        <div style="margin: 10px 50px;">
          <div style="font-size: 15px; font-weight: 500">
            数据导入功能使用说明:
          </div>
          1. 费用类别必须为: 设备费 测试费 物料费 动力费 会议费 差旅费 合作费 知识产权费 劳务费 人员费 专家费 管理费 其他费用, 增加类别或者修改类别名称, 均不能被导入<br/>
          2. 经费来源必须为: 国拨 自筹, 增加来源或者修改来源名称, 均不能被导入<br/>
          2. 支付状态必须为: 已支付 应付未付, 增加状态或者修改状态名称, 均不能被导入<br/>
          2. 支出序号必须唯一, 重复项不能被导入<br/>

        </div>-->
      </el-form>
      <div style="height: 20px;"></div>
      <div class="clearfix form-actions">
        <div style="display: flex; justify-content: center;">
          <button type="button" v-on:click="saveBudgetList()" class="ca-btn-primary">保 存</button>
          <button type="button" v-on:click="close()" class="ca-btn-primary">取 消</button>
        </div>
      </div>
    </div>

    <!--添加预算执行条目-->
    <el-dialog
        width="29cm"
        :visible.sync="addBudgetExecutionDlg"
        append-to-body
        :before-close="handleClose"
        :close-on-click-modal=false>
      <div slot="title" style="font-size: 20px;">{{ dialogTitle }}</div>
      <div>
        <BudgetExecutionAddDlg
            :key="((subprojectId) + '-' + (random))"
            :budget-execution-list="budgetExecutionList"
            :close-dlg = closeDialog
            v-on:budgetExecutionInfoList="getBudgetExecutionList"
        ></BudgetExecutionAddDlg>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import Swal from "sweetalert2";
import {dateFormat, optionKV} from "@/filter/filter";
import BudgetExecutionListUpload from "@/components/rdms/budget-execution-list-upload.vue";
import BudgetExecutionAddDlg from "@/components/std/budget-execution-add-dlg.vue";
import {webAssetUrl} from "@/shared/oss";

export default {
  components: {
    BudgetExecutionListUpload,
    BudgetExecutionAddDlg,
  },
  name: "budget-execution-subproject-dlg",

  props: {
    subprojectId: {
      default: null
    },
    closeDlg: {
      type: Function,
      default: null
    },
  },

  data() {
    return {
      budgetExecutionList: [],
      MATERIAL_USAGE_MODE: MATERIAL_USAGE_MODE,

      random: 0,

      loginUser: {},
      loginCustomer: {},
      subProject: {},

      addBudgetExecutionDlg: false,
      dialogTitle: '',

      templatePath: webAssetUrl("import_budget_execution_table.xlsx"),
    };
  },

  computed: {

  },

  mounted: function () {
    let _this = this;
    _this.getLoginUser();
    if(Tool.isNotEmpty(_this.subprojectId)){
      _this.getSubProjectInfo(_this.subprojectId);
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

    getSubProjectInfo(subprojectId){
      let _this = this;
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/sub-project/getSubProjectSimpleInfo/' + subprojectId).then((response) => {
        let resp = response.data;
        if (resp.success) {
          _this.subProject = resp.content;
        } else {
          Toast.warning(resp.message, Swal);
        }
      })
    },

    classifyFormat(row){
      return optionKV(BUDGET_CLASSIFY_STD, row.classify);
    },
    capitalFormat(row){
      return optionKV(CAPITAL_SOURCE, row.capitalSource);
    },
    paymentStatueFormat(row){
      return optionKV(PAYMENT_STATUS, row.paymentStatue);
    },


    downloadFile(path) {
      window.open(path, '_self');
    },

    getBudgetExecutionList(budgetExeList){
      let _this = this;
      _this.random = Math.random();
      _this.budgetExecutionList = budgetExeList;
    },

    addBudgetExecutionItem() {
      let _this = this;
      _this.addBudgetExecutionDlg = true;
      _this.dialogTitle = "添加预算执行信息";
    },
    closeDialog() {
      let _this = this;
      _this.addBudgetExecutionDlg = false;
    },
    handleClose(done) {
      done();
    },

    deleteItem(index){
      let _this = this;
      _this.budgetExecutionList.splice(index, 1);
    },
    /**
     * 提交申请
     */
    saveBudgetList() {
      let _this = this;
      _this.budgetExecutionList.forEach((item, index)=>{
        item.customerId = _this.subProject.customerId;
        item.projectId = _this.subProject.projectId;
        // item.parentId = ;  //后台程序填写
        item.subprojectId = _this.subprojectId;
      })

      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/budget-direct-exe/saveBudgetExeList', _this.budgetExecutionList).then((response)=>{
        let resp = response.data;
        if (resp.success) {
          _this.$bus.$emit('refresh-review-subproject-create-page', true);
          _this.close();
          Toast.success("保存成功", Swal);
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
