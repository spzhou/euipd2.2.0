<!--
 * Author: 周朔鹏
 * Date: 2024-12-23
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
              <el-input v-model="subProjectDlg.label" autocomplete="off"></el-input>
            </el-col>
          </el-form-item>
          <el-form-item label="项目编号" :label-width="formLabelWidth" required>
            <el-col :span="8">
              <el-input v-model="subProjectDlg.projectCode" autocomplete="off"></el-input>
            </el-col>
          </el-form-item>
          <el-form-item label="子项目概要" :label-width="formLabelWidth" required>
            <el-input type="textarea" :rows="3" v-model="subProjectDlg.introduce" autocomplete="off"
                      placeholder="子项目概要"></el-input>
          </el-form-item>
          <el-form-item label="子项目经理" :label-width="formLabelWidth" required>
            <el-col :span="6">
              <el-select v-model="subProjectDlg.projectManagerId" filterable placeholder="子项目经理" style="width: 100%;">
                <el-option v-for="o in projectManagers" :key="o.id" v-bind:value="o.id" :label="o.trueName"></el-option>
              </el-select>
            </el-col>
          </el-form-item>
          <el-form-item label="测试主管" :label-width="formLabelWidth" required>
            <el-col :span="6">
              <el-select v-model="subProjectDlg.testManagerId" filterable placeholder="测试主管" style="width: 100%;">
                <el-option v-for="o in allWorkers" :key="o.id" v-bind:value="o.id"
                           :label="o.trueName"></el-option>
              </el-select>
            </el-col>
          </el-form-item>
        </el-form>
      </el-col>
    </el-row>
    <div class="clearfix form-actions">
      <div style="display: flex; justify-content: center;">
        <div style="display: inline-block; margin-right: 20px;">
          <el-button type="primary" @click="saveSubProject(subProjectDlg)">创 建</el-button>
        </div>
        <el-button @click="closeDialog">取 消</el-button>
      </div>
    </div>

  </div>
</template>

<script>
import Swal from "sweetalert2";


export default {
  components: {
  },
  name: "subproject-create-module-dlg",

  props: {
    subProjectId: {  //被创建子项目的父级项目ID
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
      project: {},

      RD_TYPE: RD_TYPE,

      formLabelWidth: '120px',
      projectInfo: {},
      subProject: {},
      subProjectDlg: {
        projectCode: '',
      },
      projectManagers: [],
      appendChild: {},

      budgetEditDlg: false,
      dialogTitle: '',
      budgetData: {},

      random: 0,
    }
  },

  mounted: function () {
    let _this = this;
    _this.getLoginUser();
    _this.random = Math.random();
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
          {
            _this.projectInfo.id = _this.subProject.id;
            _this.projectInfo.projectId = _this.subProject.projectId;
            _this.projectInfo.deep = _this.subProject.deep;
            _this.projectInfo.rdType = _this.subProject.rdType;
          }
          let size = 0;
          if (Tool.isNotEmpty(_this.subProject.projectTreeList) && Tool.isNotEmpty(_this.subProject.projectTreeList[0].children)) {
            size = _this.subProject.projectTreeList[0].children.length;
          }
          _this.subProjectDlg.projectCode = _this.subProject.projectCode + '-' + (size + 1);
          _this.allWorkers = _this.subProject.workerList;

        } else {
          Toast.warning(resp.message, Swal);
        }
      })
    },

    saveSubProject(subProjectDlg) {
      let _this = this;
      // 保存校验
      if (1 !== 1
          || !Validator.require(subProjectDlg.label, "子项目名称", Swal)
          || !Validator.require(subProjectDlg.introduce, "子项目概要", Swal)
          || !Validator.require(subProjectDlg.projectManagerId, "子项目经理", Swal)
          || !Validator.require(subProjectDlg.testManagerId, "测试主管", Swal)
          || !Validator.require(subProjectDlg.projectCode, "项目编号", Swal)

          || !Validator.length(subProjectDlg.label, "子项目名称", 2, 30, Swal)
          || !Validator.length(subProjectDlg.introduce, "子项目概要", 5, 300, Swal)
          || !Validator.length(subProjectDlg.projectCode, "项目编号", 5, 300, Swal)
      ) {
        return;
      }

      Swal.fire({
        title: '确认',
        text: "确认进行子项目立项",
        icon: 'question',
        showCancelButton: true,
        confirmButtonColor: '#409EFF',
        cancelButtonColor: '#909399',
        confirmButtonText: '确认!',
        cancelButtonText: '取消!'
      }).then((result) => {
        if (result.isConfirmed) {
          _this.exeSaveSubproject(this.projectInfo, subProjectDlg);
        }
      }).catch(_ => {
      });
    },

    exeSaveSubproject(projectInfo, subProjectDlg) { //这个data就是当前点击的节点
      let _this = this;
      const newChild = {
        id: '',
        customerId: _this.loginCustomer.id,
        projectId: projectInfo.projectId,
        label: subProjectDlg.label,
        parent: projectInfo.id,
        deep: projectInfo.deep + 1,
        rdType: projectInfo.rdType,
        introduce: subProjectDlg.introduce,
        projectManagerId: subProjectDlg.projectManagerId,
        testManagerId: subProjectDlg.testManagerId,
        children: [],
        creatorId: _this.loginUser.customerUserId,
        projectCode: subProjectDlg.projectCode,
      };

      //将newChild保存到数据库,并返回分配ID的对象
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/sub-project/save-module-subproject', newChild).then((response) => {
        let resp = response.data;
        if (resp.success) {
          _this.appendChild = resp.content;
          Toast.success("保存成功", Swal);
          if (!projectInfo.children) {
            _this.$set(projectInfo, 'children', []);
          }
          projectInfo.children.push(_this.appendChild);

          _this.$bus.$emit('refresh-project-list', true);
          _this.$bus.$emit('refresh-project-subproject', true);
          _this.$bus.$emit('refresh-project-info-outline', true);
          _this.$bus.$emit('refresh-project-subproject-character-list', true);
          _this.close();
        } else {
          Toast.warning(resp.message, Swal);
        }
      });
    },

    listProjectManager(customerId) {
      let _this = this;
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/project-manager/getProjectManagerList/' + customerId).then((response) => {
        let resp = response.data;
        if (resp.success) {
          _this.projectManagers = resp.content;
        } else {
          Toast.warning(resp.message, Swal);
        }
      })
    },

    close() {
      let _this = this;
      _this.closeDialog();
    }
  }
}
</script>

<style>
.el-input-small .el-input__inner {
  height: 28px !important;
  line-height: 28px !important;
}
</style>

<style lang="scss" scoped>

</style>
