import { get, post } from '@/utils/request'
import type { Message } from '@/types'
import type { UploadVO } from '@/api/ws'

/** 获取私聊历史 */
export function getPrivateHistory(targetId: number | string, startTime?: string, endTime?: string) {
  return get<Message[]>('/message/history/private', {
    params: { targetId, startTime, endTime },
  })
}

/** 获取群聊历史 */
export function getGroupHistory(targetId: number | string, startTime?: string, endTime?: string) {
  return get<Message[]>('/message/history/group', {
    params: { targetId, startTime, endTime },
  })
}

/** 上传文件到 OSS */
export function uploadFile(file: File) {
  const fd = new FormData()
  fd.append('file', file)
  return post<UploadVO>('/oss/upload', fd)
}