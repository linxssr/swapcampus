<template>
  <div class="user-manage">
    <div class="page-header">
      <h2>用户管理</h2>
      <div class="search-bar">
        <input
          v-model="searchKeyword"
          type="text"
          placeholder="搜索学号/昵称..."
          class="search-input"
          @keyup.enter="loadUsers(1)"
        />
        <button class="search-btn" @click="loadUsers(1)">搜索</button>
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
        :class="{ active: filterStatus === 'active' }"
        @click="switchStatus('active')"
      >
        正常 ({{ activeCount }})
      </button>
      <button
        :class="{ active: filterStatus === 'banned' }"
        @click="switchStatus('banned')"
      >
        封禁 ({{ bannedCount }})
      </button>
    </div>

    <div v-if="loading" class="loading">加载中...</div>

    <div v-else-if="users.length === 0" class="empty">
      暂无用户
    </div>

    <div v-else>
      <div class="user-list">
        <table class="user-table">
          <thead>
            <tr>
              <th>ID</th>
              <th>学号</th>
              <th>昵称</th>
              <th>手机号</th>
              <th>信用分</th>
              <th>状态</th>
              <th>注册时间</th>
              <th>操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="user in users" :key="user.user_id">
              <td>{{ user.user_id }}</td>
              <td>{{ user.student_id }}</td>
              <td>{{ user.username }}</td>
              <td>{{ user.phone || '-' }}</td>
              <td>
                <span class="credit-score">{{ user.credit_score }}</span>
              </td>
              <td>
                <span :class="['status-badge', user.status === 1 ? 'active' : 'banned']">
                  {{ user.status === 1 ? '正常' : '封禁' }}
                </span>
              </td>
              <td>{{ formatDate(user.create_time) }}</td>
              <td class="actions">
                <button
                  v-if="user.status === 1"
                  class="btn-ban"
                  @click="handleBan(user.user_id, 0)"
                >
                  封禁
                </button>
                <button
                  v-else
                  class="btn-unban"
                  @click="handleBan(user.user_id, 1)"
                >
                  解封
                </button>
                <button class="btn-score" @click="openScoreDialog(user)">
                  调整信用分
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

    <!-- 调整信用分弹窗 -->
    <div v-if="showScoreDialog" class="modal-overlay" @click.self="closeScoreDialog">
      <div class="modal">
        <h3>调整信用分</h3>
        <div class="current-score">
          当前信用分：<strong>{{ currentUser?.credit_score }}</strong>
        </div>

        <div class="form-item">
          <label>调整方式</label>
          <div class="radio-group">
            <label>
              <input type="radio" v-model="scoreAction" value="add" /> 增加
            </label>
            <label>
              <input type="radio" v-model="scoreAction" value="subtract" /> 减少
            </label>
          </div>
        </div>

        <div class="form-item">
          <label>调整分数</label>
          <input
            type="number"
            v-model.number="scoreValue"
            min="1"
            :max="scoreAction === 'add' ? 10 : 20"
            step="1"
          />
          <div class="hint" v-if="scoreAction === 'add'">提示：单次最多增加10分</div>
          <div class="hint warn" v-else>提示：单次最多减少20分</div>
        </div>

        <div class="form-item">
          <label>调整原因</label>
          <input
            type="text"
            v-model="scoreReason"
            placeholder="请输入调整原因（如：交易完成奖励）"
          />
        </div>

        <div class="modal-buttons">
          <button class="btn-confirm" @click="updateCreditScore" :disabled="scoreUpdating">
            确认调整
          </button>
          <button class="btn-cancel" @click="closeScoreDialog">取消</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useAdminAuthStore } from '@/stores/modules/adminAuth'

const adminAuthStore = useAdminAuthStore()
const users = ref<any[]>([])
const loading = ref(false)
const currentPage = ref(1)
const totalPages = ref(1)
const total = ref(0)
const searchKeyword = ref('')

// 状态筛选
const filterStatus = ref<'all' | 'active' | 'banned'>('all')
const totalCount = ref(0)
const activeCount = ref(0)
const bannedCount = ref(0)

// 信用分调整相关
const showScoreDialog = ref(false)
const currentUser = ref<any>(null)
const scoreAction = ref<'add' | 'subtract'>('add')
const scoreValue = ref(0)
const scoreReason = ref('')
const scoreUpdating = ref(false)

const formatDate = (dateStr?: string) => {
  if (!dateStr) return ''
  return dateStr.replace('T', ' ').slice(0, 16)
}

// 加载用户列表
const loadUsers = async (page: number = 1) => {
  currentPage.value = page
  loading.value = true

  try {
    let url = `/api/v1/user/list?page=${page}&size=10`

    if (filterStatus.value === 'active') {
      url += `&status=1`
    } else if (filterStatus.value === 'banned') {
      url += `&status=0`
    }

    if (searchKeyword.value.trim()) {
      url += `&keyword=${encodeURIComponent(searchKeyword.value)}`
    }

    const res = await fetch(url, {
      headers: { 'Authorization': `Bearer ${adminAuthStore.token}` }
    })
    const data = await res.json()

    if (data.code === 200) {
      users.value = data.data || []
      total.value = data.total || 0
      totalPages.value = data.totalPages || 1
      totalCount.value = data.total || 0
    }
  } catch (error) {
    console.error('加载用户失败:', error)
  } finally {
    loading.value = false
  }
}

// 切换状态
const switchStatus = (status: 'all' | 'active' | 'banned') => {
  filterStatus.value = status
  loadUsers(1)
}

// 跳转页面
const goToPage = (page: number) => {
  if (page >= 1 && page <= totalPages.value) {
    loadUsers(page)
  }
}

// 封禁/解封用户
const handleBan = async (userId: number, status: number) => {
  const action = status === 0 ? '封禁' : '解封'
  if (!confirm(`确定要${action}该用户吗？`)) return

  try {
    const res = await fetch(`/api/v1/admin/user/ban/${userId}?status=${status}`, {
      method: 'PUT',
      headers: {
        'Authorization': `Bearer ${adminAuthStore.token}`
      }
    })
    const data = await res.json()

    if (data.code === 200) {
      alert(`${action}成功`)
      await loadUsers(currentPage.value)
      await loadStats()
    } else {
      alert(data.message || `${action}失败`)
    }
  } catch (error) {
    console.error(`${action}失败:`, error)
    alert(`${action}失败`)
  }
}

// 加载统计数据
const loadStats = async () => {
  try {
    const activeRes = await fetch(`/api/v1/user/list?size=1&status=1`, {
      headers: { 'Authorization': `Bearer ${adminAuthStore.token}` }
    })
    const activeData = await activeRes.json()
    if (activeData.code === 200) {
      activeCount.value = activeData.total || 0
    }

    const bannedRes = await fetch(`/api/v1/user/list?size=1&status=0`, {
      headers: { 'Authorization': `Bearer ${adminAuthStore.token}` }
    })
    const bannedData = await bannedRes.json()
    if (bannedData.code === 200) {
      bannedCount.value = bannedData.total || 0
    }
  } catch (error) {
    console.error('加载统计数据失败:', error)
  }
}

// 打开调整弹窗
const openScoreDialog = (user: any) => {
  currentUser.value = user
  scoreAction.value = 'add'
  scoreValue.value = 0
  scoreReason.value = ''
  showScoreDialog.value = true
}

// 关闭弹窗
const closeScoreDialog = () => {
  showScoreDialog.value = false
  currentUser.value = null
}

// 更新信用分
const updateCreditScore = async () => {
  if (scoreValue.value <= 0) {
    alert('请输入正整数')
    return
  }

  if (scoreAction.value === 'add' && scoreValue.value > 10) {
    alert('单次最多增加10分')
    return
  }
  if (scoreAction.value === 'subtract' && scoreValue.value > 20) {
    alert('单次最多减少20分')
    return
  }

  if (!scoreReason.value.trim()) {
    alert('请输入调整原因')
    return
  }

  scoreUpdating.value = true
  try {
    const changeAmount = scoreAction.value === 'add' ? scoreValue.value : -scoreValue.value
    const res = await fetch('/api/v1/admin/user/credit', {
      method: 'PUT',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${adminAuthStore.token}`
      },
      body: JSON.stringify({
        userId: currentUser.value.user_id,
        changeAmount: changeAmount
      })
    })
    const data = await res.json()

    if (data.code === 200) {
      alert(`信用分已${scoreAction.value === 'add' ? '增加' : '减少'} ${scoreValue.value}分，当前信用分：${data.data}`)
      closeScoreDialog()
      await loadUsers(currentPage.value)
      await loadStats()
    } else {
      alert(data.message || '调整失败')
    }
  } catch (error) {
    console.error('调整失败:', error)
    alert('调整失败')
  } finally {
    scoreUpdating.value = false
  }
}

onMounted(() => {
  loadStats()
  loadUsers(1)
})
</script>

<style scoped>
.user-manage {
  background: white;
  border-radius: 16px;
  padding: 24px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
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
  width: 200px;
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

.user-table {
  width: 100%;
  border-collapse: collapse;
}

.user-table th,
.user-table td {
  padding: 12px;
  text-align: left;
  border-bottom: 1px solid #e5e7eb;
}

.user-table th {
  background: #f9fafb;
  font-weight: 600;
  color: #374151;
}

.user-table tr:hover {
  background: #f9fafb;
}

.credit-score {
  font-weight: 600;
  color: #f59e0b;
}

.status-badge {
  padding: 4px 12px;
  border-radius: 20px;
  font-size: 12px;
}

.status-badge.active {
  background: #d1fae5;
  color: #10b981;
}

.status-badge.banned {
  background: #fee2e2;
  color: #dc2626;
}

.btn-ban {
  padding: 4px 16px;
  background: #ef4444;
  color: white;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-size: 12px;
}

.btn-unban {
  padding: 4px 16px;
  background: #10b981;
  color: white;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-size: 12px;
}

.btn-score {
  padding: 4px 12px;
  background: #f59e0b;
  color: white;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-size: 12px;
  margin-left: 8px;
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

.status-tabs {
  display: flex;
  gap: 8px;
  margin-bottom: 20px;
  padding-bottom: 12px;
  border-bottom: 1px solid #e5e7eb;
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

.actions {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
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
  font-size: 16px;
}

.current-score strong {
  color: #f59e0b;
  font-size: 20px;
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

.radio-group {
  display: flex;
  gap: 20px;
}

.radio-group label {
  display: flex;
  align-items: center;
  gap: 6px;
  cursor: pointer;
  font-weight: normal;
}

.hint {
  font-size: 12px;
  color: #10b981;
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
  background: #2563eb;
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