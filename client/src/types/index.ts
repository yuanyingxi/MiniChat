export interface User {
  id: number
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
  friendId: number
  nickname: string
  avatar: string
  remark: string
  createTime: string
  blocked?: boolean
}

export interface FriendRequestVO {
  id: number
  fromId: number
  toId: number
  remark: string
  status: number
  createTime: string
  updateTime: string
}

export interface Group {
  groupId: number
  name: string
  avatar: string
  ownerId: number
  ownerNickname: string
  notice: string
  memberCount: number
  createTime: string
}

export interface GroupMemberVO {
  userId: number
  nickname: string
  avatar: string
  role: number
  alias: string
}

export interface Conversation {
  id: number
  type: 'private' | 'group'
  targetId: number
  name: string
  avatar: string
  lastMessage: string
  lastTime: string
  unreadCount: number
}

export interface Message {
  id: number
  conversationId: number
  senderId: number
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
  memberIds: number[]
}
