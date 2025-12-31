import { createApp } from 'vue'
import { createPinia } from 'pinia'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import 'element-plus/theme-chalk/dark/css-vars.css'
import 'highlight.js/styles/github-dark.css'

import App from './App.vue'
import router from './router'

// 全域樣式
import '@/styles/base.scss'
import '@/styles/layout.scss'

const app = createApp(App)

app.use(createPinia())
app.use(router)
app.use(ElementPlus, { size: 'default' })

app.mount('#app')
