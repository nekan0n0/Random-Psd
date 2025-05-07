package com.example.randompsd.service.impl;

import com.example.randompsd.dto.PasswordRequest;
import com.example.randompsd.model.PasswordHistory;
import com.example.randompsd.repository.PasswordHistoryRepository;
import com.example.randompsd.service.PasswordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.List;
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
    public List<PasswordHistory> getRecentPasswords() {
        return passwordHistoryRepository.findTop10ByOrderByCreatedAtDesc();
    }
}
