export function isImageFile(file: File) {
  return file.type.startsWith('image/');
}

/**
 * 将 MinIO 绝对 URL 转为走 Vite 代理的相对路径，避免跨域 ORB 拦截。
 * 旧数据可能存的是 http://localhost:9000/bucket/... 格式，
 * 新数据后端已返回 /minio/bucket/... 相对路径，两种格式都能处理。
 */
export function toProxiedUrl(url: string | null | undefined): string {
  if (!url) return '';
  // 已经是相对路径，直接返回
  if (url.startsWith('/')) return url;
  try {
    const parsed = new URL(url);
    // 只代理 MinIO 地址（localhost:9000 或配置的 MinIO endpoint）
    // 外部图床（picsum.photos 等）直接原样返回
    const isMinioUrl =
      parsed.hostname === 'localhost' ||
      parsed.hostname === '127.0.0.1' ||
      parsed.port === '9000';
    if (!isMinioUrl) return url;
    return '/minio' + parsed.pathname;
  } catch {
    return url;
  }
}

export function toFormData(file: File, fieldName = 'file') {
  const formData = new FormData();
  formData.append(fieldName, file);
  return formData;
}
