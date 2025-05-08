package com.example.randompsd.service;

import com.example.randompsd.dto.PasswordRequest;
import com.example.randompsd.model.PasswordHistory;
import com.example.randompsd.model.User;

import java.util.List;
import java.util.Map;

public interface PasswordService {
    String generatePassword(int length, boolean useUpper, boolean useDigits, boolean useSpecial);
    String generatePassword(PasswordRequest request);
    
    /**
     * 保存密码历史记录
     * @param password 生成的密码
     * @param request 生成密码的请求参数
     * @return 密码历史记录
     */
    PasswordHistory savePasswordHistory(String password, PasswordRequest request);
    
    /**
     * 保存密码历史记录（与用户关联）
     * @param password 生成的密码
     * @param request 生成密码的请求参数
     * @param user 关联的用户
     * @return 密码历史记录
     */
    PasswordHistory savePasswordHistory(String password, PasswordRequest request, User user);
    
    /**
     * 获取最近生成的密码（非用户特定）
     * @return 密码历史记录列表
     */
    List<PasswordHistory> getRecentPasswords();
    
    /**
     * 获取特定用户的密码历史记录
     * @param user 用户
     * @return 用户的密码历史记录列表
     */
    List<PasswordHistory> getPasswordHistoryByUser(User user);
    
    /**
     * 评估密码强度
     * @param password 待评估的密码
     * @return 包含强度评分和评估信息的Map
     */
    Map<String, Object> evaluatePasswordStrength(String password);
}
