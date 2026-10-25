<!--
 * Author: 周朔鹏
 * Date: 2024-11-17
 * Email: 391902958@qq.com
 * Website: https://www.euipd.com
-->

<template>
  <div class="dialog-container">
    <div style="margin: 0 90px 0 0">
      <el-form label-width="180px">
        <el-form-item label="质量问题名称:" required>
          <el-col :span="11">
            <el-input v-model="quality.name"></el-input>
          </el-col>
        </el-form-item>
        <el-form-item label="问题描述:" required>
          <el-col :span="22">
            <table style="width: 100%;">
              <tr style="width: 100%; border: 1px solid #8f8e8e">
                <rich-editor
                    key="quality-desc-create"
                    v-model="quality.qualityDescription"
                    placeholder="请输入正文（不计空白与格式，5000 字以内）"
                    :max-length="5000"
                    :show-count="false"
                />
              </tr>
            </table>
            <div class="prompt-line">{{ ('(字符数: ' + (getQualityDescFontNum) + ' 个字符 备注:包括所有格式字符等, 如果格式字符过多,可以点击"Tx"按钮清理格式字符)') }}</div>
          </el-col>
        </el-form-item>
        <el-form-item label="触发条件或过程:">
          <el-col :span="22">
            <table style="width: 100%;">
              <tr style="width: 100%; border: 1px solid #8f8e8e">
                <rich-editor
                    key="quality-condition-create"
                    v-model="quality.triggerCondition"
                    placeholder="请输入正文（不计空白与格式，5000 字以内）"
                    :max-length="5000"
                    :show-count="false"
                />
              </tr>
            </table>
            <div class="prompt-line">{{ ('(字符数: ' + (getQualityConditionFontNum) + ' 个字符 备注:包括所有格式字符等, 如果格式字符过多,可以点击"Tx"按钮清理格式字符)') }}</div>
          </el-col>
        </el-form-item>
        <el-form-item label="关联项目:" required>
          <el-cascader
              style="width: 500px"
              :options="projectTreeList"
              v-model="optionValue"
              :props="{ checkStrictly: true }"
              clearable
              @change="getSelectedProjectId"
          >
          </el-cascader>
        </el-form-item>
<!--        <el-form-item label="关联特性" required>
          <el-col :span="24">
            <el-select v-model="selectCharacterId" filterable placeholder="关联特性" style="width: 220px;">
              <el-option v-for="o in characterDtoList" :key="o.id" :value="o.id" :label="o.characterName"></el-option>
            </el-select>
          </el-col>
        </el-form-item>-->
<!--        <el-form-item label="责成完成时间" required>
          <el-col :span="7">
            <el-date-picker
                type="date"
                placeholder="选择日期"
                :picker-options="pickerOptions"
                v-model="quality.planSubmissionTime">
            </el-date-picker>
          </el-col>
        </el-form-item>-->
        <el-form-item label="场景或需求方名称:">
          <el-col :span="7">
            <div style="width: 220px;">
              <el-input v-model="quality.consumer"></el-input>
            </div>
          </el-col>
        </el-form-item>
        <el-form-item label="质量反馈人:">
          <el-col :span="7">
            <div style="width: 220px;">
              {{loginUser.trueName }}
            </div>
          </el-col>
        </el-form-item>
        <el-form-item label="联系电话:">
          <el-col :span="7">
            <div style="width: 220px">
              <el-input type="number" v-model="quality.confirmTel"></el-input>
            </div>
          </el-col>
        </el-form-item>
      </el-form>
    </div>
    <div style="margin: 15px 110px 20px 0">
      <div v-if="optionValue && optionValue.length>0">
        <div class="hr hr-18 dotted hr-double"></div>
        <FileUploadCore
            :input-id="('quality-create-dlg')"
            :key="('file-upload-' + (quality.id))"
            :x-project-id="quality.subprojectId"
            :read-db-record="true"
            :project-type="PROJECT_TYPE.SUB_PROJECT.key"
            :item-id="quality.id"
            :file-group="FILE_GROUPING.QUALITY.key"
            :en-delete="true"
            v-on:uploadFileList="getUploadFileList"
        ></FileUploadCore>
      </div>
    </div>
    <div class="clearfix form-actions">
      <div style="display: flex; justify-content: center;">
        <button v-on:click="save()" class="ca-btn-primary" type="button">
          保 存
        </button>
        <button v-on:click="submit()" class="ca-btn-primary" type="button">
          提 交
        </button>
      </div>
    </div>
  </div>
</template>
<script>
import Swal from "sweetalert2";
import FileRdms from "@/components/rdms/file-rdms";
import FileUploadCore from "@/components/std/file-upload-core.vue";
import FileSaver from 'file-saver';

import RichEditor from '@/components/rich/rich-editor.vue'
import { hasPendingUploads } from '@/components/rich/rich-image'

export default {
  name: 'quality-create-dlg',

  components: {
    FileRdms,
    RichEditor,
    FileUploadCore,
  },

  props:{
    qualityItem: {
      default: null,
    },
    closeFunction: {
      type: Function,
      default: null
    },
  },

  data() {
    return {
      loginUser: {},
      loginCustomer: {},

      FILE_GROUPING: FILE_GROUPING,
      PROJECT_TYPE: PROJECT_TYPE,
      DOC_TYPE: DOC_TYPE,

      resFile: {},
      resFiles: [],
      resFileIds: [],

      projectTreeList: [],
      optionValue: [],
      preProjects: [],
      quality: {},

      characterDtoList: [],
      selectCharacterId: '',


      pickerOptions: {
        disabledDate(v) {
          return v.getTime() < new Date().getTime() - 86400000;//  - 86400000是否包括当天
        }
      },
    };
  },

  computed: {
    getQualityDescFontNum(){
      if(this.quality.qualityDescription){
        return  Tool.richTextLength(this.quality.qualityDescription);
      }
      return 0;
    },

    getQualityConditionFontNum(){
      if(this.quality.triggerCondition){
        return  Tool.richTextLength(this.quality.triggerCondition);
      }
      return 0;
    }
  },

  mounted: function() {
    let _this =this;
    _this.getLoginUser();
    if(_this.qualityItem){
      _this.quality = _this.qualityItem;
      _this.optionValue = JSON.parse(_this.qualityItem.associateProjectStr);
    }else{
      _this.quality.id = Uuid.shortUuid(8);
    }
    _this.getProjectTreeListByCustomerId();
  },
  methods:{
    getUploadFileList(resFiles){
      let _this = this;
      _this.resFiles = resFiles;
      _this.resFileIds= [];
      if(resFiles && resFiles.length>0){
        resFiles.forEach(item => {
          _this.resFileIds.push(item.id);
        })
      }
    },

    dateFormat(row, column) {
      return row.createTimeStr.substring(0,10);
    },
    versionNumFormat(row, column) {
      return row.devVersion == null ? '' : String(row.devVersion);
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

    getProjectTreeListByCustomerId(){
      let _this = this;
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/quality/getProjectTreeListByCustomerId/'+ _this.loginCustomer.id).then((response)=>{
        let resp = response.data;
        if (resp.success) {
          _this.projectTreeList = resp.content;
        } else {
          Toast.warning(resp.message,Swal);
        }
      })
    },

    getSelectedProjectId(value){
      let _this = this;
      // console.log("getSelectedProjectId", value);
      let subprojectId = value[value.length-1];
      // console.log("value[value.length-1]", subprojectId);
      // _this.getCharacterListIncludeNextLevelBySubprojectId(subprojectId);
    },

/*
    getCharacterListIncludeNextLevelBySubprojectId(subprojectId){
      let _this = this;
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/quality/getCharacterListIncludeNextLevelBySubprojectId/'+ subprojectId).then((response)=>{
        let resp = response.data;
        if (resp.success) {
          _this.characterDtoList = resp.content;
        } else {
          Toast.warning(resp.message,Swal);
        }
      })
    },
*/


    /**
     * 保存需求
     */
    save() {
      let _this = this;
      if (hasPendingUploads()) {
        Toast.warning("图片仍在上传，请稍候再保存", Swal);
        return;
      }
      // 保存校验
      if (1 !== 1
          || !Validator.require(_this.quality.id, "质量Id",Swal)
          || !Validator.require(_this.quality.name, "质量标题",Swal)
          || !Validator.require(_this.quality.qualityDescription, "问题描述",Swal)
          // || !Validator.require(_this.quality.planSubmissionTime, "责成完成时间",Swal)

          || !Validator.length(_this.quality.name, "质量标题", 4, 50,Swal)
          || !Validator.richLength(_this.quality.qualityDescription, "问题描述", 10, 5000,Swal)
      ) {
        return;
      }

      if(Tool.isEmpty_ES5(_this.optionValue) || _this.optionValue.length === 0){
        Toast.warning("关联项目不能为空!", Swal);
        return;
      }

/*
      if(Tool.isEmpty(_this.selectCharacterId) || _this.selectCharacterId === ""){
        Toast.warning("关联特性不能为空!", Swal);
        return;
      }
*/

      let iter = _this.optionValue.length;
      _this.quality.subprojectId = _this.optionValue[iter -1];
      _this.quality.projectId = _this.optionValue[0];
      _this.quality.associateProjectStr = JSON.stringify(_this.optionValue);
      // _this.quality.associateCharacterId = _this.selectCharacterId;
      _this.quality.status = QUALITY_STATUS.SAVE.key;
      _this.quality.customerId = _this.loginCustomer.id;
      _this.quality.writerId = _this.loginUser.customerUserId;
      _this.quality.fileListStr = JSON.stringify(_this.resFileIds);
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/quality/save', _this.quality).then((response)=>{
        let resp = response.data;
        if (resp.success) {
          Toast.success("保存成功", Swal);
          _this.close();
          _this.$bus.$emit('refresh-quality-define-list', true);
        } else {
          Toast.warning(resp.message, Swal);
        }
      })
    },

    /**
     * 提交需求
     */
    submit() {
      let _this = this;
      if (hasPendingUploads()) {
        Toast.warning("图片仍在上传，请稍候再保存", Swal);
        return;
      }
      // 保存校验
      if (1 !== 1
          || !Validator.require(_this.quality.id, "质量Id",Swal)
          || !Validator.require(_this.quality.name, "质量标题",Swal)
          || !Validator.require(_this.quality.qualityDescription, "问题描述",Swal)
          // || !Validator.require(_this.quality.planSubmissionTime, "责成完成时间",Swal)

          || !Validator.length(_this.quality.name, "质量标题", 4, 50,Swal)
          || !Validator.richLength(_this.quality.qualityDescription, "问题描述", 10, 5000,Swal)
      ) {
        return;
      }

      if(Tool.isEmpty_ES5(_this.optionValue) || _this.optionValue.length === 0){
        Toast.warning("关联项目不能为空!", Swal);
        return;
      }

      let iter = _this.optionValue.length;
      _this.quality.subprojectId = _this.optionValue[iter -1];
      _this.quality.projectId = _this.optionValue[0];
      _this.quality.associateProjectStr = JSON.stringify(_this.optionValue);
      _this.quality.status = QUALITY_STATUS.SUBMIT.key;
      _this.quality.customerId = _this.loginCustomer.id;
      _this.quality.writerId = _this.loginUser.customerUserId;
      _this.quality.fileListStr = JSON.stringify(_this.resFileIds);
      let text = `是否提交质量反馈给产品经理 ？`;
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
          _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/quality/submit', _this.quality).then((response)=>{
            let resp = response.data;
            if (resp.success) {
              Toast.success("保存成功", Swal);
              _this.$bus.$emit('refresh-quality-define-list', true);
              _this.$bus.$emit('notify-quality-define-submit-num', true);
              _this.$bus.$emit('notify-jobitem-count-flag', true); //执行管理
              _this.close();
            } else {
              Toast.warning(resp.message, Swal);
            }
          })
        }
      })
    },

    close(){
      let _this = this;
      _this.closeFunction();
    },

  }
};
</script>

<style lang="scss" scoped>
@import './../../../public/static/scss/caipd.scss';

</style>

