import type { User, Conversation, Message } from '@/types'

const avatarUrl = (id: number) =>
  `https://api.dicebear.com/7.x/adventurer/svg?seed=user${id}`

export const mockUsers: User[] = [
  { id: 1, phone: '13800000001', nickname: '管理员', avatar: avatarUrl(1), signature: '系统管理员', gender: null, createTime: '2025-01-01T00:00:00' },
  { id: 2, phone: '13800000002', nickname: '张三', avatar: avatarUrl(2), signature: '今天天气不错', gender: null, createTime: '2025-01-01T00:00:00' },
  { id: 3, phone: '13800000003', nickname: '李四', avatar: avatarUrl(3), signature: '学习使我快乐', gender: null, createTime: '2025-01-01T00:00:00' },
  { id: 4, phone: '13800000004', nickname: '王五', avatar: avatarUrl(4), signature: '永远年轻', gender: null, createTime: '2025-01-01T00:00:00' },
  { id: 5, phone: '13800000005', nickname: '赵六', avatar: avatarUrl(5), signature: '加油！', gender: null, createTime: '2025-01-01T00:00:00' },
  { id: 6, phone: '13800000006', nickname: '孙七', avatar: avatarUrl(6), signature: '相信过程', gender: null, createTime: '2025-01-01T00:00:00' },
  { id: 7, phone: '13800000007', nickname: '周八', avatar: avatarUrl(7), signature: '保持热爱', gender: null, createTime: '2025-01-01T00:00:00' },
  { id: 8, phone: '13800000008', nickname: '吴九', avatar: avatarUrl(8), signature: '前端开发工程师', gender: null, createTime: '2025-01-01T00:00:00' },
  { id: 9, phone: '13800000009', nickname: '郑十', avatar: avatarUrl(9), signature: '全栈工程师', gender: null, createTime: '2025-01-01T00:00:00' },
  { id: 10, phone: '13800000010', nickname: '测试用户', avatar: avatarUrl(10), signature: '测试中...', gender: null, createTime: '2025-01-01T00:00:00' },
]

const now = new Date()
const h = (hours: number) => new Date(now.getTime() - hours * 3600000).toISOString()

export const mockConversations: Conversation[] = [
  { id: 1, type: 'private', targetId: 2, name: '张三', avatar: avatarUrl(2), lastMessage: '明天一起吃饭吗？', lastTime: h(0.5), unreadCount: 2 },
  { id: 2, type: 'private', targetId: 3, name: '李四', avatar: avatarUrl(3), lastMessage: '作业写完了没？', lastTime: h(2), unreadCount: 0 },
  { id: 3, type: 'group', targetId: 1, name: '项目讨论组', avatar: avatarUrl(101), lastMessage: '王五: 需求文档已上传', lastTime: h(5), unreadCount: 5 },
  { id: 4, type: 'private', targetId: 4, name: '王五', avatar: avatarUrl(4), lastMessage: '收到，谢谢！', lastTime: h(8), unreadCount: 1 },
  { id: 5, type: 'group', targetId: 2, name: '技术交流群', avatar: avatarUrl(102), lastMessage: '孙七: 有人用过 WebSocket 吗', lastTime: h(24), unreadCount: 0 },
  { id: 6, type: 'private', targetId: 5, name: '赵六', avatar: avatarUrl(5), lastMessage: '好的好的', lastTime: h(48), unreadCount: 0 },
]

export const mockMessages: Record<number, Message[]> = {
  1: [
    { id: 1, conversationId: 1, senderId: 2, senderName: '张三', senderAvatar: avatarUrl(2), type: 'text', content: '在吗？', createdAt: h(1), read: true },
    { id: 2, conversationId: 1, senderId: 1, senderName: '管理员', senderAvatar: avatarUrl(1), type: 'text', content: '在的，怎么了？', createdAt: h(0.9), read: true },
    { id: 3, conversationId: 1, senderId: 2, senderName: '张三', senderAvatar: avatarUrl(2), type: 'text', content: '明天一起吃饭吗？', createdAt: h(0.5), read: false },
  ],
  2: [
    { id: 4, conversationId: 2, senderId: 3, senderName: '李四', senderAvatar: avatarUrl(3), type: 'text', content: '最近课程怎么样？', createdAt: h(4), read: true },
    { id: 5, conversationId: 2, senderId: 1, senderName: '管理员', senderAvatar: avatarUrl(1), type: 'text', content: '还行，就是有点忙', createdAt: h(3.5), read: true },
    { id: 6, conversationId: 2, senderId: 3, senderName: '李四', senderAvatar: avatarUrl(3), type: 'text', content: '作业写完了没？', createdAt: h(2), read: true },
  ],
  3: [
    { id: 7, conversationId: 3, senderId: 2, senderName: '张三', senderAvatar: avatarUrl(2), type: 'text', content: '大家看看新的需求文档', createdAt: h(6), read: true },
    { id: 8, conversationId: 3, senderId: 3, senderName: '李四', senderAvatar: avatarUrl(3), type: 'text', content: '好的我看看', createdAt: h(5.5), read: true },
    { id: 9, conversationId: 3, senderId: 4, senderName: '王五', senderAvatar: avatarUrl(4), type: 'text', content: '需求文档已上传', createdAt: h(5), read: false },
  ],
  4: [
    { id: 10, conversationId: 4, senderId: 1, senderName: '管理员', senderAvatar: avatarUrl(1), type: 'text', content: '文档发你了', createdAt: h(9), read: true },
    { id: 11, conversationId: 4, senderId: 4, senderName: '王五', senderAvatar: avatarUrl(4), type: 'text', content: '收到，谢谢！', createdAt: h(8), read: false },
  ],
  5: [
    { id: 12, conversationId: 5, senderId: 6, senderName: '孙七', senderAvatar: avatarUrl(6), type: 'text', content: '有人用过 WebSocket 吗', createdAt: h(24), read: true },
  ],
  6: [
    { id: 13, conversationId: 6, senderId: 1, senderName: '管理员', senderAvatar: avatarUrl(1), type: 'text', content: '明天有空吗', createdAt: h(50), read: true },
    { id: 14, conversationId: 6, senderId: 5, senderName: '赵六', senderAvatar: avatarUrl(5), type: 'text', content: '好的好的', createdAt: h(48), read: true },
  ],
}
