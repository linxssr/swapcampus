import { get } from '@/utils/request';

export function getCategoryList() {
  return get<unknown[]>('/category/list');
}
