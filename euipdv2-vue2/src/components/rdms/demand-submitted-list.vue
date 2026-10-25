<!--
 * Author: 周朔鹏
 * Date: 2025-02-23
 * Email: 391902958@qq.com
 * Website: https://www.euipd.com
-->

<template>
  <div>
    <div style="display: flex; justify-content: space-between;">
      <pagination ref="pagination" v-bind:list="list" v-bind:itemCount="6"></pagination>
      <div>
        <el-button v-show="buttonShowFlag === true" type="primary" size="medium" style="margin-right: 40px;" @click="createMulDemandToCharacterJobitem()">多需求归类特性定义</el-button>
      </div>
    </div>
    <el-table
        ref="multipleTable"
        :data="demands"
        tooltip-effect="dark"
        style="width: 100%"
        @row-click="rowClick"
        @selection-change="handleSelectionChange">
      <el-table-column
          type="selection"
          width="55">
      </el-table-column>
      <el-table-column
          prop="demandName"
          label="需求名称">
      </el-table-column>
      <el-table-column
          prop="demandCustomerName"
          label="需求方"
          show-overflow-tooltip
          width="150">
      </el-table-column>
      <el-table-column
          prop="writerName"
          label="填表人"
          align="center"
          width="100">
      </el-table-column>
      <el-table-column
          prop="confirmPersonName"
          label="需求确认人"
          align="center"
          width="100">
      </el-table-column>
      <el-table-column
          prop="confirmContactTel"
          label="需求确认电话"
          align="center"
          width="120">
      </el-table-column>
      <el-table-column
          prop="createTimeStr"
          label="提交时间"
          align="center"
          width="160"
          show-overflow-tooltip>
      </el-table-column>
      <el-table-column
          fixed="right"
          label="操作"
          align="center"
          width="280">
        <template slot-scope="scope">
          <el-button
              size="mini"
              @click.stop="demandDetail(scope.$index, scope.row)">查看详情</el-button>
          <el-button
              size="mini"
              :disabled="buttonShowFlag"
              @click.stop="createDemandToCharacterJobitem(scope.$index, scope.row)">特性定义</el-button>
          <el-button
              size="mini"
              type="warning"
              :disabled="scope.row.status === DEMAND_STATUS.COMPLETED.key || scope.row.status === DEMAND_STATUS.ABANDON.key"
              @click.stop="abandonDemand(scope.$index, scope.row)">废弃</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!--    查看详情-->
    <el-dialog
        :visible.sync="detailDialogVisible"
        width="29cm"
        :before-close="handleClose"
        :close-on-click-modal=false>
      <div slot="title" style="font-size: 20px;">{{ dialogTitle }}</div>
      <demand-detail-dialog-show
          :key="demand.id"
          :demand-item = demand
          :close-function="closeDialog"
      ></demand-detail-dialog-show>
    </el-dialog>

    <!--创建 特性定义 任务-->
    <el-dialog
        width="29cm"
        :visible.sync="characterDialogVisible"
        :before-close="handleClose"
        :close-on-click-modal=false>
      <div slot="title" style="font-size: 20px;">{{ dialogTitle }}</div>
      <div>
        <JobitemCharacterDefineDlg
            :key="('demand-' + (demand.id) + '-' + (timeStamp))"
            :demand-id-list="demandIdList"
            :pre-project-id="preProjectId"
            :project-type="PROJECT_TYPE.PRE_PROJECT.key"
            :entrance="ENTRANCE_CLASS.DEMAND.key"
            :job-type="JOB_ITEM_TYPE.DEMAND.key"
            :close-dlg = handleClose
        ></JobitemCharacterDefineDlg>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import Pagination from "@/components/pagination";
import TabButton from "@/components/rdms/tab-button";
import DemandDetailDialogShow from "@/components/rdms/demand-detail-dlg";
import DemandCreation from "@/components/rdms/demand-create-dlg";
// import CharacterItemCreateDialog from "@/components/rdms/character-create-dialog";
import File from "@/components/rdms/file";
import Swal from "sweetalert2";
import JobitemCharacterDefineDlg from "@/components/std/jobitem-character-define-dlg.vue";

export default {
  components: {
    Pagination,
    File,
    TabButton,
    DemandDetailDialogShow,
    DemandCreation,
    // CharacterItemCreateDialog,
    JobitemCharacterDefineDlg,
  },

  name: "demand-submitted-list",

  props: {
    preProjectId: {
      default: null,
    },
  },

  data: function() {
    return {
      loginUser: {},
      loginCustomer: {},

      activePageId: 'home',
      demand: {},
      demands: [],
      demandList: [], //用于向子组件传递数据
      demandDetailItems: [],

      multipleSelection: [],
      demandIdList: [],

      detailDialogVisible: false,
      characterDialogVisible: false,
      labelPosition: 'right',

      dialogTitle: 'Dialog',
      setupProjectItem: {}, //用于处理立项数据

      buttonShowFlag: false,
      timeStamp: '',

      PROJECT_TYPE: PROJECT_TYPE,
      JOB_ITEM_TYPE: JOB_ITEM_TYPE,
      ENTRANCE_CLASS: ENTRANCE_CLASS,
      DEMAND_STATUS: DEMAND_STATUS,

    }
  },
  mounted: function() {
    let _this = this;
    _this.getLoginUser();
    _this.$refs.pagination.size = 12;
    _this.list(1);

    _this.$bus.$on('refresh-demand-submitted-list', (data) => {
      if(data){
        _this.list(1);
      }
    });
  },

  beforeDestroy(){
    let _this = this;
    _this.$bus.$off('refresh-demand-submitted-list');
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

    handleSelectionChange(val) {
      let _this = this;
      _this.multipleSelection = val;
      if(_this.multipleSelection.length>1){
        _this.buttonShowFlag = true;
      }else{
        _this.buttonShowFlag = false;
      }
    },

    rowClick(row, column, e){
      let _this = this;
      _this.demandDetail(0, row);
    },

    createMulDemandToCharacterJobitem(){
      let _this = this;
      _this.dialogTitle="创建 特性定义 任务";
      _this.timeStamp = new Date().getTime();
      _this.detailDialogVisible = false;
      _this.characterDialogVisible = true;
      _this.demandIdList = [];
      _this.multipleSelection.forEach(item => {
        _this.demandIdList.push(item.id);
      });
    },

    createDemandToCharacterJobitem(index, demand) {
      let _this = this;
      _this.dialogTitle="创建 特性定义 任务";
      _this.timeStamp = new Date().getTime();
      _this.detailDialogVisible = false;
      _this.characterDialogVisible = true;
      _this.demandIdList = [];
      _this.demandIdList.push(demand.id);

    },

    demandDetail(index, demand){
      let _this = this;
      _this.dialogTitle="查看需求详情";
      _this.characterDialogVisible = false;
      _this.detailDialogVisible = true;
      _this.demand = demand;
    },

    handleClose() {
      let _this = this;
      _this.detailDialogVisible = false;
      _this.characterDialogVisible = false;
    },

    closeDialog() {
      let _this = this;
      _this.detailDialogVisible = false;
      _this.characterDialogVisible = false;
      _this.list(1);
    },

    abandonDemand(index, demand){
      let _this = this;
      _this.abandon(demand);
    },

    list(page) {
      let _this = this;
      // Loading.show();
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/demand/list-submitted', {
        page: page,
        size: _this.$refs.pagination.size,
        keyWord: _this.preProjectId, //预立项项目id
        actor: _this.loginUser.customerUserId,
      }).then((response)=>{
        // Loading.hide();
        let resp = response.data;
        if (resp.success) {
          _this.demands = resp.content.list;
          _this.$refs.pagination.render(page, resp.content.total);
          _this.$bus.$emit('refresh-demand-submitted-list', false);
        } else {
          Toast.warning(resp.message,Swal);
          // Loading.hide();
        }
      })
    },

    abandon(demand) {
      let _this = this;
      let text = `是否确认废弃"${ demand.demandName }"？废弃后不可挂接特性。`;
      Swal.fire({
        title: '确认？',
        text: text,
        icon: 'warning',
        input: 'textarea',
        inputPlaceholder: '请填写废弃原因（500 字以内），将写入需求过程记录',
        inputAttributes: { maxlength: 500 },
        inputValidator: (value) => {
          if (!value || !value.trim()) {
            return '请填写废弃原因';
          }
          return null;
        },
        showCancelButton: true,
        confirmButtonColor: '#409EFF',
        cancelButtonColor: '#909399',
        confirmButtonText: '确认!',
        cancelButtonText: '取消!'
      }).then((result) => {
        if (result.isConfirmed) {
          _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/demand/abandon', {
            id: demand.id,
            abandonReason: result.value.trim(),
          }).then((response)=>{
            let resp = response.data;
            if (resp.success) {
              Toast.success(resp.message || "废弃成功!", Swal);
              _this.list(1);
            } else {
              Toast.warning(resp.message, Swal);
            }
          })
        }
      })
    },
  }
}
</script>
