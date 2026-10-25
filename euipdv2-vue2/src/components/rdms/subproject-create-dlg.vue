<!--
 * Author: 周朔鹏
 * Date: 2025-02-19
 * Email: 391902958@qq.com
 * Website: https://www.euipd.com
-->

<template>
  <div>
    <el-row>
      <el-col :offset="1" :span="21">
        <el-form>
          <el-form-item label="名称" :label-width="formLabelWidth" required>
            <el-col :span="8">
              <el-input v-model="subProjectDlg.label" autocomplete="off"></el-input>
            </el-col>
          </el-form-item>
          <el-form-item label="项目编号" :label-width="formLabelWidth" required>
            <el-col :span="8">
              <el-input v-model="subProjectDlg.projectCode" autocomplete="off"></el-input>
            </el-col>
          </el-form-item>
          <el-form-item label="子项目概要" :label-width="formLabelWidth" required>
            <el-input type="textarea" :rows="3" v-model="subProjectDlg.introduce" autocomplete="off"
                      placeholder="子项目概要"></el-input>
          </el-form-item>
          <el-form-item label="激励政策" :label-width="formLabelWidth" required>
            <el-input type="textarea" :rows="3" v-model="subProjectDlg.incentivePolicy" autocomplete="off"
                      placeholder="激励政策"></el-input>
          </el-form-item>
<!--          <div style="position: relative; margin-left: 48px;">
            <el-form-item
                label="保密等级:">
              <el-select v-model="secretLevel" filterable placeholder="请选择" style="width: 210px;" @change="secretLevelChange">
                <el-option
                    v-for="item in options"
                    :key="item.value"
                    :label="item.label"
                    :value="item.value">
                </el-option>
              </el-select>
            </el-form-item>
            <div style="position: absolute; top: 5px; right: 80px; width: 475px;">
              <div style="font-weight: bold">保密等级说明:</div>
              1. 普通项目的文档授权是自然授权模式. 可以通过任务单的形式调动所有团队资源进行项目研发.<br/>
              2. 保密项目只能由核心团队成员执行, 项目"开发文档"只对核心成员时可见的. "产品文档"在相应的模块被复用的过程中与普通项目的授权模式相同.<br/>
            </div>
          </div>-->
          <el-form-item label="子项目经理" :label-width="formLabelWidth" required>
            <el-col :span="6">
              <el-select v-model="subProjectDlg.projectManagerId" filterable placeholder="子项目经理" style="width: 100%;">
                <el-option v-for="o in filterProjectManagers" :key="o.id" v-bind:value="o.id" :label="o.trueName"></el-option>
              </el-select>
            </el-col>
          </el-form-item>
          <el-form-item label="系统工程师" :label-width="formLabelWidth" required>
            <el-col :span="6">
              <el-select v-model="subProjectDlg.systemManagerId" filterable placeholder="系统工程师" style="width: 100%;">
                <el-option v-for="o in filterSystemManagers" :key="o.id" v-bind:value="o.id"
                           :label="o.trueName"></el-option>
              </el-select>
            </el-col>
          </el-form-item>
          <el-form-item label="测试主管" :label-width="formLabelWidth" required>
            <el-col :span="6">
              <el-select v-model="subProjectDlg.testManagerId" filterable placeholder="测试主管" style="width: 100%;">
                <el-option v-for="o in filterTestManagers" :key="o.id" v-bind:value="o.id"
                           :label="o.trueName"></el-option>
              </el-select>
            </el-col>
          </el-form-item>
          <el-form-item label="目标里程碑" :label-width="formLabelWidth" required>
            <el-row>
              <el-col :span="6">
                <el-select
                    v-model="subProjectDlg.targetMilestoneId"
                    filterable
                    @change="onTargetMilestoneChange"
                    placeholder="目标里程碑"
                    style="width: 100%;;">
                  <el-option v-for="o in milestones" :key="o.id" :value="o.id" :label="o.milestoneName"></el-option>
                </el-select>
              </el-col>
              <el-col :span="18">
                <small><em> ( 功能完成时间: 里程碑节点早于主项目要求, 以里程碑为准; 里程碑节点晚于主项目要求, 以主项目设置为准.)</em></small>
              </el-col>
            </el-row>
          </el-form-item>
          <el-form-item :label-width="formLabelWidth" required
                        v-show="subProjectDlg.targetMilestoneId"
                         label="评审时间:">
            <el-date-picker
                v-model="projectReviewTime"
                type="date"
                placeholder="选择日期"
                :picker-options="pickerOptions"
                @change="onDateChange">
            </el-date-picker>
          </el-form-item>

          <!--可选特性清单-->
          <!--          <div v-if="subProject.rdType !== RD_TYPE.DIRECT.key">-->
          <div>
            <el-form-item label="特性清单" :label-width="formLabelWidth" required>
                  <el-table
                  ref="multipleTable"
                  :data="characters"
                  tooltip-effect="dark"
                  style="width: 100%"
                  empty-text="暂无可分配的特性"
                  @selection-change="handleSelectionChange">
                <el-table-column
                    type="selection"
                    width="55">
                </el-table-column>
                <el-table-column
                    prop="characterName"
                    label="特性名称">
                </el-table-column>
                <el-table-column
                    prop="budget"
                    :formatter="budgetFormat"
                    width="120"
                    align="right"
                    label="开发预算">
                </el-table-column>
                <el-table-column
                    prop="writerName"
                    label="编制人"
                    width="120"
                    align="center"
                    show-overflow-tooltip>
                </el-table-column>
                <el-table-column
                    prop="createTimeStr"
                    label="编制日期"
                    width="150"
                    align="center"
                    show-overflow-tooltip>
                </el-table-column>
              </el-table>
              <div v-if="needCharacterList" style="margin-top: 6px; color: #909399; font-size: 12px;">
                非直接立项须至少勾选 1 项特性
              </div>
            </el-form-item>
          </div>
          <div class="hr hr-18 hr-dotted"></div>
          <div style="margin-left: 75px; font-size: 14px; font-weight: bold; margin-bottom: 15px;">集成特性清单:</div>
          <div style="display: flex; justify-content: center;">
            <el-transfer
                filterable
                filter-placeholder="请输入关键词"
                v-model="cbbValue"
                :data="cbbData"
                :titles="cbbTitles">
            </el-transfer>
          </div>
          <div class="hr hr-18 hr-dotted"></div>
          <div style="margin: 0 0 30px;">
            <div style="display: flex; flex-direction: row; justify-content: flex-start; align-items: flex-start;">
              <div style="margin-top: 8px;">
                <el-form label-width="140px" size="small">
                  <el-form-item label="特性开发基本预算">
                    <div>{{ (characterSumBudgetYuan) | rmbFormat }} 元</div>
                  </el-form-item>
                  <el-form-item label="附加费">
                    <div>{{ addChargeYuan | rmbFormat }} 元</div>
                  </el-form-item>
                  <el-form-item label="批准预算合计" required>
                    <div>
                      {{ approvedSumYuan | rmbFormat }}
                      <span style="margin-left: 6px;">元</span>
                    </div>
                  </el-form-item>
                  <el-form-item v-if="parentRemainingAfterSplitYuan != null" label="父级剩余预算">
                    <div style="color: #909399;">
                      {{ parentRemainingAfterSplitYuan | rmbFormat }} 元
                    </div>
                  </el-form-item>
                </el-form>
                <div style="margin-left: 140px; color: #909399; font-size: 12px; line-height: 1.5;">
                  拆分口径：从父级预算表按科目划转批准额度；特性开发费来自勾选功能，附加费=批准合计−特性开发费。
                </div>
              </div>
              <div style="margin-left: 30px; margin-top: 16px;">
                <el-button type="primary" size="small" :disabled="saving" @click.stop="budgetEdit()">编辑预算表</el-button>
              </div>
            </div>
          </div>
        </el-form>
      </el-col>
    </el-row>
    <div class="clearfix form-actions">
      <div style="display: flex; justify-content: center;">
        <div style="display: inline-block; margin-right: 20px;">
          <el-button type="primary" :loading="saving" :disabled="saving" @click="saveSubProject(subProjectDlg)">
            {{ saving ? '创建中...' : '创 建' }}
          </el-button>
        </div>
        <el-button :disabled="saving" @click="closeDialog">取 消</el-button>
      </div>
    </div>

    <!--通用预算填写对话框-->
    <el-dialog
        :visible.sync="budgetEditDlg"
        width="29cm"
        append-to-body
        :before-close="handleClose"
        :close-on-click-modal=false>
      <div slot="title" style="font-size: 20px;">{{ ((dialogTitle)) }}</div>
      <BudgetEditDlg
          :key="((subProjectId) + '-' + (random))"
          :project-id="subProjectId"
          :project-type="PROJECT_TYPE.SUB_PROJECT.key"
          :dev-budget="subprojectBudget"
          :parent-remaining-wan="parentRemainingWan"
          :parent-budget-detail-wan="parentBudgetDetailWan"
          :close-function="closeDlg"
          @subProjectBudgetData="getBudgetData"
      ></BudgetEditDlg>
    </el-dialog>
  </div>
</template>

<script>
import Swal from "sweetalert2";
import {rmbFormat} from "@/filter/filter";
import FileUploadCore from "@/components/std/file-upload-core.vue";
import BudgetEditDlg from "@/components/rdms/budget-edit-dlg.vue";
import moment from "moment/moment";

export default {
  components: {BudgetEditDlg, FileUploadCore},
  name: "subproject-create-dlg",

  props: {
    subProjectId: {  //被创建子项目的父级项目ID
      default: null,
    },
    projectType: {
      default: null,
    },
    closeDialog: {
      type: Function,
      default: null,
    }
  },

  data: function () {
    return {
      characters: [],
      multipleSelection: [],
      allWorkers: [],
      // checked: false,
      project: {},
      standardManhour: {},

      cbbData: [],
      cbbValueStr: '',
      cbbValue: [],
      cbbTitles: ['可选 特性','已选 特性'],
      projectDetailInfo: {},

      PROJECT_TYPE: PROJECT_TYPE,
      RD_TYPE: RD_TYPE,
      CAPITAL_SOURCE: CAPITAL_SOURCE,

      formLabelWidth: '120px',
      projectInfo: {},
      subProject: {}, //子项目信息
      subProjectDlg: {
        projectCode: '',
      }, //用于对话框的subProject变量
      projectManagers: [],
      systemManagers: [],
      appendChild: {},
      milestones: [],
      projectTeamInfo: {},
      teamIdList: [],

      developFee: 0,
      addCharge: 0,
      chargeRate: 1,
      sumBudget: 0,

      budgetEditDlg: false,
      // budgetEditVisible: false,
      dialogTitle: '',
      budgetData: { sumBudget: 0 },
      /** 父级剩余预算合计（万元），用于拆分上限提示/校验 */
      parentRemainingWan: null,
      /** 父级预算明细（万元），含各科目 FeeBt/FeeZc */
      parentBudgetDetailWan: null,
      saving: false,

      random: 0,
      secretLevel: 0,
      options: [{
        value: 0,
        label: '普通项目'
      }, {
        value: 1,
        label: '保密项目'
      }],

      subprojectBudget: {
        equipment: 0,
        test: 0,
        material: 0,
        power: 0,
        conference: 0,
        business: 0,
        cooperation: 0,
        property: 0,
        labor: 0,
        staff: 0,
        consulting: 0,
        management: 0,
        other: 0,
        performance: 0,
        infrastructure: 0,
      },
      sumMaterial: 0,
      sumStaff: 0,
      sumOther: 0,
      characterSumBudget: 0,
      timeStamp: '',
      milestoneReviewTime: {},
      projectReviewTime: {},
      pickerOptions: {
        disabledDate(v) {
          return v.getTime() < new Date().getTime() - 86400000;//  - 86400000是否包括当天
        }
      },
    }
  },

  mounted: function () {
    let _this = this;
    _this.getLoginUser();
    _this.random = Math.random();
    if (Tool.isNotEmpty(_this.subProjectId)) {
      _this.getSubProjectInfo(_this.subProjectId);
    }
  },

  computed: {
    /** 非直接立项必须勾选特性清单（与立项口径一致） */
    needCharacterList() {
      const rdType = (this.projectInfo && this.projectInfo.rdType)
          || (this.subProject && this.subProject.rdType);
      return rdType !== RD_TYPE.DIRECT.key;
    },
    approvedSumYuan() {
      return (Number(this.budgetData && this.budgetData.sumBudget) || 0) * 10000;
    },
    characterSumBudgetYuan() {
      return (Number(this.characterSumBudget) || 0) * 10000;
    },
    addChargeYuan() {
      return Math.max(0, this.approvedSumYuan - this.characterSumBudgetYuan);
    },
    /** 父级剩余（扣减本子项目已编制批准合计后的预览，万元口径再转元） */
    parentRemainingAfterSplitYuan() {
      if (this.parentRemainingWan == null || !Number.isFinite(Number(this.parentRemainingWan))) {
        return null;
      }
      const approvedWan = Number(this.budgetData && this.budgetData.sumBudget) || 0;
      const leftWan = Math.max(0, Number(this.parentRemainingWan) - approvedWan);
      return leftWan * 10000;
    },
    filterProjectManagers(){
      let _this = this;
      let resultList = [];
      if(_this.projectTeamInfo && _this.projectTeamInfo.teamIdList && _this.projectManagers && _this.secretLevel > 0){
        _this.projectManagers.forEach(item => {
          if(_this.projectTeamInfo.teamIdList.includes(item.id)){
            resultList.push(item);
          }
        })
      }else{
        resultList = _this.projectManagers;
      }

      return resultList;
    },

    filterTestManagers(){
      let _this = this;
      let resultList = [];
      if(_this.projectTeamInfo && _this.projectTeamInfo.teamIdList && _this.allWorkers && _this.secretLevel > 0){
        _this.allWorkers.forEach(item => {
          if(_this.projectTeamInfo.teamIdList.includes(item.id)){
            resultList.push(item);
          }
        })
      }else{
        resultList = _this.allWorkers;
      }

      return resultList;
    },

    filterSystemManagers(){
      let _this = this;
      let resultList = [];
      if(_this.projectTeamInfo && _this.projectTeamInfo.teamIdList && _this.systemManagers && _this.secretLevel > 0){
        _this.systemManagers.forEach(item => {
          if(_this.projectTeamInfo.teamIdList.includes(item.id)){
            resultList.push(item);
          }
        })
      }else{
        resultList = _this.systemManagers;
      }
      return resultList;
    },
  },

  methods: {
    rmbFormat,
    budgetFormat(row, column) {
      return rmbFormat(row.budget);
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
      _this.listProjectManager(_this.loginCustomer.id);
      _this.listSystemManager(_this.loginCustomer.id);
      _this.getStandardManhour();
    },

    onTargetMilestoneChange(val){
      let _this = this;
      // console.log("val:",val)
      _this.milestones.filter((item)=>{
        if(item.id === val){
          _this.milestoneReviewTime = item.reviewTime;
          _this.projectReviewTime = JSON.parse(JSON.stringify(item.reviewTime )); //深拷贝
          _this.subProjectDlg.releaseTime = _this.projectReviewTime;
        }
      });
    },

    onDateChange(){
      let _this = this;
      if (!_this.projectReviewTime) {
        _this.subProjectDlg.releaseTime = null;
        return;
      }
      let releaseTime = moment(_this.projectReviewTime);
      let original = moment(_this.milestoneReviewTime);
      if (!original.isValid()) {
        _this.subProjectDlg.releaseTime = _this.projectReviewTime;
        return;
      }
      if (releaseTime.isAfter(original, 'day')) {
        _this.projectReviewTime = original.toDate();
        _this.subProjectDlg.releaseTime = _this.projectReviewTime;
        Swal.fire({
          icon: 'error',
          title: '评审时间错误',
          text: '子项目评审时间不能晚于里程碑时间!',
          showCancelButton: false,
          confirmButtonColor: '#409EFF',
          cancelButtonColor: '#909399',
          confirmButtonText: '确认!',
          cancelButtonText: '取消!',
          returnFocus: false,
          didOpen: () => {
            const container = Swal.getContainer && Swal.getContainer();
            if (container) {
              container.style.zIndex = '10000';
            }
          },
        });
      } else {
        _this.subProjectDlg.releaseTime = _this.projectReviewTime;
      }
    },

    secretLevelChange(){
      let _this = this;
      if (_this.subProjectDlg && _this.subProjectDlg.projectManagerId
          && String(_this.subProjectDlg.projectManagerId).length > 6) {
        _this.subProjectDlg.projectManagerId = '';
      }
      if (_this.subProjectDlg && _this.subProjectDlg.testManagerId
          && String(_this.subProjectDlg.testManagerId).length > 6) {
        _this.subProjectDlg.testManagerId = '';
      }
      if (_this.subProjectDlg && _this.subProjectDlg.systemManagerId
          && String(_this.subProjectDlg.systemManagerId).length > 6) {
        _this.subProjectDlg.systemManagerId = '';
      }
    },

    getBudgetData(budgetData) {
      let _this = this;
      _this.budgetData = budgetData || { sumBudget: 0 };
      if (_this.budgetData.sumBudget == null) {
        _this.budgetData.sumBudget = 0;
      }
    },
    budgetEdit() {
      let _this = this;
      /*if(_this.subProject.rdType === RD_TYPE.DIRECT.key){
        _this.budgetEditVisible = true;
      }else{
      }*/
      _this.budgetEditDlg = true;
      _this.dialogTitle = "预算编辑";
    },

    closeDlg() {
      let _this = this;
      // _this.budgetEditVisible = false;
      _this.budgetEditDlg = false;
    },

    handleClose(done) {
      let _this = this;
      // _this.budgetEditVisible = false;
      _this.budgetEditDlg = false;
      done();
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

    getSubProjectInfo(subProjectId) {
      // debugger
      let _this = this;
      let formData = new window.FormData();
      formData.append('subProjectId', subProjectId);
      formData.append('loginUserId', _this.loginUser.customerUserId);
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/sub-project/getSubProjectInfo', formData).then((response)=>{
        let resp = response.data;
        if (resp.success) {
          _this.subProject = resp.content;
          if(_this.subProject.secretLevel){
            _this.secretLevel = _this.subProject.mainSecretLevel;
          }else{
            _this.secretLevel = 0;
          }
          {
            _this.projectInfo.id = _this.subProject.id;
            _this.projectInfo.projectId = _this.subProject.projectId;
            _this.projectInfo.deep = _this.subProject.deep;
            _this.projectInfo.rdType = _this.subProject.rdType;
          }
          let size = 0;
          if (Tool.isNotEmpty(_this.subProject.projectTreeList) && Tool.isNotEmpty(_this.subProject.projectTreeList[0].children)) {
            size = _this.subProject.projectTreeList[0].children.length;
          }
          _this.subProjectDlg.projectCode = _this.subProject.projectCode + '-' + (size + 1);
          // 默认带入父级/主项目系统工程师，可再改选
          if (_this.subProject.systemManagerId) {
            _this.$set(_this.subProjectDlg, 'systemManagerId', _this.subProject.systemManagerId);
          }
          _this.allWorkers = _this.subProject.workerList;
          _this.milestones = _this.subProject.milestoneList;
          _this.getSetupedCharacterListBySubProjectId(subProjectId);
          _this.getProjectTeamIdsByProjectId(_this.subProject.projectId);
          _this.loadParentRemainingBudget(subProjectId);
          // CBB信息
          if(_this.subProject && _this.subProject.cbbList){
            // 选用门禁只接受已发布的 CBB，上级清单中已停用、已废弃的不作候选
            _this.subProject.cbbList.filter(item => item.status === CBB_STATUS.ARCHIVED.key).forEach((item) => {
              _this.cbbData.push({
                label: item.cbbName,
                key: item.id,
              });
              /*if(Tool.isNotEmpty_ES5( _this.cbbValueStr)){
                _this.cbbValueStr = _this.cbbValueStr + ",\"" + item.id + "\"";
              }else{
                _this.cbbValueStr = "[\"" + item.id + "\"";
              }*/
            });
            // _this.cbbValueStr = _this.cbbValueStr + "]";
          }
          // _this.cbbValue = JSON.parse(_this.cbbValueStr);

        } else {
          Toast.warning(resp.message, Swal);
        }
      })
    },

    getProjectTeamIdsByProjectId(projectId) {
      let _this = this;
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/project/getProjectTeamIdsByProjectId/' + projectId).then((response) => {
        let resp = response.data;
        if (resp.success) {
          _this.projectTeamInfo = resp.content;
        } else {
          Toast.warning(resp.message, Swal);
        }
      })
    },

    numOrZero(v) {
      const n = Number(v);
      return Number.isFinite(n) ? n : 0;
    },

    loadParentRemainingBudget(subprojectId) {
      let _this = this;
      if (!_this.loginCustomer || !_this.loginCustomer.id || !subprojectId) {
        return;
      }
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/budget-direct/getBudget_subproject', {
        customerId: _this.loginCustomer.id,
        subprojectId: subprojectId,
      }).then((response) => {
        let resp = response.data;
        if (resp.success && resp.content && resp.content.hasRecord) {
          const b = resp.content;
          _this.parentBudgetDetailWan = b;
          const prefixes = [
            'equipment', 'material', 'test', 'power', 'conference', 'business',
            'cooperation', 'property', 'consulting', 'other', 'labor',
            'management', 'performance', 'staff', 'infrastructure'
          ];
          _this.parentRemainingWan = prefixes.reduce((sum, p) => {
            return sum + _this.numOrZero(b[`${p}FeeBt`]) + _this.numOrZero(b[`${p}FeeZc`]);
          }, 0);
        } else {
          _this.parentBudgetDetailWan = null;
          _this.parentRemainingWan = null;
        }
      }).catch(() => {
        _this.parentBudgetDetailWan = null;
        _this.parentRemainingWan = null;
      });
    },

    handleSelectionChange(val) {
      let _this = this;
      _this.multipleSelection = val || [];

      let devManhourApproved = 0;
      let testManhourApproved = 0;
      let materialFeeApproved = 0;
      let test = 0;
      let power = 0;
      let conference = 0;
      let business = 0;
      let cooperation = 0;
      let property = 0;
      let labor = 0;
      let consulting = 0;
      let management = 0;

      _this.multipleSelection.forEach(item => {
        if (!item) return;
        devManhourApproved += _this.numOrZero(item.devManhourApproved);
        testManhourApproved += _this.numOrZero(item.testManhourApproved);
        materialFeeApproved += _this.numOrZero(item.materialFeeApproved);
        test += _this.numOrZero(item.testFee);
        power += _this.numOrZero(item.powerFee);
        conference += _this.numOrZero(item.conferenceFee);
        business += _this.numOrZero(item.businessFee);
        cooperation += _this.numOrZero(item.cooperationFee);
        property += _this.numOrZero(item.propertyFee);
        labor += _this.numOrZero(item.laborFee);
        consulting += _this.numOrZero(item.consultingFee);
        management += _this.numOrZero(item.managementFee);
      });

      const devRate = _this.numOrZero(_this.standardManhour && _this.standardManhour.devManhourFee);
      const testRate = _this.numOrZero(_this.standardManhour && _this.standardManhour.testManhourFee);
      _this.subprojectBudget = {
        equipment: 0,
        test: test / 10000,
        material: materialFeeApproved / 10000,
        power: power / 10000,
        conference: conference / 10000,
        business: business / 10000,
        cooperation: cooperation / 10000,
        property: property / 10000,
        labor: labor / 10000,
        staff: (devManhourApproved * devRate + testManhourApproved * testRate) / 10000,
        consulting: consulting / 10000,
        management: management / 10000,
        other: 0,
        performance: 0,
        infrastructure: 0,
      };
      _this.characterSumBudget =
          _this.subprojectBudget.test +
          _this.subprojectBudget.material +
          _this.subprojectBudget.power +
          _this.subprojectBudget.conference +
          _this.subprojectBudget.business +
          _this.subprojectBudget.cooperation +
          _this.subprojectBudget.property +
          _this.subprojectBudget.labor +
          _this.subprojectBudget.staff +
          _this.subprojectBudget.consulting +
          _this.subprojectBudget.management;

      this.$nextTick(() => {
        _this.random = Math.random();
      });
    },

    //列表查询已经处于"立项状态"的特性/功能项
    getSetupedCharacterListBySubProjectId(subprojectId) {
      let _this = this;
      let formData = new window.FormData();
      formData.append('subprojectId', subprojectId);
      formData.append('loginUserId', _this.loginUser.customerUserId);
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/character/getSetupedCharacterListBySubProjectId', formData).then((response) => {
        let resp = response.data;
        if (resp.success) {
          _this.characters = resp.content || [];
        } else {
          Toast.warning(resp.message, Swal);
        }
      })
    },

    saveSubProject(subProjectDlg) {
      let _this = this;
      if (_this.saving) {
        return;
      }
      // 评审时间可能只选了日期未触发 change，提交前同步一次
      if (_this.projectReviewTime && !subProjectDlg.releaseTime) {
        subProjectDlg.releaseTime = _this.projectReviewTime;
      }
      // 保存校验
      if (1 !== 1
          || !Validator.require(subProjectDlg.label, "子项目名称", Swal)
          || !Validator.require(subProjectDlg.introduce, "子项目概要", Swal)
          || !Validator.require(subProjectDlg.incentivePolicy, "激励政策", Swal)
          || !Validator.require(subProjectDlg.projectManagerId, "子项目经理", Swal)
          || !Validator.require(subProjectDlg.testManagerId, "测试主管", Swal)
          || !Validator.require(subProjectDlg.systemManagerId, "系统工程师", Swal)
          || !Validator.require(subProjectDlg.targetMilestoneId, "目标里程碑", Swal)
          || !Validator.require(subProjectDlg.releaseTime || _this.projectReviewTime, "评审时间", Swal)
          || !Validator.require(subProjectDlg.projectCode, "项目编号", Swal)

          || !Validator.length(subProjectDlg.label, "子项目名称", 2, 30, Swal)
          || !Validator.length(subProjectDlg.introduce, "子项目概要", 5, 1500, Swal)
          || !Validator.length(subProjectDlg.incentivePolicy, "激励政策", 2, 500, Swal)
          || !Validator.length(subProjectDlg.projectCode, "项目编号", 5, 300, Swal)
      ) {
        return;
      }

      if (_this.needCharacterList && (!(_this.multipleSelection) || _this.multipleSelection.length < 1)) {
        Toast.warning("请至少勾选一项特性", Swal);
        return;
      }

      const sumBudgetWan = Number(_this.budgetData && _this.budgetData.sumBudget) || 0;
      const characterBudgetWan = Number(_this.characterSumBudget) || 0;
      if (!(sumBudgetWan > 0)) {
        Toast.warning("请先编辑预算表，批准预算合计必须大于 0", Swal);
        return;
      }
      if (sumBudgetWan + 1e-9 < characterBudgetWan) {
        Toast.warning("批准预算不能小于特性开发基本预算需求", Swal);
        return;
      }
      if (_this.parentRemainingWan != null && sumBudgetWan - _this.parentRemainingWan > 1e-9) {
        Toast.warning("批准预算不能超过父级剩余预算", Swal);
        return;
      }

      // 再次校验评审时间不得晚于里程碑
      if (_this.milestoneReviewTime && subProjectDlg.releaseTime
          && moment(subProjectDlg.releaseTime).isAfter(moment(_this.milestoneReviewTime), 'day')) {
        Toast.warning("子项目评审时间不能晚于里程碑时间", Swal);
        return;
      }

      if (!_this.projectInfo || !_this.projectInfo.id || !_this.projectInfo.projectId) {
        Toast.warning("父项目信息未加载完成，请稍后重试", Swal);
        return;
      }

      Swal.fire({
        title: '确认',
        text: "确认创建子项目？",
        icon: 'question',
        showCancelButton: true,
        confirmButtonColor: '#409EFF',
        cancelButtonColor: '#909399',
        confirmButtonText: '确认!',
        cancelButtonText: '取消!',
        returnFocus: false,
        didOpen: () => {
          const container = Swal.getContainer && Swal.getContainer();
          if (container) {
            container.style.zIndex = '10000';
          }
        },
      }).then((result) => {
        if (result.isConfirmed) {
          _this.exeSaveSubproject(_this.projectInfo, subProjectDlg);
        }
      }).catch(_ => {
      });
    },

    exeSaveSubproject(projectInfo, subProjectDlg) { //这个data就是当前点击的节点
      let _this = this;
      if (_this.saving) {
        return;
      }
      const sumBudgetWan = Number(_this.budgetData && _this.budgetData.sumBudget) || 0;
      const characterBudgetWan = Number(_this.characterSumBudget) || 0;
      const newChild = {
        id: '',
        customerId: _this.loginCustomer.id,
        projectId: projectInfo.projectId,
        label: (subProjectDlg.label || '').trim(),
        parent: projectInfo.id,
        deep: projectInfo.deep + 1,
        rdType: projectInfo.rdType,
        introduce: (subProjectDlg.introduce || '').trim(),
        projectManagerId: subProjectDlg.projectManagerId,
        testManagerId: subProjectDlg.testManagerId,
        systemManagerId: subProjectDlg.systemManagerId,
        children: [],
        targetMilestoneId: subProjectDlg.targetMilestoneId,
        incentivePolicy: (subProjectDlg.incentivePolicy || '').trim(),
        characters: _this.multipleSelection || [],
        creatorId: _this.loginUser.customerUserId,
        projectCode: (subProjectDlg.projectCode || '').trim(),
        releaseTime: subProjectDlg.releaseTime || _this.projectReviewTime,
      };

      // 与后端口径一致：budget=特性开发费，addCharge=批准合计-特性开发费，chargeRate=附加费/批准合计
      // 最终以服务端重算入库为准
      newChild.budget = characterBudgetWan * 10000;
      newChild.addCharge = Math.max(0, sumBudgetWan * 10000 - characterBudgetWan * 10000);
      newChild.chargeRate = sumBudgetWan > 0
          ? Number(newChild.addCharge) / (sumBudgetWan * 10000)
          : 0;
      newChild.budgetData = _this.budgetData;
      newChild.secretLevel = _this.secretLevel || 0;

      newChild.moduleIdListStr = JSON.stringify(_this.cbbValue || []);
      _this.saving = true;
      //将newChild保存到数据库,并返回分配ID的对象
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/sub-project/save', newChild).then((response) => {
        let resp = response.data;
        if (resp.success) {
          _this.appendChild = resp.content;
          Toast.success("创建成功", Swal);
          if (!projectInfo.children) {
            _this.$set(projectInfo, 'children', []);
          }
          projectInfo.children.push(_this.appendChild);

          _this.$bus.$emit('refresh-project-list', true);
          _this.$bus.$emit('refresh-project-subproject', true);
          _this.$bus.$emit('refresh-project-info-outline', true);
          _this.$bus.$emit('refresh-project-subproject-character-list', true);
          _this.close();
        } else {
          Toast.warning(resp.message || "创建失败", Swal);
        }
      }).catch((e) => {
        console.error(e);
        Toast.warning("创建失败，请重试", Swal);
      }).finally(() => {
        _this.saving = false;
      });
    },

    listProjectManager(customerId) {
      let _this = this;
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/project-manager/getProjectManagerList/' + customerId).then((response) => {
        let resp = response.data;
        if (resp.success) {
          _this.projectManagers = resp.content;
        } else {
          Toast.warning(resp.message, Swal);
        }
      })
    },

    listSystemManager(customerId) {
      let _this = this;
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/system-manager/listSystemManagerByCustomerId/' + customerId).then((response) => {
        let resp = response.data;
        if (resp.success) {
          _this.systemManagers = resp.content || [];
        } else {
          Toast.warning(resp.message, Swal);
        }
      })
    },

    close() {
      let _this = this;
      _this.closeDialog();
    }
  }
}
</script>

<style>
.el-input-small .el-input__inner {
  height: 28px !important;
  line-height: 28px !important;
}
</style>

<style lang="scss" scoped>

</style>
