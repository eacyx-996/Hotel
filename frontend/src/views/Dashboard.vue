<template>
  <div class="page-container">
    <div class="page-header">
      <h2>数据统计仪表盘</h2>
      <el-button type="primary" :icon="Refresh" @click="loadData">刷新数据</el-button>
    </div>

    <!-- 统计卡片 -->
    <el-row :gutter="20" style="margin-bottom: 20px">
      <el-col :span="6">
        <el-card class="stat-card" shadow="hover">
          <el-icon :size="32" color="#409EFF"><OfficeBuilding /></el-icon>
          <div class="stat-value">{{ stats.roomStats?.total || 0 }}</div>
          <div class="stat-label">客房总数</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card" shadow="hover">
          <el-icon :size="32" color="#67C23A"><SuccessFilled /></el-icon>
          <div class="stat-value">{{ stats.roomStats?.available || 0 }}</div>
          <div class="stat-label">空闲房间</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card" shadow="hover">
          <el-icon :size="32" color="#E6A23C"><Tickets /></el-icon>
          <div class="stat-value">{{ stats.totalOrders || 0 }}</div>
          <div class="stat-label">订单总数</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card" shadow="hover">
          <el-icon :size="32" color="#F56C6C"><Money /></el-icon>
          <div class="stat-value">¥{{ formatMoney(stats.totalRevenue) }}</div>
          <div class="stat-label">总营收</div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 图表区域 -->
    <el-row :gutter="20">
      <el-col :span="16">
        <el-card shadow="hover">
          <template #header><span>近7天订单数与营收趋势</span></template>
          <div ref="trendChartRef" style="height: 350px"></div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="hover">
          <template #header><span>订单状态分布</span></template>
          <div ref="pieChartRef" style="height: 350px"></div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top: 20px">
      <el-col :span="12">
        <el-card shadow="hover">
          <template #header><span>房态概览</span></template>
          <div ref="roomChartRef" style="height: 300px"></div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card shadow="hover">
          <template #header><span>房态列表</span></template>
          <el-table :data="roomStatusList" stripe style="width: 100%">
            <el-table-column prop="label" label="状态" />
            <el-table-column prop="value" label="数量" />
            <el-table-column label="占比">
              <template #default="{ row }">
                <el-progress :percentage="row.percent" :color="row.color" />
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, nextTick } from 'vue'
import * as echarts from 'echarts'
import { ElMessage } from 'element-plus'
import { Refresh } from '@element-plus/icons-vue'
import request from '@/utils/request'

const stats = ref({})
const trendChartRef = ref(null)
const pieChartRef = ref(null)
const roomChartRef = ref(null)
let trendChart, pieChart, roomChart

const statusLabels = { 0: '空闲', 1: '已预订', 2: '已入住', 3: '清洁中', 4: '维修中' }

const roomStatusList = ref([])

const formatMoney = (v) => {
  if (!v && v !== 0) return '0.00'
  return Number(v).toFixed(2)
}

const loadData = async () => {
  try {
    const res = await request.get('/dashboard/stats')
    stats.value = res.data
    await nextTick()
    renderCharts()
  } catch (e) {
    ElMessage.error('加载数据失败')
  }
}

const renderCharts = () => {
  // 重新渲染前先销毁旧实例，避免 ECharts 重复初始化警告
  trendChart?.dispose()
  pieChart?.dispose()
  roomChart?.dispose()
  const data = stats.value
  const roomStats = data.roomStats || {}
  const total = roomStats.total || 0
  const occupied = roomStats.occupied || 0
  const available = roomStats.available || 0
  const cleaning = Math.max(0, total - occupied - available)

  roomStatusList.value = [
    { label: '空闲', value: available, percent: total ? Math.round(available / total * 100) : 0, color: '#67C23A' },
    { label: '已预订/已入住', value: occupied, percent: total ? Math.round(occupied / total * 100) : 0, color: '#F56C6C' },
    { label: '其他', value: cleaning, percent: total ? Math.round(cleaning / total * 100) : 0, color: '#E6A23C' },
  ]

  // 趋势图
  const dailyStats = data.dailyStats || []
  const dates = dailyStats.map(d => d.date?.toString().slice(5) || '')
  const orders = dailyStats.map(d => d.order_count || 0)
  const revenues = dailyStats.map(d => Number(d.revenue || 0).toFixed(0))

  trendChart = echarts.init(trendChartRef.value)
  trendChart.setOption({
    tooltip: { trigger: 'axis' },
    legend: { data: ['订单数', '营收(元)'] },
    grid: { left: 40, right: 40, top: 40, bottom: 30 },
    xAxis: { type: 'category', data: dates },
    yAxis: [
      { type: 'value', name: '订单数' },
      { type: 'value', name: '营收(元)' },
    ],
    series: [
      { name: '订单数', type: 'bar', data: orders, itemStyle: { color: '#409EFF' } },
      { name: '营收(元)', type: 'line', yAxisIndex: 1, data: revenues, smooth: true, itemStyle: { color: '#F56C6C' } },
    ],
  })

  // 饼图 - 订单状态
  const statusCount = data.orderStatusCount || []
  const orderStatusNames = { 0: '待确认', 1: '已确认', 2: '已入住', 3: '已退房', 4: '已取消' }
  pieChart = echarts.init(pieChartRef.value)
  pieChart.setOption({
    tooltip: { trigger: 'item', formatter: '{b}: {c} ({d}%)' },
    legend: { orient: 'vertical', right: 10, top: 'center' },
    series: [{
      type: 'pie', radius: ['40%', '70%'], center: ['40%', '50%'],
      data: statusCount.map(s => ({
        name: orderStatusNames[s.order_status] || '未知',
        value: s.count,
      })),
      emphasis: { itemStyle: { shadowBlur: 10, shadowOffsetX: 0, shadowColor: 'rgba(0, 0, 0, 0.5)' } },
    }],
  })

  // 房态柱状图
  roomChart = echarts.init(roomChartRef.value)
  roomChart.setOption({
    tooltip: { trigger: 'axis' },
    grid: { left: 60, right: 30, top: 30, bottom: 30 },
    xAxis: { type: 'category', data: ['空闲', '已预订/已入住'] },
    yAxis: { type: 'value' },
    series: [{
      type: 'bar', barWidth: 50,
      data: [
        { value: available, itemStyle: { color: '#67C23A' } },
        { value: occupied, itemStyle: { color: '#F56C6C' } },
      ],
      label: { show: true, position: 'top', formatter: '{c}' },
    }],
  })
}

const handleResize = () => {
  trendChart?.resize()
  pieChart?.resize()
  roomChart?.resize()
}

onMounted(() => {
  loadData()
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  trendChart?.dispose()
  pieChart?.dispose()
  roomChart?.dispose()
})
</script>
