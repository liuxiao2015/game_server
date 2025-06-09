<template>
  <div class="system-management">
    <el-tabs v-model="activeTab" type="card">
      <!-- 用户管理 -->
      <el-tab-pane label="用户管理" name="user">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>后台用户管理</span>
              <el-button type="primary" @click="showAddUserDialog">添加用户</el-button>
            </div>
          </template>
          
          <el-table :data="users" style="width: 100%">
            <el-table-column prop="id" label="ID" width="80" />
            <el-table-column prop="username" label="用户名" width="150" />
            <el-table-column prop="email" label="邮箱" width="200" />
            <el-table-column prop="role" label="角色" width="120">
              <template #default="scope">
                <el-tag :type="getRoleTagType(scope.row.role)">
                  {{ scope.row.role }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="status" label="状态" width="100">
              <template #default="scope">
                <el-tag :type="scope.row.status === 'ACTIVE' ? 'success' : 'danger'">
                  {{ scope.row.status }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="lastLogin" label="最后登录" width="180" />
            <el-table-column label="操作" fixed="right" width="200">
              <template #default="scope">
                <el-button size="small" @click="editUser(scope.row)">编辑</el-button>
                <el-button size="small" type="warning" @click="resetPassword(scope.row)">重置密码</el-button>
                <el-button 
                  size="small" 
                  :type="scope.row.status === 'ACTIVE' ? 'danger' : 'success'"
                  @click="toggleUserStatus(scope.row)"
                >
                  {{ scope.row.status === 'ACTIVE' ? '禁用' : '启用' }}
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-tab-pane>
      
      <!-- 操作日志 -->
      <el-tab-pane label="操作日志" name="log">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>操作日志查询</span>
              <div>
                <el-date-picker
                  v-model="logDateRange"
                  type="datetimerange"
                  range-separator="至"
                  start-placeholder="开始日期"
                  end-placeholder="结束日期"
                  format="YYYY-MM-DD HH:mm:ss"
                  value-format="YYYY-MM-DD HH:mm:ss"
                  style="margin-right: 10px;"
                />
                <el-button type="primary" @click="searchLogs">查询</el-button>
                <el-button @click="exportLogs">导出</el-button>
              </div>
            </div>
          </template>
          
          <el-table :data="operationLogs" style="width: 100%">
            <el-table-column prop="id" label="ID" width="80" />
            <el-table-column prop="operator" label="操作人" width="120" />
            <el-table-column prop="action" label="操作" width="150" />
            <el-table-column prop="resource" label="资源" width="200" />
            <el-table-column prop="ip" label="IP地址" width="150" />
            <el-table-column prop="timestamp" label="操作时间" width="180" />
            <el-table-column prop="result" label="结果" width="100">
              <template #default="scope">
                <el-tag :type="scope.row.result === 'SUCCESS' ? 'success' : 'danger'">
                  {{ scope.row.result }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" fixed="right" width="120">
              <template #default="scope">
                <el-button size="small" @click="viewLogDetail(scope.row)">详情</el-button>
              </template>
            </el-table-column>
          </el-table>
          
          <div class="pagination" style="margin-top: 20px; text-align: right;">
            <el-pagination
              v-model:current-page="logCurrentPage"
              v-model:page-size="logPageSize"
              :page-sizes="[10, 20, 50, 100]"
              :total="logTotal"
              layout="total, sizes, prev, pager, next, jumper"
            />
          </div>
        </el-card>
      </el-tab-pane>
      
      <!-- 系统设置 -->
      <el-tab-pane label="系统设置" name="setting">
        <el-card>
          <template #header>
            <span>系统配置</span>
          </template>
          
          <el-form :model="systemSettings" label-width="150px">
            <el-form-item label="系统名称">
              <el-input v-model="systemSettings.systemName" style="width: 300px;" />
            </el-form-item>
            
            <el-form-item label="登录超时时间">
              <el-input-number 
                v-model="systemSettings.sessionTimeout" 
                :min="10" 
                :max="1440"
                controls-position="right" 
              />
              <span style="margin-left: 10px;">分钟</span>
            </el-form-item>
            
            <el-form-item label="密码策略">
              <el-checkbox-group v-model="systemSettings.passwordPolicy">
                <el-checkbox label="requireUppercase">必须包含大写字母</el-checkbox>
                <el-checkbox label="requireNumbers">必须包含数字</el-checkbox>
                <el-checkbox label="requireSpecialChars">必须包含特殊字符</el-checkbox>
              </el-checkbox-group>
            </el-form-item>
            
            <el-form-item label="最小密码长度">
              <el-input-number 
                v-model="systemSettings.minPasswordLength" 
                :min="6" 
                :max="32"
                controls-position="right" 
              />
            </el-form-item>
            
            <el-form-item label="登录失败锁定">
              <el-switch v-model="systemSettings.enableLoginLock" />
              <span style="margin-left: 10px;">最大失败次数:</span>
              <el-input-number 
                v-model="systemSettings.maxLoginAttempts" 
                :min="3" 
                :max="10"
                :disabled="!systemSettings.enableLoginLock"
                controls-position="right"
                style="margin-left: 10px; width: 120px;"
              />
            </el-form-item>
            
            <el-form-item>
              <el-button type="primary" @click="saveSettings">保存设置</el-button>
              <el-button @click="resetSettings">重置</el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </el-tab-pane>
    </el-tabs>
    
    <!-- 添加/编辑用户对话框 -->
    <el-dialog v-model="userDialogVisible" :title="isEditMode ? '编辑用户' : '添加用户'" width="500px">
      <el-form :model="userForm" :rules="userFormRules" ref="userFormRef" label-width="80px">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="userForm.username" :disabled="isEditMode" />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="userForm.email" />
        </el-form-item>
        <el-form-item label="角色" prop="role">
          <el-select v-model="userForm.role" style="width: 100%;">
            <el-option label="管理员" value="ADMIN" />
            <el-option label="运营" value="OPERATOR" />
            <el-option label="客服" value="SUPPORT" />
          </el-select>
        </el-form-item>
        <el-form-item v-if="!isEditMode" label="密码" prop="password">
          <el-input v-model="userForm.password" type="password" show-password />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="userDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveUser">确认</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'

export default {
  name: 'System',
  setup() {
    const activeTab = ref('user')
    const userDialogVisible = ref(false)
    const isEditMode = ref(false)
    const userFormRef = ref(null)
    const logDateRange = ref([])
    const logCurrentPage = ref(1)
    const logPageSize = ref(20)
    const logTotal = ref(0)
    
    // 用户列表数据
    const users = ref([
      {
        id: 1,
        username: 'admin',
        email: 'admin@game.com',
        role: 'ADMIN',
        status: 'ACTIVE',
        lastLogin: '2025-06-08 10:30:00'
      },
      {
        id: 2,
        username: 'operator1',
        email: 'op1@game.com',
        role: 'OPERATOR',
        status: 'ACTIVE',
        lastLogin: '2025-06-08 09:15:00'
      }
    ])
    
    // 操作日志数据
    const operationLogs = ref([
      {
        id: 1,
        operator: 'admin',
        action: '玩家封禁',
        resource: '玩家ID: 12345',
        ip: '192.168.1.100',
        timestamp: '2025-06-08 10:30:00',
        result: 'SUCCESS'
      },
      {
        id: 2,
        operator: 'operator1',
        action: '发送邮件',
        resource: '全服邮件',
        ip: '192.168.1.101',
        timestamp: '2025-06-08 10:25:00',
        result: 'SUCCESS'
      }
    ])
    
    // 用户表单
    const userForm = reactive({
      username: '',
      email: '',
      role: '',
      password: ''
    })
    
    // 用户表单验证规则
    const userFormRules = {
      username: [
        { required: true, message: '请输入用户名', trigger: 'blur' },
        { min: 3, max: 20, message: '长度在 3 到 20 个字符', trigger: 'blur' }
      ],
      email: [
        { required: true, message: '请输入邮箱地址', trigger: 'blur' },
        { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }
      ],
      role: [
        { required: true, message: '请选择角色', trigger: 'change' }
      ],
      password: [
        { required: true, message: '请输入密码', trigger: 'blur' },
        { min: 6, message: '密码长度不能少于6位', trigger: 'blur' }
      ]
    }
    
    // 系统设置
    const systemSettings = reactive({
      systemName: '游戏管理后台',
      sessionTimeout: 30,
      passwordPolicy: ['requireNumbers'],
      minPasswordLength: 8,
      enableLoginLock: true,
      maxLoginAttempts: 5
    })
    
    // 获取角色标签类型
    const getRoleTagType = (role) => {
      const types = {
        'ADMIN': 'danger',
        'OPERATOR': 'warning',
        'SUPPORT': 'info'
      }
      return types[role] || 'info'
    }
    
    // 显示添加用户对话框
    const showAddUserDialog = () => {
      isEditMode.value = false
      Object.assign(userForm, {
        username: '',
        email: '',
        role: '',
        password: ''
      })
      userDialogVisible.value = true
    }
    
    // 编辑用户
    const editUser = (user) => {
      isEditMode.value = true
      Object.assign(userForm, {
        id: user.id,
        username: user.username,
        email: user.email,
        role: user.role,
        password: ''
      })
      userDialogVisible.value = true
    }
    
    // 保存用户
    const saveUser = async () => {
      try {
        await userFormRef.value.validate()
        
        // TODO: 调用实际的API
        ElMessage.success(isEditMode.value ? '用户更新成功' : '用户创建成功')
        userDialogVisible.value = false
        
        // 刷新用户列表
        // loadUsers()
      } catch (error) {
        ElMessage.error('操作失败')
      }
    }
    
    // 重置密码
    const resetPassword = async (user) => {
      try {
        await ElMessageBox.confirm(`确认要重置用户 ${user.username} 的密码吗？`, '确认操作', {
          confirmButtonText: '确认',
          cancelButtonText: '取消',
          type: 'warning'
        })
        
        // TODO: 调用实际的API
        ElMessage.success('密码重置成功，新密码已发送到用户邮箱')
      } catch (error) {
        if (error !== 'cancel') {
          ElMessage.error('重置失败')
        }
      }
    }
    
    // 切换用户状态
    const toggleUserStatus = async (user) => {
      const action = user.status === 'ACTIVE' ? '禁用' : '启用'
      
      try {
        await ElMessageBox.confirm(`确认要${action}用户 ${user.username} 吗？`, '确认操作', {
          confirmButtonText: '确认',
          cancelButtonText: '取消',
          type: 'warning'
        })
        
        // TODO: 调用实际的API
        user.status = user.status === 'ACTIVE' ? 'DISABLED' : 'ACTIVE'
        ElMessage.success(`用户${action}成功`)
      } catch (error) {
        if (error !== 'cancel') {
          ElMessage.error(`${action}失败`)
        }
      }
    }
    
    // 查询日志
    const searchLogs = () => {
      // TODO: 调用实际的API查询日志
      ElMessage.success('日志查询完成')
    }
    
    // 导出日志
    const exportLogs = () => {
      // TODO: 实现日志导出功能
      ElMessage.success('日志导出中...')
    }
    
    // 查看日志详情
    const viewLogDetail = (log) => {
      ElMessageBox.alert(`操作详情：${JSON.stringify(log, null, 2)}`, '日志详情', {
        confirmButtonText: '确定'
      })
    }
    
    // 保存系统设置
    const saveSettings = () => {
      // TODO: 调用实际的API保存设置
      ElMessage.success('设置保存成功')
    }
    
    // 重置系统设置
    const resetSettings = () => {
      Object.assign(systemSettings, {
        systemName: '游戏管理后台',
        sessionTimeout: 30,
        passwordPolicy: ['requireNumbers'],
        minPasswordLength: 8,
        enableLoginLock: true,
        maxLoginAttempts: 5
      })
      ElMessage.info('设置已重置')
    }
    
    return {
      activeTab,
      userDialogVisible,
      isEditMode,
      userFormRef,
      logDateRange,
      logCurrentPage,
      logPageSize,
      logTotal,
      users,
      operationLogs,
      userForm,
      userFormRules,
      systemSettings,
      getRoleTagType,
      showAddUserDialog,
      editUser,
      saveUser,
      resetPassword,
      toggleUserStatus,
      searchLogs,
      exportLogs,
      viewLogDetail,
      saveSettings,
      resetSettings
    }
  }
}
</script>

<style scoped>
.system-management {
  height: 100%;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.pagination {
  margin-top: 20px;
  text-align: right;
}
</style>