<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { Search, Plus, SwitchButton } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import { useChatStore } from '@/stores/chat'
import ConversationList from '@/components/ConversationList.vue'
import FriendList from '@/components/FriendList.vue'
import FriendRequestsPanel from '@/components/FriendRequestsPanel.vue'
import GroupList from '@/components/GroupList.vue'
import ChatWindow from '@/components/ChatWindow.vue'
import ProfileDialog from '@/components/ProfileDialog.vue'
import AddFriendDialog from '@/components/AddFriendDialog.vue'
import CreateGroupDialog from '@/components/CreateGroupDialog.vue'

const router = useRouter()
const userStore = useUserStore()
const chatStore = useChatStore()

const activeTab = ref('conversations')
const showProfile = ref(false)
const showAddFriend = ref(false)
const showCreateGroup = ref(false)

onMounted(async () => {
  await Promise.all([
    chatStore.loadConversations(),
    chatStore.loadFriends(),
    chatStore.loadGroups(),
    chatStore.loadFriendRequests(),
  ])
})

function handleLogout() {
  userStore.logout()
  router.push('/login')
}
</script>

<template>
  <div class="chat-layout">
    <div class="sidebar">
      <div class="sidebar-header">
        <div class="user-info" @click="showProfile = true">
          <el-avatar :size="36" :src="userStore.currentUser?.avatar" />
          <span class="nickname">{{ userStore.currentUser?.nickname }}</span>
        </div>
        <el-button :icon="SwitchButton" circle size="small" @click="handleLogout" title="退出登录" />
      </div>

      <el-tabs v-model="activeTab" class="sidebar-tabs">
        <el-tab-pane label="会话" name="conversations" />
        <el-tab-pane name="friends">
          <template #label>
            <el-badge :value="chatStore.friendRequests.length" :max="99" :hidden="chatStore.friendRequests.length === 0">
              <span style="padding: 0 4px">好友</span>
            </el-badge>
          </template>
        </el-tab-pane>
        <el-tab-pane label="群组" name="groups" />
      </el-tabs>

      <div class="sidebar-actions">
        <el-button size="small" :icon="Search" @click="showAddFriend = true">添加好友</el-button>
        <el-button size="small" :icon="Plus" @click="showCreateGroup = true">创建群聊</el-button>
      </div>

      <div class="sidebar-content">
        <ConversationList v-if="activeTab === 'conversations'" />
        <template v-else-if="activeTab === 'friends'">
          <FriendRequestsPanel />
          <FriendList />
        </template>
        <GroupList v-else-if="activeTab === 'groups'" />
      </div>
    </div>

    <div class="main-area">
      <ChatWindow />
    </div>

    <ProfileDialog v-model="showProfile" />
    <AddFriendDialog v-model="showAddFriend" />
    <CreateGroupDialog v-model="showCreateGroup" />
  </div>
</template>

<style scoped>
.chat-layout {
  display: flex;
  height: 100vh;
  background: var(--line-bg-chat);
}

.sidebar {
  width: var(--line-sidebar-width);
  background: var(--line-bg-primary);
  border-right: 1px solid var(--line-border);
  display: flex;
  flex-direction: column;
}

.sidebar-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 16px;
  border-bottom: 1px solid var(--line-border-light);
}

.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  border-radius: var(--line-radius-sm);
  padding: 4px 8px;
  transition: background 0.2s;
}
.user-info:hover {
  background: var(--line-sidebar-item-hover);
}

.nickname {
  font-size: 14px;
  font-weight: 500;
  color: var(--line-text-primary);
}

.sidebar-tabs {
  padding: 0 12px;
}
.sidebar-tabs :deep(.el-tabs__header) {
  margin-bottom: 0;
}
.sidebar-tabs :deep(.el-tabs__nav) {
  width: 100%;
}
.sidebar-tabs :deep(.el-tabs__item) {
  width: 33.33%;
  text-align: center;
  font-size: 13px;
}
.sidebar-tabs :deep(.el-tabs__active-bar) {
  background-color: var(--line-green);
}
.sidebar-tabs :deep(.el-tabs__item.is-active) {
  color: var(--line-green);
}
.sidebar-tabs :deep(.el-tabs__item:hover) {
  color: var(--line-green-hover);
}
.sidebar-tabs :deep(.el-badge__content) {
  background-color: var(--line-badge-bg);
  border: none;
}

.sidebar-actions {
  display: flex;
  gap: 8px;
  padding: 8px 12px;
  border-bottom: 1px solid var(--line-border-light);
}
.sidebar-actions :deep(.el-button) {
  border-radius: var(--line-radius-pill);
  font-size: 12px;
}

.sidebar-content {
  flex: 1;
  overflow-y: auto;
}

.main-area {
  flex: 1;
  min-width: 0;
}
</style>
