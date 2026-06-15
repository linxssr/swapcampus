<template>
  <div class="admin-login-page">
    <div class="login-card">
      <div class="logo">
        <h1>SwapCampus</h1>
        <p>管理员后台</p>
      </div>

      <form @submit.prevent="handleLogin">
        <div class="form-item">
          <input
            v-model="form.adminName"
            type="text"
            placeholder="管理员账号"
            autocomplete="off"
          />
        </div>

        <div class="form-item">
          <input
            v-model="form.password"
            :type="showPassword ? 'text' : 'password'"
            placeholder="密码"
          />
          <span class="password-eye" @click="showPassword = !showPassword">
            {{ showPassword ? '🙈' : '👁️' }}
          </span>
        </div>

        <button type="submit" :disabled="loading" class="login-btn">
          {{ loading ? '登录中...' : '登录' }}
        </button>
      </form>
    </div>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAdminAuthStore } from '@/stores/modules/adminAuth'

const router = useRouter()
const adminAuthStore = useAdminAuthStore()

const form = reactive({
  adminName: '',
  password: ''
})

const loading = ref(false)
const showPassword = ref(false)

const showMessage = (message: string, type: 'success' | 'error' = 'error') => {
  const msgDiv = document.createElement('div')
  msgDiv.textContent = message
  const bgColor = type === 'success' ? '#52c41a' : '#ff4d4f'
  msgDiv.style.cssText = `
    position: fixed;
    top: 80px;
    left: 50%;
    transform: translateX(-50%);
    padding: 10px 20px;
    background: ${bgColor};
    color: white;
    border-radius: 8px;
    z-index: 9999;
    font-size: 14px;
  `
  document.body.appendChild(msgDiv)
  setTimeout(() => msgDiv.remove(), 2000)
}

const handleLogin = async () => {
  if (!form.adminName || !form.password) {
    showMessage('请输入账号和密码')
    return
  }

  loading.value = true
  try {
    const result = await adminAuthStore.login(form.adminName, form.password)
    if (result.success) {
      showMessage('登录成功', 'success')
      setTimeout(() => router.push('/admin/dashboard'), 500)
    } else {
      showMessage(result.message || '登录失败')
    }
  } catch (error: any) {
    showMessage(error.message || '登录失败')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.admin-login-page {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background: linear-gradient(135deg, #1f2937 0%, #111827 100%);
}

.login-card {
  width: 400px;
  padding: 40px;
  background: white;
  border-radius: 16px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
}

.logo {
  text-align: center;
  margin-bottom: 32px;
}

.logo h1 {
  margin: 0;
  font-size: 28px;
  color: #2563eb;
}

.logo p {
  margin: 8px 0 0;
  font-size: 14px;
  color: #6b7280;
}

.form-item {
  margin-bottom: 20px;
  position: relative;
}

.form-item input {
  width: 100%;
  padding: 14px 16px;
  border: 1px solid #e5e7eb;
  border-radius: 12px;
  font-size: 14px;
  box-sizing: border-box;
}

.form-item input:focus {
  outline: none;
  border-color: #2563eb;
}

.password-eye {
  position: absolute;
  right: 16px;
  top: 50%;
  transform: translateY(-50%);
  cursor: pointer;
}

.login-btn {
  width: 100%;
  padding: 14px;
  background: #2563eb;
  color: white;
  border: none;
  border-radius: 12px;
  font-size: 16px;
  cursor: pointer;
}

.login-btn:disabled {
  opacity: 0.6;
}
</style>