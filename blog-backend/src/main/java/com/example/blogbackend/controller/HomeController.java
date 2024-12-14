package com.example.blogbackend.controller;

import com.example.blogbackend.repository.ArticleRepository;
import com.example.blogbackend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class HomeController {

    @Autowired
    private ArticleRepository articleRepository;

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/home")
    public ResponseEntity<?> getHomeData() {
        try {
            Map<String, Object> homeData = new HashMap<>();
            
            // 获取文章总数
            long totalArticles = articleRepository.count();
            homeData.put("totalArticles", totalArticles);
            
            // 获取用户总数
            long totalUsers = userRepository.count();
            homeData.put("totalUsers", totalUsers);
            
            // 添加其他统计数据
            homeData.put("status", "success");
            homeData.put("message", "首页数据获取成功");
            
            return ResponseEntity.ok(homeData);
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("message", "获取首页数据失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    @GetMapping("")
    public ResponseEntity<?> checkApiStatus() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", "API服务正常运行中");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/error")
    public ResponseEntity<?> handleError() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "error");
        response.put("message", "发生错误");
        return ResponseEntity.badRequest().body(response);
    }
} 