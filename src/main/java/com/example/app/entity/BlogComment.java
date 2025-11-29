package com.example.app.entity;

import lombok.Data;
import org.seasar.doma.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "blog_comment")
@Data
public class BlogComment {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    
    @Column(name = "post_id")
    private Integer postId;
    
    private String author;
    
    @Column(name = "comment_text")
    private String commentText;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
}
