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
          
        </template>

                        <DynamicTabPane
                page-id="home"
                :active-page-id="activePageId">
                            <template v-slot:default>
              <p>文档下载: 是产品中心向非技术部门发布的文档的下载管理页面. 相关文档主要包括售后文档 市场相关文档以及销售相关文档等</p>
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
                      min-width="110">
                    <template slot-scope="scope">
                      <div style="display: flex; flex-direction: row; justify-content: center;">
                        <el-button type="primary" plain size="small" v-on:click.stop="productManagement(scope.row)">
                            文档下载
                        </el-button>
                      </div>
                    </template>
                  </el-table-column>
                </el-table>
              </div>
            
            
                            </template></DynamicTabPane>

            <!-- 文件下载-->
            <DynamicTabPane
                v-for="(item, index) in productList"
                :key="item.id"
                :index="index"
                prefix="product-item"
                :active-page-id="activePageId">
                <template v-slot:default>
                <div>
                  <DownloadDocPage
                      :key="item.id"
                      :project-id="item.id"
                      :project-type="PROJECT_TYPE.PROJECT.key"
                  ></DownloadDocPage>
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
import { shortDateFormat } from "@/filter/filter";
import ProjectUpdateDlg from "@/components/rdms/project-update-dlg.vue";
import ProductTaskTab from "@/components/rdms/product-task-tab.vue";
import DownloadDocPage from "@/components/rdms/download-doc-page.vue";

export default {
  components: {
    ProductTaskTab,
    ProjectUpdateDlg,
    DynamicTabItem,
    DynamicTabPane,
    WorkspaceFrame,
    DownloadDocPage,
  },
  name: "download",

  data: function() {
    return {
      LOGIN_USER_GROUP: LOGIN_USER_GROUP,
      JOB_ITEM_TYPE: JOB_ITEM_TYPE,
      ENTRANCE_CLASS: ENTRANCE_CLASS,
      PROJECT_TYPE: PROJECT_TYPE,

      activePageId: 'home',
      preActivePageId: 'home',

      archivedProjects: [],
      productList: [],

      loginUser: {},

      projectDlgVisible: false,
      projectItem: {},
    }
  },
  mounted: function() {
    let _this = this;
    _this.getLoginUser();
    _this.getAllProjectArchivedList();

  },

  beforeDestroy: function () {
    let _this = this;
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
      if(row.setupedTime){
        return shortDateFormat(row.setupedTime);
      }else{
        return '-';
      }

    },

    //tab点击事件
    tabProductManagementClick(i){
      let _this = this;
      _this.activePageId = `product-item-${i}`;
    },

    tabDevelopingManagementClick(i){
      let _this = this;
      _this.activePageId = `developing-character-item-${i}`;
    },

    tabHomeClick(){
      let _this = this;
      _this.activePageId = 'home';
      _this.preActivePageId = 'home';
      _this.getAllProjectArchivedList();
    },


    rowClick(row, column, e){
      let _this = this;
      _this.productManagement(row);
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

    getAllProjectArchivedList() {
      let _this = this;
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/project/getAllProjectArchivedListByStaff/' + _this.loginCustomer.id ).then((response)=>{
        let resp = response.data;
        if (resp.success) {
          _this.archivedProjects = resp.content;
        } else {
          Toast.warning(resp.message,Swal);
        }
      })
    },

    closeDlg(){
      let _this = this;
      _this.projectDlgVisible = false;
    },

  }
}
</script>
