/**
 * 高德地图 JS API 全局声明
 *
 * SDK 通过 index.html 中的 script 标签加载（window.AMap），
 * 这里只做最小声明，避免各页面直接使用 AMap 时出现「找不到名称」的类型错误。
 */
declare const AMap: any

interface Window {
  AMap?: any
  _AMapSecurityConfig?: {
    securityJsCode?: string
  }
}
