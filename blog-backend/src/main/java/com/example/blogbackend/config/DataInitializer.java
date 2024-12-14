package com.example.blogbackend.config;

import com.example.blogbackend.entity.User;
import com.example.blogbackend.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(DataInitializer.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        try {
            // 创建管理员账号
            if (!userRepository.existsByUsername("admin")) {
                User admin = new User();
                admin.setUsername("admin");
                admin.setPassword(passwordEncoder.encode("admin123"));
                admin.setEmail("admin@example.com");
                admin.setRole("ROLE_ADMIN");
                admin.setCreatedAt(LocalDateTime.now());
                admin.setUpdatedAt(LocalDateTime.now());
                userRepository.save(admin);
                logger.info("管理员账号创建成功: {}", admin.getUsername());
            } else {
                logger.info("管理员账号已存在，跳过创建");
            }

            // 创建测试账号
            if (!userRepository.existsByUsername("test")) {
                User test = new User();
                test.setUsername("test");
                test.setPassword(passwordEncoder.encode("test123"));
                test.setEmail("test@example.com");
                test.setRole("ROLE_USER");
                test.setCreatedAt(LocalDateTime.now());
                test.setUpdatedAt(LocalDateTime.now());
                userRepository.save(test);
                logger.info("测试账号创建成功: {}", test.getUsername());
            } else {
                logger.info("测试账号已存在，跳过创建");
            }
        } catch (Exception e) {
            logger.error("创建用户时发生错误: {}", e.getMessage(), e);
            throw e;
        }
    }
} 