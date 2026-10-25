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
      <pagination-small ref="pagination" v-bind:list="getTestingJobitemList" v-bind:itemCount="3"></pagination-small>
      <div style="height: calc(100vh - 278px); overflow: scroll; overflow-x: hidden">
        <el-table
            ref="editingItemTable"
            :data="items"
            row-key="id"
            border
            default-expand-all
            stripe
            style="width: 100%;"
            highlight-current-row
            :tree-props="{children: 'children', hasChildren: 'hasChildren'}"
            @row-click="rowClick">
          <el-table-column   label="序号" align="center" width="80">
            <template slot-scope="scope" v-if="scope.row.showFlag === 'character'">
              {{(scope.row.showFlag === 'character') ?  scope.row.index : ''}}
            </template>
          </el-table-column>
          <el-table-column
              prop="itemName"
              label="特性/任务名称"
              width="300">
          </el-table-column>
          <el-table-column
              fixed="right"
              label="分类信息"
              align="center"
              width="102">
            <template slot-scope="scope">
              <div v-if="scope.row.showFlag === 'character'">
                *功能详情*
              </div>
              <div v-else>
                在测任务
              </div>
            </template>
          </el-table-column>
          <el-table-column
              fixed="right"
              width="35">
            <template slot-scope="scope">
              <i v-if="scope.row.id === currentRow.id" class="el-icon-caret-right" style="font-size: 24px; color: #4794EF;"></i>
            </template>
          </el-table-column>
        </el-table>
      </div>

    </div>
    <div class="tab-right">
      <div v-if="items.length > 0 && recordItem.docType === DOC_TYPE.CHARACTER.key">
        <develop-character-integration-doc-page
            :key="('recordItem.id-' + (Math.random()))"
            :character-id="recordItem.id"
            :doc-type="recordItem.docType"
        ></develop-character-integration-doc-page>
      </div>
      <div v-if="items.length > 0 && recordItem.docType === DOC_TYPE.JOBITEM.key">
        <JobitemDetailInfoPage
            :key="recordItem.id + 'jobitem'"
            :jobitem-id="recordItem.id"
            :doc-type="recordItem.docType"
        ></JobitemDetailInfoPage>
      </div>
    </div>

  </div>
</template>

<script>
import File from "../../components/rdms/file";
import Swal from "sweetalert2";
import PaginationSmall from "@/components/pagination-small";
import JobitemDetailInfoPage from "@/components/std/jobitem-detail-info-page.vue";
import DevelopCompletedList from "@/components/rdms/develop-completed-list";
import DevelopCharacterIntegrationDocPage from "@/components/rdms/develop-character-integration-doc-page";


import {optionKV, shortDateFormat} from "@/filter/filter";

export default {
  components: {
    File,
    PaginationSmall,
    JobitemDetailInfoPage,
    DevelopCompletedList,
    DevelopCharacterIntegrationDocPage,
  },
  name: "develop-joblist-jobitem-testing",

  props: {
    subProjectId: {
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
      lineIndex: 0,

      // directJobItemDlg: false,
      dialogTitle: '',

      operator: '',
      status: '',
      ACTOR_TYPE: ACTOR_TYPE,
      CHARACTER_STATUS: CHARACTER_STATUS,
      JOB_ITEM_TYPE: JOB_ITEM_TYPE,
      DOC_TYPE: DOC_TYPE,
      PROJECT_TYPE: PROJECT_TYPE,

      items: [],
      currentRow: {},
      recordItem: {},
      dlgCharacterItem: {},

    }
  },

  computed: {},

  mounted: function () {
    let _this = this;
    _this.getLoginUser();

    _this.$refs.pagination.size = 12;

    if(Tool.isNotEmpty(_this.subProjectId)){
      _this.getTestingJobitemList(1);
    }

    _this.$bus.$on('refresh-develop-joblist-jobitem-testing', (data) => {
      if(data){
        _this.getTestingJobitemList(1);
      }
    });
  },

  beforeDestroy(){
    let _this = this;
    _this.$bus.$off('refresh-develop-joblist-jobitem-testing');
  },

  methods: {
/*    directJobItem(character) {
      let _this = this;
      _this.dlgCharacterItem = character;
      _this.directJobItemDlg = true;
      _this.dialogTitle = "开发任务";
    },
    closeDialog() {
      let _this = this;
      _this.directJobItemDlg = false;
    },
    handleClose(done) {
      done(); //el-dialog用于关闭对话框的内置函数.
    },*/

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
      if(row.docType === DOC_TYPE.JOBITEM.key){
        _this.currentRow = row; //用于显示当前行的状态
        _this.recordItem = row;
      }
      if(row.docType === DOC_TYPE.CHARACTER.key){
        _this.currentRow = row; //用于显示当前行的状态
        _this.recordItem = row;
      }

    },

    getTestingJobitemList(page) {
      let _this = this;
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/character/getTestingJobitemList',{
        page: page,
        size: _this.$refs.pagination.size,
        actor: _this.loginUser.customerUserId,
        keyWord: _this.subProjectId,
        type: PROJECT_TYPE.SUB_PROJECT.key,
      }).then((response)=>{
        let resp = response.data;
        if (resp.success) {
          _this.items = resp.content.list;
          if(_this.items.length > 0){
            _this.currentRow = _this.items[0];
            _this.$refs.editingItemTable.setCurrentRow(_this.currentRow);
            _this.recordItem = _this.currentRow;
            _this.$refs.pagination.render(page, resp.content.total);
          }
          _this.$bus.$emit('refresh-develop-joblist-jobitem-testing', false);
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
