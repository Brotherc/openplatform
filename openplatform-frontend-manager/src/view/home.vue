<template>
  <a-layout style="min-height: 100vh">
    <a-layout-sider v-model:collapsed="collapsed" :trigger="null" collapsible>
      <div class="logo">开放平台</div>
      <a-menu
          v-model:selectedKeys="selectedKeys"
          v-model:openKeys="openKeys"
          theme="dark"
          mode="inline"
          @click="handleMenuClick"
      >
        <a-sub-menu key="doc-center">
          <template #title>
            <span>
              <book-outlined />
              <span>文档中心</span>
            </span>
          </template>
          <a-menu-item key="group">
            <folder-outlined />
            <span>分组管理</span>
          </a-menu-item>
          <a-menu-item key="article">
            <file-text-outlined />
            <span>文章管理</span>
          </a-menu-item>
        </a-sub-menu>
        <a-sub-menu key="api-center">
          <template #title>
            <span>
              <api-outlined />
              <span>API中心</span>
            </span>
          </template>
          <a-menu-item key="api-manage">
            <code-outlined />
            <span>API管理</span>
          </a-menu-item>
        </a-sub-menu>
        <a-sub-menu key="portal-center">
          <template #title>
            <span>
              <appstore-outlined />
              <span>门户中心</span>
            </span>
          </template>
          <a-menu-item key="menu-manage">
            <menu-outlined />
            <span>菜单管理</span>
          </a-menu-item>
        </a-sub-menu>
        <a-sub-menu key="basic-setting">
          <template #title>
            <span>
              <setting-outlined />
              <span>基础设置</span>
            </span>
          </template>
          <a-menu-item key="user-manage">
            <user-outlined />
            <span>用户管理</span>
          </a-menu-item>
        </a-sub-menu>
      </a-menu>
    </a-layout-sider>
    <a-layout>
      <a-layout-header style="background: #fff; padding: 0; display: flex; justify-content: space-between; align-items: center;">
        <div class="header-left">
          <menu-unfold-outlined
              v-if="collapsed"
              class="trigger"
              @click="() => (collapsed = !collapsed)"
          />
          <menu-fold-outlined v-else class="trigger" @click="() => (collapsed = !collapsed)" />
        </div>
        <div class="header-right">
          <a-dropdown>
            <a-space class="user-info">
              <user-outlined />
              <span>{{ username || '用户' }}</span>
              <down-outlined />
            </a-space>
            <template #overlay>
              <a-menu>
                <a-menu-item key="logout" @click="handleLogout">
                  <logout-outlined />
                  退出登录
                </a-menu-item>
              </a-menu>
            </template>
          </a-dropdown>
        </div>
      </a-layout-header>
      <a-layout-content
          :style="{ margin: '24px 16px', padding: '24px 24px 24px 24px', background: '#fff', minHeight: '280px' }"
      >
        <router-view />
      </a-layout-content>
      <!--      <a-layout-footer style="text-align: center">-->
      <!--        Ant Design ©2018 Created by Ant UED-->
      <!--      </a-layout-footer>-->
    </a-layout>
  </a-layout>
</template>

<script setup lang="ts">
import { ref, watch, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import {
  BookOutlined,
  FolderOutlined,
  FolderOpenOutlined,
  FileTextOutlined,
  MenuUnfoldOutlined,
  MenuFoldOutlined,
  ApiOutlined,
  CodeOutlined,
  AppstoreOutlined,
  MenuOutlined,
  SettingOutlined,
  UserOutlined,
  DownOutlined,
  LogoutOutlined
} from '@ant-design/icons-vue'
import { Modal, message } from 'ant-design-vue'
import axios from 'axios'
import type { MenuClickEventHandler } from 'ant-design-vue/es/menu/src/interface'

const router = useRouter()
const collapsed = ref(false)
const selectedKeys = ref<string[]>([])
const openKeys = ref(['doc-center', 'api-center', 'portal-center', 'basic-setting'])
const username = ref<string>('')

// 根据当前路由设置选中的菜单项
const setSelectedKeysFromRoute = () => {
  const path = router.currentRoute.value.path
  if (path.includes('/home/article')) {
    selectedKeys.value = ['article']
  } else if (path.includes('/home/group')) {
    selectedKeys.value = ['group']
  } else if (path.includes('/home/menu-manage')) {
    selectedKeys.value = ['menu-manage']
  } else if (path.includes('/home/user-manage')) {
    selectedKeys.value = ['user-manage']
  }
}

// 监听路由变化
watch(() => router.currentRoute.value.path, () => {
  setSelectedKeysFromRoute()
})

// 获取用户信息
const getUserInfo = () => {
  const userInfo = localStorage.getItem('userInfo')
  if (userInfo) {
    try {
      const user = JSON.parse(userInfo)
      username.value = user.nickname || user.username || '用户'
    } catch (error) {
      console.error('解析用户信息失败:', error)
      username.value = '用户'
    }
  }
}

// 退出登录
const handleLogout = () => {
  Modal.confirm({
    title: '确认退出',
    content: '确定要退出登录吗？',
    okText: '确定',
    cancelText: '取消',
    onOk: async () => {
      try {
        // 调用登出接口
        await axios.post('/user/logout')
        message.success('退出登录成功')
      } catch (error) {
        // 即使登出接口调用失败，也要清除本地数据
        console.error('调用登出接口失败:', error)
        message.warning('登出接口调用失败，但已清除本地登录信息')
      } finally {
        // 无论接口调用成功与否，都要清除本地存储并跳转
        localStorage.removeItem('access_token')
        localStorage.removeItem('userInfo')
        router.push('/login')
      }
    }
  })
}

onMounted(() => {
  setSelectedKeysFromRoute()
  getUserInfo()
})

const handleMenuClick: MenuClickEventHandler = ({ key }) => {
  router.push(`/home/${key}`)
}
</script>

<style scoped>
.trigger {
  font-size: 18px;
  line-height: 64px;
  padding: 0 24px;
  cursor: pointer;
  transition: color 0.3s;
}

.trigger:hover {
  color: #1890ff;
}

.header-left {
  display: flex;
  align-items: center;
}

.header-right {
  padding-right: 24px;
}

.user-info {
  cursor: pointer;
  padding: 0 16px;
  height: 64px;
  display: flex;
  align-items: center;
  transition: background-color 0.3s;
}

.user-info:hover {
  background-color: #f5f5f5;
}

.logo {
  height: 32px;
  margin: 16px;
  /* background: rgba(255, 255, 255, 0.3); */
  color: #fff;
  font-size: 22px;
  font-weight: bold;
  display: flex;
  align-items: center;
  justify-content: center;
  letter-spacing: 1px;
}

.site-layout .site-layout-background {
  background: #fff;
}
[data-theme='dark'] .site-layout .site-layout-background {
  background: #141414;
}
</style>