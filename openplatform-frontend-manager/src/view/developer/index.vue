<template>
  <div class="developer-manage">
    <div class="header">
      <div class="header-content">
        <a-button type="primary" @click="handleAdd">
          <plus-outlined />
          新建开发者
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
            <a-form-item label="状态">
              <a-select
                v-model:value="searchForm.status"
                placeholder="请选择状态"
                allow-clear
                style="width: 120px"
              >
                <a-select-option :value="1">正常</a-select-option>
                <a-select-option :value="2">禁用</a-select-option>
              </a-select>
            </a-form-item>
            <a-form-item>
              <a-space>
                <a-button type="primary" @click="handleSearch">
                  <search-outlined />
                  查询
                </a-button>
                <a-button @click="handleReset">
                  <reload-outlined />
                  重置
                </a-button>
              </a-space>
            </a-form-item>
          </a-form>
        </div>
      </div>
    </div>

    <div class="table-container">
      <a-table
        :columns="columns"
        :data-source="tableData"
        :loading="loading"
        :pagination="pagination"
        @change="handleTableChange"
        row-key="id"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'status'">
            <a-tag :color="record.status === 1 ? 'green' : 'red'">
              {{ record.status === 1 ? '正常' : '禁用' }}
            </a-tag>
          </template>
          <template v-if="column.key === 'createTime'">
            {{ formatDate(record.createTime) }}
          </template>
          <template v-if="column.key === 'action'">
            <a-space>
              <a-button type="link" size="small" @click="handleEdit(record)">
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

    <!-- 新增/编辑开发者弹窗 -->
    <a-modal
      v-model:open="modalVisible"
      :title="modalType === 'create' ? '新建开发者' : '编辑开发者'"
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
        <a-form-item label="密码" name="password" v-if="modalType === 'create'">
          <a-input-password
            v-model:value="formData.password"
            placeholder="请输入密码"
          />
        </a-form-item>
        <a-form-item label="状态" name="status">
          <a-select
            v-model:value="formData.status"
            placeholder="请选择状态"
          >
            <a-select-option :value="1">正常</a-select-option>
            <a-select-option :value="2">禁用</a-select-option>
          </a-select>
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { message, Modal } from 'ant-design-vue'
import {
  SearchOutlined,
  ReloadOutlined,
  PlusOutlined
} from '@ant-design/icons-vue'
import axios from 'axios'

// 开发者数据接口
interface Developer {
  id: string
  username: string
  nickname: string
  status: number
  createTime?: string
  updateTime?: string
}

// 搜索表单接口
interface SearchForm {
  username?: string
  status?: number
}

// 表单数据接口
interface FormData {
  id?: string
  username: string
  password: string
  status: number
}

// 响应数据
const tableData = ref<Developer[]>([])
const loading = ref(false)
const modalVisible = ref(false)
const modalType = ref<'create' | 'edit'>('create')
const submitLoading = ref(false)
const formRef = ref()

// 搜索表单
const searchForm = reactive<SearchForm>({
  username: '',
  status: undefined
})

// 表单数据
const formData = reactive<FormData>({
  username: '',
  password: '',
  status: 1
})

// 分页配置
const pagination = reactive({
  current: 1,
  pageSize: 10,
  total: 0,
  showSizeChanger: true,
  showQuickJumper: true,
  showTotal: (total: number) => `共 ${total} 条记录`
})

// 表格列配置
const columns = [
  {
    title: '用户名',
    dataIndex: 'username',
    key: 'username'
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
    width: 150
  }
]

// 表单验证规则
const rules = computed(() => ({
  username: [
    { required: true, message: '请输入用户名' },
    { min: 3, max: 50, message: '用户名长度为3-50个字符' },
    { pattern: /^[a-zA-Z0-9_]+$/, message: '用户名只能包含字母、数字和下划线' }
  ],
  password: modalType.value === 'create' ? [
    { required: true, message: '请输入密码' },
    { min: 6, max: 20, message: '密码长度必须在6-20个字符之间' },
    { pattern: /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)[a-zA-Z\d@$!%*?&]{6,20}$/, message: '密码必须包含至少一个小写字母、一个大写字母和一个数字' }
  ] : [],
  status: [
    { required: true, message: '请选择状态' }
  ]
}))

// 格式化时间
const formatDate = (timestamp: number) => {
  if (!timestamp) return '-'
  return new Date(timestamp).toLocaleString()
}

// 获取开发者列表
const fetchDeveloperList = async () => {
  loading.value = true
  try {
    const params = {
      pageNum: pagination.current,
      pageSize: pagination.pageSize,
      ...searchForm
    }
    
    const response = await axios.get('/developer/page', { params })
    
    if (response.data && response.data.code === 0) {
      tableData.value = response.data.data.records || []
      pagination.total = response.data.data.total || 0
    } else {
      message.error(response.data.message || '获取数据失败')
    }
  } catch (error) {
    console.error('获取开发者列表失败:', error)
    message.error('获取数据失败')
  } finally {
    loading.value = false
  }
}

// 搜索开发者
const handleSearch = () => {
  pagination.current = 1
  fetchDeveloperList()
}

// 重置搜索
const handleReset = () => {
  Object.assign(searchForm, {
    username: '',
    status: undefined
  })
  pagination.current = 1
  fetchDeveloperList()
}

// 表格变化处理
const handleTableChange = (pag: any) => {
  pagination.current = pag.current
  pagination.pageSize = pag.pageSize
  fetchDeveloperList()
}

// 显示新增弹窗
const handleAdd = () => {
  modalType.value = 'create'
  modalVisible.value = true
  Object.assign(formData, {
    username: '',
    password: '',
    status: 1
  })
  formRef.value?.resetFields()
}

// 显示编辑弹窗
const handleEdit = (record: Developer) => {
  modalType.value = 'edit'
  modalVisible.value = true
  Object.assign(formData, {
    id: record.id,
    username: record.username,
    password: '',
    status: record.status
  })
  formRef.value?.resetFields()
}

// 提交表单
const handleSubmit = async () => {
  try {
    await formRef.value.validate()
    submitLoading.value = true

    if (modalType.value === 'create') {
      // 新增开发者
      const requestData: any = {
        username: formData.username,
        password: formData.password,
        status: formData.status
      }

      // 只有当昵称有值时才传递
      if (formData.nickname && formData.nickname.trim()) {
        requestData.nickname = formData.nickname.trim()
      }

      const response = await axios.post('/developer', requestData)

      if (response.data && response.data.code === 0) {
        message.success('创建开发者成功')
        modalVisible.value = false
        fetchDeveloperList()
      } else {
        message.error(response.data.message || '创建开发者失败')
      }
    } else {
      // 编辑开发者
      const requestData: any = {
        id: formData.id,
        status: formData.status
      }

      // 只有当昵称有值时才传递
      if (formData.nickname && formData.nickname.trim()) {
        requestData.nickname = formData.nickname.trim()
      }

      const response = await axios.put(`/developer/${formData.id}`, requestData)

      if (response.data && response.data.code === 0) {
        message.success('更新开发者成功')
        modalVisible.value = false
        fetchDeveloperList()
      } else {
        message.error(response.data.message || '更新开发者失败')
      }
    }
  } catch (error) {
    console.error('操作失败:', error)
    if (!error.errorFields) {
      message.error('操作失败')
    }
  } finally {
    submitLoading.value = false
  }
}

// 取消弹窗
const handleCancel = () => {
  modalVisible.value = false
  formRef.value?.resetFields()
}

// 删除开发者
const handleDelete = (record: Developer) => {
  Modal.confirm({
    title: '确认删除',
    content: `确定要删除开发者 "${record.username}" 吗？`,
    okText: '确定',
    cancelText: '取消',
    onOk: async () => {
      try {
        const response = await axios.delete(`/developer/${record.id}`)

        if (response.data && response.data.code === 0) {
          message.success('删除开发者成功')
          fetchDeveloperList()
        } else {
          message.error(response.data.message || '删除开发者失败')
        }
      } catch (error) {
        console.error('删除开发者失败:', error)
        message.error('删除失败')
      }
    }
  })
}

// 组件挂载时获取开发者列表
onMounted(() => {
  fetchDeveloperList()
})
</script>

<style scoped>
.developer-manage {
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
