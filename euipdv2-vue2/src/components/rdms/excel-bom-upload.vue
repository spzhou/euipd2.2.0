<!--
 * Author: 周朔鹏
 * Date: 2024-12-22
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
    name: 'excel-bom-upload',
    props: {
      text: {
        default: "上传文件"
      },
      inputId: {
        default: "file-upload"
      },
      subprojectId: {
        default: null,
      },
      refreshFunction: {
        type: Function,
        default: null
      },
    },
    data: function () {
      return {
        loginUser: {},
        loginCustomer: {},

        sheetContent: [],
      }
    },

    mounted: function() {
      let _this = this;
      _this.getLoginUser();
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
        if(file.name === "import_bom.xlsx"){
          _this.readBomListFromLocalFile(file, _this.readBomList)
        }else{
          Toast.error("文件名称必须为:import_bom.xlsx", Swal);
        }
      },

      // 读取本地excel文件
      readBomListFromLocalFile(file, callback) {
        var reader = new FileReader();
        reader.readAsBinaryString(file);
        reader.onload = async function (e) {
          var data = e.target.result;
          const XLSX = await import(/* webpackChunkName: "xlsx" */ 'xlsx');
          var bomList = XLSX.read(data, {type: 'binary'});
          if (callback) callback(bomList, XLSX);
        };
      },

      readBomList(bomList, XLSX) {
        let _this = this;
        if(bomList.length === 0){
          _this.refreshFunction();
          Toast.warning("表格内容有误,无法正确识别!", Swal);
          return;
        }
        var sheetContent = []
        // 遍历每张表读取
        for (var sheet in bomList.Sheets) {
          if (bomList.Sheets.hasOwnProperty(sheet)) {
            let fromTo = bomList.Sheets[sheet]['!ref'];
            sheetContent.push(XLSX.utils.sheet_to_json(bomList.Sheets[sheet]))
            break; // 如果有多张表，就注释掉这行
          }
        }
        _this.sheetContent = sheetContent[0];

        _this.saveBomList(_this.sheetContent);
      },

      saveBomList(bomItems) {
        let _this = this;
        if(bomItems.length === 0){
          _this.refreshFunction();
          Toast.warning("表格内容有误,无法正确识别!", Swal);
          return;
        }
        let bomList = [];
        if(bomItems && bomItems.length>0){
          for(let i=0; i < bomItems.length; i++){
            let item = bomItems[i];
            if(Tool.isNotEmpty(item.bomCode)){
              if(Tool.isNotEmpty(item.name)){
                if(Tool.isNotEmpty(item.model)){
                  if(Tool.isNotEmpty(item.unit)){
                    if(Tool.isNotEmpty(item.amount)){
                      if(Tool.isNotEmpty(item.preTaxUnitPrice)){
                        if(Tool.isNotEmpty(item.supplierName)){
                          bomList.push(item);
                        }
                      }
                    }
                  }
                }
              }
            }
          }

          /*bomItems.forEach((bomItem, index) => {
            if(bomItem.bomCode
                && bomItem.name
                && bomItem.model
                && bomItem.unit
                && bomItem.amount
                && bomItem.preTaxUnitPrice
                && bomItem.supplierName
            ){
              bomList.push(bomItem);
            }
          });*/

          _this.saveBomItemList(bomList)
        }
      },

      saveBomItemList(bomList){
        let _this = this;
        if(bomList.length === 0){
          _this.refreshFunction();
          Toast.warning("表格内容有误,无法正确识别!", Swal);
          return;
        }
        bomList.forEach(item => {
          item.subprojectId = _this.subprojectId;
        })
        _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/material/saveBomItem_subproject_import', bomList).then((response)=>{
          let resp = response.data;
          if (resp.success) {
            Toast.success("保存成功", Swal);
            _this.$bus.$emit('refresh-bom-edit-list-subproject-dlg', true );
          } else {
            Toast.warning(resp.message, Swal);
            _this.refreshFunction();
          }
        })
      }

    }
  }
</script>
