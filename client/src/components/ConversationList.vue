<script setup lang="ts">
import { useChatStore } from '@/stores/chat'
import type { Conversation } from '@/types'

const chatStore = useChatStore()

function formatTime(time: string) {
  if (!time) return ''
  const d = new Date(time)
  if (isNaN(d.getTime())) return ''
  const now = new Date()
  const diff = now.getTime() - d.getTime()
  if (diff < 86400000 && d.getDate() === now.getDate()) {
    return d.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
  }
  if (diff < 172800000) return '昨天'
  return `${d.getMonth() + 1}/${d.getDate()}`
}

function convKey(conv: Conversation) {
  return conv.type + '-' + conv.targetId
}

function selectConv(conv: Conversation) {
  chatStore.selectConversation(conv)
}

function isActive(conv: Conversation) {
  const active = chatStore.activeConversation
  if (!active) return false
  return active.type === conv.type && String(active.targetId) === String(conv.targetId)
}
</script>

<template>
  <div class="conversation-list">
    <div
      v-for="conv in chatStore.conversations"
      :key="convKey(conv)"
      class="conv-item"
      :class="{ active: isActive(conv) }"
      @click="selectConv(conv)"
    >
      <el-avatar :size="40" :src="conv.avatar" />
      <div class="conv-info">
        <div class="conv-top">
          <span class="conv-name">{{ conv.name }}</span>
          <span class="conv-time">{{ formatTime(conv.lastTime) }}</span>
        </div>
        <div class="conv-bottom">
          <span class="conv-msg">{{ conv.lastMessage }}</span>
          <el-badge v-if="conv.unreadCount > 0" :value="conv.unreadCount" :max="99" />
        </div>
      </div>
    </div>
    <el-empty v-if="chatStore.conversations.length === 0" description="暂无会话" :image-size="60" />
  </div>
</template>

<style scoped>
.conversation-list {
  height: 100%;
}
.conv-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 14px 20px;
  cursor: pointer;
  transition: background 0.15s;
  border-bottom: 1px solid var(--line-border-light);
}
.conv-item:hover {
  background: var(--line-sidebar-item-hover);
}
.conv-item.active {
  background: var(--line-sidebar-item-active);
}
.conv-item :deep(.el-badge__content) {
  background-color: var(--line-badge-bg);
  border: none;
}
.conv-info {
  flex: 1;
  min-width: 0;
}
.conv-top {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.conv-name {
  font-size: 14px;
  font-weight: 700;
  color: var(--line-text-primary);
}
.conv-time {
  font-size: 11px;
  color: var(--line-text-tertiary);
  flex-shrink: 0;
}
.conv-bottom {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 4px;
}
.conv-msg {
  font-size: 12px;
  color: var(--line-text-secondary);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  flex: 1;
  margin-right: 8px;
}
</style>
