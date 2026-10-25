<!--
 * Author: 周朔鹏
 * Date: 2024-11-05
 * Email: 391902958@qq.com
 * Website: https://www.euipd.com
-->


<template>
  <div class="tab-container">
    <div class="tab-left">
      <div>
        <el-table
            ref="recheckItemTable"
            :data="items"
            row-key="id"
            border
            default-expand-all
            stripe
            style="width: 100%; "
            highlight-current-row
            :tree-props="{children: 'childrenList', hasChildren: 'hasChildren'}"
            @row-click="rowClick">
          <el-table-column
              prop="characterName"
              label="特性名称"
              width="300">
          </el-table-column>
          <el-table-column
              fixed="right"
              align="center"
              prop="docType"
              label="文件类型"
              width="70">
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
      <div class="hr hr-10 dotted hr-double"></div>
      <div style="text-align: right; margin-right: 15px;"><small><em>注意: 复核通过或不通过是对所有修订或分解记录的统一操作.</em></small></div>
      <div style="display: flex; flex-direction: row; justify-content: end; padding: 10px 20px;">
        <el-button type="primary" @click="approveDlg">复核处理</el-button>
      </div>
    </div>
    <div class="tab-right">
      <div v-if="items.length > 0 ">
        <CharacterDetailInfoPage
            :key="currentRow.id + 'character'"
            :character-id="currentRow.id"
            :doc-type="DOC_TYPE.CHARACTER.key"
            :project-type="PROJECT_TYPE.DEV_PROJECT.key"
        ></CharacterDetailInfoPage>
      </div>
    </div>

    <!-- 复核：通过 / 不通过 -->
    <el-dialog
        title="复核处理"
        width="29cm"
        :visible.sync="jobItemDlgVisible_approve"
        :close-on-click-modal=false>
      <div>
        <JobitemRecheckCharacterApproveDlg
            :key="jobitemId"
            :jobitem-id="jobitemId"
            :job-type="jobType"
            :project-type="projectType"
            :index="index"
            :entrance="entrance"
            :close-dlg = closeCharacterApproveDlg>
        </JobitemRecheckCharacterApproveDlg>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import File from "../../components/rdms/file";
import Swal from "sweetalert2";
import PaginationSmall from "@/components/pagination-small";
import JobitemDetailInfoPage from "@/components/std/jobitem-detail-info-page.vue";
import CharacterDetailExaminePage from "@/components/rdms/character-detail-examine-page";

import { optionKV } from "@/filter/filter";
import CharacterDetailInfoCore from "@/components/std/character-detail-info-core.vue";
import CharacterDetailInfoPage from "@/components/rdms/character-detail-info-page.vue";
import CharacterDetailApproveDlg from "@/components/rdms/character-detail-approve-dlg.vue";
import JobitemRecheckCharacterApproveDlg from "@/components/rdms/jobitem-recheck-character-approve-dlg.vue";

export default {
  components: {
    JobitemRecheckCharacterApproveDlg,
    CharacterDetailApproveDlg,
    CharacterDetailInfoPage,
    CharacterDetailInfoCore,
    File,
    PaginationSmall,
    JobitemDetailInfoPage,
    CharacterDetailExaminePage,
  },
  name: "jobitem-character-recheck-container",

  props: {
    jobitemId: {
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
      jobItem: {},
      characterId: '',
      projectId: {
        default: null,
      },

      operator: '',
      status: '',
      ACTOR_TYPE: ACTOR_TYPE,
      CHARACTER_STATUS: CHARACTER_STATUS,
      JOB_ITEM_TYPE: JOB_ITEM_TYPE,
      DOC_TYPE: DOC_TYPE,
      PROJECT_TYPE: PROJECT_TYPE,

      items: [],
      currentRow: {},

      jobItemDlgVisible_approve: false,
    }
  },

  computed: {},

  mounted: function () {
    let _this = this;
    _this.getLoginUser();

    if(Tool.isNotEmpty(_this.jobitemId)){
      _this.getJobitemSimpleInfo(_this.jobitemId);
      _this.getCharacterReCheckJobitems(_this.jobitemId);
    }

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

    getJobitemSimpleInfo(jobitemId){
      let _this = this;
      let formData = new window.FormData();
      formData.append('jobitemId', jobitemId);
      formData.append('loginUserId', _this.loginUser.customerUserId);
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/job-items/getJobitemSimpleInfo', formData).then((response) => {
        let resp = response.data;
        if (resp.success) {
          _this.jobItem = resp.content;
        }else{
          Toast.warning(resp.message, Swal);
        }
      });
    },

    getCharacterReCheckJobitems(jobitemId) {
      let _this = this;
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/job-items/getReCheckCharactersByJobitemId/' + jobitemId).then((response)=>{
        let resp = response.data;
        if (resp.success) {
          _this.items = resp.content;
          if(_this.items){
            _this.currentRow = _this.items[0];
            _this.characterId = _this.items[0].id;
            _this.$refs.recheckItemTable.setCurrentRow(_this.currentRow);
          }
        } else {
          Toast.warning(resp.message,Swal);
        }
      })
    },

    approveDlg() {
      let _this = this;
      _this.jobItemDlgVisible_approve = true;
    },
    closeCharacterApproveDlg(){
      let _this = this;
      _this.jobItemDlgVisible_approve = false;
    },

  },
}
</script>

<style lang="scss" scoped>
@import './../../../public/static/scss/caipd.scss';

</style>
