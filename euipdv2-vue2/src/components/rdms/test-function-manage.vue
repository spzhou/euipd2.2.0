<!--
 * Author: 周朔鹏
 * Date: 2025-02-07
 * Email: 391902958@qq.com
 * Website: https://www.euipd.com
-->

<template>
  <div>
    <!--Tab 卡片-->
    <div class="row">
      <div class="col-md-12">
        <div class="tabbable">
          <ul class="nav nav-tabs" id="myTab">
            <li :class="{active : activePageId==='home'}">
              <a @click="tabHomeClick">
                <i class="green ace-icon fa fa-home bigger-120"></i>
                特性列表
              </a>
            </li>

            <!--任务管理-->
            <li v-for="(item, index) in characterItemList"
                :class="{active : activePageId===('test-character-item-' + (index))}">
              <a @click="tabCharacterItemClick(index)">
                <TabButton
                    :key="('test-character-item-' + (index))"
                    :index-in="index"
                    :close-function="closeFunction_character"
                >{{ ('任务管理 : ' + (item.characterName)) }}
                </TabButton>
              </a>
            </li>
          </ul>

          <div class="tab-content">
            <div id="home" class="tab-pane fade"
                 :class="{active : activePageId === 'home', in : activePageId === 'home' }">
              <p>任务管理: 是为<strong>测试主管</strong>提供的一个进行测试任务管理的一套功能页面.</p>
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
                          <el-button type="primary" plain size="small" v-on:click.stop="characterTestManage(scope.row)">
                            <el-badge :is-dot="scope.row.submittedJobNum >0/* || scope.row.devCompleteStatus*/" >
                              任务管理
                            </el-badge>
                          </el-button>
                      </div>
                    </template>
                  </el-table-column>
                </el-table>
              </div>
            </div>

            <!-- 任务管理-->
            <div v-for="(item, index) in characterItemList"
                 :key="('test-character-item-' + (item.id))"
                 v-bind:id="('test-character-item-' + (index))"
                 class="tab-pane fade"
                 :class="{active : activePageId === ('test-character-item-' + (index)), in : activePageId === ('test-character-item-' + (index)) }">
              <TestCharacterTaskTab
                  :key="item.id"
                  :character-id="item.id"
                  :job-type="JOB_ITEM_TYPE.TASK_TEST.key"
                  :project-type="PROJECT_TYPE.PROJECT.key"
                  :index="index"
                  :entrance="ENTRANCE_CLASS.TEST.key"
              ></TestCharacterTaskTab>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import Pagination from "@/components/pagination";
import TabButton from "@/components/rdms/tab-button";
import File from "@/components/rdms/file";
import Swal from "sweetalert2";
import {optionKV, rmbFormat, shortDateFormat} from "@/filter/filter";
import TestCharacterTaskTab from "@/components/rdms/test-character-task-tab.vue";

export default {
  components: {
    TestCharacterTaskTab,
    Pagination,
    File,
    TabButton,
  },
  name: "test-function-manage",

  props: {
    projectId: {
      default: null,
    },
    index: {
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

      activePageId: 'home',

      characterItemList: [],
      characterMaterialItemList: [],
      characterFeeItemList: [],

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

    _this.$bus.$on('refresh-develop-character-list', (data) => {
      if(data){
        _this.list(1);
      }
    });
  },

  beforeDestroy: function () {
    let _this = this;
    _this.$bus.$off('close-develop-tab');
    _this.$bus.$off('refresh-develop-character-list');
  },

  computed: {
    getOpeAreaWidth(){
      return 150;
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
      _this.activePageId = `test-character-item-${i}`;
    },

    tabHomeClick() {
      let _this = this;
      _this.activePageId = 'home';
      _this.list(1);
    },

    characterTestManage(aCharacter) {
      let _this = this;
      let flag = false;
      let indexFlag = 0;
      _this.characterItemList.forEach((characterItem, index) => {
        if (characterItem.id === aCharacter.id) {
          flag = true;
          indexFlag = index;
        }
      })
      if (flag === false) {
        _this.characterItemList.push(aCharacter);
        _this.activePageId = `test-character-item-${_this.characterItemList.length - 1}`;
      } else {
        _this.activePageId = `test-character-item-${indexFlag}`;
      }
    },
    closeFunction_character(index) {
      let _this = this;
      _this.characterItemList.splice(index, 1);
      index--;
      if(index>=0){
        _this.activePageId = `test-character-item-${index}`;
      }else{
        _this.activePageId = 'home';
      }
      _this.list(1);
    },

    rowClick(row, column, e){
      let _this = this;
      _this.characterTestManage(row);
    },

    handleClose(done) {
      done(); //el-dialog用于关闭对话框的内置函数.
      this.list(1);
    },

    list(page) {
      let _this = this;
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/character/listTestCharactersByTm', {
        page: page,
        size: _this.$refs.pagination.size,
        keyWord: _this.projectId,
        customerId: _this.loginCustomer.id, //customerId
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
@import './../../../public/static/scss/caipd.scss';

.el-badge__content.is-dot {
  height: 10px;
  width: 10px;
  padding: 0;
  right: -2px !important;
  border-radius: 50%;
}
</style>
