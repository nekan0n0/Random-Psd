package com.example.randompsd.controller;

import com.example.randompsd.dto.UserLoginRequest;
import com.example.randompsd.dto.UserRegistrationRequest;
import com.example.randompsd.model.User;
import com.example.randompsd.security.JwtUtils;
import com.example.randompsd.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtils jwtUtils;

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody UserRegistrationRequest request) {
        try {
            // 检查用户名是否已存在
            if (userService.isUsernameExists(request.getUsername())) {
                Map<String, Object> response = new HashMap<>();
                response.put("error", "用户名已被使用");
                return ResponseEntity.badRequest().body(response);
            }

            // 检查邮箱是否已存在
            if (userService.isEmailExists(request.getEmail())) {
                Map<String, Object> response = new HashMap<>();
                response.put("error", "电子邮箱已被使用");
                return ResponseEntity.badRequest().body(response);
            }

            // 注册新用户
            User user = userService.registerUser(request);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "用户注册成功");
            response.put("username", user.getUsername());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@Valid @RequestBody UserLoginRequest request) {
        try {
            Optional<User> userOpt = userService.loginUser(request);

            if (userOpt.isPresent()) {
                User user = userOpt.get();
                
                // 生成JWT令牌
                String jwt = jwtUtils.generateJwtToken(user);

                Map<String, Object> response = new HashMap<>();
                response.put("token", jwt);
                response.put("username", user.getUsername());
                return ResponseEntity.ok(response);
            } else {
                Map<String, Object> response = new HashMap<>();
                response.put("error", "用户名或密码不正确");
                return ResponseEntity.badRequest().body(response);
            }
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
}
