package com.example.randompsd.controller;

import com.example.randompsd.dto.PasswordRequest;
import com.example.randompsd.model.PasswordHistory;
import com.example.randompsd.model.User;
import com.example.randompsd.security.JwtUtils;
import com.example.randompsd.service.PasswordService;
import com.example.randompsd.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")  // 允许跨域请求
public class PasswordController {

    @Autowired
    private PasswordService passwordService;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private JwtUtils jwtUtils;

    @PostMapping("/generate")
    public ResponseEntity<?> generatePassword(@RequestBody PasswordRequest request) {
        try {
            String password = passwordService.generatePassword(request);
            
            // 获取当前用户（如果已登录）
            Optional<User> currentUser = getCurrentUser();
            
            // 保存密码历史记录（如果已登录，则关联用户）
            if (currentUser.isPresent()) {
                passwordService.savePasswordHistory(password, request, currentUser.get());
            } else {
                passwordService.savePasswordHistory(password, request);
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("password", password);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
    
    @GetMapping("/history")
    public ResponseEntity<?> getPasswordHistory() {
        try {
            // 获取当前用户（如果已登录）
            Optional<User> currentUser = getCurrentUser();
            
            List<PasswordHistory> history;
            if (currentUser.isPresent()) {
                // 如果已登录，返回用户的密码历史
                history = passwordService.getPasswordHistoryByUser(currentUser.get());
            } else {
                // 如果未登录，返回公共历史记录
                history = passwordService.getRecentPasswords();
            }
            
            return ResponseEntity.ok(history);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
    
    @PostMapping("/evaluate")
    public ResponseEntity<?> evaluatePasswordStrength(@RequestBody Map<String, String> request) {
        try {
            String password = request.get("password");
            if (password == null || password.isEmpty()) {
                throw new IllegalArgumentException("密码不能为空");
            }
            
            Map<String, Object> evaluation = passwordService.evaluatePasswordStrength(password);
            return ResponseEntity.ok(evaluation);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
    
    /**
     * 获取当前登录用户
     * @return 当前用户（如果已登录）
     */
    private Optional<User> getCurrentUser() {
        // 获取当前的认证信息
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication != null && authentication.isAuthenticated() && 
                authentication.getPrincipal() instanceof UserDetails) {
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            return userService.findByUsername(userDetails.getUsername());
        }
        
        return Optional.empty();
    }
}
