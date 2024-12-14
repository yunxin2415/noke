<template>
  <div class="user-center">
    <el-menu mode="horizontal" class="user-menu">
      <el-menu-item index="home" @click="$router.push('/')">
        <el-icon><HomeFilled /></el-icon>
        返回首页
      </el-menu-item>
      <el-menu-item index="profile" @click="activeTab = 'profile'">
        <el-icon><User /></el-icon>
        个人资料
      </el-menu-item>
      <el-menu-item index="articles" @click="activeTab = 'articles'">
        <el-icon><Document /></el-icon>
        我的文章
      </el-menu-item>
      <el-menu-item index="security" @click="activeTab = 'security'">
        <el-icon><Lock /></el-icon>
        安全设置
      </el-menu-item>
    </el-menu>

    <div class="content-area">
      <!-- 个人资料 -->
      <div v-if="activeTab === 'profile'" class="profile-section">
        <h2>个人资料</h2>
        <el-form :model="userProfile" label-width="100px">
          <el-form-item label="用户名">
            <el-input v-model="userProfile.username" disabled />
          </el-form-item>
          <el-form-item label="邮箱">
            <el-input v-model="userProfile.email" />
          </el-form-item>
          <el-form-item label="个人简介">
            <el-input v-model="userProfile.bio" type="textarea" />
          </el-form-item>
          <el-form-item label="头像">
            <el-upload
              class="avatar-uploader"
              :action="uploadUrl"
              :headers="uploadHeaders"
              :show-file-list="false"
              :on-success="handleAvatarSuccess"
              :on-error="handleAvatarError"
              :before-upload="beforeAvatarUpload"
            >
              <img v-if="userProfile.avatar" :src="userProfile.avatar" class="avatar" />
              <el-icon v-else class="avatar-uploader-icon"><Plus /></el-icon>
            </el-upload>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="updateProfile">保存修改</el-button>
          </el-form-item>
        </el-form>
      </div>

      <!-- 我的文章 -->
      <div v-if="activeTab === 'articles'" class="articles-section">
        <div class="articles-header">
          <div class="header-left">
            <h2>我的文章</h2>
            <el-select
              v-model="selectedCategory"
              placeholder="全部"
              clearable
              @change="handleCategoryChange"
              class="category-filter"
            >
              <el-option label="全部" value="" />
              <el-option label="技术" value="技术" />
              <el-option label="生活" value="生活" />
              <el-option label="随笔" value="随笔" />
              <el-option label="默认分类" value="默认分类" />
            </el-select>
          </div>
          <el-button type="primary" @click="$router.push('/create')">写新文章</el-button>
        </div>
        <el-table v-loading="loading" :data="filteredArticles" style="width: 100%">
          <el-table-column prop="title" label="标题">
            <template #default="{ row }">
              <router-link :to="'/article/' + row.id" class="article-title">
                {{ row.title }}
              </router-link>
            </template>
          </el-table-column>
          <el-table-column prop="category" label="分类" width="120">
            <template #default="{ row }">
              <el-tag size="small" type="success">{{ row.category || '默认分类' }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="标签" min-width="150">
            <template #default="{ row }">
              <el-tag
                v-for="tag in getArticleTags(row)"
                :key="tag"
                size="small"
                class="mx-1"
                type="info"
              >
                {{ tag }}
              </el-tag>
              <span v-if="!getArticleTags(row).length" class="no-tags">暂无标签</span>
            </template>
          </el-table-column>
          <el-table-column prop="createdAt" label="发布时间" width="180">
            <template #default="{ row }">
              {{ formatDate(row.createdAt) }}
            </template>
          </el-table-column>
          <el-table-column label="状态" width="120">
            <template #default="{ row }">
              <el-tag :type="row.isPrivate ? 'warning' : 'success'">
                {{ row.isPrivate ? '私密' : '公开' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="下载" width="120">
            <template #default="{ row }">
              <el-tag :type="row.isDownloadable ? 'success' : 'info'">
                {{ row.isDownloadable ? '可下载' : '不可下载' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="200">
            <template #default="{ row }">
              <el-button-group>
                <el-button size="small" @click="handleEdit(row)">编辑</el-button>
                <el-button size="small" type="danger" @click="handleDelete(row)">删除</el-button>
              </el-button-group>
            </template>
          </el-table-column>
        </el-table>

        <!-- 编辑文章对话框 -->
        <el-dialog
          v-model="editDialogVisible"
          title="编辑文章"
          width="500px"
          :close-on-click-modal="false"
        >
          <el-form :model="editForm" label-width="100px">
            <el-form-item label="分类">
              <el-select v-model="editForm.category" placeholder="选择分类" class="w-full">
                <el-option label="技术" value="技术" />
                <el-option label="生活" value="生活" />
                <el-option label="随笔" value="随笔" />
                <el-option label="默认分类" value="默认分类" />
              </el-select>
            </el-form-item>
            <el-form-item label="标签">
              <div class="tag-input-container">
                <div class="tag-list">
                  <el-tag
                    v-for="tag in editForm.tagList"
                    :key="tag"
                    closable
                    size="small"
                    @close="removeTag(tag)"
                  >
                    {{ tag }}
                  </el-tag>
                </div>
                <el-input
                  v-model="tagInputValue"
                  placeholder="输入标签内容，按空格或回车创建标签"
                  @keyup.space="handleTagCreate"
                  @keyup.enter="handleTagCreate"
                  class="tag-input"
                />
              </div>
            </el-form-item>
            <el-form-item label="可见性">
              <el-switch
                v-model="editForm.isPrivate"
                :active-value="true"
                :inactive-value="false"
                active-text="私密"
                inactive-text="公开"
              />
            </el-form-item>
            <el-form-item label="下载权限">
              <el-switch
                v-model="editForm.isDownloadable"
                :active-value="true"
                :inactive-value="false"
                active-text="允许下载"
                inactive-text="禁止下载"
              />
            </el-form-item>
          </el-form>
          <template #footer>
            <span class="dialog-footer">
              <el-button @click="editDialogVisible = false">取消</el-button>
              <el-button type="primary" @click="handleSaveEdit" :loading="saveLoading">
                保存
              </el-button>
            </span>
          </template>
        </el-dialog>
      </div>

      <!-- 安全设置 -->
      <div v-if="activeTab === 'security'" class="security-section">
        <h2>安全设置</h2>
        <el-divider content-position="left">修改密码</el-divider>
        <el-form :model="passwordForm" label-width="100px">
          <el-form-item label="当前密码" prop="currentPassword">
            <el-input v-model="passwordForm.currentPassword" type="password" show-password />
          </el-form-item>
          <el-form-item label="新密码" prop="newPassword">
            <el-input v-model="passwordForm.newPassword" type="password" show-password />
          </el-form-item>
          <el-form-item label="确认新密码" prop="confirmPassword">
            <el-input v-model="passwordForm.confirmPassword" type="password" show-password />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handlePasswordChange">修改密码</el-button>
          </el-form-item>
        </el-form>

        <el-divider content-position="left">注销账户</el-divider>
        <div class="delete-account-section">
          <p class="warning-text">注意：账户注销后将无法恢复，您的所有数据都将被永久删除。</p>
          <el-button type="danger" @click="showDeleteAccountDialog">注销账户</el-button>
        </div>

        <!-- 注销账户确认对话框 -->
        <el-dialog
          v-model="deleteAccountDialogVisible"
          title="注销账户确认"
          width="400px"
          :close-on-click-modal="false"
        >
          <div class="delete-account-dialog">
            <p class="warning-text">请输入密码确认注销账户：</p>
            <el-input
              v-model="deleteAccountPassword"
              type="password"
              show-password
              placeholder="请输入当前密码"
            />
            <p class="warning-text">
              注意：此操作将永久删除您的账户和所有相关数据，且无法恢复！
            </p>
          </div>
          <template #footer>
            <span class="dialog-footer">
              <el-button @click="deleteAccountDialogVisible = false">取消</el-button>
              <el-button
                type="danger"
                @click="handleDeleteAccount"
                :loading="deleteAccountLoading"
              >
                确认注销
              </el-button>
            </span>
          </template>
        </el-dialog>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { User, Document, Lock, Plus, HomeFilled } from '@element-plus/icons-vue'
import { getUserProfile, updateUserProfile, changePassword, deleteAccount } from '@/api/user'
import { deleteArticle as deleteArticleApi, updateArticle } from '@/api/article'
import { useStore } from 'vuex'
import { getUserArticles } from '@/api/article'

export default {
  name: 'UserCenter',
  components: {
    User,
    Document,
    Lock,
    Plus,
    HomeFilled
  },
  setup() {
    const store = useStore()
    const router = useRouter()
    const activeTab = ref('profile')
    const loading = ref(false)
    const articles = ref([])
    const userProfile = ref({
      username: '',
      email: '',
      bio: '',
      avatar: ''
    })
    const passwordForm = ref({
      currentPassword: '',
      newPassword: '',
      confirmPassword: ''
    })
    const selectedCategory = ref('')

    const uploadUrl = `${process.env.VUE_APP_API_BASE || '/api'}/upload/avatar`
    const uploadHeaders = computed(() => ({
      Authorization: `Bearer ${store.state.token}`
    }))

    // 加载用户资料
    const loadUserProfile = async () => {
      try {
        if (!store.state.token) {
          ElMessage.error('请先登录')
          router.push('/login')
          return
        }
        const response = await getUserProfile()
        if (response && response.data) {
          userProfile.value = response.data
        } else {
          userProfile.value = response
        }
      } catch (error) {
        console.error('获取用户资料失败:', error)
        if (error.response?.status === 401) {
          ElMessage.error('请先登录')
          router.push('/login')
        } else {
          ElMessage.error(error.response?.data?.message || '获取用户资料失败')
        }
      }
    }

    // 加载用户文章列表
    const loadArticles = async () => {
      try {
        if (!store.state.token) {
          ElMessage.error('请先登录')
          router.push('/login')
          return
        }
        
        const currentUser = store.state.user
        console.log('当前用户信息:', currentUser)
        
        loading.value = true
        articles.value = []
        
        console.log('开始加载用户文章列表')
        const response = await getUserArticles()
        console.log('获取到文章列表响应:', response)
        
        // 处理响应数据
        let articleData = null
        if (Array.isArray(response)) {
          console.log('响应是数组格式，文章数量:', response.length)
          articleData = response
        } else if (response && Array.isArray(response.data)) {
          console.log('响应包含data数组，文章数量:', response.data.length)
          articleData = response.data
        } else if (response && response.content && Array.isArray(response.content)) {
          console.log('响应包含content数组，文章数量:', response.content.length)
          articleData = response.content
        } else if (response && typeof response === 'object') {
          console.log('响应是对象格式:', response)
          // 如果响应是对象，尝试提取有效的文章数据
          const articles = []
          for (const key in response) {
            const item = response[key]
            if (item && typeof item === 'object' && item.id && item.title) {
              articles.push(item)
            }
          }
          if (articles.length > 0) {
            console.log('从对象中提取到文章数量:', articles.length)
            articleData = articles
          }
        }
        
        if (articleData) {
          articles.value = articleData
          console.log('最终处理后的文章列表:', articles.value)
          if (articles.value.length === 0) {
            ElMessage.info('暂无文章，快去创建你的第一篇文章！')
          }
        } else {
          console.warn('文章列表格式异常:', response)
          ElMessage.warning('获取文章列表格式异常')
          articles.value = []
        }
      } catch (error) {
        console.error('获取文章列表失败:', error)
        if (error.response?.status === 401) {
          store.dispatch('logout')
          router.push('/login')
        } else {
          ElMessage.error(error.response?.data?.message || '获取文章列表失败')
        }
        articles.value = []
      } finally {
        loading.value = false
      }
    }

    // 更新用户资料
    const updateProfile = async () => {
      try {
        const response = await updateUserProfile({
          email: userProfile.value.email,
          bio: userProfile.value.bio,
          avatar: userProfile.value.avatar
        })
        if (response && response.data) {
          userProfile.value = response.data
        }
        ElMessage.success('个人资料更新成功')
        // 更新store中的用户信息
        store.commit('setUser', userProfile.value)
      } catch (error) {
        console.error('更新个人资料失败:', error)
        ElMessage.error(error.response?.data?.message || '更新失败')
      }
    }

    // 修改密码
    const handlePasswordChange = async () => {
      if (passwordForm.value.newPassword !== passwordForm.value.confirmPassword) {
        ElMessage.error('两次输入的密码不一致')
        return
      }

      try {
        await changePassword({
          currentPassword: passwordForm.value.currentPassword,
          newPassword: passwordForm.value.newPassword,
          confirmPassword: passwordForm.value.confirmPassword
        })
        
        // 密码修改成功后，清空表单并示用户重新登录
        passwordForm.value = {
          currentPassword: '',
          newPassword: '',
          confirmPassword: ''
        }
        
        // 退出登录并跳转到登录页
        store.dispatch('logout')
        router.push('/login')
        ElMessage.success('密码修改成功，请使用新密码重新登录')
      } catch (error) {
        console.error('修改密码失败:', error)
        ElMessage.error(error.response?.data?.message || '修改失败')
      }
    }

    // 头上传相关方法
    const handleAvatarSuccess = (response) => {
      if (response && response.url) {
        userProfile.value.avatar = response.url
        // 立即更新用户资料
        updateProfile()
      } else {
        ElMessage.error('头像上传失败：响应格式错误')
      }
    }

    const handleAvatarError = (error) => {
      console.error('头像上传失败:', error)
      ElMessage.error('头像上传失败，请重试')
    }

    const beforeAvatarUpload = (file) => {
      const isImage = file.type.startsWith('image/')
      const isLt2M = file.size / 1024 / 1024 < 2

      if (!isImage) {
        ElMessage.error('上传头像图片只能是图片格式!')
        return false
      }
      if (!isLt2M) {
        ElMessage.error('上传头像图片大小不能超过 2MB!')
        return false
      }
      return true
    }

    // 格式化标签
    const getArticleTags = (article) => {
      if (!article?.tags) return []
      return article.tags.split(',').filter(tag => tag.trim() !== '')
    }

    // 格式化日期
    const formatDate = (date) => {
      if (!date) return ''
      return new Date(date).toLocaleDateString('zh-CN', {
        year: 'numeric',
        month: 'long',
        day: 'numeric',
        hour: '2-digit',
        minute: '2-digit'
      })
    }

    // 编辑文章的状态
    const editDialogVisible = ref(false)
    const editForm = ref({
      id: null,
      title: '',
      category: '',
      tagList: [], // 使用数组存储标签
      isPrivate: false,
      isDownloadable: true
    })
    const tagInputValue = ref('') // 标签输入值
    const saveLoading = ref(false)

    // 处理标签创建
    const handleTagCreate = (event) => {
      const value = tagInputValue.value.trim()
      if (!value) return
      
      // 如果输入的内容以#开头，去掉#号并添加为标签
      const tag = value.startsWith('#') ? value.substring(1) : value
      
      if (tag && !editForm.value.tagList.includes(tag)) {
        editForm.value.tagList.push(tag)
      }
      // 清空输入框
      tagInputValue.value = ''
      
      // 阻止空格键的默认行为
      if (event.key === ' ') {
        event.preventDefault()
      }
    }

    // 移除标签
    const removeTag = (tag) => {
      const index = editForm.value.tagList.indexOf(tag)
      if (index > -1) {
        editForm.value.tagList.splice(index, 1)
      }
    }

    // 打开编辑对话框
    const handleEdit = (article) => {
      editForm.value = {
        id: article.id,
        title: article.title,
        category: article.category || '默认分类',
        tagList: article.tags ? article.tags.split(',').filter(tag => tag.trim() !== '') : [],
        isPrivate: article.isPrivate || false,
        isDownloadable: article.isDownloadable !== false
      }
      tagInputValue.value = ''
      editDialogVisible.value = true
    }

    // 保存编辑
    const handleSaveEdit = async () => {
      try {
        saveLoading.value = true
        const updateData = {
          id: editForm.value.id,
          category: editForm.value.category || '默认分类',
          tags: editForm.value.tagList.join(','),
          isPrivate: editForm.value.isPrivate,
          isDownloadable: editForm.value.isDownloadable
        }
        
        await updateArticle(updateData)
        ElMessage.success('文章更新成功')
        editDialogVisible.value = false
        // 重新加载文章列表
        await loadArticles()
      } catch (error) {
        console.error('更新文章失败:', error)
        ElMessage.error(error.response?.data?.message || '更新失败，请确保您有权限执行此操作')
      } finally {
        saveLoading.value = false
      }
    }

    // 删除文章
    const handleDelete = async (article) => {
      try {
        await ElMessageBox.confirm(
          '确定要删除这篇文章吗？删除后将无法恢复。',
          '删除确认',
          {
            confirmButtonText: '确定删除',
            cancelButtonText: '取消',
            type: 'warning',
          }
        )
        
        await deleteArticleApi(article.id)
        ElMessage.success('文章删除成功')
        // 重新加载文章列表
        await loadArticles()
      } catch (error) {
        if (error !== 'cancel') {
          console.error('删除文章失败:', error)
          ElMessage.error(error.response?.data?.message || '删除文章失败')
        }
      }
    }

    // 根据分类筛选文章
    const filteredArticles = computed(() => {
      if (!selectedCategory.value) {
        return articles.value
      }
      return articles.value.filter(article => article.category === selectedCategory.value)
    })

    // 处理分类变化
    const handleCategoryChange = () => {
      if (filteredArticles.value.length === 0) {
        ElMessage.info('该分类下暂无文章')
      }
    }

    // 注销账户相关
    const deleteAccountDialogVisible = ref(false)
    const deleteAccountPassword = ref('')
    const deleteAccountLoading = ref(false)

    // 显示注销账户对话框
    const showDeleteAccountDialog = () => {
      deleteAccountPassword.value = ''
      deleteAccountDialogVisible.value = true
    }

    // 处理注销账户
    const handleDeleteAccount = async () => {
      if (!deleteAccountPassword.value) {
        ElMessage.error('请输入密码')
        return
      }

      try {
        await ElMessageBox.confirm(
          '确定要注销账户吗？此操作将永久删除您的所有数据且无法恢复！',
          '最终确认',
          {
            confirmButtonText: '确认注销',
            cancelButtonText: '取消',
            type: 'danger'
          }
        )

        deleteAccountLoading.value = true
        await deleteAccount(deleteAccountPassword.value)
        ElMessage.success('账户已注销')
        store.dispatch('logout')
        router.push('/')
      } catch (error) {
        if (error !== 'cancel') {
          console.error('注销账户失败:', error)
          ElMessage.error(error.response?.data?.message || '注销账户失败')
        }
      } finally {
        deleteAccountLoading.value = false
        deleteAccountDialogVisible.value = false
      }
    }

    onMounted(async () => {
      if (!store.state.token) {
        ElMessage.error('请先登录')
        router.push('/login')
        return
      }
      await loadUserProfile()
      await loadArticles()
    })

    return {
      activeTab,
      loading,
      articles,
      userProfile,
      passwordForm,
      uploadUrl,
      uploadHeaders,
      updateProfile,
      handlePasswordChange,
      handleAvatarSuccess,
      handleAvatarError,
      beforeAvatarUpload,
      getArticleTags,
      formatDate,
      editDialogVisible,
      editForm,
      saveLoading,
      handleEdit,
      handleSaveEdit,
      tagInputValue,
      handleTagCreate,
      removeTag,
      handleDelete,
      selectedCategory,
      filteredArticles,
      handleCategoryChange,
      deleteAccountDialogVisible,
      deleteAccountPassword,
      deleteAccountLoading,
      showDeleteAccountDialog,
      handleDeleteAccount
    }
  }
}
</script>

<style scoped>
.user-center {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

.user-menu {
  margin-bottom: 20px;
}

.content-area {
  background: #fff;
  padding: 20px;
  border-radius: 4px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.articles-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 20px;
}

.category-filter {
  width: 150px;
}

.article-title {
  color: #409EFF;
  text-decoration: none;
}

.article-title:hover {
  text-decoration: underline;
}

.avatar-uploader {
  text-align: center;
}

.avatar {
  width: 100px;
  height: 100px;
  border-radius: 50%;
  object-fit: cover;
}

.avatar-uploader-icon {
  font-size: 28px;
  color: #8c939d;
  width: 100px;
  height: 100px;
  line-height: 100px;
  text-align: center;
  border: 1px dashed #d9d9d9;
  border-radius: 50%;
}

.avatar-uploader-icon:hover {
  border-color: #409EFF;
}

h2 {
  margin-top: 0;
  margin-bottom: 20px;
  color: #303133;
}

.mx-1 {
  margin: 0 0.25rem;
}

.no-tags {
  color: #909399;
  font-size: 0.9rem;
  font-style: italic;
}

:deep(.el-tag) {
  margin: 2px;
}

:deep(.el-table .cell) {
  white-space: normal;
  line-height: 1.5;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

:deep(.el-switch) {
  margin-right: 8px;
}

.w-full {
  width: 100%;
}

:deep(.el-form-item__content) {
  flex-wrap: nowrap;
}

.tag-input-container {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.tag-list {
  display: flex;
  flex-wrap: wrap;
  gap: 4px;
  min-height: 32px;
  padding: 4px;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
}

.tag-list:empty {
  padding: 0;
  border: none;
}

.tag-input {
  margin-top: 4px;
}

:deep(.el-tag) {
  margin: 2px;
  transition: all 0.3s;
}

:deep(.el-tag:hover) {
  transform: translateY(-1px);
}

.delete-account-section {
  margin-top: 20px;
  padding: 20px;
  background-color: #fff5f5;
  border-radius: 4px;
  border: 1px solid #fde2e2;
}

.warning-text {
  color: #f56c6c;
  margin-bottom: 15px;
  line-height: 1.5;
}

.delete-account-dialog {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

:deep(.el-dialog__body) {
  padding-top: 20px;
}
</style> 