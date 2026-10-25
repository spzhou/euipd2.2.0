<!--
 * Author: 周朔鹏
 * Date: 2026-10-13
 * Email: 391902958@qq.com
 * Website: https://www.euipd.com
-->

<template>
  <span class="bom-snapshot-select">
    <span style="font-weight: normal; margin-right: 6px;">清单版本:</span>
    <el-select v-model="selectedId" size="mini" style="width: 230px;" @change="onSelect">
      <el-option value="" label="现行清单"></el-option>
      <el-option v-for="s in snapshots" :key="s.id" :value="s.id" :label="optionLabel(s)"></el-option>
    </el-select>
  </span>
</template>

<script>
import Swal from "sweetalert2";
import { dateFormat } from "@/filter/filter";

/**
 * 物料清单版本选择：列出特性或子项目清单的各版快照，选中某版时以 change(items, snapshot) 交出该版清单行，
 * 选“现行清单”时交出 change(null, null)。
 */
export default {
  name: "bom-snapshot-select",

  props: {
    ownerType: {
      type: String,
      default: 'CHARACTER',
    },
    ownerId: {
      default: null,
    },
  },

  data() {
    return {
      snapshots: [],
      selectedId: '',
    };
  },

  mounted() {
    this.reload();
  },

  watch: {
    ownerId() {
      this.selectedId = '';
      this.reload();
    },
  },

  methods: {
    reload() {
      let _this = this;
      if (!_this.ownerId) {
        _this.snapshots = [];
        return;
      }
      _this.$ajax.get(process.env.VUE_APP_SERVER + '/customer/material/bom-snapshot/list/' + _this.ownerType + '/' + _this.ownerId).then((response) => {
        let resp = response.data;
        if (resp.success) {
          _this.snapshots = (resp.content || []).slice().reverse();
        } else {
          Toast.warning(resp.message, Swal);
        }
      });
    },

    onSelect(id) {
      let _this = this;
      if (!id) {
        _this.$emit('change', null, null);
        return;
      }
      _this.$ajax.get(process.env.VUE_APP_SERVER + '/customer/material/bom-snapshot/get/' + id).then((response) => {
        let resp = response.data;
        if (resp.success) {
          _this.$emit('change', resp.content.items || [], resp.content);
        } else {
          Toast.warning(resp.message, Swal);
        }
      });
    },

    optionLabel(s) {
      return s.versionLabel + (s.locked ? '（锁定）' : '') + ' · ' + s.reasonName + ' · ' + (s.createTime ? dateFormat(s.createTime) : '');
    },
  },
};
</script>
