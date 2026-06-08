import { get, post } from '@/utils/request'
import type { Group, GroupMemberVO, CreateGroupForm } from '@/types'

export function getGroups() {
  return get<Group[]>('/group/list')
}

export function createGroup(form: CreateGroupForm) {
  return post<number>('/group', form)
}

export function leaveGroup(groupId: number) {
  return post<void>(`/group/${groupId}/quit`)
}

export function getGroupMembers(groupId: number) {
  return get<GroupMemberVO[]>(`/group/${groupId}/members`)
}
