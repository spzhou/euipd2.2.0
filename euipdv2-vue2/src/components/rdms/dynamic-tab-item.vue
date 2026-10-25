<!--
  工作区导航上的动态页签。标题由父页面用默认插槽给出。
-->
<template>
  <li :class="{active : activePageId === pageId}">
    <a @click="$emit('select', index)">
      <TabButton
          :index-in="index"
          :close-function="close">
        <slot></slot>
      </TabButton>
    </a>
  </li>
</template>

<script>
import TabButton from "@/components/rdms/tab-button";
import {tabId} from "@/shared/dynamic-tab";

export default {
  name: "dynamic-tab-item",
  components: {
    TabButton,
  },
  props: {
    index: {
      type: Number,
      required: true,
    },
    activePageId: {
      type: String,
      default: '',
    },
    prefix: {
      type: String,
      required: true,
    },
    close: {
      type: Function,
      default: null,
    },
  },
  computed: {
    pageId() {
      return tabId(this.prefix, this.index);
    },
  },
}
</script>
