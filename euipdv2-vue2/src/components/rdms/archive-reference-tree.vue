<!--
 * Author: 周朔鹏
 * Date: 2025-02-18
 * Email: 391902958@qq.com
 * Website: https://www.euipd.com
-->

<template>
  <div>
    <div class="row">
      <div style="margin: 0 15px;">
        <p>我的文献: 列出<strong>工作执行人员</strong>在参与所有项目执行过程中提交的所有文档,可以随时查阅,为后续工作执行提供基础参阅.</p>
        <div class="hr hr-18 dotted hr-double"></div>
      </div>
    </div>
    <div style="margin-top: -10px;">
      <div style="display: flex; flex-direction: row; justify-content: flex-start; align-items: center;">
        <div class="page-height-tree" style="min-width: 400px; width: 32%;">
          <el-row style="background-color: rgba(194,225,246,0.23); height: 100%; overflow: scroll;">
            <el-tree style="background-color: rgba(187,215,248,0);"
                     ref="docsTree"
                     :data="data"
                     :indent="14"
                     :show-checkbox="false"
                     node-key="id"
                     :default-expand-all="false"
                     :highlight-current="true"
                     :check-on-click-node="true"
                     :expand-on-click-node="false"
                     :accordion=false
                     @node-click="nodeClick">
              <span class="custom-tree-node" slot-scope="{ node, data }">
                <span>{{ DOC_TYPE | optionKV( data.status) }}</span> :
                <span>{{ ((data.label)) }}</span>
                <span v-if="data.status === DOC_TYPE.FILE.key" style="margin-left: 20px;">
<!--                  <el-button type="text" style="margin-left: 50px;" @click="downloadFile(data.obj)">下载</el-button>-->
                </span>
              </span>
            </el-tree>
          </el-row>
        </div>
        <div  class="page-height-tree" style="width: 68%">
          <div class="content-height">
            <div v-if="nodeObject.status === DOC_TYPE.CHARACTER.key" style="width: 100%; min-width: 990px; margin: 0 auto;">
              <div class="hr hr-12 hr-dotted"></div>
              <div>
                <CharacterDocTabs
                    :key="nodeObject.id"
                    :character-id="nodeObject.id"
                ></CharacterDocTabs>
              </div>
            </div>
            <div v-if="nodeObject.status === DOC_TYPE.JOBITEM.key" style="width: 990px; margin: 0 auto;">
              <JobitemDetailInfoCore
                  :key="nodeObject.id"
                  :jobitem-id="nodeObject.id"
              ></JobitemDetailInfoCore>
            </div>
            <div v-if="nodeObject.status === DOC_TYPE.SUB_PROJECT.key" style="width: 990px; margin: 0 auto;">
              <ProjectInfoOutline
                  :key="nodeObject.id"
                  :x-project-id="nodeObject.id"
                  :project-type="PROJECT_TYPE.SUB_PROJECT.key"
                  :entrance="ENTRANCE_CLASS.PROJECT.key"
              ></ProjectInfoOutline>
            </div>
            <div v-if="nodeObject.status === DOC_TYPE.FILE.key" style="width: 990px; margin: 0 auto;">
              <FileInfoOutline
                  :key="nodeObject.id"
                  :file-id="nodeObject.id"
              ></FileInfoOutline>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import Swal from "sweetalert2";
import {optionKV} from "@/filter/filter";
import CharacterDocTabs from "@/components/rdms/character-doc-tabs.vue";
import ProjectInfoOutline from "@/components/rdms/project-info-outline.vue";
import JobitemDetailInfoCore from "@/components/std/jobitem-detail-info-core.vue";
import FileInfoOutline from "@/components/rdms/file-info-outline.vue";
import { Loading } from 'element-ui'

export default {
  components: {
    JobitemDetailInfoCore,
    ProjectInfoOutline,
    CharacterDocTabs,
    File,
    FileInfoOutline,
  },
  name: "archive-reference-tree",

  props: {
    workerId: {
      default: null,
    }
  },

  data: function () {
    return {
      loginUser: {},
      loginCustomer: {},

      archiveTree: [],
      DOC_TYPE: DOC_TYPE,
      PROJECT_TYPE: PROJECT_TYPE,
      ENTRANCE_CLASS: ENTRANCE_CLASS,

      nodeObject: {},
    }
  },

  computed: {
    data: function(){
      return  this.archiveTree;
    },
  },

  mounted: function () {
    let _this = this;
    _this.getLoginUser();
    if(Tool.isNotEmpty(_this.workerId)){
      _this.getMyArchiveTreeByWorkerId(_this.workerId);
    }
  },

  beforeDestroy: function () {
  },

  methods: {
    optionKV,
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

    nodeClick(data) {
      let _this = this;
      _this.nodeObject = data;
      // console.log(data);
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

    getMyArchiveTreeByWorkerId(workerId) {
      let _this = this;
      let loadingInstance = Loading.service({ fullscreen: true });
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/file/rdms/getMyArchiveTreeByWorkerId/' + workerId ).then((response) => {
        loadingInstance.close();
        let resp = response.data;
        if (resp.success) {
          _this.archiveTree = resp.content;
        } else {
          Toast.warning(resp.message, Swal);
        }
      })
    },

  }
}
</script>

<style lang="scss">
@import './../../../public/static/scss/caipd.scss';

.page-height-tree{
  display: block;
  width: 100%;
  height: calc(100vh - 255px);
}
.content-height {
  height: 100%;
  overflow: scroll;
}

</style>
