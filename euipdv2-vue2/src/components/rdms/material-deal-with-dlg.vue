<!--
 * Author: 周朔鹏
 * Date: 2025-02-26
 * Email: 391902958@qq.com
 * Website: https://www.euipd.com
-->

<template>
  <div class="dialog-container">
    <div style="margin: 0 100px 15px;">
      注意 : 本表单是逐步完成的, 您完成相关工作后, 即可操作相应部分. 系统会记录相关操作和文件. 等到全部工作完成后, 再提交主管经理. 值得注意的是, 所有"费用化"物料在相应的工作评审之前, 必须提交, 以便生成评审表格, 进行费用等统计.
    </div>
    <div class="ca-operation-box">
      <div class="ca-operation-box-border">
        <div class="ca-operation-title">
          <div v-if="isReturnItem">
            Step 1 : 《物料退料审批单》
          </div>
          <div v-else>
            Step 1 : 《领料审批单》
          </div>
        </div>
        <div class="ca-operation-content">
          <div>
            <div v-if="isReturnItem">
              {{itemName}}是到公司仓库管理部门申退物料的凭证. 打印后, 需要根据公司物料管理之规定, 到相关部门签字, 并最终领取物料.
            </div>
            <div v-else>
              {{itemName}}是到公司仓库管理部门申领物料的凭证. 打印后, 需要根据公司物料管理之规定, 到相关部门签字, 并最终领取物料.
            </div>
          </div>
          <div style="display: flex; flex-direction: row; justify-content: end; margin-right: 20px; margin-top: 20px;">
            <el-button type="primary" @click="materialApprove(appItem)">打印《{{ itemName }}》</el-button>
          </div>
        </div>
      </div>
      <div class="ca-operation-box-border">
        <div class="ca-operation-title">
          Step 2 : 上传《{{itemName}}》扫描件
        </div>
        <div class="ca-operation-content">
          <div>
            经过签署的文件, 请扫描或拍照后上传到系统中, 作为物料管理的凭据. 如果有附加说明信息, 请在签署文件上填写, 并经过相关部门确认.
          </div>
          <div style="display: flex; flex-direction: row; justify-content: end; margin-top: 20px; margin-right: 20px;">
            <div style="width: 700px;">
              <FileUploadCore
                  :input-id="('material-deal-with-dlg-step2-' + (appItem.id))"
                  :key="('material-file-upload-step2-' + (appItem.id))"
                  :x-project-id="appItem.subprojectId"
                  :item-id="appItem.id"
                  :file-group="FILE_GROUPING.MATERIAL_APP.key"
                  :project-type="PROJECT_TYPE.SUB_PROJECT.key"
                  :en-delete="appItem.status === APPLICATION_STATUS.SUPPLEMENT.key || appItem.status === APPLICATION_STATUS.APPROVED.key"
                  :read-db-record="true"
                  v-on:uploadFileList="getUploadFileList_app">
              </FileUploadCore>
            </div>
          </div>
        </div>
      </div>
      <div class="ca-operation-box-border">
        <div class="ca-operation-title">
          Step 3 : 上传物料处置证明及项目单据
        </div>
        <div class="ca-operation-content">
          <div>
            物料处置是指对物料的实际使用和处理. 如果是借用, 最终的处置是归还, 并获得验收部门的签字文件; 如果是资产化, 需要获得项目经理等相关人员的验收确认; 如果是费用化, 需要进行报废或相关处置证明文件等. 对申领的物料处置完成后, 将相关单据和签字文件上传到系统, 否则, 你将长期成为该物料的保管人, 公司有权向你追回相关物资.
          </div>
          <div style="display: flex; flex-direction: row; justify-content: end; margin-top: 20px; margin-right: 20px;">
            <div style="width: 700px;">
              <FileUploadCore
                  :input-id="('material-deal-with-dlg-step3-' + (appItem.id))"
                  :key="('material-file-upload-step3-' + (appItem.id))"
                  :x-project-id="appItem.subprojectId"
                  :item-id="appItem.id"
                  :file-group="FILE_GROUPING.MATERIAL_DEAL.key"
                  :project-type="PROJECT_TYPE.SUB_PROJECT.key"
                  :en-delete="appItem.status === APPLICATION_STATUS.SUPPLEMENT.key || appItem.status === APPLICATION_STATUS.APPROVED.key"
                  :read-db-record="true"
                  v-on:uploadFileList="getUploadFileList_deal">
              </FileUploadCore>
            </div>
          </div>
        </div>
      </div>
      <div class="ca-operation-box-border">
        <div class="ca-operation-title">
          Step 4 : 打印《物料处置审批单》
        </div>
        <div class="ca-operation-content">
          <div>
            请打印物料处置审批单, 交由相关职能部门领导审批.
          </div>
          <div style="display: flex; flex-direction: row; justify-content: end; margin-right: 20px; margin-top: 20px;">
            <el-button type="primary"  @click="materialDealWithComplete(appItem)">打印《物料处置审批单》</el-button>
          </div>
        </div>
      </div>
      <div class="ca-operation-box-border">
        <div class="ca-operation-title">
          Step 5 : 上传《物料处置审批单》签审扫描件
        </div>
        <div class="ca-operation-content">
          <div>
            请上传物料处置审批单.
          </div>
          <div style="display: flex; flex-direction: row; justify-content: end; margin-top: 20px; margin-right: 20px;">
            <div style="width: 700px;">
              <FileUploadCore
                  :input-id="('material-deal-with-dlg-step5-' + (appItem.id))"
                  :key="('material-file-upload-step5-' + (appItem.id))"
                  :x-project-id="appItem.subprojectId"
                  :item-id="appItem.id"
                  :file-group="FILE_GROUPING.MATERIAL_SUBMIT.key"
                  :project-type="PROJECT_TYPE.SUB_PROJECT.key"
                  :en-delete="appItem.status === APPLICATION_STATUS.SUPPLEMENT.key || appItem.status === APPLICATION_STATUS.APPROVED.key"
                  :read-db-record="true"
                  v-on:uploadFileList="getUploadFileList_submit">
              </FileUploadCore>
            </div>
          </div>
        </div>
      </div>
      <div class="ca-operation-box-border">
        <div class="ca-operation-title">
          Step 6 : 结束
        </div>
        <div class="ca-operation-content">
          <div>
            提交后, 相关物料将从在手物料中移除, 您将结束对上述物料的管理责任.
          </div>
          <div v-if="appItem.status === APPLICATION_STATUS.SUPPLEMENT.key" style="margin: 8px 0 0 10px; color: #E6A23C;">
            复核驳回! 请做好沟通, 重新整理材料并提交.
          </div>
          <div v-if="appItem.pendingIssue" style="margin: 8px 0 0 10px; color: #E6A23C;">
            待物料管理员发料确认, 发料后才能提交.
          </div>
          <div v-else-if="appItem.issueStatus === 'ISSUED'" style="margin: 8px 0 0 10px; color: #666;">
            发料人: {{ appItem.issuerName }}, 发料时间: {{ appItem.issueTime | dateFormat }}
          </div>
          <div v-if="!appItem.pendingIssue" style="display: flex; flex-direction: row; justify-content: end; margin-right: 20px; margin-top: 20px;">
            <el-button v-if="appItem.jobType !== JOB_ITEM_TYPE.QUALITY.key
                && appItem.jobType !== JOB_ITEM_TYPE.TASK_FUNCTION.key
                && appItem.auxStatus !== JOB_ITEM_TYPE.TASK_PRODUCT.key
                && appItem.auxStatus !== JOB_ITEM_TYPE.TASK_BUG.key
                " type="primary" @click="dealWithSubmit(appItem)">提交项目经理审批</el-button>
            <el-button v-if="appItem.jobType === JOB_ITEM_TYPE.QUALITY.key
                || appItem.auxStatus === JOB_ITEM_TYPE.TASK_PRODUCT.key
                || appItem.jobType === JOB_ITEM_TYPE.TASK_PRODUCT.key
                " type="primary" @click="dealWithSubmit(appItem)">提交产品经理审批</el-button>
            <el-button v-if="appItem.jobType === JOB_ITEM_TYPE.TASK_BUG.key
                || appItem.auxStatus === JOB_ITEM_TYPE.TASK_BUG.key
                " type="primary" @click="dealWithSubmit(appItem)">提交测试主管审批</el-button>
            <el-button v-if="appItem.jobType === JOB_ITEM_TYPE.TASK_FUNCTION.key"
                       type="primary" @click="dealWithSubmit(appItem)">提交系统工程总监审批</el-button>
          </div>
        </div>
      </div>

    </div>
    <div class="clearfix form-actions">
      <div style="display: flex; justify-content: center;">
<!--        <button v-on:click="confirm(confirmManhour)" class="ca-btn-primary" type="button">保存</button>-->
        <button type="button" v-on:click="close()" class="ca-btn-primary">关闭</button>
      </div>
    </div>

    <!--打印领料单/物料退料单-->
    <el-dialog
        width="25.0cm"
        :visible.sync="materialApproveDlg"
        :before-close="handleClose"
        append-to-body
        :close-on-click-modal=false>
      <div>
        <MaterialApproveInfoDlg
            :key="appItemCode"
            :application-code="appItemCode"
            :close-dlg = closeDialog
        ></MaterialApproveInfoDlg>
      </div>
    </el-dialog>

    <!--打印物料处置审批单-->
    <el-dialog
        width="25.0cm"
        :visible.sync="materialDealWithCompleteDlg"
        :before-close="handleClose"
        append-to-body
        :close-on-click-modal=false>
      <div>
        <MaterialDealWithApproveDlg
            :key="appItemCode"
            :application-code="appItemCode"
            :close-dlg = closeDialog
        ></MaterialDealWithApproveDlg>
      </div>
    </el-dialog>

    <!--物料处置 提交-->
    <el-dialog
        width="25.0cm"
        :visible.sync="dealWithSubmitDlg"
        :before-close="handleClose"
        append-to-body
        :close-on-click-modal=false>
      <div>
        <MaterialSubmitDlg
            :key="appItemCode"
            :application-code="appItemCode"
            :close-dlg = closeDialog
        ></MaterialSubmitDlg>
      </div>
    </el-dialog>

  </div>
</template>

<script>
import FileRdms from "@/components/rdms/file-rdms";
import Swal from "sweetalert2";
import MaterialApproveInfoDlg from "@/components/std/material-approve-info-dlg.vue";
import FileUploadCore from "@/components/std/file-upload-core.vue";
import MaterialDealWithApproveDlg from "@/components/std/material-deal-with-approve-dlg.vue";
import MaterialSubmitDlg from "@/components/std/material-submit-dlg.vue";

export default {
  components: {
    MaterialApproveInfoDlg,
    FileRdms,
    FileUploadCore,
    MaterialDealWithApproveDlg,
    MaterialSubmitDlg,
  },
  name: "material-deal-with-dlg",

  data: function() {
    return {
      dialogTitle: '',
      appItemCode: '',
      materialApproveDlg: false,
      materialDealWithCompleteDlg: false,
      dealWithSubmitDlg: false,
      PROJECT_TYPE: PROJECT_TYPE,
      FILE_GROUPING: FILE_GROUPING,
      APPLICATION_STATUS: APPLICATION_STATUS,
      JOB_ITEM_TYPE: JOB_ITEM_TYPE,

      resFile: {},
      resFiles: [],
      resFileIds: [],
      resFile_deal: {},
      resFiles_deal: [],
      resFileIds_deal: [],
      resFile_submit: {},
      resFiles_submit: [],
      resFileIds_complete: [],

      itemName: '',
    }
  },

  props: {
    appItem: {
      default: null,
    },
    closeFunction: {
      type: Function,
      default: null
    },
  },

  computed: {
    isReturnItem(){
      let _this = this;
      if(_this.appItem.opeStatus === MATERIAL_OPE_STATUS.RETURN_APPROVED.key){
        _this.itemName = "物料退料审批单";
        return true;
      }else{
        _this.itemName = "领料审批单";
        return false;
      }
    }
  },

  mounted: function() {
    let _this = this;
    _this.$bus.$on('material-deal-with-dlg-close', (data)=>{
      if(data){
        _this.close();
      }
    });
  },

  methods: {
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
        _this.updateMaterialApproveFileListStr(_this.appItem);
      }
    },

    updateMaterialApproveFileListStr(appItem){
      let _this = this;
      appItem.approveFileListStr = JSON.stringify(_this.resFileIds);
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/material/updateMaterialApproveFileListStr', appItem).then((response) => {
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
        _this.updateMaterialDealWithFileListStr(_this.appItem);
      }
    },
    updateMaterialDealWithFileListStr(appItem){
      let _this = this;
      appItem.dealWithFileListStr = JSON.stringify(_this.resFileIds_deal);
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/material/updateMaterialDealWithFileListStr', appItem).then((response) => {
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
        _this.updateMaterialCompleteFileListStr(_this.appItem);
      }
    },
    updateMaterialCompleteFileListStr(appItem){
      let _this = this;
      appItem.completeFileListStr = JSON.stringify(_this.resFileIds_complete);
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/material/updateMaterialCompleteFileListStr', appItem).then((response) => {
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

    materialApprove(appItem) {
      let _this = this;
      _this.appItemCode = appItem.code;
      _this.materialApproveDlg = true;
      if(_this.appItem.opeStatus === MATERIAL_OPE_STATUS.RETURN_APPROVED.key){
        _this.dialogTitle = "物料退料审批单";
      }else{
        _this.dialogTitle = "领料审批单";
      }
    },
    materialDealWithComplete(appItem) {
      let _this = this;
      _this.appItemCode = appItem.code;
      _this.materialDealWithCompleteDlg = true;
      _this.dialogTitle = "物料处置审批单";
    },

    dealWithSubmit(appItem) {
      let _this = this;
      _this.appItemCode = appItem.code;
      _this.dealWithSubmitDlg = true;
      _this.dialogTitle = "处置完成提交审批";
    },

    closeDialog() {
      let _this = this;
      _this.materialApproveDlg = false;
      _this.materialDealWithCompleteDlg = false;
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
