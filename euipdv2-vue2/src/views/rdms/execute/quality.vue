<!--
 * Author: 周朔鹏
 * Date: 2025-02-25
 * Email: 391902958@qq.com
 * Website: https://www.euipd.com
-->

<template>
  <div>
    <!--Tab 卡片-->
        <WorkspaceFrame>
        <template v-slot:nav>
            <li :class="{active : activePageId==='home'}">
              <a @click="tabHomeClick">
                <i class="green ace-icon fa fa-home bigger-120"></i>
                质量问题列表
              </a>
            </li>

            <li v-if="isQcgm" :class="{active : activePageId==='quality-list'}">
              <a @click="tabQualityListClick">
                <i class="green ace-icon fa fa-home bigger-120"></i>
                质量处理总表
              </a>
            </li>

            <!--质量管理-->
            <DynamicTabItem
                v-for="(item, index) in qualityItemList"
                :key="('quality-handle-' + (item.id) + '-' + (index))"
                :index="index"
                :active-page-id="activePageId"
                prefix="quality-handle-item"
                :close="closeFunction_quality"
                @select="tabQualityItemClick">{{ ('质量处理 : ' + (item.name)) }}
            </DynamicTabItem>

            <!--物料管理-->
            <DynamicTabItem
                v-for="(item, index) in qualityMaterialItemList"
                :key="('material-manage-' + (item.id) + '-' + (index))"
                :index="index"
                :active-page-id="activePageId"
                prefix="material-manage-item"
                :close="closeFunction_material"
                @select="tabMaterialItemClick">{{ ('物料管理 : ' + (item.name)) }}
            </DynamicTabItem>

            <!--费用管理-->
            <DynamicTabItem
                v-for="(item, index) in qualityFeeItemList"
                :key="('fee-manage-' + (item.id) + '-' + (index))"
                :index="index"
                :active-page-id="activePageId"
                prefix="fee-manage-item"
                :close="closeFunction_fee"
                @select="tabFeeItemClick">{{ ('费用管理 : ' + (item.name)) }}
            </DynamicTabItem>

            <!--质量成本-->
            <DynamicTabItem
                v-for="(item, index) in projectQualityCostList"
                :key="('quality-cost-' + (item.id) + '-' + (index))"
                :index="index"
                :active-page-id="activePageId"
                prefix="quality-cost-item"
                :close="closeFunction_quality_cost"
                @select="tabQualityListClick">{{ ('质量: ' + (item.projectName)) }}
            </DynamicTabItem>

            <!--质量反馈-->
            <DynamicTabItem
                v-for="(item, index) in projectQualityFeedbackList"
                :key="('quality-feedback-' + (item.id) + '-' + (index))"
                :index="index"
                :active-page-id="activePageId"
                prefix="quality-feedback-item"
                :close="closeFunction_quality_feedback"
                @select="tabQualityFeedbackClick">{{ ('反馈: ' + (item.projectName)) }}
            </DynamicTabItem>

            <!--质量任务-->
            <DynamicTabItem
                v-for="(item, index) in projectQualityTaskList"
                :key="('quality-task-' + (item.id) + '-' + (index))"
                :index="index"
                :active-page-id="activePageId"
                prefix="quality-task-item"
                :close="closeFunction_quality_task"
                @select="tabQualityTaskClick">{{ ('任务: ' + (item.projectName)) }}
            </DynamicTabItem>

          
        </template>

                        <DynamicTabPane
                page-id="home"
                :active-page-id="activePageId">
                            <template v-slot:default>
              <p>质量管理: 是为<strong>产品经理</strong>提供的一套进行质量问题处理和管理的页面.</p>
              <div class="hr 12 dotted hr-double"></div>
              <div>
                <div style="display: flex; justify-content: space-between;">
                  <pagination ref="pagination_quality" v-bind:list="listQuality" v-bind:itemCount="6"></pagination>
                  <div>
                    <!--  这里可以放置按钮-->
                  </div>
                </div>
                <el-table
                    :data="qualityList"
                    row-key="id"
                    border
                    stripe
                    default-expand-all
                    highlight-current-row
                    style="width: 100%"
                    @row-click="rowClick_quality">
                  <el-table-column
                      prop="qualitySerial"
                      width="190"
                      align="center"
                      label="质量ID"
                      show-overflow-tooltip
                  ></el-table-column>
                  <el-table-column
                      prop="name"
                      label="质量问题名称"
                      align="center"
                      show-overflow-tooltip
                  ></el-table-column>
                  <el-table-column
                      prop="projectName"
                      label="项目名称"
                      align="center"
                      show-overflow-tooltip
                  ></el-table-column>
                  <el-table-column
                      prop="subprojectName"
                      label="子项目名称"
                      align="center"
                      show-overflow-tooltip
                  ></el-table-column>
                  <el-table-column
                      prop="characterName"
                      label="特性名称"
                      align="center"
                      show-overflow-tooltip
                  ></el-table-column>
                  <el-table-column
                      prop="writerName"
                      label="提交人"
                      align="center"
                      width="100"
                  ></el-table-column>
                  <el-table-column
                      prop="projectManagerName"
                      label="项目经理"
                      align="center"
                      width="100"
                  ></el-table-column>
                  <el-table-column
                      prop="status"
                      :formatter="qualityStatusFormat"
                      label="处理状态"
                      align="center"
                      width="100"
                  ></el-table-column>
                  <el-table-column
                      fixed="right"
                      label="操作"
                      align="center"
                      width="300">
                    <template slot-scope="scope">
                      <div>
                        <el-tooltip content="对质量问题进行处理" placement="top" effect="light" :open-delay=1000>
                          <el-button type="primary" plain size="small" v-on:click.stop="characterQualityManager(scope.row)">
                            <el-badge :is-dot="scope.row.status === QUALITY_STATUS.SUBMIT.key || scope.row.status === QUALITY_STATUS.JOB_SUBMIT.key" >
                              质量处理
                            </el-badge>
                          </el-button>
                        </el-tooltip>
                        <el-tooltip content="对相关特性/特性开发所需物料进行管理" placement="top" effect="light" :open-delay=1000>
                          <el-button type="primary" plain size="small" v-on:click.stop="qualityMaterialManager(scope.row)">
                            <el-badge :is-dot="scope.row.materialAppNum >0" >
                              物料管理
                            </el-badge>
                          </el-button>
                        </el-tooltip>
                        <el-tooltip content="对相关特性/特性开发过程中其他相关费用进行管理" placement="top" effect="light" :open-delay=1000>
                          <el-button type="primary" plain size="small" v-on:click.stop="qualityFeeManager(scope.row)">
                            <el-badge :is-dot="scope.row.feeAppNum >0" >
                              费用管理
                            </el-badge>
                          </el-button>
                        </el-tooltip>
                      </div>
                    </template>
                  </el-table-column>
                </el-table>
              </div>
            
            
                            </template></DynamicTabPane>

                        <DynamicTabPane
                page-id="quality-list"
                :active-page-id="activePageId">
                            <template v-slot:default>
              <p>质量处理总表: 是为<strong>质量负责人</strong>提供的一套进行质量问题处理和管理的页面.</p>
              <div class="hr 12 dotted hr-double"></div>
              <div>
                <el-table
                    :data="archivedProjects"
                    border
                    stripe
                    highlight-current-row
                    style="width: 100%">
                  <el-table-column  label="序号" align="center" width="50">
                    <template slot-scope="scope">
                      {{scope.$index + 1}}
                    </template>
                  </el-table-column>
                  <el-table-column
                      prop="projectCode"
                      align="center"
                      show-overflow-tooltip
                      label="研发代号"
                      width="150"
                  ></el-table-column>
                  <el-table-column
                      prop="projectName"
                      align="center"
                      min-width="180"
                      label="产品名称"
                      show-overflow-tooltip
                  ></el-table-column>
                  <el-table-column
                      prop="productManagerName"
                      show-overflow-tooltip
                      label="产品经理"
                      align="center"
                      min-width="100"
                  ></el-table-column>
                  <el-table-column
                      prop="superviseName"
                      label="IPMT监管人"
                      align="center"
                      min-width="100"
                  ></el-table-column>
                  <el-table-column
                      prop="projectManagerName"
                      label="项目经理"
                      align="center"
                      min-width="100"
                  ></el-table-column>
                  <el-table-column
                      prop="setupedTime"
                      :formatter="dateFormat"
                      label="立项时间"
                      align="center"
                      min-width="100"
                  ></el-table-column>
                  <el-table-column
                      fixed="right"
                      label="操作"
                      align="center"
                      width="350">
                    <template slot-scope="scope">
                      <div style="display: flex; flex-direction: row; justify-content: center;">
                        <el-button type="primary" plain size="small" v-on:click.stop="qualityFeedbackManagement(scope.row)">
                          质量反馈
                        </el-button>
                        <el-button type="primary" plain size="small" v-on:click.stop="qualityTaskManagement(scope.row)">
                          质量任务
                        </el-button>
                        <el-button type="primary" plain size="small" v-on:click.stop="qualityCostManagement(scope.row)">
                          质量成本
                        </el-button>
                      </div>
                    </template>
                  </el-table-column>
                </el-table>
              </div>
            
            
                            </template></DynamicTabPane>

            <!-- 质量处理-->
            <DynamicTabPane
                v-for="(item, index) in qualityItemList"
                :key="('quality-handle-item-' + (item.id))"
                :index="index"
                prefix="quality-handle-item"
                :active-page-id="activePageId">
                <template v-slot:default>
              <QualityManageTaskTabs
                  v-if="activePageId === ('quality-handle-item-' + (index))"
                  :key="item.id"
                  :quality-id="item.id"
                  :job-type="JOB_ITEM_TYPE.QUALITY.key"
                  :project-type="PROJECT_TYPE.SUB_PROJECT.key"
                  :index="index"
                  :entrance="ENTRANCE_CLASS.QUALITY.key"
              ></QualityManageTaskTabs>
            
                </template></DynamicTabPane>

            <!-- 物料管理-->
            <DynamicTabPane
                v-for="(item, index) in qualityMaterialItemList"
                :key="('material-manage-item-' + (item.id))"
                :index="index"
                prefix="material-manage-item"
                :active-page-id="activePageId">
                <template v-slot:default>
              <QualityMaterialList
                  v-if="activePageId === ('material-manage-item-' + (index))"
                  :key="item.id"
                  :quality-id="item.id"
                  :index="index"
              ></QualityMaterialList>
            
                </template></DynamicTabPane>

            <!-- 费用管理-->
            <DynamicTabPane
                v-for="(item, index) in qualityFeeItemList"
                :key="('fee-manage-item-' + (item.id))"
                :index="index"
                prefix="fee-manage-item"
                :active-page-id="activePageId">
                <template v-slot:default>
              <QualityFeeList
                  v-if="activePageId === ('fee-manage-item-' + (index))"
                  :key="item.id"
                  :quality-id="item.id"
                  :index="index"
              ></QualityFeeList>
            
                </template></DynamicTabPane>

            <!-- 质量成本-->
            <DynamicTabPane
                v-for="(item, index) in projectQualityCostList"
                :key="('quality-cost-content-' + (item.id) + '-' + (index))"
                :index="index"
                prefix="quality-cost-item"
                :active-page-id="activePageId">
                <template v-slot:default>
              <div>
                <QualityCostList
                    v-if="activePageId === ('quality-cost-item-' + (index))"
                    :key="item.id"
                    :project-id="item.id">
                </QualityCostList>
              </div>
            
                </template></DynamicTabPane>

            <!-- 质量反馈-->
            <DynamicTabPane
                v-for="(item, index) in projectQualityFeedbackList"
                :key="('quality-feedback-content-' + (item.id) + '-' + (index))"
                :index="index"
                prefix="quality-feedback-item"
                :active-page-id="activePageId">
                <template v-slot:default>
              <div v-if="item && item.id">
                <QualityFeedbackList
                    v-if="activePageId === ('quality-feedback-item-' + (index))"
                    :key="item.id"
                    :project-id="item.id">
                </QualityFeedbackList>
              </div>
            
                </template></DynamicTabPane>

            <!-- 质量任务-->
            <DynamicTabPane
                v-for="(item, index) in projectQualityTaskList"
                :key="('quality-task-content-' + (item.id) + '-' + (index))"
                :index="index"
                prefix="quality-task-item"
                :active-page-id="activePageId">
                <template v-slot:default>
              <div v-if="item && item.id">
                <QualityTaskList
                    v-if="activePageId === ('quality-task-item-' + (index))"
                    :key="item.id"
                    :project-id="item.id">
                </QualityTaskList>
              </div>
            
                </template></DynamicTabPane>

          
    </WorkspaceFrame>
  </div>
</template>

<script>
import Pagination from "@/components/pagination";
import {closeDynamicTab, openOrFocusTab} from "@/shared/dynamic-tab";
import DynamicTabItem from "@/components/rdms/dynamic-tab-item";
import DynamicTabPane from "@/components/rdms/dynamic-tab-pane";
import WorkspaceFrame from "@/components/rdms/workspace-frame";
import File from "@/components/rdms/file";
import Swal from "sweetalert2";
import {optionKV, shortDateFormat} from "@/filter/filter";
import QualityTaskTabs from "@/components/rdms/quality-task-tabs.vue";
import QualityManageTaskTabs from "@/components/rdms/quality-manage-task-tabs.vue";
import QualityMaterialList from "@/components/rdms/quality-material-list.vue";
import QualityFeeList from "@/components/rdms/quality-fee-list.vue";
import QualityCostList from "@/components/rdms/quality-cost-list.vue";
import QualityFeedbackList from "@/components/rdms/quality-feedback-list.vue";
import QualityTaskList from "@/components/rdms/quality-task-list.vue";
import qualityCostList from "@/components/rdms/quality-cost-list.vue";

export default {
  components: {
    QualityCostList,
    QualityFeeList,
    QualityMaterialList,
    Pagination,
    File,
    DynamicTabItem,
    DynamicTabPane,
    WorkspaceFrame,
    QualityTaskTabs,
    QualityManageTaskTabs,
    QualityFeedbackList,
    QualityTaskList,
  },
  name: "quality",
  data: function () {
    return {
      loginUser: {},
      loginCustomer: {},

      activePageId: 'home',
      preActivePageId: 'home',

      qualityItemList: [],
      qualityList: [],
      archivedProjects: [],

      qualityMaterialItemList: [],
      qualityFeeItemList: [],
      projectQualityCostList: [],
      projectQualityFeedbackList: [],
      projectQualityTaskList: [],

      ENTRANCE_CLASS: ENTRANCE_CLASS,
      JOB_ITEM_TYPE: JOB_ITEM_TYPE,
      PROJECT_TYPE: PROJECT_TYPE,
      QUALITY_STATUS: QUALITY_STATUS,
      USER_ROLE: USER_ROLE,
    }
  },
  mounted: function () {
    let _this = this;
    _this.getLoginUser();
    _this.$refs.pagination_quality.size = 6;
    _this.listQuality(1);

    _this.$bus.$on('close-quality-tab', (index) => {
      _this.closeFunction_quality(index);
    });

    _this.$bus.$on('refresh-quality-list', (data) => {
      if(data){
        _this.listQuality(1);
      }
    });

  },

  beforeDestroy: function () {
    let _this = this;
    _this.$bus.$off('close-quality-tab');
    _this.$bus.$off('refresh-quality-list');
  },

  computed: {
    isQcgm: function () {
      return typeof RoleAccess !== 'undefined'
        && RoleAccess.hasRole('QCGM', this.loginUser?.roleList);
    },
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

      if (RoleAccess.hasRole('QCGM', _this.loginUser?.roleList)) {
        _this.getArchivedProjectList();
      }
    },

    dateFormat(row, column) {
      if(row.setupedTime === null){
        return '-';
      }
      return shortDateFormat(row.setupedTime);
    },

    qualityStatusFormat(row, column) {
      return optionKV(QUALITY_STATUS, row.status);
    },

    tabQualityItemClick(i) {
      let _this = this;
      _this.activePageId = `quality-handle-item-${i}`;
    },

    tabHomeClick() {
      let _this = this;
      _this.activePageId = 'home';
      _this.preActivePageId = 'home';
      _this.listQuality(1);
    },

    tabQualityListClick() {
      let _this = this;
      _this.activePageId = 'quality-list';
      _this.preActivePageId = 'quality-list';
      _this.getArchivedProjectList();
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

    tabMaterialItemClick(i) {
      let _this = this;
      _this.activePageId = `material-manage-item-${i}`;
    },

    tabFeeItemClick(i) {
      let _this = this;
      _this.activePageId = `fee-manage-item-${i}`;
    },

    tabQualityFeedbackClick(i) {
      let _this = this;
      _this.activePageId = `quality-feedback-item-${i}`;
    },

    tabQualityTaskClick(i) {
      let _this = this;
      _this.activePageId = `quality-task-item-${i}`;
    },

    characterQualityManager(quality) {
      openOrFocusTab(this, this.qualityItemList, quality, 'quality-handle-item');
    },
    closeFunction_quality(index) {
      closeDynamicTab(this, {
        list: this.qualityItemList,
        index: index,
        prefix: 'quality-handle-item',
        fallback: 'home',
        afterClose: () => this.listQuality(1),
      });
    },

    qualityMaterialManager(quality) {
      openOrFocusTab(this, this.qualityMaterialItemList, quality, 'material-manage-item');
    },
    closeFunction_material(index) {
      closeDynamicTab(this, {
        list: this.qualityMaterialItemList,
        index: index,
        prefix: 'material-manage-item',
        fallback: () => this.preActivePageId,
        afterClose: () => this.listQuality(1),
      });
    },


    qualityFeeManager(quality) {
      openOrFocusTab(this, this.qualityFeeItemList, quality, 'fee-manage-item');
    },
    closeFunction_fee(index) {
      closeDynamicTab(this, {
        list: this.qualityFeeItemList,
        index: index,
        prefix: 'fee-manage-item',
        fallback: () => this.preActivePageId,
        afterClose: () => this.listQuality(1),
      });
    },

    qualityCostManagement(qualityCostItem) {
      openOrFocusTab(this, this.projectQualityCostList, qualityCostItem, 'quality-cost-item');
    },
    closeFunction_quality_cost(index) {
      closeDynamicTab(this, {
        list: this.projectQualityCostList,
        index: index,
        prefix: 'quality-cost-item',
        fallback: () => this.preActivePageId,
      });
    },

    qualityFeedbackManagement(qualityItem) {
      openOrFocusTab(this, this.projectQualityFeedbackList, qualityItem, 'quality-feedback-item');
    },
    closeFunction_quality_feedback(index) {
      closeDynamicTab(this, {
        list: this.projectQualityFeedbackList,
        index: index,
        prefix: 'quality-feedback-item',
        fallback: () => this.preActivePageId,
      });
    },

    qualityTaskManagement(qualityItem) {
      openOrFocusTab(this, this.projectQualityTaskList, qualityItem, 'quality-task-item');
    },
    closeFunction_quality_task(index) {
      closeDynamicTab(this, {
        list: this.projectQualityTaskList,
        index: index,
        prefix: 'quality-task-item',
        fallback: () => this.preActivePageId,
      });
    },

    rowClick_quality(row, column, e){
      let _this = this;
      _this.characterQualityManager(row);
    },

    handleClose(done) {
      done();
    },

    listQuality(page) {
      let _this = this;
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/quality/listQualityByPdm', {
        page: page,
        size: _this.$refs.pagination_quality.size,
        keyWord: _this.loginCustomer.id, //customerId
        actor: _this.loginUser.customerUserId, //projectManager
      }).then((response)=>{
        let resp = response.data;
        if (resp.success) {
          _this.qualityList = resp.content.list;
          _this.$refs.pagination_quality?.render(page, resp.content.total);
        } else {
          Toast.warning(resp.message,Swal);
        }
      })
    },
  }
}
</script>

<style lang="scss">
@import './../../../../public/static/scss/caipd.scss';

.el-badge__content.is-dot {
  height: 10px;
  width: 10px;
  padding: 0;
  right: -2px !important;
  border-radius: 50%;
}
</style>
