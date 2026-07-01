<script setup lang="ts">
import { useChatStore } from '@/stores/chat'
import type { FriendRequestVO } from '@/types'

const chatStore = useChatStore()

function handleAccept(req: FriendRequestVO) {
  chatStore.acceptFriendRequest(req.id)
}

function handleReject(req: FriendRequestVO) {
  chatStore.rejectFriendRequest(req.id)
}
</script>

<template>
  <div v-if="chatStore.friendRequests.length > 0" class="friend-requests-panel">
    <div class="requests-header">
      <span>新的好友请求</span>
      <span class="requests-count">{{ chatStore.friendRequests.length }}</span>
    </div>
    <div
      v-for="req in chatStore.friendRequests"
      :key="req.id"
      class="request-item"
    >
      <el-avatar :size="40" :src="req.fromUserAvatar" />
      <div class="request-info">
        <span class="sender-name">{{ req.fromUserNickname }}</span>
        <span class="request-remark">{{ req.remark || '请求添加你为好友' }}</span>
      </div>
      <div class="request-actions">
        <el-button size="small" type="primary" round @click.stop="handleAccept(req)">
          同意
        </el-button>
        <el-button size="small" plain round @click.stop="handleReject(req)">
          拒绝
        </el-button>
      </div>
    </div>
  </div>
</template>

<style scoped>
.friend-requests-panel {
  border-bottom: 1px solid var(--line-border);
}

.requests-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 10px 16px;
  font-size: 13px;
  font-weight: 600;
  color: var(--line-text-secondary);
  background: var(--line-bg-secondary);
}

.requests-count {
  background: var(--line-badge-bg);
  color: #fff;
  font-size: 11px;
  min-width: 18px;
  height: 18px;
  line-height: 18px;
  text-align: center;
  border-radius: 9px;
  padding: 0 5px;
}

.request-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 16px;
  border-bottom: 1px solid var(--line-border-light);
  transition: background 0.15s;
}

.request-item:hover {
  background: var(--line-sidebar-item-hover);
}

.request-info {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.sender-name {
  font-size: 14px;
  color: var(--line-text-primary);
  font-weight: 500;
}

.request-remark {
  font-size: 12px;
  color: var(--line-text-tertiary);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.request-actions {
  display: flex;
  gap: 6px;
  flex-shrink: 0;
}

.request-actions :deep(.el-button--primary) {
  background-color: var(--line-green);
  border-color: var(--line-green);
}

.request-actions :deep(.el-button--primary:hover) {
  background-color: var(--line-green-hover);
  border-color: var(--line-green-hover);
}
</style>
