<!--
 * Author: 周朔鹏
 * Date: 2025-02-26
 * Email: 391902958@qq.com
 * Website: https://www.euipd.com
-->

<template>
  <div>
    <!--Tab 卡片-->
    <div class="row">
      <div class="col-md-12">
        <div class="tabbable">
          <ul class="nav nav-tabs" id="myTab">
            <li :class="{active : activePageId==='home'}">
              <a @click="tabHomeClick">
                <i class="green ace-icon fa fa-home bigger-120"></i>
                质量反馈
              </a>
            </li>
          </ul>

          <div class="tab-content">
            <div id="home" class="tab-pane fade"
                 :class="{active : activePageId === 'home', in : activePageId === 'home' }">
              <p>质量反馈: 质量反馈是为了方便识别质量问题而提供的一套功能页面.</p>
              <div class="hr hr-18 dotted hr-double"></div>
              <div style="display: flex; justify-content: space-between;">
                <pagination
                    key="quality_recognize"
                    ref="pagination_quality"
                    v-bind:list="list"
                    v-bind:itemCount="6"
                ></pagination>
                <div>
                  <el-tooltip content="提交表单, 对发现的任何研发过程中, 或产品发布以后的所有质量问题进行反馈." placement="top" effect="light" :open-delay=1000>
                    <el-button type="primary" size="small" style="margin-right: 15px;" @click="createQualityFeedback()">质量反馈单</el-button>
                  </el-tooltip>
                </div>
              </div>
              <div>
                <el-table
                    :data="qualityList"
                    border
                    stripe
                    highlight-current-row
                    style="width: 100%"
                    @row-click="rowClick">
                  <el-table-column  label="序号" align="center" width="50">
                    <template slot-scope="scope">
                      {{scope.$index+1}}
                    </template>
                  </el-table-column>
                  <el-table-column
                      prop="qualitySerial"
                      width="180"
                      align="center"
                      label="处理编号"
                  ></el-table-column>
                  <el-table-column
                      prop="name"
                      align="center"
                      show-overflow-tooltip
                      label="问题标题"
                  ></el-table-column>
                  <el-table-column
                      prop="projectName"
                      show-overflow-tooltip
                      align="center"
                      sortable
                      label="关联项目"
                  ></el-table-column>
                  <el-table-column
                      prop="subprojectName"
                      show-overflow-tooltip
                      align="center"
                      sortable
                      label="关联子项目"
                  ></el-table-column>
                  <el-table-column
                      prop="consumer"
                      show-overflow-tooltip
                      align="center"
                      sortable
                      label="需求方名称"
                  ></el-table-column>
                  <el-table-column
                      prop="writerName"
                      sortable
                      align="center"
                      width="110"
                      label="反馈人"
                  ></el-table-column>
                  <el-table-column
                      prop="updateTime"
                      :formatter="updateTimeFormat"
                      align="center"
                      width="110"
                      label="更新时间"
                  ></el-table-column>
                  <el-table-column
                      prop="status"
                      :formatter="statusFormat"
                      align="center"
                      label="处理状态"
                      width="100"
                  ></el-table-column>
                  <el-table-column
                      fixed="right"
                      label="操作"
                      align="center"
                      width="150">
                    <template slot-scope="scope">
                      <el-button v-if="scope.row.status === QUALITY_STATUS.SAVE.key" type="primary" plain size="small" v-on:click.stop="submitQuality(scope.row)">
                        编辑/提交
                      </el-button>
                      <el-button v-if="scope.row.status === QUALITY_STATUS.RECHECK.key" type="primary" plain size="small" v-on:click.stop="recheckQuality(scope.row)">
                        质量复核
                      </el-button>
                      <el-button v-if="scope.row.status === QUALITY_STATUS.REJECT.key" type="primary" plain size="small" v-on:click.stop="qualityDetail(scope.row)">
                        查看进展
                      </el-button>
                    </template>
                  </el-table-column>
                </el-table>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!--    提交质量问题-->
    <el-dialog
        :visible.sync="dialogVisible"
        width="29cm"
        :before-close="handleClose"
        :close-on-click-modal=false>
      <div slot="title" style="font-size: 20px;">{{ dialogTitle }}</div>
      <div v-if="dialogVisible">
        <QualityCreateDlg
            :key="timestamp"
            :quality-item="qualityItem"
            :close-function="closeDialog">
        </QualityCreateDlg>
      </div>
    </el-dialog>

    <!--    质量复核-->
    <el-dialog
        :visible.sync="recheckDialogVisible"
        width="29cm"
        :before-close="handleClose"
        :close-on-click-modal=false>
      <div slot="title" style="font-size: 20px;">{{ dialogTitle }}</div>
      <div v-if="recheckDialogVisible">
        <QualityRecheckDlg
            :key="timestamp"
            :quality-item="qualityItem"
            :close-function="closeDialog">
        </QualityRecheckDlg>
      </div>
    </el-dialog>


    <!--    质量问题详情-->
    <el-dialog
        :visible.sync="qualityDetailVisible"
        width="29cm"
        :before-close="handleClose"
        :close-on-click-modal=false>
      <div v-if="qualityDetailVisible">
        <QualityDetailPlain
            :key="qualityId"
            :quality-id="qualityId">
        </QualityDetailPlain>
      </div>
    </el-dialog>

  </div>
</template>

<script>
import TabButton from "@/components/rdms/tab-button";
import Swal from "sweetalert2";
import Pagination from "@/components/pagination.vue";
import {Loading} from "element-ui";
import {formatSecond, optionKV, shortDateFormat} from "@/filter/filter";
import JobitemOwnInfoPage from "@/components/std/jobitem-own-info-page.vue";
import QualityCreateDlg from "@/components/rdms/quality-create-dlg.vue";
import QualityDetailPlain from "@/components/rdms/quality-detail-plain.vue";
import QualityRecheckDlg from "@/components/rdms/quality-recheck-dlg.vue";

export default {
  components: {
    QualityDetailPlain,
    QualityCreateDlg,
    Pagination,
    TabButton,
    JobitemOwnInfoPage,
    QualityRecheckDlg,
  },
  name: "qualitydefine",
  data: function () {
    return {
      loginUser: {},
      loginCustomer: {},
      jobItem: {},
      jobitemShowList: [],

      qualityList: [],
      QUALITY_STATUS: QUALITY_STATUS,

      activePageId: 'home',
      historyPageId: 'home',

      dialogVisible: false,
      recheckDialogVisible: false,

      qualityDetailVisible: false,
      timestamp: '',
      qualityItem: null,
      qualityId: '',

      essentialAnalysisItems: [],
      PROJECT_TYPE: PROJECT_TYPE,
      ENTRANCE_CLASS: ENTRANCE_CLASS,

      modifyJobitemDlg: false,
      dialogTitle: '',
    }
  },
  mounted: function () {
    let _this = this;
    _this.$refs.pagination_quality.size = 6;
    // _this.$refs.pagination_manage.size = 6;
    _this.getLoginUser();
    _this.list(1);

    _this.$bus.$on('refresh-quality-define-list', (data) => {
      if(data){
        _this.list(1);
      }
    });
  },

  beforeDestroy() {
    let _this = this;
    _this.$bus.$off('refresh-quality-define-list');
  },

  methods: {
    createQualityFeedback() {
      let _this = this;
      _this.timestamp = new Date().getTime();
      _this.dialogVisible = true;
      _this.qualityItem = null;
      _this.dialogTitle = "质量反馈单";
    },

    submitQuality(quality) {
      let _this = this;
      _this.timestamp = new Date().getTime();
      _this.dialogVisible = true;
      _this.qualityItem = quality;
      _this.dialogTitle = "质量反馈单";
    },

    recheckQuality(quality) {
      let _this = this;
      _this.timestamp = new Date().getTime();
      _this.recheckDialogVisible = true;
      _this.qualityItem = quality;
      _this.dialogTitle = "质量复核";
    },


    rowClick(row, column, e){
      let _this = this;
      if(row.status === QUALITY_STATUS.RECHECK.key){
        _this.recheckQuality(row);
      }else{
        _this.qualityDetail(row);
      }
    },

    qualityDetail(quality) {
      let _this = this;
      _this.timestamp = new Date().getTime();
      _this.qualityDetailVisible = true;
      _this.qualityItem = quality;
      _this.qualityId = quality.id;
      _this.dialogTitle = "质量反馈单";
    },

    handleClose(done) {
      done();
    },

    closeDialog() {
      let _this = this;
      _this.dialogVisible = false;
      _this.recheckDialogVisible = false;
      _this.qualityDetailVisible = false;
    },

    statusFormat(row) {
      return optionKV(QUALITY_STATUS, row.status);
    },

    updateTimeFormat(row, column){
      return shortDateFormat(row.updateTime);
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

    list(page) {
      let _this = this;
      let loadingInstance = Loading.service({ fullscreen: true });
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/quality/list', {
        page: page,
        size: _this.$refs.pagination_quality.size,
        customerId: _this.loginCustomer.id, //customerId
        actor: _this.loginUser.customerUserId,
      }).then((response)=>{
        loadingInstance.close();
        let resp = response.data;
        if (resp.success) {
          _this.qualityList = resp.content.list;
          _this.$refs.pagination_quality.render(page, resp.content.total);
        } else {
          Toast.warning(resp.message,Swal);
        }
      })
    },

    tabHomeClick() {
      let _this = this;
      _this.activePageId = 'home';
      _this.historyPageId = 'home';
    },

  }
}
</script>
