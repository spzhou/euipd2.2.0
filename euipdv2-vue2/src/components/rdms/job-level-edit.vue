<!--
 * Author: 周朔鹏
 * Date: 2024-11-12
 * Email: 391902958@qq.com
 * Website: https://www.euipd.com
-->

<template>
  <div>
    <el-form :label-position="labelPosition" label-width="140px">
      <el-form-item label="职级代码">
        <el-col :span="6">
          <el-select v-model="jobLevel.levelCode" filterable placeholder="请选择">
            <el-option
                v-for="item in options"
                :key="item.value"
                :label="item.label"
                :value="item.value">
            </el-option>
          </el-select>
        </el-col>
      </el-form-item>
      <el-form-item label="职级名称" >
        <el-col :span="20">
          <el-input v-model="jobLevel.leveName" placeholder="比如: 高级工程师 专家级工程师 等"></el-input>
        </el-col>
      </el-form-item>
      <el-form-item label="工时费率">
        <el-col :span="6">
          <el-input type="number" v-model="jobLevel.manHourFee" placeholder="标准工时费率"></el-input>
        </el-col>
      </el-form-item>
    </el-form>
    <div class="hr hr-18 dotted hr-double"></div>
    <div class="clearfix form-actions">
      <div style="display: flex; justify-content: center;">
        <button v-on:click="saveJobLevel()" class="ca-btn-primary" type="button">
          保存
        </button>
        <button v-on:click="close()" class="ca-btn-primary" type="button">
          关闭
        </button>
      </div>
    </div>
  </div>
</template>

<script>
import FileRdms from "@/components/rdms/file-rdms";
import Swal from "sweetalert2";

export default {
  components: {
    FileRdms,
  },
  name: "job-level-edit",

  data: function() {
    return {
      loginUser: {},
      loginCustomer: {},

      jobLevel: {},

      labelPosition: 'right',

      options: [{
        value: 'P2',
        label: 'P2'
      }, {
        value: 'M2',
        label: 'M2'
      }, {
        value: 'P3',
        label: 'P3'
      }, {
        value: 'M3',
        label: 'M3'
      }, {
        value: 'P4',
        label: 'P4'
      }, {
        value: 'M4',
        label: 'M4'
      }, {
        value: 'P5',
        label: 'P5'
      }, {
        value: 'M5',
        label: 'M5'
      }, {
        value: 'P6',
        label: 'P6'
      },{
        value: 'M6',
        label: 'M6'
      },{
        value: 'P7',
        label: 'P7'
      },{
        value: 'M7',
        label: 'M7'
      },{
        value: 'P8',
        label: 'P8'
      },{
        value: 'M8',
        label: 'M8'
      },{
        value: 'P9',
        label: 'P9'
      },{
        value: 'M9',
        label: 'M9'
      },{
        value: 'P10',
        label: 'P10'
      },{
        value: 'M10',
        label: 'M10'
      },{
        value: 'P11',
        label: 'P11'
      },{
        value: 'M11',
        label: 'M11'
      },{
        value: 'P12',
        label: 'P12'
      },{
        value: 'M12',
        label: 'M12'
      },{
        value: 'P13',
        label: 'P13'
      },{
        value: 'M13',
        label: 'M13'
      },
      ],
      // value: ''

    }
  },

  props: {
    editFlag: {
      default: null,
    },
    levelItem: {
      default: null,
    },

  },

  computed: {
  },

  mounted: function() {
    let _this = this;
    _this.getLoginUser();
    if(_this.editFlag){
      _this.jobLevel = _this.levelItem;
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

    close(){
      this.$parent.handleClose();
    },

    saveJobLevel(){
      let _this = this;
      if (1 !== 1
          || !Validator.require(_this.jobLevel.levelCode, "职级代码",Swal)
          || !Validator.require(_this.jobLevel.leveName, "职级名称",Swal)
          || !Validator.require(_this.jobLevel.manHourFee, "工时费率",Swal)

          || !Validator.length(_this.jobLevel.levelCode, "职级代码", 2, 4,Swal)
          || !Validator.length(_this.jobLevel.leveName, "职级名称", 2, 20,Swal)
          || !Validator.length(_this.jobLevel.manHourFee, "工时费率", 2, 10,Swal)
      ) {
        return;
      }

      if(_this.editFlag){
        _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/job-level/save', {
          customerId: _this.loginCustomer.id,
          levelCode: _this.jobLevel.levelCode,
          leveName: _this.jobLevel.leveName,
          manHourFee: _this.jobLevel.manHourFee,
        }).then((response)=>{
          let resp = response.data;
          if (resp.success) {
            Toast.success("保存成功", Swal);
            _this.$parent.handleClose();
          }else{
            Toast.warning(resp.message,Swal);
          }
        })
      }else{
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/job-level/glance', {
        customerId: _this.loginCustomer.id,
        levelCode: _this.jobLevel.levelCode,
      }).then((response)=>{
        let resp = response.data;
        if (resp.success) {
          let number = resp.content;
          // debugger
          if(number > 0){
            Swal.fire({
              title: '操作提示',
              text: '已经存在相同的职级代码, 是否覆盖？',
              icon: 'warning',
              showCancelButton: true,
              confirmButtonColor: '#409EFF',
              cancelButtonColor: '#909399',
              confirmButtonText: '覆盖'
            }).then((result) => {
              if (result.value) {
                _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/job-level/save', {
                  customerId: _this.loginCustomer.id,
                  levelCode: _this.jobLevel.levelCode,
                  leveName: _this.jobLevel.leveName,
                  manHourFee: _this.jobLevel.manHourFee,
                }).then((response)=>{
                  let resp = response.data;
                  if (resp.success) {
                    Toast.success("保存成功", Swal);
                    _this.$parent.handleClose();
                  }else{
                    Toast.warning(resp.message,Swal);
                  }
                })
              }
            })
          }else{
            _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/job-level/save', {
              customerId: _this.loginCustomer.id,
              levelCode: _this.jobLevel.levelCode,
              leveName: _this.jobLevel.leveName,
              manHourFee: _this.jobLevel.manHourFee,
            }).then((response)=>{
              let resp = response.data;
              if (resp.success) {
                Toast.success("保存成功", Swal);
                _this.$parent.handleClose();
              }else{
                Toast.warning(resp.message,Swal);
              }
            })
          }

        }else{
          Toast.warning(resp.message,Swal);
        }
      })
      }
    },
  }
}
</script>

<style lang="scss">
@import './../../../public/static/scss/caipd.scss';

.el-form-item__label {
  text-align: right;
  vertical-align: middle;
  float: left;
  font-size: 14px;
  color: #606266;
  line-height: 35px !important;
  padding: 0 12px 0 0;
  -webkit-box-sizing: border-box;
  box-sizing: border-box;
}
.el-dialog__body {
  padding: 30px 0 0;
  color: #606266;
  font-size: 14px;
  word-break: break-all;
}
</style>

<style lang="scss" scoped>
.el-form-item--mini.el-form-item, .el-form-item--small.el-form-item {
  margin-bottom: 18px !important;
}

</style>
