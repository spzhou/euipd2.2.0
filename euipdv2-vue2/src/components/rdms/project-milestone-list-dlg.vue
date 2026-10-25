<!--
 * Author: 周朔鹏
 * Date: 2024-12-12
 * Email: 391902958@qq.com
 * Website: https://www.euipd.com
-->

<template>
  <div class="dialog-container">
    <div style="margin: 0 80px">
      <div v-for="(item, index) in milestones" :key="item.id" style="margin-bottom: 10px;">
        <div class="hr hr-8 hr-dotted"></div>
        <div style="display: flex; flex-direction: row; justify-content: space-between; align-items: flex-start;">
          <div>
            <div style="font-size: 15px; margin-bottom: 8px;" :class="[ item.isReviewed > 1 ? 'light-text' : 'weight-text']">
              <span>
                {{ item.reviewTime | shortDateFormat}}
              </span>
              <span style="margin-left: 20px;">
                {{ item.milestoneName}}
              </span>
            </div>
            <div v-rich-html="item.checkOut" style="margin-left: 100px;" :class="[ item.isReviewed > 1 ? 'light-text' : 'normal-text']"></div>
          </div>
          <div v-if="isMainProject">
<!--            <el-button @click="showMilestoneTaskDlg(item.id)">发里程碑任务</el-button>-->
          </div>
        </div>
      </div>
    </div>
    <div style="height: 50px;"></div>
    <div class="clearfix form-actions">
      <div style="display: flex; justify-content: center;">
        <button v-on:click="close()" class="ca-btn-primary" type="button">
          关闭
        </button>
      </div>
    </div>

    <!--下发里程碑任务-->
    <el-dialog
        width="29cm"
        :visible.sync="showMilestoneDlg"
        append-to-body
        :before-close="handleClose"
        :close-on-click-modal=false>
      <div slot="title" style="font-size: 20px;">{{ dialogTitle }}</div>
      <div>
        <MilestoneTaskCreateByProjectIdDlg
            :key="projectId"
            :project-id="projectId"
            :milestone-id="milestoneId"
            :close-dlg = closeDialog
        ></MilestoneTaskCreateByProjectIdDlg>
      </div>
    </el-dialog>
  </div>
</template>
<script>
import Swal from "sweetalert2";
import {shortDateFormat} from "@/filter/filter";
import MilestoneTaskCreateByProjectIdDlg from "@/components/std/milestone-task-create-by-project-id-dlg.vue";

export default {
  name: 'project-milestone-list-dlg',

  components: {
    MilestoneTaskCreateByProjectIdDlg
  },

  props:{
    projectId:{
      default: null,
    },
    isMainProject: {
      default: false,
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
      milestones: [],
      milestoneId: '',

      showMilestoneDlg: false,
      dialogTitle: '',

      PROJECT_TYPE: PROJECT_TYPE,
    };
  },

  computed: {

  },

  mounted: function() {
    let _this =this;
    _this.getLoginUser();
    if(_this.projectId){
      _this.listMilestone(_this.projectId);
    }
  },
  methods:{
    shortDateFormat,
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

    listMilestone(projectId) {
      let _this = this;
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/milestone/listByProjectId/'+ projectId).then((response)=>{
        let resp = response.data;
        if (resp.success) {
          _this.milestones = resp.content;
        } else {
          Toast.warning(resp.message,Swal);
        }
      })
    },

    showMilestoneTaskDlg(milestoneId) {
      let _this = this;
      _this.milestoneId = milestoneId;
      _this.showMilestoneDlg = true;
      _this.dialogTitle = "下发里程碑任务";
    },
    closeDialog() {
      let _this = this;
      _this.showMilestoneDlg = false;
    },
    handleClose(done) {
      let _this = this;
      _this.showMilestoneDlg = false;
      done();
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

.normal-text{
  font-weight: 400;
}

.weight-text{
  font-weight: 500;
}

.light-text{
  font-weight: 200;
}

</style>

