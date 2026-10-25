<!--
 * Author: 周朔鹏
 * Date: 2025-02-28
 * Email: 391902958@qq.com
 * Website: https://www.euipd.com
-->

<template>
  <div>
        <WorkspaceFrame>
        <template v-slot:header>
      <div style="position: relative;">
      <el-badge v-show="testManageJobNum >0" :value="testManageJobNum" class="item" style="position: absolute; left: 220px; top: 3px; z-index: 100;"></el-badge>
      <el-badge v-show="bugfeedbackNum >0" :value="bugfeedbackNum" class="item" style="position: absolute; left: 334px; top: 3px; z-index: 100;"></el-badge>
      </div>
    
        </template>
        <template v-slot:nav>
            <li :class="{active : activePageId==='home'}">
              <a @click="tabHomeClick">
                <i class="green ace-icon fa fa-home bigger-120"></i>
                在测项目列表
              </a>
            </li>

            <li :class="{active : activePageId==='setuped'}">
              <a @click="tabSetupedClick">
                单元测试管理
              </a>
            </li>

            <li :class="{active : activePageId==='bugfeedback'}">
              <a @click="tabBugfeedbackClick">
                缺陷反馈管理
              </a>
            </li>

            <li v-permission="'520801'" :class="{active : activePageId==='test-summary'}">
              <a @click="tabTestSummaryClick">
                测试执行汇总
              </a>
            </li>

            <!--测试管理-->
            <DynamicTabItem
                v-for="(item, index) in subProject_jobItem"
                :key="item.id"
                :index="index"
                :active-page-id="activePageId"
                prefix="test-jobitem-item"
                :close="closeFunction_jobitem"
                @select="tabJobItemClick">{{ ('测试项目 : ' + (item.label)) }}
            </DynamicTabItem>

            <!--单元测试管理-->
            <DynamicTabItem
                v-for="(item, index) in setupedProject_function"
                :key="item.id"
                :index="index"
                :active-page-id="activePageId"
                prefix="setuped-project-item"
                :close="closeFunction_function"
                @select="tabFunctionClick">{{ ('单元测试管理 : ' + (item.projectName)) }}
            </DynamicTabItem>

            <!--执行数据-->
            <DynamicTabItem
                v-for="(item, index) in projectArchiveItems"
                :key="item.id"
                :index="index"
                :active-page-id="activePageId"
                prefix="archive-tree"
                :close="closeFunction_archive_data"
                @select="developArchiveClick">{{ ('文档:' + (item.projectName)) }}
            </DynamicTabItem>

            <!--任务清单-->
            <DynamicTabItem
                v-for="(item, index) in testedSubprojectItems"
                :key="item.id"
                :index="index"
                :active-page-id="activePageId"
                prefix="test-subproject"
                :close="closeFunction_tested"
                @select="testedSubprojectClick">{{ ('测试清单:' + (item.label)) }}
            </DynamicTabItem>

            <!--缺陷清单-->
            <DynamicTabItem
                v-for="(item, index) in bugSubprojectItems"
                :key="item.id"
                :index="index"
                :active-page-id="activePageId"
                prefix="bug-subproject"
                :close="closeFunction_bug"
                @select="bugSubprojectClick">{{ ('缺陷清单:' + (item.label)) }}
            </DynamicTabItem>

            <!--缺陷档案-->
            <DynamicTabItem
                v-for="(item, index) in bugSubprojectArchive"
                :key="item.id"
                :index="index"
                :active-page-id="activePageId"
                prefix="bug-archive"
                :close="closeFunction_bugArchive"
                @select="bugSubprojectArchiveClick">{{ ('缺陷档案:' + (item.label)) }}
            </DynamicTabItem>

            <!--缺陷处理-->
            <DynamicTabItem
                v-for="(item, index) in bugfeedbackItemList"
                :key="item.id"
                :index="index"
                :active-page-id="activePageId"
                prefix="bug-handle-item"
                :close="closeFunction_bugfeedback"
                @select="tabBugfeedbackItemClick">{{ ('缺陷处理 : ' + (item.name)) }}
            </DynamicTabItem>

            <!--物料管理-->
            <DynamicTabItem
                v-for="(item, index) in qualityMaterialItemList"
                :key="item.id"
                :index="index"
                :active-page-id="activePageId"
                prefix="material-manage-item"
                :close="closeFunction_material"
                @select="tabMaterialItemClick">{{ ('物料管理 : ' + (item.name)) }}
            </DynamicTabItem>

            <!--费用管理-->
            <DynamicTabItem
                v-for="(item, index) in qualityFeeItemList"
                :key="item.id"
                :index="index"
                :active-page-id="activePageId"
                prefix="fee-manage-item"
                :close="closeFunction_fee"
                @select="tabFeeItemClick">{{ ('费用管理 : ' + (item.name)) }}
            </DynamicTabItem>

          
        </template>

                        <DynamicTabPane
                page-id="home"
                :active-page-id="activePageId">
                            <template v-slot:default>
              <p>在测项目管理: 是为<strong>测试主管</strong>提供的一个进行测试任务管理的一套功能页面.</p>
              <div class="hr hr-18 dotted hr-double"></div>
              <div>
                <el-table
                    :data="subProjects"
                    border
                    highlight-current-row
                    style="width: 100%"
                    @row-click="jobItemManager">
                  <el-table-column
                      prop="projectCode"
                      label="项目编号"
                      min-width="150"
                      show-overflow-tooltip
                      align="center"
                  ></el-table-column>
                  <el-table-column
                      prop="label"
                      label="子项目名称"
                      align="center"
                      min-width="200"
                      show-overflow-tooltip
                  ></el-table-column>
                  <el-table-column
                      prop="projectName"
                      align="center"
                      label="项目名称"
                      show-overflow-tooltip
                      min-width="200"
                  ></el-table-column>
                  <el-table-column
                      prop="projectManagerName"
                      label="项目经理"
                      align="center"
                      min-width="100"
                  ></el-table-column>
                  <el-table-column
                      prop="budget"
                      :formatter= "budgetFormat"
                      label="预算"
                      align="center"
                      min-width="100"
                  ></el-table-column>
                  <el-table-column
                      prop="devVersion"
                      :formatter="versionFormat"
                      label="开发版本"
                      align="center"
                      min-width="100"
                  ></el-table-column>
                  <el-table-column
                      prop="setupedTime"
                      :formatter="setupedTimeFormat"
                      label="立项时间"
                      align="center"
                      min-width="100"
                  ></el-table-column>
                  <el-table-column
                      fixed="right"
                      label="操作"
                      align="center"
                      width="150">
                    <template slot-scope="scope">
                      <el-tooltip content="对所有测试任务任务进行管理" placement="top" effect="light" :open-delay=1000>
                        <el-button type="primary" plain size="small" v-on:click.stop="jobItemManager(scope.row)">
                          <el-badge :is-dot="scope.row.submitJobNum >0" >
                            测试管理
                          </el-badge>
                        </el-button>
                      </el-tooltip>
                    </template>
                  </el-table-column>
                </el-table>
              </div>
            
            
                            </template></DynamicTabPane>
                        <DynamicTabPane
                page-id="setuped"
                :active-page-id="activePageId">
                            <template v-slot:default>
              <p>立项项目列表页: 是为<strong>项目测试主管</strong>提供的一个处理已经立项项目测试的功能页面.</p>
              <div class="hr hr-18 dotted hr-double"></div>
              <div>
                <el-table
                    :data="setupedProjectList"
                    border
                    highlight-current-row
                    style="width: 100%"
                    @row-click="functionManage">
                  <el-table-column
                      prop="projectCode"
                      label="项目编号"
                      min-width="180"
                      show-overflow-tooltip
                      align="center"
                  ></el-table-column>
                  <el-table-column
                      prop="projectName"
                      label="项目名称"
                      align="center"
                      min-width="250"
                      show-overflow-tooltip
                  ></el-table-column>
                  <el-table-column
                      prop="productManagerName"
                      label="产品经理"
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
                      prop="devVersion"
                      :formatter="versionFormat"
                      label="开发版本"
                      align="center"
                      min-width="100"
                  ></el-table-column>
                  <el-table-column
                      prop="setupedTime"
                      :formatter="setupedTimeFormat"
                      align="center"
                      label="立项时间"
                      min-width="160"
                  ></el-table-column>
                  <el-table-column
                      fixed="right"
                      label="操作"
                      align="center"
                      width="250">
                    <template slot-scope="scope">
                      <el-button type="primary" plain size="small" v-on:click.stop="functionManage(scope.row)">
                        <el-badge :is-dot="scope.row.submitJobNum_testTask > 0">
                          单元测试管理
                        </el-badge>
                      </el-button>
                      <el-button v-on:click.s.stop="developArchiveShow(scope.row)" type="primary" plain size="small">
                        执行数据
                      </el-button>
                    </template>
                  </el-table-column>
                </el-table>
              </div>
            
            
                            </template></DynamicTabPane>

                        <DynamicTabPane
                page-id="bugfeedback"
                :active-page-id="activePageId">
                            <template v-slot:default>
              <p>缺陷反馈列表页: 是为<strong>项目测试主管</strong>提供的一个处理研发缺陷的功能页面.</p>
              <div class="hr hr-18 dotted hr-double"></div>
              <div style="display: flex; justify-content: space-between;">
                <pagination
                    key="bug_dealwith"
                    ref="pagination_bug_dealwith"
                    v-bind:list="listByTestManager"
                    v-bind:itemCount="6"
                ></pagination>
                <div>
<!--                  <el-button type="primary" size="small" style="margin-right: 15px;" @click="createBugFeedback()">缺陷反馈单</el-button>-->
                </div>
              </div>
              <div>
                <el-table
                    :data="bugList"
                    border
                    stripe
                    highlight-current-row
                    style="width: 100%"
                    @row-click="showBugFeedback">
                  <el-table-column  label="序号" align="center" width="50">
                    <template slot-scope="scope">
                      {{scope.$index+1}}
                    </template>
                  </el-table-column>
                  <el-table-column
                      prop="bugSerial"
                      width="180"
                      align="center"
                      label="处理编号"
                  ></el-table-column>
                  <el-table-column
                      prop="name"
                      align="center"
                      show-overflow-tooltip
                      label="缺陷标题"
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
                      prop="writerName"
                      sortable
                      align="center"
                      width="110"
                      label="反馈人"
                  ></el-table-column>
                  <el-table-column
                      prop="planSubmissionTime"
                      :formatter="submitTimeFormat"
                      align="center"
                      width="110"
                      label="完成时间"
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
                      width="320">
                    <template slot-scope="scope">
                      <div>
                        <el-button type="primary" plain size="small" v-on:click.stop="bugTaskDealwith(scope.row)">
                          <el-badge :is-dot="scope.row.submittedJobNum >0" >
                            缺陷处理
                          </el-badge>
                        </el-button>
                        <el-button type="primary" plain size="small" v-on:click.stop="qualityMaterialManager(scope.row)">
                          <el-badge :is-dot="scope.row.materialAppNum >0" >
                            物料管理
                          </el-badge>
                        </el-button>
                        <el-button type="primary" plain size="small" v-on:click.stop="qualityFeeManager(scope.row)">
                          <el-badge :is-dot="scope.row.feeAppNum >0" >
                            费用管理
                          </el-badge>
                        </el-button>
                      </div>
                    </template>
                  </el-table-column>
                </el-table>
              </div>
            
            
                            </template></DynamicTabPane>


                        <DynamicTabPane
                page-id="test-summary"
                :active-page-id="activePageId">
                            <template v-slot:default>
              <p>测项执行汇总: 是为<strong>测试主管</strong>提供的一个进行测试任务监管以及档案查阅的一套功能页面.</p>
              <div class="hr hr-18 dotted hr-double"></div>
              <div style="display: flex; justify-content: space-between;">
                <pagination
                    key="test_subproject_list_pagination"
                    ref="test_subproject_list_pagination"
                    v-bind:list="getTestedProjectListByTm"
                    v-bind:itemCount="6"
                ></pagination>
                <div style="margin-right: 20px;">
                  <el-input size="mini" placeholder="请输入子项目名称搜索" v-model="keyWord">
                    <el-button slot="append" icon="el-icon-search" @click="getTestedProjectListByTm(1)"></el-button>
                  </el-input>
                </div>
              </div>
              <div>
                <el-table
                    :data="testedProjectList"
                    border
                    highlight-current-row
                    style="width: 100%"
                    @row-click="testedSubprojectsShow">
                  <el-table-column
                      prop="projectCode"
                      label="项目编号"
                      min-width="150"
                      show-overflow-tooltip
                      align="center"
                  ></el-table-column>
                  <el-table-column
                      prop="label"
                      label="子项目名称"
                      align="center"
                      min-width="200"
                      show-overflow-tooltip
                  ></el-table-column>
                  <el-table-column
                      prop="projectName"
                      align="center"
                      label="项目名称"
                      show-overflow-tooltip
                      min-width="200"
                  ></el-table-column>
                  <el-table-column
                      prop="projectManagerName"
                      label="项目经理"
                      align="center"
                      min-width="100"
                  ></el-table-column>
                  <el-table-column
                                   prop="budget"
                                   :formatter= "budgetFormat"
                                   label="预算"
                                   align="center"
                                   min-width="100"
                  ></el-table-column>
                  <el-table-column
                      prop="devVersion"
                      :formatter="versionFormat"
                      label="开发版本"
                      align="center"
                      min-width="100"
                  ></el-table-column>
                  <el-table-column
                                   prop="setupedTime"
                                   :formatter="setupedTimeFormat"
                                   label="立项时间"
                                   align="center"
                                   min-width="100"
                  ></el-table-column>
                  <el-table-column
                      fixed="right"
                      label="操作"
                      align="center"
                      width="320">
                    <template slot-scope="scope">
                      <el-button type="primary" plain size="small" v-on:click.stop="testedSubprojectsShow(scope.row)">
                        任务清单
                      </el-button>
                      <el-button type="primary" plain size="small" v-on:click.stop="bugSubprojectsShow(scope.row)">
                        缺陷清单
                      </el-button>
                      <el-button type="primary" plain size="small" v-on:click.stop="bugSubprojectArchiveShow(scope.row)">
                        缺陷档案
                      </el-button>
                    </template>
                  </el-table-column>
                </el-table>
              </div>
            
            
                            </template></DynamicTabPane>

            <!-- 测试管理-->
            <DynamicTabPane
                v-for="(item, index) in subProject_jobItem"
                :key="('test-jobitem-item-' + (item.id))"
                :index="index"
                prefix="test-jobitem-item"
                :active-page-id="activePageId">
                <template v-slot:default>
              <div>
                <!--测试管理的projectType 固定为 Sub_project, 即便是产品管理过程中 实际为Project类型, 也使用sub_project类型处理-->
                <test-joblist-tab
                    v-if="activePageId === ('test-jobitem-item-' + (index))"
                    :key="item.id"
                    :sub-project-id="item.id"
                    :job-type="JOB_ITEM_TYPE.TEST.key"
                    :project-type="PROJECT_TYPE.SUB_PROJECT.key"
                    :index="index"
                    :entrance="ENTRANCE_CLASS.TEST.key"
                ></test-joblist-tab>
              </div>
            
                </template></DynamicTabPane>
            <!-- 单元测试管理-->
            <DynamicTabPane
                v-for="(item, index) in setupedProject_function"
                :key="('setuped-project-item-' + (item.id))"
                :index="index"
                prefix="setuped-project-item"
                :active-page-id="activePageId">
                <template v-slot:default>
              <div>
                <TestFunctionManage
                    v-if="activePageId === ('setuped-project-item-' + (index))"
                    :key="item.id"
                    :project-id="item.id"
                    :index="index"
                    :entrance="ENTRANCE_CLASS.TASK.key"
                ></TestFunctionManage>
              </div>
            
                </template></DynamicTabPane>

            <!-- 执行数据-->
            <DynamicTabPane
                v-for="(item, index) in projectArchiveItems"
                :key="('archive-' + (item.id))"
                :index="index"
                prefix="archive-tree"
                :active-page-id="activePageId">
                <template v-slot:default>
              <div>
                <TestArchiveContainer
                    v-if="activePageId === ('archive-tree-' + (index))"
                    :key="item.id"
                    :index="index"
                    :project-id="item.id"
                    :project-type="PROJECT_TYPE.PROJECT.key"
                    :entrance="ENTRANCE_CLASS.TEST.key">
                </TestArchiveContainer>
              </div>
            
                </template></DynamicTabPane>

            <!-- 测试清单-->
            <DynamicTabPane
                v-for="(item, index) in testedSubprojectItems"
                :key="('test-subproject-' + (item.id))"
                :index="index"
                prefix="test-subproject"
                :active-page-id="activePageId">
                <template v-slot:default>
              <div v-if="item && item.id">
                <TestSubprojectJoblist
                    v-if="activePageId === ('test-subproject-' + (index))"
                    :key="item.id"
                    :subproject-id="item.id">
                </TestSubprojectJoblist>
              </div>
            
                </template></DynamicTabPane>

            <!-- 缺陷清单-->
            <DynamicTabPane
                v-for="(item, index) in bugSubprojectItems"
                :key="('bug-subproject-' + (item.id))"
                :index="index"
                prefix="bug-subproject"
                :active-page-id="activePageId">
                <template v-slot:default>
              <div v-if="item && item.id">
                <BugSubprojectJoblist
                    v-if="activePageId === ('bug-subproject-' + (index))"
                    :key="item.id"
                    :subproject-id="item.id">
                </BugSubprojectJoblist>
              </div>
            
                </template></DynamicTabPane>

            <!-- 缺陷档案-->
            <DynamicTabPane
                v-for="(item, index) in bugSubprojectArchive"
                :key="('bug-archive-' + (item.id))"
                :index="index"
                prefix="bug-archive"
                :active-page-id="activePageId">
                <template v-slot:default>
              <div v-if="item && item.id">
                <BugSubprojectArchiveList
                    v-if="activePageId === ('bug-archive-' + (index))"
                    :key="item.id"
                    :subproject-id="item.id">
                </BugSubprojectArchiveList>
              </div>
            
                </template></DynamicTabPane>

            <!-- 缺陷处理-->
            <DynamicTabPane
                v-for="(item, index) in bugfeedbackItemList"
                :key="('bug-handle-item-' + (item.id))"
                :index="index"
                prefix="bug-handle-item"
                :active-page-id="activePageId">
                <template v-slot:default>
              <BugManageTaskTabs
                  v-if="activePageId === ('bug-handle-item-' + (index))"
                  :key="item.id"
                  :bugfeedback-id="item.id"
                  :job-type="JOB_ITEM_TYPE.TASK_BUG.key"
                  :project-type="PROJECT_TYPE.SUB_PROJECT.key"
                  :index="index"
                  :entrance="ENTRANCE_CLASS.TEST.key"
              ></BugManageTaskTabs>
            
                </template></DynamicTabPane>

            <!-- 物料管理-->
            <DynamicTabPane
                v-for="(item, index) in qualityMaterialItemList"
                :key="('material-manage-item-' + (item.id))"
                :index="index"
                prefix="material-manage-item"
                :active-page-id="activePageId">
                <template v-slot:default>
              <QualityMaterialList
                  v-if="activePageId === ('material-manage-item-' + (index))"
                  :key="item.id"
                  :quality-id="item.id"
                  :index="index"
              ></QualityMaterialList>
            
                </template></DynamicTabPane>

            <!-- 费用管理-->
            <DynamicTabPane
                v-for="(item, index) in qualityFeeItemList"
                :key="('fee-manage-item-' + (item.id))"
                :index="index"
                prefix="fee-manage-item"
                :active-page-id="activePageId">
                <template v-slot:default>
              <QualityFeeList
                  v-if="activePageId === ('fee-manage-item-' + (index))"
                  :key="item.id"
                  :quality-id="item.id"
                  :index="index"
              ></QualityFeeList>
            
                </template></DynamicTabPane>

          
    </WorkspaceFrame>

    <!--    缺陷详情-->
    <el-dialog
        :visible.sync="bugDetailVisible"
        width="29cm"
        :before-close="handleClose"
        :close-on-click-modal=false>
      <div v-if="bugDetailVisible">
        <BugfeedbackDetailPlain
            :key="bugFeedbackId"
            :bugfeedback-id="bugFeedbackId">
        </BugfeedbackDetailPlain>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import {closeDynamicTab, openOrFocusTab} from "@/shared/dynamic-tab";
import DynamicTabItem from "@/components/rdms/dynamic-tab-item";
import DynamicTabPane from "@/components/rdms/dynamic-tab-pane";
import WorkspaceFrame from "@/components/rdms/workspace-frame";
import File from "@/components/rdms/file";
import Swal from "sweetalert2";
import TestJoblistTab from "@/components/rdms/test-joblist-tab";
import { Loading } from 'element-ui'

import {dateFormat, fixed2fractional, optionKV, rmbFormat, shortDateFormat} from "@/filter/filter";
import TestFunctionManage from "@/components/rdms/test-function-manage.vue";
import TestArchiveContainer from "@/components/rdms/test-archive-container.vue";
import TestSubprojectJoblist from "@/components/rdms/test-subproject-joblist.vue";
import BugSubprojectJoblist from "@/components/rdms/bug-subproject-joblist.vue";
import QualityMaterialList from "@/components/rdms/quality-material-list.vue";
import QualityFeeList from "@/components/rdms/quality-fee-list.vue";
import Pagination from "@/components/pagination.vue";
import BugfeedbackDetailPlain from "@/components/rdms/bugfeedback-detail-plain.vue";
import BugManageTaskTabs from "@/components/rdms/bug-manage-task-tabs.vue";
import BugSubprojectArchiveList from "@/components/rdms/bug-subproject-archive-list.vue";

export default {
  components: {
    BugfeedbackDetailPlain,
    Pagination,
    BugManageTaskTabs, QualityFeeList, QualityMaterialList,
    TestArchiveContainer,
    TestFunctionManage,
    File,
    DynamicTabItem,
    DynamicTabPane,
    WorkspaceFrame,
    TestJoblistTab,
    TestSubprojectJoblist,
    BugSubprojectJoblist,
    BugSubprojectArchiveList,
  },
  name: "test",

  data: function () {
    return {
      loginUser: {},
      loginCustomer: {},

      JOB_ITEM_TYPE: JOB_ITEM_TYPE,
      PROJECT_TYPE: PROJECT_TYPE,
      ENTRANCE_CLASS: ENTRANCE_CLASS,
      QUALITY_STATUS: QUALITY_STATUS,

      activePageId: 'home',
      preActivePageId: 'home',

      bugList: [],
      aProject: {},
      subProjects: [],
      setupedProjectList: [],
      testedProjectList: [],
      projectArchiveItems: [],
      testedSubprojectItems: [],
      bugSubprojectItems: [],
      bugSubprojectArchive: [],

      subProject_jobItem: [],
      projectItems_demand: [],
      setupedProject_function: [],

      bugfeedbackItemList: [],
      qualityMaterialItemList: [],
      qualityFeeItemList: [],

      keyWord: '',
      project: '',
      setupProjectItem: {}, //用于处理立项数据
      testManageJobNum: 0,
      bugfeedbackNum: 0,

      dialogTitle: '',
      bugDetailVisible: false,
      timestamp: '',
      bugItem: null,
      bugFeedbackId: '',
    }
  },

  computed: {
  },

  mounted: function () {
    let _this = this;
    _this.getLoginUser();
    Promise.all([
      _this.getTestSubprojectListByTM(),
      _this.getTestJobNumByUserId(),
      _this.getBugFeedbackNumByUserId()
    ]);

    _this.unsubscribeTest = _this.$bus.subscribe(['jobitem', 'quality'], function () {
      _this.getTestJobNumByUserId();
      _this.getBugFeedbackNumByUserId();
      if (_this.activePageId === 'home') {
        _this.getTestSubprojectListByTM();
      }
      if (_this.activePageId === 'bugfeedback') {
        _this.listByTestManager(1);
      }
    });

    _this.$bus.$on('close-bugfeedback-dealwith-tab', (index) => {
      _this.closeFunction_bugfeedback(index);
    });

  },

  beforeDestroy(){
    let _this = this;
    _this.$bus.$off('close-bugfeedback-dealwith-tab');
    if (_this.unsubscribeTest) {
      _this.unsubscribeTest();
    }
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
      _this.getTestJobNumByUserId();
      _this.getBugFeedbackNumByUserId();
    },

    getTestJobNumByUserId() {
      let _this = this;
      return _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/job-items/getTestJobNumByUserId/' + _this.loginUser.customerUserId).then((response) => {
        let resp = response.data;
        if (resp.success) {
          _this.testManageJobNum = resp.content;
        } else {
          Toast.warning(resp.message, Swal);
        }
      })
    },

    getBugFeedbackNumByUserId() {
      let _this = this;
      return _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/bugfeedback/getBugFeedbackNumByUserId/' + _this.loginUser.customerUserId).then((response) => {
        let resp = response.data;
        if (resp.success) {
          _this.bugfeedbackNum = resp.content;
        } else {
          Toast.warning(resp.message, Swal);
        }
      })
    },

    setupedTimeFormat(row, column) {
      return shortDateFormat(row.setupedTime);
    },
    budgetFormat(row, column){
      return rmbFormat(row.budget);
    },
    statusFormat(row) {
      return optionKV(QUALITY_STATUS, row.status);
    },
    submitTimeFormat(row, column){
      return shortDateFormat(row.planSubmissionTime);
    },
    tabJobItemClick(i) {
      let _this = this;
      _this.activePageId = `test-jobitem-item-${i}`;
    },

    tabHomeClick() {
      let _this = this;
      _this.activePageId = 'home';
      _this.preActivePageId = 'home';
      _this.getTestSubprojectListByTM();
    },

    tabSetupedClick() {
      let _this = this;
      _this.activePageId = 'setuped';
      _this.preActivePageId = 'setuped';
      _this.getSetupedProjectListByTm();
    },

    tabBugfeedbackClick() {
      let _this = this;
      _this.activePageId = 'bugfeedback';
      _this.preActivePageId = 'bugfeedback';
      _this.$nextTick(function () {
        _this.$refs.pagination_bug_dealwith.size = 12;
        _this.listByTestManager(1);
      });
    },
    listByTestManager(page) {
      let _this = this;
      let loadingInstance = Loading.service({ fullscreen: true });
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/bugfeedback/listByTestManager', {
        page: page,
        size: _this.$refs.pagination_bug_dealwith.size,
        customerId: _this.loginCustomer.id, //customerId
        actor: _this.loginUser.customerUserId,
      }).then((response)=>{
        loadingInstance.close();
        let resp = response.data;
        if (resp.success) {
          _this.bugList = resp.content.list;
          _this.$refs.pagination_bug_dealwith.render(page, resp.content.total);
        } else {
          Toast.warning(resp.message,Swal);
        }
      })
    },

    tabTestSummaryClick() {
      let _this = this;
      _this.activePageId = 'test-summary';
      _this.preActivePageId = 'test-summary';
      _this.$nextTick(function () {
        _this.$refs.test_subproject_list_pagination.size = 12;
        _this.getTestedProjectListByTm(1);
      });
    },

    getSetupedProjectListByTm() {
      let _this = this;
      let formData = new window.FormData();
      formData.append('customerId', _this.loginCustomer.id);
      formData.append('loginUserId', _this.loginUser.customerUserId);
      let loadingInstance = Loading.service({ fullscreen: true });
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/project/getSetupedProjectListByTm', formData).then((response) => {
        loadingInstance.close();
        let resp = response.data;
        if (resp.success) {
          _this.setupedProjectList = resp.content;
        } else {
          Toast.warning(resp.message, Swal);
        }
      })
    },

    getTestedProjectListByTm(page) {
      let _this = this;
      let loadingInstance = Loading.service({ fullscreen: true });
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/project/getTestedSubprojectListByTm', {
        page: page,
        size: _this.$refs.test_subproject_list_pagination.size,
        customerId: _this.loginCustomer.id,
        loginUserId: _this.loginUser.customerUserId,
        keyWord: _this.keyWord,
      }).then((response) => {
        loadingInstance.close();
        let resp = response.data;
        if (resp.success) {
          _this.testedProjectList = resp.content.list;
          _this.$refs.test_subproject_list_pagination.render(page, resp.content.total);
        } else {
          Toast.warning(resp.message, Swal);
        }
      })
    },

    jobItemManager(aProject) {
      openOrFocusTab(this, this.subProject_jobItem, aProject, 'test-jobitem-item');
    },
    closeFunction_jobitem(index) {
      closeDynamicTab(this, {
        list: this.subProject_jobItem,
        index: index,
        prefix: 'test-jobitem-item',
        fallback: () => this.preActivePageId,
        afterClose: () => this.getTestSubprojectListByTM(),
      });
    },

    tabFunctionClick(i) {
      let _this = this;
      _this.activePageId = `setuped-project-item-${i}`;
    },
    functionManage(aProject) {
      openOrFocusTab(this, this.setupedProject_function, aProject, 'setuped-project-item');
    },
    closeFunction_function(index) {
      closeDynamicTab(this, {
        list: this.setupedProject_function,
        index: index,
        prefix: 'setuped-project-item',
        fallback: () => this.preActivePageId,
      });
    },

    versionFormat(row, column) {
      return row.devVersion == null ? '' : String(row.devVersion);
    },

    rowClick(row, column, e){
      let _this = this;
      _this.functionManage(row);
    },

    getTestSubprojectListByTM() {
      let _this = this;
      return _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/sub-project/getTestSubprojectListByTM/' + _this.loginUser.customerUserId).then((response)=>{
        let resp = response.data;
        if (resp.success) {
          _this.subProjects = resp.content;
        } else {
          Toast.warning(resp.message,Swal);
        }
      })
    },

    developArchiveShow(mixProject) {
      openOrFocusTab(this, this.projectArchiveItems, mixProject, 'archive-tree');
    },
    closeFunction_archive_data(index) {
      closeDynamicTab(this, {
        list: this.projectArchiveItems,
        index: index,
        prefix: 'archive-tree',
        fallback: () => this.preActivePageId,
      });
    },
    developArchiveClick(i) {
      let _this = this;
      _this.activePageId = `archive-tree-${i}`;
    },

    testedSubprojectsShow(mixProject) {
      openOrFocusTab(this, this.testedSubprojectItems, mixProject, 'test-subproject');
    },
    closeFunction_tested(index) {
      closeDynamicTab(this, {
        list: this.testedSubprojectItems,
        index: index,
        prefix: 'test-subproject',
        fallback: () => this.preActivePageId,
      });
    },
    testedSubprojectClick(i) {
      let _this = this;
      _this.activePageId = `test-subproject-${i}`;
    },


    bugSubprojectsShow(mixProject) {
      openOrFocusTab(this, this.bugSubprojectItems, mixProject, 'bug-subproject');
    },
    closeFunction_bug(index) {
      closeDynamicTab(this, {
        list: this.bugSubprojectItems,
        index: index,
        prefix: 'bug-subproject',
        fallback: () => this.preActivePageId,
      });
    },
    bugSubprojectClick(i) {
      let _this = this;
      _this.activePageId = `bug-subproject-${i}`;
    },

    bugSubprojectArchiveShow(mixProject) {
      openOrFocusTab(this, this.bugSubprojectArchive, mixProject, 'bug-archive');
    },
    closeFunction_bugArchive(index) {
      closeDynamicTab(this, {
        list: this.bugSubprojectArchive,
        index: index,
        prefix: 'bug-archive',
        fallback: () => this.preActivePageId,
      });
    },
    bugSubprojectArchiveClick(i) {
      let _this = this;
      _this.activePageId = `bug-archive-${i}`;
    },

    delete(project) {
      let _this = this;
      let text = `是否确认删除"${project.projectName}"？`;
      Swal.fire({
        title: '确认？',
        text: text,
        icon: 'warning',
        showCancelButton: true,
        confirmButtonColor: '#409EFF',
        cancelButtonColor: '#909399',
        confirmButtonText: '确认!',
        cancelButtonText: '取消!'
      }).then((result) => {
        if (result.value) {
          _this.$ajax.delete(process.env.VUE_APP_SERVER + '/customer/pre-project/del/' + project.id).then((response) => {
            let resp = response.data;
            if (resp.success) {
              Toast.success("删除成功!", Swal);
              _this.getTestSubprojectListByTM();
            } else {
              Toast.warning(resp.message, Swal);
            }
          })
        }
      })
    },

    tabBugfeedbackItemClick(i) {
      let _this = this;
      _this.activePageId = `bug-handle-item-${i}`;
    },

    bugTaskDealwith(bugfeedback) {
      openOrFocusTab(this, this.bugfeedbackItemList, bugfeedback, 'bug-handle-item');
    },
    closeFunction_bugfeedback(index) {
      closeDynamicTab(this, {
        list: this.bugfeedbackItemList,
        index: index,
        prefix: 'bug-handle-item',
        fallback: () => this.preActivePageId,
        // 关闭后不再刷新缺陷列表
      });
    },

    tabMaterialItemClick(i) {
      let _this = this;
      _this.activePageId = `material-manage-item-${i}`;
    },
    qualityMaterialManager(quality) {
      openOrFocusTab(this, this.qualityMaterialItemList, quality, 'material-manage-item');
    },
    closeFunction_material(index) {
      closeDynamicTab(this, {
        list: this.qualityMaterialItemList,
        index: index,
        prefix: 'material-manage-item',
        fallback: () => this.preActivePageId,
        // 关闭后不再刷新缺陷列表
      });
    },

    tabFeeItemClick(i) {
      let _this = this;
      _this.activePageId = `fee-manage-item-${i}`;
    },
    qualityFeeManager(quality) {
      openOrFocusTab(this, this.qualityFeeItemList, quality, 'fee-manage-item');
    },
    closeFunction_fee(index) {
      closeDynamicTab(this, {
        list: this.qualityFeeItemList,
        index: index,
        prefix: 'fee-manage-item',
        fallback: () => this.preActivePageId,
        // 关闭后不再刷新缺陷列表
      });
    },

    showBugFeedback(bugfeedback) {
      let _this = this;
      _this.timestamp = new Date().getTime();
      _this.bugDetailVisible = true;
      _this.bugItem = bugfeedback;
      _this.bugFeedbackId = bugfeedback.id;
      _this.dialogTitle = "缺陷反馈单";
    },

    handleClose(done) {
      done();
    },

    closeDialog() {
      let _this = this;
      _this.bugDetailVisible = false;
    },


    tableRowClass({row, rowIndex}) {
      // console.log("row:", Date.parse(row.planCompleteTime));
      if (Date.now() > Date.parse(row.releaseTime)) {
        return 'warning-row-3';
      }
      return '';
    }
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
