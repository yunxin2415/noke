<template>
  <div class="register-container">
    <el-card class="register-card">
      <template #header>
        <h2>注册</h2>
      </template>
      <el-form :model="registerForm" :rules="rules" ref="registerFormRef">
        <el-form-item prop="username">
          <el-input 
            v-model="registerForm.username" 
            placeholder="用户名"
            prefix-icon="User"
          />
        </el-form-item>
        <el-form-item prop="email">
          <el-input 
            v-model="registerForm.email" 
            placeholder="邮箱"
            prefix-icon="Message"
          />
        </el-form-item>
        <el-form-item prop="password">
          <el-input 
            v-model="registerForm.password" 
            type="password" 
            placeholder="密码"
            prefix-icon="Lock"
            show-password
          />
        </el-form-item>
        <el-form-item prop="confirmPassword">
          <el-input 
            v-model="registerForm.confirmPassword" 
            type="password" 
            placeholder="确认密码"
            prefix-icon="Lock"
            show-password
          />
        </el-form-item>
        <el-form-item prop="captcha">
          <div class="captcha-container">
            <el-input 
              v-model="registerForm.captcha" 
              placeholder="验证码"
              class="captcha-input"
            />
            <div class="captcha-image" @click="refreshCaptcha">
              <img v-if="captchaImage" :src="captchaImage" alt="点击刷新验证码" title="点击刷新验证码" />
              <div v-else class="captcha-loading">
                <el-icon class="is-loading"><Loading /></el-icon>
              </div>
            </div>
          </div>
        </el-form-item>
        <div v-if="errorMessage" class="error-message">
          {{ errorMessage }}
        </div>
        <el-form-item>
          <el-button type="primary" @click="handleRegister" :loading="loading" style="width: 100%">
            注册
          </el-button>
        </el-form-item>
      </el-form>
      <div class="login-links">
        <router-link to="/login">已有账号？立即登录</router-link>
      </div>
    </el-card>
  </div>
</template>

<script>
import { ref, reactive, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { register, getCaptcha } from '@/api/auth'
import { Loading } from '@element-plus/icons-vue'

export default {
  name: 'RegisterPage',
  components: {
    Loading
  },
  setup() {
    const router = useRouter()
    const registerFormRef = ref(null)
    const loading = ref(false)
    const errorMessage = ref('')
    const captchaImage = ref('')
    const captchaId = ref('')
    
    const registerForm = reactive({
      username: '',
      email: '',
      password: '',
      confirmPassword: '',
      captcha: ''
    })

    const validatePass = (rule, value, callback) => {
      if (value === '') {
        callback(new Error('请输入密码'))
      } else {
        if (registerForm.confirmPassword !== '') {
          registerFormRef.value.validateField('confirmPassword')
        }
        callback()
      }
    }

    const validatePass2 = (rule, value, callback) => {
      if (value === '') {
        callback(new Error('请再次输入密码'))
      } else if (value !== registerForm.password) {
        callback(new Error('两次输入密码不一致!'))
      } else {
        callback()
      }
    }

    const rules = {
      username: [
        { required: true, message: '请输入用户名', trigger: 'blur' },
        { min: 3, max: 20, message: '用户名长度在 3 到 20 ��字符', trigger: 'blur' }
      ],
      email: [
        { required: true, message: '请输入邮箱地址', trigger: 'blur' },
        { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }
      ],
      password: [
        { required: true, validator: validatePass, trigger: 'blur' },
        { min: 6, message: '密码长度不能小于6个字符', trigger: 'blur' },
        { 
          validator: (rule, value, callback) => {
            const hasNumber = /\d/.test(value);
            const hasLetter = /[a-zA-Z]/.test(value);
            if (!hasNumber || !hasLetter) {
              callback(new Error('密码必须包含数字和字母'));
            } else {
              callback();
            }
          }, 
          trigger: 'blur' 
        }
      ],
      confirmPassword: [
        { required: true, validator: validatePass2, trigger: 'blur' }
      ],
      captcha: [
        { required: true, message: '请输入验证码', trigger: 'blur' },
        { min: 4, max: 4, message: '验证码长度为4位', trigger: 'blur' }
      ]
    }

    // 获取验证码
    const getCaptchaImage = async () => {
      try {
        console.log('开始获取验证码')
        const response = await getCaptcha()
        
        // 从响应头中获取验证码ID
        const captchaIdHeader = response.headers['x-captcha-id']
        console.log('验证码响应头:', response.headers)
        
        if (captchaIdHeader) {
          console.log('获取到验证码ID:', captchaIdHeader)
          captchaId.value = captchaIdHeader
        } else {
          console.warn('未找到验证码ID')
          throw new Error('验证码生成失败')
        }
        
        // 检查响应数据类型
        if (!(response.data instanceof Blob)) {
          console.error('验证码响应格式错误:', response.data)
          throw new Error('验证码响应格式错误')
        }
        
        // 检查响应类型
        const contentType = response.headers['content-type']
        if (!contentType || !contentType.includes('image/')) {
          console.error('验证码响应类型错误:', contentType)
          throw new Error('验证码响应类型错误')
        }
        
        // 将blob数据转换为URL
        const blob = new Blob([response.data], { type: contentType })
        const imageUrl = URL.createObjectURL(blob)
        captchaImage.value = imageUrl
        console.log('验证码图片加载成功')
      } catch (error) {
        console.error('获取验证码失败:', error)
        ElMessage.error(error.message || '获取验证码失败，请点击刷新')
        captchaImage.value = ''
        captchaId.value = ''
      }
    }

    // 刷新验证码
    const refreshCaptcha = () => {
      if (captchaImage.value) {
        URL.revokeObjectURL(captchaImage.value) // 释放旧的URL
      }
      registerForm.captcha = ''  // 清空验证码输入
      captchaImage.value = ''    // 清空验证码图片
      getCaptchaImage()          // 重新获取验证码
    }

    const handleRegister = async () => {
      if (!registerFormRef.value) return
      errorMessage.value = ''
      
      try {
        // 验证表单
        await registerFormRef.value.validate()
        
        // 验证验证码是否已加载
        if (!captchaId.value) {
          errorMessage.value = '验证码未加载，请刷新重试'
          refreshCaptcha()
          return
        }

        // 验证验证码是否已输入
        if (!registerForm.captcha) {
          errorMessage.value = '请输入验证码'
          return
        }
        
        loading.value = true
        
        // 发送注册请求
        await register({
          username: registerForm.username.trim(),
          email: registerForm.email.trim(),
          password: registerForm.password,
          captcha: registerForm.captcha.trim(),
          captchaId: captchaId.value
        })

        ElMessage.success('注册成功，即将跳转到登录页面')
        
        // 注册成功后延迟跳转
        setTimeout(() => {
          router.push('/login')
        }, 1500)
      } catch (error) {
        console.error('注册处理错误:', error)
        errorMessage.value = error.displayMessage || '注册失败，请稍后重试'
        // 如果是验证码错误，刷新验证码
        if (error.response?.data?.message?.includes('验证码')) {
          refreshCaptcha()
        }
      } finally {
        loading.value = false
      }
    }

    // 组件挂载时获取验证码
    onMounted(() => {
      getCaptchaImage()
    })

    // 组件卸载时清理资源
    onUnmounted(() => {
      if (captchaImage.value) {
        URL.revokeObjectURL(captchaImage.value)
      }
    })

    return {
      registerForm,
      registerFormRef,
      loading,
      errorMessage,
      rules,
      handleRegister,
      captchaImage,
      refreshCaptcha
    }
  }
}
</script>

<style scoped>
.register-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  padding: 20px;
  background: linear-gradient(135deg, var(--el-color-primary-light-7) 0%, var(--el-color-primary-light-9) 100%);
}

.register-card {
  width: 100%;
  max-width: 400px;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
}

.card-header {
  text-align: center;
  padding: 20px 0;
}

.card-header span {
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
  text-align: center;
  color: var(--el-color-danger);
  font-size: 0.9rem;
  margin-top: 15px;
  padding: 12px;
  background-color: var(--el-color-danger-light-9);
  border-radius: 4px;
  border: 1px solid var(--el-color-danger-light-5);
  animation: shake 0.5s;
}

.captcha-container {
  display: flex;
  gap: 10px;
  align-items: center;
}

.captcha-input {
  flex: 1;
}

.captcha-image {
  width: 120px;
  height: 45px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  background-color: var(--el-color-info-light-9);
  border: 1px solid var(--el-border-color);
  border-radius: 4px;
  overflow: hidden;
  transition: all 0.3s ease;
}

.captcha-image:hover {
  opacity: 0.8;
  border-color: var(--el-color-primary);
}

.captcha-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.captcha-loading {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 100%;
  height: 100%;
  color: var(--el-color-primary);
  font-size: 24px;
}

@keyframes shake {
  0%, 100% { transform: translateX(0); }
  25% { transform: translateX(-5px); }
  75% { transform: translateX(5px); }
}
</style> 