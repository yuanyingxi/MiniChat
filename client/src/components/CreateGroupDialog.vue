<script setup lang="ts">
import { ref } from 'vue'
import { useUserStore } from '@/stores/user'
import { useChatStore } from '@/stores/chat'
import type { Friend } from '@/types'
import { ElMessage } from 'element-plus'

const props = defineProps<{ modelValue: boolean }>()
const emit = defineEmits<{ 'update:modelValue': [val: boolean] }>()

const userStore = useUserStore()
const chatStore = useChatStore()

const groupName = ref('')
const selectedIds = ref<(number | string)[]>([])

function toggleFriend(id: number | string) {
  const idx = selectedIds.value.indexOf(id)
  if (idx >= 0) {
    selectedIds.value.splice(idx, 1)
  } else {
    selectedIds.value.push(id)
  }
}

function isSelected(id: number | string) {
  return selectedIds.value.includes(id)
}

function getSelectedNames() {
  return chatStore.friends
    .filter(f => isSelected(f.friendId))
    .map(f => f.remark || f.nickname)
}

async function handleCreate() {
  if (!groupName.value.trim()) {
    ElMessage.warning('请输入群名')
    return
  }
  if (selectedIds.value.length === 0) {
    ElMessage.warning('请至少选择一位好友')
    return
  }
  if (!userStore.currentUser) return

  await chatStore.createGroup(
    groupName.value.trim(),
    selectedIds.value,
  )
  ElMessage.success('群聊创建成功')
  groupName.value = ''
  selectedIds.value = []
  emit('update:modelValue', false)
}
</script>

<template>
  <el-dialog
    :model-value="modelValue"
    @update:model-value="emit('update:modelValue', $event)"
    title="创建群聊"
    width="440px"
  >
    <el-input
      v-model="groupName"
      placeholder="请输入群名称"
      size="large"
      class="group-name-input"
    />

    <div class="select-hint">
      已选择 <b>{{ selectedIds.length }}</b> 位好友
      <span v-if="selectedIds.length > 0" class="selected-preview">
        — {{ getSelectedNames().join('、') }}
      </span>
    </div>

    <div class="friend-pool">
      <div
        v-for="friend in chatStore.friends"
        :key="friend.friendId"
        class="friend-row"
        :class="{ selected: isSelected(friend.friendId) }"
        @click="toggleFriend(friend.friendId)"
      >
        <el-avatar :size="40" :src="friend.avatar" />
        <div class="friend-name">{{ friend.remark || friend.nickname }}</div>
        <el-icon v-if="isSelected(friend.friendId)" class="check-icon" color="#07c160">
          <CircleCheckFilled />
        </el-icon>
        <div v-else class="check-circle" />
      </div>
      <el-empty v-if="chatStore.friends.length === 0" description="暂无好友" :image-size="50" />
    </div>

    <div class="dialog-footer">
      <el-button @click="emit('update:modelValue', false)" round>取消</el-button>
      <el-button type="primary" @click="handleCreate" :disabled="!groupName.trim() || selectedIds.length === 0" round>
        创建群聊
      </el-button>
    </div>
  </el-dialog>
</template>

<script lang="ts">
import { CircleCheckFilled } from '@element-plus/icons-vue'
export default { components: { CircleCheckFilled } }
</script>

<style scoped>
:deep(.el-dialog) {
  border-radius: var(--line-radius-lg);
}

.group-name-input {
  margin-bottom: 12px;
}

.select-hint {
  font-size: 13px;
  color: var(--line-text-secondary);
  margin-bottom: 12px;
}
.select-hint b {
  color: var(--line-green);
}
.selected-preview {
  color: var(--line-text-tertiary);
  font-size: 12px;
}

.friend-pool {
  max-height: 260px;
  overflow-y: auto;
  border: 1px solid var(--line-border);
  border-radius: var(--line-radius-md);
  padding: 4px 0;
  margin-bottom: 16px;
}

.friend-row {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 10px 16px;
  cursor: pointer;
  transition: background 0.15s;
}
.friend-row:hover {
  background: var(--line-sidebar-item-hover);
}
.friend-row.selected {
  background: #e8f8ee;
}

.friend-name {
  flex: 1;
  font-size: 14px;
  font-weight: 500;
  color: var(--line-text-primary);
}

.check-icon {
  font-size: 22px;
}

.check-circle {
  width: 22px;
  height: 22px;
  border-radius: 50%;
  border: 2px solid var(--line-border);
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}
</style>
