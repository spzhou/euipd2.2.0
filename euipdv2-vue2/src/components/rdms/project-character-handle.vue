<!--
 * Author: 周朔鹏
 * Date: 2025-04-18
 * Email: 391902958@qq.com
 * Website: https://www.euipd.com
-->

<template>
  <div>
    <el-row>
      <el-col :span="24">
        <el-table
            ref="multipleTable"
            :data="characters"
            tooltip-effect="dark"
            style="width: 100%"
            :row-class-name="tableRowClassName"
            @selection-change="handleSelectionChange"
            @row-click="tableRowClick">
          <el-table-column
              prop="id"
              align="center"
              label="ID"
              width="100">
          </el-table-column>
          <el-table-column
              prop="characterName"
              align="center"
              show-overflow-tooltip
              label="特性名称">
          </el-table-column>
          <el-table-column
              prop="subprojectName"
              label="子项目名称"
              align="center"
              show-overflow-tooltip>
          </el-table-column>
          <el-table-column
              prop="status"
              :formatter="characterStatusFormat"
              label="状态"
              align="center"
              width="80"
              show-overflow-tooltip>
          </el-table-column>
          <el-table-column
              prop="iterationVersion"
              :formatter="versionNumFormat"
              label="特性版本"
              width="220"
              align="center"
              show-overflow-tooltip>
          </el-table-column>
          <el-table-column
              prop="writerName"
              label="编制人"
              width="90"
              align="center"
              show-overflow-tooltip>
          </el-table-column>
          <el-table-column
              prop="budget"
              :formatter="currencyFormat"
              label="预算金额"
              align="center"
              width="110">
          </el-table-column>
          <el-table-column
              prop="haveUsedBudget"
              :formatter="haveUsedBudgetFormat"
              label="发生费用"
              align="center"
              width="110">
          </el-table-column>
          <el-table-column
              prop="leftTime"
              :formatter="leftTimeFormat"
              label="剩余时间"
              align="center"
              width="140">
          </el-table-column>
          <el-table-column
              prop="planCompleteTime"
              :formatter="datetimeFormat"
              label="计划完成时间"
              align="center"
              width="110">
          </el-table-column>
          <el-table-column
              fixed="right"
              label="操作"
              align="center"
              width="340">
            <template slot-scope="scope">
              <div v-if="scope.row.projectManagerId === loginUser.customerUserId">
                <div style="display: flex; flex-direction: row; justify-content: center;">
                  <div v-if="scope.row.status === CHARACTER_STATUS.SETUPED.key">
                    <el-tooltip content="您可以将一条功能分配到某个由您创建的子项目中去" placement="top" effect="light"
                                :open-delay=1000>
                      <el-button style="margin: 0 5px;"
                                 v-if="characterStatusCompare(scope.row)"
                                 @click.stop="characterDistribution(scope.row)"
                                 type="primary"
                                 plain
                                 size="small">
                        分配
                      </el-button>
                    </el-tooltip>
                  </div>
                  <div v-if="scope.row.status === CHARACTER_STATUS.SETUPED.key">
                    <el-tooltip content="您可以对特性定义进行修订：修订在修改分支上编辑，提交后由派发人确认，确认后形成最新版本；纳入项目后才更新锁定版本"
                                placement="top" effect="light" :open-delay=1000>
                      <el-button style="margin: 0 5px;"
                                 v-if="characterStatusCompare(scope.row)"
                                 @click.stop="createJobItem_iteration(scope.row)"
                                 type="primary"
                                 plain
                                 size="small">
                        修订
                      </el-button>
                    </el-tooltip>
                  </div>
                  <div v-if="scope.row.status === CHARACTER_STATUS.SETUPED.key && scope.row.jobitemType !== JOB_ITEM_TYPE.SUGGEST_UPDATE.key">
                    <el-tooltip content="您可以根据团队人力资源配置情况, 对功能进行再分解, 以便和人员能力相匹配"
                                placement="top" effect="light" :open-delay=1000>
                      <el-button style="margin: 0 5px;"
                                 v-if="characterStatusCompare(scope.row)"
                                 @click.stop="createJobItem_decompose(scope.row)"
                                 type="primary"
                                 plain
                                 size="small">
                        分解
                      </el-button>
                    </el-tooltip>
                  </div>
                  <div>
                    <el-tooltip content="启动功能研发工作, 您可以通过研发管理模块对研发过程进行管理" placement="top"
                                effect="light" :open-delay=1000>
                      <el-button style="margin: 0 5px;"
                                 v-if="characterStatusCompare(scope.row)"
                                 @click.stop="createJobItem_development(scope.row)"
                                 type="primary"
                                 plain
                                 size="small">
                        研发
                      </el-button>
                    </el-tooltip>
                  </div>
                  <div>
                    <el-tooltip content="对已经归档的档案进行查看" placement="top"
                                effect="light" :open-delay=1000>
                      <el-button style="margin: 0 5px;"
                                 v-if="scope.row.status===CHARACTER_STATUS.ARCHIVED.key
                                 || scope.row.status===CHARACTER_STATUS.INTEGRATION.key
                                 || scope.row.status===CHARACTER_STATUS.DEVELOPING.key"
                                 @click.stop="showArchiveDialog(scope.row)"
                                 type="primary"
                                 plain
                                 size="small">
                        执行数据
                      </el-button>
                    </el-tooltip>
                  </div>
                  <!-- 特性随项目立项后，项目经理不能发起终止，按钮不显示
                  <div>
                    <el-tooltip content="申请终止该特性：产品经理批准后特性废弃，未结束的工单取消，未执行预算退回项目" placement="top"
                                effect="light" :open-delay=1000>
                      <el-button style="margin: 0 5px;"
                                 v-if="terminable(scope.row)"
                                 @click.stop="applyTerminate(scope.row)"
                                 type="danger"
                                 plain
                                 size="small">
                        终止
                      </el-button>
                    </el-tooltip>
                  </div>
                  -->
                </div>
              </div>
            </template>
          </el-table-column>
        </el-table>
      </el-col>
    </el-row>

    <!--执行数据-->
    <el-dialog
        :title="((characterItem.characterName) + ' 特性开发档案')"
        width="32cm"
        :visible.sync="characterArchiveVisible"
        :before-close="handleClose"
        :close-on-click-modal=true>
      <div>
        <CharacterArchiveDlg
            :key="characterItem.id"
            :character-id="characterItem.id"
        ></CharacterArchiveDlg>
      </div>
      <div style="width: 100%; height: 50px;"></div>
    </el-dialog>

    <!--特性定义表-->
    <el-dialog
        title="特性定义表"
        width="29cm"
        :visible.sync="characterInfoVisible"
        :before-close="handleClose"
        :close-on-click-modal=true>
      <div>
        <CharacterDetailInfoCoreAll
            :key="characterItem.id"
            :character-id="characterItem.id"
        ></CharacterDetailInfoCoreAll>
      </div>
      <div style="width: 100%; height: 50px;"></div>
    </el-dialog>

    <!--创建 功能分解 任务-->
    <el-dialog
        title="创建 功能分解 任务"
        width="29cm"
        :visible.sync="jobItemDlgVisible"
        :close-on-click-modal=false>
      <div>
        <JobitemCreateByCharacterIdDlg
            :key="characterItem.id + 'decompose'"
            :character-id="characterItem.id"
            :project-type="projectType"
            :job-type="JOB_ITEM_TYPE.DECOMPOSE.key"
            :index="index"
            :entrance="entrance"
            :close-dlg=closeDlg>
        </JobitemCreateByCharacterIdDlg>
      </div>
    </el-dialog>

    <!--创建 功能修订 任务-->
    <el-dialog
        title="创建 功能修订 任务"
        width="29cm"
        :visible.sync="jobItemDlgVisible_iteration"
        :close-on-click-modal=false>
      <div>
        <JobitemCreateByCharacterIdDlg
            :key="characterItem.id + 'iteration'"
            :character-id="characterItem.id"
            :project-type="PROJECT_TYPE.DEV_PROJECT.key"
            :job-type="JOB_ITEM_TYPE.ITERATION.key"
            :index="index"
            :entrance="entrance"
            :close-dlg=closeCharacterIterationDlg>
        </JobitemCreateByCharacterIdDlg>
      </div>
    </el-dialog>

    <!--启动特性研发-->
    <el-dialog
        title="启动特性研发"
        width="29cm"
        :visible.sync="jobItemDlgVisible_development"
        :close-on-click-modal=false>
      <div>
        <characterDevelopmentStartDlg
            :key="characterItem.id"
            :character-id="characterItem.id"
            :startup-develop="startResearchAndDevelopment"
            :close-dlg="closeCharacterDevelopmentDlg"
        ></characterDevelopmentStartDlg>
      </div>
    </el-dialog>

    <!--功能分配-->
    <el-dialog
        title="向子项目中分配新功能"
        width="29cm"
        :visible.sync="distributionDlgVisible"
        :close-on-click-modal=false>
      <div style="width: 100%; display: flex; flex-direction: row; justify-content: center;">
        <el-form label-width="100px" style="width: 25cm; display: flex; flex-direction: column; align-items: center;">
          <el-row>
            <el-col :span="18">
              <el-form-item label="选择子项目" required>
                <el-col>
                  <el-form-item prop="targetSubprojectId">
                    <el-select v-model="subprojectId" filterable placeholder="请选择子项目" style="width: 85%;">
                      <el-option v-for="o in subprojects" :key="o.id" :value="o.id" :label="o.label"></el-option>
                    </el-select>
                  </el-form-item>
                </el-col>
              </el-form-item>
            </el-col>
            <el-col :span="6">
              <button type="button" v-on:click="saveDistribution()" class="ca-btn-primary">分配</button>
            </el-col>
          </el-row>
          <div class="hr-8 dotted"></div>
          <el-row>
            <el-transfer
                filterable
                filter-placeholder="请输入关键词"
                v-model="cbbValue"
                :data="cbbData"
                :titles="cbbTitles">
            </el-transfer>
          </el-row>
          <div style="height: 30px;"></div>
<!--          <el-row>
            <small style="margin-left: 20px;"><em>注:向子项目分配功能时, 附加费费率与创建子项目时的费率相同.</em></small>
          </el-row>-->
        </el-form>
      </div>
      <div class="clearfix form-actions">
        <div style="display: flex; justify-content: center;">
          <el-button type="primary" @click="distributionDlgVisible = false" style="width: 80px;">关闭</el-button>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import Swal from "sweetalert2";
import CharacterDetailInfoCoreAll from "@/components/std/character-detail-info-core-all.vue";
import JobitemCreateByCharacterIdDlg from "@/components/std/jobitem-create-by-character-id-dlg.vue";
import {iterationFormat, rmbFormat, dateFormat, formatSecond, optionKV} from "@/filter/filter"
import VersionLabel from "@/shared/version-label";
import CharacterArchiveDlg from "@/components/std/character-archive-dlg.vue";
import characterDevelopmentStartDlg from "@/components/std/character-development-start-dlg.vue";

export default {
  components: {
    CharacterDetailInfoCoreAll,
    File,
    JobitemCreateByCharacterIdDlg,
    CharacterArchiveDlg,
    characterDevelopmentStartDlg,
  },
  name: "project-character-handle",

  props: {
    subProjectId: {
      default: null
    },
    projectType: {
      default: null,
    },
    index: {
      default: null,
    },
    entrance: {
      default: null,
    }
  },

  data: function () {
    return {
      loginUser: {},
      loginCustomer: {},

      multipleSelection: [],

      cbbData: [],
      cbbValueStr: '',
      cbbValue: [],
      cbbTitles: ['可选 特性','已选 特性'],

      JOB_ITEM_TYPE: JOB_ITEM_TYPE,
      ENTRANCE_CLASS: ENTRANCE_CLASS,

      characterArchiveVisible: false,
      characterInfoVisible: false,
      jobItemDlgVisible: false,
      distributionDlgVisible: false,
      jobItemDlgVisible_iteration: false,
      jobItemDlgVisible_development: false,

      PROJECT_TYPE: PROJECT_TYPE,

      subprojectId: '',
      subprojects: [],
      characterItem: {},
      parentCharacterItem: {},
      CHARACTER_STATUS: CHARACTER_STATUS,

      characters: [],
      subProject: {},
    }
  },

  mounted: function () {
    let _this = this;
    _this.getLoginUser();

    if (Tool.isNotEmpty(_this.subProjectId)) {
      _this.listAllCharactersBySubProjectId(_this.subProjectId);
      _this.getSubProjectCbbList(_this.subProjectId);
    }

    _this.$bus.$on('refresh-project-subproject-character-list', (data) => {
      if(data){
        if (Tool.isNotEmpty(_this.subProjectId)) {
          _this.listAllCharactersBySubProjectId(_this.subProjectId);
        }
      }
    });

  },

  beforeDestroy() {
    let _this = this;
    _this.$bus.$off('refresh-project-subproject-character-list');
  },

  computed: {
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

    getSubProjectCbbList(subprojectId) {
      let _this = this;
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/sub-project/getSubProjectCbbList/' + subprojectId).then((response)=>{
        let resp = response.data;
        if (resp.success) {
          this.subProject = resp?.content;
          if (this.subProject && this.subProject.cbbList) {
            this.cbbData = []; // 清空旧数据
            // 选用门禁只接受已发布的 CBB，子项目清单中已停用、已废弃的不作候选
            for (const item of this.subProject.cbbList.filter(cbb => cbb.status === CBB_STATUS.ARCHIVED.key)) {
              this.cbbData.push({
                label: item.cbbName,
                key: item.id,
              });
            }
            /*try {
              this.cbbValue = JSON.parse(this.subProject.moduleIdListStr || '[]');
            } catch (error) {
              console.error('Failed to parse moduleIdListStr:', error);
              this.cbbValue = [];
            }*/
          }
        }else{
          Toast.warning(resp.message, Swal);
        }
      });
    },

    listAllCharactersBySubProjectId(subprojectId) {
      let _this = this;
      let formData = new window.FormData();
      formData.append('subprojectId', subprojectId);
      formData.append('loginUserId', _this.loginUser.customerUserId);
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/character/listAllCharactersBySubProjectId', formData).then((response) => {
        let resp = response.data;
        if (resp.success) {
          _this.characters = resp.content;
          _this.$bus.$emit('refresh-project-subproject-character-list', false);
        } else {
          Toast.warning(resp.message, Swal);
        }
      })
    },

    startResearchAndDevelopment(character) {
      let _this = this;
      Swal.fire({
        title: '请确认',
        text: "是否确认启动相应功能的研发？",
        icon: 'question',
        showCancelButton: true,
        confirmButtonColor: '#409EFF',
        cancelButtonColor: '#909399',
        confirmButtonText: '确认!',
        cancelButtonText: '取消!'
      }).then((result) => {
        if (result.value) {
          _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/character/startResearchAndDevelopment', {
            characterId: character.id,
            moduleIdListStr: character.moduleIdListStr,
          }).then((response) => {
            let resp = response.data;
            if (resp.success) {
              Toast.success("启动成功", Swal);
              _this.listAllCharactersBySubProjectId(_this.subProjectId);
              _this.$bus.$emit('notify-project-job-flag', true);  //项目管理
              _this.$bus.$emit('notify-develop-count-flag', true);  //研发管理

              _this.closeCharacterDevelopmentDlg();
            } else {
              Toast.warning(resp.message, Swal);
            }
          })
        }
      })
    },

    terminable(row) {
      return [CHARACTER_STATUS.SETUPED.key, CHARACTER_STATUS.DEVELOPING.key,
        CHARACTER_STATUS.INTEGRATION.key, CHARACTER_STATUS.DEV_COMPLETE.key].includes(row.status);
    },

    applyTerminate(row) {
      let _this = this;
      Swal.fire({
        title: '申请终止特性',
        text: '《' + row.characterName + '》终止后不可恢复，请填写终止原因',
        input: 'textarea',
        inputPlaceholder: '终止原因',
        icon: 'warning',
        showCancelButton: true,
        confirmButtonColor: '#F56C6C',
        cancelButtonColor: '#909399',
        confirmButtonText: '提交申请',
        cancelButtonText: '取消',
        inputValidator: (value) => (value && value.trim()) ? undefined : '请填写终止原因',
      }).then((result) => {
        if (!result.isConfirmed) {
          return;
        }
        let formData = new window.FormData();
        formData.append('reason', result.value.trim());
        _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/character/terminate/apply/' + row.id, formData).then((response) => {
          let resp = response.data;
          if (resp.success) {
            Toast.success(resp.message || '已提交', Swal);
            _this.listAllCharactersBySubProjectId(_this.subProjectId);
          } else {
            Toast.warning(resp.message, Swal);
          }
        });
      });
    },

    characterStatusCompare: function (row) {
      let statusArray = [];
      statusArray.push(CHARACTER_STATUS.SETUPED.key);
      if(statusArray.includes(row.status)){
        return true;
      }
      return false;
    },

    handleSelectionChange(val) {
      this.multipleSelection = val;
    },

    currencyFormat(row, column) {
      if (row.budget) {
        if(row.rdType !== RD_TYPE.RESEARCH.key){
          return rmbFormat(row.budget);
        }else{
          return '-';
        }
      }
      return 0;
    },

    haveUsedBudgetFormat(row, column) {
      if (row.haveUsedBudget) {
        if(row.rdType !== RD_TYPE.RESEARCH.key){
          return rmbFormat(row.haveUsedBudget);
        }else{
          return '-';
        }
      }else{
        if(row.rdType !== RD_TYPE.RESEARCH.key){
          return 0;
        }else{
          return '-';
        }
      }
    },

    datetimeFormat(row, column) {
      if(row.rdType === RD_TYPE.DEVELOPMENT.key || row.rdType === RD_TYPE.DIRECT.key || row.rdType === RD_TYPE.MODULE.key){
        if (row.planCompleteTime) {
          return dateFormat(row.planCompleteTime).substring(0, 10);
        }
        return 0;
      }else{
        return '-';
      }
    },

    versionNumFormat(row, column) {
      return VersionLabel.lockedWithLatest(row.devVersion, row.lockedIterationVersion, row.iterationVersion)
          || iterationFormat(row.iterationVersion);
    },

    characterStatusFormat(row, column) {
      return optionKV(CHARACTER_STATUS, row.status);
    },

    leftTimeFormat(row, column) {
      if(row.rdType === RD_TYPE.DEVELOPMENT.key || row.rdType === RD_TYPE.DIRECT.key || row.rdType === RD_TYPE.MODULE.key){
        let leftTime = new Date(row.planCompleteTime).getTime();
        let left = leftTime / 1000 - Date.now() / 1000;
        return formatSecond(left);
      }else{
        return '-';
      }
    },

    saveDistribution() {
      let _this = this;
      if (1 !== 1
          || !Validator.require(_this.subprojectId, "子项目ID", Swal)
          || !Validator.length(_this.subprojectId, "子项目ID", 8, 8, Swal)
      ) {
        return;
      }

      _this.characterItem.subprojectId = _this.subprojectId;   //小写的subprojectId是Character的字段名
      _this.characterItem.loginUserId = _this.loginUser.customerUserId;
      _this.characterItem.moduleIdListStr = JSON.stringify(_this.cbbValue);

      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/character/saveDistribution', _this.characterItem).then((response) => {
        let resp = response.data;
        if (resp.success) {
          _this.distributionDlgVisible = false;
          _this.listAllCharactersBySubProjectId(_this.subProjectId);
          _this.$bus.$emit('refresh-project-info-outline', true);  //刷新 项目概要
          Toast.success("提交成功", Swal);
        } else {
          Toast.warning(resp.message, Swal);
        }
      })
    },

    createJobItem_decompose(row) {
      let _this = this;
      _this.characterItem = row;
      _this.jobItemDlgVisible = true;
    },

    createJobItem_development(row) {
      let _this = this;
      _this.characterItem = row;
      _this.jobItemDlgVisible_development = true;
    },

    createJobItem_iteration(row) {
      let _this = this;
      _this.characterItem = row;
      _this.jobItemDlgVisible_iteration = true;
    },

    characterDistribution(row) {
      let _this = this;
      _this.characterItem = row;
      _this.ListSubprojectByParentId(row.subprojectId);
      _this.distributionDlgVisible = true;
    },

    ListSubprojectByParentId(parentId){
      let _this = this;
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/sub-project/ListSubprojectByIds', {
        parent: parentId,
        creatorId: _this.loginUser.customerUserId,
      }).then((response)=>{
        let resp = response.data;
        if (resp.success) {
          _this.subprojects = resp.content;
        } else {
          Toast.warning(resp.message,Swal);
        }
      })
    },

    tableRowClick(row, column, event) {
      let _this = this;
      _this.characterItem = row;
      _this.characterInfoVisible = true;
    },

    showArchiveDialog(row, column, event) {
      let _this = this;
      _this.characterItem = row;
      _this.characterArchiveVisible = true;
    },

    closeDlg() {
      let _this = this;
      _this.jobItemDlgVisible = false;
    },

    closeCharacterIterationDlg() {
      let _this = this;
      _this.jobItemDlgVisible_iteration = false;
    },

    closeCharacterDevelopmentDlg() {
      let _this = this;
      _this.jobItemDlgVisible_development = false;
    },

    handleClose(done) {
      let _this = this;
      _this.characterInfoVisible = false;
      _this.characterArchiveVisible = false;
      done()
    },

    tableRowClassName({row, rowIndex}) {
      // console.log("row:", Date.parse(row.planCompleteTime));
      if (Number(1.2) >= Number(Number(row.haveUsedBudget) / Number(row.budget)) && Number(Number(row.haveUsedBudget) / Number(row.budget)) > Number(1.0) ) {
        return 'warning-row-1';
      }else if (Number(1.5) >= Number(Number(row.haveUsedBudget) / Number(row.budget))  && Number(Number(row.haveUsedBudget) / Number(row.budget)) > Number(1.2) ) {
        return 'warning-row-2';
      }else if (Number(1.8) >= Number(Number(row.haveUsedBudget) / Number(row.budget))  && Number(Number(row.haveUsedBudget) / Number(row.budget)) > Number(1.5) ) {
        return 'warning-row-3';
      }else if (Number(2.0) >= Number(Number(row.haveUsedBudget) / Number(row.budget))  && Number(Number(row.haveUsedBudget) / Number(row.budget)) > Number(1.8) ) {
        return 'warning-row-4';
      }else if (Number(Number(row.haveUsedBudget) / Number(row.budget)) > Number(2.0) ) {
        return 'warning-row-5';
      }else if (Date.now() > Date.parse(row.planCompleteTime) && (row.status !== CHARACTER_STATUS.ARCHIVED.key && row.status !== CHARACTER_STATUS.COMPLETE.key)) {
        return 'warning-row-3';
      }
      return '';
    }
  }
}
</script>

<style>
.el-table .warning-row-1 {
  background: #FDF5E6FF;
}
.el-table .warning-row-2 {
  background: #fde8c1;
}
.el-table .warning-row-3 {
  background: #fcdba0;
}
.el-table .warning-row-4 {
  background: #facd7c;
}
.el-table .warning-row-5 {
  background: #fac059;
}

</style>

<style lang="scss" scoped>

</style>
