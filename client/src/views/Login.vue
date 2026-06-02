<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'

const router = useRouter()
const userStore = useUserStore()

const form = ref({ username: '', password: '' })
const loading = ref(false)

async function handleLogin() {
  if (!form.value.username || !form.value.password) {
    ElMessage.warning('请输入用户名和密码')
    return
  }
  loading.value = true
  try {
    await userStore.login(form.value.username, form.value.password)
    localStorage.setItem('minichat_token', userStore.token)
    ElMessage.success('登录成功')
    router.push('/chat')
  } catch (e: any) {
    ElMessage.error(e.message || '登录失败')
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="login-page">
    <div class="login-card">
      <div class="login-logo">
        <el-icon :size="28" color="#fff"><ChatDotRound /></el-icon>
      </div>
      <h2>MiniChat</h2>
      <p>即时通讯系统</p>
      <el-form :model="form" @submit.prevent="handleLogin" label-position="top">
        <el-form-item label="用户名">
          <el-input v-model="form.username" placeholder="请输入用户名" prefix-icon="User" />
        </el-form-item>
        <el-form-item label="密码">
          <el-input v-model="form.password" type="password" placeholder="请输入密码" prefix-icon="Lock" show-password />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="loading" native-type="submit" round size="large" class="login-btn">
            登 录
          </el-button>
        </el-form-item>
      </el-form>
      <div class="footer-link">
        还没有账号？<router-link to="/register">立即注册</router-link>
      </div>
    </div>
  </div>
</template>

<style scoped>
.login-page {
  height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--line-bg-primary);
}
.login-card {
  width: 380px;
  padding: 40px 32px;
  border-radius: var(--line-radius-lg);
  box-shadow: var(--line-shadow-card);
  border: 1px solid var(--line-border-light);
  text-align: center;
}
.login-logo {
  width: 56px;
  height: 56px;
  border-radius: 50%;
  background: var(--line-green);
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 20px;
}
.login-card h2 {
  margin: 0 0 4px;
  font-size: 24px;
  color: var(--line-text-primary);
  font-weight: 600;
}
.login-card p {
  margin: 0 0 28px;
  color: var(--line-text-tertiary);
  font-size: 14px;
}
.login-btn {
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
