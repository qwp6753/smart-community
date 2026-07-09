<script>
export default {
  onLaunch: function() {
    console.log('智慧小区 App 启动')

    // 检查登录状态
    const token = uni.getStorageSync('token')
    if (!token) {
      // 未登录，保持在登录页（login是第一个页面，自动显示）
      console.log('未登录，跳转登录页')
    }
  },
  onShow: function() {
    console.log('App Show')
  },
  onHide: function() {
    console.log('App Hide')
  }
}
</script>

<style lang="scss">
@import "./uni.scss";
</style>
