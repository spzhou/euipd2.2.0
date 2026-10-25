<!--
 * Author: 周朔鹏
 * Date: 2025-04-14
 * Email: 391902958@qq.com
 * Website: https://www.euipd.com
-->

<template>
  <div>
    <div  class="dialog-container">
      <div class="ca-form">
        <el-form label-width="150px" size="small">
          <el-form-item
              label="特性名称:"
              required>
<!--            <el-input style="width: 300px;"
                      v-model="setupProject.projectName"
            ></el-input>-->
            {{ setupProject.projectName }}
          </el-form-item>
          <el-form-item
              label="特性编号:">
            <div>
              {{ characterItem.iterationVersion | iterationFormat }}
            </div>
          </el-form-item>
          <el-form-item
              label="产品经理:">
            <div>
              {{ characterItem.productManagerName }}
            </div>
          </el-form-item>
          <el-form-item
              required
              label="开发目的概述:">
            <el-input style="width: 783px;"
                      type="textarea"
                      :autosize="{ minRows: 5, maxRows: 15}"
                      placeholder="请输入开发概述"
                      v-model="setupProject.projectIntroduce"
            ></el-input>
          </el-form-item>
          <el-form-item
              required
              label="监管委员:">
            <el-select v-model="setupProject.supervise"  filterable placeholder="请选择">
              <el-option
                  v-for="item in supervises"
                  :key="item.id"
                  :label="item.trueName"
                  :value="item.id">
              </el-option>
            </el-select>
          </el-form-item>
        </el-form>
      </div>
      <div class="hr hr-8 dotted"></div>
      <div style="margin-left: 75px; font-size: 14px; font-weight: bold; margin-bottom: 15px;">集成特性清单:</div>
      <div style="display: flex; justify-content: center;">
        <el-transfer
            filterable
            filter-placeholder="请输入关键词"
            v-model="cbbValue"
            :data="cbbData"
            :titles="titles">
        </el-transfer>
      </div>
      <div>
<!--        <div class="hr hr-8 dotted"></div>
        <div style="margin: 15px 110px 20px 0">
          <FileUploadCore
              :input-id="('character-setup-application-' + (preProject.id))"
              :key="('file-upload-' + (preProject.id))"
              :x-project-id="preProject.id"
              :project-type="PROJECT_TYPE.PRE_PROJECT.key"
              :item-id="preProject.id"
              :file-group="FILE_GROUPING.PRE_PROJECT.key"
              v-on:uploadFileList="getUploadFileList"
          ></FileUploadCore>
        </div>-->
        <div class="clearfix form-actions" style="margin-top: 20px;">
          <div style="display: flex; justify-content: center;">
            <button v-on:click="setupApplication()" class="ca-btn-primary" type="button">
              提交
            </button>
            <button v-on:click="close()" class="ca-btn-primary" type="button">
              关闭
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>
<script>
import Swal from "sweetalert2";
import FileUploadCore from "@/components/std/file-upload-core.vue";

export default {

  name: 'character-setup-application',
  components: {
    FileUploadCore
  },
  props:{
    characterItem:{
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

      preProject: {},
      supervises: [],
      archivedCbbList: [],

      cbbData: [],
      cbbValue: [],
      titles: ['可选 特性','已选 特性'],

      setupProject: {},

      PROJECT_TYPE: PROJECT_TYPE,
      FILE_GROUPING: FILE_GROUPING,

      resFiles: [],
      fileIdList: [],
    };
  },

  mounted: function() {
    let _this =this;
    _this.getLoginUser();

    //给申请立项项目分配ID
    _this.setupProject.id = Uuid.shortUuid(8);
    if(_this.characterItem){
      _this.setupProject.projectName = JSON.parse(JSON.stringify( _this.characterItem.characterName )); //深拷贝
      _this.setupProject.productManagerId = _this.characterItem.productManagerId;
      if(_this.characterItem.moduleIdListStr){
        _this.cbbValue = JSON.parse(_this.characterItem.moduleIdListStr);
      }
    }
    _this.setupProject.rdType = RD_TYPE.MODULE.key;
  },
  methods:{
    getLoginUser(){
      let _this = this;
      if(Tool.getLoginUser().userGroup === LOGIN_USER_GROUP.CUSTOMER.key){
        _this.loginUser = Tool.getLoginUser().adminUser;
        _this.loginCustomer =Tool.getLoginUser();
      }else{
        _this.loginUser = Tool.getLoginUser();
        _this.loginCustomer = Tool.getLoginUser().customer;
      }

      _this.listIpmtByCustomerId();
      _this.getModulePreProject(_this.loginCustomer.id);
      _this.getCbbListByCustomerId(_this.loginCustomer.id);
    },

    setupApplication() {
      let _this = this;
      // 保存校验
      if (1 !== 1
          || !Validator.require(_this.setupProject.projectName, "项目名称",Swal)
          || !Validator.require(_this.setupProject.projectIntroduce, "项目简介",Swal)
          || !Validator.require(_this.setupProject.supervise, "监管委员",Swal)

          || !Validator.length(_this.setupProject.projectName, "项目名称", 2, 50,Swal)
          || !Validator.length(_this.setupProject.projectIntroduce, "项目简介", 5, 500,Swal)
          || !Validator.length(_this.setupProject.supervise, "监管委员", 8, 8,Swal)
      ) {
        return;
      }

      function exeApplication() {{
          _this.setupProject.customerId = _this.loginCustomer.id;
          _this.setupProject.loginUserId = _this.loginUser.customerUserId;
          _this.setupProject.productManagerId = _this.preProject.productManagerId;
          _this.setupProject.fileIdList = _this.fileIdList;
          let characterList = [];
          _this.characterItem.moduleIdListStr = JSON.stringify(_this.cbbValue);
          characterList.push(_this.characterItem);
          _this.setupProject.characterList = characterList;

          _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/project/setup-application', _this.setupProject).then((response) => {
            let resp = response.data;
            if (resp.success) {
              Toast.success("保存成功", Swal);
              _this.close();
              _this.$bus.publish({ areas: ['project', 'character', 'badge'], reason: 'setup-application' });
            } else {
              Toast.warning(resp.message, Swal);
            }
          })
        }
      }

      Swal.fire({
        title: '确认',
        text: "确认提交立项申请吗?",
        icon: 'question',
        showCancelButton: true,
        confirmButtonColor: '#409EFF',
        cancelButtonColor: '#909399',
        confirmButtonText: '确认!',
        cancelButtonText: '取消!'
      }).then((result) => {
        if (result.isConfirmed) {
          exeApplication();
        }
      }).catch(_ => {
      });
    },

    listIpmtByCustomerId() {
      let _this = this;
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/project/listIpmtByCustomerId/'+ _this.loginCustomer.id).then((response)=>{
        let resp = response.data;
        if (resp.success) {
          _this.supervises = resp.content;
        } else {
          Toast.warning(resp.message,Swal);
        }
      })
    },

    getModulePreProject(customerId) {
      let _this = this;
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/pre-project/getModulePreProject/'+ customerId).then((response)=>{
        let resp = response.data;
        if (resp.success) {
          _this.preProject = resp.content;
          _this.setupProject.preProjectId = _this.preProject.id;
        } else {
          Toast.warning(resp.message,Swal);
        }
      })
    },

    getCbbListByCustomerId(customerId) {
      let _this = this;
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/cbb/getCbbListByCustomerId/' + customerId).then((response) => {
        let resp = response.data;
        if (resp.success) {
          _this.archivedCbbList = resp.content;
          if(Tool.isNotEmpty_ES5(_this.archivedCbbList)){
            _this.archivedCbbList.forEach((item) => {
              _this.cbbData.push({
                label: item.characterName,
                key: item.id,
              });
            });
          }
        } else {
          Toast.warning(resp.message, Swal);
        }
      });
    },

    getUploadFileList(resFiles){
      let _this = this;
      _this.resFiles = resFiles;
      if(Tool.isNotEmpty(resFiles)){
        _this.fileIdList = [];
        resFiles.forEach(item => {
          _this.fileIdList.push(item.id);
        })
      }

    },

    close(){
      let _this = this;
      _this.closeFunction();
    },
  }
};
</script>

<style>
.el-transfer-panel {
  border: 1px solid #EBEEF5;
  border-radius: 4px;
  overflow: hidden;
  background: #FFF;
  display: inline-block;
  vertical-align: middle;
  width: 300px !important;
  max-height: 100%;
  -webkit-box-sizing: border-box;
  box-sizing: border-box;
  position: relative;
}
</style>
<style lang="scss" scoped>
@import './../../../public/static/scss/caipd.scss';
.ca-form{
  font-size: 14px;
  margin-left: 30px;
}

</style>
