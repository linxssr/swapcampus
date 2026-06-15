<template>
  <div class="page-container">
    <PageHeader
      :title="isEdit ? '编辑商品' : '发布闲置'"
      :description="isEdit ? '修改商品信息后需重新审核' : '填写商品信息，审核通过后即可上架'"
    />

    <div class="publish-form-wrapper">
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="100px"
        class="publish-form"
      >
        <el-form-item label="商品标题" prop="title">
          <el-input
            v-model="form.title"
            placeholder="简洁明确的标题更容易卖出哦"
            maxlength="100"
            show-word-limit
          />
        </el-form-item>

        <el-form-item label="商品分类" prop="categoryId">
          <el-select v-model="form.categoryId" placeholder="请选择商品分类" class="full-width">
            <el-option
              v-for="cat in categoryList"
              :key="cat.categoryId"
              :label="cat.categoryName"
              :value="cat.categoryId"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="价格" prop="price">
          <el-input-number
            v-model="form.price"
            :min="0.01"
            :precision="2"
            :controls="false"
            placeholder="设置一个合理的价格"
            class="price-input"
          />
          <span class="price-unit">元</span>
        </el-form-item>

        <el-form-item label="商品成色" prop="quality">
          <el-radio-group v-model="form.quality" class="quality-group">
            <el-radio :value="1">全新</el-radio>
            <el-radio :value="2">几乎全新</el-radio>
            <el-radio :value="3">轻微使用痕迹</el-radio>
            <el-radio :value="4">明显使用痕迹</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item label="商品描述" prop="description">
          <el-input
            v-model="form.description"
            type="textarea"
            :rows="5"
            placeholder="详细描述商品信息：品牌型号、入手渠道、使用感受、转手原因等"
            maxlength="2000"
            show-word-limit
          />
        </el-form-item>

        <el-form-item label="商品图片" prop="imageUrls">
          <ImageUploader v-model="form.imageUrls" />
        </el-form-item>

        <el-form-item>
          <div class="form-actions">
            <el-button @click="handleCancel">取消</el-button>
            <el-button type="primary" :loading="submitLoading" @click="handleSubmit">
              {{ isEdit ? '保存修改' : '提交发布' }}
            </el-button>
          </div>
          <div class="submit-tip">
            <el-icon><InfoFilled /></el-icon>
            <span v-if="isEdit">保存后商品将重新进入审核状态，请等待管理员审核通过</span>
            <span v-else>发布后商品将进入审核状态，审核通过后即可在列表中展示</span>
          </div>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<script setup lang="ts">
import { InfoFilled } from '@element-plus/icons-vue';
import { ElMessage } from 'element-plus';
import { computed, onMounted, reactive, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import type { FormInstance, FormRules } from 'element-plus';
import PageHeader from '@/components/common/PageHeader.vue';
import ImageUploader from '@/components/upload/ImageUploader.vue';
import { getCategoryList } from '@/api/modules/category';
import { getItemDetail, publishItem, updateItem } from '@/api/modules/item';
import type { CategoryVO, ItemPublishDTO } from '@/types/item';

const route = useRoute();
const router = useRouter();
const formRef = ref<FormInstance>();
const submitLoading = ref(false);
const categoryList = ref<CategoryVO[]>([]);

const isEdit = computed(() => Boolean(route.query.itemId));
const editingItemId = computed(() => route.query.itemId as string | undefined);

const form = reactive<ItemPublishDTO>({
  title: '',
  categoryId: 0,
  price: 0.01,
  quality: 1,
  description: '',
  coverUrl: '',
  imageUrls: [],
});

const rules: FormRules = {
  title: [
    { required: true, message: '请输入商品标题', trigger: 'blur' },
    { min: 2, max: 100, message: '标题长度在 2 到 100 个字符', trigger: 'blur' },
  ],
  categoryId: [{ required: true, message: '请选择商品分类', trigger: 'change' }],
  price: [
    { required: true, message: '请输入商品价格', trigger: 'blur' },
    {
      validator: (_rule, value, callback) => {
        if (value <= 0) callback(new Error('价格必须大于 0'));
        else callback();
      },
      trigger: 'blur',
    },
  ],
  quality: [{ required: true, message: '请选择商品成色', trigger: 'change' }],
  imageUrls: [
    {
      validator: (_rule, value, callback) => {
        if (!value || value.length === 0) callback(new Error('请至少上传一张商品图片'));
        else callback();
      },
      trigger: 'change',
    },
  ],
};

async function loadCategories() {
  try {
    const res = await getCategoryList();
    if (res.data) {
      categoryList.value = res.data;
    }
  } catch {
    ElMessage.error('加载分类失败');
  }
}

async function loadItemForEdit(itemId: string) {
  try {
    const res = await getItemDetail(itemId);
    if (res.data) {
      const item = res.data;
      form.title = item.title;
      form.categoryId = item.categoryId;
      form.price = Number(item.price);
      form.quality = item.quality;
      form.description = item.description || '';
      form.coverUrl = item.coverUrl;
      form.imageUrls = item.imageUrls?.length ? item.imageUrls : [item.coverUrl];
    }
  } catch {
    ElMessage.error('加载商品信息失败');
    router.push('/profile');
  }
}

async function handleSubmit() {
  const valid = await formRef.value?.validate().catch(() => false);
  if (!valid) return;

  if (!form.imageUrls?.length) {
    ElMessage.warning('请至少上传一张商品图片');
    return;
  }

  submitLoading.value = true;
  try {
    if (isEdit.value && editingItemId.value) {
      await updateItem({
        itemId: Number(editingItemId.value),
        title: form.title,
        categoryId: form.categoryId,
        price: form.price,
        quality: form.quality,
        description: form.description || undefined,
        coverUrl: form.imageUrls[0],
        imageUrls: form.imageUrls,
      });
      ElMessage.success('保存成功，请等待管理员审核');
    } else {
      await publishItem({
        title: form.title,
        categoryId: form.categoryId,
        price: form.price,
        quality: form.quality,
        description: form.description || undefined,
        coverUrl: form.imageUrls[0],
        imageUrls: form.imageUrls,
      });
      ElMessage.success('发布成功，请等待管理员审核');
    }
    router.push('/profile');
  } catch {
    ElMessage.error(isEdit.value ? '保存失败，请重试' : '发布失败，请重试');
  } finally {
    submitLoading.value = false;
  }
}

function handleCancel() {
  router.back();
}

onMounted(async () => {
  await loadCategories();
  if (isEdit.value && editingItemId.value) {
    await loadItemForEdit(editingItemId.value);
  }
});
</script>

<style scoped>
.publish-form-wrapper {
  background: #fff;
  border-radius: 12px;
  padding: 32px;
  max-width: 720px;
}

.publish-form {
  max-width: 600px;
}

.full-width {
  width: 100%;
}

.price-input {
  width: 200px;
}

.price-unit {
  margin-left: 8px;
  color: #6b7280;
  font-size: 14px;
}

.quality-group {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.quality-option {
  display: flex;
  flex-direction: column;
}

.quality-name {
  font-weight: 600;
  color: #1f2937;
}

.quality-desc {
  font-size: 12px;
  color: #9ca3af;
}

.form-actions {
  display: flex;
  gap: 12px;
}

.submit-tip {
  margin-top: 12px;
  display: flex;
  align-items: flex-start;
  gap: 6px;
  font-size: 13px;
  color: #9ca3af;
  line-height: 1.5;
}

.submit-tip .el-icon {
  margin-top: 2px;
  flex-shrink: 0;
}
</style>
