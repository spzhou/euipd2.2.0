<!--
 * Author: 周朔鹏
 * Date: 2025-02-26
 * Email: 391902958@qq.com
 * Website: https://www.euipd.com
-->

<template>
  <div style="height: 100%;">
    <div>
      <div v-if="characterTree">
        <div v-if="characterTree.iterationCharacterList && characterTree.iterationCharacterList.length > 0">
          <div v-for="(item, index) in characterTree.iterationCharacterList"
               style="display: flex; flex-direction: row; justify-content: left; align-items: center; line-height: 24px;">
            <div style="width: 90px;">
              继承版本-{{ index + 1 }}:
            </div>
            <div style="width: 400px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap;">
              <a href="javascript:void(0);" @click="showCharacterDlg(item.id)">
                {{ item.characterName }}
              </a>
            </div>
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
        <CharacterDetailInfoCoreAll
            :key="showCharacterId"
            :character-id="showCharacterId"
        ></CharacterDetailInfoCoreAll>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import Swal from "sweetalert2";
import {optionKV, rmbFormat, shortDateFormat} from "@/filter/filter";
import FileSaver from 'file-saver';
import FeeApproveDlg from "@/components/std/fee-approve-dlg.vue";
import QualityDetailPlain from "@/components/rdms/quality-detail-plain.vue";
import CharacterDetailInfoCoreAll from "@/components/std/character-detail-info-core-all.vue";

export default {
  components: {
    CharacterDetailInfoCoreAll,
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
