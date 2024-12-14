package com.example.blogbackend.service.impl;

import com.example.blogbackend.service.FileService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class FileServiceImpl implements FileService {
    private static final Logger logger = LoggerFactory.getLogger(FileServiceImpl.class);
    private static final Set<String> ALLOWED_EXTENSIONS = new HashSet<>(Arrays.asList(
            ".jpg", ".jpeg", ".png", ".gif", ".webp"
    ));
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB

    @Value("${upload.path}")
    private String uploadPath;

    @Override
    public List<String> uploadImages(MultipartFile[] files) {
        List<String> urls = new ArrayList<>();
        
        try {
            // 获取当前日期
            LocalDate today = LocalDate.now();
            String dateFolder = today.format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
            
            // 创建基础上传目录
            String basePath = Paths.get(uploadPath, "images", dateFolder).toString();
            File baseDir = new File(basePath);
            if (!baseDir.exists() && !baseDir.mkdirs()) {
                logger.error("创建上传目录失败: {}", basePath);
                throw new IOException("无法创建上传目录");
            }

            for (MultipartFile file : files) {
                if (!isValidFile(file)) {
                    continue;
                }

                String originalFilename = file.getOriginalFilename();
                String extension = getFileExtension(originalFilename);
                
                // 生成文件名：时间戳 + UUID 后8位 + 原始扩展名
                String filename = System.currentTimeMillis() + 
                                "_" + 
                                UUID.randomUUID().toString().substring(0, 8) + 
                                extension;
                
                // 完整的文件路径
                Path filePath = Paths.get(basePath, filename);
                
                // 保存文件
                logger.info("保存文件到: {}", filePath);
                Files.write(filePath, file.getBytes());
                
                // 生成访问URL（使用正斜杠，确保URL格式正确）
                String fileUrl = String.format("/uploads/images/%s/%s", dateFolder, filename);
                urls.add(fileUrl);
                logger.info("文件上传成功: {}", fileUrl);
            }
            
        } catch (IOException e) {
            logger.error("文件上传失败", e);
            throw new RuntimeException("文件上传失败: " + e.getMessage());
        }

        return urls;
    }

    private boolean isValidFile(MultipartFile file) {
        if (file.isEmpty()) {
            logger.warn("跳过空文件");
            return false;
        }

        // 检查文件大小
        if (file.getSize() > MAX_FILE_SIZE) {
            logger.warn("文件过大: {} bytes", file.getSize());
            return false;
        }

        // 检查文件类型
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            logger.warn("不支持的文件类型: {}", contentType);
            return false;
        }

        // 检查文件扩展名
        String extension = getFileExtension(file.getOriginalFilename());
        if (!ALLOWED_EXTENSIONS.contains(extension.toLowerCase())) {
            logger.warn("不支持的文件扩展名: {}", extension);
            return false;
        }

        return true;
    }

    private String getFileExtension(String filename) {
        if (filename == null) {
            return "";
        }
        int lastDotIndex = filename.lastIndexOf(".");
        if (lastDotIndex == -1) {
            return "";
        }
        return filename.substring(lastDotIndex).toLowerCase();
    }

    @Override
    public String storeFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new RuntimeException("文件为空");
        }

        try {
            // 获取当前日期
            LocalDate today = LocalDate.now();
            String dateFolder = today.format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
            
            // 创建基础上传目录
            String basePath = Paths.get(uploadPath, "files", dateFolder).toString();
            File baseDir = new File(basePath);
            if (!baseDir.exists() && !baseDir.mkdirs()) {
                logger.error("创建上传目录失败: {}", basePath);
                throw new IOException("无法创建上传目录");
            }

            String originalFilename = file.getOriginalFilename();
            String extension = getFileExtension(originalFilename);
            
            // 生成文件名：时间戳 + UUID 后8位 + 原始扩展名
            String filename = System.currentTimeMillis() + 
                            "_" + 
                            UUID.randomUUID().toString().substring(0, 8) + 
                            extension;
            
            // 完整的文件路径
            Path filePath = Paths.get(basePath, filename);
            
            // 保存文件
            logger.info("保存文件到: {}", filePath);
            Files.write(filePath, file.getBytes());
            
            // 生成访问URL（使用正斜杠，确保URL格式正确）
            return String.format("/uploads/files/%s/%s", dateFolder, filename);
            
        } catch (IOException e) {
            logger.error("文件上传失败", e);
            throw new RuntimeException("文件上传失败: " + e.getMessage());
        }
    }
} 