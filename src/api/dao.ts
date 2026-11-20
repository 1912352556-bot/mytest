/* 导入 自定义axios实例 request */
import request from "@/utils/request";


/* 1.登录 username 和 password 是传递的形参 */
export const login = (username: string, password: string) =>
    request({
        /* 发送异步的完整地址是
            基本地址 + 进阶地址
            baseURL + url
            http://db.etoak.com:9527 + ${PATH}login?username=${username}&password=${password}
        */
        url: `/testUser/login?username=${username}&password=${password}`,
        method: 'get',
    })

/* 2.注册 pojo 这里是个对象内部封装了要注册的八个字段 */
export const register = (pojo: RegUser) =>
    request({
        url: `/testUser/add`,
        method: 'post',
        /* 使用 post 可以发送JSON,这里直接书写对象即可,底层会自动使用
        JSON.stringify(pojo) 转换为 JSON 发送给后端 get方式没有此功能 */
        data: pojo,
    })
