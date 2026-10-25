<!--
 * Author: 周朔鹏
 * Date: 2025-04-14
 * Email: 391902958@qq.com
 * Website: https://www.euipd.com
-->

<template>
  <div>
    <div style="display: flex; flex-direction: row; justify-content: space-between; align-items: center;">
      <div style="margin: 2px 0">
        <pagination ref="pagination" v-bind:list="list" v-bind:itemCount="6"></pagination>
      </div>
      <div style="margin-right: 20px;">
        <el-button type="primary" size="small" v-on:click="addCourse()">
          <i class="ace-icon fa fa-edit"></i>
          添加课程
        </el-button>
      </div>
    </div>

    <div class="container">
      <div class="row">
        <div v-for="course in courses" :key="course.id" class="col-md-4">
          <div class="thumbnail search-thumbnail">
            <div class="course-box">
              <div class="course-image">
                <img v-show="!course.image" class="media-object course-image" src="./../../../public/imgs/demo-course.jpg"/>
                <img v-show="course.image" class="media-object course-image" v-bind:src="course.image"/>
              </div>
              <div class="course-content">
                <div class="marquee">
                  <div class="marquee-wrap">
                    <div class="marquee-content">
                      {{ course.name }}
                    </div>
                  </div>
                </div>
<!--                <div class="row course-title-info">
                  <div class="info-tip">
                    <span class="label label-primary info-label">
                      {{ COURSE_LEVEL | optionKV(course.level) }}
                    </span>
                    <span class="label label-primary info-label">
                      {{ COURSE_CHARGE | optionKV(course.charge) }}
                    </span>
                    <span class="label label-primary info-label">
                      {{ COURSE_STATUS | optionKV(course.status) }}
                    </span>
                  </div>
                  <div class="price-tab">
                    <span class="price"><i class="fa fa-jpy" aria-hidden="true"></i> {{ course.price }}</span>&nbsp;
                  </div>
                </div>-->
                <div class="caption">
                  <div class="row">
                    <div class="col-sm-8">
                      <div v-for="teacher in teachers.filter(t=>{return t.id===course.teacherId})"
                           :key="teacher.id"
                           class="profile-activity profile-teacher clearfix">
                        <div>
                          <!--                          <img v-show="!teacher.image" class="pull-left" src="./../../../public/imgs/zsp.jpg"/>
                                                    <img v-show="teacher.image" class="pull-left" v-bind:src="teacher.image"/>-->
                          <a class="user" href="#"> {{ teacher.name }} </a>
                          <br>
                          {{ teacher.position }}
                        </div>
                      </div>
                    </div>
                    <div class="col-sm-4">
                      <div class="row course-title-info">
                        <div class="info-tip">
                          <span class="label label-primary info-label">
                            {{ COURSE_LEVEL | optionKV(course.level) }}
                          </span>
                          <span v-if="course.charge" class="label label-primary info-label">
                            {{ COURSE_CHARGE | optionKV(course.charge) }}
                          </span>
                                <span class="label label-primary info-label">
                            {{ COURSE_STATUS | optionKV(course.status) }}
                          </span>
                        </div>
<!--                        <div class="price-tab">
                          <span class="price"><i class="fa fa-jpy" aria-hidden="true"></i> {{ course.price }}</span>&nbsp;
                        </div>-->
                      </div>
                    </div>
                  </div>

                  <div class="parent">
                    <p class="child2">{{ course.summary }}</p>
                  </div>

                  <p class="info">
                    <!--                    <span class="badge badge-info">{{course.id}}</span>-->
                    <span class="badge badge-info">排序：{{ course.sort }}</span>
                    <span class="badge badge-info">{{ course.time }}秒</span>
                  </p>
                  <p class="but-pos">
                    <el-button v-on:click="toChapter(course)">
                      篇章
                    </el-button>
                    <el-button v-on:click="sortCourse(course)">
                      排序
                    </el-button>
                    <el-button v-on:click="edit(course)">
                      编辑
                    </el-button>
                    <!--                    <el-button v-on:click="del(course.id)">-->
                    <!--                      删除-->
                    <!--                    </el-button>-->
                  </p>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <el-dialog
        width="23cm"
        :visible.sync="createCourseDlg"
        :before-close="handleClose"
        :close-on-click-modal=false>
      <div slot="title" style="font-size: 20px;">{{ dialogTitle }}</div>
      <div v-if="createCourseDlg">
        <form class="form-horizontal">
          <div class="form-group">
            <label class="col-sm-2 control-label">
              课程分类
            </label>
            <div class="col-sm-10" style="max-height: 400px; overflow-y: scroll">
              <el-tree style="min-width: 300px;"
                       :data="treeData"
                       show-checkbox
                       node-key="id"
                       ref="tree"
                       highlight-current
                       @check-change="handleCheckChange"
                       :props="defaultProps">
              </el-tree>
            </div>
          </div>
          <div class="form-group">
            <label class="col-sm-2 control-label">封面/视频</label>
            <div class="col-sm-10">
              <div class="row">
                <div class="col-sm-6">
                  <file-rdms v-bind:input-id="'course-manage-upload-file'"
                             v-bind:text="'上传封面'"
                             v-bind:suffixs="['jpg', 'jpeg', 'png']"
                             v-bind:customer="loginCustomer.id"
                             v-bind:use="FILE_GROUPING.COURSE.key"
                             v-bind:folder="'course'"
                             v-bind:is-web-file="true"
                             v-bind:after-upload="afterUpload">
                  </file-rdms>
                  <div v-show="course.image" class="row">
                    <div class="col-md-9" style="margin-top: 10px;">
                      <img v-bind:src="course.image" class="img-responsive">
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
          <div style="width: 90%">
            <el-form label-position="right" label-width="120px" :model="course">
              <el-form-item label="名称" required>
                <el-input v-model="course.name"></el-input>
              </el-form-item>
              <el-form-item label="讲师" required>
                <span>
                  <el-select  v-model="course.teacherId" filterable placeholder="讲师">
                    <el-option v-for="o in teachers" :key="o.id" :value="o.id" :label="o.name"></el-option>
                  </el-select>
                </span>
                <span style="margin-left: 10px;">
                  <el-button @click.stop="addTeacher">添加</el-button>
                </span>
              </el-form-item>
              <el-form-item label="概述" required>
                <el-input type="textarea" rows="3" v-model="course.summary"></el-input>
              </el-form-item>
<!--              <el-form-item label="价格/元" required>
                <el-input style="width: 120px;" type="number" v-model="course.price"></el-input>
              </el-form-item>-->
              <el-form-item label="级别" required>
                <el-select  v-model="course.level" filterable placeholder="课程级别">
                  <el-option v-for="o in COURSE_LEVEL" :key="o.key" :value="o.key" :label="o.value"></el-option>
                </el-select>
              </el-form-item>
<!--              <el-form-item label="收费" required>
                <el-select  v-model="course.charge" filterable placeholder="是否收费">
                  <el-option v-for="o in COURSE_CHARGE" :key="o.key" :value="o.key" :label="o.value"></el-option>
                </el-select>
              </el-form-item>-->
              <el-form-item label="状态" required>
                <el-select  v-model="course.status" filterable placeholder="课程状态">
                  <el-option v-for="o in COURSE_STATUS" :key="o.key" :value="o.key" :label="o.value"></el-option>
                </el-select>
              </el-form-item>
            </el-form>
          </div>
        </form>

        <div class="modal-footer">
          <button v-on:click="save()" type="button" class="btn btn-primary">保存</button>
          <button type="button" class="btn btn-default" @click="closeDialog">取消</button>
        </div>
      </div>
    </el-dialog>

    <el-dialog
        width="23cm"
        :visible.sync="sortCourseDlg"
        :before-close="handleClose"
        :close-on-click-modal=false>
      <div slot="title" style="font-size: 20px;">{{ dialogTitle }}</div>
      <div v-if="sortCourseDlg">
        <div style="width: 70%; margin-bottom: 20px;">
          <form class="form-horizontal">
            <div class="form-group">
              <label class="control-label col-lg-5">
                当前排序
              </label>
              <div class="col-lg-7">
                <input class="form-control" v-model="sort.oldSort" name="oldSort" disabled>
              </div>
            </div>
            <div class="form-group">
              <label class="control-label col-lg-5">
                新排序
              </label>
              <div class="col-lg-7">
                <input class="form-control" v-model="sort.newSort" name="newSort">
              </div>
            </div>
          </form>
        </div>
        <div class="modal-footer">
          <button v-on:click="updateSort()" type="button" class="btn btn-primary">更新</button>
          <button type="button" class="btn btn-default" @click="closeDialog">取消</button>
        </div>
      </div>
    </el-dialog>

    <el-dialog
        width="23cm"
        append-to-body
        :visible.sync="createTeacherDlg"
        :before-close="handleClose"
        :close-on-click-modal=false>
      <div slot="title" style="font-size: 20px;">{{ dialogTitle_teacher }}</div>
      <div v-if="createTeacherDlg">
        <div style="width: 90%">
          <el-form label-position="right" label-width="120px" :model="teacher">
            <el-form-item label="姓名" required>
              <el-input v-model="teacher.name"></el-input>
            </el-form-item>
            <el-form-item label="简介" required>
              <el-input type="textarea" rows="3" v-model="teacher.intro"></el-input>
            </el-form-item>
          </el-form>
        </div>
        <div class="modal-footer">
          <button v-on:click="addTeacherInfo()" type="button" class="btn btn-primary">添加</button>
          <button type="button" class="btn btn-default" @click="closeAddTeacherDialog">取消</button>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import Pagination from "../../components/pagination";
import Vod from "../../components/vod";
import Swal from "sweetalert2";
import FileRdms from "@/components/rdms/file-rdms.vue";

export default {
  components: {FileRdms, Pagination, Vod},
  name: "course-manage",
  props: {
    openChapter: {
      type: Function,
      default: null
    },
  },
  data: function () {
    return {
      loginUser: {},
      loginCustomer: {},

      teacher: {},

      createCourseDlg: false,
      createTeacherDlg: false,
      sortCourseDlg: false,
      dialogTitle: '',
      dialogTitle_teacher: '',

      defaultProps: {
        children: 'children',
        label: 'name'
      },
      treeData: [],

      course: {
        price: 0,
        image: null,
        charge: COURSE_CHARGE.FREE.key,
      },
      courses: [],
      COURSE_LEVEL: COURSE_LEVEL,
      COURSE_CHARGE: COURSE_CHARGE,
      COURSE_STATUS: COURSE_STATUS,
      FILE_GROUPING: FILE_GROUPING,
      ADMIN_COURSE_CUR_PAGE: ADMIN_COURSE_CUR_PAGE,
      categorys: [],
      checkedCategorys: [],
      saveContentLabel: "",
      sort: {
        id: "",
        oldSort: 0,
        newSort: 0
      },
      teachers: [],

      resFile: {},
      resFiles: [],
      resFileIds: [],
    }
  },
  mounted: function () {
    let _this = this;
    _this.getLoginUser();
    _this.$refs.pagination.size = 12;
    _this.allCategory();
    _this.allTeacher();
    _this.curPage = _this.ADMIN_COURSE_CUR_PAGE;
    _this.list(_this.curPage);
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
    },

    handleCheckChange(){
      /*let _this = this;
      console.log(_this.$refs.tree.getCheckedKeys());*/
    },
    addCourse() {
      let _this = this;
      _this.allCategory();
      _this.course = {
        sort: _this.$refs.pagination.total + 1,
        price: 0,
        image: null,
      };
      _this.createCourseDlg = true;
      _this.dialogTitle = "新建课程";
    },

    addTeacher() {
      let _this = this;
      _this.createTeacherDlg = true;
      _this.dialogTitle_teacher = "添加讲师";
    },

    sortCourse(course) {
      let _this = this;
      _this.sort = {
        id: course.id,
        oldSort: course.sort,
        newSort: course.sort
      };
      _this.sortCourseDlg = true;
      _this.dialogTitle = "更新课程排序";
    },

    closeDialog() {
      let _this = this;
      _this.createCourseDlg = false;
      _this.sortCourseDlg = false;
    },
    closeAddTeacherDialog() {
      let _this = this;
      _this.createTeacherDlg = false;
    },
    handleClose(done) {
      done();
    },

    list(page) {
      let _this = this;
      _this.ADMIN_COURSE_CUR_PAGE = page;
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/course/list', {
        page: page,
        size: _this.$refs.pagination.size,
        customerId: _this.loginCustomer.id,
        adminFlag: true,
      }).then((response) => {
        let resp = response.data;
        _this.courses = resp.content.list;
        _this.$refs.pagination.render(page, resp.content.total);
      })
    },

    /**
     * 点击【编辑】
     */
    edit(course) {
      let _this = this;
      _this.course = $.extend({}, course);
      _this.listCategory(course.id);
      _this.createCourseDlg = true;
    },

    /**
     * 点击【保存】
     */
    save() {
      let _this = this;

      if (1 != 1
          || !Validator.require(_this.course.name, "名称",Swal)
          || !Validator.require(_this.course.teacherId, "讲师",Swal)
          || !Validator.require(_this.course.summary, "概述",Swal)
          || !Validator.require(_this.course.image, "封面",Swal)
          // || !Validator.require(_this.course.price, "价格",Swal)
          || !Validator.require(_this.course.level, "课程级别",Swal)
          // || !Validator.require(_this.course.charge, "是否收费",Swal)
          || !Validator.require(_this.course.status, "状态",Swal)
          || !Validator.length(_this.course.name, "名称", 1, 50,Swal)
          || !Validator.length(_this.course.summary, "概述", 1, 200,Swal)
          // || !Validator.length(_this.course.image, "封面", 1, 200,Swal)
          // || !Validator.isNumber(_this.course.price, "价格")
      ) {
        return;
      }
      let checkedCategorys = _this.$refs.tree.getCheckedKeys()
      if (Tool.isEmpty(checkedCategorys) || checkedCategorys.length == 0) {
        Swal.fire({
          position: 'top-end',
          icon: 'info',
          title: "请选择分类",
          showConfirmButton: false,
          timer: 1500
        })
        return;
      }
      _this.course.categorys = checkedCategorys;
      _this.course.customerId = _this.loginCustomer.id;

      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/course/save', _this.course).then((response) => {
        let resp = response.data;
        if (resp.success) {
          Toast.success("保存成功", Swal);
          _this.closeDialog();
          _this.list(_this.curPage);
        } else {
          Toast.warning("未成功保存", Swal);
        }
      })
    },

    addTeacherInfo() {
      let _this = this;
      if (1 != 1
          || !Validator.require(_this.teacher.name, "姓名",Swal)
          || !Validator.require(_this.teacher.intro, "简介",Swal)
          || !Validator.length(_this.teacher.name, "姓名", 1, 50,Swal)
          || !Validator.length(_this.teacher.intro, "简介", 1, 500,Swal)
      ) {
        return;
      }
      _this.teacher.customerId = _this.loginCustomer.id;
      _this.teacher.creatorLoginname = _this.loginUser.loginName;
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/course/teacher/save', _this.teacher).then((response) => {
        let resp = response.data;
        if (resp.success) {
          Toast.success("保存成功", Swal);
          _this.closeAddTeacherDialog();
          _this.allTeacher();
        } else {
          Toast.warning("未成功保存", Swal);
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
        text: "删除课程后不可恢复，确认删除？",
        icon: 'warning',
        showCancelButton: true,
        confirmButtonColor: '#3085d6',
        cancelButtonColor: '#d33',
        confirmButtonText: '确认!'
      }).then((result) => {
        if (result.value) {
          _this.$ajax.delete(process.env.VUE_APP_SERVER + '/customer/course/delete/' + id).then((response) => {
            let resp = response.data;
            if (resp.success) {
              _this.list(_this.ADMIN_COURSE_CUR_PAGE);
              Swal.fire({
                position: 'top-end',
                icon: 'success',
                title: "删除成功",
                showConfirmButton: false,
                timer: 1000
              })
            }
          })
        }
      })
    },

    /**
     * 点击【篇章】
     */
    toChapter(course) {
      let _this = this;
      _this.openChapter(course);
    },

    allCategory() {
      let _this = this;
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/course/category/all').then((response) => {
        let resp = response.data;
        _this.categorys = resp.content;
        _this.treeData = [];
        //组装Tree数据
        for (let i = 0; i < _this.categorys.length; i++) {
          let c = _this.categorys[i];
          if (c.parent === '00000000') {
            _this.treeData.push(c);

            for (let j = 0; j < _this.categorys.length; j++) {
              let child = _this.categorys[j];
              if (child.parent === c.id) {
                if (Tool.isEmpty(c.children)) {
                  c.children = [];
                }
                c.children.push(child);
              }
            }
          }
        }

      })
    },

    /**
     * 查找课程下所有分类
     * @param courseId
     */
    listCategory(courseId) {
      let _this = this;
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/course/list-category/' + courseId).then((res) => {
        // console.log("查找课程下所有分类结果：", res);
        let response = res.data;
        let categorys = response.content;
        _this.$refs.tree.setCheckedKeys(categorys);

      })
    },

    updateSort() {
      let _this = this;
      if (_this.sort.newSort === _this.sort.oldSort) {
        Swal.fire({
          position: 'top-end',
          icon: 'warning',
          title: "排序没有变化",
          showConfirmButton: false,
          timer: 1500
        })
        return;
      }
      _this.$ajax.post(process.env.VUE_APP_SERVER + "/customer/course/sort", _this.sort).then((res) => {
        let response = res.data;
        if (response.success) {
          Toast.success("更新成功", Swal);
          _this.closeDialog();
          _this.list(_this.curPage);
        } else {
          Swal.fire({
            position: 'top-end',
            icon: 'warning',
            title: "更新排序失败",
            showConfirmButton: false,
            timer: 1500
          })
        }
      });
    },

    allTeacher() {
      let _this = this;
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/course/teacher/my-teacher/' + _this.loginCustomer.id).then((response) => {
        let resp = response.data;
        _this.teachers = resp.content;
      })
    },

    afterUpload(resp) {
      let _this = this;
      _this.course.image = resp.content.absPath;
      _this.$forceUpdate();
    },
  }
}
</script>

<style scoped lang="scss">
.container {
  .course-box {
    width: 100%;
    height: 410px;

    .course-image {
      width: 100%;
      height: 207px;
    }

    .course-content {
      position: relative;
      width: 100%;
      height: 208px;

      .marquee {
        overflow: hidden;

        .marquee-wrap {
          width: 100%;
          animation: marquee-wrap 6s infinite linear;
        }

        .marquee-content {
          font-size: 20px;
          font-weight: bold;
          color: #297dbb;
          margin-top: -3px;
          padding: 10px 0px 5px;
          //text-align: center;
          float: left;
          white-space: nowrap;
          min-width: 100%;
          animation: marquee-content 6s infinite linear;
        }
      }

      @keyframes marquee-wrap {
        0%,
        30% {
          transform: translateX(0);
        }
        70%,
        100% {
          transform: translateX(100%);
        }
      }
      @keyframes marquee-content {
        0%,
        30% {
          transform: translateX(0);
        }
        70%,
        100% {
          transform: translateX(-100%);
        }
      }
      .course-title-info {
        .info-tip {
          display: inline-block;
          margin-left: 10px;
          .inst-info {
            margin-left: 20px;
          }
        }
        .price-tab {
          display: inline-block;
          float: right;
          margin-right: 10px;
          font-weight: bold;
          font-size: 1.5rem;

          .price {
            text-align: right;
          }
        }
      }
      .caption {
        .profile-teacher {
          padding: 1px 4px !important;
          border-bottom: 1px dotted #D0D8E0;
          position: relative;
          border-left: 1px dotted #FFF;
          border-right: 1px dotted #FFF;
        }
        .parent {
          width: 100%;
          line-height: 18px;
          font-size: 14px;
          margin-bottom: 10px;
          .child2 {
            font-size: 14px;
            //font-weight: bold;
            //color: #297dbb;
            padding: 5px 3px 0px;
            //letter-spacing: 1px;
            overflow: hidden;
            text-overflow: ellipsis;
            -webkit-line-clamp: 2;
            display: -webkit-box;
            -webkit-box-orient: vertical;
          }
        }
        .info {
          position: absolute;
          top: 140px;
          //left: 150px;
          right: 10px;
          text-align: right;
        }
        .but-pos {
          position: absolute;
          top: 170px;
          //left: 0px;
          right: 10px;
          text-align: right;
        }
      }
    }
  }
}

</style>
