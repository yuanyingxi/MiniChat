<script setup lang="ts">
import { useChatStore } from '@/stores/chat'
import { ElMessage, ElMessageBox } from 'element-plus'

const chatStore = useChatStore()

async function handleLeave(groupId: number, name: string) {
  try {
    await ElMessageBox.confirm(`确定退出群聊「${name}」？`, '退出群聊', { type: 'warning' })
    await chatStore.leaveGroup(groupId)
    ElMessage.success('已退出')
  } catch { /* cancelled */ }
}
</script>

<template>
  <div class="group-list">
    <div v-for="group in chatStore.groups" :key="group.groupId" class="group-item">
      <el-avatar :size="36" :src="group.avatar" />
      <div class="group-info">
        <span class="group-name">{{ group.name }}</span>
        <span class="group-members">{{ group.memberCount }} 人</span>
      </div>
      <el-button size="small" type="danger" text @click="handleLeave(group.groupId, group.name)">
        退出
      </el-button>
    </div>
    <el-empty v-if="chatStore.groups.length === 0" description="暂无群组" :image-size="60" />
  </div>
</template>

<style scoped>
.group-list { height: 100%; }
.group-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 16px;
  border-bottom: 1px solid var(--line-border-light);
  transition: background 0.15s;
}
.group-item:hover { background: var(--line-sidebar-item-hover); }
.group-info { flex: 1; min-width: 0; }
.group-name {
  display: block;
  font-size: 14px;
  color: var(--line-text-primary);
}
.group-members {
  display: block;
  font-size: 12px;
  color: var(--line-text-tertiary);
  margin-top: 2px;
}
</style>
