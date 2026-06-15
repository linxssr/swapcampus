<template>
  <div class="order-list">
    <div class="page-header">
      <h2>订单管理</h2>
      <div class="search-bar">
        <input
          v-model="searchKeyword"
          type="text"
          placeholder="搜索订单号/商品名..."
          class="search-input"
          @keyup.enter="loadOrders(1)"
        />
        <button class="search-btn" @click="loadOrders(1)">搜索</button>
      </div>
    </div>

    <!-- 状态筛选标签 -->
    <div class="status-tabs">
      <button
        :class="{ active: filterStatus === 'all' }"
        @click="switchStatus('all')"
      >
        全部 ({{ totalCount }})
      </button>
      <button
        :class="{ active: filterStatus === '1' }"
        @click="switchStatus('1')"
      >
        待确认 ({{ statusCount.pending }})
      </button>
      <button
        :class="{ active: filterStatus === '2' }"
        @click="switchStatus('2')"
      >
        待取件 ({{ statusCount.waiting }})
      </button>
      <button
        :class="{ active: filterStatus === '3' }"
        @click="switchStatus('3')"
      >
        已完成 ({{ statusCount.completed }})
      </button>
      <button
        :class="{ active: filterStatus === '4' }"
        @click="switchStatus('4')"
      >
        已取消 ({{ statusCount.canceled }})
      </button>
    </div>

    <div v-if="loading" class="loading">加载中...</div>

    <div v-else-if="orders.length === 0" class="empty">
      暂无订单
    </div>

    <div v-else>
      <div class="order-list-container">
        <table class="order-table">
          <thead>
            <tr>
              <th>订单号</th>
              <th>商品名称</th>
              <th>买家</th>
              <th>卖家</th>
              <th>金额</th>
              <th>交易方式</th>
              <th>状态</th>
              <th>下单时间</th>
              <th>操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="order in orders" :key="order.order_id">
              <td>{{ order.order_no }}</td>
              <td>
                <a href="#" @click.prevent="goToItemDetail(order.item_id)">
                  {{ order.item_title || `商品ID: ${order.item_id}` }}
                </a>
              </td>
              <td>{{ order.buyer_name || `用户${order.buyer_id}` }}</td>
              <td>{{ order.seller_name || `用户${order.seller_id}` }}</td>
              <td>¥{{ formatPrice(order.price) }}</td>
              <td>
                <span :class="['trade-badge', order.trade_type === 1 ? 'face' : 'locker']">
                  {{ order.trade_type === 1 ? '面交' : '快递柜' }}
                </span>
              </td>
              <td>
                <span :class="['status-badge', getOrderStatusClass(order.order_status)]">
                  {{ getOrderStatusText(order.order_status) }}
                </span>
              </td>
              <td>{{ formatDate(order.create_time) }}</td>
              <td>
                <button class="btn-detail" @click="goToOrderDetail(order.order_id)">
                  详情
                </button>
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <!-- 分页组件 -->
      <div class="pagination" v-if="totalPages > 1">
        <button :disabled="currentPage === 1" @click="goToPage(currentPage - 1)">
          上一页
        </button>
        <span class="page-info">第 {{ currentPage }} / {{ totalPages }} 页</span>
        <button :disabled="currentPage === totalPages" @click="goToPage(currentPage + 1)">
          下一页
        </button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useAdminAuthStore } from '@/stores/modules/adminAuth'

const router = useRouter()
const adminAuthStore = useAdminAuthStore()

const orders = ref<any[]>([])
const loading = ref(false)
const currentPage = ref(1)
const totalPages = ref(1)
const total = ref(0)
const searchKeyword = ref('')
const filterStatus = ref('all')

// 各状态数量统计
const statusCount = ref({
  pending: 0,
  waiting: 0,
  completed: 0,
  canceled: 0
})

const totalCount = computed(() =>
  statusCount.value.pending +
  statusCount.value.waiting +
  statusCount.value.completed +
  statusCount.value.canceled
)

const formatDate = (dateStr?: string) => {
  if (!dateStr) return ''
  return dateStr.replace('T', ' ').slice(0, 16)
}

const formatPrice = (price: number) => {
  return price.toFixed(2)
}

const getOrderStatusText = (status: number) => {
  const statusMap: Record<number, string> = {
    1: '待确认',
    2: '待取件',
    3: '已完成',
    4: '已取消'
  }
  return statusMap[status] || '未知'
}

const getOrderStatusClass = (status: number) => {
  const classMap: Record<number, string> = {
    1: 'status-pending',
    2: 'status-waiting',
    3: 'status-done',
    4: 'status-cancel'
  }
  return classMap[status] || ''
}

// 切换状态
const switchStatus = (status: string) => {
  filterStatus.value = status
  loadOrders(1)
}

// 加载订单列表
const loadOrders = async (page: number = 1) => {
  currentPage.value = page
  loading.value = true

  try {
    let url = `/api/v1/admin/order/list?page=${page}&size=10`

    if (filterStatus.value !== 'all') {
      url += `&orderStatus=${filterStatus.value}`
    }

    if (searchKeyword.value.trim()) {
      url += `&keyword=${encodeURIComponent(searchKeyword.value)}`
    }

    const res = await fetch(url, {
      headers: { 'Authorization': `Bearer ${adminAuthStore.token}` }
    })
    const data = await res.json()

    if (data.code === 200) {
      orders.value = data.data || []
      total.value = data.total || 0
      totalPages.value = data.totalPages || 1

      // 更新各状态数量
      if (data.statusCount) {
        statusCount.value = data.statusCount
      }
    }
  } catch (error) {
    console.error('加载订单失败:', error)
  } finally {
    loading.value = false
  }
}

// 跳转页面
const goToPage = (page: number) => {
  if (page >= 1 && page <= totalPages.value) {
    loadOrders(page)
  }
}

// 跳转到商品详情
const goToItemDetail = (itemId: number) => {
  router.push(`/item/${itemId}`)
}

// 跳转到订单详情
const goToOrderDetail = (orderId: number) => {
  router.push(`/order/${orderId}`)
}

onMounted(() => {
  loadOrders(1)
})
</script>

<style scoped>
.order-list {
  background: white;
  border-radius: 16px;
  padding: 24px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  padding-bottom: 16px;
  border-bottom: 1px solid #e5e7eb;
}

.page-header h2 {
  margin: 0;
  font-size: 20px;
}

.search-bar {
  display: flex;
  gap: 8px;
}

.search-input {
  padding: 8px 16px;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  width: 240px;
  font-size: 14px;
}

.search-btn {
  padding: 8px 20px;
  background: #2563eb;
  color: white;
  border: none;
  border-radius: 8px;
  cursor: pointer;
}

.status-tabs {
  display: flex;
  gap: 8px;
  margin-bottom: 20px;
  padding-bottom: 12px;
  border-bottom: 1px solid #e5e7eb;
  flex-wrap: wrap;
}

.status-tabs button {
  padding: 8px 20px;
  background: #f3f4f6;
  border: none;
  border-radius: 20px;
  cursor: pointer;
  font-size: 14px;
  transition: all 0.3s;
}

.status-tabs button.active {
  background: #2563eb;
  color: white;
}

.order-list-container {
  overflow-x: auto;
}

.order-table {
  width: 100%;
  border-collapse: collapse;
  min-width: 900px;
}

.order-table th,
.order-table td {
  padding: 12px;
  text-align: left;
  border-bottom: 1px solid #e5e7eb;
}

.order-table th {
  background: #f9fafb;
  font-weight: 600;
  color: #374151;
}

.order-table tr:hover {
  background: #f9fafb;
}

.order-table td a {
  color: #2563eb;
  text-decoration: none;
}

.order-table td a:hover {
  text-decoration: underline;
}

.trade-badge {
  padding: 4px 12px;
  border-radius: 20px;
  font-size: 12px;
}

.trade-badge.face {
  background: #e0e7ff;
  color: #4f46e5;
}

.trade-badge.locker {
  background: #fef3c7;
  color: #d97706;
}

.status-badge {
  padding: 4px 12px;
  border-radius: 20px;
  font-size: 12px;
}

.status-pending {
  background: #fef3c7;
  color: #d97706;
}

.status-waiting {
  background: #e0e7ff;
  color: #4f46e5;
}

.status-done {
  background: #d1fae5;
  color: #10b981;
}

.status-cancel {
  background: #fee2e2;
  color: #dc2626;
}

.btn-detail {
  padding: 4px 16px;
  background: #f3f4f6;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-size: 12px;
}

.btn-detail:hover {
  background: #e5e7eb;
}

.pagination {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 16px;
  margin-top: 24px;
  padding-top: 20px;
  border-top: 1px solid #e5e7eb;
}

.pagination button {
  padding: 8px 16px;
  background: #f3f4f6;
  border: none;
  border-radius: 8px;
  cursor: pointer;
}

.pagination button:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.page-info {
  font-size: 14px;
  color: #6b7280;
}

.loading, .empty {
  text-align: center;
  padding: 60px;
  color: #9ca3af;
}
</style>