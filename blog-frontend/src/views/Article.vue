<template>
  <div class="article-container">
    <el-container>
      <el-header>
        <el-menu mode="horizontal" router>
          <el-menu-item @click="goBack">
            <el-icon><ArrowLeft /></el-icon>
            返回上一页
          </el-menu-item>
        </el-menu>
      </el-header>
      
      <el-main>
        <el-card v-if="article" class="article-card">
          <template #header>
            <div class="article-header">
              <h1>{{ article.title }}</h1>
              <div class="article-meta">
                <span>作者: {{ article.author.username }}</span>
                <el-button
                  v-if="store.state.user && store.state.user.id !== article.author.id"
                  :type="isFollowing ? 'success' : 'primary'"
                  size="small"
                  :loading="isLoadingFollow"
                  @click="handleFollow(article.author.id)"
                >
                  {{ isFollowing ? '已关注' : '关注作者' }}
                </el-button>
                <span>发布时间: {{ formatDate(article.createdAt) }}</span>
                <span v-if="article.isPrivate" class="private-tag">
                  <el-tag type="warning" size="small">私密</el-tag>
                </span>
                <el-button 
                  v-if="article.isDownloadable"
                  type="primary"
                  size="small"
                  @click="downloadArticle"
                  :loading="downloading"
                >
                  下载文章
                </el-button>
                <el-tag v-else type="info" size="small">不可下载</el-tag>
              </div>
            </div>
          </template>
          <div class="article-content markdown-body" v-html="renderContent"></div>
        </el-card>
        <el-empty v-else description="文章加载中..." />
      </el-main>
    </el-container>
  </div>
</template>

<script>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowLeft } from '@element-plus/icons-vue'
import request from '@/utils/request'
import { followUser, unfollowUser, checkFollowing } from '@/api/user'
import { useStore } from 'vuex'
import MarkdownIt from 'markdown-it'
import hljs from 'highlight.js'
import 'highlight.js/styles/github.css'
import 'github-markdown-css/github-markdown.css'

// 创��markdown-it实例
const md = new MarkdownIt({
  html: true,
  linkify: true,
  typographer: true,
  highlight: function (str, lang) {
    if (lang && hljs.getLanguage(lang)) {
      try {
        return hljs.highlight(lang, str).value
      } catch (error) {
        console.error('代码高亮失败:', error)
      }
    }
    return '' // 如果语言不存在或发生错误，返回原始字符串
  }
})

export default {
  name: 'ArticlePage',
  components: {
    ArrowLeft
  },
  setup() {
    const route = useRoute()
    const router = useRouter()
    const store = useStore()
    const article = ref(null)
    const downloading = ref(false)
    const isFollowing = ref(false)
    const isLoadingFollow = ref(false)

    const renderContent = computed(() => {
      if (article.value && article.value.content) {
        return md.render(article.value.content)
      }
      return ''
    })

    const formatDate = (dateString) => {
      const date = new Date(dateString)
      return date.toLocaleString('zh-CN', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit',
        hour: '2-digit',
        minute: '2-digit'
      })
    }

    const goBack = () => {
      router.back()
    }

    // 检查是否已关注作者
    const checkFollowStatus = async (authorId) => {
      try {
        const result = await checkFollowing(authorId)
        isFollowing.value = result.following
      } catch (error) {
        console.error('检查关注状态失败:', error)
      }
    }

    // 处理关注/取消关注
    const handleFollow = async (authorId) => {
      if (!store.state.user) {
        ElMessage.warning('请先登录')
        router.push('/login')
        return
      }

      try {
        isLoadingFollow.value = true
        if (isFollowing.value) {
          await unfollowUser(authorId)
          isFollowing.value = false
        } else {
          await followUser(authorId)
          isFollowing.value = true
        }
      } catch (error) {
        console.error('操作失败:', error)
      } finally {
        isLoadingFollow.value = false
      }
    }

    const loadArticle = async () => {
      try {
        const response = await request({
          url: `/articles/${route.params.id}`,
          method: 'get'
        })
        
        if (response) {
          article.value = response
          // 检查关注状态
          if (store.state.user && article.value.author.id !== store.state.user.id) {
            await checkFollowStatus(article.value.author.id)
          }
        } else {
          ElMessage.error('文章不存在或已被删除')
        }
      } catch (error) {
        console.error('获取文章失败:', error)
        if (error.response?.status === 403) {
          ElMessage.error('该文章已设为私密，无权访问')
        } else {
          ElMessage.error('获取文章失败')
        }
      }
    }

    const downloadArticle = async () => {
      if (!article.value.isDownloadable) {
        ElMessage.warning('该文章不允许下载')
        return
      }

      try {
        downloading.value = true
        const response = await request({
          url: `/articles/${article.value.id}/download`,
          method: 'get',
          responseType: 'blob'
        })

        const blob = new Blob([response.data], { type: 'text/markdown' })
        const url = window.URL.createObjectURL(blob)
        const link = document.createElement('a')
        link.href = url
        link.download = `${article.value.title}.md`
        document.body.appendChild(link)
        link.click()
        document.body.removeChild(link)
        window.URL.revokeObjectURL(url)
        
        ElMessage.success('下载成功')
      } catch (error) {
        console.error('下载失败:', error)
        ElMessage.error('下载失败')
      } finally {
        downloading.value = false
      }
    }

    onMounted(() => {
      loadArticle()
    })

    return {
      article,
      renderContent,
      formatDate,
      goBack,
      store,
      isFollowing,
      isLoadingFollow,
      handleFollow,
      downloadArticle,
      downloading
    }
  }
}
</script>

<style scoped>
.article-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

.article-card {
  margin-top: 20px;
}

.article-header {
  padding: 20px 0;
}

.article-header h1 {
  margin: 0 0 20px 0;
  font-size: 2em;
  color: #303133;
}

.article-meta {
  color: #909399;
  font-size: 0.9em;
  display: flex;
  gap: 20px;
  align-items: center;
  flex-wrap: wrap;
}

.article-meta .el-button {
  margin: 0;
}

.article-content {
  padding: 20px 0;
  line-height: 1.6;
}

.private-tag {
  display: inline-flex;
  align-items: center;
}

.markdown-body {
  box-sizing: border-box;
  min-width: 200px;
  max-width: 980px;
  margin: 0 auto;
  padding: 45px;
}

@media (max-width: 767px) {
  .markdown-body {
    padding: 15px;
  }
}
</style> 