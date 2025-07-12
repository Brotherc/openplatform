<template>
  <div class="tree-node">
    <div 
      class="node-content"
      :class="{ 
        'selected': selectedKey === node.key,
        'level-1': level === 0,
        'level-2': level === 1,
        'level-3': level >= 2
      }"
      @click="handleNodeClick"
    >
      <div class="node-inner" :style="{ paddingLeft: `calc(20px + ${level} * 20px)` }">
        <div class="arrow-wrapper" v-if="hasChildren">
          <div 
            class="arrow"
            :class="{ 'expanded': expanded }"
            @click.stop="toggleExpand"
          >
            <svg width="12" height="12" viewBox="0 0 12 12" fill="none">
              <path d="M4.5 3L7.5 6L4.5 9" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/>
            </svg>
          </div>
        </div>
        <div class="title">{{ node.title }}</div>
      </div>
    </div>
    <div class="children" v-if="hasChildren && expanded">
      <TreeNode 
        v-for="child in node.children" 
        :key="child.key" 
        :node="child" 
        :level="level + 1"
        :selected-key="selectedKey"
        @select="$emit('select', $event)"
      />
    </div>
  </div>
</template>

<script lang="ts" setup>
import { ref, computed } from 'vue';

interface TreeNode {
  key: string;
  title: string;
  children?: TreeNode[];
}

interface Props {
  node: TreeNode;
  level: number;
  selectedKey?: string;
}

const props = defineProps<Props>();
const emit = defineEmits<{
  select: [key: string];
}>();

const expanded = ref(props.level === 0); // 默认展开第一级

const hasChildren = computed(() => {
  return props.node.children && props.node.children.length > 0;
});

const handleNodeClick = () => {
  if (hasChildren.value) {
    expanded.value = !expanded.value;
  }
  emit('select', props.node.key);
};

const toggleExpand = () => {
  expanded.value = !expanded.value;
};
</script>

<style scoped>
.tree-node {
  width: 100%;
  margin: 0;
  padding: 0;
}

.node-content {
  display: flex;
  align-items: center;
  height: 40px;
  width: 100%;
  box-sizing: border-box;
  background: transparent;
  transition: background 0.2s;
  cursor: pointer;
  border-radius: 0;
  margin: 0;
}
.node-content:hover {
  background: #e6f4ff;
}
.node-content.selected .title {
  color: #1677ff;
}
.node-content.selected {
  background: transparent;
}
.node-content.selected:hover {
  background: #e6f4ff;
}

.node-inner {
  display: flex;
  align-items: center;
  width: 100%;
  height: 100%;
  /* padding-left 由内联样式动态控制 */
}

.arrow-wrapper {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 20px;
  height: 20px;
  margin-right: 8px;
}

.arrow {
  display: flex;
  align-items: center;
  justify-content: center;
  color: #666;
  transition: all 0.2s ease;
  transform: rotate(0deg);
}
.arrow.expanded {
  transform: rotate(90deg);
}

.title {
  flex: 1;
  font-size: 15px;
  color: #222;
  transition: color 0.2s ease;
}
.level-1 .title {
  font-weight: bold;
  font-size: 16px;
}
.level-2 .title {
  font-weight: normal;
  font-size: 15px;
  color: #222;
}
.level-3 .title {
  font-weight: normal;
  font-size: 15px;
  color: #666;
}

.children {
  margin-left: 0;
  position: relative;
}
</style> 