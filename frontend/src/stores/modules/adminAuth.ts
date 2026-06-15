import { defineStore } from 'pinia';

interface AdminInfo {
  adminId: number;
  adminName: string;
  realName: string;
}

export const useAdminAuthStore = defineStore('adminAuth', {
  state: () => ({
    token: localStorage.getItem('adminToken') || '',
    adminInfo: null as AdminInfo | null,
  }),

  getters: {
    isLoggedIn: (state) => !!state.token,
  },

  actions: {
    async login(adminName: string, password: string) {
      try {
        const res = await fetch('/api/v1/admin/login', {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({ adminName, password }),
        });
        const data = await res.json();

        if (data.code === 200 && data.data) {
          const rawToken = data.data.token;
          const token = rawToken.startsWith('Bearer ') ? rawToken.substring(7) : rawToken;
          this.token = token;
          this.adminInfo = {
            adminId: data.data.adminId,
            adminName: data.data.adminName,
            realName: data.data.realName,
          };
          localStorage.setItem('adminToken', token);
          return { success: true };
        }
        return { success: false, message: data.message };
      } catch (error: any) {
        return { success: false, message: error.message };
      }
    },

    logout() {
      this.token = '';
      this.adminInfo = null;
      localStorage.removeItem('adminToken');
    },
  },
});
