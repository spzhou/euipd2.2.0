<!--
 * Author: 周朔鹏
 * Date: 2024-01-07
 * Email: 391902958@qq.com
 * Website: https://www.euipd.com
-->

<template>
  <div class="dialog-container">
    <div style="margin: 0 90px 60px 100px">
      <el-form label-width="180px">
        <el-form-item label="产品配置名称:" required>
          <el-col :span="14">
            <el-input v-model="bomConfig.name"></el-input>
          </el-col>
        </el-form-item>
        <el-form-item label="设为默认:">
          <el-checkbox v-model="defaultBom">设为默认</el-checkbox>
        </el-form-item>
      </el-form>
    </div>

    <div class="clearfix form-actions">
      <div style="display: flex; justify-content: center;">
        <button v-on:click="saveBom()" class="ca-btn-primary" type="button">
          确认
        </button>
        <button v-on:click="close()" class="ca-btn-primary" type="button">
          关闭
        </button>
      </div>
    </div>
  </div>
</template>
<script>
import Swal from "sweetalert2";
import FileRdms from "@/components/rdms/file-rdms";
import FileUploadCore from "@/components/std/file-upload-core.vue";
import FileSaver from 'file-saver';


export default {
  name: 'bom-config-create-dlg',

  components: {
    FileRdms,
    FileUploadCore,
  },

  props:{
    projectId: {
      default: null,
    },
    closeFunction: {
      type: Function,
      default: null
    },
  },

  data() {
    return {
      loginUser: {},
      loginCustomer: {},

      bomConfig: {},
      defaultBom: false,
    };
  },

  computed: {
  },

  mounted: function() {
    let _this =this;

  },
  methods:{

    /**
     * 提交需求
     */
    saveBom() {
      let _this = this;
      // 保存校验
      if (1 !== 1
          || !Validator.require(_this.bomConfig.name, "产品配置名称",Swal)
      ) {
        return;
      }

      if(_this.defaultBom){
        _this.bomConfig.isActive = 1;
      }
      _this.$bus.$emit('save-new-bom-config-item', _this.bomConfig);
      _this.close();

    },

    close(){
      let _this = this;
      _this.closeFunction();
    },

  }
};
</script>

<style lang="scss" scoped>
@import './../../../public/static/scss/caipd.scss';

</style>

