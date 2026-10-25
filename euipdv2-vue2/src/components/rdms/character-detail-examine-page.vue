<!--
 * Author: 周朔鹏
 * Date: 2025-02-23
 * Email: 391902958@qq.com
 * Website: https://www.euipd.com
-->

<template>
  <div style="height: 100%;">
    <div class="paper-height">
      <div style="height: 100%; overflow: scroll; ">
        <el-row>
          <el-col>
            <div class="a4-paper-big">
              <div id="examine-print">
                <el-row>
                  <el-col :span="24" style="width: 100%; text-align: center; font-size: 28px;margin-top: 25px;">
                    {{ characterItem.characterName }}
                  </el-col>
                </el-row>
                <div>
                  <CharacterDetailInfoCore
                      :id="('character-main-show-' + (random))"
                      :key="characterItem.id"
                      :character-id="characterItem.id"
                      :pin-type="characterItem.jobitemType === 'REVIEW_CHANGE' ? 'CHANGE_REVIEW_SUBMITTED' : 'REVIEW_SUBMITTED'"
                      :ref-id="characterItem.jobitemId"
                  ></CharacterDetailInfoCore>
                </div>

                <!-- 显示特性定义审批过程提供的附件-->
                <div v-if="processAttachments!==null  && processAttachments !== undefined && processAttachments.length >0">
                  <div class="hr hr-18 dotted hr-double"></div>
                  <el-row>
                    <el-col :span="22">
                      <div style="display: flex; align-items: end; flex-direction: column; margin-bottom: 15px;">
                        <div v-for="(item, i) in processAttachments" :key="i" style="display: flex; flex-direction: row; align-items: center; margin: 5px 0 0 50px;">
                          <div v-if="item">
                            {{ ((itme.stage) + ' : ' + (item.name))}}
                          </div>
                          <div v-if="item && item.authStatus">
                            <el-button type="text" style="margin-left: 30px;" @click="downloadFile(item)">下载</el-button>
                          </div>
                        </div>
                      </div>
                    </el-col>
                  </el-row>
                </div>

                <!--预算使用情况-->
                <div v-if="projectType === PROJECT_TYPE.DEV_PROJECT.key">
                  <div class="hr hr-18 dotted hr-double"></div>
                  <div style="margin-left: 50px;">工时分配情况:</div>
                  <div style="margin: 12px 70px 0;">
                    <div style="display: flex; flex-direction: row; justify-content: space-around; margin-bottom: 5px;">
                      <div>
                        <span style="width: 82px; text-align: right; margin-right:5px;">开发工时:</span>
                        {{ characterItem.devManhourApproved !== null ? characterItem.devManhourApproved : 0 }} 小时
                      </div>
                      <div>
                        <span  style="width: 82px; text-align: right; margin-right:5px;">测试工时:</span>
                        {{ characterItem.testManhourApproved !== null ? characterItem.testManhourApproved : 0 }} 小时
                      </div>
                      <div>
                        <span  style="width: 82px; text-align: right; margin-right:5px;">物料费:</span>
                        {{ characterItem.materialFeeApproved !== null ? characterItem.materialFeeApproved : 0 }} 元
                      </div>
                      <div>
                        <span  style="width: 82px; text-align: right; margin-right:5px;">管理费:</span>
                        {{ characterItem.managementFee }} 元
                      </div>
                    </div>
                    <div style="display: flex; flex-direction: row; justify-content: space-around; margin-bottom: 5px;">
                      <div>
                        <span  style="width: 100px; text-align: right; margin-right:5px;">第三方测试费:</span>
                        {{ characterItem.testFee }} 元
                      </div>
                      <div>
                        <span  style="width: 82px; text-align: right; margin-right:5px;">动力费:</span>
                        {{ characterItem.powerFee }} 元
                      </div>
                      <div>
                        <span  style="width: 82px; text-align: right; margin-right:5px;">会议费:</span>
                        {{ characterItem.conferenceFee }} 元
                      </div>
                      <div>
                        <span  style="width: 82px; text-align: right; margin-right:5px;">差旅费:</span>
                        {{ characterItem.businessFee }} 元
                      </div>
                    </div>
                    <div style="display: flex; flex-direction: row; justify-content: space-around; margin-bottom: 5px;">
                      <div>
                        <span  style="width: 82px; text-align: right; margin-right:5px;">合作费:</span>
                        {{ characterItem.cooperationFee }} 元
                      </div>
                      <div>
                        <span  style="width: 120px; text-align: right; margin-right:5px;">知识产权相关费用:</span>
                        {{ characterItem.propertyFee }} 元
                      </div>
                      <div>
                        <span  style="width: 82px; text-align: right; margin-right:5px;">劳务费:</span>
                        {{ characterItem.laborFee }} 元
                      </div>
                      <div>
                        <span  style="width: 82px; text-align: right; margin-right:5px;">专家费:</span>
                        {{ characterItem.consultingFee }} 元
                      </div>
                    </div>
                  </div>
                </div>

                <div v-if="characterProcessList !== null && characterProcessList !== undefined && characterProcessList.length > 0">
                  <div class="hr hr-18 dotted hr-double"></div>
                  <el-row>
                    <div style="width: var(--table-out-width, 100%); max-width: 22cm; margin: 0 auto;">
                      <div>
                        <el-table
                            ref="characterProcessTable"
                            :data="characterProcessList"
                            border
                            stripe
                            style="width: 100%"
                            highlight-current-row>
                          <el-table-column label="序号" align="center" width="50">
                            <template slot-scope="scope">
                              {{ scope.$index + 1 }}
                            </template>
                          </el-table-column>
                          <el-table-column
                              prop="processStatusName"
                              label="执行过程"
                              align="center"
                              width="70">
                          </el-table-column>
                          <el-table-column
                              prop="jobDescription"
                              label="审批描述">
                          </el-table-column>
                          <el-table-column
                              prop="executorName"
                              label="提交人"
                              align="center"
                              width="70">
                          </el-table-column>
                          <el-table-column
                              prop="nextNodeName"
                              label="下一节点"
                              align="center"
                              width="70">
                          </el-table-column>
                          <el-table-column
                              prop="createTimeStr"
                              label="签审时间"
                              align="center"
                              width="150">
                          </el-table-column>
                        </el-table>
                      </div>
                    </div>
                  </el-row>
                </div>
              </div>
            </div>
          </el-col>
        </el-row>
      </div>
    </div>
    <div class="hr hr-9 dotted hr-double"></div>
    <div v-if="entrance === ENTRANCE_CLASS.FUNCTION.key || entrance === ENTRANCE_CLASS.SYSTEM.key
              || entrance === ENTRANCE_CLASS.CHARACTER.key">
      <el-row>
        <el-col>
          <div style="display: flex; justify-content: center;">
            <!--审批服务只认特性下一节点、产品经理与最终管理人；系统工程师作为工单下一节点给出审核意见-->
            <template v-if="!isDevRevision && character.status === CHARACTER_STATUS.SUBMIT.key">
              <el-button v-if="canApproveDefinition" type="primary" @click="approveDlg">
                特性定义审批
              </el-button>
              <el-button v-else type="info" disabled>
                待产品经理审批
              </el-button>
              <el-button v-if="!canApproveDefinition && character.status === CHARACTER_STATUS.SUBMIT.key"
                         type="primary" @click="reviewOpinion">
                审核意见
              </el-button>
            </template>
            <!--开发域修订：不经特性审批，由派发人确认生成新的特性版本；纳入项目另经变更评审（实现方案第 8.5.7 节）-->
            <template v-if="isDevRevision && revisionBranch">
              <el-button v-if="revisionBranch.status === 'OPEN' && characterItem.status === CHARACTER_STATUS.SUBMIT.key"
                         type="primary" @click="confirmRevision">
                确认修订
              </el-button>
              <el-button v-if="revisionBranch.status === 'OPEN' && characterItem.status === CHARACTER_STATUS.SUBMIT.key"
                         type="primary" @click="returnRevision">
                驳回修订
              </el-button>
              <el-button v-if="revisionBranch.status === 'REVIEWING'" type="info" disabled>
                变更评审中
              </el-button>
              <el-button v-if="revisionBranch.status === 'APPROVED'" type="primary" @click="mergeDlg">
                合入确认
              </el-button>
            </template>
            <!--历史迭代建议（SUGGEST_UPDATE）记录只读，不提供修订与分解-->
            <el-button v-if="projectType === PROJECT_TYPE.PRE_PROJECT.key && characterItem.jobitemType !== JOB_ITEM_TYPE.SUGGEST_UPDATE.key
                          && character.status === CHARACTER_STATUS.APPROVED.key"
                       type="primary" @click="iterationDlg">
              功能修订
            </el-button>
            <el-button v-if="projectType === PROJECT_TYPE.PRE_PROJECT.key
                          && character.status !== CHARACTER_STATUS.REFUSE.key
                          && characterItem.jobitemType !== JOB_ITEM_TYPE.SUGGEST_UPDATE.key"
                       type="primary" @click="decomposeDlg">
              功能分解
            </el-button>
            <el-button v-if="(projectType === PROJECT_TYPE.DEV_PROJECT.key || !isHandling) && !isDevRevision" type="primary"
                       @click="rejectDlg">
              任务驳回
            </el-button>
            <el-button v-if="projectType === PROJECT_TYPE.PRE_PROJECT.key && characterItem.jobitemType === JOB_ITEM_TYPE.DEMAND.key" type="primary"
                       @click="closeCharacter">
              废弃特性
            </el-button>
          </div>
        </el-col>
      </el-row>
    </div>

    <!-- 功能审批 操作-->
    <el-dialog
        title="特性定义审批"
        width="29cm"
        :visible.sync="jobItemDlgVisible_approve"
        :close-on-click-modal=false>
      <div>
        <character-detail-approve-dlg
            :key="characterItem.id"
            :character-id="characterItem.id"
            :job-type="characterItem.jobitemType"
            :project-type="projectType"
            :index="index"
            :entrance="entrance"
            :close-dlg = closeCharacterApproveDlg>
        </character-detail-approve-dlg>
      </div>
    </el-dialog>

    <!--合入确认（修订确认上线前已通过变更评审的修订分支）-->
    <el-dialog
        title="修改分支合入确认"
        width="29cm"
        :visible.sync="jobItemDlgVisible_merge"
        :close-on-click-modal=false>
      <div v-if="jobItemDlgVisible_merge && revisionBranch">
        <RichMergeConfirmDlg
            :key="revisionBranch.id + 'merge'"
            :branch-id="revisionBranch.id"
            :close-dlg="closeMergeDlg"
            @merged="afterMerged">
        </RichMergeConfirmDlg>
      </div>
    </el-dialog>

    <!--功能修订-->
    <el-dialog
        title="创建功能修订任务"
        width="29cm"
        :visible.sync="jobItemDlgVisible_iteration"
        :close-on-click-modal=false>
      <div>
        <JobitemCreateByCharacterIdDlg
            :key="characterItem.id + 'iteration'"
            :character-id="characterItem.id"
            :project-type="projectType"
            :job-type="JOB_ITEM_TYPE.ITERATION.key"
            :index="index"
            :entrance="entrance"
            :close-dlg = closeCharacterIterationDlg>
        </JobitemCreateByCharacterIdDlg>
      </div>
    </el-dialog>

    <!--功能分解-->
    <el-dialog
        title="创建功能分解任务"
        width="29cm"
        :visible.sync="jobItemDlgVisible_decompose"
        :close-on-click-modal=false>
      <div>
        <JobitemCreateByCharacterIdDlg
            :key="characterItem.id + 'decompose'"
            :character-id="characterItem.id"
            :project-type="projectType"
            :job-type="JOB_ITEM_TYPE.DECOMPOSE.key"
            :index="index"
            :entrance="entrance"
            :close-dlg = closeCharacterDecomposeDlg>
        </JobitemCreateByCharacterIdDlg>
      </div>
    </el-dialog>

    <!--任务驳回-->
    <el-dialog
        title="任务驳回"
        width="29cm"
        :visible.sync="jobItemDlgVisible_reject"
        :close-on-click-modal=false>
      <div>
        <JobitemRejectDlg
            :key="characterItem.jobitemId"
            :jobitem-id="characterItem.jobitemId"
            :project-type="projectType"
            :index="index"
            :entrance="entrance"
            :close-dlg = closeRejectDlg>
        </JobitemRejectDlg>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import FileRdms from "@/components/rdms/file-rdms";
import FileSaver from 'file-saver';
import Swal from "sweetalert2";
import printJS from "print-js";
import CharacterDetailInfoCore from "@/components/std/character-detail-info-core.vue";
import CharacterDetailApproveDlg from "@/components/rdms/character-detail-approve-dlg";
import JobitemRejectDlg from "@/components/std/jobitem-reject-dlg.vue";
import JobitemCreateByCharacterIdDlg from "@/components/std/jobitem-create-by-character-id-dlg.vue";
import RichMergeConfirmDlg from "@/components/rich/rich-merge-confirm-dlg.vue";

export default {
  components: {
    JobitemCreateByCharacterIdDlg,
    FileRdms,
    FileSaver,
    CharacterDetailInfoCore,
    CharacterDetailApproveDlg,
    JobitemRejectDlg,
    RichMergeConfirmDlg,
  },
  name: "character-detail-examine-page",

  props: {
    character: {
      default: null,
    },
    docType: {
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
    }
  },

  data() {
    return {
      loginUser: {},
      loginCustomer: {},

      isHandling: false,  //标记是否处处理过程中

      demand: {},
      jobItemDlgVisible_iteration: false,
      jobItemDlgVisible_decompose: false,
      jobItemDlgVisible_approve: false,
      jobItemDlgVisible_reject: false,
      jobItemDlgVisible_merge: false,
      revisionBranch: null,

      characterItem: {},
      resFileIds: [],

      characterProcess: {},
      processAttachments: [],  //过程流转中产生的附件
      characterBudget: {},

      PROJECT_TYPE: PROJECT_TYPE,
      DOC_TYPE: DOC_TYPE,
      JOB_ITEM_TYPE: JOB_ITEM_TYPE,
      CHARACTER_STATUS: CHARACTER_STATUS,
      ENTRANCE_CLASS: ENTRANCE_CLASS,

      characterProcessList: [],

      random: 0,

      printData: {
        printable: 'examine-print',
        type: 'html',
        // header: this.characterItem.preProjectName,
        // headerStyle: 'font-weight: 600; font-size: 30px;',
        ignore: ['no-print'],
        targetStyles: ['*'],  //css样式，写成*代表打印样式完全继承你页面的样式
        // maxWidth:'800px'  //打印界面最大宽度，适当调整可以解决打印页面过宽，显示不完整的问题
      },
    };
  },

  computed: {
    canApproveDefinition () {
      const uid = this.loginUser && this.loginUser.customerUserId;
      const c = this.characterItem || {};
      if (uid && (c.nextNode === uid || c.productManagerId === uid)) {
        return true;
      }
      return typeof RoleAccess !== 'undefined'
        && RoleAccess.hasAny(['UltiMng'], (Tool.getLoginUser() || {}).roleList);
    },
    isDevRevision () {
      return this.projectType === PROJECT_TYPE.DEV_PROJECT.key
        && this.characterItem.jobitemType === JOB_ITEM_TYPE.ITERATION.key
    },
  },

  mounted: function () {
    let _this = this;
    _this.random = Math.random();
    _this.getLoginUser();

    if(! Tool.isEmpty(_this.character)  && (_this.docType === DOC_TYPE.CHARACTER.key)){
      _this.characterItem = _this.character;
      _this.listProcessByCharacterId(_this.characterItem.id);
      _this.getCharactersHandlingStatusOfJobitem(_this.characterItem.id);
      if (_this.isDevRevision) {
        _this.loadRevisionBranch();
      }
    }
  },

  beforeDestroy() {
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

    listProcessByCharacterId(id) {
      let _this = this;
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/character-process/list-by-characterId/' + id).then((response) => {
        let resp = response.data;
        if (resp.success) {
          _this.characterProcessList = resp.content;
          _this.processAttachments = _this.characterProcessList.processAttachmentDtos;
        } else {
          Toast.warning(resp.message, Swal);
        }
      })
    },

    reviewOpinion() {
      let _this = this;
      Swal.fire({
        title: '审核意见',
        text: '《' + _this.characterItem.characterName + '》的审核意见将记入特性过程记录，供产品经理审批参考',
        input: 'textarea',
        inputPlaceholder: '审核意见',
        showCancelButton: true,
        confirmButtonText: '提交',
        cancelButtonText: '取消',
        inputValidator: (value) => (value && value.trim()) ? undefined : '请填写审核意见',
      }).then((result) => {
        if (!result.isConfirmed) {
          return;
        }
        let formData = new window.FormData();
        formData.append('opinion', result.value.trim());
        _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/character/review-opinion/' + _this.characterItem.id, formData).then((response) => {
          let resp = response.data;
          if (resp.success) {
            Toast.success(resp.message || '已记录', Swal);
            _this.listProcessByCharacterId(_this.characterItem.id);
            _this.$bus.$emit('refresh-character-joblist-examine', true);
          } else {
            Toast.warning(resp.message, Swal);
          }
        });
      });
    },

    getCharactersHandlingStatusOfJobitem(characterId){
      let _this = this;
      let formData = new window.FormData();
      formData.append('characterId', characterId);
      formData.append('loginUserId', _this.loginUser.customerUserId);
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/job-items/getCharactersHandlingStatusOfJobitem', formData).then((response) => {
        let resp = response.data;
        if (resp.success) {
          _this.isHandling = resp.content;
        } else {
          Toast.warning(resp.message, Swal);
        }
      })
    },

    loadRevisionBranch() {
      let _this = this;
      if (Tool.isEmpty(_this.characterItem.parent)) {
        return;
      }
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/character/rich-doc/get-branches', {
        characterId: _this.characterItem.parent,
      }).then((response) => {
        let resp = response.data;
        if (resp.success) {
          _this.revisionBranch = (resp.content || []).find(b => b.editRecordId === _this.characterItem.id) || null;
        } else {
          Toast.warning(resp.message, Swal);
        }
      })
    },

    confirmRevision() {
      let _this = this;
      let no = _this.revisionBranch.featureLabel || '';
      Swal.fire({
        title: '确认修订',
        text: '确认后特性版本更新为 ' + no + '，项目锁定版本保持不变；需要时由项目经理发起纳入项目。',
        input: 'textarea',
        inputPlaceholder: '确认说明（选填）',
        showCancelButton: true,
        confirmButtonColor: '#409EFF',
        cancelButtonColor: '#909399',
        confirmButtonText: '确认',
        cancelButtonText: '取消'
      }).then((result) => {
        if (!result.isConfirmed) {
          return;
        }
        _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/character/rich-doc/revision/approve-confirm', {
          characterId: _this.characterItem.id,
          confirmNote: result.value,
        }).then((response) => {
          let resp = response.data;
          if (resp.success) {
            Toast.success('修订已确认', Swal);
            _this.afterRevisionHandled();
          } else {
            Toast.warning(resp.message, Swal);
          }
        })
      }).catch(_ => {
      });
    },

    returnRevision() {
      let _this = this;
      Swal.fire({
        title: '驳回修订',
        input: 'textarea',
        inputPlaceholder: '驳回原因',
        showCancelButton: true,
        confirmButtonColor: '#409EFF',
        cancelButtonColor: '#909399',
        confirmButtonText: '驳回',
        cancelButtonText: '取消',
        inputValidator: (value) => (!value || !value.trim()) ? '请填写驳回原因' : null
      }).then((result) => {
        if (!result.isConfirmed) {
          return;
        }
        _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/character/rich-doc/revision/approve-return', {
          characterId: _this.characterItem.id,
          reason: result.value,
        }).then((response) => {
          let resp = response.data;
          if (resp.success) {
            Toast.success('已驳回执行人', Swal);
            _this.afterRevisionHandled();
          } else {
            Toast.warning(resp.message, Swal);
          }
        })
      }).catch(_ => {
      });
    },

    afterRevisionHandled() {
      let _this = this;
      _this.$bus.$emit('refresh-character-joblist-tab-num', true);
      _this.$bus.$emit('refresh-character-joblist-examine', true);
      _this.$bus.$emit('refresh-system-examine-container-subproject', true);
      _this.$bus.publish({ areas: ['project', 'jobitem', 'badge'], reason: 'rich-revision' });
    },

    mergeDlg() {
      this.jobItemDlgVisible_merge = true;
    },

    closeMergeDlg() {
      this.jobItemDlgVisible_merge = false;
    },

    afterMerged() {
      this.$bus.$emit('refresh-system-examine-container-subproject', true);
      this.$bus.publish({ areas: ['project', 'jobitem', 'badge'], reason: 'rich-merge' });
    },

    iterationDlg() {
      let _this = this;
      _this.jobItemDlgVisible_iteration = true;
    },

    decomposeDlg() {
      let _this = this;
      _this.jobItemDlgVisible_decompose = true;
    },

    approveDlg() {
      let _this = this;
      _this.jobItemDlgVisible_approve = true;
    },

    rejectDlg() {
      let _this = this;
      _this.jobItemDlgVisible_reject = true;
    },

    closeCharacter() {
      let _this = this;
      Swal.fire({
        title: '确认',
        text: "是否确认废弃此特性? 废弃后不可恢复!",
        icon: 'question',
        showCancelButton: true,
        confirmButtonColor: '#409EFF',
        cancelButtonColor: '#909399',
        confirmButtonText: '确认!',
        cancelButtonText: '取消!'
      }).then((result) => {
        if (result.isConfirmed) {
          _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/character/close/' + _this.characterItem.id).then((response)=>{
            let resp = response.data;
            if (resp.success) {
              Toast.success("保存成功", Swal);
              _this.$bus.$emit('refresh-character-joblist-tab-num', true);
              _this.$bus.$emit('refresh-character-joblist-examine', true);
              _this.$bus.$emit('refresh-character-joblist-evaluate', true);

              _this.$bus.$emit('notify-character-submit-job-flag', true);
              _this.$bus.$emit('notify-character-evaluate-job-flag', true);
            } else {
              Toast.warning(resp.message, Swal);
            }
          })
        }
      }).catch(_ => {
      });
    },

    handlePrint(params) {
      printJS({
        printable: params.printable, // 'printFrom', // 标签元素id
        type: params.type || 'html',
        header: params.header, // '表单',
        targetStyles: ['*'],
        style: '@page {margin:20 10mm};', // 可选-打印时去掉眉页眉尾
        ignoreElements: params.ignore || [], // ['no-print']
        properties: params.properties || null
      })
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
      });
    },

    closeCharacterIterationDlg(){
      let _this = this;
      _this.jobItemDlgVisible_iteration = false;
    },

    closeCharacterDecomposeDlg(){
      let _this = this;
      _this.jobItemDlgVisible_decompose = false;
    },

    closeCharacterApproveDlg(){
      let _this = this;
      _this.jobItemDlgVisible_approve = false;
    },

    closeRejectDlg(){
      let _this = this;
      _this.jobItemDlgVisible_reject = false;
    },

  },
}
</script>

<style lang="scss" scoped>
@import './../../../public/static/scss/caipd';

.paper-height {
  height: calc(100vh - 325px);
}

.item-container {
  //margin-top: 10px;
  width: 100%;
  height: 100%;
  display: -webkit-box; /* 老版本语法: Safari, iOS, Android browser, older WebKit browsers. */
  display: -moz-box; /* 老版本语法: Firefox (buggy) */
  display: -ms-flexbox; /* 混合版本语法: IE 10 */
  display: -webkit-flex; /* 新版本语法: Chrome 21+ */
  display: flex; /* 新版本语法: Opera 12.1, Firefox 22+ */
  .item-left {
    width: 120px;
    text-align: right;
    //margin-bottom: 20px;
    //background-color: #00B83F;
  }

  .item-right {
    margin-left: 10px;
    background-color: white !important;
    -webkit-flex: 1; /* Chrome */
    -ms-flex: 1; /* IE 10 */
    flex: 1; /* NEW, Spec - Opera 12.1, Firefox 20+ */
    -webkit-box-flex: 1; /* OLD - iOS 6-, Safari 3.1-6 */
    -moz-box-flex: 1; /* OLD - Firefox 19- */
    background-color: rgba(71, 148, 239, 0.08);
  }
}

</style>
