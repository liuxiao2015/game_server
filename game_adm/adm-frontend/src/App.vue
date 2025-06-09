<template>
  <div id="app">
    <el-container class="layout">
      <el-aside width="200px" class="sidebar">
        <el-menu
          :default-active="$route.path"
          class="sidebar-menu"
          router
          background-color="#304156"
          text-color="#bfcbd9"
          active-text-color="#409EFF"
        >
          <div class="sidebar-logo">
            <h3>游戏管理后台</h3>
          </div>
          <el-menu-item index="/dashboard">
            <el-icon><Monitor /></el-icon>
            <span>数据看板</span>
          </el-menu-item>
          <el-menu-item index="/gm">
            <el-icon><Tools /></el-icon>
            <span>GM工具</span>
          </el-menu-item>
          <el-menu-item index="/system">
            <el-icon><Setting /></el-icon>
            <span>系统管理</span>
          </el-menu-item>
        </el-menu>
      </el-aside>
      
      <el-container>
        <el-header class="header">
          <div class="header-content">
            <h2>{{ pageTitle }}</h2>
            <div class="user-info">
              <el-dropdown>
                <span class="user-name">
                  管理员 <el-icon class="el-icon--right"><arrow-down /></el-icon>
                </span>
                <template #dropdown>
                  <el-dropdown-menu>
                    <el-dropdown-item>个人信息</el-dropdown-item>
                    <el-dropdown-item>退出登录</el-dropdown-item>
                  </el-dropdown-menu>
                </template>
              </el-dropdown>
            </div>
          </div>
        </el-header>
        
        <el-main class="main-content">
          <router-view />
        </el-main>
      </el-container>
    </el-container>
  </div>
</template>

<script>
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import { Monitor, Tools, Setting, ArrowDown } from '@element-plus/icons-vue'

export default {
  name: 'App',
  components: {
    Monitor,
    Tools,
    Setting,
    ArrowDown
  },
  setup() {
    const route = useRoute()
    
    const pageTitle = computed(() => {
      const titles = {
        '/dashboard': '数据看板',
        '/gm': 'GM工具',
        '/system': '系统管理'
      }
      return titles[route.path] || '游戏管理后台'
    })
    
    return {
      pageTitle
    }
  }
}
</script>

<style>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

#app {
  font-family: 'Helvetica Neue', Helvetica, 'PingFang SC', 'Hiragino Sans GB', 'Microsoft YaHei', '微软雅黑', Arial, sans-serif;
  height: 100vh;
}

.layout {
  height: 100vh;
}

.sidebar {
  background-color: #304156;
}

.sidebar-logo {
  padding: 20px;
  text-align: center;
  color: #bfcbd9;
  border-bottom: 1px solid #434a50;
}

.sidebar-logo h3 {
  margin: 0;
  font-size: 16px;
}

.sidebar-menu {
  border: none;
}

.header {
  background-color: #fff;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.12);
  padding: 0 20px;
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  height: 100%;
}

.header-content h2 {
  margin: 0;
  color: #303133;
}

.user-info {
  color: #606266;
}

.user-name {
  cursor: pointer;
}

.main-content {
  background-color: #f0f2f5;
  padding: 20px;
}
</style>