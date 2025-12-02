package com.example.app.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class UserService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public Map<String, Object> findByUsername(String username) {
        String sql = "SELECT id, username, password, role, enabled FROM blog_user WHERE username = ?";
        List<Map<String, Object>> results = jdbcTemplate.queryForList(sql, username);
        return results.isEmpty() ? null : results.get(0);
    }

    public boolean usernameExists(String username) {
        String sql = "SELECT COUNT(*) FROM blog_user WHERE username = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, username);
        return count != null && count > 0;
    }

    public void registerUser(String username, String rawPassword, String role) {
        // 平文パスワードで保存（ハッシュ化なし）
        String sql = "INSERT INTO blog_user (username, password, role, enabled) VALUES (?, ?, ?, ?)";
        jdbcTemplate.update(sql, username, rawPassword, role, true);
    }
}
