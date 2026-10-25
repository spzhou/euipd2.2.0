<!--
 * Author: 周朔鹏
 * Date: 2024-11-12
 * Email: 391902958@qq.com
 * Website: https://www.euipd.com
-->

<template>
  <div>
    <div  class="dialog-container">
      <div class="ca-form">
        <el-form label-width="150px" size="small">
          <el-form-item
              label="项目名称:"
              required>
            <el-input style="width: 300px;"
                      v-model="setupProject.projectName"
                      placeholder="请输入项目名称"
            ></el-input>
          </el-form-item>
<!--          <el-form-item required
              label="规划开发版本:">
            <el-input type="number" style="width: 300px;"
                      v-model="setupProject.devVersion"
                      placeholder="01.01.0001"
            ></el-input>
          </el-form-item>-->
          <el-form-item
              required
              label="所属产品:">
            <div style="width: 500px;">
              <product-owner-picker ref="productPicker" :value="setupProject"></product-owner-picker>
            </div>
          </el-form-item>
          <el-form-item
              label="项目简介:">
<!--            <el-input style="width: 783px;"
                      type="textarea"
                      :autosize="{ minRows: 5, maxRows: 15}"
                      placeholder="请输入项目简介"
                      v-model="setupProject.projectIntroduce"
            ></el-input>-->
            <div style="width: 90%" v-rich-html="setupProject.projectIntroduce"></div>
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
        </el-form>
      </div>
      <div>
        <div class="clearfix form-actions">
          <div style="display: flex; justify-content: center;">
            <button v-on:click="create()" class="ca-btn-primary" type="button">
              创建
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
import ProductOwnerPicker from "@/components/rdms/product-owner-picker";
import { postWithMaintenance } from "@/shared/dev-version-guard";

export default {

  name: 'project-create-module',
  components: {
    ProductOwnerPicker,
  },
  props:{
    closeFunction: {
      type: Function,
      default: null
    },
  },

  data() {
    return {
      loginUser: {},
      loginCustomer: {},
      preProject: {},

      value: [],
      workers: [],
      workerList: [],
      titles: ['可选人员','核心成员'],
      milestone: {},
      milestones: [],

      dialogFormVisible: false,
      formLabelWidth: '120px',
      supervises: [],
      productManagers: [],
      projectManagers: [],

      characterList: [],
      characterListLeft: [],
      // archivedProjects: [],
      setupProject: {
        devVersion: '01.00.0000',
        projectIntroduce: '特性开发项目对应特性仓库相关的特性开发. <br/>特性开发项目每个团队只能创建一个, 对应着整个团队所有非立项开发的特性开发管理. <br/>' +
            '在特性开发项目中, 可以执行特性迭代开发任务和直接定义的特性开发任务. 在管理实践过程中, 我们更多的将这个项目作为灵活工作量处理的一个综合项目使用' +
            '特性开发项目没有项目评审的过程, 也就是说, 他会一直存在, 这个项目用于运作特性开发微小项目, 当一个特性开发完成并完成特性评审后, 就会出现在特性仓库中.',
      },

      multipleSelection: [],
      // multipleSelectionProject: [],

      PROJECT_TYPE: PROJECT_TYPE,
      DOC_TYPE: DOC_TYPE,
      FILE_GROUPING: FILE_GROUPING,

      resFiles: [],
      fileIdList: [],
    };
  },

  mounted: function() {
    let _this =this;
    _this.getLoginUser();
    _this.setupProject.id = Uuid.shortUuid(8);
    _this.listIpmtByCustomerId();
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

    create() {
      let _this = this;
      // 保存校验
      if (1 !== 1
          || !Validator.require(_this.setupProject.projectName, "项目名称",Swal)
          // || !Validator.require(_this.setupProject.devVersion, "项目名称",Swal)
          // || !Validator.require(_this.setupProject.projectIntroduce, "项目简介",Swal)
          || !Validator.require(_this.setupProject.supervise, "监管委员",Swal)

          || !Validator.length(_this.setupProject.projectName, "项目名称", 2, 50,Swal)
          || !DevVersionUtil.requireValid(_this.setupProject.devVersion, "规划开发版本", Swal)
          // || !Validator.length(_this.setupProject.projectIntroduce, "项目简介", 5, 500,Swal)
          || !Validator.length(_this.setupProject.supervise, "监管委员", 8, 8,Swal)
          || !_this.$refs.productPicker.validate()
      ) {
        return;
      }

      function exeApplication() {
        {
          _this.setupProject.customerId = _this.loginCustomer.id;
          postWithMaintenance(_this, process.env.VUE_APP_SERVER + '/customer/project/module-setup', _this.setupProject).then((response) => {
            let resp = response.data;
            if (resp.cancelled) {
              return;
            }
            if (resp.success) {
              Toast.success("保存成功", Swal);
              _this.close();
              _this.$bus.$emit('refresh-setup-list', true);
              _this.$bus.$emit('notify-setup-count-flag', true);

            } else {
              Toast.warning(resp.message, Swal);
            }
          })
        }
      }

      Swal.fire({
        title: '确认',
        text: "确认创建项目吗?",
        icon: 'question',
        showCancelButton: true,
        confirmButtonColor: '#409EFF',
        cancelButtonColor: '#909399',
        confirmButtonText: '确认!',
        cancelButtonText: '取消!'
      }).then((result) => {
        if (result.isConfirmed) {
          exeApplication();
        }
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

    close(){
      let _this = this;
      _this.closeFunction();
    },
  }
};
</script>

<style lang="scss" scoped>
@import './../../../public/static/scss/caipd.scss';
.ca-form{
  font-size: 14px;
  margin-left: 30px;
}
</style>
