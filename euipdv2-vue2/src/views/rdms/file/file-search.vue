<!--
 * Author: 周朔鹏
 * Date: 2025-03-01
 * Email: 391902958@qq.com
 * Website: https://www.euipd.com
-->

<template>
  <div>
    <div style="display: flex; justify-content: flex-start; align-items: center">
      <div>
        <pagination key="file-search" ref="pagination" v-bind:list="list" v-bind:itemCount="6"></pagination>
      </div>
      <div style="margin-left: 80px; margin-bottom: 8px;">
        <el-input size="mini" placeholder="请输入关键词" v-model="keyWord">
          <el-button slot="append" icon="el-icon-search" @click="search(1)"></el-button>
        </el-input>
      </div>
    </div>
    <el-table
        :data="files"
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
          prop="name"
          show-overflow-tooltip
          label="文件名称">
      </el-table-column>
      <el-table-column
          label="版本"
          align="center"
          width="80">
        <template slot-scope="scope">第 {{ scope.row.versionNo || 1 }} 版</template>
      </el-table-column>
      <el-table-column
          prop="projectName"
          width="180"
          show-overflow-tooltip
          label="X项目名称">
      </el-table-column>
      <el-table-column
          prop="operatorName"
          sortable
          width="100"
          label="编制人">
      </el-table-column>
      <el-table-column
          prop="size"
          width="80"
          label="文件大小">
      </el-table-column>
      <el-table-column
          prop="path"
          show-overflow-tooltip
          label="相对路径">
      </el-table-column>
      <el-table-column
          prop="use"
          :formatter="fileGroupFormat"
          label="使用分类"
          align="center"
          width="100">
      </el-table-column>
      <el-table-column
          prop="createTime"
          show-overflow-tooltip
          :formatter="creatTimeFormat"
          label="创建时间"
          align="center"
          width="100">
      </el-table-column>
      <el-table-column
          prop="applyStatus"
          :formatter="fileApplyStatusFormat"
          label="状态"
          align="center"
          width="80">
      </el-table-column>
      <el-table-column
          fixed="right"
          label="操作"
          align="center"
          width="80">
        <template slot-scope="scope">
          <el-button v-if="scope.row.applyStatus === FILE_APPLY_STATUS.NOTSET.key" type="primary" plain size="small" @click="applyFor(scope.row)">
            申请
          </el-button>
          <el-button v-if="!searchFlag &&  scope.row.applyStatus === FILE_APPLY_STATUS.REJECT.key" type="primary" plain size="small" @click="applyClose(scope.row)">
            清除
          </el-button>
          <el-button v-if="scope.row.applyStatus === FILE_APPLY_STATUS.APPROVED.key" type="primary" plain size="small" @click="downloadFile(scope.row)">
            下载
          </el-button>

        </template>
      </el-table-column>
    </el-table>

    <!--创建任务-->
    <el-dialog
        width="29cm"
        :visible.sync="showApplyDlg"
        :before-close="handleClose"
        :close-on-click-modal=false>
      <div slot="title" style="font-size: 20px;">{{ dialogTitle }}</div>
      <div v-if="fileItem && fileItem.id">
        <FileApplyDlg
            :key="fileItem.id"
            :file-item="fileItem"
            :close-function="closeDialog">
        </FileApplyDlg>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import Pagination from "../../../components/pagination";
import {dateFormat, optionKV} from "@/filter/filter";
import { Loading } from 'element-ui'
import Swal from "sweetalert2";
import FileApplyDlg from "@/components/rdms/file-apply-dlg.vue";

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
    Pagination,
    FileApplyDlg,
  },

  name: "file-search",

  data: function () {
    return {
      loginUser: {},
      loginCustomer: {},
      keyWord: '',

      showApplyDlg: false,
      dialogTitle: '',

      currentPage: 1,
      searchFlag: false,

      file: {},
      fileItem: {
        id: "",
      },
      files: [],
      FILE_GROUPING: FILE_GROUPING,
      FILE_APPLY_STATUS: FILE_APPLY_STATUS,
    }
  },
  mounted: function () {
    let _this = this;
    _this.getLoginUser();
    _this.$refs.pagination.size = 24;
    _this.list(1);

    _this.$bus.$on('refresh-file-search-list', (data)=>{
      if(data){
        _this.list(1);
      }
    });
  },

  beforeDestroy(){
    let _this = this;
    _this.$bus.$off('refresh-file-search-list');
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

    handleClick(tab, event) {
      console.log(tab, event);
    },

    fileGroupFormat(row) {
      return optionKV(FILE_GROUPING, row.use);
    },

    fileApplyStatusFormat(row) {
      return optionKV(FILE_APPLY_STATUS, row.applyStatus);
    },

    /**
     * 列表查询
     */
    list(page) {
      let _this = this;
      _this.searchFlag = false;
      _this.currentPage = page;
      let loadingInstance = Loading.service({ fullscreen: true });
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/file/rdms/listApplyFiles', {
        page: page,
        size: _this.$refs.pagination.size,
        customerId: _this.loginCustomer.id,
        actor: _this.loginUser.customerUserId,
      }).then((response) => {
        loadingInstance.close();
        let resp = response.data;
        _this.files = resp.content.list;
        _this.$refs.pagination.render(page, resp.content.total);
      })
    },

    /**
     * 列表查询
     */
    search(page) {
      let _this = this;
      _this.searchFlag = true;
      if (Tool.isNotEmpty(_this.keyWord)
      ) {
        _this.$ajax.post(process.env.VUE_APP_SERVER + '/file/rdms/searchMoreFileByKeyWord', {
          page: page,
          size: _this.$refs.pagination.size,
          customerId: _this.loginCustomer.id,
          actor: _this.loginUser.customerUserId,
          keyWord: _this.keyWord,
        }).then((response) => {
          let resp = response.data;
          _this.files = resp.content.list;
          _this.$refs.pagination.render(page, resp.content.total);
        })
      } else {
        _this.list(page);
      }
    },

    applyClose(applyFile) {
      let _this = this;
      Swal.fire({
        title: '清除',
        text: "将清除本条申请记录",
        showCancelButton: true,
        confirmButtonColor: '#409EFF',
        cancelButtonColor: '#909399',
        confirmButtonText: '确认!',
        cancelButtonText: '取消!'
      }).then((result) => {
        if (result.isConfirmed) {
          _this.$ajax.post(process.env.VUE_APP_SERVER + '/file/rdms/applyClose',{
            fileId: applyFile.id,
            proposerId: _this.loginUser.customerUserId,
            customerId: _this.loginCustomer.id
          }).then((response) => {
            let resp = response.data;
            if (resp.success) {
              Toast.success("成功清除!", Swal);
              _this.list(1);
            } else {
              Toast.warning(resp.message, Swal);
            }
          });
        }
      }).catch(_ => {
      });

    },

    applyFor(currentRow) {
      let _this = this;
      _this.fileItem = currentRow;
      _this.showApplyDlg = true;
      _this.dialogTitle = "文档申请";
    },
    closeDialog() {
      let _this = this;
      _this.showApplyDlg = false;
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
        _this.list(_this.currentPage);
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
