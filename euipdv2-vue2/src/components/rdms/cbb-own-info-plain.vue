<!--
 * Author: 周朔鹏
 * Date: 2025-04-18
 * Email: 391902958@qq.com
 * Website: https://www.euipd.com
-->

<template>
  <div class="ca-plain-container" >
    <div class="ca-plain-form">
      <div class="ca-form-line" style="font-weight: bold">
        <div class="ca-form-label-f">
          <div class="ca-form-label-title">
            <i class="el-icon-star-on"></i>
            特性名称:
          </div>
        </div>
        <div class="ca-form-content">
          <a href="javascript:void(0)" @click="showCharacterInfo(cbbItem)">{{ cbbItem.characterName }}</a>
        </div>
      </div>
      <div>
        <div class="ca-form-line">
          <div class="ca-form-label-xl">
            <div class="ca-form-label-title">
              特性描述:
            </div>
          </div>
        </div>
        <div class="ca-form-line-content"
             v-rich-html="cbbItem.functionDescription">
        </div>
      </div>
      <div>
        <div class="ca-form-line">
          <div class="ca-form-label-xl">
            <div class="ca-form-label-title">
              生效/使用条件:
            </div>
          </div>
        </div>
        <div class="ca-form-line-content"
             v-rich-html="cbbItem.workCondition">
        </div>
      </div>
      <div>
        <div class="ca-form-line">
          <div class="ca-form-label-xl">
            <div class="ca-form-label-title">
              输入/启动逻辑:
            </div>
          </div>
        </div>
        <div class="ca-form-line-content"
             v-rich-html="cbbItem.inputLogicalDesc">
        </div>
      </div>
      <div>
        <div class="ca-form-line">
          <div class="ca-form-label-xl">
            <div class="ca-form-label-title">
              实现过程:
            </div>
          </div>
        </div>
        <div class="ca-form-line-content"
             v-rich-html="cbbItem.functionLogicalDesc">
        </div>
      </div>
      <div>
        <div class="ca-form-line">
          <div class="ca-form-label-xl">
            <div class="ca-form-label-title">
              输出/检验逻辑:
            </div>
          </div>
        </div>
        <div class="ca-form-line-content"
             v-rich-html="cbbItem.outputLogicalDesc">
        </div>
      </div>
      <div v-if="cbbItem.fileList && cbbItem.fileList.length > 0">
        <div class="ca-form-line">
          <div class="ca-form-label">
            <div class="ca-form-label-title">
              功能附件:
            </div>
          </div>
        </div>
        <div v-for="(item, i ) in cbbItem.fileList" :key="i" style="display: flex; flex-direction: row; align-items: center; margin-left: 16px;">
          <div v-if="item">
            {{ item.name }}
          </div>
          <div v-if="item && item.authStatus">
            <el-button type="text" style="margin-left: 25px;" @click="downloadFile(item)">下载</el-button>
          </div>
        </div>
      </div>
      <div v-if="cbbItem.demandList && cbbItem.demandList.length > 0">
        <div class="ca-form-line">
          <div class="ca-form-label">
            <div class="ca-form-label-title">
              需求列表:
            </div>
          </div>
        </div>
        <div v-for="(item, i ) in cbbItem.demandList" :key="i" style="display: flex; flex-direction: row; margin-left: 16px;">
<!--          <div>
            {{ item.demandName }}
          </div>-->
          <div>
            <a href="javascript:void(0)" @click="openDemandItemDlg(item)"> {{ item.demandName }}</a>
          </div>
        </div>
      </div>
      <div class="hr hr-10 dotted"></div>
      <div class="ca-form-line">
        <div class="ca-form-label">
          <div class="ca-form-label-title">
            编制人:
          </div>
        </div>
        <div class="ca-form-content">
          {{ cbbItem.writerName }}
        </div>
      </div>
      <div class="ca-form-line">
        <div class="ca-form-label">
          <div class="ca-form-label-title">
            编制时间:
          </div>
        </div>
        <div class="ca-form-content">
          {{ cbbItem.createTime | dateFormat }}
        </div>
      </div>
      <div v-if="showBudget && projectType !== PROJECT_TYPE.PRE_PROJECT.key">
        <div class="ca-form-line">
          <div class="ca-form-label">
            <div class="ca-form-label-title">
              开发预算:
            </div>
          </div>
          <div class="ca-form-content">
            {{ cbbItem.devManhourApproved }} 工时
          </div>
        </div>
        <div class="ca-form-line">
          <div class="ca-form-label">
            <div class="ca-form-label-title">
              测试预算:
            </div>
          </div>
          <div class="ca-form-content">
            {{ cbbItem.testManhourApproved }} 工时
          </div>
        </div>
        <div class="ca-form-line">
          <div class="ca-form-label">
            <div class="ca-form-label-title">
              物料费用:
            </div>
          </div>
          <div class="ca-form-content">
            {{ cbbItem.materialFeeApproved  | rmbFormat }}
          </div>
        </div>
        <div class="ca-form-line">
          <div class="ca-form-label">
            <div class="ca-form-label-title">
              其他费用:
            </div>
          </div>
          <div class="ca-form-content">
            {{ cbbItem.sumOtherFee  | rmbFormat }}
          </div>
        </div>
        <div class="ca-form-line">
          <div class="ca-form-label">
            <div class="ca-form-label-title">
              总预算:
            </div>
          </div>
          <div class="ca-form-content">
            {{ cbbItem.budget  | rmbFormat }}
          </div>
        </div>
      </div>
      <div v-if="cbbItem && cbbItem.referenceCharacterList">
        <div class="hr hr-10 dotted"></div>
        <div class="ca-form-line">
          <div class="ca-form-label-xl">
            <div class="ca-form-label-title">
              参考特性:
            </div>
          </div>
        </div>
        <div v-for="(item, i ) in cbbItem.referenceCharacterList" :key="i"
             style="margin-left: 16px; line-height: 25px; font-weight: 500;">
          <div v-if="item">
            <a href="javascript:void(0);" @click="openReference(item)">
              {{ item.characterName }}
            </a>
                <el-tag v-if="item.referenceCbbStatus === CBB_STATUS.DISABLED.key || item.referenceCbbStatus === CBB_STATUS.DISCARD.key"
                        size="mini" type="warning" style="margin-left: 6px;">{{ CBB_STATUS | optionKV(item.referenceCbbStatus) }}</el-tag>
          </div>
        </div>
      </div>
      <div>
        <div class="hr hr-10 dotted"></div>
        <CharacterInheritList
            :key="cbbId"
            :character-id="cbbId"
        ></CharacterInheritList>
      </div>

    </div>

    <!--显示特性定义表-->
    <el-dialog
        :visible.sync="showCharacterDlg"
        width="32cm"
        :before-close="handleClose"
        :close-on-click-modal=false>
      <div slot="title" style="font-size: 20px;">{{ dialogTitle }}</div>
      <div v-if="showCharacterDlg && characterItemId">
        <CharacterDocTabs
            :key="characterItemId"
            :character-id="characterItemId"
        ></CharacterDocTabs>
      </div>
    </el-dialog>

    <!--显示原始需求-->
    <el-dialog
        :visible.sync="demandDlgShow"
        width="29cm"
        :before-close="handleClose"
        :close-on-click-modal=false>
      <div slot="title" style="font-size: 20px;">{{ dialogTitle }}</div>
      <div v-if="demandDlgShow">
        <demand-detail-dialog
            :key="demandItem.id"
            :demand-item="demandItem"
            :close-function="closeDialog"
        ></demand-detail-dialog>
      </div>
    </el-dialog>
    <CbbTechDocDialog :cbb-id="techDocCbbId" :visible.sync="techDocVisible"></CbbTechDocDialog>
  </div>
</template>

<script>
import CbbTechDocDialog from "@/components/rdms/cbb-tech-doc-dialog.vue";
import FileRdms from "@/components/rdms/file-rdms";
import DemandDetailDialog from "@/components/rdms/demand-detail-dlg";
import Swal from "sweetalert2";
import JobitemDetailInfoDlg from "@/components/std/jobitem-detail-info-dlg.vue";
import CharacterDocTabs from "@/components/rdms/character-doc-tabs.vue";
import CharacterInheritList from "@/components/std/character-inherit-list.vue";

export default {
  components: {
    CbbTechDocDialog,
    CharacterDocTabs,
    JobitemDetailInfoDlg,
    FileRdms,
    DemandDetailDialog,
    CharacterInheritList,
  },
  name: "cbb-own-info-plain",

  props: {
    cbbId: {
      default: null,
    },
    projectType: {
      default: null,
    },
    showBudget: {
      default: false,
    }
  },

  data: function () {
    return {
      techDocCbbId: null,
      techDocVisible: false,
      CBB_STATUS: CBB_STATUS,
      loginUser: {},
      loginCustomer: {},

      cbbItem: {},
      characterItemId: '',

      jobitem: {
        propertyDto: {
          jobDescription: '',
        },
      },
      assistJobitemList: [],
      characterOriginInfo: {},
      dlgJobItem: {},

      dialogTitle: '',
      showCharacterDlg: false,
      demandItem: {},
      demandDlgShow: false,

      fileItems: [],
      resFile: {},
      resFiles: [],
      resFileIds: [],
      PROJECT_TYPE: PROJECT_TYPE,
    }
  },

  computed: {},

  mounted: function () {
    let _this = this;
    _this.getLoginUser();
    if(Tool.isNotEmpty(_this.cbbId)){
      _this.getCbbRecordDetailById(_this.cbbId);
    }
  },

  methods: {
    openReference(item) {
      if (item.referenceCbbId) {
        this.techDocCbbId = item.referenceCbbId;
        this.techDocVisible = true;
        return;
      }
      this.showCharacterInfo(item);
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

    getCbbRecordDetailById(cbbId) {
      let _this = this;
      let formData = new window.FormData();
      formData.append('cbbId', cbbId);
      formData.append('loginUserId', _this.loginUser.customerUserId);
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/cbb/getCbbRecordDetailById', formData).then((response) => {
        let resp = response.data;
        if (resp.success) {
          _this.cbbItem = resp.content;
        }else{
          Toast.warning(resp.message, Swal);
        }
      });
    },

    showCharacterInfo(item){
      let _this = this;
      _this.dialogTitle = '特性定义表';
      if(item.jobitemType === JOB_ITEM_TYPE.UPDATE.key){
        _this.characterItemId = item.parent;
      }else{
        _this.characterItemId = item.id;
      }

      _this.showCharacterDlg = true;

    },
    openDemandItemDlg(demandItem){
      let _this = this;
      _this.demandItem = demandItem;
      _this.demandDlgShow = true;

    },
    handleClose(done) {
      done(); //el-dialog用于关闭对话框的内置函数.
    },

    closeDialog() {
      let _this = this;
      _this.showCharacterDlg = false;
      _this.demandDlgShow = false;
    },

    downloadFile(file) {
      let _this = this;
      _this.getSignedUrl(file.path, file.suffix);
    },
    getSignedUrl (fileFullName, suffix) {
      let _this = this;
      let formData = new window.FormData();
      formData.append('fileFullName', fileFullName);
      formData.append('customer', _this.loginCustomer.id);
      formData.append('user', _this.loginUser.customerUserId);
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/file/rdms/oss-get-signed-url', formData).then((response)=>{
        let resp = response.data;
        Tool.openOssSignedUrl(resp, suffix);
        // FileSaver.saveAs(resp.content, fileName);
      });
    },

  }
}
</script>

<style lang="scss" scoped>
@import './../../../public/static/scss/caipd.scss';

.original-container{
  height: 100%;
  margin: 12px 2px 5px 15px;
  padding-right: 10px;
  //min-width: 360px;
  overflow-y: scroll;
}
</style>
