<!--
 * Author: 周朔鹏
 * Date: 2025-03-06
 * Email: 391902958@qq.com
 * Website: https://www.euipd.com
-->

<template>
  <div>
    <div class="dialog-container">
      <div style="display: flex; flex-direction: row; justify-content: center; align-items: center;">
        <div style="display: flex; flex-direction: row; justify-content: left; align-items: center; margin-right: 50px;">
          <div style="margin-right: 10px; font-weight: 500;">
            申请单号:
          </div>
          <div>
            {{ dialogInfo.code }}
          </div>
        </div>
        <div style="display: flex; flex-direction: row; justify-content: left; align-items: center;">
          <div style="margin-right: 10px; font-weight: 500;">
            任务名称:
          </div>
          <div>
            {{ jobItem.jobName }}
          </div>
        </div>
      </div>
      <div class="hr hr-12 hr-dotted"></div>
      <div style="display: flex; flex-direction: row; justify-content: center; align-items: center;">
        <div style="font-weight: 500; font-size: 14px; margin-right: 10px;">
          物料检索:
        </div>
          <!--        v-select-load-more="handleScroll"-->
        <div>
          <el-select
              v-model="selected"
              filterable
              :filter-method="selectedChange"
              @change="handleChange"
              placeholder="您可以输入物料号 物料名称 规格型号 关键字进行检索"
              style="width: 400px;">
            <el-option
                v-for="item in filteredMaterialNo"
                :key="item.materialNo"
                :label="item.materialItem"
                :value="item.materialNo">
            </el-option>
          </el-select>
        </div>
      </div>
      <div style="display: flex; flex-direction: row; justify-content: center; align-items: center; font-weight: 250">
        <small><em>注意: 下拉列表最多显示100条数据, 超过100条数据, 请用查询条件进行过滤.</em></small>
      </div>
      <el-form
          label-width="130px"
          :show-message=false
          class="dialog-width">
        <div class="hr hr-6 dotted"></div>
        <el-row>
          <div style="margin: 0 20px 20px 20px;">
            <el-table
                :data="materialList"
                stripe
                style="width: 100%">
              <el-table-column label="序号" align="center" width="80">
                <template slot-scope="scope">
                  {{ scope.$index + 1 }}
                </template>
              </el-table-column>
              <el-table-column
                  prop="bomCode"
                  align="center"
                  width="200"
                  show-overflow-tooltip
                  label="物料代码">
              </el-table-column>
              <el-table-column
                  prop="name"
                  align="center"
                  width="200"
                  show-overflow-tooltip
                  label="物料描述">
              </el-table-column>
              <el-table-column
                  prop="model"
                  align="center"
                  width="200"
                  show-overflow-tooltip
                  label="规格型号">
              </el-table-column>
              <el-table-column
                  prop="numInWarehouse"
                  align="center"
                  width="100"
                  label="库存数量">
              </el-table-column>
              <el-table-column
                  prop="amount"
                  align="center"
                  width="100"
                  label="领用数量">
                <template slot-scope="scope">
                  <el-input type="number" v-model="scope.row.amount" @change="amountChange(scope.row)" size="small"></el-input>
                </template>
              </el-table-column>
              <el-table-column
                  prop="unit"
                  align="center"
                  width="80"
                  label="计量单位">
              </el-table-column>
              <el-table-column
                  prop="warehouseCode"
                  align="center"
                  width="140"
                  show-overflow-tooltip
                  label="仓库">
                <template slot-scope="scope">
                  {{ scope.row.warehouseName || scope.row.warehouseCode }}
                </template>
              </el-table-column>
              <el-table-column
                  prop="batchCode"
                  align="center"
                  width="130"
                  label="批次号/序列号">
                <template slot-scope="scope">
                  <el-input v-model="scope.row.batchCode" size="small"></el-input>
                </template>
              </el-table-column>
              <el-table-column
                  fixed="right"
                  label="操作"
                  align="center"
                  width="110">
                <template slot-scope="scope">
                  <div>
                    <el-button type="primary" plain size="small" v-on:click.stop="deleteItem(scope.$index)">
                      删除
                    </el-button>
                  </div>
                </template>
              </el-table-column>
            </el-table>
          </div>
        </el-row>
        <el-row>
          <el-col :span="10">
            <el-form-item label="申请标题" required>
              <el-input type="text" v-model="dialogInfo.name"></el-input>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="22">
            <el-form-item label="使用方式" required>
              <el-col :span="24">
                <el-select v-model="dialogInfo.usageMode" filterable placeholder="使用方式">
                  <el-option v-for="o in MATERIAL_USAGE_MODE" :key="o.key" :value="o.key" :label="o.value"></el-option>
                </el-select>
              </el-col>
              <el-col :span="24">
                <div v-if="dialogInfo.usageMode===MATERIAL_USAGE_MODE.EXPENDITURE.key">
                  说明: "费用化"是指上述物料将在研发工作中消耗掉, <strong>作为项目成本记账</strong>.
                </div>
                <div v-if="dialogInfo.usageMode===MATERIAL_USAGE_MODE.CAPITALIZATION.key">
                  说明: "资产化"是指上述物料将被转化为公司固定资产或者成本被开发的产品的一部分. <strong>领料时暂时不计入成本</strong> 如果转化为公司固定资产, 将在评审前归还, 由公司资产部门确认; 如果作为被开发产品的一部分, 则将在评审前, 交付给相关部门, 由接收部门核验; 如果项目宣告研发失败, 则需要评估上述物料是否可回收, 由相关部门评审后, 决定项目物料成本计入.
                </div>
                <div v-if="dialogInfo.usageMode===MATERIAL_USAGE_MODE.BORROW.key">
                  说明: "借用"是指上述物料将被研发人员临时使用. 主要指相关工具、设备、临时物料等. <strong>领料时暂时不计入成本</strong> 上述物料将在评审前归还, 根据实际情况和相关公司制度, 确定损耗或折旧后, 进行成本入账. 一般要求, A项目评审前, 需归还A项目所借用的项目物资, B项目再用时, 在B项目下重新借用, 以便成本归集和管理.
                </div>
              </el-col>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="22">
            <el-form-item label="领用事由" required>
              <el-input type="textarea" rows="5" v-model="dialogInfo.reason"></el-input>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :offset="10" :span="6">
            <el-form-item label="申请人">
              <el-col :span="24">
                <el-input type="text" style="font-size: 14px;" :value="jobItem.executorName" readonly></el-input>
              </el-col>
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="申请时间">
              <el-col :span="24">
                <el-input type="text" style="font-size: 14px;" :value="getNowTime" readonly></el-input>
              </el-col>
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <div class="clearfix form-actions">
        <div style="display: flex; justify-content: center;">
          <button v-if="getMaterialListStatus"  type="button" v-on:click="application()" class="ca-btn-primary">提 交</button>
          <button v-if="! getMaterialListStatus"  type="button" class="ca-btn-primary-disable">提 交</button>
          <button type="button" v-on:click="close()" class="ca-btn-primary">取 消</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import Swal from "sweetalert2";
import {dateFormat, optionKV} from "@/filter/filter";
import MaterialAddDlg from "@/components/std/material-add-dlg.vue";
import ExcelMaterialListUpload from "@/components/rdms/excel-material-list-upload.vue";
import {Loading} from "element-ui";
import {webAssetUrl} from "@/shared/oss";

export default {
  components: {
    ExcelMaterialListUpload,
    MaterialAddDlg,
  },
  name: "material-application-dlg-sap",

  props: {
    jobitemId: {
      default: null
    },
    materialListIn: {
      default: () => [] // 使用工厂函数返回默认值
    },
    applicationInfo: {
      default: null
    },
    inheritJobType: {
      default: null
    },
    projectType: {
      default: null
    },
    closeDlg: {
      type: Function,
      default: null
    },
  },

  data() {
    return {
      dialogInfo: {
        code: '',
      },

      selected: '',
      selected1: '',
      selected2: '',

      loadingMore: false,
      page: 1,
      pageSize: 10,

      materialList: [],
      sapMaterialList: [],
      uniqueWarehouse: [],
      filteredMaterialNoList: [],
      filteredMaterialNo: [],
      materialFilterList: [],

      materialSelectItem: [],

      MATERIAL_USAGE_MODE: MATERIAL_USAGE_MODE,

      random: 0,

      loginUser: {},
      loginCustomer: {},
      jobItem: {},

      timeStamp: 0,
      templatePath: webAssetUrl("import_material_list.xlsx"),
    };
  },

  computed: {
    getNowTime(){
      return dateFormat(new Date());
    },
    getMaterialListStatus() {
      let _this = this;
      return Tool.isNotEmpty_ES5(_this.materialList);
    },
  },

  mounted: function () {
    let _this = this;
    if (_this.applicationInfo == null || _this.applicationInfo?.code == null) {
      try {
        _this.dialogInfo = {..._this.dialogInfo, code: Uuid.orderNo()};
      } catch (error) {
        console.error("Failed to generate orderNo:", error);
      }
    } else {
      _this.dialogInfo = {..._this.applicationInfo};
    }

    _this.timeStamp = Date.now();

    _this.materialList = _this.materialListIn;

    _this.getLoginUser();
    if(Tool.isNotEmpty(_this.jobitemId)){
      _this.getJobitemSimpleInfo(_this.jobitemId);
    }
  },

  methods: {
    getLoginUser(){
      let _this = this;
      if(Tool.getLoginUser().userGroup === LOGIN_USER_GROUP.CUSTOMER.key){
        _this.loginUser = Tool.getLoginUser().adminUser;
        _this.loginCustomer =Tool.getLoginUser();
      }else{
        _this.loginUser = Tool.getLoginUser();
        _this.loginCustomer = Tool.getLoginUser().customer;
      }

      _this.getSAPMaterialList();
    },

    amountChange(material){
      let _this = this;
      if(material.amount && material.numInWarehouse && Number(material.amount) > Number(material.numInWarehouse)){
        Toast.warning("库存不足：" + material.bomCode + " 库存 " + material.numInWarehouse + "，领用数量已调整为库存数量", Swal);
        material.amount = material.numInWarehouse;
      }
    },

    handleChange(value) {
      let _this = this;
      let materialNo = value.split(":")[0].trim();
      let warehouseNo = value.split(":")[1].trim();

      _this.materialSelectItem = _this.sapMaterialList.filter(item =>
          item.materialNo.trim() === materialNo && item.warehouseNo.trim() === warehouseNo
      );
      let material = {};
      material.code = _this.dialogInfo.code;
      if(_this.materialSelectItem[0].materialNo){
        material.bomCode = _this.materialSelectItem[0].materialNo;
      }else{
        material.bomCode = null;
      }

      material.name = _this.materialSelectItem[0].materialName;
      material.model = _this.materialSelectItem[0].materialSpcf;
      material.warehouseCode = _this.materialSelectItem[0].warehouseNo;
      material.warehouseName = _this.materialSelectItem[0].warehouseName;
      material.batchCode = _this.materialSelectItem[0].batchCodeSno;
      material.numInWarehouse = _this.materialSelectItem[0].num;
      material.amount = 1;
      material.unit = _this.materialSelectItem[0].unit;
      _this.materialList.push(material);

      _this.selected = '';
    },

    selectedChange(query) {
      let _this = this;
      if (!query) {
        return this.filteredMaterialNoList;
      }
      const lowerCaseSearchString = query.toLowerCase();
      _this.materialFilterList = this.filteredMaterialNoList.filter(item => {
        return item.materialNo.toLowerCase().includes(lowerCaseSearchString)
            || item.materialItem.toLowerCase().includes(lowerCaseSearchString);
      });
      if(_this.materialFilterList.length > 100){
        _this.filteredMaterialNo = _this.materialFilterList.slice(0, 100);
      }else{
        _this.filteredMaterialNo = _this.materialFilterList;
      }
      // _this.filteredMaterialNo = _this.materialFilterList.slice((_this.page - 1) * _this.pageSize, _this.page * _this.pageSize);
    },

    handleScroll() {
        this.loadingMore = true;
        this.page++;
        this.fetchData();
    },
    fetchData() {
      let _this = this;
      // _this.filteredMaterialNo = _this.filteredMaterialNoList.slice((_this.page - 1) * _this.pageSize, _this.page * _this.pageSize);
      _this.filteredMaterialNo = _this.filteredMaterialNoList.slice(0 , 100);
      this.loadingMore = false;
    },

    getJobitemSimpleInfo(jobitemId){
      let _this = this;
      let formData = new window.FormData();
      formData.append('jobitemId', jobitemId);
      formData.append('loginUserId', _this.loginUser.customerUserId);
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/job-items/getJobitemSimpleInfo', formData).then((response) => {
        let resp = response.data;
        if (resp.success) {
          _this.jobItem = resp.content;
        }else{
          Toast.warning(resp.message, Swal);
        }
      });
    },

    downloadFile(path) {
      // let path = "'http://127.0.0.1:18100/euipd-web/fm4ek0Zy/system/tHnkCpKT-import_workers_template.xlsx'";  // 必须使用变量, 静态值会提示"无法从网站提取文件"
      let fileName = "import_workers_template.xlsx";
      window.open(path, '_self');
      // FileSaver.saveAs(path, fileName);
    },

    getSAPMaterialList() {
      let _this = this;
      let loadingInstance = Loading.service({ fullscreen: true });
      let BOX = document.querySelector(".el-loading-mask");
      if (_this.isHTMLElement(BOX)) {
        (BOX).style.zIndex = "999999";
      } else {
        console.log('element is not an HTMLElement');
      }
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/material/getSAPMaterialList/' + _this.loginCustomer.id).then((response) => {
        let resp = response.data;
        loadingInstance.close();
        if (resp.success) {
          _this.sapMaterialList = resp.content;
          _this.filteredMaterialNoList = _this.sapMaterialList.map(item => ({
            materialNo: item.materialNo+ ":" + item.warehouseNo/* + ":" + item.batchCodeSno*/,
            materialItem: item.materialNo + "  " + item.materialName + (item.materialSpcf ? " / " + item.materialSpcf : "")
                + "（" + (item.warehouseName || item.warehouseNo) + "，库存 " + item.num + "）",
          }));

          _this.fetchData();

        } else {
          Toast.warning(resp.message, Swal);
        }
      });
    },
    isHTMLElement(element) {
      return element instanceof HTMLElement;
    },

    deleteItem(index){
      let _this = this;
      _this.materialList.splice(index, 1);
    },
    /**
     * 提交申请
     */
    application() {
      let _this = this;
      // 保存校验
      if (1 !== 1
          || !Validator.require(_this.dialogInfo.code, "申请单号",Swal)
          || !Validator.require(_this.dialogInfo.name, "申请标题",Swal)
          || !Validator.require(_this.dialogInfo.reason, "领用事由",Swal)
          || !Validator.require(_this.dialogInfo.usageMode, "使用方式",Swal)

          || !Validator.length(_this.dialogInfo.reason, "领用事由", 6, 800,Swal)
          || !Validator.length(_this.dialogInfo.usageMode, "使用方式", 2, 20,Swal)
          || !Validator.length(_this.dialogInfo.name, "申请标题", 2, 30,Swal)
      ) {
        return;
      }

      _this.dialogInfo.customerId = _this.jobItem.customerId;
      _this.dialogInfo.preProjectId = _this.jobItem.preProjectId;
      _this.dialogInfo.projectId = _this.jobItem.projectId;
      _this.dialogInfo.subprojectId = _this.jobItem.subprojectId;
      _this.dialogInfo.characterId = _this.jobItem.characterId;
      _this.dialogInfo.jobitemId = _this.jobitemId;
      _this.dialogInfo.writerId = _this.loginUser.customerUserId;
      _this.dialogInfo.jobType = _this.jobItem.type;

      if(_this.jobItem.type===JOB_ITEM_TYPE.QUALITY.key || _this.inheritJobType===JOB_ITEM_TYPE.QUALITY.key){
        _this.dialogInfo.nextNode = _this.jobItem.productManagerId;
        _this.dialogInfo.qualityId = _this.jobItem.qualityId;

      }else if(_this.jobItem.type===JOB_ITEM_TYPE.TASK_BUG.key || _this.inheritJobType===JOB_ITEM_TYPE.TASK_BUG.key){
        _this.dialogInfo.nextNode = _this.jobItem.testManagerId;
        _this.dialogInfo.qualityId = _this.jobItem.qualityId;  //共用的 bugId
        _this.dialogInfo.inheritJobType = _this.inheritJobType;

      }else if(_this.jobItem.type===JOB_ITEM_TYPE.TASK_FUNCTION.key){
        _this.dialogInfo.nextNode = _this.jobItem.sysgmId;  //系统工程阶段,所有的物料审批都是 系统工程总监

      }else if(_this.inheritJobType===JOB_ITEM_TYPE.TASK_FUNCTION.key){
        _this.dialogInfo.nextNode = _this.jobItem.sysgmId;  //系统工程阶段,所有的物料审批都是 系统工程总监

      }else if(_this.inheritJobType===JOB_ITEM_TYPE.TASK_PRODUCT.key){
        _this.dialogInfo.nextNode = _this.jobItem.productManagerId;

      }else if(_this.jobItem.type===JOB_ITEM_TYPE.TASK_PRODUCT.key){
        _this.dialogInfo.nextNode = _this.jobItem.productManagerId;

      }else if(_this.jobItem.type===JOB_ITEM_TYPE.TASK_TEST.key){
        _this.dialogInfo.nextNode = _this.jobItem.projectManagerId;

      }else{
        _this.dialogInfo.nextNode = _this.jobItem.projectManagerId;
      }
      _this.dialogInfo.status = APPLICATION_STATUS.APPLICATION.key;

      //检查_this.materialList的每一项的amount属性都不能小于0
      if(_this.materialList.some(item => item.amount <= 0)){
        Toast.warning("数量不能小于1", Swal);
        return;
      }
      _this.dialogInfo.materialList = _this.materialList;
      _this.dialogInfo.projectType = _this.projectType;

      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/material/saveMaterialApplication', _this.dialogInfo).then((response)=>{
        let resp = response.data;
        if (resp.success) {
          _this.close();
          _this.$bus.$emit('refresh-material-application-list', true);
          _this.$bus.$emit('notify-quality-material-application-num', true);
          _this.$bus.$emit('refresh-manhour-num', true);
          Toast.success("申请已发出", Swal);
        } else {
          Toast.warning(resp.message, Swal);
        }
      })
    },

    close(){
      let _this = this;
      _this.closeDlg();
    },

  },
}
</script>

<style lang="scss" scoped>
@import '../../../public/static/scss/caipd';

.text-box{
  display: inline-block;
  padding-left: 3px;
  padding-right: 15px;
  line-height: 30px;
  //width: 100px;
  background-color: rgba(204, 204, 204, 0.20);
}
.dialog-width{
  width: 100%;
}
.red-color{
  color: red;
}
</style>
