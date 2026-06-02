import { defineStore } from 'pinia'
import { ref } from 'vue'
import type { Conversation, Message, Friend, Group } from '@/types'
import * as msgApi from '@/api/message'
import * as friendApi from '@/api/friend'
import * as groupApi from '@/api/group'

export const useChatStore = defineStore('chat', () => {
  const conversations = ref<Conversation[]>([])
  const activeConversation = ref<Conversation | null>(null)
  const messages = ref<Message[]>([])
  const friends = ref<Friend[]>([])
  const groups = ref<Group[]>([])
  const loading = ref(false)

  async function loadConversations() {
    conversations.value = await msgApi.getConversations()
  }

  async function selectConversation(conv: Conversation) {
    activeConversation.value = conv
    conv.unreadCount = 0
    loading.value = true
    try {
      messages.value = await msgApi.getMessages(conv.id)
    } finally {
      loading.value = false
    }
  }

  async function sendMessage(
    senderId: number,
    senderName: string,
    senderAvatar: string,
    type: Message['type'],
    content: string,
    fileName?: string,
  ) {
    if (!activeConversation.value) return
    const msg = await msgApi.sendMessage(
      activeConversation.value.id,
      senderId, senderName, senderAvatar, type, content, fileName,
    )
    messages.value.push(msg)
    const conv = conversations.value.find(c => c.id === activeConversation.value!.id)
    if (conv) {
      conv.lastMessage = type === 'text' ? content : `[${type}]`
      conv.lastTime = msg.createdAt
    }
  }

  async function loadFriends() {
    friends.value = await friendApi.getFriends()
  }

  async function addFriend(userId: number) {
    const friend = await friendApi.addFriend(userId)
    friends.value.push(friend)
  }

  async function deleteFriend(userId: number) {
    await friendApi.deleteFriend(userId)
    friends.value = friends.value.filter(f => f.id !== userId)
  }

  async function blockFriend(userId: number, blocked: boolean) {
    await friendApi.blockFriend(userId, blocked)
    const f = friends.value.find(f => f.id === userId)
    if (f) f.blocked = blocked
  }

  async function loadGroups() {
    groups.value = await groupApi.getGroups()
  }

  async function createGroup(name: string, memberIds: number[], ownerId: number) {
    const group = await groupApi.createGroup({ name, memberIds }, ownerId)
    groups.value.push(group)
    return group
  }

  async function leaveGroup(groupId: number) {
    await groupApi.leaveGroup(groupId)
    groups.value = groups.value.filter(g => g.id !== groupId)
  }

  return {
    conversations, activeConversation, messages, friends, groups, loading,
    loadConversations, selectConversation, sendMessage,
    loadFriends, addFriend, deleteFriend, blockFriend,
    loadGroups, createGroup, leaveGroup,
  }
})
