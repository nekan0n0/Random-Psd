package com.example.randompsd.dto;

import lombok.Data;

@Data
public class PasswordRequest {
    private int length;
    private boolean useUpper;
    private boolean useDigits;
    private boolean useSpecial;
}
