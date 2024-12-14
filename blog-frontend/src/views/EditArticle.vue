<template>
  <div class="edit-article">
    <el-container>
      <el-header>
        <el-menu mode="horizontal" router>
          <el-menu-item index="/">返回首页</el-menu-item>
        </el-menu>
      </el-header>
      
      <el-main>
        <el-card>
          <template #header>
            <div class="card-header">
              <span>编辑文章</span>
            </div>
          </template>
          
          <el-form v-if="article" :model="articleForm" :rules="rules" ref="articleFormRef" label-width="80px">
            <el-form-item label="标题" prop="title">
              <el-input v-model="articleForm.title" placeholder="请输入文章标题"></el-input>
            </el-form-item>
            
            <el-form-item label="内容" prop="content">
              <div class="editor-container">
                <el-input
                  v-model="articleForm.content"
                  type="textarea"
                  :autosize="{ minRows: 15, maxRows: 30 }"
                  placeholder="请输入文章内容"
                  resize="none"
                ></el-input>
              </div>
            </el-form-item>
            
            <el-form-item>
              <el-button type="primary" @click="handleSubmit">保存修改</el-button>
              <el-button @click="$router.push('/')">取消</el-button>
            </el-form-item>
          </el-form>
          
          <div v-else class="loading">
            <el-empty description="文章不存在或无权限编辑" v-if="loadError">
              <el-button type="primary" @click="$router.push('/')">返回首页</el-button>
            </el-empty>
            <el-skeleton :rows="10" animated v-else />
          </div>
        </el-card>
      </el-main>
    </el-container>
  </div>
</template>

<script>
import { ref, reactive, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import axios from 'axios'
import { useStore } from 'vuex'

export default {
  name: 'EditArticlePage',
  setup() {
    const router = useRouter()
    const route = useRoute()
    const store = useStore()
    const articleFormRef = ref(null)
    const article = ref(null)
    const loadError = ref(false)
    
    const articleForm = reactive({
      title: '',
      content: ''
    })

    const rules = {
      title: [
        { required: true, message: '请输入文章标题', trigger: 'blur' },
        { min: 2, max: 100, message: '标题长度在 2 到 100 个字符', trigger: 'blur' }
      ],
      content: [
        { required: true, message: '请输入文章内容', trigger: 'blur' },
        { min: 10, message: '内容不能少于 10 个字符', trigger: 'blur' }
      ]
    }

    const fetchArticle = async () => {
      try {
        const response = await axios.get(`/articles/${route.params.id}`)
        article.value = response.data
        
        // 检查是否是文章作者
        if (article.value.author.id !== store.state.user?.id) {
          loadError.value = true
          return
        }
        
        // 填充表单
        articleForm.title = article.value.title
        articleForm.content = article.value.content
      } catch (error) {
        console.error('获取文章失败:', error)
        loadError.value = true
      }
    }

    const handleSubmit = () => {
      articleFormRef.value.validate(async (valid) => {
        if (valid) {
          try {
            await axios.put(`/articles/${route.params.id}`, articleForm)
            ElMessage.success('文章更新成功')
            router.push('/')
          } catch (error) {
            ElMessage.error(error.response?.data?.message || '更新失败')
          }
        }
      })
    }

    onMounted(() => {
      fetchArticle()
    })

    return {
      articleForm,
      articleFormRef,
      article,
      loadError,
      rules,
      handleSubmit
    }
  }
}
</script>

<style scoped>
.edit-article {
  min-height: 100vh;
  background-color: #f5f7fa;
}

.el-header {
  padding: 0;
}

.card-header {
  text-align: center;
  font-size: 20px;
}

.el-form {
  max-width: 800px;
  margin: 0 auto;
}

.loading {
  min-height: 400px;
  display: flex;
  justify-content: center;
  align-items: center;
}
</style> 