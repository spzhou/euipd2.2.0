<!--
 * Author: 周朔鹏
 * Date: 2025-02-26
 * Email: 391902958@qq.com
 * Website: https://www.euipd.com
-->

<template>
  <div style="height: 100%;">
    <div class="paper-height">
      <div style="height: 100%; overflow: scroll;">
        <div class="a4-paper-big-plain" style="width: 100%;">
          <div>
            <div style="margin: 0px 30px;">
              <div>
                <div class="ca-form-header">
                  <div v-if="!readOnly" style="width: 100%; display: flex; flex-direction: row; justify-content: flex-end;">
                    <el-tooltip content="下载BOM导入模板, 不要修改文件名称和字段名称, 填写完整后上传." placement="top" effect="light" :open-delay=1000>
                      <el-button type="text" @click="downloadFile(templatePath)">下载BOM导入模板: import_bom.xlsx</el-button>
                    </el-tooltip>
                  </div>
                  <div style="display: flex; flex-direction: row; justify-content: space-between; align-items: center;">
                    <div style="font-size: 14px; font-weight: bold; margin: 15px 0">
                      <div style="display: flex; flex-direction: row; align-items: center; justify-content: center;">
                        <div style="margin-right: 20px; line-height: 28px;">
                          BOM清单
                        </div>
                        <BomSnapshotSelect
                            style="margin-right: 20px;"
                            owner-type="SUBPROJECT"
                            :owner-id="subprojectId"
                            @change="onSnapshotChange">
                        </BomSnapshotSelect>
                        <div>
<!--                          <el-button style="margin-right: 10px;" type="primary" size="mini" @click="bomStructure">当前项目BOM结构化</el-button>-->
                        </div>
                        <div v-if="multipleSelection.length > 1">
                          <el-button type="primary" size="mini" @click="batchDel">批量删除</el-button>
                        </div>
                      </div>
                    </div>
                    <div v-if="snapshotItems"></div>
                    <div v-else-if="!subProject.bomStatus || subProject.bomStatus===CHARACTER_BOM_STATUS.EDIT.key" style="display: flex; flex-direction: row; justify-content: center; align-items: center;">
                      <el-button type="primary" @click="selectBomItem">物料拣选</el-button>
                      <el-button type="primary" @click="addBomItem">添加BOM物料</el-button>
                      <div style="margin-left: 10px;">
                        <el-tooltip content="通过导入BOM物料表" placement="top" effect="light" :open-delay=1000>
                          <ExcelBomUpload
                              :key="timeStamp"
                              :text="'导入BOM物料'"
                              :subproject-id="subprojectId"
                              :refresh-function="refreshUploadDlg"
                          ></ExcelBomUpload>
                        </el-tooltip>
                      </div>
                    </div>
                    <div v-else>
                      <div v-if="!readOnly">
                        <el-button type="primary" @click="rejectSubProjectBom">驳回重新编辑</el-button>
                      </div>
                    </div>
                  </div>
                  <div class="hr hr-6 dotted"></div>
                </div>
                <!-- @row-click="rowClick"-->
                <el-table
                    ref="multipleTable"
                    :data="snapshotItems || bomMaterialList"
                    stripe
                    show-summary
                    :summary-method="getSummaries"
                    @selection-change="handleSelectionChange"
                    style="width: 100%">
                  <el-table-column
                      type="selection"
                      width="55">
                  </el-table-column>
                  <el-table-column label="序号" align="center" width="50">
                    <template slot-scope="scope">
                      {{ scope.$index + 1 }}
                    </template>
                  </el-table-column>
                  <el-table-column
                      prop="itemType"
                      width="90"
                      :formatter="itemTypeFormat"
                      align="center"
                      label="类别">
                  </el-table-column>
                  <el-table-column
                      prop="bomCode"
                      sortable
                      show-overflow-tooltip
                      align="center"
                      width="120"
                      label="物料编码">
                  </el-table-column>
                  <el-table-column
                      prop="name"
                      show-overflow-tooltip
                      width="140"
                      align="center"
                      label="名称">
                  </el-table-column>
                  <el-table-column
                      prop="model"
                      show-overflow-tooltip
                      align="center"
                      width="140"
                      label="型号">
                  </el-table-column>
                  <el-table-column
                      prop="characterName"
                      show-overflow-tooltip
                      align="center"
                      label="所属特性">
                  </el-table-column>
                  <el-table-column
                      prop="unit"
                      width="100"
                      show-overflow-tooltip
                      align="center"
                      label="单位">
                  </el-table-column>
                  <el-table-column
                      prop="preTaxUnitPrice"
                      :formatter="unitPriceFormat"
                      width="120"
                      sortable
                      show-overflow-tooltip
                      align="center"
                      label="未税单价(元)">
                  </el-table-column>
                  <el-table-column
                      prop="amount"
                      width="80"
                      sortable
                      show-overflow-tooltip
                      align="center"
                      label="数量">
                  </el-table-column>
                  <el-table-column
                      prop="preTaxSumPrice"
                      :formatter="sumPriceFormat"
                      width="100"
                      sortable
                      show-overflow-tooltip
                      align="center"
                      label="未税小计(元)">
                  </el-table-column>
                  <el-table-column
                      v-if="!snapshotItems"
                      fixed="right"
                      label="操作"
                      align="center"
                      width="160">
                    <template slot-scope="scope">
                      <el-button v-if="!isCbbLine(scope.row)" type="text" plain size="small" @click.stop="rowClick(scope.row)">
                        编辑
                      </el-button>
                      <el-button type="text" plain size="small" @click.stop="deleteItem(scope.row.id)">
                        删除
                      </el-button>
                    </template>
                  </el-table-column>
                </el-table>
              </div>
            </div>
            <div class="clearfix form-actions">
              <div style="display: flex; justify-content: center;">
                <div style="display: flex; flex-direction: column; justify-content: center; align-items: center;">
                  <div v-if="!subProject.bomStatus || subProject.bomStatus === CHARACTER_BOM_STATUS.EDIT.key">
                    <small> 说明: 特性评审之前, 请完成BOM的编辑工作, 并提交. 提交之后, 您将不能再对各项物料信息进行编辑, 除非被退回或产品经理授权你进行编辑.</small>
                  </div>
                  <div style="margin-top:8px;">
                    <div v-if="!snapshotItems && (!subProject.bomStatus || subProject.bomStatus === CHARACTER_BOM_STATUS.EDIT.key)">
                      <el-button type="primary" v-on:click="submitSubProjectBom()">提交BOM</el-button>
                    </div>
<!--                    <div v-if="subProject.bomStatus && subProject.bomStatus === CHARACTER_BOM_STATUS.SUBMIT.key">
                      <el-button type="primary" plain disabled>提交BOM</el-button>
                    </div>-->
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!--BOM编辑对话框-->
    <el-dialog
        width="29cm"
        :visible.sync="editBomDlg"
        append-to-body
        :before-close="handleClose"
        :close-on-click-modal=false>
      <div slot="title" style="font-size: 20px;">{{ dialogTitle }}</div>
      <div v-if="editBomDlg">
        <BomEditDlg
            :key="dlgData.id"
            :bom-item="dlgData"
            :close-dlg = closeDialog
        ></BomEditDlg>
      </div>
    </el-dialog>

    <!--BOM结构化-->
    <el-dialog
        width="29cm"
        :visible.sync="structureBomDlg"
        append-to-body
        :before-close="handleClose"
        :close-on-click-modal=false>
      <div slot="title" style="font-size: 20px;">{{ dialogTitle }}</div>
      <div v-if="structureBomDlg">
        <bomStructureEditDlg
            :key="subprojectId"
            :subproject-id="subprojectId"
            :close-dlg = closeDialog
        ></bomStructureEditDlg>
      </div>
    </el-dialog>

    <!--添加BOM物料对话框-->
    <el-dialog
        width="29cm"
        :visible.sync="addBomDlg"
        append-to-body
        :before-close="handleClose"
        :close-on-click-modal=false>
      <div slot="title" style="font-size: 20px;">{{ dialogTitle }}</div>
      <div v-if="addBomDlg">
        <BomAddSubprojectDlg
            :key="random"
            :subproject-id="subprojectId"
            :close-dlg = closeDialog
        ></BomAddSubprojectDlg>
      </div>
    </el-dialog>

    <!--直接任务BOM物料拣选-->
    <el-dialog
        width="29cm"
        :visible.sync="selectBomDlg"
        append-to-body
        :before-close="handleClose"
        :close-on-click-modal=false>
      <div slot="title" style="font-size: 20px;">{{ dialogTitle }}</div>
      <div v-if="selectBomDlg">
        <BomSelectedListSubprojectDlg
            :key="random"
            :subproject-id="subprojectId"
            :close-dlg = closeDialog
        ></BomSelectedListSubprojectDlg>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import Swal from "sweetalert2";
import JobitemDetailInfoDlg from "@/components/std/jobitem-detail-info-dlg.vue";
import {optionKV, rmbFormat} from "@/filter/filter";
import ApproveSignatureCore from "@/components/rdms/approve-signature-core.vue";
import BomEditDlg from "@/components/std/bom-edit-dlg.vue";
import BomAddSubprojectDlg from "@/components/std/bom-add-subproject-dlg.vue";
import BomSelectedListSubprojectDlg from "@/components/std/bom-selected-list-subproject-dlg.vue";
import ExcelBomUpload from "@/components/rdms/excel-bom-upload.vue";
import bomStructureEditDlg from "@/components/std/bom-structure-edit-dlg.vue";
import {webAssetUrl} from "@/shared/oss";
import BomSnapshotSelect from "@/components/std/bom-snapshot-select.vue";

export default {
  components: {
    ExcelBomUpload,
    BomEditDlg,
    BomAddSubprojectDlg,
    ApproveSignatureCore,
    JobitemDetailInfoDlg,
    BomSelectedListSubprojectDlg,
    bomStructureEditDlg,
    BomSnapshotSelect,
  },
  name: "bom-edit-list-subproject-dlg",

  props: {
    subprojectId: {
      default: null,
    },
    readOnly: {
      default: false,
    },
    closeDlg: {
      type: Function,
      default: null,
    }
  },

  data() {
    return {
      loginUser: {},
      loginCustomer: {},
      bomMaterialList: [],
      multipleSelection: [],
      materialToBom: {},

      timeStamp: 0,
      dlgData: {},
      editBomDlg: false,
      structureBomDlg: false,
      addBomDlg: false,
      importBomDlg: false,
      selectBomDlg: false,
      random: 0,
      dialogTitle: '',

      characterInfo: {},
      subProject: {},
      snapshotItems: null,

      APPLICATION_STATUS: APPLICATION_STATUS,
      CHARACTER_BOM_STATUS: CHARACTER_BOM_STATUS,

      templatePath: webAssetUrl("import_bom.xlsx"),
    };
  },

  computed: {},

  mounted: function () {
    let _this = this;
    _this.timeStamp = Date.now();
    _this.getLoginUser();
    if (Tool.isNotEmpty(_this.subprojectId)) {
      _this.getSubProjectInfo(_this.subprojectId);
      _this.getBomMaterialListBySubprojectId(_this.subprojectId);
    }
    _this.$bus.$on('refresh-bom-edit-list-subproject-dlg', (data) => {
      if(data){
        _this.getBomMaterialListBySubprojectId(_this.subprojectId);
      }
    });
  },

  beforeDestroy() {
    let _this = this;
    _this.$bus.$off('refresh-bom-edit-list-subproject-dlg');
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

    handleSelectionChange(val) {
      let _this = this;
      _this.multipleSelection = val;
      // console.log(_this.$refs.multipleTable.selection);
    },

    refreshUploadDlg(){
      let _this = this;
      _this.timeStamp = Date.now();
    },

    downloadFile(path) {
      // let path = "'http://127.0.0.1:18100/euipd-web/fm4ek0Zy/system/tHnkCpKT-import_workers_template.xlsx'";  // 必须使用变量, 静态值会提示"无法从网站提取文件"
      let fileName = "import_workers_template.xlsx";
      window.open(path, '_self');
      // FileSaver.saveAs(path, fileName);
    },

    getSubProjectInfo(subProjectId){
      let _this = this;
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/sub-project/getSubProjectSimpleInfo/' + subProjectId).then((response) => {
        let resp = response.data;
        if (resp.success) {
          _this.subProject = resp.content;
        } else {
          Toast.warning(resp.message, Swal);
        }
      })
    },

    onSnapshotChange(items) {
      this.snapshotItems = items;
    },

    rowClick(row) {
      let _this = this;
      if(!_this.subProject.bomStatus || _this.subProject.bomStatus === CHARACTER_BOM_STATUS.EDIT.key){
        _this.dlgData = row;
        _this.editBomDlg = true;
        _this.dialogTitle = "BOM编辑";
      }
    },

    bomStructure(row) {
      let _this = this;
      _this.dlgData = row;
      _this.structureBomDlg = true;
      _this.dialogTitle = "BOM结构化编辑";
    },

    addBomItem() {
      let _this = this;
      _this.random = Math.random();
      _this.addBomDlg = true;
      _this.dialogTitle = "添加BOM物料";
    },

    importBomItem() {
      let _this = this;
      _this.random = Math.random();
      _this.importBomDlg = true;
      _this.dialogTitle = "导入BOM物料";
    },

    selectBomItem() {
      let _this = this;
      _this.random = Math.random();
      _this.selectBomDlg = true;
      _this.dialogTitle = "项目/子项目BOM物料拣选";
    },

    closeDialog() {
      let _this = this;
      _this.editBomDlg = false;
      _this.structureBomDlg = false;
      _this.addBomDlg = false;
      _this.selectBomDlg = false;
    },
    handleClose(done) {
      done();
    },

    unitPriceFormat(row, column) {
      if (this.isCbbLine(row)) {
        return '';
      }
      if (row.preTaxUnitPrice) {
        return rmbFormat(row.preTaxUnitPrice);
      }
      return 0;
    },

    sumPriceFormat(row, column) {
      if (this.isCbbLine(row)) {
        return '';
      }
      if (row.preTaxSumPrice) {
        return rmbFormat(row.preTaxSumPrice);
      }
      return 0;
    },

    isCbbLine(row) {
      return row.itemType === BOM_ITEM_TYPE.CBB.key;
    },

    itemTypeFormat(row) {
      return this.isCbbLine(row) ? BOM_ITEM_TYPE.CBB.value : BOM_ITEM_TYPE.MATERIAL.value;
    },

    getBomMaterialListBySubprojectId(subprojectId) {
      let _this = this;
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/material/getBomMaterialListBySubprojectId/' + subprojectId).then((response) => {
        let resp = response.data;
        if (resp.success) {
          _this.bomMaterialList = resp.content;
        } else {
          Toast.warning(resp.message, Swal);
        }
      });
    },

    submitSubProjectBom() {
      let _this = this;
      Swal.fire({
        title: '确认',
        text: "确认提交吗?",
        icon: 'warning',
        showCancelButton: true,
        confirmButtonColor: '#409EFF',
        cancelButtonColor: '#909399',
        confirmButtonText: '确认!',
        cancelButtonText: '取消!'
      }).then((result) => {
        if (result.isConfirmed) {
          _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/material/submitSubProjectBom/'+ _this.subprojectId).then((response) => {
            let resp = response.data;
            if (resp.success) {
              _this.getSubProjectInfo(_this.subprojectId);
              _this.$bus.$emit('refresh-review-subproject-create-page', true);
              Toast.success("保存成功", Swal);
              _this.closeDlg();
            }
          });
        }
      }).catch(_ => {
      });
    },

    rejectSubProjectBom() {
      let _this = this;
      Swal.fire({
        title: '驳回子项目物料清单',
        text: "驳回后退回重新编辑",
        input: 'textarea',
        inputPlaceholder: '请填写驳回理由',
        inputAttributes: { maxlength: 500 },
        inputValidator: (value) => (value && value.trim().length >= 2) ? undefined : '请填写驳回理由（至少 2 个字）',
        showCancelButton: true,
        confirmButtonColor: '#409EFF',
        cancelButtonColor: '#909399',
        confirmButtonText: '驳回',
        cancelButtonText: '取消'
      }).then((result) => {
        if (result.isConfirmed) {
          _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/material/rejectSubProjectBom/'+ _this.subprojectId, {
            rejectReason: result.value.trim(),
          }).then((response) => {
            let resp = response.data;
            if (resp.success) {
              _this.getSubProjectInfo(_this.subprojectId);
              _this.$bus.$emit('refresh-review-subproject-create-page', true);
              Toast.success("已驳回", Swal);
              if (typeof _this.closeDlg === 'function') {
                _this.closeDlg();
              }
            } else {
              Toast.warning(resp.message, Swal);
            }
          });
        }
      }).catch(_ => {
      });
    },

    batchDel(){
      let _this = this;
      Swal.fire({
        title: '确认批量删除？',
        text: "删除后不可恢复，是否确认批量删除？",
        icon: 'warning',
        showCancelButton: true,
        confirmButtonColor: '#409EFF',
        cancelButtonColor: '#909399',
        confirmButtonText: '确认!',
        cancelButtonText: '取消!'
      }).then((result) => {
        if (result.value) {
          _this.multipleSelection.forEach(row => {
            _this.exeDeleteBomItem(row.id);
          });
        }
      })
    },
    exeDeleteBomItem(id) {
      let _this = this;
      _this.$ajax.delete(process.env.VUE_APP_SERVER + '/customer/material/delete/' + id).then((response)=>{
        let resp = response.data;
        if (resp.success) {
          _this.getBomMaterialListBySubprojectId(_this.subprojectId);
          Toast.success("删除成功", Swal);
        }
      });
    },

    deleteItem(id) {
      let _this = this;
      Swal.fire({
        title: '确认？',
        text: "删除后不可恢复，是否确认删除？",
        icon: 'warning',
        showCancelButton: true,
        confirmButtonColor: '#409EFF',
        cancelButtonColor: '#909399',
        confirmButtonText: '确认!',
        cancelButtonText: '取消!'
      }).then((result) => {
        if (result.value) {
          _this.$ajax.delete(process.env.VUE_APP_SERVER + '/customer/material/delete/' + id).then((response)=>{
            let resp = response.data;
            if (resp.success) {
              _this.getBomMaterialListBySubprojectId(_this.subprojectId);
              Toast.success("删除成功", Swal);
            }
          });
        }
      })
    },

    getSummaries(param) {
      const { columns, data } = param;
      const materials = data.filter(item => !this.isCbbLine(item));
      const sums = [];
      columns.forEach((column, index) => {
        if (index === 1) {
          sums[index] = '总计';
          return;
        }
        if (column.property !== 'preTaxSumPrice') {
          sums[index] = '';
          return;
        }
        const values = materials.map(item => Number(item[column.property])).filter(value => !isNaN(value));
        sums[index] = rmbFormat(values.reduce((prev, curr) => prev + curr, 0));
      });
      return sums;
    }
  },
}
</script>

<style lang="scss" scoped>
@import './../../../public/static/scss/caipd';

.paper-height {
  height: calc(100vh - 200px);
}

</style>
