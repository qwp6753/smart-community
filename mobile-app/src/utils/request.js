const BASE_URL = 'http://localhost:8080/api'

// 全局加载计数器，防止并发请求导致loading闪烁
let loadingCount = 0

const showLoading = () => {
  if (loadingCount === 0) {
    uni.showLoading({ title: '加载中...', mask: true })
  }
  loadingCount++
}

const hideLoading = () => {
  loadingCount--
  if (loadingCount <= 0) {
    loadingCount = 0
    uni.hideLoading()
  }
}

const request = (options) => {
  return new Promise((resolve, reject) => {
    // 非静默请求显示loading
    if (!options.silent) {
      showLoading()
    }

    const token = uni.getStorageSync('token')
    uni.request({
      url: BASE_URL + options.url,
      method: options.method || 'GET',
      data: options.data,
      timeout: 15000,
      header: {
        'Authorization': token ? `Bearer ${token}` : '',
        'Content-Type': 'application/json'
      },
      success: (res) => {
        if (!options.silent) hideLoading()

        // 处理401未授权
        if (res.statusCode === 401) {
          uni.removeStorageSync('token')
          uni.removeStorageSync('userInfo')
          uni.reLaunch({ url: '/pages/login/index' })
          reject(new Error('登录已过期，请重新登录'))
          return
        }

        const data = res.data
        if (data.code === 200) {
          resolve(data)
        } else {
          uni.showToast({ title: data.message || '请求失败', icon: 'none', duration: 2000 })
          reject(new Error(data.message || '请求失败'))
        }
      },
      fail: (err) => {
        if (!options.silent) hideLoading()

        if (err.errMsg && err.errMsg.includes('timeout')) {
          uni.showToast({ title: '请求超时，请重试', icon: 'none' })
        } else {
          uni.showToast({ title: '网络错误，请检查网络', icon: 'none' })
        }
        reject(err)
      }
    })
  })
}

// 便捷方法
export const get = (url, params, silent = false) => request({ url, data: params, method: 'GET', silent })
export const post = (url, data, silent = false) => request({ url, data, method: 'POST', silent })
export const put = (url, data, silent = false) => request({ url, data, method: 'PUT', silent })
export const del = (url, silent = false) => request({ url, method: 'DELETE', silent })

export default request
