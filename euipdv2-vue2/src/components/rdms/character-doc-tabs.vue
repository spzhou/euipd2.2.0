<!--
 * Author: 周朔鹏
 * Date: 2025-02-25
 * Email: 391902958@qq.com
 * Website: https://www.euipd.com
-->

<template>
  <div style="height: 100%;">
    <div class="paper-height">
      <div style="margin-left: 10px;">
        <el-tabs
            v-model="activeName"
            @tab-click="handleClick">
          <el-tab-pane label="特性定义表" name="character-defined">
            <div v-if="activeName === 'character-defined'">
              <CharacterDetailInfoPage
                  :key="characterItemId"
                  :character-id="characterItemId"
                  :doc-type="DOC_TYPE.CHARACTER.key"
              ></CharacterDetailInfoPage>
            </div>
          </el-tab-pane>
          <el-tab-pane label="执行文档汇总" name="doc_summary">
            <div v-if="activeName === 'doc_summary'">
              <CharacterDocInfoPage
                  :key="characterItemId"
                  :is-page-show="true"
                  :show-defined="false"
                  :character-id="characterItemId">
              </CharacterDocInfoPage>
            </div>
          </el-tab-pane>
          <el-tab-pane
              label="BOM清单" name="bom_list">
            <div v-if="activeName === 'bom_list'">
              <CharacterBomList
                  :key="characterItemId"
                  :character-id="characterItemId">
              </CharacterBomList>
            </div>
          </el-tab-pane>
          <el-tab-pane v-if="characterItem.rdType !== RD_TYPE.RESEARCH.key" label="工时投入" name="manhour">
            <div v-if="activeName === 'manhour'">
              <CharacterManhourSummaryPage
                  :key="characterItemId"
                  :character-id="characterItemId">
              </CharacterManhourSummaryPage>
            </div>
          </el-tab-pane>
          <el-tab-pane
              label="综合统计表" name="summary">
            <div v-if="activeName === 'summary'">
              <CharacterExeSummaryPage
                  :key="characterItemId"
                  :character-id="characterItemId">
              </CharacterExeSummaryPage>
            </div>
          </el-tab-pane>
          <el-tab-pane
              label="质量事件" name="quality">
            <div v-if="activeName === 'quality'">
              <CharacterQualityPage
                  :key="characterItemId"
                  :character-id="characterItemId">
              </CharacterQualityPage>
            </div>
          </el-tab-pane>
          <el-tab-pane label="继承关系" name="characterTree">
            <div v-if="activeName === 'characterTree'">
              <CharacterTreePage
                  :key="characterItemId"
                  :character-id="characterItemId">
              </CharacterTreePage>
            </div>
          </el-tab-pane>
          <el-tab-pane label="基线" name="baseline">
            <div v-if="activeName === 'baseline'">
              <BaselineList
                  :key="characterItemId"
                  owner-type="CHARACTER"
                  :owner-id="characterItemId">
              </BaselineList>
            </div>
          </el-tab-pane>

        </el-tabs>
      </div>
    </div>
  </div>
</template>

<script>
import ReviewSheetPage from "@/components/std/review-sheet-page.vue";
import ReviewDevelopPage from "@/components/std/review-develop-page.vue";
import ReviewTestPage from "@/components/std/review-test-page.vue";
import ReviewFeePage from "@/components/std/review-fee-page.vue";
import ReviewSummaryPage from "@/components/std/review-summary-page.vue";
import ReviewBomList from "@/components/std/review-bom-list.vue";
import ReviewManhourPage from "@/components/std/review-manhour-page.vue";
import ReviewDocSummaryPage from "@/components/std/review-doc-summary-page.vue";
import CharacterDocDetailPage from "@/components/rdms/character-doc-detail-page.vue";
import CharacterDocInfoPage from "@/components/std/character-doc-info-page.vue";

import CharacterDetailInfoPage from "@/components/rdms/character-detail-info-page.vue";
import CharacterBomList from "@/components/std/character-bom-list.vue";
import CharacterManhourSummaryPage from "@/components/std/character-manhour-summary-page.vue";
import CharacterExeSummaryPage from "@/components/std/character-exe-summary-page.vue";
import CharacterQualityPage from "@/components/std/character-quality-page.vue";
import CharacterTreePage from "@/components/std/character-tree-page.vue";
import BaselineList from "@/components/std/baseline-list.vue";
import Swal from "sweetalert2";

export default {
  components: {
    CharacterDetailInfoPage,
    CharacterBomList,
    CharacterManhourSummaryPage,
    CharacterExeSummaryPage,
    CharacterQualityPage,

    CharacterDocInfoPage,
    CharacterDocDetailPage,
    ReviewBomList,
    ReviewSheetPage,
    ReviewDevelopPage,
    ReviewTestPage,
    ReviewFeePage,
    ReviewSummaryPage,
    ReviewManhourPage,
    ReviewDocSummaryPage,
    CharacterTreePage,
    BaselineList,
  },
  name: "character-doc-tabs",

  props: {
    characterId: {
      default: null,
    },
    jobType: {
      default: null,
    },
    projectType: {
      default: null,
    },
    index: {
      default: null,
    },
    entrance: {
      default: null,
    },
  },

  data() {
    return {
      loginUser: {},
      loginCustomer: {},
      activeName: 'character-defined',
      jobitemId: '',
      characterItem: {},
      characterItemId: '',
      DOC_TYPE: DOC_TYPE,
      RD_TYPE: RD_TYPE,
    }
  },

  mounted: function () {
    let _this = this;
    _this.getLoginUser();
    if(_this.characterId){
      _this.getCharacterDocDetailById(_this.characterId);
    }

  },

  computed: {
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

    handleClick(tab, event) {
      console.log(tab, event);
    },

    getCharacterDocDetailById(characterId) {
      let _this = this;
      let formData = new window.FormData();
      formData.append('characterId', characterId);
      formData.append('loginUserId', _this.loginUser.customerUserId);
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/character/getCharacterDocDetailById', formData).then((response) => {
        let resp = response.data;
        if (resp.success) {
          _this.characterItem = resp.content;
          _this.characterItemId = _this.characterItem.id;
        }else{
          Toast.warning(resp.message, Swal);
        }
      });
    },

  },
}
</script>

<style lang="scss" scoped>
@import './../../../public/static/scss/caipd';

.paper-height {
  height: calc(100vh - 200px);
}

</style>
