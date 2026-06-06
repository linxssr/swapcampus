<template>
  <div class="image-uploader">
    <el-upload list-type="picture-card" :auto-upload="false">
      <el-icon><Plus /></el-icon>
    </el-upload>
  </div>
</template>

<script setup lang="ts">
import { Plus } from "@element-plus/icons-vue";
</script>
