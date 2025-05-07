package com.example.randompsd.service;

import com.example.randompsd.dto.PasswordRequest;
import com.example.randompsd.model.PasswordHistory;

import java.util.List;

public interface PasswordService {
    String generatePassword(int length, boolean useUpper, boolean useDigits, boolean useSpecial);
    String generatePassword(PasswordRequest request);
    PasswordHistory savePasswordHistory(String password, PasswordRequest request);
    List<PasswordHistory> getRecentPasswords();
}
