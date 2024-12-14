-- 创建数据库
CREATE DATABASE IF NOT EXISTS blog_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 创建用户并授权
CREATE USER IF NOT EXISTS 'bloguser'@'localhost' IDENTIFIED BY '123456';
GRANT ALL PRIVILEGES ON blog_db.* TO 'bloguser'@'localhost';
FLUSH PRIVILEGES; 