package com.example.blogbackend.service.impl;

import com.example.blogbackend.entity.Article;
import com.example.blogbackend.entity.User;
import com.example.blogbackend.repository.ArticleRepository;
import com.example.blogbackend.service.ArticleService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
public class ArticleServiceImpl implements ArticleService {
    private static final Logger logger = LoggerFactory.getLogger(ArticleServiceImpl.class);
    
    @Autowired
    private ArticleRepository articleRepository;

    @Override
    public List<Article> findAll() {
        return articleRepository.findAll();
    }

    @Override
    public Page<Article> findAllPublicArticles(Pageable pageable) {
        logger.info("查询所有公开文章, 页码: {}, 大小: {}", pageable.getPageNumber(), pageable.getPageSize());
        Page<Article> result = articleRepository.findByIsPrivateFalseOrderByCreatedAtDesc(pageable);
        logger.info("查询结果: 总数={}, 当前页文章数={}", result.getTotalElements(), result.getContent().size());
        return result;
    }

    @Override
    public Page<Article> findAllPublicArticlesByCategory(String category, Pageable pageable) {
        logger.debug("按分类查询公开文章, 分类: {}, 页码: {}, 大小: {}", 
            category, pageable.getPageNumber(), pageable.getPageSize());
        return articleRepository.findAllVisibleArticlesByCategory(null, category, pageable);
    }

    @Override
    public Optional<Article> findById(Long id) {
        return articleRepository.findById(id);
    }

    @Override
    public Article save(Article article) {
        return articleRepository.save(article);
    }

    @Override
    public void deleteById(Long id) {
        articleRepository.deleteById(id);
    }

    @Override
    public List<Article> findByAuthorId(Long authorId) {
        logger.debug("根据作者ID查询文章列表 - authorId: {}", authorId);
        return articleRepository.findByAuthorId(authorId);
    }

    @Override
    public List<Article> findByAuthorOrderByCreatedAtDesc(User author) {
        logger.debug("根据作者查询文章列表 - authorId: {}", author.getId());
        return articleRepository.findByAuthorOrderByCreatedAtDesc(author.getId());
    }

    @Override
    public List<Article> findByTag(String tag) {
        if (tag == null || tag.trim().isEmpty()) {
            return Collections.emptyList();
        }
        return articleRepository.findByTagsContaining(tag);
    }

    @Override
    public Page<Article> findAllVisibleArticles(User user, Pageable pageable) {
        logger.info("查询可见文章, 用户: {}, 页码: {}, 大小: {}", 
            user != null ? user.getUsername() : "未登录", 
            pageable.getPageNumber(), 
            pageable.getPageSize());
        
        Page<Article> result;
        if (user == null) {
            logger.info("未登录用户，只返回公开文章");
            result = findAllPublicArticles(pageable);
        } else {
            logger.info("已登录用户，返回公开文章和用户自己的私密文章");
            result = articleRepository.findAllVisibleArticles(user.getId(), pageable);
        }
        
        logger.info("查询结果: 总数={}, 当前页文章数={}", result.getTotalElements(), result.getContent().size());
        return result;
    }

    @Override
    public Page<Article> findAllVisibleArticlesByCategory(User user, String category, Pageable pageable) {
        logger.info("按分类查询可见文章, 用户: {}, 分类: {}, 页码: {}, 大小: {}", 
            user != null ? user.getUsername() : "未登录", 
            category,
            pageable.getPageNumber(), 
            pageable.getPageSize());
        
        Page<Article> result;
        if (user == null) {
            logger.info("未登录用户，只返回指定分类的公开文章");
            result = articleRepository.findAllVisibleArticlesByCategory(null, category, pageable);
        } else {
            logger.info("已登录用户，返回指定分类的公开文章和用户自己的私密文章");
            result = articleRepository.findAllVisibleArticlesByCategory(user.getId(), category, pageable);
        }
        
        logger.info("查询结果: 总数={}, 当前页文章数={}", result.getTotalElements(), result.getContent().size());
        return result;
    }

    @Override
    public Article getArticleById(Long id) {
        return articleRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("文章不存在"));
    }
} 