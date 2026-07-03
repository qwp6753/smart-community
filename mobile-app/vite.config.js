import { defineConfig } from 'vite'
import uniModule from '@dcloudio/vite-plugin-uni'

// ESM 导入 CJS 包时需要通过 .default 获取真正的导出函数
const uni = uniModule.default || uniModule

export default defineConfig({
  plugins: [uni()],
  // 👇 新增下面这块 css 配置，用来静音 Sass 的警告
  css: {
    preprocessorOptions: {
      scss: {
        api: 'modern-compiler', // 尝试使用现代编译器 API（解决 legacy-js-api 警告）
        silenceDeprecations: ['import', 'legacy-js-api'] // 静音指定的废弃警告
      }
    }
  },
  server: {
    port: 8088,
    host: '0.0.0.0',
    open: true
  }
})