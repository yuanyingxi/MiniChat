import { get, post, del } from '@/utils/request'
import type { Friend, FriendRequestVO } from '@/types'

export function getFriends() {
  return get<Friend[]>('/friend/list')
}

export function sendFriendRequest(toId: number, remark?: string) {
  return post<void>('/friend/request', { toId, remark })
}

export function acceptFriendRequest(id: number) {
  return post<void>(`/friend/request/${id}/accept`)
}

export function rejectFriendRequest(id: number) {
  return post<void>(`/friend/request/${id}/reject`)
}

export function getFriendRequests() {
  return get<FriendRequestVO[]>('/friend/requests')
}

export function deleteFriend(friendId: number) {
  return del<void>(`/friend/${friendId}`)
}

export function blockFriend(friendId: number) {
  return post<void>(`/friend/${friendId}/block`)
}
