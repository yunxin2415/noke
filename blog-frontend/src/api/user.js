import request from '@/utils/request'

// 获取用户信息
export const getUserProfile = async () => {
  try {
    const response = await request({
      url: '/users/profile',
      method: 'get'
    })
    
    // 验证响应数据
    if (!response || typeof response !== 'object') {
      throw new Error('获取用户信息失败：响应格式错误')
    }
    
    return response
  } catch (error) {
    console.error('获取用户信息失败:', error)
    throw error
  }
}

// 更新用户信息
export const updateUserProfile = async (data) => {
  try {
    // 确保只发送允许的字段
    const updateData = {
      email: data.email,
      bio: data.bio,
      avatar: data.avatar
    }

    const response = await request({
      url: '/users/profile',
      method: 'put',
      data: updateData
    })
    
    // 验证响应数据
    if (!response || typeof response !== 'object') {
      throw new Error('更新用户信息失败：响应格式错误')
    }
    
    return response
  } catch (error) {
    console.error('更新用户信息失败:', error)
    throw error
  }
}

// 修改密��
export const changePassword = async (data) => {
  try {
    const response = await request({
      url: '/users/password',
      method: 'put',
      data: {
        currentPassword: data.currentPassword,
        newPassword: data.newPassword,
        confirmPassword: data.confirmPassword
      }
    })
    
    return response
  } catch (error) {
    console.error('修改密码失败:', error)
    throw error
  }
}

// 关注用户
export const followUser = async (userId) => {
  try {
    const response = await request({
      url: `/users/${userId}/follow`,
      method: 'post'
    })
    
    return response
  } catch (error) {
    console.error('关注失败:', error)
    throw error
  }
}

// 取消关注用户
export const unfollowUser = async (userId) => {
  try {
    const response = await request({
      url: `/users/${userId}/unfollow`,
      method: 'post'
    })
    
    return response
  } catch (error) {
    console.error('取消关注失败:', error)
    throw error
  }
}

// 检查是否已关注用户
export const checkFollowing = async (userId) => {
  try {
    const response = await request({
      url: `/users/${userId}/following`,
      method: 'get'
    })
    return response.data
  } catch (error) {
    console.error('检查关注状态失败:', error)
    throw error
  }
}

// 注销账户
export const deleteAccount = async (password) => {
  try {
    const response = await request({
      url: '/users/account',
      method: 'delete',
      data: { password }
    })
    return response
  } catch (error) {
    console.error('注销账户失败:', error)
    throw error
  }
} 