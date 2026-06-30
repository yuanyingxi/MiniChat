import { post } from '@/utils/request'

export function login(phone: string, password: string) {
  return post<string>('/auth/login', { phone, password })
}

export function sendSmsCode(phone: string) {
  return post<void>('/auth/sms/code', { phone })
}

export function register(phone: string, smsCode: string, password: string) {
  return post<string>('/auth/register', { phone, smsCode, password })
}

export function logout() {
  return post<void>('/auth/logout')
}
