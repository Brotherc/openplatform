<template>
  <div class="register-container">
    <div class="register-box">
      <div class="register-header">
        <h2>用户注册</h2>
        <p>创建您的开发者账号</p>
      </div>
      
      <a-form
        :model="registerForm"
        name="register"
        @finish="onRegisterFinish"
        @finishFailed="onRegisterFinishFailed"
        autocomplete="off"
        class="register-form"
      >
        <a-form-item
          name="username"
          :rules="[
            { required: true, message: '请输入用户名' },
            { min: 3, max: 50, message: '用户名长度为3-50个字符' },
            { pattern: /^[a-zA-Z0-9_]+$/, message: '用户名只能包含字母、数字和下划线' }
          ]"
        >
          <a-input
            v-model:value="registerForm.username"
            placeholder="用户名（3-50个字符，只能包含字母、数字和下划线）"
            size="large"
          >
            <template #prefix>
              <UserOutlined class="site-form-item-icon" />
            </template>
          </a-input>
        </a-form-item>

        <a-form-item
          name="password"
          :rules="[
            { required: true, message: '请输入密码' },
            { min: 6, max: 20, message: '密码长度必须在6-20个字符之间' },
            { pattern: /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)[a-zA-Z\d@$!%*?&]{6,20}$/, message: '密码必须包含至少一个小写字母、一个大写字母和一个数字，可包含特殊字符@$!%*?&' }
          ]"
        >
          <a-input-password
            v-model:value="registerForm.password"
            placeholder="密码（6-20个字符，必须包含大小写字母和数字）"
            size="large"
          >
            <template #prefix>
              <LockOutlined class="site-form-item-icon" />
            </template>
          </a-input-password>
        </a-form-item>

        <a-form-item
          name="confirmPassword"
          :rules="[
            { required: true, message: '请确认密码' },
            { validator: validateConfirmPassword }
          ]"
        >
          <a-input-password
            v-model:value="registerForm.confirmPassword"
            placeholder="确认密码"
            size="large"
          >
            <template #prefix>
              <LockOutlined class="site-form-item-icon" />
            </template>
          </a-input-password>
        </a-form-item>

        <a-form-item
          name="agreement"
          :rules="[{ required: true, message: '请同意用户协议' }]"
        >
          <a-checkbox v-model:checked="registerForm.agreement">
            我已阅读并同意
            <a href="#" @click.prevent>《用户协议》</a>
            和
            <a href="#" @click.prevent>《隐私政策》</a>
          </a-checkbox>
        </a-form-item>

        <a-form-item>
          <a-button
            :loading="registerLoading"
            type="primary"
            html-type="submit"
            class="register-form-button"
            size="large"
          >
            注册
          </a-button>
        </a-form-item>

        <div class="register-footer">
          <span>已有账号？</span>
          <a @click="goToLogin">立即登录</a>
        </div>
      </a-form>
    </div>
  </div>
</template>

<script lang="ts" setup>
import { ref, reactive } from 'vue';
import { useRouter } from 'vue-router';
import { 
  UserOutlined, 
  LockOutlined
} from '@ant-design/icons-vue';
import { message } from 'ant-design-vue';
import axios from 'axios';

const router = useRouter();

// 注册表单数据
const registerForm = reactive({
  username: '',
  password: '',
  confirmPassword: '',
  agreement: false
});

const registerLoading = ref(false);

// 确认密码验证
const validateConfirmPassword = async (_rule: any, value: string) => {
  if (value && value !== registerForm.password) {
    return Promise.reject('两次输入的密码不一致');
  }
  return Promise.resolve();
};

// 注册接口响应类型
interface RegisterResponse {
  success: boolean;
  code: number;
  message: string | null;
  data: any;
}

// 注册提交
const onRegisterFinish = async (values: any) => {
  registerLoading.value = true;
  try {
    const response = await axios.post<RegisterResponse>(`${import.meta.env.VITE_API_BASE_URL}/user/register`, {
      username: values.username,
      password: values.password
    });

    if (response.data && response.data.code === 0 && response.data.success) {
      message.success('注册成功，请登录');
      
      // 重置表单
      Object.assign(registerForm, {
        username: '',
        password: '',
        confirmPassword: '',
        agreement: false
      });
      
      // 跳转到首页或登录
      router.push('/');
    } else {
      message.error(response.data.message || '注册失败，请稍后重试');
    }
  } catch (error: any) {
    console.error('注册失败:', error);
    message.error('注册失败，请稍后重试');
  } finally {
    registerLoading.value = false;
  }
};

const onRegisterFinishFailed = (errorInfo: any) => {
  console.log('Failed:', errorInfo);
};

// 跳转到登录
const goToLogin = () => {
  router.push('/login');
};
</script>

<style scoped>
.register-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background: #f0f2f5;
  padding: 20px;
}

.register-box {
  width: 100%;
  max-width: 450px;
  padding: 40px;
  background: white;
  border-radius: 12px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
}

.register-header {
  text-align: center;
  margin-bottom: 30px;
}

.register-header h2 {
  margin: 0 0 8px 0;
  font-size: 28px;
  font-weight: 600;
  color: #1a1a1a;
}

.register-header p {
  margin: 0;
  color: #666;
  font-size: 14px;
}

.register-form {
  margin-top: 20px;
}

.register-form-button {
  width: 100%;
  height: 44px;
  font-size: 16px;
  font-weight: 500;
}

.site-form-item-icon {
  color: rgba(0, 0, 0, 0.25);
}

.register-footer {
  text-align: center;
  margin-top: 20px;
  color: #666;
}

.register-footer a {
  color: #1677ff;
  text-decoration: none;
  margin-left: 4px;
}

.register-footer a:hover {
  text-decoration: underline;
}

:deep(.ant-form-item) {
  margin-bottom: 20px;
}

:deep(.ant-checkbox-wrapper) {
  font-size: 14px;
}

:deep(.ant-checkbox-wrapper a) {
  color: #1677ff;
}
</style>