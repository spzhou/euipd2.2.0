<!--
 * Author: 周朔鹏
 * Date: 2024-12-03
 * Email: 391902958@qq.com
 * Website: https://www.euipd.com
-->

<template>
  <div>
<!--Tab 卡片-->
    <div class="row">
      <div class="col-md-12">
        <div class="tabbable">
          <ul class="nav nav-tabs" id="myTab">
            <li :class="{ active: userListIsActive}">
              <a data-toggle="tab" href="#home" @click="userListClick()">
                <i class="green ace-icon fa fa-home bigger-120"></i>
                员工列表 &nbsp;
              </a>
            </li>
            <li :class="{ active: userEditIsActive}">
              <a data-toggle="tab" href="#edit-user" @click="userEditClick()">
                添加员工 &nbsp;
              </a>
            </li>
            <li v-show="detailPageShow" :class="{ active: userDetailIsActive}">
              <a data-toggle="tab" href="#user-detail" @click="userDetailClick()">
                员工资料 &nbsp;
                <button class="ca-btn-close" v-on:click.stop="detailClos()">
                  <i class="fa fa-times" aria-hidden="true"></i>
                </button>
              </a>
            </li>
            <li v-show="modifyPageShow" :class="{ active: userModifyIsActive}">
              <a data-toggle="tab" href="#user-modify" @click="userModifyClick()">
                编辑开发者 &nbsp;
                <button class="ca-btn-close" v-on:click.stop="modifyClose()">
                  <i class="fa fa-times" aria-hidden="true"></i>
                </button>
              </a>
            </li>
          </ul>
<!--客户列表-->
          <div class="tab-content">
            <div id="home" class="tab-pane fade " :class="{active : userListIsActive, in : userListIsActive}">
              <div class="row">
                <div class="col-xs-12 col-sm-6 col-md-3">
                  <div class="input-group">
                    <input  v-model="keyWord" type="text" class="form-control" style="border-radius: 4px;" name="keywords" placeholder="关键词" />
                    <div class="input-group-btn">
                      <button v-on:click="searchList(1)" type="button" class="ca-btn-search">
                        <i class="ace-icon fa fa-search icon-on-right bigger-110"></i>
                      </button>
                    </div>
                  </div>
                </div>
              </div>
              <div class="hr hr-18 dotted hr-double"></div>
              <div>
                <div style="display: flex; flex-direction: row; justify-content: space-between; align-items: center">
                  <div>
                    <pagination ref="pagination" v-bind:list="searchList" v-bind:itemCount="6"></pagination>
                  </div>
                  <div>
                  </div>
                </div>

                <table id="simple-table" class="table  table-bordered table-hover">
                  <thead>
                  <tr>
                    <th>id</th>
                    <th>登录名(手机)</th>
                    <th>姓名</th>
                    <th>专业</th>
                    <th>学历</th>
                    <th>学位</th>
                    <th>注册时间</th>
                    <th style="width:310px;">操作</th>
                  </tr>
                  </thead>
                  <tbody>
                  <tr v-for="user in users">
                    <td>{{user.id}}</td>
                    <td>{{user.loginName}}</td>
                    <td>{{user.trueName}}</td>
                    <td>{{user.subject}}</td>
                    <td>{{user.educationBackground}}</td>
                    <td>{{user.degree}}</td>
                    <td>{{user.createTimeStr}}</td>
                    <td>
                      <div class="hidden-sm hidden-xs btn-group">
                        <button v-on:click="detail(user.id)" class="ca-btn-primary-sm">
                          <i class="fa fa-file-archive-o bigger-120"></i>详情
                        </button>
                        <button v-on:click="modify(user)" class="ca-btn-primary-sm">
                          <i class="ace-icon fa fa-pencil bigger-120"></i>编辑
                        </button>
                        <button v-on:click="editPassword(user)" class="ca-btn-primary-sm">
                          <i class="ace-icon fa fa-key bigger-120"></i>密码
                        </button>
                        <button v-on:click="del(user.id)" class="ca-btn-danger-sm">
                          <i class="ace-icon fa fa-trash-o bigger-120"></i>删除
                        </button>
                      </div>
                    </td>
                  </tr>
                  </tbody>
                </table>
              </div>
            </div>
<!--添加客户-->
            <div id="edit-user" class="tab-pane fade" :class="{active : userEditIsActive, in : userEditIsActive}">
              <p>添加员工: 您可以先通过员工手机号码进行查询,看看你要添加的员工是否已经存在.</p>
              <div class="hr hr-18 dotted hr-double"></div>
              <developer-create></developer-create>
            </div>

<!--客户详情-->
            <div id="user-detail" class="tab-pane fade" :class="{active : userDetailIsActive, in : userDetailIsActive}">
              <div>
                <span>详细信息: 显示员工的详细信息.</span>
              </div>
              <div class="hr hr-18 dotted hr-double"></div>
              <div>
                <customer-detail></customer-detail>
              </div>
            </div>
<!-- 修改客户-->
            <div id="user-modify" class="tab-pane fade" :class="{active : userModifyIsActive, in : userModifyIsActive}">
              <p>编辑开发者</p>
              <div class="hr hr-18 dotted hr-double"></div>
              <div>
                <developer-modify
                    v-bind:user-initial="user"
                >
                </developer-modify>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <div id="edit-password-modal" class="modal fade" tabindex="-1" role="dialog">
      <div class="modal-dialog" role="document">
        <div class="modal-content">
          <div class="modal-header">
            <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">&times;</span></button>
            <h4 class="modal-title">修改密码</h4>
          </div>
          <div class="modal-body">
            <form class="form-horizontal">
              <div class="form-group">
                <label class="control-label col-sm-2">密码</label>
                <div class="col-sm-10">
                  <input class="form-control" type="password" v-model="user.password" name="password">
                </div>
              </div>
            </form>
          </div>
          <div class="modal-footer">
            <button type="button" class="btn btn-white btn-default btn-round" data-dismiss="modal">
              <i class="ace-icon fa fa-times"></i>
              取消
            </button>
            <button type="button" class="btn btn-white btn-info btn-round" v-on:click="savePassword()">
              <i class="ace-icon fa fa-plus blue"></i>
              保存
            </button>
          </div>
        </div><!-- /.modal-content -->
      </div><!-- /.modal-dialog -->
    </div><!-- /.modal -->
  </div>
</template>

<script>
  import Pagination from "../../../components/pagination";
  import File from "../../../components/rdms/file";
  import DeveloperModify from "@/components/rdms/developer-modify";
  import DeveloperCreate from "@/components/rdms/developer-create";
  import Swal from "sweetalert2";

  export default {
    components: {
      Pagination,
      File,
      DeveloperModify,
      DeveloperCreate,
    },
    name: "developer",
    data: function() {
      return {
        keyWord: '',

        user: {},
        users: [],
        searchUsers:[],
        currentUserId: null,
        FILE_GROUPING: FILE_GROUPING,
        CUSTOMER_STATUS: CUSTOMER_STATUS,

        detailPageShow: false,
        modifyPageShow: false,
        selectCoursePageShow: false,

        userListIsActive: true,
        userEditIsActive: false,
        userQueryIsActive: false,
        userDetailIsActive: false,
        userModifyIsActive: false,

      }
    },
    mounted: function() {
      let _this = this;
      _this.$refs.pagination.size = 12;
      _this.searchList(1);
      // sidebar激活样式方法一
      // this.$parent.activeSidebar("system-user-sidebar");

    },
    methods: {
      /**
       * 查找是否有权限
       * @param id
       */
      hasResource(id) {
        return Tool.hasResource(id);
      },

      /**
       * 列表查询
       */
      searchList(page) {
        let _this = this;
        _this.$ajax.post(process.env.VUE_APP_SERVER + '/system/rdms/user/searchList', {
          page: page,
          size: _this.$refs.pagination.size,
          keyWord: _this.keyWord,
        }).then((response)=>{
          let resp = response.data;
          if (resp.success) {
            _this.users = resp.content.list;
            _this.$refs.pagination.render(page, resp.content.total);
          } else {
            Toast.warning(resp.message,Swal);
          }
        })
      },

      /**
       * 点击【删除】
       */
      del(id) {
        let _this = this;
        Swal.fire({
          title: '确认？',
          text: "删除后不可恢复，确认删除？",
          icon: 'warning',
          showCancelButton: true,
          confirmButtonColor: '#409EFF',
          cancelButtonColor: '#909399',
          confirmButtonText: '确认!',
          cancelButtonText: '取消!'
        }).then((result) => {
          if (result.value) {
            _this.$ajax.delete(process.env.VUE_APP_SERVER + '/system/rdms/user/del/' + id).then((response)=>{
              let resp = response.data;
              if (resp.success) {
                Toast.success("删除成功！", Swal);
                _this.searchList(1);
                _this.userListClick();
                this.detailPageShow = false;
              } else {
                Toast.warning(resp.message,Swal);
              }
            });
          }
        })
      },

      /**
       * 点击【重置密码】
       */
      editPassword(user) {
        let _this = this;
        _this.user = $.extend({}, user);
        _this.user.password = null;
        $("#edit-password-modal").modal("show");
      },

      /**
       * 点击【保存密码】
       */
      savePassword() {
        let _this = this;
        _this.$ajax.get(process.env.VUE_APP_SERVER + '/system/rdms/user/login-salt/' + _this.user.loginName).then((saltResp) => {
          let saltBody = saltResp.data || {};
          if (!saltBody.success || !saltBody.content) {
            Toast.warning(saltBody.message || "读取用户盐失败", Swal);
            return;
          }
          _this.user.password = hex_md5(_this.user.password + saltBody.content);
        _this.$ajax.post(process.env.VUE_APP_SERVER + '/system/rdms/user/save-password', _this.user).then((response)=>{
          let resp = response.data;
          if (resp.success) {
            $("#edit-password-modal").modal("hide");
            Toast.success("修改密码成功", Swal);
            _this.searchList(1);
          } else {
            Toast.warning(resp.message, Swal);
          }
        });
        });
      },

      detailClos(){
        let _this = this;
        _this.userListClick();
        this.detailPageShow = false;
      },
      modifyClose(){
        let _this = this;
        _this.userListClick();
        this.modifyPageShow = false;
      },
      selectCourseClose(){
        let _this = this;
        _this.userListClick();
        this.selectCoursePageShow = false;
      },

      detail(id){
        let _this = this;
        _this.currentUserId = id;
        _this.detailPageShow = true;
        _this.userDetailClick();
      },
      modify(user) {
        let _this = this;
        _this.user = $.extend({}, user);
        _this.modifyPageShow = true;
        _this.userModifyClick();
      },
      selectCourse() {
        let _this = this;
        _this.selectCoursePageShow = true;
        _this.selectCourseClick();
      },


      userListClick(){
        let _this = this;
        _this.userListIsActive=true;
        _this.userEditIsActive=false;
        _this.userDetailIsActive=false;
        _this.userModifyIsActive=false;
        _this.searchList(1);
      },
      userEditClick(){
        let _this = this;
        _this.userListIsActive=false;
        _this.userEditIsActive=true;
        _this.userDetailIsActive=false;
        _this.userModifyIsActive=false;
      },

      userDetailClick(){
        let _this = this;
        _this.userListIsActive=false;
        _this.userEditIsActive=false;
        _this.userDetailIsActive=true;
        _this.userModifyIsActive=false;
      },
      userModifyClick(){
        let _this = this;
        _this.userListIsActive=false;
        _this.userEditIsActive=false;
        _this.userDetailIsActive=false;
        _this.userModifyIsActive=true;
      },

    }
  }
</script>

<style lang="scss">
@import './../../../../public/static/scss/caipd.scss';

</style>
