<template>
  <div class="api-manage">
    <a-row :gutter="16">
      <!-- 左侧模块树 -->
      <a-col :span="6">
        <div class="tree-container">
          <div class="tree-header">
            <a-space>
              <a-button type="primary" @click="showCreateModal">
                <template #icon><plus-outlined /></template>
                新增
              </a-button>
              <a-dropdown>
                <a-button type="primary" :disabled="checkedKeys.length === 0">
                  <template #icon><ellipsis-outlined /></template>
                </a-button>
                <template #overlay>
                  <a-menu>
                    <a-menu-item key="publish" :disabled="checkedKeys.length === 0 || hasCategoryNode" @click="() => handlePublish(checkedKeys)">
                      <check-circle-outlined />
                      发布
                    </a-menu-item>
                    <a-menu-item key="unpublish" :disabled="checkedKeys.length === 0 || hasCategoryNode" @click="() => handleUnpublish(checkedKeys)">
                      <close-circle-outlined />
                      下架
                    </a-menu-item>
                    <a-menu-item :disabled="checkedKeys.length === 0">
                      <a-popconfirm
                        title="确定要删除选中的节点吗？"
                        ok-text="确定"
                        cancel-text="取消"
                        @confirm="() => handleBatchDelete(checkedKeys)"
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
              :block-node="true"
              :checkable="true"
              @select="onSelect"
              @check="onCheck"
            >
              <template #title="{ title, type, dataRef }">
                <div class="tree-node-content">
                  <folder-outlined v-if="type === 1" style="margin-right: 8px" />
                  <api-outlined v-else style="margin-right: 8px" />
                  <span class="node-title" :title="title">{{ title }}</span>
                  <a-tag v-if="type === 2" :color="dataRef.status === 2 ? 'success' : 'error'" style="margin-left: 8px">
                    {{ dataRef.status === 2 ? '已发布' : '未发布' }}
                  </a-tag>
                  <div class="node-actions" @click.stop>
                    <a-dropdown :trigger="['click']">
                      <a class="ant-dropdown-link" @click.prevent.stop>
                        <more-outlined />
                      </a>
                      <template #overlay>
                        <a-menu>
                          <a-menu-item key="edit" @click="() => handleEdit(dataRef)">
                            <edit-outlined />
                            编辑
                          </a-menu-item>
                          <a-menu-item key="publish" @click="() => handlePublish([dataRef.key])" :disabled="dataRef.type === 1">
                            <check-circle-outlined />
                            发布
                          </a-menu-item>
                          <a-menu-item key="unpublish" @click="() => handleUnpublish([dataRef.key])" :disabled="dataRef.type === 1">
                            <close-circle-outlined />
                            下架
                          </a-menu-item>
                          <a-popconfirm
                            title="确定要删除这个节点吗？"
                            ok-text="确定"
                            cancel-text="取消"
                            @confirm="() => handleDelete(dataRef.key)"
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
      </a-col>

      <!-- 右侧API详情 -->
      <a-col :span="19">
        <div class="api-detail" v-if="selectedApi">
          <div class="detail-header">
            <div class="detail-actions">
              <a-button type="primary" @click="handleSave">保存</a-button>
            </div>
          </div>
          <div class="detail-content">
            <a-form
              :model="apiForm"
              layout="vertical"
            >
              <a-row :gutter="16">
                <a-col :span="12">
                  <a-form-item label="API名称">
                    <a-input v-model:value="apiForm.name" placeholder="请输入API名称" />
                  </a-form-item>
                </a-col>
                <a-col :span="12">
                  <a-form-item label="中文名称">
                    <a-input v-model:value="apiForm.nameCn" placeholder="请输入中文名称" />
                  </a-form-item>
                </a-col>
              </a-row>
              <a-row :gutter="16">
                <a-col :span="24">
                  <a-form-item label="描述">
                    <a-textarea v-model:value="apiForm.description" placeholder="请输入API描述" :rows="4" />
                  </a-form-item>
                </a-col>
              </a-row>
              <a-row :gutter="16">
                <a-col :span="12">
                  <a-form-item label="请求方法">
                    <a-select v-model:value="apiForm.method" placeholder="请选择请求方法">
                      <a-select-option value="GET">GET</a-select-option>
                      <a-select-option value="POST">POST</a-select-option>
                      <a-select-option value="PUT">PUT</a-select-option>
                      <a-select-option value="DELETE">DELETE</a-select-option>
                    </a-select>
                  </a-form-item>
                </a-col>
                <a-col :span="12">
                  <a-form-item label="请求前缀">
                    <a-input v-model:value="apiForm.reqContextPath" placeholder="请输入请求前缀" />
                  </a-form-item>
                </a-col>
              </a-row>
              <a-row :gutter="16">
                <a-col :span="24">
                  <a-form-item label="请求路径">
                    <a-input v-model:value="apiForm.path" placeholder="请输入请求路径" />
                  </a-form-item>
                </a-col>
              </a-row>

              <a-row :gutter="16">
                <a-col :span="24">
                  <a-form-item label="请求参数">
                      <a-tabs v-model:activeKey="activeKey">
                        <a-tab-pane key="queryParam" tab="Query参数">
                          <a-space style="margin-bottom: 16px;">
                            <a-button @click="insertParamEvent('param')">新增</a-button>
                            <a-button @click="searchMethod('param')">刷新</a-button>
                          </a-space>

                          <vxe-table
                              show-overflow
                              keep-source
                              ref="tableRef"
                              :row-config="{keyField: 'id',drag: true}"
                              :loading="loading"
                              :tree-config="{transform: true, rowField: 'id', parentField: 'parentId', expandAll: true, defaultExpandAll: true}"
                              :edit-rules=paramRules
                              :valid-config="{ msgMode: 'full', theme: 'normal' }"
                              :edit-config="{trigger: 'click', mode: 'row', showStatus: true}"
                              :data="tableData">
                            <vxe-column field="name" title="名称" drag-sort width="250" tree-node :edit-render="{}">
                              <template #edit="{ row }">
                                <vxe-input v-model="row.name" mode="text"
                                           :disabled="disableName('param', row)"></vxe-input>
                              </template>
                            </vxe-column>
                            <vxe-column field="type" title="类型" width="125" :edit-render="{}">
                              <template #default="{ row }">
                                <span>{{ row.type }}</span>
                              </template>
                              <template #edit="{ row }">
                                <vxe-select v-model="row.type" transfer
                                            @change="onTypeChange('param', row, $event.value)">
                                  <vxe-option v-for="item in paramType" :key="item.value" :value="item.value"
                                              :label="item.label" :disabled="item.disabled"></vxe-option>
                                </vxe-select>
                              </template>
                            </vxe-column>
                            <vxe-column field="required" title="必填" width="100" :edit-render="{}">
                              <template #default="{ row }">
                                <span>{{ row.required ? '是' : '否' }}</span>
                              </template>
                              <template #edit="{ row }">
                                <vxe-select v-model="row.required" transfer>
                                  <vxe-option v-for="item in required" :key="item.value" :value="item.value"
                                              :label="item.label" :disabled="item.disabled"></vxe-option>
                                </vxe-select>
                              </template>
                            </vxe-column>
                            <vxe-column field="example" title="示例值" width="150" :edit-render="{}">
                              <template #edit="{ row }">
                                <vxe-input v-model="row.example" mode="text"></vxe-input>
                              </template>
                            </vxe-column>
                            <vxe-column field="description" title="描述" width="175" :edit-render="{}">
                              <template #edit="{ row }">
                                <vxe-input v-model="row.description" mode="text"></vxe-input>
                              </template>
                            </vxe-column>
                            <vxe-column title="操作">
                              <template #default="{ row }">
                                <a-button type="link" @click="insertNextRow('param', row, 'current')" :disabled="disableAddBrother('param', row)">添加相邻节点</a-button>
                                <a-button type="link" @click="insertRow('param', row, 'bottom')" :disabled="disableAddChildren('param', row)">添加子节点</a-button>
                                <a-button type="link" @click="removeRow('param', row)" danger>删除</a-button>
                              </template>
                            </vxe-column>
                          </vxe-table>
                        </a-tab-pane>
                        <a-tab-pane key="bodyParam" tab="Body">
                          <a-space style="margin-bottom: 16px;">
                            <a-button @click="insertParamEvent('body')">新增</a-button>
                            <a-button @click="searchMethod('body')">刷新</a-button>
                          </a-space>

                          <vxe-table
                              show-overflow
                              keep-source
                              ref="bodyTableRef"
                              :row-config="{keyField: 'id',drag: true}"
                              :loading="loading"
                              :tree-config="{transform: true, rowField: 'id', parentField: 'parentId', expandAll: true, defaultExpandAll: true}"
                              :edit-rules=paramRules
                              :valid-config="{ msgMode: 'full', theme: 'normal' }"
                              :edit-config="{trigger: 'click', mode: 'row', showStatus: true}"
                              :data="bodyTableData">
                            <vxe-column field="name" title="名称" drag-sort width="250" tree-node :edit-render="{}">
                              <template #edit="{ row }">
                                <vxe-input v-model="row.name" mode="text"
                                           :disabled="disableName('body', row)"></vxe-input>
                              </template>
                            </vxe-column>
                            <vxe-column field="type" title="类型" width="125" :edit-render="{}">
                              <template #default="{ row }">
                                <span>{{ row.type }}</span>
                              </template>
                              <template #edit="{ row }">
                                <vxe-select v-model="row.type" transfer
                                            @change="onTypeChange('body', row, $event.value)">
                                  <vxe-option v-for="item in paramType" :key="item.value" :value="item.value"
                                              :label="item.label" :disabled="item.disabled"></vxe-option>
                                </vxe-select>
                              </template>
                            </vxe-column>
                            <vxe-column field="required" title="必填" width="100" :edit-render="{}">
                              <template #default="{ row }">
                                <span>{{ row.required ? '是' : '否' }}</span>
                              </template>
                              <template #edit="{ row }">
                                <vxe-select v-model="row.required" transfer>
                                  <vxe-option v-for="item in required" :key="item.value" :value="item.value"
                                              :label="item.label" :disabled="item.disabled"></vxe-option>
                                </vxe-select>
                              </template>
                            </vxe-column>
                            <vxe-column field="example" title="示例值" width="150" :edit-render="{}">
                              <template #edit="{ row }">
                                <vxe-input v-model="row.example" mode="text"></vxe-input>
                              </template>
                            </vxe-column>
                            <vxe-column field="description" title="描述" width="175" :edit-render="{}">
                              <template #edit="{ row }">
                                <vxe-input v-model="row.description" mode="text"></vxe-input>
                              </template>
                            </vxe-column>
                            <vxe-column title="操作">
                              <template #default="{ row }">
                                <a-button type="link" @click="insertNextRow('body', row, 'current')" :disabled="disableAddBrother('body', row)">添加相邻节点</a-button>
                                <a-button type="link" @click="insertRow('body', row, 'bottom')" :disabled="disableAddChildren('body', row)">添加子节点</a-button>
                                <a-button type="link" @click="removeRow('body', row)" danger>删除
                                </a-button>
                              </template>
                            </vxe-column>
                          </vxe-table>
                        </a-tab-pane>
                        <a-tab-pane key="pathParam" tab="Path参数">
                          <a-space style="margin-bottom: 16px;">
                            <a-button @click="insertParamEvent('path')">新增</a-button>
                            <a-button @click="searchMethod('path')">刷新</a-button>
                          </a-space>

                          <vxe-table
                              show-overflow
                              keep-source
                              ref="pathTableRef"
                              :row-config="{keyField: 'id',drag: true}"
                              :loading="loading"
                              :tree-config="{transform: true, rowField: 'id', parentField: 'parentId', expandAll: true, defaultExpandAll: true}"
                              :edit-rules=paramRules
                              :valid-config="{ msgMode: 'full', theme: 'normal' }"
                              :edit-config="{trigger: 'click', mode: 'row', showStatus: true}"
                              :data="pathTableData">
                            <vxe-column field="name" title="名称" drag-sort width="250" tree-node :edit-render="{}">
                              <template #edit="{ row }">
                                <vxe-input v-model="row.name" mode="text"
                                           :disabled="disableName('path', row)"></vxe-input>
                              </template>
                            </vxe-column>
                            <vxe-column field="type" title="类型" width="125" :edit-render="{}">
                              <template #default="{ row }">
                                <span>{{ row.type }}</span>
                              </template>
                              <template #edit="{ row }">
                                <vxe-select v-model="row.type" transfer
                                            @change="onTypeChange('path', row, $event.value)">
                                  <vxe-option v-for="item in pathParamType" :key="item.value" :value="item.value"
                                              :label="item.label" :disabled="item.disabled"></vxe-option>
                                </vxe-select>
                              </template>
                            </vxe-column>
                            <vxe-column field="required" title="必填" width="100" :edit-render="{}">
                              <template #default="{ row }">
                                <span>{{ row.required ? '是' : '否' }}</span>
                              </template>
                              <template #edit="{ row }">
                                <vxe-select v-model="row.required" transfer>
                                  <vxe-option v-for="item in required" :key="item.value" :value="item.value"
                                              :label="item.label" :disabled="item.disabled"></vxe-option>
                                </vxe-select>
                              </template>
                            </vxe-column>
                            <vxe-column field="example" title="示例值" width="150" :edit-render="{}">
                              <template #edit="{ row }">
                                <vxe-input v-model="row.example" mode="text"></vxe-input>
                              </template>
                            </vxe-column>
                            <vxe-column field="description" title="描述" width="175" :edit-render="{}">
                              <template #edit="{ row }">
                                <vxe-input v-model="row.description" mode="text"></vxe-input>
                              </template>
                            </vxe-column>
                            <vxe-column title="操作">
                              <template #default="{ row }">
                                <a-button type="link" @click="insertNextRow('path', row, 'current')">添加相邻节点</a-button>
                                <a-button type="link" @click="removeRow('path', row)" danger>删除</a-button>
                              </template>
                            </vxe-column>
                          </vxe-table>
                        </a-tab-pane>
                      </a-tabs>
                  </a-form-item>
                </a-col>
              </a-row>

              <a-row :gutter="16">
                <a-col :span="24">
                  <a-form-item label="响应参数">

                      <a-space style="margin-bottom: 16px;">
                        <a-button @click="insertParamEvent('response')">新增</a-button>
                        <a-button @click="searchMethod('response')">刷新</a-button>
                      </a-space>

                      <vxe-table
                          show-overflow
                          keep-source
                          ref="responseTableRef"
                          :row-config="{keyField: 'id',drag: true}"
                          :loading="loading"
                          :tree-config="{transform: true, rowField: 'id', parentField: 'parentId', expandAll: true, defaultExpandAll: true}"
                          :edit-rules=paramRules
                          :valid-config="{ msgMode: 'full', theme: 'normal' }"
                          :edit-config="{trigger: 'click', mode: 'row', showStatus: true}"
                          :data="responseTableData">
                        <vxe-column field="name" title="名称" drag-sort width="250" tree-node :edit-render="{}">
                          <template #edit="{ row }" >
                            <vxe-input v-model="row.name" mode="text" :disabled="disableName('response', row)"></vxe-input>
                          </template>
                        </vxe-column>
                        <vxe-column field="type" title="类型" width="125" :edit-render="{}">
                          <template #default="{ row }">
                            <span>{{ row.type }}</span>
                          </template>
                          <template #edit="{ row }">
                            <vxe-select v-model="row.type" transfer @change="onTypeChange('response', row, $event.value)">
                              <vxe-option v-for="item in paramType" :key="item.value" :value="item.value" :label="item.label" :disabled="item.disabled"></vxe-option>
                            </vxe-select>
                          </template>
                        </vxe-column>
                        <vxe-column field="required" title="必填" width="100" :edit-render="{}">
                          <template #default="{ row }">
                            <span>{{ row.required ? '是' : '否' }}</span>
                          </template>
                          <template #edit="{ row }">
                            <vxe-select v-model="row.required" transfer>
                              <vxe-option v-for="item in required" :key="item.value" :value="item.value" :label="item.label" :disabled="item.disabled"></vxe-option>
                            </vxe-select>
                          </template>
                        </vxe-column>
                        <vxe-column field="example" title="示例值" width="150" :edit-render="{}">
                          <template #edit="{ row }">
                            <vxe-input v-model="row.example" mode="text"></vxe-input>
                          </template>
                        </vxe-column>
                        <vxe-column field="description" title="描述" width="175" :edit-render="{}">
                          <template #edit="{ row }">
                            <vxe-input v-model="row.description" mode="text"></vxe-input>
                          </template>
                        </vxe-column>
                        <vxe-column title="操作">
                          <template #default="{ row }">
                            <a-space>
                              <a-button type="link" @click="insertNextRow('response', row, 'current')" :disabled="disableAddBrother('response', row)">添加相邻节点</a-button>
                              <a-button type="link" @click="insertRow('response', row, 'bottom')" :disabled="disableAddChildren('response', row)">添加子节点</a-button>
                              <a-button type="link" @click="removeRow('response', row)" danger>删除</a-button>
                            </a-space>
                          </template>
                        </vxe-column>
                      </vxe-table>

                  </a-form-item>
                </a-col>
              </a-row>
            </a-form>
          </div>
        </div>
        <div class="empty-container" v-else>
          <a-empty description="请选择一个API" />
        </div>
      </a-col>
    </a-row>

    <!-- 创建/编辑弹窗 -->
    <a-modal
      v-model:visible="modalVisible"
      :title="modalType === 'create' ? '新增' : '编辑'"
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
          <a-select 
            v-model:value="formState.type" 
            placeholder="请选择类型"
            :disabled="modalType === 'edit'"
          >
            <a-select-option :value="1">分类</a-select-option>
            <a-select-option :value="2">API</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="名称" name="name">
          <a-input v-model:value="formState.name" placeholder="请输入名称" />
        </a-form-item>
        <a-form-item label="父级" name="parentId">
          <a-tree-select
            v-model:value="formState.parentId"
            :tree-data="filteredTreeData"
            placeholder="请选择父级"
            :field-names="{ 
              label: 'title', 
              value: 'value', 
              children: 'children' 
            }"
            :tree-default-expand-all="true"
            allow-clear
            :dropdown-style="{ maxHeight: '400px', overflow: 'auto' }"
            style="width: 100%"
          />
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed, nextTick } from 'vue'
import { message } from 'ant-design-vue'
import {
  PlusOutlined,
  FolderOutlined,
  ApiOutlined,
  EditOutlined,
  DeleteOutlined,
  MoreOutlined,
  EllipsisOutlined,
  CheckCircleOutlined,
  CloseCircleOutlined
} from '@ant-design/icons-vue'
import axios from 'axios'
import { VxeUI, VxeTableInstance } from 'vxe-table'

// 配置axios默认值
axios.defaults.baseURL = 'http://127.0.0.1:8080'

interface TreeNode {
  key: string
  title: string
  type: 1 | 2  // 1-模块，2-API
  children?: TreeNode[]
  parentKey?: string
  sort: number
  name?: string
  nameCn?: string
  description?: string
}

interface FormState {
  id: string | undefined
  type: 1 | 2 | undefined
  title: string
  sort: number
  parentId?: string
  name?: string
  nameCn?: string
  description?: string
}

interface ApiForm {
  name: string
  nameCn: string
  method: string
  reqContextPath: string
  path: string
  description: string
}

interface RowVO {
  id: number
  parentId: number | null
  name: string
  type: string
  required: boolean
  example: string
  description: string
}

// 状态定义
const treeData = ref<TreeNode[]>([])
const selectedKeys = ref<string[]>([])
const checkedKeys = ref<string[]>([])
const selectedApi = ref<TreeNode | null>(null)
const modalVisible = ref(false)
const modalType = ref<'create' | 'edit'>('create')
const formRef = ref()
const activeKey = ref('queryParam')
const currentNode = ref<TreeNode | null>(null)

const formState = reactive<FormState>({
  id: undefined,
  type: undefined,
  title: '',
  sort: 0,
  parentId: undefined,
  name: '',
  nameCn: '',
  description: ''
})

const apiForm = reactive<ApiForm>({
  name: '',
  nameCn: '',
  method: 'GET',
  reqContextPath: '',
  path: '',
  description: ''
})

const rules = {
  name: [{ required: true, message: '请输入名称' }],
  type: [{ required: true, message: '请选择类型' }]
}

const paramRules = {
  name: [{ required: true, message: '必须填写' }],
  type: [{ required: true, message: '必须填写' }],
  required: [{ required: true, message: '必须填写' }]
}

const paramType = ref([
  { label: 'byte', value: 'byte', disabled: false },
  { label: 'integer', value: 'integer', disabled: false },
  { label: 'long', value: 'long', disabled: false },
  { label: 'float', value: 'float', disabled: false },
  { label: 'double', value: 'double', disabled: false },
  { label: 'decimal', value: 'decimal', disabled: false },
  { label: 'string', value: 'string', disabled: false },
  { label: 'boolean', value: 'boolean', disabled: false },
  { label: 'object', value: 'object', disabled: false },
  { label: 'array', value: 'array', disabled: false }
])

const pathParamType = ref([
  { label: 'byte', value: 'byte', disabled: false },
  { label: 'integer', value: 'integer', disabled: false },
  { label: 'long', value: 'long', disabled: false },
  { label: 'float', value: 'float', disabled: false },
  { label: 'double', value: 'double', disabled: false },
  { label: 'decimal', value: 'decimal', disabled: false },
  { label: 'string', value: 'string', disabled: false },
  { label: 'boolean', value: 'boolean', disabled: false }
])

const required = ref([
  { label: '是', value: true, disabled: false },
  { label: '否', value: false, disabled: false }
])

const loading = ref(false)
const tableData = ref<RowVO[]>([])
const pathTableData = ref<RowVO[]>([])
const bodyTableData = ref<RowVO[]>([])
const responseTableData = ref<RowVO[]>([])

const tableRef = ref<VxeTableInstance<RowVO>>()
const pathTableRef = ref<VxeTableInstance<RowVO>>()
const bodyTableRef = ref<VxeTableInstance<RowVO>>()
const responseTableRef = ref<VxeTableInstance<RowVO>>()

const searchMethod = (type: string) => {
  const $table = getTableRefByParamType(type)
  if ($table) {
    // 清除所有状态
    $table.clearAll()
  }
  return Promise.resolve()
}

const stripInternalFields = (data: any[]): any[] => {
  return data.map(row => {
    const { _X_ROW_CHILD, id, parentId, ...rest } = row;
    if (_X_ROW_CHILD && Array.isArray(_X_ROW_CHILD)) {
      rest.children = stripInternalFields(_X_ROW_CHILD);
    }
    return rest;
  });
}

const insertRow = async (type: string, currRow: RowVO, locat: string) => {
  const $table = getTableRefByParamType(type)
  if ($table) {
    // 如果 null 则插入到目标节点顶部
    // 如果 -1 则插入到目标节点底部
    // 如果 row 则有插入到效的目标节点该行的位置
    const rid = Date.now()

    const name = currRow.type === 'array' ? 'items' : null
    if (locat === 'current') {
      const record = {
        id: rid,
        name: name,
        parentId: currRow.parentId // 父节点必须与当前行一致
      }
      const { row: newRow } = await $table.insertAt(record, currRow)
      await $table.setEditRow(newRow) // 插入子节点
    } else if (locat === 'top') {
      const record = {
        id: rid,
        name: name,
        parentId: currRow.id // 需要指定父节点，自动插入该节点中
      }
      const { row: newRow } = await $table.insert(record)
      await $table.setTreeExpand(currRow, true) // 将父节点展开
      await $table.setEditRow(newRow) // 插入子节点
    } else if (locat === 'bottom') {
      const record = {
        id: rid,
        name: name,
        parentId: currRow.id // 需要指定父节点，自动插入该节点中
      }
      const { row: newRow } = await $table.insertAt(record, -1)
      await $table.setTreeExpand(currRow, true) // 将父节点展开
      await $table.setEditRow(newRow) // 插入子节点
    }
  }
}

const getTableRefByParamType = (paramType: string) => {
  let $table
  if (paramType == 'param') {
    $table = tableRef.value
  } else if (paramType == 'path') {
    $table = pathTableRef.value
  } else if (paramType == 'body') {
     $table = bodyTableRef.value
  } else {
     $table = responseTableRef.value
  }
  return $table;
}

const insertNextRow = async (type: string, currRow: RowVO, locat: string) => {
  const $table = getTableRefByParamType(type)
  if ($table) {
    // 如果 null 则插入到目标节点顶部
    // 如果 -1 则插入到目标节点底部
    // 如果 row 则有插入到效的目标节点该行的位置
    const rid = Date.now()
    if (locat === 'current') {
      const record = {
        id: rid,
        parentId: currRow.parentId // 父节点必须与当前行一致
      }
      const { row: newRow } = await $table.insertNextAt(record, currRow)
      await $table.setEditRow(newRow) // 插入子节点
    }
  }
}

const removeRow = async (type: string, row: RowVO) => {
  const $table = getTableRefByParamType(type)
  if ($table) {
    await $table.remove(row)
  }
}

const insertParamEvent = async (type: string) => {
  const $table = getTableRefByParamType(type)
  if ($table) {
    const rid = Date.now()
    const record = {
      id: rid,
      parentId: null
    }
    const { row: newRow } = await $table.insertAt(record, -1)
    await $table.setEditRow(newRow)
  }
}

const checkParam = async () => {
  const $table = tableRef.value
  if ($table) {
    await $table.fullValidate(true)
    // 提示哪部分参数没有填写完整
    const paramList = $table.getFullData()
  }
}

const disableAddChildren = (type: string, row: RowVO): boolean => {
  if (row.type == 'object' || row.type == 'refObject') {
    return false
  }
  const $table = getTableRefByParamType(type)
  if ($table) {
    if (row.type == 'array' && $table.getTreeRowChildren(row) == null) {
      return false
    }
  }
  return true
}

const disableName = (type: string, row: RowVO): boolean => {
  const $table = getTableRefByParamType(type)
  if ($table) {
    const parentRow = $table.getParentRow(row)
    return parentRow && parentRow.type === 'array'
  }
  return false
}

const disableAddBrother = (type: string, row: RowVO): boolean => {
  const $table = getTableRefByParamType(type)
  if ($table) {
    const parentRow = $table.getParentRow(row)
    if (parentRow != null && parentRow.type === 'array') {
      return true;
    }
    if (type == 'body' || type == 'response') {
      return parentRow == null
    }
    return false
  }
  return false
}

const onTypeChange = (type: string, row: RowVO, value: string) => {
  const $table = getTableRefByParamType(type)

  if ($table) {
    // 删除子节点
    const children = $table.getTreeRowChildren(row)
    for (const child of children) {
      $table.remove(child)
    }
    if (value == 'array') {
      // 添加子节点
      insertRow(type, row, 'top');
    }
  }
}


// 获取树数据
const fetchTree = async () => {
  try {
    const response = await axios.get('/apiInfoCategory/getTree')
    if (response.data && response.data.code === 0) {
      // 处理返回的数据，确保符合树形结构要求
      const processTreeData = (data: any[]) => {
        return data.map(item => ({
          key: String(item.key), // 转换为字符串
          value: String(item.key), // 用于树形选择器的值
          title: item.title,
          type: item.type,
          status: item.status,
          parentKey: item.parentKey, // 保存原始的parentKey
          children: item.children ? processTreeData(item.children) : undefined
        }))
      }
      treeData.value = processTreeData(response.data.data)
      console.log('树形数据:', treeData.value) // 添加日志查看树形数据
    } else {
      message.error(response.data.message || '获取树数据失败')
    }
  } catch (error) {
    console.error('获取树数据失败:', error)
    message.error('获取树数据失败，请稍后重试')
  }
}

// 工具函数：将树结构平铺为数组，并生成唯一id和parentId
function flattenTreeWithId(tree, parentId = null, idSet = new Set()) {
  const flat = []
  function genId() {
    let id
    do {
      id = Math.random().toString(36).slice(2, 10) + Date.now().toString(36)
    } while (idSet.has(id))
    idSet.add(id)
    return id
  }
  function walk(nodes, parentId) {
    nodes.forEach(node => {
      const newNode = { ...node }
      newNode.id = genId()
      newNode.parentId = parentId
      if (newNode.children && Array.isArray(newNode.children) && newNode.children.length > 0) {
        const children = newNode.children
        delete newNode.children
        flat.push(newNode)
        walk(children, newNode.id)
      } else {
        delete newNode.children
        flat.push(newNode)
      }
    })
  }
  walk(tree, parentId)
  return flat
}

// 查询API详情并填充表单
const fetchApiDetail = async (apiInfoCategoryId: string | number) => {
  try {
    const response = await axios.get('/apiInfo/getByApiInfoCategoryId', {
      params: { apiInfoCategoryId }
    })
    if (response.data && response.data.code === 0 && response.data.data) {
      const detail = response.data.data
      // 填充表单
      apiForm.name = detail.name || ''
      apiForm.nameCn = detail.cnName || ''
      apiForm.method = detail.reqMethod || 'GET'
      apiForm.reqContextPath = detail.reqContextPath || ''
      apiForm.path = detail.reqPath || ''
      apiForm.description = detail.description || ''
      // 平铺参数表格
      tableData.value = flattenTreeWithId(detail.queryParam || [])
      pathTableData.value = flattenTreeWithId(detail.pathParam || [])
      bodyTableData.value = flattenTreeWithId(detail.requestBody || [])
      responseTableData.value = flattenTreeWithId(detail.responseBody || [])
    } else {
      // 没有数据时清空表单
      apiForm.name = ''
      apiForm.nameCn = ''
      apiForm.method = 'GET'
      apiForm.reqContextPath = ''
      apiForm.path = ''
      apiForm.description = ''
      tableData.value = []
      pathTableData.value = []
      bodyTableData.value = []
      responseTableData.value = []
    }
  } catch (error) {
    message.error('获取API详情失败')
  }
}

// 修改onSelect事件，点击API节点时查询详情
const onSelect = (selectedKeys: string[], info: any) => {
  if (info.selected && info.node.type === 2) {
    selectedApi.value = info.node
    fetchApiDetail(info.node.key)
  } else {
    selectedApi.value = null
  }
}

// 显示创建弹窗
const showCreateModal = () => {
  modalType.value = 'create'
  formState.id = undefined
  formState.type = 1
  formState.name = ''
  formState.parentId = undefined
  modalVisible.value = true
}

// 处理编辑
const handleEdit = (node: any) => {
  modalType.value = 'edit'
  currentNode.value = node
  formState.id = node.key
  formState.type = node.type
  formState.name = node.title
  formState.parentId = node.parentId
  modalVisible.value = true
}

// 处理删除
const handleDelete = async (key: string) => {
  try {
    const response = await axios.post('/apiInfoCategory/deleteById', {
      apiInfoCategoryId: Number(key)
    })
    if (response.data && response.data.code === 0) {
    message.success('删除成功')
    fetchTree()
    } else {
      message.error(response.data.message || '删除失败')
    }
  } catch (error) {
    console.error('删除失败:', error)
    message.error('删除失败，请稍后重试')
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
      const response = await axios.post('/apiInfoCategory/add', {
        name: formState.name,
        parentId: formState.parentId ? Number(formState.parentId) : 0,
        type: formState.type
      })
      if (response.data && response.data.code === 0) {
      message.success('创建成功')
        modalVisible.value = false
        fetchTree()
    } else {
        message.error(response.data.message || '创建失败')
      }
    } else {
      // 编辑接口调用
      const response = await axios.post('/apiInfoCategory/updateById', {
        apiInfoCategoryId: Number(formState.id),
        name: formState.name,
        parentId: formState.parentId ? Number(formState.parentId) : 0
      })
      if (response.data && response.data.code === 0) {
      message.success('更新成功')
    modalVisible.value = false
    fetchTree()
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
}

// 处理保存
const handleSave = async () => {
  // 先切换到所有标签页来加载数据
  const originalActiveKey = activeKey.value
  
  // 切换到Query参数标签页
  activeKey.value = 'queryParam'
  await nextTick()
  
  // 切换到Body参数标签页
  activeKey.value = 'bodyParam'
  await nextTick()
  
  // 切换到Path参数标签页
  activeKey.value = 'pathParam'
  await nextTick()
  
  // 切换到响应体标签页
  activeKey.value = 'responseBody'
  await nextTick()
  
  // 恢复原来的标签页
  activeKey.value = originalActiveKey
  await nextTick()

  // 获取最新的表格数据
  const queryParamsData = tableRef.value?.getTableData().fullData || []
  const queryParam = stripInternalFields(JSON.parse(JSON.stringify(queryParamsData)));

  const requestBodyData = bodyTableRef.value?.getTableData().fullData || []
  const requestBody = stripInternalFields(JSON.parse(JSON.stringify(requestBodyData)));

  const pathParamData = pathTableRef.value?.getTableData().fullData || []
  const pathParam = stripInternalFields(JSON.parse(JSON.stringify(pathParamData)));

  const responseBodyData = responseTableRef.value?.getTableData().fullData || []
  const responseBody = stripInternalFields(JSON.parse(JSON.stringify(responseBodyData)));

  // 准备提交的数据
  const submitData = {
    // 基本信息
    name: apiForm.name,
    cnName: apiForm.nameCn,
    reqMethod: apiForm.method,
    reqContextPath: apiForm.reqContextPath,
    reqPath: apiForm.path,
    description: apiForm.description,
    apiInfoCategoryId: selectedApi.value?.key,
    // 请求参数
    queryParam: queryParam,
    pathParam: pathParam,
    // 请求体 - 只取第一个元素
    requestBody: requestBody.length > 0 ? requestBody[0] : {},
    // 响应体 - 只取第一个元素
    responseBody: responseBody.length > 0 ? responseBody[0] : null
  }

  // 打印所有表单数据
  console.log('API详情表单数据:', submitData)

  try {
    // 调用保存API信息的接口
    const response = await axios.post('/apiInfo/save', submitData)
    if (response.data && response.data.code === 0) {
    message.success('保存成功')
    fetchTree()
    } else {
      message.error(response.data.message || '保存失败')
    }
  } catch (error) {
    console.error('保存失败:', error)
    message.error('保存失败，请稍后重试')
  }
}

// 处理勾选
const onCheck = (checked: string[], info: any) => {
  console.log('checked:', checked)
  console.log('info:', info)
}

// 处理批量删除
const handleBatchDelete = async (keys: string[]) => {
  try {
    const response = await axios.post('/apiInfoCategory/deleteByIdList', {
      apiInfoCategoryIdList: keys.map(key => Number(key))
    })
    if (response.data && response.data.code === 0) {
    message.success('删除成功')
    fetchTree()
    } else {
      message.error(response.data.message || '删除失败')
    }
  } catch (error) {
    console.error('删除失败:', error)
    message.error('删除失败，请稍后重试')
  }
}

// 处理发布
const handlePublish = async (keys: string[]) => {
  try {
    const response = await axios.post('/apiInfoCategory/update/status/batch', {
      apiInfoCategoryIdList: keys.map(key => Number(key)),
      status: 2  // 发布状态为2
    })
    if (response.data && response.data.code === 0) {
    message.success('发布成功')
    fetchTree()
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
  try {
    const response = await axios.post('/apiInfoCategory/update/status/batch', {
      apiInfoCategoryIdList: keys.map(key => Number(key)),
      status: 1  // 下架状态为1
    })
    if (response.data && response.data.code === 0) {
    message.success('下架成功')
    fetchTree()
    } else {
      message.error(response.data.message || '下架失败')
    }
  } catch (error) {
    console.error('下架失败:', error)
    message.error('下架失败，请稍后重试')
  }
}

// 添加一个计算属性来判断是否包含分类节点
const hasCategoryNode = computed(() => {
  const checkNode = (node: any): boolean => {
    if (node.type === 1) return true
    if (node.children && node.children.length > 0) {
      return node.children.some((child: any) => checkNode(child))
    }
    return false
  }
  return checkedKeys.value.some(key => {
    const node = findNodeByKey(treeData.value, key)
    return node && checkNode(node)
  })
})

// 添加一个辅助函数来查找节点
const findNodeByKey = (nodes: any[], key: string): any => {
  for (const node of nodes) {
    if (node.key === key) return node
    if (node.children && node.children.length > 0) {
      const found = findNodeByKey(node.children, key)
      if (found) return found
    }
  }
  return null
}

// 添加一个计算属性来过滤父级节点树
const filteredTreeData = computed(() => {
  if (modalType.value !== 'edit' || !currentNode.value) {
    return treeData.value
  }

  const filterNode = (node: any): any => {
    // 如果是当前节点，返回null（过滤掉）
    if (node.key === currentNode.value?.key) {
      return null
    }

    // 如果有子节点，递归过滤
    if (node.children && node.children.length > 0) {
      const filteredChildren = node.children
        .map((child: any) => filterNode(child))
        .filter(Boolean)
      
      return {
        ...node,
        children: filteredChildren
      }
    }

    return node
  }

  return treeData.value
    .map(node => filterNode(node))
    .filter(Boolean)
})

onMounted(() => {
  fetchTree()
})
</script>

<style scoped>
.api-manage {
  height: 100%;
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

.tree-container {
  background: #fff;
  padding: 24px 16px 16px 16px;
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

.api-detail {
  background: #fff;
  padding: 24px 0 16px 16px;
  border-radius: 2px;
  height: 100%;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.detail-header {
  margin-bottom: 16px;
  flex-shrink: 0;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.detail-content {
  flex: 1;
  overflow: auto;
  min-width: 0;
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

.node-actions {
  opacity: 0;
  transition: opacity 0.2s;
}

.tree-node-content:hover .node-actions {
  opacity: 1;
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
</style> 