<template>
  <div class="create-article">
    <div class="background-animation"></div>
    <el-container>
      <el-header>
        <el-menu mode="horizontal" router class="custom-menu">
          <el-menu-item index="/">返回首页</el-menu-item>
        </el-menu>
      </el-header>
      
      <el-main>
        <el-card class="glass-card">
          <template #header>
            <div class="card-header">
              <span>发布文章</span>
            </div>
          </template>
          
          <el-form :model="articleForm" :rules="rules" ref="articleFormRef" label-width="80px">
            <el-form-item label="标题" prop="title">
              <el-input v-model="articleForm.title" placeholder="请输入文章标题" class="custom-input"></el-input>
            </el-form-item>

            <el-form-item label="分类" prop="category">
              <el-select v-model="articleForm.category" placeholder="请选择文章分类" class="custom-select">
                <el-option v-for="item in categoryOptions" :key="item.value" :label="item.label" :value="item.value"></el-option>
              </el-select>
            </el-form-item>

            <div class="settings-group">
              <el-form-item label="文章设置" class="settings-label">
                <div class="settings-container">
                  <div class="setting-item">
                    <span class="setting-label">公开设置</span>
                    <el-switch
                      v-model="articleForm.isPrivate"
                      :active-value="false"
                      :inactive-value="true"
                      active-text="公开"
                      inactive-text="私密"
                      class="custom-switch"
                    />
                  </div>
                  <div class="setting-item">
                    <span class="setting-label">下载设置</span>
                    <el-switch
                      v-model="articleForm.isDownloadable"
                      active-text="允许下载"
                      inactive-text="禁止下载"
                      class="custom-switch"
                    />
                  </div>
                </div>
              </el-form-item>
            </div>

            <el-form-item label="标签" prop="tags">
              <el-tag
                v-for="tag in dynamicTags"
                :key="tag"
                class="mx-1"
                closable
                :disable-transitions="false"
                @close="handleClose(tag)"
              >
                {{ tag }}
              </el-tag>
              <el-input
                v-if="inputVisible"
                ref="InputRef"
                v-model="inputValue"
                class="tag-input"
                size="small"
                @keyup.enter="handleInputConfirm"
                @blur="handleInputConfirm"
                placeholder="输入标签后按回车"
              />
              <el-button v-else class="button-new-tag" size="small" @click="showInput">
                + 添加标签
              </el-button>
            </el-form-item>
            
            <el-form-item label="内容" prop="content">
              <div class="editor-container">
                <div class="editor-toolbar">
                  <el-upload
                    class="upload-md"
                    accept=".md,.markdown"
                    :auto-upload="false"
                    :show-file-list="false"
                    :on-change="handleFileChange"
                  >
                    <el-button type="primary" plain size="small">
                      <el-icon><Upload /></el-icon>
                      导入 Markdown 文件
                    </el-button>
                  </el-upload>
                </div>
                <MdEditor
                  v-model="articleForm.content"
                  language="zh-CN"
                  theme="dark"
                  :toolbarsExclude="[]"
                  @onUploadImg="onUploadImg"
                  :showCodeRowNumber="true"
                  placeholder="请输入 Markdown 格式的文章内容"
                  :previewTheme="previewTheme"
                  :codeTheme="codeTheme"
                />
              </div>
            </el-form-item>
            
            <el-form-item>
              <el-button type="primary" @click="handleSubmit" class="custom-button">发布文章</el-button>
              <el-button @click="$router.push('/')" class="custom-button-secondary">取消</el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </el-main>
    </el-container>

    <!-- 图片上传进度提示 -->
    <el-dialog
      v-model="uploadDialog.visible"
      :title="uploadDialog.title"
      width="300px"
      :close-on-click-modal="false"
      :show-close="false"
    >
      <el-progress
        :percentage="uploadDialog.progress"
        :status="uploadDialog.status"
      />
      <div class="upload-message">{{ uploadDialog.message }}</div>
    </el-dialog>
  </div>
</template>

<script>
import { ref, onMounted, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { MdEditor } from 'md-editor-v3'
import axios from 'axios'
import 'md-editor-v3/lib/style.css'
import { createArticle } from '@/api/article'
import { Upload } from '@element-plus/icons-vue'

export default {
  name: 'CreateArticle',
  components: {
    MdEditor,
    Upload
  },
  setup() {
    const router = useRouter()
    const articleFormRef = ref(null)
    const articleForm = ref({
      title: '',
      content: '',
      category: '默认分类',
      tags: '',
      isPrivate: false,
      isDownloadable: true
    })

    // 标签相关
    const dynamicTags = ref([])
    const inputVisible = ref(false)
    const inputValue = ref('')
    const InputRef = ref(null)

    const handleClose = (tag) => {
      dynamicTags.value.splice(dynamicTags.value.indexOf(tag), 1)
      articleForm.value.tags = dynamicTags.value.join(',')
    }

    const showInput = () => {
      inputVisible.value = true
      nextTick(() => {
        InputRef.value.focus()
      })
    }

    const handleInputConfirm = () => {
      if (inputValue.value) {
        if (inputValue.value.startsWith('#')) {
          inputValue.value = inputValue.value.substring(1)
        }
        if (inputValue.value && !dynamicTags.value.includes(inputValue.value)) {
          dynamicTags.value.push(inputValue.value)
          articleForm.value.tags = dynamicTags.value.join(',')
        }
      }
      inputVisible.value = false
      inputValue.value = ''
    }

    // 检查用户是否登录
    onMounted(() => {
      const token = localStorage.getItem('token')
      if (!token) {
        ElMessage.error('请先登录')
        router.push({
          path: '/login',
          query: { redirect: router.currentRoute.value.fullPath }
        })
        return
      }
    })

    // 编辑器主题配置
    const previewTheme = 'github'
    const codeTheme = 'atom-one-dark'

    // 上传对话框状态
    const uploadDialog = ref({
      visible: false,
      title: '上传中',
      progress: 0,
      status: '',
      message: '正在上传图片...'
    })

    const rules = {
      title: [
        { required: true, message: '请输入文章标题', trigger: 'blur' },
        { min: 2, max: 100, message: '标题长度在 2 到 100 个字符', trigger: 'blur' }
      ],
      content: [
        { required: true, message: '请输入文章内容', trigger: 'blur' },
        { min: 10, message: '内容不能少于 10 个字符', trigger: 'blur' }
      ],
      category: [
        { required: true, message: '请选择文章分类', trigger: 'change' }
      ]
    }

    // 文章分类选项
    const categoryOptions = ref([
      { value: '默认分类', label: '默认分类' },
      { value: '技术', label: '技术' },
      { value: '生活', label: '生活' },
      { value: '随笔', label: '随笔' }
    ])

    // 处理图片上传
    const onUploadImg = async (files, callback) => {
      const formData = new FormData()
      Array.from(files).forEach(file => {
        formData.append('files[]', file)
      })

      // 显示上传对话框
      uploadDialog.value.visible = true
      uploadDialog.value.progress = 0
      uploadDialog.value.status = ''
      uploadDialog.value.message = '正在上传图片...'

      try {
        const token = localStorage.getItem('token')
        if (!token) {
          throw new Error('未登录')
        }

        const res = await axios.post('/api/upload/image', formData, {
          headers: {
            'Content-Type': 'multipart/form-data',
            'Authorization': `Bearer ${token}`
          },
          onUploadProgress: (progressEvent) => {
            const progress = Math.round((progressEvent.loaded * 100) / progressEvent.total)
            uploadDialog.value.progress = progress
          }
        })
        
        if (res.data && res.data.urls) {
          const fullUrls = res.data.urls.map(url => `${window.location.origin}${url}`)
          callback(fullUrls)
          uploadDialog.value.status = 'success'
          uploadDialog.value.message = '上传成功'
          ElMessage.success('图片上传成功')

          setTimeout(() => {
            uploadDialog.value.visible = false
          }, 1000)
        } else {
          throw new Error('上传响应格式错误')
        }
      } catch (error) {
        console.error('Error uploading images:', error)
        uploadDialog.value.status = 'exception'
        uploadDialog.value.message = error.response?.data || error.message || '图片上传失败'
        ElMessage.error(uploadDialog.value.message)
        callback([])

        setTimeout(() => {
          uploadDialog.value.visible = false
        }, 2000)
      }
    }

    const handleSubmit = async () => {
      if (!articleFormRef.value) return

      try {
        const token = localStorage.getItem('token')
        if (!token) {
          ElMessage.error('请先登录')
          router.push({
            path: '/login',
            query: { redirect: router.currentRoute.value.fullPath }
          })
          return
        }

        const valid = await articleFormRef.value.validate()
        if (valid) {
          ElMessage({
            message: '正在发布文章...',
            type: 'info'
          })

          const response = await createArticle({
            title: articleForm.value.title,
            content: articleForm.value.content,
            category: articleForm.value.category,
            tags: articleForm.value.tags,
            isPrivate: articleForm.value.isPrivate,
            isDownloadable: articleForm.value.isDownloadable
          })

          console.log('创建文章响应:', response)

          // 检查响应数据的完整性
          if (!response) {
            throw new Error('发布失败，服务器响应异常')
          }

          // 确保响应中包含必要的字段
          if (!response.id) {
            throw new Error('发布失败，无法获取文章ID')
          }

          ElMessage.success('文章发布成功')
          
          // 等待一小段时间再跳转，确保消息能够显示
          setTimeout(() => {
            router.push(`/article/${response.id}`)
          }, 500)
        }
      } catch (error) {
        console.error('Error creating article:', error)
        // 根据错误类型显示不同的错误信息
        if (error.response?.status === 403) {
          ElMessage.error('没有权限，请先登录')
          router.push({
            path: '/login',
            query: { redirect: router.currentRoute.value.fullPath }
          })
        } else if (error.response?.status === 400) {
          ElMessage.error(error.response.data?.message || '请求参数错误')
        } else if (error.response?.status === 500) {
          ElMessage.error('服务器错误，请稍后重试')
        } else {
          // 显示具体的错误信息
          ElMessage.error(error.message || '发布失败，请重试')
        }
      }
    }

    // 处理文件导入
    const handleFileChange = (file) => {
      if (!file) return
      
      const reader = new FileReader()
      reader.onload = (e) => {
        try {
          const content = e.target.result
          articleForm.value.content = content
          
          // 尝试从文件名中提取标题（如果标题还没有设置）
          if (!articleForm.value.title) {
            let title = file.name.replace(/\.(md|markdown)$/i, '').trim()
            // 如果文件名太长，截取前100个字符
            if (title.length > 100) {
              title = title.substring(0, 97) + '...'
            }
            articleForm.value.title = title
          }
          
          ElMessage.success('文件导入成功')
        } catch (error) {
          console.error('文件读取失败:', error)
          ElMessage.error('文件读取失败，请确保文件格式正确')
        }
      }
      
      reader.onerror = () => {
        ElMessage.error('文件读取失败，请重试')
      }
      
      reader.readAsText(file.raw)
    }

    return {
      articleForm,
      articleFormRef,
      rules,
      handleSubmit,
      onUploadImg,
      uploadDialog,
      previewTheme,
      codeTheme,
      categoryOptions,
      // 标签相关
      dynamicTags,
      inputVisible,
      inputValue,
      InputRef,
      handleClose,
      showInput,
      handleInputConfirm,
      // 文件导入相关
      handleFileChange
    }
  }
}
</script>

<style scoped>
.create-article {
  min-height: 100vh;
  background-color: #0a192f;
  position: relative;
  overflow: hidden;
}

.background-animation {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(45deg, #0a192f, #112240, #1a365d);
  background-size: 400% 400%;
  animation: gradient 15s ease infinite;
  z-index: 0;
}

.settings-group {
  background: rgba(16, 32, 64, 0.2);
  border-radius: 8px;
  padding: 20px;
  margin-bottom: 20px;
  border: 1px solid rgba(100, 255, 218, 0.1);
}

.settings-label {
  margin-bottom: 0 !important;
}

.settings-container {
  display: flex;
  flex-wrap: wrap;
  gap: 24px;
  margin-top: 16px;
}

.setting-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 8px 16px;
  background: rgba(16, 32, 64, 0.4);
  border-radius: 6px;
  border: 1px solid rgba(100, 255, 218, 0.2);
}

.setting-label {
  color: #64ffda;
  font-size: 14px;
  min-width: 70px;
}

.custom-switch {
  --el-switch-on-color: #64ffda;
}

:deep(.el-switch__label) {
  color: #8892b0 !important;
}

:deep(.el-switch__label.is-active) {
  color: #64ffda !important;
}

:deep(.el-switch.is-checked .el-switch__core) {
  border-color: #64ffda !important;
  background-color: #64ffda !important;
}

:deep(.el-form-item__label) {
  color: #64ffda !important;
}

.custom-select {
  width: 100%;
}

:deep(.el-select) {
  width: 100%;
}

:deep(.el-select .el-input__wrapper) {
  background: rgba(16, 32, 64, 0.4);
  border: 1px solid rgba(100, 255, 218, 0.2);
  box-shadow: none;
}

:deep(.el-select .el-input__wrapper:hover) {
  border-color: #64ffda;
}

:deep(.el-select .el-input__wrapper.is-focus) {
  border-color: #64ffda;
  box-shadow: 0 0 0 1px #64ffda;
}

:deep(.el-input__wrapper) {
  background: rgba(16, 32, 64, 0.4);
  border: 1px solid rgba(100, 255, 218, 0.2);
  box-shadow: none;
}

:deep(.el-input__wrapper:hover) {
  border-color: #64ffda;
}

:deep(.el-input__wrapper.is-focus) {
  border-color: #64ffda;
  box-shadow: 0 0 0 1px #64ffda;
}

:deep(.el-button) {
  background: transparent;
  border: 1px solid #64ffda;
  color: #64ffda;
}

:deep(.el-button:hover) {
  background: rgba(100, 255, 218, 0.1);
  color: #64ffda;
}

:deep(.el-button--primary) {
  background: #64ffda;
  color: #0a192f;
}

:deep(.el-button--primary:hover) {
  background: #64ffda;
  opacity: 0.9;
}

.tag-input {
  width: 100px;
  margin-left: 12px;
  vertical-align: bottom;
}

:deep(.el-tag) {
  background: rgba(16, 32, 64, 0.4);
  border: 1px solid rgba(100, 255, 218, 0.2);
  color: #64ffda;
}

:deep(.el-tag .el-tag__close) {
  color: #64ffda;
}

:deep(.el-tag .el-tag__close:hover) {
  background-color: #64ffda;
  color: #0a192f;
}

@keyframes gradient {
  0% {
    background-position: 0% 50%;
  }
  50% {
    background-position: 100% 50%;
  }
  100% {
    background-position: 0% 50%;
  }
}

.el-container {
  position: relative;
  z-index: 1;
}

.el-header {
  padding: 0;
  height: 60px !important;
  backdrop-filter: blur(10px);
  background: rgba(255, 255, 255, 0.05);
}

.custom-menu {
  background: transparent !important;
  border: none !important;
}

.custom-menu .el-menu-item {
  color: #64ffda !important;
  transition: all 0.3s ease;
}

.custom-menu .el-menu-item:hover {
  color: #fff !important;
  background: rgba(100, 255, 218, 0.1) !important;
}

.el-main {
  padding: 20px;
  height: calc(100vh - 60px);
}

.glass-card {
  background: rgba(16, 32, 64, 0.7) !important;
  backdrop-filter: blur(10px);
  border: 1px solid rgba(100, 255, 218, 0.1) !important;
  border-radius: 16px !important;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.2) !important;
}

.card-header {
  text-align: center;
  font-size: 24px;
  margin-bottom: 20px;
  color: #64ffda;
  text-shadow: 0 0 10px rgba(100, 255, 218, 0.3);
}

.el-form {
  max-width: 1400px;
  margin: 0 auto;
}

.custom-input :deep(.el-input__wrapper) {
  background: rgba(16, 32, 64, 0.7) !important;
  border: 1px solid rgba(100, 255, 218, 0.2) !important;
  box-shadow: none !important;
  transition: all 0.3s ease;
}

.custom-input :deep(.el-input__wrapper:hover),
.custom-input :deep(.el-input__wrapper.is-focus) {
  border-color: #64ffda !important;
  box-shadow: 0 0 10px rgba(100, 255, 218, 0.2) !important;
}

.custom-input :deep(.el-input__inner) {
  color: #ccd6f6 !important;
}

.editor-container {
  border: 1px solid rgba(100, 255, 218, 0.2);
  border-radius: 16px;
  height: calc(100vh - 200px);
  min-height: 600px;
  overflow: hidden;
  width: 100%;
  transition: all 0.3s ease;
  position: relative;
  z-index: 2;
}

.editor-container:hover {
  border-color: #64ffda;
  box-shadow: 0 0 20px rgba(100, 255, 218, 0.1);
}

:deep(.md-editor) {
  border: none !important;
  height: 100% !important;
  width: 100% !important;
}

:deep(.md-editor-input) {
  padding: 16px !important;
  min-height: calc(100% - 50px) !important;
  color: #ccd6f6 !important;
}

:deep(.md-editor-preview) {
  padding: 16px !important;
  color: #ccd6f6 !important;
}

:deep(.md-editor-content) {
  height: calc(100% - 50px) !important;
}

:deep(.md-editor-area) {
  min-height: 100% !important;
  height: 100% !important;
}

:deep(.md-editor-input-wrapper) {
  height: 100% !important;
}

:deep(.md-editor-toolbar) {
  border-bottom: 1px solid rgba(100, 255, 218, 0.2);
  padding: 8px 12px !important;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.2);
  display: flex;
  justify-content: center;
  gap: 4px;
  height: 50px;
}

:deep(.md-editor-toolbar button) {
  border-radius: 8px;
  margin: 0 2px;
  padding: 8px 12px;
  font-size: 15px;
  color: #64ffda !important;
  background: transparent !important;
  transition: all 0.3s ease;
}

:deep(.md-editor-toolbar button:hover) {
  background: rgba(100, 255, 218, 0.1) !important;
  transform: translateY(-1px);
}

:deep(.md-editor-toolbar .md-editor-toolbar-item) {
  min-width: 40px;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.custom-button {
  background: #64ffda !important;
  color: #0a192f !important;
  border: none !important;
  padding: 12px 24px !important;
  border-radius: 8px !important;
  font-weight: 600 !important;
  transition: all 0.3s ease !important;
}

.custom-button:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(100, 255, 218, 0.3);
}

.custom-button-secondary {
  background: transparent !important;
  color: #64ffda !important;
  border: 1px solid #64ffda !important;
  padding: 12px 24px !important;
  border-radius: 8px !important;
  font-weight: 600 !important;
  transition: all 0.3s ease !important;
}

.custom-button-secondary:hover {
  background: rgba(100, 255, 218, 0.1) !important;
  transform: translateY(-2px);
}

:deep(.el-form-item__label) {
  color: #64ffda !important;
}

/* 上传对话框样式 */
.upload-message {
  margin-top: 16px;
  text-align: center;
  color: #64ffda;
}

:deep(.el-dialog) {
  background: rgba(16, 32, 64, 0.9) !important;
  border: 1px solid rgba(100, 255, 218, 0.2) !important;
  border-radius: 16px !important;
  backdrop-filter: blur(10px);
}

:deep(.el-dialog__title) {
  color: #64ffda !important;
}

:deep(.el-progress-bar__outer) {
  background-color: rgba(100, 255, 218, 0.1) !important;
}

:deep(.el-progress-bar__inner) {
  background-color: #64ffda !important;
}

:deep(.el-progress__text) {
  color: #64ffda !important;
}

.tag-input {
  width: 100px;
  margin-left: 12px;
  vertical-align: bottom;
}

.button-new-tag {
  margin-left: 12px;
  height: 32px;
  padding-top: 0;
  padding-bottom: 0;
}

.el-tag {
  margin-right: 12px;
  margin-bottom: 8px;
}

.editor-toolbar {
  padding: 8px;
  background-color: #1e1e1e;
  border-bottom: 1px solid #333;
  display: flex;
  justify-content: flex-end;
}

.upload-md {
  display: inline-block;
}

.upload-md .el-button {
  margin-right: 8px;
}

.el-icon {
  margin-right: 4px;
}
</style> 