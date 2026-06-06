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
