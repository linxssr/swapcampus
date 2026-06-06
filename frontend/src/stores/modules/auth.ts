import { defineStore } from 'pinia';
import { computed, ref } from 'vue';
import { StorageKey } from '@/constants/storage';
import type { UserRole } from '@/types/auth';

export const useAuthStore = defineStore('auth', () => {
  const token = ref(localStorage.getItem(StorageKey.Token) || '');
  const role = ref<UserRole>((localStorage.getItem(StorageKey.Role) as UserRole) || 'user');

  const isLogin = computed(() => Boolean(token.value));

  function setAuth(nextToken: string, nextRole: UserRole) {
    token.value = nextToken;
    role.value = nextRole;
    localStorage.setItem(StorageKey.Token, nextToken);
    localStorage.setItem(StorageKey.Role, nextRole);
  }

  function clearAuth() {
    token.value = '';
    role.value = 'user';
    localStorage.removeItem(StorageKey.Token);
    localStorage.removeItem(StorageKey.Role);
  }

  return {
    token,
    role,
    isLogin,
    setAuth,
    clearAuth,
  };
});
