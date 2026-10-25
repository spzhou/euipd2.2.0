<!--
 * Author: 周朔鹏
 * Date: 2025-02-23
 * Email: 391902958@qq.com
 * Website: https://www.euipd.com
-->

<template>
  <div class="simple-plain-container" >
    <div style="display: flex; flex-direction: row; line-height: 30px; font-weight: bold;">
      <div style="width: 90px; text-align: right;">
        特性名称:
      </div>
      <div style="line-height: 30px; margin-left: 6px;">
        <a href="javascript:void(0)" @click="showJobitemInfo(character)">{{ character.characterName }}</a>
      </div>
      <div class="hr hr-6 dotted"></div>
    </div>
    <div style="display: flex; flex-direction: row; line-height: 30px; font-weight: bold;">
      <div style="width: 90px; text-align: right;">
        特性 ID:
      </div>
      <div style="line-height: 30px; margin-left: 6px;">
        {{ character.characterSerialNo }}
      </div>
      <div class="hr hr-6 dotted"></div>
    </div>
    <div style="display: flex; flex-direction: row; line-height: 30px; font-weight: bold;">
      <div style="width: 90px; text-align: right;">
        撰写人:
      </div>
      <div style="line-height: 30px; margin-left: 6px;">
        {{ character.writerName }}
      </div>
      <div class="hr hr-6 dotted"></div>
    </div>
    <div>
      <div style="text-align: left; line-height: 30px; font-weight: bold;">
        特性描述:
      </div>
      <div style="margin-left: 15px; line-height: 21px;"
           v-rich-html="character.functionDescription">
      </div>
      <div v-if="pinBasis" style="margin-left: 15px; color: #888;">{{ pinBasis }}</div>
      <el-button v-if="character.id" type="text" size="mini" @click="lineageVisible = true">查看演进</el-button>
      <el-dialog title="版本演进" width="32cm" append-to-body :visible.sync="lineageVisible">
        <RichLineageGraph v-if="lineageVisible" url="/customer/character/rich-doc/get-lineage"
                          :params="{ characterId: character.id }"></RichLineageGraph>
      </el-dialog>
      <div class="hr hr-6 dotted"></div>
    </div>
    <div style="line-height: 30px;"  v-show="character.fileList">
      <div style="font-weight: bold;">
        定义附件:
      </div>
      <div v-for="(item, i ) in character.fileList" :key="item.id+'-'+i" style="display: flex; flex-direction: row; align-items: center; margin-left: 16px;">
        <div v-if="item">
          {{ item.name }}
        </div>
        <div v-if="item && item.authStatus">
          <el-button type="text" style="margin-left: 25px;" @click="downloadFile(item)">下载</el-button>
        </div>
      </div>
      <div class="hr hr-6 dotted"></div>
    </div>
    <div v-for="(item, index) in jobitemList">
      <JobitemOwnInfoPlain
          :key="item.id"
          :jobitem-id="item.id"
          :project-type="item.projectType"
          :index="index"
          :entrance="entrance"
      ></JobitemOwnInfoPlain>
    </div>


<!--    显示特性定义表对话框-->

    <!--    TODO 需要调试-->
    <!--显示任务信息-->
    <el-dialog
        :visible.sync="showJobitemDlg"
        width="29cm"
        :before-close="handleClose"
        :close-on-click-modal=false>
      <div slot="title" style="font-size: 20px;">{{ dialogTitle }}</div>
      <div v-if="showJobitemDlg">
        <JobitemDetailInfoDlg
            :key="character.id"
            :jobitem-id="dlgJobItem.id"
            :job-type="jobType"
            :project-type="projectType"
            :index="index"
            :entrance="entrance">
        </JobitemDetailInfoDlg>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import FileRdms from "@/components/rdms/file-rdms.vue";
import FileSaver from 'file-saver';
import JobitemDetailInfoDlg from "@/components/std/jobitem-detail-info-dlg.vue";
import JobitemOwnInfoPlain from "@/components/std/jobitem-own-info-plain.vue";
import RichLineageGraph from "@/components/rich/rich-lineage-graph.vue";

export default {
  components: {
    JobitemOwnInfoPlain,
    JobitemDetailInfoDlg,
    FileRdms,
    RichLineageGraph,
  },
  name: "character-review-show-plain",

  props: {
    characterReviewDetail: {
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

  data: function () {
    return {
      character: {},
      jobitemList: [],
      pinBasis: '',
      lineageVisible: false,
    }
  },

  computed: {
  },

  mounted: function () {
    let _this = this;
    const detail = _this.characterReviewDetail || {};
    _this.character = detail.characterDto || {};
    _this.jobitemList = detail.reviewJobitemList || [];
    _this.loadPinnedReview();
  },

  methods: {
    loadPinnedReview() {
      const c = this.character;
      if (!c || !c.id) return;
      const job = (this.jobitemList && this.jobitemList[0]) || null;
      const refId = (job && job.id) || c.jobitemId;
      if (!refId) return;
      const change = this.jobType === 'REVIEW_CHANGE' || (job && job.type === 'REVIEW_CHANGE');
      this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/character/rich-doc/get', {
        characterId: c.id,
        pinType: change ? 'CHANGE_REVIEW_SUBMITTED' : 'REVIEW_SUBMITTED',
        refId: refId
      }).then(response => {
        const resp = response.data;
        if (!resp.success || !resp.content) return;
        const view = resp.content;
        this.pinBasis = view.basisText || '';
        const sections = view.sections || [];
        const desc = sections.find(s => s.field === 'functionDescription' || s.code === 'FUNCTION_DESC');
        if (desc && desc.html) this.$set(this.character, 'functionDescription', desc.html);
      });
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

    showJobitemInfo(character){
      let _this = this;
      _this.dialogTitle = '任务信息';
      _this.dlgJobItem = character;
      _this.showJobitemDlg = true;

    },
    handleClose(done) {
      let _this = this;
      _this.showJobitemDlg = false;
      done(); //el-dialog用于关闭对话框的内置函数.
    },

    closeDialog() {
      let _this = this;
      _this.showJobitemDlg = false;
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

.simple-plain-container{
  height: 100%;
  padding-right: 10px;
  //min-width: 360px;
  overflow-y: scroll;
}
</style>
