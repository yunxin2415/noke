-- 插入管理员账户
INSERT INTO users (username, password, email, role, bio, created_at, updated_at)
VALUES 
('admin', '$2a$10$EqKcp1WFKVQXVHvMrvT8o.YwQYfOtVj.BaX.IkWqZK/Zm4kSR72.q', 'admin@example.com', 'ROLE_ADMIN', '管理员账号', NOW(), NOW())
ON DUPLICATE KEY UPDATE 
    password = VALUES(password),
    role = VALUES(role),
    updated_at = NOW();

-- 插入测试用户
INSERT INTO users (username, password, email, role, bio, created_at, updated_at)
VALUES 
('test', '$2a$10$EqKcp1WFKVQXVHvMrvT8o.YwQYfOtVj.BaX.IkWqZK/Zm4kSR72.q', 'test@example.com', 'ROLE_USER', '测试账号', NOW(), NOW())
ON DUPLICATE KEY UPDATE 
    password = VALUES(password),
    role = VALUES(role),
    updated_at = NOW();