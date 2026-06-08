import { mockMessages, mockConversations } from '@/mock/data'
import type { Message, Conversation } from '@/types'

const delay = (ms = 300) => new Promise(r => setTimeout(r, ms))

// Replace mock senderId:1 with real logged-in user ID
function patchMyMessages(data: Record<number, Message[]>) {
  const myId = localStorage.getItem('minichat_user_id')
  if (!myId) return data
  for (const msgs of Object.values(data)) {
    for (const msg of msgs) {
      if (String(msg.senderId) === '1') {
        msg.senderId = myId
      }
    }
  }
  return data
}

let messageStore = patchMyMessages(JSON.parse(JSON.stringify(mockMessages))) as Record<number, Message[]>
let conversationStore = JSON.parse(JSON.stringify(mockConversations)) as Conversation[]
let nextMsgId = 100

export async function getMessages(conversationId: number): Promise<Message[]> {
  await delay(100)
  return [...(messageStore[conversationId] ?? [])]
}

export async function sendMessage(
  conversationId: number,
  senderId: number,
  senderName: string,
  senderAvatar: string,
  type: Message['type'],
  content: string,
  fileName?: string,
): Promise<Message> {
  await delay(50)
  const msg: Message = {
    id: nextMsgId++,
    conversationId,
    senderId,
    senderName,
    senderAvatar,
    type,
    content,
    fileName,
    createdAt: new Date().toISOString(),
    read: false,
  }
  if (!messageStore[conversationId]) messageStore[conversationId] = []
  messageStore[conversationId].push(msg)

  const conv = conversationStore.find(c => c.id === conversationId)
  if (conv) {
    conv.lastMessage = type === 'text' ? content : `[${type}]`
    conv.lastTime = msg.createdAt
  }

  return { ...msg }
}

export async function getConversations(): Promise<Conversation[]> {
  await delay(100)
  return [...conversationStore].sort(
    (a, b) => new Date(b.lastTime).getTime() - new Date(a.lastTime).getTime(),
  )
}
