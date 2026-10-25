<!--
 * Author: 周朔鹏
 * Date: 2025-01-04
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
                  <div style="display: flex; flex-direction: row; justify-content: space-between; align-items: center;">
                    <div style="font-size: 14px; font-weight: bold; margin: 15px 0">
                      特性物料清单:
                      <span style="margin-left: 20px"> ( 状态 : {{ CHARACTER_BOM_STATUS | optionKV(characterInfo.bomStatus) }} )</span>
                      <BomSnapshotSelect
                          ref="snapshotSelect"
                          style="margin-left: 20px;"
                          owner-type="CHARACTER"
                          :owner-id="characterId"
                          @change="onSnapshotChange">
                      </BomSnapshotSelect>
                    </div>
                    <div v-if="snapshotItems"></div>
                    <div v-else-if="!characterInfo.bomStatus || characterInfo.bomStatus===CHARACTER_BOM_STATUS.EDIT.key" style="display: flex; flex-direction: row; justify-content: center; align-items: center;">
                      <el-button type="primary" @click="selectBomItem">物料拣选</el-button>
                      <el-button type="primary" @click="addBomItem">添加BOM物料</el-button>
                      <el-button type="primary" plain @click="openCbbDlg">带入 CBB</el-button>
                    </div>
                    <div v-else>
                      <div v-if="!readOnly">
                        <el-button type="primary" @click="rejectBom">驳回重新编辑</el-button>
                      </div>
                    </div>
                  </div>
                  <div class="hr hr-6 dotted"></div>
                </div>
                <el-table
                    :data="snapshotItems || bomMaterialList"
                    stripe
                    @row-click="rowClick"
                    style="width: 100%">
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
                      show-overflow-tooltip
                      align="center"
                      label="物料编码">
                  </el-table-column>
                  <el-table-column
                      prop="name"
                      show-overflow-tooltip
                      align="center"
                      label="名称">
                  </el-table-column>
                  <el-table-column
                      prop="model"
                      :formatter="modelFormat"
                      show-overflow-tooltip
                      align="center"
                      label="型号">
                  </el-table-column>
                  <el-table-column
                      prop="unit"
                      width="100"
                      sortable
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
                      width="100"
                      sortable
                      show-overflow-tooltip
                      align="center"
                      label="数量">
                  </el-table-column>
                  <el-table-column
                      prop="preTaxSumPrice"
                      :formatter="sumPriceFormat"
                      width="120"
                      sortable
                      show-overflow-tooltip
                      align="center"
                      label="未税小计(元)">
                  </el-table-column>
                  <el-table-column v-if="bomEditable" label="操作" width="90" align="center">
                    <template slot-scope="scope">
                      <el-button v-if="isCbbLine(scope.row)" type="danger" plain size="mini" @click.stop="deleteCbbLine(scope.row)">删除</el-button>
                    </template>
                  </el-table-column>
                </el-table>
              </div>
            </div>
            <div class="clearfix form-actions">
              <div style="display: flex; justify-content: center;">
                <div style="display: flex; flex-direction: column; justify-content: center; align-items: center;">
                  <div>
                    <small> 说明: 特性评审之前, 请完成BOM的编辑工作, 并提交.</small>
                  </div>
                  <div style="margin-top:8px;">
                    <div v-if="!snapshotItems && characterInfo.bomStatus === CHARACTER_BOM_STATUS.EDIT.key">
                      <el-button type="primary" v-on:click="submitBom()">提交BOM</el-button>
                    </div>
                    <div v-else>
                      <el-button type="primary" plain disabled>提交BOM</el-button>
                    </div>
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
      <div>
        <BomEditDlg
            :key="dlgData.id"
            :bom-item="dlgData"
            :close-dlg = closeDialog
        ></BomEditDlg>
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
      <div>
        <BomAddDlg
            :key="random"
            :character-id="characterId"
            :close-dlg = closeDialog
        ></BomAddDlg>
      </div>
    </el-dialog>

    <!--BOM物料拣选-->
    <el-dialog
        width="29cm"
        :visible.sync="selectBomDlg"
        append-to-body
        :before-close="handleClose"
        :close-on-click-modal=false>
      <div slot="title" style="font-size: 20px;">{{ dialogTitle }}</div>
      <div>
        <BomSelectedListDlg
            :key="random"
            :character-id="characterId"
            :close-dlg = closeDialog
        ></BomSelectedListDlg>
      </div>
    </el-dialog>

    <!--带入软件模块（CBB）-->
    <el-dialog
        width="480px"
        :visible.sync="cbbDlg"
        append-to-body
        :close-on-click-modal=false>
      <div slot="title" style="font-size: 20px;">带入 CBB</div>
      <div>
        <el-select v-model="cbbIdToAdd" filterable placeholder="选择已发布的 CBB" style="width: 100%">
          <el-option v-for="item in cbbOptions" :key="item.id" :label="item.characterName" :value="item.id">
            <span>{{ item.characterName }}</span>
            <span v-if="item.sourceCharacterName" style="float: right; color: #909399; font-size: 12px;">承载特性：{{ item.sourceCharacterName }}</span>
          </el-option>
        </el-select>
        <div style="margin-top: 8px;">
          <small>CBB 作为软件物料清单的条目管理：集成特性清单中的 CBB 在提交物料清单时自动带入，也可从本机构已发布的 CBB 中手工带入。</small>
        </div>
      </div>
      <div slot="footer">
        <el-button @click="cbbDlg = false">取消</el-button>
        <el-button type="primary" :disabled="!cbbIdToAdd" @click="saveCbbToBom">确定</el-button>
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
import BomAddDlg from "@/components/std/bom-add-dlg.vue";
import BomSelectedListDlg from "@/components/std/bom-selected-list-dlg.vue";
import BomSnapshotSelect from "@/components/std/bom-snapshot-select.vue";

export default {
  components: {
    BomEditDlg,
    BomAddDlg,
    ApproveSignatureCore,
    JobitemDetailInfoDlg,
    BomSelectedListDlg,
    BomSnapshotSelect,
  },
  name: "bom-edit-list",

  props: {
    characterId: {
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

      dlgData: {},
      editBomDlg: false,
      addBomDlg: false,
      selectBomDlg: false,
      random: 0,
      dialogTitle: '',

      characterInfo: {},
      snapshotItems: null,

      APPLICATION_STATUS: APPLICATION_STATUS,
      CHARACTER_BOM_STATUS: CHARACTER_BOM_STATUS,

      cbbDlg: false,
      cbbOptions: [],
      cbbIdToAdd: '',
    };
  },

  computed: {
    bomEditable() {
      return !this.readOnly && (!this.characterInfo.bomStatus || this.characterInfo.bomStatus === CHARACTER_BOM_STATUS.EDIT.key);
    },
  },

  mounted: function () {
    let _this = this;
    _this.getLoginUser();
    if (Tool.isNotEmpty(_this.characterId)) {
      _this.getCharacterSimpleInfo(_this.characterId);
      _this.getBomMaterialListByCharacterId(_this.characterId);
    }

    _this.$bus.$on('refresh-bom-edit-list', _this.onRefreshBom);
  },

  beforeDestroy() {
    let _this = this;
    _this.$bus.$off('refresh-bom-edit-list', _this.onRefreshBom);
  },

  methods: {
    onRefreshBom(data) {
      if (data) {
        this.getBomMaterialListByCharacterId(this.characterId);
      }
    },

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

    onSnapshotChange(items) {
      this.snapshotItems = items;
    },

    rowClick(row) {
      let _this = this;
      if (_this.snapshotItems || _this.isCbbLine(row)) {
        return;
      }
      if(_this.characterInfo && _this.characterInfo.bomStatus === CHARACTER_BOM_STATUS.EDIT.key){
        _this.dlgData = row;
        _this.editBomDlg = true;
      }
      _this.dialogTitle = "BOM编辑";
    },

    addBomItem() {
      let _this = this;
      _this.random = Math.random();
      if(_this.characterInfo && _this.characterInfo.bomStatus === CHARACTER_BOM_STATUS.EDIT.key){
        _this.addBomDlg = true;
      }
      _this.dialogTitle = "添加BOM物料";
    },

    selectBomItem() {
      let _this = this;
      _this.random = Math.random();
      _this.selectBomDlg = true;
      _this.dialogTitle = "BOM物料拣选";
    },

    closeDialog() {
      let _this = this;
      _this.editBomDlg = false;
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

    modelFormat(row) {
      if (this.isCbbLine(row)) {
        return row.cbbDevVersion ? '发布版本 ' + row.cbbDevVersion : '';
      }
      return row.model;
    },

    openCbbDlg() {
      let _this = this;
      _this.cbbIdToAdd = '';
      _this.cbbDlg = true;
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/cbb/getCbbListByCustomerId/' + _this.loginCustomer.id).then((response) => {
        let resp = response.data;
        if (resp.success) {
          _this.cbbOptions = resp.content || [];
        } else {
          Toast.warning(resp.message, Swal);
        }
      });
    },

    saveCbbToBom() {
      let _this = this;
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/material/saveCbbToBom/' + _this.characterId + '/' + _this.cbbIdToAdd).then((response) => {
        let resp = response.data;
        if (resp.success) {
          _this.cbbDlg = false;
          _this.getBomMaterialListByCharacterId(_this.characterId);
          Toast.success("保存成功", Swal);
        } else {
          Toast.warning(resp.message, Swal);
        }
      });
    },

    deleteCbbLine(row) {
      let _this = this;
      Swal.fire({
        title: '确认',
        text: "删除后提交物料清单时不再自动带入该 CBB，删除记入特性过程记录。确认删除吗?",
        icon: 'warning',
        showCancelButton: true,
        confirmButtonColor: '#409EFF',
        cancelButtonColor: '#909399',
        confirmButtonText: '确认!',
        cancelButtonText: '取消!'
      }).then((result) => {
        if (result.isConfirmed) {
          _this.$ajax.delete(process.env.VUE_APP_SERVER + '/customer/material/delete/' + row.id).then((response) => {
            let resp = response.data;
            if (resp.success) {
              _this.getBomMaterialListByCharacterId(_this.characterId);
              Toast.success("删除成功", Swal);
            } else {
              Toast.warning(resp.message, Swal);
            }
          });
        }
      }).catch(_ => {
      });
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

    submitBom() {
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
          _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/material/submitBom/'+ _this.characterId).then((response) => {
            let resp = response.data;
            if (resp.success) {
              _this.getCharacterSimpleInfo(_this.characterId);
              _this.$bus.$emit('refresh-review-create-page', true);
              Toast.success("保存成功", Swal);
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

    rejectBom() {
      let _this = this;
      Swal.fire({
        title: '驳回物料清单',
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
          _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/material/rejectBom/'+ _this.characterId, {
            rejectReason: result.value.trim(),
          }).then((response) => {
            let resp = response.data;
            if (resp.success) {
              _this.getCharacterSimpleInfo(_this.characterId);
              _this.$bus.$emit('refresh-review-create-page', true);
              Toast.success("保存成功", Swal);
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
  },
}
</script>

<style lang="scss" scoped>
@import './../../../public/static/scss/caipd';

.paper-height {
  height: calc(100vh - 200px);
}

</style>
