<!--
 * Author: 周朔鹏
 * Date: 2026-10-13
 * Email: 391902958@qq.com
 * Website: https://www.euipd.com
-->

<template>
  <div v-loading="loading">
    <el-table
        :data="products"
        border
        row-key="id"
        style="width: 100%"
        @expand-change="onExpand">
      <el-table-column type="expand">
        <template slot-scope="props">
          <div style="padding: 0 30px;">
            <el-table :data="releasesOf(props.row.id)" border size="mini" style="width: 100%" empty-text="尚无发布版本">
              <el-table-column label="发布版本" align="center" width="150">
                <template slot-scope="scope">{{ devVersionLabel(scope.row.devVersion) }}</template>
              </el-table-column>
              <el-table-column prop="projectName" label="来源项目" align="center" min-width="180" show-overflow-tooltip></el-table-column>
              <el-table-column label="发布时间" align="center" width="160">
                <template slot-scope="scope">{{ timeFormat(scope.row.releaseTime) }}</template>
              </el-table-column>
              <el-table-column prop="publisherName" label="发布人" align="center" width="100"></el-table-column>
              <el-table-column label="状态" align="center" width="90">
                <template slot-scope="scope">
                  <el-tag size="mini" :type="scope.row.status === 'EOL' ? 'info' : 'success'">{{ releaseStatusName(scope.row.status) }}</el-tag>
                </template>
              </el-table-column>
              <el-table-column label="停产时间" align="center" width="160">
                <template slot-scope="scope">{{ timeFormat(scope.row.eolTime) }}</template>
              </el-table-column>
              <el-table-column prop="releaseNote" label="发布说明" align="center" min-width="200" show-overflow-tooltip></el-table-column>
              <el-table-column label="操作" align="center" width="130">
                <template slot-scope="scope">
                  <el-button v-if="scope.row.baselineId" type="primary" plain size="mini"
                             @click.stop="showBaseline(scope.row)">基线</el-button>
                  <el-button v-if="scope.row.status === 'ACTIVE' && canManage(props.row)" type="warning" plain size="mini"
                             @click.stop="releaseEol(props.row, scope.row)">停产</el-button>
                </template>
              </el-table-column>
            </el-table>
          </div>
        </template>
      </el-table-column>
      <el-table-column label="序号" align="center" width="50">
        <template slot-scope="scope">{{ scope.$index + 1 }}</template>
      </el-table-column>
      <el-table-column prop="productName" label="产品名称" align="center" min-width="180" show-overflow-tooltip></el-table-column>
      <el-table-column prop="productCode" label="产品代码" align="center" width="150" show-overflow-tooltip></el-table-column>
      <el-table-column prop="productManagerName" label="产品经理" align="center" width="100"></el-table-column>
      <el-table-column prop="projectName" label="首个来源项目" align="center" min-width="160" show-overflow-tooltip></el-table-column>
      <el-table-column label="状态" align="center" width="90">
        <template slot-scope="scope">{{ productStatusName(scope.row.status) }}</template>
      </el-table-column>
      <el-table-column label="操作" align="center" width="110">
        <template slot-scope="scope">
          <el-button v-if="scope.row.status === 'ACTIVE' && canManage(scope.row)" type="danger" plain size="mini"
                     @click.stop="productEol(scope.row)">产品停产</el-button>
        </template>
      </el-table-column>
    </el-table>
    <div style="display: flex; justify-content: flex-end; margin-top: 6px;">
      <small>提示：项目完成后在项目列表“发布产品版本”，每次发布登记一个发布版本并生成发布基线；全部发布版本停产后产品为停产。</small>
    </div>

    <el-dialog :visible.sync="baselineVisible" title="发布基线" width="70%" :close-on-click-modal="false">
      <BaselineList v-if="baselineVisible" :key="baselineId" :baseline-id="baselineId"></BaselineList>
    </el-dialog>
  </div>
</template>

<script>
import Swal from "sweetalert2";
import { dateFormat } from "@/filter/filter";
import VersionLabel from "@/shared/version-label";
import BaselineList from "@/components/std/baseline-list.vue";

const PRODUCT_STATUS_NAME = { DRAFT: '未发布', ACTIVE: '在售', EOL: '停产' };
const RELEASE_STATUS_NAME = { ACTIVE: '已发布', EOL: '已停产' };

export default {
  name: "product-release-list",
  components: { BaselineList },

  data: function () {
    return {
      loading: false,
      products: [],
      releases: {},
      loginUser: {},
      baselineVisible: false,
      baselineId: '',
    };
  },

  mounted() {
    const login = Tool.getLoginUser();
    this.loginUser = login.userGroup === LOGIN_USER_GROUP.CUSTOMER.key ? login.adminUser : login;
    this.listProducts();
  },

  methods: {
    listProducts() {
      let _this = this;
      _this.loading = true;
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/product/list', { page: 1, size: 500 }).then((response) => {
        _this.loading = false;
        let resp = response.data;
        if (resp.success) {
          _this.products = (resp.content && resp.content.list) || [];
        } else {
          Toast.warning(resp.message, Swal);
        }
      }).catch(() => {
        _this.loading = false;
      });
    },

    onExpand(row, expanded) {
      if (Array.isArray(expanded) ? expanded.indexOf(row) >= 0 : expanded) {
        this.listReleases(row.id);
      }
    },

    listReleases(productId) {
      let _this = this;
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/product/release/list', { productId }).then((response) => {
        let resp = response.data;
        if (resp.success) {
          _this.$set(_this.releases, productId, resp.content || []);
        } else {
          Toast.warning(resp.message, Swal);
        }
      });
    },

    showBaseline(release) {
      this.baselineId = release.baselineId;
      this.baselineVisible = true;
    },

    releasesOf(productId) {
      return this.releases[productId] || [];
    },

    canManage(product) {
      const uid = this.loginUser && this.loginUser.customerUserId;
      const bossOrAdmin = typeof RoleAccess !== 'undefined'
          && RoleAccess.hasAny(['UltiMng'], Tool.getLoginUser().roleList);
      return bossOrAdmin || (!!uid && product.productManagerId === uid);
    },

    releaseEol(product, release) {
      let _this = this;
      Swal.fire({
        title: '确认',
        text: '发布版本 ' + _this.devVersionLabel(release.devVersion) + ' 停产？停产后不可恢复。',
        icon: 'question',
        showCancelButton: true,
        confirmButtonColor: '#409EFF',
        cancelButtonColor: '#909399',
        confirmButtonText: '确认',
        cancelButtonText: '取消'
      }).then((result) => {
        if (!result.value) {
          return;
        }
        _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/product/release/eol', { id: release.id }).then((response) => {
          let resp = response.data;
          if (resp.success) {
            Toast.success(resp.message || '已停产', Swal);
            _this.listReleases(product.id);
            _this.listProducts();
          } else {
            Toast.warning(resp.message, Swal);
          }
        });
      });
    },

    productEol(product) {
      let _this = this;
      Swal.fire({
        title: '确认',
        text: '产品“' + product.productName + '”停产？其全部发布版本一并停产，不可恢复。',
        icon: 'warning',
        showCancelButton: true,
        confirmButtonColor: '#409EFF',
        cancelButtonColor: '#909399',
        confirmButtonText: '确认',
        cancelButtonText: '取消'
      }).then((result) => {
        if (!result.value) {
          return;
        }
        _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/product/eol', { id: product.id }).then((response) => {
          let resp = response.data;
          if (resp.success) {
            Toast.success(resp.message || '已标记停产', Swal);
            _this.listProducts();
            _this.listReleases(product.id);
          } else {
            Toast.warning(resp.message, Swal);
          }
        });
      });
    },

    devVersionLabel(v) {
      return VersionLabel.devVersion(v) || '—';
    },

    productStatusName(status) {
      return PRODUCT_STATUS_NAME[status] || status;
    },

    releaseStatusName(status) {
      return RELEASE_STATUS_NAME[status] || status;
    },

    timeFormat(t) {
      return t ? dateFormat(t) : '';
    },
  },
};
</script>
