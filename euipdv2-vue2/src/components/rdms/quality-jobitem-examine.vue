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
      <div style="height: calc(100vh - 278px); overflow: scroll; overflow-x: hidden">
        <el-table
            ref="examineItemTable"
            :data="qualityJobitemMix"
            row-key="id"
            border
            default-expand-all
            stripe
            style="width: 100%;"
            highlight-current-row
            :tree-props="{children: 'children', hasChildren: 'hasChildren'}"
            @row-click="rowClick">
          <el-table-column   label="序号" align="center" width="70">
            <template slot-scope="scope" v-if="scope.row.docType === DOC_TYPE.JOBITEM.key">
              {{ scope.row.index }}
            </template>
          </el-table-column>
          <el-table-column
              prop="itemName"
              label="特性/任务名称"
              width="300">
          </el-table-column>
          <el-table-column
              fixed="right"
              label="操作/状态"
              align="center"
              width="102">
            <template slot-scope="scope">
              <div v-if="scope.row.docType === DOC_TYPE.QUALITY.key">
                <div v-if="!( scope.row.status === CHARACTER_STATUS.ARCHIVED.key || scope.row.status === CHARACTER_STATUS.REVIEW.key || scope.row.status === CHARACTER_STATUS.DEV_COMPLETE.key )">
                  <div>
                    完工清单
                  </div>
                </div>
                <div v-else>
                  {{ CHARACTER_STATUS | optionKV(scope.row.status) }}
                </div>
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
              <i v-if="currentRow && currentRow.id && scope.row.id === currentRow.id" class="el-icon-caret-right" style="font-size: 24px; color: #4794EF;"></i>
            </template>
          </el-table-column>
        </el-table>
      </div>

    </div>
    <div class="tab-right">
      <div v-if="currentRow && currentRow.docType === DOC_TYPE.QUALITY.key">
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
        <QualityExaminePage
            :key="currentRow.id"
            :jobitem-id="currentRow.id"
            :job-type="currentRow.type"
            :inherit-job-type="jobType"
            :project-type="currentRow.projectType"
            :index="index"
            :entrance="entrance"
        ></QualityExaminePage>
      </div>
    </div>

    <!--提交系统集成任务-->
    <el-dialog
        width="29cm"
        :visible.sync="issueIntegrationDlg"
        :before-close="handleClose"
        :close-on-click-modal=false>
      <div slot="title" style="font-size: 20px;">{{ dialogTitle }}</div>
      <div>
        <JobitemCreateByCharacterIdDlg
            :key="characterId"
            :character-id="characterId"
            :job-type="JOB_ITEM_TYPE.CHARACTER_INT.key"
            :project-type="projectType"
            :index="index"
            :entrance="entrance"
            :close-dlg=closeDialog
        ></JobitemCreateByCharacterIdDlg>
      </div>
    </el-dialog>

  </div>
</template>

<script>
import File from "../../components/rdms/file";
import Swal from "sweetalert2";
import JobitemDetailInfoPage from "@/components/std/jobitem-detail-info-page.vue";
import JobitemExaminePage from "@/components/std/jobitem-examine-page.vue";
import JobitemCompletedList from "@/components/std/jobitem-completed-list.vue";

import JobitemCreateByCharacterIdDlg from "@/components/std/jobitem-create-by-character-id-dlg.vue";
import QualityExaminePage from "@/components/std/quality-examine-page.vue";

export default {
  components: {
    JobitemCreateByCharacterIdDlg,
    File,
    JobitemDetailInfoPage,
    JobitemExaminePage,
    JobitemCompletedList,
    QualityExaminePage,
  },
  name: "quality-jobitem-examine",

  props: {
    qualityId: {
      default: null,
    },
    characterId: {
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

      qualityJobitemMix: [],

      DOC_TYPE: DOC_TYPE,
      CHARACTER_STATUS: CHARACTER_STATUS,
      JOB_ITEM_STATUS: JOB_ITEM_STATUS,
      JOB_ITEM_TYPE: JOB_ITEM_TYPE,

      currentRow: {},

      sumJobNum: 0,
      completeJobNum: 0,
      completeFlag: false,

      dialogTitle: '',
      issueIntegrationDlg: false,

      hasIntJobitem: false,
    }
  },

  computed: {},

  mounted: function () {
    let _this = this;
    _this.getLoginUser();
    if(Tool.isNotEmpty(_this.qualityId) ){
      _this.getQualityAndSubmitJobitems(_this.qualityId);
    }

    _this.$bus.$on('refresh-quality-jobitem-examine', (data) => {
      if(data){
        _this.getQualityAndSubmitJobitems(_this.qualityId);
      }
    });
  },

  beforeDestroy(){
    let _this = this;
    _this.$bus.$off('refresh-quality-jobitem-examine');
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

    rowClick(row, column, e){
      let _this = this;
      _this.currentRow = row;
    },

    getQualityAndSubmitJobitems(qualityId) {
      let _this = this;
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/quality/getQualityAndSubmitJobitems/' + qualityId).then((response)=>{
        let resp = response.data;
        if (resp.success) {
          let respItemMix = resp.content;
          let respItems = [];
          respItems.push(respItemMix);
          _this.qualityJobitemMix =respItems;
          if(Tool.isNotEmpty_ES5(_this.qualityJobitemMix)){
            if(Tool.isNotEmpty(_this.qualityJobitemMix[0].children)){
              if(_this.qualityJobitemMix[0].children.length > 0){
                _this.currentRow = _this.qualityJobitemMix[0].children[0];
              }else{
                _this.currentRow = _this.qualityJobitemMix[0];
              }
            }else{
              _this.currentRow = _this.qualityJobitemMix[0];
            }
          }else{
            _this.currentRow = _this.qualityJobitemMix[0];
          }
          _this.$refs.examineItemTable.setCurrentRow(_this.currentRow);

          _this.hasIntJobitem = respItemMix.hasIntJobitem;

        } else {
          Toast.warning(resp.message,Swal);
        }
      })
    },

    getCompletedJobitemList(characterId) {
      let _this = this;
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/job-items/getCompletedJobitemList/' + characterId ).then((response) => {
        let resp = response.data;
        if (resp.success) {
          _this.sumJobNum = resp.content.sumJobNum;
          _this.completeJobNum = resp.content.completeJobNum;
          _this.completeFlag = resp.content.completeFlag;

        } else {
          Toast.warning(resp.message, Swal);
        }
      })
    },

    integrationJobitem() {
      let _this = this;
      _this.issueIntegrationDlg = true;
      _this.dialogTitle = "特性集成";
    },

    closeDialog() {
      let _this = this;
      _this.issueIntegrationDlg = false;
    },
    handleClose(done) {
      done();
    },
  },
}
</script>

<style lang="scss" scoped>
@import './../../../public/static/scss/caipd.scss';

</style>
