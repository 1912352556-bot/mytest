<template>
  <div class="dict-management">
    <el-row :gutter="20">
      <!-- 字典类型列表 -->
      <el-col :span="8">
        <el-card class="dict-type-card" shadow="hover">
          <template #header>
            <div class="card-header">
              <span>字典类型</span>
              <el-button type="primary" :icon="Plus" size="small" @click="handleAddType">新增</el-button>
            </div>
          </template>

          <el-input
            v-model="typeSearchKey"
            placeholder="搜索字典类型"
            :prefix-icon="Search"
            clearable
            style="margin-bottom: 15px"
          />

          <el-table
            :data="filteredDictTypes"
            v-loading="typeLoading"
            highlight-current-row
            @current-change="handleTypeChange"
            height="500"
          >
            <el-table-column prop="dictName" label="字典名称" min-width="120" />
            <el-table-column prop="dictCode" label="字典编码" width="120" />
            <el-table-column label="操作" width="80" align="center">
              <template #default="{ row }">
                <el-button type="primary" link :icon="Edit" @click.stop="handleEditType(row)" />
                <el-button type="danger" link :icon="Delete" @click.stop="handleDeleteType(row)" />
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>

      <!-- 字典数据列表 -->
      <el-col :span="16">
        <el-card class="dict-data-card" shadow="hover">
          <template #header>
            <div class="card-header">
              <span>字典数据 {{ currentType ? `- ${currentType.typeName}` : '' }}</span>
              <el-button type="primary" :icon="Plus" size="small" :disabled="!currentType" @click="handleAddData">新增</el-button>
            </div>
          </template>

          <el-table :data="dictDataList" v-loading="dataLoading" border stripe height="500">
            <el-table-column prop="sortOrder" label="排序" width="60" align="center" />
            <el-table-column prop="dictLabel" label="字典标签" min-width="120" />
            <el-table-column prop="dictValue" label="字典值" width="120" />
            <el-table-column prop="status" label="状态" width="80" align="center">
              <template #default="{ row }">
                <el-tag :type="row.status === 1 ? 'success' : 'danger'">
                  {{ row.status === 1 ? '启用' : '禁用' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="remark" label="备注" min-width="150" show-overflow-tooltip />
            <el-table-column label="操作" width="120" align="center">
              <template #default="{ row }">
                <el-button type="primary" link :icon="Edit" @click="handleEditData(row)">编辑</el-button>
                <el-button type="danger" link :icon="Delete" @click="handleDeleteData(row)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
    </el-row>

    <!-- 字典类型对话框 -->
    <el-dialog
      v-model="typeDialogVisible"
      :title="isEditType ? '编辑字典类型' : '新增字典类型'"
      width="450px"
      :close-on-click-modal="false"
    >
      <el-form ref="typeFormRef" :model="typeForm" :rules="typeRules" label-width="100px">
        <el-form-item label="字典名称" prop="typeName">
          <el-input v-model="typeForm.typeName" placeholder="请输入字典名称" />
        </el-form-item>
        <el-form-item label="字典编码" prop="typeCode">
          <el-input v-model="typeForm.typeCode" placeholder="请输入字典编码" :disabled="isEditType" />
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="typeForm.remark" type="textarea" :rows="3" placeholder="请输入备注" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="typeDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="typeSubmitLoading" @click="handleTypeSubmit">确定</el-button>
      </template>
    </el-dialog>

    <!-- 字典数据对话框 -->
    <el-dialog
      v-model="dataDialogVisible"
      :title="isEditData ? '编辑字典数据' : '新增字典数据'"
      width="450px"
      :close-on-click-modal="false"
    >
      <el-form ref="dataFormRef" :model="dataForm" :rules="dataRules" label-width="100px">
        <el-form-item label="字典标签" prop="label">
          <el-input v-model="dataForm.label" placeholder="请输入字典标签" />
        </el-form-item>
        <el-form-item label="字典值" prop="value">
          <el-input v-model="dataForm.value" placeholder="请输入字典值" />
        </el-form-item>
        <el-form-item label="排序" prop="sort">
          <el-input-number v-model="dataForm.sort" :min="0" :max="999" style="width: 100%" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="dataForm.status">
            <el-radio :value="1">启用</el-radio>
            <el-radio :value="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="dataForm.remark" type="textarea" :rows="3" placeholder="请输入备注" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dataDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="dataSubmitLoading" @click="handleDataSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import { Search, Plus, Edit, Delete } from '@element-plus/icons-vue'
import { getDictTypeList, addDictType, updateDictType, deleteDictType, getDictDataList, addDictData, updateDictData, deleteDictData } from '@/api/dict'

// 搜索关键字
const typeSearchKey = ref('')

// 状态
const typeLoading = ref(false)
const dataLoading = ref(false)
const typeDialogVisible = ref(false)
const dataDialogVisible = ref(false)
const typeSubmitLoading = ref(false)
const dataSubmitLoading = ref(false)
const isEditType = ref(false)
const isEditData = ref(false)
const currentType = ref<any>(null)
const currentTypeId = ref<string>()
const currentDataId = ref<string>()

// 表单引用
const typeFormRef = ref<FormInstance>()
const dataFormRef = ref<FormInstance>()

// 表单数据
const typeForm = reactive({
  typeName: '',
  typeCode: '',
  remark: ''
})

const dataForm = reactive({
  label: '',
  value: '',
  sort: 0,
  status: 1,
  remark: ''
})

// 表单验证规则
const typeRules: FormRules = {
  typeName: [{ required: true, message: '请输入字典名称', trigger: 'blur' }],
  typeCode: [
    { required: true, message: '请输入字典编码', trigger: 'blur' },
    { pattern: /^[a-z_]+$/, message: '编码只能包含小写字母和下划线', trigger: 'blur' }
  ]
}

const dataRules: FormRules = {
  label: [{ required: true, message: '请输入字典标签', trigger: 'blur' }],
  value: [{ required: true, message: '请输入字典值', trigger: 'blur' }]
}

// 模拟字典类型数据
const dictTypes = ref<any[]>([])

// 模拟字典数据
const allDictData = ref<any[]>([])

// 获取字典类型列表
const fetchDictTypes = async () => {
  typeLoading.value = true
  try {
    const res = await getDictTypeList()
    dictTypes.value = (res.data.data || []) as any
  } catch (error) {
    console.error('获取字典类型失败:', error)
  } finally {
    typeLoading.value = false
  }
}

// 获取字典数据
const fetchDictData = async (typeId: string) => {
  dataLoading.value = true
  try {
    const res = await getDictDataList(typeId)
    allDictData.value = (res.data.data || []) as any
  } catch (error) {
    console.error('获取字典数据失败:', error)
  } finally {
    dataLoading.value = false
  }
}

// 过滤后的字典类型
const filteredDictTypes = computed(() => {
  if (!typeSearchKey.value) return dictTypes.value
  return dictTypes.value.filter((t: any) =>
    (t.dictName || t.typeName || '').includes(typeSearchKey.value) || 
    t.dictCode.includes(typeSearchKey.value)
  )
})

// 当前类型的字典数据
const dictDataList = computed(() => {
  if (!currentType.value) return []
  const typeCode = currentType.value.dictCode || currentType.value.typeCode
  return allDictData.value.filter((d: any) => d.dictCode === typeCode)
})

// 选择字典类型
const handleTypeChange = (row: any) => {
  currentType.value = row
  if (row) {
    fetchDictData(String(row.id))
  } else {
    allDictData.value = []
  }
}

// 新增字典类型
const handleAddType = () => {
  isEditType.value = false
  typeForm.typeName = ''
  typeForm.typeCode = ''
  typeForm.remark = ''
  typeDialogVisible.value = true
}

// 编辑字典类型
const handleEditType = (row: any) => {
  isEditType.value = true
  currentTypeId.value = String(row.id)
  Object.assign(typeForm, {
    typeName: row.dictName || row.typeName,
    typeCode: row.dictCode || row.typeCode,
    remark: row.description || row.remark || ''
  })
  typeDialogVisible.value = true
}

// 提交字典类型
const handleTypeSubmit = async () => {
  if (!typeFormRef.value) return
  await typeFormRef.value.validate(async (valid) => {
    if (!valid) return
    typeSubmitLoading.value = true
    try {
      const typeData: any = {
        dictName: typeForm.typeName,
        dictCode: typeForm.typeCode,
        description: typeForm.remark,
        status: 1
      }
      if (isEditType.value) {
        typeData.id = currentTypeId.value
        await updateDictType(typeData)
        ElMessage.success('编辑成功')
      } else {
        await addDictType(typeData)
        ElMessage.success('新增成功')
      }
      typeDialogVisible.value = false
      fetchDictTypes()
    } catch (error) {
      console.error('提交失败:', error)
    } finally {
      typeSubmitLoading.value = false
    }
  })
}

// 删除字典类型
const handleDeleteType = (row: any) => {
  const typeName = row.dictName || row.typeName
  ElMessageBox.confirm(`确定要删除字典类型"${typeName}"吗？删除后该类型下的所有字典数据也将被删除。`, '提示', {
    type: 'warning'
  }).then(async () => {
    try {
      await deleteDictType(String(row.id))
      ElMessage.success('删除成功')
      fetchDictTypes()
      if (currentType.value?.id === row.id) {
        currentType.value = null
        allDictData.value = []
      }
    } catch (error) {
      console.error('删除失败:', error)
    }
  }).catch(() => {})
}

// 新增字典数据
const handleAddData = () => {
  isEditData.value = false
  dataForm.label = ''
  dataForm.value = ''
  dataForm.sort = 0
  dataForm.status = 1
  dataForm.remark = ''
  dataDialogVisible.value = true
}

// 编辑字典数据
const handleEditData = (row: any) => {
  isEditData.value = true
  currentDataId.value = String(row.id)
  Object.assign(dataForm, {
    label: row.dictLabel || row.label,
    value: row.dictValue || row.value,
    sort: row.sortOrder || row.sort,
    status: row.status,
    remark: row.remark || ''
  })
  dataDialogVisible.value = true
}

// 提交字典数据
const handleDataSubmit = async () => {
  if (!dataFormRef.value || !currentType.value) return
  await dataFormRef.value.validate(async (valid) => {
    if (!valid) return
    dataSubmitLoading.value = true
    try {
      const type = currentType.value!
      const dictData: any = {
        dictTypeId: String(type.id),
        dictCode: type.typeCode,
        dictLabel: dataForm.label,
        dictValue: dataForm.value,
        sortOrder: dataForm.sort,
        status: dataForm.status,
        remark: dataForm.remark
      }
      if (isEditData.value) {
        dictData.id = String(currentDataId.value)
        await updateDictData(dictData)
        ElMessage.success('编辑成功')
      } else {
        await addDictData(dictData)
        ElMessage.success('新增成功')
      }
      dataDialogVisible.value = false
      fetchDictData(String(type.id))
    } catch (error) {
      console.error('提交失败:', error)
    } finally {
      dataSubmitLoading.value = false
    }
  })
}

// 删除字典数据
const handleDeleteData = (row: any) => {
  const label = row.dictLabel || row.label
  ElMessageBox.confirm(`确定要删除字典数据"${label}"吗？`, '提示', {
    type: 'warning'
  }).then(async () => {
    try {
      await deleteDictData(String(row.id))
      ElMessage.success('删除成功')
      if (currentType.value) {
        fetchDictData(String(currentType.value.id))
      }
    } catch (error) {
      console.error('删除失败:', error)
    }
  }).catch(() => {})
}

onMounted(() => {
  fetchDictTypes()
})
</script>

<style scoped lang="scss">
.dict-management {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.dict-type-card,
.dict-data-card {
  height: calc(100vh - 140px);
}
</style>
