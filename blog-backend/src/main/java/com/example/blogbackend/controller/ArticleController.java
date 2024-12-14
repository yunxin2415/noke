package com.example.blogbackend.controller;

import com.example.blogbackend.entity.Article;
import com.example.blogbackend.entity.User;
import com.example.blogbackend.service.ArticleService;
import com.example.blogbackend.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/articles")
@CrossOrigin(origins = {"http://localhost:3000", "http://127.0.0.1:3000"}, allowCredentials = "true")
public class ArticleController {
    private static final Logger logger = LoggerFactory.getLogger(ArticleController.class);

    @Autowired
    private ArticleService articleService;

    @Autowired
    private UserService userService;

    @GetMapping("/user")
    public ResponseEntity<?> getUserArticles(@AuthenticationPrincipal UserDetails userDetails) {
        try {
            if (userDetails == null) {
                logger.warn("未登录用户尝试获取个人文章列表");
                return ResponseEntity.status(401).body("请先登录");
            }

            logger.info("开始获取用户文章列表 - username: {}", userDetails.getUsername());
            
            User currentUser = userService.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("用户不存在"));
            
            logger.info("当前用户信息 - id: {}, username: {}, role: {}", 
                currentUser.getId(), 
                currentUser.getUsername(),
                currentUser.getRole());
            
            // 直接使用用户ID查询
            List<Article> articles = articleService.findByAuthorId(currentUser.getId());
            logger.info("数据库查询结果 - 用户ID: {}, 文章数量: {}", currentUser.getId(), articles.size());
            
            if (articles.isEmpty()) {
                logger.info("用户暂无文章");
                return ResponseEntity.ok(Collections.emptyList());
            }
            
            // 打印每篇文章的基本信息
            articles.forEach(article -> {
                logger.info("文章信息 - id: {}, title: {}, authorId: {}, category: {}", 
                    article.getId(), 
                    article.getTitle(),
                    article.getAuthor() != null ? article.getAuthor().getId() : "null",
                    article.getCategory());
            });
            
            List<Map<String, Object>> response = articles.stream()
                .map(article -> {
                    Map<String, Object> map = convertArticleToMap(article);
                    if (map != null) {
                        logger.info("成功转换文章 - id: {}", article.getId());
                    } else {
                        logger.warn("文章转换失败 - id: {}", article.getId());
                    }
                    return map;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
            
            logger.info("最终响应数据 - 文章数量: {}", response.size());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("获取用户文章列表失败: {}", e.getMessage(), e);
            Map<String, String> error = new HashMap<>();
            error.put("message", "获取文章列表失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @GetMapping
    public ResponseEntity<?> getArticles(
        @RequestParam(required = false, defaultValue = "") String type,
        @RequestParam(defaultValue = "1") int page,
        @RequestParam(defaultValue = "10") int size,
        @AuthenticationPrincipal UserDetails userDetails
    ) {
        try {
            logger.info("开始处理获取文章列表请求");
            logger.info("请求参数 - type: '{}', page: {}, size: {}", type, page, size);
            logger.info("认证用户: {}", userDetails != null ? userDetails.getUsername() : "未登录");
            
            // 验证分页参数
            if (page < 1) {
                Map<String, Object> error = new HashMap<>();
                error.put("code", "INVALID_PAGE");
                error.put("message", "页码不能小于1");
                logger.error("参数验证失败: {}", error);
                return ResponseEntity.badRequest().body(error);
            }
            if (size < 1 || size > 100) {
                Map<String, Object> error = new HashMap<>();
                error.put("code", "INVALID_SIZE");
                error.put("message", "每页大小必须在1到100之间");
                logger.error("参数验证失败: {}", error);
                return ResponseEntity.badRequest().body(error);
            }
            
            // 创建分页对象
            Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createdAt"));
            Page<Article> articlePage;
            
            try {
                // 获取当前用户（如果已登录）
                User currentUser = null;
                if (userDetails != null) {
                    try {
                        currentUser = userService.findByUsername(userDetails.getUsername())
                            .orElse(null);
                        if (currentUser != null) {
                            logger.info("当前用户ID: {}", currentUser.getId());
                        }
                    } catch (Exception e) {
                        logger.warn("获取用户信息失败: {}", e.getMessage());
                    }
                }
                
                // 根据分类获取文章
                switch (type.toLowerCase()) {
                    case "tech":
                        logger.info("获取技术分类文章");
                        articlePage = articleService.findAllVisibleArticlesByCategory(currentUser, "tech", pageable);
                        break;
                    case "life":
                        logger.info("获取生活分类文章");
                        articlePage = articleService.findAllVisibleArticlesByCategory(currentUser, "life", pageable);
                        break;
                    default:
                        logger.info("获取所有文章");
                        articlePage = articleService.findAllVisibleArticles(currentUser, pageable);
                }
                
                logger.info("成功获取到文章, 总数: {}, 当前页文章数: {}", 
                    articlePage.getTotalElements(), 
                    articlePage.getContent().size());
                
                // 构建响应
                Map<String, Object> response = new HashMap<>();
                response.put("content", articlePage.getContent().stream()
                    .map(this::convertArticleToMap)
                    .filter(map -> map != null)
                    .collect(Collectors.toList()));
                response.put("totalElements", articlePage.getTotalElements());
                response.put("totalPages", articlePage.getTotalPages());
                response.put("currentPage", articlePage.getNumber() + 1);
                response.put("size", articlePage.getSize());
                response.put("first", articlePage.isFirst());
                response.put("last", articlePage.isLast());
                
                logger.info("响应数据准备完成");
                return ResponseEntity.ok(response);
                
            } catch (Exception e) {
                logger.error("处理文章列表请求时发生错误: {}", e.getMessage(), e);
                Map<String, Object> error = new HashMap<>();
                error.put("code", "ARTICLE_LIST_ERROR");
                error.put("message", "获取文章列表失败: " + e.getMessage());
                error.put("details", e.getClass().getSimpleName());
                return ResponseEntity.badRequest().body(error);
            }
        } catch (Exception e) {
            logger.error("处理请求时发生未预期的错误: {}", e.getMessage(), e);
            Map<String, Object> error = new HashMap<>();
            error.put("code", "INTERNAL_ERROR");
            error.put("message", "服务器内部错误: " + e.getMessage());
            error.put("details", e.getClass().getSimpleName());
            return ResponseEntity.badRequest().body(error);
        }
    }

    private Map<String, Object> convertArticleToMap(Article article) {
        if (article == null) {
            logger.warn("尝试转换空文章对象");
            return null;
        }

        try {
            Map<String, Object> articleMap = new HashMap<>();
            articleMap.put("id", article.getId());
            articleMap.put("title", article.getTitle());
            articleMap.put("content", article.getContent());
            articleMap.put("category", article.getCategory());
            articleMap.put("tags", article.getTags());
            articleMap.put("createdAt", article.getCreatedAt());
            articleMap.put("updatedAt", article.getUpdatedAt());
            articleMap.put("isPrivate", article.isPrivate());
            articleMap.put("isDownloadable", article.isDownloadable());

            // 处理作者信息
            Map<String, Object> author = new HashMap<>();
            if (article.getAuthor() != null) {
                author.put("id", article.getAuthor().getId());
                author.put("username", article.getAuthor().getUsername());
                author.put("avatar", article.getAuthor().getAvatar());
            } else {
                author.put("id", null);
                author.put("username", "未知作者");
                author.put("avatar", null);
            }
            articleMap.put("author", author);

            return articleMap;
        } catch (Exception e) {
            logger.error("转换文章数据失败: {}", article.getId(), e);
            return null;
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getArticle(@PathVariable Long id, @AuthenticationPrincipal UserDetails userDetails) {
        try {
            Article article = articleService.findById(id)
                .orElseThrow(() -> new RuntimeException("文章不存在"));

            // 如果文章是私密的，检查访问权限
            if (article.isPrivate()) {
                // 如果用户未登录，拒绝访问
                if (userDetails == null) {
                    return ResponseEntity.status(403).body("该文章已设为私密，无权访问");
                }

                // 获取当前用户
                User currentUser = userService.findByUsername(userDetails.getUsername())
                    .orElseThrow(() -> new RuntimeException("用户不存在"));

                // 如果不是作者本人，拒绝访问
                if (!article.getAuthor().getId().equals(currentUser.getId())) {
                    return ResponseEntity.status(403).body("该文章已设为私密，无权访问");
                }
            }

            // 构建响应数据
            Map<String, Object> response = new HashMap<>();
            response.put("id", article.getId());
            response.put("title", article.getTitle());
            response.put("content", article.getContent());
            response.put("category", article.getCategory());
            response.put("tags", article.getTags());
            response.put("createdAt", article.getCreatedAt());
            response.put("updatedAt", article.getUpdatedAt());
            response.put("isPrivate", article.isPrivate());
            response.put("isDownloadable", article.isDownloadable());
            
            // 添加作者信息
            Map<String, Object> author = new HashMap<>();
            author.put("id", article.getAuthor().getId());
            author.put("username", article.getAuthor().getUsername());
            author.put("avatar", article.getAuthor().getAvatar());
            response.put("author", author);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping
    public ResponseEntity<?> createArticle(@RequestBody Map<String, Object> request, @AuthenticationPrincipal UserDetails userDetails) {
        try {
            if (userDetails == null) {
                return ResponseEntity.status(401).body("请先登录");
            }

            User currentUser = userService.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("用户不存在"));

            Article article = new Article();
            article.setTitle((String) request.get("title"));
            article.setContent((String) request.get("content"));
            article.setCategory((String) request.getOrDefault("category", "默认分类"));
            article.setAuthor(currentUser);

            // 设置文章的可见性和下载权限
            article.setPrivate((Boolean) request.getOrDefault("isPrivate", false));
            article.setDownloadable((Boolean) request.getOrDefault("isDownloadable", true));

            // 设置标签
            if (request.containsKey("tags")) {
                article.setTags((String) request.get("tags"));
            }

            article = articleService.save(article);

            // 构建响应
            Map<String, Object> response = new HashMap<>();
            response.put("id", article.getId());
            response.put("title", article.getTitle());
            response.put("content", article.getContent());
            response.put("category", article.getCategory());
            response.put("tags", article.getTags());
            response.put("createdAt", article.getCreatedAt());
            response.put("isPrivate", article.isPrivate());
            response.put("isDownloadable", article.isDownloadable());

            Map<String, Object> author = new HashMap<>();
            author.put("id", article.getAuthor().getId());
            author.put("username", article.getAuthor().getUsername());
            response.put("author", author);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateArticle(@PathVariable Long id, @RequestBody Map<String, Object> request, @AuthenticationPrincipal UserDetails userDetails) {
        try {
            if (userDetails == null) {
                return ResponseEntity.status(401).body("请先登录");
            }

            // 获取当前用户
            User currentUser = userService.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("用户不存在"));

            // 获取要���新的文章
            Article article = articleService.findById(id)
                .orElseThrow(() -> new RuntimeException("文章不存在"));

            // 检查是否是文章作者
            if (!article.getAuthor().getId().equals(currentUser.getId())) {
                return ResponseEntity.status(403).body("您没有权限修改此文章");
            }

            // 更新文章信息
            if (request.containsKey("category")) {
                article.setCategory((String) request.get("category"));
            }
            if (request.containsKey("tags")) {
                article.setTags((String) request.get("tags"));
            }
            if (request.containsKey("isPrivate")) {
                article.setPrivate((Boolean) request.get("isPrivate"));
            }
            if (request.containsKey("isDownloadable")) {
                article.setDownloadable((Boolean) request.get("isDownloadable"));
            }

            // 保存更新
            article = articleService.save(article);

            // 构建响应
            Map<String, Object> response = new HashMap<>();
            response.put("id", article.getId());
            response.put("title", article.getTitle());
            response.put("content", article.getContent());
            response.put("category", article.getCategory());
            response.put("tags", article.getTags());
            response.put("createdAt", article.getCreatedAt());
            response.put("updatedAt", article.getUpdatedAt());
            response.put("isPrivate", article.isPrivate());
            response.put("isDownloadable", article.isDownloadable());

            Map<String, Object> author = new HashMap<>();
            author.put("id", article.getAuthor().getId());
            author.put("username", article.getAuthor().getUsername());
            author.put("avatar", article.getAuthor().getAvatar());
            response.put("author", author);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", "更新文章失败: " + e.getMessage()));
        }
    }

    @GetMapping("/{id}/download")
    public ResponseEntity<?> downloadArticle(@PathVariable Long id, @AuthenticationPrincipal UserDetails userDetails) {
        try {
            // 获取文章
            Article article = articleService.findById(id)
                .orElseThrow(() -> new RuntimeException("文章不存在"));

            // 检查文章是否允许下载
            if (!article.isDownloadable()) {
                return ResponseEntity.status(403).body("该文章不允许下载");
            }

            // 如果文章是私密的，检查访问权限
            if (article.isPrivate()) {
                // 如果用户未登录，拒绝访问
                if (userDetails == null) {
                    return ResponseEntity.status(403).body("该文章已设为私密，无权访问");
                }

                // 获取当前用户
                User currentUser = userService.findByUsername(userDetails.getUsername())
                    .orElseThrow(() -> new RuntimeException("用户不存在"));

                // 如果不是作者本人，拒绝访问
                if (!article.getAuthor().getId().equals(currentUser.getId())) {
                    return ResponseEntity.status(403).body("该文章已设为私密，无权访问");
                }
            }

            // 准备文章内容
            StringBuilder markdownContent = new StringBuilder();
            markdownContent.append("# ").append(article.getTitle()).append("\n\n");
            markdownContent.append("作者: ").append(article.getAuthor().getUsername()).append("\n");
            markdownContent.append("发布时间: ").append(article.getCreatedAt()).append("\n");
            if (article.getTags() != null && !article.getTags().isEmpty()) {
                markdownContent.append("标签: ").append(article.getTags()).append("\n");
            }
            markdownContent.append("\n---\n\n");
            markdownContent.append(article.getContent());

            // 设置响应头
            String filename = article.getTitle().replaceAll("[\\\\/:*?\"<>|]", "_") + ".md";
            return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=\"" + filename + "\"")
                .header("Content-Type", "text/markdown; charset=UTF-8")
                .body(markdownContent.toString());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteArticle(@PathVariable Long id, @AuthenticationPrincipal UserDetails userDetails) {
        try {
            if (userDetails == null) {
                return ResponseEntity.status(401).body(Map.of("message", "请先登录"));
            }

            // 获取当前用户
            User currentUser = userService.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("用户不存在"));

            // 获取要删除的文章
            Article article = articleService.findById(id)
                .orElseThrow(() -> new RuntimeException("文章不存在"));

            // 检查是否是文章作者
            if (!article.getAuthor().getId().equals(currentUser.getId())) {
                return ResponseEntity.status(403).body(Map.of("message", "您没有权限删除此文章"));
            }

            // 删除文章
            articleService.deleteById(id);

            return ResponseEntity.ok(Map.of("message", "文章删除成功"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", "删除文章失败: " + e.getMessage()));
        }
    }
} 