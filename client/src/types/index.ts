export interface User {
  id: number
  username: string
  nickname: string
  avatar: string
  signature: string
  status: 'online' | 'offline'
}

export interface LoginForm {
  username: string
  password: string
}

export interface RegisterForm {
  username: string
  password: string
  confirmPassword: string
}

export interface Friend extends User {
  remark: string
  blocked: boolean
}

export interface Group {
  id: number
  name: string
  avatar: string
  ownerId: number
  members: GroupMember[]
  createdAt: string
}

export interface GroupMember {
  userId: number
  nickname: string
  avatar: string
  role: 'owner' | 'admin' | 'member'
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
