/* 此文件用来封装 ElementPlus 图标文件 */
import { type App } from 'vue'

/* 导入全局图标文件 */
import * as ElementPlusIconsVue from '@element-plus/icons-vue'

/* 此函数传入 Vue 实例从而全局加载 ElementPlus 图标 */
export const useElIcon = (app:App) => {
    /* 迭代从而一个一个组合所有图标 */
    for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
        app.component(key, component)
    }
}