<!--
  工作区页签外壳：导航、内容区，以及可选的页头（角标）。
-->
<template>
  <div>
    <slot name="header"></slot>
    <div class="row">
      <div class="col-md-12">
        <div class="tabbable">
          <ul class="nav nav-tabs" id="myTab">
            <slot name="nav"></slot>
          </ul>
          <div class="tab-content">
            <slot></slot>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: "workspace-frame",
}
</script>
