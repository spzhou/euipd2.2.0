<!--
  工作区页签内容区。固定页签传 page-id，动态页签传 prefix 和 index。
  未选中时不调用插槽，子页面不会挂载，也不会发请求。
-->
<script>
import {tabId} from "@/shared/dynamic-tab";

export default {
  name: "dynamic-tab-pane",
  props: {
    index: {
      type: Number,
      default: null,
    },
    prefix: {
      type: String,
      default: "",
    },
    pageId: {
      type: String,
      default: "",
    },
    activePageId: {
      type: String,
      default: "",
    },
  },
  computed: {
    paneId() {
      if (this.pageId) {
        return this.pageId;
      }
      return tabId(this.prefix, this.index);
    },
    isActive() {
      return this.activePageId === this.paneId;
    },
  },
  render(h) {
    const children = this.isActive && this.$scopedSlots.default
        ? this.$scopedSlots.default({active: true})
        : [];
    return h("div", {
      attrs: {id: this.paneId},
      class: {
        "tab-pane": true,
        fade: true,
        active: this.isActive,
        in: this.isActive,
      },
    }, children);
  },
}
</script>
