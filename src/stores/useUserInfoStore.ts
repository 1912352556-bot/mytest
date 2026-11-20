/* 导入创建仓库函数 */
import { defineStore } from 'pinia'

/* 创建仓库,注意一般以 use 开头 Store 结尾 第一个参数为仓库名 */
export const useUserInfoStore = defineStore('userInfo', {
    /* 初始化状态 类似 data */
    state(){
        return {
            /* 保存用户信息 */
            user:{} as RegUser,
            /* 保存工程信息 */
            
        }
    },
    /* 设置派生属性 类似 computed */
    getters:{},
    /* 设置 actions 类似 methods */
    actions:{
        /* 从浏览器中获取状态信息 */
        getUserInfo(){
            if(this.user.id){
                /* 根据用户的主键从浏览器中获取数据 注意秘钥就是主键 */
                return JSON.parse(localStorage.getItem(this.user.id)||'null')
            }
            return null
        },
        /* 将状态信息保存到浏览器中 */
        setUserInfo(){
            /* 将状态数据转换为 JSON 后保存进 浏览器本地存储器,秘钥就是主键 */
            if(this.user.id) localStorage
            .setItem(this.user.id,JSON.stringify(this.user))
        },
        /* 删除保存在浏览器中的状态信息 */
        removeUserInfo(){
            /* 根据主键删除保存在浏览器本地存储器中的数据 */
            if(this.user.id) localStorage.removeItem(this.user.id)
        },
    },
    /* 开启缓存插件 */
    persist:{
        /* 开启后刷新不再重置状态 */
        enabled:true,
    }
})
