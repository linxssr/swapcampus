<template>
  <el-upload
    class="upload-img"
    :auto-upload="false"
    :show-file-list="false"
    :file-list="[]"
    accept="image/*"
    :on-change="handleChange"
  >
    <slot>
      <el-button>图片</el-button>
    </slot>
  </el-upload>
</template>

<script setup lang="ts">
import type { UploadFile } from 'element-plus';
import { ElMessage } from 'element-plus';
import { uploadChatImage } from '@/api/modules/chat';

const emit = defineEmits<{
  uploaded: [url: string];
}>();

async function handleChange(file: UploadFile) {
  if (!file.raw) {
    return;
  }

  const formData = new FormData();
  formData.append('file', file.raw);

  try {
    const result = await uploadChatImage(formData);
    if (result.data) {
      emit('uploaded', result.data);
    } else {
      ElMessage.error('图片上传失败：未获取到地址');
    }
  } catch {
    ElMessage.error('图片上传失败');
  }
}
</script>

<style scoped>
.upload-img {
  display: inline-flex;
}
</style>
