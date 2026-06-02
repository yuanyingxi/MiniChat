<script setup lang="ts">
import { ref } from 'vue'
import { searchUsers } from '@/api/user'
import { useChatStore } from '@/stores/chat'
import type { User } from '@/types'
import { ElMessage } from 'element-plus'

const props = defineProps<{ modelValue: boolean }>()
const emit = defineEmits<{ 'update:modelValue': [val: boolean] }>()

const chatStore = useChatStore()
const keyword = ref('')
const results = ref<User[]>([])
const searching = ref(false)

async function handleSearch() {
  if (!keyword.value.trim()) return
  searching.value = true
  try {
    results.value = await searchUsers(keyword.value.trim())
  } finally {
    searching.value = false
  }
}

async function handleAdd(user: User) {
  try {
    await chatStore.addFriend(user.id)
    ElMessage.success(`已添加「${user.nickname}」为好友`)
  } catch (e: any) {
    ElMessage.warning(e.message)
  }
}

function isFriend(userId: number) {
  return chatStore.friends.some(f => f.id === userId)
}
</script>

<template>
  <el-dialog :model-value="modelValue" @update:model-value="emit('update:modelValue', $event)" title="添加好友" width="440px">
    <el-input v-model="keyword" placeholder="搜索用户名或昵称" @keyup.enter="handleSearch" clearable>
      <template #append>
        <el-button :icon="Search" @click="handleSearch" :loading="searching" />
      </template>
    </el-input>

    <div class="search-results">
      <div v-for="user in results" :key="user.id" class="result-item">
        <el-avatar :size="36" :src="user.avatar" />
        <div class="result-info">
          <span class="result-name">{{ user.nickname }}</span>
          <span class="result-username">@{{ user.username }}</span>
        </div>
        <el-button
          v-if="!isFriend(user.id)"
          size="small"
          type="primary"
          @click="handleAdd(user)"
          round
        >
          添加
        </el-button>
        <el-tag v-else size="small" type="info">已添加</el-tag>
      </div>
      <el-empty v-if="keyword && !searching && results.length === 0" description="未找到用户" :image-size="60" />
    </div>
  </el-dialog>
</template>

<script lang="ts">
import { Search } from '@element-plus/icons-vue'
export default { components: { Search } }
</script>

<style scoped>
:deep(.el-dialog) {
  border-radius: var(--line-radius-lg);
}
.search-results {
  margin-top: 16px;
  max-height: 300px;
  overflow-y: auto;
}
.result-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 8px 0;
  border-bottom: 1px solid var(--line-border-light);
}
.result-info { flex: 1; }
.result-name {
  display: block;
  font-size: 14px;
  color: var(--line-text-primary);
}
.result-username {
  display: block;
  font-size: 12px;
  color: var(--line-text-tertiary);
}
</style>
