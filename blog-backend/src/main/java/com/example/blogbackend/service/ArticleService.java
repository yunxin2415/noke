package com.example.blogbackend.service;

import com.example.blogbackend.entity.Article;
import com.example.blogbackend.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.Optional;

public interface ArticleService {
    List<Article> findAll();
    Page<Article> findAllPublicArticles(Pageable pageable);
    Page<Article> findAllPublicArticlesByCategory(String category, Pageable pageable);
    Optional<Article> findById(Long id);
    Article save(Article article);
    void deleteById(Long id);
    List<Article> findByAuthorId(Long authorId);
    List<Article> findByAuthorOrderByCreatedAtDesc(User author);
    List<Article> findByTag(String tag);
    Page<Article> findAllVisibleArticles(User user, Pageable pageable);
    Page<Article> findAllVisibleArticlesByCategory(User user, String category, Pageable pageable);
    Article getArticleById(Long id);
} 