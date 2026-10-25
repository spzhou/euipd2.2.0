<!--
 * Author: 周朔鹏
 * Date: 2025-02-26
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
                <el-input type="text" style="font-size: 14px;" :value="dialogInfo.code" readonly></el-input>
              </el-col>
            </el-form-item>
          </el-col>
          <el-col :span="7">
            <el-form-item label="申请标题:">
              <el-col :span="23">
                <el-input type="text" style="font-size: 14px;" :value="dialogInfo.name" readonly></el-input>
              </el-col>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="任务名称:">
              <el-col :span="23">
                <el-input type="text" style="font-size: 14px;" :value="dialogInfo.jobitemName" readonly></el-input>
              </el-col>
            </el-form-item>
          </el-col>

        </el-row>
        <div class="hr hr-6 dotted"></div>
        <el-row>
          <div style="margin: 0 80px 20px 80px;">
            <el-table
                :data="materialList"
                stripe
                :summary-method="getSummaries"
                show-summary
                style="width: 100%">
              <el-table-column label="序号" align="center" width="80">
                <template slot-scope="scope">
                  {{ scope.$index + 1 }}
                </template>
              </el-table-column>
              <el-table-column
                  prop="bomCode"
                  label="物料编码">
              </el-table-column>
              <el-table-column
                  prop="name"
                  align="center"
                  label="物料名称">
              </el-table-column>
              <el-table-column
                  prop="model"
                  align="center"
                  label="型号">   <!--借用 资产化 费用化-->
              </el-table-column>
              <el-table-column
                  prop="unit"
                  width="80"
                  align="center"
                  label="计量单位">
              </el-table-column>
              <el-table-column
                  prop="amount"
                  width="80"
                  align="center"
                  label="领用数量">
              </el-table-column>
              <el-table-column
                  prop="preTaxUnitPrice"
                  label="未税单价(元)"
                  align="center"
                  width="120">
              </el-table-column>
              <el-table-column
                  prop="preTaxSumPrice"
                  align="center"
                  width="110"
                  label="总价(元)">
              </el-table-column>
            </el-table>
          </div>
        </el-row>
<!--        <el-row
            v-if="docType === DOC_TYPE.CHARACTER.key && dialogInfo.status === APPLICATION_STATUS.APPLICATION.key && jobType !== JOB_ITEM_TYPE.QUALITY.key">
          <div style="text-align: right; margin-right: 100px;">
            <i class="el-icon-info" :class="[ getMaterialRemain < 0 ? redColor : '']"></i>
            物料费用使用概要:
            <em>
              审批物料费总额: {{ characterBudgetSummary.materialFeeApproved | rmbFormat }};
              <span :class="[ getMaterialRemain < 0 ? redColor : '']">可用余额: {{
                  getMaterialRemain | rmbFormat
                }}; </span>
              <span :class="[ getRemainBudget < 0 ? redColor : '']">特性预算余额: {{
                  getRemainBudget | rmbFormat
                }}</span>
            </em>
          </div>
        </el-row>-->
        <!--        <el-row  v-if="docType === DOC_TYPE.SUB_PROJECT.key && dialogInfo.status === APPLICATION_STATUS.APPLICATION.key">
                  <div style="text-align: right; margin-right: 100px;">
                    <i class="el-icon-info" :class="[ getMaterialRemain < 0 ? redColor : '']"></i>
                    费用使用概要:
                    <em>
                      费总额: {{ subProjectRemainBudget | rmbFormat }};
                      <span :class="[ getRemainBudget < 0 ? redColor : '']">非特性开发预算余额: {{ getRemainBudget | rmbFormat }}</span>
                    </em>
                  </div>
                </el-row>-->
        <el-row>
          <el-col :span="22">
            <el-form-item label="经费来源:" required v-if="opeStatus !== MATERIAL_OPE_STATUS.RETURN_APPLICATION.key">
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
            </el-form-item>
            <el-form-item label="使用方式:" required>
              <el-col :span="24">
                {{ MATERIAL_USAGE_MODE | optionKV(dialogInfo.usageMode) }}
              </el-col>
              <el-col :span="24">
                <div v-if="dialogInfo.usageMode===MATERIAL_USAGE_MODE.EXPENDITURE.key">
                  说明: "费用化"是指上述物料将在研发工作中消耗掉, <strong>作为项目成本记账</strong>.
                </div>
                <div v-if="dialogInfo.usageMode===MATERIAL_USAGE_MODE.CAPITALIZATION.key">
                  说明: "资产化"是指上述物料将被转化为公司固定资产或者成本被开发的产品的一部分.
                  <strong>领用时暂不作为成本记账</strong>. 如果转化为公司固定资产, 将在评审前归还, 由公司资产部门确认;
                  如果作为被开发产品的一部分, 则将在评审前, 交付给相关部门, 由接收部门核验; 如果项目宣告研发失败,
                  则需要评估上述物料是否可回收, 由相关部门评审后, 决定项目物料成本计入.
                </div>
                <div v-if="dialogInfo.usageMode===MATERIAL_USAGE_MODE.BORROW.key">
                  说明: "借用"是指上述物料将被研发人员临时使用. 主要指相关工具、设备、临时物料等.
                  <strong>领用时暂不作为成本记账</strong>. 上述物料将在评审前归还, 根据实际情况和相关公司制度, 确定损耗或折旧后,
                  进行成本入账. 一般要求, A项目评审前, 需归还A项目所借用的项目物资, B项目再用时, 在B项目下重新借用,
                  以便成本归集和管理.
                </div>
              </el-col>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="22">
            <el-form-item label="退料说明:" required v-if="opeStatus === MATERIAL_OPE_STATUS.RETURN_APPLICATION.key || opeStatus === MATERIAL_OPE_STATUS.RETURN_APPROVED.key">
              {{ dialogInfo.reason }}
            </el-form-item>
            <el-form-item label="领用事由:" required v-else>
              {{ dialogInfo.reason }}
            </el-form-item>

          </el-col>
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
        <div v-if="dialogInfo.status === APPLICATION_STATUS.SUBMIT.key">
          <div class="hr hr-8 dotted"></div>
          <el-row>
            <div style="margin-left: 60px;">
              <div>
                <div style="line-height: 28px;">
                  物料审批表扫描件:
                </div>
                <div style="display: flex; flex-direction: column; margin-bottom: 15px;">
                  <div v-for="(item, i) in dialogInfo.approveFileList" :key="i"
                       style="display: flex; flex-direction: row; align-items: center; margin: 5px 0 0 50px;">
                    <div v-if="item">
                      《{{ item.name }}》
                    </div>
                    <div v-if="item">
                      <el-button type="text" style="margin-left: 20px;" @click="downloadFile(item)">下载</el-button>
                    </div>
                  </div>
                </div>
              </div>
              <div>
                <div style="line-height: 28px;">
                  物料处置相关签署文件:
                </div>
                <div style="display: flex; flex-direction: column; margin-bottom: 15px;">
                  <div v-for="(item, i) in dialogInfo.dealWithFileList" :key="i"
                       style="display: flex; flex-direction: row; align-items: center; margin: 5px 0 0 50px;">
                    <div v-if="item">
                      《{{ item.name }}》
                    </div>
                    <div v-if="item">
                      <el-button type="text" style="margin-left: 20px;" @click="downloadFile(item)">下载</el-button>
                    </div>
                  </div>
                </div>
              </div>
              <div>
                <div style="line-height: 28px;">
                  物料处置线下审批文件:
                </div>
                <div style="display: flex; flex-direction: column; margin-bottom: 15px;">
                  <div v-for="(item, i) in dialogInfo.completeFileList" :key="i"
                       style="display: flex; flex-direction: row; align-items: center; margin: 5px 0 0 50px;">
                    <div v-if="item">
                      《{{ item.name }}》
                    </div>
                    <div v-if="item">
                      <el-button type="text" style="margin-left: 20px;" @click="downloadFile(item)">下载</el-button>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </el-row>
          <div class="hr hr-8 dotted"></div>
          <el-row>
            <el-form-item label="提交说明:">
              <el-col :span="24">
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
                <el-input type="text" style="font-size: 14px;" :value="dialogInfo.writerName" readonly></el-input>
              </el-col>
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="审批人:">
              <el-col :span="24">
                <el-input type="text" style="font-size: 14px;" :value="loginUser.trueName" readonly></el-input>
              </el-col>
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="审批时间:">
              <el-col :span="24">
                <el-input type="text" style="font-size: 14px;" :value="getNowTime" readonly></el-input>
              </el-col>
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <div v-if="dialogInfo.status===APPLICATION_STATUS.APPLICATION.key" class="clearfix form-actions">
        <div style="display: flex; justify-content: center;">
          <button v-if="haveMaterialList" type="button" v-on:click="approve('approve')" class="ca-btn-primary">批 准
          </button>
          <button v-if="! haveMaterialList" type="button" class="ca-btn-primary-disable">批 准</button>
          <button type="button" v-on:click="approve('reject')" class="ca-btn-primary">拒 绝</button>
        </div>
      </div>
      <div v-if="dialogInfo.status===APPLICATION_STATUS.SUBMIT.key" class="clearfix form-actions">
        <div style="display: flex; justify-content: center;">
          <button type="button" v-on:click="approve('submit_approve')" class="ca-btn-primary">批 准</button>
          <button type="button" v-on:click="approve('submit_reject')" class="ca-btn-primary">退 回</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import Swal from "sweetalert2";
import {dateFormat, optionKV, rmbFormat} from "@/filter/filter";
import MaterialAddDlg from "@/components/std/material-add-dlg.vue";
import FileSaver from 'file-saver';

export default {
  components: {
    MaterialAddDlg,
  },
  name: "material-approve-dlg",

  props: {
    applicationCode: {
      default: null
    },
    jobType: {
      default: null,
    },
    docType: {
      default: null,
    },
    opeStatus: {   // 操作状态 退料等
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
      MATERIAL_USAGE_MODE: MATERIAL_USAGE_MODE,
      APPLICATION_STATUS: APPLICATION_STATUS,
      JOB_ITEM_TYPE: JOB_ITEM_TYPE,
      DOC_TYPE: DOC_TYPE,
      CAPITAL_SOURCE: CAPITAL_SOURCE,
      MATERIAL_OPE_STATUS: MATERIAL_OPE_STATUS,

      loginUser: {},
      loginCustomer: {},
      jobItem: {},

      characterBudgetSummary: {},
      subprojectBudgetSummary: {},

      subProjectRemainBudget: 0,
      sumCharge: 0,

      redColor: 'red-color',
    };
  },

  computed: {
    getNowTime() {
      return dateFormat(new Date());
    },
    haveMaterialList() {
      let _this = this;
      return Tool.isNotEmpty_ES5(_this.materialList);
    },
    getMaterialRemain() {
      let _this = this;
      if (_this.docType === DOC_TYPE.CHARACTER.key && _this.jobType !== JOB_ITEM_TYPE.QUALITY.key) {
        if (_this.dialogInfo.usageMode === MATERIAL_USAGE_MODE.EXPENDITURE.key && _this.dialogInfo.status === APPLICATION_STATUS.APPLICATION.key) {
          return _this.characterBudgetSummary.materialFeeRemain - _this.sumCharge;
        } else {
          return _this.characterBudgetSummary.materialFeeRemain;
        }
      } else {
        return 0;
      }
    },
    getRemainBudget() {
      let _this = this;
      if (_this.docType === DOC_TYPE.CHARACTER.key && _this.jobType !== JOB_ITEM_TYPE.QUALITY.key) {
        if (_this.dialogInfo.usageMode === MATERIAL_USAGE_MODE.EXPENDITURE.key && _this.dialogInfo.status === APPLICATION_STATUS.APPLICATION.key) {
          return _this.characterBudgetSummary.remainBudget - _this.sumCharge;
        } else {
          return _this.characterBudgetSummary.remainBudget;
        }
      } else if (_this.docType === DOC_TYPE.SUB_PROJECT.key) {
        if (_this.dialogInfo.usageMode === MATERIAL_USAGE_MODE.EXPENDITURE.key && _this.dialogInfo.status === APPLICATION_STATUS.APPLICATION.key) {
          return _this.subProjectRemainBudget - _this.sumCharge;
        } else {
          return _this.subProjectRemainBudget;
        }
      } else {
        return 0;
      }
    },
  },

  mounted: function () {
    let _this = this;

    _this.getLoginUser();
    if (Tool.isNotEmpty(_this.applicationCode)) {
      _this.getMaterialApplicationByCode(_this.applicationCode);
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

    getMaterialApplicationByCode(code) {
      let _this = this;
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/material/getMaterialApplicationByCode/' + code).then((response) => {
        let resp = response.data;
        if (resp.success) {
          _this.dialogInfo = resp.content;
          if (!_this.dialogInfo.capitalSource) {
            _this.$set(_this.dialogInfo, 'capitalSource', CAPITAL_SOURCE.SELF.key);
          }
          _this.materialList = _this.dialogInfo.materialList;
          _this.materialList.forEach(item => {
            if (item.preTaxUnitPrice === null) {
              item.preTaxUnitPrice = 0;
              item.preTaxSumPrice = 0;
            }
          })

          if (_this.docType === DOC_TYPE.CHARACTER.key
              && (_this.jobType === JOB_ITEM_TYPE.DEVELOP.key
                  || _this.jobType === JOB_ITEM_TYPE.ASSIST.key
                  || _this.jobType === JOB_ITEM_TYPE.TEST.key
                  || _this.jobType === JOB_ITEM_TYPE.CHARACTER_INT.key)) {
            _this.getCharacterBudgetExeSummary(_this.dialogInfo.characterId);
          } else if (_this.docType === DOC_TYPE.SUB_PROJECT.key
              && (_this.jobType === JOB_ITEM_TYPE.TASK_SUBP.key
                  || _this.jobType === JOB_ITEM_TYPE.TEST.key)) {
            _this.getSubprojectSelfRemainBudget(_this.dialogInfo.subprojectId);
          }
        } else {
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
        } else {
          Toast.warning(resp.message, Swal);
        }
      });
    },

    getSubprojectSelfRemainBudget(subprojectId) {
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
      if (flag === 'approve' || flag === 'reject') {
        if (1 !== 1
            || !Validator.require(_this.dialogInfo.code, "申请单号", Swal)
            || !Validator.require(_this.dialogInfo.approveDescription, "审批意见", Swal)
            || (flag === 'approve' && !Validator.require(_this.dialogInfo.capitalSource, "资金来源", Swal))

            || !Validator.length(_this.dialogInfo.approveDescription, "审批意见", 2, 300, Swal)
        ) {
          return;
        }
      }

      let priceFlag = true;
      if (flag === 'approve' && Tool.isNotEmpty_ES5(_this.materialList)) {
        _this.materialList.forEach(item => {
          if (1 !== 1
              || !Validator.require(item.preTaxSumPrice, "物料价格", Swal)
          ) {
            priceFlag = false;
            Toast.warning("请正确填写物料价格!", Swal);
          }
        })
      }

      if (!priceFlag) {
        return;
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

          if (priceFlag) {
            _this.dialogInfo.materialList = _this.materialList;
            if (flag === 'approve') {
              _this.dialogInfo.approveStatus = APPLICATION_STATUS.APPROVED.key;
            } else if (flag === 'reject') {
              _this.dialogInfo.approveStatus = APPLICATION_STATUS.REJECT.key;
            } else if (flag === 'submit_approve') {
              _this.dialogInfo.approveStatus = APPLICATION_STATUS.COMPLETE.key;
            } else if (flag === 'submit_reject') {
              _this.dialogInfo.approveStatus = APPLICATION_STATUS.SUPPLEMENT.key;
            }
            _this.dialogInfo.accountCost = _this.sumCharge;  //记账费用
            _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/material/approveMaterialApplication', _this.dialogInfo).then((response) => {
              let resp = response.data;
              if (resp.success) {
                _this.close();
                _this.$bus.publish({ areas: ['material', 'character', 'project', 'product', 'develop', 'quality', 'badge'], reason: 'material-approve' });
                Toast.success("审批完成", Swal);
              } else {
                Toast.warning(resp.message, Swal);
              }
            })
          }
        }
      }).catch(_ => {
      });
    },

    close() {
      let _this = this;
      _this.closeDlg();
    },

    downloadFile(file) {
      let _this = this;
      _this.getSignedUrl(file.path, file.suffix);
    },
    getSignedUrl(fileFullName, suffix) {
      let _this = this;
      let formData = new window.FormData();
      formData.append('fileFullName', fileFullName);
      formData.append('customer', _this.loginCustomer.id);
      formData.append('user', _this.loginUser.customerUserId);
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/file/rdms/oss-get-signed-url', formData).then((response) => {
        let resp = response.data;
        Tool.openOssSignedUrl(resp, suffix);
        // FileSaver.saveAs(resp.content, fileName);
      });
    },


    getSummaries(param) {
      let _this = this;
      const {columns, data} = param;
      const sums = [];
      columns.forEach((column, index) => {
        if (index === 0) {
          sums[index] = '总价';
          return;
        }
        if (index === 1 || index === 2 || index === 3 || index === 4 || index === 5 || index === 6) {
          sums[index] = '';
          return;
        }
        const values = data.map(item => Number(item[column.property]));
        if (!values.every(value => isNaN(value))) {
          sums[index] = values.reduce((prev, curr) => {
            const value = Number(curr);
            if (!isNaN(value)) {
              return prev + curr;
            } else {
              return prev;
            }
          }, 0);
          _this.sumCharge = sums[index];
          sums[index] = rmbFormat(sums[index]);
        } else {
          sums[index] = 'N/A';
        }
      });
      return sums;
    }
  },
}
</script>

<style lang="scss" scoped>
@import '../../../public/static/scss/caipd';

.text-box {
  display: inline-block;
  padding-left: 3px;
  padding-right: 15px;
  line-height: 30px;
  //width: 100px;
  background-color: rgba(204, 204, 204, 0.20);
}

.dialog-width {
  width: 100%;
}

.red-color {
  color: red;
}
</style>
