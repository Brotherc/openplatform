<template>
  <a-layout-header class="header">
    <div class="logo-area">
      <svg class="logo" viewBox="0 0 40 40" fill="none" xmlns="http://www.w3.org/2000/svg">
        <circle cx="20" cy="20" r="20" fill="#1677ff"/>
        <text x="9" y="27" font-size="16" font-family="Arial, Helvetica, sans-serif" fill="#fff" font-weight="bold">OP</text>
      </svg>
      <span class="title">开放平台</span>
    </div>
    <a-menu
      mode="horizontal"
      :selected-keys="[selectedMenu]"
      @click="onMenuClick"
      class="menu"
    >
      <a-menu-item key="home">首页</a-menu-item>
      <a-sub-menu key="docs">
        <template #title>
          文档中心
        </template>
        <a-menu-item key="docs" style="display:none"></a-menu-item>
        <a-menu-item key="intro">平台简介</a-menu-item>
        <a-menu-item key="api">API文档</a-menu-item>
      </a-sub-menu>
      <a-menu-item key="console">控制台</a-menu-item>
    </a-menu>
    <a-button type="primary" class="login-btn">登录</a-button>
  </a-layout-header>
</template>

<script lang="ts" setup>
import { computed, watchEffect } from 'vue';
import { useRouter, useRoute } from 'vue-router';

const router = useRouter();
const route = useRoute();

const selectedMenu = computed(() => {
  if (route.path === '/' || route.path === '/home') return 'home';
  if (route.path.startsWith('/article')) return 'docs';
  if (route.path.startsWith('/console')) return 'console';
  return '';
});

// 调试：打印 selectedMenu 和 route.path
watchEffect(() => {
  // eslint-disable-next-line no-console
  console.log('route.path:', route.path, 'selectedMenu:', selectedMenu.value);
});

function onMenuClick({ key }) {
  switch (key) {
    case 'home':
      router.push('/');
      break;
    case 'intro':
    case 'api':
      router.push('/article');
      break;
    case 'console':
      router.push('/console');
      break;
  }
}
</script>

<style scoped>
.header {
  display: flex;
  align-items: center;
  background: #fff;
  box-shadow: 0 2px 8px #f0f1f2;
  padding: 0 40px;
  height: 64px;
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  z-index: 1000;
}
.logo-area {
  display: flex;
  align-items: center;
  margin-right: 32px;
}
.logo {
  height: 36px;
  width: 36px;
  margin-right: 10px;
  display: block;
}
.title {
  font-size: 20px;
  font-weight: bold;
  color: #222;
}
.menu {
  flex: 1;
  background: transparent;
  border-bottom: none;
  display: flex;
  align-items: center;
  gap: 0;
}
.login-btn {
  margin-left: 16px;
  height: 36px;
  border-radius: 6px;
  font-weight: 500;
}
</style>

<!-- 全局样式请放到 App.vue 或 main.css，不要加 scoped -->
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