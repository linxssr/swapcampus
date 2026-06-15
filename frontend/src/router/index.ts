import { createRouter, createWebHistory } from 'vue-router';
import { adminRoutes } from './modules/admin';
import { userRoutes } from './modules/user';
import { useAuthStore } from '@/stores/modules/auth';

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    ...userRoutes,
    ...adminRoutes,
    {
      path: '/:pathMatch(.*)*',
      name: 'NotFound',
      component: () => import('@/views/NotFoundView.vue'),
    },
  ],
});

router.beforeEach((to) => {
  const authStore = useAuthStore();

  // 管理员路由：用独立的 adminToken 校验
  if (to.meta.role === 'admin') {
    const adminToken = localStorage.getItem('adminToken');
    if (!adminToken) {
      return '/admin/login';
    }
    return true;
  }

  if (!to.meta.requiresAuth) {
    return true;
  }

  if (!authStore.token) {
    return '/login';
  }

  return true;
});

export default router;
