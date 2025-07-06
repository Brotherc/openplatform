<template>
  <div class="group-manage">
    <div class="search-form">
      <a-form layout="inline" :model="searchForm">
        <a-form-item label="分组名称">
          <a-input
            v-model:value="searchForm.name"
            placeholder="请输入分组名称"
            allow-clear
            style="width: 200px"
          />
        </a-form-item>
        <a-form-item label="状态">
          <a-select
            v-model:value="searchForm.status"
            placeholder="请选择状态"
            style="width: 120px"
            allow-clear
          >
            <a-select-option :value="1">未发布</a-select-option>
            <a-select-option :value="2">已发布</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item>
          <a-space>
            <a-button type="primary" @click="handleSearch">
              <template #icon><search-outlined /></template>
              查询
            </a-button>
            <a-button @click="handleReset">
              <template #icon><reload-outlined /></template>
              重置
            </a-button>
          </a-space>
        </a-form-item>
      </a-form>
    </div>

    <div class="table-operations">
      <a-button type="primary" @click="showCreateModal">
        <template #icon><plus-outlined /></template>
        新建分组
      </a-button>
    </div>
    <a-table
      :columns="columns"
      :data-source="dataSource"
      :loading="loading"
      :pagination="pagination"
      @change="handleTableChange"
    >
      <template #bodyCell="{ column, record }">
        <template v-if="column.key === 'status'">
          <a-tag :color="record.status === 2 ? 'green' : 'red'">
            {{ record.status === 2 ? '已发布' : '未发布' }}
          </a-tag>
        </template>
        <template v-if="column.key === 'sort'">
          {{ record.sort }}
        </template>
        <template v-if="column.key === 'action'">
          <a-space>
            <a @click="showEditModal(record)">编辑</a>
            <a-divider type="vertical" />
            <a v-if="record.status === 1" @click="handlePublish(record)">发布</a>
            <a v-else @click="handleUnpublish(record)">下架</a>
            <a-divider type="vertical" />
            <a-popconfirm
              title="确定要删除这个分组吗？"
              ok-text="确定"
              cancel-text="取消"
              @confirm="handleDelete(record)"
            >
              <a class="text-danger">删除</a>
            </a-popconfirm>
          </a-space>
        </template>
      </template>
    </a-table>

    <!-- 创建/编辑分组弹窗 -->
    <a-modal
      v-model:visible="modalVisible"
      :title="modalType === 'create' ? '新建分组' : '编辑分组'"
      @ok="handleModalOk"
      @cancel="handleModalCancel"
      okText="确定"
      cancelText="取消"
    >
      <a-form
        ref="formRef"
        :model="formState"
        :rules="rules"
        layout="vertical"
      >
        <a-form-item label="分组名称" name="name">
          <a-input v-model:value="formState.name" placeholder="请输入分组名称" />
        </a-form-item>
        <a-form-item label="分组描述" name="description">
          <a-textarea
            v-model:value="formState.description"
            placeholder="请输入分组描述"
            :rows="4"
          />
        </a-form-item>
        <a-form-item label="排序" name="sort">
          <a-input-number
            v-model:value="formState.sort"
            :min="0"
            placeholder="请输入排序号"
          />
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, nextTick } from 'vue'
import { message } from 'ant-design-vue'
import { PlusOutlined, SearchOutlined, ReloadOutlined } from '@ant-design/icons-vue'
import type { TablePaginationConfig } from 'ant-design-vue'
import axios from 'axios'

interface Group {
  id: string
  name: string
  description: string
  status: number
  sort: number
  createTime: string
  updateTime: string
}

interface FormState {
  id?: string
  name: string
  description: string
  sort: number
}

interface SearchForm {
  name: string
  status: number | undefined
}

const columns = [
  {
    title: '分组名称',
    dataIndex: 'name',
    key: 'name',
    width: 200,
  },
  {
    title: '描述',
    dataIndex: 'description',
    key: 'description',
    width: 300,
    ellipsis: true,
  },
  {
    title: '排序',
    dataIndex: 'sort',
    key: 'sort',
    width: 100,
  },
  {
    title: '状态',
    dataIndex: 'status',
    key: 'status',
    width: 100,
  },
  {
    title: '创建时间',
    dataIndex: 'createTime',
    key: 'createTime',
    width: 180,
  },
  {
    title: '操作',
    key: 'action',
    width: 200,
  },
]

const dataSource = ref<Group[]>([])
const loading = ref(false)
const modalVisible = ref(false)
const modalType = ref<'create' | 'edit'>('create')
const formRef = ref()
const searchForm = reactive<SearchForm>({
  name: '',
  status: undefined,
})

const formState = reactive<FormState>({
  name: '',
  description: '',
  sort: 0,
})

const rules = {
  name: [{ required: true, message: '请输入分组名称', trigger: 'blur' }],
  description: [],
  sort: [{ required: true, message: '请输入排序号', trigger: 'blur' }],
}

const pagination = reactive({
  current: 1,
  pageSize: 10,
  total: 0,
})

// 获取分组列表
const fetchGroups = async () => {
  loading.value = true
  try {
    const response = await axios.get('http://127.0.0.1:8080/docCatalogGroup/page', {
      params: {
        name: searchForm.name || undefined, // 空字符串转为undefined
        status: searchForm.status,
        page: pagination.current - 1,
        size: pagination.pageSize,
        sort: 'sort,asc'
      }
    })
    
    if (response.data.code === 0) {
      const pageData = response.data.data
      dataSource.value = pageData.content.map(item => ({
        id: item.docCatalogGroupId,
        name: item.name,
        description: item.description,
        status: item.status,
        sort: item.sort,
        createTime: new Date(item.createTime).toLocaleString('zh-CN', {
          year: 'numeric',
          month: '2-digit',
          day: '2-digit',
          hour: '2-digit',
          minute: '2-digit',
          second: '2-digit',
          hour12: false
        }).replace(/\//g, '-')
      }))
      pagination.total = pageData.totalElements
    } else {
      message.error(response.data.message || '获取分组列表失败')
    }
  } catch (error) {
    console.error('获取分组列表失败:', error)
    message.error('获取分组列表失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

// 处理查询
const handleSearch = () => {
  pagination.current = 1
  fetchGroups()
}

// 处理重置
const handleReset = () => {
  searchForm.name = ''
  searchForm.status = undefined
  pagination.current = 1
  fetchGroups()
}

// 表格变化处理
const handleTableChange = (pag: TablePaginationConfig) => {
  pagination.current = pag.current || 1
  pagination.pageSize = pag.pageSize || 10
  fetchGroups()
}

// 显示创建弹窗
const showCreateModal = () => {
  modalType.value = 'create'
  formState.id = undefined
  formState.name = ''
  formState.description = ''
  formState.sort = 0
  modalVisible.value = true
}

// 显示编辑弹窗
const showEditModal = (record: Group) => {
  modalType.value = 'edit'
  formState.id = record.id
  formState.name = record.name
  formState.description = record.description
  formState.sort = record.sort
  modalVisible.value = true
}

// 处理弹窗确认
const handleModalOk = async () => {
  try {
    await formRef.value.validate()
  } catch (error) {
    // 表单校验失败，直接返回
    return
  }

  try {
    if (modalType.value === 'create') {
      // 创建分组
      const response = await axios.post('http://127.0.0.1:8080/docCatalogGroup/add', {
        name: formState.name,
        description: formState.description,
        sort: formState.sort
      })
      
      if (response.data.code === 0) {
      message.success('创建成功')
        modalVisible.value = false
        fetchGroups()
      } else {
        message.error(response.data.message || '创建失败')
      }
    } else {
      // 更新分组
      const response = await axios.post('http://127.0.0.1:8080/docCatalogGroup/updateById', {
        docCatalogGroupId: formState.id,
        name: formState.name,
        description: formState.description,
        sort: formState.sort
      })
      
      if (response.data.code === 0) {
      message.success('更新成功')
    modalVisible.value = false
    fetchGroups()
      } else {
        message.error(response.data.message || '更新失败')
      }
    }
  } catch (error) {
    console.error('操作失败:', error)
    message.error('操作失败，请稍后重试')
  }
}

// 处理弹窗取消
const handleModalCancel = () => {
  modalVisible.value = false
  // 清空表单校验错误
  nextTick(() => {
    formRef.value?.clearValidate()
  })
}

// 删除分组
const handleDelete = async (record: Group) => {
  try {
    const response = await axios.post('http://127.0.0.1:8080/docCatalogGroup/deleteById', {
      docCatalogGroupId: record.id
    })
    
    if (response.data.code === 0) {
    message.success('删除成功')
    fetchGroups()
    } else {
      message.error(response.data.message || '删除失败')
    }
  } catch (error) {
    console.error('删除失败:', error)
    message.error('删除失败，请稍后重试')
  }
}

// 发布分组
const handlePublish = async (record: Group) => {
  try {
    const response = await axios.post('http://127.0.0.1:8080/docCatalogGroup/update/status', {
      docCatalogGroupId: record.id,
      status: 2
    })
    
    if (response.data.code === 0) {
    message.success('发布成功')
    fetchGroups()
    } else {
      message.error(response.data.message || '发布失败')
    }
  } catch (error) {
    console.error('发布失败:', error)
    message.error('发布失败，请稍后重试')
  }
}

// 下架分组
const handleUnpublish = async (record: Group) => {
  try {
    const response = await axios.post('http://127.0.0.1:8080/docCatalogGroup/update/status', {
      docCatalogGroupId: record.id,
      status: 1
    })
    
    if (response.data.code === 0) {
    message.success('下架成功')
    fetchGroups()
    } else {
      message.error(response.data.message || '下架失败')
    }
  } catch (error) {
    console.error('下架失败:', error)
    message.error('下架失败，请稍后重试')
  }
}

onMounted(() => {
  fetchGroups()
})
</script>

<style scoped>
.group-manage {
  padding: 24px;
}

.search-form {
  margin-bottom: 16px;
  padding: 24px 24px 24px 0;
  background: #fff;
  border-radius: 2px;
}

.search-form :deep(.ant-form-item) {
  margin-bottom: 0;
}

.table-operations {
  margin-bottom: 16px;
}

.text-danger {
  color: #ff4d4f;
}
</style> 