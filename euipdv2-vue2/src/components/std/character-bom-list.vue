<!--
 * Author: 周朔鹏
 * Date: 2024-01-14
 * Email: 391902958@qq.com
 * Website: https://www.euipd.com
-->

<template>
  <div style="height: 100%;">
    <div class="paper-height">
      <div style="height: 100%; overflow: scroll;">
        <div class="a4-paper-big-simple">
          <div style="margin: 50px 60px;">
            <div style="display: flex; justify-content: flex-end; margin-bottom: 8px;">
              <BomSnapshotSelect owner-type="CHARACTER" :owner-id="characterId" @change="onSnapshotChange"></BomSnapshotSelect>
            </div>
            <el-table
                :data="snapshotItems || bomMaterialList"
                stripe
                show-summary
                :summary-method="getSummaries"
                style="width: 100%">
              <el-table-column type="expand">
                <template slot-scope="props">
                  <div style="margin-left: 60px;">
                    <div class="ca-plain-form">
                      <div class="ca-form-line">
                        <div class="ca-form-label-xxl">
                          <div class="ca-form-label-title" style="font-size: 13px; color: #99a9bf;">
                            物料编码:
                          </div>
                        </div>
                        <div class="ca-form-content">
                          {{ props.row.bomCode }}
                        </div>
                      </div>
                      <div class="ca-form-line">
                        <div class="ca-form-label-xxl">
                          <div class="ca-form-label-title" style="font-size: 13px; color: #99a9bf;">
                            物料名称:
                          </div>
                        </div>
                        <div class="ca-form-content">
                          {{ props.row.name }}
                        </div>
                      </div>
                      <div class="ca-form-line">
                        <div class="ca-form-label-xxl">
                          <div class="ca-form-label-title" style="font-size: 13px; color: #99a9bf;">
                            物料型号:
                          </div>
                        </div>
                        <div class="ca-form-content">
                          {{ props.row.model }}
                        </div>
                      </div>
                      <div class="ca-form-line">
                        <div class="ca-form-label-xxl">
                          <div class="ca-form-label-title" style="font-size: 13px; color: #99a9bf;">
                            未税单价:
                          </div>
                        </div>
                        <div class="ca-form-content">
                          {{ priceText(props.row, props.row.preTaxUnitPrice) }}
                        </div>
                      </div>
                      <div class="ca-form-line">
                        <div class="ca-form-label-xxl">
                          <div class="ca-form-label-title" style="font-size: 13px; color: #99a9bf;">
                            使用数量:
                          </div>
                        </div>
                        <div class="ca-form-content">
                          {{ props.row.amount }}
                        </div>
                      </div>
                      <div class="ca-form-line">
                        <div class="ca-form-label-xxl">
                          <div class="ca-form-label-title" style="font-size: 13px; color: #99a9bf;">
                            计量单位:
                          </div>
                        </div>
                        <div class="ca-form-content">
                          {{ props.row.unit }}
                        </div>
                      </div>
                      <div class="ca-form-line">
                        <div class="ca-form-label-xxl">
                          <div class="ca-form-label-title" style="font-size: 13px; color: #99a9bf;">
                            未税小计:
                          </div>
                        </div>
                        <div class="ca-form-content">
                          {{ priceText(props.row, props.row.preTaxSumPrice) }}
                        </div>
                      </div>
                      <div class="ca-form-line">
                        <div class="ca-form-label-xxl">
                          <div class="ca-form-label-title" style="font-size: 13px; color: #99a9bf;">
                            供应厂商:
                          </div>
                        </div>
                        <div class="ca-form-content">
                          {{ props.row.supplierName }}
                        </div>
                      </div>
                    </div>
                  </div>
                </template>
              </el-table-column>
              <el-table-column
                  prop="itemType"
                  width="100"
                  :formatter="itemTypeFormat"
                  align="center"
                  label="类别">
              </el-table-column>
              <el-table-column
                  prop="bomCode"
                  sortable
                  show-overflow-tooltip
                  align="center"
                  label="物料编码">
              </el-table-column>
              <el-table-column
                  prop="name"
                  sortable
                  show-overflow-tooltip
                  align="center"
                  label="物料名称">
              </el-table-column>
              <el-table-column
                  prop="model"
                  sortable
                  show-overflow-tooltip
                  align="center"
                  label="物料型号">
              </el-table-column>
              <el-table-column
                  prop="unit"
                  width="100"
                  sortable
                  show-overflow-tooltip
                  align="center"
                  label="计量单位">
              </el-table-column>
              <el-table-column
                  prop="amount"
                  width="100"
                  sortable
                  show-overflow-tooltip
                  align="center"
                  label="使用数量">
              </el-table-column>
              <el-table-column
                  prop="preTaxSumPrice"
                  :formatter="sumPriceFormat"
                  width="100"
                  sortable
                  show-overflow-tooltip
                  align="center"
                  label="成本小计">
              </el-table-column>
            </el-table>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import Swal from "sweetalert2";
import {optionKV, rmbFormat} from "@/filter/filter";
import BomSnapshotSelect from "@/components/std/bom-snapshot-select.vue";

export default {
  components: {
    BomSnapshotSelect,
  },
  name: "character-bom-list",

  props: {
    characterId: {
      default: null,
    },
  },

  data() {
    return {
      loginUser: {},
      loginCustomer: {},
      bomMaterialList: [],
      multipleSelection: [],
      materialToBom: {},

      characterInfo: {},
      snapshotItems: null,
    };
  },

  computed: {},

  mounted: function () {
    let _this = this;
    _this.getLoginUser();
    if (Tool.isNotEmpty(_this.characterId)) {
      // _this.getCharacterSimpleInfo(_this.characterId);
      _this.getBomMaterialListByCharacterId(_this.characterId);
    }
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

    onSnapshotChange(items) {
      this.snapshotItems = items;
    },

    getCharacterSimpleInfo(characterId){
      let _this = this;
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/character/getCharacterSimpleInfo/'+ characterId).then((response)=>{
        let resp = response.data;
        if (resp.success) {
          _this.characterInfo = resp.content;
        } else {
          Toast.warning(resp.message, Swal);
        }
      })
    },

    unitPriceFormat(row, column) {
      if (row.preTaxUnitPrice) {
        return rmbFormat(row.preTaxUnitPrice);
      }
      return 0;
    },

    sumPriceFormat(row, column) {
      if (row.itemType === BOM_ITEM_TYPE.CBB.key) {
        return '';
      }
      if (row.preTaxSumPrice) {
        return rmbFormat(row.preTaxSumPrice);
      }
      return 0;
    },

    priceText(row, value) {
      return row.itemType === BOM_ITEM_TYPE.CBB.key ? '' : rmbFormat(value);
    },

    itemTypeFormat(row) {
      return row.itemType === BOM_ITEM_TYPE.CBB.key ? BOM_ITEM_TYPE.CBB.value : BOM_ITEM_TYPE.MATERIAL.value;
    },

    getBomMaterialListByCharacterId(characterId) {
      let _this = this;
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/material/getBomMaterialListByCharacterId/' + characterId).then((response) => {
        let resp = response.data;
        if (resp.success) {
          _this.bomMaterialList = resp.content;
        } else {
          Toast.warning(resp.message, Swal);
        }
      });
    },

    getSummaries(param) {
      const { columns, data } = param;
      const materials = data.filter(item => item.itemType !== BOM_ITEM_TYPE.CBB.key);
      const sums = [];
      columns.forEach((column, index) => {
        if (index === 1) {
          sums[index] = '总计';
          return;
        }
        if (column.property !== 'amount' && column.property !== 'preTaxSumPrice') {
          sums[index] = '';
          return;
        }
        const values = materials.map(item => Number(item[column.property])).filter(value => !isNaN(value));
        const total = values.reduce((prev, curr) => prev + curr, 0);
        sums[index] = column.property === 'preTaxSumPrice' ? rmbFormat(total) : total;
      });
      return sums;
    }
  },
}
</script>

<style lang="scss" scoped>
@import './../../../public/static/scss/caipd';

.paper-height {
  height: calc(100vh - 255px);
}

</style>
