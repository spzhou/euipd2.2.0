<!--
 * Author: 周朔鹏
 * Date: 2025-03-01
 * Email: 391902958@qq.com
 * Website: https://www.euipd.com
-->

<template>
  <div class="dialog-container">
    <div style="margin: 0 90px 0 0">
      <el-form label-width="180px">
        <el-form-item label="申请文件名称:">
          {{ fileItem.name }}（第 {{ fileItem.versionNo || 1 }} 版）
        </el-form-item>
        <el-form-item label="申请原因:" required>
          <el-col :span="22">
            <table style="width: 100%;">
              <tr style="width: 100%; border: 1px solid #8f8e8e">
                <rich-editor
                    key="fileItem-desc-create"
                    v-model="reason"
                    mode="simple"
                    placeholder="请输入申请原因"
                    :max-length="2000"
                    :show-count="false"
                />
              </tr>
            </table>
            <div class="prompt-line">{{ ('(字符数: ' + (getReasonDescFontNum) + ' 个字符 备注:包括所有格式字符等, 如果格式字符过多,可以点击"Tx"按钮清理格式字符)') }}</div>
          </el-col>
        </el-form-item>
        <el-form-item label="申请人:">
          <el-col :span="7">
            <div style="width: 220px;">
              {{loginUser.trueName }}
            </div>
          </el-col>
        </el-form-item>
        <el-form-item label="审批人:">
          <el-col :span="7">
            <div style="width: 220px;">
              {{approverInfo.trueName }}
            </div>
          </el-col>
        </el-form-item>
      </el-form>
    </div>

    <div class="clearfix form-actions">
      <div style="display: flex; justify-content: center;">
        <button v-on:click="apply()" class="ca-btn-primary" type="button">
          申 请
        </button>
        <button v-on:click="close()" class="ca-btn-primary" type="button">
          关 闭
        </button>
      </div>
    </div>
  </div>
</template>
<script>
import Swal from "sweetalert2";

import RichEditor from '@/components/rich/rich-editor.vue'
import {Loading} from "element-ui";

//与FILE_APPLICATION_STATUS相同 , 这里直接定义是因为, 未知原因导致 未定义错误
let FILE_APPLY_STATUS = {
  NOTSET:{key:"NOTSET", value:"可申请"},
  APPLICATION:{key:"APPLICATION", value:"申请中"},
  REJECT:{key:"REJECT", value:"拒绝"},
  APPROVED:{key:"APPROVED", value:"已批准"},
  COMPLETE:{key:"COMPLETE", value:"结束"},
  CANCEL:{key:"CANCEL", value:"取消"},
  DOWNLOADED:{key:"DOWNLOADED", value:"已下载"},
};

export default {
  name: 'file-apply-dlg',

  components: {
    RichEditor,
  },

  props:{
    fileItem: {
      default: null,
    },
    closeFunction: {
      type: Function,
      default: null
    },
  },

  data() {
    return {
      loginUser: {},
      loginCustomer: {},
      approverInfo: {},
      reason: '',


    };
  },

  computed: {
    getReasonDescFontNum(){
      if(this.reason){
        return  this.reason.length;
      }
      return 0;
    },
  },

  mounted: function() {
    let _this =this;
    _this.getLoginUser();
    _this.getApproverInfo();
  },
  methods:{
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

    getApproverInfo() {
      let _this = this;
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/file/rdms/getApproverInfo/'+ _this.fileItem.id).then((response) => {
        let resp = response.data;
        if (resp.success) {
          _this.approverInfo = resp.content;
        } else {
          Toast.warning(resp.message, Swal);
        }
      })
    },

    /**
     * 保存需求
     */
    apply() {
      let _this = this;
      // 保存校验
      if (1 !== 1
          || !Validator.require(_this.fileItem.id, "文件Id",Swal)
          || !Validator.require(_this.reason, "申请原因",Swal)

          || !Validator.length(_this.reason, "申请原因", 4, 2000,Swal)
      ) {
        return;
      }
      let fileApply = {};
      fileApply.fileId = _this.fileItem.id;
      fileApply.customerId = _this.loginCustomer.id;
      fileApply.proposerId = _this.loginUser.customerUserId;
      fileApply.reason = _this.reason;
      fileApply.type = FILE_APPLY_TYPE.APPLY.key;
      fileApply.applicationStatus = FILE_APPLY_STATUS.APPLICATION.key;
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/file/rdms/applyFile', fileApply).then((response)=>{
        let resp = response.data;
        if (resp.success) {
          Toast.success("保存成功", Swal);
          _this.close();
          _this.$bus.$emit('refresh-file-search-list', true);
        } else {
          Toast.warning(resp.message, Swal);
        }
      })
    },

    close(){
      let _this = this;
      _this.closeFunction();
    },

  }
};
</script>

<style lang="scss" scoped>
@import './../../../public/static/scss/caipd.scss';

</style>

