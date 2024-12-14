import request from '@/utils/request'
import { ElMessage } from 'element-plus'
import store from '@/store'
import router from '@/router'

// 登录
export const login = async (credentials) => {
  try {
    const response = await request({
      url: '/auth/login',
      method: 'post',
      data: credentials
    })

    // 验证响应数据的完整性
    if (!response || typeof response !== 'object') {
      throw new Error('登录失败，请稍后重试')
    }

    // 如果response本身就包含token和user，就直接使用
    const data = response.data || response
    
    if (!data.token || !data.user) {
      throw new Error('登录失败，服务器响应异常')
    }

    const { token, user } = data
    
    // 保存认证信息到 store
    await store.dispatch('login', { token, user })
    
    return data
  } catch (error) {
    console.error('登录失败:', error)
    let errorMessage = '登录失败，请稍后重试'
    
    if (error.response?.status === 400) {
      errorMessage = error.response.data?.message || '用户名或密码错误'
    } else if (error.response?.status === 401) {
      errorMessage = error.response.data?.message || '登录失败，请检查用户名和密码'
    } else if (error.response?.status === 429) {
      errorMessage = '登录尝试次数过多，请稍后再试'
    } else if (error.message) {
      errorMessage = error.message
    }
    
    error.displayMessage = errorMessage
    throw error
  }
}

// 注册
export const register = async (userData) => {
  try {
    // 验证必要字段
    if (!userData.username || !userData.email || !userData.password || !userData.captcha || !userData.captchaId) {
      throw new Error('注册信息不完整')
    }

    const response = await request({
      url: '/auth/register',
      method: 'post',
      headers: {
        'Content-Type': 'application/json',
        'X-Captcha-ID': userData.captchaId
      },
      data: {
        username: userData.username.trim(),
        email: userData.email.trim(),
        password: userData.password,
        captcha: userData.captcha.trim()
      }
    })

    return response.data
  } catch (error) {
    console.error('注册失败:', error)
    let errorMessage = '注册失败，请稍后重试'
    
    if (error.response?.status === 400) {
      if (error.response.data?.message?.includes('验证码')) {
        errorMessage = '验证码错误或已过期，请重新输入'
      } else if (error.response.data?.message?.includes('用户名')) {
        errorMessage = '用户名已被使用'
      } else if (error.response.data?.message?.includes('邮箱')) {
        errorMessage = '邮箱已被注册'
      } else {
        errorMessage = error.response.data?.message || '注册信息有误'
      }
    } else if (error.response?.status === 429) {
      errorMessage = '请求过于频繁，请稍后再试'
    } else if (error.message) {
      errorMessage = error.message
    }
    
    error.displayMessage = errorMessage
    throw error
  }
}

// 检查用户名是否存在
export const checkUsername = async (username) => {
  try {
    const response = await request({
      url: `/auth/check/${username}`,
      method: 'get'
    })
    return response.data
  } catch (error) {
    console.error('检查用户名失败:', error)
    throw error
  }
}

// 退出登录
export const logout = () => {
  store.dispatch('logout')
  router.push('/login')
  ElMessage.success('已退出登录')
}

// 获取验证码
export const getCaptcha = async () => {
  try {
    const timestamp = new Date().getTime() // 添加时间戳防止缓存
    const response = await request({
      url: `/auth/captcha?t=${timestamp}`,
      method: 'get',
      responseType: 'blob',
      headers: {
        'Accept': 'image/*',
        'Cache-Control': 'no-cache',
        'Pragma': 'no-cache'
      }
    })
    
    // 检查响应头中的验证码ID
    const captchaId = response.headers['x-captcha-id']
    if (!captchaId) {
      throw new Error('验证码响应缺少ID')
    }
    
    // 检查响应类型
    const contentType = response.headers['content-type']
    if (!contentType || !contentType.includes('image/')) {
      throw new Error('验证码响应类型错误')
    }
    
    // 检查响应数据
    if (!(response.data instanceof Blob)) {
      throw new Error('验证码响应格式错误')
    }
    
    return response
  } catch (error) {
    console.error('获取验证码失败:', error)
    if (error.response?.status === 429) {
      ElMessage.error('请求过于频繁，请稍后再试')
    } else if (error.response?.status === 406) {
      ElMessage.error('服务器不支持当前请求格式，请联系管理员')
    } else {
      ElMessage.error(error.message || '获取验证码失败，请刷新重试')
    }
    throw error
  }
} 