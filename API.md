# API 文档

## 基础信息
- 基础路径: `/api`
- 访问方式: 
  - 开发环境: `http://{hostname}:{port}/api`
  - 生产环境: `http://{hostname}/api`
- 支持的访问域名:
  - localhost
  - 127.0.0.1
  - 局域网 IP（例如：192.168.3.82）
- 认证方式: Bearer Token
- 响应格式: JSON
- 编码方式: UTF-8

## 跨域配置要求
后端需要配置以下 CORS 头:
```http
Access-Control-Allow-Origin: *
Access-Control-Allow-Methods: GET, POST, PUT, DELETE, OPTIONS
Access-Control-Allow-Headers: Origin, X-Requested-With, Content-Type, Accept, Authorization
Access-Control-Max-Age: 3600
```

## 错误处理
API 使用标准的 HTTP 状态码表示请求的状态：

- 200: 请求成功
- 400: 请求参数错误
- 401: 未授权（需要登录）
- 403: 禁止访问（权限不足）
- 404: 资源不存在
- 500: 服务器内部错误

错误响应格式：
```json
{
  "message": "错误信息",
  "error": "错误类型（可选）",
  "path": "请求路径（可选）"
}
```

## 认证相关 API

### 登录
- 路径: `POST /auth/login`
- 描述: 用户登录接口
- 权限: 公开
- 请求体:
  ```json
  {
    "username": "string",
    "password": "string"
  }
  ```
- 响应体:
  ```json
  {
    "token": "string",
    "user": {
      "id": "number",
      "username": "string",
      "email": "string",
      "avatar": "string"
    }
  }
  ```

### 注册
- 路径: `POST /auth/register`
- 描述: 用户注册接口
- 权限: 公开
- 请求体:
  ```json
  {
    "username": "string",
    "password": "string",
    "email": "string",
    "captcha": "string",
    "captchaId": "string"
  }
  ```
- 响应体:
  ```json
  {
    "message": "注册成功",
    "user": {
      "id": "number",
      "username": "string",
      "email": "string"
    }
  }
  ```

### 检查用户名
- 路径: `GET /auth/check/{username}`
- 描述: 检查用户名是否存在
- 权限: 公开
- 响应体:
  ```json
  {
    "exists": "boolean",
    "user": {
      "id": "number",
      "username": "string",
      "email": "string"
    }
  }
  ```

### 获取验证码
- 路径: `GET /auth/captcha`
- 描述: 获取登录验证码
- 权限: 公开访问
- 响应体:
  ```json
  {
    "captchaId": "string",
    "captchaImage": "string"
  }
  ```

## 文章相关

### 获取文章列表
- 路径: `GET /articles`
- 描述: 获取文章列表
- 权限: 公开
- 查询参数:
  ```
  _t: 时间戳（防止缓存）
  type: 文章类型（可选，tech/life）
  page: 页码（可选，默认1）
  size: 每页数量（可选，默认10）
  ```
- 响应体:
  ```json
  {
    "data": [
      {
        "id": "number",
        "title": "string",
        "content": "string",
        "author": {
          "id": "number",
          "username": "string",
          "avatar": "string"
        },
        "createdAt": "datetime",
        "updatedAt": "datetime"
      }
    ],
    "total": "number",
    "page": "number",
    "size": "number"
  }
  ```

### 获取用户文章列表
- 路径: `GET /articles/user`
- 描述: 获取当前用户的文章列表
- 权限: 需要认证
- 请求头: 
  ```http
  Authorization: Bearer <token>
  ```
- 响应体: 同文章列表

### 获取文章详情
- 路径: `GET /articles/{id}`
- 描述: 获取指定文章的详细信息
- 权限: 公开
- 响应体:
  ```json
  {
    "id": "number",
    "title": "string",
    "content": "string",
    "author": {
      "id": "number",
      "username": "string",
      "avatar": "string"
    },
    "createdAt": "datetime",
    "updatedAt": "datetime"
  }
  ```

### 创建文章
- 路径: `POST /articles`
- 描述: 创建新文章
- 权限: 需要认证
- 请求头:
  ```http
  Authorization: Bearer <token>
  Content-Type: application/json
  ```
- 请求体:
  ```json
  {
    "title": "string",
    "content": "string",
    "category": "string",
    "tags": "string",
    "isPrivate": "boolean",
    "isDownloadable": "boolean"
  }
  ```
- 响应体: 同文章详情

### 更新文章
- 路径: `PUT /articles/{id}`
- 描述: 更新现有文章
- 权限: 需要认证（仅作者）
- 路径参数:
  - `id`: number, 必需，文章ID
- 请求体: 创建文章
- 响应体: 同文章详情

### 删除文章
- 路径: `DELETE /articles/{id}`
- 描述: 删除文章
- 权限: 需要认证（仅作者）
- 路径参数:
  - `id`: number, 必需，文章ID
- 响应体:
  ```json
  {
    "message": "string"
  }
  ```

### 下载文章
- 路径: `GET /articles/{id}/download`
- 描述: 下载文章内容
- 权限: 需要认证（公开文章所有人可下载，私密文章仅作者）
- 路径参数:
  - `id`: number, 必需，文章ID
- 响应头:
  ```http
  Content-Type: application/octet-stream
  Content-Disposition: attachment; filename=article.md
  ```

## 用户相关

### 获取用户信息
- 路径: `GET /users/profile`
- 描述: 获取当前用户信息
- 权限: 需要认证
- 响应体:
  ```json
  {
    "id": "number",
    "username": "string",
    "email": "string",
    "bio": "string",
    "avatar": "string",
    "createdAt": "datetime"
  }
  ```

### 更新用户信息
- 路径: `PUT /users/profile`
- 描述: 更新用户信息
- 权限: 需要认证
- 请求体:
  ```json
  {
    "email": "string",
    "bio": "string",
    "avatar": "string"
  }
  ```
- 响应体: 同用户信息

### 修改密码
- 路径: `PUT /users/password`
- 描述: 修改用户密码
- 权限: 需要认证
- 请求体:
  ```json
  {
    "currentPassword": "string",
    "newPassword": "string",
    "confirmPassword": "string"
  }
  ```
- 响应体:
  ```json
  {
    "message": "string"
  }
  ```

### 关注用户
- 路径: `POST /users/{userId}/follow`
- 描述: 关注指定用户
- 权限: 需要认证
- 路径参数:
  - `userId`: number, 必需，目标用户ID
- 响应体:
  ```json
  {
    "message": "string"
  }
  ```

### 取消关注
- 路径: `POST /users/{userId}/unfollow`
- 描述: 取消关注指定用户
- 权限: 需要认证
- 路径参数:
  - `userId`: number, 必需，目标用户ID
- 响应体:
  ```json
  {
    "message": "string"
  }
  ```

### 检查关注状态
- 路径: `GET /users/{userId}/following`
- 描述: 检查当前用户是否已关注指定用户
- 权限: 需要认证
- 路径参数:
  - `userId`: number, 必需，目标用户ID
- 响应体:
  ```json
  {
    "following": "boolean"
  }
  ```

## 文件上传

### 上传头像
- 路径: `POST /upload/avatar`
- 描述: 上传用户头像
- 权限: 需要认证
- 请求头:
  ```http
  Authorization: Bearer <token>
  Content-Type: multipart/form-data
  ```
- 请求体:
  ```
  file: 图片文件（支持 jpg, png, gif）
  ```
- 响应体:
  ```json
  {
    "url": "string",
    "message": "上传成功"
  }
  ```

### 上传文章图片
- 路径: `POST /upload/image`
- 描述: 上传文章中使用的图片
- 权限: 需要认证
- 请求头:
  ```http
  Authorization: Bearer <token>
  Content-Type: multipart/form-data
  ```
- 请求体:
  ```
  file: 图片文件（支持 jpg, png, gif）
  ```
- 响应体:
  ```json
  {
    "urls": ["string"],
    "message": "上传成功"
  }
  ```

## 通用说明

### 认证方式
所有需要认证的接口都需要在请求头中携带 token：
```http
Authorization: Bearer <token>
```

### 错误响应
所有接口的错误响应格式统一为：
```json
{
  "message": "string",
  "error": "string",
  "timestamp": "datetime",
  "path": "string",
  "status": "number"
}
```

### 状态码说明
- 200: 请求成功
- 201: 创建成功
- 400: 请求参数错误
- 401: 未认证
- 403: 无权限
- 404: 资源不存在
- 500: 服务器内部错误

### 时间格式
所有时间字段使用 ISO 8601 格式：
```
YYYY-MM-DDTHH:mm:ss.sssZ
```

### 分页参数
支持分页的接口统一使用以下查询参数：
- `page`: 页码，从1开始
- `size`: 每页数量
- `sort`: 排序字段
- `order`: 排序方向（asc/desc）

### 文件访问路径
- 头像文件: `/uploads/avatars/{year}/{month}/{day}/{filename}`
- 文章图片: `/uploads/articles/{year}/{month}/{day}/{filename}`

注意：所有上传的文件都应该通过完整的 URL 访问，例如：
```
http://{hostname}:{port}/uploads/avatars/2024/12/13/example.jpg
```