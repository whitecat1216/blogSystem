package com.example.app.entity;

import org.seasar.doma.*;

@Entity
@Table(name = "blog_user")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Integer id;
    
    public String username;
    public String password;
    public String role;
    public Boolean enabled;
}
