<!--
 * Author: 周朔鹏
 * Date: 2025-02-23
 * Email: 391902958@qq.com
 * Website: https://www.euipd.com
-->

<template>
  <div>
    <div class="row">
      <div style="margin: 0 15px;">
        <p>特性建议列表: 在项目执行过程中, 工作人员可以随时规划或定义产品特性, 并向项目经理提出特性建议, 经过项目经理确认后, 将可以作为正式特性进行开发.</p>
        <div class="hr hr-18 dotted hr-double"></div>
        <div style="display: flex; flex-direction: row; justify-content: space-between; align-items: center; margin-bottom: 2px; margin-top: -10px;">
          <div>
            <pagination id="pagination_suggest" key="pagination_suggest" ref="pagination_suggest" v-bind:list="listSuggestItems" v-bind:itemCount="6"></pagination>
          </div>
          <div>
            <el-button type="primary" size="medium" style="margin-right: 40px;" @click="characterSuggest()">特性建议</el-button>
          </div>
        </div>
        <div>
          <el-table
              :data="characterSuggestList"
              row-key="id"
              border
              default-expand-all
              highlight-current-row
              style="width: 100%"
              @row-click="rowClick">
            <el-table-column label="序号" align="center" width="90">
              <template slot-scope="scope">
                {{ scope.$index + 1 }}
              </template>
            </el-table-column>
            <el-table-column
                prop="characterName"
                min-width="200"
                align="center"
                label="特性名称"
                show-overflow-tooltip
            ></el-table-column>
            <el-table-column
                prop="projectName"
                min-width="180"
                align="center"
                label="项目名称"
                show-overflow-tooltip
            ></el-table-column>
            <el-table-column
                prop="subprojectName"
                min-width="180"
                align="center"
                label="隶属子项目"
                show-overflow-tooltip
            ></el-table-column>
            <el-table-column
                prop="projectManagerName"
                min-width="120"
                show-overflow-tooltip
                sortable
                label="项目经理"
                align="center"
            ></el-table-column>
            <el-table-column
                prop="status"
                sortable
                :formatter="statusFormat"
                label="状态"
                min-width="110"
                align="center"
            ></el-table-column>
            <el-table-column
                prop="writerName"
                label="编制人"
                width="110"
                align="center"
            ></el-table-column>
            <el-table-column
                prop="createTime"
                sortable
                :formatter="dateFormat"
                label="编制日期"
                min-width="150"
                align="center"
            ></el-table-column>
            <el-table-column
                fixed="right"
                label="操作"
                align="center"
                width="160">
              <template slot-scope="scope">
                <div v-if="scope.row.status === CHARACTER_STATUS.SAVED.key">
                  <el-button type="primary" plain size="small" v-on:click.stop="edit(scope.row.id)">
                    编辑
                  </el-button>
                  <el-button type="warning" plain size="small" v-on:click.stop="del(scope.row)">
                    <!--在未提交之前可以删除-->
                    删除
                  </el-button>
                </div>
                <div v-if="scope.row.status === CHARACTER_STATUS.DISCARD.key">
                  <el-button type="warning" plain size="small" v-on:click.stop="del(scope.row)">
                    <!--在未提交之前可以删除-->
                    删除
                  </el-button>
                </div>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </div>
    </div>

    <!--查看特性定义-->
    <el-dialog
        width="29cm"
        :visible.sync="suggestItemShowDlg"
        :before-close="handleClose"
        :close-on-click-modal=false>
      <div style="width: 100%; height: 100%">
        <CharacterDetailInfoDlg
            :key="suggestCharacterId"
            :character-id="suggestCharacterId"
        ></CharacterDetailInfoDlg>
      </div>
    </el-dialog>

    <!--编辑特性建议-->
    <el-dialog
        width="29.0cm"
        :visible.sync="suggestItemEditDlg"
        :before-close="handleClose"
        :close-on-click-modal=false>
      <div slot="title" style="font-size: 20px;">{{ dialogTitle }}</div>
      <div>
        <CharacterSuggestEditDlg
            :key="editCharacterItem.id"
            :suggest-item="editCharacterItem"
            :close-function="closeDialog"
        ></CharacterSuggestEditDlg>
      </div>
    </el-dialog>

    <!--特性建议-->
    <el-dialog
        width="29.0cm"
        :visible.sync="characterSuggestDlg"
        :before-close="handleClose"
        :close-on-click-modal=false>
      <div slot="title" style="font-size: 20px;">{{ dialogTitle }}</div>
      <div>
        <CharacterSuggestCreateDlg
            :key="timeStamp"
            :writer-id="loginUser.customerUserId"
            :close-function="closeDialog"
        ></CharacterSuggestCreateDlg>
      </div>
    </el-dialog>

  </div>
</template>

<script>
import Swal from "sweetalert2";
import {dateFormat, fixed2fractional, optionKV, rmbFormat, shortDateFormat} from "@/filter/filter";
import FeeDealWithDlg from "@/components/rdms/fee-deal-with-dlg.vue";
import Pagination from "@/components/pagination";
import CharacterSuggestCreateDlg from "@/components/rdms/character-suggest-create-dlg.vue";
import CharacterSuggestEditDlg from "@/components/rdms/character-suggest-edit-dlg.vue";
import CharacterDetailInfoDlg from "@/components/rdms/character-detail-info-dlg.vue";

export default {
  components: {
    Pagination,
    FeeDealWithDlg,
    CharacterSuggestCreateDlg,
    CharacterSuggestEditDlg,
    CharacterDetailInfoDlg,
  },
  name: "character-suggest-list",

  props: {
    writerId: {
      default: null,
    }
  },

  data: function () {
    return {
      loginUser: {},
      loginCustomer: {},

      characterSuggestList: [],

      CHARACTER_STATUS: CHARACTER_STATUS,

      suggestItemShowDlg: false,
      suggestItemEditDlg: false,
      characterSuggestDlg: false,
      dialogTitle: '',
      suggestCharacterId: '',
      editCharacterItem: {},

      timeStamp: 0,
      currentPageNo: 1,
    }
  },
  mounted: function () {
    let _this = this;
    _this.$refs.pagination_suggest.size = 12;
    _this.listSuggestItems(_this.currentPageNo);
    _this.getLoginUser();
    _this.timeStamp = new Date().getTime();

    _this.$bus.$on('refresh-character-suggest-list', (data)=>{
      if(data){
        _this.listSuggestItems(_this.currentPageNo);
      }
    });
  },

  beforeDestroy: function () {
    let _this = this;
    _this.$bus.$off('refresh-character-suggest-list');
  },

  methods: {
    dateFormat(row, column) {
      return dateFormat(row.createTime);
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

    statusFormat(row, column) {
      return optionKV(CHARACTER_STATUS, row.status);
    },

    rowClick(row, column, e) {
      let _this = this;
      _this.suggestItemShow(row);
    },

    suggestItemShow(suggestItem) {
      let _this = this;
      _this.suggestCharacterId = suggestItem.id;
      _this.suggestItemShowDlg = true;
      _this.dialogTitle = "查看特性建议";
    },

    edit(characterId) {
      let _this = this;
      //读取功能的 data数据
      let formData = new window.FormData();
      formData.append('characterId', characterId);
      formData.append('loginUserId', _this.loginUser.customerUserId);
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/character/getRecordAndData', formData).then((response) => {
        let resp = response.data;
        if (resp.success) {
          _this.editCharacterItem = resp.content;
          _this.suggestItemEditDlg = true;
          _this.dialogTitle = "编辑特性建议";
        } else {
          Toast.warning(resp.message, Swal);
        }
      });
    },

    characterSuggest() {
      let _this = this;
      _this.characterSuggestDlg = true;
      _this.dialogTitle = "特性建议";
    },

    closeDialog() {
      let _this = this;
      _this.suggestItemShowDlg = false;
      _this.suggestItemEditDlg = false;
      _this.characterSuggestDlg = false;
      _this.timeStamp = new Date().getTime();
    },
    handleClose(done) {
      let _this = this;
      _this.timeStamp = new Date().getTime();
      done();
    },

    listSuggestItems(page) {
      let _this = this;
      _this.currentPageNo = page;
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/character/listSuggestItems', {
        page: page,
        size: _this.$refs.pagination_suggest.size,
        actor: _this.writerId,
        loginUserId: _this.loginUser.customerUserId,
      }).then((response) => {
        let resp = response.data;
        if (resp.success) {
          if(Tool.isNotEmpty_ES5(resp.content)){
            _this.characterSuggestList = resp.content.list;
            if(_this.characterSuggestList){
              _this.$refs.pagination_suggest.render(page, resp.content.total);
            }
          }
        } else {
          Toast.warning(resp.message, Swal);
        }
      })
    },

    del(item){
      let _this = this;
      Swal.fire({
        title: '警告',
        text: "是否确认删除? 删除后, 不可恢复!",
        icon: 'warning',
        showCancelButton: true,
        confirmButtonColor: '#409EFF',
        cancelButtonColor: '#909399',
        confirmButtonText: '确认!',
        cancelButtonText: '取消!'
      }).then((result) => {
        if (result.isConfirmed) {
          _this.$ajax.delete(process.env.VUE_APP_SERVER + '/customer/character/del/' + item.id).then((response) => {
            let resp = response.data;
            if (resp.success) {
              Toast.success("删除成功", Swal);
              _this.$bus.$emit('refresh-manhour-num', true);
              _this.listSuggestItems(_this.currentPageNo);
            } else {
              Toast.warning(resp.message, Swal);
            }
          });
        }
      }).catch(_ => {
      });
    },
  }
}
</script>

<style lang="scss">
@import './../../../public/static/scss/caipd.scss';

.el-table .application-row {
  background: $colorRowApplication;
}

.el-table .approve-row {
  background: $colorRowApproved;
}
.el-table .reject-row {
  background: $colorRowReject;
}
.el-badge__content.is-dot {
  height: 10px;
  width: 10px;
  padding: 0;
  right: -2px !important;
  border-radius: 50%;
}
</style>
