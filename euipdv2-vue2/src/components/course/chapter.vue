<!--
 * Author: 周朔鹏
 * Date: 2025-03-14
 * Email: 391902958@qq.com
 * Website: https://www.euipd.com
-->

<template>
  <div>
    <h4 class="lighter">
      课程名称: {{ course.name }}
    </h4>
    <div class="hr hr-18 hr-dotted hr-double"></div>
    <div style="display: flex; flex-direction: row; justify-content: space-between; align-items: center;">
      <div>
        <pagination ref="pagination" v-bind:list="list" v-bind:itemCount="6"></pagination>
      </div>
      <div style="margin-right: 20px;">
        <button v-on:click="add()" class="btn btn-white btn-default btn-round">
          <i class="ace-icon fa fa-edit"></i>
          添加篇章
        </button>
      </div>
    </div>
    <div style="width: 100%;">
      <el-table
          :data="chapters"
          row-key="id"
          border
          stripe
          style="width: 100%;"
          highlight-current-row>
        <el-table-column
            prop="id"
            label="ID"
            align="center"
            width="150">
        </el-table-column>
        <el-table-column
            prop="name"
            align="center"
            show-overflow-tooltip
            label="篇章名称">
        </el-table-column>
        <el-table-column
            prop="sort"
            align="center"
            sortable
            label="篇章顺序">
        </el-table-column>
        <el-table-column
            fixed="right"
            label="操作"
            align="center"
            width="300">
          <template slot-scope="scope">
            <el-button type="primary" size="small" v-on:click="toSection(scope.row)">
              小节
            </el-button>&nbsp;
            <el-button type="primary" size="small" v-on:click="edit(scope.row)">
              编辑
            </el-button>&nbsp;
            <el-button type="warning" size="small" v-on:click="del(scope.row.id)">
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>
    <el-dialog
        width="23cm"
        :visible.sync="addChapterDlg"
        :before-close="handleClose"
        :close-on-click-modal=false>
      <div slot="title" style="font-size: 20px;">{{ dialogTitle }}</div>
      <div v-if="addChapterDlg">
        <div style="width: 70%; margin-bottom: 50px;">
          <form class="form-horizontal">
            <div class="form-group">
              <label class="col-sm-5 control-label">课程名称</label>
              <div class="col-sm-7">
                <p class="form-control-static">{{ course.name }}</p>
              </div>
            </div>
            <div class="form-group">
              <label class="col-sm-5 control-label">篇章名称</label>
              <div class="col-sm-7">
                <input v-model="chapter.name" class="form-control" placeholder="名称">
              </div>
            </div>
            <div class="form-group">
              <label class="col-sm-5 control-label">篇章顺序</label>
              <div class="col-sm-7">
                <input v-model="chapter.sort" class="form-control" placeholder="顺序">
              </div>
            </div>
          </form>
        </div>
        <div class="modal-footer">
          <button v-on:click="save()" type="button" class="btn btn-primary">保存</button>
          <button type="button" class="btn btn-default" @click="closeDialog">取消</button>
        </div>
      </div>
    </el-dialog>

  </div>
</template>

<script>
import Pagination from "../../components/pagination";
import Swal from "sweetalert2";

export default {
  components: {Pagination},
  name: "chapter",

  props: {
    course: {
      default: null,
    },
    openSection: {
      type: Function,
      default: null
    },
  },
  data: function () {
    return {
      chapter: {},
      chapters: [],

      addChapterDlg: false,
      dialogTitle: '',
    }
  },
  mounted: function () {
    let _this = this;
    _this.$refs.pagination.size = 5;
    _this.list(1);

  },
  methods: {
    add() {
      let _this = this;
      _this.chapter = {};
      _this.chapter.courseId = _this.course.id;
      _this.addChapterDlg = true;
      _this.dialogTitle = "添加章节";
    },
    closeDialog() {
      let _this = this;
      _this.addChapterDlg = false;
    },
    handleClose(done) {
      done();
    },

    /**
     * 点击【编辑】
     */
    edit(chapter) {
      let _this = this;
      _this.chapter = $.extend({}, chapter);
      _this.addChapterDlg = true;
    },

    /**
     * 列表查询
     */
    list(page) {
      let _this = this;
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/chapter/list', {
        page: page,
        size: _this.$refs.pagination.size,
        courseId: _this.course.id
      }).then((response) => {
        let resp = response.data;
        _this.chapters = resp.content.list;
        _this.$refs.pagination.render(page, resp.content.total);

      })
    },

    /**
     * 点击【保存】
     */
    save() {
      let _this = this;

      // 保存校验
      if (
          !Validator.require(_this.chapter.name, "名称")
          || !Validator.require(_this.chapter.sort, "排序")
          || !Validator.length(_this.chapter.courseId, "课程ID", 1, 8)
      ) {
        return;
      }

      var numReg = /^[0-9]*$/
      var numRe = new RegExp(numReg)
      if (!numRe.test(_this.chapter.sort)) {
        Toast.success("排序请输入数字！")
        return;
      }
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/chapter/save', _this.chapter).then((response) => {
        let resp = response.data;
        if (resp.success) {
          _this.list(1);
          _this.addChapterDlg = false;
          Swal.fire({
            position: 'top-end',
            icon: 'success',
            title: "保存成功",
            showConfirmButton: false,
            timer: 1000
          })
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
      Confirm.show(Swal,"删除篇章后不可恢复，确认删除？", function () {
        _this.$ajax.delete(process.env.VUE_APP_SERVER + '/customer/chapter/delete/' + id).then((response) => {
          let resp = response.data;
          if (resp.success) {
            _this.list(1);
            Swal.fire({
              position: 'top-end',
              icon: 'success',
              title: "删除成功",
              showConfirmButton: false,
              timer: 1000
            })
          }
        })
      });
    },

    /**
     * 点击【小节】
     */
    toSection(chapter) {
      let _this = this;
      _this.openSection(chapter);
    }
  }
}
</script>
