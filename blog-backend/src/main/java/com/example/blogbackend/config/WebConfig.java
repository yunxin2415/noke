package com.example.blogbackend.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    
    private static final Logger logger = LoggerFactory.getLogger(WebConfig.class);

    @Value("${upload.path}")
    private String uploadPath;
    
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        logger.info("开始配置 CORS");
        registry.addMapping("/**")
            .allowedOrigins("http://localhost:3000", "http://127.0.0.1:3000")
            .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS", "HEAD", "PATCH")
            .allowedHeaders("*")
            .exposedHeaders("Authorization", "Content-Disposition")
            .allowCredentials(true)
            .maxAge(3600);
        
        logger.info("CORS 配置完成");
    }
    
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 配置上传文件的访问路径
        String uploadLocation = "file:" + uploadPath + "/";
        logger.info("配置静态资源路径: {}", uploadLocation);
        
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(uploadLocation)
                .setCachePeriod(0)
                .resourceChain(false);
        
        logger.info("静态资源映射配置已加载，上传路径: {}", uploadPath);
    }
    
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new HandlerInterceptor() {
            @Override
            public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
                if (request.getMethod().equals("OPTIONS")) {
                    logger.debug("处理预检请求: {} {}", request.getMethod(), request.getRequestURI());
                    return true;
                }
                
                logger.debug("收到请求: {} {} - Headers: {}", 
                    request.getMethod(), 
                    request.getRequestURI(),
                    getHeadersInfo(request));
                return true;
            }
        }).excludePathPatterns("/uploads/**");
    }
    
    private Map<String, String> getHeadersInfo(HttpServletRequest request) {
        Map<String, String> headers = new HashMap<>();
        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String key = headerNames.nextElement();
            String value = request.getHeader(key);
            headers.put(key, value);
        }
        return headers;
    }
} 