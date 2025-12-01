package com.example.app.controller;

import com.example.app.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> register(@RequestBody Map<String, String> request) {
        Map<String, Object> response = new HashMap<>();
        
        String username = request.get("username");
        String password = request.get("password");
        String role = request.getOrDefault("role", "USER");
        
        // Validation
        if (username == null || username.trim().isEmpty()) {
            response.put("success", false);
            response.put("message", "ユーザー名を入力してください");
            return ResponseEntity.badRequest().body(response);
        }
        
        if (password == null || password.length() < 4) {
            response.put("success", false);
            response.put("message", "パスワードは4文字以上で入力してください");
            return ResponseEntity.badRequest().body(response);
        }
        
        // Role validation: USER or ADMIN only
        if (!role.equals("USER") && !role.equals("ADMIN")) {
            role = "USER";
        }
        
        // Check if username already exists
        if (userService.usernameExists(username)) {
            response.put("success", false);
            response.put("message", "このユーザー名は既に使用されています");
            return ResponseEntity.badRequest().body(response);
        }
        
        try {
            userService.registerUser(username, password, role);
            response.put("success", true);
            response.put("message", "ユーザー登録が完了しました");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "登録に失敗しました: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
}
