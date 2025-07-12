<script lang="ts" setup>
import Header from './components/Header.vue';
</script>

<template>
  <Header />
  <router-view />
</template>

<style scoped>
.logo {
  height: 6em;
  padding: 1.5em;
  will-change: filter;
  transition: filter 300ms;
}
.logo:hover {
  filter: drop-shadow(0 0 2em #646cffaa);
}
.logo.vue:hover {
  filter: drop-shadow(0 0 2em #42b883aa);
}
</style>

<style>
:deep(.ant-menu-horizontal > .ant-menu-item),
:deep(.ant-menu-horizontal > .ant-menu-submenu) {
  background: transparent !important;
}
:deep(.ant-menu-horizontal > .ant-menu-item:hover),
:deep(.ant-menu-horizontal > .ant-menu-item-active),
:deep(.ant-menu-horizontal > .ant-menu-item-open),
:deep(.ant-menu-horizontal > .ant-menu-item-selected),
:deep(.ant-menu-horizontal > .ant-menu-submenu:hover),
:deep(.ant-menu-horizontal > .ant-menu-submenu-active),
:deep(.ant-menu-horizontal > .ant-menu-submenu-open),
:deep(.ant-menu-horizontal > .ant-menu-submenu-selected),
:deep(.ant-menu-horizontal > .ant-menu-submenu-title:hover),
:deep(.ant-menu-horizontal > .ant-menu-submenu-title-active),
:deep(.ant-menu-horizontal > .ant-menu-submenu-title-open),
:deep(.ant-menu-horizontal > .ant-menu-submenu-title-selected) {
  background: transparent !important;
  color: #1677ff !important;
}
:deep(.ant-menu-item-selected) {
  background: transparent !important;
  color: #1677ff !important;
}
:root :deep(.ant-menu-item-selected),
:root :deep(.ant-menu-item-active),
:root :deep(.ant-menu-item:hover),
:root :deep(.ant-menu-submenu-title-selected),
:root :deep(.ant-menu-submenu-title-active),
:root :deep(.ant-menu-submenu-title:hover),
:root :deep(.ant-menu-vertical .ant-menu-item-selected),
:root :deep(.ant-menu-vertical .ant-menu-item-active),
:root :deep(.ant-menu-vertical .ant-menu-item:hover),
:root :deep(.ant-menu-vertical .ant-menu-submenu-title-selected),
:root :deep(.ant-menu-vertical .ant-menu-submenu-title-active),
:root :deep(.ant-menu-vertical .ant-menu-submenu-title:hover) {
  background: transparent !important;
  color: #1677ff !important;
}
</style>
