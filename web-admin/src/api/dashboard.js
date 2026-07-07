import request from './request'

/** 首页统计数据 */
export const getDashboard = () => request.get('/sys/statistics/dashboard')

/** 出入趋势统计（最近N天） */
export const getRecordTrend = (days = 7) => request.get('/sys/statistics/recordTrend', { params: { days } })

/** 人员类型统计 */
export const getPersonType = () => request.get('/sys/statistics/personType')

/** 小区居民统计 */
export const getCommunityPerson = () => request.get('/sys/statistics/communityPerson')

// 兼容旧版调用
export const getStats = getDashboard
