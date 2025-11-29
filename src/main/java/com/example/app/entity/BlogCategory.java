package com.example.app.entity;

import lombok.Data;
import org.seasar.doma.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "blog_category")
@Data
public class BlogCategory {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    
    private String name;
    
    private String description;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
}
