<script setup lang="ts">
import { ref, watch, nextTick } from 'vue'
import { useUserStore } from '@/stores/user'
import { useChatStore } from '@/stores/chat'
import { MessageType, type Message, type TextContent, type ImageContent, type FileContent } from '@/types'
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
  if (!time) return ''
  const d = new Date(time)
  if (isNaN(d.getTime())) return ''
  return d.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
}

function isMine(senderId: number | string | undefined) {
  const myId = localStorage.getItem('minichat_user_id')
  const s = String(senderId)
  return (myId !== null && s === myId)
}

function getText(msg: Message): string {
  const c = msg.content
  if (typeof c === 'object' && 'text' in c) return (c as TextContent).text
  return ''
}

function getImageUrl(msg: Message): string {
  const c = msg.content
  if (typeof c === 'object' && 'url' in c) return (c as ImageContent).url
  return ''
}

function isFileAttachment(msg: Message): boolean {
  const c = msg.content
  if (typeof c !== 'object') return false
  // 有 fileName 字段就是文件
  if ('fileName' in c) return true
  // 有 url 但不是图片后缀 → 文件
  if ('url' in c) {
    const u = (c as ImageContent).url
    if (u && !/\.(jpg|jpeg|png|gif|webp|svg|bmp)(\?|#|$)/i.test(u)) return true
  }
  return false
}

function getFileNameFromUrl(msg: Message): string {
  if (isFileAttachment(msg)) {
    const c = msg.content
    if (typeof c === 'object' && 'fileName' in c) return (c as FileContent).fileName
    if (typeof c === 'object' && 'url' in c) {
      const u = (c as ImageContent).url
      const name = u.split('/').pop()?.split('?')[0]
      return name || '文件'
    }
  }
  return '文件'
}

function getFileName(msg: Message): string {
  const c = msg.content
  if (typeof c === 'object' && 'fileName' in c) return (c as FileContent).fileName
  return '未知文件'
}

function getFileUrl(msg: Message): string {
  const c = msg.content
  if (typeof c === 'object' && 'url' in c) return (c as FileContent).url ?? ''
  return ''
}

function openFile(msg: Message) {
  window.open(getFileUrl(msg), '_blank')
}

function getAvatar(senderId: number | string): string {
  const myId = localStorage.getItem('minichat_user_id')
  if (String(senderId) === myId) {
    return userStore.currentUser?.avatar || ''
  }
  const friend = chatStore.friends.find(f => String(f.friendId) === String(senderId))
  return friend?.avatar || ''
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
          :class="{ mine: isMine(msg.fromId) }"
        >
          <div v-if="!isMine(msg.fromId)" class="msg-body">
            <div class="msg-sender-avatar">
              <el-avatar :size="32" :src="getAvatar(msg.fromId)" />
            </div>
            <div class="msg-bubble-wrapper">
            <!-- 图片无气泡，可点击预览 -->
            <!-- 文件附件（可点击下载） -->
            <template v-if="isFileAttachment(msg)">
              <div class="file-msg-bubble" @click="openFile(msg)">
                <el-icon :size="28"><Document /></el-icon>
                <span class="file-name">{{ getFileNameFromUrl(msg) }}</span>
              </div>
            </template>
            <!-- 图片无气泡，可点击预览 -->
            <template v-else-if="msg.messageType === MessageType.IMAGE">
              <el-image
                :src="getImageUrl(msg)"
                :preview-src-list="[getImageUrl(msg)]"
                fit="cover"
                class="msg-image"
              />
            </template>
            <template v-else>
            <div class="msg-bubble">
              <template v-if="msg.messageType === MessageType.TEXT">{{ getText(msg) }}</template>
              <div v-else-if="msg.messageType === MessageType.VIDEO" class="media-msg">
                <el-icon><VideoCamera /></el-icon>
                <span>视频消息</span>
              </div>
            </div>
            </template>
            <div class="msg-time">{{ formatTime(msg.createTime) }}</div>
            </div>
          </div>
          <div v-else class="msg-body mine-body">
            <div class="msg-bubble-wrapper">
            <!-- 文件附件（可点击下载） -->
            <template v-if="isFileAttachment(msg)">
              <div class="file-msg-bubble" @click="openFile(msg)">
                <el-icon :size="28"><Document /></el-icon>
                <span class="file-name">{{ getFileNameFromUrl(msg) }}</span>
              </div>
            </template>
            <!-- 图片无气泡，可点击预览 -->
            <template v-else-if="msg.messageType === MessageType.IMAGE">
              <el-image
                :src="getImageUrl(msg)"
                :preview-src-list="[getImageUrl(msg)]"
                fit="cover"
                class="msg-image"
              />
            </template>
            <template v-else>
            <div class="msg-bubble">
              <template v-if="msg.messageType === MessageType.TEXT">{{ getText(msg) }}</template>
              <div v-else class="file-msg">
                <a :href="getFileUrl(msg)" target="_blank">
                  <el-icon><Document /></el-icon>
                  <span>{{ getFileName(msg) }}</span>
                </a>
              </div>
            </div>
            </template>
            <div class="msg-time">{{ formatTime(msg.createTime) }}</div>
            </div>
            <div class="msg-sender-avatar">
              <el-avatar :size="32" :src="getAvatar(msg.fromId)" />
            </div>
          </div>
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
import { Document, ChatDotRound, VideoCamera } from '@element-plus/icons-vue'
export default { components: { Document, ChatDotRound, VideoCamera } }
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

.msg-body {
  display: flex;
  gap: 8px;
  max-width: 70%;
}
.msg-body.mine-body {
  justify-content: flex-end;
}

.msg-sender-avatar {
  flex-shrink: 0;
}

.msg-bubble-wrapper {
  display: flex;
  flex-direction: column;
}

.msg-row {
  margin-bottom: 16px;
}

.msg-row.mine {
  display: flex;
  justify-content: flex-end;
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

.file-msg-bubble {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 12px 16px;
  background: var(--line-bubble-received-bg);
  border-radius: var(--line-radius-bubble);
  cursor: pointer;
  transition: background 0.15s;
}
.file-msg-bubble:hover {
  filter: brightness(0.95);
}
.file-name {
  font-size: 13px;
  color: var(--line-text-primary);
  word-break: break-all;
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
