<template>
  <div class="login-container">
    <div class="login-card">
      <h2 class="title">LinMelodyTicket 登录</h2>
      <el-form :model="form" :rules="rules" ref="formRef" size="large">
        <el-form-item prop="username">
          <el-input v-model="form.username" placeholder="用户名" :prefix-icon="User" />
        </el-form-item>
        <el-form-item prop="password">
          <el-input v-model="form.password" type="password" placeholder="密码" :prefix-icon="Lock" show-password />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleLogin" :loading="loading" style="width: 100%">登录</el-button>
        </el-form-item>
      </el-form>
      <div class="register-link">
        还没有账号？<router-link to="/register">立即注册</router-link>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Lock } from '@element-plus/icons-vue'
import request from '../utils/request'

const router = useRouter()
const formRef = ref(null)
const loading = ref(false)

const form = reactive({
  username: '',
  password: ''
})

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

async function handleLogin() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  loading.value = true
  try {
    const res = await request.post('/users/login', form)
    if (res.code === 200) {
      localStorage.setItem('token', res.data.token)
      localStorage.setItem('userId', res.data.user.id)
      localStorage.setItem('role', res.data.user.role || 'USER')
      localStorage.setItem('username', res.data.user.nickname || res.data.user.username)
      ElMessage.success('登录成功')
      router.push('/')
    } else {
      ElMessage.error(res.message)
    }
  } catch (e) {
    // Error already handled by interceptor
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-container {
  height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #f8bbd0 0%, #f48fb1 100%);
}
.login-card {
  width: 420px;
  padding: 40px;
  background: white;
  border-radius: 16px;
  box-shadow: 0 20px 60px rgba(0,0,0,0.3);
}
.title {
  text-align: center;
  margin-bottom: 30px;
  color: #333;
  font-size: 24px;
}
.register-link {
  text-align: center;
  margin-top: 15px;
  color: #999;
}
.register-link a { color: #f06292; text-decoration: none; }
</style>
