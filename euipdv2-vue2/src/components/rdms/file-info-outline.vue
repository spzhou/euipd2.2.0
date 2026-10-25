<template>
  <div>
    <div class="row">
      <div class="col-md-offset-1">
        <p>文件信息</p>
        <div class="hr hr-8 dotted hr-double"></div>
        <div>
          <el-form
              label-width="130px">
            <el-form-item label="文件名称:">
              {{ fileInfo.name }}
            </el-form-item>
            <el-form-item label="文件大小:">
              {{ fileInfo.size /1024 }} KB
            </el-form-item>
            <el-form-item label="创建日期:">
              {{ fileInfo.createTime | dateFormat  }}
            </el-form-item>
            <el-form-item label="文件作者:">
              {{ fileInfo.operatorName }}
            </el-form-item>
            <el-form-item v-if="fileInfo.authStatus" label="操作:">
              <el-button type="text" @click="downloadFile(fileInfo)">下载</el-button>
            </el-form-item>
          </el-form>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import SubprojectShowSummary from "@/components/rdms/subproject-show-summary.vue";
import Swal from "sweetalert2";

export default {
  components: {
    SubprojectShowSummary,
  },
  name: "file-info-outline",

  props:{
    fileId: {
      default: null,
    },
  },

  data: function() {
    return {
      fileInfo: {},
      random: 0,
    }
  },

  computed: {},

  mounted: function() {
    let _this = this;
    _this.getLoginUser();

    if(Tool.isNotEmpty(_this.fileId)){
      _this.getFileSimpleInfo(_this.fileId);
    }

    _this.$bus.$on('refresh-file-info-outline', (data)=> {
      if(data && _this.fileId){
        _this.getFileSimpleInfo(_this.fileId);
      }
    });
  },

  beforeDestroy() {
    let _this = this;
    _this.$bus.$off('refresh-file-info-outline');
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

    getFileSimpleInfo(fileId) {
      let _this = this;
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/file/rdms/getFileSimpleInfo/'+ fileId).then((response) => {
        let resp = response.data;
        if (resp.success) {
          _this.fileInfo = resp.content;
          /*let file = _this.fileInfo;
          _this.getSignedUrl(file.path, file.suffix);*/
        } else {
          Toast.warning(resp.message, Swal);
        }
      })
    },

    downloadFile(file) {
      let _this = this;
      _this.getSignedUrl(file.path, file.suffix);
    },
    getSignedUrl (fileFullName, suffix) {
      let _this = this;
      let formData = new window.FormData();
      formData.append('fileFullName', fileFullName);
      formData.append('customer', _this.loginCustomer.id);
      formData.append('user', _this.loginUser.customerUserId);
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/file/rdms/oss-get-signed-url', formData).then((response)=>{
        let resp = response.data;
        Tool.openOssSignedUrl(resp, suffix);
      });
    },

  }
}
</script>

<style lang="scss" scoped>

</style>
