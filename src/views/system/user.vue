<template>
  <div class="user-management">
    <!-- 搜索区域 -->
    <el-card class="search-card" shadow="hover">
      <el-form :model="searchForm" inline>
        <el-form-item label="用户名">
          <el-input v-model="searchForm.username" placeholder="请输入用户名" clearable style="width: 180px" />
        </el-form-item>
        <el-form-item label="手机号">
          <el-input v-model="searchForm.phone" placeholder="请输入手机号" clearable style="width: 180px" />
        </el-form-item>
        <el-form-item label="角色">
          <el-select v-model="searchForm.role" placeholder="请选择角色" clearable style="width: 150px">
            <el-option label="管理员" :value="0" />
            <el-option label="调度员" :value="1" />
            <el-option label="配送员" :value="2" />
            <el-option label="客户" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="请选择状态" clearable style="width: 120px">
            <el-option label="启用" :value="1" />
            <el-option label="禁用" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="Search" @click="handleSearch">搜索</el-button>
          <el-button :icon="Refresh" @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 操作按钮 -->
    <el-card class="table-card" shadow="hover">
      <template #header>
        <div class="card-header">
          <span>用户列表</span>
          <el-button type="primary" :icon="Plus" @click="handleAdd">新增用户</el-button>
        </div>
      </template>

      <!-- 用户列表 -->
      <el-table :data="userList" v-loading="loading" border stripe>
        <el-table-column prop="id" label="ID" width="80" align="center" />
        <el-table-column prop="username" label="用户名" width="120" />
        <el-table-column prop="realName" label="真实姓名" width="120" />
        <el-table-column prop="phone" label="手机号" width="140" />
        <el-table-column prop="email" label="邮箱" min-width="180" />
        <el-table-column prop="role" label="角色" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="getRoleTagType(row.role)">{{ getRoleName(row.role) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="80" align="center">
          <template #default="{ row }">
            <el-switch
              v-model="row.status"
              :active-value="1"
              :inactive-value="0"
              @change="handleStatusChange(row)"
            />
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="170" />
        <el-table-column label="操作" width="180" align="center" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link :icon="Edit" @click="handleEdit(row)">编辑</el-button>
            <el-button type="warning" link :icon="Key" @click="handleResetPwd(row)">重置密码</el-button>
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
      width="500px"
      :close-on-click-modal="false"
    >
      <el-form
        ref="formRef"
        :model="formData"
        :rules="formRules"
        label-width="100px"
      >
        <el-form-item label="用户名" prop="username">
          <el-input v-model="formData.username" placeholder="请输入用户名" :disabled="isEdit" />
        </el-form-item>
        <el-form-item label="真实姓名" prop="realname">
          <el-input v-model="formData.realname" placeholder="请输入真实姓名" />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="formData.phone" placeholder="请输入手机号" maxlength="11" />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="formData.email" placeholder="请输入邮箱" />
        </el-form-item>
        <el-form-item label="角色" prop="role">
          <el-select v-model="formData.role" placeholder="请选择角色" style="width: 100%">
            <el-option label="管理员" :value="0" />
            <el-option label="调度员" :value="1" />
            <el-option label="配送员" :value="2" />
            <el-option label="客户" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item v-if="!isEdit" label="初始密码" prop="password">
          <el-input v-model="formData.password" type="password" placeholder="请输入初始密码" show-password />
        </el-form-item>
        <el-form-item label="性别" prop="gender">
          <el-radio-group v-model="formData.gender">
            <el-radio :value="1">男</el-radio>
            <el-radio :value="2">女</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>

    <!-- 重置密码对话框 -->
    <el-dialog
      v-model="resetPwdVisible"
      title="重置密码"
      width="400px"
      :close-on-click-modal="false"
    >
      <el-form ref="resetPwdRef" :model="resetPwdForm" :rules="resetPwdRules" label-width="100px">
        <el-form-item label="新密码" prop="newPassword">
          <el-input v-model="resetPwdForm.newPassword" type="password" placeholder="请输入新密码" show-password />
        </el-form-item>
        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input v-model="resetPwdForm.confirmPassword" type="password" placeholder="请再次输入新密码" show-password />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="resetPwdVisible = false">取消</el-button>
        <el-button type="primary" :loading="resetPwdLoading" @click="handleResetPwdSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import { Search, Refresh, Plus, Edit, Delete, Key } from '@element-plus/icons-vue'
import { getUserList, addUser, updateUser, deleteUser, resetPassword, updateUserStatus } from '@/api/user'

// 搜索表单
const searchForm = reactive({
  username: '',
  phone: '',
  role: undefined as number | undefined,
  status: undefined as number | undefined
})

// 分页
const pagination = reactive({
  page: 1,
  pageSize: 10,
  total: 0
})

// 状态
const loading = ref(false)
const dialogVisible = ref(false)
const submitLoading = ref(false)
const resetPwdVisible = ref(false)
const resetPwdLoading = ref(false)
const isEdit = ref(false)
const currentUserId = ref<number>()

// 表单引用
const formRef = ref<FormInstance>()
const resetPwdRef = ref<FormInstance>()

// 对话框标题
const dialogTitle = computed(() => isEdit.value ? '编辑用户' : '新增用户')

// 表单数据
const formData = reactive<RegUser>({
  username: '',
  password: '',
  realname: '',
  phone: '',
  email: '',
  gender: 1,
  role: 0,
})

// 重置密码表单
const resetPwdForm = reactive({
  newPassword: '',
  confirmPassword: ''
})

// 表单验证规则
const formRules: FormRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '用户名长度在3-20个字符', trigger: 'blur' }
  ],
  realName: [
    { required: true, message: '请输入真实姓名', trigger: 'blur' }
  ],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ],
  email: [
    { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }
  ],
  role: [
    { required: true, message: '请选择角色', trigger: 'change' }
  ],
  password: [
    { required: true, message: '请输入初始密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度在6-20个字符', trigger: 'blur' }
  ]
}

// 重置密码验证规则
const resetPwdRules: FormRules = {
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度在6-20个字符', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请再次输入新密码', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (value !== resetPwdForm.newPassword) {
          callback(new Error('两次输入的密码不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ]
}

// 模拟数据
const userList = ref<any[]>([])

// 获取角色名称
const getRoleName = (role: number) => {
  const roleMap: Record<number, string> = { 0: '管理员', 1: '调度员', 2: '配送员', 3: '客户' }
  return roleMap[role] || '未知'
}

// 获取角色标签类型
const getRoleTagType = (role: number) => {
  const typeMap: Record<number, string> = { 0: 'danger', 1: 'warning', 2: '', 3: 'success' }
  return typeMap[role] || 'info'
}

// 获取用户列表
const fetchUserList = async () => {
  loading.value = true
  try {
    const res = await getUserList({
      pageNum: pagination.page,
      pageSize: pagination.pageSize,
      username: searchForm.username || undefined,
      phone: searchForm.phone || undefined,
      role: searchForm.role as RoleType | undefined,
    })
    userList.value = res.data.data.records
    pagination.total = res.data.data.total
  } catch (error) {
    console.error('获取用户列表失败:', error)
  } finally {
    loading.value = false
  }
}

// 搜索
const handleSearch = () => {
  pagination.page = 1
  fetchUserList()
}

// 重置
const handleReset = () => {
  searchForm.username = ''
  searchForm.phone = ''
  searchForm.role = undefined
  searchForm.status = undefined
  fetchUserList()
}

// 新增
const handleAdd = () => {
  isEdit.value = false
  resetForm()
  dialogVisible.value = true
}

// 编辑
const handleEdit = (row: any) => {
  isEdit.value = true
  formData.username = row.username
  formData.realname = row.realName || row.realname
  formData.phone = row.phone
  formData.email = row.email
  formData.gender = row.gender || 1
  formData.role = row.role
  formData.password = ''
  currentUserId.value = row.id
  dialogVisible.value = true
}

// 重置表单
const resetForm = () => {
  formData.username = ''
  formData.realname = ''
  formData.phone = ''
  formData.email = ''
  formData.gender = 1
  formData.role = 0
  formData.password = ''
}

// 提交表单
const handleSubmit = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    submitLoading.value = true
    try {
      if (isEdit.value) {
        await updateUser({ ...formData, id: String(currentUserId.value) } as any)
        ElMessage.success('编辑成功')
      } else {
        await addUser(formData)
        ElMessage.success('新增成功')
      }
      dialogVisible.value = false
      fetchUserList()
    } catch (error) {
      console.error('提交失败:', error)
    } finally {
      submitLoading.value = false
    }
  })
}

// 状态变更
const handleStatusChange = async (row: any) => {
  try {
    await updateUserStatus(row.id, row.status)
    const statusText = row.status === 1 ? '启用' : '禁用'
    ElMessage.success(`已${statusText}用户：${row.username}`)
  } catch (error) {
    row.status = row.status === 1 ? 0 : 1
    console.error('修改状态失败:', error)
  }
}

// 重置密码
const handleResetPwd = (row: any) => {
  currentUserId.value = row.id
  resetPwdForm.newPassword = ''
  resetPwdForm.confirmPassword = ''
  resetPwdVisible.value = true
}

// 提交重置密码
const handleResetPwdSubmit = async () => {
  if (!resetPwdRef.value || !currentUserId.value) return
  await resetPwdRef.value.validate(async (valid) => {
    if (!valid) return
    resetPwdLoading.value = true
    try {
      await resetPassword(String(currentUserId.value))
      resetPwdVisible.value = false
      ElMessage.success('密码重置成功')
    } catch (error) {
      console.error('重置密码失败:', error)
    } finally {
      resetPwdLoading.value = false
    }
  })
}

// 删除
const handleDelete = (row: any) => {
  ElMessageBox.confirm(`确定要删除用户“${row.username}”吗？`, '提示', {
    type: 'warning'
  }).then(async () => {
    try {
      await deleteUser(row.id)
      ElMessage.success('删除成功')
      fetchUserList()
    } catch (error) {
      console.error('删除失败:', error)
    }
  }).catch(() => {})
}

// 分页
const handleSizeChange = (val: number) => {
  pagination.pageSize = val
  pagination.page = 1
  fetchUserList()
}

const handleCurrentChange = (val: number) => {
  pagination.page = val
  fetchUserList()
}

onMounted(() => {
  fetchUserList()
})
</script>

<style scoped lang="scss">
.user-management {
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
</style>
