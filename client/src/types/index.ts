// ── 用户 & 认证 ──

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

// ── 好友 ──

export interface Friend {
  friendId: number | string
  nickname: string
  avatar: string
  remark: string
  createTime: string
}

export interface FriendRequestVO {
  id: number | string
  fromId: number | string
  toId: number | string
  remark: string
  status: number   // 0待处理 1同意 2拒绝
  createTime: string
  updateTime: string
}

// ── 群组 ──

export interface Group {
  groupId: number | string
  name: string
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
  role: number      // 0普通 1管理员 2群主
  alias: string
}

export interface CreateGroupForm {
  name: string
  memberIds: (number | string)[]
}

// ── 消息内容（对应后端 content JSON） ──

export interface TextContent {
  text: string
}

export interface ImageContent {
  url: string
  thumbnailUrl?: string
  width?: number
  height?: number
  size?: number
}

export interface FileContent {
  fileName: string
  url: string
  size?: number
  suffix?: string
}

/** 消息类型枚举 */
export enum MessageType {
  TEXT = 1,
  IMAGE = 2,
  VIDEO = 3,
  VOICE = 4,
}

// ── 消息 ──

export interface Message {
  id: number | string
  fromId: number | string
  toId: number | string
  messageType: MessageType
  content: TextContent | ImageContent | FileContent | Record<string, unknown>
  createTime: string
}

// ── 会话（前端 UI 概念，后端无此 API） ──

export interface Conversation {
  type: 'private' | 'group'
  targetId: number | string
  name: string
  avatar: string
  lastMessage: string
  lastTime: string
  unreadCount: number
}