<!--
 * Author: 周朔鹏
 * Date: 2023-12-25
 * Email: 391902958@qq.com
 * Website: https://www.euipd.com
-->

<template>
  <div class="pagination" role="group" aria-label="分页" style="display: flex; justify-content: space-between;">
    <div style="margin-bottom: 3px;">
      <button type="button" class="btn-pagination-xm"
              v-bind:disabled="page === 1"
              v-on:click="selectPage(1)">
        1
      </button>
      <button type="button" class="btn-pagination-xm"
              v-bind:disabled="page === 1"
              v-on:click="selectPage(page - 1)">
        <i class="fa fa-chevron-left" aria-hidden="true" style="font-size: 12px;"></i>
      </button>
      <button v-for="p in pages" v-bind:id="'page-' + p"
              type="button" class="btn-pagination-xm"
              v-bind:class="{'btn-pagination-xm active':page === p}"
              v-on:click="selectPage(p)">
        {{p}}
      </button>
      <button type="button" class="btn-pagination-xm"
              v-bind:disabled="page === pageTotal"
              v-on:click="selectPage(page + 1)">
        <i class="fa fa-chevron-right" aria-hidden="true" style="font-size: 12px;"></i>
      </button>
      <button type="button" class="btn-pagination-xm"
              v-bind:disabled="page === pageTotal"
              v-on:click="selectPage(pageTotal)">
        {{pageTotal||1}}
      </button>
    </div>
    &nbsp;
    <div style="margin-right: 5px; padding-top: 3px;">
        每页
        <select v-on:change="selectPageOnChange(1)"
            v-model="size" style="height: 25px;">
            <option value="1">1</option>
            <option value="6">6</option>
            <option value="12">12</option>
            <option value="24">24</option>
            <option value="48">48</option>
            <option value="96">96</option>
        </select>
        条，共【{{total}}】条
    </div>
  </div>
</template>

<script>
  export default {
    name: 'pagination-small',
    props: {
      list: {
        type: Function,
        default: null
      },
      itemCount: Number // 显示的页码数，比如总共有100页，只显示10页，其它用省略号表示
    },
    data: function () {
      return {
        total: 0, // 总行数
        size: 12, // 每页条数
        page: 1, // 当前页码
        pageTotal: 0, // 总页数
        pages: [], // 显示的页码数组
      }
    },
    methods: {
      /**
       * 渲染分页组件
       * @param page
       * @param total
       */
      render(page, total) {
        let _this = this;
        _this.page = page;
        _this.total = total;
        _this.pageTotal = Math.ceil(total / _this.size);
        _this.pages = _this.getPageItems(_this.pageTotal, page, _this.itemCount || 5);
      },

      /**
       * 查询某一页
       * @param page
       */
      selectPage(page) {
        let _this = this;
        if (page < 1) {
          page = 1;
        }
        if (page > _this.pageTotal) {
          page = _this.pageTotal;
        }
        if (this.page !== page) {
          _this.page = page;
          if (_this.list) {
            _this.list(page);
          }
        }
      },
      selectPageOnChange(page) {
        let _this = this;
        if (page < 1) {
          page = 1;
        }
        if (page > _this.pageTotal) {
          page = _this.pageTotal;
        }
        if (_this.list) {
          _this.list(page);
        }
      },


      /**
       * 当前要显示在页面上的页码
       * @param total
       * @param current
       * @param length
       * @returns {Array}
       */
      getPageItems(total, current, length) {
        let items = [];
        if (length >= total) {
          for (let i = 1; i <= total; i++) {
            items.push(i);
          }
        } else {
          let base = 0;
          // 前移
          if (current - 0 > Math.floor((length - 1) / 2)) {
            // 后移
            base = Math.min(total, current - 0 + Math.ceil((length - 1) / 2)) - length;
          }
          for (let i = 1; i <= length; i++) {
            items.push(base + i);
          }
        }
        return items;
      }
    }
  }
</script>

<style lang="scss" scoped>
@import './../../public/static/scss/caipd';
.pagination {
  display: flex;
  flex-direction: row;
  justify-content: space-evenly;
  align-items: center;
  font-size: 13px;
  margin-top: 0;
  margin-bottom: 10px;
  .btn-pagination-xm {
    width: 30px;
    height: 28px;
    background-color: $defaultBtnBackground !important;
    border: 1px $defaultBtnBackground;
    color: $colorPrimaryFont !important;
    font-weight: 600;
    margin: 0 2px;
    border-radius: 3px;
    &:hover{
      background-color: $defaultBtnHoverBackground !important;
      font-weight: 600;
    }
  }
  .btn-pagination-xm.active {
    background-color: $buttonOnPage !important;
    color: $colorWhite !important;
    font-weight: 600;
    &:hover{
      background-color: $btnHoverBackground !important;
      color: $colorHover !important;
      font-weight: 600;
    }
  }

}
</style>
