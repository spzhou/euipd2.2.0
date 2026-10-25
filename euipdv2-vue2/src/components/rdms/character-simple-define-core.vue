<!--
 * Author: 周朔鹏
 * Date: 2024-07-15
 * Email: 391902958@qq.com
 * Website: https://www.euipd.com
-->

<template>
  <div class="character-content">
    <RichEditSession ref="session" :character-id="characterItem.id" :branch-id="characterItem.richBranchId"
                     :sections="characterItem" @restore-draft="applyDraft"></RichEditSession>
    <table class="table-out">
      <tr>
        <table class="table-inn">
          <tr>
            <th style="width: 19.51%; text-align: center;">
              <span style="font-weight: 600">特性名称</span>
            </th>
            <th>
              <input style="width: 400px; border: 0;" v-model="characterItem.characterName"
                     placeholder="请输入特性名称,不少于4个字">
            </th>
            <th style="width: 14.63%; text-align: center;">
              <span>特性编号</span>
            </th>
            <th style="width: 24.39%;">
                  <span style="margin-left: 10px;">
                    {{ iterationVersion | iterationFormat }}
                  </span>
                  <el-button type="text" size="mini" style="margin-left: 6px;" @click="importVisible = true">从 Word 导入</el-button>
            </th>
          </tr>
        </table>
      </tr>
      <tr>
        <table class="table-inn">
          <tr style="border-top: 2px solid #1a1a1a;">
            <th style="width: 19.51%; text-align: center;">
              <span>特性描述</span>
            </th>
            <th style="width: 48.78%;">
                    <span style="margin-left: 10px;">
                      填表说明: 请概要描述功能或特性的概要, 要求清晰明了.
                    </span>
            </th>
            <th style="width: 31.71%;">
                  <span style="margin-left: 10px;">
                    字数 <small><em>(不计空白与格式)</em></small>: {{ getCharacterDescFontNum }} / 5000
                  </span>
            </th>
          </tr>
        </table>
      </tr>
      <tr>
        <table class="table-inn">
          <tr  style="border: 1px solid #1a1a1a">
            <rich-editor
                key="character-desc"
                v-model="characterItem.functionDescription"
                placeholder="请输入正文（不计空白与格式，5000 字以内）"
                :min-length="10"
                :max-length="5000"
                :show-count="false"
            />
          </tr>
        </table>
      </tr>
      <tr>
        <table class="table-inn">
          <tr>
            <th style="width: 19.51%; text-align: center;">
              <span>生效条件/应用范围</span>
            </th>
            <th style="width: 48.78%;">
                    <span style="margin-left: 10px;">
                      填表说明: 请详细说明这个功能或特性的生效条件.
                    </span>
            </th>
            <th style="width: 31.71%;">
                  <span style="margin-left: 10px;">
                    字数 <small><em>(不计空白与格式)</em></small>: {{ getWorkConditionFontNum }} / 5000
                  </span>
            </th>
          </tr>
        </table>
      </tr>
      <tr>
        <table class="table-inn">
          <tr style="border: 1px solid #1a1a1a">
            <rich-editor
                key="character-condition"
                v-model="characterItem.workCondition"
                placeholder="请输入正文（不计空白与格式，5000 字以内）"
                :min-length="10"
                :max-length="5000"
                :show-count="false"
            />
          </tr>
        </table>
      </tr>
      <tr>
        <table class="table-inn">
          <tr>
            <th style="width: 19.51%; text-align: center;">
              <span>输入逻辑/使用条件</span>
            </th>
            <th style="width: 48.78%;">
                    <span style="margin-left: 10px;">
                      填表说明: 请详细说明这个功能的输入是什么,输入的逻辑或条件是什么?
                    </span>
            </th>
            <th style="width: 31.71%;">
                  <span style="margin-left: 10px;">
                    字数 <small><em>(不计空白与格式)</em></small>: {{ getInputLogicalFontNum }} / 5000
                  </span>
            </th>
          </tr>
        </table>
      </tr>
      <tr>
        <table class="table-inn">
          <tr style="border: 1px solid #1a1a1a">
            <rich-editor
                key="character-input"
                v-model="characterItem.inputLogicalDesc"
                placeholder="请输入正文（不计空白与格式，5000 字以内）"
                :min-length="10"
                :max-length="5000"
                :show-count="false"
            />
          </tr>
        </table>
      </tr>
      <tr>
        <table class="table-inn">
          <tr>
            <th style="width: 19.51%; text-align: center;">
              <span>功能逻辑/功能要求</span>
            </th>
            <th style="width: 48.78%;">
                    <span style="margin-left: 10px;">
                      填表说明: 请对这个功能的执行逻辑或这行过程进行描述.
                    </span>
            </th>
            <th style="width: 31.71%;">
                  <span style="margin-left: 10px;">
                    字数 <small><em>(不计空白与格式)</em></small>: {{ getFunctionLogicalFontNum }} / 5000
                  </span>
            </th>
          </tr>
        </table>
      </tr>
      <tr>
        <table class="table-inn">
          <tr style="border: 1px solid #1a1a1a">
            <rich-editor
                key="character-logical"
                v-model="characterItem.functionLogicalDesc"
                placeholder="请输入正文（不计空白与格式，5000 字以内）"
                :min-length="10"
                :max-length="5000"
                :show-count="false"
            />
          </tr>
        </table>
      </tr>
      <tr>
        <table class="table-inn">
          <tr>
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
                    字数 <small><em>(不计空白与格式)</em></small>: {{ getOutputLogicalFontNum }} / 5000
                  </span>
            </th>
          </tr>
        </table>
      </tr>
      <tr>
        <table class="table-inn">
          <tr style="border: 1px solid #1a1a1a">
            <rich-editor
                key="character-output"
                v-model="characterItem.outputLogicalDesc"
                placeholder="请输入正文（不计空白与格式，5000 字以内）"
                :min-length="10"
                :max-length="5000"
                :show-count="false"
            />
          </tr>
        </table>
      </tr>
      <tr>
        <table class="table-inn">
          <tr>
            <th style="width: 36.59%; text-align: center;">
              <span>验证方法/测试方法/关键成果(Key Results)</span>
            </th>
            <th style="width: 31.71%;">
                    <span style="margin-left: 10px;">
                      填表说明: 请对如何测试和验证这项功能进行描述.
                    </span>
            </th>
            <th style="width: 31.71%;">
                  <span style="margin-left: 10px;">
                    字数 <small><em>(不计空白与格式)</em></small>: {{ getTestMethodFontNum }} / 5000
                  </span>
            </th>
          </tr>
        </table>
      </tr>
      <tr>
        <table class="table-inn">
          <tr style="border: 1px solid #1a1a1a">
            <rich-editor
                key="character-test"
                v-model="characterItem.testMethod"
                placeholder="请输入正文（不计空白与格式，5000 字以内）"
                :min-length="10"
                :max-length="5000"
                :show-count="false"
            />
          </tr>
        </table>
      </tr>
    </table>
    <RichImportDlg :visible.sync="importVisible" biz-type="CHARACTER_DEF" @apply="applyImport"></RichImportDlg>
  </div>
</template>

<script>
import Swal from "sweetalert2";
import RichEditor from '@/components/rich/rich-editor.vue'
import RichImportDlg from '@/components/rich/rich-import-dlg.vue'
import RichEditSession from '@/components/rich/rich-edit-session.vue'
import { hasPendingUploads } from '@/components/rich/rich-image'

export default {
  components: {
    RichEditSession,
    RichImportDlg,
    RichEditor,
  },
  name: "character-simple-define-core",

  props: {
    characterItemInit:{
      default: null,
    },
    //callback
    saveCharacterInfo: {
      type: Function,
      default: null
    },
  },

  computed: {
    getCharacterDescFontNum() {
      if (this.characterItem.functionDescription) {
        return Tool.richTextLength(this.characterItem.functionDescription);
      }
      return 0;
    },
    getWorkConditionFontNum() {
      if (this.characterItem.workCondition) {
        return Tool.richTextLength(this.characterItem.workCondition);
      }
      return 0;
    },
    getInputLogicalFontNum() {
      if (this.characterItem.inputLogicalDesc) {
        return Tool.richTextLength(this.characterItem.inputLogicalDesc);
      }
      return 0;
    },
    getFunctionLogicalFontNum() {
      if (this.characterItem.functionLogicalDesc) {
        return Tool.richTextLength(this.characterItem.functionLogicalDesc);
      }
      return 0;
    },
    getOutputLogicalFontNum() {
      if (this.characterItem.outputLogicalDesc) {
        return Tool.richTextLength(this.characterItem.outputLogicalDesc);
      }
      return 0;
    },
    getTestMethodFontNum() {
      if (this.characterItem.testMethod) {
        return Tool.richTextLength(this.characterItem.testMethod);
      }
      return 0;
    },
  },

  data() {
    return {
      importVisible: false,
      loginUser: {},
      loginCustomer: {},
      opeFlag: 'save',  //调用时, 通过 ref送参数过来
      iterationVersion: 1,
      characterItem: {},

      currentTime: {},
      currentTimeStr: '',
    };
  },

  mounted: function () {
    let _this = this;
    _this.getLoginUser();
    if(_this.characterItemInit && _this.characterItemInit.id){
      _this.characterItem = _this.characterItemInit;
      _this.iterationVersion = _this.characterItemInit.iterationVersion;
    }else{
      _this.characterItem.id = Uuid.shortUuid(8);
      _this.iterationVersion = 1;
    }
    _this.currentTime = new Date();
    _this.currentTimeStr = _this.currentTime.toLocaleString()
  },

  methods: {
    applyImport(fields) {
      Object.keys(fields).forEach(k => this.$set(this.characterItem, k, fields[k]));
      this.$set(this.characterItem, 'richSource', 'IMPORT');
    },
    applyDraft(fields) {
      Object.keys(fields).forEach(k => this.$set(this.characterItem, k, fields[k]));
    },
    getLoginUser() {
      let _this = this;
      if (Tool.getLoginUser().userGroup === LOGIN_USER_GROUP.CUSTOMER.key) {
        _this.loginUser = Tool.getLoginUser().adminUser;
        // console.log("loginUser:", _this.loginUser);
        _this.loginCustomer = Tool.getLoginUser();
      } else {
        _this.loginUser = Tool.getLoginUser();
        _this.loginCustomer = Tool.getLoginUser().customer;
      }
    },

    /**
     * 保存客户信息
     */
    saveCharacterItem() {
      let _this = this;
      if (hasPendingUploads()) {
        Toast.warning("图片仍在上传，请稍候再保存", Swal);
        return;
      }
      // 保存校验
      if (1 !== 1
          || !Validator.require(_this.characterItem.characterName, "特性名称", Swal)
          || !Validator.require(_this.characterItem.functionDescription, "特性描述", Swal)
          || !Validator.require(_this.characterItem.workCondition, "生效条件/应用范围", Swal)
          || !Validator.require(_this.characterItem.inputLogicalDesc, "输入逻辑/使用条件", Swal)
          || !Validator.require(_this.characterItem.functionLogicalDesc, "功能逻辑/功能要求", Swal)
          || !Validator.require(_this.characterItem.outputLogicalDesc, "输出逻辑/交付指标", Swal)
          || !Validator.require(_this.characterItem.testMethod, "验证方法/测试方法", Swal)

          || !Validator.length(_this.characterItem.characterName, "特性名称", 4, 80, Swal)
          || !Validator.richLength(_this.characterItem.functionDescription, "特性描述", 10, 5000, Swal)
          || !Validator.richLength(_this.characterItem.workCondition, "生效条件/应用范围", 10, 5000, Swal)
          || !Validator.richLength(_this.characterItem.inputLogicalDesc, "输入逻辑/使用条件", 10, 5000, Swal)
          || !Validator.richLength(_this.characterItem.functionLogicalDesc, "功能逻辑/功能要求", 10, 5000, Swal)
          || !Validator.richLength(_this.characterItem.outputLogicalDesc, "输出逻辑/交付指标", 10, 5000, Swal)
          || !Validator.richLength(_this.characterItem.testMethod, "验证方法/测试方法", 10, 5000, Swal)

      ) {
        return;
      }
      _this.characterItem.opeFlag = _this.opeFlag;
      if (_this.$refs.session) _this.$refs.session.stamp(_this.characterItem);
      _this.saveCharacterInfo(_this.characterItem);
    },
  },
}
</script>

<style lang="scss" scoped>
@import './../../../public/static/scss/caipd.scss';

.character-content {
  background: white;
  width: 100%;
  height: auto;
  display: block;
  box-sizing: border-box;

  .table-out {
    width: var(--table-out-width, 100%);
    max-width: 22cm;
    margin: 26px auto;
    border-top: 1px solid #1a1a1a;
    border-left: 1px solid #1a1a1a;
    border-right: 1px solid #1a1a1a;
    border-bottom: 2px solid #1a1a1a;

    .table-inn {
      width: 100%;
      table-layout: fixed;

      tr {
        line-height: 28px;
        width: 100%;

        th {
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
