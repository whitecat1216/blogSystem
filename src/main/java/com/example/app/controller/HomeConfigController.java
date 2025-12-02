package com.example.app.controller;

import com.example.app.service.HomeConfigService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/home/config")
@RequiredArgsConstructor
public class HomeConfigController {
    
    private final HomeConfigService homeConfigService;
    private final ObjectMapper objectMapper;
    
    /**
     * 現在のホーム画面設定を取得
     */
    @GetMapping
    public ResponseEntity<?> getCurrentConfig() {
        try {
            Map<String, Object> config = homeConfigService.getCurrentConfig();
            return ResponseEntity.ok(config);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "設定の取得に失敗しました: " + e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }
    
    /**
     * ホーム画面設定を更新（管理者のみ）
     */
    @PutMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateConfig(@RequestBody Map<String, String> request, Authentication auth) {
        try {
            String configJson = request.get("config_json");
            
            // JSON妥当性チェック
            try {
                JsonNode jsonNode = objectMapper.readTree(configJson);
                // 必須フィールドのチェック
                if (!jsonNode.has("title") || !jsonNode.has("sections")) {
                    Map<String, Object> error = new HashMap<>();
                    error.put("success", false);
                    error.put("message", "JSON形式が不正です。titleとsectionsは必須です。");
                    return ResponseEntity.badRequest().body(error);
                }
            } catch (Exception e) {
                Map<String, Object> error = new HashMap<>();
                error.put("success", false);
                error.put("message", "JSON形式が不正です: " + e.getMessage());
                return ResponseEntity.badRequest().body(error);
            }
            
            String username = auth != null ? auth.getName() : "system";
            Map<String, Object> result = homeConfigService.updateConfig(configJson, username);
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "設定の更新に失敗しました: " + e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }
    
    /**
     * 設定変更履歴を取得（管理者のみ）
     */
    @GetMapping("/history")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getHistory() {
        try {
            Map<String, Object> history = homeConfigService.getHistory();
            return ResponseEntity.ok(history);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "履歴の取得に失敗しました: " + e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }
    
    /**
     * 指定バージョンに復元（管理者のみ）
     */
    @PostMapping("/restore/{version}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> restoreVersion(@PathVariable Integer version, Authentication auth) {
        try {
            String username = auth != null ? auth.getName() : "system";
            Map<String, Object> result = homeConfigService.restoreVersion(version, username);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "復元に失敗しました: " + e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }
}
