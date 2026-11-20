declare interface RegUser {
    /* 主键 */
    id?: string
    /* 用户姓名 */
    username: string
    /* 用户密码 */
    password: string
    /* 真实姓名 */
    realname: string
    /* 邮箱地址 */
    email: string
    /* 电话号码 */
    phone: string
    /* 性别 */
    gender: number
    /* 权限 */
    role: number
    /* 上传头像的路径 */
    path: string
    /* 如果是批量上传多张图片则这里是一个数组,数组中保存着多个路径 */
    // paths:Array<string>
}