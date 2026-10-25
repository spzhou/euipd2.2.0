<!--
 * Author: 周朔鹏
 * Date: 2024-05-23
 * Email: 391902958@qq.com
 * Website: https://www.euipd.com
-->

<template>
  <div>
    <el-button type="primary" size="small" v-on:click="selectFile()" style="width: 150px;">
      <i class="ace-icon fa fa-upload"></i>
      {{text}}
    </el-button>
    <input class="hidden" type="file" ref="file" v-on:change="uploadFile()" v-bind:id="inputId+'-input'">
  </div>
</template>

<script>
  import Swal from "sweetalert2";
  import $ from 'jquery'
  export default {
    name: 'excel-material-list-upload',
    props: {
      text: {
        default: "上传文件"
      },
      inputId: {
        default: "file-upload"
      },
      applicationCode: {
        default: null
      },
      materialList: {
        default: null
      },
    },
    data: function () {
      return {
        loginUser: {},
        loginCustomer: {},

        sheetContent: [],

        dialogInfo: {
          id: '',
          code: '',
          bomCode: '',
          name: '',
          model: '',
          unit: '',
          amount: '',
        },
        materialApplicationList: [],
      }
    },

    mounted: function() {
      let _this = this;
      _this.getLoginUser();

      _this.materialApplicationList = _this.materialList;
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
      },

      selectFile () {
        let _this = this;
        $("#" + _this.inputId + "-input").trigger("click");
      },

      uploadFile () {
        let _this = this;
        let file = _this.$refs.file.files[0];
        if(file.name === "import_material_list.xlsx"){
          _this.readMaterialListFromLocalFile(file, _this.readMaterialList)
        }else{
          Toast.error("文件名称必须为:import_material_list.xlsx", Swal);
        }
      },

      // 读取本地excel文件
      readMaterialListFromLocalFile(file, callback) {
        var reader = new FileReader();
        reader.readAsBinaryString(file);
        reader.onload = async function (e) {
          var data = e.target.result;
          const XLSX = await import(/* webpackChunkName: "xlsx" */ 'xlsx');
          var materialList = XLSX.read(data, {type: 'binary'});
          if (callback) callback(materialList, XLSX);
        };
      },

      readMaterialList(materialList, XLSX) {
        let _this = this;
        var sheetContent = []
        // 遍历每张表读取
        for (var sheet in materialList.Sheets) {
          if (materialList.Sheets.hasOwnProperty(sheet)) {
            let fromTo = materialList.Sheets[sheet]['!ref'];
            sheetContent.push(XLSX.utils.sheet_to_json(materialList.Sheets[sheet]))
            break; // 如果有多张表，就注释掉这行
          }
        }
        _this.sheetContent = sheetContent[0];

        _this.saveMaterialList(_this.sheetContent);
      },

      saveMaterialList(materialItems) {
        let _this = this;
        let materialList = [];
        if(materialItems && materialItems.length>0){
          materialItems.forEach((materialItem, index) => {
            if(materialItem.name
                && materialItem.bomCode
                && materialItem.model
                && materialItem.unit
                && materialItem.amount
            ){
              materialList.push(materialItem);
            }
          });
          _this.addMaterialItemList(materialList)
        }
      },

      addMaterialItemList(materialList){
        let _this = this;
        materialList.forEach(item => {
          if (1 !== 1
              || !Validator.require(item.name, "物料名称",Swal, true)
              || !Validator.require(item.bomCode, "物料编号",Swal, true)
              || !Validator.require(item.model, "物料型号",Swal, true)
              || !Validator.require(item.unit, "计量单位",Swal, true)
              || !Validator.require(item.amount, "领用数量",Swal, true)

              || !Validator.length(item.name, "物料名称", 2, 80,Swal, true)
              || !Validator.length(item.bomCode, "物料编号", 2, 50,Swal, true)
              || !Validator.length(item.model, "物料型号", 2, 50,Swal, true)
              || !Validator.length(item.unit, "计量单位", 1, 10,Swal, true)
              || !Validator.length(item.amount, "领用数量", 1, 10,Swal, true)

              || !Validator.greatThanZero(item.amount, "领用数量", Swal, true)
          ) {
            return;
          }
          item.code = _this.applicationCode;
          item.id = Uuid.shortUuid(8);
          _this.materialApplicationList.push(item);
        })
        this.$emit('materialApplicationList', _this.materialApplicationList);  //监听 事件
      }

    }
  }
</script>
