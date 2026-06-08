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

  async function sendFriendRequest(toId: number, remark?: string) {
    await friendApi.sendFriendRequest(toId, remark)
  }

  async function deleteFriend(friendId: number) {
    await friendApi.deleteFriend(friendId)
    friends.value = friends.value.filter(f => f.friendId !== friendId)
  }

  async function blockFriend(friendId: number) {
    await friendApi.blockFriend(friendId)
    const f = friends.value.find(f => f.friendId === friendId)
    if (f) f.blocked = !f.blocked
  }

  async function loadGroups() {
    groups.value = await groupApi.getGroups()
  }

  async function createGroup(name: string, memberIds: number[]) {
    const groupId = await groupApi.createGroup({ name, memberIds })
    await loadGroups()
    return groupId
  }

  async function leaveGroup(groupId: number) {
    await groupApi.leaveGroup(groupId)
    groups.value = groups.value.filter(g => g.groupId !== groupId)
  }

  return {
    conversations, activeConversation, messages, friends, groups, loading,
    loadConversations, selectConversation, sendMessage,
    loadFriends, sendFriendRequest, deleteFriend, blockFriend,
    loadGroups, createGroup, leaveGroup,
  }
})
