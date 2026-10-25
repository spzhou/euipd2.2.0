<!--
 * Author: 周朔鹏
 * Date: 2024-12-22
 * Email: 391902958@qq.com
 * Website: https://www.euipd.com
-->

<template>
  <div>
    <div class="dialog-container">
      <!--
      @submit.native.prevent
      在 el-form 中 加个 @submit.native.prevent
      @submit: 表单提交
      .native 绑定系统原生事件
      .prevent 提交以后不刷新页面
      -->
      <el-form
          label-width="130px"
          :show-message=false
          @submit.native.prevent
          class="dialog-width">
        <el-row>
          <el-col :span="7">
            <el-form-item label="申请单号:">
              <el-col :span="23">
                <span style="font-size: 14px;">{{ dialogInfo.code }}</span>
              </el-col>
            </el-form-item>
          </el-col>
          <el-col :span="7">
            <el-form-item label="申请标题:">
              <el-col :span="23">
                <span style="font-size: 14px;">{{ dialogInfo.name }}</span>
              </el-col>
            </el-form-item>
          </el-col>
          <el-col :span="8" v-if="dialogInfo.characterName === null">
            <el-form-item label="任务名称:">
              <el-col :span="23">
                <span style="font-size: 14px;">{{ dialogInfo.jobitemName }}</span>
              </el-col>
            </el-form-item>
          </el-col>
          <el-col :span="8" v-if="dialogInfo.characterName">
            <el-form-item label="特性名称:">
              <el-col :span="23">
                <span style="font-size: 14px;">{{ dialogInfo.characterName }}</span>
              </el-col>
            </el-form-item>
          </el-col>
        </el-row>
        <div class="hr hr-6 dotted"></div>
        <el-row>
          <el-col :span="7">
            <el-form-item label="申请金额">
              <el-col :span="24">
                {{ dialogInfo.accountCost | rmbFormat }}
              </el-col>
            </el-form-item>
          </el-col>
          <el-col :span="7">
            <el-form-item label="费用类型">
              <el-col :span="24">
                {{ FEE_TYPE_STD | optionKV(dialogInfo.type) }}
              </el-col>
            </el-form-item>
          </el-col>
          <el-col :span="7">
            <el-form-item label="经费来源" required>
              <el-col :span="24">
                <div v-if="dialogInfo.status === APPLICATION_STATUS.APPLICATION.key">
                  <el-select v-model="dialogInfo.capitalSource" filterable placeholder="请选择">
                    <el-option
                        v-for="item in CAPITAL_SOURCE"
                        :key="item.key"
                        :label="item.value"
                        :value="item.key">
                    </el-option>
                  </el-select>
                </div>
                <div v-else>
                  {{ CAPITAL_SOURCE | optionKV(dialogInfo.capitalSource) }}
                </div>
              </el-col>
            </el-form-item>
          </el-col>

        </el-row>
        <el-row>
          <el-col :span="22">
            <el-form-item label="领用事由:">
              {{ dialogInfo.reason }}
            </el-form-item>
          </el-col>
        </el-row>
        <el-row v-if=" docType === DOC_TYPE.CHARACTER.key && dialogInfo.status === APPLICATION_STATUS.APPLICATION.key  && jobType !== JOB_ITEM_TYPE.QUALITY.key">
          <div style="text-align: left; margin:0 60px 10px;">
            <div>
              <i class="el-icon-info" :class="[ getFeeRemain < 0 ? redColor : '']"></i>
              审批前资金概要:
              <em>
                <span :class="[ getFeeRemain < 0 ? redColor : '']">"其他费"可用余额: {{ getFeeRemain | rmbFormat }}; </span>
                <span :class="[ getRemainBudget < 0 ? redColor : '']">特性预算余额: {{ getRemainBudget | rmbFormat }}</span>
              </em>
            </div>
            <div>
              <i class="el-icon-info" :class="[ getFeeRemain < 0 ? redColor : '']"></i>
              审批后资金概要:
              <em>
                <span :class="[ getFeeRemain-dialogInfo.accountCost < 0 ? redColor : '']">"其他费"可用余额: {{ getFeeRemain-dialogInfo.accountCost | rmbFormat }}; </span>
                <span :class="[ getRemainBudget-dialogInfo.accountCost < 0 ? redColor : '']">特性预算余额: {{ getRemainBudget-dialogInfo.accountCost | rmbFormat }}</span>
              </em>
            </div>
          </div>
        </el-row>
        <el-row v-if=" docType === DOC_TYPE.SUB_PROJECT.key && dialogInfo.status === APPLICATION_STATUS.APPLICATION.key ">
          <div style="text-align: left; margin:0 60px 10px;">
            <div>
              <i class="el-icon-info" :class="[ getFeeRemain < 0 ? redColor : '']"></i>
              审批前资金概要:
              <em>
                <span :class="[ getRemainBudget < 0 ? redColor : '']">子项目预算余额: {{ getRemainBudget | rmbFormat }}</span>
              </em>
            </div>
            <div>
              <i class="el-icon-info" :class="[ getFeeRemain < 0 ? redColor : '']"></i>
              审批后资金概要:
              <em>
                <span :class="[ getRemainBudget-dialogInfo.accountCost < 0 ? redColor : '']">子项目预算余额: {{ getRemainBudget-dialogInfo.accountCost | rmbFormat }}</span>
              </em>
            </div>
          </div>
        </el-row>
        <el-row>
          <el-col :span="22">
            <el-form-item label="审批意见:" required>
              <div v-if="dialogInfo.status === APPLICATION_STATUS.APPLICATION.key">
                <el-input type="textarea" rows="5" v-model="dialogInfo.approveDescription"></el-input>
              </div>
              <div v-else>
                {{ dialogInfo.approveDescription }}
              </div>
            </el-form-item>
          </el-col>
        </el-row>
        <div v-if="dialogInfo.status === APPLICATION_STATUS.SUBMIT.key || dialogInfo.status === APPLICATION_STATUS.PRE_COMPLETE.key || dialogInfo.status === APPLICATION_STATUS.COMPLETE.key">
          <div class="hr hr-8 dotted"></div>
          <el-row>
            <div style="margin-left: 60px;">
              <div>
                <div style="line-height: 28px;">
                  费用审批表扫描件:
                </div>
                <div style="display: flex; flex-direction: column; margin-bottom: 15px;">
                  <div v-for="(item, i) in dialogInfo.approveFileList" :key="i" style="display: flex; flex-direction: row; align-items: center; margin: 5px 0 0 50px;">
                    <div v-if="item">
                      《{{ item.name }}》
                    </div>
                    <div v-if="item">
                      <el-button type="text"  style="margin-left: 20px;" @click="downloadFile(item)">下载</el-button>
                    </div>
                  </div>
                </div>
              </div>
              <div>
                <div style="line-height: 28px;">
                  费用处理相关票据或证明文件:
                </div>
                <div style="display: flex; flex-direction: column; margin-bottom: 15px;">
                  <div v-for="(item, i) in dialogInfo.dealWithFileList" :key="i" style="display: flex; flex-direction: row; align-items: center; margin: 5px 0 0 50px;">
                    <div v-if="item">
                      《{{ item.name }}》
                    </div>
                    <div v-if="item">
                      <el-button type="text"  style="margin-left: 20px;" @click="downloadFile(item)">下载</el-button>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </el-row>
        </div>
        <div v-if="dialogInfo.status === APPLICATION_STATUS.SUBMIT.key">
          <div class="hr hr-8 dotted"></div>
          <el-row>
            <el-col :span="11">
              <el-form-item label="实际费用:">
                <el-col :span="23" style="font-size: 15px; font-weight: bold;">
                  {{ dialogInfo.actualCost | rmbFormat }}
                </el-col>
              </el-form-item>
            </el-col>
          </el-row>
          <el-row>
            <el-form-item label="提交说明:">
              <el-col :span="22">
                {{ dialogInfo.submitDescription }}
              </el-col>
            </el-form-item>
          </el-row>
        </div>
        <div class="hr hr-8 dotted"></div>
        <el-row>
          <el-col :offset="1" :span="6">
            <el-form-item label="申请人:">
              <el-col :span="24">
                <span style="font-size: 14px;">{{ dialogInfo.writerName }}</span>
              </el-col>
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="审批人:">
              <el-col :span="24">
                <span style="font-size: 14px;">{{ dialogInfo.approverName }}</span>
              </el-col>
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="审批时间:">
              <el-col :span="24">
                <div v-if="dialogInfo.status===APPLICATION_STATUS.APPLICATION.key">
                  <span style="font-size: 14px;">{{ getNowTime }}</span>
                </div>
                <div v-else>
                  {{ dialogInfo.approveTime | dateFormat }}
                </div>
              </el-col>
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <div v-if="dialogInfo.status===APPLICATION_STATUS.APPLICATION.key" class="clearfix form-actions">
        <div style="display: flex; justify-content: center;">
          <button type="button" v-on:click="approve('approve')" class="ca-btn-primary">批 准</button>
          <button type="button" v-on:click="approve('reject')" class="ca-btn-primary">拒 绝</button>
        </div>
      </div>
      <div v-if="dialogInfo.status===APPLICATION_STATUS.SUBMIT.key" class="clearfix form-actions">
        <div style="display: flex; justify-content: center;">
          <button type="button" v-on:click="approve('submit_approve')" class="ca-btn-primary">核 准</button>
          <button type="button" v-on:click="approve('submit_reject')" class="ca-btn-primary">退 回</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import Swal from "sweetalert2";
import {dateFormat, optionKV, rmbFormat} from "@/filter/filter";
import FileSaver from 'file-saver';

export default {
  components: {
  },
  name: "fee-approve-dlg",

  props: {
    applicationCode: {
      default: null
    },
    jobType: {
      default: null
    },
    docType: {
      default: null,
    },
    closeDlg: {
      type: Function,
      default: null
    },
  },

  data() {
    return {
      dialogInfo: {
        code: '',
        capitalSource: CAPITAL_SOURCE.SELF.key,
      },
      materialList: [],
      APPLICATION_STATUS: APPLICATION_STATUS,
      JOB_ITEM_TYPE: JOB_ITEM_TYPE,
      DOC_TYPE: DOC_TYPE,
      CAPITAL_SOURCE: CAPITAL_SOURCE,

      loginUser: {},
      loginCustomer: {},
      jobItem: {},

      characterBudgetSummary: {},
      subProject: {},
      subProjectRemainBudget: 0,

      FEE_TYPE_STD: FEE_TYPE_STD,

      redColor: 'red-color',
    };
  },

  computed: {
    getNowTime(){
      return dateFormat(new Date());
    },

    getFeeRemain(){
      let _this = this;
      if(_this.docType === DOC_TYPE.CHARACTER.key && _this.jobType !== JOB_ITEM_TYPE.QUALITY.key){
        return _this.characterBudgetSummary.otherFeeRemain;
      }else{
        return 0;
      }

    },
    getRemainBudget(){
      let _this = this;
      if(_this.docType === DOC_TYPE.CHARACTER.key && _this.jobType !== JOB_ITEM_TYPE.QUALITY.key){
        return _this.characterBudgetSummary.remainBudget;
      }else if(_this.docType === DOC_TYPE.SUB_PROJECT.key){
        return _this.subProjectRemainBudget;
      }else{
        return 0;
      }
    },
  },

  mounted: function () {
    let _this = this;

    _this.getLoginUser();
    if(Tool.isNotEmpty(_this.applicationCode)){
      _this.getFeeApplicationByCode(_this.applicationCode);
    }
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

    getFeeApplicationByCode(code){
      let _this = this;
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/fee/getFeeApplicationByCode/' + code).then((response) => {
        let resp = response.data;
        if (resp.success) {
          _this.dialogInfo = resp.content;
          if (!_this.dialogInfo.capitalSource) {
            _this.$set(_this.dialogInfo, 'capitalSource', CAPITAL_SOURCE.SELF.key);
          }
          if(_this.docType === DOC_TYPE.CHARACTER.key
              && (_this.jobType === JOB_ITEM_TYPE.DEVELOP.key
                  || _this.jobType === JOB_ITEM_TYPE.ASSIST.key
                  || _this.jobType === JOB_ITEM_TYPE.TEST.key
                  || _this.jobType === JOB_ITEM_TYPE.CHARACTER_INT.key)){
            _this.getCharacterBudgetExeSummary(_this.dialogInfo.characterId)
          } else if (_this.docType === DOC_TYPE.SUB_PROJECT.key
              && (_this.jobType === JOB_ITEM_TYPE.TASK_SUBP.key
                  || _this.jobType === JOB_ITEM_TYPE.TEST.key)) {
            _this.getSubprojectSelfRemainBudget(_this.dialogInfo.subprojectId);
          }
        }else{
          Toast.warning(resp.message, Swal);
        }
      });
    },

    getCharacterBudgetExeSummary(characterId) {
      let _this = this;
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/character/getCharacterBudgetExeSummary/' + characterId).then((response) => {
        let resp = response.data;
        if (resp.success) {
          _this.characterBudgetSummary = resp.content;
        }else{
          Toast.warning(resp.message, Swal);
        }
      });
    },

    getSubprojectSelfRemainBudget(subprojectId){
      let _this = this;
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/sub-project/getSubprojectSelfRemainBudget/' + subprojectId).then((response) => {
        let resp = response.data;
        if (resp.success) {
          _this.subProjectRemainBudget = resp.content;
        } else {
          Toast.warning(resp.message, Swal);
        }
      })
    },

    /**
     * 审批
     */
    approve(flag) {
      let _this = this;
      // 保存校验
      if( flag === 'approve' || flag === 'reject' ){
        if (1 !== 1
            || !Validator.require(_this.dialogInfo.code, "申请单号",Swal)
            || !Validator.require(_this.dialogInfo.approveDescription, "审批意见",Swal)
            || (flag === 'approve' && !Validator.require(_this.dialogInfo.capitalSource, "资金来源",Swal))

            || !Validator.length(_this.dialogInfo.approveDescription, "审批意见", 2, 300,Swal)
        ) {
          return;
        }
      }

      Swal.fire({
        title: '确认',
        text: "是否确认执行审批操作?",
        icon: 'question',
        showCancelButton: true,
        confirmButtonColor: '#409EFF',
        cancelButtonColor: '#909399',
        confirmButtonText: '确认!',
        cancelButtonText: '取消!'
      }).then((result) => {
        if (result.isConfirmed) {
          if(flag === 'approve'){
            _this.dialogInfo.approveStatus = APPLICATION_STATUS.APPROVED.key ;
          }else if(flag === 'reject'){
            _this.dialogInfo.approveStatus = APPLICATION_STATUS.REJECT.key ;
          }else if(flag === 'submit_approve'){
            _this.dialogInfo.approveStatus = APPLICATION_STATUS.PRE_COMPLETE.key ;
          }else if(flag === 'submit_reject'){
            _this.dialogInfo.approveStatus = APPLICATION_STATUS.SUPPLEMENT.key ;
          }
          _this.dialogInfo.loginUserId = _this.loginUser.customerUserId;
          _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/fee/approveFeeApplication', _this.dialogInfo).then((response)=>{
            let resp = response.data;
            if (resp.success) {
              _this.close();
              _this.$bus.publish({ areas: ['fee', 'character', 'project', 'product', 'develop', 'quality', 'badge'], reason: 'fee-approve' });
              Toast.success("审批完成", Swal);
            } else {
              Toast.warning(resp.message, Swal);
            }
          })
        }
      }).catch(_ => {
      });
    },

    close(){
      let _this = this;
      _this.closeDlg();
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

  },
}
</script>

<style>
.el-input--mini .el-input__inner {
  height: 28px;
  line-height: 28px;
  text-align: center !important;
}
</style>

<style lang="scss" scoped>
@import '../../../public/static/scss/caipd';

.text-box{
  display: inline-block;
  padding-left: 3px;
  padding-right: 15px;
  line-height: 30px;
  //width: 100px;
  background-color: rgba(204, 204, 204, 0.20);
}
.dialog-width{
  width: 100%;
}
.red-color{
  color: red;
}
</style>
