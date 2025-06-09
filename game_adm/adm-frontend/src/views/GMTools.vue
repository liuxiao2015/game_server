<template>
  <div class="gm-tools">
    <el-tabs v-model="activeTab" type="card">
      <!-- 玩家管理 -->
      <el-tab-pane label="玩家管理" name="player">
        <el-card>
          <template #header>
            <span>玩家查询与管理</span>
          </template>
          
          <!-- 玩家搜索 -->
          <el-form :inline="true" class="search-form">
            <el-form-item label="搜索玩家">
              <el-input
                v-model="playerSearchKeyword"
                placeholder="输入玩家ID、用户名或邮箱"
                clearable
                style="width: 300px;"
              />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="searchPlayer">搜索</el-button>
            </el-form-item>
          </el-form>
          
          <!-- 玩家信息 -->
          <div v-if="playerInfo" class="player-info">
            <el-descriptions title="玩家信息" :column="2" border>
              <el-descriptions-item label="玩家ID">{{ playerInfo.playerId }}</el-descriptions-item>
              <el-descriptions-item label="用户名">{{ playerInfo.username }}</el-descriptions-item>
              <el-descriptions-item label="等级">{{ playerInfo.level }}</el-descriptions-item>
              <el-descriptions-item label="状态">
                <el-tag :type="playerInfo.status === 'ACTIVE' ? 'success' : 'danger'">
                  {{ playerInfo.status }}
                </el-tag>
              </el-descriptions-item>
              <el-descriptions-item label="最后登录">{{ playerInfo.lastLoginTime }}</el-descriptions-item>
            </el-descriptions>
            
            <!-- 操作按钮 -->
            <div class="player-actions" style="margin-top: 20px;">
              <el-button type="warning" @click="showBanDialog">封禁玩家</el-button>
              <el-button type="danger" @click="kickPlayer">踢出玩家</el-button>
              <el-button type="primary" @click="showItemDialog">发放物品</el-button>
            </div>
          </div>
        </el-card>
      </el-tab-pane>
      
      <!-- 邮件系统 -->
      <el-tab-pane label="邮件系统" name="mail">
        <el-card>
          <template #header>
            <span>邮件发送</span>
          </template>
          
          <el-form :model="mailForm" label-width="100px">
            <el-form-item label="收件人">
              <el-radio-group v-model="mailForm.recipientType">
                <el-radio label="all">全服玩家</el-radio>
                <el-radio label="specific">指定玩家</el-radio>
              </el-radio-group>
            </el-form-item>
            
            <el-form-item v-if="mailForm.recipientType === 'specific'" label="玩家ID">
              <el-input v-model="mailForm.specificIds" placeholder="输入玩家ID，多个用逗号分隔" />
            </el-form-item>
            
            <el-form-item label="邮件标题">
              <el-input v-model="mailForm.title" placeholder="输入邮件标题" />
            </el-form-item>
            
            <el-form-item label="邮件内容">
              <el-input
                v-model="mailForm.content"
                type="textarea"
                :rows="4"
                placeholder="输入邮件内容"
              />
            </el-form-item>
            
            <el-form-item label="附件">
              <el-button @click="showAttachmentDialog">添加附件</el-button>
              <el-tag
                v-for="(item, index) in mailForm.attachments"
                :key="index"
                closable
                @close="removeAttachment(index)"
                style="margin-left: 10px;"
              >
                物品{{ item.itemId }} x{{ item.quantity }}
              </el-tag>
            </el-form-item>
            
            <el-form-item>
              <el-button type="primary" @click="sendMail">发送邮件</el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </el-tab-pane>
      
      <!-- 服务器控制 -->
      <el-tab-pane label="服务器控制" name="server">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-card>
              <template #header>
                <span>系统广播</span>
              </template>
              
              <el-form>
                <el-form-item>
                  <el-input
                    v-model="broadcastMessage"
                    type="textarea"
                    :rows="3"
                    placeholder="输入广播消息"
                  />
                </el-form-item>
                <el-form-item>
                  <el-button type="primary" @click="sendBroadcast">发送广播</el-button>
                </el-form-item>
              </el-form>
            </el-card>
          </el-col>
          
          <el-col :span="12">
            <el-card>
              <template #header>
                <span>配置管理</span>
              </template>
              
              <el-form>
                <el-form-item label="配置类型">
                  <el-select v-model="configType" placeholder="选择配置类型">
                    <el-option label="游戏配置" value="game" />
                    <el-option label="物品配置" value="item" />
                    <el-option label="任务配置" value="task" />
                  </el-select>
                </el-form-item>
                <el-form-item>
                  <el-button type="warning" @click="reloadConfig">重载配置</el-button>
                </el-form-item>
              </el-form>
            </el-card>
          </el-col>
        </el-row>
      </el-tab-pane>
    </el-tabs>
    
    <!-- 封禁对话框 -->
    <el-dialog v-model="banDialogVisible" title="封禁玩家" width="400px">
      <el-form :model="banForm" label-width="80px">
        <el-form-item label="封禁原因">
          <el-input v-model="banForm.reason" placeholder="输入封禁原因" />
        </el-form-item>
        <el-form-item label="封禁时长">
          <el-select v-model="banForm.duration">
            <el-option label="1小时" :value="1" />
            <el-option label="24小时" :value="24" />
            <el-option label="7天" :value="168" />
            <el-option label="永久" :value="-1" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="banDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmBan">确认封禁</el-button>
      </template>
    </el-dialog>
    
    <!-- 物品发放对话框 -->
    <el-dialog v-model="itemDialogVisible" title="发放物品" width="400px">
      <el-form :model="itemForm" label-width="80px">
        <el-form-item label="物品ID">
          <el-input v-model="itemForm.itemId" placeholder="输入物品ID" />
        </el-form-item>
        <el-form-item label="数量">
          <el-input-number v-model="itemForm.quantity" :min="1" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="itemDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmSendItem">确认发放</el-button>
      </template>
    </el-dialog>
    
    <!-- 附件添加对话框 -->
    <el-dialog v-model="attachmentDialogVisible" title="添加附件" width="400px">
      <el-form :model="attachmentForm" label-width="80px">
        <el-form-item label="物品ID">
          <el-input v-model="attachmentForm.itemId" placeholder="输入物品ID" />
        </el-form-item>
        <el-form-item label="数量">
          <el-input-number v-model="attachmentForm.quantity" :min="1" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="attachmentDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="addAttachment">添加</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import { ref, reactive } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { gmApi } from '../api/gm'

export default {
  name: 'GMTools',
  setup() {
    const activeTab = ref('player')
    const playerSearchKeyword = ref('')
    const playerInfo = ref(null)
    const broadcastMessage = ref('')
    const configType = ref('')
    
    // 对话框状态
    const banDialogVisible = ref(false)
    const itemDialogVisible = ref(false)
    const attachmentDialogVisible = ref(false)
    
    // 表单数据
    const banForm = reactive({
      reason: '',
      duration: 24
    })
    
    const itemForm = reactive({
      itemId: '',
      quantity: 1
    })
    
    const mailForm = reactive({
      recipientType: 'all',
      specificIds: '',
      title: '',
      content: '',
      attachments: []
    })
    
    const attachmentForm = reactive({
      itemId: '',
      quantity: 1
    })
    
    // 搜索玩家
    const searchPlayer = async () => {
      if (!playerSearchKeyword.value.trim()) {
        ElMessage.warning('请输入搜索关键词')
        return
      }
      
      try {
        // TODO: 调用实际的API
        // const result = await gmApi.queryPlayer(playerSearchKeyword.value)
        // playerInfo.value = result
        
        // 模拟数据
        playerInfo.value = {
          playerId: 12345,
          username: 'test_player',
          level: 50,
          status: 'ACTIVE',
          lastLoginTime: '2025-06-08 10:30:00'
        }
        
        ElMessage.success('查询成功')
      } catch (error) {
        ElMessage.error('查询失败: ' + error.message)
      }
    }
    
    // 显示封禁对话框
    const showBanDialog = () => {
      banDialogVisible.value = true
    }
    
    // 确认封禁
    const confirmBan = async () => {
      try {
        // TODO: 调用实际的API
        // await gmApi.banPlayer(playerInfo.value.playerId, banForm.reason, banForm.duration)
        
        ElMessage.success('玩家封禁成功')
        banDialogVisible.value = false
      } catch (error) {
        ElMessage.error('封禁失败: ' + error.message)
      }
    }
    
    // 踢出玩家
    const kickPlayer = async () => {
      try {
        await ElMessageBox.confirm('确认要踢出该玩家吗？', '确认操作', {
          confirmButtonText: '确认',
          cancelButtonText: '取消',
          type: 'warning'
        })
        
        // TODO: 调用实际的API
        // await gmApi.kickPlayer(playerInfo.value.playerId)
        
        ElMessage.success('玩家已被踢出')
      } catch (error) {
        if (error !== 'cancel') {
          ElMessage.error('踢出失败: ' + error.message)
        }
      }
    }
    
    // 显示物品对话框
    const showItemDialog = () => {
      itemDialogVisible.value = true
    }
    
    // 确认发放物品
    const confirmSendItem = async () => {
      try {
        const items = [{
          itemId: itemForm.itemId,
          quantity: itemForm.quantity
        }]
        
        // TODO: 调用实际的API
        // await gmApi.sendItems(playerInfo.value.playerId, items)
        
        ElMessage.success('物品发放成功')
        itemDialogVisible.value = false
      } catch (error) {
        ElMessage.error('发放失败: ' + error.message)
      }
    }
    
    // 发送邮件
    const sendMail = async () => {
      try {
        // TODO: 调用实际的API
        // await gmApi.sendMail(mailForm)
        
        ElMessage.success('邮件发送成功')
        
        // 重置表单
        Object.assign(mailForm, {
          recipientType: 'all',
          specificIds: '',
          title: '',
          content: '',
          attachments: []
        })
      } catch (error) {
        ElMessage.error('发送失败: ' + error.message)
      }
    }
    
    // 显示附件对话框
    const showAttachmentDialog = () => {
      attachmentDialogVisible.value = true
    }
    
    // 添加附件
    const addAttachment = () => {
      mailForm.attachments.push({
        itemId: attachmentForm.itemId,
        quantity: attachmentForm.quantity
      })
      
      // 重置表单
      attachmentForm.itemId = ''
      attachmentForm.quantity = 1
      attachmentDialogVisible.value = false
    }
    
    // 移除附件
    const removeAttachment = (index) => {
      mailForm.attachments.splice(index, 1)
    }
    
    // 发送广播
    const sendBroadcast = async () => {
      if (!broadcastMessage.value.trim()) {
        ElMessage.warning('请输入广播消息')
        return
      }
      
      try {
        // TODO: 调用实际的API
        // await gmApi.broadcast(broadcastMessage.value)
        
        ElMessage.success('广播发送成功')
        broadcastMessage.value = ''
      } catch (error) {
        ElMessage.error('广播失败: ' + error.message)
      }
    }
    
    // 重载配置
    const reloadConfig = async () => {
      if (!configType.value) {
        ElMessage.warning('请选择配置类型')
        return
      }
      
      try {
        // TODO: 调用实际的API
        // await gmApi.reloadConfig(configType.value)
        
        ElMessage.success('配置重载成功')
      } catch (error) {
        ElMessage.error('重载失败: ' + error.message)
      }
    }
    
    return {
      activeTab,
      playerSearchKeyword,
      playerInfo,
      broadcastMessage,
      configType,
      banDialogVisible,
      itemDialogVisible,
      attachmentDialogVisible,
      banForm,
      itemForm,
      mailForm,
      attachmentForm,
      searchPlayer,
      showBanDialog,
      confirmBan,
      kickPlayer,
      showItemDialog,
      confirmSendItem,
      sendMail,
      showAttachmentDialog,
      addAttachment,
      removeAttachment,
      sendBroadcast,
      reloadConfig
    }
  }
}
</script>

<style scoped>
.gm-tools {
  height: 100%;
}

.search-form {
  margin-bottom: 20px;
}

.player-info {
  margin-top: 20px;
}

.player-actions {
  text-align: right;
}

.player-actions .el-button {
  margin-left: 10px;
}
</style>