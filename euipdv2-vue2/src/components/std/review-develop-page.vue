<!--
 * Author: 周朔鹏
 * Date: 2024-11-05
 * Email: 391902958@qq.com
 * Website: https://www.euipd.com
-->

<template>
  <div style="height: 100%;">
    <div class="paper-height">
      <div style="height: 100%; overflow: scroll;">
        <div class="a4-paper-big-simple">
          <div style="margin: 50px 60px;">
            <el-table
                :data="developJobList"
                row-key="id"
                stripe
                highlight-current-row
                style="width: 100%">
              <el-table-column type="expand">
                <template slot-scope="props">
                  <div style="margin-left: 60px;">
                    <div class="ca-plain-form">
                      <div class="ca-form-line">
                        <div class="ca-form-label-xxl">
                          <div class="ca-form-label-title" style="font-size: 13px; color: #99a9bf;">
                            任务名称:
                          </div>
                        </div>
                        <div class="ca-form-content">
                          {{ props.row.jobName }}
                        </div>
                      </div>
                      <div class="ca-form-line">
                        <div class="ca-form-label-xxl">
                          <div class="ca-form-label-title" style="font-size: 13px; color: #99a9bf;">
                            特性名称:
                          </div>
                        </div>
                        <div class="ca-form-content">
                          {{ props.row.characterName }}
                        </div>
                      </div>
                      <div class="ca-form-line">
                        <div class="ca-form-label-xxl">
                          <div class="ca-form-label-title" style="font-size: 13px; color: #99a9bf;">
                            工作任务描述:
                          </div>
                        </div>
                        <div class="ca-form-content">
                          {{ props.row.taskDescription }}
                        </div>
                      </div>
                      <div class="ca-form-line">
                        <div class="ca-form-label-xxl">
                          <div class="ca-form-label-title" style="font-size: 13px; color: #99a9bf;">
                            工作评分:
                          </div>
                        </div>
                        <div class="ca-form-content">
                          {{ props.row.evaluateScore }}
                        </div>
                      </div>
                      <div class="ca-form-line">
                        <div class="ca-form-label-xxl">
                          <div class="ca-form-label-title" style="font-size: 13px; color: #99a9bf;">
                            工作评价:
                          </div>
                        </div>
                        <div class="ca-form-content">
                          {{ props.row.evaluateDesc }}
                        </div>
                      </div>
                      <div class="ca-form-line">
                        <div class="ca-form-label-xxl">
                          <div class="ca-form-label-title" style="font-size: 13px; color: #99a9bf;">
                            项目经理:
                          </div>
                        </div>
                        <div class="ca-form-content">
                          {{ props.row.projectManagerName }}
                        </div>
                      </div>
                      <div class="ca-form-line">
                        <div class="ca-form-label-xxl">
                          <div class="ca-form-label-title" style="font-size: 13px; color: #99a9bf;">
                            产品经理:
                          </div>
                        </div>
                        <div class="ca-form-content">
                          {{ props.row.productManagerName }}
                        </div>
                      </div>
                      <div class="ca-form-line">
                        <div class="ca-form-label-xxl">
                          <div class="ca-form-label-title" style="font-size: 13px; color: #99a9bf;">
                            交付附件:
                          </div>
                        </div>
                        <div v-show="props.row.fileList.length>0" style="display: flex; flex-direction: column; margin-bottom: 15px;">
                          <div v-for="(item, i) in props.row.fileList" :key="i" style="display: flex; flex-direction: row; align-items: center;">
                            <div v-if="item" style="margin-top: 6px;">
                              {{item.name}}
                            </div>
                            <div v-if="item && item.authStatus">
                              <el-button type="text" style="margin-left: 50px;" @click="downloadFile(item)">下载</el-button>
                            </div>
                          </div>
                        </div>
                      </div>
                    </div>
                  </div>
                  <div v-if="props.row.assistJobitemList && props.row.assistJobitemList.length>0" style="margin-left: 60px;">
                    <el-table
                        :data="props.row.assistJobitemList"
                        row-key="id"
                        stripe
                        highlight-current-row
                        style="width: 100%">
                      <el-table-column type="expand">
                        <template slot-scope="childProps">
                          <div style="margin-left: 60px;">
                            <div class="ca-plain-form">
                              <div class="ca-form-line">
                                <div class="ca-form-label-xxl">
                                  <div class="ca-form-label-title" style="font-size: 13px; color: #99a9bf;">
                                    任务名称:
                                  </div>
                                </div>
                                <div class="ca-form-content">
                                  {{ childProps.row.jobName }}
                                </div>
                              </div>
                              <div class="ca-form-line">
                                <div class="ca-form-label-xxl">
                                  <div class="ca-form-label-title" style="font-size: 13px; color: #99a9bf;">
                                    特性名称:
                                  </div>
                                </div>
                                <div class="ca-form-content">
                                  {{ childProps.row.characterName }}
                                </div>
                              </div>
                              <div class="ca-form-line">
                                <div class="ca-form-label-xxl">
                                  <div class="ca-form-label-title" style="font-size: 13px; color: #99a9bf;">
                                    工作任务描述:
                                  </div>
                                </div>
                                <div class="ca-form-content">
                                  {{ childProps.row.taskDescription }}
                                </div>
                              </div>
                              <div class="ca-form-line">
                                <div class="ca-form-label-xxl">
                                  <div class="ca-form-label-title" style="font-size: 13px; color: #99a9bf;">
                                    工作评分:
                                  </div>
                                </div>
                                <div class="ca-form-content">
                                  {{ childProps.row.evaluateScore }}
                                </div>
                              </div>
                              <div class="ca-form-line">
                                <div class="ca-form-label-xxl">
                                  <div class="ca-form-label-title" style="font-size: 13px; color: #99a9bf;">
                                    工作评价:
                                  </div>
                                </div>
                                <div class="ca-form-content">
                                  {{ childProps.row.evaluateDesc }}
                                </div>
                              </div>
                              <div class="ca-form-line">
                                <div class="ca-form-label-xxl">
                                  <div class="ca-form-label-title" style="font-size: 13px; color: #99a9bf;">
                                    项目经理:
                                  </div>
                                </div>
                                <div class="ca-form-content">
                                  {{ childProps.row.projectManagerName }}
                                </div>
                              </div>
                              <div class="ca-form-line">
                                <div class="ca-form-label-xxl">
                                  <div class="ca-form-label-title" style="font-size: 13px; color: #99a9bf;">
                                    产品经理:
                                  </div>
                                </div>
                                <div class="ca-form-content">
                                  {{ childProps.row.productManagerName }}
                                </div>
                              </div>
                              <div class="ca-form-line">
                                <div class="ca-form-label-xxl">
                                  <div class="ca-form-label-title" style="font-size: 13px; color: #99a9bf;">
                                    交付附件:
                                  </div>
                                </div>
                                <div v-show="childProps.row.fileList.length>0" style="display: flex; flex-direction: column; margin-bottom: 15px;">
                                  <div v-for="(item, i) in childProps.row.fileList" :key="i" style="display: flex; flex-direction: row;">
                                    <div v-if="item" style="margin-top: 6px;">
                                      {{item.name}}
                                    </div>
                                    <div v-if="item && item.authStatus">
                                      <el-button type="text" style="margin-left: 50px;" @click="downloadFile(item)">下载</el-button>
                                    </div>
                                  </div>
                                </div>
                              </div>
                            </div>
                          </div>
                        </template>
                      </el-table-column>
                      <el-table-column
                          prop="jobSerial"
                          label="任务号"
                          width="180">
                      </el-table-column>
                      <el-table-column
                          prop="jobName"
                          show-overflow-tooltip
                          label="任务名称">
                      </el-table-column>
                      <el-table-column
                          prop="type"
                          width="100"
                          :formatter="typeFormat"
                          label="任务类型">
                      </el-table-column>
                      <el-table-column
                          prop="executorName"
                          label="执行人"
                          width="100">
                      </el-table-column>
                      <el-table-column
                          prop="manhour"
                          width="100"
                          label="标准工时">
                      </el-table-column>
                    </el-table>
                  </div>
                </template>
              </el-table-column>
              <el-table-column
                  prop="jobSerial"
                  label="任务号"
                  width="180">
              </el-table-column>
              <el-table-column
                  prop="jobName"
                  label="任务名称">
              </el-table-column>
              <el-table-column
                  prop="type"
                  :formatter="typeFormat"
                  label="任务类型">
              </el-table-column>
              <el-table-column
                  prop="executorName"
                  label="执行人"
                  width="180">
              </el-table-column>
              <el-table-column
                  prop="manhour"
                  label="标准工时">
              </el-table-column>
            </el-table>
          </div>

        </div>
      </div>
    </div>

    <!--显示任务过程信息-->
    <el-dialog
        width="29cm"
        :visible.sync="showJobitemProcessDlg"
        :before-close="handleClose"
        :close-on-click-modal=false>
      <div slot="title" style="font-size: 20px;">{{ dialogTitle }}</div>
      <div>
        <JobitemDetailInfoDlg
            :key="('detail-' + (jobitemId))"
            :jobitem-id="jobItem.id"
        ></JobitemDetailInfoDlg>
      </div>
    </el-dialog>

  </div>
</template>

<script>
import Swal from "sweetalert2";
import JobitemDetailInfoDlg from "@/components/std/jobitem-detail-info-dlg.vue";
import {optionKV} from "@/filter/filter";
import FileSaver from 'file-saver';

export default {
  components: {
    JobitemDetailInfoDlg,

  },
  name: "review-develop-page",

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

  computed: {
    getJobDescriptionFontNum() {
      if (Tool.isNotEmpty(this.jobitemProperty.jobDescription)) {
        return Tool.richTextLength(this.jobitemProperty.jobDescription);
      }
      return 0;
    },
  },

  data() {
    return {
      loginUser: {},
      loginCustomer: {},
      developJobList: [],

      showJobitemProcessDlg: false,
      dialogTitle: '',

      jobItem: {},
      jobitemProperty: {
        jobDescription: '',
      },
    };
  },

  mounted: function () {
    let _this = this;
    _this.getLoginUser();
    if (Tool.isNotEmpty(_this.jobitemId)) {
      _this.getDevelopJobitemListByReviewJobitem(_this.jobitemId);
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

    typeFormat(row, column) {
      return optionKV(JOB_ITEM_TYPE, row.type);
    },

    getDevelopJobitemListByReviewJobitem(jobitemId) {
      let _this = this;
      let formData = new window.FormData();
      formData.append('jobitemId', jobitemId);
      formData.append('loginUserId', _this.loginUser.customerUserId);
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/job-items/getDevelopJobitemListByReviewJobitem', formData).then((response) => {
        let resp = response.data;
        if (resp.success) {
          _this.developJobList = resp.content;
        } else {
          Toast.warning(resp.message, Swal);
        }
      });
    },

    showJobitemProcess() {
      let _this = this;
      _this.showJobitemProcessDlg = true;
      _this.dialogTitle = "任务处理过程";
    },
    handleClose(done) {
      let _this = this;
      _this.showJobitemProcessDlg = false;
      done();
    },

    closeDialog() {
      let _this = this;
      _this.showJobitemProcessDlg = false;
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

</style>
