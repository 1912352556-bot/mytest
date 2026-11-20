<script setup lang="ts">
/* 
    type UploadUserFile 上传文件的对象接口 
    type UploadProps 上传钩子函数的接口
*/
import {
    ElMessage, type FormInstance, type FormItemInstance
    , type FormRules, type UploadUserFile, type UploadProps, ElMessageBox
} from 'element-plus';
import { ref, reactive, toRefs } from 'vue'
/* 导入接口函数 */
import { register } from '@/api/dao';
import { useRouter, type Router } from 'vue-router'
/* 导入 md5 加密函数 */
import { getMd5Info } from '@/utils/md5';

let imageUrl = ref<string>('')
const router: Router = useRouter()

const myRegForm = ref<FormInstance | undefined>()

const regForm = reactive<RegUser>({
    username: '',
    password: '',
    realname: '',
    email: '',
    phone: '',
    gender: 0,  /* 默认性别为 男 */
    role: 0,    /* 默认权限为 用户 */
    path: '',    /* 封装上传成功后返回的 url 地址 */
    /* paths:[],  */   /* 注意 如果是批量上传则使用此数组 封装多个 url 地址 */
})
const { username, password, realname, email, phone, gender, role, path }
    = toRefs(regForm)

const regRules = reactive<FormRules<RegUser>>({
    username: [
        { required: true, message: '请输入用户姓名', trigger: 'blur' },
        { min: 6, max: 10, message: '长度6到10个字符', trigger: 'blur' },
        {
            pattern: /^[a-zA-Z0-9_]*$/, message: '只能英文数字下划线组成'
            , trigger: 'blur'
        }
    ],
    password: [
        { required: true, message: '请输入用户密码', trigger: 'blur' },
        { min: 6, max: 32, message: '长度6到32个字符', trigger: 'blur' },
        {
            pattern: /^[a-zA-Z0-9_]*$/, message: '只能英文数字下划线组成'
            , trigger: 'blur'
        }
    ],
    realname: [
        { required: true, message: '请输入真实姓名', trigger: 'blur' },
        { min: 6, max: 10, message: '长度6到10个字符', trigger: 'blur' },
        {
            pattern: /^[a-zA-Z0-9_]*$/, message: '只能英文数字下划线组成'
            , trigger: 'blur'
        }
    ],
    email: [
        { required: true, message: '请输入邮箱地址', trigger: 'blur' },
        {
            pattern: /^[A-Za-z0-9\u4e00-\u9fa5]+@[a-zA-Z0-9_-]+(\.[a-zA-Z0-9_-]+)+$/
            , message: '请输入合法邮箱地址', trigger: 'blur'
        }
    ],
    phone: [
        { required: true, message: '请输入电话号码', trigger: 'blur' },
        {
            pattern: /^(?:(?:\+|00)86)?1\d{10}$/
            , message: '请输入合法电话号码', trigger: 'blur'
        },
        /* {
            type:'number',message:'必须是数字',
        } */
    ],
    path: [
        { required: true, message: '请上传头像!', }
    ]
})
const resetForm = async (formEl: FormInstance | undefined) => {
    if (!formEl) return
    await formEl.resetFields()
}
const submitForm = async (formEl: FormInstance | undefined) => {
    if (!formEl) return
    formEl.validate(valid => {
        if (valid) {
            /* 给密码重新加密 */
            password.value = getMd5Info(password.value)
            /* 调用接口函数 */
            register(regForm).then(resp => {
                ElMessage.success('注册成功!')
                router.push({ name: 'login' })
            })
            return
        }
        ElMessage({
            type: 'warning',
            message: '表单验证失败!',
            showClose: true,
        })
    })
}
/* ------- 以下是 上传 的业务逻辑 ----------- */
/* 获取嵌套上传的 el-form-item 组件 */
const myupload = ref<FormItemInstance | undefined>()

/* 一定利用好这个数组,因为是单双向绑定的,是进行多个文件上传时非常好用 */
const fileList = ref<Array<UploadUserFile>>([
    /* {
        name: 'element-plus-logo.svg',
        url: 'https://element-plus.org/images/element-plus-logo.svg',
    },
    {
        name: 'element-plus-logo2.svg',
        url: 'https://element-plus.org/images/element-plus-logo.svg',
    }, */
])

/*  
    超过最大上传个数执行的钩子 
    uploadFiles 已经上传成功的文件的对象 通过length 可以获取已经上传成功的文件个数
    files 目前正在上传的文件对象,通过length 可以获取已经目前正在上传的文件个数
*/
const handleExceed: UploadProps['onExceed'] = (files, uploadFiles) => {
    ElMessage.warning('最多上传1个文件...')
}



const handleAvatarSuccess: UploadProps['onSuccess'] = (response, uploadFile) => {
    /* 给path赋值 */
    path.value = response.data.path
    imageUrl.value = response.data.path
    /* 手动去掉上传成功的验证提示 */
    myupload.value?.clearValidate()
}
/* 点击叉号执行删除之前的钩子 */
const beforeRemove: UploadProps['beforeRemove'] = (uploadFile, uploadFiles) => {
    return ElMessageBox.confirm(
        `您确定删除本文件吗?`
    ).then(
        () => true,
        () => false
    )
}
/* 点击叉号执行删除之后执行的钩子 */
const handleRemove: UploadProps['onRemove'] = (file, uploadFiles) => {
    path.value = ''
}


</script>

<template>
    <div class="flex-center vh100">
        <el-card style=" border-radius: 50px;">
            <el-form :model="regForm" :rules="regRules" size="small" class="flex-column flex-center w350" status-icon ref="myRegForm">
            <h1 class=" mg20">用户注册</h1>
            <el-form-item prop="username">
                <el-input v-model="username" clearable prefix-icon="User" autofocus class="w200"
                    placeholder="请输入用户姓名(必填)"></el-input>
            </el-form-item>
            <el-form-item prop="password">
                <el-input v-model="password" clearable prefix-icon="Lock" show-password class="w200"
                    placeholder="请输入用户密码(必填)"></el-input>
            </el-form-item>
            <el-form-item prop="realname">
                <el-input v-model="realname" clearable prefix-icon="UserFilled" class="w200"
                    placeholder="请输入真实姓名(必填)"></el-input>
            </el-form-item>
            <el-form-item prop="email">
                <el-input v-model="email" clearable prefix-icon="Eleme" class="w200"
                    placeholder="请输入邮箱地址(必填)"></el-input>
            </el-form-item>
            <el-form-item prop="phone">
                <el-input v-model="phone" clearable prefix-icon="Iphone" class="w200"
                    placeholder="请输入电话号码(必填)"></el-input>
            </el-form-item>
            <el-form-item prop="gender" label="性别:">
                <!-- 此处使用了 ElementPlus 的 RadioGroup 单选框组 -->
                <el-radio-group v-model="gender">
                    <!-- 
                        label 显示在页面上的文本 
                        :value="0" 这里的 0 是 number
                        :value="'0'" 这里的 0 是 字符串
                        value="0" 这里的 0 是 字符串
                    -->
                    <el-radio label="男" :value="0"></el-radio>
                    <el-radio label="女" :value="1"></el-radio>
                </el-radio-group>
            </el-form-item>
            <el-form-item prop="role" label="权限:">
                <el-radio-group v-model="role">
                    <el-radio label="用户" :value="0"></el-radio>
                    <el-radio label="管理员" :value="1"></el-radio>
                </el-radio-group>
            </el-form-item>
            <el-form-item prop="path" label="上传头像:" ref="myupload">
                <!-- 此处使用了 ElementPlus 的 Upload 上传组件
                注意此组件是 ElementPlus 中众多中里唯一一个 自带异步功能的组件
                它没有使用 axios 更没有使用我们自己封装的 自定义axios 实例 request
                因此无需书写接口函数 注意由于没有使用 axios 此组件服务器返回的
                对象 response 使用时不要再书写两个 data 了 
                    action 异步上传的完整地址
                    multiple 是否允许一次上传多个文件,如果书写此属性则允许按住
                    shift键一次上传多个文件
                    v-model:file-list="fileList" 
                    这里单双向绑定了一个数组,这个数组用来封装上传成功后的对象,
                    注意只要我们上传成功,则自动添加进数组,上传失败,自动从数组删除
                    一切都是自动的单双向绑定的 注意此数组是一个 RefImpl 
                    :limit="1" 表示最多上传文件数,如果超过后面的数字,则执行
                    钩子函数
                    :on-exceed="handleExceed" 超过最大上传数激发此函数
                    :before-upload="beforeAvatarUpload" 上传之前执行的最后一个钩子
                    一般用来进行验证
                    :on-success="handleAvatarSuccess" 上传成功之后执行的钩子
                    :on-remove="handleRemove"    删除之前问询
                    :before-remove="beforeRemove" 删除上传的文件
                -->
                <!-- <el-upload class="avatar-uploader w200 h200" action="http://db.etoak.com:9527/sysFile/fileUpload"
                    :show-file-list="false" :on-success="handleAvatarSuccess">
                    <img v-if="imageUrl" :src="imageUrl" class="avatar" />
                    <el-icon v-else class="avatar-uploader-icon">
                        <Plus />
                    </el-icon>
                </el-upload> -->
                <el-upload action="http://db.etoak.com:9527/sysFile/fileUpload" list-type="picture-card"
                    :auto-upload="true" :show-file-list="false" :limit="1" :on-success="handleAvatarSuccess"
                    :on-exceed="handleExceed">
                    <img v-if="imageUrl" :src="imageUrl" class="avatar" style="width: 100%;"/>
                    <el-icon v-else >
                        <Plus />
                    </el-icon>


                </el-upload>
            </el-form-item>
            <el-form-item>
                <el-button type="primary" plain round @click="router.push({name:'login'})">登录</el-button>
                <el-button type="primary" round @click="submitForm(myRegForm)">注册</el-button>
                <el-button type="info" round @click="resetForm(myRegForm)">取消</el-button>
            </el-form-item>
        </el-form>
        </el-card>
    </div>
</template>

<style scoped></style>