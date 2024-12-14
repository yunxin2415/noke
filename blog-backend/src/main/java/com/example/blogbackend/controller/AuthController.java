package com.example.blogbackend.controller;

import com.example.blogbackend.entity.User;
import com.example.blogbackend.repository.UserRepository;
import com.example.blogbackend.service.UserService;
import com.example.blogbackend.security.JwtTokenProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = {"http://localhost:3000", "http://127.0.0.1:3000"}, allowCredentials = "true")
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);
    private final Map<String, CaptchaInfo> captchaStore = new ConcurrentHashMap<>(); // 使用线程安全的Map
    private final Random random = new Random();
    private static final long CAPTCHA_EXPIRE_MINUTES = 5; // 验证码5分钟过期

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserService userService;

    @Autowired
    private JwtTokenProvider tokenProvider;

    // 存储验证码的内部类
    private static class CaptchaInfo {
        String code;
        LocalDateTime createTime;

        CaptchaInfo(String code) {
            this.code = code;
            this.createTime = LocalDateTime.now();
        }

        boolean isExpired() {
            return LocalDateTime.now().isAfter(createTime.plusMinutes(CAPTCHA_EXPIRE_MINUTES));
        }
    }

    // 生成验证码
    @GetMapping("/captcha")
    public ResponseEntity<?> generateCaptcha() {
        try {
            // 生成随机验证码
            String captcha = generateRandomCode();
            String sessionId = generateSessionId();
            
            logger.debug("生成新验证码 - sessionId: {}", sessionId);
            
            // 存储验证码
            captchaStore.put(sessionId, new CaptchaInfo(captcha));
            
            // 生成验证码图片
            BufferedImage image = generateCaptchaImage(captcha);
            
            // 将图片转换为字节数组
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(image, "png", baos);
            byte[] imageBytes = baos.toByteArray();
            
            // 设置响应头
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.IMAGE_PNG);
            headers.set("X-Captcha-ID", sessionId);
            headers.setCacheControl("no-cache, no-store, must-revalidate");
            headers.setPragma("no-cache");
            headers.setExpires(0);
            
            // 清理过期的验证码
            cleanExpiredCaptchas();
            
            return ResponseEntity
                .ok()
                .headers(headers)
                .body(imageBytes);
        } catch (Exception e) {
            logger.error("生成验证码失败", e);
            return ResponseEntity.badRequest().body(Map.of("message", "生成验证码失败"));
        }
    }

    // 注册
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Map<String, String> registerRequest, @RequestHeader("X-Captcha-ID") String captchaId) {
        try {
            String username = registerRequest.get("username");
            String password = registerRequest.get("password");
            String email = registerRequest.get("email");
            String captcha = registerRequest.get("captcha");

            logger.debug("收到注册请求 - username: {}, email: {}, captchaId: {}", username, email, captchaId);

            // 验证参数
            if (username == null || username.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("message", "用户名不能为空"));
            }
            if (password == null || password.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("message", "密码不能为空"));
            }
            if (email == null || email.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("message", "邮箱不能为空"));
            }
            if (captcha == null || captcha.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("message", "验证码不能为空"));
            }

            // 验证验证码
            CaptchaInfo storedCaptchaInfo = captchaStore.get(captchaId);
            if (storedCaptchaInfo == null) {
                logger.warn("验证码不存在 - captchaId: {}", captchaId);
                return ResponseEntity.badRequest().body(Map.of("message", "验证码已过期，请重新获取"));
            }

            if (storedCaptchaInfo.isExpired()) {
                logger.warn("验证码已过期 - captchaId: {}", captchaId);
                captchaStore.remove(captchaId);
                return ResponseEntity.badRequest().body(Map.of("message", "验证码已过期，请重新获取"));
            }

            if (!storedCaptchaInfo.code.equalsIgnoreCase(captcha.trim())) {
                logger.warn("验证码错误 - captchaId: {}, expected: {}, actual: {}", 
                    captchaId, storedCaptchaInfo.code, captcha);
                return ResponseEntity.badRequest().body(Map.of("message", "验证码错误"));
            }

            // 验证用户名是否已存在
            if (userRepository.existsByUsername(username)) {
                logger.warn("用户名已存在 - username: {}", username);
                return ResponseEntity.status(409).body(Map.of("message", "用户名已被使用"));
            }

            // 验证邮箱是否已存在
            if (userRepository.existsByEmail(email)) {
                logger.warn("邮箱已存在 - email: {}", email);
                return ResponseEntity.status(409).body(Map.of("message", "邮箱已被使用"));
            }

            // 创建新用户
            User user = new User();
            user.setUsername(username.trim());
            user.setPassword(passwordEncoder.encode(password));
            user.setEmail(email.trim());
            user.setRole("ROLE_USER");
            user.setCreatedAt(LocalDateTime.now());
            user.setUpdatedAt(LocalDateTime.now());

            User savedUser = userRepository.save(user);
            savedUser.setPassword(null); // 移除密码

            // 删除已使用的验证码
            captchaStore.remove(captchaId);
            
            logger.info("用户注册成功 - username: {}, email: {}", username, email);

            return ResponseEntity.ok(Map.of(
                "message", "注册成功",
                "user", savedUser
            ));
        } catch (Exception e) {
            logger.error("注册失败", e);
            return ResponseEntity.badRequest().body(Map.of("message", "注册失败: " + e.getMessage()));
        }
    }

    // 清理过期的验证码
    private void cleanExpiredCaptchas() {
        captchaStore.entrySet().removeIf(entry -> entry.getValue().isExpired());
    }

    // 生成随机验证码
    private String generateRandomCode() {
        StringBuilder code = new StringBuilder();
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        for (int i = 0; i < 4; i++) {
            code.append(chars.charAt(random.nextInt(chars.length())));
        }
        return code.toString();
    }

    // 生成会话ID
    private String generateSessionId() {
        return java.util.UUID.randomUUID().toString();
    }

    // 生成验证码图片
    private BufferedImage generateCaptchaImage(String code) {
        int width = 120;
        int height = 40;
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();

        // 设置背景
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, width, height);

        // 绘制干扰线
        g.setColor(Color.LIGHT_GRAY);
        for (int i = 0; i < 20; i++) {
            int x1 = random.nextInt(width);
            int y1 = random.nextInt(height);
            int x2 = random.nextInt(width);
            int y2 = random.nextInt(height);
            g.drawLine(x1, y1, x2, y2);
        }

        // 绘制验证码
        g.setColor(Color.BLUE);
        g.setFont(new Font("Arial", Font.BOLD, 24));
        for (int i = 0; i < code.length(); i++) {
            g.drawString(String.valueOf(code.charAt(i)), 25 * i + 20, 25);
        }

        g.dispose();
        return image;
    }

    // 登录
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> loginRequest) {
        try {
            String username = loginRequest.get("username");
            String password = loginRequest.get("password");

            logger.debug("收到登录请求 - username: {}", username);

            // 验证参数
            if (username == null || username.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("message", "用户名不能为空"));
            }
            if (password == null || password.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("message", "密码不能为空"));
            }

            // 验证用户名和密码
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(username, password)
            );

            // 设置认证信息
            SecurityContextHolder.getContext().setAuthentication(authentication);

            // 生成JWT令牌
            String jwt = tokenProvider.generateToken(authentication);

            // 获取用户信息
            User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
            user.setPassword(null); // 移除密码

            logger.info("用户登录成功 - username: {}", username);

            // 构建响应
            Map<String, Object> response = new HashMap<>();
            response.put("token", jwt);
            response.put("user", user);

            return ResponseEntity.ok(response);

        } catch (BadCredentialsException e) {
            logger.warn("登录失败 - username: {}, 原因: 用户名或密码错误", loginRequest.get("username"));
            return ResponseEntity.badRequest().body(Map.of("message", "用户名或密码错误"));
        } catch (Exception e) {
            logger.error("登录失败: {} - {}", e.getClass().getSimpleName(), e.getMessage(), e);
            return ResponseEntity.badRequest().body(Map.of(
                "message", "系统错误，请稍后重试",
                "path", "/auth/login",
                "error", e.getClass().getSimpleName()
            ));
        }
    }

    // 检查用户名是否存在
    @GetMapping("/check/{username}")
    public ResponseEntity<?> checkUser(@PathVariable String username) {
        try {
            boolean exists = userRepository.existsByUsername(username);
            User user = null;
            if (exists) {
                user = userRepository.findByUsername(username).get();
                user.setPassword("***"); // 隐藏密码
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("exists", exists);
            response.put("user", user);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("检查用户名失败", e);
            return ResponseEntity.badRequest().body(Map.of("message", "检查用户名失败: " + e.getMessage()));
        }
    }
} 