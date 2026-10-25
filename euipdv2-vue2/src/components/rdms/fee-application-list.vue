<!--
 * Author: 周朔鹏
 * Date: 2024-12-22
 * Email: 391902958@qq.com
 * Website: https://www.euipd.com
-->

<template>
  <div>
    <div class="row">
      <div style="margin: 0 15px;">
        <p>费用管理: 是为<strong>工作执行人员</strong>提供的进行差旅等费用管理的功能页面.</p>
        <div class="hr hr-18 dotted hr-double"></div>
        <div>
          <el-table
              :data="feeApplicationList"
              row-key="id"
              border
              default-expand-all
              highlight-current-row
              style="width: 100%"
              :row-class-name="tableRowClassName"
              @row-click="rowClick">
            <el-table-column label="序号" align="center" width="90">
              <template slot-scope="scope">
                {{ scope.$index + 1 }}
              </template>
            </el-table-column>
            <el-table-column
                prop="code"
                width="180"
                align="center"
                label="申请单号"
                show-overflow-tooltip
            ></el-table-column>
            <el-table-column
                prop="name"
                align="center"
                label="申请标题"
                show-overflow-tooltip
            ></el-table-column>
            <el-table-column
                prop="jobitemName"
                show-overflow-tooltip
                sortable
                label="任务名称"
                align="center"
            ></el-table-column>
            <el-table-column
                prop="accountCost"
                :formatter="currencyFormat"
                sortable
                width="130"
                label="金额"
                align="center"
            ></el-table-column>
            <el-table-column
                prop="type"
                :formatter="typeFormat"
                width="110"
                sortable
                label="费用类型"
                align="center"
            ></el-table-column>
            <el-table-column
                prop="status"
                sortable
                :formatter="statusFormat"
                label="状态"
                width="110"
                align="center"
            ></el-table-column>
            <el-table-column
                prop="writerName"
                label="申请人"
                width="110"
                align="center"
            ></el-table-column>
            <el-table-column
                prop="approverName"
                label="审批人"
                width="110"
                align="center"
            ></el-table-column>
            <el-table-column
                prop="createTime"
                sortable
                :formatter="dateFormat"
                label="申请日期"
                width="150"
                align="center"
            ></el-table-column>
            <el-table-column
                fixed="right"
                label="操作"
                align="center"
                width="120">
              <template slot-scope="scope">
                <div>
                  <el-button v-if="scope.row.status !== APPLICATION_STATUS.REJECT.key && scope.row.status !== APPLICATION_STATUS.APPLICATION.key" type="primary" plain size="small" v-on:click.stop="feeDealWith(scope.row)">
                    费用处理
                  </el-button>
                  <el-button v-if="scope.row.status === APPLICATION_STATUS.REJECT.key" type="primary" plain size="small" v-on:click.stop="confirmCancel(scope.row)">
                    确认
                  </el-button>
                </div>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </div>
    </div>

    <!--打印费用申请单-->
    <el-dialog
        width="25.0cm"
        :visible.sync="feeApproveDlg"
        :before-close="handleClose"
        :close-on-click-modal=false>
      <div>
        <FeeApproveInfoDlg
            :key="appItemCode"
            :application-code="appItemCode"
            :onlyShowInfo="true"
            :close-dlg = closeDialog
        ></FeeApproveInfoDlg>
      </div>
    </el-dialog>

    <!--费用处理-->
    <el-dialog
        width="29.0cm"
        :visible.sync="feeDealWithDlg"
        :before-close="handleClose"
        :close-on-click-modal=false>
      <div slot="title" style="font-size: 20px;">{{ dialogTitle }}</div>
      <div>
        <FeeDealWithDlg
            :key="appItem.code"
            :app-item="appItem"
            :close-function="closeDialog"
        ></FeeDealWithDlg>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import TabButton from "@/components/rdms/tab-button";
import File from "@/components/rdms/file";
import Swal from "sweetalert2";
import {dateFormat, fixed2fractional, optionKV, rmbFormat, shortDateFormat} from "@/filter/filter";
import SubprojectTaskTab from "@/components/rdms/subproject-task-tab.vue";
import DevelopCharacterTaskTab from "@/components/rdms/develop-character-task-tab.vue";
import FeeApproveInfoDlg from "@/components/std/fee-approve-info-dlg.vue";
import FeeDealWithDlg from "@/components/rdms/fee-deal-with-dlg.vue";

export default {
  components: {
    SubprojectTaskTab,
    DevelopCharacterTaskTab,
    File,
    TabButton,
    FeeDealWithDlg,
    FeeApproveInfoDlg,
  },
  name: "fee-application-list",

  props: {
    writerId: {
      default: null,
    }
  },

  data: function () {
    return {
      loginUser: {},
      loginCustomer: {},

      feeApplicationList: [],

      APPLICATION_STATUS: APPLICATION_STATUS,

      feeApproveDlg: false,
      feeDealWithDlg: false,
      dealWithFlag: '',
      dialogTitle: '',
      appItemCode: '',

      appItem: {},
    }
  },
  mounted: function () {
    let _this = this;
    _this.getLoginUser();
    if(Tool.isNotEmpty(_this.writerId)){
      _this.getFeeApplicationListByWriterId(_this.writerId);
    }

    _this.$bus.$on('refresh-fee-application-list', (data)=>{
      if(data){
        _this.getFeeApplicationListByWriterId(_this.writerId);
      }
    });
  },

  beforeDestroy: function () {
    let _this = this;
  },

  methods: {
    dateFormat(row, column) {
      return dateFormat(row.createTime);
    },

    tableRowClassName({row, rowIndex}) {
      if (row.status === APPLICATION_STATUS.APPLICATION.key || row.status === APPLICATION_STATUS.SUPPLEMENT.key) {
        return 'application-row';
      } else if (row.status === APPLICATION_STATUS.APPROVED.key) {
        return 'approve-row';
      } else if (row.status === APPLICATION_STATUS.REJECT.key) {
        return 'reject-row';
      }
      return '';
    },

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

    statusFormat(row, column) {
      return optionKV(APPLICATION_STATUS, row.status);
    },

    typeFormat(row, column) {
      return optionKV(FEE_TYPE_STD, row.type);
    },

    currencyFormat(row, column) {
      if (row.accountCost) {
        return rmbFormat(row.accountCost);
      }
      return 0;
    },

    rowClick(row, column, e) {
      let _this = this;
      _this.feeApprove(row);
    },

    feeApprove(appItem) {
      let _this = this;
      _this.appItemCode = appItem.code;
      _this.feeApproveDlg = true;
      _this.dialogTitle = "费用申请单";
    },

    feeDealWith(appItem) {
      let _this = this;
      _this.appItem = appItem;
      _this.feeDealWithDlg = true;
      _this.dialogTitle = "费用处理";
    },

    closeDialog() {
      let _this = this;
      _this.feeApproveDlg = false;
      _this.feeDealWithDlg = false;
    },
    handleClose(done) {
      done();
    },

    getFeeApplicationListByWriterId(writerId) {
      let _this = this;
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/fee/getFeeApplicationListByWriterId/' + writerId ).then((response) => {
        let resp = response.data;
        if (resp.success) {
          _this.feeApplicationList = resp.content;
        } else {
          Toast.warning(resp.message, Swal);
        }
      })
    },

    confirmCancel(item){
      let _this = this;
      Swal.fire({
        title: '确认',
        text: "如确实必要, 请沟通好后, 重新提交申请.",
        icon: 'question',
        showCancelButton: true,
        confirmButtonColor: '#409EFF',
        cancelButtonColor: '#909399',
        confirmButtonText: '确认!',
        cancelButtonText: '取消!'
      }).then((result) => {
        if (result.isConfirmed) {
          _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/fee/confirmCancel/' + item.code).then((response) => {
            let resp = response.data;
            if (resp.success) {
              _this.getFeeApplicationListByWriterId(_this.writerId);
            } else {
              Toast.warning(resp.message, Swal);
            }
          });
        }
      }).catch(_ => {
      });
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
