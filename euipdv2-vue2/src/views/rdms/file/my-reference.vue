<!--
 * Author: 周朔鹏
 * Date: 2024-12-03
 * Email: 391902958@qq.com
 * Website: https://www.euipd.com
-->

<template>
  <div>
    <div style="display: flex; justify-content: flex-start; align-items: center">
      <div>
        <pagination key="file-reference" ref="pagination" v-bind:list="list" v-bind:itemCount="6"></pagination>
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
        <template slot-scope="scope">第 {{ scope.row.fileVersionNo || 1 }} 版</template>
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
          prop="downloadorName"
          width="100"
          sortable
          label="下载人">
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
          prop="downloadIp"
          show-overflow-tooltip
          label="下载IP"
          align="center"
          width="100">
      </el-table-column>
      <el-table-column
          prop="downloadTime"
          :formatter="downloadTimeFormat"
          show-overflow-tooltip
          label="下载时间"
          align="center"
          width="100">
      </el-table-column>
      <el-table-column
          fixed="right"
          label="操作"
          align="center"
          width="80">
        <template slot-scope="scope">
          <el-button v-if="scope.row.downloadorId === loginUser.customerUserId" type="primary" plain size="small" @click="downloadFile(scope.row)">
            下载
          </el-button>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>

<script>
import Pagination from "../../../components/pagination";
import {dateFormat, optionKV} from "@/filter/filter";
import { Loading } from 'element-ui'
import Swal from "sweetalert2";

export default {
  components: {
    Pagination
  },

  name: "my-reference",

  data: function () {
    return {
      loginUser: {},
      loginCustomer: {},
      keyWord: '',

      file: {},
      files: [],
      FILE_GROUPING: FILE_GROUPING,
    }
  },
  mounted: function () {
    let _this = this;
    _this.getLoginUser();
    _this.$refs.pagination.size = 24;
    _this.list(1);
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

    downloadTimeFormat(row, column){
      return dateFormat(row.downloadTime);
    },

    handleClick(tab, event) {
      console.log(tab, event);
    },

    fileGroupFormat(row) {
      return optionKV(FILE_GROUPING, row.use);
    },

    /**
     * 列表查询
     */
    list(page) {
      let _this = this;
      let loadingInstance = Loading.service({ fullscreen: true });
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/file/rdms/list-download', {
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
      if (Tool.isNotEmpty(_this.keyWord)
      ) {
        _this.$ajax.post(process.env.VUE_APP_SERVER + '/file/rdms/searchDownloadFileByKeyWord', {
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
