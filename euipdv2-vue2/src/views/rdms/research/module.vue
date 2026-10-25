<!--
 * Author: 周朔鹏
 * Date: 2025-04-18
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
                CBB 列表
              </a>
            </li>

            <!--CBB描述页面-->
            <DynamicTabItem
                v-for="(item, index) in cbbDetailList"
                :key="item.id"
                :index="index"
                :active-page-id="activePageId"
                prefix="cbb-detail-item"
                :close="closeFunction_character"
                @select="tabManageClick">{{ ('CBB : ' + (item.characterName)) }}
            </DynamicTabItem>
          
        </template>

                        <DynamicTabPane
                page-id="home"
                :active-page-id="activePageId">
                            <template v-slot:default>
              <p>特性仓库: 管理经评审发布的 CBB（标准构建单元）；特性级 CBB 由通过评审的特性发布，集成级 CBB 由子项目集成成果经 CBB 定义发布.</p>
              <div class="hr hr-18 dotted hr-double"></div>
              <div>
                <div style="display: flex; justify-content: space-between;">
                  <pagination ref="pagination_module" v-bind:list="listModule" v-bind:itemCount="6"></pagination>
                  <div class="cbb-filter-bar">
                    <el-select v-model="filter.status" size="small" clearable placeholder="全部状态" style="width: 120px" @change="listModule(1)">
                      <el-option v-for="item in listedStatuses" :key="item.key" :label="item.value" :value="item.key"></el-option>
                    </el-select>
                    <el-select v-model="filter.type" size="small" clearable placeholder="全部类型" style="width: 120px" @change="listModule(1)">
                      <el-option label="特性级 CBB" :value="DOC_TYPE.CHARACTER.key"></el-option>
                      <el-option label="集成级 CBB" :value="DOC_TYPE.PROJECT.key"></el-option>
                    </el-select>
                    <el-input v-model="filter.keyWord" size="small" clearable placeholder="名称或发布版本" style="width: 180px"
                              @keyup.enter.native="listModule(1)" @clear="listModule(1)"></el-input>
                    <el-button size="small" type="primary" plain @click="listModule(1)">查询</el-button>
                  </div>
                </div>
                <el-table
                    :data="moduleList"
                    border
                    stripe
                    highlight-current-row
                    style="width: 100%"
                    @row-click="rowClick">
                  <el-table-column
                      prop="id"
                      align="center"
                      label="CBB ID"
                      width="200"
                  ></el-table-column>
                  <el-table-column
                      prop="cbbName"
                      show-overflow-tooltip
                      align="center"
                      min-width="200"
                      label="CBB 名称">
                    <template slot-scope="scope">
                      {{ scope.row.cbbName }}
                      <el-tooltip v-if="scope.row.sourceGroupSize > 1" content="同一承载特性先后发布的 CBB，按发布版本由新到旧排列" placement="top" effect="light" :open-delay=1000>
                        <el-tag size="mini" type="info">同源 {{ scope.row.sourceGroupIndex }}/{{ scope.row.sourceGroupSize }}</el-tag>
                      </el-tooltip>
                    </template>
                  </el-table-column>
                  <el-table-column
                      align="center"
                      show-overflow-tooltip
                      label="承载特性"
                      min-width="160">
                    <template slot-scope="scope">
                      {{ scope.row.character ? scope.row.character.characterName : '' }}
                      <span v-if="scope.row.sourceSerial" style="color: #909399;">（{{ scope.row.sourceSerial }}）</span>
                    </template>
                  </el-table-column>
                  <el-table-column
                      prop="createrName"
                      align="center"
                      min-width="80"
                      label="创建人"
                  ></el-table-column>
                  <el-table-column
                      prop="systemManagerName"
                      align="center"
                      label="系统工程师"
                      min-width="80"
                  ></el-table-column>
                  <el-table-column
                      prop="projectManagerName"
                      align="center"
                      label="项目经理"
                      min-width="80"
                  ></el-table-column>
                  <el-table-column
                      prop="productManagerName"
                      align="center"
                      label="产品经理"
                      min-width="80"
                  ></el-table-column>
                  <el-table-column
                      prop="devVersion"
                      align="center"
                      :formatter="versionFormat"
                      label="发布版本"
                      width="150"
                  ></el-table-column>
                  <el-table-column
                      prop="status"
                      :formatter="statusFormat"
                      align="center"
                      label="状态"
                      width="100"
                  ></el-table-column>
                  <el-table-column
                      prop="releaseTime"
                      :formatter="releaseTimeFormat"
                      align="center"
                      label="发布时间"
                      width="160"
                  ></el-table-column>
                  <el-table-column
                      fixed="right"
                      label="操作"
                      align="center"
                      width="340">
                    <template slot-scope="scope">
                      <el-tooltip content="查看 CBB 的描述与相关信息" placement="top" effect="light" :open-delay=1000>
                        <el-button type="primary" plain size="small" v-on:click.stop="cbbDetailShow(scope.row)">
                          CBB 描述
                        </el-button>
                      </el-tooltip>
                      <template v-if="canManage(scope.row)">
                        <el-tooltip content="停用后不再出现在选用候选中，已有引用不变" placement="top" effect="light" :open-delay=1000>
                          <el-button v-if="scope.row.status === CBB_STATUS.ARCHIVED.key" type="warning" plain size="small" v-on:click.stop="toggleDisabled(scope.row, true)">
                            停用
                          </el-button>
                        </el-tooltip>
                        <el-button v-if="scope.row.status === CBB_STATUS.DISABLED.key" type="success" plain size="small" v-on:click.stop="toggleDisabled(scope.row, false)">
                          启用
                        </el-button>
                        <el-tooltip content="废弃后不再出现在选用候选中，不可恢复；已有引用不变" placement="top" effect="light" :open-delay=1000>
                          <el-button v-if="scope.row.status === CBB_STATUS.ARCHIVED.key || scope.row.status === CBB_STATUS.DISABLED.key"
                                     type="danger" plain size="small" v-on:click.stop="openDiscardDialog(scope.row)">
                            废弃
                          </el-button>
                        </el-tooltip>
                        <el-button v-if="scope.row.status !== CBB_STATUS.DISCARD.key" type="info" plain size="small" v-on:click.stop="openOwnerDialog(scope.row)">
                          负责人
                        </el-button>
                      </template>
                    </template>
                  </el-table-column>
                </el-table>
                <div style="display: flex; flex-direction: row; justify-content: flex-end;">
                  <small>提示：CBB 发布后不再修改。需要新的 CBB 时，迭代其承载特性，评审通过后重新发布；不再推荐使用的 CBB 可停用或废弃。</small>
                </div>
              </div>
            
            
                            </template></DynamicTabPane>

            <!-- CBB描述页面-->
            <DynamicTabPane
                v-for="(item, index) in cbbDetailList"
                :key="('character-' + (item.id))"
                :index="index"
                prefix="cbb-detail-item"
                :active-page-id="activePageId">
                <template v-slot:default>
              <div>
                <CbbDetailInfoPage
                    :key="item.id"
                    :character-id="item.id"
                    :cbb-id="item.cbbId"
                    :doc-type="DOC_TYPE.CHARACTER.key"
                ></CbbDetailInfoPage>
              </div>
            
                </template></DynamicTabPane>
          
    </WorkspaceFrame>

    <el-dialog title="变更 CBB 负责人" :visible.sync="ownerDialog.visible" width="420px" append-to-body>
      <p>当前负责人：{{ ownerDialog.row.productManagerName || '未指定' }}</p>
      <el-select v-model="ownerDialog.ownerId" filterable placeholder="选择新的负责人" style="width: 100%">
        <el-option v-for="w in workers" :key="w.id" :label="w.trueName" :value="w.id"></el-option>
      </el-select>
      <small>变更该 CBB 的产品经理；历史版本保持原负责人，已废弃的 CBB 不可变更。</small>
      <span slot="footer">
        <el-button size="small" @click="ownerDialog.visible = false">取消</el-button>
        <el-button size="small" type="primary" :disabled="!ownerDialog.ownerId" @click="changeOwner">确定</el-button>
      </span>
    </el-dialog>

    <el-dialog title="废弃 CBB" :visible.sync="discardDialog.visible" width="480px" append-to-body>
      <p>CBB：{{ discardDialog.row.cbbName }}</p>
      <el-input v-model="discardDialog.reason" type="textarea" :rows="4" maxlength="500" show-word-limit
                placeholder="请填写废弃原因（10 至 500 字），将随废弃通知发给引用方"></el-input>
      <small>废弃后不可恢复，不再出现在选用候选中；已有引用与物料清单不变，引用方仍可查看技术文档。</small>
      <span slot="footer">
        <el-button size="small" @click="discardDialog.visible = false">取消</el-button>
        <el-button size="small" type="danger" :disabled="discardReasonLength < 10" @click="discard">确定废弃</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
import Pagination from "@/components/pagination";
import {closeDynamicTab, openOrFocusTab} from "@/shared/dynamic-tab";
import DynamicTabItem from "@/components/rdms/dynamic-tab-item";
import DynamicTabPane from "@/components/rdms/dynamic-tab-pane";
import WorkspaceFrame from "@/components/rdms/workspace-frame";
import CharacterJoblistTab from "@/components/rdms/character-joblist-tab";

import File from "@/components/rdms/file";
import Swal from "sweetalert2";
import {dateFormat, fixed2fractional, optionKV} from "@/filter/filter";
import CbbDetailInfoPage from "@/components/rdms/cbb-detail-info-page.vue";

export default {
  components: {
    Pagination,
    File,
    DynamicTabItem,
    DynamicTabPane,
    WorkspaceFrame,
    CharacterJoblistTab,
    CbbDetailInfoPage,
  },
  name: "module",
  data: function () {
    return {
      loginUser: {},
      loginCustomer: {},
      moduleList: [],

      activePageId: 'home',

      respContent: {},
      cbbItem: {},
      cbbDetailList: [],

      dialogVisible: false,

      DOC_TYPE: DOC_TYPE,
      RD_TYPE: RD_TYPE,
      CHARACTER_STATUS: CHARACTER_STATUS,
      CBB_STATUS: CBB_STATUS,
      listedStatuses: [CBB_STATUS.ARCHIVED, CBB_STATUS.DISABLED, CBB_STATUS.DISCARD],
      filter: {status: '', type: '', keyWord: ''},
      workers: [],
      ownerDialog: {visible: false, row: {}, ownerId: ''},
      discardDialog: {visible: false, row: {}, reason: ''},

      dialogTitle: 'Dialog',
      project: {},
      setupProjectItem: {}, //用于处理立项数据

      timeStamp: '',

      gitProjectList: [],

      //枚举类型的使用
      dialogFlag: {
        type: String,
        validator: function (value) {
          return ["SETUP", "CREATE", "EDIT"].includes(value);
        },
      },
    }
  },
  mounted: function () {
    let _this = this;
    _this.getLoginUser();
    _this.$refs.pagination_module.size = 12;
    _this.listModule(1);
  },

  computed: {
    discardReasonLength() {
      return (this.discardDialog.reason || '').trim().length;
    },
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

    releaseTimeFormat(row, column){
      return dateFormat(row.releaseTime);
    },
    statusFormat(row, column) {
      return optionKV(CBB_STATUS, row.status);
    },
    canManage(row) {
      let roleList = (Tool.getLoginUser() || {}).roleList;
      return row.productManagerId === this.loginUser.customerUserId
        || (typeof RoleAccess !== 'undefined' && RoleAccess.hasAny(['SYSGM', 'UltiMng'], roleList));
    },
    toggleDisabled(row, disable) {
      let _this = this;
      let action = disable ? 'disable' : 'enable';
      Swal.fire({
        title: disable ? '停用该 CBB？' : '启用该 CBB？',
        text: disable ? '停用后不再出现在选用候选中，已有引用不变。' : '启用后重新出现在选用候选中。',
        showCancelButton: true,
        confirmButtonText: '确定',
        cancelButtonText: '取消',
      }).then((result) => {
        if (!result.value) {
          return;
        }
        _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/cbb/' + action + '/' + row.id).then((response) => {
          let resp = response.data;
          if (resp.success) {
            Toast.success(resp.message, Swal);
            _this.listModule(1);
          } else {
            Toast.warning(resp.message, Swal);
          }
        });
      });
    },
    openDiscardDialog(row) {
      this.discardDialog = {visible: true, row: row, reason: ''};
    },
    discard() {
      let _this = this;
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/cbb/discard/' + _this.discardDialog.row.id,
          {reason: _this.discardDialog.reason.trim()}).then((response) => {
        let resp = response.data;
        if (resp.success) {
          Toast.success(resp.message, Swal);
          _this.discardDialog.visible = false;
          _this.listModule(1);
        } else {
          Toast.warning(resp.message, Swal);
        }
      });
    },
    openOwnerDialog(row) {
      let _this = this;
      _this.ownerDialog = {visible: true, row: row, ownerId: ''};
      if (_this.workers.length === 0) {
        _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/worker/listAllWorkers/' + _this.loginCustomer.id).then((response) => {
          let resp = response.data;
          if (resp.success) {
            _this.workers = resp.content || [];
          }
        });
      }
    },
    changeOwner() {
      let _this = this;
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/cbb/change-owner/' + _this.ownerDialog.row.id + '/' + _this.ownerDialog.ownerId).then((response) => {
        let resp = response.data;
        if (resp.success) {
          Toast.success('已变更 ' + resp.content + ' 条记录', Swal);
          _this.ownerDialog.visible = false;
          _this.listModule(1);
        } else {
          Toast.warning(resp.message, Swal);
        }
      });
    },
    //tab点击事件
    tabManageClick(i) {
      let _this = this;
      _this.activePageId = `cbb-detail-item-${i}`;
    },

    versionFormat(row ) {
      return row.devVersion == null ? '' : String(row.devVersion);
    },

    tabHomeClick() {
      let _this = this;
      _this.activePageId = 'home';
      _this.listModule(1);
    },

    gitConnectTest(){
      let _this = this;
      _this.$ajax.get(process.env.VUE_APP_SERVER + '/customer/api/gitlab/projects').then((response)=>{
        let resp = response;
        if (resp.status===200) {
          _this.gitProjectList = resp.data;
        }
      })
    },

    rowClick(row ){
      let _this = this;
      _this.cbbDetailShow(row);
    },

    cbbDetailShow(cbbItem) {
      openOrFocusTab(this, this.cbbDetailList, Object.assign({}, cbbItem.character, {cbbId: cbbItem.id}), 'cbb-detail-item');
    },
    closeFunction_character(index) {
      closeDynamicTab(this, {
        list: this.cbbDetailList,
        index: index,
        prefix: 'cbb-detail-item',
        fallback: 'home',
        afterClose: () => this.listModule(1),
      });
    },

    handleClose(done) {
      done(); //el-dialog用于关闭对话框的内置函数.
      this.listModule(1);
    },

    listModule(page) {
      let _this = this;
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/cbb/list-module',{
        page: page,
        size: _this.$refs.pagination_module.size,
        customerId: _this.loginCustomer.id,
        loginUserId: _this.loginUser.customerUserId,
        status: _this.filter.status || null,
        type: _this.filter.type || null,
        keyWord: _this.filter.keyWord || null,
      }).then((response)=>{
        let resp = response.data;
        if (resp.success) {
          _this.respContent = resp.content;
          _this.moduleList = resp.content.list;
          if(_this.moduleList && _this.moduleList.length>0){
            _this.$refs.pagination_module.render(page, resp.content.total);
          }

        } else {
          Toast.warning(resp.message,Swal);
        }
      })
    },

    closeDialog() {
      let _this = this;
      _this.dialogVisible = false;
      _this.listModule(1);
    },
  }
}
</script>

<style lang="scss">
@import './../../../../public/static/scss/caipd.scss';

.cbb-filter-bar {
  display: flex;
  gap: 8px;
  align-items: center;
}

.el-badge__content.is-dot {
  height: 10px;
  width: 10px;
  padding: 0;
  right: -2px !important;
  border-radius: 50%;
}
</style>
