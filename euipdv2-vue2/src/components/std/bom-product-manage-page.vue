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
        <div class="a4-paper-big-simple" style="width: 30cm;">
          <div>
            <div style="margin: 40px 30px;">
              <div>
                <div class="ca-form-header">
                  <div style="display: flex; flex-direction: row; justify-content: space-between; align-items: center;">
                    <div style="font-size: 14px; font-weight: bold; margin: 15px 0">
                      BOM清单
                    </div>
                    <div style="display: flex; flex-direction: row; justify-content: center; align-items: center;">
                      <el-button type="primary" size="mini" @click="addBomItem">添加BOM物料</el-button>
                      <el-button type="primary" size="mini" @click="structureBom">BOM结构化编辑</el-button>
                    </div>
                  </div>
                  <div class="hr hr-6 dotted"></div>
                </div>
                <el-table
                    :data="bomMaterialList"
                    stripe
                    show-summary
                    :summary-method="getSummaries"
                    style="width: 100%">
                  <el-table-column label="序号" align="center" width="50">
                    <template slot-scope="scope">
                      {{ scope.$index + 1 }}
                    </template>
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
                      show-overflow-tooltip
                      align="center"
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
                  <el-table-column
                      fixed="right"
                      label="操作"
                      align="center"
                      width="130">
                    <template slot-scope="scope">
                      <div style="display: flex; flex-direction: row; justify-content: center; align-items:center;">
                        <el-button @click.stop="edit(scope.row)" type="primary" size="mini">编辑</el-button>
                        <el-button @click.stop="deleteItem(scope.row.id)" type="primary" size="mini">删除</el-button>
                      </div>
                    </template>
                  </el-table-column>
                </el-table>
              </div>
            </div>
            <div v-if="false" class="clearfix form-actions">
              <div style="display: flex; justify-content: center;">
                <div style="display: flex; flex-direction: column; justify-content: center; align-items: center;">
                  <div>
                    <small> 提示: 修改BOM前保存副本, 以便留存历史记录, 或在某种情况下, 恢复之前的BOM. 本功能为高级版本功能!</small>
                  </div>
                  <div style="margin-top:8px;">
                    <div>
                      <el-button type="primary" plain disabled v-on:click="''">保存BOM副本</el-button>
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
      <div v-if="editBomDlg">
        <BomEditProductDlg
            :key="dlgData.id"
            :bom-item="dlgData"
            :project-id="projectId"
            :close-dlg = closeDialog
        ></BomEditProductDlg>
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
        <BomAddProjectDlg
            :key="random"
            :project-id="projectId"
            :close-dlg = closeDialog
        ></BomAddProjectDlg>
      </div>
    </el-dialog>

    <!--BOM物料结构化编辑-->
    <el-dialog
        width="32cm"
        :visible.sync="structureBomDlg"
        append-to-body
        :before-close="handleClose"
        :close-on-click-modal=false>
      <div slot="title" style="font-size: 20px;">{{ dialogTitle }}</div>
      <div v-if="structureBomDlg">
        <SubprojectBomStructure
            ref="refSubprojectBomStructure"
            :key="projectId.id"
            :subproject-id="projectId"
            :is-main-project="true"
            :is-dlg="true"
            :close-dlg = closeDialog
        ></SubprojectBomStructure>
      </div>
      <div style="height: 20px"></div>
    </el-dialog>


  </div>
</template>

<script>
import Swal from "sweetalert2";
import JobitemDetailInfoDlg from "@/components/std/jobitem-detail-info-dlg.vue";
import {optionKV, rmbFormat} from "@/filter/filter";
import ApproveSignatureCore from "@/components/rdms/approve-signature-core.vue";
import BomEditProductDlg from "@/components/std/bom-edit-product-dlg.vue";
import BomAddProjectDlg from "@/components/std/bom-add-project-dlg.vue";
import SubprojectBomStructure from "@/components/rdms/subproject-bom-structure.vue";
import subprojectBomStructure from "@/components/rdms/subproject-bom-structure.vue";

export default {
  components: {
    BomEditProductDlg,
    BomAddProjectDlg,
    ApproveSignatureCore,
    JobitemDetailInfoDlg,
    SubprojectBomStructure
  },
  name: "bom-product-manage-page",

  props: {
    projectId: {
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

      dlgData: {},
      editBomDlg: false,
      addBomDlg: false,
      structureBomDlg: false,
      selectBomDlg: false,
      random: 0,
      dialogTitle: '',

      characterInfo: {},
      subProject: {},

      APPLICATION_STATUS: APPLICATION_STATUS,
      CHARACTER_BOM_STATUS: CHARACTER_BOM_STATUS,
    };
  },

  computed: {
    subprojectBomStructure() {
      return subprojectBomStructure
    }
  },

  mounted: function () {
    let _this = this;
    _this.getLoginUser();
    if (Tool.isNotEmpty(_this.projectId)) {
      _this.getSubProjectInfo(_this.projectId);
      _this.getBomMaterialListByProjectId(_this.projectId);
    }
    _this.$bus.$on('refresh-bom-product-manage-page', (data) => {
      if(data){
        _this.getBomMaterialListByProjectId(_this.projectId);
      }
    });

  },

  beforeDestroy() {
    let _this = this;
    _this.$bus.$off('refresh-bom-product-manage-page');
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

    edit(row) {
      let _this = this;
      if(_this.subProject.bomStatus || _this.subProject.bomStatus === CHARACTER_BOM_STATUS.EDIT.key){
        _this.dlgData = row;
        _this.editBomDlg = true;
        _this.dialogTitle = "BOM编辑";
      }
    },

    addBomItem() {
      let _this = this;
      _this.random = Math.random();
      _this.addBomDlg = true;
      _this.dialogTitle = "添加BOM物料";
    },

    structureBom() {
      let _this = this;
      _this.random = Math.random();
      _this.structureBomDlg = true;
      _this.dialogTitle = "BOM物料结构化编辑";
    },

    closeDialog() {
      let _this = this;
      _this.editBomDlg = false;
      _this.addBomDlg = false;
      _this.structureBomDlg = false;
      _this.selectBomDlg = false;
    },
    handleClose(done) {
      let _this = this;
      //对窗口中的数据进行保存
      Swal.fire({
        title: '请确认',
        text: '是否保存对BOM结构的编辑?',
        icon: 'question',
        showCancelButton: true,
        confirmButtonColor: '#409EFF',
        cancelButtonColor: '#909399',
        confirmButtonText: '确认!',
        cancelButtonText: '取消!'
      }).then((result) => {
        if (result.value) {
          const refSubprojectBomStructure = this.$refs.refSubprojectBomStructure;
          if (refSubprojectBomStructure && typeof refSubprojectBomStructure.saveBomTree === 'function') {
            try {
              refSubprojectBomStructure.saveBomTree();
            } catch (error) {
              console.error('Failed to save BOM tree:', error);
            }
          } else {
            console.warn('refSubprojectBomStructure is not available or saveBomTree method is not defined');
          }
        }
      })
      //执行默认行为
      done();
    },

    unitPriceFormat(row, column) {
      if (row.preTaxUnitPrice) {
        return rmbFormat(row.preTaxUnitPrice);
      }
      return 0;
    },

    sumPriceFormat(row, column) {
      if (row.preTaxSumPrice) {
        return rmbFormat(row.preTaxSumPrice);
      }
      return 0;
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
              _this.getBomMaterialListByProjectId(_this.projectId);
              Toast.success("删除成功", Swal);
            }
          });
        }
      })
    },

    getBomMaterialListByProjectId(projectId) {
      let _this = this;
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/material/getBomMaterialListByProjectId/' + projectId).then((response) => {
        let resp = response.data;
        if (resp.success) {
          _this.bomMaterialList = resp.content;
        } else {
          Toast.warning(resp.message, Swal);
        }
      });
    },

    getSummaries(param) {
      let _this = this;
      const { columns, data } = param;
      const sums = [];
      columns.forEach((column, index) => {
        if (index === 1) {
          sums[index] = '总计';
          return;
        }
        if (index === 0 || index === 2 || index === 3 || index === 4 || index === 5 || index === 6 || index === 7 || index === 9) {
          sums[index] = '';
          return;
        }
        const values = data.filter(item => item.itemType !== BOM_ITEM_TYPE.CBB.key).map(item => Number(item[column.property]));
        if (!values.every(value => isNaN(value))) {
          sums[index] = values.reduce((prev, curr) => {
            const value = Number(curr);
            if (!isNaN(value)) {
              return prev + curr;
            } else {
              return prev;
            }
          }, 0);
          if(index === 8){
            sums[index] = rmbFormat(sums[index]);
          }
        } else {
          sums[index] = 'N/A';
        }
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
