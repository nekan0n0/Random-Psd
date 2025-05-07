package com.example.randompsd.controller;

import com.example.randompsd.dto.PasswordRequest;
import com.example.randompsd.model.PasswordHistory;
import com.example.randompsd.service.PasswordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")  // 允许跨域请求
public class PasswordController {

    @Autowired
    private PasswordService passwordService;

    @PostMapping("/generate")
    public ResponseEntity<?> generatePassword(@RequestBody PasswordRequest request) {
        try {
            String password = passwordService.generatePassword(request);
            
            // 保存密码历史记录
            passwordService.savePasswordHistory(password, request);
            
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
            List<PasswordHistory> history = passwordService.getRecentPasswords();
            return ResponseEntity.ok(history);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
}
