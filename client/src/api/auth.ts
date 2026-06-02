import { mockUsers } from '@/mock/data'
import type { LoginForm, RegisterForm, User } from '@/types'

const delay = (ms = 300) => new Promise(r => setTimeout(r, ms))

export async function login(form: LoginForm): Promise<User> {
  await delay()
  const user = mockUsers.find(u => u.username === form.username)
  if (!user) throw new Error('用户名或密码错误')
  return { ...user }
}

export async function register(_form: RegisterForm): Promise<void> {
  await delay()
}

export async function logout(): Promise<void> {
  await delay(100)
}
