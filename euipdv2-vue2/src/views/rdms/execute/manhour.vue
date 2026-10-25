<!--
 * Author: 周朔鹏
 * Date: 2025-04-18
 * Email: 391902958@qq.com
 * Website: https://www.euipd.com
-->

<template>
  <div>
        <WorkspaceFrame>
        <template v-slot:header>
      <div style="position: relative;">
      <el-badge v-show="suggestNum.materialAppNum>0" :value="suggestNum.materialAppNum" class="item"
                style="position: absolute; left: 255px; top: 3px; z-index: 100;"></el-badge>
      <el-badge v-show="suggestNum.feeAppNum>0" :value="suggestNum.feeAppNum" class="item"
                style="position: absolute; left: 345px; top: 3px; z-index: 100;"></el-badge>
      <el-badge v-show="suggestNum.bugfeedbackNum>0" :value="suggestNum.bugfeedbackNum" class="item"
                style="position: absolute; left: 434px; top: 3px; z-index: 100;"></el-badge>
<!--      <el-badge v-show="suggestNum.suggestNum>0" :value="suggestNum.suggestNum" class="item"
                style="position: absolute; left: 545px; top: 3px; z-index: 100;"></el-badge>-->
      </div>
    
        </template>
        <template v-slot:nav>
            <li :class="{active : activePageId==='home'}">
              <a @click="tabHomeClick">
                <i class="green ace-icon fa fa-home bigger-120"></i>
                任务列表
              </a>
            </li>
            <li :class="{active : activePageId==='archive'}">
              <a @click="archiveManageClick">
                我的文献
              </a>
            </li>
            <li :class="{active : activePageId==='material'}">
              <a @click="materialManageClick">
                物料处理
              </a>
            </li>
            <li :class="{active : activePageId==='fee-manage'}">
              <a @click="feeManageClick">
                费用处理
              </a>
            </li>

            <li :class="{active : activePageId==='bug-feedback'}">
              <a @click="bugFeedbackClick">
                缺陷反馈
              </a>
            </li>
<!--            <li :class="{active : activePageId==='character-suggest'}">
              <a @click="suggestClick">
                增补特性建议
              </a>
            </li>-->
            <li :class="{active : activePageId==='complete-task'}">
              <a @click="completeTaskClick">
                完工清单
              </a>
            </li>

            <!--任务处理-->
            <DynamicTabItem
                v-for="(item, index) in jobItemList"
                :key="item.id"
                :index="index"
                :active-page-id="activePageId"
                prefix="job-item-item"
                :close="closeFunction_job_item"
                @select="tabJobItemClick">{{ ('任务: ' + (item.jobName)) }}
            </DynamicTabItem>
          
        </template>

            <!--列表页面-->
                        <DynamicTabPane
                page-id="home"
                :active-page-id="activePageId">
                            <template v-slot:default>
              <div style="display: flex; flex-direction: row; justify-content: space-between; align-items: center;">
                <p>执行管理: 是为<strong>工作执行人员</strong>提供的一套综合办公管理页面,用于对具体的工作内容进行管理.</p>
                <div>
                  <el-button @click="showPerformance">显示统计数据</el-button>
                </div>
              </div>

              <div class="hr hr-18 dotted hr-double"></div>
              <div style="display: flex; justify-content: space-between;">
                <pagination ref="pagination" v-bind:list="list_jobitems" v-bind:itemCount="6"></pagination>
                <div style="margin-right: 20px;">
                  <el-input size="mini" placeholder="请输入任务名称搜索" v-model="keyWord">
                    <el-button slot="append" icon="el-icon-search" @click="search(1)"></el-button>
                  </el-input>
                </div>
              </div>
              <div>
                <el-table
                    :data="jobItems"
                    border
                    highlight-current-row
                    :row-class-name="tableRowClass"
                    style="width: 100%"
                    @row-click="rowClick">
                  <el-table-column
                      prop="jobSerial"
                      align="center"
                      label="任务号"
                      show-overflow-tooltip
                      width="160"
                  ></el-table-column>
                  <el-table-column
                      prop="type"
                      align="center"
                      label="任务类型"
                      :formatter="jobitemTypeFormat"
                      min-width="95"
                  ></el-table-column>
                  <el-table-column
                      prop="processStatus"
                      align="center"
                      label="任务状态"
                      :formatter="processStatusFormat"
                      width="80"
                  ></el-table-column>
                  <el-table-column
                      prop="jobName"
                      align="center"
                      show-overflow-tooltip
                      :min-width="200"
                      label="任务名称"
                  ></el-table-column>
                  <el-table-column
                      prop="projectName"
                      align="center"
                      :min-width="200"
                      label="预立项 / 项目名称"
                      show-overflow-tooltip
                      :formatter="projectNameFormat"
                  ></el-table-column>
                  <el-table-column
                      prop="subprojectName"
                      align="center"
                      :min-width="200"
                      label="子项目名称"
                      show-overflow-tooltip
                  ></el-table-column>
                  <el-table-column
                      prop="executorName"
                      align="center"
                      label="执行人"
                      width="90"
                  ></el-table-column>
                  <el-table-column
                      prop="leftTime"
                      align="center"
                      label="剩余时间"
                      min-width="120"
                  ></el-table-column>
                  <el-table-column
                      prop="planSubmissionTimeStr"
                      label="提交时间"
                      align="center"
                      min-width="100"
                  ></el-table-column>
                  <el-table-column
                      fixed="right"
                      label="操作"
                      align="center"
                      :width="getOpeAreaWidth">
                    <template slot-scope="scope">
                      <el-button v-on:click.stop="jobItemDealWith(scope.row)" type="primary" plain size="small">
                        <el-badge :is-dot="scope.row.submitJobNum >0" >
                          任务处理
                        </el-badge>
                      </el-button>
                      <el-button
                          v-if="scope.row.type === JOB_ITEM_TYPE.SUGGEST.key"
                          v-on:click.stop="refuseJobitem(scope.row)" type="warning" plain size="small">
                        取消建议
                      </el-button>
                      <el-button v-if="((scope.row.type === JOB_ITEM_TYPE.DEVELOP.key
                      || scope.row.type === JOB_ITEM_TYPE.QUALITY.key
                      || scope.row.type === JOB_ITEM_TYPE.TASK_BUG.key
                      || scope.row.type === JOB_ITEM_TYPE.ASSIST.key
                      || scope.row.type === JOB_ITEM_TYPE.TEST.key
                      || scope.row.type === JOB_ITEM_TYPE.CHARACTER_INT.key
                      || scope.row.type === JOB_ITEM_TYPE.TASK_SUBP.key
                      || scope.row.type === JOB_ITEM_TYPE.TASK_FUNCTION.key
                      || scope.row.type === JOB_ITEM_TYPE.TASK_PRODUCT.key
                      || scope.row.type === JOB_ITEM_TYPE.TASK_TEST.key
                      || scope.row.type === JOB_ITEM_TYPE.TASK.key)
                      && scope.row.reviewResult !== REVIEW_RESULT.CONDITIONAL.key)"
                                 v-on:click.stop="materialApplication(scope.row)" type="primary" plain size="small">
                        领料申请
                      </el-button>
                      <el-button v-if="((scope.row.type === JOB_ITEM_TYPE.DEVELOP.key
                      || scope.row.type === JOB_ITEM_TYPE.QUALITY.key
                      || scope.row.type === JOB_ITEM_TYPE.TASK_BUG.key
                      || (scope.row.type === JOB_ITEM_TYPE.ASSIST.key)
                      || scope.row.type === JOB_ITEM_TYPE.TEST.key
                      || scope.row.type === JOB_ITEM_TYPE.CHARACTER_INT.key
                      || scope.row.type === JOB_ITEM_TYPE.TASK_SUBP.key
                      || scope.row.type === JOB_ITEM_TYPE.TASK_FUNCTION.key
                      || scope.row.type === JOB_ITEM_TYPE.TASK_PRODUCT.key
                      || scope.row.type === JOB_ITEM_TYPE.TASK_TEST.key
                      || scope.row.type === JOB_ITEM_TYPE.TASK.key)
                      && scope.row.reviewResult !== REVIEW_RESULT.CONDITIONAL.key)"
                                 v-on:click.stop="feeApplication(scope.row)" type="primary" plain size="small">
                        费用申请
                      </el-button>
                    </template>
                  </el-table-column>
                </el-table>
              </div>
            
            
                            </template></DynamicTabPane>

            <!--我的文献-->
                        <DynamicTabPane
                page-id="archive"
                :active-page-id="activePageId">
                            <template v-slot:default>
              <div class="paper-height">
                <div v-if="activePageId === 'archive'" style="height: 100%; overflow: scroll; ">
                  <ArchiveReferenceTree
                      :key="((loginUser.customerUserId))"
                      :worker-id="loginUser.customerUserId"
                  ></ArchiveReferenceTree>
                </div>
              </div>
            
            
                            </template></DynamicTabPane>

            <!--完工清单-->
                        <DynamicTabPane
                page-id="complete-task"
                :active-page-id="activePageId">
                            <template v-slot:default>
              <div class="paper-height">
                <div v-if="activePageId === 'complete-task'" style="height: 100%; overflow: scroll; ">
                  <WorkerCompleteTaskList
                      :key="((loginUser.customerUserId))"
                      :worker-id="loginUser.customerUserId"
                  ></WorkerCompleteTaskList>
                </div>
              </div>
            
            
                            </template></DynamicTabPane>

            <!--物料管理-->
                        <DynamicTabPane
                page-id="material"
                :active-page-id="activePageId">
                            <template v-slot:default>
              <div v-if="activePageId === 'material'">
                <MaterialApplicationList
                    :key="((loginUser.customerUserId) + '-' + (random))"
                    :writer-id="loginUser.customerUserId"
                ></MaterialApplicationList>
              </div>
            
            
                            </template></DynamicTabPane>

            <!--费用管理-->
                        <DynamicTabPane
                page-id="fee-manage"
                :active-page-id="activePageId">
                            <template v-slot:default>
              <div v-if="activePageId === 'fee-manage'">
                <FeeApplicationList
                    :key="((loginUser.customerUserId) + '-' + (random))"
                    :writer-id="loginUser.customerUserId"
                ></FeeApplicationList>
              </div>
            
            
                            </template></DynamicTabPane>

            <!--缺陷反馈-->
                        <DynamicTabPane
                page-id="bug-feedback"
                :active-page-id="activePageId">
                            <template v-slot:default>
              <div v-if="activePageId === 'bug-feedback'">
                <BugFeedbackList
                    :key="((loginUser.customerUserId) + '-' + (random))"
                    :writer-id="loginUser.customerUserId"
                ></BugFeedbackList>
              </div>
            
            
                            </template></DynamicTabPane>

            <!--增补特性建议-->
                        <DynamicTabPane
                page-id="character-suggest"
                :active-page-id="activePageId">
                            <template v-slot:default>
              <div v-if="loginUser.customerUserId && activePageId === 'character-suggest'">
                <CharacterSuggestList
                    :key="((loginUser.customerUserId) + '-' + (random))"
                    :writer-id="loginUser.customerUserId"
                ></CharacterSuggestList>
              </div>
            
            
                            </template></DynamicTabPane>


            <!-- 任务处理-->
            <DynamicTabPane
                v-for="(item, index) in jobItemList"
                :key="item.id"
                :index="index"
                prefix="job-item-item"
                :active-page-id="activePageId">
                <template v-slot:default>
              <div v-if="item.type === JOB_ITEM_TYPE.QUALITY.key">
                <JobitemQualityTaskTab
                    :key="item.id"
                    :jobitem-id="item.id"
                    :job-type="item.type"
                    :project-type="item.projectType"
                    :index="index"
                    :entrance="ENTRANCE_CLASS.MANHOUR.key">
                </JobitemQualityTaskTab>
              </div>
              <div v-else-if="item.type === JOB_ITEM_TYPE.TASK_BUG.key">
                <JobitemBugTaskTab
                    :key="item.id"
                    :jobitem-id="item.id"
                    :job-type="item.type"
                    :project-type="item.projectType"
                    :index="index"
                    :entrance="ENTRANCE_CLASS.MANHOUR.key">
                </JobitemBugTaskTab>
              </div>
              <div v-else-if="item.type === JOB_ITEM_TYPE.DEMAND.key">
                <jobitem-demand-container
                    :key="item.id"
                    :jobitem-id="item.id"
                    :job-type="item.type"
                    :project-type="item.projectType"
                    :index="index"
                    :entrance="ENTRANCE_CLASS.MANHOUR.key">
                </jobitem-demand-container>
              </div>
              <div v-else-if="item.type === JOB_ITEM_TYPE.DECOMPOSE.key">
                <jobitem-decompose-container
                    :key="item.id"
                    :jobitem-id="item.id"
                    :job-type="item.type"
                    :project-type="item.projectType"
                    :index="index"
                    :entrance="ENTRANCE_CLASS.MANHOUR.key">
                </jobitem-decompose-container>
              </div>
              <div v-else-if="item.type ===  JOB_ITEM_TYPE.ITERATION.key ">
                <jobitem-iteration-container
                    :key="item.id"
                    :job-item="item"
                    :jobitem-id="item.id"
                    :job-type="item.type"
                    :project-type="item.projectType"
                    :index="index"
                    :entrance="ENTRANCE_CLASS.MANHOUR.key">
                </jobitem-iteration-container>
              </div>
              <div v-else-if="item.type === JOB_ITEM_TYPE.UPDATE.key">
                <JobitemUpdateContainer
                    :key="item.id"
                    :job-item="item"
                    :jobitem-id="item.id"
                    :job-type="item.type"
                    :project-type="item.projectType"
                    :index="index"
                    :entrance="ENTRANCE_CLASS.MANHOUR.key">
                </JobitemUpdateContainer>
              </div>
              <div v-else-if="item.type === JOB_ITEM_TYPE.MERGE.key">
                <JobitemMergeContainer
                    :key="item.id"
                    :job-item="item"
                    :jobitem-id="item.id"
                    :job-type="item.type"
                    :project-type="item.projectType"
                    :index="index"
                    :entrance="ENTRANCE_CLASS.MANHOUR.key">
                </JobitemMergeContainer>
              </div>
              <div v-else-if="item.type === JOB_ITEM_TYPE.CHA_RECHECK.key">
                <jobitem-character-evaluate-container
                    :key="item.id"
                    :jobitem-id="item.id"
                    :job-type="item.type"
                    :project-type="item.projectType"
                    :index="index"
                    :entrance="ENTRANCE_CLASS.MANHOUR.key">
                </jobitem-character-evaluate-container>
              </div>
              <div v-else-if="item.type === JOB_ITEM_TYPE.DEVELOP.key">
                <jobitem-develop-container
                    :key="item.id"
                    :jobitem-id="item.id"
                    :job-type="item.type"
                    :project-type="item.projectType"
                    :index="index"
                    :entrance="ENTRANCE_CLASS.MANHOUR.key">
                </jobitem-develop-container>
              </div>
              <div v-else-if="item.type === JOB_ITEM_TYPE.TASK_TEST.key">
                <jobitem-develop-container
                    :key="item.id"
                    :jobitem-id="item.id"
                    :job-type="item.type"
                    :project-type="item.projectType"
                    :index="index"
                    :entrance="ENTRANCE_CLASS.MANHOUR.key">
                </jobitem-develop-container>
              </div>
              <div v-else-if="item.type === JOB_ITEM_TYPE.TEST.key">
                <div>
                  <JobitemTaskTabTest
                      :key="item.id"
                      :jobitem-id="item.id"
                      :job-type="item.type"
                      :project-type="item.projectType"
                      :index="index"
                      :entrance="ENTRANCE_CLASS.MANHOUR.key">
                  </JobitemTaskTabTest>
                </div>
              </div>
              <div v-else-if="item.type === JOB_ITEM_TYPE.ASSIST.key || item.type === JOB_ITEM_TYPE.BUG.key">
                <jobitem-assist-container
                    :key="item.id"
                    :jobitem-id="item.id"
                    :job-type="item.type"
                    :project-type="item.projectType"
                    :index="index"
                    :entrance="ENTRANCE_CLASS.MANHOUR.key">
                </jobitem-assist-container>
              </div>
              <div v-else-if="item.type === JOB_ITEM_TYPE.REVIEW.key || item.type === JOB_ITEM_TYPE.REVIEW_CHANGE.key">
                <jobitem-review-pjm-container
                    :key="item.id"
                    :jobitem-id="item.id"
                    :job-type="item.type"
                    :project-type="item.projectType"
                    :index="index"
                    :entrance="ENTRANCE_CLASS.MANHOUR.key">
                </jobitem-review-pjm-container>
              </div>
              <div v-else-if="item.type === JOB_ITEM_TYPE.REVIEW_PRO.key || item.type === JOB_ITEM_TYPE.REVIEW_SUBP.key
                || item.type === JOB_ITEM_TYPE.REVIEW_MILESTONE.key">
                <jobitem-review-po-container
                    :key="item.id"
                    :jobitem-id="item.id"
                    :job-type="item.type"
                    :project-type="item.projectType"
                    :index="index"
                    :entrance="ENTRANCE_CLASS.MANHOUR.key">
                </jobitem-review-po-container>
              </div>
              <div v-else-if="item.type === JOB_ITEM_TYPE.REVIEW_COO.key">
                <JobitemReviewCooContainer
                    :key="item.id"
                    :project-type="item.projectType"
                    :jobitem-id="item.id"
                    :job-type="item.type"
                    :index="index"
                    :entrance="ENTRANCE_CLASS.MANHOUR.key">
                </JobitemReviewCooContainer>
              </div>
              <div v-else-if="item.type === JOB_ITEM_TYPE.REVIEW_NOTIFY.key">
                <JobitemReviewNotifyContainer
                    :key="item.id"
                    :project-type="item.projectType"
                    :jobitem-id="item.id"
                    :job-type="item.type"
                    :index="index"
                    :entrance="ENTRANCE_CLASS.MANHOUR.key">
                </JobitemReviewNotifyContainer>
              </div>
              <div v-else-if="item.type === JOB_ITEM_TYPE.MILESTONE_NOTIFY.key">
                <JobitemMilestoneNotifyContainer
                    :key="item.id"
                    :project-type="item.projectType"
                    :jobitem-id="item.id"
                    :job-type="item.type"
                    :index="index"
                    :entrance="ENTRANCE_CLASS.MANHOUR.key">
                </JobitemMilestoneNotifyContainer>
              </div>
              <div v-else-if="item.type === JOB_ITEM_TYPE.BUDGET_ADJUST_NOTIFY.key || item.type === JOB_ITEM_TYPE.DEFINE_UPDATE_NOTIFY.key
                || item.type === JOB_ITEM_TYPE.CBB_UPDATE_NOTIFY.key || item.type === JOB_ITEM_TYPE.PLAN_ISSUE_NOTIFY.key">
                <BudgetAdjustNotifyContainer
                    :key="item.id"
                    :project-type="item.projectType"
                    :jobitem-id="item.id"
                    :job-type="item.type"
                    :index="index"
                    :entrance="ENTRANCE_CLASS.MANHOUR.key"
                    :confirm-path="notifyConfirmPath(item.type)">
                </BudgetAdjustNotifyContainer>
              </div>
              <div v-else-if="item.type === JOB_ITEM_TYPE.REFUSE_NOTIFY.key">
                <JobitemRefuseNotifyContainer
                    :key="item.id"
                    :project-type="item.projectType"
                    :jobitem-id="item.id"
                    :job-type="item.type"
                    :index="index"
                    :entrance="ENTRANCE_CLASS.MANHOUR.key">
                </JobitemRefuseNotifyContainer>
              </div>
              <div v-else-if="item.type === JOB_ITEM_TYPE.CHARACTER_INT.key ">
                <JobitemTaskTab
                    :key="item.id"
                    :jobitem-id="item.id"
                    :job-type="item.type"
                    :project-type="item.projectType"
                    :index="index"
                    :entrance="ENTRANCE_CLASS.MANHOUR.key">
                </JobitemTaskTab>
              </div>
              <div v-else-if="item.type === JOB_ITEM_TYPE.TASK.key ">
                <JobitemTaskTab
                    :key="item.id"
                    :jobitem-id="item.id"
                    :job-type="item.type"
                    :project-type="item.projectType"
                    :index="index"
                    :entrance="ENTRANCE_CLASS.MANHOUR.key">
                </JobitemTaskTab>
              </div>
              <div v-else-if="item.type === JOB_ITEM_TYPE.TASK_SUBP.key || item.type === JOB_ITEM_TYPE.TASK_PRO.key">
                <JobitemTaskTab
                    :key="item.id"
                    :jobitem-id="item.id"
                    :job-type="item.type"
                    :project-type="item.projectType"
                    :index="index"
                    :entrance="ENTRANCE_CLASS.MANHOUR.key">
                </JobitemTaskTab>
              </div>
              <div v-else-if="item.type === JOB_ITEM_TYPE.TASK_PRODUCT.key">
                <JobitemTaskTab
                    :key="item.id"
                    :jobitem-id="item.id"
                    :job-type="item.type"
                    :project-type="item.projectType"
                    :index="index"
                    :entrance="ENTRANCE_CLASS.MANHOUR.key">
                </JobitemTaskTab>
              </div>
              <div v-else-if="item.type === JOB_ITEM_TYPE.TASK_FUNCTION.key">
                <JobitemTaskTab
                    :key="item.id"
                    :jobitem-id="item.id"
                    :job-type="item.type"
                    :project-type="item.projectType"
                    :index="index"
                    :entrance="ENTRANCE_CLASS.MANHOUR.key">
                </JobitemTaskTab>
              </div>
              <div v-else-if="item.type === JOB_ITEM_TYPE.REVIEW_MILESTONE_PRE.key">
                <JobitemMilestonePreContainer
                    :key="item.id"
                    :jobitem-id="item.id"
                    :job-type="item.type"
                    :project-type="item.projectType"
                    :index="index"
                    :entrance="ENTRANCE_CLASS.MANHOUR.key">
                </JobitemMilestonePreContainer>
              </div>
              <div v-else-if="item.type === JOB_ITEM_TYPE.SUGGEST.key">
                <JobitemSuggestContainer
                    :key="item.id"
                    :jobitem-id="item.id"
                    :job-type="item.type"
                    :project-type="item.projectType"
                    :index="index"
                    :entrance="ENTRANCE_CLASS.MANHOUR.key">
                </JobitemSuggestContainer>
              </div>
              <div v-else-if="item.type === JOB_ITEM_TYPE.SUGGEST_UPDATE.key">
                <JobitemAllInfoPlain
                    :key="item.id"
                    :jobitem-id="item.id"
                    :job-type="item.type"
                    :project-type="item.projectType"
                    :index="index"
                    :entrance="ENTRANCE_CLASS.MANHOUR.key">
                </JobitemAllInfoPlain>
              </div>
                <div v-else-if="item.type === JOB_ITEM_TYPE.CBB_DEFINE.key">
                  <JobitemCbbDefineContainer
                      :key="item.id"
                      :jobitem-id="item.id"
                      :job-type="item.type"
                      :project-type="item.projectType"
                      :index="index"
                      :entrance="ENTRANCE_CLASS.MANHOUR.key">
                  </JobitemCbbDefineContainer>
              </div>
              <div v-else-if="item.type === JOB_ITEM_TYPE.CHARACTER_TERMINATE.key">
                <JobitemCharacterTerminateContainer
                    :key="item.id"
                    :jobitem-id="item.id"
                    :job-type="item.type"
                    :project-type="item.projectType"
                    :index="index"
                    :entrance="ENTRANCE_CLASS.MANHOUR.key">
                </JobitemCharacterTerminateContainer>
              </div>
              <div v-else>
                <jobitem-develop-container
                    :key="item.id"
                    :jobitem-id="item.id"
                    :job-type="item.type"
                    :project-type="item.projectType"
                    :index="index"
                    :entrance="ENTRANCE_CLASS.MANHOUR.key">
                </jobitem-develop-container>
              </div>
            
                </template></DynamicTabPane>
          
    </WorkspaceFrame>

    <!--领料申请-->
    <el-dialog
        width="36cm"
        :visible.sync="materialApplicationDlg"
        :before-close="handleClose"
        :close-on-click-modal=false>
      <div slot="title" style="font-size: 20px;">{{ dialogTitle }}</div>
      <div v-if="materialApplicationDlg">
        <MaterialApplicationDlgSap
            :key="((jobitemDlg.id) + '-' + (random))"
            :jobitem-id="jobitemDlg.id"
            :inherit-job-type="jobitemDlg.auxType"
            :project-type="jobitemDlg.projectType"
            :close-dlg=closeDialog
        ></MaterialApplicationDlgSap>
      </div>
    </el-dialog>

    <!--费用申请-->
    <el-dialog
        width="29cm"
        :visible.sync="feeApplicationDlg"
        :before-close="handleClose"
        :close-on-click-modal=false>
      <div slot="title" style="font-size: 20px;">{{ dialogTitle }}</div>
      <div v-if="feeApplicationDlg">
        <FeeApplicationDlg
            :key="((jobitemId) + '-' + (random))"
            :jobitem-id="jobitemId"
            :inherit-job-type="jobitemDlg.auxType"
            :project-type="jobitemDlg.projectType"
            :close-dlg=closeDialog
        ></FeeApplicationDlg>
      </div>
    </el-dialog>

    <!--调阅文档申请-->
    <el-dialog
        width="29cm"
        :visible.sync="archiveApplicationDlg"
        :before-close="handleClose"
        :close-on-click-modal=false>
      <div slot="title" style="font-size: 20px;">{{ dialogTitle }}</div>
      <div v-if="archiveApplicationDlg">
        <ArchiveApplicationDlg
            :key="jobitemId"
            :jobitem-id="jobitemId"
            :close-dlg=closeDialog
        ></ArchiveApplicationDlg>
      </div>
    </el-dialog>
    <div>
      <el-dialog
          fullscreen
          :visible.sync="issueJobitemDlg"
          :before-close="handleClose"
          :close-on-click-modal=false>
        <div style="margin-top: -10px; margin-left: 30px; position: relative;">
          <div style="position: absolute; right: 45px; top: -62px; font-size: 26px; font-weight: 600; color: #303133;">
            {{ loginCustomer.customerName || loginCustomer.name }}
          </div>
          <div v-if="issueJobitemDlg">
            <ProjectPerformanceListPage
                v-bind:customer-id="loginCustomer.id">
            </ProjectPerformanceListPage>
          </div>
        </div>
        <div style="margin: 0 30px ">
          <div style="display: flex; flex-direction: row; justify-content: space-between; align-items: center;">
            <div style="font-size: 16px;">上述数据由<span style="color: #ae0404; font-weight: 550">EUIPD</span>统计发布，每日更新</div>
          </div>
        </div>
      </el-dialog>
    </div>
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
import JobitemDecomposeContainer from "@/components/rdms/jobitem-decompose-container";
import JobitemIterationContainer from "@/components/rdms/jobitem-iteration-container";
import JobitemDemandContainer from "@/components/rdms/jobitem-demand-container";
import JobitemDevelopContainer from "@/components/rdms/jobitem-develop-container";
import JobitemTaskTab from "@/components/rdms/jobitem-task-tab.vue";
import JobitemAssistContainer from "@/components/rdms/jobitem-assist-container";
import JobitemReviewCooContainer from "@/components/rdms/jobitem-review-coo-container.vue";
import JobitemReviewPjmContainer from "@/components/rdms/jobitem-review-pjm-container";
import JobitemReviewNotifyContainer from "@/components/rdms/jobitem-review-notify-container";
import JobitemCharacterEvaluateContainer from "@/components/rdms/jobitem-character-recheck-container.vue";
import JobitemRefuseNotifyContainer from "@/components/rdms/jobitem-refuse-notify-container.vue";
import JobitemUpdateContainer from "@/components/rdms/jobitem-update-container.vue";
import JobitemTaskTabTest from "@/components/rdms/jobitem-task-tab-test.vue";
import JobitemMergeContainer from "@/components/rdms/jobitem-merge-container.vue";
import {formatSecond, optionKV} from "@/filter/filter";
import JobitemReviewPoContainer from "@/components/rdms/jobitem-review-po-container.vue";
import FeeApplicationDlg from "@/components/std/fee-application-dlg.vue";
import MaterialApplicationList from "@/components/rdms/material-application-list.vue";
import FeeApplicationList from "@/components/rdms/fee-application-list.vue";
import JobitemQualityAnalysisContainer from "@/components/rdms/jobitem-quality-analysis-container.vue";
import JobitemQualityTaskTab from "@/components/rdms/jobitem-quality-task-tab.vue";
import ArchiveApplicationDlg from "@/components/std/archive-application-dlg.vue";
import ArchiveReferenceTree from "@/components/rdms/archive-reference-tree.vue";
import JobitemMilestoneNotifyContainer from "@/components/rdms/jobitem-milestone-notify-container.vue";
import JobitemMilestonePreContainer from "@/components/rdms/jobitem-milestone-pre-container.vue";
import CharacterSuggestList from "@/components/rdms/character-suggest-list.vue";
import JobitemSuggestContainer from "@/components/rdms/jobitem-suggest-container.vue";
import JobitemAllInfoPlain from "@/components/std/jobitem-all-info-plain.vue";
import BudgetAdjustNotifyContainer from "@/components/rdms/budget-adjust-notify-container.vue";
import MaterialApplicationDlgSap from "@/components/std/material-application-dlg-sap.vue";
import ProjectPerformanceListPage from "@/components/rdms/project-performance-list-page.vue";
import BugFeedbackList from "@/components/rdms/bug-feedback-list.vue";
import JobitemBugTaskTab from "@/components/rdms/jobitem-bug-task-tab.vue";
import WorkerCompleteTaskList from "@/components/rdms/worker-complete-task-list.vue";
import JobitemCbbDefineContainer from "@/components/rdms/jobitem-cbb-define-container.vue";
import JobitemCharacterTerminateContainer from "@/components/rdms/jobitem-character-terminate-container.vue";

export default {
  components: {
    ProjectPerformanceListPage,
    JobitemReviewPoContainer,
    Pagination,
    File,
    DynamicTabItem,
    DynamicTabPane,
    WorkspaceFrame,
    JobitemDecomposeContainer,
    JobitemIterationContainer,
    JobitemDemandContainer,
    JobitemDevelopContainer,
    JobitemTaskTab,
    JobitemAssistContainer,
    JobitemReviewCooContainer,
    JobitemReviewPjmContainer,
    JobitemReviewNotifyContainer,
    JobitemRefuseNotifyContainer,
    JobitemCharacterEvaluateContainer,
    JobitemUpdateContainer,
    JobitemTaskTabTest,
    JobitemMergeContainer,
    MaterialApplicationList,
    FeeApplicationDlg,
    FeeApplicationList,
    JobitemQualityAnalysisContainer,
    JobitemQualityTaskTab,
    ArchiveApplicationDlg,
    ArchiveReferenceTree,
    JobitemMilestoneNotifyContainer,
    JobitemMilestonePreContainer,
    CharacterSuggestList,
    JobitemSuggestContainer,
    JobitemAllInfoPlain,
    BudgetAdjustNotifyContainer,
    MaterialApplicationDlgSap,
    BugFeedbackList,
    JobitemBugTaskTab,
    WorkerCompleteTaskList,
    JobitemCbbDefineContainer,
    JobitemCharacterTerminateContainer,
  },
  name: "manhour",

  data: function () {
    return {
      LOGIN_USER_GROUP: LOGIN_USER_GROUP,
      JOB_ITEM_TYPE: JOB_ITEM_TYPE,
      ENTRANCE_CLASS: ENTRANCE_CLASS,
      REVIEW_RESULT: REVIEW_RESULT,
      PROJECT_TYPE: PROJECT_TYPE,

      activePageId: 'home',

      jobItems: [],
      jobitem: {},
      jobitemDlg: {},
      jobitemId: '',
      jobItemList: [],

      loginUser: {},
      loginCustomer: {},

      suggestNum: {},

      materialApplicationDlg: false,
      feeApplicationDlg: false,
      archiveApplicationDlg: false,
      dialogTitle: '',

      random: 0,
      currentPageNo: 1,
      issueJobitemDlg: false,
      keyWord: '',
    }
  },

  computed: {
    getOpeAreaWidth() {
      return 320;
    }
  },

  mounted: function () {
    let _this = this;
    _this.getLoginUser();
    _this.getSuggestNum();

    _this.$refs.pagination.size = 12;
    _this.list_jobitems(_this.currentPageNo);

    _this.$bus.$on('close-manhour-tab', (index) => {
      _this.closeFunction_job_item(index);
    });
    _this.unsubscribeManhour = _this.$bus.subscribe(['jobitem'], function () {
      _this.list_jobitems(_this.currentPageNo);
      _this.getSuggestNum();
    });
  },

  beforeDestroy: function () {
    let _this = this;
    _this.$bus.$off('close-manhour-tab');
    if (_this.unsubscribeManhour) {
      _this.unsubscribeManhour();
    }
  },

  methods: {
    notifyConfirmPath(type) {
      if (type === JOB_ITEM_TYPE.DEFINE_UPDATE_NOTIFY.key) {
        return '/customer/job-items/confirmDefineUpdateNotify/';
      }
      if (type === JOB_ITEM_TYPE.CBB_UPDATE_NOTIFY.key) {
        return '/customer/job-items/confirmCbbUpdateNotify/';
      }
      if (type === JOB_ITEM_TYPE.PLAN_ISSUE_NOTIFY.key) {
        return '/customer/job-items/confirmPlanIssueNotify/';
      }
      return '/customer/job-items/confirmBudgetAdjustNotify/';
    },
    getLoginUser() {
      let _this = this;
      if (Tool.getLoginUser().userGroup === LOGIN_USER_GROUP.CUSTOMER.key) {
        _this.loginUser = Tool.getLoginUser().adminUser;
        _this.loginCustomer = Tool.getLoginUser();
      } else {
        _this.loginUser = Tool.getLoginUser();
        _this.loginCustomer = Tool.getLoginUser().customer;
      }
    },
    showPerformance() {
      let _this = this;
      _this.issueJobitemDlg = true;
    },
    materialApplication(jobitem) {
      let _this = this;
      _this.jobitemId = jobitem.id;
      _this.jobitemDlg = jobitem;
      _this.materialApplicationDlg = true;
      _this.dialogTitle = "领料申请";
      _this.random = Math.random();
    },
    feeApplication(jobitem) {
      let _this = this;
      _this.jobitemId = jobitem.id;
      _this.jobitemDlg = jobitem;
      _this.feeApplicationDlg = true;
      _this.dialogTitle = "费用申请";
      _this.random = Math.random();
    },

    archiveApplication(jobitem) {
      let _this = this;
      _this.jobitemId = jobitem.id;
      _this.jobitemDlg = jobitem;
      _this.archiveApplicationDlg = true;
      _this.dialogTitle = "文档调阅申请";
      _this.random = Math.random();
    },

    closeDialog() {
      let _this = this;
      _this.materialApplicationDlg = false;
      _this.feeApplicationDlg = false;
      _this.archiveApplicationDlg = false;
    },
    handleClose(done) {
      done();
    },

    //tab点击事件
    tabJobItemClick(i) {
      let _this = this;
      _this.activePageId = `job-item-item-${i}`;
    },

    tabHomeClick() {
      let _this = this;
      _this.activePageId = 'home';
      _this.list_jobitems(_this.currentPageNo);
    },

    feeManageClick() {
      let _this = this;
      _this.activePageId = 'fee-manage';
      _this.random = Math.random();
    },

    bugFeedbackClick() {
      let _this = this;
      _this.activePageId = 'bug-feedback';
      _this.random = Math.random();
    },

    suggestClick() {
      let _this = this;
      _this.activePageId = 'character-suggest';
      _this.random = Math.random();
      _this.$bus.$emit('refresh-manhour-num', true);
    },

    archiveManageClick() {
      let _this = this;
      _this.activePageId = 'archive';
      _this.random = Math.random();
    },

    completeTaskClick() {
      let _this = this;
      _this.activePageId = 'complete-task';
      _this.random = Math.random();
    },

    materialManageClick() {
      let _this = this;
      _this.activePageId = 'material';
      _this.random = Math.random();
    },

    jobitemTypeFormat(row) {
      return optionKV(JOB_ITEM_TYPE, row.type);
    },

    processStatusFormat(row) {
      return optionKV(JOB_ITEM_PROCESS, row.processStatus);
    },

    rowClick(row) {
      let _this = this;
      _this.jobItemDealWith(row);
    },

    projectNameFormat(row) {
      if (Tool.isEmpty(row.projectName)) {
        return row.preProjectName;
      }
      return row.projectName;
    },

    refuseJobitem(jobItem) {
      let _this = this;
      Swal.fire({
        title: '取消建议',
        icon: 'question',
        text: "是否确认取消本条增补建议?",
        showCancelButton: true,
        confirmButtonColor: '#409EFF',
        cancelButtonColor: '#909399',
        confirmButtonText: '确认!',
        cancelButtonText: '返回'
      }).then((result) => {
        if (result.isConfirmed) {
          _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/job-items/closeJobitem/' + jobItem.id).then((response) => {
            let resp = response.data;
            if (resp.success) {
              Toast.success("操作成功!", Swal);
              _this.list_jobitems(_this.currentPageNo);
            } else {
              Toast.warning(resp.message, Swal);
            }
          });
        }
      }).catch(_ => {
      });
    },

    jobItemDealWith(jobItem) {
      // 统一待办会列出待我审批、我任项目经理的工单；这里只承载执行人处理，审批在各管理页的审核入口。
      // CBB 定义审核、特性终止审批没有独立管理页，已提交且下一节点为本人时在此打开审核视图
      const cbbDefineReview = (jobItem.type === JOB_ITEM_TYPE.CBB_DEFINE.key
              || jobItem.type === JOB_ITEM_TYPE.CHARACTER_TERMINATE.key)
          && jobItem.status === JOB_ITEM_STATUS.SUBMIT.key
          && jobItem.nextNode === this.loginUser.customerUserId;
      if (!cbbDefineReview
          && (jobItem.executorId !== this.loginUser.customerUserId || jobItem.status !== JOB_ITEM_STATUS.HANDLING.key)) {
        Toast.warning("该任务不由您执行" + (jobItem.status === JOB_ITEM_STATUS.SUBMIT.key ? "，已提交待审批" : "")
            + "，请到所属项目、研发或质量管理的审核页处理", Swal);
        return;
      }
      openOrFocusTab(this, this.jobItemList, jobItem, 'job-item-item');
    },
    closeFunction_job_item(index) {
      closeDynamicTab(this, {
        list: this.jobItemList,
        index: index,
        prefix: 'job-item-item',
        fallback: 'home',
        afterClose: () => this.list_jobitems(this.currentPageNo),
      });
    },

//查询所有任务
    list_jobitems(page, retried) {
      let _this = this;
      _this.currentPageNo = page;
      // 列表页签未选中时不渲染分页组件；切回列表页签会重新查询
      if (!_this.$refs.pagination) {
        if (_this.activePageId === 'home' && !retried) {
          _this.$nextTick(() => _this.list_jobitems(page, true));
        }
        return;
      }
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/job-items/listByLoginUserId', {
        page: page,
        size: _this.$refs.pagination.size,
        loginUserId: _this.loginUser.customerUserId, //adminUserId
        keyWord: '',  //不进行数据过滤
      }).then((response) => {
        // debugger
        let resp = response.data;
        if (resp.success) {
          _this.jobItems = resp.content.list;
          //计算剩余时间
          _this.jobItems.forEach((item) => {
            if (item.planSubmissionTime) {
              let leftTime = new Date(item.planSubmissionTime).getTime();
              let left = leftTime / 1000 - Date.now() / 1000;
              item.leftTime = formatSecond(left);
            }
          })
          _this.$refs.pagination.render(page, resp.content.total);
        } else {
          Toast.warning(resp.message, Swal);
        }
      })
    },
    search(page) {
      let _this = this;
      if (Tool.isNotEmpty(_this.keyWord)) {
        _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/job-items/listByLoginUserId', {
          page: page,
          size: _this.$refs.pagination.size,
          loginUserId: _this.loginUser.customerUserId,
          keyWord: _this.keyWord,
        }).then((response) => {
          let resp = response.data;
          _this.jobItems = resp.content.list;
          _this.$refs.pagination.render(page, resp.content.total);
        })
      } else {
        _this.list_jobitems(page);
      }
    },

    getSuggestNum() {
      let _this = this;
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/character/get-suggest-items-num/' + _this.loginUser.customerUserId).then((response) => {
        let resp = response.data;
        if (resp.success) {
          _this.suggestNum = resp.content;
        } else {
          Toast.warning(resp.message, Swal);
        }
      })
    },

    tableRowClass({row, rowIndex}) {
      // console.log("row:", Date.parse(row.planCompleteTime));
      if (Date.now() > Date.parse(row.planSubmissionTime)) {
        return 'warning-row-3';
      }
      return '';
    }
  }
}
</script>


<style lang="scss" scoped>
.paper-height {
  height: calc(100vh - 210px);
}
</style>
