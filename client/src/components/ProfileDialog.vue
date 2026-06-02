<script setup lang="ts">
import { ref, watch } from 'vue'
import { useUserStore } from '@/stores/user'
import { ElMessage, ElMessageBox } from 'element-plus'

const props = defineProps<{ modelValue: boolean }>()
const emit = defineEmits<{ 'update:modelValue': [val: boolean] }>()

const userStore = useUserStore()

const form = ref({ nickname: '', signature: '' })
const passwordForm = ref({ oldPassword: '', newPassword: '' })

watch(() => props.modelValue, (val) => {
  if (val && userStore.currentUser) {
    form.value.nickname = userStore.currentUser.nickname
    form.value.signature = userStore.currentUser.signature
  }
})

function handleClose() {
  emit('update:modelValue', false)
}

async function handleSaveProfile() {
  await userStore.updateProfile({
    nickname: form.value.nickname,
    signature: form.value.signature,
  })
  ElMessage.success('个人信息已更新')
}

async function handleChangePassword() {
  if (!passwordForm.value.newPassword) {
    ElMessage.warning('请输入新密码')
    return
  }
  ElMessage.success('密码已修改（Mock）')
  passwordForm.value = { oldPassword: '', newPassword: '' }
}

async function handleDeleteAccount() {
  try {
    await ElMessageBox.confirm('注销后账号数据将无法恢复，确定要注销吗？', '注销账号', { type: 'error' })
    await userStore.deleteAccount()
    ElMessage.success('账号已注销')
    handleClose()
  } catch { /* cancelled */ }
}
</script>

<template>
  <el-dialog :model-value="modelValue" @update:model-value="emit('update:modelValue', $event)" title="个人信息" width="440px">
    <el-form :model="form" label-width="80px">
      <el-form-item label="头像">
        <el-avatar :size="56" :src="userStore.currentUser?.avatar" />
      </el-form-item>
      <el-form-item label="用户名">
        <el-input :model-value="userStore.currentUser?.username" disabled />
      </el-form-item>
      <el-form-item label="昵称">
        <el-input v-model="form.nickname" />
      </el-form-item>
      <el-form-item label="签名">
        <el-input v-model="form.signature" type="textarea" :rows="2" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="handleSaveProfile" round>保存修改</el-button>
      </el-form-item>
    </el-form>

    <el-divider>修改密码</el-divider>
    <el-form :model="passwordForm" label-width="80px">
      <el-form-item label="当前密码">
        <el-input v-model="passwordForm.oldPassword" type="password" show-password />
      </el-form-item>
      <el-form-item label="新密码">
        <el-input v-model="passwordForm.newPassword" type="password" show-password />
      </el-form-item>
      <el-form-item>
        <el-button @click="handleChangePassword">修改密码</el-button>
      </el-form-item>
    </el-form>

    <el-divider />
    <el-button type="danger" text @click="handleDeleteAccount">注销账号</el-button>
  </el-dialog>
</template>

<style scoped>
:deep(.el-dialog) {
  border-radius: var(--line-radius-lg);
}
:deep(.el-button--primary) {
  border-radius: var(--line-radius-pill);
}
</style>
