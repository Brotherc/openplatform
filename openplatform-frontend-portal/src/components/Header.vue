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
    <a-button type="default" class="register-btn">注册</a-button>
    <a-button type="primary" class="login-btn" @click="showLoginModal">登录</a-button>
  </a-layout-header>

  <!-- 登录弹窗 -->
  <a-modal
    v-model:open="loginModalVisible"
    title="用户登录"
    :footer="null"
    width="400px"
    centered
  >
    <a-form
      :model="loginForm"
      name="login"
      @finish="onLoginFinish"
      @finishFailed="onLoginFinishFailed"
      autocomplete="off"
      class="login-form"
    >
      <a-form-item
        name="username"
        :rules="[{ required: true, message: '请输入用户名' }]"
      >
        <a-input
          v-model:value="loginForm.username"
          placeholder="用户名"
          size="large"
        >
          <template #prefix>
            <UserOutlined class="site-form-item-icon" />
          </template>
        </a-input>
      </a-form-item>

      <a-form-item
        name="password"
        :rules="[{ required: true, message: '请输入密码' }]"
      >
        <a-input-password
          v-model:value="loginForm.password"
          placeholder="密码"
          size="large"
        >
          <template #prefix>
            <LockOutlined class="site-form-item-icon" />
          </template>
        </a-input-password>
      </a-form-item>

      <a-form-item>
        <a-form-item name="remember" no-style>
          <a-checkbox v-model:checked="loginForm.remember">记住密码</a-checkbox>
        </a-form-item>
        <a class="login-form-forgot" href="">忘记密码</a>
      </a-form-item>

      <a-form-item>
        <a-button
          :loading="loginLoading"
          type="primary"
          html-type="submit"
          class="login-form-button"
          size="large"
        >
          登录
        </a-button>
      </a-form-item>
    </a-form>
  </a-modal>
</template>

<script lang="ts" setup>
import { ref, computed, onMounted, reactive } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import { UserOutlined, LockOutlined } from '@ant-design/icons-vue';
import { message } from 'ant-design-vue';
import axios from 'axios';

const router = useRouter();
const route = useRoute();
const menuList = ref<any[]>([]);

// 登录弹窗相关
const loginModalVisible = ref(false);
const loginLoading = ref(false);
const loginForm = reactive({
  username: '',
  password: '',
  remember: true
});

interface LoginResponse {
  success: boolean;
  code: number;
  message: string | null;
  data: {
    userId: number;
    username: string;
    status: number;
    nickname: string | null;
    createBy: number;
    createTime: number;
    updateBy: number;
    updateTime: number;
    token: string;
  };
}

// 显示登录弹窗
const showLoginModal = () => {
  loginModalVisible.value = true;
};

// 登录提交
const onLoginFinish = async (values: any) => {
  loginLoading.value = true;
  try {
    const response = await axios.post<LoginResponse>(`${import.meta.env.VITE_API_BASE_URL}/user/login`, {
      username: values.username,
      password: values.password
    });

    if (response.data && response.data.code === 0 && response.data.success) {
      message.success('登录成功');
      
      // 保存用户信息到localStorage
      localStorage.setItem('userInfo', JSON.stringify(response.data.data));
      localStorage.setItem('access_token', response.data.data.token);
      
      // 关闭弹窗
      loginModalVisible.value = false;
      
      // 重置表单
      Object.assign(loginForm, {
        username: '',
        password: '',
        remember: true
      });
      
      // 可以在这里刷新页面或更新用户状态
      window.location.reload();
    } else {
      message.error(response.data.message || '登录失败，用户名或密码错误');
    }
  } catch (error: any) {
    console.error('登录失败:', error);
    message.error('登录失败，请稍后重试');
  } finally {
    loginLoading.value = false;
  }
};

const onLoginFinishFailed = (errorInfo: any) => {
  console.log('Failed:', errorInfo);
};

const fetchMenu = async () => {
  try {
    const res = await axios.get(`${import.meta.env.VITE_API_BASE_URL}/menu/portal/tree`);
    menuList.value = res.data?.data || [];
  } catch (e) {
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
.register-btn {
  margin-left: 16px;
  height: 36px;
  border-radius: 6px;
  font-weight: 500;
}
.login-btn {
  margin-left: 8px;
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
/* 登录弹窗样式 */
.login-form {
  padding: 20px 0;
}

.login-form-forgot {
  float: right;
  color: #1677ff;
}

.login-form-button {
  width: 100%;
}

.site-form-item-icon {
  color: rgba(0, 0, 0, 0.25);
}
</style> 