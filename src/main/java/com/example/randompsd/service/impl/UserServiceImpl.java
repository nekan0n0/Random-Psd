package com.example.randompsd.service.impl;

import com.example.randompsd.dto.UserLoginRequest;
import com.example.randompsd.dto.UserRegistrationRequest;
import com.example.randompsd.model.User;
import com.example.randompsd.repository.UserRepository;
import com.example.randompsd.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Override
    public User registerUser(UserRegistrationRequest request) {
        // 检查用户名和邮箱是否已存在
        if (isUsernameExists(request.getUsername())) {
            throw new RuntimeException("用户名已存在");
        }
        
        if (isEmailExists(request.getEmail())) {
            throw new RuntimeException("电子邮箱已存在");
        }
        
        // 创建用户对象
        User user = new User();
        user.setUsername(request.getUsername());
        // 加密密码
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setEmail(request.getEmail());
        
        // 保存用户
        return userRepository.save(user);
    }

    @Override
    public Optional<User> loginUser(UserLoginRequest request) {
        Optional<User> userOpt = userRepository.findByUsername(request.getUsername());
        
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            // 验证密码
            if (passwordEncoder.matches(request.getPassword(), user.getPassword())) {
                return Optional.of(user);
            }
        }
        
        return Optional.empty();
    }

    @Override
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    @Override
    public boolean isUsernameExists(String username) {
        return userRepository.existsByUsername(username);
    }

    @Override
    public boolean isEmailExists(String email) {
        return userRepository.existsByEmail(email);
    }
}
