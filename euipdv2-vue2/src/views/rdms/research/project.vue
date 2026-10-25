<!--
 * Author: 周朔鹏
 * Date: 2025-03-05
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
                开发项目
              </a>
            </li>
            <li :class="{active : activePageId==='archive'}">
              <a @click="tabArchiveClick">
                项目档案
              </a>
            </li>
            <li :class="{active : activePageId==='project-data'}">
              <a @click="tabProjectDataClick">
                项目数据(实时)
              </a>
            </li>

            <!--任务管理-->
            <DynamicTabItem
                v-for="(item, index) in projectTaskItems"
                :key="('task-manage-' + (item.id) + '-' + (index))"
                :index="index"
                :active-page-id="activePageId"
                prefix="task-manage"
                :close="closeFunction_develop"
                @select="developManagerClick">{{ ('任务:' + (item.label)) }}
            </DynamicTabItem>

            <!--执行数据-->
            <DynamicTabItem
                v-for="(item, index) in projectArchiveItems"
                :key="('archive-tree-' + (item.id) + '-' + (index))"
                :index="index"
                :active-page-id="activePageId"
                prefix="archive-tree"
                :close="closeFunction_archive_data"
                @select="developArchiveClick">{{ ('文档:' + (item.label)) }}
            </DynamicTabItem>

            <!--特性管理-->
            <DynamicTabItem
                v-for="(item, index) in projectSystemItems"
                :key="('project-system-' + (item.id) + '-' + (index))"
                :index="index"
                :active-page-id="activePageId"
                prefix="project-system"
                :close="closeFunction_system"
                @select="systemManagerClick">{{ ('系统:' + (item.label)) }}
            </DynamicTabItem>

            <!--项目管理-->
            <DynamicTabItem
                v-for="(item, index) in subprojectManageItems"
                :key="('subproject-manage-' + (item.id) + '-' + (index))"
                :index="index"
                :active-page-id="activePageId"
                prefix="subproject-manage-item"
                :close="closeFunction_manage"
                @select="subprojectManagerClick">{{ ('项目:' + (item.label)) }}
            </DynamicTabItem>

            <!--项目档案-->
            <DynamicTabItem
                v-for="(item, index) in projectArchiveList"
                :key="('project-archive-' + (item.id) + '-' + (index))"
                :index="index"
                :active-page-id="activePageId"
                prefix="project-archive-item"
                :close="closeFunction_archive"
                @select="tabProjectArchiveClick">{{ ('档案: ' + (item.label)) }}
            </DynamicTabItem>

            <!--任务清单-->
            <DynamicTabItem
                v-for="(item, index) in projectTaskList"
                :key="('project-task-list-' + (item.id) + '-' + (index))"
                :index="index"
                :active-page-id="activePageId"
                prefix="project-task-list"
                :close="closeFunction_taskList"
                @select="tabProjectTaskClick">{{ ('任务: ' + (item.label)) }}
            </DynamicTabItem>

            <!--物料管理-->
            <DynamicTabItem
                v-for="(item, index) in subprojectMaterialList"
                :key="('material-manage-' + (item.id) + '-' + (index))"
                :index="index"
                :active-page-id="activePageId"
                prefix="material-manage-item"
                :close="closeFunction_material"
                @select="tabMaterialItemClick">{{ ('物料 : ' + (item.label)) }}
            </DynamicTabItem>

            <!--费用管理-->
            <DynamicTabItem
                v-for="(item, index) in subprojectFeeList"
                :key="('fee-manage-' + (item.id) + '-' + (index))"
                :index="index"
                :active-page-id="activePageId"
                prefix="fee-manage-item"
                :close="closeFunction_fee"
                @select="tabFeeItemClick">{{ ('费用 : ' + (item.label)) }}
            </DynamicTabItem>

            <!--BOM结构化-->
            <DynamicTabItem
                v-for="(item, index) in subprojectBomStructure"
                :key="('bom-structure-' + (item.id) + '-' + (index))"
                :index="index"
                :active-page-id="activePageId"
                prefix="bom-structure-item"
                :close="closeBomStructureManage"
                @select="tabBomStructureClick">{{ ('BOM : ' + (item.label)) }}
            </DynamicTabItem>

            <!--gantt-->
            <DynamicTabItem
                v-for="(item, index) in subprojectGanttList"
                :key="('subproject-gantt-' + (item.id) + '-' + (index))"
                :index="index"
                :active-page-id="activePageId"
                prefix="subproject-gantt-item"
                :close="closeGantt"
                @select="tabGanttClick">{{ ('计划 : ' + (item.label)) }}
            </DynamicTabItem>

          
        </template>

                        <DynamicTabPane
                page-id="home"
                :active-page-id="activePageId">
                            <template v-slot:default>
              <div style="display: flex; flex-direction: row; justify-content: space-between; align-items: center;">
                <div>
                  <p>项目管理: 项目管理是给<strong>项目经理</strong>提供的一个综合管理界面. 通过这个界面可以对项目进行子项目分解, 分配特性, 创建任务等操作.</p>
                </div>
                <div>
                  <el-input
                      class="project-search-input"
                      v-model="searchName"
                      size="mini"
                      placeholder="页面内容搜索-输入项目名称"/>
                </div>
              </div>
              <div class="hr hr-18 dotted hr-double"></div>
              <div>
                <el-table
                    v-loading="tableLoading"
                    :data="pagedMixProjects"
                    border
                    highlight-current-row
                    :row-class-name="tableRowClass"
                    style="width: 100%"> <!--@row-click="rowClick"-->
                  <el-table-column
                      prop="projectCode"
                      min-width="150"
                      show-overflow-tooltip
                      align="center"
                      label="项目代号"
                  ></el-table-column>
                  <el-table-column
                      prop="label"
                      min-width="250"
                      align="center"
                      show-overflow-tooltip
                      label="项目名称"
                  ></el-table-column>
                  <el-table-column
                      prop="productManagerName"
                      align="center"
                      label="产品经理"
                      min-width="100"
                  ></el-table-column>
                  <el-table-column
                      prop="projectManagerName"
                      align="center"
                      label="项目经理"
                      min-width="100"
                  ></el-table-column>
                  <el-table-column
                      prop="testManagerName"
                      align="center"
                      label="测试主管"
                      min-width="100"
                  ></el-table-column>
                  <el-table-column
                      prop="systemManagerName"
                      align="center"
                      label="系统工程师"
                      min-width="100"
                  ></el-table-column>
                  <el-table-column
                      prop="superviseName"
                      label="监管委员"
                      align="center"
                      min-width="100"
                  ></el-table-column>
                  <el-table-column
                      prop="rdType"
                      align="center"
                      :formatter="rdTypeFormat"
                      label="研发类型"
                      min-width="100"
                  ></el-table-column>
                  <el-table-column
                      prop="createTime"
                      :formatter="creatTimeFormat"
                      label="创建时间"
                      align="center"
                      min-width="110"
                  ></el-table-column>
                  <el-table-column
                      prop="releaseTime"
                      :formatter="releaseTimeFormat"
                      label="发布时间"
                      align="center"
                      min-width="110"
                  ></el-table-column>
                  <el-table-column label="完工率" align="center" width="76">
                    <template slot-scope="scope">{{ statFmt.percent(statOf(scope.row).completeRate, 0) }}</template>
                  </el-table-column>
                  <el-table-column label="逾期数" align="center" width="70">
                    <template slot-scope="scope">
                      <span :class="{'project-stat-danger': statOf(scope.row).overdueNum > 0}">{{ statFmt.count(statOf(scope.row).overdueNum) }}</span>
                    </template>
                  </el-table-column>
                  <el-table-column label="预算执行率" align="center" width="90">
                    <template slot-scope="scope">{{ statFmt.percent(statOf(scope.row).budgetExeRate, 0) }}</template>
                  </el-table-column>
                  <el-table-column label="健康度" align="center" width="76">
                    <template slot-scope="scope">
                      <el-tooltip v-if="statOf(scope.row).health" :disabled="!(statOf(scope.row).healthReasons || []).length"
                                  :content="(statOf(scope.row).healthReasons || []).join('；')" placement="top">
                        <el-tag size="mini" :type="statFmt.healthType(statOf(scope.row).health)">{{ statFmt.healthName(statOf(scope.row).health) }}</el-tag>
                      </el-tooltip>
                      <span v-else>-</span>
                    </template>
                  </el-table-column>
                  <el-table-column
                      prop="status"
                      :formatter="statusFormat"
                      label="状态"
                      align="center"
                      min-width="110"
                  ></el-table-column>
                  <el-table-column
                      fixed="right"
                      label="操作"
                      align="center"
                      :width="getOpeAreaWidth">
                    <template slot-scope="scope">
                      <el-tooltip content="根据专业方向或者公司的部门设置,对已经立项的项目进行分解, 以便符合专业分工的要求" placement="top" effect="light" :open-delay="1000">
                        <el-button v-if="canManageProject(scope.row)" v-on:click.stop="subprojectManagement(scope.row)" type="primary" plain size="mini">
                          <el-badge :is-dot="false" >
                            项目管理
                          </el-badge>
                        </el-button>
                      </el-tooltip>
                      <el-tooltip content="对项目计划进行预先编辑, 以便实现任务自动下发功能." placement="top" effect="light" :open-delay="1000">
                        <el-button v-if="canManageProject(scope.row)" v-on:click.stop="ganttManage(scope.row)" type="primary" plain size="mini">
                          计划管理
                        </el-button>
                      </el-tooltip>
                      <el-tooltip content="对研发过程任务进行管理. 应当尽量避免使用任务任务处理特性开发." placement="top" effect="light" :open-delay="1000">
                        <el-button v-if="canManageTask(scope.row)" v-on:click.stop="developManagement(scope.row)" type="primary" plain size="small">
                          <el-badge :is-dot="scope.row.submitJobNum_subProject >0" >
                            任务管理
                          </el-badge>
                        </el-button>
                      </el-tooltip>
                      <el-tooltip content="对已经立项的特性进行修订和分解管理, 以丰富和完善细节. 在分解过程中, 应当充分考虑团队人员的知识结构和专业能力" placement="top" effect="light" :open-delay="1000">
                        <el-button v-if="canManageCharacter(scope.row)" v-on:click.stop="characterManagement(scope.row)" type="primary" plain size="mini">
                          <el-badge :is-dot="scope.row.submitJobNum > 0 || scope.row.evaluateJobNum >0" >
                            特性
                          </el-badge>
                        </el-button>
                      </el-tooltip>
                      <el-tooltip content="查看项目过程数据与归档树" placement="top" effect="light" :open-delay="1000">
                        <el-button v-if="canViewData(scope.row)" v-on:click.stop="developArchiveShow(scope.row)" type="primary" plain size="small">
                            数据
                        </el-button>
                      </el-tooltip>
                      <el-tooltip content="项目开发完成后, 请及时归档." placement="top" effect="light" :open-delay="1000">
                        <el-button v-if="scope.row.isCompleted && canArchiveProject(scope.row)" v-on:click.stop="archiveProject(scope.row)" type="primary" plain size="small">
                            归档
                        </el-button>
                      </el-tooltip>
                      <el-tooltip content="仅已完成的主项目可发布为产品台账" placement="top" effect="light" :open-delay="1000">
                        <el-button v-if="canPublishProduct(scope.row)" v-on:click.stop="publishProductFromProject(scope.row)" type="primary" plain size="small">
                            发布产品版本
                        </el-button>
                      </el-tooltip>
                      <el-tooltip content="全部子项目完成后, 对主项目发起项目评审" placement="top" effect="light" :open-delay="1000">
                        <el-button v-if="canLaunchProjectReview(scope.row)" v-on:click.stop="launchProjectReview(scope.row)" type="primary" plain size="small">
                            项目评审
                        </el-button>
                      </el-tooltip>
                      <el-tooltip content="对项目任务所需物料进行管理" placement="top" effect="light" :open-delay="1000">
                        <el-button v-if="canManageMaterialOrFee(scope.row)" type="primary" plain size="small" v-on:click.stop="subprojectTaskMaterialManage(scope.row)">
                          <el-badge :is-dot="scope.row.materialAppNum >0" >
                            物料
                          </el-badge>
                        </el-button>
                      </el-tooltip>
                      <el-tooltip content="对项目任务处理过程中其他相关费用进行管理" placement="top" effect="light" :open-delay="1000">
                        <el-button v-if="canManageMaterialOrFee(scope.row)" type="primary" plain size="small" v-on:click.stop="subprojectFeeManage(scope.row)">
                          <el-badge :is-dot="scope.row.feeAppNum >0" >
                            费用
                          </el-badge>
                        </el-button>
                      </el-tooltip>
                      <el-tooltip content="对BOM进行结构化编辑" placement="top" effect="light" :open-delay="1000">
                        <el-button type="primary" plain size="small" v-on:click.stop="bomStructureManage(scope.row)">
                            BOM
                        </el-button>
                      </el-tooltip>
                    </template>
                  </el-table-column>
                </el-table>
                <div class="project-pagination-wrap">
                  <el-pagination
                      background
                      layout="total, prev, pager, next, sizes"
                      :total="filteredMixProjects.length"
                      :page-size="tablePageSize"
                      :current-page.sync="tablePage"
                      :page-sizes="[10, 20, 50, 100]"
                      @size-change="onTablePageSizeChange"
                  />
                </div>
              </div>
            
            
                            </template></DynamicTabPane>

                        <DynamicTabPane
                page-id="archive"
                :active-page-id="activePageId">
                            <template v-slot:default>
              <div style="display: flex; flex-direction: row; justify-content: space-between; align-items: center;">
                <div>
                  <p>项目档案: 项目档案是给<strong>项目经理</strong>提供的一个对已经完工的项目进行档案查看和管理的页面.</p>
                </div>
                <div>
                  <el-input
                      class="project-search-input"
                      v-model="searchName"
                      size="mini"
                      placeholder="页面内容搜索-输入项目名称"/>
                </div>
              </div>
              <div class="hr hr-18 dotted hr-double"></div>
              <div>
                <el-table
                    v-loading="tableLoading"
                    :data="pagedMixProjects"
                    border
                    stripe
                    highlight-current-row
                    style="width: 100%"
                    @row-click="rowClick">
                  <el-table-column
                      prop="projectCode"
                      width="150"
                      show-overflow-tooltip
                      align="center"
                      label="项目代号"
                  ></el-table-column>
                  <el-table-column
                      prop="label"
                      align="center"
                      min-width="200"
                      show-overflow-tooltip
                      label="项目名称"
                  ></el-table-column>
                  <el-table-column
                      prop="productManagerName"
                      align="center"
                      label="产品经理"
                      width="100"
                  ></el-table-column>
                  <el-table-column
                      prop="projectManagerName"
                      align="center"
                      label="项目经理"
                      width="100"
                  ></el-table-column>
                  <el-table-column
                      prop="testManagerName"
                      align="center"
                      label="测试主管"
                      min-width="100"
                  ></el-table-column>
                  <el-table-column
                      prop="systemManagerName"
                      align="center"
                      label="系统工程师"
                      min-width="100"
                  ></el-table-column>
                  <el-table-column
                      prop="superviseName"
                      label="监管委员"
                      align="center"
                      width="100"
                  ></el-table-column>
                  <el-table-column
                      prop="rdType"
                      align="center"
                      :formatter="rdTypeFormat"
                      label="研发类型"
                      width="100"
                  ></el-table-column>
                  <el-table-column
                      prop="createTime"
                      :formatter="creatTimeFormat"
                      label="创建时间"
                      align="center"
                      width="110"
                  ></el-table-column>
                  <el-table-column
                      prop="releaseTime"
                      :formatter="releaseTimeFormat"
                      label="发布时间"
                      align="center"
                      width="110"
                  ></el-table-column>
                  <el-table-column
                      prop="status"
                      :formatter="statusFormat"
                      label="状态"
                      align="center"
                      width="110"
                  ></el-table-column>
                  <el-table-column
                      fixed="right"
                      label="操作"
                      align="center"
                      width="220">
                    <template slot-scope="scope">
                        <el-button v-on:click.stop="subprojectManagement(scope.row)" type="primary" plain size="small">
                            项目结构
                        </el-button>
                        <el-button v-on:click.stop="projectArchiveManagement(scope.row)" type="primary" plain size="small">
                            项目档案
                        </el-button>
                    </template>
                  </el-table-column>
                </el-table>
                <div class="project-pagination-wrap">
                  <el-pagination
                      background
                      layout="total, prev, pager, next, sizes"
                      :total="filteredMixProjects.length"
                      :page-size="tablePageSize"
                      :current-page.sync="tablePage"
                      :page-sizes="[10, 20, 50, 100]"
                      @size-change="onTablePageSizeChange"
                  />
                </div>
              </div>
            
            
                            </template></DynamicTabPane>

                        <DynamicTabPane
                page-id="project-data"
                :active-page-id="activePageId">
                            <template v-slot:default>
              <div style="display: flex; flex-direction: row; justify-content: space-between; align-items: center;">
                <div>
                  <p>项目执行数据: 项目执行数据是给<strong>产品经理</strong>提供的一个对执行中的项目进行数据交付监管的页面.</p>
                </div>
                <div>
                  <el-input
                      class="project-search-input"
                      v-model="searchName"
                      size="mini"
                      placeholder="页面内容搜索-输入项目名称"/>
                </div>
              </div>
              <div class="hr hr-18 dotted hr-double"></div>
              <div>
                <el-table
                    v-loading="tableLoading"
                    :data="filteredMixProjectDatas"
                    border
                    stripe
                    highlight-current-row
                    style="width: 100%">
                  <el-table-column
                      prop="projectCode"
                      width="150"
                      show-overflow-tooltip
                      align="center"
                      label="项目代号"
                      sortable
                  ></el-table-column>
                  <el-table-column
                      prop="label"
                      align="center"
                      min-width="200"
                      show-overflow-tooltip
                      label="项目名称"
                  ></el-table-column>
                  <el-table-column
                      prop="productManagerName"
                      align="center"
                      label="产品经理"
                      width="100"
                      sortable
                  ></el-table-column>
                  <el-table-column
                      prop="projectManagerName"
                      align="center"
                      label="项目经理"
                      width="100"
                      sortable
                  ></el-table-column>
                  <el-table-column
                      prop="testManagerName"
                      align="center"
                      label="测试主管"
                      min-width="100"
                      sortable
                  ></el-table-column>
                  <el-table-column
                      prop="systemManagerName"
                      align="center"
                      label="系统工程师"
                      min-width="100"
                      sortable
                  ></el-table-column>
                  <el-table-column
                      prop="superviseName"
                      label="监管委员"
                      align="center"
                      width="100"
                      sortable
                  ></el-table-column>
                  <el-table-column
                      prop="rdType"
                      align="center"
                      :formatter="rdTypeFormat"
                      label="研发类型"
                      width="100"
                      sortable
                  ></el-table-column>
                  <el-table-column
                      prop="createTime"
                      :formatter="creatTimeFormat"
                      label="创建时间"
                      align="center"
                      width="110"
                      sortable
                  ></el-table-column>
                  <el-table-column
                      prop="releaseTime"
                      sortable
                      :formatter="releaseTimeFormat"
                      label="发布时间"
                      align="center"
                      width="110"
                  ></el-table-column>
                  <el-table-column label="完工率" align="center" width="76">
                    <template slot-scope="scope">{{ statFmt.percent(statOf(scope.row).completeRate, 0) }}</template>
                  </el-table-column>
                  <el-table-column label="逾期数" align="center" width="70">
                    <template slot-scope="scope">
                      <span :class="{'project-stat-danger': statOf(scope.row).overdueNum > 0}">{{ statFmt.count(statOf(scope.row).overdueNum) }}</span>
                    </template>
                  </el-table-column>
                  <el-table-column label="预算执行率" align="center" width="90">
                    <template slot-scope="scope">{{ statFmt.percent(statOf(scope.row).budgetExeRate, 0) }}</template>
                  </el-table-column>
                  <el-table-column label="健康度" align="center" width="76">
                    <template slot-scope="scope">
                      <el-tooltip v-if="statOf(scope.row).health" :disabled="!(statOf(scope.row).healthReasons || []).length"
                                  :content="(statOf(scope.row).healthReasons || []).join('；')" placement="top">
                        <el-tag size="mini" :type="statFmt.healthType(statOf(scope.row).health)">{{ statFmt.healthName(statOf(scope.row).health) }}</el-tag>
                      </el-tooltip>
                      <span v-else>-</span>
                    </template>
                  </el-table-column>
                  <el-table-column
                      prop="status"
                      :formatter="statusFormat"
                      label="状态"
                      align="center"
                      width="110"
                      sortable
                  ></el-table-column>
                  <el-table-column
                      fixed="right"
                      label="操作"
                      align="center"
                      width="220">
                    <template slot-scope="scope">
                        <el-button v-on:click.stop="projectArchiveManagement(scope.row)" type="primary" plain size="small">
                            项目档案
                        </el-button>
                        <el-button v-on:click.stop="projectTaskFeeList(scope.row)" type="primary" plain size="small">
                            任务清单
                        </el-button>
                    </template>
                  </el-table-column>
                </el-table>
              </div>
            
            
                            </template></DynamicTabPane>

            <!-- 任务管理-->
            <DynamicTabPane
                v-for="(item, index) in projectTaskItems"
                :key="('task-' + (item.id))"
                :index="index"
                prefix="task-manage"
                :active-page-id="activePageId">
                <template v-slot:default>
              <div>
                <SubprojectTaskTab
                    v-if="activePageId === ('task-manage-' + (index))"
                    :key="item.id "
                    :sub-project-id="item.id"
                    :job-type="JOB_ITEM_TYPE.TASK_SUBP.key"
                    :project-type="PROJECT_TYPE.SUB_PROJECT.key"
                    :index="index"
                    :entrance="ENTRANCE_CLASS.PROJECT.key"
                ></SubprojectTaskTab>
              </div>
            
                </template></DynamicTabPane>

            <!-- 执行数据-->
            <DynamicTabPane
                v-for="(item, index) in projectArchiveItems"
                :key="('archive-tree-' + (item.id))"
                :index="index"
                prefix="archive-tree"
                :active-page-id="activePageId">
                <template v-slot:default>
              <div>
                <ProjectArchiveContainer
                    v-if="activePageId === ('archive-tree-' + (index))"
                    :key="item.id"
                    :index="index"
                    :project-id="item.id"
                    :project-type="PROJECT_TYPE.PROJECT.key"
                    :entrance="ENTRANCE_CLASS.PROJECT.key">
                </ProjectArchiveContainer>
              </div>
            
                </template></DynamicTabPane>

            <!-- 特性审核-->
            <DynamicTabPane
                v-for="(item, index) in projectSystemItems"
                :key="item.id"
                :index="index"
                prefix="project-system"
                :active-page-id="activePageId">
                <template v-slot:default>
              <div>
                <ProjectSystemTab
                    v-if="activePageId === ('project-system-' + (index))"
                    :key="item.id"
                    :x-project-id="item.id"
                    :project-type="PROJECT_TYPE.DEV_PROJECT.key"
                    :index="index"
                    :entrance="ENTRANCE_CLASS.SYSTEM.key"
                ></ProjectSystemTab>
              </div>
            
                </template></DynamicTabPane>

            <!-- 项目管理-->
            <DynamicTabPane
                v-for="(item, index) in subprojectManageItems"
                :key="('subproject-' + (item.id))"
                :index="index"
                prefix="subproject-manage-item"
                :active-page-id="activePageId">
                <template v-slot:default>
              <project-subproject
                  v-if="activePageId === ('subproject-manage-item-' + (index))"
                  :key="item.id"
                  :sub-project-id="item.id"
                  :project-type="PROJECT_TYPE.DEV_PROJECT.key"
                  :index="index"
                  :entrance="ENTRANCE_CLASS.PROJECT.key"
                  :close-function="closeFunction_manage">
              </project-subproject>
            
                </template></DynamicTabPane>

            <!-- 项目档案-->
            <DynamicTabPane
                v-for="(item, index) in projectArchiveList"
                :key="('project-archive-item-' + (item.id))"
                :index="index"
                prefix="project-archive-item"
                :active-page-id="activePageId">
                <template v-slot:default>
              <div>
                <ProjectArchiveContainer
                    v-if="activePageId === ('project-archive-item-' + (index))"
                    :key="item.id"
                    :index="index"
                    :project-id="item.id"
                    :project-type="PROJECT_TYPE.PROJECT.key"
                    :entrance="ENTRANCE_CLASS.PRODUCT.key">
                </ProjectArchiveContainer>
              </div>
            
                </template></DynamicTabPane>

            <!-- 任务清单-->
            <DynamicTabPane
                v-for="(item, index) in projectTaskList"
                :key="('project-task-list-' + (item.id))"
                :index="index"
                prefix="project-task-list"
                :active-page-id="activePageId">
                <template v-slot:default>
              <div>
                <SubprojectTaskList
                    v-if="activePageId === ('project-task-list-' + (index))"
                    :key="item.id"
                    :index="index"
                    :subproject-id="item.id"
                    :project-type="PROJECT_TYPE.SUB_PROJECT.key"
                    :entrance="ENTRANCE_CLASS.PROJECT.key">
                </SubprojectTaskList>
              </div>
            
                </template></DynamicTabPane>

            <!-- 物料管理-->
            <DynamicTabPane
                v-for="(item, index) in subprojectMaterialList"
                :key="('material-manage-item-' + (item.id))"
                :index="index"
                prefix="material-manage-item"
                :active-page-id="activePageId">
                <template v-slot:default>
              <subprojectTaskMaterialList
                  v-if="activePageId === ('material-manage-item-' + (index))"
                  :key="item.id"
                  :subproject-id="item.id"
              ></subprojectTaskMaterialList>
            
                </template></DynamicTabPane>

            <!-- 费用管理-->
            <DynamicTabPane
                v-for="(item, index) in subprojectFeeList"
                :key="('fee-manage-item-' + (item.id))"
                :index="index"
                prefix="fee-manage-item"
                :active-page-id="activePageId">
                <template v-slot:default>
              <SubprojectTaskFeeList
                  v-if="activePageId === ('fee-manage-item-' + (index))"
                  :key="item.id"
                  :subproject-id="item.id"
              ></SubprojectTaskFeeList>
            
                </template></DynamicTabPane>

            <!-- BOM结构化-->
            <DynamicTabPane
                v-for="(item, index) in subprojectBomStructure"
                :key="('bom-structure-item-' + (item.id))"
                :index="index"
                prefix="bom-structure-item"
                :active-page-id="activePageId">
                <template v-slot:default>
              <SubprojectBomStructure
                  v-if="activePageId === ('bom-structure-item-' + (index))"
                  :key="item.id"
                  :subproject-id="item.id"
                  :is-main-project="item.isMainProject"
              ></SubprojectBomStructure>
            
                </template></DynamicTabPane>

            <!-- Gantt：仅挂载当前激活 Tab，避免 dhtmlx 单例多实例串扰 -->
            <DynamicTabPane
                v-for="(item, index) in subprojectGanttList"
                :key="('subproject-gantt-item-' + (item.id))"
                :index="index"
                prefix="subproject-gantt-item"
                :active-page-id="activePageId">
                <template v-slot:default>
              <div v-if="activePageId === ('subproject-gantt-item-' + (index))">
                <Gantt
                    v-if="activePageId === ('subproject-gantt-item-' + (index))"
                    :key="item.id"
                    :subproject-id="item.id"
                    :date-type="'day'"
                ></Gantt>
              </div>
            
                </template></DynamicTabPane>

          
    </WorkspaceFrame>

    <el-dialog
        :visible.sync="projectReviewDlgVisible"
        width="29cm"
        :close-on-click-modal=false>
      <div slot="title" style="font-size: 20px;">项目评审</div>
      <review-create-issue-dlg
          v-if="projectReviewDlgVisible"
          :x-id="projectReviewId"
          :doc-type="DOC_TYPE.PROJECT.key"
          :project-type="PROJECT_TYPE.PROJECT.key"
          :entrance="ENTRANCE_CLASS.PROJECT.key"
          :close-dlg="closeProjectReviewDlg">
      </review-create-issue-dlg>
    </el-dialog>
    <el-dialog
        :visible.sync="publishDlgVisible"
        width="20cm"
        destroy-on-close
        :close-on-click-modal=false>
      <div slot="title" style="font-size: 20px;">发布产品版本</div>
      <ProductPublishDlg
          v-if="publishDlgVisible"
          :key="publishProject.id"
          :project="publishProject"
          :close-function="closePublishDlg">
      </ProductPublishDlg>
    </el-dialog>
  </div>
</template>

<script>
import {closeDynamicTab, openOrFocusTab} from "@/shared/dynamic-tab";
import DynamicTabItem from "@/components/rdms/dynamic-tab-item";
import DynamicTabPane from "@/components/rdms/dynamic-tab-pane";
import WorkspaceFrame from "@/components/rdms/workspace-frame";
import ProjectEdit from "@/components/rdms/project-edit";
import projectMilestone from "@/components/rdms/project-milestone";
import ProjectSubproject from "@/components/rdms/project-subproject";

import File from "@/components/rdms/file";
import Swal from "sweetalert2";
import {optionKV, rmbFormat, shortDateFormat} from "@/filter/filter";
import ProjectSystemTab from "@/components/rdms/project-system-tab.vue";
import SubprojectTaskTab from "@/components/rdms/subproject-task-tab.vue";
import subprojectTaskMaterialList from "@/components/rdms/subproject-task-material-list.vue";
import SubprojectTaskFeeList from "@/components/rdms/subproject-task-fee-list.vue";
import * as statFmt from "@/shared/stat-format";
import { listProjectCardsByIds } from "@/shared/statistics-api";
import ProjectArchiveContainer from "@/components/rdms/project-archive-container.vue";
import SubprojectBomStructure from "@/components/rdms/subproject-bom-structure.vue";
import SubprojectTaskList from "@/components/rdms/subproject-task-list.vue";
import ReviewCreateIssueDlg from "@/components/std/review-create-issue-dlg.vue";
import ProductPublishDlg from "@/components/rdms/product-publish-dlg.vue";

export default {
  components: {
    ProductPublishDlg,
    ReviewCreateIssueDlg,
    ProjectArchiveContainer,
    SubprojectTaskFeeList,
    subprojectTaskMaterialList,
    SubprojectTaskTab,
    ProjectSystemTab,
    File,
    DynamicTabItem,
    DynamicTabPane,
    WorkspaceFrame,
    ProjectEdit,
    projectMilestone,
    ProjectSubproject,
    SubprojectBomStructure,
    Gantt: () => import(/* webpackChunkName: "gantt" */ "@/components/std/gantt.vue"),
    SubprojectTaskList,
  },
  name: "project",
  data: function () {
    return {
      publishDlgVisible: false,
      publishProject: {},
      loginUser: {},
      loginCustomer: {},

      PROJECT_TYPE: PROJECT_TYPE,
      ENTRANCE_CLASS: ENTRANCE_CLASS,
      JOB_ITEM_TYPE: JOB_ITEM_TYPE,
      RD_TYPE: RD_TYPE,
      PROJECT_STATUS: PROJECT_STATUS,
      DOC_TYPE: DOC_TYPE,
      projectReviewDlgVisible: false,
      projectReviewId: '',

      searchName: '',
      activePageId: 'home',
      preActivePageId: 'home',
      mixProject: {},
      mixProjects: [],
      tableLoading: false,
      tablePage: 1,
      tablePageSize: 20,

      projectTaskItems: [],
      projectArchiveItems: [],
      projectSystemItems: [],
      subprojectManageItems: [],
      projectArchiveList: [],
      projectTaskList: [],

      subprojectMaterialList: [],
      subprojectFeeList: [],
      subprojectBomStructure: [],
      subprojectGanttList: [],
      mixProjectDatas: [],
      statFmt: statFmt,
      projectStats: {},
      projectStatSeq: 0,

    }
  },
  watch: {
    searchName() {
      this.tablePage = 1;
    },
    pagedMixProjects(rows) {
      if (this.activePageId === 'home') {
        this.loadProjectStats(rows);
      }
    },
    filteredMixProjectDatas(rows) {
      if (this.activePageId === 'project-data') {
        this.loadProjectStats(rows);
      }
    },
  },
  mounted: function () {
    let _this = this;
    _this.getLoginUser();
    _this.getSubProjectForPjm();

    _this.$bus.$on('close-project-develop-tab', (index) => {
      _this.closeFunction_develop(index);
    });
    _this.unsubscribeProject = _this.$bus.subscribe(['project'], function () {
      _this.getSubProjectForPjm();
    });
  },

  beforeDestroy: function () {
    let _this = this;
    _this.$bus.$off('close-project-develop-tab');
    if (_this.unsubscribeProject) {
      _this.unsubscribeProject();
    }
  },

  computed: {
    filteredMixProjects() {
      const keyword = (this.searchName || '').toLowerCase();
      const list = this.mixProjects || [];
      if (!keyword) {
        return list;
      }
      return list.filter(data => (data.label || '').toLowerCase().includes(keyword));
    },
    pagedMixProjects() {
      const start = (this.tablePage - 1) * this.tablePageSize;
      return this.filteredMixProjects.slice(start, start + this.tablePageSize);
    },
    filteredMixProjectDatas() {
      const keyword = (this.searchName || '').toLowerCase();
      const list = this.mixProjectDatas || [];
      if (!keyword) {
        return list;
      }
      return list.filter(data => (data.label || '').toLowerCase().includes(keyword));
    },
    getOpeAreaWidth(){
      return 700;
    }
  },

  methods: {
    canManageProject(row) {
      return !(row.isReviewing || row.isCompleted || row.isDevCompleted);
    },
    canManageTask(row) {
      return this.canManageProject(row) && row.rdType !== RD_TYPE.MODULE.key;
    },
    canManageCharacter(row) {
      return this.canManageProject(row) && row.status !== PROJECT_STATUS.OUT_SOURCE.key;
    },
    canViewData(row) {
      return row.status !== PROJECT_STATUS.OUT_SOURCE.key;
    },
    // 开发完成仅保留评审相关入口；物料/费用与项目管理一致隐藏
    canManageMaterialOrFee(row) {
      return !(row.isReviewing || row.isCompleted || row.isDevCompleted || row.status === PROJECT_STATUS.OUT_SOURCE.key);
    },
    isMainProjectRow(subproject) {
      return !!(subproject && subproject.id
          && (subproject.id === subproject.projectId || subproject.id === subproject.parent));
    },
    canLaunchProjectReview(row) {
      return this.isMainProjectRow(row)
          && !(row.isReviewing || row.isCompleted)
          && (row.status === PROJECT_STATUS.SETUPED.key || row.status === PROJECT_STATUS.ONGOING.key);
    },
    launchProjectReview(row) {
      this.projectReviewId = row.projectId || row.id;
      this.projectReviewDlgVisible = true;
    },
    closeProjectReviewDlg() {
      this.projectReviewDlgVisible = false;
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

    rdTypeFormat(row, column) {
      return optionKV(RD_TYPE, row.rdType);
    },

    creatTimeFormat(row, column){
      return shortDateFormat(row.createTime);
    },

    releaseTimeFormat(row, column){
      if(row.releaseTime === null){
        return "--"
      }
      return shortDateFormat(row.releaseTime);
    },

    statusFormat(row, column) {
      return optionKV(PROJECT_STATUS, row.status);
    },

    rowClick(row, column, e){
      let _this = this;
      // _this.subprojectBoardManage(row);
      _this.subprojectManagement(row);
    },

    developManagerClick(i) {
      let _this = this;
      _this.activePageId = `task-manage-${i}`;
    },

    developArchiveClick(i) {
      let _this = this;
      _this.activePageId = `archive-tree-${i}`;
    },

    systemManagerClick(i) {
      let _this = this;
      _this.activePageId = `project-system-${i}`;
    },

    subprojectManagerClick(i) {
      let _this = this;
      _this.activePageId = `subproject-manage-item-${i}`;
    },

    tabProjectArchiveClick(i){
      let _this = this;
      _this.activePageId = `project-archive-item-${i}`;
    },

    tabProjectTaskClick(i){
      let _this = this;
      _this.activePageId = `project-task-list-${i}`;
    },

    tabMaterialItemClick(i) {
      let _this = this;
      _this.activePageId = `material-manage-item-${i}`;
    },

    tabFeeItemClick(i) {
      let _this = this;
      _this.activePageId = `fee-manage-item-${i}`;
    },

    tabBomStructureClick(i) {
      let _this = this;
      _this.activePageId = `bom-structure-item-${i}`;
    },

    tabGanttClick(i) {
      let _this = this;
      _this.activePageId = `subproject-gantt-item-${i}`;
    },

    tabHomeClick() {
      let _this = this;
      _this.activePageId = 'home';
      _this.preActivePageId = 'home';
      _this.getSubProjectForPjm();
    },

    tabArchiveClick() {
      let _this = this;
      _this.activePageId = 'archive';
      _this.preActivePageId = 'archive';
      _this.getArchiveSubProjectForPjm();
    },

    tabProjectDataClick() {
      let _this = this;
      _this.activePageId = 'project-data';
      _this.preActivePageId = 'project-data';
      _this.getSubprojectExeDataByPdm();
    },

    archiveProject(subproject){
      let _this = this;
      Swal.fire({
        title: '确认',
        text: "系统将对项目进行归档处理",
        icon: 'question',
        showCancelButton: true,
        confirmButtonColor: '#409EFF',
        cancelButtonColor: '#909399',
        confirmButtonText: '确认!',
        cancelButtonText: '取消!'
      }).then((result) => {
        if (result.isConfirmed) {
          _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/sub-project/archive/' + subproject.id).then((response) => {
            let resp = response.data;
            if (resp.success) {
              _this.getSubProjectForPjm();
              Toast.success("归档成功!", Swal);
            } else {
              Toast.warning(resp.message, Swal);
            }
          });
        }
      }).catch(_ => {
      });
    },

    hasFullAuthority() {
      return typeof RoleAccess !== 'undefined'
        && RoleAccess.hasAny(['UltiMng'], Tool.getLoginUser().roleList);
    },

    /** 归档限产品经理或监管委员，最终管理人可以代办。 */
    canArchiveProject(row) {
      const uid = this.loginUser && this.loginUser.customerUserId;
      return !!row && (this.hasFullAuthority()
        || (!!uid && (row.productManagerId === uid || row.supervise === uid)));
    },

    /** 主项目根节点且状态为 COMPLETE 时可发布，限产品经理。关闭与归档不显示。 */
    canPublishProduct(row) {
      if (!row || row.status !== PROJECT_STATUS.COMPLETE.key) {
        return false;
      }
      const uid = this.loginUser && this.loginUser.customerUserId;
      const isPdm = this.hasFullAuthority() || (!!uid && row.productManagerId === uid);
      return isPdm && (row.id === row.projectId || row.id === row.parent);
    },

    publishProductFromProject(row) {
      this.publishProject = { id: row.projectId || row.id, projectName: row.label || row.projectName || row.itemName };
      this.publishDlgVisible = true;
    },

    closePublishDlg(done) {
      this.publishDlgVisible = false;
    },

    developManagement(mixProject) {
      openOrFocusTab(this, this.projectTaskItems, mixProject, 'task-manage');
    },
    closeFunction_develop(index) {
      closeDynamicTab(this, {
        list: this.projectTaskItems,
        index: index,
        prefix: 'task-manage',
        fallback: () => this.preActivePageId,
      });
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

    characterManagement(mixProject) {
      openOrFocusTab(this, this.projectSystemItems, mixProject, 'project-system');
    },
    closeFunction_system(index) {
      closeDynamicTab(this, {
        list: this.projectSystemItems,
        index: index,
        prefix: 'project-system',
        fallback: () => this.preActivePageId,
      });
    },

    subprojectManagement(mixProject) {
      openOrFocusTab(this, this.subprojectManageItems, mixProject, 'subproject-manage-item', { refresh: true });
    },
    closeFunction_manage(index) {
      closeDynamicTab(this, {
        list: this.subprojectManageItems,
        index: index,
        prefix: 'subproject-manage-item',
        fallback: () => this.preActivePageId,
      });
    },

    projectArchiveManagement(project){
      openOrFocusTab(this, this.projectArchiveList, project, 'project-archive-item');
    },
    closeFunction_archive(index) {
      closeDynamicTab(this, {
        list: this.projectArchiveList,
        index: index,
        prefix: 'project-archive-item',
        fallback: () => this.preActivePageId,
      });
    },

    projectTaskFeeList(project){
      openOrFocusTab(this, this.projectTaskList, project, 'project-task-list');
    },
    closeFunction_taskList(index) {
      closeDynamicTab(this, {
        list: this.projectTaskList,
        index: index,
        prefix: 'project-task-list',
        fallback: () => this.preActivePageId,
      });
    },

    subprojectTaskMaterialManage(subproject) {
      openOrFocusTab(this, this.subprojectMaterialList, subproject, 'material-manage-item');
    },
    closeFunction_material(index) {
      closeDynamicTab(this, {
        list: this.subprojectMaterialList,
        index: index,
        prefix: 'material-manage-item',
        fallback: () => this.preActivePageId,
      });
    },

    subprojectFeeManage(subproject) {
      openOrFocusTab(this, this.subprojectFeeList, subproject, 'fee-manage-item');
    },
    closeFunction_fee(index) {
      closeDynamicTab(this, {
        list: this.subprojectFeeList,
        index: index,
        prefix: 'fee-manage-item',
        fallback: () => this.preActivePageId,
      });
    },

    bomStructureManage(subproject) {
      openOrFocusTab(this, this.subprojectBomStructure, subproject, 'bom-structure-item', { markMain: true });
    },
    closeBomStructureManage(index) {
      closeDynamicTab(this, {
        list: this.subprojectBomStructure,
        index: index,
        prefix: 'bom-structure-item',
        fallback: () => this.preActivePageId,
      });
    },

    ganttManage(subproject) {
      openOrFocusTab(this, this.subprojectGanttList, subproject, 'subproject-gantt-item', { markMain: true });
    },
    closeGantt(index) {
      closeDynamicTab(this, {
        list: this.subprojectGanttList,
        index: index,
        prefix: 'subproject-gantt-item',
        fallback: () => this.preActivePageId,
      });
    },

    onTablePageSizeChange(size) {
      this.tablePageSize = size;
      this.tablePage = 1;
    },

    statOf(row) {
      return (row && this.projectStats[row.id]) || {};
    },

    // 列表行可为主项目或子项目，统计指标按当前页编号批量取；范围外的行不返回，显示“-”
    loadProjectStats(rows) {
      const ids = (rows || []).map(r => r.id).filter(Boolean);
      if (!ids.length) {
        return;
      }
      const seq = ++this.projectStatSeq;
      listProjectCardsByIds(ids).then(cards => {
        if (seq !== this.projectStatSeq) {
          return;
        }
        const next = Object.assign({}, this.projectStats);
        (cards || []).forEach(c => {
          next[c.id] = c;
        });
        this.projectStats = next;
      }).catch(err => {
        console.warn('项目统计指标加载失败', err && err.message);
      });
    },

    getSubProjectForPjm() {
      let _this = this;
      _this.tableLoading = true;
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/sub-project/getSubProjectForPjm', {
        customerId: _this.loginCustomer.id,
        projectManagerId: _this.loginUser.customerUserId,
      }).then((response) => {
        let resp = response.data;
        if (resp.success) {
          _this.mixProjects = resp.content || [];
          _this.tablePage = 1;
        } else {
          Toast.warning(resp.message, Swal);
        }
      }).finally(() => {
        _this.tableLoading = false;
      })
    },

    getArchiveSubProjectForPjm() {
      let _this = this;
      _this.tableLoading = true;
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/sub-project/getArchiveSubProjectForPjm', {
        customerId: _this.loginCustomer.id,
        projectManagerId: _this.loginUser.customerUserId,
      }).then((response) => {
        let resp = response.data;
        if (resp.success) {
          _this.mixProjects = resp.content || [];
          _this.tablePage = 1;
        } else {
          Toast.warning(resp.message, Swal);
        }
      }).finally(() => {
        _this.tableLoading = false;
      })
    },

    getSubprojectExeDataByPdm() {
      let _this = this;
      _this.tableLoading = true;
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/sub-project/getSubprojectExeData', {
        customerId: _this.loginCustomer.id,
        productManagerId: _this.loginUser.customerUserId,
      }).then((response) => {
        let resp = response.data;
        if (resp.success) {
          _this.mixProjectDatas = resp.content;
        } else {
          Toast.warning(resp.message, Swal);
        }
      }).finally(() => {
        _this.tableLoading = false;
      })
    },

    tableRowClass({row}) {
      if (!row || !row.releaseTime) {
        return '';
      }
      const releaseMs = Date.parse(row.releaseTime);
      if (Number.isNaN(releaseMs)) {
        return '';
      }
      if (Date.now() > releaseMs
          && row.status !== PROJECT_STATUS.ARCHIVED.key
          && row.status !== PROJECT_STATUS.COMPLETE.key) {
        return 'warning-row-3';
      }
      return '';
    }
  }
}
</script>

<style>
/* 仅作用于本页搜索框，避免影响 el-pagination 的「条/页」下拉宽度导致溢出 */
.project-search-input.el-input--mini .el-input__inner {
  height: 28px;
  line-height: 28px;
  text-align: center !important;
  width: 222px !important;
}

.project-pagination-wrap {
  margin-top: 12px;
  width: 100%;
  box-sizing: border-box;
  display: flex;
  justify-content: flex-end;
  flex-wrap: wrap;
  overflow: hidden;
}

.project-stat-danger {
  color: #F56C6C;
}
</style>
