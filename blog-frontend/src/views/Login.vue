<template>
  <div class="login-container">
    <el-card class="login-card">
      <template #header>
        <div class="card-header">
          <el-button 
            class="back-button" 
            :icon="ArrowLeft"
            @click="$router.push('/')" 
            text
          >返回首页</el-button>
          <h2>登录</h2>
        </div>
      </template>
      <el-form :model="loginForm" :rules="rules" ref="loginFormRef" @submit.prevent="handleLogin">
        <el-form-item prop="username">
          <el-input 
            v-model="loginForm.username" 
            placeholder="用户名"
            prefix-icon="User"
            @keyup.enter="handleLogin"
          />
        </el-form-item>
        <el-form-item prop="password">
          <el-input 
            v-model="loginForm.password" 
            type="password" 
            placeholder="密码"
            prefix-icon="Lock"
            show-password
            @keyup.enter="handleLogin"
          />
        </el-form-item>
        <div v-if="errorMessage" class="error-message">
          {{ errorMessage }}
        </div>
        <el-form-item>
          <el-button type="primary" @click="handleLogin" :loading="loading" style="width: 100%">
            {{ loading ? '登录中...' : '登录' }}
          </el-button>
        </el-form-item>
      </el-form>
      <div class="login-links">
        <router-link to="/register">还没有账号？立即注册</router-link>
      </div>
    </el-card>
  </div>
</template>

<script>
import { ref, reactive } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { login } from '@/api/auth'
import { useStore } from 'vuex'
import { ArrowLeft } from '@element-plus/icons-vue'

export default {
  name: 'LoginPage',
  setup() {
    const router = useRouter()
    const route = useRoute()
    const store = useStore()
    const loginFormRef = ref(null)
    const loading = ref(false)
    const errorMessage = ref('')

    const loginForm = reactive({
      username: '',
      password: ''
    })

    const rules = {
      username: [
        { required: true, message: '请输入用户名', trigger: 'blur' },
        { min: 2, max: 20, message: '用户名长度在 2 到 20 个字符', trigger: 'blur' }
      ],
      password: [
        { required: true, message: '请输入密码', trigger: 'blur' },
        { min: 6, message: '密码不能少于 6 个字符', trigger: 'blur' }
      ]
    }

    const handleLogin = async () => {
      if (!loginFormRef.value) return
      
      try {
        // 验证表单
        await loginFormRef.value.validate()
        
        // 开始加载
        loading.value = true
        errorMessage.value = ''
        
        // 发送登录请求
        const response = await login(loginForm)
        
        // 检查响应
        if (!response || !response.token || !response.user) {
          throw new Error('登录失败，请稍后重试')
        }
        
        // 存储登录状态
        await store.dispatch('login', {
          token: response.token,
          user: response.user
        })
        
        // 登录成功提示
        ElMessage.success('登录成功')
        
        // 跳转
        const redirect = route.query.redirect || '/'
        await router.push(redirect)
        
      } catch (error) {
        console.error('登录失败:', error)
        errorMessage.value = error.displayMessage || '登录失败，请稍后重试'
        
        // 清除可能存在的无效状态
        store.dispatch('logout')
      } finally {
        loading.value = false
      }
    }

    return {
      loginForm,
      loginFormRef,
      loading,
      rules,
      handleLogin,
      errorMessage,
      ArrowLeft,
    }
  }
}
</script>

<style scoped>
.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  padding: 20px;
  background: linear-gradient(135deg, var(--el-color-primary-light-7) 0%, var(--el-color-primary-light-9) 100%);
}

.login-card {
  width: 100%;
  max-width: 400px;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
}

.card-header {
  position: relative;
  text-align: center;
  padding: 20px 0;
}

.back-button {
  position: absolute;
  left: 0;
  top: 50%;
  transform: translateY(-50%);
  font-size: 14px;
}

.card-header h2 {
  margin: 0;
  font-size: 1.8rem;
  font-weight: 600;
  color: var(--el-text-color-primary);
}

.el-form {
  padding: 20px;
}

.el-form-item {
  margin-bottom: 25px;
}

.el-input {
  --el-input-height: 45px;
  font-size: 1.1rem;
}

.el-button {
  height: 45px;
  font-size: 1.1rem;
}

.error-message {
  color: var(--el-color-danger);
  font-size: 14px;
  text-align: center;
  padding: 8px;
  margin-bottom: 16px;
  background-color: var(--el-color-danger-light-9);
  border-radius: 4px;
  border: 1px solid var(--el-color-danger-light-7);
}

.login-links {
  text-align: center;
  padding: 0 20px 20px;
}

.login-links a {
  color: var(--el-color-primary);
  text-decoration: none;
  font-size: 14px;
}

.login-links a:hover {
  color: var(--el-color-primary-light-3);
  text-decoration: underline;
}
</style> 