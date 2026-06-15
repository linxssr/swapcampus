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
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.login-card {
  width: 400px;
  padding: 40px;
  background: #ffffff;
  border-radius: 16px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.2);
}

.login-header {
  text-align: center;
  margin-bottom: 32px;
}

.brand {
  margin: 0;
  font-size: 32px;
  font-weight: 700;
  color: #667eea;
}

.subtitle {
  margin: 8px 0 0;
  color: #6b7280;
  font-size: 14px;
}

.login-form {
  margin-bottom: 24px;
}

.login-btn {
  width: 100%;
  height: 44px;
  font-size: 16px;
}

.login-footer {
  text-align: center;
  font-size: 14px;
  color: #6b7280;
}

.link {
  color: #667eea;
  font-weight: 600;
  margin-left: 4px;
}

.link:hover {
  text-decoration: underline;
}

.admin-entry {
  text-align: center;
  margin-top: 16px;
  padding-top: 16px;
  border-top: 1px solid #f3f4f6;
}

.admin-link {
  font-size: 13px;
  color: #9ca3af;
  text-decoration: none;
}

.admin-link:hover {
  color: #667eea;
  text-decoration: underline;
}
</style>
