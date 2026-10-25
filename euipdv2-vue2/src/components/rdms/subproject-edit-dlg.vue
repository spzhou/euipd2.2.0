<!--
 * Author: 周朔鹏
 * Date: 2024-12-12
 * Email: 391902958@qq.com
 * Website: https://www.euipd.com
-->

<template>
  <div>
    <el-row>
      <el-col :offset="1" :span="21">
        <el-form>
          <el-form-item label="名称" :label-width="formLabelWidth" required>
            <el-col :span="8">
              <el-input v-model="projectDlgInfo.label" autocomplete="off"></el-input>
            </el-col>
          </el-form-item>
          <el-form-item label="子项目概要" :label-width="formLabelWidth" required>
            <el-input type="textarea" rows="3" v-model="projectDlgInfo.introduce" autocomplete="off"></el-input>
<!--            <textarea rows="3" type="text" style="width: 100%;" placeholder="子项目概要"
                      v-model="projectDlgInfo.introduce" autocomplete="off"></textarea>-->
          </el-form-item>
          <el-form-item
              v-if="projectDlgInfo.rdType !== RD_TYPE.MODULE.key"
              label="激励政策" :label-width="formLabelWidth">
            <el-input type="textarea" :rows="3" v-model="projectDlgInfo.incentivePolicy" autocomplete="off"
                      placeholder="激励政策"></el-input>
<!--            <textarea rows="2" type="text" style="width: 100%;" placeholder="激励政策"
                      v-model="projectDlgInfo.incentivePolicy" autocomplete="off"></textarea>-->
          </el-form-item>
          <el-form-item label="项目经理" :label-width="formLabelWidth" required>
            <el-col :span="6">
              <el-select v-model="projectDlgInfo.projectManagerId" filterable placeholder="项目经理" style="width: 100%;">
                <el-option v-for="o in projectManagers" :key="o.id" v-bind:value="o.id" :label="o.trueName"></el-option>
              </el-select>
            </el-col>
          </el-form-item>
          <el-form-item label="测试主管" :label-width="formLabelWidth" required>
            <el-col :span="6">
              <el-select v-model="projectDlgInfo.testManagerId" filterable placeholder="测试主管" style="width: 100%;">
                <el-option v-for="o in allWorkers" :key="o.id" v-bind:value="o.id" :label="o.trueName"></el-option>
              </el-select>
            </el-col>
          </el-form-item>
          <el-form-item label="系统工程师" :label-width="formLabelWidth" required>
            <el-col :span="6">
              <el-select v-model="projectDlgInfo.systemManagerId" filterable placeholder="系统工程师" style="width: 100%;">
                <el-option v-for="o in systemManagers" :key="o.id" v-bind:value="o.id" :label="o.trueName"></el-option>
              </el-select>
            </el-col>
          </el-form-item>
        </el-form>
      </el-col>
    </el-row>
    <div class="clearfix form-actions">
      <div style="display: flex; justify-content: center;">
        <div style="display: inline-block; margin-right: 20px;">
          <el-button type="primary" @click="saveProjectInfo(projectDlgInfo)">保 存</el-button>
        </div>
        <el-button @click="closeDialog">取 消</el-button>
      </div>
    </div>
  </div>
</template>

<script>
import Swal from "sweetalert2";

export default {
  components: {},
  name: "subproject-edit-dlg",

  props: {
    subProjectId: {
      default: null,
    },
    projectType: {
      default: null,
    },
    closeDialog: {
      type: Function,
      default: null,
    }
  },

  data: function () {
    return {
      allWorkers: [],

      originTeam: {},

      PROJECT_TYPE: PROJECT_TYPE,
      RD_TYPE: RD_TYPE,

      formLabelWidth: '120px',
      subProject: {}, //子项目信息
      projectDlgInfo: {}, //用于对话框的subProject变量
      projectManagers: [],
      systemManagers: [],
    }
  },

  mounted: function () {
    let _this = this;
    _this.getLoginUser();

    if (Tool.isNotEmpty(_this.subProjectId)) {
      _this.getSubProjectInfo(_this.subProjectId);
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
      _this.listProjectManager(_this.loginCustomer.id);
      _this.listSystemManager(_this.loginCustomer.id);
    },

    getSubProjectInfo(subProjectId) {
      // debugger
      let _this = this;
      let formData = new window.FormData();
      formData.append('subProjectId', subProjectId);
      formData.append('loginUserId', _this.loginUser.customerUserId);
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/sub-project/getSubProjectInfo', formData).then((response)=>{
        let resp = response.data;
        if (resp.success) {
          _this.subProject = resp.content;
          //将原始的团队保存下来
          _this.originTeam.originProjectManagerId = _this.subProject.projectManagerId;
          _this.originTeam.originProductManagerId = _this.subProject.productManagerId;
          _this.originTeam.originTestManagerId = _this.subProject.testManagerId;
          _this.originTeam.originIpmtId = _this.subProject.supervise;

          {
            _this.projectDlgInfo = _this.subProject;
          }
          _this.allWorkers = _this.subProject.workerList;
        } else {
          Toast.warning(resp.message, Swal);
        }
      })
    },

    saveProjectInfo(projectDlgInfo) {
      let _this = this;
      if (!Validator.require(projectDlgInfo.systemManagerId, "系统工程师", Swal)) {
        return;
      }
      const projectInfo = {
        id: projectDlgInfo.id,
        customerId: _this.loginCustomer.id,
        projectId: projectDlgInfo.projectId,
        label: projectDlgInfo.label,
        parent: projectDlgInfo.parent,
        deep: projectDlgInfo.deep,
        introduce: projectDlgInfo.introduce,
        projectManagerId: projectDlgInfo.projectManagerId,
        testManagerId: projectDlgInfo.testManagerId,
        systemManagerId: projectDlgInfo.systemManagerId,
        children: [],
        incentivePolicy: projectDlgInfo.incentivePolicy,
      };

      //将原始的团队保存下来, 以供后台程序对比
      projectInfo.originProjectManagerId = _this.originTeam.originProjectManagerId;
      projectInfo.originProductManagerId = _this.originTeam.originProductManagerId;
      projectInfo.originTestManagerId = _this.originTeam.originTestManagerId;
      projectInfo.originIpmtId = _this.originTeam.originIpmtId;

      Swal.fire({
        title: '确认',
        text: "是否确认修改项目信息?",
        icon: 'question',
        showCancelButton: true,
        confirmButtonColor: '#409EFF',
        cancelButtonColor: '#909399',
        confirmButtonText: '确认!',
        cancelButtonText: '取消!'
      }).then((result) => {
        if (result.isConfirmed) {
          _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/sub-project/editSubProjectInfo', projectInfo).then((response) => {
            let resp = response.data;
            if (resp.success) {
              Toast.success("保存成功", Swal);
              _this.close();
            } else {
              Toast.warning(resp.message, Swal);
            }
          });
        }
      }).catch(_ => {
      });
    },

    listProjectManager(customerId) {
      let _this = this;
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/project-manager/getProjectManagerList/' + customerId ).then((response)=>{
        let resp = response.data;
        if (resp.success) {
          _this.projectManagers = resp.content;
        } else {
          Toast.warning(resp.message,Swal);
        }
      })
    },

    listSystemManager(customerId) {
      let _this = this;
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/system-manager/listSystemManagerByCustomerId/' + customerId).then((response) => {
        let resp = response.data;
        if (resp.success) {
          _this.systemManagers = resp.content || [];
        } else {
          Toast.warning(resp.message, Swal);
        }
      })
    },

    close(){
      let _this = this;
      _this.closeDialog();
    }
  }
}
</script>

<style lang="scss" scoped>

</style>
