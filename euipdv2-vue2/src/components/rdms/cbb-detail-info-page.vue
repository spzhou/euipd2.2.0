<!--
 * Author: 周朔鹏
 * Date: 2025-04-18
 * Email: 391902958@qq.com
 * Website: https://www.euipd.com
-->

<template>
  <div style="height: 100%;" >
    <div class="paper-height">
      <div style="height: 100%; overflow: scroll;">
        <div class="container-paper">
          <el-row>
            <el-col :span="19">
              <el-alert v-if="cbbRecord.status === CBB_STATUS.DISCARD.key" type="error" :closable="false" show-icon
                        :title="'已废弃：' + (cbbRecord.discardUserName || '') + ' 于 ' + discardTimeText + ' 废弃，原因：' + (cbbRecord.discardReason || '')"></el-alert>
              <el-alert v-else-if="cbbRecord.status === CBB_STATUS.DISABLED.key" type="warning" :closable="false" show-icon
                        title="已停用：不再作为选用候选，已有引用不变。"></el-alert>
              <el-tabs v-model="activeTab" @tab-click="onTabClick">
                <el-tab-pane label="特性定义" name="define">
                  <div style="width: 100%; text-align: center; font-size: 28px;margin-top: 25px;">
                    {{ characterItem.characterName }}
                  </div>
                  <CharacterDetailInfoCore
                      :id="characterId"
                      :key="characterId"
                      :character-id="characterId"
                  ></CharacterDetailInfoCore>
                </el-tab-pane>
                <el-tab-pane v-if="cbbId" label="技术文档" name="techDoc">
                  <CbbTechDocDialog v-if="techDocOpened" :cbb-id="cbbId" embedded></CbbTechDocDialog>
                </el-tab-pane>
                <el-tab-pane v-if="cbbId" label="被引用" name="whereUsed">
                  <p>本 CBB 在主项目、子项目、特性与 CBB 定义记录中的引用情况。</p>
                  <el-table :data="whereUsedList" border stripe size="small" v-loading="whereUsedLoading" empty-text="暂无选用方">
                    <el-table-column prop="kind" label="选用方" width="120" align="center" :formatter="kindFormat"></el-table-column>
                    <el-table-column prop="name" label="名称" min-width="200" show-overflow-tooltip></el-table-column>
                    <el-table-column prop="cbbDevVersion" label="发布版本" width="100" align="center"></el-table-column>
                    <el-table-column prop="status" label="状态" width="120" align="center" :formatter="usageStatusFormat"></el-table-column>
                  </el-table>
                </el-tab-pane>
              </el-tabs>
            </el-col>
            <el-col :span="5">
              <div style="margin: 50px 0 0 -50px;">
                <div style="line-height: 40px; font-size: 15px; font-weight: 500;">
                  <a href="javascript:void(0);" @click="lineageVisible = true">CBB 内容版本演进</a>
                </div>
                <div style="line-height: 40px; font-size: 15px; font-weight: 500;">
                  承载特性的迭代记录:
                </div>
                <div v-for="(item, index) in inheritTree" :key="index" style="line-height: 30px;">
                  <div>
                    <a href="javascript:void(0);" @click="showCharacterDlg(item.id)">
                      {{ item.characterName }}
                    </a>
                  </div>
                </div>
              </div>
            </el-col>
          </el-row>
        </div>
      </div>
    </div>

    <!--特性定义表-->
    <el-dialog
        width="29cm"
        append-to-body
        :visible.sync="dlgCharacterShow"
        :before-close="handleClose"
        :close-on-click-modal=false>
      <div slot="title" style="font-size: 20px;">{{ dialogTitle }}</div>
      <div>
        <CharacterDetailInfoCoreAll
            :key="showCharacterId"
            :character-id="showCharacterId"
        ></CharacterDetailInfoCoreAll>
      </div>
    </el-dialog>

    <!--CBB 内容线各版本及其来源特性版本-->
    <el-dialog
        title="CBB 内容版本演进"
        width="32cm"
        append-to-body
        :visible.sync="lineageVisible"
        :close-on-click-modal=false>
      <RichLineageGraph v-if="lineageVisible" url="/customer/cbb/rich-doc/get-lineage"
                        :params="{ bizId: characterId }"></RichLineageGraph>
    </el-dialog>
  </div>
</template>

<script>

import Swal from "sweetalert2";
import CharacterDetailInfoCoreAll from "@/components/std/character-detail-info-core-all.vue";
import RichLineageGraph from "@/components/rich/rich-lineage-graph.vue";
import {dateFormat, optionKV} from "@/filter/filter";
import CbbTechDocDialog from "@/components/rdms/cbb-tech-doc-dialog.vue";

export default {
  components: {
    CbbTechDocDialog,
    RichLineageGraph,
    CharacterDetailInfoCoreAll,
    // 与 character-detail-info-core 存在循环引用（core → character-doc-tabs → baseline-list → 本组件），须异步加载
    CharacterDetailInfoCore: () => import("@/components/std/character-detail-info-core.vue"),
  },
  name: "cbb-detail-info-page",

  props: {
    characterId: {  //因为数据存储的形式是和Character复用的
      default: null,
    },
    docType: {
      default: null,
    },
    cbbId: {
      default: null,
    },
  },

  data() {
    return {
      loginUser: {},
      loginCustomer: {},
      characterItem: {},

      characterInheritTree: [],
      inheritTree: [],
      DOC_TYPE: DOC_TYPE,

      showCharacterId: '',
      dlgCharacterShow: false,
      dialogTitle: '',
      lineageVisible: false,
      activeTab: 'define',
      whereUsedList: [],
      whereUsedLoading: false,
      whereUsedLoaded: false,
      techDocOpened: false,
      cbbRecord: {},
      CBB_STATUS: CBB_STATUS,
    };
  },

  computed: {
    discardTimeText() {
      return dateFormat(this.cbbRecord.discardTime);
    },
  },

  mounted: function () {
    let _this = this;
    _this.getLoginUser();
    if(_this.characterId){
      _this.getCharacterTree(_this.characterId);
    }
    if(_this.cbbId){
      _this.getCbbRecord();
    }
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

    getCharacterTree(characterId) {
      let _this = this;
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/character/getCharacterTree/' + characterId).then((response) => {
        let resp = response.data;
        if (resp.success) {
          _this.characterInheritTree = resp.content;
          _this.inheritTree = _this.characterInheritTree.iterationCharacterList;
          _this.characterItem = _this.characterInheritTree.thisCharacter;
        } else {
          Toast.warning(resp.message, Swal);
        }
      })
    },

    showCharacterDlg(characterId) {
      let _this = this;
      _this.showCharacterId = characterId;
      _this.dlgCharacterShow = true;
      _this.dialogTitle = "特性定义表";
    },
    closeDialog() {
      let _this = this;
      _this.dlgCharacterShow = false;
    },
    handleClose(done) {
      done();
    },
    onTabClick(tab) {
      if (tab.name === 'whereUsed' && !this.whereUsedLoaded) {
        this.getWhereUsed();
      }
      if (tab.name === 'techDoc') {
        this.techDocOpened = true;
      }
    },
    getCbbRecord() {
      let _this = this;
      let formData = new window.FormData();
      formData.append('cbbId', _this.cbbId);
      formData.append('loginUserId', _this.loginUser.customerUserId);
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/cbb/getCbbRecordDetailById', formData).then((response) => {
        let resp = response.data;
        if (resp.success) {
          _this.cbbRecord = resp.content || {};
        }
      });
    },
    getWhereUsed() {
      let _this = this;
      _this.whereUsedLoading = true;
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/cbb/where-used/' + _this.cbbId).then((response) => {
        _this.whereUsedLoading = false;
        let resp = response.data;
        if (resp.success) {
          _this.whereUsedList = resp.content || [];
          _this.whereUsedLoaded = true;
        } else {
          Toast.warning(resp.message, Swal);
        }
      }).catch(() => {
        _this.whereUsedLoading = false;
      });
    },
    kindFormat(row) {
      return {PROJECT: '主项目', SUBPROJECT: '子项目', CHARACTER: '特性', CBB_DEFINE: 'CBB 定义记录'}[row.kind] || row.kind;
    },
    usageStatusFormat(row) {
      let source = row.kind === 'CHARACTER' || row.kind === 'CBB_DEFINE' ? CHARACTER_STATUS : PROJECT_STATUS;
      return optionKV(source, row.status);
    },
  },
}
</script>

<style lang="scss" scoped>
@import './../../../public/static/scss/caipd';

.paper-height {
  height: calc(100vh - 250px);
}
</style>
