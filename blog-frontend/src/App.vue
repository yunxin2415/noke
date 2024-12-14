<template>
  <router-view></router-view>
</template>

<script>
export default {
  name: 'App',
  mounted() {
    // 处理 ResizeObserver 错误
    const resizeHandler = () => {
      const resizeObserverError = document.getElementById('webpack-dev-server-client-overlay')
      if (resizeObserverError) {
        resizeObserverError.style.display = 'none'
      }
    }
    window.addEventListener('error', (e) => {
      if (e.message === 'ResizeObserver loop completed with undelivered notifications.') {
        e.stopImmediatePropagation()
      }
    })
    window.addEventListener('resize', resizeHandler)
  }
}
</script>

<style>
:root {
  --app-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  --app-radius: 12px;
  --app-bg-color: #f5f7fa;
  --app-card-bg: rgba(255, 255, 255, 0.9);
  --app-transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

html, body {
  margin: 0;
  padding: 0;
  height: 100%;
  background-color: var(--app-bg-color);
}

#app {
  font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, Helvetica, Arial, sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  height: 100%;
}

/* 全局卡片样式 */
.el-card {
  border: none !important;
  border-radius: var(--app-radius) !important;
  box-shadow: var(--app-shadow) !important;
  backdrop-filter: blur(10px);
  background-color: var(--app-card-bg) !important;
  transition: var(--app-transition);
}

.el-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.12) !important;
}

/* 全局按钮样式 */
.el-button {
  border-radius: 8px !important;
  transition: var(--app-transition);
}

.el-button:not(.is-text):hover {
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

/* 全局输入框样式 */
.el-input__wrapper {
  border-radius: 8px !important;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05) !important;
}

/* 全局菜单样式 */
.el-menu {
  border-radius: var(--app-radius) !important;
  box-shadow: var(--app-shadow) !important;
  backdrop-filter: blur(10px);
  background-color: var(--app-card-bg) !important;
}

/* 全局头像样式 */
.el-avatar {
  box-shadow: var(--app-shadow);
}

/* 全局表格样式 */
.el-table {
  border-radius: var(--app-radius) !important;
  overflow: hidden;
}

/* 全局对话框样式 */
.el-dialog {
  border-radius: var(--app-radius) !important;
  overflow: hidden;
}

/* 全局下拉菜单样式 */
.el-dropdown-menu {
  border-radius: 10px !important;
  box-shadow: var(--app-shadow) !important;
  backdrop-filter: blur(10px);
  background-color: var(--app-card-bg) !important;
}

/* 全局加载动画样式 */
.el-loading-mask {
  backdrop-filter: blur(5px);
  background-color: rgba(255, 255, 255, 0.8) !important;
}

/* 全局滚动条样式 */
::-webkit-scrollbar {
  width: 8px;
  height: 8px;
}

::-webkit-scrollbar-track {
  background: transparent;
}

::-webkit-scrollbar-thumb {
  background: #ddd;
  border-radius: 4px;
}

::-webkit-scrollbar-thumb:hover {
  background: #ccc;
}

/* 全局过渡动画 */
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

.slide-fade-enter-active,
.slide-fade-leave-active {
  transition: all 0.3s ease;
}

.slide-fade-enter-from,
.slide-fade-leave-to {
  transform: translateY(10px);
  opacity: 0;
}

/* 隐藏开发服务器错误覆盖层 */
#webpack-dev-server-client-overlay {
  display: none !important;
}
</style>
