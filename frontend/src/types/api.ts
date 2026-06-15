export interface ApiResult<T = unknown> {
  code: number;
  msg: string;
  data: T;
}

export interface PageQuery {
  pageNum?: number;
  pageSize?: number;
}

export interface PageResult<T> {
  records: T[];
  total: number;
}

export interface Category {
  categoryId: number;
  categoryName: string;
  sort: number;
  status: number;
}

export interface Item {
  itemId: number;
  userId: number;
  categoryId: number;
  title: string;
  description: string;
  price: number;
  quality: number;
  coverUrl: string;
  auditStatus: number;
  auditAdminId?: number;
  publishStatus: number;
  createTime: string;
  updateTime: string;
}

export interface ItemVO extends Item {
  categoryName?: string;
  qualityDesc?: string;
  imageUrls?: string[];
  auditStatusDesc?: string;
  sellerName?: string;
  sellerCreditScore?: number;
  collectId?: number;
  collectTime?: string;
}

export interface ItemFilterParams {
  categoryId?: number;
  minPrice?: number;
  maxPrice?: number;
  quality?: number;
}

export const QUALITY_OPTIONS = [
  { label: '全新', value: 1 },
  { label: '几乎全新', value: 2 },
  { label: '轻微使用痕迹', value: 3 },
  { label: '明显使用痕迹', value: 4 },
] as const;
