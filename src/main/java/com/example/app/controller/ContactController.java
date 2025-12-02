package com.example.app.controller;

import com.example.app.entity.ContactMessage;
import com.example.app.service.ContactService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/contact")
public class ContactController {

    @Autowired
    private ContactService contactService;

    /**
     * 公開API: お問い合わせ送信（認証不要）
     */
    @PostMapping("/submit")
    public ResponseEntity<Map<String, Object>> submitContact(@RequestBody ContactMessage message) {
        try {
            ContactMessage created = contactService.createContact(message);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "お問い合わせを受け付けました");
            response.put("id", created.getId());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 管理者API: お問い合わせ一覧取得
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<ContactMessage>> getAllContacts() {
        List<ContactMessage> contacts = contactService.getAllContacts();
        return ResponseEntity.ok(contacts);
    }

    /**
     * 管理者API: お問い合わせ詳細取得
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ContactMessage> getContactById(@PathVariable Integer id) {
        ContactMessage contact = contactService.getContactById(id);
        if (contact != null) {
            return ResponseEntity.ok(contact);
        }
        return ResponseEntity.notFound().build();
    }

    /**
     * 管理者API: お問い合わせ更新（ステータス変更など）
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ContactMessage> updateContact(
            @PathVariable Integer id,
            @RequestBody ContactMessage message) {
        message.setId(id);
        ContactMessage updated = contactService.updateContact(message);
        if (updated != null) {
            return ResponseEntity.ok(updated);
        }
        return ResponseEntity.notFound().build();
    }

    /**
     * 管理者API: お問い合わせに返信（メール送信 + ステータス更新）
     */
    @PostMapping("/{id}/reply")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> replyToContact(
            @PathVariable Integer id,
            @RequestBody Map<String, String> payload) {
        try {
            String replyText = payload.get("reply");
            String username = payload.get("repliedBy");
            
            if (replyText == null || replyText.trim().isEmpty()) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("error", "返信内容が空です");
                return ResponseEntity.badRequest().body(response);
            }
            
            ContactMessage updated = contactService.replyToContact(id, replyText, username);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "返信を送信しました");
            response.put("contact", updated);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 管理者API: お問い合わせ削除
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> deleteContact(@PathVariable Integer id) {
        boolean deleted = contactService.deleteContact(id);
        
        Map<String, Object> response = new HashMap<>();
        if (deleted) {
            response.put("success", true);
            response.put("message", "お問い合わせを削除しました");
            return ResponseEntity.ok(response);
        } else {
            response.put("success", false);
            response.put("error", "お問い合わせが見つかりません");
            return ResponseEntity.notFound().build();
        }
    }
}
