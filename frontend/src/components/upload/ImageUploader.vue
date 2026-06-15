<template>
  <div class="image-uploader">
    <div class="image-list">
      <div
        v-for="(url, index) in modelValue"
        :key="url"
        class="image-item"
      >
        <img :src="url" class="preview-img" />
        <div class="image-mask">
          <el-icon class="delete-icon" @click.stop="handleRemove(index)"><Delete /></el-icon>
        </div>
        <span v-if="index === 0" class="cover-tag">封面</span>
      </div>

      <el-upload
        v-if="modelValue.length < maxCount"
        class="upload-trigger"
        :auto-upload="false"
        :show-file-list="false"
        accept="image/*"
        :on-change="handleChange"
      >
        <div class="upload-placeholder">
          <el-icon v-if="!uploading" class="plus-icon"><Plus /></el-icon>
          <el-icon v-else class="loading-icon"><Loading /></el-icon>
          <span class="upload-text">{{ uploading ? '上传中...' : '添加图片' }}</span>
        </div>
      </el-upload>
    </div>

    <div class="upload-tip">
      最多上传 {{ maxCount }} 张，第一张为封面图，支持 jpg/png/gif，单张不超过 5MB
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import { ElMessage } from 'element-plus';
import { Plus, Delete, Loading } from '@element-plus/icons-vue';
import type { UploadFile } from 'element-plus';
import { uploadItemImage } from '@/api/modules/item';

const props = withDefaults(defineProps<{
  modelValue: string[];
  maxCount?: number;
}>(), {
  maxCount: 6,
});

const emit = defineEmits<{
  'update:modelValue': [urls: string[]];
}>();

const uploading = ref(false);

async function handleChange(file: UploadFile) {
  if (!file.raw) return;

  if (!file.raw.type.startsWith('image/')) {
    ElMessage.error('只能上传图片文件');
    return;
  }
  if (file.raw.size > 5 * 1024 * 1024) {
    ElMessage.error('图片大小不能超过 5MB');
    return;
  }

  uploading.value = true;
  try {
    const formData = new FormData();
    formData.append('file', file.raw);
    const res = await uploadItemImage(formData);

    console.log('上传响应:', res);

    if (res.code === 200 && res.data) {
      emit('update:modelValue', [...props.modelValue, res.data]);
      ElMessage.success('图片上传成功');
    } else {
      ElMessage.error(res.msg || '上传失败');
    }
  } catch (error) {
    console.error('上传异常:', error);
    ElMessage.error('图片上传失败，请重试');
  } finally {
    uploading.value = false;
  }
}

function handleRemove(index: number) {
  const updated = props.modelValue.filter((_, i) => i !== index);
  emit('update:modelValue', updated);
}
</script>

<style scoped>
.image-uploader {
  width: 100%;
}

.image-list {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.image-item {
  position: relative;
  width: 100px;
  height: 100px;
  border-radius: 6px;
  overflow: hidden;
  border: 1px solid #e5e7eb;
}

.preview-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
}

.image-mask {
  position: absolute;
  inset: 0;
  background: rgba(0, 0, 0, 0.4);
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transition: opacity 0.2s;
}

.image-item:hover .image-mask {
  opacity: 1;
}

.delete-icon {
  font-size: 22px;
  color: #fff;
  cursor: pointer;
}

.cover-tag {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  background: rgba(37, 99, 235, 0.75);
  color: #fff;
  font-size: 11px;
  text-align: center;
  padding: 2px 0;
}

.upload-trigger {
  width: 100px;
  height: 100px;
}

.upload-trigger :deep(.el-upload) {
  width: 100px;
  height: 100px;
  border: 1px dashed #d1d5db;
  border-radius: 6px;
  cursor: pointer;
  transition: border-color 0.2s;
}

.upload-trigger :deep(.el-upload:hover) {
  border-color: #2563eb;
}

.upload-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 4px;
  color: #9ca3af;
}

.plus-icon {
  font-size: 22px;
}

.loading-icon {
  font-size: 22px;
  animation: spin 1s linear infinite;
}

.upload-text {
  font-size: 12px;
}

.upload-tip {
  margin-top: 8px;
  font-size: 12px;
  color: #9ca3af;
}

@keyframes spin {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}
</style>
