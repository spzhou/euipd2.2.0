<!--
 * Author: 周朔鹏
 * Date: 2025-02-23
 * Email: 391902958@qq.com
 * Website: https://www.euipd.com
-->

<template>
  <div style="margin-top: -20px;">
    <div style="height: 100%; overflow-y: scroll;">
      <div class="paper-big-plain">
        <div style="margin: 40px 30px;">
          <el-form  label-width="150px" size="small">
            <el-form-item
                required
                label="目标项目名称:">
              <el-select
                  v-model="setupedProjectId"
                  filterable
                  @change="onSelectChange(setupedProjectId)"
                  :placeholder="((hasProject))"
                  style="width: 300px;">
                <el-option
                    v-for="item in projectList"
                    :key="item.id"
                    :label="item.projectName"
                    :value="item.id"
                ></el-option>
              </el-select>
            </el-form-item>
          </el-form>
          <div class="hr hr-18 dotted hr-double"></div>
          <div style="margin-left: 2px; font-size: 15px; margin-bottom: 15px;">特性清单</div>
          <div>
            <div style="width: 100%">
              <el-table
                  ref="characterTable"
                  :data="characterList"
                  border
                  stripe
                  style="width: 100%"
                  highlight-current-row
                  :summary-method="getSummaries"
                  show-summary
                  @selection-change="handleSelectionChange"
                  @row-click="tableRowClick"
                  row-key="id">
                <el-table-column
                    type="selection"
                    align="center"
                    width="55">
                </el-table-column>
                <el-table-column
                    prop="characterName"
                    show-overflow-tooltip
                    label="特性名称">
                </el-table-column>
                <el-table-column
                    prop="devManhourApproved"
                    label="审批开发工时"
                    align="center"
                    width="100">
                </el-table-column>
                <el-table-column
                    prop="testManhourApproved"
                    label="审批测试工时"
                    align="center"
                    width="100">
                </el-table-column>
                <el-table-column
                    prop="materialFeeApproved"
                    label="审批物料费"
                    align="center"
                    width="90">
                </el-table-column>
                <el-table-column
                    prop="sumOtherFee"
                    label="审批其他杂费"
                    align="center"
                    width="100">
                </el-table-column>
                <el-table-column
                    label="里程碑"
                    align="center"
                    width="160">
                  <template slot-scope="scope">
                    <el-select
                        v-model="scope.row.milestoneId"
                        filterable
                        placeholder="选择里程碑">
                      <el-option
                          v-for="item in setupProject.milestoneList"
                          :key="item.id"
                          :label="item.milestoneName"
                          :value="item.id">
                      </el-option>
                    </el-select>
                  </template>
                </el-table-column>
                <el-table-column
                    fixed="right"
                    label="操作"
                    width="150"
                    align="center">
                  <template slot-scope="scope">
                    <el-tooltip content="对产品经理提报的预算进行复核或修改" placement="top" effect="light" :open-delay=1000>
                      <el-button
                          size="mini"
                          @click.stop="confirmManhour(scope.row)">
                        预算核定
                      </el-button>
                    </el-tooltip>
                  </template>
                </el-table-column>
              </el-table>
            </div>
          </div>
        </div>
      </div>
      <div class="clearfix form-actions">
        <div style="display: flex; justify-content: center;">
          <button v-on:click="append()" class="ca-btn-primary" type="button">
            追加
          </button>
          <button v-on:click="close()" class="ca-btn-primary" type="button">
            关闭
          </button>
        </div>
      </div>
    </div>
    <!--    预算核定-->
    <el-dialog
        :visible.sync="dialogVisible"
        append-to-body
        width="29cm"
        :before-close="handleClose"
        :close-on-click-modal=false>
      <div slot="title" style="font-size: 20px;">{{ ((dialogTitle) + ' : ' + (characterItem.characterName)) }}</div>
      <character-budget-confirm
          :key="characterItem.id"
          :character-item="characterItem"
          :close-function="closeDialog"
      ></character-budget-confirm>
    </el-dialog>

    <!--特性定义表-->
    <el-dialog
        title="特性定义表"
        width="29cm"
        :visible.sync="characterInfoVisible"
        append-to-body
        :before-close="handleClose"
        :close-on-click-modal=true>
      <div>
        <CharacterDetailInfoCoreAll
            :id="'character-detail-show-all-append'"
            :key="characterShow.id"
            :character-id="characterShow.id"
        ></CharacterDetailInfoCoreAll>
      </div>
      <div style="width: 100%; height: 50px;"></div>
    </el-dialog>
  </div>
</template>
<script>
import Swal from "sweetalert2";
import CharacterBudgetConfirm from "@/components/rdms/character-budget-confirm-dlg";
import CharacterDetailInfoCoreAll from "@/components/std/character-detail-info-core-all.vue";
import projectMilestone from "@/components/rdms/project-milestone";
import moment from 'moment';
import {rmbFormat} from "@/filter/filter";

export default {
  components:{
    CharacterBudgetConfirm,
    CharacterDetailInfoCoreAll,
    projectMilestone,
  },

  name: 'project-append',
  props:{
    project: {
      default: null,
    },
    index: {
      default: null,
    },
    closeFunction: {
      type: Function,
      default: null
    },
  },

  data() {
    return {
      loginUser: {},
      loginCustomer: {},

      projectList: [], //已立项项目列表
      setupedProjectId: '',

      value: [],

      characterList: [],
      setupProject: {},

      multipleSelection: [],
      standardManhour: {},
      dialogVisible: false,
      characterItem: {},
      dialogTitle: '',
      budget: 0,

      characterShow: {},
      characterInfoVisible: false,

      originalReleaseTime: '',
      releaseTimeShowFlag: false,

      DOC_TYPE: DOC_TYPE,
      hasProject: '',

      formLabelWidth: '120px',
      developFee: 0,
      addCharge: 0,
      chargeRate: 0,
      sumBudget: 0,
      projectInfo: {},
    };
  },

  mounted: function() {
    let _this =this;
    _this.getLoginUser();
    _this.setupProject = _this.project;
    _this.listSetupedProjects();

    _this.getCharacterListByProjectId(_this.project.id);
    _this.getStandardManhour();

    _this.$bus.$on('refresh-project-append-character-list', (data)=>{
      if(data){
        _this.getCharacterListByProjectId(_this.project.id);
      }
    });
  },
  computed: {
  },

  methods:{
    onSelectChange(setupedProjectId){
      //查出已经立项的项目的里程碑
      let _this = this;
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/milestone/listByProjectId/' + setupedProjectId).then((response) => {
        let resp = response.data;
        if (resp.success) {
          _this.setupProject.milestoneList = resp.content;
          _this.getProjectSimpleInfo(setupedProjectId);
        } else {
          Toast.warning(resp.message, Swal);
        }
      })
    },

    getProjectSimpleInfo(projectId){
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

    listSetupedProjects() {
      let _this = this;
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/project/list-setuped-projects/' + _this.loginCustomer.id).then((response) => {
        let resp = response.data;
        if (resp.success) {
          _this.projectList = resp.content;
          if(Tool.isNotEmpty_ES5(_this.projectList) && _this.projectList.length > 0){
            _this.hasProject = '请选择已立项项目';
          }else{
            _this.hasProject = '尚无可追加项目,请先立项操作';
          }
        } else {
          Toast.warning(resp.message, Swal);
        }
      })
    },

    getTableData(){
      //使用nextTick方法
      this.$nextTick(() => {
        this.characterList.forEach((item) => {
          this.$refs.characterTable.toggleRowSelection(item);
        });
      });
    },

    tableRowClick(row, column, event){
      // debugger
      let _this = this;
      console.log("tableRowClick-row:", row);
      _this.characterShow = row;
      _this.characterInfoVisible = true;
    },

    /**
     * 工时核定
     */
    confirmManhour(row){
      let _this = this;
      _this.dialogVisible = true;
      _this.dialogTitle = "预算核定";
      _this.characterItem = row;
    },

    //统计表格的汇总信息
    getSummaries(param) {
      let _this = this;
      const {columns, data} = param;
      // debugger
      const sums = [];
      const sums_number = [];
      columns.forEach((column, index) => {
        if (index === 0 ) {
          sums[index] = '总计';
          return;
        }
        if (index === 6 || index === 7) {
          sums[index] = '';
          return;
        }
        // const values = data.map(item => Number(item[column.property]));  //关键程序行
        const values = _this.multipleSelection.map(item => Number(item[column.property]));  //关键程序行
        // const values = data.map(item => item[column.property]);

        if (!values.every(value => isNaN(value))) {
          sums[index] = values.reduce((prev, curr) => {
            const value = Number(curr);
            if (!isNaN(value)) {
              return prev + curr;
            } else {
              return prev;
            }
          }, 0);
          sums_number[index] = sums[index];
          if(index >= 4 && index <= 5){
            sums[index] += ' 元';
          }else{
            sums[index] += ' 小时';
          }
        } else {
          sums[index] = 'N/A';
        }
      });
      //计算总工时预算金额
      sums[1] = sums_number[2]* _this.standardManhour.devManhourFee
          + sums_number[3] * _this.standardManhour.testManhourFee
          + sums_number[4] + sums_number[5];
      _this.budget = sums[1];
      sums[1] = rmbFormat(sums[1]);
      return sums;
    },

    handleSelectionChange(val) {
      let _this = this;
      _this.multipleSelection = val;

      _this.developFee = 0;
      _this.multipleSelection.forEach(item => {
        _this.developFee += item.budget;
      })
    },

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

    getCharacterListByProjectId(projectId) {
      let _this = this;
      let formData = new window.FormData();
      formData.append('projectId', projectId);
      formData.append('loginUserId', _this.loginUser.customerUserId);
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/character/getSetupListByProjectId', formData).then((response) => {
        let resp = response.data;
        if (resp.success) {
          _this.characterList = resp.content;
          _this.getTableData();
        } else {
          Toast.warning(resp.message, Swal);
        }
      })
    },

    getStandardManhour(){
      let _this = this;
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/manhour/get-standard/' + _this.loginCustomer.id).then((response) => {
        let resp = response.data;
        if (resp.success) {
          _this.standardManhour = resp.content;
        } else {
          Toast.warning(resp.message, Swal);
        }
      })
    },

    /**
     * 追加
     */
    append() {
      let _this = this;
      _this.setupProject.characterList = _this.multipleSelection; //立项仅包含选中的部分

      //特性清单不能为空
      if(_this.setupProject.characterList === null || ! _this.setupProject.characterList > 0){
        Toast.warning("特性清单不能为空", Swal);
        return;
      }

      //特性清单中都必须选择里程碑
      let flag = false;
      _this.setupProject.characterList.forEach(item => {
        if(item.milestoneId === null){
          if(!flag){
            flag = true;
          }
        }
      })
      if(flag){
        Swal.fire({
          title: '请选择里程碑',
          text: '全部特性开发都必须指定里程碑,请正确选择!',
          showCancelButton: false,
          confirmButtonColor: '#409EFF',
          cancelButtonColor: '#909399',
          confirmButtonText: '确认!',
          cancelButtonText: '取消!'
        }).then((result) => {
        })
        return;
      }

      _this.setupProject.budget = _this.budget;
      if(! _this.setupProject.budget > 0){
        Toast.warning("预算不能为零", Swal);
        return;
      }
      _this.setupProject.customerId = _this.loginCustomer.id;
      _this.setupProject.targetProjectId = _this.setupedProjectId;
      _this.setupProject.loginUserId = _this.loginUser.customerUserId;

      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/project/append', _this.setupProject).then((response)=>{
        let resp = response.data;
        if (resp.success) {
          _this.$bus.$emit('notify-setup-count-flag', true);   //立项管理
          Toast.success("保存成功", Swal);
          _this.characterList = [];
          _this.close();
          _this.$bus.$emit('refresh-setup-list',true);  //刷新申请立项列表
        } else {
          Toast.warning(resp.message, Swal);
        }
      })
    },

    reject(character){
      let _this = this;
      // console.log("character",character);
      Swal.fire({
        title: '驳回特性',
        text: '驳回后,相应特性定义将退回给产品经理处理',
        input: 'textarea',
        inputPlaceholder: '请填写驳回理由',
        inputAttributes: { maxlength: 500 },
        inputValidator: (value) => (value && value.trim().length >= 2) ? undefined : '请填写驳回理由（至少 2 个字）',
        showCancelButton: true,
        confirmButtonColor: '#409EFF',
        cancelButtonColor: '#909399',
        confirmButtonText: '驳回',
        cancelButtonText: '取消'
      }).then((result) => {
        if (result.isConfirmed) {
          _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/character/reject/' + character.id, {
            rejectReason: result.value.trim(),
          }).then((response)=>{
            let resp = response.data;
            if (resp.success) {
              Toast.success("已驳回给产品经理", Swal);
              _this.getCharacterListByProjectId(_this.project.id);
            } else {
              Toast.warning(resp.message, Swal);
            }
          })
        }
      })
    },

    close(){
      let _this = this;
      _this.closeFunction(_this.index);
    },

    closeDialog() {
      let _this = this;
      _this.dialogVisible = false;
    },

    handleClose(done) {
      let _this = this;
      _this.dialogVisible = false;
      _this.characterInfoVisible = false;
      done();
    },
  }
};
</script>

<style lang="scss" scoped>
@import './../../../public/static/scss/caipd.scss';
.ca-form{
  font-size: 14px;
  margin: 40px 30px 0;
}
.paper-height {
  height: calc(100vh - 200px);
}

.paper-big-plain {
  background: white;
  width: 28.5cm;
  display: block;
  margin: 0 auto;
  box-sizing: border-box;
}

</style>
