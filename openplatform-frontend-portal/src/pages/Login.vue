<template>
  <div class="login-container">
    <div class="login-box">
      <div class="login-header">
        <h2>用户登录</h2>
        <p>欢迎回到开放平台</p>
      </div>
      
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

        <div class="login-footer">
          <span>还没有账号？</span>
          <a @click="goToRegister">立即注册</a>
        </div>
      </a-form>
    </div>
  </div>
</template>

<script lang="ts" setup>
import { ref, reactive } from 'vue';
import { useRouter } from 'vue-router';
import { UserOutlined, LockOutlined } from '@ant-design/icons-vue';
import { message } from 'ant-design-vue';
import axios from 'axios';

const router = useRouter();

// 登录表单数据
const loginForm = reactive({
  username: '',
  password: '',
  remember: true
});

const loginLoading = ref(false);

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
      
      // 重置表单
      Object.assign(loginForm, {
        username: '',
        password: '',
        remember: true
      });
      
      // 跳转到首页
      router.push('/');
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

// 跳转到注册页面
const goToRegister = () => {
  router.push('/register');
};
</script>

<style scoped>
.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background: #f0f2f5;
  padding: 20px;
}

.login-box {
  width: 100%;
  max-width: 450px;
  padding: 40px;
  background: white;
  border-radius: 12px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
}

.login-header {
  text-align: center;
  margin-bottom: 30px;
}

.login-header h2 {
  margin: 0 0 8px 0;
  font-size: 28px;
  font-weight: 600;
  color: #1a1a1a;
}

.login-header p {
  margin: 0;
  color: #666;
  font-size: 14px;
}

.login-form {
  margin-top: 20px;
}

.login-form-forgot {
  float: right;
  color: #1677ff;
  text-decoration: none;
}

.login-form-forgot:hover {
  text-decoration: underline;
}

.login-form-button {
  width: 100%;
  height: 44px;
  font-size: 16px;
  font-weight: 500;
}

.site-form-item-icon {
  color: rgba(0, 0, 0, 0.25);
}

.login-footer {
  text-align: center;
  margin-top: 20px;
  color: #666;
}

.login-footer a {
  color: #1677ff;
  text-decoration: none;
  margin-left: 4px;
}

.login-footer a:hover {
  text-decoration: underline;
}

:deep(.ant-form-item) {
  margin-bottom: 20px;
}

:deep(.ant-checkbox-wrapper) {
  font-size: 14px;
}
</style>