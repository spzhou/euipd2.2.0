<!--
 * Author: 周朔鹏
 * Date: 2024-11-04
 * Email: 391902958@qq.com
 * Website: https://www.euipd.com
-->

<template>
  <div class="tab-container">
    <div class="tab-left">
      <el-pagination
          @size-change="handleSizeChange"
          @current-change="listApproved"
          :current-page="currentPage"
          :page-sizes="[5, 15, 30, 50, 100]"
          :page-size="15"
          layout="sizes, prev, pager, next, total"
          :total="total">
      </el-pagination>

      <div style="height: calc(100vh - 278px); overflow: scroll; overflow-x: hidden">
        <el-table
            ref="approveItemTable"
            :data="items"
            border
            stripe
            style="width: 100%;"
            highlight-current-row
            @row-click="rowClick">
          <el-table-column   label="序号" align="center" width="50">
            <template slot-scope="scope">
              {{scope.$index+1}}
            </template>
          </el-table-column>
          <el-table-column
              prop="characterName"
              label="特性名称"
              min-width="140">
          </el-table-column>
          <el-table-column
              fixed="right"
              prop="createTimeStr"
              :formatter="dateFormat"
              label="编写日期"
              width="90">
          </el-table-column>
          <el-table-column
              fixed="right"
              width="35">
            <template slot-scope="scope">
              <i v-show="scope.row.id === currentRow.id" class="el-icon-caret-right" style="font-size: 24px; color: #4794EF;"></i>
            </template>
          </el-table-column>
        </el-table>
      </div>

    </div>
    <div class="tab-right">
      <div v-if="items.length > 0 ">
        <character-item-detail-approved
            :key="characterItem.id"
            ref="characterItemDetail"
            :character="characterItem"
        ></character-item-detail-approved>
      </div>

    </div>
  </div>
</template>

<script>
import File from "../../components/rdms/file";
import Swal from "sweetalert2";
import PaginationSmall from "@/components/pagination-small";
import CharacterItemDetailApproved from "@/components/rdms/character-detail-approved";

export default {
  components: {
    File,
    PaginationSmall,
    CharacterItemDetailApproved,
  },
  name: "character-joblist-approved",

  props: {
    preProject: {
      default: null,
    },
    projectType: {
      default: null,
    }
  },

  data: function () {
    return {
      loginUser: {},
      loginCustomer: {},

      ACTOR_TYPE: ACTOR_TYPE,
      CHARACTER_STATUS: CHARACTER_STATUS,

      items: [],
      currentRow: {},
      // characterEditChanged: false,
      characterItem: {
        preProjectName: '',
        customerName:'',
        productManagerName: '',
        characterName: '',
        functionDescription: '',
        workCondition: '',
        inputLogicalDesc: '',
        functionLogicalDesc: '',
        outputLogicalDesc: '',
        testMethod: '',
      },

      total: 0,
      pageSize: 15,
      currentPage: 1,
    }
  },

  computed: {},

  mounted: function () {
    let _this = this;
    _this.getLoginUser();
    if(Tool.isNotEmpty(_this.preProject)){
      _this.listApproved(1);
    }

    _this.$bus.$on('refresh-character-joblist-approved', (data) => {
      if(data){
        _this.listApproved(1);
      }
    });
  },

  beforeDestroy(){
    let _this = this;
    _this.$bus.$off('refresh-character-joblist-approved');
  },

  methods: {
    handleSizeChange(val) {
      let _this = this;
      _this.pageSize = val;
      _this.listApproved(1)
    },

    dateFormat(row) {
      return row.createTimeStr.substring(0,10);
    },
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

    testEmit(){
      console.log('emitEvent')
    },

    rowClick(row){
      let _this = this;
      _this.currentRow = row; //用于显示当前行的状态
      _this.characterItem = row;
    },

    listApproved(page) {
      let _this = this;
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/character/list-approved',{
        page: page,
        // size: _this.$refs.pagination.size,
        size: _this.pageSize,
        actor: _this.loginUser.customerUserId,
        keyWord: _this.preProject.id,
        type: PROJECT_TYPE.PRE_PROJECT.key,
        loginUserId: _this.loginUser.customerUserId,
      }).then((response)=>{
        let resp = response.data;
        if (resp.success) {
          _this.items = resp.content.list;
          if(_this.items.length > 0){
            _this.currentRow = _this.items[0];
            _this.$refs.approveItemTable.setCurrentRow(_this.currentRow);
            _this.characterItem = _this.currentRow;
            // _this.$refs.pagination.render(page, resp.content.total);
            _this.total = resp.content.total;
          }
          _this.$bus.$emit('refresh-character-joblist-approved', false);
        } else {
          Toast.warning(resp.message,Swal);
        }
      })
    },
  },
}
</script>

<style lang="scss" scoped>
@import './../../../public/static/scss/caipd.scss';

</style>
