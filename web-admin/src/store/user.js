import { defineStore } from 'pinia'
import { ref } from 'vue'
import { login as loginApi, logout as logoutApi, currentUser as currentUserApi } from '@/api/auth'

export const useUserStore = defineStore('user', () => {
  // 1. 从 localStorage 恢复 token
  const token = ref(localStorage.getItem('token') || '')

  // 2. 从 localStorage 恢复 userInfo
  const userInfo = ref(
      localStorage.getItem('userInfo') ? JSON.parse(localStorage.getItem('userInfo')) : null
  )

  // 3. 从 localStorage 恢复 permissions (Set 集合)
  const permissions = ref(
      new Set(localStorage.getItem('permissions') ? JSON.parse(localStorage.getItem('permissions')) : [])
  )

  // 4. 【核心修复】从 localStorage 恢复 menus 菜单列表
  const menus = ref(
      localStorage.getItem('menus') ? JSON.parse(localStorage.getItem('menus')) : []
  )

  const setToken = (val) => {
    token.value = val
    localStorage.setItem('token', val)
  }

  // 退出或清空权限时，同步清理所有的本地缓存
  const clearToken = () => {
    token.value = ''
    localStorage.removeItem('token')
    localStorage.removeItem('userInfo')
    localStorage.removeItem('permissions')
    localStorage.removeItem('menus')
  }

  const loginAction = async (username, password, captchaKey, captchaCode) => {
    const res = await loginApi({ username, password, captchaKey, captchaCode })
    setToken(res.data.token)

    // 缓存用户信息
    userInfo.value = res.data.userInfo
    localStorage.setItem('userInfo', JSON.stringify(res.data.userInfo || {}))

    // 缓存权限点
    const perms = res.data.permissions || []
    permissions.value = new Set(perms)
    localStorage.setItem('permissions', JSON.stringify(perms))

    // 【核心修复】持久化缓存菜单列表，防止刷新后丢失
    const menuList = res.data.menus || []
    menus.value = menuList
    localStorage.setItem('menus', JSON.stringify(menuList))

    return res
  }

  const fetchCurrentUser = async () => {
    const res = await currentUserApi()
    userInfo.value = res.data
    localStorage.setItem('userInfo', JSON.stringify(res.data || {}))
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