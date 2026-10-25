<!--
 * Author: 周朔鹏
 * Date: 2026-08-02
 * Email: 391902958@qq.com
 * Website: https://www.euipd.com
 * 文件下载链接：authStatus 由服务填写。能查看该页（令牌含 FILE:VIEW）即为真。单独登记了权限的下载按钮另按按钮白名单显示。
-->
<template>
  <el-button
      v-if="file && file.authStatus"
      type="text"
      :style="btnStyle"
      @click="downloadFile">
    {{ label }}
  </el-button>
</template>

<script>
export default {
  name: "file-download-link",
  props: {
    file: { type: Object, default: null },
    label: { type: String, default: "下载" },
    marginLeft: { type: String, default: "20px" },
  },
  computed: {
    btnStyle() {
      return { marginLeft: this.marginLeft };
    },
  },
  methods: {
    downloadFile() {
      const file = this.file;
      if (!file || !file.path) {
        return;
      }
      const loginUser = Tool.getActorUser ? Tool.getActorUser() : Tool.getLoginUser();
      const loginCustomer = Tool.getLoginCustomer
          ? Tool.getLoginCustomer()
          : (Tool.getLoginUser() || {}).customer;
      if (!loginUser || !loginCustomer) {
        return;
      }
      const formData = new window.FormData();
      formData.append("fileFullName", file.path);
      formData.append("customer", loginCustomer.id);
      formData.append("user", loginUser.customerUserId);
      this.$ajax
          .post(process.env.VUE_APP_SERVER + "/file/rdms/oss-get-signed-url", formData)
          .then((response) => {
            Tool.openOssSignedUrl(response.data, file.suffix);
          });
    },
  },
};
</script>
