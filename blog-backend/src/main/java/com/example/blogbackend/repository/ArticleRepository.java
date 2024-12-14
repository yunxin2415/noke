package com.example.blogbackend.repository;

import com.example.blogbackend.entity.Article;
import com.example.blogbackend.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ArticleRepository extends JpaRepository<Article, Long> {
    @Query("SELECT a FROM Article a WHERE a.author.id = :authorId ORDER BY a.createdAt DESC")
    List<Article> findByAuthorOrderByCreatedAtDesc(@Param("authorId") Long authorId);
    
    @Query("SELECT a FROM Article a WHERE a.author.id = :authorId ORDER BY a.createdAt DESC")
    List<Article> findByAuthorId(@Param("authorId") Long authorId);
    
    List<Article> findByTagsContaining(String tag);
    
    @Query("SELECT a FROM Article a WHERE a.isPrivate = false ORDER BY a.createdAt DESC")
    Page<Article> findByIsPrivateFalseOrderByCreatedAtDesc(Pageable pageable);
    
    @Query("SELECT a FROM Article a WHERE " +
           "CASE " +
           "  WHEN :userId IS NULL THEN a.isPrivate = false " +
           "  ELSE (a.isPrivate = false OR a.author.id = :userId) " +
           "END " +
           "ORDER BY a.createdAt DESC")
    Page<Article> findAllVisibleArticles(@Param("userId") Long userId, Pageable pageable);
    
    @Query("SELECT a FROM Article a WHERE " +
           "CASE " +
           "  WHEN :userId IS NULL THEN (a.isPrivate = false AND a.category = :category) " +
           "  ELSE ((a.isPrivate = false OR a.author.id = :userId) AND a.category = :category) " +
           "END " +
           "ORDER BY a.createdAt DESC")
    Page<Article> findAllVisibleArticlesByCategory(@Param("userId") Long userId, @Param("category") String category, Pageable pageable);
    
    // 根据作者ID删除所有文章
    void deleteByAuthorId(Long authorId);
} 