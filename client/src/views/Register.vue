<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'

const router = useRouter()
const userStore = useUserStore()

const form = ref({ username: '', password: '', confirmPassword: '' })
const loading = ref(false)

async function handleRegister() {
  if (!form.value.username || !form.value.password) {
    ElMessage.warning('请填写完整信息')
    return
  }
  if (form.value.password !== form.value.confirmPassword) {
    ElMessage.warning('两次密码不一致')
    return
  }
  loading.value = true
  try {
    await userStore.register(form.value.username, form.value.password)
    ElMessage.success('注册成功，请登录')
    router.push('/login')
  } catch (e: any) {
    ElMessage.error(e.message || '注册失败')
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="register-page">
    <div class="register-card">
      <div class="register-logo">
        <el-icon :size="28" color="#fff"><ChatDotRound /></el-icon>
      </div>
      <h2>注册账号</h2>
      <p>加入 MiniChat</p>
      <el-form :model="form" @submit.prevent="handleRegister" label-position="top">
        <el-form-item label="用户名">
          <el-input v-model="form.username" placeholder="请输入用户名" prefix-icon="User" />
        </el-form-item>
        <el-form-item label="密码">
          <el-input v-model="form.password" type="password" placeholder="请输入密码" prefix-icon="Lock" show-password />
        </el-form-item>
        <el-form-item label="确认密码">
          <el-input v-model="form.confirmPassword" type="password" placeholder="请再次输入密码" prefix-icon="Lock" show-password />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="loading" native-type="submit" round size="large" class="register-btn">
            注 册
          </el-button>
        </el-form-item>
      </el-form>
      <div class="footer-link">
        已有账号？<router-link to="/login">返回登录</router-link>
      </div>
    </div>
  </div>
</template>

<style scoped>
.register-page {
  height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--line-bg-primary);
}
.register-card {
  width: 380px;
  padding: 40px 32px;
  border-radius: var(--line-radius-lg);
  box-shadow: var(--line-shadow-card);
  border: 1px solid var(--line-border-light);
  text-align: center;
}
.register-logo {
  width: 56px;
  height: 56px;
  border-radius: 50%;
  background: var(--line-green);
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 20px;
}
.register-card h2 {
  margin: 0 0 4px;
  font-size: 24px;
  color: var(--line-text-primary);
  font-weight: 600;
}
.register-card p {
  margin: 0 0 28px;
  color: var(--line-text-tertiary);
  font-size: 14px;
}
.register-btn {
  width: 100%;
  height: 44px;
  font-size: 15px;
}
.footer-link {
  text-align: center;
  font-size: 13px;
  color: var(--line-text-tertiary);
}
</style>
