<!--
 * Author: 周朔鹏
 * Date: 2024-06-13
 * Email: 391902958@qq.com
 * Website: https://www.euipd.com
-->

<template>
  <div>
    <el-button type="primary" size="small" v-on:click="selectFile()" style="width: 150px;">
      <i class="ace-icon fa fa-upload"></i>
      {{text}}
    </el-button>
    <input class="hidden" type="file" ref="file" v-on:change="uploadFile()" v-bind:id="inputId+'-input'">
  </div>
</template>

<script>
  import Swal from "sweetalert2";
  import $ from 'jquery'
  export default {
    name: 'budget-execution-list-upload',
    props: {
      text: {
        default: "上传文件"
      },
      inputId: {
        default: "file-upload"
      },
      budgetExecutionList: {
        default: null
      },
    },
    data: function () {
      return {
        loginUser: {},
        loginCustomer: {},

        sheetContent: [],
        budgetExecutionDataList: [],
        classify: [
            '设备费', '测试费', '物料费', '动力费', '会议费', '差旅费', '合作费', '知识产权费', '劳务费', '人员费', '专家费', '管理费', '其他费用'
        ],
        capitalSource: [
            '国拨', '自筹'
        ],
        paymentStatue: [
          '已支付', '应付未付'
        ],
      }
    },

    mounted: function() {
      let _this = this;
      _this.getLoginUser();

      _this.budgetExecutionDataList = _this.budgetExecutionList;
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

      selectFile () {
        let _this = this;
        $("#" + _this.inputId + "-input").trigger("click");
      },

      uploadFile () {
        let _this = this;
        let file = _this.$refs.file.files[0];
        if(file && file.name === "import_budget_execution_table.xlsx"){
          _this.readBudgetExeListFromLocalFile(file, _this.readBudgetExeList)
        }else{
          Toast.error("文件名称必须为:import_budget_execution_table.xlsx", Swal);
        }
      },

      // 读取本地excel文件
      readBudgetExeListFromLocalFile(file, callback) {
        var reader = new FileReader();
        reader.readAsBinaryString(file);
        reader.onload = async function (e) {
          var data = e.target.result;
          const XLSX = await import(/* webpackChunkName: "xlsx" */ 'xlsx');
          var budgetExecutionList = XLSX.read(data, {type: 'binary'});
          if (callback) callback(budgetExecutionList, XLSX);
        };
      },

      readBudgetExeList(budgetExecutionList, XLSX) {
        let _this = this;
        var sheetContent = []
        // 遍历每张表读取
        for (var sheet in budgetExecutionList.Sheets) {
          if (budgetExecutionList.Sheets.hasOwnProperty(sheet)) {
            let fromTo = budgetExecutionList.Sheets[sheet]['!ref'];
            sheetContent.push(XLSX.utils.sheet_to_json(budgetExecutionList.Sheets[sheet]))
            break; // 如果有多张表，就注释掉这行
          }
        }
        _this.sheetContent = sheetContent[0];

        _this.saveBudgetExeList(_this.sheetContent);
      },

      saveBudgetExeList(budgetExeList) {
        let _this = this;
        let budgetExecutionList = [];
        if(budgetExeList && budgetExeList.length>0){
          budgetExeList.forEach((budgetExeItem, index) => {
            if(budgetExeItem.serialNo
                && budgetExeItem.name
                && budgetExeItem.classify
                && budgetExeItem.capitalSource
                && budgetExeItem.amount
                && budgetExeItem.paymentStatue
            ){
              budgetExecutionList.push(budgetExeItem);
            }
          });
          _this.addBudgetExeItemList(budgetExecutionList)
        }
      },

      addBudgetExeItemList(budgetExecutionList){
        let _this = this;
        budgetExecutionList.forEach(item => {
          if (1 !== 1
              || !Validator.require(item.serialNo, "支出序号",Swal, true)
              || !Validator.require(item.name, "支出名称",Swal, true)
              || !Validator.require(item.classify, "费用科目",Swal, true)
              || !Validator.require(item.capitalSource, "资金来源",Swal, true)
              || !Validator.require(item.amount, "支出数额",Swal, true)
              || !Validator.require(item.paymentStatue, "支付状态",Swal, true)

              || !Validator.length(item.serialNo, "支出序号", 2, 50,Swal, true)
              || !Validator.length(item.name, "支出名称", 2, 200,Swal, true)
              || !Validator.length(item.classify, "费用科目", 2, 20,Swal, true)
              || !Validator.length(item.capitalSource, "资金来源", 2, 20,Swal, true)
              || !Validator.length(item.amount, "支出数额", 1, 12,Swal, true)
              || !Validator.length(item.paymentStatue, "支付状态", 1, 20,Swal, true)

              || !Validator.greatThanZero(item.amount, "支出数额", Swal, true)
              || !Validator.arrayIncludes(_this.classify, item.classify, "费用科目", Swal, true)
              || !Validator.arrayIncludes(_this.capitalSource, item.capitalSource, "资金来源", Swal, true)
              || !Validator.arrayIncludes(_this.paymentStatue, item.paymentStatue, "支付状态", Swal, true)
          ) {
            return;
          }
          //判断现有清单中序号是否重复?重复的不添加  但数据向数据库保存时, 后台将全局进一步验证序号是否重复问题
          let serialNoList = _this.budgetExecutionDataList.map(itemData => itemData.serialNo);
          if(!serialNoList.includes(item.serialNo)){
            item.id = Uuid.shortUuid(8);
            _this.budgetExecutionDataList.push(item);
          }
        })
        this.$emit('budgetExecutionDataList', _this.budgetExecutionDataList);  //监听 事件
      }
    }
  }
</script>
