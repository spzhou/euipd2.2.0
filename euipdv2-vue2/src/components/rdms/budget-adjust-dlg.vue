<!--
 * Author: 周朔鹏
 * Date: 2024-06-14
 * Email: 391902958@qq.com
 * Website: https://www.euipd.com
-->

<template>
  <div class="dialog-container">
    <div style="margin: 0 20px 20px;">
      <el-table
          :data="tableData"
          :summary-method="getSummaries"
          show-summary
          style="width: 100%">
<!--        <el-table-column label="序号" align="center" width="80">
          <template slot-scope="scope">
            {{ scope.$index+1 }}
          </template>
        </el-table-column>-->
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
            min-width="100">
        </el-table-column>
        <el-table-column
            align="center"
            label="批准预算">
          <el-table-column
              prop="budgetApprovedBt"
              align="center"
              label="国拨资金( 万元 )"
              width="150">
          </el-table-column>
          <el-table-column
              prop="budgetApprovedZc"
              align="center"
              width="150"
              label="自筹资金( 万元 )">
          </el-table-column>
        </el-table-column>
        <el-table-column
            align="center"
            label="调整后预算( 注: 无调整 可不填 )">
          <el-table-column
              align="center"
              label="国拨资金( 万元 )"
              width="150">
            <template slot-scope="scope">
              <el-input type="number" v-model="scope.row.budgetBt"></el-input>
            </template>
          </el-table-column>
          <el-table-column
              align="center"
              width="150"
              label="自筹资金( 万元 )">
            <template slot-scope="scope">
              <el-input type="number" v-model="scope.row.budgetZc"></el-input>
            </template>
          </el-table-column>
        </el-table-column>
      </el-table>
    </div>
    <div class="clearfix form-actions">
      <div style="display: flex; justify-content: center;">
        <button v-on:click="saveBudget()" class="ca-btn-primary" type="button">确定</button>
        <button type="button" v-on:click="close()" class="ca-btn-primary">关闭</button>
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
  name: "budget-adjust-dlg",

  data: function() {
    return {
      loginUser: {},
      loginCustomer: {},

      character: {},
      PROJECT_TYPE: PROJECT_TYPE,

      tableData: [{
        num: BUDGET_CLASSIFY_STD.EQUIPMENT.key,
        itemType: BUDGET_CLASSIFY_STD.EQUIPMENT.value,
        budgetApprovedBt: 0,
        budgetApprovedZc: 0,
        budgetBt: 0,
        budgetZc: 0,
      }, {
        num: BUDGET_CLASSIFY_STD.MATERIAL.key,
        itemType: BUDGET_CLASSIFY_STD.MATERIAL.value,
        budgetApprovedBt: 0,
        budgetApprovedZc: 0,
        budgetBt: 0,
        budgetZc: 0,
      }, {
        num: BUDGET_CLASSIFY_STD.TEST.key,
        itemType: BUDGET_CLASSIFY_STD.TEST.value,
        budgetApprovedBt: 0,
        budgetApprovedZc: 0,
        budgetBt: 0,
        budgetZc: 0,
      }, {
        num: BUDGET_CLASSIFY_STD.POWER.key,
        itemType: BUDGET_CLASSIFY_STD.POWER.value,
        budgetApprovedBt: 0,
        budgetApprovedZc: 0,
        budgetBt: 0,
        budgetZc: 0,
      }, {
        num: BUDGET_CLASSIFY_STD.CONFERENCE.key,
        itemType: BUDGET_CLASSIFY_STD.CONFERENCE.value,
        budgetApprovedBt: 0,
        budgetApprovedZc: 0,
        budgetBt: 0,
        budgetZc: 0,
      }, {
        num: BUDGET_CLASSIFY_STD.BUSINESS.key,
        itemType: BUDGET_CLASSIFY_STD.BUSINESS.value,
        budgetApprovedBt: 0,
        budgetApprovedZc: 0,
        budgetBt: 0,
        budgetZc: 0,
      }, {
        num: BUDGET_CLASSIFY_STD.COOPERATION.key,
        itemType: BUDGET_CLASSIFY_STD.COOPERATION.value,
        budgetApprovedBt: 0,
        budgetApprovedZc: 0,
        budgetBt: 0,
        budgetZc: 0,
      }, {
        num: BUDGET_CLASSIFY_STD.PROPERTY.key,
        itemType: BUDGET_CLASSIFY_STD.PROPERTY.value,
        budgetApprovedBt: 0,
        budgetApprovedZc: 0,
        budgetBt: 0,
        budgetZc: 0,
      }, {
        num: BUDGET_CLASSIFY_STD.CONSULTING.key,
        itemType: BUDGET_CLASSIFY_STD.CONSULTING.value,
        budgetApprovedBt: 0,
        budgetApprovedZc: 0,
        budgetBt: 0,
        budgetZc: 0,
      }, {
        num: BUDGET_CLASSIFY_STD.OTHER.key,
        itemType: BUDGET_CLASSIFY_STD.OTHER.value,
        budgetApprovedBt: 0,
        budgetApprovedZc: 0,
        budgetBt: 0,
        budgetZc: 0,
      }, {
        num: BUDGET_CLASSIFY_STD.LABOR.key,
        itemType: BUDGET_CLASSIFY_STD.LABOR.value,
        budgetApprovedBt: 0,
        budgetApprovedZc: 0,
        budgetBt: 0,
        budgetZc: 0,
      }, {
        num: BUDGET_CLASSIFY_STD.MANAGEMENT.key,
        itemType: BUDGET_CLASSIFY_STD.MANAGEMENT.value,
        budgetApprovedBt: 0,
        budgetApprovedZc: 0,
        budgetBt: 0,
        budgetZc: 0,
      }, {
        num: BUDGET_CLASSIFY_STD.PERFORMANCE.key,
        itemType: BUDGET_CLASSIFY_STD.PERFORMANCE.value,
        budgetApprovedBt: 0,
        budgetApprovedZc: 0,
        budgetBt: 0,
        budgetZc: 0,
      }, {
        num: BUDGET_CLASSIFY_STD.STAFF.key,
        itemType: BUDGET_CLASSIFY_STD.STAFF.value,
        budgetApprovedBt: 0,
        budgetApprovedZc: 0,
        budgetBt: 0,
        budgetZc: 0,
      }, {
        num: BUDGET_CLASSIFY_STD.INFRASTRUCTURE.key,
        itemType: BUDGET_CLASSIFY_STD.INFRASTRUCTURE.value,
        budgetApprovedBt: 0,
        budgetApprovedZc: 0,
        budgetBt: 0,
        budgetZc: 0,
      }],

      budgetData: {},
    }
  },

  props: {
    subprojectId:{
      default: null,
    },
    projectType:{
      default: null,
    },
    closeFunction: {
      type: Function,
      default: null
    },
  },

  computed: {
  },

  mounted: function() {
    let _this = this;
    _this.getLoginUser();
    _this.getBudget();

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

    getSummaries(param) {
      const { columns, data } = param;
      // debugger
      const sums = [];
      columns.forEach((column, index) => {
        if (index === 0) {
          sums[index] = '总价';
          return;
        }

        const budgetApprovedBts = data.map(item => Number(item.budgetApprovedBt));
        if (!budgetApprovedBts.every(value => isNaN(value))) {
          sums[2] = budgetApprovedBts.reduce((prev, curr) => {
            const value = Number(curr);
            if (!isNaN(value)) {
              return prev + curr;
            } else {
              return prev;
            }
          }, 0);
          sums[2] = fixed4fractional(sums[2]);
          sums[2] += ' 万元';
        } else {
          sums[2] = 'N/A';
        }

        const budgetApprovedZcs = data.map(item => Number(item.budgetApprovedZc));
        if (!budgetApprovedZcs.every(value => isNaN(value))) {
          sums[3] = budgetApprovedZcs.reduce((prev, curr) => {
            const value = Number(curr);
            if (!isNaN(value)) {
              return prev + curr;
            } else {
              return prev;
            }
          }, 0);
          sums[3] = fixed4fractional(sums[3]);
          sums[3] += ' 万元';
        } else {
          sums[3] = 'N/A';
        }

        const budgetBts = data.map(item => Number(item.budgetBt));
        if (!budgetBts.every(value => isNaN(value))) {
          sums[4] = budgetBts.reduce((prev, curr) => {
            const value = Number(curr);
            if (!isNaN(value)) {
              return prev + curr;
            } else {
              return prev;
            }
          }, 0);
          sums[4] = fixed4fractional(sums[4]);
          sums[4] += ' 万元';
        } else {
          sums[4] = 'N/A';
        }

        const budgetZcs = data.map(item => Number(item.budgetZc));
        if (!budgetZcs.every(value => isNaN(value))) {
          sums[5] = budgetZcs.reduce((prev, curr) => {
            const value = Number(curr);
            if (!isNaN(value)) {
              return prev + curr;
            } else {
              return prev;
            }
          }, 0);
          sums[5] = fixed4fractional(sums[5]);
          sums[5] += ' 万元';
        } else {
          sums[5] = 'N/A';
        }

        /*sums[1] = budgetApprovedBt + budgetApprovedZc + budgetBt + budgetZc;
        sums[1] += ' 万元';*/
      });

      return sums;
    },

    saveBudget(){
      let _this = this;

      _this.tableData.forEach((data, index) => {
        if(data.num === BUDGET_CLASSIFY_STD.EQUIPMENT.key){
          //设备费
          _this.budgetData.equipmentFeeBt = data.budgetBt > 0 ? data.budgetBt : 0;
          _this.budgetData.equipmentFeeZc = data.budgetZc > 0 ? data.budgetZc : 0;
        }
        if(data.num === BUDGET_CLASSIFY_STD.MATERIAL.key){
          //物料费
          _this.budgetData.materialFeeBt = data.budgetBt > 0 ? data.budgetBt : 0;
          _this.budgetData.materialFeeZc = data.budgetZc > 0 ? data.budgetZc : 0;
        }
        if(data.num === BUDGET_CLASSIFY_STD.TEST.key){
          //测试化验加工费
          _this.budgetData.testFeeBt = data.budgetBt > 0 ? data.budgetBt : 0;
          _this.budgetData.testFeeZc = data.budgetZc > 0 ? data.budgetZc : 0;
        }

        if(data.num === BUDGET_CLASSIFY_STD.POWER.key){
          //燃料动力费
          _this.budgetData.powerFeeBt = data.budgetBt > 0 ? data.budgetBt : 0;
          _this.budgetData.powerFeeZc = data.budgetZc > 0 ? data.budgetZc : 0;
        }
        if(data.num === BUDGET_CLASSIFY_STD.CONFERENCE.key){
          //会议费
          _this.budgetData.conferenceFeeBt = data.budgetBt > 0 ? data.budgetBt : 0;
          _this.budgetData.conferenceFeeZc = data.budgetZc > 0 ? data.budgetZc : 0;
        }
        if(data.num === BUDGET_CLASSIFY_STD.BUSINESS.key){
          //差旅费
          _this.budgetData.businessFeeBt = data.budgetBt > 0 ? data.budgetBt : 0;
          _this.budgetData.businessFeeZc = data.budgetZc > 0 ? data.budgetZc : 0;
        }
        if(data.num === BUDGET_CLASSIFY_STD.COOPERATION.key){
          //合作交流费
          _this.budgetData.cooperationFeeBt = data.budgetBt > 0 ? data.budgetBt : 0;
          _this.budgetData.cooperationFeeZc = data.budgetZc > 0 ? data.budgetZc : 0;
        }
        if(data.num === BUDGET_CLASSIFY_STD.PROPERTY.key){
          //知识产权类费用
          _this.budgetData.propertyFeeBt = data.budgetBt > 0 ? data.budgetBt : 0;
          _this.budgetData.propertyFeeZc = data.budgetZc > 0 ? data.budgetZc : 0;
        }
        if(data.num === BUDGET_CLASSIFY_STD.CONSULTING.key){
          //专家咨询费
          _this.budgetData.consultingFeeBt = data.budgetBt > 0 ? data.budgetBt : 0;
          _this.budgetData.consultingFeeZc = data.budgetZc > 0 ? data.budgetZc : 0;
        }
        if(data.num === BUDGET_CLASSIFY_STD.OTHER.key){
          //其他费用
          _this.budgetData.otherFeeBt = data.budgetBt > 0 ? data.budgetBt : 0;
          _this.budgetData.otherFeeZc = data.budgetZc > 0 ? data.budgetZc : 0;
        }
        if(data.num === BUDGET_CLASSIFY_STD.LABOR.key){
          //劳务费
          _this.budgetData.laborFeeBt = data.budgetBt > 0 ? data.budgetBt : 0;
          _this.budgetData.laborFeeZc = data.budgetZc > 0 ? data.budgetZc : 0;
        }
        if(data.num === BUDGET_CLASSIFY_STD.MANAGEMENT.key){
          //管理费
          _this.budgetData.managementFeeBt = data.budgetBt > 0 ? data.budgetBt : 0;
          _this.budgetData.managementFeeZc = data.budgetZc > 0 ? data.budgetZc : 0;
        }
        if(data.num === BUDGET_CLASSIFY_STD.PERFORMANCE.key){
          //绩效费
          _this.budgetData.performanceFeeBt = data.budgetBt > 0 ? data.budgetBt : 0;
          _this.budgetData.performanceFeeZc = data.budgetZc > 0 ? data.budgetZc : 0;
        }
        if(data.num === BUDGET_CLASSIFY_STD.STAFF.key){
          //人员费
          _this.budgetData.staffFeeBt = data.budgetBt > 0 ? data.budgetBt : 0;
          _this.budgetData.staffFeeZc = data.budgetZc > 0 ? data.budgetZc : 0;
        }
        if(data.num === BUDGET_CLASSIFY_STD.INFRASTRUCTURE.key){
          //基础建设费
          _this.budgetData.infrastructureFeeBt = data.budgetBt > 0 ? data.budgetBt : 0;
          _this.budgetData.infrastructureFeeZc = data.budgetZc > 0 ? data.budgetZc : 0;
        }
      });

      _this.budgetData.customerId = _this.loginCustomer.id;
      _this.budgetData.subprojectId = _this.subprojectId;  //主项目的情况, 如果是子项目, 后台程序会做出修改
      _this.budgetData.modifierId = _this.loginUser.customerUserId;

      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/budget-direct/saveBudget_subproject', _this.budgetData).then((response)=>{
        let resp = response.data;
        if (resp.success) {
          // _this.$bus.$emit('refresh-project-setup-budget', true);
          _this.getBudget();
          _this.close();
        } else {
          Toast.warning(resp.message, Swal);
        }
      })
      _this.close();
    },

    getBudget(){
      let _this = this;
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/budget-direct/getBudget_subproject', {
        customerId: _this.loginCustomer.id,
        subprojectId: _this.subprojectId,
      }).then((response)=>{
        let resp = response.data;
        if (resp.success) {
          let budgetData = resp.content;
          if(budgetData.hasRecord){
            _this.tableData = [{
              num: BUDGET_CLASSIFY_STD.EQUIPMENT.key,
              itemType: BUDGET_CLASSIFY_STD.EQUIPMENT.value,
              budgetApprovedBt: budgetData.equipmentFeeApprovedBt,
              budgetApprovedZc: budgetData.equipmentFeeApprovedZc,
              budgetBt: budgetData.equipmentFeeBt,
              budgetZc: budgetData.equipmentFeeZc,
            },{
              num: BUDGET_CLASSIFY_STD.MATERIAL.key,
              itemType: BUDGET_CLASSIFY_STD.MATERIAL.value,
              budgetApprovedBt: budgetData.materialFeeApprovedBt,
              budgetApprovedZc: budgetData.materialFeeApprovedZc,
              budgetBt: budgetData.materialFeeBt,
              budgetZc: budgetData.materialFeeZc,
            },{
              num: BUDGET_CLASSIFY_STD.TEST.key,
              itemType: BUDGET_CLASSIFY_STD.TEST.value,
              budgetApprovedBt: budgetData.testFeeApprovedBt,
              budgetApprovedZc: budgetData.testFeeApprovedZc,
              budgetBt: budgetData.testFeeBt,
              budgetZc: budgetData.testFeeZc,
            }, {
              num: BUDGET_CLASSIFY_STD.POWER.key,
              itemType: BUDGET_CLASSIFY_STD.POWER.value,
              budgetApprovedBt: budgetData.powerFeeApprovedBt,
              budgetApprovedZc: budgetData.powerFeeApprovedZc,
              budgetBt: budgetData.powerFeeBt,
              budgetZc: budgetData.powerFeeZc,
            }, {
              num: BUDGET_CLASSIFY_STD.CONFERENCE.key,
              itemType: BUDGET_CLASSIFY_STD.CONFERENCE.value,
              budgetApprovedBt: budgetData.conferenceFeeApprovedBt,
              budgetApprovedZc: budgetData.conferenceFeeApprovedZc,
              budgetBt: budgetData.conferenceFeeBt,
              budgetZc: budgetData.conferenceFeeZc,
            }, {
              num: BUDGET_CLASSIFY_STD.BUSINESS.key,
              itemType: BUDGET_CLASSIFY_STD.BUSINESS.value,
              budgetApprovedBt: budgetData.businessFeeApprovedBt,
              budgetApprovedZc: budgetData.businessFeeApprovedZc,
              budgetBt: budgetData.businessFeeBt,
              budgetZc: budgetData.businessFeeZc,
            }, {
              num: BUDGET_CLASSIFY_STD.COOPERATION.key,
              itemType: BUDGET_CLASSIFY_STD.COOPERATION.value,
              budgetApprovedBt: budgetData.cooperationFeeApprovedBt,
              budgetApprovedZc: budgetData.cooperationFeeApprovedZc,
              budgetBt: budgetData.cooperationFeeBt,
              budgetZc: budgetData.cooperationFeeZc,
            }, {
              num: BUDGET_CLASSIFY_STD.PROPERTY.key,
              itemType: BUDGET_CLASSIFY_STD.PROPERTY.value,
              budgetApprovedBt: budgetData.propertyFeeApprovedBt,
              budgetApprovedZc: budgetData.propertyFeeApprovedZc,
              budgetBt: budgetData.propertyFeeBt,
              budgetZc: budgetData.propertyFeeZc,
            }, {
              num: BUDGET_CLASSIFY_STD.CONSULTING.key,
              itemType: BUDGET_CLASSIFY_STD.CONSULTING.value,
              budgetApprovedBt: budgetData.consultingFeeApprovedBt,
              budgetApprovedZc: budgetData.consultingFeeApprovedZc,
              budgetBt: budgetData.consultingFeeBt,
              budgetZc: budgetData.consultingFeeZc,
            },{
              num: BUDGET_CLASSIFY_STD.OTHER.key,
              itemType: BUDGET_CLASSIFY_STD.OTHER.value,
              budgetApprovedBt: budgetData.otherFeeApprovedBt,
              budgetApprovedZc: budgetData.otherFeeApprovedZc,
              budgetBt: budgetData.otherFeeBt,
              budgetZc: budgetData.otherFeeZc,
            }, {
              num: BUDGET_CLASSIFY_STD.LABOR.key,
              itemType: BUDGET_CLASSIFY_STD.LABOR.value,
              budgetApprovedBt: budgetData.laborFeeApprovedBt,
              budgetApprovedZc: budgetData.laborFeeApprovedZc,
              budgetBt: budgetData.laborFeeBt,
              budgetZc: budgetData.laborFeeZc,
            }, {
              num: BUDGET_CLASSIFY_STD.MANAGEMENT.key,
              itemType: BUDGET_CLASSIFY_STD.MANAGEMENT.value,
              budgetApprovedBt: budgetData.managementFeeApprovedBt,
              budgetApprovedZc: budgetData.managementFeeApprovedZc,
              budgetBt: budgetData.managementFeeBt,
              budgetZc: budgetData.managementFeeZc,
            },{
              num: BUDGET_CLASSIFY_STD.PERFORMANCE.key,
              itemType: BUDGET_CLASSIFY_STD.PERFORMANCE.value,
              budgetApprovedBt: budgetData.performanceFeeApprovedBt,
              budgetApprovedZc: budgetData.performanceFeeApprovedZc,
              budgetBt: budgetData.performanceFeeBt,
              budgetZc: budgetData.performanceFeeZc,
            },{
              num: BUDGET_CLASSIFY_STD.STAFF.key,
              itemType: BUDGET_CLASSIFY_STD.STAFF.value,
              budgetApprovedBt: budgetData.staffFeeApprovedBt,
              budgetApprovedZc: budgetData.staffFeeApprovedZc,
              budgetBt: budgetData.staffFeeBt,
              budgetZc: budgetData.staffFeeZc,
            },{
              num: BUDGET_CLASSIFY_STD.INFRASTRUCTURE.key,
              itemType: BUDGET_CLASSIFY_STD.INFRASTRUCTURE.value,
              budgetApprovedBt: budgetData.infrastructureFeeApprovedBt,
              budgetApprovedZc: budgetData.infrastructureFeeApprovedZc,
              budgetBt: budgetData.infrastructureFeeBt,
              budgetZc: budgetData.infrastructureFeeZc,
            }];
          }
        } else {
          Toast.warning(resp.message, Swal);
        }
      })
    },

    close(){
      let _this = this;
      _this.closeFunction();
    },

  }
}
</script>

<style lang="scss">
@import './../../../public/static/scss/caipd';

</style>
