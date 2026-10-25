<!--
 * Author: 周朔鹏
 * Date: 2025-04-25
 * Email: 391902958@qq.com
 * Website: https://www.euipd.com
-->

<template>
  <div>
    <!--Tab 卡片-->
    <div class="row">
      <div class="col-md-12">
        <div class="tabbable">
          <ul class="nav nav-tabs" id="myTab">
            <li :class="{active : activePageId==='home'}">
              <a @click="tabHomeClick">
                <i class="green ace-icon fa fa-home bigger-120"></i>
                特性物料
              </a>
            </li>
          </ul>

          <div class="tab-content">
            <div id="home" class="tab-pane fade"
                 :class="{active : activePageId === 'home', in : activePageId === 'home' }">
              <div style="display: flex; flex-direction: row; justify-content: flex-start; align-items: center;">
<!--                <el-input style="width: 300px;" v-model="search" size="mini" placeholder="输入ID或名称关键词进行搜索"/>-->
                <p style="margin-left: 5px;">物料管理: 物料管理是给<strong>项目经理</strong>和<strong>物料管理员</strong>提供的物料综合管理页面. 审批通过的领料单由物料管理员发料确认后, 申请人才能处置并提交; 审批通过的退料单由物料管理员收料确认入库后, 退料人才能提交复核.</p>
              </div>
              <div class="hr hr-10 dotted hr-double"></div>
              <div class="tab-content-height">
                <el-table
                    v-loading="tableLoading"
                    class="material-manage-table"
                    :data="pagedMaterialList"
                    border
                    highlight-current-row
                    style="width: 100%"
                    :row-class-name="tableRowClassName"
                    @row-click="rowClick">
                  <el-table-column  label="序号" align="center" width="50">
                    <template slot-scope="scope">
                      {{scope.$index+1}}
                    </template>
                  </el-table-column>
                  <el-table-column
                      prop="code"
                      width="170"
                      align="center"
                      label="申请单号"
                  ></el-table-column>
                  <el-table-column
                      prop="name"
                      align="center"
                      show-overflow-tooltip
                      label="申请标题"
                  ></el-table-column>
                  <el-table-column
                      prop="accountCost"
                      :formatter="currencyFormat"
                      align="center"
                      label="记账金额"
                      width="120"
                  ></el-table-column>
                  <el-table-column
                      prop="jobitemName"
                      show-overflow-tooltip
                      align="center"
                      label="任务名称"
                  ></el-table-column>
                  <el-table-column
                      prop="subprojectName"
                      show-overflow-tooltip
                      align="center"
                      label="子/项目名称"
                  ></el-table-column>
                  <el-table-column
                      prop="usageMode"
                      :formatter="usageModeFormat"
                      align="center"
                      label="使用方式"
                      width="100"
                  ></el-table-column>
                  <el-table-column
                      prop="status"
                      :formatter="statusFormat"
                      align="center"
                      label="审批状态"
                      width="100"
                  ></el-table-column>
                  <el-table-column
                      label="发料 / 收料"
                      align="center"
                      width="130">
                    <template slot-scope="scope">
                      <el-tag v-if="scope.row.pendingIssue" type="warning" size="small">{{ isReturnSlip(scope.row) ? '待收料' : '待发料' }}</el-tag>
                      <div v-else-if="scope.row.issueStatus === 'ISSUED'" style="line-height: 18px;">
                        {{ scope.row.issuerName }}<br/>{{ issueTimeFormat(scope.row) }}
                      </div>
                    </template>
                  </el-table-column>
                  <el-table-column
                      prop="opeStatus"
                      width="110"
                      :formatter="opeStatusFormat"
                      label="退料状态"
                      align="center"
                  ></el-table-column>
                  <el-table-column
                      prop="writerName"
                      align="center"
                      label="申请人"
                      width="100"
                  ></el-table-column>
                  <el-table-column
                      prop="createTime"
                      :formatter="dateFormat"
                      align="center"
                      label="申请时间"
                      width="100"
                  ></el-table-column>
                  <el-table-column
                      fixed="right"
                      label="操作"
                      align="center"
                      width="230">
                    <template slot-scope="scope">
                      <div>
                        <el-tooltip content="对相关物料进行管理" placement="top" effect="light"
                                    :open-delay=1000>
                          <el-button type="primary" plain size="small" v-on:click.stop="materialApprove(scope.row)">
                            <el-badge :is-dot="false">
                              详情 / 审批
                            </el-badge>
                          </el-button>
                        </el-tooltip>
                        <el-button v-if="canIssue(scope.row) && !isReturnSlip(scope.row)" type="warning" plain size="small"
                                   v-on:click.stop="materialIssue(scope.row)">发料确认</el-button>
                        <el-button v-if="canIssue(scope.row) && isReturnSlip(scope.row)" type="warning" plain size="small"
                                   v-on:click.stop="materialReceive(scope.row)">收料确认</el-button>
                      </div>
                    </template>
                  </el-table-column>
                </el-table>
                <div style="margin-top: 12px; text-align: right;">
                  <el-pagination
                      background
                      layout="total, prev, pager, next, sizes"
                      :total="materialManagerList.length"
                      :page-size="tablePageSize"
                      :current-page.sync="tablePage"
                      :page-sizes="[10, 20, 50, 100]"
                      @size-change="onTablePageSizeChange"
                  />
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
    <!--物料审批-->
    <el-dialog
        width="33cm"
        :visible.sync="materialApproveDlg"
        :before-close="handleClose"
        :close-on-click-modal=false>
      <div slot="title" style="font-size: 20px;">{{ dialogTitle }}</div>
      <div v-if="materialApproveDlg">
        <MaterialApproveDlgSap
            :key="((appItem.code) + '-' + (random))"
            :application-code="appItem.code"
            :job-type="JOB_ITEM_TYPE.TASK_FUNCTION.key"
            :doc-type="DOC_TYPE.PRE_PROJECT.key"
            :ope-status="appItem.opeStatus"
            :close-dlg = closeDialog
        ></MaterialApproveDlgSap>
      </div>
    </el-dialog>
    <!--发料确认-->
    <el-dialog
        width="28cm"
        :visible.sync="materialIssueDlg"
        :before-close="handleClose"
        :close-on-click-modal=false>
      <div slot="title" style="font-size: 20px;">发料确认</div>
      <div v-if="materialIssueDlg">
        <MaterialIssueDlg
            :key="((appItem.code) + '-issue-' + (random))"
            :application-code="appItem.code"
            :close-dlg="closeIssueDialog"
        ></MaterialIssueDlg>
      </div>
    </el-dialog>
    <!--退料收料确认-->
    <el-dialog
        width="28cm"
        :visible.sync="materialReceiveDlg"
        :before-close="handleClose"
        :close-on-click-modal=false>
      <div slot="title" style="font-size: 20px;">退料收料确认</div>
      <div v-if="materialReceiveDlg">
        <MaterialReceiveDlg
            :key="((appItem.code) + '-receive-' + (random))"
            :application-code="appItem.code"
            :close-dlg="closeReceiveDialog"
        ></MaterialReceiveDlg>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import TabButton from "@/components/rdms/tab-button";
import Swal from "sweetalert2";
import {isReturnSlip, optionKV, rmbFormat, shortDateFormat} from "@/filter/filter";
import BomManageTabs from "@/components/std/bom-manage-tabs.vue";
import MaterialApproveDlgSap from "@/components/std/material-approve-dlg-sap.vue";
import MaterialIssueDlg from "@/components/std/material-issue-dlg.vue";
import MaterialReceiveDlg from "@/components/std/material-receive-dlg.vue";

export default {
  components: {
    MaterialIssueDlg,
    MaterialReceiveDlg,
    MaterialApproveDlgSap,
    TabButton,
    BomManageTabs,
  },
  name: "material",
  data: function () {
    return {
      loginUser: {},
      loginCustomer: {},
      materialManagerList: [],
      materialApproveDlg: false,
      materialIssueDlg: false,
      materialReceiveDlg: false,
      dialogTitle: '',
      appItem: {},
      random: 0,



      characterMaterialInfoList: [],

      characterMaterialItemList: [],
      characterMaterialCheckList: [],

      PROJECT_TYPE: PROJECT_TYPE,
      ENTRANCE_CLASS: ENTRANCE_CLASS,
      JOB_ITEM_TYPE: JOB_ITEM_TYPE,
      DOC_TYPE: DOC_TYPE,

      activePageId: 'home',
      search: '',
      characterId: '',
      tableLoading: false,
      tablePage: 1,
      tablePageSize: 20,
    }
  },

  computed: {
    pagedMaterialList() {
      const start = (this.tablePage - 1) * this.tablePageSize;
      return (this.materialManagerList || []).slice(start, start + this.tablePageSize);
    }
  },

  created(){
    //获取路由参数
    let _this = this;
    _this.characterId = this.$route.query.characterId;
    _this.search = this.$route.query.characterId;
  },

  mounted: function () {
    let _this = this;
    _this.getLoginUser();

    _this.unsubscribeMaterial = _this.$bus.subscribe(['material'], function () {
      _this.getListByNextNodeIdForApprover(_this.loginUser.customerUserId);
    });

  },

  beforeDestroy: function () {
    let _this = this;
    if (_this.unsubscribeMaterial) {
      _this.unsubscribeMaterial();
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

      _this.getListByNextNodeIdForApprover(_this.loginUser.customerUserId);
    },

    tableRowClassName({row, rowIndex}) {
      if (row.status === APPLICATION_STATUS.APPLICATION.key || row.status === APPLICATION_STATUS.SUBMIT.key ) {
        return 'application-row';
      } else if (row.status === APPLICATION_STATUS.APPROVED.key) {
        return 'approve-row';
      } else if (row.status === APPLICATION_STATUS.REJECT.key) {
        return 'reject-row';
      }
      return '';
    },

    opeStatusFormat(row, column) {
      return optionKV(MATERIAL_OPE_STATUS, row.opeStatus);
    },
    dateFormat(row, column) {
      return shortDateFormat(row.createTime);
    },
    usageModeFormat(row, column) {
      return optionKV(MATERIAL_USAGE_MODE, row.usageMode);
    },
    statusFormat(row, column) {
      return optionKV(APPLICATION_STATUS, row.status);
    },
    currencyFormat(row, column) {
      if (row.accountCost) {
        return rmbFormat(row.accountCost);
      }
      return 0;
    },

    materialApprove(application) {
      let _this = this;
      _this.appItem = application;
      _this.random = Math.random();
      _this.materialApproveDlg = true;
      if(_this.appItem.opeStatus === MATERIAL_OPE_STATUS.RETURN_APPLICATION.key
          || _this.appItem.opeStatus === MATERIAL_OPE_STATUS.RETURN_APPROVED.key
          || _this.appItem.opeStatus === MATERIAL_OPE_STATUS.RETURN_COMPLETE.key){
        _this.dialogTitle = "物料退料审批";
      }else{
        _this.dialogTitle = "领料审批";
      }
    },
    closeDialog() {
      let _this = this;
      _this.materialApproveDlg = false;
    },
    canIssue(row) {
      if (!row.pendingIssue) {
        return false;
      }
      return row.nextNode === this.loginUser.customerUserId
          || Tool.hasAnyRole(['MATM', 'UltiMng']);
    },
    materialIssue(row) {
      this.appItem = row;
      this.random = Math.random();
      this.materialIssueDlg = true;
    },
    isReturnSlip(row) {
      return isReturnSlip(row);
    },
    materialReceive(row) {
      this.appItem = row;
      this.random = Math.random();
      this.materialReceiveDlg = true;
    },
    closeReceiveDialog() {
      this.materialReceiveDlg = false;
      this.getListByNextNodeIdForApprover(this.loginUser.customerUserId);
    },
    closeIssueDialog() {
      this.materialIssueDlg = false;
      this.getListByNextNodeIdForApprover(this.loginUser.customerUserId);
    },
    issueTimeFormat(row) {
      return row.issueTime ? shortDateFormat(row.issueTime) : '';
    },
    handleClose(done) {
      done();
    },

    rowClick(row, column, e){
      let _this = this;
      _this.materialApprove(row);
    },

    tabHomeClick() {
      let _this = this;
      _this.activePageId = 'home';
      _this.getListByNextNodeIdForApprover(_this.loginUser.customerUserId);
    },

    onTablePageSizeChange(size) {
      this.tablePageSize = size;
      this.tablePage = 1;
    },

    getListByNextNodeIdForApprover(loginUser) {
      let _this = this;
      _this.tableLoading = true;
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/material/getListByNextNodeIdForApprover/' + loginUser ).then((response) => {
        let resp = response.data;
        if (resp.success) {
          _this.materialManagerList = resp.content || [];
          _this.tablePage = 1;
        } else {
          Toast.warning(resp.message, Swal);
        }
      }).finally(() => {
        _this.tableLoading = false;
      })
    },

  }
}
</script>

<style lang="scss">
@import "@/../public/static/scss/config.scss";

.tab-content-height{
  height: calc(100vh - 255px);
  overflow: scroll;
}
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
.material-manage-table .el-table__fixed-right .el-table__fixed-body-wrapper {
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
