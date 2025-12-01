package com.example.app.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/category")
public class CategoryController {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @GetMapping("/list")
    public ResponseEntity<List<Map<String,Object>>> getAllCategories() {
        List<Map<String,Object>> categories = jdbcTemplate.queryForList("SELECT id, name FROM blog_category ORDER BY name");
        return ResponseEntity.ok(categories);
    }

    /**
     * カテゴリを取得または作成する
     * @param request { "name": "カテゴリ名" }
     * @return { "id": 123 }
     */
    @PostMapping("/ensure")
    public ResponseEntity<Map<String,Object>> ensureCategory(@RequestBody Map<String,String> request) {
        String name = request.get("name");
        List<Map<String,Object>> existingRows = jdbcTemplate.queryForList("SELECT id FROM blog_category WHERE name = ?", name);
        int id;
        if (!existingRows.isEmpty()) {
            id = (Integer) existingRows.get(0).get("id");
        } else {
            id = jdbcTemplate.queryForObject("INSERT INTO blog_category (name) VALUES (?) RETURNING id", Integer.class, name);
        }
        Map<String,Object> result = new HashMap<>();
        result.put("id", id);
        return ResponseEntity.ok(result);
    }
}
