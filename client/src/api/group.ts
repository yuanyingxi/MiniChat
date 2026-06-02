import { mockGroups } from '@/mock/data'
import type { Group, GroupMember, CreateGroupForm } from '@/types'

const delay = (ms = 300) => new Promise(r => setTimeout(r, ms))

let groups = [...mockGroups]
let nextId = 100

export async function getGroups(): Promise<Group[]> {
  await delay()
  return [...groups]
}

export async function createGroup(form: CreateGroupForm, ownerId: number): Promise<Group> {
  await delay()
  const { mockUsers } = await import('@/mock/data')
  const members: GroupMember[] = form.memberIds
    .map(id => mockUsers.find(u => u.id === id))
    .filter(Boolean)
    .map(u => ({ userId: u!.id, nickname: u!.nickname, avatar: u!.avatar, role: 'member' as const }))
  members.unshift({
    userId: ownerId,
    nickname: mockUsers.find(u => u.id === ownerId)?.nickname ?? '',
    avatar: mockUsers.find(u => u.id === ownerId)?.avatar ?? '',
    role: 'owner',
  })
  const group: Group = {
    id: nextId++,
    name: form.name,
    avatar: `https://api.dicebear.com/7.x/adventurer/svg?seed=group${nextId}`,
    ownerId,
    members,
    createdAt: new Date().toISOString(),
  }
  groups.push(group)
  return { ...group }
}

export async function leaveGroup(groupId: number): Promise<void> {
  await delay()
  groups = groups.filter(g => g.id !== groupId)
}
