<!--
 * Author: 周朔鹏
 * Date: 2025-02-26
 * Email: 391902958@qq.com
 * Website: https://www.euipd.com
-->

<template>
  <div class="dialog-container">
    <el-tabs v-model="detailTab">
      <el-tab-pane label="办理" name="work">
    <div style="margin: 0 100px 15px;">
      注意 : 本表单是逐步完成的, 您完成相关工作后, 即可操作相应部分. 系统会记录相关操作和文件. 等到全部工作完成后, 再提交项目经理. 值得注意的是, 在相应的工作评审之前, 必须提交, 以便生成评审表格, 进行费用等统计.
    </div>
    <div class="ca-operation-box">
      <div class="ca-operation-box-border">
        <div class="ca-operation-title">
          Step 1 : 《费用申请审批单》
        </div>
        <div class="ca-operation-content">
          <div>
            批复的费用申请审批单是费用支付的授权文件. 如果根据公司规定, 还需要其他主管领导签字, 您可以打印后提交签署..
          </div>
          <div style="display: flex; flex-direction: row; justify-content: end; margin-right: 20px; margin-top: 20px;">
            <el-button v-if="appItem.status !== APPLICATION_STATUS.PRE_COMPLETE.key && appItem.status !== APPLICATION_STATUS.COMPLETE.key" type="primary" @click="feeApprove(appItem)">打印《费用申请审批单》</el-button>
            <el-button v-if="appItem.status === APPLICATION_STATUS.PRE_COMPLETE.key || appItem.status === APPLICATION_STATUS.COMPLETE.key" type="primary" disabled>打印《费用申请审批单》</el-button>
          </div>
        </div>
      </div>
      <div class="ca-operation-box-border">
        <div class="ca-operation-title">
          Step 2 : 上传《费用申请审批单》扫描件
        </div>
        <div class="ca-operation-content">
          <div>
            经过其他领导签字的文件, 请扫描或拍照后上传到系统中, 作为费用支付的凭据.
          </div>
          <div style="display: flex; flex-direction: row; justify-content: end; margin-top: 20px; margin-right: 20px;">
            <div style="width: 700px;">
              <FileUploadCore
                  :input-id="('fee-deal-with-dlg-step2-' + (appItem.id))"
                  :enable="appItem.status !== APPLICATION_STATUS.PRE_COMPLETE.key && appItem.status !== APPLICATION_STATUS.COMPLETE.key"
                  :key="('fee-file-upload-step2-' + (appItem.id))"
                  :x-project-id="appItem.subprojectId"
                  :item-id="appItem.id"
                  :file-group="FILE_GROUPING.FEE_APP.key"
                  :project-type="PROJECT_TYPE.SUB_PROJECT.key"
                  :en-delete="appItem.status === APPLICATION_STATUS.APPROVED.key"
                  :read-db-record="true"
                  v-on:uploadFileList="getUploadFileList_app">
              </FileUploadCore>
            </div>
          </div>
        </div>
      </div>
      <div class="ca-operation-box-border">
        <div class="ca-operation-title">
          Step 3 : 费用支付证明
        </div>
        <div class="ca-operation-content">
          <div>
            请上传支付证明和相关票据扫描件或照片. 经过项目经理审批确认后, 正式计入项目成本.
          </div>
          <div style="display: flex; flex-direction: row; justify-content: end; margin-top: 20px; margin-right: 20px;">
            <div style="width: 700px;">
              <FileUploadCore
                  :input-id="('fee-deal-with-dlg-step3-' + (appItem.id))"
                  :enable="appItem.status !== APPLICATION_STATUS.PRE_COMPLETE.key && appItem.status !== APPLICATION_STATUS.COMPLETE.key"
                  :key="('fee-file-upload-step3-' + (appItem.id))"
                  :x-project-id="appItem.subprojectId"
                  :item-id="appItem.id"
                  :file-group="FILE_GROUPING.FEE_DEAL.key"
                  :project-type="PROJECT_TYPE.SUB_PROJECT.key"
                  :en-delete="appItem.status === APPLICATION_STATUS.SUPPLEMENT.key || APPLICATION_STATUS.APPROVED.key"
                  :read-db-record="true"
                  v-on:uploadFileList="getUploadFileList_deal">
              </FileUploadCore>
            </div>
          </div>
        </div>
      </div>
      <div class="ca-operation-box-border">
        <div class="ca-operation-title">
          Step 4 : 提交产品/项目经理复核
        </div>
        <div class="ca-operation-content">
          <div>
            提交后, 产品经理/项目经理将对所有证明文件进行复核. 产品经理/项目经理也可能退回要求增补相关证明文件.
          </div>
          <div v-if="appItem.status === APPLICATION_STATUS.SUPPLEMENT.key" style="margin: 8px 0 0 10px; color: #E6A23C;">
            复核驳回! 请做好沟通, 重新整理材料并提交.
          </div>
          <div style="display: flex; flex-direction: row; justify-content: end; margin-right: 20px; margin-top: 20px;">
            <div v-if="appItem.jobType !== JOB_ITEM_TYPE.QUALITY.key
            && appItem.jobType !== JOB_ITEM_TYPE.TASK_FUNCTION.key
            && (appItem.jobType !== JOB_ITEM_TYPE.TASK_BUG.key && appItem.auxType !== JOB_ITEM_TYPE.TASK_BUG.key )
            && appItem.auxStatus !== DOC_TYPE.PROJECT.key
            && appItem.auxStatus !== DOC_TYPE.PRE_PROJECT.key
            ">
              <el-button v-if="appItem.status !== APPLICATION_STATUS.PRE_COMPLETE.key && appItem.status !== APPLICATION_STATUS.COMPLETE.key"
                         type="primary" @click="dealWithSubmit(appItem)">提交项目经理审批</el-button>
              <el-button v-if="appItem.status === APPLICATION_STATUS.PRE_COMPLETE.key || appItem.status === APPLICATION_STATUS.COMPLETE.key"
                         type="primary" disabled>提交项目经理审批</el-button>
            </div>
            <div v-if="appItem.jobType === JOB_ITEM_TYPE.QUALITY.key || appItem.auxStatus === DOC_TYPE.PROJECT.key">
              <el-button v-if="appItem.status !== APPLICATION_STATUS.PRE_COMPLETE.key && appItem.status !== APPLICATION_STATUS.COMPLETE.key"
                         type="primary" @click="dealWithSubmit(appItem)">提交产品经理审批</el-button>
              <el-button v-if="appItem.status === APPLICATION_STATUS.PRE_COMPLETE.key || appItem.status === APPLICATION_STATUS.COMPLETE.key"
                         type="primary" disabled>提交产品经理审批</el-button>
            </div>
            <div v-if="/*appItem.jobType === JOB_ITEM_TYPE.TASK_BUG.key || */appItem.auxStatus === DOC_TYPE.SUB_PROJECT.key">
              <el-button v-if="appItem.status !== APPLICATION_STATUS.PRE_COMPLETE.key && appItem.status !== APPLICATION_STATUS.COMPLETE.key"
                         type="primary" @click="dealWithSubmit(appItem)">提交测试主管审批</el-button>
              <el-button v-if="appItem.status === APPLICATION_STATUS.PRE_COMPLETE.key || appItem.status === APPLICATION_STATUS.COMPLETE.key"
                         type="primary" disabled>提交测试主管审批</el-button>
            </div>
            <div v-if="appItem.jobType === JOB_ITEM_TYPE.TASK_FUNCTION.key || appItem.auxStatus === DOC_TYPE.PRE_PROJECT.key">
              <el-button v-if="appItem.status !== APPLICATION_STATUS.PRE_COMPLETE.key && appItem.status !== APPLICATION_STATUS.COMPLETE.key"
                         type="primary" @click="dealWithSubmit(appItem)">提交系统工程总监审批</el-button>
              <el-button v-if="appItem.status === APPLICATION_STATUS.PRE_COMPLETE.key || appItem.status === APPLICATION_STATUS.COMPLETE.key"
                         type="primary" disabled>提交系统工程总监审批</el-button>

            </div>
          </div>
        </div>
      </div>
      <div class="ca-operation-box-border">
        <div class="ca-operation-title">
          Step 5 : 打印《费用处理审批单》
        </div>
        <div class="ca-operation-content">
          <div>
            经过批复后, 请打印费用处理审批单, 与相关原始票据一起, 根据公司相关财务规定进行报销或核销.
          </div>
          <div style="display: flex; flex-direction: row; justify-content: end; margin-right: 20px; margin-top: 20px;">
            <!--审批完成后可以打印-->
            <div v-if="appItem.status === APPLICATION_STATUS.PRE_COMPLETE.key">
              <el-button type="primary"  @click="feeDealWithComplete(appItem)">打印《费用处理审批单》</el-button>
            </div>
            <div v-else>
              <el-button v-if="appItem.status !== APPLICATION_STATUS.PRE_COMPLETE.key" type="info" plain disabled>打印《费用处理审批单》</el-button>
            </div>
          </div>
        </div>
      </div>
      <div class="ca-operation-box-border">
        <div class="ca-operation-title">
          Step 6 : 归档
        </div>
        <div class="ca-operation-content">
          <div>
            完成所有相关工作后, 请提交归档. 归档后, 您的费用管理页面将不再出现此单信息.
          </div>
          <div style="display: flex; flex-direction: row; justify-content: end; margin-right: 20px; margin-top: 20px;">
            <!--审批完成后可以打印-->
            <div v-if="appItem.status === APPLICATION_STATUS.PRE_COMPLETE.key">
              <el-button type="primary"  @click="complete(appItem.code)">归档</el-button>
            </div>
            <div v-else>
              <el-button v-if="appItem.status !== APPLICATION_STATUS.PRE_COMPLETE.key" type="info" plain disabled>归档</el-button>
            </div>
          </div>
        </div>
      </div>

    </div>
    <div class="clearfix form-actions">
      <div style="display: flex; justify-content: center;">
        <button type="button" v-on:click="close()" class="ca-btn-primary">关闭</button>
      </div>
    </div>
      </el-tab-pane>
      <el-tab-pane label="流程图" name="diagram">
        <FlowViewer
            v-if="detailTab === 'diagram' && appItem && appItem.code"
            fixed-biz-code="FEE_APPLY"
            :fixed-biz-id="appItem.code">
        </FlowViewer>
      </el-tab-pane>
    </el-tabs>

    <!--打印费用申请单-->
    <el-dialog
        width="25.0cm"
        :visible.sync="materialApproveDlg"
        :before-close="handleClose"
        append-to-body
        :close-on-click-modal=false>
      <div>
        <FeeApproveInfoDlg
            :key="appItemCode"
            :application-code="appItemCode"
            :close-dlg = closeDialog
        ></FeeApproveInfoDlg>
      </div>
    </el-dialog>

    <!--打印费用处理审批单-->
    <el-dialog
        width="25.0cm"
        :visible.sync="feeDealWithCompleteDlg"
        :before-close="handleClose"
        append-to-body
        :close-on-click-modal=false>
      <div>
        <FeeDealWithApproveDlg
            :key="appItemCode"
            :application-code="appItemCode"
            :close-dlg = closeDialog
        ></FeeDealWithApproveDlg>
      </div>
    </el-dialog>

    <!--费用处理 提交-->
    <el-dialog
        width="25.0cm"
        :visible.sync="dealWithSubmitDlg"
        :before-close="handleClose"
        append-to-body
        :close-on-click-modal=false>
      <div>
        <FeeSubmitDlg
            :key="appItemCode"
            :application-code="appItemCode"
            :close-dlg = closeDialog
        ></FeeSubmitDlg>
      </div>
    </el-dialog>

  </div>
</template>

<script>
import FileRdms from "@/components/rdms/file-rdms";
import Swal from "sweetalert2";
import FileUploadCore from "@/components/std/file-upload-core.vue";
import FeeApproveInfoDlg from "@/components/std/fee-approve-info-dlg.vue";
import FeeDealWithApproveDlg from "@/components/std/fee-deal-with-approve-dlg.vue";
import FeeSubmitDlg from "@/components/std/fee-submit-dlg.vue";
import App from "@/app.vue";

export default {
  components: {
    FeeApproveInfoDlg,
    FileRdms,
    FileUploadCore,
    FeeDealWithApproveDlg,
    FeeSubmitDlg,
    FlowViewer: () => import(/* webpackChunkName: "flow-viewer" */ "@/components/rdms/flow/flow-viewer.vue"),
  },
  name: "fee-deal-with-dlg",

  data: function() {
    return {
      dialogTitle: '',
      detailTab: 'work',
      appItemCode: '',
      materialApproveDlg: false,
      feeDealWithCompleteDlg: false,
      dealWithSubmitDlg: false,
      PROJECT_TYPE: PROJECT_TYPE,
      FILE_GROUPING: FILE_GROUPING,
      APPLICATION_STATUS: APPLICATION_STATUS,
      JOB_ITEM_TYPE: JOB_ITEM_TYPE,
      DOC_TYPE: DOC_TYPE,

      resFile: {},
      resFiles: [],
      resFileIds: [],
      resFile_deal: {},
      resFiles_deal: [],
      resFileIds_deal: [],
      resFile_submit: {},
      resFiles_submit: [],
      resFileIds_complete: [],

    }
  },

  props: {
    appItem: {
      default: null,
    },
    dealWithFlag: {
      default: null,
    },
    closeFunction: {
      type: Function,
      default: null
    },
  },

  computed: {
  },

  mounted: function() {
    let _this = this;
    _this.$bus.$on('fee-deal-with-dlg-close', (data)=>{
      if(data){
        _this.close();
      }
    });
  },

  methods: {
    complete(code){
      let _this = this;
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/fee/complete/' + code).then((response) => {
        let resp = response.data;
        if (resp.success) {
          _this.close();
          _this.$bus.$emit('refresh-fee-application-list', true);
          _this.$bus.$emit('refresh-manhour-num', true);
        }else{
          Toast.warning(resp.message, Swal);
        }
      });
    },

    getUploadFileList_app(resFiles){
      let _this = this;
      _this.resFiles = resFiles;
      if(_this.resFiles !== null){
        _this.resFileIds= [];
        if(resFiles && resFiles.length>0){
        resFiles.forEach(item => {
          _this.resFileIds.push(item.id);
        })
      }
        _this.updateFeeApproveFileListStr(_this.appItem);
      }
    },
    updateFeeApproveFileListStr(appItem){
      let _this = this;
      appItem.approveFileListStr = JSON.stringify(_this.resFileIds);
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/fee/updateFeeApproveFileListStr', appItem).then((response) => {
        let resp = response.data;
        if (! resp.success) {
          Toast.warning(resp.message, Swal);
        }
      });
    },

    getUploadFileList_deal(resFiles){
      let _this = this;
      _this.resFiles_deal = resFiles;
      if(_this.resFiles_deal !== null){
        _this.resFileIds_deal= [];
        resFiles.forEach(item => {
          _this.resFileIds_deal.push(item.id);
        })
        _this.updateFeeDealWithFileListStr(_this.appItem);
      }
    },
    updateFeeDealWithFileListStr(appItem){
      let _this = this;
      appItem.dealWithFileListStr = JSON.stringify(_this.resFileIds_deal);
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/fee/updateFeeDealWithFileListStr', appItem).then((response) => {
        let resp = response.data;
        if (! resp.success) {
          Toast.warning(resp.message, Swal);
        }
      });
    },

    getUploadFileList_submit(resFiles){
      let _this = this;
      _this.resFiles_submit = resFiles;
      if(_this.resFiles_submit !== null){
        _this.resFileIds_complete= [];
        resFiles.forEach(item => {
          _this.resFileIds_complete.push(item.id);
        })
        _this.updateFeeCompleteFileListStr(_this.appItem);
      }
    },
    updateFeeCompleteFileListStr(appItem){
      let _this = this;
      appItem.completeFileListStr = JSON.stringify(_this.resFileIds_complete);
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/fee/updateFeeCompleteFileListStr', appItem).then((response) => {
        let resp = response.data;
        if (! resp.success) {
          Toast.warning(resp.message, Swal);
        }
      });
    },

    confirm(){
      let _this = this;
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/character/save-confirm-manhour', _this.character).then((response)=>{
        let resp = response.data;
        if (resp.success) {
          _this.closeFunction();
        } else {
          Toast.warning(resp.message, Swal);
        }
      })
    },

    feeApprove(appItem) {
      let _this = this;
      _this.appItemCode = appItem.code;
      _this.materialApproveDlg = true;
      _this.dialogTitle = "费用申请审批单";
    },

    feeDealWithComplete(appItem) {
      let _this = this;
      _this.appItemCode = appItem.code;
      _this.feeDealWithCompleteDlg = true;
      _this.dialogTitle = "费用处理审批单";
    },

    dealWithSubmit(appItem) {
      let _this = this;
      _this.appItemCode = appItem.code;
      _this.dealWithSubmitDlg = true;
      _this.dialogTitle = "支付完成审批";
    },

    closeDialog() {
      let _this = this;
      _this.materialApproveDlg = false;
      _this.feeDealWithCompleteDlg = false;
      _this.dealWithSubmitDlg = false;
    },
    handleClose(done) {
      done();
    },

    close(){
      let _this = this;
      _this.closeFunction();
    },

  }
}
</script>
<style>
.ca-operation-box{

}
.ca-operation-box-border{
  width: 900px;
  padding: 15px 15px;
  min-height: 60px;
  margin: 20px auto;
  border-radius: 8px;
  border-top: 1px dotted #1a1a1a;
  border-right: 1px dotted #1a1a1a;
  border-bottom: 1px dotted #1a1a1a;
  border-left: 1px dotted #1a1a1a;
  position: relative;
}
.ca-operation-title{
  position: absolute;
  top: -15px;
  left: 20px;
  line-height: 28px;
  padding-left: 10px;
  padding-right: 10px;
  background-color: white;
  font-weight: bold;
  font-size: 14px;
}
.ca-operation-content{

}
</style>

<style lang="scss">
@import './../../../public/static/scss/caipd.scss';

</style>
