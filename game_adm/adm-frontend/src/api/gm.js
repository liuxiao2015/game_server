import request from '../utils/request'

export const gmApi = {
  // 玩家管理
  queryPlayer(keyword) {
    return request.get('/adm/gm/players/search', {
      params: { keyword }
    })
  },
  
  banPlayer(playerId, reason, durationHours) {
    return request.post(`/adm/gm/players/${playerId}/ban`, null, {
      params: { reason, durationHours }
    })
  },
  
  kickPlayer(playerId) {
    return request.post(`/adm/gm/players/${playerId}/kick`)
  },
  
  // 物品发放
  sendItems(playerId, items) {
    return request.post(`/adm/gm/players/${playerId}/items`, items)
  },
  
  // 邮件系统
  sendMail(mailRequest) {
    return request.post('/adm/gm/mail', mailRequest)
  },
  
  // 服务器控制
  broadcast(message) {
    return request.post('/adm/gm/broadcast', null, {
      params: { message }
    })
  },
  
  reloadConfig(configType) {
    return request.post('/adm/gm/config/reload', null, {
      params: { configType }
    })
  },
  
  executeScript(script) {
    return request.post('/adm/gm/script/execute', null, {
      params: { script }
    })
  }
}