<!--
 * Author: 周朔鹏
 * Date: 2025-04-14
 * Email: 391902958@qq.com
 * Website: https://www.euipd.com
-->

<template>
  <div style="margin-top: -20px;">
    <div style="height: 100%;">
      <div class="a4-paper-big-plain">
        <div class="ca-form">
          <el-form label-width="150px" size="small">
            <el-form-item
                label="项目名称:"
                required>
              <el-input style="width: 300px;"
                        v-model="setupProject.projectName"
              ></el-input>
            </el-form-item>
            <el-form-item
                label="项目代号:"
                required>
              <el-input style="width: 300px;"
                        v-model="setupProject.projectCode"
                        :placeholder="getDataMonth"
              ></el-input>
            </el-form-item>
            <el-form-item
                label="规划开发版本:">
              <div>
                {{ setupProject.devVersion }}
              </div>
            </el-form-item>
            <el-form-item
                required
                label="项目简介:">
              <el-input style="width: 700px;"
                        type="textarea"
                        :autosize="{ minRows: 5, maxRows: 15}"
                        placeholder="请填写项目简介"
                        v-model="setupProject.projectIntroduce"
              ></el-input>
            </el-form-item>
<!--            <div style="position: relative;">
              <el-form-item
                  label="保密等级:">
                <el-select v-model="secretLevel" placeholder="请选择">
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
            <el-form-item
                required
                label="监管委员:">
              <el-select v-model="setupProject.supervise" filterable placeholder="请选择">
                <el-option
                    v-for="item in supervises"
                    :key="item.id"
                    :label="staffOptionLabel(item)"
                    :value="item.id">
                </el-option>
              </el-select>
            </el-form-item>
            <el-form-item
                required
                label="产品经理:">
              <el-select v-model="setupProject.productManagerId" filterable placeholder="请选择">
                <el-option
                    v-for="item in productManagers"
                    :key="item.id"
                    :label="staffOptionLabel(item)"
                    :value="item.id">
                </el-option>
              </el-select>
            </el-form-item>
            <el-form-item
                required
                label="项目经理:">
              <el-select v-model="setupProject.projectManagerId" filterable placeholder="请选择">
                <el-option
                    v-for="item in projectManagers"
                    :key="item.id"
                    :label="staffOptionLabel(item)"
                    :value="item.id">
                </el-option>
              </el-select>
            </el-form-item>
            <el-form-item
                required
                label="系统工程:">
              <el-select v-model="setupProject.systemManagerId" filterable placeholder="请选择">
                <el-option
                    v-for="item in systemManagers"
                    :key="item.id"
                    :label="staffOptionLabel(item)"
                    :value="item.id">
                </el-option>
              </el-select>
            </el-form-item>
            <el-form-item
                required
                label="测试主管:">
              <el-select v-model="setupProject.testManagerId" filterable placeholder="请选择">
                <el-option
                    v-for="item in workerList"
                    :key="item.key"
                    :label="item.label"
                    :value="item.key">
                </el-option>
              </el-select>
            </el-form-item>
            <el-form-item
                required
                label="核心成员:">
            </el-form-item>
            <div>
              <el-transfer style="margin-left: 72px; margin-bottom: 15px;"
                           ref="character-setup"
                           filterable
                           filter-placeholder="请输入姓名"
                           v-model="value"
                           :data="workerList"
                           :titles="titles">
              </el-transfer>
            </div>
            <el-form-item
                required
                label="激励政策:">
              <el-input style="width: 700px;"
                        type="textarea"
                        :autosize="{ minRows: 3, maxRows: 10}"
                        placeholder="请填写激励政策"
                        v-model="setupProject.incentivePolicy"
              ></el-input>
            </el-form-item>
            <el-form-item  v-show="!releaseTimeShowFlag"
                           required
                           label="发布时间:">
              <el-date-picker
                  v-model="setupProject.releaseTime"
                  type="date"
                  placeholder="选择日期"
                  :picker-options="pickerOptions"
                  @change="onDateChange">
              </el-date-picker>
            </el-form-item>
          </el-form>
        </div>
        <!-- 添加里程碑-->
        <div class="hr hr-18 dotted"></div>
        <div>
          <project-milestone
              v-if="setupProject.id"
              ref="milestoneRef"
              :key="setupProject.id + timeStamp"
              :get-db-record=true
              :project="setupProject"
              :milestones="setupProject.milestoneList">
          </project-milestone>
        </div>

        <!--特性清单-->
        <div class="hr hr-18 dotted"></div>
        <div style="padding: 0 80px;">
          <div style="margin-left: 1px; font-size: 15px; margin-bottom: 10px;">特性清单</div>
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
                    label="其他费用总计"
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
                    width="120"
                    align="center">
                  <template slot-scope="scope">
                    <el-tooltip content="对产品经理提报的预算进行复核或修改" placement="top" effect="light" :open-delay=1000>
                      <el-button
                          size="mini"
                          @click.stop="confirmManhour(scope.$index, scope.row)">
                        预算核定
                      </el-button>
                    </el-tooltip>
                  </template>
                </el-table-column>
              </el-table>
            </div>
          </div>
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
        <div style="padding: 0 40px;">
          <div  style="display: flex; flex-direction: row; justify-content: space-between; align-items: center;">
            <div style="display: flex; flex-direction: row; justify-content: flex-start; align-items: center;">
              <div style="margin-top: 16px;">
                <el-form label-width="150px" size="small">
                  <el-form-item
                      label="项目总预算:"
                      required>
                    <div>
                      {{ directSumBudget | rmbFormat}}
                      <span style="margin-left: 6px;">元</span>
                    </div>
                  </el-form-item>
                </el-form>
              </div>
              <div style="margin-left: 30px;">
                <el-button type="primary" size="small" @click.stop="budgetEdit()">编辑预算表</el-button>
              </div>
            </div>
            <div style="display: flex; flex-direction: row; justify-content: flex-end; align-items: center; margin-right: 60px;">
              <div style="font-weight: 500; margin-right: 6px;">
                选择项目分类:
              </div>
              <el-select
                  v-model="categoryId"
                  filterable
                  placeholder="选择项目分类">
                <el-option
                    v-for="item in categoryList"
                    :key="item.id"
                    :label="item.name"
                    :value="item.id">
                </el-option>
              </el-select>
            </div>
          </div>

          <div class="hr hr-12 dotted"></div>
          <div style="margin: 15px 0px 20px 0">
            <FileUploadCore
                :input-id="('project-setup-' + (setupProject.id))"
                :key="('file-upload-' + (setupProject.id))"
                :x-project-id="setupProject.id"
                :project-type="PROJECT_TYPE.PROJECT.key"
                :item-id="setupProject.id"
                :file-group="FILE_GROUPING.PROJECT.key"
                :en-delete="true"
                v-on:uploadFileList="getUploadFileList"
            ></FileUploadCore>
          </div>
        </div>

      </div>
      <div>
        <div class="clearfix form-actions" v-loading="actionBusy">
          <div style="display: flex; justify-content: center;">
            <!-- SETUP：指定 IPMT/最终管理人审批 -->
            <button v-if="canApproveSetup"
                    v-on:click="setup()"
                    class="ca-btn-primary" type="button"
                    :disabled="actionBusy">
              {{ actionBusy ? '处理中...' : '立项' }}
            </button>
            <button v-if="canRejectSetup"
                    v-on:click="rejectApplication()" class="ca-btn-primary" type="button"
                    :disabled="actionBusy">
              驳回
            </button>
            <!-- SETUP_EDIT：创建者/最终管理人编辑与提交审批（待审批态禁止「保存」，避免打回草稿） -->
            <button v-if="canSubmitSetup"
                    v-on:click="saveSetupInfo('submit')" class="ca-btn-primary" type="button"
                    :disabled="actionBusy">
              {{ actionBusy ? '处理中...' : '提交' }}
            </button>
            <button v-if="canSaveSetup"
                    v-on:click="saveSetupInfo('save')" class="ca-btn-primary" type="button"
                    :disabled="actionBusy">
              {{ actionBusy ? '处理中...' : '保存' }}
            </button>
            <button v-if="canDeleteDirect"
                    v-on:click="deleteProject(setupProject)" class="ca-btn-primary" type="button"
                    :disabled="actionBusy">
              删除
            </button>
            <button v-if="canWithdrawDev"
                    v-on:click="withdrawProject(setupProject)" class="ca-btn-primary" type="button"
                    :disabled="actionBusy">
              撤回
            </button>
            <button v-on:click="close()" class="ca-btn-primary" type="button" :disabled="actionBusy">
              关闭
            </button>
          </div>
        </div>
      </div>
    </div>

    <!--    预算核定对话框-->
    <el-dialog
        :visible.sync="dialogVisible"
        width="23cm"
        append-to-body
        :before-close="handleClose"
        :close-on-click-modal=false>
      <div slot="title" style="font-size: 20px;">{{ ((dialogTitle) + ' : ' + (characterItem.characterName)) }}</div>
      <character-budget-confirm-dlg
          :key="characterItem.id"
          :character-item="characterItem"
          :close-function="closeDialog"
      ></character-budget-confirm-dlg>
    </el-dialog>

    <!--特性定义表-->
    <el-dialog
        title="特性定义表"
        width="29cm"
        append-to-body
        :visible.sync="characterInfoVisible"
        :before-close="handleClose"
        :close-on-click-modal=true>
      <div>
        <CharacterDetailInfoCoreAll
            :id="'character-detail-show-all-setup'"
            :key="characterShow.id"
            :character-id="characterShow.id"
        ></CharacterDetailInfoCoreAll>
      </div>
      <div style="width: 100%; height: 50px;"></div>
    </el-dialog>

    <!--通用预算填写对话框-->
    <el-dialog
        :visible.sync="budgetEditDlg"
        width="23cm"
        append-to-body
        :before-close="handleClose"
        :close-on-click-modal=false>
      <div slot="title" style="font-size: 20px;">{{ ((dialogTitle)) }}</div>
      <BudgetEditDlg
          :key="((projectId) + '-' + (random))"
          :project-id="projectId"
          :project-type="PROJECT_TYPE.PROJECT.key"
          :dev-budget="devBudget"
          :close-function="closeDialog"
      ></BudgetEditDlg>
    </el-dialog>

  </div>
</template>
<script>
import Swal from "sweetalert2";
import CharacterBudgetConfirmDlg from "@/components/rdms/character-budget-confirm-dlg";
import CharacterDetailInfoCoreAll from "@/components/std/character-detail-info-core-all.vue";
import projectMilestone from "@/components/rdms/project-milestone";
import moment from 'moment';
import {fixed2fractional, rmbFormat} from "@/filter/filter";
import FileUploadCore from "@/components/std/file-upload-core.vue";
import BudgetEditDlg from "@/components/rdms/budget-edit-dlg.vue";
import DevVersionUtil from "@/utils/dev-version-util";

export default {
  components:{
    FileUploadCore,
    CharacterBudgetConfirmDlg,
    CharacterDetailInfoCoreAll,
    projectMilestone,
    BudgetEditDlg,
  },

  name: 'project-setup',
  props:{
    projectId: {
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

      value: [],
      workers: [],
      workerList: [],
      titles: ['可选人员','核心成员'],

      cbbData: [],
      cbbValueStr: '',
      cbbValue: [],
      cbbTitles: ['可选 特性','已选 特性'],
      projectDetailInfo: {},

      supervises: [],
      productManagers: [],
      projectManagers: [],
      systemManagers: [],

      characterList: [],

      characterListLeft: [],
      project: {},
      setupProject: {},

      multipleSelection: [],

      standardManhour: {},
      dialogVisible: false,
      // budgetEditVisible: false,
      budgetEditDlg: false,
      directSumBudget: 0,

      characterItem: {},
      dialogTitle: '',
      budget: 0,
      addCharge: 0,
      chargeRate: 15,
      sumBudged: 0,

      staffBudget: 0,
      materialBudget: 0,
      otherBudget: 0,

      devBudget: {
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
      },

      characterShow: {},
      characterInfoVisible: false,

      categoryId: '',
      categoryList: [],
      originalReleaseTime: '',
      releaseTimeShowFlag: false,

      timeStamp: '',
      random: 0,

      CHARACTER_STATUS : CHARACTER_STATUS,
      PROJECT_TYPE : PROJECT_TYPE,
      DOC_TYPE: DOC_TYPE,
      RD_TYPE: RD_TYPE,
      FILE_GROUPING: FILE_GROUPING,
      PROJECT_STATUS: PROJECT_STATUS,

      resFiles: [],
      fileIdList: null, // null=尚未同步；[]=已同步且无附件（允许删光）
      fileListReady: false,

      role_IPMT: false,
      role_UltiMng: false,
      role_ADMIN: false,
      actionBusy: false,

      secretLevel: 0,
      options: [{
        value: 0,
        label: '普通项目'
      }, {
        value: 1,
        label: '保密项目'
      }],

      pickerOptions: {
        disabledDate(v) {
          return v.getTime() < new Date().getTime() - 86400000;//  - 86400000是否包括当天
        }
      },
    };
  },

  computed:{
    getDataMonth(){
      let date = new Date();
      let year = date.getFullYear();
      let month = date.getMonth()+1;
      return '例如:PJ-' + year + '-' + month + '-001';
    },
    hasFullAuthority() {
      return !!(this.role_UltiMng
        || (typeof RoleAccess !== 'undefined'
          && RoleAccess.hasAny(['UltiMng'], this.loginUserRoleList || this.loginUser?.roleList)));
    },
    isSupervise() {
      const uid = this.loginUser && this.loginUser.customerUserId;
      const supervise = (this.projectDetailInfo && this.projectDetailInfo.supervise)
        || (this.setupProject && this.setupProject.supervise);
      return !!(uid && supervise && uid === supervise);
    },
    isCreator() {
      const uid = this.loginUser && this.loginUser.customerUserId;
      const creater = this.setupProject && this.setupProject.createrId;
      return !!(uid && creater && uid === creater);
    },
    isSetupEdit() {
      return !!(this.setupProject
        && this.setupProject.status === this.PROJECT_STATUS.SETUP_EDIT.key);
    },
    isSetupPending() {
      return !!(this.setupProject
        && this.setupProject.status === this.PROJECT_STATUS.SETUP.key);
    },
    canEditDraft() {
      return this.isSetupEdit && (this.isCreator || this.hasFullAuthority || this.isSupervise);
    },
    /** 草稿保存：仅 SETUP_EDIT */
    canSaveSetup() {
      return this.canEditDraft;
    },
    /** 提交审批：仅 SETUP_EDIT */
    canSubmitSetup() {
      return this.canEditDraft;
    },
    /** 立项通过：仅待审批 SETUP，且为指定监管委员或最终管理人/超管 */
    canApproveSetup() {
      return this.isSetupPending && (this.isSupervise || this.hasFullAuthority);
    },
    canRejectSetup() {
      return this.canApproveSetup;
    },
    canDeleteDirect() {
      return this.isSetupEdit
        && this.setupProject.rdType === this.RD_TYPE.DIRECT.key
        && (this.isCreator || this.hasFullAuthority)
        && !this.isSupervise;
    },
    canWithdrawDev() {
      return this.isSetupEdit
        && this.setupProject.rdType === this.RD_TYPE.DEVELOPMENT.key
        && (this.isCreator || this.hasFullAuthority)
        && !this.isSupervise;
    },
  },
  mounted: function() {
    let _this =this;

    _this.getLoginUser();
    _this.listIpmtByCustomerId();
    _this.listProductManagerByCustomerId();
    _this.listProjectManagerByCustomerId();
    _this.listSystemManagerByCustomerId();
    _this.listAllWorker();
    _this.getProjectInfo(_this.projectId);
    _this.getCharacterListByProjectId(_this.projectId);
    _this.getStandardManhour();
    _this.getDirectSumBudget();
    _this.getProjectDetailInfo(_this.projectId);
    _this.timeStamp = new Date().getTime();
    _this.random = Math.random();

    _this.$bus.$on('refresh-project-setup-budget', (flag) => {
      if (flag) {
        _this.getDirectSumBudget();
      }
    });
    _this.$bus.$on('refresh-project-setup-budget-table', (flag) => {
      if (flag) {
        _this.getCharacterListByProjectId(_this.projectId);
      }
    });

  },

  beforeDestroy() {
    let _this = this;
    _this.$bus.$off('refresh-project-setup-budget');
    _this.$bus.$off('refresh-project-setup-budget-table');
  },

  methods:{
    fixed2fractional,
    rmbFormat,
    getLoginUser(){
      let _this = this;
      if(Tool.getLoginUser().userGroup === LOGIN_USER_GROUP.CUSTOMER.key){
        _this.loginUser = Tool.getLoginUser().adminUser;
        _this.loginCustomer =Tool.getLoginUser();
      }else{
        _this.loginUser = Tool.getLoginUser();
        _this.loginCustomer = Tool.getLoginUser().customer;
      }

      _this.getCbbListByCustomerId(_this.loginCustomer.id);
      _this.getCategoryList_all_show(_this.loginCustomer.id);

      {
        _this.loginUserRoleList = Tool.getLoginUser().roleList;
        RoleAccess.applyFlags(_this, {
          role_IPMT: 'IPMT',
          role_UltiMng: 'UltiMng',
          role_ADMIN: 'ADMIN',
        }, _this.loginUserRoleList);
      }
    },

    getUploadFileList(resFiles){
      let _this = this;
      _this.resFiles = resFiles || [];
      _this.fileIdList = _this.resFiles
          .map((item) => item && item.id)
          .filter((id) => !!id);
      _this.fileListReady = true;
    },

    /** 保存/立项前确保附件列表已从上传组件同步（含删光后的空数组） */
    ensureFileListReady() {
      if (!this.fileListReady || this.fileIdList === null) {
        Toast.warning("附件列表加载中，请稍后再试", Swal);
        return false;
      }
      this.setupProject.fileIdList = this.fileIdList;
      return true;
    },

    getCategoryList_all_show(customerId) {
      let _this = this;
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/category-project/getCategoryList_all_show/' + customerId).then((response) => {
        let resp = response.data;
        if (resp.success) {
          _this.categoryList = resp.content;
        } else {
          Toast.warning(resp.message, Swal);
        }
      })
    },

    getCbbListByCustomerId(customerId) {
      let _this = this;
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/cbb/getCbbListByCustomerId/' + customerId).then((response) => {
        let resp = response.data;
        if (resp.success) {
          _this.archivedCbbList = resp.content;
          if(Tool.isNotEmpty_ES5(_this.archivedCbbList)){
            _this.archivedCbbList.forEach((item) => {
              _this.cbbData.push({
                label: item.characterName,
                key: item.id,
              });
            });
          }
        } else {
          Toast.warning(resp.message, Swal);
        }
      });
    },

    getProjectDetailInfo(projectId){
      let _this = this;
      let formData = new window.FormData();
      formData.append('projectId', projectId);
      formData.append('loginUserId', _this.loginUser.customerUserId);
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/project/getProjectDetailInfo', formData).then((response)=>{
        let resp = response.data;
        if (resp.success) {
          _this.cbbValueStr = [];
          _this.projectDetailInfo = resp.content;

          if (_this.projectDetailInfo && Array.isArray(_this.projectDetailInfo.cbbList)) {
            try {
              const ids = _this.projectDetailInfo.cbbList.map(item => item.id);
              _this.cbbValueStr = JSON.stringify(ids);
              if (_this.cbbValueStr.length > 6) {
                _this.cbbValue = JSON.parse(_this.cbbValueStr);
              }
            } catch (error) {
              console.error('Error processing cbbList:', error);
            }
          }

        } else {
          Toast.warning(resp.message,Swal);
        }
      })
    },

    getProjectInfo(projectId){
      let _this = this;
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/project/getProjectInfo/' + projectId).then((response) => {
        let resp = response.data;
        if (resp.success) {
          _this.setupProject = resp.content || {};
          const normalized = DevVersionUtil.normalizeLegacy(_this.setupProject.devVersion);
          if (normalized) {
            _this.setupProject.devVersion = normalized;
          }
          if(_this.setupProject.secretLevel){
            _this.secretLevel = _this.setupProject.secretLevel;
          }else{
            _this.secretLevel = 0;
          }

          if(_this.setupProject.keyMemberList){
            _this.value = _this.setupProject.keyMemberList;   //value如果是null会报错!!!
          }

          if (_this.setupProject.categoryIdListStr) {
            try {
              // 约定：[根分类「全部项目」= customerId, 可选子分类]。回显取最后一项。
              let categoryIdList = JSON.parse(_this.setupProject.categoryIdListStr);
              if (Array.isArray(categoryIdList) && categoryIdList.length > 0) {
                _this.categoryId = categoryIdList[categoryIdList.length - 1];
              }
            } catch (error) {
              console.error('Failed to parse categoryIdListStr:', error);
            }
          }


          _this.getReleaseTime(_this.setupProject.milestoneList);
          _this.ensureSelectedStaffOptions();
        } else {
          Toast.warning(resp.message, Swal);
        }
      })
    },

    /** el-select 选项未包含当前值时会直接显示 ID，需用姓名补齐选项 */
    staffOptionLabel(item) {
      if (!item) {
        return '';
      }
      return item.trueName || item.loginName || item.label || '';
    },

    ensureStaffOption(listKey, selectedId, selectedName) {
      if (!selectedId) {
        return;
      }
      const list = Array.isArray(this[listKey]) ? this[listKey] : [];
      if (list.some((item) => item && item.id === selectedId)) {
        this[listKey] = list;
        return;
      }
      const name = selectedName
          || (this.workers || []).reduce((found, worker) => {
            if (found) return found;
            return worker && worker.id === selectedId ? (worker.trueName || worker.loginName) : null;
          }, null);
      if (!name) {
        return;
      }
      this[listKey] = list.concat([{ id: selectedId, trueName: name }]);
    },

    ensureSelectedStaffOptions() {
      const p = this.setupProject || {};
      this.ensureStaffOption('supervises', p.supervise, p.superviseName);
      this.ensureStaffOption('productManagers', p.productManagerId, p.productManagerName);
      this.ensureStaffOption('projectManagers', p.projectManagerId, p.projectManagerName);
      this.ensureStaffOption('systemManagers', p.systemManagerId, p.systemManagerName);
    },

    getArchivedProjectList() {
      let _this = this;
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/project/getArchivedProjectList', {
        customerId: _this.loginCustomer.id,
        customerUserId: _this.loginUser.customerUserId,
      }).then((response)=>{
        let resp = response.data;
        if (resp.success) {
          _this.archivedProjects = resp.content;
        } else {
          Toast.warning(resp.message,Swal);
        }
      })
    },

    getAddChargeRate(){
      let _this = this;
      if(_this.chargeRate < 0){
        _this.chargeRate =0;
        return _this.chargeRate;
      }else{
        return _this.chargeRate;
      }
    },

    reject(index, character){
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
              _this.getCharacterListByProjectId(_this.projectId);
            } else {
              Toast.warning(resp.message, Swal);
            }
          })
        }
      })
    },

    getReleaseTime(milestones){
      let _this = this;
      if(Tool.isNotEmpty(milestones)){
        _this.setupProject.milestoneList =milestones; //update milestones
        let milestone =  milestones.find(item => (item.isRelease === "1" && item.deleted === "0"));
        if(milestone){
          _this.setupProject.releaseTime = milestone.reviewTime;
          _this.originalReleaseTime = milestone.reviewTime;
          _this.releaseTimeShowFlag = true;
        }
      }

    },

    onDateChange(){
      let _this =this;
      _this.timeStamp = new Date().getTime();
      //至少要大于最远的里程碑日期
      let releaseTime = moment(_this.setupProject.releaseTime);
      let original = moment(_this.originalReleaseTime);
      // debugger
      if(releaseTime < original){
        _this.setupProject.releaseTime = _this.originalReleaseTime;
        Swal.fire({
          title: '发布时间错误',
          text: '发布时间不能早于发布里程碑时间,您可以修改发布里程碑时间',
          showCancelButton: false,
          confirmButtonColor: '#409EFF',
          cancelButtonColor: '#909399',
          confirmButtonText: '确认!',
          cancelButtonText: '取消!'
        }).then((result) => {
          console.log(result);
        })
      }
    },

    getTableData(){
      //使用nextTick方法
      this.$nextTick(() => {
        this.characterList.forEach((item) => {
          this.$refs.characterTable.toggleRowSelection(item);
        });
      });
    },

    tableRowClick(row){
      let _this = this;
      _this.characterShow = row;
      _this.characterInfoVisible = true;
    },

    /**
     * 工时核定
     */
    confirmManhour(index, row){
      let _this = this;
      _this.dialogVisible = true;
      _this.dialogTitle = "预算核定";
      _this.characterItem = row;
    },

    budgetEdit(){
      let _this = this;
      _this.random = Math.random();
      _this.budgetEditDlg = true;
      _this.dialogTitle = "预算编辑";
      if(Tool.isNotEmpty_ES5(_this.multipleSelection)){
        _this.handleSelectionChange(_this.multipleSelection);
      }
    },

    closeDialog() {
      let _this = this;
      _this.dialogVisible = false;
      _this.budgetEditDlg = false;
      // _this.budgetEditVisible = false;
    },

    handleClose(done) {
      let _this = this;
      _this.dialogVisible = false;
      _this.budgetEditDlg = false;
      _this.characterInfoVisible = false;
      done(); //el-dialog用于关闭对话框的内置函数.
    },

    //统计表格的汇总信息
    getSummaries(param) {
      let _this = this;
      const {columns, data} = param;
      console.log(data);
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
      _this.budget = sums[1] ? sums[1] : 0;
      sums[1] = rmbFormat(sums[1]);
      // sums[2] = '总预算: ¥ ' + sums[2] + ' 元'

      _this.random = Math.random();
      _this.staffBudget = sums_number[2]* _this.standardManhour.devManhourFee + sums_number[3] * _this.standardManhour.testManhourFee;
      _this.materialBudget = sums_number[4];
      _this.otherBudget = sums_number[5];

      return sums;
    },

    handleSelectionChange(val) {
      let _this = this;
      _this.multipleSelection = val;

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
      let staff = 0;
      let consulting = 0;
      let management = 0;

      _this.multipleSelection.forEach(item => {
        devManhourApproved += item.devManhourApproved;
        testManhourApproved += item.testManhourApproved;
        materialFeeApproved += item.materialFeeApproved;
        test += item.testFee;
        power += item.powerFee;
        conference += item.conferenceFee;
        business += item.businessFee;
        cooperation += item.cooperationFee;
        property += item.propertyFee;
        labor += item.laborFee;
        consulting += item.consultingFee;
        management += item.managementFee;
      })

      _this.devBudget = {
        equipment: 0,
        test: test/10000,
        material: materialFeeApproved / 10000,
        power: power/10000,
        conference: conference/10000,
        business: business/10000,
        cooperation: cooperation/10000,
        property: property/10000,
        labor: labor/10000,
        staff: (devManhourApproved * _this.standardManhour.devManhourFee + testManhourApproved * _this.standardManhour.testManhourFee)/10000,
        consulting: consulting/10000,
        management: management/10000,
        other: 0,
      }
    },

    dateFormat(row) {
      return row.createTimeStr.substring(0,10);
    },
    releaseTimeFormat(row) {
      return row.releaseTime.substring(0,10);
    },

    versionNumFormat(row) {
      return row.devVersion == null ? '' : String(row.devVersion);
    },

    listAllWorker() {
      let _this = this;
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/worker/listAllWorkers/'+ _this.loginCustomer.id).then((response)=>{
        let resp = response.data;
        if (resp.success) {
          _this.workers = resp.content || [];
          _this.workerList = [];
          _this.workers.forEach((worker) => {
            if(worker !== null){
              _this.workerList.push({
                label: worker.trueName || worker.loginName,
                key: worker.id
              });
            }
          });
          _this.ensureSelectedStaffOptions();
        } else {
          Toast.warning(resp.message,Swal);
        }
      })
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
          // debugger
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

    getDirectSumBudget(){
      let _this = this;
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/budget-direct/getDirectSumBudget', {
        customerId: _this.loginCustomer.id,
        projectId: _this.projectId,
      }).then((response)=>{
        let resp = response.data;
        if (resp.success) {
          _this.directSumBudget = resp.content;
        } else {
          Toast.warning(resp.message, Swal);
        }
      })
    },
    blurActive() {
      if (document.activeElement && typeof document.activeElement.blur === 'function') {
        document.activeElement.blur();
      }
    },

    confirmSwal(title, text) {
      this.blurActive();
      return Swal.fire({
        title: title,
        text: text,
        icon: 'question',
        showCancelButton: true,
        confirmButtonColor: '#409EFF',
        cancelButtonColor: '#909399',
        confirmButtonText: '确认!',
        cancelButtonText: '取消!',
        heightAuto: false,
        returnFocus: false,
        didOpen: () => {
          const container = Swal.getContainer();
          if (container) {
            container.style.zIndex = '10000';
          }
        },
      }).then((result) => !!(result.isConfirmed || result.value));
    },

    validateSetupForm(requireCategory) {
      let _this = this;
      _this.setupProject.keyMemberList = _this.value;
      const normalized = DevVersionUtil.normalizeLegacy(_this.setupProject.devVersion);
      if (normalized) {
        _this.setupProject.devVersion = normalized;
      }
      if (1 !== 1
          || !Validator.require(_this.setupProject.projectName, "项目名称", Swal)
          || !Validator.require(_this.setupProject.projectCode, "项目代号", Swal)
          || !Validator.require(_this.setupProject.devVersion, "开发版本", Swal)
          || !Validator.require(_this.setupProject.projectIntroduce, "项目简介", Swal)
          || !Validator.require(_this.setupProject.supervise, "项目监管人", Swal)
          || !Validator.require(_this.setupProject.productManagerId, "产品经理", Swal)
          || !Validator.require(_this.setupProject.projectManagerId, "项目经理", Swal)
          || !Validator.require(_this.setupProject.systemManagerId, "系统工程", Swal)
          || !Validator.require(_this.setupProject.testManagerId, "测试主管", Swal)
          || !Validator.require(_this.setupProject.keyMemberList, "关键成员", Swal)
          || !Validator.require(_this.setupProject.incentivePolicy, "激励政策", Swal)
          || !Validator.require(_this.setupProject.releaseTime, "发布时间", Swal)
          || (requireCategory && !Validator.require(_this.categoryId, "项目分类", Swal))

          || !Validator.length(_this.setupProject.projectName, "项目名称", 2, 50, Swal)
          || !Validator.length(_this.setupProject.projectCode, "项目代号", 2, 50, Swal)
          || !Validator.length(_this.setupProject.projectIntroduce, "项目简介", 5, 1500, Swal)
          || !Validator.length(_this.setupProject.supervise, "项目监管人", 8, 8, Swal)
          || !Validator.length(_this.setupProject.productManagerId, "产品经理", 8, 8, Swal)
          || !Validator.length(_this.setupProject.projectManagerId, "项目经理", 8, 8, Swal)
          || !DevVersionUtil.requireValid(_this.setupProject.devVersion, "开发版本", Swal)
      ) {
        return false;
      }
      if (!_this.setupProject.keyMemberList || !_this.setupProject.keyMemberList.length) {
        Toast.warning("关键成员不能为空", Swal);
        return false;
      }
      return true;
    },

    afterSetupWriteSuccess(selectedCharacters) {
      let _this = this;
      _this.characterListLeft = [];
      (_this.characterList || []).forEach(item => {
        let selected = false;
        (selectedCharacters || []).forEach(character => {
          if (item.id === character.id) {
            selected = true;
          }
        });
        if (!selected) {
          _this.characterListLeft.push(item);
        }
      });
      if (_this.characterListLeft.length) {
        _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/character/modifyCharactersToApprovedStatus', _this.characterListLeft).then((response) => {
          let resp = response.data;
          if (!resp.success) {
            Toast.warning(resp.message, Swal);
          }
        });
      }
      _this.$bus.$emit('refresh-setup-list', true);
      _this.$bus.$emit('notify-setup-count-flag', true);
    },

    /**
     * 立项（审批通过：SETUP → SETUPED）
     */
    setup() {
      let _this = this;
      if (_this.actionBusy) {
        return;
      }
      if (!_this.canApproveSetup) {
        Toast.warning("仅指定的 IPMT 委员或管理员可立项审批", Swal);
        return;
      }
      if (!_this.validateSetupForm(true)) {
        return;
      }
      if (!_this.setupProject.milestoneList || !_this.setupProject.milestoneList.length) {
        Toast.warning("里程碑不能为空，请先创建里程碑", Swal);
        return;
      }
      if (!_this.setupProject.releaseTime) {
        Toast.warning("请先配置发布时间/发布里程碑", Swal);
        return;
      }
      if (!_this.directSumBudget || !(_this.directSumBudget > 0)) {
        Toast.warning("预算不能为零，请先编辑预算表", Swal);
        return;
      }
      const selectedCharacters = _this.multipleSelection || [];
      // 直接立项允许 0 条；其他立项形式至少勾选一项
      if (_this.setupProject.rdType !== _this.RD_TYPE.DIRECT.key
          && !selectedCharacters.length) {
        Toast.warning("请至少勾选一项特性", Swal);
        return;
      }
      let missingMilestone = false;
      let notReadyCharacter = false;
      selectedCharacters.forEach(item => {
        if (!item.milestoneId) {
          missingMilestone = true;
        }
        // 与后端 SETUP 门禁对齐：须为 SETUP 或 APPROVED
        const st = item.status;
        if (st !== CHARACTER_STATUS.SETUP.key && st !== CHARACTER_STATUS.APPROVED.key) {
          notReadyCharacter = true;
        }
      });
      if (missingMilestone) {
        Toast.warning("全部特性都必须指定里程碑", Swal);
        return;
      }
      if (notReadyCharacter) {
        Toast.warning("存在未就绪特性（须为 SETUP 或 APPROVED）", Swal);
        return;
      }

      _this.setupProject.budget = _this.budget;
      _this.setupProject.addCharge = _this.directSumBudget - _this.budget;
      _this.setupProject.chargeRate = Number(_this.budget) === 0 ? 1 : Number(_this.setupProject.addCharge) / Number(_this.directSumBudget);
      if (_this.setupProject.addCharge < 0) {
        Toast.warning("附加费不能小于零", Swal);
        return;
      }

      _this.confirmSwal('确认', '确认立项通过？通过后项目进入已立项状态。').then((ok) => {
        if (!ok) {
          return;
        }
        if (!_this.ensureFileListReady()) {
          return;
        }
        _this.actionBusy = true;
        _this.setupProject.customerId = _this.loginCustomer.id;
        _this.setupProject.loginUserId = _this.loginUser.customerUserId;
        _this.setupProject.characterList = selectedCharacters;
        _this.setupProject.moduleIdListStr = JSON.stringify(_this.cbbValue);
        _this.setupProject.secretLevel = _this.secretLevel;
        _this.setupProject.categoryId = _this.categoryId;
        _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/project/setup', _this.setupProject).then((response) => {
          let resp = response.data;
          if (resp.success) {
            Toast.success(resp.message || "立项成功", Swal);
            const selected = (_this.setupProject.characterList || []).slice();
            _this.close();
            _this.afterSetupWriteSuccess(selected);
          } else {
            Toast.warning(resp.message, Swal);
          }
        }).catch((e) => {
          console.error(e);
          Toast.error("立项失败，请重试", Swal);
        }).finally(() => {
          _this.actionBusy = false;
        });
      });
    },

    /**
     * 保存 / 提交立项信息
     */
    saveSetupInfo(saveOrSubmitFlag) {
      let _this = this;
      if (_this.actionBusy) {
        return;
      }
      if (saveOrSubmitFlag === 'save' && !_this.canSaveSetup) {
        Toast.warning("当前状态不可保存；待审批中请使用「立项」或「驳回」", Swal);
        return;
      }
      if (saveOrSubmitFlag === 'submit' && !_this.canSubmitSetup) {
        Toast.warning("当前状态不可提交；请等待 IPMT 审批或先驳回后再编辑", Swal);
        return;
      }
      const requireCategory = saveOrSubmitFlag === 'submit' || saveOrSubmitFlag === 'save';
      if (!_this.validateSetupForm(requireCategory)) {
        return;
      }
      if (saveOrSubmitFlag === 'submit') {
        if (!_this.setupProject.milestoneList || !_this.setupProject.milestoneList.length) {
          Toast.warning("里程碑不能为空，请先创建里程碑", Swal);
          return;
        }
        if (!_this.directSumBudget || !(_this.directSumBudget > 0)) {
          Toast.warning("预算不能为零", Swal);
          return;
        }
      }

      const tip = saveOrSubmitFlag === 'submit' ? '请确认立项信息填写正确并提交给指定 IPMT 委员审批!' : '确认保存立项信息吗?';
      _this.confirmSwal('确认', tip).then((ok) => {
        if (!ok) {
          return;
        }
        if (!_this.ensureFileListReady()) {
          return;
        }
        _this.actionBusy = true;
        _this.setupProject.customerId = _this.loginCustomer.id;
        _this.setupProject.loginUserId = _this.loginUser.customerUserId;
        _this.setupProject.characterList = _this.multipleSelection;
        _this.setupProject.moduleIdListStr = JSON.stringify(_this.cbbValue);
        _this.setupProject.secretLevel = _this.secretLevel;
        _this.setupProject.saveOrSubmitFlag = saveOrSubmitFlag;
        _this.setupProject.categoryId = _this.categoryId;
        _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/project/save-setup-info', _this.setupProject).then((response) => {
          let resp = response.data;
          if (resp.success) {
            Toast.success(saveOrSubmitFlag === 'submit' ? "提交成功" : "保存成功", Swal);
            const selected = (_this.setupProject.characterList || []).slice();
            _this.close();
            _this.afterSetupWriteSuccess(selected);
          } else {
            Toast.warning(resp.message, Swal);
          }
        }).catch((e) => {
          console.error(e);
          Toast.error("操作失败，请重试", Swal);
        }).finally(() => {
          _this.actionBusy = false;
        });
      });
    },

    /**
     * 驳回立项申请（走 reject-application）
     */
    rejectApplication() {
      let _this = this;
      if (_this.actionBusy) {
        return;
      }
      if (!_this.canRejectSetup) {
        Toast.warning("仅指定的 IPMT 委员或管理员可驳回", Swal);
        return;
      }
      _this.blurActive();
      Swal.fire({
        title: '驳回立项申请',
        text: '驳回后退回立项编辑，创建者可修改后再次提交',
        input: 'textarea',
        inputPlaceholder: '请填写驳回意见',
        inputAttributes: { maxlength: 500 },
        showCancelButton: true,
        confirmButtonColor: '#409EFF',
        cancelButtonColor: '#909399',
        confirmButtonText: '驳回',
        cancelButtonText: '取消',
        heightAuto: false,
        returnFocus: false,
        inputValidator: (value) => (value && value.trim().length >= 2) ? undefined : '请填写驳回意见（至少 2 个字）',
        didOpen: () => {
          const container = Swal.getContainer();
          if (container) {
            container.style.zIndex = '10000';
          }
        },
      }).then((result) => {
        if (!result.isConfirmed) {
          return;
        }
        _this.actionBusy = true;
        _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/project/reject-application', {
          id: _this.setupProject.id,
          loginUserId: _this.loginUser.customerUserId,
          supervise: _this.loginUser.customerUserId,
          rejectReason: result.value.trim(),
        }).then((response) => {
          let resp = response.data;
          if (resp.success) {
            Toast.success("已驳回，项目已退回立项编辑", Swal);
            _this.close();
            _this.$bus.$emit('refresh-setup-list', true);
            _this.$bus.$emit('notify-setup-count-flag', true);
          } else {
            Toast.warning(resp.message, Swal);
          }
        }).catch((e) => {
          console.error(e);
          Toast.error("驳回失败，请重试", Swal);
        }).finally(() => {
          _this.actionBusy = false;
        });
      });
    },

    listIpmtByCustomerId() {
      let _this = this;
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/project/listIpmtByCustomerId/'+ _this.loginCustomer.id).then((response)=>{
        let resp = response.data;
        if (resp.success) {
          _this.supervises = resp.content || [];
          _this.ensureSelectedStaffOptions();
        } else {
          Toast.warning(resp.message,Swal);
        }
      })
    },

    listProductManagerByCustomerId() {
      let _this = this;
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/project/listProductManagerByCustomerId/'+ _this.loginCustomer.id).then((response)=>{
        let resp = response.data;
        if (resp.success) {
          _this.productManagers = resp.content || [];
          _this.ensureSelectedStaffOptions();
        } else {
          Toast.warning(resp.message,Swal);
        }
      })
    },

    listProjectManagerByCustomerId() {
      let _this = this;
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/project/listProjectManagerByCustomerId/'+ _this.loginCustomer.id).then((response)=>{
        let resp = response.data;
        if (resp.success) {
          _this.projectManagers = resp.content || [];
          _this.ensureSelectedStaffOptions();
        } else {
          Toast.warning(resp.message,Swal);
        }
      })
    },

    listSystemManagerByCustomerId() {
      let _this = this;
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/project/listSystemManagerByCustomerId/'+ _this.loginCustomer.id).then((response)=>{
        let resp = response.data;
        if (resp.success) {
          _this.systemManagers = resp.content || [];
          _this.ensureSelectedStaffOptions();
        } else {
          Toast.warning(resp.message,Swal);
        }
      })
    },

    downloadFile(file) {
      let _this = this;
      _this.getSignedUrl(file.path, file.suffix);
    },
    getSignedUrl (fileFullName, suffix) {
      let _this = this;
      let formData = new window.FormData();
      formData.append('fileFullName', fileFullName);
      formData.append('customer', _this.loginCustomer.id);
      formData.append('user', _this.loginUser.customerUserId);
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/file/rdms/oss-get-signed-url', formData).then((response)=>{
        let resp = response.data;
        Tool.openOssSignedUrl(resp, suffix);
        // FileSaver.saveAs(resp.content, fileName);
      });
    },

    deleteProject(project) {
      let _this = this;
      if (_this.actionBusy) {
        return;
      }
      _this.confirmSwal('确认？', `是否确认删除"${ project.projectName }"？`).then((ok) => {
        if (!ok) {
          return;
        }
        _this.actionBusy = true;
        _this.$ajax.delete(process.env.VUE_APP_SERVER + '/customer/project/del/'+ project.id).then((response)=>{
          let resp = response.data;
          if (resp.success) {
            _this.$bus.$emit('refresh-setup-list', true);
            _this.$bus.$emit('notify-setup-count-flag', true);
            _this.close();
            Toast.success("删除成功!", Swal);
          } else {
            Toast.warning(resp.message, Swal);
          }
        }).catch((e) => {
          console.error(e);
          Toast.error("删除失败，请重试", Swal);
        }).finally(() => {
          _this.actionBusy = false;
        });
      });
    },

    withdrawProject(project) {
      let _this = this;
      if (_this.actionBusy) {
        return;
      }
      _this.confirmSwal('确认？', `是否确认撤回"${ project.projectName }"？`).then((ok) => {
        if (!ok) {
          return;
        }
        _this.actionBusy = true;
        _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/project/withdrawProject/'+ project.id).then((response)=>{
          let resp = response.data;
          if (resp.success) {
            _this.$bus.$emit('refresh-setup-list', true);
            _this.$bus.$emit('notify-setup-count-flag', true);
            _this.close();
            Toast.success("撤回成功!", Swal);
          } else {
            Toast.warning(resp.message, Swal);
          }
        }).catch((e) => {
          console.error(e);
          Toast.error("撤回失败，请重试", Swal);
        }).finally(() => {
          _this.actionBusy = false;
        });
      });
    },

    close(){
      let _this = this;
      _this.closeFunction(_this.index);
    },

  }
};
</script>

<style>
.el-input-small .el-input__inner {
  height: 28px !important;
  line-height: 28px !important;
}
</style>

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
