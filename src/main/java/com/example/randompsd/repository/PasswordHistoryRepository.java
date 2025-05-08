package com.example.randompsd.repository;

import com.example.randompsd.model.PasswordHistory;
import com.example.randompsd.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PasswordHistoryRepository extends JpaRepository<PasswordHistory, Long> {
    List<PasswordHistory> findTop10ByOrderByCreatedAtDesc();
    List<PasswordHistory> findByUserOrderByCreatedAtDesc(User user);
    List<PasswordHistory> findTop10ByUserOrderByCreatedAtDesc(User user);
}
