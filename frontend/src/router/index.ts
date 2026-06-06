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

  if (!to.meta.requiresAuth) {
    return true;
  }

  if (!authStore.token) {
    return to.meta.role === 'admin' ? '/admin/login' : '/login';
  }

  if (to.meta.role && to.meta.role !== authStore.role) {
    return authStore.role === 'admin' ? '/admin/dashboard' : '/';
  }

  return true;
});

export default router;
