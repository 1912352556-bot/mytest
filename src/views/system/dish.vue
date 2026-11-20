<template>
  <div class="dish-management">
    <!-- 搜索区域 -->
    <el-card class="search-card" shadow="hover">
      <el-form :model="searchForm" inline>
        <el-form-item label="商品名称">
          <el-input v-model="searchForm.dishName" placeholder="请输入商品名称" clearable style="width: 180px" />
        </el-form-item>
        <el-form-item label="商品分类">
          <el-select v-model="searchForm.categoryId" placeholder="请选择分类" clearable style="width: 150px">
            <el-option
              v-for="item in categoryOptions"
              :key="item.id"
              :label="item.categoryName"
              :value="item.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="储存温层">
          <el-select v-model="searchForm.storageType" placeholder="请选择温层" clearable style="width: 130px">
            <el-option
              v-for="item in storageOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="请选择状态" clearable style="width: 120px">
            <el-option label="上架" :value="1" />
            <el-option label="下架" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="Search" @click="handleSearch">搜索</el-button>
          <el-button :icon="Refresh" @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 商品列表 -->
    <el-card class="table-card" shadow="hover">
      <template #header>
        <div class="card-header">
          <span>生鲜商品列表</span>
          <el-button type="primary" :icon="Plus" @click="handleAdd">新增商品</el-button>
        </div>
      </template>

      <el-table :data="dishList" v-loading="loading" border stripe>
        <el-table-column prop="id" label="ID" width="70" align="center" />
        <el-table-column prop="imageUrl" label="图片" width="90" align="center">
          <template #default="{ row }">
            <el-image
              v-if="row.imageUrl"
              :src="row.imageUrl"
              :preview-src-list="[row.imageUrl]"
              fit="cover"
              style="width: 56px; height: 56px; border-radius: 4px"
            />
            <span v-else class="empty-text">无图片</span>
          </template>
        </el-table-column>
        <el-table-column label="商品名称" min-width="160">
          <template #default="{ row }">
            <div class="goods-name">{{ row.dishName }}</div>
            <div class="goods-code">{{ row.dishCode }}</div>
          </template>
        </el-table-column>
        <el-table-column label="分类" width="110" align="center">
          <template #default="{ row }">
            <el-tag effect="plain">{{ getCategoryName(row.categoryId) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="spec" label="规格" width="110" align="center">
          <template #default="{ row }">{{ row.spec || '-' }}</template>
        </el-table-column>
        <el-table-column label="温层" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="getStorageTagType(row.storageType)" size="small">
              {{ getStorageName(row.storageType) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="保质期" width="90" align="center">
          <template #default="{ row }">
            <span :class="{ 'expire-soon': isShortShelfLife(row.shelfLifeDays) }">
              {{ row.shelfLifeDays ? row.shelfLifeDays + '天' : '-' }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="origin" label="产地" width="130" show-overflow-tooltip>
          <template #default="{ row }">{{ row.origin || '-' }}</template>
        </el-table-column>
        <el-table-column label="单价" width="100" align="right">
          <template #default="{ row }">
            <span class="price">¥{{ Number(row.price || 0).toFixed(2) }}</span>
            <span class="unit">/{{ row.unit }}</span>
          </template>
        </el-table-column>
        <el-table-column label="损耗率" width="90" align="center">
          <template #default="{ row }">
            <span>{{ row.lossRate != null ? row.lossRate + '%' : '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="库存" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="getStockTagType(row.stock)">{{ row.stock || 0 }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="80" align="center">
          <template #default="{ row }">
            <el-switch
              v-model="row.status"
              :active-value="1"
              :inactive-value="0"
              @change="handleStatusChange(row)"
            />
          </template>
        </el-table-column>
        <el-table-column prop="description" label="商品描述" min-width="180" show-overflow-tooltip />
        <el-table-column label="操作" width="150" align="center" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link :icon="Edit" @click="handleEdit(row)">编辑</el-button>
            <el-button type="danger" link :icon="Delete" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-wrapper">
        <el-pagination
          v-model:current-page="pagination.page"
          v-model:page-size="pagination.pageSize"
          :page-sizes="[10, 20, 50, 100]"
          :total="pagination.total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <!-- 新增/编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="720px"
      :close-on-click-modal="false"
    >
      <el-form
        ref="formRef"
        :model="formData"
        :rules="formRules"
        label-width="100px"
      >
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="商品名称" prop="dishName">
              <el-input v-model="formData.dishName" placeholder="如：有机小白菜" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="商品分类" prop="categoryId">
              <el-select v-model="formData.categoryId" placeholder="请选择分类" style="width: 100%">
                <el-option
                  v-for="item in categoryOptions"
                  :key="item.id"
                  :label="item.categoryName"
                  :value="item.id"
                />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="单价" prop="price">
              <el-input-number v-model="formData.price" :min="0" :precision="2" :step="0.5" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="单位" prop="unit">
              <el-select
                v-model="formData.unit"
                placeholder="请选择或输入单位"
                filterable
                allow-create
                default-first-option
                style="width: 100%"
              >
                <el-option v-for="u in unitOptions" :key="u" :label="u" :value="u" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="规格" prop="spec">
              <el-input v-model="formData.spec" placeholder="如：500g/份、30枚/盒" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="储存温层" prop="storageType">
              <el-radio-group v-model="formData.storageType">
                <el-radio v-for="item in storageOptions" :key="item.value" :value="item.value">
                  {{ item.label }}
                </el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="保质期" prop="shelfLifeDays">
              <el-input-number
                v-model="formData.shelfLifeDays"
                :min="1"
                :step="1"
                style="width: 100%"
              />
              <span class="form-tip">天</span>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="产地" prop="origin">
              <el-input v-model="formData.origin" placeholder="如：山东寿光" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="损耗率" prop="lossRate">
              <el-input-number
                v-model="formData.lossRate"
                :min="0"
                :max="100"
                :precision="2"
                :step="0.5"
                style="width: 100%"
              />
              <span class="form-tip">%</span>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="当前库存" prop="stock">
              <el-input-number v-model="formData.stock" :min="0" :step="1" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="状态" prop="status">
              <el-radio-group v-model="formData.status">
                <el-radio :value="1">上架</el-radio>
                <el-radio :value="0">下架</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="商品图片" prop="imageUrl">
          <el-upload
            class="avatar-uploader"
            action="#"
            :show-file-list="false"
            :auto-upload="false"
            :on-change="handleImageChange"
            :disabled="uploadLoading"
          >
            <img v-if="formData.imageUrl" :src="formData.imageUrl" class="avatar" />
            <el-icon v-else class="avatar-uploader-icon">
              <Plus v-if="!uploadLoading" />
              <span v-else>上传中...</span>
            </el-icon>
          </el-upload>
          <div class="form-tip block">支持jpg/png/gif格式，大小不超过2MB</div>
        </el-form-item>
        <el-form-item label="商品描述" prop="description">
          <el-input
            v-model="formData.description"
            type="textarea"
            :rows="3"
            placeholder="如：当日采摘，叶片脆嫩，冷链直发"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
/**
 * 生鲜商品管理
 * 维护蔬菜、水果、肉禽蛋、水产海鲜、乳品烘焙、粮油调味等生鲜商品，
 * 相比预制菜额外维护保质期、储存温层、产地、规格与损耗率等生鲜属性。
 */
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules, type UploadFile } from 'element-plus'
import { Search, Refresh, Plus, Edit, Delete } from '@element-plus/icons-vue'
import {
  getDishList,
  addDish,
  updateDish,
  deleteDish,
  updateDishStatus,
  getCategoryList
} from '@/api/dish'
import { uploadImage } from '@/api/file'

/* 储存温层选项：1-常温 2-冷藏 3-冷冻 */
const storageOptions = [
  { label: '常温', value: 1 },
  { label: '冷藏', value: 2 },
  { label: '冷冻', value: 3 }
]

/* 常用生鲜计量单位 */
const unitOptions = ['份', '盒', 'kg', '袋', '瓶', '条']

/* 搜索表单 */
const searchForm = reactive({
  dishName: '',
  categoryId: '',
  storageType: undefined as number | undefined,
  status: undefined as number | undefined
})

/* 分页 */
const pagination = reactive({
  page: 1,
  pageSize: 10,
  total: 0
})

/* 状态 */
const loading = ref(false)
const dialogVisible = ref(false)
const submitLoading = ref(false)
const isEdit = ref(false)
const currentDishId = ref<number>()

/* 表单引用 */
const formRef = ref<FormInstance>()

/* 商品分类选项（来自后台分类表，避免前端写死品类） */
const categoryOptions = ref<DishCategory[]>([])

/* 对话框标题 */
const dialogTitle = computed(() => isEdit.value ? '编辑生鲜商品' : '新增生鲜商品')

/* 表单数据 */
const formData = reactive({
  dishName: '',
  categoryId: '' as string | number,
  price: 0,
  unit: '份',
  stock: 0,
  status: 1,
  imageUrl: '',
  description: '',
  shelfLifeDays: 7,
  storageType: 2,
  origin: '',
  spec: '',
  lossRate: 0
})

/* 表单验证规则 */
const formRules: FormRules = {
  dishName: [
    { required: true, message: '请输入商品名称', trigger: 'blur' }
  ],
  categoryId: [
    { required: true, message: '请选择商品分类', trigger: 'change' }
  ],
  price: [
    { required: true, message: '请输入单价', trigger: 'blur' },
    { type: 'number', min: 0.01, message: '单价必须大于0', trigger: 'blur' }
  ],
  unit: [
    { required: true, message: '请选择或输入单位', trigger: 'change' }
  ],
  storageType: [
    { required: true, message: '请选择储存温层', trigger: 'change' }
  ],
  shelfLifeDays: [
    { required: true, message: '请输入保质期', trigger: 'blur' }
  ]
}

/* 商品列表 */
const dishList = ref<any[]>([])

/* 获取商品分类 */
const fetchCategories = async () => {
  try {
    const res = await getCategoryList()
    categoryOptions.value = res.data.data || []
  } catch (error) {
    console.error('获取商品分类失败:', error)
    /* 兜底：与 init.sql 中的生鲜品类保持一致 */
    categoryOptions.value = [
      { id: '1', categoryName: '蔬菜', sortOrder: 1, status: 1 },
      { id: '2', categoryName: '水果', sortOrder: 2, status: 1 },
      { id: '3', categoryName: '肉禽蛋', sortOrder: 3, status: 1 },
      { id: '4', categoryName: '水产海鲜', sortOrder: 4, status: 1 },
      { id: '5', categoryName: '乳品烘焙', sortOrder: 5, status: 1 },
      { id: '6', categoryName: '粮油调味', sortOrder: 6, status: 1 }
    ]
  }
}

/* 获取商品列表 */
const fetchDishList = async () => {
  loading.value = true
  try {
    const res = await getDishList({
      pageNum: pagination.page,
      pageSize: pagination.pageSize,
      dishName: searchForm.dishName || undefined,
      categoryId: searchForm.categoryId || undefined,
      storageType: searchForm.storageType as StorageType | undefined,
      status: searchForm.status
    })
    dishList.value = res.data.data.records
    pagination.total = res.data.data.total
  } catch (error) {
    console.error('获取商品列表失败:', error)
  } finally {
    loading.value = false
  }
}

/* 分类名称 */
const getCategoryName = (categoryId: string | number) => {
  const matched = categoryOptions.value.find(item => String(item.id) === String(categoryId))
  return matched?.categoryName || '未分类'
}

/* 温层名称 */
const getStorageName = (storageType?: number) => {
  return storageOptions.find(item => item.value === storageType)?.label || '-'
}

/* 温层标签样式 */
const getStorageTagType = (storageType?: number) => {
  if (storageType === 1) return 'info'
  if (storageType === 2) return 'primary'
  if (storageType === 3) return 'warning'
  return 'info'
}

/* 短保商品高亮（3天以内视为短保，需优先出清） */
const isShortShelfLife = (shelfLifeDays?: number) => {
  return !!shelfLifeDays && shelfLifeDays <= 3
}

/* 库存标签类型 */
const getStockTagType = (stock: number) => {
  if (!stock) return 'danger'
  if (stock < 20) return 'warning'
  return 'success'
}

/* 搜索 */
const handleSearch = () => {
  pagination.page = 1
  fetchDishList()
}

/* 重置 */
const handleReset = () => {
  searchForm.dishName = ''
  searchForm.categoryId = ''
  searchForm.storageType = undefined
  searchForm.status = undefined
  fetchDishList()
}

/* 新增 */
const handleAdd = () => {
  isEdit.value = false
  resetForm()
  dialogVisible.value = true
}

/* 编辑 */
const handleEdit = (row: any) => {
  isEdit.value = true
  currentDishId.value = row.id
  Object.assign(formData, {
    dishName: row.dishName || row.name,
    categoryId: row.categoryId || row.category,
    price: Number(row.price || 0),
    unit: row.unit || '份',
    stock: row.stock || 0,
    status: row.status,
    imageUrl: row.imageUrl || '',
    description: row.description || '',
    shelfLifeDays: row.shelfLifeDays || 7,
    storageType: row.storageType || 1,
    origin: row.origin || '',
    spec: row.spec || '',
    lossRate: row.lossRate != null ? Number(row.lossRate) : 0
  })
  dialogVisible.value = true
}

/* 重置表单 */
const resetForm = () => {
  formData.dishName = ''
  formData.categoryId = ''
  formData.price = 0
  formData.unit = '份'
  formData.stock = 0
  formData.status = 1
  formData.imageUrl = ''
  formData.description = ''
  formData.shelfLifeDays = 7
  formData.storageType = 2
  formData.origin = ''
  formData.spec = ''
  formData.lossRate = 0
}

/* 图片选择和上传 */
const uploadLoading = ref(false)
const handleImageChange = async (file: UploadFile) => {
  if (!file.raw) return

  /* 文件类型校验 */
  const isImage = file.raw.type.startsWith('image/')
  if (!isImage) {
    ElMessage.error('只能上传图片文件！')
    return
  }

  /* 文件大小校验（2MB） */
  const isLt2M = file.raw.size / 1024 / 1024 < 2
  if (!isLt2M) {
    ElMessage.error('图片大小不能超过 2MB！')
    return
  }

  try {
    uploadLoading.value = true
    const res = await uploadImage(file.raw)
    if (res.data.code === 200) {
      formData.imageUrl = res.data.data
      ElMessage.success('图片上传成功')
    } else {
      ElMessage.error(res.data.msg || '图片上传失败')
    }
  } catch (error) {
    console.error('图片上传失败:', error)
    ElMessage.error('图片上传失败，请重试')
  } finally {
    uploadLoading.value = false
  }
}

/* 提交表单 */
const handleSubmit = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    submitLoading.value = true
    try {
      const dishData: any = {
        dishName: formData.dishName,
        categoryId: formData.categoryId,
        price: formData.price,
        stock: formData.stock,
        unit: formData.unit,
        description: formData.description,
        imageUrl: formData.imageUrl,
        status: formData.status,
        shelfLifeDays: formData.shelfLifeDays,
        storageType: formData.storageType,
        origin: formData.origin,
        spec: formData.spec,
        lossRate: formData.lossRate
      }
      if (isEdit.value) {
        dishData.id = String(currentDishId.value)
        await updateDish(dishData)
        ElMessage.success('编辑成功')
      } else {
        await addDish(dishData)
        ElMessage.success('新增成功')
      }
      dialogVisible.value = false
      fetchDishList()
    } catch (error) {
      console.error('提交失败:', error)
    } finally {
      submitLoading.value = false
    }
  })
}

/* 上下架 */
const handleStatusChange = async (row: any) => {
  try {
    await updateDishStatus(row.id, row.status)
    const statusText = row.status === 1 ? '上架' : '下架'
    ElMessage.success(`商品"${row.dishName}"已${statusText}`)
  } catch (error) {
    row.status = row.status === 1 ? 0 : 1
    console.error('修改状态失败:', error)
  }
}

/* 删除 */
const handleDelete = (row: any) => {
  ElMessageBox.confirm(`确定要删除商品"${row.dishName}"吗？`, '提示', {
    type: 'warning'
  }).then(async () => {
    try {
      await deleteDish(row.id)
      ElMessage.success('删除成功')
      fetchDishList()
    } catch (error) {
      console.error('删除失败:', error)
    }
  }).catch(() => {})
}

/* 分页 */
const handleSizeChange = (val: number) => {
  pagination.pageSize = val
  pagination.page = 1
  fetchDishList()
}

const handleCurrentChange = (val: number) => {
  pagination.page = val
  fetchDishList()
}

onMounted(() => {
  fetchCategories()
  fetchDishList()
})
</script>

<style scoped lang="scss">
.dish-management {
  padding: 20px;
}

.search-card {
  margin-bottom: 20px;
}

.table-card {
  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }
}

.pagination-wrapper {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
}

.price {
  color: #f56c6c;
  font-weight: bold;
}

.unit {
  color: #909399;
  font-size: 12px;
}

.goods-name {
  font-weight: 500;
  color: #303133;
}

.goods-code {
  font-size: 12px;
  color: #909399;
}

.empty-text {
  color: #ccc;
}

.expire-soon {
  color: #e6a23c;
  font-weight: 500;
}

.form-tip {
  margin-left: 6px;
  color: #909399;
  font-size: 12px;

  &.block {
    display: block;
    margin-left: 0;
    margin-top: 5px;
  }
}

.avatar-uploader {
  :deep(.el-upload) {
    border: 1px dashed var(--el-border-color);
    border-radius: 6px;
    cursor: pointer;
    position: relative;
    overflow: hidden;
    transition: var(--el-transition-duration-fast);

    &:hover {
      border-color: var(--el-color-primary);
    }
  }
}

.avatar-uploader-icon {
  font-size: 28px;
  color: #8c939d;
  width: 120px;
  height: 120px;
  text-align: center;
  line-height: 120px;
}

.avatar {
  width: 120px;
  height: 120px;
  display: block;
  object-fit: cover;
}
</style>
