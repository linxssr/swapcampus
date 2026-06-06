import type { RouteRecordRaw } from 'vue-router';

export const userRoutes: RouteRecordRaw[] = [
  {
    path: '/',
    component: () => import('@/layouts/UserLayout.vue'),
    children: [
      {
        path: '',
        name: 'Home',
        component: () => import('@/views/user/home/HomeView.vue'),
      },
      {
        path: 'items',
        name: 'ItemList',
        component: () => import('@/views/user/item/ItemListView.vue'),
      },
      {
        path: 'items/:id',
        name: 'ItemDetail',
        component: () => import('@/views/user/item/ItemDetailView.vue'),
      },
      {
        path: 'publish',
        name: 'ItemPublish',
        component: () => import('@/views/user/item/ItemPublishView.vue'),
        meta: { requiresAuth: true },
      },
      {
        path: 'profile',
        name: 'Profile',
        component: () => import('@/views/user/profile/ProfileView.vue'),
        meta: { requiresAuth: true },
      },
      {
        path: 'orders',
        name: 'OrderList',
        component: () => import('@/views/user/order/OrderListView.vue'),
        meta: { requiresAuth: true },
      },
      {
        path: 'chat',
        name: 'Chat',
        component: () => import('@/views/user/chat/ChatView.vue'),
        children: [
          {
            path: 'room/:toUid',
            name: 'ChatRoom',
            component: () => import('@/views/user/chat/ChatRoomView.vue'),
          },
        ],
      },
    ],
  },
  {
    path: '/login',
    name: 'UserLogin',
    component: () => import('@/views/user/auth/LoginView.vue'),
  },
  {
    path: '/register',
    name: 'UserRegister',
    component: () => import('@/views/user/auth/RegisterView.vue'),
  },
];
