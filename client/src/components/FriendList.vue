<script setup lang="ts">
import { useChatStore } from '@/stores/chat'
import { ElMessage, ElMessageBox } from 'element-plus'

const chatStore = useChatStore()

async function handleDelete(friendId: number, name: string) {
  try {
    await ElMessageBox.confirm(`确定删除好友「${name}」？`, '删除好友', { type: 'warning' })
    await chatStore.deleteFriend(friendId)
    ElMessage.success('已删除')
  } catch { /* cancelled */ }
}

async function handleBlock(friendId: number, blocked: boolean, name: string) {
  await chatStore.blockFriend(friendId)
  ElMessage.success(!blocked ? `已拉黑「${name}」` : `已取消拉黑「${name}」`)
}
</script>

<template>
  <div class="friend-list">
    <div v-for="friend in chatStore.friends" :key="friend.friendId" class="friend-item">
      <el-avatar :size="36" :src="friend.avatar" />
      <div class="friend-info">
        <span class="friend-name">
          {{ friend.remark || friend.nickname }}
          <el-tag v-if="friend.blocked" type="danger" size="small">已拉黑</el-tag>
        </span>
        <span class="friend-sig">{{ friend.remark }}</span>
      </div>
      <el-dropdown trigger="click">
        <el-button size="small" :icon="MoreFilled" circle />
        <template #dropdown>
          <el-dropdown-menu>
            <el-dropdown-item @click="handleBlock(friend.friendId, !!friend.blocked, friend.nickname)">
              {{ friend.blocked ? '取消拉黑' : '拉黑' }}
            </el-dropdown-item>
            <el-dropdown-item @click="handleDelete(friend.friendId, friend.nickname)" divided>
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
}
.friend-item:hover { background: var(--line-sidebar-item-hover); }
.friend-info { flex: 1; min-width: 0; }
.friend-name {
  font-size: 14px;
  color: var(--line-text-primary);
  display: flex;
  align-items: center;
  gap: 6px;
}
.friend-sig {
  display: block;
  font-size: 12px;
  color: var(--line-text-tertiary);
  margin-top: 2px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
</style>
