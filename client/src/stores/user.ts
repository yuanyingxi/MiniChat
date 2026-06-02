import { defineStore } from 'pinia'
import { ref } from 'vue'
import type { User } from '@/types'
import * as authApi from '@/api/auth'
import * as userApi from '@/api/user'

export const useUserStore = defineStore('user', () => {
  const currentUser = ref<User | null>(null)
  const token = ref('')

  async function login(username: string, password: string) {
    const user = await authApi.login({ username, password })
    currentUser.value = user
    token.value = 'mock-token-' + user.id
  }

  async function register(username: string, password: string) {
    await authApi.register({ username, password, confirmPassword: password })
  }

  async function logout() {
    await authApi.logout()
    currentUser.value = null
    token.value = ''
  }

  async function updateProfile(data: Partial<User>) {
    if (!currentUser.value) return
    const updated = await userApi.updateUser(currentUser.value.id, data)
    currentUser.value = updated
  }

  async function deleteAccount() {
    if (!currentUser.value) return
    await userApi.deleteUser(currentUser.value.id)
    currentUser.value = null
    token.value = ''
  }

  return { currentUser, token, login, register, logout, updateProfile, deleteAccount }
})
