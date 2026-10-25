<!--
 * Author: 周朔鹏
 * Date: 2025-02-23
 * Email: 391902958@qq.com
 * Website: https://www.euipd.com
-->

<template>
  <div style="height: 100%;">
    <div class="paper-height">
      <div style="height: 100%; overflow: scroll;">
        <el-row>
          <el-col>
            <div class="a4-paper-big-plain">
              <div id="examine-print">
                <el-row>
                  <el-col :span="24" style="width: 100%; text-align: center; font-size: 28px;margin-top: 25px;">
                    {{ characterItem.characterName }}
                  </el-col>
                </el-row>
                <div>
                  <CharacterDetailInfoCore
                      :id="('character-main-show-' + (random))"
                      :key="characterItem.id"
                      :character-id="characterItem.id"
                      :pin-type="pinType"
                      :ref-id="refId"
                  ></CharacterDetailInfoCore>
                </div>

<!--             显示特性定义审批过程提供的附件-->
                <div v-show="processAttachments!==null  && processAttachments !== undefined && processAttachments.length >0">
                  <div class="hr hr-18 dotted hr-double"></div>
                  <el-row>
                    <el-col :span="22">
                      <div style="display: flex; align-items: end; flex-direction: column; justify-content: center; margin-bottom: 15px;">
                        <div v-for="(item, i) in processAttachments" :key="i" style="display: flex; flex-direction: row; align-items: center; margin: 5px 0 0 50px;">
                          <div v-if="item">
                            {{ ((item.stage) + '阶段 : ' + (item.name))}}
                          </div>
                          <div v-if="item && item.authStatus">
                            <el-button type="text" style="margin-left: 50px;" @click="downloadFile(item)">下载</el-button>
                          </div>
                        </div>
                      </div>
                    </el-col>
                  </el-row>
                </div>
                <div class="hr hr-18 dotted hr-double"></div>
                <div v-if="characterProcessList !== null && characterProcessList !== undefined && characterProcessList.length > 0">
                  <el-row>
                    <div style="width: var(--table-out-width, 100%); max-width: 22cm; margin: 0 auto;">
                      <div>
                        <el-table
                            ref="characterProcessTable"
                            :data="characterProcessList"
                            border
                            stripe
                            style="width: 100%"
                            highlight-current-row>
                          <el-table-column label="序号" align="center" width="50">
                            <template slot-scope="scope">
                              {{ scope.$index + 1 }}
                            </template>
                          </el-table-column>
                          <el-table-column
                              prop="processStatusName"
                              label="执行过程"
                              align="center"
                              width="70">
                          </el-table-column>
                          <el-table-column
                              prop="jobDescription"
                              label="审批描述">
                          </el-table-column>
                          <el-table-column
                              prop="executorName"
                              label="签审人"
                              align="center"
                              width="70">
                          </el-table-column>
                          <el-table-column
                              prop="nextNodeName"
                              label="下一节点"
                              align="center"
                              width="70">
                          </el-table-column>
                          <el-table-column
                              prop="createTimeStr"
                              label="签审时间"
                              align="center"
                              width="150">
                          </el-table-column>
                        </el-table>
                      </div>
                    </div>
                  </el-row>
                </div>
              </div>
            </div>
          </el-col>
        </el-row>
      </div>
    </div>
  </div>
</template>

<script>
import FileRdms from "@/components/rdms/file-rdms.vue";
import FileSaver from 'file-saver';
import Swal from "sweetalert2";
import printJS from "print-js";
import CharacterDetailInfoCore from "@/components/std/character-detail-info-core.vue";

export default {
  components: {
    FileRdms,
    FileSaver,
    CharacterDetailInfoCore,
  },
  name: "character-detail-info-core-all",

  props: {
    characterId: {
      default: null,
    },
    pinType: { default: null },
    refId: { default: null },
  },

  data() {
    return {
      loginUser: {},
      loginCustomer: {},

      characterItem: {},
      characterProcess: {},
      processAttachments: [],  //过程流转中产生的附件

      PROJECT_TYPE: PROJECT_TYPE,
      DOC_TYPE: DOC_TYPE,

      characterProcessList: [],

      random: 0,
      printData: {
        printable: 'examine-print',
        type: 'html',
        // header: this.characterItem.preProjectName,
        // headerStyle: 'font-weight: 600; font-size: 30px;',
        ignore: ['no-print'],
        targetStyles: ['*'],  //css样式，写成*代表打印样式完全继承你页面的样式
        // maxWidth:'800px'  //打印界面最大宽度，适当调整可以解决打印页面过宽，显示不完整的问题
      },
    };
  },

  computed: {},

  mounted: function () {
    let _this = this;
    _this.random = Math.random();
    _this.getLoginUser();
    if(Tool.isNotEmpty(_this.characterId)){
      _this.getCharacterInfo(_this.characterId);
    }
  },

  beforeDestroy() {
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

    getCharacterInfo(characterId){
      let _this = this;
      let formData = new window.FormData();
      formData.append('characterId', characterId);
      formData.append('loginUserId', _this.loginUser.customerUserId);
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/character/getCharacterRecordDetailById', formData).then((response) => {
        let resp = response.data;
        if (resp.success) {
          _this.characterItem = resp.content;
          _this.listProcessByCharacterId(_this.characterItem.id);
        } else {
          Toast.warning(resp.message,Swal);
        }
      })
    },

    listProcessByCharacterId(id) {
      let _this = this;
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/character-process/listProcessAllInfoByCharacterId/' + id).then((response) => {
        let resp = response.data;
        if (resp.success) {
          _this.characterProcessList = resp.content.characterProcessDtos;
          _this.processAttachments = resp.content.processAttachmentDtos;
        } else {
          Toast.warning(resp.message, Swal);
        }
      })
    },

    handlePrint(params) {
      printJS({
        printable: params.printable, // 'printFrom', // 标签元素id
        type: params.type || 'html',
        header: params.header, // '表单',
        targetStyles: ['*'],
        style: '@page {margin:20 10mm};', // 可选-打印时去掉眉页眉尾
        ignoreElements: params.ignore || [], // ['no-print']
        properties: params.properties || null
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
        // FileSaver.saveAs(resp.content, fileName);
      });
    },

  },
}
</script>

<style lang="scss" scoped>
@import './../../../public/static/scss/caipd';

.paper-height {
  height: calc(100vh - 325px);
}

.a4-paper-big-plain {
  //margin-top: 20px;
  background: white;
  width: 25cm;
  //height: 29.7cm;
  display: block;
  margin: 0 auto;
  padding: 0 25px;
  margin-bottom: 0.5cm;
  //box-shadow: 0 0 0.5cm rgba(0, 0, 0, 0.5);
  overflow-y: scroll;
  box-sizing: border-box;
}
</style>
