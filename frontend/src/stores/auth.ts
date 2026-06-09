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

export const useAuthStore = defineStore('auth', () => {
  const token = ref<string>(localStorage.getItem('token') || '')
  const userInfo = ref<UserInfo | null>(
    JSON.parse(localStorage.getItem('userInfo') || 'null')
  )

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

  async function login(username: string, password: string) {
    const res = await post('/auth/login', { username, password })
    const { token: jwt, user } = res.data
    setToken(jwt)
    setUserInfo(user)
    return res
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