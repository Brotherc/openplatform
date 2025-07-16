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
                <h2>{{ selectedNode.apiDetail.name }}</h2>
                <p>{{ selectedNode.apiDetail.desc }}</p>
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
import { ref, watch, onMounted, onBeforeUnmount, nextTick, computed } from 'vue';
import { useRoute } from 'vue-router';
import axios from 'axios';
import TreeNode from '../components/TreeNode.vue';
import MarkdownIt from 'markdown-it';

const md = new MarkdownIt();

const route = useRoute();
const docCatalogGroupId = computed(() => route.query.docCatalogGroupId);

const treeData = ref<any[]>([]);

const fetchTreeData = async () => {
  if (!docCatalogGroupId.value) return;
  const res = await axios.get(`${import.meta.env.VITE_API_BASE_URL}/docCatalog/getTree/portal`, {
    params: { docCatalogGroupId: docCatalogGroupId.value }
  });
  treeData.value = res.data?.data || [];
};

onMounted(fetchTreeData);
watch(docCatalogGroupId, fetchTreeData);

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

// 新增：获取文章内容方法
const fetchArticleContent = async (id: string) => {
  const res = await axios.get(`${import.meta.env.VITE_API_BASE_URL}/docCatalog/getDocumentById/portal`, {
    params: { id }
  });
  return res.data?.data?.content || '';
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
}
.empty-tip {
  color: #bbb;
  font-size: 16px;
  text-align: center;
  margin-top: 80px;
}
</style>
