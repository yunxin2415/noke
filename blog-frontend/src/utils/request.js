import axios from 'axios'
import store from '@/store'

// 创建axios实例
const service = axios.create({
  baseURL: process.env.VUE_APP_API_BASE || '/api',
  timeout: 15000,
  headers: {
    'Content-Type': 'application/json',
    'Accept': 'application/json',
    'X-Requested-With': 'XMLHttpRequest'
  }
})

// 请求拦截器
service.interceptors.request.use(
  config => {
    // 在发送请求之前做些什么
    const token = store.state.token
    if (token) {
      // 对所有请求都添加token，除了登录和注册
      if (!config.url.includes('/auth/login') && !config.url.includes('/auth/register')) {
        config.headers['Authorization'] = `Bearer ${token}`
      }
    }
    
    // 处理文件上传和验证码请求的 Content-Type
    if (config.url.includes('/upload/')) {
      delete config.headers['Content-Type']
      config.headers['Accept'] = '*/*'
    } else if (config.url.includes('/auth/captcha')) {
      // 验证码请求特殊处理
      config.headers['Accept'] = 'image/*'
      config.headers['Cache-Control'] = 'no-cache'
      config.headers['Pragma'] = 'no-cache'
      if (config.responseType === 'blob') {
        delete config.headers['Content-Type']
      }
    } else if (config.method === 'post' || config.method === 'put') {
      config.headers['Content-Type'] = 'application/json'
    }
    
    // 确保请求头中包含正确的 Accept
    if (!config.headers['Accept']) {
      config.headers['Accept'] = 'application/json'
    }
    
    // 添加跨域支持
    config.withCredentials = true
    
    console.log('发送请求:', {
      url: config.url,
      baseURL: config.baseURL,
      method: config.method,
      headers: config.headers,
      params: config.params,
      data: config.data
    })
    return config
  },
  error => {
    console.error('请求错误:', error)
    return Promise.reject(error)
  }
)

// 响应拦截器
service.interceptors.response.use(
  response => {
    // 如果响应类型是 blob，直接返回
    if (response.config.responseType === 'blob') {
      return response
    }
    
    // 处理普通响应
    const responseData = response.data

    // 如果响应是空的，返回整个响应对象
    if (!responseData && responseData !== 0) {
      return response
    }

    // 如果响应包含错误信息，抛出错误
    if (responseData.error || responseData.code === 'ERROR') {
      const error = new Error(responseData.message || '请求失败')
      error.response = response
      throw error
    }

    // 如果响应是字符串类型，可能是错误消息
    if (typeof responseData === 'string') {
      if (responseData.includes('error') || responseData.includes('失败')) {
        throw new Error(responseData)
      }
      return responseData
    }

    // 如果响应状态码在 200-299 之间，返回响应数据
    if (response.status >= 200 && response.status < 300) {
      // 如果响应包含分页信息，直接返回整个响应数据
      if (responseData && typeof responseData === 'object' && 
          ('content' in responseData || 'totalElements' in responseData)) {
        // 确保content中的每个文章对象都有author字段
        if (responseData.content && Array.isArray(responseData.content)) {
          responseData.content = responseData.content.map(article => {
            if (!article.author) {
              article.author = { username: '未知' }
            }
            return article
          })
        }
        return responseData
      }
      // 如果响应数据是对象，并且包含data字段，返回data字段
      if (responseData && typeof responseData === 'object' && responseData.data !== undefined) {
        return responseData.data
      }
      // 否则返回整个响应数据对象
      return responseData
    }

    // 其他情况返回整个响应
    return response
  },
  error => {
    console.error('响应错误:', {
      status: error.response?.status,
      statusText: error.response?.statusText,
      data: error.response?.data,
      headers: error.response?.headers,
      config: error.config
    })
    
    if (error.response) {
      const { status, data } = error.response
      let errorMessage = data?.message || '请求失败，请稍后重试'
      
      switch (status) {
        case 400:
          if (data?.message?.includes('验证码')) {
            errorMessage = '验证码错误或已过期，请重新输入'
          } else {
            errorMessage = data?.message || '请求参数有误，请检查输入'
          }
          break
        case 401:
          store.dispatch('logout')
          errorMessage = data?.message || '请先登录后再操作'
          break
        case 403:
          errorMessage = data?.message || '您���有权限执行此操作'
          // 如果是权限问题，可能需要重新登录
          if (data?.message?.includes('token')) {
            store.dispatch('logout')
          }
          break
        case 404:
          errorMessage = data?.message || '请求的资源不存在'
          break
        case 429:
          errorMessage = '请求过于频繁，请稍后再试'
          break
        case 500:
          errorMessage = data?.message || '服务器出现错误，请稍后重试'
          break
        default:
          errorMessage = data?.message || '请求失败，请稍后重试'
      }
      
      const enhancedError = new Error(errorMessage)
      enhancedError.response = error.response
      enhancedError.data = error.response.data
      return Promise.reject(enhancedError)
    } else if (error.request) {
      const networkError = new Error('无法连接到服务器，请检查网络连接')
      networkError.request = error.request
      return Promise.reject(networkError)
    } else {
      return Promise.reject(new Error('请求发生错误，请稍后重试'))
    }
  }
)

export default service 