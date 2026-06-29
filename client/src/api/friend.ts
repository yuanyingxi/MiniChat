import { get, post, del } from '@/utils/request'
import type { Friend, FriendRequestVO } from '@/types'

export function getFriends() {
  return get<Friend[]>('/friend/list')
}

export function sendFriendRequest(toId: number | string, remark?: string) {
  return post<void>('/friend/request', { toId, remark })
}

export function acceptFriendRequest(id: number | string) {
  return post<void>(`/friend/request/${id}/accept`)
}

export function rejectFriendRequest(id: number | string) {
  return post<void>(`/friend/request/${id}/reject`)
}

export function getFriendRequests() {
  return get<FriendRequestVO[]>('/friend/requests')
}

export function deleteFriend(friendId: number | string) {
  return del<void>(`/friend/${friendId}`)
}

export function blockFriend(friendId: number | string) {
  return post<void>(`/friend/${friendId}/block`)
}
