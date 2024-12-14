<template>
  <div class="home-page">
    <div class="header">
      <div class="logo">
        <span>💻 博客系统</span>
      </div>
      <div class="search">
        <el-input
          v-model="searchText"
          placeholder="搜索文章..."
          prefix-icon="el-icon-search"
        />
      </div>
      <div class="actions">
        <el-button type="primary" @click="$router.push('/create')" v-if="isLoggedIn">+ 发布文章</el-button>
        <template v-if="!isLoggedIn">
          <el-button type="primary" @click="$router.push('/login')">登录</el-button>
          <el-button @click="$router.push('/register')">注册</el-button>
        </template>
        <el-dropdown v-else @command="handleCommand" trigger="hover">
          <div class="user-avatar-container">
            <el-avatar :size="32" :src="userInfo.avatar || '/default-avatar.png'" />
            <span class="username">{{ userInfo.username }}</span>
          </div>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="userCenter">
                <el-icon><User /></el-icon>用户中心
              </el-dropdown-item>
              <el-dropdown-item command="logout" divided>
                <el-icon><SwitchButton /></el-icon>退出登录
              </el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
    </div>

    <div class="main-content">
      <div class="content-wrapper">
        <!-- 左侧内容区域 -->
        <div class="left-content">
          <!-- 筛选区域 -->
          <div class="filter-section">
            <el-select v-model="selectedCategory" placeholder="选择分类" clearable @change="handleFilterChange">
              <el-option
                v-for="category in categories"
                :key="category.id"
                :label="category.name"
                :value="category.id"
              />
            </el-select>
            <el-select v-model="selectedTag" placeholder="选择标签" clearable @change="handleFilterChange">
              <el-option
                v-for="tag in tags"
                :key="tag.id"
                :label="tag.name"
                :value="tag.id"
              />
            </el-select>
          </div>

          <div class="tabs">
            <el-tabs v-model="activeTab">
              <el-tab-pane label="全部文章" name="all">
              </el-tab-pane>
              <el-tab-pane label="技术" name="tech"></el-tab-pane>
              <el-tab-pane label="生活" name="life"></el-tab-pane>
            </el-tabs>
          </div>

          <div class="view-toggle">
            <el-radio-group v-model="viewMode">
              <el-radio-button label="card">卡片视图</el-radio-button>
              <el-radio-button label="list">列表视图</el-radio-button>
            </el-radio-group>
          </div>

          <div class="articles" :class="viewMode">
            <!-- 文章列表或卡片将在这里渲染 -->
            <div class="empty-state" v-if="!articles || articles.length === 0">
              <el-empty description="暂无文章">
                <el-button type="primary" @click="$router.push('/create')">写第一篇文章</el-button>
              </el-empty>
            </div>
            <template v-else>
              <!-- 卡片视图 -->
              <template v-if="viewMode === 'card'">
                <el-card 
                  v-for="article in articles" 
                  :key="article.id" 
                  class="article-card" 
                  shadow="hover"
                  @click="viewArticle(article.id, '/')"
                >
                  <div class="article-header">
                    <h3 class="article-title">
                      {{ article.title }}
                      <el-tag v-if="article.isPrivate" type="warning" size="small" class="ml-2">私密</el-tag>
                    </h3>
                    <div class="article-meta">
                      <span class="author">作者: {{ article.author?.username || '未知' }}</span>
                      <span class="category">
                        <el-tag size="small" type="success">{{ article.category || '默认分类' }}</el-tag>
                      </span>
                      <span class="date">{{ formatDate(article.createdAt) }}</span>
                    </div>
                  </div>
                  <div class="article-preview">{{ article.content?.substring(0, 200) }}...</div>
                  <div class="article-footer">
                    <div class="article-tags">
                      <el-tag 
                        v-for="tag in getArticleTags(article)" 
                        :key="tag" 
                        size="small" 
                        type="info"
                        class="article-tag"
                      >
                        {{ tag }}
                      </el-tag>
                      <span v-if="!getArticleTags(article).length" class="no-tags">暂无标签</span>
                    </div>
                  </div>
                </el-card>
              </template>
              <!-- 列表视图 -->
              <template v-else>
                <el-table :data="articles" style="width: 100%">
                  <el-table-column prop="title" label="标题" min-width="200">
                    <template #default="scope">
                      <span class="article-link" @click="viewArticle(scope.row.id)">
                        {{ scope.row.title }}
                        <el-tag v-if="scope.row.isPrivate" type="warning" size="small" class="ml-2">私密</el-tag>
                      </span>
                    </template>
                  </el-table-column>
                  <el-table-column label="作者" width="120">
                    <template #default="scope">
                      {{ scope.row.author?.username || '未知' }}
                    </template>
                  </el-table-column>
                  <el-table-column prop="category" label="分类" width="120">
                    <template #default="scope">
                      <el-tag size="small" type="success">{{ scope.row.category || '默认分类' }}</el-tag>
                    </template>
                  </el-table-column>
                  <el-table-column label="标签" min-width="150">
                    <template #default="scope">
                      <el-tag
                        v-for="tag in getArticleTags(scope.row)"
                        :key="tag"
                        size="small"
                        class="mx-1"
                        type="info"
                      >
                        {{ tag }}
                      </el-tag>
                      <span v-if="!getArticleTags(scope.row).length" class="no-tags">暂无标签</span>
                    </template>
                  </el-table-column>
                  <el-table-column label="发布时间" width="180">
                    <template #default="scope">
                      {{ formatDate(scope.row.createdAt) }}
                    </template>
                  </el-table-column>
                </el-table>
              </template>
            </template>
          </div>

          <!-- 分页组件 -->
          <div class="pagination-container">
            <el-pagination
              v-model:current-page="currentPage"
              v-model:page-size="pageSize"
              :page-sizes="[10, 20, 30, 50]"
              :total="total"
              layout="total, sizes, prev, pager, next, jumper"
              @size-change="handleSizeChange"
              @current-change="handlePageChange"
            />
          </div>
        </div>

        <!-- 右侧信息区域 -->
        <div class="right-sidebar">
          <!-- 分类统计卡片 -->
          <el-card class="category-card">
            <template #header>
              <div class="card-header">
                <span>📚 分类</span>
              </div>
            </template>
            <div class="category-list">
              <el-tag
                v-for="category in categories"
                :key="category.id"
                :type="selectedCategory === category.id ? 'primary' : ''"
                class="category-tag"
                @click="handleCategoryClick(category.id)"
              >
                {{ category.name }}
              </el-tag>
            </div>
          </el-card>

          <!-- 标签云卡片 -->
          <el-card class="tags-card">
            <template #header>
              <div class="card-header">
                <span>🏷️ 标签云</span>
              </div>
            </template>
            <div class="tag-cloud">
              <el-tag
                v-for="tag in tags"
                :key="tag.id"
                :type="selectedTag === tag.id ? 'primary' : ''"
                class="tag-item"
                @click="handleTagClick(tag.id)"
              >
                {{ tag.name }}
              </el-tag>
            </div>
          </el-card>

          <!-- 欢迎卡片 -->
          <el-card class="welcome-card">
            <template #header>
              <div class="welcome-header">
                <span>👋 欢迎来到博客</span>
              </div>
            </template>
            <div class="welcome-content">
              <p>在这里，你可以分享你的知识和想法</p>
              <el-button type="primary" @click="$router.push('/create')">开始创作</el-button>
            </div>
          </el-card>

          <!-- 统计信息卡片 -->
          <el-card class="stats-card">
            <template #header>
              <div class="stats-header">
                <span>📊 统计信息</span>
              </div>
            </template>
            <div class="stats-content">
              <div class="stat-item">
                <span class="label">文章数：</span>
                <span class="value">{{ stats.articleCount || 0 }}</span>
              </div>
            </div>
          </el-card>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, onMounted, watch, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getArticles, getCategories, getTags } from '@/api/article'
import { User, SwitchButton } from '@element-plus/icons-vue'
import { useStore } from 'vuex'

export default {
  name: 'HomePage',
  components: {
    User,
    SwitchButton
  },
  setup() {
    const router = useRouter()
    const store = useStore()
    const searchText = ref('')
    const activeTab = ref('all')
    const viewMode = ref('card')
    const articles = ref([])
    const loading = ref(false)
    const stats = ref({
      articleCount: 0
    })

    // 添加分页相关的变量
    const currentPage = ref(1)
    const pageSize = ref(10)
    const total = ref(0)

    // 计算属性：是否已登录
    const isLoggedIn = computed(() => store.state.token)
    // 计算属性：用户信息
    const userInfo = computed(() => store.state.user || {})

    // 分类和标签数据
    const categories = ref([])
    const tags = ref([])
    const selectedCategory = ref('')
    const selectedTag = ref('')

    const handleCommand = (command) => {
      switch (command) {
        case 'userCenter':
          router.push('/user')
          break
        case 'logout':
          handleLogout()
          break
      }
    }

    const handleLogout = () => {
      store.dispatch('logout')
      router.push('/login')
      ElMessage.success('已退出登录')
    }

    const loadArticles = async (params = {}) => {
      if (loading.value) return
      
      try {
        loading.value = true
        const type = activeTab.value === 'all' ? '' : activeTab.value
        const requestParams = {
          page: currentPage.value,
          size: pageSize.value,
          type,
          category: selectedCategory.value,
          tag: selectedTag.value,
          ...params
        }
        
        const response = await getArticles(requestParams)
        
        if (response && response.content) {
          articles.value = response.content
          total.value = response.totalElements || 0
          stats.value.articleCount = response.totalElements || 0
        } else {
          articles.value = []
          total.value = 0
          stats.value.articleCount = 0
        }
      } catch (error) {
        console.error('加载文章失败:', error)
        ElMessage.error('加载文章失败')
        articles.value = []
        total.value = 0
        stats.value.articleCount = 0
      } finally {
        loading.value = false
      }
    }

    const viewArticle = (id, from = '/') => {
      if (loading.value) return
      router.push({
        path: `/article/${id}`,
        query: { from }
      })
    }

    const formatDate = (date) => {
      if (!date) return ''
      return new Date(date).toLocaleDateString('zh-CN', {
        year: 'numeric',
        month: 'long',
        day: 'numeric'
      })
    }

    const getArticleTags = (article) => {
      if (!article?.tags) return []
      return article.tags.split(',').filter(tag => tag.trim() !== '')
    }

    // 监听 activeTab 变化
    watch(activeTab, () => {
      loadArticles()
    })

    // 获取分类和标签数据
    const fetchCategories = async () => {
      try {
        const response = await getCategories()
        categories.value = response
      } catch (error) {
        console.error('获取分类失败:', error)
      }
    }

    const fetchTags = async () => {
      try {
        const response = await getTags()
        tags.value = response
      } catch (error) {
        console.error('获取标签失败:', error)
      }
    }

    // 处理筛选变化
    const handleFilterChange = async () => {
      try {
        currentPage.value = 1 // 重置页码
        await loadArticles()
      } catch (error) {
        ElMessage.error('筛选文章失败')
      }
    }

    // 处理分页变化
    const handlePageChange = (page) => {
      currentPage.value = page
      loadArticles()
    }

    // 处理每页数量变化
    const handleSizeChange = (size) => {
      pageSize.value = size
      currentPage.value = 1
      loadArticles()
    }

    // 点击分类标签
    const handleCategoryClick = (categoryId) => {
      selectedCategory.value = categoryId === selectedCategory.value ? '' : categoryId
      handleFilterChange()
    }

    // 点击标签
    const handleTagClick = (tagId) => {
      selectedTag.value = tagId === selectedTag.value ? '' : tagId
      handleFilterChange()
    }

    onMounted(async () => {
      await Promise.all([
        fetchCategories(),
        fetchTags(),
        loadArticles()
      ])
    })

    return {
      searchText,
      activeTab,
      viewMode,
      articles,
      stats,
      loading,
      isLoggedIn,
      userInfo,
      currentPage,
      pageSize,
      total,
      handleCommand,
      handleLogout,
      viewArticle,
      formatDate,
      getArticleTags,
      categories,
      tags,
      selectedCategory,
      selectedTag,
      handleFilterChange,
      handleCategoryClick,
      handleTagClick,
      handlePageChange,
      handleSizeChange
    }
  }
}
</script>

<style scoped>
.home-page {
  min-height: 100vh;
  background-color: #f5f7fa;
}

.header {
  background-color: #fff;
  padding: 1rem 2rem;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.logo {
  font-size: 1.5rem;
  font-weight: bold;
  color: #409eff;
}

.search {
  flex: 1;
  max-width: 500px;
  margin: 0 2rem;
}

.actions {
  display: flex;
  gap: 1rem;
}

.main-content {
  padding: 2rem;
  max-width: 1400px;
  margin: 0 auto;
}

.content-wrapper {
  display: flex;
  gap: 2rem;
}

.left-content {
  flex: 1;
  min-width: 0;
}

.right-sidebar {
  width: 300px;
  flex-shrink: 0;
}

.welcome-card,
.stats-card {
  margin-bottom: 1rem;
}

.article-card {
  margin-bottom: 1rem;
  cursor: pointer;
  transition: all 0.3s ease;
}

.article-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.article-header {
  margin-bottom: 1rem;
}

.article-title {
  margin: 0;
  color: #303133;
}

.article-meta {
  margin-top: 0.5rem;
  font-size: 0.9rem;
  color: #909399;
  display: flex;
  gap: 1rem;
  align-items: center;
  flex-wrap: wrap;
}

.article-preview {
  color: #606266;
  margin-bottom: 1rem;
}

.article-footer {
  margin-top: 1rem;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.article-tags {
  display: flex;
  gap: 0.5rem;
  flex-wrap: wrap;
}

.article-tag {
  margin: 0.25rem;
  transition: all 0.3s ease;
}

.article-tag:hover {
  transform: scale(1.05);
}

.article-link {
  cursor: pointer;
  color: #303133;
  text-decoration: none;
}

.article-link:hover {
  color: #409eff;
}

.empty-state {
  padding: 2rem;
  text-align: center;
}

.stat-item {
  display: flex;
  justify-content: space-between;
  padding: 0.5rem 0;
}

.view-toggle {
  margin-bottom: 1rem;
  display: flex;
  justify-content: flex-end;
}

.user-avatar-container {
  display: flex;
  align-items: center;
  cursor: pointer;
  padding: 0 8px;
}

.user-avatar-container:hover {
  background-color: #f5f7fa;
  border-radius: 4px;
}

.username {
  margin-left: 8px;
  font-size: 14px;
  color: #606266;
}

.el-dropdown-menu {
  padding: 5px 0;
}

.el-dropdown-menu .el-icon {
  margin-right: 8px;
}

.filter-section {
  margin-bottom: 20px;
  display: flex;
  gap: 10px;
}

.category-card,
.tags-card {
  margin-bottom: 20px;
}

.category-list,
.tag-cloud {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.category-tag,
.tag-item {
  cursor: pointer;
  margin: 4px;
  transition: all 0.3s;
}

.category-tag:hover,
.tag-item:hover {
  transform: scale(1.05);
}

.card-header {
  display: flex;
  align-items: center;
  font-weight: bold;
}

.card-header span {
  margin-left: 8px;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}

:deep(.el-pagination) {
  justify-content: center;
  padding: 1rem;
  background-color: white;
  border-radius: 4px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

:deep(.el-pagination .el-pagination__total) {
  margin-right: 16px;
}

:deep(.el-pagination .el-pagination__sizes) {
  margin-right: 16px;
}

.mx-1 {
  margin: 0 0.25rem;
}

.no-tags {
  color: #909399;
  font-size: 0.9rem;
  font-style: italic;
}

.el-button-group {
  display: flex;
  gap: 4px;
}

:deep(.el-table) {
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

:deep(.el-table__header) {
  background-color: #f5f7fa;
}

:deep(.el-table__row) {
  transition: all 0.3s ease;
}

:deep(.el-table__row:hover) {
  background-color: #f5f7fa;
}

:deep(.el-tag + .el-tag) {
  margin-left: 4px;
}
</style> 