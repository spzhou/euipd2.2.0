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
        <el-form-item label="需求名称:" required>
          <el-col :span="11">
            <el-input v-model="demand.demandName"></el-input>
          </el-col>
        </el-form-item>
      </el-form>
      <el-form label-width="180px">
        <el-form-item label="需求描述:" required>
          <el-col :span="24">
            <rich-editor
                key="demand-desc-create"
                v-model="demand.demandDescription"
                placeholder="请输入正文（不计空白与格式，5000 字以内）"
                :max-length="5000"
                :show-count="false"
            />
            <div class="prompt-line">{{ ('(字符数: ' + (getDemandDescFontNum) + ' 个字符 备注:包括所有格式字符等, 如果格式字符过多,可以点击"Tx"按钮清理格式字符)') }}</div>
          </el-col>
        </el-form-item>
      </el-form>
      <el-form label-width="180px">
        <el-form-item label="生效条件或使用前提:">
          <el-col :span="24">
            <rich-editor
                key="demand-condition-create"
                v-model="demand.workCondition"
                placeholder="请输入正文（不计空白与格式，5000 字以内）"
                :max-length="5000"
                :show-count="false"
            />
            <div class="prompt-line">{{ ('(字符数: ' + (getDemandConditionFontNum) + ' 个字符 备注:包括所有格式字符等, 如果格式字符过多,可以点击"Tx"按钮清理格式字符)') }}</div>
          </el-col>
        </el-form-item>
      </el-form>
      <el-form label-width="180px">
        <el-form-item label="关联项目:" required>
          <el-col :span="24">
            <el-select v-model="demand.preProjectId" filterable placeholder="请选择">
              <el-option
                  v-for="item in preProjects"
                  :key="item.id"
                  :label="item.preProjectName"
                  :value="item.id">
              </el-option>
            </el-select>
          </el-col>
        </el-form-item>
      </el-form>
      <el-form label-width="180px">
        <el-form-item label="场景或需求方名称:" required>
          <el-col :span="24">
            <el-input v-model="demand.demandCustomerName"></el-input>
          </el-col>
        </el-form-item>
      </el-form>
      <el-form label-width="180px">
        <el-form-item label="需求确认人姓名:" required>
          <el-col :span="24">
            <el-input v-model="demand.confirmPersonName"></el-input>
          </el-col>
        </el-form-item>
      </el-form>
      <el-form label-width="180px">
        <el-form-item label="需求确认人电话:" required>
          <el-col :span="24">
            <el-input v-model="demand.confirmContactTel"></el-input>
          </el-col>
        </el-form-item>
      </el-form>

    </div>
    <div style="margin: 15px 110px 20px 0">
      <div v-if="demand.preProjectId">
        <div class="hr hr-18 dotted hr-double"></div>
        <FileUploadCore
            :input-id="('demand-create-dlg')"
            :key="('file-upload-' + (demand.id))"
            :x-project-id="demand.preProjectId"
            :read-db-record="true"
            :project-type="PROJECT_TYPE.PRE_PROJECT.key"
            :item-id="demand.id"
            :file-group="FILE_GROUPING.DEMAND.key"
            :en-delete="true"
            v-on:uploadFileList="getUploadFileList"
        ></FileUploadCore>
      </div>
    </div>
    <div class="clearfix form-actions">
      <div style="display: flex; justify-content: center;">
        <button v-on:click="save()" class="ca-btn-primary" type="button">
          保存
        </button>
        <button v-on:click="submit()" class="ca-btn-primary" type="button">
          提交
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
  name: 'demand-create-dlg',

  components: {
    FileRdms,
    RichEditor,
    FileUploadCore,
  },

  props:{
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

      preProjects: [],
      demand: {},

    };
  },

  computed: {
    getDemandDescFontNum(){
      if(this.demand.demandDescription){
        return  Tool.richTextLength(this.demand.demandDescription);
      }
      return 0;
    },

    getDemandConditionFontNum(){
      if(this.demand.workCondition){
        return  Tool.richTextLength(this.demand.workCondition);
      }
      return 0;
    }


  },

  mounted: function() {
    let _this =this;
    _this.getLoginUser();
    _this.listPreProject();
    _this.demand.id = Uuid.shortUuid(8);
  },
  methods:{
    getUploadFileList(resFiles){
      let _this = this;
      _this.resFiles = resFiles;
      _this.resFileIds= [];
      if(resFiles && resFiles.length>0){
        if(resFiles && resFiles.length>0){
        resFiles.forEach(item => {
          _this.resFileIds.push(item.id);
        })
      }
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

    listPreProject() {
      let _this = this;
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/pre-project/list/'+ _this.loginCustomer.id).then((response)=>{
        let resp = response.data;
        if (resp.success) {
          _this.preProjects = resp.content;
          if(_this.preProjects.length < 1){
            Swal.fire({
              title: '操作提示',
              text: '暂无收集需求的预立项项目,请联系产品经理？',
              icon: 'warning',
              showCancelButton: false,
              confirmButtonColor: '#409EFF',
              cancelButtonColor: '#909399',
              confirmButtonText: '关闭'
            }).then((result) => {
              if (result.value) {
                _this.close();
              }
            })
          }
        } else {
          Toast.warning(resp.message,Swal);
        }
      })
    },

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
          || !Validator.require(_this.demand.demandName, "需求名称",Swal)
          || !Validator.require(_this.demand.demandDescription, "需求描述",Swal)
          || !Validator.require(_this.demand.preProjectId, "关联项目",Swal)
          || !Validator.require(_this.demand.demandCustomerName, "场景或需求方名称",Swal)
          || !Validator.require(_this.demand.confirmPersonName, "需求确认人姓名",Swal)
          || !Validator.isMobile(_this.demand.confirmContactTel, "手机号码", Swal)

          || !Validator.length(_this.demand.demandName, "需求名称", 4, 50,Swal)
          || !Validator.richLength(_this.demand.demandDescription, "需求描述", 10, 5000,Swal)
          || !Validator.richLength(_this.demand.workCondition, "生效条件", 0, 5000,Swal)
      ) {
        return;
      }

      _this.demand.customerId = _this.loginCustomer.id;
      _this.demand.writerId = _this.loginUser.customerUserId;
      _this.demand.fileList = _this.resFileIds;
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/demand/save', _this.demand).then((response)=>{
        let resp = response.data;
        if (resp.success) {
          Toast.success("保存成功", Swal);
          _this.close();
          _this.$bus.$emit('refresh-demand-list', true);
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
          || !Validator.require(_this.demand.demandName, "需求名称",Swal)
          || !Validator.require(_this.demand.demandDescription, "需求描述",Swal)
          || !Validator.require(_this.demand.preProjectId, "关联项目",Swal)
          || !Validator.require(_this.demand.demandCustomerName, "场景或需求方名称",Swal)
          || !Validator.require(_this.demand.confirmPersonName, "需求确认人姓名",Swal)
          || !Validator.isMobile(_this.demand.confirmContactTel, "手机号码", Swal)

          || !Validator.length(_this.demand.demandName, "需求名称", 4, 50,Swal)
          || !Validator.richLength(_this.demand.demandDescription, "需求描述", 10, 5000,Swal)
          || !Validator.richLength(_this.demand.workCondition, "生效条件", 0, 5000,Swal)
      ) {
        return;
      }

      _this.demand.customerId = _this.loginCustomer.id;
      _this.demand.writerId = _this.loginUser.customerUserId;
      _this.demand.fileList = _this.resFileIds;
      let text = `是否将需求提交给产品经理 ？`;
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
          _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/demand/submit', _this.demand).then((response)=>{
            let resp = response.data;
            if (resp.success) {
              Toast.success("保存成功", Swal);
              _this.$bus.$emit('refresh-demand-list', true);
              _this.close();
            } else {
              Toast.warning(resp.message, Swal);
            }
          })
        }
      })
    },


    reset(){
      let _this = this;
      _this.demand = {};
      _this.demand.id = Uuid.shortUuid(8);
      _this.resFiles = [];
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

