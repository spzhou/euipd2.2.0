<!--
 * Author: 周朔鹏
 * Date: 2025-02-13
 * Email: 391902958@qq.com
 * Website: https://www.euipd.com
-->

<template>
  <div>
    <div  class="dialog-container">
      <div class="ca-form">
        <el-form label-width="150px" size="small">
          <el-form-item
              label="项目名称:"
              required>
            <el-input style="width: 300px;"
                      v-model="setupProject.projectName"
            ></el-input>
          </el-form-item>
          <el-form-item
              label="主开发版本:">
            <div>
              {{ setupProject.devVersion }}
            </div>
          </el-form-item>
          <el-form-item
              required
              label="项目简介:">
            <el-input style="width: 783px;"
                      type="textarea"
                      :autosize="{ minRows: 5, maxRows: 15}"
                      placeholder="请输入项目简介"
                      v-model="setupProject.projectIntroduce"
            ></el-input>
          </el-form-item>
          <el-form-item
              required
              label="监管委员:">
            <el-select v-model="setupProject.supervise" filterable placeholder="请选择">
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
      <div>
        <div class="hr hr-8 dotted"></div>
        <div style="margin-left: 75px; font-size: 14px; font-weight: bold; margin-bottom: 15px;">特性清单:</div>
        <div style="display: flex; justify-content: center;">
          <div style="width: 22cm;">
            <el-table
                ref="characterTable"
                :data="characterList"
                border
                stripe
                style="width: 100%"
                highlight-current-row
                @selection-change="handleSelectionChange">
              <el-table-column
                  type="selection"
                  width="55">
              </el-table-column>
              <el-table-column   label="序号" align="center" width="50">
                <template slot-scope="scope">
                  {{scope.$index+1}}
                </template>
              </el-table-column>
              <el-table-column
                  prop="characterName"
                  show-overflow-tooltip
                  label="特性名称">
              </el-table-column>
              <el-table-column
                  prop="writerName"
                  label="撰写人"
                  width="120"
                  align="center">
              </el-table-column>
              <el-table-column
                  prop="iterationVersion"
                  :formatter="versionNumFormat"
                  label="特性版本"
                  align="center"
                  width="100">
              </el-table-column>
              <el-table-column
                  prop="createTimeStr"
                  :formatter="dateFormat"
                  label="撰写日期"
                  align="center"
                  width="100">
              </el-table-column>
            </el-table>
          </div>
        </div>
        <div class="hr hr-8 dotted"></div>
<!--        <div style="margin-left: 75px; font-size: 14px; font-weight: bold; margin-bottom: 15px;">集成特性清单:</div>
        <div style="display: flex; justify-content: center;">
          <el-transfer
              filterable
              filter-placeholder="请输入关键词"
              v-model="value"
              :data="data"
              :titles="titles">
          </el-transfer>
        </div>
        <div class="hr hr-12 dotted"></div>-->
<!--        <div style="margin: 15px 110px 20px 0">
          <FileUploadCore
              :input-id="('project-setup-application-' + (setupProject.id))"
              :key="('file-upload-' + (setupProject.id))"
              :x-project-id="setupProject.id"
              :project-type="PROJECT_TYPE.PROJECT.key"
              :item-id="setupProject.id"
              :file-group="FILE_GROUPING.PROJECT.key"
              :en-delete="true"
              v-on:uploadFileList="getUploadFileList"
          ></FileUploadCore>
        </div>-->
        <div style="margin-bottom: 30px;"></div>
        <div class="clearfix form-actions">
          <div style="display: flex; justify-content: center;">
            <button v-on:click="setupApplication()" class="ca-btn-primary" type="button">
              创建
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
import { iterationFormat } from "@/filter/filter";
import FileUploadCore from "@/components/std/file-upload-core.vue";
import VersionLabel from "@/shared/version-label";

export default {

  name: 'project-setup-application',
  components: {
    FileUploadCore
  },
  props:{
    preProject: {
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

      data: [],
      value: [],
      titles: ['可选 特性','已选 特性'],
      archivedCharacterList: [],

      workers: [],
      workerList: [],
      milestone: {},
      milestones: [],

      dialogFormVisible: false,
      formLabelWidth: '120px',
      supervises: [],
      productManagers: [],
      projectManagers: [],

      characterList: [],
      characterListLeft: [],
      // archivedProjects: [],
      setupProject: {},

      multipleSelection: [],
      // multipleSelectionProject: [],

      PROJECT_TYPE: PROJECT_TYPE,
      DOC_TYPE: DOC_TYPE,
      FILE_GROUPING: FILE_GROUPING,

      resFiles: [],
      fileIdList: [],
    };
  },

  mounted: function() {
    let _this =this;
    _this.getLoginUser();
    _this.setupProject = JSON.parse(JSON.stringify( _this.preProject )); //深拷贝
    //给预立项项目分配ID
    _this.setupProject.id = Uuid.shortUuid(8);

    _this.setupProject.preProjectId = _this.preProject.id;

    _this.listIpmtByCustomerId();
    _this.listProductManagerByCustomerId();
    _this.listProjectManagerByCustomerId();
    _this.listAllWorker();

    _this.listAllApprovedCharacters();
    // _this.getArchivedProjectList();
  },
  methods:{
    handleSelectionChange(val) {
      let _this = this;
      _this.multipleSelection = val;
    },

/*    projectSelectionChange(val) {
      let _this = this;
      _this.multipleSelectionProject = val;
    },*/

    dateFormat(row) {
      return row.createTimeStr.substring(0,10);
    },
    releaseTimeFormat(row) {
      return row.releaseTime.substring(0,10);
    },

    versionNumFormat(row) {
      return VersionLabel.featureVersion(this.setupProject.devVersion, row.iterationVersion)
          || iterationFormat(row.iterationVersion);
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
      _this.getArchivedCharacterListByCustomerId(_this.loginCustomer.id);
    },

    getArchivedCharacterListByCustomerId(customerId) {
      let _this = this;
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/character/getArchivedCharacterListByCustomerId/' + customerId).then((response) => {
        let resp = response.data;
        if (resp.success) {
          _this.archivedCharacterList = resp.content;
          _this.archivedCharacterList.forEach((item) => {
            _this.data.push({
              label: item.characterName,
              key: item.id,
            });
          });
        } else {
          Toast.warning(resp.message, Swal);
        }
      });
    },

    listAllWorker() {
      let _this = this;
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/worker/listAllWorkers/'+ _this.loginCustomer.id).then((response)=>{
        let resp = response.data;
        if (resp.success) {
          _this.workers = resp.content;
          _this.workers.forEach((worker) => {
            if(worker !== null){
              _this.workerList.push({
                label: worker.trueName,
                key: worker.id
              });
            }
          });
        } else {
          Toast.warning(resp.message,Swal);
        }
      })
    },

    listAllApprovedCharacters() {
      // debugger
      let _this = this;
      let formData = new window.FormData();
      formData.append('preProjectId', _this.preProject.id);
      formData.append('loginUserId', _this.loginUser.customerUserId);
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/character/listAllApprovedByPreProjectId', formData).then((response)=>{
        let resp = response.data;
        if (resp.success) {
          _this.characterList = resp.content;
        } else {
          Toast.warning(resp.message,Swal);
        }
      })
    },

/*    getArchivedProjectList() {
      let _this = this;
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/project/getArchivedProjectList', {
        customerId: _this.loginCustomer.id,
        customerUserId: _this.loginUser.customerUserId,
      }).then((response)=>{
        let resp = response.data;
        if (resp.success) {
          _this.archivedProjects = resp.content;
        } else {
          Toast.warning(resp.message,Swal);
        }
      })
    },*/

    /**
     * 申请立项
     */
    setupApplication() {
      let _this = this;
      _this.setupProject.moduleIdListStr = JSON.stringify(_this.value);
      _this.setupProject.characterList = _this.multipleSelection; //立项仅包含选中的部分
      // 保存校验
      if (1 !== 1
          || !Validator.require(_this.setupProject.projectName, "项目名称",Swal)
          || !Validator.require(_this.setupProject.projectIntroduce, "项目简介",Swal)
          || !Validator.require(_this.setupProject.supervise, "监管委员",Swal)
          || !Validator.require(_this.setupProject.characterList, "特性列表",Swal)

          || !Validator.length(_this.setupProject.projectName, "项目名称", 2, 50,Swal)
          || !Validator.length(_this.setupProject.projectIntroduce, "项目简介", 5, 500,Swal)
          || !Validator.length(_this.setupProject.supervise, "监管委员", 8, 8,Swal)
      ) {
        return;
      }

      function exeApplication() {
        if (Tool.isEmpty(_this.setupProject.characterList) || _this.setupProject.characterList.length === 0) {
          Swal.fire({
            /*title: '特性清单不能为空!',*/
            text: "特性清单不能为空!",
            icon: 'warning',
            showCancelButton: false,
            confirmButtonColor: '#409EFF',
            cancelButtonColor: '#909399',
            confirmButtonText: '关闭'
          }).then((result) => {
            if (result.isConfirmed) {
              // _this.close();
            }
          }).catch(_ => {
          });
        } else {
          _this.setupProject.customerId = _this.loginCustomer.id;
          _this.setupProject.loginUserId = _this.loginUser.customerUserId;
          _this.setupProject.productManagerId = _this.preProject.productManagerId;
          _this.setupProject.fileIdList = _this.fileIdList;

          _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/project/setup-application', _this.setupProject).then((response) => {
            let resp = response.data;
            if (resp.success) {
              Toast.success("保存成功", Swal);
              _this.close();
              _this.$bus.$emit('notify-setup-count-flag', true);
              _this.$bus.publish({ areas: ['project', 'character', 'badge'], reason: 'setup-application' });
              //处理立项过程中未选择的功能项
              _this.characterList.forEach(item => {
                let flag = false;
                _this.setupProject.characterList.forEach(character => {
                  if (item.id === character.id) {
                    flag = true;
                  }
                })
                if (!flag) {
                  _this.characterListLeft.push(item);
                }
              })
              _this.characterList = _this.characterListLeft;
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

    listProductManagerByCustomerId() {
      let _this = this;
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/project/listProductManagerByCustomerId/'+ _this.loginCustomer.id).then((response)=>{
        let resp = response.data;
        if (resp.success) {
          _this.productManagers = resp.content;
        } else {
          Toast.warning(resp.message,Swal);
        }
      })
    },

    listProjectManagerByCustomerId() {
      let _this = this;
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/project/listProjectManagerByCustomerId/'+ _this.loginCustomer.id).then((response)=>{
        let resp = response.data;
        if (resp.success) {
          _this.projectManagers = resp.content;
        } else {
          Toast.warning(resp.message,Swal);
        }
      })
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

<style lang="scss" scoped>
@import './../../../public/static/scss/caipd.scss';
.ca-form{
  font-size: 14px;
  margin-left: 30px;
}
</style>
