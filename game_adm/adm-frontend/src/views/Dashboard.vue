<template>
  <div class="dashboard">
    <el-row :gutter="20">
      <!-- 实时统计卡片 -->
      <el-col :span="6" v-for="stat in realtimeStats" :key="stat.title">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-value">{{ stat.value }}</div>
            <div class="stat-title">{{ stat.title }}</div>
          </div>
          <div class="stat-icon">
            <el-icon :size="40" :color="stat.color">
              <component :is="stat.icon" />
            </el-icon>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top: 20px;">
      <!-- 在线人数趋势图 -->
      <el-col :span="12">
        <el-card>
          <template #header>
            <span>在线人数趋势</span>
          </template>
          <div ref="onlineChart" class="chart-container"></div>
        </el-card>
      </el-col>

      <!-- 收入统计图 -->
      <el-col :span="12">
        <el-card>
          <template #header>
            <span>收入统计</span>
          </template>
          <div ref="revenueChart" class="chart-container"></div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top: 20px;">
      <!-- 用户留存分析 -->
      <el-col :span="8">
        <el-card>
          <template #header>
            <span>用户留存分析</span>
          </template>
          <div ref="retentionChart" class="chart-container" style="height: 300px;"></div>
        </el-card>
      </el-col>

      <!-- 服务器状态 -->
      <el-col :span="16">
        <el-card>
          <template #header>
            <span>服务器状态</span>
          </template>
          <el-table :data="serverStatus" style="width: 100%">
            <el-table-column prop="name" label="服务名称" width="150" />
            <el-table-column prop="status" label="状态" width="100">
              <template #default="scope">
                <el-tag :type="scope.row.status === 'RUNNING' ? 'success' : 'danger'">
                  {{ scope.row.status }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="cpu" label="CPU使用率" />
            <el-table-column prop="memory" label="内存使用率" />
            <el-table-column prop="connections" label="连接数" />
          </el-table>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script>
import { ref, onMounted, onBeforeUnmount } from 'vue'
import * as echarts from 'echarts'
import { statisticsApi } from '../api/statistics'

export default {
  name: 'Dashboard',
  setup() {
    const onlineChart = ref(null)
    const revenueChart = ref(null)
    const retentionChart = ref(null)
    
    let onlineChartInstance = null
    let revenueChartInstance = null
    let retentionChartInstance = null
    let refreshTimer = null
    
    const realtimeStats = ref([
      { title: '当前在线', value: '1,234', icon: 'User', color: '#409EFF' },
      { title: '今日新增', value: '456', icon: 'Plus', color: '#67C23A' },
      { title: '今日收入', value: '¥12,345', icon: 'Money', color: '#E6A23C' },
      { title: '活跃用户', value: '5,678', icon: 'Star', color: '#F56C6C' }
    ])
    
    const serverStatus = ref([
      { name: 'Gateway', status: 'RUNNING', cpu: '25%', memory: '45%', connections: '1,234' },
      { name: 'Logic', status: 'RUNNING', cpu: '35%', memory: '55%', connections: '2,345' },
      { name: 'Chat', status: 'RUNNING', cpu: '15%', memory: '35%', connections: '567' },
      { name: 'Payment', status: 'RUNNING', cpu: '20%', memory: '40%', connections: '123' }
    ])
    
    const initOnlineChart = () => {
      onlineChartInstance = echarts.init(onlineChart.value)
      const option = {
        tooltip: {
          trigger: 'axis'
        },
        xAxis: {
          type: 'category',
          data: ['00:00', '04:00', '08:00', '12:00', '16:00', '20:00', '24:00']
        },
        yAxis: {
          type: 'value'
        },
        series: [{
          data: [820, 932, 901, 934, 1290, 1330, 1320],
          type: 'line',
          smooth: true,
          areaStyle: {}
        }]
      }
      onlineChartInstance.setOption(option)
    }
    
    const initRevenueChart = () => {
      revenueChartInstance = echarts.init(revenueChart.value)
      const option = {
        tooltip: {
          trigger: 'axis'
        },
        xAxis: {
          type: 'category',
          data: ['周一', '周二', '周三', '周四', '周五', '周六', '周日']
        },
        yAxis: {
          type: 'value'
        },
        series: [{
          data: [1200, 1320, 1010, 1340, 1590, 1830, 1720],
          type: 'bar',
          itemStyle: {
            color: '#409EFF'
          }
        }]
      }
      revenueChartInstance.setOption(option)
    }
    
    const initRetentionChart = () => {
      retentionChartInstance = echarts.init(retentionChart.value)
      const option = {
        tooltip: {
          trigger: 'item'
        },
        series: [{
          name: '留存率',
          type: 'pie',
          radius: ['40%', '70%'],
          data: [
            { value: 65, name: '1日留存' },
            { value: 45, name: '3日留存' },
            { value: 35, name: '7日留存' },
            { value: 25, name: '30日留存' }
          ],
          emphasis: {
            itemStyle: {
              shadowBlur: 10,
              shadowOffsetX: 0,
              shadowColor: 'rgba(0, 0, 0, 0.5)'
            }
          }
        }]
      }
      retentionChartInstance.setOption(option)
    }
    
    const refreshData = async () => {
      try {
        // TODO: 调用实际的API获取数据
        // const onlineData = await statisticsApi.getOnlineStatistics()
        // const revenueData = await statisticsApi.getRevenue()
        console.log('Refreshing dashboard data...')
      } catch (error) {
        console.error('Failed to refresh data:', error)
      }
    }
    
    onMounted(() => {
      initOnlineChart()
      initRevenueChart()
      initRetentionChart()
      
      // 定时刷新数据
      refreshTimer = setInterval(refreshData, 30000) // 30秒刷新一次
      
      // 窗口大小改变时重新调整图表
      window.addEventListener('resize', () => {
        onlineChartInstance?.resize()
        revenueChartInstance?.resize()
        retentionChartInstance?.resize()
      })
    })
    
    onBeforeUnmount(() => {
      if (refreshTimer) {
        clearInterval(refreshTimer)
      }
      onlineChartInstance?.dispose()
      revenueChartInstance?.dispose()
      retentionChartInstance?.dispose()
    })
    
    return {
      onlineChart,
      revenueChart,
      retentionChart,
      realtimeStats,
      serverStatus
    }
  }
}
</script>

<style scoped>
.dashboard {
  height: 100%;
}

.stat-card {
  margin-bottom: 20px;
}

.stat-card .el-card__body {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px;
}

.stat-content {
  flex: 1;
}

.stat-value {
  font-size: 24px;
  font-weight: bold;
  color: #303133;
  margin-bottom: 8px;
}

.stat-title {
  font-size: 14px;
  color: #909399;
}

.stat-icon {
  opacity: 0.8;
}

.chart-container {
  height: 350px;
  width: 100%;
}
</style>