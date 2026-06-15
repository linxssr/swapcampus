import { get } from '@/utils/request';
import type { CategoryVO } from '@/types/item';

export function getCategoryList() {
  return get<CategoryVO[]>('/category/list');
}
