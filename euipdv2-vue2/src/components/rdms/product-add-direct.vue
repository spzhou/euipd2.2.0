<!--
 * Author: 周朔鹏
 * Date: 2024-12-19
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
                label="产品名称:"
                required>
              <el-input style="width: 300px;"
                        v-model="setupProject.projectName"
              ></el-input>
            </el-form-item>
            <el-form-item
                label="产品代号:"
                required>
              <el-input style="width: 300px;"
                        v-model="setupProject.projectCode"
                        :placeholder="getDataMonth"
              ></el-input>
            </el-form-item>
            <el-form-item
                label="既有版本:">
              <div>
                <el-input style="width: 300px;"
                          v-model="setupProject.devVersion"
                          placeholder="01.01.0001"
                ></el-input>
              </div>
            </el-form-item>
            <el-form-item
                required
                label="产品简介:">
              <el-input style="width: 700px;"
                        type="textarea"
                        :autosize="{ minRows: 5, maxRows: 15}"
                        placeholder="请填写产品简介"
                        v-model="setupProject.projectIntroduce"
              ></el-input>
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
                label="产品经理:">
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
                label="项目经理:">
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
            <el-form-item
                required
                label="核心成员:">
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
            <el-form-item
                required
                label="激励政策:">
              <el-input style="width: 700px;"
                        type="textarea"
                        :autosize="{ minRows: 3, maxRows: 10}"
                        placeholder="请填写激励政策"
                        v-model="setupProject.incentivePolicy"
              ></el-input>
            </el-form-item>
            <el-form-item required
                           label="发布时间:">
              <el-date-picker
                  v-model="setupProject.releaseTime"
                  type="date"
                  placeholder="选择日期">
              </el-date-picker>
            </el-form-item>
          </el-form>
        </div>
        <div class="hr hr-18 hr-dotted"></div>
      </div>
      <div>
        <div class="clearfix form-actions">
          <div style="display: flex; justify-content: center;">
            <button v-on:click="saveProduct" class="ca-btn-primary" type="button">
              添加
            </button>
            <button v-on:click="deleteProject(setupProject)" class="ca-btn-primary" type="button">
              删除
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
import {fixed2fractional} from "@/filter/filter";
import DevVersionUtil from "@/utils/dev-version-util";

export default {
  components:{
  },

  name: 'product-add-direct',
  props:{
    closeDlg: {
      type: Function,
      default: null
    },
  },

  data() {
    return {
      loginUser: {},
      loginCustomer: {},

      value: [],
      workers: [],
      workerList: [],
      titles: ['可选人员','核心成员'],

      supervises: [],
      productManagers: [],
      projectManagers: [],

      setupProject: {},

      originalReleaseTime: '',
      releaseTimeShowFlag: false,

      role_IPMT: false,

      pickerOptions: {
        disabledDate(v) {
          return v.getTime() < new Date().getTime() - 86400000;//  - 86400000是否包括当天
        }
      },
    };
  },

  computed:{
    getDataMonth(){
      let date = new Date();
      let year = date.getFullYear();
      let month = date.getMonth()+1;
      return '例如:PJ-' + year + '-' + month + '-001';
    },
  },
  mounted: function() {
    let _this =this;

    _this.getLoginUser();
    _this.listIpmtByCustomerId();
    _this.listProductManagerByCustomerId();
    _this.listProjectManagerByCustomerId();
    _this.listAllWorker();

  },

  beforeDestroy() {
    let _this = this;
  },

  methods:{
    fixed2fractional,
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

    saveProduct() {
      let _this = this;
      _this.setupProject.keyMemberList = _this.value;

      // 保存校验
      if (1 !== 1
          || !Validator.require(_this.setupProject.projectName, "项目名称",Swal)
          || !Validator.require(_this.setupProject.projectCode, "项目代号",Swal)
          || !Validator.require(_this.setupProject.devVersion, "开发版本",Swal)
          || !Validator.require(_this.setupProject.projectIntroduce, "项目简介",Swal)
          || !Validator.require(_this.setupProject.supervise, "项目监管人",Swal)
          || !Validator.require(_this.setupProject.productManagerId, "产品经理",Swal)
          || !Validator.require(_this.setupProject.projectManagerId, "项目经理",Swal)
          || !Validator.require(_this.setupProject.testManagerId, "测试主管",Swal)
          || !Validator.require(_this.setupProject.keyMemberList, "关键成员",Swal)
          || !Validator.require(_this.setupProject.incentivePolicy, "激励政策",Swal)
          || !Validator.require(_this.setupProject.releaseTime, "发布时间",Swal)

          || !Validator.length(_this.setupProject.projectName, "项目名称", 2, 50,Swal)
          || !Validator.length(_this.setupProject.projectCode, "项目代号", 2, 50,Swal)
          || !Validator.length(_this.setupProject.projectIntroduce, "项目简介", 5, 500,Swal)
          || !Validator.length(_this.setupProject.supervise, "项目监管人", 8, 8,Swal)
          || !Validator.length(_this.setupProject.productManagerId, "产品经理", 8, 8,Swal)
          || !Validator.length(_this.setupProject.projectManagerId, "项目经理", 8, 8,Swal)
          || !DevVersionUtil.requireValid(_this.setupProject.devVersion, "开发版本", Swal)
      ) {
        return;
      }
      _this.setupProject.devVersion = String(_this.setupProject.devVersion || '').trim();

      Swal.fire({
        title: '确认',
        text: "请确认添加既有产品!",
        icon: 'question',
        showCancelButton: true,
        confirmButtonColor: '#409EFF',
        cancelButtonColor: '#909399',
        confirmButtonText: '确认!',
        cancelButtonText: '取消!'
      }).then((result) => {
        if (result.isConfirmed) {
          exeApplicationSaveSetupInfo();
        }
      }).catch(_ => {
      });

      function exeApplicationSaveSetupInfo() {
        _this.setupProject.id = Uuid.shortUuid(8);
        _this.setupProject.customerId = _this.loginCustomer.id;
        _this.setupProject.createrId = _this.loginUser.customerUserId;
        _this.status = PROJECT_STATUS.COMPLETE.key;
        _this.rdType = RD_TYPE.PRODUCT.key;   //既有产品

        // 已添加文件权限控制代码
        _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/project/save-product', _this.setupProject).then((response) => {
          let resp = response.data;
          if (resp.success) {
            Toast.success("保存成功", Swal);
            _this.$bus.$emit('refresh-product-list', true);
            _this.close();
          } else {
            Toast.warning(resp.message, Swal);
          }
        })
      }
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

    deleteProject(project) {
      let _this = this;
      let text = `是否确认删除"${ project.projectName }"？`;
      Swal.fire({
        title: '确认？',
        text: text,
        icon: 'warning',
        showCancelButton: true,
        confirmButtonColor: '#409EFF',
        cancelButtonColor: '#909399',
        confirmButtonText: '确认!',
        cancelButtonText: '取消!'
      }).then((result) => {
        if (result.value) {
          _this.$ajax.delete(process.env.VUE_APP_SERVER + '/customer/project/del/'+ project.id).then((response)=>{
            let resp = response.data;
            if (resp.success) {
              _this.$bus.$emit('refresh-setup-list', true);  //刷新申请立项列表
              _this.$bus.$emit('notify-setup-count-flag', true);   //立项管理
              _this.close();
              Toast.success("删除成功!", Swal);
            } else {
              Toast.warning(resp.message, Swal);
            }
          })
        }
      })
    },

    close(){
      let _this = this;
      _this.closeDlg();
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
