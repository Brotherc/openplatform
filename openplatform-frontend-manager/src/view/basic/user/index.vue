<template>
  <div class="user-manage">
    <div class="header">
      <div class="header-content">
        <a-button type="primary" @click="showAddModal">
          <plus-outlined />
          新建用户
        </a-button>

        <div class="search-form">
          <a-form layout="inline" :model="searchForm">
            <a-form-item label="用户名">
              <a-input
                  v-model:value="searchForm.username"
                  placeholder="请输入用户名"
                  allow-clear
                  style="width: 200px"
              />
            </a-form-item>
            <a-form-item label="昵称">
              <a-input
                  v-model:value="searchForm.nickname"
                  placeholder="请输入昵称"
                  allow-clear
                  style="width: 200px"
              />
            </a-form-item>
            <a-form-item label="状态">
              <a-select
                  v-model:value="searchForm.status"
                  placeholder="请选择状态"
                  allow-clear
                  style="width: 120px"
              >
                <a-select-option :value="1">禁用</a-select-option>
                <a-select-option :value="2">启用</a-select-option>
              </a-select>
            </a-form-item>
            <a-form-item>
              <a-button type="primary" @click="handleSearch">
                <search-outlined />
                查询
              </a-button>
              <a-button style="margin-left: 8px" @click="handleReset">
                <reload-outlined />
                重置
              </a-button>
            </a-form-item>
          </a-form>
        </div>
      </div>
    </div>

    <div class="table-container">
      <a-table
          :columns="columns"
          :data-source="userList"
          :pagination="pagination"
          :loading="loading"
          row-key="userId"
          @change="handleTableChange"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'status'">
            <a-tag :color="record.status === 2 ? 'green' : 'red'">
              {{ record.status === 2 ? '启用' : '禁用' }}
            </a-tag>
          </template>
          <template v-else-if="column.key === 'createTime'">
            {{ formatDateTime(record.createTime) }}
          </template>
          <template v-else-if="column.key === 'action'">
            <a-space>
              <a-button type="link" size="small" @click="showEditModal(record)">
                编辑
              </a-button>
              <a-button type="link" size="small" danger @click="handleDelete(record)">
                删除
              </a-button>
            </a-space>
          </template>
        </template>
      </a-table>
    </div>

    <!-- 新增/编辑用户弹窗 -->
    <a-modal
        v-model:open="modalVisible"
        :title="modalType === 'create' ? '新建用户' : '编辑用户'"
        :confirm-loading="submitLoading"
        @ok="handleSubmit"
        @cancel="handleCancel"
        ok-text="确定"
        cancel-text="取消"
    >
      <a-form
          ref="formRef"
          :model="formData"
          :rules="rules"
          :label-col="{ span: 24 }"
          :wrapper-col="{ span: 24 }"
      >
        <a-form-item label="用户名" name="username" v-if="modalType === 'create'">
          <a-input
              v-model:value="formData.username"
              placeholder="请输入用户名"
              :disabled="modalType === 'edit'"
          />
        </a-form-item>
        <a-form-item label="密码" name="password">
          <a-input-password
              v-model:value="formData.password"
              placeholder="请输入密码"
          />
        </a-form-item>
        <a-form-item label="昵称" name="nickname">
          <a-input
              v-model:value="formData.nickname"
              placeholder="请输入昵称"
          />
        </a-form-item>
        <a-form-item label="状态" name="status">
          <a-select
              v-model:value="formData.status"
              placeholder="请选择状态"
          >
            <a-select-option :value="1">禁用</a-select-option>
            <a-select-option :value="2">启用</a-select-option>
          </a-select>
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { message, Modal } from 'ant-design-vue'
import { PlusOutlined, SearchOutlined, ReloadOutlined } from '@ant-design/icons-vue'
import axios from 'axios'

// 设置API基础URL
axios.defaults.baseURL = 'http://127.0.0.1:8080'

// 用户数据接口
interface User {
  userId: string
  username: string
  nickname: string
  status: number
  createTime?: string
  updateTime?: string
}

// 搜索表单接口
interface SearchForm {
  username?: string
  nickname?: string
  status?: number
}

// 表单数据接口
interface FormData {
  userId?: string
  username: string
  password: string
  nickname: string
  status: number
}

// 响应数据
const userList = ref<User[]>([])
const loading = ref(false)
const modalVisible = ref(false)
const modalType = ref<'create' | 'edit'>('create')
const submitLoading = ref(false)
const formRef = ref()

// 搜索表单
const searchForm = reactive<SearchForm>({
  username: '',
  nickname: '',
  status: undefined
})

// 表单数据
const formData = reactive<FormData>({
  username: '',
  password: '',
  nickname: '',
  status: 2
})

// 分页配置
const pagination = reactive({
  current: 1,
  pageSize: 10,
  total: 0,
  showSizeChanger: true,
  showQuickJumper: true,
  showTotal: (total: number) => `共 ${total} 条记录`,
  pageSizeOptions: ['10', '20', '50', '100'],
  locale: {
    items_per_page: '条/页',
    jump_to: '跳至',
    jump_to_confirm: '确定',
    page: '页'
  }
})

// 表格列定义
const columns = [
  {
    title: '用户名',
    dataIndex: 'username',
    key: 'username',
    width: 150
  },
  {
    title: '昵称',
    dataIndex: 'nickname',
    key: 'nickname',
    width: 150
  },
  {
    title: '状态',
    dataIndex: 'status',
    key: 'status',
    width: 100
  },
  {
    title: '创建时间',
    dataIndex: 'createTime',
    key: 'createTime',
    width: 180
  },
  {
    title: '操作',
    key: 'action',
    width: 150,
    fixed: 'right'
  }
]

// 表单验证规则
const rules = computed(() => ({
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '用户名长度在 3 到 20 个字符', trigger: 'blur' }
  ],
  password: [
    { required: modalType.value === 'create', message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度在 6 到 20 个字符', trigger: 'blur' }
  ],
  nickname: [
    { max: 50, message: '昵称长度不能超过 50 个字符', trigger: 'blur' }
  ],
  status: [
    { required: true, message: '请选择状态', trigger: 'change' }
  ]
}))

// 获取用户列表
const fetchUserList = async () => {
  loading.value = true
  try {
    const params = {
      page: pagination.current - 1, // 后端从0开始
      size: pagination.pageSize,
      ...searchForm
    }

    const response = await axios.get('/user/page', { params })

    if (response.data && response.data.code === 0) {
      userList.value = response.data.data.content || []
      pagination.total = response.data.data.totalElements || 0
    } else {
      message.error(response.data.message || '获取用户列表失败')
    }
  } catch (error) {
    console.error('获取用户列表失败:', error)
    message.error('获取用户列表失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

// 搜索
const handleSearch = () => {
  pagination.current = 1
  fetchUserList()
}

// 重置搜索
const handleReset = () => {
  Object.assign(searchForm, {
    username: '',
    nickname: '',
    status: undefined
  })
  pagination.current = 1
  fetchUserList()
}

// 表格变化处理
const handleTableChange = (pag: any) => {
  pagination.current = pag.current
  pagination.pageSize = pag.pageSize
  fetchUserList()
}

// 显示新增弹窗
const showAddModal = () => {
  modalType.value = 'create'
  modalVisible.value = true
  Object.assign(formData, {
    username: '',
    password: '',
    nickname: '',
    status: 2
  })
  formRef.value?.resetFields()
}

// 显示编辑弹窗
const showEditModal = (record: User) => {
  modalType.value = 'edit'
  modalVisible.value = true
  Object.assign(formData, {
    userId: record.userId,
    username: record.username,
    password: '',
    nickname: record.nickname,
    status: record.status
  })
  formRef.value?.resetFields()
}

// 提交表单
const handleSubmit = async () => {
  try {
    await formRef.value?.validate()
    submitLoading.value = true

    if (modalType.value === 'create') {
      // 新增用户
      const requestData: any = {
        username: formData.username,
        password: formData.password,
        status: formData.status
      }

      // 只有当昵称有值时才传递
      if (formData.nickname && formData.nickname.trim()) {
        requestData.nickname = formData.nickname.trim()
      }

      const response = await axios.post('/user/add', requestData)

      if (response.data && response.data.code === 0) {
        message.success('创建用户成功')
        modalVisible.value = false
        fetchUserList()
      } else {
        message.error(response.data.message || '创建用户失败')
      }
    } else {
      // 编辑用户
      const requestData: any = {
        userId: formData.userId,
        status: formData.status
      }

      // 只有当昵称有值时才传递
      if (formData.nickname && formData.nickname.trim()) {
        requestData.nickname = formData.nickname.trim()
      }

      // 只有当密码有值时才传递
      if (formData.password && formData.password.trim()) {
        requestData.password = formData.password.trim()
      }

      const response = await axios.post('/user/updateById', requestData)

      if (response.data && response.data.code === 0) {
        message.success('更新用户成功')
        modalVisible.value = false
        fetchUserList()
      } else {
        message.error(response.data.message || '更新用户失败')
      }
    }
  } catch (error) {
    console.error('提交失败:', error)
    if (error.errorFields) {
      // 表单验证错误
      return
    }
    message.error('操作失败，请稍后重试')
  } finally {
    submitLoading.value = false
  }
}

// 取消操作
const handleCancel = () => {
  modalVisible.value = false
  formRef.value?.resetFields()
}

// 格式化日期时间
const formatDateTime = (timestamp: string | number | undefined): string => {
  if (!timestamp) return '-'

  const date = new Date(timestamp)
  if (isNaN(date.getTime())) return '-'

  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  const hours = String(date.getHours()).padStart(2, '0')
  const minutes = String(date.getMinutes()).padStart(2, '0')
  const seconds = String(date.getSeconds()).padStart(2, '0')

  return `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`
}

// 删除用户
const handleDelete = (record: User) => {
  Modal.confirm({
    title: '确认删除',
    content: `确定要删除用户 "${record.username}" 吗？`,
    okText: '确定',
    cancelText: '取消',
    onOk: async () => {
      try {
        const response = await axios.post('/user/deleteById', {
          userId: record.userId
        })

        if (response.data && response.data.code === 0) {
          message.success('删除用户成功')
          fetchUserList()
        } else {
          message.error(response.data.message || '删除用户失败')
        }
      } catch (error) {
        console.error('删除用户失败:', error)
        message.error('删除用户失败，请稍后重试')
      }
    }
  })
}

// 组件挂载时获取用户列表
onMounted(() => {
  fetchUserList()
})
</script>

<style scoped>
.user-manage {
  padding: 24px;
}

.header {
  margin-bottom: 16px;
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: #fff;
  margin-bottom: 24px;
  border-radius: 6px;
}

.search-form {
  margin: 0;
}

.table-container {
  background: #fff;
  border-radius: 6px;
}

:deep(.ant-table-thead > tr > th) {
  background: #fafafa;
}
</style>
