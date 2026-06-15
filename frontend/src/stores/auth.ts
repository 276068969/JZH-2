import { defineStore } from 'pinia'
import { ref } from 'vue'
import { post } from '@/utils/request'
import router from '@/router'

export interface UserInfo {
  id: number
  username: string
  realName: string
  role: string
  roles: string[]
}

export interface LoginResult {
  userInfo: UserInfo
  warning?: string
}

function safeParseJSON<T>(value: string | null, defaultValue: T): T {
  if (value === null || value === undefined || value === ''
      || value === 'null' || value === 'undefined') {
    return defaultValue
  }
  try {
    const parsed = JSON.parse(value)
    if (parsed === null || parsed === undefined) {
      return defaultValue
    }
    return parsed as T
  } catch {
    console.warn('localStorage 缓存数据损坏，已重置:', value)
    return defaultValue
  }
}

function isValidUserInfo(data: any): data is UserInfo {
  return data
    && typeof data.id === 'number'
    && typeof data.username === 'string'
    && typeof data.role === 'string'
    && Array.isArray(data.roles)
}

export const useAuthStore = defineStore('auth', () => {
  const token = ref<string>(localStorage.getItem('token') || '')

  const rawUserInfo = safeParseJSON<UserInfo | null>(
    localStorage.getItem('userInfo'),
    null
  )
  const userInfo = ref<UserInfo | null>(
    isValidUserInfo(rawUserInfo) ? rawUserInfo : null
  )

  if (!userInfo.value && token.value) {
    console.warn('检测到 token 存在但 userInfo 损坏，已自动清理脏缓存')
    token.value = ''
    localStorage.removeItem('token')
    localStorage.removeItem('userInfo')
  }

  function setToken(val: string) {
    token.value = val
    localStorage.setItem('token', val)
  }

  function setUserInfo(info: UserInfo) {
    userInfo.value = info
    localStorage.setItem('userInfo', JSON.stringify(info))
  }

  function hasRole(role: string): boolean {
    return userInfo.value?.roles?.includes(role) ?? false
  }

  function hasAnyRole(roles: string[]): boolean {
    return roles.some((r) => userInfo.value?.roles?.includes(r)) ?? false
  }

  async function login(username: string, password: string): Promise<LoginResult> {
    const res = await post('/auth/login', { username, password })
    const data = res.data.data

    if (!data || !data.token || !data.username) {
      throw new Error('登录响应数据异常')
    }

    const roles = Array.isArray(data.roles) && data.roles.length > 0
      ? data.roles
      : (data.role ? [data.role] : ['VIEWER'])

    setToken(data.token)
    const info: UserInfo = {
      id: data.id ?? 0,
      username: data.username,
      realName: data.realName || data.username,
      role: data.role || 'VIEWER',
      roles: roles
    }
    setUserInfo(info)
    return {
      userInfo: info,
      warning: data.warning || undefined
    }
  }

  function logout() {
    token.value = ''
    userInfo.value = null
    localStorage.removeItem('token')
    localStorage.removeItem('userInfo')
    router.push('/login')
  }

  return { token, userInfo, setToken, setUserInfo, login, logout, hasRole, hasAnyRole }
})