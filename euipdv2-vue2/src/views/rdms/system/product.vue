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

            <!--项目档案-->
            <DynamicTabItem
                v-for="(item, index) in projectArchiveList"
                :key="('project-archive-' + (item.id) + '-' + (index))"
                :index="index"
                :active-page-id="activePageId"
                prefix="project-archive-item"
                :close="closeFunction_archive"
                @select="tabProjectArchiveClick">{{ ('档案: ' + (item.projectName)) }}
            </DynamicTabItem>

            <!--质量成本-->
            <DynamicTabItem
                v-for="(item, index) in projectQualityCostList"
                :key="('quality-cost-' + (item.id) + '-' + (index))"
                :index="index"
                :active-page-id="activePageId"
                prefix="quality-cost-item"
                :close="closeFunction_quality"
                @select="tabQualityCostClick">{{ ('质量: ' + (item.projectName)) }}
            </DynamicTabItem>

            <!--BOM清单-->
            <DynamicTabItem
                v-for="(item, index) in projectBomManageList"
                :key="('bom-manage-' + (item.id) + '-' + (index))"
                :index="index"
                :active-page-id="activePageId"
                prefix="bom-manage-item"
                :close="closeFunction_bom"
                @select="tabBomManageClick">{{ ('BOM: ' + (item.projectName)) }}
            </DynamicTabItem>

            <!--开发中产品管理-->
            <DynamicTabItem
                v-for="(item, index) in developingProjectList"
                :key="('developing-' + (item.id) + '-' + (index))"
                :index="index"
                :active-page-id="activePageId"
                prefix="developing-character-item"
                :close="closeFunction_developing"
                @select="tabDevelopingManagementClick">{{ ('开发中: ' + (item.projectName)) }}
            </DynamicTabItem>

          
        </template>

                        <DynamicTabPane
                page-id="home"
                :active-page-id="activePageId">
                            <template v-slot:default>
              <p>产品管理: 是为<strong>产品经理</strong>对产品进行综合管理的页面. 包括: 发布管理(版本管理) BOM清单 预算管理 等</p>
              <div class="hr hr-18 dotted hr-double"></div>
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
                      width="320">
                    <template slot-scope="scope">
                      <div style="display: flex; flex-direction: row; justify-content: center;">
                        <el-button type="primary" plain size="small" v-on:click.stop="projectArchiveManagement(scope.row)">
                          项目档案
                        </el-button>
                        <el-button type="primary" plain size="small" v-on:click.stop="qualityCostManagement(scope.row)">
                          质量成本
                        </el-button>
                        <el-button type="primary" plain size="small" v-on:click.stop="bomManagement(scope.row)">
                          BOM清单
                        </el-button>
                      </div>
                    </template>
                  </el-table-column>
                </el-table>
              </div>
            
            
                            </template></DynamicTabPane>

                        <DynamicTabPane
                page-id="develop"
                :active-page-id="activePageId">
                            <template v-slot:default>
              <p>开发中管理: 是为<strong>产品经理</strong>对开发中产品进行全面了解和管理的工作界面.</p>
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
                      width="120">
                    <template slot-scope="scope">
                      <div style="display: flex; flex-direction: row; justify-content: center;">
                        <el-button type="primary" plain size="small" v-on:click.stop="developingManagement(scope.row)">
                          执行数据
                        </el-button>
                      </div>
                    </template>
                  </el-table-column>
                </el-table>
              </div>
            
            
                            </template></DynamicTabPane>

            <!-- 项目档案-->
            <DynamicTabPane
                v-for="(item, index) in projectArchiveList"
                :key="('project-archive-content-' + (item.id) + '-' + (index))"
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
                :key="('quality-cost-content-' + (item.id) + '-' + (index))"
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

            <!-- BOM清单-->
            <DynamicTabPane
                v-for="(item, index) in projectBomManageList"
                :key="('bom-manage-content-' + (item.id) + '-' + (index))"
                :index="index"
                prefix="bom-manage-item"
                :active-page-id="activePageId">
                <template v-slot:default>
              <div>
                <BomProductListPage
                    :key="item.id"
                    :project-id="item.id">
                </BomProductListPage>
              </div>
            
                </template></DynamicTabPane>

            <!-- 开发中产品管理-->
            <DynamicTabPane
                v-for="(item, index) in developingProjectList"
                :key="('developing-content-' + (item.id) + '-' + (index))"
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
          
    </WorkspaceFrame>

    <!--项目迭代-->
    <el-dialog
        title="项目迭代"
        width="23cm"
        :visible.sync="projectDlgVisible"
        :close-on-click-modal=false>
      <div v-if="projectDlgVisible">
        <ProjectUpdateDlg
            :key="projectItem.id"
            :project-id="projectItem.id"
            :close-function="closeDlg"
        ></ProjectUpdateDlg>
      </div>
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
import { shortDateFormat } from "@/filter/filter";
import ProjectUpdateDlg from "@/components/rdms/project-update-dlg.vue";
import QualityCostList from "@/components/rdms/quality-cost-list.vue";
import ProjectConfigContainer from "@/components/rdms/project-config-container.vue";
import CharacterDevelopingData from "@/components/rdms/character-developing-data.vue";
import ProjectArchiveContainer from "@/components/rdms/project-archive-container.vue";
import BomProductListPage from "@/components/std/bom-product-list-page.vue";

export default {
  components: {
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
    BomProductListPage,
  },
  name: "product",

  data: function() {
    return {
      LOGIN_USER_GROUP: LOGIN_USER_GROUP,
      JOB_ITEM_TYPE: JOB_ITEM_TYPE,
      ENTRANCE_CLASS: ENTRANCE_CLASS,
      PROJECT_TYPE: PROJECT_TYPE,

      activePageId: 'home',
      preActivePageId: 'home',

      archivedProjects: [],
      developingProjects: [],
      jobitem: {},
      projectList: [],
      developingProjectList: [],
      projectConfigList: [],
      projectArchiveList: [],
      projectQualityCostList: [],
      projectBomManageList: [],
      projectBomList: [],

      loginUser: {},

      projectDlgVisible: false,
      projectItem: {},
    }
  },
  mounted: function() {
    let _this = this;
    _this.getLoginUser();
    _this.getAllProjectArchivedList();

    _this.$bus.$on('close-system-product-character-doc-page', (index) => {
      _this.closeFunction_character(index);
    });

    _this.$bus.$on('refresh-system-product-list', (data) => {
      if(data){
        _this.getAllProjectArchivedList();
      }
    });
  },

  beforeDestroy: function () {
    let _this = this;
    _this.$bus.$off('close-system-product-character-doc-page');
    _this.$bus.$off('refresh-system-product-list');
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
    dateFormat(row, column) {
      return shortDateFormat(row.setupedTime);
    },


    tabDevelopingManagementClick(i){
      let _this = this;
      _this.activePageId = `developing-character-item-${i}`;
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
      _this.getAllProjectArchivedList();
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

    getAllProjectArchivedList() {
      let _this = this;
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/project/getAllProjectArchivedList/' + _this.loginCustomer.id ).then((response)=>{
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

    projectUpdateDlg(project){
      let _this = this;
      _this.projectItem = project;
      _this.projectDlgVisible = true;
    },

    closeDlg(){
      let _this = this;
      _this.projectDlgVisible = false;
    },

  }
}
</script>
