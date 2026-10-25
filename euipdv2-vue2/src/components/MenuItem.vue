<!--
 * Author: 周朔鹏
 * Date: 2025-08-04
 * Email: 391902958@qq.com
 * Website: https://www.euipd.com
-->

<template>
  <!-- 完全复制原始HTML结构，保证与原始HTML100%一致 -->
  <div class="ca-menu-item" :id="id" :class="containerClass">
    <router-link :to="route" :style="linkStyle">
      <div class="light"></div>
      <div class="left-icon">
        <i :class="iconClass" aria-hidden="true" :style="iconStyle"></i>
      </div>
      <div class="menu-content-fix">
        <el-badge v-if="badge" :is-dot="badgeIsDot" :value="badgeValue">
          {{ title }}
        </el-badge>
        <template v-else>{{ title }}</template>
        <i v-if="hasList" 
           style="position: absolute; top: 12px; left: 85px;" 
           class="fa fa-list-alt" 
           aria-hidden="true"></i>
      </div>
      <div class="right-icon">
        <i class="fa fa-chevron-right iconfont" aria-hidden="true"></i>
      </div>
    </router-link>
  </div>
</template>

<script>
export default {
  name: "MenuItem",
  props: {
    id: {
      type: String,
      required: true
    },
    route: {
      type: String,
      required: true
    },
    icon: {
      type: String,
      required: true
    },
    title: {
      type: String,
      required: true
    },
    linkStyle: {
      type: Object,
      default: () => ({})
    },
    iconStyle: {
      type: Object,
      default: () => ({ fontSize: '19px' })
    },
    badge: {
      type: Boolean,
      default: false
    },
    badgeIsDot: {
      type: Boolean,
      default: false
    },
    badgeValue: {
      type: [String, Number],
      default: ''
    },
    hasList: {
      type: Boolean,
      default: false
    }
  },
  computed: {
    iconClass() {
      return this.icon;
    },
    containerClass() {
      // 确保容器具有正确的class，与原始HTML保持一致
      return '';
    }
  }
}
</script>

<style lang="scss">
/* 
 * 重要：不使用scoped，确保样式能正确继承
 * 这里引入相同的sidebar样式以确保一致性
 */
@import "../../public/static/scss/sidebar.scss";
@import "../../public/static/scss/config.scss";

/* 强制确保组件样式与原始HTML一致 */
.ca-menu-item {
  height: 40px !important;
  display: flex !important;
  position: relative !important;
  transition: 0.5s !important;
  border-radius: 6px !important;
}

.ca-menu-item:hover {
  background: rgba(255, 255, 255, 0.1) !important;
}

.ca-menu-item.active {
  background: rgba(255, 255, 255, 0.1) !important;
}

.ca-menu-item .light {
  width: 6px !important;
  height: 40px !important;
  background: #62cb44 !important;
  position: absolute !important;
  left: 0 !important;
  transition: 0.5s !important;
  border-top-right-radius: 4px !important;
  border-bottom-right-radius: 4px !important;
  opacity: 0 !important;
}

.ca-menu-item:hover .light {
  opacity: 1 !important;
}

.ca-menu-item .left-icon {
  position: absolute !important;
  left: 15px !important;
  width: 40px !important;
  height: 40px !important;
  display: flex !important;
  justify-content: center !important;
  align-items: center !important;
  color: rgba(245, 253, 255, 0.5) !important;
}

.ca-menu-item .menu-content-fix {
  position: absolute !important;
  left: 60px !important;
  width: 120px !important;
  height: 40px !important;
  display: flex !important;
  justify-content: flex-start !important;
  align-items: center !important;
  transition: 0.5s !important;
  overflow: hidden !important;
  opacity: 1 !important;
}

.ca-menu-item .right-icon {
  position: absolute !important;
  right: 15px !important;
  width: 20px !important;
  height: 40px !important;
  display: flex !important;
  justify-content: center !important;
  align-items: center !important;
  transition: 0.5s !important;
  opacity: 0 !important;
}

.ca-menu-item:hover .right-icon {
  width: 20px !important;
  display: flex !important;
  justify-content: center !important;
  align-items: center !important;
  opacity: 1 !important;
}

.ca-menu-item.active .right-icon {
  width: 20px !important;
  display: flex !important;
  justify-content: center !important;
  align-items: center !important;
  opacity: 1 !important;
}

/* MenuItem标题颜色设置 - 覆盖全局a标签样式 */
.ca-menu-item a {
  color: $menuTitleColor !important;
  text-decoration: none !important;
  outline: none !important;
}

/* 覆盖 bootstrap a:focus 的 -webkit-focus-ring，避免图标/标题出现白色框线 */
.ca-menu-item a:hover,
.ca-menu-item a:focus,
.ca-menu-item a:focus-visible,
.ca-menu-item a:active {
  color: $menuTitleHoverColor !important;
  text-decoration: none !important;
  outline: none !important;
  box-shadow: none !important;
}

.ca-menu-item.active a {
  color: $menuTitleHoverColor !important;
}

/* 确保badge内的文字也使用正确的颜色 */
.ca-menu-item .el-badge__content {
  color: $menuTitleColor !important;
}
</style>
