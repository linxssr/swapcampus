<template>
  <div class="page-container">
    <PageHeader
      :title="isEdit ? '编辑商品' : '发布闲置'"
      :description="isEdit ? '修改商品信息后需重新审核' : '填写商品信息，审核通过后即可上架'"
    />

    <div class="publish-layout">
      <!-- 左栏：主要信息 -->
      <div class="publish-main">
        <el-form
          ref="formRef"
          :model="form"
          :rules="rules"
          label-position="top"
          class="publish-form"
        >
          <el-form-item label="商品标题" prop="title">
            <el-input
              v-model="form.title"
              placeholder="简洁明确的标题更容易卖出哦"
              maxlength="100"
              show-word-limit
              size="large"
            />
          </el-form-item>

          <div class="form-row">
            <el-form-item label="商品分类" prop="categoryId" class="form-col">
              <el-select v-model="form.categoryId" placeholder="请选择商品分类" class="full-width" size="large">
                <el-option
                  v-for="cat in categoryList"
                  :key="cat.categoryId"
                  :label="cat.categoryName"
                  :value="cat.categoryId"
                />
              </el-select>
            </el-form-item>

            <el-form-item label="价格（元）" prop="price" class="form-col">
              <el-input-number
                v-model="form.price"
                :min="0.01"
                :precision="2"
                :controls="false"
                placeholder="请输入价格"
                class="full-width"
                size="large"
              />
            </el-form-item>
          </div>

          <el-form-item label="商品成色" prop="quality">
            <el-radio-group v-model="form.quality" class="quality-group">
              <el-radio :value="1" class="quality-radio">
                <span class="quality-name">全新</span>
                <span class="quality-desc">未使用，全新状态</span>
              </el-radio>
              <el-radio :value="2" class="quality-radio">
                <span class="quality-name">几乎全新</span>
                <span class="quality-desc">轻微使用，无明显痕迹</span>
              </el-radio>
              <el-radio :value="3" class="quality-radio">
                <span class="quality-name">轻微使用痕迹</span>
                <span class="quality-desc">正常使用，有少量痕迹</span>
              </el-radio>
              <el-radio :value="4" class="quality-radio">
                <span class="quality-name">明显使用痕迹</span>
                <span class="quality-desc">较多使用，功能完好</span>
              </el-radio>
            </el-radio-group>
          </el-form-item>

          <el-form-item label="商品描述" prop="description">
            <el-input
              v-model="form.description"
              type="textarea"
              :rows="6"
              placeholder="详细描述商品信息：品牌型号、入手渠道、使用感受、转手原因等，描述越详细越容易成交"
              maxlength="2000"
              show-word-limit
            />
          </el-form-item>
        </el-form>
      </div>

      <!-- 右栏：图片上传 + 提交 -->
      <div class="publish-side">
        <div class="side-card">
          <div class="side-card-title">商品图片</div>
          <el-form ref="imgFormRef" :model="form" :rules="rules">
            <el-form-item prop="imageUrls">
              <ImageUploader v-model="form.imageUrls" />
            </el-form-item>
          </el-form>
          <p class="img-tip">首张图片将作为封面，最多上传 9 张</p>
        </div>

        <div class="side-card submit-card">
          <el-button
            type="primary"
            size="large"
            class="submit-btn"
            :loading="submitLoading"
            @click="handleSubmit"
          >
            {{ isEdit ? '保存修改' : '提交发布' }}
          </el-button>
          <el-button size="large" class="submit-btn" @click="handleCancel">取消</el-button>
          <div class="submit-tip">
            <el-icon><InfoFilled /></el-icon>
            <span v-if="isEdit">保存后商品将重新进入审核状态</span>
            <span v-else>发布后进入审核状态，审核通过后上架</span>
          </div>
        </div>
      </div>
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
const imgFormRef = ref<FormInstance>();
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
  const [mainValid, imgValid] = await Promise.all([
    formRef.value?.validate().catch(() => false),
    imgFormRef.value?.validate().catch(() => false),
  ]);
  if (!mainValid || !imgValid) return;

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
.publish-layout {
  display: grid;
  grid-template-columns: 1fr 320px;
  gap: 24px;
  align-items: start;
}

.publish-main {
  background: #fff;
  border-radius: 16px;
  padding: 32px 36px;
}

.publish-form {
  width: 100%;
}

.form-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20px;
}

.form-col {
  width: 100%;
}

.full-width {
  width: 100%;
}

.quality-group {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 10px;
  width: 100%;
}

.quality-radio {
  display: flex;
  align-items: center;
  gap: 0;
  border: 1px solid #e5e7eb;
  border-radius: 10px;
  padding: 12px 14px;
  margin: 0;
  cursor: pointer;
  transition: border-color 0.2s, background 0.2s;
  height: auto;
}

.quality-radio:hover {
  border-color: #93c5fd;
  background: #eff6ff;
}

:deep(.quality-radio.el-radio.is-checked) {
  border-color: #2563eb;
  background: #eff6ff;
}

:deep(.quality-radio .el-radio__label) {
  display: flex;
  flex-direction: column;
  gap: 2px;
  padding-left: 8px;
}

.quality-name {
  font-size: 14px;
  font-weight: 600;
  color: #1f2937;
  line-height: 1.4;
}

.quality-desc {
  font-size: 12px;
  color: #9ca3af;
  line-height: 1.4;
  white-space: nowrap;
}

.publish-side {
  display: flex;
  flex-direction: column;
  gap: 16px;
  position: sticky;
  top: 84px;
}

.side-card {
  background: #fff;
  border-radius: 16px;
  padding: 24px;
}

.side-card-title {
  font-size: 15px;
  font-weight: 600;
  color: #1f2937;
  margin-bottom: 16px;
}

.img-tip {
  margin: 8px 0 0;
  font-size: 12px;
  color: #9ca3af;
  text-align: center;
}

.submit-card {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.submit-btn {
  width: 100%;
}

.submit-tip {
  display: flex;
  align-items: flex-start;
  gap: 6px;
  font-size: 12px;
  color: #9ca3af;
  line-height: 1.5;
  padding-top: 4px;
}

.submit-tip .el-icon {
  margin-top: 1px;
  flex-shrink: 0;
}

@media (max-width: 900px) {
  .publish-layout {
    grid-template-columns: 1fr;
  }

  .publish-side {
    position: static;
  }

  .quality-group {
    grid-template-columns: 1fr;
  }
}
</style>
