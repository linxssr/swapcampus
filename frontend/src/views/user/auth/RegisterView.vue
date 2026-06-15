<template>
  <div class="register-page">
    <div class="register-card">
      <div class="register-header">
        <h1 class="brand">SwapCampus</h1>
        <p class="subtitle">校园闲置交易平台 · 注册</p>
      </div>

      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        class="register-form"
        @submit.prevent="handleRegister"
      >
        <el-form-item prop="studentId">
          <el-input
            v-model="form.studentId"
            placeholder="请输入学号（作为唯一账号）"
            size="large"
            prefix-icon="Postcard"
          />
        </el-form-item>

        <el-form-item prop="username">
          <el-input
            v-model="form.username"
            placeholder="请输入昵称"
            size="large"
            prefix-icon="User"
          />
        </el-form-item>

        <el-form-item prop="password">
          <el-input
            v-model="form.password"
            type="password"
            placeholder="请输入密码（至少6位）"
            size="large"
            prefix-icon="Lock"
            show-password
          />
        </el-form-item>

        <el-form-item prop="confirmPassword">
          <el-input
            v-model="form.confirmPassword"
            type="password"
            placeholder="请确认密码"
            size="large"
            prefix-icon="Lock"
            show-password
          />
        </el-form-item>

        <el-form-item prop="phone">
          <el-input
            v-model="form.phone"
            placeholder="请输入手机号（选填）"
            size="large"
            prefix-icon="Phone"
          />
        </el-form-item>

        <el-form-item>
          <el-button
            type="primary"
            size="large"
            class="register-btn"
            :loading="loading"
            native-type="submit"
          >
            注册
          </el-button>
        </el-form-item>
      </el-form>

      <div class="register-footer">
        <span>已有账号？</span>
        <RouterLink to="/login" class="link">立即登录</RouterLink>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ElMessage } from 'element-plus';
import { reactive, ref } from 'vue';
import { useRouter } from 'vue-router';
import type { FormInstance, FormRules } from 'element-plus';
import { userRegister } from '@/api/modules/user';

const router = useRouter();
const formRef = ref<FormInstance>();
const loading = ref(false);

const form = reactive({
  studentId: '',
  username: '',
  password: '',
  confirmPassword: '',
  phone: '',
});

const validateConfirm = (_rule: unknown, value: string, callback: (error?: Error) => void) => {
  if (value !== form.password) {
    callback(new Error('两次输入的密码不一致'));
  } else {
    callback();
  }
};

const rules: FormRules = {
  studentId: [
    { required: true, message: '请输入学号', trigger: 'blur' },
    { min: 6, message: '学号至少6位', trigger: 'blur' },
  ],
  username: [
    { required: true, message: '请输入昵称', trigger: 'blur' },
    { min: 2, max: 20, message: '昵称长度在2-20个字符', trigger: 'blur' },
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码至少6位', trigger: 'blur' },
  ],
  confirmPassword: [
    { required: true, message: '请确认密码', trigger: 'blur' },
    { validator: validateConfirm, trigger: 'blur' },
  ],
  phone: [
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' },
  ],
};

async function handleRegister() {
  const valid = await formRef.value?.validate().catch(() => false);
  if (!valid) return;

  loading.value = true;
  try {
    const res = await userRegister({
      studentId: form.studentId,
      username: form.username,
      password: form.password,
      phone: form.phone || undefined,
    });
    
    if (res.code === 200) {
      ElMessage.success('注册成功，请登录');
      router.push('/login');
    } else {
      ElMessage.error(res.message || '注册失败');
    }
  } catch (error: any) {
    ElMessage.error(error.response?.data?.message || '注册失败，请稍后重试');
  } finally {
    loading.value = false;
  }
}
</script>

<style scoped>
.register-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--color-bg);
  background-image:
    repeating-linear-gradient(0deg, transparent, transparent 31px, rgba(45,48,71,0.05) 31px, rgba(45,48,71,0.05) 32px),
    repeating-linear-gradient(90deg, transparent, transparent 31px, rgba(45,48,71,0.05) 31px, rgba(45,48,71,0.05) 32px);
}

.register-card {
  width: 420px;
  padding: 40px;
  background: var(--color-surface);
  border: var(--border-pixel);
  border-radius: var(--radius-pixel);
  box-shadow: var(--shadow-hard);
}

.register-header {
  text-align: center;
  margin-bottom: 28px;
}

.brand {
  margin: 0;
  font-size: 30px;
  font-weight: 800;
  color: var(--color-text);
  letter-spacing: -1px;
}

.subtitle {
  margin: 8px 0 0;
  color: var(--color-text-sub);
  font-size: 13px;
  font-weight: 600;
}

.register-form { margin-bottom: 20px; }

.register-btn {
  width: 100%;
  height: 44px;
  font-size: 15px;
  font-weight: 700;
}

.register-footer {
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
</style>
