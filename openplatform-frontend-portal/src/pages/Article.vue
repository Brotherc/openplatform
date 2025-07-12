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
      <div class="main-content"></div>
    </div>
  </div>
</template>

<script lang="ts" setup>
import { ref } from 'vue';
import TreeNode from '../components/TreeNode.vue';

const treeData = [
  {
    key: '1',
    title: '移动端插件',
    children: [
      {
        key: '1-1',
        title: '入驻指南',
        children: [
          { key: '1-1-1', title: '入驻流程' },
          { key: '1-1-2', title: '视觉交互规范' }
        ]
      },
      {
        key: '1-2',
        title: '开发文档',
        children: [
          { key: '1-2-1', title: '授权说明' },
          { key: '1-2-2', title: 'SDK使用指南' }
        ]
      }
    ]
  }
];

const selectedKey = ref('');

const handleSelect = (key: string) => {
  selectedKey.value = key;
  console.log('选中:', key);
};
</script>

<style scoped>
.article-page {
  min-height: 100vh;
  background: #f5f5f5;
}
.content-wrapper {
  display: flex;
  margin-top: 64px;
  min-height: calc(100vh - 64px);
}
.sidebar {
  width: 280px;
  background: #f7f8fa;
  border-right: none;
  overflow-y: auto;
  position: fixed;
  left: 0;
  top: 64px;
  bottom: 0;
  z-index: 100;
}
.sidebar-header {
  padding: 20px 20px 10px 20px;
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
  padding: 10px 0 0 0;
  background: transparent;
}
.main-content {
  flex: 1;
  margin-left: 280px;
  background: #fff;
  min-height: calc(100vh - 64px);
}
</style> 