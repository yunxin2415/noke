import { createRouter, createWebHistory } from 'vue-router'
import store from '@/store'
import Admin from '@/views/Admin.vue'

const routes = [
  {
    path: '/',
    name: 'HomePage',
    component: () => import('../views/Home.vue'),
    props: route => ({ type: route.query.type || '' })
  },
  {
    path: '/login',
    name: 'LoginPage',
    component: () => import('../views/Login.vue')
  },
  {
    path: '/register',
    name: 'RegisterPage',
    component: () => import('../views/Register.vue')
  },
  {
    path: '/article/:id',
    name: 'ArticlePage',
    component: () => import('../views/Article.vue')
  },
  {
    path: '/create',
    name: 'CreateArticlePage',
    component: () => import('../views/CreateArticle.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/edit/:id',
    name: 'EditArticlePage',
    component: () => import('../views/EditArticle.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/user',
    name: 'UserCenter',
    component: () => import('../views/UserCenter.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/admin',
    name: 'Admin',
    component: Admin,
    meta: { 
      requiresAuth: true,
      requiresAdmin: true 
    }
  },
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: () => import('../views/NotFound.vue')
  }
]

const router = createRouter({
  history: createWebHistory(process.env.BASE_URL),
  routes
})

// 路由守卫
router.beforeEach((to, from, next) => {
  // 检查并修复用户状态
  store.dispatch('checkAndRepairUserState')
  
  // 获取目标路由是否需要认证
  const requiresAuth = to.matched.some(record => record.meta.requiresAuth)
  
  // 检查用户是否已认证
  const isAuthenticated = store.getters.isAuthenticated
  
  if (requiresAuth && !isAuthenticated) {
    // 如果需要认证但用户未登录，重定向到登录页
    next({
      path: '/login',
      query: { redirect: to.fullPath }
    })
  } else if (to.path === '/login' && isAuthenticated) {
    // 如果用户已登录但试图访问登录页，重定向到首页
    next({ path: '/' })
  } else {
    next()
  }
})

export default router 