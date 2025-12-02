package com.example.app.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ContactMessage {
    private Integer id;
    private String name;
    private String email;
    private String subject;
    private String message;
    private String status; // new, replied, archived
    private String reply;
    private LocalDateTime repliedAt;
    private String repliedBy;
    private LocalDateTime createdAt;
}
