package com.example.blogbackend.controller;

import com.example.blogbackend.entity.User;
import com.example.blogbackend.entity.Article;
import com.example.blogbackend.repository.UserRepository;
import com.example.blogbackend.repository.ArticleRepository;
import com.example.blogbackend.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;
import java.util.List;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = {"http://localhost:3000", "http://127.0.0.1:3000"}, allowCredentials = "true")
public class UserController {
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ArticleRepository articleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // 获取用户文章列表
    @GetMapping("/articles")
    public ResponseEntity<?> getUserArticles() {
        try {
            // 获取当前用户
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            
            logger.debug("获取用户文章列表: {}", username);

            // 获取用户
            User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

            // 获取文章列表
            List<Article> articles = articleRepository.findByAuthorOrderByCreatedAtDesc(user.getId());
            
            return ResponseEntity.ok(articles);
        } catch (Exception e) {
            logger.error("获取用户文章列表失败", e);
            return ResponseEntity.badRequest().body(Map.of("message", "获取用户文章列表失败: " + e.getMessage()));
        }
    }

    // 获取用户信息
    @GetMapping("/profile")
    public ResponseEntity<?> getUserProfile() {
        try {
            // 获取当前用户
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            
            logger.debug("获取用户信息: {}", username);

            // 获取用户
            User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

            // 移除敏感信息
            user.setPassword(null);

            return ResponseEntity.ok(user);
        } catch (Exception e) {
            logger.error("获取用户信息失败", e);
            return ResponseEntity.badRequest().body(Map.of("message", "获取用户信息失败: " + e.getMessage()));
        }
    }

    // 更新用户信息
    @PutMapping("/profile")
    public ResponseEntity<?> updateProfile(@RequestBody User updatedUser) {
        try {
            // 获取当前用户
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            
            // 获取用户
            User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

            // 更新允许修改的字段
            if (updatedUser.getEmail() != null) {
                user.setEmail(updatedUser.getEmail());
            }
            if (updatedUser.getBio() != null) {
                user.setBio(updatedUser.getBio());
            }
            if (updatedUser.getAvatar() != null) {
                user.setAvatar(updatedUser.getAvatar());
            }

            // 保存更新
            user = userRepository.save(user);
            
            // 移除敏感信息
            user.setPassword(null);

            return ResponseEntity.ok(user);
        } catch (Exception e) {
            logger.error("更新用户信息失败", e);
            return ResponseEntity.badRequest().body(Map.of("message", "更新用户信息失败: " + e.getMessage()));
        }
    }

    // 修改密码
    @PutMapping("/password")
    public ResponseEntity<?> updatePassword(@RequestBody Map<String, String> request) {
        try {
            // 获取当前用户
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            
            // 获取请求参数
            String currentPassword = request.get("currentPassword");
            String newPassword = request.get("newPassword");
            
            // 验证参数
            if (currentPassword == null || currentPassword.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("message", "当前密码不能为空"));
            }
            if (newPassword == null || newPassword.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("message", "新密码不能为空"));
            }
            if (newPassword.length() < 6) {
                return ResponseEntity.badRequest().body(Map.of("message", "新密码长度不能小于6个字符"));
            }

            // 获取用户
            User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

            // 验证当前密码
            if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
                return ResponseEntity.badRequest().body(Map.of("message", "当前密码错误"));
            }

            // 更新密码
            user.setPassword(passwordEncoder.encode(newPassword));
            userRepository.save(user);

            return ResponseEntity.ok(Map.of("message", "密码修改成功"));
        } catch (Exception e) {
            logger.error("修改密码失败", e);
            return ResponseEntity.badRequest().body(Map.of("message", "修改密码失败: " + e.getMessage()));
        }
    }

    // 注销账户
    @DeleteMapping("/deactivate")
    public ResponseEntity<?> deactivateAccount(@RequestBody Map<String, String> request) {
        try {
            // 获取当前用户
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            String password = request.get("password");

            // 验证密码
            User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

            if (!passwordEncoder.matches(password, user.getPassword())) {
                return ResponseEntity.badRequest().body(Map.of("message", "密码错误"));
            }

            // 删除用户
            userRepository.delete(user);

            return ResponseEntity.ok(Map.of("message", "账户已注销"));
        } catch (Exception e) {
            logger.error("注销账户失败", e);
            return ResponseEntity.badRequest().body(Map.of("message", "注销账户失败: " + e.getMessage()));
        }
    }

    @PostMapping("/{userId}/follow")
    public ResponseEntity<?> followUser(@PathVariable Long userId, @AuthenticationPrincipal UserDetails userDetails) {
        User currentUser = userService.findByUsername(userDetails.getUsername())
            .orElseThrow(() -> new RuntimeException("当前用户不存在"));
        
        if (currentUser.getId().equals(userId)) {
            return ResponseEntity.badRequest().body(Map.of("message", "不能关注自己"));
        }

        User targetUser = userService.findById(userId)
            .orElseThrow(() -> new RuntimeException("目标用户不存在"));

        userService.followUser(currentUser, targetUser);
        return ResponseEntity.ok().body(Map.of("message", "关注成功"));
    }

    @PostMapping("/{userId}/unfollow")
    public ResponseEntity<?> unfollowUser(@PathVariable Long userId, @AuthenticationPrincipal UserDetails userDetails) {
        User currentUser = userService.findByUsername(userDetails.getUsername())
            .orElseThrow(() -> new RuntimeException("当前用户不存在"));

        User targetUser = userService.findById(userId)
            .orElseThrow(() -> new RuntimeException("目标用户不存在"));

        userService.unfollowUser(currentUser, targetUser);
        return ResponseEntity.ok().body(Map.of("message", "取消关注成功"));
    }

    @GetMapping("/{userId}/following")
    public ResponseEntity<?> checkFollowing(@PathVariable Long userId, @AuthenticationPrincipal UserDetails userDetails) {
        User currentUser = userService.findByUsername(userDetails.getUsername())
            .orElseThrow(() -> new RuntimeException("当前用户不存在"));

        boolean isFollowing = userService.isFollowing(currentUser, userId);
        return ResponseEntity.ok().body(Map.of("following", isFollowing));
    }

    @Transactional
    @DeleteMapping("/account")
    public ResponseEntity<?> deleteAccount(@RequestBody Map<String, String> request, @AuthenticationPrincipal UserDetails userDetails) {
        try {
            if (userDetails == null) {
                return ResponseEntity.status(401).body(Map.of("message", "请先登录"));
            }

            String password = request.get("password");
            if (password == null || password.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("message", "请输入密码"));
            }

            // 获取当前用户
            User user = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("用户不存在"));

            // 验证密码
            if (!passwordEncoder.matches(password, user.getPassword())) {
                return ResponseEntity.status(403).body(Map.of("message", "密码错误"));
            }

            // 在事务中删除用户的所有文章和用户账户
            articleRepository.deleteByAuthorId(user.getId());
            userRepository.delete(user);

            return ResponseEntity.ok(Map.of("message", "账户已成功注销"));
        } catch (Exception e) {
            logger.error("注销账户失败", e);
            throw new RuntimeException("注销账户失败: " + e.getMessage());
        }
    }
} 