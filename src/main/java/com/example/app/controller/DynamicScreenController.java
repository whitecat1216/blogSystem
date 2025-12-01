package com.example.app.controller;

import com.example.app.dto.ScreenDefinition;
import com.example.app.service.DynamicScreenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@RestController
@RequestMapping("/api/screen")
public class DynamicScreenController {

    @Autowired
    private DynamicScreenService screenService;

    @GetMapping("/{screenName}/definition")
    public ResponseEntity<ScreenDefinition> getScreenDefinition(@PathVariable String screenName) {
        try {
            ScreenDefinition definition = screenService.loadScreenDefinition(screenName);
            return ResponseEntity.ok(definition);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/{screenName}/data")
    public ResponseEntity<Map<String, Object>> searchData(
            @PathVariable String screenName,
            @RequestParam(required = false) Map<String, Object> searchParams,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int pageSize) {
        
        try {
            ScreenDefinition definition = screenService.loadScreenDefinition(screenName);
            
            if (searchParams == null) {
                searchParams = new HashMap<>();
            }
            // 検索対象のフィールドのみを抽出（page, pageSize などの非検索パラメータを除外）
            Map<String, Object> filteredParams = new HashMap<>();
            if (definition.getSearchFields() != null) {
                for (ScreenDefinition.SearchField field : definition.getSearchFields()) {
                    String key = field.getKey();
                    if (searchParams.containsKey(key)) {
                        filteredParams.put(key, searchParams.get(key));
                    }
                }
            }
            
            List<Map<String, Object>> records = screenService.searchRecords(
                definition.getTableName(), filteredParams, page, pageSize);
            int total = screenService.countRecords(definition.getTableName(), filteredParams);
            
            Map<String, Object> response = new HashMap<>();
            response.put("records", records);
            response.put("total", total);
            response.put("page", page);
            response.put("pageSize", pageSize);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/{screenName}/data/{id}")
    public ResponseEntity<Map<String, Object>> getRecord(
            @PathVariable String screenName,
            @PathVariable int id) {
        
        try {
            ScreenDefinition definition = screenService.loadScreenDefinition(screenName);
            Map<String, Object> record = screenService.getRecord(definition.getTableName(), id);
            
            if (record == null) {
                return ResponseEntity.notFound().build();
            }
            
            return ResponseEntity.ok(record);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/{screenName}/data")
    public ResponseEntity<Void> createRecord(
            @PathVariable String screenName,
            @RequestBody Map<String, Object> data) {
        try {
            ScreenDefinition definition = screenService.loadScreenDefinition(screenName);
            // sanitize richtext fields (e.g., content)
            sanitizeRichText(definition, data);
            // tagIds (multiselect) handling for blog_post
            List<Integer> tagIds = null;
            if ("blog_post".equals(definition.getTableName()) && data.containsKey("tagIds")) {
                Object raw = data.get("tagIds");
                if (raw instanceof List<?>) {
                    try {
                        tagIds = ((List<?>) raw).stream().map(v -> Integer.valueOf(v.toString())).toList();
                    } catch (Exception ignored) {}
                }
                data.remove("tagIds"); // not a direct column
            }
            int newId = screenService.createRecordReturnId(definition.getTableName(), data);
            if (tagIds != null) {
                screenService.updatePostTags(newId, tagIds);
            }
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Create error: " + e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{screenName}/data/{id}")
    public ResponseEntity<Void> updateRecord(
            @PathVariable String screenName,
            @PathVariable int id,
            @RequestBody Map<String, Object> data) {
        try {
            ScreenDefinition definition = screenService.loadScreenDefinition(screenName);
            sanitizeRichText(definition, data);
            List<Integer> tagIds = null;
            if ("blog_post".equals(definition.getTableName()) && data.containsKey("tagIds")) {
                Object raw = data.get("tagIds");
                if (raw instanceof List<?>) {
                    try {
                        tagIds = ((List<?>) raw).stream().map(v -> Integer.valueOf(v.toString())).toList();
                    } catch (Exception ignored) {}
                }
                data.remove("tagIds");
            }
            screenService.updateRecord(definition.getTableName(), id, data);
            if (tagIds != null) {
                screenService.updatePostTags(id, tagIds);
            }
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    private void sanitizeRichText(ScreenDefinition definition, Map<String,Object> data){
        try {
            if (definition.getFormFields() == null) return;
            for (ScreenDefinition.FormField f : definition.getFormFields()) {
                if ("richtext".equalsIgnoreCase(f.getType())) {
                    Object v = data.get(f.getKey());
                    if (v != null) {
                        // 見出しタグ(h1,h2,h3)も許可した拡張サニタイズ
                        Safelist allowed = Safelist.relaxed().addTags("h1","h2","h3");
                        String cleaned = Jsoup.clean(v.toString(), allowed);
                        data.put(f.getKey(), cleaned);
                    }
                }
            }
        } catch (Exception ignored) {}
    }

    @DeleteMapping("/{screenName}/data/{id}")
    public ResponseEntity<Void> deleteRecord(
            @PathVariable String screenName,
            @PathVariable int id) {
        
        try {
            ScreenDefinition definition = screenService.loadScreenDefinition(screenName);
            screenService.deleteRecord(definition.getTableName(), id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/upload")
    public ResponseEntity<Map<String, Object>> uploadFile(@RequestParam("file") MultipartFile file) {
        try {
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().build();
            }
            String original = file.getOriginalFilename();
            String ext = "";
            if (original != null && original.contains(".")) {
                ext = original.substring(original.lastIndexOf('.'));
            }
            String dateDir = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
            Path uploadRoot = Paths.get("src", "main", "resources", "static", "uploads", dateDir);
            Files.createDirectories(uploadRoot);
            String filename = UUID.randomUUID().toString().replace("-", "") + ext;
            Path target = uploadRoot.resolve(filename);
            file.transferTo(target.toFile());
            String url = "/uploads/" + dateDir + "/" + filename;
            Map<String, Object> resp = new HashMap<>();
            resp.put("url", url);
            return ResponseEntity.ok(resp);
        } catch (IOException e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}
