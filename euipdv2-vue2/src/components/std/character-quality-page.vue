<!--
 * Author: 周朔鹏
 * Date: 2024-11-06
 * Email: 391902958@qq.com
 * Website: https://www.euipd.com
-->

<template>
  <div style="height: 100%;">
    <div class="paper-height">
      <div style="height: 100%; overflow: scroll;">
        <div class="a4-paper-big-simple">
          <div style="margin: 50px 60px;">
            <div style="font-size: 14px; line-height: 28px; margin: 5px 0; font-weight: bold">
              质量事件列表:
            </div>
            <el-table
                :data="qualityList"
                row-key="id"
                border
                stripe
                default-expand-all
                highlight-current-row
                style="width: 100%"
                @row-click="rowClick">
              <el-table-column
                  prop="qualitySerial"
                  width="180"
                  align="center"
                  label="质量ID"
                  show-overflow-tooltip
              ></el-table-column>
              <el-table-column
                  prop="name"
                  label="质量问题名称"
                  align="center"
                  show-overflow-tooltip
              ></el-table-column>
              <el-table-column
                  prop="projectName"
                  label="项目名称"
                  align="center"
                  show-overflow-tooltip
              ></el-table-column>
              <el-table-column
                  prop="subprojectName"
                  label="子项目名称"
                  align="center"
                  show-overflow-tooltip
              ></el-table-column>
              <el-table-column
                  prop="writerName"
                  label="提交人"
                  align="center"
                  width="80"
              ></el-table-column>
              <el-table-column
                  prop="createTime"
                  :formatter="dateFormat"
                  label="发生时间"
                  align="center"
                  width="100"
              ></el-table-column>
            </el-table>
          </div>
        </div>
      </div>
    </div>

    <!--质量信息-->
    <el-dialog
        width="29cm"
        append-to-body
        :visible.sync="feeApproveDlg"
        :before-close="handleClose"
        :close-on-click-modal=false>
      <div slot="title" style="font-size: 20px;">{{ dialogTitle }}</div>
      <div>
        <QualityDetailPlain
            :key="qualityItem.id"
            :quality-id="qualityItem.id"
        ></QualityDetailPlain>
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

export default {
  components: {
    QualityDetailPlain,
    FeeApproveDlg

  },
  name: "character-quality-page",

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

      qualityList: [],
      qualityItem: {},

      feeApproveDlg: false,
      dialogTitle: '',

      JOB_ITEM_STATUS: JOB_ITEM_STATUS,
    };
  },

  mounted: function () {
    let _this = this;
    _this.getLoginUser();
    if (Tool.isNotEmpty(_this.characterId)) {
      _this.listQualityByCharacterId(_this.characterId);

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

    dateFormat(row, column) {
      return shortDateFormat(row.createTime);
    },

    listQualityByCharacterId(characterId) {
      let _this = this;
      let formData = new window.FormData();
      formData.append('characterId', characterId);
      formData.append('loginUserId', _this.loginUser.customerUserId);
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/quality/listQualityByCharacterId', formData).then((response)=>{
        let resp = response.data;
        if (resp.success) {
          _this.qualityList = resp.content;
        } else {
          Toast.warning(resp.message,Swal);
        }
      })
    },

    rowClick(row, column, e) {
      let _this = this;
      _this.qualityInfoShowDlg(row);
    },
    qualityInfoShowDlg(quality) {
      let _this = this;
      _this.qualityItem = quality;
      _this.feeApproveDlg = true;
      _this.dialogTitle = "质量事件及处理信息";
    },
    closeDialog() {
      let _this = this;
      _this.feeApproveDlg = false;
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
