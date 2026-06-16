import { del, get, post, put } from '@/utils/request';
import type { ItemDetailVO, ItemPublishDTO, ItemUpdateDTO, ItemVO } from '@/types/item';

export function uploadItemImage(data: FormData) {
  return post<string>('/item/uploadImg', data);
}

export function publishItem(data: ItemPublishDTO) {
  return post<ItemVO>('/item/publish', data);
}

export function updateItem(data: ItemUpdateDTO) {
  return put<ItemVO>('/item/update', data);
}

export function deleteItem(id: number | string) {
  return del<void>(`/item/delete/${id}`);
}

export function getItemDetail(id: number | string) {
  return get<ItemDetailVO>(`/item/detail/${id}`);
}

export function getMyItems() {
  return get<ItemVO[]>('/item/my');
}

export function getItemsByCategory(categoryId: number | string) {
  return get<ItemVO[]>(`/item/cate/${categoryId}`);
}

export function searchItems(key: string) {
  return get<ItemVO[]>('/item/search', { params: { key } });
}

export function filterItems(params: {
  categoryId?: number;
  minPrice?: number;
  maxPrice?: number;
  quality?: number;
}) {
  return get<ItemVO[]>('/item/filter', { params });
}

export function recordBrowse(itemId: number | string) {
  return post<void>(`/item/browse/${itemId}`);
}

export function getRecommendItems(limit = 8) {
  return get<ItemVO[]>('/item/recommend', { params: { limit } });
}
