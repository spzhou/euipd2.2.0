<!--
 * Author: 周朔鹏
 * Date: 2025-02-23
 * Email: 391902958@qq.com
 * Website: https://www.euipd.com
-->

<template>
  <div class="simple-plain-container" >
    <div>
      <div style="display: flex; flex-direction: row; line-height: 30px; font-weight: bold;">
        <div style="width: 90px; text-align: right;">
          子项目名称:
        </div>
        <div style="line-height: 30px; margin-left: 6px;">
          <a href="javascript:void(0)" @click="showJobitemInfo(subProject)">{{ subProject.label }}</a>
        </div>
        <div class="hr hr-6 dotted"></div>
      </div>
      <div style="display: flex; flex-direction: row; line-height: 30px; font-weight: bold;">
        <div style="width: 90px; text-align: right;">
          子项目ID:
        </div>
        <div style="line-height: 30px; margin-left: 6px;">
          {{ subProject.id }}
        </div>
        <div class="hr hr-6 dotted"></div>
      </div>
      <div style="display: flex; flex-direction: row; line-height: 30px; font-weight: bold;">
        <div style="width: 90px; text-align: right;">
          项目经理:
        </div>
        <div style="line-height: 30px; margin-left: 6px;">
          {{ subProject.projectManagerName }}
        </div>
      </div>
      <div style="display: flex; flex-direction: row; line-height: 30px; font-weight: bold;">
        <div style="width: 90px; text-align: right;">
          测试主管:
        </div>
        <div style="line-height: 30px; margin-left: 6px;">
          {{ subProject.testManagerName }}
        </div>
      </div>
      <div style="display: flex; flex-direction: row; line-height: 30px; font-weight: bold;">
        <div style="width: 90px; text-align: right;">
          项目预算:
        </div>
        <div style="line-height: 30px; margin-left: 6px;">
          {{ subProject.budget }}
        </div>
      </div>
      <div class="hr hr-6 dotted"></div>
      <div>
        <div style="text-align: left; line-height: 30px; font-weight: bold;">
          项目简介:
        </div>
        <div style="margin-left: 15px; line-height: 21px;"
             v-rich-html="subProject.introduce">
        </div>
        <div class="hr hr-6 dotted"></div>
      </div>
    </div>

    <!--    TODO 需要调试-->
    <div v-for="(item, index) in jobitemList">
      <JobitemOwnInfoPlain
          :key="item.id"
          :jobitem-id="item.id"
          :project-type="item.projectType"
          :index="index"
          :entrance="entrance"
      ></JobitemOwnInfoPlain>

    </div>

    <!--    TODO 需要调试-->
    <div v-for="(item, index) in characterReviewDetailList">
      <CharacterReviewShowPlain
          :key="item.id"
          :character-review-detail="item"
          :job-type="item.type"
          :project-type="item.projectType"
          :index="index"
          :entrance="entrance"
      ></CharacterReviewShowPlain>
    </div>

    <!--    TODO 需要调试-->
<!--    迭代调用自己-->
    <div v-for="(item, index) in subProjectReviewDetailList">
      <SubProjectReviewShowPlain
          :key="item.id"
          :subproject-review-detail="item"
          :job-type="item.type"
          :project-type="item.projectType"
          :index="index"
          :entrance="entrance"
      ></SubProjectReviewShowPlain>
    </div>

    <!--    TODO 需要调试-->
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
            :key="subProject.id"
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
import CharacterReviewShowPlain from "@/components/std/character-review-show-plain.vue";
import JobitemOwnInfoPlain from "@/components/std/jobitem-own-info-plain.vue";

export default {
  components: {
    JobitemOwnInfoPlain,
    CharacterReviewShowPlain,
    JobitemDetailInfoDlg,
    FileRdms,
  },
  name: "subProject-review-show-plain",

  props: {
    subprojectReviewDetail: {
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
      subProject: {},
      jobitemList: [],
      characterReviewDetailList: [],
      subProjectReviewDetailList: [],
    }
  },

  computed: {
  },

  mounted: function () {
    let _this = this;
    _this.subProject = subprojectReviewDetail.subProjectDto;
    _this.jobitemList = subprojectReviewDetail.reviewJobitemList;
    _this.characterReviewDetailList = subprojectReviewDetail.characterReviewDetailList;
    _this.subProjectReviewDetailList = subprojectReviewDetail.subProjectReviewDetailList;
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

    showJobitemInfo(subProject){
      let _this = this;
      _this.dialogTitle = '任务信息';
      _this.dlgJobItem = subProject;
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
