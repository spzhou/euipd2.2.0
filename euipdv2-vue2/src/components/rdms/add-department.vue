<!--
 * Author: 周朔鹏
 * Date: 2025-03-24
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
                label="上级部门:">
              <el-input style="width: 300px;"
                        :value="currentDepartment?.label"
                        disabled
              ></el-input>
            </el-form-item>
            <el-form-item
                label="部门名称:"
                required>
              <el-input style="width: 300px;"
                        v-model="department.departName"
                        placeholder="填写部门名称"
              ></el-input>
            </el-form-item>
            <el-form-item
                required
                label="部门描述:">
              <el-input style="width: 700px;"
                        type="textarea"
                        rows="2"
                        placeholder="请填写部门描述"
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
            <el-form-item label="部门成员:">
            </el-form-item>
            <div>
              <el-transfer style="margin-left: 72px; margin-bottom: 15px;"
                           filterable
                           filter-placeholder="请输入姓名"
                           v-model="memberIdList"
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
            <button v-on:click="addDepartment" class="ca-btn-primary" type="button">
              添加
            </button>
            <button v-on:click="close" class="ca-btn-primary" type="button">
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

export default {
  components:{
  },
  name: 'add-department',
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
      department: {},
      workerList: [],
      memberIdList: [],
      titles: ['可选人员','部门成员'],
    };
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
    },

    addDepartment() {
      let _this = this;
      if (1 !== 1
          || !Validator.require(_this.department.departName, "部门名称",Swal)
          || !Validator.require(_this.department.managerId, "部门负责人",Swal)
          || !Validator.require(_this.department.departDescription, "部门描述",Swal)

          || !Validator.length(_this.department.departName, "部门名称", 2, 20,Swal)
          || !Validator.length(_this.department.departDescription, "部门描述", 2, 100,Swal)
      ) {
        return;
      }

      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/department/save', {
        parent: _this.currentDepartment.id,
        departName: _this.department.departName,
        customerId: _this.loginCustomer.id,
        managerId: _this.department.managerId,
        departDescription: _this.department.departDescription,
        memberIdList: _this.memberIdList,
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

    listAllWorker() {
      let _this = this;
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/worker/listAllWorkers/'+ _this.loginCustomer.id).then((response)=>{
        let resp = response.data;
        if (resp.success) {
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

    close(){
      let _this = this;
      _this.closeFunction();
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
