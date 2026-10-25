<!--
 * Author: 周朔鹏
 * Date: 2024-01-11
 * Email: 391902958@qq.com
 * Website: https://www.euipd.com
-->

<!--todo 角色管理页面功能需要重新规划和开发-->
<template>
  <div>
    <div>
      <div style="display: flex; justify-content: space-between;">
        <pagination ref="pagination" v-bind:list="list" v-bind:itemCount="12"></pagination>
        <div>
          <el-button type="primary" v-on:click="add()">
            <i class="ace-icon fa fa-edit"></i>
            新建权限组
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
            <button v-on:click.stop="edit(scope.row)" class="ca-btn-primary-xsm"  style="width: 110px;">
              <i class="ace-icon fa fa-pencil bigger-120"></i>
              编辑组信息
            </button>
            <!--删除-->
            <el-button v-if="scope.row.id !== '00000000' && scope.row.id !== '00000001' && scope.row.id !== '00000002'" v-on:click="del(scope.row.id)" type="warning" size="mini">
              <i class="ace-icon fa fa-trash-o bigger-120"></i>
            </el-button>
            <el-button v-if="scope.row.id === '00000000' || scope.row.id === '00000001' || scope.row.id === '00000002'" disabled type="warning" size="mini">
              <i class="ace-icon fa fa-trash-o bigger-120"></i>
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      <div style="width: 100%; margin: 8px 0;">
        <small style="float: right;"><em><i class="el-icon-info"></i>操作说明: 请到团队管理页面, 为成员授权.</em></small>
      </div>
    </div>

<!-- 编辑角色信息-->
    <el-dialog
        title="新建/编辑角色信息"
        width="29cm"
        :visible.sync="editRoleDialog"
        :before-close="handleClose"
        :close-on-click-modal=false>
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
              <button type="button" v-on:click="save()" class="ca-btn-primary">保 存</button>
              <button type="button" v-on:click="close()" class="ca-btn-primary">关 闭</button>
            </div>
          </div>
        </div><!-- /.modal-content -->
      </div>
    </el-dialog>
  </div>
</template>

<script>
  import Pagination from "../../../components/pagination";
  import $ from 'jquery';
  import Swal from "sweetalert2";
  import CharacterDetailInfoCoreAll from "@/components/std/character-detail-info-core-all.vue";

  export default {
    components: {CharacterDetailInfoCoreAll, Pagination},

    name: "role",
    data: function() {
      return {
        role: {},
        roles: [],
        keyWord: '',

        editRoleDialog: false,
      }
    },
    mounted: function() {
      let _this = this;
      _this.$refs.pagination.size = 12;
      _this.list(1);

    },
    methods: {
      add() {
        let _this = this;
        _this.role = {};
        _this.editRoleDialog = true;
      },
      edit(role) {
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


      list(page) {
        let _this = this;
        _this.$ajax.post(process.env.VUE_APP_SERVER + '/system/rdms/role/customer/list', {
          page: page,
          size: _this.$refs.pagination.size,
        }).then((response)=>{
          let resp = response.data;
          _this.roles = resp.content.list;
          _this.$refs.pagination.render(page, resp.content.total);

        })
      },

      /**
       * 点击【保存】
       */
      save() {
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

        _this.$ajax.post(process.env.VUE_APP_SERVER + '/system/rdms/role/customer/save', _this.role).then((response)=>{
          let resp = response.data;
          if (resp.success) {
            _this.list(1);
            Toast.success("保存成功", Swal);
            _this.close();
          } else {
            Toast.warning(resp.message, Swal);
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
            _this.$ajax.delete(process.env.VUE_APP_SERVER + '/system/rdms/role/customer/delete/' + id).then((response)=>{
              let resp = response.data;
              if (resp.success) {
                _this.list(1);
                Swal.fire({
                  position: 'top-end',
                  icon: 'success',
                  title: "删除成功",
                  showConfirmButton: false,
                  timer: 1000
                })
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
