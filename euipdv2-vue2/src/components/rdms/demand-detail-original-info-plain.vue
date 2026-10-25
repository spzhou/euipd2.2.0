<!--
 * Author: 周朔鹏
 * Date: 2024-11-03
 * Email: 391902958@qq.com
 * Website: https://www.euipd.com
-->

<template>
  <div class="original-container" >
    <JobitemOwnInfoPlain
        :key="jobItem.id"
        :jobitem-id="jobItem.id"
        :job-type="jobItem.type"
        :project-type="jobItem.projectType"
        :index="index"
        :entrance="entrance"
    ></JobitemOwnInfoPlain>
    <div v-for="(demand, index) in demandList" :key="index">
      <div class="hr hr-18 dotted hr-double"></div>
      <div style="display: flex; flex-direction: row; line-height: 30px; font-weight: bold;">
        <div style="width: 60px; text-align: right;">
          需求名称:
        </div>
        <div style="line-height: 30px; margin-left: 6px;">
          {{ demand.demandName }}
        </div>
      </div>
      <div style="display: flex; flex-direction: row; line-height: 30px; font-weight: bold;">
        <div style="width: 60px; text-align: right;">
          项目名称:
        </div>
        <div style="line-height: 30px;margin-left: 6px;">
          {{ demand.preProjectName }}
        </div>
      </div>
      <div style="display: flex; flex-direction: row; line-height: 30px; font-weight: bold;">
        <div style="width: 60px; text-align: right;">
          产品经理:
        </div>
        <div style="line-height: 30px;margin-left: 6px;">
          {{ demand.productManagerName }}
        </div>
      </div>
      <div class="hr hr-6 dotted"></div>
      <div>
        <div style="text-align: left; line-height: 30px; font-weight: bold;">
          需求描述:
        </div>
        <div style="margin-left: 15px; line-height: 21px;"
             v-rich-html="demand.demandDescription">
        </div>
      </div>
      <div class="hr hr-6 dotted"></div>
      <div>
        <div style="text-align: left; line-height: 30px; font-weight: bold;">
          生效条件/前提条件:
        </div>
        <div style="margin-left: 15px; line-height: 21px;"
             v-rich-html="demand.workCondition">
        </div>
      </div>
      <div class="hr hr-6 dotted"></div>

      <div style="line-height: 30px;"  v-show="demand.resFiles">
        <div style="font-weight: bold;">
          附件列表:
        </div>
        <div v-for="(item, i ) in demand.resFiles" :key="item.id+'-'+i" style="display: flex; flex-direction: row; align-items: center; margin-left: 16px;">
          <div v-if="item">
            {{ item.name }}
          </div>
          <div v-if="item && item.authStatus">
            <el-button type="text" style="margin-left: 25px;" @click="downloadFile(item)">下载</el-button>
          </div>
        </div>
        <div class="hr hr-6 dotted"></div>
      </div>
      <div style="display: flex; flex-direction: row; line-height: 30px; font-weight: bold;">
        <div style="width: 65px; text-align: left;">
          编制人:
        </div>
        <div style="line-height: 30px; margin-left: 6px;">
          {{ demand.writerName }}
        </div>
      </div>
      <div style="display: flex; flex-direction: row; line-height: 30px; font-weight: bold;">
        <div style="width: 65px; text-align: left;">
          编制时间:
        </div>
        <div style="line-height: 30px; margin-left: 6px;">
          {{ demand.createTime | dateFormat }}
        </div>
      </div>
      <div style="display: flex; flex-direction: row; line-height: 30px; font-weight: bold;">
        <div style="width: 65px; text-align: left;">
          需求方:
        </div>
        <div style="line-height: 30px; margin-left: 6px;">
          {{ demand.demandCustomerName }}
        </div>
      </div>
      <div style="display: flex; flex-direction: row; line-height: 30px; font-weight: bold;">
        <div style="width: 65px; text-align: left;">
          需求提出:
        </div>
        <div style="line-height: 30px; margin-left: 6px;">
          {{ demand.confirmPersonName }}
        </div>
      </div>
      <div style="display: flex; flex-direction: row; line-height: 30px; font-weight: bold;">
        <div style="width: 65px; text-align: left;">
          联系方式:
        </div>
        <div style="line-height: 30px; margin-left: 6px;">
          {{ demand.confirmContactTel }}
        </div>
      </div>
      <div class="hr hr-18 dotted hr-double"></div>
    </div>
  </div>
</template>

<script>
import FileRdms from "@/components/rdms/file-rdms";
import DemandDetailDialog from "@/components/rdms/demand-detail-dlg";
import Swal from "sweetalert2";
import FileSaver from 'file-saver';
import JobitemOwnInfoPlain from "@/components/std/jobitem-own-info-plain.vue";

export default {
  components: {
    JobitemOwnInfoPlain,
    FileRdms,
    DemandDetailDialog,
  },
  name: "demand-detail-original-info-plain",

  props: {
    jobitemId: {
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
      loginUser: {},
      loginCustomer: {},

      jobItem: {},
      demandList: [],

      character: {},
      dialogTitle: '原始需求',
      demandItem: {},

      fileItems: [],
      resFile: {},
      resFiles: [],
      resFileIds: [],

    }
  },

  computed: {},

  mounted: function () {
    let _this = this;
    _this.getLoginUser();
    _this.getJobitemDetailInfo(_this.jobitemId);

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

    getJobitemDetailInfo(jobitemId){
      let _this = this;
      let formData = new window.FormData();
      formData.append('jobitemId', jobitemId);
      formData.append('loginUserId', _this.loginUser.customerUserId);
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/job-items/getJobitemDetailInfo', formData).then((response) => {
        let resp = response.data;
        if (resp.success) {
          _this.jobItem = resp.content;
          if(_this.jobItem.type === 'DEMAND'){
            _this.demandList = _this.jobItem.demandDtoList;
          }
        }else{
          Toast.warning(resp.message, Swal);
        }
      });
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
