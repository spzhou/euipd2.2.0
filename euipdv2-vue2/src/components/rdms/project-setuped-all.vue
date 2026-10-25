<!--
 * Author: 周朔鹏
 * Date: 2025-02-28
 * Email: 391902958@qq.com
 * Website: https://www.euipd.com
-->

<template>
  <div>
    <el-row>
      <el-col :span="24">
        <div style="display: flex; justify-content: flex-start;">
          <pagination ref="pagination_setuped_all" v-bind:list="setupedList" v-bind:itemCount="6"></pagination>
          <div style="margin-left: 30px;">
            <el-input size="mini" placeholder="请输入项目名称关键词" v-model="keyWord">
              <el-button slot="append" icon="el-icon-search" @click="setupedList(1)"></el-button>
            </el-input>
          </div>
        </div>
      </el-col>
    </el-row>
    <el-table
        :data="setupedProjects"
        border
        stripe
        highlight-current-row
        style="width: 100%"
        @row-click="rowClick_setuped">
      <el-table-column  label="序号" align="center" width="50">
        <template slot-scope="scope">
          {{scope.$index+1}}
        </template>
      </el-table-column>
      <el-table-column
          prop="projectCode"
          align="center"
          label="项目编号"
          width="150"
          show-overflow-tooltip
      ></el-table-column>
      <el-table-column
          prop="projectName"
          align="center"
          label="项目名称"
          min-width="200"
          show-overflow-tooltip
      ></el-table-column>
      <el-table-column
          prop="productManagerName"
          align="center"
          label="产品经理"
          sortable
          min-width="90"
          show-overflow-tooltip
      ></el-table-column>
      <el-table-column
          prop="projectManagerName"
          align="center"
          label="项目经理"
          sortable
          min-width="90"
          show-overflow-tooltip
      ></el-table-column>
      <el-table-column
          prop="testManagerName"
          align="center"
          label="测试主管"
          sortable
          min-width="90"
          show-overflow-tooltip
      ></el-table-column>
      <el-table-column
          prop="superviseName"
          align="center"
          label="监管委员"
          min-width="90"
          show-overflow-tooltip
      ></el-table-column>
      <el-table-column
          prop="rdType"
          align="center"
          :formatter="rdTypeFormat"
          label="研发类型"
          min-width="100"
          show-overflow-tooltip
      ></el-table-column>
      <el-table-column
          prop="createTimeStr"
          label="立项时间"
          sortable
          align="center"
          min-width="100"
          show-overflow-tooltip
      ></el-table-column>
      <el-table-column
          prop="releaseTimeStr"
          label="发布时间"
          sortable
          align="center"
          min-width="100"
          show-overflow-tooltip
      ></el-table-column>
      <el-table-column
          fixed="right"
          label="操作"
          align="center"
          width="440">
        <template slot-scope="scope">
          <el-tooltip content="显示对应项目进度仪表盘" placement="top" effect="light" :open-delay=1000>
            <el-button type="primary" plain size="small" v-on:click.stop="summaryProject_setuped(scope.row)">
              仪表盘
            </el-button>
          </el-tooltip>
          <el-tooltip content="对预立项项目信息进行编辑" placement="top" effect="light" :open-delay=1000>
            <el-button type="primary" plain size="small" v-on:click.stop="detailProject_setuped(scope.row)">
              概要
            </el-button>
          </el-tooltip>
          <el-tooltip content="该开发版本下全部特性的版本演进关系" placement="top" effect="light" :open-delay=1000>
            <el-button type="primary" plain size="small" v-on:click.stop="showLineage(scope.row)">
              演进
            </el-button>
          </el-tooltip>
          <el-tooltip v-permission="'520101'" content="对预立项项目信息进行编辑" placement="top" effect="light" :open-delay=1000>
            <el-button type="primary" plain size="small" v-on:click.stop="editProject(scope.row)">
              编辑
            </el-button>
          </el-tooltip>
          <el-tooltip v-permission="'520101'" content="对预立项项目信息进行编辑" placement="top" effect="light" :open-delay=1000>
            <el-button type="primary" plain size="small" v-on:click.stop="addCategoryItem(scope.row)">
              分类
            </el-button>
          </el-tooltip>
          <el-tooltip v-permission="'520102'" content="项目关闭有讲无法在任何管理窗口中显示" placement="top" effect="light" :open-delay=1000>
            <el-button type="warning" plain size="small" v-on:click.stop="abandonProject(scope.row)">
              废弃
            </el-button>
          </el-tooltip>
        </template>
      </el-table-column>
    </el-table>

    <!--添加分类-->
    <el-dialog
        width="23cm"
        append-to-body
        :visible.sync="addCategoryItemDlg"
        :before-close="handleClose"
        :close-on-click-modal=false>
      <div slot="title" style="font-size: 20px;">{{ dialogTitle }}</div>
      <div>
        <EditProjectCategory
            :key="opeProject.id"
            :project-id="opeProject.id"
            :close-dlg="closeDialog"
        ></EditProjectCategory>
      </div>
    </el-dialog>

    <!--特性版本演进-->
    <el-dialog
        width="32cm"
        append-to-body
        :title="'特性版本演进：' + (lineageProject.projectName || '')"
        :visible.sync="lineageDlg"
        :close-on-click-modal=false>
      <RichLineageGraph v-if="lineageDlg" url="/customer/project/rich-doc/get-lineage"
                        :params="{ projectId: lineageProject.id }" :default-include-work="false"></RichLineageGraph>
    </el-dialog>
  </div>
</template>

<script>
import Pagination from "@/components/pagination";
import {optionKV} from "@/filter/filter";
import { Loading } from 'element-ui'
import EditProjectCategory from "@/components/rdms/edit-project-category.vue";
import RichLineageGraph from "@/components/rich/rich-lineage-graph.vue";

export default {
  components: {
    EditProjectCategory,
    RichLineageGraph,
    Pagination,

  },
  name: "project-setuped-all",

  data: function() {
    return {
      loginUser: {},
      loginCustomer: {},
      keyWord: '',
      setupedProjects: [], //预立项项目列表

      addCategoryItemDlg: false,
      dialogTitle: 'Dialog',
      opeProject: {},
      lineageDlg: false,
      lineageProject: {},
    }
  },

  props: {
    categoryId: {
      default: null
    },
    detailProject_setuped: {
      type: Function,
      default: null
    },
    summaryProject_setuped: {
      type: Function,
      default: null
    },
    editProject: {
      type: Function,
      default: null
    },
    abandonProject: {
      type: Function,
      default: null
    },

  },

  computed: {
  },

  mounted: function() {
    let _this = this;
    _this.getLoginUser();

    _this.$refs.pagination_setuped_all.size = 12;
    _this.setupedList(1);

    _this.$bus.$on('refresh-project-setuped-all', (data)=>{
      if(data){
        _this.setupedList(1);
      }
    });
  },

  beforeDestroy() {
    let _this = this;
    _this.$bus.$off('refresh-project-setuped-all');
  },

  methods: {
    showLineage(project){
      this.lineageProject = project;
      this.lineageDlg = true;
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

    setupedList(page) {
      let _this = this;
      let loadingInstance = Loading.service({ fullscreen: true });
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/project/list-setuped-project', {
        page: page,
        size: _this.$refs.pagination_setuped_all.size,
        customerId: _this.loginCustomer.id, //customerId
        actor: _this.loginUser.customerUserId,
        keyWord: _this.keyWord,
        flag: _this.categoryId,
      }).then((response) => {
        loadingInstance.close();
        let resp = response.data;
        if (resp.success) {
          _this.setupedProjects = resp.content.list;
          _this.$refs.pagination_setuped_all.render(page, resp.content.total);
        } else {
          Toast.warning(resp.message, Swal);
        }
      })
    },

    rowClick_setuped(row){
      let _this = this;
      _this.detailProject_setuped(row);
    },

    rdTypeFormat(row, column) {
      return optionKV(RD_TYPE, row.rdType);
    },

    addCategoryItem(row, column) {
      let _this = this;
      _this.opeProject = row;
      _this.addCategoryItemDlg = true;
      _this.dialogTitle = "编辑项目分类:" + row.projectName;
    },

    handleClose(done) {
      done();
    },

    closeDialog() {
      let _this = this;
      _this.addCategoryItemDlg = false;
    },


  }
}
</script>

<style lang="scss">
@import './../../../public/static/scss/caipd.scss';
</style>
