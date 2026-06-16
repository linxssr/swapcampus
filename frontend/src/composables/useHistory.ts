import { useAuthStore } from '@/stores/modules/auth';
import { recordBrowse } from '@/api/modules/item';

const STORAGE_KEY = 'swap_browse_history';
const MAX_LOCAL = 50;

interface LocalHistoryEntry {
  itemId: number;
  categoryId: number;
  browseTime: number;
}

function readLocal(): LocalHistoryEntry[] {
  try {
    const raw = localStorage.getItem(STORAGE_KEY);
    return raw ? (JSON.parse(raw) as LocalHistoryEntry[]) : [];
  } catch {
    return [];
  }
}

function writeLocal(entries: LocalHistoryEntry[]): void {
  localStorage.setItem(STORAGE_KEY, JSON.stringify(entries));
}

export function useHistory() {
  const authStore = useAuthStore();

  async function track(itemId: number, categoryId: number) {
    // 1. 写 localStorage（无论是否登录）
    const list = readLocal().filter((e) => e.itemId !== itemId);
    list.unshift({ itemId, categoryId, browseTime: Date.now() });
    writeLocal(list.slice(0, MAX_LOCAL));

    // 2. 登录状态下同步上报后端
    if (authStore.isLoggedIn) {
      try {
        await recordBrowse(itemId);
      } catch {
        // 静默失败，不影响用户体验
      }
    }
  }

  return { track };
}
