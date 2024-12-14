package com.example.blogbackend.controller;

import com.example.blogbackend.entity.User;
import com.example.blogbackend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = {"http://localhost:3000", "http://127.0.0.1:3000"}, allowCredentials = "true")
@PreAuthorize("hasRole('ROLE_ADMIN')")  // 整个控制器都需要管理员权限
public class AdminController {

    @Autowired
    private UserRepository userRepository;

    // 获取所有用户列表
    @GetMapping("/users")
    public ResponseEntity<?> getAllUsers() {
        try {
            List<User> users = userRepository.findAll();
            // 移除敏感信息
            users.forEach(user -> user.setPassword(null));
            return ResponseEntity.ok(users);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "获取用户列表失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    // 更新用户信息
    @PutMapping("/users/{id}")
    public ResponseEntity<?> updateUser(@PathVariable Long id, @RequestBody User updatedUser) {
        try {
            User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

            // 更新允许的字段
            if (updatedUser.getEmail() != null) {
                user.setEmail(updatedUser.getEmail());
            }
            if (updatedUser.getRole() != null) {
                user.setRole(updatedUser.getRole());
            }
            // 可以添加其他允许更新的字段

            User savedUser = userRepository.save(user);
            savedUser.setPassword(null); // 移除敏感信息

            Map<String, Object> response = new HashMap<>();
            response.put("message", "用户更新成功");
            response.put("user", savedUser);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "更新用户失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    // 删除用户
    @DeleteMapping("/users/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        try {
            // 检查用户是否存在
            if (!userRepository.existsById(id)) {
                throw new RuntimeException("用户不存在");
            }

            // 不允许删除管理员账号
            User user = userRepository.findById(id).get();
            if ("ROLE_ADMIN".equals(user.getRole())) {
                throw new RuntimeException("不能删除管理员账号");
            }

            userRepository.deleteById(id);
            
            Map<String, String> response = new HashMap<>();
            response.put("message", "用户删除成功");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "删除用户失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
} 