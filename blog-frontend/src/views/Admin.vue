<template>
  <div class="admin-container">
    <el-container>
      <el-header class="admin-header">
        <h2>管理后台</h2>
        <el-button @click="$router.push('/')" type="primary" plain>
          <el-icon><Back /></el-icon>返回首页
        </el-button>
      </el-header>
      
      <el-main>
        <el-tabs v-model="activeTab" class="admin-tabs">
          <el-tab-pane label="用户管理" name="users">
            <el-table :data="users" style="width: 100%">
              <el-table-column prop="id" label="ID" width="80" />
              <el-table-column prop="username" label="用户名" width="120" />
              <el-table-column prop="email" label="邮箱" width="180" />
              <el-table-column prop="role" label="角色" width="100" />
              <el-table-column prop="createdAt" label="创建时间" width="180">
                <template #default="scope">
                  {{ formatDate(scope.row.createdAt) }}
                </template>
              </el-table-column>
              <el-table-column label="操作">
                <template #default="scope">
                  <el-button-group>
                    <el-button size="small" @click="editUser(scope.row)">
                      编辑
                    </el-button>
                    <el-button 
                      size="small" 
                      type="danger" 
                      @click="deleteUser(scope.row)"
                      :disabled="scope.row.role === 'ROLE_ADMIN'"
                    >
                      删除
                    </el-button>
                  </el-button-group>
                </template>
              </el-table-column>
            </el-table>
          </el-tab-pane>
          
          <el-tab-pane label="文章管理" name="articles">
            <el-table :data="articles" style="width: 100%">
              <el-table-column prop="id" label="ID" width="80" />
              <el-table-column prop="title" label="标题" width="200" />
              <el-table-column prop="author.username" label="作者" width="120" />
              <el-table-column prop="category" label="分类" width="100" />
              <el-table-column prop="createdAt" label="创建时间" width="180">
                <template #default="scope">
                  {{ formatDate(scope.row.createdAt) }}
                </template>
              </el-table-column>
              <el-table-column label="操作">
                <template #default="scope">
                  <el-button-group>
                    <el-button size="small" @click="viewArticle(scope.row)">
                      查看
                    </el-button>
                    <el-button size="small" type="danger" @click="deleteArticle(scope.row)">
                      删除
                    </el-button>
                  </el-button-group>
                </template>
              </el-table-column>
            </el-table>
          </el-tab-pane>
        </el-tabs>
      </el-main>
    </el-container>
  </div>
</template>

<script>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessageBox, ElMessage } from 'element-plus'
import { Back } from '@element-plus/icons-vue'
import { getUsers, deleteUser as deleteUserApi } from '@/api/admin'
import { getArticles, deleteArticle } from '@/api/article'
import { useStore } from 'vuex'

export default {
  name: 'AdminPage',
  components: {
    Back
  },
  setup() {
    const store = useStore()
    const router = useRouter()
    const activeTab = ref('users')
    const users = ref([])
    const articles = ref([])

    const loadUsers = async () => {
      try {
        const response = await getUsers()
        users.value = response.data
      } catch (error) {
        ElMessage.error('获取用户列表失败')
        console.error(error)
      }
    }

    const loadArticles = async () => {
      try {
        const response = await getArticles()
        articles.value = response.data
      } catch (error) {
        ElMessage.error('获取文章列表失败')
        console.error(error)
      }
    }

    const editUser = (user) => {
      // 实现用户编辑逻辑
      console.log('Edit user:', user)
    }

    const handleDeleteUser = async (user) => {
      try {
        await ElMessageBox.confirm(
          `确定要删除用户 ${user.username} 吗？`,
          '警告',
          {
            confirmButtonText: '确定',
            cancelButtonText: '取消',
            type: 'warning'
          }
        )
        
        await deleteUserApi(user.id)
        ElMessage.success('删除成功')
        await loadUsers()
      } catch (error) {
        if (error !== 'cancel') {
          ElMessage.error('删除失败')
          console.error(error)
        }
      }
    }

    const viewArticle = (article) => {
      router.push(`/article/${article.id}`)
    }

    const handleDeleteArticle = async (article) => {
      try {
        await ElMessageBox.confirm(
          `确定要删除文章 "${article.title}" 吗？`,
          '警告',
          {
            confirmButtonText: '确定',
            cancelButtonText: '取消',
            type: 'warning'
          }
        )
        
        await deleteArticle(article.id)
        ElMessage.success('删除成功')
        await loadArticles()
      } catch (error) {
        if (error !== 'cancel') {
          ElMessage.error('删除失败')
          console.error(error)
        }
      }
    }

    const formatDate = (date) => {
      return new Date(date).toLocaleString()
    }

    onMounted(() => {
      const user = store.state.user
      if (!user || user.role !== 'ROLE_ADMIN') {
        ElMessage.error('需要管理员权限')
        router.push('/')
        return
      }
      loadUsers()
      loadArticles()
    })

    return {
      activeTab,
      users,
      articles,
      editUser,
      deleteUser: handleDeleteUser,
      viewArticle,
      handleDeleteArticle,
      formatDate
    }
  }
}
</script>

<style scoped>
.admin-container {
  min-height: 100vh;
  background-color: #f5f7fa;
}

.admin-header {
  background-color: #fff;
  padding: 20px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.admin-tabs {
  background-color: #fff;
  padding: 20px;
  border-radius: 4px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.el-table {
  margin-top: 20px;
}
</style> 