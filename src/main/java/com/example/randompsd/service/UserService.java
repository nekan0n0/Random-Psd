package com.example.randompsd.service;

import com.example.randompsd.dto.UserLoginRequest;
import com.example.randompsd.dto.UserRegistrationRequest;
import com.example.randompsd.model.User;

import java.util.Optional;

public interface UserService {
    
    /**
     * 注册新用户
     * 
     * @param request 用户注册请求
     * @return 注册成功的用户
     */
    User registerUser(UserRegistrationRequest request);
    
    /**
     * 用户登录
     * 
     * @param request 用户登录请求
     * @return 登录成功返回用户信息，失败返回空
     */
    Optional<User> loginUser(UserLoginRequest request);
    
    /**
     * 通过用户名查找用户
     * 
     * @param username 用户名
     * @return 用户信息（如果存在）
     */
    Optional<User> findByUsername(String username);
    
    /**
     * 检查用户名是否已存在
     * 
     * @param username 用户名
     * @return 如果存在返回true，否则返回false
     */
    boolean isUsernameExists(String username);
    
    /**
     * 检查电子邮箱是否已存在
     * 
     * @param email 电子邮箱
     * @return 如果存在返回true，否则返回false
     */
    boolean isEmailExists(String email);
}
