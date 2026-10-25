<!--
 * Author: 周朔鹏
 * Date: 2025-02-23
 * Email: 391902958@qq.com
 * Website: https://www.euipd.com
-->

<template>
  <div style="height: 100%;">
    <div class="paper-height">
      <div style="height: 100%; overflow: scroll;">
        <div :class="[ isPageShow ? simplePage : plantPage]">
          <div :class="[ isPageShow ? pageMargin : '']">
            <div>
              <div v-if="showDefined">
                <div class="ca-form">
                  <div class="ca-form-header">
                    <div class="ca-form-header-line start" style="margin-left: 12px;">
                      <div class="ca-form-header-line-label" style="line-height: 30px; font-size: 15px; font-weight: 500">
                        <i class="el-icon-document"></i> 功能特性名称:
                      </div>
                      <div class="ca-form-header-line-content">
                        {{ characterDocPageInfo.characterName }}
                      </div>
                    </div>
                  </div>
                  <div class="ca-form-content">
                    <div class="ca-form-content-line">
                      <div class="ca-form-content-line-label md right">
                        编制人:
                      </div>
                      <div class="ca-form-content-line-content">
                        {{ characterDocPageInfo.writerName }}
                      </div>
                    </div>
                  </div>
                  <div class="ca-form-content">
                    <div class="ca-form-content-line">
                      <div class="ca-form-content-line-label md right">
                        产品经理:
                      </div>
                      <div class="ca-form-content-line-content">
                        {{ characterDocPageInfo.productManagerName }}
                      </div>
                    </div>
                  </div>
                  <div class="ca-form-content">
                    <div class="ca-form-content-line">
                      <div class="ca-form-content-line-label md right">
                        项目经理:
                      </div>
                      <div class="ca-form-content-line-content">
                        {{ characterDocPageInfo.projectManagerName }}
                      </div>
                    </div>
                  </div>
                  <div class="hr hr-10 hr-dotted"></div>
                  <div class="ca-form-content">
                    <div class="ca-form-content-line">
                      <div class="ca-form-content-line-label md right">
                        特性描述:
                      </div>
                      <div class="ca-form-content-line-content" :class="[ isPageShow ? width760 : width850]">
                        <div v-rich-html="characterDocPageInfo.functionDescription"></div>
                      </div>
                    </div>
                  </div>
                  <div class="ca-form-content">
                    <div class="ca-form-content-line">
                      <div class="ca-form-content-line-label md right">
                        生效条件:
                      </div>
                      <div class="ca-form-content-line-content" :class="[ isPageShow ? width760 : width850]">
                        <div v-rich-html="characterDocPageInfo.workCondition"></div>
                      </div>
                    </div>
                  </div>
                  <div class="ca-form-content">
                    <div class="ca-form-content-line">
                      <div class="ca-form-content-line-label md right">
                        输入逻辑:
                      </div>
                      <div class="ca-form-content-line-content" :class="[ isPageShow ? width760 : width850]">
                        <div v-rich-html="characterDocPageInfo.inputLogicalDesc"></div>
                      </div>
                    </div>
                  </div>
                  <div class="ca-form-content">
                    <div class="ca-form-content-line">
                      <div class="ca-form-content-line-label md right">
                        处理逻辑:
                      </div>
                      <div class="ca-form-content-line-content" :class="[ isPageShow ? width760 : width850]">
                        <div v-rich-html="characterDocPageInfo.functionLogicalDesc"></div>
                      </div>
                    </div>
                  </div>
                  <div class="ca-form-content">
                    <div class="ca-form-content-line">
                      <div class="ca-form-content-line-label md right">
                        输出逻辑:
                      </div>
                      <div class="ca-form-content-line-content" :class="[ isPageShow ? width760 : width850]">
                        <div v-rich-html="characterDocPageInfo.outputLogicalDesc"></div>
                      </div>
                    </div>
                  </div>
                  <div class="ca-form-content">
                    <div class="ca-form-content-line">
                      <div class="ca-form-content-line-label md right">
                        测试方法:
                      </div>
                      <div class="ca-form-content-line-content" :class="[ isPageShow ? width760 : width850]">
                        <div v-rich-html="characterDocPageInfo.testMethod"></div>
                      </div>
                    </div>
                  </div>
                </div>
                <div v-if="characterDocPageInfo.characterFileList">
                  <div style="padding-left: 15px; font-weight: bold;">
                    <i class="el-icon-paperclip"></i> <span>特性定义附件 : </span>
                  </div>
                  <div v-for="(file, i) in characterDocPageInfo.characterFileList" :key="('character-file-' + (i))" style="padding-left: 106px;">
                    <div v-if="file.authStatus" style="line-height: 28px;">
                      <a href="javascript:void(0)" @click="downloadFile(file)">  {{ file.name }} </a>
                    </div>
                    <div v-else style="line-height: 28px;">
                      {{ file.name }}
                    </div>
                  </div>
                </div>
              </div> <!--功能信息-->

              <div v-if="characterDocPageInfo.allDevJobList">
                <div class="hr hr-15 hr-dotted hr-double"></div>
                <div style="line-height: 30px; font-size: 15px; font-weight: 500">
                  <i class="el-icon-document"></i> 开发任务执行信息
                </div>
                <div> <!--所有任务-->
                  <div v-for="(jobitem, index) in characterDocPageInfo.allDevJobList" :key="index"> <!--任务循环-->
                    <div class="ca-form"> <!--一条任务的信息-->
                      <div class="ca-form-header">
                        <div class="ca-form-header-line start" style="margin-left: 12px; font-weight: 500">
                          <div class="ca-form-header-line-label" style="margin-left: 26px;">
                            任务名称:
                          </div>
                          <div class="ca-form-header-line-content">
                            {{ jobitem.jobName }}
                          </div>
                        </div>
                      </div>
                      <div class="ca-form-content">
                        <div class="ca-form-content-line">
                          <div class="ca-form-content-line-content">
                            <div style="display: inline-block; margin-left: 52px;">执行人: {{ jobitem.executorName }}</div>
                            <div style="display: inline-block; margin-left: 30px;">任务类型: {{ JOB_ITEM_TYPE | optionKV(jobitem.type)}}</div>
                            <div style="display: inline-block; margin-left: 30px;">工时数量: {{ jobitem.manhour }}</div>
                            <div style="display: inline-block; margin-left: 30px;">质量评分: {{ jobitem.evaluateScore }}</div>
                          </div>
                        </div>
                      </div>
                      <div class="ca-form-content">
                        <div class="ca-form-content-line">
                          <div class="ca-form-content-line-label md right">
                            作业评价:
                          </div>
                          <div class="ca-form-content-line-content">
                            <div v-rich-html="jobitem.evaluateDesc"></div>
                          </div>
                        </div>
                      </div>
                      <div class="ca-form-content">
                        <div class="ca-form-content-line">
                          <div class="ca-form-content-line-label md right">
                            任务描述:
                          </div>
                          <div class="ca-form-content-line-content" :class="[ isPageShow ? width760 : width850]">
                            <div v-rich-html="jobitem.taskDescription"></div>
                          </div>
                        </div>
                      </div>
                      <div  v-if="jobitem.taskFileList" class="ca-form-content">
                        <div class="ca-form-content-line">
                          <div class="ca-form-content-line-label md right">
                            <i class="el-icon-paperclip"></i> 任务附件:
                          </div>
                          <div class="ca-form-content-line-content" :class="[ isPageShow ? width760 : width850]">
                            <div>
                              <div v-for="(file, i) in jobitem.taskFileList" :key="('dev-file-' + (i))">
                                <div v-if="file.authStatus" style="line-height: 28px;">
                                  <a href="javascript:void(0)" @click="downloadFile(file)">  {{ file.name }} </a>
                                </div>
                                <div v-else style="line-height: 28px;">
                                  {{ file.name }}
                                </div>
                              </div>
                            </div>
                          </div>
                        </div>
                      </div>
                      <div class="ca-form-content">
                        <div class="ca-form-content-line">
                          <div class="ca-form-content-line-label md right">
                            工作陈述:
                          </div>
                          <div class="ca-form-content-line-content" :class="[ isPageShow ? width760 : width850]">
                            <div v-rich-html="jobitem.submitDescription"></div>
                          </div>
                        </div>
                      </div>
                      <div  v-if="jobitem.submitFileList" class="ca-form-content">
                        <div class="ca-form-content-line">
                          <div class="ca-form-content-line-label md right">
                            <i class="el-icon-paperclip"></i> 交付附件:
                          </div>
                          <div class="ca-form-content-line-content" :class="[ isPageShow ? width760 : width850]">
                            <div>
                              <div v-for="(file, i) in jobitem.submitFileList" :key="('dev-submit-file-' + (i))">
                                <div v-if="file.authStatus" style="line-height: 28px;">
                                  <a href="javascript:void(0)" @click="downloadFile(file)">  {{ file.name }} </a>
                                </div>
                                <div v-else style="line-height: 28px;">
                                  {{ file.name }}
                                </div>
                              </div>
                            </div>
                          </div>
                        </div>
                      </div>
                    </div>
                    <div class="hr hr-10 hr-dotted"></div>
                  </div> <!--任务循环-->
                </div>
              </div> <!--开发任务-->
              <div v-if="characterDocPageInfo.allTestJobList  && characterDocPageInfo.allTestJobList.length>0">
                <div class="hr hr-10 hr-dotted hr-double"></div>
                <div style="line-height: 30px; font-size: 15px; font-weight: 500">
                  <i class="el-icon-document"></i> 测试任务执行信息
                </div>
                <div> <!--所有任务-->
                  <div v-for="(jobitem, index) in characterDocPageInfo.allTestJobList" :key="index"> <!--任务循环-->
                    <div class="ca-form"> <!--一条任务的信息-->
                      <div class="ca-form-header">
                        <div class="ca-form-header-line start" style="margin-left: 12px; font-weight: 500">
                          <div class="ca-form-header-line-label" style="margin-left: 26px;">
                            任务名称:
                          </div>
                          <div class="ca-form-header-line-content">
                            {{ jobitem.jobName }}
                          </div>
                        </div>
                      </div>
                      <div class="ca-form-content">
                        <div class="ca-form-content-line">
                          <div class="ca-form-content-line-content">
                            <div style="display: inline-block; margin-left: 52px;">执行人: {{ jobitem.executorName }}</div>
                            <div style="display: inline-block; margin-left: 30px;">任务类型: {{ JOB_ITEM_TYPE | optionKV(jobitem.type)}}</div>
                            <div style="display: inline-block; margin-left: 30px;">工时数量: {{ jobitem.manhour }}</div>
                            <div style="display: inline-block; margin-left: 30px;">质量评分: {{ jobitem.evaluateScore }}</div>
                          </div>
                        </div>
                      </div>
                      <div class="ca-form-content">
                        <div class="ca-form-content-line">
                          <div class="ca-form-content-line-label md right">
                            作业评价:
                          </div>
                          <div class="ca-form-content-line-content">
                            <div v-rich-html="jobitem.evaluateDesc"></div>
                          </div>
                        </div>
                      </div>
                      <div class="ca-form-content">
                        <div class="ca-form-content-line">
                          <div class="ca-form-content-line-label md right">
                            任务描述:
                          </div>
                          <div class="ca-form-content-line-content" :class="[ isPageShow ? width760 : width850]">
                            <div v-rich-html="jobitem.taskDescription"></div>
                          </div>
                        </div>
                      </div>
                      <div  v-if="jobitem.taskFileList" class="ca-form-content">
                        <div class="ca-form-content-line">
                          <div class="ca-form-content-line-label md right">
                            <i class="el-icon-paperclip"></i> 任务附件:
                          </div>
                          <div class="ca-form-content-line-content" :class="[ isPageShow ? width760 : width850]">
                            <div>
                              <div v-for="(file, i) in jobitem.taskFileList" :key="('test-file-' + (i))">
                                <div v-if="file.authStatus" style="line-height: 28px;">
                                  <a href="javascript:void(0)" @click="downloadFile(file)">  {{ file.name }} </a>
                                </div>
                                <div v-else style="line-height: 28px;">
                                  {{ file.name }}
                                </div>
                              </div>
                            </div>
                          </div>
                        </div>
                      </div>
                      <div class="ca-form-content">
                        <div class="ca-form-content-line">
                          <div class="ca-form-content-line-label md right">
                            工作陈述:
                          </div>
                          <div class="ca-form-content-line-content" :class="[ isPageShow ? width760 : width850]">
                            <div v-rich-html="jobitem.submitDescription"></div>
                          </div>
                        </div>
                      </div>
                      <div  v-if="jobitem.submitFileList" class="ca-form-content">
                        <div class="ca-form-content-line">
                          <div class="ca-form-content-line-label md right">
                            <i class="el-icon-paperclip"></i> 交付附件:
                          </div>
                          <div class="ca-form-content-line-content" :class="[ isPageShow ? width760 : width850]">
                            <div>
                              <div v-for="(file, i) in jobitem.submitFileList" :key="('test-submit-file-' + (i))">
                                <div v-if="file.authStatus" style="line-height: 28px;">
                                  <a href="javascript:void(0)" @click="downloadFile(file)">  {{ file.name }} </a>
                                </div>
                                <div v-else style="line-height: 28px;">
                                  {{ file.name }}
                                </div>
                              </div>
                            </div>
                          </div>
                        </div>
                      </div>
                    </div>
                    <div class="hr hr-10 hr-dotted"></div>
                  </div> <!--任务循环-->
                </div>
              </div> <!--测试任务-->

              <div v-if="characterDocPageInfo.allIntegrationJobList && characterDocPageInfo.allIntegrationJobList.length>0">
                <div class="hr hr-10 hr-dotted hr-double"></div>
                <div style="line-height: 30px; font-size: 15px; font-weight: 500">
                  <i class="el-icon-document"></i> 特性集成执行信息
                </div>
                <div> <!--所有任务-->
                  <div v-for="(jobitem, index) in characterDocPageInfo.allIntegrationJobList" :key="index"> <!--任务循环-->
                    <div class="ca-form"> <!--一条任务的信息-->
                      <div class="ca-form-header">
                        <div class="ca-form-header-line start" style="margin-left: 12px; font-weight: 500">
                          <div class="ca-form-header-line-label" style="margin-left: 26px;">
                            任务名称:
                          </div>
                          <div class="ca-form-header-line-content">
                            {{ jobitem.jobName }}
                          </div>
                        </div>
                      </div>
                      <div class="ca-form-content">
                        <div class="ca-form-content-line">
                          <div class="ca-form-content-line-content">
                            <div style="display: inline-block; margin-left: 52px;">执行人: {{ jobitem.executorName }}</div>
                            <div style="display: inline-block; margin-left: 30px;">任务类型: {{ JOB_ITEM_TYPE | optionKV(jobitem.type)}}</div>
                            <div style="display: inline-block; margin-left: 30px;">工时数量: {{ jobitem.manhour }}</div>
                            <div style="display: inline-block; margin-left: 30px;">质量评分: {{ jobitem.evaluateScore }}</div>
                          </div>
                        </div>
                      </div>
                      <div class="ca-form-content">
                        <div class="ca-form-content-line">
                          <div class="ca-form-content-line-label md right">
                            作业评价:
                          </div>
                          <div class="ca-form-content-line-content">
                            <div v-rich-html="jobitem.evaluateDesc"></div>
                          </div>
                        </div>
                      </div>
                      <div class="ca-form-content">
                        <div class="ca-form-content-line">
                          <div class="ca-form-content-line-label md right">
                            任务描述:
                          </div>
                          <div class="ca-form-content-line-content" :class="[ isPageShow ? width760 : width850]">
                            <div v-rich-html="jobitem.taskDescription"></div>
                          </div>
                        </div>
                      </div>
                      <div  v-if="jobitem.taskFileList" class="ca-form-content">
                        <div class="ca-form-content-line">
                          <div class="ca-form-content-line-label md right">
                            <i class="el-icon-paperclip"></i> 任务附件:
                          </div>
                          <div class="ca-form-content-line-content" :class="[ isPageShow ? width760 : width850]">
                            <div>
                              <div v-for="(file, i) in jobitem.taskFileList" :key="('int-file-' + (i))">
                                <div v-if="file.authStatus" style="line-height: 28px;">
                                  <a href="javascript:void(0)" @click="downloadFile(file)">  {{ file.name }} </a>
                                </div>
                                <div v-else style="line-height: 28px;">
                                  {{ file.name }}
                                </div>
                              </div>
                            </div>
                          </div>
                        </div>
                      </div>
                      <div class="ca-form-content">
                        <div class="ca-form-content-line">
                          <div class="ca-form-content-line-label md right">
                            工作陈述:
                          </div>
                          <div class="ca-form-content-line-content" :class="[ isPageShow ? width760 : width850]">
                            <div v-rich-html="jobitem.submitDescription"></div>
                          </div>
                        </div>
                      </div>
                      <div  v-if="jobitem.submitFileList" class="ca-form-content">
                        <div class="ca-form-content-line">
                          <div class="ca-form-content-line-label md right">
                            <i class="el-icon-paperclip"></i> 交付附件:
                          </div>
                          <div class="ca-form-content-line-content" :class="[ isPageShow ? width760 : width850]">
                            <div>
                              <div v-for="(file, i) in jobitem.submitFileList" :key="('int-submit-file-' + (i))">
                                <div v-if="file.authStatus" style="line-height: 28px;">
                                  <a href="javascript:void(0)" @click="downloadFile(file)">  {{ file.name }} </a>
                                </div>
                                <div v-else style="line-height: 28px;">
                                  {{ file.name }}
                                </div>
                              </div>
                            </div>
                          </div>
                        </div>
                      </div>
                    </div>
                    <div class="hr hr-10 hr-dotted"></div>
                  </div> <!--任务循环-->
                </div>
              </div> <!--集成任务-->
              <div  v-if="characterDocPageInfo.allReviewJobList && characterDocPageInfo.allReviewJobList.length>0">
                <div class="hr hr-10 hr-dotted hr-double"></div>
                <div style="line-height: 30px; font-size: 15px; font-weight: 500">
                  <i class="el-icon-document"></i> 评审任务执行信息
                </div>
                <div> <!--所有任务-->
                  <div v-for="(jobitem, index) in characterDocPageInfo.allReviewJobList" :key="index"> <!--任务循环-->
                    <div class="ca-form"> <!--一条任务的信息-->
                      <div class="ca-form-header">
                        <div class="ca-form-header-line start" style="margin-left: 12px; font-weight: 500">
                          <div class="ca-form-header-line-label" style="margin-left: 26px;">
                            任务名称:
                          </div>
                          <div class="ca-form-header-line-content">
                            {{ jobitem.jobName }}
                          </div>
                        </div>
                      </div>
                      <div class="ca-form-content">
                        <div class="ca-form-content-line">
                          <div class="ca-form-content-line-content">
                            <div style="display: inline-block; margin-left: 52px;">执行人: {{ jobitem.executorName }}</div>
                            <div style="display: inline-block; margin-left: 30px;">任务类型: {{ JOB_ITEM_TYPE | optionKV(jobitem.type)}}</div>
                            <div style="display: inline-block; margin-left: 30px;">工时数量: {{ jobitem.manhour }}</div>
                            <div style="display: inline-block; margin-left: 30px;">质量评分: {{ jobitem.evaluateScore }}</div>
                          </div>
                        </div>
                      </div>
                      <div class="ca-form-content">
                        <div class="ca-form-content-line">
                          <div class="ca-form-content-line-label md right">
                            作业评价:
                          </div>
                          <div class="ca-form-content-line-content">
                            <div v-rich-html="jobitem.evaluateDesc"></div>
                          </div>
                        </div>
                      </div>
                      <div class="ca-form-content">
                        <div class="ca-form-content-line">
                          <div class="ca-form-content-line-label md right">
                            任务描述:
                          </div>
                          <div class="ca-form-content-line-content" :class="[ isPageShow ? width760 : width850]">
                            <div v-rich-html="jobitem.taskDescription"></div>
                          </div>
                        </div>
                      </div>
                      <div  v-if="jobitem.taskFileList" class="ca-form-content">
                        <div class="ca-form-content-line">
                          <div class="ca-form-content-line-label md right">
                            <i class="el-icon-paperclip"></i> 任务附件:
                          </div>
                          <div class="ca-form-content-line-content" :class="[ isPageShow ? width760 : width850]">
                            <div>
                              <div v-for="(file, i) in jobitem.taskFileList" :key="('review-file-' + (i))">
                                <div v-if="file.authStatus" style="line-height: 28px;">
                                  <a href="javascript:void(0)" @click="downloadFile(file)">  {{ file.name }} </a>
                                </div>
                                <div v-else style="line-height: 28px;">
                                  {{ file.name }}
                                </div>
                              </div>
                            </div>
                          </div>
                        </div>
                      </div>
                      <div class="ca-form-content">
                        <div class="ca-form-content-line">
                          <div class="ca-form-content-line-label md right">
                            工作陈述:
                          </div>
                          <div class="ca-form-content-line-content" :class="[ isPageShow ? width760 : width850]">
                            <div v-rich-html="jobitem.submitDescription"></div>
                          </div>
                        </div>
                      </div>
                      <div  v-if="jobitem.submitFileList" class="ca-form-content">
                        <div class="ca-form-content-line">
                          <div class="ca-form-content-line-label md right">
                            <i class="el-icon-paperclip"></i> 工作交付附件:
                          </div>
                          <div class="ca-form-content-line-content" :class="[ isPageShow ? width760 : width850]">
                            <div>
                              <div v-for="(file, i) in jobitem.submitFileList" :key="('review-submit-file-' + (i))">
                                <div v-if="file.authStatus" style="line-height: 28px;">
                                  <a href="javascript:void(0)" @click="downloadFile(file)">  {{ file.name }} </a>
                                </div>
                                <div v-else style="line-height: 28px;">
                                  {{ file.name }}
                                </div>
                              </div>
                            </div>
                          </div>
                        </div>
                      </div>
                    </div>
                    <div class="hr hr-10 hr-dotted"></div>
                  </div> <!--任务循环-->
                </div>
              </div> <!--评审任务-->
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import Swal from "sweetalert2";
import FileSaver from 'file-saver';

export default {
  components: {
    FileSaver,
  },
  name: "character-doc-info-page",

  props: {
    characterId: {
      default: null,
    },
    isPageShow: {
      default: false,
    },
    showDefined: {
      default: true,
    },
  },

  computed: {
  },

  data() {
    return {
      loginUser: {},
      loginCustomer: {},
      characterDocPageInfo: {},
      JOB_ITEM_TYPE: JOB_ITEM_TYPE,

      // budgetExeDtoList: [],

      plantPage: 'a4-paper-big-plain',
      simplePage: 'a4-paper-big-simple',
      pageMargin: 'page-margin',
      width850: 'width-850',
      width760: 'width-760',
    };
  },

  mounted: function () {
    let _this = this;
    _this.getLoginUser();
    if (Tool.isNotEmpty(_this.characterId)) {
      _this.getCharacterDocPageInfo(_this.characterId);
    }

  },

  methods: {
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

    getCharacterDocPageInfo(characterId) {
      let _this = this;
      let formData = new window.FormData();
      formData.append('characterId', characterId);
      formData.append('loginUserId', _this.loginUser.customerUserId);
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/character/getCharacterDocPageInfo', formData).then((response) => {
        let resp = response.data;
        if (resp.success) {
          _this.characterDocPageInfo = resp.content;
          // _this.budgetExeDtoList.push(_this.characterDocPageInfo.budgetExeDto) ;
        }else{
          Toast.warning(resp.message, Swal);
        }
      });
    },

    downloadFile(file) {
      let _this = this;
      _this.getSignedUrl(file.path, file.suffix);
    },
    getSignedUrl (fileFullName, suffix) {
      let _this = this;
      let formData = new window.FormData();
      formData.append('fileFullName', fileFullName);
      formData.append('customer', _this.loginCustomer.id);
      formData.append('user', _this.loginUser.customerUserId);
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/file/rdms/oss-get-signed-url', formData).then((response)=>{
        let resp = response.data;
        Tool.openOssSignedUrl(resp, suffix);
        // FileSaver.saveAs(resp.content, fileName);
      });
    },

  },
}
</script>

<style lang="scss" scoped>
@import './../../../public/static/scss/caipd';

.paper-height {
  height: calc(100vh - 255px);
}
.page-margin{
  margin: 50px;
}
</style>
