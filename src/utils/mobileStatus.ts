/**
 * 移动端共用的格式化 / 状态映射工具
 */

/** 订单状态：0-待调度 1-已调度 2-配送中 3-已完成 4-已取消 */
export const ORDER_STATUS_TEXT: Record<number, string> = {
  0: '待调度',
  1: '已调度',
  2: '配送中',
  3: '已完成',
  4: '已取消'
}

/** 订单状态对应的标签样式 */
export const ORDER_STATUS_CLASS: Record<number, string> = {
  0: 'm-tag--wait',
  1: 'm-tag--wait',
  2: 'm-tag--run',
  3: 'm-tag--done',
  4: 'm-tag--cancel'
}

/** 配送状态：0-待取货 1-配送中 2-已送达 3-异常 */
export const DELIVERY_STATUS_TEXT: Record<number, string> = {
  0: '待取货',
  1: '配送中',
  2: '已送达',
  3: '异常'
}

/** 配送状态对应的标签样式 */
export const DELIVERY_STATUS_CLASS: Record<number, string> = {
  0: 'm-tag--wait',
  1: 'm-tag--run',
  2: 'm-tag--done',
  3: 'm-tag--error'
}

const pad = (value: number) => String(value).padStart(2, '0')

/**
 * 解析后端返回的时间字符串
 * 兼容 "2026-11-20 15:00:00"、"2026-11-20T15:00:00" 与时间戳
 */
export const parseTime = (value?: string | number | null): Date | null => {
  if (value === undefined || value === null || value === '') {
    return null
  }
  if (typeof value === 'number') {
    return new Date(value)
  }
  const normalized = value.includes('T') ? value : value.replace(' ', 'T')
  const date = new Date(normalized)
  return Number.isNaN(date.getTime()) ? null : date
}

/** 格式化为 "MM-DD HH:mm"，full 为 true 时带上年份 */
export const formatTime = (value?: string | number | null, full = false): string => {
  const date = parseTime(value)
  if (!date) {
    return '-'
  }
  const datePart = full
    ? `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())}`
    : `${pad(date.getMonth() + 1)}-${pad(date.getDate())}`
  return `${datePart} ${pad(date.getHours())}:${pad(date.getMinutes())}`
}

/** 距离期望送达时间的描述，如 "还有 32 分钟" */
export const expectedHint = (value?: string | null): string => {
  const date = parseTime(value)
  if (!date) {
    return ''
  }
  const diffMinutes = Math.round((date.getTime() - Date.now()) / 60000)
  if (diffMinutes < -60) {
    return `已超时 ${Math.abs(Math.round(diffMinutes / 60))} 小时`
  }
  if (diffMinutes < 0) {
    return `已超时 ${Math.abs(diffMinutes)} 分钟`
  }
  if (diffMinutes < 60) {
    return `还有 ${diffMinutes} 分钟`
  }
  return `还有 ${Math.round(diffMinutes / 60)} 小时`
}
