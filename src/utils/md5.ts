/* 此方法用来对密码进行 md5 加密,防止明文传送 */
/* 导入 MD5 */
import { Md5 } from "ts-md5";
/* 此函数传入未加密字符串获取加密后的字符串 */
export const getMd5Info = (oldPass:string) => {
    /* 创建一个 md5 对象 */
    const md5: any = new Md5()
    /* 传入原先的密码进行加密,返回一个加密后的新密码 */
    return md5.appendAsciiStr(oldPass).end()
}