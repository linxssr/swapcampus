import type { RouteRecordRaw } from 'vue-router';

export const adminRoutes: RouteRecordRaw[] = [
  {
    path: '/admin/login',
    name: 'AdminLogin',
    component: () => import('@/views/admin/auth/AdminLoginView.vue'),
  },
  {
    path: '/admin',
    component: () => import('@/layouts/AdminLayout.vue'),
    meta: { requiresAuth: true, role: 'admin' },
    redirect: '/admin/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'AdminDashboard',
        component: () => import('@/views/admin/dashboard/DashboardView.vue'),
      },
      {
        path: 'items',
        name: 'AdminItemAudit',
        component: () => import('@/views/admin/item/ItemAuditView.vue'),
      },
      {
        path: 'users',
        name: 'AdminUserManage',
        component: () => import('@/views/admin/user/UserManageView.vue'),
      },
      {
        path: 'reports',
        name: 'AdminReportManage',
        component: () => import('@/views/admin/report/ReportManageView.vue'),
      },
      {
        path: 'categories',
        name: 'AdminCategoryManage',
        component: () => import('@/views/admin/category/CategoryManageView.vue'),
      },
    ],
  },
];
