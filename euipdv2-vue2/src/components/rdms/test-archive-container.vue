<!--
 * Author: 周朔鹏
 * Date: 2025-01-02
 * Email: 391902958@qq.com
 * Website: https://www.euipd.com
-->

<template>
  <div class="paper-height">
    <el-row style="height: 100%; width: 100%;">
      <el-col :span="8" style="background-color: rgba(194,225,246,0.23); height: 100%; overflow: scroll;">
        <div style="margin: 0 30px 10px; width: 780px;">
          <div style="margin: 8px 0; font-weight: 500;">
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
                   @node-click="nodeClick">
              <span class="custom-tree-node" slot-scope="{ node, data }">
                <span>{{ DOC_TYPE | optionKV( data.status) }}</span> :
                <span>{{ ((data.label)) }}</span>
                <span v-if="data.status === DOC_TYPE.FILE.key" style="margin-left: 20px;">{{
                    data.obj.createTime | dateFormat
                  }}
                </span>
                <span v-if="data.status === DOC_TYPE.FILE.key" style="margin-left: 20px;">
<!--                  <el-button type="text" style="margin-left: 50px;" @click="downloadFile(data.obj)">下载</el-button>-->
                </span>
              </span>
          </el-tree>
        </div>
      </el-col>
      <el-col :span="16" class="content-height">
        <div v-if="nodeObject.status === DOC_TYPE.JOBITEM.key" style="width: 990px; margin: 0 auto;">
          <JobitemDetailInfoCore
              :key="nodeObject.id"
              :jobitem-id="nodeObject.id"
          ></JobitemDetailInfoCore>
        </div>
        <div v-if="nodeObject.status === DOC_TYPE.FILE.key" style="width: 990px; margin: 0 auto;">
          <FileInfoOutline
              :key="nodeObject.id"
              :file-id="nodeObject.id"
          ></FileInfoOutline>
        </div>
      </el-col>
    </el-row>
  </div>
</template>

<script>
import Swal from "sweetalert2";
import { Loading } from 'element-ui';
import JobitemDetailInfoCore from "@/components/std/jobitem-detail-info-core.vue";
import FileInfoOutline from "@/components/rdms/file-info-outline.vue";

export default {
  components: {
    FileInfoOutline,
    JobitemDetailInfoCore,
  },
  name: "test-archive-container",

  props: {
    projectId: {
      default: null,
    },
    index: {
      default: null,
    },
    projectType: {
      default: null,
    },
    entrance: {
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
      random: 0,

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
    if(_this.projectId && _this.projectType === PROJECT_TYPE.PROJECT.key){
      _this.getTestArchiveTreeByProjectId(_this.projectId);
    }
  },

  methods: {
    nodeClick(data) {
      let _this = this;
      _this.nodeObject = data;
      // console.log(data);
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

    getTestArchiveTreeByProjectId(projectId) {
      let _this = this;
      let loadingInstance = Loading.service({ fullscreen: true });
      let formData = new window.FormData();
      formData.append('projectId', projectId);
      formData.append('loginUserId', _this.loginUser.customerUserId);
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/file/rdms/getTestArchiveTreeByProjectId', formData).then((response) => {
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
  height: calc(100vh - 210px);
}
.content-height {
  height: 100%;
  overflow: scroll;
}

</style>
