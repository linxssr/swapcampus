<template>
  <el-pagination layout="prev, pager, next, total" :total="total" />
</template>

<script setup lang="ts">
defineProps<{
  total: number;
}>();
</script>
