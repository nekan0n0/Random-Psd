package com.example.randompsd.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Data
public class PasswordHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String password;
    private int length;
    private boolean useUpper;
    private boolean useDigits;
    private boolean useSpecial;
    private LocalDateTime createdAt;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    @JsonIgnore // 防止序列化时出现循环引用
    private User user;

    @PrePersist
    public void prePersist() {
        createdAt = LocalDateTime.now();
    }
}
