<!--
 * Author: 周朔鹏
 * Date: 2025-04-26
 * Email: 391902958@qq.com
 * Website: https://www.euipd.com
-->

<template>
  <div>
    <div class="row">
      <div style="margin: 0 15px;">
        <p>物料管理: 是为<strong>工作执行人员</strong>提供的进行在手研发物料管理的功能页面.</p>
        <div class="hr hr-18 dotted hr-double"></div>
        <div>
          <el-table
              class="material-application-table"
              :data="materialApplicationList"
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
                align="center"
                label="申请单号"
                width="180"
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
                label="金额"
                align="center"
            ></el-table-column>
            <el-table-column
                prop="usageMode"
                width="110"
                :formatter="usageModeFormat"
                sortable
                label="使用方式"
                align="center"
            ></el-table-column>
            <el-table-column
                prop="status"
                width="110"
                sortable
                :formatter="statusFormat"
                label="状态"
                align="center"
            ></el-table-column>
            <el-table-column
                prop="writerName"
                width="110"
                label="申请人"
                align="center"
            ></el-table-column>
            <el-table-column
                prop="createTime"
                sortable
                :formatter="dateFormat"
                label="申请日期"
                align="center"
            ></el-table-column>
            <el-table-column
                fixed="right"
                label="操作"
                align="center"
                width="220">
              <template slot-scope="scope">
                <div>
                  <el-button disabled v-if="scope.row.pendingIssue" type="text">
                    待物料管理员发料
                  </el-button>
                  <el-button v-if="!scope.row.pendingIssue
                  && scope.row.status !== APPLICATION_STATUS.REJECT.key
                  && scope.row.status !== APPLICATION_STATUS.PRE_APPROVED.key
                  && scope.row.status !== APPLICATION_STATUS.APPLICATION.key
                  && scope.row.opeStatus !== MATERIAL_OPE_STATUS.RETURN.key"
                             type="primary" plain size="small" v-on:click.stop="materialDealWith(scope.row)">
                    物料处置
                  </el-button>
                  <el-button v-if="!scope.row.pendingIssue
                  && scope.row.status === APPLICATION_STATUS.APPROVED.key
                  && scope.row.opeStatus === null"
                             type="primary" plain size="small" v-on:click.stop="materialReturnApplication(scope.row)">
                    退料申请
                  </el-button>
                  <el-button v-if="scope.row.status === APPLICATION_STATUS.REJECT.key && scope.row.opeStatus === null " type="primary" plain size="small" v-on:click.stop="materialModify(scope.row)">
                    修改
                  </el-button>
                  <el-button v-if="scope.row.status === APPLICATION_STATUS.REJECT.key" type="primary" plain size="small" v-on:click.stop="confirmCancel(scope.row)">
                    废弃
                  </el-button>
                  <el-button disabled v-if="scope.row.opeStatus === MATERIAL_OPE_STATUS.RETURN.key && scope.row.status !== APPLICATION_STATUS.APPROVED.key" type="text" >
                    退料申请审批中...
                  </el-button>
                  <el-button v-if="scope.row.opeStatus === MATERIAL_OPE_STATUS.RETURN.key && scope.row.status === APPLICATION_STATUS.APPROVED.key" type="primary" plain size="small" v-on:click.stop="materialDealWith(scope.row)" >
                    物料处置
                  </el-button>
                </div>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </div>
    </div>

    <!--打印领料单-->
    <el-dialog
        width="25.0cm"
        :visible.sync="materialApproveDlg"
        :before-close="handleClose"
        :close-on-click-modal=false>
      <div>
        <MaterialApproveInfoDlg
            :key="appItemCode"
            :application-code="appItemCode"
            :onlyShowInfo="true"
            :close-dlg = closeDialog
        ></MaterialApproveInfoDlg>
      </div>
    </el-dialog>

    <!--物料处置-->
    <el-dialog
        width="29.0cm"
        :visible.sync="materialDealWithDlg"
        :before-close="handleClose"
        :close-on-click-modal=false>
      <div slot="title" style="font-size: 20px;">{{ dialogTitle }}</div>
      <div>
        <MaterialDealWithDlg
            :key="appItem.code"
            :app-item="appItem"
            :close-function="closeDialog"
        ></MaterialDealWithDlg>
      </div>
    </el-dialog>

    <!--物料退料申请-->
    <el-dialog
        width="36cm"
        :visible.sync="materialReturnAppDlg"
        :before-close="handleClose"
        :close-on-click-modal=false>
      <div slot="title" style="font-size: 20px;">{{ dialogTitle }}</div>
      <div>
        <MaterialApplicationReturnDlg
            :key="appItem.code"
            :app-item="appItem"
            :close-dlg=closeDialog
        ></MaterialApplicationReturnDlg>
      </div>
    </el-dialog>

    <!--修改后,重新领料-->
    <el-dialog
        width="36cm"
        :visible.sync="materialApplicationDlg"
        :before-close="handleClose"
        :close-on-click-modal=false>
      <div slot="title" style="font-size: 20px;">{{ dialogTitle }}</div>
      <div v-if="materialApplicationDlg">
        <MaterialApplicationDlgSap
            :key="((jobitemDlg.id) + '-' + (random))"
            :jobitem-id="jobitemDlg.id"
            :inherit-job-type="jobitemDlg.auxType"
            :project-type="jobitemDlg.projectType"
            :material-list-in="materialList"
            :application-info="materialAppItem"
            :close-dlg=closeDialog
        ></MaterialApplicationDlgSap>
      </div>
    </el-dialog>

  </div>
</template>

<script>
import TabButton from "@/components/rdms/tab-button";
import File from "@/components/rdms/file";
import Swal from "sweetalert2";
import {dateFormat, fixed2fractional, optionKV, rmbFormat, shortDateFormat, materialStatusLabel} from "@/filter/filter";
import SubprojectTaskTab from "@/components/rdms/subproject-task-tab.vue";
import DevelopCharacterTaskTab from "@/components/rdms/develop-character-task-tab.vue";
import MaterialApproveInfoDlg from "@/components/std/material-approve-info-dlg.vue";
import MaterialDealWithDlg from "@/components/rdms/material-deal-with-dlg.vue";
import MaterialApplicationReturnDlg from "@/components/std/material-application-return-dlg.vue";
import MaterialApplicationDlgSap from "@/components/std/material-application-dlg-sap.vue";

export default {
  components: {
    MaterialApplicationDlgSap,
    MaterialApproveInfoDlg,
    SubprojectTaskTab,
    DevelopCharacterTaskTab,
    File,
    TabButton,
    MaterialDealWithDlg,
    MaterialApplicationReturnDlg,
  },
  name: "material-application-list",

  props: {
    writerId: {
      default: null,
    }
  },

  data: function () {
    return {
      loginUser: {},
      loginCustomer: {},

      materialApplicationList: [],

      activePageId: 'home',

      characterItemList: [],
      characterMaterialItemList: [],

      projectItems_demand: [],

      ENTRANCE_CLASS: ENTRANCE_CLASS,
      JOB_ITEM_TYPE: JOB_ITEM_TYPE,
      PROJECT_TYPE: PROJECT_TYPE,
      MATERIAL_USAGE_MODE: MATERIAL_USAGE_MODE,
      AUX_STATUS: AUX_STATUS,
      APPLICATION_STATUS: APPLICATION_STATUS,
      MATERIAL_OPE_STATUS: MATERIAL_OPE_STATUS,

      materialApproveDlg: false,
      materialDealWithDlg: false,
      materialReturnAppDlg: false,
      dealWithFlag: '',
      appItemCode: '',

      random: 0,
      dialogTitle: '',
      materialApplicationDlg: false,
      jobitemDlg: {},
      materialAppItem: {},
      materialList: [],

      appItem: {},
    }
  },
  mounted: function () {
    let _this = this;
    _this.getLoginUser();
    if(Tool.isNotEmpty(_this.writerId)){
      _this.getMaterialApplicationListByWriterId(_this.writerId);
    }

    _this.$bus.$on('refresh-material-application-list', (data)=>{
      if(data){
        _this.getMaterialApplicationListByWriterId(_this.writerId);
      }
    });
  },

  beforeDestroy: function () {
    let _this = this;
    _this.$bus.$off('refresh-material-application-list');
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
      return materialStatusLabel(row);
    },

    usageModeFormat(row, column) {
      return optionKV(MATERIAL_USAGE_MODE, row.usageMode);
    },

    currencyFormat(row, column) {
      if (row.accountCost) {
        return rmbFormat(row.accountCost);
      }
      return 0;
    },

    rowClick(row, column, e) {
      let _this = this;
      _this.materialApprove(row);
    },

    materialApprove(appItem) {
      let _this = this;
      _this.appItemCode = appItem.code;
      _this.materialApproveDlg = true;
      _this.dialogTitle = "领料审批单";
    },

    materialDealWith(appItem) {
      let _this = this;
      _this.appItem = appItem;
      _this.materialDealWithDlg = true;
      _this.dialogTitle = "物料处置";
    },

    materialReturnApplication(appItem) {
      let _this = this;
      _this.appItem = appItem;
      _this.materialReturnAppDlg = true;
      _this.dialogTitle = "物料退料申请";
    },

    closeDialog() {
      let _this = this;
      _this.materialApproveDlg = false;
      _this.materialDealWithDlg = false;
      _this.materialReturnAppDlg = false;
      _this.materialApplicationDlg = false;
    },
    handleClose(done) {
      done();
    },

    getMaterialApplicationListByWriterId(writerId) {
      let _this = this;
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/material/getMaterialApplicationListByWriterId/' + writerId ).then((response) => {
        let resp = response.data;
        if (resp.success) {
          _this.materialApplicationList = resp.content;
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
          _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/material/confirmCancel/' + item.code).then((response) => {
            let resp = response.data;
            if (resp.success) {
              _this.getMaterialApplicationListByWriterId(_this.writerId);
            } else {
              Toast.warning(resp.message, Swal);
            }
          });
        }
      }).catch(_ => {
      });

    },

    materialModify(materialAppItem){
      let _this = this;
      // console.log('item:', materialAppItem);
      _this.materialAppItem = materialAppItem;
      //根据物料申请单的单号获得物料清单
      if(materialAppItem?.code){
        _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/material/getMaterialListByApplicationCode/' + materialAppItem.code ).then((response) => {
          let resp = response.data;
          if (resp.success) {
            _this.materialList = resp.content;
          } else {
            Toast.warning(resp.message, Swal);
          }
        })
      }

      if(_this.materialAppItem){
        _this.materialApplication(materialAppItem)
      }
    },
    materialApplication(materialAppItem) {
      let _this = this;
      _this.jobitemId = materialAppItem?.jobitemId;
      if(_this.jobitemId){
        _this.getJobitemSimpleInfo(_this.jobitemId)
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
          _this.jobitemDlg = resp.content;
          _this.materialApplicationDlg = true;
          _this.dialogTitle = "领料申请";
          _this.random = Math.random();
        }else{
          Toast.warning(resp.message, Swal);
        }
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
/* 行底色半透明，固定操作列须垫白底，否则横向滚动时透出下层单元格 */
.material-application-table .el-table__fixed-right .el-table__fixed-body-wrapper {
  background: #fff;
}
.el-badge__content.is-dot {
  height: 10px;
  width: 10px;
  padding: 0;
  right: -2px !important;
  border-radius: 50%;
}
</style>
