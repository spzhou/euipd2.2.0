<!--
 * Author: 周朔鹏
 * Date: 2025-04-05
 * Email: 391902958@qq.com
 * Website: https://www.euipd.com
-->

<template>
  <div>
    <h4 class="lighter">
      <i class="ace-icon fa fa-hand-o-right icon-animated-hand-pointer blue"></i>
      课程: {{chapter.courseName}}
      <i class="ace-icon fa fa-hand-o-right icon-animated-hand-pointer blue"></i>
      篇章: {{chapter.name}}
    </h4>
    <div class="hr hr-18 hr-dotted hr-double"></div>
    <div style="display: flex; flex-direction: row; justify-content: space-between; align-items: center;">
      <div>
        <pagination ref="pagination" v-bind:list="list" v-bind:itemCount="6"></pagination>
      </div>
      <div style="margin-right: 20px;">
        <button v-on:click="add()" class="btn btn-white btn-default btn-round">
          <i class="ace-icon fa fa-edit"></i>
          新增
        </button>
      </div>
    </div>

    <el-table
        :data="sections"
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
          prop="title"
          align="center"
          width="300"
          show-overflow-tooltip
          label="标题">
      </el-table-column>
      <el-table-column
          prop="vod"
          align="center"
          width="280"
          label="VOD">
      </el-table-column>
      <el-table-column
          prop="time"
          :formatter="secondFormat"
          align="center"
          sortable
          label="时长">
      </el-table-column>
      <el-table-column
          prop="charge"
          :formatter="formatCharge"
          align="center"
          sortable
          label="收费">
      </el-table-column>
      <el-table-column
          prop="sort"
          align="center"
          sortable
          label="排序">
      </el-table-column>
      <el-table-column
          fixed="right"
          label="操作"
          align="center"
          width="260">
        <template slot-scope="scope">
          <el-button type="primary" size="small" v-on:click="play(scope.row)">
            <i class="ace-icon fa fa-video-camera bigger-120"></i>预览
          </el-button>
          <el-button type="primary" size="small" v-on:click="edit(scope.row)">
            <i class="ace-icon fa fa-pencil bigger-120"></i>编辑
          </el-button>
          <el-button type="primary" size="small" v-on:click="del(scope.row.id)">
            <i class="ace-icon fa fa-trash-o bigger-120"></i>删除
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <DialogPlayer ref="dialogPlayer"></DialogPlayer>

<!--    添加小节-->
    <el-dialog
        width="23cm"
        :visible.sync="addSectionDlg"
        :before-close="handleClose"
        :close-on-click-modal=false>
      <div slot="title" style="font-size: 20px;">{{ dialogTitle }}</div>
      <div v-if="addSectionDlg">
        <div style="width: 90%; margin-bottom: 50px;">
          <el-form label-position="right" label-width="120px" :model="section">
            <el-form-item label="课程">
              <el-input v-text="chapter.courseName"></el-input>
            </el-form-item>
            <el-form-item label="篇章">
              <el-input v-text="chapter.name"></el-input>
            </el-form-item>
            <el-form-item label="小节标题" required>
              <el-input v-model="section.title"></el-input>
            </el-form-item>
            <el-form-item v-if="!isEdit" label="视频" required>
              <div>
                <vod v-bind:input-id="'video-upload'"
                     v-bind:text="'选择上传文件'"
                     v-bind:suffixs="['mp4']"
                     v-bind:use="FILE_USE.COURSE.key"
                     v-bind:after-upload="afterUpload"></vod>
                <div v-show="section.video" class="row">
                  <div class="col-md-9">
                  </div>
                </div>
              </div>
            </el-form-item>
            <el-form-item label="视频">
              <el-input v-text="section.video"/>
            </el-form-item>
            <el-form-item label="VOD">
              <el-input v-text="section.vod"></el-input>
            </el-form-item>
            <el-form-item label="时长">
              {{ section.time | formatDuration }}
<!--              <el-input v-text="section.time"></el-input> <span>秒</span>-->
            </el-form-item>
<!--            <el-form-item label="收费" required>
              <el-select  v-model="section.charge" filterable placeholder="是否收费">
                <el-option v-for="o in SECTION_CHARGE" :key="o.key" :value="o.key" :label="o.value"></el-option>
              </el-select>
            </el-form-item>-->
            <el-form-item label="顺序" required>
              <el-input v-model="section.sort"/>
            </el-form-item>
            <el-form-item label="视频文本">
              <el-input type="textarea" rows="5" v-model="section.contentHtml"/>
            </el-form-item>
          </el-form>
        </div>
        <div class="modal-footer">
          <button v-if="section.vod" v-on:click="save()" type="button" class="btn btn-primary">保存</button>
          <button v-if="! section.vod" type="button" class="btn btn-primary" disabled >保存</button>
          <button type="button" class="btn btn-default" @click="closeDialog">取消</button>
        </div>
      </div>
    </el-dialog>

  </div>
</template>

<script>
  import Pagination from "../../components/pagination";
  import VodFileAliyun from "@/components/vod-file-aliyun";
  import Vod from "../../components/vod";
  import DialogPlayer from "../dialog-player.vue";
  import Swal from "sweetalert2";
  import {formatDuration, optionKV} from "@/filter/filter";
  export default {
    components: {DialogPlayer, Pagination, VodFileAliyun, Vod},
    name: "course-section",

    props: {
      chapterId:  {
        default: null
      },
    },
    data: function() {
      return {
        chapter: {},
        section: {},
        sections: [],
        SECTION_CHARGE: SECTION_CHARGE,
        FILE_USE: FILE_USE,

        addSectionDlg: false,
        dialogTitle: '',
        isEdit: false,
      }
    },
    mounted: function() {
      let _this = this;
      _this.$refs.pagination.size = 20;
      _this.getChapterInfo();
    },
    methods: {
      formatDuration,

      add() {
        let _this = this;
        _this.section = {};
        _this.addSectionDlg = true;
        _this.dialogTitle = "添加小节";
        _this.isEdit = false;
      },
      edit(section) {
        let _this = this;
        _this.section = $.extend({}, section);
        _this.addSectionDlg = true;
        _this.dialogTitle = "编辑小节";
        _this.isEdit = true;
      },
      closeDialog() {
        let _this = this;
        _this.addSectionDlg = false;
      },
      handleClose(done) {
        done();
      },

      secondFormat(row, column){
        return formatDuration(row.time);
      },
      formatCharge(row) {
        return optionKV(SECTION_CHARGE, row.charge);
      },
      getChapterInfo() {
        let _this = this;
        _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/chapter/getChapterInfo/' + _this.chapterId).then((response)=>{
          let resp = response.data;
          _this.chapter = resp.content;
          _this.list(1);
        })
      },
      list(page) {
        let _this = this;
        _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/section/list', {
          page: page,
          size: _this.$refs.pagination.size,
          chapterId: _this.chapter.id
        }).then((response)=>{
          let resp = response.data;
          _this.sections = resp.content.list;
          _this.$refs.pagination.render(page, resp.content.total);

        })
      },

      save() {
        let _this = this;

        // 保存校验
        if (1 != 1
          || !Validator.require(_this.section.title, "标题",Swal)
          || !Validator.require(_this.section.video, "视频",Swal)
          || !Validator.require(_this.section.vod, "VideoId",Swal)
          || !Validator.require(_this.section.time, "时长",Swal)
          || !Validator.require(_this.section.sort, "顺序",Swal)
          || !Validator.isNumber(_this.section.time, "时长",Swal)
          || !Validator.isNumber(_this.section.sort, "顺序",Swal)
          || !Validator.length(_this.section.title, "标题", 1, 50,Swal)
          || !Validator.length(_this.section.video, "视频", 1, 500,Swal)
          || !Validator.length(_this.section.vod, "VideoId", 1, 100,Swal)
        ) {
          return;
        }
        if(_this.section.contentHtml){
          Validator.length(_this.section.contentHtml, "文本内容", 1, 15000)
        }
        _this.section.chapterId = _this.chapter.id;
        _this.section.charge = SECTION_CHARGE.FREE.key;

        _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/section/save', _this.section).then((response)=>{
          let resp = response.data;
          if (resp.success) {
            _this.closeDialog();
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
            _this.$ajax.delete(process.env.VUE_APP_SERVER + '/customer/section/delete/' + id).then((response)=>{
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
            });
          }
        })
      },

      afterUpload(resp) {
        let _this = this;
        let video = resp.path;
        let vod = resp.vod;
        let title = resp.title;
        let time = resp.time;
        _this.section.video = video;
        _this.section.vod = vod;
        // _this.section.title = title;
        _this.section.time = time;
        // 解决不能实时预览的问题
        _this.$forceUpdate();
      },

      /**
       * 播放视频
       * @param section
       */
      play(section) {
        let _this = this;
        _this.$refs.dialogPlayer.playVod(section.vod);
      }
    }
  }
</script>

<style scoped lang="scss">
  video {
    width: 100%;
    height: auto;
    margin-top: 10px;
  }
</style>
