<template>
  <div class="report-manage">
    <div class="page-header">
      <h2>举报管理</h2>
      <div class="search-bar">
        <input
          v-model="searchKeyword"
          type="text"
          placeholder="搜索举报用户/商品/原因..."
          class="search-input"
          @keyup.enter="loadReports(filterStatus, 1)"
        />
        <button class="search-btn" @click="loadReports(filterStatus, 1)">搜索</button>
      </div>
    </div>

    <div class="tabs">
      <button
        :class="{ active: filterStatus === 0 }"
        @click="loadReports(0, 1)"
      >
        待处理 ({{ pendingCount }})
      </button>
      <button
        :class="{ active: filterStatus === 1 }"
        @click="loadReports(1, 1)"
      >
        已处理
      </button>
    </div>

    <div v-if="loading" class="loading">加载中...</div>

    <div v-else-if="reports.length === 0" class="empty">
      暂无举报
    </div>

    <div v-else>
      <div class="report-list">
        <div v-for="report in reports" :key="report.report_id" class="report-card">
          <div class="report-header">
            <span class="report-id">举报ID: {{ report.report_id }}</span>
            <span :class="['status-badge', report.handle_status === 0 ? 'pending' : 'handled']">
              {{ report.handle_status === 0 ? '待处理' : '已处理' }}
            </span>
          </div>

          <div class="report-content">
            <div class="info-row">
              <span class="label">举报用户：</span>
              <span class="value">{{ report.report_user_name || `用户${report.report_user}` }}</span>
            </div>
            <div class="info-row">
              <span class="label">举报商品：</span>
              <span class="value">
                <a href="#" @click.prevent="goToItemDetail(report.item_id)">{{ report.item_title || `商品ID: ${report.item_id}` }}</a>
              </span>
            </div>
            <div class="info-row">
              <span class="label">举报原因：</span>
              <span class="value reason">{{ report.reason }}</span>
            </div>
            <div class="info-row">
              <span class="label">举报时间：</span>
              <span class="value">{{ formatDate(report.create_time) }}</span>
            </div>
          </div>

          <div class="report-actions" v-if="report.handle_status === 0">
            <textarea
              v-model="handleResult[report.report_id]"
              placeholder="请输入处理结果..."
              class="handle-input"
              rows="2"
            ></textarea>
            <button class="btn-handle" @click="handleReport(report.report_id)">
              确认处理
            </button>
          </div>

          <div class="report-result" v-else>
            <span class="result-label">处理结果：</span>
            <span class="result-value">{{ report.handle_result || '已处理' }}</span>
          </div>
        </div>
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

    <!-- 扣除信用分弹窗 -->
    <div v-if="showCreditDialog" class="modal-overlay" @click.self="closeCreditDialog">
      <div class="modal">
        <h3>扣除信用分</h3>
        <div class="current-score">
          被举报用户ID：<strong>{{ targetUserId }}</strong>
        </div>

        <div class="form-item">
          <label>扣除分数</label>
          <input
            type="number"
            v-model.number="deductScore"
            min="1"
            max="20"
            step="1"
          />
          <div class="hint warn">提示：单次最多扣除20分</div>
        </div>

        <div class="form-item">
          <label>扣除原因</label>
          <input
            type="text"
            v-model="deductReason"
            placeholder="请输入扣除原因（如：举报核实，违规商品）"
          />
        </div>

        <div class="modal-buttons">
          <button class="btn-confirm" @click="confirmDeduct" :disabled="deducting">
            确认扣除
          </button>
          <button class="btn-cancel" @click="skipDeduct">不扣除</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAdminAuthStore } from '@/stores/modules/adminAuth'

const router = useRouter()
const adminAuthStore = useAdminAuthStore()

const reports = ref<any[]>([])
const loading = ref(false)
const filterStatus = ref(0)
const pendingCount = ref(0)
const currentPage = ref(1)
const totalPages = ref(1)
const total = ref(0)
const searchKeyword = ref('')
const handleResult = ref<Record<number, string>>({})

// 扣除信用分相关
const showCreditDialog = ref(false)
const currentReportId = ref<number | null>(null)
const currentHandleResult = ref('')
const targetUserId = ref<number | null>(null)
const deductScore = ref(0)
const deductReason = ref('')
const deducting = ref(false)

const formatDate = (dateStr?: string) => {
  if (!dateStr) return ''
  return dateStr.replace('T', ' ').slice(0, 16)
}

// 跳转到商品详情
const goToItemDetail = (itemId: number) => {
  router.push(`/item/${itemId}`)
}

// 获取被举报商品的发布者ID
const getItemSellerId = async (itemId: number) => {
  try {
    const token = adminAuthStore.token
    const res = await fetch(`/api/v1/user/items/batch?ids=${itemId}`, {
      headers: { 'Authorization': `Bearer ${token}` }
    })
    const data = await res.json()
    console.log('批量接口返回完整数据:', JSON.stringify(data, null, 2))

    if (data.code === 200 && data.data && data.data.length > 0) {
      console.log('商品数据字段:', Object.keys(data.data[0]))
      return data.data[0].user_id
    }
    return null
  } catch (error) {
    console.error('获取商品信息失败:', error)
    return null
  }
}

// 扣除信用分
const deductCredit = async (userId: number, points: number) => {
  try {
    const token = adminAuthStore.token
    const res = await fetch('/api/v1/admin/user/credit', {
      method: 'PUT',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${token}`
      },
      body: JSON.stringify({
        userId: userId,
        changeAmount: -points
      })
    })
    const data = await res.json()
    if (data.code === 200) {
      alert(`已扣除${points}信用分，当前信用分：${data.data}`)
      return true
    } else {
      alert(data.message || '扣除失败')
      return false
    }
  } catch (error) {
    console.error('扣除失败:', error)
    alert('扣除失败')
    return false
  }
}

// 完成处理（不扣除信用分）
const completeHandle = async (reportId: number, result: string) => {
  try {
    const res = await fetch('/api/v1/admin/report/handle', {
      method: 'PUT',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${adminAuthStore.token}`
      },
      body: JSON.stringify({
        reportId: reportId,
        result: result
      })
    })
    const data = await res.json()

    if (data.code === 200) {
      alert('处理成功')
      delete handleResult.value[reportId]
      await loadReports(filterStatus.value, currentPage.value)
    } else {
      alert(data.message || '处理失败')
    }
  } catch (error) {
    console.error('处理失败:', error)
    alert('处理失败')
  }
}

// 处理举报
const handleReport = async (reportId: number) => {
  const result = handleResult.value[reportId]
  if (!result || !result.trim()) {
    alert('请输入处理结果')
    return
  }

  const report = reports.value.find(r => r.report_id === reportId)
  if (!report) {
    alert('举报信息不存在')
    return
  }

  const confirmMsg = '是否扣除被举报用户的信用分？\n点击"确定"进入扣除页面，点击"取消"直接完成处理'
  if (confirm(confirmMsg)) {
    const sellerId = await getItemSellerId(report.item_id)
    if (sellerId) {
      currentReportId.value = reportId
      currentHandleResult.value = result
      targetUserId.value = sellerId
      deductScore.value = 5
      deductReason.value = '举报核实，违规商品'
      showCreditDialog.value = true
      return
    } else {
      alert('无法获取商品信息，将直接完成处理')
      await completeHandle(reportId, result)
    }
  } else {
    await completeHandle(reportId, result)
  }
}

// 确认扣除并处理
const confirmDeduct = async () => {
  if (deductScore.value <= 0) {
    alert('请输入正整数')
    return
  }
  if (deductScore.value > 20) {
    alert('单次最多扣除20分')
    return
  }
  if (!deductReason.value.trim()) {
    alert('请输入扣除原因')
    return
  }

  deducting.value = true
  try {
    const deducted = await deductCredit(targetUserId.value!, deductScore.value)
    if (!deducted) {
      deducting.value = false
      return
    }

    const res = await fetch('/api/v1/admin/report/handle', {
      method: 'PUT',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${adminAuthStore.token}`
      },
      body: JSON.stringify({
        reportId: currentReportId.value,
        result: currentHandleResult.value + `（已扣除${deductScore.value}信用分，原因：${deductReason.value}）`
      })
    })
    const data = await res.json()

    if (data.code === 200) {
      alert('处理成功，已扣除信用分')
      delete handleResult.value[currentReportId.value!]
      closeCreditDialog()
      await loadReports(filterStatus.value, currentPage.value)
    } else {
      alert(data.message || '处理失败')
    }
  } catch (error) {
    console.error('处理失败:', error)
    alert('处理失败')
  } finally {
    deducting.value = false
  }
}

// 不扣除，直接完成处理
const skipDeduct = async () => {
  closeCreditDialog()
  await completeHandle(currentReportId.value!, currentHandleResult.value)
}

// 关闭扣除弹窗
const closeCreditDialog = () => {
  showCreditDialog.value = false
  currentReportId.value = null
  targetUserId.value = null
  deductScore.value = 0
  deductReason.value = ''
}

// 加载举报列表
const loadReports = async (status: number, page: number = 1) => {
  filterStatus.value = status
  currentPage.value = page
  loading.value = true

  try {
    let url = `/api/v1/admin/report/list?page=${page}&size=10`

    if (status === 0 || status === 1) {
      url += `&handleStatus=${status}`
    }

    if (searchKeyword.value.trim()) {
      url += `&keyword=${encodeURIComponent(searchKeyword.value)}`
    }

    const res = await fetch(url, {
      headers: { 'Authorization': `Bearer ${adminAuthStore.token}` }
    })
    const data = await res.json()

    if (data.code === 200) {
      reports.value = data.data || []
      total.value = data.total || 0
      totalPages.value = data.totalPages || 1

      if (status === 0) {
        pendingCount.value = data.total || 0
      }
    }
  } catch (error) {
    console.error('加载举报失败:', error)
  } finally {
    loading.value = false
  }
}

// 跳转页面
const goToPage = (page: number) => {
  if (page >= 1 && page <= totalPages.value) {
    loadReports(filterStatus.value, page)
  }
}

onMounted(() => {
  loadReports(0, 1)
})
</script>

<style scoped>
.report-manage {
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

.search-btn:hover {
  background: #1d4ed8;
}

.tabs {
  display: flex;
  gap: 8px;
  margin-bottom: 20px;
}

.tabs button {
  padding: 8px 20px;
  background: #f3f4f6;
  border: none;
  border-radius: 20px;
  cursor: pointer;
  font-size: 14px;
}

.tabs button.active {
  background: #2563eb;
  color: white;
}

.report-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.report-card {
  padding: 20px;
  background: #f9fafb;
  border-radius: 16px;
  transition: all 0.3s;
}

.report-card:hover {
  background: #f3f4f6;
}

.report-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
  padding-bottom: 12px;
  border-bottom: 1px solid #e5e7eb;
}

.report-id {
  font-size: 14px;
  color: #6b7280;
}

.status-badge {
  padding: 4px 12px;
  border-radius: 20px;
  font-size: 12px;
}

.status-badge.pending {
  background: #fef3c7;
  color: #d97706;
}

.status-badge.handled {
  background: #d1fae5;
  color: #10b981;
}

.report-content {
  margin-bottom: 16px;
}

.info-row {
  margin-bottom: 8px;
  font-size: 14px;
}

.info-row .label {
  display: inline-block;
  width: 80px;
  color: #6b7280;
}

.info-row .value {
  color: #374151;
}

.info-row .value a {
  color: #2563eb;
  text-decoration: none;
}

.info-row .value a:hover {
  text-decoration: underline;
}

.info-row .reason {
  color: #dc2626;
}

.report-actions {
  margin-top: 12px;
}

.handle-input {
  width: 100%;
  padding: 12px;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  font-size: 14px;
  resize: vertical;
  box-sizing: border-box;
}

.handle-input:focus {
  outline: none;
  border-color: #2563eb;
}

.btn-handle {
  margin-top: 12px;
  padding: 8px 24px;
  background: #2563eb;
  color: white;
  border: none;
  border-radius: 8px;
  cursor: pointer;
}

.btn-handle:hover {
  background: #1d4ed8;
}

.report-result {
  margin-top: 12px;
  padding: 12px;
  background: #e0e7ff;
  border-radius: 8px;
}

.result-label {
  font-size: 14px;
  font-weight: 500;
  color: #4f46e5;
}

.result-value {
  margin-left: 8px;
  font-size: 14px;
  color: #374151;
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

.loading,
.empty {
  text-align: center;
  padding: 60px;
  color: #9ca3af;
}

/* 弹窗样式 */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
}

.modal {
  background: white;
  border-radius: 16px;
  padding: 24px;
  width: 400px;
}

.modal h3 {
  margin: 0 0 16px;
}

.current-score {
  margin: 0 0 20px;
  padding: 12px;
  background: #f3f4f6;
  border-radius: 8px;
  font-size: 14px;
  text-align: center;
}

.current-score strong {
  color: #ef4444;
  font-size: 18px;
}

.form-item {
  margin-bottom: 16px;
}

.form-item label {
  display: block;
  margin-bottom: 8px;
  font-size: 14px;
  font-weight: 500;
  color: #374151;
}

.form-item input {
  width: 100%;
  padding: 10px 12px;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  font-size: 14px;
  box-sizing: border-box;
}

.hint {
  font-size: 12px;
  margin-top: 4px;
}

.hint.warn {
  color: #f59e0b;
}

.modal-buttons {
  display: flex;
  gap: 12px;
  margin-top: 20px;
}

.btn-confirm {
  flex: 1;
  padding: 10px;
  background: #ef4444;
  color: white;
  border: none;
  border-radius: 8px;
  cursor: pointer;
}

.btn-confirm:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.btn-cancel {
  flex: 1;
  padding: 10px;
  background: #f3f4f6;
  color: #374151;
  border: none;
  border-radius: 8px;
  cursor: pointer;
}
</style>