import request from '../utils/request'

export const statisticsApi = {
  // 获取实时在线统计
  getOnlineStatistics() {
    return request.get('/adm/statistics/online')
  },
  
  // 获取活跃用户统计
  getActiveUsers(startDate, endDate) {
    return request.get('/adm/statistics/active-users', {
      params: { startDate, endDate }
    })
  },
  
  // 获取收入统计
  getRevenue(startDate, endDate) {
    return request.get('/adm/statistics/revenue', {
      params: { startDate, endDate }
    })
  },
  
  // 获取留存分析
  getRetention(days) {
    return request.get(`/adm/statistics/retention/${days}`)
  },
  
  // 获取付费分析
  getPaymentAnalysis() {
    return request.get('/adm/statistics/payment-analysis')
  }
}