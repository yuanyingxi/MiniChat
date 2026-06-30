<script setup lang="ts">
import { ref } from 'vue'
import { useChatStore } from '@/stores/chat'
import { MessageType } from '@/types'
import * as msgApi from '@/api/message'
import { ElMessage } from 'element-plus'

const chatStore = useChatStore()

const inputText = ref('')
const sending = ref(false)
const uploading = ref(false)

function getChatType(): 1 | 2 {
  const conv = chatStore.activeConversation
  return conv?.type === 'group' ? 2 : 1
}

function getTargetId(): number | string {
  return chatStore.activeConversation?.targetId ?? ''
}

// ── 文字 ──

async function handleSend() {
  const text = inputText.value.trim()
  if (!text || sending.value) return
  sending.value = true
  try {
    chatStore.sendMessage(getChatType(), getTargetId(), MessageType.TEXT, { text })
    inputText.value = ''
  } finally {
    sending.value = false
  }
}

// ── 图片 ──

function handleImageUpload() {
  const input = document.createElement('input')
  input.type = 'file'
  input.accept = 'image/*'
  input.onchange = async () => {
    const file = input.files?.[0]
    if (!file) return
    uploading.value = true
    try {
      const res = await msgApi.uploadFile(file)
      chatStore.sendMessage(getChatType(), getTargetId(), MessageType.IMAGE, { url: res.url })
    } catch {
      ElMessage.error('图片上传失败')
    } finally {
      uploading.value = false
    }
  }
  input.click()
}

// ── 文件 ──

function handleFileUpload() {
  const input = document.createElement('input')
  input.type = 'file'
  input.onchange = async () => {
    const file = input.files?.[0]
    if (!file) return
    uploading.value = true
    try {
      const res = await msgApi.uploadFile(file)
      chatStore.sendMessage(getChatType(), getTargetId(), MessageType.IMAGE, {
        fileName: res.originalName || file.name,
        url: res.url,
        size: file.size,
      })
    } catch {
      ElMessage.error('文件上传失败')
    } finally {
      uploading.value = false
    }
  }
  input.click()
}
</script>

<template>
  <div class="message-input">
    <button class="attach-btn" @click="handleImageUpload" title="发送图片" :disabled="uploading">
      <el-icon><Picture /></el-icon>
    </button>
    <el-input
      v-model="inputText"
      placeholder="输入消息..."
      @keyup.enter="handleSend"
      clearable
    />
    <button class="attach-btn" @click="handleFileUpload" title="发送文件" :disabled="uploading">
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
