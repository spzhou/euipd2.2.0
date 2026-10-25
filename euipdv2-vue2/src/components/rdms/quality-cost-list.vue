<!--
 * Author: 周朔鹏
 * Date: 2025-02-05
 * Email: 391902958@qq.com
 * Website: https://www.euipd.com
-->

<template>
  <div>
    <div class="row">
      <div style="margin: 0 15px;">
        <p>质量成本: 相应的项目发生的质量成本的统计表, 质量成本是处理所有由于研发问题造成的质量问题的总成本.
          对于所有的特性 子项目而言, 评审通过即是验收通过. 评审之后发生的问题均是质量问题, 对于研发过程而言, 研发质量和由于研发缺陷造成的产品质量从管理意义上来讲是统一的.</p>
        <div class="hr hr-18 dotted hr-double"></div>
        <div>
          <el-table
              :data="qualityCostList"
              row-key="id"
              border
              default-expand-all
              highlight-current-row
              show-summary
              :summary-method="getSummaries"
              style="width: 100%"
              @row-click="rowClick">
            <el-table-column
                prop="qualitySerial"
                align="center"
                label="质量反馈号"
                show-overflow-tooltip
            ></el-table-column>
            <el-table-column
                prop="jobSerial"
                align="center"
                label="质量任务号"
                show-overflow-tooltip
            ></el-table-column>
            <el-table-column
                prop="name"
                show-overflow-tooltip
                label="质量反馈名称"
                align="center"
            ></el-table-column>
            <el-table-column
                prop="subprojectName"
                show-overflow-tooltip
                sortable
                label="关联子项目名称"
                align="center"
            ></el-table-column>
            <el-table-column
                prop="characterName"
                show-overflow-tooltip
                sortable
                label="关联特性名称"
                align="center"
            ></el-table-column>
            <el-table-column
                prop="projectManagerName"
                width="120"
                sortable
                label="子项目经理"
                align="center"
            ></el-table-column>
            <el-table-column
                prop="sumManhourCost"
                width="120"
                :formatter="sumManhourCostFormat"
                sortable
                label="工时费用"
                align="center"
            ></el-table-column>
            <el-table-column
                prop="sumMaterialCost"
                width="120"
                :formatter="sumMaterialCostFormat"
                sortable
                label="物料费用"
                align="center"
            ></el-table-column>
            <el-table-column
                prop="sumFeeCost"
                width="120"
                :formatter="sumFeeCostFormat"
                sortable
                label="其他费用"
                align="center"
            ></el-table-column>
            <el-table-column
                prop="sumCost"
                width="120"
                :formatter="sumCostFormat"
                sortable
                label="质量成本小计"
                align="center"
            ></el-table-column>
          </el-table>
        </div>
      </div>
    </div>

    <el-dialog
        width="33cm"
        :visible.sync="qualityDealwithDlg"
        :before-close="handleClose"
        :close-on-click-modal=false>
      <div slot="title" style="font-size: 20px;">{{ dialogTitle }}</div>
      <div>
        <QualityDealwithInfo
            :key="qualityItem.id"
            :quality-id="qualityItem.id"
            :close-function="closeDialog"
        ></QualityDealwithInfo>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import TabButton from "@/components/rdms/tab-button";
import File from "@/components/rdms/file";
import Swal from "sweetalert2";
import { rmbFormat} from "@/filter/filter";
import QualityDealwithInfo from "@/components/rdms/quality-dealwith-info.vue";

export default {
  components: {
    File,
    TabButton,
    QualityDealwithInfo
  },
  name: "quality-cost-list",

  props: {
    projectId: {
      default: null
    },
  },

  data: function () {
    return {
      loginUser: {},
      loginCustomer: {},

      qualityCostList: [],

      qualityDealwithDlg: false,
      dialogTitle: '',
      qualityItem: '',

      random: 0,
    }
  },
  mounted: function () {
    let _this = this;
    _this.getLoginUser();
    _this.getQualityCostListByProjectId(_this.projectId);

    _this.$bus.$on('refresh-quality-cost-list', (data)=>{
      if(data){
        _this.getQualityCostListByProjectId(_this.projectId);
      }
    });

  },

  beforeDestroy: function () {
    let _this = this;
    _this.$bus.$off('refresh-quality-cost-list');
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

    sumManhourCostFormat(row, column) {
      if (row.sumManhourCost) {
        return rmbFormat(row.sumManhourCost);
      }
      return 0;
    },

    sumMaterialCostFormat(row, column) {
      if (row.sumMaterialCost) {
        return rmbFormat(row.sumMaterialCost);
      }
      return 0;
    },

    sumFeeCostFormat(row, column) {
      if (row.sumFeeCost) {
        return rmbFormat(row.sumFeeCost);
      }
      return 0;
    },

    sumCostFormat(row, column) {
      if (row.sumCost) {
        return rmbFormat(row.sumCost);
      }
      return 0;
    },

    rowClick(row, column, e) {
      let _this = this;
      _this.qualityDealwith(row);
    },

    qualityDealwith(qualityItem) {
      let _this = this;
      _this.qualityItem = qualityItem;
      _this.qualityDealwithDlg = true;
      _this.dialogTitle = "质量问题处理记录";
    },
    closeDialog() {
      let _this = this;
      _this.qualityDealwithDlg = false;
    },
    handleClose(done) {
      done();
    },

    getQualityCostListByProjectId(projectId) {
      let _this = this;
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/quality/getQualityCostListByProjectId/' + projectId ).then((response) => {
        let resp = response.data;
        if (resp.success) {
          if(resp.content){
            _this.qualityCostList = resp.content;
          }else{
            _this.qualityCostList = [];
          }
        } else {
          Toast.warning(resp.message, Swal);
        }
      })
    },

    //统计表格的汇总信息
    getSummaries(param) {
      let _this = this;
      const {columns, data} = param;
      console.log(data);
      const sums = [];
      const sums_number = [];
      columns.forEach((column, index) => {
        if ( index === 1 || index === 2 || index === 3 || index === 4 || index === 5 ) {
          sums[index] = '';
          return;
        }
        if (index === 0) {
          sums[index] = '总计';
          return;
        }
        if(Tool.isNotEmpty_ES5(data)){
          const values = data.map(item => Number(item[column.property]));  //关键程序行

          if (!values.every(value => isNaN(value))) {
            sums[index] = values.reduce((prev, curr) => {
              const value = Number(curr);
              if (!isNaN(value)) {
                return prev + curr;
              } else {
                return prev;
              }
            }, 0);
            sums[index] = rmbFormat(sums[index]);
          } else {
            sums[index] = 'N/A';
          }
        }else{
          sums[index] = 'N/A';
        }
      });

      return sums;
    },
  }
}
</script>

<style lang="scss">
@import './../../../public/static/scss/caipd.scss';

.el-table .application-row {
  background: $colorRowApplication;
}

.el-table .approve-row {
  background: $colorRowApproved;
}
.el-table .reject-row {
  background: $colorRowReject;
}
.el-badge__content.is-dot {
  height: 10px;
  width: 10px;
  padding: 0;
  right: -2px !important;
  border-radius: 50%;
}
</style>
