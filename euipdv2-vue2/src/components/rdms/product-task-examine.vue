<!--
 * Author: 周朔鹏
 * Date: 2024-05-26
 * Email: 391902958@qq.com
 * Website: https://www.euipd.com
-->

<!-- -->
<template>
  <div class="tab-container">
    <div class="tab-left">
      <pagination-small ref="pagination" v-bind:list="getProjectAndTaskJobitems_submit" v-bind:itemCount="3"></pagination-small>
      <div style="height: calc(100vh - 278px); overflow: scroll; overflow-x: hidden">
        <el-table
            ref="examineItemTable"
            :data="projectJobitemMix"
            row-key="id"
            border
            default-expand-all
            stripe
            style="width: 100%;"
            highlight-current-row
            :tree-props="{children: 'children', hasChildren: 'hasChildren'}"
            @row-click="rowClick">
          <el-table-column   label="序号" align="center" width="80">
            <template slot-scope="scope" v-if="scope.row.docType === DOC_TYPE.JOBITEM.key">
              {{ scope.row.index  }}
            </template>
          </el-table-column>
          <el-table-column
              prop="itemName"
              label="项目/任务任务名称"
              show-overflow-tooltip
              width="350">
          </el-table-column>
          <el-table-column
              fixed="right"
              label="操作"
              align="center"
              width="102">
            <template slot-scope="scope">
              <div v-if="scope.row.docType === DOC_TYPE.PROJECT.key">
                完工清单
              </div>
              <div v-else>
                {{ JOB_ITEM_STATUS | optionKV(scope.row.status) }}
              </div>
            </template>
          </el-table-column>
          <el-table-column
              fixed="right"
              width="35">
            <template slot-scope="scope">
              <i v-show="scope.row.id === currentRow.id" class="el-icon-caret-right" style="font-size: 24px; color: #4794EF;"></i>
            </template>
          </el-table-column>
        </el-table>
      </div>

    </div>
    <div class="tab-right">
      <div v-if="currentRow && currentRow.docType ===  DOC_TYPE.PROJECT.key">
        <div>
          <jobitem-completed-list
              :key="currentRow.id"
              :x-id="currentRow.id"
              :doc-type="currentRow.docType"
              :job-type="null"
              :project-type="projectType"
              :index="index"
              :entrance="entrance"
          ></jobitem-completed-list>
        </div>
      </div>
      <div v-if="currentRow && currentRow.docType === DOC_TYPE.JOBITEM.key">
        <JobitemExaminePage
            :key="currentRow.id"
            :jobitem-id="currentRow.id"
            :job-type="currentRow.type"
            :inherit-job-type="jobType"
            :project-type="currentRow.projectType"
            :index="index"
            :entrance="entrance"
        ></JobitemExaminePage>
      </div>
    </div>

  </div>
</template>

<script>
import File from "../../components/rdms/file";
import Swal from "sweetalert2";
import PaginationSmall from "@/components/pagination-small";
import JobitemDetailInfoPage from "@/components/std/jobitem-detail-info-page.vue";
import JobitemExaminePage from "@/components/std/jobitem-examine-page.vue";
import JobitemCompletedList from "@/components/std/jobitem-completed-list.vue";

import { optionKV } from "@/filter/filter";
import JobitemCreateByProjectIdDlg from "@/components/std/jobitem-create-by-project-id-dlg.vue";

export default {
  components: {
    JobitemCreateByProjectIdDlg,
    File,
    PaginationSmall,
    JobitemDetailInfoPage,
    JobitemExaminePage,
    JobitemCompletedList,
  },
  name: "product-task-examine",

  props: {
    projectId: {
      default: null,
    },
    jobType: {
      default: null,
    },
    projectType: {
      default: null,
    },
    index: {
      default: null,
    },
    entrance: {
      default: null,
    },
  },

  data: function () {
    return {
      loginUser: {},
      loginCustomer: {},

      jobitemId: {
        default: null,
      },
      projectJobitemMix: [],

      PARENT_CHILDREN: PARENT_CHILDREN,
      DOC_TYPE: DOC_TYPE,
      FILE_GROUPING: FILE_GROUPING,
      JOB_ITEM_TYPE: JOB_ITEM_TYPE,
      PROJECT_TYPE: PROJECT_TYPE,
      ENTRANCE_CLASS: ENTRANCE_CLASS,
      JOB_ITEM_STATUS: JOB_ITEM_STATUS,
      PROJECT_STATUS: PROJECT_STATUS,
      currentRow: {},
      dlgProject: {},

    }
  },

  computed: {},

  mounted: function () {
    let _this = this;
    _this.getLoginUser();
    if(Tool.isNotEmpty(_this.projectId) ){
      _this.getProjectAndTaskJobitems_submit(_this.projectId);
    }

    _this.$bus.$on('refresh-product-task-examine', (data) => {
      if(data){
        _this.getProjectAndTaskJobitems_submit(_this.projectId);
      }
    });
  },

  beforeDestroy(){
    let _this = this;
    _this.$bus.$off('refresh-product-task-examine');
  },

  methods: {

    docTypeFormat(row, column) {
      return optionKV(DOC_TYPE, row.docType);
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

    rowClick(row, column, e){
      let _this = this;
      _this.currentRow = row;
    },

    getProjectAndTaskJobitems_submit(projectId) {
      let _this = this;
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/project/getProjectAndTaskJobitems_submit/' + projectId).then((response)=>{
        let resp = response.data;
        if (resp.success) {
          let respItemMix = resp.content;
          let respItems = [];
          respItems.push(respItemMix);
          _this.projectJobitemMix =respItems;
          if(Tool.isNotEmpty_ES5( _this.projectJobitemMix)){
            if(Tool.isNotEmpty(_this.projectJobitemMix[0].children)){
              _this.currentRow = _this.projectJobitemMix[0].children[0];
            }else{
              _this.currentRow = _this.projectJobitemMix[0];
            }
          }else{
            _this.currentRow = _this.projectJobitemMix[0];
          }
          _this.$refs.examineItemTable.setCurrentRow(_this.currentRow);
          _this.hasIntJobitem = respItemMix.hasIntJobitem;
        } else {
          Toast.warning(resp.message,Swal);
        }
      })
    },

  },
}
</script>

<style lang="scss" scoped>
@import './../../../public/static/scss/caipd.scss';

</style>
