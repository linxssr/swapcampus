export interface ItemVO {
  itemId: number;
  userId: number;
  categoryId: number;
  categoryName: string;
  title: string;
  description: string;
  price: number;
  quality: number;
  qualityDesc: string;
  coverUrl: string;
  imageUrls?: string[];
  auditStatus: number;
  auditStatusDesc: string;
  publishStatus: number;
  createTime: string;
  updateTime: string;
  sellerName: string;
  sellerCreditScore: number;
}

export interface ItemDetailVO {
  itemId: number;
  userId: number;
  sellerName: string;
  sellerCreditScore: number;
  categoryId: number;
  categoryName: string;
  title: string;
  description: string;
  price: number;
  quality: number;
  qualityDesc: string;
  coverUrl: string;
  imageUrls: string[];
  auditStatus: number;
  auditStatusDesc: string;
  publishStatus: number;
  createTime: string;
  updateTime: string;
  isOwner: boolean;
}

export interface ItemPublishDTO {
  title: string;
  description?: string;
  price: number;
  quality: number;
  categoryId: number;
  coverUrl: string;
  imageUrls?: string[];
}

export interface ItemUpdateDTO {
  itemId: number;
  title: string;
  description?: string;
  price: number;
  quality: number;
  categoryId: number;
  coverUrl: string;
  imageUrls?: string[];
}

export interface CategoryVO {
  categoryId: number;
  categoryName: string;
}

export const QUALITY_MAP: Record<number, string> = {
  1: '全新',
  2: '几乎全新',
  3: '轻微使用痕迹',
  4: '明显使用痕迹',
};
