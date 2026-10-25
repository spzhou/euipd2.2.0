<!--
 * Author: 周朔鹏
 * Date: 2024-11-12
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
          <el-col :span="24">
            <el-form-item label="特性名称">
              <el-row>
                <el-col :span="12">
                  <el-select v-model="dialogInfo.characterId" filterable placeholder="请选择特性">
                    <el-option v-for="o in characterList" :key="o.id" :value="o.id" :label="o.characterName"></el-option>
                  </el-select>
                </el-col>
                <el-col>
                  <small><em>说明:请不要随意修改BOM条目隶属的特性, 否则, 在BOM配置过程中会出现偏差.</em></small>
                </el-col>
              </el-row>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="12">
            <el-form-item label="物料名称" required>
              <el-col :span="23">
                <el-input v-model="dialogInfo.name"></el-input>
              </el-col>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="12">
            <el-form-item label="物料编号" required>
              <el-col :span="23">
                <el-input v-model="dialogInfo.bomCode"></el-input>
              </el-col>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="12">
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
        </el-row>
        <el-row>
          <el-col :span="6">
            <el-form-item label="未税单价" required>
              <el-col :span="22">
                <el-input type="number" v-model="dialogInfo.preTaxUnitPrice"></el-input>
              </el-col>
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="使用数量" required>
              <el-col :span="22">
                <el-input type="number" v-model="dialogInfo.amount"></el-input>
              </el-col>
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="供货税率">
              <el-col :span="22">
                <el-input type="number" v-model="dialogInfo.taxRate"></el-input>
              </el-col>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="11">
            <el-form-item label="供应商名称" required>
              <el-col :span="23">
                <el-input v-model="dialogInfo.supplierName"></el-input>
              </el-col>
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="销售联系人">
              <el-col :span="22">
                <el-input v-model="dialogInfo.sales"></el-input>
              </el-col>
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="联系电话">
              <el-col :span="22">
                <el-input v-model="dialogInfo.contactPhone"></el-input>
              </el-col>
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <div class="clearfix form-actions">
        <div style="display: flex; justify-content: center;">
          <button  type="button" v-on:click="save()" class="ca-btn-primary">保 存</button>
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
  name: "bom-edit-product-dlg",

  props: {
    bomItem: {
      default: null
    },
    projectId: {
      default: null,
    },
    closeDlg: {
      type: Function,
      default: null
    },
  },

  data() {
    return {
      loginUser: {},
      loginCustomer: {},
      characterList: [],
      dialogInfo: {},
    };
  },

  computed: {

  },

  mounted: function () {
    let _this = this;
    _this.getLoginUser();
    _this.dialogInfo = _this.bomItem;
    _this.getArchivedCharacterListByProjectId(_this.projectId);
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

    save() {
      let _this = this;
      if (1 !== 1
          || !Validator.require(_this.dialogInfo.name, "物料名称",Swal)
          || !Validator.require(_this.dialogInfo.bomCode, "物料编号",Swal)
          || !Validator.require(_this.dialogInfo.model, "物料型号",Swal)
          || !Validator.require(_this.dialogInfo.unit, "计量单位",Swal)
          || !Validator.require(_this.dialogInfo.preTaxUnitPrice, "未税单价",Swal)
          || !Validator.require(_this.dialogInfo.amount, "使用数量",Swal)
          || !Validator.require(_this.dialogInfo.supplierName, "供应商",Swal)

          || !Validator.length(_this.dialogInfo.name, "物料名称", 2, 80,Swal)
          || !Validator.length(_this.dialogInfo.bomCode, "物料编号", 2, 50,Swal)
          || !Validator.length(_this.dialogInfo.model, "物料型号", 2, 50,Swal)
          || !Validator.length(_this.dialogInfo.unit, "计量单位", 1, 10,Swal)
          || !Validator.length(_this.dialogInfo.amount, "使用数量", 1, 10,Swal)
          || !Validator.length(_this.dialogInfo.preTaxUnitPrice, "未税单价", 1, 12,Swal)
          || !Validator.length(_this.dialogInfo.supplierName, "供应商", 2, 100,Swal)
      ) {
        return;
      }

      Swal.fire({
        title: '确认',
        text: "是否确认修改BOM? 确定后, 当前产品BOM将发生改变! 建议修改之前保存副本!",
        icon: 'warning',
        showCancelButton: true,
        confirmButtonColor: '#409EFF',
        cancelButtonColor: '#909399',
        confirmButtonText: '确认!',
        cancelButtonText: '取消!'
      }).then((result) => {
        if (result.value) {
          _this.dialogInfo.loginUserId = _this.loginUser.customerUserId;
          _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/material/saveEditedBomItem', _this.dialogInfo).then((response) => {
            let resp = response.data;
            if (resp.success) {
              _this.$bus.$emit('refresh-bom-product-manage-page', true);
              Toast.success("保存成功", Swal);
              _this.close();
            }
          })
        }
      })
    },

    getArchivedCharacterListByProjectId(projectId) {
      let _this = this;
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/material/getArchivedCharacterListByProjectId/' + projectId).then((response) => {
        let resp = response.data;
        if (resp.success) {
          _this.characterList = resp.content;
        } else {
          Toast.warning(resp.message, Swal);
        }
      });
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
