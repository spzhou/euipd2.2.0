<!--
 * Author: 周朔鹏
 * Date: 2025-03-01
 * Email: 391902958@qq.com
 * Website: https://www.euipd.com
-->

<template>
  <div>
    <el-table
        :data="fileApplyList"
        row-key="id"
        border
        default-expand-all
        stripe
        style="width: 100%;"
        highlight-current-row>
      <el-table-column
          prop="id"
          label="ID"
          align="center"
          width="100">
      </el-table-column>
      <el-table-column
          prop="fileName"
          align="center"
          show-overflow-tooltip
          label="文件名称">
      </el-table-column>
      <el-table-column
          label="版本"
          align="center"
          width="80">
        <template slot-scope="scope">第 {{ scope.row.fileVersionNo || 1 }} 版</template>
      </el-table-column>
      <el-table-column
          prop="proposerName"
          width="120"
          align="center"
          sortable
          label="申请人">
      </el-table-column>
      <el-table-column
          prop="useClass"
          :formatter="fileUseFormat"
          label="使用分类"
          align="center"
          width="100">
      </el-table-column>
      <el-table-column
          prop="type"
          :formatter="typeFormat"
          label="申请类型"
          align="center"
          width="100">
      </el-table-column>
      <el-table-column
          prop="fileCreateTime"
          show-overflow-tooltip
          :formatter="fileCreateTimeFormat"
          label="文件上传时间"
          align="center"
          width="200">
      </el-table-column>
      <el-table-column
          prop="createTime"
          show-overflow-tooltip
          :formatter="creatTimeFormat"
          label="申请时间"
          align="center"
          width="200">
      </el-table-column>
      <el-table-column
          fixed="right"
          label="操作"
          align="center"
          width="120">
        <template slot-scope="scope">
          <el-button type="primary" plain size="small" @click="approve(scope.row)">
            审 批
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <!--创建任务-->
    <el-dialog
        width="29cm"
        :visible.sync="showApproveDlg"
        :before-close="handleClose"
        :close-on-click-modal=false>
      <div slot="title" style="font-size: 20px;">{{ dialogTitle }}</div>
      <div v-if="fileItem && fileItem.id">
        <FileApproveDlg
            :key="fileItem.id"
            :file-apply-id="fileItem.id"
            :close-function="closeDialog">
        </FileApproveDlg>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import {dateFormat, optionKV} from "@/filter/filter";
import { Loading } from 'element-ui'
import FileApproveDlg from "@/components/rdms/file-approve-dlg.vue";

//与FILE_APPLICATION_STATUS相同 , 这里直接定义是因为, 未知原因导致 未定义错误
let FILE_APPLY_STATUS = {
  NOTSET:{key:"NOTSET", value:"可申请"},
  APPLICATION:{key:"APPLICATION", value:"申请中"},
  REJECT:{key:"REJECT", value:"拒绝"},
  APPROVED:{key:"APPROVED", value:"已批准"},
  COMPLETE:{key:"COMPLETE", value:"结束"},
  CANCEL:{key:"CANCEL", value:"取消"},
  DOWNLOADED:{key:"DOWNLOADED", value:"已下载"},
};

export default {
  components: {
    FileApproveDlg,
  },

  name: "file-apply-approve",

  data: function () {
    return {
      loginUser: {},
      loginCustomer: {},

      fileApplyList: [],

      showApproveDlg: false,
      dialogTitle: '',

      file: {},
      fileItem: {
        id: '',
      },
      files: [],

      FILE_GROUPING: FILE_GROUPING,
      FILE_APPLY_STATUS: FILE_APPLY_STATUS,
    }
  },
  mounted: function () {
    let _this = this;
    _this.getLoginUser();
    _this.list(1);

    _this.$bus.$on('refresh-file-apply-list', (data)=>{
      if(data){
        _this.list(1);
      }
    });
  },

  beforeDestroy(){
    let _this = this;
    _this.$bus.$off('refresh-file-apply-list');
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

    creatTimeFormat(row, column){
      return dateFormat(row.createTime);
    },

    fileCreateTimeFormat(row, column){
      return dateFormat(row.fileCreateTime);
    },

    handleClick(tab, event) {
      console.log(tab, event);
    },

    fileUseFormat(row) {
      return optionKV(FILE_GROUPING, row.useClass);
    },

    typeFormat(row) {
      return optionKV(FILE_APPLY_TYPE, row.type);
    },

    fileApplyStatusFormat(row) {
      return optionKV(FILE_APPLY_STATUS, row.applyStatus);
    },

    /**
     * 列表查询
     */
    list() {
      let _this = this;
      let loadingInstance = Loading.service({ fullscreen: true });
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/file/rdms/getApplyList/'+ _this.loginUser.customerUserId).then((response) => {
        loadingInstance.close();
        let resp = response.data;
        _this.fileApplyList = resp.content;
      })
    },

    approve(currentRow) {
      let _this = this;
      _this.fileItem = currentRow;
      _this.showApproveDlg = true;
      _this.dialogTitle = "文档审批";
    },
    closeDialog() {
      let _this = this;
      _this.showApproveDlg = false;
    },
    handleClose(done) {
      done(); //el-dialog用于关闭对话框的内置函数.
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

  }
}
</script>

<style>
.el-input--mini .el-input__inner {
  height: 28px;
  line-height: 28px;
  text-align: center !important;
  width: 222px  !important;
}
</style>
