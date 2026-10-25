<!--
 * Author: 周朔鹏
 * Date: 2024-11-07
 * Email: 391902958@qq.com
 * Website: https://www.euipd.com
-->

<template>
  <div style="height: 100%;">
    <div class="paper-height">
      <div style="height: 100%; overflow: scroll;">
        <div class="a4-paper-big-simple">
          <div style="margin: 0 40px 10px; width: 100%;">
            <div style="margin: 40px 0 8px; font-weight: 600;">
              项目文档列表:
            </div>
            <el-tree style="background-color: rgba(187,215,248,0);"
                     ref="docsTree"
                     :data="data"
                     :indent="14"
                     node-key="id"
                     :default-expand-all="true"
                     :highlight-current="true"
                     :check-on-click-node="false"
                     :expand-on-click-node="false"
                     :accordion=false
            >  <!--@node-click="nodeClick"-->
              <span class="custom-tree-node" slot-scope="{ node, data }">
                <span>{{ DOC_TYPE | optionKV( data.status) }}</span> :
                <span>{{ ((data.label)) }}</span>
                <span v-if="data.status === DOC_TYPE.FILE.key" style="margin-left: 20px;">{{
                    data.obj.createTime | dateFormat
                  }}
                </span>
                <span v-if="data.status === DOC_TYPE.FILE.key" style="margin-left: 20px;">
                  <el-button v-if="data.obj && data.obj.authStatus" type="text" style="margin-left: 50px;" @click="downloadFile(data.obj)">下载</el-button>
                </span>
              </span>
            </el-tree>
          </div>
        </div>
      </div>
    </div>
  </div>

</template>

<script>
import Swal from "sweetalert2";
import { Loading } from 'element-ui';

export default {
  components: {
  },
  name: "subproject-archive-tree",

  props: {
    projectId: {
      default: null,
    },
  },

  data: function () {
    return {
      loginUser: {},
      loginCustomer: {},
      DOC_TYPE: DOC_TYPE,
      PROJECT_TYPE: PROJECT_TYPE,
      ENTRANCE_CLASS: ENTRANCE_CLASS,

      docFileTree: [],

      nodeObject: {},
    }
  },

  computed: {
    data: function(){
      return  this.docFileTree;
    },
  },

  mounted: function () {
    let _this = this;
    _this.getLoginUser();
    if(_this.projectId){
      _this.getArchiveTreeBySubprojectId(_this.projectId);
    }
  },

  methods: {
    nodeClick(data) {
      let _this = this;
      _this.nodeObject = data;
      console.log(data);
    },

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

    getArchiveTreeBySubprojectId(subprojectId) {
      let _this = this;
      let loadingInstance = Loading.service({ fullscreen: true });
      let formData = new window.FormData();
      formData.append('subprojectId', subprojectId);
      formData.append('loginUserId', _this.loginUser.customerUserId);
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/file/rdms/getArchiveTreeBySubprojectId', formData).then((response) => {
        loadingInstance.close();
        let resp = response.data;
        if (resp.success) {
          _this.docFileTree = resp.content;
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

  },
}
</script>

<style lang="scss" scoped>
@import './../../../public/static/scss/caipd.scss';
.paper-height {
  height: calc(100vh - 290px);
}
.content-height {
  height: 100%;
  overflow: scroll;
}

</style>

