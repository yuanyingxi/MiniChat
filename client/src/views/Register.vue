<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { sendSmsCode } from '@/api/auth'
import { ElMessage } from 'element-plus'

const router = useRouter()
const userStore = useUserStore()

const form = ref({ phone: '', smsCode: '', password: '' })
const loading = ref(false)
const smsCountdown = ref(0)
let smsTimer: ReturnType<typeof setInterval> | null = null

async function handleSendSms() {
  if (!form.value.phone || !/^1[3-9]\d{9}$/.test(form.value.phone)) {
    ElMessage.warning('请输入正确的手机号')
    return
  }
  try {
    await sendSmsCode(form.value.phone)
    ElMessage.success('验证码已发送')
    smsCountdown.value = 60
    smsTimer = setInterval(() => {
      smsCountdown.value--
      if (smsCountdown.value <= 0 && smsTimer) {
        clearInterval(smsTimer)
        smsTimer = null
      }
    }, 1000)
  } catch (e: any) {
    ElMessage.error(e.message || '发送失败')
  }
}

async function handleRegister() {
  if (!form.value.phone || !form.value.smsCode || !form.value.password) {
    ElMessage.warning('请填写完整信息')
    return
  }
  if (!/^1[3-9]\d{9}$/.test(form.value.phone)) {
    ElMessage.warning('请输入正确的手机号')
    return
  }
  if (form.value.password.length < 6) {
    ElMessage.warning('密码至少6位')
    return
  }
  loading.value = true
  try {
    await userStore.register(form.value.phone, form.value.smsCode, form.value.password)
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
        <el-form-item label="手机号">
          <el-input v-model="form.phone" placeholder="请输入手机号" prefix-icon="Iphone" maxlength="11" />
        </el-form-item>
        <el-form-item label="验证码">
          <div class="sms-row">
            <el-input v-model="form.smsCode" placeholder="请输入验证码" maxlength="6" />
            <el-button
              :disabled="smsCountdown > 0"
              @click="handleSendSms"
              class="sms-btn"
            >
              {{ smsCountdown > 0 ? `${smsCountdown}s` : '获取验证码' }}
            </el-button>
          </div>
        </el-form-item>
        <el-form-item label="密码">
          <el-input v-model="form.password" type="password" placeholder="请输入密码（6-20位）" prefix-icon="Lock" show-password />
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
.sms-row {
  display: flex;
  gap: 8px;
  width: 100%;
}
.sms-row .el-input {
  flex: 1;
}
.sms-btn {
  flex-shrink: 0;
  white-space: nowrap;
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
