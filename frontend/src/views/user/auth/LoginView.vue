<template>
  <div class="login-page">
    <div class="login-card">
      <div class="login-header">
        <h1 class="brand">SwapCampus</h1>
        <p class="subtitle">校园闲置交易平台</p>
      </div>

      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        class="login-form"
        @submit.prevent="handleLogin"
      >
        <el-form-item prop="studentId">
          <el-input
            v-model="form.studentId"
            placeholder="请输入学号"
            size="large"
            prefix-icon="User"
          />
        </el-form-item>

        <el-form-item prop="password">
          <el-input
            v-model="form.password"
            type="password"
            placeholder="请输入密码"
            size="large"
            prefix-icon="Lock"
            show-password
            @keyup.enter="handleLogin"
          />
        </el-form-item>

        <el-form-item>
          <el-button
            type="primary"
            size="large"
            class="login-btn"
            :loading="loading"
            native-type="submit"
          >
            登录
          </el-button>
        </el-form-item>
      </el-form>

      <div class="login-footer">
        <span>还没有账号？</span>
        <RouterLink to="/register" class="link">立即注册</RouterLink>
      </div>

      <div class="admin-entry">
        <RouterLink to="/admin/login" class="admin-link">管理员入口</RouterLink>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ElMessage } from 'element-plus';
import { reactive, ref } from 'vue';
import { useRouter } from 'vue-router';
import type { FormInstance, FormRules } from 'element-plus';
import { useAuthStore } from '@/stores/modules/auth';

const router = useRouter();
const authStore = useAuthStore();
const formRef = ref<FormInstance>();
const loading = ref(false);

const form = reactive({
  studentId: '',
  password: '',
});

const rules: FormRules = {
  studentId: [{ required: true, message: '请输入学号', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
};

async function handleLogin() {
  const valid = await formRef.value?.validate().catch(() => false);
  if (!valid) return;

  loading.value = true;
  try {
    const result = await authStore.login(form.studentId, form.password);
    if (result.success) {
      ElMessage.success('登录成功');
      router.push('/');
    } else {
      ElMessage.error(result.message || '登录失败');
    }
  } catch (error: any) {
    ElMessage.error(error.response?.data?.message || '学号或密码错误');
  } finally {
    loading.value = false;
  }
}
</script>

<style scoped>
.login-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--color-bg);
  background-image:
    repeating-linear-gradient(0deg, transparent, transparent 31px, rgba(45,48,71,0.05) 31px, rgba(45,48,71,0.05) 32px),
    repeating-linear-gradient(90deg, transparent, transparent 31px, rgba(45,48,71,0.05) 31px, rgba(45,48,71,0.05) 32px);
}

.login-card {
  width: 400px;
  padding: 40px;
  background: var(--color-surface);
  border: var(--border-pixel);
  border-radius: var(--radius-pixel);
  box-shadow: var(--shadow-hard);
}

.login-header {
  text-align: center;
  margin-bottom: 32px;
}

.brand {
  margin: 0;
  font-size: 30px;
  font-weight: 800;
  color: var(--color-text);
  letter-spacing: -1px;
}

.brand span {
  color: var(--color-primary-dk);
}

.subtitle {
  margin: 8px 0 0;
  color: var(--color-text-sub);
  font-size: 13px;
  font-weight: 600;
}

.login-form { margin-bottom: 20px; }

.login-btn {
  width: 100%;
  height: 44px;
  font-size: 15px;
  font-weight: 700;
}

.login-footer {
  text-align: center;
  font-size: 14px;
  font-weight: 600;
  color: var(--color-text-sub);
}

.link {
  color: var(--color-primary-dk);
  font-weight: 700;
  margin-left: 4px;
  text-decoration: none;
}

.link:hover { text-decoration: underline; }

.admin-entry {
  text-align: center;
  margin-top: 14px;
  padding-top: 14px;
  border-top: 1.5px solid #e5e0d8;
}

.admin-link {
  font-size: 12px;
  font-weight: 600;
  color: var(--color-text-sub);
  text-decoration: none;
}

.admin-link:hover { color: var(--color-text); text-decoration: underline; }
</style>
