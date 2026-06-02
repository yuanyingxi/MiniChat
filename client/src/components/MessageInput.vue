<script setup lang="ts">
import { ref } from 'vue'
import { useUserStore } from '@/stores/user'
import { useChatStore } from '@/stores/chat'

const userStore = useUserStore()
const chatStore = useChatStore()

const inputText = ref('')

async function handleSend() {
  const text = inputText.value.trim()
  if (!text || !userStore.currentUser) return
  await chatStore.sendMessage(
    userStore.currentUser.id,
    userStore.currentUser.nickname,
    userStore.currentUser.avatar,
    'text',
    text,
  )
  inputText.value = ''
}

function handleImageUpload() {
  if (!userStore.currentUser) return
  chatStore.sendMessage(
    userStore.currentUser.id,
    userStore.currentUser.nickname,
    userStore.currentUser.avatar,
    'image',
    'https://picsum.photos/200/150?random=' + Date.now(),
  )
}

function handleFileUpload() {
  if (!userStore.currentUser) return
  chatStore.sendMessage(
    userStore.currentUser.id,
    userStore.currentUser.nickname,
    userStore.currentUser.avatar,
    'file',
    'example-document.pdf',
    '示例文档.pdf',
  )
}
</script>

<template>
  <div class="message-input">
    <button class="attach-btn" @click="handleImageUpload" title="发送图片">
      <el-icon><Picture /></el-icon>
    </button>
    <el-input
      v-model="inputText"
      placeholder="输入消息..."
      @keyup.enter="handleSend"
      clearable
    />
    <button class="attach-btn" @click="handleFileUpload" title="发送文件">
      <el-icon><FolderOpened /></el-icon>
    </button>
    <el-button type="primary" @click="handleSend" :disabled="!inputText.trim()" round>
      发送
    </el-button>
  </div>
</template>

<script lang="ts">
import { Picture, FolderOpened } from '@element-plus/icons-vue'
export default { components: { Picture, FolderOpened } }
</script>

<style scoped>
.message-input {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 16px;
  background: var(--line-bg-primary);
  border-top: 1px solid var(--line-border);
}
.message-input :deep(.el-input) {
  flex: 1;
}
.message-input :deep(.el-input__wrapper) {
  border-radius: var(--line-input-radius);
  padding: 4px 16px;
}
.attach-btn {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  border: none;
  background: var(--line-bg-secondary);
  color: var(--line-text-secondary);
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  flex-shrink: 0;
  transition: background 0.15s;
}
.attach-btn:hover {
  background: var(--line-border);
}
</style>
