<!--
 * Author: 周朔鹏
 * Date: 2024-07-08
 * Email: 391902958@qq.com
 * Website: https://www.euipd.com
-->

<template>
  <div>
    <!--Tab 卡片-->
    <div class="row">
      <div class="col-md-12">
        <div class="tabbable">
          <ul class="nav nav-tabs" id="myTab">
            <li class="active">
              <a data-toggle="tab" href="#home">
                <i class="green ace-icon fa fa-home bigger-120"></i>
                员工列表
              </a>
            </li>

            <li>
              <a data-toggle="tab" href="#edit-worker">
                添加/编辑开发者
              </a>
            </li>

          </ul>

          <div class="tab-content">
            <div id="home" class="tab-pane fade in active">
              <p>合作管理: 对外合作, 以构建完整的技术合作产业链.</p>
              <p>通过合作管理模块, 任何一个产品研发机构, 可以通过平台充分利用第三方或公开市场的研发人员开展合作研发.</p>
              <div class="hr hr-18 dotted hr-double"></div>
              <div>
                <pagination ref="pagination" v-bind:list="list" v-bind:itemCount="6"></pagination>
                <table id="simple-table-worker" class="table  table-bordered table-hover">
                  <thead>
                  <tr>
                    <th>id</th>
                    <th>登录名(手机)</th>
                    <th>姓名</th>
                    <th>部门</th>
                    <th>职务</th>
                    <th>岗位名称</th>
                    <th>加入时间</th>
                    <th>操作</th>
                  </tr>
                  </thead>

                  <tbody>
                  <tr v-for="worker in users" :key="worker.id">
                    <td>{{worker.id}}</td>
                    <td>{{worker.loginName}}</td>
                    <td>{{worker.trueName}}</td>
                    <td>{{worker.departmentName}}</td>
                    <td>{{worker.title}}</td>
                    <td>{{worker.jobPosition}}</td>
                    <td>{{worker.joinTimeStr}}</td>
                    <td>
                      <div class="hidden-sm hidden-xs btn-group">
                        <button v-if="canResetPassword" v-on:click="editPassword(worker)" class="ca-btn-primary-sm">
                          <i class="ace-icon fa fa-key bigger-120"></i>密码
                        </button>
                        <button v-on:click="edit(worker)" class="ca-btn-primary-sm">
                          <i class="ace-icon fa fa-pencil bigger-120"></i>编辑
                        </button>
                        <button v-on:click="del(worker.id)" class="ca-btn-danger-sm">
                          <i class="ace-icon fa fa-trash-o bigger-120"></i>删除
                        </button>
                      </div>
                    </td>
                  </tr>
                  </tbody>
                </table>
              </div>
            </div>

            <div id="edit-worker" class="tab-pane fade">
              <p>添加/编辑开发者</p>
              <div class="hr hr-18 dotted hr-double"></div>
              <div class="form-horizontal">
                <div class="row">
<!--                  第一列-->
                  <div class="col-md-6">
                    <div class="form-group">
                      <label class="col-sm-3 control-label no-padding-right" for="form-field-loginName"> 登录名(手机)<span style="color:red;">*</span> </label>
                      <div class="col-sm-9">
                        <input v-model="worker.loginName" type="text" id="form-field-loginName" placeholder="登录名(手机)" class="col-xs-10 col-sm-5" />
                        <span class="input-group-btn">
                          <button v-on:click="loginSearch()" class="btn btn-sm btn-default" type="button">
                            <i class="ace-icon fa fa-search bigger-110"></i>
                            查询
                          </button>
                         </span>
                      </div>
                    </div>
                    <div class="form-group">
                      <label class="col-sm-3 control-label no-padding-right" for="form-field-trueName"> 姓名<span style="color:red;">*</span> </label>
                      <div class="col-sm-9">
                        <input v-model="worker.trueName" type="text" id="form-field-trueName" placeholder="姓名" class="col-xs-10 col-sm-5" />
                      </div>
                    </div>
                    <div class="form-group">
                    <label class="col-sm-3 control-label no-padding-right" for="form-field-subject"> 专业<span style="color:red;">*</span> </label>
                    <div class="col-sm-9">
                      <input v-model="worker.subject" type="text" id="form-field-subject" placeholder="专业" class="col-xs-10 col-sm-5" />
                    </div>
                  </div>
                    <div class="form-group">
                      <label class="col-sm-3 control-label no-padding-right" for="form-field-educationBackground"> 学历<span style="color:red;">*</span> </label>
                      <div class="col-sm-9">
                        <input v-model="worker.educationBackground" type="text" id="form-field-educationBackground" placeholder="学历" class="col-xs-10 col-sm-5" />
                      </div>
                    </div>
                    <div class="form-group">
                      <label class="col-sm-3 control-label no-padding-right" for="form-field-degree"> 学位<span style="color:red;">*</span> </label>
                      <div class="col-sm-9">
                        <input v-model="worker.degree" type="text" id="form-field-degree" placeholder="学位" class="col-xs-10 col-sm-5" />
                      </div>
                    </div>
                    <div class="form-group">
                      <label class="col-sm-3 control-label no-padding-right" for="form-field-email"> 电子邮件 </label>
                      <div class="col-sm-9">
                        <input v-model="worker.email" type="text" id="form-field-email" placeholder="电子邮件" class="col-xs-10 col-sm-5" />
                      </div>
                    </div>
                    <div class="form-group">
                      <label class="col-sm-3 control-label no-padding-right" > 生日 </label>
                      <div class="col-sm-9">
                        <el-date-picker
                            v-model="worker.birthday"
                            type="date"
                            :picker-options="pickerOptions"
                            placeholder="选择日期">
                        </el-date-picker>
                      </div>
                    </div>
                    <div class="form-group">
                      <label class="col-sm-3 control-label no-padding-right"> 能力描述<span style="color:red;">*</span> </label>
                      <div class="col-sm-9">
                        <!--                    <input v-model="worker.description" type="text" id="form-field-description" placeholder="能力描述" class="col-xs-10 col-sm-5" />-->
                        <textarea rows="5" v-model="worker.description" type="text" placeholder="能力描述" class="col-xs-12 col-sm-9"></textarea>
                      </div>
                    </div>
                    <div class="form-group">
                      <label class="col-sm-3 control-label no-padding-right">员工头像</label>
                      <div class="col-sm-6">
                        <file-rdms v-bind:input-id="'image-upload-profile-supplychain'"
                              v-bind:text="'上传员工头像'"
                              v-bind:suffixs="['jpg', 'jpeg', 'png']"
                              v-bind:customer="loginCustomer.id"
                              v-bind:use="FILE_GROUPING.USER.key"
                              v-bind:after-upload="afterUpload">
                        </file-rdms>
                        <div v-show="worker.profile" class="row">
                          <div class="col-md-4">
                            <img v-bind:src="worker.profile" alt="profile" class="img-responsive">
                          </div>
                        </div>
                      </div>
                    </div>

                  </div>
<!--                  第二列-->
                  <div class="col-md-6">
                    <div class="form-group">
                      <label class="col-sm-3 control-label no-padding-right" for="form-field-customerName"> 团队名称 </label>
                      <div class="col-sm-9">
                        <input v-model="worker.customerName" readonly="" type="text" id="form-field-customerName" placeholder="团队名称" class="col-xs-12 col-sm-9" />
                      </div>
                    </div>
                    <div class="form-group">
                      <label class="col-sm-3 control-label no-padding-right" for="form-field-officeAddress"> 工作地址 </label>
                      <div class="col-sm-9">
                        <input v-model="worker.officeAddress" readonly="" type="text" id="form-field-officeAddress" placeholder="工作地址" class="col-xs-12 col-sm-9" />
                      </div>
                    </div>
                    <div class="form-group">
                      <label class="col-sm-3 control-label no-padding-right" for="form-field-jobNum"> 工号<span style="color:red;">*</span> </label>
                      <div class="col-sm-9">
                        <input v-model="worker.jobNum" type="text" id="form-field-jobNum" placeholder="工号" class="col-xs-10 col-sm-5" />
                      </div>
                    </div>

                    <div class="form-group">
                      <!-- v-model="worker.departmentId"  的值是:  v-bind:value="o.id-->
                      <label class="col-sm-3 control-label no-padding-right"> 部门名称<span style="color:red;">*</span> </label>
                      <div class="col-sm-9">
                        <select v-model="worker.departmentId" class="col-xs-10 col-sm-5" >
                          <option v-for="o in departments" :key="o.id" v-bind:value="o.id">{{o.departName}}</option>
                        </select>
                      </div>
                    </div>
                    <div class="form-group">
                      <label class="col-sm-3 control-label no-padding-right" for="form-field-title"> 职务<span style="color:red;">*</span> </label>
                      <div class="col-sm-9">
                        <input v-model="worker.title" type="text" id="form-field-title" placeholder="职务" class="col-xs-10 col-sm-5" />
                      </div>
                    </div>
                    <div class="form-group">
                      <label class="col-sm-3 control-label no-padding-right" for="form-field-jobPosition"> 标准岗位名称<span style="color:red;">*</span> </label>
                      <div class="col-sm-9">
                        <input v-model="worker.jobPosition" type="text" id="form-field-jobPosition" placeholder="标准岗位名称" class="col-xs-10 col-sm-5" />
                      </div>
                    </div>
                    <div class="form-group">
                      <label class="col-sm-3 control-label no-padding-right" for="form-field-pLevel"> 专业等级 </label>
                      <div class="col-sm-9">
                        <input v-model="worker.professionLevel" type="text" id="form-field-pLevel" placeholder="专业等级" class="col-xs-10 col-sm-5" />
                      </div>
                    </div>
                    <div class="form-group">
                      <label class="col-sm-3 control-label no-padding-right" for="form-field-mLevel"> 管理岗位等级 </label>
                      <div class="col-sm-9">
                        <input v-model="worker.managementLevel" type="text" id="form-field-mLevel" placeholder="管理岗位等级" class="col-xs-10 col-sm-5" />
                      </div>
                    </div>
                    <div class="form-group">
                      <label class="col-sm-3 control-label no-padding-right" for="form-field-manHourFee"> 工时费 </label>
                      <div class="col-sm-9">
                        <input v-model="worker.manHourFee" type="text" id="form-field-manHourFee" placeholder="工时费" class="col-xs-10 col-sm-5" />
                      </div>
                    </div>
                    <div class="form-group">
                      <label class="col-sm-3 control-label no-padding-right" for="form-field-salaryPerMonth"> 税前工资/月 </label>
                      <div class="col-sm-9">
                        <input v-model="worker.salaryPerMonth" type="text" id="form-field-salaryPerMonth" placeholder="工时费" class="col-xs-10 col-sm-5" />
                      </div>
                    </div>
                    <div class="form-group">
                      <label class="col-sm-3 control-label no-padding-right" > 加入时间<span style="color:red;">*</span> </label>
                      <div class="col-sm-9">
                        <el-date-picker
                            v-model="worker.joinTime"
                            type="date"
                            :picker-options="pickerOptions"
                            placeholder="选择日期">
                        </el-date-picker>
                      </div>

                    </div>
                    <div class="form-group">
                      <label class="col-sm-3 control-label no-padding-right"> 状态<span style="color:red;">*</span> </label>
                      <div class="col-sm-9">
                        <select v-model="worker.status" class="col-xs-10 col-sm-5" >
                          <option v-for="o in WORKER_STATUS" :key="o.key" v-bind:value="o.key">{{o.value}}</option>
                        </select>
                      </div>
                    </div>
                  </div>
                </div>

                <div class="clearfix form-actions">
                  <div class="col-md-offset-6 col-md-6">
                    <button v-on:click="saveUser()" class="btn btn-info" type="button">
                      <i class="ace-icon fa fa-check bigger-110"></i>
                      保存
                    </button>
                    &nbsp; &nbsp; &nbsp;
                    <button class="btn" type="reset">
                      <i class="ace-icon fa fa-undo bigger-110"></i>
                      重填
                    </button>
                  </div>
                </div>
                <!--                <div class="hr hr-24"></div>-->
              </div>
            </div>

          </div>
        </div>
      </div>
    </div>

    <div id="edit-modal" class="modal fade" tabindex="-1" role="dialog">
      <div class="modal-dialog" role="document">
        <div class="modal-content">
          <div class="modal-header">
            <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">&times;</span></button>
            <h4 class="modal-title">编辑员工信息</h4>
          </div>
          <div class="modal-body">
            <div class="form-horizontal">
              <div class="form-group">
                <label class="col-sm-3 control-label no-padding-right" for="form-field-1-id"> 用户编号 </label>
                <div class="col-sm-9">
                  <input v-model="worker.id" readonly="" type="text" id="form-field-1-id" placeholder="用户编号" class="col-xs-10 col-sm-5" />
                </div>
              </div>
              <div class="form-group">
                <label class="col-sm-3 control-label no-padding-right" for="form-field-1-trueName"> 真实姓名 </label>
                <div class="col-sm-9">
                  <input v-model="worker.trueName" type="text" id="form-field-1-trueName" placeholder="真实姓名" class="col-xs-10 col-sm-5" />
                </div>
              </div>
              <div class="form-group">
                <label class="col-sm-3 control-label no-padding-right" for="form-field-1-subject"> 专业 </label>
                <div class="col-sm-9">
                  <input v-model="worker.subject" type="text" id="form-field-1-subject" placeholder="工号" class="col-xs-12 col-sm-9" />
                </div>
              </div>
              <div class="form-group">
                <label class="col-sm-3 control-label no-padding-right" for="form-field-1-educationBackground"> 学历 </label>
                <div class="col-sm-9">
                  <input v-model="worker.educationBackground" type="text" id="form-field-1-educationBackground" placeholder="工号" class="col-xs-12 col-sm-9" />
                </div>
              </div>
              <div class="form-group">
                <label class="col-sm-3 control-label no-padding-right" for="form-field-1-degree"> 学位 </label>
                <div class="col-sm-9">
                  <input v-model="worker.degree" type="text" id="form-field-1-degree" placeholder="工号" class="col-xs-12 col-sm-9" />
                </div>
              </div>
              <div class="form-group">
                <label class="col-sm-3 control-label no-padding-right" for="form-field-1-title"> 职务 </label>
                <div class="col-sm-9">
                  <input v-model="worker.title" type="text" id="form-field-1-title" placeholder="职务" class="col-xs-10 col-sm-5" />
                </div>
              </div>
              <div class="form-group">
                <label class="col-sm-3 control-label no-padding-right" for="form-field-1-email"> 电子邮件 </label>
                <div class="col-sm-9">
                  <input v-model="worker.email" type="text" id="form-field-1-email" placeholder="电子邮件" class="col-xs-10 col-sm-5" />
                </div>
              </div>
              <div class="form-group">
                <label class="col-sm-3 control-label no-padding-right"> 员工状态 </label>
                <div class="col-sm-9">
                  <select v-model="worker.status" class="col-xs-10 col-sm-5" >
                    <option v-for="o in WORKER_STATUS" :key="o.key" v-bind:value="o.key">{{o.value}}</option>
                  </select>
                </div>
              </div>
              <div class="form-group">
                <label class="col-sm-3 control-label no-padding-right">员工头像</label>
                <div class="col-sm-6">
                  <file-rdms v-bind:input-id="'image-upload-supplychain'"
                        v-bind:text="'上传员工头像'"
                        v-bind:suffixs="['jpg', 'jpeg', 'png']"
                        v-bind:customer="loginCustomer.id"
                        v-bind:use="FILE_GROUPING.USER.key"
                        v-bind:after-upload="afterUpload">
                  </file-rdms>
                  <div v-show="worker.profile" class="row">
                    <div class="col-md-4">
                      <img v-bind:src="worker.profile" alt="profile" class="img-responsive">
                    </div>
                  </div>
                </div>
              </div>
              <div class="clearfix form-actions">
                <div class="col-md-offset-3 col-md-9">
                  <button v-on:click="saveUser()" class="btn btn-info" type="button">
                    <i class="ace-icon fa fa-check bigger-110"></i>
                    保存
                  </button>
                  <button class="btn btn-default" data-dismiss="modal">
                    <i class="ace-icon fa fa-remove bigger-110"></i>
                    取消
                  </button>
                </div>
              </div>
              <!--                <div class="hr hr-24"></div>-->
            </div>
          </div>
        </div><!-- /.modal-content -->
      </div><!-- /.modal-dialog -->
    </div><!-- /.modal -->

    <div id="edit-password-modal" class="modal fade" tabindex="-1" role="dialog">
      <div class="modal-dialog" role="document">
        <div class="modal-content">
          <div class="modal-header">
            <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">&times;</span></button>
            <h4 class="modal-title">修改密码</h4>
          </div>
          <div class="modal-body">
            <div class="form-horizontal">
              <div class="form-group">
                <label class="control-label col-sm-2">密码</label>
                <div class="col-sm-10">
                  <input class="form-control" type="password" v-model="worker.password" name="password">
                </div>
              </div>
            </div>
          </div>
          <div class="modal-footer">
            <button type="button" class="btn btn-white btn-default btn-round" data-dismiss="modal">
              <i class="ace-icon fa fa-times"></i>
              取消
            </button>
            <button type="button" class="btn btn-white btn-info btn-round" v-on:click="savePassword()">
              <i class="ace-icon fa fa-plus blue"></i>
              保存密码
            </button>
          </div>
        </div><!-- /.modal-content -->
      </div><!-- /.modal-dialog -->
    </div><!-- /.modal -->
  </div>
</template>

<script>
import Pagination from "../../../components/pagination";
import FileRdms from "../../../components/rdms/file-rdms";
import Swal from "sweetalert2";

export default {
  components: {
    Pagination,
    FileRdms
  },
  name: "supplychain",
  data: function() {
    return {
      loginUser: {},
      loginCustomer: {},

      keyWord: '',
      canResetPassword: Tool.hasModulePermission('USER', 'UPDATE'),

      worker: {},
      users: [],
      searchUsers:[],

      FILE_GROUPING: FILE_GROUPING,
      WORKER_STATUS: WORKER_STATUS,
      value1: '',
      checked: true,
      categorys: [],
      tree: {},
      customerName: '',
      departments:[],

      pickerOptions: {
        disabledDate(time) {
          return time.getTime() > Date.now();
        },
        shortcuts: [{
          text: '今天',
          onClick(picker) {
            picker.$emit('pick', new Date());
          }
        }, {
          text: '昨天',
          onClick(picker) {
            const date = new Date();
            date.setTime(date.getTime() - 3600 * 1000 * 24);
            picker.$emit('pick', date);
          }
        }, {
          text: '一周前',
          onClick(picker) {
            const date = new Date();
            date.setTime(date.getTime() - 3600 * 1000 * 24 * 7);
            picker.$emit('pick', date);
          }
        }]
      },

    }
  },
  mounted: function() {
    let _this = this;
    _this.getLoginUser();

    _this.$refs.pagination.size = 12;
    _this.list(1);
    _this.getDepartments();
    // sidebar激活样式方法一
    // this.$parent.activeSidebar("system-worker-sidebar");

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

    /**
     * 查找是否有权限
     * @param id
     */
    hasResource(id) {
      return Tool.hasResource(id);
    },

    /**
     * 获取当前客户公司的所有部门信息
     */
    getDepartments(){
      let _this = this;
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/department/get-departments', {
        customerId: _this.loginCustomer.id,
      }).then((response)=>{
        let resp = response.data;
        if (resp.success) {
          _this.departments = resp.content.departments;
          _this.worker.customerName = resp.content.customerName;
        } else {
          Toast.warning(resp.message,Swal);
        }
      })
    },

    /*      /!**
           * 点击【新增】
           *!/
          add() {
            let _this = this;
            _this.worker = {};
            $("#form-modal").modal("show");
          },*/

    /**
     * 点击【编辑】
     */
    edit(worker) {
      let _this = this;
      _this.worker = $.extend({}, worker);
      $("#edit-modal").modal("show");
    },

    /**
     * 列表查询
     */
    list(page) {
      let _this = this;
      // Loading.show();
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/worker/list', {
        page: page,
        size: _this.$refs.pagination.size,
        customerId: _this.loginCustomer.id,
      }).then((response)=>{
        // Loading.hide();
        let resp = response.data;
        if (resp.success) {
          _this.users = resp.content.list;
          _this.customerName = _this.users[0].customerName;
          _this.$refs.pagination.render(page, resp.content.total);
        } else {
          Toast.warning(resp.message,Swal);
        }
      })
    },

    /**
     * 保存客户信息
     */
    saveUser() {
      let _this = this;

      // 保存校验
      if (1 !== 1
          || !Validator.require(_this.worker.loginName, "登录名称", Swal)
          || !Validator.require(_this.worker.trueName, "真实姓名", Swal)
          // || !Validator.require(_this.worker.customerName, "团队名称", Swal)
          // || !Validator.require(_this.worker.baseAddress, "工作地址", Swal)
          || !Validator.require(_this.worker.subject, "学科专业", Swal)
          || !Validator.require(_this.worker.educationBackground, "学历", Swal)
          || !Validator.require(_this.worker.degree, "学位", Swal)
          || !Validator.require(_this.worker.description, "能力描述", Swal)
          || !Validator.require(_this.worker.jobNum, "工号", Swal)
          || !Validator.require(_this.worker.departmentId, "部门ID", Swal)
          || !Validator.require(_this.worker.title, "职务", Swal)
          || !Validator.require(_this.worker.jobPosition, "标准岗位名称", Swal)

          || !Validator.length(_this.worker.loginName, "登录名称", 11, 11, Swal)
          || !Validator.length(_this.worker.trueName, "真实姓名", 2, 4, Swal)
          // || !Validator.length(_this.worker.customerName, "团队名称", 5, 50, Swal)
          // || !Validator.length(_this.worker.baseAddress, "工作地址", 5, 100, Swal)
          || !Validator.length(_this.worker.subject, "学科专业", 2, 20, Swal)
          || !Validator.length(_this.worker.educationBackground, "学历", 2, 20, Swal)
          || !Validator.length(_this.worker.degree, "学位", 2, 20, Swal)
          || !Validator.length(_this.worker.description, "能力描述", 10, 500, Swal)
          || !Validator.length(_this.worker.jobNum, "工号", 3, 10, Swal)
          || !Validator.length(_this.worker.departmentId, "部门ID", 8, 8, Swal)
          || !Validator.length(_this.worker.title, "职务", 2, 20, Swal)
          || !Validator.length(_this.worker.jobPosition, "标准岗位名称", 3, 12, Swal)
      ) {
        return;
      }

      // Loading.show();
      //从session中取出登录用户(这里是企业用户customer的信息)信息
      _this.worker.customerId = _this.loginCustomer.id;
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/worker/save', _this.worker).then((response)=>{
        // Loading.hide();
        let resp = response.data;
        if (resp.success) {
          // $("#form-modal").modal("hide");
          $("#edit-modal").modal("hide");
          _this.list(1);
          Swal.fire({
            position: 'top-end',
            icon: 'success',
            title: "保存成功",
            showConfirmButton: false,
            timer: 1000
          })
        } else {
          //Toast.warning(resp.message);
          Swal.fire({
            position: 'top-end',
            icon: 'warning',
            title: resp.message,
            showConfirmButton: false,
            timer: 1500
          })
        }
      })
    },

//根据客户登录名查询
    loginSearch(){
      let _this = this;
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/worker/login-search', _this.worker).then((response)=>{
        // Loading.hide();
        let resp = response.data;
        if (resp.success) {
          _this.users = resp.content;
          _this.worker = _this.users[0];
          _this.list(1);
        } else {
          Swal.fire({
            position: 'top-end',
            icon: 'warning',
            title: resp.message,
            showConfirmButton: false,
            timer: 1500
          })
        }
      })
    },

    /**
     * 点击【删除】
     */
    del(id) {
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
          _this.$ajax.delete(process.env.VUE_APP_SERVER + '/customer/worker/del/' + id).then((response)=>{
            // Loading.hide();
            let resp = response.data;
            if (resp.success) {
              _this.list(1);
              //Toast.success("删除成功！");
              Swal.fire({
                position: 'top-end',
                icon: 'success',
                title: "删除成功",
                showConfirmButton: false,
                timer: 1000
              })
            } else {
              Toast.warning(resp.message,Swal);
            }
          });
        }
      })
    },

    /**
     * 点击【重置密码】
     */
    editPassword(worker) {
      let _this = this;
      _this.worker = $.extend({}, worker);
      _this.worker.password = null;
      $("#edit-password-modal").modal("show");
    },

    /**
     * 点击【保存密码】
     */
    savePassword() {
      let _this = this;
      _this.$ajax.get(process.env.VUE_APP_SERVER + '/system/rdms/user/login-salt/' + _this.worker.loginName).then((saltResp) => {
        let saltBody = saltResp.data || {};
        if (!saltBody.success || !saltBody.content) {
          Toast.warning(saltBody.message || "读取用户盐失败", Swal);
          return;
        }
        _this.worker.password = hex_md5(_this.worker.password + saltBody.content);
      // Loading.show();
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/system/rdms/user/save-password', _this.worker).then((response)=>{
        // Loading.hide();
        let resp = response.data;
        if (resp.success) {
          $("#edit-password-modal").modal("hide");
          _this.list(1);
          Swal.fire({
            position: 'top-end',
            icon: 'success',
            title: "修改密码成功",
            showConfirmButton: false,
            timer: 1000
          })
        } else {
          //Toast.warning(resp.message);
          Swal.fire({
            position: 'top-end',
            icon: 'warning',
            title: resp.message,
            showConfirmButton: false,
            timer: 1500
          })
        }
      });
      });
    },

    afterUpload(resp) {
      let _this = this;
      _this.worker.profile = resp.content.path;
      // 新增讲师，上传头像后不能实时预览，解决方法二
      _this.$forceUpdate();
    },
  }
}
</script>
