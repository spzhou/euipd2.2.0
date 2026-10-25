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
            <li :class="{active: activeTab === 'stock'}">
              <a @click="switchTab('stock')">
                <i class="green ace-icon fa fa-archive bigger-120"></i>
                库存
              </a>
            </li>
            <li :class="{active: activeTab === 'log'}">
              <a @click="switchTab('log')">
                <i class="blue ace-icon fa fa-list bigger-120"></i>
                库存台账
              </a>
            </li>
          </ul>

          <div class="tab-content">
            <div v-show="activeTab === 'stock'" class="tab-pane fade active in">
              <p class="inventory-tip">
                研发仓库物料由<strong>物料管理员</strong>录入或按模板导入，入库单价按移动加权平均计算；<!--
                -->研发人员领料时取库存单价，审批不改价。盘点差异用“调整”登记，调整只记台账不记账。
              </p>
              <div class="inventory-toolbar">
                <el-form :inline="true" size="small" @submit.native.prevent>
                  <el-form-item label="仓库">
                    <el-select v-model="stockFilter.warehouseCode" clearable placeholder="全部" style="width: 180px;">
                      <el-option v-for="w in warehouseList" :key="w.code" :value="w.code"
                                 :label="w.name + '（' + w.code + '）'"></el-option>
                    </el-select>
                  </el-form-item>
                  <el-form-item label="关键字">
                    <el-input v-model="stockFilter.keyword" clearable placeholder="物料编码、名称或规格型号"
                              style="width: 220px;"></el-input>
                  </el-form-item>
                  <el-form-item>
                    <el-checkbox v-model="stockFilter.positiveOnly">只看有库存</el-checkbox>
                  </el-form-item>
                </el-form>
                <div>
                  <el-button type="success" size="small" @click="openInbound">
                    <i class="ace-icon fa fa-plus"></i> 入库录入
                  </el-button>
                  <el-button type="primary" size="small" @click="openImport">
                    <i class="ace-icon fa fa-upload"></i> Excel 导入
                  </el-button>
                  <el-button size="small" @click="downloadTemplate">
                    <i class="ace-icon fa fa-download"></i> 下载模板
                  </el-button>
                  <el-button size="small" @click="exportStock">
                    <i class="ace-icon fa fa-file-excel-o"></i> 导出
                  </el-button>
                </div>
              </div>
              <el-table v-loading="stockLoading" :data="pagedStock" border style="width: 100%">
                <el-table-column label="序号" align="center" width="60">
                  <template slot-scope="scope">{{ (stockPage - 1) * stockPageSize + scope.$index + 1 }}</template>
                </el-table-column>
                <el-table-column label="仓库" align="center" width="160" show-overflow-tooltip>
                  <template slot-scope="scope">{{ scope.row.warehouseName || scope.row.warehouseCode }}</template>
                </el-table-column>
                <el-table-column prop="bomCode" label="物料编码" align="center" width="200" show-overflow-tooltip></el-table-column>
                <el-table-column prop="name" label="物料名称" align="center" show-overflow-tooltip></el-table-column>
                <el-table-column prop="model" label="规格型号" align="center" show-overflow-tooltip></el-table-column>
                <el-table-column prop="unit" label="单位" align="center" width="70"></el-table-column>
                <el-table-column prop="quantity" label="库存数量" align="center" width="100"></el-table-column>
                <el-table-column label="未税单价(元)" align="center" width="120">
                  <template slot-scope="scope">{{ priceText(scope.row.unitPrice) }}</template>
                </el-table-column>
                <el-table-column label="库存金额(元)" align="center" width="130">
                  <template slot-scope="scope">{{ amountText(scope.row) }}</template>
                </el-table-column>
                <el-table-column label="更新时间" align="center" width="160">
                  <template slot-scope="scope">{{ timeText(scope.row.updateTime) }}</template>
                </el-table-column>
                <el-table-column label="操作" align="center" width="160" fixed="right">
                  <template slot-scope="scope">
                    <el-button type="warning" plain size="small" @click="openAdjust(scope.row)">调整</el-button>
                    <el-button plain size="small" @click="showLogsOf(scope.row)">台账</el-button>
                  </template>
                </el-table-column>
              </el-table>
              <div class="inventory-pager">
                <el-pagination background layout="total, prev, pager, next, sizes"
                               :total="filteredStock.length" :page-size="stockPageSize"
                               :current-page.sync="stockPage" :page-sizes="[20, 50, 100]"
                               @size-change="size => { stockPageSize = size; stockPage = 1; }"/>
              </div>
            </div>

            <div v-show="activeTab === 'log'" class="tab-pane fade active in">
              <div class="inventory-toolbar">
                <el-form :inline="true" size="small" @submit.native.prevent>
                  <el-form-item label="仓库">
                    <el-select v-model="logQuery.warehouseCode" clearable placeholder="全部" style="width: 160px;">
                      <el-option v-for="w in warehouseList" :key="w.code" :value="w.code"
                                 :label="w.name + '（' + w.code + '）'"></el-option>
                    </el-select>
                  </el-form-item>
                  <el-form-item label="物料编码">
                    <el-input v-model="logQuery.bomCode" clearable style="width: 180px;"></el-input>
                  </el-form-item>
                  <el-form-item label="类型">
                    <el-select v-model="logQuery.operation" clearable placeholder="全部" style="width: 120px;">
                      <el-option v-for="(label, key) in OPERATIONS" :key="key" :value="key" :label="label"></el-option>
                    </el-select>
                  </el-form-item>
                  <el-form-item label="日期">
                    <el-date-picker v-model="logDates" type="daterange" value-format="yyyy-MM-dd"
                                    start-placeholder="开始" end-placeholder="结束" style="width: 240px;"></el-date-picker>
                  </el-form-item>
                  <el-form-item>
                    <el-button type="primary" @click="loadLogs">查询</el-button>
                  </el-form-item>
                </el-form>
                <div class="inventory-tip-small">最多显示最近 2000 条</div>
              </div>
              <el-table v-loading="logLoading" :data="logList" border style="width: 100%">
                <el-table-column label="时间" align="center" width="160">
                  <template slot-scope="scope">{{ timeText(scope.row.createTime) }}</template>
                </el-table-column>
                <el-table-column label="类型" align="center" width="100">
                  <template slot-scope="scope">{{ OPERATIONS[scope.row.operation] || scope.row.operation }}</template>
                </el-table-column>
                <el-table-column label="仓库" align="center" width="140" show-overflow-tooltip>
                  <template slot-scope="scope">{{ scope.row.warehouseName || scope.row.warehouseCode }}</template>
                </el-table-column>
                <el-table-column prop="bomCode" label="物料编码" align="center" width="180" show-overflow-tooltip></el-table-column>
                <el-table-column prop="quantity" label="变动数量" align="center" width="90"></el-table-column>
                <el-table-column prop="quantityBefore" label="变动前" align="center" width="80"></el-table-column>
                <el-table-column prop="balanceAfter" label="变动后" align="center" width="80"></el-table-column>
                <el-table-column label="单价(元)" align="center" width="110">
                  <template slot-scope="scope">
                    {{ priceText(scope.row.unitPrice) }}
                    <span v-if="scope.row.priceBefore != null" class="inventory-tip-small">（原 {{ priceText(scope.row.priceBefore) }}）</span>
                  </template>
                </el-table-column>
                <el-table-column prop="refCode" label="单据号" align="center" width="170" show-overflow-tooltip></el-table-column>
                <el-table-column label="来源" align="center" show-overflow-tooltip>
                  <template slot-scope="scope">{{ sourceText(scope.row) }}</template>
                </el-table-column>
                <el-table-column prop="operatorName" label="操作人" align="center" width="90"></el-table-column>
              </el-table>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 入库录入 -->
    <el-dialog title="入库录入" width="640px" :visible.sync="inboundVisible" :close-on-click-modal="false">
      <el-form label-width="100px" :model="inbound" size="small" @submit.native.prevent>
        <el-form-item label="仓库" required>
          <el-select v-model="inbound.warehouseCode" style="width: 100%;">
            <el-option v-for="w in activeWarehouses" :key="w.code" :value="w.code"
                       :label="w.name + '（' + w.code + '）'"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="物料编码" required>
          <el-autocomplete v-model="inbound.bomCode" :fetch-suggestions="suggestStock" style="width: 100%;"
                           placeholder="新物料直接填写；已有物料可从库存中选" @select="fillFromStock"></el-autocomplete>
        </el-form-item>
        <el-form-item label="物料名称" required>
          <el-input v-model="inbound.name" maxlength="100"></el-input>
        </el-form-item>
        <el-form-item label="规格型号">
          <el-input v-model="inbound.model" maxlength="200"></el-input>
        </el-form-item>
        <el-form-item label="单位" required>
          <el-input v-model="inbound.unit" maxlength="20" style="width: 160px;"></el-input>
        </el-form-item>
        <el-form-item label="数量" required>
          <el-input-number v-model="inbound.quantity" :min="0" :precision="2" :controls="false"></el-input-number>
        </el-form-item>
        <el-form-item label="未税单价" required>
          <el-input-number v-model="inbound.unitPrice" :min="0" :precision="4" :controls="false"></el-input-number>
          <span class="inventory-tip-small">元</span>
        </el-form-item>
        <el-form-item label="批次号">
          <el-input v-model="inbound.batchCode" maxlength="100"></el-input>
        </el-form-item>
        <el-form-item label="外部单据号">
          <el-input v-model="inbound.extRef" maxlength="100" placeholder="外部采购系统的订单或到货单号"></el-input>
        </el-form-item>
        <el-form-item label="供应商">
          <el-input v-model="inbound.supplierName" maxlength="100"></el-input>
        </el-form-item>
        <el-form-item label="说明">
          <el-input type="textarea" rows="2" v-model="inbound.remark" maxlength="200" show-word-limit></el-input>
        </el-form-item>
      </el-form>
      <div slot="footer">
        <el-button @click="inboundVisible = false">取 消</el-button>
        <el-button type="primary" :loading="saving" @click="submitInbound">入 库</el-button>
      </div>
    </el-dialog>

    <!-- Excel 导入 -->
    <el-dialog title="Excel 导入入库" width="1100px" :visible.sync="importVisible" :close-on-click-modal="false">
      <div class="inventory-import-head">
        <input ref="importFile" type="file" accept=".xlsx,.xls" @change="readImportFile"/>
        <span class="inventory-tip-small">按模板填写，单次最多 1000 行；任一行有误则整批不入库。</span>
      </div>
      <el-table :data="importRows" border height="420" style="width: 100%; margin-top: 10px;">
        <el-table-column type="index" label="行" width="50" align="center"></el-table-column>
        <el-table-column prop="warehouseCode" label="仓库编码" width="100" align="center"></el-table-column>
        <el-table-column prop="bomCode" label="物料编码" width="160" show-overflow-tooltip></el-table-column>
        <el-table-column prop="name" label="物料名称" show-overflow-tooltip></el-table-column>
        <el-table-column prop="model" label="规格型号" show-overflow-tooltip></el-table-column>
        <el-table-column prop="unit" label="单位" width="60" align="center"></el-table-column>
        <el-table-column prop="quantity" label="数量" width="80" align="center"></el-table-column>
        <el-table-column prop="unitPrice" label="未税单价" width="90" align="center"></el-table-column>
        <el-table-column prop="batchCode" label="批次号" width="100" show-overflow-tooltip></el-table-column>
        <el-table-column prop="extRef" label="外部单据号" width="110" show-overflow-tooltip></el-table-column>
        <el-table-column prop="supplierName" label="供应商" width="100" show-overflow-tooltip></el-table-column>
      </el-table>
      <div slot="footer">
        <el-button @click="importVisible = false">取 消</el-button>
        <el-button type="primary" :loading="saving" :disabled="!importRows.length" @click="submitImport">
          导入 {{ importRows.length }} 行
        </el-button>
      </div>
    </el-dialog>

    <!-- 库存调整 -->
    <el-dialog title="库存调整" width="560px" :visible.sync="adjustVisible" :close-on-click-modal="false">
      <el-form label-width="110px" size="small" @submit.native.prevent>
        <el-form-item label="物料">
          {{ [adjust.bomCode, adjust.name, adjust.model].filter(v => !!v).join(' / ') }}
        </el-form-item>
        <el-form-item label="当前数量">{{ adjust.currentQuantity }} {{ adjust.unit }}</el-form-item>
        <el-form-item label="调整后数量">
          <el-input-number v-model="adjust.quantity" :min="0" :precision="2" :controls="false"></el-input-number>
        </el-form-item>
        <el-form-item label="当前单价">{{ priceText(adjust.currentPrice) }} 元</el-form-item>
        <el-form-item label="调整后单价">
          <el-input-number v-model="adjust.unitPrice" :min="0" :precision="4" :controls="false"></el-input-number>
          <span class="inventory-tip-small">改价不影响已发料单据的记账金额</span>
        </el-form-item>
        <el-form-item label="调整原因" required>
          <el-input type="textarea" rows="3" v-model="adjust.remark" maxlength="150" show-word-limit
                    placeholder="如：盘点差异、录入错误更正"></el-input>
        </el-form-item>
      </el-form>
      <div slot="footer">
        <el-button @click="adjustVisible = false">取 消</el-button>
        <el-button type="primary" :loading="saving" @click="submitAdjust">确 定</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import Swal from "sweetalert2";
import {dateFormat} from "@/filter/filter";

const TEMPLATE_HEADERS = ['仓库编码', '物料编码', '物料名称', '规格型号', '单位', '数量', '未税单价', '批次号', '外部单据号', '供应商', '说明'];
const TEMPLATE_FIELDS = ['warehouseCode', 'bomCode', 'name', 'model', 'unit', 'quantity', 'unitPrice', 'batchCode', 'extRef', 'supplierName', 'remark'];

export default {
  name: "inventory",

  data() {
    return {
      OPERATIONS: {
        INBOUND: '入库',
        OUTBOUND: '领料出库',
        BORROW: '借用出库',
        RETURN: '退料入库',
        ADJUST: '库存调整',
      },
      loginCustomer: {},
      activeTab: 'stock',
      warehouseList: [],
      stockList: [],
      stockLoading: false,
      stockFilter: { warehouseCode: '', keyword: '', positiveOnly: false },
      stockPage: 1,
      stockPageSize: 20,
      logList: [],
      logLoading: false,
      logQuery: { warehouseCode: '', bomCode: '', operation: '' },
      logDates: null,
      saving: false,
      inboundVisible: false,
      inbound: {},
      importVisible: false,
      importRows: [],
      adjustVisible: false,
      adjust: {},
    };
  },

  computed: {
    activeWarehouses() {
      return this.warehouseList.filter(w => w.status === 'ACTIVE');
    },
    filteredStock() {
      let f = this.stockFilter;
      let keyword = (f.keyword || '').trim().toLowerCase();
      return this.stockList.filter(row => {
        if (f.warehouseCode && row.warehouseCode !== f.warehouseCode) {
          return false;
        }
        if (f.positiveOnly && !(row.quantity > 0)) {
          return false;
        }
        if (!keyword) {
          return true;
        }
        return [row.bomCode, row.name, row.model].some(v => (v || '').toLowerCase().indexOf(keyword) >= 0);
      });
    },
    pagedStock() {
      let start = (this.stockPage - 1) * this.stockPageSize;
      return this.filteredStock.slice(start, start + this.stockPageSize);
    },
  },

  watch: {
    stockFilter: {
      deep: true,
      handler() {
        this.stockPage = 1;
      }
    },
  },

  mounted() {
    let login = Tool.getLoginUser();
    this.loginCustomer = login.userGroup === LOGIN_USER_GROUP.CUSTOMER.key ? login : login.customer;
    this.loadWarehouses();
    this.loadStock();
  },

  methods: {
    url(path) {
      return process.env.VUE_APP_SERVER + '/customer/material-stock' + path;
    },
    priceText(value) {
      return value == null ? '' : Number(value).toFixed(4);
    },
    amountText(row) {
      if (row.unitPrice == null || row.quantity == null) {
        return '';
      }
      return (Number(row.unitPrice) * Number(row.quantity)).toFixed(2);
    },
    timeText(value) {
      return value ? dateFormat(value) : '';
    },
    sourceText(row) {
      return [row.extRef, row.supplierName, row.batchCode ? '批次 ' + row.batchCode : '', row.remark]
          .filter(v => !!v).join('；');
    },

    switchTab(tab) {
      this.activeTab = tab;
      if (tab === 'log' && !this.logList.length) {
        this.loadLogs();
      }
    },

    loadWarehouses() {
      let _this = this;
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/material-warehouse/list/' + _this.loginCustomer.id).then((response) => {
        let resp = response.data;
        if (resp.success) {
          _this.warehouseList = resp.content || [];
        } else {
          Toast.warning(resp.message, Swal);
        }
      });
    },

    loadStock() {
      let _this = this;
      _this.stockLoading = true;
      _this.$ajax.post(_this.url('/list/' + _this.loginCustomer.id)).then((response) => {
        let resp = response.data;
        if (resp.success) {
          _this.stockList = resp.content || [];
        } else {
          Toast.warning(resp.message, Swal);
        }
      }).finally(() => {
        _this.stockLoading = false;
      });
    },

    loadLogs() {
      let _this = this;
      _this.logLoading = true;
      let query = Object.assign({}, _this.logQuery, {
        customerId: _this.loginCustomer.id,
        startDate: _this.logDates ? _this.logDates[0] : null,
        endDate: _this.logDates ? _this.logDates[1] : null,
      });
      _this.$ajax.post(_this.url('/logs'), query).then((response) => {
        let resp = response.data;
        if (resp.success) {
          _this.logList = resp.content || [];
        } else {
          Toast.warning(resp.message, Swal);
        }
      }).finally(() => {
        _this.logLoading = false;
      });
    },

    showLogsOf(row) {
      this.logQuery = { warehouseCode: row.warehouseCode, bomCode: row.bomCode, operation: '' };
      this.logDates = null;
      this.activeTab = 'log';
      this.loadLogs();
    },

    openInbound() {
      let first = this.activeWarehouses[0];
      this.inbound = {
        warehouseCode: first ? first.code : '', bomCode: '', name: '', model: '', unit: '',
        quantity: undefined, unitPrice: undefined, batchCode: '', extRef: '', supplierName: '', remark: ''
      };
      this.inboundVisible = true;
    },

    suggestStock(query, callback) {
      let q = (query || '').trim().toLowerCase();
      let warehouse = this.inbound.warehouseCode;
      let rows = this.stockList.filter(row => row.warehouseCode === warehouse
          && (!q || row.bomCode.toLowerCase().indexOf(q) >= 0 || (row.name || '').toLowerCase().indexOf(q) >= 0))
          .slice(0, 50)
          .map(row => ({ value: row.bomCode + ' / ' + row.name, row: row }));
      callback(rows);
    },

    fillFromStock(item) {
      this.inbound.bomCode = item.row.bomCode;
      this.inbound.name = item.row.name;
      this.inbound.model = item.row.model || '';
      this.inbound.unit = item.row.unit;
    },

    submitInbound() {
      let _this = this;
      let d = _this.inbound;
      if (!d.warehouseCode || !(d.bomCode || '').trim() || !(d.name || '').trim() || !(d.unit || '').trim()) {
        Toast.warning("请填写仓库、物料编码、物料名称与单位", Swal);
        return;
      }
      if (!(d.quantity > 0)) {
        Toast.warning("数量须大于 0", Swal);
        return;
      }
      if (d.unitPrice == null || d.unitPrice < 0) {
        Toast.warning("请填写未税单价", Swal);
        return;
      }
      _this.saving = true;
      let payload = Object.assign({}, d, { customerId: _this.loginCustomer.id });
      _this.$ajax.post(_this.url('/inbound'), payload).then((response) => {
        let resp = response.data;
        if (resp.success) {
          Toast.success("入库成功", Swal);
          _this.inboundVisible = false;
          _this.loadStock();
          _this.logList = [];
        } else {
          Toast.warning(resp.message, Swal);
        }
      }).finally(() => {
        _this.saving = false;
      });
    },

    async downloadTemplate() {
      const XLSX = await import(/* webpackChunkName: "xlsx" */ 'xlsx');
      let sample = ['LOCAL', 'R-0603-10K', '贴片电阻', '0603 10kΩ ±1%', '个', 100, 0.02, 'B20261009', 'PO-2026-001', '某电子有限公司', ''];
      let sheet = XLSX.utils.aoa_to_sheet([TEMPLATE_HEADERS, sample]);
      let book = XLSX.utils.book_new();
      XLSX.utils.book_append_sheet(book, sheet, '入库');
      XLSX.writeFile(book, '研发物料入库模板.xlsx');
    },

    async exportStock() {
      const XLSX = await import(/* webpackChunkName: "xlsx" */ 'xlsx');
      let rows = this.filteredStock.map(row => ({
        '仓库编码': row.warehouseCode,
        '仓库名称': row.warehouseName || '',
        '物料编码': row.bomCode,
        '物料名称': row.name,
        '规格型号': row.model || '',
        '单位': row.unit,
        '库存数量': row.quantity,
        '未税单价': row.unitPrice == null ? '' : Number(row.unitPrice),
        '库存金额': this.amountText(row),
      }));
      let book = XLSX.utils.book_new();
      XLSX.utils.book_append_sheet(book, XLSX.utils.json_to_sheet(rows), '库存');
      XLSX.writeFile(book, '研发物料库存.xlsx');
    },

    openImport() {
      this.importRows = [];
      this.importVisible = true;
      this.$nextTick(() => {
        if (this.$refs.importFile) {
          this.$refs.importFile.value = '';
        }
      });
    },

    readImportFile(event) {
      let _this = this;
      let file = event.target.files && event.target.files[0];
      if (!file) {
        return;
      }
      let reader = new FileReader();
      reader.onload = async (e) => {
        const XLSX = await import(/* webpackChunkName: "xlsx" */ 'xlsx');
        let book = XLSX.read(new Uint8Array(e.target.result), { type: 'array' });
        let sheet = book.Sheets[book.SheetNames[0]];
        let matrix = XLSX.utils.sheet_to_json(sheet, { header: 1, defval: '' });
        if (!matrix.length) {
          Toast.warning("文件没有数据", Swal);
          return;
        }
        let header = matrix[0].map(h => String(h).trim());
        let missing = TEMPLATE_HEADERS.slice(0, 7).filter(h => header.indexOf(h) < 0);
        if (missing.length) {
          Toast.warning("缺少列：" + missing.join('、') + "。请使用模板", Swal);
          return;
        }
        let rows = [];
        matrix.slice(1).forEach(line => {
          if (line.every(cell => String(cell).trim() === '')) {
            return;
          }
          let row = {};
          TEMPLATE_HEADERS.forEach((h, i) => {
            let idx = header.indexOf(h);
            let value = idx < 0 ? '' : line[idx];
            row[TEMPLATE_FIELDS[i]] = typeof value === 'string' ? value.trim() : value;
          });
          row.warehouseCode = row.warehouseCode || 'LOCAL';
          row.quantity = row.quantity === '' ? null : Number(row.quantity);
          row.unitPrice = row.unitPrice === '' ? null : Number(row.unitPrice);
          ['bomCode', 'name', 'model', 'unit', 'batchCode', 'extRef', 'supplierName', 'remark'].forEach(k => {
            row[k] = row[k] === '' || row[k] == null ? null : String(row[k]);
          });
          rows.push(row);
        });
        if (rows.length > 1000) {
          Toast.warning("单次最多导入 1000 行，当前 " + rows.length + " 行", Swal);
          return;
        }
        _this.importRows = rows;
      };
      reader.readAsArrayBuffer(file);
    },

    submitImport() {
      let _this = this;
      _this.saving = true;
      _this.$ajax.post(_this.url('/inbound-batch'), { customerId: _this.loginCustomer.id, rows: _this.importRows }).then((response) => {
        let resp = response.data;
        if (resp.success) {
          Toast.success(resp.message || "导入成功", Swal);
          _this.importVisible = false;
          _this.loadStock();
          _this.logList = [];
        } else {
          Swal.fire({ icon: 'warning', title: '导入未完成', text: resp.message, width: 720 });
        }
      }).finally(() => {
        _this.saving = false;
      });
    },

    openAdjust(row) {
      this.adjust = {
        id: row.id, bomCode: row.bomCode, name: row.name, model: row.model || '', unit: row.unit,
        currentQuantity: row.quantity, currentPrice: row.unitPrice,
        quantity: row.quantity, unitPrice: row.unitPrice == null ? undefined : Number(row.unitPrice), remark: ''
      };
      this.adjustVisible = true;
    },

    submitAdjust() {
      let _this = this;
      let a = _this.adjust;
      let reason = (a.remark || '').trim();
      if (reason.length < 2 || reason.length > 150) {
        Toast.warning("调整原因须为 2～150 字", Swal);
        return;
      }
      let quantityChanged = a.quantity != null && Number(a.quantity) !== Number(a.currentQuantity);
      let priceChanged = a.unitPrice != null && (a.currentPrice == null || Number(a.unitPrice) !== Number(a.currentPrice));
      if (!quantityChanged && !priceChanged) {
        Toast.warning("调整前后没有变化", Swal);
        return;
      }
      _this.saving = true;
      let payload = {
        id: a.id,
        customerId: _this.loginCustomer.id,
        quantity: quantityChanged ? a.quantity : null,
        unitPrice: priceChanged ? a.unitPrice : null,
        remark: reason,
      };
      _this.$ajax.post(_this.url('/adjust'), payload).then((response) => {
        let resp = response.data;
        if (resp.success) {
          Toast.success("调整成功", Swal);
          _this.adjustVisible = false;
          _this.loadStock();
          _this.logList = [];
        } else {
          Toast.warning(resp.message, Swal);
        }
      }).finally(() => {
        _this.saving = false;
      });
    },
  }
}
</script>

<style scoped>
.inventory-tip {
  margin: 0 0 8px 5px;
}
.inventory-tip-small {
  color: #909399;
  font-size: 12px;
  margin-left: 6px;
}
.inventory-toolbar {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  flex-wrap: wrap;
}
.inventory-pager {
  margin-top: 12px;
  text-align: right;
}
.inventory-import-head {
  display: flex;
  align-items: center;
}
</style>
