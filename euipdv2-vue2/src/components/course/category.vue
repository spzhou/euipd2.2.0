<!--
 * Author: 周朔鹏
 * Date: 2025-03-11
 * Email: 391902958@qq.com
 * Website: https://www.euipd.com
-->

<template>
  <div>
    <el-row>
      <el-col :span="12">
        <div style="display: flex; flex-direction: row; justify-content: flex-start; align-items: center;">
          <el-button type="primary" size="small" @click="addCatalogLevel1Dlg">
            <i class="ace-icon fa fa-edit"></i>
            添加一级分类
          </el-button>
        </div>
        <div class="table-height">
          <el-table
              :data="levelTop"
              row-key="id"
              border
              default-expand-all
              stripe
              style="width: 100%;"
              highlight-current-row
              @row-click="rowClick">
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
                label="一级分类">
            </el-table-column>
            <el-table-column
                prop="sort"
                width="80"
                align="center"
                sortable
                label="顺序">
            </el-table-column>
            <el-table-column
                fixed="right"
                label="操作"
                align="center"
                width="180">
              <template slot-scope="scope">
                <el-button type="primary" plain size="small" @click.stop="editCatalogDlg(scope.row)">
                  编辑
                </el-button>
                <el-button type="warning" plain size="small" @click.stop="del(scope.row.id)">
                  删除
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </el-col>
      <el-col :span="12">
        <div style="padding-left: 10px;">
          <div style="display: flex; flex-direction: row; justify-content: flex-start; align-items: center;">
            <el-button type="primary" size="small" @click="addCatalogLevel2Dlg">
              <i class="ace-icon fa fa-edit"></i>
              添加二级分类
            </el-button>
          </div>
          <div class="table-height">
            <el-table
                :data="level2"
                row-key="id"
                border
                default-expand-all
                stripe
                style="width: 100%;"
                highlight-current-row
                @row-click="rowClick">
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
                  label="二级分类">
              </el-table-column>
              <el-table-column
                  prop="sort"
                  width="80"
                  align="center"
                  sortable
                  label="顺序">
              </el-table-column>
              <el-table-column
                  fixed="right"
                  label="操作"
                  align="center"
                  width="180">
                <template slot-scope="scope">
                  <el-button type="primary" plain size="small" @click.stop="editCatalogDlg(scope.row)">
                    编辑
                  </el-button>
                  <el-button type="warning" plain size="small" @click.stop="del(scope.row.id)">
                    删除
                  </el-button>
                </template>
              </el-table-column>
            </el-table>
          </div>
        </div>
      </el-col>
    </el-row>

    <!--添加一级分类-->
    <el-dialog
        width="23cm"
        :visible.sync="showAddCatalogLevel1Dlg"
        :before-close="handleClose"
        :close-on-click-modal=false>
      <div slot="title" style="font-size: 20px;">{{ dialogTitle }}</div>
      <div v-if="showAddCatalogLevel1Dlg">
        <CourseAddCategoryDlg
            :parent-category="category"
            :level-top="null"
            :close-dlg="closeDialog"
        ></CourseAddCategoryDlg>
      </div>
    </el-dialog>

    <!--添加二级分类-->
    <el-dialog
        width="23cm"
        :visible.sync="showAddCatalogLevel2Dlg"
        :before-close="handleClose"
        :close-on-click-modal=false>
      <div slot="title" style="font-size: 20px;">{{ dialogTitle }}</div>
      <div v-if="showAddCatalogLevel2Dlg">
        <CourseAddCategoryDlg
            :key="random"
            :parent-category="parentCategory"
            :level-top="levelTop"
            :close-dlg="closeDialog"
        ></CourseAddCategoryDlg>
      </div>
    </el-dialog>

    <!--编辑分类-->
    <el-dialog
        width="23cm"
        :visible.sync="showEditCatalogDlg"
        :before-close="handleClose"
        :close-on-click-modal=false>
      <div slot="title" style="font-size: 20px;">{{ dialogTitle }}</div>
      <div v-if="showEditCatalogDlg">
        <CourseEditCategoryDlg
            :key="editCategory.id"
            :category="editCategory"
            :close-dlg="closeDialog"
        ></CourseEditCategoryDlg>
      </div>
    </el-dialog>

  </div>
</template>

<script>
  import Swal from "sweetalert2";
  import CourseAddCategoryDlg from "@/components/std/course-add-category-dlg.vue";
  import CourseEditCategoryDlg from "@/components/std/course-edit-category-dlg.vue";

  export default {
    components: {
      CourseAddCategoryDlg,
      CourseEditCategoryDlg,
    },

    name: "category",
    data: function() {
      return {
        category: {},
        editCategory: {},
        parentCategory: {},
        categorys: [],

        levelTop: [],
        level1: [],
        level2: [],
        active: {},

        showAddCatalogLevel1Dlg: false,
        showAddCatalogLevel2Dlg: false,
        showEditCatalogDlg: false,
        dialogTitle: '',
        random: Math.random(),
      }
    },

    computed: {
    },

    mounted: function() {
      let _this = this;
      _this.all();

      _this.$bus.$on('category-click-level1-item', (data) => {
        if(data){
          _this.all(data);
        }
      });

    },
    methods: {
      rowClick(row, column, e){
        let _this = this;
        _this.level2 = _this.level1.find(item => item.id === row.id)?.children;
        _this.parentCategory = row;
      },
      addCatalogLevel1Dlg() {
        let _this = this;
        _this.category = {
          id: "00000000",
          parent: "00000000",
          name: "根分类",
          sort: 0,
        };
        _this.showAddCatalogLevel1Dlg = true;
        _this.dialogTitle = "添加一级分类";
      },
      addCatalogLevel2Dlg() {
        let _this = this;
        _this.random = Math.random();
        _this.showAddCatalogLevel2Dlg = true;
        _this.dialogTitle = "添加二级分类";
      },
      editCatalogDlg(category) {
        let _this = this;
        _this.editCategory = category;
        _this.showEditCatalogDlg = true;
        _this.dialogTitle = "编辑分类";
      },
      handleClose(done) {
        done();
      },
      closeDialog() {
        let _this = this;
        _this.showAddCatalogLevel1Dlg = false;
        _this.showAddCatalogLevel2Dlg = false;
        _this.showEditCatalogDlg = false;
      },

      all(data) {
        let _this = this;
        _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/course/category/all').then((response) => {
          let resp = response.data;
          _this.categorys = resp.content;

          // 将所有记录格式化成树形结构
          _this.level1 = [];
          _this.levelTop = []; // 初始化 levelTop

          for (let i = 0; i < _this.categorys.length; i++) {
            let c = _this.categorys[i];
            if (c.parent === '00000000') {
              // 去掉 children 属性
              let itemWithoutChildren = $.extend({}, c);
              delete itemWithoutChildren.children;
              _this.level1.push(c);
              _this.levelTop.push(itemWithoutChildren); // 加入到 levelTop

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
          // 遍历查看Level的每一项,如果没有children属性,则添加children并设置为 []
          for (let i = 0; i < _this.level1.length; i++) {
            let item = _this.level1[i];
            if (Tool.isEmpty(item.children)) {
              item.children = [];
            }
          }

          if (_this.level1 && _this.level1.length > 0) {
            _this.level2 = _this.level1[0].children;
            _this.parentCategory = _this.level1[0];
          }

          //如果是emit刷新的情况
          if(data){
            _this.rowClick(data, null, null);
          }
        })
      },

      /**
       * 点击删除
       */
      del(id) {
        let _this = this;
        Swal.fire({
          title: '确认',
          text: "是否确认删除分类?",
          icon: 'warning',
          showCancelButton: true,
          confirmButtonColor: '#409EFF',
          cancelButtonColor: '#909399',
          confirmButtonText: '确认!',
          cancelButtonText: '取消!'
        }).then((result) => {
          if (result.isConfirmed) {
            _this.$ajax.delete(process.env.VUE_APP_SERVER + '/customer/course/category/delete/' + id).then((response)=>{
              let resp = response.data;
              if (resp.success) {
                _this.all();
                Toast.success("删除成功", Swal);
              }
            })
          }
        }).catch(_ => {
        });
      },

    }
  }
</script>

<style lang="scss" scoped>
  .active td {
    background-color: #d6e9c6 !important;
  }

  .table-height {
    display: block;
    width: 100%;
    height: calc(100vh - 270px);
    overflow-y: auto;
  }

</style>
