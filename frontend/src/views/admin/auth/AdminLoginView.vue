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

      <div class="switch-links">
        <span>普通用户？</span>
        <RouterLink to="/login">登录</RouterLink>
        <span class="divider">|</span>
        <RouterLink to="/register">注册</RouterLink>
      </div>
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
  background: #1a1c2e;
  background-image:
    repeating-linear-gradient(0deg, transparent, transparent 31px, rgba(255,255,255,0.03) 31px, rgba(255,255,255,0.03) 32px),
    repeating-linear-gradient(90deg, transparent, transparent 31px, rgba(255,255,255,0.03) 31px, rgba(255,255,255,0.03) 32px);
}

.login-card {
  width: 400px;
  padding: 40px;
  background: #252840;
  border: 2px solid var(--color-primary);
  border-radius: 2px;
  box-shadow: 6px 6px 0px var(--color-primary-dk);
}

.logo {
  text-align: center;
  margin-bottom: 32px;
}

.logo h1 {
  margin: 0;
  font-size: 26px;
  font-weight: 800;
  color: var(--color-primary);
  letter-spacing: 1px;
}

.logo p {
  margin: 8px 0 0;
  font-size: 12px;
  font-weight: 700;
  color: #6b7280;
  letter-spacing: 2px;
  text-transform: uppercase;
}

.form-item {
  margin-bottom: 18px;
  position: relative;
}

.form-item input {
  width: 100%;
  padding: 12px 14px;
  border: 2px solid #3d4165;
  border-radius: 2px;
  font-size: 14px;
  font-weight: 600;
  box-sizing: border-box;
  background: #1a1c2e;
  color: #e8e9f0;
  transition: border-color 0.1s;
}

.form-item input::placeholder {
  color: #4b5563;
  font-weight: 500;
}

.form-item input:focus {
  outline: none;
  border-color: var(--color-primary);
}

.password-eye {
  position: absolute;
  right: 12px;
  top: 50%;
  transform: translateY(-50%);
  cursor: pointer;
  font-size: 16px;
}

.login-btn {
  width: 100%;
  padding: 13px;
  background: var(--color-primary);
  color: var(--color-text);
  border: 2px solid var(--color-primary-dk);
  border-radius: 2px;
  font-size: 15px;
  font-weight: 800;
  cursor: pointer;
  box-shadow: 3px 3px 0 var(--color-primary-dk);
  transition: box-shadow 0.1s, transform 0.1s;
  letter-spacing: 0.5px;
}

.login-btn:hover:not(:disabled) {
  box-shadow: 1px 1px 0 var(--color-primary-dk);
  transform: translate(2px, 2px);
}

.login-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
  box-shadow: none;
}

.switch-links {
  margin-top: 22px;
  text-align: center;
  font-size: 13px;
  font-weight: 600;
  color: #6b7280;
  padding-top: 18px;
  border-top: 1.5px solid #3d4165;
}

.switch-links a {
  color: var(--color-primary);
  text-decoration: none;
  margin: 0 6px;
  font-weight: 700;
}

.switch-links a:hover { text-decoration: underline; }

.switch-links .divider {
  color: #3d4165;
  margin: 0 2px;
}
</style>