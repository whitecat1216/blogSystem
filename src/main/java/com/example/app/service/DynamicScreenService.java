package com.example.app.service;

import com.example.app.dto.ScreenDefinition;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Service
public class DynamicScreenService {

    @Autowired
    private ResourceLoader resourceLoader;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public ScreenDefinition loadScreenDefinition(String screenName) throws IOException {
        try {
            String path = "classpath:screens/" + screenName + ".json";
            System.out.println("Loading screen definition from: " + path);
            Resource resource = resourceLoader.getResource(path);
            
            if (!resource.exists()) {
                System.err.println("Screen definition file not found: " + path);
                throw new IOException("Screen definition file not found: " + screenName);
            }
            
            System.out.println("Resource exists: " + resource.exists() + ", readable: " + resource.isReadable());
            ScreenDefinition definition = objectMapper.readValue(resource.getInputStream(), ScreenDefinition.class);
            System.out.println("Successfully loaded screen definition: " + definition.getTitle());
            return definition;
        } catch (Exception e) {
            System.err.println("Error loading screen definition for: " + screenName);
            e.printStackTrace();
            throw e;
        }
    }

    public List<Map<String, Object>> searchRecords(String tableName, Map<String, Object> searchParams, 
                                                     int page, int pageSize) {
        try {
            StringBuilder sql = new StringBuilder("SELECT * FROM " + tableName + " WHERE 1=1");
            
            // 検索条件の追加
            for (Map.Entry<String, Object> entry : searchParams.entrySet()) {
                if (entry.getValue() != null && !entry.getValue().toString().isEmpty()) {
                    sql.append(" AND ").append(entry.getKey())
                       .append(" LIKE '%").append(entry.getValue()).append("%'");
                }
            }
            
            // ページング
            int offset = page * pageSize;
            sql.append(" LIMIT ").append(pageSize).append(" OFFSET ").append(offset);
            
            return jdbcTemplate.queryForList(sql.toString());
        } catch (Exception e) {
            System.err.println("Error searching records in table " + tableName + ": " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to search records: " + e.getMessage(), e);
        }
    }

    public int countRecords(String tableName, Map<String, Object> searchParams) {
        try {
            StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM " + tableName + " WHERE 1=1");
            
            for (Map.Entry<String, Object> entry : searchParams.entrySet()) {
                if (entry.getValue() != null && !entry.getValue().toString().isEmpty()) {
                    sql.append(" AND ").append(entry.getKey())
                       .append(" LIKE '%").append(entry.getValue()).append("%'");
                }
            }
            
            Integer count = jdbcTemplate.queryForObject(sql.toString(), Integer.class);
            return count != null ? count : 0;
        } catch (Exception e) {
            System.err.println("Error counting records in table " + tableName + ": " + e.getMessage());
            e.printStackTrace();
            return 0;
        }
    }

    public Map<String, Object> getRecord(String tableName, int id) {
        String sql = "SELECT * FROM " + tableName + " WHERE id = ?";
        List<Map<String, Object>> results = jdbcTemplate.queryForList(sql, id);
        return results.isEmpty() ? null : results.get(0);
    }

    public void createRecord(String tableName, Map<String, Object> data) {
        StringBuilder columns = new StringBuilder();
        StringBuilder values = new StringBuilder();
        
        for (String key : data.keySet()) {
            if (columns.length() > 0) {
                columns.append(", ");
                values.append(", ");
            }
            columns.append(key);
            values.append("?");
        }
        
        String sql = "INSERT INTO " + tableName + " (" + columns + ") VALUES (" + values + ")";
        jdbcTemplate.update(sql, data.values().toArray());
    }

    public void updateRecord(String tableName, int id, Map<String, Object> data) {
        StringBuilder set = new StringBuilder();
        
        for (String key : data.keySet()) {
            if (set.length() > 0) {
                set.append(", ");
            }
            set.append(key).append(" = ?");
        }
        
        String sql = "UPDATE " + tableName + " SET " + set + " WHERE id = ?";
        Object[] params = new Object[data.size() + 1];
        int i = 0;
        for (Object value : data.values()) {
            params[i++] = value;
        }
        params[i] = id;
        
        jdbcTemplate.update(sql, params);
    }

    public void deleteRecord(String tableName, int id) {
        String sql = "DELETE FROM " + tableName + " WHERE id = ?";
        jdbcTemplate.update(sql, id);
    }
}
