<!--
 * Author: 周朔鹏
 * Date: 2024-03-21
 * Email: 391902958@qq.com
 * Website: https://www.euipd.com
-->

<template>
  <div class="page-content">
    <div class="page-wrapper">
      <div style="display: flex; justify-content: flex-end; margin-bottom: 5px;">
        <div>
          <el-tooltip content="添加职级" placement="top" effect="light" :open-delay=1000>
            <el-button type="primary" size="medium" @click="addJobLevel()">
              添加职级
            </el-button>
          </el-tooltip>
        </div>
      </div>
<!--      <div class="hr hr-18 dotted hr-double"></div>-->
      <el-table
          :data="tableData"
          border
          style="width: 100%;">
        <el-table-column
            type="index"
            label="序号"
            align="center"
            width="50">
        </el-table-column>
        <el-table-column
            prop="levelCode"
            label="职级代码"
            align="center"
            width="180">
        </el-table-column>
        <el-table-column
            prop="leveName"
            label="职级名称"
            width="180"
            align="center">
        </el-table-column>
        <el-table-column
            prop="manHourFee"
            width="180"
            align="center"
            label="工时费率">
        </el-table-column>
        <el-table-column
            fixed="right"
            label="操作"
            align="center"
            width="110">
          <template slot-scope="scope">
            <el-button @click="edit(scope.row)" type="text" size="small">编辑</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!--    添加职级-->
    <el-dialog
        :visible.sync="jobLevelDialogVisible"
        width="23cm"
        :before-close="handleClose"
        :close-on-click-modal=false>
      <div slot="title" style="font-size: 20px;">{{ dialogTitle }}</div>
      <job-level-add
          :key="timeStamp"
          :edit-flag="editFlag"
          :level-item="levelItem">
      </job-level-add>
    </el-dialog>
  </div>
</template>

<script>
import FileRdms from "@/components/rdms/file-rdms";
import JobLevelAdd from "@/components/rdms/job-level-edit.vue";
import Swal from "sweetalert2";

export default {
  components: {
    FileRdms,
    JobLevelAdd,
  },
  name: "job-level",

  data: function() {
    return {
      loginUser: {},
      loginCustomer: {},

      jobLevelDialogVisible: false,
      dialogTitle: "",
      timeStamp: 0,

      tableData: [],

      editFlag: false,
      levelItem: {},
    }
  },

  props: {

  },

  computed: {
  },

  mounted: function() {
    let _this = this;
    _this.getLoginUser();
    _this.listAll()

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

    edit(row) {
      // console.log(row);
      let _this = this;
      _this.timeStamp = Date.now();
      _this.jobLevelDialogVisible = true;
      _this.dialogTitle = "编辑职级";
      _this.editFlag = true;
      _this.levelItem = row;
    },

    addJobLevel() {
      let _this = this;
      _this.timeStamp = Date.now();
      _this.jobLevelDialogVisible = true;
      _this.dialogTitle = "添加职级";
      _this.editFlag = false;
    },

    listAll(){
      let _this = this;
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/job-level/list-all/' + _this.loginCustomer.id).then((response)=>{
        let resp = response.data;
        if (resp.success) {
          _this.tableData = resp.content;
        }else{
          Toast.warning(resp.message,Swal);
        }
      })
    },

    handleClose(done) {
      let _this = this;
      done(); //el-dialog用于关闭对话框的内置函数.
      _this.editFlag = false;
      _this.listAll();
    },

  }
}
</script>

<style lang="scss">
@import './../../../public/static/scss/caipd.scss';
</style>

<style lang="scss" scoped>
.page-wrapper{
  max-width: 700px;
  margin: 0 auto;
}
</style>
