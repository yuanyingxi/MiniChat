import { get, put, post } from '@/utils/request'
import type { User } from '@/types'

export function getUser(id: number) {
  return get<User>(`/user/${id}`)
}

export function updateUser(id: number, data: Partial<User>) {
  return put<void>(`/user/${id}`, data)
}

export function searchUsers(keyword: string) {
  return get<User[]>('/friend/search', { params: { keyword } })
}

export function uploadAvatar(file: File) {
  const formData = new FormData()
  formData.append('file', file)
  return post<string>('/user/avatar', formData)
}
