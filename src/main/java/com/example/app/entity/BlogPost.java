package com.example.app.entity;

import lombok.Data;
import org.seasar.doma.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "blog_post")
@Data
public class BlogPost {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    
    private String title;
    
    private String content;
    
    @Column(name = "author_id")
    private Integer authorId;
    
    private String status;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
