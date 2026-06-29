import type { Message, TextContent, ImageContent, FileContent, MessageType } from '@/types'

// ── WS 消息帧 ──

export interface UploadVO {
  url: string
  originalName: string
}

export interface AckMessage {
  messageId: number | string
}

export interface WsFrame {
  type: 0 | 1 | 2 | 3   // 1=发消息  2=ACK  0=收到消息  3=心跳
  data: Record<string, unknown>
}

// ── WS 发送的消息体 ──

export interface SendMessagePayload {
  chatType: 1 | 2     // 1私聊 2群聊
  toId: number | string
  messageType: MessageType
  content: string     // JSON string of TextContent | ImageContent | FileContent
}

// ── WS 连接管理 ──

type MessageHandler = (msg: Message) => void
type HeartbeatHandler = () => void

let ws: WebSocket | null = null
let manualClose = false
let _onMessage: MessageHandler | null = null
let _onHeartbeat: HeartbeatHandler | null = null

function getWsUrl(): string {
  const token = localStorage.getItem('minichat_token')
  const isDev = import.meta.env.DEV
  if (isDev) {
    // 开发环境直连网关
    return `ws://localhost:8080/ws?token=${token}`
  }
  // 生产环境用当前协议 + 域名
  const proto = location.protocol === 'https:' ? 'wss:' : 'ws:'
  return `${proto}//${location.host}/ws?token=${token}`
}

export function connect(onMessage: MessageHandler, onHeartbeat?: HeartbeatHandler) {
  if (ws && ws.readyState < 2) return // 已连接或正在连接

  manualClose = false
  _onMessage = onMessage
  _onHeartbeat = onHeartbeat ?? null

  const url = getWsUrl()
  ws = new WebSocket(url)

  ws.onopen = () => {
    console.log('[WS] 已连接')
    // 启动心跳
    startHeartbeat()
  }

  ws.onmessage = (event) => {
    try {
      const frame: WsFrame = JSON.parse(event.data)
      switch (frame.type) {
        case 0: // 收到新消息
          _onMessage?.(frame.data as unknown as Message)
          break
        case 2: // ACK
          console.log('[WS] ACK:', frame.data.messageId)
          break
        case 3: // 心跳响应
          break
      }
    } catch {
      console.warn('[WS] 无法解析消息:', event.data)
    }
  }

  ws.onclose = () => {
    console.log('[WS] 断开')
    stopHeartbeat()
    ws = null
    if (!manualClose) {
      // 意外断开，3 秒后重连
      setTimeout(() => connect(_onMessage!, _onHeartbeat ?? undefined), 3000)
    }
  }

  ws.onerror = (e) => {
    console.error('[WS] 错误:', e)
  }
}

export function disconnect() {
  manualClose = true
  stopHeartbeat()
  ws?.close()
  ws = null
}

let heartbeatTimer: ReturnType<typeof setInterval> | null = null

function startHeartbeat() {
  stopHeartbeat()
  heartbeatTimer = setInterval(() => {
    if (ws?.readyState === WebSocket.OPEN) {
      ws.send(JSON.stringify({ type: 3, data: {} }))
      _onHeartbeat?.()
    }
  }, 30_000) // 30秒一次心跳
}

function stopHeartbeat() {
  if (heartbeatTimer) {
    clearInterval(heartbeatTimer)
    heartbeatTimer = null
  }
}

/** 通过 WS 发送聊天消息 */
export function sendWsMessage(payload: SendMessagePayload) {
  if (!ws || ws.readyState !== WebSocket.OPEN) {
    console.warn('[WS] 未连接，无法发送消息')
    return
  }
  ws.send(JSON.stringify({
    type: 1,
    data: payload,
  }))
}

/** 判断 WS 是否已连接 */
export function isConnected(): boolean {
  return ws?.readyState === WebSocket.OPEN
}