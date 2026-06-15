import { defineStore } from 'pinia';
import type { UserInfo, MyItem, MyOrder } from '@/types/auth';
import { loginApi, getUserInfoApi, getMyItemsApi, getMyOrdersApi } from '@/api/modules/user';
import { StorageKey } from '@/constants/storage';

export const useAuthStore = defineStore('auth', {
  state: () => ({
    token: localStorage.getItem(StorageKey.Token) || '',
    userInfo: null as UserInfo | null,
    myItems: [] as MyItem[],
    myOrders: [] as MyOrder[],
  }),

  getters: {
    isLoggedIn: (state) => !!state.token,
    displayName: (state) => state.userInfo?.username || state.userInfo?.studentId || '用户',
    creditScore: (state) => state.userInfo?.creditScore || 0,
    isAccountNormal: (state) => state.userInfo?.status === 1,
  },

  actions: {
    async login(studentId: string, password: string) {
      try {
        const res = await loginApi({ studentId, password });

        if (res.code !== 200) {
          return {
            success: false,
            message: res.message || '登录失败',
          };
        }

        const rawToken = res.data.token;
        const token = rawToken.startsWith('Bearer ') ? rawToken.substring(7) : rawToken;

        const userInfo: UserInfo = {
          userId: res.data.userId,
          username: res.data.username,
          studentId: studentId,
          creditScore: res.data.creditScore,
          status: 1,
          avatar: '',
          phone: '',
          createTime: res.data.createTime,
        };

        this.token = token;
        this.userInfo = userInfo;
        localStorage.setItem(StorageKey.Token, token);

        await this.fetchUserInfo();

        return { success: true, data: res.data };
      } catch (error: any) {
        return {
          success: false,
          message: error.response?.data?.message || error.message || '登录失败',
        };
      }
    },

    async fetchUserInfo() {
      try {
        const token = this.token || localStorage.getItem(StorageKey.Token);
        if (!token) {
          return null;
        }

        const res = await getUserInfoApi();

        if (res.code === 200 && res.data) {
          this.userInfo = {
            ...this.userInfo,
            ...res.data,
          };
          return this.userInfo;
        }
        return null;
      } catch (error) {
        console.error('获取用户信息失败', error);
        return null;
      }
    },

    async fetchMyItems() {
      try {
        const res = await getMyItemsApi();
        if (res.code === 200 && res.data) {
          this.myItems = res.data;
          return this.myItems;
        }
        return [];
      } catch (error) {
        console.error('获取我的发布失败', error);
        return [];
      }
    },

    async fetchMyOrders(type: 'buy' | 'sell' = 'buy') {
      try {
        const res = await getMyOrdersApi({ type });
        if (res.code === 200 && res.data) {
          this.myOrders = res.data;
          return this.myOrders;
        }
        return [];
      } catch (error) {
        console.error('获取我的订单失败', error);
        return [];
      }
    },

    updateUserInfo(data: Partial<UserInfo>) {
      if (this.userInfo) {
        this.userInfo = { ...this.userInfo, ...data };
      }
    },

    logout() {
      this.token = '';
      this.userInfo = null;
      this.myItems = [];
      this.myOrders = [];
      localStorage.removeItem(StorageKey.Token);
    },

    // 兼容旧代码的简化接口
    get isLogin() {
      return this.isLoggedIn;
    },

    get role() {
      return 'user' as const;
    },

    setAuth(nextToken: string, _nextRole: string) {
      this.token = nextToken;
      localStorage.setItem(StorageKey.Token, nextToken);
    },

    clearAuth() {
      this.logout();
    },
  },
});
