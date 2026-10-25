<!--
 * Author: 周朔鹏
 * Date: 2025-02-25
 * Email: 391902958@qq.com
 * Website: https://www.euipd.com
-->

<template>
  <div class="quality-content">
    <table class="table-out">
      <tr>
        <table class="table-inn">
          <tr>
            <th style="width: 160px; text-align: center;">
              <span style="font-weight: bold">质量处理标题</span>
            </th>
            <th>
              <input style="width: 720px; border: 0; padding-left: 8px;" v-model="qualityItem.analysisName"
                     placeholder="请输入质量问题分析标题,不少于4个字">
            </th>
          </tr>
        </table>
      </tr>
      <tr>
        <table class="table-inn">
          <tr style="border-top: 2px solid #1a1a1a;">
            <th style="width: 19.51%; text-align: center;">
              <span>质量问题描述</span>
            </th>
            <th style="width: 48.78%;">
                    <span style="margin-left: 10px;">
                      填表说明: 对质量问题所进行的详细描述.内容来自质量反馈单.
                    </span>
            </th>
            <th style="width: 31.71%;">
                  <span style="margin-left: 10px;">
                    字数 <small><em>(不计空白与格式)</em></small>: {{ getQualityDescriptionNum }} / 5000
                  </span>
            </th>
          </tr>
        </table>
      </tr>
      <tr>
        <table class="table-inn">
          <tr>
            <div style="width: 100%; border-top: 1px solid #1a1a1a; border-right: 1px solid #1a1a1a; border-left: 1px solid #1a1a1a; padding: 3px 5px 3px 15px;">
              <span v-rich-html="qualityItem.qualityDescription"></span>
<!--              {{ qualityItem.qualityDescription }}-->
            </div>
          </tr>
        </table>
      </tr>
      <tr>
        <table class="table-inn">
          <tr style="border-top: 2px solid #1a1a1a;">
            <th style="width: 19.51%; text-align: center;">
              <span>触发条件/问题现象</span>
            </th>
            <th style="width: 48.78%;">
                    <span style="margin-left: 10px;">
                      填表说明: 对问题触发的条件或者现象进行描述.内容来自质量反馈单.
                    </span>
            </th>
            <th style="width: 31.71%;">
                  <span style="margin-left: 10px;">
                    字数 <small><em>(不计空白与格式)</em></small>: {{ getTriggerConditionNum }} / 5000
                  </span>
            </th>
          </tr>
        </table>
      </tr>
      <tr>
        <table class="table-inn">
          <tr>
            <div style="width: 100%; border-top: 1px solid #1a1a1a; border-right: 1px solid #1a1a1a; border-left: 1px solid #1a1a1a; padding: 3px 5px 3px 15px; min-height: 20px;">
              <span v-rich-html="qualityItem.triggerCondition"></span>
<!--              {{ qualityItem.triggerCondition }}-->
            </div>
          </tr>
<!--          <tr>
            <rich-editor
                key="quality-condition"
                v-model="qualityItem.workCondition"
                placeholder="请输入正文（不计空白与格式，5000 字以内）"
                :max-length="5000"
                :show-count="false"
            />
          </tr>-->
        </table>
      </tr>
      <tr v-if="resFiles && resFiles.length>0">
        <table class="table-inn">
          <tr style="border-top: 2px solid #1a1a1a;">
            <th style="width: 19.51%; text-align: center;">
              <span>参考附件</span>
            </th>
            <th style="width: 80.49%;">
                    <span style="margin-left: 10px;">
                      填表说明: 质量问题发现者提供的参考附件.
                    </span>
            </th>
          </tr>
        </table>
      </tr>
      <tr v-if="resFiles && resFiles.length>0">
        <table class="table-inn">
          <tr style="border-left: 1px solid #1a1a1a; border-top: 1px solid #1a1a1a; border-right: 1px solid #1a1a1a;">
            <div style="display: flex; align-items: flex-start; flex-direction: column; justify-content: center; margin-left: 12px;">
              <div v-for="(item, i) in resFiles" :key="i" style="display: flex; flex-direction: row; ">
                <div v-if="item">
                  {{ item.name }}
                </div>
                <div style="width: 60px; display: flex; flex-direction: row; align-items: center; justify-content: space-evenly;">
<!--              <div v-if="item && item.authStatus">-->
                  <div>
                    <el-button type="text" @click="downloadFile(item)">下载</el-button>
                  </div>
                </div>
              </div>
            </div>
          </tr>
        </table>
      </tr>
      <tr>
        <table class="table-inn">
          <tr style="border-top: 2px solid #1a1a1a;">
            <th style="width: 19.51%; text-align: center;">
              <span>质量问题本质分析</span>
            </th>
            <th style="width: 48.78%;">
                    <span style="margin-left: 10px;">
                      填表说明: 从技术的角度,对指令问题的发生尽心本质分析.
                    </span>
            </th>
            <th style="width: 31.71%;">
                  <span style="margin-left: 10px;">
                    字数 <small><em>(不计空白与格式)</em></small>: {{ getEssentialAnalysisNum }} / 5000
                  </span>
            </th>
          </tr>
        </table>
      </tr>
      <tr>
        <table class="table-inn">
          <tr style="border-left: 1px solid #1a1a1a; border-top: 1px solid #1a1a1a; border-right: 1px solid #1a1a1a">
            <rich-editor
                key="quality-input"
                v-model="qualityItem.essentialAnalysis"
                placeholder="请输入正文（不计空白与格式，5000 字以内）"
                :max-length="5000"
                :show-count="false"
            />
          </tr>
        </table>
      </tr>
      <tr>
        <table class="table-inn">
          <tr style="border-top: 2px solid #1a1a1a;">
            <th style="width: 19.51%; text-align: center;">
              <span>解决方案/处理措施</span>
            </th>
            <th style="width: 48.78%;">
                    <span style="margin-left: 10px;">
                      填表说明: 针对问题的根源,提出解决方案,并对实施方法进行详细描述.
                    </span>
            </th>
            <th style="width: 31.71%;">
                  <span style="margin-left: 10px;">
                    字数 <small><em>(不计空白与格式)</em></small>: {{ getSolutionNum }} / 5000
                  </span>
            </th>
          </tr>
        </table>
      </tr>
      <tr>
        <table class="table-inn">
          <tr style="border-left: 1px solid #1a1a1a; border-top: 1px solid #1a1a1a; border-right: 1px solid #1a1a1a">
            <rich-editor
                key="quality-logical"
                v-model="qualityItem.solution"
                placeholder="请输入正文（不计空白与格式，5000 字以内）"
                :max-length="5000"
                :show-count="false"
            />
          </tr>
        </table>
      </tr>
      <tr>
        <table class="table-inn">
          <tr style="border-top: 2px solid #1a1a1a;">
            <th style="width: 36.59%; text-align: center;">
              <span>输出逻辑/交付指标/开发目标(Objectives)</span>
            </th>
            <th style="width: 31.71%;">
                    <span style="margin-left: 10px;">
                      填表说明: 请对这个功能的输出进行描述.
                    </span>
            </th>
            <th style="width: 31.71%;">
                  <span style="margin-left: 10px;">
                    字数 <small><em>(不计空白与格式)</em></small>: {{ getOutputIndicatorNum }} / 5000
                  </span>
            </th>
          </tr>
        </table>
      </tr>
      <tr>
        <table class="table-inn">
          <tr style="border-left: 1px solid #1a1a1a; border-top: 1px solid #1a1a1a; border-right: 1px solid #1a1a1a">
            <rich-editor
                key="quality-output"
                v-model="qualityItem.outputIndicator"
                placeholder="请输入正文（不计空白与格式，5000 字以内）"
                :max-length="5000"
                :show-count="false"
            />
          </tr>
        </table>
      </tr>
      <tr>
        <table class="table-inn">
          <tr style="border-top: 2px solid #1a1a1a;">
            <th style="width: 36.59%; text-align: center;">
              <span>验证方法/测试方法/关键成果(Key Results)</span>
            </th>
            <th style="width: 31.71%;">
                    <span style="margin-left: 10px;">
                      填表说明: 对如何测试验证进行陈述.
                    </span>
            </th>
            <th style="width: 31.71%;">
                  <span style="margin-left: 10px;">
                    字数 <small><em>(不计空白与格式)</em></small>: {{ getTestMethodNum }} / 5000
                  </span>
            </th>
          </tr>
        </table>
      </tr>
      <tr>
        <table class="table-inn">
          <tr style="border-left: 1px solid #1a1a1a; border-top: 1px solid #1a1a1a; border-right: 1px solid #1a1a1a">
            <rich-editor
                key="quality-test"
                v-model="qualityItem.testMethod"
                placeholder="请输入正文（不计空白与格式，5000 字以内）"
                :max-length="5000"
                :show-count="false"
            />
          </tr>
        </table>
      </tr>
    </table>
  </div>
</template>

<script>
import Swal from "sweetalert2";
import FileSaver from 'file-saver';
import RichEditor from '@/components/rich/rich-editor.vue'
import { hasPendingUploads } from '@/components/rich/rich-image'


export default {
  components: {
    RichEditor,
    FileSaver,
  },
  name: "quality-analysis-core",

  props: {
    getQualityAnalysisInfo: {
      type: Function,
      default: null
    },
    quality: {
      default: null
    },
  },

  computed: {
    getQualityDescriptionNum() {
      if (this.qualityItem.qualityDescription) {
        return Tool.richTextLength(this.qualityItem.qualityDescription);
      }
      return 0;
    },
    getTriggerConditionNum() {
      if (this.qualityItem.triggerCondition) {
        return Tool.richTextLength(this.qualityItem.triggerCondition);
      }
      return 0;
    },
    getEssentialAnalysisNum() {
      if (this.qualityItem.essentialAnalysis) {
        return Tool.richTextLength(this.qualityItem.essentialAnalysis);
      }
      return 0;
    },
    getSolutionNum() {
      if (this.qualityItem.solution) {
        return Tool.richTextLength(this.qualityItem.solution);
      }
      return 0;
    },
    getOutputIndicatorNum() {
      if (this.qualityItem.outputIndicator) {
        return Tool.richTextLength(this.qualityItem.outputIndicator);
      }
      return 0;
    },
    getTestMethodNum() {
      if (this.qualityItem.testMethod) {
        return Tool.richTextLength(this.qualityItem.testMethod);
      }
      return 0;
    },
  },

  data() {
    return {
      loginUser: {},
      loginCustomer: {},

      qualityItem: {
        characterName: '',
      },

      resFiles: [],

    };
  },

  mounted: function () {
    let _this = this;
    _this.getLoginUser();

    if(Tool.isNotEmpty(_this.quality)){
      _this.qualityItem = _this.quality;
      _this.resFiles = _this.qualityItem.fileList;
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

    getQualityAnalysisData() {
      let _this = this;
      if (hasPendingUploads()) {
        Toast.warning("图片仍在上传，请稍候再保存", Swal);
        return;
      }
      // 保存校验
      if (1 !== 1
          || !Validator.require(_this.qualityItem.analysisName, "质量分析标题", Swal)
          || !Validator.require(_this.qualityItem.qualityDescription, "质量问题描述", Swal)
          // || !Validator.require(_this.qualityItem.triggerCondition, "触发条件/问题现象", Swal)
          || !Validator.require(_this.qualityItem.essentialAnalysis, "质量问题本质分析", Swal)
          || !Validator.require(_this.qualityItem.solution, "解决方案/处理措施", Swal)
          || !Validator.require(_this.qualityItem.outputIndicator, "输出逻辑/交付指标", Swal)
          || !Validator.require(_this.qualityItem.testMethod, "验证方法/测试方法", Swal)

          || !Validator.length(_this.qualityItem.analysisName, "质量分析标题", 4, 50, Swal)
          || !Validator.richLength(_this.qualityItem.qualityDescription, "质量问题描述", 10, 5000, Swal)
          // || !Validator.richLength(_this.qualityItem.triggerCondition, "触发条件/问题现象", 10, 5000, Swal)
          || !Validator.richLength(_this.qualityItem.essentialAnalysis, "质量问题本质分析", 10, 5000, Swal)
          || !Validator.richLength(_this.qualityItem.solution, "解决方案/处理措施", 10, 5000, Swal)
          || !Validator.richLength(_this.qualityItem.outputIndicator, "输出逻辑/交付指标", 10, 5000, Swal)
          || !Validator.richLength(_this.qualityItem.testMethod, "验证方法/测试方法", 10, 5000, Swal)
      ) {
        return;
      }
      _this.getQualityAnalysisInfo(_this.qualityItem);
    },
  },
}
</script>

<style lang="scss" scoped>
@import './../../../public/static/scss/caipd.scss';

.quality-content {
  background: white;
  width: 100%;
  height: auto;
  display: block;
  box-sizing: border-box;
  .table-out{
    width: var(--table-out-width, 100%);
    max-width: 22cm;
    margin: 26px auto;
    border-top: 1px solid #1a1a1a;
    border-left: 1px solid #1a1a1a;
    border-right: 1px solid #1a1a1a;
    border-bottom: 2px solid #1a1a1a;
    .table-inn{
      width: 100%;
      tr{
        line-height: 28px;
        width: 100%;
        th{
          border-top: 1px solid #1a1a1a;
          border-left: 1px solid #1a1a1a;
          border-right: 1px solid #1a1a1a;
          font-weight: 350;
        }
      }
    }
  }
}
</style>
