import axios, { type AxiosRequestConfig } from 'axios';
import { ElMessage } from 'element-plus';
import { StorageKey } from '@/constants/storage';
import type { ApiResult } from '@/types/api';

const request = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL,
  timeout: 10000,
});

request.interceptors.request.use((config) => {
  const token = localStorage.getItem(StorageKey.Token);

  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }

  return config;
});

request.interceptors.response.use(
  (response) => response.data,
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
