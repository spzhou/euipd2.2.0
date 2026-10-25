<!--
 * Author: 周朔鹏
 * Date: 2025-02-26
 * Email: 391902958@qq.com
 * Website: https://www.euipd.com
-->

<template>
  <div style="height: 100%;">
    <div>
      <div style="display: flex; flex-direction: row; justify-content: center; align-items: center;">
        <div v-if="characterTree" style="width: 800px; font-size: 14px;">
          <div v-if="characterTree.rootCharacter" style="display: flex; flex-direction: row; justify-content: center; align-items: center; line-height: 24px;">
            <div style="width: 90px;">
              原始功能:
            </div>
            <div style="width: 400px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap;">
              <a href="javascript:void(0);" @click="showCharacterDlg(characterTree.rootCharacter.id)">
                {{characterTree.rootCharacter.characterName}}
              </a>
            </div>
            <div>
              {{characterTree.rootCharacter.updateTime | dateFormat}}
            </div>
          </div>
          <div v-if="characterTree.thisCharacter" style="display: flex; flex-direction: row; justify-content: left; align-items: center; line-height: 24px; font-weight: 550;">
            <div style="width: 90px;">
              本条功能:
            </div>
            <div style="width: 400px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap;">
              <a href="javascript:void(0);" @click="showCharacterDlg(characterTree.thisCharacter.id)">
                {{characterTree.thisCharacter.characterName}}
              </a>
            </div>
            <div>
              {{characterTree.thisCharacter.updateTime | dateFormat}}
            </div>

          </div>
          <div  v-if="characterTree.iterationCharacterList && characterTree.iterationCharacterList.length > 0">
            <div v-for="(item, index) in characterTree.iterationCharacterList" style="display: flex; flex-direction: row; justify-content: left; align-items: center; line-height: 24px;">
              <div style="width: 90px;">
                继承版本-{{index + 1}}:
              </div>
              <div style="width: 400px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap;">
                <a href="javascript:void(0);" @click="showCharacterDlg(item.id)">
                  {{item.characterName}}
                </a>
              </div>
              <div style="width: 250px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap;">
                {{item.updateTime | dateFormat}}
              </div>
            </div>
          </div>
          <div v-if="characterTree.versionList && characterTree.versionList.length > 0" style="margin-top: 16px;">
            <div style="line-height: 28px; font-weight: 550;">特性版本（修订历史）</div>
            <el-table :data="characterTree.versionList.slice().reverse()" border size="mini" style="width: 100%">
              <el-table-column prop="versionLabel" label="特性版本" align="center" width="150"></el-table-column>
              <el-table-column prop="characterName" label="当时名称" align="center" min-width="180" show-overflow-tooltip></el-table-column>
              <el-table-column prop="contentVersionLabel" label="内容版本" align="center" width="90"></el-table-column>
              <el-table-column prop="sourceName" label="产生方式" align="center" width="110"></el-table-column>
              <el-table-column label="时间" align="center" width="150">
                <template slot-scope="scope">{{ scope.row.createTime | dateFormat }}</template>
              </el-table-column>
              <el-table-column prop="creatorName" label="操作人" align="center" width="90"></el-table-column>
              <el-table-column label="说明" align="center" width="110">
                <template slot-scope="scope">
                  <el-tag v-if="scope.row.latest" size="mini" type="success">最新</el-tag>
                  <el-tag v-if="scope.row.projectLocked" size="mini" style="margin-left: 4px;">项目锁定</el-tag>
                </template>
              </el-table-column>
            </el-table>
          </div>
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
      <div v-if="dlgCharacterShow">
        <CharacterDetailInfoCoreAll2
            :key="showCharacterId"
            :character-id="showCharacterId"
        ></CharacterDetailInfoCoreAll2>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import Swal from "sweetalert2";
import FeeApproveDlg from "@/components/std/fee-approve-dlg.vue";
import QualityDetailPlain from "@/components/rdms/quality-detail-plain.vue";
import CharacterDetailInfoCoreAll2 from "@/components/std/character-detail-info-core-all2.vue";

export default {
  components: {
    CharacterDetailInfoCoreAll2,
    QualityDetailPlain,
    FeeApproveDlg

  },
  name: "character-tree-page",

  props: {
    characterId: {
      default: null,
    },
  },

  computed: {
  },

  data() {
    return {
      loginUser: {},
      loginCustomer: {},

      characterTree: {},
      showCharacterId: '',

      dlgCharacterShow: false,
      dialogTitle: '',
    };
  },

  mounted: function () {
    let _this = this;
    _this.getLoginUser();
    if (Tool.isNotEmpty(_this.characterId)) {
      _this.getCharacterTree(_this.characterId);

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
          _this.characterTree = resp.content;
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

  },
}
</script>

<style lang="scss" scoped>
@import './../../../public/static/scss/caipd';

.paper-height {
  height: calc(100vh - 255px);
}

</style>
