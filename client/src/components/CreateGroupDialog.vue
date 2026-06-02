<script setup lang="ts">
import { ref } from 'vue'
import { useUserStore } from '@/stores/user'
import { useChatStore } from '@/stores/chat'
import { ElMessage } from 'element-plus'

const props = defineProps<{ modelValue: boolean }>()
const emit = defineEmits<{ 'update:modelValue': [val: boolean] }>()

const userStore = useUserStore()
const chatStore = useChatStore()

const groupName = ref('')
const selectedFriends = ref<number[]>([])

async function handleCreate() {
  if (!groupName.value.trim()) {
    ElMessage.warning('请输入群名')
    return
  }
  if (selectedFriends.value.length === 0) {
    ElMessage.warning('请至少选择一位好友')
    return
  }
  if (!userStore.currentUser) return

  await chatStore.createGroup(groupName.value.trim(), selectedFriends.value, userStore.currentUser.id)
  ElMessage.success('群聊创建成功')
  groupName.value = ''
  selectedFriends.value = []
  emit('update:modelValue', false)
}
</script>

<template>
  <el-dialog :model-value="modelValue" @update:model-value="emit('update:modelValue', $event)" title="创建群聊" width="440px">
    <el-form label-position="top">
      <el-form-item label="群名称">
        <el-input v-model="groupName" placeholder="请输入群名称" />
      </el-form-item>
      <el-form-item label="选择好友">
        <el-checkbox-group v-model="selectedFriends">
          <el-checkbox
            v-for="friend in chatStore.friends.filter(f => !f.blocked)"
            :key="friend.id"
            :value="friend.id"
            :label="friend.remark || friend.nickname"
          />
        </el-checkbox-group>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="handleCreate" round>创建群聊</el-button>
      </el-form-item>
    </el-form>
  </el-dialog>
</template>

<style scoped>
:deep(.el-dialog) {
  border-radius: var(--line-radius-lg);
}
</style>
