package com.example.app.controller;

import com.example.app.dto.ScreenDefinition;
import com.example.app.service.DynamicScreenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
            screenService.createRecord(definition.getTableName(), data);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
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
            screenService.updateRecord(definition.getTableName(), id, data);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
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
}
