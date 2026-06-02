import { mockUsers } from '@/mock/data'
import type { User } from '@/types'

const delay = (ms = 300) => new Promise(r => setTimeout(r, ms))

export async function getUser(id: number): Promise<User> {
  await delay()
  const user = mockUsers.find(u => u.id === id)
  if (!user) throw new Error('用户不存在')
  return { ...user }
}

export async function updateUser(id: number, data: Partial<User>): Promise<User> {
  await delay()
  const user = mockUsers.find(u => u.id === id)
  if (!user) throw new Error('用户不存在')
  return { ...user, ...data }
}

export async function deleteUser(_id: number): Promise<void> {
  await delay()
}

export async function searchUsers(keyword: string): Promise<User[]> {
  await delay()
  if (!keyword) return []
  return mockUsers.filter(
    u => u.username.includes(keyword) || u.nickname.includes(keyword),
  )
}
