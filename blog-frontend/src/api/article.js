import request from '@/utils/request'
import { ElMessage } from 'element-plus'

// 获取文章列表
export const getArticles = async (params = {}) => {
  try {
    // 提取有效参数
    const { 
      page = 1, 
      size = 10, 
      type = '',
      category = '',
      tag = ''
    } = params
    
    const response = await request({
      url: '/articles',
      method: 'get',
      headers: {
        'Accept': 'application/json'
      },
      params: {
        page,
        size,
        type,
        category,
        tag
      }
    })
    return response
  } catch (error) {
    console.error('获取文章列表失败:', error)
    throw error
  }
}

// 获取所有分类
export const getCategories = async () => {
  try {
    const response = await request({
      url: '/categories',
      method: 'get'
    })
    return response
  } catch (error) {
    console.error('获取分类列表失败:', error)
    throw error
  }
}

// 获取所有标签
export const getTags = async () => {
  try {
    const response = await request({
      url: '/tags',
      method: 'get'
    })
    return response
  } catch (error) {
    console.error('获取标签列表失败:', error)
    throw error
  }
}

// 获取用户文章列表
export const getUserArticles = async () => {
  try {
    console.log('开始获取用户文章列表');
    const response = await request({
      url: '/articles/user',
      method: 'get',
      headers: {
        'Accept': 'application/json'
      }
    });
    
    console.log('获取用户文章列表原始响应:', response);
    
    // 检查响应格式
    if (Array.isArray(response)) {
      console.log('响应是数组格式，文章数量:', response.length);
      return response;
    } else if (response && Array.isArray(response.data)) {
      console.log('响应包含data数组，文章数量:', response.data.length);
      return response.data;
    } else if (response && response.content && Array.isArray(response.content)) {
      console.log('响应包含content数组，文章数量:', response.content.length);
      return response.content;
    } else if (response && typeof response === 'object' && Object.keys(response).length > 0) {
      // 如果响应是非空对象，尝试将其转换为数组
      console.log('响应是对象格式，尝试转换:', response);
      const articles = [];
      for (const key in response) {
        if (typeof response[key] === 'object' && response[key] !== null) {
          articles.push(response[key]);
        }
      }
      if (articles.length > 0) {
        console.log('成功将对象转换为数组，文章数量:', articles.length);
        return articles;
      }
    }
    
    console.warn('响应格式异常:', response);
    ElMessage.warning('获取文章列表格式异常');
    return [];
  } catch (error) {
    console.error('获取用户文章列表失败:', error);
    if (error.response?.status === 401) {
      ElMessage.error('请先登录后再查看文章');
    } else {
      ElMessage.error(error.response?.data?.message || '获取文章列表失败');
    }
    throw error;
  }
}

// 获取文章详情
export const getArticle = async (id) => {
  try {
    const response = await request({
      url: `/articles/${id}`,
      method: 'get',
      headers: {
        'Accept': 'application/json'
      }
    })
    return response.data
  } catch (error) {
    console.error('获取文章详情失败:', error)
    throw error
  }
}

// 创建文章
export const createArticle = async (data) => {
  try {
    const response = await request({
      url: '/articles',
      method: 'post',
      headers: {
        'Content-Type': 'application/json',
        'Accept': 'application/json'
      },
      data: {
        title: data.title,
        content: data.content,
        category: data.category || '默认分类',
        tags: data.tags || '',
        isPrivate: !!data.isPrivate,
        isDownloadable: !!data.isDownloadable
      }
    })

    // 直接返回响应，因为响应拦截器已经处理了数据提取
    return response
  } catch (error) {
    console.error('创建文章失败:', error)
    // 重新包装错误以包含更多信息
    const enhancedError = new Error(error.response?.data?.message || error.message)
    enhancedError.response = error.response
    enhancedError.data = error.response?.data
    throw enhancedError
  }
}

// 更新文章
export const updateArticle = async (data) => {
  try {
    const response = await request({
      url: `/articles/${data.id}`,
      method: 'put',
      data: {
        category: data.category,
        tags: data.tags,
        isPrivate: data.isPrivate,
        isDownloadable: data.isDownloadable
      }
    })
    return response
  } catch (error) {
    console.error('更新文章失败:', error)
    throw error
  }
}

// 删除文章
export const deleteArticle = async (id) => {
  try {
    const response = await request({
      url: `/articles/${id}`,
      method: 'delete',
      headers: {
        'Accept': 'application/json'
      }
    })
    ElMessage.success('文章删除成功')
    return response
  } catch (error) {
    console.error('删除文章失败:', error)
    ElMessage.error(error.response?.data?.message || '删除文章失败')
    throw error
  }
}

// 下载文章
export const downloadArticle = async (id) => {
  try {
    const response = await request({
      url: `/articles/${id}/download`,
      method: 'get',
      responseType: 'blob',
      headers: {
        'Accept': 'application/octet-stream'
      }
    })
    return response.data
  } catch (error) {
    console.error('下载文章失败:', error)
    throw error
  }
} 