<!--
 * Author: 周朔鹏
 * Date: 2024-11-27
 * Email: 391902958@qq.com
 * Website: https://www.euipd.com
-->

<template>
  <div>
    <div class="dialog-container">
      <el-form
          label-width="130px"
          :show-message=false
          class="dialog-width">
        <el-row>
          <el-col :span="11">
            <el-form-item label="申请单号">
              <el-col :span="23">
                <el-input type="text" style="font-size: 14px;" v-text="dialogInfo.code"></el-input>
              </el-col>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="11">
            <el-form-item label="物料名称" required>
              <el-col :span="23">
                <el-input v-model="dialogInfo.name"></el-input>
              </el-col>
            </el-form-item>
          </el-col>
          <el-col :span="11">
            <el-form-item label="物料编号" required>
              <el-col :span="23">
                <el-input v-model="dialogInfo.bomCode"></el-input>
              </el-col>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="10">
            <el-form-item label="物料型号" required>
              <el-col :span="23">
                <el-input v-model="dialogInfo.model"></el-input>
              </el-col>
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="计量单位" required>
              <el-col :span="22">
                <el-input v-model="dialogInfo.unit"></el-input>
              </el-col>
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="领用数量" required>
              <el-col :span="22">
                <el-input type="number" v-model="dialogInfo.amount"></el-input>
              </el-col>
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <div class="clearfix form-actions" style="margin-top: 50px;">
        <div style="display: flex; justify-content: center;">
          <button  type="button" v-on:click="addToList(dialogInfo)" class="ca-btn-primary">添 加</button>
          <button type="button" v-on:click="close()" class="ca-btn-primary">取 消</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import Swal from "sweetalert2";

export default {
  components: {
  },
  name: "material-add-dlg",

  props: {
    applicationCode: {
      default: null
    },
    materialList: {
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
        id: '',
        code: '',
        bomCode: '',
        name: '',
        model: '',
        unit: '',
        amount: '',
      },
      materialApplicationList: [],

    };
  },

  computed: {

  },

  mounted: function () {
    let _this = this;
    _this.dialogInfo.code = _this.applicationCode;
    _this.dialogInfo.id = Uuid.shortUuid(8);
    _this.materialApplicationList = _this.materialList;
  },

  methods: {
    addToList(dialogInfo) {
      let _this = this;
      if (1 !== 1
          || !Validator.require(dialogInfo.code, "申请单号",Swal)
          || !Validator.require(dialogInfo.name, "物料名称",Swal)
          || !Validator.require(dialogInfo.bomCode, "物料编号",Swal)
          || !Validator.require(dialogInfo.model, "物料型号",Swal)
          || !Validator.require(dialogInfo.unit, "计量单位",Swal)
          || !Validator.require(dialogInfo.amount, "领用数量",Swal)

          || !Validator.length(dialogInfo.name, "物料名称", 2, 80,Swal)
          || !Validator.length(dialogInfo.bomCode, "物料编号", 2, 50,Swal)
          || !Validator.length(dialogInfo.model, "物料型号", 2, 50,Swal)
          || !Validator.length(dialogInfo.unit, "计量单位", 1, 10,Swal)
          || !Validator.length(dialogInfo.amount, "领用数量", 1, 10,Swal)

          || !Validator.greatThanZero(dialogInfo.amount, "领用数量", Swal)
      ) {
        return;
      }
      _this.materialApplicationList.push(dialogInfo);
      this.$emit('materialApplicationList', _this.materialApplicationList);  //监听事件__使用者通过v-on接收数据
      _this.close();
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

.dialog-width{
  width: 100%;
}

</style>
