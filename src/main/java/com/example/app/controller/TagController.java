package com.example.app.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/tag")
public class TagController {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @GetMapping("/list")
    public ResponseEntity<List<Map<String,Object>>> list() {
        List<Map<String,Object>> tags = jdbcTemplate.queryForList("SELECT id, name FROM blog_tag ORDER BY name");
        return ResponseEntity.ok(tags);
    }

    @PostMapping
    public ResponseEntity<Map<String,Object>> create(@RequestBody Map<String,Object> body) {
        Object nameObj = body.get("name");
        if (nameObj == null || nameObj.toString().trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        String name = nameObj.toString().trim();
        // check existing
        List<Map<String,Object>> existingRows = jdbcTemplate.queryForList("SELECT id FROM blog_tag WHERE name = ?", name);
        Integer id;
        if (!existingRows.isEmpty()) {
            id = (Integer) existingRows.get(0).get("id");
        } else {
            id = jdbcTemplate.queryForObject("INSERT INTO blog_tag (name) VALUES (?) RETURNING id", Integer.class, name);
        }
        Map<String,Object> resp = new HashMap<>();
        resp.put("id", id);
        resp.put("name", name);
        return ResponseEntity.ok(resp);
    }
}
