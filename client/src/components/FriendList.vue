<script setup lang="ts">
import { useChatStore } from '@/stores/chat'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { Friend } from '@/types'

const chatStore = useChatStore()

function handleClick(friend: Friend) {
  chatStore.openFriendChat(friend)
}

async function handleDelete(e: Event, friendId: number | string, name: string) {
  e.stopPropagation()
  try {
    await ElMessageBox.confirm(`确定删除好友「${name}」？`, '删除好友', { type: 'warning' })
    await chatStore.deleteFriend(friendId)
    ElMessage.success('已删除')
  } catch { /* cancelled */ }
}

async function handleBlock(e: Event, friendId: number | string, name: string) {
  e.stopPropagation()
  await chatStore.blockFriend(friendId)
  ElMessage.success(`已处理「${name}」`)
}
</script>

<template>
  <div class="friend-list">
    <div
      v-for="friend in chatStore.friends"
      :key="friend.friendId"
      class="friend-item"
      @click="handleClick(friend)"
    >
      <el-avatar :size="36" :src="friend.avatar" />
      <div class="friend-info">
        <span class="friend-name">{{ friend.remark || friend.nickname }}</span>
      </div>
      <el-dropdown trigger="click" @click.stop>
        <el-button size="small" :icon="MoreFilled" circle />
        <template #dropdown>
          <el-dropdown-menu>
            <el-dropdown-item @click="handleBlock($event, friend.friendId, friend.nickname)">
              拉黑/取消拉黑
            </el-dropdown-item>
            <el-dropdown-item @click="handleDelete($event, friend.friendId, friend.nickname)" divided>
              <span style="color: #f56c6c">删除好友</span>
            </el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>
    </div>
    <el-empty v-if="chatStore.friends.length === 0" description="暂无好友" :image-size="60" />
  </div>
</template>

<script lang="ts">
import { MoreFilled } from '@element-plus/icons-vue'
export default { components: { MoreFilled } }
</script>

<style scoped>
.friend-list { height: 100%; }
.friend-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 16px;
  border-bottom: 1px solid var(--line-border-light);
  transition: background 0.15s;
  cursor: pointer;
}
.friend-item:hover { background: var(--line-sidebar-item-hover); }
.friend-info { flex: 1; min-width: 0; }
.friend-name {
  font-size: 14px;
  color: var(--line-text-primary);
  font-weight: 500;
}
</style>
