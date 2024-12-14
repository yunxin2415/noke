package com.example.blogbackend.controller;

import com.example.blogbackend.service.FileService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/upload")
@CrossOrigin(origins = {"http://localhost:3000", "http://127.0.0.1:3000"}, allowCredentials = "true")
public class UploadController {
    private static final Logger logger = LoggerFactory.getLogger(UploadController.class);

    @Value("${upload.path}")
    private String uploadPath;

    @Autowired
    private FileService fileService;

    @PostMapping(value = "/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadImage(@RequestParam("files[]") MultipartFile[] files) {
        try {
            logger.info("开始处理文件上传，文件数量: {}", files.length);
            List<String> urls = fileService.uploadImages(files);

            if (urls.isEmpty()) {
                return ResponseEntity.badRequest().body("没有文件被上传");
            }

            Map<String, Object> response = new HashMap<>();
            response.put("urls", urls);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("文件上传失败", e);
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/avatar")
    public ResponseEntity<?> uploadAvatar(@RequestParam("file") MultipartFile file) {
        try {
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                    "code", 400,
                    "message", "请选择要上传的文件"
                ));
            }

            // 检查文件类型
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                return ResponseEntity.badRequest().body(Map.of(
                    "code", 400,
                    "message", "只能上传图片文件"
                ));
            }

            // 检查文件大小（2MB）
            if (file.getSize() > 2 * 1024 * 1024) {
                return ResponseEntity.badRequest().body(Map.of(
                    "code", 400,
                    "message", "文件大小不能超过2MB"
                ));
            }

            // 获取当前日期
            String dateFolder = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
            
            // 创建基础上传目录
            String basePath = Paths.get(uploadPath, "avatars", dateFolder).toString();
            File baseDir = new File(basePath);
            if (!baseDir.exists() && !baseDir.mkdirs()) {
                logger.error("创建上传目录失败: {}", basePath);
                throw new IOException("无法创建上传目录");
            }

            String originalFilename = file.getOriginalFilename();
            String extension = originalFilename != null ? originalFilename.substring(originalFilename.lastIndexOf(".")).toLowerCase() : ".jpg";
            
            // 生成文件名：时间戳 + UUID 后8位 + 原始扩展名
            String filename = System.currentTimeMillis() + 
                            "_" + 
                            UUID.randomUUID().toString().substring(0, 8) + 
                            extension;
            
            // 完整的文件路径
            Path filePath = Paths.get(basePath, filename);
            
            // 保存文件
            logger.info("保存头像文件到: {}", filePath);
            Files.write(filePath, file.getBytes());
            
            // 生成访问URL（使用正斜杠，确保URL格式正确）
            String fileUrl = String.format("/uploads/avatars/%s/%s", dateFolder, filename);
            logger.info("头像文件URL: {}", fileUrl);

            return ResponseEntity.ok(Map.of(
                "code", 200,
                "message", "上传成功",
                "data", fileUrl
            ));

        } catch (Exception e) {
            logger.error("头像上传失败", e);
            return ResponseEntity.status(500).body(Map.of(
                "code", 500,
                "message", "头像上传失败：" + e.getMessage()
            ));
        }
    }
} 