import { defineStore } from 'pinia'
import { ref } from 'vue'
import type { User } from '@/types'
import * as authApi from '@/api/auth'
import * as userApi from '@/api/user'
import { useChatStore } from '@/stores/chat'

export const useUserStore = defineStore('user', () => {
  const currentUser = ref<User | null>(null)
  const token = ref(localStorage.getItem('minichat_token') || '')

  async function init() {
    if (!token.value || currentUser.value) return
    const userId = localStorage.getItem('minichat_user_id')
    if (!userId) return
    try {
      currentUser.value = await userApi.getUser(userId)
      // 登录态恢复后，连 WS
      useChatStore().initWs()
    } catch {
      logout()
    }
  }

  async function login(phone: string, password: string) {
    const jwt = await authApi.login(phone, password)
    token.value = jwt
    localStorage.setItem('minichat_token', jwt)
    const parts = jwt.split('.')
    if (parts.length === 3) {
      const payload = JSON.parse(atob(parts[1]!))
      const userId = String(payload.sub ?? '')
      localStorage.setItem('minichat_user_id', userId)
      const user = await userApi.getUser(userId)
      currentUser.value = user
      // 登录成功后连 WS
      useChatStore().initWs()
    }
  }

  async function register(phone: string, smsCode: string, password: string) {
    await authApi.register(phone, smsCode, password)
  }

  async function logout() {
    try { await authApi.logout() } catch { /* ignore */ }
    useChatStore().closeWs()
    currentUser.value = null
    token.value = ''
    localStorage.removeItem('minichat_token')
    localStorage.removeItem('minichat_user_id')
  }

  async function updateProfile(data: Partial<User>) {
    if (!currentUser.value) return
    // 用 localStorage 里的字符串 ID，避免雪花ID JS精度丢失
    const userId = localStorage.getItem('minichat_user_id')
    if (!userId) return
    await userApi.updateUser(userId, data)
    currentUser.value = { ...currentUser.value, ...data }
  }

  async function deleteAccount() {
    if (!currentUser.value) return
    useChatStore().closeWs()
    currentUser.value = null
    token.value = ''
    localStorage.removeItem('minichat_token')
    localStorage.removeItem('minichat_user_id')
  }

  return { currentUser, token, init, login, register, logout, updateProfile, deleteAccount }
})
