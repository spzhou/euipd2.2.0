<!--
 * Author: 周朔鹏
 * Date: 2025-02-26
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
                需求列表
              </a>
            </li>
          </ul>

          <div class="tab-content">
            <div id="home" class="tab-pane fade"
                 :class="{active : activePageId === 'home', in : activePageId === 'home' }">
              <p>需求识别: 是为了广泛的收集和识别需求提供的一套工作页面.</p>
              <div class="hr hr-18 dotted hr-double"></div>
              <div>
                <div style="display: flex; justify-content: space-between;">
                  <pagination ref="pagination" v-bind:list="list" v-bind:itemCount="6"></pagination>
                  <div>
                    <el-tooltip content="对客户需求/痛点和抱怨进行识别和反馈" placement="top" effect="light" :open-delay=1000>
                      <el-button type="primary" size="small" style="margin-right: 15px;" @click="createDemand()">新建需求</el-button>
                    </el-tooltip>
                  </div>
                </div>
                <el-table
                    :data="demands"
                    border
                    stripe
                    highlight-current-row
                    style="width: 100%"
                    @row-click="rowClick">
                  <el-table-column  label="序号" align="center" width="50">
                    <template slot-scope="scope">
                      {{scope.$index+1}}
                    </template>
                  </el-table-column>
                  <el-table-column
                      prop="demandName"
                      show-overflow-tooltip
                      label="需求名称"
                  ></el-table-column>
                  <el-table-column
                      prop="preProjectName"
                      width="130"
                      show-overflow-tooltip
                      label="关联预立项项目"
                  ></el-table-column>
                  <el-table-column
                      prop="demandCustomerName"
                      show-overflow-tooltip
                      width="130"
                      label="需求方"
                  ></el-table-column>
                  <el-table-column
                      prop="writerName"
                      width="80"
                      label="填表人"
                  ></el-table-column>
                  <el-table-column
                      prop="confirmPersonName"
                      width="80"
                      label="确认人"
                  ></el-table-column>
                  <el-table-column
                      prop="confirmContactTel"
                      width="110"
                      label="需求确认电话"
                  ></el-table-column>
                  <el-table-column
                      prop="createTimeStr"
                      width="160"
                      label="提交时间"
                  ></el-table-column>
                  <el-table-column
                      fixed="right"
                      label="操作"
                      align="center"
                      width="300">
                    <template slot-scope="scope">
                      <el-tooltip content="查看客户需求详细信息" placement="top" effect="light" :open-delay=1000>
                        <el-button v-on:click.stop="demandDetailDlg(scope.row)" type="primary" plain size="small">
                          详情
                        </el-button>
                      </el-tooltip>
                      <el-tooltip content="对已经编制并保存的需求信息进行修订" placement="top" effect="light" :open-delay=1000>
                        <el-button v-on:click.stop="demandModify(scope.row)" type="primary" plain size="small">
                          修改
                        </el-button>
                      </el-tooltip>
                      <el-tooltip content="将编辑好的客户需求提交给产品经理" placement="top" effect="light" :open-delay=1000>
                        <el-button v-on:click.stop="submit(scope.row)" type="primary" plain size="small">
                          提交
                        </el-button>
                      </el-tooltip>
                      <el-tooltip content="删除已经编制保存的客户需求" placement="top" effect="light" :open-delay=1000>
                        <el-button v-on:click.stop="deleteDemand(scope.row)" type="warning" plain size="small">
                          删除
                        </el-button>
                      </el-tooltip>
                    </template>
                  </el-table-column>
                </el-table>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!--    创建需求-->
    <el-dialog
        :visible.sync="dialogVisible"
        width="29cm"
        :before-close="handleClose"
        :close-on-click-modal=false>
      <div slot="title" style="font-size: 20px;">{{ dialogTitle }}</div>
      <div v-if="dialogVisible">
        <demand-create-dlg
            :key="timestamp"
            :close-function="closeDialog">
        </demand-create-dlg>
      </div>
    </el-dialog>

    <!--    需求详情-->
    <el-dialog
        :visible.sync="detailDialogVisible"
        width="29cm"
        :before-close="handleClose"
        :close-on-click-modal=false>
      <div slot="title" style="font-size: 20px;">{{ dialogTitle }}</div>
      <div v-if="detailDialogVisible">
        <demand-detail-dlg
            :key="((demand.id) + '-' + (random))"
            :demand-item=demand
            :close-function="closeDialog">
        </demand-detail-dlg>
      </div>
    </el-dialog>

    <!--    修改需求-->
    <el-dialog
        :visible.sync="modifyDialogVisible"
        width="29cm"
        :before-close="handleClose"
        :close-on-click-modal=false>
      <div slot="title" style="font-size: 20px;">{{ dialogTitle }}</div>
      <div v-if="modifyDialogVisible">
        <demand-modify-dlg
            :key="demand.id"
            :demand-item=demand
            :close-function="closeDialog">
        </demand-modify-dlg>
      </div>
    </el-dialog>

  </div>
</template>

<script>
import Pagination from "@/components/pagination";
import TabButton from "@/components/rdms/tab-button";
import DemandDetail from "@/components/rdms/demand-detail";
import DemandCreateDlg from "@/components/rdms/demand-create-dlg";
import DemandModifyDlg from "@/components/rdms/demand-modify-dlg";
import DemandDetailDlg from "@/components/rdms/demand-detail-dlg";

import File from "@/components/rdms/file";
import Swal from "sweetalert2";

export default {
  components: {
    Pagination,
    File,
    TabButton,
    DemandDetail,
    DemandCreateDlg,
    DemandModifyDlg,
    DemandDetailDlg,
  },
  name: "demand",
  data: function () {
    return {
      loginUser: {},
      loginCustomer: {},
      random: 0,

      activePageId: 'home',
      demand: {},
      demands: [],
      demandDetailItems: [],

      dialogVisible: false,
      modifyDialogVisible: false,
      detailDialogVisible: false,

      dialogTitle: 'Dialog',
      setupProjectItem: {}, //用于处理立项数据

      timestamp: '',
    }
  },
  mounted: function () {
    let _this = this;
    _this.getLoginUser();
    _this.$refs.pagination.size = 12;
    _this.list(1);

    _this.unsubscribeDemand = _this.$bus.subscribe(['demand'], function () {
      _this.list(1);
    });
  },

  beforeDestroy(){
    let _this = this;
    if (_this.unsubscribeDemand) {
      _this.unsubscribeDemand();
    }
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

    rowClick(row, column, e){
      let _this = this;
      _this.demandDetailDlg(row);
    },

    tabHomeClick() {
      let _this = this;
      _this.activePageId = 'home';
      _this.list(1);
    },

    createDemand() {
      let _this = this;
      _this.timestamp = new Date().getTime();
      _this.dialogVisible = true;
      _this.dialogTitle = "添加客户需求/痛点描述";
    },

    demandModify(demand) {
      let _this = this;
      _this.modifyDialogVisible = true;
      _this.dialogTitle = "修订客户需求/痛点描述";
      _this.demand = demand;
    },

    demandDetailDlg(demand) {
      let _this = this;
      _this.random = Math.random();
      _this.detailDialogVisible = true;
      _this.dialogTitle = "需求详情";
      _this.demand = demand;
    },

    handleClose(done) {
      done(); //el-dialog用于关闭对话框的内置函数.
      this.list(1);
    },

    /**
     * 提交需求
     */
    submit(demand) {
      let _this = this;
      let text = `是否将需求提交给产品经理 ？`;
      Swal.fire({
        title: '确认？',
        text: text,
        icon: 'warning',
        position: 'center',
        showCancelButton: true,
        confirmButtonColor: '#409EFF',
        cancelButtonColor: '#909399',
        confirmButtonText: '确认!',
        cancelButtonText: '取消!'
      }).then((result) => {
        if (result.value) {
          _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/demand/submit/' + demand.id).then((response) => {
            let resp = response.data;
            if (resp.success) {
              Toast.success("保存成功", Swal);
              _this.list(1);
            } else {
              Toast.warning(resp.message, Swal);
            }
          })
        }
      })
    },

    deleteDemand(demand) {
      let _this = this;
      _this.delete(demand);
    },

    list(page) {
      let _this = this;
      // Loading.show();
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/demand/list', {
        page: page,
        size: _this.$refs.pagination.size,
        keyWord: _this.loginCustomer.id, //customerId
        actor: _this.loginUser.customerUserId,
      }).then((response) => {
        // Loading.hide();
        let resp = response.data;
        if (resp.success) {
          _this.demands = resp.content.list;
          _this.$refs.pagination.render(page, resp.content.total);
          _this.$bus.$emit('refresh-demand-list', false);
        } else {
          Toast.warning(resp.message, Swal);
          // Loading.hide();
        }
      })
    },

    delete(demand) {
      let _this = this;
      if (demand.status && demand.status !== DEMAND_STATUS.SAVED.key) {
        Toast.warning("输入错误", Swal);
        return;
      }
      let text = `是否确认删除"${demand.demandName}"？`;
      Swal.fire({
        title: '确认？',
        text: text,
        icon: 'warning',
        showCancelButton: true,
        confirmButtonColor: '#409EFF',
        cancelButtonColor: '#909399',
        confirmButtonText: '确认!',
        cancelButtonText: '取消!'
      }).then((result) => {
        if (result.value) {
          _this.$ajax.delete(process.env.VUE_APP_SERVER + '/customer/demand/del/' + demand.id).then((response) => {
            let resp = response.data;
            if (resp.success) {
              Toast.success("删除成功!", Swal);
              _this.list(1);
            } else {
              Toast.warning(resp.message, Swal);
            }
          })
        }
      })
    },

    closeDialog() {
      let _this = this;
      _this.dialogVisible = false;
      _this.detailDialogVisible = false;
      _this.modifyDialogVisible = false;
      _this.list(1);
    },
  }
}
</script>
