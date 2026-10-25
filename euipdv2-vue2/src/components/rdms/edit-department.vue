<!--
 * Author: 周朔鹏
 * Date: 2025-04-05
 * Email: 391902958@qq.com
 * Website: https://www.euipd.com
-->

<template>
  <div style="margin-top: -20px;">
    <div style="height: 100%;">
      <div class="a4-paper-big-plain">
        <div class="ca-form">
          <el-form label-width="150px" size="small">
            <el-form-item
                required
                label="上级部门:">
              <el-select v-model="department.parent" filterable placeholder="请选择" :disabled="isFixed">
                <el-option
                    v-for="item in parentOptions"
                    :key="item.id"
                    :label="item.departName"
                    :value="item.id">
                </el-option>
              </el-select>
            </el-form-item>
            <el-form-item
                required
                label="部门名称:">
              <el-input style="width: 300px;"
                        v-model="department.departName"
                        :disabled="isFixed"
                        placeholder="请填写部门名称"
              ></el-input>
              <span v-if="isFixed" class="fixed-tip">固定部门，名称与上级部门不可修改</span>
            </el-form-item>

            <el-form-item
                required
                label="部门描述:">
              <el-input style="width: 700px;"
                        type="textarea"
                        rows="2"
                        placeholder="请填写部门简介"
                        v-model="department.departDescription"
              ></el-input>
            </el-form-item>
            <el-form-item
                required
                label="部门负责人:">
              <el-select v-model="department.managerId" filterable placeholder="请选择">
                <el-option
                    v-for="item in workerList"
                    :key="item.key"
                    :label="item.label"
                    :value="item.key">
                </el-option>
              </el-select>
            </el-form-item>
            <el-form-item
                required
                label="部门成员:">
            </el-form-item>
            <div>
              <el-transfer style="margin-left: 72px; margin-bottom: 15px;"
                           ref="character-setup"
                           filterable
                           filter-placeholder="请输入姓名"
                           v-model="value"
                           :data="workerList"
                           :titles="titles">
              </el-transfer>
            </div>
          </el-form>
        </div>
      </div>
      <div>
        <div class="clearfix form-actions">
          <div style="display: flex; justify-content: center;">
            <button v-on:click="editDepartment" class="ca-btn-primary" type="button">
              保存
            </button>
            <button v-on:click="close" class="ca-btn-primary" type="button">
              关闭
            </button>
            <button v-if="(!value || value.length === 0) && !currentDepartment.children && !isFixed" v-on:click="del" class="ca-btn-primary" type="button">
              删除
            </button>

          </div>
        </div>
      </div>
    </div>

  </div>
</template>
<script>
import Swal from "sweetalert2";
import { Loading } from 'element-ui'

export default {
  components:{
  },

  name: 'edit-department',
  props:{
    currentDepartment: {
      default: null,
    },
    closeFunction: {
      type: Function,
      default: null
    },
    refreshList: {
      type: Function,
      default: null
    },
  },

  data() {
    return {
      loginUser: {},
      loginCustomer: {},
      department: {
        parent: this.currentDepartment.parent,
        managerId: this.currentDepartment.managerId,
        departName: this.currentDepartment.label,
        departDescription: this.currentDepartment.description,
        fixed: this.currentDepartment.fixed,
        name: this.currentDepartment.name,
      },
      departmentList: [],
      workerList: [],
      value: this.currentDepartment.memberIdList? this.currentDepartment.memberIdList:[],
      titles: ['可选人员','部门成员'],
    };
  },

  computed:{
    isFixed() {
      return this.department.fixed === 1 || this.department.fixed === true;
    },
    parentOptions() {
      return this.departmentList.filter(d => d.id !== this.currentDepartment.id);
    },
  },
  mounted: function() {
    let _this =this;
    _this.getLoginUser();

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
      _this.listAllWorker();
      _this.listDepartments();
    },

    listAllWorker() {
      let _this = this;
      let loadingInstance = Loading.service({ fullscreen: true });
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/worker/listAllWorkers/'+ _this.loginCustomer.id).then((response)=>{
        let resp = response.data;
        if (resp.success) {
          loadingInstance.close();
          let workers = resp.content;
          workers.forEach((worker) => {
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

    listDepartments() {
      let _this = this;
      let loadingInstance = Loading.service({ fullscreen: true });
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/department/listDepartment/' + _this.loginCustomer.id).then((response) => {
        let resp = response.data;
        if (resp.success) {
          loadingInstance.close();
          _this.departmentList = resp.content;
        }
      })
    },

    close(){
      let _this = this;
      _this.closeFunction();
    },

    del() {
      let _this = this;
      Swal.fire({
        title: '确认？',
        text: "删除后不可恢复，确认删除？",
        icon: 'warning',
        showCancelButton: true,
        confirmButtonColor: '#409EFF',
        cancelButtonColor: '#909399',
        confirmButtonText: '确认!',
        cancelButtonText: '取消!'
      }).then((result) => {
        if (result.value) {
          _this.$ajax.delete(process.env.VUE_APP_SERVER + '/customer/department/del/' + _this.currentDepartment.id).then((response)=>{
            let resp = response.data;
            if (resp.success) {
              _this.close();
              _this.refreshList();
              Swal.fire({
                position: 'top-end',
                icon: 'success',
                title: "删除成功",
                showConfirmButton: false,
                timer: 1000
              })
            } else {
              Toast.warning(resp.message, Swal);
            }
          });
        }
      })
    },

    editDepartment() {
      let _this = this;
      if (1 !== 1
          || !Validator.require(_this.department.parent, "上级部门",Swal)
          || !Validator.require(_this.department.departName, "部门名称",Swal)
          || !Validator.require(_this.department.managerId, "部门负责人",Swal)
          || !Validator.require(_this.department.departDescription, "部门描述",Swal)

          || !Validator.length(_this.department.departName, "部门名称", 2, 20,Swal)
          || !Validator.length(_this.department.departDescription, "部门描述", 2, 100,Swal)
      ) {
        return;
      }

      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/department/edit', {
        id: _this.currentDepartment.id,
        parent: _this.department.parent,
        departName: _this.department.departName,
        customerId: _this.loginCustomer.id,
        managerId: _this.department.managerId,
        departDescription: _this.department.departDescription,
        fixed: _this.department.fixed,
        name: _this.department.name,
        memberIdList: _this.value
      }).then((response) => {
        let resp = response.data;
        if (resp.success) {
          _this.close();
          _this.refreshList();
        } else {
          Toast.warning(resp.message, Swal);
        }
      })
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
.fixed-tip {
  color: #909399;
  font-size: 12px;
  margin-left: 10px;
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
