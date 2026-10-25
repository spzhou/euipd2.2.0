<!--
 * Author: 周朔鹏
 * Date: 2025-02-26
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
                已发布产品
              </a>
            </li>
            <li :class="{active : activePageId==='develop'}">
              <a @click="tabDevelopClick">
                开发中产品
              </a>
            </li>
            <li :class="{active : activePageId==='release'}">
              <a @click="tabReleaseClick">
                产品版本
              </a>
            </li>

            <!--产品管理-->
            <DynamicTabItem
                v-for="(item, index) in productList"
                :key="item.id"
                :index="index"
                :active-page-id="activePageId"
                prefix="product-item"
                :close="closeFunction_product"
                @select="tabProductManagementClick">{{ ('产品: ' + (item.projectName)) }}
            </DynamicTabItem>

            <!--特性管理-->
            <DynamicTabItem
                v-for="(item, index) in projectList"
                :key="item.id"
                :index="index"
                :active-page-id="activePageId"
                prefix="project-character-item"
                :close="closeFunction_character"
                @select="tabCharacterManagementClick">{{ ('功能: ' + (item.projectName)) }}
            </DynamicTabItem>

            <!--配置管理-->
            <DynamicTabItem
                v-for="(item, index) in projectConfigList"
                :key="item.id"
                :index="index"
                :active-page-id="activePageId"
                prefix="project-config-item"
                :close="closeFunction_config"
                @select="tabProjectConfigClick">{{ ('产品配置: ' + (item.projectName)) }}
            </DynamicTabItem>

            <!--项目档案-->
            <DynamicTabItem
                v-for="(item, index) in projectArchiveList"
                :key="item.id"
                :index="index"
                :active-page-id="activePageId"
                prefix="project-archive-item"
                :close="closeFunction_archive"
                @select="tabProjectArchiveClick">{{ ('档案: ' + (item.projectName)) }}
            </DynamicTabItem>

            <!--质量成本-->
            <DynamicTabItem
                v-for="(item, index) in projectQualityCostList"
                :key="item.id"
                :index="index"
                :active-page-id="activePageId"
                prefix="quality-cost-item"
                :close="closeFunction_quality"
                @select="tabQualityCostClick">{{ ('质量: ' + (item.projectName)) }}
            </DynamicTabItem>

            <!--BOM管理-->
            <DynamicTabItem
                v-for="(item, index) in projectBomManageList"
                :key="item.id"
                :index="index"
                :active-page-id="activePageId"
                prefix="bom-manage-item"
                :close="closeFunction_bom"
                @select="tabBomManageClick">{{ ('BOM: ' + (item.projectName)) }}
            </DynamicTabItem>

            <!--开发中产品管理-->
            <DynamicTabItem
                v-for="(item, index) in developingProjectList"
                :key="item.id"
                :index="index"
                :active-page-id="activePageId"
                prefix="developing-character-item"
                :close="closeFunction_developing"
                @select="tabDevelopingManagementClick">{{ ('开发中: ' + (item.projectName)) }}
            </DynamicTabItem>

            <!--物料管理-->
            <DynamicTabItem
                v-for="(item, index) in productTaskMaterialList"
                :key="item.id"
                :index="index"
                :active-page-id="activePageId"
                prefix="material-manage-item"
                :close="closeFunction_material"
                @select="tabMaterialItemClick">{{ ('物料管理 : ' + (item.projectName)) }}
            </DynamicTabItem>

            <!--费用管理-->
            <DynamicTabItem
                v-for="(item, index) in productFeeList"
                :key="item.id"
                :index="index"
                :active-page-id="activePageId"
                prefix="fee-manage-item"
                :close="closeFunction_fee"
                @select="tabFeeItemClick">{{ ('费用管理 : ' + (item.projectName)) }}
            </DynamicTabItem>

          
        </template>

                        <DynamicTabPane
                page-id="home"
                :active-page-id="activePageId">
                            <template v-slot:default>
              <div style="display: flex; justify-content: space-between;">
                <div>
                  <p>产品管理: 是为<strong>产品经理</strong>对产品进行综合管理的页面. 包括: 发布管理(版本管理) BOM管理 预算管理 等</p>
                </div>
                <div style="display: flex; flex-direction: row; justify-content: center; align-items: center;">
                  <div style="margin-right: 20px;">
                    <el-tooltip content="直接创建一款产品,以进行规范管理" placement="top" effect="light" :open-delay=1000>
                      <el-button v-if="false" type="primary" @click="createDirectProject()">添加既有产品</el-button>
                    </el-tooltip>
                  </div>
                  <div style="margin-right: 30px;">
                    <el-tooltip content="直接创建一款产品,以进行规范管理" placement="top" effect="light" :open-delay=1000>
                      <el-button v-permission="'521801'" type="primary" @click="showStageStatisticsInfo()">预算汇总数据</el-button>
                    </el-tooltip>
                  </div>
                </div>
              </div>
              <div class="hr hr-10 dotted"></div>
              <div>
                <el-table
                    :data="archivedProjects"
                    border
                    stripe
                    highlight-current-row
                    style="width: 100%"
                    @row-click="rowClick">
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
                      prop="status"
                      :formatter="projectStatusFormat"
                      label="状态"
                      align="center"
                      min-width="80"
                  ></el-table-column>
                  <el-table-column
                      fixed="right"
                      label="操作"
                      align="center"
                      width="1050">
                    <template slot-scope="scope">
                      <div style="display: flex; flex-direction: row; justify-content: center;">
                        <el-button v-if="canPublishProduct(scope.row)"
                                   type="primary" plain size="small" v-on:click.stop="publishProductFromProject(scope.row)">
                          发布产品版本
                        </el-button>
                        <el-button v-if="canArchiveProject(scope.row)"
                                   type="primary" plain size="small" v-on:click.stop="archiveProject(scope.row)">
                          归档
                        </el-button>
                        <el-button v-if="scope.row.productManagerId === loginUser.customerUserId"
                                   type="primary" plain size="small" v-on:click.stop="productManagement(scope.row)">
                          <el-badge :is-dot="scope.row.submitJobNum >0" >
                            产品管理
                          </el-badge>
                        </el-button>
                        <el-button v-if="scope.row.rdType !== RD_TYPE.PRODUCT.key  && scope.row.productManagerId === loginUser.customerUserId"
                                   type="primary" plain size="small" v-on:click.stop="characterManagement(scope.row)">
                          特性管理
                        </el-button>
                        <el-button v-if="scope.row.rdType !== RD_TYPE.PRODUCT.key && scope.row.productManagerId === loginUser.customerUserId"
                                   type="primary" plain size="small" v-on:click.stop="projectConfigManagement(scope.row)">
                          产品配置
                        </el-button>
                        <el-button type="primary" plain size="small" v-on:click.stop="projectArchiveManagement(scope.row)">
                          项目档案
                        </el-button>
                        <el-button type="primary" plain size="small" v-on:click.stop="qualityCostManagement(scope.row)">
                          质量成本
                        </el-button>
                        <el-button v-if="scope.row.rdType !== RD_TYPE.PRODUCT.key  && scope.row.productManagerId === loginUser.customerUserId"
                                   type="primary" plain size="small" v-on:click.stop="bomManagement(scope.row)">
                          BOM管理
                        </el-button>
                        <el-button type="primary" plain size="small"
                                   v-on:click.stop="showBudgetStatisticsInfo(scope.row)">
                          PLM预算
                        </el-button>
                        <el-button v-if="scope.row.productManagerId === loginUser.customerUserId"
                                   type="primary" plain size="small" v-on:click.stop="productMaterialManage(scope.row)">
                          <el-badge :is-dot="scope.row.materialAppNum >0">
                            物料管理
                          </el-badge>
                        </el-button>
                        <el-button v-if="scope.row.productManagerId === loginUser.customerUserId"
                                   type="primary" plain size="small" v-on:click.stop="productFeeManage(scope.row)">
                          <el-badge :is-dot="scope.row.feeAppNum >0">
                            费用管理
                          </el-badge>
                        </el-button>
<!--                        申请预算 放到 PLM预算 页面中去-->
<!--                        <el-button disabled type="primary" plain size="small" v-on:click.stop="bomManagement(scope.row)">
                          预算申请
                        </el-button>-->
                      </div>
                    </template>
                  </el-table-column>
                </el-table>
              </div>
            
            
                            </template></DynamicTabPane>

                        <DynamicTabPane
                page-id="release"
                :active-page-id="activePageId">
                <template v-slot:default>
              <div>
                <p>产品版本: 产品与其发布版本。项目完成后发布产品版本，每次发布登记一个发布版本，可逐个停产。</p>
              </div>
              <div class="hr hr-10 dotted"></div>
              <ProductReleaseList v-if="activePageId === 'release'" :key="releaseListKey"></ProductReleaseList>
                </template></DynamicTabPane>

                        <DynamicTabPane
                page-id="develop"
                :active-page-id="activePageId">
                            <template v-slot:default>
              <div style="display: flex; flex-direction: row; justify-content: space-between; align-items: center;">
                <div>
                  <p>开发中管理: 是为<strong>产品经理</strong>对开发中产品进行全面了解和管理的工作界面.</p>
                </div>
                <div style="margin-right: 30px;">
                  <el-button v-permission="'521801'" type="primary" @click.stop="showStageStatisticsInfo">预算汇总数据</el-button>
                </div>
              </div>
              <div class="hr hr-18 dotted hr-double"></div>
              <div>
                <el-table
                    :data="developingProjects"
                    border
                    stripe
                    highlight-current-row
                    style="width: 100%"
                    @row-click="rowClick_developing">
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
                      label="产品名称"
                      show-overflow-tooltip
                  ></el-table-column>
                  <el-table-column
                      prop="productManagerName"
                      show-overflow-tooltip
                      label="产品经理"
                      align="center"
                      width="110"
                  ></el-table-column>
                  <el-table-column
                      prop="superviseName"
                      label="IPMT监管人"
                      align="center"
                      width="110"
                  ></el-table-column>
                  <el-table-column
                      prop="projectManagerName"
                      label="项目经理"
                      align="center"
                      width="110"
                  ></el-table-column>
                  <el-table-column
                      prop="setupedTime"
                      :formatter="dateFormat"
                      label="立项时间"
                      align="center"
                      width="110"
                  ></el-table-column>
                  <el-table-column
                      fixed="right"
                      label="操作"
                      align="center"
                      width="240">
                    <template slot-scope="scope">
                      <div style="display: flex; flex-direction: row; justify-content: center;">
                        <el-button type="primary" plain size="small" v-on:click.stop="developingManagement(scope.row)">
                          执行数据
                        </el-button>
                        <el-button type="primary" plain size="small" v-on:click.stop="showBudgetStatisticsInfo_main(scope.row)">
                          预算数据
                        </el-button>
                      </div>
                    </template>
                  </el-table-column>
                </el-table>
              </div>
            
            
                            </template></DynamicTabPane>

            <!-- 产品管理-->
            <DynamicTabPane
                v-for="(item, index) in productList"
                :key="item.id"
                :index="index"
                prefix="product-item"
                :active-page-id="activePageId">
                <template v-slot:default>
                <div>
                  <ProductTaskTab
                      :key="item.id "
                      :project-id="item.id"
                      :job-type="JOB_ITEM_TYPE.TASK_PRODUCT.key"
                      :project-type="PROJECT_TYPE.PROJECT.key"
                      :index="index"
                      :entrance="ENTRANCE_CLASS.PRODUCT.key"
                  ></ProductTaskTab>
                </div>
            
                </template></DynamicTabPane>

            <!-- 特性管理-->
            <DynamicTabPane
                v-for="(item, index) in projectList"
                :key="item.id"
                :index="index"
                prefix="project-character-item"
                :active-page-id="activePageId">
                <template v-slot:default>
                <div>
                  <ProjectCharacterDocContainer
                      :key="item.id"
                      :index="index"
                      :project-id="item.id"
                      :project-type="PROJECT_TYPE.PROJECT.key"
                      :entrance="ENTRANCE_CLASS.PRODUCT.key">
                  </ProjectCharacterDocContainer>
                </div>
            
                </template></DynamicTabPane>

            <!-- 配置管理-->
            <DynamicTabPane
                v-for="(item, index) in projectConfigList"
                :key="item.id"
                :index="index"
                prefix="project-config-item"
                :active-page-id="activePageId">
                <template v-slot:default>
                <div>
                  <ProjectConfigContainer
                      :key="item.id"
                      :index="index"
                      :project-id="item.id"
                      :project-type="PROJECT_TYPE.PROJECT.key"
                      :entrance="ENTRANCE_CLASS.PRODUCT.key">
                  </ProjectConfigContainer>
                </div>
            
                </template></DynamicTabPane>

            <!-- 项目档案-->
            <DynamicTabPane
                v-for="(item, index) in projectArchiveList"
                :key="item.id"
                :index="index"
                prefix="project-archive-item"
                :active-page-id="activePageId">
                <template v-slot:default>
                <div>
                  <ProjectArchiveContainer
                      :key="item.id"
                      :index="index"
                      :project-id="item.id"
                      :project-type="PROJECT_TYPE.PROJECT.key"
                      :entrance="ENTRANCE_CLASS.PRODUCT.key">
                  </ProjectArchiveContainer>
                </div>
            
                </template></DynamicTabPane>

            <!-- 质量成本-->
            <DynamicTabPane
                v-for="(item, index) in projectQualityCostList"
                :key="item.id"
                :index="index"
                prefix="quality-cost-item"
                :active-page-id="activePageId">
                <template v-slot:default>
                <div>
                  <QualityCostList
                      :key="item.id"
                      :project-id="item.id">
                  </QualityCostList>
                </div>
            
                </template></DynamicTabPane>

            <!-- BOM管理-->
            <DynamicTabPane
                v-for="(item, index) in projectBomManageList"
                :key="item.id"
                :index="index"
                prefix="bom-manage-item"
                :active-page-id="activePageId">
                <template v-slot:default>
                <div>
                  <BomProductManagePage
                      :key="item.id"
                      :project-id="item.id">
                  </BomProductManagePage>
                </div>
            
                </template></DynamicTabPane>

            <!-- 开发中产品管理-->
            <DynamicTabPane
                v-for="(item, index) in developingProjectList"
                :key="item.id"
                :index="index"
                prefix="developing-character-item"
                :active-page-id="activePageId">
                <template v-slot:default>
              <div>
                <CharacterDevelopingData
                    :key="item.id"
                    :index="index"
                    :project-id="item.id"
                    :project-type="PROJECT_TYPE.PROJECT.key"
                    :entrance="ENTRANCE_CLASS.PRODUCT.key">
                </CharacterDevelopingData>
              </div>
            
                </template></DynamicTabPane>

            <!-- 物料管理-->
            <DynamicTabPane
                v-for="(item, index) in productTaskMaterialList"
                :key="('material-manage-item-' + (item.id))"
                :index="index"
                prefix="material-manage-item"
                :active-page-id="activePageId">
                <template v-slot:default>
              <ProductTaskMaterialList
                  :key="item.id"
                  :project-id="item.id"
              ></ProductTaskMaterialList>
            
                </template></DynamicTabPane>

            <!-- 费用管理-->
            <DynamicTabPane
                v-for="(item, index) in productFeeList"
                :key="('fee-manage-item-' + (item.id))"
                :index="index"
                prefix="fee-manage-item"
                :active-page-id="activePageId">
                <template v-slot:default>
              <ProductTaskFeeList
                  :key="item.id"
                  :project-id="item.id"
              ></ProductTaskFeeList>
            
                </template></DynamicTabPane>

            <!--产品中心预算汇总-->
            <el-dialog
                width="40cm"
                :visible.sync="showStageStatisticsDlg"
                :before-close="handleClose"
                :close-on-click-modal=false>
              <div slot="title" style="font-size: 20px;">{{ dialogTitle }}</div>
              <div v-if="showStageStatisticsDlg">
                <BudgetStageStatisticsPage
                    :key="loginCustomer.id"
                    :customer-id="loginCustomer.id"
                    :stage="BUDGET_TYPE.PRODUCT.key"
                ></BudgetStageStatisticsPage>
              </div>
            </el-dialog>
          
    </WorkspaceFrame>

    <!--添加已有产品-->
    <el-dialog
        :visible.sync="createDirectSetupDlg"
        width="29cm"
        :close-on-click-modal=false>
      <div slot="title" style="font-size: 20px;">{{ dialogTitle }}</div>
      <div v-if="createDirectSetupDlg">
        <ProductAddDirect
            :id="timeStamp"
            :key="('direct-' + (timeStamp))"
            :close-dlg="closeDialog"
        ></ProductAddDirect>
      </div>
    </el-dialog>

    <!--查看已发布预算执行汇总表-->
    <el-dialog
        width="40cm"
        :visible.sync="showStatisticsDlg"
        destroy-on-close
        :before-close="handleClose"
        :close-on-click-modal=false>
      <div slot="title" style="font-size: 20px;">{{ dialogTitle }}</div>
      <div v-if="showStatisticsDlg">
        <budgetDirectStatisticsPage
            :key="project.id"
            :x-project-id="project.id"
            :project-type="PROJECT_TYPE.PRODUCT.key"
            :en-button="true"
        ></budgetDirectStatisticsPage>
      </div>
    </el-dialog>

    <!--查看开发中项目预算执行汇总表-->
    <el-dialog
        width="40cm"
        :visible.sync="showStatisticsDlg_main"
        destroy-on-close
        :before-close="handleClose"
        :close-on-click-modal=false>
      <div slot="title" style="font-size: 20px;">{{ dialogTitle }}</div>
      <div v-if="showStatisticsDlg_main">
        <budgetDirectStatisticsPage
            :key="((subproject.id) + '-project')"
            :x-project-id="subproject.id"
            :project-type="PROJECT_TYPE.PROJECT.key"
        ></budgetDirectStatisticsPage>
      </div>
    </el-dialog>

    <el-dialog
        :visible.sync="publishDlgVisible"
        width="20cm"
        destroy-on-close
        :close-on-click-modal=false>
      <div slot="title" style="font-size: 20px;">发布产品版本</div>
      <ProductPublishDlg
          v-if="publishDlgVisible"
          :key="publishProject.id"
          :project="publishProject"
          :close-function="closePublishDlg">
      </ProductPublishDlg>
    </el-dialog>
  </div>
</template>

<script>
import {closeDynamicTab, openOrFocusTab} from "@/shared/dynamic-tab";
import DynamicTabItem from "@/components/rdms/dynamic-tab-item";
import DynamicTabPane from "@/components/rdms/dynamic-tab-pane";
import WorkspaceFrame from "@/components/rdms/workspace-frame";
import Swal from "sweetalert2";
import JobitemReviewCooContainer from "@/components/rdms/jobitem-review-coo-container.vue";
import ProjectCharacterDocContainer from "@/components/rdms/project-character-doc-container.vue";
import { optionKV, shortDateFormat } from "@/filter/filter";
import ProjectUpdateDlg from "@/components/rdms/project-update-dlg.vue";
import QualityCostList from "@/components/rdms/quality-cost-list.vue";
import ProjectConfigContainer from "@/components/rdms/project-config-container.vue";
import CharacterDevelopingData from "@/components/rdms/character-developing-data.vue";
import ProjectArchiveContainer from "@/components/rdms/project-archive-container.vue";
import BomProductManagePage from "@/components/std/bom-product-manage-page.vue";
import ProductTaskTab from "@/components/rdms/product-task-tab.vue";
import ProductAddDirect from "@/components/rdms/product-add-direct.vue";
import ProductTaskMaterialList from "@/components/rdms/product-task-material-list.vue";
import ProductTaskFeeList from "@/components/rdms/product-task-fee-list.vue";
import BudgetDirectStatisticsPage from "@/components/std/budget-direct-statistics-page.vue";
import BudgetStageStatisticsPage from "@/components/std/budget-stage-statistics-page.vue";
import ProductReleaseList from "@/components/rdms/product-release-list.vue";
import ProductPublishDlg from "@/components/rdms/product-publish-dlg.vue";

export default {
  components: {
    ProductPublishDlg,
    ProductReleaseList,
    BudgetStageStatisticsPage,
    BudgetDirectStatisticsPage,
    ProductTaskFeeList,
    ProductTaskMaterialList,
    ProductAddDirect,
    ProductTaskTab,
    ProjectUpdateDlg,
    DynamicTabItem,
    DynamicTabPane,
    WorkspaceFrame,
    ProjectCharacterDocContainer,
    JobitemReviewCooContainer,
    QualityCostList,
    ProjectConfigContainer,
    CharacterDevelopingData,
    ProjectArchiveContainer,
    BomProductManagePage,
  },
  name: "product",

  data: function() {
    return {
      LOGIN_USER_GROUP: LOGIN_USER_GROUP,
      JOB_ITEM_TYPE: JOB_ITEM_TYPE,
      ENTRANCE_CLASS: ENTRANCE_CLASS,
      PROJECT_TYPE: PROJECT_TYPE,
      RD_TYPE: RD_TYPE,
      BUDGET_TYPE: BUDGET_TYPE,

      activePageId: 'home',
      preActivePageId: 'home',
      releaseListKey: 0,
      publishDlgVisible: false,
      publishProject: {},

      archivedProjects: [],
      developingProjects: [],
      jobitem: {},
      projectList: [],
      productList: [],
      developingProjectList: [],
      projectConfigList: [],
      projectArchiveList: [],
      projectQualityCostList: [],
      projectBomManageList: [],
      projectBomList: [],
      createDirectSetupDlg: false,
      showStageStatisticsDlg: false,

      loginUser: {},
      loginCustomer: {},

      projectItem: {},
      dialogTitle: 'Dialog',
      timeStamp: '',

      productTaskMaterialList: [],
      productFeeList: [],

      showStatisticsDlg: false,
      showStatisticsDlg_main: false,
      project: {},
      subproject: {},
    }
  },
  mounted: function() {
    let _this = this;
    _this.timeStamp = new Date().getTime();
    _this.getLoginUser();
    _this.getArchivedProjectList();

    _this.$bus.$on('close-product-character-doc-page', (index) => {
      _this.closeFunction_character(index);
    });

    _this.$bus.$on('refresh-product-list', (data) => {
      if(data){
        _this.getArchivedProjectList();
      }
    });
  },

  beforeDestroy: function () {
    let _this = this;
    _this.$bus.$off('close-product-character-doc-page');
    _this.$bus.$off('refresh-product-list');
  },

  computed: {

  },

  methods: {
    projectStatusFormat(row) {
      return optionKV(PROJECT_STATUS, row.status);
    },

    hasFullAuthority() {
      return typeof RoleAccess !== 'undefined'
        && RoleAccess.hasAny(['UltiMng'], Tool.getLoginUser().roleList);
    },

    /** 已完成的研发项目可发布为产品台账，限产品经理。 */
    canPublishProduct(row) {
      const uid = this.loginUser && this.loginUser.customerUserId;
      return !!row && row.status === PROJECT_STATUS.COMPLETE.key
        && row.rdType !== RD_TYPE.PRODUCT.key
        && (this.hasFullAuthority() || (!!uid && row.productManagerId === uid));
    },

    /** 已完成的项目可归档，限产品经理或监管委员。 */
    canArchiveProject(row) {
      const uid = this.loginUser && this.loginUser.customerUserId;
      return !!row && row.status === PROJECT_STATUS.COMPLETE.key
        && (this.hasFullAuthority() || (!!uid && (row.productManagerId === uid || row.supervise === uid)));
    },

    publishProductFromProject(row) {
      this.publishProject = { id: row.id, projectName: row.projectName };
      this.publishDlgVisible = true;
    },

    closePublishDlg(done) {
      this.publishDlgVisible = false;
      if (done) {
        this.releaseListKey++;
      }
    },

    archiveProject(row) {
      let _this = this;
      Swal.fire({
        title: '确认',
        text: "系统将对项目进行归档处理",
        icon: 'question',
        showCancelButton: true,
        confirmButtonColor: '#409EFF',
        cancelButtonColor: '#909399',
        confirmButtonText: '确认!',
        cancelButtonText: '取消!'
      }).then((result) => {
        if (result.isConfirmed) {
          _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/sub-project/archive/' + row.id).then((response) => {
            let resp = response.data;
            if (resp.success) {
              _this.getArchivedProjectList();
              Toast.success("归档成功!", Swal);
            } else {
              Toast.warning(resp.message, Swal);
            }
          });
        }
      }).catch(_ => {
      });
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
    showBudgetStatisticsInfo(project) {
      let _this = this;
      _this.project = project;
      _this.showStatisticsDlg = true;
      _this.dialogTitle = "查看预算执行汇总表";
    },
    showBudgetStatisticsInfo_main(subproject) {
      let _this = this;
      _this.subproject = subproject;
      _this.showStatisticsDlg_main = true;
      _this.dialogTitle = "查看预算执行汇总表";
    },
    showStageStatisticsInfo() {
      let _this = this;
      _this.showStageStatisticsDlg = true;
      _this.dialogTitle = "产品PLM阶段预算汇总表";
    },
    handleClose(done) {
      done();
    },

    dateFormat(row, column) {
      if(row.setupedTime === null){
        return '-';
      }
      return shortDateFormat(row.setupedTime);
    },

    createDirectProject() {
      let _this = this;
      _this.timeStamp = new Date().getTime();
      _this.dialogTitle = "添加既有产品";
      _this.createDirectSetupDlg = true;
    },
    closeDialog() {
      let _this = this;
      _this.createDirectSetupDlg = false;
      _this.showStatisticsDlg = false;
      _this.showStageStatisticsDlg = false;
      _this.showStatisticsDlg_main = false;
    },

    //tab点击事件
    tabProductManagementClick(i){
      let _this = this;
      _this.activePageId = `product-item-${i}`;
    },

    tabCharacterManagementClick(i){
      let _this = this;
      _this.activePageId = `project-character-item-${i}`;
    },

    tabDevelopingManagementClick(i){
      let _this = this;
      _this.activePageId = `developing-character-item-${i}`;
    },

    tabProjectConfigClick(i){
      let _this = this;
      _this.activePageId = `project-config-item-${i}`;
    },

    tabProjectArchiveClick(i){
      let _this = this;
      _this.activePageId = `project-archive-item-${i}`;
    },

    tabQualityCostClick(i){
      let _this = this;
      _this.activePageId = `quality-cost-item-${i}`;
    },

    tabBomManageClick(i){
      let _this = this;
      _this.activePageId = `bom-manage-item-${i}`;
    },

    tabHomeClick(){
      let _this = this;
      _this.activePageId = 'home';
      _this.preActivePageId = 'home';
      _this.getArchivedProjectList();
    },

    tabReleaseClick(){
      this.activePageId = 'release';
      this.preActivePageId = 'release';
      this.releaseListKey = new Date().getTime();
    },

    tabDevelopClick(){
      let _this = this;
      _this.activePageId = 'develop';
      _this.preActivePageId = 'develop';
      _this.getDevelopingProjectList();
    },

    rowClick(row, column, e){
      let _this = this;
      _this.characterManagement(row);
    },

    rowClick_developing(row, column, e){
      let _this = this;
      _this.developingManagement(row);
    },

    characterManagement(project) {
      openOrFocusTab(this, this.projectList, project, 'project-character-item');
    },
    closeFunction_character(index) {
      closeDynamicTab(this, {
        list: this.projectList,
        index: index,
        prefix: 'project-character-item',
        fallback: () => this.preActivePageId,
        afterClose: () => this.getArchivedProjectList(),
      });
    },

    productManagement(project) {
      openOrFocusTab(this, this.productList, project, 'product-item');
    },
    closeFunction_product(index) {
      closeDynamicTab(this, {
        list: this.productList,
        index: index,
        prefix: 'product-item',
        fallback: () => this.preActivePageId,
      });
    },

    developingManagement(project) {
      openOrFocusTab(this, this.developingProjectList, project, 'developing-character-item');
    },
    closeFunction_developing(index) {
      closeDynamicTab(this, {
        list: this.developingProjectList,
        index: index,
        prefix: 'developing-character-item',
        fallback: () => this.preActivePageId,
      });
    },

    projectConfigManagement(project) {
      openOrFocusTab(this, this.projectConfigList, project, 'project-config-item');
    },
    closeFunction_config(index) {
      closeDynamicTab(this, {
        list: this.projectConfigList,
        index: index,
        prefix: 'project-config-item',
        fallback: () => this.preActivePageId,
        afterClose: () => this.getArchivedProjectList(),
      });
    },

    projectArchiveManagement(project) {
      openOrFocusTab(this, this.projectArchiveList, project, 'project-archive-item');
    },
    closeFunction_archive(index) {
      closeDynamicTab(this, {
        list: this.projectArchiveList,
        index: index,
        prefix: 'project-archive-item',
        fallback: () => this.preActivePageId,
      });
    },

    qualityCostManagement(qualityCostItem) {
      openOrFocusTab(this, this.projectQualityCostList, qualityCostItem, 'quality-cost-item');
    },
    closeFunction_quality(index) {
      closeDynamicTab(this, {
        list: this.projectQualityCostList,
        index: index,
        prefix: 'quality-cost-item',
        fallback: () => this.preActivePageId,
      });
    },

    bomManagement(projectBom) {
      openOrFocusTab(this, this.projectBomManageList, projectBom, 'bom-manage-item');
    },
    closeFunction_bom(index) {
      closeDynamicTab(this, {
        list: this.projectBomManageList,
        index: index,
        prefix: 'bom-manage-item',
        fallback: () => this.preActivePageId,
      });
    },

    tabMaterialItemClick(i) {
      let _this = this;
      _this.activePageId = `material-manage-item-${i}`;
    },
    productMaterialManage(product) {
      openOrFocusTab(this, this.productTaskMaterialList, product, 'material-manage-item');
    },
    closeFunction_material(index) {
      closeDynamicTab(this, {
        list: this.productTaskMaterialList,
        index: index,
        prefix: 'material-manage-item',
        fallback: () => this.preActivePageId,
      });
    },

    tabFeeItemClick(i) {
      let _this = this;
      _this.activePageId = `fee-manage-item-${i}`;
    },

    productFeeManage(product) {
      openOrFocusTab(this, this.productFeeList, product, 'fee-manage-item');
    },
    closeFunction_fee(index) {
      closeDynamicTab(this, {
        list: this.productFeeList,
        index: index,
        prefix: 'fee-manage-item',
        fallback: () => this.preActivePageId,
      });
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

    getDevelopingProjectList() {
      let _this = this;
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/project/getDevelopingProjectList', {
        customerId: _this.loginCustomer.id,
        customerUserId: _this.loginUser.customerUserId,
      }).then((response)=>{
        let resp = response.data;
        if (resp.success) {
          _this.developingProjects = resp.content;
        } else {
          Toast.warning(resp.message,Swal);
        }
      })
    },

  }
}
</script>
