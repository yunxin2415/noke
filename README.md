# 博客系统

一个现代化的个人博客系统，提供文章管理、用户互动等功能。

## 功能特性

### 文章管理
- 支持 Markdown 格式写作
- 文章分类和标签管理
- 私密/公开文章设置
- 文章下载功能
- 支持导入 Markdown 文件

### 用户系统
- 用户注册和登录
- 个人资料管理
- 修改密码
- 账户注销
- 用户关注功能

### 界面特性
- 响应式设计
- 支持明暗主题切换
- 文章卡片/列表视图切换
- 代码高亮显示
- 标签云展示

## 技术栈

### 前端
- Vue 3
- Element Plus
- Markdown Editor
- Axios
- Vue Router
- Vuex

### 后端
- Spring Boot
- Spring Security
- Spring Data JPA
- MySQL
- JWT 认证

## 开发环境要求

- Node.js >= 14
- JDK >= 11
- MySQL >= 8.0

## 安装说明

1. 克隆项目
```bash
git clone [项目地址]
```

2. 前端设置
```bash
cd blog-frontend
npm install
```

3. 后端设置
```bash
cd blog-backend
mvn install
```

4. 配置数据库
- 创建 MySQL 数据库
- 修改 `application.properties` 中的数据库配置

## 运行项目

1. 启动后端服务
```bash
cd blog-backend
..\apache-maven-3.9.6\bin\mvn spring-boot:run
```

2. 启动前端服务
```bash
cd blog-frontend
npm run serve
```

访问 `http://localhost:3000` 即可使用系统

## 主要功能说明

### 文章管理
- 创建文章：支持 Markdown 编辑器，可设置分类、标签、私密性等
- 编辑文章：支持修改文章内容、分类、标签等属性
- 删除文章：支持删除已发布的文章
- 文章列表：支持按分类、标签筛选，支持卡片/列表视图切换

### 用户功能
- 个人中心：管理个人资料、修改密码
- 文章管理：查看和管理个人发布的文章
- 关注系统：关注其他用户，查看关注状态

### 安全特性
- JWT 认证
- 密码加密存储
- 接口权限控制
- 防止 XSS 攻击

## API 文档

### 文章相关
- `GET /api/articles` - 获取文章列表
- `POST /api/articles` - 创建新文章
- `PUT /api/articles/{id}` - 更新文章
- `DELETE /api/articles/{id}` - 删除文章
- `GET /api/articles/{id}/download` - 下载文章

### 用户相关
- `POST /api/auth/register` - 用户注册
- `POST /api/auth/login` - 用户登录
- `GET /api/users/profile` - 获取用户信息
- `PUT /api/users/profile` - 更新用户信息
- `DELETE /api/users/account` - 注销账户

## 贡献指南

1. Fork 项目
2. 创建功能分支
3. 提交更改
4. 发起 Pull Request

## 许可证

MIT License