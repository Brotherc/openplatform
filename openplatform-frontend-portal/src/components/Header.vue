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
      <template v-for="item in menuList" :key="item.key">
        <a-sub-menu v-if="item.children && item.children.length" :key="item.key">
          <template #title>{{ item.title }}</template>
          <a-menu-item v-for="child in item.children" :key="child.key">{{ child.title }}</a-menu-item>
        </a-sub-menu>
        <a-menu-item v-else :key="item.key">{{ item.title }}</a-menu-item>
      </template>
    </a-menu>
    <a-button type="primary" class="login-btn">登录</a-button>
  </a-layout-header>
</template>

<script lang="ts" setup>
import { ref, computed, onMounted } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import axios from 'axios';

const router = useRouter();
const route = useRoute();
const menuList = ref<any[]>([]);

const fetchMenu = async () => {
  try {
    const res = await axios.get(`${import.meta.env.VITE_API_BASE_URL}/menu/tree/portal`);
    menuList.value = res.data?.data || [];
  } catch (e) {
    // eslint-disable-next-line no-console
    console.error('菜单接口获取失败', e);
  }
};

onMounted(fetchMenu);

// 递归查找当前 path 对应的一级菜单 key
function findSelectedKeyByPath(list: any[], path: string): string {
  for (const item of list) {
    if (item.path === path && item.path) return item.key;
    if (item.children && item.children.length) {
      for (const child of item.children) {
        if (child.path === path && child.path) return item.key;
      }
    }
  }
  // 兜底：如果 path 是 /article，优先 docs
  if (path.startsWith('/article')) {
    const docs = list.find(i => i.title.includes('文档') || i.key === 'docs');
    if (docs) return docs.key;
  }
  return '';
}

const selectedMenu = computed(() => {
  return findSelectedKeyByPath(menuList.value, route.path);
});

function findPathByKey(list: any[], key: string): string | null {
  for (const item of list) {
    if (item.key === key && item.path) return item.path;
    if (item.children && item.children.length) {
      for (const child of item.children) {
        if (child.key === key && child.path) return child.path;
      }
    }
  }
  return null;
}

function onMenuClick({ key }: { key: string }) {
  const path = findPathByKey(menuList.value, key);
  if (path) {
    router.push(path);
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