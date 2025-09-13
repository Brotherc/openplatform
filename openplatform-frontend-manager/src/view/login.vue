<template>
  <div class="login-container">
    <div class="login-box">
      <div class="login-header">
        <h2>系统登录</h2>
      </div>
      <a-form
          :model="formState"
          name="login"
          @finish="onFinish"
          @finishFailed="onFinishFailed"
          autocomplete="off"
      >
        <a-form-item
            name="username"
            :rules="[{ required: true, message: '请输入用户名' }]"
        >
          <a-input
              v-model:value="formState.username"
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
              v-model:value="formState.password"
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
            <a-checkbox v-model:checked="formState.remember">记住密码</a-checkbox>
          </a-form-item>
          <a class="login-form-forgot" href="">忘记密码</a>
        </a-form-item>

        <a-form-item>
          <a-button
              :loading="loading"
              type="primary"
              html-type="submit"
              class="login-form-button"
              size="large"
          >
            登录
          </a-button>
        </a-form-item>
      </a-form>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { UserOutlined, LockOutlined } from '@ant-design/icons-vue'
import { message } from 'ant-design-vue'
import { useRouter } from 'vue-router'
import axios from 'axios'

interface FormState {
  username: string
  password: string
  remember: boolean
}

interface LoginResponse {
  success: boolean
  code: number
  message: string | null
  data: {
    userId: number
    username: string
    status: number
    nickname: string | null
    createBy: number
    createTime: number
    updateBy: number
    updateTime: number
    token: string
  }
}

const router = useRouter()
const loading = ref(false)
const formState = reactive<FormState>({
  username: '',
  password: '',
  remember: true
})

const onFinish = async (values: FormState) => {
  loading.value = true
  try {
    // 调用登录接口
    const response = await axios.post<LoginResponse>('/user/login', {
      username: values.username,
      password: values.password
    })

    if (response.data && response.data.code === 0 && response.data.success) {
      // 登录成功
      message.success('登录成功')

      // 保存用户信息到localStorage
      localStorage.setItem('userInfo', JSON.stringify(response.data.data))

      // 保存token到localStorage
      localStorage.setItem('access_token', response.data.data.token)

      // 跳转到首页
      router.push('/home')
    } else {
      // 登录失败
      message.error(response.data.message || '登录失败，用户名或密码错误')
    }
  } catch (error: any) {
    // 统一错误处理已在axios拦截器中处理，这里只需要记录日志
    console.error('登录失败:', error)
  } finally {
    loading.value = false
  }
}

const onFinishFailed = (errorInfo: any) => {
  console.log('Failed:', errorInfo)
}

// 检查本地是否有token，如果有则直接跳转到首页
const checkTokenAndRedirect = () => {
  const token = localStorage.getItem('access_token')
  const userInfo = localStorage.getItem('userInfo')

  if (token && userInfo) {
    // 有token和用户信息，直接跳转到首页
    router.push('/home')
  }
}

// 组件挂载时检查token
onMounted(() => {
  checkTokenAndRedirect()
})
</script>

<style scoped>
.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background: #f0f2f5;
}

.login-box {
  width: 100%;
  max-width: 400px;
  padding: 40px;
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.09);
}

.login-header {
  text-align: center;
  margin-bottom: 30px;
}

.login-header h2 {
  color: rgba(0, 0, 0, 0.85);
  font-size: 24px;
  margin: 0;
  font-weight: 500;
}

.login-form-forgot {
  float: right;
  color: #1890ff;
}

.login-form-button {
  width: 100%;
}

.site-form-item-icon {
  color: rgba(0, 0, 0, 0.25);
}
</style>