package com.example.blogbackend.service;

import org.springframework.web.multipart.MultipartFile;
import java.util.List;

public interface FileService {
    /**
     * 上传图片文件
     * @param files 图片文件数组
     * @return 上传后的文件URL列表
     */
    List<String> uploadImages(MultipartFile[] files);
    
    /**
     * 存储单个文件
     * @param file 要存储的文件
     * @return 存储后的文件URL
     */
    String storeFile(MultipartFile file);
} 