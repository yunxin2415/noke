import { createStore } from 'vuex'

function isTokenExpired(token) {
  if (!token) return true
  try {
    const base64Url = token.split('.')[1]
    const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/')
    const payload = JSON.parse(window.atob(base64))
    return payload.exp * 1000 < Date.now()
  } catch (e) {
    console.error('Token解析失败:', e)
    return true
  }
}

export default createStore({
  state: {
    user: null,
    token: null,
    initialized: false
  },
  getters: {
    isAuthenticated: state => {
      return !!state.token && !!state.user && !isTokenExpired(state.token)
    },
    isAdmin: state => state.user?.role === 'ROLE_ADMIN',
    currentUser: state => state.user,
    userAvatar: state => state.user?.avatar || '/default-avatar.png'
  },
  mutations: {
    initializeState(state) {
      try {
        const storedUser = localStorage.getItem('user')
        const storedToken = localStorage.getItem('token')
        
        if (storedUser && storedToken && !isTokenExpired(storedToken)) {
          const user = JSON.parse(storedUser)
          // 验证用户数据的完整性
          if (user && user.id && user.username) {
            state.user = user
            state.token = storedToken
          } else {
            // 数据不完整，清除存储
            state.user = null
            state.token = null
            localStorage.removeItem('user')
            localStorage.removeItem('token')
          }
        } else {
          state.user = null
          state.token = null
          localStorage.removeItem('user')
          localStorage.removeItem('token')
        }
        state.initialized = true
      } catch (error) {
        console.error('初始化状态失败:', error)
        state.user = null
        state.token = null
        localStorage.removeItem('user')
        localStorage.removeItem('token')
        state.initialized = true
      }
    },
    setUser(state, user) {
      // 验证用户数据的完整性
      if (user && (!user.id || !user.username)) {
        console.error('无效的用户数据:', user)
        return
      }
      
      state.user = user
      if (user) {
        localStorage.setItem('user', JSON.stringify(user))
      } else {
        localStorage.removeItem('user')
      }
    },
    setToken(state, token) {
      state.token = token
      if (token) {
        localStorage.setItem('token', token)
      } else {
        localStorage.removeItem('token')
      }
    },
    updateAvatar(state, avatar) {
      if (state.user) {
        state.user = {
          ...state.user,
          avatar: avatar || '/default-avatar.png'
        }
        localStorage.setItem('user', JSON.stringify(state.user))
      }
    },
    clearState(state) {
      state.user = null
      state.token = null
      localStorage.removeItem('user')
      localStorage.removeItem('token')
    }
  },
  actions: {
    initializeStore({ commit, dispatch }) {
      commit('initializeState')
      // 如果有token，检查其有效性
      const token = localStorage.getItem('token')
      if (token && isTokenExpired(token)) {
        console.warn('Token已过期，清除登录状态')
        dispatch('logout')
      }
    },
    login({ commit }, { user, token }) {
      if (!user || !user.id || !user.username || !token) {
        console.error('登录信息不完整:', { user, token })
        throw new Error('登录信息不完整')
      }
      
      // 验证token
      if (isTokenExpired(token)) {
        console.error('Token无效或已过期')
        throw new Error('Token无效或已过期')
      }
      
      commit('setUser', user)
      commit('setToken', token)
    },
    logout({ commit }) {
      console.log('执行登出操作')
      commit('clearState')
    },
    updateUserAvatar({ commit }, avatar) {
      commit('updateAvatar', avatar)
    },
    // 检查并修复用户状态
    checkAndRepairUserState({ commit, state, dispatch }) {
      if (!state.initialized) {
        commit('initializeState')
      }
      
      // 检查token是否存在且有效
      if (state.token) {
        if (isTokenExpired(state.token)) {
          console.warn('Token已过期，清除登录状态')
          dispatch('logout')
          return false
        }
        
        if (!state.user || !state.user.id) {
          console.warn('用户状态不一致，清除登录状态')
          dispatch('logout')
          return false
        }
      }
      
      return true
    }
  },
  modules: {
  }
}) 