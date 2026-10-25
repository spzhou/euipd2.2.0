<!--
 * Author: 周朔鹏
 * Date: 2024-05-16
 * Email: 391902958@qq.com
 * Website: https://www.euipd.com
-->

<template>
  <div style="margin: 0 20px 0 50px;">
    <div v-if="fileList && fileList.length>0" style="margin: 5px 0 10px;">
      为任务执行人选择所需附件: <small><u><em>(如果需要增加其他附件, 可以通过上传附件增补)</em></u></small>
    </div>
    <el-checkbox-group v-model="checkedFileIdList" @change="fileIdListChange">
      <el-checkbox v-for="(file, i) in fileList" :label="file.id" :key="i">{{file.name}}</el-checkbox>
    </el-checkbox-group>
  </div>
</template>

<script>
import FileRdms from "@/components/rdms/file-rdms";
import Swal from "sweetalert2";

export default {
  components: {
    FileRdms,
  },
  name: "select-jobitem-file-list",

  data: function () {
    return {
      fileList: [],
      checkedFileIdList: [],
    }
  },

  props: {
    jobitemId: {
      default: null,
    },
  },

  mounted: function () {
    let _this = this;
    if(Tool.isNotEmpty(_this.jobitemId)){
      _this.getJobitemAllAttachments(_this.jobitemId);
    }
  },

  methods: {
    getJobitemAllAttachments(jobitemId) {
      let _this = this;
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/job-items/getJobitemAllAttachments/' + jobitemId).then((response) => {
        let resp = response.data;
        if (resp.success) {
          _this.fileList = resp.content;
        } else {
          Toast.warning(resp.message, Swal);
        }
      })
    },

    fileIdListChange(value){
      let _this = this;
      _this.checkedFileIdList = value;
      _this.$emit("checkedFileIdList", _this.checkedFileIdList);
    }
  },

}
</script>

<style lang="scss">
@import './../../../public/static/scss/caipd.scss';

</style>
