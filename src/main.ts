/* 导入主样式 */
import '@/styles/main.scss'
import { createApp } from 'vue'
/* 导入 ElementPlus 依赖 */
import ElementPlus from 'element-plus'
/* 导入国际化文件 */
import zhCn from 'element-plus/es/locale/lang/zh-cn'
import { createPinia } from 'pinia'
/* 导⼊ Pinia 缓存插件 */
import piniaPluginPersist from '../node_modules/pinia-plugin-persist'
import App from './App.vue'
/* 导入路由表实例 */
import router from './router'
/* 导入导航守卫 */
import '@/router/permission'
/* 导入注册图标的工具函数 */
import { useElIcon } from './utils/globalIcon'
const app = createApp(App)
/* 加载 ElementPlus 第三方组件库 同时加载中文国际化文件 */
app.use(ElementPlus, {
    locale: zhCn,
})
/* 调用工具函数 传入 Vue 实例 app 注册所有的图标 */
useElIcon(app)
/* 创建状态管理库实例 */
const store = createPinia()
/* 状态管理库加载缓存插件 */
store.use(piniaPluginPersist)
/* Vue实例加载 Pinia状态管理库 */
app.use(store)
app.use(router)
app.mount('#app')
