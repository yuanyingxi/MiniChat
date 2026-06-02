import { mockFriends } from '@/mock/data'
import type { Friend } from '@/types'

const delay = (ms = 300) => new Promise(r => setTimeout(r, ms))

let friends = [...mockFriends]

export async function getFriends(): Promise<Friend[]> {
  await delay()
  return [...friends]
}

export async function addFriend(userId: number): Promise<Friend> {
  await delay()
  const { mockUsers } = await import('@/mock/data')
  const user = mockUsers.find(u => u.id === userId)
  if (!user) throw new Error('用户不存在')
  if (friends.find(f => f.id === userId)) throw new Error('已是好友')
  const friend: Friend = { ...user, remark: '', blocked: false }
  friends.push(friend)
  return { ...friend }
}

export async function deleteFriend(userId: number): Promise<void> {
  await delay()
  friends = friends.filter(f => f.id !== userId)
}

export async function blockFriend(userId: number, blocked: boolean): Promise<void> {
  await delay()
  const f = friends.find(f => f.id === userId)
  if (f) f.blocked = blocked
}
