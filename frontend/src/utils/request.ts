import axios, { type AxiosRequestConfig } from 'axios';
import { ElMessage } from 'element-plus';
import { StorageKey } from '@/constants/storage';
import type { ApiResult } from '@/types/api';
import { toProxiedUrl } from '@/utils/upload';

const request = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL,
  timeout: 10000,
});

request.interceptors.request.use((config) => {
  const isAdminRequest = config.url?.includes('/admin/');
  const adminToken = localStorage.getItem('adminToken');
  const userToken = localStorage.getItem(StorageKey.Token);

  if (isAdminRequest && adminToken) {
    config.headers.Authorization = `Bearer ${adminToken}`;
  } else if (userToken) {
    config.headers.Authorization = `Bearer ${userToken}`;
  }

  return config;
});

const IMAGE_URL_KEYS = ['coverUrl', 'imageUrls', 'avatar', 'peerAvatar', 'sellerAvatar'];

function rewriteImageUrls(obj: unknown): void {
  if (!obj || typeof obj !== 'object') return;
  if (Array.isArray(obj)) {
    obj.forEach(rewriteImageUrls);
    return;
  }
  const record = obj as Record<string, unknown>;
  for (const key of Object.keys(record)) {
    const val = record[key];
    if (IMAGE_URL_KEYS.includes(key)) {
      if (typeof val === 'string') {
        record[key] = toProxiedUrl(val);
      } else if (Array.isArray(val)) {
        record[key] = val.map((u) => (typeof u === 'string' ? toProxiedUrl(u) : u));
      }
    } else if (val && typeof val === 'object') {
      rewriteImageUrls(val);
    }
  }
}

request.interceptors.response.use(
  (response) => {
    rewriteImageUrls(response.data);
    return response.data;
  },
  (error) => {
    ElMessage.error(error?.response?.data?.msg || error.message || '请求失败');
    return Promise.reject(error);
  },
);

export function get<T>(url: string, config?: AxiosRequestConfig) {
  return request.get<unknown, ApiResult<T>>(url, config);
}

export function post<T>(url: string, data?: unknown, config?: AxiosRequestConfig) {
  return request.post<unknown, ApiResult<T>>(url, data, config);
}

export function put<T>(url: string, data?: unknown, config?: AxiosRequestConfig) {
  return request.put<unknown, ApiResult<T>>(url, data, config);
}

export function del<T>(url: string, config?: AxiosRequestConfig) {
  return request.delete<unknown, ApiResult<T>>(url, config);
}

export default request;
