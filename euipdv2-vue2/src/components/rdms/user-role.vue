<!--
 * Author: 周朔鹏
 * Date: 2024-03-25
 * Email: 391902958@qq.com
 * Website: https://www.euipd.com
-->

<template>
  <div>
    <div style="display: flex; justify-content: space-between;">
      <pagination ref="pagination" v-bind:list="list" v-bind:itemCount="12"></pagination>
      <div>
        <el-button type="primary" v-on:click="addRole()">
          <i class="ace-icon fa fa-edit"></i>
          新增权限组
        </el-button>
      </div>
    </div>
    <el-table
        :data="roles"
        border
        stripe
        highlight-current-row
        style="width: 100%">
      <el-table-column  label="序号" align="center" width="100">
        <template slot-scope="scope">
          {{scope.$index+1}}
        </template>
      </el-table-column>
      <el-table-column
          prop="id"
          align="center"
          label="权限组ID"
          width="120"
      ></el-table-column>
      <el-table-column
          prop="name"
          align="center"
          label="权限组名称"
      ></el-table-column>
      <el-table-column
          prop="desc"
          align="center"
          label="权限描述"
      ></el-table-column>
      <el-table-column
          fixed="right"
          label="操作"
          align="center"
          width="300">
        <template slot-scope="scope">
          <el-button v-on:click="roleNameEdit(scope.row)" type="primary" size="mini">
            编辑组信息
          </el-button>
          <el-button v-if="scope.row.id !== '00000000' && scope.row.id !== '00000001' && scope.row.id !== '00000002'" v-on:click="del(scope.row.id)" type="warning" size="mini">
            <i class="ace-icon fa fa-trash-o bigger-120"></i>
          </el-button>
          <el-button v-if="scope.row.id === '00000000' || scope.row.id === '00000001' || scope.row.id === '00000002'" disabled type="warning" size="mini">
            <i class="ace-icon fa fa-trash-o bigger-120"></i>
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 编辑/添加角色-->
    <el-dialog
        title="添加/编辑角色信息"
        width="29cm"
        :visible.sync="editRoleDialog"
        :before-close="handleClose"
        :close-on-click-modal=true>
      <div>
        <div>
          <div class="modal-body">
            <form class="form-horizontal">
              <div class="form-group">
                <label class="col-sm-2 control-label">角色</label>
                <div class="col-sm-8">
                  <input v-model="role.name" class="form-control">
                </div>
              </div>
              <div class="form-group">
                <label class="col-sm-2 control-label">描述</label>
                <div class="col-sm-8">
                  <input v-model="role.desc" class="form-control">
                </div>
              </div>
            </form>
          </div>
          <div class="clearfix form-actions">
            <div style="display: flex; justify-content: center;">
              <button type="button" v-on:click="createRole()" class="ca-btn-primary">保 存</button>
              <button type="button" v-on:click="close()" class="ca-btn-primary">取 消</button>
            </div>
          </div>
        </div><!-- /.modal-content -->
      </div>
    </el-dialog>

  </div>
</template>

<script>
  import Pagination from "../../components/pagination";
  import Swal from "sweetalert2";
  import $ from "jquery";

  export default {
    components: {
      Pagination,
    },
    name: "user-role",
    data: function() {
      return {
        loginUser: {},
        loginCustomer: {},

        role: {},
        roles: [],
        keyWord: '',

        editRoleDialog: false,
      }
    },
    mounted: function() {
      let _this = this;
      _this.getLoginUser();
      _this.$refs.pagination.size = 12;
      _this.list(1);

    },
    methods: {
      addRole() {
        let _this = this;
        _this.role = {};
        _this.editRoleDialog = true;
      },

      roleNameEdit(role) {
        let _this = this;
        _this.role = $.extend({}, role);
        _this.editRoleDialog = true;
      },

      handleClose(done) {
        let _this = this;
        _this.editRoleDialog = false;
        done()
      },
      close() {
        let _this = this;
        _this.editRoleDialog = false;
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


      /**
       * 列表查询
       */
      list(page) {
        let _this = this;
        _this.$ajax.post(process.env.VUE_APP_SERVER + '/system/rdms/role/user/list', {
          page: page,
          size: _this.$refs.pagination.size,
          keyWord: _this.loginCustomer.id,
        }).then((response)=>{
          let resp = response.data;
          _this.roles = resp.content.list;
          _this.$refs.pagination.render(page, resp.content.total);
        })
      },

      /**
       * 点击【保存】
       */
      createRole() {
        let _this = this;

        // 保存校验
        if (1 !== 1
          || !Validator.require(_this.role.name, "角色",Swal)
          || !Validator.length(_this.role.name, "角色", 1, 50,Swal)
          || !Validator.require(_this.role.desc, "描述",Swal)
          || !Validator.length(_this.role.desc, "描述", 1, 100,Swal)
        ) {
          return;
        }
        _this.role.customerId = _this.loginCustomer.id;
        _this.$ajax.post(process.env.VUE_APP_SERVER + '/system/rdms/role/user/save', _this.role).then((response)=>{
          let resp = response.data;
          if (resp.success) {
            _this.list(1);
            Toast.success("保存成功", Swal);
            _this.editRoleDialog = false;
          } else {
            Toast.warning(resp.message, Swal);
            _this.editRoleDialog = false;
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
          text: "删除角色后不可恢复，确认删除？",
          icon: 'warning',
          showCancelButton: true,
          confirmButtonColor: '#409EFF',
          cancelButtonColor: '#909399',
          confirmButtonText: '确认!',
          cancelButtonText: '取消!'
        }).then((result) => {
          if (result.value) {
            _this.$ajax.delete(process.env.VUE_APP_SERVER + '/system/rdms/role/user/delete/' + id).then((response)=>{
              let resp = response.data;
              if (resp.success) {
                _this.list(1);
                Toast.success("删除成功", Swal);
              }
            });
          }
        })
      },

    }
  }
</script>

<style lang="scss">

</style>
