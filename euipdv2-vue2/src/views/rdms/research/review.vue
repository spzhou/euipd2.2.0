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
                评审列表
              </a>
            </li>

            <!--评审管理-->
            <DynamicTabItem
                v-for="(item, index) in review_jobitem"
                :key="('review_jobitem-tab-' + (item.id))"
                :index="index"
                :active-page-id="activePageId"
                prefix="review_jobitem-item"
                :close="closeFunction_jobitem"
                @select="tabJobItemClick">{{ ('评审管理 : ' + (item.jobName)) }}
            </DynamicTabItem>
          
        </template>

                        <DynamicTabPane
                page-id="home"
                :active-page-id="activePageId">
                            <template v-slot:default>
              <p>评审管理: 是为<strong>评审负责人</strong>提供的一个进行评审任务管理的一套功能页面.</p>
              <div class="hr hr-18 dotted hr-double"></div>
              <div>
                <div style="display: flex; justify-content: space-between;">
                  <pagination ref="pagination" v-bind:list="list" v-bind:itemCount="6"></pagination>
                  <div>
                  <!--  这里可以放置按钮-->
                  </div>
                </div>
                <el-table
                    :data="reviewItems"
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
                      prop="jobName"
                      label="评审标题"
                      align="center"
                      min-width="180"
                      show-overflow-tooltip
                  ></el-table-column>
                  <el-table-column
                      prop="type"
                      :formatter="JobitemTypeFormatter"
                      label="类型"
                      align="center"
                      min-width="100"
                      show-overflow-tooltip
                  ></el-table-column>
                  <el-table-column
                      prop="characterName"
                      label="特性名称"
                      align="center"
                      min-width="180"
                      show-overflow-tooltip
                  ></el-table-column>
                  <el-table-column
                      prop="subprojectName"
                      label="子项目名称"
                      align="center"
                      min-width="180"
                      show-overflow-tooltip
                  ></el-table-column>
                  <el-table-column
                      prop="projectName"
                      label="项目名称"
                      align="center"
                      min-width="180"
                      show-overflow-tooltip
                  ></el-table-column>
                  <el-table-column
                      prop="projectManagerName"
                      label="项目经理"
                      align="center"
                      width="100"
                  ></el-table-column>
                  <el-table-column
                      prop="createTime"
                      :formatter="dateFormat"
                      label="提交时间"
                      align="center"
                      width="160"
                  ></el-table-column>
                  <el-table-column
                      prop="status"
                      :formatter="JobitemStatusFormatter"
                      label="评审状态"
                      align="center"
                      width="80"
                  ></el-table-column>
                  <el-table-column
                      fixed="right"
                      label="操作"
                      align="center"
                      width="200">
                    <template slot-scope="scope">
                      <div style="display: flex; flex-direction: row; justify-content: space-evenly;">
                        <div>
                          <el-tooltip content="对评审工作进行管理" placement="top" effect="light" :open-delay=1000>
                            <el-button type="primary" plain size="small" v-on:click.stop="reviewManage(scope.row)">
                              <el-badge :is-dot="scope.row.submitJobNum >0" >
                                评审管理
                              </el-badge>
                            </el-button>
                          </el-tooltip>
                        </div>
                        <div v-if="scope.row.status === JOB_ITEM_STATUS.COMPLETED.key && (scope.row.childrenJobComplateFlag || scope.row.type === JOB_ITEM_TYPE.REVIEW_MILESTONE.key)">
                          <el-tooltip content="对评审完成的资料进行归档. 归档后, 可以通过档案管理进行查询." placement="top" effect="light" :open-delay=1000>
                            <el-button type="primary" plain size="small" v-on:click.stop="archive(scope.row)">
                              归档
                            </el-button>
                          </el-tooltip>
                        </div>
<!--                        <div v-if="scope.row.status === JOB_ITEM_STATUS.COMPLETED.key && scope.row.childrenJobComplateFlag && scope.row.type===JOB_ITEM_TYPE.REVIEW.key ">
                          <el-tooltip content="发起对经过评审的功能或者子项目进行迭代,确定后,会产生新的迭代预立项项目." placement="top" effect="light" :open-delay=1000>
                            <el-button type="primary" plain size="small" @click.stop="iterationDlg(scope.row)">
                              迭代
                            </el-button>
                          </el-tooltip>
                        </div>-->
                      </div>
                    </template>
                  </el-table-column>
                </el-table>
              </div>
            
            
                            </template></DynamicTabPane>

            <!-- 评审管理-->
            <DynamicTabPane
                v-for="(item, index) in review_jobitem"
                :key="('review_jobitem-item-' + (item.id))"
                :index="index"
                prefix="review_jobitem-item"
                :active-page-id="activePageId">
                <template v-slot:default>
              <div>
                <ReviewJoblistTab
                    :key="item.id"
                    :jobitem-id="item.id"
                    :job-type="item.type"
                    :project-type="item.projectType"
                    :index="index"
                    :entrance="ENTRANCE_CLASS.REVIEW.key"
                ></ReviewJoblistTab>
              </div>
            
                </template></DynamicTabPane>
          
    </WorkspaceFrame>

    <!--特性迭代-->
    <el-dialog
        title="特性迭代"
        width="800px"
        :visible.sync="jobItemDlgVisible_iteration"
        :close-on-click-modal=false>
      <div v-if="jobItemDlgVisible_iteration">
        <CharacterUpdateDlg
            :key="characterId"
            :character-id="characterId"
            :close-function="closeDlg"
        ></CharacterUpdateDlg>
      </div>
    </el-dialog>
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
import ReviewJoblistTab from "@/components/rdms/review-joblist-tab.vue";
import {dateFormat, fixed2fractional, optionKV, rmbFormat } from "@/filter/filter";
import JobitemRejectDlg from "@/components/std/jobitem-reject-dlg.vue";
import CharacterUpdateDlg from "@/components/rdms/character-update-dlg.vue";

export default {
  components: {
    CharacterUpdateDlg,
    JobitemRejectDlg,
    Pagination,
    File,
    DynamicTabItem,
    DynamicTabPane,
    WorkspaceFrame,
    ReviewJoblistTab,
  },
  name: "review",

  data: function () {
    return {
      loginUser: {},
      loginCustomer: {},

      activePageId: 'home',
      jobitem: {},
      reviewItems: [],
      projectItems: [],
      review_jobitem: [],
      projectItems_demand: [],

      project: '',
      setupProjectItem: {}, //用于处理立项数据

      ENTRANCE_CLASS: ENTRANCE_CLASS,
      JOB_ITEM_STATUS: JOB_ITEM_STATUS,
      JOB_ITEM_TYPE: JOB_ITEM_TYPE,

      dialogTitle: '',
      jobItemDlgVisible_iteration: false,
      characterId: '',

    }
  },

  computed: {
  },

  mounted: function () {
    let _this = this;
    _this.getLoginUser();
    _this.$refs.pagination.size = 12;
    _this.list(1);

    _this.$bus.$on('close-review-tab', (index) => {
      _this.closeFunction_jobitem(index);
    });
    _this.$bus.$on('refresh-review-list', (data) => {
      if(data){
        _this.list(1);
      }
    });
  },

  beforeDestroy: function () {
    let _this = this;
    _this.$bus.$off('close-review-tab');
    _this.$bus.$off('refresh-review-list');
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

    budgetFormat(row, column){
      return rmbFormat(row.budget);
    },
    dateFormat(row, column) {
      return dateFormat(row.createTime);
    },
    JobitemStatusFormatter(row, column) {
      return optionKV(JOB_ITEM_STATUS, row.status);
    },
    JobitemTypeFormatter(row, column) {
      return optionKV(JOB_ITEM_TYPE, row.type);
    },

    tabJobItemClick(i) {
      let _this = this;
      _this.activePageId = `review_jobitem-item-${i}`;
    },

    tabHomeClick() {
      let _this = this;
      _this.activePageId = 'home';
      _this.list(1);
    },

    iterationDlg(data) {
      let _this = this;
      _this.characterId = data.characterId;
      _this.jobItemDlgVisible_iteration = true;
    },

    closeDlg(){
      let _this = this;
      _this.jobItemDlgVisible_iteration = false;
    },

    reviewManage(jobitem) {
      openOrFocusTab(this, this.review_jobitem, jobitem, 'review_jobitem-item');
    },
    closeFunction_jobitem(index) {
      closeDynamicTab(this, {
        list: this.review_jobitem,
        index: index,
        prefix: 'review_jobitem-item',
        fallback: 'home',
        afterClose: () => this.list(1),
      });
    },


    versionFormat(row, column) {
      return row.devVersion == null ? '' : String(row.devVersion);
    },

    rowClick(row, column, e){
      let _this = this;
      _this.reviewManage(row);
    },

    list(page) {
      let _this = this;
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/job-items/listReviewJobitems', {
        page: page,
        size: _this.$refs.pagination.size,
        actor: _this.loginUser.customerUserId,
      }).then((response)=>{
        let resp = response.data;
        if (resp.success) {
          _this.reviewItems = resp.content.list;
          _this.$refs.pagination.render(page, resp.content.total);
          // _this.$bus.$emit('refresh-review-list', false);
        } else {
          Toast.warning(resp.message,Swal);
        }
      })
    },

    archive(jobitem) {
      let _this = this;
      let text = `是否确认归档:"${jobitem.jobName}"?`;
      Swal.fire({
        title: '确认？',
        text: text,
        icon: 'question',
        showCancelButton: true,
        confirmButtonColor: '#409EFF',
        cancelButtonColor: '#909399',
        confirmButtonText: '确认!',
        cancelButtonText: '取消!'
      }).then((result) => {
        if (result.value) {
          _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/job-items/archive', {
            jobitemId: jobitem.id,
            executorId: _this.loginUser.customerUserId,
          }).then((response) => {
            let resp = response.data;
            if (resp.success) {
              Toast.success("归档成功!", Swal);
              _this.list(1);
              _this.$bus.$emit('notify-character-review-count-flag', true);  //评审管理
            } else {
              Toast.warning(resp.message, Swal);
            }
          })
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
