import { defineStore } from 'pinia'
import { ref } from 'vue'
import type { Conversation, Message, Friend, Group, FriendRequestVO, TextContent, ImageContent, FileContent } from '@/types'
import * as msgApi from '@/api/message'
import * as friendApi from '@/api/friend'
import * as groupApi from '@/api/group'
import { connect, disconnect, sendWsMessage, isConnected } from '@/api/ws'
import { ElMessage } from 'element-plus'

export const useChatStore = defineStore('chat', () => {
  const conversations = ref<Conversation[]>([])
  const activeConversation = ref<Conversation | null>(null)
  const messages = ref<Message[]>([])
  const friends = ref<Friend[]>([])
  const friendRequests = ref<FriendRequestVO[]>([])
  const groups = ref<Group[]>([])
  const loading = ref(false)

  // ── WS 连接 & 消息接收 ──

  function initWs() {
    connect(
      // onMessage: 收到新消息
      (msg: Message) => {
        // 如果是当前会话，直接追加
        if (activeConversation.value) {
          const conv = activeConversation.value
          if (
            (conv.type === 'private' && String(msg.fromId) === String(conv.targetId)) ||
            (conv.type === 'private' && String(msg.toId) === String(conv.targetId)) ||
            (conv.type === 'group' && String(msg.toId) === String(conv.targetId))
          ) {
            messages.value.push(msg)
          }
        }
        // 更新会话列表的最后消息
        updateConversationPreview(msg)
      },
      // onHeartbeat
      undefined,
    )
  }

  function closeWs() {
    disconnect()
    messages.value = []
    activeConversation.value = null
  }

  function updateConversationPreview(msg: Message) {
    // 私聊: 对方 ID 可能是 fromId 也可能是 toId
    const myId = localStorage.getItem('minichat_user_id') || ''
    const peerId = String(msg.fromId) === myId ? String(msg.toId) : String(msg.fromId)
    // 找到对应会话更新 lastMessage
    const conv = conversations.value.find(c =>
      (c.type === 'private' && String(c.targetId) === peerId) ||
      (c.type === 'group' && String(c.targetId) === String(msg.toId)),
    )
    const preview = getContentPreview(msg.content)
    if (conv) {
      conv.lastMessage = preview
      conv.lastTime = msg.createTime
    }
  }

  function getContentPreview(content: Message['content']): string {
    if (typeof content === 'string') return content
    if ('text' in content) return (content as TextContent).text
    if ('url' in content) return '[图片]'
    if ('fileName' in content) return `[文件]${(content as unknown as FileContent).fileName}`
    return '[消息]'
  }

  // ── 会话：从好友 + 群组构建 ──

  async function loadConversations() {
    await Promise.all([loadFriends(), loadGroups()])
    // 用好友列表构建私聊会话
    const privateConvs: Conversation[] = friends.value.map(f => ({
      type: 'private' as const,
      targetId: f.friendId,
      name: f.remark || f.nickname,
      avatar: f.avatar,
      lastMessage: '',
      lastTime: '',
      unreadCount: 0,
    }))
    // 用群组列表构建群聊会话
    const groupConvs: Conversation[] = groups.value.map(g => ({
      type: 'group' as const,
      targetId: g.groupId,
      name: g.name,
      avatar: '',
      lastMessage: '',
      lastTime: g.createTime,
      unreadCount: 0,
    }))
    conversations.value = [...privateConvs, ...groupConvs]
  }

  // ── 快捷跳转到好友/群聊 ──

  function openFriendChat(friend: Friend) {
    const conv: Conversation = {
      type: 'private',
      targetId: friend.friendId,
      name: friend.remark || friend.nickname,
      avatar: friend.avatar,
      lastMessage: '',
      lastTime: '',
      unreadCount: 0,
    }
    // 如果会话列表里没有，补进去
    const existing = conversations.value.find(
      c => c.type === 'private' && String(c.targetId) === String(friend.friendId),
    )
    if (!existing) {
      conversations.value.unshift(conv)
    }
    selectConversation(existing || conv)
  }

  function openGroupChat(group: Group) {
    const conv: Conversation = {
      type: 'group',
      targetId: group.groupId,
      name: group.name,
      avatar: '',
      lastMessage: '',
      lastTime: group.createTime,
      unreadCount: 0,
    }
    const existing = conversations.value.find(
      c => c.type === 'group' && String(c.targetId) === String(group.groupId),
    )
    if (!existing) {
      conversations.value.unshift(conv)
    }
    selectConversation(existing || conv)
  }

  // ── 选择会话 & 加载历史 ──

  async function selectConversation(conv: Conversation) {
    activeConversation.value = conv
    conv.unreadCount = 0
    loading.value = true
    try {
      if (conv.type === 'private') {
        messages.value = await msgApi.getPrivateHistory(conv.targetId)
      } else {
        messages.value = await msgApi.getGroupHistory(conv.targetId)
      }
    } finally {
      loading.value = false
    }
  }

  // ── 发送消息（通过 WS） ──

  function sendMessage(
    chatType: 1 | 2,
    toId: number | string,
    messageType: number,
    contentObj: TextContent | ImageContent | FileContent,
  ) {
    if (!isConnected()) {
      ElMessage.warning('未连接到服务器，正在重连...')
      initWs()
      return
    }
    // 立即添加到本地消息列表（乐观更新）
    const myId = localStorage.getItem('minichat_user_id') || ''
    const tempMsg: Message = {
      id: 'temp_' + Date.now(),
      fromId: myId,
      toId,
      messageType,
      content: contentObj,
      createTime: new Date().toISOString(),
    }
    messages.value.push(tempMsg)

    // 更新会话预览
    updateConversationPreview(tempMsg)

    // 后端 SendMessageReq.content 期望纯字符串:
    //   文本 → 纯文字,  图片/视频/语音 → URL,  文件 → URL/文件名
    let rawContent: string
    if ('text' in contentObj) {
      rawContent = (contentObj as TextContent).text
    } else if ('url' in contentObj) {
      rawContent = (contentObj as ImageContent).url
    } else {
      rawContent = ''
    }

    sendWsMessage({
      chatType,
      toId,
      messageType,
      content: rawContent,
    })
  }

  // ── 好友 ──

  async function loadFriends() {
    friends.value = await friendApi.getFriends()
  }

  async function sendFriendRequest(toId: number | string, remark?: string) {
    await friendApi.sendFriendRequest(toId, remark)
    ElMessage.success('好友请求已发送')
  }

  async function deleteFriend(friendId: number | string) {
    await friendApi.deleteFriend(friendId)
    friends.value = friends.value.filter(f => f.friendId !== friendId)
    conversations.value = conversations.value.filter(c =>
      !(c.type === 'private' && String(c.targetId) === String(friendId)),
    )
  }

  async function blockFriend(friendId: number | string) {
    await friendApi.blockFriend(friendId)
    ElMessage.success('已处理')
  }

  // ── 好友请求 ──

  async function loadFriendRequests() {
    friendRequests.value = await friendApi.getFriendRequests()
  }

  async function acceptFriendRequest(id: number | string) {
    await friendApi.acceptFriendRequest(id)
    ElMessage.success('已同意好友请求')
    await loadFriendRequests()
    await loadConversations()   // 内部会 reload 好友列表并重建会话
  }

  async function rejectFriendRequest(id: number | string) {
    await friendApi.rejectFriendRequest(id)
    ElMessage.success('已拒绝好友请求')
    await loadFriendRequests()
  }

  // ── 群组 ──

  async function loadGroups() {
    groups.value = await groupApi.getGroups()
  }

  async function createGroup(name: string, memberIds: (number | string)[]) {
    await groupApi.createGroup({ name, memberIds })
    ElMessage.success('群组已创建')
    await loadGroups()
    await loadConversations()
  }

  async function leaveGroup(groupId: number | string) {
    await groupApi.leaveGroup(groupId)
    groups.value = groups.value.filter(g => g.groupId !== groupId)
    conversations.value = conversations.value.filter(c =>
      !(c.type === 'group' && String(c.targetId) === String(groupId)),
    )
    if (activeConversation.value?.targetId === groupId) {
      activeConversation.value = null
      messages.value = []
    }
  }

  return {
    conversations, activeConversation, messages, friends, friendRequests, groups, loading,
    initWs, closeWs,
    loadConversations, selectConversation, openFriendChat, openGroupChat, sendMessage,
    loadFriends, sendFriendRequest, deleteFriend, blockFriend,
    loadFriendRequests, acceptFriendRequest, rejectFriendRequest,
    loadGroups, createGroup, leaveGroup,
  }
})