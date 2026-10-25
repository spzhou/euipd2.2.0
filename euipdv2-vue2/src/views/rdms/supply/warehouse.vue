<!--
 * Author: 周朔鹏
 * Date: 2026-10-09
 * Email: 391902958@qq.com
 * Website: https://www.euipd.com
-->

<template>
  <div>
    <div class="row">
      <div class="col-md-12">
        <div class="tabbable">
          <ul class="nav nav-tabs">
            <li class="active">
              <a>
                <i class="green ace-icon fa fa-cubes bigger-120"></i>
                仓库管理
              </a>
            </li>
          </ul>

          <div class="tab-content">
            <div class="tab-pane fade active in">
              <div class="warehouse-head">
                <p>研发物料仓库由<strong>物料管理员</strong>维护。仓库编码建立后不可修改；仓库仍有库存时不能停用。默认仓库为“研发物料仓（LOCAL）”。</p>
                <el-button type="success" size="small" @click="openEdit(null)">
                  <i class="ace-icon fa fa-plus"></i> 新增仓库
                </el-button>
              </div>
              <div class="hr hr-10 dotted hr-double"></div>
              <el-table v-loading="loading" :data="warehouseList" border style="width: 100%">
                <el-table-column label="序号" align="center" width="50">
                  <template slot-scope="scope">{{ scope.$index + 1 }}</template>
                </el-table-column>
                <el-table-column prop="code" label="仓库编码" align="center" min-width="110" show-overflow-tooltip></el-table-column>
                <el-table-column prop="name" label="仓库名称" align="center" min-width="120" show-overflow-tooltip></el-table-column>
                <el-table-column prop="location" label="存放地点" align="center" min-width="100" show-overflow-tooltip></el-table-column>
                <el-table-column prop="remark" label="说明" align="center" min-width="100" show-overflow-tooltip></el-table-column>
                <el-table-column label="状态" align="center" width="80">
                  <template slot-scope="scope">
                    <el-tag :type="scope.row.status === 'ACTIVE' ? 'success' : 'info'" size="small">
                      {{ scope.row.status === 'ACTIVE' ? '启用' : '停用' }}
                    </el-tag>
                  </template>
                </el-table-column>
                <el-table-column label="操作" align="center" width="150">
                  <template slot-scope="scope">
                    <el-button type="primary" plain size="small" @click="openEdit(scope.row)">编辑</el-button>
                    <el-button v-if="scope.row.status === 'ACTIVE'" type="warning" plain size="small"
                               @click="changeStatus(scope.row, 'INACTIVE')">停用</el-button>
                    <el-button v-else type="success" plain size="small"
                               @click="changeStatus(scope.row, 'ACTIVE')">启用</el-button>
                  </template>
                </el-table-column>
              </el-table>
            </div>
          </div>
        </div>
      </div>
    </div>

    <el-dialog :title="form.id ? '编辑仓库' : '新增仓库'" width="560px" :visible.sync="editVisible"
               :close-on-click-modal="false">
      <el-form label-width="100px" :model="form" @submit.native.prevent>
        <el-form-item label="仓库编码" required>
          <el-input v-model="form.code" :disabled="!!form.id" maxlength="50"
                    placeholder="2～50 位字母、数字、短横线或下划线"></el-input>
        </el-form-item>
        <el-form-item label="仓库名称" required>
          <el-input v-model="form.name" maxlength="50"></el-input>
        </el-form-item>
        <el-form-item label="存放地点">
          <el-input v-model="form.location" maxlength="100"></el-input>
        </el-form-item>
        <el-form-item label="说明">
          <el-input type="textarea" rows="3" v-model="form.remark" maxlength="200" show-word-limit></el-input>
        </el-form-item>
      </el-form>
      <div slot="footer">
        <el-button @click="editVisible = false">取 消</el-button>
        <el-button type="primary" :loading="saving" @click="save">保 存</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import Swal from "sweetalert2";

export default {
  name: "warehouse",

  data() {
    return {
      loginCustomer: {},
      warehouseList: [],
      loading: false,
      editVisible: false,
      saving: false,
      form: {},
    };
  },

  mounted() {
    let login = Tool.getLoginUser();
    this.loginCustomer = login.userGroup === LOGIN_USER_GROUP.CUSTOMER.key ? login : login.customer;
    this.load();
  },

  methods: {
    url(path) {
      return process.env.VUE_APP_SERVER + '/customer/material-warehouse' + path;
    },

    load() {
      let _this = this;
      _this.loading = true;
      _this.$ajax.post(_this.url('/list/' + _this.loginCustomer.id)).then((response) => {
        let resp = response.data;
        if (resp.success) {
          _this.warehouseList = resp.content || [];
        } else {
          Toast.warning(resp.message, Swal);
        }
      }).finally(() => {
        _this.loading = false;
      });
    },

    openEdit(row) {
      this.form = row ? Object.assign({}, row) : { code: '', name: '', location: '', remark: '' };
      this.editVisible = true;
    },

    save() {
      let _this = this;
      if (!_this.form.id && !/^[A-Za-z0-9_-]{2,50}$/.test((_this.form.code || '').trim())) {
        Toast.warning("仓库编码为 2～50 位字母、数字、短横线或下划线", Swal);
        return;
      }
      let name = (_this.form.name || '').trim();
      if (name.length < 2 || name.length > 50) {
        Toast.warning("仓库名称须为 2～50 字", Swal);
        return;
      }
      _this.saving = true;
      let payload = Object.assign({}, _this.form, { customerId: _this.loginCustomer.id, name: name });
      _this.$ajax.post(_this.url('/save'), payload).then((response) => {
        let resp = response.data;
        if (resp.success) {
          Toast.success("保存成功", Swal);
          _this.editVisible = false;
          _this.load();
        } else {
          Toast.warning(resp.message, Swal);
        }
      }).finally(() => {
        _this.saving = false;
      });
    },

    changeStatus(row, status) {
      let _this = this;
      let action = status === 'ACTIVE' ? '启用' : '停用';
      Swal.fire({
        title: '确认',
        text: '是否' + action + '仓库“' + row.name + '”？',
        icon: 'question',
        showCancelButton: true,
        confirmButtonColor: '#409EFF',
        cancelButtonColor: '#909399',
        confirmButtonText: '确认',
        cancelButtonText: '取消'
      }).then((result) => {
        if (!result.isConfirmed) {
          return;
        }
        _this.$ajax.post(_this.url('/update-status'), {
          id: row.id, status: status, customerId: _this.loginCustomer.id
        }).then((response) => {
          let resp = response.data;
          if (resp.success) {
            Toast.success("已" + action, Swal);
            _this.load();
          } else {
            Toast.warning(resp.message, Swal);
          }
        });
      });
    },
  }
}
</script>

<style scoped>
.warehouse-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.warehouse-head p {
  margin: 0 0 0 5px;
}
</style>
