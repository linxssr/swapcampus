<template>
  <div class="dashboard">
    <h1>欢迎，{{ adminAuthStore.adminInfo?.realName || '管理员' }}</h1>
    <p class="subtitle">今天是 {{ today }}，祝您工作愉快</p>

    <!-- 第一行：核心指标 -->
    <div class="stats-grid">
      <div class="stat-card">
        <div class="stat-icon pending">⏳</div>
        <div class="stat-info">
          <div class="stat-value">{{ pendingItemCount }}</div>
          <div class="stat-label">待审核商品</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon report">⚠️</div>
        <div class="stat-info">
          <div class="stat-value">{{ pendingReportCount }}</div>
          <div class="stat-label">待处理举报</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon user">👥</div>
        <div class="stat-info">
          <div class="stat-value">{{ totalUserCount }}</div>
          <div class="stat-label">总用户数</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon item">🛒</div>
        <div class="stat-info">
          <div class="stat-value">{{ totalItemCount }}</div>
          <div class="stat-label">总商品数</div>
        </div>
      </div>
    </div>

    <!-- 第二行：今日数据 -->
    <div class="stats-grid">
      <div class="stat-card">
        <div class="stat-icon today">📈</div>
        <div class="stat-info">
          <div class="stat-value">{{ todayNewUser }}</div>
          <div class="stat-label">今日新增用户</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon today">🆕</div>
        <div class="stat-info">
          <div class="stat-value">{{ todayNewItem }}</div>
          <div class="stat-label">今日新增商品</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon today">💰</div>
        <div class="stat-info">
          <div class="stat-value">¥{{ todayIncome }}</div>
          <div class="stat-label">今日成交额</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon today">📦</div>
        <div class="stat-info">
          <div class="stat-value">{{ todayOrderCount }}</div>
          <div class="stat-label">今日订单数</div>
        </div>
      </div>
    </div>

    <!-- 第三行：订单分布 + 分类分布（饼图） -->
    <div class="row-two-col">
      <div class="card">
        <h3>
          订单状态分布
          <router-link to="/admin/order/list" class="more">查看更多 →</router-link>
        </h3>
        <div ref="orderChartRef" class="chart-container"></div>
      </div>

      <div class="card">
        <h3>
          商品分类分布
          <router-link to="/admin/category/manage" class="more">查看更多 →</router-link>
        </h3>
        <div ref="categoryChartRef" class="chart-container"></div>
      </div>
    </div>

    <!-- 第四行：待办列表 -->
    <div class="row-two-col">
      <div class="card">
        <h3>
          待审核商品
          <router-link to="/admin/item/audit" class="more">查看更多 →</router-link>
        </h3>
        <div v-if="pendingItems.length === 0" class="empty-tip">暂无待审核商品</div>
        <div v-else class="todo-list">
          <div v-for="item in pendingItems" :key="item.item_id" class="todo-item">
            <span class="title">{{ item.title }}</span>
            <span class="time">{{ formatDate(item.create_time) }}</span>
          </div>
        </div>
      </div>

      <div class="card">
        <h3>
          待处理举报
          <router-link to="/admin/report/list" class="more">查看更多 →</router-link>
        </h3>
        <div v-if="pendingReports.length === 0" class="empty-tip">暂无待处理举报</div>
        <div v-else class="todo-list">
          <div v-for="report in pendingReports" :key="report.report_id" class="todo-item">
            <span class="title">{{ report.item_title || `商品${report.item_id}` }}</span>
            <span class="reason">{{ report.reason?.slice(0, 20) }}...</span>
          </div>
        </div>
      </div>
    </div>

    <!-- 快捷操作 -->
    <div class="quick-actions">
      <h3>快捷操作</h3>
      <div class="action-buttons">
        <button class="action-btn" @click="goToItemAudit">商品审核</button>
        <button class="action-btn" @click="goToReportList">举报处理</button>
        <button class="action-btn" @click="goToUserManage">用户管理</button>
        <button class="action-btn" @click="goToCategoryManage">分类管理</button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { useAdminAuthStore } from '@/stores/modules/adminAuth'
import * as echarts from 'echarts'

const router = useRouter()
const adminAuthStore = useAdminAuthStore()

// 统计数据
const pendingItemCount = ref(0)
const pendingReportCount = ref(0)
const totalUserCount = ref(0)
const totalItemCount = ref(0)
const todayNewUser = ref(0)
const todayNewItem = ref(0)
const todayIncome = ref(0)
const todayOrderCount = ref(0)

// 订单状态统计
const orderStatusCount = ref({
  pending: 0,
  waiting: 0,
  completed: 0,
  canceled: 0
})

// 待办列表
const pendingItems = ref<any[]>([])
const pendingReports = ref<any[]>([])
const topCategories = ref<any[]>([])

// 图表 DOM 引用
const orderChartRef = ref<HTMLElement>()
const categoryChartRef = ref<HTMLElement>()
let orderChart: echarts.ECharts | null = null
let categoryChart: echarts.ECharts | null = null

const today = new Date().toLocaleDateString('zh-CN')

const formatDate = (dateStr?: string) => {
  if (!dateStr) return ''
  return dateStr.replace('T', ' ').slice(0, 16)
}

// 初始化订单状态饼图
const initOrderChart = () => {
  if (!orderChartRef.value) return

  if (orderChart) {
    orderChart.dispose()
  }

  orderChart = echarts.init(orderChartRef.value)

  const option = {
    tooltip: {
      trigger: 'item',
      formatter: '{b}: {d}% ({c}单)'
    },
    legend: {
      orient: 'vertical',
      left: 'left',
      data: ['待确认', '待取件', '已完成', '已取消']
    },
    series: [
      {
        name: '订单状态',
        type: 'pie',
        radius: '55%',
        center: ['50%', '50%'],
        data: [
          { value: orderStatusCount.value.pending, name: '待确认', itemStyle: { color: '#f59e0b' } },
          { value: orderStatusCount.value.waiting, name: '待取件', itemStyle: { color: '#3b82f6' } },
          { value: orderStatusCount.value.completed, name: '已完成', itemStyle: { color: '#10b981' } },
          { value: orderStatusCount.value.canceled, name: '已取消', itemStyle: { color: '#ef4444' } }
        ],
        emphasis: {
          scale: true
        },
        label: {
          show: true,
          formatter: '{b}: {d}%'
        }
      }
    ]
  }

  orderChart.setOption(option)
}

// 初始化分类分布饼图
const initCategoryChart = () => {
  if (!categoryChartRef.value) return

  if (categoryChart) {
    categoryChart.dispose()
  }

  categoryChart = echarts.init(categoryChartRef.value)

  // 分类颜色
  const colors = ['#f59e0b', '#10b981', '#3b82f6', '#ef4444', '#8b5cf6', '#ec4899', '#06b6d4', '#84cc16']

  const data = topCategories.value.map((cat, index) => ({
    value: cat.count,
    name: cat.category_name,
    itemStyle: { color: colors[index % colors.length] }
  }))

  const option = {
    tooltip: {
      trigger: 'item',
      formatter: '{b}: {d}% ({c}件)'
    },
    legend: {
      orient: 'vertical',
      left: 'left',
      data: topCategories.value.map(cat => cat.category_name)
    },
    series: [
      {
        name: '商品分类',
        type: 'pie',
        radius: '55%',
        center: ['50%', '50%'],
        data: data,
        emphasis: {
          scale: true
        },
        label: {
          show: true,
          formatter: '{b}: {d}%'
        }
      }
    ]
  }

  categoryChart.setOption(option)
}

// 更新图表
const updateCharts = () => {
  nextTick(() => {
    if (orderChart) {
      orderChart.setOption({
        series: [{
          data: [
            { value: orderStatusCount.value.pending, name: '待确认' },
            { value: orderStatusCount.value.waiting, name: '待取件' },
            { value: orderStatusCount.value.completed, name: '已完成' },
            { value: orderStatusCount.value.canceled, name: '已取消' }
          ]
        }]
      })
    }

    if (categoryChart && topCategories.value.length > 0) {
      const colors = ['#f59e0b', '#10b981', '#3b82f6', '#ef4444', '#8b5cf6', '#ec4899', '#06b6d4', '#84cc16']
      const data = topCategories.value.map((cat, index) => ({
        value: cat.count,
        name: cat.category_name,
        itemStyle: { color: colors[index % colors.length] }
      }))

      categoryChart.setOption({
        legend: { data: topCategories.value.map(cat => cat.category_name) },
        series: [{ data: data }]
      })
    }
  })
}

// 加载所有数据
const loadAllStats = async () => {
  const token = adminAuthStore.token

  try {
    const [statRes, orderRes, categoryRes, itemListRes, reportListRes] = await Promise.all([
      fetch('/api/v1/admin/dashboard/stat', { headers: { 'Authorization': `Bearer ${token}` } }),
      fetch('/api/v1/admin/dashboard/order-status', { headers: { 'Authorization': `Bearer ${token}` } }),
      fetch('/api/v1/admin/dashboard/category-stats', { headers: { 'Authorization': `Bearer ${token}` } }),
      fetch('/api/v1/admin/item/list?auditStatus=0&page=1&size=5', { headers: { 'Authorization': `Bearer ${token}` } }),
      fetch('/api/v1/admin/report/list?handleStatus=0&page=1&size=5', { headers: { 'Authorization': `Bearer ${token}` } })
    ])

    const statData = await statRes.json()
    if (statData.code === 200) {
      pendingItemCount.value = statData.data.pendingItem
      pendingReportCount.value = statData.data.pendingReport
      totalUserCount.value = statData.data.totalUser
      totalItemCount.value = statData.data.totalItem
      todayNewUser.value = statData.data.todayNewUser
      todayNewItem.value = statData.data.todayNewItem
      todayOrderCount.value = statData.data.todayOrderCount
      todayIncome.value = statData.data.todayIncome
    }

    const orderData = await orderRes.json()
    if (orderData.code === 200) {
      orderData.data.forEach((item: any) => {
        if (item.status === 1) orderStatusCount.value.pending = item.count
        else if (item.status === 2) orderStatusCount.value.waiting = item.count
        else if (item.status === 3) orderStatusCount.value.completed = item.count
        else if (item.status === 4) orderStatusCount.value.canceled = item.count
      })
    }

    const categoryData = await categoryRes.json()
    if (categoryData.code === 200) {
      topCategories.value = categoryData.data || []
    }

    const itemListData = await itemListRes.json()
    if (itemListData.code === 200) {
      pendingItems.value = itemListData.data || []
    }

    const reportListData = await reportListRes.json()
    if (reportListData.code === 200) {
      pendingReports.value = reportListData.data || []
    }

    // 数据加载完成后初始化图表
    await nextTick()
    initOrderChart()
    initCategoryChart()
  } catch (error) {
    console.error('加载仪表盘数据失败:', error)
  }
}

// 快捷跳转
const goToItemAudit = () => router.push('/admin/item/audit')
const goToReportList = () => router.push('/admin/report/list')
const goToUserManage = () => router.push('/admin/user/manage')
const goToCategoryManage = () => router.push('/admin/category/manage')

onMounted(() => {
  loadAllStats()
})
</script>

<style scoped>
/* 保留原有样式，添加图表容器样式 */
.dashboard {
  padding: 20px;
}

.dashboard h1 {
  margin: 0 0 8px;
  font-size: 24px;
}

.subtitle {
  color: #6b7280;
  margin: 0 0 32px;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
  margin-bottom: 24px;
}

.stat-card {
  background: white;
  border-radius: 16px;
  padding: 20px;
  display: flex;
  align-items: center;
  gap: 16px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.stat-icon {
  width: 56px;
  height: 56px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28px;
}

.stat-icon.pending { background: #fef3c7; }
.stat-icon.report { background: #fee2e2; }
.stat-icon.user { background: #d1fae5; }
.stat-icon.item { background: #e0e7ff; }
.stat-icon.today { background: #f3e8ff; }

.stat-value {
  font-size: 32px;
  font-weight: bold;
  color: #1f2937;
}

.stat-label {
  font-size: 14px;
  color: #6b7280;
  margin-top: 4px;
}

.row-two-col {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 20px;
  margin-bottom: 24px;
}

.card {
  background: white;
  border-radius: 16px;
  padding: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.card h3 {
  margin: 0 0 16px;
  font-size: 16px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.more {
  font-size: 12px;
  color: #2563eb;
  text-decoration: none;
}

/* 图表容器 */
.chart-container {
  width: 100%;
  height: 280px;
}

.todo-list {
  max-height: 200px;
  overflow-y: auto;
}

.todo-item {
  display: flex;
  justify-content: space-between;
  padding: 10px 0;
  border-bottom: 1px solid #f0f0f0;
}

.todo-item .title {
  font-weight: 500;
}

.todo-item .time, .reason {
  font-size: 12px;
  color: #9ca3af;
}

.empty-tip {
  text-align: center;
  padding: 40px;
  color: #9ca3af;
}

.quick-actions {
  background: white;
  border-radius: 16px;
  padding: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.quick-actions h3 {
  margin: 0 0 16px;
  font-size: 16px;
}

.action-buttons {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}

.action-btn {
  padding: 8px 20px;
  background: #f3f4f6;
  border: none;
  border-radius: 8px;
  cursor: pointer;
}

.action-btn:hover {
  background: #2563eb;
  color: white;
}
</style>