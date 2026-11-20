<script setup lang="ts">
/**
 * 新增订单页面
 */
import { ref, reactive, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { createOrder } from '@/api/order'
import { getActiveDishes } from '@/api/dish'
import MapPicker from '@/components/MapPicker.vue'

const router = useRouter()

/* 加载状态 */
const loading = ref(false)
const submitLoading = ref(false)

/* 表单引用 */
const formRef = ref<FormInstance>()

/* 表单数据 */
const formData = reactive<CreateOrderRequest>({
  customerName: '',
  customerPhone: '',
  deliveryAddress: '',
  deliveryArea: '',
  deliveryLocation: undefined,
  expectedDeliveryTime: '',
  remark: '',
  orderItems: []
})

/* 商品列表 */
const dishList = ref<Dish[]>([])

/* 当前选择的商品 */
const currentDish = reactive({
  dishId: '',
  quantity: 1
})

/* 表单验证规则 */
const formRules = reactive<FormRules>({
  customerName: [
    { required: true, message: '请输入客户名称', trigger: 'blur' },
    { min: 2, max: 50, message: '客户名称长度在2-50个字符', trigger: 'blur' }
  ],
  customerPhone: [
    { required: true, message: '请输入联系电话', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号码', trigger: 'blur' }
  ],
  deliveryAddress: [
    { required: true, message: '请选择配送地址', trigger: 'change' }
  ],
  // deliveryArea: [
  //   { required: true, message: '请选择配送区域', trigger: 'change' }
  // ],
  expectedDeliveryTime: [
    { required: true, message: '请选择期望配送时间', trigger: 'change' }
  ]
})

/* 配送区域选项 */
const areaOptions = [
  { label: '朝阳区', value: '朝阳区' },
  { label: '海淀区', value: '海淀区' },
  { label: '西城区', value: '西城区' },
  { label: '东城区', value: '东城区' },
  { label: '丰台区', value: '丰台区' },
  { label: '通州区', value: '通州区' }
]

/* 已选商品列表 */
const selectedDishes = computed(() => {
  return formData.orderItems.map(item => {
    const dish = dishList.value.find(d => d.id === item.dishId)
    return {
      ...item,
      dishName: dish?.dishName || '',
      spec: dish?.spec || '',
      storageType: dish?.storageType,
      price: dish?.price || 0,
      subtotal: (dish?.price || 0) * item.quantity
    }
  })
})

/* 订单总金额 */
const totalAmount = computed(() => {
  return selectedDishes.value.reduce((sum, item) => sum + item.subtotal, 0)
})

/* 获取商品列表 */
const fetchDishList = async () => {
  loading.value = true
  try {
    const res = await getActiveDishes()
    dishList.value = res.data.data || []
  } catch (error) {
    /* 模拟数据：接口不可用时展示的生鲜商品样例 */
    dishList.value = [
      { id: '1', dishName: '有机小白菜', dishCode: 'SCC001', categoryId: '1', price: 6.8, unit: '份', spec: '500g/份', storageType: 2, status: 1 },
      { id: '2', dishName: '沙瓤番茄', dishCode: 'FQ001', categoryId: '1', price: 5.5, unit: '份', spec: '500g/份', storageType: 1, status: 1 },
      { id: '3', dishName: '丹东红颜草莓', dishCode: 'HYCM001', categoryId: '2', price: 39.9, unit: '盒', spec: '250g/盒', storageType: 2, status: 1 },
      { id: '4', dishName: '黑猪里脊', dishCode: 'HZLJ001', categoryId: '3', price: 32, unit: '份', spec: '500g/份', storageType: 2, status: 1 },
      { id: '5', dishName: '鲜活基围虾', dishCode: 'JWS001', categoryId: '4', price: 68, unit: '份', spec: '500g/份', storageType: 2, status: 1 },
      { id: '6', dishName: '巴氏鲜牛奶', dishCode: 'BSNN001', categoryId: '5', price: 19.9, unit: '瓶', spec: '950ml/瓶', storageType: 2, status: 1 }
    ] as Dish[]
  } finally {
    loading.value = false
  }
}

/* 添加商品 */
const handleAddDish = () => {
  if (!currentDish.dishId) {
    ElMessage.warning('请选择商品')
    return
  }
  if (currentDish.quantity < 1) {
    ElMessage.warning('数量必须大于0')
    return
  }
  
  /* 检查是否已存在 */
  const existIndex = formData.orderItems.findIndex(item => item.dishId === currentDish.dishId)
  if (existIndex > -1) {
    /* 已存在，增加数量 */
    formData.orderItems[existIndex].quantity += currentDish.quantity
  } else {
    /* 不存在，添加新项 */
    formData.orderItems.push({
      dishId: currentDish.dishId,
      quantity: currentDish.quantity
    })
  }
  
  /* 重置选择 */
  currentDish.dishId = ''
  currentDish.quantity = 1
}

/* 移除商品 */
const handleRemoveDish = (index: number) => {
  formData.orderItems.splice(index, 1)
}

/* 修改数量 */
const handleQuantityChange = (index: number, value: number) => {
  if (value < 1) {
    formData.orderItems[index].quantity = 1
  }
}

/* 提交表单 */
const handleSubmit = async () => {
  if (!formRef.value) return
  
  await formRef.value.validate(async (valid) => {
    if (!valid) {
      ElMessage.warning('请完善表单信息')
      return
    }
    
    if (formData.orderItems.length === 0) {
      ElMessage.warning('请至少添加一个商品')
      return
    }
    
    submitLoading.value = true
    try {
      await createOrder(formData)
      ElMessage.success('订单创建成功')
      router.push('/order/list')
    } catch (error) {
      console.error('创建订单失败:', error)
    } finally {
      submitLoading.value = false
    }
  })
}

/* 返回列表 */
const handleCancel = () => {
  router.push('/order/list')
}

/* 禁用过去的时间 */
const disabledDate = (time: Date) => {
  return time.getTime() < Date.now() - 3600 * 1000 * 24
}

/* 地图位置变化 */
const handleLocationChange = (location: LocationInfo | null) => {
  if (location) {
    formData.deliveryAddress = location.address
    formData.deliveryLocation = location
  }
}

onMounted(() => {
  fetchDishList()
})
</script>

<template>
  <div class="order-add-container">
    <el-card shadow="never">
      <template #header>
        <div class="card-header">
          <span>新增订单</span>
        </div>
      </template>

      <el-form 
        ref="formRef" 
        :model="formData" 
        :rules="formRules" 
        label-width="100px"
        style="max-width: 800px"
      >
        <!-- 客户信息 -->
        <el-divider content-position="left">客户信息</el-divider>
        
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="客户名称" prop="customerName">
              <el-input v-model="formData.customerName" placeholder="请输入客户名称" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="联系电话" prop="customerPhone">
              <el-input v-model="formData.customerPhone" placeholder="请输入联系电话" />
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-form-item label="配送地址" prop="deliveryAddress">
          <MapPicker 
            v-model="formData.deliveryLocation"
            placeholder="点击选择配送位置或输入地址搜索"
            @change="handleLocationChange"
          />
        </el-form-item>
        
        <el-row :gutter="20">
          <!-- <el-col :span="12">
            <el-form-item label="配送区域" prop="deliveryArea">
              <el-select v-model="formData.deliveryArea" placeholder="请选择配送区域" style="width: 100%">
                <el-option 
                  v-for="area in areaOptions" 
                  :key="area.value" 
                  :label="area.label" 
                  :value="area.value" 
                />
              </el-select>
            </el-form-item>
          </el-col> -->
          <el-col :span="12">
            <el-form-item label="期望配送时间" prop="expectedDeliveryTime">
              <el-date-picker
                v-model="formData.expectedDeliveryTime"
                type="datetime"
                placeholder="选择期望配送时间"
                value-format="YYYY-MM-DD HH:mm:ss"
                :disabled-date="disabledDate"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
        </el-row>
        
        <!-- 商品选择 -->
        <el-divider content-position="left">订单商品</el-divider>
        
        <el-row :gutter="10" class="dish-selector">
          <el-col :span="10">
            <el-select 
              v-model="currentDish.dishId" 
              placeholder="请选择商品" 
              filterable
              style="width: 100%"
            >
              <el-option 
                v-for="dish in dishList" 
                :key="dish.id" 
                :label="`${dish.dishName}${dish.spec ? '（' + dish.spec + '）' : ''} - ¥${dish.price}/${dish.unit}`" 
                :value="dish.id" 
              />
            </el-select>
          </el-col>
          <el-col :span="6">
            <el-input-number 
              v-model="currentDish.quantity" 
              :min="1" 
              :max="999"
              controls-position="right"
              style="width: 100%"
            />
          </el-col>
          <el-col :span="4">
            <el-button type="primary" @click="handleAddDish">
              <el-icon><Plus /></el-icon>
              添加
            </el-button>
          </el-col>
        </el-row>
        
        <!-- 已选商品列表 -->
        <el-table :data="selectedDishes" stripe border style="width: 100%; margin-top: 16px">
          <el-table-column prop="dishName" label="商品名称" />
          <el-table-column prop="spec" label="规格" width="120" align="center">
            <template #default="{ row }">{{ row.spec || '-' }}</template>
          </el-table-column>
          <el-table-column prop="price" label="单价" width="100" align="right">
            <template #default="{ row }">¥{{ row.price.toFixed(2) }}</template>
          </el-table-column>
          <el-table-column label="数量" width="150" align="center">
            <template #default="{ row, $index }">
              <el-input-number 
                v-model="formData.orderItems[$index].quantity" 
                :min="1" 
                :max="999"
                size="small"
                controls-position="right"
                @change="(val: number) => handleQuantityChange($index, val)"
              />
            </template>
          </el-table-column>
          <el-table-column prop="subtotal" label="小计" width="100" align="right">
            <template #default="{ row }">
              <span class="subtotal">¥{{ row.subtotal.toFixed(2) }}</span>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="80" align="center">
            <template #default="{ $index }">
              <el-button type="danger" link @click="handleRemoveDish($index)">移除</el-button>
            </template>
          </el-table-column>
        </el-table>
        
        <!-- 订单总金额 -->
        <div class="total-amount">
          <span>订单总金额：</span>
          <span class="amount">¥{{ totalAmount.toFixed(2) }}</span>
        </div>
        
        <!-- 备注 -->
        <el-divider content-position="left">订单备注</el-divider>
        
        <el-form-item label="备注" prop="remark">
          <el-input 
            v-model="formData.remark" 
            type="textarea" 
            :rows="3"
            placeholder="请输入订单备注（选填）" 
          />
        </el-form-item>
        
        <!-- 操作按钮 -->
        <el-form-item>
          <el-button type="primary" :loading="submitLoading" @click="handleSubmit">
            提交订单
          </el-button>
          <el-button @click="handleCancel">取消</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<style scoped lang="scss">
.order-add-container {
  .card-header {
    font-size: 16px;
    font-weight: 600;
  }
  
  .dish-selector {
    margin-bottom: 16px;
    align-items: center;
  }
  
  .subtotal {
    color: #F56C6C;
    font-weight: 600;
  }
  
  .total-amount {
    margin-top: 16px;
    text-align: right;
    font-size: 16px;
    
    .amount {
      color: #F56C6C;
      font-size: 24px;
      font-weight: 600;
      margin-left: 8px;
    }
  }
}
</style>
