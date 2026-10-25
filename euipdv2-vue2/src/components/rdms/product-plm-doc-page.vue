<!--
 * Author: 周朔鹏
 * Date: 2024-05-22
 * Email: 391902958@qq.com
 * Website: https://www.euipd.com
-->

<template>
  <div class="paper-height">
    <div style="height: 100%; overflow: scroll;">
      <div class="a4-paper-big-simple">
        <div class="header-nav">
          <div>文件分类:</div>
          <div>
            <a v-on:click="onClickLevel1('00000000')" id="category-00000000" href="javascript:void(0);" class="cur">全部</a>
            <a href="javascript:void(0);" v-for="(o, index) in level1" :key="index" v-on:click="onClickLevel1(o.id)" v-bind:id="'category-' + o.id">{{ o.name }}</a>
          </div>
        </div>
        <div style="display: flex; justify-content: flex-end; margin-right: 20px;">
          <pagination ref="pagination" v-bind:list="list" v-bind:itemCount="6"></pagination>
        </div>
        <div style="margin: 18px 30px;">
          <el-table
              :data="categoryFileList"
              border
              stripe
              highlight-current-row
              style="width: 100%">
            <el-table-column
                prop="fileId"
                width="90"
                show-overflow-tooltip
                align="center"
                label="文件ID"
            ></el-table-column>
            <el-table-column
                prop="name"
                align="left"
                min-width="200"
                show-overflow-tooltip
                label="文件名称"
            ></el-table-column>
            <el-table-column
                prop="writerName"
                align="center"
                label="编写人"
                width="100"
            ></el-table-column>
            <el-table-column
                prop="fileCreateTime"
                :formatter="fileCreatTimeFormat"
                label="创建时间"
                align="center"
                width="110"
            ></el-table-column>
            <el-table-column
                prop="categoryNameListStr"
                label="分类"
                align="center"
                min-width="80"
            ></el-table-column>
            <el-table-column
                fixed="right"
                label="操作"
                align="center"
                width="150">
              <template slot-scope="scope">
                <el-button v-on:click.s.stop="addCategory(scope.row)" type="primary" plain size="small">
                  分类
                </el-button>
                <el-button v-on:click.s.stop="fileDownload(scope.row)" type="primary" plain size="small">
                  下载
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </div>
    </div>

    <!--设置文件分类-->
    <el-dialog
        width="29cm"
        append-to-body
        :visible.sync="categoryFileDlg"
        :before-close="handleClose"
        :close-on-click-modal=false>
      <div slot="title" style="font-size: 20px;">{{ dialogTitle }}</div>
      <div>
        <EditFileCategory
            :key="fileId"
            :customer-id="loginCustomer.id"
            :file-id="fileId"
            :close-dlg="closeDialog"
        ></EditFileCategory>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import Swal from "sweetalert2";
import $ from 'jquery'
import {shortDateFormat} from "@/filter/filter";
import Pagination from "@/components/pagination.vue";
import EditFileCategory from "@/components/rdms/edit-file-category.vue";

export default {
  components: {
    EditFileCategory,
    Pagination

  },
  name: "product-plm-doc-page",

  data: function () {
    return {
      loginUser: {},
      loginCustomer: {},
      fileInfo: {},

      categoryList: [],
      categoryFileList: [],
      level1: [],
      level2: [],
      level1Id: "",
      level2Id: "",

      categoryFileDlg: false,
      dialogTitle: '',
      fileId: '',
    }
  },

  props: {
    projectId: {
      default: null,
    },
    projectType: {
      default: null
    },
    entrance: {
      default: null,
    }
  },

  computed: {},

  mounted: function () {
    let _this = this;
    _this.$refs.pagination.size = 24;

    _this.getLoginUser();
    _this.list(1);

    _this.$bus.$on('refresh-product-plm-doc-page', (data) => {
      if(data){
        _this.list(1);
      }
    });
    _this.$bus.$on('refresh-product-plm-doc-page-category', (data) => {
      if(data){
        _this.getCategoryList_all(_this.loginCustomer.id);
      }
    });

  },

  beforeDestroy(){
    let _this = this;
    _this.$bus.$off('refresh-product-plm-doc-page');
    _this.$bus.$off('refresh-product-plm-doc-page-category');
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

      _this.getCategoryList_all(_this.loginCustomer.id);
    },

    list(page) {
      let _this = this;
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/category-file/list', {
        page: page,
        size: _this.$refs.pagination.size,
        keyWord: _this.projectId,
        actor: _this.level1Id,
      }).then((response) => {
        let resp = response.data;
        if (resp.success) {
          _this.categoryFileList = resp.content.list;
          _this.$refs.pagination.render(page, resp.content.total);
        } else {
          Toast.warning(resp.message, Swal);
        }
      })
    },

    getCategoryList_all(customerId) {
      let _this = this;
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/category/getCategoryList_all/' + customerId).then((response) => {
        let resp = response.data;
        if (resp.success) {
          _this.categoryList = resp.content;
          _this.onClickLevel1("00000000");
        } else {
          Toast.warning(resp.message, Swal);
        }
      })
    },

/*    getAllCategoryFilesByProjectId(projectId) {
      let _this = this;
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/category-file/getAllCategoryFilesByProjectId/' + projectId).then((response) => {
        let resp = response.data;
        if (resp.success) {
          _this.categoryFileList = resp.content;
        } else {
          Toast.warning(resp.message, Swal);
        }
      })
    },*/

    fileCreatTimeFormat(row, column){
      return shortDateFormat(row.fileCreateTime);
    },

    /**
     * 点击一级分类时
     * @param level1Id
     */
    onClickLevel1(level1Id) {
      let _this = this;
      _this.level1Id = level1Id;
      // 点击一级分类时，显示激活状态
      $("#category-" + level1Id).siblings("a").removeClass("cur");
      $("#category-" + level1Id).addClass("cur");

      if(level1Id === "00000000"){
        _this.level1 = [];
        for (let i = 0; i < _this.categoryList.length; i++) {
          let c = _this.categoryList[i];
          if (c.parent === '00000000') {
            _this.level1.push(c);
          } else {
            _this.level2.push(c);
          }
        }
      }else{
        _this.level2 = [];
        for (let i = 0; i < _this.categoryList.length; i++) {
          let c = _this.categoryList[i];
          if (c.parent === level1Id) {
            _this.level2.push(c);
          }
        }
      }

      //处理函数位置
      _this.list(1);
    },

    fileDownload(file) {
      let _this = this;
      _this.getFileSimpleInfo(file.fileId);
    },

    getFileSimpleInfo(fileId) {
      let _this = this;
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/file/rdms/getFileSimpleInfo/'+ fileId).then((response) => {
        let resp = response.data;
        if (resp.success) {
          _this.fileInfo = resp.content;
          let file = _this.fileInfo;
          _this.getSignedUrl(file.path, file.suffix);
        } else {
          Toast.warning(resp.message, Swal);
        }
      })
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
      });
    },

    addCategory(categoryFile) {
      let _this = this;
      _this.fileId = categoryFile.fileId
      _this.categoryFileDlg = true;
      _this.dialogTitle = "设置文件分类";
    },
    closeDialog() {
      let _this = this;
      _this.categoryFileDlg = false;
    },
    handleClose(done) {
      done();
    },
  }
}
</script>

<style lang="scss">
@import './../../../public/static/scss/caipd.scss';
.paper-height {
  height: calc(100vh - 240px);
}
/* 头部 一级分类 */
.header-nav {
  background: #fff;
  /*background-color: #d6e9c6;*/
  box-shadow: 0 8px 16px 0 rgba(28,31,33,.1);
  margin: 15px 0 5px 0;
  box-sizing: border-box;
  font-size: 15px;
  font-weight: 500;
  line-height: 50px;
  height: 45px;

  display: flex;
  flex-direction: row;
  justify-content: flex-start;
  align-items: center;
}
.header-nav>div {
  margin-left: 18px;
}
.header-nav a {
  float: left;
  color: #07111b;
  margin-right: 20px;
  position: relative;
}
.header-nav a:hover {
  color: rgba(2, 112, 255, 0.8);
}
.header-nav a.cur {
  color: rgba(2, 112, 255, 0.8);
}
.header-nav a.cur:before {
  display: block;
}
.header-nav a:before {
  display: none;
  content: ' ';
  position: absolute;
  bottom: 8px;
  background: rgba(255, 2, 2, 0.80);
  width: 16px;
  height: 3px;
  left: 50%;
  margin-left: -8px;
}
</style>
