<!--
 * Author: 周朔鹏
 * Date: 2024-11-12
 * Email: 391902958@qq.com
 * Website: https://www.euipd.com
-->

<template>
  <div style="margin-top: -20px;">
    <div style="height: 100%;">
      <div class="paper-big-plain">
        <div class="ca-form">
          <el-form label-width="150px" size="small">
            <el-form-item
                label="项目名称:"
                required>
              <el-input style="width: 300px;"
                        v-model="setupProject.projectName"
              ></el-input>
            </el-form-item>
            <el-form-item
                label="项目代号:"
                required>
              <el-input style="width: 300px;"
                        v-model="setupProject.projectCode"
                        :placeholder="getDataMonth"
              ></el-input>
            </el-form-item>
            <el-form-item
                label="规划开发版本:">
              <div>
                {{ setupProject.devVersion }}
              </div>
            </el-form-item>
            <el-form-item
                required
                label="项目简介:">
              <div style="width: 750px;" v-rich-html="setupProject.projectIntroduce"></div>
            </el-form-item>
            <el-form-item
                required
                label="监管委员:">
              <el-select v-model="setupProject.supervise" filterable placeholder="请选择">
                <el-option
                    v-for="item in supervises"
                    :key="item.id"
                    :label="item.trueName"
                    :value="item.id">
                </el-option>
              </el-select>
            </el-form-item>
            <el-form-item
                required
                label="产品总监:">
              <el-select v-model="setupProject.productManagerId" filterable placeholder="请选择">
                <el-option
                    v-for="item in productManagers"
                    :key="item.id"
                    :label="item.trueName"
                    :value="item.id">
                </el-option>
              </el-select>
            </el-form-item>
            <el-form-item
                required
                label="研发总监:">
              <el-select v-model="setupProject.projectManagerId" filterable placeholder="请选择">
                <el-option
                    v-for="item in projectManagers"
                    :key="item.id"
                    :label="item.trueName"
                    :value="item.id">
                </el-option>
              </el-select>
            </el-form-item>
            <el-form-item
                required
                label="测试主管:">
              <el-select v-model="setupProject.testManagerId" filterable placeholder="请选择">
                <el-option
                    v-for="item in workerList"
                    :key="item.key"
                    :label="item.label"
                    :value="item.key">
                </el-option>
              </el-select>
            </el-form-item>
          </el-form>
        </div>
      </div>
      <div>
        <div class="clearfix form-actions">
          <div style="display: flex; justify-content: center;">
            <button v-if="!isSetupEdit" v-on:click="setup()" class="ca-btn-primary" type="button">
              立项
            </button>
            <button v-if="canWithdraw" v-on:click="withdrawProject()" class="ca-btn-primary" type="button"
                    :disabled="actionBusy">
              撤回
            </button>
            <button v-on:click="close()" class="ca-btn-primary" type="button">
              关闭
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>
<script>
import Swal from "sweetalert2";
import DevVersionUtil from "@/utils/dev-version-util";

export default {
  components:{
  },

  name: 'project-setup-module',
  props:{
    projectId: {
      default: null,
    },
    index: {
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

      workers: [],
      workerList: [],

      supervises: [],
      productManagers: [],
      projectManagers: [],

      setupProject: {
      },

      timeStamp: '',
      random: 0,
      actionBusy: false,

    };
  },

  computed:{
    getDataMonth(){
      let date = new Date();
      let year = date.getFullYear();
      let month = date.getMonth()+1;
      return '例如:PJ-' + year + '-' + month + '-001';
    },
    isSetupEdit() {
      return !!(this.setupProject && this.setupProject.status === PROJECT_STATUS.SETUP_EDIT.key);
    },
    hasFullAuthority() {
      return typeof RoleAccess !== 'undefined'
        && RoleAccess.hasAny(['UltiMng'], Tool.getLoginUser().roleList || this.loginUser.roleList);
    },
    /** 草稿撤回：仅 SETUP_EDIT，创建人或最终管理人；监管委员走驳回 */
    canWithdraw() {
      const uid = this.loginUser && this.loginUser.customerUserId;
      const isCreator = !!(uid && this.setupProject.createrId === uid);
      const isSupervise = !!(uid && this.setupProject.supervise === uid);
      return this.isSetupEdit && (isCreator || this.hasFullAuthority) && !isSupervise;
    },
  },
  mounted: function() {
    let _this =this;

    _this.getLoginUser();
    _this.listIpmtByCustomerId();
    _this.listProductManagerByCustomerId();
    _this.listProjectManagerByCustomerId();
    _this.listAllWorker();
    _this.getProjectInfo(_this.projectId);
    _this.timeStamp = Date.getTime;
    _this.random = Math.random();

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

    getProjectInfo(projectId){
      let _this = this;
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/project/getProjectInfo/' + projectId).then((response) => {
        let resp = response.data;
        if (resp.success) {
          _this.setupProject = resp.content;
        } else {
          Toast.warning(resp.message, Swal);
        }
      })
    },

    listAllWorker() {
      let _this = this;
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/worker/listAllWorkers/'+ _this.loginCustomer.id).then((response)=>{
        let resp = response.data;
        if (resp.success) {
          _this.workers = resp.content;
          _this.workers.forEach((worker) => {
            if(worker !== null){
              _this.workerList.push({
                label: worker.trueName,
                key: worker.id
              });
            }
          });
        } else {
          Toast.warning(resp.message,Swal);
        }
      })
    },

    /**
     * 立项
     */
    setup() {
      let _this = this;

      // 保存校验
      if (1 !== 1
          || !Validator.require(_this.setupProject.projectName, "项目名称",Swal)
          || !Validator.require(_this.setupProject.projectCode, "项目代号",Swal)
          || !Validator.require(_this.setupProject.devVersion, "开发版本",Swal)
          || !Validator.require(_this.setupProject.projectIntroduce, "项目简介",Swal)
          || !Validator.require(_this.setupProject.supervise, "项目监管人",Swal)
          || !Validator.require(_this.setupProject.productManagerId, "产品总监",Swal)
          || !Validator.require(_this.setupProject.projectManagerId, "研发总监",Swal)
          || !Validator.require(_this.setupProject.testManagerId, "测试主管",Swal)

          || !Validator.length(_this.setupProject.projectName, "项目名称", 2, 50,Swal)
          || !Validator.length(_this.setupProject.projectCode, "项目代号", 2, 50,Swal)
          || !Validator.length(_this.setupProject.projectIntroduce, "项目简介", 5, 500,Swal)
          || !DevVersionUtil.requireValid(_this.setupProject.devVersion, "开发版本", Swal)
      ) {
        return;
      }

      function exeApplicationSetup() {
        _this.setupProject.customerId = _this.loginCustomer.id;
        _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/project/setup-module-project', _this.setupProject).then((response) => {
          let resp = response.data;
          if (resp.success) {
            Toast.success("保存成功", Swal);
            _this.close();

            _this.$bus.$emit('refresh-setup-list', true);  //刷新申请立项列表
            _this.$bus.$emit('notify-setup-count-flag', true);   //立项管理
          } else {
            Toast.warning(resp.message, Swal);
          }
        })
      }

      Swal.fire({
        title: '确认',
        text: "确认立项吗?",
        icon: 'question',
        showCancelButton: true,
        confirmButtonColor: '#409EFF',
        cancelButtonColor: '#909399',
        confirmButtonText: '确认!',
        cancelButtonText: '取消!'
      }).then((result) => {
        if (result.isConfirmed) {
          exeApplicationSetup();
        }
      }).catch(_ => {
      });

    },

    withdrawProject() {
      let _this = this;
      if (_this.actionBusy) {
        return;
      }
      Swal.fire({
        title: '确认？',
        text: `是否确认撤回"${ _this.setupProject.projectName }"？`,
        icon: 'question',
        showCancelButton: true,
        confirmButtonColor: '#409EFF',
        cancelButtonColor: '#909399',
        confirmButtonText: '确认!',
        cancelButtonText: '取消!'
      }).then((result) => {
        if (!result.isConfirmed) {
          return;
        }
        _this.actionBusy = true;
        _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/project/withdrawProject/' + _this.setupProject.id).then((response) => {
          let resp = response.data;
          if (resp.success) {
            _this.$bus.$emit('refresh-setup-list', true);
            _this.$bus.$emit('notify-setup-count-flag', true);
            _this.close();
            Toast.success("撤回成功!", Swal);
          } else {
            Toast.warning(resp.message, Swal);
          }
        }).finally(() => {
          _this.actionBusy = false;
        });
      }).catch(_ => {
      });
    },

    listIpmtByCustomerId() {
      let _this = this;
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/project/listIpmtByCustomerId/'+ _this.loginCustomer.id).then((response)=>{
        let resp = response.data;
        if (resp.success) {
          _this.supervises = resp.content;
        } else {
          Toast.warning(resp.message,Swal);
        }
      })
    },

    listProductManagerByCustomerId() {
      let _this = this;
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/project/listProductManagerByCustomerId/'+ _this.loginCustomer.id).then((response)=>{
        let resp = response.data;
        if (resp.success) {
          _this.productManagers = resp.content;
        } else {
          Toast.warning(resp.message,Swal);
        }
      })
    },

    listProjectManagerByCustomerId() {
      let _this = this;
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/project/listProjectManagerByCustomerId/'+ _this.loginCustomer.id).then((response)=>{
        let resp = response.data;
        if (resp.success) {
          _this.projectManagers = resp.content;
        } else {
          Toast.warning(resp.message,Swal);
        }
      })
    },

    close(){
      let _this = this;
      _this.closeFunction(_this.index);
    },

  }
};
</script>

<style>
.el-input-small .el-input__inner {
  height: 28px !important;
  line-height: 28px !important;
}
</style>

<style lang="scss" scoped>
@import './../../../public/static/scss/caipd.scss';
.ca-form{
  font-size: 14px;
  margin: 40px 30px 0;
}
.paper-height {
  height: calc(100vh - 200px);
}
.paper-big-plain {
  background: white;
  width: 28.5cm;
  display: block;
  margin: 0 auto;
  box-sizing: border-box;
}
</style>
