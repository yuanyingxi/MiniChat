<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useChatStore } from '@/stores/chat'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { Group } from '@/types'
import * as groupApi from '@/api/group'

const chatStore = useChatStore()

// 群头像缓存: groupId → 前4个成员头像URL
const groupAvatars = ref<Record<string, string[]>>({})

onMounted(async () => {
  for (const g of chatStore.groups) {
    try {
      const members = await groupApi.getGroupMembers(g.groupId)
      groupAvatars.value[String(g.groupId)] = members.slice(0, 4).map(m => m.avatar || '')
    } catch {
      groupAvatars.value[String(g.groupId)] = []
    }
  }
})

function getAvatars(groupId: number | string): string[] {
  return groupAvatars.value[String(groupId)] || ['', '', '', '']
}

function handleClick(group: Group) {
  chatStore.openGroupChat(group)
}

async function handleLeave(e: Event, groupId: number | string, name: string) {
  e.stopPropagation()
  try {
    await ElMessageBox.confirm(`确定退出群聊「${name}」？`, '退出群聊', { type: 'warning' })
    await chatStore.leaveGroup(groupId)
    ElMessage.success('已退出')
  } catch { /* cancelled */ }
}
</script>

<template>
  <div class="group-list">
    <div
      v-for="group in chatStore.groups"
      :key="group.groupId"
      class="group-item"
      @click="handleClick(group)"
    >
      <!-- 默认群头像：成员头像拼贴 -->
      <div class="group-avatar-grid">
        <el-avatar v-for="(url, i) in getAvatars(group.groupId)" :key="i" :size="18" :src="url" />
      </div>
      <div class="group-info">
        <span class="group-name">{{ group.name }}</span>
        <span class="group-members">{{ group.memberCount }} 人</span>
      </div>
      <el-button size="small" type="danger" text @click="handleLeave($event, group.groupId, group.name)">
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
  cursor: pointer;
}
.group-item:hover { background: var(--line-sidebar-item-hover); }

.group-avatar-grid {
  display: grid;
  grid-template-columns: 18px 18px;
  grid-template-rows: 18px 18px;
  gap: 1px;
  width: 37px;
  height: 37px;
  padding: 1px;
  background: #ddd;
  border-radius: 6px;
  flex-shrink: 0;
}
.group-avatar-grid :deep(.el-avatar) {
  border-radius: 3px;
}

.group-info { flex: 1; min-width: 0; }
.group-name {
  display: block;
  font-size: 14px;
  color: var(--line-text-primary);
  font-weight: 500;
}
.group-members {
  display: block;
  font-size: 12px;
  color: var(--line-text-tertiary);
  margin-top: 2px;
}
</style>
