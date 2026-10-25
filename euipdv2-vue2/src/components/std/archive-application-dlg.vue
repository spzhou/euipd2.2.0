<!--
 * Author: 周朔鹏
 * Date: 2025-03-01
 * Email: 391902958@qq.com
 * Website: https://www.euipd.com
-->

<template>
  <div>
    <div class="dialog-container">
      <div class="block" style="margin: 0 110px 10px;">
        <div style="margin: 8px 0; font-weight: 500;">
          参考文档列表:
        </div>
        <el-tree style="background-color: rgba(187,215,248,0);"
                 ref="docsTree"
                 :data="data"
                 :indent="14"
                 :show-checkbox="true"
                 node-key="id"
                 :default-expand-all="false"
                 :highlight-current="true"
                 :check-on-click-node="true"
                 :expand-on-click-node="false"
                 :accordion=false
                 @node-click="nodeClick">
              <span class="custom-tree-node" slot-scope="{ node, data }">
                <span>{{ ((data.label)) }}</span>
                <span v-if="data.status === 'file'" style="margin-left: 20px;">{{
                    data.obj.operatorName
                  }}
                </span>
                <span v-if="data.status === 'file'" style="margin-left: 20px;">{{
                    data.obj.createTime | dateFormat
                  }}
                </span>

              </span>
        </el-tree>
      </div>
      <el-form label-width="180px">
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
              {{jobItem.productManagerName }}
            </div>
          </el-col>
        </el-form-item>
      </el-form>

      <div class="clearfix form-actions">
        <div style="display: flex; justify-content: center;">
          <button type="button" v-on:click="apply()" class="ca-btn-primary">申 请</button>
          <button type="button" v-on:click="close()" class="ca-btn-primary">取 消</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import Swal from "sweetalert2";
import RichEditor from '@/components/rich/rich-editor.vue'

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
  components: {
    RichEditor,
  },
  name: "archive-application-dlg",

  props: {
    jobitemId: {
      default: null
    },
    closeDlg: {
      type: Function,
      default: null
    },
  },

  data() {
    return {
      loginUser: {},
      loginCustomer: {},
      jobItem: {},
      docFileTree: [],

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
    //使用计算属性实现传入数据的动态响应
    data: function(){
      return  this.docFileTree;
    },
  },

  mounted: function () {
    let _this = this;
    _this.getLoginUser();
    if(Tool.isNotEmpty(_this.jobitemId)){
      _this.getJobitemRecordDetailInfo(_this.jobitemId);
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

    apply() {
      let _this = this;
      // console.log(this.$refs.docsTree.getCheckedNodes());
      let nodeList = _this.$refs.docsTree.getCheckedNodes();
      let fileList = [];
      nodeList.forEach(item => {
        if(item.status === 'file'){
          fileList.push(item);
        }
      })

      let fileIds = fileList.map(item => item.id);
      // console.log("fileIds", fileIds);


      // 保存校验
      if (1 !== 1
          || !Validator.require(fileIds, "文件Id",Swal)
          || !Validator.require(_this.reason, "申请原因",Swal)

          || !Validator.length(_this.reason, "申请原因", 4, 2000,Swal)
      ) {
        return;
      }
      let fileApply = {};
      fileApply.fileIdList = fileIds;
      fileApply.customerId = _this.loginCustomer.id;
      fileApply.proposerId = _this.loginUser.customerUserId;
      fileApply.approverId = _this.jobItem.productManagerId;  //文档调阅 产品经理审批
      fileApply.reason = _this.reason;
      fileApply.type = FILE_APPLY_TYPE.CONSULT.key;
      fileApply.applicationStatus = FILE_APPLY_STATUS.APPLICATION.key;
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/file/rdms/applyFileList', fileApply).then((response)=>{
        let resp = response.data;
        if (resp.success) {
          Toast.success("保存成功", Swal);
          _this.close();
        } else {
          Toast.warning(resp.message, Swal);
        }
      })
    },

    nodeClick(data){
      let _this = this;

    },

    getJobitemRecordDetailInfo(jobitemId){
      let _this = this;
      let formData = new window.FormData();
      formData.append('jobitemId', jobitemId);
      formData.append('loginUserId', _this.loginUser.customerUserId);
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/job-items/getJobitemRecordDetailInfo', formData).then((response) => {
        let resp = response.data;
        if (resp.success) {
          _this.jobItem = resp.content;
          if(_this.jobItem.type === JOB_ITEM_TYPE.TASK_SUBP.key){
            _this.getArchiveTreeBySubprojectId(_this.jobItem.subprojectId);
          }else{
            _this.getDocTreeByCharacterId(_this.jobItem.characterId);
          }
        }else{
          Toast.warning(resp.message, Swal);
        }
      });
    },

    getDocTreeByCharacterId(characterId) {
      let _this = this;
      let formData = new window.FormData();
      formData.append('characterId', characterId);
      formData.append('loginUserId', _this.loginUser.customerUserId);
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/character/getDocTreeByCharacterId', formData).then((response) => {
        let resp = response.data;
        if (resp.success) {
          _this.docFileTree = resp.content;
        }else{
          Toast.warning(resp.message, Swal);
        }
      });
    },

    getArchiveTreeBySubprojectId(subprojectId) {
      let _this = this;
      let formData = new window.FormData();
      formData.append('subprojectId', subprojectId);
      formData.append('loginUserId', _this.loginUser.customerUserId);
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/file/rdms/getArchiveTreeBySubprojectId', formData).then((response) => {
        let resp = response.data;
        if (resp.success) {
          _this.docFileTree = resp.content;
        } else {
          Toast.warning(resp.message, Swal);
        }
      })
    },

    close(){
      let _this = this;
      _this.closeDlg();
    },

  },
}
</script>

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
