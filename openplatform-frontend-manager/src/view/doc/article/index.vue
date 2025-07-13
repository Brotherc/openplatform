<template>
  <div class="article-manage-flex">
    <div class="left-tree">
      <!-- 左侧目录树 -->
      <div class="tree-container">
        <div class="tree-header">
          <a-space>
            <a-select
              v-model:value="selectedGroup"
              style="width: 200px"
              placeholder="请选择分组"
              @change="handleGroupChange"
            >
              <a-select-option v-for="group in groups" :key="group.id" :value="group.id">
                {{ group.name }}
              </a-select-option>
            </a-select>
            <a-dropdown>
              <a-button type="primary">
                <template #icon><ellipsis-outlined /></template>
              </a-button>
              <template #overlay>
                <a-menu>
                  <a-menu-item key="create" @click="showCreateModal">
                    <plus-outlined />
                    新增
                  </a-menu-item>
                  <a-menu-item key="publish" :disabled="checkedKeys.length === 0" @click="() => handlePublish(checkedKeys)">
                    <check-circle-outlined />
                    发布
                  </a-menu-item>
                  <a-menu-item key="unpublish" :disabled="checkedKeys.length === 0" @click="() => handleUnpublish(checkedKeys)">
                    <close-circle-outlined />
                    下架
                  </a-menu-item>
                  <a-menu-item :disabled="checkedKeys.length === 0">
                    <a-popconfirm
                      title="确定要删除选中的节点吗？"
                      ok-text="确定"
                      cancel-text="取消"
                      @confirm="() => handleDelete(checkedKeys)"
                    >
                      <span>
                        <delete-outlined />
                        删除
                      </span>
                    </a-popconfirm>
                  </a-menu-item>
                </a-menu>
              </template>
            </a-dropdown>
          </a-space>
        </div>
        <div class="tree-content">
          <a-tree
            v-model:selectedKeys="selectedKeys"
            v-model:checkedKeys="checkedKeys"
            :tree-data="treeData"
            :checkable="true"
            :block-node="true"
            :show-title="true"
            @select="onSelect"
            @check="onCheck"
          >
            <template #title="{ title, type, status, dataRef }">
              <div class="tree-node-content">
                <folder-outlined v-if="type === 1" style="margin-right: 8px" />
                <file-outlined v-else style="margin-right: 8px" />
                <span class="node-title" :title="title">{{ title }}</span>
                <span class="node-status">
                  <a-tag v-if="status === 2" color="success">已发布</a-tag>
                  <a-tag v-else color="error">未发布</a-tag>
                </span>
                <div class="node-actions" @click.stop>
                  <a-dropdown v-if="type !== 'group'" :trigger="['click']">
                    <a class="ant-dropdown-link" @click.prevent.stop>
                      <more-outlined />
                    </a>
                    <template #overlay>
                      <a-menu>
                        <a-menu-item key="edit" @click="() => {
                          handleEdit(dataRef);
                          selectedKeys.value = [dataRef.key];
                        }">
                          <edit-outlined />
                          编辑
                        </a-menu-item>
                        <a-menu-item key="publish" @click="() => {
                          handlePublish([dataRef.key]);
                          selectedKeys.value = [dataRef.key];
                        }">
                          <check-circle-outlined />
                          发布
                        </a-menu-item>
                        <a-menu-item key="unpublish" @click="() => {
                          handleUnpublish([dataRef.key]);
                          selectedKeys.value = [dataRef.key];
                        }">
                          <close-circle-outlined />
                          下架
                        </a-menu-item>
                        <a-popconfirm
                          v-model:visible="deleteConfirmVisible"
                          title="确定要删除这个节点吗？"
                          ok-text="确定"
                          cancel-text="取消"
                          @confirm="() => {
                            handleSingleDelete(dataRef.key);
                            selectedKeys.value = [];
                            selectedArticle.value = null;
                          }"
                        >
                          <a-menu-item key="delete">
                            <delete-outlined />
                            删除
                          </a-menu-item>
                        </a-popconfirm>
                      </a-menu>
                    </template>
                  </a-dropdown>
                </div>
              </div>
            </template>
          </a-tree>
        </div>
      </div>
    </div>
    <div class="right-content">
      <div class="editor-container" v-if="selectedArticle">
        <div class="editor-header">
          <a-space>
            <h2>{{ selectedArticle.title }}</h2>
            <div class="editor-actions">
              <a-button type="primary" @click="handleSave">保存</a-button>
            </div>
          </a-space>
        </div>
        <div class="editor-content">
          <MdEditor v-model="selectedArticle.content" style="height: 100%; flex: 1; width: 100%;" />
        </div>
      </div>
      
      <div class="api-detail-container" v-else-if="apiDetail">
        <div class="api-title">
          <span class="api-title-main">{{ apiDetail.name }}</span>
          <span class="api-title-cn">{{ apiDetail.cnName }}</span>
        </div>
        <div class="api-title-desc">{{ apiDetail.description }}</div>

        <div class="api-section">
          <div class="api-section-title">请求参数</div>
          <a-table
            :columns="paramColumns"
            :data-source="apiDetail.reqParamDisplayJson"
            :pagination="false"
            :show-header="true"
            :bordered="false"
            size="middle"
            :row-key="record => record.name"
            :expandable="{ childrenColumnName: 'children' }"
          >
            <template #bodyCell="{ column, record }">
              <template v-if="column.key === 'required'">
                {{ record.required ? '是' : '否' }}
              </template>
            </template>
          </a-table>
        </div>

        <div class="api-section">
          <div class="api-section-title">响应参数</div>
          <a-table
            :columns="paramColumns"
            :data-source="apiDetail.returnInfoDisplayJson"
            :pagination="false"
            :show-header="true"
            :bordered="false"
            size="middle"
            :row-key="record => record.name"
            :expandable="{ childrenColumnName: 'children' }"
          >
            <template #bodyCell="{ column, record }">
              <template v-if="column.key === 'required'">
                {{ record.required ? '是' : '否' }}
              </template>
            </template>
          </a-table>
        </div>
      </div>
      
      <div class="empty-container" v-else>
        <a-empty description="请选择或创建一篇文章" />
      </div>
    </div>
  </div>

  <!-- 创建/编辑弹窗 -->
  <a-modal
    v-model:visible="modalVisible"
    :title="modalType === 'create' ? '新建' : '编辑'"
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
      <a-form-item label="类型" name="type">
        <a-radio-group v-model:value="formState.type" :disabled="modalType === 'edit'">
          <a-radio :value="1">目录</a-radio>
          <a-radio :value="2">文章</a-radio>
          <a-radio :value="3">API</a-radio>
        </a-radio-group>
      </a-form-item>
      <a-form-item label="父级节点" name="parentId">
        <a-tree-select
          v-model:value="formState.parentId"
          style="width: 100%"
          :dropdown-style="{ maxHeight: '400px', overflow: 'auto' }"
          :tree-data="treeData"
          placeholder="请选择父级节点"
          :field-names="{ label: 'title', value: 'key', children: 'children' }"
          :tree-default-expand-all="true"
          allowClear
        />
      </a-form-item>
      <a-form-item v-if="formState.type === 3" label="关联API节点" name="apiInfoCategoryId">
        <a-tree-select
          v-model:value="formState.apiInfoCategoryId"
          style="width: 100%"
          :dropdown-style="{ maxHeight: '400px', overflow: 'auto' }"
          :tree-data="apiTreeData"
          placeholder="请选择API节点"
          :field-names="{ label: 'title', value: 'key', children: 'children' }"
          :tree-default-expand-all="true"
          :loading="apiTreeLoading"
          allowClear
        />
      </a-form-item>
      <a-form-item label="标题" name="title">
        <a-input v-model:value="formState.title" placeholder="请输入标题" />
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
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, watch, nextTick } from 'vue'
import { message } from 'ant-design-vue'
import { PlusOutlined, EllipsisOutlined, CheckCircleOutlined, CloseCircleOutlined, FolderOutlined, FolderOpenOutlined, FileOutlined, EditOutlined, DeleteOutlined, MoreOutlined } from '@ant-design/icons-vue'
import { MdEditor } from 'md-editor-v3'
import 'md-editor-v3/lib/style.css'
import axios from 'axios'

// 配置axios默认值
axios.defaults.baseURL = 'http://127.0.0.1:8080'

interface Group {
  id: string
  name: string
}

interface TreeNode {
  key: string
  title: string
  type: 1 | 2 | 3  // 1-目录，2-文章，3-API
  children?: TreeNode[]
  parentKey?: string
  sort: number
  status: number
}

interface Article {
  id: string
  title: string
  content: string
  type: 1 | 2 | 3  // 1-目录，2-文章，3-API
  parentId?: string
  sort: number
}

interface FormState {
  type: 1 | 2 | 3  // 1-目录，2-文章，3-API
  title: string
  sort: number
  parentId?: string
  docCatalogId?: string  // 添加主键字段
  apiInfoCategoryId?: string  // 添加API节点主键字段
}

// 状态定义
const groups = ref<Group[]>([])
const selectedGroup = ref<string>('')
const treeData = ref<TreeNode[]>([])
const selectedKeys = ref<string[]>([])
const checkedKeys = ref<string[]>([])
const selectedArticle = ref<Article | null>(null)
const modalVisible = ref(false)
const modalType = ref<'create' | 'edit'>('create')
const formRef = ref()
const deleteConfirmVisible = ref(false)

// API树相关
const apiTreeData = ref([])
const apiTreeLoading = ref(false)

// API详情相关
const apiDetail = ref(null)
const apiDetailLoading = ref(false)
const currentApiDocCatalogId = ref<string | null>(null)

// 获取API详情
const fetchApiDetail = async (docCatalogId: string) => {
  currentApiDocCatalogId.value = docCatalogId
  apiDetailLoading.value = true
  try {
    const response = await axios.get('/docCatalog/getApiInfoById', {
      params: { docCatalogId }
    })
    if (response.data && response.data.code === 0 && response.data.data) {
      apiDetail.value = response.data.data
    } else {
      apiDetail.value = null
      message.error(response.data.message || '获取API详情失败')
    }
  } catch (error) {
    console.error('获取API详情失败:', error)
    message.error('获取API详情失败，请稍后重试')
    apiDetail.value = null
  } finally {
    apiDetailLoading.value = false
  }
}

// 参数表格列定义
const paramColumns = [
  { title: '参数名', dataIndex: 'name', key: 'name', width: 180 },
  { title: '类型', dataIndex: 'type', key: 'type', width: 120 },
  { title: '是否必填', dataIndex: 'required', key: 'required', width: 100 },
  { title: '示例值', dataIndex: 'example', key: 'example', width: 120 },
  { title: '描述', dataIndex: 'description', key: 'description' }
]

const formState = reactive<FormState>({
  type: 2,  // 默认创建文章
  title: '',
  sort: 0,
  apiInfoCategoryId: undefined
})

const rules = {
  type: [{ required: true, message: '请选择类型' }],
  title: [{ required: true, message: '请输入标题' }],
  sort: [{ required: true, message: '请输入排序号' }],
  parentId: [],
  apiInfoCategoryId: [
    { required: true, message: '请选择关联API节点', trigger: 'change' }
  ]
}

// 获取分组列表
const fetchGroups = async () => {
  try {
    const response = await axios.get('http://127.0.0.1:8080/docCatalogGroup/getList')
    
    if (response.data.code === 0) {
      groups.value = response.data.data.map(item => ({
        id: item.docCatalogGroupId,
        name: item.name
      }))
      // 自动选择第一个分组
      if (groups.value.length > 0 && !selectedGroup.value) {
        selectedGroup.value = groups.value[0].id
      }
    } else {
      message.error(response.data.message || '获取分组列表失败')
    }
  } catch (error) {
    console.error('获取分组列表失败:', error)
    message.error('获取分组列表失败，请稍后重试')
  }
}

// 获取文档树
const fetchDocTree = async () => {
  if (!selectedGroup.value) return
  try {
    const response = await axios.get('http://127.0.0.1:8080/docCatalog/getTree', {
      params: {
        docCatalogGroupId: selectedGroup.value
      }
    })
    
    if (response.data.code === 0) {
      treeData.value = response.data.data
      // 检查右侧详情是否还在树中
      if (
        currentApiDocCatalogId.value &&
        !findNodeInTree(treeData.value, currentApiDocCatalogId.value)
      ) {
        apiDetail.value = null
        currentApiDocCatalogId.value = null
      }
    } else {
      message.error(response.data.message || '获取文档树失败')
    }
  } catch (error) {
    console.error('获取文档树失败:', error)
    message.error('获取文档树失败，请稍后重试')
  }
}

function findNodeInTree(tree, key) {
  for (const node of tree) {
    if (String(node.key) === String(key)) return true
    if (node.children && findNodeInTree(node.children, key)) return true
  }
  return false
}

// 处理apiTreeData，只允许type为2的节点可选
function processApiTreeForSelect(tree) {
  return (tree || []).map(node => {
    const newNode = { ...node }
    if (newNode.type !== 2) {
      newNode.disabled = true
    }
    if (newNode.children && newNode.children.length > 0) {
      newNode.children = processApiTreeForSelect(newNode.children)
    }
    return newNode
  })
}

// 获取API树
const fetchApiTree = async () => {
  apiTreeLoading.value = true
  try {
    const response = await axios.get('/apiInfoCategory/getTree')
    if (response.data && response.data.code === 0) {
      apiTreeData.value = processApiTreeForSelect(response.data.data)
    } else {
      message.error(response.data.message || '获取API树失败')
    }
  } catch (error) {
    message.error('获取API树失败')
  } finally {
    apiTreeLoading.value = false
  }
}

// 监听分组选择变化
watch(selectedGroup, (newValue) => {
  if (newValue) {
    fetchDocTree()
  }
})

// 监听弹窗打开和类型变化，类型为API时拉取API树
watch([modalVisible, () => formState.type], ([visible, type]) => {
  if (visible && type === 3) {
    fetchApiTree()
  }
})

// 选择api节点时自动填充标题
watch(() => formState.apiInfoCategoryId, (val) => {
  if (formState.type === 3 && val && !formState.title) {
    // 在apiTreeData中查找title
    function findTitle(nodes, key) {
      for (const node of nodes) {
        if (node.key === key) return node.title
        if (node.children) {
          const t = findTitle(node.children, key)
          if (t) return t
        }
      }
      return ''
    }
    const title = findTitle(apiTreeData.value, val)
    if (title) formState.title = title
  }
})

// 处理分组变更
const handleGroupChange = (value: string) => {
  selectedGroup.value = value
  selectedKeys.value = []
  checkedKeys.value = []
  selectedArticle.value = null
  apiDetail.value = null
}

// 选择节点
const onSelect = async (selectedKeys: string[], info: any) => {
  if (selectedKeys.length === 0) {
    selectedArticle.value = null;
    apiDetail.value = null;
    return;
  }
  const node = info.node
  if (node.dataRef.type === 2) {  // 2 表示文章
    try {
      const response = await axios.get('http://127.0.0.1:8080/docCatalog/getDocumentById', {
        params: {
          id: node.dataRef.key
        }
      })
      
      if (response.data.code === 0) {
        selectedArticle.value = {
          id: node.dataRef.key,
          title: node.dataRef.title,
          content: response.data.data.content || '',
          type: 2,
          sort: node.dataRef.sort,
          parentId: node.dataRef.parentKey,
        }
        apiDetail.value = null
      } else {
        message.error(response.data.message || '获取文章详情失败')
        selectedArticle.value = null;
      }
    } catch (error) {
      console.error('获取文章详情失败:', error)
      message.error('获取文章详情失败，请稍后重试')
      selectedArticle.value = null;
    }
  } else if (node.dataRef.type === 3) {  // 3 表示API
    selectedArticle.value = null
    fetchApiDetail(node.dataRef.key)
  } else {
    selectedArticle.value = null
    apiDetail.value = null
  }
}

// 处理勾选
const onCheck = (checkedKeys: string[], info: any) => {
  console.log('checkedKeys:', checkedKeys)
  console.log('info:', info)
}

// 显示创建弹窗
const showCreateModal = () => {
  if (!selectedGroup.value) {
    message.warning('请先选择分组')
    return
  }
  modalType.value = 'create'
  // 重置所有表单字段
  formState.type = 2  // 默认创建文章
  formState.title = ''
  formState.sort = 0
  formState.parentId = undefined
  formState.apiInfoCategoryId = undefined
  modalVisible.value = true
}

// 处理编辑
const handleEdit = async (node: any) => {
  console.log('handleEdit called with node:', node);
  if (!node) {
    console.error('No node data provided');
    return;
  }
  
  modalType.value = 'edit';
  formState.docCatalogId = node.key;  // 设置主键
  formState.title = node.title;
  formState.sort = node.sort;
  formState.parentId = node.parentKey;
  formState.type = node.type;
  if (node.type === 3) {
    try {
      const response = await axios.get('/docCatalog/getApiInfoById', { params: { docCatalogId: node.key } });
      if (response.data && response.data.code === 0 && response.data.data) {
        formState.apiInfoCategoryId = response.data.data.apiInfoCategoryId;
      } else {
        formState.apiInfoCategoryId = undefined;
      }
    } catch (e) {
      formState.apiInfoCategoryId = undefined;
    }
  } else {
    formState.apiInfoCategoryId = undefined;
  }
  modalVisible.value = true;
  console.log('Form state after setting:', formState);
}

// 处理删除
const handleDelete = async (keys: string[]) => {
  if (!selectedGroup.value) {
    message.warning('请先选择分组')
    return
  }
  try {
    const response = await axios.post('http://127.0.0.1:8080/docCatalog/deleteByIdList', {
      docCatalogIdList: keys
    })
    if (response.data.code === 0) {
      message.success('删除成功')
      fetchDocTree()
      // 如果当前右侧显示的apiDetail被删除了，清空右侧
      if (currentApiDocCatalogId.value && keys.includes(String(currentApiDocCatalogId.value))) {
        apiDetail.value = null
        currentApiDocCatalogId.value = null
      }
    } else {
      message.error(response.data.message || '删除失败')
    }
  } catch (error) {
    console.error('删除失败:', error)
    message.error('删除失败，请稍后重试')
  }
}

// 处理单个删除
const handleSingleDelete = async (key: string) => {
  if (!selectedGroup.value) {
    message.warning('请先选择分组')
    return
  }
  try {
    const response = await axios.post('http://127.0.0.1:8080/docCatalog/deleteById', {
      docCatalogId: key
    })
    
    if (response.data.code === 0) {
      message.success('删除成功')
      fetchDocTree()
    } else {
      message.error(response.data.message || '删除失败')
    }
  } catch (error) {
    console.error('删除失败:', error)
    message.error('删除失败，请稍后重试')
  }
}

// 处理分组删除
const handleGroupDelete = async () => {
  if (!selectedGroup.value) {
    message.warning('请先选择分组')
    return
  }
  try {
    // TODO: 调用后端API删除分组
    message.success('删除成功')
    fetchDocTree()
  } catch (error) {
    message.error('删除失败')
  }
}

// 处理保存文章
const handleSave = async () => {
  if (!selectedArticle.value) return
  try {
    const response = await axios.post('http://127.0.0.1:8080/docCatalog/saveDocument', {
      docCatalogId: selectedArticle.value.id,
      content: selectedArticle.value.content
    })
    
    if (response.data.code === 0) {
      message.success('保存成功')
    } else {
      message.error(response.data.message || '保存失败')
    }
  } catch (error) {
    console.error('保存失败:', error)
    message.error('保存失败，请稍后重试')
  }
}

// 处理弹窗确认
const handleModalOk = async () => {
  try {
    await formRef.value.validate()
  } catch (error) {
    return
  }

  try {
    if (modalType.value === 'create') {
      // 创建节点
      const response = await axios.post('/docCatalog/add', {
        name: formState.title,
        docCatalogGroupId: selectedGroup.value,
        parentId: formState.parentId,
        type: formState.type,
        sort: formState.sort,
        apiInfoCategoryId: formState.apiInfoCategoryId
      })
      
      if (response.data.code === 0) {
        message.success('创建成功')
        modalVisible.value = false
        fetchDocTree()
      } else {
        message.error(response.data.message || '创建失败')
      }
    } else {
      // 更新节点
      const response = await axios.post('/docCatalog/updateById', {
        docCatalogId: formState.docCatalogId,
        name: formState.title,
        parentId: formState.parentId,
        sort: formState.sort,
        apiInfoCategoryId: formState.apiInfoCategoryId
      })
      
      if (response.data.code === 0) {
        message.success('更新成功')
        modalVisible.value = false
        fetchDocTree()
        // 如果是API节点，刷新右侧API详情
        if (formState.type === 3 && formState.docCatalogId) {
          fetchApiDetail(formState.docCatalogId)
        }
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

// 处理发布
const handlePublish = async (keys: string[]) => {
  if (!selectedGroup.value) {
    message.warning('请先选择分组')
    return
  }
  try {
    const response = await axios.post('http://127.0.0.1:8080/docCatalog/update/status/batch', {
      docCatalogIdList: keys,
      status: 2
    })
    
    if (response.data.code === 0) {
      message.success('发布成功')
      fetchDocTree()
    } else {
      message.error(response.data.message || '发布失败')
    }
  } catch (error) {
    console.error('发布失败:', error)
    message.error('发布失败，请稍后重试')
  }
}

// 处理下架
const handleUnpublish = async (keys: string[]) => {
  if (!selectedGroup.value) {
    message.warning('请先选择分组')
    return
  }
  try {
    const response = await axios.post('http://127.0.0.1:8080/docCatalog/update/status/batch', {
      docCatalogIdList: keys,
      status: 1
    })
    
    if (response.data.code === 0) {
      message.success('下架成功')
      fetchDocTree()
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
.article-manage-flex {
  display: flex;
  height: 100%;
}
.left-tree {
  width: 300px;
  min-width: 300px;
  max-width: 300px;
  height: 100%;
}
.right-content {
  flex: 1;
  min-width: 0;
  height: 100%;
  display: flex;
  flex-direction: column;
}
.tree-container {
  background: #fff;
  padding: 16px 0 16px 8px;
  border-radius: 2px;
  height: 100%;
  display: flex;
  flex-direction: column;
  position: relative;
  z-index: 1;
  overflow: hidden;
  min-width: 300px;
  border-right: 1px solid #f0f0f0;
}

.tree-header {
  margin-bottom: 16px;
  flex-shrink: 0;
  display: flex;
  align-items: center;
}

.tree-content {
  flex: 1;
  overflow: auto;
  min-width: 0;
}

.editor-container {
  width: 100%;
  background: #fff;
  padding: 16px 16px 16px 16px;
  border-radius: 2px;
  height: 100%;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.editor-header {
  margin-bottom: 16px;
  flex-shrink: 0;
}

.editor-header .ant-space {
  width: 100%;
  justify-content: space-between;
}

.editor-actions {
  display: flex;
  gap: 8px;
}

.editor-content {
  flex: 1;
  min-height: 0;
  height: 100%;
  display: flex;
  flex-direction: column;
  width: 100%;
}

.empty-container {
  background: #fff;
  padding: 16px;
  border-radius: 2px;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
}

:deep(.ant-tree) {
  background: transparent;
}

:deep(.ant-tree-node-selected) {
  background-color: #e6f7ff !important;
}

:deep(.ant-tree-node-content-wrapper) {
  flex: 1;
  min-width: 0;
}

.tree-node-content {
  display: flex;
  align-items: center;
  width: 100%;
}

.node-title {
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.node-status {
  margin-left: auto;
  padding: 0 8px;
}

.node-actions {
  padding-left: 8px;
}

.node-status :deep(.ant-tag) {
  margin: 0;
  padding: 0 8px;
  height: 22px;
  line-height: 20px;
  font-size: 12px;
}

:deep(.ant-dropdown-link) {
  color: rgba(0, 0, 0, 0.45);
  padding: 4px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
}

:deep(.ant-dropdown-link:hover) {
  color: #1890ff;
}

:deep(.ant-tree-indent-unit) {
  width: 24px;
}

:deep(.ant-tree-switcher) {
  width: 24px;
}

:deep(.ant-tree-switcher-icon) {
  color: rgba(0, 0, 0, 0.45);
  font-size: 16px;
}

:deep(.ant-tree-checkbox) {
  margin: 0;
  margin-right: 8px;
}

:deep(.ant-select) {
  width: 200px;
  flex-shrink: 0;
}

:deep(.ant-row) {
  margin: 0 !important;
  height: 100%;
  flex-wrap: nowrap;
}

:deep(.ant-col) {
  height: 100%;
  display: flex;
  flex-direction: column;
}

:deep(.ant-col-6) {
  flex: 0 0 300px;
  max-width: 300px;
  min-width: 300px;
}

:deep(.ant-col-18) {
  flex: 1;
  min-width: 400px;
}

:deep(.ant-space) {
  flex-wrap: nowrap;
  gap: 0 !important;
}

:deep(.ant-space-item) {
  margin-right: 8px;
}

.api-detail-container {
  background: #fff;
  padding: 32px 16px 24px 32px;
  border-radius: 4px;
  min-height: 100%;
}
.api-title {
  margin-bottom: 4px;
}
.api-title-main {
  font-size: 28px;
  font-weight: bold;
  margin-right: 16px;
}
.api-title-cn {
  color: #888;
  font-size: 20px;
  margin-left: 0;
  vertical-align: middle;
  position: relative;
  top: -4px;
}
.api-title-desc {
  color: #888;
  font-size: 16px;
  margin-bottom: 32px;
  margin-left: 2px;
}
.api-section {
  margin-bottom: 32px;
}
.api-section-title {
  font-weight: bold;
  font-size: 18px;
  margin-bottom: 12px;
}
:deep(.md-editor) {
  flex: 1;
  min-height: 0;
  height: 100%;
  width: 100%;
}
</style> 