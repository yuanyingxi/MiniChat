export interface User {
  id: number | string
  phone: string
  nickname: string
  avatar: string
  signature: string
  gender: number | null
  createTime: string
}

export interface LoginForm {
  phone: string
  password: string
}

export interface RegisterForm {
  phone: string
  smsCode: string
  password: string
}

export interface Friend {
  friendId: number | string
  nickname: string
  avatar: string
  remark: string
  createTime: string
  blocked?: boolean
}

export interface FriendRequestVO {
  id: number | string
  fromId: number | string
  toId: number | string
  remark: string
  status: number
  createTime: string
  updateTime: string
}

export interface Group {
  groupId: number | string
  name: string
  avatar: string
  ownerId: number | string
  ownerNickname: string
  notice: string
  memberCount: number
  createTime: string
}

export interface GroupMemberVO {
  userId: number | string
  nickname: string
  avatar: string
  role: number
  alias: string
}

export interface Conversation {
  id: number | string
  type: 'private' | 'group'
  targetId: number | string
  name: string
  avatar: string
  lastMessage: string
  lastTime: string
  unreadCount: number
}

export interface Message {
  id: number | string
  conversationId: number | string
  senderId: number | string
  senderName: string
  senderAvatar: string
  type: 'text' | 'image' | 'file'
  content: string
  fileName?: string
  fileSize?: number
  createdAt: string
  read: boolean
}

export interface CreateGroupForm {
  name: string
  memberIds: (number | string)[]
}
