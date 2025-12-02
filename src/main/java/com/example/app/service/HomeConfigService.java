package com.example.app.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class HomeConfigService {
    
    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper;
    
    /**
     * 現在のホーム画面設定を取得
     */
    public Map<String, Object> getCurrentConfig() throws IOException {
        // まずDBから取得を試みる
        try {
            String sql = "SELECT id, config_json, version, updated_at, updated_by FROM home_config ORDER BY id DESC LIMIT 1";
            List<Map<String, Object>> results = jdbcTemplate.queryForList(sql);
            
            if (!results.isEmpty()) {
                return results.get(0);
            }
        } catch (Exception e) {
            log.warn("Failed to fetch config from database, falling back to file: " + e.getMessage());
        }
        
        // DBになければファイルから読み込む
        ClassPathResource resource = new ClassPathResource("screens/home.json");
        String json = new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        
        Map<String, Object> config = new HashMap<>();
        config.put("id", null);
        config.put("config_json", json);
        config.put("version", 1);
        config.put("updated_at", LocalDateTime.now());
        config.put("updated_by", "file");
        
        return config;
    }
    
    /**
     * ホーム画面設定を更新
     */
    @Transactional
    public Map<String, Object> updateConfig(String configJson, String username) throws IOException {
        // テーブルが存在するか確認
        ensureTablesExist();
        
        // 現在の設定を取得
        Map<String, Object> current = getCurrentConfig();
        Integer currentVersion = current.get("version") != null ? (Integer) current.get("version") : 0;
        Integer newVersion = currentVersion + 1;
        
        // 現在の設定を履歴に保存
        if (current.get("id") != null) {
            String historySql = "INSERT INTO home_config_history (config_id, version, config_json, updated_at, updated_by) " +
                              "VALUES (?, ?, ?, ?, ?)";
            jdbcTemplate.update(historySql, 
                current.get("id"),
                current.get("version"),
                current.get("config_json"),
                current.get("updated_at"),
                current.get("updated_by")
            );
        }
        
        // 新しい設定を保存
        if (current.get("id") != null) {
            String updateSql = "UPDATE home_config SET config_json = ?, version = ?, updated_at = CURRENT_TIMESTAMP, updated_by = ? WHERE id = ?";
            jdbcTemplate.update(updateSql, configJson, newVersion, username, current.get("id"));
        } else {
            String insertSql = "INSERT INTO home_config (config_json, version, updated_by) VALUES (?, ?, ?)";
            jdbcTemplate.update(insertSql, configJson, newVersion, username);
        }
        
        // ファイルにも書き込み（オプション）
        try {
            writeToFile(configJson);
        } catch (Exception e) {
            log.warn("Failed to write to file: " + e.getMessage());
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("version", newVersion);
        result.put("message", "ホーム画面設定を更新しました");
        
        return result;
    }
    
    /**
     * 設定変更履歴を取得
     */
    public Map<String, Object> getHistory() {
        ensureTablesExist();
        
        String sql = "SELECT id, version, config_json, updated_at, updated_by FROM home_config_history " +
                    "ORDER BY version DESC LIMIT 50";
        List<Map<String, Object>> records = jdbcTemplate.queryForList(sql);
        
        Map<String, Object> result = new HashMap<>();
        result.put("records", records);
        result.put("total", records.size());
        
        return result;
    }
    
    /**
     * 指定バージョンに復元
     */
    @Transactional
    public Map<String, Object> restoreVersion(Integer version, String username) throws IOException {
        ensureTablesExist();
        
        // 履歴から指定バージョンを取得
        String sql = "SELECT config_json FROM home_config_history WHERE version = ? LIMIT 1";
        List<Map<String, Object>> results = jdbcTemplate.queryForList(sql, version);
        
        if (results.isEmpty()) {
            throw new RuntimeException("指定されたバージョンが見つかりません: " + version);
        }
        
        String configJson = (String) results.get(0).get("config_json");
        
        // 復元
        return updateConfig(configJson, username + " (restored from v" + version + ")");
    }
    
    /**
     * テーブルが存在することを確認
     */
    private void ensureTablesExist() {
        try {
            String createConfigTable = "CREATE TABLE IF NOT EXISTS home_config (" +
                "id SERIAL PRIMARY KEY, " +
                "config_json TEXT NOT NULL, " +
                "version INTEGER NOT NULL DEFAULT 1, " +
                "updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                "updated_by VARCHAR(100))";
            jdbcTemplate.execute(createConfigTable);
            
            String createHistoryTable = "CREATE TABLE IF NOT EXISTS home_config_history (" +
                "id SERIAL PRIMARY KEY, " +
                "config_id INTEGER NOT NULL, " +
                "version INTEGER NOT NULL, " +
                "config_json TEXT NOT NULL, " +
                "updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                "updated_by VARCHAR(100), " +
                "FOREIGN KEY (config_id) REFERENCES home_config(id) ON DELETE CASCADE)";
            jdbcTemplate.execute(createHistoryTable);
        } catch (Exception e) {
            log.error("Failed to create tables: " + e.getMessage());
        }
    }
    
    /**
     * ファイルに書き込み
     */
    private void writeToFile(String json) throws IOException {
        try {
            // 整形されたJSONに変換
            Object jsonObj = objectMapper.readValue(json, Object.class);
            String prettyJson = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(jsonObj);
            
            // ファイルパスを取得
            Path filePath = Paths.get("src/main/resources/screens/home.json");
            
            // ファイルに書き込み
            Files.writeString(filePath, prettyJson, StandardCharsets.UTF_8, 
                StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            
            log.info("Successfully wrote config to file: " + filePath);
        } catch (Exception e) {
            log.error("Failed to write config to file: " + e.getMessage());
            throw e;
        }
    }
}
