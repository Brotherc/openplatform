<template>
  <div class="menu-manage">
    <div class="header">
      <a-button type="primary" @click="showAddModal">
        <plus-outlined />
        新增菜单
      </a-button>
    </div>

    <a-table
      :columns="columns"
      :data-source="menuList"
      :loading="loading"
      :pagination="pagination"
      @change="handleTableChange"
      row-key="menuId"
      :expandable="false"
    >
      <template #bodyCell="{ column, record }">
        <template v-if="column.key === 'status'">
          <a-tag :color="record.status === 1 ? 'red' : 'green'">
            {{ record.status === 1 ? '禁用' : '启用' }}
          </a-tag>
        </template>
        <template v-else-if="column.key === 'action'">
          <a-space>
            <a-button type="link" size="small" @click="showEditModal(record)">
              编辑
            </a-button>
            <a-button type="link" size="small" @click="handleDelete(record)" danger>
              删除
            </a-button>
          </a-space>
        </template>
      </template>
    </a-table>

    <!-- 新增/编辑菜单弹窗 -->
    <a-modal
      v-model:open="modalVisible"
      :title="isEdit ? '编辑菜单' : '新增菜单'"
      @ok="handleSubmit"
      @cancel="handleCancel"
      :confirm-loading="submitLoading"
      ok-text="确定"
      cancel-text="取消"
    >
      <a-form
        ref="formRef"
        :model="formData"
        :rules="rules"
        layout="vertical"
      >
        <a-form-item label="菜单名称" name="name">
          <a-input v-model:value="formData.name" placeholder="请输入菜单名称" />
        </a-form-item>
        <a-form-item label="菜单路径" name="path">
          <a-input v-model:value="formData.path" placeholder="请输入菜单路径" />
        </a-form-item>
        <a-form-item label="父级菜单" name="parentId">
          <a-tree-select
            v-model:value="formData.parentId"
            :tree-data="getFilteredParentOptions()"
            placeholder="请选择父级菜单"
            allow-clear
            tree-default-expand-all
            :field-names="{ children: 'children', label: 'name', value: 'menuId' }"
          />
        </a-form-item>
        <a-form-item label="菜单图标" name="icon">
          <a-input v-model:value="formData.icon" placeholder="请输入菜单图标" />
        </a-form-item>
        <a-form-item label="排序" name="sort">
          <a-input-number v-model:value="formData.sort" :min="0" placeholder="请输入排序" />
        </a-form-item>
        <a-form-item label="状态" name="status">
          <a-radio-group v-model:value="formData.status">
            <a-radio :value="2">启用</a-radio>
            <a-radio :value="1">禁用</a-radio>
          </a-radio-group>
        </a-form-item>
        <a-form-item label="描述" name="description">
          <a-textarea v-model:value="formData.description" placeholder="请输入菜单描述" :rows="3" />
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, h } from 'vue'
import { message, Modal } from 'ant-design-vue'
import { Tag } from 'ant-design-vue'
import { PlusOutlined } from '@ant-design/icons-vue'
import type { FormInstance } from 'ant-design-vue'

interface MenuItem {
  id: number
  name: string
  path: string
  icon: string
  sort: number
  status: number
  description: string
  parentId: number | null
  level: number
  children?: MenuItem[]
  createTime: string
  updateTime: string
}

const loading = ref(false)
const modalVisible = ref(false)
const isEdit = ref(false)
const submitLoading = ref(false)
const formRef = ref<FormInstance>()

const menuList = ref<MenuItem[]>([])
const parentMenuOptions = ref<MenuItem[]>([])

const pagination = reactive({
  current: 1,
  pageSize: 10,
  total: 0,
  showSizeChanger: true,
  showQuickJumper: true,
  showTotal: (total: number) => `共 ${total} 条记录`
})

const formData = reactive({
  id: 0,
  name: '',
  path: '',
  icon: '',
  sort: 0,
  status: 2,
  description: '',
  parentId: null as number | null
})

const rules = {
  name: [{ required: true, message: '请输入菜单名称', trigger: 'blur' }],
  path: [{ required: true, message: '请输入菜单路径', trigger: 'blur' }],
  sort: [{ required: true, message: '请输入排序', trigger: 'blur' }]
}

const columns = [
  {
    title: '菜单名称',
    dataIndex: 'name',
    key: 'name'
  },
  {
    title: '菜单路径',
    dataIndex: 'path',
    key: 'path'
  },
  {
    title: '菜单图标',
    dataIndex: 'icon',
    key: 'icon'
  },
  {
    title: '排序',
    dataIndex: 'sort',
    key: 'sort',
    width: 80
  },
  {
    title: '状态',
    dataIndex: 'status',
    key: 'status',
    width: 100,
    customRender: ({ record }: any) => {
      if (record.status == 2) {
        return h(Tag, { color: 'green' }, { default: () => '启用' })
      } else {
        return h(Tag, { color: 'red' }, { default: () => '禁用' })
      }
    }
  },
  {
    title: '描述',
    dataIndex: 'description',
    key: 'description'
  },
  {
    title: '创建时间',
    dataIndex: 'createTime',
    key: 'createTime',
    width: 180,
    customRender: ({ record }: any) => formatDate(record.createTime)
  },
  {
    title: '操作',
    key: 'action',
    width: 150,
    fixed: 'right'
  }
]

// 构建树形菜单数据
const buildTreeData = (data: MenuItem[]): MenuItem[] => {
  const map = new Map()
  const result: MenuItem[] = []
  
  // 创建映射
  data.forEach(item => {
    map.set(item.id, { ...item, children: [] })
  })
  
  // 构建树形结构
  data.forEach(item => {
    const node = map.get(item.id)
    if (item.parentId === null || item.parentId === 0) {
      result.push(node)
    } else {
      const parent = map.get(item.parentId)
      if (parent) {
        parent.children.push(node)
      }
    }
  })
  
  return result
}

// 展平树形数据为列表，并移除children字段，防止a-table渲染树形表格
const flattenTreeData = (treeData: MenuItem[]): MenuItem[] => {
  const result: MenuItem[] = []
  const flatten = (items: MenuItem[]) => {
    items.forEach(item => {
      // 解构时去掉children字段
      const { children, ...rest } = item
      result.push(rest as MenuItem)
      if (children && children.length > 0) {
        flatten(children)
      }
    })
  }
  flatten(treeData)
  return result
}

// 获取菜单列表
const fetchMenuList = async () => {
  loading.value = true
  try {
    const pageParam = pagination.current - 1;
    // 联调真实分页接口 GET /menu/page?page=1&size=10
    const response = await fetch(`http://localhost:8080/menu/page?page=${pageParam}&size=${pagination.pageSize}&sort=sort,asc`)
    
    if (response.ok) {
      const data = await response.json()
      if (data.code === 0) {
        // 适配后端返回结构
        const content = data.data?.content || []
        menuList.value = flattenTreeData(content)
        parentMenuOptions.value = [] // 如需父子关系可后续处理
        pagination.total = data.data?.totalElements || 0
      } else {
        message.error(data.message || '获取菜单列表失败')
      }
    } else {
      // 模拟数据
      const mockData = [
        {
          menuId: 1,
          name: '系统管理',
          path: '/system',
          icon: 'setting',
          sort: 1,
          status: 2,
          description: '系统管理模块',
          parentId: null,
          createTime: '2024-01-01 10:00:00',
          updateTime: '2024-01-01 10:00:00'
        }
      ]
      menuList.value = flattenTreeData(mockData)
      pagination.total = mockData.length
    }
  } catch (error) {
    console.error('获取菜单列表失败:', error)
    message.error('获取菜单列表失败')
  } finally {
    loading.value = false
  }
}

// 表格变化处理
const handleTableChange = (pag: any) => {
  pagination.current = pag.current
  pagination.pageSize = pag.pageSize
  fetchMenuList()
}

// 显示新增弹窗
const showAddModal = async () => {
  isEdit.value = false
  modalVisible.value = true
  resetForm()
  await fetchParentMenuTree()
}

// 显示编辑弹窗
const showEditModal = async (record: MenuItem) => {
  isEdit.value = true
  modalVisible.value = true
  Object.assign(formData, {
    id: record.id || record.menuId,
    name: record.name,
    path: record.path,
    icon: record.icon,
    sort: record.sort,
    status: record.status,
    description: record.description,
    parentId: record.parentId === 0 ? null : record.parentId
  })
  // 获取父级菜单树
  await fetchParentMenuTree()
}

// 获取父级菜单树
const fetchParentMenuTree = async () => {
  try {
    const res = await fetch('http://localhost:8080/menu/tree')
    const data = await res.json()
    if (data.code === 0) {
      parentMenuOptions.value = data.data || []
    } else {
      parentMenuOptions.value = []
    }
  } catch (e) {
    parentMenuOptions.value = []
  }
}

// 重置表单
const resetForm = () => {
  Object.assign(formData, {
    id: 0,
    name: '',
    path: '',
    icon: '',
    sort: 0,
    status: 2,
    description: '',
    parentId: null
  })
  formRef.value?.resetFields()
}

// 提交表单
const handleSubmit = async () => {
  try {
    await formRef.value?.validate()
    submitLoading.value = true
    
    let response
    if (isEdit.value) {
      // 编辑接口联调
      response = await fetch('http://localhost:8080/menu/updateById', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json'
        },
        body: JSON.stringify({
          menuId: formData.id || formData.menuId,
          name: formData.name,
          description: formData.description,
          parentId: formData.parentId == null ? 0 : formData.parentId,
          status: formData.status,
          path: formData.path,
          icon: formData.icon,
          sort: formData.sort
        })
      })
    } else {
      // 新增菜单接口联调
      response = await fetch('http://localhost:8080/menu/add', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json'
        },
        body: JSON.stringify({
          name: formData.name,
          description: formData.description,
          parentId: formData.parentId == null ? 0 : formData.parentId,
          status: formData.status,
          path: formData.path,
          icon: formData.icon,
          sort: formData.sort
        })
      })
    }
    
    const resJson = await response.json()
    if (resJson.code === 0) {
      message.success(isEdit.value ? '编辑成功' : '新增成功')
      modalVisible.value = false
      fetchMenuList()
    } else {
      message.error(resJson.message || '操作失败')
    }
  } catch (error) {
    console.error('提交失败:', error)
    message.error('提交失败')
  } finally {
    submitLoading.value = false
  }
}

// 获取过滤后的父级菜单选项（排除当前编辑的菜单及其子菜单）
const getFilteredParentOptions = () => {
  if (!isEdit.value || !formData.id && !formData.menuId) {
    return parentMenuOptions.value
  }
  // 递归收集所有需要排除的id（当前节点及其所有子节点）
  const excludeIds = new Set()
  const collectIds = (nodes) => {
    nodes.forEach(node => {
      if (node.menuId === (formData.id || formData.menuId)) {
        addAllChildren(node)
      } else if (node.children && node.children.length > 0) {
        collectIds(node.children)
      }
    })
  }
  const addAllChildren = (node) => {
    excludeIds.add(node.menuId)
    if (node.children && node.children.length > 0) {
      node.children.forEach(child => addAllChildren(child))
    }
  }
  collectIds(parentMenuOptions.value)
  // 递归过滤
  const filterTree = (nodes) => {
    return nodes
      .filter(node => !excludeIds.has(node.menuId))
      .map(node => ({
        ...node,
        children: node.children ? filterTree(node.children) : []
      }))
  }
  return filterTree(parentMenuOptions.value)
}

// 取消操作
const handleCancel = () => {
  modalVisible.value = false
  resetForm()
}

// 删除菜单
const handleDelete = (record: MenuItem) => {
  Modal.confirm({
    title: '确认删除',
    content: `确定要删除菜单"${record.name}"吗？`,
    okText: '确定',
    cancelText: '取消',
    onOk: async () => {
      try {
        const response = await fetch('http://localhost:8080/menu/deleteById', {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json'
          },
          body: JSON.stringify({ menuId: record.id || record.menuId })
        })
        const resJson = await response.json()
        if (resJson.code === 0) {
          message.success('删除成功')
          fetchMenuList()
        } else {
          message.error(resJson.message || '删除失败')
        }
      } catch (error) {
        console.error('删除失败:', error)
        message.error('删除失败')
      }
    }
  })
}

// 时间戳转日期字符串
function formatDate(ts: number | string) {
  if (!ts) return ''
  const date = new Date(Number(ts))
  const pad = (n: number) => n.toString().padStart(2, '0')
  return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())} ${pad(date.getHours())}:${pad(date.getMinutes())}:${pad(date.getSeconds())}`
}

onMounted(() => {
  fetchMenuList()
})
</script>

<style scoped>
.menu-manage {
  padding: 24px;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.header h2 {
  margin: 0;
  color: #1890ff;
}
</style> 