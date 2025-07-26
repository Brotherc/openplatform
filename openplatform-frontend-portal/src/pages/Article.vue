<template>
  <div class="article-page">
    <div class="content-wrapper">
      <div class="sidebar">
        <div class="sidebar-header">
          <h3>文档目录</h3>
        </div>
        <div class="doc-tree">
          <TreeNode 
            v-for="node in treeData" 
            :key="node.key" 
            :node="node" 
            :level="0"
            :selected-key="selectedKey"
            @select="handleSelect"
          />
        </div>
      </div>
      <div class="main-content-with-toc">
        <div class="main-content" ref="mainContentRef">
          <template v-if="selectedNode">
            <template v-if="selectedNode.type === 2">
              <div v-html="articleHtml"></div>
            </template>
            <template v-else-if="selectedNode.type === 3">
              <div class="api-detail">
                <div v-if="selectedNode.apiDetail" class="api-info">
                  <div class="api-header">
                    <h2>{{ selectedNode.apiDetail.name }}</h2>
                    <span class="api-cn-name">{{ selectedNode.apiDetail.cnName }}</span>
                  </div>
                  <div class="api-description">
                    <p>{{ selectedNode.apiDetail.description }}</p>
                  </div>
                  
                  <div class="api-params">
                    <h3>请求参数</h3>
                    <a-table 
                      v-if="selectedNode.apiDetail.queryParam && selectedNode.apiDetail.queryParam.length"
                      :columns="requestColumns" 
                      :data-source="selectedNode.apiDetail.queryParam" 
                      :pagination="false"
                      size="small"
                      bordered
                    />
                    <div v-else class="no-params">无请求参数</div>
                  </div>
                  
                  <div class="api-response">
                    <h3>响应参数</h3>
                    <a-table 
                      v-if="selectedNode.apiDetail.returnInfoDisplayJson && selectedNode.apiDetail.returnInfoDisplayJson.length"
                      :columns="responseColumns" 
                      :data-source="selectedNode.apiDetail.returnInfoDisplayJson" 
                      :pagination="false"
                      size="small"
                      bordered
                      :row-key="(record) => record.name"
                    />
                    <div v-else class="no-response">无响应参数</div>
                  </div>
                </div>
                <div v-else class="loading">加载中...</div>
              </div>
            </template>
            <template v-else>
              <div class="empty-tip">请选择文章</div>
            </template>
          </template>
          <template v-else>
            <div class="empty-tip">请选择内容</div>
          </template>
        </div>
        <div class="toc-panel" v-if="selectedNode && selectedNode.type === 2 && tocList.length">
          <ul>
            <li 
              v-for="item in tocList" 
              :key="item.id" 
              :class="{ active: activeId === item.id }"
              :style="{ paddingLeft: (item.level - 1) * 12 + 'px' }"
            >
              <a :href="'#' + item.id" @click.prevent="scrollToHeading(item.id)">{{ item.title }}</a>
            </li>
          </ul>
        </div>
      </div>
    </div>
  </div>
</template>

<script lang="ts" setup>
import { ref, watch, onMounted, onBeforeUnmount, nextTick } from 'vue';
import { useRoute } from 'vue-router';
import axios from 'axios';
import TreeNode from '../components/TreeNode.vue';
import MarkdownIt from 'markdown-it';
import { Table } from 'ant-design-vue';

const md = new MarkdownIt();

const route = useRoute();

const treeData = ref<any[]>([]);

// 请求参数表格列配置
const requestColumns = [
  {
    title: '参数名',
    dataIndex: 'name',
    key: 'name',
    width: 200,
  },
  {
    title: '类型',
    dataIndex: 'type',
    key: 'type',
    width: 120,
  },
  {
    title: '是否必填',
    dataIndex: 'required',
    key: 'required',
    width: 100,
    customRender: ({ text }: { text: boolean }) => text ? '是' : '否',
  },
  {
    title: '示例值',
    dataIndex: 'example',
    key: 'example',
    width: 120,
    customRender: ({ text }: { text: any }) => text || '-',
  },
  {
    title: '描述',
    dataIndex: 'description',
    key: 'description',
  },
];

// 响应参数表格列配置
const responseColumns = [
  {
    title: '参数名',
    dataIndex: 'name',
    key: 'name',
    width: 200,
  },
  {
    title: '类型',
    dataIndex: 'type',
    key: 'type',
    width: 120,
  },
  {
    title: '是否必填',
    dataIndex: 'required',
    key: 'required',
    width: 100,
    customRender: ({ text }: { text: boolean }) => text ? '是' : '否',
  },
  {
    title: '示例值',
    dataIndex: 'example',
    key: 'example',
    width: 120,
    customRender: ({ text }: { text: any }) => text || '-',
  },
  {
    title: '描述',
    dataIndex: 'description',
    key: 'description',
  },
];

const fetchTreeData = async () => {
  const groupId = route.query.docCatalogGroupId;
  if (!groupId) return;
  const res = await axios.get(`${import.meta.env.VITE_API_BASE_URL}/docCatalog/getTree/portal`, {
    params: { docCatalogGroupId: groupId }
  });
  treeData.value = res.data?.data || [];
  // 新增：自动选中第一个 type 非1 节点
  const firstNode = findFirstNonType1Node(treeData.value);
  if (firstNode) {
    handleSelect(firstNode.key);
  } else {
    selectedKey.value = '';
    selectedNode.value = null;
  }
};

onMounted(fetchTreeData);
watch(
  () => route.query.docCatalogGroupId,
  () => {
    fetchTreeData();
  }
);

const selectedKey = ref('');
const selectedNode = ref<any>(null);

const findNode = (nodes: any[], key: string): any => {
  for (const node of nodes) {
    if (node.key === key) return node;
    if (node.children) {
      const found = findNode(node.children, key);
      if (found) return found;
    }
  }
  return null;
};

const handleSelect = (key: string) => {
  selectedKey.value = key;
  selectedNode.value = findNode(treeData.value, key);
};

const articleHtml = ref('');
const tocList = ref<{ id: string; title: string; level: number }[]>([]);
const activeId = ref('');
const mainContentRef = ref<HTMLElement | null>(null);

// 新增：查找第一个 type 非1 节点
function findFirstNonType1Node(nodes: any[]): any | null {
  for (const node of nodes) {
    if (node.type !== 1) return node;
    if (node.children && node.children.length) {
      const found = findFirstNonType1Node(node.children);
      if (found) return found;
    }
  }
  return null;
}

// 新增：获取文章内容方法
const fetchArticleContent = async (id: string) => {
  const res = await axios.get(`${import.meta.env.VITE_API_BASE_URL}/docCatalog/getDocumentById/portal`, {
    params: { id }
  });
  return res.data?.data?.content || '';
};

// 新增：获取API详情方法
const fetchApiInfo = async (docCatalogId: string) => {
  const res = await axios.get(`${import.meta.env.VITE_API_BASE_URL}/docCatalog/getApiInfoById/portal`, {
    params: { docCatalogId }
  });
  return res.data?.data || null;
};

function generateId(text: string) {
  return text.trim().toLowerCase().replace(/[^\w]+/g, '-');
}

function generateTOC(markdown: string) {
  const lines = markdown.split('\n');
  const toc: { id: string; title: string; level: number }[] = [];
  let htmlLines: string[] = [];

  lines.forEach(line => {
    const match = /^(#{1,6})\s+(.*)/.exec(line);
    if (match) {
      const level = match[1].length;
      const title = match[2];
      const id = generateId(title);
      toc.push({ id, title, level });
      htmlLines.push(`<h${level} id="${id}">${title}</h${level}>`);
    } else {
      htmlLines.push(md.render(line));
    }
  });

  return { html: htmlLines.join('\n'), toc };
}

watch(
  () => selectedNode.value,
  async (node) => {
    if (node && node.type === 2) {
      // 修改：接口获取内容
      const content = await fetchArticleContent(node.key);
      const { html, toc } = generateTOC(content);
      articleHtml.value = html;
      tocList.value = toc;
      await nextTick();
      updateActiveToc();
    } else if (node && node.type === 3) {
      // 新增：获取API详情
      const apiInfo = await fetchApiInfo(node.key);
      node.apiDetail = apiInfo;
    }
  },
  { immediate: true }
);

function updateActiveToc() {
  const headings = Array.from(document.querySelectorAll('.main-content h1, .main-content h2, .main-content h3'));
  const scrollTop = window.scrollY || document.documentElement.scrollTop;
  for (let i = headings.length - 1; i >= 0; i--) {
    const heading = headings[i];
    if (heading.offsetTop - 80 <= scrollTop) {
      activeId.value = heading.id;
      break;
    }
  }
}

function scrollToHeading(id: string) {
  const heading = document.getElementById(id);
  if (heading) {
    window.scrollTo({
      top: heading.offsetTop - 64,
      behavior: 'smooth'
    });
    activeId.value = id;
  }
}

onMounted(() => {
  window.addEventListener('scroll', updateActiveToc);
  nextTick(() => updateActiveToc());
});

onBeforeUnmount(() => {
  window.removeEventListener('scroll', updateActiveToc);
});
</script>

<style scoped>
.article-page {
  min-height: 100vh;
  background: #f5f5f5;
}
.content-wrapper {
  display: flex;
  margin-top: 64px;
  min-height: 100vh;
}
.sidebar {
  width: 280px;
  background: #f7f8fa;
  overflow-y: auto;
  position: fixed;
  left: 0;
  top: 64px;
  bottom: 0;
  z-index: 100;
}
.sidebar-header {
  padding: 20px;
  border-bottom: 1px solid #e8e8e8;
  background: #f7f8fa;
}
.sidebar-header h3 {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
  color: #222;
}
.doc-tree {
  padding: 10px 0 0;
}
.main-content-with-toc {
  display: flex;
  flex: 1;
  margin-left: 280px;
  background: #fff;
  padding: 32px;
}
.main-content {
  flex: 1;
  min-width: 0;
}
.toc-panel {
  width: 240px;
  margin-left: 32px;
  border-left: 2px solid #e5e6eb;
  padding: 0;
  position: sticky;
  top: 80px;
  align-self: flex-start;
  max-height: 80vh;
  overflow-y: auto;
  font-size: 15px;
}
.toc-panel ul {
  padding-left: 18px;
  list-style: none;
}
.toc-panel li {
  padding-top: 6px;
  padding-bottom: 6px;
}
.toc-panel li.active a {
  color: #1677ff;
}
.toc-panel li a {
  text-decoration: none;
  color: #222;
}
.toc-panel li a:hover {
  color: #1677ff;
}
.api-detail h2 {
  margin-top: 0;
  margin-bottom: 20px;
  color: #222;
  font-size: 24px;
}
.api-info h2 {
  margin-top: 0;
  margin-bottom: 20px;
  color: #222;
  font-size: 24px;
}
.api-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 8px;
}
.api-header h2 {
  margin: 0;
  margin-bottom: 0;
  font-size: 32px;
  font-weight: 700;
}
.api-cn-name {
  color: #666;
  font-size: 16px;
  margin-top: 4px;
}
.api-description {
  margin-bottom: 24px;
}
.api-description p {
  margin: 0;
  color: #666;
  line-height: 1.6;
}
.api-params, .api-response {
  margin-bottom: 32px;
  margin-top: 32px;
}
.api-params h3, .api-response h3 {
  margin-bottom: 16px;
  color: #222;
  font-size: 18px;
}
.no-params, .no-response {
  color: #999;
  font-style: italic;
  padding: 16px;
  text-align: center;
}
.loading {
  text-align: center;
  color: #666;
  padding: 40px;
}
.empty-tip {
  color: #bbb;
  font-size: 16px;
  text-align: center;
  margin-top: 80px;
}
</style>
