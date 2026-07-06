import { defineStore } from 'pinia'
import { ref } from 'vue'
import { login as loginApi, logout as logoutApi, currentUser as currentUserApi } from '@/api/auth'

export const useUserStore = defineStore('user', () => {
  const token = ref(localStorage.getItem('token') || '')
  const userInfo = ref(null)
  const permissions = ref(new Set())
  const menus = ref([])

  const setToken = (val) => {
    token.value = val
    localStorage.setItem('token', val)
  }

  const clearToken = () => {
    token.value = ''
    localStorage.removeItem('token')
  }

  const loginAction = async (username, password, captchaKey, captchaCode) => {
    const res = await loginApi({ username, password, captchaKey, captchaCode })
    setToken(res.data.token)
    userInfo.value = res.data.userInfo
    permissions.value = new Set(res.data.permissions || [])
    menus.value = res.data.menus || []
    return res
  }

  const fetchCurrentUser = async () => {
    const res = await currentUserApi()
    userInfo.value = res.data
    return res
  }

  const hasPermission = (perm) => {
    return permissions.value.has(perm)
  }

  const logout = async () => {
    try { await logoutApi() } catch (e) { /* 网络异常不阻塞本地清理 */ }
    clearToken()
    userInfo.value = null
    permissions.value = new Set()
    menus.value = []
  }

  return {
    token,
    userInfo,
    permissions,
    menus,
    setToken,
    clearToken,
    loginAction,
    fetchCurrentUser,
    hasPermission,
    logout
  }
})
