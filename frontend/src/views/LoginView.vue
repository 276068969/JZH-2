<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { Lock, User } from '@element-plus/icons-vue'
import { useAuthStore } from '@/stores/auth'
import { ElMessage } from 'element-plus'

const router = useRouter()
const authStore = useAuthStore()

const loginFormRef = ref()
const loading = ref(false)

const loginForm = reactive({
  username: 'admin',
  password: 'admin123'
})

const loginRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

async function handleLogin() {
  const valid = await loginFormRef.value?.validate().catch(() => false)
  if (!valid) return

  loading.value = true
  try {
    const result = await authStore.login(loginForm.username, loginForm.password)
    if (result.warning) {
      ElMessage.warning(result.warning)
    } else {
      ElMessage.success('登录成功')
    }
    router.push('/dashboard')
  } catch (err: any) {
    const msg = err?.response?.data?.message || err?.message || '登录失败'
    ElMessage.error(msg + '，请检查用户名或密码，或确认后端服务已启动')
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="login-container">
    <div class="login-bg">
      <div class="bg-shape shape1"></div>
      <div class="bg-shape shape2"></div>
      <div class="bg-shape shape3"></div>
    </div>

    <div class="login-card">
      <div class="card-header">
        <div class="header-icon">
          <el-icon :size="40"><Lock /></el-icon>
        </div>
        <h1 class="system-title">监狱综合管理平台</h1>
        <p class="system-subtitle">Prison Management Platform</p>
      </div>

      <el-form
        ref="loginFormRef"
        :model="loginForm"
        :rules="loginRules"
        class="login-form"
        size="large"
      >
        <el-form-item prop="username">
          <el-input
            v-model="loginForm.username"
            placeholder="请输入用户名"
            :prefix-icon="User"
          />
        </el-form-item>

        <el-form-item prop="password">
          <el-input
            v-model="loginForm.password"
            type="password"
            placeholder="请输入密码"
            :prefix-icon="Lock"
            show-password
            @keyup.enter="handleLogin"
          />
        </el-form-item>

        <el-form-item>
          <el-button
            type="primary"
            :loading="loading"
            class="login-btn"
            @click="handleLogin"
          >
            {{ loading ? '登录中...' : '登 录' }}
          </el-button>
        </el-form-item>
      </el-form>

      <div class="card-footer">
        <div class="account-tips">
          <p class="tips-title">可登录账号（密码 = 账号 + 123）：</p>
          <p class="tips-row">
            <el-tag type="danger" size="small">admin/admin123</el-tag>
            <span>超级管理员</span>
          </p>
          <p class="tips-row">
            <el-tag type="warning" size="small">manager/manager123</el-tag>
            <span>监狱管理员</span>
          </p>
          <p class="tips-row">
            <el-tag type="success" size="small">doctor/doctor123</el-tag>
            <span>医务人员（推荐）</span>
          </p>
          <p class="tips-row">
            <el-tag size="small">guard/guard123</el-tag>
            <span>狱警</span>
          </p>
          <p class="tips-row">
            <el-tag type="info" size="small">viewer/viewer123</el-tag>
            <span>只读查看</span>
          </p>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.login-container {
  width: 100%;
  height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #0a1628 0%, #1a2a4a 50%, #0d1b33 100%);
  position: relative;
  overflow: hidden;
}

.login-bg {
  position: absolute;
  inset: 0;
  pointer-events: none;
}

.bg-shape {
  position: absolute;
  border-radius: 50%;
  opacity: 0.06;
  background: #409eff;
}

.shape1 {
  width: 600px;
  height: 600px;
  top: -200px;
  right: -150px;
}

.shape2 {
  width: 400px;
  height: 400px;
  bottom: -100px;
  left: -100px;
}

.shape3 {
  width: 250px;
  height: 250px;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
}

.login-card {
  width: 420px;
  background: rgba(255, 255, 255, 0.95);
  border-radius: 12px;
  padding: 40px 36px 30px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
  z-index: 1;
  backdrop-filter: blur(10px);
}

.card-header {
  text-align: center;
  margin-bottom: 32px;
}

.header-icon {
  width: 70px;
  height: 70px;
  margin: 0 auto 16px;
  background: linear-gradient(135deg, #409eff, #1a3a6b);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
}

.system-title {
  font-size: 22px;
  color: #1a2a4a;
  font-weight: 700;
  margin: 0 0 6px;
  letter-spacing: 2px;
}

.system-subtitle {
  font-size: 12px;
  color: #909399;
  letter-spacing: 1px;
  margin: 0;
}

.login-form {
  margin-top: 8px;
}

.login-btn {
  width: 100%;
  height: 44px;
  font-size: 16px;
  letter-spacing: 4px;
  background: linear-gradient(135deg, #409eff, #1a3a6b);
  border: none;
}

.login-btn:hover {
  background: linear-gradient(135deg, #66b1ff, #2a4a8b);
}

.card-footer {
  text-align: left;
  margin-top: 20px;
  font-size: 12px;
  color: #909399;
  background: #f5f7fa;
  border-radius: 6px;
  padding: 12px;
}

.account-tips .tips-title {
  margin: 0 0 8px;
  font-weight: 600;
  color: #606266;
  font-size: 12px;
}

.account-tips .tips-row {
  margin: 4px 0;
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 12px;
  color: #909399;
}
</style>
