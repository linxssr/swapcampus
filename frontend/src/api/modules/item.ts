import { del, get, post, put } from '@/utils/request';

export function uploadItemImage(data: FormData) {
  return post<string>('/item/uploadImg', data);
}

export function publishItem(data: unknown) {
  return post<void>('/item/publish', data);
}

export function updateItem(data: unknown) {
  return put<void>('/item/update', data);
}

export function deleteItem(id: number | string) {
  return del<void>(`/item/delete/${id}`);
}

export function getItemsByCategory(categoryId: number | string) {
  return get<unknown[]>(`/item/cate/${categoryId}`);
}

export function searchItems(key: string) {
  return get<unknown[]>('/item/search', { params: { key } });
}

export function filterItems(params: unknown) {
  return get<unknown[]>('/item/filter', { params });
}
