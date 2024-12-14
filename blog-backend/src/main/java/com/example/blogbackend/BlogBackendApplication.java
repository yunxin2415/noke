package com.example.blogbackend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@EntityScan("com.example.blogbackend.entity")
@EnableJpaRepositories("com.example.blogbackend.repository")
@ComponentScan(basePackages = "com.example.blogbackend")
@EnableTransactionManagement
public class BlogBackendApplication {
    public static void main(String[] args) {
        SpringApplication.run(BlogBackendApplication.class, args);
    }
} 