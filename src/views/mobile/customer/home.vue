<template>
  <div class="m-page m-home">
    <van-nav-bar title="生鲜商城" :border="false" fixed placeholder />

    <van-search
      v-model="keyword"
      shape="round"
      placeholder="搜索商品，如 番茄、牛奶"
      background="transparent"
    />

    <!-- 分类横向滚动 -->
    <div class="m-cats">
      <div
        class="m-cats__item"
        :class="{ 'm-cats__item--active': activeCategory === 'all' }"
        @click="activeCategory = 'all'"
      >
        全部
      </div>
      <div
        v-for="cat in categories"
        :key="cat.id"
        class="m-cats__item"
        :class="{ 'm-cats__item--active': activeCategory === cat.id }"
        @click="activeCategory = cat.id"
      >
        {{ cat.categoryName }}
      </div>
    </div>

    <van-loading v-if="loading" class="m-loading" vertical>加载中...</van-loading>

    <template v-else>
      <div v-if="filteredDishes.length === 0" class="m-empty">
        <van-empty description="没有找到相关商品" />
      </div>

      <div v-for="dish in filteredDishes" :key="dish.id" class="m-dish">
        <div class="m-dish__thumb">
          <img v-if="dish.imageUrl" :src="dish.imageUrl" :alt="dish.dishName" />
          <span v-else>{{ dish.dishName.slice(0, 1) }}</span>
        </div>
        <div class="m-dish__main">
          <div class="m-dish__name m-ellipsis">{{ dish.dishName }}</div>
          <div class="m-dish__meta">
            <span v-if="dish.spec">{{ dish.spec }}</span>
            <span>{{ dish.categoryName || '未分类' }}</span>
          </div>
          <div class="m-row m-dish__bottom">
            <div>
              <span class="m-price">{{ Number(dish.price).toFixed(2) }}</span>
              <span class="m-dish__unit">/{{ dish.unit }}</span>
            </div>
            <van-stepper
              v-if="dish.stock > 0"
              :model-value="cart[dish.id] || 0"
              :min="0"
              :max="dish.stock"
              button-size="24px"
              @change="(value: number) => setQty(dish, value)"
            />
            <span v-else class="m-dish__soldout">已售罄</span>
          </div>
          <div class="m-dish__stock">库存 {{ dish.stock }}</div>
        </div>
      </div>
    </template>

    <!-- 底部结算条 -->
    <div class="m-fixed-bar">
      <div class="m-cart-total">
        共 {{ totalQty }} 件
        <span class="m-price">{{ totalAmount.toFixed(2) }}</span>
      </div>
      <van-button
        type="primary"
        round
        class="m-cart-btn"
        :disabled="totalQty === 0"
        @click="openCheckout"
      >
        去下单
      </van-button>
    </div>

    <!-- 下单面板 -->
    <van-popup v-model:show="showCheckout" position="bottom" round :style="{ height: '82%' }">
      <div class="m-sheet">
        <div class="m-sheet__title">确认订单</div>
        <div class="m-sheet__scroll">
          <div class="m-sheet__goods">
            <div v-for="item in cartItems" :key="item.dish.id" class="m-sheet__goods-item">
              <span class="m-ellipsis">{{ item.dish.dishName }}</span>
              <span class="m-muted">×{{ item.qty }}</span>
              <span class="m-price">{{ (item.dish.price * item.qty).toFixed(2) }}</span>
            </div>
          </div>

          <van-cell-group inset>
            <van-field
              v-model="form.customerName"
              label="联系人"
              placeholder="请输入联系人姓名"
              :rules="[{ required: true, message: '请填写联系人' }]"
            />
            <van-field
              v-model="form.customerPhone"
              label="手机号"
              type="tel"
              maxlength="11"
              placeholder="请输入手机号"
              :rules="[{ required: true, message: '请填写手机号' }]"
            />
            <van-field
              v-model="form.deliveryArea"
              label="配送区域"
              placeholder="如 高新区、历下区"
            />
            <van-field
              v-model="form.deliveryAddress"
              label="配送地址"
              type="textarea"
              rows="2"
              autosize
              maxlength="120"
              show-word-limit
              placeholder="如 高新区科创路 18 号 3 单元 501"
              :rules="[{ required: true, message: '请填写配送地址' }]"
            />
            <div class="m-sheet__map-row" @click="openMapPicker">
              <van-icon name="location" color="#16a34a" size="18" />
              <span class="m-sheet__map-text">
                {{ pickedCoords ? `已地图选点 ${pickedCoords}` : '在地图上选点，自动填写地址与区域' }}
              </span>
              <van-icon name="arrow" color="#b3c0b8" />
            </div>
            <van-field
              v-model="form.expectedDeliveryTime"
              label="期望送达"
              readonly
              placeholder="请选择期望送达时间"
              @click="showTimePicker = true"
            />
            <van-field
              v-model="form.remark"
              label="备注"
              placeholder="选填，如 放门口"
              maxlength="60"
            />
          </van-cell-group>
        </div>

        <div class="m-sheet__footer">
          <div class="m-cart-total">
            合计
            <span class="m-price">{{ totalAmount.toFixed(2) }}</span>
          </div>
          <van-button type="primary" round :loading="submitting" @click="submitOrder">
            提交订单
          </van-button>
        </div>
      </div>
    </van-popup>

    <!-- 期望送达时间选择 -->
    <van-popup v-model:show="showTimePicker" position="bottom" round>
      <van-date-picker
        v-model="pickerValue"
        type="datetime"
        title="选择期望送达时间"
        :min-date="minDate"
        :max-date="maxDate"
        @confirm="onTimeConfirm"
        @cancel="showTimePicker = false"
      />
    </van-popup>

    <!-- 地图选点 -->
    <MobileMapPicker ref="mapPickerRef" @confirm="onMapConfirm" />
  </div>
</template>

<script setup lang="ts">
/**
 * 客户端首页：商品浏览 + 购物车 + 提交订单
 */
import { computed, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { showToast } from 'vant'
import { getCustomerHome, createMobileOrder, getMobileProfile } from '@/api/mobile'
import MobileMapPicker from '@/components/MobileMapPicker.vue'

const router = useRouter()

const loading = ref(true)
const submitting = ref(false)
const categories = ref<MobileCategory[]>([])
const dishes = ref<MobileDish[]>([])
const keyword = ref('')
const activeCategory = ref<'all' | string>('all')

/* 购物车：dishId -> 数量 */
const cart = reactive<Record<string, number>>({})

const showCheckout = ref(false)
const showTimePicker = ref(false)
const pickerValue = ref<string[]>([])

const now = new Date()
const minDate = new Date(now.getFullYear(), now.getMonth(), now.getDate(), now.getHours(), now.getMinutes())
const maxDate = new Date(now.getTime() + 7 * 24 * 3600 * 1000)

const form = reactive({
  customerName: '',
  customerPhone: '',
  deliveryArea: '',
  deliveryAddress: '',
  expectedDeliveryTime: '',
  remark: '',
  /* 地图选点结果：未选点时为 null，下单时不传 */
  longitude: null as number | null,
  latitude: null as number | null
})

/* 地图选点组件 */
const mapPickerRef = ref<InstanceType<typeof MobileMapPicker> | null>(null)

/* 已选坐标的展示文本 */
const pickedCoords = computed(() => {
  if (form.longitude === null || form.latitude === null) {
    return ''
  }
  return `${Number(form.longitude).toFixed(5)}, ${Number(form.latitude).toFixed(5)}`
})

const pad = (value: number) => String(value).padStart(2, '0')
const formatDateTime = (date: Date) =>
  `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())} ${pad(date.getHours())}:${pad(date.getMinutes())}`

const filteredDishes = computed(() => {
  const kw = keyword.value.trim()
  return dishes.value.filter((dish) => {
    const matchCategory = activeCategory.value === 'all' || dish.categoryId === activeCategory.value
    const matchKeyword = !kw || dish.dishName.includes(kw) || (dish.categoryName || '').includes(kw)
    return matchCategory && matchKeyword
  })
})

const cartItems = computed(() =>
  dishes.value
    .filter((dish) => (cart[dish.id] || 0) > 0)
    .map((dish) => ({ dish, qty: cart[dish.id] }))
)

const totalQty = computed(() => cartItems.value.reduce((sum, item) => sum + item.qty, 0))
const totalAmount = computed(() =>
  cartItems.value.reduce((sum, item) => sum + Number(item.dish.price) * item.qty, 0)
)

const setQty = (dish: MobileDish, value: number) => {
  const max = dish.stock || 0
  const qty = Math.max(0, Math.min(Number(value) || 0, max))
  if (qty === 0) {
    delete cart[dish.id]
  } else {
    cart[dish.id] = qty
  }
}

const loadData = async () => {
  loading.value = true
  try {
    const res = await getCustomerHome()
    categories.value = res.data.data.categories || []
    dishes.value = res.data.data.dishes || []
  } catch (error) {
    /* 错误已统一提示 */
  } finally {
    loading.value = false
  }
}

const loadProfile = async () => {
  try {
    const res = await getMobileProfile()
    const profile = res.data.data
    form.customerName = profile.realname || ''
    form.customerPhone = profile.phone || ''
  } catch (error) {
    /* 忽略：下单时仍可手动填写 */
  }
}

const openCheckout = () => {
  if (totalQty.value === 0) {
    return
  }
  if (!form.expectedDeliveryTime) {
    form.expectedDeliveryTime = formatDateTime(new Date(Date.now() + 60 * 60 * 1000))
  }
  pickerValue.value = [
    form.expectedDeliveryTime.slice(0, 4),
    form.expectedDeliveryTime.slice(5, 7),
    form.expectedDeliveryTime.slice(8, 10),
    form.expectedDeliveryTime.slice(11, 13),
    form.expectedDeliveryTime.slice(14, 16)
  ]
  showCheckout.value = true
}

const onTimeConfirm = ({ selectedValues }: { selectedValues: string[] }) => {
  const [year, month, day, hour, minute] = selectedValues
  form.expectedDeliveryTime = `${year}-${month}-${day} ${hour}:${minute}`
  showTimePicker.value = false
}

/* 打开地图选点：已选过则回显 */
const openMapPicker = () => {
  const init =
    form.longitude !== null && form.latitude !== null
      ? {
          address: form.deliveryAddress,
          longitude: form.longitude,
          latitude: form.latitude
        }
      : null
  mapPickerRef.value?.open(init)
}

/* 地图选点确认：回填地址、区域与坐标 */
const onMapConfirm = (location: LocationInfo) => {
  form.deliveryAddress = location.address
  if (location.district) {
    form.deliveryArea = location.district
  }
  form.longitude = location.longitude
  form.latitude = location.latitude
  showToast('已选择地图位置')
}

const submitOrder = async () => {
  if (!form.customerName.trim()) {
    showToast('请填写联系人')
    return
  }
  if (!/^1\d{10}$/.test(form.customerPhone.trim())) {
    showToast('请填写正确的手机号')
    return
  }
  if (!form.deliveryAddress.trim()) {
    showToast('请填写配送地址')
    return
  }
  if (totalQty.value === 0) {
    showToast('请先选择商品')
    return
  }

  submitting.value = true
  try {
    const res = await createMobileOrder({
      customerName: form.customerName.trim(),
      customerPhone: form.customerPhone.trim(),
      deliveryArea: form.deliveryArea.trim() || '默认区域',
      deliveryAddress: form.deliveryAddress.trim(),
      expectedDeliveryTime: form.expectedDeliveryTime,
      remark: form.remark.trim() || undefined,
      longitude: form.longitude ?? undefined,
      latitude: form.latitude ?? undefined,
      items: cartItems.value.map((item) => ({ dishId: item.dish.id, quantity: item.qty }))
    })
    showToast(
      res.data.data?.autoAssigned
        ? res.data.data.dispatchMessage || '下单成功，已分配配送员'
        : '下单成功，正在等待调度'
    )
    Object.keys(cart).forEach((key) => delete cart[key])
    showCheckout.value = false
    const orderId = res.data.data?.orderId
    if (orderId) {
      router.push(`/m/customer/order/${orderId}`)
    } else {
      router.push('/m/customer/orders')
    }
  } catch (error) {
    /* 错误已统一提示 */
  } finally {
    submitting.value = false
  }
}

onMounted(() => {
  loadData()
  loadProfile()
})
</script>

<style scoped>
.m-home {
  padding-bottom: 128px;
}

.m-cats {
  display: flex;
  gap: 8px;
  overflow-x: auto;
  padding: 4px 12px 10px;
  scrollbar-width: none;
}

.m-cats::-webkit-scrollbar {
  display: none;
}

.m-cats__item {
  flex: 0 0 auto;
  padding: 5px 14px;
  border-radius: 999px;
  background: #fff;
  color: #4b5f53;
  font-size: 13px;
  border: 1px solid #e3efe7;
}

.m-cats__item--active {
  background: #16a34a;
  border-color: #16a34a;
  color: #fff;
  font-weight: 600;
}

.m-loading {
  padding: 40px 0;
}

.m-dish {
  display: flex;
  gap: 12px;
  background: #fff;
  border-radius: 12px;
  padding: 10px;
  margin-bottom: 10px;
}

.m-dish__thumb {
  width: 84px;
  height: 84px;
  flex: 0 0 84px;
  border-radius: 10px;
  overflow: hidden;
  background: linear-gradient(135deg, #e8f6ed, #d0eddb);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28px;
  color: #16a34a;
  font-weight: 700;
}

.m-dish__thumb img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.m-dish__main {
  flex: 1;
  min-width: 0;
}

.m-dish__name {
  font-size: 15px;
  font-weight: 600;
  color: #1f2d24;
}

.m-dish__meta {
  display: flex;
  gap: 8px;
  margin-top: 3px;
  font-size: 11px;
  color: #93a49a;
}

.m-dish__bottom {
  margin-top: 8px;
}

.m-dish__unit {
  font-size: 11px;
  color: #93a49a;
  margin-left: 2px;
}

.m-dish__stock {
  margin-top: 4px;
  font-size: 11px;
  color: #b3c0b8;
}

.m-dish__soldout {
  font-size: 12px;
  color: #b3c0b8;
}

.m-fixed-bar {
  justify-content: space-between;
}

.m-cart-total {
  font-size: 13px;
  color: #4b5f53;
  display: flex;
  align-items: center;
  gap: 6px;
}

.m-cart-total .m-price {
  font-size: 17px;
}

.m-cart-btn {
  min-width: 120px;
}

.m-sheet {
  display: flex;
  flex-direction: column;
  height: 100%;
}

.m-sheet__title {
  padding: 16px;
  text-align: center;
  font-size: 16px;
  font-weight: 600;
  color: #14342b;
}

.m-sheet__scroll {
  flex: 1;
  overflow-y: auto;
  padding-bottom: 12px;
}

.m-sheet__goods {
  margin: 0 16px 12px;
  padding: 10px 12px;
  background: #f6fcf8;
  border-radius: 10px;
}

.m-sheet__goods-item {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 13px;
  padding: 4px 0;
}

.m-sheet__goods-item span:first-child {
  flex: 1;
  min-width: 0;
}

/* 表单内的「地图选点」入口 */
.m-sheet__map-row {
  display: flex;
  align-items: center;
  gap: 6px;
  margin: 0 16px 12px;
  padding: 9px 12px;
  border: 1px dashed #b9e3c9;
  border-radius: 10px;
  background: #f6fcf8;
}

.m-sheet__map-text {
  flex: 1;
  font-size: 12.5px;
  color: #16a34a;
}

.m-sheet__footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 10px 16px calc(10px + env(safe-area-inset-bottom));
  border-top: 1px solid #e3efe7;
  background: #fff;
}
</style>
