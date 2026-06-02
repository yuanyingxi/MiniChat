<script setup lang="ts">
import { ref, watch, nextTick } from 'vue'
import { useUserStore } from '@/stores/user'
import { useChatStore } from '@/stores/chat'
import MessageInput from './MessageInput.vue'

const userStore = useUserStore()
const chatStore = useChatStore()

const messagesRef = ref<HTMLElement>()

watch(
  () => chatStore.messages.length,
  async () => {
    await nextTick()
    if (messagesRef.value) {
      messagesRef.value.scrollTop = messagesRef.value.scrollHeight
    }
  },
)

function formatTime(time: string) {
  return new Date(time).toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
}
</script>

<template>
  <div class="chat-window">
    <template v-if="chatStore.activeConversation">
      <div class="chat-header">
        <span>{{ chatStore.activeConversation.name }}</span>
      </div>
      <div ref="messagesRef" class="messages-area" v-loading="chatStore.loading">
        <div
          v-for="msg in chatStore.messages"
          :key="msg.id"
          class="msg-row"
          :class="{ mine: msg.senderId === userStore.currentUser?.id }"
        >
          <el-avatar v-if="msg.senderId !== userStore.currentUser?.id" :size="32" :src="msg.senderAvatar" />
          <div class="msg-body">
            <div v-if="msg.senderId !== userStore.currentUser?.id" class="msg-sender">
              {{ msg.senderName }}
            </div>
            <div class="msg-bubble">
              <template v-if="msg.type === 'text'">{{ msg.content }}</template>
              <el-image
                v-else-if="msg.type === 'image'"
                :src="msg.content"
                fit="cover"
                class="msg-image"
              />
              <div v-else-if="msg.type === 'file'" class="file-msg">
                <el-icon><Document /></el-icon>
                <span>{{ msg.fileName || msg.content }}</span>
              </div>
            </div>
            <div class="msg-time">{{ formatTime(msg.createdAt) }}</div>
          </div>
          <el-avatar v-if="msg.senderId === userStore.currentUser?.id" :size="32" :src="msg.senderAvatar" />
        </div>
      </div>
      <MessageInput />
    </template>
    <template v-else>
      <div class="welcome">
        <el-icon :size="64" color="#dcdfe6"><ChatDotRound /></el-icon>
        <h3>MiniChat</h3>
        <p>选择一个会话开始聊天</p>
      </div>
    </template>
  </div>
</template>

<script lang="ts">
import { Document, ChatDotRound } from '@element-plus/icons-vue'
export default { components: { Document, ChatDotRound } }
</script>

<style scoped>
.chat-window {
  display: flex;
  flex-direction: column;
  height: 100%;
  background: var(--line-bg-chat);
}

.chat-header {
  height: 50px;
  display: flex;
  align-items: center;
  padding: 0 20px;
  background: var(--line-bg-primary);
  border-bottom: 1px solid var(--line-border);
  font-size: 15px;
  font-weight: 500;
  color: var(--line-text-primary);
}

.messages-area {
  flex: 1;
  overflow-y: auto;
  padding: 20px 24px;
}

.msg-row {
  display: flex;
  gap: 8px;
  margin-bottom: 16px;
}
.msg-row.mine {
  flex-direction: row-reverse;
}

.msg-body {
  max-width: 60%;
}

.msg-sender {
  font-size: 12px;
  color: var(--line-text-secondary);
  margin-bottom: 4px;
}

.msg-bubble {
  padding: 8px 14px;
  border-radius: var(--line-radius-bubble);
  border-top-left-radius: 4px;
  font-size: 14px;
  line-height: 1.5;
  word-break: break-word;
  background: var(--line-bubble-received-bg);
  color: var(--line-bubble-received-text);
  box-shadow: var(--line-shadow-sm);
}
.mine .msg-bubble {
  background: var(--line-bubble-sent-bg);
  color: var(--line-bubble-sent-text);
  border-top-left-radius: var(--line-radius-bubble);
  border-top-right-radius: 4px;
}

.msg-image {
  max-width: 200px;
  border-radius: var(--line-radius-md);
}

.msg-time {
  font-size: 11px;
  color: var(--line-text-tertiary);
  margin-top: 4px;
}
.mine .msg-time {
  text-align: right;
}

.file-msg {
  display: flex;
  align-items: center;
  gap: 6px;
  cursor: pointer;
}

.welcome {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: var(--line-text-tertiary);
}
.welcome h3 {
  margin: 16px 0 4px;
  color: var(--line-text-secondary);
  font-size: 20px;
}
.welcome p {
  font-size: 14px;
}
</style>
