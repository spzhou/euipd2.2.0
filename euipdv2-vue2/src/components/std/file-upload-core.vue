<!--
 * Author: 周朔鹏
 * Date: 2024-11-17
 * Email: 391902958@qq.com
 * Website: https://www.euipd.com
-->

<template>
  <div>
    <el-row>
      <slot></slot>
      <el-col :span="24">
        <el-form
            v-show="resFiles && resFiles.length > 0"
            class="file-upload-list-form"
            size="small"
            label-width="0">
          <el-row class="file-upload-list-header" type="flex" align="middle">
            <el-col :span="11" class="col-left">文件名</el-col>
            <el-col :span="4" class="col-center">上传日期</el-col>
            <el-col :span="4" class="col-center">上传者姓名</el-col>
            <el-col :span="5" class="col-center">操作</el-col>
          </el-row>
          <template v-for="(item, i) in resFiles">
          <el-row
              :key="item.id || i"
              class="file-upload-list-row"
              type="flex"
              align="middle">
            <el-col :span="11" class="col-left">
              <el-form-item>
                <el-button
                    v-if="item && item.authStatus"
                    type="text"
                    class="file-name-link"
                    :title="item.name"
                    @click="downloadFile(item)">
                  {{ item.name }}
                </el-button>
                <span v-else class="file-name" :title="item.name">{{ item.name }}</span>
                <el-button
                    v-if="(item.versionNo || 1) > 1"
                    type="text"
                    class="file-version"
                    title="展开历史版本"
                    @click="toggleVersions(item)">第 {{ item.versionNo }} 版</el-button>
                <span v-else class="file-version">第 1 版</span>
              </el-form-item>
            </el-col>
            <el-col :span="4" class="col-center">
              <el-form-item>
                {{ item.createTime | dateFormat }}
              </el-form-item>
            </el-col>
            <el-col :span="4" class="col-center">
              <el-form-item>
                {{ item.operatorName || '-' }}
              </el-form-item>
            </el-col>
            <el-col :span="5" class="col-center">
              <el-form-item>
                <el-button v-if="enable && item && item.authStatus && item.id" type="text" @click="selectNewVersion(item)">上传新版本</el-button>
                <el-button v-if="enDelete && item && item.authStatus" type="text" @click="deleteFile(item)">删除</el-button>
              </el-form-item>
            </el-col>
          </el-row>
          <div
              v-if="versionRows[item.id]"
              :key="'v-' + (item.id || i)"
              class="file-version-history">
            <div v-for="v in versionRows[item.id]" :key="v.id" class="file-version-history-row">
              <span class="file-version">第 {{ v.versionNo || 1 }} 版</span>
              <el-button
                  v-if="item.authStatus"
                  type="text"
                  class="file-name-link"
                  :title="v.name"
                  @click="downloadFile(v)">{{ v.name }}</el-button>
              <span v-else class="file-name" :title="v.name">{{ v.name }}</span>
              <span class="file-version-meta">{{ v.createTime | dateFormat }} {{ v.operatorName || '' }}</span>
            </div>
          </div>
          </template>
        </el-form>
        <div v-if="enable" style="display: none;">
          <FileRdms ref="versionUploader"
                    v-bind:input-id="('file-version-' + (inputId))"
                    v-bind:suffixs=G_SUFFIXS
                    v-bind:customer="loginCustomer.id"
                    v-bind:use="fileGroup"
                    v-bind:folder="xProjectId"
                    v-bind:after-upload="afterVersionUpload">
          </FileRdms>
        </div>
<!--Ftp 上传-->
        <div v-if="enable && false" style="text-align: right; margin-top: 10px;">
          <FtpRdms v-bind:input-id="('file-upload-' + (inputId))"
                     v-bind:text="'上传附件'"
                     v-bind:suffixs=G_SUFFIXS
                     v-bind:customer="loginCustomer.id"
                     v-bind:use="fileGroup"
                     v-bind:folder="xProjectId"
                     v-bind:after-upload="afterUpload">
          </FtpRdms>
        </div>
<!--Oss 上传-->
        <div v-if="enable && true" style="text-align: right; margin-top: 10px;">
          <FileRdms v-bind:input-id="('file-upload-' + (inputId))"
                     v-bind:text="'上传附件'"
                     v-bind:suffixs=G_SUFFIXS
                     v-bind:customer="loginCustomer.id"
                     v-bind:use="fileGroup"
                     v-bind:folder="xProjectId"
                     v-bind:after-upload="afterUpload">
          </FileRdms>
        </div>
      </el-col>
    </el-row>
  </div>
</template>

<script>
import FileRdms from "../../components/rdms/file-rdms";
import FtpRdms from "@/components/rdms/ftp-rdms.vue";
import Swal from "sweetalert2";
import FileSaver from 'file-saver';
import $ from "jquery";
import {Loading} from "element-ui";

export default {
  components: {
    FileRdms,
    FtpRdms,
    FileSaver,
  },
  name: "file-upload-core",

  props: {
    inputId: {
      default: null,
    },
    itemId: {
      default: null
    },
    xProjectId: {
      default: null
    },
    projectType: {
      default: null,
    },
    fileGroup: {
      default: FILE_GROUPING.DEFAULT.key,
    },
    enable:{  //上传按钮使能
      type: Boolean,
      default: true,
    },
    enDelete:{
      type: Boolean,
      default: false,
    },
    readDbRecord: {  //是否读取数据库已上传的记录
      type: Boolean,
      default: true,
    },
  },


  data() {
    return {
      loginUser: {},
      loginCustomer: {},

      resFile: {},
      resFiles: [],
      resFileIds: [],
      versionSource: null,
      versionRows: {},

      FILE_GROUPING: FILE_GROUPING,
      PROJECT_TYPE: PROJECT_TYPE,
      DOC_TYPE: DOC_TYPE,

      G_SUFFIXS: G_SUFFIXS,
    };
  },


  mounted: function () {
    let _this = this;
    _this.getLoginUser();

    if(Tool.isNotEmpty(_this.itemId)){
      if(_this.readDbRecord){
        _this.getUploadedFiles();  //由于一个预立项项目可能对应多个项目, 因此,有的时候不能读取数据库已经上传的文件列表
      }
    }
  },

  methods: {
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

    /** 按上传时间逆序（新的在前） */
    sortFilesByCreateTimeDesc(files) {
      return (files || []).slice().sort((a, b) => {
        const ta = a && a.createTime ? new Date(a.createTime).getTime() : 0;
        const tb = b && b.createTime ? new Date(b.createTime).getTime() : 0;
        return tb - ta;
      });
    },

    getUploadedFiles(){
      let _this = this;
      _this.resFiles = [];
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/file/rdms/getFileListByUseAndItemId', {
        use: _this.fileGroup,
        itemId: _this.itemId,
        operatorId: _this.loginUser.customerUserId,
      }).then((response) => {
        let resp = response.data;
        if (resp.success) {
          _this.resFiles = _this.sortFilesByCreateTimeDesc(resp.content || []);
          _this.$emit('uploadFileList', _this.resFiles);  //安装事件（含空列表，便于父组件同步删光）
        } else {
          Toast.warning(resp.message, Swal);
        }
      }).catch((e) => {
        console.error(e);
        Toast.error("附件列表加载失败", Swal);
      })
    },

    selectNewVersion(item) {
      this.versionSource = item;
      this.$refs.versionUploader.selectFile();
    },

    afterVersionUpload(resp) {
      let previous = this.versionSource;
      this.versionSource = null;
      if (previous) {
        this.afterUpload(resp, previous);
      }
    },

    toggleVersions(item) {
      let _this = this;
      if (_this.versionRows[item.id]) {
        _this.$delete(_this.versionRows, item.id);
        return;
      }
      _this.$ajax.get(process.env.VUE_APP_SERVER + '/file/rdms/versions/' + (item.docKey || item.id)).then((response) => {
        let resp = response.data;
        if (resp.success) {
          _this.$set(_this.versionRows, item.id, resp.content || []);
        } else {
          Toast.warning(resp.message, Swal);
        }
      });
    },

    afterUpload(resp, previous) {
      let _this = this;
      if (!resp || !resp.content) {
        Toast.warning("上传结果无效", Swal);
        return;
      }
      _this.resFile = resp.content;
      _this.resFile.customerId = _this.loginCustomer.id;
      _this.resFile.operatorId = _this.loginUser.customerUserId;
      _this.resFile.itemId = _this.itemId;  //对应file_grouping参数 可能是 Character Jobitem demand 等
      if(Tool.isNotEmpty(_this.projectType)){
        if(_this.projectType === PROJECT_TYPE.PRE_PROJECT.key){
          _this.resFile.preProjectId = _this.xProjectId;
        }
        if(_this.projectType === PROJECT_TYPE.PROJECT.key){
          _this.resFile.projectId = _this.xProjectId;
        }
        if(_this.projectType === PROJECT_TYPE.SUB_PROJECT.key || _this.projectType === PROJECT_TYPE.DEV_PROJECT.key){
          _this.resFile.subprojectId = _this.xProjectId;
        }
      }
      if (previous) {
        _this.resFile.previousFileId = previous.id;
      }
      _this.saveFile(_this.resFile, previous); // 将上传完成的数据保存到数据库
      _this.$forceUpdate();
    },

    saveFile(file, previous){
      let _this = this;
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/file/rdms/save', file).then((response)=>{
        let resp = response.data;
        if (resp.success) {
          let file = resp.content;
          if(! _this.resFiles){
            _this.resFiles = [];
          }
          file.authStatus = true;
          if (!file.operatorName) {
            file.operatorName = (_this.loginUser && (_this.loginUser.trueName || _this.loginUser.loginName)) || '-';
          }
          if (!file.createTime) {
            file.createTime = new Date();
          }
          if (previous) {
            _this.resFiles = _this.resFiles.filter(f => f.id !== previous.id);
            _this.resFileIds = _this.resFileIds.filter(id => id !== previous.id);
            _this.$delete(_this.versionRows, previous.id);
          }
          _this.resFiles.push(file);
          _this.resFiles = _this.sortFilesByCreateTimeDesc(_this.resFiles);
          _this.resFileIds.push(file.id);
          //向父组件发送数据  在父组件中使用 v-on监听
          this.$emit('uploadFileList', _this.resFiles);
        } else {
          Toast.warning(resp.message || "附件保存失败", Swal);
        }
      }).catch((e) => {
        console.error(e);
        Toast.error("附件保存失败，请重试", Swal);
      })
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

    deleteFile(file) {
      let _this = this;
      let text = `是否确认删除"${ file.name }"？`;
      if (document.activeElement && typeof document.activeElement.blur === 'function') {
        document.activeElement.blur();
      }
      Swal.fire({
        title: '确认？',
        text: text,
        icon: 'warning',
        showCancelButton: true,
        confirmButtonColor: '#409EFF',
        cancelButtonColor: '#909399',
        confirmButtonText: '确认!',
        cancelButtonText: '取消!',
        returnFocus: false,
        didOpen: () => {
          // Element Dialog 默认 z-index≈2000，Swal 默认≈1060，嵌套对话框内需抬高层级
          const container = Swal.getContainer();
          if (container) {
            container.style.zIndex = '10000';
          }
        }
      }).then((result) => {
        if (result.isConfirmed || result.value) {
          _this.$ajax.delete(process.env.VUE_APP_SERVER + '/file/rdms/del/'+ file.id).then((response)=>{
            let resp = response.data;
            if (resp.success) {
              Toast.success("删除成功!", Swal);
              _this.getUploadedFiles();
            } else {
              Toast.warning(resp.message, Swal);
            }
          })
        }
      })
    },
  },
}
</script>

<style lang="scss" scoped>
.file-upload-list-form {
  margin-bottom: 10px;
  width: 100%;

  .file-upload-list-header {
    padding: 0 8px 8px;
    color: #909399;
    font-size: 13px;
    border-bottom: 1px solid #ebeef5;
  }

  .file-upload-list-row {
    padding: 0 8px;
    border-bottom: 1px solid #f2f2f2;

    ::v-deep .el-form-item {
      margin-bottom: 0;
    }
  }

  .col-left {
    text-align: left;
  }

  .col-center {
    text-align: center;

    ::v-deep .el-form-item__content {
      text-align: center;
    }
  }

  .file-name,
  .file-name-link {
    display: inline-block;
    max-width: calc(100% - 64px);
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
    vertical-align: middle;
  }

  .file-name-link {
    padding: 0;
    font-size: 13px;
  }

  .file-version {
    margin-left: 6px;
    padding: 0;
    font-size: 12px;
    color: #909399;
  }

  .file-version-history {
    padding: 4px 8px 6px 24px;
    background: #fafafa;
    border-bottom: 1px solid #f2f2f2;
  }

  .file-version-history-row {
    line-height: 26px;
    font-size: 12px;

    .file-version {
      margin: 0 8px 0 0;
    }
  }

  .file-version-meta {
    margin-left: 8px;
    color: #909399;
  }
}
</style>
