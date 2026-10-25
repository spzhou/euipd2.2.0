<!--
 * Author: 周朔鹏
 * Date: 2026-10-13
 * Email: 391902958@qq.com
 * Website: https://www.euipd.com
-->

<template>
  <div class="product-owner-picker">
    <el-radio-group v-model="mode" size="mini" @change="onModeChange">
      <el-radio-button label="select">选择已有产品</el-radio-button>
      <el-radio-button label="new">新建产品</el-radio-button>
    </el-radio-group>
    <div v-if="mode === 'select'" style="margin-top: 6px;">
      <el-select v-model="value.productId" filterable clearable placeholder="请选择所属产品" style="width: 100%;">
        <el-option
            v-for="item in products"
            :key="item.id"
            :label="productLabel(item)"
            :value="item.id">
        </el-option>
      </el-select>
    </div>
    <div v-else style="margin-top: 6px; display: flex; gap: 6px;">
      <el-input v-model="value.newProductName" placeholder="新产品名称" style="flex: 2;"></el-input>
      <el-input v-model="value.newProductCode" placeholder="产品代码（可不填）" style="flex: 1;"></el-input>
    </div>
  </div>
</template>

<script>
import Swal from "sweetalert2";

export default {
  name: "product-owner-picker",

  props: {
    value: {
      type: Object,
      required: true,
    },
  },

  data: function () {
    return {
      mode: 'select',
      products: [],
    };
  },

  created() {
    ['productId', 'newProductName', 'newProductCode'].forEach((key) => {
      if (!Object.prototype.hasOwnProperty.call(this.value, key)) {
        this.$set(this.value, key, '');
      }
    });
    if (!this.value.productId && this.value.newProductName) {
      this.mode = 'new';
    }
    this.listProducts();
  },

  methods: {
    listProducts() {
      let _this = this;
      _this.$ajax.get(process.env.VUE_APP_SERVER + '/customer/pre-project/product/list').then((response) => {
        let resp = response.data;
        if (resp.success) {
          _this.products = resp.content || [];
          if (_this.products.length === 0 && !_this.value.productId) {
            _this.mode = 'new';
          }
        } else {
          Toast.warning(resp.message, Swal);
        }
      });
    },

    productLabel(item) {
      return item.productCode ? item.productName + '（' + item.productCode + '）' : item.productName;
    },

    onModeChange(mode) {
      if (mode === 'select') {
        this.value.newProductName = '';
        this.value.newProductCode = '';
      } else {
        this.value.productId = '';
      }
    },

    /** 提交前校验：须选择产品或填写新产品名称。 */
    validate() {
      const ok = this.mode === 'select'
          ? !!this.value.productId
          : !!String(this.value.newProductName || '').trim();
      if (!ok) {
        Toast.warning('请选择所属产品或新建产品', Swal);
      }
      return ok;
    },
  },
};
</script>
