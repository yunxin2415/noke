package com.example.blogbackend.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "articles")
@Data
@NoArgsConstructor
public class Article {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Column(length = 50)
    private String category = "默认分类";

    @Column(columnDefinition = "TEXT")
    private String tags;

    @Column(name = "content_file_path")
    private String contentFilePath;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "author_id", nullable = false)
    private User author;

    @Column(name = "is_private", nullable = false)
    private Boolean isPrivate = false;

    @Column(name = "is_downloadable", nullable = false)
    private Boolean isDownloadable = true;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Getter methods
    public boolean isPrivate() {
        return isPrivate != null && isPrivate;
    }

    public boolean isDownloadable() {
        return isDownloadable != null && isDownloadable;
    }

    // Setter methods
    public void setPrivate(boolean isPrivate) {
        this.isPrivate = isPrivate;
    }

    public void setDownloadable(boolean isDownloadable) {
        this.isDownloadable = isDownloadable;
    }

    // For backward compatibility
    public Boolean getIsPrivate() {
        return isPrivate;
    }

    public Boolean getIsDownloadable() {
        return isDownloadable;
    }

    public void setIsPrivate(Boolean isPrivate) {
        this.isPrivate = isPrivate;
    }

    public void setIsDownloadable(Boolean isDownloadable) {
        this.isDownloadable = isDownloadable;
    }
} 