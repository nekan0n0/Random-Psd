package com.example.randompsd.service.impl;

import com.example.randompsd.dto.PasswordRequest;
import com.example.randompsd.model.PasswordHistory;
import com.example.randompsd.model.User;
import com.example.randompsd.repository.PasswordHistoryRepository;
import com.example.randompsd.service.PasswordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
public class PasswordServiceImpl implements PasswordService {

    @Autowired
    private PasswordHistoryRepository passwordHistoryRepository;

    @Override
    public String generatePassword(int length, boolean useUpper, boolean useDigits, boolean useSpecial) {
        // 定义字符集
        String lower = "abcdefghijklmnopqrstuvwxyz";
        String upper = useUpper ? "ABCDEFGHIJKLMNOPQRSTUVWXYZ" : "";
        String digits = useDigits ? "0123456789" : "";
        String special = useSpecial ? "!@#$%^&*()_+-=[]{}|;:,.<>?" : "";
        
        String allChars = lower + upper + digits + special;
        
        if (allChars.isEmpty()) {
            allChars = lower; // 至少使用小写字母
        }
        
        SecureRandom random = new SecureRandom();
        final String chars = allChars; // 创建一个final变量供lambda表达式使用
        
        // 生成随机密码
        return IntStream.range(0, length)
                .map(i -> random.nextInt(chars.length()))
                .mapToObj(i -> chars.charAt(i))
                .collect(StringBuilder::new, StringBuilder::append, StringBuilder::append)
                .toString();
    }

    @Override
    public String generatePassword(PasswordRequest request) {
        return generatePassword(
                request.getLength(),
                request.isUseUpper(),
                request.isUseDigits(),
                request.isUseSpecial()
        );
    }

    @Override
    public PasswordHistory savePasswordHistory(String password, PasswordRequest request) {
        PasswordHistory history = new PasswordHistory();
        history.setPassword(password);
        history.setLength(request.getLength());
        history.setUseUpper(request.isUseUpper());
        history.setUseDigits(request.isUseDigits());
        history.setUseSpecial(request.isUseSpecial());
        
        return passwordHistoryRepository.save(history);
    }
    
    @Override
    public PasswordHistory savePasswordHistory(String password, PasswordRequest request, User user) {
        PasswordHistory history = new PasswordHistory();
        history.setPassword(password);
        history.setLength(request.getLength());
        history.setUseUpper(request.isUseUpper());
        history.setUseDigits(request.isUseDigits());
        history.setUseSpecial(request.isUseSpecial());
        history.setUser(user);
        
        return passwordHistoryRepository.save(history);
    }

    @Override
    public List<PasswordHistory> getRecentPasswords() {
        return passwordHistoryRepository.findTop10ByOrderByCreatedAtDesc();
    }
    
    @Override
    public List<PasswordHistory> getPasswordHistoryByUser(User user) {
        return passwordHistoryRepository.findTop10ByUserOrderByCreatedAtDesc(user);
    }
    
    @Override
    public Map<String, Object> evaluatePasswordStrength(String password) {
        Map<String, Object> result = new HashMap<>();
        int score = 0;
        StringBuilder feedback = new StringBuilder();
        
        // 评估长度（满分40分）
        int lengthScore = Math.min(password.length() * 4, 40);
        score += lengthScore;
        
        // 检查字符类型
        boolean hasLower = Pattern.compile("[a-z]").matcher(password).find();
        boolean hasUpper = Pattern.compile("[A-Z]").matcher(password).find();
        boolean hasDigit = Pattern.compile("\\d").matcher(password).find();
        boolean hasSpecial = Pattern.compile("[^a-zA-Z0-9]").matcher(password).find();
        
        // 字符多样性评分（每种类型10分，满分40分）
        int diversityScore = 0;
        if (hasLower) diversityScore += 10;
        if (hasUpper) diversityScore += 10;
        if (hasDigit) diversityScore += 10;
        if (hasSpecial) diversityScore += 10;
        score += diversityScore;
        
        // 额外规则评分（满分20分）
        int extraScore = 0;
        
        // 中间有数字或特殊字符（10分）
        if (password.length() > 2 && (Pattern.compile("[^a-zA-Z].*[^a-zA-Z]").matcher(password.substring(1, password.length() - 1)).find())) {
            extraScore += 10;
        }
        
        // 字符类型交替出现（10分）
        int alterations = 0;
        for (int i = 1; i < password.length(); i++) {
            char prev = password.charAt(i-1);
            char curr = password.charAt(i);
            
            boolean prevIsLetter = Character.isLetter(prev);
            boolean currIsLetter = Character.isLetter(curr);
            
            if (prevIsLetter != currIsLetter) {
                alterations++;
            } else if (prevIsLetter && currIsLetter) {
                boolean prevIsUpper = Character.isUpperCase(prev);
                boolean currIsUpper = Character.isUpperCase(curr);
                if (prevIsUpper != currIsUpper) {
                    alterations++;
                }
            }
        }
        if (alterations >= password.length() / 3) {
            extraScore += 10;
        }
        
        score += extraScore;
        
        // 总分不超过100
        score = Math.min(score, 100);
        
        // 强度级别
        String strength;
        if (score < 40) {
            strength = "弱";
            feedback.append("密码强度较弱，建议增加长度和复杂性。");
        } else if (score < 70) {
            strength = "中等";
            feedback.append("密码强度中等，可以考虑增加特殊字符或大小写混合。");
        } else if (score < 90) {
            strength = "强";
            feedback.append("密码强度良好。");
        } else {
            strength = "非常强";
            feedback.append("密码强度极佳！");
        }
        
        // 具体建议
        if (!hasUpper) feedback.append("\n添加大写字母可以提高密码强度。");
        if (!hasLower) feedback.append("\n添加小写字母可以提高密码强度。");
        if (!hasDigit) feedback.append("\n添加数字可以提高密码强度。");
        if (!hasSpecial) feedback.append("\n添加特殊字符可以提高密码强度。");
        if (password.length() < 12) feedback.append("\n建议密码长度至少为12位。");
        
        result.put("score", score);
        result.put("strength", strength);
        result.put("feedback", feedback.toString());
        
        return result;
    }
}
