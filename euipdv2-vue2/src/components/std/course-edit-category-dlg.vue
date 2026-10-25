<!--
 * Author: 周朔鹏
 * Date: 2025-03-11
 * Email: 391902958@qq.com
 * Website: https://www.euipd.com
-->

<template>
  <div>
    <div class="dialog-container">
      <div style="display: flex; flex-direction: row; justify-content: center; align-items: center;">
        <div style="width: 500px; margin-bottom: 30px;">
          <el-form
              label-width="130px"
              :show-message=false
              class="dialog-width">
            <el-row>
              <el-col>
                <el-form-item label="父类名称">
                  <el-col :span="19">
                    <div v-if="category.id==='00000000'">
                      <div v-text="parentCategory.name"/>
                    </div>
                    <div v-else>
                      <el-select v-model="category.parent" placeholder="请选择">
                        <el-option
                            v-for="item in levelTop"
                            :key="item.id"
                            :label="item.name"
                            :value="item.id">
                        </el-option>
                      </el-select>
                    </div>
                  </el-col>
                </el-form-item>
              </el-col>
            </el-row>
            <el-row>
              <el-col>
                <el-form-item label="分类名称" required>
                  <el-col :span="19">
                    <el-input v-model="category.name"></el-input>
                  </el-col>
                </el-form-item>
              </el-col>
            </el-row>
            <el-row>
              <el-col>
                <el-form-item label="顺序号">
                  <el-col :span="19">
                    <el-input v-model="category.sort"></el-input>
                  </el-col>
                </el-form-item>
              </el-col>
            </el-row>
          </el-form>
        </div>
      </div>
      <div class="clearfix form-actions">
        <div style="display: flex; justify-content: center;">
          <button  type="button" v-on:click="save()" class="ca-btn-primary">保 存</button>
          <button type="button" v-on:click="close()" class="ca-btn-primary">取 消</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import Swal from "sweetalert2";

export default {
  components: {
  },
  name: "course-edit-category-dlg",

  props: {
    category: {
      default: null,
    },
    closeDlg: {
      type: Function,
      default: null
    },
  },

  data() {
    return {
      loginUser: {},
      loginCustomer: {},

      parentCategory: {
        id: '00000000',
        name: '根分类',
        parent: '00000000',
        sort: 0,
      },
      levelTop: [],
    };
  },

  computed: {
  },

  mounted: function () {
    let _this = this;
    _this.getLoginUser();
    if(_this.category && _this.category.parent){
      _this.getLevel1List();
    }
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

    getLevel1List() {
      let _this = this;
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/course/category/getLevel1List').then((response)=>{
        let resp = response.data;
        if (resp.success) {
          _this.levelTop = resp.content;
        }
        let rootCategory = {
          id: '00000000',
          name: '根分类',
          parent: '00000000',
          sort: 0,
        }
        _this.levelTop.push(rootCategory);
      })
    },

    save() {
      let _this = this;
      // 保存校验
      if (1 !== 1
          || !Validator.require(_this.category.parent, "父级分类")
          || !Validator.require(_this.category.name, "名称")
          || !Validator.length(_this.category.name, "名称", 1, 50)
      ) {
        return;
      }

      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/course/category/save', _this.category).then((response)=>{
        let resp = response.data;
        if (resp.success) {
          _this.$bus.$emit('category-click-level1-item', _this.category );
          Toast.success("保存成功", Swal);
          _this.close();
        }
      })
    },

    close(){
      let _this = this;
      _this.closeDlg();
    },
  },
}
</script>

<style lang="scss" scoped>
@import '../../../public/static/scss/caipd';

.dialog-width{
  width: 100%;
}

</style>
