<!--
 * Author: 周朔鹏
 * Date: 2025-02-18
 * Email: 391902958@qq.com
 * Website: https://www.euipd.com
-->

<template>
  <div>
    <!--Tab 卡片-->
        <WorkspaceFrame>
        <template v-slot:nav>
            <li :class="{active : activePageId==='home'}">
              <a @click="tabHomeClick">
                <i class="green ace-icon fa fa-home bigger-120"></i>
                特性列表
              </a>
            </li>

            <!--研发管理-->
            <DynamicTabItem
                v-for="(item, index) in characterItemList"
                :key="item.id"
                :index="index"
                :active-page-id="activePageId"
                prefix="develop-character-item"
                :close="closeFunction_character"
                @select="tabCharacterItemClick">{{ ('研发管理 : ' + (item.characterName)) }}
            </DynamicTabItem>

            <!--物料管理-->
            <DynamicTabItem
                v-for="(item, index) in characterMaterialItemList"
                :key="item.id"
                :index="index"
                :active-page-id="activePageId"
                prefix="material-manage-item"
                :close="closeFunction_material"
                @select="tabMaterialItemClick">{{ ('物料管理 : ' + (item.characterName)) }}
            </DynamicTabItem>

            <!--费用管理-->
            <DynamicTabItem
                v-for="(item, index) in characterFeeItemList"
                :key="item.id"
                :index="index"
                :active-page-id="activePageId"
                prefix="fee-manage-item"
                :close="closeFunction_fee"
                @select="tabFeeItemClick">{{ ('费用管理 : ' + (item.characterName)) }}
            </DynamicTabItem>

            <!--参考文献-->
            <DynamicTabItem
                v-for="(item, index) in referenceItemList"
                :key="item.id"
                :index="index"
                :active-page-id="activePageId"
                prefix="reference-manage-item"
                :close="closeFunction_reference"
                @select="tabReferenceItemClick">{{ ('参考文献 : ' + (item.characterName)) }}
            </DynamicTabItem>


          
        </template>

                        <DynamicTabPane
                page-id="home"
                :active-page-id="activePageId">
                            <template v-slot:default>
              <p>研发管理: 是为<strong>项目经理</strong>提供的一个进行研发任务管理的一套功能页面.</p>
              <div class="hr hr-18 dotted hr-double"></div>
              <div>
                <div style="display: flex; justify-content: space-between;">
                  <pagination ref="pagination" v-bind:list="list" v-bind:itemCount="6"></pagination>
                  <div>
                  <!--  这里可以放置按钮-->
                  </div>
                </div>
                <el-table
                    :data="characters"
                    row-key="id"
                    border
                    default-expand-all
                    :row-class-name="tableRowClass"
                    highlight-current-row
                    style="width: 100%"
                    @row-click="rowClick">
                  <el-table-column  label="序号" align="center" width="90">
                    <template slot-scope="scope">
                      {{scope.$index + 1 }}
                    </template>
                  </el-table-column>
                  <el-table-column
                      prop="id"
                      width="100"
                      align="center"
                      label="特性 ID"
                      show-overflow-tooltip
                  ></el-table-column>
                  <el-table-column
                      prop="characterName"
                      align="center"
                      label="特性名称"
                      min-width="250"
                      show-overflow-tooltip
                  ></el-table-column>
                  <el-table-column
                      prop="subprojectName"
                      label="子项目名称"
                      align="center"
                      min-width="250"
                      show-overflow-tooltip
                  ></el-table-column>
                  <el-table-column
                      prop="writerName"
                      label="编写人"
                      align="center"
                      min-width="90"
                  ></el-table-column>
                  <el-table-column
                      prop="productManagerName"
                      label="产品经理"
                      align="center"
                      min-width="90"
                  ></el-table-column>
                  <el-table-column
                      prop="projectManagerName"
                      label="项目经理"
                      align="center"
                      min-width="100"
                  ></el-table-column>
                  <el-table-column
                      prop="budget"
                      :formatter= "budgetFormat"
                      label="预算"
                      align="center"
                      min-width="100"
                  ></el-table-column>
                  <el-table-column
                      prop="status"
                      :formatter="characterStatusFormat"
                      label="开发状态"
                      align="center"
                      min-width="90"
                  ></el-table-column>
                  <el-table-column
                      prop="planCompleteTime"
                      :formatter="shortTimeFormat"
                      label="里程碑时间"
                      align="center"
                      min-width="100"
                  ></el-table-column>
                  <el-table-column
                      fixed="right"
                      label="操作"
                      align="center"
                      :width="getOpeAreaWidth">
                    <template slot-scope="scope">
                      <div>
                        <el-tooltip content="对相关特性/特性开发的任务进行管理" placement="top" effect="light" :open-delay=1000>
                          <el-button type="primary" plain size="small" v-on:click.stop="characterDevManager(scope.row)">
                            <el-badge :is-dot="scope.row.submittedJobNum >0 || scope.row.devCompleteStatus" >
                              研发管理
                            </el-badge>
                          </el-button>
                        </el-tooltip>
                        <el-tooltip content="对相关特性/特性开发所需物料进行管理" placement="top" effect="light" :open-delay=1000>
                          <el-button type="primary" plain size="small" v-on:click.stop="characterMaterialManager(scope.row)">
                            <el-badge :is-dot="scope.row.materialAppNum >0" >
                              物料管理
                            </el-badge>
                          </el-button>
                        </el-tooltip>
                        <el-tooltip content="对相关特性/特性开发过程中其他相关费用进行管理" placement="top" effect="light" :open-delay=1000>
                          <el-button type="primary" plain size="small" v-on:click.stop="characterFeeManager(scope.row)">
                            <el-badge :is-dot="scope.row.feeAppNum >0" >
                              费用管理
                            </el-badge>
                          </el-button>
                        </el-tooltip>
                        <el-tooltip content="打开参阅的特性开发文献" placement="top" effect="light" :open-delay=1000>
                          <el-button type="primary" plain size="small" v-on:click.stop="referenceManager(scope.row)">
                              参考文献
                          </el-button>
                        </el-tooltip>

                      </div>
                    </template>
                  </el-table-column>
                </el-table>
              </div>
            
            
                            </template></DynamicTabPane>

            <!-- 研发管理-->
            <DynamicTabPane
                v-for="(item, index) in characterItemList"
                :key="('develop-character-item-' + (item.id))"
                :index="index"
                prefix="develop-character-item"
                :active-page-id="activePageId">
                <template v-slot:default>
              <DevelopCharacterTaskTab
                  v-if="activePageId === ('develop-character-item-' + (index))"
                  :key="item.id"
                  :character-id="item.id"
                  :job-type="JOB_ITEM_TYPE.DEVELOP.key"
                  :project-type="PROJECT_TYPE.SUB_PROJECT.key"
                  :index="index"
                  :entrance="ENTRANCE_CLASS.DEVELOP.key"
              ></DevelopCharacterTaskTab>
            
                </template></DynamicTabPane>

            <!-- 物料管理-->
            <DynamicTabPane
                v-for="(item, index) in characterMaterialItemList"
                :key="('material-manage-item-' + (item.id))"
                :index="index"
                prefix="material-manage-item"
                :active-page-id="activePageId">
                <template v-slot:default>
              <CharacterMaterialList
                  v-if="activePageId === ('material-manage-item-' + (index))"
                  :key="item.id"
                  :character-id="item.id"
                  :job-type="JOB_ITEM_TYPE.DEVELOP.key"
                  :project-type="PROJECT_TYPE.SUB_PROJECT.key"
                  :index="index"
                  :entrance="ENTRANCE_CLASS.DEVELOP.key"
              ></CharacterMaterialList>
            
                </template></DynamicTabPane>

            <!-- 费用管理-->
            <DynamicTabPane
                v-for="(item, index) in characterFeeItemList"
                :key="('fee-manage-item-' + (item.id))"
                :index="index"
                prefix="fee-manage-item"
                :active-page-id="activePageId">
                <template v-slot:default>
              <CharacterFeeList
                  v-if="activePageId === ('fee-manage-item-' + (index))"
                  :key="item.id"
                  :character-id="item.id"
                  :job-type="JOB_ITEM_TYPE.DEVELOP.key"
                  :project-type="PROJECT_TYPE.SUB_PROJECT.key"
                  :index="index"
                  :entrance="ENTRANCE_CLASS.DEVELOP.key"
              ></CharacterFeeList>
            
                </template></DynamicTabPane>

            <!-- 参考文献-->
            <DynamicTabPane
                v-for="(item, index) in referenceItemList"
                :key="('reference-manage-item-' + (item.id))"
                :index="index"
                prefix="reference-manage-item"
                :active-page-id="activePageId">
                <template v-slot:default>
              <ArchiveReferenceTreeCharacter
                  v-if="activePageId === ('reference-manage-item-' + (index))"
                  :key="item.id"
                  :character-id="item.id"
              ></ArchiveReferenceTreeCharacter>
            
                </template></DynamicTabPane>

          
    </WorkspaceFrame>
  </div>
</template>

<script>
import Pagination from "@/components/pagination";
import {closeDynamicTab, openOrFocusTab} from "@/shared/dynamic-tab";
import DynamicTabItem from "@/components/rdms/dynamic-tab-item";
import DynamicTabPane from "@/components/rdms/dynamic-tab-pane";
import WorkspaceFrame from "@/components/rdms/workspace-frame";
import File from "@/components/rdms/file";
import Swal from "sweetalert2";
import {optionKV, rmbFormat, shortDateFormat} from "@/filter/filter";
import SubprojectTaskTab from "@/components/rdms/subproject-task-tab.vue";
import DevelopCharacterTaskTab from "@/components/rdms/develop-character-task-tab.vue";
import CharacterMaterialList from "@/components/rdms/character-material-list.vue";
import CharacterFeeList from "@/components/rdms/character-fee-list.vue";
import ArchiveReferenceTreeCharacter from "@/components/rdms/archive-reference-tree-character.vue";

export default {
  components: {
    SubprojectTaskTab,
    DevelopCharacterTaskTab,
    Pagination,
    File,
    DynamicTabItem,
    DynamicTabPane,
    WorkspaceFrame,
    CharacterMaterialList,
    CharacterFeeList,
    ArchiveReferenceTreeCharacter,
  },
  name: "develop",
  data: function () {
    return {
      loginUser: {},
      loginCustomer: {},

      activePageId: 'home',

      characterItemList: [],
      characterMaterialItemList: [],
      characterFeeItemList: [],
      referenceItemList: [],

      characters: [],
      projectItems_demand: [],

      ENTRANCE_CLASS: ENTRANCE_CLASS,
      JOB_ITEM_TYPE: JOB_ITEM_TYPE,
      PROJECT_TYPE: PROJECT_TYPE,
    }
  },
  mounted: function () {
    let _this = this;
    _this.getLoginUser();
    _this.$refs.pagination.size = 6;
    _this.list(1);

    _this.$bus.$on('close-develop-tab', (index) => {
      _this.closeFunction_character(index);
    });

    _this.unsubscribeDevelop = _this.$bus.subscribe(['develop'], function () {
      _this.list(1);
    });
  },

  beforeDestroy: function () {
    let _this = this;
    _this.$bus.$off('close-develop-tab');
    if (_this.unsubscribeDevelop) {
      _this.unsubscribeDevelop();
    }
  },

  computed: {
    getOpeAreaWidth(){
      return 400;
    }
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

    shortTimeFormat(row, column){
      return shortDateFormat(row.planCompleteTime);
    },

    budgetFormat(row, column){
      if(row.rdType !== RD_TYPE.RESEARCH.key){
        return rmbFormat(row.budget);
      }else{
        return '-';
      }

    },

    docTypeFormat(row, column) {
      return optionKV(DOC_TYPE, row.docType);
    },

    characterStatusFormat(row, column) {
      return optionKV(CHARACTER_STATUS, row.status);
    },
    tabCharacterItemClick(i) {
      let _this = this;
      _this.activePageId = `develop-character-item-${i}`;
    },

    tabMaterialItemClick(i) {
      let _this = this;
      _this.activePageId = `material-manage-item-${i}`;
    },

    tabFeeItemClick(i) {
      let _this = this;
      _this.activePageId = `fee-manage-item-${i}`;
    },

    tabReferenceItemClick(i) {
      let _this = this;
      _this.activePageId = `reference-manage-item-${i}`;
    },

    tabHomeClick() {
      let _this = this;
      _this.activePageId = 'home';
      _this.list(1);
    },

    characterDevManager(aCharacter) {
      openOrFocusTab(this, this.characterItemList, aCharacter, 'develop-character-item');
    },
    closeFunction_character(index) {
      closeDynamicTab(this, {
        list: this.characterItemList,
        index: index,
        prefix: 'develop-character-item',
        fallback: 'home',
        afterClose: () => this.list(1),
      });
    },

    characterMaterialManager(aCharacter) {
      openOrFocusTab(this, this.characterMaterialItemList, aCharacter, 'material-manage-item');
    },
    closeFunction_material(index) {
      closeDynamicTab(this, {
        list: this.characterMaterialItemList,
        index: index,
        prefix: 'material-manage-item',
        fallback: 'home',
        afterClose: () => this.list(1),
      });
    },

    characterFeeManager(aCharacter) {
      openOrFocusTab(this, this.characterFeeItemList, aCharacter, 'fee-manage-item');
    },
    closeFunction_fee(index) {
      closeDynamicTab(this, {
        list: this.characterFeeItemList,
        index: index,
        prefix: 'fee-manage-item',
        fallback: 'home',
        afterClose: () => this.list(1),
      });
    },

    referenceManager(aCharacter) {
      openOrFocusTab(this, this.referenceItemList, aCharacter, 'reference-manage-item');
    },
    closeFunction_reference(index) {
      closeDynamicTab(this, {
        list: this.referenceItemList,
        index: index,
        prefix: 'reference-manage-item',
        fallback: 'home',
        afterClose: () => this.list(1),
      });
    },

    rowClick(row, column, e){
      let _this = this;
      _this.characterDevManager(row);
    },

    handleClose(done) {
      done(); //el-dialog用于关闭对话框的内置函数.
      this.list(1);
    },

    list(page, retried) {
      let _this = this;
      // 列表页签未选中时不渲染分页组件；切回列表页签会重新查询
      if (!_this.$refs.pagination) {
        if (_this.activePageId === 'home' && !retried) {
          _this.$nextTick(() => _this.list(page, true));
        }
        return;
      }
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/character/listDevelopingCharactersByPjm', {
        page: page,
        size: _this.$refs.pagination.size,
        keyWord: _this.loginCustomer.id, //customerId
        actor: _this.loginUser.customerUserId, //projectManager
        loginUserId: _this.loginUser.customerUserId,
      }).then((response)=>{
        let resp = response.data;
        if (resp.success) {
          _this.characters = resp.content.list;
          _this.$refs.pagination?.render(page, resp.content.total);
        } else {
          Toast.warning(resp.message,Swal);
        }
      })
    },

    tableRowClass({row, rowIndex}) {
      // console.log("row:", Date.parse(row.planCompleteTime));
      if (Date.now() > Date.parse(row.planCompleteTime)) {
        return 'warning-row-3';
      }
      return '';
    }
  }
}
</script>

<style lang="scss">
@import './../../../../public/static/scss/caipd.scss';

.el-badge__content.is-dot {
  height: 10px;
  width: 10px;
  padding: 0;
  right: -2px !important;
  border-radius: 50%;
}
</style>
